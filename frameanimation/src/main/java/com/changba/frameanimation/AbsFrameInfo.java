package com.changba.frameanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 抽象帧信息
 * <p>
 * 子类需要实现 {@link AbsFrameInfo#decodeBitmap} 方法，根据情况是从资源中加载还是从内部存储中加载
 *
 * @author HeXuebin on 2021/1/16.
 */
public abstract class AbsFrameInfo {
    private static final String TAG = AbsFrameInfo.class.getSimpleName();
    /**
     * 加载完成的当前帧的 bitmap
     */
    private Bitmap frameBitmap;

    private final BitmapFactory.Options options = new BitmapFactory.Options();
    /**
     * 是否获取过宽高了
     */
    private boolean isDecodeBounds = false;
    private int width;
    private int height;

    /**
     * 获取要加载图片的大小
     *
     * @param fileCache
     */
    void decodeBitmapSize(ImageFileCache fileCache, Context context) {
        if (isDecodeBounds) {
            return;
        }
        isDecodeBounds = true;
        // 仅仅获取图片宽高
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        decodeBitmap(options, fileCache, context);
        width = options.outWidth;
        height = options.outHeight;
    }

    Bitmap loadBitmap(Bitmap inBitmap, ImageFileCache fileCache, Context context) {
        if (width == 0 || height == 0) {
            return null;
        }
        // bitmap 复用
        options.inMutable = true;
        options.inBitmap = inBitmap;
        // 加载图片
        options.inJustDecodeBounds = false;
        return decodeBitmap(options, fileCache, context);
    }

    protected abstract Bitmap decodeBitmap(BitmapFactory.Options options, ImageFileCache fileCache, Context context);

    /**
     * 处理图片
     * 动画器中加载获取到加载完的图片后会调用这个方法，子类可以自行实现对加载后的图片进行处理，这里默认实现直接返回
     *
     * @param bitmap 加载完成的图片
     * @return 处理完的 bitmap
     */
    protected Bitmap processBitmap(Bitmap bitmap) {
        return bitmap;
    }

    public Bitmap getFrameBitmap() {
        return frameBitmap;
    }

    public void setFrameBitmap(Bitmap frameBitmap) {
        this.frameBitmap = frameBitmap;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BitmapFactory.Options getOptions() {
        return options;
    }
}
