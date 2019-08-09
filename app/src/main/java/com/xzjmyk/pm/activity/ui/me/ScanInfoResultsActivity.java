package com.xzjmyk.pm.activity.ui.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Contacts;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.xmpp.model.AddAttentionResult;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;

import java.util.HashMap;

/**
 * Created by FANGlh on 2017/6/7.
 * function:二维码扫描后非html的String动态展示界面，根据是不是名片二维码显示两种不同的布局
 */

public class ScanInfoResultsActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.special_text_ll)
    private LinearLayout special_text_ll;
    @ViewInject(R.id.special_text_et)
    private EditText special_text_et;
    @ViewInject(R.id.qr_code_info_ll)
    private RelativeLayout qr_code_info_ll;
    @ViewInject(R.id.name_tv)
    private TextView name_tv;
    @ViewInject(R.id.phone_tv)
    private TextView phone_tv;
    @ViewInject(R.id.dail_phone_im)
    private ImageView dail_phone_im;
    @ViewInject(R.id.add_tolocal_im)
    private ImageView add_tolocal_im;
    @ViewInject(R.id.add_touufriend_im)
    private ImageView add_touufriend_im;
    @ViewInject(R.id.send_message_im)
    private ImageView send_message_im;
    private String phone;
    private String name;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_code_result);
        ViewUtils.inject(this);
        initView();
    }
    private void initView() {
        Intent intent = getIntent();
        Boolean isQRData = intent.getBooleanExtra("isQRData",false);
        String ScanResults = intent.getStringExtra("ScanResults");
        if (StringUtil.isEmpty(ScanResults)) return;
        if (isQRData) {
            //TODO 显示名片的布局
            special_text_ll.setVisibility(View.GONE);
            qr_code_info_ll.setVisibility(View.VISIBLE);
            dail_phone_im.setOnClickListener(this);
            add_tolocal_im.setOnClickListener(this);
            add_touufriend_im.setOnClickListener(this);
            send_message_im.setOnClickListener(this);
            JSONObject resultObject = JSON.parseObject(ScanResults);
            if (resultObject == null) return;
            showQRData(resultObject);
        }else {
            //TODO 显示特殊字符的布局
            special_text_ll.setVisibility(View.VISIBLE);
            qr_code_info_ll.setVisibility(View.GONE);
            showSpecialText(ScanResults);
        }
    }

    private void showSpecialText(String ScanResults) {
        special_text_et.setText(ScanResults);
    }

    private void showQRData(JSONObject resultObject) {
        name = resultObject.getString("uu_name");
        phone = resultObject.getString("uu_phone");
        userid = resultObject.getString("uu_userid");

        name_tv.setText(name);
        phone_tv.setText(phone);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dail_phone_im: // 拨打电话
                if (StringUtil.isEmpty(phone_tv.getText().toString())) return;
                CommonUtil.phoneAction(mContext,phone_tv.getText().toString());
                break;
            case R.id.add_tolocal_im://保存至本地联系人
                ToastMessage("号码已复制剪切板");
                SystemUtil.copyText(getApplicationContext(),phone_tv.getText().toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Contacts.People.CONTENT_URI);
                        startActivity(intent);
                    }
                },500);
                break;
            case R.id.add_touufriend_im://添加为UU好友
                doAdduuF();
                break;
            case R.id.send_message_im:
                ToastMessage("号码已复制剪切板");
                SystemUtil.copyText(getApplicationContext(),phone_tv.getText().toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse("smsto:" + phone_tv.getText().toString());
                        Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
                        startActivity(intent);
                    }
                },500);
                break;
        }
    }

    private void doAdduuF() {
        if (StringUtil.isEmpty(userid)) {
            return;
        }
        progressDialog.show();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", userid);

        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                mConfig.FRIENDS_ATTENTION_ADD, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                progressDialog.dismiss();
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success && result.getData() != null) {// 接口加关注成功
                    if (result.getData().getType() == 1 || result.getData().getType() == 3) {// 单方关注成功或已经是关注的getS
                        ToastMessage(getString(R.string.add_attention_succ));
                    } else if (result.getData().getType() == 2 || result.getData().getType() == 4) {// 已经是好友了
                        ToastMessage(getString(R.string.add_friend_succ));
                    } else if (result.getData().getType() == 5) {
                        ToastMessage(getString(R.string.add_attention_failed));
                    }
                }
                progressDialog.dismiss();
            }
        }, AddAttentionResult.class, params);
        addDefaultRequest(request);
    }
}
