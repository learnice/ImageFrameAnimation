package com.hexuebin.frameanimation;

import android.graphics.Bitmap;

/**
 * bitmap 内存复用(用两张防止干扰)
 *
 * @author HeXuebin on 2021/2/6.
 */
public class BitmapDoubleBuffer {
    private static final String TAG = BitmapDoubleBuffer.class.getSimpleName();
    private final Bitmap[] doubleBuffer = new Bitmap[2];
    private int useBufferIndex = 1;

    public BitmapDoubleBuffer() {
    }

    /**
     * 获取下一张可用 buffer
     *
     * @param frameInfo 帧信息
     * @return
     */
    public Bitmap getNextBuffer(AbsFrameInfo frameInfo) {
        useBufferIndex = useBufferIndex == 0 ? 1 : 0;
        Bitmap inBitmap = doubleBuffer[useBufferIndex];
        if (!isEnough(inBitmap, frameInfo)) {
            createBuffer(frameInfo);
        }
        return doubleBuffer[useBufferIndex];
    }

    private boolean isEnough(Bitmap inBitmap, AbsFrameInfo frameInfo) {
        return inBitmap != null && BitmapUtils.canUseForInBitmap(inBitmap, frameInfo.getOptions());
    }

    /**
     * 重建 buffer
     *
     * @param frameInfo
     */
    private void createBuffer(AbsFrameInfo frameInfo) {
        doubleBuffer[0] = Bitmap.createBitmap(frameInfo.getWidth(), frameInfo.getHeight(), Bitmap.Config.ARGB_8888);
        doubleBuffer[1] = Bitmap.createBitmap(frameInfo.getWidth(), frameInfo.getHeight(), Bitmap.Config.ARGB_8888);
    }
}
