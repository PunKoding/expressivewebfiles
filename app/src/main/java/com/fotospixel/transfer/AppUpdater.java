package com.fotospixel.transfer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-app automatic updater backed by GitHub Releases (PunKoding/expressivewebfiles).
 */
public final class AppUpdater {

    public static final String GITHUB_OWNER = "PunKoding";
    public static final String GITHUB_REPO = "expressivewebfiles";
    public static final String LATEST_RELEASE_URL =
            "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
    public static final String ACTION_INSTALL_STATUS =
            "com.fotospixel.transfer.ACTION_UPDATE_INSTALL_STATUS";

    private static final String PREFS = "fotospixel_updater";
    private static final String KEY_AUTO_CHECK = "auto_check";
    private static final String KEY_LAST_CHECK_MS = "last_check_ms";
    private static final String KEY_SKIPPED_VERSION = "skipped_version";
    private static final long AUTO_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L;
    private static final String UPDATE_FILE_PREFIX = "update_";

    private AppUpdater() {}

    public static final class ReleaseInfo {
        public final String versionName;
        public final String tagName;
        public final String releaseName;
        public final String changelog;
        public final String apkUrl;
        public final String apkName;
        public final long apkSize;
        public final String sha256Url;
        public final String htmlUrl;

        public ReleaseInfo(String versionName, String tagName, String releaseName, String changelog,
                           String apkUrl, String apkName, long apkSize, String sha256Url, String htmlUrl) {
            this.versionName = versionName;
            this.tagName = tagName;
            this.releaseName = releaseName;
            this.changelog = changelog;
            this.apkUrl = apkUrl;
            this.apkName = apkName;
            this.apkSize = apkSize;
            this.sha256Url = sha256Url;
            this.htmlUrl = htmlUrl;
        }
    }

    public static final class CheckResult {
        public enum Status {
            UPDATE_AVAILABLE,
            UP_TO_DATE,
            NO_RELEASE,
            ERROR
        }

        public final Status status;
        public final ReleaseInfo release;
        public final String message;

        private CheckResult(Status status, ReleaseInfo release, String message) {
            this.status = status;
            this.release = release;
            this.message = message;
        }

        public static CheckResult available(ReleaseInfo release) {
            return new CheckResult(Status.UPDATE_AVAILABLE, release, "Nueva versión v" + release.versionName);
        }

        public static CheckResult upToDate(ReleaseInfo release) {
            return new CheckResult(Status.UP_TO_DATE, release, "Ya tienes la última versión");
        }

        public static CheckResult noRelease(String message) {
            return new CheckResult(Status.NO_RELEASE, null, message);
        }

        public static CheckResult error(String message) {
            return new CheckResult(Status.ERROR, null, message);
        }
    }

    public interface DownloadProgressListener {
        void onProgress(long downloadedBytes, long totalBytes);
    }

    public enum InstallResult {
        SESSION_COMMITTED,
        NEEDS_PERMISSION,
        FAILED
    }

