package com.hexuebin.frameanimation

/**
 * 动画监听
 *
 * @author HeXuebin on 2021/6/26.
 */
interface AnimationListener {
    /**
     * 动画开始
     */
    fun onAnimationStart()

    /**
     * 动画结束
     */
    fun onAnimationEnd()

    /**
     * 动画被取消
     */
    fun onAnimationCancel()

    /**
     * 动画运行帧信息回调
     *
     * @param frameInfo
     */
    fun onFrame(frameInfo: AbsFrameInfo)
}