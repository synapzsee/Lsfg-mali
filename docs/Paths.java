package com.syed.lsfgmali;

import android.content.Context;
import java.io.File;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Shell.kt */
@Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\n\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u000b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007"}, d2 = {"Lcom/syed/lsfgmali/Paths;", "", "<init>", "()V", "shaderCache", "", "ctx", "Landroid/content/Context;", "dllCopy", "Ljava/io/File;", "panVkDir", "panVkIcdJson"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class Paths {
    public static final Paths INSTANCE = new Paths();

    private Paths() {
    }

    public final String shaderCache(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        String absolutePath = new File(ctx.getFilesDir(), "shader_cache").getAbsolutePath();
        Intrinsics.checkNotNullExpressionValue(absolutePath, "getAbsolutePath(...)");
        return absolutePath;
    }

    public final File dllCopy(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        return new File(ctx.getFilesDir(), "Lossless.dll");
    }

    public final File panVkDir(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        File $this$panVkDir_u24lambda_u240 = new File(ctx.getFilesDir(), "panvk");
        $this$panVkDir_u24lambda_u240.mkdirs();
        return $this$panVkDir_u24lambda_u240;
    }

    public final File panVkIcdJson(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        return new File(panVkDir(ctx), "panfrost_icd.json");
    }
}
