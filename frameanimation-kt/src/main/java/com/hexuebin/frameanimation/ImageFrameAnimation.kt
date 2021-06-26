package com.hexuebin.frameanimation

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.widget.ImageView
import java.util.concurrent.*

/**
 * 图片帧动画：使用几张图的内存运行整个动画
 * <p>
 * 1. 使用 {@link Choreographer} 控制帧率，驱动动画运行
 * 2. 使用 线程池加载图片资源
 * 3. 使用 {@link BitmapFactory.Options#inBitmap} 复用内存，保证使用一张图的内存运行整个动画
 *
 * @author HeXuebin on 2021/6/26.
 */
class ImageFrameAnimation {
    private val TAG: String = "ImageFrameAnimation"

    companion object {
        const val DEBUG: Boolean = true

        /**
         * 最大帧率（默认设备帧率）
         */
        private const val DEFAULT_MAX_FPS = 60

        /**
         * 最小帧率
         */
        private const val DEFAULT_MIN_FPS = 1

        /**
         * 帧率，默认 30 帧
         */
        private const val DEFAULT_FPS = 30

        /**
         * 保证帧的时间在 Vsync 之前
         */
        private const val FRAME_DELAY_OFFSET = -5

        /**
         * 一帧的时间
         */
        private const val DEFAULT_A_FRAME_OF_TIME = 1000f / DEFAULT_MAX_FPS
    }

    /**
     * 图片控件
     */
    private var mImageView: ImageView
    private var context: Context

    /**
     * 任务线程池
     */
    private val poolExecutor: ExecutorService = ThreadPoolExecutor(
        1, 1,
        0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(), ThreadFactoryDefault()
    )

    /**
     * 线程任务
     */
    private val mFrameCallable = FrameCallable()

    /**
     * Choreographer 控制帧率，驱动动画运行
     */
    private val choreographer = Choreographer.getInstance()

    /**
     * 动画配置：帧率
     */
    private var mFps = DEFAULT_FPS

    /**
     * 动画配置：每帧的间隔
     */
    private var mIntervalPerFrame = (1000 / DEFAULT_FPS + FRAME_DELAY_OFFSET).toLong()

    /**
     * 任务执行结果
     */
    private var future: Future<AbsFrameInfo>? = null

    /**
     * bitmap 内存复用
     */
    private val mDoubleBuffer: BitmapDoubleBuffer = BitmapDoubleBuffer()

    /**
     * 文件缓存
     */
    private val mFileCache: ImageFileCache = ImageFileCache()

    /**
     * 动画运行的模式
     */
    private val mExecutionMode: IExecutionMode

    /**
     * 回调动画状态
     */
    private var listener: AnimationListener? = null

    /**
     * 构造方法，采用 [NormalExecutionMode] 模式运行
     *
     * @param imageView 需要运行动画的 View
     */
    constructor(imageView: ImageView) {
        mImageView = imageView
        context = imageView.context
        mExecutionMode = NormalExecutionMode()
    }

    /**
     * 构造方法
     *
     * @param imageView     需要运行动画的 View
     * @param executionMode 动画运行模式，默认实现了 [NormalExecutionMode]、[NormalExecutionMode]
     */
    constructor(
        imageView: ImageView,
        executionMode: IExecutionMode
    ) {
        mImageView = imageView
        context = imageView.context
        mExecutionMode = executionMode
    }

    fun setFps(fps: Int) {
        mFps = Math.min(Math.max(DEFAULT_MIN_FPS, fps), DEFAULT_MAX_FPS)
        val stride = DEFAULT_MAX_FPS / mFps
        mIntervalPerFrame = (stride * DEFAULT_A_FRAME_OF_TIME + FRAME_DELAY_OFFSET).toLong()
    }

    fun setRepeatCount(value: Int) {
        mExecutionMode.setRepeatCount(value)
    }

    fun setRepeatMode(@RepeatMode value: Int) {
        mExecutionMode.setRepeatMode(value)
    }

    /**
     * 开始动画，会暂停当前正在播放的动画
     *
     * @param frameInfoList 帧信息
     */
    fun startAnim(frameInfoList: List<AbsFrameInfo>?) {
        if (frameInfoList == null) {
            return
        }
        reset()
        clear()
        addAll(frameInfoList)
        postUpdate()
        callbackOnStart()
    }

    /**
     * 添加动画，在当前正在播放的动画后添加动画
     *
     * @param frameInfoList 帧信息
     */
    fun addAnim(frameInfoList: List<AbsFrameInfo>?) {
        if (frameInfoList == null) {
            return
        }
        if (!isRunning()) {
            reset()
            callbackOnStart()
        }
        addAll(frameInfoList)
        postUpdate()
    }

    /**
     * 取消动画
     */
    fun cancelAnim() {
        clear()
        choreographer.removeFrameCallback(frameCallback)
        callbackOnCancel()
    }

    /**
     * 设置状态监听
     *
     * @param listener
     */
    fun setListener(listener: AnimationListener?) {
        this.listener = listener
    }

