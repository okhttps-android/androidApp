package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.core.base.SupportToolBarActivity;
import com.core.widget.listener.EditChangeListener;
import com.modular.appmessages.R;
import com.modular.appmessages.fragment.ApprovalListFragment;
import com.modular.appmessages.fragment.TaskListFragment;
import com.modular.apputils.widget.MenuVoiceSearchView;
import com.uas.appworks.CRM.erp.activity.TaskAddErpActivity;

public class TaskNewActivity extends SupportToolBarActivity {
    private String[] tabTitle;
    private TaskListFragment mCurrentFragment;
    private TaskListFragment daibanFragment, passFragment, pushFragment;
    private ViewPager mViewPager;
    private MenuVoiceSearchView mVoiceSearchView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabTitle = new String[]{getString(R.string.task_wait_todo), getString(R.string.task_done_deal), getString(R.string.task_request_me)};
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
        TabLayout mTabLayout = findViewById(R.id.mTabLayout);
        mViewPager = findViewById(R.id.mViewPager);
        ImageView addImg = findViewById(R.id.addImg);
        mVoiceSearchView = findViewById(R.id.mVoiceSearchView);
        findViewById(R.id.tabUnReadNum).setVisibility(View.GONE);
        mVoiceSearchView.addTextChangedListener(mEditChangeListener);
        findViewById(R.id.backImg).setOnClickListener(mOnClickListener);
        addImg.setVisibility(View.VISIBLE);
        addImg.setOnClickListener(mOnClickListener);


        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[0]));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[1]));
        mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[2]));
        ViewPageAdapter mAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
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
            }else if (view.getId()==R.id.addImg){
                startActivityForResult(new Intent(ct, TaskAddErpActivity.class), 0x17);
            }
        }
    };


    private class ViewPageAdapter extends FragmentPagerAdapter {

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (object != null && object instanceof TaskListFragment) {
                mCurrentFragment = (TaskListFragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            TaskListFragment fragment = null;
            int tabItem = position + 1;
            switch (position) {
                case 0:
                    if (daibanFragment == null) {
                        daibanFragment = TaskListFragment.newInstance(tabItem);
                        daibanFragment.setLoadedListener(mLoadedListener);
                    }
                    fragment = daibanFragment;
                    break;
                case 1:
                    if (passFragment == null) {
                        passFragment = TaskListFragment.newInstance(tabItem);
                        passFragment.setLoadedListener(mLoadedListener);
                    }
                    fragment = passFragment;
                    break;
                case 2:
                    if (pushFragment == null) {
                        pushFragment = TaskListFragment.newInstance(tabItem);
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
