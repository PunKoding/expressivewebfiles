package com.fotospixel.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks active heavy file operations (copy, move, recursive delete, pack, extract)
 * so both notifications / Live Activities and the UI reflect progress in real time,
 * including explicit deletion status.
 */
public final class FileOpMonitor {

    public static final String OP_COPY = "copy";
    public static final String OP_MOVE = "move";
    public static final String OP_DELETE = "delete";
    public static final String OP_COMPRESS = "compress";
    public static final String OP_EXTRACT = "extract";

    public static final class ActiveFileOp {
        public final String id;
        public final String opType;
        public final String title;
        public final String destLabel;
        public volatile String currentItem;
        public final AtomicInteger itemsDone = new AtomicInteger(0);
        public final int totalItems;
        public final AtomicLong bytesDone = new AtomicLong(0L);
        public final long totalBytes;
        public final long startedAtMs;
        public final AtomicBoolean cancelled = new AtomicBoolean(false);
        volatile long lastNotifyNs = 0L;

        ActiveFileOp(String id, String opType, String title, String destLabel,
                     int totalItems, long totalBytes) {
            this.id = id;
            this.opType = opType;
            this.title = title;
            this.destLabel = destLabel;
            this.currentItem = title;
            this.totalItems = Math.max(0, totalItems);
            this.totalBytes = Math.max(0L, totalBytes);
            this.startedAtMs = System.currentTimeMillis();
        }

        public boolean isDelete() {
            return OP_DELETE.equals(opType);
        }

        public int computeProgressPercent() {
            if (totalBytes > 0L) {
                return (int) Math.min(100L, (bytesDone.get() * 100L) / totalBytes);
            }
            if (totalItems > 0) {
                return (int) Math.min(100L, (itemsDone.get() * 100L) / totalItems);
            }
            return -1; // indeterminate
        }
    }

    private static final FileOpMonitor INSTANCE = new FileOpMonitor();

    private final CopyOnWriteArrayList<ActiveFileOp> activeOps = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(1);
    private volatile Runnable onChangeListener;
    private volatile String lastCompletionSummary;
    private volatile long lastCompletionAtMs = 0L;

    private FileOpMonitor() {
    }

    public static FileOpMonitor getInstance() {
        return INSTANCE;
    }

    public void setOnChangeListener(Runnable listener) {
        this.onChangeListener = listener;
    }

    public ActiveFileOp beginOp(String opType, String title, String destLabel,
                                int totalItems, long totalBytes) {
        String id = "op-" + seq.getAndIncrement();
        ActiveFileOp op = new ActiveFileOp(id, opType, title, destLabel, totalItems, totalBytes);
        activeOps.add(op);
        notifyChanged();
        return op;
    }

    public void updateProgress(ActiveFileOp op, String currentItem, int itemsDelta, long bytesDelta) {
        if (op == null) return;
        if (currentItem != null && !currentItem.isEmpty()) {
            op.currentItem = currentItem;
        }
        if (itemsDelta > 0) {
            op.itemsDone.addAndGet(itemsDelta);
        }
        if (bytesDelta > 0L) {
            op.bytesDone.addAndGet(bytesDelta);
        }
        long now = System.nanoTime();
        if (now - op.lastNotifyNs >= 150_000_000L) {
            op.lastNotifyNs = now;
            notifyChanged();
        }
    }

    public void endOp(ActiveFileOp op, boolean success, String completionSummary) {
        if (op != null) {
            activeOps.remove(op);
        }
        if (success && completionSummary != null && !completionSummary.isEmpty()) {
            lastCompletionSummary = completionSummary;
            lastCompletionAtMs = System.currentTimeMillis();
        }
        notifyChanged();
    }

    public void cancelAll() {
        for (ActiveFileOp op : activeOps) {
            op.cancelled.set(true);
        }
        notifyChanged();
    }

    public List<ActiveFileOp> getActiveOps() {
        return new ArrayList<>(activeOps);
    }

    public ActiveFileOp getActiveDeleteOp() {
        for (ActiveFileOp op : activeOps) {
            if (OP_DELETE.equals(op.opType)) {
                return op;
            }
        }
        return null;
    }

    public String getRecentCompletionNotice(long maxAgeMs) {
        String s = lastCompletionSummary;
        if (s == null) return null;
        if (System.currentTimeMillis() - lastCompletionAtMs <= maxAgeMs) {
            return s;
        }
        lastCompletionSummary = null;
        return null;
    }

    public void clearCompletionNotice() {
        lastCompletionSummary = null;
    }

    private void notifyChanged() {
        Runnable l = onChangeListener;
        if (l != null) {
            l.run();
        }
    }
}
