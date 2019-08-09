package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.SwitchView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.core.app.AppConfig.ALARMA_CLICK;


//签到提醒
public class AlarmaActivity extends BaseActivity implements View.OnClickListener, SwitchView.OnCheckedChangeListener {
    @ViewInject(R.id.isalarma_sw)
    private SwitchView isalarma_sw;
    @ViewInject(R.id.auto_signin_sw)
    private SwitchView auto_signin_sw;
    @ViewInject(R.id.tv_alar)
    private TextView tv_alar;//上班时间

    @ViewInject(R.id.tv_ualar)
    private TextView tv_ualar;//下班时间
    @ViewInject(R.id.ualar_rl)
    private RelativeLayout ualar_rl;//下班时间
    @ViewInject(R.id.alar_rl)
    private RelativeLayout alar_rl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oa_alarma);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        boolean isAlarm = CommonUtil.getSharedPreferencesBoolean(ct, ALARMA_CLICK, false);
        boolean isAuto =   true ;
        isalarma_sw.setChecked(isAlarm);
        auto_signin_sw.setChecked(isAuto);
        setTextColor(isAlarm);
        alar_rl.setOnClickListener(this);
        ualar_rl.setOnClickListener(this);
        isalarma_sw.setOnCheckedChangeListener(this);
        auto_signin_sw.setOnCheckedChangeListener(this);
        int d = CommonUtil.getSharedPreferencesInt(ct, "ALARM_OFFWORK_TIME", 5);
        int u = CommonUtil.getSharedPreferencesInt(ct, "ALARM_WORK_TIME", 5);
        tv_alar.setText("提前" + u + "分钟");
        tv_ualar.setText("延迟" + d + "分钟");

    }

    @Override
    public void onClick(View view) {
        String pre = null;
        String[] time = getResources().getStringArray(R.array.alarm_time);
        int requestCode = 0;
        switch (view.getId()) {
            case R.id.alar_rl:
                pre = "提前";
                requestCode = 0x21;
                break;
            case R.id.ualar_rl:
                pre = "延迟";
                requestCode = 0x22;
                break;
            default:
                pre = "提前";
                requestCode = 0x21;
                break;
        }
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        for (String e : time) {
            bean = new SelectBean();
            bean.setName(pre + e);
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", "选择时间");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCheckedChanged(View view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.isalarma_sw:
                CommonUtil.setSharedPreferences(ct, ALARMA_CLICK, isChecked);
                setTextColor(isChecked);
                break;
            case R.id.auto_signin_sw:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) return;
            String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
            if (requestCode == 0x22) {
                tv_ualar.setText(name);
                CommonUtil.setSharedPreferences(ct, "ALARM_OFFWORK_TIME", getNumByString(name));
            } else {
                tv_alar.setText(name);
                CommonUtil.setSharedPreferences(ct, "ALARM_WORK_TIME", getNumByString(name));
            }
            if (requestCode == 0x21 || requestCode == 0x22) {
            }
        }
    }

    private int getNumByString(String chche) {
        if (StringUtil.isEmpty(chche)) return 5;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(chche);
        if (m.find()) {
            return Integer.parseInt(m.group(0));
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setTextColor(boolean clicked) {
        alar_rl.setClickable(clicked);
        ualar_rl.setClickable(clicked);
        if (Build.VERSION.SDK_INT >= 9) {
            if (clicked) {
                alar_rl.setAlpha(1f);
                ualar_rl.setAlpha(1f);
            } else {
                alar_rl.setAlpha(0.26f);
                ualar_rl.setAlpha(0.26f);
            }
        }
    }
}
