package com.lsfg.android.session;

import android.hardware.HardwareBuffer;
import android.view.Surface;
import kotlin.Metadata;

/* compiled from: NativeBridge.kt */
@Metadata(d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0002\b \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u0016\n\u0002\b\u000e\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\t\u0010\u0004\u001a\u00020\u0005H\u0086 J\u0019\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\u0005H\u0086 J!\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u000e\u001a\u00020\u0005H\u0086 J\u0011\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u0005H\u0086 J\u0091\u0002\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u00172\u0006\u0010\u001b\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u000b2\u0006\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u001f\u001a\u00020\u00152\u0006\u0010 \u001a\u00020\u00152\u0006\u0010!\u001a\u00020\u00172\u0006\u0010\"\u001a\u00020\u00172\u0006\u0010#\u001a\u00020\u000b2\u0006\u0010$\u001a\u00020\u00152\u0006\u0010%\u001a\u00020\u00152\u0006\u0010&\u001a\u00020\u00152\u0006\u0010'\u001a\u00020\u00152\u0006\u0010(\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u000b2\u0006\u0010*\u001a\u00020\u000b2\u0006\u0010+\u001a\u00020\u00152\u0006\u0010,\u001a\u00020\u00152\u0006\u0010-\u001a\u00020\u00152\u0006\u0010.\u001a\u00020\u000b2\u0006\u0010/\u001a\u00020\u00152\u0006\u00100\u001a\u00020\u00152\u0006\u00101\u001a\u00020\u00152\u0006\u00102\u001a\u00020\u000bH\u0086 J\t\u00103\u001a\u00020\u0017H\u0086 J\u0011\u00104\u001a\u00020\u00172\u0006\u0010\u000e\u001a\u00020\u0005H\u0086 J\t\u00105\u001a\u00020\u0005H\u0086 J#\u00106\u001a\u00020\u00072\b\u00107\u001a\u0004\u0018\u0001082\u0006\u00109\u001a\u00020\u000b2\u0006\u0010:\u001a\u00020\u000bH\u0086 J\u0019\u0010;\u001a\u00020\u00072\u0006\u0010<\u001a\u00020=2\u0006\u0010>\u001a\u00020?H\u0086 J\t\u0010@\u001a\u00020?H\u0086 J\t\u0010A\u001a\u00020?H\u0086 J\t\u0010B\u001a\u00020?H\u0086 J\t\u0010C\u001a\u00020DH\u0086 J\t\u0010E\u001a\u00020DH\u0086 J\u0011\u0010F\u001a\u00020\u000b2\u0006\u0010G\u001a\u00020HH\u0086 J\u0011\u0010I\u001a\u00020\u000b2\u0006\u0010J\u001a\u00020HH\u0086 J\u0011\u0010K\u001a\u00020\u00072\u0006\u0010L\u001a\u00020\u0017H\u0086 J\u0011\u0010M\u001a\u00020\u00072\u0006\u0010N\u001a\u00020\u0017H\u0086 J\u0011\u0010O\u001a\u00020\u00072\u0006\u0010P\u001a\u00020?H\u0086 J1\u0010Q\u001a\u00020\u00072\u0006\u0010.\u001a\u00020\u000b2\u0006\u0010/\u001a\u00020\u00152\u0006\u00100\u001a\u00020\u00152\u0006\u00101\u001a\u00020\u00152\u0006\u00102\u001a\u00020\u000bH\u0086 J\u0011\u0010R\u001a\u00020\u00072\u0006\u0010N\u001a\u00020\u0017H\u0086 J!\u0010S\u001a\u00020\u00072\u0006\u0010>\u001a\u00020?2\u0006\u0010T\u001a\u00020?2\u0006\u0010U\u001a\u00020?H\u0086 J\t\u0010V\u001a\u00020\u0007H\u0086 "}, d2 = {"Lcom/lsfg/android/session/NativeBridge;", "", "<init>", "()V", "nativeVersion", "", "initCrashReporter", "", "crashPath", "logPath", "extractShaders", "", "dllPath", "dllSha256", "cacheDir", "probeShaders", "initContext", "width", "height", "multiplier", "flowScale", "", "performance", "", "hdr", "antiArtifacts", "framegenFp16", "npuPostProcessing", "npuPreset", "npuUpscaleFactor", "npuAmount", "npuRadius", "npuThreshold", "npuFp16", "cpuPostProcessing", "cpuPreset", "cpuStrength", "cpuSaturation", "cpuVibrance", "cpuVignette", "gpuPostProcessing", "gpuStage", "gpuMethod", "gpuUpscaleFactor", "gpuSharpness", "gpuStrength", "targetFpsCap", "emaAlpha", "outlierRatio", "vsyncSlackMs", "queueDepth", "isNpuAvailable", "isFramegenFp16Supported", "getNpuSummary", "setOutputSurface", "surface", "Landroid/view/Surface;", "w", "h", "pushFrame", "hardwareBuffer", "Landroid/hardware/HardwareBuffer;", "timestampNs", "", "getGeneratedFrameCount", "getPostedFrameCount", "getUniqueCaptureCount", "getAverageQueueMs", "", "getAverageLatencyMs", "getRecentPostIntervalsNs", "outIntervalsNs", "", "getProfileWindowNs", "out", "setBypass", "bypass", "setAntiArtifacts", "enabled", "setVsyncPeriodNs", "periodNs", "setPacingParams", "setShizukuTimingEnabled", "reportShizukuTiming", "frameTimeNs", "pacingJitterNs", "destroyContext"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class NativeBridge {
    public static final NativeBridge INSTANCE = new NativeBridge();

    public final native void destroyContext();

    public final native int extractShaders(String dllPath, String dllSha256, String cacheDir);

    public final native double getAverageLatencyMs();

    public final native double getAverageQueueMs();

    public final native long getGeneratedFrameCount();

    public final native String getNpuSummary();

    public final native long getPostedFrameCount();

    public final native int getProfileWindowNs(long[] out);

    public final native int getRecentPostIntervalsNs(long[] outIntervalsNs);

    public final native long getUniqueCaptureCount();

    public final native int initContext(String cacheDir, int width, int height, int multiplier, float flowScale, boolean performance, boolean hdr, boolean antiArtifacts, boolean framegenFp16, boolean npuPostProcessing, int npuPreset, int npuUpscaleFactor, float npuAmount, float npuRadius, float npuThreshold, boolean npuFp16, boolean cpuPostProcessing, int cpuPreset, float cpuStrength, float cpuSaturation, float cpuVibrance, float cpuVignette, boolean gpuPostProcessing, int gpuStage, int gpuMethod, float gpuUpscaleFactor, float gpuSharpness, float gpuStrength, int targetFpsCap, float emaAlpha, float outlierRatio, float vsyncSlackMs, int queueDepth);

    public final native void initCrashReporter(String crashPath, String logPath);

    public final native boolean isFramegenFp16Supported(String cacheDir);

    public final native boolean isNpuAvailable();

    public final native String nativeVersion();

    public final native int probeShaders(String cacheDir);

    public final native void pushFrame(HardwareBuffer hardwareBuffer, long timestampNs);

    public final native void reportShizukuTiming(long timestampNs, long frameTimeNs, long pacingJitterNs);

    public final native void setAntiArtifacts(boolean enabled);

    public final native void setBypass(boolean bypass);

    public final native void setOutputSurface(Surface surface, int w, int h);

    public final native void setPacingParams(int targetFpsCap, float emaAlpha, float outlierRatio, float vsyncSlackMs, int queueDepth);

    public final native void setShizukuTimingEnabled(boolean enabled);

    public final native void setVsyncPeriodNs(long periodNs);

    private NativeBridge() {
    }

    static {
        System.loadLibrary("lsfg-android");
    }
}
