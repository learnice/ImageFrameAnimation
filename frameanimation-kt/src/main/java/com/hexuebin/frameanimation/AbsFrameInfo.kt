package com.hexuebin.frameanimation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * 抽象帧信息
 * <p>
 * 子类需要实现 {@link AbsFrameInfo#decodeBitmap} 方法，根据情况是从资源中加载还是从内部存储中加载
 *
 * @author HeXuebin on 2021/6/26.
 */
abstract class AbsFrameInfo {
    private val TAG: String = "AbsFrameInfo"

    /**
     * 加载完成的当前帧的 bitmap
     */
    private var frameBitmap: Bitmap? = null

    private val options = BitmapFactory.Options()

    /**
     * 是否获取过宽高了
     */
    private var isDecodeBounds = false
    private var width = 0
    private var height = 0

    /**
     * 获取要加载图片的大小
     *
     * @param fileCache
     */
    fun decodeBitmapSize(fileCache: ImageFileCache, context: Context) {
        if (isDecodeBounds) {
            return
        }
        isDecodeBounds = true
        // 仅仅获取图片宽高
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        decodeBitmap(options, fileCache, context)
        width = options.outWidth
        height = options.outHeight
    }

    fun loadBitmap(inBitmap: Bitmap, fileCache: ImageFileCache, context: Context): Bitmap? {
        if (width == 0 || height == 0) {
            return null
        }
        // bitmap 复用
        options.inMutable = true
        options.inBitmap = inBitmap
        // 加载图片
        options.inJustDecodeBounds = false
        return decodeBitmap(options, fileCache, context)
    }

    protected abstract fun decodeBitmap(
        options: BitmapFactory.Options,
        fileCache: ImageFileCache,
        context: Context
    ): Bitmap?

    /**
     * 处理图片
     * 动画器中加载获取到加载完的图片后会调用这个方法，子类可以自行实现对加载后的图片进行处理，这里默认实现直接返回
     *
     * @param bitmap 加载完成的图片
     * @return 处理完的 bitmap
     */
    fun processBitmap(bitmap: Bitmap?): Bitmap? {
        return bitmap
    }

    open fun getFrameName(): String {
        return ""
    }

    fun getFrameBitmap(): Bitmap? {
        return frameBitmap
    }

    fun setFrameBitmap(frameBitmap: Bitmap?) {
        this.frameBitmap = frameBitmap
    }

    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }

    fun getOptions(): BitmapFactory.Options {
        return options
    }
}