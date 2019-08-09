package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.RecycleViewDivider;
import com.google.gson.Gson;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.BusinessTargetsAdapter;
import com.modular.appmessages.model.BusinessStatisticsBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 商家营业指标页面
 * @date 2017/11/3 11:25
 */

public class BusinessTargetsActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private HttpClient mHttpClient;
    private String mIndustry;
    private String mIndustrycode;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<BusinessStatisticsBean.TargetsBean> mTargetsBeen;
    private BusinessTargetsAdapter mBusinessTargetsAdapter;
    private RecycleViewDivider mRecycleViewDivider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_targets);
        init();
        initEvents();
        initDatas();
    }

    private void init() {
        setTitle(R.string.company_business_targets);
        mHttpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).build(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.business_targets_rv);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecycleViewDivider = new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL);
        mRecyclerView.addItemDecoration(mRecycleViewDivider);
        mTargetsBeen = new ArrayList<>();
        mBusinessTargetsAdapter = new BusinessTargetsAdapter(this, mTargetsBeen);
        mRecyclerView.setAdapter(mBusinessTargetsAdapter);

    }

    private void initEvents() {
        mBusinessTargetsAdapter.setOnItemClickListener(new BusinessTargetsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(BusinessTargetsActivity.this, BusinessTargetDetailActivity.class);
                intent.putExtra("target_detail", mTargetsBeen.get(position));
                startActivity(intent);
            }
        });
    }

    private void initDatas() {
        if (CommonUtil.isNetWorkConnected(this)) {
            progressDialog.show();
            obtainIndustry();
        } else {
            ToastUtil.showToast(this, R.string.networks_out);
        }
    }


    private void obtainIndustry() {
        mHttpClient.Api().send(new HttpClient.Builder()
                        .url("user/appCompanyType")
                        .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu"))
                        .add("token", MyApplication.getInstance().mAccessToken)
                        .method(Method.GET)
                        .build()
                , new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        progressDialog.dismiss();
                        if (!JSONUtil.validate(o.toString()) || o == null) {
                            ToastUtil.showToast(BusinessTargetsActivity.this, getString(R.string.industry_access_failure));
                            return;
                        }
                        JSONArray array = JSON.parseObject(o.toString()).getJSONArray("result");
                        if (ListUtils.isEmpty(array)) {
                            ToastUtil.showToast(BusinessTargetsActivity.this, getString(R.string.industry_access_failure));
                            return;
                        }
                        JSONObject object = array.getJSONObject(0);
                        if (object == null) {
                            ToastUtil.showToast(BusinessTargetsActivity.this, getString(R.string.industry_access_failure));
                            return;
                        }
                        mIndustry = array.getJSONObject(0).getString("sc_industry");
                        mIndustrycode = array.getJSONObject(0).getString("sc_industrycode");

                        analysisTargets();
                    }

                    @Override
                    public void onFailure(Object t) {
                        progressDialog.dismiss();
                        ToastUtil.showToast(BusinessTargetsActivity.this, getString(R.string.industry_access_failure));
                    }
                }));
    }

    private void analysisTargets() {
        String statistics = CommonUtil.getAssetsJson(BusinessTargetsActivity.this, "business_statistics.json");
        if (JSONUtil.validate(statistics)) {
            JSONArray industryArray = JSON.parseArray(statistics);
            if (industryArray != null) {
                for (int i = 0; i < industryArray.size(); i++) {
                    JSONObject industryObject = industryArray.getJSONObject(i);
                    String industry = JSONUtil.getText(industryObject, "industry");
                    if (industry.equals(mIndustry)) {
                        JSONArray targetsArray = industryObject.getJSONArray("targets");
                        Gson gson = new Gson();
                        if (targetsArray != null) {
                            for (int j = 0; j < targetsArray.size(); j++) {
                                JSONObject targetObject = targetsArray.getJSONObject(j);
                                BusinessStatisticsBean.TargetsBean targetsBean = gson.fromJson(targetObject.toJSONString(), BusinessStatisticsBean.TargetsBean.class);
                                mTargetsBeen.add(targetsBean);
                            }
                            mBusinessTargetsAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }

            }
        }
    }
}
