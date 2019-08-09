package com.xzjmyk.pm.activity.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.core.app.AppConfig;

/**
 * 群聊UI的更新
 * 
 */
public class MucgroupUpdateUtil {

	public static final String ACTION_UPDATE = AppConfig.sPackageName + ".action.muc_group.update";// 群聊界面

	public static IntentFilter getUpdateActionFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_UPDATE);
		return intentFilter;
	}

	public static void broadcastUpdateUi(Context context) {
		context.sendBroadcast(new Intent(ACTION_UPDATE));
	}

}
