package com.modular.apputils.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

/**
 * Created by pengminggong on 2016/10/31.
 */

public class WifiReceiverUtil {
    private WifiChangeChangedReceiver receiver = null;

    public void regReceiver(Context ct, OnWifiStatusChangeLinstener linstener) {
        this.linstener = linstener;
        if (receiver == null) receiver = new WifiChangeChangedReceiver();
        IntentFilter intenInfter = new IntentFilter();
        intenInfter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        ct.registerReceiver(receiver, intenInfter);
    }

    public void unRegReceiver(Context ct) {
        if (receiver == null) receiver = new WifiChangeChangedReceiver();
        ct.unregisterReceiver(receiver);
    }

    class WifiChangeChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 这个监听wifi的打开与关闭，与wifi的连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED://禁用
                        if (linstener != null)
                            linstener.callBack(false);
                        break;
                    case WifiManager.WIFI_STATE_ENABLED://启用
                        linstener.callBack(true);
                        break;
                    case WifiManager.WIFI_STATE_DISABLING://禁用
                        linstener.callBack(false);
                        break;
                    case WifiManager.WIFI_STATE_ENABLING://有可能
                        linstener.callBack(true);
                        break;
                }
            }
        }
    }

    private OnWifiStatusChangeLinstener linstener;

    public interface OnWifiStatusChangeLinstener {
        void callBack(boolean isOpen);
    }
}
