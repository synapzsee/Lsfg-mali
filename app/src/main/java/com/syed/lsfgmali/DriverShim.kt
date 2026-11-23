package com.syed.lsfgmali

/**
 * Tiny JNI shim (liblsfgmali_shim.so) that sets the Vulkan loader environment
 * before the loader is initialised. Must stay in this package because the
 * prebuilt library exports Java_com_syed_lsfgmali_DriverShim_*.
 */
object DriverShim {
    init {
        System.loadLibrary("lsfgmali_shim")
    }

    external fun setPanVkEnv(icdJsonPath: String): Int
    external fun clearDriverEnv(): Int
}
