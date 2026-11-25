package com.fotospixel.transfer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Detects the host Android version and OEM skin (One UI, HyperOS, Nothing OS,
 * ColorOS, realme UI, MagicOS, EMUI) and supplies a native visual profile covering
 * typography, geometry, elevation/borders, header layout, navigation bar chrome,
 * sheet gravity, and motion physics.
 */
public final class OsDesignSystem {

    public enum SkinId {
        AUTO("Automático (Sistema)"),
        MATERIAL_ANDROID_9("Material Design (Android 8–9)"),
        ANDROID_10_11("Material Theme (Android 10–11)"),
        MATERIAL_YOU("Material You (Android 12–15)"),
        MATERIAL_EXPRESSIVE("Material 3 Expressive (Android 16+)"),
        ONE_UI("Samsung One UI"),
        HYPER_OS("Xiaomi HyperOS"),
        NOTHING_OS("Nothing OS"),
        COLOR_OS("OPPO ColorOS"),
        REALME_UI("realme UI"),
        MAGIC_OS("Honor MagicOS"),
        EMUI("Huawei EMUI");

        public final String selectorLabel;

        SkinId(String selectorLabel) {
            this.selectorLabel = selectorLabel;
        }
    }

    public enum HeaderStyle {
        ANDROID_9_APPBAR,
        ONE_UI_HERO,
        NOTHING_DOT,
        OEM_CLEAN_LARGE,
        M3_EDITORIAL
    }

    public enum NavStyle {
        DOCKED_CLASSIC,
        DOCKED_M3_PILL,
        FLOATING_CAPSULE
    }

    public enum MotionStyle {
        CLASSIC_RIPPLE,
        SMOOTH_DAMPED,
        FLUID_SPRING,
        SNAPPY_TECH
    }

    public enum NavIndicatorStyle {
        MD2_TINT_ONLY,
        GOOGLE_TOP_BAR,
        M3_ICON_PILL,
        ONE_UI_UNDERLINE,
        REALME_YELLOW_PILL,
        HARMONY_TINT,
        FLOATING_CAPSULE
    }

    public enum FolderBadgeStyle {
        GOOGLE_PIE_CIRCLE,
        GOOGLE_OUTLINED,
        M3_TONAL,
        SAMSUNG_GOLD,
        XIAOMI_SKY,
        NOTHING_MONO,
        COLOROS_AQUA,
        REALME_YELLOW,
        MAGIC_CYAN,
        HARMONY_BLUE
    }

    public enum HeroCardStyle {
        PLAIN_TONAL,
        MATERIAL_ELEVATED,
        GOOGLE_OUTLINED,
        ONE_UI_BLOCK,
        HYPER_BENTO_FROST,
        NOTHING_WIREFRAME,
        AQUAMORPHIC_GRADIENT,
        REALME_GT_STRIPE,
        MAGIC_PORTAL_RIM,
        HARMONY_SERVICE_CARD
    }

    public static final class DetectionResult {
        public final SkinId skinId;
        public final String displayLabel;

        public DetectionResult(SkinId skinId, String displayLabel) {
            this.skinId = skinId;
            this.displayLabel = displayLabel;
        }
    }

    public static final class Profile {
        public final SkinId id;
        public final SkinId detectedHardwareId;
        public final String displayName;
        public final String hardwareLabel;
        public final boolean isNight;
        public final HeaderStyle headerStyle;
        public final NavStyle navStyle;
        public final MotionStyle motionStyle;

        public final int cardRadiusDp;
        public final int heroRadiusDp;
        public final int innerRadiusDp;
        public final int buttonRadiusDp;
        public final int chipRadiusDp;
        public final int dialogRadiusDp;

        public final float cardElevationDp;
        public final int cardStrokeAlpha;
        public final boolean bottomSheetDialogs;
        public final boolean bottomSheetHandle;
        public final boolean uppercaseSectionHeaders;
        public final boolean dotMatrixGraph;
        public final boolean pinnedTopToolbar;
        public final boolean externalSectionHeaders;
        public final boolean groupedListDividers;
        public final boolean showRowChevrons;
        public final boolean uppercaseButtons;

