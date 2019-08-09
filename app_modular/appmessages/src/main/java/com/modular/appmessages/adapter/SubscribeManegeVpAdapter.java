package com.modular.appmessages.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by RaoMeng on 2016/9/5.
 */
public class SubscribeManegeVpAdapter extends FragmentPagerAdapter{
    private Context mContext;
    private List<Fragment> mFragments;
    private List<String> mStrings;

    public SubscribeManegeVpAdapter(Context context,List<Fragment> fragments,List<String> strings,FragmentManager fm) {
        super(fm);
        mContext = context;
        mFragments = fragments;
        mStrings = strings;
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
        return mStrings.get(position);
    }
}
