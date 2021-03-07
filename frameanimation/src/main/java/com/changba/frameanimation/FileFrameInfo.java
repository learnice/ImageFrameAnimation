package com.changba.frameanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

/**
 * 文件帧信息
 * <p>
 * 这种实现会从内部存储中加载 bitmap
 *
 * @author HeXuebin on 2021/1/16.
 */
public class FileFrameInfo extends AbsFrameInfo {
    private static final String TAG = FileFrameInfo.class.getSimpleName();
    /**
     * 图片地址 (要显示空图传 null)
     */
    protected final String path;

    public FileFrameInfo(String path) {
        this.path = path;
    }

    @Override
    protected Bitmap decodeBitmap(BitmapFactory.Options options, ImageFileCache fileCache, Context context) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (fileCache == null) {
            return BitmapFactory.decodeFile(path, options);
        }
        byte[] bytes = fileCache.get(path);
        if (bytes == null) {
            byte[] fileBytes = FileUtils.readFileToMem(path);
            fileCache.put(path, fileBytes);
            bytes = fileBytes;
        }
        if (bytes == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }
}
