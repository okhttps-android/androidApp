package com.modular.appmessages.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;

import com.core.base.BaseActivity;
import com.core.widget.listener.EditChangeListener;
import com.modular.appmessages.R;
import com.modular.appmessages.fragment.ApprovalListFragment;
import com.modular.apputils.widget.MenuVoiceSearchView;

public class ApprovalNewListActivity extends BaseActivity {
    private  String[] tabTitle ;
    private ApprovalListFragment mCurrentFragment;
    private ApprovalListFragment daibanFragment, passFragment, pushFragment;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MenuVoiceSearchView mVoiceSearchView;
    private AppCompatTextView tabUnReadNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabTitle = new String[]{getString(R.string.status_pending), getString(R.string.task_confimed), getString(R.string.task_request_me)};
        initView();

    }


    @Override
    public int getToolBarId() {
        return R.id.cycleCountToolBar;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_approval_list;
    }

    private void initView() {
        mTabLayout = findViewById(R.id.mTabLayout);
        mViewPager = findViewById(R.id.mViewPager);
        findViewById(R.id.addImg).setVisibility(View.GONE);
        mVoiceSearchView = findViewById(R.id.mVoiceSearchView);
        tabUnReadNum = findViewById(R.id.tabUnReadNum);
        mVoiceSearchView.addTextChangedListener(mEditChangeListener);
        findViewById(R.id.backImg).setOnClickListener(mOnClickListener);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[0]));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[1]));
        mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[2]));
        ViewPageAdapter mAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mVoiceSearchView.setText("");
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    private EditChangeListener mEditChangeListener = new EditChangeListener() {
        @Override
        public void afterTextChanged(Editable editable) {
            if (isUpdateText) {
                isUpdateText = false;
                return;
            }
            String input = editable == null ? "" : editable.toString();
            if (mCurrentFragment != null) {
                mCurrentFragment.searchByKey(input);
            }
        }
    };
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.backImg) {
                onBackPressed();
            }
        }
    };
   

    private class ViewPageAdapter extends FragmentPagerAdapter {

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (object != null && object instanceof ApprovalListFragment) {
                mCurrentFragment = (ApprovalListFragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            ApprovalListFragment fragment = null;
            int tabItem = position + 1;
            switch (position) {
                case 0:
                    if (daibanFragment == null) {
                        daibanFragment = ApprovalListFragment.newInstance(tabItem);
                        daibanFragment.setLoadedListener(mLoadedListener);
                        daibanFragment.setUpdateNumListener(new ApprovalListFragment.UpdateNumListener() {
                            @Override
                            public void update(int num) {
                                if (tabUnReadNum != null) {
                                    if (num<=0){
                                        tabUnReadNum.setVisibility(View.GONE);
                                    }else{
                                        tabUnReadNum.setVisibility(View.VISIBLE);
                                        tabUnReadNum.setText(String.valueOf(num));
                                    }
                                }
                            }
                        });
                    }
                    fragment = daibanFragment;
                    break;
                case 1:
                    if (passFragment == null) {
                        passFragment = ApprovalListFragment.newInstance(tabItem);
                        passFragment.setLoadedListener(mLoadedListener);
                    }
                    fragment = passFragment;
                    break;
                case 2:
                    if (pushFragment == null) {
                        pushFragment = ApprovalListFragment.newInstance(tabItem);
                        pushFragment.setLoadedListener(mLoadedListener);
                    }
                    fragment = pushFragment;
                    break;
            }

            return fragment;
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

    private boolean isUpdateText;
    private ApprovalListFragment.LoadedListener mLoadedListener = new ApprovalListFragment.LoadedListener() {
        @Override
        public void loaded() {
            isUpdateText = true;
            mVoiceSearchView.setText("");
        }
    };
}
