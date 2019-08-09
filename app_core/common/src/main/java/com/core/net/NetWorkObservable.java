package com.core.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.net.ConnectivityManager;
import android.util.Log;

import com.common.LogUtil;
import com.core.app.AppConfig;
import com.core.net.utils.NetUtils;


public class NetWorkObservable extends Observable<NetWorkObservable.NetWorkObserver> {
	public static interface NetWorkObserver {
		public void onNetWorkStatusChange(boolean connected);
	}

	private Context mContext;
	private ConnectivityManager mConnectivityManager;
	private boolean mIsNetWorkActive;// 当前网络是否连接上
	private boolean mRegisted;// 是否注册了广播

	public NetWorkObservable(Context context) {
		mContext = context;
		// 获取程序启动时的网络状态
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mIsNetWorkActive = NetUtils.isNetWorkConnected(mContext);
				//isGprsOrWifiConnected();
		if (AppConfig.DEBUG) {
			Log.d(AppConfig.TAG, "mIsNetWorkActive:" + mIsNetWorkActive);
		}
		// 注册网络监听广播
		IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mNetWorkChangeReceiver, intentFilter);
		mRegisted = true;
	}

	private boolean isGprsOrWifiConnected(Context ct) {
		return NetUtils.isNetWorkConnected(ct);
	}

	private BroadcastReceiver mNetWorkChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				return;
			}
			final boolean isConnected = isGprsOrWifiConnected(context);
			LogUtil.d("NetWork","isConnected:"+isConnected);
			if (mIsNetWorkActive != isConnected) {// 和之前的状态不同
				mIsNetWorkActive = isConnected;
				notifyChanged(mIsNetWorkActive);
			}
		}
	};

	public void notifyChanged(boolean connected) {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				mObservers.get(i).onNetWorkStatusChange(connected);
			}
		}
	}

	public void release() {
		if (mRegisted && mContext != null) {
			mContext.unregisterReceiver(mNetWorkChangeReceiver);
			mRegisted = false;
		}
		unregisterAll();
	}

	public boolean isNetworkActive() {
		return mIsNetWorkActive;
	}

}
