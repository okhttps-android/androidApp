package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.ActivityUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.Approval;
import com.core.model.EmployeesEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerScrollView;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.core.widget.SquareCenterImageView;
import com.core.widget.view.MyGridView;
import com.modular.apputils.utils.PopupWindowHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appworks.CRM.erp.activity.DeviceDataFormAddActivity;
import com.uas.appworks.OA.erp.activity.form.DataFormDetailActivity;
import com.uas.appworks.OA.erp.activity.form.FormListSelectActivity;
import com.uas.appworks.OA.erp.adapter.ComDocGriddataOutAdapter;
import com.uas.appworks.OA.erp.adapter.CommonDocMainMsgAdapter;
import com.uas.appworks.OA.erp.model.CommonApprovalFlowBean;
import com.uas.appworks.OA.erp.model.CommonDocAMBean;
import com.uas.appworks.OA.erp.utils.approvautils.ApprovaNodeUtil;
import com.uas.appworks.OA.erp.utils.approvautils.NodeAdapter;
import com.uas.appworks.OA.platform.activity.BusinessTravelActivity;
import com.uas.appworks.R;
import com.uas.appworks.activity.DeviceQueryActivity;
import com.uas.appworks.crm3_0.activity.CustomerBillInputActivity;
import com.uas.appworks.crm3_0.activity.CustomerListActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用表单通用详情界面
 * 考勤单据里面的所有单据详情界面都会用到
 * Created by FANGlh on 2016/11/16.
 */
public class CommonDocDetailsActivity extends BaseActivity {

    private static final int COM_DOC_DEVICE_TURN_SACN = 1214;
    private static final int COM_DOC_RESUB_SUCCESSFUL = 1213;
    private static final int COM_DOC_REDELETE_SUCCESSFUL = 1215;
    private static final int NEW_DOC_RESUB_SUCCESSFUL = 52601;
    private static final int New_DOC_REDELETE_SUCCESSFUL = 52602;
    private static final int NEW_DELETE_DOC_REQUEST = 52603;
    private int mNoc = 1;
    private final static int COMMON_DOC_AF_REQUEST = 1125;
    private final static int COMMON_DOC_MSG_REQUEST = 1126;
    private static final int DELETE_DOC_REQUEST = 1127;
    private final int SURE_DEVICE = 11251;
    private EmptyLayout mEmptyLayput_approvalflow;
    private CommonApprovalFlowBean mCommonApprovalFlowBean;
    private CommonDocAMBean mCommonDocAMBean;
    private DBManager manager;
    private List<String> im_ids;
    private int mkeyValue;
    private String mCaller;
    private EmptyLayout mEmptyLayput_applymsg;
    private JSONArray formdataBeans;
    private JSONArray griddataBeans;
    private List<CommonDocAMBean.DatasBean.FormconfigsBean> mFormconfigsBean;
    private List<CommonDocAMBean.DatasBean.GridconfigsBean> mGridconfigsBean;
    private CommonDocMainMsgAdapter mCommonDocMainMsgAdapter;
    private ComDocGriddataOutAdapter mComDocGriddataOutAdapter;

    private MyListView main_msg_lv;
    private ImageView photo_im;
    private TextView name_tv;
    private TextView section_tv;
    private TextView status_tv;
    private MyListView appflow_lv;
    private MyListView second_msg_lv;
    private LinearLayout secondmsg_ll;
    private LinearLayout approval_ll;
    private LinearLayout agree_ll;
    private LinearLayout change_deal_man_ll;
    private LinearLayout disagree_ll;
    private LinearLayout resanddel_ll;
    private TextView sureTv;
    private TextView turnScrapTv;
    private LinearLayout deviceLL;
    private LinearLayout resubmit_ll;
    private LinearLayout delete_ll;
    private CustomerScrollView commondoc_sv;
    private View hide_above_af;
    private LinearLayout ex_invoice_ll;
    private MyGridView grid_view;
    private GridViewAdapter mAdapter;
    private String last_status;
    private String update;
    private Boolean platform;
    private String em_number;
    private String type_emcode;
    private String real_status;
    private String emCode;
    private String mTitle;
    private String statusKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected void onResume() {
        initDate();
        commondoc_sv.smoothScrollTo(0, 0);
        super.onResume();
    }

    private boolean device;
    private boolean message;
    private boolean back_normal;
    private boolean form_new_bill;

