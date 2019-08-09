package com.xzjmyk.pm.activity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.andreabaccega.widget.FormEditText;
import com.common.data.DateFormatUtil;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.app.Constants;
import com.common.data.ListUtils;
import com.core.utils.CommonInterface;
import com.xzjmyk.pm.activity.view.crouton.Crouton;
import com.xzjmyk.pm.activity.view.crouton.Style;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2016/9/19.
 */
public class ApplySubscribeActivity extends BaseActivity {
    private final static int APPLY_SUBSCRIPTION = 33;
    private final static int GET_AS_ID = 34;
    private final static int SUBMIT_SUBSCRIPTION = 35;

    private FormEditText mSubscribeNameEt, mDataEt, mStatusEt, mSubscribePersonEt, mPostEt, mBranchEt, mReasonEt;
    private int mAsId;
    private DBManager dbManager;

    private String mEmCode, mEmName, mEmDepart, mEmPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_subscribe);
        setTitle("申请订阅");
        initView();
        initEvent();
        initData();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_AS_ID:
                    String resultStr = msg.getData().getString("result");
                    try {
                        JSONObject resultJsonObject = new JSONObject(resultStr);
                        if (resultJsonObject != null && resultJsonObject.getBoolean("success")) {
                            mAsId = resultJsonObject.getInt("id");
                            Log.d("apply_subscribe_id", mAsId + "");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case APPLY_SUBSCRIPTION:
                    String saveResult = msg.getData().getString("result");
                    Log.d("apply_subscribe_save", saveResult + "");
                    Crouton.makeText(ApplySubscribeActivity.this, "申请保存成功", Style.CONFIRM).show();
                    try {
                        JSONObject saveObject = new JSONObject(saveResult);
                        if (saveObject != null && saveObject.getBoolean("success")) {
                            sendSubmitRequest();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SUBMIT_SUBSCRIPTION:
                    progressDialog.dismiss();
                    String submitResult = msg.getData().getString("result");
                    Log.d("apply_subscrive_submit", submitResult);
                    Crouton.makeText(ApplySubscribeActivity.this, "申请提交成功", Style.CONFIRM).show();
                    com.core.utils.CommonUtil.imageToast(ApplySubscribeActivity.this, R.drawable.ic_apply_submit_success, "", 2000);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(msg.getData().getString("result"));
                    break;
            }
        }
    };

    private void initView() {
        mSubscribeNameEt = (FormEditText) findViewById(R.id.apply_subscribe_name_et);
        mDataEt = (FormEditText) findViewById(R.id.apply_subscribe_data_et);
        mStatusEt = (FormEditText) findViewById(R.id.apply_subscribe_status_et);
        mSubscribePersonEt = (FormEditText) findViewById(R.id.apply_subscribe_person_et);
        mPostEt = (FormEditText) findViewById(R.id.apply_subscribe_post_et);
        mBranchEt = (FormEditText) findViewById(R.id.apply_subscribe_branch_et);
        mReasonEt = (FormEditText) findViewById(R.id.apply_subscribe_reason_et);

        CommonUtil.getCommonId(this, "SUBSAPPLY_SEQ", mHandler, GET_AS_ID);

        mEmCode = CommonUtil.getSharedPreferences(ct, "erp_username");
    }

    private void initEvent() {

    }


    private void initData() {
        dbManager = new DBManager(ct);
        List<EmployeesEntity> db = dbManager.select_getEmployee(
                new String[]{CommonUtil.getSharedPreferences(ct, "erp_master"),
                        CommonUtil.getSharedPreferences(ct, "erp_username")}
                , "whichsys=? and em_code=? ");
        if (!ListUtils.isEmpty(db)) {
            for (EmployeesEntity model : db) {
                Log.i(TAG, "initData:" + model.getEM_DEPART());
                Log.i(TAG, "initData:" + model.getEM_POSITION());
                Log.i(TAG, "initData:" + model.getCOMPANY());
                Log.i(TAG, "initData:" + model.getEm_IMID());

            }
            mEmName = db.get(0).getEM_NAME();
            mEmDepart = db.get(0).getEM_DEPART();
            mEmPort = db.get(0).getEM_POSITION();
        }

        mDataEt.setText(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd"));
        mStatusEt.setText("在录入");
        mSubscribePersonEt.setText(mEmName);
        mPostEt.setText(mEmPort);
        mBranchEt.setText(mEmDepart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_apply_subscribe, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.subscribe_apply:
//                Crouton.makeText(this, "提交成功", Style.CONFIRM).show();
                CommonInterface.getInstance().getCodeByNet("SubsApply", new CommonInterface.OnResultListener() {
                    @Override
                    public void result(boolean isOk, int result, String message) {
                        sendApplyRequest(APPLY_SUBSCRIPTION, message);
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 提交保存
     */
    private void sendSubmitRequest() {
        String submitUrl = CommonUtil.getAppBaseUrl(ct) + "/common/submitCommon.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "SubsApply");
        params.put("id", mAsId);

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, submitUrl, params, mHandler, headers, SUBMIT_SUBSCRIPTION, null, null, "post");
    }

    /**
     * 申请保存
     *
     * @param what
     * @param code
     */
    private void sendApplyRequest(int what, String code) {
        String formStore = "";
        if (mSubscribeNameEt.testValidity() && mDataEt.testValidity()
                && mStatusEt.testValidity() && mSubscribePersonEt.testValidity()
                && mPostEt.testValidity() && mBranchEt.testValidity()) {
            progressDialog.show();
            formStore =
                    "{\n" +
                            "\"id_\":" + mAsId
                            + ",\n" +
                            "\"code_\":\"" + code
                            + "\",\n" +
                            "\"name_\":\"" + ""
                            + "\",\n" +
                            "\"date_\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd")
                            + "\",\n" +
                            "\"status_\":\"" + "在录入"
                            + "\",\n" +
                            "\"statuscode_\":\"" + "ENTERING"
                            + "\",\n" +
                            "\"empcode_\":\"" + mEmCode
                            + "\",\n" +
                            "\"empname_\":\"" + mEmName
                            + "\",\n" +
                            "\"empdep_\":\"" + mEmDepart
                            + "\",\n" +
                            "\"num_id_\":\"" + ""
                            + "\",\n" +
                            "\"num_title_\":\"" + ""
                            + "\",\n" +
                            "\"reason_\":\"" + " "
                            + "\"\n" +
                            "}";
        } else {
            return;
        }

        String url = CommonUtil.getAppBaseUrl(ct) + "/common/saveCommon.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        params.put("caller", "SubsApply");

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }
}
