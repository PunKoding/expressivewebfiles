package com.fotospixel.transfer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends Activity {

    private OsDesignSystem.Profile skin;
    private ThemeTokens theme;
    private int ripplePrimary;

    private final TransferStats stats = TransferStats.getInstance();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final List<View> entranceCards = new ArrayList<>();

    private int activeTabIndex = 0; // 0 = Servidor, 1 = Archivos, 2 = Almacenamiento, 3 = Ajustes
    private ScrollView mainScrollView;
    private LinearLayout serverTabView;
    private LinearLayout filesTabView;
    private LinearLayout storageTabView;
    private LinearLayout settingsTabView;
    private final OsSkinRenderer.NavTabHolder[] navTabs = new OsSkinRenderer.NavTabHolder[4];
    private OsSkinRenderer.HeaderRefs headerRefs;
    private LinearLayout floatingBar;

    // Storage tab (DaisyDisk / SquirrelDisk Sunburst analyzer)
    private final ExecutorService storageScanExecutor = Executors.newSingleThreadExecutor();
    private volatile AtomicBoolean activeStorageScanCancel;
    private volatile StorageAnalyzer.Node storageTreeRoot;
    private volatile StorageAnalyzer.Node storageFocusNode;
    private File storageSelectedVolumeRoot;
    private final Set<File> storageCollectorFiles = new LinkedHashSet<>();
    private final Map<File, StorageAnalyzer.Node> storageCollectorNodes = new LinkedHashMap<>();
    private LinearLayout storageVolumesRow;
    private TextView storageVolumeMetaText;
    private View storageVolumeBarFill;
    private View storageVolumeBarRem;
    private View storageScanStatusCard;
    private TextView storageScanStatusTitle;
    private TextView storageScanStatusDetail;
    private ProgressBar storageScanProgressBar;
    private View storageDeleteStatusCard;
    private TextView storageDeleteStatusTitle;
    private TextView storageDeleteStatusDetail;
    private ProgressBar storageDeleteProgressBar;
    private LinearLayout storageBreadcrumbsRow;
    private SunburstChartView storageSunburstView;
    private LinearLayout storageCollectorCard;
    private TextView storageCollectorTitle;
    private LinearLayout storageCollectorChipsRow;
    private TextView storageListHeaderTitle;
    private TextView storageListCountsText;
    private LinearLayout storageListContainer;

    // Advanced Explorer tab state (MiXplorer style)
    private static final String PREFS_EXPLORER = "fotospixel_explorer";
    private static final String KEY_PINNED_DIRS = "pinned_dirs";
    private File storageRoot;
    private File currentBrowseDir;
    private String fileSearchQuery = "";
    private int fileSortMode = 0; // 0 = Nombre, 1 = Fecha, 2 = Tamaño, 3 = Tipo
    private String activeCategory = null; // null, image, video, audio, doc, archive, recent
    private boolean deepSearchMode = false;
    private final List<File> fileTabs = new ArrayList<>();
    private int activeFileTab = 0;
    private final List<String> pinnedPaths = new ArrayList<>();
    private boolean selectionMode = false;
    private boolean searchExpanded = false;
    private final Set<String> selectedPaths = new LinkedHashSet<>();
    private final List<String> clipboardPaths = new ArrayList<>();
    private boolean clipboardCut = false;
    private LinearLayout fileTabsRow;
    private LinearLayout extVolumesRow;
    private HorizontalScrollView extVolumesScroll;
    private LinearLayout breadcrumbsRow;
    private HorizontalScrollView breadcrumbsScroll;
    private LinearLayout searchBoxWrap;
    private EditText searchEt;
    private LinearLayout selectionBarCard;
    private TextView selectionCountText;
    private LinearLayout clipboardBarCard;
    private TextView clipboardSummaryText;
    private View filesSearchBtn;
    private View filesSelectBtn;
    private View filesDeepSearchBtn;
    private TextView filesPathBadge;
    private TextView filesSummaryText;
    private View zipCurrentDirBtn;
    private View filesSortBtn;
    private LinearLayout filesListContainer;
    private View zipStatusCard;
    private TextView zipStatusTitle;
    private TextView zipStatusDetail;
    private ProgressBar zipStatusBar;
    private final ExecutorService zipExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService fileBgExecutor = Executors.newSingleThreadExecutor();
    private volatile AtomicBoolean activeZipCancel;
    private volatile FileOpMonitor.ActiveFileOp activeUiFileOp;
    private volatile int searchRequestToken = 0;
    private boolean mediaReceiverRegistered = false;
    private final BroadcastReceiver mediaMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            uiHandler.postDelayed(() -> {
                if (activeTabIndex == 1) {
                    refreshFilesList();
                } else if (activeTabIndex == 2) {
                    refreshStorageVolumesBar();
                }
            }, 350);
        }
    };

    private View pulseDot;
    private ObjectAnimator pulseAnimator;
    private TextView statusText;
    private View statusPill;
    private Button toggleBtn;
    private Button quickToggleServerBtn;
    private Button restartBtn;
    private TextView osDesignSummaryText;

    private View permCard;
    private View promoCard;

    private TextView upSpeedText;
    private TextView upTotalText;
    private TextView downSpeedText;
    private TextView downTotalText;
    private SpeedGraphView speedGraph;

    // Animated rolling speed state
    private long dispUpBps = 0L;
    private long targetUpBps = -1L;
    private ValueAnimator upSpeedAnim;
    private long dispDownBps = 0L;
    private long targetDownBps = -1L;
    private ValueAnimator downSpeedAnim;

    private final Map<String, long[]> txSpeedDispMap = new HashMap<>();

    private ImageView qrImageView;
    private String lastQrUrl = "";
    private Bitmap lastQrBitmap;
    private TextView webUrlText;
    private TextView ipFallbackText;

    private TextView activeCountBadge;
    private Button stopAllBtn;
    private LinearLayout activeContainer;
    private LinearLayout recentContainer;

    private TextView settingsStateText;
    private TextView storageText;
    private ProgressBar storageProgress;
    private TextView permStatusText;
    private EditText userEdit;
    private EditText passEdit;
    private TextView updateStatusText;
    private Button checkUpdateBtn;
    private Button autoUpdateToggleBtn;
    private final ExecutorService updateExecutor = Executors.newSingleThreadExecutor();

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshUi();
            uiHandler.postDelayed(this, 450);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        skin = OsDesignSystem.resolve(this);
        theme = ThemeTokens.forProfile(this, skin);
        setTheme(theme.isLight
                ? android.R.style.Theme_Material_Light_NoActionBar
                : android.R.style.Theme_Material_NoActionBar);
        super.onCreate(savedInstanceState);
        ripplePrimary = ThemeTokens.withAlpha(theme.primary, theme.isLight ? 44 : 68);
        applySystemBars();
        buildUi();
        playEntranceAnimations();
        requestNeededPermissions();
        if (savedInstanceState == null
                && TransferService.isServerDesiredRunning(this)
                && !stats.isServerRunning()) {
            startTransferService(TransferService.ACTION_START);
        }
        if (savedInstanceState == null && AppUpdater.shouldAutoCheck(this)) {
            uiHandler.postDelayed(() -> checkForUpdates(true), 2500L);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mediaReceiverRegistered) {
            IntentFilter mf = new IntentFilter();
            mf.addAction(Intent.ACTION_MEDIA_MOUNTED);
            mf.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            mf.addAction(Intent.ACTION_MEDIA_REMOVED);
            mf.addAction(Intent.ACTION_MEDIA_EJECT);
            mf.addDataScheme("file");
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(mediaMountReceiver, mf, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(mediaMountReceiver, mf);
            }
            mediaReceiverRegistered = true;
        }
        refreshUi();
        uiHandler.removeCallbacks(refreshRunnable);
        uiHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaReceiverRegistered) {
            try {
                unregisterReceiver(mediaMountReceiver);
            } catch (Exception ignored) {
            }
            mediaReceiverRegistered = false;
        }
        uiHandler.removeCallbacks(refreshRunnable);
        stopPulseAnimation();
        if (upSpeedAnim != null) upSpeedAnim.cancel();
        if (downSpeedAnim != null) downSpeedAnim.cancel();
    }

    @Override
    protected void onDestroy() {
        if (activeStorageScanCancel != null) {
            activeStorageScanCancel.set(true);
        }
        storageScanExecutor.shutdownNow();
        zipExecutor.shutdownNow();
        fileBgExecutor.shutdownNow();
        updateExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (activeTabIndex == 2) {
            if (storageFocusNode != null && storageTreeRoot != null
                    && storageFocusNode != storageTreeRoot && storageFocusNode.parent != null) {
                StorageAnalyzer.Node up = storageFocusNode.parent;
                if (storageSunburstView != null) storageSunburstView.setFocusedNode(up);
                onStorageFocusChanged(up);
                return;
            }
        }
        if (activeTabIndex == 1) {
            if (selectionMode) {
                selectionMode = false;
                selectedPaths.clear();
                refreshFilesList();
                return;
            }
            if (activeCategory != null || deepSearchMode) {
                activeCategory = null;
                deepSearchMode = false;
                refreshFilesList();
                return;
            }
            if (currentBrowseDir != null && !isAtCurrentVolumeRoot(currentBrowseDir)
                    && currentBrowseDir.getParentFile() != null) {
                navigateToDir(currentBrowseDir.getParentFile());
                return;
            }
        }
        super.onBackPressed();
    }

    private void applySystemBars() {
        Window w = getWindow();
        if (w == null) return;
        int statusColor;
        if (skin.pinnedTopToolbar) {
            statusColor = theme.isLight ? Color.parseColor("#1557B0") : Color.parseColor("#141416");
        } else {
            statusColor = theme.surface;
        }
        int navColor = (skin.navStyle == OsDesignSystem.NavStyle.FLOATING_CAPSULE)
                ? theme.surface
                : theme.surfaceContainerLow;
        w.setStatusBarColor(statusColor);
        w.setNavigationBarColor(navColor);
        w.getDecorView().setBackgroundColor(theme.surface);

        boolean lightStatus = ThemeTokens.isColorLight(statusColor);
        boolean lightNav = ThemeTokens.isColorLight(navColor);
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController ctrl = w.getInsetsController();
            if (ctrl != null) {
                ctrl.setSystemBarsAppearance(
                        lightStatus ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                ctrl.setSystemBarsAppearance(
                        lightNav ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        } else {
            int flags = w.getDecorView().getSystemUiVisibility();
            if (lightStatus) flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            else flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (lightNav) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            else flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void buildUi() {
        FrameLayout outer = new FrameLayout(this);
        outer.setBackgroundColor(theme.surface);

        LinearLayout shellColumn = new LinearLayout(this);
        shellColumn.setOrientation(LinearLayout.VERTICAL);
        outer.addView(shellColumn, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mainScrollView = new ScrollView(this);
        mainScrollView.setFillViewport(true);
        mainScrollView.setVerticalScrollBarEnabled(false);

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        final int pad = dp(skin.pageHorizontalPadDp);
        final int bottomSpace = dp(skin.navStyle == OsDesignSystem.NavStyle.FLOATING_CAPSULE ? 112 : 86);
        root.setPadding(pad, pad, pad, bottomSpace);
        mainScrollView.addView(root);

        headerRefs = OsSkinRenderer.buildHeader(
                this,
                root,
                skin,
                theme,
                () -> {
                    if (stats.isServerRunning()) stopServerFromUi();
                    else startServerFromUi();
                },
                null);
        statusPill = headerRefs.statusPill;
        pulseDot = headerRefs.pulseDot;
        statusText = headerRefs.statusText;
        attachSpringTouch(statusPill);

        if (headerRefs.pinnedToolbar != null) {
            shellColumn.addView(headerRefs.pinnedToolbar, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        shellColumn.addView(mainScrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Tab 1: Servidor
        serverTabView = new LinearLayout(this);
        serverTabView.setOrientation(LinearLayout.VERTICAL);
        buildServerTab(serverTabView);
        root.addView(serverTabView);

        // Tab 2: Archivos (Explorador avanzado + ZIP)
        filesTabView = new LinearLayout(this);
        filesTabView.setOrientation(LinearLayout.VERTICAL);
        filesTabView.setVisibility(View.GONE);
        buildFilesTab(filesTabView);
        root.addView(filesTabView);

        // Tab 3: Almacenamiento (DaisyDisk / SquirrelDisk Sunburst)
        storageTabView = new LinearLayout(this);
        storageTabView.setOrientation(LinearLayout.VERTICAL);
        storageTabView.setVisibility(View.GONE);
        buildStorageTab(storageTabView);
        root.addView(storageTabView);

        // Tab 4: Ajustes
        settingsTabView = new LinearLayout(this);
        settingsTabView.setOrientation(LinearLayout.VERTICAL);
        settingsTabView.setVisibility(View.GONE);
        buildSettingsTab(settingsTabView);
        root.addView(settingsTabView);

        buildBottomNavBar(outer, root, pad);

        setContentView(outer);
    }

    private void buildBottomNavBar(final FrameLayout outer, final LinearLayout root, final int pad) {
        floatingBar = new LinearLayout(this);
        floatingBar.setOrientation(LinearLayout.HORIZONTAL);
        floatingBar.setGravity(Gravity.CENTER_VERTICAL);

        final boolean isFloating = (skin.navStyle == OsDesignSystem.NavStyle.FLOATING_CAPSULE);
        int screenW = getResources().getDisplayMetrics().widthPixels;
        boolean stackedNav = isStackedNav();

        final String[] labels = new String[]{"Servidor", "Archivos", "Almacenamiento", "Ajustes"};
        final int[] icons = new int[]{
                R.drawable.ic_m3_dns, R.drawable.ic_m3_folder, R.drawable.ic_m3_storage, R.drawable.ic_m3_tune
        };

        if (isFloating) {
            int strokeColor = ThemeTokens.withAlpha(theme.outlineVariant, skin.cardStrokeAlpha > 0 ? skin.cardStrokeAlpha : 0x55);
            floatingBar.setBackground(roundedRect(
                    theme.surfaceContainerHighest,
                    strokeColor,
                    dp(skin.id == OsDesignSystem.SkinId.NOTHING_OS ? 28 : 34)));
            int barPad = stackedNav ? dp(5) : dp(6);
            floatingBar.setPadding(barPad, barPad, barPad, barPad);
            floatingBar.setElevation(skin.id == OsDesignSystem.SkinId.NOTHING_OS ? 0f : dp(12));

            int barBtnW = Math.max(dp(70), (screenW - dp(isFloating && !stackedNav ? 64 : 40)) / 4);
            if (!stackedNav) {
                barBtnW = Math.min(dp(140), barBtnW);
            }
            for (int i = 0; i < 4; i++) {
                final int tabIdx = i;
                navTabs[i] = OsSkinRenderer.buildNavTab(
                        this, labels[i], icons[i], skin, theme, ripplePrimary,
                        barBtnW, true, v -> switchTab(tabIdx));
                OsSkinRenderer.styleNavTab(this, navTabs[i], i == activeTabIndex, skin, theme, ripplePrimary);
                floatingBar.addView(navTabs[i].root);
            }

            FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            barLp.bottomMargin = dp(18);
            outer.addView(floatingBar, barLp);
        } else {
            int dockedBg = (skin.navStyle == OsDesignSystem.NavStyle.DOCKED_M3_PILL)
                    ? theme.surfaceContainer
                    : theme.surfaceContainerLow;
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(dockedBg);
            floatingBar.setBackground(bg);
            floatingBar.setPadding(dp(4), dp(4), dp(4), dp(4));
            floatingBar.setElevation(skin.id == OsDesignSystem.SkinId.MATERIAL_ANDROID_9 ? dp(8) : dp(2));

            LinearLayout dockedWrapper = new LinearLayout(this);
            dockedWrapper.setOrientation(LinearLayout.VERTICAL);
            if (skin.navStyle == OsDesignSystem.NavStyle.DOCKED_CLASSIC) {
                View topDivider = new View(this);
                topDivider.setBackgroundColor(ThemeTokens.withAlpha(theme.outlineVariant, 0x90));
                dockedWrapper.addView(topDivider, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, dp(1))));
            }
            dockedWrapper.addView(floatingBar, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int i = 0; i < 4; i++) {
                final int tabIdx = i;
                navTabs[i] = OsSkinRenderer.buildNavTab(
                        this, labels[i], icons[i], skin, theme, ripplePrimary,
                        0, false, v -> switchTab(tabIdx));
                OsSkinRenderer.styleNavTab(this, navTabs[i], i == activeTabIndex, skin, theme, ripplePrimary);
                floatingBar.addView(navTabs[i].root);
            }

            FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);
            outer.addView(dockedWrapper, barLp);
        }

        outer.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            if (headerRefs.pinnedToolbar != null) {
                headerRefs.pinnedToolbar.setPadding(dp(16), top + dp(10), dp(14), dp(10));
                root.setPadding(pad, dp(10), pad, bottom + dp(84));
            } else {
                root.setPadding(pad, top + dp(14), pad, bottom + dp(isFloating ? 108 : 84));
            }
            if (isFloating) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) floatingBar.getLayoutParams();
                lp.bottomMargin = bottom + dp(16);
                floatingBar.setLayoutParams(lp);
            } else {
                floatingBar.setPadding(dp(4), dp(4), dp(4), bottom + dp(4));
            }
            return insets;
        });
    }

    private void stopServerFromUi() {
        TransferService.stopServiceCompletely(this);
        Toast.makeText(this, "Servidor detenido", Toast.LENGTH_SHORT).show();
        uiHandler.post(this::refreshUi);
        uiHandler.postDelayed(this::refreshUi, 260);
    }

    private void startServerFromUi() {
        TransferService.setServerDesiredRunning(this, true);
        startTransferService(TransferService.ACTION_START);
        Toast.makeText(this, "Iniciando servidor...", Toast.LENGTH_SHORT).show();
        uiHandler.postDelayed(this::refreshUi, 260);
    }

    private void buildServerTab(LinearLayout parent) {
        // Permissions Alert Cards (only visible when missing)
        LinearLayout pCard = createExpressiveCard(parent, theme.surfaceContainerHigh, 28);
        permCard = pCard;
        TextView pTitle = new TextView(this);
        pTitle.setText("Permiso de almacenamiento requerido");
        pTitle.setTextColor(theme.error);
        pTitle.setTypeface(Typeface.DEFAULT_BOLD);
        pTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        pCard.addView(pTitle);

        TextView pDesc = new TextView(this);
        pDesc.setText("Concede acceso completo para guardar fotos directamente en DCIM y Pictures.");
        pDesc.setTextColor(theme.onSurfaceVariant);
        pDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        pDesc.setPadding(0, dp(4), 0, dp(12));
        pCard.addView(pDesc);

        Button pBtn = createIconPillButton("Conceder permiso", R.drawable.ic_m3_folder, theme.errorContainer, theme.onErrorContainer);
        pBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)));
        pBtn.setOnClickListener(v -> openAllFilesPermission());
        pCard.addView(pBtn);

        LinearLayout prCard = createExpressiveCard(parent, theme.surfaceContainerHigh, 28);
        promoCard = prCard;
        TextView prTitle = new TextView(this);
        prTitle.setText("Live Updates de Android 16/17");
        prTitle.setTextColor(theme.primary);
        prTitle.setTypeface(Typeface.DEFAULT_BOLD);
        prTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        prCard.addView(prTitle);

        TextView prDesc = new TextView(this);
        prDesc.setText("Activa las notificaciones promocionadas para seguir cada transferencia desde el panel y lockscreen.");
        prDesc.setTextColor(theme.onSurfaceVariant);
        prDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        prDesc.setPadding(0, dp(4), 0, dp(12));
        prCard.addView(prDesc);

        Button prBtn = createIconPillButton("Activar Live Updates", R.drawable.ic_m3_dns, theme.primaryContainer, theme.onPrimaryContainer);
        prBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)));
        prBtn.setOnClickListener(v -> openPromotedNotificationsSettings());
        prCard.addView(prBtn);

        // Hero Velocity & Waveform Card
        LinearLayout speedCard = createExpressiveCard(parent, theme.surfaceContainer, 32);
        OsSkinRenderer.addSectionHeader(this, parent, speedCard, "Velocidad en vivo", R.drawable.ic_m3_dns, theme.primary, skin, theme, true);
        OsSkinRenderer.applyHeroCardDecoration(this, speedCard, skin, theme, true);

        LinearLayout speedRow = new LinearLayout(this);
        speedRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout upBox = createSpeedTile(
                "Subida", R.drawable.ic_m3_upload,
                theme.primaryContainer, theme.onPrimaryContainer);
        LinearLayout.LayoutParams upLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        upLp.rightMargin = dp(6);
        upBox.setLayoutParams(upLp);
        upSpeedText = (TextView) upBox.getChildAt(1);
        upTotalText = (TextView) upBox.getChildAt(2);
        speedRow.addView(upBox);

        LinearLayout downBox = createSpeedTile(
                "Descarga", R.drawable.ic_m3_download,
                theme.tertiaryContainer, theme.onTertiaryContainer);
        LinearLayout.LayoutParams downLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        downLp.leftMargin = dp(6);
        downBox.setLayoutParams(downLp);
        downSpeedText = (TextView) downBox.getChildAt(1);
        downTotalText = (TextView) downBox.getChildAt(2);
        speedRow.addView(downBox);
        speedCard.addView(speedRow);

        speedGraph = new SpeedGraphView(this);
        speedGraph.applyDesignProfile(skin, theme);
        speedGraph.setBackground(roundedRect(theme.surfaceContainerLowest,
                skin.cardStrokeAlpha > 0 ? ThemeTokens.withAlpha(theme.outlineVariant, skin.cardStrokeAlpha) : 0,
                dp(skin.innerRadiusDp)));
        LinearLayout.LayoutParams sgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(96));
        sgLp.topMargin = dp(12);
        speedGraph.setLayoutParams(sgLp);
        speedCard.addView(speedGraph);

        // Quick Share / Portal Access & QR Card
        LinearLayout qrCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, qrCard, "Acceso desde navegador", R.drawable.ic_m3_qr, theme.secondary, skin, theme, true);

        LinearLayout qrRow = new LinearLayout(this);
        qrRow.setOrientation(LinearLayout.HORIZONTAL);
        qrRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout qrFrame = new LinearLayout(this);
        qrFrame.setBackground(rippleBg(Color.WHITE, Color.argb(40, 0, 0, 0),
                theme.isLight ? theme.outlineVariant : 0, dp(skin.innerRadiusDp)));
        qrFrame.setPadding(dp(8), dp(8), dp(8), dp(8));
        qrFrame.setGravity(Gravity.CENTER);
        attachSpringTouch(qrFrame);
        qrFrame.setOnClickListener(v -> showEnlargedQrDialog());

        qrImageView = new ImageView(this);
        qrImageView.setContentDescription("Código QR del portal web");
        qrImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        qrImageView.setAdjustViewBounds(true);
        qrImageView.setLayoutParams(new LinearLayout.LayoutParams(dp(106), dp(106)));
        qrFrame.addView(qrImageView);
        qrRow.addView(qrFrame);

        LinearLayout qrMeta = new LinearLayout(this);
        qrMeta.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams qmLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        qmLp.leftMargin = dp(16);
        qrMeta.setLayoutParams(qmLp);

        TextView qrTitle = new TextView(this);
        qrTitle.setText("Conecta otro dispositivo");
        qrTitle.setTextColor(theme.onSurface);
        qrTitle.setTypeface(Typeface.DEFAULT_BOLD);
        qrTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        qrMeta.addView(qrTitle);

        webUrlText = new TextView(this);
        webUrlText.setText("http://...");
        webUrlText.setTextColor(theme.primary);
        webUrlText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        webUrlText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        webUrlText.setSingleLine(false);
        webUrlText.setMaxLines(2);
        webUrlText.setPadding(dp(12), dp(8), dp(12), dp(8));
        webUrlText.setBackground(roundedRect(theme.surfaceContainerHigh, 0, dp(14)));
        LinearLayout.LayoutParams urlLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        urlLp.topMargin = dp(6);
        urlLp.bottomMargin = dp(6);
        webUrlText.setLayoutParams(urlLp);
        qrMeta.addView(webUrlText);

        ipFallbackText = new TextView(this);
        ipFallbackText.setText("IP alternativa: ...");
        ipFallbackText.setTextColor(theme.onSurfaceVariant);
        ipFallbackText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        ipFallbackText.setPadding(dp(2), 0, dp(2), dp(6));
        ipFallbackText.setOnClickListener(v -> {
            Object tag = v.getTag();
            if (tag != null) {
                copyToClipboard("IP alternativa", tag.toString());
            }
        });
        qrMeta.addView(ipFallbackText);

        TextView qrHint = new TextView(this);
        qrHint.setText("Toca el código para mostrarlo en pantalla completa.");
        qrHint.setTextColor(theme.onSurfaceVariant);
        qrHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        qrMeta.addView(qrHint);

        qrRow.addView(qrMeta);
        qrCard.addView(qrRow);

        LinearLayout qrActions = new LinearLayout(this);
        qrActions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams qraLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        qraLp.topMargin = dp(14);
        qrActions.setLayoutParams(qraLp);

        Button copyWebBtn = createIconPillButton("Copiar URL", R.drawable.ic_m3_copy, theme.primary, theme.onPrimary);
        LinearLayout.LayoutParams cwLp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        cwLp.rightMargin = dp(6);
        copyWebBtn.setLayoutParams(cwLp);
        copyWebBtn.setOnClickListener(v -> copyToClipboard("URL Web", webUrlText.getText()));
        qrActions.addView(copyWebBtn);

        Button enlargeQrBtn = createIconPillButton("Ampliar QR", R.drawable.ic_m3_qr, theme.secondaryContainer, theme.onSecondaryContainer);
        LinearLayout.LayoutParams eqLp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        eqLp.leftMargin = dp(6);
        enlargeQrBtn.setLayoutParams(eqLp);
        enlargeQrBtn.setOnClickListener(v -> showEnlargedQrDialog());
        qrActions.addView(enlargeQrBtn);
        qrCard.addView(qrActions);

        quickToggleServerBtn = createIconPillButton("Detener servidor", R.drawable.ic_m3_power, theme.errorContainer, theme.onErrorContainer);
        LinearLayout.LayoutParams qtLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        qtLp.topMargin = dp(10);
        quickToggleServerBtn.setLayoutParams(qtLp);
        quickToggleServerBtn.setOnClickListener(v -> {
            if (stats.isServerRunning()) stopServerFromUi();
            else startServerFromUi();
        });
        qrCard.addView(quickToggleServerBtn);
        OsSkinRenderer.applyHeroCardDecoration(this, qrCard, skin, theme, false);

        // Active Transfers Card
        LinearLayout activeCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        LinearLayout activeHead = new LinearLayout(this);
        activeHead.setOrientation(LinearLayout.HORIZONTAL);
        activeHead.setGravity(Gravity.CENTER_VERTICAL);
        activeHead.setPadding(0, 0, 0, dp(12));

        LinearLayout aTitleWrap = createSectionHeader("Transferencias activas", R.drawable.ic_m3_upload, theme.primary);
        aTitleWrap.setPadding(0, 0, 0, 0);
        aTitleWrap.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        activeHead.addView(aTitleWrap);

        stopAllBtn = createIconPillButton("Detener todo", R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer);
        LinearLayout.LayoutParams sabLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
        sabLp.rightMargin = dp(8);
        stopAllBtn.setLayoutParams(sabLp);
        stopAllBtn.setPadding(dp(12), 0, dp(12), 0);
        stopAllBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        stopAllBtn.setVisibility(View.GONE);
        stopAllBtn.setOnClickListener(v -> {
            int n = stats.cancelAllTransfers();
            Toast.makeText(this, "Detenidas " + n + " transferencia(s)", Toast.LENGTH_SHORT).show();
            uiHandler.post(this::refreshUi);
        });
        activeHead.addView(stopAllBtn);

        activeCountBadge = new TextView(this);
        activeCountBadge.setTextColor(theme.onSecondaryContainer);
        activeCountBadge.setBackground(roundedRect(theme.secondaryContainer, 0, dp(20)));
        activeCountBadge.setPadding(dp(12), dp(5), dp(12), dp(5));
        activeCountBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        activeCountBadge.setTypeface(Typeface.DEFAULT_BOLD);
        activeHead.addView(activeCountBadge);
        activeCard.addView(activeHead);

        activeContainer = new LinearLayout(this);
        activeContainer.setOrientation(LinearLayout.VERTICAL);
        activeCard.addView(activeContainer);

        // Recent Transfers Card
        LinearLayout recentCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, recentCard, "Historial reciente", R.drawable.ic_m3_storage, theme.tertiary, skin, theme, true);
        recentContainer = new LinearLayout(this);
        recentContainer.setOrientation(LinearLayout.VERTICAL);
        recentCard.addView(recentContainer);
    }

    private void buildStorageTab(LinearLayout parent) {
        final boolean compact = isCompactScreen();

        // 1. Header & Volume Selector Card
        LinearLayout topCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        LinearLayout topHeaderRow = new LinearLayout(this);
        topHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        topHeaderRow.setGravity(Gravity.CENTER_VERTICAL);
        topHeaderRow.setPadding(0, 0, 0, dp(8));

        LinearLayout titleWrap = new LinearLayout(this);
        titleWrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams twLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleWrap.setLayoutParams(twLp);

        TextView headTv = new TextView(this);
        headTv.setText("Almacenamiento");
        headTv.setTextColor(theme.onSurface);
        headTv.setTypeface(Typeface.DEFAULT_BOLD);
        headTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleWrap.addView(headTv);

        storageVolumeMetaText = new TextView(this);
        storageVolumeMetaText.setText("Analiza qué carpetas ocupan más espacio");
        storageVolumeMetaText.setTextColor(theme.onSurfaceVariant);
        storageVolumeMetaText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        storageVolumeMetaText.setPadding(0, dp(2), 0, 0);
        titleWrap.addView(storageVolumeMetaText);
        topHeaderRow.addView(titleWrap);

        FrameLayout openFilesCircle = createRoundIconButton(
                R.drawable.ic_m3_folder, theme.secondaryContainer, theme.onSecondaryContainer, 38, "Abrir en Archivos");
        LinearLayout.LayoutParams ofLp = new LinearLayout.LayoutParams(dp(38), dp(38));
        ofLp.rightMargin = dp(6);
        openFilesCircle.setLayoutParams(ofLp);
        openFilesCircle.setOnClickListener(v -> openFocusedInFilesTab());
        topHeaderRow.addView(openFilesCircle);

        if (compact) {
            FrameLayout rescanCircle = createRoundIconButton(
                    R.drawable.ic_m3_refresh, theme.primaryContainer, theme.onPrimaryContainer, 38, "Analizar");
            rescanCircle.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
            rescanCircle.setOnClickListener(v -> startStorageScan(
                    storageSelectedVolumeRoot != null ? storageSelectedVolumeRoot : storageRoot));
            topHeaderRow.addView(rescanCircle);
        } else {
            Button rescanPill = createIconPillButton(
                    "Analizar", R.drawable.ic_m3_refresh, theme.primaryContainer, theme.onPrimaryContainer);
            rescanPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            rescanPill.setPadding(dp(12), dp(6), dp(14), dp(6));
            rescanPill.setOnClickListener(v -> startStorageScan(
                    storageSelectedVolumeRoot != null ? storageSelectedVolumeRoot : storageRoot));
            topHeaderRow.addView(rescanPill);
        }
        topCard.addView(topHeaderRow);

        HorizontalScrollView volScroll = new HorizontalScrollView(this);
        volScroll.setHorizontalScrollBarEnabled(false);
        storageVolumesRow = new LinearLayout(this);
        storageVolumesRow.setOrientation(LinearLayout.HORIZONTAL);
        storageVolumesRow.setPadding(0, dp(2), 0, dp(8));
        volScroll.addView(storageVolumesRow);
        topCard.addView(volScroll);

        LinearLayout volBarTrack = new LinearLayout(this);
        volBarTrack.setOrientation(LinearLayout.HORIZONTAL);
        volBarTrack.setWeightSum(100f);
        volBarTrack.setBackground(roundedRect(theme.surfaceContainerHighest, 0, dp(999)));
        LinearLayout.LayoutParams vbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        volBarTrack.setLayoutParams(vbLp);

        storageVolumeBarFill = new View(this);
        storageVolumeBarFill.setBackground(roundedRect(theme.primary, 0, dp(999)));
        storageVolumeBarFill.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        volBarTrack.addView(storageVolumeBarFill);

        storageVolumeBarRem = new View(this);
        storageVolumeBarRem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 99f));
        volBarTrack.addView(storageVolumeBarRem);
        topCard.addView(volBarTrack);

        // 2. Live Scan Status Card (hidden until scanning)
        LinearLayout scanCard = createExpressiveCard(parent, theme.primaryContainer, 24);
        storageScanStatusCard = scanCard;
        scanCard.setVisibility(View.GONE);
        LinearLayout scanTop = new LinearLayout(this);
        scanTop.setOrientation(LinearLayout.HORIZONTAL);
        scanTop.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout scanTxtCol = new LinearLayout(this);
        scanTxtCol.setOrientation(LinearLayout.VERTICAL);
        scanTxtCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        storageScanStatusTitle = new TextView(this);
        storageScanStatusTitle.setTextColor(theme.onPrimaryContainer);
        storageScanStatusTitle.setTypeface(Typeface.DEFAULT_BOLD);
        storageScanStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        scanTxtCol.addView(storageScanStatusTitle);

        storageScanStatusDetail = new TextView(this);
        storageScanStatusDetail.setTextColor(ThemeTokens.withAlpha(theme.onPrimaryContainer, 220));
        storageScanStatusDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        storageScanStatusDetail.setSingleLine(true);
        storageScanStatusDetail.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        scanTxtCol.addView(storageScanStatusDetail);
        scanTop.addView(scanTxtCol);

        FrameLayout stopScanBtn = createRoundIconButton(
                R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer, 34, "Cancelar escaneo");
        stopScanBtn.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
        stopScanBtn.setOnClickListener(v -> {
            if (activeStorageScanCancel != null) activeStorageScanCancel.set(true);
        });
        scanTop.addView(stopScanBtn);
        scanCard.addView(scanTop);

        storageScanProgressBar = createExpressiveProgressBar(
                theme.onPrimaryContainer, ThemeTokens.withAlpha(theme.onPrimaryContainer, 55), 999);
        LinearLayout.LayoutParams spbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6));
        spbLp.topMargin = dp(8);
        storageScanProgressBar.setLayoutParams(spbLp);
        storageScanProgressBar.setIndeterminate(true);
        scanCard.addView(storageScanProgressBar);

        // 3. Live Delete Progress Card (mirrors 2nd Live Activity when deleting inside Almacenamiento)
        LinearLayout delCard = createExpressiveCard(parent, theme.errorContainer, 24);
        storageDeleteStatusCard = delCard;
        delCard.setVisibility(View.GONE);
        storageDeleteStatusTitle = new TextView(this);
        storageDeleteStatusTitle.setTextColor(theme.onErrorContainer);
        storageDeleteStatusTitle.setTypeface(Typeface.DEFAULT_BOLD);
        storageDeleteStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        delCard.addView(storageDeleteStatusTitle);

        storageDeleteStatusDetail = new TextView(this);
        storageDeleteStatusDetail.setTextColor(ThemeTokens.withAlpha(theme.onErrorContainer, 220));
        storageDeleteStatusDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        storageDeleteStatusDetail.setSingleLine(true);
        storageDeleteStatusDetail.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        storageDeleteStatusDetail.setPadding(0, dp(2), 0, dp(8));
        delCard.addView(storageDeleteStatusDetail);

        storageDeleteProgressBar = createExpressiveProgressBar(
                theme.onErrorContainer, ThemeTokens.withAlpha(theme.onErrorContainer, 60), 999);
        storageDeleteProgressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(6)));
        delCard.addView(storageDeleteProgressBar);

        // 4. SquirrelDisk Collector ("Liberar espacio")
        storageCollectorCard = createExpressiveCard(parent, theme.errorContainer, 24);
        storageCollectorCard.setVisibility(View.GONE);
        storageCollectorTitle = new TextView(this);
        storageCollectorTitle.setTextColor(theme.onErrorContainer);
        storageCollectorTitle.setTypeface(Typeface.DEFAULT_BOLD);
        storageCollectorTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        storageCollectorCard.addView(storageCollectorTitle);

        TextView colSub = new TextView(this);
        colSub.setText("Elementos marcados para borrar en bloque (toca un chip para quitar):");
        colSub.setTextColor(ThemeTokens.withAlpha(theme.onErrorContainer, 210));
        colSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        colSub.setPadding(0, dp(2), 0, dp(8));
        storageCollectorCard.addView(colSub);

        HorizontalScrollView colChipsScroll = new HorizontalScrollView(this);
        colChipsScroll.setHorizontalScrollBarEnabled(false);
        storageCollectorChipsRow = new LinearLayout(this);
        storageCollectorChipsRow.setOrientation(LinearLayout.HORIZONTAL);
        colChipsScroll.addView(storageCollectorChipsRow);
        storageCollectorCard.addView(colChipsScroll);

        LinearLayout colBtns = new LinearLayout(this);
        colBtns.setOrientation(LinearLayout.HORIZONTAL);
        colBtns.setPadding(0, dp(10), 0, 0);

        Button clearColBtn = createIconPillButton(
                "Vaciar", 0, ThemeTokens.withAlpha(theme.onErrorContainer, 28), theme.onErrorContainer);
        clearColBtn.setCompoundDrawablesRelative(null, null, null, null);
        clearColBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        LinearLayout.LayoutParams ccbLp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        ccbLp.rightMargin = dp(6);
        clearColBtn.setLayoutParams(ccbLp);
        clearColBtn.setOnClickListener(v -> clearStorageCollector());
        colBtns.addView(clearColBtn);

        Button purgeColBtn = createIconPillButton(
                "Liberar espacio", R.drawable.ic_m3_stop, theme.error, theme.onError);
        purgeColBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        LinearLayout.LayoutParams pcbLp = new LinearLayout.LayoutParams(0, dp(40), 1.5f);
        pcbLp.leftMargin = dp(6);
        purgeColBtn.setLayoutParams(pcbLp);
        purgeColBtn.setOnClickListener(v -> confirmPurgeStorageCollector());
        colBtns.addView(purgeColBtn);
        storageCollectorCard.addView(colBtns);

        // 5. DaisyDisk / SquirrelDisk Sunburst Card
        LinearLayout chartCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        HorizontalScrollView crumbScroll = new HorizontalScrollView(this);
        crumbScroll.setHorizontalScrollBarEnabled(false);
        storageBreadcrumbsRow = new LinearLayout(this);
        storageBreadcrumbsRow.setOrientation(LinearLayout.HORIZONTAL);
        storageBreadcrumbsRow.setPadding(0, 0, 0, dp(8));
        crumbScroll.addView(storageBreadcrumbsRow);
        chartCard.addView(crumbScroll);

        storageSunburstView = new SunburstChartView(this);
        storageSunburstView.applyDesignProfile(skin, theme);
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sbLp.gravity = Gravity.CENTER_HORIZONTAL;
        storageSunburstView.setLayoutParams(sbLp);
        storageSunburstView.setOnNodeActionListener(new SunburstChartView.OnNodeActionListener() {
            @Override
            public void onFocusChanged(StorageAnalyzer.Node newFocus) {
                onStorageFocusChanged(newFocus);
            }

            @Override
            public void onFileTap(StorageAnalyzer.Node fileNode) {
                showStorageNodeOptionsDialog(fileNode);
            }

            @Override
            public void onNodeLongPress(StorageAnalyzer.Node node) {
                toggleStorageCollectorNode(node);
            }
        });
        chartCard.addView(storageSunburstView);

        TextView chartHint = new TextView(this);
        chartHint.setText("Toca un sector para profundizar · Centro para subir · Mantén pulsado para marcar");
        chartHint.setTextColor(theme.onSurfaceVariant);
        chartHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        chartHint.setGravity(Gravity.CENTER);
        chartHint.setPadding(0, dp(8), 0, 0);
        chartCard.addView(chartHint);

        // 6. Ranked Breakdown Card
        LinearLayout listCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        LinearLayout listTop = new LinearLayout(this);
        listTop.setOrientation(LinearLayout.HORIZONTAL);
        listTop.setGravity(Gravity.CENTER_VERTICAL);
        listTop.setPadding(0, 0, 0, dp(10));

        storageListHeaderTitle = new TextView(this);
        storageListHeaderTitle.setText("Carpetas por tamaño");
        storageListHeaderTitle.setTextColor(theme.onSurface);
        storageListHeaderTitle.setTypeface(Typeface.DEFAULT_BOLD);
        storageListHeaderTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        storageListHeaderTitle.setSingleLine(true);
        storageListHeaderTitle.setEllipsize(TextUtils.TruncateAt.END);
        storageListHeaderTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        listTop.addView(storageListHeaderTitle);

        storageListCountsText = new TextView(this);
        storageListCountsText.setTextColor(theme.onPrimaryContainer);
        storageListCountsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        storageListCountsText.setTypeface(Typeface.DEFAULT_BOLD);
        storageListCountsText.setBackground(roundedRect(theme.primaryContainer, 0, dp(999)));
        storageListCountsText.setPadding(dp(10), dp(4), dp(10), dp(4));
        listTop.addView(storageListCountsText);
        listCard.addView(listTop);

        storageListContainer = new LinearLayout(this);
        storageListContainer.setOrientation(LinearLayout.VERTICAL);
        listCard.addView(storageListContainer);
    }

    private void refreshStorageVolumesBar() {
        if (storageVolumesRow == null) return;
        storageVolumesRow.removeAllViews();
        List<StorageVolumes.VolumeInfo> vols = StorageVolumes.listMounted(this);
        if (storageSelectedVolumeRoot == null || !storageSelectedVolumeRoot.exists()) {
            storageSelectedVolumeRoot = !vols.isEmpty() ? vols.get(0).rootDir : storageRoot;
        }
        StorageVolumes.VolumeInfo activeVol = null;
        for (int i = 0; i < vols.size(); i++) {
            final StorageVolumes.VolumeInfo v = vols.get(i);
            boolean sel = storageSelectedVolumeRoot != null
                    && v.rootDir.getAbsolutePath().equals(storageSelectedVolumeRoot.getAbsolutePath());
            if (sel) activeVol = v;

            TextView chip = new TextView(this);
            chip.setText(v.label + " (" + TransferStats.formatBytes(v.freeBytes) + " libres)");
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setTextColor(sel ? theme.onSecondaryContainer : theme.onSurfaceVariant);
            chip.setBackground(rippleBg(
                    sel ? theme.secondaryContainer : theme.surfaceContainerHigh,
                    ripplePrimary, 0, dp(skin.chipRadiusDp)));
            chip.setPadding(dp(12), dp(7), dp(12), dp(7));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(6);
            chip.setLayoutParams(lp);
            attachSpringTouch(chip);
            chip.setOnClickListener(view -> {
                storageSelectedVolumeRoot = v.rootDir;
                startStorageScan(v.rootDir);
            });
            storageVolumesRow.addView(chip);
        }

        if (activeVol == null && storageSelectedVolumeRoot != null) {
            activeVol = StorageVolumes.findForFile(this, storageSelectedVolumeRoot);
        }
        long total = activeVol != null ? activeVol.totalBytes : 0L;
        long free = activeVol != null ? activeVol.freeBytes : 0L;
        if (total <= 0L && storageSelectedVolumeRoot != null) {
            try {
                StatFs sf = new StatFs(storageSelectedVolumeRoot.getAbsolutePath());
                total = sf.getTotalBytes();
                free = sf.getAvailableBytes();
            } catch (Exception ignored) {
            }
        }
        long used = Math.max(0L, total - free);
        int pct = total > 0L ? (int) Math.min(100L, Math.round((used * 100.0) / total)) : 0;
        String volTitle = activeVol != null ? activeVol.label : "Almacenamiento interno";
        storageVolumeMetaText.setText(volTitle + ": " + TransferStats.formatBytes(used)
                + " usados · " + TransferStats.formatBytes(free) + " libres (" + pct + "%)");

        float usedW = Math.max(1f, pct);
        float remW = Math.max(1f, 100f - pct);
        storageVolumeBarFill.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, usedW));
        storageVolumeBarRem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, remW));
    }

    private void startStorageScan(final File targetRoot) {
        final File rootDir = (targetRoot != null && targetRoot.exists()) ? targetRoot : storageRoot;
        if (rootDir == null || !rootDir.exists()) return;
        storageSelectedVolumeRoot = rootDir;
        refreshStorageVolumesBar();

        if (activeStorageScanCancel != null) {
            activeStorageScanCancel.set(true);
        }
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        activeStorageScanCancel = cancelFlag;

        storageScanStatusCard.setVisibility(View.VISIBLE);
        storageScanStatusTitle.setText("Analizando " + resolveActiveVolumeLabel(rootDir) + "...");
        storageScanStatusDetail.setText("Recorriendo carpetas y calculando tamaños...");

        storageScanExecutor.execute(() -> {
            StorageAnalyzer.Node scanned = StorageAnalyzer.scanTree(
                    rootDir,
                    cancelFlag,
                    (scannedBytes, files, dirs, currentPath) -> uiHandler.post(() -> {
                        if (cancelFlag.get()) return;
                        storageScanStatusDetail.setText(
                                TransferStats.formatBytes(scannedBytes) + " · "
                                + dirs + " carpetas · " + files + " archivos");
                    }));

            uiHandler.post(() -> {
                storageScanStatusCard.setVisibility(View.GONE);
                if (cancelFlag.get()) {
                    Toast.makeText(this, "Escaneo detenido", Toast.LENGTH_SHORT).show();
                    return;
                }
                storageTreeRoot = scanned;
                storageFocusNode = scanned;
                pruneMissingCollectorNodes();
                if (storageSunburstView != null) {
                    storageSunburstView.setMarkedFiles(storageCollectorFiles);
                    storageSunburstView.setTree(scanned, scanned);
                }
                renderStorageAnalysisUi();
            });
        });
    }

    private void onStorageFocusChanged(StorageAnalyzer.Node newFocus) {
        if (newFocus == null) return;
        storageFocusNode = newFocus;
        renderStorageAnalysisUi();
    }

    private void renderStorageAnalysisUi() {
        if (storageFocusNode == null || storageListContainer == null || storageBreadcrumbsRow == null) return;

        // 1. Breadcrumbs
        storageBreadcrumbsRow.removeAllViews();
        List<StorageAnalyzer.Node> trail = new ArrayList<>();
        StorageAnalyzer.Node cur = storageFocusNode;
        while (cur != null) {
            trail.add(0, cur);
            cur = cur.parent;
        }
        for (int i = 0; i < trail.size(); i++) {
            final StorageAnalyzer.Node node = trail.get(i);
            final boolean isLast = (i == trail.size() - 1);
            TextView crumb = new TextView(this);
            crumb.setText(i == 0 ? "Raíz" : node.name);
            crumb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            crumb.setTypeface(Typeface.DEFAULT_BOLD);
            crumb.setTextColor(isLast ? theme.onPrimaryContainer : theme.onSurface);
            crumb.setBackground(rippleBg(
                    isLast ? theme.primaryContainer : theme.surfaceContainerHigh,
                    ripplePrimary, 0, dp(skin.chipRadiusDp)));
            crumb.setPadding(dp(11), dp(6), dp(11), dp(6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(5);
            crumb.setLayoutParams(lp);
            attachSpringTouch(crumb);
            crumb.setOnClickListener(v -> {
                if (storageSunburstView != null) storageSunburstView.setFocusedNode(node);
                onStorageFocusChanged(node);
            });
            storageBreadcrumbsRow.addView(crumb);
        }

        // 2. Counts & Header
        storageListHeaderTitle.setText(
                storageFocusNode == storageTreeRoot ? "Carpetas más pesadas" : storageFocusNode.name);
        storageListCountsText.setText(
                storageFocusNode.dirCount + " carp · " + storageFocusNode.fileCount + " arch");

        // 3. Breakdown rows
        storageListContainer.removeAllViews();
        List<StorageAnalyzer.Node> children = storageFocusNode.children;
        if (children.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Carpeta vacía");
            empty.setTextColor(theme.onSurfaceVariant);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(16), 0, dp(16));
            storageListContainer.addView(empty);
            return;
        }

        long parentTotal = Math.max(1L, storageFocusNode.size);
        int limit = Math.min(children.size(), 75);
        for (int i = 0; i < limit; i++) {
            final StorageAnalyzer.Node child = children.get(i);
            float pct = (child.size * 100f) / parentTotal;
            int swatch = storageSunburstView != null
                    ? storageSunburstView.getPaletteColor(child.colorIndex)
                    : theme.primary;
            boolean marked = storageCollectorFiles.contains(child.file);

            if (skin.groupedListDividers && i > 0) {
                storageListContainer.addView(OsSkinRenderer.createGroupedDivider(this, theme, 24));
            }
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int rBg = marked
                    ? theme.errorContainer
                    : (skin.groupedListDividers ? Color.TRANSPARENT : theme.surfaceContainerHigh);
            row.setBackground(rippleBg(
                    rBg,
                    ripplePrimary,
                    marked ? theme.error : 0,
                    dp(skin.groupedListDividers ? 10 : skin.innerRadiusDp)));
            row.setPadding(dp(skin.groupedListDividers ? 6 : 12), dp(10), dp(skin.groupedListDividers ? 4 : 10), dp(10));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.bottomMargin = skin.groupedListDividers ? 0 : dp(8);
            row.setLayoutParams(rlp);

            View dot = new View(this);
            dot.setBackground(roundedRect(swatch, 0, dp(999)));
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(12), dp(12));
            dlp.rightMargin = dp(10);
            dot.setLayoutParams(dlp);
            row.addView(dot);

            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView nameTv = new TextView(this);
            nameTv.setText((child.isDir ? "📁 " : "") + child.name);
            nameTv.setTextColor(theme.onSurface);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            nameTv.setSingleLine(true);
            nameTv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            mid.addView(nameTv);

            String sub = child.isDir
                    ? String.format(Locale.US, "%d carp · %d arch · %.1f%%", child.dirCount, child.fileCount, pct)
                    : String.format(Locale.US, "Archivo · %.1f%%", pct);
            TextView subTv = new TextView(this);
            subTv.setText(sub);
            subTv.setTextColor(theme.onSurfaceVariant);
            subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            subTv.setSingleLine(true);
            subTv.setEllipsize(TextUtils.TruncateAt.END);
            mid.addView(subTv);

            LinearLayout barTrack = new LinearLayout(this);
            barTrack.setOrientation(LinearLayout.HORIZONTAL);
            barTrack.setWeightSum(100f);
            barTrack.setBackground(roundedRect(theme.surfaceContainerLowest, 0, dp(999)));
            LinearLayout.LayoutParams btLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(5));
            btLp.topMargin = dp(4);
            barTrack.setLayoutParams(btLp);

            float fillWeight = Math.max(2f, Math.min(100f, pct));
            float remWeight = Math.max(0f, 100f - fillWeight);
            View fillV = new View(this);
            fillV.setBackground(roundedRect(marked ? theme.error : swatch, 0, dp(999)));
            fillV.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fillWeight));
            barTrack.addView(fillV);
            View remV = new View(this);
            remV.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, remWeight));
            barTrack.addView(remV);
            mid.addView(barTrack);
            row.addView(mid);

            TextView sizeTv = new TextView(this);
            sizeTv.setText(TransferStats.formatBytes(child.size));
            sizeTv.setTextColor(marked ? theme.error : theme.onSurface);
            sizeTv.setTypeface(Typeface.DEFAULT_BOLD);
            sizeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            sizeTv.setPadding(dp(8), 0, dp(6), 0);
            row.addView(sizeTv);

            FrameLayout markCircle = createRoundIconButton(
                    marked ? R.drawable.ic_m3_check : R.drawable.ic_m3_stop,
                    marked ? theme.error : theme.surfaceContainerLowest,
                    marked ? theme.onError : theme.error,
                    34,
                    "Marcar para liberar espacio");
            LinearLayout.LayoutParams mcLp = new LinearLayout.LayoutParams(dp(34), dp(34));
            mcLp.rightMargin = dp(5);
            markCircle.setLayoutParams(mcLp);
            markCircle.setOnClickListener(v -> toggleStorageCollectorNode(child));
            row.addView(markCircle);

            FrameLayout moreCircle = createRoundIconButton(
                    R.drawable.ic_m3_more, theme.surfaceContainerLowest, theme.onSurfaceVariant, 34, "Opciones");
            moreCircle.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
            moreCircle.setOnClickListener(v -> showStorageNodeOptionsDialog(child));
            row.addView(moreCircle);

            row.setOnClickListener(v -> {
                if (child.isDir) {
                    if (storageSunburstView != null) storageSunburstView.setFocusedNode(child);
                    onStorageFocusChanged(child);
                } else {
                    showStorageNodeOptionsDialog(child);
                }
            });
            storageListContainer.addView(row);
        }

        if (children.size() > limit) {
            long remSize = 0L;
            for (int i = limit; i < children.size(); i++) remSize += children.get(i).size;
            TextView tail = new TextView(this);
            tail.setText("+ " + (children.size() - limit) + " elementos menores ("
                    + TransferStats.formatBytes(remSize) + ")");
            tail.setTextColor(theme.onSurfaceVariant);
            tail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tail.setGravity(Gravity.CENTER);
            tail.setPadding(0, dp(6), 0, dp(4));
            storageListContainer.addView(tail);
        }
    }

    private void toggleStorageCollectorNode(StorageAnalyzer.Node node) {
        if (node == null || node.file == null || node == storageTreeRoot) return;
        if (storageCollectorFiles.contains(node.file)) {
            storageCollectorFiles.remove(node.file);
            storageCollectorNodes.remove(node.file);
        } else {
            storageCollectorFiles.add(node.file);
            storageCollectorNodes.put(node.file, node);
        }
        renderStorageCollectorCard();
        if (storageSunburstView != null) {
            storageSunburstView.setMarkedFiles(storageCollectorFiles);
        }
        renderStorageAnalysisUi();
    }

    private void clearStorageCollector() {
        storageCollectorFiles.clear();
        storageCollectorNodes.clear();
        renderStorageCollectorCard();
        if (storageSunburstView != null) {
            storageSunburstView.setMarkedFiles(storageCollectorFiles);
        }
        renderStorageAnalysisUi();
    }

    private void pruneMissingCollectorNodes() {
        List<File> toDrop = new ArrayList<>();
        for (File f : storageCollectorFiles) {
            if (f == null || !f.exists()) toDrop.add(f);
        }
        for (File f : toDrop) {
            storageCollectorFiles.remove(f);
            storageCollectorNodes.remove(f);
        }
        renderStorageCollectorCard();
    }

    private void renderStorageCollectorCard() {
        if (storageCollectorCard == null) return;
        if (storageCollectorFiles.isEmpty()) {
            storageCollectorCard.setVisibility(View.GONE);
            return;
        }
        storageCollectorCard.setVisibility(View.VISIBLE);
        storageCollectorChipsRow.removeAllViews();

        long totalBytes = 0L;
        for (StorageAnalyzer.Node n : storageCollectorNodes.values()) {
            totalBytes += n.size;
            final StorageAnalyzer.Node item = n;
            TextView tag = new TextView(this);
            tag.setText(item.name + " (" + TransferStats.formatBytes(item.size) + ") ×");
            tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            tag.setTypeface(Typeface.DEFAULT_BOLD);
            tag.setTextColor(theme.onErrorContainer);
            tag.setBackground(rippleBg(
                    ThemeTokens.withAlpha(theme.onErrorContainer, 28), ripplePrimary, 0, dp(999)));
            tag.setPadding(dp(10), dp(5), dp(10), dp(5));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tlp.rightMargin = dp(6);
            tag.setLayoutParams(tlp);
            tag.setOnClickListener(v -> toggleStorageCollectorNode(item));
            storageCollectorChipsRow.addView(tag);
        }
        storageCollectorTitle.setText(storageCollectorFiles.size()
                + (storageCollectorFiles.size() == 1 ? " elemento" : " elementos")
                + " · " + TransferStats.formatBytes(totalBytes) + " liberables");
    }

    private void confirmPurgeStorageCollector() {
        if (storageCollectorNodes.isEmpty()) return;
        final List<StorageAnalyzer.Node> snapshot = new ArrayList<>(storageCollectorNodes.values());
        final List<File> targets = new ArrayList<>();
        long estBytes = 0L;
        for (StorageAnalyzer.Node n : snapshot) {
            if (isAncestorAlsoMarked(n)) continue;
            if (n.file.exists()) {
                targets.add(n.file);
                estBytes += n.size;
            }
        }
        if (targets.isEmpty()) {
            clearStorageCollector();
            return;
        }
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(
                d,
                "¿Liberar " + TransferStats.formatBytes(estBytes) + "?",
                "Se eliminarán definitivamente " + targets.size()
                        + (targets.size() == 1 ? " elemento marcado." : " elementos marcados."));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button cancelB = createIconPillButton("Cancelar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        clp.rightMargin = dp(6);
        cancelB.setLayoutParams(clp);
        cancelB.setOnClickListener(v -> d.dismiss());
        row.addView(cancelB);

        Button okB = createIconPillButton("Liberar ahora", R.drawable.ic_m3_stop, theme.error, theme.onError);
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(0, dp(42), 1.3f);
        olp.leftMargin = dp(6);
        okB.setLayoutParams(olp);
        okB.setOnClickListener(v -> {
            d.dismiss();
            runMonitoredFileOp(FileOpMonitor.OP_DELETE, targets, null, () -> {
                for (StorageAnalyzer.Node n : snapshot) {
                    if (n.file != null && !n.file.exists()) {
                        StorageAnalyzer.removeNodeFromTree(n);
                    }
                }
                updateAfterStorageDelete();
            });
        });
        row.addView(okB);
        box.addView(row);
        d.show();
    }

    private boolean isAncestorAlsoMarked(StorageAnalyzer.Node node) {
        StorageAnalyzer.Node p = node.parent;
        while (p != null) {
            if (storageCollectorFiles.contains(p.file)) return true;
            p = p.parent;
        }
        return false;
    }

    private void updateAfterStorageDelete() {
        pruneMissingCollectorNodes();
        while (storageFocusNode != null && storageFocusNode != storageTreeRoot
                && (storageFocusNode.file == null || !storageFocusNode.file.exists())) {
            storageFocusNode = storageFocusNode.parent;
        }
        if (storageFocusNode == null) storageFocusNode = storageTreeRoot;
        if (storageSunburstView != null) {
            storageSunburstView.setMarkedFiles(storageCollectorFiles);
            storageSunburstView.setFocusedNode(storageFocusNode);
        }
        renderStorageAnalysisUi();
        refreshStorageVolumesBar();
    }

    private void showStorageNodeOptionsDialog(final StorageAnalyzer.Node node) {
        if (node == null || node.file == null) return;
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(
                d,
                node.name,
                TransferStats.formatBytes(node.size) + (node.isDir
                        ? (" · " + node.dirCount + " carpetas · " + node.fileCount + " archivos")
                        : " · Archivo"));

        if (node.isDir) {
            addModalOption(box, d, "Explorar en Sunburst", R.drawable.ic_m3_storage,
                    theme.primaryContainer, theme.onPrimaryContainer, () -> {
                        if (storageSunburstView != null) storageSunburstView.setFocusedNode(node);
                        onStorageFocusChanged(node);
                    });
        }
        boolean marked = storageCollectorFiles.contains(node.file);
        addModalOption(box, d,
                marked ? "Quitar de Liberar espacio" : "Marcar para Liberar espacio",
                R.drawable.ic_m3_check,
                marked ? theme.secondaryContainer : theme.errorContainer,
                marked ? theme.onSecondaryContainer : theme.onErrorContainer,
                () -> toggleStorageCollectorNode(node));

        addModalOption(box, d, "Abrir en Archivos", R.drawable.ic_m3_folder,
                theme.surfaceContainerHighest, theme.onSurface, () -> {
                    File dest = node.isDir ? node.file : node.file.getParentFile();
                    if (dest != null && dest.isDirectory()) {
                        navigateToDir(dest);
                        switchTab(1);
                    }
                });

        addModalOption(box, d, "Propiedades y hashes", R.drawable.ic_m3_tune,
                theme.surfaceContainerHighest, theme.onSurface,
                () -> showPropertiesDialog(node.file));

        addModalOption(box, d, "Eliminar ahora", R.drawable.ic_m3_stop,
                theme.error, theme.onError, () -> confirmSingleStorageDelete(node));

        d.show();
    }

    private void confirmSingleStorageDelete(final StorageAnalyzer.Node node) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(
                d,
                "¿Eliminar " + (node.isDir ? "la carpeta" : "el archivo") + "?",
                node.name + " (" + TransferStats.formatBytes(node.size) + ")");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button cancelB = createIconPillButton("Cancelar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        clp.rightMargin = dp(6);
        cancelB.setLayoutParams(clp);
        cancelB.setOnClickListener(v -> d.dismiss());
        row.addView(cancelB);

        Button okB = createIconPillButton("Eliminar", R.drawable.ic_m3_stop, theme.error, theme.onError);
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        olp.leftMargin = dp(6);
        okB.setLayoutParams(olp);
        okB.setOnClickListener(v -> {
            d.dismiss();
            runMonitoredFileOp(FileOpMonitor.OP_DELETE, Collections.singletonList(node.file), null, () -> {
                if (!node.file.exists()) {
                    StorageAnalyzer.removeNodeFromTree(node);
                    updateAfterStorageDelete();
                }
            });
        });
        row.addView(okB);
        box.addView(row);
        d.show();
    }

    private void openFocusedInFilesTab() {
        File dir = (storageFocusNode != null && storageFocusNode.isDir && storageFocusNode.file != null
                && storageFocusNode.file.exists())
                ? storageFocusNode.file
                : (storageSelectedVolumeRoot != null ? storageSelectedVolumeRoot : storageRoot);
        if (dir != null && dir.isDirectory()) {
            navigateToDir(dir);
            switchTab(1);
        }
    }

    private void buildSettingsTab(LinearLayout parent) {
        LinearLayout srvCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, srvCard, "Control del servidor", R.drawable.ic_m3_power, theme.primary, skin, theme, true);

        settingsStateText = new TextView(this);
        settingsStateText.setTextColor(theme.onSurface);
        settingsStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        settingsStateText.setPadding(0, 0, 0, dp(14));
        srvCard.addView(settingsStateText);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);

        toggleBtn = createIconPillButton("Detener", R.drawable.ic_m3_power, theme.errorContainer, theme.onErrorContainer);
        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        tbLp.rightMargin = dp(6);
        toggleBtn.setLayoutParams(tbLp);
        toggleBtn.setOnClickListener(v -> {
            if (stats.isServerRunning()) {
                stopServerFromUi();
            } else {
                startServerFromUi();
            }
        });
        actionRow.addView(toggleBtn);

        restartBtn = createIconPillButton("Reiniciar", R.drawable.ic_m3_refresh, theme.secondaryContainer, theme.onSecondaryContainer);
        LinearLayout.LayoutParams rbLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        rbLp.leftMargin = dp(6);
        restartBtn.setLayoutParams(rbLp);
        restartBtn.setOnClickListener(v -> {
            startTransferService(TransferService.ACTION_RESTART);
            Toast.makeText(this, "Reiniciando servidor...", Toast.LENGTH_SHORT).show();
            uiHandler.postDelayed(this::refreshUi, 300);
        });
        actionRow.addView(restartBtn);
        srvCard.addView(actionRow);

        // Web Portal Credentials Card
        LinearLayout credCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, credCard, "Autenticación del portal web", R.drawable.ic_m3_key, theme.secondary, skin, theme, true);

        TextView credDesc = new TextView(this);
        credDesc.setText("Define el usuario y contraseña para entrar en la web desde el navegador. Si dejas el usuario vacío, el portal será abierto sin contraseña.");
        credDesc.setTextColor(theme.onSurfaceVariant);
        credDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        credDesc.setPadding(0, 0, 0, dp(14));
        credCard.addView(credDesc);

        LinearLayout credInputs = new LinearLayout(this);
        credInputs.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout userCol = new LinearLayout(this);
        userCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ucLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        ucLp.rightMargin = dp(6);
        userCol.setLayoutParams(ucLp);
        userCol.addView(createSmallLabel("Usuario"));
        userEdit = createExpressiveEditText(TransferService.getSavedUser(this), "Usuario");
        userCol.addView(userEdit);
        credInputs.addView(userCol);

        LinearLayout passCol = new LinearLayout(this);
        passCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams pcLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        pcLp.leftMargin = dp(6);
        passCol.setLayoutParams(pcLp);
        passCol.addView(createSmallLabel("Contraseña"));
        passEdit = createExpressiveEditText(TransferService.getSavedPass(this), "Contraseña");
        passCol.addView(passEdit);
        credInputs.addView(passCol);
        credCard.addView(credInputs);

        Button saveRestartBtn = createIconPillButton(
                "Guardar y reiniciar servidor", R.drawable.ic_m3_refresh, theme.primary, theme.onPrimary);
        LinearLayout.LayoutParams srLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        srLp.topMargin = dp(16);
        saveRestartBtn.setLayoutParams(srLp);
        saveRestartBtn.setOnClickListener(v -> {
            String newUser = userEdit.getText().toString().trim();
            String newPass = passEdit.getText().toString();
            TransferService.saveCredentials(this, newUser, newPass);
            startTransferService(TransferService.ACTION_RESTART);
            String msg = newUser.isEmpty()
                    ? "Portal abierto guardado · Servidor reiniciado"
                    : "Credenciales web guardadas (" + newUser + ") · Servidor reiniciado";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            uiHandler.postDelayed(this::refreshUi, 300);
        });
        credCard.addView(saveRestartBtn);

        // Storage & System Status Card
        LinearLayout storeCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, storeCard, "Almacenamiento y permisos", R.drawable.ic_m3_storage, theme.tertiary, skin, theme, true);

        storageText = new TextView(this);
        storageText.setTextColor(theme.onSurface);
        storageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        storeCard.addView(storageText);

        storageProgress = createExpressiveProgressBar(theme.primary, theme.surfaceContainerHighest, 10);
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        spLp.topMargin = dp(10);
        spLp.bottomMargin = dp(14);
        storageProgress.setLayoutParams(spLp);
        storageProgress.setMax(100);
        storeCard.addView(storageProgress);

        permStatusText = new TextView(this);
        permStatusText.setTextColor(theme.onSurfaceVariant);
        permStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        storeCard.addView(permStatusText);

        // Native OS / OEM Design Language Card
        LinearLayout designCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, designCard, "Diseño nativo del sistema", R.drawable.ic_m3_tune, theme.primary, skin, theme, true);

        osDesignSummaryText = new TextView(this);
        OsDesignSystem.SkinId curOver = OsDesignSystem.getSavedOverride(this);
        osDesignSummaryText.setText("Detectado: " + skin.hardwareLabel
                + "\nActivo: " + (curOver == OsDesignSystem.SkinId.AUTO ? ("Automático (" + skin.displayName + ")") : skin.displayName));
        osDesignSummaryText.setTextColor(theme.onSurface);
        osDesignSummaryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        osDesignSummaryText.setPadding(0, 0, 0, dp(12));
        designCard.addView(osDesignSummaryText);

        Button pickSkinBtn = createIconPillButton(
                "Elegir capa / lenguaje visual", R.drawable.ic_m3_tune, theme.secondaryContainer, theme.onSecondaryContainer);
        pickSkinBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        pickSkinBtn.setOnClickListener(v -> showDesignSystemPickerDialog());
        designCard.addView(pickSkinBtn);

        // About & App Version Card (always at the bottom of Ajustes)
        LinearLayout aboutCard = createExpressiveCard(parent, theme.surfaceContainerLow, 28);
        OsSkinRenderer.addSectionHeader(this, parent, aboutCard, "Acerca de", R.drawable.ic_m3_brand, theme.primary, skin, theme, true);

        TextView verTitle = new TextView(this);
        verTitle.setText("Expressive Web Files · v" + AppVersion.NAME);
        verTitle.setTextColor(theme.onSurface);
        verTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        verTitle.setTypeface(Typeface.DEFAULT_BOLD);
        verTitle.setPadding(0, 0, 0, dp(8));
        aboutCard.addView(verTitle);

        updateStatusText = new TextView(this);
        boolean autoUpd = AppUpdater.isAutoCheckEnabled(this);
        updateStatusText.setText("Comprobación automática: " + (autoUpd ? "Activada" : "Desactivada") + " · GitHub Releases");
        updateStatusText.setTextColor(theme.onSurfaceVariant);
        updateStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        updateStatusText.setPadding(0, 0, 0, dp(10));
        aboutCard.addView(updateStatusText);

        checkUpdateBtn = createIconPillButton(
                "Buscar actualizaciones", R.drawable.ic_m3_refresh, theme.primaryContainer, theme.onPrimaryContainer);
        LinearLayout.LayoutParams cubLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
        cubLp.bottomMargin = dp(8);
        checkUpdateBtn.setLayoutParams(cubLp);
        checkUpdateBtn.setOnClickListener(v -> checkForUpdates(false));
        aboutCard.addView(checkUpdateBtn);

        autoUpdateToggleBtn = createIconPillButton(
                autoUpd ? "Auto-actualizar: Activado" : "Auto-actualizar: Desactivado",
                R.drawable.ic_m3_tune, theme.surfaceContainerHighest, theme.onSurface);
        autoUpdateToggleBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        autoUpdateToggleBtn.setOnClickListener(v -> {
            boolean next = !AppUpdater.isAutoCheckEnabled(this);
            AppUpdater.setAutoCheckEnabled(this, next);
            autoUpdateToggleBtn.setText(next ? "Auto-actualizar: Activado" : "Auto-actualizar: Desactivado");
            updateStatusText.setText("Comprobación automática: " + (next ? "Activada" : "Desactivada") + " · GitHub Releases");
            Toast.makeText(this,
                    next ? "Comprobación automática activada" : "Comprobación automática desactivada",
                    Toast.LENGTH_SHORT).show();
        });
        aboutCard.addView(autoUpdateToggleBtn);
    }

    private void checkForUpdates(final boolean silent) {
        if (!silent && checkUpdateBtn != null) {
            checkUpdateBtn.setEnabled(false);
            checkUpdateBtn.setText("Comprobando GitHub...");
        }
        updateExecutor.execute(() -> {
            final AppUpdater.CheckResult res = AppUpdater.checkLatestRelease(MainActivity.this);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (checkUpdateBtn != null) {
                    checkUpdateBtn.setEnabled(true);
                    checkUpdateBtn.setText("Buscar actualizaciones");
                }
                switch (res.status) {
                    case UPDATE_AVAILABLE:
                        if (silent && res.release.versionName.equals(AppUpdater.getSkippedVersion(MainActivity.this))) {
                            return;
                        }
                        if (updateStatusText != null) {
                            updateStatusText.setText("Nueva versión disponible: v" + res.release.versionName);
                        }
                        showUpdateAvailableDialog(res.release);
                        break;
                    case UP_TO_DATE:
                        if (updateStatusText != null) {
                            updateStatusText.setText("Al día · Última versión v" + AppVersion.NAME);
                        }
                        if (!silent) {
                            Toast.makeText(MainActivity.this,
                                    "Ya tienes la última versión (v" + AppVersion.NAME + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case NO_RELEASE:
                        if (updateStatusText != null) {
                            updateStatusText.setText(res.message);
                        }
                        if (!silent) {
                            Toast.makeText(MainActivity.this, res.message, Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        if (!silent) {
                            if (updateStatusText != null) {
                                updateStatusText.setText(res.message);
                            }
                            Toast.makeText(MainActivity.this, res.message, Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            });
        });
    }

    private void showUpdateAvailableDialog(final AppUpdater.ReleaseInfo info) {
        final Dialog d = new Dialog(this);
        String sizePart = info.apkSize > 0 ? (" · " + TransferStats.formatBytes(info.apkSize)) : "";
        LinearLayout box = showExpressiveModal(
                d,
                "Actualización v" + info.versionName,
                "Instalada: v" + AppVersion.NAME + sizePart);

        ScrollView notesScroll = new ScrollView(this);
        LinearLayout.LayoutParams nslp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(150));
        nslp.bottomMargin = dp(12);
        notesScroll.setLayoutParams(nslp);

        TextView notesView = new TextView(this);
        String bodyText = (info.changelog == null || info.changelog.isEmpty())
                ? "Mejoras y correcciones en esta versión."
                : info.changelog;
        notesView.setText(bodyText);
        notesView.setTextColor(theme.onSurfaceVariant);
        notesView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        notesView.setPadding(dp(2), dp(2), dp(2), dp(2));
        notesScroll.addView(notesView);
        box.addView(notesScroll);

        final ProgressBar dlBar = createExpressiveProgressBar(theme.primary, theme.surfaceContainerHighest, 10);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        barLp.bottomMargin = dp(6);
        dlBar.setLayoutParams(barLp);
        dlBar.setMax(100);
        dlBar.setVisibility(View.GONE);
        box.addView(dlBar);

        final TextView dlText = new TextView(this);
        dlText.setTextColor(theme.onSurfaceVariant);
        dlText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        dlText.setPadding(0, 0, 0, dp(10));
        dlText.setVisibility(View.GONE);
        box.addView(dlText);

        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final boolean[] downloading = new boolean[]{false};
        final File[] readyApk = new File[]{null};

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        final Button laterBtn = createIconPillButton("Más tarde", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        laterBtn.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        llp.rightMargin = dp(6);
        laterBtn.setLayoutParams(llp);

        final Button installBtn = createIconPillButton("Descargar e instalar", R.drawable.ic_m3_download, theme.primary, theme.onPrimary);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(0, dp(44), 1.4f);
        ilp.leftMargin = dp(6);
        installBtn.setLayoutParams(ilp);

        laterBtn.setOnClickListener(v -> {
            if (downloading[0]) {
                cancelled.set(true);
                dlText.setText("Cancelando descarga...");
            } else {
                AppUpdater.markSkippedVersion(MainActivity.this, info.versionName);
                d.dismiss();
            }
        });

        installBtn.setOnClickListener(v -> {
            if (readyApk[0] != null && readyApk[0].isFile()) {
                AppUpdater.InstallResult ir = AppUpdater.installApk(MainActivity.this, readyApk[0]);
                if (ir == AppUpdater.InstallResult.SESSION_COMMITTED) {
                    d.dismiss();
                } else if (ir == AppUpdater.InstallResult.NEEDS_PERMISSION) {
                    dlText.setVisibility(View.VISIBLE);
                    dlText.setText("Concede el permiso de instalar apps desconocidas y pulsa «Instalar ahora»");
                } else {
                    Toast.makeText(MainActivity.this, "No se pudo iniciar la instalación", Toast.LENGTH_LONG).show();
                }
                return;
            }
            if (downloading[0]) return;
            downloading[0] = true;
            cancelled.set(false);
            installBtn.setEnabled(false);
            installBtn.setText("Descargando...");
            laterBtn.setText("Cancelar");
            dlBar.setVisibility(View.VISIBLE);
            dlBar.setProgress(0);
            dlText.setVisibility(View.VISIBLE);
            dlText.setText("Conectando con GitHub Releases...");

            updateExecutor.execute(() -> {
                try {
                    final long[] lastUiUpdate = new long[]{0L};
                    File apk = AppUpdater.downloadApk(MainActivity.this, info, (done, total) -> {
                        long now = SystemClock.uptimeMillis();
                        if (now - lastUiUpdate[0] < 120L && done < total) return;
                        lastUiUpdate[0] = now;
                        runOnUiThread(() -> {
                            if (!d.isShowing()) return;
                            int pct = total > 0 ? (int) Math.min(100L, (done * 100L) / total) : 0;
                            dlBar.setProgress(pct);
                            String progressLabel = total > 0
                                    ? ("Descargando... " + pct + "% (" + TransferStats.formatBytes(done) + " / " + TransferStats.formatBytes(total) + ")")
                                    : ("Descargando... " + TransferStats.formatBytes(done));
                            dlText.setText(progressLabel);
                        });
                    }, cancelled);

                    runOnUiThread(() -> {
                        downloading[0] = false;
                        if (!d.isShowing()) return;
                        readyApk[0] = apk;
                        dlBar.setProgress(100);
                        AppUpdater.InstallResult ir = AppUpdater.installApk(MainActivity.this, apk);
                        if (ir == AppUpdater.InstallResult.SESSION_COMMITTED) {
                            d.dismiss();
                        } else if (ir == AppUpdater.InstallResult.NEEDS_PERMISSION) {
                            installBtn.setEnabled(true);
                            installBtn.setText("Instalar ahora");
                            laterBtn.setText("Más tarde");
                            dlText.setText("Concede el permiso de instalar apps desconocidas y pulsa «Instalar ahora»");
                        } else {
                            installBtn.setEnabled(true);
                            installBtn.setText("Reintentar");
                            laterBtn.setText("Cerrar");
                            dlText.setText("Error al preparar el instalador de Android");
                        }
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        downloading[0] = false;
                        if (!d.isShowing()) return;
                        installBtn.setEnabled(true);
                        installBtn.setText("Descargar e instalar");
                        laterBtn.setText("Más tarde");
                        dlBar.setVisibility(View.GONE);
                        dlText.setText(ex.getMessage());
                    });
                }
            });
        });

        row.addView(laterBtn);
        row.addView(installBtn);
        box.addView(row);
        d.setOnDismissListener(dialog -> {
            if (downloading[0]) {
                cancelled.set(true);
            }
        });
        d.show();
    }

    private void showDesignSystemPickerDialog() {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(
                d,
                "Lenguaje de diseño",
                "Detectado en este dispositivo: " + skin.hardwareLabel);
        OsDesignSystem.SkinId current = OsDesignSystem.getSavedOverride(this);
        for (final OsDesignSystem.SkinId candidate : OsDesignSystem.SkinId.values()) {
            boolean sel = (candidate == current);
            String label = (candidate == OsDesignSystem.SkinId.AUTO)
                    ? ("Automático (" + skin.hardwareLabel + ")")
                    : candidate.selectorLabel;
            addModalOption(
                    box,
                    d,
                    label,
                    sel ? R.drawable.ic_m3_check : 0,
                    sel ? theme.primaryContainer : theme.surfaceContainerHighest,
                    sel ? theme.onPrimaryContainer : theme.onSurface,
                    () -> {
                        OsDesignSystem.saveOverride(this, candidate);
                        recreate();
                    });
        }
        d.show();
    }

    private boolean isCompactScreen() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float widthDp = dm.widthPixels / Math.max(1f, dm.density);
        return widthDp < 440f;
    }

    private FrameLayout createRoundIconButton(int iconRes, int fillColor, int iconColor, int sizeDp, String contentDesc) {
        FrameLayout btn = new FrameLayout(this);
        btn.setBackground(rippleBg(fillColor, ripplePrimary, 0, dp(999)));
        btn.setClickable(true);
        btn.setFocusable(true);
        if (contentDesc != null) btn.setContentDescription(contentDesc);

        ImageView iv = new ImageView(this);
        int iconDp = Math.max(15, (int) (sizeDp * 0.54f));
        iv.setImageDrawable(tintedIcon(iconRes, iconColor, iconDp));
        FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(dp(iconDp), dp(iconDp), Gravity.CENTER);
        btn.addView(iv, ilp);
        btn.setTag(iv);
        attachSpringTouch(btn);
        return btn;
    }

    private void updateRoundButtonVisual(View v, int iconRes, int fillColor, int iconColor, int iconDp) {
        if (v == null) return;
        v.setBackground(rippleBg(fillColor, ripplePrimary, 0, dp(999)));
        if (v instanceof Button) {
            Button b = (Button) v;
            b.setTextColor(iconColor);
            if (iconRes > 0) setButtonIconTint(b, iconRes, iconColor, iconDp);
        } else if (v.getTag() instanceof ImageView) {
            ImageView iv = (ImageView) v.getTag();
            iv.setImageDrawable(tintedIcon(iconRes, iconColor, iconDp));
        }
    }

    private File resolveActiveVolumeRoot(File dir) {
        if (dir != null) {
            StorageVolumes.VolumeInfo vol = StorageVolumes.findForFile(this, dir);
            if (vol != null && vol.rootDir != null) {
                return vol.rootDir;
            }
        }
        return storageRoot;
    }

    private String resolveActiveVolumeLabel(File dir) {
        if (dir != null) {
            StorageVolumes.VolumeInfo vol = StorageVolumes.findForFile(this, dir);
            if (vol != null && !vol.id.isEmpty()) {
                return vol.label;
            }
        }
        return "Raíz";
    }

    private boolean isAtCurrentVolumeRoot(File dir) {
        if (dir == null) return true;
        File volRoot = resolveActiveVolumeRoot(dir);
        return volRoot == null || dir.equals(volRoot);
    }

    private String currentRelDisplayPath() {
        if (currentBrowseDir == null) return "/";
        File volRoot = resolveActiveVolumeRoot(currentBrowseDir);
        if (volRoot == null) return "/";
        String rootPath = volRoot.getAbsolutePath();
        String curPath = currentBrowseDir.getAbsolutePath();
        String rel = curPath.equals(rootPath) ? "/" : curPath.replace(rootPath, "");
        if (!rel.startsWith("/")) rel = "/" + rel;
        String volLabel = resolveActiveVolumeLabel(currentBrowseDir);
        return "Raíz".equals(volLabel) ? rel : (volLabel + ":" + rel);
    }

    private void buildFilesTab(LinearLayout parent) {
        storageRoot = Environment.getExternalStorageDirectory();
        currentBrowseDir = storageRoot;
        File dlInit = new File(storageRoot, "Download");
        if (dlInit.isDirectory()) {
            currentBrowseDir = dlInit;
        }
        fileTabs.clear();
        fileTabs.add(currentBrowseDir);
        activeFileTab = 0;
        loadPinnedPaths();

        final boolean compact = isCompactScreen();

        // ZIP / File-op progress card (hidden until an archive or file op is active)
        LinearLayout zCard = createExpressiveCard(parent, theme.primaryContainer, 28);
        zipStatusCard = zCard;
        zCard.setVisibility(View.GONE);

        LinearLayout zTop = new LinearLayout(this);
        zTop.setOrientation(LinearLayout.HORIZONTAL);
        zTop.setGravity(Gravity.CENTER_VERTICAL);

        zipStatusTitle = new TextView(this);
        zipStatusTitle.setTextColor(theme.onPrimaryContainer);
        zipStatusTitle.setTypeface(Typeface.DEFAULT_BOLD);
        zipStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        zipStatusTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        zTop.addView(zipStatusTitle);

        Button zCancel = createIconPillButton("Cancelar", R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer);
        zCancel.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(34)));
        zCancel.setPadding(dp(12), 0, dp(12), 0);
        zCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        zCancel.setOnClickListener(v -> {
            AtomicBoolean flag = activeZipCancel;
            if (flag != null) {
                flag.set(true);
            }
            if (activeUiFileOp != null) {
                activeUiFileOp.cancelled.set(true);
                FileOpMonitor.getInstance().cancelAll();
            }
        });
        zTop.addView(zCancel);
        zCard.addView(zTop);

        zipStatusBar = createExpressiveProgressBar(theme.onPrimaryContainer, ThemeTokens.withAlpha(theme.onPrimaryContainer, 55), 8);
        zipStatusBar.setMax(100);
        zipStatusBar.setIndeterminate(true);
        LinearLayout.LayoutParams zbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        zbLp.topMargin = dp(10);
        zbLp.bottomMargin = dp(8);
        zipStatusBar.setLayoutParams(zbLp);
        zCard.addView(zipStatusBar);

        zipStatusDetail = new TextView(this);
        zipStatusDetail.setTextColor(ThemeTokens.withAlpha(theme.onPrimaryContainer, 220));
        zipStatusDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        zipStatusDetail.setFontFeatureSettings("tnum");
        zCard.addView(zipStatusDetail);

        // Explorer Navigation Card (MiXplorer Tabbed × M3E)
        LinearLayout ctrlCard = createExpressiveCard(parent, theme.surfaceContainerLow, 26);
        ctrlCard.setPadding(dp(compact ? 12 : 18), dp(14), dp(compact ? 12 : 18), dp(14));

        // Row 1: Bookmarks/Categories Drawer Button + Scrollable Folder Tabs + New Tab Button
        LinearLayout topTabBar = new LinearLayout(this);
        topTabBar.setOrientation(LinearLayout.HORIZONTAL);
        topTabBar.setGravity(Gravity.CENTER_VERTICAL);
        topTabBar.setPadding(0, 0, 0, dp(10));

        FrameLayout drawerBtn = createRoundIconButton(
                R.drawable.ic_m3_menu, theme.secondaryContainer, theme.onSecondaryContainer, 36, "Marcadores y bibliotecas");
        LinearLayout.LayoutParams dbLp = new LinearLayout.LayoutParams(dp(36), dp(36));
        dbLp.rightMargin = dp(8);
        drawerBtn.setLayoutParams(dbLp);
        drawerBtn.setOnClickListener(v -> showBookmarksLibrarySheet());
        topTabBar.addView(drawerBtn);

        HorizontalScrollView tabsHsv = new HorizontalScrollView(this);
        tabsHsv.setHorizontalScrollBarEnabled(false);
        tabsHsv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        fileTabsRow = new LinearLayout(this);
        fileTabsRow.setOrientation(LinearLayout.HORIZONTAL);
        fileTabsRow.setGravity(Gravity.CENTER_VERTICAL);
        tabsHsv.addView(fileTabsRow);
        topTabBar.addView(tabsHsv);

        FrameLayout addTabCircle = createRoundIconButton(
                R.drawable.ic_m3_add, theme.surfaceContainerHigh, theme.onSurfaceVariant, 34, "Nueva pestaña");
        LinearLayout.LayoutParams atLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        atLp.leftMargin = dp(6);
        addTabCircle.setLayoutParams(atLp);
        addTabCircle.setOnClickListener(v -> {
            if (fileTabs.size() >= 6) {
                Toast.makeText(this, "Máximo 6 pestañas abiertas", Toast.LENGTH_SHORT).show();
                return;
            }
            File dir = (currentBrowseDir != null) ? currentBrowseDir : storageRoot;
            fileTabs.add(dir);
            activeFileTab = fileTabs.size() - 1;
            activeCategory = null;
            deepSearchMode = false;
            refreshFilesList();
        });
        topTabBar.addView(addTabCircle);
        ctrlCard.addView(topTabBar);

        // Mounted External Media Chips (SD / USB OTG — visible automatically when attached)
        extVolumesScroll = new HorizontalScrollView(this);
        extVolumesScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams evLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        evLp.bottomMargin = dp(8);
        extVolumesScroll.setLayoutParams(evLp);
        extVolumesScroll.setVisibility(View.GONE);
        extVolumesRow = new LinearLayout(this);
        extVolumesRow.setOrientation(LinearLayout.HORIZONTAL);
        extVolumesRow.setGravity(Gravity.CENTER_VERTICAL);
        extVolumesScroll.addView(extVolumesRow);
        ctrlCard.addView(extVolumesScroll);

        // Row 2: Clickable Breadcrumb Trail + Status Badge
        breadcrumbsScroll = new HorizontalScrollView(this);
        breadcrumbsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams bcLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bcLp.bottomMargin = dp(10);
        breadcrumbsScroll.setLayoutParams(bcLp);
        breadcrumbsScroll.setBackground(roundedRect(theme.surfaceContainer, 0, dp(16)));
        breadcrumbsScroll.setPadding(dp(10), dp(6), dp(10), dp(6));

        breadcrumbsRow = new LinearLayout(this);
        breadcrumbsRow.setOrientation(LinearLayout.HORIZONTAL);
        breadcrumbsRow.setGravity(Gravity.CENTER_VERTICAL);
        breadcrumbsScroll.addView(breadcrumbsRow);
        ctrlCard.addView(breadcrumbsScroll);

        // Hidden compatibility reference kept minimal
        filesPathBadge = new TextView(this);
        filesPathBadge.setVisibility(View.GONE);

        // Row 3: Single Clean M3E Toolbar (round icon buttons on compact width, icon pills on wide)
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        if (compact) {
            View[] spacerWrap = new View[6];

            FrameLayout searchIconBtn = createRoundIconButton(
                    R.drawable.ic_m3_search, theme.surfaceContainerHigh, theme.onSurfaceVariant, 38, "Buscar");
            filesSearchBtn = searchIconBtn;
            searchIconBtn.setOnClickListener(v -> toggleSearchPanel());
            spacerWrap[0] = searchIconBtn;

            FrameLayout sortIconBtn = createRoundIconButton(
                    R.drawable.ic_m3_tune, theme.surfaceContainerHigh, theme.onSurfaceVariant, 38, "Ordenar");
            filesSortBtn = sortIconBtn;
            sortIconBtn.setOnClickListener(v -> showSortDialog());
            spacerWrap[1] = sortIconBtn;

            FrameLayout selIconBtn = createRoundIconButton(
                    R.drawable.ic_m3_check, theme.surfaceContainerHigh, theme.onSurfaceVariant, 38, "Seleccionar");
            filesSelectBtn = selIconBtn;
            selIconBtn.setOnClickListener(v -> {
                selectionMode = !selectionMode;
                if (!selectionMode) selectedPaths.clear();
                refreshFilesList();
            });
            spacerWrap[2] = selIconBtn;

            FrameLayout zipIconBtn = createRoundIconButton(
                    R.drawable.ic_m3_zip, theme.primaryContainer, theme.onPrimaryContainer, 38, "Empaquetar carpeta");
            zipCurrentDirBtn = zipIconBtn;
            zipIconBtn.setOnClickListener(v -> packCurrentFolderPrompt());
            spacerWrap[3] = zipIconBtn;

            FrameLayout refIconBtn = createRoundIconButton(
                    R.drawable.ic_m3_refresh, theme.secondaryContainer, theme.onSecondaryContainer, 38, "Actualizar");
            refIconBtn.setOnClickListener(v -> refreshFilesList());
            spacerWrap[4] = refIconBtn;

            FrameLayout createIconBtn = createRoundIconButton(
                    R.drawable.ic_m3_add, theme.primary, theme.onPrimary, 38, "Crear nuevo");
            createIconBtn.setOnClickListener(v -> showCreateActionsSheet());
            spacerWrap[5] = createIconBtn;

            for (int i = 0; i < spacerWrap.length; i++) {
                LinearLayout cell = new LinearLayout(this);
                cell.setGravity(Gravity.CENTER);
                cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                spacerWrap[i].setLayoutParams(new FrameLayout.LayoutParams(dp(38), dp(38)));
                cell.addView(spacerWrap[i]);
                toolbar.addView(cell);
            }
        } else {
            HorizontalScrollView tbHsv = new HorizontalScrollView(this);
            tbHsv.setHorizontalScrollBarEnabled(false);
            LinearLayout tbRow = new LinearLayout(this);
            tbRow.setOrientation(LinearLayout.HORIZONTAL);

            Button bSearch = makeSmallChip("Buscar", R.drawable.ic_m3_search);
            filesSearchBtn = bSearch;
            bSearch.setOnClickListener(v -> toggleSearchPanel());
            tbRow.addView(bSearch);

            Button bSort = makeSmallChip("Orden: Nombre", R.drawable.ic_m3_tune);
            filesSortBtn = bSort;
            bSort.setOnClickListener(v -> showSortDialog());
            tbRow.addView(bSort);

            Button bSel = makeSmallChip("Seleccionar", R.drawable.ic_m3_check);
            filesSelectBtn = bSel;
            bSel.setOnClickListener(v -> {
                selectionMode = !selectionMode;
                if (!selectionMode) selectedPaths.clear();
                refreshFilesList();
            });
            tbRow.addView(bSel);

            Button bZip = createIconPillButton("Empaquetar", R.drawable.ic_m3_zip, theme.primaryContainer, theme.onPrimaryContainer);
            bZip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            bZip.setPadding(dp(12), 0, dp(14), 0);
            setButtonIconTint(bZip, R.drawable.ic_m3_zip, theme.onPrimaryContainer, 15);
            LinearLayout.LayoutParams zLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36));
            zLp.rightMargin = dp(6);
            bZip.setLayoutParams(zLp);
            zipCurrentDirBtn = bZip;
            bZip.setOnClickListener(v -> packCurrentFolderPrompt());
            tbRow.addView(bZip);

            Button bNew = createIconPillButton("+ Nuevo", R.drawable.ic_m3_add, theme.primary, theme.onPrimary);
            bNew.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            bNew.setPadding(dp(12), 0, dp(14), 0);
            setButtonIconTint(bNew, R.drawable.ic_m3_add, theme.onPrimary, 15);
            LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36));
            nLp.rightMargin = dp(6);
            bNew.setLayoutParams(nLp);
            bNew.setOnClickListener(v -> showCreateActionsSheet());
            tbRow.addView(bNew);

            Button bRef = makeSmallChip("Actualizar", R.drawable.ic_m3_refresh);
            bRef.setOnClickListener(v -> refreshFilesList());
            tbRow.addView(bRef);

            tbHsv.addView(tbRow);
            toolbar.addView(tbHsv);
        }
        ctrlCard.addView(toolbar);

        // Collapsible Inline Search Panel (MiXplorer style — hidden until magnifier tapped)
        searchBoxWrap = new LinearLayout(this);
        searchBoxWrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        swLp.topMargin = dp(10);
        searchBoxWrap.setLayoutParams(swLp);
        searchBoxWrap.setVisibility(View.GONE);

        LinearLayout searchInputRow = new LinearLayout(this);
        searchInputRow.setOrientation(LinearLayout.HORIZONTAL);
        searchInputRow.setGravity(Gravity.CENTER_VERTICAL);

        searchEt = new EditText(this);
        searchEt.setHint("Nombre de archivo o carpeta...");
        searchEt.setHintTextColor(ThemeTokens.withAlpha(theme.onSurfaceVariant, 130));
        searchEt.setTextColor(theme.onSurface);
        searchEt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        searchEt.setSingleLine(true);
        searchEt.setBackground(roundedRect(theme.surfaceContainerHigh, ThemeTokens.withAlpha(theme.outlineVariant, 0x70), dp(18)));
        searchEt.setPadding(dp(14), dp(9), dp(14), dp(9));
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        sLp.rightMargin = dp(6);
        searchEt.setLayoutParams(sLp);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fileSearchQuery = (s != null) ? s.toString().trim().toLowerCase(Locale.US) : "";
                refreshFilesList();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchInputRow.addView(searchEt);

        Button deepChip = makeSmallChip("Subcarpetas", 0);
        deepChip.setCompoundDrawablesRelative(null, null, null, null);
        deepChip.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36)));
        filesDeepSearchBtn = deepChip;
        deepChip.setOnClickListener(v -> {
            deepSearchMode = !deepSearchMode;
            if (deepSearchMode) activeCategory = null;
            refreshFilesList();
        });
        searchInputRow.addView(deepChip);

        FrameLayout clearSearchBtn = createRoundIconButton(
                R.drawable.ic_m3_stop, theme.surfaceContainerHighest, theme.onSurfaceVariant, 34, "Limpiar búsqueda");
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        csLp.leftMargin = dp(6);
        clearSearchBtn.setLayoutParams(csLp);
        clearSearchBtn.setOnClickListener(v -> {
            searchEt.setText("");
            deepSearchMode = false;
            searchExpanded = false;
            refreshFilesList();
        });
        searchInputRow.addView(clearSearchBtn);

        searchBoxWrap.addView(searchInputRow);
        ctrlCard.addView(searchBoxWrap);

        // Multi-Select Contextual Bar (hidden unless selectionMode)
        selectionBarCard = createExpressiveCard(parent, theme.primaryContainer, 24);
        selectionBarCard.setPadding(dp(compact ? 12 : 16), dp(12), dp(compact ? 12 : 16), dp(12));
        selectionBarCard.setVisibility(View.GONE);
        LinearLayout selTop = new LinearLayout(this);
        selTop.setOrientation(LinearLayout.HORIZONTAL);
        selTop.setGravity(Gravity.CENTER_VERTICAL);
        selectionCountText = new TextView(this);
        selectionCountText.setTextColor(theme.onPrimaryContainer);
        selectionCountText.setTypeface(Typeface.DEFAULT_BOLD);
        selectionCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        selectionCountText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        selTop.addView(selectionCountText);

        Button selAllBtn = makeBarActionBtn("Todo", theme.secondaryContainer, theme.onSecondaryContainer);
        selAllBtn.setOnClickListener(v -> toggleSelectAllDisplayed());
        selTop.addView(selAllBtn);

        Button selExit = makeBarActionBtn("Salir", theme.surfaceContainerHighest, theme.onSurface);
        ((LinearLayout.LayoutParams) selExit.getLayoutParams()).rightMargin = 0;
        selExit.setOnClickListener(v -> {
            selectionMode = false;
            selectedPaths.clear();
            refreshFilesList();
        });
        selTop.addView(selExit);
        selectionBarCard.addView(selTop);

        HorizontalScrollView selHsv = new HorizontalScrollView(this);
        selHsv.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams shLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shLp.topMargin = dp(10);
        selHsv.setLayoutParams(shLp);
        LinearLayout selRow = new LinearLayout(this);
        selRow.setOrientation(LinearLayout.HORIZONTAL);

        Button bCopy = makeBarActionBtn("Copiar", theme.secondaryContainer, theme.onSecondaryContainer);
        bCopy.setOnClickListener(v -> stageClipboardFromSelection(false));
        selRow.addView(bCopy);

        Button bCut = makeBarActionBtn("Cortar", theme.secondaryContainer, theme.onSecondaryContainer);
        bCut.setOnClickListener(v -> stageClipboardFromSelection(true));
        selRow.addView(bCut);

        Button bZip = makeBarActionBtn("ZIP", theme.primary, theme.onPrimary);
        bZip.setOnClickListener(v -> packSelectionArchive(false));
        selRow.addView(bZip);

        Button bTar = makeBarActionBtn("TAR.GZ", theme.primary, theme.onPrimary);
        bTar.setOnClickListener(v -> packSelectionArchive(true));
        selRow.addView(bTar);

        Button bDel = makeBarActionBtn("Eliminar", theme.errorContainer, theme.onErrorContainer);
        bDel.setOnClickListener(v -> deleteSelectionBatch());
        selRow.addView(bDel);

        selHsv.addView(selRow);
        selectionBarCard.addView(selHsv);

        // Clipboard Paste Bar (hidden unless clipboardPaths non-empty)
        clipboardBarCard = createExpressiveCard(parent, theme.tertiaryContainer, 24);
        clipboardBarCard.setPadding(dp(compact ? 12 : 16), dp(12), dp(compact ? 12 : 16), dp(12));
        clipboardBarCard.setVisibility(View.GONE);
        LinearLayout clipRow = new LinearLayout(this);
        clipRow.setOrientation(LinearLayout.HORIZONTAL);
        clipRow.setGravity(Gravity.CENTER_VERTICAL);
        clipboardSummaryText = new TextView(this);
        clipboardSummaryText.setTextColor(theme.onTertiaryContainer);
        clipboardSummaryText.setTypeface(Typeface.DEFAULT_BOLD);
        clipboardSummaryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        clipboardSummaryText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        clipRow.addView(clipboardSummaryText);

        Button pasteBtn = makeBarActionBtn("Pegar", theme.primary, theme.onPrimary);
        pasteBtn.setOnClickListener(v -> pasteClipboardHere());
        clipRow.addView(pasteBtn);

        Button clearClipBtn = makeBarActionBtn("✕", theme.surfaceContainerHigh, theme.onSurface);
        ((LinearLayout.LayoutParams) clearClipBtn.getLayoutParams()).rightMargin = 0;
        clearClipBtn.setOnClickListener(v -> {
            clipboardPaths.clear();
            refreshFilesList();
        });
        clipRow.addView(clearClipBtn);
        clipboardBarCard.addView(clipRow);

        // Spacious Files List Card
        LinearLayout listCard = createExpressiveCard(parent, theme.surfaceContainerLow, 26);
        listCard.setPadding(dp(compact ? 10 : 18), dp(14), dp(compact ? 10 : 18), dp(14));
        filesSummaryText = new TextView(this);
        filesSummaryText.setTextColor(theme.onSurfaceVariant);
        filesSummaryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        filesSummaryText.setPadding(dp(4), 0, dp(4), dp(10));
        listCard.addView(filesSummaryText);

        filesListContainer = new LinearLayout(this);
        filesListContainer.setOrientation(LinearLayout.VERTICAL);
        listCard.addView(filesListContainer);

        rebuildExplorerStrips();
    }

    private void toggleSearchPanel() {
        searchExpanded = !searchExpanded;
        if (searchBoxWrap != null) {
            searchBoxWrap.setVisibility(searchExpanded ? View.VISIBLE : View.GONE);
        }
        if (searchExpanded && searchEt != null) {
            searchEt.requestFocus();
        }
        refreshFilesList();
    }

    private void packCurrentFolderPrompt() {
        if (currentBrowseDir == null || isAtCurrentVolumeRoot(currentBrowseDir)) {
            Toast.makeText(this, "Abre una subcarpeta para empaquetarla", Toast.LENGTH_SHORT).show();
        } else {
            showPackFormatDialog(Collections.singletonList(currentBrowseDir), currentBrowseDir.getName());
        }
    }

    private void showSortDialog() {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, "Ordenar elementos", "Elige cómo ordenar carpetas y archivos.");
        String[] labels = new String[]{"Nombre (A-Z)", "Fecha (más recientes)", "Tamaño (mayor primero)", "Tipo / Extensión"};
        String[] shortLabels = new String[]{"Nombre", "Fecha", "Tamaño", "Tipo"};
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            boolean active = (fileSortMode == idx);
            addModalOption(box, d, labels[i], R.drawable.ic_m3_tune,
                    active ? theme.primaryContainer : theme.surfaceContainerHighest,
                    active ? theme.onPrimaryContainer : theme.onSurface,
                    () -> {
                        fileSortMode = idx;
                        if (filesSortBtn instanceof Button) {
                            ((Button) filesSortBtn).setText("Orden: " + shortLabels[idx]);
                        }
                        refreshFilesList();
                    });
        }
        Button cancelB = createIconPillButton("Cerrar", 0, Color.TRANSPARENT, theme.onSurfaceVariant);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        cancelB.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        cancelB.setOnClickListener(v -> d.dismiss());
        box.addView(cancelB);
        d.show();
    }

    private void showCreateActionsSheet() {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, "Acciones en carpeta", currentRelDisplayPath());
        addModalOption(box, d, "Nueva carpeta", R.drawable.ic_m3_folder, theme.primaryContainer, theme.onPrimaryContainer,
                this::showCreateFolderDialog);
        addModalOption(box, d, "Nuevo archivo de texto", R.drawable.ic_m3_storage, theme.secondaryContainer, theme.onSecondaryContainer,
                this::showCreateFileDialog);
        if (currentBrowseDir != null && !isAtCurrentVolumeRoot(currentBrowseDir)) {
            addModalOption(box, d, "Empaquetar carpeta (ZIP / TAR.GZ)", R.drawable.ic_m3_zip, theme.tertiaryContainer, theme.onTertiaryContainer,
                    this::packCurrentFolderPrompt);
            addModalOption(box, d, "Fijar en marcadores", R.drawable.ic_m3_menu, theme.surfaceContainerHighest, theme.onSurface,
                    () -> pinFolder(currentBrowseDir));
        }
        Button cancelB = createIconPillButton("Cerrar", 0, Color.TRANSPARENT, theme.onSurfaceVariant);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        cancelB.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        cancelB.setOnClickListener(v -> d.dismiss());
        box.addView(cancelB);
        d.show();
    }

    private void showBookmarksLibrarySheet() {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, "Marcadores y medios", "Unidades externas y accesos MiXplorer");

        TextView vHead = createSmallLabel("UNIDADES Y MEDIOS EXTERNOS");
        vHead.setPadding(0, 0, 0, dp(6));
        box.addView(vHead);

        List<StorageVolumes.VolumeInfo> vols = StorageVolumes.listMounted(this);
        File activeVolRoot = resolveActiveVolumeRoot(currentBrowseDir);
        boolean hasExternal = false;
        for (final StorageVolumes.VolumeInfo vol : vols) {
            if (!vol.id.isEmpty()) hasExternal = true;
            boolean isCurrentVol = activeVolRoot != null && activeVolRoot.equals(vol.rootDir);
            String prefix = StorageVolumes.KIND_USB.equals(vol.kind)
                    ? "USB: "
                    : (StorageVolumes.KIND_SD.equals(vol.kind) ? "SD: " : "");
            String freeHint = vol.freeBytes > 0L ? (" · " + TransferStats.formatBytes(vol.freeBytes) + " libres") : "";
            addModalOption(box, d, prefix + vol.label + freeHint, R.drawable.ic_m3_storage,
                    isCurrentVol ? theme.primaryContainer : (vol.id.isEmpty() ? theme.surfaceContainerHighest : theme.tertiaryContainer),
                    isCurrentVol ? theme.onPrimaryContainer : (vol.id.isEmpty() ? theme.onSurface : theme.onTertiaryContainer),
                    () -> navigateToDir(vol.rootDir));
        }
        if (!hasExternal) {
            TextView noExt = new TextView(this);
            noExt.setText("No hay tarjetas SD ni unidades USB OTG / USB-C conectadas ahora mismo.");
            noExt.setTextColor(theme.onSurfaceVariant);
            noExt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            noExt.setPadding(dp(4), 0, dp(4), dp(8));
            box.addView(noExt);
        }

        TextView bHead = createSmallLabel("ACCESOS RÁPIDOS");
        bHead.setPadding(0, dp(6), 0, dp(6));
        box.addView(bHead);

        String[][] shortcuts = new String[][]{
                {"Almacenamiento raíz", ""},
                {"Cámara (DCIM/Camera)", "DCIM/Camera"},
                {"Imágenes (Pictures)", "Pictures"},
                {"Descargas (Download)", "Download"},
                {"Documentos (Documents)", "Documents"}
        };
        for (final String[] sc : shortcuts) {
            addModalOption(box, d, sc[0], R.drawable.ic_m3_folder, theme.surfaceContainerHighest, theme.onSurface, () -> {
                File target = sc[1].isEmpty() ? storageRoot : new File(storageRoot, sc[1]);
                if (!target.exists() && !sc[1].isEmpty()) target = storageRoot;
                navigateToDir(target);
            });
        }

        if (currentBrowseDir != null && !isAtCurrentVolumeRoot(currentBrowseDir)) {
            boolean alreadyPinned = pinnedPaths.contains(currentBrowseDir.getAbsolutePath());
            addModalOption(box, d,
                    alreadyPinned ? ("Quitar marcador: " + currentBrowseDir.getName()) : ("★ Fijar " + currentBrowseDir.getName()),
                    R.drawable.ic_m3_add, theme.secondaryContainer, theme.onSecondaryContainer, () -> {
                        if (alreadyPinned) {
                            pinnedPaths.remove(currentBrowseDir.getAbsolutePath());
                            savePinnedPaths();
                            Toast.makeText(this, "Marcador quitado", Toast.LENGTH_SHORT).show();
                        } else {
                            pinFolder(currentBrowseDir);
                        }
                    });
        }

        for (final String pPath : new ArrayList<>(pinnedPaths)) {
            final File pDir = new File(pPath);
            if (!pDir.isDirectory()) continue;
            addModalOption(box, d, "★ " + pDir.getName(), R.drawable.ic_m3_folder, theme.primaryContainer, theme.onPrimaryContainer,
                    () -> navigateToDir(pDir));
        }

        TextView cHead = createSmallLabel("BIBLIOTECAS POR TIPO");
        cHead.setPadding(0, dp(8), 0, dp(6));
        box.addView(cHead);

        String[][] cats = new String[][]{
                {"Vista de carpeta actual", ""},
                {"Imágenes", "image"},
                {"Vídeos", "video"},
                {"Audio y música", "audio"},
                {"Documentos y texto", "doc"},
                {"Archivos ZIP / TAR.GZ / APK", "archive"},
                {"Recientes (últimos 7 días)", "recent"}
        };
        for (final String[] cat : cats) {
            final String catVal = cat[1].isEmpty() ? null : cat[1];
            boolean isAct = (catVal == null && activeCategory == null) || (catVal != null && catVal.equals(activeCategory));
            addModalOption(box, d, cat[0], catVal == null ? R.drawable.ic_m3_folder : R.drawable.ic_m3_storage,
                    isAct ? theme.primary : theme.surfaceContainerHighest,
                    isAct ? theme.onPrimary : theme.onSurface,
                    () -> {
                        activeCategory = catVal;
                        deepSearchMode = false;
                        refreshFilesList();
                    });
        }

        Button cancelB = createIconPillButton("Cerrar", 0, Color.TRANSPARENT, theme.onSurfaceVariant);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        cancelB.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        cancelB.setOnClickListener(v -> d.dismiss());
        box.addView(cancelB);
        d.show();
    }

    private Button makeSmallChip(String label, int iconRes) {
        Button b = createIconPillButton(label, iconRes, theme.surfaceContainerHigh, theme.onSurface);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        b.setPadding(dp(11), 0, dp(13), 0);
        if (iconRes > 0) setButtonIconTint(b, iconRes, theme.primary, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36));
        lp.rightMargin = dp(6);
        b.setLayoutParams(lp);
        return b;
    }

    private Button makeBarActionBtn(String label, int bg, int fg) {
        Button b = createIconPillButton(label, 0, bg, fg);
        b.setCompoundDrawablesRelative(null, null, null, null);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        b.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
        lp.rightMargin = dp(6);
        b.setLayoutParams(lp);
        return b;
    }

    private void loadPinnedPaths() {
        pinnedPaths.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_EXPLORER, MODE_PRIVATE);
        String raw = prefs.getString(KEY_PINNED_DIRS, "");
        if (raw != null && !raw.isEmpty()) {
            for (String p : raw.split("\\|")) {
                if (!p.isEmpty()) {
                    File f = new File(p);
                    if (f.isDirectory() && !pinnedPaths.contains(p)) {
                        pinnedPaths.add(p);
                    }
                }
            }
        }
    }

    private void savePinnedPaths() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pinnedPaths.size(); i++) {
            if (i > 0) sb.append('|');
            sb.append(pinnedPaths.get(i));
        }
        getSharedPreferences(PREFS_EXPLORER, MODE_PRIVATE).edit().putString(KEY_PINNED_DIRS, sb.toString()).apply();
    }

    private void pinFolder(File dir) {
        if (dir == null || !dir.isDirectory() || isAtCurrentVolumeRoot(dir)) return;
        String path = dir.getAbsolutePath();
        if (!pinnedPaths.contains(path)) {
            pinnedPaths.add(path);
            savePinnedPaths();
            rebuildExplorerStrips();
            Toast.makeText(this, "Marcador fijado: " + dir.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ya estaba fijado", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDir(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        activeCategory = null;
        deepSearchMode = false;
        currentBrowseDir = dir;
        if (activeFileTab >= 0 && activeFileTab < fileTabs.size()) {
            fileTabs.set(activeFileTab, dir);
        }
        refreshFilesList();
    }

    private void rebuildExplorerStrips() {
        if (fileTabsRow == null || breadcrumbsRow == null) return;

        // 1. Folder tabs strip (MiXplorer style)
        fileTabsRow.removeAllViews();
        for (int i = 0; i < fileTabs.size(); i++) {
            final int idx = i;
            File td = fileTabs.get(i);
            String label = isAtCurrentVolumeRoot(td) ? resolveActiveVolumeLabel(td) : td.getName();
            boolean isActive = (idx == activeFileTab);

            LinearLayout tabPill = new LinearLayout(this);
            tabPill.setOrientation(LinearLayout.HORIZONTAL);
            tabPill.setGravity(Gravity.CENTER_VERTICAL);
            tabPill.setBackground(rippleBg(
                    isActive ? theme.primaryContainer : theme.surfaceContainerHigh,
                    ripplePrimary, 0, dp(18)));
            tabPill.setPadding(dp(12), dp(6), dp(fileTabs.size() > 1 ? 8 : 12), dp(6));
            LinearLayout.LayoutParams tpLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
            tpLp.rightMargin = dp(6);
            tabPill.setLayoutParams(tpLp);
            tabPill.setOnClickListener(v -> {
                activeFileTab = idx;
                activeCategory = null;
                deepSearchMode = false;
                currentBrowseDir = fileTabs.get(idx);
                refreshFilesList();
            });

            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextColor(isActive ? theme.onPrimaryContainer : theme.onSurfaceVariant);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv.setSingleLine(true);
            tv.setMaxWidth(dp(120));
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tabPill.addView(tv);

            if (fileTabs.size() > 1) {
                TextView closeTv = new TextView(this);
                closeTv.setText("  ✕");
                closeTv.setTextColor(isActive ? theme.onPrimaryContainer : theme.onSurfaceVariant);
                closeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                closeTv.setOnClickListener(v -> {
                    if (fileTabs.size() <= 1) return;
                    fileTabs.remove(idx);
                    if (activeFileTab >= fileTabs.size()) activeFileTab = fileTabs.size() - 1;
                    currentBrowseDir = fileTabs.get(activeFileTab);
                    refreshFilesList();
                });
                tabPill.addView(closeTv);
            }
            fileTabsRow.addView(tabPill);
        }

        // 1b. External Mounted Drives Strip (SD card / USB OTG / USB-C) when mounted
        if (extVolumesRow != null && extVolumesScroll != null) {
            List<StorageVolumes.VolumeInfo> vols = StorageVolumes.listMounted(this);
            extVolumesRow.removeAllViews();
            if (vols.size() > 1) {
                extVolumesScroll.setVisibility(View.VISIBLE);
                File activeVolRoot = resolveActiveVolumeRoot(currentBrowseDir);
                for (final StorageVolumes.VolumeInfo vol : vols) {
                    boolean activeVol = activeVolRoot != null && activeVolRoot.equals(vol.rootDir);
                    String shortName = vol.id.isEmpty()
                            ? "Interno"
                            : ((StorageVolumes.KIND_USB.equals(vol.kind) ? "USB · " : "SD · ") + vol.label);
                    Button vb = createIconPillButton(shortName, R.drawable.ic_m3_storage,
                            activeVol ? theme.tertiaryContainer : theme.surfaceContainerHigh,
                            activeVol ? theme.onTertiaryContainer : theme.onSurfaceVariant);
                    vb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    vb.setPadding(dp(10), 0, dp(12), 0);
                    setButtonIconTint(vb, R.drawable.ic_m3_storage,
                            activeVol ? theme.onTertiaryContainer : theme.primary, 14);
                    LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, dp(32));
                    vlp.rightMargin = dp(6);
                    vb.setLayoutParams(vlp);
                    vb.setOnClickListener(v -> navigateToDir(vol.rootDir));
                    extVolumesRow.addView(vb);
                }
            } else {
                extVolumesScroll.setVisibility(View.GONE);
            }
        }

        // 2. Clickable Breadcrumbs strip (respects internal or SD/USB volume root)
        breadcrumbsRow.removeAllViews();
        if (activeCategory != null) {
            TextView badge = new TextView(this);
            badge.setText("Biblioteca: " + categoryDisplayName(activeCategory) + "  ✕");
            badge.setTextColor(theme.onPrimaryContainer);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            badge.setBackground(rippleBg(theme.primaryContainer, ripplePrimary, 0, dp(12)));
            badge.setPadding(dp(10), dp(4), dp(10), dp(4));
            badge.setOnClickListener(v -> {
                activeCategory = null;
                refreshFilesList();
            });
            breadcrumbsRow.addView(badge);
        } else {
            File volRoot = resolveActiveVolumeRoot(currentBrowseDir);
            String volLabel = resolveActiveVolumeLabel(currentBrowseDir);
            addBreadcrumbSeg(volLabel, volRoot, currentBrowseDir.equals(volRoot));
            String rootPath = (volRoot != null) ? volRoot.getAbsolutePath() : "";
            String curPath = currentBrowseDir.getAbsolutePath();
            if (!rootPath.isEmpty() && !curPath.equals(rootPath) && curPath.startsWith(rootPath)) {
                String rel = curPath.substring(rootPath.length());
                if (rel.startsWith("/")) rel = rel.substring(1);
                String[] parts = rel.split("/");
                File walk = volRoot;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].isEmpty()) continue;
                    walk = new File(walk, parts[i]);
                    TextView sep = new TextView(this);
                    sep.setText(" › ");
                    sep.setTextColor(theme.onSurfaceVariant);
                    sep.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    breadcrumbsRow.addView(sep);
                    addBreadcrumbSeg(parts[i], walk, i == parts.length - 1);
                }
            }
            if (deepSearchMode) {
                TextView dTag = new TextView(this);
                dTag.setText(" · Subcarpetas ✕");
                dTag.setTextColor(theme.primary);
                dTag.setTypeface(Typeface.DEFAULT_BOLD);
                dTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                dTag.setOnClickListener(v -> {
                    deepSearchMode = false;
                    refreshFilesList();
                });
                breadcrumbsRow.addView(dTag);
            }
        }
        if (breadcrumbsScroll != null) {
            breadcrumbsScroll.post(() -> breadcrumbsScroll.fullScroll(View.FOCUS_RIGHT));
        }

        // 3. Toolbar toggle visual states
        updateRoundButtonVisual(filesSearchBtn, R.drawable.ic_m3_search,
                searchExpanded ? theme.primaryContainer : theme.surfaceContainerHigh,
                searchExpanded ? theme.onPrimaryContainer : theme.onSurfaceVariant, 20);
        updateRoundButtonVisual(filesSelectBtn, R.drawable.ic_m3_check,
                selectionMode ? theme.primaryContainer : theme.surfaceContainerHigh,
                selectionMode ? theme.onPrimaryContainer : theme.onSurfaceVariant, 20);
        if (filesDeepSearchBtn != null) {
            filesDeepSearchBtn.setBackground(rippleBg(
                    deepSearchMode ? theme.primaryContainer : theme.surfaceContainerHigh,
                    ripplePrimary, 0, dp(999)));
            if (filesDeepSearchBtn instanceof Button) {
                ((Button) filesDeepSearchBtn).setTextColor(deepSearchMode ? theme.onPrimaryContainer : theme.onSurface);
            }
        }
    }

    private void addBreadcrumbSeg(String label, final File targetDir, boolean isLast) {
        TextView seg = new TextView(this);
        seg.setText(label);
        seg.setTextColor(isLast ? theme.onSurface : theme.primary);
        seg.setTypeface(isLast ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        seg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        seg.setBackground(rippleBg(Color.TRANSPARENT, ripplePrimary, 0, dp(10)));
        seg.setPadding(dp(4), dp(2), dp(4), dp(2));
        seg.setOnClickListener(v -> navigateToDir(targetDir));
        breadcrumbsRow.addView(seg);
    }

    private static String categoryDisplayName(String cat) {
        if ("image".equals(cat)) return "Imágenes";
        if ("video".equals(cat)) return "Vídeos";
        if ("audio".equals(cat)) return "Audio";
        if ("doc".equals(cat)) return "Documentos";
        if ("archive".equals(cat)) return "Comprimidos";
        if ("recent".equals(cat)) return "Recientes (7d)";
        return cat;
    }

    private final List<File> displayedFiles = new ArrayList<>();

    private void refreshFilesList() {
        if (filesListContainer == null || storageRoot == null) return;
        if (currentBrowseDir == null || !currentBrowseDir.isDirectory()) {
            currentBrowseDir = storageRoot;
        }

        if (searchBoxWrap != null) {
            boolean showSearch = searchExpanded || deepSearchMode || !fileSearchQuery.isEmpty();
            searchBoxWrap.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        }

        rebuildExplorerStrips();

        String rel = currentRelDisplayPath();
        filesPathBadge.setText(activeCategory != null ? ("Filtro: " + activeCategory) : rel);

        boolean isAtRoot = isAtCurrentVolumeRoot(currentBrowseDir);
        boolean inFlatDir = (activeCategory == null && !deepSearchMode);
        if (zipCurrentDirBtn != null) {
            zipCurrentDirBtn.setEnabled(inFlatDir && !isAtRoot);
            zipCurrentDirBtn.setAlpha((inFlatDir && !isAtRoot) ? 1f : 0.42f);
        }

        selectionBarCard.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        selectionCountText.setText(selectedPaths.size() + (selectedPaths.size() == 1 ? " seleccionado" : " seleccionados"));

        if (!clipboardPaths.isEmpty()) {
            clipboardBarCard.setVisibility(View.VISIBLE);
            clipboardSummaryText.setText((clipboardCut ? "Mover " : "Copiar ")
                    + clipboardPaths.size() + " → "
                    + (isAtRoot ? resolveActiveVolumeLabel(currentBrowseDir) : currentBrowseDir.getName()));
        } else {
            clipboardBarCard.setVisibility(View.GONE);
        }

        if (activeCategory != null || deepSearchMode) {
            final int tok = ++searchRequestToken;
            final String q = fileSearchQuery;
            final String cat = activeCategory;
            final File activeRoot = resolveActiveVolumeRoot(currentBrowseDir);
            final File startDir = (cat != null) ? (activeRoot != null ? activeRoot : storageRoot) : currentBrowseDir;
            filesSummaryText.setText(cat != null ? "Escaneando biblioteca..." : "Buscando en subcarpetas...");
            filesListContainer.removeAllViews();
            fileBgExecutor.execute(() -> {
                final List<File> found = FileOps.searchRecursive(startDir, q, cat, 320);
                sortFileList(found);
                uiHandler.post(() -> {
                    if (tok != searchRequestToken) return;
                    renderFileEntries(found, true);
                });
            });
            return;
        }

        searchRequestToken++;
        File[] raw = currentBrowseDir.listFiles();
        List<File> filtered = new ArrayList<>();
        if (raw != null) {
            for (File f : raw) {
                if (FolderZipper.shouldSkipName(f.getName())) continue;
                if (!fileSearchQuery.isEmpty() && !f.getName().toLowerCase(Locale.US).contains(fileSearchQuery)) continue;
                filtered.add(f);
            }
        }
        sortFileList(filtered);
        renderFileEntries(filtered, false);
    }

    private void sortFileList(List<File> list) {
        list.sort(new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                if (fileSortMode == 1) {
                    return Long.compare(b.lastModified(), a.lastModified());
                } else if (fileSortMode == 2) {
                    return Long.compare(b.length(), a.length());
                } else if (fileSortMode == 3) {
                    int ec = getExt(a.getName()).compareToIgnoreCase(getExt(b.getName()));
                    if (ec != 0) return ec;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
    }

    private static String getExt(String name) {
        int dot = name.lastIndexOf('.');
        return (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
    }

    private void renderFileEntries(List<File> filtered, boolean showRelParent) {
        displayedFiles.clear();
        int dirCount = 0;
        int fileCount = 0;
        long totalBytes = 0L;
        for (File f : filtered) {
            if (f.isDirectory()) dirCount++;
            else {
                fileCount++;
                totalBytes += f.length();
            }
        }

        filesSummaryText.setText(dirCount + (dirCount == 1 ? " carpeta" : " carpetas")
                + " · " + fileCount + (fileCount == 1 ? " archivo" : " archivos")
                + " (" + TransferStats.formatBytes(totalBytes) + ")");

        filesListContainer.removeAllViews();

        boolean isAtRoot = isAtCurrentVolumeRoot(currentBrowseDir);
        if (!showRelParent && !isAtRoot && currentBrowseDir.getParentFile() != null) {
            final File parentDir = currentBrowseDir.getParentFile();
            LinearLayout upRow = new LinearLayout(this);
            upRow.setOrientation(LinearLayout.HORIZONTAL);
            upRow.setGravity(Gravity.CENTER_VERTICAL);
            upRow.setBackground(rippleBg(theme.surfaceContainerHighest, ripplePrimary, 0, dp(20)));
            upRow.setPadding(dp(10), dp(10), dp(12), dp(10));
            LinearLayout.LayoutParams uLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            uLp.bottomMargin = dp(8);
            upRow.setLayoutParams(uLp);
            attachSpringTouch(upRow);

            FrameLayout upBadge = createRoundIconButton(
                    R.drawable.ic_m3_folder, theme.secondaryContainer, theme.onSecondaryContainer, 36, "Subir nivel");
            upBadge.setBackground(roundedRect(theme.secondaryContainer, 0, dp(13)));
            LinearLayout.LayoutParams ubLp = new LinearLayout.LayoutParams(dp(36), dp(36));
            ubLp.rightMargin = dp(10);
            upBadge.setLayoutParams(ubLp);
            upBadge.setOnClickListener(v -> navigateToDir(parentDir));
            upRow.addView(upBadge);

            TextView upTv = new TextView(this);
            upTv.setText("..  (Subir nivel)");
            upTv.setTextColor(theme.onSurface);
            upTv.setTypeface(Typeface.DEFAULT_BOLD);
            upTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            upRow.addView(upTv);

            upRow.setOnClickListener(v -> navigateToDir(parentDir));
            filesListContainer.addView(upRow);
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(fileSearchQuery.isEmpty() ? "No hay elementos aquí." : "No hay coincidencias.");
            empty.setTextColor(theme.onSurfaceVariant);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            empty.setPadding(dp(6), dp(10), dp(6), dp(10));
            filesListContainer.addView(empty);
            return;
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.US);
        int limit = Math.min(250, filtered.size());
        final boolean compact = isCompactScreen();
        for (int i = 0; i < limit; i++) {
            File f = filtered.get(i);
            displayedFiles.add(f);
            if (skin.groupedListDividers && i > 0) {
                filesListContainer.addView(OsSkinRenderer.createGroupedDivider(this, theme, 52));
            }
            filesListContainer.addView(buildFileExplorerRow(f, df, showRelParent, compact));
        }
        if (filtered.size() > limit) {
            TextView note = new TextView(this);
            note.setText("Mostrando " + limit + " de " + filtered.size() + " elementos. Usa el buscador para filtrar.");
            note.setTextColor(theme.onSurfaceVariant);
            note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            note.setPadding(dp(6), dp(6), dp(6), dp(4));
            filesListContainer.addView(note);
        }
    }

    private static boolean isArchiveName(String lower) {
        return lower.endsWith(".zip") || lower.endsWith(".tar.gz") || lower.endsWith(".tgz");
    }

    private static boolean isImageName(String lower) {
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".webp")
                || lower.endsWith(".gif") || lower.endsWith(".bmp");
    }

    private static boolean isMediaName(String lower) {
        return isImageName(lower) || lower.endsWith(".heic")
                || lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv")
                || lower.endsWith(".mp3") || lower.endsWith(".flac") || lower.endsWith(".wav")
                || lower.endsWith(".ogg") || lower.endsWith(".m4a");
    }

    private static boolean isTextName(String lower) {
        return lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".json")
                || lower.endsWith(".xml") || lower.endsWith(".csv") || lower.endsWith(".log")
                || lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".css")
                || lower.endsWith(".js") || lower.endsWith(".java") || lower.endsWith(".py")
                || lower.endsWith(".sh") || lower.endsWith(".yml") || lower.endsWith(".yaml")
                || lower.endsWith(".ini") || lower.endsWith(".conf") || lower.endsWith(".properties");
    }

    private View buildFileExplorerRow(final File item, SimpleDateFormat df, boolean showRelParent, boolean compact) {
        final boolean isDir = item.isDirectory();
        final String lower = item.getName().toLowerCase(Locale.US);
        final boolean isArchive = !isDir && isArchiveName(lower);
        final boolean isMedia = !isDir && isMediaName(lower);
        final String absPath = item.getAbsolutePath();
        final boolean isSelected = selectedPaths.contains(absPath);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        boolean rowSelected = selectionMode && isSelected;
        int bgColor = rowSelected
                ? theme.primaryContainer
                : (skin.groupedListDividers ? Color.TRANSPARENT : theme.surfaceContainerHigh);
        int rowRadius = skin.groupedListDividers ? dp(10) : dp(skin.innerRadiusDp);
        row.setBackground(rippleBg(bgColor, ripplePrimary, 0, rowRadius));
        row.setPadding(dp(skin.groupedListDividers ? 6 : 10), dp(10), dp(skin.groupedListDividers ? 4 : 8), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = skin.groupedListDividers ? 0 : dp(8);
        row.setLayoutParams(lp);
        attachSpringTouch(row);

        // Leading OEM-specific folder/file badge
        OsSkinRenderer.BadgeSpec bSpec = OsSkinRenderer.resolveExplorerBadge(
                skin, theme, isDir, isArchive, isMedia, rowSelected);
        int badgeIcon;
        if (rowSelected) {
            badgeIcon = R.drawable.ic_m3_check;
        } else if (isDir) {
            badgeIcon = R.drawable.ic_m3_folder;
        } else if (isArchive) {
            badgeIcon = R.drawable.ic_m3_zip;
        } else if (isMedia) {
            badgeIcon = R.drawable.ic_m3_photo;
        } else {
            badgeIcon = R.drawable.ic_m3_storage;
        }

        FrameLayout leadBadge = new FrameLayout(this);
        leadBadge.setBackground(rippleBg(bSpec.bgColor, ripplePrimary, bSpec.strokeColor, dp(bSpec.radiusDp)));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(36), dp(36));
        blp.rightMargin = dp(10);
        leadBadge.setLayoutParams(blp);
        ImageView leadIv = new ImageView(this);
        leadIv.setImageDrawable(tintedIcon(badgeIcon, bSpec.fgColor, 19));
        leadBadge.addView(leadIv, new FrameLayout.LayoutParams(dp(19), dp(19), Gravity.CENTER));
        leadBadge.setOnClickListener(v -> {
            selectionMode = true;
            if (selectedPaths.contains(absPath)) selectedPaths.remove(absPath);
            else selectedPaths.add(absPath);
            if (selectedPaths.isEmpty()) selectionMode = false;
            refreshFilesList();
        });
        row.addView(leadBadge);

        // Center column — maximized space with up to 2 lines for readable folder/file names
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tLp.rightMargin = dp(4);
        textCol.setLayoutParams(tLp);

        TextView nameTv = new TextView(this);
        nameTv.setText(item.getName());
        nameTv.setTextColor((selectionMode && isSelected) ? theme.onPrimaryContainer : theme.onSurface);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        nameTv.setMaxLines(2);
        nameTv.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(nameTv);

        String subInfo;
        if (isDir) {
            int cnt = FolderZipper.countVisibleChildren(item);
            subInfo = (cnt == 0 ? "Vacía" : (cnt + " elem.")) + " · " + df.format(new Date(item.lastModified()));
        } else {
            subInfo = TransferStats.formatBytes(item.length()) + " · " + df.format(new Date(item.lastModified()));
        }
        if (showRelParent && item.getParentFile() != null && storageRoot != null) {
            String relP = item.getParentFile().getAbsolutePath().replace(storageRoot.getAbsolutePath(), "");
            if (relP.isEmpty()) relP = "/";
            subInfo += " · " + relP;
        }
        TextView subTv = new TextView(this);
        subTv.setText(subInfo);
        subTv.setTextColor((selectionMode && isSelected) ? theme.onPrimaryContainer : theme.onSurfaceVariant);
        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        subTv.setSingleLine(true);
        subTv.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(subTv);
        row.addView(textCol);

        // Trailing action buttons — round icon-only on narrow screens so names breathe cleanly
        if (!selectionMode) {
            if (isDir) {
                if (compact) {
                    FrameLayout zipCircle = createRoundIconButton(
                            R.drawable.ic_m3_zip, theme.primaryContainer, theme.onPrimaryContainer, 34, "Comprimir ZIP");
                    LinearLayout.LayoutParams zlp = new LinearLayout.LayoutParams(dp(34), dp(34));
                    zlp.rightMargin = dp(4);
                    zipCircle.setLayoutParams(zlp);
                    zipCircle.setOnClickListener(v -> compressFolderToZip(item));
                    zipCircle.setOnLongClickListener(v -> {
                        showPackFormatDialog(Collections.singletonList(item), item.getName());
                        return true;
                    });
                    row.addView(zipCircle);
                } else {
                    Button zipBtn = createIconPillButton("ZIP", R.drawable.ic_m3_zip, theme.primaryContainer, theme.onPrimaryContainer);
                    zipBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    zipBtn.setPadding(dp(10), 0, dp(12), 0);
                    zipBtn.setCompoundDrawablePadding(dp(4));
                    setButtonIconTint(zipBtn, R.drawable.ic_m3_zip, theme.onPrimaryContainer, 14);
                    LinearLayout.LayoutParams zpLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
                    zpLp.rightMargin = dp(6);
                    zipBtn.setLayoutParams(zpLp);
                    zipBtn.setOnClickListener(v -> compressFolderToZip(item));
                    row.addView(zipBtn);
                }
            } else if (isArchive) {
                if (compact) {
                    FrameLayout extCircle = createRoundIconButton(
                            R.drawable.ic_m3_download, theme.secondaryContainer, theme.onSecondaryContainer, 34, "Extraer");
                    LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(dp(34), dp(34));
                    elp.rightMargin = dp(4);
                    extCircle.setLayoutParams(elp);
                    extCircle.setOnClickListener(v -> extractArchiveAsync(item));
                    row.addView(extCircle);
                } else {
                    Button extBtn = createIconPillButton("Extraer", R.drawable.ic_m3_download, theme.secondaryContainer, theme.onSecondaryContainer);
                    extBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    extBtn.setPadding(dp(10), 0, dp(12), 0);
                    extBtn.setCompoundDrawablePadding(dp(4));
                    setButtonIconTint(extBtn, R.drawable.ic_m3_download, theme.onSecondaryContainer, 14);
                    LinearLayout.LayoutParams epLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(34));
                    epLp.rightMargin = dp(6);
                    extBtn.setLayoutParams(epLp);
                    extBtn.setOnClickListener(v -> extractArchiveAsync(item));
                    row.addView(extBtn);
                }
            }

            FrameLayout moreCircle = createRoundIconButton(
                    R.drawable.ic_m3_more,
                    skin.groupedListDividers ? Color.TRANSPARENT : theme.surfaceContainerHighest,
                    theme.onSurfaceVariant, 34, "Más opciones");
            moreCircle.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
            moreCircle.setOnClickListener(v -> showItemOptionsDialog(item));
            row.addView(moreCircle);
            if (isDir && skin.showRowChevrons) {
                row.addView(OsSkinRenderer.createRowChevron(this, theme));
            }
        }

        row.setOnClickListener(v -> {
            if (selectionMode) {
                if (selectedPaths.contains(absPath)) selectedPaths.remove(absPath);
                else selectedPaths.add(absPath);
                refreshFilesList();
            } else if (isDir) {
                navigateToDir(item);
            } else if (isArchive) {
                openArchiveInspectorDialog(item);
            } else if (isImageName(lower)) {
                openImagePreviewDialog(item);
            } else if (isTextName(lower)) {
                openTextEditorDialog(item);
            } else {
                showItemOptionsDialog(item);
            }
        });

        row.setOnLongClickListener(v -> {
            selectionMode = true;
            selectedPaths.add(absPath);
            refreshFilesList();
            return true;
        });

        return row;
    }

    private void toggleSelectAllDisplayed() {
        boolean allSelected = !displayedFiles.isEmpty();
        for (File f : displayedFiles) {
            if (!selectedPaths.contains(f.getAbsolutePath())) {
                allSelected = false;
                break;
            }
        }
        if (allSelected) {
            selectedPaths.clear();
        } else {
            for (File f : displayedFiles) {
                selectedPaths.add(f.getAbsolutePath());
            }
        }
        refreshFilesList();
    }

    private List<File> collectSelectedFiles() {
        List<File> list = new ArrayList<>();
        for (String p : selectedPaths) {
            File f = new File(p);
            if (f.exists() && !FolderZipper.shouldSkipName(f.getName())) {
                list.add(f);
            }
        }
        return list;
    }

    private void stageClipboardFromSelection(boolean cut) {
        List<File> items = collectSelectedFiles();
        if (items.isEmpty()) {
            Toast.makeText(this, "Selecciona primero uno o más elementos", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboardPaths.clear();
        for (File f : items) clipboardPaths.add(f.getAbsolutePath());
        clipboardCut = cut;
        selectionMode = false;
        selectedPaths.clear();
        refreshFilesList();
        Toast.makeText(this, (cut ? "Cortados: " : "Copiados: ") + clipboardPaths.size(), Toast.LENGTH_SHORT).show();
    }

    private void stageSingleClipboard(File item, boolean cut) {
        clipboardPaths.clear();
        clipboardPaths.add(item.getAbsolutePath());
        clipboardCut = cut;
        refreshFilesList();
        Toast.makeText(this, (cut ? "Cortado: " : "Copiado: ") + item.getName(), Toast.LENGTH_SHORT).show();
    }

    private void applyProgressCardTheme(boolean isDelete) {
        if (zipStatusCard == null) return;
        int bg = isDelete ? theme.errorContainer : theme.primaryContainer;
        int fg = isDelete ? theme.onErrorContainer : theme.onPrimaryContainer;
        zipStatusCard.setBackground(roundedRect(bg, 0, dp(28)));
        zipStatusTitle.setTextColor(fg);
        zipStatusDetail.setTextColor(ThemeTokens.withAlpha(fg, 220));
    }

    private void runMonitoredFileOp(final String opType, final List<File> sources,
                                    final File destDir, final Runnable onCompleteOk) {
        if (sources == null || sources.isEmpty()) return;
        if (activeZipCancel != null || activeUiFileOp != null) {
            Toast.makeText(this, "Ya hay una operación de archivo en curso", Toast.LENGTH_SHORT).show();
            return;
        }
        final boolean isDel = FileOpMonitor.OP_DELETE.equals(opType);
        final boolean isMove = FileOpMonitor.OP_MOVE.equals(opType);
        final String title = (sources.size() == 1) ? sources.get(0).getName() : (sources.size() + " elementos");
        final String verb = isDel ? "Eliminando " : (isMove ? "Moviendo " : "Copiando ");
        final String destLabel = (destDir != null) ? ("→ " + resolveActiveVolumeLabel(destDir)) : currentRelDisplayPath();

        final boolean showStorageCard = (activeTabIndex == 2 && isDel && storageDeleteStatusCard != null);
        final View activeStatusCard = showStorageCard ? storageDeleteStatusCard : zipStatusCard;
        final TextView activeTitleView = showStorageCard ? storageDeleteStatusTitle : zipStatusTitle;
        final TextView activeDetailView = showStorageCard ? storageDeleteStatusDetail : zipStatusDetail;
        final ProgressBar activeBarView = showStorageCard ? storageDeleteProgressBar : zipStatusBar;

        if (!showStorageCard) {
            applyProgressCardTheme(isDel);
        }
        activeStatusCard.setVisibility(View.VISIBLE);
        activeTitleView.setText(verb + title);
        activeBarView.setIndeterminate(true);
        activeDetailView.setText("Calculando elementos...");
        if (mainScrollView != null) mainScrollView.smoothScrollTo(0, 0);

        fileBgExecutor.execute(() -> {
            long[] totals = FileOps.countItemsAndBytes(sources);
            final FileOpMonitor monitor = FileOpMonitor.getInstance();
            final FileOpMonitor.ActiveFileOp op = monitor.beginOp(
                    opType, title, destLabel, (int) totals[1], totals[0]);
            activeUiFileOp = op;

            final long[] lastUiMs = new long[]{0L};
            boolean ok = false;
            int doneCount = 0;
            String err = null;
            try {
                FileOps.ProgressListener cb = (name, iDelta, bDelta) -> {
                    monitor.updateProgress(op, name, iDelta, bDelta);
                    long now = SystemClock.uptimeMillis();
                    if (now - lastUiMs[0] >= 120L) {
                        lastUiMs[0] = now;
                        final int pct = op.computeProgressPercent();
                        final long doneB = op.bytesDone.get();
                        final long totB = op.totalBytes;
                        final int doneI = op.itemsDone.get();
                        final int totI = op.totalItems;
                        final String cur = op.currentItem;
                        uiHandler.post(() -> {
                            if (pct >= 0) {
                                activeBarView.setIndeterminate(false);
                                activeBarView.setProgress(pct);
                            } else {
                                activeBarView.setIndeterminate(true);
                            }
                            String prefix = isDel ? "Eliminando: " : "";
                            String stat = (totB > 0L)
                                    ? (TransferStats.formatBytes(doneB) + " / " + TransferStats.formatBytes(totB) + " (" + Math.max(0, pct) + "%)")
                                    : (doneI + "/" + totI + " elem.");
                            activeDetailView.setText(prefix + cur + " · " + stat);
                        });
                    }
                };
                for (File src : sources) {
                    if (isDel) {
                        if (FileOps.deleteRecursiveWithProgress(src, op.cancelled, cb) > 0) doneCount++;
                    } else if (isMove) {
                        doneCount += FileOps.moveRecursive(src, destDir, op.cancelled, cb);
                    } else {
                        doneCount += FileOps.copyRecursive(src, destDir, op.cancelled, cb);
                    }
                }
                ok = !op.cancelled.get();
            } catch (Exception e) {
                err = e.getMessage();
            } finally {
                monitor.endOp(op, ok, ok && (totals[1] > 1 || totals[0] >= 256L * 1024L) ? (verb + title) : null);
                activeUiFileOp = null;
                final boolean finalOk = ok;
                final int finalCount = doneCount;
                final String finalErr = err;
                if (finalOk && destDir != null) {
                    try {
                        MediaScannerConnection.scanFile(this, new String[]{destDir.getAbsolutePath()}, null, null);
                    } catch (Exception ignored) {
                    }
                }
                uiHandler.post(() -> {
                    activeStatusCard.setVisibility(View.GONE);
                    if (!showStorageCard) applyProgressCardTheme(false);
                    if (finalOk) {
                        if (onCompleteOk != null) onCompleteOk.run();
                        String msg = isDel
                                ? ("Eliminados: " + finalCount)
                                : ((isMove ? "Movido(s) " : "Copiado(s) ") + finalCount + " archivo(s)");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    } else if (op.cancelled.get()) {
                        Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + (finalErr != null ? finalErr : "Operación fallida"), Toast.LENGTH_LONG).show();
                    }
                    refreshFilesList();
                });
            }
        });
    }

    private void pasteClipboardHere() {
        if (clipboardPaths.isEmpty() || currentBrowseDir == null || !currentBrowseDir.isDirectory()) return;
        final List<File> sources = new ArrayList<>();
        for (String p : clipboardPaths) {
            File f = new File(p);
            if (f.exists()) sources.add(f);
        }
        if (sources.isEmpty()) {
            clipboardPaths.clear();
            refreshFilesList();
            return;
        }
        final boolean isCut = clipboardCut;
        final File destDir = currentBrowseDir;
        runMonitoredFileOp(isCut ? FileOpMonitor.OP_MOVE : FileOpMonitor.OP_COPY, sources, destDir, () -> {
            if (isCut) clipboardPaths.clear();
        });
    }

    private void packSelectionArchive(boolean asTarGz) {
        List<File> items = collectSelectedFiles();
        if (items.isEmpty()) {
            Toast.makeText(this, "Selecciona primero los archivos o carpetas", Toast.LENGTH_SHORT).show();
            return;
        }
        File targetDir = currentBrowseDir != null ? currentBrowseDir : storageRoot;
        String baseName = (items.size() == 1) ? stripExtension(items.get(0).getName()) : ("seleccion_" + items.size());
        File dest = FolderZipper.resolveUniqueArchiveFile(targetDir, baseName, asTarGz ? ".tar.gz" : ".zip");
        selectionMode = false;
        selectedPaths.clear();
        refreshFilesList();
        compressItemsAsync(items, dest, asTarGz);
    }

    private static String stripExtension(String name) {
        if (name.endsWith(".tar.gz")) return name.substring(0, name.length() - 7);
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private void deleteSelectionBatch() {
        final List<File> items = collectSelectedFiles();
        if (items.isEmpty()) {
            Toast.makeText(this, "No hay elementos seleccionados", Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d,
                "¿Eliminar " + items.size() + " elemento(s)?",
                "Esta acción no se puede deshacer.");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button cancelB = createIconPillButton("Cancelar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        clp.rightMargin = dp(6);
        cancelB.setLayoutParams(clp);
        cancelB.setOnClickListener(v -> d.dismiss());
        row.addView(cancelB);

        Button delB = createIconPillButton("Eliminar todo", R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        dlp.leftMargin = dp(6);
        delB.setLayoutParams(dlp);
        delB.setOnClickListener(v -> {
            d.dismiss();
            runMonitoredFileOp(FileOpMonitor.OP_DELETE, items, null, () -> {
                selectionMode = false;
                selectedPaths.clear();
            });
        });
        row.addView(delB);
        box.addView(row);
        d.show();
    }

    private void showPackFormatDialog(final List<File> items, final String baseName) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, "Empaquetar " + baseName, "Elige el formato de archivo comprimido.");
        File parentDir = (items.get(0).getParentFile() != null) ? items.get(0).getParentFile() : storageRoot;

        Button zBtn = createIconPillButton("Comprimir como .ZIP", R.drawable.ic_m3_zip, theme.primary, theme.onPrimary);
        LinearLayout.LayoutParams zlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        zlp.bottomMargin = dp(8);
        zBtn.setLayoutParams(zlp);
        zBtn.setOnClickListener(v -> {
            d.dismiss();
            File dest = FolderZipper.resolveUniqueArchiveFile(parentDir, baseName, ".zip");
            compressItemsAsync(items, dest, false);
        });
        box.addView(zBtn);

        Button tBtn = createIconPillButton("Empaquetar como .TAR.GZ", R.drawable.ic_m3_zip, theme.secondaryContainer, theme.onSecondaryContainer);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        tlp.bottomMargin = dp(8);
        tBtn.setLayoutParams(tlp);
        tBtn.setOnClickListener(v -> {
            d.dismiss();
            File dest = FolderZipper.resolveUniqueArchiveFile(parentDir, baseName, ".tar.gz");
            compressItemsAsync(items, dest, true);
        });
        box.addView(tBtn);

        Button cBtn = createIconPillButton("Cancelar", 0, Color.TRANSPARENT, theme.onSurfaceVariant);
        cBtn.setCompoundDrawablesRelative(null, null, null, null);
        cBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40)));
        cBtn.setOnClickListener(v -> d.dismiss());
        box.addView(cBtn);
        d.show();
    }

    private void compressFolderToZip(final File folder) {
        if (folder == null || !folder.isDirectory()) return;
        File destZip = FolderZipper.resolveUniqueZipFile(folder);
        compressItemsAsync(Collections.singletonList(folder), destZip, false);
    }

    private void compressItemsAsync(final List<File> items, final File destArchive, final boolean asTarGz) {
        if (items == null || items.isEmpty() || destArchive == null) return;
        if (activeZipCancel != null || activeUiFileOp != null) {
            Toast.makeText(this, "Ya hay una operación de archivo en curso", Toast.LENGTH_SHORT).show();
            return;
        }
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        activeZipCancel = cancelFlag;

        applyProgressCardTheme(false);
        zipStatusBar.setIndeterminate(true);
        zipStatusCard.setVisibility(View.VISIBLE);
        zipStatusTitle.setText("Creando " + destArchive.getName());
        zipStatusDetail.setText("Preparando archivos...");
        if (mainScrollView != null) mainScrollView.smoothScrollTo(0, 0);

        final String rootPath = storageRoot.getAbsolutePath();
        final File parentFile = destArchive.getParentFile();
        String relFolder = (parentFile != null) ? parentFile.getAbsolutePath().replace(rootPath, "") : "/";
        if (relFolder.isEmpty()) relFolder = "/";
        final TransferStats.ActiveTransfer tx = stats.beginTransfer(
                destArchive.getName(), relFolder, TransferStats.DIR_DOWNLOAD,
                asTarGz ? "App TAR.GZ" : "App ZIP", -1L);
        tx.setCancelCleanup(() -> cancelFlag.set(true));

        final long[] lastUiMs = new long[]{0L};
        zipExecutor.execute(() -> {
            boolean ok = false;
            FolderZipper.ZipResult res = null;
            String errMsg = null;
            try {
                FolderZipper.ProgressCallback cb = (delta, entry) -> {
                    stats.recordBytes(tx, (int) delta);
                    long now = SystemClock.uptimeMillis();
                    if (now - lastUiMs[0] >= 150L) {
                        lastUiMs[0] = now;
                        final long written = tx.bytesTransferred.get();
                        final String eName = entry;
                        uiHandler.post(() -> zipStatusDetail.setText(
                                "Escrito: " + TransferStats.formatBytes(written) + " · " + eName));
                    }
                };
                res = asTarGz
                        ? FolderZipper.packTarGzToFile(items, destArchive, cancelFlag, cb)
                        : FolderZipper.zipItemsToFile(items, destArchive, cancelFlag, cb);
                ok = !cancelFlag.get();
            } catch (Exception e) {
                errMsg = e.getMessage();
            } finally {
                stats.endTransfer(tx, ok);
                activeZipCancel = null;
                final boolean success = ok;
                final FolderZipper.ZipResult finalRes = res;
                final String finalErr = errMsg;
                if (success) {
                    try {
                        MediaScannerConnection.scanFile(this, new String[]{destArchive.getAbsolutePath()}, null, null);
                    } catch (Exception ignored) {
                    }
                }
                uiHandler.post(() -> {
                    zipStatusCard.setVisibility(View.GONE);
                    if (success && finalRes != null) {
                        Toast.makeText(this,
                                "Guardado: " + destArchive.getName() + " (" + finalRes.filesCount + " archivos)",
                                Toast.LENGTH_LONG).show();
                        refreshFilesList();
                    } else if (cancelFlag.get()) {
                        Toast.makeText(this, "Compresión cancelada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al empaquetar" + (finalErr != null ? ": " + finalErr : ""), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void extractArchiveAsync(final File archive) {
        if (archive == null || !archive.isFile()) return;
        if (activeZipCancel != null || activeUiFileOp != null) {
            Toast.makeText(this, "Ya hay una operación de archivo en curso", Toast.LENGTH_SHORT).show();
            return;
        }
        final String lower = archive.getName().toLowerCase(Locale.US);
        final boolean isTarGz = lower.endsWith(".tar.gz") || lower.endsWith(".tgz");
        final File destDir = FolderZipper.resolveUniqueExtractDir(archive);
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        activeZipCancel = cancelFlag;

        applyProgressCardTheme(false);
        zipStatusBar.setIndeterminate(true);
        zipStatusCard.setVisibility(View.VISIBLE);
        zipStatusTitle.setText("Extrayendo " + archive.getName());
        zipStatusDetail.setText("Destino: " + destDir.getName());
        if (mainScrollView != null) mainScrollView.smoothScrollTo(0, 0);

        final String rootPath = storageRoot.getAbsolutePath();
        String relFolder = destDir.getAbsolutePath().replace(rootPath, "");
        if (relFolder.isEmpty()) relFolder = "/";
        final TransferStats.ActiveTransfer tx = stats.beginTransfer(
                archive.getName(), relFolder, TransferStats.DIR_DOWNLOAD, "App Extraer", archive.length());
        tx.setCancelCleanup(() -> cancelFlag.set(true));

        final long[] lastUiMs = new long[]{0L};
        zipExecutor.execute(() -> {
            boolean ok = false;
            FolderZipper.ZipResult res = null;
            String errMsg = null;
            try {
                FolderZipper.ProgressCallback cb = (delta, entry) -> {
                    stats.recordBytes(tx, (int) delta);
                    long now = SystemClock.uptimeMillis();
                    if (now - lastUiMs[0] >= 150L) {
                        lastUiMs[0] = now;
                        final long written = tx.bytesTransferred.get();
                        final String eName = entry;
                        uiHandler.post(() -> zipStatusDetail.setText(
                                "Extraído: " + TransferStats.formatBytes(written) + " · " + eName));
                    }
                };
                res = isTarGz
                        ? FolderZipper.extractTarGz(archive, destDir, cancelFlag, cb)
                        : FolderZipper.extractZip(archive, destDir, cancelFlag, cb);
                ok = !cancelFlag.get();
            } catch (Exception e) {
                errMsg = e.getMessage();
            } finally {
                stats.endTransfer(tx, ok);
                activeZipCancel = null;
                final boolean success = ok;
                final FolderZipper.ZipResult finalRes = res;
                final String finalErr = errMsg;
                if (success) {
                    try {
                        MediaScannerConnection.scanFile(this, new String[]{destDir.getAbsolutePath()}, null, null);
                    } catch (Exception ignored) {
                    }
                }
                uiHandler.post(() -> {
                    zipStatusCard.setVisibility(View.GONE);
                    if (success && finalRes != null) {
                        Toast.makeText(this,
                                "Extraído en " + destDir.getName() + " (" + finalRes.filesCount + " archivos)",
                                Toast.LENGTH_LONG).show();
                        refreshFilesList();
                    } else if (cancelFlag.get()) {
                        Toast.makeText(this, "Extracción cancelada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al extraer" + (finalErr != null ? ": " + finalErr : ""), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showCreateFolderDialog() {
        showTextPromptDialog(
                "Nueva carpeta",
                "Se creará dentro de " + currentRelDisplayPath(),
                "",
                "Nombre de la carpeta",
                "Crear",
                name -> {
                    if (name.contains("/") || name.contains("\\") || "..".equals(name)) {
                        Toast.makeText(this, "Nombre no válido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File sub = new File(currentBrowseDir, name);
                    if (sub.exists()) {
                        Toast.makeText(this, "Ya existe una carpeta con ese nombre", Toast.LENGTH_SHORT).show();
                    } else if (sub.mkdirs()) {
                        refreshFilesList();
                    } else {
                        Toast.makeText(this, "No se pudo crear la carpeta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateFileDialog() {
        showTextPromptDialog(
                "Nuevo archivo de texto",
                "Se creará dentro de " + currentRelDisplayPath(),
                "nota.txt",
                "Nombre del archivo (ej. notas.md)",
                "Crear y abrir",
                name -> {
                    if (name.contains("/") || name.contains("\\") || "..".equals(name)) {
                        Toast.makeText(this, "Nombre no válido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final File target = new File(currentBrowseDir, name);
                    if (target.exists()) {
                        Toast.makeText(this, "Ya existe un archivo con ese nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fileBgExecutor.execute(() -> {
                        try {
                            FileOps.writeTextFile(target, "");
                            uiHandler.post(() -> {
                                refreshFilesList();
                                openTextEditorDialog(target);
                            });
                        } catch (Exception e) {
                            uiHandler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                });
    }

    private void showRenameDialog(final File item) {
        showTextPromptDialog(
                "Renombrar",
                "Nombre actual: " + item.getName(),
                item.getName(),
                "Nuevo nombre",
                "Guardar",
                newName -> {
                    if (newName.contains("/") || newName.contains("\\") || "..".equals(newName)) {
                        Toast.makeText(this, "Nombre no válido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File dest = new File(item.getParentFile(), newName);
                    if (dest.exists()) {
                        Toast.makeText(this, "Ya existe un elemento con ese nombre", Toast.LENGTH_SHORT).show();
                    } else if (item.renameTo(dest)) {
                        refreshFilesList();
                    } else {
                        Toast.makeText(this, "No se pudo renombrar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addModalOption(LinearLayout box, Dialog d, String label, int iconRes, int bg, int fg, Runnable action) {
        Button b = createIconPillButton(label, iconRes, bg, fg);
        if (iconRes <= 0) b.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        lp.bottomMargin = dp(7);
        b.setLayoutParams(lp);
        b.setOnClickListener(v -> {
            d.dismiss();
            action.run();
        });
        box.addView(b);
    }

    private void showItemOptionsDialog(final File item) {
        final Dialog d = new Dialog(this);
        final String lower = item.getName().toLowerCase(Locale.US);
        LinearLayout box = showExpressiveModal(d, item.getName(),
                item.isDirectory() ? "Carpeta" : TransferStats.formatBytes(item.length()));

        if (item.isDirectory()) {
            addModalOption(box, d, "Abrir carpeta", R.drawable.ic_m3_folder, theme.secondaryContainer, theme.onSecondaryContainer,
                    () -> navigateToDir(item));
            if (fileTabs.size() < 6) {
                addModalOption(box, d, "Abrir en nueva pestaña", R.drawable.ic_m3_folder, theme.surfaceContainerHighest, theme.onSurface,
                        () -> {
                            fileTabs.add(item);
                            activeFileTab = fileTabs.size() - 1;
                            navigateToDir(item);
                        });
            }
            addModalOption(box, d, "Empaquetar (ZIP / TAR.GZ)", R.drawable.ic_m3_zip, theme.primary, theme.onPrimary,
                    () -> showPackFormatDialog(Collections.singletonList(item), item.getName()));
            addModalOption(box, d, "Fijar marcador rápido", 0, theme.surfaceContainerHighest, theme.onSurface,
                    () -> pinFolder(item));
        } else {
            if (isArchiveName(lower)) {
                addModalOption(box, d, "Ver contenido del archivo", R.drawable.ic_m3_zip, theme.primary, theme.onPrimary,
                        () -> openArchiveInspectorDialog(item));
                addModalOption(box, d, "Extraer en subcarpeta", R.drawable.ic_m3_download, theme.secondaryContainer, theme.onSecondaryContainer,
                        () -> extractArchiveAsync(item));
            }
            if (isImageName(lower)) {
                addModalOption(box, d, "Vista previa de imagen", R.drawable.ic_m3_photo, theme.primaryContainer, theme.onPrimaryContainer,
                        () -> openImagePreviewDialog(item));
            }
            if (isTextName(lower) || item.length() <= 256 * 1024L) {
                addModalOption(box, d, "Abrir visor / editor de texto", R.drawable.ic_m3_storage, theme.secondaryContainer, theme.onSecondaryContainer,
                        () -> openTextEditorDialog(item));
            }
            addModalOption(box, d, "Comprimir en ZIP / TAR.GZ", R.drawable.ic_m3_zip, theme.surfaceContainerHighest, theme.onSurface,
                    () -> showPackFormatDialog(Collections.singletonList(item), stripExtension(item.getName())));
        }

        if ((activeCategory != null || deepSearchMode) && item.getParentFile() != null) {
            addModalOption(box, d, "Ir a su carpeta contenedora", R.drawable.ic_m3_folder, theme.surfaceContainerHighest, theme.onSurface,
                    () -> navigateToDir(item.getParentFile()));
        }

        addModalOption(box, d, "Copiar al portapapeles", R.drawable.ic_m3_copy, theme.surfaceContainerHighest, theme.onSurface,
                () -> stageSingleClipboard(item, false));
        addModalOption(box, d, "Cortar (Mover)", R.drawable.ic_m3_copy, theme.surfaceContainerHighest, theme.onSurface,
                () -> stageSingleClipboard(item, true));
        addModalOption(box, d, "Propiedades y MD5 / SHA-256", 0, theme.surfaceContainerHighest, theme.onSurface,
                () -> showPropertiesDialog(item));
        addModalOption(box, d, "Renombrar", R.drawable.ic_m3_tune, theme.surfaceContainerHighest, theme.onSurface,
                () -> showRenameDialog(item));
        addModalOption(box, d, "Eliminar", R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer,
                () -> confirmDeleteItem(item));

        Button cancelB = createIconPillButton("Cerrar", 0, Color.TRANSPARENT, theme.onSurfaceVariant);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        cancelB.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        cancelB.setOnClickListener(v -> d.dismiss());
        box.addView(cancelB);
        d.show();
    }

    private void openArchiveInspectorDialog(final File archive) {
        final Dialog d = new Dialog(this);
        final LinearLayout box = showExpressiveModal(d, archive.getName(), "Leyendo índice del archivo comprimido...");
        final TextView infoTv = new TextView(this);
        infoTv.setTextColor(theme.onSurfaceVariant);
        infoTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        infoTv.setTypeface(Typeface.MONOSPACE);
        infoTv.setText("Cargando entradas...");
        infoTv.setPadding(0, 0, 0, dp(12));
        box.addView(infoTv);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button closeB = createIconPillButton("Cerrar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        closeB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        clp.rightMargin = dp(6);
        closeB.setLayoutParams(clp);
        closeB.setOnClickListener(v -> d.dismiss());
        row.addView(closeB);

        Button extB = createIconPillButton("Extraer todo", R.drawable.ic_m3_download, theme.primary, theme.onPrimary);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        elp.leftMargin = dp(6);
        extB.setLayoutParams(elp);
        extB.setOnClickListener(v -> {
            d.dismiss();
            extractArchiveAsync(archive);
        });
        row.addView(extB);
        box.addView(row);
        d.show();

        final boolean isTarGz = archive.getName().toLowerCase(Locale.US).endsWith(".tar.gz")
                || archive.getName().toLowerCase(Locale.US).endsWith(".tgz");
        fileBgExecutor.execute(() -> {
            try {
                List<FolderZipper.ArchiveEntry> list = isTarGz
                        ? FolderZipper.listTarGzEntries(archive, 400)
                        : FolderZipper.listZipEntries(archive, 400);
                StringBuilder sb = new StringBuilder();
                for (FolderZipper.ArchiveEntry e : list) {
                    sb.append(e.isDir ? "[DIR] " : "      ").append(e.name);
                    if (!e.isDir) sb.append(" (").append(TransferStats.formatBytes(e.size)).append(")");
                    sb.append('\n');
                }
                final String body = list.isEmpty() ? "(Archivo vacío)" : sb.toString();
                uiHandler.post(() -> infoTv.setText(body));
            } catch (Exception ex) {
                uiHandler.post(() -> infoTv.setText("Error al leer contenido: " + ex.getMessage()));
            }
        });
    }

    private void openTextEditorDialog(final File file) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, file.getName(), "Editor de texto UTF-8 (máx. 512 KB)");

        final EditText editorEt = new EditText(this);
        editorEt.setText("Cargando...");
        editorEt.setEnabled(false);
        editorEt.setTextColor(theme.onSurface);
        editorEt.setTypeface(Typeface.MONOSPACE);
        editorEt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        editorEt.setGravity(Gravity.TOP | Gravity.START);
        editorEt.setMinLines(10);
        editorEt.setMaxLines(18);
        editorEt.setBackground(roundedRect(theme.surfaceContainerHighest, ThemeTokens.withAlpha(theme.outline, 0x70), dp(16)));
        editorEt.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        elp.bottomMargin = dp(12);
        editorEt.setLayoutParams(elp);
        box.addView(editorEt);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button cancelB = createIconPillButton("Cerrar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        clp.rightMargin = dp(6);
        cancelB.setLayoutParams(clp);
        cancelB.setOnClickListener(v -> d.dismiss());
        row.addView(cancelB);

        final Button saveB = createIconPillButton("Guardar", 0, theme.primary, theme.onPrimary);
        saveB.setCompoundDrawablesRelative(null, null, null, null);
        saveB.setEnabled(false);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        slp.leftMargin = dp(6);
        saveB.setLayoutParams(slp);
        saveB.setOnClickListener(v -> {
            final String content = editorEt.getText().toString();
            saveB.setEnabled(false);
            fileBgExecutor.execute(() -> {
                try {
                    FileOps.writeTextFile(file, content);
                    uiHandler.post(() -> {
                        d.dismiss();
                        Toast.makeText(this, "Guardado: " + file.getName(), Toast.LENGTH_SHORT).show();
                        refreshFilesList();
                    });
                } catch (Exception ex) {
                    uiHandler.post(() -> {
                        saveB.setEnabled(true);
                        Toast.makeText(this, "Error al guardar: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
        row.addView(saveB);
        box.addView(row);
        d.show();

        fileBgExecutor.execute(() -> {
            try {
                final String content = FileOps.readTextFile(file, 512 * 1024);
                uiHandler.post(() -> {
                    editorEt.setText(content);
                    editorEt.setEnabled(true);
                    saveB.setEnabled(true);
                });
            } catch (Exception ex) {
                uiHandler.post(() -> editorEt.setText("No se pudo abrir: " + ex.getMessage()));
            }
        });
    }

    private void openImagePreviewDialog(final File imgFile) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, imgFile.getName(), TransferStats.formatBytes(imgFile.length()));

        final ImageView iv = new ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(280));
        ilp.bottomMargin = dp(12);
        iv.setLayoutParams(ilp);
        box.addView(iv);

        Button closeB = createIconPillButton("Cerrar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        closeB.setCompoundDrawablesRelative(null, null, null, null);
        closeB.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        closeB.setOnClickListener(v -> d.dismiss());
        box.addView(closeB);
        d.show();

        fileBgExecutor.execute(() -> {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bounds);
            int maxDim = Math.max(bounds.outWidth, bounds.outHeight);
            int sample = 1;
            while (maxDim / sample > 1200) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            final Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), opts);
            uiHandler.post(() -> {
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                } else {
                    Toast.makeText(this, "No se pudo decodificar la imagen", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showPropertiesDialog(final File item) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, "Propiedades", item.getName());

        String relRaw = (storageRoot != null)
                ? item.getAbsolutePath().replace(storageRoot.getAbsolutePath(), "")
                : item.getAbsolutePath();
        if (relRaw.isEmpty()) relRaw = "/";
        final String relPath = relRaw;
        SimpleDateFormat fullDf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

        final TextView propsTv = new TextView(this);
        propsTv.setTextColor(theme.onSurface);
        propsTv.setTypeface(Typeface.MONOSPACE);
        propsTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        propsTv.setPadding(dp(12), dp(12), dp(12), dp(12));
        propsTv.setBackground(roundedRect(theme.surfaceContainerHighest, 0, dp(16)));
        final String baseInfo = "Ruta: " + relPath
                + "\nTipo: " + (item.isDirectory() ? "Carpeta" : "Archivo")
                + "\nPermisos: " + FileOps.formatPermissions(item)
                + "\nModificado: " + fullDf.format(new Date(item.lastModified()))
                + (item.isDirectory() ? "\nCalculando tamaño y contenido..." : ("\nTamaño: " + TransferStats.formatBytes(item.length()) + "\nCalculando MD5 y SHA-256..."));
        propsTv.setText(baseInfo);
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pLp.bottomMargin = dp(12);
        propsTv.setLayoutParams(pLp);
        box.addView(propsTv);

        final Button copyHashBtn = createIconPillButton("Copiar ruta", R.drawable.ic_m3_copy, theme.secondaryContainer, theme.onSecondaryContainer);
        LinearLayout.LayoutParams chLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        chLp.bottomMargin = dp(8);
        copyHashBtn.setLayoutParams(chLp);
        final String[] copyVal = new String[]{item.getAbsolutePath()};
        copyHashBtn.setOnClickListener(v -> copyToClipboard("Propiedad", copyVal[0]));
        box.addView(copyHashBtn);

        Button closeB = createIconPillButton("Cerrar", 0, Color.TRANSPARENT, theme.onSurfaceVariant);
        closeB.setCompoundDrawablesRelative(null, null, null, null);
        closeB.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        closeB.setOnClickListener(v -> d.dismiss());
        box.addView(closeB);
        d.show();

        fileBgExecutor.execute(() -> {
            if (item.isDirectory()) {
                long[] st = FileOps.folderStats(item);
                final String upd = "Ruta: " + relPath
                        + "\nTipo: Carpeta\nPermisos: " + FileOps.formatPermissions(item)
                        + "\nModificado: " + fullDf.format(new Date(item.lastModified()))
                        + "\nArchivos: " + st[1] + " · Subcarpetas: " + st[2]
                        + "\nTamaño total: " + TransferStats.formatBytes(st[0]);
                uiHandler.post(() -> propsTv.setText(upd));
            } else {
                try {
                    String[] hashes = FileOps.computeMd5Sha256(item);
                    final String upd = "Ruta: " + relPath
                            + "\nTamaño: " + TransferStats.formatBytes(item.length()) + " (" + item.length() + " B)"
                            + "\nPermisos: " + FileOps.formatPermissions(item)
                            + "\nModificado: " + fullDf.format(new Date(item.lastModified()))
                            + "\nMD5: " + hashes[0]
                            + "\nSHA-256: " + hashes[1];
                    copyVal[0] = "MD5=" + hashes[0] + "\nSHA256=" + hashes[1];
                    uiHandler.post(() -> {
                        propsTv.setText(upd);
                        copyHashBtn.setText("Copiar MD5 y SHA-256");
                    });
                } catch (Exception ignored) {
                }
            }
        });
    }

    private LinearLayout showExpressiveModal(Dialog d, String title, String subtitle) {
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout outer = new FrameLayout(this);
        int hp = dp(skin.bottomSheetDialogs ? 12 : 18);
        outer.setPadding(hp, dp(18), hp, dp(skin.bottomSheetDialogs ? 14 : 18));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.setBackground(roundedRect(
                theme.surfaceContainerHigh,
                ThemeTokens.withAlpha(theme.outlineVariant, Math.max(0x55, skin.cardStrokeAlpha)),
                dp(skin.dialogRadiusDp)));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(skin.bottomSheetHandle ? 12 : 20), dp(20), dp(18));
        OsSkinRenderer.maybeAddBottomSheetHandle(this, box, skin, theme);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(theme.onSurface);
        t.setTypeface(skin.titleTypeface());
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        box.addView(t);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView s = new TextView(this);
            s.setText(subtitle);
            s.setTextColor(theme.onSurfaceVariant);
            s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            s.setPadding(0, dp(4), 0, dp(12));
            box.addView(s);
        } else {
            t.setPadding(0, 0, 0, dp(12));
        }

        sv.addView(box, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        outer.addView(sv, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        d.setContentView(outer);

        Window win = d.getWindow();
        if (win != null) {
            win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            win.setGravity(skin.bottomSheetDialogs ? Gravity.BOTTOM : Gravity.CENTER);
        }
        return box;
    }

    private void confirmDeleteItem(final File item) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d,
                "¿Eliminar " + (item.isDirectory() ? "la carpeta" : "el archivo") + "?",
                item.getName());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button cancelB = createIconPillButton("Cancelar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        clp.rightMargin = dp(6);
        cancelB.setLayoutParams(clp);
        cancelB.setOnClickListener(v -> d.dismiss());
        row.addView(cancelB);

        Button delB = createIconPillButton("Eliminar", R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        dlp.leftMargin = dp(6);
        delB.setLayoutParams(dlp);
        delB.setOnClickListener(v -> {
            d.dismiss();
            runMonitoredFileOp(FileOpMonitor.OP_DELETE, Collections.singletonList(item), null, null);
        });
        row.addView(delB);
        box.addView(row);
        d.show();
    }

    private interface TextPromptCallback {
        void onSubmit(String text);
    }

    private void showTextPromptDialog(String title, String subtitle, String initial,
                                      String hint, String confirmLabel, final TextPromptCallback cb) {
        final Dialog d = new Dialog(this);
        LinearLayout box = showExpressiveModal(d, title, subtitle);

        final EditText et = createExpressiveEditText(initial, hint);
        et.setTypeface(Typeface.DEFAULT);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setBackground(roundedRect(theme.surfaceContainerHighest, ThemeTokens.withAlpha(theme.outline, 0x90), dp(16)));
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        elp.bottomMargin = dp(16);
        et.setLayoutParams(elp);
        box.addView(et);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button cancelB = createIconPillButton("Cancelar", 0, theme.secondaryContainer, theme.onSecondaryContainer);
        cancelB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        clp.rightMargin = dp(6);
        cancelB.setLayoutParams(clp);
        cancelB.setOnClickListener(v -> d.dismiss());
        row.addView(cancelB);

        Button okB = createIconPillButton(confirmLabel, 0, theme.primary, theme.onPrimary);
        okB.setCompoundDrawablesRelative(null, null, null, null);
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        olp.leftMargin = dp(6);
        okB.setLayoutParams(olp);
        okB.setOnClickListener(v -> {
            String val = et.getText().toString().trim();
            if (!val.isEmpty()) {
                d.dismiss();
                cb.onSubmit(val);
            }
        });
        row.addView(okB);
        box.addView(row);
        d.show();
    }

    private boolean isStackedNav() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (dm.widthPixels / Math.max(1f, dm.density)) < 540f;
    }

    private void switchTab(int targetIdx) {
        if (activeTabIndex == targetIdx) return;
        activeTabIndex = targetIdx;

        serverTabView.setVisibility(targetIdx == 0 ? View.VISIBLE : View.GONE);
        filesTabView.setVisibility(targetIdx == 1 ? View.VISIBLE : View.GONE);
        storageTabView.setVisibility(targetIdx == 2 ? View.VISIBLE : View.GONE);
        settingsTabView.setVisibility(targetIdx == 3 ? View.VISIBLE : View.GONE);

        View show = (targetIdx == 0) ? serverTabView
                : ((targetIdx == 1) ? filesTabView
                : ((targetIdx == 2) ? storageTabView : settingsTabView));
        show.setAlpha(0f);
        show.setTranslationY(dp(12));
        show.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(240)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        if (mainScrollView != null) {
            mainScrollView.scrollTo(0, 0);
        }

        if (targetIdx == 1) {
            refreshFilesList();
        } else if (targetIdx == 2) {
            refreshStorageVolumesBar();
            if (storageTreeRoot == null) {
                startStorageScan(storageSelectedVolumeRoot != null ? storageSelectedVolumeRoot : storageRoot);
            }
        }

        for (int i = 0; i < 4; i++) {
            OsSkinRenderer.styleNavTab(this, navTabs[i], i == targetIdx, skin, theme, ripplePrimary);
        }
    }

    private LinearLayout createExpressiveCard(LinearLayout parent, int bgColor, int radiusDp) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int effectiveRadiusDp = (radiusDp >= 30) ? skin.heroRadiusDp : skin.cardRadiusDp;
        int strokeCol = (skin.cardStrokeAlpha > 0)
                ? ThemeTokens.withAlpha(theme.outlineVariant, skin.cardStrokeAlpha)
                : 0;
        card.setBackground(roundedRect(bgColor, strokeCol, dp(effectiveRadiusDp)));
        if (skin.cardElevationDp > 0f) {
            card.setElevation(skin.cardElevationDp * getResources().getDisplayMetrics().density);
        }
        int p = dp(skin.cardPaddingDp);
        card.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(skin.cardGapDp);
        card.setLayoutParams(lp);
        parent.addView(card);
        entranceCards.add(card);
        return card;
    }

    private LinearLayout createSectionHeader(String text, int iconRes, int iconTint) {
        return OsSkinRenderer.buildSectionHeaderRow(this, text, iconRes, iconTint, skin, theme);
    }

    private TextView createSmallLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(theme.onSurfaceVariant);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(dp(4), 0, 0, dp(5));
        return tv;
    }

    private EditText createExpressiveEditText(String initial, String hint) {
        EditText et = new EditText(this);
        et.setText(initial);
        et.setHint(hint);
        et.setHintTextColor(ThemeTokens.withAlpha(theme.onSurfaceVariant, 130));
        et.setTextColor(theme.onSurface);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setTypeface(Typeface.MONOSPACE);
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        et.setBackground(roundedRect(theme.surfaceContainerHigh, ThemeTokens.withAlpha(theme.outlineVariant, 0x80), dp(16)));
        et.setPadding(dp(14), dp(12), dp(14), dp(12));
        return et;
    }

    private Button createIconPillButton(String label, int iconRes, int fillColor, int textColor) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(skin.uppercaseButtons);
        b.setTextColor(textColor);
        b.setTypeface(skin.bodyMediumTypeface());
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        b.setBackground(rippleBg(fillColor, ripplePrimary, 0, dp(skin.buttonRadiusDp)));
        b.setStateListAnimator(null);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setGravity(Gravity.CENTER);
        b.setCompoundDrawablePadding(dp(7));
        b.setSingleLine(true);
        b.setEllipsize(TextUtils.TruncateAt.END);
        setButtonIconTint(b, iconRes, textColor, 17);
        attachSpringTouch(b);
        return b;
    }

    private void setButtonIconTint(Button b, int iconRes, int color, int sizeDp) {
        Drawable d = tintedIcon(iconRes, color, sizeDp);
        b.setCompoundDrawablesRelative(d, null, null, null);
    }

    private Drawable tintedIcon(int resId, int color, int sizeDp) {
        if (resId <= 0) return null;
        Drawable raw = getDrawable(resId);
        if (raw == null) return null;
        Drawable d = raw.mutate();
        d.setTint(color);
        int px = dp(sizeDp);
        d.setBounds(0, 0, px, px);
        return d;
    }

    private LinearLayout createSpeedTile(String label, int iconRes, int containerBg, int onContainer) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setBackground(roundedRect(containerBg, 0, dp(skin.innerRadiusDp)));
        tile.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView iv = new ImageView(this);
        iv.setImageDrawable(tintedIcon(iconRes, onContainer, 16));
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(16), dp(16));
        ilp.rightMargin = dp(6);
        iv.setLayoutParams(ilp);
        top.addView(iv);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextColor(ThemeTokens.withAlpha(onContainer, 225));
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(lbl);
        tile.addView(top);

        TextView val = new TextView(this);
        val.setText("0 B/s");
        val.setTextColor(onContainer);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        val.setSingleLine(true);
        val.setAutoSizeTextTypeUniformWithConfiguration(14, 24, 1, TypedValue.COMPLEX_UNIT_SP);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        val.setLetterSpacing(-0.02f);
        val.setFontFeatureSettings("tnum");
        val.setPadding(0, dp(6), 0, dp(2));
        tile.addView(val);

        TextView tot = new TextView(this);
        tot.setText("Total: 0 B");
        tot.setTextColor(ThemeTokens.withAlpha(onContainer, 200));
        tot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tot.setFontFeatureSettings("tnum");
        tile.addView(tot);
        return tile;
    }

    private ProgressBar createExpressiveProgressBar(int fillColor, int trackColor, int radiusDp) {
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        GradientDrawable track = roundedRect(trackColor, 0, dp(radiusDp));
        GradientDrawable fillShape = roundedRect(fillColor, 0, dp(radiusDp));
        ScaleDrawable fillScale = new ScaleDrawable(fillShape, Gravity.START, 1f, -1f);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{track, fillScale});
        ld.setId(0, android.R.id.background);
        ld.setId(1, android.R.id.progress);
        pb.setProgressDrawable(ld);
        Drawable ind = pb.getIndeterminateDrawable();
        if (ind != null) {
            ind.mutate().setColorFilter(fillColor, PorterDuff.Mode.SRC_IN);
        }
        return pb;
    }

    private void animateGlobalSpeed(final boolean isUp, final long newTarget) {
        if (isUp) {
            if (newTarget == targetUpBps) return;
            targetUpBps = newTarget;
            if (upSpeedAnim != null) upSpeedAnim.cancel();
            final long start = dispUpBps;
            upSpeedAnim = ValueAnimator.ofFloat(start, newTarget);
            upSpeedAnim.setDuration(400);
            upSpeedAnim.setInterpolator(new DecelerateInterpolator());
            upSpeedAnim.addUpdateListener(a -> {
                dispUpBps = Math.round((float) a.getAnimatedValue());
                upSpeedText.setText(TransferStats.formatSpeed(dispUpBps));
            });
            upSpeedAnim.start();
        } else {
            if (newTarget == targetDownBps) return;
            targetDownBps = newTarget;
            if (downSpeedAnim != null) downSpeedAnim.cancel();
            final long start = dispDownBps;
            downSpeedAnim = ValueAnimator.ofFloat(start, newTarget);
            downSpeedAnim.setDuration(400);
            downSpeedAnim.setInterpolator(new DecelerateInterpolator());
            downSpeedAnim.addUpdateListener(a -> {
                dispDownBps = Math.round((float) a.getAnimatedValue());
                downSpeedText.setText(TransferStats.formatSpeed(dispDownBps));
            });
            downSpeedAnim.start();
        }
    }

    private void refreshUi() {
        boolean running = stats.isServerRunning();
        String ip = stats.getServerIp();
        if (ip == null || ip.isEmpty() || "0.0.0.0".equals(ip)) {
            ip = TransferService.getLocalWifiIp();
        }
        int port = stats.getServerPort();
        String host = stats.getLocalHost();
        if (host == null || host.isEmpty()) {
            host = LocalDnsResponder.resolveBaseBrand() + ".files.local";
        }
        String ipUrl = LocalDnsResponder.formatHttpUrl(ip, port);
        String webUrl = LocalDnsResponder.formatHttpUrl(host, port);

        int pillRad = dp(skin.chipRadiusDp == 999 ? 24 : skin.chipRadiusDp);
        int btnRad = dp(skin.buttonRadiusDp);
        if (running) {
            statusText.setText("En línea");
            statusText.setTextColor(theme.onSecondaryContainer);
            int dotColor = (skin.id == OsDesignSystem.SkinId.NOTHING_OS)
                    ? Color.parseColor("#D71921")
                    : theme.primary;
            pulseDot.setBackground(circleDrawable(dotColor));
            statusPill.setBackground(roundedRect(theme.secondaryContainer, 0, pillRad));
            toggleBtn.setText("Detener");
            toggleBtn.setTextColor(theme.onErrorContainer);
            toggleBtn.setBackground(rippleBg(theme.errorContainer, ripplePrimary, 0, btnRad));
            setButtonIconTint(toggleBtn, R.drawable.ic_m3_power, theme.onErrorContainer, 17);
            if (quickToggleServerBtn != null) {
                quickToggleServerBtn.setText("Detener servidor");
                quickToggleServerBtn.setTextColor(theme.onErrorContainer);
                quickToggleServerBtn.setBackground(rippleBg(theme.errorContainer, ripplePrimary, 0, btnRad));
                setButtonIconTint(quickToggleServerBtn, R.drawable.ic_m3_power, theme.onErrorContainer, 16);
            }
            settingsStateText.setText("Activo en " + webUrl + "\nIP alternativa: " + ipUrl);
            startPulseAnimation();
        } else {
            statusText.setText("Detenido");
            statusText.setTextColor(theme.onErrorContainer);
            pulseDot.setBackground(circleDrawable(theme.error));
            statusPill.setBackground(roundedRect(theme.errorContainer, 0, pillRad));
            toggleBtn.setText("Iniciar");
            toggleBtn.setTextColor(theme.onPrimary);
            toggleBtn.setBackground(rippleBg(theme.primary, ripplePrimary, 0, btnRad));
            setButtonIconTint(toggleBtn, R.drawable.ic_m3_power, theme.onPrimary, 17);
            if (quickToggleServerBtn != null) {
                quickToggleServerBtn.setText("Iniciar servidor");
                quickToggleServerBtn.setTextColor(theme.onPrimary);
                quickToggleServerBtn.setBackground(rippleBg(theme.primary, ripplePrimary, 0, btnRad));
                setButtonIconTint(quickToggleServerBtn, R.drawable.ic_m3_power, theme.onPrimary, 16);
            }
            settingsStateText.setText("Servidor detenido");
            stopPulseAnimation();
        }

        boolean hasStoragePerm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasStoragePerm = Environment.isExternalStorageManager();
        } else {
            hasStoragePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        permCard.setVisibility(hasStoragePerm ? View.GONE : View.VISIBLE);

        boolean hasPromo = Build.VERSION.SDK_INT < 36 || TransferService.canPostPromotedNotifications(this);
        promoCard.setVisibility(hasPromo ? View.GONE : View.VISIBLE);

        String promoSummary = (Build.VERSION.SDK_INT < 36)
                ? ("Notificación en vivo: Activa (Android " + Build.VERSION.RELEASE + ")")
                : ("Live Updates Android 16/17: " + (hasPromo ? "Activado" : "Pendiente"));
        permStatusText.setText("Almacenamiento completo: " + (hasStoragePerm ? "Concedido" : "Pendiente")
                + " · " + promoSummary);

        webUrlText.setText(webUrl);
        if (ipFallbackText != null) {
            ipFallbackText.setText("IP alternativa (toca): " + ipUrl);
            ipFallbackText.setTag(ipUrl);
        }
        if (headerRefs != null && headerRefs.extraSubBannerText != null) {
            String bannerLabel;
            if (skin.id == OsDesignSystem.SkinId.EMUI) {
                bannerLabel = (running ? "HarmonyOS · " : "Detenido · ") + webUrl;
            } else if (skin.id == OsDesignSystem.SkinId.MAGIC_OS) {
                bannerLabel = "Magic Capsule · " + (running ? webUrl : "Servidor detenido");
            } else {
                bannerLabel = "Aqua Dynamics · " + (running ? webUrl : "Detenido");
            }
            headerRefs.extraSubBannerText.setText(bannerLabel);
        }

        if (!ipUrl.equals(lastQrUrl)) {
            lastQrUrl = ipUrl;
            try {
                lastQrBitmap = QrCodeGenerator.generateBitmap(ipUrl, dp(180), Color.parseColor("#111318"), Color.WHITE);
                qrImageView.setImageBitmap(lastQrBitmap);
            } catch (Exception ignored) {
            }
        }

        try {
            StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long avail = sf.getAvailableBytes();
            long total = sf.getTotalBytes();
            long used = Math.max(0L, total - avail);
            int pct = total > 0 ? (int) ((used * 100L) / total) : 0;
            storageText.setText("Almacenamiento interno: " + TransferStats.formatBytes(avail)
                    + " libres de " + TransferStats.formatBytes(total) + " (" + pct + "% usado)");
            storageProgress.setProgress(pct);
        } catch (Exception ignored) {
        }

        animateGlobalSpeed(true, stats.getUploadSpeedBps());
        animateGlobalSpeed(false, stats.getDownloadSpeedBps());
        upTotalText.setText("Total: " + TransferStats.formatBytes(stats.getTotalUploadBytes()));
        downTotalText.setText("Total: " + TransferStats.formatBytes(stats.getTotalDownloadBytes()));
        speedGraph.updateSamples(stats.getUploadHistory(), stats.getDownloadHistory());

        List<TransferStats.ActiveTransfer> actives = stats.getActiveTransfers();
        activeCountBadge.setText(actives.size() + (actives.size() == 1 ? " activa" : " activas"));
        stopAllBtn.setVisibility(actives.isEmpty() ? View.GONE : View.VISIBLE);
        syncActiveTransferViews(actives);

        List<TransferStats.CompletedTransfer> recents = stats.getRecentTransfers();
        recentContainer.removeAllViews();
        if (recents.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Ninguna transferencia completada todavía.");
            empty.setTextColor(theme.onSurfaceVariant);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            empty.setPadding(0, dp(2), 0, dp(2));
            recentContainer.addView(empty);
        } else {
            int limit = Math.min(12, recents.size());
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss", Locale.US);
            for (int i = 0; i < limit; i++) {
                if (skin.groupedListDividers && i > 0) {
                    recentContainer.addView(OsSkinRenderer.createGroupedDivider(this, theme, 32));
                }
                recentContainer.addView(buildRecentRow(recents.get(i), tf));
            }
        }
    }

    private void syncActiveTransferViews(List<TransferStats.ActiveTransfer> actives) {
        if (actives.isEmpty()) {
            txSpeedDispMap.clear();
            if (activeContainer.getChildCount() != 1 || !"EMPTY".equals(activeContainer.getChildAt(0).getTag())) {
                activeContainer.removeAllViews();
                TextView empty = new TextView(this);
                empty.setTag("EMPTY");
                empty.setText("Esperando transferencias desde el portal web...");
                empty.setTextColor(theme.onSurfaceVariant);
                empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                empty.setPadding(0, dp(2), 0, dp(2));
                activeContainer.addView(empty);
            }
            return;
        }

        if (activeContainer.getChildCount() == 1 && "EMPTY".equals(activeContainer.getChildAt(0).getTag())) {
            activeContainer.removeAllViews();
        }

        boolean sameSet = (activeContainer.getChildCount() == actives.size());
        if (sameSet) {
            for (int i = 0; i < actives.size(); i++) {
                Object tag = activeContainer.getChildAt(i).getTag();
                if (!actives.get(i).id.equals(tag)) {
                    sameSet = false;
                    break;
                }
            }
        }

        long now = System.currentTimeMillis();
        if (!sameSet) {
            activeContainer.removeAllViews();
            for (TransferStats.ActiveTransfer tx : actives) {
                View row = createActiveRow(tx, now);
                row.setAlpha(0f);
                row.setTranslationY(dp(12));
                row.setScaleX(0.96f);
                row.setScaleY(0.96f);
                activeContainer.addView(row);
                row.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(260)
                        .setInterpolator(new OvershootInterpolator(1.3f))
                        .start();
            }
        } else {
            for (int i = 0; i < actives.size(); i++) {
                updateActiveRow((LinearLayout) activeContainer.getChildAt(i), actives.get(i), now);
            }
        }
    }

    private View createActiveRow(final TransferStats.ActiveTransfer tx, long now) {
        LinearLayout row = new LinearLayout(this);
        row.setTag(tx.id);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackground(roundedRect(theme.surfaceContainerHigh, 0, dp(22)));
        row.setPadding(dp(15), dp(13), dp(15), dp(13));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        row.setLayoutParams(lp);

        boolean isUp = TransferStats.DIR_UPLOAD.equals(tx.direction);
        int dirAccent = isUp ? theme.primary : theme.tertiary;

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView dirIco = new ImageView(this);
        dirIco.setImageDrawable(tintedIcon(
                isUp ? R.drawable.ic_m3_upload : R.drawable.ic_m3_download,
                dirAccent, 18));
        LinearLayout.LayoutParams diLp = new LinearLayout.LayoutParams(dp(18), dp(18));
        diLp.rightMargin = dp(8);
        dirIco.setLayoutParams(diLp);
        top.addView(dirIco);

        TextView name = new TextView(this);
        name.setText(tx.fileName);
        name.setTextColor(theme.onSurface);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(name);

        Button cancelBtn = createIconPillButton("Detener", R.drawable.ic_m3_stop, theme.errorContainer, theme.onErrorContainer);
        LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(32));
        cbLp.leftMargin = dp(8);
        cancelBtn.setLayoutParams(cbLp);
        cancelBtn.setPadding(dp(12), 0, dp(12), 0);
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        cancelBtn.setOnClickListener(v -> {
            stats.cancelTransfer(tx.id);
            Toast.makeText(this, "Transferencia detenida: " + tx.fileName, Toast.LENGTH_SHORT).show();
            refreshUi();
        });
        top.addView(cancelBtn);
        row.addView(top);

        LinearLayout folderRow = new LinearLayout(this);
        folderRow.setOrientation(LinearLayout.HORIZONTAL);
        folderRow.setGravity(Gravity.CENTER_VERTICAL);
        folderRow.setPadding(0, dp(6), 0, dp(8));

        LinearLayout folderChip = new LinearLayout(this);
        folderChip.setOrientation(LinearLayout.HORIZONTAL);
        folderChip.setGravity(Gravity.CENTER_VERTICAL);
        folderChip.setBackground(roundedRect(theme.surfaceContainerHighest, 0, dp(12)));
        folderChip.setPadding(dp(9), dp(4), dp(10), dp(4));

        ImageView fIco = new ImageView(this);
        fIco.setImageDrawable(tintedIcon(R.drawable.ic_m3_folder, theme.secondary, 13));
        LinearLayout.LayoutParams fiLp = new LinearLayout.LayoutParams(dp(13), dp(13));
        fiLp.rightMargin = dp(5);
        fIco.setLayoutParams(fiLp);
        folderChip.addView(fIco);

        TextView folder = new TextView(this);
        folder.setText(tx.folder);
        folder.setTextColor(theme.onSurfaceVariant);
        folder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        folder.setTypeface(Typeface.MONOSPACE);
        folderChip.addView(folder);
        folderRow.addView(folderChip);
        row.addView(folderRow);

        ProgressBar pb = createExpressiveProgressBar(dirAccent, theme.surfaceContainerLowest, 8);
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        pbLp.bottomMargin = dp(6);
        pb.setLayoutParams(pbLp);
        pb.setMax(100);
        row.addView(pb);

        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.HORIZONTAL);
        TextView sizeTv = new TextView(this);
        sizeTv.setTextColor(theme.onSurfaceVariant);
        sizeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sizeTv.setFontFeatureSettings("tnum");
        sizeTv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        meta.addView(sizeTv);

        TextView spTv = new TextView(this);
        spTv.setTextColor(dirAccent);
        spTv.setTypeface(Typeface.DEFAULT_BOLD);
        spTv.setFontFeatureSettings("tnum");
        spTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        meta.addView(spTv);
        row.addView(meta);

        updateActiveRow(row, tx, now);
        return row;
    }

    private void updateActiveRow(LinearLayout row, TransferStats.ActiveTransfer tx, long now) {
        long bytes = tx.bytesTransferred.get();
        long speed = tx.speedBps;
        if (speed == 0 && bytes > 0) {
            speed = (bytes * 1000L) / Math.max(1L, now - tx.startedAtMs);
        }

        ProgressBar pb = (ProgressBar) row.getChildAt(2);
        if (tx.totalBytes > 0) {
            pb.setIndeterminate(false);
            int pct = (int) Math.min(100L, (bytes * 100L) / tx.totalBytes);
            ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), pct).setDuration(350).start();
        } else {
            pb.setIndeterminate(true);
        }

        LinearLayout meta = (LinearLayout) row.getChildAt(3);
        TextView sizeTv = (TextView) meta.getChildAt(0);
        final TextView spTv = (TextView) meta.getChildAt(1);

        String sizeStr = TransferStats.formatBytes(bytes);
        if (tx.totalBytes > 0) {
            int pct = (int) Math.min(100L, (bytes * 100L) / tx.totalBytes);
            sizeStr += " / " + TransferStats.formatBytes(tx.totalBytes) + " (" + pct + "%)";
        }
        sizeTv.setText(sizeStr);

        long[] state = txSpeedDispMap.get(tx.id);
        if (state == null) {
            state = new long[]{speed};
            txSpeedDispMap.put(tx.id, state);
            spTv.setText(TransferStats.formatSpeed(speed));
        } else if (state[0] != speed) {
            final long[] stRef = state;
            ValueAnimator va = ValueAnimator.ofFloat(state[0], speed);
            va.setDuration(380);
            va.setInterpolator(new DecelerateInterpolator());
            va.addUpdateListener(a -> {
                stRef[0] = Math.round((float) a.getAnimatedValue());
                spTv.setText(TransferStats.formatSpeed(stRef[0]));
            });
            va.start();
        }
    }

    private View buildRecentRow(TransferStats.CompletedTransfer done, SimpleDateFormat tf) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackground(roundedRect(
                skin.groupedListDividers ? Color.TRANSPARENT : theme.surfaceContainerHigh,
                0,
                dp(skin.groupedListDividers ? 8 : skin.innerRadiusDp)));
        row.setPadding(dp(skin.groupedListDividers ? 4 : 14), dp(10), dp(skin.groupedListDividers ? 4 : 14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = skin.groupedListDividers ? 0 : dp(8);
        row.setLayoutParams(lp);

        boolean isUp = TransferStats.DIR_UPLOAD.equals(done.direction);
        int accent = done.cancelled ? theme.error : (isUp ? theme.primary : theme.tertiary);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView dirIco = new ImageView(this);
        dirIco.setImageDrawable(tintedIcon(
                isUp ? R.drawable.ic_m3_upload : R.drawable.ic_m3_download,
                accent, 15));
        LinearLayout.LayoutParams diLp = new LinearLayout.LayoutParams(dp(15), dp(15));
        diLp.rightMargin = dp(8);
        dirIco.setLayoutParams(diLp);
        top.addView(dirIco);

        TextView name = new TextView(this);
        name.setText(done.fileName);
        name.setTextColor(done.ok ? theme.onSurface : theme.error);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(name);

        TextView sp = new TextView(this);
        sp.setText(done.cancelled ? "Detenida" : TransferStats.formatSpeed(done.avgSpeedBps));
        sp.setTextColor(accent);
        sp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sp.setTypeface(Typeface.DEFAULT_BOLD);
        sp.setFontFeatureSettings("tnum");
        top.addView(sp);
        row.addView(top);

        TextView sub = new TextView(this);
        sub.setText(tf.format(new Date(done.completedAtMs))
                + " · " + TransferStats.formatBytes(done.bytes)
                + " · " + done.folder);
        sub.setTextColor(theme.onSurfaceVariant);
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sub.setPadding(dp(23), dp(3), 0, 0);
        row.addView(sub);

        return row;
    }

    private void showEnlargedQrDialog() {
        if (lastQrUrl == null || lastQrUrl.isEmpty()) return;
        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout outer = new FrameLayout(this);
        outer.setPadding(dp(20), dp(20), dp(20), dp(20));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setBackground(roundedRect(
                theme.surfaceContainerHigh,
                ThemeTokens.withAlpha(theme.outlineVariant, Math.max(0x60, skin.cardStrokeAlpha)),
                dp(skin.dialogRadiusDp)));
        box.setPadding(dp(22), dp(skin.bottomSheetHandle ? 14 : 24), dp(22), dp(22));
        box.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        OsSkinRenderer.maybeAddBottomSheetHandle(this, box, skin, theme);

        TextView t = new TextView(this);
        t.setText("Expressive Web Files");
        t.setTextColor(theme.onSurface);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, dp(16));
        box.addView(t);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxBoxInner = dm.widthPixels - dp(40) - dp(44) - dp(28);
        int maxH = (int) (dm.heightPixels * 0.44f);
        int qrSize = Math.min(dp(260), Math.max(dp(160), Math.min(maxBoxInner, maxH)));

        LinearLayout whiteWrap = new LinearLayout(this);
        whiteWrap.setBackground(roundedRect(Color.WHITE, 0, dp(24)));
        whiteWrap.setPadding(dp(14), dp(14), dp(14), dp(14));
        whiteWrap.setGravity(Gravity.CENTER);

        ImageView bigQr = new ImageView(this);
        bigQr.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bigQr.setAdjustViewBounds(true);
        Bitmap bigBmp = QrCodeGenerator.generateBitmap(lastQrUrl, qrSize, Color.parseColor("#111318"), Color.WHITE);
        bigQr.setImageBitmap(bigBmp);
        bigQr.setLayoutParams(new LinearLayout.LayoutParams(qrSize, qrSize));
        whiteWrap.addView(bigQr);
        box.addView(whiteWrap);

        TextView urlTv = new TextView(this);
        urlTv.setText(lastQrUrl);
        urlTv.setTextColor(theme.primary);
        urlTv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        urlTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        urlTv.setSingleLine(false);
        urlTv.setMaxLines(3);
        urlTv.setTextIsSelectable(true);
        urlTv.setGravity(Gravity.CENTER);
        urlTv.setBackground(roundedRect(theme.surfaceContainerHighest, 0, dp(16)));
        urlTv.setPadding(dp(14), dp(11), dp(14), dp(11));
        LinearLayout.LayoutParams uLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        uLp.topMargin = dp(16);
        uLp.bottomMargin = dp(16);
        urlTv.setLayoutParams(uLp);
        box.addView(urlTv);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button copyBtn = createIconPillButton("Copiar URL", R.drawable.ic_m3_copy, theme.primary, theme.onPrimary);
        LinearLayout.LayoutParams cpLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        cpLp.rightMargin = dp(6);
        copyBtn.setLayoutParams(cpLp);
        copyBtn.setOnClickListener(v -> copyToClipboard("URL Web", lastQrUrl));
        btnRow.addView(copyBtn);

        Button closeBtn = createIconPillButton("Cerrar", R.drawable.ic_m3_stop, theme.secondaryContainer, theme.onSecondaryContainer);
        LinearLayout.LayoutParams clLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        clLp.leftMargin = dp(6);
        closeBtn.setLayoutParams(clLp);
        closeBtn.setOnClickListener(v -> d.dismiss());
        btnRow.addView(closeBtn);
        box.addView(btnRow);

        outer.addView(box);
        d.setContentView(outer);

        Window win = d.getWindow();
        if (win != null) {
            win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            win.setGravity(skin.bottomSheetDialogs ? Gravity.BOTTOM : Gravity.CENTER);
            WindowManager.LayoutParams wlp = win.getAttributes();
            wlp.screenBrightness = 1.0f;
            win.setAttributes(wlp);
        }
        d.show();
    }

    private void playEntranceAnimations() {
        if (skin.motionStyle == OsDesignSystem.MotionStyle.CLASSIC_RIPPLE) {
            return;
        }
        long dur = (skin.motionStyle == OsDesignSystem.MotionStyle.SNAPPY_TECH) ? 180L : 300L;
        long stagger = (skin.motionStyle == OsDesignSystem.MotionStyle.SNAPPY_TECH) ? 20L : 36L;
        DecelerateInterpolator interp = new DecelerateInterpolator(1.6f);
        for (int i = 0; i < entranceCards.size(); i++) {
            View card = entranceCards.get(i);
            card.setAlpha(0f);
            card.setTranslationY(dp(14));
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(dur)
                    .setStartDelay(i * stagger)
                    .setInterpolator(interp)
                    .start();
        }
    }

    private void startPulseAnimation() {
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            return;
        }
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                pulseDot,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.38f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.38f, 1f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.4f, 1f)
        );
        pulseAnimator.setDuration(1400);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.start();
    }

    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        pulseDot.setScaleX(1f);
        pulseDot.setScaleY(1f);
        pulseDot.setAlpha(1f);
    }

    private void attachSpringTouch(final View v) {
        if (skin == null || skin.motionStyle == OsDesignSystem.MotionStyle.CLASSIC_RIPPLE) {
            return;
        }
        final float pressScale;
        final boolean overshoot;
        if (skin.motionStyle == OsDesignSystem.MotionStyle.FLUID_SPRING) {
            pressScale = 0.94f;
            overshoot = true;
        } else if (skin.motionStyle == OsDesignSystem.MotionStyle.SNAPPY_TECH) {
            pressScale = 0.98f;
            overshoot = false;
        } else {
            pressScale = 0.97f;
            overshoot = false;
        }
        v.setOnTouchListener((view, ev) -> {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().scaleX(pressScale).scaleY(pressScale).setDuration(85).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f)
                            .setInterpolator(overshoot ? new OvershootInterpolator(2.3f) : new DecelerateInterpolator(1.6f))
                            .setDuration(overshoot ? 240 : 140).start();
                    break;
            }
            return false;
        });
    }

    private void copyToClipboard(String label, CharSequence value) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(this, "Copiado: " + value, Toast.LENGTH_SHORT).show();
        }
    }

    private void startTransferService(String action) {
        Intent i = new Intent(this, TransferService.class);
        i.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 102);
            }
        }
    }

    private void openAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            }
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 102);
        }
    }

    private void openPromotedNotificationsSettings() {
        try {
            Intent intent = new Intent("android.settings.APP_NOTIFICATION_PROMOTION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                fallback.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(fallback);
            } catch (Exception ignored) {
            }
        }
    }

    private Drawable rippleBg(int fill, int rippleColor, int stroke, int radiusPx) {
        GradientDrawable content = roundedRect(fill, stroke, radiusPx);
        GradientDrawable mask = roundedRect(Color.WHITE, 0, radiusPx);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, mask);
    }

    private GradientDrawable roundedRect(int fill, int stroke, int radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        if (stroke != 0) {
            d.setStroke(dp(1), stroke);
        }
        d.setCornerRadius(radiusPx);
        return d;
    }

    private GradientDrawable circleDrawable(int fill) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(fill);
        return d;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
