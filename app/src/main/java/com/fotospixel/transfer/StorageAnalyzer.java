package com.fotospixel.transfer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hierarchical disk-usage scanner (DaisyDisk / SquirrelDisk engine) for internal
 * storage and mounted SD / USB OTG volumes.
 */
public final class StorageAnalyzer {

    public static final class Node {
        public final File file;
        public final String name;
        public final boolean isDir;
        public long size;
        public int fileCount;
        public int dirCount;
        public Node parent;
        public final List<Node> children = new ArrayList<>();
        public int colorIndex;

        public Node(File file, String name, boolean isDir, Node parent) {
            this.file = file;
            this.name = name;
            this.isDir = isDir;
            this.parent = parent;
        }
    }

    public interface PathMapper {
        String mapPath(File file);
    }

    public interface ProgressCallback {
        void onProgress(long scannedBytes, int files, int dirs, String currentPath);
    }

    private static final Comparator<Node> NODE_DESC = new Comparator<Node>() {
        @Override
        public int compare(Node a, Node b) {
            int cmp = Long.compare(b.size, a.size);
            if (cmp != 0) return cmp;
            if (a.isDir != b.isDir) return a.isDir ? -1 : 1;
            return a.name.compareToIgnoreCase(b.name);
        }
    };

    private StorageAnalyzer() {}

    /**
     * Scans rootDir recursively, skipping temporary transfer artifacts and
     * preventing symlink cycles via canonical path tracking.
     */
    public static Node scanTree(File rootDir, AtomicBoolean cancelled, ProgressCallback cb) {
        if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory()) {
            return new Node(rootDir, rootDir != null ? rootDir.getName() : "Almacenamiento", true, null);
        }
        String rootName = rootDir.getName();
        if (rootName == null || rootName.isEmpty()) rootName = "Almacenamiento";
        Node root = new Node(rootDir, rootName, true, null);
        Set<String> visitedDirs = new HashSet<>();
        long[] stats = new long[3]; // [0]=bytes, [1]=files, [2]=dirs
        long[] lastNotifyMs = new long[1];

        String rootCanon = safeCanonical(rootDir);
        if (rootCanon != null) visitedDirs.add(rootCanon);

        scanChildren(root, rootCanon, visitedDirs, stats, lastNotifyMs, cancelled, cb);

