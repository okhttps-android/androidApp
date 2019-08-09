package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.OAConfig;
import com.core.model.SelectBean;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.SwitchView;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.widget.view.selectcalendar.bean.Data;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;

public class AddMeetingActivity extends BaseActivity implements View.OnClickListener, RecognizerDialogListener {
    private TextView start_tv;
    private SwitchView create_ric_sv;
    private TextView end_tv;
    private TextView users_tv;
    private TextView user_tv;
    private TextView location_et;
    private EditText name_et;
    private EditText about_et;
    private TextView tag_et;
    private int voice_type = 0;
    private AddMeetingActivity ct;
    private final int whatSignin = 0x12;
    private final int whatAdd = 0x11;
    private String baseUrl;
    private int maId = 0;
    private long time = System.currentTimeMillis();
    ArrayList<SelectEmUser> entities = null;//成员
    //已选择参会人员存放入listpeoplechoose
    //ArrayList<Employees> listpeoplechoose = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.getData().get("result");

            switch (msg.what) {
                case 0x16:
                    if (parseObject(message).containsKey("success") && parseObject(message).getBoolean("success")) {
                        if (ma_code != null)
                            signinAble(parseObject(message).getInteger("id"));
                    } else {
                        ToastUtil.showToast(ct, "获取id错误");
                    }
                    submiting = false;
                    break;
                case whatSignin:
                    JSONObject json = parseObject(message);
                    if (json.getBoolean("success") != null && json.getBoolean("success")) {
                        // 创建会议成功  开始日程
                        if (create_ric_sv.isChecked()) {
                            ToastUtil.showToast(ct, R.string.add_meet_success_adding_task);
                            CommonInterface.getInstance().getCodeByNet("ProjectTask", new CommonInterface.OnResultListener() {
                                @Override
                                public void result(boolean isOk, int what, String message) {
                                    sendHttpResquest(message);
                                }
                            });
                        } else {
                            ToastUtil.showToast(ct, R.string.add_meet_success);
                            sumitExamine();
//                            endOfActivity();
                        }
//                        CreateRoomUtil.getInstance().createRoom(ct, entities, name_et.getText().toString() + "会议群", "创建为了提醒会议成员", new CreateRoomUtil.OnCreateRoomListener() {
//                            @Override
//                            public void result(boolean isOk) {
//                                if (isOk) {
//                                    ToastUtil.showToast(ct, "已经为您的会议建立群组");
//                                    Intent intent = new Intent();
//                                    intent.putExtra("data", "data");
//                                    setResult(0x14, intent);
//                                    progressDialog.dismiss();
//                                    finish();
//                                }
//                            }
//                        });
//                        createRoom(name_et.getText().toString() + "会议群", "创建为了提醒会议成员");
                    } else {
                        progressDialog.dismiss();
                        ToastUtil.showToast(ct, R.string.error_try_agen);
                    }
                    break;
                case whatAdd:
                    break;
                case 0x15://添加会议日程
                    progressDialog.dismiss();
                    ToastUtil.showToast(ct, R.string.meet_add_task_success);
                    sumitExamine();
//                    endOfActivity();
                    break;
                case 0x13:
                    endOfActivity();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    if (System.currentTimeMillis() - time > 10000) {
                        ToastUtil.showToast(ct, R.string.error_try_agen);
                        return;
                    }

                    LogUtil.i("gong","message="+message);
                    ToastUtil.showToast(ct, message == null ? "" : message);
                    break;
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.push == item.getItemId()) {
            startActivity(new Intent(ct, MeetingActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    //TODO 修改保存按钮
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.save) {
//            CommonInterface.getInstance().getCodeByNet("Meetingroomapply", new CommonInterface.OnResultListener() {
//                @Override
//                public void result(boolean isOk, int what, String message) {
//                    ma_code = message;
//                    getIdByNet();
//                }
//            });
//
//        }
//        return super.onOptionsItemSelected(item);
//    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_visit_save, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meeting);
        AndroidBug5497Workaround.assistActivity(this);
        ViewUtils.inject(this);
        baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        ct = this;
        initView();
    }


    private void initView() {
        if (getIntent()!=null){
            whichPage=getIntent().getStringExtra("whichPage");
        }
        start_tv = (TextView) findViewById(R.id.start_tv);
        create_ric_sv = (SwitchView) findViewById(R.id.create_ric_sv);
        end_tv = (TextView) findViewById(R.id.end_tv);
        users_tv = (TextView) findViewById(R.id.users_tv);
        location_et = (TextView) findViewById(R.id.location_et);
        name_et = (EditText) findViewById(R.id.name_et);
        about_et = (EditText) findViewById(R.id.about_et);
        tag_et = (TextView) findViewById(R.id.tag_et);
        user_tv = (TextView) findViewById(R.id.user_tv);
        String name = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(name))
            name = MyApplication.getInstance().mLoginUser.getNickName();
        user_tv.setText(name);
        findViewById(R.id.bt_save).setOnClickListener(this);
        findViewById(R.id.start_rl).setOnClickListener(this);
        findViewById(R.id.end_rl).setOnClickListener(this);
        findViewById(R.id.users_rl).setOnClickListener(this);
        findViewById(R.id.tag_rl).setOnClickListener(this);
        findViewById(R.id.location_rl).setOnClickListener(this);
        findViewById(R.id.voice_name_iv).setOnClickListener(this);
        findViewById(R.id.voice_introduce_iv).setOnClickListener(this);
        start_tv.setText(TimeUtils.f_long_2_str(System.currentTimeMillis() + 1000));
        end_tv.setText(TimeUtils.f_long_2_str(System.currentTimeMillis() + (60 * 1000 * 2 * 60) + 1000));
    }

    private String ma_code = null;

    @Override
    public void onClick(View view) {
        Intent intent = null;
        if (view.getId() == R.id.start_rl) {
            turn2SelectCalendar();
        } else if (view.getId() == R.id.end_rl) {
            turn2SelectCalendar();
        } else if (view.getId() == R.id.users_rl) {
            intent = new Intent("com.modular.main.SelectCollisionActivity");
            SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                    .setSureText(getString(R.string.common_sure))
                    .setSelectType(getString(R.string.member))
                    .setTitle(getString(R.string.select_user))
                    .setSelectCode(getSelectCode());
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, 0x21);
        } else if (view.getId() == R.id.tag_rl) {
            getMeetTag();
        } else if (view.getId() == R.id.location_rl) {
            getMeetRoom();
        } else if (view.getId() == R.id.voice_name_iv) {
            voice_type = 1;
            RecognizerDialog dialog = new RecognizerDialog(this, null);
            dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
            dialog.setListener(this);
            dialog.show();
        } else if (view.getId() == R.id.voice_introduce_iv) {
            voice_type = 2;
            RecognizerDialog dialog = new RecognizerDialog(this, null);
            dialog = new RecognizerDialog(this, null);
            dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
            dialog.setListener(this);
            dialog.show();
        } else if (view.getId() == R.id.bt_save) {
            if (submiting) {

            } else {
                CommonInterface.getInstance().getCodeByNet("Meetingroomapply", new CommonInterface.OnResultListener() {
                    @Override
                    public void result(boolean isOk, int what, String message) {
                        ma_code = message;
                        getIdByNet();
                    }
                });
                submiting = true;
            }
        }
    }

