package com.uas.appme.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;

/**
 * Created by FANGlh on 2017/10/10.
 * function:
 */

public class BSettingActivity extends SupportToolBarActivity implements View.OnClickListener {

    private String sc_industry = null;
    private String sc_industrycode = null;
    private RelativeLayout rTyperl;
    private RelativeLayout tableSetRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bsetting_activity);
        initView();
        judgeWhichType(); //判断商家属于什么行业
    }

    private void judgeWhichType() {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appCompanyType")
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appCompanyType", o.toString()+"");

                //{"result":[{"sc_industry":"医疗","sc_industrycode":"10001"}]}
                JSONArray array = JSON.parseObject(o.toString()).getJSONArray("result");
                if (ListUtils.isEmpty(array)) return;
                JSONObject object = array.getJSONObject(0);
                if (object == null) return;
                sc_industry = array.getJSONObject(0).getString("sc_industry");
                sc_industrycode = array.getJSONObject(0).getString("sc_industrycode");

                if ("餐饮".equals(sc_industry)) {
                    tableSetRl.setVisibility(View.VISIBLE);
                }else {
                    tableSetRl.setVisibility(View.GONE);
                }
            }
        }));

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image_setting_rl){
            startActivity(new Intent(this,ImageSettingActivity.class));
        }else if (v.getId() == R.id.keshi_setting_rl){
            
        }else if (v.getId() == R.id.people_setting_rl){
            startActivity(new Intent(this,PersonSettingActivity.class)
            .putExtra("sc_industry",sc_industry)
            .putExtra("sc_industrycode",sc_industrycode));
        }else if (v.getId() == R.id.employeerest_rl){
            startActivity(new Intent(ct,BComSetEmployeeRestActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode));
        }else if (v.getId() == R.id.companyrest_rl){
            startActivity(new Intent(ct,BComSetCompanyRestActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode));
        }else if (v.getId() == R.id.batchplace_rl){
            startActivity(new Intent(ct,BSettingLocationActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode));
        }else if (v.getId() == R.id.business_hours_rl){
            startActivity(new Intent(ct,BusinessHoursSetting.class)
            .putExtra("setType",1));
        }else if (v.getId() == R.id.booking_timetype_rl){
            startActivity(new Intent(ct,BusinessHoursSetting.class)
                    .putExtra("setType",2));
        }else if (v.getId() == R.id.business_introduction_rl){
            startActivity(new Intent(ct,BusinessHoursSetting.class)
                    .putExtra("setType",3));
        }else if (v.getId() == R.id.room_type_rl){
            startActivity(new Intent(ct,BusinessHoursSetting.class)
                    .putExtra("setType",4));
        }else if (v.getId() == R.id.table_setting_rl){
            startActivity(new Intent(ct,TableSetActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode));
        }
    }

    private void initView() {
        findViewById(R.id.image_setting_rl).setOnClickListener(this);
        findViewById(R.id.keshi_setting_rl).setOnClickListener(this);
        findViewById(R.id.people_setting_rl).setOnClickListener(this);
        findViewById(R.id.employeerest_rl).setOnClickListener(this);
        findViewById(R.id.companyrest_rl).setOnClickListener(this);
        findViewById(R.id.batchplace_rl).setOnClickListener(this);
        findViewById(R.id.business_hours_rl).setOnClickListener(this);
        findViewById(R.id.booking_timetype_rl).setOnClickListener(this);
        findViewById(R.id.business_introduction_rl).setOnClickListener(this);
        rTyperl = (RelativeLayout) findViewById(R.id.room_type_rl);
        rTyperl.setOnClickListener(this);

        tableSetRl = (RelativeLayout) findViewById(R.id.table_setting_rl);
        tableSetRl.setOnClickListener(this);


    }
}
