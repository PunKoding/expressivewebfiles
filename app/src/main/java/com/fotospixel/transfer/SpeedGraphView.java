package com.fotospixel.transfer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

public final class SpeedGraphView extends View {

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint upStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint downStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint upFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint downFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private long[] upSamples = new long[30];
    private long[] downSamples = new long[30];
    private int upColor = Color.parseColor("#34D399");
    private int downColor = Color.parseColor("#C084FC");
    private final float density;
    private OsDesignSystem.Profile designProfile;
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SpeedGraphView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;

        gridPaint.setColor(Color.parseColor("#2A364F"));
        gridPaint.setStrokeWidth(1f * density);
        gridPaint.setStyle(Paint.Style.STROKE);

        dotPaint.setColor(Color.parseColor("#44474F"));
        dotPaint.setStyle(Paint.Style.FILL);

        upStrokePaint.setColor(upColor);
        upStrokePaint.setStrokeWidth(2.4f * density);
        upStrokePaint.setStyle(Paint.Style.STROKE);
        upStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        upStrokePaint.setStrokeJoin(Paint.Join.ROUND);

        downStrokePaint.setColor(downColor);
        downStrokePaint.setStrokeWidth(2.4f * density);
        downStrokePaint.setStyle(Paint.Style.STROKE);
        downStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        downStrokePaint.setStrokeJoin(Paint.Join.ROUND);

