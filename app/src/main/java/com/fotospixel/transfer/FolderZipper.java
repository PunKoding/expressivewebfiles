package com.fotospixel.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * High-speed recursive archive engine supporting ZIP and TAR.GZ packing,
 * extraction (with Zip-Slip traversal protection), and archive listing.
 */
public final class FolderZipper {

    private static final int COPY_BUF_SIZE = 65536;

    public static final class ZipResult {
        public final int filesCount;
        public final long bytesWritten;

        public ZipResult(int filesCount, long bytesWritten) {
            this.filesCount = filesCount;
            this.bytesWritten = bytesWritten;
        }
    }

    public static final class ArchiveEntry {
        public final String name;
        public final boolean isDir;
        public final long size;
        public final long compressedSize;

        public ArchiveEntry(String name, boolean isDir, long size, long compressedSize) {
            this.name = name;
            this.isDir = isDir;
            this.size = size;
            this.compressedSize = compressedSize;
        }
    }

    public interface ProgressCallback {
        void onBytes(long deltaBytes, String currentEntry);
    }

    private interface StreamPacker {
        ZipResult pack(OutputStream out) throws IOException;
    }

    private FolderZipper() {
    }

    public static boolean shouldSkipName(String name) {
        return name == null || name.startsWith("._") || name.startsWith(".fp_part_") || ".DS_Store".equals(name);
    }

