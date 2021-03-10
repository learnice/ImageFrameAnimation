package com.changba.frameanimation;

import java.util.List;

/**
 * 运行模式接口，不同的模式需要自行实现
 *
 * @author HeXuebin on 3/10/21.
 */
public interface IExecutionMode {
    /**
     * 获取下一帧数据
     *
     * @return
     */
    AbsFrameInfo getNextFrame();

    /**
     * 获取当前帧数据
     *
     * @return
     */
    AbsFrameInfo getCurFrame();

    /**
     * 是否是运行状态
     *
     * @return
     */
    boolean isRunning();

    /**
     * 重置运行参数
     */
    void reset();

    /**
     * 清除动画帧队列
     */
    void clear();

    /**
     * 添加动画帧数据
     *
     * @param frameInfoList
     */
    void addALL(List<AbsFrameInfo> frameInfoList);

    /**
     * 默认方法色通知重试次数
     *
     * @param value
     */
    default void setRepeatCount(int value) {

    }

    /**
     * 设置重试的模式
     *
     * @param value
     */
    default void setRepeatMode(@ImageFrameAnimation.RepeatMode int value) {

    }
}