    public static boolean isAutoCheckEnabled(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_AUTO_CHECK, true);
    }

    public static void setAutoCheckEnabled(Context ctx, boolean enabled) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_AUTO_CHECK, enabled)
                .apply();
    }

    public static boolean shouldAutoCheck(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_AUTO_CHECK, true)) {
            return false;
        }
        long lastCheck = prefs.getLong(KEY_LAST_CHECK_MS, 0L);
        return (System.currentTimeMillis() - lastCheck) >= AUTO_CHECK_INTERVAL_MS;
    }

    public static void markSkippedVersion(Context ctx, String versionName) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SKIPPED_VERSION, versionName == null ? "" : versionName)
                .apply();
    }

    public static String getSkippedVersion(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SKIPPED_VERSION, "");
    }

    /**
     * Compares two SemVer strings (stripping leading 'v'/'V' and any '-...' pre-release suffix).
     * Returns > 0 if v1 > v2, 0 if equal, < 0 if v1 < v2.
     */
    public static int compareVersions(String v1, String v2) {
        int[] p1 = parseVersionParts(v1);
        int[] p2 = parseVersionParts(v2);
        for (int i = 0; i < 3; i++) {
            if (p1[i] != p2[i]) {
                return Integer.compare(p1[i], p2[i]);
            }
        }
        return 0;
    }

    private static int[] parseVersionParts(String raw) {
        int[] out = new int[]{0, 0, 0};
        if (raw == null) return out;
        String clean = raw.trim();
        if (clean.startsWith("v") || clean.startsWith("V")) {
            clean = clean.substring(1).trim();
        }
        int dash = clean.indexOf('-');
        if (dash >= 0) {
            clean = clean.substring(0, dash);
        }
        int plus = clean.indexOf('+');
        if (plus >= 0) {
            clean = clean.substring(0, plus);
        }
        String[] parts = clean.split("\\.");
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            try {
                out[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {
                out[i] = 0;
            }
        }
        return out;
    }

    private static String normalizeVersion(String tag) {
        if (tag == null) return "0.0.0";
        String clean = tag.trim();
        if (clean.startsWith("v") || clean.startsWith("V")) {
            clean = clean.substring(1).trim();
        }
        return clean;
    }

    public static CheckResult checkLatestRelease(Context ctx) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(LATEST_RELEASE_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "ExpressiveWebFiles/" + AppVersion.NAME);

            int code = conn.getResponseCode();
            if (code == 404) {
                recordCheckTime(ctx);
                return CheckResult.noRelease("Aún no hay ningún release publicado en GitHub");
            }
            if (code == 403) {
                return CheckResult.error("Límite de consultas de GitHub alcanzado. Inténtalo más tarde.");
            }
            if (code != 200) {
                return CheckResult.error("Error consultando GitHub (HTTP " + code + ")");
            }

            recordCheckTime(ctx);
            String body = readUtf8(conn.getInputStream(), 512 * 1024);
            JSONObject json = new JSONObject(body);

            String tagName = json.optString("tag_name", "").trim();
            String versionName = normalizeVersion(tagName);
            String releaseName = json.optString("name", tagName).trim();
            String changelog = json.optString("body", "").trim();
            String htmlUrl = json.optString("html_url", "").trim();

            String apkUrl = "";
            String apkName = "";
            long apkSize = 0L;
            String sha256Url = "";

            JSONArray assets = json.optJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.optJSONObject(i);
                    if (asset == null) continue;
                    String name = asset.optString("name", "");
                    String lower = name.toLowerCase(Locale.US);
                    String dlUrl = asset.optString("browser_download_url", "");
                    if (apkUrl.isEmpty() && lower.endsWith(".apk") && !dlUrl.isEmpty()) {
                        apkUrl = dlUrl;
                        apkName = name;
                        apkSize = asset.optLong("size", 0L);
                    } else if (sha256Url.isEmpty() && (lower.endsWith(".sha256") || lower.endsWith(".sha256sum")) && !dlUrl.isEmpty()) {
                        sha256Url = dlUrl;
                    }
                }
            }

            if (apkUrl.isEmpty()) {
                return CheckResult.noRelease("El último release no contiene un archivo APK");
            }

            ReleaseInfo info = new ReleaseInfo(
                    versionName, tagName, releaseName, changelog, apkUrl, apkName, apkSize, sha256Url, htmlUrl);
            if (compareVersions(versionName, AppVersion.NAME) > 0) {
                return CheckResult.available(info);
            }
            return CheckResult.upToDate(info);
        } catch (Exception e) {
            return CheckResult.error("No se pudo comprobar actualizaciones: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static File downloadApk(Context ctx, ReleaseInfo info,
                                   DownloadProgressListener listener, AtomicBoolean cancelled) throws IOException {
        cleanOldUpdates(ctx);
        String safeVer = info.versionName.replaceAll("[^0-9A-Za-z._-]", "_");
        File outFile = new File(ctx.getCacheDir(), UPDATE_FILE_PREFIX + safeVer + ".apk");
        File tmpFile = new File(ctx.getCacheDir(), UPDATE_FILE_PREFIX + safeVer + ".apk.tmp");
        if (tmpFile.exists()) tmpFile.delete();
        if (outFile.exists()) outFile.delete();

        HttpURLConnection conn = openWithRedirects(info.apkUrl, 5);
        try {
            int code = conn.getResponseCode();
            if (code != 200) {
                throw new IOException("HTTP " + code + " al descargar el APK");
            }
            long total = conn.getContentLengthLong();
            if (total <= 0L && info.apkSize > 0L) {
                total = info.apkSize;
            }
            byte[] buf = new byte[65536];
            long downloaded = 0L;
            try (InputStream in = new BufferedInputStream(conn.getInputStream(), 65536);
                 FileOutputStream fos = new FileOutputStream(tmpFile)) {
                int r;
                while ((r = in.read(buf)) != -1) {
                    if (cancelled != null && cancelled.get()) {
                        throw new IOException("Descarga cancelada");
                    }
                    fos.write(buf, 0, r);
                    downloaded += r;
                    if (listener != null) {
                        listener.onProgress(downloaded, total);
                    }
                }
                fos.getFD().sync();
            }
        } catch (IOException e) {
            tmpFile.delete();
            throw e;
        } finally {
            conn.disconnect();
        }

        if (cancelled != null && cancelled.get()) {
            tmpFile.delete();
            throw new IOException("Descarga cancelada");
        }

        if (info.sha256Url != null && !info.sha256Url.isEmpty()) {
            String expected = fetchExpectedSha256(info.sha256Url);
            if (!expected.isEmpty()) {
                String actual = computeSha256(tmpFile);
                if (!expected.equalsIgnoreCase(actual)) {
                    tmpFile.delete();
                    throw new IOException("Error de integridad SHA-256");
                }
            }
        }

        if (!tmpFile.renameTo(outFile)) {
            tmpFile.delete();
            throw new IOException("No se pudo guardar el archivo APK descargado");
        }
        return outFile;
    }

    public static InstallResult installApk(Activity activity, File apkFile) {
        if (activity == null || apkFile == null || !apkFile.isFile()) {
            return InstallResult.FAILED;
        }
        if (Build.VERSION.SDK_INT >= 26 && !activity.getPackageManager().canRequestPackageInstalls()) {
            Intent permIntent = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(permIntent);
            return InstallResult.NEEDS_PERMISSION;
        }

        PackageInstaller installer = activity.getPackageManager().getPackageInstaller();
        PackageInstaller.Session session = null;
        try {
            PackageInstaller.SessionParams params =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setSize(apkFile.length());
            if (Build.VERSION.SDK_INT >= 31) {
                params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
            }
            int sessionId = installer.createSession(params);
            session = installer.openSession(sessionId);

            try (InputStream in = new BufferedInputStream(new FileInputStream(apkFile), 65536);
                 OutputStream out = session.openWrite("update.apk", 0, apkFile.length())) {
                byte[] buf = new byte[65536];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
                session.fsync(out);
            }

            Intent cbIntent = new Intent(activity, UpdateInstallReceiver.class);
            cbIntent.setAction(ACTION_INSTALL_STATUS);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            PendingIntent pending = PendingIntent.getBroadcast(activity, sessionId, cbIntent, flags);
            session.commit(pending.getIntentSender());
            session.close();
            session = null;
            return InstallResult.SESSION_COMMITTED;
        } catch (Exception e) {
            if (session != null) {
                try {
                    session.abandon();
                } catch (Exception ignored) {
                }
            }
            return InstallResult.FAILED;
        }
    }

    public static void cleanOldUpdates(Context ctx) {
        File cacheDir = ctx.getCacheDir();
        if (cacheDir == null) return;
        File[] files = cacheDir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String n = f.getName();
            if (n.startsWith(UPDATE_FILE_PREFIX) && (n.endsWith(".apk") || n.endsWith(".apk.tmp"))) {
                f.delete();
            }
        }
    }

    private static void recordCheckTime(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_CHECK_MS, System.currentTimeMillis())
                .apply();
    }

    private static HttpURLConnection openWithRedirects(String urlStr, int maxRedirects) throws IOException {
        String current = urlStr;
        for (int i = 0; i <= maxRedirects; i++) {
            HttpURLConnection conn = (HttpURLConnection) new URL(current).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "ExpressiveWebFiles/" + AppVersion.NAME);
            int code = conn.getResponseCode();
            if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                String loc = conn.getHeaderField("Location");
                conn.disconnect();
                if (loc == null || loc.isEmpty()) {
                    throw new IOException("Redirección vacía al descargar");
                }
                current = new URL(new URL(current), loc).toString();
                continue;
            }
            return conn;
        }
        throw new IOException("Demasiadas redirecciones al descargar el APK");
    }

    private static String fetchExpectedSha256(String shaUrl) {
        HttpURLConnection conn = null;
        try {
            conn = openWithRedirects(shaUrl, 5);
            if (conn.getResponseCode() != 200) return "";
            String text = readUtf8(conn.getInputStream(), 4096).trim();
            String token = text.split("\\s+")[0].trim();
            if (token.matches("[0-9a-fA-F]{64}")) {
                return token;
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return "";
    }

    private static String computeSha256(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[65536];
            try (InputStream in = new BufferedInputStream(new FileInputStream(file), 65536)) {
                int r;
                while ((r = in.read(buf)) != -1) {
                    md.update(buf, 0, r);
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format(Locale.US, "%02x", b & 0xff));
            }
            return sb.toString();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error calculando SHA-256", e);
        }
    }

    private static String readUtf8(InputStream in, int maxBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int total = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            if (total + r > maxBytes) {
                baos.write(buf, 0, maxBytes - total);
                break;
            }
            baos.write(buf, 0, r);
            total += r;
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }
}
