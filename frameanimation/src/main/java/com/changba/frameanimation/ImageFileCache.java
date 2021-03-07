package com.changba.frameanimation;

import android.util.LruCache;

/**
 * @author HeXuebin on 2021/1/25.
 */
public class ImageFileCache {
    private static final String TAG = ImageFileCache.class.getSimpleName();
    /**
     * 兆字节
     */
    private static final float M_BYTE = 1024 * 1024;
    /**
     * 40 MB
     */
    private static final float cacheSize = 40 * M_BYTE;


    private final LruCache<Object, FileByteCache> cache = new LruCache<Object, FileByteCache>((int) cacheSize) {
        @Override
        protected int sizeOf(Object key, FileByteCache value) {
            return value.getBytes().length;
        }
    };


    public void put(Object fileName, byte[] bytes) {
        cache.put(fileName, new FileByteCache(fileName, bytes));
        if (ImageFrameAnimation.DEBUG) {
            //Log.d(TAG, "put: fileName = " + fileName + ", 当前 cache 大小 = " + cache.size() / M_BYTE + "M");
        }
    }

    public byte[] get(Object fileName) {
        FileByteCache byteCache = cache.get(fileName);
        if (byteCache == null) {
            return null;
        }
        return byteCache.getBytes();
    }

    public void clean() {
        cache.trimToSize(-1);
    }

    static class FileByteCache {
        private final Object fileName;
        private final byte[] bytes;

        public FileByteCache(Object fileName, byte[] bytes) {
            this.fileName = fileName;
            this.bytes = bytes;
        }

        public Object getFileName() {
            return fileName;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}
