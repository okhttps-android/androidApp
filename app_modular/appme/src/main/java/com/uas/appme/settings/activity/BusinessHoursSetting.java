package com.uas.appme.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by FANGlh on 2017/10/23.
 * function:
 */

public class BusinessHoursSetting extends BaseActivity implements View.OnClickListener {
    private TextView mTvStartTime;
    private TextView mTvEndTime;
    private Button mSubmitBtn;
    private LinearLayout set_hour_ll;
    private LinearLayout set_booktype_ll;
    private TextView period_tv;
    private TextView timpoint_tv;
    private int sc_booktype = -1;
    private int sc_cytype = -1;
    private int setType;
    private FormEditText bIntroductionEt;
    private LinearLayout roomType_ll;
    private TextView rtype1_tv;
    private TextView rtype2_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_hours_setting_activity);
        initView();
        initData();
    }



    private void initView() {
        setType = getIntent().getIntExtra("setType",-1);

        //设置营业时间
        mTvStartTime = (TextView) findViewById(R.id.tv_startTime);
        mTvEndTime = (TextView) findViewById(R.id.tv_endTime);
        mSubmitBtn = (Button) findViewById(R.id.submit_btn);

        mTvStartTime.setOnClickListener(this);
        mTvEndTime.setOnClickListener(this);
        mSubmitBtn.setOnClickListener(this);

        //设置预约时间类型
        set_hour_ll = (LinearLayout)findViewById(R.id.set_hour_ll);
        set_booktype_ll = (LinearLayout)findViewById(R.id.set_booktype_ll);

        period_tv = (TextView) findViewById(R.id.period_tv);
        period_tv.setOnClickListener(this);

        timpoint_tv = (TextView) findViewById(R.id.timpoint_tv);
        timpoint_tv.setOnClickListener(this);

        //设置包房预订类型
        roomType_ll = (LinearLayout) findViewById(R.id.set_roomtype_ll);
        rtype1_tv = (TextView) findViewById(R.id.rtype1_tv);
        rtype2_tv = (TextView) findViewById(R.id.rtype2_tv);
        rtype1_tv.setOnClickListener(this);
        rtype2_tv.setOnClickListener(this);

        bIntroductionEt = (FormEditText) findViewById(R.id.business_Introduction_et);
        if (setType == 1){
            setTitle(getString(R.string.setting_worktime));
            set_hour_ll.setVisibility(View.VISIBLE);
            set_booktype_ll.setVisibility(View.GONE);
            bIntroductionEt.setVisibility(View.GONE);
            roomType_ll.setVisibility(View.GONE);
        }else if (setType == 2){
           setTitle(getString(R.string.setting_time_type));
            set_hour_ll.setVisibility(View.GONE);
            set_booktype_ll.setVisibility(View.VISIBLE);
            bIntroductionEt.setVisibility(View.GONE);
            roomType_ll.setVisibility(View.GONE);
        }else if (setType == 3){
            setTitle(getString(R.string.business_detail1));
            set_hour_ll.setVisibility(View.GONE);
            set_booktype_ll.setVisibility(View.GONE);
            bIntroductionEt.setVisibility(View.VISIBLE);
            roomType_ll.setVisibility(View.GONE);
        }else if (setType == 4){
            setTitle(getString(R.string.room_type));
            set_hour_ll.setVisibility(View.GONE);
            set_booktype_ll.setVisibility(View.GONE);
            bIntroductionEt.setVisibility(View.GONE);
            roomType_ll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.submit_btn){
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            if (setType == 1)
                doSaveBHours();
            else if (setType == 2)
                doSaveTimeType();
            else if (setType == 3)
                doSaveBIntroduction();
            else if (setType == 4)
                doSaveRoomtype();
        }else if (v.getId() == R.id.tv_startTime){
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("endDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("hasMenu", false)
                            .putExtra("type", 5)
                    , 0x24);
        }else if (v.getId() == R.id.tv_endTime){
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("endDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("hasMenu", false)
                            .putExtra("type", 5)
                    , 0x24);
        }else if (v.getId() == R.id.period_tv){
            sc_booktype = 1;
            period_tv.setBackgroundResource(R.color.aqua);
            timpoint_tv.setBackgroundResource(R.color.white);
        }else if (v.getId() == R.id.timpoint_tv){
            sc_booktype = 0;
            period_tv.setBackgroundResource(R.color.white);
            timpoint_tv.setBackgroundResource(R.color.aqua);
        }else if (v.getId() == R.id.rtype1_tv){
            sc_cytype = 0;
            rtype1_tv.setBackgroundResource(R.color.aqua);
            rtype2_tv.setBackgroundResource(R.color.white);
        }else if (v.getId() == R.id.rtype2_tv){
            sc_cytype = 1;
            rtype1_tv.setBackgroundResource(R.color.white);
            rtype2_tv.setBackgroundResource(R.color.aqua);
        }
    }

    private void doSaveRoomtype() {
        if ( sc_cytype == -1){
            ToastMessage(getString(R.string.please_select_room_type));
            return;
        }
        Map<String,Object> param = new HashMap<>();
        param.put("sc_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        param.put(" sc_cytype", sc_cytype);

        LogUtil.prinlnLongMsg("appStoreUpdate",JSONUtil.map2JSON(param));
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreUpdate")
                .add("map", JSONUtil.map2JSON(param))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    LogUtil.prinlnLongMsg("appStoreUpdate", o.toString()+"");
                    //{"result":"true"}
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        Toast.makeText(ct,getString(R.string.save_success),Toast.LENGTH_LONG).show();
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private void doSaveBIntroduction() {
        if (StringUtil.isEmpty(bIntroductionEt.getText().toString())){
            ToastMessage(getString(R.string.please_input_bdi));
            return;
        }
        if (bIntroductionEt.getText().toString().contains("'")){
            ToastMessage(getString(R.string.unlaw_str));
            return;
        }

        Map<String,Object> param = new HashMap<>();
        param.put("sc_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        param.put("sc_introduce",bIntroductionEt.getText().toString());

        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreUpdate")
                .add("map", JSONUtil.map2JSON(param))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    LogUtil.prinlnLongMsg("appStoreUpdate", o.toString()+"");
                    //{"result":"true"}
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        Toast.makeText(ct,getString(R.string.save_success),Toast.LENGTH_LONG).show();
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    private void doSaveTimeType() {
        if (sc_booktype == -1){
            ToastMessage(getString(R.string.select_bookT_type));
            return;
        }
        Map<String,Object> param = new HashMap<>();
        param.put("sc_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        param.put("sc_booktype",sc_booktype);

        LogUtil.prinlnLongMsg("appStoreUpdate",JSONUtil.map2JSON(param));
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreUpdate")
                .add("map", JSONUtil.map2JSON(param))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    LogUtil.prinlnLongMsg("appStoreUpdate", o.toString()+"");
                    //{"result":"true"}
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        Toast.makeText(ct,getString(R.string.save_success),Toast.LENGTH_LONG).show();
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        switch (requestCode) {
            case 0x24://时间
                String   startTime = data.getStringExtra("startDate");
                String   endTime = data.getStringExtra("endDate");
//                String displayDate = startTime.substring(11, 16) + "-" + endTime.substring(11, 16);
//                tvBookTimes.setText(displayDate);
                mTvStartTime.setText(startTime);
                mTvEndTime.setText(endTime);
                break;

        }
    }

    //获取是否之前有设置的时间
    private void initData() {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appCompanyAdmin")
                .add("companyid",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("token", MyApplication.getInstance().mAccessToken)
                .add("userid",MyApplication.getInstance().mLoginUser.getUserId())
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    LogUtil.prinlnLongMsg("appCompanyAdmin", o.toString()+"");
// {"endtime":"17:00","result":"1","starttime":"10:00","url":"http://113.105.74.140:8081/u/123/100123/201709/o/ab6d93f74f9b4ec7a06f7dbfd725ec38.png"}


                    //预约时间段
                    if (o.toString().contains("starttime"))
                        mTvStartTime.setText(JSON.parseObject(o.toString()).getString("starttime")+"");
                    if (o.toString().contains("endtime"))
                        mTvEndTime.setText(JSON.parseObject(o.toString()).getString("endtime"));
                    String timekind = JSON.parseObject(o.toString()).getString("timekind");
                   //预约时间类型
                    if (!StringUtil.isEmpty(timekind)){
                        if ("1".equals(timekind)){
                            sc_booktype = 1;
                            period_tv.setBackgroundResource(R.color.aqua);
                            timpoint_tv.setBackgroundResource(R.color.white);
                        }else {
                            sc_booktype = 0;
                            period_tv.setBackgroundResource(R.color.white);
                            timpoint_tv.setBackgroundResource(R.color.aqua);
                        }
                    }

                    //商家详情介绍
                    if (o.toString().contains("introduce") && !StringUtil.isEmpty(JSON.parseObject(o.toString()).getString("introduce"))){
            bIntroductionEt.setText(JSON.parseObject(o.toString()).getString("introduce"));
                    }

                    //包房预订类型
                    String cytype = JSON.parseObject(o.toString()).getString("cytype");
                    if (!StringUtil.isEmpty(cytype)){
                        if ("1".equals(cytype)){
                            sc_cytype = 1;
                            rtype1_tv.setBackgroundResource(R.color.white);
                            rtype2_tv.setBackgroundResource(R.color.aqua);
                        }else {
                            sc_cytype = 0;
                            rtype1_tv.setBackgroundResource(R.color.aqua);
                            rtype2_tv.setBackgroundResource(R.color.white);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }));
    }
    private void doSaveBHours() {
        if (StringUtil.isEmpty(mTvStartTime.getText().toString())||
                StringUtil.isEmpty(mTvEndTime.getText().toString())){
            ToastMessage(getString(R.string.time_canot_empty));
            return;
        }
        if (mTvStartTime.getText().toString().compareTo(mTvEndTime.getText().toString())>=0){
            ToastMessage(getString(R.string.not_time_start_biger_end));
            return;
        }
        Map<String,Object> param = new HashMap<>();
        param.put("sc_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        param.put("sc_starttime",mTvStartTime.getText().toString());
        param.put("sc_endtime",mTvEndTime.getText().toString());

        LogUtil.prinlnLongMsg("appStoreUpdate",JSONUtil.map2JSON(param));
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreUpdate")
                .add("map", JSONUtil.map2JSON(param))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    LogUtil.prinlnLongMsg("appStoreUpdate", o.toString()+"");
                    //{"result":"true"}
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        Toast.makeText(ct,getString(R.string.save_success),Toast.LENGTH_LONG).show();
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
