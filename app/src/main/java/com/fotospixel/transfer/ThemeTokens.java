package com.fotospixel.transfer;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import java.util.Locale;

/**
 * Shared adaptive design-token palette resolving authentic Light and Dark
 * surface/ink/accent tokens per OS/OEM layer and exporting CSS/shape tokens
 * identically to both native views and WebUi.
 */
public final class ThemeTokens {

    public final int surface;
    public final int surfaceDim;
    public final int surfaceBright;
    public final int surfaceContainerLowest;
    public final int surfaceContainerLow;
    public final int surfaceContainer;
    public final int surfaceContainerHigh;
    public final int surfaceContainerHighest;

    public final int primary;
    public final int onPrimary;
    public final int primaryContainer;
    public final int onPrimaryContainer;

    public final int secondary;
    public final int onSecondary;
    public final int secondaryContainer;
    public final int onSecondaryContainer;

    public final int tertiary;
    public final int onTertiary;
    public final int tertiaryContainer;
    public final int onTertiaryContainer;

    public final int onSurface;
    public final int onSurfaceVariant;
    public final int outline;
    public final int outlineVariant;

    public final int error;
    public final int onError;
    public final int errorContainer;
    public final int onErrorContainer;

    public final boolean isLight;
    public final OsDesignSystem.Profile profile;

    private ThemeTokens(
            int surface, int surfaceDim, int surfaceBright,
            int surfaceContainerLowest, int surfaceContainerLow,
            int surfaceContainer, int surfaceContainerHigh, int surfaceContainerHighest,
            int primary, int onPrimary, int primaryContainer, int onPrimaryContainer,
            int secondary, int onSecondary, int secondaryContainer, int onSecondaryContainer,
            int tertiary, int onTertiary, int tertiaryContainer, int onTertiaryContainer,
            int onSurface, int onSurfaceVariant, int outline, int outlineVariant,
            int error, int onError, int errorContainer, int onErrorContainer,
            OsDesignSystem.Profile profile) {
        this.surface = surface;
        this.surfaceDim = surfaceDim;
        this.surfaceBright = surfaceBright;
        this.surfaceContainerLowest = surfaceContainerLowest;
        this.surfaceContainerLow = surfaceContainerLow;
        this.surfaceContainer = surfaceContainer;
        this.surfaceContainerHigh = surfaceContainerHigh;
        this.surfaceContainerHighest = surfaceContainerHighest;
        this.primary = primary;
        this.onPrimary = onPrimary;
        this.primaryContainer = primaryContainer;
        this.onPrimaryContainer = onPrimaryContainer;
        this.secondary = secondary;
        this.onSecondary = onSecondary;
        this.secondaryContainer = secondaryContainer;
        this.onSecondaryContainer = onSecondaryContainer;
        this.tertiary = tertiary;
        this.onTertiary = onTertiary;
        this.tertiaryContainer = tertiaryContainer;
        this.onTertiaryContainer = onTertiaryContainer;
        this.onSurface = onSurface;
        this.onSurfaceVariant = onSurfaceVariant;
        this.outline = outline;
        this.outlineVariant = outlineVariant;
        this.error = error;
        this.onError = onError;
        this.errorContainer = errorContainer;
        this.onErrorContainer = onErrorContainer;
        this.isLight = isColorLight(surface);
        this.profile = profile;
    }

    public static ThemeTokens read(Context ctx) {
        return forProfile(ctx, OsDesignSystem.resolve(ctx));
    }