        public final NavIndicatorStyle navIndicatorStyle;
        public final FolderBadgeStyle folderBadgeStyle;
        public final HeroCardStyle heroCardStyle;

        public final int pageHorizontalPadDp;
        public final int cardPaddingDp;
        public final int cardGapDp;

        Profile(
                SkinId id,
                SkinId detectedHardwareId,
                String displayName,
                String hardwareLabel,
                boolean isNight,
                HeaderStyle headerStyle,
                NavStyle navStyle,
                MotionStyle motionStyle,
                NavIndicatorStyle navIndicatorStyle,
                FolderBadgeStyle folderBadgeStyle,
                HeroCardStyle heroCardStyle,
                int cardRadiusDp,
                int heroRadiusDp,
                int innerRadiusDp,
                int buttonRadiusDp,
                int chipRadiusDp,
                int dialogRadiusDp,
                float cardElevationDp,
                int cardStrokeAlpha,
                boolean bottomSheetDialogs,
                boolean uppercaseSectionHeaders,
                boolean dotMatrixGraph,
                boolean pinnedTopToolbar,
                boolean externalSectionHeaders,
                boolean groupedListDividers,
                boolean showRowChevrons,
                boolean uppercaseButtons,
                int pageHorizontalPadDp,
                int cardPaddingDp,
                int cardGapDp) {
            this.id = id;
            this.detectedHardwareId = detectedHardwareId;
            this.displayName = displayName;
            this.hardwareLabel = hardwareLabel;
            this.isNight = isNight;
            this.headerStyle = headerStyle;
            this.navStyle = navStyle;
            this.motionStyle = motionStyle;
            this.navIndicatorStyle = navIndicatorStyle;
            this.folderBadgeStyle = folderBadgeStyle;
            this.heroCardStyle = heroCardStyle;
            this.cardRadiusDp = cardRadiusDp;
            this.heroRadiusDp = heroRadiusDp;
            this.innerRadiusDp = innerRadiusDp;
            this.buttonRadiusDp = buttonRadiusDp;
            this.chipRadiusDp = chipRadiusDp;
            this.dialogRadiusDp = dialogRadiusDp;
            this.cardElevationDp = cardElevationDp;
            this.cardStrokeAlpha = cardStrokeAlpha;
            this.bottomSheetDialogs = bottomSheetDialogs;
            this.bottomSheetHandle = bottomSheetDialogs;
            this.uppercaseSectionHeaders = uppercaseSectionHeaders;
            this.dotMatrixGraph = dotMatrixGraph;
            this.pinnedTopToolbar = pinnedTopToolbar;
            this.externalSectionHeaders = externalSectionHeaders;
            this.groupedListDividers = groupedListDividers;
            this.showRowChevrons = showRowChevrons;
            this.uppercaseButtons = uppercaseButtons;
            this.pageHorizontalPadDp = pageHorizontalPadDp;
            this.cardPaddingDp = cardPaddingDp;
            this.cardGapDp = cardGapDp;
        }

        public Typeface heroTitleTypeface() {
            if (id == SkinId.ONE_UI) {
                return Typeface.create("sans-serif-light", Typeface.NORMAL);
            }
            return titleTypeface();
        }

        public Typeface titleTypeface() {
            if (id == SkinId.NOTHING_OS) {
                return Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            }
            if (id == SkinId.MATERIAL_ANDROID_9 || id == SkinId.ANDROID_10_11) {
                return Typeface.create("sans-serif-medium", Typeface.BOLD);
            }
            return Typeface.create("sans-serif", Typeface.BOLD);
        }

        public Typeface sectionTypeface() {
            if (id == SkinId.NOTHING_OS) {
                return Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            }
            if (id == SkinId.MATERIAL_ANDROID_9 || id == SkinId.ANDROID_10_11) {
                return Typeface.create("sans-serif-medium", Typeface.BOLD);
            }
            return Typeface.DEFAULT_BOLD;
        }

