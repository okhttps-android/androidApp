package com.modular.apputils.utils.playsdk;


import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.modular.apputils.listener.OnPlayListener;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by Bitlike on 2017/12/25.
 */
public class WxPlay {
    private IWXAPI wxApi;
    private static WxPlay api;
    public static WxPlay api() {
        if (api == null) {
            synchronized (WxPlay.class) {
                if (api == null) {
                    api = new WxPlay();
                }
            }
        }
        return api;
    }
        private WxPlay() {
    }

    public void wxPay(Context context, String orderInfo, OnPlayListener onPlayListener) {
        wxApi = WXAPIFactory.createWXAPI(context, Constants.WXPAY_APPID);
//      orderInfo = orderInfo.replaceAll("\\\\", "");
        LogUtil.d("wxpayorderinfo", orderInfo);
        if (JSONUtil.validate(orderInfo)) {
            JSONObject orderObject = JSON.parseObject(orderInfo);
            PayReq request = new PayReq();
            request.appId = Constants.WXPAY_APPID;
            request.partnerId = JSONUtil.getText(orderObject, "partnerid");
            request.prepayId = JSONUtil.getText(orderObject, "prepayid");
            request.packageValue = JSONUtil.getText(orderObject, "package");
            request.nonceStr = JSONUtil.getText(orderObject, "noncestr");
            request.timeStamp = JSONUtil.getText(orderObject, "timestamp");
            request.sign = JSONUtil.getText(orderObject, "sign");

           boolean result= wxApi.sendReq(request);
           /*if (result){
               onPlayListener.onSuccess("200","订单支付成功");
           }*/
        } else {
            onPlayListener.onFailure("500", "订单信息获取异常");
        }

    }

}
