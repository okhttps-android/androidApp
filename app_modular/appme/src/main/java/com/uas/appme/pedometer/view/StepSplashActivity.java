package com.uas.appme.pedometer.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.core.app.Constants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.uas.appme.R;
import com.uas.appme.pedometer.utils.StepHttpUtils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *Created by FANGlh on 2017/4/24.
 * function:
 */
public class StepSplashActivity extends Activity {
    private ImageView iv_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.step_splash);
        iv_start = (ImageView) findViewById(R.id.iv_start);
        initImage();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity();
            }
        }, 3000);

    }

    private void initImage() {
        File dir = getFilesDir();
        final File imgFile = new File(dir, "start.jpg");
        if (imgFile.exists()) {
            iv_start.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        } else {
            iv_start.setImageResource(R.drawable.start_splash1);
        }

        final ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnim.setFillAfter(true);
        scaleAnim.setDuration(3000);
        scaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (StepHttpUtils.isNetworkConnected(StepSplashActivity.this)) {
                    StepHttpUtils.get(Constants.START, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] bytes) {
                            try {
                                JSONObject jsonObject = new JSONObject(new String(bytes));
                                String url = jsonObject.getString("img");
                                StepHttpUtils.getImage(url, new BinaryHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                                        saveImage(imgFile, bytes);
                                        startActivity();
                                    }

                                    @Override
                                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                                        startActivity();
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                            startActivity();
                        }
                    });
                } else {
                    Toast.makeText(StepSplashActivity.this, getString(R.string.networks_out), Toast.LENGTH_LONG).show();
                    startActivity();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_start.startAnimation(scaleAnim);

    }

    private void startActivity() {
        Intent intent = new Intent(StepSplashActivity.this, NewStepActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        finish();
    }

    public void saveImage(File file, byte[] bytes) {
        try {
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
