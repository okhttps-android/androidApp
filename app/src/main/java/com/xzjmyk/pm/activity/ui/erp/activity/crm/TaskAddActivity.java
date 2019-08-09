package com.xzjmyk.pm.activity.ui.erp.activity.crm;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.Employees;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.utils.time.wheel.OptionPicker;
import com.core.widget.view.Activity.SelectActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.UserSelectActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaskAddActivity extends BaseActivity implements View.OnClickListener {
    private final int LOAD_SUCCESS_ADD = 0x54;
    @ViewInject(R.id.tv_name)
    private FormEditText tv_name;
    @ViewInject(R.id.tv_executive)
    private FormEditText tv_executive;
    @ViewInject(R.id.tv_date)
    private FormEditText tv_date;
    @ViewInject(R.id.tv_priority)
    private FormEditText tv_priority;
    @ViewInject(R.id.tv_type)
    private FormEditText tv_type;
    @ViewInject(R.id.tv_customer)
    private FormEditText tv_customer;
    @ViewInject(R.id.tv_notice)
    private FormEditText tv_notice;
    @ViewInject(R.id.tv_content)
    private FormEditText tv_content;
    @ViewInject(R.id.tv_date_start)
    private FormEditText tv_date_start;

    private String code = null;
    private int type;
    private boolean isBusiness;
    private String form = "";
    private boolean isPower;
    private boolean isB2b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent() != null) {
            int reid = getIntent().getIntExtra("reid", -1);
            isPower = getIntent().getBooleanExtra("isPower", true);
            if (reid != -1) {
                int theme = getSharedPreferences("cons", MODE_PRIVATE).getInt("theme", reid);
                setTheme(theme);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_add);
        initView();
        initData();
    }

    private void initView() {
        ViewUtils.inject(this);
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        type = getIntent().getIntExtra("type", 0);
        code = getIntent().getStringExtra("data");
        form = getIntent().getStringExtra("from");
        String bc_doman = getIntent().getStringExtra("bc_doman");
        String bc_custname = getIntent().getStringExtra("bc_custname");
        isBusiness = getIntent().getBooleanExtra("business", false);
        if (type == 0) {
            setTitle("创建任务");
            tv_type.setText("任务");
        } else if (type == 1) {
           setTitle("创建日程");
            tv_type.setText("日程");
            tv_name.setText(bc_custname);
            tv_executive.setText(bc_doman);
        } else if (type == 2) {
          setTitle("创建会议任务");
            tv_type.setText("会议任务");
            tv_name.setText(getIntent().getStringExtra("meetname"));
            tv_name.setFocusable(false);
        }
        tv_notice.setOnClickListener(this);
        tv_priority.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        tv_date_start.setOnClickListener(this);
        tv_executive.setOnClickListener(this);
        tv_date.setKeyListener(null);
        tv_date.setFocusable(false);
        tv_date_start.setKeyListener(null);
        tv_date_start.setFocusable(false);
        tv_executive.setKeyListener(null);
        tv_executive.setFocusable(false);
        tv_notice.setKeyListener(null);
        tv_notice.setFocusable(false);
        tv_priority.setKeyListener(null);
        tv_priority.setFocusable(false);


    }

    private void initData() {
        if (!isB2b)
            getCodeByNet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        menu.getItem(0).setTitle("确定");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_save:
                if (!isPower) {
                    showToast( "您没有查看列表的权限");
                    return true;
                }
                if (tv_name.testValidity() &&
                        tv_executive.testValidity()
                        && tv_date_start.testValidity()
                        && tv_date.testValidity()
                        && tv_priority.testValidity()
                        ) {
                    if (isB2b)
                        sendhttpB2b();
                    else
                        sendHttpResquest(Constants.HTTP_SUCCESS_INIT);
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void showDateDialog(final boolean b) {
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
                String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00";
                String curtime = TimeUtils.f_long_2_str(System.currentTimeMillis());
                if (curtime.compareTo(time) > 0){
                    showToast( "不能选择过去时间");
                    return;
                }
                if (b) {
                    tv_date_start.setText(time);
                } else {
                    if (time.compareTo(tv_date_start.getText().toString()) < 0) {
                        showToast( "结束时间不能小于开始时间");
                    } else {
                        tv_date.setText(time);
                    }
                }
            }
        });
        picker.show();
    }

    public void showDialog(final FormEditText tv, final boolean b) {
        final DateTimePickerDialog dialog = new DateTimePickerDialog(this, System.currentTimeMillis());
        dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dia, long date) {
                String str = TimeUtils.f_long_2_str(date);
                String start = null;
                if (!b) {
                    start = tv_date_start.getText().toString();
                    if (str.compareTo(start) < 0) {
                        ToastUtil.showToast(ct, "结束时间不能小于开始时间");
                    } else {
                        tv.setText(str);
                    }
                } else {
                    tv.setText(str);
//                    start = TimeUtils.f_long_2_str(System.currentTimeMillis());
//                    if (str.compareTo(start) < 0) {
//                        ToastUtil.showToast(ct, "开始时间不能小于当前时间");
//                    } else {
//
//                    }
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage("保存成功！");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!StringUtil.isEmpty(code))
                                setCodeByNet(code);
                            else
                                endActivity();

                        }
                    }, 3000);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage("保存异常！");
                    break;
                case endCodeWhat:
                    ToastMessage("保存上传成功！");
                    endActivity();
                    break;
                case codeWhat://获取编号
                    progressDialog.dismiss();
                    tv_code = JSON.parseObject(msg.getData().getString("result")).getString("code");
                    break;
                case LOAD_SUCCESS_ADD:
                    ToastMessage("保存成功！");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!StringUtil.isEmpty(code))
                                setCodeByNet(code);
                            else
                                endActivity();

                        }
                    }, 3000);
                    break;
            }
        }
    };

    private void endActivity() {
        Intent intent = new Intent();
        intent.putExtra("data", "data");
        setResult(0x20, intent);
        finish();
    }

    private void sendHttpResquest(int what) {
        if (TextUtils.isEmpty(tv_executive.getText())) {
            ToastMessage("执行人为必填项");
        }
        progressDialog.show();
        String str = "";
        if (type == 0) {
            str = "Task";
        } else if (type == 1) {
            str = "Schedule";
        } else if (type == 2) {
            str = "MTask";
        } else {
            str = "Task";
        }
        String strs = null;
        if (isBusiness) {
            strs = "\"handstatus\":\"" + "未完成" + "\",\n" +//编号
                    "\"handstatuscode\":\"" + "UNFINISHED" + "\",\n";
        }//BusinessDetailInfo
        String formStore;
        if (!StringUtil.isEmpty(form) && form.equals("BusinessDetailInfo")) {
            formStore = "{\n" +
                    "\"taskcode\":\"" + tv_code + "\",\n" +//编号
                    "\"name\":\"" + tv_name.getText().toString() + "\",\n" +//名称
                    "\"resourcename\":\"" +//执行人
                    tv_executive.getText().toString() +
                    "\",\n" +
                    "\"enddate\":\"" +//结束时间
                    tv_date.getText().toString() +
                    "\",\n" +
                    "\"class\":\"" +//商机详情进来
                    "bstask" +
                    "\",\n" +
                    "\"startdate\":\"" +//结束时间
                    tv_date_start.getText().toString() +
                    "\",\n" +
                    "\"tasklevel\":\"" +
                    tv_priority.getText().toString() +//任务优先级
                    "\",\n" +
                    "\"taskorschedule\":\"" + str//任务类型
                    + "\",\n" +
                    "\"custname\":\"" +
                    tv_customer.getText().toString() +//联系人
                    "\",\n" +
                    "\"SOURCECODE\":\"" +
                    code +//联系人
                    "\",\n" +
                    "\"timealert\":\"" +
                    tv_notice.getText().toString() +//提醒
                    "\",\n" +
                    "\"description\":\"" +
                    tv_content.getText().toString() +
                    "\",\n" + (isBusiness ? strs : "") +
                    "}";

        } else {
            formStore = "{\n" +
                    "\"taskcode\":\"" + tv_code + "\",\n" +//编号
                    "\"name\":\"" + tv_name.getText().toString() + "\",\n" +//名称
                    "\"resourcename\":\"" +//执行人
                    tv_executive.getText().toString() +
                    "\",\n" +
                    "\"enddate\":\"" +//结束时间
                    tv_date.getText().toString() +
                    "\",\n" +
                    "\"startdate\":\"" +//结束时间
                    tv_date_start.getText().toString() +
                    "\",\n" +
                    "\"tasklevel\":\"" +
                    tv_priority.getText().toString() +//任务优先级
                    "\",\n" +
                    "\"taskorschedule\":\"" + str//任务类型
                    + "\",\n" +
                    "\"custname\":\"" +
                    tv_customer.getText().toString() +//联系人
                    "\",\n" +
//                    "\",\n" +
//                    "\"type\":\"" + 1 +//是否需要待确认
                    "\"timealert\":\"" +
                    tv_notice.getText().toString() +//提醒
                    "\",\n" +
                    "\"description\":\"" +
                    tv_content.getText().toString() +
                    "\",\n" + (isBusiness ? strs : "") +
                    "}";
        }
        String url = CommonUtil.getAppBaseUrl(ct) + "plm/task/addbilltask.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void sendhttpB2b() {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_save;
        Map<String, Object> formStore = new HashMap<>();
        Map<String, Object> param = new HashMap<String, Object>();
        formStore.put("detail", tv_content.getText().toString());
        formStore.put("recordercode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));
        formStore.put("uu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        formStore.put("taskname", tv_name.getText().toString());
        formStore.put("domancode", tv_executive.getTag());//执行人uu
        formStore.put("startdate", tv_date_start.getText().toString());
        formStore.put("enddate", tv_date.getText().toString());
        param.put("formStore", JSONUtil.map2JSON(formStore));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        com.core.net.http.ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, LOAD_SUCCESS_ADD, null, null, "post");
    }

    private void showNoticeDialog() {
        OptionPicker sex_option = new OptionPicker(this, new String[]{
                "10分钟", "30分钟", "60分钟"
        });
        sex_option.setOffset(1);
        sex_option.setSelectedIndex(1);
        sex_option.setTextSize(18);
        sex_option.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(int position, String option) {
                tv_notice.setText(option);
            }

        });
        sex_option.show();
    }

    private void showPriorityDialog(final EditText et, int type) {
        String[] str = null;
        if (type == 0x11) {//
            str = new String[]{"特急",
                    "紧急", "一般", "不紧急"
            };
        } else if (type == 0x12) {
            str = new String[]{
                    "任务", "日程"
            };
        }
        OptionPicker sex_option = new OptionPicker(this, str);
        sex_option.setOffset(1);
        sex_option.setSelectedIndex(1);
        sex_option.setTextSize(18);
        sex_option.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(int position, String option) {
                et.setText(option);
            }

        });
        sex_option.show();
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_date_start:
                showDateDialog(true);
                break;
            case R.id.tv_date:
                if (tv_date_start.getText() != null && tv_date_start.getText().toString().length() > 0) {
//                    showDialog(tv_date, false);
                    showDateDialog(false);
                } else {
                    ToastUtil.showToast(ct, "请先填写开始日期");
                }
