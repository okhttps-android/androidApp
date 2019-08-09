package com.xzjmyk.pm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

/**
 * Created by RaoMeng on 2016/10/24.
 */
public class CustomCrashActivity extends Activity {

    private Button mRestartBtn, mCloseBtn;
    private ImageView mErrorIv;
    private Intent intent;
    private String stackTraceString;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crash);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mCloseBtn = (Button) findViewById(R.id.close_btn);
        mErrorIv = (ImageView) findViewById(R.id.error_image);
        intent = getIntent();
        stackTraceString = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, intent);

        new MaterialDialog.Builder(this).content(stackTraceString)
                    .build().show();

        HttpClient httpClient = new HttpClient.Builder("http://10.1.80.56:8085/")
                .isDebug(true)
                .build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mail/errorInfo")
                .add("message", stackTraceString)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
               
               
            }
        }));
        
        final Class<? extends Activity> activityClassFromIntent = CustomActivityOnCrash.getRestartActivityClassFromIntent(intent);
        final CustomActivityOnCrash.EventListener listenerFromIntent = CustomActivityOnCrash.getEventListenerFromIntent(intent);

        mRestartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomCrashActivity.this, activityClassFromIntent);
                CustomActivityOnCrash.restartApplicationWithIntent(CustomCrashActivity.this, intent, listenerFromIntent);
            }
        });

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomActivityOnCrash.closeApplication(CustomCrashActivity.this, listenerFromIntent);
            }
        });
    }
}