package com.uas.appworks.activity.businessManage;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.core.base.activity.MvpBaseActivity;
import com.core.utils.CommonUtil;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.businessManageHome.BusinessManageHomeContract;
import com.uas.appworks.activity.businessManage.businessManageHome.BusinessManageHomePresenterImpl;
import com.uas.appworks.adapter.BusinessHomeRecordAdapter;
import com.uas.appworks.model.bean.BusinessOverTimeBean;
import com.uas.appworks.model.bean.BusinessRankBean;
import com.uas.appworks.model.bean.BusinessRecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/11 13:46
 */
public class BusinessRecordListActivity extends MvpBaseActivity<BusinessManageHomeContract.IBusinessManageHomePresenter>
        implements BusinessManageHomeContract.IBusinessManageHomeView {

    private RefreshLayout mRefreshLayout;
    private int mPageIndex = 1, mPageSize = 20;
    private RecyclerView mRecyclerView;
    private List<BusinessRecordBean> mBusinessRecordBeans;
    private BusinessHomeRecordAdapter mBusinessHomeRecordAdapter;
    private View mEmptyView;
    private TextView mEmptyText;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_record_list;
    }

    @Override
    protected void initView() {
        setTitle("销售记录");

        mRefreshLayout = $(R.id.business_record_list_refresh);
        mRecyclerView = $(R.id.business_record_list_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        mBusinessRecordBeans = new ArrayList<>();
        mBusinessHomeRecordAdapter = new BusinessHomeRecordAdapter(mBusinessRecordBeans);
        mRecyclerView.setAdapter(mBusinessHomeRecordAdapter);
    }

    @Override
    protected BusinessManageHomeContract.IBusinessManageHomePresenter initPresenter() {
        return new BusinessManageHomePresenterImpl();
    }

    @Override
    protected void initEvent() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                mPageIndex = 1;
                mPresenter.getBusinessRecord(mContext, CommonUtil.getEmcode(), mPageIndex, mPageSize);
            }
        });

        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                mPageIndex++;
                mPresenter.getBusinessRecord(mContext, CommonUtil.getEmcode(), mPageIndex, mPageSize);
            }
        });
    }

    @Override
    protected void initData() {
        mPresenter.getBusinessRecord(mContext, CommonUtil.getEmcode(), mPageIndex, mPageSize);
    }

    @Override
    public void requestDataSuccess(String resultJson) {

    }

    @Override
    public void requestRecordSuccess(List<BusinessRecordBean> businessRecordBeans) {
        if (mPageIndex == 1){
            mBusinessRecordBeans.clear();
        }
        mBusinessRecordBeans.addAll(businessRecordBeans);
        mBusinessHomeRecordAdapter.notifyDataSetChanged();

        if (mBusinessRecordBeans.size() == 0){
            if (mEmptyView == null || mEmptyText == null) {
                mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
            }
            mEmptyText.setText("销售记录为空");
            mBusinessHomeRecordAdapter.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void requestOvertimeSuccess(List<BusinessOverTimeBean> businessOverTimeBeans) {

    }

    @Override
    public void requestRankSuccess(List<BusinessRankBean> businessRankBeans) {

    }

    @Override
    public void requestAllSuccess(String resultJson, List<BusinessRecordBean> businessRecordBeans, List<BusinessOverTimeBean> businessOverTimeBeans, List<BusinessRankBean> businessRankBeans) {

    }

    @Override
    public void requestOptionSuccess(int flag, String resultJson) {

    }

    @Override
    public void requestFail(int flag, String failStr) {
        if (mPageIndex == 1){
            if (mEmptyView == null || mEmptyText == null) {
                mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
            }
            mEmptyText.setText(failStr);
            mBusinessHomeRecordAdapter.setEmptyView(mEmptyView);
        }else {
            mPageIndex--;
            toast(failStr);
        }
    }

    @Override
    public void showLoading(String loadStr) {
        if (mRefreshLayout != null && !(mRefreshLayout.isRefreshing() || mRefreshLayout.isLoading())) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        if (mRefreshLayout != null) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(0);
            }
            if (mRefreshLayout.isLoading()) {
                mRefreshLayout.finishLoadMore(0);
            }
        }
        progressDialog.dismiss();
    }
}
