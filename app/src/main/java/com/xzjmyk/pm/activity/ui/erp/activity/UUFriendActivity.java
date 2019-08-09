package com.xzjmyk.pm.activity.ui.erp.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;

import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.fragment.UUFriendFragment;

/**
 * Created by pengminggong on 2016/11/7.
 */

public class UUFriendActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_common_fragment);
        boolean isPeculiar = getIntent().getBooleanExtra("isPeculiar", false);
        UUFriendFragment groupChatFrament = new UUFriendFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_content, groupChatFrament);
        fragmentTransaction.commit();
    }
}
