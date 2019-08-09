package com.baidu.aip.excep.activity;

import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.excep.model.FaceVerify;
import com.baidu.aip.excep.utils.FaceConfig;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.model.User;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.widget.VeriftyDialog;

public class RealTimeDetectFaceActivty extends DetectLoginActivity {
    private final int MAX_DETECT_COUNT = 1;


    @Override
    protected String getBaseUrl() {
        return "https://aip.baidubce.com/";
    }

    @Override
    public void uploadData(final String faceBase64) {
        final String master = CommonUtil.getEnuu(ct);
        final String imid = MyApplication.getInstance().getLoginUserId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                verify(faceBase64, master, imid, true);
            }
        });
    }


    private void showRegisterDialog(final String faceBase64, final String group_id_list, final String user_id) {
        new VeriftyDialog.Builder(this)
                .setCanceledOnTouchOutside(false)
                .setContent("您未录入人脸照片,点击\"确认\"录入系统")
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                        if (clickSure) {
                            String company = CommonUtil.getSharedPreferences(ct, "erp_commpany");
                            String master = CommonUtil.getSharedPreferences(ct, "Master_ch");
                            String name = CommonUtil.getName();
                            User user = MyApplication.getInstance().mLoginUser;
                            String phone = null;
                            if (user != null) {
                                phone = user.getTelephone();
                            }
                            String userInfo = company + "_" + master + "_" + phone + "_" + name;//公司名_帐套名_电话_姓名
                            register(faceBase64, group_id_list, user_id, userInfo);
                        } else {
                            setResult(0);
                            finish();
                        }
                    }
                });

    }

    //验证和注册错误
    private void showErrorDialog(String result) {
        String message = "";
        if (result.contains("liveness check fail")) {
            message = "不能拿照片骗我哦";
        } else {
            message = result;
        }
        mUploading = true;
        new VeriftyDialog.Builder(this)
                .setCanceledOnTouchOutside(false)
                .setContent(message)
                .setSureText("再试一次")
                .setShowCancel(true)
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                        if (clickSure) {
                            reBrushes();
                        } else {
                            finish();
                        }
                    }
                });
    }

    /**
     * 校验身份
     *
     * @param faceBase64    脸部信息
     * @param group_id_list 组Id
     * @param user_id       用户Id
     * @param isFirst       是否是第一次验证，如果是第一次，验证失败的话重新验证
     */
    private void verify(final String faceBase64, final String group_id_list, final String user_id, final boolean isFirst) {
        if (isFirst) {
            showProgress();
        }
        LogUtil.i("gong", "isMain" + (Looper.getMainLooper() == Looper.myLooper()));
        mUploading = true;
        FaceConfig.loadToken(new FaceConfig.FaceTokenListener() {
            @Override
            public void callBack(String accessToken) {
                LogUtil.i("gong", "accessToken=" + accessToken);
                httpClient.Api().send(new HttpClient.Builder()
                        .url("rest/2.0/face/v3/search")
                        .add("access_token", accessToken)
                        .header("Content-Type", "application/json")
                        .add("image", faceBase64)
                        .add("image_type", "BASE64")
                        .add("liveness_control", "NORMAL")
                        .add("user_id", user_id)
                        .isDebug(true)
                        .add("group_id_list", group_id_list)
                        .method(Method.POST).build(), new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        try {
                            handleDetectResult(o.toString(), faceBase64, group_id_list, user_id, isFirst);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dismissProgress();
                    }

                    @Override
                    public void onFailure(Object t) {
                        LogUtil.i("gong", "onFailure=" + t.toString());
                        dismissProgress();
                        detectError();
                    }
                }));
            }
        });
    }

    private void register(final String faceBase64, final String group_id, final String user_id, final String user_info) {
        showProgress();
        mUploading = true;
        FaceConfig.loadToken(new FaceConfig.FaceTokenListener() {
            @Override
            public void callBack(String accessToken) {
                LogUtil.i("gong", "accessToken=" + accessToken);
                httpClient.Api().send(new HttpClient.Builder()
                        .url("rest/2.0/face/v3/faceset/user/add")
                        .add("access_token", accessToken)
                        .header("Content-Type", "application/json")
                        .add("image", faceBase64)
                        .add("image_type", "BASE64")
                        .add("quality_control", "NORMAL")
                        .add("liveness_control", "NORMAL")
                        .add("user_id", user_id)
                        .add("user_info", user_info)
                        .isDebug(true)
                        .add("group_id", group_id)
                        .method(Method.POST).build(), new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        LogUtil.i("gong", "verify onResponse=" + o.toString());
                        try {
                            JSONObject object = JSON.parseObject(o.toString());
                            String error_msg = JSONUtil.getText(object, "error_msg");
                            if (TextUtils.isEmpty(error_msg) || error_msg.equals("SUCCESS")) {
                                faceRegisterErp();
                                okAndEnd(faceBase64);
                            } else {
                                showErrorDialog(error_msg);
                                return;
                            }
                        } catch (Exception e) {
                        }
                        reBrushes();
                        dismissProgress();
                    }

                    @Override
                    public void onFailure(Object t) {
                        dismissProgress();
                        LogUtil.i("gong", "verify onFailure=" + t.toString());
                    }
                }));
            }
        });
    }

    /**
     * 向erp注册人脸
     */
    private void faceRegisterErp() {
        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(this),
                new HttpParams.Builder()
                        .url("mobile/updateUploadPictureSign.action")
                        .method(Method.POST)
                        .addParam("master", CommonUtil.getMaster())
                        .addParam("em_imid", MyApplication.getInstance().mLoginUser.getUserId())
                        .addParam("emcode", CommonUtil.getEmcode())
                        .addParam("em_uploadsign", 1)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {

                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {

                    }
                });
    }

    /**
     * 处理人脸扫描
     *
     * @param messgae
     * @param isFirst
     * @throws
     */
    private void handleDetectResult(String messgae, final String faceBase64, final String group_id_list, final String user_id, boolean isFirst) throws Exception {
        LogUtil.i("gong", "onResponse=" + messgae);
        JSONObject object = JSON.parseObject(messgae);
        String error_msg = JSONUtil.getText(object, "error_msg");
        int error_code = JSONUtil.getInt(object, "error_code");
        if (TextUtils.isEmpty(error_msg) || error_msg.equals("SUCCESS")) {
            JSONObject result = JSONUtil.getJSONObject(object, "result");
            JSONArray user_list = JSONUtil.getJSONArray(result, "user_list");
            if (ListUtils.isEmpty(user_list)) {
                //验证成功，但是没有注册时候
                showRegisterDialog(faceBase64, group_id_list, user_id);
            } else {
                FaceVerify mFaceVerify = new FaceVerify();
                for (int i = 0; i < user_list.size(); i++) {
                    JSONObject userObject = user_list.getJSONObject(i);
                    float score = JSONUtil.getFloat(userObject, "score");
                    if (mFaceVerify.getScore() < score) {
                        mFaceVerify.setScore(score);
                        mFaceVerify.setUserId(JSONUtil.getText(userObject, "user_id"));
                        mFaceVerify.setUserInfo(JSONUtil.getText(userObject, "user_info"));
                        mFaceVerify.setGroupId(JSONUtil.getText(userObject, "group_id"));
                        if (mFaceVerify.isPass()) {
                            break;
                        }
                    }
                }
                if (mFaceVerify.isPass()) {
                    okAndEnd(faceBase64);
                } else {
                    detectError();
                }
            }
        } else if (222207 == error_code) {
//            if (isFirst) {
//                verify(faceBase64, group_id_list, user_id, false);
//            } else {
//                Log.e("facesignresult","222207");
            HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(this),
                    new HttpParams.Builder()
                            .url("mobile/getUploadPictureSign.action")
                            .method(Method.POST)
                            .addParam("master", CommonUtil.getMaster())
                            .addParam("em_imid", MyApplication.getInstance().mLoginUser.getUserId())
                            .addParam("emcode", CommonUtil.getEmcode())
                            .build(), new HttpCallback() {
                        @Override
                        public void onSuccess(int flag, Object o) throws Exception {
                            dismissProgress();
                            try {
                                String result = o.toString();
                                JSONObject resultObject = JSON.parseObject(result);
                                int uploadsign = JSONUtil.getInt(resultObject, "em_uploadsign");
                                if (uploadsign == 1) {
                                    detectError();
                                } else {
                                    showRegisterDialog(faceBase64, group_id_list, user_id);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                detectError();
                            }
                        }

                        @Override
                        public void onFail(int flag, String failStr) throws Exception {
                            dismissProgress();
                            detectError();
                        }
                    });
//            }
        } else {
            detectError();
        }
    }

    /**
     * 操作成功并退出
     *
     * @param faceBase64
     */
    public void okAndEnd(String faceBase64) {
        Intent intent = new Intent();
        intent.putExtra(Constants.Intents.FACE_SIGN_BASE64, faceBase64);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 人脸识别失败
     */
    public void detectError() {
        if (mDetectCount >= MAX_DETECT_COUNT) {
            showErrorDialog("抱歉，没认出你哦");
            mDetectCount = 0;
        } else {
            reBrushes();
        }
    }


}
