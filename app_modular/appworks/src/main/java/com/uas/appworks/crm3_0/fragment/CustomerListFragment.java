package com.uas.appworks.crm3_0.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.common.LogUtil;
import com.core.base.EasyFragment;
import com.core.utils.CommonUtil;
import com.uas.appworks.R;

/**
 * 客户列表|客户地图列表
 */
public class CustomerListFragment extends EasyFragment {

    private String[] tabTitle;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private int type;//当前类型，1.列表界面  2.地图界面
    private String mCaller;//当前类型，1.列表界面  2.地图界面

    public static CustomerListFragment newInstance(int type, String mCaller) {
        Bundle args = new Bundle();
        CustomerListFragment fragment = new CustomerListFragment();
        args.putInt("type", type);
        args.putString("Caller", mCaller);
        fragment.setArguments(args);
        return fragment;
    }

    public void onItemSelected(MenuItem item) {
        if (lastFragment != null) {
            if (lastFragment instanceof CustomerMapDetailsFragment) {
                ((CustomerMapDetailsFragment) lastFragment).onItemSelected(item);
            } if (lastFragment instanceof CustomerListDetailsFragment) {
                ((CustomerListDetailsFragment) lastFragment).onItemSelected(item);
            }
        }

    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_customer_3_0;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            type = 1;
            if (getArguments() != null) {
                type = getArguments().getInt("type", 1);
                mCaller = getArguments().getString("Caller", "Customer!Base");
            }
            if (TextUtils.isEmpty(mCaller)) {
                mCaller = "Customer!Base";
            }
            LogUtil.i("gong", "type=" + type);
            initView(getmRootView());
        }
    }

    private void initView(View rootView) {
        tabTitle = new String[]{getString(R.string.my_responsible), getString(R.string.my_subordinate)};
        mTabLayout = findViewById(R.id.mTabLayout);
        mViewPager = findViewById(R.id.mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        for (String tab : tabTitle) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tab));
        }
        ViewPageAdapter mAdapter = new ViewPageAdapter(getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。

    }

    private Fragment lastFragment;

    private class ViewPageAdapter extends FragmentStatePagerAdapter {

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (object != null && object instanceof Fragment) {
                lastFragment = (Fragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            String mCondition;
            boolean isMe = false;
            if (position == 0) {
                mCondition = "cu_sellercode=\'" + CommonUtil.getEmcode() + "\'";
                isMe = true;
            } else {
                mCondition = "(\n" +
                        "cu_sellercode in (\n" +
                        " select em_code from employee left join job on em_defaulthsid=jo_id where jo_subof=\n" +
                        " (select em_defaulthsid from employee where em_code = \'" + CommonUtil.getEmcode() + "\')" +
                        ")\n" +
                        ")";
            }
            if (type == 2) {//地图界面
                return CustomerMapDetailsFragment.newInstance(isMe, mCaller, mCondition);
            } else {//列表界面
                return CustomerListDetailsFragment.newInstance(isMe, mCaller, mCondition);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitle[position];
        }

        @Override
        public int getCount() {
            return tabTitle.length;
        }
    }
}
