package com.xzjmyk.pm.activity.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uas.appworks.OA.erp.utils.AutoErpSigninUitl;


/**
 * 日期改变监听器
 * <p>
 * Created by Bitliker on 2017/2/10.
 */

public class DateTimeReceiver extends BroadcastReceiver {
    private final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_DATE_CHANGED.equals(action)) {
            AutoErpSigninUitl signinUitl = new AutoErpSigninUitl();
            signinUitl.loadMissionPlan();
        }
    }
}
