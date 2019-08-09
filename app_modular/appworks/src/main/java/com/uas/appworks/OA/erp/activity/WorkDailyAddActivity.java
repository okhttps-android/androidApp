package com.uas.appworks.OA.erp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.widget.crouton.Crouton;
import com.core.widget.view.Activity.SelectActivity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.platform.activity.PlatDailyShowActivity;
import com.uas.appworks.OA.platform.activity.PlatWDdetailyActivity;
import com.uas.appworks.OA.platform.model.PlatDailySubBackBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by FANGlh on 2016/11/1.
 */
public class WorkDailyAddActivity extends BaseActivity implements RecognizerDialogListener, View.OnClickListener {

    private static final int DAILY_SUBMITTED_SUCCESSFULLY = 1101;  //提交请求成功后返回
    private static final int CLEAR_AF_UPDATE_DOC_STATE = 1208;
    private static final int DAILY_RESUBMITTED_SUCCESSFULLY = 1209;  //反提交请求成功后返回
    private static final int LAST_SUBMIT_SUCCESSFULLY = 1210;
    private static final int GRAB_JOB_CONTENT = 1219;
    private static final int B2B_DAILY_SUBMITTED = 307;
    private static final int B2B_DAILY_UPDATE = 30901;
    private FormEditText add_summary;
    private ImageView share_experience;
    private boolean imOk = false;
    private boolean erpOk = false;
    private boolean isSbmit = false;
    private int mkeyValue;
    private TextView summary_limit_tv;
    private TextView plan_limit_tv;
    private TextView experience_limit_tv;
    private TextView add_plan;
    private TextView add_experience;
    private String resubmit;
    private Button add_submit;
    private String wd_finishedtask;
    private String wd_unfinishedtask;
    private PlatDailySubBackBean mPlatDailySubBackBean;
    private String fromqzone;
    private ImageView voice_summary;
    private ImageView voice_plan;
    private ImageView voice_experience;
    private Boolean platform;
    private String last_change_experience;
    private String last_change_plan;
    private String last_change_summary;
    private String caller = "WorkDaily";
    private String noid;
    private void initIds() {
        add_summary = (FormEditText) findViewById(R.id.add_work_daily_summary_et);
        share_experience = (ImageView) findViewById(R.id.add_work_daily_sharing_experience_im);
        summary_limit_tv = (TextView) findViewById(R.id.summary_limit_tv);
        experience_limit_tv = (TextView) findViewById(R.id.experience_limit_tv);
        add_plan = (TextView) findViewById(R.id.add_work_daily_plan_et);
        add_experience = (TextView) findViewById(R.id.add_work_daily_experience_et);
        add_submit = (Button) findViewById(R.id.add_work_daily_submit_iv);
        voice_summary  = (ImageView) findViewById(R.id.voice_summary_iv);
        voice_plan = (ImageView) findViewById(R.id.voice_plan_iv);
        voice_experience = (ImageView) findViewById(R.id.voice_experience_iv);
        plan_limit_tv  = (TextView) findViewById(R.id.plan_limit_tv);
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case DAILY_SUBMITTED_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("submit_message", result);
                            if (JSON.parseObject(result).containsKey("success") && JSON.parseObject(result).getBoolean("success")) {
                                Toast.makeText(ct, getString(R.string.daily_submit_success), Toast.LENGTH_SHORT).show();
                                try {
                                    mkeyValue = new JSONObject(result).getJSONArray("data").getJSONObject(0).getInt("WD_ID");
                                    wd_finishedtask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_CONTEXT");
                                    wd_unfinishedtask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_UNFINISHEDTASK");
                                    Log.i("First_mkeyValue", mkeyValue + " ");
                                    Log.i("wd_context", wd_finishedtask + " ");
                                    Log.i("wd_context", wd_unfinishedtask + " ");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                doGrabJobContent(mkeyValue);
                                if (imOk) {
                                    isSbmit = false;
                                    doGrabJobContent(mkeyValue);
                                }

                            } else {
                                Crouton.makeText(ct, getString(R.string.daily_submit_failed));
                                progressDialog.dismiss();
                                add_submit.setEnabled(true);
                            }

                        }
                    }
                    break;
                case CLEAR_AF_UPDATE_DOC_STATE:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("updata_message", result);
                            if (JSON.parseObject(result).containsKey("success") && JSON.parseObject(result).getBoolean("success")) {
                                if ((!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit")) ||
                                        (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub"))) {
                                    ToastMessage(getString(R.string.daily_update_success));

                                    // 更新之后再提交覆盖之前同id单据
                                    Map<String, Object> params = new HashMap<>();
                                    String url = CommonUtil.getAppBaseUrl(ct) + "/oa/persontask/submitWorkDaily.action";
                                    params.put("caller", "WorkDaily");
                                    params.put("id", mkeyValue);
                                    LinkedHashMap<String, Object> last_headers = new LinkedHashMap<>();
                                    last_headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                                    ViewUtil.httpSendRequest(ct, url, params, handler, last_headers, LAST_SUBMIT_SUCCESSFULLY, null, null, "post");

                                }
                            }
                        }
                    }
                    break;

