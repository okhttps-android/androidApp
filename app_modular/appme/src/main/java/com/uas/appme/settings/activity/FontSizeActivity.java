package com.uas.appme.settings.activity;

import android.content.Intent;
import android.os.Bundle;

import com.common.LogUtil;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.uas.appme.R;
import com.uas.appme.widget.SetTextSizeView;

public class FontSizeActivity extends SupportToolBarActivity {
    private static final String TAG = "FontSizeActivity";
    SetTextSizeView  seek_font;
    Boolean isClickSeek=false;//是否点击了调节字体大小
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);
        LogUtil.d(TAG,"onCreate()");
        setTitle(getString(R.string.app_font_size_setting));
        isClickSeek=getIntent().getBooleanExtra("isClickSeek",false);
        seek_font=findViewById(R.id.seek_font);
        seek_font.setDefaultPosition(CommonUtil.getSharedPreferencesInt(MyApplication.getInstance(),"app_font_scale",0));
        seek_font.setOnPointResultListener(new SetTextSizeView.OnPointResultListener() {
            @Override
            public void onPointResult(int position) {
                isClickSeek=true;
                CommonUtil.setSharedPreferences(FontSizeActivity.this, "app_font_scale", position);
                switch (position) {
                    case 0:
                        initFontScale(position);
                        switchSettingAction();
                        break;
                    case 1:
                        initFontScale(position);
                        switchSettingAction();
                        break;
                    case 2:
                        initFontScale(position);
                        switchSettingAction();
                        break;

                }
            }
        });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d(TAG,"onNewIntent()");
    }

    public void switchSettingAction() {
        Intent it = new Intent("com.uas.appme.settings.activity.FontSizeActivity");
        it.putExtra("isClickSeek",isClickSeek);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(it);
        overridePendingTransition(0, 0);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogUtil.d(TAG,"isClickSeek:"+isClickSeek);
        if (isClickSeek){
            Intent it = new Intent("com.modular.main.MainActivity");
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
            overridePendingTransition(0, 0);
        }
    }
}