//                showDateDialog((FormEditText) v.findViewById(R.id.tv_date));
                break;
            case R.id.tv_notice:
                showNoticeDialog();
                break;
            case R.id.tv_priority:
                String[] str = new String[]{"特急", "紧急", "一般", "不紧急"};
                ArrayList<SelectBean> beans = new ArrayList<>();
                SelectBean bean = null;
                for (String e : str) {
                    bean = new SelectBean();
                    bean.setName(e);
                    bean.setClick(false);
                    beans.add(bean);
                }
                intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putParcelableArrayListExtra("data", beans);
                intent.putExtra("title", "紧急程度");
                startActivityForResult(intent, 0x22);
                break;
            case R.id.tv_executive:
                if (isB2b) {
                    HashMap param = new HashMap<>();
                    param.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("param", param);
                    intent = new Intent(ct, SelectActivity.class);
                    intent.putExtra("type", 1);
                    intent.putExtra("isSingle", false);
                    intent.putExtra("reid", R.style.OAThemeMeet);
                    intent.putExtras(bundle);
                    intent.putExtra("key", "data");
                    intent.putExtra("method", "get");
                    intent.putExtra("showKey", "emname");
                    intent.putExtra("action", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getUsersInfo);//
                    startActivityForResult(intent, 0x11);
                } else {
                    intent = new Intent(ct, UserSelectActivity.class);
                    intent.putExtra("single", false);
                    intent.putExtra("isme", true);
                    intent.putExtra("users", tv_executive.getText().toString().trim());
                    startActivityForResult(intent, 0x11);
                }

                break;
        }
    }

    private static final int codeWhat = 0x11;
    private static final int endCodeWhat = 0x12;
    private String tv_code;

    private void getCodeByNet() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
        final Map<String, Object> param = new HashMap<>();
        String caller = "ProjectTask";
        param.put("caller", caller);
        param.put("type", 2);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, codeWhat, null, null, "post");
    }

    private void setCodeByNet(String code) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateLastdate.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("bc_code", code);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, endCodeWhat, null, null, "post");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (requestCode == 0x11 && resultCode == 0x11) {

            ArrayList<Employees> list = data.getParcelableArrayListExtra("data");
            if (ListUtils.isEmpty(list)) return;
            StringBuilder str = new StringBuilder();
            for (Employees e : list) {
                str.append("," + e.getEm_name());
            }
            str.delete(0, 1);
            tv_executive.setText(str.toString());
        } else if (requestCode == 0x22 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) return;
            tv_priority.setText(StringUtil.isEmpty(b.getName()) ? "" : b.getName());
        } else if (isB2b && requestCode == 0x11) {
            ArrayList<SelectBean> temps = data.getParcelableArrayListExtra("data");
            if (ListUtils.isEmpty(temps)) return;
            StringBuilder codes = new StringBuilder();
            StringBuilder names = new StringBuilder();
            for (SelectBean bean : temps) {
                JSONObject json = JSON.parseObject(bean.getJson());
                codes.append(json.getString("emcode") + ",");
                names.append(bean.getName() + ",");
            }
            StringUtil.removieLast(codes);
            StringUtil.removieLast(names);
            tv_executive.setText(names);
            tv_executive.setTag(codes);
        }
    }
}
