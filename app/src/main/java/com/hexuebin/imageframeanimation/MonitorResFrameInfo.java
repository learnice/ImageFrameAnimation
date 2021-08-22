package com.hexuebin.imageframeanimation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.hexuebin.frameanimation.ResFrameInfo;

/**
 * @author HeXuebin on 2021/8/22.
 */
public class MonitorResFrameInfo extends ResFrameInfo {
    public static final String TAG = "MonitorResFrameInfo";
    private final Paint mPaint = new Paint();

    private int frames;

    public MonitorResFrameInfo(int res, int frames) {
        super(res);
        this.frames = frames;
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1f);
        mPaint.setTextSize(16f);
    }

    @Override
    protected Bitmap processBitmap(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(String.valueOf(frames), getWidth() / 2f, 50f, mPaint);
        return super.processBitmap(bitmap);
    }
}
