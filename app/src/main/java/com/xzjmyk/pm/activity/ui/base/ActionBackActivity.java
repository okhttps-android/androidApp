package com.xzjmyk.pm.activity.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;

public class ActionBackActivity extends StackActivity {

    //手指上下滑动时的最小速度
    private static final int YSPEED_MIN = 200;
    //手指向右滑动时的最小距离
    private static final int XDISTANCE_MIN = 200;

    private float downX, downY;//按下的坐标
    //记录手指移动时的坐标。
    private float moveX;
    //用于计算手指滑动的速度。
    private VelocityTracker speed;


    protected Context mContext;
    public AppConfig mConfig;
    private boolean isDestroyed = false;
    protected String TAG;// 获取Tag，用于日志输出等标志

    public ActionBackActivity() {
        TAG = this.getClass().getSimpleName();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!b) {
            createVelocityTracker(ev);
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //滑动的距离
                    moveX = ev.getRawX() - downX;
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mConfig = MyApplication.getInstance().getConfig();
        mContext = this;
        if (AppConfig.DEBUG) {
            Log.e(AppConfig.TAG, TAG + " onCreate");
        }
    }

    /**
     * 创建VelocityTracker对象，并将触摸界面的滑动事件加入到VelocityTracker当中。
     *
     * @param event
     */
    private void createVelocityTracker(MotionEvent event) {
        if (speed == null) {
            speed = VelocityTracker.obtain();
        }
        speed.addMovement(event);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        speed.recycle();
        speed = null;
    }

    /**
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        speed.computeCurrentVelocity(1000);
        int velocity = (int) speed.getYVelocity();
        return Math.abs(velocity);
    }

    @Override
    public boolean isDestroyed() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return super.isDestroyed();
        }
        return isDestroyed;
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
//        mConfig = null;
        mContext = null;
        if (AppConfig.DEBUG) {
            Log.e(AppConfig.TAG, TAG + " onDestroy");
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return onHomeAsUp();
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean onHomeAsUp() {
        finish();
        overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    private void touchUp() {
        if (getScrollVelocity() > YSPEED_MIN && moveX > XDISTANCE_MIN) {
            recycleVelocityTracker();
//            finish();
        } else {
            recycleVelocityTracker();
        }

    }

    private boolean b;

    protected void setNotTouchEvent(boolean b) {
        this.b = b;
    }


    @Override
    public void startActivity(Intent intent) {
        if (intent.getAction() != null) {
            if (!intent.getAction().equals(Intent.ACTION_MAIN)) {
                super.startActivity(intent);
                overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
            } else {
                super.startActivity(intent);
                overridePendingTransition(R.anim.anim_to_main_in, R.anim.anim_to_main_out);
            }
        } else {
            super.startActivity(intent);
            overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
    }
}
