package com.uas.appworks.OA.erp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.PermissionUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.MyListView;
import com.core.widget.NScrollerGridView;
import com.core.widget.SquareCenterImageView;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.MultiImagePreviewActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.MyGridView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.apputils.widget.InputDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appworks.OA.erp.activity.form.FormListSelectActivity;
import com.uas.appworks.OA.erp.model.AddCostBean;
import com.uas.appworks.OA.erp.model.CommonDocAMBean;
import com.uas.appworks.OA.erp.model.CostBean;
import com.uas.appworks.OA.erp.model.CostFormModel;
import com.uas.appworks.OA.erp.model.CostSingleBean;
import com.uas.appworks.OA.erp.model.CostTypeSingleBean;
import com.uas.appworks.OA.erp.model.CostUpdatePModel;
import com.uas.appworks.OA.erp.model.SaveCostDetailsGridStoreBean;
import com.uas.appworks.OA.erp.model.UpdateCostDetailsGridStoreBean;
import com.uas.appworks.R;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
/**
 * Created by FANGlh on 2017/6/9.
 * function:
 */

public class ExpenseReimbursementActivity extends BaseActivity implements View.OnClickListener {
    private FormEditText reimbursement_title_fet;
    private FormEditText reimbursement_type_fet;
    private FormEditText reimbursement_currency_fet;
    private Button btn_save;
    private NScrollerGridView gv_details_list;
    private TextView sum_money_tv;
    private LinearLayout detail_table_ll;
    private MyListView table_details_list;
    private TextView add_details_tv;
    private MyGridView mGridView;
    private GridViewAdapter mAdapter;
    private ArrayList<String> mPhotoList;
    private Uri mNewPhotoUri;// 拍照和图库 获得图片的URI
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
    private static final int SELECT_REIMBURSEMENT_TYPE = 3;
    private static final int SELECT_CURRENCY = 4;
    private PopupWindow setWindow = null;//
    private PopupWindow setCalculatorWindow = null;
    private Button btn_sure;
    private int VERSION_CODES;

    private DetailsTableAdapter dtAdapter;
    private GridDataAdapter gAdapter;
    private List<String> selected_detailsData; //点击后要返回的消费明细类型
    private double sum_money = 0.0; // 最后算的的总金额
    private List<CostFormModel> costFormModel_list;
    private CostFormModel costFormModel;
    private String money_editString = "";
    private TextView c_result;
    private TextView c_edit;
    //声明两个参数。接收tvResult前后的值
    private double num1 = 0, num2 = 0;
    private double Result = 0;//计算结果
    private int op = 0;//判断操作数，
    private boolean isClickEqu = false;//判断是否按了“=”按钮
    private String caller = "FeePlease!FYBX";
    private CostBean mCostBean;
    private List<CostBean.DataBean.FormdetailBean> mFormconfigsBean;
    private List<CostBean.DataBean.GridetailBean> mGridconfigsBean;

    private CommonDocAMBean mCommonDocAMBean;
    private List<CommonDocAMBean.DatasBean.FormconfigsBean> mFormconfigsList;
    private List<CommonDocAMBean.DatasBean.GridconfigsBean> mGridconfigsList;

    private AddCostBean mAddCostBean;
    private List<AddCostBean> mAddCost_list; // popwindow要展示的消费明细类型
    private String[] default_Cost = {"交通费", "通讯费", "招待费", "差旅费", "办公费", "税费", "物业费", "其他"};

    //点击新增需要展示的消费明数据细主表
    private List<CostTypeSingleBean> costtypesinglebeanList;
    private CostTypeSingleBean mcosttypesinglebean;
    private CostSingleBean money_combdatas;
    private String deparmentname;
    private int keyValue;
    private int formid = 0; //重新提交带过来的id


