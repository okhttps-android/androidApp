package com.xzjmyk.pm.activity.util.oa;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.DBManager;
import com.core.dao.SignAutoLogDao;
import com.core.model.B2BMsg;
import com.core.model.Friend;
import com.core.model.XmppMessage;
import com.core.utils.TimeUtils;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;
import com.core.xmpp.model.SignAutoLogEntity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.uas.applocation.UasLocationHelper;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.xzjmyk.pm.activity.ui.erp.activity.WebViewCommActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.WebViewLoadActivity;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("NewApi")
public class CommonUtil {
    private final static String AppShareName = "setting";
    public static int counter = 0;

    /**
     * @desc:获取erp的根路径
     * @author：Administrator on 2016/2/18 15:12
     */
    public static String getAppBaseUrl(Context ct) {
        return com.core.utils.CommonUtil.getAppBaseUrl(ct);
    }

    // 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
            // Log.e(TAG, "error " + e.getMessage());
        }
        return apiKey;
    }

    /**
     * 判断str1中包含str2的个数
     *
     * @param str1
     * @param str2
     * @return counter
     */
    public static int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else if (str1.indexOf(str2) != -1) {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) +
                    str2.length()), str2);
            return counter;
        }
        return 0;
    }

    /**
     * @desc:公共对话框，选择日期
     * @author：Arison on 2016/11/9
     */
    public static void showDateDialog(Context ct, final View tv) {
        DateTimePicker picker = new DateTimePicker((Activity) ct, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                if (tv instanceof TextView) {
                    ((TextView) tv).setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                } else if (tv instanceof EditText) {
                    ((EditText) tv).setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                }


            }
        });
        picker.show();
    }

    /**
     * @desc:JSONArraya排序
     * @author：Arison on 2016/11/8
     */
    public static JSONArray sortJsonArray(JSONArray jsonArr) {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.size(); i++) {
            if ("待审批".equals(jsonArr.getJSONObject(i).getString("JP_STATUS"))) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "JP_LAUNCHTIME";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                Long valA = null;
                Long valB = null;
                try {
                    valA = (Long) a.get(KEY_NAME);
                    valB = (Long) b.get(KEY_NAME);
                } catch (JSONException e) {
                    //do something
                }
                int result = valA.compareTo(valB);
                return -result;
            }
        });
        jsonArr.clear();
        for (int i = 0; i < jsonValues.size(); i++) {
            jsonArr.add(i, jsonValues.get(i));

        }
        return jsonArr;
    }


    public final static UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA platform) {
            com.umeng.socialize.utils.Log.d("plat", "platform" + platform);
            if (platform.name().equals("WEIXIN_FAVORITE")) {
                Toast.makeText(MyApplication.getInstance(), platform + " 收藏成功", Toast.LENGTH_SHORT).show();
            } else {
                if (platform.name().equals("WEIXIN")) {
                    Toast.makeText(MyApplication.getInstance(), "微信" + " 分享成功", Toast.LENGTH_SHORT).show();
                } else if (platform.name().equals("QZONE")) {
                    Toast.makeText(MyApplication.getInstance(), "QQ空间" + " 分享成功", Toast.LENGTH_SHORT).show();
                } else if (platform.name().equals("SINA")) {
                    Toast.makeText(MyApplication.getInstance(), "微博" + " 分享成功", Toast.LENGTH_SHORT).show();
                } else if (platform.name().equals("QQ")) {
                    Toast.makeText(MyApplication.getInstance(), "QQ" + " 分享成功", Toast.LENGTH_SHORT).show();
                } else if (platform.name().equals("WEIXIN_CIRCLE")) {
                    Toast.makeText(MyApplication.getInstance(), "朋友圈" + " 分享成功", Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(MyApplication.getInstance(), platform + " 分享失败", Toast.LENGTH_SHORT).show();
            if (t != null) {
                com.umeng.socialize.utils.Log.d("throw", "throw:" + t.getMessage());
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            if (platform.name().equals("WEIXIN")) {
                Toast.makeText(MyApplication.getInstance(), "微信" + "分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform.name().equals("QZONE")) {
                Toast.makeText(MyApplication.getInstance(), "QQ空间" + "分享取消了", Toast.LENGTH_SHORT).show();

            } else if (platform.name().equals("SINA")) {
                Toast.makeText(MyApplication.getInstance(), "微博" + "分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform.name().equals("QQ")) {
                Toast.makeText(MyApplication.getInstance(), "QQ" + "分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform.name().equals("WEIXIN_CIRCLE")) {
                Toast.makeText(MyApplication.getInstance(), "朋友圈" + "分享取消了", Toast.LENGTH_SHORT).show();
            }

        }
    };

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

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 300, 300);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }


    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static BitmapFactory.Options getBitmapOptions() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 300, 300);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return options;
    }


    /**
     * 显示图片的所有配置
     *
     * @return
     */
    private DisplayImageOptions getWholeOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.loading) //设置图片在下载期间显示的图片
//                .showImageForEmptyUri(R.drawable.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.drawable.error)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
                //.decodingOptions(BitmapFactory.Options decodingOptions)//设置图片的解码配置
                .delayBeforeLoading(0)//int delayInMillis为你设置的下载前的延迟时间
                //设置图片加入缓存前，对bitmap进行设置
                //.preProcessor(BitmapProcessor preProcessor)
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .build();//构建完成
        return options;
    }


    public static String getRandomAccount() {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) // 字符串
            {
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写字母还是小写字母
                val += (char) (choice + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(charOrNum)) // 数字
            {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val.toLowerCase();
    }

    /**
     * @author LiuJie
     * @功能:获取账户的邮箱地址
     */
    public static void getUserEmail(Context context) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String accountName = account.name;
                String accountType = account.type;
                System.out.println("name:" + accountName + "\n" + "type:" + accountType);
            }
        }
    }

    /**
     * @author LiuJie
     * @功能:获取应用程序内存使用情况
     */
    public static String getMemory() {
        // 应用程序最大可用内存
        int maxMemory = ((int) Runtime.getRuntime().maxMemory()) / 1024 / 1024;
        // 应用程序已获得内存
        long totalMemory = ((int) Runtime.getRuntime().totalMemory()) / 1024 / 1024;
        // 应用程序已获得内存中未使用内存
        long freeMemory = ((int) Runtime.getRuntime().freeMemory()) / 1024 / 1024;
        System.out.println(
                "maxMemory=" + maxMemory + "M,\ntotalMemory=" + totalMemory + "M,\nfreeMemory=" + freeMemory + "M");
        return "maxMemory=" + maxMemory + "M,\ntotalMemory=" + totalMemory + "M,\nfreeMemory=" + freeMemory + "M";
    }

    /**
     * @author LiuJie
     * @功能:获取设备屏幕尺寸和密度
     */