    public static ThemeTokens forProfile(Context ctx, OsDesignSystem.Profile profile) {
        OsDesignSystem.Profile p = (profile != null)
                ? profile
                : OsDesignSystem.buildProfile(OsDesignSystem.SkinId.MATERIAL_EXPRESSIVE, true);
        boolean night = p.isNight;

        switch (p.id) {
            case MATERIAL_ANDROID_9:
                return night
                        ? make("#121212", "#0D0D0D", "#2C2C30", "#0D0D0D", "#1E1E1E", "#232326", "#2C2C30", "#36373B",
                               "#8AB4F8", "#062E6F", "#174EA6", "#D2E3FC",
                               "#9AA0A6", "#202124", "#3C4043", "#E8EAED",
                               "#4DB6AC", "#00332C", "#004D40", "#B2DFDB",
                               "#E8EAED", "#9AA0A6", "#80868B", "#3C4043",
                               "#F28B82", "#601410", "#8C1D18", "#F9DEDC", p)
                        : make("#F5F7FA", "#ECEFF3", "#FFFFFF", "#EEF1F5", "#FFFFFF", "#FFFFFF", "#F1F3F4", "#E8EAED",
                               "#1A73E8", "#FFFFFF", "#E8F0FE", "#174EA6",
                               "#5F6368", "#FFFFFF", "#E8EAED", "#202124",
                               "#00796B", "#FFFFFF", "#E0F2F1", "#004D40",
                               "#202124", "#5F6368", "#70757A", "#DADCE0",
                               "#D93025", "#FFFFFF", "#FCE8E6", "#A50E0E", p);

            case ANDROID_10_11:
                return night
                        ? make("#121212", "#0C0C0C", "#2E2E2E", "#0C0C0C", "#1F1F1F", "#212121", "#2A2A2D", "#35353A",
                               "#8AB4F8", "#062E6F", "#174EA6", "#D2E3FC",
                               "#9AA0A6", "#202124", "#3C4043", "#E8EAED",
                               "#81C995", "#073B1A", "#0D652D", "#CEEAD6",
                               "#E8EAED", "#9AA0A6", "#80868B", "#3C4043",
                               "#F28B82", "#601410", "#8C1D18", "#F9DEDC", p)
                        : make("#FFFFFF", "#F1F3F4", "#FFFFFF", "#F1F3F4", "#FFFFFF", "#FFFFFF", "#F8F9FA", "#E8EAED",
                               "#1A73E8", "#FFFFFF", "#E8F0FE", "#174EA6",
                               "#5F6368", "#FFFFFF", "#E8EAED", "#202124",
                               "#137333", "#FFFFFF", "#E6F4EA", "#0D652D",
                               "#202124", "#5F6368", "#747775", "#DADCE0",
                               "#D93025", "#FFFFFF", "#FCE8E6", "#A50E0E", p);

            case ONE_UI:
                return night
                        ? make("#000000", "#000000", "#2A2A2E", "#000000", "#17171A", "#1C1C20", "#242429", "#2E2E35",
                               "#4DA3FF", "#002A5C", "#00458F", "#D8ECFF",
                               "#A0A0AA", "#1C1C1E", "#2C2C33", "#EAEAEE",
                               "#3CD6C6", "#003832", "#00524A", "#C7F5F0",
                               "#F2F2F7", "#A1A1AA", "#72727A", "#2E2E35",
                               "#FF6961", "#3D0502", "#6E130F", "#FFD6D3", p)
                        : make("#F2F2F7", "#E6E6EE", "#FFFFFF", "#E8E8EF", "#FFFFFF", "#FFFFFF", "#EAEAF0", "#DFDFE7",
                               "#0070EC", "#FFFFFF", "#DCEEFF", "#004A99",
                               "#545459", "#FFFFFF", "#E5E5EC", "#1C1C1E",
                               "#007C72", "#FFFFFF", "#D6F5F2", "#004D46",
                               "#1C1C1E", "#5A5A60", "#76767D", "#D1D1D8",
                               "#D92B22", "#FFFFFF", "#FFE5E4", "#8F130D", p);

            case HYPER_OS:
                return night
                        ? make("#0A0B0E", "#060709", "#282B34", "#060709", "#17181E", "#1B1D24", "#22242C", "#2C2F3A",
                               "#4DA3FF", "#002B5E", "#0B468C", "#DCEEFF",
                               "#9AA0B0", "#14161D", "#272A34", "#ECEFF5",
                               "#35DCA2", "#003824", "#00593B", "#C9F9E8",
                               "#F1F3F9", "#A2A7B5", "#767B88", "#2C2F3A",
                               "#FF7B72", "#470804", "#7D1610", "#FFDAD6", p)
                        : make("#F6F7FB", "#EBEEF5", "#FFFFFF", "#EFF1F7", "#FFFFFF", "#FFFFFF", "#EFF1F8", "#E3E6F0",
                               "#006FE8", "#FFFFFF", "#E0F0FF", "#004899",
                               "#585E6C", "#FFFFFF", "#E8EBF2", "#1B1E26",
                               "#007E56", "#FFFFFF", "#DCF7EC", "#005234",
                               "#171920", "#595F6B", "#797F8C", "#DCDFE8",
                               "#D92E25", "#FFFFFF", "#FFE6E4", "#8F140E", p);

            case NOTHING_OS:
                return night
                        ? make("#000000", "#000000", "#2A2A30", "#000000", "#121214", "#161619", "#1F1F23", "#2A2A30",
                               "#FFFFFF", "#000000", "#242429", "#FFFFFF",
                               "#A1A1AA", "#000000", "#1C1C20", "#F4F4F5",
                               "#FF6B70", "#2B0002", "#590C11", "#FFD6D8",
                               "#F5F5F7", "#A1A1AA", "#6E6E78", "#2F2F36",
                               "#FF6E66", "#2E0002", "#5C0E11", "#FFD7D5", p)
                        : make("#EFEFF2", "#E4E4E9", "#FFFFFF", "#E4E4E9", "#FFFFFF", "#FFFFFF", "#E5E5EA", "#D9D9E0",
                               "#111113", "#FFFFFF", "#DCDCE2", "#111113",
                               "#52525B", "#FFFFFF", "#E0E0E6", "#141417",
                               "#D71921", "#FFFFFF", "#FFE5E7", "#8A0A10",
                               "#111113", "#52525B", "#71717A", "#C7C7D1",
                               "#C81018", "#FFFFFF", "#FFE3E5", "#7F090E", p);

            case COLOR_OS:
                return night
                        ? make("#0D0E12", "#08090C", "#2A2E38", "#08090C", "#191B21", "#1D2027", "#242730", "#2F333E",
                               "#5996FF", "#002966", "#0044AD", "#DCE9FF",
                               "#9BA2B2", "#15171E", "#282B36", "#EDF0F7",
                               "#38D9A9", "#003828", "#00573F", "#C8F7E8",
                               "#F0F2F7", "#A4AAB8", "#747A88", "#2F333E",
                               "#FF7B72", "#470804", "#7D1610", "#FFDAD6", p)
                        : make("#F5F6FA", "#EAEBF2", "#FFFFFF", "#ECEEF4", "#FFFFFF", "#FFFFFF", "#ECEDF3", "#E2E4EC",
                               "#0066FF", "#FFFFFF", "#E0ECFF", "#003B99",
                               "#565B68", "#FFFFFF", "#E8EAF2", "#1A1C23",
                               "#007E5B", "#FFFFFF", "#D9F7EE", "#065F46",
                               "#181A20", "#575C68", "#787E8C", "#DADCE5",
                               "#D92B22", "#FFFFFF", "#FFE5E4", "#8F130D", p);

            case REALME_UI:
                return night
                        ? make("#0E0F13", "#090A0D", "#2B2E39", "#090A0D", "#181A20", "#1D2028", "#242731", "#303441",
                               "#FFC800", "#181300", "#4D3A00", "#FFE885",
                               "#A0A6B5", "#161820", "#282C38", "#EDEFF5",
                               "#6BA5FF", "#002766", "#123E8A", "#D9E8FF",
                               "#F2F3F7", "#A6ACB9", "#777D8B", "#313542",
                               "#FF7B72", "#470804", "#7D1610", "#FFDAD6", p)
                        : make("#F4F5F7", "#EAECEF", "#FFFFFF", "#ECEEF2", "#FFFFFF", "#FFFFFF", "#EAECEF", "#DFE2E8",
                               "#FFC400", "#141416", "#FFF0B3", "#2B1E00",
                               "#1E2028", "#FFFFFF", "#E4E7EE", "#14161C",
                               "#1D4ED8", "#FFFFFF", "#DBEAFE", "#0F2D8A",
                               "#121316", "#4F535E", "#737885", "#D4D8E1",
                               "#D92B22", "#FFFFFF", "#FFE5E4", "#8F130D", p);

            case MAGIC_OS:
                return night
                        ? make("#0A0C10", "#06080B", "#282D3C", "#06080B", "#161922", "#1A1E28", "#212531", "#2C3140",
                               "#639AFF", "#002766", "#103F96", "#DCE8FF",
                               "#9BA3B4", "#141720", "#262B38", "#EDF0F7",
                               "#38C6DF", "#00353E", "#005361", "#C6F4FB",
                               "#EFF2F8", "#A3AAB9", "#737A89", "#2C3140",
                               "#FF7B72", "#470804", "#7D1610", "#FFDAD6", p)
                        : make("#F3F5F9", "#E8EBF2", "#FFFFFF", "#ECEEF5", "#FFFFFF", "#FFFFFF", "#E9ECF2", "#DFE3EC",
                               "#1464F6", "#FFFFFF", "#E1ECFF", "#0A3F9E",
                               "#555C69", "#FFFFFF", "#E7EBF3", "#191D26",
                               "#00768C", "#FFFFFF", "#D9F4F9", "#004E5C",
                               "#161922", "#555C69", "#777E8C", "#D8DCE6",
                               "#D92B22", "#FFFFFF", "#FFE5E4", "#8F130D", p);

            case EMUI:
                return night
                        ? make("#0E0F12", "#08090B", "#2D2F38", "#08090B", "#1C1D22", "#202228", "#272930", "#32353E",
                               "#5E94FF", "#002670", "#0B3FA8", "#DCE7FF",
                               "#9CA1AE", "#17181D", "#2A2D36", "#EDEFF4",
                               "#36C9B0", "#00362D", "#005446", "#C8F5ED",
                               "#F1F3F7", "#A4A9B5", "#767B86", "#32353E",
                               "#FF7B72", "#470804", "#7D1610", "#FFDAD6", p)
                        : make("#F1F3F5", "#E5E8EC", "#FFFFFF", "#EAECEF", "#FFFFFF", "#FFFFFF", "#E6E9ED", "#DCE0E6",
                               "#0A59F7", "#FFFFFF", "#E0EAFF", "#0036A3",
                               "#545863", "#FFFFFF", "#E6E9EE", "#18191C",
                               "#007E6A", "#FFFFFF", "#D7F5EF", "#004F42",
                               "#18191C", "#545863", "#757A85", "#D6DAE0",
                               "#D92B22", "#FFFFFF", "#FFE5E4", "#8F130D", p);

            case MATERIAL_YOU:
                return resolveMaterialYou(ctx, p, night);

            case MATERIAL_EXPRESSIVE:
            default:
                return resolveMaterialExpressive(ctx, p, night);
        }
    }

