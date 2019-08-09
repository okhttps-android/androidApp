package com.xzjmyk.pm.activity.ui.erp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.TimeUtils;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.ui.erp.model.LeaveAddEntity;
import com.xzjmyk.pm.activity.ui.erp.model.LeaveUpdateEntity;
import com.xzjmyk.pm.activity.ui.platform.pageforms.FormDetailActivity;
import com.xzjmyk.pm.activity.ui.platform.pageforms.LeavePageActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by FANGlh on 2017/3/22.
 * function:独立版本请假单
 */
public class PlatLeaveAddActivity extends BaseActivity implements RecognizerDialogListener,View.OnClickListener{
    private static final int DOC_FIRST_SAVE = 0x322;
    private static final int DOC_UPDATE_SAVE = 0x323;
    @ViewInject(R.id.et_leave_category)
    private FormEditText et_leave_category;
    @ViewInject(R.id.tv_start_time)
    private FormEditText tv_start_time;
    @ViewInject(R.id.tv_end_time)
    private FormEditText tv_end_time;
    @ViewInject(R.id.et_leave_reason)
    private FormEditText et_leave_reason;
    @ViewInject(R.id.voice_search_iv)
    private ImageView voice_search_iv;
    @ViewInject(R.id.bt_save)
    private Button bt_save;
    private String[] mLeaveTypes;
    private String resubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.plat_leave_add);
        ViewUtils.inject(this);
        mLeaveTypes = MyApplication.getInstance().getResources().getStringArray(R.array.platLeave_type_list);
        tv_start_time.setKeyListener(null);
        tv_end_time.setKeyListener(null);
        et_leave_category.setKeyListener(null);

        tv_start_time.setFocusable(false);
        tv_end_time.setFocusable(false);
        et_leave_category.setFocusable(false);
        String current_time = TimeUtils.f_long_2_str(System.currentTimeMillis());
        tv_start_time.setText(current_time);
        tv_end_time.setText(current_time);
        et_leave_category.setOnClickListener(this);
        tv_start_time.setOnClickListener(this);
        tv_end_time.setOnClickListener(this);
        et_leave_reason.setOnClickListener(this);
        voice_search_iv.setOnClickListener(this);
        bt_save.setOnClickListener(this);

        Intent intent = getIntent();
        resubmit = intent.getStringExtra("submittype");
        if (!StringUtil.isEmpty(resubmit)){
            doShowResubmitData(intent);
        }

    }

    private void doShowResubmitData(Intent intent) {
        String date = intent.getStringExtra("data");
        mkeyValue = intent.getIntExtra("mkeyValue", -1);
        String leave_category = JSON.parseObject(date).getString(getString(R.string.leave_type));
        String leave_reason = JSON.parseObject(date).getString(getString(R.string.leave_reason));
        String start_time = JSON.parseObject(date).getString(getString(R.string.start_time));
        String end_time = JSON.parseObject(date).getString(getString(R.string.end_time));

        et_leave_category.setText(leave_category);
        et_leave_reason.setText(leave_reason);
        tv_start_time.setText(start_time);
        tv_end_time.setText(end_time);

        bt_save.setText(getString(R.string.common_resubmit_button));
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        et_leave_reason.setText(et_leave_reason.getText().toString() + text);
    }

    @Override
    public void onError(SpeechError speechError) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.et_leave_category:
                selectLeaveType();
                break;
            case R.id.tv_start_time:
