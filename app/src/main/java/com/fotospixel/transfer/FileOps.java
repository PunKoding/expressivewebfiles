package com.fotospixel.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared file-management operations inspired by MiXplorer:
 * recursive copy/move, recursive smart search, folder stats, MD5/SHA-256 hashes,
 * permission formatting, and UTF-8 text read/write.
 */
public final class FileOps {

    private static final int BUF_SIZE = 65536;
    private static final long MAX_HASH_SIZE = 512L * 1024L * 1024L;

    public interface ProgressListener {
        void onProgress(String currentName, int itemsDelta, long bytesDelta);
    }

    private FileOps() {
    }

    public static long[] countItemsAndBytes(List<File> items) {
        long totalBytes = 0L;
        long totalItems = 0L;
        if (items == null) return new long[]{0L, 0L};
        ArrayDeque<File> stack = new ArrayDeque<>();
        for (File f : items) {
            if (f != null && f.exists()) stack.push(f);
        }
        while (!stack.isEmpty()) {
            File cur = stack.pop();
            totalItems++;
            if (cur.isDirectory()) {
                File[] children = cur.listFiles();
                if (children != null) {
                    for (File c : children) {
                        if (!FolderZipper.shouldSkipName(c.getName())) {
                            stack.push(c);
                        }
                    }
                }
            } else if (cur.isFile()) {
                totalBytes += cur.length();
            }
        }
        return new long[]{totalBytes, totalItems};
    }

    public static File resolveUniqueChild(File destDir, String fileName) {
        File candidate = new File(destDir, fileName);
        if (!candidate.exists()) {
            return candidate;
        }
        int dot = (!candidate.isDirectory()) ? fileName.lastIndexOf('.') : -1;
        String base = (dot > 0) ? fileName.substring(0, dot) : fileName;
        String ext = (dot > 0) ? fileName.substring(dot) : "";
        int idx = 2;
        while (candidate.exists()) {
            candidate = new File(destDir, base + "_" + idx + ext);
            idx++;
        }
        return candidate;
    }

    public static int copyRecursive(File src, File destDir, AtomicBoolean cancelled) throws IOException {
        return copyRecursive(src, destDir, cancelled, null);
    }

    public static int copyRecursive(File src, File destDir, AtomicBoolean cancelled,
                                    ProgressListener cb) throws IOException {
        if (src == null || !src.exists()) {
            throw new IOException("Origen no encontrado");
        }
        if (destDir == null || (!destDir.isDirectory() && !destDir.mkdirs())) {
            throw new IOException("Carpeta de destino inválida");
        }
        if (src.isDirectory()) {
            String srcCanon = src.getCanonicalPath();
            String dstCanon = destDir.getCanonicalPath();
            if (dstCanon.equals(srcCanon) || dstCanon.startsWith(srcCanon + File.separator)) {
                throw new IOException("No se puede copiar una carpeta dentro de sí misma");
            }
        }
        File target = resolveUniqueChild(destDir, src.getName());
        return copyNode(src, target, cancelled, cb);
    }

    private static int copyNode(File src, File target, AtomicBoolean cancelled, ProgressListener cb) throws IOException {
        if (cancelled != null && cancelled.get()) {
            throw new IOException("Copia cancelada");
        }
        if (src.isDirectory()) {
            if (!target.isDirectory() && !target.mkdirs()) {
                throw new IOException("No se pudo crear carpeta: " + target.getName());
            }
            if (cb != null) {
                cb.onProgress(src.getName(), 1, 0L);
            }
            int count = 0;
            File[] children = src.listFiles();
            if (children != null) {
                for (File c : children) {
                    if (FolderZipper.shouldSkipName(c.getName())) continue;
                    count += copyNode(c, new File(target, c.getName()), cancelled, cb);
                }
            }
            return count;
        } else {
            byte[] buf = new byte[BUF_SIZE];
            try (FileInputStream fis = new FileInputStream(src);
                 BufferedInputStream bis = new BufferedInputStream(fis, BUF_SIZE);
                 FileOutputStream fos = new FileOutputStream(target);
                 BufferedOutputStream bos = new BufferedOutputStream(fos, BUF_SIZE)) {
                if (cb != null) {
                    cb.onProgress(src.getName(), 0, 0L);
                }
                int n;
                while ((n = bis.read(buf)) >= 0) {
                    if (cancelled != null && cancelled.get()) {
                        try {
                            target.delete();
                        } catch (Exception ignored) {
                        }
                        throw new IOException("Copia cancelada");
                    }
                    if (n > 0) {
                        bos.write(buf, 0, n);
                        if (cb != null) {
                            cb.onProgress(src.getName(), 0, n);
                        }
                    }
                }
                bos.flush();
            }
            if (cb != null) {
                cb.onProgress(src.getName(), 1, 0L);
            }
            return 1;
        }
    }

