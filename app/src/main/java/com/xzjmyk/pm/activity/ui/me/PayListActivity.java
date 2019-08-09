package com.xzjmyk.pm.activity.ui.me;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.config.BaseConfig;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.core.widget.MyListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnPlayListener;
import com.modular.apputils.utils.playsdk.AliPlay;
import com.modular.apputils.utils.playsdk.WxPlay;
import com.xzjmyk.pm.activity.R;

public class PayListActivity extends BaseActivity implements OnPlayListener {

    private Button pay_btn;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_list);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(type){
                    case 1:
//                        payName="微信";
                        toWxPay();
                        break;
                    case 2:
//                        payName="支付宝";
                        toAliPay();
                        break;
                    case 3:
//                        payName="银联";
                        break;
                    default:break;
                }
            }
        });
    }

    /**
     * 支付宝
     */
    private void toAliPay() {
        new HttpClient.Builder("http://qq784602719.imwork.net:43580/")
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("fruits/alipay/createOrder.do")
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        Log.i(TAG, "Success:" + o.toString());
                        String message = o.toString();
                        message = JSON.parseObject(o.toString()).getString("data");
                        Log.i(TAG, "message:" + message);
                        AliPlay.api().alipay(PayListActivity.this, message, PayListActivity.this);
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    /**
     * 微信支付
     */
    private void toWxPay() {
                new HttpClient.Builder("http://nf20718343.iask.in:15161")
                        .isDebug(BaseConfig.isDebug())
                        .build()
                        .Api()
                        .send(new HttpClient.Builder()
                        .url("/wxpay/appPay")
                                .add("userid", MyApplication.getInstance().getLoginUserId())
                                .add("totalFee",0.01)
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        Log.i(TAG, "Success:" + o.toString());
                        String message = o.toString();
                        JSONObject data = JSON.parseObject(JSON.parseObject(o.toString()).getString("msg"));
                        message = data.toJSONString();
                        Log.i(TAG, "message:" + message);
                        WxPlay.api().wxPay(ct, message, PayListActivity.this);
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    private void initView() {
        FormEditText money_et = findViewById(R.id.money_et);
        pay_btn = findViewById(R.id.pay_btn);
        MyListView orders_lv = findViewById(R.id.orders_lv);

        type = getIntent().getIntExtra("paytype", -1);
        String payName = null;
        switch(type){
            case 1:
                payName="微信";
                break;
            case 2:
                payName="支付宝";
                break;
            case 3:
                payName="银联";
                break;
            default:break;
        }
        pay_btn.setText(payName);
    }
    private void initData() {

    }

    @Override
    public void onSuccess(String resultStatus, String resultInfo) {
        ToastUtil.showToast(ct, "支付成功");
    }

    @Override
    public void onFailure(String resultStatus, String resultInfo) {
        ToastUtil.showToast(ct, "支付失败");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