        if (cb != null && (cancelled == null || !cancelled.get())) {
            cb.onProgress(root.size, root.fileCount, root.dirCount, rootDir.getAbsolutePath());
        }
        return root;
    }

    private static void scanChildren(
            Node dirNode,
            String dirCanon,
            Set<String> visitedDirs,
            long[] stats,
            long[] lastNotifyMs,
            AtomicBoolean cancelled,
            ProgressCallback cb) {
        if (cancelled != null && cancelled.get()) return;

        File[] entries = dirNode.file.listFiles();
        if (entries == null || entries.length == 0) return;

        for (File entry : entries) {
            if (cancelled != null && cancelled.get()) return;
            String name = entry.getName();
            if (name == null || name.isEmpty() || ".".equals(name) || "..".equals(name)) continue;
            if (name.startsWith(".fotospixel_")) continue;

            // Skip symlinks that resolve outside expected canonical child path.
            String entryCanon = safeCanonical(entry);
            if (dirCanon != null && entryCanon != null) {
                String expected = dirCanon.endsWith(File.separator)
                        ? dirCanon + name
                        : dirCanon + File.separator + name;
                if (!entryCanon.equals(expected)) {
                    continue;
                }
            }

            if (entry.isDirectory()) {
                if (entryCanon != null && !visitedDirs.add(entryCanon)) {
                    continue;
                }
                Node childDir = new Node(entry, name, true, dirNode);
                stats[2]++;
                scanChildren(childDir, entryCanon, visitedDirs, stats, lastNotifyMs, cancelled, cb);
                dirNode.children.add(childDir);
                dirNode.size += childDir.size;
                dirNode.fileCount += childDir.fileCount;
                dirNode.dirCount += 1 + childDir.dirCount;
                maybeNotify(stats, lastNotifyMs, entry.getAbsolutePath(), cb);
            } else if (entry.isFile()) {
                long len = Math.max(0L, entry.length());
                Node childFile = new Node(entry, name, false, dirNode);
                childFile.size = len;
                childFile.fileCount = 1;
                dirNode.children.add(childFile);
                dirNode.size += len;
                dirNode.fileCount += 1;
                stats[0] += len;
                stats[1]++;
                maybeNotify(stats, lastNotifyMs, null, cb);
            }
        }

        sortAndIndex(dirNode);
    }

    private static void maybeNotify(long[] stats, long[] lastNotifyMs, String path, ProgressCallback cb) {
        if (cb == null) return;
        long now = System.currentTimeMillis();
        if (now - lastNotifyMs[0] >= 120L) {
            lastNotifyMs[0] = now;
            cb.onProgress(stats[0], (int) stats[1], (int) stats[2], path != null ? path : "");
        }
    }

    private static void sortAndIndex(Node parent) {
        Collections.sort(parent.children, NODE_DESC);
        for (int i = 0; i < parent.children.size(); i++) {
            parent.children.get(i).colorIndex = i;
        }
    }

    private static String safeCanonical(File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException | SecurityException e) {
            return null;
        }
    }

    /**
     * Removes a deleted node from its parent and subtracts its byte/item totals
     * from all ancestors while preserving descending size order.
     */
    public static boolean removeNodeFromTree(Node target) {
        if (target == null || target.parent == null) return false;
        Node parent = target.parent;
        if (!parent.children.remove(target)) return false;

        long deltaSize = target.size;
        int deltaFiles = target.isDir ? target.fileCount : 1;
        int deltaDirs = target.isDir ? (1 + target.dirCount) : 0;
        target.parent = null;

        Node cur = parent;
        while (cur != null) {
            cur.size = Math.max(0L, cur.size - deltaSize);
            cur.fileCount = Math.max(0, cur.fileCount - deltaFiles);
            cur.dirCount = Math.max(0, cur.dirCount - deltaDirs);
            sortAndIndex(cur);
            cur = cur.parent;
        }
        return true;
    }

    /**
     * Serializes the disk-usage subtree rooted at node to JSON up to maxDepth rings
     * and at most maxChildren explicit children per folder (grouping tiny tail entries
     * into otherSize / otherCount).
     */
    public static String toJson(Node node, int maxDepth, int maxChildren, PathMapper mapper) {
        StringBuilder sb = new StringBuilder(4096);
        appendJson(sb, node, 0, Math.max(1, maxDepth), maxChildren, mapper);
        return sb.toString();
    }

    private static void appendJson(
            StringBuilder sb,
            Node node,
            int depth,
            int maxDepth,
            int maxChildren,
            PathMapper mapper) {
        String mappedPath = mapper != null
                ? mapper.mapPath(node.file)
                : (node.file != null ? node.file.getAbsolutePath() : "");
        if (mappedPath == null) mappedPath = "";

        sb.append('{');
        sb.append("\"name\":\"").append(TransferStats.escapeJson(node.name)).append("\",");
        sb.append("\"path\":\"").append(TransferStats.escapeJson(mappedPath)).append("\",");
        sb.append("\"isDir\":").append(node.isDir).append(',');
        sb.append("\"size\":").append(node.size).append(',');
        sb.append("\"fileCount\":").append(node.fileCount).append(',');
        sb.append("\"dirCount\":").append(node.dirCount).append(',');
        sb.append("\"colorIndex\":").append(node.colorIndex);

        if (node.isDir && depth < maxDepth) {
            int total = node.children.size();
            int limit = maxChildren > 0 ? Math.min(total, maxChildren) : total;
            long otherSize = 0L;
            int otherCount = 0;

            sb.append(",\"children\":[");
            for (int i = 0; i < limit; i++) {
                if (i > 0) sb.append(',');
                appendJson(sb, node.children.get(i), depth + 1, maxDepth, maxChildren, mapper);
            }
            sb.append(']');

            for (int i = limit; i < total; i++) {
                otherSize += node.children.get(i).size;
                otherCount++;
            }
            sb.append(",\"otherSize\":").append(otherSize);
            sb.append(",\"otherCount\":").append(otherCount);
        } else {
            sb.append(",\"hasMore\":").append(node.isDir && !node.children.isEmpty());
        }
        sb.append('}');
    }
}
