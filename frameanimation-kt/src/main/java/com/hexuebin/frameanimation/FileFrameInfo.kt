package com.hexuebin.frameanimation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils

/**
 * 文件帧信息
 * <p>
 * 这种实现会从内部存储中加载 bitmap
 *
 * @author HeXuebin on 2021/6/26.
 */
class FileFrameInfo(
    /**
     * 图片地址 (要显示空图传 null)
     */
    private val path: String
) : AbsFrameInfo() {
    private val TAG: String = "FileFrameInfo"

    override fun decodeBitmap(
        options: BitmapFactory.Options,
        fileCache: ImageFileCache,
        context: Context
    ): Bitmap? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        var bytes: ByteArray? = fileCache.get(path)
        if (bytes == null) {
            val fileBytes: ByteArray? = FileUtils.readFileToMem(path)
            fileCache.put(path, fileBytes)
            bytes = fileBytes
        }
        return if (bytes == null) {
            null
        } else BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    override fun getFrameName(): String {
        return path
    }
}