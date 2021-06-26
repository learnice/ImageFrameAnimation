package com.hexuebin.frameanimation

/**
 * 运行模式接口，不同的模式需要自行实现
 *
 * @author HeXuebin on 2021/6/26.
 */
interface IExecutionMode {
    /**
     * 获取下一帧数据
     *
     * @return
     */
    fun nextFrame(): AbsFrameInfo

    /**
     * 是否是运行状态
     *
     * @return
     */
    fun isRunning(): Boolean

    /**
     * 重置运行参数
     */
    fun reset()

    /**
     * 清除动画帧队列
     */
    fun clear()

    /**
     * 添加动画帧数据
     *
     * @param frameInfoList
     */
    fun addAll(frameInfoList: List<AbsFrameInfo>)

    /**
     * 默认方法设置重试次数
     *
     * @param value
     */
    fun setRepeatCount(value: Int) {}

    /**
     * 设置重试的模式
     *
     * @param value
     */
    fun setRepeatMode(@RepeatMode value: Int) {}
}