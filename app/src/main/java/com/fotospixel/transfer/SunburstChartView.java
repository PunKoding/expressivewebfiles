package com.fotospixel.transfer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.TextPaint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interactive DaisyDisk / SquirrelDisk concentric Sunburst ring chart styled with
 * Material 3 Expressive tokens. Supports 3 concentric rings, clockwise sweep reveal,
 * tap-to-drill-down on folders, center-hub tap to zoom out, and long-press to queue
 * any slice into the space-freeing collector.
 */
public class SunburstChartView extends View {

    public interface OnNodeActionListener {
        void onFocusChanged(StorageAnalyzer.Node newFocus);
        void onFileTap(StorageAnalyzer.Node fileNode);
        void onNodeLongPress(StorageAnalyzer.Node node);
    }

    private static final class ArcHit {
        final StorageAnalyzer.Node node;
        final int depth; // 1..3
        final float rIn;
        final float rOut;
        final float startDeg; // clockwise from 12 o'clock (-90 canvas)
        final float sweepDeg;
        final int fillColor;
        final boolean isOtherSlice;

        ArcHit(StorageAnalyzer.Node node, int depth, float rIn, float rOut,
               float startDeg, float sweepDeg, int fillColor, boolean isOtherSlice) {
            this.node = node;
            this.depth = depth;
            this.rIn = rIn;
            this.rOut = rOut;
            this.startDeg = startDeg;
            this.sweepDeg = sweepDeg;
            this.fillColor = fillColor;
            this.isOtherSlice = isOtherSlice;
        }
    }

    private ThemeTokens tokens;
    private int[] palette;
    private StorageAnalyzer.Node root;
    private StorageAnalyzer.Node focused;
    private StorageAnalyzer.Node selectedNode;
    private final Set<File> markedFiles = new HashSet<>();
    private final List<ArcHit> arcs = new ArrayList<>();
    private OnNodeActionListener listener;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hubBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint sizePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint cuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF scratchRect = new RectF();

    private float animProgress = 1f;
    private ValueAnimator sweepAnimator;
    private float density;
    private float cx;
    private float cy;
    private float hubR;
    private float[] ringIn = new float[3];
    private float[] ringOut = new float[3];

