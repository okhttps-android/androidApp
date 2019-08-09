package com.uas.appworks.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.core.base.fragment.BaseMVPFragment;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.SimpleView;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.uas.appworks.R;
import com.uas.appworks.adapter.BusinessRecordsAdapter;
import com.uas.appworks.model.bean.BusinessFollowBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/25 10:24
 */
public class BusinessRecordsFragment extends BaseMVPFragment<SimplePresenter> implements SimpleView {
    private static final String ARGUMENTS_BUSINESS_BCID = "arguments_business_bcid";

    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private List<BusinessFollowBean> mBusinessFollowBeans;
    private BusinessRecordsAdapter mBusinessRecordsAdapter;

    private View mEmptyView;
    private TextView mEmptyText;
    private long mBcId;

    public static BusinessRecordsFragment newInstance(long bc_id) {
        BusinessRecordsFragment businessRecordsFragment = new BusinessRecordsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ARGUMENTS_BUSINESS_BCID, bc_id);
        businessRecordsFragment.setArguments(bundle);
        return businessRecordsFragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_business_mine_list;
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initViews() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mBcId = arguments.getLong(ARGUMENTS_BUSINESS_BCID);
        }
        mRefreshLayout = $(R.id.business_mine_list_refresh);
        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(false);

        mRecyclerView = $(R.id.business_mine_list_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        mBusinessFollowBeans = new ArrayList<>();
        mBusinessRecordsAdapter = new BusinessRecordsAdapter(mBusinessFollowBeans);
        mRecyclerView.setAdapter(mBusinessRecordsAdapter);
    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void initDatas() {
        mPresenter.httpRequest(mContext, CommonUtil.getAppBaseUrl(mContext),
                new HttpParams.Builder()
                        .url("mobile/crm/businessChanceRecords.action")
                        .method(Method.GET)
                        .addParam("bcid", mBcId)
                        .addHeader("Cookie", CommonUtil.getErpCookie(mContext))
                        .build());
    }

    @Override
    public void requestSuccess(int what, Object object) {
        LogUtil.prinlnLongMsg("raomRecordsSuc", object.toString());
        try {
            String result = object.toString();
            if (!JSONUtil.validate(result)) {
                if (mEmptyView == null || mEmptyText == null) {
                    mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                    mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
                }
                mEmptyText.setText("跟进记录为空");
                mBusinessRecordsAdapter.setEmptyView(mEmptyView);
                return;
            }
            JSONObject resultObject = JSONObject.parseObject(result);
            JSONArray listdataArray = resultObject.getJSONArray("listdata");
            if (listdataArray == null || listdataArray.size() == 0) {
                if (mEmptyView == null || mEmptyText == null) {
                    mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                    mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
                }
                mEmptyText.setText("跟进记录为空");
                mBusinessRecordsAdapter.setEmptyView(mEmptyView);
                return;
            }
            for (int i = 0; i < listdataArray.size(); i++) {
                JSONObject listData = listdataArray.getJSONObject(i);
                if (listData != null) {
                    BusinessFollowBean businessFollowBean = new BusinessFollowBean();
                    businessFollowBean.setDoman(JSONUtil.getText(listData, "doman"));
                    businessFollowBean.setRemark(JSONUtil.getText(listData, "remark"));
                    businessFollowBean.setGeneration(JSONUtil.getText(listData, "generation"));
                    businessFollowBean.setNextgeneration(JSONUtil.getText(listData, "nextgeneration"));
                    businessFollowBean.setType(JSONUtil.getText(listData, "type"));
                    businessFollowBean.setDotime(JSONUtil.getText(listData, "dotime"));
                    businessFollowBean.setRemarkbf(JSONUtil.getText(listData, "remarkbf"));
                    businessFollowBean.setRemarkdt(JSONUtil.getText(listData, "remarkdt"));

                    mBusinessFollowBeans.add(businessFollowBean);
                }
            }
            mBusinessRecordsAdapter.notifyDataSetChanged();
            if (mBusinessFollowBeans.size() == 0) {
                if (mEmptyView == null || mEmptyText == null) {
                    mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                    mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
                }
                mEmptyText.setText("跟进记录为空");
                mBusinessRecordsAdapter.setEmptyView(mEmptyView);
                return;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        LogUtil.prinlnLongMsg("raomRecordsFail", errorMsg);
        if (mEmptyView == null || mEmptyText == null) {
            mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
            mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
        }
        mEmptyText.setText(errorMsg);
        mBusinessRecordsAdapter.setEmptyView(mEmptyView);
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }
}
