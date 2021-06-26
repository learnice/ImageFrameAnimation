package com.hexuebin.frameanimation

import android.util.LruCache

/**
 * @author HeXuebin on 2021/6/26.
 */
class ImageFileCache {
    private val TAG: String = "ImageFileCache"

    /**
     * 兆字节
     */
    private val M_BYTE = (1024 * 1024).toFloat()

    /**
     * 40 MB
     */
    private val cacheSize = 40 * M_BYTE


    private val cache: LruCache<Any, FileByteCache> = object : LruCache<Any, FileByteCache>(
        cacheSize.toInt()
    ) {
        override fun sizeOf(key: Any, value: FileByteCache): Int {
            return value.bytes?.size ?: 0
        }
    }


    fun put(fileName: Any, bytes: ByteArray?) {
        cache.put(fileName, FileByteCache(fileName, bytes))
        if (ImageFrameAnimation.DEBUG) {
            //Log.d(TAG, "put: fileName = " + fileName + ", 当前 cache 大小 = " + cache.size() / M_BYTE + "M");
        }
    }

    fun get(fileName: Any): ByteArray? {
        val byteCache = cache[fileName] ?: return null
        return byteCache.bytes
    }

    fun clean() {
        cache.trimToSize(-1)
    }

    internal class FileByteCache(val fileName: Any, val bytes: ByteArray?)
}