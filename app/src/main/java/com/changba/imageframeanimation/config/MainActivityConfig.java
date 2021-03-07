package com.changba.imageframeanimation.config;


import com.changba.imageframeanimation.AndroidFrameAnimationActivity;
import com.changba.imageframeanimation.ImageFrameAnimationActivity;

/**
 * @author HeXuebin on 2020/12/27.
 */
public enum MainActivityConfig implements IActivityConfig {

    ANDROID_FRAME_ANIMATION("Android 原生帧动画", AndroidFrameAnimationActivity.class),

    IMAGE_FRAME_ANIMATION("ImageFrameAnimation", ImageFrameAnimationActivity.class);

    private final String name;
    private final Class c;

    MainActivityConfig(String name, Class c) {
        this.name = name;
        this.c = c;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class getClazz() {
        return c;
    }
}
