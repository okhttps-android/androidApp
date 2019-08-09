package com.core.app;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by FANGlh on 2017/8/7.
 * function:
 */

public class MyActivityManager {
    private static MyActivityManager sInstance = new MyActivityManager();
    private static WeakReference<Activity> sCurrentActivityWeakRef;


    private MyActivityManager() {

    }

    public static MyActivityManager getInstance() {
        return sInstance;
    }

    public static Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        sCurrentActivityWeakRef = new WeakReference<Activity>(activity);
    }
}
