package com.fotospixel.transfer;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Enumerates mounted storage volumes: primary internal storage, SD cards,
 * and USB OTG / USB-C mass-storage drives across Android 8–17.
 */
public final class StorageVolumes {

    public static final String KIND_INTERNAL = "internal";
    public static final String KIND_SD = "sd";
    public static final String KIND_USB = "usb";

    public static final class VolumeInfo {
        public final String id;
        public final String label;
        public final String kind;
        public final File rootDir;
        public final long totalBytes;
        public final long freeBytes;
        public final boolean writable;

        public VolumeInfo(String id, String label, String kind, File rootDir,
                          long totalBytes, long freeBytes, boolean writable) {
            this.id = id;
            this.label = label;
            this.kind = kind;
            this.rootDir = rootDir;
            this.totalBytes = totalBytes;
            this.freeBytes = freeBytes;
            this.writable = writable;
        }
    }

    private StorageVolumes() {
    }

    public static List<VolumeInfo> listMounted(Context context) {
        List<VolumeInfo> result = new ArrayList<>();
        Set<String> seenPaths = new LinkedHashSet<>();
        Set<String> usedIds = new LinkedHashSet<>();

        File primaryRoot = canonicalSafe(Environment.getExternalStorageDirectory());
        if (primaryRoot != null) {
            seenPaths.add(primaryRoot.getPath());
            usedIds.add("");
            long[] sp = statVolume(primaryRoot);
            result.add(new VolumeInfo(
                    "",
                    "Almacenamiento interno",
                    KIND_INTERNAL,
                    primaryRoot,
                    sp[0],
                    sp[1],
                    primaryRoot.canWrite()
            ));
        }

        if (context == null) {
            return result;
        }

        Map<String, String> pathToDesc = new HashMap<>();
        List<File> candidates = new ArrayList<>();

        // 1. Enumerate via StorageManager.getStorageVolumes()
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (sm != null) {
                List<StorageVolume> vols = sm.getStorageVolumes();
                if (vols != null) {
                    for (StorageVolume sv : vols) {
                        if (sv == null || sv.isPrimary()) continue;
                        String st = sv.getState();
                        if (!Environment.MEDIA_MOUNTED.equals(st) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(st)) {
                            continue;
                        }
                        File dir = resolveStorageVolumeDir(sv);
                        File canon = canonicalSafe(dir);
                        if (canon != null && canon.isDirectory()) {
                            candidates.add(canon);
                            String desc = null;
                            try {
                                desc = sv.getDescription(context);
                            } catch (Exception ignored) {
                            }
                            if (desc != null && !desc.trim().isEmpty()) {
                                pathToDesc.put(canon.getPath(), desc.trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Fallback via getExternalFilesDirs(null) -> strip /Android/data/<pkg>/files
        try {
            File[] extDirs = context.getExternalFilesDirs(null);
            if (extDirs != null) {
                for (File appDir : extDirs) {
                    if (appDir == null) continue;
                    File root = stripAndroidDataSuffix(appDir);
                    File canon = canonicalSafe(root);
                    if (canon != null && canon.isDirectory()) {
                        candidates.add(canon);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 3. Direct /storage scan for USB OTG / SD mount points not covered above
        File storageDir = new File("/storage");
        File[] children = storageDir.listFiles();
        if (children != null) {
            for (File child : children) {
                String n = child.getName();
                if ("emulated".equals(n) || "self".equals(n) || "enc_emulated".equals(n)) {
                    continue;
                }
                File canon = canonicalSafe(child);
                if (canon != null && canon.isDirectory() && canon.canRead()) {
                    candidates.add(canon);
                }
            }
        }

        for (File dir : candidates) {
            String path = dir.getPath();
            if (!seenPaths.add(path)) continue;
            if (!isReadableMount(dir)) continue;

            String desc = pathToDesc.get(path);
            String kind = classifyVolumeKind(desc, path);
            String label = buildFriendlyLabel(kind, desc, dir);
            String id = makeUniqueId(dir.getName(), usedIds);
            long[] sp = statVolume(dir);
            boolean canWrite = dir.canWrite();
            result.add(new VolumeInfo(id, label, kind, dir, sp[0], sp[1], canWrite));
        }

        return result;
    }

    public static VolumeInfo findById(Context context, String id) {
        String key = (id == null) ? "" : id.trim();
        for (VolumeInfo v : listMounted(context)) {
            if (v.id.equals(key)) {
                return v;
            }
        }
        return null;
    }

    public static VolumeInfo findForFile(Context context, File file) {
        File canon = canonicalSafe(file);
        if (canon == null) return null;
        String path = canon.getPath();
        VolumeInfo best = null;
        for (VolumeInfo v : listMounted(context)) {
            String vp = v.rootDir.getPath();
            if (path.equals(vp) || path.startsWith(vp + File.separator)) {
                if (best == null || vp.length() > best.rootDir.getPath().length()) {
                    best = v;
                }
            }
        }
        return best;
    }

    public static boolean isVolumeRoot(Context context, File file) {
        File canon = canonicalSafe(file);
        if (canon == null) return false;
        String path = canon.getPath();
        for (VolumeInfo v : listMounted(context)) {
            if (v.rootDir.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }

    private static File resolveStorageVolumeDir(StorageVolume sv) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                File d = sv.getDirectory();
                if (d != null && d.isDirectory()) return d;
            } catch (Exception ignored) {
            }
        }
        try {
            Method m = StorageVolume.class.getMethod("getPathFile");
            Object res = m.invoke(sv);
            if (res instanceof File && ((File) res).isDirectory()) {
                return (File) res;
            }
        } catch (Exception ignored) {
        }
        try {
            Method m = StorageVolume.class.getMethod("getPath");
            Object res = m.invoke(sv);
            if (res instanceof String) {
                File f = new File((String) res);
                if (f.isDirectory()) return f;
            }
        } catch (Exception ignored) {
        }
        String uuid = sv.getUuid();
        if (uuid != null && !uuid.isEmpty()) {
            File f = new File("/storage/" + uuid);
            if (f.isDirectory()) return f;
        }
        return null;
    }

    private static File stripAndroidDataSuffix(File appDir) {
        String p = appDir.getAbsolutePath();
        int idx = p.indexOf("/Android/data/");
        if (idx > 0) {
            return new File(p.substring(0, idx));
        }
        return null;
    }

    private static boolean isReadableMount(File dir) {
        if (!dir.isDirectory() || !dir.canRead()) return false;
        try {
            StatFs st = new StatFs(dir.getAbsolutePath());
            return st.getTotalBytes() > 0L;
        } catch (Exception e) {
            return dir.list() != null;
        }
    }

    private static String classifyVolumeKind(String desc, String path) {
        String haystack = ((desc != null ? desc : "") + " " + path).toLowerCase(Locale.US);
        if (haystack.contains("usb") || haystack.contains("otg") || haystack.contains("mass")
                || haystack.contains("pendrive") || haystack.contains("flash")
                || haystack.contains("drive") || haystack.contains("disk")
                || haystack.contains("hdd") || haystack.contains("ssd")
                || haystack.contains("usbhost")) {
            return KIND_USB;
        }
        return KIND_SD;
    }

    private static String buildFriendlyLabel(String kind, String desc, File dir) {
        if (desc != null && !desc.isEmpty()) {
            return desc;
        }
        String fallbackPrefix = KIND_USB.equals(kind) ? "USB OTG" : "Tarjeta SD";
        return fallbackPrefix + " (" + dir.getName() + ")";
    }

    private static String makeUniqueId(String rawName, Set<String> usedIds) {
        String base = (rawName == null ? "ext" : rawName).replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isEmpty()) base = "ext";
        String candidate = base;
        int n = 2;
        while (usedIds.contains(candidate)) {
            candidate = base + "_" + n;
            n++;
        }
        usedIds.add(candidate);
        return candidate;
    }

    private static long[] statVolume(File dir) {
        try {
            StatFs fs = new StatFs(dir.getAbsolutePath());
            return new long[]{fs.getTotalBytes(), fs.getAvailableBytes()};
        } catch (Exception e) {
            return new long[]{0L, 0L};
        }
    }

    private static File canonicalSafe(File f) {
        if (f == null) return null;
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            return f.getAbsoluteFile();
        }
    }
}