                case LAST_SUBMIT_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            if (JSON.parseObject(result).containsKey("success") && JSON.parseObject(result).getBoolean("success")) {
                                LogUtil.prinlnLongMsg("LAST_SUBMIT_message", result);
                                Toast.makeText(ct, getString(R.string.daily_resubmit_success), Toast.LENGTH_SHORT).show();
                                try {
                                    wd_finishedtask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_CONTEXT");
                                    wd_unfinishedtask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_UNFINISHEDTASK");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                doGrabJobContent(mkeyValue);
                            } else {
                                Crouton.makeText(ct, getString(R.string.daily_resubmit_failed));
                                progressDialog.dismiss();
                                add_submit.setEnabled(true);
                            }
                        }
                    }
                    break;
                case GRAB_JOB_CONTENT:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("GRAB_JOB_CONTENT", result);
                            if (!TextUtils.isEmpty(wd_finishedtask) || !TextUtils.isEmpty(wd_unfinishedtask)) {
                                ToastMessage(getString(R.string.workcontext_grap_success));
                            }
                            judgeApprovers(mkeyValue);

                        }
                    }
                    break;

                case 0x14:
                    String message = bundle.getString("result");
                    if (!StringUtil.isEmpty(message) && JSONUtil.validate(message)) {
                        com.alibaba.fastjson.JSONObject object = JSON.parseObject(message);
                        if (object.containsKey("assigns")) {
                            JSONArray array = JSON.parseObject(message).getJSONArray("assigns");
                            com.alibaba.fastjson.JSONObject o = array.getJSONObject(0);
                            String noid = "";
                            if (o != null && o.containsKey("JP_NODEID")) {
                                noid = o.getString("JP_NODEID");
                            }
                            JSONArray data = null;
                            if (o != null && o.containsKey("JP_CANDIDATES")) {
                                data = o.getJSONArray("JP_CANDIDATES");
                            }
                            if (!StringUtil.isEmpty(noid) && data != null && data.size() > 0) {
                                sendToSelect(noid, data);
                            } else {
                                progressDialog.dismiss();
                                jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                            }

                        } else {
                            progressDialog.dismiss();
                            jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                        }
                    } else {
                        progressDialog.dismiss();
                        jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                    }
                    break;
                case 0x15:
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            jumptododetail(mkeyValue); //延时跳转，确认抓取成功
//
//                        }
//                    }, 500);
                    progressDialog.dismiss();
                    jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                    break;


                // 以下为平台部分
                case B2B_DAILY_SUBMITTED:  // 初次日报提交成功
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("B2B_DAILY_SUBMITTED", result);
                            com.alibaba.fastjson.JSONObject resultJsonObject = JSON.parseObject(result);
                            mPlatDailySubBackBean = JSON.parseObject(resultJsonObject.toString(), PlatDailySubBackBean.class);
                            Toast.makeText(getApplicationContext(), getString(R.string.submit_success), Toast.LENGTH_SHORT).show();
                            mkeyValue = mPlatDailySubBackBean.getData().getWd_id();
                            Log.i("return_wd_id", mkeyValue + "");
                            jumptoplatdetail(mkeyValue);
                            finish();
                        }
                    }
                    break;
                case B2B_DAILY_UPDATE: // 平台日报更新
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            com.alibaba.fastjson.JSONObject resultJsonObject = JSON.parseObject(result);
                            mPlatDailySubBackBean = JSON.parseObject(resultJsonObject.toString(), PlatDailySubBackBean.class);
                            Toast.makeText(getApplicationContext(), getString(R.string.daily_resubmit_success), Toast.LENGTH_SHORT).show();
                            mkeyValue = mPlatDailySubBackBean.getData().getWd_id();
                            Log.i("return_wd_id", mkeyValue + "");
                            jumptoplatdetail(mkeyValue);
                            finish();
                        }
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                            progressDialog.dismiss();
                            add_submit.setEnabled(true);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x22) {
            if (data != null) {
                SelectBean b = data.getParcelableExtra("data");
                if (b == null) return;
                String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                getEmnameByReturn(name);
            }else {
                jumptododetail(mkeyValue);
            }
        }
    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) return;
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                selectApprovers(name);
            } else {
                progressDialog.dismiss();
                jumptododetail(mkeyValue);
            }
        } else {
            progressDialog.dismiss();
            jumptododetail(mkeyValue);
        }
    }

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
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x15, null, null, "post");
    }

    private void sendToSelect(String noid, JSONArray data) {
        this.noid = noid;
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

    private void judgeApprovers(int mkeyValue) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        param.put("id", mkeyValue);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x14, null, null, "post");
    }

    private void jumptoplatdetail(int mkeyValue) {
        Intent intent = new Intent(activity, PlatWDdetailyActivity.class);
        intent.putExtra("Date", DateFormatUtil.long2Str(DateFormatUtil.YMD));
//        intent.putExtra("Date",mPlatDailySubBackBean.getData().getWd_date());
        intent.putExtra("caller", "WorkDaily");
        intent.putExtra("ID", mkeyValue);
        intent.putExtra("fromwhere", "submitdaily");
        intent.putExtra("Content", add_summary.getText().toString());
        intent.putExtra("WD_Status", "已提交");
        intent.putExtra("Plan", add_plan.getText().toString());
        intent.putExtra("Experience", add_experience.getText().toString());
        progressDialog.dismiss();
        add_submit.setEnabled(true);
        startActivity(intent);
        finish();

    }


    public void doGrabJobContent(int mkeyValue) {
        //跳转之前抓取工作内容
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/oa/persontask/catchWorkContent.action";
        HashMap<String, Object> params = new HashMap<>();
        params.put("caller", "WorkDaily");
        params.put("id", mkeyValue);
        LogUtil.d(JSON.toJSONString(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, handler, headers, GRAB_JOB_CONTENT, null, null, "post");
    }


    public void jumptododetail(int mkeyValue) {
        Intent intent = new Intent(activity, DailydetailsActivity.class);
        intent.putExtra("Date", DateFormatUtil.long2Str(DateFormatUtil.YMD));
        intent.putExtra("caller", "WorkDaily");
        intent.putExtra("ID", mkeyValue);
        intent.putExtra("fromwhere", "submitdaily");
        intent.putExtra("Content", add_summary.getText().toString());
        intent.putExtra("WD_Status", "已提交");
        intent.putExtra("Plan", add_plan.getText().toString());
        intent.putExtra("Experience", add_experience.getText().toString());
        intent.putExtra("Donetask", wd_finishedtask);
        intent.putExtra("Undotask", wd_unfinishedtask);
        progressDialog.dismiss();
        add_submit.setEnabled(true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        doVoiceClickEvent();
    }



    private int voice_type = 0;

    private void doVoiceClickEvent() {
        voice_summary.setOnClickListener(this);
        voice_plan.setOnClickListener(this);
        voice_experience.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.voice_summary_iv)
            voice_type = 1;
        else if (v.getId() == R.id.voice_plan_iv)
            voice_type = 2;
        else if (v.getId() == R.id.voice_experience_iv)
            voice_type = 3;
        RecognizerDialogUtil.showRecognizerDialog(ct, this);
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        if (voice_type == 1) {
            add_summary.setText(add_summary.getText().toString() + text);
        } else if (voice_type == 2) {
            add_plan.setText(add_plan.getText().toString() + text);
        } else if (voice_type == 3) {
            add_experience.setText(add_experience.getText().toString() + text);
        }
    }

    @Override
    public void onError(SpeechError speechError) {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!TextUtils.isEmpty(resubmit) && (resubmit.equals("resubmit") || resubmit.equals("unsub_tosub"))) {

        } else {
            getMenuInflater().inflate(R.menu.menu_list, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.push) {
            if (!platform) {
                startActivity(new Intent(activity, WorkDailyShowActivity.class)
                        .putExtra("fromwhere", "nosubmitdaily"));
            } else {
                startActivity(new Intent(activity, PlatDailyShowActivity.class)
                        .putExtra("fromwhere", "nosubmitdaily"));
            }

        }

        if (item.getItemId() == android.R.id.home) {
            Intent intent = getIntent();
            fromqzone = intent.getStringExtra("fromqzone");
            if (add_summary.getText().toString().length() > 0 ||
                    add_plan.getText().toString().length() > 0 ||
                    add_experience.getText().toString().length() > 0) {
                new AlertDialog
                        .Builder(mContext)
                        .setTitle(getString(R.string.common_notice))
                        .setMessage(getString(R.string.daily_exit_notice))
                        .setNegativeButton(getString(R.string.common_cancel), null)
                        .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!TextUtils.isEmpty(resubmit) && ("unsub_tosub".equals(resubmit) || "resubmit".equals(resubmit))) {
                                    if (!platform) {
                                        startActivity(new Intent(WorkDailyAddActivity.this, WorkDailyShowActivity.class));
                                    } else {
                                        startActivity(new Intent(WorkDailyAddActivity.this, PlatDailyShowActivity.class));
                                    }
                                } else if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {

                                } else {
                                    Intent intent1 = new Intent("com.modular.main.MainActivity");
                                    startActivity(intent1);
                                }
                                finish();
                            }
                        }).show();
            } else if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {
                finish();
            } else {
                Intent intent1 = new Intent("com.modular.main.MainActivity");
                startActivity(intent1);
                finish();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (add_summary.getText().toString().length() > 0 ||
                add_plan.getText().toString().length() > 0 ||
                add_experience.getText().toString().length() > 0) {
            new AlertDialog
                    .Builder(mContext)
                    .setTitle(getString(R.string.common_notice))
                    .setMessage(getString(R.string.daily_exit_notice))
                    .setNegativeButton(getString(R.string.common_cancel), null)
                    .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(resubmit) && "unsub_tosub".equals(resubmit)) {
                                if (!platform) {
                                    startActivity(new Intent(WorkDailyAddActivity.this, WorkDailyShowActivity.class));
                                } else {
                                    startActivity(new Intent(WorkDailyAddActivity.this, PlatDailyShowActivity.class));
                                }
                            } else {
                                Intent intent1 = new Intent("com.modular.main.MainActivity");
                                startActivity(intent1);
                            }
                            finish();
                        }
                    }).show();
        } else {
            Intent intent1 = new Intent("com.modular.main.MainActivity");
            startActivity(intent1);
            finish();
        }

    }

    public void initView() {
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;// 判断当前为平台用户
        mPlatDailySubBackBean = new PlatDailySubBackBean();
        setContentView(R.layout.activity_add_work_daily);
        initIds();
        setTitle(getString(R.string.oaworkdaily_title));
        ViewUtils.inject(this);

        // 对输入的三项内容进行动态监听限制字数，只提醒，不限制输入
        add_summary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (add_summary.getText().toString().length() > 500) {
                    summary_limit_tv.setVisibility(View.VISIBLE);
                } else {
                    summary_limit_tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                last_change_summary = add_summary.getText().toString();
            }
        });

        add_plan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (add_plan.getText().toString().length() > 300) {
                    plan_limit_tv.setVisibility(View.VISIBLE);
                } else {
                    plan_limit_tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                last_change_plan = add_plan.getText().toString();
            }
        });

        add_experience.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (add_experience.getText().toString().length() > 300) {
                    experience_limit_tv.setVisibility(View.VISIBLE);
                } else {
                    experience_limit_tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                last_change_experience = add_experience.getText().toString();
            }
        });

        Intent intent = getIntent();
        resubmit = intent.getStringExtra("resubmit");
        // 判断有没从列表界面、详情界面返回的重新提交的数据
        if (!TextUtils.isEmpty(resubmit)) {
            if (resubmit.equals("unsub_tosub")) {
                add_submit.setText(getString(R.string.common_submit_button));
            } else if (resubmit.equals("resubmit")) {
                add_submit.setText(getString(R.string.common_resubmit_button));
            }
            if (!TextUtils.isEmpty(intent.getStringExtra("rs_summary"))) {
                add_summary.setText(intent.getStringExtra("rs_summary"));
            }

            if (!TextUtils.isEmpty(intent.getStringExtra("rs_plan"))) {
                add_plan.setText(intent.getStringExtra("rs_plan"));
            } else {
                add_plan.setText("");
            }
            if (!TextUtils.isEmpty(intent.getStringExtra("rs_experience"))) {
                add_experience.setText(intent.getStringExtra("rs_experience"));
            } else {
                add_experience.setText("");
            }

            if (!TextUtils.isEmpty(intent.getStringExtra("rs_donetask"))) {
                wd_finishedtask = intent.getStringExtra("rs_donetask");
            }
//            if (!TextUtils.isEmpty(intent.getStringExtra("rs_undotask"))){
//                wd_unfinishedtask = intent.getStringExtra("rs_undotask");
//            }

        }
        mkeyValue = intent.getIntExtra("id", 0);
        Log.i("resubmit_mkeyValue", mkeyValue + "");


        add_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (add_summary.testValidity() &&
                        add_plan.getText().toString().length() <= 300 &&
                        add_experience.getText().toString().length() <= 300) {
                    sendAble();
                }
            }
        });
        share_experience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtil.isEmpty(add_experience.getText().toString())) {
                    ToastMessage(getString(R.string.share_experience_notice));
                } else {
                    Intent intent = new Intent("com.modilar.circle.SendShuoshuoActivity");
                    intent.putExtra("Experience", add_experience.getText().toString());
                    intent.putExtra("type", 0);
                    if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {
                        startActivity(intent);
                        finish();
                    } else {
                        startActivity(intent);
                    }

                }
            }
        });


    }

    private void showsubmitDialog() {
        new AlertDialog
                .Builder(mContext)
                .setTitle(getString(R.string.common_notice))
                .setMessage(getString(R.string.daily_submit_notice))
                .setNegativeButton(getString(R.string.common_cancel), null)
                .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (platform) {  // 判断当前为平台用户
                            sendWorkDailyByB2B(add_summary.getText().toString(),
                                    add_plan.getText().toString(),
                                    add_experience.getText().toString());
                        } else {
                            sendWorkDailyByErp(add_summary.getText().toString(),
                                    add_plan.getText().toString(),
                                    add_experience.getText().toString());
                        }

                    }
                }).show();
    }


    private void sendAble() {
        StringBuilder builder = new StringBuilder();
        if (!StringUtil.isEmpty(add_summary.getText().toString())) {
            builder.append(getString(R.string.wd_summary_title) + add_summary.getText().toString() + "\n");
        } else {
            Crouton.makeText(ct, R.string.add_summed);
            return;
        }
        if (!StringUtil.isEmpty(add_plan.getText().toString())) {
            builder.append(getString(R.string.wd_plan_title) + add_plan.getText().toString() + "\n");
        }
        /*else{
            Crouton.makeText(ct, R.string.add_plan);
            return;
        }*/
        if (!StringUtil.isEmpty(add_experience.getText().toString())) {
            builder.append(getString(R.string.wd_experience_title) + add_experience.getText().toString() + "\n");
        }
        if (MyApplication.getInstance().isNetworkActive()) {
            showsubmitDialog();
        } else {
            ToastMessage(getResources().getString(R.string.networks_out));
        }
    }

    private void sendWorkDailyByErp(String s1, String s2, String s3) {
        progressDialog.show();
        add_submit.setEnabled(false);
        //执行重新提交之更改单据状态操作
        if ((!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit") && mkeyValue != 0) ||
                (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub") && mkeyValue != 0)) {
            String sb_summary = string2Json(last_change_summary);
            String sb_plan = string2Json(last_change_plan);
            String sb_experience = string2Json(last_change_experience);

//            String sb_summary = last_change_summary;
//            String sb_plan = last_change_plan;
//            String sb_experience = last_change_experience;
            Map<String, Object> formStoreMap2 = new HashMap<>();
            formStoreMap2.put("wd_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap2.put("wd_comment", sb_summary);
            formStoreMap2.put("wd_plan", sb_plan);
            formStoreMap2.put("wd_experience", sb_experience);
            formStoreMap2.put("wd_id", String.valueOf(mkeyValue));   //这个地方之前传int类型调试很久都更新不了，只能用String类型，不明白后台处理机制
            //更新单据数据
            String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/oa/persontask/updateWorkDaily.action";
            HashMap<String, Object> params = new HashMap<>();
            String formStore2 = JSONUtil.map2JSON(formStoreMap2);
            params.put("caller", "WorkDaily");
            params.put("formStore", formStore2);
            LogUtil.d(JSON.toJSONString(params));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, handler, headers, CLEAR_AF_UPDATE_DOC_STATE, null, null, "post");
            Log.i("flhupdatedaily: ", formStore2 + " ");

        } else {
//             执行第一次直接提交
//            s1 = string2Json(s1);
//            s2 = string2Json(s2);
//            s3 = string2Json(s3);
            Map<String, Object> formStoreMap = new HashMap<>();
            formStoreMap.put("wd_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("wd_comment", s1);
            formStoreMap.put("wd_plan", s2);
            formStoreMap.put("wd_experience", s3);

            String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "mobile/addWorkReport.action";
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", "WorkDaily");
            params.put("formStore", formStore);
            LogUtil.d(JSON.toJSONString(params));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, handler, headers, DAILY_SUBMITTED_SUCCESSFULLY, null, null, "post");
            Log.i("flhaddworkdaily: ", formStore + " ");
        }

    }

    private void sendWorkDailyByB2B(String s1, String s2, String s3) {
        progressDialog.show();
        Map<String, Object> formStoreMap = new HashMap<>();
        formStoreMap.put("wd_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        formStoreMap.put("wd_comment", s1);
        formStoreMap.put("wd_plan", s2);
        formStoreMap.put("wd_experience", s3);
        formStoreMap.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        formStoreMap.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        add_submit.setEnabled(false);
        if ((!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit") && mkeyValue != 0) ||
                (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub") && mkeyValue != 0)) {
            //重新提交
            formStoreMap.put("wd_id", mkeyValue);
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().first_add_workdaily;
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", "WorkDaily");
            params.put("formStore", formStore);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(this, url, params, handler, headers, B2B_DAILY_UPDATE, null, null, "post");

        } else {  //第一次提交
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().first_add_workdaily;
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", "WorkDaily");
            params.put("formStore", formStore);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(this, url, params, handler, headers, B2B_DAILY_SUBMITTED, null, null, "post");
        }

    }

    /**
     * JSON字符串特殊字符处理，比如：“\A1;1300”
     *
     * @param s
     * @return String
     */
    public String string2Json(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public OnFinishOa onFinishOa;


    public interface OnFinishOa {
        void onFinish();
    }

    public void setOnFinishOa(OnFinishOa onFinishOa) {
        this.onFinishOa = onFinishOa;
    }
}