    private static ThemeTokens resolveMaterialYou(Context ctx, OsDesignSystem.Profile p, boolean night) {
        ThemeTokens base = night
                ? make("#141218", "#100E14", "#36343B", "#0F0D13", "#1D1B20", "#211F26", "#2B2930", "#36343B",
                       "#D0BCFF", "#381E72", "#4F378B", "#EADDFF",
                       "#CCC2DC", "#332D41", "#4A4458", "#E8DEF8",
                       "#EFB8C8", "#492532", "#633B48", "#FFD8E4",
                       "#E6E0E9", "#CAC4D0", "#938F99", "#49454F",
                       "#F2B8B5", "#601410", "#8C1D18", "#F9DEDC", p)
                : make("#FEF7FF", "#F3EDF7", "#FFFFFF", "#FFFFFF", "#F7F2FA", "#F3EDF7", "#ECE6F0", "#E6E0E9",
                       "#6750A4", "#FFFFFF", "#EADDFF", "#21005D",
                       "#625B71", "#FFFFFF", "#E8DEF8", "#1D192B",
                       "#7D5260", "#FFFFFF", "#FFD8E4", "#31111D",
                       "#1D1B20", "#49454F", "#79747E", "#CAC4D0",
                       "#B3261E", "#FFFFFF", "#F9DEDC", "#410E0B", p);

        if (ctx != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (night) {
                return new ThemeTokens(
                        safeColor(ctx, android.R.color.system_neutral1_900, base.surface),
                        base.surfaceDim,
                        base.surfaceBright,
                        safeColor(ctx, android.R.color.system_neutral1_1000, base.surfaceContainerLowest),
                        safeColor(ctx, android.R.color.system_neutral1_800, base.surfaceContainerLow),
                        safeColor(ctx, android.R.color.system_neutral1_800, base.surfaceContainer),
                        safeColor(ctx, android.R.color.system_neutral1_700, base.surfaceContainerHigh),
                        safeColor(ctx, android.R.color.system_neutral1_600, base.surfaceContainerHighest),
                        safeColor(ctx, android.R.color.system_accent1_200, base.primary),
                        safeColor(ctx, android.R.color.system_accent1_800, base.onPrimary),
                        safeColor(ctx, android.R.color.system_accent1_700, base.primaryContainer),
                        safeColor(ctx, android.R.color.system_accent1_100, base.onPrimaryContainer),
                        safeColor(ctx, android.R.color.system_accent2_200, base.secondary),
                        safeColor(ctx, android.R.color.system_accent2_800, base.onSecondary),
                        safeColor(ctx, android.R.color.system_accent2_700, base.secondaryContainer),
                        safeColor(ctx, android.R.color.system_accent2_100, base.onSecondaryContainer),
                        safeColor(ctx, android.R.color.system_accent3_200, base.tertiary),
                        safeColor(ctx, android.R.color.system_accent3_800, base.onTertiary),
                        safeColor(ctx, android.R.color.system_accent3_700, base.tertiaryContainer),
                        safeColor(ctx, android.R.color.system_accent3_100, base.onTertiaryContainer),
                        safeColor(ctx, android.R.color.system_neutral1_100, base.onSurface),
                        safeColor(ctx, android.R.color.system_neutral2_200, base.onSurfaceVariant),
                        safeColor(ctx, android.R.color.system_neutral2_400, base.outline),
                        safeColor(ctx, android.R.color.system_neutral2_700, base.outlineVariant),
                        base.error, base.onError, base.errorContainer, base.onErrorContainer, p);
            } else {
                return new ThemeTokens(
                        safeColor(ctx, android.R.color.system_neutral1_10, base.surface),
                        base.surfaceDim,
                        base.surfaceBright,
                        safeColor(ctx, android.R.color.system_neutral1_0, base.surfaceContainerLowest),
                        safeColor(ctx, android.R.color.system_neutral1_50, base.surfaceContainerLow),
                        safeColor(ctx, android.R.color.system_neutral1_50, base.surfaceContainer),
                        safeColor(ctx, android.R.color.system_neutral1_100, base.surfaceContainerHigh),
                        safeColor(ctx, android.R.color.system_neutral1_200, base.surfaceContainerHighest),
                        safeColor(ctx, android.R.color.system_accent1_600, base.primary),
                        safeColor(ctx, android.R.color.system_accent1_0, base.onPrimary),
                        safeColor(ctx, android.R.color.system_accent1_100, base.primaryContainer),
                        safeColor(ctx, android.R.color.system_accent1_900, base.onPrimaryContainer),
                        safeColor(ctx, android.R.color.system_accent2_600, base.secondary),
                        safeColor(ctx, android.R.color.system_accent2_0, base.onSecondary),
                        safeColor(ctx, android.R.color.system_accent2_100, base.secondaryContainer),
                        safeColor(ctx, android.R.color.system_accent2_900, base.onSecondaryContainer),
                        safeColor(ctx, android.R.color.system_accent3_600, base.tertiary),
                        safeColor(ctx, android.R.color.system_accent3_0, base.onTertiary),
                        safeColor(ctx, android.R.color.system_accent3_100, base.tertiaryContainer),
                        safeColor(ctx, android.R.color.system_accent3_900, base.onTertiaryContainer),
                        safeColor(ctx, android.R.color.system_neutral1_900, base.onSurface),
                        safeColor(ctx, android.R.color.system_neutral2_700, base.onSurfaceVariant),
                        safeColor(ctx, android.R.color.system_neutral2_500, base.outline),
                        safeColor(ctx, android.R.color.system_neutral2_200, base.outlineVariant),
                        base.error, base.onError, base.errorContainer, base.onErrorContainer, p);
            }
        }
        return base;
    }

