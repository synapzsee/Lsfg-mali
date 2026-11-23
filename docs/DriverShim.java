package com.syed.lsfgmali;

import kotlin.Metadata;

/* compiled from: Shell.kt */
@Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0011\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0086 J\t\u0010\b\u001a\u00020\u0005H\u0086 "}, d2 = {"Lcom/syed/lsfgmali/DriverShim;", "", "<init>", "()V", "setPanVkEnv", "", "icdJsonPath", "", "clearDriverEnv"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class DriverShim {
    public static final DriverShim INSTANCE = new DriverShim();

    public final native int clearDriverEnv();

    public final native int setPanVkEnv(String icdJsonPath);

    private DriverShim() {
    }

    static {
        System.loadLibrary("lsfgmali_shim");
    }
}
