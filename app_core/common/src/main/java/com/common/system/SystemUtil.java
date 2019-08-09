package com.common.system;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.StringUtil;
import com.core.app.R;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * android本身api工具类集合
 * Created by Bitliker on 2017/8/10.
 */
public class SystemUtil {

    public static void turn2SetIntent(Context ct) {
        Uri packageURI = Uri.parse("package:" + getCurrentPkgName(ct));
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        ct.startActivity(intent);
    }

    /*判断是否手机插入Sd卡*/
    public static boolean sdCardUseable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /*复制文本到剪切板*/
    public static void copyText(Context context, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label", text);
            clipboardManager.setPrimaryClip(clipData);
        } else {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(text);
        }
    }

    /*获取Sd卡的总容量*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSdCardTotalSize() {
        if (!sdCardUseable()) {
            return 0;
        }
        // 取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // 获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSizeLong();
        // 获取所有数据块数
        long allBlocks = sf.getBlockCountLong();
        // 返回SD卡大小
        return (allBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

    /*获取Sd卡的可用容量*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSdCardFreeSize() {
        if (!sdCardUseable()) {
            return 0;
        }
        // 取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // 获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSizeLong();
        // 空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocksLong();
        // 返回SD卡空闲大小
        // return freeBlocks * blockSize; //单位Byte
        // return (freeBlocks * blockSize)/1024; //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

    /*获取 开机时间*/
    public static String getBootTimeString() {
        long ut = SystemClock.elapsedRealtime() / 1000;
        int h = (int) ((ut / 3600));
        int m = (int) ((ut / 60) % 60);
        LogUtil.i("ApiUtil", h + ":" + m);
        return h + ":" + m;
    }

    /*获取 系统信息*/
    public static String getSystemInfo() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(date);
        StringBuilder sb = new StringBuilder();
        sb.append("_______  系统信息  ").append(time).append(" ______________");
        sb.append("\nID                 :").append(Build.ID);
        sb.append("\nBRAND              :").append(Build.BRAND);
        sb.append("\nMODEL              :").append(Build.MODEL);
        sb.append("\nRELEASE            :").append(Build.VERSION.RELEASE);
        sb.append("\nSDK                :").append(Build.VERSION.SDK);

        sb.append("\n_______ OTHER _______");
        sb.append("\nBOARD              :").append(Build.BOARD);
        sb.append("\nPRODUCT            :").append(Build.PRODUCT);
        sb.append("\nDEVICE             :").append(Build.DEVICE);
        sb.append("\nFINGERPRINT        :").append(Build.FINGERPRINT);
        sb.append("\nHOST               :").append(Build.HOST);
        sb.append("\nTAGS               :").append(Build.TAGS);
        sb.append("\nTYPE               :").append(Build.TYPE);
        sb.append("\nTIME               :").append(Build.TIME);
        sb.append("\nINCREMENTAL        :").append(Build.VERSION.INCREMENTAL);

        sb.append("\n_______ CUPCAKE-3 _______");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            sb.append("\nDISPLAY            :").append(Build.DISPLAY);
        }

        sb.append("\n_______ DONUT-4 _______");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            sb.append("\nSDK_INT            :").append(Build.VERSION.SDK_INT);
            sb.append("\nMANUFACTURER       :").append(Build.MANUFACTURER);
            sb.append("\nBOOTLOADER         :").append(Build.BOOTLOADER);
            sb.append("\nCPU_ABI            :").append(Build.CPU_ABI);
            sb.append("\nCPU_ABI2           :").append(Build.CPU_ABI2);
            sb.append("\nHARDWARE           :").append(Build.HARDWARE);
            sb.append("\nUNKNOWN            :").append(Build.UNKNOWN);
            sb.append("\nCODENAME           :").append(Build.VERSION.CODENAME);
        }

        sb.append("\n_______ GINGERBREAD-9 _______");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            sb.append("\nSERIAL             :").append(Build.SERIAL);
        }
        LogUtil.d(sb.toString());
        return sb.toString();
    }

    public static String getMac(Context ct) {
        return MacUtil.getMac(ct);
    }

    /* 获取手机唯一序列号 */
    public static String getDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = tm.getDeviceId();// 手机设备ID，这个ID会被用为用户访问统计
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString().replaceAll("-", "");
            }
            return deviceId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* 获取操作系统版本号 */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /* 获取操作系统版本号 */
    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /* 获取手机型号 */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /* 获取app的版本信息 */
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;// 系统版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* 获取app的版本名 */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;// 系统版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDeviceInfo() {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) BaseConfig.getContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = null;
            if (PermissionUtil.lacksPermissions(BaseConfig.getContext(), Manifest.permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }
            String mac = null;
            FileReader fstream = null;
            try {
                fstream = new FileReader("/sys/class/net/wlan0/address");
            } catch (FileNotFoundException e) {
                fstream = new FileReader("/sys/class/net/eth0/address");
            }
            BufferedReader in = null;
            if (fstream != null) {
                try {
                    in = new BufferedReader(fstream, 1024);
                    mac = in.readLine();
                } catch (IOException e) {
                } finally {
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(BaseConfig.getContext().getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 　　附：Wifi状态有以下几种：(括号内为所对应的的int值)
     * 　　1. WifiManager.WIFI_STATE_DISABLED (1)   //禁用WiFi状态
     * 　　2. WifiManager.WIFI_STATE_ENABLED (3)  //WiFi启用状态
     * 　　3. WifiManager.WIFI_STATE_DISABLING (0)  //禁用WiFi状态
     * 　　4  WifiManager.WIFI_STATE_ENABLING  (2)    //可以启动wifi
     *
     * @param context
     * @return
     */
    public static int getWifiStatus(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            return wifiManager.getWifiState();
        }
        return 0;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static void phoneAction(final BaseActivity mContext, final String phone) {
        if (mContext == null) {
            return;
        }
        if (!StringUtil.isMobileNumber(phone)) {
            ToastUtil.showToast(mContext, R.string.phone_number_format_error);
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(mContext).title(R.string.dialog_confim_phone).content(mContext.getString(R.string.dialog_phone) + phone)
                .positiveText(R.string.dialog_phone_action).negativeText(R.string.common_cancel).autoDismiss(false).callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mContext.requestPermission(Manifest.permission.CALL_PHONE, new Runnable() {
                            @Override
                            public void run() {
                                // 用intent启动拨打电话
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    //预防万一
                                    return;
                                }
                                mContext.startActivity(intent);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(mContext, R.string.not_system_permission);
                            }
                        });
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).build();

        dialog.show();


    }

    @Deprecated
    public static void phoneAction(final Context mContext, final String phone) {
        if (mContext == null) return;
        if (mContext instanceof BaseActivity) {
            phoneAction((BaseActivity) mContext, phone);
        } else {
            if (!PermissionUtil.lacksPermissions(mContext, Manifest.permission.CALL_PHONE)) {
                final PopupWindow window = new PopupWindow(mContext);
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_select_alert_pop, null);
                window.setContentView(view);
                window.setBackgroundDrawable(new BitmapDrawable());
                DisplayUtil.backgroundAlpha(mContext, 0.5f);
                window.setTouchable(true);
                setPopupWindowHW((Activity) mContext, window);
                window.setOutsideTouchable(false);
                window.setFocusable(true);
                TextView title_tv = (TextView) view.findViewById(R.id.title_tv);
                TextView message_tv = (TextView) view.findViewById(R.id.message_tv);
                TextView sure_tv = (TextView) view.findViewById(R.id.sure_tv);
                title_tv.setText(mContext.getString(R.string.dialog_confim_phone));
                message_tv.setText(mContext.getString(R.string.dialog_phone) + phone);
                sure_tv.setText(R.string.dialog_phone_action);

                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        DisplayUtil.backgroundAlpha(mContext, 1f);
                    }
                });
                view.findViewById(R.id.goto_tv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
                sure_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 用intent启动拨打电话
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mContext.startActivity(intent);
                        window.dismiss();
                    }
                });
                window.showAtLocation(view, Gravity.CENTER, 0, 0);
            } else if (mContext instanceof Activity) {
                PermissionUtil.requestPermission((Activity) mContext, PermissionUtil.DEFAULT_REQUEST, Manifest.permission.CALL_PHONE);
            }
        }
    }
    
    

    public static void setPopupWindowHW(Activity ct, PopupWindow window) {
        window.getContentView().measure(0, 0);
        window.setHeight(window.getContentView().getMeasuredHeight() + 30);
        window.setWidth(getWidth(ct));
    }

    private static int getWidth(Activity ct) {
        DisplayMetrics dm = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) (dm.widthPixels * (0.8));
    }

    /**
     * android 5.0之后如何获取当前运行的应用包名？
     *
     * @param context
     * @return
     */
    public static String getCurrentPkgName(Context context) {
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        int START_TASK_TO_FRONT = 2;
        String pkgName = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List appList = am.getRunningAppProcesses();
        List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : processes) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (state != null && state == START_TASK_TO_FRONT) {
                    currentInfo = app;
                    break;
                }
            }
        }
        if (currentInfo != null) {
            pkgName = currentInfo.processName;
        }
        return pkgName;
    }
}
