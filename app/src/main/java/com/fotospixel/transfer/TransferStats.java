package com.fotospixel.transfer;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class TransferStats {

    public static final String DIR_UPLOAD = "UPLOAD";
    public static final String DIR_DOWNLOAD = "DOWNLOAD";

    public static final class ActiveTransfer {
        public final String id;
        public final String fileName;
        public final String folder;
        public final String direction;
        public final String protocol;
        public final long totalBytes;
        public final long startedAtMs;
        public final AtomicLong bytesTransferred = new AtomicLong(0);
        public final AtomicBoolean cancelled = new AtomicBoolean(false);
        public volatile long speedBps = 0;

        private final List<Closeable> channels = new ArrayList<>(4);
        private volatile Runnable cancelCleanup;
        long lastSampleBytes = 0;
        long lastSampleTimeMs;

        ActiveTransfer(String id, String fileName, String folder, String direction, String protocol, long totalBytes) {
            this.id = id;
            this.fileName = fileName;
            this.folder = folder;
            this.direction = direction;
            this.protocol = protocol;
            this.totalBytes = totalBytes;
            long now = System.currentTimeMillis();
            this.startedAtMs = now;
            this.lastSampleTimeMs = now;
        }

        public void attachCloseable(Closeable c) {
            if (c == null) return;
            boolean alreadyCancelled;
            synchronized (channels) {
                alreadyCancelled = cancelled.get();
                if (!alreadyCancelled) {
                    channels.add(c);
                }
            }
            if (alreadyCancelled) {
                try {
                    c.close();
                } catch (IOException ignored) {
                }
            }
        }

        public void detachCloseable(Closeable c) {
            if (c == null) return;
            synchronized (channels) {
                channels.remove(c);
            }
        }

        public void setCancelCleanup(Runnable r) {
            this.cancelCleanup = r;
            if (cancelled.get() && r != null) {
                try {
                    r.run();
                } catch (Exception ignored) {
                }
            }
        }

        public void markCancelled() {
            cancelled.set(true);
        }

        public void closeAndCleanupNow() {
            cancelled.set(true);
            List<Closeable> toClose;
            synchronized (channels) {
                toClose = new ArrayList<>(channels);
                channels.clear();
            }
            for (Closeable c : toClose) {
                try {
                    if (c instanceof Socket) {
                        try {
                            ((Socket) c).setSoLinger(true, 0);
                        } catch (IOException ignored) {
                        }
                    }
                    c.close();
                } catch (IOException ignored) {
                }
            }
            Runnable cleanup = this.cancelCleanup;
            this.cancelCleanup = null;
            if (cleanup != null) {
                try {
                    cleanup.run();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static final class CompletedTransfer {
        public final String fileName;
        public final String folder;
        public final String direction;
        public final String protocol;
        public final long bytes;
        public final long avgSpeedBps;
        public final long completedAtMs;
        public final boolean ok;
        public final boolean cancelled;

        CompletedTransfer(String fileName, String folder, String direction, String protocol,
                          long bytes, long avgSpeedBps, long completedAtMs, boolean ok, boolean cancelled) {
            this.fileName = fileName;
            this.folder = folder;
            this.direction = direction;
            this.protocol = protocol;
            this.bytes = bytes;
            this.avgSpeedBps = avgSpeedBps;
            this.completedAtMs = completedAtMs;
            this.ok = ok;
            this.cancelled = cancelled;
        }
    }

    private static final TransferStats INSTANCE = new TransferStats();
    private static final int HISTORY_LEN = 30;
    private static final int MAX_RECENT = 50;

    private final AtomicInteger idSeq = new AtomicInteger(1);
    private final ConcurrentHashMap<String, ActiveTransfer> activeMap = new ConcurrentHashMap<>();
    private final ArrayDeque<CompletedTransfer> recentDeque = new ArrayDeque<>();

    private final AtomicLong totalUploadBytes = new AtomicLong(0);
    private final AtomicLong totalDownloadBytes = new AtomicLong(0);

    private volatile long uploadSpeedBps = 0;
    private volatile long downloadSpeedBps = 0;

    private final long[] uploadHistory = new long[HISTORY_LEN];
    private final long[] downloadHistory = new long[HISTORY_LEN];

    private long lastSampleTimeMs = System.currentTimeMillis();
    private long lastTotalUploadBytes = 0;
    private long lastTotalDownloadBytes = 0;

    private volatile boolean serverRunning = false;
    private volatile String serverIp = "0.0.0.0";
    private volatile int serverPort = 8080;
    private volatile String authUser = "pixel";
    private volatile String authPass = "pixel";
    private volatile Runnable onStateChangedListener;
    private final ExecutorService cancelExecutor = Executors.newCachedThreadPool();

    private TransferStats() {
    }

    public static TransferStats getInstance() {
        return INSTANCE;
    }

    public void setOnStateChangedListener(Runnable listener) {
        this.onStateChangedListener = listener;
    }

    private void notifyStateChangedAsync() {
        Runnable l = this.onStateChangedListener;
        if (l != null) {
            cancelExecutor.execute(l);
        }
    }

    public void setServerState(boolean running, String ip, int port) {
        this.serverRunning = running;
        if (ip != null && !ip.isEmpty()) {
            this.serverIp = ip;
        }
        if (port > 0) {
            this.serverPort = port;
        }
    }

    public void setCredentials(String user, String pass) {
        this.authUser = (user == null) ? "" : user;
        this.authPass = (pass == null) ? "" : pass;
    }

    public String getAuthUser() {
        return authUser;
    }

    public String getAuthPass() {
        return authPass;
    }

    public boolean isServerRunning() {
        return serverRunning;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public ActiveTransfer beginTransfer(String fileName, String folder, String direction, String protocol, long totalBytes) {
        String id = "tx-" + idSeq.getAndIncrement();
        String normFolder = (folder == null || folder.isEmpty()) ? "/" : (folder.startsWith("/") ? folder : "/" + folder);
        ActiveTransfer tx = new ActiveTransfer(id, fileName, normFolder, direction, protocol, totalBytes);
        activeMap.put(id, tx);
        return tx;
    }

    public ActiveTransfer getActiveTransfer(String id) {
        return (id == null) ? null : activeMap.get(id);
    }

    public void recordBytes(ActiveTransfer tx, int delta) {
        if (tx == null || delta <= 0) {
            return;
        }
        tx.bytesTransferred.addAndGet(delta);
        if (DIR_UPLOAD.equals(tx.direction)) {
            totalUploadBytes.addAndGet(delta);
        } else {
            totalDownloadBytes.addAndGet(delta);
        }
    }

    public boolean cancelTransfer(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        ActiveTransfer tx = activeMap.get(id);
        if (tx != null) {
            tx.markCancelled();
            endTransfer(tx, false);
            cancelExecutor.execute(tx::closeAndCleanupNow);
            notifyStateChangedAsync();
            return true;
        }
        return false;
    }

    public int cancelAllTransfers() {
        List<ActiveTransfer> list = new ArrayList<>(activeMap.values());
        for (ActiveTransfer tx : list) {
            tx.markCancelled();
            endTransfer(tx, false);
            cancelExecutor.execute(tx::closeAndCleanupNow);
        }
        if (!list.isEmpty()) {
            notifyStateChangedAsync();
        }
        return list.size();
    }

    public void endTransfer(ActiveTransfer tx, boolean ok) {
        if (tx == null) {
            return;
        }
        if (activeMap.remove(tx.id) == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long bytes = tx.bytesTransferred.get();
        long elapsedMs = Math.max(1L, now - tx.startedAtMs);
        long avgSpeed = (bytes * 1000L) / elapsedMs;
        boolean wasCancelled = tx.cancelled.get();

        CompletedTransfer done = new CompletedTransfer(
                tx.fileName,
                tx.folder,
                tx.direction,
                tx.protocol,
                bytes,
                avgSpeed,
                now,
                ok && !wasCancelled,
                wasCancelled
        );
        synchronized (recentDeque) {
            recentDeque.addFirst(done);
            while (recentDeque.size() > MAX_RECENT) {
                recentDeque.removeLast();
            }
        }
    }

    public synchronized void sampleSpeeds() {
        long now = System.currentTimeMillis();
        long dtMs = now - lastSampleTimeMs;
        if (dtMs < 150) {
            return;
        }

        long curUp = totalUploadBytes.get();
        long curDown = totalDownloadBytes.get();

        long upDelta = Math.max(0L, curUp - lastTotalUploadBytes);
        long downDelta = Math.max(0L, curDown - lastTotalDownloadBytes);

        long upSpeed = (upDelta * 1000L) / dtMs;
        long downSpeed = (downDelta * 1000L) / dtMs;

        uploadSpeedBps = upSpeed;
        downloadSpeedBps = downSpeed;

        lastTotalUploadBytes = curUp;
        lastTotalDownloadBytes = curDown;
        lastSampleTimeMs = now;

        System.arraycopy(uploadHistory, 1, uploadHistory, 0, HISTORY_LEN - 1);
        uploadHistory[HISTORY_LEN - 1] = upSpeed;

        System.arraycopy(downloadHistory, 1, downloadHistory, 0, HISTORY_LEN - 1);
        downloadHistory[HISTORY_LEN - 1] = downSpeed;

        for (ActiveTransfer tx : activeMap.values()) {
            long txBytes = tx.bytesTransferred.get();
            long txDt = now - tx.lastSampleTimeMs;
            if (txDt >= 150) {
                long txDelta = Math.max(0L, txBytes - tx.lastSampleBytes);
                tx.speedBps = (txDelta * 1000L) / txDt;
                tx.lastSampleBytes = txBytes;
                tx.lastSampleTimeMs = now;
            } else {
                long totalDt = Math.max(1L, now - tx.startedAtMs);
                tx.speedBps = (txBytes * 1000L) / totalDt;
            }
        }
    }

    public long getUploadSpeedBps() {
        return uploadSpeedBps;
    }

    public long getDownloadSpeedBps() {
        return downloadSpeedBps;
    }

    public long getTotalUploadBytes() {
        return totalUploadBytes.get();
    }

    public long getTotalDownloadBytes() {
        return totalDownloadBytes.get();
    }

    public List<ActiveTransfer> getActiveTransfers() {
        return new ArrayList<>(activeMap.values());
    }

    public List<CompletedTransfer> getRecentTransfers() {
        synchronized (recentDeque) {
            return new ArrayList<>(recentDeque);
        }
    }

    public synchronized long[] getUploadHistory() {
        return uploadHistory.clone();
    }

    public synchronized long[] getDownloadHistory() {
        return downloadHistory.clone();
    }

    public String toJson(String ip, int port, long freeBytes, long totalBytes, String deviceModel) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append('{');
        sb.append("\"serverRunning\":").append(serverRunning).append(',');
        sb.append("\"ip\":\"").append(escapeJson(ip)).append("\",");
        sb.append("\"port\":").append(port).append(',');
        sb.append("\"authEnabled\":").append(!authUser.isEmpty()).append(',');
        sb.append("\"authUser\":\"").append(escapeJson(authUser)).append("\",");
        sb.append("\"deviceModel\":\"").append(escapeJson(deviceModel)).append("\",");
        sb.append("\"storageFreeBytes\":").append(freeBytes).append(',');
        sb.append("\"storageTotalBytes\":").append(totalBytes).append(',');
        sb.append("\"uploadSpeedBps\":").append(uploadSpeedBps).append(',');
        sb.append("\"downloadSpeedBps\":").append(downloadSpeedBps).append(',');
        sb.append("\"totalUploadBytes\":").append(totalUploadBytes.get()).append(',');
        sb.append("\"totalDownloadBytes\":").append(totalDownloadBytes.get()).append(',');

        long[] upHistCopy;
        long[] downHistCopy;
        synchronized (this) {
            upHistCopy = uploadHistory.clone();
            downHistCopy = downloadHistory.clone();
        }

        sb.append("\"uploadHistory\":[");
        for (int i = 0; i < upHistCopy.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(upHistCopy[i]);
        }
        sb.append("],");

        sb.append("\"downloadHistory\":[");
        for (int i = 0; i < downHistCopy.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(downHistCopy[i]);
        }
        sb.append("],");

        sb.append("\"activeTransfers\":[");
        List<ActiveTransfer> actives = getActiveTransfers();
        long now = System.currentTimeMillis();
        for (int i = 0; i < actives.size(); i++) {
            ActiveTransfer tx = actives.get(i);
            long curBytes = tx.bytesTransferred.get();
            long speed = tx.speedBps;
            if (speed == 0 && curBytes > 0) {
                speed = (curBytes * 1000L) / Math.max(1L, now - tx.startedAtMs);
            }
            if (i > 0) sb.append(',');
            sb.append('{');
            sb.append("\"id\":\"").append(escapeJson(tx.id)).append("\",");
            sb.append("\"fileName\":\"").append(escapeJson(tx.fileName)).append("\",");
            sb.append("\"folder\":\"").append(escapeJson(tx.folder)).append("\",");
            sb.append("\"direction\":\"").append(escapeJson(tx.direction)).append("\",");
            sb.append("\"protocol\":\"").append(escapeJson(tx.protocol)).append("\",");
            sb.append("\"bytesTransferred\":").append(curBytes).append(',');
            sb.append("\"totalBytes\":").append(tx.totalBytes).append(',');
            sb.append("\"speedBps\":").append(speed).append(',');
            sb.append("\"startedAtMs\":").append(tx.startedAtMs);
            sb.append('}');
        }
        sb.append("],");

        sb.append("\"recentTransfers\":[");
        List<CompletedTransfer> recents = getRecentTransfers();
        for (int i = 0; i < recents.size(); i++) {
            CompletedTransfer done = recents.get(i);
            if (i > 0) sb.append(',');
            sb.append('{');
            sb.append("\"fileName\":\"").append(escapeJson(done.fileName)).append("\",");
            sb.append("\"folder\":\"").append(escapeJson(done.folder)).append("\",");
            sb.append("\"direction\":\"").append(escapeJson(done.direction)).append("\",");
            sb.append("\"protocol\":\"").append(escapeJson(done.protocol)).append("\",");
            sb.append("\"bytes\":").append(done.bytes).append(',');
            sb.append("\"avgSpeedBps\":").append(done.avgSpeedBps).append(',');
            sb.append("\"completedAtMs\":").append(done.completedAtMs).append(',');
            sb.append("\"ok\":").append(done.ok).append(',');
            sb.append("\"cancelled\":").append(done.cancelled);
            sb.append('}');
        }
        sb.append("]");
        sb.append('}');
        return sb.toString();
    }

    public static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format(Locale.US, "\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    public static String formatSpeed(long bps) {
        if (bps < 1024L) {
            return bps + " B/s";
        } else if (bps < 1024L * 1024L) {
            return String.format(Locale.US, "%.1f KB/s", bps / 1024.0);
        } else if (bps < 1024L * 1024L * 1024L) {
            return String.format(Locale.US, "%.2f MB/s", bps / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.US, "%.2f GB/s", bps / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "—";
        }
        if (bytes < 1024L) {
            return bytes + " B";
        } else if (bytes < 1024L * 1024L) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024L * 1024L * 1024L) {
            return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
