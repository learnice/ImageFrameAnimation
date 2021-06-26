package com.hexuebin.frameanimation

import android.content.Context
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * @author HeXuebin on 2021/6/26.
 */
object FileUtils {
    fun readFileToMem(fileName: String): ByteArray? {
        if (TextUtils.isEmpty(fileName)) {
            return null
        }
        val file = File(fileName)
        if (!file.exists()) {
            return null
        }
        var buffer: ByteArray
        try {
            FileInputStream(file).use { fis ->
                val streamLength = fis.available()
                buffer = ByteArray(streamLength)
                val readLen = fis.read(buffer)
                if (readLen == streamLength) {
                    return buffer
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun readResToMem(resId: Int, context: Context): ByteArray? {
        var buffer: ByteArray
        try {
            context.resources.openRawResource(resId).use { `is` ->
                val streamLength = `is`.available()
                buffer = ByteArray(streamLength)
                val readLen = `is`.read(buffer)
                if (readLen == streamLength) {
                    return buffer
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}