package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.fragment.OutofficeFragment;
import com.xzjmyk.pm.activity.ui.erp.fragment.OutofficePlayFragment;

public class OutofficeActivity extends BaseActivity {
    private RadioButton sin_rb;

    private OutofficeFragment outofficeFragment;
    private FragmentManager manager;
    private OutofficePlayFragment outofficePlayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outoffice);
        initView();
    }

    private void initView() {
        sin_rb = (RadioButton) findViewById(R.id.sin_rb);
        outofficeFragment = new OutofficeFragment();
        manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_fl, outofficeFragment).commit();
        sin_rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    manager.beginTransaction().replace(R.id.content_fl, outofficeFragment).commit();
                } else {
                    if (outofficePlayFragment == null) {
                        outofficePlayFragment = new OutofficePlayFragment();
                    }
                    manager.beginTransaction().replace(R.id.content_fl, outofficePlayFragment).commit();
                }
            }
        });
    }


}