    private fun postUpdate() {
        choreographer.removeFrameCallback(frameCallback)
        choreographer.postFrameCallbackDelayed(frameCallback, mIntervalPerFrame)
    }

    private val frameCallback = FrameCallback {
        try {
            render()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            if (isRunning()) {
                submitTask(getNextFrame())
                postUpdate()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            if (isRunning()) {
                submitTask(getNextFrame())
                postUpdate()
            }
        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun render() {
        // 动画开始
        if (future == null) {
            if (isRunning()) {
                submitTask(getNextFrame())
                postUpdate()
            } else {
                future = null
            }
        } else {
            if (future!!.isDone) {
                val frameInfo: AbsFrameInfo = future!!.get()
                callbackOnFrame(frameInfo)
                mImageView.setImageBitmap(frameInfo.getFrameBitmap())
                if (isRunning()) {
                    submitTask(getNextFrame())
                    postUpdate()
                } else {
                    // 动画结束
                    future = null
                    callbackOnEnd()
                }
            } else {
                // 丢帧了
                postUpdate()
                if (DEBUG) {
                    Log.e(TAG, "render: Drop frame!!!")
                }
            }
        }
    }

    /**
     * 获取下一帧
     *
     * @return
     */
    private fun getNextFrame(): AbsFrameInfo {
        return mExecutionMode.nextFrame()
    }

    /**
     * 是否运行状态
     *
     * @return
     */
    private fun isRunning(): Boolean {
        return mExecutionMode.isRunning()
    }

    /**
     * 重置
     */
    private fun reset() {
        mExecutionMode.reset()
    }

    /**
     * 添加所有数据
     *
     * @param frameInfoList
     */
    private fun addAll(frameInfoList: List<AbsFrameInfo>) {
        mExecutionMode.addAll(frameInfoList)
    }

    /**
     * 清除数据
     */
    private fun clear() {
        mExecutionMode.clear()
    }

    /**
     * 回调开始
     */
    private fun callbackOnStart() {
        listener?.onAnimationStart()
    }

    /**
     * 回调结束
     */
    private fun callbackOnEnd() {
        listener?.onAnimationEnd()
    }

    /**
     * 回调取消
     */
    private fun callbackOnCancel() {
        listener?.onAnimationCancel()
    }

    /**
     * 回调当前播放的帧
     *
     * @param frameInfo
     */
    private fun callbackOnFrame(frameInfo: AbsFrameInfo) {
        listener?.onFrame(frameInfo)
    }

    private fun submitTask(frameInfo: AbsFrameInfo) {
        mFrameCallable.setFrameInfo(frameInfo)
        mFrameCallable.setDoubleBuffer(mDoubleBuffer)
        mFrameCallable.setFileCache(mFileCache)
        mFrameCallable.setContext(context)
        future = poolExecutor.submit(mFrameCallable)
    }

    protected fun finalize() {
        if (!poolExecutor.isShutdown) {
            poolExecutor.shutdown()
        }
    }

    /**
     * 线程工厂，管理线程创建，避免线程泛滥
     */
    private class ThreadFactoryDefault : ThreadFactory {
        private var tId = 0
        override fun newThread(r: Runnable): Thread {
            tId++
            val t = Thread(r, "ImageFrameAnimation - $tId")
            t.priority = Thread.NORM_PRIORITY
            return t
        }
    }

    private class FrameCallable : Callable<AbsFrameInfo> {
        private lateinit var frameInfo: AbsFrameInfo
        private lateinit var doubleBuffer: BitmapDoubleBuffer
        private lateinit var fileCache: ImageFileCache
        private lateinit var context: Context

        fun setFrameInfo(frameInfo: AbsFrameInfo) {
            this.frameInfo = frameInfo
        }

        fun setDoubleBuffer(doubleBuffer: BitmapDoubleBuffer) {
            this.doubleBuffer = doubleBuffer
        }

        fun setFileCache(fileCache: ImageFileCache) {
            this.fileCache = fileCache
        }

        fun setContext(context: Context) {
            this.context = context
        }

        override fun call(): AbsFrameInfo {
            var start: Long = 0
            if (DEBUG) {
                start = SystemClock.elapsedRealtimeNanos()
            }
            // 获取宽高
            frameInfo.decodeBitmapSize(fileCache, context)
            // 获取 buffer
            val inBitmap: Bitmap = doubleBuffer.getNextBuffer(frameInfo)
            // 加载图片
            var bitmap: Bitmap? = frameInfo.loadBitmap(inBitmap, fileCache, context)
            // 处理图片
            if (bitmap?.isMutable == true) {
                bitmap = frameInfo.processBitmap(bitmap)
            }
            frameInfo.setFrameBitmap(bitmap)
            if (DEBUG) {
                val end = SystemClock.elapsedRealtimeNanos()
                //Log.d(TAG, "call: 加载处理照片用时 = " + (end - start) / 1000 + "微秒");
            }
            return frameInfo
        }
    }
}