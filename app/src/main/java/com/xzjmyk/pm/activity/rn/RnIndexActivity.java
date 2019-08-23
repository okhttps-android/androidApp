package com.xzjmyk.pm.activity.rn;

import android.content.Intent;
import android.content.res.Configuration;

import com.facebook.react.ReactActivity;

public class RnIndexActivity extends ReactActivity {

    @Override
    protected String getMainComponentName() {
        return "UU_RN";
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent intent = new Intent("onConfigurationChanged");
        intent.putExtra("newConfig", newConfig);
        this.sendBroadcast(intent);
    }
}
