package com.syed.lsfgmali.data

import android.content.Context
import android.os.Build
import java.io.File

data class DeviceProfile(
    val manufacturer: String,
    val model: String,
    val soc: String,
    val socManufacturer: String,
    val board: String,
    val hardware: String,
    val gpuName: String,
    val eglDriver: String,
    val androidRelease: String,
    val sdkInt: Int,
    val kernel: String,
    val abi: String,
    val lowRam: Boolean,
    val isMali: Boolean,
    val isBifrost: Boolean,
    val isHelioG85: Boolean,
) {
    val gpuShort: String
        get() = when {
            gpuName.isNotBlank() -> gpuName
            isHelioG85 -> "Mali-G52 MC2 (Bifrost)"
            isBifrost -> "Mali Bifrost"
            isMali -> "Mali"
            else -> "Unknown GPU"
        }
}

object DeviceInfo {
    fun detect(context: Context): DeviceProfile {
        val soc = runCatching {
            if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else ""
        }.getOrDefault("")
        val socMan = runCatching {
            if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else ""
        }.getOrDefault("")
        val board = Build.BOARD.orEmpty()
        val hardware = Build.HARDWARE.orEmpty()
        val platform = prop("ro.board.platform") + " " + prop("ro.mediatek.platform")
        val egl = prop("ro.hardware.egl")
        val gpuInfo = readFirst(
            "/sys/class/misc/mali0/device/gpuinfo",
            "/proc/gpuinfo",
            "/sys/kernel/gpu/gpu_model",
        )
        val gpuName = extractGpuName(gpuInfo)
        val isMali = (egl + " " + gpuInfo + " " + gpuName).contains("mali", ignoreCase = true) ||
            (egl.contains("mali", ignoreCase = true))
        val isBifrost = (gpuInfo + " " + gpuName + " " + soc + " " + platform).let {
            it.contains("bifrost", true) || it.contains("g52", true) ||
                it.contains("g31", true) || it.contains("g51", true) || it.contains("g76", true)
        }
        val isG85 = (soc + " " + platform + " " + board).let {
            it.contains("mt6769", true) || it.contains("6769", true) || it.contains("g85", true)
        }
        val lowRam = runCatching {
            (context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager).isLowRamDevice
        }.getOrDefault(false)
        return DeviceProfile(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            soc = soc.ifBlank { platform.trim() },
            socManufacturer = socMan,
            board = board,
            hardware = hardware,
            gpuName = gpuName,
            eglDriver = egl,
            androidRelease = Build.VERSION.RELEASE.orEmpty(),
            sdkInt = Build.VERSION.SDK_INT,
            kernel = System.getProperty("os.version").orEmpty(),
            abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty(),
            lowRam = lowRam,
            isMali = isMali,
            isBifrost = isBifrost,
            isHelioG85 = isG85,
        )
    }

    private fun extractGpuName(raw: String): String {
        if (raw.isBlank()) return ""
        val m = Regex("Mali-G\\d+(?:[ -]?MC\\d+)?").find(raw)
        return m?.value ?: raw.lineSequence().firstOrNull().orEmpty().take(48)
    }

    private fun readFirst(vararg paths: String): String {
        for (p in paths) {
            runCatching {
                val f = File(p)
                if (f.canRead()) return f.readText().trim()
            }
        }
        return ""
    }

    private fun prop(key: String): String = runCatching {
        val cls = Class.forName("android.os.SystemProperties")
        val get = cls.getMethod("get", String::class.java)
        (get.invoke(null, key) as? String).orEmpty()
    }.getOrDefault("")
}
