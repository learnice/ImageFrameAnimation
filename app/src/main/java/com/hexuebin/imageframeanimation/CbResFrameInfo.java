package com.hexuebin.imageframeanimation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.hexuebin.frameanimation.ResFrameInfo;

/**
 * @author HeXuebin on 2021/8/22.
 */
public class CbResFrameInfo extends ResFrameInfo {
    private static final String TAG = CbResFrameInfo.class.getSimpleName();
    private static final float X_START_POSITION = 0.66f;
    private static final float Y_START_POSITION = 0.5f;

    private final Bitmap[] numBitmaps;
    private final Paint mPaint = new Paint();

    public CbResFrameInfo(int res, Bitmap[] numBitmaps) {
        super(res);
        this.numBitmaps = numBitmaps;
    }

    @Override
    protected Bitmap processBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return bitmap;
        }
        if (numBitmaps == null) {
            return bitmap;
        }
        Canvas canvas = new Canvas(bitmap);
        float left = X_START_POSITION * bitmap.getWidth();
        float top = Y_START_POSITION * bitmap.getHeight();
        for (int i = 0; i < numBitmaps.length; i++) {
            canvas.drawBitmap(numBitmaps[i], left, top - numBitmaps[i].getHeight() / 2f, mPaint);
            left += numBitmaps[i].getWidth();
        }
        return bitmap;
    }
}
