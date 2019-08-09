package com.uas.appworks.activity;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CityIndustryParentAdapter;
import com.uas.appworks.model.bean.CityIndustryMenuBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 产城服务应用设置页面
 * @date 2017/11/17 9:10
 */

public class CityIndustryFuncSetActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView {
    private final int OBTAIN_CITY_INDUSTRY_SERVICE = 0x12;
    private final int OBTAIN_CITY_INDUSTRY_CONFIG = 0x13;

    private PullToRefreshListView mPullToRefreshListView;
    private CityIndustryParentAdapter mCityIndustryParentAdapter;
    private List<CityIndustryMenuBean> mCityIndustryMenuBeans;

    @Override
    protected int getLayout() {
        return R.layout.activity_city_industry;
    }

    @Override
    protected void initView() {
     setTitle(R.string.city_industry_service);
        mPullToRefreshListView = $(R.id.city_industry_ptlv);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mCityIndustryMenuBeans = new ArrayList<>();
        mCityIndustryParentAdapter = new CityIndustryParentAdapter(this, mCityIndustryMenuBeans);
        mPullToRefreshListView.setAdapter(mCityIndustryParentAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        obtainCityIndustryService();
//        obtainCityIndustryConfig();
    }

    private void obtainCityIndustryService() {
        Map<String, Object> params = new HashMap<>();
        params.put("kind", "cc");

        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));

        HttpParams request = new HttpParams.Builder()
                .flag(OBTAIN_CITY_INDUSTRY_SERVICE)
                .url("api/serve/mainPage/getServices.action")
                .setHeaders(header)
                .setParams(params)
                .method(Method.GET)
                .build();
        mPresenter.cityRequest(this, request);
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void requestSuccess(int what, Object object) {
        if (what == OBTAIN_CITY_INDUSTRY_SERVICE) {
            if (object != null) {
                Log.e("CityIndustryActivity", "service_success = " + object.toString());
                Gson gson = new Gson();
                String result = object.toString();
                if (JSONUtil.validate(result)) {
                    mCityIndustryMenuBeans.clear();
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray configArray = resultObject.getJSONArray("configs");
                    if (configArray != null) {
                        for (int i = 0; i < configArray.size(); i++) {
                            JSONObject configObject = configArray.getJSONObject(i);
                            CityIndustryMenuBean cityIndustryMenuBean = gson.fromJson(configObject.toString(), CityIndustryMenuBean.class);
                            mCityIndustryMenuBeans.add(cityIndustryMenuBean);
                        }
                    }
                    mCityIndustryParentAdapter.notifyDataSetChanged();
                }

            }

        } else if (what == OBTAIN_CITY_INDUSTRY_CONFIG) {
            Log.e("CityIndustryActivity", "config_success = " + object.toString());
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        Log.e("CityIndustryActivity", "error = " + errorMsg);
        toast(errorMsg);
    }

}
