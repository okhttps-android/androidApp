package com.uas.appworks.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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
import com.core.model.EmployeesEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerScrollView;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.core.widget.SquareCenterImageView;
import com.core.widget.view.MyGridView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appworks.OA.erp.activity.ExpenseReimbursementActivity;
import com.uas.appworks.OA.erp.activity.form.DataFormDetailActivity;
import com.uas.appworks.OA.erp.adapter.ComDocGriddataOutAdapter;
import com.uas.appworks.OA.erp.adapter.CommonDocMainMsgAdapter;
import com.uas.appworks.OA.erp.model.CommonApprovalFlowBean;
import com.uas.appworks.OA.erp.model.CommonDocAMBean;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CityServiceProcessAdapter;
import com.uas.appworks.adapter.EnclosureItemAdapter;
import com.uas.appworks.model.bean.CityIndustryEnclosureBean;
import com.uas.appworks.model.bean.CityServiceProcessBean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 产城服务通用表单详情页
 */
public class CommonCityIndustryDetailsActivity extends BaseActivity {

    private static final int COM_DOC_RESUB_SUCCESSFUL = 1213;
    private static final int COM_DOC_REDELETE_SUCCESSFUL = 1215;
    private static final int NEW_DOC_RESUB_SUCCESSFUL = 52601;
    private static final int New_DOC_REDELETE_SUCCESSFUL = 52602;
    private static final int NEW_DELETE_DOC_REQUEST = 52603;
    private int mNoc = 1;
    private final static int COMMON_DOC_AF_REQUEST = 1125;
    private final static int COMMON_DOC_MSG_REQUEST = 1126;
    private static final int DELETE_DOC_REQUEST = 1127;
    private final int GET_CAPTION_PATH = 0x11;