    private boolean submiting = false;

    private String whichPage="";
    private void endOfActivity() {
//        Intent intent = new Intent();
//        intent.putExtra("data", "data");
//        setResult(0x14, intent);
//        finish();
//        progressDialog.dismiss();
        if (!TextUtils.isEmpty(whichPage)&&whichPage.equals("ScheduleActivity")){
            setResult(0x11);
            finish();
        }else{
            startActivity(new Intent(ct, MeetingActivity.class));
            finish();
        }
    }

    //判断是否可以进行创建
    private void signinAble(int id) {
        if (StringUtil.isEmpty(name_et.getText().toString())) {
            ToastUtil.showToast(this, R.string.meet_name_error);
            return;
        }
        if (StringUtil.isEmpty(start_tv.getText().toString())) {
            ToastUtil.showToast(this, R.string.start_time_error);
            return;
        }
        if (StringUtil.isEmpty(end_tv.getText().toString())) {
            ToastUtil.showToast(this, R.string.end_time_error);
            return;
        }
        if (StringUtil.isEmpty(location_et.getText().toString())) {
            ToastUtil.showToast(this, R.string.position_error);
            return;
        }
        if (StringUtil.isEmpty(user_tv.getText().toString())) {
            ToastUtil.showToast(this, R.string.meet_manager_error);
            return;
        }
        if (entities == null || entities.size() <= 0) {
            ToastUtil.showToast(this, R.string.join_numer_error);
            return;
        }
        if (start_tv.getText().toString().compareTo(end_tv.getText().toString()) > 0) {
            ToastUtil.showToast(this, R.string.not_time_start_biger_end);
            return;
        }
        if (start_tv.getText().toString().toString().compareTo(end_tv.getText().toString().trim()) >= 0) {
            ToastUtil.showToast(ct, R.string.not_time_start_biger_end);
            return;
        }
        StringBuilder builder = new StringBuilder();
        StringBuilder builderId = new StringBuilder();
        for (int i = 0; i < entities.size(); i++) {
            if (i == 0) {
                builder.append(entities.get(i).getEmName());
                builderId.append("employee#" + entities.get(i).getEmId());
            } else {
                builder.append(";" + entities.get(i).getEmName());
                builderId.append(";employee#" + entities.get(i).getEmId());
            }
        }
        doSignin(builder.toString(), builderId.toString(), id);
    }

