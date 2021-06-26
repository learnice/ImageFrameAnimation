package com.hexuebin.frameanimation

import android.graphics.Bitmap

/**
 * bitmap 内存复用(用两张防止干扰)
 *
 * @author HeXuebin on 2021/6/26.
 */
class BitmapDoubleBuffer {
    private val TAG: String = "BitmapDoubleBuffer"
    private val doubleBuffer = arrayOfNulls<Bitmap>(2)
    private var useBufferIndex = 1

    /**
     * 获取下一张可用 buffer
     *
     * @param frameInfo 帧信息
     * @return
     */
    fun getNextBuffer(frameInfo: AbsFrameInfo): Bitmap {
        useBufferIndex = if (useBufferIndex == 0) 1 else 0
        val inBitmap = doubleBuffer[useBufferIndex]
        if (!isEnough(inBitmap, frameInfo)) {
            createBuffer(frameInfo)
        }
        return doubleBuffer[useBufferIndex]!!
    }

    private fun isEnough(
        inBitmap: Bitmap?,
        frameInfo: AbsFrameInfo
    ): Boolean {
        return inBitmap != null && BitmapUtils.canUseForInBitmap(inBitmap, frameInfo.getOptions())
    }

    /**
     * 重建 buffer
     *
     * @param frameInfo
     */
    private fun createBuffer(frameInfo: AbsFrameInfo) {
        doubleBuffer[0] = Bitmap.createBitmap(
            frameInfo.getWidth(),
            frameInfo.getHeight(),
            Bitmap.Config.ARGB_8888
        )
        doubleBuffer[1] = Bitmap.createBitmap(
            frameInfo.getWidth(),
            frameInfo.getHeight(),
            Bitmap.Config.ARGB_8888
        )
    }
}