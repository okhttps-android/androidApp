package com.uas.appworks.activity.businessManage.businessMineList;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.core.app.Constants;
import com.core.base.activity.MvpBaseActivity;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.BusinessCompanyListActivity;
import com.uas.appworks.adapter.TabViewpagerAdapter;
import com.uas.appworks.fragment.BusinessMineListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/11 17:22
 */
public class BusinessMineListActivity extends MvpBaseActivity {
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TabViewpagerAdapter mTabViewpagerAdapter;
    private List<String> mTitleStrings;
    private List<Fragment> mFragments;
    private BusinessMineListFragment mChargeFragment, mBranchFragment;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_mine_list;
    }

    @Override
    protected void initView() {
        setTitle("我的商机");

        mTabLayout = $(R.id.business_mine_list_tl);
        mViewPager = $(R.id.business_mine_list_vp);

        mTitleStrings = new ArrayList<>();
        mTitleStrings.add("我负责的");
        mTitleStrings.add("我下属的");

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleStrings.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleStrings.get(1)));

        mChargeFragment = BusinessMineListFragment.newInstance(BusinessMineListFragment.FLAG_BUSINESS_CHARGE);
        mBranchFragment = BusinessMineListFragment.newInstance(BusinessMineListFragment.FLAG_BUSINESS_BRANCH);

        mFragments = new ArrayList<>();
        mFragments.add(mChargeFragment);
        mFragments.add(mBranchFragment);
        mTabViewpagerAdapter = new TabViewpagerAdapter(this, mFragments, mTitleStrings, getSupportFragmentManager());

        mViewPager.setOffscreenPageLimit(mFragments.size() - 1);
        mViewPager.setAdapter(mTabViewpagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected BusinessMineListContract.IBusinessMineListPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.search) {
            int selectedTabPosition = mTabLayout.getSelectedTabPosition();
            if (selectedTabPosition == 0) {
                startActivity(new Intent(mContext, BusinessCompanyListActivity.class)
                        .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, BusinessCompanyListActivity.PAGE_BUSINESS_CHARGE));
            } else if (selectedTabPosition == 1) {
                startActivity(new Intent(mContext, BusinessCompanyListActivity.class)
                        .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, BusinessCompanyListActivity.PAGE_BUSINESS_BRANCH));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}
