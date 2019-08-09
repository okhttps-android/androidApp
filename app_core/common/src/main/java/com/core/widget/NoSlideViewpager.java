package com.core.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by RaoMeng on 2016/9/5.
 */
public class NoSlideViewpager extends ViewPager {
    //true时不可滑动，false时可滑动
    private boolean noSlide = false;

    public NoSlideViewpager(Context context) {
        super(context);
    }

    public NoSlideViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isNoSlide() {
        return noSlide;
    }

    public void setNoSlide(boolean noSlide) {
        this.noSlide = noSlide;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (noSlide) {

            return false;
        } else {
            return super.onTouchEvent(ev);

        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (noSlide) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);

        }
    }
}
