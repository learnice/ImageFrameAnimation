package com.hexuebin.frameanimation

import java.util.*

/**
 * normal 运行模式
 * <p>
 * 支持重复播放次数、重复播放的模式
 *
 * @author HeXuebin on 2021/6/26.
 */
class NormalExecutionMode : IExecutionMode {
    private val TAG: String = "NormalExecutionMode"

    /**
     * 动画帧数据
     */
    private val mFrameInfoList: MutableList<AbsFrameInfo> =
        ArrayList<AbsFrameInfo>()

    /**
     * 下一帧的索引
     */
    private var mAnimNextFrameIndex = 0

    /**
     * 动画配置：动画重复播放次数播放
     */
    private var mRepeatCount = 0

    /**
     * 动画配置：动画重复播放模式
     */
    private var mRepeatMode: Int = RESTART

    /**
     * 动画运行的次数
     */
    private var mAnimRunningTimes = 0

    /**
     * 动画运行中变化方向（可以认为它是一个向量，可以用它来跳帧）
     */
    private var mAnimRunningDirection = 1

    override fun nextFrame(): AbsFrameInfo {
        val frameInfo: AbsFrameInfo = mFrameInfoList[mAnimNextFrameIndex]
        mAnimNextFrameIndex += mAnimRunningDirection
        // 如果到边界了
        if (mAnimNextFrameIndex <= -1 || mAnimNextFrameIndex >= mFrameInfoList.size) {
            // 运行次数++
            mAnimRunningTimes++
            if (mRepeatMode == REVERSE) {
                mAnimRunningDirection = -mAnimRunningDirection
                mAnimNextFrameIndex += mAnimRunningDirection
            }
            if (mRepeatMode == RESTART) {
                mAnimNextFrameIndex = 0
            }
        }
        return frameInfo
    }

    override fun isRunning(): Boolean {
        if (mFrameInfoList.isEmpty()) {
            return false
        }
        if (mRepeatCount == INFINITE) {
            return true
        }
        return mRepeatCount + 1 != mAnimRunningTimes
    }

    override fun reset() {
        mAnimNextFrameIndex = 0
        mAnimRunningTimes = 0
        mAnimRunningDirection = 1
    }

    override fun clear() {
        mFrameInfoList.clear()
    }

    override fun addAll(frameInfoList: List<AbsFrameInfo>) {
        mFrameInfoList.addAll(frameInfoList)
    }

    override fun setRepeatCount(value: Int) {
        mRepeatCount = value
    }

    override fun setRepeatMode(value: Int) {
        mRepeatMode = value
    }
}