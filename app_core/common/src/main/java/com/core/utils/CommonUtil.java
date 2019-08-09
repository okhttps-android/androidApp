package com.core.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.dao.DBManager;
import com.core.dao.SignAutoLogDao;
import com.core.interfac.OnVoiceCompleteListener;
import com.core.model.EmployeesEntity;
import com.core.xmpp.model.SignAutoLogEntity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.uas.applocation.UasLocationHelper;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

import static android.view.View.DRAWING_CACHE_QUALITY_AUTO;
import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;
import static android.view.View.DRAWING_CACHE_QUALITY_LOW;

/**
 * Created by Arison on 2017/8/29.
 */
public class CommonUtil {

    private final static String AppShareName = "setting";
    public static int counter = 0;
    
    public   static void displayBriefMemory(){
        ActivityManager manager = (ActivityManager)MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        int heapSize = manager.getMemoryClass();
        int maxHeapSize = manager.getLargeMemoryClass(); 
        
        LogUtil.d(TAG, "heapSize:"+heapSize+" maxHeapSize:"+maxHeapSize);
        
    }

    public static String getAppBaseUrl(Context ct) {
        if (ct == null) {
            ct = MyApplication.getInstance();
        }
        String baseUrl = getSharedPreferences(ct, "erp_baseurl");
        return baseUrl;
    }

