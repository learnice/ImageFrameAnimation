package com.changba.imageframeanimation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.changba.frameanimation.AbsFrameInfo;
import com.changba.frameanimation.AnimationListener;
import com.changba.frameanimation.FileFrameInfo;
import com.changba.frameanimation.ImageFrameAnimation;
import com.changba.frameanimation.LiveExecutionMode;
import com.changba.frameanimation.ResFrameInfo;

import java.util.ArrayList;
import java.util.List;

public class ImageFrameAnimationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ImageFrameAnimationActivity.class.getSimpleName();

    private ImageView mIvFrame;
    private Button mBtnSetData;
    private Button mBtnStart;
    private Button mBtnEnd;
    private ImageFrameAnimation imageFrameAnimation;
    List<AbsFrameInfo> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_frame_animation);
        setTitle("ImageFrameAnimation 组件");
        initView();
    }


    private void initView() {
        mIvFrame = findViewById(R.id.iv_frame_perfect);
        mBtnSetData = findViewById(R.id.btn_set_data);
        mBtnSetData.setOnClickListener(this);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);
        mBtnEnd = findViewById(R.id.btn_end);
        mBtnEnd.setOnClickListener(this);
        // 初始化
        imageFrameAnimation = new ImageFrameAnimation(mIvFrame, new LiveExecutionMode());
        imageFrameAnimation.setFps(60);
        imageFrameAnimation.setRepeatMode(ImageFrameAnimation.RESTART);
        imageFrameAnimation.setRepeatCount(ImageFrameAnimation.INFINITE);
        imageFrameAnimation.setListener(new AnimationListener() {
            @Override
            public void onAnimationStart() {
                Log.d(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd() {
                Log.d(TAG, "onAnimationEnd: ");
            }

            @Override
            public void onAnimationCancel() {
                Log.d(TAG, "onAnimationCancel: ");
            }

            @Override
            public void onFrame(AbsFrameInfo frameInfo) {
                Log.d(TAG, "onFrame: " + frameInfo.getFrameName());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_data:
                generateFrameInfoResData();
                break;
            case R.id.btn_start:
                imageFrameAnimation.addAnim(mData);
                break;
            case R.id.btn_end:
                imageFrameAnimation.cancelAnim();
                break;
            default:
                break;
        }
    }

    /**
     * 从资源文件加载
     */
    private void generateFrameInfoResData() {
        int[] res = getRes();
        List<AbsFrameInfo> list = new ArrayList<>();
        for (int j = 0; j < res.length; j++) {
            ResFrameInfo frameInfo = new ResFrameInfo(res[j]);
            list.add(frameInfo);
        }
        mData = list;
    }

    /**
     * 从 sd 卡加载
     */
    private void generateFrameInfoFileData() {
        List<AbsFrameInfo> list = new ArrayList<>();
        String pathBase = "/sdcard/dbg2/";
        for (int i = 0; i < 50; i++) {
            String path = pathBase + i + ".png";
            FileFrameInfo fileFrameInfo = new FileFrameInfo(path);
            list.add(fileFrameInfo);
        }
        mData = list;
    }

    private int[] getRes() {
        TypedArray typedArray = getResources().obtainTypedArray(R.array.draw_list);
        int len = typedArray.length();
        int[] resId = new int[len];
        for (int i = 0; i < len; i++) {
            resId[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return resId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageFrameAnimation != null) {
            imageFrameAnimation.cancelAnim();
        }
    }
}