package com.syed.lsfgmali.data

import java.io.File

/**
 * Reads the FileVersion out of a Windows PE (Lossless.dll ships as a Windows
 * image even though only its embedded shader resources are consumed).
 */
object DllVersion {
    private const val FIXED_FILE_INFO = 0xFEEF04BD.toInt()
    private const val STRUC_VERSION = 0x00010000

    fun detect(file: File): String? = runCatching { detectUnsafe(file) }.getOrNull()

    private fun detectUnsafe(file: File): String? {
        val bytes = file.readBytes()
        if (bytes.size < 0x100) return null
        if (bytes[0] != 'M'.code.toByte() || bytes[1] != 'Z'.code.toByte()) return null
        val pe = readInt(bytes, 0x3C)
        if (pe <= 0 || pe + 6 > bytes.size) return null
        if (bytes[pe] != 'P'.code.toByte() || bytes[pe + 1] != 'E'.code.toByte()) return null

        var index = indexOfFixedInfo(bytes, 0)
        while (index >= 0) {
            if (index + 20 <= bytes.size && readInt(bytes, index + 4) == STRUC_VERSION) {
                val ms = readInt(bytes, index + 8)
                val ls = readInt(bytes, index + 12)
                if (ms > 0) {
                    return "%d.%d.%d.%d".format(
                        ms ushr 16,
                        ms and 0xFFFF,
                        ls ushr 16,
                        ls and 0xFFFF,
                    )
                }
            }
            index = indexOfFixedInfo(bytes, index + 1)
        }
        return null
    }

    private fun readInt(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)

    private fun indexOfFixedInfo(bytes: ByteArray, from: Int): Int {
        var i = from
        while (i + 4 <= bytes.size) {
            if (readInt(bytes, i) == FIXED_FILE_INFO) return i
            i++
        }
        return -1
    }
}
