package com.hexuebin.frameanimation;

/**
 * 动画监听
 *
 * @author HeXuebin on 2021/1/16.
 */
public interface AnimationListener {
    /**
     * 动画开始
     */
    void onAnimationStart();

    /**
     * 动画结束
     */
    void onAnimationEnd();

    /**
     * 动画被取消
     */
    void onAnimationCancel();

    /**
     * 动画运行帧信息回调
     *
     * @param frameInfo
     */
    void onFrame(AbsFrameInfo frameInfo);
}
