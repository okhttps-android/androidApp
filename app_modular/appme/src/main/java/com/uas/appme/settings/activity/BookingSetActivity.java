package com.uas.appme.settings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.TimePicker;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.lidroid.xutils.ViewUtils;
import com.uas.appme.R;
import com.uas.appworks.OA.erp.model.MapData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @desc:设置时间段
 * @author：Arison on 2017/6/26
 */
public class BookingSetActivity extends SupportToolBarActivity implements  View.OnClickListener {

    private Button submit_btn;
    private TextView tv_endTime;
    private TextView tv_startTime;
    private final int what_getTime = 0;
    private final int what_setTime = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case what_getTime:
                 //{"endtime":"23:59","starttime":"00:00"}
                    String result;
                    try {
                        result = msg.getData().getString("result");
                        String starttime=JSON.parseObject(result).getString("starttime");
                        String endtime=JSON.parseObject(result).getString("endtime");
                        if(!StringUtil.isEmpty(starttime)){
                            tv_startTime.setText(starttime);
                        }else{
                            tv_startTime.setText("00:00");
                        }
                         
                        if (!StringUtil.isEmpty(endtime)){
                            tv_endTime.setText(endtime);
                        }else{
                            tv_endTime.setText("23:59");
                        }
                      
                    } finally {
                        
                    }
                    break;
                case what_setTime:
                    try {
                       result=msg.getData().getString("result");
                        if ("true".equals(JSON.parseObject(result).getString("result"))){
                            ToastMessage(getString(R.string.save_ok));
                        }
                    } catch (Exception e) {
                        
                    }
                    break;
                case Constants.APP_NOTNETWORK:
                    ToastMessage(msg.getData().getString("result"));
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    ToastMessage(getString(R.string.make_adeal_failed));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_set);
        ViewUtils.inject(this);
        setTitle(getString(R.string.booking_time_set));
        submit_btn = (Button) findViewById(R.id.submit_btn);
        tv_endTime = (TextView) findViewById(R.id.tv_endTime);
        tv_startTime = (TextView) findViewById(R.id.tv_startTime);

        tv_startTime.setOnClickListener(this);
        tv_endTime.setOnClickListener(this);
        submit_btn.setOnClickListener(this);
        initData();
    }


    protected void initData() {
        getBookingTime();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.submit_btn){
            setBookingTime();
        }else if (v.getId() == R.id.tv_startTime){
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("endDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("hasMenu", false)
                            .putExtra("type", 2)
                    , 0x24);
        }else if (v.getId() == R.id.tv_endTime){
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("endDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("hasMenu", false)
                            .putExtra("type", 2)
                    , 0x24);
        }
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
                tv_startTime.setText(startTime);
                tv_endTime.setText(endTime);
                break;
            
        }
    }

    private void showDateDialog(Context ct, final TextView tv, int hh, int minth) {
        TimePicker picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
        picker.setSelectedItem(hh, minth);
        picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
            @Override
            public void onTimePicked(String hour, String minute) {
                String time = hour + ":" + minute;
                tv.setText(time);
            }
        });
        picker.show();
    }


    public void getBookingTime() {
        String url = Constants.IM_BASE_URL() + "user/appUsertime";
        Map<String, Object> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("userid", MyApplication.getInstance().mLoginUser.getUserId());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what_getTime, null, null, "post");
    }

    public void setBookingTime() {
        if (StringUtil.isEmpty(tv_startTime.getText().toString())||
                StringUtil.isEmpty(tv_endTime.getText().toString())){
            ToastMessage("时间段不能为空！");
            return;
        }
        if (tv_startTime.getText().toString().compareTo(tv_endTime.getText().toString())>=0){
            ToastMessage(getString(R.string.not_time_start_biger_end));
            return;
        }
        String url = Constants.IM_BASE_URL() + "user/appTimeSet";
        Map<String, Object> params = new HashMap<>();
        MapData data = new MapData();
        data.setAt_userid(MyApplication.getInstance().mLoginUser.getUserId());
        data.setAt_username(MyApplication.getInstance().mLoginUser.getNickName());
        data.setAt_startdate(tv_startTime.getText().toString());
        data.setAt_enddate(tv_endTime.getText().toString());
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("map", JSON.toJSONString(data));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what_setTime, null, null, "post");
    }


}
