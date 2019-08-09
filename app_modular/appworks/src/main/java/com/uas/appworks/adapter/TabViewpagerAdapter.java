package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/15 11:02
 */

public class TabViewpagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private List<Fragment> mFragments;
    private List<String> mTitles;

    public TabViewpagerAdapter(Context context, List<Fragment> fragments, List<String> titles, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
