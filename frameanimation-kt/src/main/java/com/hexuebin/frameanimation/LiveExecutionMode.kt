package com.hexuebin.frameanimation

import java.util.LinkedList


/**
 * Live 运行模式
 * <p>
 * 每次会从 link remove 一个去播放，直到 LinkedList#isEmpty() 返回 true
 *
 * @author HeXuebin on 2021/6/26.
 */
class LiveExecutionMode : IExecutionMode {
    private val TAG: String = "LiveExecutionMode"

    /**
     * 动画帧数据
     */
    private val mFrameInfoList: LinkedList<AbsFrameInfo> = LinkedList<AbsFrameInfo>()

    override fun nextFrame(): AbsFrameInfo {
        return mFrameInfoList.remove()
    }

    override fun isRunning(): Boolean {
        return !mFrameInfoList.isEmpty()
    }

    override fun reset() {}

    override fun clear() {
        mFrameInfoList.clear()
    }

    override fun addAll(frameInfoList: List<AbsFrameInfo>) {
        mFrameInfoList.addAll(frameInfoList)
    }
}