    private static ThemeTokens resolveMaterialExpressive(Context ctx, OsDesignSystem.Profile p, boolean night) {
        ThemeTokens base = night
                ? make("#131313", "#131313", "#393939", "#0E0E0E", "#1B1B1B", "#1F1F1F", "#2A2A2A", "#353535",
                       "#CABEFF", "#322075", "#49398C", "#E6DEFF",
                       "#CAC3DC", "#312E41", "#484458", "#E6DFF9",
                       "#EDB8CC", "#492535", "#623B4B", "#FFD8E5",
                       "#E2E2E2", "#C6C6C6", "#919191", "#474747",
                       "#FFB4AB", "#690005", "#93000A", "#FFDAD6", p)
                : make("#FAF8FF", "#EEECF6", "#FFFFFF", "#FFFFFF", "#F4F1FA", "#EEEBF6", "#E7E3F0", "#DEDAE8",
                       "#5D43A6", "#FFFFFF", "#E6DEFF", "#1E0B5C",
                       "#5E5872", "#FFFFFF", "#E4DFF8", "#1D192B",
                       "#8E3D62", "#FFFFFF", "#FFD8E6", "#3B0721",
                       "#1C1B22", "#484552", "#787484", "#CAC5D6",
                       "#BA1A1A", "#FFFFFF", "#FFDAD6", "#410002", p);

        if (ctx != null && Build.VERSION.SDK_INT >= 34) {
            if (night) {
                return new ThemeTokens(
                        safeColor(ctx, android.R.color.system_surface_dark, base.surface),
                        safeColor(ctx, android.R.color.system_surface_dim_dark, base.surfaceDim),
                        safeColor(ctx, android.R.color.system_surface_bright_dark, base.surfaceBright),
                        safeColor(ctx, android.R.color.system_surface_container_lowest_dark, base.surfaceContainerLowest),
                        safeColor(ctx, android.R.color.system_surface_container_low_dark, base.surfaceContainerLow),
                        safeColor(ctx, android.R.color.system_surface_container_dark, base.surfaceContainer),
                        safeColor(ctx, android.R.color.system_surface_container_high_dark, base.surfaceContainerHigh),
                        safeColor(ctx, android.R.color.system_surface_container_highest_dark, base.surfaceContainerHighest),
                        safeColor(ctx, android.R.color.system_primary_dark, base.primary),
                        safeColor(ctx, android.R.color.system_on_primary_dark, base.onPrimary),
                        safeColor(ctx, android.R.color.system_primary_container_dark, base.primaryContainer),
                        safeColor(ctx, android.R.color.system_on_primary_container_dark, base.onPrimaryContainer),
                        safeColor(ctx, android.R.color.system_secondary_dark, base.secondary),
                        safeColor(ctx, android.R.color.system_on_secondary_dark, base.onSecondary),
                        safeColor(ctx, android.R.color.system_secondary_container_dark, base.secondaryContainer),
                        safeColor(ctx, android.R.color.system_on_secondary_container_dark, base.onSecondaryContainer),
                        safeColor(ctx, android.R.color.system_tertiary_dark, base.tertiary),
                        safeColor(ctx, android.R.color.system_on_tertiary_dark, base.onTertiary),
                        safeColor(ctx, android.R.color.system_tertiary_container_dark, base.tertiaryContainer),
                        safeColor(ctx, android.R.color.system_on_tertiary_container_dark, base.onTertiaryContainer),
                        safeColor(ctx, android.R.color.system_on_surface_dark, base.onSurface),
                        safeColor(ctx, android.R.color.system_on_surface_variant_dark, base.onSurfaceVariant),
                        safeColor(ctx, android.R.color.system_outline_dark, base.outline),
                        safeColor(ctx, android.R.color.system_outline_variant_dark, base.outlineVariant),
                        safeColor(ctx, android.R.color.system_error_dark, base.error),
                        safeColor(ctx, android.R.color.system_on_error_dark, base.onError),
                        safeColor(ctx, android.R.color.system_error_container_dark, base.errorContainer),
                        safeColor(ctx, android.R.color.system_on_error_container_dark, base.onErrorContainer), p);
            } else {
                return new ThemeTokens(
                        safeColor(ctx, android.R.color.system_surface_light, base.surface),
                        safeColor(ctx, android.R.color.system_surface_dim_light, base.surfaceDim),
                        safeColor(ctx, android.R.color.system_surface_bright_light, base.surfaceBright),
                        safeColor(ctx, android.R.color.system_surface_container_lowest_light, base.surfaceContainerLowest),
                        safeColor(ctx, android.R.color.system_surface_container_low_light, base.surfaceContainerLow),
                        safeColor(ctx, android.R.color.system_surface_container_light, base.surfaceContainer),
                        safeColor(ctx, android.R.color.system_surface_container_high_light, base.surfaceContainerHigh),
                        safeColor(ctx, android.R.color.system_surface_container_highest_light, base.surfaceContainerHighest),
                        safeColor(ctx, android.R.color.system_primary_light, base.primary),
                        safeColor(ctx, android.R.color.system_on_primary_light, base.onPrimary),
                        safeColor(ctx, android.R.color.system_primary_container_light, base.primaryContainer),
                        safeColor(ctx, android.R.color.system_on_primary_container_light, base.onPrimaryContainer),
                        safeColor(ctx, android.R.color.system_secondary_light, base.secondary),
                        safeColor(ctx, android.R.color.system_on_secondary_light, base.onSecondary),
                        safeColor(ctx, android.R.color.system_secondary_container_light, base.secondaryContainer),
                        safeColor(ctx, android.R.color.system_on_secondary_container_light, base.onSecondaryContainer),
                        safeColor(ctx, android.R.color.system_tertiary_light, base.tertiary),
                        safeColor(ctx, android.R.color.system_on_tertiary_light, base.onTertiary),
                        safeColor(ctx, android.R.color.system_tertiary_container_light, base.tertiaryContainer),
                        safeColor(ctx, android.R.color.system_on_tertiary_container_light, base.onTertiaryContainer),
                        safeColor(ctx, android.R.color.system_on_surface_light, base.onSurface),
                        safeColor(ctx, android.R.color.system_on_surface_variant_light, base.onSurfaceVariant),
                        safeColor(ctx, android.R.color.system_outline_light, base.outline),
                        safeColor(ctx, android.R.color.system_outline_variant_light, base.outlineVariant),
                        safeColor(ctx, android.R.color.system_error_light, base.error),
                        safeColor(ctx, android.R.color.system_on_error_light, base.onError),
                        safeColor(ctx, android.R.color.system_error_container_light, base.errorContainer),
                        safeColor(ctx, android.R.color.system_on_error_container_light, base.onErrorContainer), p);
            }
        }
        return base;
    }