    private Handler erhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = msg.getData().getString("result");
            switch (msg.what) {
                case 0x01:
                    if (!StringUtil.isEmpty(result)) {
                        handleTypeAndM(result);
                    }
                    LogUtil.prinlnLongMsg("0x01result", result);
                    break;
                case 0x02:
                    if (!StringUtil.isEmpty(result)) {
                        try {
                            JSONObject resultJsonObject = JSON.parseObject(result);
                            String dataString = resultJsonObject.getString("data").toString();
                            costtypesinglebeanList = JSON.parseArray(dataString, CostTypeSingleBean.class);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    LogUtil.prinlnLongMsg("0x02result", result);
                    break;

                case 0x15:
                    if (!StringUtil.isEmail(result)) {
                        LogUtil.prinlnLongMsg("0x15result", result);
                        if (!ListUtils.isEmpty(costtypesinglebeanList))
                            costtypesinglebeanList.clear();
                        try {
                            JSONObject resultJsonObject = JSON.parseObject(result);
                            JSONArray array = resultJsonObject.getJSONArray("combdatas");
                            if (!ListUtils.isEmpty(array)) {
                                for (int i = 0; i < array.size(); i++) {
                                    mcosttypesinglebean = new CostTypeSingleBean();
                                    mcosttypesinglebean.setFk_name(array.getJSONObject(i).getString("DISPLAY"));
                                    mcosttypesinglebean.setFk_desc(array.getJSONObject(i).getString("VALUE"));
                                    costtypesinglebeanList.add(mcosttypesinglebean);
                                }
                            }
                            LogUtil.prinlnLongMsg("costtypesinglebeanList0x15", costtypesinglebeanList.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 0x03:
                    if (!StringUtil.isEmpty(result)) {
                        money_combdatas = handleSingleArray(result, money_combdatas);  //币种列表数据
                    }
                    LogUtil.prinlnLongMsg("0x03result", result);
                    break;
                case 0x04:
                    if (!StringUtil.isEmpty(result)) {
                        handleCostDetailsData(result);  //点击新增需要显示的消费明细View数据
                    }
                    LogUtil.prinlnLongMsg("0x04result", result);
                    break;
                case 0x16:
                    if (!StringUtil.isEmpty(result)) {

                    }
                    LogUtil.prinlnLongMsg("0x16result", result);
                    break;
                case 0x05:
                    if (!StringUtil.isEmpty(result)) {
                        //{"fp_id": 38366,"success": true,"fpd_id": [[28859,1]]}
                        keyValue = JSON.parseObject(result).getIntValue("fp_id");
                        sendPicture();
                    }
                    LogUtil.prinlnLongMsg("0x05result", result);
                    break;

                case 0x06:
                    if (!StringUtil.isEmpty(result)) {
                        handleUpdateData(result);
                    }
                    LogUtil.prinlnLongMsg("0x06result", result);
                    break;
                case 0x07:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x07result", result);
                        progressDialog.dismiss();
                        Toast.makeText(ct, getString(R.string.invoice_submitted_successfully), Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                judgeApprovers(keyValue);
                            }
                        }, 1000);
                    }
                    break;
                case 0x08:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x08reasult", result);
                        keyValue = formid;
                        sendPicture();
                    }
                    break;
                case 0x09:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x09result", result);
                        costFormModel_list.remove(cd_table_position);
                        if (!ListUtils.isEmpty(selected_detailsData))
                            selected_detailsData.remove(cd_table_position);

                        if ("S".equals(dg_type)) {
                            if (cd_table_position < costFormModel_list.size())
                                costFormModel_list.remove(cd_table_position);
                        }
                        if (costFormModel_list.size() == 0) {
                            detail_table_ll.setVisibility(View.GONE);
                            sum_money_tv.setText(null);
                        }
                        doAutoCalculateJudge();
                        dtAdapter.notifyDataSetChanged();
                    }
                    break;
                case 0x10:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x10result", result);
                        try {
                            JSONObject resultObject = JSON.parseObject(result);
                            if (resultObject != null) {
                                mCostUpdatePModel = new CostUpdatePModel();
                                mCostUpdatePModel = JSON.parseObject(JSON.toJSONString(resultObject), CostUpdatePModel.class);
                                doShowUpP(mCostUpdatePModel);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        progressDialog.dismiss();
                    }
                    break;
                case 0x11:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x11result", result);
                    }
                    break;
                case 0x12:
                    if (StringUtil.isEmpty(result)) break;
                    if (!StringUtil.isEmpty(result) && JSONUtil.validate(result)) {
                        JSONObject object = JSON.parseObject(result);
                        if (object.containsKey("assigns")) {
                            JSONArray array = JSON.parseObject(result).getJSONArray("assigns");
                            JSONObject o = array.getJSONObject(0);
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
                                commitSuccess(keyValue);
                            }

                        } else {
                            progressDialog.dismiss();
                            commitSuccess(keyValue);
                        }
                    } else {
                        progressDialog.dismiss();
                        commitSuccess(keyValue);
                    }
                    break;
                case 0x13:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x13result", result);
                        commitSuccess(keyValue); //延时跳转，确认抓取成功
                    }
                    progressDialog.dismiss();
                    break;
                case 0x14:
                    if (!StringUtil.isEmpty(result)) {
                        Log.i("0x14result", result);
                        judgeApprovers(keyValue);
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    btn_save.setEnabled(true);
                    if (JSONUtil.validate(result)) {
                        ToastMessage(JSON.parseObject(result).getString("exceptionInfo"));
                    } else {
                        ToastMessage(result);
                    }
                    progressDialog.dismiss();
                    btn_save.setEnabled(true);
                    break;
            }
        }
    };
    private CostUpdatePModel mCostUpdatePModel;
    private int et_position;
    private TextView details_presentation;


    private void doShowUpP(CostUpdatePModel mCostUpdatePModel) {

        if (ListUtils.isEmpty(mCostUpdatePModel.getFiles())) return;
        if (!ListUtils.isEmpty(mPhotoList)) mPhotoList.clear();
        for (int i = 0; i < mCostUpdatePModel.getFiles().size(); i++) {
            mPhotoList.add(mCostUpdatePModel.getFiles().get(i).getFp_path());
        }

        mAdapter.setCUPmodel(mCostUpdatePModel);
        mAdapter.notifyDataSetChanged();
        progressDialog.dismiss();
        Log.i("mPhotoList_update", JSON.toJSONString(mPhotoList));
    }


    private void getPictUrl(List<String> pathlist) {
        Log.i("pathlist", JSON.toJSONString(pathlist));
        for (int i = 0; i < pathlist.size(); i++) {
            String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "common/download.action";
            Map<String, Object> params = new HashMap<>();
            params.put("path", pathlist.get(i));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x11, null, null, "get");
        }
    }

    private int cd_table_position;
    private String fb_attach;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_reimbursement);
        ViewUtils.inject(this);
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (PermissionUtil.lacksPermissions(ct, permission)) {
                PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permission);
            }
        }
        initView();
        initTypeAndMoneyData();  // 获取该表单相关字段，

        initPicturesEvent();   //添加发票事件
    }

    private void initView() {
        VERSION_CODES = Build.VERSION.SDK_INT;

        initids();
        reimbursement_title_fet.setOnClickListener(this);
        reimbursement_type_fet.setKeyListener(null);
        reimbursement_type_fet.setFocusable(false);
        reimbursement_type_fet.setOnClickListener(this);

        reimbursement_currency_fet.setKeyListener(null);
        reimbursement_currency_fet.setFocusable(false);
        reimbursement_currency_fet.setOnClickListener(this);

        btn_save.setOnClickListener(this);

        mGridView = (MyGridView) findViewById(R.id.grid_view);
        findViewById(R.id.add_details_tv).setOnClickListener(this);
        findViewById(R.id.automatic_calculation_tv).setOnClickListener(this);
        findViewById(R.id.i_calculation_tv).setOnClickListener(this);
        mPhotoList = new ArrayList<String>();
        mAdapter = new GridViewAdapter();
        mGridView.setAdapter(mAdapter);

        detail_table_ll.setVisibility(View.GONE);

        costFormModel_list = new ArrayList<>();
        costFormModel = new CostFormModel();

        mGridconfigsBean = new ArrayList<>();
        mFormconfigsBean = new ArrayList<>();
        costtypesinglebeanList = new ArrayList<>();
        mcosttypesinglebean = new CostTypeSingleBean();
        money_combdatas = new CostSingleBean();
        mCostBean = new CostBean();

        mAddCostBean = new AddCostBean();
        mAddCost_list = new ArrayList<>();
        //消费明细表格适配器

        dtAdapter = new DetailsTableAdapter();
        table_details_list.setAdapter(dtAdapter);

        mFormconfigsList = new ArrayList<>();
        mGridconfigsList = new ArrayList<>();
        Intent intent = getIntent();
        formid = intent.getIntExtra("id", 0);
        if (formid != 0) {
            getUpdateData(formid);  //  获取单据数据
            btn_save.setText(getString(R.string.common_update_button));
            keyValue = formid;
            Log.i("formid", formid + "if");
        } else {
            Log.i("formid", formid + "else");
        }

    }

    private void initids() {
        reimbursement_title_fet = (FormEditText) findViewById(R.id.reimbursement_title_fet);
        reimbursement_type_fet = (FormEditText) findViewById(R.id.reimbursement_type_fet);
        reimbursement_currency_fet = (FormEditText) findViewById(R.id.reimbursement_currency_fet);
        btn_save = (Button) findViewById(R.id.btn_save);
        gv_details_list = (NScrollerGridView) findViewById(R.id.gv_details_list);
        sum_money_tv = (TextView) findViewById(R.id.sum_money_tv);
        detail_table_ll = (LinearLayout) findViewById(R.id.detail_table_ll);
        table_details_list = (MyListView) findViewById(R.id.table_details_list);
        add_details_tv = (TextView) findViewById(R.id.add_details_tv);
    }

    /**
     * //获取发票图片路径接口
     */
    private void getUpdatePicture() {
        String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "common/getFilePaths.action";
        Map<String, Object> params = new HashMap<>();
        params.put("field", "fb_attach");
//        params.put("caller",caller);
        params.put("id", fb_attach);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x10, null, null, "get");

    }

    /**
     * 重新提交操作根据id，获取单据内容
     *
     * @param id
     */
    private void getUpdateData(int id) {
        progressDialog.show();
        if (!CommonUtil.isNetWorkConnected(mContext)) {
            ToastMessage(getString(R.string.networks_out));
        }
        String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/common/getformandgriddata.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("id", id);
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x06, null, null, "get");
    }

    private void commitSuccess(final int id) {
        ToastMessage(getString(R.string.submit_success));
        progressDialog.dismiss();
        btn_save.setEnabled(true);
        erhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) return;
                startActivity(new Intent(mContext, CommonDocDetailsActivity.class)
                        .putExtra("caller", caller)
                        .putExtra("keyValue", id)
                        .putExtra("status", "已提交"));

                overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
                finish();
            }
        }, 500);
    }

    private CostSingleBean handleSingleArray(String result, CostSingleBean combdatas) {
        LogUtil.prinlnLongMsg("handleSingleList_result", result);
        JSONObject resultJsonObject = null;
        try {
            resultJsonObject = JSON.parseObject(result);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (resultJsonObject != null) {
            combdatas = JSON.parseObject(resultJsonObject.toString(), CostSingleBean.class);
            LogUtil.prinlnLongMsg("combdatasCh", JSON.toJSONString(combdatas));
            return combdatas;
        } else {
            return null;
        }
    }

    private void handleCostDetailsData(String result) {
        try {
            JSONObject resultJsonObject = JSON.parseObject(result);
            if (resultJsonObject != null) {
                String dataresult = resultJsonObject.getString("data");
                if (!StringUtil.isEmpty(dataresult)) {
                    JSONArray dataArray = JSON.parseArray(dataresult);
                    mAddCost_list.addAll(JSON.parseArray(dataArray.toString(), AddCostBean.class));
                    LogUtil.prinlnLongMsg("mAddCost_list", JSON.toJSONString(mAddCost_list));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新时录入界面数据的处理初始化
     *
     * @param result
     */

    private void handleUpdateData(String result) {
        mFormconfigsList.clear();
        mGridconfigsList.clear();
        try {
            JSONObject resultJsonObject = JSON.parseObject(result);
            JSONObject dataObjecty = resultJsonObject.getJSONObject("datas");
            JSONArray formdataArray = dataObjecty.getJSONArray("formdata");
            JSONArray griddataArray = dataObjecty.getJSONArray("griddata");
            mCommonDocAMBean = JSON.parseObject(resultJsonObject.toString(), CommonDocAMBean.class);
            if (mCommonDocAMBean.getDatas() == null) return;
            if (!ListUtils.isEmpty(mCommonDocAMBean.getDatas().getFormdata())
                    && !ListUtils.isEmpty(mCommonDocAMBean.getDatas().getFormconfigs())) {

                int allFormconfigs_num = mCommonDocAMBean.getDatas().getFormconfigs().size();
                for (int i = 0; i < allFormconfigs_num; i++) {
                    String fd_caption = mCommonDocAMBean.getDatas().getFormconfigs().get(i).getFD_CAPTION();
                    String fd_field = mCommonDocAMBean.getDatas().getFormconfigs().get(i).getFD_FIELD();
                    int mfd_isdefault = mCommonDocAMBean.getDatas().getFormconfigs().get(i).getMFD_ISDEFAULT();
                    if (mfd_isdefault == -1 && fd_caption != null && fd_field != null) {
                        mFormconfigsList.add(mCommonDocAMBean.getDatas().getFormconfigs().get(i));
                    }
                }
                doShowFormData(formdataArray, mFormconfigsList);
                LogUtil.prinlnLongMsg("mFormconfigsList", JSON.toJSONString(mFormconfigsList));
                LogUtil.prinlnLongMsg("formdataArray", JSON.toJSONString(formdataArray));

            }

            if (!ListUtils.isEmpty(mCommonDocAMBean.getDatas().getGriddata())
                    && !ListUtils.isEmpty(mCommonDocAMBean.getDatas().getGridconfigs())) {
                int allGridconfigs_num = mCommonDocAMBean.getDatas().getGridconfigs().size();
                for (int i = 0; i < allGridconfigs_num; i++) {
                    String dg_caption = mCommonDocAMBean.getDatas().getGridconfigs().get(i).getDG_CAPTION();
                    String dg_field = mCommonDocAMBean.getDatas().getGridconfigs().get(i).getDG_FIELD();
                    int mdg_isdefault = mCommonDocAMBean.getDatas().getGridconfigs().get(i).getMDG_ISDEFAULT();
                    if (mdg_isdefault == -1 && dg_caption != null && dg_field != null) {
                        mGridconfigsList.add(mCommonDocAMBean.getDatas().getGridconfigs().get(i));
                    }
                }
                doShowGridData(griddataArray);
                LogUtil.prinlnLongMsg("mGridconfigsList", JSON.toJSONString(mGridconfigsList));
                LogUtil.prinlnLongMsg("griddataArray", JSON.toJSONString(griddataArray));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doShowGridData(JSONArray griddataArray) {
        if (ListUtils.isEmpty(griddataArray)) return;
        for (int i = 0; i < griddataArray.size(); i++) {
            costFormModel = new CostFormModel();
            costFormModel.setCost_type(griddataArray.getJSONObject(i).getString("fpd_d1"));
            costFormModel.setCost_money(griddataArray.getJSONObject(i).getDouble("fpd_total"));
            costFormModel.setFpd_id(griddataArray.getJSONObject(i).getInteger("fpd_id"));
            costFormModel.setRemark(JSONUtil.getText(griddataArray.getJSONObject(i), "fpd_d7"));
            costFormModel_list.add(costFormModel);
            if (i == griddataArray.size() - 1) {
                dtAdapter.setModels(costFormModel_list);
                dtAdapter.notifyDataSetChanged();
                detail_table_ll.setVisibility(View.VISIBLE);
                doAutoCalculateJudge();
//                progressDialog.dismiss();
                LogUtil.prinlnLongMsg("costFormModel_list", JSON.toJSONString(costFormModel_list));
            }
        }
    }

    private void doShowFormData(JSONArray formdataArray, List<CommonDocAMBean.DatasBean.FormconfigsBean> mFormconfigsList) {
        if (!ListUtils.isEmpty(mFormconfigsList) && !ListUtils.isEmpty(formdataArray)) {

            fb_attach = formdataArray.getJSONObject(0).getString("fb_attach");
            Log.i("fb_attach_ss", fb_attach + "?");
            if (!StringUtil.isEmpty(fb_attach)) {
                getUpdatePicture(); //获取发票图片
            } else {
                progressDialog.dismiss();
            }


            for (int i = 0; i < mFormconfigsList.size(); i++) {
                String key = mFormconfigsList.get(i).getFD_FIELD();
                if (StringUtil.isEmpty(key)) return;
                String value = formdataArray.getJSONObject(0).getString(key);
                if (StringUtil.isEmpty(value)) return;
                switch (key) {
                    case "fp_class":
                        reimbursement_type_fet.setText(value);
                        break;
                    case "fp_v13":
                        reimbursement_currency_fet.setText(value);
                        break;
                    case "fp_pleaseamount":
                        sum_money_tv.setText(value);
                        break;
                    case "fp_v3":
                        reimbursement_title_fet.setText(value);
                        break;
                }
            }
        }
    }

    private void handleTypeAndM(String result) {
        try {
            JSONObject amresultJsonObject = JSON.parseObject(result);
            JSONObject dataObjecty = amresultJsonObject.getJSONObject("data");
            JSONArray formdataArray = dataObjecty.getJSONArray("formdetail");
            JSONArray griddataArray = dataObjecty.getJSONArray("gridetail");

            if (formdataArray == null || griddataArray == null) {
                // TODO
            } else {
                mCostBean = JSON.parseObject(amresultJsonObject.toString(), CostBean.class);
                if (mCostBean.getData().getFormdetail() != null && mCostBean.getData().getGridetail() != null) {
                    int allFormconfigs_num = mCostBean.getData().getFormdetail().size();
                    for (int i = 0; i < allFormconfigs_num; i++) {
                        String fd_caption = mCostBean.getData().getFormdetail().get(i).getFd_caption();
                        String fd_field = mCostBean.getData().getFormdetail().get(i).getFd_field();
                        int mfd_isdefault = mCostBean.getData().getFormdetail().get(i).getMfd_isdefault();
                        if (mfd_isdefault == -1 && fd_caption != null && fd_field != null) {
                            mFormconfigsBean.add(mCostBean.getData().getFormdetail().get(i));
                        }

                    }
                    LogUtil.prinlnLongMsg("mFormconfigsBean_eg", JSON.toJSONString(mFormconfigsBean));
                    getTypeAndMoneyData();   //获取报销类型、币种数据
                }

                //  获取需要显示的从表单配置数据属性保存到mGridconfigsBean中
                if (mCostBean.getData().getGridetail() != null && mCostBean.getData().getGridetail() != null) {
                    int allGridconfigs_num = mCostBean.getData().getGridetail().size();
                    for (int i = 0; i < allGridconfigs_num; i++) {
                        String dg_caption = mCostBean.getData().getGridetail().get(i).getDg_caption();
                        String dg_field = mCostBean.getData().getGridetail().get(i).getDg_field();
                        int mdg_isdefault = mCostBean.getData().getGridetail().get(i).getMdg_isdefault();
                        if (mdg_isdefault == -1 && dg_caption != null && dg_field != null) {
                            mGridconfigsBean.add(mCostBean.getData().getGridetail().get(i));
                        }
                    }
                    LogUtil.prinlnLongMsg("mGridconfigsBean_eg", JSON.toJSONString(mGridconfigsBean));
                    getCostDetailsData();     //获取新增要展示明细列表数据源
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void getCostDetailsData() {

        String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
        String em_code = CommonUtil.getSharedPreferences(mContext, "erp_username");
        String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
        String selection = "em_code=? and whichsys=? ";
        DBManager manager = new DBManager(this);
        try {
            EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
            if (bean != null) {
                //            String imId = String.valueOf(bean.getEm_IMID());
                //            String imName = String.valueOf(bean.getEM_NAME());
                deparmentname = String.valueOf(bean.getEM_DEPART());
                //            String imPosition = String.valueOf(bean.getEM_POSITION());
                LogUtil.prinlnLongMsg("deparmentname", deparmentname);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "";
        Map<String, Object> params_Details = new HashMap<>();
        params_Details.put("which", "grid");
        params_Details.put("caller", "FeeCategorySet");
        params_Details.put("field", "fpd_d1");
        params_Details.put("page", "1");
        params_Details.put("condition", "1=1" + " AND fcs_departmentname='" + deparmentname + "'");
        params_Details.put("pageSize", "1000");
        LinkedHashMap<String, Object> headers1 = new LinkedHashMap<>();
        headers1.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        if (!ListUtils.isEmpty(mGridconfigsBean)) {
            for (int i = 0; i < mGridconfigsBean.size(); i++) {
                if ("fpd_d1".equals(mGridconfigsBean.get(i).getDg_field())) {
                    dg_type = mGridconfigsBean.get(i).getDg_type();
                    if ("S".equals(dg_type)) {
                        url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getComboValue.action";
                        ViewUtil.httpSendRequest(ct, url, params_Details, erhandler, headers1, 0x04, null, null, "get");
                    } else if ("DF".equals(dg_type)) {
                        url = CommonUtil.getAppBaseUrl(ct) + "common/dbfind.action";
                        ViewUtil.httpSendRequest(ct, url, params_Details, erhandler, headers1, 0x04, null, null, "get");
                    }
                }
            }
        }

    }

    private String fd_type = "";
    private String dg_type = "";

    private void getTypeAndMoneyData() {

        String url = "";
        Map<String, Object> params_type = new HashMap<>();
        params_type.put("caller", caller);
        params_type.put("field", "fp_class");
        params_type.put("page", "1");
        params_type.put("which", "form");
        params_type.put("condition", "1=1");
        params_type.put("pageSize", "1000");
        LinkedHashMap<String, Object> headers1 = new LinkedHashMap<>();
        headers1.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        if (!ListUtils.isEmpty(mFormconfigsBean)) {
            for (int i = 0; i < mFormconfigsBean.size(); i++) {
                if ("fp_class".equals(mFormconfigsBean.get(i).getFd_field())) {
                    fd_type = mFormconfigsBean.get(i).getFd_type();
                    if ("C".equals(fd_type)) {
                        url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getComboValue.action";
                        ViewUtil.httpSendRequest(ct, url, params_type, erhandler, headers1, 0x15, null, null, "get");
                    } else if ("SF".equals(fd_type)) {
                        url = CommonUtil.getAppBaseUrl(ct) + "common/dbfind.action";
                        ViewUtil.httpSendRequest(ct, url, params_type, erhandler, headers1, 0x02, null, null, "get");
                    }
                }
            }
        }
        String url2 = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getComboValue.action";
        Map<String, Object> params_money = new HashMap<>();
        params_money.put("caller", caller);
        params_money.put("field", "fp_v13");
        LinkedHashMap<String, Object> headers2 = new LinkedHashMap<>();
        headers2.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url2, params_money, erhandler, headers2, 0x03, null, null, "get");
    }

    private void initTypeAndMoneyData() {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getformandgriddetail.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("condition", "1=1");
        params.put("id", 0);
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x01, null, null, "get");
    }


    private void showCalculationWindow() {
        if (setCalculatorWindow == null) initCalculationWindow();
        setCalculatorWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        if (VERSION_CODES < 24) // API版本太高不断刷新View亮度，会有黑缝隙bug
            DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void initCalculationWindow() {
        View calculationviewContext = LayoutInflater.from(ct).inflate(R.layout.simple_calculator_ppview, null);

        setCalculatorWindow = new PopupWindow(calculationviewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setCalculatorWindow.setAnimationStyle(R.style.MenuAnimationFade);
//        setCalculatorWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setCalculatorWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closeCalculatorWindow();
            }
        });
        initCalculationView(calculationviewContext);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reimbursement_type_fet) {
            LogUtil.prinlnLongMsg("combdatasType", JSON.toJSONString(costtypesinglebeanList));
            if (ListUtils.isEmpty(costtypesinglebeanList)) {
                ToastMessage(getString(R.string.crm_nodatas));
                return;
            }
            doTypeSingleSelect(costtypesinglebeanList, 2, getString(R.string.Reimbursement_type), SELECT_REIMBURSEMENT_TYPE);

        } else if (v.getId() == R.id.reimbursement_currency_fet) {
            LogUtil.prinlnLongMsg("combdatasMoney", JSON.toJSONString(money_combdatas));
            if (money_combdatas == null) {
                ToastMessage(getString(R.string.crm_nodatas));
                return;
            }
            doSingleSelect(money_combdatas, 2, getString(R.string.currency), SELECT_CURRENCY);
        } else if (v.getId() == R.id.add_details_tv) {
            if ("S".equals(dg_type)) {
                showEditWindow();
//                }else if ("DF".equals(dg_type)){
            } else {
                showPopupWindow();
            }
            putDownInput();
        } else if (v.getId() == R.id.automatic_calculation_tv) {
            doAutoCalculateJudge();
        } else if (v.getId() == R.id.i_calculation_tv) {
            showCalculationWindow();
        } else if (v.getId() == R.id.btn_save) {
            doSaveJudge();
        } else if (v.getId() == R.id.c_0) {
            c_onclick("0");
        } else if (v.getId() == R.id.c_1) {
            c_onclick("1");
        } else if (v.getId() == R.id.c_2) {
            c_onclick("2");
        } else if (v.getId() == R.id.c_3) {
            c_onclick("3");
        } else if (v.getId() == R.id.c_4) {
            c_onclick("4");
        } else if (v.getId() == R.id.c_5) {
            c_onclick("5");
        } else if (v.getId() == R.id.c_6) {
            c_onclick("6");
        } else if (v.getId() == R.id.c_7) {
            c_onclick("7");
        } else if (v.getId() == R.id.c_8) {
            c_onclick("8");
        } else if (v.getId() == R.id.c_9) {
            c_onclick("9");
        } else if (v.getId() == R.id.c_point) {
            if (StringUtil.isEmpty(c_edit.getText().toString())) return;
            if (!StringUtil.isEmpty(c_edit.getText().toString()) && c_edit.getText().toString().contains("."))
                return;
            if (StringUtil.isEmpty(c_edit.getText().toString()) && StringUtil.isEmpty(c_result.getText().toString()))
                return;
            c_onclick(".");
        } else if (v.getId() == R.id.c_c) {
            num1 = 0;
            num2 = 0;
            Result = 0;//计算结果
            op = 0;//判断操作数，
            c_result.setText(null);
            c_edit.setText(null);
        } else if (v.getId() == R.id.c_add) {
            String stringadd = c_edit.getText().toString();
            if (!StringUtil.isEmpty(stringadd)) {
                num1 = Double.valueOf(stringadd);
                Result = Result + num1;
            }
            c_edit.setText(null);
            c_result.setText(Result + "");
            op = 1;
            isClickEqu = false;
        } else if (v.getId() == R.id.c_cancel) {
            num1 = 0;
            num2 = 0;
            Result = 0;//计算结果
            op = 0;//判断操作数，
            c_result.setText(null);
            c_edit.setText(null);
            closeCalculatorWindow();
        } else if (v.getId() == R.id.c_equal) {
            String stringresult = c_result.getText().toString();
            String nowedit = c_edit.getText().toString();
            if (StringUtil.isEmpty(stringresult)) {
                if (!StringUtil.isEmpty(nowedit)) {
                    double d = CommonUtil.getTwoPointDouble(Double.valueOf(nowedit));
                    costFormModel_list.get(et_position).setCost_money(CommonUtil.getTwoPointDouble(Double.valueOf(nowedit)));
                    dtAdapter.notifyDataSetChanged();
                    doAutoCalculateJudge();
                    c_edit.setText(null);
                    Log.i("left=,right=,d=", stringresult + "," + nowedit + "," + d);
                    closeCalculatorWindow();
                } else {
                    ToastMessage("请输入");
                }

            } else {
                Result = Double.valueOf(stringresult);
                if (!StringUtil.isEmpty(nowedit)) {
                    num2 = Double.valueOf(nowedit);
                    c_result.setText(null);
                    switch (op) {
                        case 0:
                            Result = num2;
                            break;
                        case 1:
                            Result = Result + num2;
                            break;
                        case 2:
                            Result = Result - num2;
                            break;
                        default:
                            Result = 0;
                            break;
                    }
                    c_edit.setText(null);
                    c_result.setText(String.valueOf(Result));
                }
                isClickEqu = true;
                costFormModel_list.get(et_position).setCost_money(CommonUtil.getTwoPointDouble(Double.valueOf(Result)));
                dtAdapter.notifyDataSetChanged();
                doAutoCalculateJudge();
                c_result.setText(null);
                closeCalculatorWindow();

            }
            num1 = 0;
            num2 = 0;
            Result = 0;//计算结果
            op = 0;//判断操作数，
            c_result.setText(null);
            c_edit.setText(null);
        } else if (v.getId() == R.id.c_backspace) {
            String myStr = c_edit.getText().toString();
            try {
                c_edit.setText(myStr.substring(0, myStr.length() - 1));
            } catch (Exception e) {
                c_edit.setText("");
            }
        }
    }

    private PopupWindow etpopupWindow = null;

    private void showEditWindow() {
// 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.item_select_cost_details, null);

        // 设置按钮的点击事件
        final EditText detail_et = (EditText) contentView.findViewById(R.id.input_cost_detail_et);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 150);
        contentView.findViewById(R.id.cancel_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etpopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.sure_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtil.isEmpty(detail_et.getText().toString())) {
                    ToastMessage(getString(R.string.input_cost_name));
                    return;
                } else {
                    costFormModel = new CostFormModel();
                    costFormModel.setCost_type(detail_et.getText().toString());
                    costFormModel_list.add(costFormModel);

                    dtAdapter.setModels(costFormModel_list);
                    dtAdapter.notifyDataSetChanged();
                    detail_table_ll.setVisibility(View.VISIBLE);
                    Log.i("costFormModel_list", JSON.toJSONString(costFormModel_list) + "et");
                    etpopupWindow.dismiss();
                }
            }
        });
        etpopupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        etpopupWindow.setTouchable(true);
        etpopupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        etpopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_round_bg));
        // 设置好参数之后再show
        etpopupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }

    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (etpopupWindow == null) return;
        etpopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(ExpenseReimbursementActivity.this, 1f);
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

    private void c_onclick(String s) {
        if (c_edit.getText().length() > 7 || CommonUtil.getNumByString(c_edit.getText().toString()) > 1000000)
            return;
        if (isClickEqu) {
            c_edit.setText(null);
            isClickEqu = false;
        }
        String myString = c_edit.getText().toString();
        myString += s;
        c_edit.setText(myString);
    }

    private void doSaveJudge() {
        if (!reimbursement_title_fet.testValidity()) return;

        if (StringUtil.isEmpty(reimbursement_type_fet.getText().toString())) {
            ToastMessage(getString(R.string.select_cost_type));
            return;
        }

        if (StringUtil.isEmpty(reimbursement_currency_fet.getText().toString())) {
            ToastMessage(getString(R.string.select_money_type));
            return;
        }

        if (ListUtils.isEmpty(costFormModel_list)) {
            ToastMessage(getString(R.string.add_cost_details));
            return;
        }
        for (int i = 0; i < costFormModel_list.size(); i++) {
            if (costFormModel_list.get(i).getCost_money() == 0) {
                ToastMessage(getString(R.string.enter_details_money));
                return;
            }
        }
        if (ListUtils.isEmpty(mPhotoList) && StringUtil.isEmpty(fb_attach)) {
            ToastMessage(getString(R.string.please_add_image));
            return;
        }
        if (CommonUtil.isNetWorkConnected(ct)) {
            if (formid != 0) {
                doUpdateCommit();
            } else {
                doFirstCommit();
            }
        } else {
            ToastMessage(getString(R.string.networks_out));
        }

    }

    private void doUpdateCommit() {
        progressDialog.show();
        btn_save.setEnabled(false);
        final String url = CommonUtil.getAppBaseUrl(ct) + "mobile/oa/UpdateSubmitFYBX.action";
//        final String url= "https://192.168.253.252:8080/ERP/mobile/oa/UpdateSubmitFYBX.action";
        Map<String, Object> formStoreMap = new HashMap<>();
        formStoreMap.put("fp_v3", reimbursement_title_fet.getText().toString());
        formStoreMap.put("fp_class", reimbursement_type_fet.getText().toString());
        formStoreMap.put("fp_v13", reimbursement_currency_fet.getText().toString());
        formStoreMap.put("fp_id", formid);
        final String formStore = JSON.toJSONString(formStoreMap);

        final List<UpdateCostDetailsGridStoreBean> gridStore_list = new ArrayList<>();
        UpdateCostDetailsGridStoreBean gridStore_bean;
        for (int i = 0; i < costFormModel_list.size(); i++) {
            gridStore_bean = new UpdateCostDetailsGridStoreBean();
            gridStore_bean.setFpd_d1(costFormModel_list.get(i).getCost_type());
            gridStore_bean.setFpd_total(costFormModel_list.get(i).getCost_money());
            gridStore_bean.setFpd_d7(costFormModel_list.get(i).getRemark());
            gridStore_bean.setFpd_id(costFormModel_list.get(i).getFpd_id());
            gridStore_bean.setFpd_fpid(formid);
            gridStore_list.add(gridStore_bean);

            if (i == (costFormModel_list.size() - 1)) {
                Map<String, Object> params = new HashMap<>();
                params.put("caller", caller);
                params.put("formStore", formStore);
                params.put("param1", JSON.toJSONString(gridStore_list, myprofilter)); // 过滤器过滤新增  "fpd_id": 0,那一行
                params.put("param2", "");
                LogUtil.prinlnLongMsg("gridStore_list", JSON.toJSONString(gridStore_list, myprofilter));
                LinkedHashMap<String, Object> headers1 = new LinkedHashMap<>();
                headers1.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                ViewUtil.httpSendRequest(ct, url, params, erhandler, headers1, 0x08, null, null, "post");
            }
        }
    }

    PropertyFilter myprofilter = new PropertyFilter() {
        @Override
        public boolean apply(Object object, String name, Object value) {
            if (name.equalsIgnoreCase("fpd_id") && value.equals(0)) {
                return false;
            }
            return true;
        }
    };

    private void doFirstCommit() {
        progressDialog.show();
        btn_save.setEnabled(false);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/oa/saveAndSubmitFYBX.action";
        Map<String, Object> formStoreMap = new HashMap<>();
        formStoreMap.put("fp_v3", reimbursement_title_fet.getText().toString());
        formStoreMap.put("fp_class", reimbursement_type_fet.getText().toString());
        formStoreMap.put("fp_v13", reimbursement_currency_fet.getText().toString());
        String formStore = JSON.toJSONString(formStoreMap);

        List<SaveCostDetailsGridStoreBean> gridStore_list = new ArrayList<>();
        SaveCostDetailsGridStoreBean gridStore_bean;
        for (int i = 0; i < costFormModel_list.size(); i++) {
            gridStore_bean = new SaveCostDetailsGridStoreBean();
            gridStore_bean.setFpd_d1(costFormModel_list.get(i).getCost_type());
            gridStore_bean.setFpd_d7(costFormModel_list.get(i).getRemark());
            gridStore_bean.setFpd_total(costFormModel_list.get(i).getCost_money());
            gridStore_list.add(gridStore_bean);

            if (i == (costFormModel_list.size() - 1)) {
                Map<String, Object> params = new HashMap<>();
                params.put("caller", caller);
                params.put("formStore", formStore);
                params.put("param", JSON.toJSONString(gridStore_list));
                LogUtil.prinlnLongMsg("gridStore_list", JSON.toJSONString(gridStore_list));
                LinkedHashMap<String, Object> headers1 = new LinkedHashMap<>();
                headers1.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                ViewUtil.httpSendRequest(ct, url, params, erhandler, headers1, 0x05, null, null, "post");
            }
        }
    }

    private void initCalculationView(View calculationviewContext) {
        calculationviewContext.findViewById(R.id.c_0).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_1).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_2).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_3).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_4).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_5).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_6).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_7).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_8).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_9).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_c).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_point).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_backspace).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_cancel).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_add).setOnClickListener(this);
        calculationviewContext.findViewById(R.id.c_equal).setOnClickListener(this);

        c_result = (TextView) calculationviewContext.findViewById(R.id.c_result);
        c_edit = (TextView) calculationviewContext.findViewById(R.id.c_edit);
    }

    private void closeCalculatorWindow() {
        if (setCalculatorWindow != null)
            setCalculatorWindow.dismiss();
        if (VERSION_CODES < 24)
            DisplayUtil.backgroundAlpha(this, 1f);
    }

    private void showPopupWindow() {
        btn_save.setVisibility(View.GONE);
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
    }

    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();

        btn_save.setVisibility(View.VISIBLE);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.expense_details_menu, null);

        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
        gv_details_list = (NScrollerGridView) viewContext.findViewById(R.id.gv_details_list);
        btn_sure = (Button) viewContext.findViewById(R.id.btn_sure);
        details_presentation = (TextView) viewContext.findViewById(R.id.details_presentation);
        initAddView();
        initAddDatas();
        initAddEvent();
    }

    private void initAddEvent() {
        // 明细过长时长按，toast出明细内容
        gv_details_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ct, mAddCost_list.get(position).getFcs_itemname(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        gv_details_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridDataAdapter.ViewModle modle = (GridDataAdapter.ViewModle) view.getTag();
                // TODO 记录当下点击的位置
                if (!ListUtils.isEmpty(selected_detailsData) && selected_detailsData.contains(mAddCost_list.get(position).getFcs_itemname())) {
                    selected_detailsData.remove(mAddCost_list.get(position).getFcs_itemname());
                } else {
                    selected_detailsData.add(mAddCost_list.get(position).getFcs_itemname());
                }
                gAdapter.setSelected_data(selected_detailsData);
                gAdapter.notifyDataSetChanged();
                Log.i("selected_detailsData", selected_detailsData.toString() + "ss");
            }
        });


        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ListUtils.isEmpty(selected_detailsData)) {
                    if (!ListUtils.isEmpty(costFormModel_list))

                        costFormModel_list.clear();
                    dtAdapter.setModels(null);
                    dtAdapter.notifyDataSetChanged();
                    detail_table_ll.setVisibility(View.GONE);
                    sum_money_tv.setText(null);
                    closePopupWindow();
                    return;
                }
                Log.i("selected_detailsData", selected_detailsData.toString() + "ss");
                if (ListUtils.isEmpty(costFormModel_list)) {
                    for (int i = 0; i < selected_detailsData.size(); i++) {
                        costFormModel = new CostFormModel();
                        costFormModel.setCost_type(selected_detailsData.get(i));
                        costFormModel_list.add(costFormModel);

                        if (i == (selected_detailsData.size() - 1)) {
                            dtAdapter.setModels(costFormModel_list);
                            dtAdapter.notifyDataSetChanged();
                            detail_table_ll.setVisibility(View.VISIBLE);
                            Log.i("costFormModel_list", JSON.toJSONString(costFormModel_list) + "tlt");
                        }
                    }
                } else {
                    for (int i = 0; i < 5; i++) {
                    doDetailsHandle();
                   }
                }

                closePopupWindow();
            }
        });
    }

    private void doDetailsHandle() {
        String details_key = "";
        for (int d = 0; d < costFormModel_list.size(); d++) {
            details_key = details_key + "," + costFormModel_list.get(d).getCost_type();

            if (d == costFormModel_list.size() - 1) {
                for (int i = 0; i < selected_detailsData.size(); i++) {
                    if (!details_key.contains(selected_detailsData.get(i).toString())) {
                        costFormModel = new CostFormModel();
                        costFormModel.setCost_type(selected_detailsData.get(i));
                        costFormModel_list.add(costFormModel);
                    }

                    if (i == (selected_detailsData.size() - 1)) {
                        for (int j = 0; j < costFormModel_list.size(); j++) {
                            if (!JSON.toJSONString(selected_detailsData).contains(costFormModel_list.get(j).getCost_type())) {
                                costFormModel_list.remove(j);
                            }

                            if (j == (costFormModel_list.size() - 1)) {
                                dtAdapter.setModels(costFormModel_list);
                                dtAdapter.notifyDataSetChanged();
                                detail_table_ll.setVisibility(View.VISIBLE);
                                Log.i("costFormModel_list", JSON.toJSONString(costFormModel_list) + "tlt");
                            }
                        }
                    }
                }
            }
        }
    }

    private void initAddDatas() {
        if (ListUtils.isEmpty(mAddCost_list)) {
            for (int i = 0; i < default_Cost.length; i++) {
                AddCostBean m = new AddCostBean();
                m.setFcs_itemname(default_Cost[i]);
                mAddCost_list.add(m);
            }
        }
        if (!ListUtils.isEmpty(costFormModel_list) && ListUtils.isEmpty(selected_detailsData)) {
            for (int i = 0; i < costFormModel_list.size(); i++) {
                selected_detailsData.add(costFormModel_list.get(i).getCost_type());
                if (i == (costFormModel_list.size() - 1)) {
                    gAdapter.setSelected_data(selected_detailsData);
                    gAdapter.notifyDataSetChanged();
                }
            }
        }
        gAdapter.setSelected_data(selected_detailsData);
        gAdapter.notifyDataSetChanged();

        gAdapter.setmAddCostBean_list(mAddCost_list);
        gAdapter.notifyDataSetChanged();
        if (ListUtils.isEmpty(costFormModel_list)) {
//            details_presentation.setVisibility(View.VISIBLE);
        } else {
//            details_presentation.setVisibility(View.GONE);
            ToastMessage("明细较长时，可长按明细查看");
        }
    }

    private void initAddView() {
        selected_detailsData = new ArrayList<>();
        gAdapter = new GridDataAdapter(mContext, mAddCost_list);
        gv_details_list.setAdapter(gAdapter);
    }

    private void doAutoCalculateJudge() {
        sum_money = 0.0;
        Log.i("costFormModel_list", JSON.toJSONString(costFormModel_list));
        for (int i = 0; i < costFormModel_list.size(); i++) {
            if (costFormModel_list.get(i).getCost_money() > 0) {
                sum_money = sum_money + Double.valueOf(costFormModel_list.get(i).getCost_money());
                if (i == (costFormModel_list.size() - 1)) {
                    DecimalFormat df = new DecimalFormat("0.##");
                    Double d = new Double(CommonUtil.getTwoPointDouble(sum_money));
                    if (!df.format(d).contains("."))
                        sum_money_tv.setText(df.format(d) + ".0");
                    else
                        sum_money_tv.setText(df.format(d) + "");

                }
            } else {
//                ToastMessage("请输入消费金额");
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == 0x02 && data != null) {
            mPhotoList.addAll(data.getStringArrayListExtra("files"));
            Log.i("files0x01", data.getStringArrayListExtra("files").toString());
            Log.i("mPhotoList", mPhotoList.toString());
//            doImageFiltering(mPhotoList);
            mAdapter.notifyDataSetInvalidated();
        }
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    mPhotoList.add(mNewPhotoUri.getPath());
                    mAdapter.notifyDataSetInvalidated();
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    mPhotoList.add(path);
                    mAdapter.notifyDataSetInvalidated();
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }

        if (resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b != null) {
                switch (requestCode) {
                    case SELECT_REIMBURSEMENT_TYPE:
                        if (!b.getName().contains("(")) {
                            reimbursement_type_fet.setText(b.getName());
                        } else {
                            for (int i = 0; i < costtypesinglebeanList.size(); i++) {
                                if (b.getName().equals(costtypesinglebeanList.get(i).getFk_name() + "(" + costtypesinglebeanList.get(i).getFk_desc() + ")")) {
                                    reimbursement_type_fet.setText(costtypesinglebeanList.get(i).getFk_name());
                                }
                            }
                        }

                        break;
                    case SELECT_CURRENCY:
                        reimbursement_currency_fet.setText(b.getName());
                        break;
                    default:
                }
            }

        }

        if (requestCode == 0x22) {
            if (data != null) {
                SelectBean b = data.getParcelableExtra("data");
                if (b == null) return;
                String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                getEmnameByReturn(name);
            } else {
                commitSuccess(keyValue);
            }
        }
    }

    /**
     * 消费明细表格适配器
     */
    private class DetailsTableAdapter extends BaseAdapter implements Serializable {

        private List<CostFormModel> models;

        public List<CostFormModel> getModels() {
            return models;
        }

        public void setModels(List<CostFormModel> models) {
            this.models = models;
        }

        @Override
        public int getCount() {
            return ListUtils.isEmpty(models) ? 0 : models.size();
        }

        @Override
        public Object getItem(int position) {
            return ListUtils.isEmpty(models) ? 0 : models.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
//            if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(ct, R.layout.item_expense_details_table, null);
            viewHolder.details_type = (TextView) convertView.findViewById(R.id.details_type_tv);
            viewHolder.details_money = (FormEditText) convertView.findViewById(R.id.details_money_tv);
            viewHolder.remark_et = (FormEditText) convertView.findViewById(R.id.remark_et);
            viewHolder.details_delete = (ImageView) convertView.findViewById(R.id.details_delete_im);
            viewHolder.details_money.setKeyListener(null);
            viewHolder.details_money.setFocusable(false);
            viewHolder.remark_et.setKeyListener(null);
            viewHolder.remark_et.setFocusable(false);

            convertView.setTag(viewHolder);
//            }else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
            CostFormModel data = models.get(position);
            viewHolder.details_type.setText(data.getCost_type() + "");
            viewHolder.remark_et.setText(TextUtils.isEmpty(data.getRemark()) ? "" : data.getRemark());
            if (models.get(position).getCost_money() > 0) {
                viewHolder.details_money.setText(data.getCost_money() + "");
            }

            viewHolder.details_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupWindowHelper.showAlart(ExpenseReimbursementActivity.this,
                            getString(R.string.common_notice), getString(R.string.sure_delete_this_details),
                            new PopupWindowHelper.OnSelectListener() {
                                @Override
                                public void select(boolean selectOk) {
                                    if (selectOk) {
                                        if (models.get(position).getFpd_id() != 0) {
                                            cd_table_position = position;
                                            doDeleteCostDetailshttp(position);
                                        } else {

                                            if ("S".equals(dg_type)) {
                                                models.remove(position);
                                            } else if ("DF".equals(dg_type)) {
                                                if (!ListUtils.isEmpty(selected_detailsData)  && selected_detailsData.size() > position) {
                                                    selected_detailsData.remove(position);
                                                    models.remove(position);
                                                }
                                            }
                                            if (ListUtils.isEmpty(models)) {
                                                detail_table_ll.setVisibility(View.GONE);
                                                sum_money_tv.setText(null);
                                            }
                                            doAutoCalculateJudge();
                                            notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                }
            });

            viewHolder.details_money.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_position = position;
                    showCalculationWindow();
                }
            });
            viewHolder.remark_et.setTag(position);
            viewHolder.remark_et.setOnClickListener(mOnClickListener);
           /* viewHolder.details_money.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (StringUtil.isEmpty(s.toString())) {
                        sum_money_tv.setText(null);
                        return;
                    }
                    if (finalViewHolder.details_money.testValidity()){
                        money_editString = s.toString();
                        models.get(position).setCost_money(Double.valueOf(money_editString)); //TODO有bug
                        doAutoCalculateJudge();
                    }

                }
            });*/
            return convertView;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.remark_et) {
                    if (view.getTag() != null && view.getTag() instanceof Integer) {
                        final int postion = (int) view.getTag();
                        if (postion < ListUtils.getSize(models)) {
                            CostFormModel model = models.get(postion);
                            showRemarks(postion, model);

                        }
                    }
                }
            }
        };

        class ViewHolder {
            TextView details_type;
            FormEditText details_money;
            FormEditText remark_et;
            ImageView details_delete;

        }


    }

    private InputDialog mInputDialogChece;

    private void showRemarks(final int postion, CostFormModel model) {
        mInputDialogChece = new InputDialog.Builder(ct)
                .setContent(model.getRemark())
                .setCancelText("清空")
                .setTitle("备注")
                .setSureText("保存")
                .build(new InputDialog.OnDialogClickListener() {
                    @Override
                    public boolean result(boolean clickSure, CharSequence content) {
                        if (postion < ListUtils.getSize(dtAdapter.getModels())) {
                            dtAdapter.getModels().get(postion).setRemark(TextUtils.isEmpty(content) ? "" : content.toString());
                            dtAdapter.notifyDataSetInvalidated();
                            if (!clickSure) {
                                mInputDialogChece.setContent("");
                            }
                        }

                        return clickSure;
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mInputDialogChece != null) {
                mInputDialogChece.dismiss();
            }
        } catch (Exception e) {

        }
    }


    private void doDeleteCostDetailshttp(int position) {
        //TODO 删除details 请求
        String url = CommonUtil.getAppBaseUrl(ct) + "common/deleteDetail.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("gridcaller", caller);
        params.put("condition", "fpd_id=" + costFormModel_list.get(position).getFpd_id());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        if (platform) {
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        } else {
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        }
        ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x09, null, null, "post");
    }

    /**
     * 新增弹出PopupWindow表格适配器
     */
    private class GridDataAdapter extends BaseAdapter {
        private Context ct;
        private LayoutInflater inflater;
        private int selected = 0;
        private List<String> selected_data;
        private List<AddCostBean> mAddCostBean_list;

        public List<AddCostBean> getmAddCostBean_list() {
            return mAddCostBean_list;
        }

        public void setmAddCostBean_list(List<AddCostBean> mAddCostBean_list) {
            this.mAddCostBean_list = mAddCostBean_list;
        }

        public List<String> getSelected_data() {
            return selected_data;
        }

        public void setSelected_data(List<String> selected_data) {
            this.selected_data = selected_data;
        }

        GridDataAdapter(Context ct, List<AddCostBean> mAddCostBean_list) {
            this.ct = ct;
            this.mAddCostBean_list = mAddCostBean_list;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mAddCostBean_list == null ? 0 : mAddCostBean_list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getSelected() {
            return selected;
        }

        public void setSelected(int selected) {
            this.selected = selected;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridDataAdapter.ViewModle modle = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_simple_text, parent, false);
                modle = new GridDataAdapter.ViewModle();
                modle.tv_text = (TextView) convertView.findViewById(R.id.tv_text);
                convertView.setTag(modle);
            } else {
                modle = (GridDataAdapter.ViewModle) convertView.getTag();
            }
            modle.tv_text.setText(mAddCostBean_list.get(position).getFcs_itemname());
            modle.tv_text.setSelected(false);
            modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.black));
            if (!ListUtils.isEmpty(selected_data)) {
                for (int i = 0; i < getSelected_data().size(); i++) {
                    if (getSelected_data().get(i).equals(mAddCostBean_list.get(position).getFcs_itemname())) {
                        modle.tv_text.setSelected(true);
                        modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.white));
                    }
                }
            }
            return convertView;
        }

        class ViewModle {
            TextView tv_text;
        }
    }

    /**
     * 图片适配器
     */
    private class GridViewAdapter extends BaseAdapter {
        private CostUpdatePModel CUPmodel;

        public CostUpdatePModel getCUPmodel() {
            return CUPmodel;
        }

        public void setCUPmodel(CostUpdatePModel CUPmodel) {
            this.CUPmodel = CUPmodel;
        }

        @Override
        public int getCount() {
            if (mPhotoList.size() >= 9) {
                return 9;
            }
            return mPhotoList.size() + 1;
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
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (mPhotoList.size() == 0) {
                return 1;// View Type 1代表添加更多的视图
            } else if (mPhotoList.size() < 9) {
                if (position < mPhotoList.size()) {
                    return 0;// View Type 0代表普通的ImageView视图
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        }
//        private String getImageUrl2(String path  ) {
//            return CommonUtil.getAppBaseUrl(ct) + "common/download.action?path=" + path + "&sessionId=" +
//                    CommonUtil.getSharedPreferences(ct, "sessionId") +
//                    "&sessionUser=" + CommonUtil.getSharedPreferences(ct, "erp_username") +
//                    "&master=" + CommonUtil.getSharedPreferences(ct, "erp_master");
//        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == 0) {// 普通的视图
                SquareCenterImageView imageView = new SquareCenterImageView(ct);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                String url = mPhotoList.get(position);
                if (url == null) {
                    url = "";
                }
                if (getCUPmodel() != null && !ListUtils.isEmpty(getCUPmodel().getFiles())
                        && position < getCUPmodel().getFiles().size()) {
                    int id = getCUPmodel().getFiles().get(position).getFp_id();
                    String path = getCUPmodel().getFiles().get(position).getFp_path();
                    ImageLoader.getInstance().displayImage(getImageUrl(id), imageView);
                } else {
                    ImageLoader.getInstance().displayImage(Uri.fromFile(new File(url)).toString(), imageView);
                }
                return imageView;
            } else {
                View view = LayoutInflater.from(ct).inflate(R.layout.layout_circle_add_more_item,
                        parent, false);
                ImageView iconImageView = (ImageView) view.findViewById(R.id.icon_image_view);
                TextView voiceTextTv = (TextView) view.findViewById(R.id.text_tv);
                iconImageView.setBackgroundResource(R.drawable.add_picture);
                voiceTextTv.setText(R.string.qzone_add_picture);
                return view;
            }
        }
    }

    private String getImageUrl(int id) {
        return CommonUtil.getAppBaseUrl(ct) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                CommonUtil.getSharedPreferences(ct, "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(ct, "erp_username") +
                "&master=" + CommonUtil.getSharedPreferences(ct, "erp_master");
    }

    /**
     * 通用跳转界面单选方法
     *
     * @param combdatas
     * @param type        2：单选
     * @param title
     * @param requestCode
     */
    private void doSingleSelect(CostSingleBean combdatas, int type, String title, int requestCode) {
        //TODO Datas为接口返回
        if (ListUtils.isEmpty(combdatas.getCombdatas())) return;
        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < combdatas.getCombdatas().size(); i++) {
            selectBean = new SelectBean();
            selectBean.setName(combdatas.getCombdatas().get(i).getDISPLAY());
            formBeaan.add(selectBean);
        }
        Intent intent = new Intent();
        intent.setClass(this, SelectActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, requestCode);
    }

    private void doTypeSingleSelect(List<CostTypeSingleBean> costtypesinglebeanList, int type, String title, int requestCode) {
        //TODO Datas为接口返回
        if (ListUtils.isEmpty(costtypesinglebeanList)) return;

        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < costtypesinglebeanList.size(); i++) {
            selectBean = new SelectBean();
            if (StringUtil.isEmpty(costtypesinglebeanList.get(i).getFk_desc())) {
                selectBean.setName(costtypesinglebeanList.get(i).getFk_name());
            } else {
                selectBean.setName(costtypesinglebeanList.get(i).getFk_name() + "(" + costtypesinglebeanList.get(i).getFk_desc() + ")");
            }
            formBeaan.add(selectBean);
        }
        Intent intent = new Intent();
        intent.setClass(this, SelectActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, requestCode);
    }

    private void initPicturesEvent() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewType = mAdapter.getItemViewType(position);
                int mPosition = (int) parent.getItemIdAtPosition(position);
                if (viewType == 1) {
                    if (PermissionUtil.lacksPermissions(ct, Manifest.permission.CAMERA)) {
                        ToastUtil.showToast(ct, R.string.not_camera_permission);
                        PermissionUtil.requestPermission(ExpenseReimbursementActivity.this, PermissionUtil.DEFAULT_REQUEST, Manifest.permission.CAMERA);

                    } else {
                        showSelectPictureDialog();//添加
                    }
                    //TODO 选择图片页面
//                    startActivityForResult(new Intent(ct, PhoneSelectActivity.class), 222);
                } else {
                    showPictureActionDialog(mPosition);
                }

                Log.i("P_position", mPosition + "");
            }
        });
    }

    private void showSelectPictureDialog() {
        String[] items = new String[]{getString(R.string.c_take_picture), getString(R.string.c_photo_album)};
//        String[] items = new String[]{getString(R.string.c_take_picture)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            try {
                                takePhoto();
                            } catch (Exception e) {
                                String message = e.getMessage();
                                if (!StringUtil.isEmpty(message) && message.contains("Permission")) {
                                    ToastUtil.showToast(ct, R.string.not_system_permission);
                                }
                            }
                        } else {
                            selectPhoto();
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void showPictureActionDialog(final int mPosition) {
        String[] items = new String[]{getString(R.string.look_over), getString(R.string.common_delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.pictures)
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("dP_position", mPosition + "");
                        if (which == 0) {// 查看
                            if (mCostUpdatePModel != null && !ListUtils.isEmpty(mCostUpdatePModel.getFiles())
                                    && mPosition < mCostUpdatePModel.getFiles().size()) {
//                                Toast.makeText(ct,"下载的图片，不可与本地选择的图片滑动查看",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                                intent.putExtra(AppConstant.EXTRA_IMAGE_URI,
                                        getImageUrl(mCostUpdatePModel.getFiles().get(mPosition).getFp_id()));
                                startActivity(intent);

                            } else {
                                Intent intent = new Intent(ct, MultiImagePreviewActivity.class);
                                intent.putExtra(AppConstant.EXTRA_IMAGES, mPhotoList);
                                intent.putExtra(AppConstant.EXTRA_POSITION, mPosition);
                                intent.putExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
                                startActivity(intent);
                            }

                        } else {// 删除
                            deletePhoto(mPosition);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void deletePhoto(final int position) {
        if (!StringUtil.isEmpty(fb_attach)) {
            doDeletePAttach(position);
            LogUtil.prinlnLongMsg("dfb_attach", fb_attach);
        }
        mPhotoList.remove(position);
        mAdapter.notifyDataSetInvalidated();
    }

    /**
     * 删除服务器中传来的图片，需要删除fb_attach中的id；
     *
     * @param fb_attach
     * @param posi
     */
    private List<String> fb_attach_list;

    private void doDeletePAttach(int posi) {
        fb_attach_list = new ArrayList<>();
        String[] split = fb_attach.split(";");
        if (posi < split.length) {
            for (int i = 0; i < split.length; i++) {
                if (i != posi) {
                    fb_attach_list.add(split[i]);
                }

                if (i == split.length - 1) {
                    fb_attach = "";
                    for (int j = 0; j < fb_attach_list.size(); j++) {
                        fb_attach = fb_attach + fb_attach_list.get(j) + ";";

                        if (j == fb_attach_list.size() - 1) {
                            Log.i("now_fb_attach", fb_attach.toString());
                            mCostUpdatePModel.getFiles().remove(posi);
                            mAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    private void takePhoto() {
        try {
            mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().getLoginUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
            CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int Max_Size = 9;

    private void selectPhoto() {
//        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
        Intent intent = new Intent();
        intent.putExtra("MAX_SIZE", Max_Size);
        intent.putExtra("CURRENT_SIZE", mPhotoList == null ? 0 : mPhotoList.size());
        intent.setClass(ct, ImgFileListActivity.class);
        startActivityForResult(intent, 0x01);
    }

    private void putDownInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(add_details_tv.getWindowToken(), 0);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (formid == 0) {
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
            startActivity(new Intent(mContext, FormListSelectActivity.class)
                    .putExtra("caller", caller)
                    .putExtra("title", getToolBarTitle().toString())
            );
        }
        if (item.getItemId() == android.R.id.home) {
//            if (formid != 0){
            finish();
//            }else{
//                startActivity(new Intent(ExpenseReimbursementActivity.this, OAActivity.class)
//                        .putExtra("WorkDailyAdd", "WorkDailyAdd"));
//            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        if (formid != 0){
        finish();
//        }else {
//            startActivity(new Intent(ExpenseReimbursementActivity.this, OAActivity.class)
//                    .putExtra("WorkDailyAdd", "WorkDailyAdd"));
//        }
        super.onBackPressed();
    }

    private void sendPicture() {
        if (ListUtils.isEmpty(mPhotoList)) {
            if (formid != 0) {//TODO 更新操作时可能带有图片过去却被删掉了，做删除图片附件操作，这里直接传空附件既可以了
                doDeleteUp_fb_attach();
            } else {
                judgeApprovers(keyValue);
            }
            return;
        } else {
            LogUtil.prinlnLongMsg("mPhotoListsP", JSON.toJSONString(mPhotoList));
            for (int i = 0; i < mPhotoList.size(); i++) {
                String path = mPhotoList.get(i);
                sendPictureRequest(path, i);
            }
        }

     /*   erhandler.postDelayed(new Runnable() {//仅仅针对弱网下
            @Override
            public void run() {
                if (now_p < mPhotoList.size() -1){
                    ToastMessage(getString(R.string.too_long_to_http));
                    btn_save.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        },15000);*/

    }

    private Boolean platform = ApiUtils.getApiModel() instanceof ApiPlatform;
    private int sended_p = 0;
    private String update = "";

    private void sendPictureRequest(String path, final int now_p) {
        if (StringUtil.isEmpty(path)) return;
        File waterBitmapToFile = new File(path);
        if (!waterBitmapToFile.isFile()) {
            Log.i("now_p", now_p + "");
            if (now_p == mPhotoList.size() - 1) {
                doUpdateId(update);
            } else if (now_p < mPhotoList.size() - 1) {
                return;
            }

        } else {
            File file = ImageUtil.compressBitmapToFile(path, 100, 360, 480);
            RequestParams params = new RequestParams();
            if (platform) {
                params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
                params.addHeader("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
                params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));

            } else {
                params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
                params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(ct, "erp_username"));
            }
            params.addBodyParameter("type", "common");
            params.addBodyParameter("img", file == null ? waterBitmapToFile : file);
            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/uploadEmployeeAttach.action";
            final HttpUtils http = new HttpUtils();
            http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
                @Override
                public void onStart() {
                    progressDialog.show();
                    ViewUtil.ToastMessage(ct, getString(R.string.sending_picture) + "...");
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    if (isUploading) {

                    } else {

                    }
                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    if (JSONUtil.validate(responseInfo.result) && JSON.parseObject(responseInfo.result).getBoolean("success")) {
                        sended_p++;
                        update = update + getID(JSON.parseObject(responseInfo.result).getString("id")) + ";";
                        Log.i("update_cb_attch", update + "");
                        if (now_p == mPhotoList.size() - 1) {
                            //TODO 更新附件ID接口
//                            ViewUtil.ToastMessage(ct, getString(R.string.Uploaded_successfully));
                            doUpdateId(update);
                        }
                    }
                }


                @Override
                public void onFailure(HttpException error, String msg) {
                    ViewUtil.ToastMessage(ct, getString(R.string.common_save_failed) + msg);
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void doDeleteUp_fb_attach() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/attach/change.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("table", "FeePlease");
        params.put("update", "fb_attach='" + "" + "'");  // TODO 附件字段fb_attach
        params.put("condition", "fp_id = '" + formid + "'");
        params.put("type", "删除附件");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        if (platform) {
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        } else {
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        }
        ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x14, null, null, "post");
    }

    private void doUpdateId(String update) {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/attach/change.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("table", "FeePlease");
        if (!StringUtil.isEmpty(fb_attach)) {
            update = fb_attach + update;
        }
        params.put("update", "fb_attach='" + update + "'");  // TODO 附件字段fb_attach
        params.put("condition", "fp_id = '" + keyValue + "'");
        params.put("type", "添加附件");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        if (platform) {
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        } else {
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        }
        ViewUtil.httpSendRequest(ct, url, params, erhandler, headers, 0x07, null, null, "post");
    }

    private int getID(String chche) {
        if (StringUtil.isEmpty(chche)) return 0;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(chche);
        if (m.find()) {
            return Integer.parseInt(m.group(0));
        }
        return -1;
    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
//        for (String permission : permissions) {
//            if (PermissionUtil.lacksPermissions(ct, permission)) {
//                PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permission);
//            }
//        }
//    }

    // 审批人选择操作
    private void judgeApprovers(int keyValue) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        param.put("id", keyValue);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, erhandler, headers, 0x12, null, null, "post");
    }

    private String noid;

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
                commitSuccess(keyValue);
            }
        } else {
            progressDialog.dismiss();
            commitSuccess(keyValue);
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
        ViewUtil.httpSendRequest(ct, url, param, erhandler, headers, 0x13, null, null, "post");
    }

    private void doImageFiltering(ArrayList<String> mPhotoList) {
        for (int i = 0; i < mPhotoList.size(); i++) {
            File file = new File(mPhotoList.get(i).toString());
            if (!file.isFile()) {
//                mPhotoList.remove(i);
                Toast.makeText(ct, "第" + (i + 1) + "张图片格式不对，可能会上传失败，建议更换", Toast.LENGTH_LONG).show();
                mAdapter.notifyDataSetInvalidated();
                break;
            }
            if (i == mPhotoList.size() - 1) {
                mAdapter.notifyDataSetInvalidated();
            }
        }
    }
}