//    public static String getDeviceInfo(Context ct) {
//        DisplayMetrics metric = new DisplayMetrics();
//        metric = ct.getApplicationContext().getResources().getDisplayMetrics();
//        int width = metric.widthPixels; // 屏幕宽度（像素）
//        int height = metric.heightPixels; // 屏幕高度（像素）
//        float density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
//        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
//        return "width=" + width + "\nheight=" + height + "\ndensity=" + density + "\ndensityDpi=" + densityDpi;
//    }

    // 获取手机型号
    public static String getDeviceModel() {
        String model = Build.MODEL;
        return model;
    }

    // 获取手机生产厂商
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }


    /**
     * 获得当前包的版本号 return
     **/
    public static String ApkVersionCode(Context context) {
        if (context != null) {
            PackageManager pack = context.getPackageManager();
            PackageInfo packageInfo = null;
            String versionCode = null;
            try {
                packageInfo = pack.getPackageInfo(context.getPackageName(), 0);
                versionCode = packageInfo.versionName;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            return versionCode;
        } else {
            return null;
        }

    }

    /**
     * 检验是否是合法的IP地址
     *
     * @param address String IP地址
     * @return boolean IP地址是否合法
     */
    public static boolean isIpAddress(String address) {
        String regex = "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(address);
        return m.matches();
    }

    /**
     * 检验是否是正确的网址
     *
     * @param s
     * @return
     */
    public static boolean isWebsite(String s) {
        String regex = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\\\/])+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        return m.matches();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static boolean setSharedPreferences(Context ct, String key, int value) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
            boolean falg = sPreferences.edit().putInt(key, value).commit();
            return falg;
        } else {
            return false;
        }
    }

    public static boolean setSharedPreferences(Context ct, String key, String value) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
            boolean falg = sPreferences.edit().putString(key, value).commit();
            return falg;
        } else {
            return false;
        }
    }

    public static boolean setSharedPreferences(Context ct, String key, String value, String preName) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(preName, Context.MODE_PRIVATE);
            boolean falg = sPreferences.edit().putString(key, value).commit();
            return falg;
        } else {
            return false;
        }
    }

    public static boolean setSharedPreferences(Context ct, String key, boolean value) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
            boolean falg = sPreferences.edit().putBoolean(key, value).commit();
            return falg;
        } else {
            return false;
        }
    }

    public static boolean clearSharedPreferences(Context ct, String key) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
            boolean falg = sPreferences.edit().remove(key).commit();
            return falg;
        } else {
            return false;
        }

    }

    public static String getSharedPreferences(Context ct, String key) {
        if (key == null || ct == null) {
            return null;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        String value = sPreferences.getString(key, null);
        return value;
    }

    public static String getSharedPreferences(Context ct, String key, String preName) {
        if (key == null) {
            return null;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(preName, Context.MODE_PRIVATE);
        String value = sPreferences.getString(key, null);
        return value;
    }

    public static boolean getSharedPreferencesBoolean(Context ct, String key) {
        if (key == null) {
            return false;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        boolean value = sPreferences.getBoolean(key, false);
        return value;
    }

    public static void putSharedPreferencesBoolean(Context ct, String key, boolean value) {
        if (key == null) {
            return;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        sPreferences.edit().putBoolean(key, value).commit();
    }

    public static long getSharedPreferencesLong(Context ct, String key) {
        if (key == null) {
            return 0;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        long value = sPreferences.getLong(key, 0);
        return value;
    }

    public static int getSharedPreferencesInt(Context ct, String key, int defValues) {
        if (key == null) {
            return 0;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        int value = sPreferences.getInt(key, defValues);
        return value;
    }

    public static boolean putSharedPreferencesLong(Context ct, String key, long values) {
        if (key == null) {
            return false;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        sPreferences.edit().putLong(key, values).commit();
        return true;
    }

    public static boolean putSharedPreferencesInt(Context ct, String key, int values) {
        if (key == null) {
            return false;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        sPreferences.edit().putInt(key, values).commit();
        return true;
    }

    public static boolean getSharedPreferencesBoolean(Context ct, String key, boolean defalutValue) {
        if (key == null) {
            return false;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        boolean value = sPreferences.getBoolean(key, defalutValue);
        return value;
    }

    public static void setSharedPreferences(Context ct, String key, float value) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
            sPreferences.edit().putFloat(key, value);
            sPreferences.edit().putFloat(key, value).apply();
        }
    }

    public static double getSharedPreferencesfloat(Context ct, String key, float defValues) {
        if (key == null) {
            return 0;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        double value = sPreferences.getFloat(key, defValues);
        return value;
    }

    public static String getStringDate(Long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String getStringDateMM(Long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(date);
        return dateString;
    }

    public static String getStringDate(Long date, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * @author Administrator
     * @功能:判断sd卡的存在
     */
    public static boolean isExistSDCard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static boolean isBiteman() {
        String baseUrl = getSharedPreferences(MyApplication.getInstance(), "erp_base");
        return !StringUtil.isEmpty(baseUrl) && baseUrl.contains("http://202.104.151.184:8099/ERP/");
    }

    /**
     * 检测网络是否可用
     *
     * @param context
     * @return
     */
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

    /**
     * 检测Sdcard是否存在
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getStrng(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    @SuppressWarnings("deprecation")
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity.getClassName();
        else
            return "";
    }

    public static Bitmap convertBitmap(Bitmap oldBitmap, int reqWidth, int reqHeight) {
        // 获取图片的宽高
        int width = oldBitmap.getWidth();
        int height = oldBitmap.getHeight();

        float scaleWidth = 0;
        float scaleHeight = 0;

        if (width < height) {
            if (height < reqHeight) {
                int newHeight = reqHeight;
                float newWidth = width * (((float) reqHeight) / height);
                // 计算缩放比例
                scaleWidth = newWidth / width + 1;
                scaleHeight = ((float) newHeight) / height + 1;
            } else {
                // 设置想要的大小
                int newWidth = reqWidth;
                float newHeight = height * (((float) reqWidth) / width);
                // 计算缩放比例
                scaleWidth = ((float) newWidth) / width;
                scaleHeight = newHeight / height;
            }
        } else {
            if (width < reqWidth) {
                // 设置想要的大小
                int newWidth = reqWidth;
                float newHeight = height * (((float) reqWidth) / width);
                // 计算缩放比例
                scaleWidth = ((float) newWidth) / width + 1;
                scaleHeight = newHeight / height + 1;
            } else {
                // 设置想要的大小
                int newHeight = reqHeight;
                float newWidth = width * (((float) reqHeight) / height);
                // 计算缩放比例
                scaleWidth = newWidth / width;
                scaleHeight = ((float) newHeight) / height;
            }
        }
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(oldBitmap, 0, 0, width, height, matrix, true);
        return newbm;
    }

    // 转换dip为px
    public static int convertDip2Px(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    // 转换px为dip
    public static int convertPx2Dip(Context context, int px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
    }

    public String UTF8ToISO(String value) {
        String vString = null;
        try {
            vString = new String(value.getBytes("UTF-8"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return vString;
    }

    public String ISOToUTF8(String value) {
        String vString = null;
        try {
            vString = new String(value.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return vString;
    }

    /**
     * 函数说明：判断wify是否连接上
     *
     * @param context
     * @return
     */
    public boolean isWiFiActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo ni : infos) {
                    if (ni.getTypeName().equals("WIFI") && ni.isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 把毫秒转化成日期
     *
     * @param dateFormat(日期格式，例如：MM/ dd/yyyy HH:mm:ss)
     * @param millSec(毫秒数)
     * @return
     */
    public static String transferLongToDate(String dateFormat, Long millSec) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(millSec);
        return sdf.format(date);
    }


    public static void pushProcessB2bMsg(Context ct, String content, String master) {
        DBManager db = new DBManager(ct);
        B2BMsg model = new B2BMsg();
        model.setContent(content);
        model.setHasRead(0);
        model.setTime(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        model.setMaster(master);
        db.saveB2bMsg(model);
        db.closeDB();
        Log.i("Arison", "" + "数据保存成功！");
    }

    public static void pushProcessMsg(Context ct, String ownerId) {
        // 添加一条系统提示
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_TIP);
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));// 随机产生一个PacketId
        chatMessage.setFromUserId(Friend.ID_ERP_PROCESS);
        chatMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
        // 为了使得初始生成的系统消息排在新朋友前面，所以在时间节点上延迟一点 1s
        chatMessage.setTimeSend(CalendarUtil.getSecondMillion());
        chatMessage.setContent("您有一条新的待处理流程...");
        chatMessage.setMySend(false);// 表示不是自己发的
        // 往消息表里插入一条记录
        ChatMessageDao.getInstance().saveNewSingleChatMessage(ownerId, Friend.ID_ERP_PROCESS, chatMessage);
        // 往朋友表里面插入一条未读记录
        FriendDao.getInstance().markUserMessageUnRead(ownerId, Friend.ID_ERP_PROCESS);
        // 更新消息记录
        FriendDao.getInstance().updateLastChatMessage(ownerId, Friend.ID_ERP_PROCESS, chatMessage);
        MsgBroadcast.broadcastMsgNumReset(ct);
        MsgBroadcast.broadcastMsgUiUpdate(ct);
    }

    /**
     * @注释：加载通用网页
     */
    public static void loadWebView(Context context, String url, String title, String master, String masterId, String uu) {
        Intent intent = new Intent(context, WebViewLoadActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("p", title);
        intent.putExtra("master", master);
        intent.putExtra("masterId", masterId);
        intent.putExtra("uu", uu);
        if (!StringUtil.isEmpty(masterId) && !StringUtil.isEmpty(uu)) {
            intent.putExtra("isStartApp", "true");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.getApplicationContext().startActivity(intent);
    }

    /**
     * @desc:跳转到B2B
     * @author：Administrator on 2016/4/8 14:55
     */
    public static void loadWebViewToB2B(Context ct, String url, String title) {
        Intent intent = new Intent(ct, WebViewCommActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("p", title);
        intent.putExtra("isStartApp", "true");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ct.getApplicationContext().startActivity(intent);
    }

    /**
     * @desc:修改textView样式
     * @author：Arison on 2016/8/3
     */
    public static void textSpanForStyle(
            TextView view,
            String input,
            String match,
            int color) {
        SpannableStringBuilder style = new SpannableStringBuilder(input);
        Pattern highlight = Pattern.compile(match);
        Matcher m = highlight.matcher(style.toString());
        while (m.find()) {
//            style.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            style.setSpan(new ForegroundColorSpan(color), m.start(), m.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new StrikethroughSpan(), m.start(), m.end(), 
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new UnderlineSpan(), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        view.setText(style);
    }


    /**
     * @desc:修改textView样式 批量修改
     * @author：Arison on 2016/8/3
     */
    public static void textAarrySpanForStyle(
            TextView view,
            String input,
            String[] match,
            int color) {
        SpannableStringBuilder style = new SpannableStringBuilder(input);

        for (String item : match) {
            if (item == null) item = "0";
            Pattern highlight = Pattern.compile(item);
            Matcher m = highlight.matcher(style.toString());
            while (m.find()) {
//                style.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), m.start(), m.end(),
//                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                style.setSpan(new ForegroundColorSpan(color), m.start(), m.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new StrikethroughSpan(), m.start(), m.end(), 
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                style.setSpan(new UnderlineSpan(), m.start(), m.end(),
//                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        view.setText(style);
    }

    private static long mLastClickTime;
    private static final long SPACE_TIME = 1000;//重复点击间隔时间

    /**
     * 是否控件重复点击
     *
     * @return
     */
    public static boolean isRepeatClick() {
        long currentTime = System.currentTimeMillis();
        long intervals = currentTime - mLastClickTime;
        if (intervals > 0 && intervals < SPACE_TIME) {
            return true;
        }
        mLastClickTime = currentTime;
        return false;

    }

    /**
     * 对话框是否展示
     *
     * @param dialog
     * @return
     */
    public static boolean isDialogShowing(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            return true;
        }
        return false;
    }

    public static void getCommonId(Context ct, String seq, Handler mHandler, int codeWhat) {
        getCommonId(CommonUtil.getAppBaseUrl(ct), ct, seq, mHandler, codeWhat);
    }

    public static void getCommonId(String action, Context ct, String seq, Handler mHandler, int codeWhat) {
        String url = action + "/common/getId.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("seq", seq);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        com.core.net.http.ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, codeWhat, null, null, "post");
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context
     * @param className 某个界面名称
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 去除字符串中的换行符，空格
     *
     * @param s
     * @return
     */
    public static String removeStringMark(String s) {
        String result = "";
        if (!TextUtils.isEmpty(s)) {
            Pattern pattern = Pattern.compile("\\s*|\t|\r|\n");
//            Pattern pattern = Pattern.compile("\r|\n");
            Matcher matcher = pattern.matcher(s);
            result = matcher.replaceAll("");
        }
        return result;
    }

    /**
     * 返回输入框内去除换行符和空格之后的字符串
     *
     * @param et
     * @return
     */
    public static String getNoMarkEditText(FormEditText et) {
        String temp = "";
        temp = et.getText().toString().trim();
        temp = removeStringMark(temp);
        return StringUtil.toHttpString(temp);
    }

    /**
     * 打开一个文件
     *
     * @param file
     * @return
     */
    public static Intent getFileIntent(File file) {
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    /**
     * 判断文件类型
     *
     * @param f
     * @return
     */
    public static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

        /* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
//	              /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        LogUtil.d("下载文件类型：" + type);
        return type;
    }

    /**
     * @desc:打印异常信息
     * @author： on 2016/10/14
     */
    public static String getExceptionStack(Context ct, Throwable ex, boolean isDisplay) {
        StringBuffer sb = new StringBuffer();
        sb.append("----------------------异常信息输出-------------------------------------\n");
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        String phone = CommonUtil.getSharedPreferences(ct, "user_phone");
        String password = CommonUtil.getSharedPreferences(ct, "user_password");
        String master_ch = CommonUtil.getSharedPreferences(ct, "Master_ch");
        String company = CommonUtil.getSharedPreferences(ct, "erp_commpany");
        sb.append(result);
        if (isDisplay) {
            sb.append("\n----------------------用户信息输出-------------------------------------");
            sb.append("\n phone:" + phone);
            sb.append("\n password:" + password);
            sb.append("\n master_ch:" + master_ch);
            sb.append("\n company:" + company);
            sb.append("\n----------------------设备信息输出-------------------------------------");
            //获取设备大小
            //  String deviceInfo = CommonUtil.getDeviceInfo(ct);
            //   System.out.println("deviceInfo=" + deviceInfo);
            // sb.append("\n" + deviceInfo);
            sb.append("\n手机型号：" + CommonUtil.getDeviceModel());
            sb.append("\n手机生产厂商：" + CommonUtil.getDeviceManufacturer());
            //获取应用程序内存使用情况
            sb.append("\n----------------------内存信息输出-------------------------------------\n");
            sb.append(CommonUtil.getMemory());
            //获取应用程序的当前版本号
            sb.append("\n----------------------版本信息输出-------------------------------------\n");
            sb.append("\n应用版本号：" + CommonUtil.ApkVersionCode(ct));
        }
        return sb.toString();
    }

    public final static int GET_NET_TIME = 101;

    /**
     * 获取网络时间
     *
     * @param mHandler
     */
    public static void getNetTime(final Handler mHandler) {
        new Thread() {

            private Message message;
            private Date date;

            @Override
            public void run() {
                String nowTime = null;
                URLConnection uc = null;
                try {
                    URL url = new URL("http://www.baidu.com");
                    uc = url.openConnection();
                    uc.setReadTimeout(3000);
                    uc.setConnectTimeout(3000);
                    uc.connect();

                } catch (Exception e) {
                    message = Message.obtain();
                    message.what = GET_NET_TIME;
                    message.obj = null;
                    mHandler.sendMessage(message);
                }
                long id = uc.getDate();
                if (id != 0) {
                    date = new Date(id);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    nowTime = format.format(date);

                    Log.i("时间", date.getHours() + "时" + date.getMinutes() + "分"
                            + date.getSeconds() + "秒" + "\n" + nowTime);
                    message = Message.obtain();
                    message.what = GET_NET_TIME;
                    message.obj = nowTime;
                } else {
                    message = Message.obtain();
                    message.what = GET_NET_TIME;
                    message.obj = null;
                }

                mHandler.sendMessage(message);
            }
        }.start();
    }


    /**
     * EditText竖直方向是否可以滚动
     *
     * @param editText 需要判断的EditText
     * @return true：可以滚动  false：不可以滚动
     */
    public static boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

    private static DateTimePickerDialog datePickDialog = null;

    /**
     * 弹出选择时间框，并显示在指定TextView上
     *
     * @param context
     * @param textView
     */
    public static void showDataPickDialog(Context context, final TextView textView) {
        if (datePickDialog == null) {
            datePickDialog = new DateTimePickerDialog(context, System.currentTimeMillis());
        }

        datePickDialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dia, long date) {
                textView.setText(CommonUtil.getStringDateMM(date));
                /** @注释：保证 初始化当前时间 */
                datePickDialog = null;
            }
        });

        if (!datePickDialog.isShowing()) {
            datePickDialog.show();
        }
    }

    /**
     * 弹出选择时间框，并显示在指定TextView上
     *
     * @param context
     * @param textView
     */
    public static void showDataPickDialog(Context context, final TextView textView, final String dateFormat) {
        if (datePickDialog == null) {
            datePickDialog = new DateTimePickerDialog(context, System.currentTimeMillis());
        }

        datePickDialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dia, long date) {
                textView.setText(CommonUtil.getStringDate(date, dateFormat));
                /** @注释：保证 初始化当前时间 */
                datePickDialog = null;
            }
        });

        if (!datePickDialog.isShowing()) {
            datePickDialog.show();
        }
    }

    /**
     * 去除集合中相同元素
     *
     * @param datas
     * @return
     */
    public static List<Object> getSingleElement(List<Object> datas) {
        List<Object> resultList = new ArrayList<>();
        Iterator<Object> iterator = datas.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (!resultList.contains(next)) {
                resultList.add(next);
            }
        }
        return resultList;
    }


    /**
     * @desc:打电话确认框
     * @author：Arison on 2016/11/3
     */
    public static void phoneAction(Context mContext, String phone) {
        SystemUtil.phoneAction(mContext, phone);
    }


    /**
     * 将阿拉伯数字转换为汉字一、二、三..方法
     *
     * @return
     * @author：FANGlh 2016-11-28
     */
    public static String numToCN(int d) {
//        String[] str = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
        String[] str = {" ", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
//        String ss[] = new String[] { "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿" };
        String ss[] = new String[]{" ", "一", "十", "百", "千", "万", "十", "百", "千", "亿"};
        String s = String.valueOf(d);
        System.out.println(s);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            String index = String.valueOf(s.charAt(i));
            sb = sb.append(str[Integer.parseInt(index)]);
        }
        String sss = String.valueOf(sb);
        int i = 0;
        for (int j = sss.length(); j > 0; j--) {
            sb = sb.insert(j, ss[i++]);
        }
        return sb.toString();
    }


    /**
     * @param ：将Bitmap保存图片到指定的路径:uu/myqzonepic下
     * @author: FANGlh 2016-12-6
     */
    public static void saveImageToLocal(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "uu/myqzonepic");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + "uu/myqzonepic")));
        com.core.net.http.ViewUtil.ToastMessage(context, "图片已保存至：sdcard/uu/myqzonepic");
    }


    /**
     * @param ：取String[]中首个字符串中的数字 number
     * @author: FANGlh 2016-12-6
     */
    public static int getNumByString(String chche) {
        if (StringUtil.isEmpty(chche)) return -1;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(chche);
        if (m.find()) {
            return Integer.parseInt(m.group(0));
        }
        return -1;
    }

    public static long getlongNumByString(String chche) {
        if (StringUtil.isEmpty(chche)) return -1;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(chche);
        if (m.find()) {
            return Long.parseLong(m.group(0));
        }
        return -1;
    }

    /**
     * @param ：去除一段字符串中的标点只保留文字
     * @author: FANGlh 2017-2-9
     */

    public static String getPlaintext(String stringInfo) {
        Pattern p = Pattern.compile("[.,，？！。\"\\?!:']");//增加对应的标点

        Matcher m = p.matcher(stringInfo);

        String first = m.replaceAll(""); //把英文标点符号替换成空，即去掉英文标点符号

        p = Pattern.compile(" {2,}");//去除多余空格

        m = p.matcher(first);

        String second = m.replaceAll(" ");

        String nulltext = "";
        if (!TextUtils.isEmpty(second)) {
            return second;
        } else {
            return nulltext;
        }
    }

    /**
     * @return 返回double保留两位小数
     * @author: FANGlh 2017-3-30
     */

    public static double getTwoPointDouble(double num) {
        try {
            DecimalFormat df = new DecimalFormat(".##");
            double dis = Math.abs(num);
            return Double.valueOf(df.format(dis));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getTwoPointStr(double m) {
        String strValue = "";
        try {
            DecimalFormat df = new DecimalFormat("#,###.##");
            strValue = df.format(m);
            return strValue;
        } catch (Exception e) {
            return strValue;
        }
    }

    /*获取账套，erp的账套  b2b的公司uu号*/
    public static String getMaster() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String master = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")
                : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
        return master;

    }

    public static String getEmcode() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String emcode = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu") : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        if (emcode == null) emcode = "";
        return emcode;
    }

    public static String getName() {
        String name = getSharedPreferences(MyApplication.getInstance(), "erp_emname");
        if (StringUtil.isEmpty(name))
            name = MyApplication.getInstance().mLoginUser.getNickName();
        return StringUtil.isEmpty(name) ? "" : name;
    }

    /**
     * Created by FANGlh on 2017/5/2.
     * 针对月份，日期 返回xx格式 eg：1-1，需要两次取1 返回01
     *
     * @return
     */
    public static String getZeroNumber(int nummber) {
        String s = "";
        if (nummber < 10) {
            s = "0" + nummber;
        } else {
            s = nummber + "";
        }
        return s;
    }

    /**
     * Created by FANGlh on 2017/5/5.
     * function:判断是不是发布版本，true：发布版本
     */
    public static Boolean isReleaseVersion() {
//        return !BuildConfig.DEBUG;
        return !BaseConfig.isDebug();
    }

    /**
     * Created by FANGlh on 2017/5/9.
     * function:日志保存本地通用方法
     * aa_type; //操作类型 打卡签到or外勤签到
     * aa_location; // 当前位置
     * aa_remark; //  失败原因
     * aa_date; // 时间
     * aa_telephone; // 手机
     */

    public static void saveAutoLogtoLocal(String aa_type, String aa_remark) {
        String aa_date = TimeUtils.f_long_2_str(System.currentTimeMillis());
        String aa_location = UasLocationHelper.getInstance().getUASLocation().getAddress();
        String aa_telephone = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "user_phone");
        Log.i("aa_date,aa_tel", aa_date + "," + aa_telephone);

        SignAutoLogEntity signAutoLogEntity = new SignAutoLogEntity();
        signAutoLogEntity.setAa_type(aa_type);
        signAutoLogEntity.setAa_location(aa_location);
        signAutoLogEntity.setAa_remark(aa_remark);
        signAutoLogEntity.setAa_date(aa_date);
        signAutoLogEntity.setAa_telephone(aa_telephone);
        SignAutoLogDao.addNewData(signAutoLogEntity);
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 将double格式化为指定小数位的String，不足小数位用0补全
     *
     * @param v     需要格式化的数字
     * @param scale 小数点后保留几位
     * @return
     */
    public static String roundByScale(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The   scale   must   be   a   positive   integer   or   zero");
        }
        if (scale == 0) {
            return new DecimalFormat("0").format(v);
        }
        String formatStr = "0.";
        for (int i = 0; i < scale; i++) {
            formatStr = formatStr + "0";
        }
        return new DecimalFormat(formatStr).format(v);
    }
}
