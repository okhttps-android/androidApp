package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.MyListView;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.model.SelectAimModel;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.CRM.erp.adapter.AddBusinessAdapter;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建商机
 * Created by Bitliker on 2017/5/8.
 */
public class AddBusinessActivity extends OABaseActivity implements View.OnClickListener, OnHttpResultListener {
    private final int LOAD_ID = 0x201;
    private final int LOAD_CODE = 0x202;
    private final int SELECT_COMPANY = 0x761;
    private final int SELECT_REMARK = 0x762;
    private final int SAVE_BUSINESS = 0x763;
    private final int LOAD_BUSINESS_CHANCESTAGE = 0x764;
    private final int UPDATE_BUSINESS = 0x765;
    private final int JUDGE_APPROVERS = 0x766;
    private final int SEND_SELECT = 0x767;
    private final int SELECT_APPROVERS = 0x768;

    private TextView company_tv;
    private TextView company_add_tv;
    private TextView remark_tv;
    private TextView business_stage_tv;//商机阶段
    private TextView business_Library_tv;//商机库
    private MyListView contact_lv;
    private AddBusinessAdapter adapter;
    private LatLng latLng;
    private boolean isUpdata;
    private int bc_id;
    private String bc_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_business);
        initIDS();
        initEvent();
        initView();
    }

    private void initIDS() {
        company_tv = (TextView) findViewById(R.id.company_tv);
        company_add_tv = (TextView) findViewById(R.id.company_add_tv);
        remark_tv = (TextView) findViewById(R.id.remark_tv);
        business_stage_tv = (TextView) findViewById(R.id.business_stage_tv);
        business_Library_tv = (TextView) findViewById(R.id.business_Library_tv);
        contact_lv = (MyListView) findViewById(R.id.contact_lv);
    }

    private void initView() {
      setTitle(R.string.crm_creat_business);
        Intent intent = getIntent();
        isUpdata = intent == null ? false : intent.getBooleanExtra("isUpdata", false);
        if (isUpdata) {
           setTitle(R.string.update_business);
            findViewById(R.id.business_stage_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.business_Library_rl).setVisibility(View.VISIBLE);
            bc_id = intent.getIntExtra("id", 0);
            bc_code = intent.getStringExtra("code");
            company_tv.setText(StringUtil.getMessage(intent.getStringExtra("company")));//企业名称
            company_add_tv.setText(StringUtil.getMessage(intent.getStringExtra("companyAdd")));//企业地址
            remark_tv.setText(StringUtil.getMessage(intent.getStringExtra("remark")));//描述==商机名称
            business_stage_tv.setText(StringUtil.getMessage(intent.getStringExtra("businessStage")));//商机阶段
            business_Library_tv.setText(StringUtil.getMessage(intent.getStringExtra("businessLibrary")));//商机库
            List<EmployeesModel> contacts = intent.getParcelableArrayListExtra("contact");
            adapter = new AddBusinessAdapter(this, contacts);
            company_tv.setFocusable(false);
            company_tv.setOnClickListener(null);
        } else {
            List<EmployeesModel> contacts = new ArrayList<>();
            contacts.add(new EmployeesModel());
            adapter = new AddBusinessAdapter(this, contacts);
        }
        contact_lv.setAdapter(adapter);
    }


    private void initEvent() {
        findViewById(R.id.company_tv).setOnClickListener(this);
        findViewById(R.id.remark_tv).setOnClickListener(this);
        findViewById(R.id.additem_tv).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);
        findViewById(R.id.business_stage_tv).setOnClickListener(this);
        findViewById(R.id.business_Library_tv).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.company_tv) {
            intent = new Intent("com.modular.form.SelectAimActivity").putExtra("title", getString(R.string.select) + getString(R.string.Company_Name));
            startActivityForResult(intent, SELECT_COMPANY);
        } else if (v.getId() == R.id.remark_tv) {
            startActivityForResult(new Intent(ct, SearchSelectActivity.class), AppConstant.RESULT_CODE);
        } else if (v.getId() == R.id.additem_tv) {
            adapter.getContacts().add(new EmployeesModel());
            adapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.submit_btn) {
            if (canSubmit()) {
                if (isUpdata) updateBusiness();
                else
                    getIdByNet();
            }
        } else if (v.getId() == R.id.business_Library_tv) {
            loadBusiness();
        } else if (v.getId() == R.id.business_stage_tv) {
            loadJieDuan();
        }
    }

    private boolean canSubmit() {
        if (TextUtils.isEmpty(company_tv.getText())) {
            ToastUtil.showToast(ct, getString(R.string.Company_Name) + getString(R.string.is_must_input));
            return false;
        } else if (company_tv.getText().length() >= 50) {
            ToastUtil.showToast(ct, getString(R.string.Company_Name) + getString(R.string.more_length));
            return false;
        } else if (!ListUtils.isEmpty(adapter.getContacts())) {
            for (EmployeesModel e : adapter.getContacts()) {
                if (!StringUtil.isEmpty(e.getEmployeeNames()) && e.getEmployeeNames().length() > 30) {
                    ToastUtil.showToast(ct, getString(R.string.common_Contact_person) + getString(R.string.more_length));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            if (requestCode == SEND_SELECT) {
                finish();
            }
            return;
        }
        switch (requestCode) {
            case SELECT_COMPANY:
                SelectAimModel entity = data.getParcelableExtra("data");
                AddBusinessActivity.this.latLng = entity.getLatLng();
                PopupWindowHelper.create(this, getString(R.string.perfect_company_name), entity, new PopupWindowHelper.OnClickListener() {
                    @Override
                    public void result(SelectAimModel model) {
                        company_tv.setText(model.getName());
                        company_add_tv.setText(model.getAddress());
                        if (JSONUtil.validate(model.getObject())) {
                            JSONObject object = JSON.parseObject(model.getObject());
                            String code = JSONUtil.getText(object, "CU_CODE");
                            company_tv.setTag(code);
                        }
                    }
                });
                break;
            case SELECT_REMARK:
                String message = data.getStringExtra("data");
                String remark = StringUtil.isEmpty(message) ? getResources().getString(R.string.maintain_customers) : message;
                remark_tv.setText(remark);
                break;
            case AppConstant.RESULT_CODE:
                if (AppConstant.RESULT_CODE == resultCode) {
                    String result = data.getStringExtra("data");
                    if (!StringUtil.isEmpty(result))
                        remark_tv.setText(result);
                }
                break;
            case 0x24:
                SelectBean businessLibrary = data.getParcelableExtra("data");
                business_Library_tv.setText(businessLibrary.getName());
                break;
            case 0x22:
                SelectBean businessStage = data.getParcelableExtra("data");
                business_stage_tv.setText(businessStage.getName());
                break;
            case SEND_SELECT:
                SelectBean b = data.getParcelableExtra("data");
                String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                getEmnameByReturn(name);
                break;
        }
    }

    /**
     * @desc:加载商机库
     * @author：Arison on 2016/7/20
     */
    public void loadBusiness() {
        HashMap param = new HashMap<>();
        param.put("condition", "1=1");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combos");
        intent.putExtra("showKey", "BD_NAME");
        intent.putExtra("action", "/mobile/crm/getNichehouse.action");
        intent.putExtra("title", getString(R.string.business_Library));
        startActivityForResult(intent, 0x24);
    }

    /*加载阶段*/
    public void loadJieDuan() {
        HashMap param = new HashMap<>();
        param.put("condition", "1=1");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "stages");
        intent.putExtra("showKey", "BS_NAME");
        intent.putExtra("action", "/mobile/crm/getBusinessChanceStage.action");
        intent.putExtra("title", getString(R.string.business_stage));
        startActivityForResult(intent, 0x22);
    }

    private void getIdByNet() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastUtil.showToast(ct, R.string.networks_out);
            return;
        }
        progressDialog.show();
        Map<String, Object> param = new HashMap<>();
        param.put("seq", "BUSINESSCHANCE_SEQ");
        Request request = new Request.Bulider()
                .setWhat(LOAD_ID)
                .setUrl("common/getId.action")
                .setParam(param)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void getCodeByNet(Bundle bundle) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", 2);
        param.put("caller", "BusinessChance");
        Request request = new Request.Bulider()
                .setWhat(LOAD_CODE)
                .setUrl("common/getCodeString.action")
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    private void loadBusinessChanceStage(Bundle bundle) {
        String url = "mobile/crm/getBusinessChanceStage.action";
        Map<String, Object> param = new HashMap<>();
        param.put(" condition", "1=1");
        Request request = new Request.Bulider()
                .setWhat(LOAD_BUSINESS_CHANCESTAGE)
                .setParam(param)
                .setUrl(url)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void saveBusiness(Bundle bundle) {
        String url = "crm/chance/saveBusinessChance.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "BusinessChance");
        param.put("formStore", JSONUtil.map2JSON(getFormStore(false, bundle)));
        Request request = new Request.Bulider()
                .setWhat(SAVE_BUSINESS)
                .setParam(param)
                .setUrl(url)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void submit(final int mBcId) {
        if (mBcId <= 0) return;
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true)
                .connectTimeout(5000)
                .readTimeout(5000)
                .build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("crm/chance/submitBusinessChance.action")
                .add("id", mBcId)
                .add("caller", "BusinessChance")
                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId"))
                .add("master", CommonUtil.getSharedPreferences(ct, "erp_master"))
                .add("sessionUser", CommonUtil.getSharedPreferences(ct, "erp_username"))
                .add("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"))
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object t) {
                String message = t.toString();
                judgeApprovers(mBcId);
            }

            @Override
            public void onFailure(Object t) {
                String message = t.toString();
                ToastUtil.showToast(ct, message);
            }
        }));
    }

    private void judgeApprovers(int mBcId) {
        progressDialog.show();
        String url = "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "BusinessChance");
        param.put("id", mBcId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        Request request = new Request.Bulider()
                .setWhat(JUDGE_APPROVERS)
                .setMode(Request.Mode.POST)
                .setParam(param)
                .setUrl(url)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void updateBusiness() {
        String url = "crm/chance/updateBusinessChance.action";
        Bundle bundle = new Bundle();
        bundle.putString("BS_NAME", StringUtil.getTextRexHttp(business_stage_tv));
        bundle.putString("bc_nichehouse", StringUtil.getTextRexHttp(business_Library_tv));
        bundle.putInt("bc_id", bc_id);
        bundle.putString("bc_code", bc_code);
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "BusinessChance");
        param.put("formStore", JSONUtil.map2JSON(getFormStore(true, bundle)));
        Request request = new Request.Bulider()
                .setWhat(UPDATE_BUSINESS)
                .setParam(param)
                .setBundle(bundle)
                .setUrl(url)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void selectApprovers(String emName) {
        progressDialog.show();
        String url = "common/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", emName);
        params.put("nodeId", noid);
        param.put("_noc", 1);
        param.put("params", JSONUtil.map2JSON(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        Request request = new Request.Bulider()
                .setWhat(SELECT_APPROVERS)
                .setParam(param)
                .setUrl(url)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void saveContact(String bc_code, List<EmployeesModel> contacts) {
        List<Map<String, Object>> formStores = new ArrayList<>();
        String cuname = company_tv.getText().toString();
        String cuCode = (String) company_tv.getTag();
        for (EmployeesModel e : contacts) {
            if (!StringUtil.isEmpty(e.getEmployeeNames()))
                formStores.add(CommonInterface.getInstance().getFormStoreContact(
                        bc_code, e.getEmployeeNames(), e.getEmployeecode(), cuname, cuCode, StringUtil.getTextRexHttp(company_add_tv), ""));
        }
        CommonInterface.getInstance().addContact(formStores, this);
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (!isJSON) return;
        JSONObject object = JSON.parseObject(message);
        if (bundle == null) bundle = new Bundle();
        switch (what) {
            case LOAD_ID:
                int id = JSONUtil.getInt(object, "id");
                bundle.putInt("bc_id", id);
                getCodeByNet(bundle);
                break;
            case LOAD_CODE://获取编号
                String code = object.getString("code");
                bundle.putString("bc_code", code);
                loadBusinessChanceStage(bundle);
                break;
            case SAVE_BUSINESS:
            case UPDATE_BUSINESS:
                progressDialog.dismiss();
                if (object.containsKey("success") && object.getBoolean("success")) {
                    ToastUtil.showToast(ct, R.string.save_success);
//                    finish();
                }
                if (bundle != null) {
                    int bc_id = bundle.getInt("bc_id");
                    submit(bc_id);
                }
                break;
            case LOAD_BUSINESS_CHANCESTAGE:
                JSONArray stages = object.getJSONArray("stages");
                String bs_name = "";
                if (ListUtils.getSize(stages) > 0)
                    bs_name = stages.getJSONObject(0).getString("BS_NAME");
                bundle.putString("BS_NAME", bs_name);
                saveBusiness(bundle);
                break;
            case JUDGE_APPROVERS:
                handlerAssigns(object);
                break;
            case SELECT_APPROVERS:
                progressDialog.dismiss();
                finish();
                Toast.makeText(MyApplication.getInstance(), R.string.submit_success, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        if (!StringUtil.isEmpty(message))
            ToastUtil.showToast(ct, StringUtil.getChinese(message));
        progressDialog.dismiss();
    }

    public Map<String, Object> getFormStore(boolean update, Bundle bundle) {
        String bc_contact = "";
        String bc_tel = "";
        String bc_currentprocess = "";
        String bc_code = "";
        String bc_nichehouse = "";
        int bc_id = 0;
        if (bundle != null) {
            bc_id = bundle.getInt("bc_id");
            bc_code = bundle.getString("bc_code");
            bc_currentprocess = bundle.getString("BS_NAME");
            bc_nichehouse = bundle.getString("bc_nichehouse");
        }
        if (adapter != null && ListUtils.getSize(adapter.getContacts()) > 0) {
            saveContact(bc_code, adapter.getContacts());
            EmployeesModel contact = adapter.getContacts().get(0);
            bc_contact = contact.getEmployeeNames();
            bc_tel = contact.getEmployeecode();
        }
        String name = CommonUtil.getName();
        String company = company_tv.getText().toString();
        String address = company_add_tv.getText().toString();
        String time = DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS);
        String status = "在录入";
        String statusCode = "ENTERING";
//        if (update){
//             status="已审核";
//             statusCode="COMMITED";
//        }
        Map<String, Object> formStore = new HashMap<>();
        formStore.put("bc_id", bc_id);
        formStore.put("bc_code", bc_code);
//        formStore.put("bc_custcode", "");
        formStore.put("bc_doman", StringUtil.toHttpString(name));
        formStore.put("bc_domancode", CommonUtil.getEmcode());
        formStore.put("bc_statuscode", statusCode);
        formStore.put("bc_status", status);
        formStore.put("bc_recorder", name);
        formStore.put("bc_recorddate", time);
        formStore.put("bc_lastdate", time);
        formStore.put("bc_description", StringUtil.toHttpString(remark_tv.getText().toString()));
        formStore.put("bc_custname", StringUtil.toHttpString(company));
        formStore.put("bc_address", StringUtil.toHttpString(address));
        formStore.put("bc_desc6", "");
        if (latLng != null) {
            formStore.put("bc_longitude", latLng.longitude);
            formStore.put("bc_latitude", latLng.latitude);
        }
        formStore.put("bc_contact", StringUtil.toHttpString(bc_contact));
        formStore.put("bc_tel", bc_tel);
        formStore.put("bc_currentprocess", StringUtil.toHttpString(bc_currentprocess));
        formStore.put("bc_remark", StringUtil.toHttpString(remark_tv.getText().toString()));
        formStore.put("bc_date7", DateFormatUtil.long2Str(DateFormatUtil.YMD));
        formStore.put("bc_desc8", "");//成本金额
        formStore.put("bc_desc4", "");//部门
        formStore.put("bc_desc5", "");//岗位
        formStore.put("bc_from", "");// 商机来源
        formStore.put("bc_nichehouse", StringUtil.isEmpty(bc_nichehouse) ? "" : bc_nichehouse);//商机库
        return formStore;
    }

    private void handlerAssigns(JSONObject object) {
        JSONArray assigns = JSONUtil.getJSONArray(object, "assigns");
        if (!ListUtils.isEmpty(assigns)) {
            JSONObject o = assigns.getJSONObject(0);
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
                finish();
            }
        } else {
            progressDialog.dismiss();
            finish();
        }
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
        startActivityForResult(intent, SEND_SELECT);
    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) {
            finish();
            return;
        }
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                selectApprovers(name);
            } else {
                progressDialog.dismiss();
                finish();
            }
        } else {
            progressDialog.dismiss();
            finish();
        }
    }
}
