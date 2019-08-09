package com.uas.appme.settings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.DatePicker;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by FANGlh on 2017/11/10.
 * function:
 */

public class CheckWagesActivity extends BaseActivity implements View.OnClickListener {
    private TextView mWagesDate;
    private FormEditText mWagesPhoneEt;
    private FormEditText mWagesPasswordEt;
    private FormEditText mWagesCodeEt;
    private Button mWagesCodeBtn;
    private Button mCheckBtn;
    private String myearmonth = DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMM");//默认当前月
    private String checkYear;
    private String checkMonth;
    private TextView mTel;
    private String master;
    private EditText inputPSEt;
    private ImageView eye_im;
    private Boolean canSeePW = false;
    private String emcode;
    private Button seconds_tv;
    private long SecondT = 59;
    private boolean isRun = false;
    private Boolean codeTimeOut = false;
    private String vecodeStr = "";
    private Handler timeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1212){
                SecondT--;
                seconds_tv.setText(getString(R.string.get_again)+"("+SecondT+"s)");
                if (SecondT==0) {
                    codeTimeOut = true;
                    seconds_tv.setVisibility(View.GONE);
                    mWagesCodeBtn.setVisibility(View.VISIBLE);
                    isRun = false;
                    SecondT = 59;
                }
            Log.i("HttpLogs",SecondT+"");
        }
        }
    };
    /**
     * 开启倒计时
     */
    private void startRun() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isRun) {
                    try {
                        Thread.sleep(1000); // sleep 1000ms
                        Message message = Message.obtain();
                        message.what = 1212;
                        timeHandler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        checkIsHavePassword();//查询是设置过查询密码
    }

    private void checkIsHavePassword() {
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/checkPassword.action")
                .add("phone",mTel.getText().toString())
                .add("emcode",emcode)
                .add("em_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("master",master)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("checkPassword", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                if (o.toString().contains("success") && !JSON.parseObject(o.toString()).getBoolean("success"))
                    showSetPWW();

            }
        }));
    }

    //设置查询密码PP
    private PopupWindow popupWindow = null;
    private void showSetPWW() {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.set_wage_password, null);
        contentView.findViewById(R.id.cancel_tv).setOnClickListener(this);
        contentView.findViewById(R.id.sure_btn).setOnClickListener(this);
        eye_im = (ImageView) contentView.findViewById(R.id.eye_im);
        eye_im.setOnClickListener(this);
        inputPSEt = (EditText)contentView.findViewById(R.id.input_ps_et);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 200);
        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(getResources().getDrawable(com.uas.appworks.R.drawable.pop_round_bg));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }

    private void initView() {
        setContentView(R.layout.check_wages_activity);
        setTitle(getString(R.string.str_company_salary));

        mWagesDate = (TextView) findViewById(R.id.wages_date);
        mWagesPasswordEt = (FormEditText) findViewById(R.id.wages_password_et);
        mWagesCodeEt = (FormEditText) findViewById(R.id.wages_code_et);
        mWagesCodeBtn = (Button) findViewById(R.id.wages_code_btn);
        mCheckBtn = (Button) findViewById(R.id.check_btn);
        mTel = (TextView) findViewById(R.id.wages_phone_tv);
        findViewById(R.id.forget_password_btn).setOnClickListener(this);
        mWagesDate.setOnClickListener(this);
        mWagesCodeBtn.setOnClickListener(this);
        mCheckBtn.setOnClickListener(this);
        seconds_tv = (Button)findViewById(R.id.seconds_tv);

        mTel.setText(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"user_phone"));
        master = CommonUtil.getSharedPreferences(ct, "erp_master");
//        master = "uas_dev";
        emcode = CommonUtil.getEmcode();
