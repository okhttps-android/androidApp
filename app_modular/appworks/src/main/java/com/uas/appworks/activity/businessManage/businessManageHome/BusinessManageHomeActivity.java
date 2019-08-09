package com.uas.appworks.activity.businessManage.businessManageHome;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.base.activity.MvpBaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.OASigninPicker;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.CRM.erp.activity.BusinessActivity;
import com.uas.appworks.CRM.erp.activity.BusinessDetailActivty;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.BusinessBillInputActivity;
import com.uas.appworks.activity.businessManage.BusinessCompanyListActivity;
import com.uas.appworks.activity.businessManage.BusinessOvertimeListActivity;
import com.uas.appworks.activity.businessManage.BusinessRankListActivity;
import com.uas.appworks.activity.businessManage.BusinessRecordListActivity;
import com.uas.appworks.activity.businessManage.businessMineList.BusinessMineListActivity;
import com.uas.appworks.adapter.BusinessHomeOvertimeAdapter;
import com.uas.appworks.adapter.BusinessHomeRankAdapter;
import com.uas.appworks.adapter.BusinessHomeRecordAdapter;
import com.uas.appworks.model.bean.BusinessOverTimeBean;
import com.uas.appworks.model.bean.BusinessRankBean;
import com.uas.appworks.model.bean.BusinessRecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 商机管理主页面
 * @date 2018/9/10 13:43
 */
