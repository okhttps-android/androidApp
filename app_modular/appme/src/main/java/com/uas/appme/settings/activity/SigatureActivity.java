package com.uas.appme.settings.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.core.base.BaseActivity;
import com.uas.appme.R;
import com.uas.appme.settings.handwritedemo.LandscapeActivity;

import java.io.File;

/**
 * Created by FANGlh on 2017/11/8.
 * function:
 */

public class SigatureActivity extends BaseActivity implements View.OnClickListener{

    private ImageView s_image;
    public static String path1= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ls.png";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.signture_activity);
        setTitle("签名");
        findViewById(R.id.btn2).setOnClickListener(this);
        s_image = (ImageView) findViewById(R.id.img2);
        s_image.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn2){
            startActivityForResult(new Intent(this, LandscapeActivity.class)
                            .putExtra("signCode",002)
                    , 101);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==101 && requestCode == 101){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bm = BitmapFactory.decodeFile(path1, options);
            s_image.setImageBitmap(bm);
        }

    }
}
