package com.hexuebin.frameanimation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build

/**
 * bitmap 工具类
 * <p>
 * copy (https://developer.android.com/topic/performance/graphics/manage-memory)
 *
 * @author HeXuebin on 2021/6/26.
 */
object BitmapUtils {
    fun canUseForInBitmap(candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            val width = targetOptions.outWidth / targetOptions.inSampleSize
            val height = targetOptions.outHeight / targetOptions.inSampleSize
            val byteCount = width * height * getBytesPerPixel(candidate.config)
            return byteCount <= candidate.allocationByteCount
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.width == targetOptions.outWidth && candidate.height == targetOptions.outHeight && targetOptions.inSampleSize == 1
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    fun getBytesPerPixel(config: Bitmap.Config): Int {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4
        } else if (config == Bitmap.Config.RGB_565) {
            return 2
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1
        }
        return 1
    }
}