package com.hexuebin.imageframeanimation;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.hexuebin.frameanimation.AbsFrameInfo;
import com.hexuebin.frameanimation.AnimationListener;
import com.hexuebin.frameanimation.FileFrameInfo;
import com.hexuebin.frameanimation.IExecutionMode;
import com.hexuebin.frameanimation.ImageFrameAnimation;
import com.hexuebin.frameanimation.LiveExecutionMode;
import com.hexuebin.frameanimation.NormalExecutionMode;
import com.hexuebin.frameanimation.ResFrameInfo;

import java.util.ArrayList;
import java.util.List;

public class ImageFrameAnimationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ImageFrameAnimationActivity.class.getSimpleName();

    private ImageView mIvFrame;
    private Button mBtnNormalInit;
    private Button mBtnNormalStart;
    private Button mBtnLineInit;
    private Button mBtnLineAdd;
    private Button mBtnEnd;
    private ImageFrameAnimation imageFrameAnimation;
    /**
     * perfect 连续次数
     */
    private int perfectContinuous;
    private Bitmap multiBitmap;
    private final Bitmap[] numBitmaps = new Bitmap[10];
    private int[] perfectResIds;

    private final AnimationListener animationListener = new AnimationListener() {
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_frame_animation);
        setTitle("ImageFrameAnimation 组件");
        initView();
        initNumBitmap();
    }

    private void initView() {
        mIvFrame = findViewById(R.id.iv_frame_perfect);
        mBtnNormalInit = findViewById(R.id.btn_normal_init);
        mBtnNormalInit.setOnClickListener(this);
        mBtnNormalStart = findViewById(R.id.btn_normal_start);
        mBtnNormalStart.setOnClickListener(this);
        mBtnEnd = findViewById(R.id.btn_end);
        mBtnEnd.setOnClickListener(this);
        mBtnLineInit = findViewById(R.id.btn_line_init);
        mBtnLineInit.setOnClickListener(this);
        mBtnLineAdd = findViewById(R.id.btn_line_add);
        mBtnLineAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_normal_init:
                if (imageFrameAnimation != null) {
                    imageFrameAnimation.cancelAnim();
                }
                // Normal 运行模式
                IExecutionMode normalExecutionMode = new NormalExecutionMode();
                // 初始化
                imageFrameAnimation = new ImageFrameAnimation(mIvFrame, normalExecutionMode);
                // 运行帧率（默认为 30）
                imageFrameAnimation.setFps(30);
                // 设置 RepeatMode 重新开始（默认为 RESTART）
                imageFrameAnimation.setRepeatMode(ImageFrameAnimation.RESTART);
                // 设置 RepeatCount 无限循环 （默认为 0 次）
                imageFrameAnimation.setRepeatCount(ImageFrameAnimation.INFINITE);
                // 监听
                imageFrameAnimation.setListener(animationListener);
                break;
            case R.id.btn_normal_start:
                if (imageFrameAnimation == null) {
                    return;
                }
                List<AbsFrameInfo> list = generateFrameInfoResData();
                // 开始动画
                imageFrameAnimation.startAnim(list);
                break;
            case R.id.btn_line_init:
                if (imageFrameAnimation != null) {
                    imageFrameAnimation.cancelAnim();
                }
                perfectContinuous = 0;
                perfectResIds = getRes(R.array.score_perfect_array);
                // Live 运行模式
                IExecutionMode liveExecutionMode = new LiveExecutionMode();
                // 创建 ImageFrameAnimation 传入需要的 IExecutionMode
                imageFrameAnimation = new ImageFrameAnimation(mIvFrame, liveExecutionMode);
                // 运行帧率（默认为 30）
                imageFrameAnimation.setFps(30);
                // 监听
                imageFrameAnimation.setListener(animationListener);
                break;
            case R.id.btn_line_add:
                if (imageFrameAnimation == null) {
                    return;
                }
                if (perfectResIds == null) {
                    perfectResIds = getRes(R.array.score_perfect_array);
                }
                Bitmap[] numBitmaps = generateBitmapNumData(++perfectContinuous);
                List<AbsFrameInfo> list1 = generatePerfectAnimRes(numBitmaps);
                // 如果选用了 Live 运行方式，需要调用 addAnim 给组件添加资源，供组件消费（主线程调用）
                imageFrameAnimation.addAnim(list1);
                break;
            case R.id.btn_end:
                if (imageFrameAnimation == null) {
                    return;
                }
                perfectContinuous = 0;
                // cancel 动画
                imageFrameAnimation.cancelAnim();
                break;
            default:
                break;
        }
    }

    /**
     * 从资源文件加载
     */
    private List<AbsFrameInfo> generateFrameInfoResData() {
        int[] res = getRes(R.array.draw_list);
        List<AbsFrameInfo> list = new ArrayList<>();
        for (int j = 0; j < res.length; j++) {
            ResFrameInfo frameInfo = new MonitorResFrameInfo(res[j], j);
            list.add(frameInfo);
        }
        return list;
    }

    /**
     * 从 sd 卡加载
     */
    private List<AbsFrameInfo> generateFrameInfoFileData() {
        List<AbsFrameInfo> list = new ArrayList<>();
        String pathBase = "/sdcard/dbg2/";
        for (int i = 0; i < 50; i++) {
            String path = pathBase + i + ".png";
            FileFrameInfo fileFrameInfo = new FileFrameInfo(path);
            list.add(fileFrameInfo);
        }
        return list;
    }

    private int[] getRes(int arrayId) {
        TypedArray typedArray = getResources().obtainTypedArray(arrayId);
        int len = typedArray.length();
        int[] resId = new int[len];
        for (int i = 0; i < len; i++) {
            resId[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return resId;
    }

    private void initNumBitmap() {
        multiBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_mult);
        numBitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num0);
        numBitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num1);
        numBitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num2);
        numBitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num3);
        numBitmaps[4] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num4);
        numBitmaps[5] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num5);
        numBitmaps[6] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num6);
        numBitmaps[7] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num7);
        numBitmaps[8] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num8);
        numBitmaps[9] = BitmapFactory.decodeResource(getResources(), R.drawable.score_effect_num9);
    }

    /**
     * 数字传换为对应图片
     *
     * @param num
     * @return
     */
    private Bitmap[] generateBitmapNumData(int num) {
        if (num < 2) {
            return null;
        }
        String numString = String.valueOf(num);
        Bitmap[] bitmaps = new Bitmap[numString.length() + 1];
        bitmaps[0] = multiBitmap;
        for (int i = 1; i < bitmaps.length; i++) {
            bitmaps[i] = numBitmaps[Integer.parseInt(numString.substring(i - 1, i))];
        }
        return bitmaps;
    }

    /**
     * 生成动画每一帧
     *
     * @return
     */
    private List<AbsFrameInfo> generatePerfectAnimRes(Bitmap[] numBitmaps) {
        List<AbsFrameInfo> list = new ArrayList<>();
        for (int j = 0; j < perfectResIds.length; j++) {
            CbResFrameInfo frameInfo;
            if (j > 11 && j < perfectResIds.length - 2) {
                frameInfo = new CbResFrameInfo(perfectResIds[j], numBitmaps);
            } else {
                frameInfo = new CbResFrameInfo(perfectResIds[j], null);
            }
            list.add(frameInfo);
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageFrameAnimation != null) {
            imageFrameAnimation.cancelAnim();
        }
    }
}