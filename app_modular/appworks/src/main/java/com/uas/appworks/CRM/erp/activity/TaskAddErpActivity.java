package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2017/3/30.
 * function:erp添加任务界面同步于b2b任务添加界面
 */
public class TaskAddErpActivity extends BaseActivity implements View.OnClickListener, RecognizerDialogListener {
    private static final int TASK_ADD_ERP = 0x330;
    private EditText et_title;
    private EditText et_task_detail;
    private TextView et_task_people;
    private ImageView iv_find;
    private TextView tv_title;
    private TextView detailsTV;
    private TextView et_startime;
    private TextView et_endtime;
    private ImageView voice_search_iv;

    private String[] tagValues;
    private int save = 0;
    private String selectCode = null;
    private int type = 0;
    private boolean timeSelected = false;//是否已经进行选择时间操作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_taskerp_add);
        initIDS();
        initView();
    }

    private void initIDS() {

        voice_search_iv = (ImageView) findViewById(R.id.voice_search_iv);
        tv_title = (TextView) findViewById(R.id.tv_title);
        detailsTV = (TextView) findViewById(R.id.detailsTV);
        et_endtime = (TextView) findViewById(R.id.et_endtime);
        et_title = (EditText) findViewById(R.id.et_title);
        et_task_detail = (EditText) findViewById(R.id.et_task_detail);
        et_task_people = (TextView) findViewById(R.id.et_task_people);
        iv_find = (ImageView) findViewById(R.id.iv_find);
        et_startime = (TextView) findViewById(R.id.et_startime);
    }

    private void initView() {

        ViewUtils.inject(this);
        ct = this;
        TAG = "TaskAddErpActivity";
        int title = R.string.oacreat_calender;
        String people = "";
        Intent intent = getIntent();
        if (intent != null) {
            people = intent.getStringExtra("people");
            selectCode = intent.getStringExtra(AppConstant.EXTRA_EM_CODE);
            type = intent.getIntExtra("type", 0);
            switch (type) {
                case 1:
                    title = R.string.oacreat_calender;
                    tv_title.setText("日程标题");
                    detailsTV.setText("日程详情");
                    break;
                case 2:
                    title = R.string.create_meet_task;
                    et_title.setText(intent.getStringExtra("meetname"));
                    et_title.setFocusable(false);
                    break;
                default:
                    title = R.string.task_add;
            }
        }
        if (!StringUtil.isEmpty(people)) {
            et_task_people.setText(people);
            selectNames = people;
        }
       setTitle(title);
        iv_find.setOnClickListener(this);
        et_startime.setOnClickListener(this);
        et_endtime.setOnClickListener(this);

        String current_time = DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS);
        et_startime.setText(current_time);
        et_endtime.setText(current_time);

        voice_search_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizerDialogUtil.showRecognizerDialog(ct, TaskAddErpActivity.this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        String sT = et_startime.getText().toString();
        String eT = et_endtime.getText().toString();
        if (v.getId() == R.id.et_startime) {
            if (timeSelected) {
                sT = sT + ":00"; //这里传的跳时间选择界面时需要带上秒，统一考勤界面的时间选择逻辑
                eT = eT + ":00";
            }
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", sT)
                            .putExtra("endDate", eT)
                            .putExtra("hasMenu", false)
                            .putExtra("caller", "Workovertime")
                    , 0x30);
        } else if (v.getId() == R.id.iv_find) {
            Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
            SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                    .setTitle(getString(R.string.select_doman))
                    .setSingleAble(false)
                    .setSelectCode(selectCode);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, 0x01);
        } else if (v.getId() == R.id.et_endtime) {
            if (timeSelected) {
                sT = sT + ":00";
                eT = eT + ":00";
            }
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", sT)
                            .putExtra("endDate", eT)
                            .putExtra("hasMenu", false)
                            .putExtra("caller", "Workovertime")
                    , 0x30);
        }
    }

    private void showDateDialog(Context ct, final TextView tv) {
        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                String time = year + "-" + month + "-" + day + " " + hour + ":" + minute;
                tv.setText(time);
            }
        });
        picker.show();
    }

    private String selectNames = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 0x01://执行人多选
                List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");

                if (ListUtils.isEmpty(employeesList)) {
                    selectNames = "";
                    selectCode = "";
                    et_task_people.setVisibility(View.GONE);
                    return;
                } else {
                    et_task_people.setVisibility(View.VISIBLE);
                }
                StringBuilder select = new StringBuilder();
                StringBuilder selectCode = new StringBuilder();
                for (SelectEmUser e : employeesList) {
                    select.append(e.getEmName() + ",");
                    selectCode.append(e.getEmCode() + ",");
                }
                StringUtil.removieLast(select);
                StringUtil.removieLast(selectCode);
                this.selectCode = selectCode.toString();
                selectNames = select.toString();
                et_task_people.setText(selectNames);
                break;
            default:
                break;
        }

        if (requestCode == 0x30 && resultCode == 0x11) {
            timeSelected = true;
            String startDate = data.getStringExtra("startDate");
            String endDate = data.getStringExtra("endDate");
//            startDate=startDate+":00";
//            endDate=endDate+":00";
            et_startime.setText(startDate);
            et_endtime.setText(endDate);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visit_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.save) {
            if (MyApplication.getInstance().isNetworkActive()) {
                if (save == 0) {
                    dosaveJudge();
                }
            } else {
                ToastUtil.showToast(ct, R.string.networks_out);
            }

        }
        return true;
    }

    private void dosaveJudge() {
        if (StringUtil.isEmpty(et_title.getText().toString())) {
            ToastMessage(getString(type == 1 ? R.string.schedule_title_must_input : R.string.task_title_must_input));
            return;
        }
        if (StringUtil.isEmpty(et_task_detail.getText().toString())) {
            ToastMessage(getString(type == 1 ? R.string.schedule_detail_must_input : R.string.task_detail_must_input));
            return;
        }
        if (StringUtil.isEmpty(selectNames)) {
            ToastMessage(getString(R.string.task_doman_must_input));
            return;
        }
        if (selectNames.length() > 1000) {
            ToastMessage(getString(R.string.task_limit_doman));
            return;
        }
        if (StringUtil.isEmpty(et_startime.getText().toString())) {
            ToastMessage(getString(R.string.task_startT_must_input));
            return;
        }
        if (StringUtil.isEmpty(et_endtime.getText().toString())) {
            ToastMessage(getString(R.string.task_endT_must_input));
            return;
        }
        if (et_startime.getText().toString().compareTo(et_endtime.getText().toString()) >= 0) {
            ToastMessage(getString(R.string.endT_large_startT));
            return;
        }

        doHttpSave(selectNames);
        /*String[] people = et_task_people.getTags();
        people = tagValues;
        String resourcename = "";
        if (people != null) {
            for (int i = 0; i < people.length; i++) {
                if (i == people.length - 1) {
                    resourcename = resourcename + people[i].trim();
                } else {
                    resourcename = resourcename + people[i].trim() + ",";
                }
            }
        } else {
            ToastMessage("执行人为必选项");
            return;
        }*/


    }

    private void doHttpSave(String resourcename) {
        save = 1;
        progressDialog.show();
        String taskorschedule;
        if (type == 1) {
            taskorschedule = "Schedule";
        } else if (type == 2) {
            taskorschedule = "MTask";
        } else {
            taskorschedule = "Task";
        }
        Map<String, Object> formStoreMap = new HashMap<>();
        formStoreMap.put("taskorschedule", taskorschedule);
        formStoreMap.put("startdate", et_startime.getText().toString() + ":00");
        formStoreMap.put("enddate", et_endtime.getText().toString() + ":00");
        formStoreMap.put("name", et_title.getText().toString());
        formStoreMap.put("description", StringUtil.toHttpString(et_task_detail.getText().toString()));
        formStoreMap.put("resourcename", resourcename);

        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/plm/task/addbilltask.action";
        HashMap<String, Object> params = new HashMap<>();
        String formStore = JSONUtil.map2JSON(formStoreMap);
        params.put("formStore", formStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, handler, headers, TASK_ADD_ERP, null, null, "post");
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TASK_ADD_ERP:
                    progressDialog.dismiss();
                    if (msg.getData() != null) {
                        String task_erp_result = msg.getData().getString("result");
                        Log.i("task_erp_result", task_erp_result + "");
                        if (JSON.parseObject(task_erp_result).containsKey("success") && JSON.parseObject(task_erp_result).getBoolean("success")) {
                            Toast.makeText(mContext, getString(R.string.task_send_success), Toast.LENGTH_LONG).show();
                            setResult(0x20, null);
                            finish();
                        }
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                            progressDialog.dismiss();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        et_task_detail.setText(et_task_detail.getText().toString() + CommonUtil.getPlaintext(text));
    }

    @Override
    public void onError(SpeechError speechError) {

    }
}
