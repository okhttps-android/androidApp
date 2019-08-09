package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.work.WorkModelDao;
import com.core.model.WorkModel;
import com.core.utils.TimeUtils;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;

import java.util.List;


public class MyRuleSetActivity extends BaseActivity {

    private TextView name_tv;
    private TextView type_tv;
    private TextView empty_tv;
    private TextView time_tv;
    private TextView date_tv;
    private LinearLayout context_ll;
    private RelativeLayout empty_rl;

    private List<WorkModel> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rule_set);
        ViewUtils.inject(this);
        initView();
        initDate();

        findViewById(R.id.click_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ct, HolidaysActivity.class));
            }
        });
    }

    private void initView() {
        name_tv = (TextView) findViewById(R.id.name_tv);
        type_tv = (TextView) findViewById(R.id.name_tv);
        empty_tv = (TextView) findViewById(R.id.empty_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
        date_tv = (TextView) findViewById(R.id.date_tv);
        context_ll = (LinearLayout) findViewById(R.id.context_ll);
        empty_rl = (RelativeLayout) findViewById(R.id.empty_rl);
    }


    boolean isFree;

    private void initDate() {
        if (getIntent() != null) {
            models = getIntent().getParcelableArrayListExtra("data");
            isFree = getIntent().getBooleanExtra("isFree", false);
            String days = getIntent().getStringExtra("day");
            String name = getIntent().getStringExtra("name");
            if (!StringUtil.isEmpty(name)) name_tv.setText(name);
            try {
                String d = getWeek(days);
                date_tv.setText(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            models = WorkModelDao.getInstance().query(false);
        }
        if (isFree || ListUtils.isEmpty(models)) {
            context_ll.setVisibility(View.GONE);
            empty_rl.setVisibility(View.VISIBLE);
            if (!isFree) {
                if (MyApplication.getInstance().isNetworkActive())
                    empty_tv.setText(getString(R.string.no_flights));
                else empty_tv.setText(R.string.networks_out);
            }
        } else {
            context_ll.setVisibility(View.VISIBLE);
            empty_rl.setVisibility(View.GONE);
            showWorkData();
        }

    }


    private void showWorkData() {
        if (isFree) {
            time_tv.setText(getString(R.string.no_));
        } else {
            StringBuilder timeShow = new StringBuilder();
            long allTime = 0;
            for (WorkModel e : models) {
                String t1 = e.getWorkTime();
                String t2 = e.getOffTime();
                timeShow.append(t1 + "-" + t2 + " ");
                try {
                    allTime += DateFormatUtil.getDifferSS(t1, t2);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            float h = Float.valueOf(allTime) / (60 * 60);
            String hour = float2String(h);
            timeShow.append("  " + hour + getString(R.string.hour));
            time_tv.setText(timeShow.toString());
        }
    }


    private String float2String(float hour) {
        if (hour == 0) return "0";

        String h = String.valueOf(hour);
        if (!StringUtil.isEmpty(h) && h.length() > 2) {
            h = h.substring(0, 3);
        }
        if (h.indexOf(".") > 0) {
            //正则表达
            h = h.replaceAll("0+?$", "");//去掉后面无用的零
            h = h.replaceAll("[.]$", "");//如小数点后面全是零则去掉小数点
        }

        return h;
    }

    private long getAllTime(String start, String end) {
        if (StringUtil.isEmpty(start) || StringUtil.isEmpty(end)) return 0;
        long startTime = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + start + ":00");
        long endTime = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + end + ":00");
        return endTime - startTime;
    }

    public String getWeek(String d) throws Exception {
        if (StringUtil.isEmpty(d)) return "";
        String[] days = d.split(",");
        if (days == null || days.length <= 0) return "";
        int day = 0;
        StringBuilder stringBuilder = new StringBuilder("周");
        for (String e : days) {
            day = Integer.valueOf(e);
            String str = "";
            switch (day) {
                case 1:
                    str = "一";
                    break;
                case 2:
                    str = "二";
                    break;
                case 3:
                    str = "三";
                    break;
                case 4:
                    str = "四";
                    break;
                case 5:
                    str = "五";
                    break;
                case 6:
                    str = "六";
                    break;
                case 7:
                    str = "日";
                    break;
            }
            stringBuilder.append(str + "、");
        }
        if (stringBuilder.length() > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
