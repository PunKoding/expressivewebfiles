package com.fotospixel.transfer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Renders genuine OS- and OEM-specific chrome (headers, capsules, card decorators,
 * folder icon badges, list dividers/chevrons, bottom-navigation indicators, and
 * sheet handles) for each skin profile in {@link OsDesignSystem}.
 */
final class OsSkinRenderer {

    static final class HeaderRefs {
        View pinnedToolbar;
        LinearLayout statusPill;
        View pulseDot;
        TextView statusText;
        TextView extraSubBannerText;
        boolean darkToolbarText;
    }

    static final class NavTabHolder {
        final LinearLayout root;
        final View topBar;
        final FrameLayout iconWrap;
        final ImageView iconView;
        final TextView labelView;
        final View bottomBar;
        final int iconRes;

        NavTabHolder(LinearLayout root, View topBar, FrameLayout iconWrap,
                     ImageView iconView, TextView labelView, View bottomBar, int iconRes) {
            this.root = root;
            this.topBar = topBar;
            this.iconWrap = iconWrap;
            this.iconView = iconView;
            this.labelView = labelView;
            this.bottomBar = bottomBar;
            this.iconRes = iconRes;
        }
    }

    static final class BadgeSpec {
        final int bgColor;
        final int fgColor;
        final int radiusDp;
        final int strokeColor;

        BadgeSpec(int bgColor, int fgColor, int radiusDp, int strokeColor) {
            this.bgColor = bgColor;
            this.fgColor = fgColor;
            this.radiusDp = radiusDp;
            this.strokeColor = strokeColor;
        }
    }

    private OsSkinRenderer() {}

    static int dp(Context ctx, float v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable rounded(Context ctx, int fill, int stroke, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        if (stroke != 0) {
            d.setStroke(Math.max(1, dp(ctx, 1)), stroke);
        }
        d.setCornerRadius(dp(ctx, radiusDp));
        return d;
    }

    static Drawable ripple(Context ctx, int fill, int rippleColor, int stroke, int radiusDp) {
        GradientDrawable content = rounded(ctx, fill, stroke, radiusDp);
        GradientDrawable mask = rounded(ctx, Color.WHITE, 0, radiusDp);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, mask);
    }

    static Drawable tinted(Context ctx, int resId, int color, int sizeDp) {
        if (resId <= 0) return null;
        Drawable raw = ctx.getDrawable(resId);
        if (raw == null) return null;
        Drawable d = raw.mutate();
        d.setTint(color);
        int px = dp(ctx, sizeDp);
        d.setBounds(0, 0, px, px);
        return d;
    }

    /**
     * Builds the native top header chrome (pinned Android 9 Toolbar, centered One UI
     * Viewing Area, HyperConnect poster, Aqua Dynamics / Magic Capsule, HarmonyOS bar,
     * Nothing dot matrix, etc.) and returns interactive refs for MainActivity.
     */
    static HeaderRefs buildHeader(
            Context ctx,
            LinearLayout root,
            OsDesignSystem.Profile skin,
            ThemeTokens theme,
            Runnable onToggleServer,
            View.OnTouchListener springTouch) {
        HeaderRefs refs = new HeaderRefs();
        OsDesignSystem.SkinId id = skin.id;

        if (id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9 && skin.pinnedTopToolbar) {
            LinearLayout appBar = new LinearLayout(ctx);
            appBar.setOrientation(LinearLayout.HORIZONTAL);
            appBar.setGravity(Gravity.CENTER_VERTICAL);
            int barBg = theme.isLight ? theme.primary : Color.parseColor("#202124");
            int barFg = theme.isLight ? theme.onPrimary : theme.onSurface;
            appBar.setBackgroundColor(barBg);
            appBar.setElevation(dp(ctx, 4));
            appBar.setPadding(dp(ctx, 16), dp(ctx, 10), dp(ctx, 14), dp(ctx, 10));

            ImageView ico = new ImageView(ctx);
            ico.setImageDrawable(tinted(ctx, R.drawable.ic_m3_brand, barFg, 22));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(ctx, 24), dp(ctx, 24));
            ilp.rightMargin = dp(ctx, 12);
            ico.setLayoutParams(ilp);
            appBar.addView(ico);

            LinearLayout col = new LinearLayout(ctx);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView t = new TextView(ctx);
            t.setText("Expressive Web Files");
            t.setTextColor(barFg);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.5f);
            t.setTypeface(skin.titleTypeface());
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(t);

            TextView sub = new TextView(ctx);
            sub.setText(Build.MODEL + " · Android " + Build.VERSION.RELEASE);
            sub.setTextColor(ThemeTokens.withAlpha(barFg, 205));
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            col.addView(sub);
            appBar.addView(col);