    private float downX;
    private float downY;
    private boolean longPressTriggered;
    private ArcHit pendingLongPressArc;
    private final int touchSlop;

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (pendingLongPressArc != null && !pendingLongPressArc.isOtherSlice
                    && pendingLongPressArc.node != null && listener != null) {
                longPressTriggered = true;
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                listener.onNodeLongPress(pendingLongPressArc.node);
            }
        }
    };

    public SunburstChartView(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        applyTokens(ThemeTokens.read(context));
    }

    public void applyTokens(ThemeTokens t) {
        applyDesignProfile(t != null ? t.profile : null, t);
    }

    public void applyDesignProfile(OsDesignSystem.Profile skin, ThemeTokens t) {
        this.tokens = t;
        boolean dark = !t.isLight;
        if (skin != null && skin.id == OsDesignSystem.SkinId.NOTHING_OS) {
            this.palette = new int[] {
                    Color.parseColor("#D71921"),
                    t.primary,
                    dark ? Color.parseColor("#A1A1AA") : Color.parseColor("#52525B"),
                    dark ? Color.parseColor("#52525B") : Color.parseColor("#71717A"),
                    dark ? Color.parseColor("#FF6B70") : Color.parseColor("#991B1B"),
                    dark ? Color.parseColor("#D4D4D8") : Color.parseColor("#3F3F46"),
                    dark ? Color.parseColor("#F59E0B") : Color.parseColor("#B45309"),
                    dark ? Color.parseColor("#71717A") : Color.parseColor("#27272A")
            };
        } else {
            this.palette = new int[] {
                    t.primary,
                    t.tertiary,
                    dark ? Color.parseColor("#FFCA57") : Color.parseColor("#D98200"),
                    t.secondary,
                    dark ? Color.parseColor("#66DCB8") : Color.parseColor("#138A68"),
                    dark ? Color.parseColor("#A7AEFF") : Color.parseColor("#545CD6"),
                    dark ? Color.parseColor("#FF95AD") : Color.parseColor("#D14569"),
                    dark ? Color.parseColor("#73D6FF") : Color.parseColor("#0C84B2")
            };
        }

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.BUTT);
        trackPaint.setColor(t.surfaceContainerHigh);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        markStrokePaint.setStyle(Paint.Style.STROKE);
        markStrokePaint.setStrokeCap(Paint.Cap.BUTT);
        markStrokePaint.setStrokeWidth(dp(3));
        markStrokePaint.setColor(t.error);

        hubPaint.setStyle(Paint.Style.FILL);
        hubPaint.setColor(t.surfaceContainerLowest);

        hubBorderPaint.setStyle(Paint.Style.STROKE);
        hubBorderPaint.setStrokeWidth(dp(1.5f));
        hubBorderPaint.setColor(t.outlineVariant);

        Typeface titleTf = (skin != null) ? skin.titleTypeface() : Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        Typeface cueTf = (skin != null) ? skin.sectionTypeface() : Typeface.create(Typeface.DEFAULT, Typeface.BOLD);

        titlePaint.setColor(t.onSurface);
        titlePaint.setTypeface(titleTf);
        titlePaint.setTextSize(sp(13));
        titlePaint.setTextAlign(Paint.Align.CENTER);

        sizePaint.setColor(t.primary);
        sizePaint.setTypeface(titleTf);
        sizePaint.setTextSize(sp(15));
        sizePaint.setTextAlign(Paint.Align.CENTER);

        cuePaint.setColor(t.onSurfaceVariant);
        cuePaint.setTypeface(cueTf);
        cuePaint.setTextSize(sp(10.5f));
        cuePaint.setTextAlign(Paint.Align.CENTER);

        rebuildArcs();
        invalidate();
    }

    public int getPaletteColor(int colorIndex) {
        if (palette == null || palette.length == 0) return tokens != null ? tokens.primary : Color.BLUE;
        return palette[Math.abs(colorIndex) % palette.length];
    }

    public void setOnNodeActionListener(OnNodeActionListener listener) {
        this.listener = listener;
    }

    public void setTree(StorageAnalyzer.Node root, StorageAnalyzer.Node initialFocus) {
        this.root = root;
        this.focused = initialFocus != null ? initialFocus : root;
        this.selectedNode = null;
        rebuildArcs();
        startSweepAnimation();
    }

    public void setFocusedNode(StorageAnalyzer.Node node) {
        if (node == null) return;
        this.focused = node;
        this.selectedNode = null;
        rebuildArcs();
        startSweepAnimation();
    }

    public StorageAnalyzer.Node getFocusedNode() {
        return focused;
    }

    public StorageAnalyzer.Node getRootNode() {
        return root;
    }

    public void setSelectedNode(StorageAnalyzer.Node node) {
        this.selectedNode = node;
        invalidate();
    }

    public void setMarkedFiles(Set<File> files) {
        markedFiles.clear();
        if (files != null) markedFiles.addAll(files);
        invalidate();
    }

    public void refreshData() {
        rebuildArcs();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int side = Math.min(Math.max(w, dp(220)), dp(312));
        setMeasuredDimension(side, side);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float side = Math.min(w, h);
        cx = w * 0.5f;
        cy = h * 0.5f;
        float maxR = Math.max(dp(40), side * 0.5f - dp(8));
        hubR = maxR * 0.30f;
        float ringSpace = (maxR - hubR - dp(6)) / 3f;
        float gap = dp(2.5f);
        for (int i = 0; i < 3; i++) {
            ringIn[i] = hubR + dp(3) + i * ringSpace;
            ringOut[i] = ringIn[i] + Math.max(dp(8), ringSpace - gap);
        }
        rebuildArcs();
    }

    private void rebuildArcs() {
        arcs.clear();
        if (focused == null || focused.size <= 0 || ringOut[0] <= ringIn[0]) return;
        buildRingArcs(focused, 1, 0f, 360f, -1);
    }

    private void buildRingArcs(
            StorageAnalyzer.Node parent,
            int depth,
            float startDeg,
            float availableSweep,
            int branchBaseColor) {
        if (depth > 3 || parent.children.isEmpty() || parent.size <= 0 || availableSweep < 1.8f) {
            return;
        }
        float rIn = ringIn[depth - 1];
        float rOut = ringOut[depth - 1];

        float cursor = startDeg;
        float usedOtherSweep = 0f;
        StorageAnalyzer.Node tailRep = null;

        for (int i = 0; i < parent.children.size(); i++) {
            StorageAnalyzer.Node child = parent.children.get(i);
            if (child.size <= 0) continue;

            float ratio = (float) child.size / (float) parent.size;
            float rawSweep = ratio * availableSweep;

            int baseColor = (depth == 1)
                    ? getPaletteColor(child.colorIndex)
                    : shiftBranchColor(branchBaseColor, i);

            if (rawSweep < 1.35f) {
                usedOtherSweep += rawSweep;
                if (tailRep == null) tailRep = child;
                continue;
            }

            float gapDeg = depth == 1 ? 1.15f : (depth == 2 ? 0.75f : 0.45f);
            float visualSweep = rawSweep > gapDeg * 2f ? rawSweep - gapDeg : rawSweep * 0.85f;
            float visualStart = cursor + (rawSweep - visualSweep) * 0.5f;

            int fill = applyDepthTone(baseColor, depth);
            arcs.add(new ArcHit(child, depth, rIn, rOut, visualStart, visualSweep, fill, false));

            if (child.isDir && !child.children.isEmpty() && depth < 3) {
                buildRingArcs(child, depth + 1, visualStart, visualSweep, baseColor);
            }
            cursor += rawSweep;
        }

        if (usedOtherSweep >= 0.9f && tailRep != null) {
            float gapDeg = 0.6f;
            float visualSweep = usedOtherSweep > gapDeg * 2f ? usedOtherSweep - gapDeg : usedOtherSweep;
            int otherColor = ThemeTokens.withAlpha(tokens.outline, depth == 1 ? 190 : 120);
            arcs.add(new ArcHit(tailRep, depth, rIn, rOut, cursor, visualSweep, otherColor, true));
        }
    }

    private int shiftBranchColor(int baseColor, int childIndex) {
        if (childIndex == 0) return baseColor;
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[0] = (hsv[0] + ((childIndex % 2 == 0) ? 10f : -10f) + 360f) % 360f;
        float shiftV = (childIndex % 3 == 1) ? 0.08f : ((childIndex % 3 == 2) ? -0.07f : 0f);
        hsv[2] = Math.max(0.22f, Math.min(0.96f, hsv[2] + shiftV));
        return Color.HSVToColor(hsv);
    }

    private int applyDepthTone(int color, int depth) {
        if (depth == 1) return color;
        int alpha = depth == 2 ? 212 : 162;
        return ThemeTokens.withAlpha(color, alpha);
    }

    private void startSweepAnimation() {
        if (sweepAnimator != null) sweepAnimator.cancel();
        animProgress = 0.05f;
        sweepAnimator = ValueAnimator.ofFloat(0.05f, 1f);
        sweepAnimator.setDuration(320L);
        sweepAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
        sweepAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animProgress = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        sweepAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tokens == null) return;

        // 1. Concentric track background rings
        for (int i = 0; i < 3; i++) {
            float midR = (ringIn[i] + ringOut[i]) * 0.5f;
            float strokeW = ringOut[i] - ringIn[i];
            if (strokeW <= 0) continue;
            trackPaint.setStrokeWidth(strokeW);
            trackPaint.setColor(ThemeTokens.withAlpha(tokens.surfaceContainerHigh, i == 0 ? 220 : (i == 1 ? 150 : 95)));
            canvas.drawCircle(cx, cy, midR, trackPaint);
        }

        // 2. Animated Sunburst arcs
        float maxSweep = 360f * animProgress;
        for (int i = 0; i < arcs.size(); i++) {
            ArcHit hit = arcs.get(i);
            if (hit.startDeg >= maxSweep) continue;
            float drawSweep = Math.min(hit.sweepDeg, maxSweep - hit.startDeg);
            if (drawSweep <= 0.15f) continue;

            float midR = (hit.rIn + hit.rOut) * 0.5f;
            float strokeW = hit.rOut - hit.rIn;
            scratchRect.set(cx - midR, cy - midR, cx + midR, cy + midR);

            boolean isMarked = !hit.isOtherSlice && hit.node != null && markedFiles.contains(hit.node.file);
            boolean isSelected = !hit.isOtherSlice && hit.node != null && hit.node == selectedNode;

            arcPaint.setStrokeWidth(strokeW);
            if (isMarked) {
                arcPaint.setColor(ThemeTokens.withAlpha(tokens.error, hit.depth == 1 ? 235 : 195));
            } else if (isSelected) {
                arcPaint.setColor(hit.fillColor);
            } else {
                arcPaint.setColor(hit.fillColor);
            }
            canvas.drawArc(scratchRect, hit.startDeg - 90f, drawSweep, false, arcPaint);

            if (isMarked || isSelected) {
                float outerLineR = hit.rOut - dp(1.5f);
                scratchRect.set(cx - outerLineR, cy - outerLineR, cx + outerLineR, cy + outerLineR);
                markStrokePaint.setColor(isMarked ? tokens.onErrorContainer : tokens.onSurface);
                canvas.drawArc(scratchRect, hit.startDeg - 90f, drawSweep, false, markStrokePaint);
            }
        }

        // 3. Center donut hub
        canvas.drawCircle(cx, cy, hubR - dp(1.5f), hubPaint);
        canvas.drawCircle(cx, cy, hubR - dp(1.5f), hubBorderPaint);

        if (focused != null) {
            float maxTextW = hubR * 1.62f;
            String label = focused.name != null ? focused.name : "Almacenamiento";
            CharSequence ellipLabel = TextUtils.ellipsize(label, titlePaint, maxTextW, TextUtils.TruncateAt.END);
            String sizeText = TransferStats.formatBytes(focused.size);
            boolean canZoomOut = focused != root && focused.parent != null;

            if (canZoomOut) {
                canvas.drawText(ellipLabel, 0, ellipLabel.length(), cx, cy - dp(10), titlePaint);
                canvas.drawText(sizeText, cx, cy + dp(9), sizePaint);
                canvas.drawText("↑ Subir", cx, cy + dp(25), cuePaint);
            } else {
                canvas.drawText(ellipLabel, 0, ellipLabel.length(), cx, cy - dp(3), titlePaint);
                canvas.drawText(sizeText, cx, cy + dp(16), sizePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            downX = x;
            downY = y;
            longPressTriggered = false;
            pendingLongPressArc = hitTestArc(x, y);
            if (pendingLongPressArc != null && !pendingLongPressArc.isOtherSlice) {
                postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            float dx = x - downX;
            float dy = y - downY;
            if (dx * dx + dy * dy > touchSlop * touchSlop) {
                removeCallbacks(longPressRunnable);
                pendingLongPressArc = null;
            }
            return true;
        }

        if (action == MotionEvent.ACTION_CANCEL) {
            removeCallbacks(longPressRunnable);
            pendingLongPressArc = null;
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            removeCallbacks(longPressRunnable);
            if (longPressTriggered) {
                pendingLongPressArc = null;
                return true;
            }
            float dx = x - downX;
            float dy = y - downY;
            if (dx * dx + dy * dy <= touchSlop * touchSlop) {
                handleTap(x, y);
            }
            pendingLongPressArc = null;
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void handleTap(float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float dist = (float) Math.hypot(dx, dy);

        if (dist <= hubR) {
            if (focused != null && focused != root && focused.parent != null) {
                StorageAnalyzer.Node up = focused.parent;
                setFocusedNode(up);
                if (listener != null) listener.onFocusChanged(up);
            }
            return;
        }

        ArcHit hit = hitTestArc(x, y);
        if (hit == null || hit.isOtherSlice || hit.node == null) return;

        if (hit.node.isDir) {
            setFocusedNode(hit.node);
            if (listener != null) listener.onFocusChanged(hit.node);
        } else {
            selectedNode = hit.node;
            invalidate();
            if (listener != null) listener.onFileTap(hit.node);
        }
    }

    private ArcHit hitTestArc(float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float dist = (float) Math.hypot(dx, dy);
        if (dist <= hubR) return null;

        // Angle clockwise from 12 o'clock (0..360)
        float deg = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90f;
        if (deg < 0f) deg += 360f;

        for (int i = 0; i < arcs.size(); i++) {
            ArcHit hit = arcs.get(i);
            if (dist >= hit.rIn && dist <= hit.rOut
                    && deg >= hit.startDeg && deg <= hit.startDeg + hit.sweepDeg) {
                return hit;
            }
        }
        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(longPressRunnable);
        if (sweepAnimator != null) {
            sweepAnimator.cancel();
            sweepAnimator = null;
        }
    }

    private int dp(float v) {
        return Math.round(v * density);
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}
