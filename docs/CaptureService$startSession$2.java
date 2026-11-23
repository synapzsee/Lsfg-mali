package com.syed.lsfgmali;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.lsfg.android.session.NativeBridge;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: CaptureService.kt */
@Metadata(d1 = {"\u0000!\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0016J(\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\bH\u0016J\u0010\u0010\u000b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0016"}, d2 = {"com/syed/lsfgmali/CaptureService$startSession$2", "Landroid/view/SurfaceHolder$Callback;", "surfaceCreated", "", "holder", "Landroid/view/SurfaceHolder;", "surfaceChanged", "f", "", "w", "h", "surfaceDestroyed"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class CaptureService$startSession$2 implements SurfaceHolder.Callback {
    final /* synthetic */ SurfaceView $sv;
    final /* synthetic */ CaptureService this$0;

    CaptureService$startSession$2(CaptureService $receiver, SurfaceView $sv) {
        this.this$0 = $receiver;
        this.$sv = $sv;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        Object m11constructorimpl;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        String rotationName;
        Intrinsics.checkNotNullParameter(holder, "holder");
        CaptureService captureService = this.this$0;
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService$startSession$2 captureService$startSession$2 = this;
            NativeBridge nativeBridge = NativeBridge.INSTANCE;
            Surface surface = holder.getSurface();
            i = captureService.captureW;
            i2 = captureService.captureH;
            nativeBridge.setOutputSurface(surface, i, i2);
            LogBus logBus = LogBus.INSTANCE;
            i3 = captureService.captureW;
            i4 = captureService.captureH;
            i5 = captureService.currentRotation;
            rotationName = captureService.rotationName(i5);
            logBus.log("output surface attached at " + i3 + "x" + i4 + " (rotation=" + rotationName + ")");
            m11constructorimpl = Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        Throwable it = Result.m14exceptionOrNullimpl(m11constructorimpl);
        if (it != null) {
            LogBus.INSTANCE.log("setOutputSurface failed: " + it.getMessage());
        }
        this.this$0.attemptSkipScreenshot(this.$sv);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        int i;
        int i2;
        int i3;
        String rotationName;
        boolean z;
        int i4;
        int i5;
        Intrinsics.checkNotNullParameter(holder, "holder");
        LogBus logBus = LogBus.INSTANCE;
        i = this.this$0.captureW;
        i2 = this.this$0.captureH;
        CaptureService captureService = this.this$0;
        i3 = this.this$0.currentRotation;
        rotationName = captureService.rotationName(i3);
        logBus.log("overlay surface changed: " + w + "x" + h + " (capture " + i + "x" + i2 + ", rotation=" + rotationName + ")");
        z = this.this$0.contextReady;
        if (z) {
            boolean z2 = w > h;
            i4 = this.this$0.captureW;
            i5 = this.this$0.captureH;
            if (z2 != (i4 > i5)) {
                this.this$0.onRotationPossiblyChanged("surfaceChanged");
            }
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
        Intrinsics.checkNotNullParameter(holder, "holder");
        try {
            Result.Companion companion = Result.INSTANCE;
            CaptureService$startSession$2 captureService$startSession$2 = this;
            NativeBridge.INSTANCE.setOutputSurface(null, 0, 0);
            Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th));
        }
    }
}