            buildStatusPillInto(ctx, appBar, refs, true, onToggleServer, null);
            refs.pinnedToolbar = appBar;
            refs.darkToolbarText = true;
            return refs;
        }

        if (id == OsDesignSystem.SkinId.ONE_UI) {
            LinearLayout hero = new LinearLayout(ctx);
            hero.setOrientation(LinearLayout.VERTICAL);
            hero.setGravity(Gravity.CENTER_HORIZONTAL);
            hero.setPadding(dp(ctx, 8), dp(ctx, 26), dp(ctx, 8), dp(ctx, 18));

            TextView t = new TextView(ctx);
            t.setText("Expressive Web Files");
            t.setTextColor(theme.onSurface);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            t.setGravity(Gravity.CENTER);
            t.setTypeface(skin.heroTitleTypeface());
            t.setLetterSpacing(-0.02f);
            hero.addView(t);

            TextView sub = new TextView(ctx);
            sub.setText(Build.MODEL + " · " + skin.displayName);
            sub.setTextColor(theme.onSurfaceVariant);
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            sub.setGravity(Gravity.CENTER);
            sub.setPadding(0, dp(ctx, 4), 0, dp(ctx, 12));
            hero.addView(sub);

            buildStatusPillInto(ctx, hero, refs, false, onToggleServer, springTouch);
            root.addView(hero);
            return refs;
        }

        if (id == OsDesignSystem.SkinId.ANDROID_10_11) {
            LinearLayout gHead = new LinearLayout(ctx);
            gHead.setOrientation(LinearLayout.VERTICAL);
            gHead.setGravity(Gravity.CENTER_HORIZONTAL);
            gHead.setPadding(dp(ctx, 6), dp(ctx, 8), dp(ctx, 6), dp(ctx, 10));

            View gDots = createGoogleFourDotsView(ctx);
            LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(dp(ctx, 68), dp(ctx, 14));
            glp.bottomMargin = dp(ctx, 6);
            gDots.setLayoutParams(glp);
            gHead.addView(gDots);

            TextView t = new TextView(ctx);
            t.setText("Expressive Web Files");
            t.setTextColor(theme.onSurface);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
            t.setTypeface(skin.titleTypeface());
            t.setGravity(Gravity.CENTER);
            gHead.addView(t);

            TextView sub = new TextView(ctx);
            sub.setText(Build.MODEL + " · Android " + Build.VERSION.RELEASE);
            sub.setTextColor(theme.onSurfaceVariant);
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            sub.setPadding(0, dp(ctx, 2), 0, dp(ctx, 10));
            gHead.addView(sub);

            buildStatusPillInto(ctx, gHead, refs, false, onToggleServer, null);
            root.addView(gHead);
            return refs;
        }

        if (id == OsDesignSystem.SkinId.COLOR_OS || id == OsDesignSystem.SkinId.MAGIC_OS) {
            boolean isMagic = (id == OsDesignSystem.SkinId.MAGIC_OS);
            LinearLayout capWrap = new LinearLayout(ctx);
            capWrap.setOrientation(LinearLayout.HORIZONTAL);
            capWrap.setGravity(Gravity.CENTER);
            capWrap.setPadding(0, dp(ctx, 2), 0, dp(ctx, 10));

            LinearLayout cap = new LinearLayout(ctx);
            cap.setOrientation(LinearLayout.HORIZONTAL);
            cap.setGravity(Gravity.CENTER_VERTICAL);
            int capBg = Color.parseColor(isMagic ? "#0C1018" : "#12161F");
            int capStroke = Color.parseColor(isMagic ? "#3B82FF" : "#1AD1A5");
            cap.setBackground(rounded(ctx, capBg, capStroke, 999));
            cap.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 14), dp(ctx, 6));
            cap.setOnClickListener(v -> onToggleServer.run());

            View capDot = new View(ctx);
            capDot.setBackground(rounded(ctx, capStroke, 0, 999));
            LinearLayout.LayoutParams cdp = new LinearLayout.LayoutParams(dp(ctx, 8), dp(ctx, 8));
            cdp.rightMargin = dp(ctx, 7);
            capDot.setLayoutParams(cdp);
            cap.addView(capDot);

            TextView capTv = new TextView(ctx);
            capTv.setText(isMagic ? "Magic Capsule · Servidor LAN" : "Aqua Dynamics · Portal Web");
            capTv.setTextColor(Color.WHITE);
            capTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            capTv.setTypeface(Typeface.DEFAULT_BOLD);
            capTv.setSingleLine(true);
            capTv.setEllipsize(TextUtils.TruncateAt.END);
            cap.addView(capTv);
            capWrap.addView(cap);
            root.addView(capWrap);
            refs.extraSubBannerText = capTv;
        }

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(ctx, 2), dp(ctx, 4), dp(ctx, 2), dp(ctx, 6));

        if (id == OsDesignSystem.SkinId.MATERIAL_EXPRESSIVE) {
            ImageView logoBadge = new ImageView(ctx);
            logoBadge.setImageDrawable(tinted(ctx, R.drawable.ic_m3_brand, theme.onPrimaryContainer, 24));
            logoBadge.setBackground(rounded(ctx, theme.primaryContainer, 0, skin.innerRadiusDp));
            logoBadge.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
            LinearLayout.LayoutParams lbLp = new LinearLayout.LayoutParams(dp(ctx, 48), dp(ctx, 48));
            lbLp.rightMargin = dp(ctx, 14);
            logoBadge.setLayoutParams(lbLp);
            header.addView(logoBadge);
        } else if (id == OsDesignSystem.SkinId.NOTHING_OS) {
            View dotBadge = createNothingDotBadge(ctx, theme);
            LinearLayout.LayoutParams dbLp = new LinearLayout.LayoutParams(dp(ctx, 44), dp(ctx, 44));
            dbLp.rightMargin = dp(ctx, 12);
            dotBadge.setLayoutParams(dbLp);
            header.addView(dotBadge);
        } else if (id == OsDesignSystem.SkinId.REALME_UI) {
            TextView gtTag = new TextView(ctx);
            gtTag.setText("GT");
            gtTag.setTextColor(Color.parseColor("#141416"));
            gtTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            gtTag.setTypeface(Typeface.DEFAULT_BOLD);
            gtTag.setGravity(Gravity.CENTER);
            gtTag.setBackground(rounded(ctx, Color.parseColor("#FFC400"), 0, 10));
            LinearLayout.LayoutParams gtLp = new LinearLayout.LayoutParams(dp(ctx, 38), dp(ctx, 38));
            gtLp.rightMargin = dp(ctx, 10);
            gtTag.setLayoutParams(gtLp);
            header.addView(gtTag);
        }

        LinearLayout titleCol = new LinearLayout(ctx);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(ctx);
        title.setText(id == OsDesignSystem.SkinId.NOTHING_OS
                ? "EXPRESSIVE WEB FILES"
                : "Expressive Web Files");
        title.setTextColor(theme.onSurface);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, skin.titleTextSizeSp());
        title.setMaxLines(2);
        title.setTypeface(skin.titleTypeface());
        title.setLetterSpacing(skin.titleLetterSpacing());
        titleCol.addView(title);

        TextView sub = new TextView(ctx);
        String subTxt = Build.MODEL + " · " + skin.displayName;
        if (id == OsDesignSystem.SkinId.HYPER_OS) {
            subTxt = "● HyperConnect · " + skin.displayName;
        }
        sub.setText(id == OsDesignSystem.SkinId.NOTHING_OS
                ? subTxt.toUpperCase(Locale.US)
                : subTxt);
        sub.setTextColor(id == OsDesignSystem.SkinId.HYPER_OS ? theme.tertiary : theme.onSurfaceVariant);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, id == OsDesignSystem.SkinId.NOTHING_OS ? 11 : 12.5f);
        if (id == OsDesignSystem.SkinId.NOTHING_OS) {
            sub.setTypeface(Typeface.MONOSPACE);
        }
        titleCol.addView(sub);
        header.addView(titleCol);

        buildStatusPillInto(ctx, header, refs, false, onToggleServer, springTouch);
        root.addView(header);

        if (id == OsDesignSystem.SkinId.EMUI) {
            LinearLayout hBar = new LinearLayout(ctx);
            hBar.setOrientation(LinearLayout.HORIZONTAL);
            hBar.setGravity(Gravity.CENTER_VERTICAL);
            hBar.setBackground(rounded(ctx, theme.surfaceContainerHigh, 0, 999));
            hBar.setPadding(dp(ctx, 14), dp(ctx, 9), dp(ctx, 14), dp(ctx, 9));
            LinearLayout.LayoutParams hbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hbLp.topMargin = dp(ctx, 4);
            hbLp.bottomMargin = dp(ctx, 2);
            hBar.setLayoutParams(hbLp);
            hBar.setOnClickListener(v -> onToggleServer.run());

            ImageView hIco = new ImageView(ctx);
            hIco.setImageDrawable(tinted(ctx, R.drawable.ic_m3_dns, theme.primary, 16));
            LinearLayout.LayoutParams hilp = new LinearLayout.LayoutParams(dp(ctx, 16), dp(ctx, 16));
            hilp.rightMargin = dp(ctx, 8);
            hIco.setLayoutParams(hilp);
            hBar.addView(hIco);

            TextView hTv = new TextView(ctx);
            hTv.setText("HarmonyOS Servicio · Cargando dirección...");
            hTv.setTextColor(theme.onSurfaceVariant);
            hTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            hTv.setSingleLine(true);
            hTv.setEllipsize(TextUtils.TruncateAt.END);
            hBar.addView(hTv);
            root.addView(hBar);
            refs.extraSubBannerText = hTv;
        }

        return refs;
    }

    private static void buildStatusPillInto(
            Context ctx,
            LinearLayout parent,
            HeaderRefs refs,
            boolean compactLightFg,
            Runnable onToggleServer,
            View.OnTouchListener springTouch) {
        LinearLayout pill = new LinearLayout(ctx);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(ctx, 11), dp(ctx, 6), dp(ctx, 13), dp(ctx, 6));
        if (springTouch != null) {
            pill.setOnTouchListener(springTouch);
        }
        pill.setOnClickListener(v -> onToggleServer.run());

        View dot = new View(ctx);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(ctx, 8), dp(ctx, 8));
        dotLp.rightMargin = dp(ctx, 6);
        dot.setLayoutParams(dotLp);
        pill.addView(dot);

        TextView txt = new TextView(ctx);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, compactLightFg ? 11.5f : 12f);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        pill.addView(txt);
        parent.addView(pill);

        refs.statusPill = pill;
        refs.pulseDot = dot;
        refs.statusText = txt;
    }

    private static View createGoogleFourDotsView(Context ctx) {
        return new View(ctx) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final int[] cols = new int[] {
                    Color.parseColor("#4285F4"),
                    Color.parseColor("#EA4335"),
                    Color.parseColor("#FBBC04"),
                    Color.parseColor("#34A853")
            };
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;
                float r = Math.min(w / 11f, h * 0.36f);
                float cy = h * 0.5f;
                float startX = w * 0.5f - (3f * r * 1.65f);
                for (int i = 0; i < 4; i++) {
                    p.setColor(cols[i]);
                    canvas.drawCircle(startX + i * (r * 3.3f), cy, r, p);
                }
            }
        };
    }

    private static View createNothingDotBadge(Context ctx, ThemeTokens theme) {
        final int bgCol = theme.surfaceContainerHigh;
        final int borderCol = theme.outlineVariant;
        final int onCol = theme.onSurface;
        final int redCol = Color.parseColor("#D71921");
        return new View(ctx) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            {
                setBackground(rounded(ctx, bgCol, borderCol, 12));
            }
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;
                float padPx = w * 0.22f;
                float step = (w - padPx * 2f) / 4f;
                float r = Math.max(2f, w * 0.048f);
                int[][] mask = {
                        {0, 0, 1, 0, 2},
                        {0, 1, 1, 1, 0},
                        {1, 0, 1, 0, 1},
                        {0, 1, 1, 1, 0},
                        {0, 0, 1, 0, 0}
                };
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        int v = mask[row][col];
                        if (v == 0) p.setColor(ThemeTokens.withAlpha(onCol, 45));
                        else if (v == 2) p.setColor(redCol);
                        else p.setColor(onCol);
                        canvas.drawCircle(padPx + col * step, padPx + row * step, r, p);
                    }
                }
            }
        };
    }

    /**
     * Applies OEM signature hero surface treatments (Aquamorphic water gradient,
     * HyperOS frosted sheen, Magic Portal rim, realme GT stripe, HarmonyOS bottom pill).
     */
    static void applyHeroCardDecoration(
            Context ctx,
            LinearLayout card,
            OsDesignSystem.Profile skin,
            ThemeTokens theme,
            boolean isPrimarySpeedHero) {
        if (card == null || skin == null) return;
        float rPx = dp(ctx, isPrimarySpeedHero ? skin.heroRadiusDp : skin.cardRadiusDp);
        switch (skin.heroCardStyle) {
            case AQUAMORPHIC_GRADIENT: {
                int[] cols = theme.isLight
                        ? new int[] { Color.parseColor("#EAF2FF"), Color.parseColor("#E6FAF4") }
                        : new int[] { Color.parseColor("#16233B"), Color.parseColor("#112926") };
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, cols);
                gd.setCornerRadius(rPx);
                gd.setStroke(dp(ctx, 1), ThemeTokens.withAlpha(theme.primary, theme.isLight ? 45 : 70));
                card.setBackground(gd);
                break;
            }
            case HYPER_BENTO_FROST: {
                int topCol = theme.isLight ? Color.parseColor("#FFFFFF") : Color.parseColor("#1F222C");
                int botCol = theme.isLight ? Color.parseColor("#F1F4FC") : Color.parseColor("#171922");
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{topCol, botCol});
                gd.setCornerRadius(rPx);
                gd.setStroke(dp(ctx, 1), ThemeTokens.withAlpha(theme.outlineVariant, 0x55));
                card.setBackground(gd);
                break;
            }
            case MAGIC_PORTAL_RIM: {
                int[] cols = theme.isLight
                        ? new int[] { Color.parseColor("#FFFFFF"), Color.parseColor("#EDF3FF") }
                        : new int[] { Color.parseColor("#171B26"), Color.parseColor("#142038") };
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, cols);
                gd.setCornerRadius(rPx);
                gd.setStroke(Math.max(2, dp(ctx, 1.5f)), ThemeTokens.withAlpha(theme.primary, theme.isLight ? 90 : 120));
                card.setBackground(gd);
                break;
            }
            case REALME_GT_STRIPE: {
                if (isPrimarySpeedHero) {
                    View stripe = new View(ctx);
                    stripe.setBackground(rounded(ctx, Color.parseColor("#FFC400"), 0, 999));
                    LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(ctx, 44), dp(ctx, 4));
                    slp.bottomMargin = dp(ctx, 10);
                    stripe.setLayoutParams(slp);
                    card.addView(stripe, 0);
                }
                break;
            }
            case HARMONY_SERVICE_CARD: {
                if (!isPrimarySpeedHero) {
                    View bar = new View(ctx);
                    bar.setBackground(rounded(ctx, ThemeTokens.withAlpha(theme.outline, 130), 0, 999));
                    LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(ctx, 30), dp(ctx, 3.5f));
                    blp.gravity = Gravity.CENTER_HORIZONTAL;
                    blp.topMargin = dp(ctx, 12);
                    bar.setLayoutParams(blp);
                    card.addView(bar);
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Renders OS-authentic section headers: external subheaders floating outside the card
     * on One UI, saturated squircle icon badges on EMUI/MagicOS, colored Pie circles on Android 9, etc.
     */
    static LinearLayout addSectionHeader(
            Context ctx,
            LinearLayout externalParent,
            LinearLayout cardTarget,
            String text,
            int iconRes,
            int semanticTint,
            OsDesignSystem.Profile skin,
            ThemeTokens theme,
            boolean isExternalCandidate) {
        if (skin.externalSectionHeaders && isExternalCandidate && externalParent != null && cardTarget != null) {
            int idx = externalParent.indexOfChild(cardTarget);
            TextView extLabel = new TextView(ctx);
            extLabel.setText(text);
            extLabel.setTextColor(theme.onSurfaceVariant);
            extLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            extLabel.setTypeface(skin.sectionTypeface());
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            elp.topMargin = dp(ctx, 14);
            elp.bottomMargin = dp(ctx, 4);
            elp.leftMargin = dp(ctx, 10);
            extLabel.setLayoutParams(elp);
            if (idx >= 0) {
                externalParent.addView(extLabel, idx);
                LinearLayout.LayoutParams clp = (LinearLayout.LayoutParams) cardTarget.getLayoutParams();
                if (clp != null) {
                    clp.topMargin = dp(ctx, 2);
                    cardTarget.setLayoutParams(clp);
                }
            } else {
                externalParent.addView(extLabel);
            }
            return null;
        }

        LinearLayout row = buildSectionHeaderRow(ctx, text, iconRes, semanticTint, skin, theme);
        if (cardTarget != null) {
            cardTarget.addView(row);
        }
        return row;
    }

    static LinearLayout buildSectionHeaderRow(
            Context ctx,
            String text,
            int iconRes,
            int semanticTint,
            OsDesignSystem.Profile skin,
            ThemeTokens theme) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(ctx, 12));

        OsDesignSystem.SkinId id = skin.id;
        if (id == OsDesignSystem.SkinId.EMUI || id == OsDesignSystem.SkinId.MAGIC_OS
                || id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9 || id == OsDesignSystem.SkinId.ANDROID_10_11) {
            int badgeRad = (id == OsDesignSystem.SkinId.EMUI || id == OsDesignSystem.SkinId.MAGIC_OS) ? 10 : 999;
            int boxDp = (id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9) ? 28 : 26;
            FrameLayout badge = new FrameLayout(ctx);
            boolean filledWhiteIcon = (id != OsDesignSystem.SkinId.ANDROID_10_11);
            int bg = filledWhiteIcon ? semanticTint : ThemeTokens.withAlpha(semanticTint, 32);
            int fg = filledWhiteIcon ? Color.WHITE : semanticTint;
            badge.setBackground(rounded(ctx, bg, 0, badgeRad));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(ctx, boxDp), dp(ctx, boxDp));
            blp.rightMargin = dp(ctx, 10);
            badge.setLayoutParams(blp);
            ImageView iv = new ImageView(ctx);
            iv.setImageDrawable(tinted(ctx, iconRes, fg, 15));
            badge.addView(iv, new FrameLayout.LayoutParams(dp(ctx, 15), dp(ctx, 15), Gravity.CENTER));
            row.addView(badge);
        } else {
            ImageView iv = new ImageView(ctx);
            iv.setImageDrawable(tinted(ctx, iconRes, semanticTint, 19));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(ctx, 19), dp(ctx, 19));
            ilp.rightMargin = dp(ctx, 8);
            iv.setLayoutParams(ilp);
            row.addView(iv);
        }

        TextView tv = new TextView(ctx);
        String trText = I18n.tr(text);
        tv.setText(skin.uppercaseSectionHeaders ? trText.toUpperCase(Locale.getDefault()) : trText);
        tv.setTextColor(id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9 ? semanticTint : theme.onSurface);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, skin.uppercaseSectionHeaders ? 13f : 15.5f);
        tv.setTypeface(skin.sectionTypeface());
        tv.setLetterSpacing(skin.uppercaseSectionHeaders ? 0.06f : skin.titleLetterSpacing());
        row.addView(tv);
        return row;
    }

    /**
     * Resolves OEM-specific folder/archive/media badge colors & corner styles for file explorer rows.
     */
    static BadgeSpec resolveExplorerBadge(
            OsDesignSystem.Profile skin,
            ThemeTokens theme,
            boolean isDir,
            boolean isArchive,
            boolean isMedia,
            boolean selected) {
        if (selected) {
            return new BadgeSpec(theme.primary, theme.onPrimary, 999, 0);
        }
        switch (skin.folderBadgeStyle) {
            case SAMSUNG_GOLD:
                if (isDir) return new BadgeSpec(Color.parseColor("#FFB300"), Color.WHITE, 12, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#8E6CE8"), Color.WHITE, 12, 0);
                if (isMedia) return new BadgeSpec(Color.parseColor("#00A396"), Color.WHITE, 12, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 12, 0);

            case XIAOMI_SKY:
                if (isDir) return new BadgeSpec(Color.parseColor("#2B95FF"), Color.WHITE, 11, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#FF8C24"), Color.WHITE, 11, 0);
                if (isMedia) return new BadgeSpec(Color.parseColor("#00B578"), Color.WHITE, 11, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 11, 0);

            case HARMONY_BLUE:
                if (isDir) return new BadgeSpec(Color.parseColor("#0A59F7"), Color.WHITE, 10, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#F58220"), Color.WHITE, 10, 0);
                if (isMedia) return new BadgeSpec(Color.parseColor("#00A48A"), Color.WHITE, 10, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 10, 0);

            case MAGIC_CYAN:
                if (isDir) return new BadgeSpec(Color.parseColor("#1D6FFF"), Color.WHITE, 12, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#0097B2"), Color.WHITE, 12, 0);
                if (isMedia) return new BadgeSpec(Color.parseColor("#5B4DFF"), Color.WHITE, 12, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 12, 0);

            case COLOROS_AQUA:
                if (isDir) return new BadgeSpec(Color.parseColor("#009688"), Color.WHITE, 12, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#0066FF"), Color.WHITE, 12, 0);
                if (isMedia) return new BadgeSpec(Color.parseColor("#20B486"), Color.WHITE, 12, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 12, 0);

            case REALME_YELLOW:
                if (isDir) return new BadgeSpec(Color.parseColor("#FFC400"), Color.parseColor("#141416"), 10, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#1E2028"), Color.parseColor("#FFC400"), 10, 0);
                if (isMedia) return new BadgeSpec(theme.tertiaryContainer, theme.onTertiaryContainer, 10, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 10, 0);

            case GOOGLE_PIE_CIRCLE:
                if (isDir) return new BadgeSpec(Color.parseColor("#1A73E8"), Color.WHITE, 999, 0);
                if (isArchive) return new BadgeSpec(Color.parseColor("#F29900"), Color.WHITE, 999, 0);
                if (isMedia) return new BadgeSpec(Color.parseColor("#00897B"), Color.WHITE, 999, 0);
                return new BadgeSpec(Color.parseColor("#5F6368"), Color.WHITE, 999, 0);

            case GOOGLE_OUTLINED:
                if (isDir) return new BadgeSpec(theme.primaryContainer, theme.onPrimaryContainer, 999, theme.outlineVariant);
                if (isArchive) return new BadgeSpec(theme.tertiaryContainer, theme.onTertiaryContainer, 999, theme.outlineVariant);
                if (isMedia) return new BadgeSpec(theme.secondaryContainer, theme.onSecondaryContainer, 999, theme.outlineVariant);
                return new BadgeSpec(theme.surfaceContainerHigh, theme.onSurfaceVariant, 999, theme.outlineVariant);

            case NOTHING_MONO:
                int border = theme.outlineVariant;
                if (isDir) return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurface, 10, border);
                if (isArchive || isMedia) return new BadgeSpec(theme.surfaceContainerHigh, Color.parseColor("#D71921"), 10, border);
                return new BadgeSpec(theme.surfaceContainerLow, theme.onSurfaceVariant, 10, border);

            case M3_TONAL:
            default:
                if (isDir) return new BadgeSpec(theme.primaryContainer, theme.onPrimaryContainer, 13, 0);
                if (isArchive) return new BadgeSpec(theme.tertiaryContainer, theme.onTertiaryContainer, 13, 0);
                if (isMedia) return new BadgeSpec(theme.secondaryContainer, theme.onSecondaryContainer, 13, 0);
                return new BadgeSpec(theme.surfaceContainerHighest, theme.onSurfaceVariant, 13, 0);
        }
    }

    static View createGroupedDivider(Context ctx, ThemeTokens theme, int leftIndentDp) {
        View v = new View(ctx);
        v.setBackgroundColor(ThemeTokens.withAlpha(theme.outlineVariant, 0x7A));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.leftMargin = dp(ctx, leftIndentDp);
        v.setLayoutParams(lp);
        return v;
    }

    static TextView createRowChevron(Context ctx, ThemeTokens theme) {
        TextView tv = new TextView(ctx);
        tv.setText("›");
        tv.setTextColor(ThemeTokens.withAlpha(theme.onSurfaceVariant, 180));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv.setPadding(dp(ctx, 4), 0, dp(ctx, 2), 0);
        return tv;
    }

    static void maybeAddBottomSheetHandle(Context ctx, LinearLayout box, OsDesignSystem.Profile skin, ThemeTokens theme) {
        if (skin == null || !skin.bottomSheetHandle) return;
        View handle = new View(ctx);
        handle.setBackground(rounded(ctx, ThemeTokens.withAlpha(theme.outline, 130), 0, 999));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 4));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = dp(ctx, 12);
        handle.setLayoutParams(lp);
        box.addView(handle);
    }

    /**
     * Builds the custom bottom navigation tab matching each OS indicator paradigm.
     */
    static NavTabHolder buildNavTab(
            Context ctx,
            String label,
            int iconRes,
            OsDesignSystem.Profile skin,
            ThemeTokens theme,
            int rippleColor,
            int widthPx,
            boolean isFloating,
            View.OnClickListener onClick) {
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        root.setClickable(true);
        root.setFocusable(true);
        root.setOnClickListener(onClick);
        LinearLayout.LayoutParams rlp = (widthPx > 0)
                ? new LinearLayout.LayoutParams(widthPx, dp(ctx, 52))
                : new LinearLayout.LayoutParams(0, dp(ctx, 54), 1f);
        if (isFloating) {
            rlp.leftMargin = dp(ctx, 2);
            rlp.rightMargin = dp(ctx, 2);
        }
        root.setLayoutParams(rlp);

        View topBar = new View(ctx);
        LinearLayout.LayoutParams tblp = new LinearLayout.LayoutParams(dp(ctx, 32), dp(ctx, 3));
        tblp.bottomMargin = dp(ctx, 3);
        topBar.setLayoutParams(tblp);
        topBar.setVisibility(skin.navIndicatorStyle == OsDesignSystem.NavIndicatorStyle.GOOGLE_TOP_BAR
                ? View.INVISIBLE : View.GONE);
        root.addView(topBar);

        FrameLayout iconWrap = new FrameLayout(ctx);
        LinearLayout.LayoutParams iwlp = new LinearLayout.LayoutParams(dp(ctx, 54), dp(ctx, 26));
        iwlp.bottomMargin = dp(ctx, 2);
        iconWrap.setLayoutParams(iwlp);

        ImageView iconView = new ImageView(ctx);
        iconView.setImageDrawable(tinted(ctx, iconRes, theme.onSurfaceVariant, 18));
        iconWrap.addView(iconView, new FrameLayout.LayoutParams(dp(ctx, 18), dp(ctx, 18), Gravity.CENTER));
        root.addView(iconWrap);

        TextView labelView = new TextView(ctx);
        labelView.setText(label);
        labelView.setGravity(Gravity.CENTER);
        labelView.setSingleLine(true);
        labelView.setAutoSizeTextTypeUniformWithConfiguration(8, 11, 1, TypedValue.COMPLEX_UNIT_SP);
        labelView.setPadding(dp(ctx, 2), 0, dp(ctx, 2), 0);
        labelView.setTextColor(theme.onSurfaceVariant);
        labelView.setTypeface(skin.bodyMediumTypeface());
        root.addView(labelView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        View bottomBar = new View(ctx);
        boolean isNothing = (skin.id == OsDesignSystem.SkinId.NOTHING_OS);
        boolean hasOneUiUnderline = (skin.navIndicatorStyle == OsDesignSystem.NavIndicatorStyle.ONE_UI_UNDERLINE);
        LinearLayout.LayoutParams bblp = isNothing
                ? new LinearLayout.LayoutParams(dp(ctx, 4), dp(ctx, 4))
                : new LinearLayout.LayoutParams(dp(ctx, 20), dp(ctx, 3));
        bblp.topMargin = dp(ctx, 2);
        bottomBar.setLayoutParams(bblp);
        bottomBar.setVisibility((hasOneUiUnderline || isNothing) ? View.INVISIBLE : View.GONE);
        root.addView(bottomBar);

        return new NavTabHolder(root, topBar, iconWrap, iconView, labelView, bottomBar, iconRes);
    }

    static void styleNavTab(
            Context ctx,
            NavTabHolder tab,
            boolean active,
            OsDesignSystem.Profile skin,
            ThemeTokens theme,
            int rippleColor) {
        if (tab == null) return;
        int fg = active ? theme.primary : theme.onSurfaceVariant;
        tab.iconWrap.setBackground(null);
        tab.root.setBackground(ripple(ctx, Color.TRANSPARENT, rippleColor, 0, 14));

        switch (skin.navIndicatorStyle) {
            case FLOATING_CAPSULE: {
                int bg = active ? theme.primaryContainer : Color.TRANSPARENT;
                int capFg = active ? theme.onPrimaryContainer : theme.onSurfaceVariant;
                int rad = (skin.buttonRadiusDp == 999) ? 26 : skin.buttonRadiusDp;
                tab.root.setBackground(ripple(ctx, bg, rippleColor, 0, rad));
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, capFg, 18));
                tab.labelView.setTextColor(capFg);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                if (skin.id == OsDesignSystem.SkinId.NOTHING_OS) {
                    tab.bottomBar.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
                    tab.bottomBar.setBackground(rounded(ctx, Color.parseColor("#D71921"), 0, 999));
                }
                break;
            }
            case M3_ICON_PILL: {
                if (active) {
                    tab.iconWrap.setBackground(rounded(ctx, theme.secondaryContainer, 0, 999));
                }
                int icoCol = active ? theme.onSecondaryContainer : theme.onSurfaceVariant;
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, icoCol, 18));
                tab.labelView.setTextColor(active ? theme.onSurface : theme.onSurfaceVariant);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                break;
            }
            case REALME_YELLOW_PILL: {
                if (active) {
                    tab.iconWrap.setBackground(rounded(ctx, Color.parseColor("#FFC400"), 0, 999));
                }
                int icoCol = active ? Color.parseColor("#141416") : theme.onSurfaceVariant;
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, icoCol, 18));
                tab.labelView.setTextColor(active ? theme.onSurface : theme.onSurfaceVariant);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                break;
            }
            case ONE_UI_UNDERLINE: {
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, fg, 18));
                tab.labelView.setTextColor(fg);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                tab.bottomBar.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
                tab.bottomBar.setBackground(rounded(ctx, theme.primary, 0, 999));
                break;
            }
            case GOOGLE_TOP_BAR: {
                tab.topBar.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
                tab.topBar.setBackground(rounded(ctx, theme.primary, 0, 999));
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, fg, 18));
                tab.labelView.setTextColor(fg);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                break;
            }
            case HARMONY_TINT: {
                if (active && skin.id == OsDesignSystem.SkinId.COLOR_OS) {
                    tab.iconWrap.setBackground(rounded(ctx, ThemeTokens.withAlpha(theme.primary, 32), 0, 12));
                }
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, fg, 18));
                tab.labelView.setTextColor(fg);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                break;
            }
            case MD2_TINT_ONLY:
            default: {
                tab.iconView.setImageDrawable(tinted(ctx, tab.iconRes, fg, active ? 19 : 18));
                tab.labelView.setTextColor(fg);
                tab.labelView.setTypeface(active ? skin.sectionTypeface() : skin.bodyMediumTypeface());
                tab.labelView.setScaleX(active ? 1.04f : 1.0f);
                tab.labelView.setScaleY(active ? 1.04f : 1.0f);
                break;
            }
        }
    }
}
