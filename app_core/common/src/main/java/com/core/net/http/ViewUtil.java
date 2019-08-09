package com.core.net.http;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.thread.ThreadPool;
import com.common.thread.ThreadUtil;
import com.core.adapter.ItemPopListAdapter;
import com.core.adapter.ItemsSelectType1;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.base.BaseActivity;
import com.core.model.LoginEntity;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.DialogUtils;
import com.core.utils.FlexJsonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;
import com.core.widget.crouton.Crouton;
import com.core.widget.crouton.Style;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ViewUtil {
    public static Context ct;
    public static String erp_phone;
    public static String erp_password;
    public static String erp_master;
    public static String erp_username;
    public static String erp_baseurl;
    public static String b2b_uu;
    public static String erp_uu;//推送切公司
    public static String erp_masterId;//推送切公司
    public static String erp_company;
    public static String mBusinessCode;//公司营业执照
    public static MaterialDialog mdProcessDialog;
    private static MaterialDialog loginERPDialog;
    private static List<LoginEntity> erpEntities;
    private static boolean hasErp = true;//是否有erp
    public static Crouton crouton;

    public static CustomProgressDialog progressDialog;

    public static void ToastMessage(Context cont, String msg) {
        ToastUtil.showToast(cont,msg);
    }


    public static void ToastMessage(Context cont, String msg, int toastColor, int toastTime) {
        ToastUtil.showToast(cont,msg);
    }

    public static void ShowMessageTitle(Context ct, String msg) {
        try {
            if (ct == null || ct.isRestricted()) return;
            new MaterialDialog.Builder(ct).title(MyApplication.getInstance().getString(R.string.app_dialog_title)).content(msg)
                    .positiveText(MyApplication.getInstance().getString(R.string.app_dialog_close)).autoDismiss(false)
                    .callback(new ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
        } catch (Exception e) {

        }
    }

    public static void ShowMessageTitleAutoDismiss(Context ct, String msg, long time) {
        try {
            final MaterialDialog messageDialog = new MaterialDialog.Builder(ct).title("系统提示").content(msg)
                    .positiveText("关闭").autoDismiss(false)
                    .callback(new ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        messageDialog.dismiss();
                    } catch (Exception e) {

                    }
                }
            }, time);
        } catch (Exception e) {

        }
    }


    public static String getDataFromServer(String url,
                                           Map<String, String> params, String method) {
        String result = null;
        HttpClient hClient = new HttpClient();
        try {
            if (method.equals("get")) {
                result = hClient.sendGetRequest(url, params);
            }
            if (method.equals("post")) {
                result = hClient.sendPostRequest(url, params);
            }
        } catch (Exception e) {
            handler.sendEmptyMessage(Constants.SocketTimeoutException);
        }
        return result;
    }


    @Deprecated
    public static void startNetThread(final String url,
                                      final Map<String, String> params, final Handler handler,
                                      final int what, final Message message, final Bundle bundle,
                                      final String request) {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                boolean isNetHas = NetUtils.isNetWorkConnected(MyApplication.getInstance());
                if (isNetHas) {
                    String result = getDataFromServer(url, params, request);
                    if (result != null) {
                        if (bundle == null || message == null) {
                            Bundle bundle = new Bundle();
                            Message message = new Message();
                            bundle.putString("result", result);
                            message.setData(bundle);
                            message.what = what;
                            handler.sendMessage(message);
                        } else {
                            bundle.putString("result", result);
                            message.setData(bundle);
                            message.what = what;
                            handler.sendMessage(message);
                        }
                    } else {
//                        Bundle bundle = new Bundle();
//                        Message message = new Message();
//                        bundle.putString("result", "{\n" +
//                                "\"exception\":\"系统内部错误！\"\n" +
//                                "}");
//                        message.setData(bundle);
//                        message.what = Constants.APP_SOCKETIMEOUTEXCEPTION;
//                        handler.sendMessage(message);
                    }
                } else {
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    bundle.putString("result", "{\n" +
                            "\"exception\":\"" + MyApplication.getInstance().getString(R.string.common_notlinknet) + "！\"\n" +
                            "}");
                    message.setData(bundle);
                    message.what = Constants.APP_SOCKETIMEOUTEXCEPTION;
                    handler.sendMessage(message);

                }
            }
        });
    }


    public static void startNetThread(final Context ct, final String url,
                                      final Map<String, String> params, final Handler handler,
                                      final int what, final Message message, final Bundle bundle,
                                      final String request) {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                boolean isNetHas = NetUtils.isNetWorkConnected(ct);
                if (isNetHas) {
                    String result = getDataFromServer(url, params, request);
                    LogUtil.prinlnLongMsg("result", "http 发送请求-----------------------------------------");
                    LogUtil.prinlnLongMsg("result", "url：" + url);
                    LogUtil.prinlnLongMsg("resultParams", "parms：" + params.toString());
                    LogUtil.prinlnLongMsg("result", "http:" + result);
                    if (result != null) {
                        if (bundle == null || message == null) {
                            Bundle bundle = new Bundle();
                            Message message = new Message();
                            bundle.putString("result", result);
                            message.setData(bundle);
                            message.what = what;
                            handler.sendMessage(message);
                        } else {
                            bundle.putString("result", result);
                            message.setData(bundle);
                            message.what = what;
                            handler.sendMessage(message);
                        }
                    } else {
                        // ToastMessage(ct, "服务器未知错误！");
                    }
                } else {
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    bundle.putString("result", "{\"exception\":\"" + MyApplication.getInstance().getString(R.string.common_notlinknet) + "\"}");
                    message.setData(bundle);
                    message.what = what;
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * @注释：管理平台接口登录功能
     */
    public static void LoginTask(final String user_phone, final String user_password, final Context ct) {
        ViewUtil.ct = ct;
        progressDialog= CustomProgressDialog.createDialog(ct);
        LogUtil.d("AppInfo","password:"+user_password+" phone:"+user_phone);
        mdProcessDialog = new MaterialDialog.Builder(ct).title(
                MyApplication.getInstance().getString(R.string.app_dialog_title))
                .content(MyApplication.getInstance().getString(R.string.login_progress_erp))
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .build();
        if (!((BaseActivity) ViewUtil.ct).isFinishing()) {
            mdProcessDialog.show();
        }
        HttpRequest.getInstance().sendRequest(Constants.ACCOUNT_CENTER_HOST,
                new HttpParams.Builder()
                        .url("sso/login/mobile")
                        .addParam("mobile", user_phone)
                        .addParam("password", user_password)
                        .method(Method.POST)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        Log.d("账户中心登录成功", o.toString());
                        String result = o.toString();
                        JSONObject resultObject = JSON.parseObject(result);
                        boolean error = JSONUtil.getBoolean(resultObject, "error");
                        if (error) {
                            String errorMsg = JSONUtil.getText(resultObject, "errMsg");
                            if (mdProcessDialog != null) {
                                mdProcessDialog.dismiss();
                            }
                            ToastMessage(MyApplication.getInstance(),errorMsg);
                        } else {
                            String loginToken = JSONUtil.getText(resultObject, "token");
                            CommonUtil.setSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN, loginToken);
                            JSONArray accountArray = resultObject.getJSONArray("datalist");
                            if (accountArray != null) {
                                String accounts = JSON.toJSONString(accountArray);
                                LoginSucess(accounts, user_phone, user_password);
                            } else {
                                ToastMessage(MyApplication.getInstance(),"企业列表为空");
                            }
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        Log.d("账户中心登录失败", failStr);
                        if (mdProcessDialog != null) {
                            mdProcessDialog.dismiss();
                        }
                        ToastMessage(MyApplication.getInstance(),failStr);
                    }
                });
    }

    @Deprecated
    public static void showDialogB2B(final String phone, final String password, final List<LoginEntity.Spaces> models) {
        String[] items = new String[models.size()];
//        int select = 0;
        for (int j = 0; j < models.size(); j++) {
            items[j] = models.get(j).getName();
        }
//        if (CommonUtil.isDialogShowing(loginERPDialog)) {
//            return;
//        } else {
        new MaterialDialog.Builder(ct)
                .title(MyApplication.getInstance().getString(R.string.login_company_select))
                .items(items)
                .itemsCallbackSingleChoice(0,
                        new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog,
                                                       View view, int which, CharSequence text) {
                                if (!StringUtil.isEmpty(text.toString())) {
                                    LoginEntity.Spaces model = models.get(which);
                                    CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "spaceId", String.valueOf(model.getId()));
                                    CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyName", String.valueOf(model.getName()));
                                    ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().setEnuu(String.valueOf(model.getEnuu()));
//                                    CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu", String.valueOf(model.getEnuu()));
                                    LoginB2BTask(phone, password, model.getEnuu());
                                }
                                return true;
                            }
                        }).positiveText(MyApplication.getInstance().getString(R.string.common_sure)).show();