    private static ThemeTokens make(
            String surface, String surfaceDim, String surfaceBright,
            String surfaceContainerLowest, String surfaceContainerLow,
            String surfaceContainer, String surfaceContainerHigh, String surfaceContainerHighest,
            String primary, String onPrimary, String primaryContainer, String onPrimaryContainer,
            String secondary, String onSecondary, String secondaryContainer, String onSecondaryContainer,
            String tertiary, String onTertiary, String tertiaryContainer, String onTertiaryContainer,
            String onSurface, String onSurfaceVariant, String outline, String outlineVariant,
            String error, String onError, String errorContainer, String onErrorContainer,
            OsDesignSystem.Profile profile) {
        return new ThemeTokens(
                Color.parseColor(surface), Color.parseColor(surfaceDim), Color.parseColor(surfaceBright),
                Color.parseColor(surfaceContainerLowest), Color.parseColor(surfaceContainerLow),
                Color.parseColor(surfaceContainer), Color.parseColor(surfaceContainerHigh), Color.parseColor(surfaceContainerHighest),
                Color.parseColor(primary), Color.parseColor(onPrimary), Color.parseColor(primaryContainer), Color.parseColor(onPrimaryContainer),
                Color.parseColor(secondary), Color.parseColor(onSecondary), Color.parseColor(secondaryContainer), Color.parseColor(onSecondaryContainer),
                Color.parseColor(tertiary), Color.parseColor(onTertiary), Color.parseColor(tertiaryContainer), Color.parseColor(onTertiaryContainer),
                Color.parseColor(onSurface), Color.parseColor(onSurfaceVariant), Color.parseColor(outline), Color.parseColor(outlineVariant),
                Color.parseColor(error), Color.parseColor(onError), Color.parseColor(errorContainer), Color.parseColor(onErrorContainer),
                profile);
    }

