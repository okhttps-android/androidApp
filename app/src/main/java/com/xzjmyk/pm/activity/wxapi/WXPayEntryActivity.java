package com.xzjmyk.pm.activity.wxapi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.utils.ToastUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnPlayListener;
import com.modular.apputils.utils.playsdk.AliPlay;
import com.modular.apputils.utils.playsdk.WxPlay;
import com.modular.apputils.widget.VeriftyDialog;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.uas.appme.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @desc:微信支付回调，支付测试类
 * @author：Arison on 2018/5/2
 */
public class WXPayEntryActivity extends AppCompatActivity implements View.OnClickListener, OnPlayListener, IWXAPIEventHandler {
    private IWXAPI api;
    private static final String TAG = "PayTestActivity";
    // String baseUrl = "http://nf20718343.iask.in:15161/";
    String baseUrl = "http://qq784602719.imwork.net:43580/";

    private ListView lvWxPay;
    private ListView lvAliPay;


    PayTestAdapter wxAdapter;
    PayTestAdapter aliAdapter;

    List<Order> wxDatas = new ArrayList<>();
    List<Order> aliDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pay_test);
//        setTitle("支付测试");
//        findViewById(R.id.btn_wxPay).setOnClickListener(this);
//        findViewById(R.id.btn_wxRefund).setOnClickListener(this);
//        lvWxPay = findViewById(R.id.lv_wxPay);
//        findViewById(R.id.btn_aliPay).setOnClickListener(this);
//        findViewById(R.id.btn_aliRefund).setOnClickListener(this);
//        lvAliPay = findViewById(R.id.lv_aliPay);
//        wxAdapter = new PayTestAdapter(this, wxDatas);
//        aliAdapter = new PayTestAdapter(this, aliDatas);
//        lvWxPay.setAdapter(wxAdapter);
//        lvAliPay.setAdapter(aliAdapter);
//        initEvent();
        initData();
    }

    private void initEvent() {
        lvWxPay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PayTestAdapter.ViewHolder holder = (PayTestAdapter.ViewHolder) view.getTag();
                LogUtil.d(TAG, "list action:" + holder.btnAction.getText().toString());
                holder.btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LogUtil.d(TAG, "action:" + holder.btnAction.getText().toString());
                        if ("支付".equals(holder.btnAction.getText().toString())) {
                            ToastUtil.showToast(WXPayEntryActivity.this, "支付");
                            wxPay(holder.orderID.getText().toString());
                        } else if ("退款".equals(holder.btnAction.getText().toString())) {
                            ToastUtil.showToast(WXPayEntryActivity.this, "退款");
                            wxRefund(holder.orderID.getText().toString());
                        }
                    }
                });

            }
        });

        lvAliPay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final PayTestAdapter.ViewHolder holder = (PayTestAdapter.ViewHolder) view.getTag();
                LogUtil.d(TAG, "list action:" + holder.btnAction.getText().toString());
                holder.btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LogUtil.d(TAG, "action:" + holder.btnAction.getText().toString());
                        if ("支付".equals(holder.btnAction.getText().toString())) {
                            ToastUtil.showToast(WXPayEntryActivity.this, "支付");
                            aliPay(holder.orderID.getText().toString());
                        } else if ("退款".equals(holder.btnAction.getText().toString())) {
                            ToastUtil.showToast(WXPayEntryActivity.this, "退款");
                            aliRefund(holder.orderID.getText().toString());
                        }
                    }
                });
            }
        });
    }

    private void initData() {
        api = WXAPIFactory.createWXAPI(this, Constants.WXPAY_APPID, false);
        api.handleIntent(getIntent(), this);
//        getWxOrders();
//        getAliPayOrders();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api = WXAPIFactory.createWXAPI(this, Constants.WXPAY_APPID, false);
        api.handleIntent(intent, this);
    }

    private void getAliPayOrders() {
        new HttpClient.Builder(baseUrl)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("alipay/orderquery")
                        .add("userId", MyApplication.getInstance().mLoginUser.getTelephone())
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        if (!ListUtils.isEmpty(aliDatas)) {
                            aliDatas.clear();
                        }
                        LogUtil.i(TAG, "支付宝list:" + o.toString());
                        JSONArray jsonArray = JSON.parseArray(o.toString());
                        for (int i = 0; i < jsonArray.size(); i++) {
                            Order model = new Order();
                            JSONObject object = jsonArray.getJSONObject(i);
                            model.setFee(String.valueOf(object.getDouble("fee")));
                            model.setOutTradeNo(object.getString("outTradeNo"));
                            model.setTradeState(object.getString("tradeState"));
                            model.setTransactionId(object.getString("trade_no"));
                            model.setTimeStart(String.valueOf(object.getLong("timeStart")));
                            aliDatas.add(model);
                        }
                        if (!ListUtils.isEmpty(aliDatas)) {
                            aliAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    private void getWxOrders() {
        new HttpClient.Builder(baseUrl)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("wxpay/wxAppQuery")
                        .add("userid", MyApplication.getInstance().mLoginUser.getTelephone())
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        if (!ListUtils.isEmpty(wxDatas)) {
                            wxDatas.clear();
                        }
                        LogUtil.i(TAG, "微信list:" + o.toString());
                        JSONArray jsonArray = JSON.parseArray(o.toString());
                        for (int i = 0; i < jsonArray.size(); i++) {
                            Order model = new Order();
                            JSONObject object = jsonArray.getJSONObject(i);
                            model.setFee(String.valueOf(object.getDouble("fee")));
                            model.setOutTradeNo(object.getString("outTradeNo"));
                            model.setTradeState(object.getString("tradeState"));
                            model.setTransactionId(object.getString("transactionId"));
                            model.setTimeStart(String.valueOf(object.getLong("timeStart")));
                            wxDatas.add(model);
                        }
                        if (!ListUtils.isEmpty(wxDatas)) {
                            wxAdapter.notifyDataSetChanged();
                        }
                        ToastUtil.showToast(WXPayEntryActivity.this, "微信订单列表数据已刷新！");
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_wxPay) {
            wxPay(null);
        } else if (i == R.id.btn_wxRefund) {
            // wxRefund();
        } else if (i == R.id.btn_aliPay) {
            aliPay(null);
        } else if (i == R.id.btn_aliRefund) {
            // aliRefund();
        }
    }

    private void aliRefund(String outTradeNo) {
        LogUtil.d(TAG, "outTradeNo:" + outTradeNo);
        new HttpClient.Builder(baseUrl)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("alipay/tradeRefund")
                        .add("outTradeNo", outTradeNo)
                        .add("refundAmount", "0.01")
                        .add("fee", "0.01")
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        LogUtil.i(TAG, "Success:" + o.toString());

                        getAliPayOrders();

                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    private void aliPay(String orderId) {
        LogUtil.d(TAG, "orderId:" + orderId);
        new HttpClient.Builder(baseUrl)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("alipay/appPay")
                        .add("userId", MyApplication.getInstance().mLoginUser.getTelephone())
                        .add("totalAmount", "0.01")
                        .add("out_trade_no", orderId)
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        LogUtil.i(TAG, "Success:" + o.toString());
                        String message = o.toString();
                        message = JSON.parseObject(o.toString()).getString("data");
                        AliPlay.api().alipay(WXPayEntryActivity.this, message, WXPayEntryActivity.this);
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    private void wxRefund(String outTradeNo) {
        LogUtil.d(TAG, "退款操作---商户订单号：" + outTradeNo);
        new HttpClient.Builder(baseUrl)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("wxpay/appRefund")
                        .add("outTradeNo", outTradeNo)
                        .add("refund_fee", "0.01")
                        .add("fee", "0.01")
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        Log.i(TAG, "Success:" + o.toString());
                        getWxOrders();
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    private void wxPay(String orderId) {
        LogUtil.d(TAG, "userid:" + MyApplication.getInstance().mLoginUser.getTelephone() + " orderId:" + orderId);
        new HttpClient.Builder(baseUrl)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("wxpay/appPay")
                        .add("userid", MyApplication.getInstance().mLoginUser.getTelephone())
                        .add("fee", "0.01")
                        .add("out_trade_no", orderId)
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        Log.i(TAG, "Success:" + o.toString());
                        String message = o.toString();
                        JSONObject data = JSON.parseObject(JSON.parseObject(o.toString()).getString("data"));
                        message = data.toJSONString();
                        Log.i(TAG, "message:" + message);
                        WxPlay.api().wxPay(WXPayEntryActivity.this, message, WXPayEntryActivity.this);
                    }

                    @Override
                    public void onFailure(Object t) {
                        Log.i(TAG, "Failure:" + t.toString());
                    }
                }));
    }

    @Override
    public void onSuccess(String resultStatus, String resultInfo) {
        LogUtil.d(TAG, "支付宝支付成功！");
        // getAliPayOrders();

    }

    @Override
    public void onFailure(String resultStatus, String resultInfo) {
        //getAliPayOrders();
    }


    //
    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        LogUtil.d(TAG, "onPayFinish, errCode = " + baseResp.errCode);
        // getWxOrders();
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            if (baseResp.errCode == BaseResp.ErrCode.ERR_OK) {
                Toast.makeText(this, R.string.str_error_wechat_pay_success, Toast.LENGTH_LONG).show();
                new VeriftyDialog.Builder(this)
                        .setCanceledOnTouchOutside(false)
                        .setContent("感谢您的爱心!")
                        .setShowCancel(false)
                        .build(new VeriftyDialog.OnDialogClickListener() {
                            @Override
                            public void result(boolean clickSure) {
                                finish();
                            }
                        });
            } else if (baseResp.errCode == BaseResp.ErrCode.ERR_COMM) {
                Toast.makeText(this, R.string.str_error_wechat_pay_fail, Toast.LENGTH_LONG).show();
                finish();
            } else if (baseResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                Toast.makeText(this, R.string.str_error_wechat_pay_cancel, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    public class Order {
        String timeExpire;
        String timeStart;
        String outRefundNo;
        String totalFee;
        String refundSuccessTime;
        String outTradeNo;
        String fee;
        String transactionId;
        String tradeState;

        public String getTimeExpire() {
            return timeExpire;
        }

        public void setTimeExpire(String timeExpire) {
            this.timeExpire = timeExpire;
        }

        public String getTimeStart() {
            return timeStart;
        }

        public void setTimeStart(String timeStart) {
            this.timeStart = timeStart;
        }

        public String getOutRefundNo() {
            return outRefundNo;
        }

        public void setOutRefundNo(String outRefundNo) {
            this.outRefundNo = outRefundNo;
        }

        public String getTotalFee() {
            return totalFee;
        }

        public void setTotalFee(String totalFee) {
            this.totalFee = totalFee;
        }

        public String getRefundSuccessTime() {
            return refundSuccessTime;
        }

        public void setRefundSuccessTime(String refundSuccessTime) {
            this.refundSuccessTime = refundSuccessTime;
        }

        public String getOutTradeNo() {
            return outTradeNo;
        }

        public void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getTradeState() {
            return tradeState;
        }

        public void setTradeState(String tradeState) {
            this.tradeState = tradeState;
        }
        //           "timeExpire":1525227228000,
//                   "timeStart":1525226959000,
//                   "outRefundNo":"1525231745951",
//                   "totalFee":1,
//                   "refundSuccessTime":1525231745000,
//                   "outTradeNo":"1525226959568",
//                   "fee":0.01,
//                   "transactionId":"4200000132201805020747499624",
//                   "tradeState":"2"
    }

    public class PayTestAdapter extends BaseAdapter {

        private List<Order> objects = new ArrayList<Order>();

        private Context context;
        private LayoutInflater layoutInflater;

        public PayTestAdapter(Context context, List<Order> data) {
            this.context = context;
            this.objects = data;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public Order getItem(int position) {
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_pay_test, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            initializeViews((Order) getItem(position), (ViewHolder) convertView.getTag());
            return convertView;
        }

        private void initializeViews(Order object, ViewHolder holder) {
            holder.orderID.setText(object.getOutTradeNo());
            holder.orderNo.setText(object.getTransactionId());
            if ("0".equals(object.getTradeState())) {
                holder.orderState.setText("支付");
                holder.btnAction.setText("支付");
                holder.btnAction.setEnabled(true);
            } else if ("1".equals(object.getTradeState())) {
                holder.orderState.setText("退款");
                holder.btnAction.setText("退款");
                holder.btnAction.setEnabled(true);
            } else {
                holder.orderState.setText("已退款");
                holder.btnAction.setText("已退款");
                holder.btnAction.setEnabled(false);
            }

            holder.orderNum.setText(object.getFee());
            holder.orderTime.setText(DateFormatUtil.getStrDate4Date(new Date(Long.valueOf(object.getTimeStart())), "yyyy-MM-dd HH:mm:ss"));

        }

        protected class ViewHolder {
            private TextView orderID;
            private TextView orderState;
            private TextView orderNum;
            private TextView orderTime;
            private TextView orderNo;
            private TextView btnAction;

            public ViewHolder(View view) {
                orderID = view.findViewById(R.id.orderID);
                orderState = view.findViewById(R.id.orderState);
                orderNum = view.findViewById(R.id.orderNum);
                orderTime = view.findViewById(R.id.orderTime);
                orderNo = view.findViewById(R.id.orderNo);
                btnAction = view.findViewById(R.id.btn_action);
            }
        }
    }

}
