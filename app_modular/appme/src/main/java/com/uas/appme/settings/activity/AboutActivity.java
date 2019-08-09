package com.uas.appme.settings.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.system.SystemUtil;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.core.widget.view.Activity.CommonWebviewActivity;
import com.lidroid.xutils.ViewUtils;
import com.uas.appme.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.uuzuche.lib_zxing.activity.CodeUtils;

public class AboutActivity extends SupportToolBarActivity implements View.OnClickListener {

    private RelativeLayout me_name;
    private RelativeLayout me_function;
    private RelativeLayout me_QRcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("AboutActivity extends","onCreate()");
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.set_about));
        TextView versionTv = (TextView) findViewById(R.id.version_tv);
        String appName = getString(R.string.app_name);
        String versionName = SystemUtil.getVersionName(mContext);
        versionTv.setText(appName + " " + versionName);
        initView();

        ViewUtils.inject(this);
        me_name.setOnClickListener(this);
        me_function.setOnClickListener(this);
        me_QRcode.setOnClickListener(this);
    }

    private void initView() {
        me_name = (RelativeLayout) findViewById(R.id.me_name);
        me_function = (RelativeLayout) findViewById(R.id.me_function);
        me_QRcode = (RelativeLayout) findViewById(R.id.me_QRcode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_about) {
            try {
                new ShareAction(activity).setDisplayList(
                        SHARE_MEDIA.SINA,
                        SHARE_MEDIA.QQ,
                        SHARE_MEDIA.QZONE,
                        SHARE_MEDIA.WEIXIN,
                        SHARE_MEDIA.WEIXIN_CIRCLE,
                        SHARE_MEDIA.WEIXIN_FAVORITE,
                        SHARE_MEDIA.MORE)
                        .withTitle("UU互联")
                        .withText("UU互联 Android客户端" + SystemUtil.getVersionName(mContext))
                        .withMedia(new UMImage(activity, "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png"))
                        .withTargetUrl("http://www.usoftchina.com/usoft/uas_client.html")
                        .setCallback(CommonUtil.umShareListener)
                        .open();
            } catch (Exception e) {

            }
        } else if (item.getItemId() == android.R.id.home) {

            onBackPressed();
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.me_name) {
            try {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                IntentUtils.webLinks(ct, "http://apk.91.com/Soft/Android/com.xzjmyk.pm.activity-54.html", "UU互联");
            }
        } else if (v.getId() == R.id.me_function) {
            String url = "";
            url = "http://113.105.74.140:8080/new/";
            IntentUtils.webLinks(ct, url, getString(R.string.about_function));

        } else if (v.getId() == R.id.me_QRcode) {
            ImageView imageView = new ImageView(ct);
            imageView.setImageResource(R.drawable.ic_uu_scan_code);
            new MaterialDialog.Builder(ct)
                    .title(getString(R.string.about_qcode_msg))
                    .customView(imageView, false)
                    .titleGravity(GravityEnum.CENTER)
                    .show();
            final Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CodeUtils.analyzeBitmap(bitmap, new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            Intent intent = new Intent();
                            intent.setClass(ct, CommonWebviewActivity.class);
                            intent.putExtra("scan_url", result);
                            Log.d("image_url", result);
                            startActivity(intent);
                        }

                        @Override
                        public void onAnalyzeFailed() {

                        }
                    });
                    return false;
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
