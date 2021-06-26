package com.hexuebin.frameanimation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * 资源帧信息
 * <p>
 * 这种实现会从 res 中加载 bitmap
 *
 * @author HeXuebin on 2021/6/26.
 */
class ResFrameInfo(
    /**
     * 资源 id (要显示空图传 0)
     */
    private val res: Int
) : AbsFrameInfo() {
    private val TAG: String = "ResFrameInfo"

    private var frameName: String = res.toString()

    override fun decodeBitmap(
        options: BitmapFactory.Options,
        fileCache: ImageFileCache,
        context: Context
    ): Bitmap? {
        if (res == 0) {
            return null
        }
        var bytes: ByteArray? = fileCache.get(res)
        if (bytes == null) {
            val fileBytes: ByteArray? = FileUtils.readResToMem(res, context)
            fileCache.put(res, fileBytes)
            bytes = fileBytes
        }
        return if (bytes == null) {
            null
        } else BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    override fun getFrameName(): String {
        return frameName
    }
}