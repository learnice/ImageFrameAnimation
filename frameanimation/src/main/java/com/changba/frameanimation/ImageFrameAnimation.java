package com.changba.frameanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.widget.ImageView;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 图片帧动画：使用几张图的内存运行整个动画
 * <p>
 * 1. 使用 {@link Choreographer} 控制帧率，驱动动画运行
 * 2. 使用 线程池加载图片资源
 * 3. 使用 {@link BitmapFactory.Options#inBitmap} 复用内存，保证使用一张图的内存运行整个动画
 *
 * @author HeXuebin on 2021/1/15.
 */
public class ImageFrameAnimation {
    private static final String TAG = ImageFrameAnimation.class.getSimpleName();
    public static final boolean DEBUG = BuildConfig.DEBUG;
    /**
     * 支持的帧率（这个帧率是我们加载图片的帧率，显示帧率只能做到屏幕刷新率的负幂次）
     */
    private static final float[] SUPPORT_FPS = {60, 30, 15, 7.5f, 3.75f, 1.875f};
    /**
     * 保证帧的时间在 Vsync 之前
     */
    private static final int FRAME_DELAY_OFFSET = -5;
    /**
     * 帧率，默认 30 帧
     */
    private static final int DEFAULT_FPS = (int) SUPPORT_FPS[1];
    /**
     * 图片控件
     */
    private ImageView mImageView;
    /**
     * 任务线程池
     */
    private final ExecutorService poolExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactoryDefault());

    /**
     * 线程任务
     */
    private final FrameCallable mFrameCallable = new FrameCallable();
    /**
     * Choreographer 控制帧率，驱动动画运行
     */
    private final Choreographer choreographer = Choreographer.getInstance();
    /**
     * 动画配置：帧率
     */
    private int mFps = DEFAULT_FPS;
    /**
     * 动画配置：每帧的间隔
     */
    private long mIntervalPerFrame = 1000 / DEFAULT_FPS + FRAME_DELAY_OFFSET;
    /**
     * 帧数据
     */
    private final LinkedList<AbsFrameInfo> mFrameInfoList = new LinkedList<>();
    /**
     * 下一帧的索引
     */
    private int mAnimNextFrameIndex = 0;
    /**
     * 任务执行结果
     */
    private Future<AbsFrameInfo> future;
    /**
     * bitmap 内存复用
     */
    private final BitmapDoubleBuffer mDoubleBuffer = new BitmapDoubleBuffer();

    /**
     * 动画配置：动画重复播放次数播放
     */
    private int mRepeatCount = 0;
    /**
     * 动画配置：动画重复播放模式
     */
    private int mRepeatMode = RESTART;
    /**
     * 动画运行的次数
     */
    private int mAnimRunningTimes = 0;
    /**
     * 动画运行中变化方向（可以认为它是一个向量，可以用它来跳帧）
     */
    private int mAnimRunningDirection = 1;
    /**
     * 回调动画状态
     */
    private AnimationListener listener;

    private final ImageFileCache mFileCache = new ImageFileCache();

    private Context context;

    public ImageFrameAnimation(ImageView imageView) {
        if (imageView == null) {
            throw new NullPointerException("imageView must be non-null");
        }
        this.mImageView = imageView;
        this.context = imageView.getContext();
    }

    public void setFps(int fps) {
        this.mFps = fps;
        // 找出一个支持的 最接近的 fps
        float minDifference = Math.abs(SUPPORT_FPS[0] - fps);
        int minIndex = 0;
        for (int i = 1; i < SUPPORT_FPS.length; i++) {
            float tmp = Math.abs(SUPPORT_FPS[i] - fps);
            if (tmp < minDifference) {
                minIndex = i;
                minDifference = tmp;
            }
        }
        // 计算帧间隔时间
        int realFrameTime = (int) (1000 / SUPPORT_FPS[minIndex]);
        this.mIntervalPerFrame = realFrameTime + FRAME_DELAY_OFFSET;
    }

    public void setRepeatCount(int value) {
        mRepeatCount = value;
    }

    public void setRepeatMode(@RepeatMode int value) {
        mRepeatMode = value;
    }

    /**
     * 开始动画，会暂停当前正在播放的动画
     *
     * @param frameInfoList 帧信息
     */
    public void startAnim(List<AbsFrameInfo> frameInfoList) {
        if (frameInfoList == null) {
            return;
        }
        reset();
        mFrameInfoList.clear();
        mFrameInfoList.addAll(frameInfoList);
        postUpdate();
        if (listener != null) {
            listener.onAnimationStart();
        }
    }

    /**
     * 添加动画，在当前正在播放的动画后添加动画
     *
     * @param frameInfoList 帧信息
     */
    public void addAnim(List<AbsFrameInfo> frameInfoList) {
        if (frameInfoList == null) {
            return;
        }
        mFrameInfoList.addAll(frameInfoList);
        if (!isRunning()) {
            reset();
        }
        postUpdate();
        if (listener != null) {
            listener.onAnimationStart();
        }
    }

    /**
     * 取消动画
     */
    public void cancelAnim() {
        mFrameInfoList.clear();
        choreographer.removeFrameCallback(frameCallback);
        if (listener != null) {
            listener.onAnimationCancel();
        }
    }

    /**
     * 设置状态监听
     *
     * @param listener
     */
    public void setListener(AnimationListener listener) {
        this.listener = listener;
    }

    private void postUpdate() {
        choreographer.removeFrameCallback(frameCallback);
        choreographer.postFrameCallbackDelayed(frameCallback, mIntervalPerFrame);
    }

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            try {
                render();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                if (isRunning()) {
                    submitTask(getNextFrame());
                    postUpdate();
                }
            }
        }
    };

    private void render() throws ExecutionException, InterruptedException {
        // 动画开始
        if (future == null) {
            if (isRunning()) {
                submitTask(getCurFrame());
                postUpdate();
            } else {
                future = null;
            }
        } else {
            if (future.isDone()) {
                AbsFrameInfo frameInfo = future.get();
                if (listener != null) {
                    listener.onFrameCallBack(frameInfo);
                }
                mImageView.setImageBitmap(frameInfo.getFrameBitmap());
                if (isRunning()) {
                    submitTask(getNextFrame());
                    postUpdate();
                } else {
                    // 动画结束
                    future = null;
                }
            } else {
                // 丢帧了
                postUpdate();
                if (DEBUG) {
                    Log.e(TAG, "render: Drop frame!!!");
                }
            }
        }
    }

    private AbsFrameInfo getNextFrame() {
        AbsFrameInfo frameInfo;
        frameInfo = mFrameInfoList.get(mAnimNextFrameIndex);
        mAnimNextFrameIndex += mAnimRunningDirection;
        // 如果到边界了
        if (mAnimNextFrameIndex <= -1 || mAnimNextFrameIndex >= mFrameInfoList.size()) {
            // 运行次数++
            mAnimRunningTimes++;
            if (mRepeatMode == REVERSE) {
                mAnimRunningDirection = -mAnimRunningDirection;
                mAnimNextFrameIndex += mAnimRunningDirection;
            }
            if (mRepeatMode == RESTART) {
                mAnimNextFrameIndex = 0;
            }
        }
        return frameInfo;
    }

    private AbsFrameInfo getCurFrame() {
        AbsFrameInfo frameInfo;
        frameInfo = mFrameInfoList.get(mAnimNextFrameIndex);
        return frameInfo;
    }

    private boolean isRunning() {
        if (mRepeatCount == INFINITE) {
            return true;
        }
        if (mRepeatCount + 1 == mAnimRunningTimes) {
            if (listener != null) {
                listener.onAnimationEnd();
            }
            return false;
        }
        return true;
    }

    private void reset() {
        mAnimNextFrameIndex = 0;
        mAnimRunningTimes = 0;
        mAnimRunningDirection = 1;
    }

    private void submitTask(AbsFrameInfo frameInfo) {
        mFrameCallable.setFrameInfo(frameInfo);
        mFrameCallable.setDoubleBuffer(mDoubleBuffer);
        mFrameCallable.setFileCache(mFileCache);
        mFrameCallable.setContext(context);
        future = poolExecutor.submit(mFrameCallable);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!poolExecutor.isShutdown()) {
            poolExecutor.shutdown();
        }
    }

    /**
     * 线程工厂，管理线程创建，避免线程泛滥
     */
    private static class ThreadFactoryDefault implements ThreadFactory {
        private int tId;

        @Override
        public Thread newThread(Runnable r) {
            tId++;
            Thread t = new Thread(r, "ImageFrameAnimation - " + tId);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    private static class FrameCallable implements Callable<AbsFrameInfo> {
        private AbsFrameInfo frameInfo;
        private BitmapDoubleBuffer doubleBuffer;
        private ImageFileCache fileCache;
        private Context context;

        public void setFrameInfo(AbsFrameInfo frameInfo) {
            this.frameInfo = frameInfo;
        }

        public void setDoubleBuffer(BitmapDoubleBuffer doubleBuffer) {
            this.doubleBuffer = doubleBuffer;
        }

        public void setFileCache(ImageFileCache fileCache) {
            this.fileCache = fileCache;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        public AbsFrameInfo call() {
            long start = 0;
            if (DEBUG) {
                start = SystemClock.elapsedRealtimeNanos();
            }
            // 获取宽高
            frameInfo.decodeBitmapSize(fileCache, context);
            // 获取 buffer
            Bitmap inBitmap = doubleBuffer.getNextBuffer(frameInfo);
            // 加载图片
            Bitmap bitmap = frameInfo.loadBitmap(inBitmap, fileCache, context);
            // 处理图片
            if (bitmap.isMutable()) {
                bitmap = frameInfo.processBitmap(bitmap);
            }
            frameInfo.setFrameBitmap(bitmap);
            if (DEBUG) {
                long end = SystemClock.elapsedRealtimeNanos();
                //Log.d(TAG, "call: 加载处理照片用时 = " + (end - start) / 1000 + "微秒");
            }
            return frameInfo;
        }
    }

    @IntDef({RESTART, REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    public static final int INFINITE = -1;
}
