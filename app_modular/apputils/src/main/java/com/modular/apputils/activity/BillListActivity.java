package com.modular.apputils.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.base.OABaseActivity;
import com.modular.apputils.R;
import com.modular.apputils.fragment.BIllListFragment;
import com.modular.apputils.model.BillConfig;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.model.BillListGroupModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BillListActivity extends OABaseActivity implements BIllListFragment.OnBillListListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private List<BillListConfig> billListConfigList;//需要显示的列表,必须传
    private Class mDetailsClass;//进入详情界面
    private Class mInputClass;//返回编辑界面
    private HashMap<String, String> dbfindCondition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            billListConfigList = intent.getParcelableArrayListExtra(Constants.Intents.CONFIG);
            String mTitle = intent.getStringExtra(Constants.Intents.TITLE);
            Serializable mDetailsSerializable = intent.getSerializableExtra(Constants.Intents.DETAILS_CLASS);
            if (mDetailsSerializable != null && mDetailsSerializable instanceof Class) {
                mDetailsClass = (Class) mDetailsSerializable;
            }
            Serializable mSerializable = intent.getSerializableExtra(Constants.Intents.DB_FIND_CONDITION);
            if (mSerializable != null && mSerializable instanceof HashMap) {
                dbfindCondition = (HashMap<String, String>) mSerializable;
            }

            Serializable mInputSerializable = intent.getSerializableExtra(Constants.Intents.INPUT_CLASS);
            if (mInputSerializable != null && mInputSerializable instanceof Class) {
                mInputClass = (Class) mInputSerializable;
            }
            if (mTitle != null) {
                setTitle(mTitle);
            }
        }
        mTabLayout = findViewById(R.id.mTabLayout);
        mViewPager = findViewById(R.id.mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        ViewPageAdapter mAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        if (ListUtils.getSize(billListConfigList) <= 1) {
            mTabLayout.setVisibility(View.GONE);
        }
    }


    private class ViewPageAdapter extends FragmentStatePagerAdapter {
        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            BillListConfig billConfig = billListConfigList.get(position);
            return BIllListFragment.newInstance(billConfig,mDetailsClass)
                    .setOnBillListListener(BillListActivity.this);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (ListUtils.getSize(billListConfigList) > position) {
                return billListConfigList.get(position).getTitle();
            } else {
                return "hide";
            }
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(billListConfigList);
        }
    }

    @Override
    public void itemClick(BillListConfig billConfig, int formId, BillListGroupModel mBillListGroupModel) {
        if (mBillListGroupModel.getStatus().equals("在录入")) {
            startActivity(new Intent(ct, mInputClass != null ? mInputClass : BillInputActivity.class)
                    .putExtra(Constants.Intents.CALLER, billConfig.getCaller())
                    .putExtra(Constants.Intents.MY_DOIT, billConfig.isMe())
                    .putExtra(Constants.Intents.DB_FIND_CONDITION, dbfindCondition)
                    .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                    .putExtra(Constants.Intents.ID, mBillListGroupModel.getId()));
        } else if (mDetailsClass != null) {
            ArrayList<BillListGroupModel.BillListField> fields = new ArrayList<>();
            if (billConfig.isNeedForward()) {
                if (!ListUtils.isEmpty(mBillListGroupModel.getBillFields())) {
                    fields.addAll(mBillListGroupModel.getBillFields());
                }
                if (!ListUtils.isEmpty(mBillListGroupModel.getHideBillFields())) {
                    fields.addAll(mBillListGroupModel.getHideBillFields());
                }
            }
            startActivity(new Intent(ct, mDetailsClass)
                    .putExtra(Constants.Intents.CALLER, billConfig.getCaller())
                    .putExtra(Constants.Intents.DB_FIND_CONDITION, dbfindCondition)
                    .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                    .putExtra(Constants.Intents.MY_DOIT, billConfig.isMe())
                    .putExtra(Constants.Intents.INPUT_CLASS, getToolBarTitle())
                    .putExtra(Constants.Intents.BILL_LIST_FIELD_FORWARD, fields)
                    .putExtra(Constants.Intents.ID, mBillListGroupModel.getId()));
        } else {
            //进入通用详情界面
            startActivity(new Intent("com.modular.form.erp.activity.CommonDocDetailsActivity")
                    .putExtra("caller", billConfig.getCaller())
                    .putExtra("keyValue", mBillListGroupModel.getId())
                    .putExtra("update", "1")
                    .putExtra(Constants.Intents.MY_DOIT, billConfig.isMe())
                    .putExtra("title", getToolBarTitle())
                    .putExtra("statusKey", "")
                    .putExtra("status", "已提交"));
        }
    }

}
