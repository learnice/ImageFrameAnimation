package com.hexuebin.frameanimation;

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
     * 最大帧率（默认设备帧率）
     */
    private static final int DEFAULT_MAX_FPS = 60;
    /**
     * 最小帧率
     */
    private static final int DEFAULT_MIN_FPS = 1;
    /**
     * 帧率，默认 30 帧
     */
    private static final int DEFAULT_FPS = 30;
    /**
     * 保证帧的时间在 Vsync 之前
     */
    private static final int FRAME_DELAY_OFFSET = -5;
    /**
     * 一帧的时间
     */
    private static final float DEFAULT_A_FRAME_OF_TIME = 1000f / DEFAULT_MAX_FPS;
    /**
     * 图片控件
     */
    private ImageView mImageView;
    private Context context;
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
     * 任务执行结果
     */
    private Future<AbsFrameInfo> future;
    /**
     * bitmap 内存复用
     */
    private final BitmapDoubleBuffer mDoubleBuffer = new BitmapDoubleBuffer();
    /**
     * 文件缓存
     */
    private final ImageFileCache mFileCache = new ImageFileCache();
    /**
     * 动画运行的模式
     */
    private final IExecutionMode mExecutionMode;
    /**
     * 回调动画状态
     */
    private AnimationListener listener;

    /**
     * 构造方法，采用 {@link NormalExecutionMode} 模式运行
     *
     * @param imageView 需要运行动画的 View
     */
    public ImageFrameAnimation(ImageView imageView) {
        if (imageView == null) {
            throw new NullPointerException("imageView must be non-null");
        }
        this.mImageView = imageView;
        this.context = imageView.getContext();
        this.mExecutionMode = new NormalExecutionMode();
    }

    /**
     * 构造方法
     *
     * @param imageView     需要运行动画的 View
     * @param executionMode 动画运行模式，默认实现了 {@link NormalExecutionMode}、{@link NormalExecutionMode}
     */
    public ImageFrameAnimation(ImageView imageView, IExecutionMode executionMode) {
        if (imageView == null) {
            throw new NullPointerException("imageView must be non-null");
        }
        if (executionMode == null) {
            throw new NullPointerException("executionMode must be non-null");
        }
        this.mImageView = imageView;
        this.context = imageView.getContext();
        this.mExecutionMode = executionMode;
    }

    public void setFps(int fps) {
        this.mFps = Math.min(Math.max(DEFAULT_MIN_FPS, fps), DEFAULT_MAX_FPS);
        int stride = DEFAULT_MAX_FPS / mFps;
        mIntervalPerFrame = (long) (stride * DEFAULT_A_FRAME_OF_TIME + FRAME_DELAY_OFFSET);
    }

    public void setRepeatCount(int value) {
        mExecutionMode.setRepeatCount(value);
    }

    public void setRepeatMode(@RepeatMode int value) {
        mExecutionMode.setRepeatMode(value);
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
        clear();
        addAll(frameInfoList);
        postUpdate();
        callbackOnStart();
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
        if (!isRunning()) {
            reset();
            callbackOnStart();
        }
        addAll(frameInfoList);
        postUpdate();
    }

    /**
     * 取消动画
     */
    public void cancelAnim() {
        clear();
        choreographer.removeFrameCallback(frameCallback);
        callbackOnCancel();
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

    private final Choreographer.FrameCallback frameCallback = frameTimeNanos -> {
        try {
            render();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            if (isRunning()) {
                submitTask(getNextFrame());
                postUpdate();
            }
        }
    };

    private void render() throws ExecutionException, InterruptedException {
        // 动画开始
        if (future == null) {
            if (isRunning()) {
                submitTask(getNextFrame());
                postUpdate();
            } else {
                future = null;
            }
        } else {
            if (future.isDone()) {
                AbsFrameInfo frameInfo = future.get();
                callbackOnFrame(frameInfo);
                mImageView.setImageBitmap(frameInfo.getFrameBitmap());
                if (isRunning()) {
                    submitTask(getNextFrame());
                    postUpdate();
                } else {
                    // 动画结束
                    future = null;
                    callbackOnEnd();
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

    /**
     * 获取下一帧
     *
     * @return
     */
    private AbsFrameInfo getNextFrame() {
        return mExecutionMode.nextFrame();
    }

    /**
     * 是否运行状态
     *
     * @return
     */
    private boolean isRunning() {
        return mExecutionMode.isRunning();
    }

    /**
     * 重置
     */
    private void reset() {
        mExecutionMode.reset();
    }

    /**
     * 添加所有数据
     *
     * @param frameInfoList
     */
    private void addAll(List<AbsFrameInfo> frameInfoList) {
        mExecutionMode.addAll(frameInfoList);
    }

    /**
     * 清除数据
     */
    private void clear() {
        mExecutionMode.clear();
    }

    /**
     * 回调开始
     */
    private void callbackOnStart() {
        if (listener != null) {
            listener.onAnimationStart();
        }
    }

    /**
     * 回调结束
     */
    private void callbackOnEnd() {
        if (listener != null) {
            listener.onAnimationEnd();

        }
    }

    /**
     * 回调取消
     */
    private void callbackOnCancel() {
        if (listener != null) {
            listener.onAnimationCancel();
        }
    }

    /**
     * 回调当前播放的帧
     *
     * @param frameInfo
     */
    private void callbackOnFrame(AbsFrameInfo frameInfo) {
        if (listener != null) {
            listener.onFrame(frameInfo);
        }
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