//        emcode = "U0747";



        //默认显示查询时间
        Date date = new Date(System.currentTimeMillis());
        int year =  CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy"));
        int month = CalendarUtil.getMonth(date);
        String Smonth = "";
        if (month < 10 && month > 1) {
            Smonth = "0" + Integer.valueOf(month-1);
        }else if (month == 1){
            year = year-1;
            Smonth = 12+"";
        }else {
            Smonth = month-1 + "";
        }
        mWagesDate.setText(year + "年" + Smonth+"月");
        checkYear = year+""; checkMonth = Smonth;
    }

    @Override
    public void onClick(View v) {
       int id =  v.getId();
        if (id == R.id.wages_date){
            DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
            picker.setRange(CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy"))-2, CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy")));
            picker.setSelectedItem(
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1);
            picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
                @Override
                public void onDatePicked(String year, String month) {
                    myearmonth =  year + "年" + month+"月";
                    checkYear = year;
                    checkMonth = month;
                    mWagesDate.setText(myearmonth);
                }
            });
            picker.show();
        }else if (id == R.id.wages_code_btn){//获取验证码
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
           }
            codeTimeOut = false;
            mWagesCodeBtn.setVisibility(View.GONE);
            seconds_tv.setVisibility(View.VISIBLE);
            isRun = true;
            startRun();

 HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//            HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
            httpClient.Api().send(new HttpClient.Builder()
                    .url("mobile/salary/verificationCode.action")
                    .header("Cookie","JSESSIONID="+CommonUtil.getSharedPreferences(BaseConfig.getContext(), "sessionId"))
                    .add("phone",mTel.getText().toString())
                    .method(Method.POST)
                    .build(),new ResultSubscriber<>(new ResultListener<Object>() {
                @Override
                public void onResponse(Object o) {
                    LogUtil.prinlnLongMsg("verificationCode", o.toString()+"");
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")) {
                        Toast.makeText(ct,getString(R.string.msg_send_success),Toast.LENGTH_LONG).show();
//                        mWagesCodeEt.setText(JSON.parseObject(o.toString()).getString("vecode")+"");
                        vecodeStr = JSON.parseObject(o.toString()).getString("vecode");
                    }

                }
            }));
        }else if (id == R.id.check_btn){
            if (StringUtil.isEmpty(checkYear) || StringUtil.isEmpty(checkMonth)){
                ToastMessage(getString(R.string.input_search_date));
                return;
            }else if (StringUtil.isEmpty(mWagesCodeEt.getText().toString())){
                ToastMessage(getString(R.string.input_search_ycode));
                return;
            }else if (StringUtil.isEmpty(mWagesPasswordEt.getText().toString())){
                ToastMessage(getString(R.string.input_search_password));
                return;
            }
            LogUtil.i("vecodeStr="+vecodeStr +'\n' + "inputStr="+mWagesCodeEt.getText().toString());
            if( !mWagesCodeEt.getText().toString().equals(vecodeStr)){
                ToastMessage(getString(R.string.auth_code_error));
                return;
            }
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            searchWage();
        }else if (id == R.id.cancel_tv){
            popupWindow.dismiss();
        }else if (id == R.id.sure_btn){
            if (StringUtil.isEmpty(inputPSEt.getText().toString())){
                ToastMessage(getString(R.string.input_search_password));
                return;
            }
            if (inputPSEt.getText().toString().length()<6){
                ToastMessage(getString(R.string.please_settting_password));
                return;
            }
            doSavePW();
        }else if (id == R.id.forget_password_btn){//忘记密码
            startActivity(new Intent(ct,FindWagePWActivity.class));
        }else if (id == R.id.eye_im){
            if (canSeePW){
                canSeePW = false;
                inputPSEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eye_im.setImageResource(R.drawable.icon_unshow);
            }else {
                canSeePW = true;
                inputPSEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eye_im.setImageResource(R.drawable.icon_show);
            }
        }
    }

    private void doSavePW() {
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/changePassword.action")
                .add("emcode", emcode)
                .add("password",inputPSEt.getText().toString())
                .add("phone",mTel.getText().toString())
                .add("em_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("master",master)
                .method(Method.POST)
                .header("Cookie","JSESSIONID="+CommonUtil.getSharedPreferences(BaseConfig.getContext(), "sessionId"))
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("changePassword", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;

                if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")){
                    Toast.makeText(ct, getString(R.string.setting_password_success), Toast.LENGTH_LONG).show();
                    popupWindow.dismiss();
                }

            }
        }));
    }

    private void searchWage() {
        if (codeTimeOut){
            ToastMessage(getString(R.string.code_outtime));
            return;
        }
                HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/getEmSalary.action")
                .add("emcode", emcode)
                .add("password",mWagesPasswordEt.getText().toString())
                .add("phone",mTel.getText().toString())
                .add("vecode",mWagesCodeEt.getText().toString())
                .add("date",checkYear+"-"+checkMonth)
                .add("master",master)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("getEmSalary", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                try {
                    if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")){
                        String ps = mWagesPasswordEt.getText().toString();
                        String vc = mWagesCodeEt.getText().toString();
                        String p = mTel.getText().toString();
                        startActivity(new Intent(ct,WagesDetailsActivity.class)
                                .putExtra("checkYear",checkYear)
                                .putExtra("checkMonth",checkMonth)
                                .putExtra("WageDatas",o.toString())
                                .putExtra("password",ps)
                                .putExtra("vecode",vc)
                                .putExtra("phone",p));
                        finish();
                    }else if (o.toString().contains("reason")){
                        ToastMessage(JSON.parseObject(o.toString()).getString("reason"));
                    }else if (o.toString().contains("exceptionInfo")){
                        ToastMessage(JSON.parseObject(o.toString()).getString("exceptionInfo"));
                    }
                } catch (Exception e) {
                    LogUtil.prinlnLongMsg("HttpLogs",e.toString());
                    e.printStackTrace();
                }

            }
        }));
    }
    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(CheckWagesActivity.this, 1f);
            }
        });
    }

    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }
}
