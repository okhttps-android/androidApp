package com.uas.appworks.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.core.app.Constants;
import com.core.base.fragment.MvpBaseFragment;
import com.core.utils.CommonUtil;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.businessDetailActivity.BusinessDetailNewActivity;
import com.uas.appworks.activity.businessManage.businessMineList.BusinessMineListContract;
import com.uas.appworks.activity.businessManage.businessMineList.BusinessMineListPresenterImpl;
import com.uas.appworks.adapter.BusinessMineAdapter;
import com.uas.appworks.model.bean.BusinessMineChildBean;
import com.uas.appworks.model.bean.CommonColumnsBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/12 10:21
 */
public class BusinessMineListFragment extends MvpBaseFragment<BusinessMineListContract.IBusinessMineListPresenter>
        implements BusinessMineListContract.IBusinessMineListView {
    private static final String ARGUMENTS_BUSINESS_WHICHPAGE = "whichPage";
    private static final String ARGUMENTS_BUSINESS_BCCODE = "bc_code";

    public static final int FLAG_BUSINESS_CHARGE = 1;
    public static final int FLAG_BUSINESS_BRANCH = 2;
    public static final int FLAG_BUSINESS_ASSOCIATED = 3;

    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private TextView mEmptyText;
    private List<BusinessMineChildBean> mBusinessMineBeans;
    private BusinessMineAdapter mBusinessMineAdapter;
    private RefreshLayout mRefreshLayout;
    private String mCaller;
    private String mCondition = "1 = 1";
    private int mPageIndex = 1;
    private int mPageSize = 20;
    private int mWhichPage;
    private String mBcCode;

    public static BusinessMineListFragment newInstance(int whichPage) {
        BusinessMineListFragment businessMineListFragment = new BusinessMineListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENTS_BUSINESS_WHICHPAGE, whichPage);
        businessMineListFragment.setArguments(bundle);
        return businessMineListFragment;
    }

    public static BusinessMineListFragment newInstance(int whichPage, String bc_code) {
        BusinessMineListFragment businessMineListFragment = new BusinessMineListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENTS_BUSINESS_WHICHPAGE, whichPage);
        bundle.putString(ARGUMENTS_BUSINESS_BCCODE, bc_code);
        businessMineListFragment.setArguments(bundle);
        return businessMineListFragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_business_mine_list;
    }

    @Override
    protected BusinessMineListContract.IBusinessMineListPresenter initPresenter() {
        return new BusinessMineListPresenterImpl();
    }

    @Override
    protected void initViews() {
        mCaller = "BusinessChance";
        Bundle arguments = getArguments();
        if (arguments != null) {
            mWhichPage = arguments.getInt(ARGUMENTS_BUSINESS_WHICHPAGE);
            mBcCode = arguments.getString(ARGUMENTS_BUSINESS_BCCODE);
        } else {
            mWhichPage = FLAG_BUSINESS_CHARGE;
        }

        mRefreshLayout = $(R.id.business_mine_list_refresh);
        if (mWhichPage == FLAG_BUSINESS_ASSOCIATED) {
            mRefreshLayout.setEnableRefresh(false);
            mRefreshLayout.setEnableLoadMore(false);
            mCaller = "BusinessProductQuery";
        }

        mRecyclerView = $(R.id.business_mine_list_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        mRecyclerView.setNestedScrollingEnabled(false);

        mBusinessMineBeans = new ArrayList<>();
        mBusinessMineAdapter = new BusinessMineAdapter(mBusinessMineBeans);
        mRecyclerView.setAdapter(mBusinessMineAdapter);
    }

    @Override
    protected void initEvents() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                mPageIndex = 1;
                loadListData();
            }
        });

        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                mPageIndex++;
                loadListData();
            }
        });

        mBusinessMineAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (mWhichPage == FLAG_BUSINESS_ASSOCIATED) {
                    return;
                }
                String page = "";
                if (mWhichPage == FLAG_BUSINESS_BRANCH) {
                    page = "businessBranch";
                } else if (mWhichPage == FLAG_BUSINESS_CHARGE) {
                    page = "businessCharge";
                }
                BusinessMineChildBean businessMineChildBean = mBusinessMineBeans.get(position);
                startActivity(new Intent(mContext, BusinessDetailNewActivity.class)
                        .putExtra("id", businessMineChildBean.getId())
                        .putExtra("type", businessMineChildBean.getBcType())
                        .putExtra("stage", businessMineChildBean.getStageCode())
                        .putExtra("bc_code", businessMineChildBean.getBcCode())
                        .putExtra("bc_description", businessMineChildBean.getBcDescription())
                        .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, page));
            }
        });
    }

    @Override
    protected void initDatas() {
        if (mWhichPage == FLAG_BUSINESS_CHARGE) {
            mCondition = "bc_domancode=\'" + CommonUtil.getEmcode() + "\'";
        } else if (mWhichPage == FLAG_BUSINESS_BRANCH) {
            mCondition = "(bc_domancode in (select em_code from employee left join job on em_defaulthsid=jo_id where jo_subof=(select em_defaulthsid from employee where em_code =\'" + CommonUtil.getEmcode() + "\')))";
        } else if (mWhichPage == FLAG_BUSINESS_ASSOCIATED) {
            mCondition = "bp_bscode=\'" + mBcCode + "\'";
        }

        loadListData();
    }

    private void loadListData() {
        Map<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("condition", mCondition);
        params.put("page", mPageIndex);
        params.put("pageSize", mPageSize);
        mPresenter.getBusinessMineList(mContext, params);
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

    @Override
    public void requestListSuccess(List<BusinessMineChildBean> businessMineBeans, List<CommonColumnsBean> commonColumnsBeans) {
        if (mPageIndex == 1) {
            mBusinessMineBeans.clear();
        }
        mBusinessMineBeans.addAll(businessMineBeans);
        mBusinessMineAdapter.notifyDataSetChanged();

        if (mBusinessMineBeans.size() == 0) {
            if (mEmptyView == null || mEmptyText == null) {
                mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
            }
            mEmptyText.setText("列表数据为空");
            mBusinessMineAdapter.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void requestListFail(String failMsg) {
        if (mPageIndex == 1) {
            if (mEmptyView == null || mEmptyText == null) {
                mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
            }
            mEmptyText.setText(failMsg);
            mBusinessMineAdapter.setEmptyView(mEmptyView);
        } else {
            mPageIndex--;
            toast(failMsg);
        }
    }
}
