package com.common;

import android.text.TextUtils;
import android.util.Log;

import com.common.config.BaseConfig;
import com.common.data.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

	public static void d(String TAG, String method, String msg) {
		Log.d(TAG, "[" + method + "]" + msg);
	}

	public static void d(String TAG, String msg) {
//		if (BaseConfig.showLogAble()) {
			Log.d(TAG, "[" + getFileLineMethod() + "]" + msg);
//		}
	}

	public static void d(String msg) {
		if (BaseConfig.showLogAble() && !TextUtils.isEmpty(msg)) {
			Log.d(_FILE_(), "[" + getLineMethod() + "]" + msg);
		}
	}

	public static void e(String msg) {
		if (BaseConfig.showLogAble()) {
			Log.e(_FILE_(), getLineMethod() + msg);
		}
	}

	public static void e(String TAG, String msg) {
		if (BaseConfig.showLogAble()) {
			Log.e(TAG, getLineMethod() + msg);
		}
	}

	public static void i(String msg) {
		prinlnLongMsg("gongpengming", msg);

	}

	public static void i(String tag, String msg) {
		if (BaseConfig.showLogAble()) {
			if (!StringUtil.isEmpty(msg)) {
				if (TextUtils.isEmpty(tag)) {
					Log.i("gongpengming", msg);
				} else {
					prinlnLongMsg(tag, msg);
				}
			}
		}
	}

	public static String getFileLineMethod() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[")
				.append(traceElement.getFileName()).append(" | ")
				.append(traceElement.getLineNumber()).append(" | ")
				.append(traceElement.getMethodName()).append("]");
		return toStringBuffer.toString();
	}

	public static String getLineMethod() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		StringBuffer toStringBuffer = new StringBuffer("[")
				.append(traceElement.getLineNumber()).append(" | ")
				.append(traceElement.getMethodName()).append("]");
		return toStringBuffer.toString();
	}

	public static String _FILE_() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
		return traceElement.getFileName();
	}

	public static String _FUNC_() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getMethodName();
	}

	public static int _LINE_() {
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getLineNumber();
	}

	public static String _TIME_() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return sdf.format(now);
	}

	public static void prinlnLongMsg(String TAG, String responseInfo) {
		if (responseInfo != null) {
			if (responseInfo.length() >=3000) {
				Log.v(TAG, "sb.length = " + responseInfo.length());
				int chunkCount = responseInfo.length() / 3000;     // integer division
				for (int i = 0; i <= chunkCount; i++) {
					int max = 3000 * (i + 1);
					if (max >= responseInfo.length()) {
						Log.i(TAG, "【"+ i + "】" + responseInfo.substring(3000 * i));
					} else {
						Log.i(TAG, "【" + i+ "】" + responseInfo.substring(3000 * i, max));
					}
				}
			} else {
				Log.d(TAG, "sb.length = " + responseInfo.length());
				Log.i(TAG, responseInfo.toString());
			}
		}

	}
}
