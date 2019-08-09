package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.work.WorkModelDao;
import com.core.model.SelectBean;
import com.core.model.WorkModel;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.widget.crouton.Crouton;
import com.core.widget.view.Activity.SelectActivity;
import com.uas.applocation.UasLocationHelper;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 1.手机变更单  type==1  macAddress  mac地址
 * 2.补卡申请    type==2  macAddress  补卡班次  yyy-MM-dd 周三 HH:mm:ss
 * Created by pengminggong on 2016/10/26.
 */
public class ChangeMobileActivity extends BaseActivity {
    private TextView mac_tv;
    private TextView title_tv;
    private TextView sum_tag;
    private EditText sum_tv;
    private Button exit_btn;

    private int type;
    private String tag = null;
    private String caller;
    private boolean submitOk = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            progressDialog.dismiss();
            String message = bundle.getString("result");
            switch (msg.what) {
                case 0x11://变更mac地址
                    submitOk = true;
                    try {
                        int id = JSON.parseObject(message).getInteger("id");
                        submitApprover(id);
                    } catch (Exception e) {
                        progressDialog.dismiss();
                    }
                    break;
                case 0x12://提交审批流
                    progressDialog.dismiss();
                    int id = bundle.getInt("id");
                    judgeApprovers(id);
                    break;
                case 0x13://申请补卡申诉
                    submitOk = true;
                    if (JSON.parseObject(message).containsKey("success") && JSON.parseObject(message).getBoolean("success")) {
                        endActivity();
                    } else {
                        Crouton.makeText(ct, "未知错误");
                    }
                    break;
                case 0x14://获取审批人列表
                    if (!StringUtil.isEmpty(message) && JSONUtil.validate(message)) {
                        JSONObject object = JSON.parseObject(message);
                        if (object.containsKey("assigns") && !ListUtils.isEmpty(object.getJSONArray("assigns"))) {
                            JSONArray array = object.getJSONArray("assigns");
                            JSONObject o = array.getJSONObject(0);
                            String noid = "";
                            if (o != null && o.containsKey("JP_NODEID")) {
                                noid = o.getString("JP_NODEID");
                            }
                            JSONArray data = null;
                            if (o != null && o.containsKey("JP_CANDIDATES")) {
                                data = o.getJSONArray("JP_CANDIDATES");
                            }
                            if (!StringUtil.isEmpty(noid) && data != null && data.size() > 0)
                                sendToSelect(noid, data);
                        } else {
                            endActivity();
                        }
                    } else {
                        endActivity();
                    }
                    break;
                case 0x15://提交审批人
                    endActivity();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (submitOk) {
                        endActivity();
                        return;
                    }
                    if (StringUtil.isEmpty(message)) return;
                    String showMessage = null;
                    if (JSONUtil.validate(message) && JSON.parseObject(message).containsKey("exceptionInfo")) {
                        JSONObject exceptionInfo = JSON.parseObject(message);
                        showMessage = exceptionInfo.getString("exceptionInfo");
                    } else {
                        showMessage = message;
                    }
                    Crouton.makeText(ct, showMessage);
                    break;
                default:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    break;
            }
            submiting = false;
        }


    };
    private boolean isB2b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_mobile);
        if (getIntent() != null) {
            type = getIntent().getIntExtra("type", 1);
            tag = getIntent().getStringExtra("tag");
        }
        initView();
    }

    private void endActivity() {
     showToast(R.string.change_signin_ok);
        Intent intent = new Intent();
        intent.putExtra("isChange", true);
        if (type == 2) {
            WorkModel model = getIntent().getParcelableExtra("model");
            if (model != null) {
                boolean isWork = getIntent().getBooleanExtra("isWork", false);
                if (isWork)
                    model.setWorkAllegedly(model.getWorkTime());
                else
                    model.setOffAllegedly(model.getOffTime());
                WorkModelDao.getInstance().update(model);
            }
        }
        setResult(0x20, intent);
        finish();
    }

    private boolean submiting = false;

    private void initView() {

        mac_tv = (TextView) findViewById(R.id.mac_tv);
        title_tv = (TextView) findViewById(R.id.title_tv);
        sum_tag = (TextView) findViewById(R.id.sum_tag);
        sum_tv = (EditText) findViewById(R.id.sum_tv);
        exit_btn = (Button) findViewById(R.id.exit_btn);

        String title;
        String sumTag;
        int actionTitle;
        if (type == 2) {
            actionTitle = R.string.supple_signin;
            title = getString(R.string.mobile_signcard);
            sumTag = getString(R.string.complaint_reson);
            caller = "MobileSignCard";

        } else {
            caller = "MobileMacChange";
            actionTitle = R.string.change_mobile;
            title = getString(R.string.mobile_mac);
            sumTag = getString(R.string.mobile_change_name);
        }
        setTitle(actionTitle);
        title_tv.setText(title);
        sum_tag.setText(sumTag);
        sum_tv.setHint(getString(R.string.common_input2) + sumTag);
        String mac = null;
        if (getIntent() != null)
            mac = getIntent().getStringExtra("macAddress");
        mac_tv.setText(StringUtil.isEmpty(mac) ? "" : mac);

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = TextUtils.isEmpty(sum_tv.getText()) ? "" : sum_tv.getText().toString();
                if (StringUtil.isEmpty(message))
                  showToast( R.string.input_reason);
                else {
                    if (submiting) return;
                    message = StringUtil.toHttpString(message);
                    submiting = true;
                    isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
                    if (type == 2) {
                        if (isB2b)
                            suppleB2bSignin(message);
                        else
                            suppleSignin(message);
                    } else
                        validatorMac();
                }
            }


        });
    }

    /*获取审批人列表*/
    private void judgeApprovers(int id) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        param.put("id", id);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x14, null, null, "post");
    }

    private String noid;

    /**
     * @param noid
     * @param data
     */
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


    private void suppleB2bSignin(String message) {
        progressDialog.show();
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveSignApp;
        Map<String, Object> formStore = new HashMap<>();
        formStore.put("emuu", CommonUtil.getEmcode());
        formStore.put("remark", message);
        formStore.put("mobile", MyApplication.getInstance().mLoginUser.getTelephone());
        formStore.put("signtime", getTime(tag));
        formStore.put("address", UasLocationHelper.getInstance().getUASLocation().getAddress());
        formStore.put("uu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        Map<String, Object> param = new HashMap<>();
        param.put("formStore", JSONUtil.map2JSON(formStore));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x13, null, null, "post");
    }

    /*补卡申请申诉*/
    private void suppleSignin(String message) {
        progressDialog.show();
        String url = "mobile/oa/saveAndSubmitMobileSignCard.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        Map<String, Object> params = new HashMap<>();
        String emname = CommonUtil.getName();
        params.put("ms_emname", emname);
        params.put("ms_remark", message);
        params.put("ms_status", "在录入");
        params.put("ms_statuscode", "ENTERING");
        params.put("ms_emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("ms_address", UasLocationHelper.getInstance().getUASLocation().getAddress());//地址
        params.put("ms_mobile", MyApplication.getInstance().mLoginUser.getTelephone());//手机号
        //班次时间:
        params.put("ms_signtime", getTime(tag));//申诉时间
        String formStore = JSONUtil.map2JSON(params);
        param.put("formStore", formStore);
        Request request = new Request.Bulider()
                .setWhat(0x13)
                .setMode(Request.Mode.POST)
                .setParam(param)
                .setUrl(url)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
            @Override
            public void result(int what, boolean isJSON, String message, Bundle bundle) {
//                endActivity();
                if (isJSON) {
                    int ms_id = JSONUtil.getInt(message, "ms_id");
                    Log.d("hims_id", ms_id + "");
                    if (ms_id > 0) {
                        judgeApprovers(ms_id);
                    } else {
                        String exceptionInfo = JSONUtil.getText(message, "exceptionInfo");
                        if (!StringUtil.isEmpty(exceptionInfo)) {
                        showToast( exceptionInfo);
                        }
                    }
                }
                submiting = false;
                progressDialog.dismiss();
            }

            @Override
            public void error(int what, String message, Bundle bundle) {
                LogUtil.i("审批流触发失败");
                progressDialog.dismiss();
                if (!StringUtil.isEmpty(message))
                   showToast( message);
                submiting = false;
            }
        });


    }


    private String getTime(String tag) {
        if (StringUtil.isEmpty(tag)) return null;
        if (getIntent() != null && !StringUtil.isEmpty(getIntent().getStringExtra("date"))) {
            return getIntent().getStringExtra("date") + " " + tag + ":00";
        } else {
            return DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + tag + ":00";
        }
    }

    /*提交新的mac地址*/
    private synchronized void validatorMac() {
        //获取网络数据
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/saveCommon.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("mm_emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        params.put("mm_emname", emname);
        params.put("mm_macaddress", mac_tv.getText().toString());
        params.put("mm_remark", sum_tv.getText().toString());
        String addr = UasLocationHelper.getInstance().getUASLocation().getAddress();
        params.put("mm_address", addr);
        params.put("mm_status", "在录入");
        params.put("mm_statuscode", "ENTERING");
        params.put("mm_date", TimeUtils.f_long_2_str(System.currentTimeMillis()));
        param.put("caller", caller);
        param.put("formStore", JSONUtil.map2JSON(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x11, null, null, "get");
    }


    /**
     * 提交审批流
     *
     * @param id
     */
    private void submitApprover(int id) {
        //获取网络数据
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/submitCommon.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        param.put("id", id);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x12, message, bundle, "get");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x22) {
            if (data != null) {
                SelectBean b = data.getParcelableExtra("data");
                if (b == null) return;
                String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                getEmnameByReturn(name);
            } else endActivity();
        }
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
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x15, null, null, "post");
    }
}
