package com.uas.appworks.OA.platform.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.core.app.Constants;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.widget.SpaceItemDecoration;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.uas.appworks.OA.platform.adapter.BusinessTravelAdapter;
import com.uas.appworks.OA.platform.model.BusinessTravel;
import com.uas.appworks.R;
import com.uas.appworks.utils.TravelUtils;

import java.util.ArrayList;
import java.util.List;

public class BusinessTravelActivity extends BaseNetActivity implements OnSmartHttpListener {
    private BaseRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RelativeLayout remainRl;
    private TextView remainTag;
    private TextView remainTv;
    private LinearLayout leaderLL;
    private LinearLayout clickTrain;
    private LinearLayout clickAir;
    private LinearLayout clickHotel;

    private BusinessTravelAdapter mAdapter;
    private boolean isLead = false;
    private String cusCode;
    private String appSceret;
    private String appkey;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_business_travel;
    }

    @Override
    protected void init() throws Exception {
        initView();
        loadData();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        if (isLead && !isHasMenu) {
//            isHasMenu = true;
//            getMenuInflater().inflate(R.menu.menu_add_travel, menu);
//        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.addTravel == item.getItemId()) {
            TravelUtils.showSelect(ct,cusCode, appkey, appSceret, new BusinessTravel());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(BaseConfig.getContext());
    }

    private void initView() {
        LogUtil.i("BusinessTravelActivity");
        mRefreshLayout = findViewById(R.id.mRefreshLayout);
        leaderLL = findViewById(R.id.leaderLL);
        leaderLL.setVisibility(View.GONE);
        clickTrain = findViewById(R.id.clickTrain);
        clickAir = findViewById(R.id.clickAir);
        clickHotel = findViewById(R.id.clickHotel);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        remainRl = findViewById(R.id.remainRl);
        remainTag = findViewById(R.id.remainTag);
        remainTv = findViewById(R.id.remainTv);

        clickTrain.setOnClickListener(mOnClickListener);
        clickHotel.setOnClickListener(mOnClickListener);
        clickAir.setOnClickListener(mOnClickListener);
        remainTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence remain = remainTv.getText();
                if (!TextUtils.isEmpty(remain)) {
                    LogUtil.i("remain=" + remain);
                    if (remain.toString().equals(getString(R.string.click_to))) {
                        Intent intent = new Intent("com.modular.form.TravelDataFormDetailActivity");
                        String travelCaller = CommonUtil.getSharedPreferences(ct, Constants.WORK_TRAVEL_CALLER_CACHE);
                        intent.putExtra("caller", TextUtils.isEmpty(travelCaller) ? "FeePlease!CCSQ!new" : travelCaller);
                        startActivity(intent);
                    } else {
                        SystemUtil.phoneAction(BusinessTravelActivity.this, remain.toString());
                    }
                }
            }
        });
        mRefreshLayout.setEnabledPullUp(false);
        mRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }

            @Override
            public void onLoadMore() {
//                loadData(++page);
            }
        });
    }


    private View.OnClickListener mOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id=view.getId();
            if (R.id.clickAir == id) {
                TravelUtils.reserve(ct,cusCode, appkey, appSceret, getLeaderModel(BusinessTravel.AIR));
            } else if (R.id.clickHotel == id) {
                TravelUtils.reserve(ct,cusCode, appkey, appSceret, getLeaderModel(BusinessTravel.HOTEL));
            } else if (R.id.clickTrain == id) {
                TravelUtils.reserve(ct,cusCode, appkey, appSceret, getLeaderModel(BusinessTravel.TRAIN));
            }
        }
    };

    private BusinessTravel getLeaderModel(int type) {
        BusinessTravel model = new BusinessTravel();
        model.setType(type);
        return model;
    }
    private void showPop() {
        final PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(R.layout.pop_click_menu, null);
        window.setContentView(view);
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.color.transparent));
        DisplayUtil.backgroundAlpha(ct, 0.4f);
        window.setTouchable(true);
        window.setHeight(DisplayUtil.dip2px(ct, 200));
        window.setWidth(DisplayUtil.getSreechWidth(ct));
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        int actionBarHeight = getSupportActionBar().getHeight() + DisplayUtil.dip2px(ct, 10);
        window.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, actionBarHeight);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
    }


    public void loadData() {
        if (!NetUtils.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
            return;
        }

        if (!mRefreshLayout.isRefreshing()) {
            showProgress();
        }
        Parameter.Builder builder = new Parameter.Builder();
        builder.url("mobile/getBussinessTrip.action")
                .addParams("emcode", CommonUtil.getEmcode())
                .mode(Method.POST)
                .showLog(BaseConfig.showLogAble());
        requestCompanyHttp(builder, this);
    }


    @Override
    public void onSuccess(int what, String message, Tags tags) throws Exception {
        handlerData(message);
        mRefreshLayout.stopRefresh();
        dismissProgress();
    }

    @Override
    public void onFailure(int what, String message, Tags tags) throws Exception {
        mRefreshLayout.stopRefresh();
        dismissProgress();
    }


    private void handlerData(String message) throws Exception {
        LogUtil.i("message=" + message);
        JSONObject object = JSON.parseObject(message);
        int isLead = JSONUtil.getInt(object, "isLead");
        cusCode = JSONUtil.getText(object, "travelCard");
        appkey = JSONUtil.getText(object, "appKey");
        appSceret = JSONUtil.getText(object, "appSceret");
        //表示可以预定
        this.isLead = isLead > 0 && !TextUtils.isEmpty(appkey);
        JSONArray listdata = JSONUtil.getJSONArray(object, "listdata");
        List<BusinessTravel> models = new ArrayList<>();
        leaderLL.setVisibility(this.isLead ? View.VISIBLE : View.GONE);
//        if (this.isLead) {
//            BusinessTravel model = new BusinessTravel();
//            model.setType(BusinessTravel.LEADER);
//            models.add(model);
//        }
        String cttpid = JSONUtil.getText(object, "em_iccode");
        for (int i = 0; i < listdata.size(); i++) {
            JSONObject data = listdata.getJSONObject(i);
            String fp_id = JSONUtil.getText(data, "FP_ID");
            long startTime = JSONUtil.getLong(data, "FP_PRESTARTDATE");
            long endTime = JSONUtil.getLong(data, "FP_PREENDDATE");
            JSONArray reimbursements = JSONUtil.getJSONArray(data, "reimbursement");
            List<BusinessTravel> gridModels = new ArrayList<>();
            for (int j = 0; j < reimbursements.size(); j++) {
                BusinessTravel e = new BusinessTravel(cttpid, fp_id, startTime, endTime, reimbursements.getJSONObject(j));
                if (e.getType() != BusinessTravel.TITLE) {
                    gridModels.add(e);
                }
            }
            if (!ListUtils.isEmpty(gridModels)) {
                models.add(BusinessTravel.createTitle(data));
                models.addAll(gridModels);
            }
        }
        //判断
        if (TextUtils.isEmpty(appkey)) {
            remainRl.setVisibility(View.VISIBLE);
            mRefreshLayout.setVisibility(View.GONE);
            remainTag.setText("您的账号未开通商旅服务，请联系负责人");
            remainTv.setText(R.string.administrators_phone);
        } else {
            remainRl.setVisibility(View.GONE);
            mRefreshLayout.setVisibility(View.VISIBLE);
            setAdapter(models);
        }
        if (BaseConfig.isDebug()) {
            LogUtil.i("message=" + message);
        }
    }


    private void setAdapter(List<BusinessTravel> models) {
        if (mAdapter == null) {
            mAdapter = new BusinessTravelAdapter(ct,cusCode, appkey, appSceret, models);
            mRecyclerView.addItemDecoration(new SpaceItemDecoration(10));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
        }
        if (ListUtils.isEmpty(models)) {
            if (!isLead) {
                remainRl.setVisibility(View.VISIBLE);
                mRefreshLayout.setVisibility(View.GONE);
                remainTag.setText("请先录入出差单");
                remainTv.setText(R.string.click_to);
            } else {
                remainRl.setVisibility(View.GONE);
                mRefreshLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x21 && resultCode == 0x21) {
            loadData();
        }
    }

}
