package com.uas.appworks.OA.platform.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.common.LogUtil;
import com.core.base.SupportToolBarActivity;
import com.modular.apputils.widget.DivideRadioGroup;
import com.uas.appworks.OA.platform.fragment.ActivityFragment;
import com.uas.appworks.OA.platform.fragment.CharitableListFragment;
import com.uas.appworks.OA.platform.fragment.UserFragment;
import com.uas.appworks.R;


public class CharitableActivity extends SupportToolBarActivity {
    private final String LIST_FRAGMENT = "listFragment";
    private final String ACTIVITY_FRAGMENT = "activityFragment";
    private final String USER_FRAGMENT = "userFragment";


    private TextView listNumTv;
    private TextView activityNumTv;
    private TextView userNumTv;

    private Fragment lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charitable);
        initView();
    }


    private void initView() {
        DivideRadioGroup selectRg = (DivideRadioGroup) findViewById(R.id.selectRg);
        listNumTv = (TextView) findViewById(R.id.listNumTv);
        activityNumTv = (TextView) findViewById(R.id.activityNumTv);
        userNumTv = (TextView) findViewById(R.id.userNumTv);
        CharitableListFragment listFragment = new CharitableListFragment();
        changeFragment(listFragment, LIST_FRAGMENT);
        selectRg.setOnCheckedChangeListener(new DivideRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DivideRadioGroup group, int checkedId) {
                Fragment fragment = null;
                String tag = null;
                if (checkedId == R.id.listRb) {
                    fragment = getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT);
                    if (fragment == null) {
                        fragment = new CharitableListFragment();
                    }
                    tag = LIST_FRAGMENT;
                } else if (checkedId == R.id.activityRb) {
                    LogUtil.i("checkedId == R.id.activityRb");
                    fragment = getSupportFragmentManager().findFragmentByTag(ACTIVITY_FRAGMENT);
                    if (fragment == null) {
                        fragment = new ActivityFragment();
                    }
                    tag = ACTIVITY_FRAGMENT;
                } else if (checkedId == R.id.userRb) {
                    fragment = getSupportFragmentManager().findFragmentByTag(USER_FRAGMENT);
                    if (fragment == null) {
                        fragment = new UserFragment();
                    }
                    tag = USER_FRAGMENT;
                }

                changeFragment(fragment, tag);

            }
        });
    }

    private void changeFragment(Fragment addFragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开始事物
        if (addFragment == null) {
            return;
        }
        if (addFragment == lastFragment) {
            return;
        }
        if (lastFragment != null && lastFragment != addFragment) {// 如果最后一次加载的不是现在要加载的Fragment，那么僵最后一次加载的移出
            fragmentTransaction.detach(lastFragment);
        }
        if (!addFragment.isAdded())// 如果还没有添加，就加上
            fragmentTransaction.add(R.id.contantFl, addFragment, tag);
        if (addFragment.isDetached())
            fragmentTransaction.attach(addFragment);
        lastFragment = addFragment;
        fragmentTransaction.commitAllowingStateLoss();
    }


}
