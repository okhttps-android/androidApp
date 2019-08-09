package com.uas.appworks.activity.businessManage;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.LogUtil;
import com.core.app.Constants;
import com.core.base.activity.MvpBaseActivity;
import com.core.interfac.OnVoiceCompleteListener;
import com.core.utils.CommonUtil;
import com.core.widget.VoiceSearchView;
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
 * @date 2018/9/21 15:02
 */
public class BusinessCompanyListActivity extends MvpBaseActivity<BusinessMineListContract.IBusinessMineListPresenter>
        implements BusinessMineListContract.IBusinessMineListView {
    public static final int PAGE_BUSINESS_COMPANY = 863;
    public static final int PAGE_BUSINESS_CHARGE = 864;
    public static final int PAGE_BUSINESS_BRANCH = 865;

    private RefreshLayout mRefreshLayout;
    private int mPageIndex = 1, mPageSize = 20;
    private RecyclerView mRecyclerView;
    private VoiceSearchView mVoiceSearchView;
    private List<BusinessMineChildBean> mBusinessMineBeans;
    private List<CommonColumnsBean> mCommonColumnsBeans;
    private BusinessMineAdapter mBusinessMineAdapter;
    private View mEmptyView;
    private TextView mEmptyText;
    private String mCaller;
    private String mCondition = "1 = 1";
    private int mWhichPage;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_company_list;
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            mWhichPage = intent.getIntExtra(Constants.FLAG.COMMON_WHICH_PAGE, 0);
        }

        if (mWhichPage == PAGE_BUSINESS_COMPANY) {
            setTitle(getString(R.string.company_business_library));
            mCaller = "BusinessChance";
            mCondition = "1 = 1";
        } else if (mWhichPage == PAGE_BUSINESS_CHARGE) {
            setTitle(getString(R.string.business_charge));
            mCaller = "BusinessChance";
            mCondition = "bc_domancode=\'" + CommonUtil.getEmcode() + "\'";
        } else if (mWhichPage == PAGE_BUSINESS_BRANCH) {
            setTitle(getString(R.string.business_branch));
            mCaller = "BusinessChance";
            mCondition = "(bc_domancode in (select em_code from employee left join job on em_defaulthsid=jo_id where jo_subof=(select em_defaulthsid from employee where em_code =\'" + CommonUtil.getEmcode() + "\')))";
        }

        mRefreshLayout = $(R.id.business_company_list_refresh);
        mVoiceSearchView = $(R.id.business_company_list_vsv);
        mRecyclerView = $(R.id.business_company_list_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBusinessMineBeans = new ArrayList<>();
        mBusinessMineAdapter = new BusinessMineAdapter(mBusinessMineBeans);
        mRecyclerView.setAdapter(mBusinessMineAdapter);
    }

    @Override
    protected BusinessMineListContract.IBusinessMineListPresenter initPresenter() {
        return new BusinessMineListPresenterImpl();
    }

    @Override
    protected void initEvent() {
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

        mVoiceSearchView.setOnVoiceCompleteListener(new OnVoiceCompleteListener() {
            @Override
            public void onVoiceComplete(String text) {
                mPageIndex = 1;
                searchEvent(text);
            }
        });

        mVoiceSearchView.setOnEnterActionListener(new VoiceSearchView.OnEnterActionListener() {
            @Override
            public void onEnterAction() {
                mPageIndex = 1;
                String text = mVoiceSearchView.getText().toString();
                searchEvent(text);
            }
        });

        mBusinessMineAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String detailPage = "";
                if (mWhichPage == PAGE_BUSINESS_COMPANY) {
                    detailPage = "businessCompany";
                } else if (mWhichPage == PAGE_BUSINESS_CHARGE) {
                    detailPage = "businessCharge";
                } else if (mWhichPage == PAGE_BUSINESS_BRANCH) {
                    detailPage = "businessBranch";
                }
                BusinessMineChildBean businessMineChildBean = mBusinessMineBeans.get(position);
                if (businessMineChildBean.getItemType() == BusinessMineChildBean.BUSINESS_MINE_PARENT) {
                    return;
                }
                startActivity(new Intent(mContext, BusinessDetailNewActivity.class)
                        .putExtra("id", businessMineChildBean.getId())
                        .putExtra("type", businessMineChildBean.getBcType())
                        .putExtra("bc_code", businessMineChildBean.getBcCode())
                        .putExtra("bc_description", businessMineChildBean.getBcDescription())
                        .putExtra("stage", businessMineChildBean.getStageCode())
                        .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, detailPage));
            }
        });

    }

    private void searchEvent(String text) {
        if (mWhichPage == PAGE_BUSINESS_COMPANY) {
            mCondition = "1 = 1";
        } else if (mWhichPage == PAGE_BUSINESS_CHARGE) {
            mCondition = "bc_domancode=\'" + CommonUtil.getEmcode() + "\'";
        } else if (mWhichPage == PAGE_BUSINESS_BRANCH) {
            mCondition = "(bc_domancode in (select em_code from employee left join job on em_defaulthsid=jo_id where jo_subof=(select em_defaulthsid from employee where em_code =\'" + CommonUtil.getEmcode() + "\')))";
        }

        if (!TextUtils.isEmpty(text)) {
            String searchCondition = "";
            if (mCommonColumnsBeans != null) {
                for (int i = 0; i < mCommonColumnsBeans.size(); i++) {
                    CommonColumnsBean commonColumnsBean = mCommonColumnsBeans.get(i);
                    if (commonColumnsBean != null) {
                        searchCondition += "instr(" + commonColumnsBean.getDataIndex()
                                + ",\'" + text + "\')>0 or ";
                    }
                }
            }

            searchCondition = "(" + searchCondition.substring(0, searchCondition.length() - 4) + ")";

            mCondition = searchCondition + " and (" + mCondition + ")";
        }

        loadListData();
    }

    @Override
    protected void initData() {
        loadListData();
    }

    private void loadListData() {
        LogUtil.prinlnLongMsg("raoCondition", mCondition);
        Map<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("condition", mCondition);
        params.put("page", mPageIndex);
        params.put("pageSize", mPageSize);
        mPresenter.getBusinessMineList(mContext, params);
    }

    @Override
    public void requestListSuccess(List<BusinessMineChildBean> businessMineBeans, List<CommonColumnsBean> commonColumnsBeans) {
        mCommonColumnsBeans = commonColumnsBeans;
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