//        }

    }

    // 登录B2B
    public static void LoginB2BTask(String phone, String password, String enuu) {
        mdProcessDialog.setContent(MyApplication.getInstance().getString(R.string.login_progress_b2b));
        String url = ApiConfig.getInstance(new ApiPlatform()).getmApiBase().login;
        LogUtil.d("HttpLogs", "b2b login url:" + url);
        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", "b2b");
        params.put("username", phone);
        params.put("password", password);
//        if (!BaseConfig.isDebug()) {
//        params.put("spaceId", String.valueOf(spaceId));
        params.put("spaceUU", enuu);
//        } else {
//            params.put("spaceId", "76035");
//        }
        LogUtil.d("HttpLogs", "params:" + JSON.toJSONString(params));
        startNetThread(url, params, handler, Constants.SUCCESS_B2B, null, null, "post");
    }


    public static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SUCCESS_LOGIN:
//                    LoginSucess(msg);
                    break;
                case Constants.SUCCESS_ERP:
                    String result = msg.getData().getString("result");
                    loginErpSuccess(result);
                    break;
                case Constants.SUCCESS_B2B:
                    if (msg.getData() != null) {
                        ChangeStatusB2B(msg.getData().getString("result"));
                    }
                    break;
                case Constants.SocketTimeoutException:
                    if (mdProcessDialog != null && mdProcessDialog.isShowing()) {
                        mdProcessDialog.dismiss();
                    }
                    ToastMessage(MyApplication.getInstance(), "当前网络不佳，请稍等！", Style.holoRedLight, 3000);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (JSONUtil.validate(msg.getData().getString("result"))) {
                        ToastMessage(MyApplication.getInstance(),
                                JSON.parseObject(msg.getData().getString("result")).getString("exception"), Style.holoRedLight, 3000);
                    } else {
                        boolean isNetHas = NetUtils.isNetWorkConnected(ct);
                        if (isNetHas)
                            ToastMessage(MyApplication.getInstance(), "系统内部错误！", Style.holoRedLight, 3000);
                    }
                    break;
                default:
                    break;
            }
        }

    };

    private static void loginErpSuccess(String result) {
        //B2B商务以及邀请注册等功能需要用到B2B的个人uu号
        CommonUtil.setSharedPreferences(ct, "b2b_uu", b2b_uu);
        CommonUtil.setSharedPreferences(ct, Constants.CACHE.EN_BUSINESS_CODE, mBusinessCode);
        try {
            if (JSONUtil.validate(result) && result != null) {
                //登录ERP成功，清除B2B缓存
                CommonUtil.clearSharedPreferences(ct, Constants.B2B_SESSION_CACHE);
                CommonUtil.clearSharedPreferences(ct, Constants.B2B_UID_CACHE);
                boolean success = JSON.parseObject(result).getBoolean("success");
                if (success) {
                    Intent intent = new Intent("com.app.home.update");
                    intent.putExtra("falg", "home");
                    ct.sendBroadcast(intent);
                    ChangeStatusERP(result);
                } else {
                    String reason = JSON.parseObject(result).getString("reason");
                    ToastMessage(MyApplication.getInstance(), reason, Style.holoGreenLight, 3000);
                }
            } else {
                ToastMessage(MyApplication.getInstance(), "接口数据非法！", Style.holoRedLight, 3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastMessage(MyApplication.getInstance(), MyApplication.getInstance().getString(R.string.login_error_erp), Style.holoRedLight, 3000);
        }
    }

    public static void ChangeStatusERP(String result) {
        ChangeStatusERP(result, true);
    }

    public static void ChangeStatusERP(String result, boolean isBrodcast) {
        if (mdProcessDialog != null) {
            mdProcessDialog.setContent(MyApplication.getInstance().getString(R.string.login_success_erp));
        }
        if (ct == null) ct = MyApplication.getInstance();
        Map<String, Object> dataMap = FlexJsonUtil.fromJson(result);
        try {
            CommonUtil.setSharedPreferences(ct, "sessionId",
                    dataMap.get("sessionId").toString());
            CommonUtil.setSharedPreferences(ct, "erp_username",
                    dataMap.get("erpaccount").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //必须不为空，才能不覆盖原有的根路径
        if (!StringUtil.isEmpty(erp_baseurl)) {
            Object master = dataMap.get("master");
            Object masterId = dataMap.get("masterId");
            String erpMaster = "";
            String erpMasterId = "";
            if (master != null) {
                erpMaster = master.toString();
            }
            if (masterId != null) {
                erpMasterId = masterId.toString();
            }
            CommonUtil.setSharedPreferences(ct, "erp_master", erpMaster);
            CommonUtil.setSharedPreferences(ct, "erp_commpany", erp_company);
            String enuu = String.valueOf(dataMap.get("uu"));
            if ("null".equals(enuu) || "(null)".equals(enuu)) {
                enuu = "";
            }
            CommonUtil.setSharedPreferences(ct, "erp_uu", enuu);
            CommonUtil.setSharedPreferences(ct, "erp_masterId", erpMasterId);
            //添加获取报表地址
            String en_admin = JSONUtil.getText(result, "EN_ADMIN");
            String extrajaSperurl = JSONUtil.getText(result,
                    "jasper".equals(en_admin) ? "EN_URL" : "EN_EXTRAJASPERURL");
            LogUtil.i("gong","login extrajaSperurl="+extrajaSperurl);
            CommonUtil.setSharedPreferences(ct, "extrajaSperurl", extrajaSperurl);
            CommonUtil.setSharedPreferences(ct, "erp_login", true);
            CommonUtil.setSharedPreferences(ct, "erp_emname", String.valueOf(dataMap.get("emname")));
        }
        if (erp_phone != null) {
            CommonUtil.setSharedPreferences(ct, "user_phone", erp_phone);
            CommonUtil.setSharedPreferences(ct, "user_password", erp_password);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mdProcessDialog != null) {
                    mdProcessDialog.cancel();
                }
            }
        }, 1500);
        if (ct instanceof Activity) {
            if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_baseurl"))) {
            } else {
                ct.startActivity(new Intent("com.modular.main.DataDownloadActivity"));
            }
            CommonUtil.setSharedPreferences(ct, "erp_baseurl", erp_baseurl);
        }else{
            if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_baseurl"))) {
            } else {
              MyApplication.getInstance().startActivity(new Intent("com.modular.main.DataDownloadActivity"));
            }
            CommonUtil.setSharedPreferences(ct, "erp_baseurl", erp_baseurl);
        }
        if (isBrodcast) {
            sendBrodcast("ERP");
        }
    }

    public static void ChangeStatusB2B(String result) {
        LogUtil.d("LoginInfo", "B2B login:" + result);
        if (!JSONUtil.validate(result)) return;
        if (JSON.parseObject(result).getBoolean("success") == null) {
            if (mdProcessDialog == null) return;
            mdProcessDialog.setContent(MyApplication.getInstance().getString(R.string.login_error_b2b));
            mdProcessDialog.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mdProcessDialog.cancel();
                }
            }, 1500);
            return;
        }
        if (erp_phone != null) {
            CommonUtil.setSharedPreferences(ct, "user_phone", erp_phone);
            CommonUtil.setSharedPreferences(ct, "user_password", erp_password);
        }
        if (ct == null) ct = MyApplication.getInstance();

        CommonUtil.setSharedPreferences(ct, "erp_login", false);
        CommonUtil.setSharedPreferences(ct, "b2b_uu", b2b_uu);
        CommonUtil.setSharedPreferences(ct, Constants.CACHE.EN_BUSINESS_CODE, mBusinessCode);
        mdProcessDialog.setContent(MyApplication.getInstance().getString(R.string.login_success_b2b));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mdProcessDialog.cancel();
            }
        }, 1500);
        if (ct instanceof Activity) {
            if (CommonUtil.getSharedPreferencesBoolean(MyApplication.getInstance(), "b2b_login")) {
            } else {
                CommonUtil.setSharedPreferences(ct, "b2b_login", true);
                ct.startActivity(new Intent("com.modular.main.DataDownloadActivity"));
            }
        }

        sendBrodcast("B2B");
    }

    public static void sendBrodcast(String falg) {
        Intent intent = new Intent("com.app.home.update");
        intent.putExtra("falg", falg);
        if (ct != null) {
            LocalBroadcastManager.getInstance(ct).sendBroadcast(intent);
        }
    }

    /**
     * @desc:uas账套对话框
     * @author：Arison on 2017/3/6
     */
    private static void loginERPItemDialog(final String phone, final String password) {
        String[] items = new String[erpEntities.size()];
        int select = 0;
        for (int j = 0; j < erpEntities.size(); j++) {
            items[j] = erpEntities.get(j).getName();
        }
        if (DialogUtils.isDialogShowing(loginERPDialog)) {
            return;
        } else {
            if (loginERPDialog != null) {
                select = loginERPDialog.getSelectedIndex();
            }
            if (ct == null) return;
            loginERPDialog = new MaterialDialog.Builder(ct)
                    .title(ct.getString(R.string.user_dialog_company))
                    .items(items)
                    .itemsCallbackSingleChoice(select,
                            new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog,
                                                           View view, int which, CharSequence text) {
                                    if (!StringUtil.isEmpty(text.toString())) {
                                        LoginEntity entity = erpEntities
                                                .get(which);
                                        erp_phone = phone;
                                        erp_password = password;
                                        erp_username = entity.getAccount();
                                        erp_master = entity.getMaster();
                                        erp_baseurl = entity.getWebsite();
                                        erp_company = entity.getName();
                                        erp_uu = String.valueOf(entity
                                                .getEnuu());
                                        erp_masterId = String.valueOf(entity.getMasterId());
                                        LoginERPTask(entity.getWebsite(), erp_uu,
                                                entity.getMaster(), phone, password);
                                        //清除账套列表缓存
                                        CommonUtil.clearSharedPreferences(ct, "erp_masterlist");
                                    }
                                    return true;
                                }
                            }).positiveText(ct.getString(R.string.common_sure)).show();
        }

    }


   
      /*@功能:管理平台的返回信息处理*/
    public static void LoginSucess(String json, String phone, String password) {
        LogUtil.d("AppInfo","phone:"+phone+"password:"+password);
        CommonUtil.setSharedPreferences(ct, "loginJson", json);
        try {
            JSONArray array = JSON.parseArray(json);
            if (!ListUtils.isEmpty(array)) {
                JSONObject object = array.getJSONObject(0);
                if (object != null && object.containsKey("userName")) {
                    String userName = getNameByB2b(object.getString("userName"), phone);
                    if (!StringUtil.isEmpty(userName))
                        CommonUtil.setSharedPreferences(MyApplication.getInstance(), "erp_emname", userName);
                }
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
        if (JSONUtil.validate(json)) {
            // showDialogCompany(json, phone, password);
            showLoginDialog(json, phone, password);
        } else {
            ToastMessage(ct, "接口数据非法!", Style.holoRedLight, 3000);
        }
    }



    public static void showLoginDialog(String loginResult, final String phone, final String password) {
        LogUtil.d("AppInfo","phone:"+phone+"password:"+password);
        List<LoginEntity> loginMsg = JSON.parseArray(loginResult, LoginEntity.class);
        List<LoginEntity> loginEntities = new ArrayList<>();
        boolean isHasUas = false;
        if (!ListUtils.isEmpty(loginMsg)) {
            if (loginMsg.size() > 1) {
                isHasUas = true;
            }
            for (int i = 0; i < loginMsg.size(); i++) {
                LoginEntity model = loginMsg.get(i);
                if (model.getPlatform().equals("ERP")) {
                    isHasUas = true;
                    if (BaseConfig.isDebug()&&model.getName().equals("UAS研发系统（测试）")){
                        model.setWebsite("https://218.17.158.219:9443/uas_dev/");
                    }

                    loginEntities.add(model);
                } else if (model.getPlatform().equals("B2B")) {
                    List<LoginEntity.Spaces> sModel = model.getSpaces();
                    b2b_uu = model.getAccount();
                    if (!ListUtils.isEmpty(sModel)) {
                        for (int j = 0; j < sModel.size(); j++) {
                            //去重逻辑
                            boolean isHas = false;
                            for (int k = 0; k < loginMsg.size(); k++) {
                                if (sModel.get(j).getName().equals(loginMsg.get(k).getName())) {
                                    isHas = true; //存在  uas系统里面存在同名的
                                }
                            }
                            if (!isHas) {
                                LoginEntity bModel = new LoginEntity();
                                bModel.setEnuu(Integer.valueOf(sModel.get(j).getEnuu()));
                                bModel.setName(sModel.get(j).getName());
                                bModel.setSpaceId(sModel.get(j).getId());
                                bModel.setPlatform("B2B");
                                bModel.setUserName(model.getUserName());
                                bModel.setBusinessCode(sModel.get(j).getBusinessCode());
                                loginEntities.add(bModel);
                            }
                        }
                    } else {
                        if (!isHasUas) {
                            //没有uas的情况
//                            model.setPlatform("个人");
//                            model.setName(model.getUserName() + "(个人用户)");
//                            loginEntities.add(model);
                            CommonUtil.setSharedPreferences(ct, "b2b_uu", b2b_uu);
                            CommonUtil.clearSharedPreferences(ct, Constants.CACHE.EN_BUSINESS_CODE);
                            CommonUtil.setSharedPreferences(MyApplication.getInstance(), "userRole", "1");
                            ct.startActivity(new Intent("com.modular.main.DataDownloadActivity"));
                        } else {

                        }
                    }
                }
            }
            LogUtil.d("HttpLogs", "login dialog:" + JSON.toJSONString(loginEntities));
            if (ListUtils.isEmpty(loginEntities)) return;
            erpEntities = loginEntities;
            String[] items = new String[loginEntities.size()];
            int select = 0;
            List<ItemsSelectType1> itemsSelectType1s=new ArrayList<>();
            for (int j = 0; j < loginEntities.size(); j++) {
                ItemsSelectType1 model=new ItemsSelectType1();
                model.setName(loginEntities.get(j).getName());
                items[j] = loginEntities.get(j).getName();
                itemsSelectType1s.add(model);
                String companyName=CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany");
                if (!StringUtil.isEmpty(companyName)){
                    if (companyName.equals(loginEntities.get(j).getName())){
                        selectId=j;
                    }
                }
            }
            if (!DialogUtils.isDialogShowing(loginERPDialog)) {
//                if (loginERPDialog != null) {
//                    select = loginERPDialog.getSelectedIndex();
//                }
//                if (select > items.length) {
//                    select = 0;
//                }
                LogUtil.d("AppInfo","phone:"+phone+"password:"+password);
                
                showPopDialog((Activity) ct,itemsSelectType1s,phone,password);
//                loginERPDialog = new MaterialDialog.Builder(ct)
//                        .title(ct.getString(R.string.user_dialog_company))
//                        .items(items)
//                        .itemsCallbackSingleChoice(select,
//                                new MaterialDialog.ListCallbackSingleChoice() {
//                                    @Override
//                                    public boolean onSelection(MaterialDialog dialog,
//                                                               View view, int which, CharSequence text) {
//                                        return selectCompanyItems(which, text, phone, password);
//                                    }
//                                }).positiveText(ct.getString(R.string.common_sure)).show();

            }
        }
    }


    public static PopupWindow popupWindow = null;
    public static int selectId=0;
    public static ItemPopListAdapter adapter;
    public static void  showPopDialog(final Activity ct, List<ItemsSelectType1> itemsSelectType1s
    , final String phone, final String password){
        erp_phone=phone;
        erp_password=password;
        View view = null;
        if (DialogUtils.isDialogShowing(mdProcessDialog)){
            mdProcessDialog.dismiss();
        }
        if (DialogUtils.isDialogShowing(popupWindow)){
            popupWindow.dismiss();
            popupWindow=null;
        }
        popupWindow=null;
        WindowManager windowManager = (WindowManager)ct. getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_dialog_list, null);
            ListView plist = view.findViewById(R.id.mList);
            List<ItemsSelectType1> datas =itemsSelectType1s;
            adapter = new ItemPopListAdapter(ct, datas);
            adapter.setSelectId(selectId);
            plist.setAdapter(adapter);
            plist.setSelection(selectId);
            Drawable drawable = ct.getResources().getDrawable(R.drawable.selector_check_items);
            plist.setSelector(drawable);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    selectId = position;
                    adapter.setSelectId(selectId);
                    adapter.notifyDataSetChanged();
                    ItemPopListAdapter.ViewHolder viewHolder= (ItemPopListAdapter.ViewHolder) view.getTag();
                    String text= viewHolder.tvItemName.getText().toString();
                   
                    selectCompanyItems(selectId, text,erp_phone, erp_password);
                }});
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth()- DensityUtil.dp2px(50), LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(ct, 0.5f);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(ct.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }
        
  

    private static boolean selectCompanyItems(int which, CharSequence text, String phone, String password) {
        LogUtil.i("AppInfo","text；"+text+"phone:"+phone+" password:"+password);
        if (!CommonUtil.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
            return true;
        }
        if (!StringUtil.isEmpty(text.toString())) {
            LoginEntity entity = erpEntities.get(which);
            erp_phone = phone;
            erp_password = password;
            mBusinessCode = entity.getBusinessCode();
            if ("ERP".equals(entity.getPlatform())) {
                erp_username = entity.getAccount();
                erp_master = entity.getMaster();
                erp_baseurl = entity.getWebsite();
                erp_company = entity.getName();
                erp_uu = String.valueOf(entity.getEnuu());
                erp_masterId = String.valueOf(entity.getMasterId());
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "userRole", "2");
                LoginERPTask(entity.getWebsite(), erp_uu, entity.getMaster(), phone, password);
                CommonUtil.clearSharedPreferences(MyApplication.getInstance(), "erp_masterlist");  //清除账套列表缓存
            }
            if ("B2B".equals(entity.getPlatform())) {
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "spaceId", String.valueOf(entity.getSpaceId()));
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "companyName", String.valueOf(entity.getName()));
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "companyEnUu", String.valueOf(entity.getEnuu()));
                //兼容uas
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "erp_uu", String.valueOf(entity.getEnuu()));
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "erp_commpany", String.valueOf(entity.getName()));
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "userRole", "3");
                LoginB2BTask(phone, password, entity.getEnuu() + "");//登录B2B
            }
            if ("个人".equals(entity.getPlatform())) {
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "userRole", "1");
                ct.startActivity(new Intent("com.modular.main.DataDownloadActivity"));
            }
        }
        return true;
    }


    @Deprecated
    private static void showDialogCompany(String json, String phone, String password) {
        List<LoginEntity> logMsg = JSON.parseArray(json, LoginEntity.class);
        erpEntities = new ArrayList<>();
        LoginEntity logB2b = null;
        if (logMsg != null && !logMsg.isEmpty()) {
            for (int i = 0; i < logMsg.size(); i++) {
                LoginEntity map = logMsg.get(i);
                if (map.getPlatform().equals("ERP")) {
                    erpEntities.add(map);
                } else if (map.getPlatform().equals("B2B")) {
                    b2b_uu = map.getAccount();
                    erp_phone = phone;
                    erp_password = password;
                    logB2b = map;
                }

            }
            if (erpEntities.size() == 1) {// erp账户的数量是多个，一个默认进入，多个让用户选进入
                LoginEntity entity = erpEntities.get(0);
                erp_phone = phone;
                erp_password = password;
                erp_username = entity.getAccount();
                erp_master = entity.getMaster();
                erp_baseurl = entity.getWebsite();
                erp_uu = String.valueOf(entity.getEnuu());
                erp_company = entity.getName();
                erp_masterId = String.valueOf(entity.getMasterId());
                LoginERPTask(entity.getWebsite(), erp_uu, entity.getMaster(), phone, password);
            } else if (erpEntities.size() > 1) {
                loginERPItemDialog(phone, password);
            }
            if (erpEntities.size() > 0) {
                hasErp = true;
            } else {
                hasErp = false;
            }
            if (logB2b != null && !hasErp) {
                List<LoginEntity.Spaces> sModel = logB2b.getSpaces();
                if (!ListUtils.isEmpty(sModel)) {
                    if (sModel.size() > 1) {
                        //选择
                        showDialogB2B(phone, password, sModel);
                    } else {
                        //个人用户
//                        mdProcessDialog.cancel();
                        CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "spaceId", String.valueOf(sModel.get(0).getId()));
                        CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyName", String.valueOf(sModel.get(0).getName()));
                        CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu", String.valueOf(sModel.get(0).getEnuu()));
                        LoginB2BTask(phone, password, sModel.get(0).getEnuu());//登录B2B
                    }
                } else {
                    CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "userRole", "1");
                    if (ct != null) {
                        ct.startActivity(new Intent("com.modular.main.MainActivity"));
                    }
                }
            }

        } else {
            mdProcessDialog.setContent(MyApplication.getInstance().getString(R.string.login_error_info));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mdProcessDialog.cancel();
                }
            }, 2000);
        }
    }

    ;

    //TODO 针对于平台的测试、正式数据库不一致
    private static String getNameByB2b(String userName, String phone) {
        if (StringUtil.isEmpty(phone)) return userName;
        LogUtil.d("phone=" + phone);
        if (phone.equals("13111110001")) {
            return "吕全明";
        } else if (phone.equals("13111110002")) {
            return "曹秋莲";
        } else if (phone.equals("13111110003")) {
            return "陈爱平";
        } else if (phone.equals("13111110004")) {
            return "CS008";
        } else if (phone.equals("13111110005")) {
            return "CS009";
        } else if (phone.equals("15012345676")) {
            return "品质";
        } else if (phone.equals("15012345678")) {
            return "邓国超移动";
        }
        return userName;
    }

    /**
     * @author LiuJie
     * @功能:比较两个日期大小
     */
    public static boolean isCheckDateTime(String DATE1, String DATE2,
                                          String format) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                System.out.println("dt1 在dt2前");
                return true;
            } else if (dt1.getTime() < dt2.getTime()) {
                System.out.println("dt1在dt2后");
                return false;
            } else {
                return true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return true;
    }

    public static void getDataFormServer(Context ct, Handler handler,
                                         String url, Map<String, String> param, int what) {
        ViewUtil.ct = ct;
        startNetThread(url, param, handler, what, null, null, "get");
    }


    /**
     * @author Administrator
     * @功能:封装网络请求 httpclient4.3
     */
    public static void httpSendRequest(
            final Context ct,
            final String url,
            final Map<String, Object> params,
            final Handler handler,
            final LinkedHashMap<String, Object> headers,
            final int what,
            Message message,
            Bundle bundle,
            final String request) {
        final Message finalMessage = (message == null ? Message.obtain() : message);
        final Bundle finalBundle = (bundle == null ? new Bundle() : bundle);
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                boolean isNetHas = NetUtils.isNetWorkConnected(ct);
                if (isNetHas) {
                    if (!(ApiUtils.getApiModel() instanceof ApiPlatform)) {
                        if (!params.containsKey("sessionId")) {
                            params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
                        }
                        if (!params.containsKey("master") || params.get("master") == null) {
                            params.put("master", CommonUtil.getMaster());
                        }
                        if (!params.containsKey("sessionUser"))
                            params.put("sessionUser", CommonUtil.getSharedPreferences(ct, "erp_username"));
                        if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_username"))
                                && headers != null) {
                            headers.put("sessionUser", CommonUtil.getSharedPreferences(ct, "erp_username"));
                        }
                    }
//                    headers.put(Constants.ANDROID_USER_AGENT_KEY, Constants.ANDROID_USER_AGENT_VALUE);
                    HttpUtil.Response result = httpSendTask(url, params, headers, request);
                    LogUtil.prinlnLongMsg("result", "http 发送请求-----------------------------------------");
                    LogUtil.prinlnLongMsg("result", "url：" + url);
                    LogUtil.prinlnLongMsg("result", "parms：" + params.toString());
                    if (headers!=null) {
                        LogUtil.prinlnLongMsg("result", "headers：" + headers.toString());
                    }
                    if (result != null) {
                        Log.i("result", "result：" + result.getStatusCode());
                        // Log.i("result", "statusCode：" +result.getResponseText());
                        LogUtil.prinlnLongMsg("result", result.getResponseText());
                        Log.i("result", "http 接收响应-----------------------------------------");
                        if (result.getStatusCode() == 200) {
                            finalBundle.putString("result", result.getResponseText());
                            finalBundle.putInt("code", 200);
                            finalMessage.setData(finalBundle);
                            finalMessage.what = what;
                            handler.sendMessage(finalMessage);
                            if (!StringUtil.isEmpty(result.getResponseText()) && JSONUtil.validate(result.getResponseText())) {
                                if (JSON.parse(result.getResponseText()) instanceof JSONObject) {
                                    if (JSON.parseObject(result.getResponseText()).containsKey("sessionId")) {
                                        CommonUtil.setSharedPreferences(ct, "sessionId", JSON.parseObject(result.getResponseText()).getString("sessionId"));
                                    }
                                }
                            }
                        } else {
                            String exception = "未知错误";
                            if (result.getStatusCode() == 500) {
                                if (JSONUtil.validate(result.getResponseText())) {
                                    if (JSON.parse(result.getResponseText()) instanceof JSONObject) {
                                        exception = JSON.parseObject(result.getResponseText()).getString("exceptionInfo");
                                    } else {
                                        exception = "500系统错误";
                                    }
                                } else {
                                    exception = "500系统错误";
                                }
                            }
                            if (result.getStatusCode() == 404) {
                                exception = "404系统错误";
                            }
                            finalBundle.putString("response", result.getResponseText());
                            finalBundle.putString("result", exception);
                            finalBundle.putInt("what", what);
                            finalBundle.putInt("code", result.getStatusCode());
                            finalMessage.setData(finalBundle);
                            finalMessage.what = Constants.APP_SOCKETIMEOUTEXCEPTION;
                            handler.sendMessage(finalMessage);
                        }
                    } else {
                        LogUtil.prinlnLongMsg("result", "result == null");
                        finalBundle.putString("result", MyApplication.getInstance().getResources().getString(R.string.networks_out));
                        finalBundle.putInt("what", what);
                        finalBundle.putInt("code", 0);
                        finalMessage.setData(finalBundle);
                        finalMessage.what = Constants.APP_SOCKETIMEOUTEXCEPTION;
                        handler.sendMessage(finalMessage);
                    }
                } else {
                    finalBundle.putString("result", MyApplication.getInstance().getResources().getString(R.string.networks_out));
                    finalBundle.putInt("what", what);
                    finalBundle.putInt("code", -1);
                    finalMessage.setData(finalBundle);
                    finalMessage.what = Constants.APP_NOTNETWORK;
                    handler.sendMessage(finalMessage);
                }
            }
        });
    }

    public static HttpUtil.Response httpSendTask(
            String url,
            Map<String, Object> params,
            LinkedHashMap<String, Object> headers,
            String method) {
        HttpUtil.Response response = null;
        try {
            if (method.equals("get")) {
                response = HttpUtil.sendGetRequest(url, params, headers, false);
            }
            if (method.equals("post")) {
                response = HttpUtil.sendPostRequest(url, params, headers, false);
            }
        } catch (Exception e) {
            if (e != null)
                Log.i("gongpengming", "httpSendTask Exception=" + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }


    /**
     * @注释：ERP自动登录
     */
    public static void AutoLoginErp(Context ct) {
        if (!StringUtil.isEmpty(CommonUtil.getAppBaseUrl(ct))) {
            ViewUtil.ct = ct;
            String url = CommonUtil.getAppBaseUrl(ct);
            String master = CommonUtil.getSharedPreferences(ct, "erp_master");
            String phone = CommonUtil.getSharedPreferences(ct, "user_phone");
            String password = CommonUtil.getSharedPreferences(ct, "user_password");
            LoginERPTask(ct, url, master, phone, password);
        }
    }

    // 登录ERP
    public static void LoginERPTask(Context ct, String url, String master, String username, String password) {
        url = url + "mobile/login.action";
        Map<String, Object> params = new HashMap<String, Object>();
        String accountToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        params.put("token", accountToken);
//        params.put("username", username);
//        params.put("password", password);
        params.put("master", master);
        httpSendRequest(ct, url, params, handler, null, Constants.SUCCESS_ERP, null, null, "post");
    }

    // 登录ERP gongpengming
    public static void LoginERPTask(Context ct, final Handler handler, final int what) {
        final String url = CommonUtil.getAppBaseUrl(ct) + "mobile/login.action";
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        String accountToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        final Map<String, String> params = new HashMap<String, String>();

        params.put("token", accountToken);
        params.put("master", master);
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                boolean isNetHas = NetUtils.isNetWorkConnected(MyApplication.getInstance());
                if (isNetHas) {
                    String result = getDataFromServer(url, params, "post");
                    if (result != null) {
                        if (JSONUtil.validate(result)) {
                            JSONObject object = JSON.parseObject(result);
                            String sessionId = object.containsKey("sessionId") ? object.getString("sessionId") : "";
                            if (!StringUtil.isEmpty(sessionId)) {
                                CommonUtil.setSharedPreferences(MyApplication.getInstance(), "sessionId", sessionId);
                            }
                        }
                        Bundle bundle = new Bundle();
                        Message message = new Message();
                        bundle.putString("result", result);
                        message.setData(bundle);
                        message.what = what;
                        handler.sendMessage(message);
                    } else {
                        Bundle bundle = new Bundle();
                        Message message = new Message();
                        bundle.putString("result", "{\n" +
                                "\"exception\":\"系统内部错误！\"\n" +
                                "}");
                        message.setData(bundle);
                        message.what = Constants.APP_SOCKETIMEOUTEXCEPTION;
                        handler.sendMessage(message);
                    }
                } else {
                    Bundle bundle = new Bundle();
                    Message message = new Message();
                    bundle.putString("result", "{\n" +
                            "\"exception\":\"" + MyApplication.getInstance().getString(R.string.common_notlinknet) + "\"\n" +
                            "}");
                    message.setData(bundle);
                    message.what = Constants.APP_SOCKETIMEOUTEXCEPTION;
                    handler.sendMessage(message);
                }
            }
        });

    }

    // 登录ERP
    public static void LoginERPTask(String url, String enuu, String master, String username, String password) {
        mdProcessDialog.setContent(MyApplication.getInstance().getString(R.string.login_progress_erp));
        progressDialog.show();
//        if (BaseConfig.isDebug()){
////            url=CommonUtil.getAppBaseUrl(MyApplication.getInstance());
////        }
        String loginToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        LogUtil.i("gong","url="+url+"mobile/login.action");
        LogUtil.i("gong","token="+loginToken);
        LogUtil.i("gong","enuu="+enuu);
        HttpRequest.getInstance().sendRequest(url,
                new HttpParams.Builder()
                        .url("mobile/login.action")
                        .addParam("token", loginToken)
                        .addParam("enuu", enuu)
                        .connectTimeOut(3)
                        .readTimeOut(3)
                        .method(Method.POST)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        Log.d("erp登录成功", o.toString());
                        progressDialog.dismiss();
                        if (o != null) {
                            loginErpSuccess(o.toString());
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        Log.e("erp登录失败", "登录失败:"+failStr);
                        progressDialog.dismiss();
                        ToastMessage(MyApplication.getInstance(),"登录失败:"+failStr);
                        mdProcessDialog.setContent("登录失败");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mdProcessDialog.dismiss();
                            }
                        }, 1000);
