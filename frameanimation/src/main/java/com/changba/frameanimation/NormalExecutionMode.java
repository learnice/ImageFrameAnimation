package com.changba.frameanimation;

import java.util.ArrayList;
import java.util.List;

import static com.changba.frameanimation.ImageFrameAnimation.INFINITE;
import static com.changba.frameanimation.ImageFrameAnimation.RESTART;
import static com.changba.frameanimation.ImageFrameAnimation.REVERSE;

/**
 * normal 运行模式
 * <p>
 * 支持重复播放次数、重复播放的模式
 *
 * @author HeXuebin on 3/10/21.
 */
public class NormalExecutionMode implements IExecutionMode {
    private static final String TAG = NormalExecutionMode.class.getSimpleName();
    /**
     * 动画帧数据
     */
    private final List<AbsFrameInfo> mFrameInfoList = new ArrayList<>();
    /**
     * 下一帧的索引
     */
    private int mAnimNextFrameIndex = 0;
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

    @Override
    public AbsFrameInfo getNextFrame() {
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

    @Override
    public AbsFrameInfo getCurFrame() {
        AbsFrameInfo frameInfo;
        frameInfo = mFrameInfoList.get(mAnimNextFrameIndex);
        return frameInfo;
    }

    @Override
    public boolean isRunning() {
        if (mFrameInfoList.isEmpty()) {
            return false;
        }
        if (mRepeatCount == INFINITE) {
            return true;
        }
        if (mRepeatCount + 1 == mAnimRunningTimes) {
            return false;
        }
        return true;
    }

    @Override
    public void reset() {
        mAnimNextFrameIndex = 0;
        mAnimRunningTimes = 0;
        mAnimRunningDirection = 1;
    }

    @Override
    public void clear() {
        mFrameInfoList.clear();
    }

    @Override
    public void addALL(List<AbsFrameInfo> frameInfoList) {
        mFrameInfoList.addAll(frameInfoList);
    }

    @Override
    public void setRepeatCount(int value) {
        this.mRepeatCount = value;
    }

    @Override
    public void setRepeatMode(int value) {
        this.mRepeatMode = value;
    }
}
