package com.syed.lsfgmali.data

import android.content.Context
import java.io.File

object Paths {
    fun shaderCache(ctx: Context): String =
        File(ctx.filesDir, "shader_cache").absolutePath

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
