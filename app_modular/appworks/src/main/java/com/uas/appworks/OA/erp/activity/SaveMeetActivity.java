package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.widget.view.Activity.SelectActivity;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.OA.erp.model.MeetingDocBean;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.fastjson.JSON.parseObject;

//会议纪要
public class SaveMeetActivity extends BaseActivity {
    private EditText summary_et;
    private EditText resolution_et;
    private EditText wreckage_et;
    private EditText follow_et;
    private int id;
    private String noid = null;
    private long oldTime = System.currentTimeMillis();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            String message = (String) msg.getData().get("result");
            switch (msg.what) {
                case 0x11://提交会议纪要保存
                    JSONObject json = parseObject(message);
                    if (json.containsKey("success") && json.getBoolean("success")) {
                        updataCode();//保存成功后更新编号
                    } else {
                        ToastUtil.showToast(SaveMeetActivity.this, R.string.make_adeal_failed);
                    }
                    break;
                case 0x12://保存后更新编号后提交审批流
                    submitMeeting(id);
                    break;
                case 0x13:
                    try {
                        if (!StringUtil.isEmpty(message) && message.length() > 10) {
                            if (JSONUtil.validate(message)) {
                                JSONArray array = JSON.parseObject(message).getJSONArray("assigns");
                                JSONObject object = array.getJSONObject(0);
                                noid = object.getString("JP_NODEID");
                                JSONArray data = object.getJSONArray("JP_CANDIDATES");
                                sendToSelect(data);
                            }
                        } else {//只有一个审批人
                            //TODO 只有一个审批人
//                            submitMeeting(id);
                        }
                    } catch (Exception e) {
                    }
                    break;
                case 0x14:
                    finish();
                    break;
                case 0x16://获取id
                    if (parseObject(message).containsKey("success") && parseObject(message).getBoolean("success")) {
                        id = parseObject(message).getInteger("id");
                        CommonInterface.getInstance().getCodeByNet("MeetingDoc", new CommonInterface.OnResultListener() {
                            @Override
                            public void result(boolean isOk, int  result, String message) {
                                saveAble(message, id);
                            }
                        });

                    } else {
                    }
                    break;
                case 0x17://提交审批流
//                    TODO 先关闭发布版本
                    judgeApprovers();
                    break;
                default:
                    if (System.currentTimeMillis() - oldTime > 20000) {//请求大于20秒
                        ToastUtil.showToast(ct, R.string.networks_out);
                    }
                    if (!StringUtil.isEmpty(message) && JSONUtil.validate(message)) {
                        JSONObject errMessage = JSON.parseObject(message);
                        if (errMessage.containsKey("exceptionInfo")) {
                            ToastUtil.showToast(ct, StringUtil.isEmpty(errMessage.getString("exceptionInfo")) ? "" : errMessage.getString("exceptionInfo"));
                        }
                    } else {
                        ToastUtil.showToast(ct, StringUtil.getChinese(message));
                    }
                    break;
            }
        }
    };


    private MeetingDocBean bean;

    private void getIdByNet() {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getId.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("seq", "MEETINGDOC_SEQ");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x16, null, null, "post");
    }

    //提交动作，增加判断节点是否有多人的情况
    private void judgeApprovers() {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "MeetingDoc");
        param.put("id", id);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x13, null, null, "post");
    }

    //提交动作，增加判断节点是否有多人的情况
    private void selectApprovers(String emName) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", emName);
        params.put("nodeId", noid);
        param.put("_noc", 1);
        param.put("params", JSONUtil.map2JSON(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x14, null, null, "post");
    }

    //保存会议后提交审批流
    private void submitMeeting(int id) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "oa/meeting/submitMeetingDoc.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "MeetingDoc");
        param.put("id", id);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x17, null, null, "post");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            getIdByNet();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visit_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_meet);
        AndroidBug5497Workaround.assistActivity(this);
        ViewUtils.inject(this);
        bean = getIntent().getParcelableExtra("data");
        initIDs();
    }

    private void initIDs(){
        summary_et = (EditText) findViewById(R.id.summary_et);
        resolution_et = (EditText) findViewById(R.id.resolution_et);
        wreckage_et = (EditText) findViewById(R.id.wreckage_et);
        follow_et = (EditText) findViewById(R.id.follow_et);
    }

    private void sendToSelect(JSONArray data) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        for (int i = 0; i < data.size(); i++) {
            bean = new SelectBean();
            bean.setName(data.getString(i));
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", getString(R.string.select_approvel_people));
        startActivityForResult(intent, 0x22);
    }

    private void saveAble(String code, int id) {
        StringBuilder builder = new StringBuilder();
        String chche = "";





        chche = summary_et.getText().toString();
        if (StringUtil.isEmpty(chche)) {
            ToastUtil.showToast(ct, R.string.input_meeting_note);
            return;
        }
        builder.append("会议纪要:" + chche + "             ");
        chche = resolution_et.getText().toString();
        if (StringUtil.isEmpty(chche)) {
            ToastUtil.showToast(ct, R.string.input_meeting_resolution);
            return;
        }
        builder.append("会议决议:" + chche + "             ");
        chche = wreckage_et.getText().toString();
        if (StringUtil.isEmpty(chche)) {
            ToastUtil.showToast(ct, R.string.input_meeting_left);
            return;
        }
        builder.append("遗留问题:" + chche + "             ");
        chche = follow_et.getText().toString();
        if (StringUtil.isEmpty(chche)) {
            ToastUtil.showToast(ct, R.string.input_meeting_next);
            return;
        }
        doSave(code, builder.toString(), id);
    }

    private void updataCode() {
        //获取网络数据
        if (bean == null) {
            ToastUtil.showToast(this, R.string.error_system_findunknow_error);
            return;
        }
        oldTime = System.currentTimeMillis();
        String code = getIntent().getStringExtra("code");
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") + "mobile/crm/updateMatype.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("ma_code", code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, handler, headers, 0x12, null, null, "post");
    }

    /**
     * @param fileno   文号、编号
     * @param strChche 会议纪要填写内容
     * @param id
     */
    private void doSave(String fileno, String strChche, int id) {
        //获取网络数据
        if (bean == null) {
            ToastUtil.showToast(this, "系统出错，请稍后再试");
            return;
        }
        oldTime = System.currentTimeMillis();
        progressDialog.show();
        bean.setMd_contents(strChche);
        bean.setMd_fileno(fileno);
        bean.setMd_id(id);
        bean.setMd_recorderdate(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        Map<String, Object> formStoreMap = new HashMap<>();
        formStoreMap.put("md_id", bean.getMd_id());
        formStoreMap.put("md_fileno", bean.getMd_fileno());
        formStoreMap.put("md_recorder", bean.getMd_recorder());
        formStoreMap.put("md_recorderdate", bean.getMd_recorderdate());
        formStoreMap.put("md_status", bean.getMd_status());
        formStoreMap.put("md_title", bean.getMd_title());
        formStoreMap.put("md_mtname", bean.getMd_mtname());
        formStoreMap.put("md_meetingname", bean.getMd_meetingname());
        formStoreMap.put("md_meetingcode", bean.getMd_meetingcode());
        formStoreMap.put("md_mrcode", bean.getMd_mrcode());
        formStoreMap.put("md_mrname", bean.getMd_mrname());
        formStoreMap.put("md_starttime", bean.getMd_starttime());
        formStoreMap.put("md_statuscode", bean.getMd_statuscode());
        formStoreMap.put("md_endtime", bean.getMd_endtime());
        formStoreMap.put("md_meetingplace", bean.getMd_mrname());
        formStoreMap.put("md_group", bean.getMd_group());
        formStoreMap.put("md_attachs", bean.getMd_attachs());
        formStoreMap.put("md_contents", bean.getMd_contents());
        formStoreMap.put("md_groupid", bean.getMd_groupid());
        formStoreMap.put("md_meetingconvener", bean.getMd_recorder());
        formStoreMap.put("md_meetingparticipants", bean.getMd_group());
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") + "oa/meeting/saveMeetingDoc.action";
        Map<String, Object> param = new HashMap<>();
        String formStore = JSONUtil.map2JSON(formStoreMap);
        String caller = "MeetingDoc";
        param.put("caller", caller);
        param.put("formStore", formStore);
        param.put("sessionId", CommonUtil.getSharedPreferences(this, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, handler, headers, 0x11, null, null, "post");
    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) return;
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name))
                selectApprovers(name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x22 && resultCode == 0x20) {
            if (requestCode == 0x22) {
                if (data != null) {
                    SelectBean b = data.getParcelableExtra("data");
                    if (b == null) return;
                    String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                    getEmnameByReturn(name);
                } else finish();
            }
        }
    }
}