    public void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            device = intent.getBooleanExtra("device", false);
            message = intent.getBooleanExtra("message", false);
            back_normal = intent.getBooleanExtra("back_normal", false);
            form_new_bill = intent.getBooleanExtra("form_new_bill", false);
            statusKey = intent.getStringExtra("statusKey");
        }

        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        setContentView(R.layout.activity_common_docui);
        initIDS();
        mEmptyLayput_applymsg = new EmptyLayout(this, main_msg_lv);
        mEmptyLayput_applymsg.setShowEmptyButton(false);
        mEmptyLayput_applymsg.setShowErrorButton(false);
        mEmptyLayput_applymsg.setShowLoadingButton(false);

        //加载主单据信息适配器
        formdataBeans = new JSONArray();
        mFormconfigsBean = new ArrayList<>();
        mCommonDocMainMsgAdapter = new CommonDocMainMsgAdapter(this);

        //加载从单据信息外面层listview适配器
        griddataBeans = new JSONArray();
        mGridconfigsBean = new ArrayList<>();
        mComDocGriddataOutAdapter = new ComDocGriddataOutAdapter(this);

        mEmptyLayput_approvalflow = new EmptyLayout(this, appflow_lv);
        mEmptyLayput_approvalflow.setShowEmptyButton(false);
        mEmptyLayput_approvalflow.setShowErrorButton(false);
        mEmptyLayput_approvalflow.setShowLoadingButton(false);
        //加载审批流的适配器
        mCommonApprovalFlowBean = new CommonApprovalFlowBean();

        manager = new DBManager(this);
        im_ids = new ArrayList<>();
        em_number = new String(); //单据申请人编号
        real_status = new String();//单据真实状态

        String imageUri = "drawable://" + R.drawable.common_header_boy;
        AvatarHelper.getInstance().display(imageUri, photo_im, true);

        im_ids = new ArrayList<>();
        afpeople_names = new ArrayList<>();


        mAdapter = new GridViewAdapter();
        grid_view.setAdapter(mAdapter);
    }

    private void initIDS() {
        main_msg_lv = (MyListView) findViewById(R.id.common_docui_main_msg_lv);
        photo_im = (ImageView) findViewById(R.id.common_docui_photo_img);
        name_tv = (TextView) findViewById(R.id.common_docui_name_tv);
        section_tv = (TextView) findViewById(R.id.common_docui_Section_tv);
        status_tv = (TextView) findViewById(R.id.common_docui_status_tv);
        appflow_lv = (MyListView) findViewById(R.id.common_docui_approval_flow_lv);
        second_msg_lv = (MyListView) findViewById(R.id.common_docui_second_msg_lv);
        secondmsg_ll = (LinearLayout) findViewById(R.id.common_docui_secondmsg_ll);
        approval_ll = (LinearLayout) findViewById(R.id.common_docui_agree_and_change_ll);
        agree_ll = (LinearLayout) findViewById(R.id.common_docui_agree_ll);
        change_deal_man_ll = (LinearLayout) findViewById(R.id.common_docui_change_dealman_ll);
        disagree_ll = (LinearLayout) findViewById(R.id.common_docui_disagree_ll);
        resanddel_ll = (LinearLayout) findViewById(R.id.item_common_docui_res_and_del_ll);
        sureTv = (TextView) findViewById(R.id.sureTv);
        turnScrapTv = findViewById(R.id.turnScrapTv);
        deviceLL = findViewById(R.id.deviceLL);
        resubmit_ll = (LinearLayout) findViewById(R.id.common_docui_resubmit_ll);
        delete_ll = (LinearLayout) findViewById(R.id.common_docui_delete_ll);
        commondoc_sv = (CustomerScrollView) findViewById(R.id.common_docui_sv);
        hide_above_af = findViewById(R.id.hide_above_af);
        ex_invoice_ll = (LinearLayout) findViewById(R.id.ex_invoice_ll);
        grid_view = (MyGridView) findViewById(R.id.grid_view);

    }

    private void initDate() {
        final Intent intent = getIntent();
        mCaller = intent.getStringExtra("caller");
        mkeyValue = intent.getIntExtra("keyValue", -1);

        real_status = intent.getStringExtra("status");
        update = intent.getStringExtra("update");
        Log.i("mCaller", mCaller + "");
        Log.isLoggable("keyValue", mkeyValue);
        Log.i("real_status", real_status + "");

        if (mkeyValue == -1) {
            LogUtil.i("mkeyValue==-1");
            String keyValue = intent.getStringExtra("keyValue");
            LogUtil.i("keyValue=" + keyValue);
            if (!StringUtil.isEmpty(keyValue)) {
                try {
                    mkeyValue = Integer.valueOf(keyValue);
                    LogUtil.i("mkeyValue=" + mkeyValue);
                } catch (Exception e) {
                    LogUtil.i("e=" + e.getMessage());
                }
            }
        }

        //当获取到正确的单据caller和单据id时
        if (!TextUtils.isEmpty(mCaller) && mkeyValue != -1) {
            if ("Ask4Leave".equals(mCaller)) {
                mTitle = getString(R.string.oavacation_apply);
            }
            if ("FeePlease!CCSQ!new".equals(mCaller)) {
                mTitle = getString(R.string.oatravel_apply);
            }
            if ("Workovertime".equals(mCaller) || "ExtraWork$".equals(mCaller)) {
                mTitle = getString(R.string.oaovertime_apply);
            }
            if ("SpeAttendance".equals(mCaller)) {
                mTitle = getString(R.string.oaspecial_attendance_apply);
            }
            if ("MaterielApply".equals(mCaller)) {
                mTitle = getString(R.string.oamaterials_apply);
            } else if ("MainTain".equals(mCaller)) {
                mTitle = getString(R.string.oaservice_apply);
            }
            if ("StandbyApplication".equals(mCaller)) {
                mTitle = getString(R.string.oaStandby_machine_apply);
            } else if (!StringUtil.isEmpty(intent.getStringExtra("title"))) {
                mTitle = intent.getStringExtra("title");
            }
            if ("FeePlease!FYBX".equals(mCaller)) {
                mTitle = getString(R.string.Expense_reimbursement);
            }
            if (StringUtil.isEmpty(mTitle)){
                mTitle = intent.getStringExtra("title");
            }
            setTitle(mTitle + getString(R.string.doc_detail));
            //请求获取单据提交内容数据
            progressDialog.show();
            String docmsg_url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/common/getformandgriddata.action";
            Map<String, Object> param_am = new HashMap<>();
            param_am.put("caller", mCaller);
            param_am.put("id", mkeyValue);
            LinkedHashMap headers_am = new LinkedHashMap();
            headers_am.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
            ViewUtil.httpSendRequest(getApplicationContext(), docmsg_url, param_am, mHandler, headers_am, COMMON_DOC_MSG_REQUEST, null, null, "post");


        } else {
            appflow_lv.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.comdoc_list, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.more_doc){
//            startActivity(new Intent(mContext,FormListSelectActivity.class)
//                    .putExtra("caller", mCaller));
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!"FeePlease!FYBX".equals(mCaller) && !device&&!form_new_bill) {
            getMenuInflater().inflate(R.menu.menu_common_docdetails, menu);
            MenuItem item = menu.getItem(0);
            String title = null;
            if (getToolBarTitle() != null) {
                title = getToolBarTitle().toString();
            }
            if (real_status == null || !(real_status.equals("已提交") || real_status.equals("已审核"))
                    || title == null || !"出差单详情".equals(title)) {
                item.setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            startActivity(new Intent(activity, DataFormDetailActivity.class)
                    .putExtra("caller", mCaller).putExtra("title", mTitle)
            );
            finish();
        } else if (R.id.toTravel == item.getItemId()) {
            startActivity(new Intent(ct, BusinessTravelActivity.class));
            finish();
        }
        if (item.getItemId() == android.R.id.home) {
           if(back_normal){
               onBackPressed();
           } if (message) {
                endMessage();
            } else if (device) {
                endDevice();
            } else if (form_new_bill){
                endNewBill();
            }else {
                if (ActivityUtils.isExsitMianActivity(mContext, FormListSelectActivity.class)) {
                    LogUtil.d("正常返回...");
                    onBackPressed();
                } else {
                    LogUtil.d("非正常返回...");
                    startActivity(new Intent(mContext, FormListSelectActivity.class)
                            .putExtra("caller", mCaller)
                            .putExtra("statusKey", statusKey)
                            .putExtra("title", mTitle));
                    finish();
                }
            }
        }
        return true;
    }


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            switch (msg.what) {
                case SURE_DEVICE:

                    String deviceResult = msg.getData().getString("result");
                    boolean success = JSONUtil.getBoolean(deviceResult, "success");
                    if (success) {
                        //TODO 确定成功
                        ToastUtil.showToast(ct, "确认成功");
//                        finish();
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    break;
                case COMMON_DOC_MSG_REQUEST:
                    String msg_result = msg.getData().getString("result");
                    AMshow(msg_result); //单据申请信息展示
                    break;
                case COMMON_DOC_AF_REQUEST:
                    String af_result = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("af_result", af_result);
                    AFshow(af_result);  //单据审批流信息展示
                    break;
                case DELETE_DOC_REQUEST:
                    String del_result = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("del_result", del_result);
                    Toast.makeText(getApplicationContext(), getString(R.string.delete_all_succ), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    FormListSelectActivity.isdelete = true;
                    FormListSelectActivity.reload = false;
                    setResult(0x21);
                    finish();
                    break;
                case NEW_DELETE_DOC_REQUEST:
                    if (msg.getData() != null) {
                        String new_delete_doc_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("new_delete_doc_result", new_delete_doc_result);
                        Toast.makeText(getApplicationContext(), getString(R.string.delete_all_succ), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        FormListSelectActivity.isdelete = true;
                        FormListSelectActivity.reload = false;
                        setResult(0x21);
                        finish();
                    }
                    break;
                case COM_DOC_RESUB_SUCCESSFUL:  //已提交状态 重新提交操作前的反提交
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("RESUBMITTED_message", result);
//                            Toast.makeText(ct, "单据反提交成功", Toast.LENGTH_SHORT).show();
                            if (form_new_bill){
                                startActivity(new Intent(ct, CustomerBillInputActivity.class)
                                        .putExtra(Constants.Intents.CALLER, mCaller)
                                        .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                                        .putExtra(Constants.Intents.MY_DOIT, true)
                                        .putExtra(Constants.Intents.ID, mkeyValue));
                            }else{
                                startActivity(new Intent(CommonDocDetailsActivity.this, device ? DeviceDataFormAddActivity.class : DataFormDetailActivity.class)
                                        .putExtra("caller", mCaller)
                                        .putExtra("id", mkeyValue)
                                        .putExtra("title", mTitle)
                                        .putExtra("submittype", "resubmit")
                                        .putExtra("status", "已提交"));
                            }

                            finish();
                        }
                    }
                    break;
                case COM_DOC_REDELETE_SUCCESSFUL:  // 已提交状态 删除操作前的反提交
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String refordelete_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("refordelete", refordelete_result);
                            DeleteComDocRequest(mCaller, mkeyValue);
                        }
                    }
                    break;
                case NEW_DOC_RESUB_SUCCESSFUL:
                    if (msg.getData() != null) {
                        String new_doc_resub_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("new_doc_resub_result", new_doc_resub_result + "");
                        if (StringUtil.isEmpty(new_doc_resub_result)) return;
                        if (JSON.parseObject(new_doc_resub_result).containsKey("success")
                                && JSON.parseObject(new_doc_resub_result).getBoolean("success")) {
                            startActivity(new Intent(CommonDocDetailsActivity.this, DataFormDetailActivity.class)
                                    .putExtra("caller", mCaller)
                                    .putExtra("id", mkeyValue)
                                    .putExtra("title", mTitle)
                                    .putExtra("submittype", "resubmit")
                                    .putExtra("status", "已提交"));

                            finish();
                        }

                    }
                    break;

                case New_DOC_REDELETE_SUCCESSFUL:
                    if (msg.getData() != null) {
                        String new_doc_redelete_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("new_doc_redelete_result", new_doc_redelete_result);
                        if (StringUtil.isEmpty(new_doc_redelete_result)) return;
                        if (JSON.parseObject(new_doc_redelete_result).containsKey("success")
                                && JSON.parseObject(new_doc_redelete_result).getBoolean("success")) {
                            DeleteComDocRequest(mCaller, mkeyValue);
                        }
                    }
                    break;

                case 0x01:
                    if (msg.getData() != null) {
                        String result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("0x01result", result);
                        if (!StringUtil.isEmpty(result) && result.contains("success") &&
                                JSON.parseObject(result).getBoolean("success")) {
                            startActivity(new Intent(CommonDocDetailsActivity.this, ExpenseReimbursementActivity.class)
                                    .putExtra("caller", mCaller)
                                    .putExtra("id", mkeyValue)
                                    .putExtra("submittype", "resubmit")
                                    .putExtra("status", "已提交"));
                            finish();
                        }
                    }
                    break;
                case 0x02:
                    if (msg.getData() != null) {
                        String result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("0x02result", result);
                        DeleteComDocRequest(mCaller, mkeyValue);
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    Toast.makeText(CommonDocDetailsActivity.this, msg.getData().getString("result"), Toast.LENGTH_LONG).show();
                    if (!TextUtils.isEmpty(real_status) && real_status.equals("已审核")) {
                        commondoc_sv.setVisibility(View.VISIBLE);
                        resanddel_ll.setVisibility(View.GONE);
                    } else if (!TextUtils.isEmpty(real_status) && real_status.equals("已提交")) {
                        commondoc_sv.setVisibility(View.VISIBLE);
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    break;

                case COM_DOC_DEVICE_TURN_SACN://设备管理送检单转报废申请
                    JSONObject turnSacnObject = JSON.parseObject(message);
                    if (JSONUtil.getBoolean(turnSacnObject, "success")) {
                        int sacnId = JSONUtil.getInt(turnSacnObject, "data");
                        startActivity(new Intent(ct, CommonDocDetailsActivity.class)
                                .putExtra("caller", mCaller)
                                .putExtra("keyValue", sacnId)
                                .putExtra("device", true)
                                .putExtra("status", "在录入"));
                        finish();
                    }
                    break;
            }
        }
    };

    /**
     * @param mCaller
     * @param mkeyValue
     */
    private void DeleteComDocRequest(String mCaller, int mkeyValue) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("id", mkeyValue);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        String url = "";
        if ("ExtraWork$".equals(mCaller)) {
            url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/hr/attendance/deleteExtraWork.action";
            ViewUtil.httpSendRequest(this, url, params, mHandler, headers, NEW_DELETE_DOC_REQUEST, null, null, "post");
        } else {
            url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/commondelete.action";
            ViewUtil.httpSendRequest(this, url, params, mHandler, headers, DELETE_DOC_REQUEST, null, null, "get");
        }

    }

    /**
     * 处理请求申请单据url后获取到的数据并显示
     *
     * @param msg_result
     */
    public void AMshow(String msg_result) {
        mFormconfigsBean.clear();
        mGridconfigsBean.clear();
        try {
            JSONObject amresultJsonObject = JSON.parseObject(msg_result);
            JSONObject dataObjecty = amresultJsonObject.getJSONObject("datas");
            JSONArray formdataArray = dataObjecty.getJSONArray("formdata");
            showExP(formdataArray);
            JSONArray griddataArray = dataObjecty.getJSONArray("griddata");

            LogUtil.prinlnLongMsg("amresultJsonObject", amresultJsonObject.toString());
            if (formdataArray == null) {
                mEmptyLayput_applymsg.showEmpty();
                resanddel_ll.setVisibility(View.GONE);
            } else {
                commondoc_sv.setVisibility(View.VISIBLE);
                mCommonDocMainMsgAdapter.setFormdataBeans(formdataArray);
                if (griddataArray == null) {
                    secondmsg_ll.setVisibility(View.GONE);
                } else {
                    mComDocGriddataOutAdapter.setGriddataBeans(griddataArray);
                    mComDocGriddataOutAdapter.setCaller(mCaller);
                }
                mCommonDocAMBean = JSON.parseObject(amresultJsonObject.toString(), CommonDocAMBean.class);

                if (mCommonDocAMBean.getDatas().getFormdata() == null) {
                    mEmptyLayput_applymsg.showEmpty();
                } else {

                    //  获取需要显示的主表单配置数据属性保存到mFormconfigsBean中
                    if (mCommonDocAMBean.getDatas().getFormdata() != null && mCommonDocAMBean.getDatas().getFormconfigs() != null) {
                        int allFormconfigs_num = mCommonDocAMBean.getDatas().getFormconfigs().size();
                        type_emcode = new String();
                        for (int i = 0; i < allFormconfigs_num; i++) {
                            String fd_caption = mCommonDocAMBean.getDatas().getFormconfigs().get(i).getFD_CAPTION();
                            String fd_field = mCommonDocAMBean.getDatas().getFormconfigs().get(i).getFD_FIELD();
                            int mfd_isdefault = mCommonDocAMBean.getDatas().getFormconfigs().get(i).getMFD_ISDEFAULT();
                            if (mfd_isdefault == -1 && fd_caption != null && fd_field != null) {
                                mFormconfigsBean.add(mCommonDocAMBean.getDatas().getFormconfigs().get(i));
                            }

                            //申请单据申请人emcode，从申请单据信息获取
                            if ((!TextUtils.isEmpty(fd_field) && fd_field.equals("va_emcode")) ||
                                    (!TextUtils.isEmpty(fd_field) && fd_field.equals("wo_emcode")) ||
                                    (!TextUtils.isEmpty(fd_field) && fd_field.equals("sa_appmancode")) ||
                                    (!TextUtils.isEmpty(fd_field) && fd_field.equals("FP_PEOPLE2"))) {
                                if (!fd_field.equals("null")) {
                                    em_number = formdataArray.getJSONObject(0).getString(fd_field);
                                    Log.i("em_number", em_number + "");
                                }
                            }

                            //单据真正的状态
                            if ((!TextUtils.isEmpty(fd_field) && fd_field.equals("va_status")) ||
                                    (!TextUtils.isEmpty(fd_field) && fd_field.equals("wo_status")) ||
                                    (!TextUtils.isEmpty(fd_field) && fd_field.equals("sa_status")) ||
                                    (!TextUtils.isEmpty(fd_field) && fd_field.equals("FP_status"))) {
                                if (!fd_field.equals("null")) {
                                    last_status = formdataArray.getJSONObject(0).getString(fd_field);
                                }
                            }

                            SetComDocStatus(real_status); //显示单据提交状态
                            getapplypeomsg(em_number);
                        }
                        mCommonDocMainMsgAdapter.setmFormconfigsBean(mFormconfigsBean);
                        main_msg_lv.setAdapter(mCommonDocMainMsgAdapter);
                        LogUtil.prinlnLongMsg("mFormconfigsBean", mFormconfigsBean.toString());
                    }

                    //  获取需要显示的从表单配置数据属性保存到mGridconfigsBean中
                    if (mCommonDocAMBean.getDatas().getGriddata() != null && mCommonDocAMBean.getDatas().getGridconfigs() != null) {
                        int allGridconfigs_num = mCommonDocAMBean.getDatas().getGridconfigs().size();
                        for (int i = 0; i < allGridconfigs_num; i++) {
                            String dg_caption = mCommonDocAMBean.getDatas().getGridconfigs().get(i).getDG_CAPTION();
                            String dg_field = mCommonDocAMBean.getDatas().getGridconfigs().get(i).getDG_FIELD();
                            int mdg_isdefault = mCommonDocAMBean.getDatas().getGridconfigs().get(i).getMDG_ISDEFAULT();
                            if (mdg_isdefault == -1 && dg_caption != null && dg_field != null) {
                                mGridconfigsBean.add(mCommonDocAMBean.getDatas().getGridconfigs().get(i));
                            }
                        }
                        mComDocGriddataOutAdapter.setmGridconfigsBean(mGridconfigsBean);
                        second_msg_lv.setAdapter(mComDocGriddataOutAdapter);
                        LogUtil.prinlnLongMsg("mGridconfigsBean", mGridconfigsBean.toString());
                    }
                }
            }
        } catch (JSONException e) {
            if (e != null)
                LogUtil.i("JSONException =" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            if (e != null)
                LogUtil.i("Exception =" + e.getMessage());
            e.printStackTrace();
        }

        //请求获取单据审批流数据
        String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "common/getCurrentJnodes.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", mCaller);
        param.put("keyValue", mkeyValue);
        param.put("_noc", mNoc);
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, mHandler, headers, COMMON_DOC_AF_REQUEST, null, null, "post");

    }

    private void sureDevice() {
        progressDialog.show();
        //请求获取单据审批流数据
        String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/device/confirmDeal.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", mCaller);
        param.put("id", mkeyValue);
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, mHandler, headers, SURE_DEVICE, null, null, "post");

    }

    /**
     * 申请单据的审批，提交，录入状态
     */
    private void SetComDocStatus(String real_status) {

        if (!TextUtils.isEmpty(real_status)) {
            if (real_status.equals("已审核")) {
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.approval));
                status_tv.setText(getString(R.string.status_approved));
                resanddel_ll.setVisibility(View.GONE);
            } else if (real_status.equals("已提交")) {

                if (!TextUtils.isEmpty(last_status) && last_status.equals("已审核")) {
                    status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.approval));
                    status_tv.setText(getString(R.string.status_approved));
                    resanddel_ll.setVisibility(View.GONE);
                } else {
                    status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.no_approval));
                    status_tv.setText(getString(R.string.status_pending));
                    resanddel_ll.setVisibility(View.VISIBLE);
                }

            } else if (real_status.equals("在录入")) {
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.titleBlue));
                status_tv.setText(getString(R.string.status_unsubmit));
                resanddel_ll.setVisibility(View.VISIBLE);
            }
        }
        if (device) {
            LogUtil.i("real_status=" + real_status);
            //只有故障单有按钮
            if (real_status.equals("已审核") && !StringUtil.isEmpty(mCaller) && mCaller.equals("DeviceChange!Inspect")) {
                resanddel_ll.setVisibility(View.GONE);
                deviceLL.setVisibility(View.VISIBLE);
                sureTv.setOnClickListener(mOnClickListener);
                turnScrapTv.setVisibility(View.VISIBLE);
                turnScrapTv.setOnClickListener(mOnClickListener);
            }

        }
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.sureTv) {
                sureDevice();
            } else if (id == R.id.turnScrapTv) {
                deviceTurnScrap();
            }
        }
    };


    /**
     * 处理请求审批流url后获取到的数据并显示
     *
     * @param af_result
     */
    private String em_code;
    private List<String> afpeople_names;
    private String af_name;

    public void AFshow(String af_result) {
        try {
            JSONObject resultJsonObject = JSON.parseObject(af_result);
            //JSONArray dailynodeArray = resultJsonObject.getJSONArray("nodes");
            if (resultJsonObject == null) {
                appflow_lv.setVisibility(View.GONE);
            } else {
                mCommonApprovalFlowBean = JSON.parseObject(resultJsonObject.toString(), CommonApprovalFlowBean.class);
                em_code = new String();
                if (manager == null) manager = new DBManager(mContext);
                if ("已审核".equals(real_status) && (mCommonApprovalFlowBean.getData().size() == 0 ||
                        mCommonApprovalFlowBean.getData().size() == mCommonApprovalFlowBean.getNodes().size())) {  // TODO 已审核直接从node里面取数据
                    for (int i = 0; i < mCommonApprovalFlowBean.getNodes().size(); i++) {
                        //取名字
                        if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManName())) {
                            afpeople_names.add(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManName());
                        } else {
                            afpeople_names.add("");
                        }
                        //取头像id
                        if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManId())) {
                            em_code = mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManId();
                            if (em_code.contains(",")) {
                                String str[] = em_code.split(",");
                                em_code = str[0];
//                                ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                            }
                        } else {
                            em_code = " ";
                        }
                        try {
                            String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
                            String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                            String selection = "em_code=? and whichsys=? ";
                            //获取数据库数据
                            EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                            if (bean != null) {
                                String imId = String.valueOf(bean.getEm_IMID());
                                Log.i("todo", "imId=" + imId);
                                im_ids.add(imId);
                            } else {
                                im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if ("已提交".equals(real_status)) {
                    String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
                    if (!ListUtils.isEmpty(afpeople_names))
                        afpeople_names.clear();
                    if (!ListUtils.isEmpty(im_ids))
                        im_ids.clear();
                    // TODO 已提交状态判断是否有变更处理人，所以得先去process中判断,好麻烦噢
                    if (!ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {
                        int processnum = mCommonApprovalFlowBean.getProcesss().size();
                        //取process数据
                        for (int i = 0; i < processnum; i++) {
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealMan())) {
                                em_code = mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealMan();
                                if (em_code.contains(",")) {
                                    String str[] = em_code.split(",");
                                    em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                                }
                                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                                String selection = "em_code=? and whichsys=? ";
                                try {
                                    EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                                    if (bean != null) {
                                        //获取数据库数据获得名字
                                        String imName = String.valueOf(bean.getEM_NAME());
                                        if (!StringUtil.isEmpty(imName)) {
                                            af_name = imName;
                                        }
                                        //从数据库数据获得imid
                                        String imId = String.valueOf(bean.getEm_IMID());
                                        Log.i("todo", "imId=" + imId);
                                        im_ids.add(imId);
                                    } else {
                                        im_ids.add("");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                im_ids.add("");
                            }

                            //获取process审批人姓名
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealManName())) {
                                afpeople_names.add(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealManName());
                            } else if (!TextUtils.isEmpty(af_name)) {
                                afpeople_names.add(af_name);
                            } else {
                                afpeople_names.add("");
                            }
                        }

                        //取data数据
                        for (int j = processnum; j < mCommonApprovalFlowBean.getData().size(); j++) {
                            //取process之后的审批人名字
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMANNAME())) {
                                afpeople_names.add(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMANNAME());
                            } else {
                                afpeople_names.add("");
                            }
                            //取process之后的imid
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMAN())) {
                                em_code = mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMAN();
                                if (em_code.contains(",")) {
                                    String str[] = em_code.split(",");
                                    em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                                }
                                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                                String selection = "em_code=? and whichsys=? ";
                                try {
                                    //获取数据库数据
                                    EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                                    if (bean != null) {
                                        String imId = String.valueOf(bean.getEm_IMID());
                                        Log.i("todo", "imId=" + imId);
                                        im_ids.add(imId);
                                    } else {
                                        im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                im_ids.add("");
                            }
                        }
                    }

                    // TODO 标准版刚刚提交时无变更时，全部取data数据
                    if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {
                        for (int i = 0; i < mCommonApprovalFlowBean.getData().size(); i++) {
                            //取名字
                            if (manager == null) manager = new DBManager(mContext);
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMANNAME())) {
                                afpeople_names.add(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMANNAME());
                            } else {
                                afpeople_names.add("");
                            }
                            //取头像id
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMAN())) {
                                em_code = mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMAN();
                                if (em_code.contains(",")) {
                                    String str[] = em_code.split(",");
                                    em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                                }
                            } else {
                                em_code = " ";
                            }
                            try {
                                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                                String selection = "em_code=? and whichsys=? ";
                                //获取数据库数据
                                EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                                if (bean != null) {
                                    String imId = String.valueOf(bean.getEm_IMID());
                                    Log.i("todo", "imId=" + imId);
                                    im_ids.add(imId);
                                } else {
                                    im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                } else if ("已审核".equals(real_status) && mCommonApprovalFlowBean.getData().size() != 0
                        && mCommonApprovalFlowBean.getData().size() != mCommonApprovalFlowBean.getNodes().size()) {
                    appflow_lv.setVisibility(View.GONE);
                }
                Log.i("last_afpeople_names", afpeople_names.toString());
                Log.i("last_imids", im_ids.toString());
//                mCommonDocApprovalFlowAdapter.setIm_ids(im_ids);
//                mCommonDocApprovalFlowAdapter.setAfpeople_names(afpeople_names);
                // 设置数据之前先通过数据库获取到所有的imid保存到内存中
//                mCommonDocApprovalFlowAdapter.setmCommonApprovalFlowBean(mCommonApprovalFlowBean);
//                appflow_lv.setAdapter(mCommonDocApprovalFlowAdapter);

                if (ListUtils.isEmpty(mCommonApprovalFlowBean.getData()) && ListUtils.isEmpty(mCommonApprovalFlowBean.getNodes())) {
                    hide_above_af.setVisibility(View.GONE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        handlerNode(af_result);
        getemcodefromAF();
    }

    private void handlerNode(String message) {
        List<Approval> approvals = ApprovaNodeUtil.handlerNode(manager, message);
        if (!ListUtils.isEmpty(approvals)) {
            NodeAdapter nodeAdapter = new NodeAdapter(this, approvals);
            appflow_lv.setAdapter(nodeAdapter);
        } else {
            //TODO 为空时候
        }
        progressDialog.dismiss();
    }

    /**
     * 申请单据申请人emcode,从审批流数据获取
     */
    public void getemcodefromAF() {

        //  当单据是提交状态 触发审批流时获取
        if (!ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss())) {
            if (TextUtils.isEmpty(em_number)) {
                em_number = mCommonApprovalFlowBean.getProcesss().get(0).getJp_launcherId();   //申请人编号 从process获取
//                name_tv.setText(mCommonApprovalFlowBean.getProcesss().get(0).getJp_launcherName());  //申请人姓名 从process获取
                getapplypeomsg(em_number);
            }
        }

        // 当单据未提交 未触发审批流时，默认静态显示申请人的信息
        if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && TextUtils.isEmpty(em_number)) {
            String en_code = CommonUtil.getSharedPreferences(ct, "erp_username");
            manager = new DBManager(ct);
            try {
                List<EmployeesEntity> db = manager.select_getEmployee(
                        new String[]{CommonUtil.getSharedPreferences(ct, "erp_master"),
                                CommonUtil.getSharedPreferences(ct, "erp_username")}
                        , "whichsys=? and em_code=? ");

                if (!ListUtils.isEmpty(db)) {
                    section_tv.setText(db.get(0).getEM_DEPART() + ">" + db.get(0).getEM_POSITION());
                    AvatarHelper.getInstance().displayAvatar(String.valueOf(db.get(0).getEm_IMID()), photo_im, true);
                    if (!StringUtil.isEmpty(db.get(0).getEM_NAME())) {
                        name_tv.setText(db.get(0).getEM_NAME());
                    } else {
                        name_tv.setText(MyApplication.getInstance().mLoginUser.getNickName());
                    }
                } else {
                    String em_name = CommonUtil.getName();
                    name_tv.setText(em_name);
                    String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                    AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String em_name = CommonUtil.getName();
            name_tv.setText(em_name);
            String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
            AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
        }

        // 界面下面的按钮操作
        if ((!ListUtils.isEmpty(mCommonApprovalFlowBean.getNodes()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData()) &&
                mCommonApprovalFlowBean.getData().size() == mCommonApprovalFlowBean.getNodes().size())
                && mCommonApprovalFlowBean.getNodes().get(mCommonApprovalFlowBean.getNodes().size() - 1).getJn_dealResult().equals("同意")) {
            resanddel_ll.setVisibility(View.GONE);
            approval_ll.setVisibility(View.GONE);
        } else {
            approval_ll.setVisibility(View.GONE);
            resubmit_ll.setOnClickListener(new View.OnClickListener() { // 重新提交
                @Override
                public void onClick(View v) {
                    if ((device||form_new_bill) && StringUtil.getText(status_tv).equals(getString(R.string.status_pending))) {
                        reSubmit();
                    } else if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss())
                            && ListUtils.isEmpty(mCommonApprovalFlowBean.getNodes())
                            && ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {  //在录入状态不反提交
                        startActivity(new Intent(CommonDocDetailsActivity.this, device ? DeviceDataFormAddActivity.class : DataFormDetailActivity.class)
                                .putExtra("caller", mCaller)
                                .putExtra("id", mkeyValue)
                                .putExtra("title", mTitle)
                                .putExtra("submittype", "dosubmit"));
                        setResult(0x21);
                        finish();

                    } else {  //已提交状态可进行反提交
                        reSubmit();
                    }

                }
            });
        }
        // 删除
        delete_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(real_status) && real_status.equals("在录入")) {
                    PopupWindowHelper.showAlart(CommonDocDetailsActivity.this,
                            getString(R.string.common_notice), getString(R.string.delete_doc_notice2),
                            new PopupWindowHelper.OnSelectListener() {
                                @Override
                                public void select(boolean selectOk) {
                                    if (selectOk) {
                                        DeleteComDocRequest(mCaller, mkeyValue);
                                    }
                                }
                            });
                } else if (!TextUtils.isEmpty(real_status) && real_status.equals("已提交")) {
                    PopupWindowHelper.showAlart(CommonDocDetailsActivity.this,
                            getString(R.string.common_notice), getString(R.string.delete_sumited_notice1),
                            new PopupWindowHelper.OnSelectListener() {
                                @Override
                                public void select(boolean selectOk) {
                                    if (selectOk) {
                                        if (device && "DeviceChange!Inspect".equals(mCaller)) {
                                            ToastUtil.showToast(ct, "送检单不允许删除");
                                        } else {
                                            progressDialog.show();
                                            Map<String, Object> params = new HashMap<>();
                                            params.put("caller", mCaller);
                                            params.put("id", mkeyValue);
                                            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                                            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                                            String url = "";
                                            if ("ExtraWork$".equals(mCaller)) { // 新的加班单反提交
                                                url = CommonUtil.getAppBaseUrl(ct) + "/hr/attendance/resSubmitExtraWork.action";
                                                ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, New_DOC_REDELETE_SUCCESSFUL, null, null, "post");
                                            } else if ("FeePlease!FYBX".equals(mCaller)) {
                                                url = CommonUtil.getAppBaseUrl(ct) + "oa/fee/resSubmitFeePlease.action";
                                                ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x02, null, null, "post");
                                            } else { // 通用老的反提交s
                                                url = CommonUtil.getAppBaseUrl(ct) + "/mobile/commonres.action";
                                                ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, COM_DOC_REDELETE_SUCCESSFUL, null, null, "post");
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void reSubmit() {
        PopupWindowHelper.showAlart(CommonDocDetailsActivity.this,
                getString(R.string.common_notice), getString(R.string.daily_resubmit_notice1),
                new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            reSubmitNet();
                        }
                    }
                });
    }

    private void reSubmitNet() {
        Map<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("id", mkeyValue);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        String url = "";
        if ("ExtraWork$".equals(mCaller)) { // 新的加班单反提交
            url = CommonUtil.getAppBaseUrl(ct) + "/hr/attendance/resSubmitExtraWork.action";
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, NEW_DOC_RESUB_SUCCESSFUL, null, null, "post");
        } else if ("FeePlease!FYBX".equals(mCaller)) { // 报销单
            url = CommonUtil.getAppBaseUrl(ct) + "oa/fee/resSubmitFeePlease.action";
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x01, null, null, "post");
        } else if ("DeviceChange!Inspect".equals(mCaller)) {
            url = CommonUtil.getAppBaseUrl(ct) + "/mobile/device/deviceInspectRes.action";
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, COM_DOC_RESUB_SUCCESSFUL, null, null, "post");
        } else {// 通用老的反提交
            url = CommonUtil.getAppBaseUrl(ct) + "/mobile/commonres.action";
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, COM_DOC_RESUB_SUCCESSFUL, null, null, "post");
        }
    }

    private void deviceTurnScrap() {
        progressDialog.show();
        Map<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("id", mkeyValue);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/device/turnScrap.action";
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, COM_DOC_DEVICE_TURN_SACN, null, null, "post");
    }

    private void getapplypeomsg(String em_number) {
        if (TextUtils.isEmpty(em_number)) {
            String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
            String[] selectionArgs = {em_number == null ? "" : em_number, whichsys};
            String selection = "em_code=? and whichsys=? ";
            try {
                //获取数据库数据
                EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                if (bean != null) {
                    String imId = String.valueOf(bean.getEm_IMID());
                    String imName = String.valueOf(bean.getEM_NAME());
                    String imDepartment = String.valueOf(bean.getEM_DEPART());
                    String imPosition = String.valueOf(bean.getEM_POSITION());

                    name_tv.setText(imName);
                    if (!StringUtil.isEmpty(imId)) {
                        AvatarHelper.getInstance().display(imId, photo_im, true, false);
                    } else {
                        String imageUri = "drawable://" + R.drawable.common_header_boy;
                        AvatarHelper.getInstance().display(imageUri, photo_im, true);
                    }//显示圆角图片
                    section_tv.setText(imDepartment + ">" + imPosition);

                    Log.i("aptodo", "imId=" + imId + "imName" + imName + "imDepartment" + imDepartment + "imPosition" + imPosition);
                } else {
                    name_tv.setText(CommonUtil.getName());
                    String imId = MyApplication.getInstance().getLoginUserId();
                    if (!StringUtil.isEmpty(imId)) {
                        AvatarHelper.getInstance().display(imId, photo_im, true, false);
                    }
//                    ToastMessage("单据申请人数据获取异常");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        progressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.closeDB();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (message) {
                endMessage();
            } else if (device) {
                endDevice();
            } else if (form_new_bill) {
                endNewBill();
            } else {
                if (ActivityUtils.isExsitMianActivity(mContext, FormListSelectActivity.class)) {
                    LogUtil.d("正常返回...");
                    if ("1".equals(update)) {
                        startActivity(new Intent(mContext, FormListSelectActivity.class)
                                .putExtra("caller", mCaller)
                                .putExtra("update", update)
                                .putExtra("statusKey", statusKey)
                                .putExtra("title", mTitle));
                        finish();
                    } else {
                        onBackPressed();
                    }
                } else {
                    LogUtil.d("非正常返回..." + update);
                    if ("1".equals(update)) {
                        FormListSelectActivity.reload = true;
                    } else {
                        FormListSelectActivity.reload = false;
                    }
                    startActivity(
                            new Intent(mContext, FormListSelectActivity.class)
                                    .putExtra("caller", mCaller)
                                    .putExtra("statusKey", statusKey)
                                    .putExtra("title", mTitle));
                    finish();
                }
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void endMessage() {
        onBackPressed();
    }

    private void endNewBill() {
        startActivity(new Intent(ct, CustomerListActivity.class)
                .putExtra(Constants.Intents.CALLER, mCaller)
                .putExtra(Constants.Intents.TITLE, getToolBarTitle()));
    }
    private void endDevice() {
        String dc_class = null;
        if (mCaller.equals("DeviceChange!Use")) {
            dc_class = "使用转移";
        } else if (mCaller.equals("DeviceChange!Scrap")) {
            dc_class = "报废申请";
        } else if (mCaller.equals("DeviceChange!Maintain")) {
            dc_class = "保养维护";
        } else if (mCaller.equals("DeviceChange!Inspect")) {
            dc_class = "故障送检";
        }
        if (ActivityUtils.isExsitMianActivity(mContext, DeviceQueryActivity.class)) {
            onBackPressed();
        } else {
            startActivity(new Intent(ct, DeviceQueryActivity.class).putExtra(Constants.FLAG.DEVICE_CALLER, mCaller)
                    .putExtra(Constants.FLAG.DEVICE_CLASS, dc_class)
                    .putExtra(Constants.FLAG.DEVICE_WHICH_PAGE, Constants.FLAG.DEVICE_FROM_COMMON)
            );
            setResult(0x21);
            finish();
        }
    }

    /**
     * 图片适配器
     */
    private class GridViewAdapter extends BaseAdapter {
        private String fb_attachs[];

        public String[] getFb_attachs() {
            return fb_attachs;
        }

        public void setFb_attachs(String[] fb_attachs) {
            this.fb_attachs = fb_attachs;
        }

        @Override
        public int getCount() {
            return getFb_attachs() == null ? 0 : getFb_attachs().length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SquareCenterImageView imageView = new SquareCenterImageView(ct);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int id = CommonUtil.getNumByString(getFb_attachs()[position]);
            ImageLoader.getInstance().displayImage(getImageUrl(id), imageView);
            return imageView;
        }

    }

    private String fb_attachs[] = {};

    /**
     * 获取报销单的发票并显示
     *
     * @param formdataArray
     */
    private void showExP(JSONArray formdataArray) {
        if (formdataArray != null && "FeePlease!FYBX".equals(mCaller)) {
            try {
                String fb_attach = formdataArray.getJSONObject(0).getString("fb_attach");
                Log.i("fb_attachshow", fb_attach + "show");
                if (fb_attach == null || "null".equals(fb_attach) || fb_attach.length() <= 0) {
                    ex_invoice_ll.setVisibility(View.GONE);
                } else {
                    fb_attachs = fb_attach.split(";");
                    mAdapter.setFb_attachs(fb_attachs);
                    grid_view.deferNotifyDataSetChanged();
                    ex_invoice_ll.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return;
        }

        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int mPosition = (int) parent.getItemIdAtPosition(position);
                Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI,
                        getImageUrl(CommonUtil.getNumByString(fb_attachs[position])));
                startActivity(intent);
                Log.i("P_position", mPosition + "");
            }
        });
    }

    private String getImageUrl(int id) {
        return CommonUtil.getAppBaseUrl(ct) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                CommonUtil.getSharedPreferences(ct, "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(ct, "erp_username") +
                "&master=" + CommonUtil.getSharedPreferences(ct, "erp_master");
    }
}
