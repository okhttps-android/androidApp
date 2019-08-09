/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.excep.utils;


import android.content.Context;
import android.text.TextUtils;

import com.baidu.aip.FaceEnvironment;
import com.baidu.aip.FaceSDKManager;
import com.baidu.idl.facesdk.FaceTracker;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;


public class FaceConfig {


    // 为了apiKey,secretKey为您调用百度人脸在线接口的，如注册，识别等。
    // 为了的安全，建议放在您的服务端，端把人脸传给服务器，在服务端端进行人脸注册、识别放在示例里面是为了您快速看到效果
    public static String apiKey = "8B4k81ViOG3XWAoG4dDgSB2I";
    public static String secretKey = "hryH0Lhmmt0yvGSXTveTwcMIRCA7rfIK";
    public static String licenseID = "UUFaceID-face-android";
    public static String licenseFileName = "idl-license.face-android";
    public static String accessToken;

    public static void initFace(Context context) {
        FaceSDKManager.getInstance().init(context, licenseID, licenseFileName);
        FaceTracker tracker = FaceSDKManager.getInstance().getFaceTracker(context);  //.getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整

        // 模糊度范围 (0-1) 推荐小于0.7
        tracker.set_blur_thr(FaceEnvironment.VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        tracker.set_illum_thr(FaceEnvironment.VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        tracker.set_cropFaceSize(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        tracker.set_eulur_angle_thr(FaceEnvironment.VALUE_HEAD_PITCH, FaceEnvironment.VALUE_HEAD_ROLL,
                FaceEnvironment.VALUE_HEAD_YAW);

        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        tracker.set_min_face_size(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        //
        tracker.set_notFace_thr(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        tracker.set_occlu_thr(FaceEnvironment.VALUE_OCCLUSION);
        // 是否进行质量检测
        tracker.set_isCheckQuality(true);
        // 是否进行活体校验
        tracker.set_isVerifyLive(false);


    }


    public static void loadToken(FaceTokenListener mFaceTokenListener) {
        if (mFaceTokenListener != null) {
            if (!TextUtils.isEmpty(accessToken)) {
                mFaceTokenListener.callBack(accessToken);
            } else {
                initToken(mFaceTokenListener);
            }
        }
    }

    public static void initToken(final FaceTokenListener mFaceTokenListener) {
        HttpClient httpClient = new HttpClient.Builder("https://aip.baidubce.com/").isDebug(BaseConfig.isDebug())
                .connectTimeout(5000)
                .readTimeout(5000).build();
        httpClient.Api().get(new HttpClient.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", apiKey)
                .method(Method.GET)
                .url("oauth/2.0/token")
                .add("client_secret", secretKey).build(), new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.i("gong", "initToken onResponse=" + o.toString());
                try {
                    String message = o.toString();
                    accessToken = JSONUtil.getText(message, "access_token");
                    if (mFaceTokenListener != null) {
                        mFaceTokenListener.callBack(accessToken);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Object t) {
                LogUtil.i("gong", "initToken onFailure=" + t.toString());
            }
        }));
    }


    public interface FaceTokenListener {
        void callBack(String accessToken);
    }

}
