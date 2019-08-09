package com.common.system;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.common.LogUtil;

/**
 * 这个类存在严重bug不能够再使用了
 * 
 */
@Deprecated
public class PermissionUtil {
	public static final int DEFAULT_REQUEST = 0x31;


	// 判断权限集合
	public static boolean lacksPermissions(Context context, String... permissions) {
		if (Build.VERSION.SDK_INT >= 23) {
			for (String permission : permissions) {
				if (lacksPermission(context, permission)) {
					return true;
				}
			}
		} else {
			return false;
		}
		return false;
	}

	// 判断是否缺少权限
	private static boolean lacksPermission(Context context, String permission) {
		int status = ContextCompat.checkSelfPermission(context, permission);
		LogUtil.i("permission=" + permission);
		LogUtil.i("status=" + status);
		return status == PackageManager.PERMISSION_DENIED;
	}


	public static void requestPermission(Activity ct, int requestCode, String... permissions) {
		ActivityCompat.requestPermissions(ct, permissions, requestCode);
	}
}
