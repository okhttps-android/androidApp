package com.uas.appworks.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;

import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.core.widget.SearchActionView;
import com.uas.appworks.R;
import com.uas.appworks.adapter.TabViewpagerAdapter;
import com.uas.appworks.fragment.B2BBusinessListFragment;
import com.uas.appworks.model.bean.B2BBusinessListBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 公共询价单列表页面
 * @date 2018/1/15 10:09
 */

public class PublicInquiryListActivity extends BaseMVPActivity<WorkPlatPresenter> {

    private TabPageIndicator mTabPageIndicator;
    private UnderlinePageIndicator mUnderlinePageIndicator;
    private List<Fragment> mFragments;
    private TabViewpagerAdapter mTabViewpagerAdapter;
    private ViewPager mViewPager;
    private List<String> mTitleStrings;
    private SearchActionView mSearchActionView;

    private B2BBusinessListFragment mAllFragment, mDoneFragment;
    private String mKeyWord;

    @Override
    protected int getLayout() {
        return R.layout.activity_tab_viewpager;
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    @Override
    protected void initView() {
        setTitle("");
        mSearchActionView = new SearchActionView(mContext);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(mSearchActionView);
        mSearchActionView.setSearchHint(getString(R.string.str_public_inquiry_list));

        mTabPageIndicator = (TabPageIndicator) findViewById(R.id.tab_viewpager_tab);
        mUnderlinePageIndicator = (UnderlinePageIndicator) findViewById(R.id.tab_viewpager_undertab);
        mViewPager = (ViewPager) findViewById(R.id.tab_viewpager_vp);

        mAllFragment = new B2BBusinessListFragment();
        mDoneFragment = new B2BBusinessListFragment();

        mAllFragment.setListType(B2BBusinessListBean.PUBLIC_INQUIRY_LIST);
        mAllFragment.setListState("");
        mDoneFragment.setListType(B2BBusinessListBean.PUBLIC_INQUIRY_LIST);
        mDoneFragment.setListState("done");

        mFragments = new ArrayList<>();
        mFragments.add(mAllFragment);
        mFragments.add(mDoneFragment);

        mTitleStrings = new ArrayList<>();
        mTitleStrings.add(getString(R.string.all));
        mTitleStrings.add(getString(R.string.str_quoted_price));
        mTabViewpagerAdapter = new TabViewpagerAdapter(this, mFragments, mTitleStrings, getSupportFragmentManager());

        mViewPager.setOffscreenPageLimit(mFragments.size() - 1);
        mViewPager.setAdapter(mTabViewpagerAdapter);
        mTabPageIndicator.setViewPager(mViewPager);
        mUnderlinePageIndicator.setFades(false);
        mUnderlinePageIndicator.setViewPager(mViewPager);
        mTabPageIndicator.setOnPageChangeListener(mUnderlinePageIndicator);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {

        mSearchActionView.setOnEnterActionListener(new SearchActionView.OnEnterActionListener() {
            @Override
            public void onEnterAction() {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mKeyWord = mSearchActionView.getText();
                    obtainListData();
                } else {
                    toast(R.string.networks_out);
                }
            }
        });

        mSearchActionView.setOnVoiceCompleteListener(new SearchActionView.OnVoiceCompleteListener() {
            @Override
            public void onVoiceComplete(String text) {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mKeyWord = mSearchActionView.getText();
                    obtainListData();
                } else {
                    toast(R.string.networks_out);
                }
            }
        });

        mSearchActionView.setOnTextChangedListener(new SearchActionView.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(mKeyWord)) {
                    if (CommonUtil.isNetWorkConnected(mContext)) {
                        mKeyWord = "";
                        obtainListData();
                    }
                }
            }
        });
    }

    private void obtainListData() {
        mAllFragment.getListData(1, mKeyWord);
        mDoneFragment.getListData(1, mKeyWord);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}
