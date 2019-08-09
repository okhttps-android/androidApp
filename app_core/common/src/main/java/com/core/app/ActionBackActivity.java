package com.core.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.core.base.StackActivity;
import com.core.utils.CommonUtil;


public class ActionBackActivity extends StackActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
        mConfig = MyApplication.getInstance().getConfig();
        mContext = this;
        if (AppConfig.DEBUG) {
            Log.e(AppConfig.TAG, TAG + " onCreate");
        }
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
        CommonUtil.closeKeybord(this);
        super.finish();
        overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
    }

    protected void finish(boolean anim) {
        if (anim) {
            finish();
        } else {
            super.finish();
        }
    }
}
