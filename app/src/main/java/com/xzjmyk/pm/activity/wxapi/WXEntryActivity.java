package com.xzjmyk.pm.activity.wxapi;

import android.os.Bundle;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity implements IWXAPIEventHandler {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onReq(BaseReq req) {
        super.onReq(req);
    }

    @Override
    public void onResp(BaseResp resp) {
        super.onResp(resp);
        /*try {
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK: {
                    Toast.makeText(this, "微信分享成功", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();*/
    }

}