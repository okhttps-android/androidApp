package com.uas.appworks.activity.businessManage.businessDetailActivity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.base.activity.MvpBaseActivity;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.utils.CommonUtil;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.uas.appworks.CRM.erp.activity.DbfindList2Activity;
import com.uas.appworks.R;
import com.uas.appworks.activity.SchedulerCreateActivity;
import com.uas.appworks.activity.businessManage.businessChangeStage.BusinessChangeStageActivity;
import com.uas.appworks.activity.businessManage.businessStage.BusinessStageActivity;
import com.uas.appworks.adapter.CommonFormDetailAdapter;
import com.uas.appworks.adapter.TabViewpagerAdapter;
import com.uas.appworks.fragment.BusinessMineListFragment;
import com.uas.appworks.fragment.BusinessRecordsFragment;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.model.bean.BusinessStageBean;
import com.uas.appworks.model.bean.CommonFormBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/18 14:07
 */
public class BusinessDetailNewActivity extends MvpBaseActivity<BusinessDetailContract.BusinessDetailPresenter>
        implements BusinessDetailContract.BusinessDetailView, View.OnClickListener {
    private static final int REQUEST_CHANGE_PRINCIPAL = 363;
    private static final int REQUEST_CHANGE_STAGE = 364;
    private static final int FILL_BUSINESS_RECORD = 365;
    private static final int REQUEST_BUSINESS_DISTRIBUTION = 366;

    private RecyclerView mMainRecyclerView;
    private CircleProgressBar mCircleProgressBar;
    private ImageView mStageImageView;
    private TextView mStageContentTextView, mStageIndexTextView,
            mAddScheduleBtn, mFollowBtn, mAddRecordBtn, mChangeStageBtn, mChangePrincipalBtn, mDistributionBtn, mReceiveBtn;
    private TabLayout mTabLayout;
    private TabViewpagerAdapter mTabViewpagerAdapter;
    private List<String> mTitleStrings;
    private List<Fragment> mFragments;
    private ViewPager mViewPager;
    private LinearLayout mBottomLayout, mOperateLayout;
    private CommonFormDetailAdapter mCommonFormDetailAdapter;
    private List<CommonFormBean> mCommonFormBeans;
    private List<BusinessStageBean> mBusinessStageBeans;
    private BusinessStageBean mCurrentBusinessStage;
    private int mId;
    private String mBctype, mStageCode, mBcCode, mBcdescription, mWhichPage;
    private BusinessMineListFragment mProductFragment;
    private BusinessRecordsFragment mRecordsFragment;
    private PopupWindow mFollowPopupWindow;
    private View mFollowView;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_detail_new;
    }

    @Override
    protected void initView() {
        setTitle(getString(R.string.str_business_detail));

        Intent intent = getIntent();
        if (intent != null) {
            mId = intent.getIntExtra("id", -1);
            mBctype = intent.getStringExtra("type");
            mBcCode = intent.getStringExtra("bc_code");
            mWhichPage = intent.getStringExtra(Constants.FLAG.COMMON_WHICH_PAGE);

            mStageCode = intent.getStringExtra("stage");
            mBcdescription = intent.getStringExtra("bc_description");
        }

        mBottomLayout = $(R.id.business_detail_new_bottom_ll);
        mStageImageView = $(R.id.business_detail_new_stage_iv);
        mOperateLayout = $(R.id.business_detail_new_operate_ll);
        mDistributionBtn = $(R.id.business_detail_new_distribution_tv);
        mReceiveBtn = $(R.id.business_detail_new_receive_tv);
        if ("businessCharge".equals(mWhichPage)) {
            mBottomLayout.setVisibility(View.VISIBLE);
            mStageImageView.setVisibility(View.VISIBLE);
            mOperateLayout.setVisibility(View.GONE);
        } else {
            mBottomLayout.setVisibility(View.GONE);
            mStageImageView.setVisibility(View.GONE);
            if ("businessCompany".equals(mWhichPage)) {
                mPresenter.requestBusinessType(this, mId, CommonUtil.getEmcode());
            }
        }
        mAddScheduleBtn = $(R.id.business_detail_new_add_schedule_tv);
        mFollowBtn = $(R.id.business_detail_new_follow_tv);
        mMainRecyclerView = $(R.id.business_detail_new_main_rv);
        mMainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMainRecyclerView.setNestedScrollingEnabled(false);
        mCommonFormBeans = new ArrayList<>();
        mCommonFormDetailAdapter = new CommonFormDetailAdapter(mCommonFormBeans);
        mMainRecyclerView.setAdapter(mCommonFormDetailAdapter);

        mStageIndexTextView = $(R.id.business_detail_new_stage_tv);
        mStageContentTextView = $(R.id.business_detail_stage_content_tv);
        mViewPager = $(R.id.business_detail_new_vp);
        mCircleProgressBar = $(R.id.business_detail_new_progress);
        mBusinessStageBeans = new ArrayList<>();

        initTablayout();

        initFollowPop();
    }

    private void initFollowPop() {
        mFollowView = View.inflate(this, R.layout.pop_business_follow_menu, null);

        mAddRecordBtn = mFollowView.findViewById(R.id.business_follow_menu1);
        mChangeStageBtn = mFollowView.findViewById(R.id.business_follow_menu2);
        mChangePrincipalBtn = mFollowView.findViewById(R.id.business_follow_menu3);

        mFollowPopupWindow = new PopupWindow(mFollowView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mFollowPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mFollowPopupWindow.setOutsideTouchable(true);
        mFollowPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mFollowPopupWindow != null) {
                    mFollowPopupWindow.dismiss();
                }
                DisplayUtil.backgroundAlpha(mContext, 1f);
            }
        });
    }

    private void initTablayout() {
        mTabLayout = $(R.id.business_detail_new_tablayout);
        mTitleStrings = new ArrayList<>();
        mTitleStrings.add(getString(R.string.follow_records));
        mTitleStrings.add(getString(R.string.connect_information));

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleStrings.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleStrings.get(1)));

        mRecordsFragment = BusinessRecordsFragment.newInstance(mId);
        mProductFragment = BusinessMineListFragment.newInstance(BusinessMineListFragment.FLAG_BUSINESS_ASSOCIATED, mBcCode);

        mFragments = new ArrayList<>();
        mFragments.add(mRecordsFragment);
        mFragments.add(mProductFragment);
        mTabViewpagerAdapter = new TabViewpagerAdapter(this, mFragments, mTitleStrings, getSupportFragmentManager());

        mViewPager.setOffscreenPageLimit(mFragments.size() - 1);
        mViewPager.setAdapter(mTabViewpagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected BusinessDetailContract.BusinessDetailPresenter initPresenter() {
        return new BusinessDetailPresenterImpl();
    }

    @Override
    protected void initEvent() {
        mStageImageView.setOnClickListener(this);
        mAddScheduleBtn.setOnClickListener(this);
        mFollowBtn.setOnClickListener(this);

        mAddRecordBtn.setOnClickListener(this);
        mChangeStageBtn.setOnClickListener(this);
        mChangePrincipalBtn.setOnClickListener(this);

        mReceiveBtn.setOnClickListener(this);
        mDistributionBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if ("项目商机".equals(mBctype)) {
            mPresenter.requestMainDetail(this, mId, "ProjectBusinessChance");
        } else if ("OEM商机".equals(mBctype)) {
            mPresenter.requestMainDetail(this, mId, "OEMBusinessChance");
        } else {
            mPresenter.requestMainDetail(this, mId, "BusinessChance");
        }
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
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.business_detail_new_stage_iv) {
            startActivityForResult(new Intent(mContext, BusinessStageActivity.class)
                    .putExtra("stageBeans", (Serializable) mBusinessStageBeans)
                    .putExtra("currentStage", mCurrentBusinessStage)
                    .putExtra("stageCode", mStageCode)
                    .putExtra("bc_code", mBcCode)
                    .putExtra("bc_type", mBctype), REQUEST_CHANGE_STAGE);
        } else if (i == R.id.business_detail_new_add_schedule_tv) {
            /*String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
            if (StringUtil.isEmpty(emname)) {
                emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
            }
            mPresenter.requestScheduleList(this, mBcCode, emname);*/
            Schedule mSchedule = new Schedule(Schedule.TYPE_UU);
            mSchedule.setRemarks(mBcdescription);
            mSchedule.setTag("商机日程");

            startActivityForResult(new Intent(ct, SchedulerCreateActivity.class)
                    .putExtra(com.uas.appworks.datainquiry.Constants.Intents.ENABLE, true)
                    .putExtra(com.uas.appworks.datainquiry.Constants.Intents.MODEL, mSchedule), 0x11);
        } else if (i == R.id.business_detail_new_follow_tv) {
            if (mFollowPopupWindow != null && mFollowView != null) {
                mFollowView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int measuredWidth = mFollowView.getMeasuredWidth();
                int measuredHeight = mFollowView.getMeasuredHeight();
                int[] location = new int[2];
                mFollowBtn.getLocationOnScreen(location);
                mFollowPopupWindow.showAtLocation(mFollowBtn, Gravity.NO_GRAVITY,
                        (location[0] + mFollowBtn.getWidth() / 2) - measuredWidth / 2,
                        location[1] - measuredHeight);
                DisplayUtil.backgroundAlpha(mContext, 0.5f);
            }
        } else if (i == R.id.business_follow_menu1) {
            closeFollowMenu();
            startActivityForResult(new Intent(mContext, BusinessChangeStageActivity.class)
                    .putExtra("nextStage", mCurrentBusinessStage)
                    .putExtra("currentStage", mCurrentBusinessStage)
                    .putExtra("whichPage", "businessDetail")
                    .putExtra("bc_code", mBcCode), FILL_BUSINESS_RECORD);
        } else if (i == R.id.business_follow_menu2) {
            closeFollowMenu();
            startActivityForResult(new Intent(mContext, BusinessStageActivity.class)
                    .putExtra("stageBeans", (Serializable) mBusinessStageBeans)
                    .putExtra("currentStage", mCurrentBusinessStage)
                    .putExtra("stageCode", mStageCode)
                    .putExtra("bc_code", mBcCode)
                    .putExtra("bc_type", mBctype), REQUEST_CHANGE_STAGE);
        } else if (i == R.id.business_follow_menu3) {
            closeFollowMenu();
            Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
            SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                    .setSureText(getString(R.string.common_sure))
                    .setSelectType("负责人")
                    .setTitle(getString(R.string.select_user))
                    .setSingleAble(true)
                    .setReBackSelect(true);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, REQUEST_CHANGE_PRINCIPAL);
        } else if (i == R.id.business_detail_new_receive_tv) {
            mPresenter.canBusinessReceive(this);
        } else if (i == R.id.business_detail_new_distribution_tv) {
            startActivityForResult(new Intent(ct, DbfindList2Activity.class)
                    , REQUEST_BUSINESS_DISTRIBUTION);
        } else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_CHANGE_PRINCIPAL) {
            SelectEmUser user = data.getParcelableExtra("data");
            if (user != null && !StringUtil.isEmpty(user.getEmCode())) {
                mPresenter.changeDoman(mContext, user, mBcCode);
            }
        } else if (requestCode == REQUEST_BUSINESS_DISTRIBUTION) {
            String bt_doman = data.getStringExtra("en_name");
            String en_code = data.getStringExtra("en_code");
            mPresenter.requestBusinessReceive(this, 1, mBcCode, bt_doman, en_code);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void closeFollowMenu() {
        if (mFollowPopupWindow != null) {
            mFollowPopupWindow.dismiss();
        }
        DisplayUtil.backgroundAlpha(mContext, 1f);
    }

    @Override
    public void requestMainDetailSuccess(List<CommonFormBean> allDetailList, List<CommonFormBean> visibleDetailList) {
        for (int i = 0; i < allDetailList.size(); i++) {
            CommonFormBean commonFormBean = allDetailList.get(i);
            if (commonFormBean != null) {
                String field = commonFormBean.getField();
                if ("bc_currentprocesscode".equals(field)) {
                    mStageCode = commonFormBean.getValue();
                    break;
                }
            }
        }

        mPresenter.requestStageList(this);

        mCommonFormBeans.addAll(visibleDetailList);
        mCommonFormDetailAdapter.notifyDataSetChanged();
    }

    @Override
    public void requestStageSuccess(List<BusinessStageBean> businessStageBeans) {
        if (businessStageBeans != null) {
            for (int i = 0; i < businessStageBeans.size(); i++) {
                BusinessStageBean businessStageBean = businessStageBeans.get(i);
                if (businessStageBean != null) {
                    if (mBctype != null && mBctype.equals(businessStageBean.getBS_TYPE())) {
                        mBusinessStageBeans.add(businessStageBean);
                    }
                }
            }
        }
        if (mBusinessStageBeans.size() == 0) {
            mStageImageView.setVisibility(View.GONE);
        }

        boolean isExist = false;
        if (mBusinessStageBeans != null && mBusinessStageBeans.size() > 0) {
            mCircleProgressBar.setMax(mBusinessStageBeans.size());
            for (int i = 0; i < mBusinessStageBeans.size(); i++) {
                BusinessStageBean businessStageBean = mBusinessStageBeans.get(i);

                if (mStageCode != null && mStageCode.equals(businessStageBean.getBS_CODE())) {
                    String bs_point = businessStageBean.getBS_POINT();
                    if (!TextUtils.isEmpty(bs_point)) {
                        String[] split = bs_point.split("#");

                        if (split != null && split.length > 0) {
                            String pointStr = "";
                            for (int j = 0; j < split.length; j++) {
                                pointStr += ((j + 1) + "、" + split[j] + "\n");
                            }
                            if (pointStr.length() > 1) {
                                pointStr = pointStr.substring(0, pointStr.length() - 1);
                            }
                            mStageContentTextView.setText(pointStr);
                        }
                    }
                    mCurrentBusinessStage = businessStageBean;
                    int stageIndex = i + 1;
                    mCircleProgressBar.setProgress(stageIndex);
                    try {
//                        mStageIndexTextView.setText("第" +
//                                NumberUtils.translateNumber2Chinese(businessStageBean.getBS_DETNO()) + "阶段");
                        mStageIndexTextView.setText(businessStageBean.getBS_NAME());
                    } catch (Exception e) {
                        mStageIndexTextView.setText(businessStageBean.getBS_NAME());
                    }

                    isExist = true;
                    break;
                }

            }
        }

        if (!isExist) {
            mStageIndexTextView.setText(R.string.stage_empty);
            mStageContentTextView.setText("");
        }
    }

    @Override
    public void changeDomanSuccess() {
        toast(getString(R.string.change_stage_success));
    }

    @Override
    public void requestScheduleListSuccess(JSONArray jsonArray) {
        if (jsonArray == null) {
            toast(getString(R.string.schedule_request_fail));
            return;
        }
        if (jsonArray.size() > 0) {
            toast("不能重复添加到日程！");
        } else {
            Intent intent = new Intent("com.modular.appworks.TaskAddActivity");
            intent.putExtra("type", 1);
            intent.putExtra("from", "BusinessDetailInfo");
            intent.putExtra("data", mBcCode);
            intent.putExtra("bc_doman", CommonUtil.getName());
            intent.putExtra("bc_custname", mBcdescription);
            startActivityForResult(intent, 0x11);
        }

    }

    @Override
    public void requestBusinessTypeSuccess(boolean isGra, boolean isDis) {
        if (isGra) {
            mOperateLayout.setVisibility(View.VISIBLE);
            mReceiveBtn.setVisibility(View.VISIBLE);
        }
        if (isDis) {
            mOperateLayout.setVisibility(View.VISIBLE);
            mDistributionBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void canBusinessReceiveSuccess() {
        mPresenter.requestBusinessReceive(this, 0, mBcCode
                , CommonUtil.getSharedPreferences(ct, "erp_emname"),
                CommonUtil.getSharedPreferences(ct, "erp_username"));
    }

    @Override
    public void requestBusinessReceiveSuccess(int type) {
        String result = getString(R.string.qiang_business_success) + ","
                + getString(R.string.business_notice1);
        if (type == 0) {
            result = getString(R.string.qiang_business_success) + ","
                    + getString(R.string.business_notice1);
        } else if (type == 1) {
            result = getString(R.string.split_business_success);
        }
        toast(result);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!BusinessDetailNewActivity.this.isDestroyed()) {
                    finish();
                }
            }
        }, 1500);
    }

    @Override
    public void requestFail(int flag, String failStr) {
//        switch (flag) {
//            case BusinessDetailPresenterImpl.REQUEST_BUSINESS_DETAIL:
        toast(failStr);
//                break;
//        }
    }
}
