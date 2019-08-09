package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.SubscribeManegeVpAdapter;
import com.modular.appmessages.fragment.BaseFragment;
import com.modular.appmessages.fragment.SubscriptionAllFragment;
import com.modular.appmessages.fragment.SubscriptionMyFragment;
import com.core.widget.NoSlideViewpager;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2016/9/5.
 */
public class SubcribeManageActivity extends BaseActivity{
    private TabPageIndicator mTabPageIndicator;
    private UnderlinePageIndicator mUnderlinePageIndicator;
    private SubscriptionAllFragment mSubscriptionAllFragment;
    private SubscriptionMyFragment mSubscriptionMyFragment;
    private List<Fragment> mSubscribeFragments;
    private SubscribeManegeVpAdapter mSubscribeManegeVpAdapter;
    private NoSlideViewpager mViewPager;
    private List<String> mTitleStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_manage);
        setTitle(getString(R.string.subscribe_manager));

        mTabPageIndicator = (TabPageIndicator) findViewById(R.id.subscribe_manage_tab);
        mUnderlinePageIndicator = (UnderlinePageIndicator) findViewById(R.id.subscribe_manage_undertab);
        mViewPager = (NoSlideViewpager) findViewById(R.id.subscribe_manage_vp);

        mSubscriptionAllFragment = BaseFragment.newInstance(SubscriptionAllFragment.class);
        mSubscriptionMyFragment = BaseFragment.newInstance(SubscriptionMyFragment.class);

        mSubscribeFragments = new ArrayList<>();
        mSubscribeFragments.add(mSubscriptionAllFragment);
        mSubscribeFragments.add(mSubscriptionMyFragment);

        mTitleStrings = new ArrayList<>();
        mTitleStrings.add(getString(R.string.subscribe_unsure));
        mTitleStrings.add(getString(R.string.subscribe_confirmed));
        mSubscribeManegeVpAdapter = new SubscribeManegeVpAdapter(this,mSubscribeFragments,mTitleStrings,getSupportFragmentManager());

        mViewPager.setAdapter(mSubscribeManegeVpAdapter);
        mTabPageIndicator.setViewPager(mViewPager);
        mUnderlinePageIndicator.setFades(false);
        mUnderlinePageIndicator.setViewPager(mViewPager);
        mTabPageIndicator.setOnPageChangeListener(mUnderlinePageIndicator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_apply_subscribe,menu);
        MenuItem item = menu.getItem(0);
        item.setTitle(getString(R.string.user_setting));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.subscribe_apply){
            Intent intent = new Intent();
            intent.setClass(this,ManageAllSubscriptionActivity.class);
            startActivityForResult(intent, Constants.REQUEST_MANAGE_ALL_SUBSCRIPTON);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == Constants.REQUEST_MANAGE_ALL_SUBSCRIPTON
                && resultCode == Constants.RESULT_MANAGE_ALL_SUBSCRIPTION
                && data != null){
            mTabPageIndicator.setCurrentItem(0);
            mUnderlinePageIndicator.setCurrentItem(0);
            SubscriptionAllFragment item = (SubscriptionAllFragment) mSubscribeManegeVpAdapter.getItem(0);
            item.getDbSubsData();
        }
    }
}
