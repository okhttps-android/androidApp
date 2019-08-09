package com.xzjmyk.pm.activity.ui.platform.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.core.model.SelectBean;
import com.core.base.BaseActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by FANGlh on 2017/3/23.
 * function:
 */
public class ChangeDealManActivity extends BaseActivity implements RecognizerDialogListener {

    @ViewInject(R.id.reason_tv)
    private TextView reason_tv;
    @ViewInject(R.id.notice)
    private TextView notice;
    @ViewInject(R.id.btn_save)
    private Button btn_save;
    @ViewInject(R.id.voice_iv)
    private ImageView voice_iv;
    private int detail_id;
    private int single_man_emcode;
    private String single_man_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_dealman);
        ViewUtils.inject(this);
        initView();
        initClickEvent();
    }

    private void initClickEvent() {
        reason_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap param = new HashMap<>();
                param.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
                Bundle bundle = new Bundle();
                bundle.putSerializable("param", param);
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("isSingle", false);
                intent.putExtra("reid", R.style.OAThemeMeet);
                intent.putExtras(bundle);
                intent.putExtra("key", "data");
                intent.putExtra("method", "get");
                intent.putExtra("showKey", "emname");
                intent.putExtra("action", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getUsersInfo);//
                startActivityForResult(intent, 0x322);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = reason_tv.getText().toString();
                if (!StringUtil.isEmpty(reason) && detail_id != -1 && single_man_emcode != -1) {
                    doChangeManRequest(reason, detail_id, single_man_emcode);
                    btn_save.setEnabled(false);
                    progressDialog.show();
                } else {
                    ToastMessage("请输入变更原因");
                }

            }
        });
        voice_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizerDialog dialog = new RecognizerDialog(ct, null);
                dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
                dialog.setListener(ChangeDealManActivity.this);
                dialog.show();
            }
        });
    }

    private String[] tagValues;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == 0x322) {
            ArrayList<SelectBean> temps = data.getParcelableArrayListExtra("data");
            if (temps == null) return;
            String[] tag_values = new String[temps.size()];
            tagValues = new String[temps.size()];
            int i = 0;
            String name_text = "";
            for (SelectBean bean : temps) {
                JSONObject json = JSON.parseObject(bean.getJson());
                tag_values[i] = bean.getName();
                tagValues[i] = json.getString("emcode");
                if (!StringUtil.isEmpty(tag_values[i])) {
                    name_text = name_text + "@" + tag_values[i];
                }
                i++;
            }
            reason_tv.setText(reason_tv.getText().toString() + name_text);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doChangeManRequest(String reason, int detail_id, int single_man_emcode) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_change_dealman_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", detail_id);
        param.put("reason", reason);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", single_man_emcode);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, CHANGE_DEAL_MAN, null, null, "post");

    }

    private void initView() {

        Intent intent = getIntent();
        detail_id = intent.getIntExtra("deal_id", -1);
        single_man_emcode = intent.getIntExtra("single_man_emcode", -1);
        single_man_name = intent.getStringExtra("single_man_name");

        LogUtil.d("detail_id,single_man_emcode,single_man_name", detail_id + "," + single_man_emcode + "," + single_man_name);
        if (!StringUtil.isEmpty(single_man_name))
            setTitle("变更给" + single_man_name);
        else {
            Toast.makeText(getApplicationContext(), "变更人数据未获取成功", Toast.LENGTH_LONG).show();
        }

    }

    private static final int CHANGE_DEAL_MAN = 0x324;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CHANGE_DEAL_MAN:
                    if (msg.getData() != null) {
                        String change_deal_man_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("change_deal_man_result", change_deal_man_result);
                        Toast.makeText(getApplicationContext(), "变更成功", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra("change", true);
                        setResult(0x333, intent);
                        finish();
                    }
                    btn_save.setEnabled(true);
                    progressDialog.dismiss();
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    btn_save.setEnabled(true);
                    progressDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        reason_tv.setText(reason_tv.getText().toString() + text);
    }

    @Override
    public void onError(SpeechError speechError) {

    }
}
