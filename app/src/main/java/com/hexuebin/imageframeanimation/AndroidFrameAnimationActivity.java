package com.hexuebin.imageframeanimation;

import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class AndroidFrameAnimationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AndroidFrameAnimationActivity.class.getSimpleName();
    private ImageView mIvFrame;
    private Button mBtnSetData;
    private Button mBtnStart;
    private Button mBtnEnd;
    private AnimationDrawable animationDrawable = new AnimationDrawable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_frame_animation);
        setTitle("Android 原生帧动画");
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
        if (mIvFrame.getDrawable() != null) {
            animationDrawable = (AnimationDrawable) mIvFrame.getDrawable();
        } else {
            mIvFrame.setImageDrawable(animationDrawable);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_data:
                //generateFrameInfo();
                break;
            case R.id.btn_start:
                animationDrawable.start();
                break;
            case R.id.btn_end:
                animationDrawable.stop();
                break;
            default:
                break;
        }
    }

    private void generateFrameInfo() {
        int[] res = getRes();
        for (int re : res) {
            animationDrawable.addFrame(ResourcesCompat.getDrawable(getResources(), re, null), 42);
        }
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
}