    public static String getReportUrl(Context ct) {
        if (ct == null) {
            return "";
        }
        String baseUrl = getSharedPreferences(ct, "extrajaSperurl");
        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = getAppBaseUrl(ct);
        }
        return baseUrl;
    }

    /**
     * 账户中心token
     *
     * @param context
     * @return
     */
    public static String getAccountToken(Context context) {
        if (context == null) {
            return "";
        }
        String accountToken = CommonUtil.getSharedPreferences(context, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        return accountToken;
    }

    public static String getEnuu(Context context) {
        if (context == null) {
            return "";
        }
        String enuu = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu");
//        enuu = "10041166";
        return enuu;
    }

    public static String getUseruu(Context context) {
        if (context == null) {
            return "";
        }
        String useruu = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu");
//        useruu = "1000003217";
        return useruu;
    }

    public static long getEnuuLong(Context context) {
        if (context == null) {
            return -1;
        }
        String enuu = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu");
        long enuuLong = -1;
        try {
            enuuLong = Long.parseLong(enuu);
        } catch (Exception e) {
            enuuLong = -1;
        }
//        enuuLong = 10041166;
        return enuuLong;
    }

    public static long getUseruuLong(Context context) {
        if (context == null) {
            return -1;
        }
        String useruu = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu");
        long useruuLong = -1;
        try {
            useruuLong = Long.parseLong(useruu);
        } catch (Exception e) {
            useruuLong = -1;
        }
//        useruuLong = 1000003217;
        return useruuLong;
    }

    public static String getEnName() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String enName = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyName") : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_commpany");
        if (enName == null) {
            enName = "";
        }
        return enName;
    }

    /**
     * 智慧产城的根路径
     *
     * @param ct
     * @return
     */
    public static String getCityBaseUrl(Context ct) {
        if (ct == null) {
            return "";
        }
//        return "https://admin-city.ubtob.com/";
        return "https://city-service.ubtob.com/city/thxz/";
//        return "http://192.168.253.29:8080/ERP/";
    }

    /**
     * 询价服务的根路径
     *
     * @param ct
     * @return
     */
    public static String getInquiryBaseUrl(Context ct) {
        if (ct == null) {
            return "";
        }
        return "https://api-inquiry.usoftchina.com/";
    }

    //日程管理网址
    public static String getSchedulerBaseUrl() {
//        if(BaseConfig.isDebug()){
//             return "http://192.168.253.130:8080/schedule/";
//        }
        return "https://mobile.ubtob.com:8443/schedule/";
    }

    /**
     * B2B身份uid
     *
     * @param ct
     * @return
     */
    public static String getB2BUid(Context ct) {
        if (ct == null) {
            return "";
        }
        return getSharedPreferences(ct, Constants.B2B_UID_CACHE) == null ? "" : getSharedPreferences(ct, Constants.B2B_UID_CACHE);
    }

    private static final String TAG = "CommonUtil";

    /**
     * B2B身份session
     *
     * @param ct
     * @return
     */
    public static String getB2BSession(Context ct) {
        if (ct == null) {
            LogUtil.d(TAG, "ct ==null");
            return "";
        }
        return getSharedPreferences(ct, Constants.B2B_SESSION_CACHE) == null ? "" : getSharedPreferences(ct, Constants.B2B_SESSION_CACHE);
    }

    /**
     * ERP身份session
     *
     * @param ct
     * @return
     */
    public static String getErpCookie(Context ct) {
        if (ct == null) {
            return "";
        }
        return "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId");
    }

    /**
     * Created by FANGlh on 2017/5/5.
     * function:判断是不是发布版本，true：发布版本
     */
    public static Boolean isReleaseVersion() {
//                return !BuildConfig.DEBUG;  //垃圾方法，得根据自己选择的debug还是release模式返回，根本就不好用，
//                 只要改变 common目录下的versionconfiguration就可以了
//        if (!StringUtil.isEmpty(PropertiesUtil.readData(MyApplication.getInstance(), "release_version", R.raw.versionconfiguration)) &&                "true".equals(PropertiesUtil.readData(MyApplication.getInstance(), "release_version", R.raw.versionconfiguration)))            return true;
//        else
//            return false;
        return !BaseConfig.isDebug();
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

    public static boolean setUniqueSharedPreferences(Context ct, String key, String value) {
        if (key != null) {
            SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
            boolean falg = false;
            String userId = MyApplication.getInstance().mLoginUser.getUserId();
            String role = CommonUtil.getUserRole();
            if (role.equals("1")) {//个人用户
                falg = sPreferences.edit().putString(userId + key, value).commit();
            } else if (role.equals("3")) {//b2b用户
                String companyName = CommonUtil.getSharedPreferences(ct, "companyName");
                falg = sPreferences.edit().putString(userId + companyName + key, value).commit();
            } else if (role.equals("2")) {//ERP用户
                String erp_company = CommonUtil.getSharedPreferences(ct, "erp_commpany");
                String erp_master = CommonUtil.getSharedPreferences(ct, "erp_master");
                falg = sPreferences.edit().putString(userId + erp_company + erp_master + key, value).commit();
            }
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

    public static String getUniqueSharedPreferences(Context ct, String key) {
        if (key == null || ct == null) {
            return null;
        }
        SharedPreferences sPreferences = ct.getSharedPreferences(AppShareName, Context.MODE_PRIVATE);
        String value = null;
        String userId = MyApplication.getInstance().mLoginUser.getUserId();
        String role = CommonUtil.getUserRole();
        if (role.equals("1")) {//个人用户
            value = sPreferences.getString(userId + key, null);
        } else if (role.equals("3")) {//b2b用户
            String companyName = CommonUtil.getSharedPreferences(ct, "companyName");
            value = sPreferences.getString(userId + companyName + key, null);
        } else if (role.equals("2")) {//ERP用户
            String erp_company = CommonUtil.getSharedPreferences(ct, "erp_commpany");
            String erp_master = CommonUtil.getSharedPreferences(ct, "erp_master");
            value = sPreferences.getString(userId + erp_company + erp_master + key, null);
        }
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

    // 转换dip为px
    public static int convertDip2Px(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
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
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {

        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {

        }
    };


    /*获取账套，erp的账套  b2b的公司uu号*/
    public static String getMaster() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String master = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")
                : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
//      if (BaseConfig.isDebug()){
//          master="UAS_DEV";
//      }
        return master;

    }

    public static String getEmcode() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String emcode = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu") : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        if (emcode == null) emcode = "";
        return emcode;
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
            try {
                return Integer.parseInt(m.group(0));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }


    public static String getName() {
        String name = getSharedPreferences(MyApplication.getInstance(), "erp_emname");
        if (StringUtil.isEmpty(name)) {
            DBManager dbManager = new DBManager();
            String userId = MyApplication.getInstance().getLoginUserId();
            String whichsys = CommonUtil.getMaster();
            List<EmployeesEntity> entities = dbManager.select_getEmployee(new String[]{userId, whichsys}, "em_imid=? and whichsys=?");
            if (!ListUtils.isEmpty(entities)) {
                name = entities.get(0).getEM_NAME();
            }
        }
        if (StringUtil.isEmpty(name)) {
            name = MyApplication.getInstance().mLoginUser.getNickName();
        }
        return StringUtil.isEmpty(name) ? "" : name;
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
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return versionCode;
        } else {
            return null;
        }

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

    /**
     * @desc:打电话确认框
     * @author：Arison on 2016/11/3
     */
    public static void phoneAction(final Context mContext, final String phone) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext).title(R.string.dialog_confim_phone).content(mContext.getString(R.string.dialog_phone) + phone)
                .positiveText(R.string.dialog_phone_action).negativeText(R.string.common_cancel).autoDismiss(false).callback(new MaterialDialog.ButtonCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // 用intent启动拨打电话
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).build();

        dialog.show();
    }

    public static String getUserRole() {
        String userRole = getSharedPreferences(MyApplication.getInstance(), "userRole");
        return userRole == null ? "2" : userRole;
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
     * 检测网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission")
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
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

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
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


    /**
     * 检测Sdcard是否存在
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 打开软键盘
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void openKeybord(final EditText mEditText, final Context mContext) {
        if (mContext != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditText, 0);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
//                    InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }, 100);
        }
    }

    /**
     * 关闭软键盘
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void closeKeybord(final EditText mEditText, final Activity mContext) {
        if (mContext != null) {
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
//                    if (imm.isActive(mEditText)){
                    imm.hideSoftInputFromWindow(mContext.getWindow().getDecorView().getWindowToken(), 0);
//                    }
                }
            }, 100);

//            final Activity activity = (Activity) mContext;
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    InputMethodManager mInputKeyBoard = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    if (activity.getCurrentFocus() != null) {
//                        mInputKeyBoard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
////                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//                    }
//                }
//            });
        }

    }

    /**
     * 关闭软键盘
     *
     * @param mContext 上下文
     */
    public static void closeKeybord(final Activity mContext) {
        if (mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && mContext.getCurrentFocus() != null) {
                if (mContext.getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
    }

    public static boolean isBiteman() {
        String baseUrl = getSharedPreferences(MyApplication.getInstance(), "erp_base");
        return !StringUtil.isEmpty(baseUrl) && baseUrl.contains("http://202.104.151.184:8099/ERP/");
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
     * 从asset路径下读取对应文件转String输出
     *
     * @param mContext
     * @return
     */
    public static String getAssetsJson(Context mContext, String fileName) {
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    //删除文件夹
    //param folderPath 文件夹完整绝对路径
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUTF8String(String str) {
        String xmString = "";
        String xmlUTF8 = "";
        StringBuffer sb = new StringBuffer();
        sb.append(str);
        try {
            xmString = new String(sb.toString().getBytes("UTF-8"));
            xmlUTF8 = URLEncoder.encode(xmString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return xmlUTF8;
    }

    /**
     * 获取IP与域名
     *
     * @param uri
     * @return
     */
    public static URI getIP(URI uri) {
        URI effectiveURI = null;

        try {
            // URI(String scheme, String userInfo, String host, int port, String
            // path, String query,String fragment)
            effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
        } catch (Throwable var4) {
            effectiveURI = null;
        }

        return effectiveURI;
    }

    /**
     * 语音输入
     */
    public static void getVoiceText(Context context, final EditText editText, @Nullable final OnVoiceCompleteListener onVoiceCompleteListener) {
        RecognizerDialog dialog = new RecognizerDialog(context, null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                String s = editText.getText().toString() + CommonUtil.getPlaintext(text);
                editText.setText(s);
                editText.setSelection(s.length());

                if (onVoiceCompleteListener != null && b) {
                    onVoiceCompleteListener.onVoiceComplete(s);
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        dialog.show();

    }

    /**
     * 获取某视图下截图，返回其BitMap对象 ,该方案目前只能针对简单的view视图
     *
     * @param view
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap getViewToBitmap(View view) {
        Bitmap bitmap = null;
        int w_r = view.getRight();
        int w_l = view.getLeft();
        int width = w_r - -w_l;


        int h_b = view.getBottom();
        int h_t = view.getTop();
        int height = h_b - h_t;

        final boolean opaque = view.getDrawingCacheBackgroundColor() != 0 || view.isOpaque();
        Bitmap.Config quality;
        if (!opaque) {
            switch (view.getDrawingCacheQuality()) {
                case DRAWING_CACHE_QUALITY_AUTO:
                case DRAWING_CACHE_QUALITY_LOW:
                case DRAWING_CACHE_QUALITY_HIGH:
                default:
                    quality = Bitmap.Config.ARGB_8888;
                    break;
            }
        } else {
            quality = Bitmap.Config.RGB_565;
        }
        if (width <= 0 || height <= 0) {
            width = 960;
            height = 960;
        }
        bitmap = Bitmap.createBitmap(MyApplication.getInstance().getResources().getDisplayMetrics(),
                width, height, quality);
        bitmap.setDensity(MyApplication.getInstance().getResources().getDisplayMetrics().densityDpi);
        if (opaque) bitmap.setHasAlpha(false);
        boolean clear = view.getDrawingCacheBackgroundColor() != 0;
        Canvas canvas = new Canvas(bitmap);
        if (clear) {
            bitmap.eraseColor(view.getDrawingCacheBackgroundColor());
        }
        view.computeScroll();
        final int restoreCount = canvas.save();
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);
        canvas.restoreToCount(restoreCount);
        canvas.setBitmap(null);
        return bitmap;
    }


    /**
     * 获取某视图下截图，返回其BitMap对象 ,该方案目前只可用
     *
     * @param view
     * @return
     */
    public static Bitmap getViewToBitmap2(View view) {
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }

    public static String UTF8ToISO(String value) {
        String vString = null;
        try {
            vString = new String(value.getBytes("UTF-8"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return vString;
    }

    public static String ISOToUTF8(String value) {
        String vString = null;
        try {
            vString = new String(value.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return vString;
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
     * 是否控件重复点击
     *
     * @return
     */
    public static boolean isRepeatClick(long time) {
        long currentTime = System.currentTimeMillis();
        long intervals = currentTime - mLastClickTime;
        if (intervals > 0 && intervals < time) {
            return true;
        }
        mLastClickTime = currentTime;
        return false;

    }

    /**
     * EditText竖直方向是否可以滚动
     *
     * @param editText 需要判断的EditText
     * @return true：可以滚动   false：不可以滚动
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

    /**
     * 集合的深度克隆
     *
     * @param src
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }


    /**
     * double 相减
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double doublesubtract(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.subtract(bd2).doubleValue();
    }

    /**
     * double 相加
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double doubleAddition(double d1, double d2) {
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(d1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(d2));
        return bigDecimal1.add(bigDecimal2).doubleValue();
    }

    /**
     * double 乘法
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double doubleMul(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.multiply(bd2).doubleValue();
    }

    /**
     * double 除法
     *
     * @param d1
     * @param d2
     * @param scale 四舍五入 小数点位数
     * @return
     */
    public static double doubleDiv(double d1, double d2, int scale) {
        //  当然在此之前，你要判断分母是否为0，
        //  为0你可以根据实际需求做相应的处理

        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.divide(bd2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * double 相比较大小
     *
     * @param d1
     * @param d2
     * @return 小于：-1；等于：0；大于：1
     */
    public static int doubleCompare(double d1, double d2) {
        BigDecimal data1 = new BigDecimal(Double.toString(d1));
        BigDecimal data2 = new BigDecimal(Double.toString(d2));
        return data1.compareTo(data2);
    }

    /**
     * double值小数点后为0则不显示，保留四位小数
     *
     * @param v
     * @return
     */
    public static String doubleFormat(double v) {
        DecimalFormat decimalFormat = new DecimalFormat("###.####");
        return decimalFormat.format(v);
    }


    /**
     * 添加图片的toast
     *
     * @param context
     * @param imageId  图片id
     * @param content  文字内容
     * @param duration 显示时长
     */
    public static void imageToast(Context context, int imageId, String content, int duration) {
        Toast toast = new Toast(context);
        //显示的时间
        toast.setDuration(duration);
        //显示的位置
        toast.setGravity(Gravity.BOTTOM, 0, 300);

        //自定义toast布局
        LinearLayout toastLayout = new LinearLayout(context);
        toastLayout.setGravity(Gravity.CENTER_VERTICAL);
        toastLayout.setOrientation(LinearLayout.HORIZONTAL);

        //添加ImageView
        ImageView toastImage = new ImageView(context);
        toastImage.setImageResource(imageId);
        toastLayout.addView(toastImage);

        //添加TextView
        TextView toastText = new TextView(context);
        toastText.setBackgroundColor(context.getResources().getColor(R.color.toast_bg));
        toastText.setText(content);
        toastLayout.addView(toastText);

        toastLayout.setBackgroundColor(context.getResources().getColor(R.color.toast_bg));
        toast.setView(toastLayout);
        toast.show();
    }

    /**
     * @desc:修改textView样式(添加下划线)
     * @author：Arison on 2016/8/3
     */
    public static void textUnderlineForStyle(
            TextView view,
            String input,
            String match) {
        SpannableStringBuilder style = new SpannableStringBuilder(input);
        Pattern highlight = Pattern.compile(match);
        Matcher m = highlight.matcher(style.toString());
        while (m.find()) {
//            style.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new ForegroundColorSpan(color), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new StrikethroughSpan(), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            style.setSpan(new UnderlineSpan(), m.start(), m.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        view.setText(style);
    }


    public static ACProgressFlower newLoading(Context context, String text) {
        return new ACProgressFlower.Builder(context)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text(text)
                .fadeColor(Color.DKGRAY).build();
    }

}
