package com.xzjmyk.pm.activity.ui.base;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;

/**
 * 让App字体不受系统设置字体的影响
 *
 * @author Dean Tao
 */
public class DefaultResourceActivity extends AppCompatActivity {
    /* System default config */
    private static Configuration config = new Configuration();

//    @Override
//    public Resources getResources() {
//        Resources res = super.getResources();
//        res.updateConfiguration(config, res.getDisplayMetrics());
//        return res;
//    }
}
