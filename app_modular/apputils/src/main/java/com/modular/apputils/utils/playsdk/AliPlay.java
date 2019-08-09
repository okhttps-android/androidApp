package com.modular.apputils.utils.playsdk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.common.LogUtil;
import com.common.thread.ThreadPool;
import com.modular.apputils.listener.OnPlayListener;

import java.util.Map;

/**
 * Created by Bitlike on 2017/11/13.
 */

public class AliPlay extends Handler {
    private final int SDK_PAY_FLAG = 1;

    private static AliPlay api;

    public static AliPlay api() {
        if (api == null) {
            synchronized (AliPlay.class) {
                if (api == null) {
                    api = new AliPlay();
                }
            }
        }
        return api;
    }

    private AliPlay() {
        super(Looper.getMainLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg != null && msg.what == SDK_PAY_FLAG) {
            try {
                handerPlayResult(msg);
            } catch (Exception e) {
                if (e != null) {
                    LogUtil.i("AliPlay Exception=" + e.getMessage());
                }
            }
        }
    }

    private void handerPlayResult(Message msg) throws Exception {
        Bundle bundle = msg.getData();
        if (bundle == null) return;
        OnPlayListener onPlayListener = (OnPlayListener) bundle.getSerializable("onPlayListener");
        if (onPlayListener == null) return;
        PayResult payResult = new PayResult((Map<String, String>) msg.obj);
            /*对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。*/
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
        LogUtil.i("resultInfo=" + resultInfo);
        String resultStatus = payResult.getResultStatus();
        // 判断resultStatus 为9000则代表支付成功
        if (TextUtils.equals(resultStatus, "9000")) {
            // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
            if (onPlayListener != null) {
                onPlayListener.onSuccess(resultStatus, resultInfo);
            }
        } else {
            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
            if (onPlayListener != null) {
                onPlayListener.onFailure(resultStatus, resultInfo);
            }
        }
    }


    public void alipay(final Activity ct, final String orderInfo, final OnPlayListener onPlayListener) {
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    payThread(ct, orderInfo, onPlayListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void payThread(Activity ct, String orderInfo, OnPlayListener onPlayListener) throws Exception {
        PayTask alipay = new PayTask(ct);
        Map<String, String> result = alipay.payV2(orderInfo, true);
        LogUtil.i(result.toString());
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putSerializable("onPlayListener", onPlayListener);
        msg.setData(bundle);
        msg.what = SDK_PAY_FLAG;
        msg.obj = result;
        sendMessage(msg);
    }
}
