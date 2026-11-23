package com.syed.lsfgmali.data

import android.content.Context
import com.syed.lsfgmali.DriverShim

/**
 * Writes the PanVK ICD manifest into app-private storage and selects it through
 * the tiny JNI setenv shim before the Vulkan loader is initialised.
 *
 * The ICD .so itself lives in the APK's native library directory.
 */
object DriverSetup {
    fun writeIcdJson(ctx: Context): String {
        val lib = "${ctx.applicationInfo.nativeLibraryDir}/libvulkan_panfrost.so"
        val json = """
            {
                "ICD": {
                    "api_version": "1.4.363",
                    "library_arch": "64",
                    "library_path": "$lib"
                },
                "file_format_version": "1.0.1"
            }
        """.trimIndent()
        val file = Paths.panVkIcdJson(ctx)
        file.writeText(json)
        return file.absolutePath
    }

    /** Must run before the first Vulkan instance is created. */
    fun apply(ctx: Context, driver: DriverMode): Int = runCatching {
        if (driver == DriverMode.PANVK) {
            val json = writeIcdJson(ctx)
            val rc = DriverShim.setPanVkEnv(json)
            LogBus.log("PanVK ICD selected via VK_DRIVER_FILES (shim rc=$rc): $json")
            rc
        } else {
            val rc = DriverShim.clearDriverEnv()
            LogBus.log("Stock Mali driver selected (driver env cleared, rc=$rc)")
            rc
        }
    }.getOrElse {
        LogBus.log("driver env setup failed: ${it.message}")
        -1
    }
}