    private static int safeColor(Context ctx, int resId, int fallback) {
        try {
            return ctx.getColor(resId);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static boolean isColorLight(int color) {
        double r = linearize(Color.red(color) / 255.0);
        double g = linearize(Color.green(color) / 255.0);
        double b = linearize(Color.blue(color) / 255.0);
        double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return lum >= 0.45;
    }

    private static double linearize(double v) {
        return (v <= 0.03928) ? (v / 12.92) : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }

    public static String toHex(int color) {
        return String.format(Locale.US, "#%06X", (0xFFFFFF & color));
    }

    public String toJson() {
        String skinName = (profile != null) ? profile.displayName : "Material 3 Expressive";
        int cardRadius = (profile != null) ? profile.cardRadiusDp : 28;
        int heroRadius = (profile != null) ? profile.heroRadiusDp : 32;
        int btnRadius = (profile != null) ? profile.buttonRadiusDp : 999;
        String font = (profile != null) ? profile.webFontStack() : "system-ui, sans-serif";
        String cardBorder = (profile != null && profile.cardStrokeAlpha > 0)
                ? ("1px solid " + toHex(outlineVariant))
                : "none";
        String cardShadow = (profile != null && profile.cardElevationDp > 0f && isLight)
                ? ("0 2px 10px rgba(0,0,0,0.08)")
                : "none";
        boolean uppercaseBtns = (profile != null && profile.uppercaseButtons);
        String heroGrad = "none";
        if (profile != null && profile.heroCardStyle == OsDesignSystem.HeroCardStyle.AQUAMORPHIC_GRADIENT) {
            heroGrad = isLight
                    ? "linear-gradient(135deg, #EDF3FF 0%, #E7FAF5 100%)"
                    : "linear-gradient(135deg, #162238 0%, #112A27 100%)";
        } else if (profile != null && profile.heroCardStyle == OsDesignSystem.HeroCardStyle.MAGIC_PORTAL_RIM) {
            heroGrad = isLight
                    ? "linear-gradient(135deg, #EEF3FF 0%, #FFFFFF 100%)"
                    : "linear-gradient(135deg, #161F33 0%, #161922 100%)";
        }

        return "{\"surface\":\"" + toHex(surface) + "\""
                + ",\"surfaceDim\":\"" + toHex(surfaceDim) + "\""
                + ",\"surfaceBright\":\"" + toHex(surfaceBright) + "\""
                + ",\"surfaceContainerLowest\":\"" + toHex(surfaceContainerLowest) + "\""
                + ",\"surfaceContainerLow\":\"" + toHex(surfaceContainerLow) + "\""
                + ",\"surfaceContainer\":\"" + toHex(surfaceContainer) + "\""
                + ",\"surfaceContainerHigh\":\"" + toHex(surfaceContainerHigh) + "\""
                + ",\"surfaceContainerHighest\":\"" + toHex(surfaceContainerHighest) + "\""
                + ",\"primary\":\"" + toHex(primary) + "\""
                + ",\"onPrimary\":\"" + toHex(onPrimary) + "\""
                + ",\"primaryContainer\":\"" + toHex(primaryContainer) + "\""
                + ",\"onPrimaryContainer\":\"" + toHex(onPrimaryContainer) + "\""
                + ",\"secondary\":\"" + toHex(secondary) + "\""
                + ",\"onSecondary\":\"" + toHex(onSecondary) + "\""
                + ",\"secondaryContainer\":\"" + toHex(secondaryContainer) + "\""
                + ",\"onSecondaryContainer\":\"" + toHex(onSecondaryContainer) + "\""
                + ",\"tertiary\":\"" + toHex(tertiary) + "\""
                + ",\"onTertiary\":\"" + toHex(onTertiary) + "\""
                + ",\"tertiaryContainer\":\"" + toHex(tertiaryContainer) + "\""
                + ",\"onTertiaryContainer\":\"" + toHex(onTertiaryContainer) + "\""
                + ",\"onSurface\":\"" + toHex(onSurface) + "\""
                + ",\"onSurfaceVariant\":\"" + toHex(onSurfaceVariant) + "\""
                + ",\"outline\":\"" + toHex(outline) + "\""
                + ",\"outlineVariant\":\"" + toHex(outlineVariant) + "\""
                + ",\"error\":\"" + toHex(error) + "\""
                + ",\"onError\":\"" + toHex(onError) + "\""
                + ",\"errorContainer\":\"" + toHex(errorContainer) + "\""
                + ",\"onErrorContainer\":\"" + toHex(onErrorContainer) + "\""
                + ",\"skinName\":\"" + TransferStats.escapeJson(skinName) + "\""
                + ",\"isLight\":" + isLight
                + ",\"cardRadiusPx\":" + cardRadius
                + ",\"heroRadiusPx\":" + heroRadius
                + ",\"btnRadiusPx\":" + btnRadius
                + ",\"fontFamily\":\"" + TransferStats.escapeJson(font) + "\""
                + ",\"cardBorder\":\"" + TransferStats.escapeJson(cardBorder) + "\""
                + ",\"cardShadow\":\"" + TransferStats.escapeJson(cardShadow) + "\""
                + ",\"uppercaseBtns\":" + uppercaseBtns
                + ",\"heroGradient\":\"" + TransferStats.escapeJson(heroGrad) + "\"}";
    }
}