//                        ToastMessage(MyApplication.getInstance(), failStr, Style.holoRedLight, 2000);
                    }
                });
    }


    public static void clearAccount(Context ct) {
        CommonUtil.clearSharedPreferences(ct, "erp_baseurl");
        CommonUtil.clearSharedPreferences(ct, "erp_master");
        CommonUtil.clearSharedPreferences(ct, "erp_commpany");
        CommonUtil.clearSharedPreferences(ct, "erp_uu");
        CommonUtil.clearSharedPreferences(ct, "erp_masterId");
        CommonUtil.clearSharedPreferences(ct, "erp_login");
        CommonUtil.clearSharedPreferences(ct, "b2b_login");
        CommonUtil.clearSharedPreferences(ct, "b2b_uu");
        CommonUtil.clearSharedPreferences(ct, "Master_ch");
        CommonUtil.clearSharedPreferences(ct, "erp_emname");
        CommonUtil.clearSharedPreferences(ct, "userRole");
        CommonUtil.clearSharedPreferences(ct, Constants.B2B_SESSION_CACHE);
        CommonUtil.clearSharedPreferences(ct, Constants.B2B_UID_CACHE);
        CommonUtil.clearSharedPreferences(ct, Constants.CACHE.EN_BUSINESS_CODE);
        CommonUtil.clearSharedPreferences(ct, Constants.CACHE.B2B_BUSINESS_ENUU);
    }
}
