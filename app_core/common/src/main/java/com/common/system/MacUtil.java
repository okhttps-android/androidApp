package com.common.system;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.common.data.StringUtil;
import com.common.preferences.SystemSpUtil;

import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by pengminggong on 2016/10/31.
 */

public class MacUtil {

    public static String getMac(Context ct) {
        String mac = null;
        String macOld = SystemSpUtil.api().getString("macaddress");
        if (macisEmpty(macOld)) {
            mac = getLocalMacAddress();
            if (macisEmpty(mac))
                mac = getMacFromWifiInfo(ct);
            if (macisEmpty(mac))
                mac = getLocalEthernetMacAddress();
            if (!macisEmpty(mac)) {
                SystemSpUtil.api().put("macaddress", mac);
            } else {
                tryOpenWifi(ct);
                mac = "";
            }
        } else {
            mac = macOld;
        }
        return mac;
    }

    private static boolean macisEmpty(String mac) {
        return StringUtil.isEmpty(mac) || mac.contains("00:00:00");
    }

    /*通过wifiInfo获取ip*/
    private static String getIpFromWifiInfo(Context ct) {
        WifiInfo info = getWifiInfo(ct);
        if (null != info)
            return int2ip(info.getIpAddress());
        else
            return null;
    }

    /*通过wifiInfo获取mac*/
    private static String getMacFromWifiInfo(Context ct) {
        //在wifi未开启状态下，仍然可以获取MAC地址，但是IP地址必须在已连接状态下否则为0
        WifiInfo info = getWifiInfo(ct);
        if (null != info) {
            return info.getMacAddress();
        } else
            return null;
    }

    /*获取wifiInfo*/
    private static WifiInfo getWifiInfo(Context ct) {
        WifiManager wifiMgr = (WifiManager) ct.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr == null) {
            return null;
        }
        return wifiMgr.getConnectionInfo();
    }

    /*将地址整形转化为字符串*/
    private static String int2ip(long ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    //获取
    private static String getLocalMacAddress() {
        String mac = null;
        try {
            String path = "sys/class/net/eth0/address";
            FileInputStream fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[8192];
            int byteCount_name = fis_name.read(buffer_name);
            if (byteCount_name > 0) {
                mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }


            if (mac == null) {
                fis_name.close();
                return "";
            }
            fis_name.close();
        } catch (Exception io) {
            String path = "sys/class/net/wlan0/address";
            FileInputStream fis_name;
            try {
                fis_name = new FileInputStream(path);
                byte[] buffer_name = new byte[8192];
                int byteCount_name = fis_name.read(buffer_name);
                if (byteCount_name > 0) {
                    mac = new String(buffer_name, 0, byteCount_name, "utf-8");
                }
                fis_name.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (mac == null) {
            return "";
        } else {
            return mac.trim();
        }

    }

    private static boolean tryOpenWifi(Context ct) {
        boolean softOpenWifi = false;
        WifiManager manager = (WifiManager) ct.getSystemService(Context.WIFI_SERVICE);
        int state = manager.getWifiState();
        if (state != WifiManager.WIFI_STATE_ENABLED && state != WifiManager.WIFI_STATE_ENABLING) {
            manager.setWifiEnabled(true);
            softOpenWifi = true;
        }
        return softOpenWifi;
    }

    private static String getLocalEthernetMacAddress() {
        String mac = null;
        try {
            Enumeration localEnumeration = NetworkInterface
                    .getNetworkInterfaces();

            while (localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = (NetworkInterface) localEnumeration
                        .nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();

                if (interfaceName == null) {
                    continue;
                }
                if (interfaceName.equals("eth0")) {
                    mac = convertToMac(localNetworkInterface
                            .getHardwareAddress());
                    if (mac != null && mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
                if (interfaceName.equals("wlan0")) {
                    mac = convertToMac(localNetworkInterface
                            .getHardwareAddress());
                    if (mac != null && mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    private static String convertToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        if (mac == null) {
            return "";
        }
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int value = 0;
            if (b >= 0 && b <= 16) {
                value = b;
                sb.append("0" + Integer.toHexString(value));
            } else if (b > 16) {
                value = b;
                sb.append(Integer.toHexString(value));
            } else {
                value = 256 + b;
                sb.append(Integer.toHexString(value));
            }
            if (i != mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
}
