package com.uas.appworks.OA.platform.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.EasyFragment;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appworks.OA.platform.activity.JoinCharitActivity;
import com.uas.appworks.R;

import java.text.DecimalFormat;

/**
 * Created by Bitlike on 2017/11/8.
 */

public class UserFragment extends EasyFragment implements View.OnClickListener {
    private HttpClient httpClient = new HttpClient.Builder(Constants.charitBaseUrl()).isDebug(true).build();

    private TextView nameTv;
    private TextView phoneTv;
    private TextView companyTv;
    private TextView projectNumTv;
    private TextView charitTv;
    private TextView activityNumTv;


    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_user;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
        initData();
    }

    private void initView() {
        nameTv = (TextView) findViewById(R.id.nameTv);
        phoneTv = (TextView) findViewById(R.id.phoneTv);
        companyTv = (TextView) findViewById(R.id.companyTv);
        charitTv = (TextView) findViewById(R.id.charitTv);//捐款总额
        projectNumTv = (TextView) findViewById(R.id.projectNumTv);
        activityNumTv = (TextView) findViewById(R.id.activityNumTv);

        findViewById(R.id.projectRl).setOnClickListener(this);
        findViewById(R.id.activityRl).setOnClickListener(this);
        findViewById(R.id.messageRl).setOnClickListener(this);

        nameTv.setText(CommonUtil.getName());
        phoneTv.setText(CommonUtil.getSharedPreferences(ct, "user_phone"));
        String company = CommonUtil.getSharedPreferences(ct, "erp_commpany");
        if (StringUtil.isEmpty(company)) {
            companyTv.setText("");
        } else {
            companyTv.setText(company);
        }
    }

    private void initData() {
        if (!NetUtils.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
            String userInfo = PreferenceUtils.getString("userInfo");
            if (!StringUtil.isEmpty(userInfo)) {
                try {
                    handleData(userInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String imid = MyApplication.getInstance().mLoginUser.getUserId();
        LogUtil.i("imid=" + imid);
        httpClient.Api().send(new HttpClient.Builder()
                .url("center")
                .add("imid", imid)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    handleData(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("userFragement initData Exception" + e.getMessage());
                    }
                }
            }
        }));
    }

    private void handleData(String message) {
        JSONObject object = JSON.parseObject(message);
        String sumMoney = JSONUtil.getText(object, "sumMoney");
        String sumActJoin = JSONUtil.getText(object, "sumActJoin");
        String sumProJoin = JSONUtil.getText(object, "sumProJoin");
        JSONObject user = JSONUtil.getJSONObject(object, "user");
        if (user != null && !user.isEmpty()) {
            String company = JSONUtil.getText(user, "company");
            String name = JSONUtil.getText(user, "name");
            String tel = JSONUtil.getText(user, "tel");

            if (!StringUtil.isEmpty(name) && !name.endsWith("军")) {
                nameTv.setText(name);
            }
            if (!StringUtil.isEmpty(company)) {
                companyTv.setText(company);
            }
            if (!StringUtil.isEmpty(tel)) {
                phoneTv.setText(tel);
            }
        }
        if (sumMoney != null) {
            DecimalFormat df = new DecimalFormat(".##");
            try {
                charitTv.setText(df.format(Float.valueOf(sumMoney)));
            } catch (Exception e) {
                charitTv.setText(sumMoney);
            }
        } else {
            charitTv.setText("0.00");
        }
        projectNumTv.setText("(" + sumProJoin + ")");
        activityNumTv.setText("(" + sumActJoin + ")");
        PreferenceUtils.putString("userInfo", message);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.projectRl) {
            ct.startActivity(new Intent(ct, JoinCharitActivity.class)
                    .putExtra("type", 1)
                    .putExtra("sumNum", projectNumTv.getText().toString())
                    .putExtra("sumMoney", charitTv.getText().toString())
                    .putExtra("title", "已参与项目"));
        } else if (view.getId() == R.id.activityRl) {
            ct.startActivity(new Intent(ct, JoinCharitActivity.class)
                    .putExtra("type", 2)
                    .putExtra("title", "已参与活动")
                    .putExtra("sumNum", activityNumTv.getText().toString()));
        } else if (view.getId() == R.id.messageRl) {

        }
    }
}
