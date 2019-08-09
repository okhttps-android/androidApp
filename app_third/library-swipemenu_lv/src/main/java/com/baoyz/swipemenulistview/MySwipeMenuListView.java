package com.baoyz.swipemenulistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by RaoMeng on 2016/9/7.
 */
public class MySwipeMenuListView extends SwipeMenuListView {
    private GestureDetector mGestureDetector;
    private View.OnTouchListener mGestureListener;

    public MySwipeMenuListView(Context context) {
        super(context);
        mGestureDetector = new GestureDetector(context,onGestureListener);
    }

    public MySwipeMenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context,onGestureListener);
    }

    public MySwipeMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context,onGestureListener);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean b =  mGestureDetector.onTouchEvent(ev);
        Log.w("MyLog", "-- " + b + " --");
        return super.onTouchEvent(ev);
    }

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY != 0 && distanceX != 0) {
            }
            if (Math.abs(distanceY) >= Math.abs(distanceX)) {
                return true;
            }
            //当手指触到listview的时候，让父ScrollView交出ontouch权限，也就是让父scrollview 停住不能滚动
            setParentScrollAble(false);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    /**
     * 是否把滚动事件交给父scrollview
     *
     * @param flag
     */
    private void setParentScrollAble(boolean flag) {
        //这里的parentScrollView就是listview外面的那个scrollview
        Log.w("MyLog", "setParentScrollAble -- " + flag);
        getParent().requestDisallowInterceptTouchEvent(!flag);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2 , MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