    public static boolean deleteRecursive(File f) {
        if (f == null) {
            return false;
        }
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) {
                    if (!deleteRecursive(c)) {
                        return false;
                    }
                }
            }
        }
        return f.delete();
    }

    public static int countVisibleChildren(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return 0;
        }
        File[] list = dir.listFiles();
        if (list == null) {
            return 0;
        }
        int c = 0;
        for (File f : list) {
            if (!shouldSkipName(f.getName())) {
                c++;
            }
        }
        return c;
    }

    public static File resolveUniqueZipFile(File folder) {
        File parent = folder.getParentFile();
        if (parent == null) {
            parent = folder;
        }
        String baseName = folder.getName();
        if (baseName == null || baseName.isEmpty()) {
            baseName = "carpeta";
        }
        return resolveUniqueArchiveFile(parent, baseName, ".zip");
    }

    public static File resolveUniqueArchiveFile(File parent, String baseName, String ext) {
        String cleanBase = (baseName == null || baseName.trim().isEmpty()) ? "archivo" : baseName.trim();
        File candidate = new File(parent, cleanBase + ext);
        int idx = 2;
        while (candidate.exists()) {
            candidate = new File(parent, cleanBase + "_" + idx + ext);
            idx++;
        }
        return candidate;
    }

    public static File resolveUniqueExtractDir(File archiveFile) {
        File parent = archiveFile.getParentFile();
        String name = archiveFile.getName();
        String lower = name.toLowerCase(java.util.Locale.US);
        String base;
        if (lower.endsWith(".tar.gz")) {
            base = name.substring(0, name.length() - 7);
        } else if (lower.endsWith(".tgz")) {
            base = name.substring(0, name.length() - 4);
        } else if (lower.endsWith(".zip")) {
            base = name.substring(0, name.length() - 4);
        } else {
            base = name + "_extraido";
        }
        if (base.isEmpty()) base = "extraido";
        File candidate = new File(parent, base);
        int idx = 2;
        while (candidate.exists()) {
            candidate = new File(parent, base + "_" + idx);
            idx++;
        }
        return candidate;
    }

    public static ZipResult zipFolderToFile(File rootFolder, File destZip,
                                            AtomicBoolean cancelled, ProgressCallback cb) throws IOException {
        if (rootFolder == null || !rootFolder.isDirectory()) {
            throw new IOException("La ruta de origen no es una carpeta");
        }
        return zipItemsToFile(Collections.singletonList(rootFolder), destZip, cancelled, cb);
    }

    public static ZipResult zipItemsToFile(final List<File> items, File destZip,
                                           final AtomicBoolean cancelled, final ProgressCallback cb) throws IOException {
        return writeAtomicArchive(destZip, cancelled, out -> zipItemsToStream(items, out, cancelled, cb));
    }

    public static ZipResult packTarGzToFile(final List<File> items, File destTarGz,
                                            final AtomicBoolean cancelled, final ProgressCallback cb) throws IOException {
        return writeAtomicArchive(destTarGz, cancelled, out -> packTarGzToStream(items, out, cancelled, cb));
    }

    private static ZipResult writeAtomicArchive(File destFile, AtomicBoolean cancelled,
                                                StreamPacker packer) throws IOException {
        File parent = destFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino");
        }
        File tempFile = new File(parent, ".fp_part_" + UUID.randomUUID().toString() + ".tmp");
        boolean ok = false;
        try {
            ZipResult res;
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos, COPY_BUF_SIZE)) {
                res = packer.pack(bos);
                bos.flush();
            }
            checkCancelled(cancelled);
            if (destFile.exists()) {
                destFile.delete();
            }
            if (!tempFile.renameTo(destFile)) {
                throw new IOException("No se pudo guardar el archivo comprimido final");
            }
            ok = true;
            return res;
        } finally {
            if (!ok) {
                tempFile.delete();
            }
        }
    }

    public static ZipResult zipFolderToStream(File rootFolder, OutputStream out,
                                              AtomicBoolean cancelled, ProgressCallback cb) throws IOException {
        if (rootFolder == null || !rootFolder.isDirectory()) {
            throw new IOException("La ruta de origen no es una carpeta");
        }
        return zipItemsToStream(Collections.singletonList(rootFolder), out, cancelled, cb);
    }

    public static ZipResult zipItemsToStream(List<File> items, OutputStream out,
                                             AtomicBoolean cancelled, ProgressCallback cb) throws IOException {
        if (items == null || items.isEmpty()) {
            throw new IOException("No hay elementos para comprimir");
        }

        final String[] activeEntry = new String[]{""};
        final CountingOutputStream cos = new CountingOutputStream(out, delta -> {
            if (cb != null && delta > 0) {
                cb.onBytes(delta, activeEntry[0]);
            }
        });

        OutputStream nonClosing = wrapNonClosing(cos);
        ZipOutputStream zos = new ZipOutputStream(nonClosing, StandardCharsets.UTF_8);
        zos.setLevel(Deflater.BEST_SPEED);

        int[] filesCount = new int[]{0};
        byte[] buf = new byte[COPY_BUF_SIZE];

        for (File item : items) {
            checkCancelled(cancelled);
            if (item == null || !item.exists() || shouldSkipName(item.getName())) {
                continue;
            }
            String entryName = item.getName();
            if (entryName == null || entryName.isEmpty()) {
                entryName = "archivo";
            }
            if (item.isDirectory()) {
                writeDirRecursiveZip(item, entryName, zos, buf, filesCount, activeEntry, cancelled);
            } else if (item.isFile()) {
                writeSingleZipFile(item, entryName, zos, buf, filesCount, activeEntry, cancelled);
            }
        }
        checkCancelled(cancelled);

        zos.finish();
        zos.flush();
        cos.flush();
        return new ZipResult(filesCount[0], cos.totalWritten);
    }

    private static void writeSingleZipFile(File file, String entryPath, ZipOutputStream zos,
                                           byte[] buf, int[] filesCount, String[] activeEntry,
                                           AtomicBoolean cancelled) throws IOException {
        activeEntry[0] = entryPath;
        ZipEntry entry = new ZipEntry(entryPath);
        entry.setTime(file.lastModified());
        zos.putNextEntry(entry);
        try (FileInputStream fis = new FileInputStream(file)) {
            int n;
            while ((n = fis.read(buf)) >= 0) {
                checkCancelled(cancelled);
                if (n > 0) {
                    zos.write(buf, 0, n);
                }
            }
        }
        zos.closeEntry();
        filesCount[0]++;
    }

    private static void writeDirRecursiveZip(File dir, String relPrefix, ZipOutputStream zos,
                                             byte[] buf, int[] filesCount, String[] activeEntry,
                                             AtomicBoolean cancelled) throws IOException {
        checkCancelled(cancelled);
        File[] children = sortedChildren(dir);
        boolean wroteAnyChild = false;
        for (File child : children) {
            checkCancelled(cancelled);
            String name = child.getName();
            if (shouldSkipName(name)) {
                continue;
            }
            wroteAnyChild = true;
            String entryPath = relPrefix + "/" + name;
            if (child.isDirectory()) {
                writeDirRecursiveZip(child, entryPath, zos, buf, filesCount, activeEntry, cancelled);
            } else if (child.isFile()) {
                writeSingleZipFile(child, entryPath, zos, buf, filesCount, activeEntry, cancelled);
            }
        }
        if (!wroteAnyChild) {
            activeEntry[0] = relPrefix + "/";
            ZipEntry dirEntry = new ZipEntry(relPrefix + "/");
            dirEntry.setTime(dir.lastModified());
            zos.putNextEntry(dirEntry);
            zos.closeEntry();
        }
    }

    public static List<ArchiveEntry> listZipEntries(File zipFile, int maxEntries) throws IOException {
        List<ArchiveEntry> list = new ArrayList<>();
        try (ZipFile zf = new ZipFile(zipFile, StandardCharsets.UTF_8)) {
            Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements() && list.size() < maxEntries) {
                ZipEntry e = en.nextElement();
                list.add(new ArchiveEntry(e.getName(), e.isDirectory(), Math.max(0L, e.getSize()), Math.max(0L, e.getCompressedSize())));
            }
        }
        return list;
    }

    public static ZipResult extractZip(File zipFile, File destDir,
                                       AtomicBoolean cancelled, ProgressCallback cb) throws IOException {
        if (!destDir.isDirectory() && !destDir.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino");
        }
        String destPrefix = destDir.getCanonicalPath() + File.separator;
        byte[] buf = new byte[COPY_BUF_SIZE];
        int filesCount = 0;
        long bytesWritten = 0L;

        try (FileInputStream fis = new FileInputStream(zipFile);
             BufferedInputStream bis = new BufferedInputStream(fis, COPY_BUF_SIZE);
             ZipInputStream zis = new ZipInputStream(bis, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                checkCancelled(cancelled);
                String name = entry.getName();
                if (name == null || name.isEmpty()) continue;
                File outFile = new File(destDir, name).getCanonicalFile();
                if (!outFile.getPath().startsWith(destPrefix) && !outFile.getPath().equals(destDir.getCanonicalPath())) {
                    throw new SecurityException("Entrada ZIP fuera de destino bloqueada: " + name);
                }
                if (entry.isDirectory()) {
                    if (!outFile.isDirectory() && !outFile.mkdirs()) {
                        throw new IOException("No se pudo crear subcarpeta: " + name);
                    }
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("No se pudo crear carpeta padre: " + name);
                    }
                    try (FileOutputStream fos = new FileOutputStream(outFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos, COPY_BUF_SIZE)) {
                        int n;
                        while ((n = zis.read(buf)) >= 0) {
                            checkCancelled(cancelled);
                            if (n > 0) {
                                bos.write(buf, 0, n);
                                bytesWritten += n;
                                if (cb != null) cb.onBytes(n, name);
                            }
                        }
                    }
                    filesCount++;
                }
                zis.closeEntry();
            }
        }
        return new ZipResult(filesCount, bytesWritten);
    }

    // ---- Pure-Java POSIX USTAR TAR.GZ engine ----

    public static ZipResult packTarGzToStream(List<File> items, OutputStream out,
                                              AtomicBoolean cancelled, ProgressCallback cb) throws IOException {
        if (items == null || items.isEmpty()) {
            throw new IOException("No hay elementos para comprimir");
        }
        final String[] activeEntry = new String[]{""};
        final CountingOutputStream cos = new CountingOutputStream(out, delta -> {
            if (cb != null && delta > 0) cb.onBytes(delta, activeEntry[0]);
        });
        OutputStream nonClosing = wrapNonClosing(cos);
        GZIPOutputStream gzos = new GZIPOutputStream(nonClosing, COPY_BUF_SIZE);
        int[] filesCount = new int[]{0};
        byte[] buf = new byte[COPY_BUF_SIZE];

        for (File item : items) {
            checkCancelled(cancelled);
            if (item == null || !item.exists() || shouldSkipName(item.getName())) continue;
            if (item.isDirectory()) {
                writeTarDirRecursive(item, item.getName(), gzos, buf, filesCount, activeEntry, cancelled);
            } else if (item.isFile()) {
                writeTarFileEntry(item, item.getName(), gzos, buf, filesCount, activeEntry, cancelled);
            }
        }
        // Two 512-byte zero blocks denote end of TAR archive
        gzos.write(new byte[1024]);
        gzos.finish();
        gzos.flush();
        cos.flush();
        return new ZipResult(filesCount[0], cos.totalWritten);
    }

    private static void writeTarDirRecursive(File dir, String relPrefix, OutputStream tarOut,
                                             byte[] buf, int[] filesCount, String[] activeEntry,
                                             AtomicBoolean cancelled) throws IOException {
        checkCancelled(cancelled);
        activeEntry[0] = relPrefix + "/";
        writeTarHeader(tarOut, relPrefix + "/", 0L, dir.lastModified(), true);
        File[] children = sortedChildren(dir);
        for (File child : children) {
            checkCancelled(cancelled);
            if (shouldSkipName(child.getName())) continue;
            String entryPath = relPrefix + "/" + child.getName();
            if (child.isDirectory()) {
                writeTarDirRecursive(child, entryPath, tarOut, buf, filesCount, activeEntry, cancelled);
            } else if (child.isFile()) {
                writeTarFileEntry(child, entryPath, tarOut, buf, filesCount, activeEntry, cancelled);
            }
        }
    }

    private static void writeTarFileEntry(File file, String entryPath, OutputStream tarOut,
                                          byte[] buf, int[] filesCount, String[] activeEntry,
                                          AtomicBoolean cancelled) throws IOException {
        activeEntry[0] = entryPath;
        long size = file.length();
        writeTarHeader(tarOut, entryPath, size, file.lastModified(), false);
        long rem = size;
        try (FileInputStream fis = new FileInputStream(file)) {
            int n;
            while (rem > 0 && (n = fis.read(buf, 0, (int) Math.min(buf.length, rem))) >= 0) {
                checkCancelled(cancelled);
                if (n > 0) {
                    tarOut.write(buf, 0, n);
                    rem -= n;
                }
            }
        }
        int pad = (int) ((512L - (size % 512L)) % 512L);
        if (pad > 0) {
            tarOut.write(new byte[pad]);
        }
        filesCount[0]++;
    }

    private static void writeTarHeader(OutputStream tarOut, String rawPath, long size,
                                       long mtimeMs, boolean isDir) throws IOException {
        byte[] pathBytes = rawPath.getBytes(StandardCharsets.UTF_8);
        String namePart = rawPath;
        String prefixPart = "";
        if (pathBytes.length > 100) {
            // Try splitting into POSIX USTAR prefix (max 155) + name (max 100)
            int splitIdx = rawPath.indexOf('/');
            while (splitIdx > 0) {
                String candPrefix = rawPath.substring(0, splitIdx);
                String candName = rawPath.substring(splitIdx + 1);
                if (candPrefix.getBytes(StandardCharsets.UTF_8).length <= 155
                        && candName.getBytes(StandardCharsets.UTF_8).length <= 100) {
                    prefixPart = candPrefix;
                    namePart = candName;
                    break;
                }
                splitIdx = rawPath.indexOf('/', splitIdx + 1);
            }
            if (prefixPart.isEmpty()) {
                // Support GNU LongName ('L') record when single path component exceeds 100 bytes
                writeTarHeaderRaw(tarOut, "././@LongLink", pathBytes.length + 1L, 0L, (byte) 'L', "");
                tarOut.write(pathBytes);
                tarOut.write(0);
                int pad = (int) ((512L - ((pathBytes.length + 1L) % 512L)) % 512L);
                if (pad > 0) tarOut.write(new byte[pad]);
                namePart = rawPath.substring(0, Math.min(rawPath.length(), 90));
            }
        }
        writeTarHeaderRaw(tarOut, namePart, size, mtimeMs, isDir ? (byte) '5' : (byte) '0', prefixPart);
    }

    private static void writeTarHeaderRaw(OutputStream tarOut, String name, long size, long mtimeMs,
                                          byte typeFlag, String prefix) throws IOException {
        byte[] hdr = new byte[512];
        putAsciiBytes(hdr, 0, 100, name);
        putOctalBytes(hdr, 100, 8, (typeFlag == '5') ? 0755L : 0644L);
        putOctalBytes(hdr, 108, 8, 0L);
        putOctalBytes(hdr, 116, 8, 0L);
        putOctalBytes(hdr, 124, 12, Math.max(0L, size));
        putOctalBytes(hdr, 136, 12, Math.max(0L, mtimeMs / 1000L));
        Arrays.fill(hdr, 148, 156, (byte) ' ');
        hdr[156] = typeFlag;
        putAsciiBytes(hdr, 257, 6, "ustar");
        hdr[263] = '0';
        hdr[264] = '0';
        putAsciiBytes(hdr, 345, 155, prefix);

        int sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += (hdr[i] & 0xFF);
        }
        putOctalBytes(hdr, 148, 7, sum);
        hdr[155] = ' ';
        tarOut.write(hdr);
    }

    public static List<ArchiveEntry> listTarGzEntries(File tarGzFile, int maxEntries) throws IOException {
        List<ArchiveEntry> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(tarGzFile);
             BufferedInputStream bis = new BufferedInputStream(fis, COPY_BUF_SIZE);
             GZIPInputStream gzis = new GZIPInputStream(bis, COPY_BUF_SIZE)) {
            byte[] hdr = new byte[512];
            String pendingLongName = null;
            while (list.size() < maxEntries) {
                int read = readFully(gzis, hdr, 512);
                if (read < 512 || isZeroBlock(hdr)) break;
                byte type = hdr[156];
                long size = parseOctal(hdr, 124, 12);
                String name = parseTarName(hdr);
                if (type == 'L') {
                    byte[] nameData = new byte[(int) Math.min(65536L, size)];
                    readFully(gzis, nameData, nameData.length);
                    skipPadding(gzis, size, nameData.length);
                    int end = nameData.length;
                    while (end > 0 && nameData[end - 1] == 0) end--;
                    pendingLongName = new String(nameData, 0, end, StandardCharsets.UTF_8);
                    continue;
                }
                if (pendingLongName != null) {
                    name = pendingLongName;
                    pendingLongName = null;
                }
                if (type == 'x' || type == 'g') {
                    skipFully(gzis, size + ((512L - (size % 512L)) % 512L));
                    continue;
                }
                boolean isDir = (type == '5') || name.endsWith("/");
                list.add(new ArchiveEntry(name, isDir, size, -1L));
                long toSkip = size + ((512L - (size % 512L)) % 512L);
                if (toSkip > 0) skipFully(gzis, toSkip);
            }
        }
        return list;
    }

    public static ZipResult extractTarGz(File tarGzFile, File destDir,
                                         AtomicBoolean cancelled, ProgressCallback cb) throws IOException {
        if (!destDir.isDirectory() && !destDir.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino");
        }
        String destPrefix = destDir.getCanonicalPath() + File.separator;
        String destRootPath = destDir.getCanonicalPath();
        byte[] buf = new byte[COPY_BUF_SIZE];
        int filesCount = 0;
        long bytesWritten = 0L;

        try (FileInputStream fis = new FileInputStream(tarGzFile);
             BufferedInputStream bis = new BufferedInputStream(fis, COPY_BUF_SIZE);
             GZIPInputStream gzis = new GZIPInputStream(bis, COPY_BUF_SIZE)) {
            byte[] hdr = new byte[512];
            String pendingLongName = null;
            while (true) {
                checkCancelled(cancelled);
                int read = readFully(gzis, hdr, 512);
                if (read < 512 || isZeroBlock(hdr)) break;
                byte type = hdr[156];
                long size = parseOctal(hdr, 124, 12);
                String name = parseTarName(hdr);
                if (type == 'L') {
                    byte[] nameData = new byte[(int) Math.min(65536L, size)];
                    readFully(gzis, nameData, nameData.length);
                    skipPadding(gzis, size, nameData.length);
                    int end = nameData.length;
                    while (end > 0 && nameData[end - 1] == 0) end--;
                    pendingLongName = new String(nameData, 0, end, StandardCharsets.UTF_8);
                    continue;
                }
                if (pendingLongName != null) {
                    name = pendingLongName;
                    pendingLongName = null;
                }
                if (type == 'x' || type == 'g') {
                    skipFully(gzis, size + ((512L - (size % 512L)) % 512L));
                    continue;
                }
                if (name.startsWith("./")) name = name.substring(2);
                if (name.isEmpty()) {
                    skipFully(gzis, size + ((512L - (size % 512L)) % 512L));
                    continue;
                }

                File outFile = new File(destDir, name).getCanonicalFile();
                if (!outFile.getPath().startsWith(destPrefix) && !outFile.getPath().equals(destRootPath)) {
                    throw new SecurityException("Entrada TAR.GZ fuera de destino bloqueada: " + name);
                }

                if (type == '5' || name.endsWith("/")) {
                    if (!outFile.isDirectory() && !outFile.mkdirs()) {
                        throw new IOException("No se pudo crear subcarpeta: " + name);
                    }
                    skipFully(gzis, size + ((512L - (size % 512L)) % 512L));
                } else if (type == '0' || type == 0) {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("No se pudo crear carpeta padre: " + name);
                    }
                    long rem = size;
                    try (FileOutputStream fos = new FileOutputStream(outFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos, COPY_BUF_SIZE)) {
                        while (rem > 0) {
                            checkCancelled(cancelled);
                            int n = gzis.read(buf, 0, (int) Math.min(buf.length, rem));
                            if (n < 0) throw new EOFException("Archivo TAR.GZ truncado");
                            bos.write(buf, 0, n);
                            rem -= n;
                            bytesWritten += n;
                            if (cb != null) cb.onBytes(n, name);
                        }
                    }
                    long pad = (512L - (size % 512L)) % 512L;
                    if (pad > 0) skipFully(gzis, pad);
                    filesCount++;
                } else {
                    skipFully(gzis, size + ((512L - (size % 512L)) % 512L));
                }
            }
        }
        return new ZipResult(filesCount, bytesWritten);
    }

    private static File[] sortedChildren(File dir) {
        File[] children = dir.listFiles();
        if (children == null) return new File[0];
        Arrays.sort(children, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
        return children;
    }

    private static OutputStream wrapNonClosing(final OutputStream cos) {
        return new FilterOutputStream(cos) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                out.flush();
            }
        };
    }

    private static void putAsciiBytes(byte[] dst, int off, int maxLen, String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(maxLen, b.length);
        System.arraycopy(b, 0, dst, off, len);
    }

    private static void putOctalBytes(byte[] dst, int off, int width, long value) {
        String oct = Long.toOctalString(value);
        int digits = width - 1;
        for (int i = 0; i < digits; i++) {
            dst[off + i] = '0';
        }
        int start = Math.max(0, digits - oct.length());
        int srcStart = Math.max(0, oct.length() - digits);
        for (int i = start; i < digits; i++) {
            dst[off + i] = (byte) oct.charAt(srcStart + (i - start));
        }
        dst[off + digits] = 0;
    }

    private static long parseOctal(byte[] buf, int off, int len) {
        long res = 0;
        int end = off + len;
        int i = off;
        while (i < end && (buf[i] == ' ' || buf[i] == 0)) i++;
        while (i < end && buf[i] >= '0' && buf[i] <= '7') {
            res = (res << 3) + (buf[i] - '0');
            i++;
        }
        return res;
    }

    private static String parseTarName(byte[] hdr) {
        String name = parseCString(hdr, 0, 100);
        String prefix = parseCString(hdr, 345, 155);
        return prefix.isEmpty() ? name : (prefix + "/" + name);
    }

    private static String parseCString(byte[] buf, int off, int maxLen) {
        int end = off;
        int limit = off + maxLen;
        while (end < limit && buf[end] != 0) end++;
        return new String(buf, off, end - off, StandardCharsets.UTF_8);
    }

    private static boolean isZeroBlock(byte[] block) {
        for (byte b : block) {
            if (b != 0) return false;
        }
        return true;
    }

    private static int readFully(InputStream in, byte[] dst, int len) throws IOException {
        int got = 0;
        while (got < len) {
            int n = in.read(dst, got, len - got);
            if (n < 0) break;
            got += n;
        }
        return got;
    }

    private static void skipPadding(InputStream in, long size, int alreadyRead) throws IOException {
        long rem = (size - alreadyRead) + ((512L - (size % 512L)) % 512L);
        if (rem > 0) skipFully(in, rem);
    }

    private static void skipFully(InputStream in, long bytes) throws IOException {
        byte[] skipBuf = new byte[4096];
        long rem = bytes;
        while (rem > 0) {
            int n = in.read(skipBuf, 0, (int) Math.min(skipBuf.length, rem));
            if (n < 0) break;
            rem -= n;
        }
    }

    private static void checkCancelled(AtomicBoolean cancelled) throws IOException {
        if (cancelled != null && cancelled.get()) {
            throw new IOException("Operación cancelada por el usuario");
        }
    }

    private interface DeltaListener {
        void onDelta(long bytes);
    }

    private static final class CountingOutputStream extends FilterOutputStream {
        long totalWritten = 0L;
        private final DeltaListener listener;

        CountingOutputStream(OutputStream out, DeltaListener listener) {
            super(out);
            this.listener = listener;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            totalWritten++;
            listener.onDelta(1L);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            if (len > 0) {
                totalWritten += len;
                listener.onDelta(len);
            }
        }
    }
}