    private void showDateDialog(TextView tv) {
        showDateDialog(tv, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
    }

    private void showDateDialog(final TextView tv, int yeas, int month, int day, int hh, int mm) {
        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2010, 2030);
        picker.setSelectedItem(yeas, month, day, hh, mm);
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00";
                if (tv.getId() == R.id.start_tv) {
                    if (!StringUtil.isEmpty(end_tv.getText().toString())) {
                        if (end_tv.getText().toString().compareTo(time) < 0) {
                            ToastUtil.showToast(ct, R.string.not_time_start_biger_end);
                        } else {
                            start_tv.setText(time);
                        }
                    } else {
                        start_tv.setText(time);
                    }
                } else if (tv.getId() == R.id.end_tv) {
                    if (time.compareTo(start_tv.getText().toString()) <= 0) {
                        ToastUtil.showToast(ct, R.string.not_time_start_biger_end);
                    } else {
                        end_tv.setText(time);
                    }
                } else {
                    ToastUtil.showToast(ct, R.string.please_input_start_time);
                }
            }
        });
        picker.show();
    }

    private void getMeetRoom() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("caller", "MeetingRoom");
        param.put("condition", "mr_statuscode='AUDITED'");
        param.put("currentMaster", CommonUtil.getSharedPreferences(this, "erp_master"));
        param.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        param.put("page", 1);
        param.put("pageSize", 30);
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "listdata");
        intent.putExtra("showKey", "mr_name");
        intent.putExtra("action", "mobile/common/list.action");
        intent.putExtra("title", getResources().getString(R.string.meet_position));
        startActivityForResult(intent, 0x23);
    }

    private void getMeetTag() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("caller", "Meetingroomapply");
        param.put("condition", "1=1");
        param.put("currentMaster", CommonUtil.getSharedPreferences(this, "erp_master"));
        param.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        param.put("field", "ma_tag");
        param.put("page", 1);
        param.put("pageSize", 30);
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combos");
        intent.putExtra("showKey", "DLC_VALUE");
        intent.putExtra("action", "mobile/crm/getBusinessChanceCombo.action");
        intent.putExtra("title", getResources().getString(R.string.meet_tag));
        startActivityForResult(intent, 0x22);
    }


    private void getIdByNet() {
        time = System.currentTimeMillis();
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getId.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("seq", "Meetingroomapply_SEQ");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x16, null, null, "post");
    }

    //提交表单
    private void doSignin(String gruop, String ids, int id) {
        maId = id;
        time = System.currentTimeMillis();
        progressDialog.show();
        if (baseUrl == null) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }

        String url = baseUrl + "oa/meeting/saveMeetingroomapply.action";
        final Map<String, Object> param = new HashMap<>();
        String form = "{" +
                "\"ma_mrcode\":" + "\"" + mr_code + "\",\n" +
                "\"ma_code\":" + "\"" + ma_code + "\",\n" +
                "\"ma_group\":" + "\"" + gruop + "\",\n" +
                "\"ma_groupid\":" + "\"" + ids + "\",\n" +
                "\"ma_id\":" + "\"" + id + "\",\n" +
                "\"ma_isturndoc\":" + "\"" + "否" + "\",\n" +
                "\"ma_mrname\":" + "\"" + location_et.getText().toString() + "\",\n" +
                "\"ma_recorddate\":" + "\"" + DateFormatUtil.long2Str(DateFormatUtil.YMD) + "\",\n" +
                "\"ma_recorder\":" + "\"" + user_tv.getText().toString() + "\",\n" +
                "\"ma_remark\":" + "\"" + about_et.getText().toString() + "\",\n" +
                "\"ma_starttime\":" + "\"" + start_tv.getText().toString() + "\",\n" +
                "\"ma_endtime\":" + "\"" + end_tv.getText().toString() + "\",\n" +
                "\"ma_status\":" + "\"" + "已提交" + "\",\n" +
                "\"ma_statuscode\":" + "\"" + "ENTERING" + "\",\n" +
                "\"ma_tag\":" + "\"" + tag_et.getText().toString() + "\",\n" +
                "\"ma_theme\":" + "\"" + name_et.getText().toString() + "\"\n" +
                "}";
        String caller = "Meetingroomapply";//旧的 Meetingroomapply
        param.put("caller", caller);
        param.put("param", "[]");
        param.put("formStore", form);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, whatSignin, null, null, "post");
    }

    private void sendHttpResquest(String code) {
        time = System.currentTimeMillis();
        progressDialog.show();
        StringBuilder resourcename = new StringBuilder();
        if (entities != null && entities.size() > 0) {
            for (SelectEmUser e : entities) {
                resourcename.append("," + e.getEmName());
            }
            resourcename.delete(0, 1);
        }
        String formStore = "{\n" +
                "\"taskcode\":\"" + code + "\",\n" +//编号
                "\"name\":\"" + name_et.getText().toString() + "\",\n" +//名称
                "\"resourcename\":\"" +//执行人
                resourcename.toString() +
                "\",\n" +
                "\"startdate\":\"" +//结束时间
                start_tv.getText().toString() +
                "\",\n" +
                "\"enddate\":\"" +//结束时间
                end_tv.getText().toString() +
                "\",\n" +
                "\"tasklevel\":\"" +
                "紧急" +//任务优先级
                "\",\n" +
                "\"taskorschedule\":\"" + "Schedule"//任务类型
                + "\",\n" +
                "\"custname\":\"" +
                "" +//联系人
                "\",\n" +
                "\"timealert\":\"" +
                "" +//提醒
                "\",\n" +
                "\"description\":\"" +
                "通知参加" + name_et.getText().toString() + "会议" +
                "\",\n" +
                "}";
        String url = CommonUtil.getAppBaseUrl(ct) + "plm/task/addbilltask.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, 0x15, null, null, "post");
    }

    //提交审批流
    private void sumitExamine() {
        String url = CommonUtil.getAppBaseUrl(ct) + "oa/meeting/submitMeetingroomapply.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "Meetingroomapply");
        params.put("id", maId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, 0x13, null, null, "post");
    }


    private String mr_code;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void turn2SelectCalendar() {
        startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                        .putExtra("startDate", StringUtil.getTextRexHttp(start_tv))
                        .putExtra("endDate", StringUtil.getTextRexHttp(end_tv))
                        .putExtra("hasMenu", false)
//                        .putExtra("id", categoryIndex)
//                        .putExtra("field", data.getField())
                        .putExtra("object", new Data())
                        .putExtra("caller", "Workovertime")
                , 0x30);
    }

    private SelectEmUser getMeForDB() {
        DBManager manager = new DBManager();
        String whichsys = CommonUtil.getMaster();
        String em_code = CommonUtil.getEmcode();
        String[] selectionArgs = {em_code, whichsys};
        String selection = "em_code=? and whichsys=? ";
        //获取数据库数据
        EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
        manager.closeDB();
        if (bean != null) {
            return new SelectEmUser(bean);
        }
        return null;
    }

    @Override
    //从列表选择的人员展示
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (requestCode == 0x21 && resultCode == 0x20) {
            entities = data.getParcelableArrayListExtra("data");
            boolean isHasMe = false;
            if (entities != null) {
                String emCode = CommonUtil.getEmcode();
                for (SelectEmUser user : entities) {
                    if (!StringUtil.isEmpty(emCode) && !StringUtil.isEmpty(user.getEmCode()) && user.getEmCode().equals(emCode)) {
                        isHasMe = true;
                        break;
                    }
                }
            }
            if (!isHasMe) {
                SelectEmUser meUser = getMeForDB();
                if (meUser != null) {
                    entities.add(0, meUser);
                }
            }
            if (!ListUtils.isEmpty(entities)) {
                StringBuilder builder = new StringBuilder();
                for (SelectEmUser e : entities) {
                    builder.append("," + e.getEmName());
                }
                if (builder.length() > 2)
                    builder.delete(0, 1);
                users_tv.setText(builder.toString());
            }
        } else if (requestCode == 0x22 && resultCode == 0x20) {//标签
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) return;
            tag_et.setText(StringUtil.isEmpty(b.getName()) ? "" : b.getName());
        } else if (requestCode == 0x23 && resultCode == 0x20) {//地点
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) {
                return;
            } else {
                location_et.setText((StringUtil.isEmpty(b.getName()) ? "" : b.getName()));
                String json = b.getJson();
                //String o = (String) b.getObject();
                if (StringUtil.isEmpty(json)) {
                    return;
                }
                if (JSONUtil.validate(json)) {
                    JSONObject object = JSON.parseObject(json);
                    if (object == null) return;
                    mr_code = object.containsKey("mr_code") ? object.getString("mr_code") : "";

                }
            }
        } else if (requestCode == 0x30 && resultCode == 0x11) {
            String startDate = data.getStringExtra("startDate");
            String endDate = data.getStringExtra("endDate");
            start_tv.setText(StringUtil.getMessage(startDate) + ":00");
            end_tv.setText(StringUtil.getMessage(endDate) + ":00");
        }
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        if (voice_type == 1) {
            String text = JsonParser.parseIatResult(recognizerResult.getResultString());
            name_et.setText(name_et.getText().toString() + CommonUtil.getPlaintext(text));
        } else if (voice_type == 2) {
            String text = JsonParser.parseIatResult(recognizerResult.getResultString());
            about_et.setText(about_et.getText().toString() + text);
        }
    }

    @Override
    public void onError(SpeechError speechError) {

    }

    public String getSelectCode() {
        if (ListUtils.isEmpty(entities)) return "";
        StringBuilder builder = new StringBuilder();
        for (SelectEmUser e : entities) {
            builder.append(e.getEmCode() + ",");
        }
        StringUtil.removieLast(builder);
        return builder.toString();
    }
}
