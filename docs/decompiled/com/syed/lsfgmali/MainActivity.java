package com.syed.lsfgmali;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.lsfg.android.session.NativeBridge;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.io.ByteStreamsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;

/* compiled from: MainActivity.kt */
@Metadata(d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 22\u00020\u0001:\u00012B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0015\u001a\u00020\r2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0014J\b\u0010\u0018\u001a\u00020\rH\u0014J\b\u0010\u0019\u001a\u00020\rH\u0014J\b\u0010\u001a\u001a\u00020\rH\u0014J\b\u0010\u001b\u001a\u00020\fH\u0002J\u0010\u0010\u001c\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\fH\u0002J\b\u0010\u001e\u001a\u00020\rH\u0002J\b\u0010\u001f\u001a\u00020\rH\u0002J\b\u0010 \u001a\u00020!H\u0002J\b\u0010\"\u001a\u00020\rH\u0002J\b\u0010#\u001a\u00020\rH\u0002J\"\u0010$\u001a\u00020\r2\u0006\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020&2\b\u0010(\u001a\u0004\u0018\u00010)H\u0014J\u0010\u0010*\u001a\u00020\r2\u0006\u0010+\u001a\u00020,H\u0002J\u0010\u0010-\u001a\u00020\f2\u0006\u0010.\u001a\u00020/H\u0002J\b\u00100\u001a\u00020\rH\u0002J\b\u00101\u001a\u00020\rH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.¢\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R#\u0010\u000e\u001a\n \u0010*\u0004\u0018\u00010\u000f0\u000f8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012"}, d2 = {"Lcom/syed/lsfgmali/MainActivity;", "Landroid/app/Activity;", "<init>", "()V", "txtDllStatus", "Landroid/widget/TextView;", "txtFlowLabel", "txtLog", "btnOverlayPerm", "Landroid/widget/Button;", "logListener", "Lkotlin/Function1;", "", "", "prefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "getPrefs", "()Landroid/content/SharedPreferences;", "prefs$delegate", "Lkotlin/Lazy;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onResume", "onPause", "driver", "setDriver", "d", "preparePanVkJson", "applyDriverEnv", "flowScale", "", "updateFlowLabel", "refreshDllStatus", "onActivityResult", "requestCode", "", "resultCode", "data", "Landroid/content/Intent;", "importDll", "uri", "Landroid/net/Uri;", "sha256", "f", "Ljava/io/File;", "refreshOverlayButton", "startCapture", "Companion"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MainActivity extends Activity {
    public static final int REQ_CAPTURE = 2;
    public static final int REQ_DLL = 1;
    public static final int REQ_NOTIF = 4;
    public static final int REQ_OVERLAY = 3;
    private Button btnOverlayPerm;
    private final Function1<String, Unit> logListener = new Function1() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda3
        @Override // kotlin.jvm.functions.Function1
        public final Object invoke(Object obj) {
            Unit logListener$lambda$1;
            logListener$lambda$1 = MainActivity.logListener$lambda$1(MainActivity.this, (String) obj);
            return logListener$lambda$1;
        }
    };

    /* renamed from: prefs$delegate, reason: from kotlin metadata */
    private final Lazy prefs = LazyKt.lazy(new Function0() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda4
        @Override // kotlin.jvm.functions.Function0
        public final Object invoke() {
            SharedPreferences prefs_delegate$lambda$2;
            prefs_delegate$lambda$2 = MainActivity.prefs_delegate$lambda$2(MainActivity.this);
            return prefs_delegate$lambda$2;
        }
    });
    private TextView txtDllStatus;
    private TextView txtFlowLabel;
    private TextView txtLog;

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit logListener$lambda$1(final MainActivity this$0, final String line) {
        Intrinsics.checkNotNullParameter(line, "line");
        this$0.runOnUiThread(new Runnable() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.logListener$lambda$1$lambda$0(MainActivity.this, line);
            }
        });
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void logListener$lambda$1$lambda$0(MainActivity this$0, String $line) {
        TextView textView = this$0.txtLog;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("txtLog");
            textView = null;
        }
        textView.append($line + "\n");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final SharedPreferences getPrefs() {
        return (SharedPreferences) this.prefs.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final SharedPreferences prefs_delegate$lambda$2(MainActivity this$0) {
        return this$0.getSharedPreferences("shell", 0);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        Object m11constructorimpl;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.txtDllStatus = (TextView) findViewById(R.id.txtDllStatus);
        this.txtFlowLabel = (TextView) findViewById(R.id.txtFlowLabel);
        this.txtLog = (TextView) findViewById(R.id.txtLog);
        this.btnOverlayPerm = (Button) findViewById(R.id.btnOverlayPerm);
        preparePanVkJson();
        applyDriverEnv();
        try {
            Result.Companion companion = Result.INSTANCE;
            MainActivity $this$onCreate_u24lambda_u243 = this;
            NativeBridge nativeBridge = NativeBridge.INSTANCE;
            String absolutePath = new File($this$onCreate_u24lambda_u243.getFilesDir(), "crash.txt").getAbsolutePath();
            Intrinsics.checkNotNullExpressionValue(absolutePath, "getAbsolutePath(...)");
            String absolutePath2 = new File($this$onCreate_u24lambda_u243.getFilesDir(), "native.log").getAbsolutePath();
            Intrinsics.checkNotNullExpressionValue(absolutePath2, "getAbsolutePath(...)");
            nativeBridge.initCrashReporter(absolutePath, absolutePath2);
            LogBus.INSTANCE.log("engine version: " + NativeBridge.INSTANCE.nativeVersion());
            m11constructorimpl = Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
        }
        Throwable it = Result.m14exceptionOrNullimpl(m11constructorimpl);
        if (it != null) {
            LogBus.INSTANCE.log("native init failed: " + it.getMessage());
        }
        LogBus.INSTANCE.addListener(this.logListener);
        LogBus.INSTANCE.log("LSFG-for-Mali shell ready. Driver: " + driver() + " (applies at process start; changing it takes full effect after an app restart).");
        RadioButton rbPanvk = (RadioButton) findViewById(R.id.rbPanvk);
        RadioButton rbStock = (RadioButton) findViewById(R.id.rbStock);
        if (Intrinsics.areEqual(driver(), "panvk")) {
            rbPanvk.setChecked(true);
        } else {
            rbStock.setChecked(true);
        }
        rbPanvk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda6
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.onCreate$lambda$5(MainActivity.this, compoundButton, z);
            }
        });
        rbStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda7
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.onCreate$lambda$6(MainActivity.this, compoundButton, z);
            }
        });
        SeekBar seek = (SeekBar) findViewById(R.id.seekFlow);
        seek.setProgress(RangesKt.coerceIn((int) ((flowScale() - 0.5f) * 100), 0, 25));
        updateFlowLabel();
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.syed.lsfgmali.MainActivity$onCreate$5
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                SharedPreferences prefs;
                prefs = MainActivity.this.getPrefs();
                prefs.edit().putFloat("flow", (p / 100.0f) + 0.5f).apply();
                MainActivity.this.updateFlowLabel();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar sb) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar sb) {
            }
        });
        CheckBox $this$onCreate_u24lambda_u248 = (CheckBox) findViewById(R.id.chkReduce);
        $this$onCreate_u24lambda_u248.setChecked(getPrefs().getBoolean("reduce", false));
        $this$onCreate_u24lambda_u248.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda8
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.onCreate$lambda$8$lambda$7(MainActivity.this, compoundButton, z);
            }
        });
        LogSaver.INSTANCE.start(this);
        ((Button) findViewById(R.id.btnShareLog)).setOnClickListener(new View.OnClickListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$9(MainActivity.this, view);
            }
        });
        ((Button) findViewById(R.id.btnPickDll)).setOnClickListener(new View.OnClickListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$11(MainActivity.this, view);
            }
        });
        Button button = this.btnOverlayPerm;
        if (button == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnOverlayPerm");
            button = null;
        }
        button.setOnClickListener(new View.OnClickListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$12(MainActivity.this, view);
            }
        });
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(new View.OnClickListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.startCapture();
            }
        });
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(new View.OnClickListener() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$14(MainActivity.this, view);
            }
        });
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 4);
        }
        refreshDllStatus();
        refreshOverlayButton();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$5(MainActivity this$0, CompoundButton compoundButton, boolean checked) {
        if (checked) {
            this$0.setDriver("panvk");
            this$0.applyDriverEnv();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$6(MainActivity this$0, CompoundButton compoundButton, boolean checked) {
        if (checked) {
            this$0.setDriver("stock");
            this$0.applyDriverEnv();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$8$lambda$7(MainActivity this$0, CompoundButton compoundButton, boolean c) {
        this$0.getPrefs().edit().putBoolean("reduce", c).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$9(MainActivity this$0, View it) {
        LogSaver.INSTANCE.shareLatest(this$0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$11(MainActivity this$0, View it) {
        Intent i = new Intent("android.intent.action.OPEN_DOCUMENT");
        i.setType("*/*");
        i.addCategory("android.intent.category.OPENABLE");
        this$0.startActivityForResult(i, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$12(MainActivity this$0, View it) {
        Intent i = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + this$0.getPackageName()));
        this$0.startActivityForResult(i, 3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$14(MainActivity this$0, View it) {
        CaptureService.INSTANCE.stop(this$0);
        LogBus.INSTANCE.log("stop requested");
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        LogBus.INSTANCE.removeListener(this.logListener);
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        ForegroundState.INSTANCE.setMainActivityForeground(true);
        refreshOverlayButton();
    }

    @Override // android.app.Activity
    protected void onPause() {
        ForegroundState.INSTANCE.setMainActivityForeground(false);
        super.onPause();
    }

    private final String driver() {
        String string = getPrefs().getString("driver", "panvk");
        return string == null ? "panvk" : string;
    }

    private final void setDriver(String d) {
        getPrefs().edit().putString("driver", d).apply();
        LogBus.INSTANCE.log("driver preference = " + d + " (full effect on next app start)");
    }

    private final void preparePanVkJson() {
        String json = StringsKt.trimIndent("\n            {\n                \"ICD\": {\n                    \"api_version\": \"1.4.363\",\n                    \"library_arch\": \"64\",\n                    \"library_path\": \"" + getApplicationInfo().nativeLibraryDir + "/libvulkan_panfrost.so\"\n                },\n                \"file_format_version\": \"1.0.1\"\n            }\n        ");
        try {
            Result.Companion companion = Result.INSTANCE;
            MainActivity $this$preparePanVkJson_u24lambda_u2415 = this;
            FilesKt.writeText$default(Paths.INSTANCE.panVkIcdJson($this$preparePanVkJson_u24lambda_u2415), json, null, 2, null);
            Result.m11constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            Result.m11constructorimpl(ResultKt.createFailure(th));
        }
    }

    private final void applyDriverEnv() {
        if (Intrinsics.areEqual(driver(), "panvk")) {
            DriverShim driverShim = DriverShim.INSTANCE;
            String absolutePath = Paths.INSTANCE.panVkIcdJson(this).getAbsolutePath();
            Intrinsics.checkNotNullExpressionValue(absolutePath, "getAbsolutePath(...)");
            int rc = driverShim.setPanVkEnv(absolutePath);
            LogBus.INSTANCE.log("PanVK ICD selected via VK_DRIVER_FILES (shim rc=" + rc + "): " + Paths.INSTANCE.panVkIcdJson(this).getAbsolutePath());
            return;
        }
        DriverShim.INSTANCE.clearDriverEnv();
        LogBus.INSTANCE.log("stock Mali driver selected (driver env cleared)");
    }

    private final float flowScale() {
        return getPrefs().getFloat("flow", 0.7f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateFlowLabel() {
        TextView textView = this.txtFlowLabel;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("txtFlowLabel");
            textView = null;
        }
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String format = String.format("%.2f", Arrays.copyOf(new Object[]{Float.valueOf(flowScale())}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        textView.setText("Flow scale: " + format + "  (multiplier fixed at 2x, 3.1P performance variant)");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void refreshDllStatus() {
        String str;
        TextView textView = null;
        String name = getPrefs().getString("dll_name", null);
        boolean ready = getPrefs().getBoolean("dll_ready", false);
        TextView textView2 = this.txtDllStatus;
        if (textView2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("txtDllStatus");
        } else {
            textView = textView2;
        }
        if (name != null) {
            str = ready ? "Lossless.dll: " + name + " — shaders extracted ✓" : "Lossless.dll: " + name + " — extraction not completed (see log)";
        }
        textView.setText(str);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Uri uri;
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == -1 && data != null && (uri = data.getData()) != null) {
                    try {
                        Result.Companion companion = Result.INSTANCE;
                        MainActivity $this$onActivityResult_u24lambda_u2416 = this;
                        $this$onActivityResult_u24lambda_u2416.getContentResolver().takePersistableUriPermission(uri, 1);
                        Result.m11constructorimpl(Unit.INSTANCE);
                    } catch (Throwable th) {
                        Result.Companion companion2 = Result.INSTANCE;
                        Result.m11constructorimpl(ResultKt.createFailure(th));
                    }
                    String name = uri.getLastPathSegment();
                    if (name == null) {
                        name = "Lossless.dll";
                    }
                    getPrefs().edit().putString("dll_name", name).putBoolean("dll_ready", false).apply();
                    refreshDllStatus();
                    LogBus.INSTANCE.log("copying DLL (" + name + ") to private storage…");
                    new Thread(new Runnable() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            MainActivity.this.importDll(uri);
                        }
                    }).start();
                    break;
                }
                break;
            case 2:
                if (resultCode == -1 && data != null) {
                    CaptureService.INSTANCE.start(this, resultCode, data, flowScale(), getPrefs().getBoolean("reduce", false));
                    break;
                } else {
                    LogBus.INSTANCE.log("screen-capture consent denied — cannot start");
                    break;
                }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void importDll(Uri uri) {
        Object m11constructorimpl;
        try {
            File dest = Paths.INSTANCE.dllCopy(this);
            InputStream openInputStream = getContentResolver().openInputStream(uri);
            if (openInputStream != null) {
                FileOutputStream fileOutputStream = openInputStream;
                try {
                    InputStream input = fileOutputStream;
                    fileOutputStream = new FileOutputStream(dest);
                    try {
                        FileOutputStream it = fileOutputStream;
                        ByteStreamsKt.copyTo$default(input, it, 0, 2, null);
                        CloseableKt.closeFinally(fileOutputStream, null);
                        CloseableKt.closeFinally(fileOutputStream, null);
                        String sha = sha256(dest);
                        getPrefs().edit().putString("dll_sha", sha).apply();
                        LogBus.INSTANCE.log("DLL copied (" + dest.length() + " bytes, sha256 " + StringsKt.take(sha, 12) + "…). Extracting shaders…");
                        String cache = Paths.INSTANCE.shaderCache(this);
                        new File(cache).mkdirs();
                        NativeBridge nativeBridge = NativeBridge.INSTANCE;
                        String absolutePath = dest.getAbsolutePath();
                        Intrinsics.checkNotNullExpressionValue(absolutePath, "getAbsolutePath(...)");
                        int rc = nativeBridge.extractShaders(absolutePath, sha, cache);
                        LogBus.INSTANCE.log("extractShaders rc=" + rc + " " + (rc == 0 ? "(ok)" : "(FAILED — wrong file? must be a genuine Lossless.dll)"));
                        if (rc != 0) {
                            return;
                        }
                        LogBus.INSTANCE.log("probeShaders rc=" + NativeBridge.INSTANCE.probeShaders(cache) + " (0 = this driver accepts every shader; -12 = a shader was rejected)");
                        try {
                            Result.Companion companion = Result.INSTANCE;
                            MainActivity mainActivity = this;
                            m11constructorimpl = Result.m11constructorimpl(Boolean.valueOf(NativeBridge.INSTANCE.isFramegenFp16Supported(cache)));
                        } catch (Throwable th) {
                            Result.Companion companion2 = Result.INSTANCE;
                            m11constructorimpl = Result.m11constructorimpl(ResultKt.createFailure(th));
                        }
                        if (Result.m17isFailureimpl(m11constructorimpl)) {
                            m11constructorimpl = false;
                        }
                        boolean fp16 = ((Boolean) m11constructorimpl).booleanValue();
                        LogBus.INSTANCE.log("FP16 framegen path available: " + fp16 + " (needs the FP16 shader set in the DLL — 3.2.1.0 does not ship it; FP32 path will be used)");
                        getPrefs().edit().putBoolean("dll_ready", true).apply();
                        runOnUiThread(new Runnable() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda1
                            @Override // java.lang.Runnable
                            public final void run() {
                                MainActivity.this.refreshDllStatus();
                            }
                        });
                    } finally {
                    }
                } finally {
                }
            } else {
                throw new IllegalStateException("could not open the picked file");
            }
        } catch (Exception e) {
            LogBus.INSTANCE.log("DLL import failed: " + e.getMessage());
        }
    }

    private final String sha256(File f) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fileInputStream = new FileInputStream(f);
        try {
            FileInputStream input = fileInputStream;
            byte[] buf = new byte[65536];
            while (true) {
                int n = input.read(buf);
                if (n < 0) {
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(fileInputStream, null);
                    byte[] digest = md.digest();
                    Intrinsics.checkNotNullExpressionValue(digest, "digest(...)");
                    return ArraysKt.joinToString$default(digest, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new Function1() { // from class: com.syed.lsfgmali.MainActivity$$ExternalSyntheticLambda0
                        @Override // kotlin.jvm.functions.Function1
                        public final Object invoke(Object obj) {
                            CharSequence sha256$lambda$23;
                            sha256$lambda$23 = MainActivity.sha256$lambda$23(((Byte) obj).byteValue());
                            return sha256$lambda$23;
                        }
                    }, 30, (Object) null);
                }
                md.update(buf, 0, n);
            }
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence sha256$lambda$23(byte it) {
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(it)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    private final void refreshOverlayButton() {
        Button button = this.btnOverlayPerm;
        if (button == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnOverlayPerm");
            button = null;
        }
        button.setVisibility(Settings.canDrawOverlays(this) ? 8 : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startCapture() {
        if (!getPrefs().getBoolean("dll_ready", false)) {
            Toast.makeText(this, "Pick your Lossless.dll first", 0).show();
            LogBus.INSTANCE.log("start blocked: DLL not prepared");
        } else {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Grant 'display over other apps' first", 0).show();
                Button button = this.btnOverlayPerm;
                if (button == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("btnOverlayPerm");
                    button = null;
                }
                button.performClick();
                return;
            }
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MediaProjectionManager.class);
            startActivityForResult(mpm.createScreenCaptureIntent(), 2);
        }
    }
}
