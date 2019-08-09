package com.uas.appworks.crm3_0.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.file.FileUtils;
import com.common.ui.CameraUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseToolBarActivity;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.model.SelectAimModel;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.SelectNetAcitivty;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.modular.apputils.widget.BillUpdatePopup;
import com.modular.apputils.widget.VeriftyDialog;
import com.uas.appworks.R;
import com.uas.appworks.adapter.BillListDetailsAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerVisitDetailsActivity extends OABaseActivity implements BillUpdatePopup.OnUpdateSelectListener {
    private final int LOAD_FORM = 0x11;
    private final int TURN_VISIT_RECORD = 0x12;
    private final int UN_SUBMIT = 0x13;
    private final int SAVE_OUT_ADDRESS = 0x14;

    public final int REQUESTCODE_C = 0x21;
    public final int REQUESTCODE_DB_FIND = 0x22;
    public final int REQUESTCODE_ENCLOSURE = 0x23;
    public final int REQUESTCODE_C_NET = 0x24;
    public final int REQUESTCODE_ENCLOSURE_LOW = 0x25;
    public final int REQUEST_CODE_CAPTURE_PHOTO = 0x26;
    public final int REQUEST_CODE_SELECT_MORE = 0x27;

    private final int TAG_ADDRESS_SELECT = 0x652;

    private ListView mListView;
    private String mCaller;
    private int mId;
    private UUHttpHelper mUUHttpHelper;
    private BillListDetailsAdapter mListAdapter;
    private TextView statusTv;
    private String status;
    private boolean isMe;
    private String mBillStatus;
    private List<BillGroupModel> mBillGroupModels;
    private BillUpdatePopup mBillUpdatePopup;
    public int selectPosition = -1;
    private List<BillGroupModel> mUpdateGroupModels = new ArrayList<>();
    private SelectAimModel address;//当前地址

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (TextUtils.isEmpty(mBillStatus)) {
            return super.onPrepareOptionsMenu(menu);
        } else if ("已审核".equals(mBillStatus)) {
            if (TextUtils.isEmpty(status) || (!"已完成".equals(status) && !"已转单".equals(status))) {
                if (mListAdapter != null && !ListUtils.isEmpty(mListAdapter.getUpdateBillModels())) {
                    getMenuInflater().inflate(com.modular.apputils.R.menu.menu_input_update, menu);
                }
            }
        } else {
            getMenuInflater().inflate(com.modular.apputils.R.menu.menu_input_edit, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (com.modular.apputils.R.id.edit == item.getItemId()) {
            new VeriftyDialog.Builder(ct)
                    .setTitle(getString(com.modular.apputils.R.string.app_name))
                    .setContent("是否确定反提交该单据?")
                    .build(new VeriftyDialog.OnDialogClickListener() {
                        @Override
                        public void result(boolean clickSure) {
                            if (clickSure) {
                                unSubmit();
                            }
                        }
                    });
        } else if (item.getItemId() == R.id.update) {
            if (mListAdapter != null && !ListUtils.isEmpty(mListAdapter.getUpdateBillModels())) {
                try {
                    mUpdateGroupModels = CommonUtil.deepCopy(mListAdapter.getGroupModels());
                } catch (Exception e) {
                    mUpdateGroupModels = mListAdapter.getGroupModels();
                    e.printStackTrace();
                }
                mBillUpdatePopup.setGroupModels(mUpdateGroupModels).showPopupWindow();
            } else {
                toast(R.string.no_fields_to_update);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void unSubmit() {
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/commonres.action")
                        .addParams("caller", mCaller)
                        .addParams("id", mId)
                        .record(UN_SUBMIT)
                , mOnSmartHttpListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_visit_details);
        initView();
    }

    private void initView() {
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        mListView = findViewById(R.id.mListView);
        statusTv = findViewById(R.id.statusTv);
        mBillUpdatePopup = new BillUpdatePopup(mContext, this);
        start(getIntent());
    }

    public void start(Intent intent) {
        if (intent != null) {
            mCaller = intent.getStringExtra(Constants.Intents.CALLER);
            status = intent.getStringExtra(Constants.Intents.STATUS);
            isMe = intent.getBooleanExtra(Constants.Intents.MY_DOIT, false);
            String mTitle = intent.getStringExtra(Constants.Intents.TITLE);
            mId = intent.getIntExtra(Constants.Intents.ID, 0);
            if (mTitle != null) {
                setTitle(mTitle);
            }
        }
        if (TextUtils.isEmpty(status) || (!"已完成".equals(status) && !"已转单".equals(status))) {
            statusTv.setText("完成拜访");
            statusTv.setTextColor(0xFF2F98F9);
            statusTv.setOnClickListener(mOnClickListener);
        } else {
            statusTv.setText("已完成拜访");
            statusTv.setTextColor(0xFF666666);
            statusTv.setOnClickListener(null);
        }
        loadFormandGridDetail();
    }

    private void loadFormandGridDetail() {
        showLoading();
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/common/getformandgriddata.action")
                        .addParams("caller", mCaller)
                        .addParams("id", mId)
                        .record(LOAD_FORM)
                , mOnSmartHttpListener);
    }

    private void turnVisitRecord() {
        showLoading();
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/turnVisitRecord.action")
                        .addParams("mpId", mId)
                        .addParams("caller", mCaller)
                        .mode(Method.POST)
                        .record(TURN_VISIT_RECORD)
                , mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject jsonObject = JSON.parseObject(message);
            switch (what) {
                case LOAD_FORM:
                    handlerFormData(JSONUtil.getJSONObject(jsonObject, "datas"));
                    break;
                case TURN_VISIT_RECORD:
                    JSONObject data = JSONUtil.getJSONObject(jsonObject, "data");
                    String errMsg = JSONUtil.getText(data, "errMsg");
                    if (!TextUtils.isEmpty(errMsg)) {
                        showToast(errMsg);
                    } else {
                        int vrId = JSONUtil.getInt(data, "vrId");
                        showToast("转单成功！！");
                        startActivity(new Intent(ct, VisitRecordBillInputActivity.class)
                                .putExtra(Constants.Intents.CALLER, "VisitRecord")
                                .putExtra(Constants.Intents.TITLE, "拜访记录")
                                .putExtra(Constants.Intents.ID, vrId));
                        finish();
                    }
                    break;
                case UN_SUBMIT:
                    showToast("反提交成功");
                    startActivity(new Intent(ct, CustomerVisitBillInputActivity.class)
                            .putExtra(Constants.Intents.CALLER, mCaller)
                            .putExtra(Constants.Intents.DETAILS_CLASS, CustomerVisitDetailsActivity.class)
                            .putExtra(Constants.Intents.INPUT_CLASS, CustomerVisitBillInputActivity.class)
                            .putExtra(Constants.Intents.TITLE, ((BaseToolBarActivity) ct).getToolBarTitle())
                            .putExtra(Constants.Intents.ID, mId)
                    );
                    finish();
                    break;
                case SAVE_OUT_ADDRESS:
                    break;
            }
            dimssLoading();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            dimssLoading();
            if (JSONUtil.validateJSONObject(message)) {
                showToast(JSONUtil.getText(message, "exceptionInfo"));
            } else {
                showToast(message);
            }

        }
    };

    private void handlerFormData(JSONObject datas) throws Exception {
        mBillGroupModels = new ArrayList<>();
        //配置主表
        JSONArray formdatas = JSONUtil.getJSONArray(datas, "formdata");
        if (!ListUtils.isEmpty(formdatas)) {
            JSONObject formdata = formdatas.getJSONObject(0);//主表数据
            mBillStatus = JSONUtil.getText(formdata, "mp_status");

            JSONArray formconfigs = JSONUtil.getJSONArray(datas, "formconfigs");//主表配置
            BillGroupModel mGroupModel = new BillGroupModel();
            mGroupModel.setForm(true);
            mGroupModel.setDeleteAble(false);
            mGroupModel.setGroupIndex(0);
            mGroupModel.setGroup(" ");
            handlerModelByObject(mGroupModel, formconfigs, formdata);
            mBillGroupModels.add(mGroupModel);
        }
        JSONArray gridconfigs = JSONUtil.getJSONArray(datas, "gridconfigs");
        if (!ListUtils.isEmpty(gridconfigs)) {
            String myEmCode = CommonUtil.getEmcode();
            JSONArray griddatas = JSONUtil.getJSONArray(datas, "griddata");
            for (int i = 0; i < griddatas.size(); i++) {
                JSONObject griddata = griddatas.getJSONObject(i);
                BillGroupModel mGroupModel = new BillGroupModel();
                mGroupModel.setForm(false);
                mGroupModel.setGroupIndex(i + 1);
                mGroupModel.setGroup("明细表" + mGroupModel.getGroupIndex());
                mGroupModel.setDeleteAble(false);
                handlerModelByObject(mGroupModel, gridconfigs, griddata);
                mBillGroupModels.add(mGroupModel);
                String perCode = JSONUtil.getText(griddata, "mpd_personnum");
                /*if (perCode.equals(myEmCode)) {
                    String status = JSONUtil.getText(griddata, "mpd_status");
                    if (TextUtils.isEmpty(status) || !"已完成".equals(status)) {
                        statusTv.setText("完成拜访");
                        statusTv.setTextColor(0xFF2F98F9);
                        statusTv.setOnClickListener(mOnClickListener);
                    } else {
                        statusTv.setText("已完成拜访");
                        statusTv.setTextColor(0xFF666666);
                        statusTv.setOnClickListener(null);
                    }
                }*/
            }
        }
        setAdapter(mBillGroupModels);

        //刷新optionsmenu
        supportInvalidateOptionsMenu();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (R.id.statusTv == view.getId()) {
                new VeriftyDialog.Builder(ct)
                        .setTitle(getString(R.string.app_name))
                        .setContent("是否将当前拜访计划生成拜访报告单？")
                        .build(new VeriftyDialog.OnDialogClickListener() {
                            @Override
                            public void result(boolean clickSure) {
                                if (clickSure) {
                                    turnVisitRecord();
                                }
                            }
                        });
            }
        }
    };

    /**
     * 根据跟进人人员编号判断当前单据状态
     *
     * @param myEmCode
     * @param perCode
     */
    private void handlerEmcode(String myEmCode, String perCode) {

    }

    private void handlerModelByObject(BillGroupModel mGroupModel, JSONArray configs, JSONObject object) {
        for (int i = 0; i < configs.size(); i++) {
            JSONObject config = configs.getJSONObject(i);
            if (config == null) continue;
            String caption = JSONUtil.getText(config, "FD_CAPTION", "DG_CAPTION");//获取第一个字段字段名称
            String field = JSONUtil.getText(config, "FD_FIELD", "DG_FIELD");//字段名称
            String type = JSONUtil.getText(config, "FD_TYPE", "DG_TYPE");
            String dbFind = JSONUtil.getText(config, "FD_DBFIND", "DG_TYPE");
            int isdefault = JSONUtil.getInt(config, "MFD_ISDEFAULT", "MDG_ISDEFAULT");
            int appwidth = JSONUtil.getInt(config, "FD_APPWIDTH", "DG_APPWIDTH");
            String findFunctionName = JSONUtil.getText(config, "DG_FINDFUNCTIONNAME");
            String allowBlank = JSONUtil.getText(config, "FD_ALLOWBLANK", "DG_ALLOWBLANK");
            String logicType = JSONUtil.getText(config, "FD_LOGICTYPE", "DG_LOGICTYPE");
            String updatable = JSONUtil.getText(config, "FD_MODIFY", "DG_MODIFY");

            JSONArray combostore = JSONUtil.getJSONArray(config, "COMBOSTORE");//本地值
            BillGroupModel.BillModel mBillModel = new BillGroupModel.BillModel();
            mBillModel.setFindFunctionName(findFunctionName);
            mBillModel.setCaption(caption);
            mBillModel.setAppwidth(appwidth);
            mBillModel.setIsdefault(isdefault);
            mBillModel.setDbfind(dbFind);
            mBillModel.setGroupIndex(mGroupModel.getGroupIndex());
            if ("mp_address".equals(field)) {
                mBillModel.setType("DF");
            } else {
                mBillModel.setType(type);
            }
            mBillModel.setLogicType(logicType);
            mBillModel.setField(field);
            mBillModel.setAllowBlank(allowBlank);
            mBillModel.setValue(JSONUtil.getText(object, field));
            mBillModel.setUpdatable("T".equals(updatable));

            if (!ListUtils.isEmpty(combostore)) {
                List<BillGroupModel.LocalData> localDatas = new ArrayList<>();
                for (int j = 0; j < combostore.size(); j++) {
                    JSONObject combosModel = combostore.getJSONObject(j);
                    BillGroupModel.LocalData mLocalData = new BillGroupModel.LocalData();
                    mLocalData.display = JSONUtil.getText(combosModel, "DLC_DISPLAY");
                    mLocalData.value = JSONUtil.getText(combosModel, "DLC_VALUE");
                    localDatas.add(mLocalData);
                }
                mBillModel.setLocalDatas(localDatas);
            }
            if (mBillModel.getIsdefault() == -1 && !mBillModel.getType().equals("H")) {
                mGroupModel.addShow(mBillModel);

                if ("T".equals(updatable)) {
                    mGroupModel.addUpdate(mBillModel);
                }
            } else {
                mGroupModel.addHide(mBillModel);
            }

        }
    }

    public void setAdapter(List<BillGroupModel> groupModels) {
        if (mListAdapter == null) {
            mListAdapter = new BillListDetailsAdapter(ct, groupModels);
            mListView.setAdapter(mListAdapter);
        } else {
            mListAdapter.updateGroupModels(groupModels);
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onUpdateSelect(int position, BillGroupModel.BillModel model) {
        if (("mp_address".equals(model.getField()))) {
            selectPosition = model.getGroupIndex();
            Intent intent = new Intent("com.modular.form.SelectAimActivity");
            startActivityForResult(intent, TAG_ADDRESS_SELECT);
            return;
        }
        if (TextUtils.isEmpty(model.getType())) {
            return;
        }
        if (!ListUtils.isEmpty(model.getLocalDatas())) {
            //本地数据不为空的情况下
            selectByLocal(position, model);
        } else {
            //本地数据为空，获取网络数据
            switch (model.getType()) {
                case "C"://单项选择
                    getComboValue(position, model);
                    break;
                case "DT"://时间选择
                    showDateDialog(true, position);
                    break;
                case "D"://日期选择
                case "T"://时间选择
                    showDateDialog(false, position);
                    break;
                case "SF":
                case "DF":
                    //DBFind 选择
                    findBydbFind(model);
                    break;
                case "MF":
                    //多选
                    findMore(position, model);
                    break;

            }
        }
    }

    @Override
    public void onUpdateEnclosure(int position, BillGroupModel.BillModel model) {
        selectPosition = position;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            toSelectEnclosure();
        } else {
            showSelectPictureDialog(model);
        }
    }

    @Override
    public void onUpdateConfirm(List<BillGroupModel> billGroupModels, List<BillGroupModel.BillModel> updateBillModels) {
        mUpdateGroupModels = billGroupModels;
        if (!CommonUtil.isRepeatClick(4000)) {
            submitBill();
        }
//        saveAndSubmit(billGroupModels);
    }

    private void submitBill() {
        String mp_cuname = "";
        String mp_address = "";
        List<BillGroupModel.BillModel> showBillModels = mBillUpdatePopup.getBillUpdateAdapter().getShowBillModels();
        for (BillGroupModel.BillModel billModel : showBillModels) {
            if ("mp_address".equals(billModel.getField())) {
                mp_address = billModel.getValue();

                if (StringUtil.isEmpty(mp_address) && billModel.isUpdatable()) {
                    toast("当前地址不能为空");
                    return;
                }
            }
            if (StringUtil.hasOneEqual(billModel.getField(), "mp_cuname", "mp_xmmc_user")) {
                mp_cuname = billModel.getValue();
            }
        }


//        if (!StringUtil.isEmpty(mp_cuname) || !StringUtil.isEmpty(mp_address)) {
        if (address == null) {
            address = new SelectAimModel();
        }
        address.setName(mp_cuname);
        address.setAddress(mp_address);

        saveAddressAndSubmit();
    }

    private void saveAddressAndSubmit() {
        List<BillGroupModel> billGroupModels = mBillUpdatePopup.getGroupModels();
        /*if (address == null || address.getLatLng() == null) {
            toast("地址信息为空，请重新选择当前地址");
            return;
        }*/

        BillGroupModel latLngGroupModel = new BillGroupModel();
        latLngGroupModel.setForm(true);

        BillGroupModel.BillModel deviceModel = new BillGroupModel.BillModel();
        deviceModel.setField("mp_sourceequipment");
        deviceModel.setDisplay("Android");
        latLngGroupModel.addHide(deviceModel);
        if (address != null && address.getLatLng() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("company", address.getName());
            map.put("companyAddress", address.getAddress());
            map.put("visitTime", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
            map.put("latitude", address.getLatLng().latitude);
            map.put("longitude", address.getLatLng().longitude);
            saveOutAddress(map);


            BillGroupModel.BillModel latBillModel = new BillGroupModel.BillModel();
            latBillModel.setField("mp_latitude");
            latBillModel.setDisplay(address.getLatLng().latitude + "");
            BillGroupModel.BillModel lngBillModel = new BillGroupModel.BillModel();

            lngBillModel.setField("mp_longitude");
            lngBillModel.setDisplay(address.getLatLng().longitude + "");

            latLngGroupModel.addHide(latBillModel);
            latLngGroupModel.addHide(lngBillModel);
        }
        billGroupModels.add(latLngGroupModel);

        saveAndSubmit(billGroupModels);
    }

    //保存外勤计划目的地
    private void saveOutAddress(Map<String, Object> tagMap) {
        String company = (String) tagMap.get("company");
        String companyAddress = (String) tagMap.get("companyAddress");
        String visitTime = (String) tagMap.get("visitTime");
        double longitude = (double) tagMap.get("longitude");
        double latitude = (double) tagMap.get("latitude");

        Map<String, Object> formStore = new HashMap<>();
        formStore.put("Md_company", company);//拜访公司
        if (companyAddress.length() >= 200) {
            companyAddress = companyAddress.substring(0, 190);
        }
        formStore.put("Md_address", companyAddress);//拜访地址
        formStore.put("Md_visitcount", 1);//固定为1，由后台加1
        formStore.put("Md_visittime", visitTime);//预计时间
        formStore.put("Md_longitude", longitude);//经度
        formStore.put("Md_latitude", latitude);//纬度
        String emcode = CommonUtil.getEmcode();
        formStore.put("Md_emcode", emcode);//纬度
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/addOutAddress.action")
                        .addParams("caller", "lp")
                        .mode(Method.POST).record(SAVE_OUT_ADDRESS)
                        .addParams("formStore", JSONUtil.map2JSON(formStore))
                , mOnSmartHttpListener
        );
    }

    public void saveAndSubmit(List<BillGroupModel> mGroupModels) {
        showLoading();
        List<BillGroupModel.BillModel> formFields = new ArrayList<>();
        List<List<BillGroupModel.BillModel>> gridBillMap = new ArrayList<>();
        List<BillGroupModel.GridTab> otherGridList = new ArrayList<>();
        for (BillGroupModel e : mGroupModels) {
            if (e.isForm()) {
                if (e.getShowBillFields() != null) {
                    formFields.addAll(e.getShowBillFields());
                }
                if (e.getHideBillFields() != null) {
                    formFields.addAll(e.getHideBillFields());
                }

            } else {
                List<BillGroupModel.GridTab> gridTabs = e.getGridTabs();
                if (gridTabs != null && gridTabs.size() > 0) {
                    otherGridList = gridTabs;
                    break;
                }
                List<BillGroupModel.BillModel> gridBillFields = new ArrayList<>();
                if (e.getShowBillFields() != null) {
                    gridBillFields.addAll(e.getShowBillFields());
                }
                if (e.getHideBillFields() != null) {
                    gridBillFields.addAll(e.getHideBillFields());
                }
                gridBillMap.add(gridBillFields);
            }
        }
        if (otherGridList == null || otherGridList.size() <= 0) {
            saveAndSubmit(mGroupModels, formFields, gridBillMap);
        } else {
//            saveAndSubmit(formFields, otherGridList, 0);
        }
    }

    public void saveAndSubmit(final List<BillGroupModel> mGroupModels, List<BillGroupModel.BillModel> formFields, List<List<BillGroupModel.BillModel>> gridBillMap) {
        Map<String, Object> formStore = new HashMap<>();
        for (BillGroupModel.BillModel e : formFields) {
            if (e.isUpdatable() && TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 &&
                    ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                toast(e.getCaption() + "为必填项");
                dimssLoading();
                return;
            }
            if (isEnclosureNeedSubmit(e)) {
                pushEnclosure(e);
                return;
            }
            formStore.put(e.getField(), e.getDisplay());
        }

        List<Map<String, Object>> gridStoreList = analysisGrid(gridBillMap);
        if (gridStoreList == null) {
            return;
        }
        if (formStore == null || gridStoreList == null) {
            dimssLoading();
            return;
        }

        String formStoreStr = JSONUtil.map2JSON(formStore);
        String gridStoreStr = JSONUtil.map2JSON(gridStoreList);

        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        String emCode = CommonUtil.getEmcode();
        com.me.network.app.http.HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(mContext),
                new HttpParams.Builder()
                        .url("common/mobile/modifydata.action")
                        .flag(0)
                        .method(Method.POST)
                        .addParam("caller", mCaller)
                        .addParam("formStore", formStoreStr)
                        .addParam("gridStore", gridStoreStr)
                        .addParam("sessionId", sessionId)
                        .addParam("sessionUser", emCode)
                        .addHeader("sessionUser", emCode)
                        .addHeader("Cookie", "JSESSIONID=" + sessionId)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        dimssLoading();
                        setAdapter(mGroupModels);

                        Toast.makeText(ct, R.string.update_success, Toast.LENGTH_SHORT).show();

                        if (mBillUpdatePopup != null) {
                            mBillUpdatePopup.dismiss();
                        }

                        loadFormandGridDetail();
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        progressDialog.dismiss();
                        Toast.makeText(ct, failStr, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void saveAndSubmit(List<BillGroupModel.BillModel> formFields, List<BillGroupModel.GridTab> otherGridList, int flag) {
        Map<String, Object> formStore = new HashMap<>();
        for (BillGroupModel.BillModel e : formFields) {
            if (TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 &&
                    ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                toast(e.getCaption() + "为必填项");
                progressDialog.dismiss();
                return;
            }
            if (isEnclosureNeedSubmit(e)) {
                pushEnclosure(e);
                return;
            }
            formStore.put(e.getField(), e.getDisplay());
        }

        List<Map<String, Object>> otherGridStoreList = new ArrayList<>();
        for (BillGroupModel.GridTab gridTab : otherGridList) {
            if (gridTab != null) {
                List<BillGroupModel> tabGroupModels = gridTab.getBillGroupModels();
                String otherCaller = gridTab.getCaller();
                if (tabGroupModels != null && tabGroupModels.size() > 0) {
                    List<List<BillGroupModel.BillModel>> otherGridMap = new ArrayList<>();
                    for (BillGroupModel tabGroupModel : tabGroupModels) {
                        List<BillGroupModel.BillModel> otherGrid = new ArrayList<>();
                        List<BillGroupModel.BillModel> otherShowFields = tabGroupModel.getShowBillFields();
                        List<BillGroupModel.BillModel> otherHideFields = tabGroupModel.getHideBillFields();

                        if (otherShowFields != null) {
                            otherGrid.addAll(otherShowFields);
                        }
                        if (otherHideFields != null) {
                            otherGrid.addAll(otherHideFields);
                        }
                        otherGridMap.add(otherGrid);
                    }
                    List<Map<String, Object>> otherGridStoreItem = analysisGrid(otherGridMap);
                    if (otherGridStoreItem == null) {
                        return;
                    }
                    String otherGridStoreItemStr = JSONUtil.map2JSON(otherGridStoreItem);
                    JSONArray otherGridStoreItemArray = JSON.parseArray(otherGridStoreItemStr);

                    Map<String, Object> OtherGridStoreItemMap = new HashMap<>();
                    OtherGridStoreItemMap.put("dgcaller", otherCaller);
                    OtherGridStoreItemMap.put("dgData", otherGridStoreItemArray);

                    otherGridStoreList.add(OtherGridStoreItemMap);
                }
            }
        }

        String formStoreStr = JSONUtil.map2JSON(formStore);
        String otherGridStoreListStr = JSONUtil.map2JSON(otherGridStoreList);
        LogUtil.prinlnLongMsg("billJson", formStoreStr);
        LogUtil.prinlnLongMsg("billJson", otherGridStoreListStr);

        /*requestCompanyHttp(new Parameter.Builder()
                        .url(mId == 0 ? "mobile/oa/commonSaveAndSubmit.action" : "mobile/commonUpdate.action")
                        .mode(Method.POST)
                        .addParams("caller", mCaller)
                        .addParams("keyid", String.valueOf(mId))
                        .addParams("formStore", formStoreStr)
                        .addParams("othergridStore", otherGridStoreListStr)
                        .record(SAVE_AND_SUBMIT)
                , this);*/
    }


    @Nullable
    private List<Map<String, Object>> analysisGrid(List<List<BillGroupModel.BillModel>> gridBillMap) {
        List<Map<String, Object>> gridStoreList = new ArrayList<>();
        for (List<BillGroupModel.BillModel> e : gridBillMap) {
            if (!ListUtils.isEmpty(e)) {
                Map<String, Object> gridStore = new HashMap<>();
                for (BillGroupModel.BillModel billModel : e) {
                    if (TextUtils.isEmpty(billModel.getValue())
                            && ("necessaryField".equals(billModel.getAllowBlank())
                            || "F".equals(billModel.getAllowBlank())) && billModel.getIsdefault() == -1) {
                        toast(billModel.getCaption() + "为必填项");
                        progressDialog.dismiss();
                        return null;
                    }
                    if (isEnclosureNeedSubmit(billModel)) {
                        pushEnclosure(billModel);
                        return null;
                    }
                    gridStore.put(billModel.getField(), billModel.getDisplay());
                }
                gridStoreList.add(gridStore);
            }
        }
        return gridStoreList;
    }

    public boolean isEnclosureNeedSubmit(BillGroupModel.BillModel billModel) {
        return billModel.getType().equals("FF") && TextUtils.isEmpty(billModel.getValue()) && !ListUtils.isEmpty(billModel.getLocalDatas());

    }

    public void pushEnclosure(BillGroupModel.BillModel mBillModel) {
        mBillModel.setLength(0);
        uploadFile(mBillModel);
    }

    //上传文件
    private void uploadFile(final BillGroupModel.BillModel mBillModel) {
        String path = null;
        if (mBillModel.getLength() < ListUtils.getSize(mBillModel.getLocalDatas())) {
            //没有上传完成
            BillGroupModel.LocalData data = mBillModel.getLocalDatas().get(mBillModel.getLength());
            path = data.display;
        } else {
            toast("开始上传完成=" + mBillModel.getLength());
            progressDialog.dismiss();
            saveAndSubmit(mListAdapter.getGroupModels());
            return;
        }
        toast("开始上传附件！！");
        File mFile = new File(path);
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
        params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.addBodyParameter("type", "common");
        params.addBodyParameter("file", mFile);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/uploadAttachs.action";
        final HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                LogUtil.i("gong", "result-" + result);
                if (JSONUtil.validate(result) && JSON.parseObject(result).getBoolean("success")) {
                    mBillModel.setLength(mBillModel.getLength() + 1);
                    JSONObject resultObject = JSON.parseObject(result);
                    String phoneId = JSONUtil.getText(resultObject, "id");
                    mBillModel.setValue(mBillModel.getValue() + "" + StringUtil.getLastBracket(phoneId) + ";");
                    uploadFile(mBillModel);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                toast("附件上传失败");
            }
        });

    }

    private void toSelectEnclosure() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUESTCODE_ENCLOSURE);
    }

    private void toSelectEnclosure(BillGroupModel.BillModel model) {
        Intent intent = new Intent(ct, ImgFileListActivity.class);
        intent.putExtra("MAX_SIZE", 9);
        intent.putExtra("CURRENT_SIZE", ListUtils.getSize(model.getLocalDatas()));
        startActivityForResult(intent, REQUESTCODE_ENCLOSURE_LOW);
    }

    private void showSelectPictureDialog(final BillGroupModel.BillModel model) {
        String[] items = new String[]{getString(com.modular.apputils.R.string.c_take_picture), getString(com.modular.apputils.R.string.c_photo_album)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            try {
                                takePhoto();
                            } catch (Exception e) {
                                String message = e.getMessage();
                                if (!StringUtil.isEmpty(message) && message.contains("Permission")) {
                                    ToastUtil.showToast(ct, com.modular.apputils.R.string.not_system_permission);
                                }
                            }
                        } else {
                            toSelectEnclosure(model);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private Uri mNewPhotoUri;

    private void takePhoto() throws Exception {
        Uri mNewPhotoUri = CameraUtil.getOutputMediaFileUri(mContext, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        if (mNewPhotoUri != null) {
            CameraUtil.captureImage((Activity) ct,
                    mNewPhotoUri,
                    REQUEST_CODE_CAPTURE_PHOTO);
        } else {
            ToastUtil.showToast(this, "uri is null");
        }
    }

    private void findMore(int position, BillGroupModel.BillModel model) {
        HashMap param = new HashMap<>();
        String[] fields = new String[]{"sa_custname"};
        BillGroupModel mGroupModel = mListAdapter.getBillGroupModel(model.getGroupIndex());
        boolean isForm = mGroupModel == null || mGroupModel.isForm();

        param.put("caller", mCaller);
        param.put("page", "1");
        param.put("which", "form");
        param.put("condition", "1=1");
        param.put("pageSize", "1000");
        param.put("field", model.getField());
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("isSingle", false);
        intent.putExtra("reid", com.modular.apputils.R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combdatas");
        intent.putExtra("showKey", model.getField());
        intent.putExtra("fields", fields);
        intent.putExtra("action", "common/dbfind.action");
        intent.putExtra("title", model.getCaption());
        intent.putExtra("id", model.getGroupIndex());
        intent.putExtra("isForm", isForm);
        startActivityForResult(intent, REQUEST_CODE_SELECT_MORE);
    }


    /**
     * 选择本地数据选项
     *
     * @param position 索引
     * @param model    点击对象
     */
    public void selectByLocal(int position, BillGroupModel.BillModel model) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean;
        for (BillGroupModel.LocalData localData : model.getLocalDatas()) {
            bean = new SelectBean();
            bean.setJson(localData.display);
            bean.setShowName(localData.value);
            bean.setName(localData.value);
            bean.setIndex(position);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", model.getCaption());
        startActivityForResult(intent, REQUESTCODE_C);
    }

    public void getComboValue(int position, BillGroupModel.BillModel model) {
        HashMap param = new HashMap<>();
        param.put("caller", mCaller);
        param.put("field", model.getField());
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", com.modular.apputils.R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combdatas");
        intent.putExtra("showKey", "DISPLAY");
        intent.putExtra("action", "mobile/common/getComboValue.action");
        intent.putExtra("title", model.getCaption());
        intent.putExtra("id", position);//需要把zum
        startActivityForResult(intent, REQUESTCODE_C_NET);
    }

    /**
     * 选择日期
     */
    public void showDateDialog(final boolean needTime, final int position) {
        DateTimePicker picker = new DateTimePicker(this, needTime ? DateTimePicker.HOUR_OF_DAY : DateTimePicker.YEAR_MONTH_DAY);
        picker.setRange(CalendarUtil.getYear() - 100, CalendarUtil.getYear() + 20);
        if (needTime) {
            picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay(), CalendarUtil.getHour(), CalendarUtil.getMinute());
        } else {
            picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        }
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                String date = year + "-" + month + "-" + day;
                String dateTime = date + " " + hour + ":" + minute + ":00";
                mBillUpdatePopup.updateBillModelValues(position, needTime ? dateTime : date, needTime ? dateTime : date);
            }
        });
        picker.setOnCancelListener(new DateTimePicker.OnCancelListener() {
            @Override
            public void onCancel() {
                mBillUpdatePopup.updateBillModelValues(position, "", "");
            }
        });
        picker.show();
    }

    /**
     * dbfind 查找
     *
     * @param model
     */
    public void findBydbFind(BillGroupModel.BillModel model) {
        String gridCaller = "";
        String fieldKey = model.getField();
        BillGroupModel mGroupModel = mListAdapter.getBillGroupModel(model.getGroupIndex());
        boolean isForm = mGroupModel == null || mGroupModel.isForm();
        if (!isForm && !StringUtil.isEmpty(model.getFindFunctionName())) {
            String[] mFindFunctionNames = model.getFindFunctionName().split("\\|");
            if (mFindFunctionNames != null && mFindFunctionNames.length > 1) {
                gridCaller = mFindFunctionNames[0];
//                fieldKey = mFindFunctionNames[1];
            }
        }
        String mCondition = "";
        /*if (dbFindCondition != null && dbFindCondition.containsKey(fieldKey)) {
            mCondition = dbFindCondition.get(fieldKey);
        }*/
        startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                        .putExtra("fieldKey", fieldKey)
                        .putExtra("caller", mGroupModel == null ? mCaller
                                : mGroupModel.getBillCaller())
                        .putExtra("gCaller", gridCaller)
                        .putExtra("mCondition", mCondition)
                        .putExtra("isDevice", false)
                        .putExtra("isForm", isForm)
                        .putExtra("groupId", model.getGroupIndex())
                , REQUESTCODE_DB_FIND);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        switch (requestCode) {
            case REQUEST_CODE_SELECT_MORE:
                if (resultCode == 0x21) {//多选框
                    int groupIndex = data.getIntExtra("id", 0);
                    boolean isForm = data.getBooleanExtra("isForm", true);
                    ArrayList<SelectBean> muliData = data.getParcelableArrayListExtra("data");
                    JSONObject object = null;
                    if (!ListUtils.isEmpty(muliData)) {
                        object = JSON.parseObject(muliData.get(0).getJson());
                        String dbFind = muliData.get(0).getDbfinds();
                        for (int i = 1; i < muliData.size(); i++) {
                            SelectBean b = muliData.get(i);
                            JSONObject bObject = JSON.parseObject(b.getJson());
                            for (Map.Entry<String, Object> entry : bObject.entrySet()) {
                                String oldText = object.getString(entry.getKey());
                                object.put(entry.getKey(), oldText + "#" + entry.getValue());
                            }
                        }
                        if (!TextUtils.isEmpty(dbFind)) {
                            JSONArray fbFindArray = JSON.parseArray(dbFind);
                            if (!ListUtils.isEmpty(fbFindArray)) {
                                for (int i = 0; i < fbFindArray.size(); i++) {
                                    String dbGridField = JSONUtil.getText(fbFindArray.getJSONObject(i), "dbGridField");
                                    String field = JSONUtil.getText(fbFindArray.getJSONObject(i), "field");
                                    if (object.containsKey(dbGridField)) {
                                        object.put(field, object.get(dbGridField));
                                        object.remove(dbGridField);
                                    }
                                }
                            }
                        }
                    }
//                    mBillAdapter.updateBillModelValues(id, values, values);
                    LogUtil.i("gong", "object=" + object);
                    LogUtil.i("gong", "groupIndex=" + groupIndex);
                    handlerSelectDbFind(object, groupIndex, isForm);
                }
                break;
            case REQUESTCODE_C_NET:
                SelectBean mSelectBeanC = data.getParcelableExtra("data");
                int position = data.getIntExtra("id", -1);
                if (mSelectBeanC != null && position >= 0) {
                    String display = StringUtil.isEmpty(mSelectBeanC.getName()) ? "" : mSelectBeanC.getName();
                    String value = StringUtil.isEmpty(mSelectBeanC.getName()) ? "" : mSelectBeanC.getName();
                    mBillUpdatePopup.updateBillModelValues(position, value, display);
                }
                break;
            case REQUESTCODE_C:
                mSelectBeanC = data.getParcelableExtra("data");
                if (mSelectBeanC != null) {
                    position = mSelectBeanC.getIndex();
                    String value = StringUtil.isEmpty(mSelectBeanC.getShowName()) ? "" : mSelectBeanC.getShowName();
                    String display = StringUtil.isEmpty(mSelectBeanC.getJson()) ? "" : mSelectBeanC.getJson();
                    mBillUpdatePopup.updateBillModelValues(position, value, display);
                }
                break;
            case REQUESTCODE_DB_FIND:
                String json = data.getStringExtra("data");
                int groupIndex = data.getIntExtra("groupId", 0);
                boolean isForm = data.getBooleanExtra("isForm", true);
                handlerSelectDbFind(JSON.parseObject(json), groupIndex, isForm);
                break;
            case REQUESTCODE_ENCLOSURE:
                if (resultCode == Activity.RESULT_OK) {
                    String path;
                    Uri uri = data.getData();
                    if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                        path = uri.getPath();
                    } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                        path = FileUtils.getPath(this, uri);
                    } else {//4.4以下下系统调用方法
                        path = FileUtils.getRealPathFromURI(this, uri);
                    }
                    LogUtil.i("gong", "uri=" + uri.toString());
                    File file = null;
                    if (!TextUtils.isEmpty(path)) {
                        file = new File(path);
                    }
                    if (file != null && file.exists() && file.isFile()) {
                        String value = file.getName();
                        String display = path;
                        mBillUpdatePopup.getBillUpdateAdapter().addBillModelData(selectPosition, value, display);
                        selectPosition = -1;
                    }

                }
            case REQUEST_CODE_CAPTURE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (mNewPhotoUri != null) {
                        String value = mNewPhotoUri.getPath();
                        String display = value;
                        mBillUpdatePopup.getBillUpdateAdapter().addBillModelData(selectPosition, value, display);
                        selectPosition = -1;
                    } else {
                        ToastUtil.showToast(this, com.modular.apputils.R.string.c_take_picture_failed);
                    }
                }
                break;
            case REQUESTCODE_ENCLOSURE_LOW:
                List<String> filePaths = data.getStringArrayListExtra("files");
                if (!ListUtils.isEmpty(filePaths)) {
                    File file = null;
                    List<BillGroupModel.LocalData> localDatas = new ArrayList<>();
                    for (String filePath : filePaths) {
                        if (!TextUtils.isEmpty(filePath)) {
                            file = new File(filePath);
                        }
                        if (file != null && file.exists() && file.isFile()) {
                            BillGroupModel.LocalData localData = new BillGroupModel.LocalData();
                            localData.value = file.getName();
                            localData.display = filePath;
                            localDatas.add(localData);
                        }
                    }
                    mBillUpdatePopup.getBillUpdateAdapter().addBillModelData(selectPosition, localDatas);
                    selectPosition = -1;
                }
                break;
            case TAG_ADDRESS_SELECT:
                SelectAimModel chcheAimModel = data.getParcelableExtra("data");
                sureSelectAim(chcheAimModel);
                break;
        }
    }

    private void sureSelectAim(SelectAimModel entity) {
        if (entity == null || selectPosition < 0 || selectPosition > ListUtils.getSize(mBillUpdatePopup.getGroupModels())) {
            selectPosition = -1;
            return;
        }
        String company = StringUtil.isEmpty(entity.getName()) ? "" : entity.getName();
        String companyAddress = StringUtil.isEmpty(entity.getAddress()) ? "" : entity.getAddress();
        BillGroupModel mBillGroupModel = mBillUpdatePopup.getBillGroupModel(selectPosition);
        if (mBillGroupModel != null) {
            if (!ListUtils.isEmpty(mBillGroupModel.getShowBillFields())) {
                for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
                    if ("mp_address".equals(e.getField()) || "当前地址".equals(e.getCaption())) {
                        e.setValue(companyAddress);
                        if (address == null) {
                            address = new SelectAimModel();
                        }
                        address.setLatLng(entity.getLatLng());
                    }
                }
            }
            if (!ListUtils.isEmpty(mBillGroupModel.getHideBillFields())) {
                for (BillGroupModel.BillModel e : mBillGroupModel.getHideBillFields()) {
                    if ("mp_address".equals(e.getField()) || "当前地址".equals(e.getCaption())) {
                        e.setValue(companyAddress);
                        if (address == null) {
                            address = new SelectAimModel();
                        }
                        address.setLatLng(entity.getLatLng());
                    }
                }
            }
        }
        mBillUpdatePopup.notifyDataSetChanged();
    }

    protected void handlerSelectDbFind(JSONObject object, int groupId, boolean isForm) {
        List<BillGroupModel> groupModels = mListAdapter.getGroupModels();
        if (isForm) {
            List<BillGroupModel.BillModel> formBillModels = mBillUpdatePopup.getBillUpdateAdapter().getFormBillModels();

            if (!ListUtils.isEmpty(formBillModels)) {
                for (BillGroupModel.BillModel billModel : formBillModels) {
                    if (object.containsKey(billModel.getField())) {
                        billModel.setValue(JSONUtil.getText(object, billModel.getField()));
                    }
                }
            }
        } else {
            BillGroupModel mBillGroupModel = mBillUpdatePopup.getBillUpdateAdapter().getBillGroupModel(groupId);
            if (mBillGroupModel != null) {
                if (!ListUtils.isEmpty(mBillGroupModel.getShowBillFields())) {
                    for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
                        if (object.containsKey(e.getField())) {
                            e.setValue(JSONUtil.getText(object, e.getField()));
                        }
                    }
                }
                if (!ListUtils.isEmpty(mBillGroupModel.getHideBillFields())) {
                    for (BillGroupModel.BillModel e : mBillGroupModel.getHideBillFields()) {
                        if (object.containsKey(e.getField())) {
                            e.setValue(JSONUtil.getText(object, e.getField()));
                        }
                    }
                }
            }
        }

        List<BillGroupModel> groupModels1 = mListAdapter.getGroupModels();

        mBillUpdatePopup.getBillUpdateAdapter().notifyDataSetChanged();
    }

}
