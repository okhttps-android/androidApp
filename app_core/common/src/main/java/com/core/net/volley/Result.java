package com.core.net.volley;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.hmac.Md5Util;
import com.common.system.SystemUtil;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.dao.UserDao;
import com.core.model.LoginRegisterResult;
import com.core.model.User;
import com.core.utils.ToastUtil;
import com.core.utils.helper.LoginHelper;
import com.core.utils.sp.UserSp;
import com.uas.applocation.UasLocationHelper;

import java.util.HashMap;

public class Result {
    /**
     * 通用的Http Result VerifyCode http 请求返回的结果码 <br/>
     * 0表示一般性错误</br> 1-100表示成功</br> 大于100000表示一些详细的错误</br>
     */
    public final static int CODE_ERROE = 0;// 未知的错误 或者系统内部错误
    public final static int CODE_SUCCESS = 1;// 正确的Http请求返回状态码
    public final static int CODE_ARGUMENT_ERROR1 = 1010101;// 请求参数验证失败，缺少必填参数或参数错误
    public final static int CODE_ARGUMENT_ERROR2 = 1010102;// 缺少请求参数：%1$s

    public final static int CODE_INTERNAL_ERROR = 1020101;// 接口内部异常
    public final static int CODE_NO_TOKEN = 1030101;// 缺少访问令牌
    public final static int CODE_TOKEN_ERROR = 1030102;// 访问令牌过期或无效

    /* 登陆接口的Http Result VerifyCode */
    public final static int CODE_ACCOUNT_INEXISTENCE = 1040101;// 帐号不存在
    public final static int CODE_ACCOUNT_ERROE = 1040102;// 帐号或密码错误

    public static final String RESULT_CODE = "resultCode";
    public static final String RESULT_MSG = "resultMsg";
    public static final String DATA = "data";

    private int resultCode;
    private String resultMsg;
    //private JSONObject jsonObject;//返回响应正文数据
    private String resultData;//响应正文

    public static boolean defaultParser(Context context, Result result, boolean showToast) {
        if (result == null) {
            if (showToast) {
                Toast.makeText(context, context.getString(R.string.data_exception), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        if (result.resultCode == CODE_SUCCESS) {// 成功
            return true;
        } else if (result.resultCode == CODE_NO_TOKEN) {// 缺少参数Token
            //TODO 发出异常登录的广播
//			LoginHelper.broadcastToken(context);
//			LoginHelper.broadcastConflict(context);
            if (showToast)
                showResultToast(context, result);
            return false;
        } else if (result.resultCode == CODE_TOKEN_ERROR) {// Token过期或错误
            //TODO 发出异常登录的广播
            loginIM(context);
//			LoginHelper.broadcastToken(context);
//			LoginHelper.broadcastConflict(context);
            if (showToast)
                showResultToast(context, result);
            return false;
        } else if (result.resultCode == CODE_INTERNAL_ERROR) {//接口内部异常
           // Crouton.makeText(context, R.string.service_start_failed, 2000);
            ToastUtil.showToast(context,R.string.service_start_failed);
            return false;
        } else {
            if (showToast) {
                showResultToast(context, result);
            }
            return false;
        }
    }

    //当发现taken过期时候重新登陆
    public static void loginIM(final Context context) {
        String userId = UserSp.getInstance(context).getUserId("");
        User user = UserDao.getInstance().getUserByUserId(userId);
        final String phoneNumber = user.getTelephone();
        final String password = user.getPassword();
        AppConfig mConfig = MyApplication.getInstance().getConfig();
        // 加密之后的密码
        final String requestTag = "login";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("telephone", Md5Util.toMD5(phoneNumber));// 账号登陆的时候需要MD5加密，服务器需求
        params.put("password", password);
        // 附加信息
        params.put("model", SystemUtil.getModel());
        params.put("osVersion", SystemUtil.getOsVersion());
        params.put("serial", SystemUtil.getDeviceId(context));
        // 地址信息
        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        final StringJsonObjectRequest<LoginRegisterResult> request = new StringJsonObjectRequest<LoginRegisterResult>(mConfig.USER_LOGIN,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                    }
                }, new StringJsonObjectRequest.Listener<LoginRegisterResult>() {
            @Override
            public void onResponse(ObjectResult<LoginRegisterResult> result) {
                if (result == null) {
                    return;
                }
                boolean success = false;
                if (result.getResultCode() == Result.CODE_SUCCESS) {
                    success = LoginHelper.setLoginUser(context, phoneNumber, password, result);// 设置登陆用户信息
                }
                if (success) {// 登陆IM成功

                } else {// 登录失败

                }
            }
        }, LoginRegisterResult.class, params);
        request.setTag(requestTag);
        MyApplication.getInstance().getFastVolley().addDefaultRequest("Result", request);
    }

    private static void showResultToast(Context context, Result result) {
        try {
            if (TextUtils.isEmpty(result.resultMsg)) {
                Toast.makeText(context, context.getString(R.string.data_exception), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, result.resultMsg, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
    //	public JSONObject getJsonObject() {
//		return jsonObject;
//	}
//
//	public void setJsonObject(JSONObject jsonObject) {
//		this.jsonObject = jsonObject;
//	}

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    @Override
    public String toString() {
        return JSON.toJSON(this).toString();
    }

}