//                showDateDialog(this, tv_start_time);

                startActivityForResult(new Intent(mContext,SelectCalendarActivity.class)
                        .putExtra("startDate", tv_start_time.getText().toString())
                        .putExtra("endDate", tv_end_time.getText().toString())
                        .putExtra("hasMenu",true)
                        .putExtra("caller", "Ask4Leave")
                        ,0x30);
                break;
            case R.id.tv_end_time:
                startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                        .putExtra("startDate", tv_start_time.getText().toString())
                        .putExtra("endDate", tv_end_time.getText().toString())
                        .putExtra("hasMenu", true)
                        .putExtra("caller", "Ask4Leave")
                        , 0x30);
                break;
            case R.id.et_leave_reason:

                break;
            case R.id.voice_search_iv:
                RecognizerDialogUtil.showRecognizerDialog(ct,this);
                break;
            case R.id.bt_save:
                if (et_leave_reason.testValidity() &&
                        tv_start_time.testValidity() &&
                        tv_end_time.testValidity() &&
                        et_leave_category.testValidity()
                        ) {
                    boolean falg = ViewUtil.isCheckDateTime(tv_start_time.getText()
                                    .toString(),
                            tv_end_time.getText().toString(), "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage(getString(R.string.endT_large_startT));
                    } else {
                        if (MyApplication.getInstance().isNetworkActive()){
                            httpSave();
                        }else {
                            ToastMessage(getResources().getString(R.string.networks_out));
                        }
                    }
                }
                break;
        }
    }

    /**
     * @注释：保存
     */
    private String jsondata;
    int mkeyValue = -1;

    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case DOC_FIRST_SAVE:
                    if (msg.getData() != null){
                        try{
                            String doc_first_save_result = msg.getData().getString("result");
                            Log.d("doc_first_save_result", doc_first_save_result);
                            if(StringUtil.isEmpty(doc_first_save_result)) return;
                            Toast.makeText(getApplicationContext(),getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
                            mkeyValue = JSON.parseObject(doc_first_save_result).getIntValue("va_id");
                            if (mkeyValue != -1){
                                jumpTODetails(mkeyValue);
                            }
                            progressDialog.dismiss();
                        }catch (JSONException e){
                            e.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        bt_save.setEnabled(true);
                        progressDialog.dismiss();
                    }
                break;

                case DOC_UPDATE_SAVE:
                    if (msg.getData() != null) {
                        try{
                            if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                                String doc_update_save_result = msg.getData().getString("result");
                                Log.d("doc_update_save_result",doc_update_save_result);
                                Toast.makeText(getApplicationContext(),getString(R.string.update_success),Toast.LENGTH_LONG).show();
                                jumpTODetails(mkeyValue);
                            }else {
                                bt_save.setEnabled(true);
                                progressDialog.dismiss();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        bt_save.setEnabled(true);
                        progressDialog.dismiss();
                    }
                    break;
                default:
                    try{
                        if (msg.getData() != null) {
                            if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                                ToastMessage(msg.getData().getString("result"));
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
            }
        }
    };

    private void jumpTODetails(int mkeyValue) {
        JSONObject map = new JSONObject(true);
        map.put(getString(R.string.leave_type), et_leave_category.getText().toString());
//        map.put("单据状态", "已提交");
        map.put(getString(R.string.start_time), tv_start_time.getText().toString());
        map.put(getString(R.string.end_time), tv_end_time.getText().toString());
        map.put(getString(R.string.leave_reason), et_leave_reason.getText().toString());
        Log.d("mkeyValue", mkeyValue + "");
        LogUtil.d(map.toJSONString());
        startActivity(new Intent(PlatLeaveAddActivity.this, FormDetailActivity.class)
                .putExtra("data", map.toString())
                .putExtra("title", getString(R.string.vacation_doc))
                .putExtra("mkeyValue", mkeyValue)
                .putExtra("whichpage", 1)
                .putExtra("status", "已提交")
                .putExtra("ADDUI", "ADDUI"));
        bt_save.setEnabled(true);
        progressDialog.dismiss();
        finish();
    }

    public void httpSave() {
        bt_save.setEnabled(false);
        progressDialog.show();
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().leave_save_url;
        Map<String, Object> params = new HashMap<String, Object>();
        if (mkeyValue != -1){
            LeaveUpdateEntity entity = getSaveUpdateJsonData();
            jsondata = FlexJsonUtil.toJson(entity);
        }else {
            LeaveAddEntity entity = getSaveJsonData();
            jsondata = FlexJsonUtil.toJson(entity);
        }
        System.out.println("url:" + url);
        System.out.println("formStore=" + jsondata);
        params.put("formStore", jsondata);
        params.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        if (mkeyValue != -1){
            ViewUtil.httpSendRequest(ct, url, params, handler, headers, DOC_UPDATE_SAVE, null, null, "post");
        }else {
            ViewUtil.httpSendRequest(ct, url, params, handler, headers, DOC_FIRST_SAVE, null, null, "post");
        }
    }

    private LeaveUpdateEntity getSaveUpdateJsonData() {
        LeaveUpdateEntity entity = new LeaveUpdateEntity();
        entity.setVa_id(mkeyValue);
        entity.setVa_vacationtype(et_leave_category.getText().toString());
        entity.setVa_remark(et_leave_reason.getText().toString());
        entity.setVa_startime(tv_start_time.getText().toString());
        entity.setVa_endtime(tv_end_time.getText().toString());
        entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));
        return entity;
    }

    private LeaveAddEntity getSaveJsonData() {
            LeaveAddEntity entity = new LeaveAddEntity();
            entity.setVa_vacationtype(et_leave_category.getText().toString());
            entity.setVa_remark(et_leave_reason.getText().toString());
            entity.setVa_startime(tv_start_time.getText().toString());
            entity.setVa_endtime(tv_end_time.getText().toString());
            entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
            entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));
        return entity;
    }

    private void selectLeaveType() {
        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < mLeaveTypes.length; i++) {
            selectBean = new SelectBean();
            selectBean.setName(mLeaveTypes[i]);
            formBeaan.add(selectBean);
        }

        Intent intent = new Intent();
        intent.setClass(this, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("title", getString(R.string.leave_type));
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, 0x11);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0x11:
                if (resultCode == 0x20) {
                    SelectBean b = data.getParcelableExtra("data");
                    if (b != null) {
                        et_leave_category.setText(b.getName());
                    }
                }
                break;
            default:
                break;
        }

        if (requestCode == 0x30 && resultCode == 0x11){
            String startDate=data.getStringExtra("startDate");
            String endDate=data.getStringExtra("endDate");
            startDate=startDate+":00";
            endDate=endDate+":00";
            tv_start_time.setText(startDate);
            tv_end_time.setText(endDate);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDateDialog(Context ct, final EditText tv) {
        DateTimePicker picker = new DateTimePicker((Activity) ct, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        //赋值当前系统时间
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));

        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                tv.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
            }
        });
        picker.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_platdoc_set, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.oa_leave){
            Intent intent = new Intent();
            intent.setClass(this, LeavePageActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == android.R.id.home){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if ( !TextUtils.isEmpty(resubmit) && (resubmit.equals("resubmit"))){

        }else {
            getMenuInflater().inflate(R.menu.menu_platdoc_set, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        super.onBackPressed();
    }

}