        public Typeface bodyMediumTypeface() {
            if (id == SkinId.MATERIAL_ANDROID_9 || id == SkinId.ANDROID_10_11) {
                return Typeface.create("sans-serif-medium", Typeface.NORMAL);
            }
            return Typeface.DEFAULT_BOLD;
        }

        public float titleLetterSpacing() {
            if (id == SkinId.NOTHING_OS) return 0.03f;
            if (id == SkinId.MATERIAL_EXPRESSIVE || id == SkinId.HYPER_OS) return -0.02f;
            if (id == SkinId.ONE_UI) return -0.015f;
            return 0f;
        }

        public float titleTextSizeSp() {
            switch (headerStyle) {
                case ONE_UI_HERO:
                    return 25f;
                case NOTHING_DOT:
                    return 18f;
                case OEM_CLEAN_LARGE:
                    return 22f;
                case ANDROID_9_APPBAR:
                    return 20f;
                case M3_EDITORIAL:
                default:
                    return 21f;
            }
        }

        public String webFontStack() {
            if (id == SkinId.NOTHING_OS) {
                return "'JetBrains Mono', ui-monospace, 'SFMono-Regular', Menlo, monospace";
            }
            if (id == SkinId.ONE_UI) {
                return "'SamsungOne', 'Roboto', system-ui, -apple-system, sans-serif";
            }
            if (id == SkinId.HYPER_OS) {
                return "'MiSans', 'Inter', system-ui, -apple-system, sans-serif";
            }
            if (id == SkinId.MATERIAL_ANDROID_9 || id == SkinId.ANDROID_10_11) {
                return "'Roboto', 'Segoe UI', system-ui, sans-serif";
            }
            return "'Google Sans Flex', 'Google Sans', 'Plus Jakarta Sans', 'Inter', system-ui, sans-serif";
        }
    }

    private static final String PREFS_NAME = "fotospixel_prefs";
    private static final String KEY_SKIN_OVERRIDE = "ui_skin_override";

    private static final String[] SYS_PROP_KEYS = new String[] {
            "ro.nothing.version.id",
            "ro.build.nothing.version",
            "ro.build.version.oneui",
            "ro.mi.os.version.name",
            "ro.mi.os.version.code",
            "ro.miui.ui.version.name",
            "ro.build.version.realmeui",
            "ro.build.version.opporom",
            "ro.build.version.oplusrom",
            "ro.build.version.magic",
            "msc.config.magic.version",
            "ro.build.version.emui"
    };

    private OsDesignSystem() {}

