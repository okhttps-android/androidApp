package com.xzjmyk.pm.activity.ui.me;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.xzjmyk.pm.activity.CaptureResultActivity;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.utils.helper.AvatarHelper;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.io.UnsupportedEncodingException;

/**
 * Created by FANGlh on 2017/6/5.
 * function:
 */

public class InfoCodeActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.code_main)
    private ImageView code_main;
    @ViewInject(R.id.common_docui_photo_img)
    private ImageView photo_im;
    @ViewInject(R.id.common_docui_name_tv)
    private TextView name_tv;
    @ViewInject(R.id.common_docui_Section_tv)
    private TextView section_tv;
    // 图片宽度的一半
    private static final int IMAGE_HALFWIDTH = 20;
    // 显示二维码图片
    private ImageView imageview;
    // 插入到二维码里面的图片对象
    private Bitmap mBitmap;
    // 需要插图图片的大小 这里设定为40*40
    int[] pixels = new int[2*IMAGE_HALFWIDTH * 2*IMAGE_HALFWIDTH];
    private String uu_phone;
    private String uu_name;
    private String loginUserId;
    private PopupWindow setWindow = null;//
    private static final int REQUEST_CODE = 17681;
    private Bitmap code_bitmap;
    private String s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_code_main);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        if (!CommonUtil.isNetWorkConnected(ct)){
          ToastMessage(getString(R.string.networks_out));
            return;
        }
        progressDialog.show();
        uu_phone = MyApplication.getInstance().mLoginUser.getTelephone();
        uu_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
        loginUserId = MyApplication.getInstance().mLoginUser.getUserId();

        name_tv.setText(uu_name + "");
        section_tv.setText(uu_phone+"");
        // 构造对象
        imageview = new ImageView(this);
        // 构造需要插入的图片对象
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBitmap = AvatarHelper.getInstance().returnBitmap(loginUserId,false);
                // 缩放图片
                Matrix m = new Matrix();
                float sx = (float) 2*IMAGE_HALFWIDTH / mBitmap.getWidth();
                float sy = (float) 2*IMAGE_HALFWIDTH / mBitmap.getHeight();
                m.setScale(sx, sy);
                // 重新构造一个40*40的图片
                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
                        mBitmap.getHeight(), m, false);
            }
        }).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                     s = "{\n"
                            +  "\"uu_name\":\"" + uu_name + "\",\n"+
                            "\"uu_phone\":\"" + uu_phone + "\",\n"+
                             "\"uu_userid\":\"" + loginUserId + "\"\n"
                            //TODO 需要更多字段自行添加即可
                            +"}";
                    if (mBitmap == null) {
                        Log.i("mBitmap","null");
                        mBitmap = ((BitmapDrawable) getResources().getDrawable(
                                R.drawable.uuu)).getBitmap();

                    }
                    Log.i("mBitmap",mBitmap.toString());
                    code_bitmap = cretaeBitmap(new String(s.getBytes(),
                            "ISO-8859-1"));  //解决客户端向服务器发送一个请求出现乱码问题
                    code_main.setImageBitmap(code_bitmap);
                    AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        },1000);

        //TODO 二维码添加点击事件，事后去掉
        code_main.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(ct, ScanInfoResultsActivity.class)
                        .putExtra("ScanResults",s)
                        .putExtra("isQRData",true));// true ：扫描到的是名片信息标志
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_infocode, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.title:
                showPopupWindow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.infocode_menu_more, null);
        viewContext.findViewById(R.id.share_2code).setOnClickListener(this);
        viewContext.findViewById(R.id.change_style).setOnClickListener(this);
        viewContext.findViewById(R.id.save_to_MBphone).setOnClickListener(this);
        viewContext.findViewById(R.id.scan_2dcode).setOnClickListener(this);
        viewContext.findViewById(R.id.cancel_tv).setOnClickListener(this);

        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.share_2code:
                String scan_result_url = AvatarHelper.doBitmapTurnToStringurl(code_bitmap);
                Log.i("scan_result_url",scan_result_url+"");
                if (StringUtil.isEmpty(scan_result_url)) return;
                new ShareAction(activity).setDisplayList(
                        SHARE_MEDIA.SINA,
                        SHARE_MEDIA.QQ,
                        SHARE_MEDIA.QZONE,
                        SHARE_MEDIA.WEIXIN,
                        SHARE_MEDIA.WEIXIN_CIRCLE,
                        SHARE_MEDIA.WEIXIN_FAVORITE,
                        SHARE_MEDIA.MORE)
                        .withTitle("UU互联")
                        .withText("名片二维码分享" + SystemUtil.getVersionName(mContext))
                        .withMedia(new UMImage(activity, "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png"))
//                        .withMedia(new UMImage(activity,scan_result_url))
                        .withTargetUrl(scan_result_url)
                        .setCallback(CommonUtil.umShareListener)
                        .open();
                closePopupWindow();
                break;
            case R.id.change_style:
                closePopupWindow();
                break;
            case R.id.save_to_MBphone:
                CommonUtil.saveImageToLocal(getApplicationContext(),code_bitmap);
                closePopupWindow();
                break;
            case R.id.scan_2dcode:
                Intent intent = new Intent();
                intent.setClass(this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                closePopupWindow();
                break;
            case R.id.cancel_tv:
                closePopupWindow();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            data.setClass(this, CaptureResultActivity.class);
            startActivity(data);
        }
    }

    /**
     * 生成二维码
     * @throws WriterException
     */
    public Bitmap cretaeBitmap(String str) throws WriterException {
        // 生成二维矩阵,编码时指定小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, 300, 300);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH && y > halfH - IMAGE_HALFWIDTH
                        && y < halfH + IMAGE_HALFWIDTH) {
                    pixels[y * width + x] = mBitmap.getPixel(x - halfW + IMAGE_HALFWIDTH, y
                            - halfH + IMAGE_HALFWIDTH);
                } else {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                }

            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

}
