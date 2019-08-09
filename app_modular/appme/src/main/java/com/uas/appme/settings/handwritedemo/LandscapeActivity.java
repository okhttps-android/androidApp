package com.uas.appme.settings.handwritedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.core.base.SupportToolBarActivity;
import com.uas.appme.R;
import com.uas.appme.settings.activity.SigatureActivity;
import com.uas.appme.settings.activity.WagesDetailsActivity;

import java.io.IOException;

public class LandscapeActivity extends SupportToolBarActivity implements View.OnClickListener {
   private LinePathView pathView;
    private int signCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_write);
        setResult(50);
        initView();
    }

    private void initView(){
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.clear1).setOnClickListener(this);
        findViewById(R.id.save1).setOnClickListener(this);
        pathView = (LinePathView) findViewById(R.id.view);
        pathView.setPaintWidth(5);

        signCode = getIntent().getIntExtra("signCode",-1);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.clear1){
            pathView.clear();
        }else if (v.getId()== R.id.back){
            finish();
        }
        else if (v.getId() == R.id.save1){
            if (pathView.getTouched())
            {
                try {
                    switch (signCode){
                        case 001:
                            pathView.save(WagesDetailsActivity.plainpath,false,10);
                            break;
                        case 002:
                            pathView.save(SigatureActivity.path1,false,10);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                setResult(101);
                finish();
            }else {
                Toast.makeText(LandscapeActivity.this,"您没有签名~", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