    public static SkinId getSavedOverride(Context ctx) {
        if (ctx == null) return SkinId.AUTO;
        String raw = ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SKIN_OVERRIDE, SkinId.AUTO.name());
        try {
            return SkinId.valueOf(raw);
        } catch (Exception ignored) {
            return SkinId.AUTO;
        }
    }

    public static void saveOverride(Context ctx, SkinId skinId) {
        if (ctx == null) return;
        SkinId target = (skinId != null) ? skinId : SkinId.AUTO;
        ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SKIN_OVERRIDE, target.name())
                .apply();
    }

    public static DetectionResult detectHardwareSkin() {
        Map<String, String> props = new HashMap<>();
        for (String key : SYS_PROP_KEYS) {
            String val = readSysProp(key);
            if (!val.isEmpty()) {
                props.put(key, val);
            }
        }
        return detectFromProps(
                Build.VERSION.SDK_INT,
                Build.VERSION.RELEASE,
                Build.MANUFACTURER,
                Build.BRAND,
                Build.DISPLAY,
                props);
    }

    /**
     * Pure detection helper accepting Build metadata and system properties for
     * deterministic OEM skin resolution and desktop unit testing.
     */
    public static DetectionResult detectFromProps(
            int sdkInt,
            String release,
            String manufacturer,
            String brand,
            String display,
            Map<String, String> props) {
        String mfg = safeLower(manufacturer);
        String brd = safeLower(brand);
        String dsp = safeLower(display);
        String rel = (release != null && !release.isEmpty()) ? release : String.valueOf(sdkInt);

        String nothingVer = firstNonEmpty(props, "ro.nothing.version.id", "ro.build.nothing.version");
        if (!nothingVer.isEmpty() || containsAny(mfg, "nothing", "cmf") || containsAny(brd, "nothing", "cmf")) {
            return new DetectionResult(SkinId.NOTHING_OS,
                    nothingVer.isEmpty() ? "Nothing OS" : ("Nothing OS " + nothingVer));
        }

        String oneUiVer = firstNonEmpty(props, "ro.build.version.oneui");
        if (!oneUiVer.isEmpty() || containsAny(mfg, "samsung") || containsAny(brd, "samsung")) {
            return new DetectionResult(SkinId.ONE_UI, formatOneUiLabel(oneUiVer));
        }

        String hyperVer = firstNonEmpty(props, "ro.mi.os.version.name", "ro.mi.os.version.code");
        String miuiVer = firstNonEmpty(props, "ro.miui.ui.version.name");
        if (!hyperVer.isEmpty() || !miuiVer.isEmpty()
                || containsAny(mfg, "xiaomi", "redmi", "poco")
                || containsAny(brd, "xiaomi", "redmi", "poco")
                || dsp.contains("hyperos")) {
            String ver = !hyperVer.isEmpty() ? hyperVer : "";
            return new DetectionResult(SkinId.HYPER_OS,
                    ver.isEmpty() ? "Xiaomi HyperOS" : ("Xiaomi HyperOS " + ver));
        }

        // realme UI must be checked before ColorOS because realme devices also populate oplusrom
        String realmeVer = firstNonEmpty(props, "ro.build.version.realmeui");
        if (!realmeVer.isEmpty() || containsAny(mfg, "realme") || containsAny(brd, "realme")) {
            return new DetectionResult(SkinId.REALME_UI,
                    realmeVer.isEmpty() ? "realme UI" : ("realme UI " + realmeVer));
        }

        String colorVer = firstNonEmpty(props, "ro.build.version.oplusrom", "ro.build.version.opporom");
        if (!colorVer.isEmpty() || containsAny(mfg, "oppo", "oneplus") || containsAny(brd, "oppo", "oneplus")) {
            return new DetectionResult(SkinId.COLOR_OS,
                    colorVer.isEmpty() ? "OPPO ColorOS" : ("ColorOS " + colorVer));
        }

        // MagicOS must be checked before EMUI because legacy/hybrid Honor builds expose ro.build.version.emui
        String magicVer = firstNonEmpty(props, "ro.build.version.magic", "msc.config.magic.version");
        if (!magicVer.isEmpty() || containsAny(mfg, "honor") || containsAny(brd, "honor")) {
            return new DetectionResult(SkinId.MAGIC_OS,
                    magicVer.isEmpty() ? "Honor MagicOS" : ("Honor MagicOS " + magicVer));
        }

        String emuiVer = firstNonEmpty(props, "ro.build.version.emui");
        if (!emuiVer.isEmpty() || containsAny(mfg, "huawei") || containsAny(brd, "huawei")) {
            return new DetectionResult(SkinId.EMUI,
                    emuiVer.isEmpty() ? "Huawei EMUI" : ("Huawei " + emuiVer));
        }

        if (sdkInt >= 36) {
            return new DetectionResult(SkinId.MATERIAL_EXPRESSIVE, "Material 3 Expressive · Android " + rel);
        }
        if (sdkInt >= 31) {
            return new DetectionResult(SkinId.MATERIAL_YOU, "Material You · Android " + rel);
        }
        if (sdkInt >= 29) {
            return new DetectionResult(SkinId.ANDROID_10_11, "Material Theme · Android " + rel);
        }
        return new DetectionResult(SkinId.MATERIAL_ANDROID_9, "Material Design · Android " + rel);
    }

    public static Profile resolve(Context ctx) {
        DetectionResult hw = detectHardwareSkin();
        SkinId override = getSavedOverride(ctx);
        SkinId activeId = (override != SkinId.AUTO) ? override : hw.skinId;
        boolean night = false;
        if (ctx != null) {
            int mask = ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            night = (mask == Configuration.UI_MODE_NIGHT_YES);
        }
        String displayLabel = (override == SkinId.AUTO) ? hw.displayLabel : activeId.selectorLabel;
        return buildProfile(activeId, hw.skinId, displayLabel, hw.displayLabel, night);
    }

    public static Profile buildProfile(SkinId id, boolean isNight) {
        SkinId resolved = (id == null || id == SkinId.AUTO) ? SkinId.MATERIAL_EXPRESSIVE : id;
        return buildProfile(resolved, resolved, resolved.selectorLabel, resolved.selectorLabel, isNight);
    }

    private static Profile buildProfile(
            SkinId id,
            SkinId hwId,
            String displayName,
            String hardwareLabel,
            boolean isNight) {
        switch (id) {
            case MATERIAL_ANDROID_9:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.ANDROID_9_APPBAR, NavStyle.DOCKED_CLASSIC, MotionStyle.CLASSIC_RIPPLE,
                        NavIndicatorStyle.MD2_TINT_ONLY, FolderBadgeStyle.GOOGLE_PIE_CIRCLE, HeroCardStyle.MATERIAL_ELEVATED,
                        6, 8, 6, 4, 16, 6,
                        isNight ? 1f : 3.0f, isNight ? 0x55 : 0x22, false, true, false,
                        true, false, true, false, true,
                        14, 16, 10);
            case ANDROID_10_11:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.ANDROID_9_APPBAR, NavStyle.DOCKED_CLASSIC, MotionStyle.CLASSIC_RIPPLE,
                        NavIndicatorStyle.GOOGLE_TOP_BAR, FolderBadgeStyle.GOOGLE_OUTLINED, HeroCardStyle.GOOGLE_OUTLINED,
                        12, 14, 8, 8, 999, 14,
                        0f, 0x9A, false, false, false,
                        false, false, true, false, false,
                        16, 16, 12);
            case MATERIAL_YOU:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.M3_EDITORIAL, NavStyle.DOCKED_M3_PILL, MotionStyle.SMOOTH_DAMPED,
                        NavIndicatorStyle.M3_ICON_PILL, FolderBadgeStyle.M3_TONAL, HeroCardStyle.PLAIN_TONAL,
                        22, 24, 14, 999, 12, 28,
                        0f, 0, false, false, false,
                        false, false, false, false, false,
                        16, 18, 12);
            case ONE_UI:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.ONE_UI_HERO, NavStyle.DOCKED_CLASSIC, MotionStyle.SMOOTH_DAMPED,
                        NavIndicatorStyle.ONE_UI_UNDERLINE, FolderBadgeStyle.SAMSUNG_GOLD, HeroCardStyle.ONE_UI_BLOCK,
                        26, 26, 16, 999, 999, 26,
                        0f, 0, true, false, false,
                        false, true, true, true, false,
                        14, 16, 10);
            case HYPER_OS:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.OEM_CLEAN_LARGE, NavStyle.FLOATING_CAPSULE, MotionStyle.FLUID_SPRING,
                        NavIndicatorStyle.FLOATING_CAPSULE, FolderBadgeStyle.XIAOMI_SKY, HeroCardStyle.HYPER_BENTO_FROST,
                        24, 26, 16, 14, 999, 28,
                        isNight ? 0f : 1.5f, 0x30, true, false, false,
                        false, false, true, true, false,
                        16, 18, 12);
            case NOTHING_OS:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.NOTHING_DOT, NavStyle.FLOATING_CAPSULE, MotionStyle.SNAPPY_TECH,
                        NavIndicatorStyle.FLOATING_CAPSULE, FolderBadgeStyle.NOTHING_MONO, HeroCardStyle.NOTHING_WIREFRAME,
                        18, 20, 12, 999, 999, 22,
                        0f, 0xFF, false, true, true,
                        false, false, false, false, false,
                        16, 18, 12);
            case COLOR_OS:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.OEM_CLEAN_LARGE, NavStyle.DOCKED_CLASSIC, MotionStyle.SMOOTH_DAMPED,
                        NavIndicatorStyle.HARMONY_TINT, FolderBadgeStyle.COLOROS_AQUA, HeroCardStyle.AQUAMORPHIC_GRADIENT,
                        22, 24, 14, 14, 999, 24,
                        isNight ? 0f : 1.8f, 0x24, true, false, false,
                        false, false, false, true, false,
                        16, 18, 12);
            case REALME_UI:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.OEM_CLEAN_LARGE, NavStyle.DOCKED_CLASSIC, MotionStyle.SMOOTH_DAMPED,
                        NavIndicatorStyle.REALME_YELLOW_PILL, FolderBadgeStyle.REALME_YELLOW, HeroCardStyle.REALME_GT_STRIPE,
                        18, 20, 12, 12, 999, 24,
                        isNight ? 0f : 1.2f, 0x26, true, false, false,
                        false, false, false, false, false,
                        16, 18, 12);
            case MAGIC_OS:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.OEM_CLEAN_LARGE, NavStyle.DOCKED_CLASSIC, MotionStyle.SMOOTH_DAMPED,
                        NavIndicatorStyle.HARMONY_TINT, FolderBadgeStyle.MAGIC_CYAN, HeroCardStyle.MAGIC_PORTAL_RIM,
                        22, 24, 14, 999, 999, 24,
                        isNight ? 0f : 1.2f, 0x28, true, false, false,
                        false, false, true, true, false,
                        16, 18, 12);
            case EMUI:
                return new Profile(id, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.OEM_CLEAN_LARGE, NavStyle.DOCKED_CLASSIC, MotionStyle.SMOOTH_DAMPED,
                        NavIndicatorStyle.HARMONY_TINT, FolderBadgeStyle.HARMONY_BLUE, HeroCardStyle.HARMONY_SERVICE_CARD,
                        16, 18, 12, 999, 999, 24,
                        isNight ? 0f : 1.0f, 0x22, true, false, false,
                        false, false, true, true, false,
                        14, 16, 10);
            case MATERIAL_EXPRESSIVE:
            default:
                return new Profile(SkinId.MATERIAL_EXPRESSIVE, hwId, displayName, hardwareLabel, isNight,
                        HeaderStyle.M3_EDITORIAL, NavStyle.FLOATING_CAPSULE, MotionStyle.FLUID_SPRING,
                        NavIndicatorStyle.FLOATING_CAPSULE, FolderBadgeStyle.M3_TONAL, HeroCardStyle.PLAIN_TONAL,
                        28, 32, 20, 999, 999, 28,
                        0f, 0, false, false, false,
                        false, false, false, false, false,
                        18, 20, 14);
        }
    }

    private static String readSysProp(String key) {
        try {
            Class<?> sp = Class.forName("android.os.SystemProperties");
            Method get = sp.getMethod("get", String.class, String.class);
            Object val = get.invoke(null, key, "");
            return val != null ? String.valueOf(val).trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.US);
    }

    private static boolean containsAny(String haystack, String... needles) {
        if (haystack == null || haystack.isEmpty()) return false;
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }

    private static String firstNonEmpty(Map<String, String> props, String... keys) {
        if (props == null) return "";
        for (String k : keys) {
            String v = props.get(k);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return "";
    }

    public static boolean supportsMonetPalette(SkinId id) {
        return id == SkinId.MATERIAL_YOU || id == SkinId.MATERIAL_EXPRESSIVE;
    }

    private static String formatOneUiLabel(String raw) {
        if (raw == null || raw.isEmpty()) return "Samsung One UI";
        try {
            int code = Integer.parseInt(raw);
            if (code >= 10000) {
                int major = code / 10000;
                int minor = (code % 10000) / 100;
                return "Samsung One UI " + major + "." + minor;
            }
        } catch (NumberFormatException ignored) {
        }
        return "Samsung One UI " + raw;
    }
}