    private EmptyLayout mEmptyLayput_approvalflow;
    private CommonApprovalFlowBean mCommonApprovalFlowBean;
    private CommonDocAMBean mCommonDocAMBean;
    private DBManager manager;
    private List<String> im_ids;
    private String mCaller = "";
    private int mkeyValue;
    private String mServiceId;
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
    private LinearLayout resubmit_ll;
    private LinearLayout delete_ll;
    private LinearLayout mFilesLinearLayout;
    private MyListView mFilesListView;
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
    private List<CityIndustryEnclosureBean> mCityIndustryEnclosureBeans;
    private EnclosureItemAdapter mEnclosureItemAdapter;
    private List<CityServiceProcessBean> mCityServiceProcessBeans;
    private CityServiceProcessAdapter mCityServiceProcessAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initDate();
    }

    @Override
    protected void onResume() {
//        commondoc_sv.smoothScrollTo(0, 0);
        super.onResume();
    }

    public void initView() {
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        setContentView(R.layout.activity_common_docui);
        initIDS();
        mEmptyLayput_applymsg = new EmptyLayout(this, main_msg_lv);
        mEmptyLayput_applymsg.setShowEmptyButton(false);
        mEmptyLayput_applymsg.setShowErrorButton(false);
        mEmptyLayput_applymsg.setShowLoadingButton(false);

        mCityIndustryEnclosureBeans = new ArrayList<>();
        mEnclosureItemAdapter = new EnclosureItemAdapter(this, mCityIndustryEnclosureBeans);
        mFilesListView.setAdapter(mEnclosureItemAdapter);
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

        mEnclosureItemAdapter.setOnEnclosureItemClickListener(new EnclosureItemAdapter.OnEnclosureItemClickListener() {
            @Override
            public void onEnclosureClick(View view, int position) {
                String url = "";
                try {
                    url = CommonUtil.getCityBaseUrl(mContext) + "api/serve/download.action"
                            + "?path=" + URLEncoder.encode(mCityIndustryEnclosureBeans.get(position).getFp_path(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, url);
                intent.putExtra(AppConstant.EXTRA_IMAGE_SESSION, CommonUtil.getB2BUid(mContext));
                mContext.startActivity(intent);
            }
        });
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
        resubmit_ll = (LinearLayout) findViewById(R.id.common_docui_resubmit_ll);
        delete_ll = (LinearLayout) findViewById(R.id.common_docui_delete_ll);
        commondoc_sv = (CustomerScrollView) findViewById(R.id.common_docui_sv);
        hide_above_af = findViewById(R.id.hide_above_af);
        ex_invoice_ll = (LinearLayout) findViewById(R.id.ex_invoice_ll);
        grid_view = (MyGridView) findViewById(R.id.grid_view);
        mFilesLinearLayout = (LinearLayout) findViewById(R.id.common_docui_files_ll);
        mFilesListView = (MyListView) findViewById(R.id.common_docui_files_lv);
    }

    private void initDate() {
        final Intent intent = getIntent();
        mkeyValue = intent.getIntExtra("keyValue", -1);
        real_status = intent.getStringExtra("status");
        update = intent.getStringExtra("update");
        mServiceId = intent.getStringExtra("serve_id");
        mTitle = intent.getStringExtra("title");
        if (!StringUtil.isEmpty(mTitle)) {
           setTitle(mTitle + getString(R.string.doc_detail));
        }
        //请求获取单据提交内容数据
        progressDialog.show();
        String docmsg_url = CommonUtil.getCityBaseUrl(getApplicationContext()) + "api/serve/config.action";
        Map<String, Object> param_am = new HashMap<>();
        param_am.put("serve_id", mServiceId);
        param_am.put("id", mkeyValue);
        LinkedHashMap headers_am = new LinkedHashMap();
        headers_am.put("Cookie", CommonUtil.getB2BUid(this));
        ViewUtil.httpSendRequest(getApplicationContext(), docmsg_url, param_am, mHandler, headers_am, COMMON_DOC_MSG_REQUEST, null, null, "get");

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
        if (!"FeePlease!FYBX".equals(mCaller)) {
            getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            startActivity(new Intent(activity, CommonDataFormActivity.class)
                    .putExtra("serve_id", mServiceId)
                    .putExtra("title", mTitle));
            finish();
        }
        if (item.getItemId() == android.R.id.home) {
            if (ActivityUtils.isExsitMianActivity(mContext, CommonFormListActivity.class)) {
                LogUtil.d("正常返回...");
                onBackPressed();
            } else {
                LogUtil.d("非正常返回...");
                startActivity(new Intent(mContext, CommonFormListActivity.class)
                        .putExtra("serveId", mServiceId)
                        .putExtra("caller", mCaller)
                        .putExtra("title", mTitle));
                finish();
            }
        }
        return true;
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                    Toast.makeText(getApplicationContext(), getString(R.string.common_delete), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    CommonFormListActivity.isdelete = true;
                    CommonFormListActivity.reload = false;
                    finish();
                    break;
                case NEW_DELETE_DOC_REQUEST:
                    if (msg.getData() != null) {
                        String new_delete_doc_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("new_delete_doc_result", new_delete_doc_result);
                        Toast.makeText(getApplicationContext(), getString(R.string.common_delete), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        CommonFormListActivity.isdelete = true;
                        CommonFormListActivity.reload = false;
                        finish();
                    }
                    break;
                case COM_DOC_RESUB_SUCCESSFUL:  //已提交状态 重新提交操作前的反提交
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("RESUBMITTED_message", result);
//                            Toast.makeText(ct, "单据反提交成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CommonCityIndustryDetailsActivity.this, DataFormDetailActivity.class)
                                    .putExtra("caller", mCaller)
                                    .putExtra("id", mkeyValue)
                                    .putExtra("submittype", "resubmit")
                                    .putExtra("status", "已提交"));

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
                            startActivity(new Intent(CommonCityIndustryDetailsActivity.this, DataFormDetailActivity.class)
                                    .putExtra("caller", mCaller)
                                    .putExtra("id", mkeyValue)
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
                            startActivity(new Intent(CommonCityIndustryDetailsActivity.this, ExpenseReimbursementActivity.class)
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
                case GET_CAPTION_PATH:
                    String result = msg.getData().getString("result");
                    Log.d("captionPath", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray filesArray = resultObject.getJSONArray("files");
                    if (filesArray != null) {
                        for (int i = 0; i < filesArray.size(); i++) {
                            CityIndustryEnclosureBean cityIndustryEnclosureBean = new CityIndustryEnclosureBean();
                            JSONObject fileObject = filesArray.getJSONObject(i);
                            int fp_id = JSONUtil.getInt(fileObject, "fp_id");
                            String fp_path = JSONUtil.getText(fileObject, "fp_path");
                            int fp_size = JSONUtil.getInt(fileObject, "fp_size");
                            String fp_name = JSONUtil.getText(fileObject, "fp_name");
                            String fp_date = JSONUtil.getText(fileObject, "fp_date");
                            String fp_man = JSONUtil.getText(fileObject, "fp_man");

                            cityIndustryEnclosureBean.setFp_id(fp_id);
                            cityIndustryEnclosureBean.setFp_path(fp_path);
                            cityIndustryEnclosureBean.setFp_size(fp_size);
                            cityIndustryEnclosureBean.setFp_name(fp_name);
                            cityIndustryEnclosureBean.setFp_date(fp_date);
                            cityIndustryEnclosureBean.setFp_man(fp_man);

                            mCityIndustryEnclosureBeans.add(cityIndustryEnclosureBean);
                        }
                        mFilesLinearLayout.setVisibility(View.VISIBLE);
                        mEnclosureItemAdapter.notifyDataSetChanged();
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Toast.makeText(CommonCityIndustryDetailsActivity.this, msg.getData().getString("result"), Toast.LENGTH_LONG).show();
                    if (!TextUtils.isEmpty(real_status) && real_status.equals("已审核")) {
                        commondoc_sv.setVisibility(View.VISIBLE);
                        resanddel_ll.setVisibility(View.GONE);
                    } else if (!TextUtils.isEmpty(real_status) && real_status.equals("已提交")) {
                        commondoc_sv.setVisibility(View.VISIBLE);
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
        Map<String, Object> formStoreMap = new HashMap<>();
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
        Log.d("cityindustrydetail", "result->" + msg_result);
        try {
            JSONObject amresultJsonObject = JSON.parseObject(msg_result);
            JSONObject dataObjecty = amresultJsonObject.getJSONObject("datas");
            JSONArray formdataArray = dataObjecty.getJSONArray("formdata");
            showExP(formdataArray);
            JSONArray griddataArray = dataObjecty.getJSONArray("griddata");
            JSONArray formconfigsArray = dataObjecty.getJSONArray("formconfigs");

            if (formconfigsArray != null) {
                boolean filesExist = false;
                String fd_field = null;
                for (int i = 0; i < formconfigsArray.size(); i++) {
                    JSONObject formconfigsObject = formconfigsArray.getJSONObject(i);
                    String fd_caption = JSONUtil.getText(formconfigsObject, "FD_CAPTION");
                    if ("附件".equals(fd_caption)) {
                        fd_field = JSONUtil.getText(formconfigsObject, "FD_FIELD");
                        filesExist = true;
                        break;
                    }
                }

                if (filesExist) {
                    String caption_id = JSONUtil.getText(formdataArray.getJSONObject(0), fd_field);
                    if (!TextUtils.isEmpty(caption_id)) {
                        progressDialog.show();
                        String docmsg_url = CommonUtil.getCityBaseUrl(getApplicationContext()) + "api/serve/getFilePaths.action";
                        Map<String, Object> param_am = new HashMap<>();
                        param_am.put("field", "fb_attach");
                        param_am.put("id", caption_id);
                        LinkedHashMap headers_am = new LinkedHashMap();
                        headers_am.put("Cookie", CommonUtil.getB2BUid(this));
                        ViewUtil.httpSendRequest(getApplicationContext(), docmsg_url, param_am, mHandler, headers_am, GET_CAPTION_PATH, null, null, "get");
                    } else {
                        mFilesLinearLayout.setVisibility(View.GONE);
                    }
                } else {
                    mFilesLinearLayout.setVisibility(View.GONE);
                }
            }

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
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //请求获取单据审批流数据
        String url = CommonUtil.getCityBaseUrl(getApplicationContext()) + "api/serve/getProcesses.action";
        Map<String, Object> param = new HashMap<>();
        param.put("serve_id", mServiceId);
        param.put("id", mkeyValue);
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", CommonUtil.getB2BUid(mContext));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, mHandler, headers, COMMON_DOC_AF_REQUEST, null, null, "post");

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
                    resanddel_ll.setVisibility(View.GONE);
                }

            } else if (real_status.equals("在录入")) {
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.titleBlue));
                status_tv.setText(getString(R.string.status_unsubmit));
                resanddel_ll.setVisibility(View.GONE);
            }
        }
    }

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
            if (af_result == null || !JSONUtil.validate(af_result)) {
                appflow_lv.setVisibility(View.GONE);
                return;
            }
            JSONObject resultJsonObject = JSON.parseObject(af_result);
            if (resultJsonObject == null) {
                appflow_lv.setVisibility(View.GONE);
            } else {
                handlerNode(af_result);
            }
            getemcodefromAF();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handlerNode(String message) {
        mCityServiceProcessBeans = new ArrayList<>();
        JSONObject resultObject = JSON.parseObject(message);
        JSONArray processArray = resultObject.getJSONArray("process");
        if (processArray != null) {
            for (int i = processArray.size() - 1; i >= 0; i--) {
                JSONObject processObject = processArray.getJSONObject(i);
                CityServiceProcessBean cityServiceProcessBean = new CityServiceProcessBean();
                cityServiceProcessBean.setStatus(JSONUtil.getText(processObject, "status"));
                cityServiceProcessBean.setTime(JSONUtil.getText(processObject, "time"));
                mCityServiceProcessBeans.add(cityServiceProcessBean);
            }
            mCityServiceProcessAdapter = new CityServiceProcessAdapter(this, mCityServiceProcessBeans);
            appflow_lv.setAdapter(mCityServiceProcessAdapter);
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
                    String em_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
                    name_tv.setText(em_name);
                    String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                    AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

                    if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss())
                            && ListUtils.isEmpty(mCommonApprovalFlowBean.getNodes())
                            && ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {  //在录入状态不反提交

                        startActivity(new Intent(CommonCityIndustryDetailsActivity.this, DataFormDetailActivity.class)
                                .putExtra("caller", mCaller)
                                .putExtra("id", mkeyValue)
                                .putExtra("submittype", "dosubmit"));


                    } else {  //已提交状态可进行反提交
                        new AlertDialog
                                .Builder(mContext)
                                .setTitle(getString(R.string.common_notice))
                                .setMessage(getString(R.string.daily_resubmit_notice1))
                                .setNegativeButton(getString(R.string.common_cancel), null)
                                .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                        } else { // 通用老的反提交
                                            url = CommonUtil.getAppBaseUrl(ct) + "/mobile/commonres.action";
                                            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, COM_DOC_RESUB_SUCCESSFUL, null, null, "post");
                                        }

                                    }
                                }).show();
                    }

                }
            });

        }

        // 删除
        delete_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(real_status) && real_status.equals("在录入")) {
                    new AlertDialog
                            .Builder(mContext)
                            .setTitle(getString(R.string.common_notice))
                            .setMessage(getString(R.string.delete_doc_notice2))
                            .setNegativeButton(getString(R.string.common_cancel), null)
                            .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DeleteComDocRequest(mCaller, mkeyValue);
                                }
                            }).show();
                } else if (!TextUtils.isEmpty(real_status) && real_status.equals("已提交")) {
                    new AlertDialog
                            .Builder(mContext)
                            .setTitle(getString(R.string.common_notice))
                            .setMessage(getString(R.string.delete_sumited_notice1))
                            .setNegativeButton(getString(R.string.common_cancel), null)
                            .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {  //反提交
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
                            }).show();
                }
            }
        });
    }

    private void getapplypeomsg(String em_number) {
        if (!em_number.isEmpty()) {
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
            if (ActivityUtils.isExsitMianActivity(mContext, CommonFormListActivity.class)) {
                LogUtil.d("正常返回...");
                if ("1".equals(update)) {
                    startActivity(new Intent(mContext, CommonFormListActivity.class)
                            .putExtra("serveId", mServiceId)
                            .putExtra("caller", mCaller)
                            .putExtra("title", mTitle)
                            .putExtra("update", update));
                    finish();
                } else {
                    onBackPressed();
                }
            } else {
                LogUtil.d("非正常返回..." + update);
                if ("1".equals(update)) {
                    CommonFormListActivity.reload = true;
                } else {
                    CommonFormListActivity.reload = false;
                }
                startActivity(
                        new Intent(mContext, CommonFormListActivity.class)
                                .putExtra("serveId", mServiceId)
                                .putExtra("caller", mCaller)
                                .putExtra("title", mTitle));
                finish();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
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
