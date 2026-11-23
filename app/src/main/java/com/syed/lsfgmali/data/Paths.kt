package com.syed.lsfgmali.data

import android.content.Context
import java.io.File

object Paths {
    /**
     * The native extractor writes `<cache>/<id>.spv`, `<cache>/fp16/<id>.spv` and
     * `<cache>/fp32/<id>.spv`; it does not create the top-level directory, so it
     * must exist before extractShaders() is called.
     */
    fun shaderCacheDir(ctx: Context): File =
        File(ctx.filesDir, "shader_cache").apply { mkdirs() }

    fun shaderCache(ctx: Context): String = shaderCacheDir(ctx).absolutePath

    fun dllCopy(ctx: Context): File =
        File(ctx.filesDir, "Lossless.dll")

    fun panVkDir(ctx: Context): File =
        File(ctx.filesDir, "panvk").apply { mkdirs() }

    fun panVkIcdJson(ctx: Context): File =
        File(panVkDir(ctx), "panfrost_icd.json")

    fun logsDir(ctx: Context): File =
        File(ctx.filesDir, "logs").apply { mkdirs() }

    fun crashFile(ctx: Context): String =
        File(ctx.filesDir, "crash.txt").absolutePath

    fun nativeLog(ctx: Context): String =
        File(ctx.filesDir, "native.log").absolutePath
}