        upFillPaint.setStyle(Paint.Style.FILL);
        downFillPaint.setStyle(Paint.Style.FILL);
    }

    public void applyDesignProfile(OsDesignSystem.Profile skin, ThemeTokens theme) {
        this.designProfile = skin;
        if (theme == null) return;
        if (skin != null && skin.dotMatrixGraph) {
            int nothingRed = Color.parseColor("#D71921");
            setColors(nothingRed, theme.onSurface, ThemeTokens.withAlpha(theme.onSurfaceVariant, 110));
            dotPaint.setColor(ThemeTokens.withAlpha(theme.onSurfaceVariant, 95));
            upStrokePaint.setStrokeWidth(2.0f * density);
            downStrokePaint.setStrokeWidth(1.8f * density);
        } else if (skin != null && skin.id == OsDesignSystem.SkinId.REALME_UI) {
            setColors(Color.parseColor("#FFC400"), theme.tertiary, ThemeTokens.withAlpha(theme.outlineVariant, 0x80));
            upStrokePaint.setStrokeWidth(2.6f * density);
            downStrokePaint.setStrokeWidth(2.0f * density);
        } else if (skin != null && skin.id == OsDesignSystem.SkinId.COLOR_OS) {
            setColors(Color.parseColor("#007AFF"), Color.parseColor("#00B894"), ThemeTokens.withAlpha(theme.outlineVariant, 0x60));
            upStrokePaint.setStrokeWidth(2.6f * density);
            downStrokePaint.setStrokeWidth(2.2f * density);
        } else if (skin != null && skin.id == OsDesignSystem.SkinId.HYPER_OS) {
            setColors(theme.primary, theme.tertiary, ThemeTokens.withAlpha(theme.outlineVariant, 0x55));
            upStrokePaint.setStrokeWidth(2.8f * density);
            downStrokePaint.setStrokeWidth(2.5f * density);
        } else if (skin != null && skin.id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9) {
            setColors(theme.primary, theme.tertiary, ThemeTokens.withAlpha(theme.outlineVariant, 0x70));
            upStrokePaint.setStrokeWidth(2.0f * density);
            downStrokePaint.setStrokeWidth(2.0f * density);
        } else {
            setColors(theme.primary, theme.tertiary, ThemeTokens.withAlpha(theme.outlineVariant, 0x70));
            upStrokePaint.setStrokeWidth(2.4f * density);
            downStrokePaint.setStrokeWidth(2.4f * density);
        }
    }

    public void setColors(int uploadColor, int downloadColor, int gridColor) {
        this.upColor = uploadColor;
        this.downColor = downloadColor;
        upStrokePaint.setColor(uploadColor);
        downStrokePaint.setColor(downloadColor);
        gridPaint.setColor(gridColor);
        invalidate();
    }

    public void updateSamples(long[] uploadHistory, long[] downloadHistory) {
        if (uploadHistory != null) {
            this.upSamples = uploadHistory;
        }
        if (downloadHistory != null) {
            this.downSamples = downloadHistory;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        float padTop = h * 0.12f;
        float padBottom = h * 0.10f;
        float plotH = Math.max(1f, h - padTop - padBottom);

        // Grid background (dot matrix on Nothing OS, subtle horizontal guides otherwise)
        if (designProfile != null && designProfile.dotMatrixGraph) {
            float step = Math.max(10f, 14f * density);
            float dotR = Math.max(1.1f, 1.3f * density);
            for (float gy = padTop; gy <= h - padBottom; gy += step) {
                for (float gx = step * 0.5f; gx < w; gx += step) {
                    canvas.drawCircle(gx, gy, dotR, dotPaint);
                }
            }
        } else {
            canvas.drawLine(0f, padTop + plotH * 0.25f, w, padTop + plotH * 0.25f, gridPaint);
            canvas.drawLine(0f, padTop + plotH * 0.62f, w, padTop + plotH * 0.62f, gridPaint);
            canvas.drawLine(0f, h - padBottom, w, h - padBottom, gridPaint);
        }

        long maxVal = 100L * 1024L;
        for (long v : upSamples) {
            if (v > maxVal) maxVal = v;
        }
        for (long v : downSamples) {
            if (v > maxVal) maxVal = v;
        }

        drawSeries(canvas, downSamples, maxVal, w, h, padTop, plotH, padBottom, downStrokePaint, downFillPaint, downColor);
        drawSeries(canvas, upSamples, maxVal, w, h, padTop, plotH, padBottom, upStrokePaint, upFillPaint, upColor);
    }

    private void drawSeries(Canvas canvas, long[] samples, long maxVal, int w, int h,
                            float padTop, float plotH, float padBottom,
                            Paint strokePaint, Paint fillPaint, int color) {
        if (samples == null || samples.length < 2) {
            return;
        }
        int count = samples.length;
        float[] xs = new float[count];
        float[] ys = new float[count];
        float baseY = h - padBottom;

        for (int i = 0; i < count; i++) {
            xs[i] = (i / (float) (count - 1)) * w;
            float norm = Math.min(1f, Math.max(0f, samples[i] / (float) maxVal));
            ys[i] = baseY - norm * plotH;
        }

        boolean linearSegs = designProfile != null
                && (designProfile.dotMatrixGraph || designProfile.id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9);

        Path linePath = new Path();
        linePath.moveTo(xs[0], ys[0]);
        for (int i = 0; i < count - 1; i++) {
            if (linearSegs) {
                linePath.lineTo(xs[i + 1], ys[i + 1]);
            } else {
                float midX = (xs[i] + xs[i + 1]) * 0.5f;
                linePath.cubicTo(midX, ys[i], midX, ys[i + 1], xs[i + 1], ys[i + 1]);
            }
        }

        Path fillPath = new Path(linePath);
        fillPath.lineTo(xs[count - 1], baseY);
        fillPath.lineTo(xs[0], baseY);
        fillPath.close();

        int fillTopAlpha = (designProfile != null && designProfile.dotMatrixGraph) ? 28
                : ((designProfile != null && designProfile.id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9) ? 40 : 72);
        int alphaTop = Color.argb(fillTopAlpha, Color.red(color), Color.green(color), Color.blue(color));
        int alphaBot = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
        fillPaint.setShader(new LinearGradient(0f, padTop, 0f, baseY, alphaTop, alphaBot, Shader.TileMode.CLAMP));

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, strokePaint);
    }
}
