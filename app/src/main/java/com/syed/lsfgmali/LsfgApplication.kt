package com.syed.lsfgmali

import android.app.Application
import com.lsfg.android.session.NativeBridge
import com.syed.lsfgmali.data.DeviceInfo
import com.syed.lsfgmali.data.DriverSetup
import com.syed.lsfgmali.data.EngineState
import com.syed.lsfgmali.data.LogBus
import com.syed.lsfgmali.data.LogSaver
import com.syed.lsfgmali.data.Paths
import com.syed.lsfgmali.data.SettingsStore

class LsfgApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val store = SettingsStore.get(this)
        val device = DeviceInfo.detect(this)

        if (store.settings.value.autoDeviceProfile &&
            !store.settings.value.deviceProfileApplied &&
            device.isHelioG85
        ) {
            store.applyG85Profile()
            store.update { it.copy(deviceProfileApplied = true) }
            LogBus.log("Helio G85 / Bifrost detected — applied performance profile (50% capture, FP16 auto, target 60)")
        }

        // Environment must be set before the Vulkan loader is initialised.
        runCatching { DriverSetup.apply(this, store.settings.value.driver) }
            .onFailure { LogBus.log("driver setup failed: ${it.message}") }

        LogSaver.start(this)

        runCatching {
            NativeBridge.initCrashReporter(Paths.crashFile(this), Paths.nativeLog(this))
            LogBus.log("engine version: ${NativeBridge.nativeVersion()}")
        }.onFailure { LogBus.log("native init failed: ${it.message}") }

        LogBus.log(
            "device: ${device.manufacturer} ${device.model} · ${device.soc.ifBlank { "SoC?" }} · " +
                "GPU ${device.gpuShort} · Android ${device.androidRelease} (SDK ${device.sdkInt}) · " +
                "kernel ${device.kernel} · ${device.abi}",
        )
        LogBus.log("LSFG-for-Mali 2.0 shell ready · driver ${store.settings.value.driver}")

        EngineState.update { it.copy(driver = if (store.settings.value.driver == com.syed.lsfgmali.data.DriverMode.PANVK) "PanVK" else "Stock Mali") }
    }
}
