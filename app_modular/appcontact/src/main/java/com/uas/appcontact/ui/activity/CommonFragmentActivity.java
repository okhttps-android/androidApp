package com.uas.appcontact.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.uas.appcontact.R;
import com.uas.appcontact.ui.fragment.AutoCreateChatFragment;
import com.uas.appcontact.ui.fragment.GroupChatFragment;

public class CommonFragmentActivity extends BaseActivity {

    private FrameLayout fl_content;
    private int type;
    private int imStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_fragment);
        imStatus = getIntent().getIntExtra("imStatus", -1);
        initView();
    }


    public void initView() {
        fl_content= (FrameLayout) findViewById(R.id.fl_content);
        type = getIntent().getIntExtra("type", Constants.TYPE_CHAT_All);
        switch (type) {
            case Constants.TYPE_CHAT_All:
                GroupChatFragment groupChatFrament = new GroupChatFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fl_content, groupChatFrament);
                fragmentTransaction.commit();
               setTitle(getString(R.string.contact_business));
                break;
            case Constants.TYPE_CHAT_MANAGE:
                AutoCreateChatFragment manageChatFrament = new AutoCreateChatFragment();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fl_content, manageChatFrament);
                fragmentTransaction.commit();
            setTitle("管理群");
                break;
        }
    }

    public int getImStatus() {
        return imStatus;
    }


}
