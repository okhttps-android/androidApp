package com.uas.appworks.OA.platform.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.thread.ThreadPool;
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.uas.appworks.OA.platform.adapter.PurchaseDetailsAdapter;
import com.uas.appworks.OA.platform.model.Purchase;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Bitlike on 2018/1/15.
 */

public class PurchaseDetailsActivity extends BaseNetActivity implements OnSmartHttpListener {

    private String varId;
    private String varStatus;
    private String enUU;
    private String mJson;
    private String mPhone;

    private RecyclerView mRecyclerView;
    private PurchaseDetailsAdapter mAdapter;

    private boolean changeStatus;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initIntent(intent);
        initData();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_purchase_details;
    }

    @Override
    protected String getBaseUrl() {
        String url = new ApiPlatform().getBaseUrl();
        LogUtil.i("baseUrl=" + url);
        return url;
    }

    @Override
    protected void init() throws Exception {
        Intent intent = getIntent();
        initIntent(intent);
        initView();
        initData();
    }

    private void initIntent(Intent intent) {
        if (intent != null) {
            varId = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
            varStatus = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE);
            mJson = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON);
            enUU = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ENUU);
            mPhone = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_TEL);

            if (Constants.FLAG.GET_LOCAL_ENUU.equals(enUU)) {
                enUU = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu");
            } else {
                enUU = CommonUtil.getSharedPreferences(mContext, Constants.CACHE.B2B_BUSINESS_ENUU);
            }
            if (TextUtils.isEmpty(mPhone)) {
                mPhone = MyApplication.getInstance().mLoginUser.getTelephone();
            }
        }
        if (varStatus == null) {
            varStatus = "";
        }
        changeStatus = false;
        setStatus();
    }

    private void setStatus() {
        String title = "";
        switch (varStatus) {
            case Constants.FLAG.STATE_PURCHASE_ORDER_END:
                title = getString(R.string.str_case_closed);
                break;
            case Constants.FLAG.STATE_PURCHASE_ORDER_DONE:
                title = getString(R.string.str_have_replied);
                break;
            case Constants.FLAG.STATE_PURCHASE_ORDER_TODO:
                title = getString(R.string.str_wait_for_reply);
                break;
        }
        setTitle(title);
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));

    }

    private void initData() {
        if (mJson != null && JSONUtil.validate(mJson)) {
            try {
                handleMessage(mJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadData();

    }


    private void loadData() {
        if (!CommonUtil.isNetWorkConnected(mContext)) {
            ToastUtil.showToast(mContext, R.string.networks_out);
        }
        showProgress();
        Parameter.Builder builder = new Parameter.Builder();
        String url = "mobile/sale/orders/" + varId + "/info";//卖方
        builder.mode(Method.GET)
                .url(url)
                .record(0x11)
                .showLog(true)
                .autoProgress(true)
                .addParams("en_uu", enUU)
                .addParams("user_tel", mPhone)
                .addParams("id", varId);
        requestHttp(builder, this);
    }

    private boolean replyed;

    private void verifiReply(List<Purchase> purchases) {
        if (TextUtils.isEmpty(enUU)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.prompt_title)
                    .content(R.string.notice_cannot_quote)
                    .positiveText(R.string.have_knew)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            materialDialog.dismiss();
                        }
                    }).build().show();
            return;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 1; i < purchases.size(); i++) {
            Purchase purchase = purchases.get(i);
            if (!TextUtils.isEmpty(purchase.getDate()) && purchase.isCanInput()) {
                Map<String, Object> map = new HashMap<>();
                map.put("qty", StringUtil.getFirstInt(purchase.getNumber(), 0));
                map.put("delivery", DateFormatUtil.str2Long(purchase.getDate(), DateFormatUtil.YMD));//
                map.put("remark", purchase.getRemarksInput());
                map.put("pdId", purchase.getId());
                mapList.add(map);
            }
        }
        if (!ListUtils.isEmpty(mapList)) {
            String json = JSONUtil.map2JSON(mapList);
            LogUtil.d("purchasejson", json);
            reply(json);
        } else {
            ToastUtil.showToast(ct, "没有可以提交的明细表单");
        }
    }

    private void reply(String json) {
        Parameter.Builder builder = new Parameter.Builder();
        builder.mode(Method.POST)
                .url("mobile/sale/orders/reply")
                .addParams("en_uu", enUU)
                .addParams("json", json)
                .addParams("user_tel", mPhone)
                .record(0x12)
                .showLog(true)
                .autoProgress(true);
        requestHttp(builder, this);

    }


    private void setData2Adapter(List<Purchase> dataList) {
        mAdapter = new PurchaseDetailsAdapter(ct, varStatus, dataList);
        mAdapter.setOnReplyLisenter(new PurchaseDetailsAdapter.OnReplyLisenter() {
            @Override
            public void reply(List<Purchase> purchases) {
                PurchaseDetailsActivity.this.verifiReply(purchases);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        switch (what) {
            case 0x11:
                handleMessage(message);
                break;
            case 0x12:
                if (!this.isFinishing()) {
                    replyed = true;
                    loadData();
                }
                break;
        }
        dismissProgress();
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {
        ToastUtil.showToast(ct, message);
        if (0x12 == what && StringUtil.isEmpty(message) && this != null) {
            loadData();
        }
    }


    private void handleMessage(final String message) throws Exception {
        LogUtil.i("message=" + message);
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                JSONObject object = JSON.parseObject(message);
                setVarStatus(object);
                final List<Purchase> dataList = new ArrayList<>();
                JSONArray orderItems = JSONUtil.getJSONArray(object, "orderItems");

                Purchase mainPurchase = getPurchase(true, object);
                float top = 0;
                if (!ListUtils.isEmpty(orderItems)) {
                    Purchase purchase = null;
                    for (int i = 0; i < orderItems.size(); i++) {
                        purchase = getPurchase(false, orderItems.getJSONObject(i));
                        dataList.add(purchase);
                        try {
                            top += Float.valueOf(purchase.getAmount());
                        } catch (Exception e) {
                            LogUtil.i("e=" + e.getMessage());
                        }
                    }
                }
                mainPurchase.setTotal(String.valueOf(top));
                dataList.add(0, mainPurchase);
                OAHttpHelper.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        setStatus();
                        if (replyed) {
                            Toast.makeText(MyApplication.getInstance(), "回复成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            setData2Adapter(dataList);
                        }
                    }
                });
            }
        });
    }

    private void setVarStatus(JSONObject object) {
        String end = JSONUtil.getText(object, "end");
        if (!TextUtils.isEmpty(end) && "1".equals(end)) {
            if (!varStatus.equals(Constants.FLAG.STATE_PURCHASE_ORDER_END)) {
                changeStatus = true;
                varStatus = Constants.FLAG.STATE_PURCHASE_ORDER_END;
            }
        } else {
            String status = JSONUtil.getText(object, "status");
            String reply = JSONUtil.getText(object, "reply");
            if ("200".equals(status)) {
                if (!varStatus.equals(Constants.FLAG.STATE_PURCHASE_ORDER_TODO)) {
                    changeStatus = true;
                    varStatus = Constants.FLAG.STATE_PURCHASE_ORDER_TODO;
                }
            } else if ("201".equals(status)) {
                if (!varStatus.equals(Constants.FLAG.STATE_PURCHASE_ORDER_DONE)) {
                    changeStatus = true;
                    varStatus = Constants.FLAG.STATE_PURCHASE_ORDER_DONE;
                }
            }
        }
    }


    private Purchase getPurchase(boolean first, JSONObject object) {
        Purchase purchase = new Purchase();
        int id = JSONUtil.getInt(object, "id");
        String address = JSONUtil.getText(object, "shipAddress");//收货地址
        String time = DateFormatUtil.long2Str(JSONUtil.getLong(object, "erpDate", "date"), DateFormatUtil.YMD);//单据时间
        String date = DateFormatUtil.long2Str(JSONUtil.getLong(object, "replyDelivery", "delivery"), DateFormatUtil.YMD);//交货日期
        String replyRemark = JSONUtil.getText(object, "replyRemark");//采购单号
        int status = JSONUtil.getInt(object, "status");//状态
        String code;//采购单号
        String remarks;//备注|产品
        String alls;//总额|产品规格
        if (first) {
            String currency = JSONUtil.getText(object, "currency");//采购单号
            code = JSONUtil.getText(object, "code");//采购单号
            remarks = JSONUtil.getText(object, "remark");//备注
            alls = JSONUtil.getText(object, "amount");//总额
            JSONObject enterprise = JSONUtil.getJSONObject(object, "enterprise");//客户
            String client = JSONUtil.getText(enterprise, "enName");
            purchase.setCurrency(currency);
            purchase.setCustomer(client);
        } else {
            JSONObject product = JSONUtil.getJSONObject(object, "product");
            code = JSONUtil.getText(product, "code");
            remarks = JSONUtil.getText(product, "title");
            alls = JSONUtil.getText(product, "spec");
            String unit = JSONUtil.getText(product, "unit");
            String amount = JSONUtil.getText(object, "amount");
            String price = JSONUtil.getText(object, "price");
            String latestReplyQty = JSONUtil.getText(object, "latestReplyQty");
            String number = "";
            if (TextUtils.isEmpty(latestReplyQty) || "0".equals(latestReplyQty)) {
                number = JSONUtil.getText(object, "qty");//数量
            } else {
                number = latestReplyQty;
            }

            purchase.setNumber(number);
            purchase.setAmount(amount);
            purchase.setPrice(price);
            purchase.setUnit(unit);
        }
        purchase.setCanInput(status == 200);
        purchase.setId(id);
        purchase.setCode(code);
        purchase.setAddress(address);
        purchase.setTime(time);
        purchase.setRemarks(remarks);
        purchase.setTotal(alls);
        purchase.setDate(date);
        purchase.setRemarksInput(replyRemark);
        return purchase;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (changeStatus) {
                setResult(Constants.FLAG.RESULT_PURCHASE_ORDER, new Intent().putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, varStatus));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && changeStatus) {
            setResult(Constants.FLAG.RESULT_PURCHASE_ORDER, new Intent().putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, varStatus));
        }
        return super.onKeyDown(keyCode, event);
    }
}
