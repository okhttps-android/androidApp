package com.uas.appworks.crm3_0.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.core.base.EasyFragment;
import com.uas.appworks.R;

public class TestFragment extends EasyFragment {


    public static TestFragment newInstance(String text) {
        Bundle args = new Bundle();
        TestFragment fragment = new TestFragment();
        args.putString("text",text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_test;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView){
            TextView text=findViewById(R.id.text);
            if (getArguments()!=null){
                text.setText(getArguments().getString("text",""));
            }
        }
    }
}
