package com.syed.lsfgmali;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.HardwareBuffer;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.lsfg.android.session.NativeBridge;
import java.lang.reflect.Method;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Triple;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: CaptureService.kt */
@Metadata(d1 = {"\u0000\u0094\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\b\u0003\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0012*\u0002+.\u0018\u0000 _2\u00020\u0001:\u0001_B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u00100\u001a\u000201H\u0016J\u0014\u00102\u001a\u0004\u0018\u0001032\b\u00104\u001a\u0004\u0018\u000105H\u0016J\"\u00106\u001a\u00020\u00122\b\u00104\u001a\u0004\u0018\u0001052\u0006\u00107\u001a\u00020\u00122\u0006\u00108\u001a\u00020\u0012H\u0016J\u0010\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\u0012H\u0002J\"\u0010<\u001a\u0014\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00120=2\u0006\u0010>\u001a\u00020?H\u0002J\u0018\u0010@\u001a\u00020\u00122\u0006\u0010A\u001a\u00020\u00122\u0006\u0010B\u001a\u00020\u0012H\u0002J \u0010C\u001a\u00020\t2\u0006\u0010A\u001a\u00020\u00122\u0006\u0010B\u001a\u00020\u00122\u0006\u0010D\u001a\u00020\u000fH\u0002J\b\u0010E\u001a\u000201H\u0002J\b\u0010F\u001a\u000201H\u0002J\u0010\u0010G\u001a\u0002012\u0006\u0010H\u001a\u00020:H\u0002J(\u0010I\u001a\u0002012\u0006\u0010J\u001a\u00020\u00122\u0006\u0010K\u001a\u00020\u00122\u0006\u0010;\u001a\u00020\u00122\u0006\u0010H\u001a\u00020:H\u0002J\u0018\u0010L\u001a\u00020M2\u0006\u0010N\u001a\u00020\u00122\u0006\u0010O\u001a\u00020\u0012H\u0002J\u0010\u0010P\u001a\u0002012\u0006\u0010H\u001a\u00020:H\u0002J\u0018\u0010Q\u001a\u0002012\u0006\u0010N\u001a\u00020\u00122\u0006\u0010O\u001a\u00020\u0012H\u0002J\u0010\u0010R\u001a\u0002012\u0006\u0010S\u001a\u00020:H\u0002J\b\u0010T\u001a\u000201H\u0002J\u0010\u0010U\u001a\u0002012\u0006\u0010S\u001a\u00020:H\u0002J(\u0010V\u001a\u0002012\u0006\u0010W\u001a\u00020\u00122\u0006\u0010X\u001a\u0002052\u0006\u0010Y\u001a\u00020\u00162\u0006\u0010Z\u001a\u00020\u0014H\u0002J\u0010\u0010[\u001a\u0002012\u0006\u0010\\\u001a\u00020\u000bH\u0002J\b\u0010]\u001a\u000201H\u0002J\b\u0010^\u001a\u000201H\u0016R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0016X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\u0014X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010'\u001a\u00020(X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020(X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010*\u001a\u00020+X\u0082\u0004¢\u0006\u0004\n\u0002\u0010,R\u0010\u0010-\u001a\u00020.X\u0082\u0004¢\u0006\u0004\n\u0002\u0010/"}, d2 = {"Lcom/syed/lsfgmali/CaptureService;", "Landroid/app/Service;", "<init>", "()V", "projection", "Landroid/media/projection/MediaProjection;", "virtualDisplay", "Landroid/hardware/display/VirtualDisplay;", "imageReader", "Landroid/media/ImageReader;", "overlayView", "Landroid/view/SurfaceView;", "ioThread", "Landroid/os/HandlerThread;", "ioHandler", "Landroid/os/Handler;", "mainHandler", "statsTicks", "", "contextReady", "", "sessionFlow", "", "sessionScale", "sessionDensityDpi", "captureW", "captureH", "currentRotation", "reconfiguring", "displayListener", "Landroid/hardware/display/DisplayManager$DisplayListener;", "sessionActive", "tornDown", "overlayAttached", "overlayGeneration", "overlaySurfaceW", "overlaySurfaceH", "overlaySuppressedOwnApp", "overlayStarved", "lastUniqueCaptures", "", "lastCaptureAdvanceMs", "watchdogRunnable", "com/syed/lsfgmali/CaptureService$watchdogRunnable$1", "Lcom/syed/lsfgmali/CaptureService$watchdogRunnable$1;", "statsRunnable", "com/syed/lsfgmali/CaptureService$statsRunnable$1", "Lcom/syed/lsfgmali/CaptureService$statsRunnable$1;", "onCreate", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onStartCommand", "flags", "startId", "rotationName", "", "rotation", "computeCaptureGeometry", "Lkotlin/Triple;", "wm", "Landroid/view/WindowManager;", "initEngine", "width", "height", "createReader", "handler", "registerDisplayListener", "unregisterDisplayListener", "onRotationPossiblyChanged", "source", "reconfigure", "newW", "newH", "overlayLayoutParams", "Landroid/view/WindowManager$LayoutParams;", "w", "h", "repinWindowIfNeeded", "logOverlaySizeCheck", "createOverlay", "reason", "destroyOverlay", "refreshOverlayVisibility", "startSession", "resultCode", "data", "flow", "reduce", "attemptSkipScreenshot", "sv", "teardown", "onDestroy", "Companion"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class CaptureService extends Service {
    private static final String ACTION_START = "com.syed.lsfgmali.START";
    private static final String ACTION_STOP = "com.syed.lsfgmali.STOP";
    private static final String CHANNEL = "lsfgmali";

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final int NOTIF_ID = 42;
    private static final long STARVE_THRESHOLD_MS = 2000;
    private int captureH;
    private int captureW;
    private boolean contextReady;
    private int currentRotation;
    private DisplayManager.DisplayListener displayListener;
    private ImageReader imageReader;
    private Handler ioHandler;
    private HandlerThread ioThread;
    private long lastCaptureAdvanceMs;
    private boolean overlayAttached;
    private int overlayGeneration;
    private boolean overlayStarved;
    private boolean overlaySuppressedOwnApp;
    private int overlaySurfaceH;
    private int overlaySurfaceW;
    private SurfaceView overlayView;
    private MediaProjection projection;
    private boolean reconfiguring;
    private boolean sessionActive;
    private int sessionDensityDpi;
    private int statsTicks;
    private boolean tornDown;
    private VirtualDisplay virtualDisplay;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private float sessionFlow = 0.7f;
    private float sessionScale = 1.0f;
    private long lastUniqueCaptures = -1;
    private final CaptureService$watchdogRunnable$1 watchdogRunnable = new Runnable() { // from class: com.syed.lsfgmali.CaptureService$watchdogRunnable$1
        @Override // java.lang.Runnable
        public void run() {
            boolean z;
            boolean z2;
            boolean z3;
            Handler handler;
            boolean z4;
            Object m11constructorimpl;
            long j;
            long j2;
            boolean z5;
            long j3;
            long j4;
            boolean z6;
            z = CaptureService.this.sessionActive;
            if (z) {
                boolean ownApp = ForegroundState.INSTANCE.getMainActivityForeground();
                z2 = CaptureService.this.overlaySuppressedOwnApp;
                if (ownApp != z2) {
                    CaptureService.this.overlaySuppressedOwnApp = ownApp;
                    if (ownApp) {
                        LogBus.INSTANCE.log("overlay held: own app in foreground — overlay must not present over MainActivity");
                    } else {
                        LogBus.INSTANCE.log("own app backgrounded — overlay may present");
                    }
                    CaptureService.this.refreshOverlayVisibility(ownApp ? "own app foreground" : "own app backgrounded");
                }
                z3 = CaptureService.this.contextReady;
                if (z3) {
                    z4 = CaptureService.this.reconfiguring;
                    if (!z4) {
                        try {
                            Result.Companion companion = Result.INSTANCE;
                            CaptureService$watchdogRunnable$1 captureService$watchdogRunnable$1 = this;
                            m11constructorimpl = Result.m11constructorimpl(Long.valueOf(NativeBridge.INSTANCE.getUniqueCaptureCount()));
                        } catch (Throwable th) {
                            Result.Companion companion2 = Result.INSTANCE;
                            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
                        }
                        j = CaptureService.this.lastUniqueCaptures;
                        Long valueOf = Long.valueOf(j);
                        if (Result.m17isFailureimpl(m11constructorimpl)) {
                            m11constructorimpl = valueOf;
                        }
                        long unique = ((Number) m11constructorimpl).longValue();
                        long now = SystemClock.elapsedRealtime();
                        j2 = CaptureService.this.lastUniqueCaptures;
                        if (unique != j2) {
                            CaptureService.this.lastUniqueCaptures = unique;
                            CaptureService.this.lastCaptureAdvanceMs = now;
                            z6 = CaptureService.this.overlayStarved;
                            if (z6) {
                                CaptureService.this.overlayStarved = false;
                                LogBus.INSTANCE.log("captures resumed (unique=" + unique + ") — overlay can show again");
                                CaptureService.this.refreshOverlayVisibility("captures resumed");
                            }
                        } else {
                            z5 = CaptureService.this.overlayStarved;
                            if (!z5) {
                                j3 = CaptureService.this.lastCaptureAdvanceMs;
                                if (now - j3 >= 2000) {
                                    CaptureService.this.overlayStarved = true;
                                    LogBus logBus = LogBus.INSTANCE;
                                    j4 = CaptureService.this.lastCaptureAdvanceMs;
                                    logBus.log("overlay hidden: no new captures for " + ((now - j4) / 1000) + "s (unique stuck at " + unique + ") — content starved (game closed/backgrounded/static)");
                                    CaptureService.this.refreshOverlayVisibility("starved");
                                }
                            }
                        }
                    }
                }
                handler = CaptureService.this.mainHandler;
                handler.postDelayed(this, 1000L);
            }
        }
    };
    private final CaptureService$statsRunnable$1 statsRunnable = new Runnable() { // from class: com.syed.lsfgmali.CaptureService$statsRunnable$1
        @Override // java.lang.Runnable
        public void run() {
            boolean z;
            int i;
            int i2;
            Handler handler;
            z = CaptureService.this.contextReady;
            if (z) {
                i = CaptureService.this.statsTicks;
                CaptureService.this.statsTicks = i + 1;
                i2 = CaptureService.this.statsTicks;
                if (i2 % 5 == 1) {
                    try {
                        Result.Companion companion = Result.INSTANCE;
                        CaptureService$statsRunnable$1 captureService$statsRunnable$1 = this;
                        LogBus.INSTANCE.log("frames: generated=" + NativeBridge.INSTANCE.getGeneratedFrameCount() + " posted=" + NativeBridge.INSTANCE.getPostedFrameCount() + " unique-captures=" + NativeBridge.INSTANCE.getUniqueCaptureCount());
                        Result.m11constructorimpl(Unit.INSTANCE);
                    } catch (Throwable th) {
                        Result.Companion companion2 = Result.INSTANCE;
                        Result.m11constructorimpl(ResultKt.createFailure(th));
                    }
                }
                handler = CaptureService.this.mainHandler;
                handler.postDelayed(this, 1000L);
            }
        }
    };

    /* compiled from: CaptureService.kt */
    @Metadata(d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J.\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016J\u000e\u0010\u0017\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000"}, d2 = {"Lcom/syed/lsfgmali/CaptureService$Companion;", "", "<init>", "()V", "ACTION_START", "", "ACTION_STOP", "NOTIF_ID", "", "CHANNEL", "STARVE_THRESHOLD_MS", "", "start", "", "ctx", "Landroid/content/Context;", "resultCode", "data", "Landroid/content/Intent;", "flow", "", "reduce", "", "stop"}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final void start(Context ctx, int resultCode, Intent data, float flow, boolean reduce) {
            Intrinsics.checkNotNullParameter(ctx, "ctx");
            Intrinsics.checkNotNullParameter(data, "data");
            Intent $this$start_u24lambda_u240 = new Intent(ctx, (Class<?>) CaptureService.class);
            $this$start_u24lambda_u240.setAction(CaptureService.ACTION_START);
            $this$start_u24lambda_u240.putExtra("resultCode", resultCode);
            $this$start_u24lambda_u240.putExtra("data", data);
            $this$start_u24lambda_u240.putExtra("flow", flow);
            $this$start_u24lambda_u240.putExtra("reduce", reduce);
            ctx.startForegroundService($this$start_u24lambda_u240);
        }

        public final void stop(Context ctx) {
            Intrinsics.checkNotNullParameter(ctx, "ctx");
            Intent i = new Intent(ctx, (Class<?>) CaptureService.class);
            i.setAction(CaptureService.ACTION_STOP);
            ctx.startService(i);
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        LogSaver.INSTANCE.start(this);
        NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, "LSFG capture", 2));
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action.hashCode()) {
                case -732276663:
                    if (action.equals(ACTION_START)) {
                        int resultCode = intent.getIntExtra("resultCode", 0);
                        Intent data = (Intent) intent.getParcelableExtra("data");
                        float flow = intent.getFloatExtra("flow", 0.7f);
                        boolean reduce = intent.getBooleanExtra("reduce", false);
                        if (data == null) {
                            LogBus.INSTANCE.log("service start failed: no projection consent data");
                            stopSelf();
                            break;
                        } else {
                            startSession(resultCode, data, flow, reduce);
                            break;
                        }
                    }
                    break;
                case 1638946587:
                    if (action.equals(ACTION_STOP)) {
                        teardown();
                        stopSelf();
                        break;
                    }
                    break;
            }
            return 2;
        }
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String rotationName(int rotation) {
        switch (rotation) {
            case 0:
                return "0 (portrait)";
            case 1:
                return "90 (landscape)";
            case 2:
                return "180 (portrait, inverted)";
            case 3:
                return "270 (landscape, inverted)";
            default:
                return String.valueOf(rotation);
        }
    }

    private final Triple<Integer, Integer, Integer> computeCaptureGeometry(WindowManager wm) {
        int logicalW;
        int logicalH;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        int rotation = wm.getDefaultDisplay().getRotation();
        int natW = Math.min(metrics.widthPixels, metrics.heightPixels);
        int natH = Math.max(metrics.widthPixels, metrics.heightPixels);
        switch (rotation) {
            case 1:
            case 3:
                logicalW = natH;
                break;
            case 2:
            default:
                logicalW = natW;
                break;
        }
        switch (rotation) {
            case 1:
            case 3:
                logicalH = natW;
                break;
            case 2:
            default:
                logicalH = natH;
                break;
        }
        int cw = (int) (logicalW * this.sessionScale);
        int ch = (int) (logicalH * this.sessionScale);
        if (cw % 2 == 1) {
            cw--;
        }
        if (ch % 2 == 1) {
            ch--;
        }
        return new Triple<>(Integer.valueOf(cw), Integer.valueOf(ch), Integer.valueOf(rotation));
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x00b3  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00e0  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0125  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final int initEngine(int width, int height) {
        Object m11constructorimpl;
        boolean fp16;
        Object m11constructorimpl2;
        Throwable it;
        int rc;
        String cache = Paths.INSTANCE.shaderCache(this);
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService captureService = this;
            m11constructorimpl = Result.m11constructorimpl(Boolean.valueOf(NativeBridge.INSTANCE.isFramegenFp16Supported(cache)));
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        if (Result.m17isFailureimpl(m11constructorimpl)) {
            m11constructorimpl = false;
        }
        boolean fp162 = ((Boolean) m11constructorimpl).booleanValue();
        try {
            Result.Companion companion3 = Result.INSTANCE;
            CaptureService $this$initEngine_u24lambda_u241 = this;
            fp16 = fp162;
            try {
                m11constructorimpl2 = Result.m11constructorimpl(Integer.valueOf(NativeBridge.INSTANCE.initContext(cache, width, height, 2, $this$initEngine_u24lambda_u241.sessionFlow, true, false, false, fp162, false, 0, 0, 0.0f, 0.0f, 0.0f, false, false, 0, 0.0f, 0.0f, 0.0f, 0.0f, false, 0, 0, 0.0f, 0.0f, 0.0f, 0, 0.125f, 4.0f, 2.0f, 4)));
            } catch (Throwable th2) {
                th = th2;
                Result.Companion companion4 = Result.INSTANCE;
                m11constructorimpl2 = Result.m11constructorimpl(ResultKt.createFailure(th));
                it = Result.m14exceptionOrNullimpl(m11constructorimpl2);
                if (it != null) {
                }
                rc = ((Number) m11constructorimpl2).intValue();
                if (rc != 0) {
                }
                return rc;
            }
        } catch (Throwable th3) {
            th = th3;
            fp16 = fp162;
        }
        it = Result.m14exceptionOrNullimpl(m11constructorimpl2);
        if (it != null) {
            LogBus.INSTANCE.log("initContext threw: " + it.getMessage());
            m11constructorimpl2 = -999;
        }
        rc = ((Number) m11constructorimpl2).intValue();
        if (rc != 0) {
            LogBus.INSTANCE.log("engine initialised (2x, 3.1P, flow=" + this.sessionFlow + ", fp16=" + fp16 + ", size " + width + "x" + height + ")");
        }
        return rc;
    }

    private final ImageReader createReader(int width, int height, Handler handler) {
        ImageReader reader = ImageReader.newInstance(width, height, 1, 4);
        Intrinsics.checkNotNullExpressionValue(reader, "newInstance(...)");
        reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { // from class: com.syed.lsfgmali.CaptureService$$ExternalSyntheticLambda0
            @Override // android.media.ImageReader.OnImageAvailableListener
            public final void onImageAvailable(ImageReader imageReader) {
                CaptureService.createReader$lambda$4(CaptureService.this, imageReader);
            }
        }, handler);
        return reader;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void createReader$lambda$4(CaptureService this$0, ImageReader r) {
        Object m11constructorimpl;
        try {
            Result.Companion companion = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(r.acquireLatestImage());
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        if (Result.m17isFailureimpl(m11constructorimpl)) {
            m11constructorimpl = null;
        }
        Image image = (Image) m11constructorimpl;
        if (image == null) {
            return;
        }
        try {
            try {
                HardwareBuffer hb = image.getHardwareBuffer();
                if (hb != null && this$0.contextReady) {
                    NativeBridge.INSTANCE.pushFrame(hb, image.getTimestamp());
                }
            } catch (Exception e) {
                LogBus.INSTANCE.log("pushFrame failed: " + e.getMessage());
            }
        } finally {
            image.close();
        }
    }

    private final void registerDisplayListener() {
        if (this.displayListener != null) {
            return;
        }
        DisplayManager dm = (DisplayManager) getSystemService(DisplayManager.class);
        DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() { // from class: com.syed.lsfgmali.CaptureService$registerDisplayListener$listener$1
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int displayId) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int displayId) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    CaptureService.this.onRotationPossiblyChanged("displayListener");
                }
            }
        };
        this.displayListener = displayListener;
        dm.registerDisplayListener(displayListener, this.mainHandler);
    }

    private final void unregisterDisplayListener() {
        DisplayManager.DisplayListener listener = this.displayListener;
        if (listener == null) {
            return;
        }
        this.displayListener = null;
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService $this$unregisterDisplayListener_u24lambda_u245 = this;
            ((DisplayManager) $this$unregisterDisplayListener_u24lambda_u245.getSystemService(DisplayManager.class)).unregisterDisplayListener(listener);
            Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onRotationPossiblyChanged(String source) {
        if (this.virtualDisplay == null || !this.contextReady || this.reconfiguring) {
            return;
        }
        WindowManager wm = (WindowManager) getSystemService(WindowManager.class);
        Intrinsics.checkNotNull(wm);
        Triple<Integer, Integer, Integer> computeCaptureGeometry = computeCaptureGeometry(wm);
        int nw = computeCaptureGeometry.component1().intValue();
        int nh = computeCaptureGeometry.component2().intValue();
        int rotation = computeCaptureGeometry.component3().intValue();
        if (nw == this.captureW && nh == this.captureH) {
            if (rotation != this.currentRotation) {
                LogBus.INSTANCE.log("rotation event (" + source + "): rotation=" + rotationName(rotation) + " — capture size unchanged " + this.captureW + "x" + this.captureH);
                this.currentRotation = rotation;
                return;
            }
            return;
        }
        reconfigure(nw, nh, rotation, source);
    }

    private final void reconfigure(int newW, int newH, int rotation, String source) {
        Object m11constructorimpl;
        Unit unit;
        Object m11constructorimpl2;
        Unit unit2;
        this.reconfiguring = true;
        try {
        } catch (Throwable th) {
            th = th;
        }
        try {
            LogBus.INSTANCE.log("rotation change (" + source + "): rotation " + rotationName(this.currentRotation) + " -> " + rotationName(rotation) + ", capture " + this.captureW + "x" + this.captureH + " -> " + newW + "x" + newH + " — reconfiguring session");
            this.contextReady = false;
            destroyOverlay();
            try {
                Result.Companion companion = Result.INSTANCE;
                CaptureService $this$reconfigure_u24lambda_u246 = this;
                VirtualDisplay virtualDisplay = $this$reconfigure_u24lambda_u246.virtualDisplay;
                if (virtualDisplay != null) {
                    virtualDisplay.setSurface(null);
                }
                Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th2) {
                Result.Companion companion2 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th2));
            }
            try {
                Result.Companion companion3 = Result.INSTANCE;
                CaptureService $this$reconfigure_u24lambda_u247 = this;
                ImageReader imageReader = $this$reconfigure_u24lambda_u247.imageReader;
                if (imageReader != null) {
                    imageReader.close();
                    unit2 = Unit.INSTANCE;
                } else {
                    unit2 = null;
                }
                Result.m11constructorimpl(unit2);
            } catch (Throwable th3) {
                Result.Companion companion4 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th3));
            }
            this.imageReader = null;
            try {
                Result.Companion companion5 = Result.INSTANCE;
                CaptureService captureService = this;
                NativeBridge.INSTANCE.destroyContext();
                Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th4) {
                Result.Companion companion6 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th4));
            }
            int rc = initEngine(newW, newH);
            if (rc != 0) {
                LogBus.INSTANCE.log("INIT FAILED rc=" + rc + " after rotation reconfigure — engine did not restart. Send back: this log + logcat lines tagged 'lsfg-vk-framegen'. Stopping the session.");
                teardown();
                stopSelf();
                this.reconfiguring = false;
                return;
            }
            this.captureW = newW;
            this.captureH = newH;
            this.currentRotation = rotation;
            this.lastUniqueCaptures = -1L;
            this.lastCaptureAdvanceMs = SystemClock.elapsedRealtime();
            this.overlayStarved = false;
            refreshOverlayVisibility("rotation reconfigure");
            Handler handler = this.ioHandler;
            if (handler == null) {
                LogBus.INSTANCE.log("rotation reconfigure aborted: capture IO handler gone");
                teardown();
                stopSelf();
                this.reconfiguring = false;
                return;
            }
            ImageReader reader = createReader(this.captureW, this.captureH, handler);
            this.imageReader = reader;
            boolean recreated = false;
            MediaProjection proj = this.projection;
            if (proj != null) {
                try {
                    Result.Companion companion7 = Result.INSTANCE;
                    CaptureService $this$reconfigure_u24lambda_u2410 = this;
                    VirtualDisplay newVd = proj.createVirtualDisplay(CHANNEL, $this$reconfigure_u24lambda_u2410.captureW, $this$reconfigure_u24lambda_u2410.captureH, $this$reconfigure_u24lambda_u2410.sessionDensityDpi, 16, reader.getSurface(), null, handler);
                    if (newVd != null) {
                        try {
                            Result.Companion companion8 = Result.INSTANCE;
                            VirtualDisplay virtualDisplay2 = $this$reconfigure_u24lambda_u2410.virtualDisplay;
                            if (virtualDisplay2 != null) {
                                virtualDisplay2.release();
                                unit = Unit.INSTANCE;
                            } else {
                                unit = null;
                            }
                            Result.m11constructorimpl(unit);
                        } catch (Throwable th5) {
                            Result.Companion companion9 = Result.INSTANCE;
                            Result.m11constructorimpl(ResultKt.createFailure(th5));
                        }
                        $this$reconfigure_u24lambda_u2410.virtualDisplay = newVd;
                        recreated = true;
                        LogBus.INSTANCE.log("virtual display recreated at " + $this$reconfigure_u24lambda_u2410.captureW + "x" + $this$reconfigure_u24lambda_u2410.captureH + " (rotation reconfigure — never resized)");
                    } else {
                        LogBus.INSTANCE.log("virtual display recreate returned null during reconfigure — falling back to resize");
                    }
                    m11constructorimpl = Result.m11constructorimpl(Unit.INSTANCE);
                } catch (Throwable th6) {
                    Result.Companion companion10 = Result.INSTANCE;
                    m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th6));
                }
                Throwable it = Result.m14exceptionOrNullimpl(m11constructorimpl);
                if (it != null) {
                    LogBus.INSTANCE.log("virtual display recreate failed during reconfigure: " + it.getMessage() + " — falling back to resize");
                }
                Result.m10boximpl(m11constructorimpl);
            } else {
                LogBus.INSTANCE.log("virtual display recreate skipped during reconfigure: no MediaProjection — falling back to resize");
            }
            if (!recreated) {
                try {
                    Result.Companion companion11 = Result.INSTANCE;
                    CaptureService $this$reconfigure_u24lambda_u2412 = this;
                    VirtualDisplay virtualDisplay3 = $this$reconfigure_u24lambda_u2412.virtualDisplay;
                    if (virtualDisplay3 != null) {
                        virtualDisplay3.resize($this$reconfigure_u24lambda_u2412.captureW, $this$reconfigure_u24lambda_u2412.captureH, $this$reconfigure_u24lambda_u2412.sessionDensityDpi);
                    }
                    VirtualDisplay virtualDisplay4 = $this$reconfigure_u24lambda_u2412.virtualDisplay;
                    if (virtualDisplay4 != null) {
                        virtualDisplay4.setSurface(reader.getSurface());
                    }
                    m11constructorimpl2 = Result.m11constructorimpl(Unit.INSTANCE);
                } catch (Throwable th7) {
                    Result.Companion companion12 = Result.INSTANCE;
                    m11constructorimpl2 = Result.m11constructorimpl(ResultKt.createFailure(th7));
                }
                Throwable it2 = Result.m14exceptionOrNullimpl(m11constructorimpl2);
                if (it2 != null) {
                    LogBus.INSTANCE.log("virtualDisplay resize failed: " + it2.getMessage());
                }
            }
            try {
                Result.Companion companion13 = Result.INSTANCE;
                CaptureService $this$reconfigure_u24lambda_u2414 = this;
                WindowManager wm = (WindowManager) $this$reconfigure_u24lambda_u2414.getSystemService(WindowManager.class);
                float rate = wm.getDefaultDisplay().getRefreshRate();
                if (rate > 0.0f) {
                    NativeBridge.INSTANCE.setVsyncPeriodNs((long) (1.0E9d / rate));
                }
                Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th8) {
                Result.Companion companion14 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th8));
            }
            this.contextReady = true;
            LogBus.INSTANCE.log("rotation reconfigure complete: rotation=" + rotationName(this.currentRotation) + ", capture " + this.captureW + "x" + this.captureH + ", overlay attached=" + this.overlayAttached + ", overlay window pinned " + this.captureW + "x" + this.captureH);
            this.reconfiguring = false;
        } catch (Throwable th9) {
            th = th9;
            this.reconfiguring = false;
            throw th;
        }
    }

    private final WindowManager.LayoutParams overlayLayoutParams(int w, int h) {
        int i;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(w, h, 2038, 280, -1);
        lp.gravity = 51;
        lp.x = 0;
        lp.y = 0;
        if (Build.VERSION.SDK_INT >= 30) {
            i = 3;
        } else {
            i = 1;
        }
        lp.layoutInDisplayCutoutMode = i;
        return lp;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void repinWindowIfNeeded(String source) {
        Object m11constructorimpl;
        SurfaceView v = this.overlayView;
        if (v == null) {
            return;
        }
        WindowManager wm = (WindowManager) getSystemService(WindowManager.class);
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        WindowManager.LayoutParams lp = layoutParams instanceof WindowManager.LayoutParams ? (WindowManager.LayoutParams) layoutParams : null;
        if (lp == null) {
            return;
        }
        LogBus.INSTANCE.log("overlay window params (" + source + "): window " + lp.width + "x" + lp.height + ", view " + v.getWidth() + "x" + v.getHeight() + ", rotation=" + rotationName(this.currentRotation) + ", capture " + this.captureW + "x" + this.captureH);
        if (lp.width != this.captureW || lp.height != this.captureH) {
            try {
                Result.Companion companion = Result.INSTANCE;
                CaptureService $this$repinWindowIfNeeded_u24lambda_u2415 = this;
                wm.updateViewLayout(v, $this$repinWindowIfNeeded_u24lambda_u2415.overlayLayoutParams($this$repinWindowIfNeeded_u24lambda_u2415.captureW, $this$repinWindowIfNeeded_u24lambda_u2415.captureH));
                LogBus.INSTANCE.log("overlay window re-pinned (" + source + "): " + lp.width + "x" + lp.height + " -> " + $this$repinWindowIfNeeded_u24lambda_u2415.captureW + "x" + $this$repinWindowIfNeeded_u24lambda_u2415.captureH);
                m11constructorimpl = Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
            }
            Throwable it = Result.m14exceptionOrNullimpl(m11constructorimpl);
            if (it != null) {
                LogBus.INSTANCE.log("overlay window re-pin failed (" + source + "): " + it.getMessage());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void logOverlaySizeCheck(int w, int h) {
        if (w == this.captureW && h == this.captureH) {
            LogBus.INSTANCE.log("overlay size check: OK — surface " + w + "x" + h + " == capture " + this.captureW + "x" + this.captureH);
            return;
        }
        LogBus.INSTANCE.log("overlay size check: MISMATCH — surface " + w + "x" + h + " vs capture " + this.captureW + "x" + this.captureH);
    }

    private final void createOverlay(final String reason) {
        Object m11constructorimpl;
        if (this.overlayView != null) {
            destroyOverlay();
        }
        boolean focusable = true;
        this.overlayGeneration++;
        final int gen = this.overlayGeneration;
        final SurfaceView sv = new SurfaceView(this);
        this.overlayView = sv;
        WindowManager.LayoutParams lp = overlayLayoutParams(this.captureW, this.captureH);
        sv.getHolder().addCallback(new SurfaceHolder.Callback() { // from class: com.syed.lsfgmali.CaptureService$createOverlay$1
            @Override // android.view.SurfaceHolder.Callback
            public void surfaceCreated(SurfaceHolder holder) {
                int i;
                Object m11constructorimpl2;
                int i2;
                int i3;
                int i4;
                int i5;
                Intrinsics.checkNotNullParameter(holder, "holder");
                int i6 = gen;
                i = this.overlayGeneration;
                if (i6 != i) {
                    return;
                }
                this.repinWindowIfNeeded("surfaceCreated");
                CaptureService captureService = this;
                int i7 = gen;
                String str = reason;
                try {
                    Result.Companion companion = Result.INSTANCE;
                    CaptureService$createOverlay$1 captureService$createOverlay$1 = this;
                    NativeBridge nativeBridge = NativeBridge.INSTANCE;
                    Surface surface = holder.getSurface();
                    i2 = captureService.captureW;
                    i3 = captureService.captureH;
                    nativeBridge.setOutputSurface(surface, i2, i3);
                    LogBus logBus = LogBus.INSTANCE;
                    i4 = captureService.captureW;
                    i5 = captureService.captureH;
                    logBus.log("output surface attached at " + i4 + "x" + i5 + " (fresh surface gen=" + i7 + ", " + str + ")");
                    m11constructorimpl2 = Result.m11constructorimpl(Unit.INSTANCE);
                } catch (Throwable th) {
                    Result.Companion companion2 = Result.INSTANCE;
                    m11constructorimpl2 = Result.m11constructorimpl(ResultKt.createFailure(th));
                }
                Throwable it = Result.m14exceptionOrNullimpl(m11constructorimpl2);
                if (it != null) {
                    LogBus.INSTANCE.log("setOutputSurface failed: " + it.getMessage());
                }
                this.attemptSkipScreenshot(sv);
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
                int i;
                int i2;
                int i3;
                int i4;
                String rotationName;
                boolean z;
                boolean z2;
                int i5;
                int i6;
                int i7;
                int i8;
                int i9;
                String rotationName2;
                int i10;
                Intrinsics.checkNotNullParameter(holder, "holder");
                int i11 = gen;
                i = this.overlayGeneration;
                if (i11 != i) {
                    return;
                }
                this.overlaySurfaceW = w;
                this.overlaySurfaceH = h;
                LogBus logBus = LogBus.INSTANCE;
                i2 = this.captureW;
                i3 = this.captureH;
                CaptureService captureService = this;
                i4 = this.currentRotation;
                rotationName = captureService.rotationName(i4);
                logBus.log("overlay surface changed: " + w + "x" + h + " (capture " + i2 + "x" + i3 + ", rotation=" + rotationName + ")");
                this.logOverlaySizeCheck(w, h);
                this.repinWindowIfNeeded("surfaceChanged");
                WindowManager wm = (WindowManager) this.getSystemService(WindowManager.class);
                int rotNow = wm.getDefaultDisplay().getRotation();
                z = this.contextReady;
                if (z) {
                    i10 = this.currentRotation;
                    if (rotNow != i10) {
                        this.onRotationPossiblyChanged("surfaceChanged");
                        return;
                    }
                }
                z2 = this.contextReady;
                if (z2) {
                    boolean z3 = w > h;
                    i5 = this.captureW;
                    i6 = this.captureH;
                    if (z3 != (i5 > i6)) {
                        LogBus logBus2 = LogBus.INSTANCE;
                        i7 = this.captureW;
                        i8 = this.captureH;
                        CaptureService captureService2 = this;
                        i9 = this.currentRotation;
                        rotationName2 = captureService2.rotationName(i9);
                        logBus2.log("surface size-only change ignored (no rotation change): surface " + w + "x" + h + " vs capture " + i7 + "x" + i8 + ", rotation still " + rotationName2);
                    }
                }
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceDestroyed(SurfaceHolder holder) {
                int i;
                Intrinsics.checkNotNullParameter(holder, "holder");
                int i2 = gen;
                i = this.overlayGeneration;
                if (i2 != i) {
                    return;
                }
                try {
                    Result.Companion companion = Result.INSTANCE;
                    CaptureService$createOverlay$1 captureService$createOverlay$1 = this;
                    NativeBridge.INSTANCE.setOutputSurface(null, 0, 0);
                    Result.m11constructorimpl(Unit.INSTANCE);
                } catch (Throwable th) {
                    Result.Companion companion2 = Result.INSTANCE;
                    Result.m11constructorimpl(ResultKt.createFailure(th));
                }
            }
        });
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService $this$createOverlay_u24lambda_u2417 = this;
            ((WindowManager) $this$createOverlay_u24lambda_u2417.getSystemService(WindowManager.class)).addView(sv, lp);
            $this$createOverlay_u24lambda_u2417.overlayAttached = true;
            LogBus.INSTANCE.log("overlay created (" + reason + "): fresh SurfaceView, window pinned " + lp.width + "x" + lp.height + ", rotation=" + $this$createOverlay_u24lambda_u2417.rotationName($this$createOverlay_u24lambda_u2417.currentRotation));
            boolean touchable = (lp.flags & 16) != 0;
            if ((lp.flags & 8) == 0) {
                focusable = false;
            }
            LogBus.INSTANCE.log("overlay flags: NOT_TOUCHABLE=" + touchable + ", NOT_FOCUSABLE=" + focusable + " (flags=0x" + Integer.toHexString(lp.flags) + ") — touches and focus pass through to the game");
            m11constructorimpl = Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        Throwable it = Result.m14exceptionOrNullimpl(m11constructorimpl);
        if (it != null) {
            this.overlayView = null;
            this.overlayAttached = false;
            LogBus.INSTANCE.log("overlay addView failed: " + it.getMessage());
        }
    }

    private final void destroyOverlay() {
        Object m11constructorimpl;
        SurfaceView v = this.overlayView;
        if (v == null) {
            return;
        }
        this.overlayGeneration++;
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService captureService = this;
            NativeBridge.INSTANCE.setOutputSurface(null, 0, 0);
            Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        WindowManager wm = (WindowManager) getSystemService(WindowManager.class);
        try {
            Result.Companion companion3 = Result.INSTANCE;
            CaptureService captureService2 = this;
            wm.removeViewImmediate(v);
            m11constructorimpl = Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th2) {
            Result.Companion companion4 = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th2));
        }
        if (Result.m14exceptionOrNullimpl(m11constructorimpl) != null) {
            try {
                Result.Companion companion5 = Result.INSTANCE;
                CaptureService captureService3 = this;
                wm.removeView(v);
                Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th3) {
                Result.Companion companion6 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th3));
            }
        }
        this.overlayView = null;
        this.overlayAttached = false;
        this.overlaySurfaceW = 0;
        this.overlaySurfaceH = 0;
        LogBus.INSTANCE.log("overlay destroyed: view removed, surface released — no stale frame can persist");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void refreshOverlayVisibility(String reason) {
        if (this.sessionActive) {
            boolean desired = (this.overlaySuppressedOwnApp || this.overlayStarved) ? false : true;
            if (desired && !this.overlayAttached) {
                createOverlay(reason);
            } else if (!desired && this.overlayAttached) {
                destroyOverlay();
                LogBus.INSTANCE.log("overlay hidden (" + reason + ")");
            }
        }
    }

    private final void startSession(int resultCode, Intent data, float flow, boolean reduce) {
        this.tornDown = false;
        Notification notif = new Notification.Builder(this, CHANNEL).setContentTitle("LSFG-for-Mali").setContentText("Frame generation running (2x)").setSmallIcon(android.R.drawable.ic_dialog_info).build();
        Intrinsics.checkNotNullExpressionValue(notif, "build(...)");
        startForeground(NOTIF_ID, notif, 32);
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MediaProjectionManager.class);
        MediaProjection proj = mpm.getMediaProjection(resultCode, data);
        this.projection = proj;
        proj.registerCallback(new MediaProjection.Callback() { // from class: com.syed.lsfgmali.CaptureService$startSession$1
            @Override // android.media.projection.MediaProjection.Callback
            public void onStop() {
                LogBus.INSTANCE.log("projection stopped by system/user");
                CaptureService.this.teardown();
                CaptureService.this.stopSelf();
            }
        }, this.mainHandler);
        this.sessionFlow = flow;
        this.sessionScale = reduce ? 0.75f : 1.0f;
        WindowManager wm = (WindowManager) getSystemService(WindowManager.class);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        this.sessionDensityDpi = metrics.densityDpi;
        Intrinsics.checkNotNull(wm);
        Triple<Integer, Integer, Integer> computeCaptureGeometry = computeCaptureGeometry(wm);
        int cw = computeCaptureGeometry.component1().intValue();
        int ch = computeCaptureGeometry.component2().intValue();
        int rotation = computeCaptureGeometry.component3().intValue();
        this.captureW = cw;
        this.captureH = ch;
        this.currentRotation = rotation;
        LogBus.INSTANCE.log("session start: rotation=" + rotationName(rotation) + ", display " + metrics.widthPixels + "x" + metrics.heightPixels + ", capture size " + cw + "x" + ch + " (scale " + this.sessionScale + ")");
        int rc = initEngine(cw, ch);
        if (rc == 0) {
            this.contextReady = true;
            try {
                Result.Companion companion = Result.INSTANCE;
                CaptureService captureService = this;
                float rate = wm.getDefaultDisplay().getRefreshRate();
                if (rate > 0.0f) {
                    NativeBridge.INSTANCE.setVsyncPeriodNs((long) (1.0E9d / rate));
                }
                Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th));
            }
            this.mainHandler.postDelayed(this.statsRunnable, 1000L);
            HandlerThread $this$startSession_u24lambda_u2424 = new HandlerThread("capture-io");
            $this$startSession_u24lambda_u2424.start();
            this.ioThread = $this$startSession_u24lambda_u2424;
            HandlerThread handlerThread = this.ioThread;
            Intrinsics.checkNotNull(handlerThread);
            Handler handler = new Handler(handlerThread.getLooper());
            this.ioHandler = handler;
            ImageReader reader = createReader(cw, ch, handler);
            this.imageReader = reader;
            this.virtualDisplay = proj.createVirtualDisplay(CHANNEL, cw, ch, this.sessionDensityDpi, 16, reader.getSurface(), null, handler);
            LogBus.INSTANCE.log("virtual display created at " + cw + "x" + ch);
            registerDisplayListener();
            this.sessionActive = true;
            this.overlayStarved = false;
            this.lastUniqueCaptures = -1L;
            this.lastCaptureAdvanceMs = SystemClock.elapsedRealtime();
            this.overlaySuppressedOwnApp = ForegroundState.INSTANCE.getMainActivityForeground();
            if (this.overlaySuppressedOwnApp) {
                LogBus.INSTANCE.log("overlay held at session start: own app in foreground");
            }
            refreshOverlayVisibility("session start");
            this.mainHandler.postDelayed(this.watchdogRunnable, 1000L);
            LogBus.INSTANCE.log("capture running — switch to your game/video. Overlay shows the generated output.");
            return;
        }
        LogBus.INSTANCE.log("INIT FAILED rc=" + rc + " — engine did not start. Send back: this log + logcat lines tagged 'lsfg-vk-framegen' (the 'Device features probe:' line names the failing gate). If PanVK was selected, also try Stock (restart the app after switching).");
        teardown();
        stopSelf();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void attemptSkipScreenshot(SurfaceView sv) {
        try {
            SurfaceControl sc = sv.getSurfaceControl();
            if (sc == null) {
                CaptureService captureService = this;
                LogBus.INSTANCE.log("skip-screenshot: no SurfaceControl on overlay (loop mitigation unavailable)");
                return;
            }
            Class txnClass = Class.forName("android.view.SurfaceControl$Transaction");
            Object txn = txnClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            Method m = txnClass.getDeclaredMethod("setSkipScreenshot", Class.forName("android.view.SurfaceControl"), Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(txn, sc, true);
            txnClass.getMethod("apply", new Class[0]).invoke(txn, new Object[0]);
            LogBus.INSTANCE.log("skip-screenshot: overlay excluded from capture ✓");
        } catch (Exception e) {
            LogBus.INSTANCE.log("skip-screenshot unavailable on this ROM (" + e.getClass().getSimpleName() + ") — if the picture fades to black, that's the known MediaTek feedback loop (F2.15); note it in your report");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void teardown() {
        Unit unit;
        Unit unit2;
        Unit unit3;
        if (this.tornDown) {
            return;
        }
        this.tornDown = true;
        this.sessionActive = false;
        this.mainHandler.removeCallbacks(this.watchdogRunnable);
        this.mainHandler.removeCallbacks(this.statsRunnable);
        this.contextReady = false;
        unregisterDisplayListener();
        destroyOverlay();
        LogBus.INSTANCE.log("teardown: overlay removed");
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService $this$teardown_u24lambda_u2426 = this;
            VirtualDisplay virtualDisplay = $this$teardown_u24lambda_u2426.virtualDisplay;
            if (virtualDisplay != null) {
                virtualDisplay.release();
                unit3 = Unit.INSTANCE;
            } else {
                unit3 = null;
            }
            Result.m11constructorimpl(unit3);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        this.virtualDisplay = null;
        try {
            Result.Companion companion3 = Result.INSTANCE;
            CaptureService $this$teardown_u24lambda_u2427 = this;
            ImageReader imageReader = $this$teardown_u24lambda_u2427.imageReader;
            if (imageReader != null) {
                imageReader.close();
                unit2 = Unit.INSTANCE;
            } else {
                unit2 = null;
            }
            Result.m11constructorimpl(unit2);
        } catch (Throwable th2) {
            Result.Companion companion4 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th2));
        }
        this.imageReader = null;
        LogBus.INSTANCE.log("teardown: virtual display released");
        try {
            Result.Companion companion5 = Result.INSTANCE;
            CaptureService captureService = this;
            NativeBridge.INSTANCE.destroyContext();
            Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th3) {
            Result.Companion companion6 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th3));
        }
        LogBus.INSTANCE.log("teardown: engine context destroyed");
        try {
            Result.Companion companion7 = Result.INSTANCE;
            CaptureService $this$teardown_u24lambda_u2429 = this;
            MediaProjection mediaProjection = $this$teardown_u24lambda_u2429.projection;
            if (mediaProjection != null) {
                mediaProjection.stop();
                unit = Unit.INSTANCE;
            } else {
                unit = null;
            }
            Result.m11constructorimpl(unit);
        } catch (Throwable th4) {
            Result.Companion companion8 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th4));
        }
        this.projection = null;
        HandlerThread handlerThread = this.ioThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
        this.ioThread = null;
        this.ioHandler = null;
        try {
            Result.Companion companion9 = Result.INSTANCE;
            CaptureService $this$teardown_u24lambda_u2430 = this;
            $this$teardown_u24lambda_u2430.stopForeground(1);
            Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th5) {
            Result.Companion companion10 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th5));
        }
        LogBus.INSTANCE.log("teardown complete: overlay removed -> virtual display released -> engine stopped");
        LogBus.INSTANCE.log("session stopped");
    }

    @Override // android.app.Service
    public void onDestroy() {
        teardown();
        super.onDestroy();
    }
}
