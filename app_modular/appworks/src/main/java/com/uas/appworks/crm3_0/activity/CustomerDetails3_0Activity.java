package com.uas.appworks.crm3_0.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.modular.apputils.activity.BillDetailsActivity;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.presenter.BillDetailsPresenter;
import com.uas.appworks.OA.erp.utils.MostLinearLayoutManager;
import com.uas.appworks.R;
import com.uas.appworks.activity.SchedulerCreateActivity;
import com.uas.appworks.crm3_0.fragment.CustomerDetailsBottomListFragment;
import com.uas.appworks.model.CustomerBindBill;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.presenter.CustomerDetailsPresenter;
import com.uas.appworks.presenter.imp.ICustomerDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户详情界面
 */
public class CustomerDetails3_0Activity extends BillDetailsActivity implements ICustomerDetails {
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private LinearLayout bottomLL;
    private TextView createScheduleTv;
    private TextView changeDoManTv;

    private String[] tabTitle;
    private final int TAB_NUM = 4;
    private SparseArray<ArrayList<CustomerBindBill>> mBottomDataList = new SparseArray<>();
    private ViewPageAdapter mAdapter;


    @Override
    public BillDetailsPresenter newBillDetailsPresenter() {
        return new CustomerDetailsPresenter(ct,this);
    }

    @Override
    public Intent getIntent() {
        Intent intent=super.getIntent();
        if (intent==null){
            intent=new Intent();
        }
        intent.putExtra(Constants.Intents.INPUT_CLASS,CustomerBillInputActivity.class);
        return intent;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_customer_details_3_0;
    }

    @Override
    protected void initView() {
        super.initView();
        tabTitle = new String[TAB_NUM];
        mTabLayout = (TabLayout) findViewById(R.id.mTabLayout);
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        bottomLL = (LinearLayout) findViewById(R.id.bottomLL);
        createScheduleTv = (TextView) findViewById(R.id.createScheduleTv);
        changeDoManTv = (TextView) findViewById(R.id.changeDoManTv);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        for (String tab : tabTitle) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tab));
        }
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mViewPager.setVisibility(View.GONE);
        String mCaller="";
        if (getIntent()!=null)
        {
            mCaller = getIntent().getStringExtra(Constants.Intents.CALLER);
        }
        if (isMe&&!StringUtil.isEmpty(mCaller)&&mCaller.equals("Customer!Base")) {
            bottomLL.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);
            createScheduleTv.setOnClickListener(mOnClickListener);
            changeDoManTv.setOnClickListener(mOnClickListener);
        } else {
            mTabLayout.setVisibility(View.GONE);
            bottomLL.setVisibility(View.GONE);
        }
        mRecyclerView.setLayoutManager(new MostLinearLayoutManager(ct));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == createScheduleTv) {
                List<BillGroupModel> mGroupModels = mBillDetailsAdapter.getBillGroupModels();
                if (!ListUtils.isEmpty(mGroupModels)) {
                    BillGroupModel mGroupModel = mGroupModels.get(0);
                    if (mGroupModel != null) {
                        if (!ListUtils.isEmpty(mGroupModel.getShowBillFields())) {
                            String mRemarks = "客户日程";
                            for (BillGroupModel.BillModel billModel : mGroupModel.getShowBillFields()) {
                                if (billModel.getCaption().equals("客户名称")) {
                                    mRemarks = billModel.getValue();
                                }
                            }
                            Schedule mSchedule = new Schedule(Schedule.TYPE_UU);
                            mSchedule.setRemarks(mRemarks);
                            mSchedule.setTag("客户日程");
                            startActivityForResult(new Intent(ct, SchedulerCreateActivity.class)
                                    .putExtra(com.uas.appworks.datainquiry.Constants.Intents.ENABLE, true)
                                    .putExtra(com.uas.appworks.datainquiry.Constants.Intents.MODEL, mSchedule), 0x11);
                        }
                    }
                }
            } else if (changeDoManTv == view) {
                //客户转
            }
        }
    };

    @Override
    public void updateStatus(String status) {
        this.status = status;
        if (status.equals("已审核")) {
            supportInvalidateOptionsMenu();
        }
    }



    @Override
    public void setBottomDatas(ArrayList<CustomerBindBill> mCusBusiness, ArrayList<CustomerBindBill> mCusContacts, ArrayList<CustomerBindBill> mCusReport, ArrayList<CustomerBindBill> mCusAddress) {
        mBottomDataList.put(0, mCusAddress);
        mBottomDataList.put(1, mCusContacts);
        mBottomDataList.put(2, mCusReport);
        mBottomDataList.put(3, mCusBusiness);
        tabTitle[0] = "客户地址 (" + ListUtils.getSize(mCusAddress) + ")";
        tabTitle[1] = "客户联系人 (" + ListUtils.getSize(mCusContacts) + ")";
        tabTitle[2] = "拜访记录 (" + ListUtils.getSize(mCusReport) + ")";
        tabTitle[3] = "客户商机 (" + ListUtils.getSize(mCusBusiness) + ")";
        mAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
    }


    private class ViewPageAdapter extends FragmentPagerAdapter {
        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            ArrayList<CustomerBindBill> bills = null;
            if (mBottomDataList != null && mBottomDataList.size() > position && mBottomDataList.get(position) != null) {
                bills = mBottomDataList.get(position);
            }
            return CustomerDetailsBottomListFragment.newInstance(isMe,mBillDetailsPresenter.getId(),position, bills);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (tabTitle != null && tabTitle.length > position && tabTitle[position] != null) {
                return tabTitle[position];
            } else {
                return "";
            }
        }

        @Override
        public int getCount() {
            return TAB_NUM;
        }
    }


}
