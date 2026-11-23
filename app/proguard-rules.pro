# JNI entry points are name-matched; never rename or strip them.
-keep class com.lsfg.android.session.NativeBridge { *; }
-keep class com.syed.lsfgmali.native.DriverShim { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# The shim .so exports Java_com_syed_lsfgmali_DriverShim_*.
-keep class com.syed.lsfgmali.DriverShim { *; }

# Reflection: SurfaceControl$Transaction#setSkipScreenshot
-dontwarn android.view.SurfaceControl$Transaction