    public static int moveRecursive(File src, File destDir, AtomicBoolean cancelled) throws IOException {
        return moveRecursive(src, destDir, cancelled, null);
    }

    public static int moveRecursive(File src, File destDir, AtomicBoolean cancelled,
                                    ProgressListener cb) throws IOException {
        if (src == null || !src.exists()) {
            throw new IOException("Origen no encontrado");
        }
        if (destDir == null || (!destDir.isDirectory() && !destDir.mkdirs())) {
            throw new IOException("Carpeta de destino inválida");
        }
        if (src.isDirectory()) {
            String srcCanon = src.getCanonicalPath();
            String dstCanon = destDir.getCanonicalPath();
            if (dstCanon.equals(srcCanon) || dstCanon.startsWith(srcCanon + File.separator)) {
                throw new IOException("No se puede mover una carpeta dentro de sí misma");
            }
        }
        if (src.getParentFile() != null && src.getParentFile().getCanonicalPath().equals(destDir.getCanonicalPath())) {
            return 1; // already in destination
        }
        File target = resolveUniqueChild(destDir, src.getName());
        if (src.renameTo(target)) {
            if (cb != null) {
                long[] st = target.isDirectory() ? countItemsAndBytes(java.util.Collections.singletonList(target))
                        : new long[]{target.length(), 1L};
                cb.onProgress(target.getName(), (int) st[1], st[0]);
            }
            return 1;
        }
        int copied = copyNode(src, target, cancelled, cb);
        if (cancelled != null && cancelled.get()) {
            throw new IOException("Movimiento cancelado");
        }
        FolderZipper.deleteRecursive(src);
        return copied;
    }

    public static int deleteRecursiveWithProgress(File target, AtomicBoolean cancelled,
                                                  ProgressListener cb) throws IOException {
        if (target == null || !target.exists()) {
            return 0;
        }
        if (cancelled != null && cancelled.get()) {
            throw new IOException("Eliminación cancelada");
        }
        int deleted = 0;
        if (target.isDirectory()) {
            ArrayDeque<File> traverse = new ArrayDeque<>();
            ArrayDeque<File> deleteOrder = new ArrayDeque<>();
            traverse.push(target);
            while (!traverse.isEmpty()) {
                if (cancelled != null && cancelled.get()) {
                    throw new IOException("Eliminación cancelada");
                }
                File cur = traverse.pop();
                deleteOrder.push(cur);
                if (cur.isDirectory()) {
                    File[] children = cur.listFiles();
                    if (children != null) {
                        for (File c : children) {
                            traverse.push(c);
                        }
                    }
                }
            }
            while (!deleteOrder.isEmpty()) {
                if (cancelled != null && cancelled.get()) {
                    throw new IOException("Eliminación cancelada");
                }
                File f = deleteOrder.pop();
                long sz = f.isFile() ? f.length() : 0L;
                String name = f.getName();
                if (f.delete()) {
                    deleted++;
                    if (cb != null) {
                        cb.onProgress(name, 1, sz);
                    }
                }
            }
        } else {
            long sz = target.length();
            String name = target.getName();
            if (target.delete()) {
                deleted++;
                if (cb != null) {
                    cb.onProgress(name, 1, sz);
                }
            }
        }
        return deleted;
    }

    public static long[] folderStats(File dir) {
        long[] res = new long[]{0L, 0L, 0L}; // [totalBytes, files, dirs]
        if (dir == null || !dir.isDirectory()) {
            return res;
        }
        ArrayDeque<File> stack = new ArrayDeque<>();
        stack.push(dir);
        while (!stack.isEmpty()) {
            File cur = stack.pop();
            File[] children = cur.listFiles();
            if (children == null) continue;
            for (File c : children) {
                if (FolderZipper.shouldSkipName(c.getName())) continue;
                if (c.isDirectory()) {
                    res[2]++;
                    stack.push(c);
                } else if (c.isFile()) {
                    res[1]++;
                    res[0] += c.length();
                }
            }
        }
        return res;
    }

