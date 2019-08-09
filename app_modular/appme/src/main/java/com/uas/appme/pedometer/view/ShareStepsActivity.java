package com.uas.appme.pedometer.view;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.uas.appme.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by FANGlh on 2017/9/29.
 * function:
 */

public class ShareStepsActivity extends BaseActivity {
    private LinearLayout mShareLl;
    private ImageView mStepsShareBg;
    private CircleImageView mSharerIm;
    private TextView mMtRankTvStr;
    private TextView mMtRankTv;
    private TextView mMtStepsTvStr;
    private TextView mMtStepsTv;
    private ImageView mNssb;
    private ArrayList<String> mPhotoList;
    private FormEditText mTextEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_steps_activity);
        initView();
        initData();
    }

    private void initView() {
        progressDialog.show();
        mShareLl = (LinearLayout) findViewById(R.id.share_ll);
        mStepsShareBg = (ImageView) findViewById(R.id.steps_share_bg);
        mSharerIm = (CircleImageView) findViewById(R.id.sharer_im);
        mMtRankTvStr = (TextView) findViewById(R.id.mt_rank_tv_str);
        mMtRankTv = (TextView) findViewById(R.id.mt_rank_tv);
        mMtStepsTvStr = (TextView) findViewById(R.id.mt_steps_tv_str);
        mMtStepsTv = (TextView) findViewById(R.id.mt_steps_tv);
        mNssb = (ImageView) findViewById(R.id.nssb);
        mPhotoList = new ArrayList<>();
        mTextEdit = (FormEditText)findViewById(R.id.share_steps_et);

    }

    private void initData() {
        int my_rank = getIntent().getIntExtra("my_rank", -1);
        String my_steps = getIntent().getStringExtra("my_steps");
        String im_ids = getIntent().getStringExtra("im_ids");

        mMtRankTv.setText(String.valueOf(my_rank));
        mMtStepsTv.setText(my_steps);
        Bitmap bitmap = CommonUtil.getViewToBitmap2(mShareLl);
        mNssb.setImageBitmap(bitmap);

        if (bitmap == null) return;
        //TODO 将Bitmap对象转换为String的 url
        final String url =CommonUtil.convertBitmapToString(bitmap);
        mPhotoList.add(url);
        AvatarHelper.getInstance().display(CommonUtil.getNumByString(im_ids) + "", mSharerIm, true, false);//显示圆角图片
        progressDialog.dismiss();
        findViewById(R.id.btn_attention).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.modilar.circle.SendShuoshuoActivity");
                intent.putExtra("type", 1);
                intent.putExtra("bitmap_url",url);
                startActivity(intent);
                finish();
            }
        });
    }
}
