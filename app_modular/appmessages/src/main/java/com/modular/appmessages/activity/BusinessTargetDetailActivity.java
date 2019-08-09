package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.RecycleViewDivider;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.BusinessTargetsDetailAdapter;
import com.modular.appmessages.model.BusinessStatisticsBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 商家营业指标具体值页面
 * @date 2017/11/3 11:26
 */

public class BusinessTargetDetailActivity extends BaseActivity {
    private BusinessStatisticsBean.TargetsBean mTargetsBean = new BusinessStatisticsBean.TargetsBean();
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecycleViewDivider mRecycleViewDivider;
    private BusinessTargetsDetailAdapter mBusinessTargetsDetailAdapter;
    private List<List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean>> mTargetDetailsBeen;
    private HttpClient mHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_target_detail);

        init();
        initDatas();
    }

    private void initDatas() {
        if (CommonUtil.isNetWorkConnected(this)) {
            progressDialog.show();
            obtainTotalList();
        } else {
            ToastUtil.showToast(this, R.string.networks_out);
        }
    }

    private void obtainTotalList() {
        mHttpClient.Api().send(new HttpClient.Builder()
                .url("user/appTotalList")
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu"))
                .add("userid", MyApplication.getInstance().mLoginUser.getUserId())
                .add("typelist", mTargetsBean.getTypelist())
                .add("token", MyApplication.getInstance().mAccessToken)
                .add("startdate", "")
                .add("enddate", "")
                .add("pageIndex", "0")
                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId"))
                .build(), new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                progressDialog.dismiss();
                String result = o.toString();
                Log.d("targetdetail", result);
                if (JSONUtil.validate(result)) {
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray datalistArray = resultObject.getJSONArray("datalist");
                    if (datalistArray != null) {
                        for (int i = 0; i < datalistArray.size(); i++) {
                            JSONObject jsonObject = datalistArray.getJSONObject(i);
                            List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean> targetDetails = mTargetsBean.getTargetDetails();
                            List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean> myTargetDetails = new ArrayList<BusinessStatisticsBean.TargetsBean.TargetDetailsBean>();
                            if (targetDetails != null) {
                                for (int j = 0; j < targetDetails.size(); j++) {
                                    BusinessStatisticsBean.TargetsBean.TargetDetailsBean targetDetailsBean = targetDetails.get(j);
                                    BusinessStatisticsBean.TargetsBean.TargetDetailsBean myTargetDetailsBean = new BusinessStatisticsBean.TargetsBean.TargetDetailsBean();
                                    myTargetDetailsBean.setDetailKey(targetDetailsBean.getDetailKey());
                                    myTargetDetailsBean.setDetailName(targetDetailsBean.getDetailName());
                                    myTargetDetailsBean.setDetailValue(JSONUtil.getText(jsonObject, targetDetailsBean.getDetailKey()));
                                    myTargetDetails.add(myTargetDetailsBean);
                                }
                            }
                            mTargetDetailsBeen.add(myTargetDetails);
                        }
                        mBusinessTargetsDetailAdapter.notifyDataSetChanged();
                    } else {
                        ToastUtil.showToast(BusinessTargetDetailActivity.this, getString(R.string.statistical_data_acquisition_failed));
                    }
                } else {
                    ToastUtil.showToast(BusinessTargetDetailActivity.this, getString(R.string.statistical_data_acquisition_failed));
                }
            }

            @Override
            public void onFailure(Object t) {
                progressDialog.dismiss();
                ToastUtil.showToast(BusinessTargetDetailActivity.this, getString(R.string.statistical_data_acquisition_failed));
            }
        }));
    }

    private void init() {
        Intent intent = getIntent();
        if (intent != null) {
            mTargetsBean = (BusinessStatisticsBean.TargetsBean) intent.getSerializableExtra("target_detail");
            setTitle(mTargetsBean.getTargetName());
        }
        mHttpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).build();

        mRecycleView = (RecyclerView) findViewById(R.id.business_target_detail_rv);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleViewDivider = new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 10, getResources().getColor(R.color.gray_light));
        mRecycleView.addItemDecoration(mRecycleViewDivider);
        mTargetDetailsBeen = new ArrayList<>();
        mBusinessTargetsDetailAdapter = new BusinessTargetsDetailAdapter(this, mTargetDetailsBeen);
        mRecycleView.setAdapter(mBusinessTargetsDetailAdapter);
    }
}
