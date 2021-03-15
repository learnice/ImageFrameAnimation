package com.changba.frameanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 资源帧信息
 * <p>
 * 这种实现会从 res 中加载 bitmap
 *
 * @author HeXuebin on 2021/1/16.
 */
public class ResFrameInfo extends AbsFrameInfo {
    private static final String TAG = ResFrameInfo.class.getSimpleName();
    /**
     * 资源 id (要显示空图传 0)
     */
    protected final int res;

    private String frameName;

    public ResFrameInfo(int res) {
        this.res = res;
        frameName = String.valueOf(res);
    }

    @Override
    protected Bitmap decodeBitmap(BitmapFactory.Options options, ImageFileCache fileCache, Context context) {
        if (res == 0 || context == null) {
            return null;
        }
        if (fileCache == null) {
            return BitmapFactory.decodeResource(context.getResources(), res, options);
        }
        byte[] bytes = fileCache.get(res);
        if (bytes == null) {
            byte[] fileBytes = FileUtils.readResToMem(res, context);
            fileCache.put(res, fileBytes);
            bytes = fileBytes;
        }
        if (bytes == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }

    @Override
    public String getFrameName() {
        return frameName;
    }
}
