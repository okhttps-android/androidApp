package com.uas.appworks.crm3_0.activity;


import android.content.Intent;
import android.graphics.PixelFormat;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.common.data.TextUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;

import com.modular.apputils.widget.DivideRadioGroup;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.fragment.CustomerListFragment;

import java.util.Map;

/**
 * 客户列表界面
 */
public class CustomerListActivity extends BaseActivity {
    private final String LIST = "List";
    private final String MAP = "Map";

    private CustomerListFragment mCurrentFragment;
    private String mCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list_3_0);
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mCurrentFragment.onItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        if (getIntent() != null) {
            String title = getIntent().getStringExtra(Constants.Intents.TITLE);
            mCaller = getIntent().getStringExtra(Constants.Intents.CALLER);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title + "列表");
            } else {
                setTitle("列表");
            }
        }
        if (TextUtils.isEmpty(mCaller)) {
            mCaller = "Customer!Base";
        }
        DivideRadioGroup tabBottomRg = findViewById(R.id.tabBottomRg);
        changeFragment(LIST);
        tabBottomRg.setVisibility(View.GONE);
        tabBottomRg.setOnCheckedChangeListener(new DivideRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DivideRadioGroup group, int checkedId) {
                String tag = null;
                if (R.id.customerListRb == checkedId) {
                    tag = LIST;
                } else if (R.id.customerLocationRb == checkedId) {
                    tag = MAP;
                }
                changeFragment(tag);
            }
        });
    }

    private void changeFragment(String tag) {
        CustomerListFragment addFragment = getAddFragment(tag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开始事物
        if (addFragment == null) {
            return;
        }
        if (addFragment == mCurrentFragment) {
            return;
        }
        if (mCurrentFragment != null && mCurrentFragment != addFragment) {// 如果最后一次加载的不是现在要加载的Fragment，那么僵最后一次加载的移出
            fragmentTransaction.detach(mCurrentFragment);
        }
        if (!addFragment.isAdded())// 如果还没有添加，就加上
            fragmentTransaction.add(R.id.customerFl, addFragment, tag);
        if (addFragment.isDetached())
            fragmentTransaction.attach(addFragment);
        mCurrentFragment = addFragment;
        fragmentTransaction.commitAllowingStateLoss();
    }


    //    获取当前的fragment
    private CustomerListFragment getAddFragment(String mTag) {
        Fragment mAddFragment = getSupportFragmentManager().findFragmentByTag(mTag);
        if (mAddFragment == null) {
            if (mTag.equals(MAP)) {
                mAddFragment = CustomerListFragment.newInstance(2, mCaller);
            } else {
                mAddFragment = CustomerListFragment.newInstance(1, mCaller);
            }
        }
        if (mAddFragment instanceof CustomerListFragment) {
            return (CustomerListFragment) mAddFragment;
        } else return null;
    }


    private void changeTab(String mTag) {
        CustomerListFragment mAddFragment = getAddFragment(mTag);
        if (mCurrentFragment != mAddFragment) {
            FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
            if (mCurrentFragment != null) {
                mTransaction.hide(mCurrentFragment);
            }
            if (!mAddFragment.isAdded()) {
                mTransaction.add(R.id.customerFl, mAddFragment, mTag);
            } else {
                mTransaction.show(mAddFragment);
            }
            mCurrentFragment = mAddFragment;
            mTransaction.commitAllowingStateLoss();
        }
    }


}
