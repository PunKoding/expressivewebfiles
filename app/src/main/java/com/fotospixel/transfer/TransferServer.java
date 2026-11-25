package com.fotospixel.transfer;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TransferServer {

    private static final String TAG = "FotosPixel";
    public static final String DEFAULT_USER = "pixel";
    public static final String DEFAULT_PASS = "pixel";

    private static final int IO_BUFFER_SIZE = 524288;      // 512 KB coalesced FUSE writes
    private static final int STREAM_BUF_SIZE = 262144;     // 256 KB buffered socket stream
    private static final String SESSION_COOKIE = "fp_session";
    private static final long SESSION_TTL_MS = 24L * 60L * 60L * 1000L;

    private static final class ChunkedUploadSession {
        final String txId;
        final File tempFile;
        final File targetFile;
        final long totalBytes;
        final RandomAccessFile raf;
        final FileChannel channel;
        final TransferStats.ActiveTransfer tx;
        final AtomicBoolean finalized = new AtomicBoolean(false);

        ChunkedUploadSession(String txId, File tempFile, File targetFile, long totalBytes,
                             RandomAccessFile raf, FileChannel channel, TransferStats.ActiveTransfer tx) {
            this.txId = txId;
            this.tempFile = tempFile;
            this.targetFile = targetFile;
            this.totalBytes = totalBytes;
            this.raf = raf;
            this.channel = channel;
            this.tx = tx;
        }

        void closeQuietly() {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
            try {
                raf.close();
            } catch (IOException ignored) {
            }
        }

        void closeAndDeleteTemp() {
            if (finalized.compareAndSet(false, true)) {
                closeQuietly();
                tempFile.delete();
            }
        }
    }

    private final Context context;
    private final int port;
    private final String authUser;
    private final String authPass;
    private final File rootDir;
    private final String rootDirPath;
    private final Object dirLock = new Object();
    private final TransferStats stats = TransferStats.getInstance();
    private final ConcurrentHashMap<String, Long> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChunkedUploadSession> chunkSessions = new ConcurrentHashMap<>();

    private volatile ServerSocket serverSocket;
    private volatile boolean running = false;
    private ExecutorService pool;
    private ExecutorService mediaScannerPool;
    private Thread acceptThread;

    public TransferServer(Context context, int port) throws IOException {
        this(context, port, DEFAULT_USER, DEFAULT_PASS);
    }

    public TransferServer(Context context, int port, String authUser, String authPass) throws IOException {
        this.context = context.getApplicationContext();
        this.port = port;
        this.authUser = (authUser == null) ? "" : authUser;
        this.authPass = (authPass == null) ? "" : authPass;
        this.rootDir = Environment.getExternalStorageDirectory().getCanonicalFile();
        this.rootDirPath = this.rootDir.getPath();
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        ServerSocket ss = new ServerSocket();
        ss.setReuseAddress(true);
        // Leave kernel TCP receive/send window auto-tuning free up to tcp_rmem max (8 MB).
        ss.bind(new InetSocketAddress(port), 128);
        this.serverSocket = ss;
        this.running = true;
        this.pool = Executors.newFixedThreadPool(24);
        this.mediaScannerPool = Executors.newSingleThreadExecutor();

        this.acceptThread = new Thread(() -> {
            Log.i(TAG, "TransferServer listening on port " + port + " root=" + rootDirPath
                    + " auth=" + (authUser.isEmpty() ? "open" : authUser));
            while (running) {
                try {
                    Socket client = ss.accept();
                    pool.execute(() -> handleClient(client));
                } catch (IOException e) {
                    if (running) {
                        Log.w(TAG, "Accept error: " + e.getMessage());
                    }
                }
            }
        }, "FotosPixel-Accept");
        this.acceptThread.start();
    }

    public synchronized void stop() {
        running = false;
        stats.cancelAllTransfers();
        for (ChunkedUploadSession s : chunkSessions.values()) {
            s.closeAndDeleteTemp();
        }
        chunkSessions.clear();
        sessions.clear();
        ServerSocket ss = this.serverSocket;
        this.serverSocket = null;
        if (ss != null) {
            try {
                ss.close();
            } catch (IOException ignored) {
            }
        }
        if (pool != null) {
            pool.shutdownNow();
            pool = null;
        }
        if (mediaScannerPool != null) {
            mediaScannerPool.shutdownNow();
            mediaScannerPool = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void handleClient(Socket socket) {
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(60000);

            BufferedInputStream in = new BufferedInputStream(socket.getInputStream(), STREAM_BUF_SIZE);
            OutputStream out = new BufferedOutputStream(socket.getOutputStream(), STREAM_BUF_SIZE);

            int requestsOnConn = 0;
            while (running && !socket.isClosed() && requestsOnConn < 400) {
                String requestLine = readAsciiLine(in, 16384);
                if (requestLine == null) {
                    break;
                }
                if (requestLine.isEmpty()) {
                    continue;
                }
                requestsOnConn++;

                String[] parts = requestLine.split(" ");
                if (parts.length < 2) {
                    sendSimple(out, 400, "Bad Request", "Invalid request line", false);
                    break;
                }

                String method = parts[0].toUpperCase(Locale.US);
                String target = parts[1];
                if (target.startsWith("http://") || target.startsWith("https://")) {
                    int schemeEnd = target.indexOf("://") + 3;
                    int slashIdx = target.indexOf('/', schemeEnd);
                    target = (slashIdx >= 0) ? target.substring(slashIdx) : "/";
                }

                Map<String, String> headers = new HashMap<>();
                int headerCount = 0;
                while (headerCount < 128) {
                    String line = readAsciiLine(in, 16384);
                    if (line == null || line.isEmpty()) {
                        break;
                    }
                    int colon = line.indexOf(':');
                    if (colon > 0) {
                        String k = line.substring(0, colon).trim().toLowerCase(Locale.US);
                        String v = line.substring(colon + 1).trim();
                        if ("cookie".equals(k) && headers.containsKey(k)) {
                            headers.put(k, headers.get(k) + "; " + v);
                        } else {
                            headers.put(k, v);
                        }
                    }
                    headerCount++;
                }

                String rawPath = target;
                String rawQuery = "";
                int qIdx = target.indexOf('?');
                if (qIdx >= 0) {
                    rawPath = target.substring(0, qIdx);
                    rawQuery = target.substring(qIdx + 1);
                }

                boolean wantKeepAlive = !"close".equalsIgnoreCase(headers.get("connection"));
                Map<String, String> query = parseQuery(rawQuery);
                boolean keepOpen = handleWebApi(socket, method, rawPath, query, headers, in, out, wantKeepAlive);
                out.flush();
                if (!keepOpen) {
                    break;
                }
            }
        } catch (SecurityException se) {
            Log.w(TAG, "Security block: " + se.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Client connection ended: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Server error", e);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean handleWebApi(Socket socket, String method, String rawPath, Map<String, String> query,
                                 Map<String, String> headers, BufferedInputStream in, OutputStream out,
                                 boolean keepAlive) throws IOException {
        if ("OPTIONS".equals(method)) {
            sendCorsOptions(out, keepAlive);
            return keepAlive;
        }

        if ("GET".equals(method) && ("/".equals(rawPath) || "/index.html".equals(rawPath))) {
            byte[] htmlBytes = WebUi.HTML.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "text/html; charset=utf-8", htmlBytes.length, null, keepAlive);
            out.write(htmlBytes);
            return keepAlive;
        }

        if ("GET".equals(method) && "/api/auth".equals(rawPath)) {
            boolean authEnabled = !authUser.isEmpty();
            boolean authed = isAuthorized(headers);
            String themeJson = ThemeTokens.read(context).toJson();
            String json = "{\"authEnabled\":" + authEnabled
                    + ",\"authenticated\":" + authed
                    + ",\"user\":\"" + TransferStats.escapeJson(authUser) + "\""
                    + ",\"theme\":" + themeJson + "}";
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("POST".equals(method) && "/api/login".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            String user = query.getOrDefault("user", "");
            String pass = query.getOrDefault("pass", "");
            if (bodyBytes.length > 0) {
                try {
                    JSONObject obj = new JSONObject(new String(bodyBytes, StandardCharsets.UTF_8));
                    if (obj.has("user")) user = obj.optString("user", "");
                    if (obj.has("pass")) pass = obj.optString("pass", "");
                } catch (Exception ignored) {
                }
            }
            if (authUser.isEmpty() || (authUser.equals(user) && authPass.equals(pass))) {
                String token = UUID.randomUUID().toString().replace("-", "");
                sessions.put(token, System.currentTimeMillis());
                Map<String, String> extra = noCacheHeaders();
                extra.put("Set-Cookie", SESSION_COOKIE + "=" + token
                        + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400");
                byte[] data = ("{\"ok\":true,\"user\":\"" + TransferStats.escapeJson(authUser) + "\"}")
                        .getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, extra, keepAlive);
                out.write(data);
                return keepAlive;
            } else {
                sendJsonError(out, 401, "Unauthorized", "Usuario o contraseña incorrectos", keepAlive);
                return keepAlive;
            }
        }

        if ("POST".equals(method) && "/api/logout".equals(rawPath)) {
            consumeSmallBody(in, headers, out);
            String tok = extractSessionCookie(headers);
            if (tok != null) {
                sessions.remove(tok);
            }
            Map<String, String> extra = noCacheHeaders();
            extra.put("Set-Cookie", SESSION_COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
            byte[] data = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, extra, keepAlive);
            out.write(data);
            return keepAlive;
        }

        if (rawPath.startsWith("/api/") && !isAuthorized(headers)) {
            if (!"POST".equals(method) && !"PUT".equals(method)) {
                sendJsonError(out, 401, "Unauthorized", "Sesión requerida", keepAlive);
                return keepAlive;
            }
            drainSmallBodyWithoutContinue(in, headers);
            sendJsonError(out, 401, "Unauthorized", "Sesión requerida", false);
            return false;
        }

        if ("GET".equals(method) && "/api/status".equals(rawPath)) {
            long[] fs = getStorageStats();
            String model = Build.MANUFACTURER + " " + Build.MODEL;
            String baseJson = stats.toJson(stats.getServerIp(), port, fs[0], fs[1], model);
            String themeJson = ThemeTokens.read(context).toJson();
            String json = baseJson.substring(0, baseJson.length() - 1)
                    + ",\"theme\":" + themeJson + "}";
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("GET".equals(method) && "/api/volumes".equals(rawPath)) {
            List<StorageVolumes.VolumeInfo> vols = StorageVolumes.listMounted(context);
            StringBuilder sb = new StringBuilder(512);
            sb.append("{\"ok\":true,\"volumes\":[");
            for (int i = 0; i < vols.size(); i++) {
                if (i > 0) sb.append(',');
                StorageVolumes.VolumeInfo v = vols.get(i);
                String vPath = v.id.isEmpty() ? "" : ("@ext/" + v.id);
                sb.append("{\"id\":\"").append(TransferStats.escapeJson(v.id)).append('"')
                        .append(",\"label\":\"").append(TransferStats.escapeJson(v.label)).append('"')
                        .append(",\"kind\":\"").append(TransferStats.escapeJson(v.kind)).append('"')
                        .append(",\"path\":\"").append(TransferStats.escapeJson(vPath)).append('"')
                        .append(",\"totalBytes\":").append(v.totalBytes)
                        .append(",\"freeBytes\":").append(v.freeBytes)
                        .append(",\"writable\":").append(v.writable)
                        .append('}');
            }
            sb.append("]}");
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("GET".equals(method) && "/api/analyze".equals(rawPath)) {
            String dirParam = query.getOrDefault("dir", "");
            int depth = 3;
            try {
                depth = Math.max(1, Math.min(5, Integer.parseInt(query.getOrDefault("depth", "3"))));
            } catch (Exception ignored) {
            }
            File dir;
            try {
                dir = resolveSafePath(dirParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!dir.exists() || !dir.isDirectory()) {
                sendJsonError(out, 404, "Not Found", "Carpeta no encontrada", keepAlive);
                return keepAlive;
            }
            StorageVolumes.VolumeInfo vol = StorageVolumes.findForFile(context, dir);
            String volLabel = vol != null ? vol.label : "Almacenamiento interno";
            long totalBytes = vol != null ? vol.totalBytes : 0L;
            long freeBytes = vol != null ? vol.freeBytes : 0L;
            if (totalBytes <= 0L) {
                try {
                    StatFs sf = new StatFs(dir.getAbsolutePath());
                    totalBytes = sf.getTotalBytes();
                    freeBytes = sf.getAvailableBytes();
                } catch (Exception ignored) {
                }
            }
            StorageAnalyzer.Node rootNode = StorageAnalyzer.scanTree(
                    dir,
                    new java.util.concurrent.atomic.AtomicBoolean(false),
                    null);
            String rootJson = StorageAnalyzer.toJson(rootNode, depth, 60, new StorageAnalyzer.PathMapper() {
                @Override
                public String mapPath(File file) {
                    return relPathOf(file);
                }
            });
            String dirRel = relPathOf(dir);
            String json = "{\"ok\":true"
                    + ",\"dir\":\"" + TransferStats.escapeJson(dirRel) + "\""
                    + ",\"volumeLabel\":\"" + TransferStats.escapeJson(volLabel) + "\""
                    + ",\"totalBytes\":" + totalBytes
                    + ",\"freeBytes\":" + freeBytes
                    + ",\"root\":" + rootJson + "}";
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("POST".equals(method) && "/api/cancel".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            String id = query.getOrDefault("id", "");
            if (id.isEmpty() && bodyBytes.length > 0) {
                try {
                    id = new JSONObject(new String(bodyBytes, StandardCharsets.UTF_8)).optString("id", "");
                } catch (Exception ignored) {
                }
            }
            boolean ok;
            if (id.isEmpty() || "all".equalsIgnoreCase(id)) {
                stats.cancelAllTransfers();
                ok = true;
            } else {
                ok = stats.cancelTransfer(id);
            }
            byte[] data = ("{\"ok\":" + ok + "}").getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("GET".equals(method) && "/api/list".equals(rawPath)) {
            String dirParam = query.getOrDefault("dir", "");
            File dir;
            try {
                dir = resolveSafePath(dirParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!dir.exists() || !dir.isDirectory()) {
                sendJsonError(out, 404, "Not Found", "Carpeta no encontrada", keepAlive);
                return keepAlive;
            }
            File[] children = dir.listFiles();
            if (children == null) {
                children = new File[0];
            }
            Arrays.sort(children, new Comparator<File>() {
                @Override
                public int compare(File a, File b) {
                    if (a.isDirectory() != b.isDirectory()) {
                        return a.isDirectory() ? -1 : 1;
                    }
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });

            List<File> list = new ArrayList<>();
            for (File f : children) {
                if (!FolderZipper.shouldSkipName(f.getName())) {
                    list.add(f);
                }
            }
            StringBuilder sb = new StringBuilder(2048);
            sb.append("{\"dir\":\"").append(TransferStats.escapeJson(relPathOf(dir))).append("\",\"entries\":");
            appendEntriesJson(sb, list, false);
            sb.append('}');
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("POST".equals(method) && "/api/mkdir".equals(rawPath)) {
            consumeSmallBody(in, headers, out);
            String dirParam = query.getOrDefault("dir", "");
            File dir;
            try {
                dir = resolveSafePath(dirParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Ruta inválida", keepAlive);
                return keepAlive;
            }
            if (!ensureDir(dir)) {
                sendJsonError(out, 500, "Internal Server Error", "No se pudo crear la carpeta", keepAlive);
                return keepAlive;
            }
            byte[] data = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, null, keepAlive);
            out.write(data);
            return keepAlive;
        }

        if (("POST".equals(method) || "PUT".equals(method)) && "/api/upload".equals(rawPath)) {
            String dirParam = query.getOrDefault("dir", "DCIM/Camera");
            String nameParam = query.getOrDefault("name", "upload_" + System.currentTimeMillis());
            String cleanName = sanitizeFileName(nameParam);
            File targetDir;
            File targetFile;
            try {
                targetDir = resolveSafePath(dirParam);
                if (!ensureDir(targetDir)) {
                    sendJsonError(out, 500, "Internal Server Error", "No se pudo crear la carpeta de destino", false);
                    return false;
                }
                targetFile = resolveSafeChild(targetDir, cleanName);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Ruta no permitida", false);
                return false;
            }

            File tempFile = new File(targetDir, ".fp_part_" + UUID.randomUUID().toString() + ".tmp");
            long expectedLen = parseContentLength(headers);
            String folderDisplay = "/" + relPathOf(targetDir);
            TransferStats.ActiveTransfer tx = stats.beginTransfer(
                    cleanName, folderDisplay, TransferStats.DIR_UPLOAD, "Web", expectedLen);
            tx.attachCloseable(socket);
            tx.setCancelCleanup(() -> tempFile.delete());
            boolean promoted = false;
            long written = 0;
            try {
                written = streamBodyToTempFile(in, headers, out, tempFile, tx);
                if (tx.cancelled.get() || (expectedLen >= 0 && written != expectedLen)) {
                    tempFile.delete();
                    return false;
                }
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                if (!tempFile.renameTo(targetFile)) {
                    sendJsonError(out, 500, "Internal Server Error", "Error al guardar el archivo final", false);
                    return false;
                }
                promoted = true;
                String resp = "{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(targetFile))
                        + "\",\"bytes\":" + written + "}";
                byte[] data = resp.getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, null, keepAlive);
                out.write(data);
                out.flush();
                notifyMediaScannerAsync(targetFile);
                return keepAlive;
            } finally {
                tx.detachCloseable(socket);
                if (!promoted) {
                    tempFile.delete();
                }
                stats.endTransfer(tx, promoted);
            }
        }

        if ("POST".equals(method) && "/api/upload_init".equals(rawPath)) {
            consumeSmallBody(in, headers, out);
            String dirParam = query.getOrDefault("dir", "DCIM/Camera");
            String nameParam = query.getOrDefault("name", "upload_" + System.currentTimeMillis());
            long totalBytes = -1L;
            try {
                totalBytes = Long.parseLong(query.getOrDefault("total", "-1"));
            } catch (NumberFormatException ignored) {
            }
            String cleanName = sanitizeFileName(nameParam);
            File targetDir;
            File targetFile;
            try {
                targetDir = resolveSafePath(dirParam);
                if (!ensureDir(targetDir)) {
                    sendJsonError(out, 500, "Internal Server Error", "No se pudo crear la carpeta", keepAlive);
                    return keepAlive;
                }
                targetFile = resolveSafeChild(targetDir, cleanName);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Ruta no permitida", keepAlive);
                return keepAlive;
            }

            File tempFile = new File(targetDir, ".fp_part_" + UUID.randomUUID().toString() + ".tmp");
            RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
            if (totalBytes > 0) {
                raf.setLength(totalBytes);
            }
            FileChannel ch = raf.getChannel();
            String folderDisplay = "/" + relPathOf(targetDir);
            TransferStats.ActiveTransfer tx = stats.beginTransfer(
                    cleanName, folderDisplay, TransferStats.DIR_UPLOAD, "Web", totalBytes);
            ChunkedUploadSession session = new ChunkedUploadSession(
                    tx.id, tempFile, targetFile, totalBytes, raf, ch, tx);
            chunkSessions.put(tx.id, session);
            tx.attachCloseable(ch);
            tx.setCancelCleanup(() -> {
                chunkSessions.remove(tx.id);
                session.closeAndDeleteTemp();
            });

            byte[] data = ("{\"ok\":true,\"id\":\"" + TransferStats.escapeJson(tx.id) + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if (("POST".equals(method) || "PUT".equals(method)) && "/api/upload_chunk".equals(rawPath)) {
            String txId = query.getOrDefault("id", "");
            long offset = 0L;
            try {
                offset = Long.parseLong(query.getOrDefault("offset", "0"));
            } catch (NumberFormatException ignored) {
            }
            ChunkedUploadSession session = chunkSessions.get(txId);
            if (session == null || session.tx.cancelled.get() || session.finalized.get()) {
                drainSmallBodyWithoutContinue(in, headers);
                sendJsonError(out, 409, "Conflict", "Transferencia cancelada o no encontrada", false);
                return false;
            }

            TransferStats.ActiveTransfer tx = session.tx;
            tx.attachCloseable(socket);
            try {
                long written = streamBodyToChannel(in, headers, out, session.channel, offset, tx);
                if (tx.cancelled.get()) {
                    cleanupChunkSession(txId);
                    return false;
                }
                byte[] data = ("{\"ok\":true,\"bytes\":" + written + "}").getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (IOException e) {
                cleanupChunkSession(txId);
                throw e;
            } finally {
                tx.detachCloseable(socket);
                if (tx.cancelled.get()) {
                    chunkSessions.remove(txId);
                    session.closeAndDeleteTemp();
                    stats.endTransfer(tx, false);
                }
            }
        }

        if ("POST".equals(method) && "/api/upload_finish".equals(rawPath)) {
            consumeSmallBody(in, headers, out);
            String txId = query.getOrDefault("id", "");
            ChunkedUploadSession session = chunkSessions.remove(txId);
            if (session == null) {
                sendJsonError(out, 404, "Not Found", "Sesión no encontrada", keepAlive);
                return keepAlive;
            }
            TransferStats.ActiveTransfer tx = session.tx;
            long gotBytes = tx.bytesTransferred.get();
            if (tx.cancelled.get() || (session.totalBytes >= 0 && gotBytes < session.totalBytes)) {
                session.closeAndDeleteTemp();
                stats.endTransfer(tx, false);
                sendJsonError(out, 409, "Conflict", "Transferencia incompleta o cancelada", keepAlive);
                return keepAlive;
            }
            if (!session.finalized.compareAndSet(false, true)) {
                sendJsonError(out, 409, "Conflict", "Sesión ya finalizada", keepAlive);
                return keepAlive;
            }
            session.closeQuietly();
            if (session.targetFile.exists()) {
                session.targetFile.delete();
            }
            if (!session.tempFile.renameTo(session.targetFile)) {
                session.tempFile.delete();
                stats.endTransfer(tx, false);
                sendJsonError(out, 500, "Internal Server Error", "No se pudo mover el archivo", keepAlive);
                return keepAlive;
            }
            stats.endTransfer(tx, true);
            notifyMediaScannerAsync(session.targetFile);
            String resp = "{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(session.targetFile))
                    + "\",\"bytes\":" + gotBytes + "}";
            byte[] data = resp.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if (("GET".equals(method) || "HEAD".equals(method)) && "/api/download".equals(rawPath)) {
            String pathParam = query.getOrDefault("path", "");
            boolean inline = "1".equals(query.get("inline"));
            File file;
            try {
                file = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendSimple(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!file.exists() || !file.isFile()) {
                sendSimple(out, 404, "Not Found", "Archivo no encontrado", keepAlive);
                return keepAlive;
            }
            return serveFile(socket, method, file, headers, out, !inline, keepAlive);
        }

        if ("GET".equals(method) && "/api/zip".equals(rawPath)) {
            String pathParam = query.getOrDefault("path", "");
            String format = query.getOrDefault("format", "zip").toLowerCase(Locale.US);
            boolean isTarGz = "targz".equals(format) || "tar.gz".equals(format) || "tgz".equals(format);
            List<File> items = new ArrayList<>();
            try {
                if (!pathParam.isEmpty()) {
                    File t = resolveSafePath(pathParam);
                    if (isProtectedVolumeRoot(t)) {
                        sendSimple(out, 400, "Bad Request", "Selecciona una subcarpeta para comprimir", keepAlive);
                        return keepAlive;
                    }
                    if (!t.exists()) {
                        sendSimple(out, 404, "Not Found", "Elemento no encontrado", keepAlive);
                        return keepAlive;
                    }
                    items.add(t);
                } else {
                    sendSimple(out, 400, "Bad Request", "Falta la ruta", keepAlive);
                    return keepAlive;
                }
            } catch (SecurityException e) {
                sendSimple(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }

            File primary = items.get(0);
            String ext = isTarGz ? ".tar.gz" : ".zip";
            String archiveName = sanitizeFileName(primary.getName()) + ext;
            String safeAscii = archiveName.replace('"', '_');
            File parentDir = primary.getParentFile();
            String folderDisplay = "/" + relPathOf(parentDir != null ? parentDir : rootDir);
            TransferStats.ActiveTransfer tx = stats.beginTransfer(
                    archiveName, folderDisplay, TransferStats.DIR_DOWNLOAD, isTarGz ? "Web TAR.GZ" : "Web ZIP", -1L);
            tx.attachCloseable(socket);
            boolean ok = false;
            try {
                Map<String, String> extra = noCacheHeaders();
                extra.put("Content-Disposition", "attachment; filename=\"" + safeAscii + "\"");
                sendHeaders(out, 200, "OK", isTarGz ? "application/gzip" : "application/zip", -1L, extra, false);
                if (isTarGz) {
                    FolderZipper.packTarGzToStream(items, out, tx.cancelled, (delta, entry) -> stats.recordBytes(tx, (int) delta));
                } else {
                    FolderZipper.zipItemsToStream(items, out, tx.cancelled, (delta, entry) -> stats.recordBytes(tx, (int) delta));
                }
                out.flush();
                ok = !tx.cancelled.get();
            } catch (IOException e) {
                Log.d(TAG, "Archive stream ended: " + e.getMessage());
            } finally {
                tx.detachCloseable(socket);
                stats.endTransfer(tx, ok);
            }
            return false;
        }

        if ("POST".equals(method) && "/api/zip_create".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            JSONObject bodyObj = parseJsonQuietly(bodyBytes);
            String format = query.getOrDefault("format", bodyObj != null ? bodyObj.optString("format", "zip") : "zip")
                    .toLowerCase(Locale.US);
            boolean isTarGz = "targz".equals(format) || "tar.gz".equals(format) || "tgz".equals(format);
            String ext = isTarGz ? ".tar.gz" : ".zip";
            String customName = query.getOrDefault("name", bodyObj != null ? bodyObj.optString("name", "") : "").trim();

            List<File> items;
            try {
                items = resolveTargetFiles(query, bodyObj);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (items.isEmpty()) {
                sendJsonError(out, 400, "Bad Request", "Selecciona al menos un archivo o carpeta", keepAlive);
                return keepAlive;
            }

            File firstItem = items.get(0);
            File parentDir = firstItem.getParentFile();
            if (parentDir == null) parentDir = rootDir;
            String baseName = !customName.isEmpty()
                    ? sanitizeFileName(customName)
                    : (items.size() == 1 ? firstItem.getName() : "seleccion_" + items.size());
            File destArchive = FolderZipper.resolveUniqueArchiveFile(parentDir, baseName, ext);
            String folderDisplay = "/" + relPathOf(parentDir);
            TransferStats.ActiveTransfer tx = stats.beginTransfer(
                    destArchive.getName(), folderDisplay, TransferStats.DIR_DOWNLOAD, isTarGz ? "TAR.GZ" : "ZIP", -1L);
            boolean ok = false;
            try {
                FolderZipper.ZipResult res = isTarGz
                        ? FolderZipper.packTarGzToFile(items, destArchive, tx.cancelled, (delta, entry) -> stats.recordBytes(tx, (int) delta))
                        : FolderZipper.zipItemsToFile(items, destArchive, tx.cancelled, (delta, entry) -> stats.recordBytes(tx, (int) delta));
                ok = !tx.cancelled.get();
                if (ok) {
                    notifyMediaScannerAsync(destArchive);
                    String resp = "{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(destArchive))
                            + "\",\"name\":\"" + TransferStats.escapeJson(destArchive.getName())
                            + "\",\"bytes\":" + destArchive.length()
                            + ",\"files\":" + res.filesCount + "}";
                    byte[] data = resp.getBytes(StandardCharsets.UTF_8);
                    sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                    out.write(data);
                    return keepAlive;
                }
                sendJsonError(out, 409, "Conflict", "Compresión cancelada", keepAlive);
                return keepAlive;
            } catch (IOException e) {
                sendJsonError(out, 500, "Internal Server Error",
                        e.getMessage() != null ? e.getMessage() : "Error al comprimir", keepAlive);
                return keepAlive;
            } finally {
                stats.endTransfer(tx, ok);
            }
        }

        if ("POST".equals(method) && "/api/extract".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            JSONObject bodyObj = parseJsonQuietly(bodyBytes);
            String pathParam = query.getOrDefault("path", bodyObj != null ? bodyObj.optString("path", "") : "");
            String destParam = query.getOrDefault("destDir", bodyObj != null ? bodyObj.optString("destDir", "") : "");
            File archive;
            try {
                archive = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!archive.isFile()) {
                sendJsonError(out, 404, "Not Found", "Archivo comprimido no encontrado", keepAlive);
                return keepAlive;
            }
            String lower = archive.getName().toLowerCase(Locale.US);
            boolean isTarGz = lower.endsWith(".tar.gz") || lower.endsWith(".tgz");
            boolean isZip = lower.endsWith(".zip");
            if (!isZip && !isTarGz) {
                sendJsonError(out, 400, "Bad Request", "Formato soportado para extraer: .zip, .tar.gz, .tgz", keepAlive);
                return keepAlive;
            }
            File destDir;
            try {
                destDir = destParam.trim().isEmpty()
                        ? FolderZipper.resolveUniqueExtractDir(archive)
                        : resolveSafePath(destParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Carpeta de destino inválida", keepAlive);
                return keepAlive;
            }
            TransferStats.ActiveTransfer tx = stats.beginTransfer(
                    archive.getName(), "/" + relPathOf(destDir), TransferStats.DIR_UPLOAD, "Extraer", archive.length());
            boolean ok = false;
            try {
                FolderZipper.ZipResult res = isTarGz
                        ? FolderZipper.extractTarGz(archive, destDir, tx.cancelled, (delta, e) -> stats.recordBytes(tx, (int) delta))
                        : FolderZipper.extractZip(archive, destDir, tx.cancelled, (delta, e) -> stats.recordBytes(tx, (int) delta));
                ok = !tx.cancelled.get();
                if (ok) {
                    String resp = "{\"ok\":true,\"dest\":\"" + TransferStats.escapeJson(relPathOf(destDir))
                            + "\",\"files\":" + res.filesCount + ",\"bytes\":" + res.bytesWritten + "}";
                    byte[] data = resp.getBytes(StandardCharsets.UTF_8);
                    sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                    out.write(data);
                    return keepAlive;
                }
                sendJsonError(out, 409, "Conflict", "Extracción cancelada", keepAlive);
                return keepAlive;
            } catch (Exception e) {
                sendJsonError(out, 500, "Internal Server Error",
                        e.getMessage() != null ? e.getMessage() : "Error al extraer", keepAlive);
                return keepAlive;
            } finally {
                stats.endTransfer(tx, ok);
            }
        }

        if ("GET".equals(method) && "/api/archive_list".equals(rawPath)) {
            String pathParam = query.getOrDefault("path", "");
            File archive;
            try {
                archive = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!archive.isFile()) {
                sendJsonError(out, 404, "Not Found", "Archivo no encontrado", keepAlive);
                return keepAlive;
            }
            String lower = archive.getName().toLowerCase(Locale.US);
            try {
                List<FolderZipper.ArchiveEntry> entries;
                if (lower.endsWith(".tar.gz") || lower.endsWith(".tgz")) {
                    entries = FolderZipper.listTarGzEntries(archive, 500);
                } else if (lower.endsWith(".zip")) {
                    entries = FolderZipper.listZipEntries(archive, 500);
                } else {
                    sendJsonError(out, 400, "Bad Request", "Formato no soportado para inspeccionar (.zip, .tar.gz, .tgz)", keepAlive);
                    return keepAlive;
                }
                StringBuilder sb = new StringBuilder(1024);
                sb.append("{\"ok\":true,\"name\":\"").append(TransferStats.escapeJson(archive.getName())).append("\",\"entries\":[");
                for (int i = 0; i < entries.size(); i++) {
                    if (i > 0) sb.append(',');
                    FolderZipper.ArchiveEntry e = entries.get(i);
                    sb.append("{\"name\":\"").append(TransferStats.escapeJson(e.name))
                            .append("\",\"isDir\":").append(e.isDir)
                            .append(",\"size\":").append(e.size)
                            .append(",\"compressedSize\":").append(e.compressedSize).append('}');
                }
                sb.append("]}");
                byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (IOException e) {
                sendJsonError(out, 500, "Internal Server Error", "No se pudo leer el archivo comprimido", keepAlive);
                return keepAlive;
            }
        }

        if ("POST".equals(method) && ("/api/copy".equals(rawPath) || "/api/move".equals(rawPath))) {
            boolean isMove = "/api/move".equals(rawPath);
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            JSONObject bodyObj = parseJsonQuietly(bodyBytes);
            String destParam = query.getOrDefault("destDir", bodyObj != null ? bodyObj.optString("destDir", "") : "");
            File destDir;
            List<File> items;
            try {
                destDir = resolveSafePath(destParam);
                ensureDir(destDir);
                items = resolveTargetFiles(query, bodyObj);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (items.isEmpty()) {
                sendJsonError(out, 400, "Bad Request", "No hay elementos seleccionados", keepAlive);
                return keepAlive;
            }
            long[] totals = FileOps.countItemsAndBytes(items);
            String title = (items.size() == 1) ? items.get(0).getName() : (items.size() + " elementos");
            StorageVolumes.VolumeInfo destVol = StorageVolumes.findForFile(context, destDir);
            String destLabel = "→ " + ((destVol != null && !destVol.id.isEmpty()) ? destVol.label : ("/" + relPathOf(destDir)));
            final FileOpMonitor monitor = FileOpMonitor.getInstance();
            final FileOpMonitor.ActiveFileOp op = monitor.beginOp(
                    isMove ? FileOpMonitor.OP_MOVE : FileOpMonitor.OP_COPY,
                    title, destLabel, (int) totals[1], totals[0]);
            int count = 0;
            boolean ok = false;
            try {
                FileOps.ProgressListener cb = (name, iDelta, bDelta) -> monitor.updateProgress(op, name, iDelta, bDelta);
                for (File src : items) {
                    count += isMove
                            ? FileOps.moveRecursive(src, destDir, op.cancelled, cb)
                            : FileOps.copyRecursive(src, destDir, op.cancelled, cb);
                }
                ok = !op.cancelled.get();
                String resp = "{\"ok\":true,\"count\":" + count + ",\"dest\":\"" + TransferStats.escapeJson(relPathOf(destDir)) + "\"}";
                byte[] data = resp.getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (IOException e) {
                int code = op.cancelled.get() ? 409 : 500;
                String st = op.cancelled.get() ? "Conflict" : "Internal Server Error";
                sendJsonError(out, code, st, e.getMessage() != null ? e.getMessage() : "Error en operación", keepAlive);
                return keepAlive;
            } finally {
                monitor.endOp(op, ok, ok ? ((isMove ? "Movidos: " : "Copiados: ") + title) : null);
            }
        }

        if ("POST".equals(method) && "/api/delete_batch".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            JSONObject bodyObj = parseJsonQuietly(bodyBytes);
            List<File> items;
            try {
                items = resolveTargetFiles(query, bodyObj);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (items.isEmpty()) {
                sendJsonError(out, 400, "Bad Request", "No hay elementos seleccionados", keepAlive);
                return keepAlive;
            }
            long[] totals = FileOps.countItemsAndBytes(items);
            String title = (items.size() == 1) ? items.get(0).getName() : (items.size() + " elementos");
            final FileOpMonitor monitor = FileOpMonitor.getInstance();
            final FileOpMonitor.ActiveFileOp op = monitor.beginOp(
                    FileOpMonitor.OP_DELETE, title, "Almacenamiento", (int) totals[1], totals[0]);
            int deleted = 0;
            boolean ok = false;
            try {
                FileOps.ProgressListener cb = (name, iDelta, bDelta) -> monitor.updateProgress(op, name, iDelta, bDelta);
                for (File f : items) {
                    if (FileOps.deleteRecursiveWithProgress(f, op.cancelled, cb) > 0) deleted++;
                }
                ok = !op.cancelled.get();
                byte[] data = ("{\"ok\":true,\"deleted\":" + deleted + "}").getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (IOException e) {
                int code = op.cancelled.get() ? 409 : 500;
                sendJsonError(out, code, op.cancelled.get() ? "Conflict" : "Error",
                        e.getMessage() != null ? e.getMessage() : "Error al eliminar", keepAlive);
                return keepAlive;
            } finally {
                monitor.endOp(op, ok, ok && totals[1] > 1 ? ("Eliminados: " + title) : null);
            }
        }

        if ("POST".equals(method) && "/api/touch".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            JSONObject bodyObj = parseJsonQuietly(bodyBytes);
            String dirParam = query.getOrDefault("dir", bodyObj != null ? bodyObj.optString("dir", "") : "");
            String nameParam = query.getOrDefault("name", bodyObj != null ? bodyObj.optString("name", "") : "");
            String content = bodyObj != null ? bodyObj.optString("content", "") : "";
            if (nameParam.trim().isEmpty()) {
                sendJsonError(out, 400, "Bad Request", "Nombre requerido", keepAlive);
                return keepAlive;
            }
            try {
                File dir = resolveSafePath(dirParam);
                if (!ensureDir(dir)) {
                    sendJsonError(out, 500, "Internal Server Error", "No se pudo crear la carpeta", keepAlive);
                    return keepAlive;
                }
                File target = resolveSafeChild(dir, sanitizeFileName(nameParam));
                if (target.exists()) {
                    sendJsonError(out, 409, "Conflict", "Ya existe un archivo con ese nombre", keepAlive);
                    return keepAlive;
                }
                FileOps.writeTextFile(target, content);
                notifyMediaScannerAsync(target);
                byte[] data = ("{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(target)) + "\"}")
                        .getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
        }

        if ("GET".equals(method) && "/api/info".equals(rawPath)) {
            String pathParam = query.getOrDefault("path", "");
            File target;
            try {
                target = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!target.exists()) {
                sendJsonError(out, 404, "Not Found", "Elemento no encontrado", keepAlive);
                return keepAlive;
            }
            boolean isDir = target.isDirectory();
            long size = isDir ? 0L : target.length();
            long filesCount = isDir ? 0L : 1L;
            long dirsCount = 0L;
            String md5 = "—";
            String sha256 = "—";
            if (isDir) {
                long[] st = FileOps.folderStats(target);
                size = st[0];
                filesCount = st[1];
                dirsCount = st[2];
            } else {
                String[] h = FileOps.computeMd5Sha256(target);
                md5 = h[0];
                sha256 = h[1];
            }
            String resp = "{\"ok\":true"
                    + ",\"name\":\"" + TransferStats.escapeJson(target.equals(rootDir) ? "/" : target.getName()) + "\""
                    + ",\"path\":\"" + TransferStats.escapeJson(relPathOf(target)) + "\""
                    + ",\"isDir\":" + isDir
                    + ",\"size\":" + size
                    + ",\"mtime\":" + target.lastModified()
                    + ",\"mime\":\"" + TransferStats.escapeJson(isDir ? "inode/directory" : guessMime(target.getName())) + "\""
                    + ",\"perms\":\"" + FileOps.formatPermissions(target) + "\""
                    + ",\"fileCount\":" + filesCount
                    + ",\"dirCount\":" + dirsCount
                    + ",\"md5\":\"" + md5 + "\""
                    + ",\"sha256\":\"" + sha256 + "\"}";
            byte[] data = resp.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("GET".equals(method) && "/api/search".equals(rawPath)) {
            String dirParam = query.getOrDefault("dir", "");
            String qParam = query.getOrDefault("q", "");
            String catParam = query.getOrDefault("category", "");
            File startDir;
            try {
                startDir = resolveSafePath(dirParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            List<File> found = FileOps.searchRecursive(startDir, qParam, catParam, 250);
            StringBuilder sb = new StringBuilder(2048);
            sb.append("{\"ok\":true,\"dir\":\"").append(TransferStats.escapeJson(relPathOf(startDir))).append("\",\"entries\":");
            appendEntriesJson(sb, found, true);
            sb.append('}');
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("GET".equals(method) && "/api/text".equals(rawPath)) {
            String pathParam = query.getOrDefault("path", "");
            File target;
            try {
                target = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (!target.isFile()) {
                sendJsonError(out, 404, "Not Found", "Archivo no encontrado", keepAlive);
                return keepAlive;
            }
            try {
                String content = FileOps.readTextFile(target, 1024 * 1024);
                String resp = "{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(target))
                        + "\",\"name\":\"" + TransferStats.escapeJson(target.getName())
                        + "\",\"content\":\"" + TransferStats.escapeJson(content) + "\"}";
                byte[] data = resp.getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (IOException e) {
                sendJsonError(out, 400, "Bad Request",
                        e.getMessage() != null ? e.getMessage() : "No se puede abrir como texto", keepAlive);
                return keepAlive;
            }
        }

        if ("POST".equals(method) && "/api/text".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            JSONObject bodyObj = parseJsonQuietly(bodyBytes);
            String pathParam = query.getOrDefault("path", bodyObj != null ? bodyObj.optString("path", "") : "");
            if (bodyObj == null || !bodyObj.has("content")) {
                sendJsonError(out, 400, "Bad Request", "Contenido requerido", keepAlive);
                return keepAlive;
            }
            File target;
            try {
                target = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (target.isDirectory()) {
                sendJsonError(out, 400, "Bad Request", "No se puede sobrescribir una carpeta", keepAlive);
                return keepAlive;
            }
            try {
                FileOps.writeTextFile(target, bodyObj.optString("content", ""));
                notifyMediaScannerAsync(target);
                String resp = "{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(target))
                        + "\",\"bytes\":" + target.length() + "}";
                byte[] data = resp.getBytes(StandardCharsets.UTF_8);
                sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
                out.write(data);
                return keepAlive;
            } catch (IOException e) {
                sendJsonError(out, 500, "Internal Server Error", "Error al guardar el archivo", keepAlive);
                return keepAlive;
            }
        }

        if ("POST".equals(method) && "/api/rename".equals(rawPath)) {
            byte[] bodyBytes = consumeSmallBody(in, headers, out);
            String pathParam = query.getOrDefault("path", "");
            String newNameParam = query.getOrDefault("name", "");
            if (bodyBytes.length > 0) {
                try {
                    JSONObject obj = new JSONObject(new String(bodyBytes, StandardCharsets.UTF_8));
                    if (pathParam.isEmpty() && obj.has("path")) pathParam = obj.optString("path", "");
                    if (newNameParam.isEmpty() && obj.has("name")) newNameParam = obj.optString("name", "");
                } catch (Exception ignored) {
                }
            }
            if (newNameParam.trim().isEmpty()) {
                sendJsonError(out, 400, "Bad Request", "El nombre nuevo no puede estar vacío", keepAlive);
                return keepAlive;
            }
            File target;
            try {
                target = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (isProtectedVolumeRoot(target)) {
                sendJsonError(out, 403, "Forbidden", "No se puede renombrar la raíz", keepAlive);
                return keepAlive;
            }
            if (!target.exists()) {
                sendJsonError(out, 404, "Not Found", "Elemento no encontrado", keepAlive);
                return keepAlive;
            }
            String cleanNew = sanitizeFileName(newNameParam);
            File parent = target.getParentFile();
            if (parent == null) {
                sendJsonError(out, 403, "Forbidden", "Carpeta padre inválida", keepAlive);
                return keepAlive;
            }
            File dest;
            try {
                dest = resolveSafeChild(parent, cleanNew);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Nombre no permitido", keepAlive);
                return keepAlive;
            }
            if (dest.exists() && !dest.equals(target)) {
                sendJsonError(out, 409, "Conflict", "Ya existe un elemento con ese nombre", keepAlive);
                return keepAlive;
            }
            boolean ok = target.equals(dest) || target.renameTo(dest);
            if (!ok) {
                sendJsonError(out, 500, "Internal Server Error", "No se pudo renombrar", keepAlive);
                return keepAlive;
            }
            if (dest.isFile()) {
                notifyMediaScannerAsync(dest);
            }
            String resp = "{\"ok\":true,\"path\":\"" + TransferStats.escapeJson(relPathOf(dest))
                    + "\",\"name\":\"" + TransferStats.escapeJson(dest.getName()) + "\"}";
            byte[] data = resp.getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, 200, "OK", "application/json; charset=utf-8", data.length, noCacheHeaders(), keepAlive);
            out.write(data);
            return keepAlive;
        }

        if ("DELETE".equals(method) && "/api/file".equals(rawPath)) {
            consumeSmallBody(in, headers, out);
            String pathParam = query.getOrDefault("path", "");
            File target;
            try {
                target = resolveSafePath(pathParam);
            } catch (SecurityException e) {
                sendJsonError(out, 403, "Forbidden", "Acceso denegado", keepAlive);
                return keepAlive;
            }
            if (isProtectedVolumeRoot(target)) {
                sendJsonError(out, 403, "Forbidden", "No se puede eliminar la raíz", keepAlive);
                return keepAlive;
            }
            if (!target.exists()) {
                sendJsonError(out, 404, "Not Found", "No existe", keepAlive);
                return keepAlive;
            }
            long[] totals = FileOps.countItemsAndBytes(java.util.Collections.singletonList(target));
            final FileOpMonitor monitor = FileOpMonitor.getInstance();
            final FileOpMonitor.ActiveFileOp op = monitor.beginOp(
                    FileOpMonitor.OP_DELETE, target.getName(), "/" + relPathOf(target),
                    (int) totals[1], totals[0]);
            boolean deleted = false;
            try {
                FileOps.ProgressListener cb = (name, iDelta, bDelta) -> monitor.updateProgress(op, name, iDelta, bDelta);
                deleted = FileOps.deleteRecursiveWithProgress(target, op.cancelled, cb) > 0 && !target.exists();
            } catch (IOException ignored) {
                deleted = false;
            } finally {
                monitor.endOp(op, deleted, deleted && totals[1] > 1 ? ("Eliminado: " + target.getName()) : null);
            }
            byte[] data = ("{\"ok\":" + deleted + "}").getBytes(StandardCharsets.UTF_8);
            sendHeaders(out, deleted ? 200 : (op.cancelled.get() ? 409 : 500),
                    deleted ? "OK" : "Error",
                    "application/json; charset=utf-8", data.length, null, keepAlive);
            out.write(data);
            return keepAlive;
        }

        sendSimple(out, 404, "Not Found", "Endpoint no encontrado", keepAlive);
        return keepAlive;
    }

    private void cleanupChunkSession(String txId) {
        ChunkedUploadSession s = chunkSessions.remove(txId);
        if (s != null) {
            s.closeAndDeleteTemp();
            stats.endTransfer(s.tx, false);
        }
    }

    private boolean serveFile(Socket socket, String method, File file, Map<String, String> headers,
                              OutputStream out, boolean attachment, boolean keepAlive) throws IOException {
        long fileLen = file.length();
        String mime = guessMime(file.getName());
        long start = 0;
        long end = fileLen - 1;
        boolean isRange = false;

        String rangeHeader = headers.get("range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String spec = rangeHeader.substring(6).trim();
            int comma = spec.indexOf(',');
            if (comma >= 0) spec = spec.substring(0, comma).trim();
            int dash = spec.indexOf('-');
            if (dash >= 0) {
                String left = spec.substring(0, dash).trim();
                String right = spec.substring(dash + 1).trim();
                try {
                    if (left.isEmpty() && !right.isEmpty()) {
                        long suffix = Long.parseLong(right);
                        start = Math.max(0L, fileLen - suffix);
                    } else if (!left.isEmpty()) {
                        start = Long.parseLong(left);
                        if (!right.isEmpty()) {
                            end = Math.min(fileLen - 1, Long.parseLong(right));
                        }
                    }
                    isRange = true;
                } catch (NumberFormatException ignored) {
                    isRange = false;
                }
            }
        }

        if (fileLen == 0) {
            isRange = false;
            start = 0;
            end = -1;
        } else if (isRange && (start < 0 || start >= fileLen || end < start)) {
            Map<String, String> extra = new HashMap<>();
            extra.put("Content-Range", "bytes */" + fileLen);
            sendHeaders(out, 416, "Range Not Satisfiable", "text/plain", 0, extra, keepAlive);
            return keepAlive;
        }

        long contentLen = (fileLen == 0) ? 0L : (end - start + 1);
        Map<String, String> extra = new HashMap<>();
        extra.put("Accept-Ranges", "bytes");
        extra.put("Last-Modified", formatRfc1123(file.lastModified()));
        extra.put("ETag", "\"" + fileLen + "-" + file.lastModified() + "\"");
        if (attachment) {
            extra.put("Content-Disposition", "attachment; filename=\"" + file.getName().replace("\"", "_") + "\"");
        }
        if (isRange) {
            extra.put("Content-Range", "bytes " + start + "-" + end + "/" + fileLen);
            sendHeaders(out, 206, "Partial Content", mime, contentLen, extra, keepAlive);
        } else {
            sendHeaders(out, 200, "OK", mime, contentLen, extra, keepAlive);
        }

        if ("HEAD".equals(method) || contentLen == 0) {
            return keepAlive;
        }

        File parent = file.getParentFile();
        String folderDisplay = (parent == null) ? "/" : ("/" + relPathOf(parent));
        TransferStats.ActiveTransfer tx = stats.beginTransfer(
                file.getName(), folderDisplay, TransferStats.DIR_DOWNLOAD, "Web", contentLen);
        tx.attachCloseable(socket);
        boolean ok = false;
        try (FileInputStream fis = new FileInputStream(file)) {
            if (start > 0) {
                long skipped = 0;
                while (skipped < start) {
                    long s = fis.skip(start - skipped);
                    if (s <= 0) {
                        if (fis.read() < 0) break;
                        s = 1;
                    }
                    skipped += s;
                }
            }
            byte[] buf = new byte[IO_BUFFER_SIZE];
            long remaining = contentLen;
            while (remaining > 0) {
                if (tx.cancelled.get()) {
                    throw new IOException("Transfer cancelled");
                }
                int toRead = (int) Math.min(buf.length, remaining);
                int n = fis.read(buf, 0, toRead);
                if (n < 0) break;
                out.write(buf, 0, n);
                stats.recordBytes(tx, n);
                remaining -= n;
            }
            out.flush();
            ok = (remaining == 0) && !tx.cancelled.get();
            return ok && keepAlive;
        } finally {
            tx.detachCloseable(socket);
            stats.endTransfer(tx, ok);
        }
    }

    private long streamBodyToTempFile(BufferedInputStream in, Map<String, String> headers,
                                      OutputStream rawOut, File tempFile,
                                      TransferStats.ActiveTransfer tx) throws IOException {
        maybeSendContinue(headers, rawOut);
        long totalWritten = 0;
        String te = headers.getOrDefault("transfer-encoding", "").toLowerCase(Locale.US);
        boolean chunked = te.contains("chunked");
        long contentLength = parseContentLength(headers);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] writeBuf = new byte[IO_BUFFER_SIZE];
            int fill = 0;

            if (chunked) {
                while (true) {
                    if (tx.cancelled.get()) throw new IOException("Transfer cancelled");
                    String sizeLine = readAsciiLine(in, 4096);
                    if (sizeLine == null) throw new IOException("Unexpected EOF in chunked stream");
                    int semi = sizeLine.indexOf(';');
                    if (semi >= 0) sizeLine = sizeLine.substring(0, semi);
                    sizeLine = sizeLine.trim();
                    if (sizeLine.isEmpty()) continue;
                    long chunkSize = Long.parseLong(sizeLine, 16);
                    if (chunkSize == 0) {
                        while (true) {
                            String trailer = readAsciiLine(in, 4096);
                            if (trailer == null || trailer.isEmpty()) break;
                        }
                        break;
                    }
                    long rem = chunkSize;
                    while (rem > 0) {
                        if (tx.cancelled.get()) throw new IOException("Transfer cancelled");
                        int toRead = (int) Math.min(writeBuf.length - fill, rem);
                        int n = in.read(writeBuf, fill, toRead);
                        if (n < 0) throw new IOException("Unexpected EOF inside chunk");
                        stats.recordBytes(tx, n);
                        fill += n;
                        totalWritten += n;
                        rem -= n;
                        if (fill == writeBuf.length) {
                            fos.write(writeBuf, 0, fill);
                            fill = 0;
                        }
                    }
                    readAsciiLine(in, 64);
                }
            } else if (contentLength > 0) {
                long rem = contentLength;
                while (rem > 0) {
                    if (tx.cancelled.get()) throw new IOException("Transfer cancelled");
                    int toRead = (int) Math.min(writeBuf.length - fill, rem);
                    int n = in.read(writeBuf, fill, toRead);
                    if (n < 0) throw new IOException("Unexpected EOF reading body");
                    stats.recordBytes(tx, n);
                    fill += n;
                    totalWritten += n;
                    rem -= n;
                    if (fill == writeBuf.length) {
                        fos.write(writeBuf, 0, fill);
                        fill = 0;
                    }
                }
            }
            if (fill > 0) {
                fos.write(writeBuf, 0, fill);
            }
            fos.flush();
        }
        return totalWritten;
    }

    private long streamBodyToChannel(InputStream in, Map<String, String> headers,
                                     OutputStream rawOut, FileChannel channel, long startOffset,
                                     TransferStats.ActiveTransfer tx) throws IOException {
        maybeSendContinue(headers, rawOut);
        long contentLength = parseContentLength(headers);
        if (contentLength <= 0) {
            return 0L;
        }
        byte[] writeBuf = new byte[IO_BUFFER_SIZE];
        int fill = 0;
        long totalWritten = 0;
        long writePos = startOffset;
        long rem = contentLength;
        while (rem > 0) {
            if (tx.cancelled.get()) throw new IOException("Transfer cancelled");
            int toRead = (int) Math.min(writeBuf.length - fill, rem);
            int n = in.read(writeBuf, fill, toRead);
            if (n < 0) throw new IOException("Unexpected EOF reading chunk");
            stats.recordBytes(tx, n);
            fill += n;
            totalWritten += n;
            rem -= n;
            if (fill == writeBuf.length) {
                writeChannelFully(channel, writeBuf, fill, writePos);
                writePos += fill;
                fill = 0;
            }
        }
        if (fill > 0) {
            writeChannelFully(channel, writeBuf, fill, writePos);
        }
        return totalWritten;
    }

    private static void writeChannelFully(FileChannel channel, byte[] data, int len, long pos) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(data, 0, len);
        long curPos = pos;
        while (bb.hasRemaining()) {
            int w = channel.write(bb, curPos);
            curPos += w;
        }
    }

    private byte[] consumeSmallBody(BufferedInputStream in, Map<String, String> headers,
                                  OutputStream rawOut) throws IOException {
        maybeSendContinue(headers, rawOut);
        String te = headers.getOrDefault("transfer-encoding", "").toLowerCase(Locale.US);
        boolean chunked = te.contains("chunked");
        long contentLength = parseContentLength(headers);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int maxBytes = 1024 * 1024;

        if (chunked) {
            while (true) {
                String sizeLine = readAsciiLine(in, 4096);
                if (sizeLine == null) break;
                int semi = sizeLine.indexOf(';');
                if (semi >= 0) sizeLine = sizeLine.substring(0, semi);
                sizeLine = sizeLine.trim();
                if (sizeLine.isEmpty()) continue;
                long chunkSize = Long.parseLong(sizeLine, 16);
                if (chunkSize == 0) {
                    while (true) {
                        String trailer = readAsciiLine(in, 4096);
                        if (trailer == null || trailer.isEmpty()) break;
                    }
                    break;
                }
                long rem = chunkSize;
                while (rem > 0) {
                    int n = in.read(buf, 0, (int) Math.min(buf.length, rem));
                    if (n < 0) break;
                    if (baos.size() < maxBytes) {
                        baos.write(buf, 0, Math.min(n, maxBytes - baos.size()));
                    }
                    rem -= n;
                }
                readAsciiLine(in, 64);
            }
        } else if (contentLength > 0) {
            long rem = contentLength;
            while (rem > 0) {
                int n = in.read(buf, 0, (int) Math.min(buf.length, rem));
                if (n < 0) break;
                if (baos.size() < maxBytes) {
                    baos.write(buf, 0, Math.min(n, maxBytes - baos.size()));
                }
                rem -= n;
            }
        }
        return baos.toByteArray();
    }

    private boolean isAuthorized(Map<String, String> headers) {
        if (authUser.isEmpty()) {
            return true;
        }
        String tok = extractSessionCookie(headers);
        if (tok != null) {
            Long createdAt = sessions.get(tok);
            if (createdAt != null) {
                if (System.currentTimeMillis() - createdAt <= SESSION_TTL_MS) {
                    return true;
                }
                sessions.remove(tok);
            }
        }
        String auth = headers.get("authorization");
        if (auth != null) {
            String trimmed = auth.trim();
            if (trimmed.length() >= 7 && trimmed.regionMatches(true, 0, "Basic ", 0, 6)) {
                try {
                    String decoded = new String(
                            Base64.getDecoder().decode(trimmed.substring(6).trim()),
                            StandardCharsets.UTF_8);
                    return (authUser + ":" + authPass).equals(decoded);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return false;
    }

    private static String extractSessionCookie(Map<String, String> headers) {
        String cookie = headers.get("cookie");
        if (cookie == null || cookie.isEmpty()) {
            return null;
        }
        String[] parts = cookie.split(";");
        for (String p : parts) {
            String trimmed = p.trim();
            int eq = trimmed.indexOf('=');
            if (eq > 0 && SESSION_COOKIE.equals(trimmed.substring(0, eq).trim())) {
                return trimmed.substring(eq + 1).trim();
            }
        }
        return null;
    }

    private void drainSmallBodyWithoutContinue(BufferedInputStream in, Map<String, String> headers) throws IOException {
        String expect = headers.get("expect");
        if (expect != null && expect.toLowerCase(Locale.US).contains("100-continue")) {
            return;
        }
        long contentLength = parseContentLength(headers);
        if (contentLength > 0 && contentLength <= 65536) {
            byte[] buf = new byte[4096];
            long rem = contentLength;
            while (rem > 0) {
                int n = in.read(buf, 0, (int) Math.min(buf.length, rem));
                if (n < 0) break;
                rem -= n;
            }
        }
    }

    private void maybeSendContinue(Map<String, String> headers, OutputStream rawOut) throws IOException {
        String expect = headers.get("expect");
        if (expect != null && expect.toLowerCase(Locale.US).contains("100-continue")) {
            rawOut.write("HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            rawOut.flush();
        }
    }

    private void appendEntriesJson(StringBuilder sb, List<File> entries, boolean includePath) {
        sb.append('[');
        boolean first = true;
        for (File f : entries) {
            if (!first) sb.append(',');
            first = false;
            String name = f.getName();
            boolean isDir = f.isDirectory();
            String mime = isDir ? "inode/directory" : guessMime(name);
            boolean isImage = !isDir && mime.startsWith("image/");
            sb.append('{');
            sb.append("\"name\":\"").append(TransferStats.escapeJson(name)).append("\",");
            if (includePath) {
                String rel = relPathOf(f);
                File p = f.getParentFile();
                String parentRel = (p != null) ? relPathOf(p) : "";
                sb.append("\"path\":\"").append(TransferStats.escapeJson(rel)).append("\",");
                sb.append("\"dir\":\"").append(TransferStats.escapeJson(parentRel)).append("\",");
            }
            sb.append("\"isDir\":").append(isDir).append(',');
            sb.append("\"size\":").append(isDir ? 0L : f.length()).append(',');
            if (isDir) {
                sb.append("\"itemCount\":").append(FolderZipper.countVisibleChildren(f)).append(',');
            }
            sb.append("\"mtime\":").append(f.lastModified()).append(',');
            sb.append("\"mime\":\"").append(TransferStats.escapeJson(mime)).append("\",");
            sb.append("\"isImage\":").append(isImage);
            sb.append('}');
        }
        sb.append(']');
    }

    private static JSONObject parseJsonQuietly(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return new JSONObject(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isProtectedVolumeRoot(File f) {
        if (f == null || f.equals(rootDir)) return true;
        return StorageVolumes.isVolumeRoot(context, f);
    }

    private File resolveBaseRootFor(File file) {
        String p = file.getPath();
        if (p.equals(rootDirPath) || p.startsWith(rootDirPath + File.separator)) {
            return rootDir;
        }
        StorageVolumes.VolumeInfo vol = StorageVolumes.findForFile(context, file);
        return (vol != null) ? vol.rootDir : rootDir;
    }

    private List<File> resolveTargetFiles(Map<String, String> query, JSONObject body) throws IOException {
        List<File> out = new ArrayList<>();
        if (body != null && body.has("paths")) {
            JSONArray arr = body.optJSONArray("paths");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String p = arr.optString(i, "");
                    if (!p.isEmpty()) {
                        File f = resolveSafePath(p);
                        if (!isProtectedVolumeRoot(f) && f.exists()) out.add(f);
                    }
                }
            }
        }
        if (out.isEmpty()) {
            String single = query.getOrDefault("path", body != null ? body.optString("path", "") : "");
            if (!single.isEmpty()) {
                File f = resolveSafePath(single);
                if (!isProtectedVolumeRoot(f) && f.exists()) out.add(f);
            }
        }
        return out;
    }

    private boolean ensureDir(File dir) {
        if (dir.isDirectory()) {
            return true;
        }
        synchronized (dirLock) {
            return dir.isDirectory() || dir.mkdirs();
        }
    }

    private File resolveSafePath(String relPath) throws IOException {
        String clean = normalizeRelPath(relPath);
        File baseRoot = rootDir;
        String basePath = rootDirPath;
        String subRel = clean;

        if ("@ext".equals(clean) || clean.startsWith("@ext/")) {
            String rest = clean.length() > 5 ? clean.substring(5) : "";
            int slash = rest.indexOf('/');
            String volId = (slash >= 0) ? rest.substring(0, slash) : rest;
            subRel = (slash >= 0) ? rest.substring(slash + 1) : "";
            StorageVolumes.VolumeInfo vol = (volId.isEmpty()) ? null : StorageVolumes.findById(context, volId);
            if (vol == null) {
                throw new SecurityException("External volume unavailable: " + volId);
            }
            baseRoot = vol.rootDir;
            basePath = baseRoot.getPath();
        }

        File candidate = subRel.isEmpty() ? baseRoot : new File(baseRoot, subRel);
        File canonical = candidate.getCanonicalFile();
        String path = canonical.getPath();
        if (!path.equals(basePath) && !path.startsWith(basePath + File.separator)) {
            throw new SecurityException("Path traversal blocked: " + relPath);
        }
        return canonical;
    }

    private File resolveSafeChild(File dir, String childName) throws IOException {
        File canonicalDir = dir.getCanonicalFile();
        File baseRoot = resolveBaseRootFor(canonicalDir);
        String basePath = baseRoot.getPath();
        File candidate = new File(canonicalDir, childName).getCanonicalFile();
        String path = candidate.getPath();
        if (!path.startsWith(basePath + File.separator)) {
            throw new SecurityException("Child traversal blocked: " + childName);
        }
        return candidate;
    }

    private String relPathOf(File file) {
        if (file == null) return "";
        String p = file.getPath();
        if (p.equals(rootDirPath)) {
            return "";
        }
        if (p.startsWith(rootDirPath + File.separator)) {
            return p.substring(rootDirPath.length() + 1).replace(File.separatorChar, '/');
        }
        StorageVolumes.VolumeInfo vol = StorageVolumes.findForFile(context, file);
        if (vol != null && !vol.id.isEmpty()) {
            String vp = vol.rootDir.getPath();
            String prefix = "@ext/" + vol.id;
            if (p.equals(vp)) {
                return prefix;
            }
            if (p.startsWith(vp + File.separator)) {
                return prefix + "/" + p.substring(vp.length() + 1).replace(File.separatorChar, '/');
            }
        }
        return "";
    }

    private void notifyMediaScannerAsync(final File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        String name = file.getName();
        if (name.startsWith(".") || file.length() == 0) {
            return;
        }
        ExecutorService exec = this.mediaScannerPool;
        if (exec != null && !exec.isShutdown()) {
            final String absPath = file.getAbsolutePath();
            exec.execute(() -> {
                try {
                    MediaScannerConnection.scanFile(context, new String[]{absPath}, null, null);
                } catch (Exception e) {
                    Log.w(TAG, "MediaScanner warning: " + e.getMessage());
                }
            });
        }
    }

    private long[] getStorageStats() {
        try {
            StatFs sf = new StatFs(rootDirPath);
            return new long[]{sf.getAvailableBytes(), sf.getTotalBytes()};
        } catch (Exception e) {
            return new long[]{0L, 0L};
        }
    }

    private static void sendHeaders(OutputStream out, int code, String statusText,
                                    String contentType, long contentLength,
                                    Map<String, String> extraHeaders, boolean keepAlive) throws IOException {
        StringBuilder sb = new StringBuilder(512);
        sb.append("HTTP/1.1 ").append(code).append(' ').append(statusText).append("\r\n");
        sb.append("Server: ExpressiveWebFiles/2.2\r\n");
        sb.append("Date: ").append(formatRfc1123(System.currentTimeMillis())).append("\r\n");
        if (keepAlive) {
            sb.append("Connection: keep-alive\r\n");
            sb.append("Keep-Alive: timeout=30, max=400\r\n");
        } else {
            sb.append("Connection: close\r\n");
        }
        sb.append("Access-Control-Allow-Origin: *\r\n");
        if (contentType != null) {
            sb.append("Content-Type: ").append(contentType).append("\r\n");
        }
        if (contentLength >= 0) {
            sb.append("Content-Length: ").append(contentLength).append("\r\n");
        }
        if (extraHeaders != null) {
            for (Map.Entry<String, String> e : extraHeaders.entrySet()) {
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
            }
        }
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static void sendCorsOptions(OutputStream out, boolean keepAlive) throws IOException {
        Map<String, String> extra = new HashMap<>();
        extra.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        extra.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
        sendHeaders(out, 204, "No Content", null, 0, extra, keepAlive);
    }

    private static void sendSimple(OutputStream out, int code, String statusText,
                                   String bodyText, boolean keepAlive) throws IOException {
        byte[] b = bodyText.getBytes(StandardCharsets.UTF_8);
        sendHeaders(out, code, statusText, "text/plain; charset=utf-8", b.length, null, keepAlive);
        out.write(b);
    }

    private static void sendJsonError(OutputStream out, int code, String statusText,
                                      String msg, boolean keepAlive) throws IOException {
        byte[] b = ("{\"ok\":false,\"error\":\"" + TransferStats.escapeJson(msg) + "\"}").getBytes(StandardCharsets.UTF_8);
        sendHeaders(out, code, statusText, "application/json; charset=utf-8", b.length, noCacheHeaders(), keepAlive);
        out.write(b);
    }

    private static Map<String, String> noCacheHeaders() {
        Map<String, String> m = new HashMap<>();
        m.put("Cache-Control", "no-store, no-cache, must-revalidate");
        return m;
    }

    private static long parseContentLength(Map<String, String> headers) {
        String cl = headers.get("content-length");
        if (cl == null || cl.isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(cl.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static String readAsciiLine(InputStream in, int maxLen) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        while (true) {
            int b = in.read();
            if (b < 0) {
                if (baos.size() == 0) return null;
                break;
            }
            if (b == '\n') {
                break;
            }
            if (baos.size() < maxLen) {
                baos.write(b);
            }
        }
        byte[] arr = baos.toByteArray();
        int len = arr.length;
        if (len > 0 && arr[len - 1] == '\r') {
            len--;
        }
        return new String(arr, 0, len, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return map;
        }
        String[] pairs = rawQuery.split("&");
        for (String p : pairs) {
            if (p.isEmpty()) continue;
            int eq = p.indexOf('=');
            if (eq >= 0) {
                String k = percentDecode(p.substring(0, eq), true);
                String v = percentDecode(p.substring(eq + 1), true);
                map.put(k, v);
            } else {
                map.put(percentDecode(p, true), "");
            }
        }
        return map;
    }

    private static String percentDecode(String s, boolean plusAsSpace) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '+' && plusAsSpace) {
                out.write(' ');
            } else if (c == '%' && i + 2 < s.length()) {
                int hi = Character.digit(s.charAt(i + 1), 16);
                int lo = Character.digit(s.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    out.write((hi << 4) | lo);
                    i += 2;
                } else {
                    out.write('%');
                }
            } else {
                byte[] utf8 = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                out.write(utf8, 0, utf8.length);
            }
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String normalizeRelPath(String raw) {
        if (raw == null) return "";
        String s = raw.replace('\\', '/').trim();
        while (s.startsWith("/")) s = s.substring(1);
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static String sanitizeFileName(String name) {
        String base = name.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) base = base.substring(slash + 1);
        base = base.replace('\0', '_').trim();
        if (base.isEmpty() || ".".equals(base) || "..".equals(base)) {
            return "archivo_" + System.currentTimeMillis();
        }
        return base;
    }

    private static String formatRfc1123(long ms) {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return fmt.format(new Date(ms));
    }

    private static String guessMime(String name) {
        String lower = name.toLowerCase(Locale.US);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".heic") || lower.endsWith(".heif")) return "image/heif";
        if (lower.endsWith(".dng")) return "image/x-adobe-dng";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".flac")) return "audio/flac";
        if (lower.endsWith(".m4a") || lower.endsWith(".aac")) return "audio/mp4";
        if (lower.endsWith(".ogg") || lower.endsWith(".opus")) return "audio/ogg";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt") || lower.endsWith(".log") || lower.endsWith(".md") || lower.endsWith(".csv")) return "text/plain; charset=utf-8";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".zip")) return "application/zip";
        if (lower.endsWith(".tar.gz") || lower.endsWith(".tgz") || lower.endsWith(".gz")) return "application/gzip";
        if (lower.endsWith(".apk")) return "application/vnd.android.package-archive";
        return "application/octet-stream";
    }
}
