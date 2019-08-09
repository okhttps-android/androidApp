package com.uas.appworks.activity;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.core.utils.SpanUtils;
import com.core.utils.time.wheel.OASigninPicker;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.uas.appworks.R;
import com.uas.appworks.adapter.EnterpriseInviteStatisticsAdapter;
import com.uas.appworks.model.bean.InviteStatisticsBean;
import com.uas.appworks.presenter.WorkPlatPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 企业邀请统计页面
 * @date 2018/3/25 19:56
 */

public class EnterpriseInviteStatisticsActivity extends BaseMVPActivity {
    private TextView mEnterpriseTextView, mYearTextView;
    private RecyclerView mRecyclerView;
    private SpanUtils mSpanUtils;
    private OASigninPicker mDatePicker;
    private List<InviteStatisticsBean> mInviteStatisticsBeans;
    private EnterpriseInviteStatisticsAdapter mStatisticsAdapter;
    private String mSelectedYear;

    @Override
    protected int getLayout() {
        return R.layout.activity_enterprise_invite_statistics;
    }

    @Override
    protected void initView() {
        setTitle("企业邀请统计");

        mEnterpriseTextView = $(R.id.enterprise_invite_statistics_name_tv);
        mYearTextView = $(R.id.enterprise_invite_statistics_year_tv);
        mRecyclerView = $(R.id.enterprise_invite_statistics_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatePicker = new OASigninPicker(this, 2000, 2050, false, false);
        mDatePicker.setRange(2050, 12, 31);

        mInviteStatisticsBeans = new ArrayList<>();
        mStatisticsAdapter = new EnterpriseInviteStatisticsAdapter(mInviteStatisticsBeans);
        mRecyclerView.setAdapter(mStatisticsAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mDatePicker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                mSelectedYear = year;
                getStatisticsData();
            }
        });

        mYearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yearText = mYearTextView.getText().toString();
                String yearStr = yearText.replace("年", "");
                int year;
                try {
                    year = Integer.parseInt(yearStr);
                } catch (Exception e) {
                    year = Integer.parseInt(DateFormatUtil.long2Str("yyyy"));
                }
                mDatePicker.setSelectedItem(year);

                mDatePicker.show();
            }
        });
    }

    @Override
    protected void initData() {
        mSelectedYear = DateFormatUtil.long2Str("yyyy");
        setDateText();
        mEnterpriseTextView.setText(CommonUtil.getEnName());

        if (CommonUtil.isNetWorkConnected(mContext)) {
            getStatisticsData();
        } else {
            toast(R.string.networks_out);
        }
    }

    private void setDateText() {
        mSpanUtils = new SpanUtils();
        SpannableStringBuilder yearSpan = mSpanUtils.append(mSelectedYear + "年").setForegroundColor(Color.RED).setUnderline().create();
        mYearTextView.setText(yearSpan);
    }

    private void getStatisticsData() {
        progressDialog.show();
        HttpRequest.getInstance().sendRequest(new ApiPlatform().getBaseUrl(),
                new HttpParams.Builder()
                        .url("public/invitation/count/groupBydate")
                        .addParam("enUU", CommonUtil.getEnuuLong(this))
                        .addParam("userUU", CommonUtil.getUseruuLong(this))
                        .addParam("userTel", MyApplication.getInstance().mLoginUser.getTelephone())
                        .addParam("businessCode", CommonUtil.getSharedPreferences(mContext, Constants.CACHE.EN_BUSINESS_CODE))
                        .addParam("year", mSelectedYear)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        Log.d("statisticsdatesuc", o.toString());
                        progressDialog.dismiss();
                        mInviteStatisticsBeans.clear();
                        setDateText();
                        initListData();
                        mStatisticsAdapter.setYear(mSelectedYear);
                        if (o == null) {
                            mStatisticsAdapter.notifyDataSetChanged();
                            return;
                        }
                        String result = o.toString();
                        if (!JSONUtil.validate(result)) {
                            mStatisticsAdapter.notifyDataSetChanged();
                            return;
                        }
                        JSONArray resultArray = JSON.parseArray(result);
                        if (resultArray == null || resultArray.size() == 0) {
                            mStatisticsAdapter.notifyDataSetChanged();
                            return;
                        }
                        for (int i = 0; i < resultArray.size(); i++) {
                            JSONObject resultObject = resultArray.getJSONObject(i);
                            int month = JSONUtil.getInt(resultObject, "month");
                            int inviteCount = JSONUtil.getInt(resultObject, "count");
                            int registerCount = JSONUtil.getInt(resultObject, "doneCount");
                            if (month > 0) {
                                mInviteStatisticsBeans.get(month - 1).setInviteCount(inviteCount);
                                mInviteStatisticsBeans.get(month - 1).setRegisterCount(registerCount);
                            }
                        }

                        mStatisticsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        progressDialog.dismiss();
                        toast(failStr);
                        Log.d("statisticsdatefai", failStr);
                    }
                });
    }

    private void initListData() {
        for (int i = 1; i <= 12; i++) {
            InviteStatisticsBean inviteStatisticsBean = new InviteStatisticsBean();
            inviteStatisticsBean.setMonth(i);
            inviteStatisticsBean.setInviteCount(0);
            inviteStatisticsBean.setRegisterCount(0);

            mInviteStatisticsBeans.add(inviteStatisticsBean);
        }
    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}
