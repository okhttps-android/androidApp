package com.uas.appcontact.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andreabaccega.widget.FormEditText;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.view.Activity.SelectActivity;
import com.uas.appcontact.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by FANGlh on 2017/4/12.
 * function: 独立版新增人员
 */
public class PlatContactAddPeopleActivity  extends BaseActivity implements View.OnClickListener{
    private static final int SAVE_REQUEST = 0x41301;
    private FormEditText user_name_tv;
    private FormEditText user_sex_tv;
    private FormEditText user_department_tv;
    private FormEditText user_job_tv;
    private FormEditText user_tel_tv;
    private FormEditText user_email_tv;
    private Button btn_save;
    private int mSex = 1;
    private String[] mDepartment;
    private String[] mJobs;
//    private String [][] mJobs;
//            {
//            {"总经理","副总经理","秘书"},
//            {"经理","采购员"},
//            {"经理","业务员"},
//            {"经理","会计","出纳"},
//            {"经理","人事专员","行政专员"},
//            {"经理","工程师"}
//    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plat_add_people);
        initView();
    }

    private void initView() {
        user_name_tv= (FormEditText) findViewById(R.id.user_name_tv);
        user_sex_tv= (FormEditText) findViewById(R.id.user_sex_tv);
        user_department_tv= (FormEditText) findViewById(R.id.user_department_tv);
        user_job_tv= (FormEditText) findViewById(R.id.user_job_tv);
        user_tel_tv= (FormEditText) findViewById(R.id.user_tel_tv);
        user_email_tv= (FormEditText) findViewById(R.id.user_email_tv);
        btn_save= (Button) findViewById(R.id.btn_save);




        user_sex_tv.setKeyListener(null);
        user_sex_tv.setFocusable(false);
        user_sex_tv.setOnClickListener(this);

        user_department_tv.setKeyListener(null);
        user_department_tv.setFocusable(false);
        user_department_tv.setOnClickListener(this);

        user_job_tv.setKeyListener(null);
        user_job_tv.setFocusable(false);
        user_job_tv.setOnClickListener(this);
        btn_save.setOnClickListener(this);

        mDepartment = getResources().getStringArray(R.array.plat_department_list);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if (R.id.user_sex_tv==id){
            showSelectSexDialog();
        }else if (R.id.btn_save==id){
            saveJudge();
        }else if (R.id.user_department_tv==id){
            doSelectDepartment();
        }else if (R.id.user_job_tv==id){
            if (StringUtil.isEmpty(user_department_tv.getText().toString())){
                ToastMessage(getString(R.string.select_department_first));
            }else {
                doSelectJob();
            }
        }
    }

    private void doSelectJob() {
        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
//        int item = -1;
        String choice_department = user_department_tv.getText().toString();
        if (getString(R.string.plat_department1).equals(choice_department))
            mJobs = getResources().getStringArray(R.array.plat_job1);
        if (getString(R.string.plat_department2).equals(choice_department))
            mJobs = getResources().getStringArray(R.array.plat_job2);
        if (getString(R.string.plat_department3).equals(choice_department))
            mJobs = getResources().getStringArray(R.array.plat_job3);
        if (getString(R.string.plat_department4).equals(choice_department))
            mJobs = getResources().getStringArray(R.array.plat_job4);
        if (getString(R.string.plat_department5).equals(choice_department))
            mJobs = getResources().getStringArray(R.array.plat_job5);
        if (getString(R.string.plat_department6).equals(choice_department))
            mJobs = getResources().getStringArray(R.array.plat_job6);
        /*switch (user_department_tv.getText().toString()){
            case "总经办":
                item = 0;
                break;
            case "采购部":
                item = 1;
                break;
            case "销售部":
                item = 2;
                break;
            case "财务部":
                item = 3;
                break;
            case "人事行政部":
                item = 4;
                break;
            case "研发部":
                item = 5;
                break;
            default:
        }*/
        for (int i = 0; i < mJobs.length; i++) {
            selectBean = new SelectBean();
            selectBean.setName(mJobs[i]);
            formBeaan.add(selectBean);
        }

        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("title", getString(R.string.select_jobs));
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, 0x12);
    }

    private void doSelectDepartment() {
        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < mDepartment.length; i++) {
            selectBean = new SelectBean();
            selectBean.setName(mDepartment[i]);
            formBeaan.add(selectBean);
        }
        Intent intent = new Intent(ct,SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("title", getString(R.string.select_department));
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, 0x11);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (resultCode == 0x20){
            SelectBean b = data.getParcelableExtra("data");
            if (b != null){
                switch (requestCode){
                    case 0x11:
                        user_department_tv.setText(b.getName());
                        user_job_tv.setText("");
                        break;
                    case 0x12:
                        user_job_tv.setText(b.getName());
                        break;
                    default:
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void saveJudge() {
        if (user_name_tv.testValidity() && user_sex_tv.testValidity()
                && user_department_tv.testValidity() && user_job_tv.testValidity() &&
                user_tel_tv.testValidity()&& user_email_tv.testValidity()
                ){

            if (user_email_tv.getText().toString().contains("@")){
                saveRequest();
            }else {
                ToastMessage(getString(R.string.Please_imput_the_correct_email_format));
            }

        }
    }

    private void saveRequest() {
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.networks_out));
            return;
        }
        progressDialog.show();
        btn_save.setEnabled(false);
        Map<String,Object> formStoreMap = new HashMap<>();
        formStoreMap.put("userName",user_name_tv.getText().toString());
        formStoreMap.put("userSex",getString(R.string.user_body).equals(user_sex_tv.getText().toString()) ? "M" : "F");
        formStoreMap.put("userTel",user_tel_tv.getText().toString());
        formStoreMap.put("userEmail",user_email_tv.getText().toString());

        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().add_people;
        HashMap<String,Object> param = new HashMap<>();
        String formStore = JSON.toJSONString(formStoreMap);
        param.put("formStore",formStore);
        param.put("dept",user_department_tv.getText().toString());
        param.put("role",user_job_tv.getText().toString());
        param.put("enuu",  CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode",CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(this, url, param, handler, headers, SAVE_REQUEST, null, null, "post");
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SAVE_REQUEST:
                    if (msg.getData() != null){
                        String save_result = msg.getData().getString("result");
                        Log.i("save_result",save_result);
                        if (!StringUtil.isEmpty(save_result)){
                            if (JSON.parseObject(save_result).containsKey("success") &&
                                    "人员添加成功".equals(JSON.parseObject(save_result).getString("success"))){
                                Toast.makeText(getApplicationContext(), getString(R.string.add_success), Toast.LENGTH_LONG);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                },1000);
                            }
                        }
                    }
                    progressDialog.dismiss();
                    btn_save.setEnabled(true);
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                            progressDialog.dismiss();
                            btn_save.setEnabled(true);
                        }
                    }
                    break;
            }
        }
    };
    private void showSelectSexDialog() {
        // 1是男，0是女，2是全部
        String[] sexs = new String[]{ getString(R.string.user_body), getString(R.string.user_girl)};
        int checkItem = 0;
        new AlertDialog.Builder(this).setTitle(getString(R.string.select_sex_title))
                .setSingleChoiceItems(sexs, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            user_sex_tv.setText(R.string.user_body);
                        } else {
                            user_sex_tv.setText(R.string.user_girl);
                        }
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
    }

}
