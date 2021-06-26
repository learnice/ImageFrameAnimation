package com.hexuebin.frameanimation;

import java.util.LinkedList;
import java.util.List;

/**
 * Live 运行模式
 * <p>
 * 每次会从 link remove 一个去播放，直到 LinkedList#isEmpty() 返回 true
 *
 * @author HeXuebin on 3/10/21.
 */
public class LiveExecutionMode implements IExecutionMode {
    private static final String TAG = LiveExecutionMode.class.getSimpleName();
    /**
     * 动画帧数据
     */
    private final LinkedList<AbsFrameInfo> mFrameInfoList = new LinkedList<>();

    @Override
    public AbsFrameInfo nextFrame() {
        AbsFrameInfo frameInfo = mFrameInfoList.remove();
        return frameInfo;
    }

    @Override
    public boolean isRunning() {
        return !mFrameInfoList.isEmpty();
    }

    @Override
    public void reset() {

    }

    @Override
    public void clear() {
        mFrameInfoList.clear();
    }

    @Override
    public void addAll(List<AbsFrameInfo> frameInfoList) {
        mFrameInfoList.addAll(frameInfoList);
    }
}
