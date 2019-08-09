package com.core.base;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by Arison on 2017/8/25.
 */

public class ActivityStack {

    private Stack<Activity> stack;
    private static ActivityStack instance;

    private ActivityStack() {
        stack = new Stack<Activity>();
    }

    public static ActivityStack getInstance() {
        if (instance == null) {
            synchronized (ActivityStack.class) {
                if (instance == null) {
                    instance = new ActivityStack();
                }
            }
        }
        return instance;
    }

    public void pop(Activity activity) {
        if (activity != null) {
            stack.remove(activity);
        }
    }

    public void push(Activity activity) {
        stack.add(activity);
    }

    public boolean has() {
        return stack.size() > 0;
    }

    public void exit() {
        for (int i = 0; i < stack.size(); i++) {
            Activity activity = stack.get(i);
            stack.remove(i);
            i--;
            if (activity != null) {
                activity.finish();
                activity = null;
            }
        }
    }
}