    public static String[] computeMd5Sha256(File file) {
        if (file == null || !file.isFile() || file.length() > MAX_HASH_SIZE) {
            return new String[]{"—", "—"};
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[BUF_SIZE];
            try (FileInputStream fis = new FileInputStream(file)) {
                int n;
                while ((n = fis.read(buf)) >= 0) {
                    if (n > 0) {
                        md5.update(buf, 0, n);
                        sha256.update(buf, 0, n);
                    }
                }
            }
            return new String[]{toHex(md5.digest()), toHex(sha256.digest())};
        } catch (Exception e) {
            return new String[]{"—", "—"};
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v < 16) sb.append('0');
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    public static String formatPermissions(File f) {
        if (f == null) return "---";
        return (f.canRead() ? "r" : "-")
                + (f.canWrite() ? "w" : "-")
                + (f.canExecute() ? "x" : "-");
    }

    public static boolean matchesCategory(File f, String category, long recentCutoffMs) {
        if (category == null || category.isEmpty() || "all".equals(category)) {
            return true;
        }
        if ("recent".equals(category)) {
            return f.isFile() && f.lastModified() >= recentCutoffMs;
        }
        if (f.isDirectory()) {
            return false;
        }
        String lower = f.getName().toLowerCase(Locale.US);
        switch (category) {
            case "image":
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                        || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".heic")
                        || lower.endsWith(".heif") || lower.endsWith(".bmp") || lower.endsWith(".dng")
                        || lower.endsWith(".svg");
            case "video":
                return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv")
                        || lower.endsWith(".webm") || lower.endsWith(".avi") || lower.endsWith(".m4v");
            case "audio":
                return lower.endsWith(".mp3") || lower.endsWith(".flac") || lower.endsWith(".wav")
                        || lower.endsWith(".m4a") || lower.endsWith(".aac") || lower.endsWith(".ogg")
                        || lower.endsWith(".opus");
            case "doc":
                return lower.endsWith(".pdf") || lower.endsWith(".txt") || lower.endsWith(".md")
                        || lower.endsWith(".json") || lower.endsWith(".xml") || lower.endsWith(".csv")
                        || lower.endsWith(".log") || lower.endsWith(".html") || lower.endsWith(".htm")
                        || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".xls")
                        || lower.endsWith(".xlsx");
            case "archive":
                return lower.endsWith(".zip") || lower.endsWith(".tar") || lower.endsWith(".tar.gz")
                        || lower.endsWith(".tgz") || lower.endsWith(".gz") || lower.endsWith(".7z")
                        || lower.endsWith(".rar") || lower.endsWith(".apk");
            default:
                return true;
        }
    }

    public static List<File> searchRecursive(File startDir, String query, String category, int maxResults) {
        List<File> matches = new ArrayList<>();
        if (startDir == null || !startDir.isDirectory()) {
            return matches;
        }
        String q = (query == null) ? "" : query.trim().toLowerCase(Locale.US);
        String cat = (category == null) ? "" : category.trim().toLowerCase(Locale.US);
        long cutoff = System.currentTimeMillis() - (7L * 24L * 60L * 60L * 1000L);

        ArrayDeque<File> queue = new ArrayDeque<>();
        queue.add(startDir);
        while (!queue.isEmpty() && matches.size() < maxResults) {
            File cur = queue.poll();
            File[] children = cur.listFiles();
            if (children == null) continue;
            for (File c : children) {
                String name = c.getName();
                if (FolderZipper.shouldSkipName(name)) continue;
                boolean nameOk = q.isEmpty() || name.toLowerCase(Locale.US).contains(q);
                boolean catOk = matchesCategory(c, cat, cutoff);
                if (nameOk && catOk) {
                    matches.add(c);
                    if (matches.size() >= maxResults) break;
                }
                if (c.isDirectory() && !"Android".equals(name)) {
                    queue.add(c);
                }
            }
        }
        return matches;
    }

    public static String readTextFile(File f, int maxBytes) throws IOException {
        if (f == null || !f.isFile()) {
            throw new IOException("Archivo no encontrado");
        }
        long len = f.length();
        if (len > maxBytes) {
            throw new IOException("El archivo supera el límite de edición (" + (maxBytes / 1024) + " KB)");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) Math.max(256, len));
        byte[] buf = new byte[8192];
        try (FileInputStream fis = new FileInputStream(f)) {
            int n;
            while ((n = fis.read(buf)) >= 0) {
                if (n > 0) baos.write(buf, 0, n);
            }
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static void writeTextFile(File f, String content) throws IOException {
        byte[] data = (content == null ? "" : content).getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(data);
            fos.flush();
        }
    }
}
