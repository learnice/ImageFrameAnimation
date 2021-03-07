package com.changba.imageframeanimation;

import android.app.Application;

/**
 * @author HeXuebin on 2020-01-04.
 */
public class App extends Application {


    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
    }
}
