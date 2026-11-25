package com.fotospixel.transfer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TransferService extends Service {

    private static final String TAG = "FotosPixel";
    public static final int PREFERRED_PORT = 80;
    public static final int PORT = 8080;
    public static final String ACTION_START = "com.fotospixel.transfer.START";
    public static final String ACTION_STOP = "com.fotospixel.transfer.STOP";
    public static final String ACTION_RESTART = "com.fotospixel.transfer.RESTART";
    public static final String ACTION_CANCEL_TRANSFERS = "com.fotospixel.transfer.CANCEL_TRANSFERS";
    public static final String ACTION_CANCEL_FILE_OPS = "com.fotospixel.transfer.CANCEL_FILE_OPS";

    private static final String PREFS_NAME = "fotospixel_prefs";
    private static final String KEY_USER = "web_user";
    private static final String KEY_PASS = "web_pass";
    private static final String KEY_DESIRED_RUNNING = "server_desired_running";

    private static final String OLD_CHANNEL_ID = "fotospixel_service";
    private static final String CHANNEL_ID = "fotospixel_live_updates";
    private static final String FILE_OP_CHANNEL_ID = "fotospixel_file_ops";
    static final int NOTIF_ID = 1001;
    static final int NOTIF_FILE_OP_ID = 1002;
    private static final int NOTIF_FILE_OP_DONE_ID = 1003;

    private static volatile TransferService activeInstance;
    private volatile boolean stopping = false;

    private static final int COLOR_IDLE = Color.parseColor("#38BDF8");
    private static final int COLOR_UPLOAD = Color.parseColor("#22C55E");
    private static final int COLOR_DOWNLOAD = Color.parseColor("#A855F7");

    private final TransferStats stats = TransferStats.getInstance();
    private final FileOpMonitor fileOps = FileOpMonitor.getInstance();
    private TransferServer server;
    private LocalDnsResponder dnsResponder;
    private ScheduledExecutorService ticker;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private String lastNotifSummary = "";
    private boolean fileOpNotifShowing = false;
    private String lastCompletionShown = "";
    private boolean loggedCapabilities = false;

    public static String getSavedUser(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sp.getString(KEY_USER, TransferServer.DEFAULT_USER);
    }

    public static String getSavedPass(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sp.getString(KEY_PASS, TransferServer.DEFAULT_PASS);
    }

    public static void saveCredentials(Context context, String user, String pass) {
        String u = (user == null) ? "" : user.trim();
        String p = (pass == null) ? "" : pass;
        context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_USER, u)
                .putString(KEY_PASS, p)
                .apply();
        TransferStats.getInstance().setCredentials(u, p);
    }

    public static boolean isServerDesiredRunning(Context context) {
        if (context == null) return true;
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_DESIRED_RUNNING, true);
    }

    public static void setServerDesiredRunning(Context context, boolean running) {
        if (context == null) return;
        context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DESIRED_RUNNING, running)
                .apply();
    }

    public static void notifyStateChangedIfRunning() {
        TransferService inst = activeInstance;
        if (inst != null && !inst.stopping) {
            inst.lastNotifSummary = "";
            inst.refreshNotificationsFromMonitor();
        }
    }

    public static void stopServiceCompletely(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        setServerDesiredRunning(appCtx, false);
        TransferStats.getInstance().cancelAllTransfers();
        FileOpMonitor.getInstance().cancelAll();
        TransferStats.getInstance().setServerState(false, getLocalWifiIp(), PORT);

        TransferService inst = activeInstance;
        if (inst != null) {
            inst.stopServerAndService();
        }
        try {
            appCtx.stopService(new Intent(appCtx, TransferService.class));
        } catch (Exception ignored) {
        }

        final NotificationManager nm = (NotificationManager) appCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIF_ID);
            nm.cancel(NOTIF_FILE_OP_ID);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isServerDesiredRunning(appCtx)) {
                    nm.cancel(NOTIF_ID);
                    nm.cancel(NOTIF_FILE_OP_ID);
                }
            }, 250L);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        activeInstance = this;
        createNotificationChannel();
        if (fileOps.getActiveOps().isEmpty()) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIF_FILE_OP_ID);
            }
        }
        fileOps.setOnChangeListener(this::refreshNotificationsFromMonitor);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent != null) ? intent.getAction() : ACTION_START;
        if (ACTION_STOP.equals(action)) {
            stopServiceCompletely(this);
            return START_NOT_STICKY;
        }
        if (ACTION_CANCEL_TRANSFERS.equals(action)) {
            stats.cancelAllTransfers();
            notifyStateChangedIfRunning();
            return START_STICKY;
        }
        if (ACTION_CANCEL_FILE_OPS.equals(action)) {
            fileOps.cancelAll();
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIF_FILE_OP_ID);
            }
            notifyStateChangedIfRunning();
            return START_STICKY;
        }

        stopping = false;
        activeInstance = this;
        setServerDesiredRunning(this, true);

        String user = getSavedUser(this);
        String pass = getSavedPass(this);
        stats.setCredentials(user, pass);

        String ip = getLocalWifiIp();
        int curPort = (server != null && server.isRunning()) ? server.getBoundPort() : stats.getServerPort();
        stats.setServerState(true, ip, curPort);
        Notification notif = buildNotification(ip, stats.getActiveTransfers());
        logLiveUpdateCapabilities(notif);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIF_ID, notif);
        }

        if (ACTION_RESTART.equals(action)) {
            restartServer(user, pass);
        } else {
            startServer(user, pass);
        }
        return START_STICKY;
    }

    private synchronized void restartServer(String user, String pass) {
        if (stopping) return;
        Log.i(TAG, "Restarting TransferServer (user=" + (user.isEmpty() ? "<open>" : user) + ")");
        if (dnsResponder != null) {
            dnsResponder.stop();
            dnsResponder = null;
        }
        stats.setLocalHost("");
        if (server != null) {
            server.stop();
            server = null;
        }
        lastNotifSummary = "";
        startServer(user, pass);
        if (stopping || server == null || !server.isRunning()) return;
        String ip = getLocalWifiIp();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_ID, buildNotification(ip, stats.getActiveTransfers()));
        }
    }

    private synchronized void startServer(String user, String pass) {
        if (stopping) return;
        if (server == null || !server.isRunning()) {
            try {
                stats.setCredentials(user, pass);
                server = new TransferServer(this, PREFERRED_PORT, user, pass);
                server.start();
                int activePort = server.getBoundPort();
                String ip = getLocalWifiIp();
                stats.setServerState(true, ip, activePort);
                if (dnsResponder == null) {
                    dnsResponder = new LocalDnsResponder(this);
                }
                dnsResponder.start(ip, activePort, host -> {
                    stats.setLocalHost(host);
                    lastNotifSummary = "";
                    notifyStateChangedIfRunning();
                });
                acquireLocks();
            } catch (IOException e) {
                Log.e(TAG, "Failed to start TransferServer", e);
                stats.setServerState(false, getLocalWifiIp(), PORT);
            }
        }

        stats.setOnStateChangedListener(() -> {
            synchronized (TransferService.this) {
                try {
                    if (stopping || server == null || !server.isRunning()) {
                        return;
                    }
                    lastNotifSummary = "";
                    String ip = getLocalWifiIp();
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null) {
                        nm.notify(NOTIF_ID, buildNotification(ip, stats.getActiveTransfers()));
                    }
                } catch (Exception ignored) {
                }
            }
        });

        if (ticker == null || ticker.isShutdown()) {
            ticker = Executors.newSingleThreadScheduledExecutor();
            ticker.scheduleAtFixedRate(() -> {
                synchronized (TransferService.this) {
                    try {
                        if (stopping) return;
                        stats.sampleSpeeds();
                        String ip = getLocalWifiIp();
                        boolean running = server != null && server.isRunning();
                        if (!running) return;
                        int activePort = server.getBoundPort();
                        stats.setServerState(true, ip, activePort);
                        if (dnsResponder != null) {
                            dnsResponder.updateIpIfChanged(ip);
                        }
                        List<TransferStats.ActiveTransfer> actives = stats.getActiveTransfers();
                        List<FileOpMonitor.ActiveFileOp> ops = fileOps.getActiveOps();
                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (nm != null && !stopping) {
                            syncFileOpNotification(nm, ops);
                            if (!actives.isEmpty() || fileOps.getActiveDeleteOp() != null) {
                                lastNotifSummary = "";
                                nm.notify(NOTIF_ID, buildNotification(ip, actives));
                            } else {
                                String summary = ip + "|true|" + stats.getAuthUser() + "|idle";
                                if (!summary.equals(lastNotifSummary)) {
                                    lastNotifSummary = summary;
                                    nm.notify(NOTIF_ID, buildNotification(ip, actives));
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Ticker error", e);
                    }
                }
            }, 500, 500, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void refreshNotificationsFromMonitor() {
        try {
            if (stopping || server == null || !server.isRunning()) {
                return;
            }
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            String ip = getLocalWifiIp();
            lastNotifSummary = "";
            syncFileOpNotification(nm, fileOps.getActiveOps());
            nm.notify(NOTIF_ID, buildNotification(ip, stats.getActiveTransfers()));
        } catch (Exception ignored) {
        }
    }

    private synchronized void stopServerAndService() {
        stopping = true;
        stats.setOnStateChangedListener(null);
        fileOps.setOnChangeListener(null);
        stats.cancelAllTransfers();
        fileOps.cancelAll();
        if (ticker != null) {
            ticker.shutdownNow();
            ticker = null;
        }
        if (dnsResponder != null) {
            dnsResponder.stop();
            dnsResponder = null;
        }
        stats.setLocalHost("");
        if (server != null) {
            server.stop();
            server = null;
        }
        fileOpNotifShowing = false;
        stats.setServerState(false, getLocalWifiIp(), PORT);
        releaseLocks();
        stopForeground(STOP_FOREGROUND_REMOVE);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIF_ID);
            nm.cancel(NOTIF_FILE_OP_ID);
        }
        stopSelf();
    }

    private void acquireLocks() {
        try {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExpressiveWebFiles::TransferWakeLock");
                    wakeLock.setReferenceCounted(false);
                }
            }
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        } catch (Exception e) {
            Log.w(TAG, "WakeLock error: " + e.getMessage());
        }

        try {
            if (wifiLock == null) {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    int wifiMode = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            ? WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                            : WifiManager.WIFI_MODE_FULL_HIGH_PERF;
                    wifiLock = wm.createWifiLock(wifiMode, "ExpressiveWebFiles::WifiLock");
                    wifiLock.setReferenceCounted(false);
                }
            }
            if (wifiLock != null && !wifiLock.isHeld()) {
                wifiLock.acquire();
            }
        } catch (Exception e) {
            Log.w(TAG, "WifiLock error: " + e.getMessage());
        }
    }

    private void releaseLocks() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception ignored) {
        }
        try {
            if (wifiLock != null && wifiLock.isHeld()) {
                wifiLock.release();
            }
        } catch (Exception ignored) {
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                return;
            }
            try {
                nm.deleteNotificationChannel(OLD_CHANNEL_ID);
            } catch (Exception ignored) {
            }
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Transferencias en vivo (Live Updates)",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            ch.setDescription("Muestra en tiempo real los archivos, rutas, borrados y velocidades de transferencia");
            ch.setSound(null, null);
            ch.enableVibration(false);
            nm.createNotificationChannel(ch);

            NotificationChannel opCh = new NotificationChannel(
                    FILE_OP_CHANNEL_ID,
                    "Operaciones de archivos (Copiar/Mover/Borrar)",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            opCh.setDescription("Progreso en vivo al copiar, mover o eliminar carpetas grandes y medios externos");
            opCh.setSound(null, null);
            opCh.enableVibration(false);
            nm.createNotificationChannel(opCh);
        }
    }

    private void syncFileOpNotification(NotificationManager nm, List<FileOpMonitor.ActiveFileOp> ops) {
        if (ops != null && !ops.isEmpty()) {
            fileOpNotifShowing = true;
            nm.notify(NOTIF_FILE_OP_ID, buildFileOpNotification(ops.get(0), ops));
        } else if (fileOpNotifShowing) {
            fileOpNotifShowing = false;
            nm.cancel(NOTIF_FILE_OP_ID);
            String done = fileOps.getRecentCompletionNotice(5000L);
            if (done != null && !done.equals(lastCompletionShown)) {
                lastCompletionShown = done;
                fileOps.clearCompletionNotice();
                ThemeTokens theme = ThemeTokens.read(this);
                Intent openIntent = new Intent(this, MainActivity.class);
                openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent openPi = PendingIntent.getActivity(
                        this, 3, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Notification doneNotif = new Notification.Builder(this, FILE_OP_CHANNEL_ID)
                        .setContentTitle("Operación completada")
                        .setContentText(done)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setColor(theme.primary)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(openPi)
                        .build();
                nm.notify(NOTIF_FILE_OP_DONE_ID, doneNotif);
            }
        }
    }

    private Notification buildFileOpNotification(FileOpMonitor.ActiveFileOp op, List<FileOpMonitor.ActiveFileOp> ops) {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(
                this, 4, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent cancelOpIntent = new Intent(this, TransferActionReceiver.class);
        cancelOpIntent.setAction(ACTION_CANCEL_FILE_OPS);
        PendingIntent cancelOpPi = PendingIntent.getBroadcast(
                this, 14, cancelOpIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        ThemeTokens theme = ThemeTokens.read(this);
        boolean isDel = FileOpMonitor.OP_DELETE.equals(op.opType);
        boolean isMove = FileOpMonitor.OP_MOVE.equals(op.opType);

        int accentColor = isDel ? theme.error : (isMove ? theme.tertiary : theme.primary);
        int iconFgColor = isDel ? theme.onError : (isMove ? theme.onTertiary : theme.onPrimary);
        int trackerMode = isDel ? 3 : 4;
        int smallIcon = isDel ? android.R.drawable.ic_menu_delete : android.R.drawable.stat_sys_download;

        String verb;
        String shortPrefix;
        if (isDel) {
            verb = "Eliminando";
            shortPrefix = "Elim ";
        } else if (isMove) {
            verb = "Moviendo";
            shortPrefix = "Mov ";
        } else if (FileOpMonitor.OP_COMPRESS.equals(op.opType)) {
            verb = "Comprimiendo";
            shortPrefix = "ZIP ";
        } else if (FileOpMonitor.OP_EXTRACT.equals(op.opType)) {
            verb = "Extrayendo";
            shortPrefix = "Ext ";
        } else {
            verb = "Copiando";
            shortPrefix = "Cop ";
        }

        int pct = op.computeProgressPercent();
        boolean indeterminate = (pct < 0);
        int clampedPct = indeterminate ? 0 : pct;

        String title = verb + ": " + op.title + (ops.size() > 1 ? " (+" + (ops.size() - 1) + ")" : "");
        String text = (isDel ? "Borrando archivo: " : "Archivo: ") + op.currentItem;
        String progressLine = (op.totalBytes > 0L)
                ? (TransferStats.formatBytes(op.bytesDone.get()) + " / " + TransferStats.formatBytes(op.totalBytes) + " (" + clampedPct + "%)")
                : (op.itemsDone.get() + (op.totalItems > 0 ? (" / " + op.totalItems) : "") + " elem.");
        String subText = (op.destLabel != null && !op.destLabel.isEmpty())
                ? (op.destLabel + " · " + progressLine)
                : progressLine;
        String chipText = shortPrefix + (indeterminate ? (op.itemsDone.get() + "") : (clampedPct + "%"));

        Notification.Builder b = new Notification.Builder(this, FILE_OP_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSubText(subText)
                .setSmallIcon(smallIcon)
                .setColor(accentColor)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(openPi);

        boolean useLiveActivity = Build.VERSION.SDK_INT >= 36
                && applyExpressiveProgressStyle(b, clampedPct, indeterminate, trackerMode, accentColor, iconFgColor);

        Icon cancelActionIcon = Icon.createWithResource(this, R.drawable.ic_m3_stop);
        if (useLiveActivity) {
            setRequestPromotedOngoing(b, true);
            setShortCriticalText(b, chipText);
            b.addAction(new Notification.Action.Builder(cancelActionIcon, "Cancelar", cancelOpPi).build());
        } else {
            b.setLargeIcon(createTrackerBitmap(trackerMode, accentColor, iconFgColor));
            Notification.BigTextStyle big = new Notification.BigTextStyle();
            big.setBigContentTitle(title);
            StringBuilder body = new StringBuilder();
            body.append(isDel ? "Eliminando: " : "Archivo actual: ").append(op.currentItem).append('\n');
            if (op.destLabel != null && !op.destLabel.isEmpty()) {
                body.append("Ubicación: ").append(op.destLabel).append('\n');
            }
            body.append("Progreso: ").append(progressLine);
            if (op.totalItems > 0) {
                body.append(" · ").append(op.itemsDone.get()).append('/').append(op.totalItems).append(" elem.");
            }
            big.bigText(body.toString());
            b.setProgress(100, clampedPct, indeterminate);
            b.setStyle(big);
            b.addAction(new Notification.Action.Builder(cancelActionIcon, "Cancelar", cancelOpPi).build());
        }
        return b.build();
    }

    private Notification buildNotification(String ip, List<TransferStats.ActiveTransfer> actives) {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(
                this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, TransferActionReceiver.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getBroadcast(
                this, 11, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent cancelIntent = new Intent(this, TransferActionReceiver.class);
        cancelIntent.setAction(ACTION_CANCEL_TRANSFERS);
        PendingIntent cancelPi = PendingIntent.getBroadcast(
                this, 12, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent cancelOpIntent = new Intent(this, TransferActionReceiver.class);
        cancelOpIntent.setAction(ACTION_CANCEL_FILE_OPS);
        PendingIntent cancelOpPi = PendingIntent.getBroadcast(
                this, 13, cancelOpIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        ThemeTokens theme = ThemeTokens.read(this);

        String title;
        String text;
        String subText;
        String chipText;
        int progressPct = 0;
        boolean indeterminate = false;
        boolean active = (actives != null && !actives.isEmpty());
        int trackerMode = 0; // 0=idle, 1=upload, 2=download, 3=delete
        int accentColor = theme.primary;
        int iconFgColor = theme.onPrimary;

        TransferStats.ActiveTransfer primaryTx = active ? actives.get(0) : null;
        FileOpMonitor.ActiveFileOp delOp = fileOps.getActiveDeleteOp();
        long primaryCur = 0L;
        long primarySpeed = 0L;
        String primaryFullPath = "/";

        if (primaryTx != null) {
            boolean isUpload = TransferStats.DIR_UPLOAD.equals(primaryTx.direction);
            trackerMode = isUpload ? 1 : 2;
            accentColor = isUpload ? theme.primary : theme.tertiary;
            iconFgColor = isUpload ? theme.onPrimary : theme.onTertiary;
            primaryCur = primaryTx.bytesTransferred.get();
            primarySpeed = primaryTx.speedBps;
            if (primarySpeed == 0 && primaryCur > 0) {
                primarySpeed = (primaryCur * 1000L) / Math.max(1L, System.currentTimeMillis() - primaryTx.startedAtMs);
            }
            primaryFullPath = "/".equals(primaryTx.folder)
                    ? ("/" + primaryTx.fileName)
                    : (primaryTx.folder + "/" + primaryTx.fileName);
            title = (isUpload ? "↑ " : "↓ ") + primaryTx.fileName
                    + (actives.size() > 1 ? " (+" + (actives.size() - 1) + ")" : "");
            text = TransferStats.formatSpeed(primarySpeed) + " · " + primaryFullPath;
            if (primaryTx.totalBytes > 0) {
                progressPct = (int) Math.min(100L, (primaryCur * 100L) / primaryTx.totalBytes);
                subText = primaryTx.protocol + " · " + TransferStats.formatBytes(primaryCur)
                        + " / " + TransferStats.formatBytes(primaryTx.totalBytes) + " (" + progressPct + "%)";
            } else {
                indeterminate = true;
                subText = primaryTx.protocol + " · " + TransferStats.formatBytes(primaryCur);
            }
            if (delOp != null) {
                subText += " · Eliminando: " + delOp.currentItem;
            }
            chipText = formatCompactChipSpeed(isUpload, primarySpeed);
        } else if (delOp != null) {
            active = true;
            trackerMode = 3;
            accentColor = theme.error;
            iconFgColor = theme.onError;
            int delPct = delOp.computeProgressPercent();
            indeterminate = (delPct < 0);
            progressPct = indeterminate ? 0 : delPct;
            title = "Eliminando archivos (" + delOp.title + ")";
            text = "Borrando: " + delOp.currentItem;
            subText = (delOp.totalBytes > 0L)
                    ? (TransferStats.formatBytes(delOp.bytesDone.get()) + " / " + TransferStats.formatBytes(delOp.totalBytes) + " (" + progressPct + "%)")
                    : (delOp.itemsDone.get() + (delOp.totalItems > 0 ? (" / " + delOp.totalItems) : "") + " elem.");
            chipText = "Elim " + (indeterminate ? (delOp.itemsDone.get() + "") : (progressPct + "%"));
        } else {
            String u = stats.getAuthUser();
            int activePort = stats.getServerPort();
            String host = stats.getLocalHost();
            String primaryUrl = LocalDnsResponder.formatHttpUrl(
                    (host == null || host.isEmpty()) ? ip : host, activePort);
            title = "Expressive Web Files";
            text = "Portal Web · " + primaryUrl;
            subText = u.isEmpty() ? "Acceso abierto (sin contraseña)" : ("Usuario web: " + u);
            chipText = "Activo";
        }

        Notification.Builder b = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSubText(subText)
                .setSmallIcon(trackerMode == 3 ? android.R.drawable.ic_menu_delete : android.R.drawable.stat_sys_upload)
                .setColor(accentColor)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(openPi);

        boolean useLiveActivity = Build.VERSION.SDK_INT >= 36
                && applyExpressiveProgressStyle(b, progressPct, indeterminate, trackerMode, accentColor, iconFgColor);

        Icon powerActionIcon = Icon.createWithResource(this, R.drawable.ic_m3_power);
        Icon stopActionIcon = Icon.createWithResource(this, R.drawable.ic_m3_stop);

        if (useLiveActivity) {
            setRequestPromotedOngoing(b, true);
            setShortCriticalText(b, chipText);
            if (primaryTx == null && delOp != null) {
                b.addAction(new Notification.Action.Builder(stopActionIcon, "Cancelar borrado", cancelOpPi).build());
            }
            b.addAction(new Notification.Action.Builder(powerActionIcon, "Detener", stopPi).build());
        } else {
            // Standard rich Android notification for OS versions without Live Activities (API 26–35)
            b.setLargeIcon(createTrackerBitmap(trackerMode, accentColor, iconFgColor));
            Notification.BigTextStyle big = new Notification.BigTextStyle();
            if (primaryTx != null) {
                boolean isUpload = TransferStats.DIR_UPLOAD.equals(primaryTx.direction);
                big.setBigContentTitle((isUpload ? "Subiendo: " : "Descargando: ") + primaryTx.fileName);
                StringBuilder body = new StringBuilder();
                body.append("Archivo: ").append(primaryTx.fileName).append('\n');
                body.append("Ruta: ").append(primaryFullPath).append('\n');
                body.append("Velocidad: ").append(TransferStats.formatSpeed(primarySpeed))
                        .append(" · ").append(TransferStats.formatBytes(primaryCur));
                if (primaryTx.totalBytes > 0) {
                    body.append(" / ").append(TransferStats.formatBytes(primaryTx.totalBytes))
                            .append(" (").append(progressPct).append("%)");
                }
                if (delOp != null) {
                    body.append("\nEliminando archivo: ").append(delOp.currentItem);
                }
                if (actives.size() > 1) {
                    int extra = Math.min(3, actives.size() - 1);
                    for (int i = 1; i <= extra; i++) {
                        TransferStats.ActiveTransfer ex = actives.get(i);
                        body.append("\n• ").append(ex.fileName)
                                .append(" (").append(TransferStats.formatSpeed(ex.speedBps)).append(")");
                    }
                    if (actives.size() > 4) {
                        body.append("\n+").append(actives.size() - 4).append(" más");
                    }
                }
                big.bigText(body.toString());
                b.setProgress(100, progressPct, indeterminate);
                b.addAction(new Notification.Action.Builder(stopActionIcon, "Detener transferencia", cancelPi).build());
                b.addAction(new Notification.Action.Builder(powerActionIcon, "Apagar servidor", stopPi).build());
            } else if (delOp != null) {
                big.setBigContentTitle("Eliminando: " + delOp.title);
                StringBuilder body = new StringBuilder();
                body.append("Borrando ahora: ").append(delOp.currentItem).append('\n');
                body.append("Progreso: ").append(subText);
                if (delOp.destLabel != null && !delOp.destLabel.isEmpty()) {
                    body.append('\n').append("Ubicación: ").append(delOp.destLabel);
                }
                big.bigText(body.toString());
                b.setProgress(100, progressPct, indeterminate);
                b.addAction(new Notification.Action.Builder(stopActionIcon, "Cancelar borrado", cancelOpPi).build());
                b.addAction(new Notification.Action.Builder(powerActionIcon, "Apagar servidor", stopPi).build());
            } else {
                String u = stats.getAuthUser();
                int activePort = stats.getServerPort();
                String host = stats.getLocalHost();
                String friendlyUrl = LocalDnsResponder.formatHttpUrl(host, activePort);
                String ipUrl = LocalDnsResponder.formatHttpUrl(ip, activePort);
                b.setContentTitle("Expressive Web Files · En línea");
                big.setBigContentTitle("Expressive Web Files · Servidor activo");
                StringBuilder body = new StringBuilder();
                if (!friendlyUrl.isEmpty()) {
                    body.append("Portal Web: ").append(friendlyUrl).append('\n');
                    body.append("IP alternativa: ").append(ipUrl).append('\n');
                } else {
                    body.append("Portal Web: ").append(ipUrl).append('\n');
                }
                body.append("Acceso: ").append(u.isEmpty() ? "Abierto (sin contraseña)" : ("Usuario \"" + u + "\"")).append('\n');
                body.append("Sesión: ↑ ").append(TransferStats.formatBytes(stats.getTotalUploadBytes()))
                        .append(" · ↓ ").append(TransferStats.formatBytes(stats.getTotalDownloadBytes())).append('\n');
                body.append("Listo para recibir carpetas y archivos.");
                big.bigText(body.toString());
                b.addAction(new Notification.Action.Builder(powerActionIcon, "Detener servidor", stopPi).build());
            }
            b.setStyle(big);
        }

        return b.build();
    }

    private void logLiveUpdateCapabilities(Notification notif) {
        if (loggedCapabilities) {
            return;
        }
        loggedCapabilities = true;
        boolean canPromote = canPostPromotedNotifications(this);
        boolean hasPromotable = false;
        try {
            Method m = Notification.class.getMethod("hasPromotableCharacteristics");
            Object res = m.invoke(notif);
            if (res instanceof Boolean) {
                hasPromotable = (Boolean) res;
            }
        } catch (Exception e) {
            Log.d(TAG, "hasPromotableCharacteristics unavailable: " + e.getMessage());
        }
        Log.i(TAG, "LiveUpdate capabilities: sdk=" + Build.VERSION.SDK_INT
                + " canPostPromoted=" + canPromote
                + " hasPromotableCharacteristics=" + hasPromotable);
    }

    public static boolean canPostPromotedNotifications(Context context) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                return false;
            }
            Method m = NotificationManager.class.getMethod("canPostPromotedNotifications");
            Object res = m.invoke(nm);
            return (res instanceof Boolean) && (Boolean) res;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            Log.w(TAG, "canPostPromotedNotifications error: " + e.getMessage());
            return false;
        }
    }

    private static void setRequestPromotedOngoing(Notification.Builder b, boolean enabled) {
        Bundle extras = new Bundle();
        extras.putBoolean("android.requestPromotedOngoing", enabled);
        b.addExtras(extras);
        try {
            Method m = Notification.Builder.class.getMethod("setRequestPromotedOngoing", boolean.class);
            m.invoke(b, enabled);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            Log.w(TAG, "setRequestPromotedOngoing error: " + e.getMessage());
        }
    }

    private static void setShortCriticalText(Notification.Builder b, String text) {
        try {
            Method m;
            try {
                m = Notification.Builder.class.getMethod("setShortCriticalText", String.class);
            } catch (NoSuchMethodException e) {
                m = Notification.Builder.class.getMethod("setShortCriticalText", CharSequence.class);
            }
            m.invoke(b, text);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            Log.w(TAG, "setShortCriticalText error: " + e.getMessage());
        }
    }

    private static boolean applyExpressiveProgressStyle(Notification.Builder b, int progress, boolean indeterminate,
                                                        int trackerMode, int accentColor, int iconFgColor) {
        try {
            Class<?> psClass = Class.forName("android.app.Notification$ProgressStyle");
            Object ps = psClass.getDeclaredConstructor().newInstance();
            psClass.getMethod("setStyledByProgress", boolean.class).invoke(ps, true);
            if (indeterminate) {
                psClass.getMethod("setProgressIndeterminate", boolean.class).invoke(ps, true);
            } else {
                psClass.getMethod("setProgress", int.class).invoke(ps, Math.max(0, Math.min(100, progress)));
            }

            try {
                Class<?> segClass = Class.forName("android.app.Notification$ProgressStyle$Segment");
                Object seg = segClass.getConstructor(int.class).newInstance(100);
                segClass.getMethod("setColor", int.class).invoke(seg, accentColor);
                psClass.getMethod("addProgressSegment", segClass).invoke(ps, seg);
            } catch (Exception segEx) {
                Log.d(TAG, "ProgressStyle.Segment skipped: " + segEx.getMessage());
            }

            try {
                Icon tracker = Icon.createWithBitmap(createTrackerBitmap(trackerMode, accentColor, iconFgColor));
                psClass.getMethod("setProgressTrackerIcon", Icon.class).invoke(ps, tracker);
            } catch (Exception iconEx) {
                Log.d(TAG, "ProgressStyle.TrackerIcon skipped: " + iconEx.getMessage());
            }

            b.setStyle((Notification.Style) ps);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (Exception e) {
            Log.w(TAG, "ProgressStyle error: " + e.getMessage());
            return false;
        }
    }

    private static Bitmap createTrackerBitmap(int mode, int accentColor, int iconFgColor) {
        int size = 64;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(accentColor);
        c.drawCircle(size / 2f, size / 2f, size * 0.45f, bg);

        Paint fg = new Paint(Paint.ANTI_ALIAS_FLAG);
        fg.setColor(iconFgColor);
        fg.setStyle(Paint.Style.STROKE);
        fg.setStrokeWidth(6.5f);
        fg.setStrokeCap(Paint.Cap.ROUND);
        fg.setStrokeJoin(Paint.Join.ROUND);

        Path p = new Path();
        if (mode == 0) {
            p.moveTo(22f, 38f);
            p.lineTo(32f, 22f);
            p.lineTo(42f, 38f);
            c.drawPath(p, fg);
        } else if (mode == 1) {
            p.moveTo(32f, 46f);
            p.lineTo(32f, 18f);
            p.moveTo(20f, 30f);
            p.lineTo(32f, 18f);
            p.lineTo(44f, 30f);
            c.drawPath(p, fg);
        } else if (mode == 2) {
            p.moveTo(32f, 18f);
            p.lineTo(32f, 46f);
            p.moveTo(20f, 34f);
            p.lineTo(32f, 46f);
            p.lineTo(44f, 34f);
            c.drawPath(p, fg);
        } else if (mode == 3) {
            // Delete X / trash emblem
            p.moveTo(22f, 22f);
            p.lineTo(42f, 42f);
            p.moveTo(42f, 22f);
            p.lineTo(22f, 42f);
            c.drawPath(p, fg);
        } else {
            // Copy / move chevron right
            p.moveTo(18f, 32f);
            p.lineTo(46f, 32f);
            p.moveTo(34f, 20f);
            p.lineTo(46f, 32f);
            p.lineTo(34f, 44f);
            c.drawPath(p, fg);
        }
        return bmp;
    }

    private static String formatCompactChipSpeed(boolean isUp, long bps) {
        String arrow = isUp ? "↑" : "↓";
        if (bps < 1024L) {
            return arrow + bps + "B";
        } else if (bps < 1024L * 1024L) {
            long kb = Math.max(1L, Math.round(bps / 1024.0));
            return arrow + kb + "KB";
        } else if (bps < 1024L * 1024L * 1024L) {
            double mb = bps / (1024.0 * 1024.0);
            if (mb < 10.0) {
                return String.format(Locale.US, "%s%.1fMB", arrow, mb);
            }
            return String.format(Locale.US, "%s%.0fMB", arrow, mb);
        } else {
            double gb = bps / (1024.0 * 1024.0 * 1024.0);
            return String.format(Locale.US, "%s%.1fGB", arrow, gb);
        }
    }

    public static String getLocalWifiIp() {
        String fallback = null;
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return "127.0.0.1";
            for (NetworkInterface nif : Collections.list(ifaces)) {
                if (!nif.isUp() || nif.isLoopback()) continue;
                String name = nif.getName();
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (name != null && (name.startsWith("wlan") || name.startsWith("ap") || name.startsWith("eth"))) {
                            return ip;
                        }
                        if (fallback == null) {
                            fallback = ip;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return (fallback != null) ? fallback : "127.0.0.1";
    }

    @Override
    public void onDestroy() {
        stopping = true;
        if (activeInstance == this) {
            activeInstance = null;
        }
        stats.setOnStateChangedListener(null);
        fileOps.setOnChangeListener(null);
        stats.cancelAllTransfers();
        fileOps.cancelAll();
        if (ticker != null) {
            ticker.shutdownNow();
            ticker = null;
        }
        if (dnsResponder != null) {
            dnsResponder.stop();
            dnsResponder = null;
        }
        stats.setLocalHost("");
        if (server != null) {
            server.stop();
            server = null;
        }
        releaseLocks();
        stats.setServerState(false, getLocalWifiIp(), PORT);
        stopForeground(STOP_FOREGROUND_REMOVE);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIF_ID);
            nm.cancel(NOTIF_FILE_OP_ID);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
