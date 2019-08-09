package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.app.AppConfig;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonInterface;
import com.core.widget.view.SwitchView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.OA.erp.activity.MissionActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OutofficeSetActivity extends BaseActivity {
    @ViewInject(R.id.location_tv)
    private TextView location_tv;
    @ViewInject(R.id.location_tag)
    private TextView location_tag;
    @ViewInject(R.id.location_rl)
    private RelativeLayout location_rl;
    @ViewInject(R.id.mission_rl)
    private RelativeLayout mission_rl;
    @ViewInject(R.id.allow_chance_location)
    private SwitchView allow_chance_location;
    @ViewInject(R.id.isImage)
    private SwitchView isImage;
    @ViewInject(R.id.auto_signin_sw)
    private SwitchView auto_signin_sw;

    private String baseUrl;
    private String emcode;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            JSONObject object = null;
            switch (msg.what) {
                case 0x11:
                    object = JSON.parseObject(message);
                    if (object.containsKey("success") && object.getBoolean("success"))
                        ToastMessage("保存成功");
                    else
                        ToastMessage("保存失败");
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            endActivity();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outoffice_set);
        ViewUtils.inject(this);
        initview();
    }


    @Override
    public void onBackPressed() {
        endActivity();
    }

    private void endActivity() {
        Intent intent = new Intent();
        intent.putExtra("isImage", isImage.isChecked());
        intent.putExtra("isAddress", allow_chance_location.isChecked());
        intent.putExtra("distance", StringUtil.getFirstInt(location_tv.getText().toString().trim(), 100));
        setResult(0x21, intent);
        finish();
    }

    private void updataSet(String key, String value) {
        String formStore = "{\"" + key + "\":\"" + value + "\"}";
        Map<String, Object> param = new HashMap<>();
        param.put("formStore", formStore);
        net("mobile/updateconfigs.action", param, 0x11);
    }


    private void net(String action, Map<String, Object> param, int what) {
        progressDialog.show();
        if (baseUrl == null)
            baseUrl = CommonUtil.getSharedPreferences(ct, "erp_baseurl");
        if (emcode == null)
            emcode = CommonUtil.getSharedPreferences(ct, "erp_username");
        String url = baseUrl + action;
        param.put("caller", "AppConfigs");
        param.put("emcode", emcode);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, what, null, null, "get");
    }


    private void initview() {
        mission_rl.setVisibility(AppConfig.IS_MISSION ? View.VISIBLE : View.GONE);
        Intent intent = getIntent();
        if (intent != null) {
            isImage.setChecked(intent.getBooleanExtra("isImage", false));
            allow_chance_location.setChecked(intent.getBooleanExtra("isAddress", false));
            setClickAble(intent.getBooleanExtra("isAddress", false));
            location_tv.setText(intent.getIntExtra("distance", 0) + "米");
        }
        isImage.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                CommonUtil.setSharedPreferences(ct, "isImage", isChecked);
                updataSet("isImage", "" + (isChecked ? 1 : 0));
            }
        });
        allow_chance_location.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                setClickAble(isChecked);
                updataSet("isAddress", "" + (isChecked ? 1 : 0));
                location_rl.setClickable(isChecked);
                location_rl.setFocusable(isChecked);
            }
        });
        location_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDalogs();
            }
        });
        location_rl.setClickable(intent.getBooleanExtra("isAddress", false));
        location_rl.setFocusable(intent.getBooleanExtra("isAddress", false));
        boolean isAutoMission = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
        auto_signin_sw.setChecked(isAutoMission);
        auto_signin_sw.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                PreferenceUtils.putBoolean(AppConfig.AUTO_MISSION, isChecked);
                if (!isChecked) return;
                updataSet(isChecked);
            }
        });
    }

    private void ShowDalogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OutofficeSetActivity.this);
        builder.setTitle("选择微调距离");
        //    指定下拉列表的显示数据
        final String[] cities = {"100米", "200米", "400米", "600米", "1000米"};
        //    设置一个下拉的列表选择项
        builder.setItems(cities, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                location_tv.setText(cities[which]);
                updataSet("distance", "" + StringUtil.getFirstInt(cities[which], 100));
            }
        });
        builder.show();
    }


    public void setClickAble(boolean clickAble) {
        location_rl.setClickable(clickAble);
        location_rl.setFocusable(clickAble);
        int color = R.color.hintColor;
        if (clickAble)
            color = R.color.text_main;
        location_tag.setTextColor(getResources().getColor(color));
        location_tv.setTextColor(getResources().getColor(color));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x20 && resultCode == 0x20) {
            boolean isAuto = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
            //如果进去选择为自动外勤，退出
            Intent intent = new Intent();
            intent.putExtra("isAuto", isAuto);
            setResult(0x20, intent);
            finish();
        }
    }


    private void updataSet(final boolean isAuto) {

        int distance = PreferenceUtils.getInt(AppConfig.ALARM_MISSION_DISTANCE, 500);
        final int time = PreferenceUtils.getInt(AppConfig.AUTO_MISSION_TIME, 30);
        int faceSign = PreferenceUtils.getInt(AppConfig.FACE_SIGN, -1);
        CommonInterface.getInstance().addOutSet(distance, time, isAuto, false, faceSign, new CommonInterface.OnResultListener() {
            @Override
            public void result(boolean isOk, int result, String message) {
                if (isOk && isAuto) {
                    Intent intent = new Intent(ct, MissionActivity.class);
                    intent.putExtra("flag", 2);
                    intent.putExtra(AppConfig.IS_ADMIN, true);//上传管理员状态
                    startActivityForResult(intent, 0x20);
                }
            }
        });

    }
}