public class BusinessManageHomeActivity extends MvpBaseActivity<BusinessManageHomeContract.IBusinessManageHomePresenter>
        implements BusinessManageHomeContract.IBusinessManageHomeView, View.OnClickListener {

    private RefreshLayout mRefreshLayout;
    private TextView mDataTimeTextView, mDataRefreshTextView,
            mRecordRefreshTextView, mOvertimeRefreshTextView,
            mRankRefreshTextView, mAddProjectBtn, mAddOemBtn, mAddCompanyBtn,
            mNewlyTextView, mChangeTextView, mWinTextView, mLoseTextView, mInvalidTextView,
            mFollowTextView;
    private String mDataDateTime, mDataRefreshTime, mRecordRefreshTime, mOvertimeRefreshTime, mRankRefreshTime;
    private RecyclerView mrecordRecyclerView, mOvertimeRecyclerView, mRankRecyclerView;
    private LinearLayout mDataTimeLayout, mRecordAllLayout,
            mOvertimeAllLayout, mRankAllLayout,
            mHeaderMineLayout, mHeaderCompanyLayout,
            mHeaderDistributionLayout, mHeaderGrapLayout, mHeaderFunnelLayout;
    private List<BusinessRecordBean> mBusinessRecordBeans;
    private List<BusinessOverTimeBean> mBusinessOverTimeBeans;
    private List<BusinessRankBean> mBusinessRankBeans;
    private BusinessHomeRecordAdapter mBusinessHomeRecordAdapter;
    private BusinessHomeOvertimeAdapter mBusinessHomeOvertimeAdapter;
    private BusinessHomeRankAdapter mBusinessHomeRankAdapter;
    private PopupWindow mMenuPopupWindow;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_manage_home;
    }

    @Override
    protected void initView() {
        setTitle(R.string.str_work_business_manage);

        mDataTimeTextView = $(R.id.business_manage_home_data_date_tv);
        mRefreshLayout = $(R.id.business_manage_home_refreshlayout);

        mDataRefreshTextView = $(R.id.business_manage_home_data_refresh_tv);
        mRecordRefreshTextView = $(R.id.business_manage_home_record_refresh_tv);
        mOvertimeRefreshTextView = $(R.id.business_manage_home_overtime_refresh_tv);
        mRankRefreshTextView = $(R.id.business_manage_home_rank_refresh_tv);
        mRecordAllLayout = $(R.id.business_manage_home_record_all);
        mOvertimeAllLayout = $(R.id.business_manage_home_overtime_all);
        mRankAllLayout = $(R.id.business_manage_home_rank_all);
        mDataTimeLayout = $(R.id.business_manage_home_data_date_ll);
        mHeaderMineLayout = $(R.id.business_manage_home_mine_ll);
        mHeaderCompanyLayout = $(R.id.business_manage_home_company_ll);
        mHeaderDistributionLayout = $(R.id.business_manage_home_distribution_ll);
        mHeaderGrapLayout = $(R.id.business_manage_home_grap_ll);
        mHeaderFunnelLayout = $(R.id.business_manage_home_funnel_ll);
        mNewlyTextView = $(R.id.business_manage_home_data_newly_tv);
        mChangeTextView = $(R.id.business_manage_home_data_change_tv);
        mWinTextView = $(R.id.business_manage_home_data_win_tv);
        mLoseTextView = $(R.id.business_manage_home_data_lose_tv);
        mInvalidTextView = $(R.id.business_manage_home_data_invalid_tv);
        mFollowTextView = $(R.id.business_manage_home_data_follow_tv);

        mDataRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mRecordRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mOvertimeRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mRankRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);

        mrecordRecyclerView = $(R.id.business_manage_home_record_rv);
        mrecordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mrecordRecyclerView.setNestedScrollingEnabled(false);
        mBusinessRecordBeans = new ArrayList<>();
        mBusinessHomeRecordAdapter = new BusinessHomeRecordAdapter(mBusinessRecordBeans);
        mrecordRecyclerView.setAdapter(mBusinessHomeRecordAdapter);

        mOvertimeRecyclerView = $(R.id.business_manage_home_ovetime_rv);
        mOvertimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mOvertimeRecyclerView.setNestedScrollingEnabled(false);
        mBusinessOverTimeBeans = new ArrayList<>();
        mBusinessHomeOvertimeAdapter = new BusinessHomeOvertimeAdapter(mBusinessOverTimeBeans);
        mOvertimeRecyclerView.setAdapter(mBusinessHomeOvertimeAdapter);

        mRankRecyclerView = $(R.id.business_manage_home_rank_rv);
        mRankRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRankRecyclerView.setNestedScrollingEnabled(false);
        mRankRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        mBusinessRankBeans = new ArrayList<>();
        mBusinessHomeRankAdapter = new BusinessHomeRankAdapter(mBusinessRankBeans);
        mRankRecyclerView.setAdapter(mBusinessHomeRankAdapter);

        initMenuPop();
    }

    private void initMenuPop() {
        View menuView = View.inflate(this, R.layout.pop_business_home_menu, null);

        mAddProjectBtn = menuView.findViewById(R.id.business_home_menu1);
        mAddOemBtn = menuView.findViewById(R.id.business_home_menu2);
        mAddCompanyBtn = menuView.findViewById(R.id.business_home_menu3);

        mMenuPopupWindow = new PopupWindow(menuView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mMenuPopupWindow.setOutsideTouchable(true);
        mMenuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mMenuPopupWindow != null) {
                    mMenuPopupWindow.dismiss();
                }
                DisplayUtil.backgroundAlpha(mContext, 1f);
            }
        });
    }

    @Override
    protected BusinessManageHomeContract.IBusinessManageHomePresenter initPresenter() {
        return new BusinessManageHomePresenterImpl();
    }

    @Override
    protected void initEvent() {
        mDataRefreshTextView.setOnClickListener(this);
        mRecordRefreshTextView.setOnClickListener(this);
        mOvertimeRefreshTextView.setOnClickListener(this);
        mRankRefreshTextView.setOnClickListener(this);
        mRecordAllLayout.setOnClickListener(this);
        mOvertimeAllLayout.setOnClickListener(this);
        mRankAllLayout.setOnClickListener(this);
        mDataTimeLayout.setOnClickListener(this);
        mHeaderMineLayout.setOnClickListener(this);
        mHeaderCompanyLayout.setOnClickListener(this);
        mHeaderDistributionLayout.setOnClickListener(this);
        mHeaderGrapLayout.setOnClickListener(this);
        mHeaderFunnelLayout.setOnClickListener(this);
        mAddProjectBtn.setOnClickListener(this);
        mAddOemBtn.setOnClickListener(this);
        mAddCompanyBtn.setOnClickListener(this);

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                mDataDateTime = DateFormatUtil.long2Str("yyyy年MM月");
                mPresenter.getBusinessAll(mContext, DateFormatUtil.long2Str(DateFormatUtil.YM), CommonUtil.getEmcode());
            }
        });
    }

    @Override
    protected void initData() {
        mDataDateTime = DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy年MM月");
        mDataTimeTextView.setText(mDataDateTime);
        mPresenter.getBusinessAll(mContext, DateFormatUtil.long2Str(DateFormatUtil.YM), CommonUtil.getEmcode());
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.business_manage_home_data_refresh_tv) {
            mDataDateTime = DateFormatUtil.long2Str("yyyy年MM月");
            mPresenter.getBusinessData(mContext, DateFormatUtil.long2Str(DateFormatUtil.YM));
        } else if (i == R.id.business_manage_home_record_refresh_tv) {
            mPresenter.getBusinessRecord(mContext, CommonUtil.getEmcode(), 1, 2);
        } else if (i == R.id.business_manage_home_overtime_refresh_tv) {
            mPresenter.getBusinessOvertime(mContext, CommonUtil.getEmcode(), 1, 2);
        } else if (i == R.id.business_manage_home_rank_refresh_tv) {
            mPresenter.getBusinessRank(mContext, 1, 3);
        } else if (i == R.id.business_manage_home_record_all) {
            startActivity(BusinessRecordListActivity.class);
        } else if (i == R.id.business_manage_home_overtime_all) {
            startActivity(BusinessOvertimeListActivity.class);
        } else if (i == R.id.business_manage_home_rank_all) {
            startActivity(BusinessRankListActivity.class);
        } else if (i == R.id.business_manage_home_data_date_ll) {
            String year = mDataDateTime.substring(0, 4);
            String month = mDataDateTime.substring(5, 7);
            showDateDialog(year, month, mDataTimeTextView);
        } else if (i == R.id.business_manage_home_mine_ll) {
            startActivity(BusinessMineListActivity.class);
        } else if (i == R.id.business_manage_home_company_ll) {
            startActivity(new Intent(mContext, BusinessCompanyListActivity.class)
                    .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, BusinessCompanyListActivity.PAGE_BUSINESS_COMPANY));
        } else if (i == R.id.business_manage_home_distribution_ll) {
            startActivity(new Intent(this, BusinessDetailActivty.class).putExtra("bt_type", 2));
        } else if (i == R.id.business_manage_home_grap_ll) {
            startActivity(new Intent(this, BusinessDetailActivty.class).putExtra("bt_type", 1));
        } else if (i == R.id.business_manage_home_funnel_ll) {
            mPresenter.getOptionList(this, BusinessManageHomePresenterImpl.REQUEST_OPTION_LIST2
                    , "sys", "isNewBusinessChance");
        } else if (i == R.id.business_home_menu1) {
            startActivity(new Intent(ct, BusinessBillInputActivity.class)
                    .putExtra(Constants.Intents.CALLER, "ProjectBusinessChance")
                    .putExtra(Constants.Intents.TITLE, getString(R.string.project_business_chance))
                    .putExtra(Constants.Intents.ID, 0));
            if (mMenuPopupWindow != null) {
                mMenuPopupWindow.dismiss();
            }
            DisplayUtil.backgroundAlpha(mContext, 1f);
        } else if (i == R.id.business_home_menu2) {
            startActivity(new Intent(ct, BusinessBillInputActivity.class)
                    .putExtra(Constants.Intents.CALLER, "OEMBusinessChance")
                    .putExtra(Constants.Intents.TITLE, getString(R.string.oem_business_chance))
                    .putExtra(Constants.Intents.ID, 0));
            if (mMenuPopupWindow != null) {
                mMenuPopupWindow.dismiss();
            }
            DisplayUtil.backgroundAlpha(mContext, 1f);
        } else if (i == R.id.business_home_menu3) {
            startActivity(new Intent(ct, BusinessBillInputActivity.class)
                    .putExtra(Constants.Intents.CALLER, "BusinessChance")
                    .putExtra(Constants.Intents.TITLE, getString(R.string.str_company_business_list))
                    .putExtra(Constants.Intents.ID, 0));
            if (mMenuPopupWindow != null) {
                mMenuPopupWindow.dismiss();
            }
            DisplayUtil.backgroundAlpha(mContext, 1f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            mPresenter.getOptionList(this, BusinessManageHomePresenterImpl.REQUEST_OPTION_LIST1
                    , "sys", "isNewBusinessChance");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showLoading(String loadStr) {
        if (mRefreshLayout != null && !mRefreshLayout.isRefreshing()) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(0);
        }
        progressDialog.dismiss();
    }

    @Override
    public void requestDataSuccess(String resultJson) {
        try {
            analysisData(resultJson);
        } catch (Exception e) {

        }
    }

    @Override
    public void requestRecordSuccess(List<BusinessRecordBean> businessRecordBeans) {
        analysisRecord(businessRecordBeans);
    }

    @Override
    public void requestOvertimeSuccess(List<BusinessOverTimeBean> businessOverTimeBeans) {
        analysisOvertime(businessOverTimeBeans);
    }

    @Override
    public void requestRankSuccess(List<BusinessRankBean> businessRankBeans) {
        analysisRank(businessRankBeans);
    }

    @Override
    public void requestAllSuccess(String resultJson, List<BusinessRecordBean> businessRecordBeans, List<BusinessOverTimeBean> businessOverTimeBeans, List<BusinessRankBean> businessRankBeans) {
        analysisData(resultJson);

        List<BusinessRecordBean> recordBeans = getCountList(businessRecordBeans, 2);
        List<BusinessOverTimeBean> overTimeBeans = getCountList(businessOverTimeBeans, 2);
        List<BusinessRankBean> rankBeans = getCountList(businessRankBeans, 3);

        analysisRecord(recordBeans);
        analysisOvertime(overTimeBeans);
        analysisRank(rankBeans);
    }

    @Override
    public void requestOptionSuccess(int flag, String resultJson) {
        if (flag == BusinessManageHomePresenterImpl.REQUEST_OPTION_LIST1) {
            if (TextUtils.isEmpty(resultJson)) {
                mAddCompanyBtn.setVisibility(View.VISIBLE);
                mAddProjectBtn.setVisibility(View.GONE);
                mAddOemBtn.setVisibility(View.GONE);
            } else {
                mAddCompanyBtn.setVisibility(View.GONE);
                mAddProjectBtn.setVisibility(View.VISIBLE);
                mAddOemBtn.setVisibility(View.VISIBLE);
            }
            if (mMenuPopupWindow != null) {
                View view = getWindow().findViewById(R.id.add_item);
                mMenuPopupWindow.showAsDropDown(view);
                DisplayUtil.backgroundAlpha(mContext, 0.5f);
            }
        } else if (flag == BusinessManageHomePresenterImpl.REQUEST_OPTION_LIST2) {
            String type = "";
            if (TextUtils.isEmpty(resultJson)) {
                type = "";
            } else {
                type = "项目商机";
            }
            startActivity(new Intent(mContext, BusinessActivity.class)
                    .putExtra(Constants.FLAG.BUSINESS_TYPE, type)
                    .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, "businessManage"));
        }
    }

    private <T extends Object> List<T> getCountList(List<T> tList, int count) {
        List<T> resultList = new ArrayList<>();
        if (tList != null) {
            for (int i = 0; i < tList.size(); i++) {
                if (i < count) {
                    T t = tList.get(i);
                    resultList.add(t);
                }
            }
        }
        return resultList;
    }

    private void analysisRank(List<BusinessRankBean> businessRankBeans) {
        mBusinessRankBeans.clear();
        mBusinessRankBeans.addAll(businessRankBeans);
        mBusinessHomeRankAdapter.notifyDataSetChanged();

        mRankRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mRankRefreshTextView.setText(mRankRefreshTime);
    }

    private void analysisOvertime(List<BusinessOverTimeBean> businessOverTimeBeans) {
        mBusinessOverTimeBeans.clear();
        mBusinessOverTimeBeans.addAll(businessOverTimeBeans);
        mBusinessHomeOvertimeAdapter.notifyDataSetChanged();

        mOvertimeRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mOvertimeRefreshTextView.setText(mOvertimeRefreshTime);
    }

    private void analysisRecord(List<BusinessRecordBean> businessRecordBeans) {
        mBusinessRecordBeans.clear();
        mBusinessRecordBeans.addAll(businessRecordBeans);
        mBusinessHomeRecordAdapter.notifyDataSetChanged();

        mRecordRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mRecordRefreshTextView.setText(mRecordRefreshTime);
    }

    private void analysisData(String resultJson) {
        mDataTimeTextView.setText(mDataDateTime);

        mDataRefreshTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
        mDataRefreshTextView.setText(mDataRefreshTime);

        JSONObject resultObject = JSON.parseObject(resultJson);
        mNewlyTextView.setText(JSONUtil.getText(resultObject, "buinessNum"));
        mChangeTextView.setText(JSONUtil.getText(resultObject, "changeNum"));
        mWinTextView.setText(JSONUtil.getText(resultObject, "winNum"));
        mLoseTextView.setText(JSONUtil.getText(resultObject, "loseNum"));
        mInvalidTextView.setText(JSONUtil.getText(resultObject, "invalidNum"));
        mFollowTextView.setText(JSONUtil.getText(resultObject, "folowNum"));
    }

    @Override
    public void requestFail(int flag, String failStr) {
        toast(failStr);
        if (flag == BusinessManageHomePresenterImpl.REQUEST_OPTION_LIST1) {
            mAddCompanyBtn.setVisibility(View.VISIBLE);
            mAddProjectBtn.setVisibility(View.GONE);
            mAddOemBtn.setVisibility(View.GONE);
        }
        if (mMenuPopupWindow != null) {
            View view = getWindow().findViewById(R.id.add_item);
            mMenuPopupWindow.showAsDropDown(view);
            DisplayUtil.backgroundAlpha(mContext, 0.5f);
        }
    }

    private void showDateDialog(String year, String month, final TextView tv) {
        OASigninPicker picker = new OASigninPicker(this, 2000, 2030, false);
        picker.setRange(2030, 12, 31);
        try {
            picker.setSelectedItem(Integer.parseInt(year), Integer.parseInt(month));
        } catch (Exception e) {
            picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth());
        }

        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                mDataDateTime = year + "年" + month + "月";
                mPresenter.getBusinessData(mContext, year + "-" + month);
            }
        });
        picker.show();
    }
}
