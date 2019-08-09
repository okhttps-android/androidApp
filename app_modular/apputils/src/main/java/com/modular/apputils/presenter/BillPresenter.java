package com.modular.apputils.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.model.SelectBean;
import com.core.utils.CommonUtil;
import com.core.widget.view.Activity.SelectActivity;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.me.network.app.http.Method;
import com.modular.apputils.R;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.presenter.imp.IBill;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BillPresenter extends BaseNetPresenter implements OnSmartHttpListener {
    public final int LOAD_FORM = 0x11;//获取配置接口
    public final int SAVE_AND_SUBMIT = 0x12;//保存提交接口
    public final int LOAD_JUDGE_APPROVAL = 0x13;//获取审批人员
    public final int SELECT_APPROVAL = 0x14;//选择审批人

    protected IBill iBill;
    protected String mCaller;//当前单据的Caller
    protected int mId;//当前单据拥有的id，新增默认为0   如果mid为-1，说明保存时候使用更新的接口
    protected HashMap<String, String> hashMap;//从外面传进来的默认值

    private String detailKeyField;//从表id字段
    private String keyField;//主表id字段
    private String statusCodeField;//状态码字段
    private String statusField;//状态字段
    private String detailMainKeyField;//从表
    private boolean multidetailgrid = false;

    @Override
    public String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(ct);
    }

    public BillPresenter(Context ct, IBill iBill) {
        super(ct);
        this.iBill = iBill;
    }

    public String getFormCaller() {
        return mCaller;
    }

    public int getFormId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getStatusField() {
        return statusField;
    }


    public void start(Intent intent) {
        if (intent != null) {
            mCaller = intent.getStringExtra(Constants.Intents.CALLER);
            String mTitle = intent.getStringExtra(Constants.Intents.TITLE);
            mId = intent.getIntExtra(Constants.Intents.ID, 0);
            Serializable mSerializable = intent.getSerializableExtra(Constants.Intents.HASH_MAP);
            if (mSerializable != null && mSerializable instanceof HashMap) {
                hashMap = (HashMap<String, String>) mSerializable;
            }
            if (mTitle != null) {
                iBill.setTitle(mTitle);
            }
        }
        loadFormandGridDetail();
    }

    public void saveAndSubmit(List<BillGroupModel.BillModel> formFields, List<List<BillGroupModel.BillModel>> gridBillMap) {
        Map<String, Object> formStore = new HashMap<>();
        for (BillGroupModel.BillModel e : formFields) {
            if (TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 &&
                    ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                iBill.showToast(e.getCaption() + "为必填项");
                iBill.dimssLoading();
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
            iBill.dimssLoading();
            return;
        }

//        String formStoreStr = JSONUtil.map2JSON(formStore);
//        String gridStoreStr = JSONUtil.map2JSON(gridStoreList);
        String formStoreStr = JSON.toJSONString(formStore);
        String gridStoreStr = JSON.toJSONString(gridStoreList);

//        LogUtil.i("gong", "formStoreStr=" + formStoreStr);
//        LogUtil.i("gong", "gridStoreStr=" + gridStoreStr);
        try {
            requestCompanyHttp(new Parameter.Builder()
                            .url(mId == 0 ? "mobile/oa/commonSaveAndSubmit.action" : "mobile/commonUpdate.action")
                            .addParams("caller", mCaller)
                            .mode(Method.POST)
                            .addParams("keyid", String.valueOf(mId))
                            .addParams("formStore", formStoreStr)
                            .addParams("gridStore", gridStoreStr)
                            .record(SAVE_AND_SUBMIT)
                    , this);
        } catch (Exception e) {
            iBill.dimssLoading();
            iBill.showToast("网络请求异常");
        }
    }

    public void saveAndSubmit(List<BillGroupModel.BillModel> formFields, List<BillGroupModel.GridTab> otherGridList, int flag) {
        Map<String, Object> formStore = new HashMap<>();
        for (BillGroupModel.BillModel e : formFields) {
            if (TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 &&
                    ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                iBill.showToast(e.getCaption() + "为必填项");
                iBill.dimssLoading();
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
//                    String otherGridStoreItemStr = JSONUtil.map2JSON(otherGridStoreItem);
                    String otherGridStoreItemStr = JSON.toJSONString(otherGridStoreItem);
                    JSONArray otherGridStoreItemArray = JSON.parseArray(otherGridStoreItemStr);

                    Map<String, Object> OtherGridStoreItemMap = new HashMap<>();
                    OtherGridStoreItemMap.put("dgcaller", otherCaller);
                    OtherGridStoreItemMap.put("dgData", otherGridStoreItemArray);

                    otherGridStoreList.add(OtherGridStoreItemMap);
                }
            }
        }

//        String formStoreStr = JSONUtil.map2JSON(formStore);
//        String otherGridStoreListStr = JSONUtil.map2JSON(otherGridStoreList);
        String formStoreStr = JSON.toJSONString(formStore);
        String otherGridStoreListStr = JSON.toJSONString(otherGridStoreList);
        LogUtil.prinlnLongMsg("billJson", formStoreStr);
        LogUtil.prinlnLongMsg("billJson", otherGridStoreListStr);
        LogUtil.prinlnLongMsg("billJson", mCaller);
        LogUtil.prinlnLongMsg("billJson",String.valueOf(mId));

        try {
            requestCompanyHttp(new Parameter.Builder()
                            .url(mId == 0 ? "mobile/oa/commonSaveAndSubmit.action" : "mobile/commonUpdate.action")
                            .mode(Method.POST)
                            .addParams("caller", mCaller)
                            .addParams("keyid", String.valueOf(mId))
                            .addParams("formStore", formStoreStr)
                            .addParams("othergridStore", otherGridStoreListStr)
                            .record(SAVE_AND_SUBMIT)
                    , this);
        } catch (Exception e) {
            iBill.dimssLoading();
            iBill.showToast("网络请求异常");
        }
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
                        iBill.showToast(billModel.getCaption() + "为必填项");
                        iBill.dimssLoading();
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

    public void saveAndSubmit(List<BillGroupModel> mGroupModels) {
        iBill.showLoading();
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
            saveAndSubmit(formFields, gridBillMap);
        } else {
            saveAndSubmit(formFields, otherGridList, 0);
        }
    }


    private Map<String, Object> getFormStore(List<BillGroupModel.BillModel> formFields) {
        Map<String, Object> formStore = new HashMap<>();
        for (BillGroupModel.BillModel e : formFields) {
            if (TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 && ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                iBill.showToast(e.getCaption() + "为必填项");
                return null;
            }
            formStore.put(e.getField(), e.getValue());
        }
        return formStore;
    }

    private List<Map<String, Object>> getGridStore(Map<String, List<BillGroupModel.BillModel>> gridBillMap) {
        List<Map<String, Object>> gridStoreList = new ArrayList<>();
        for (Map.Entry<String, List<BillGroupModel.BillModel>> e : gridBillMap.entrySet()) {
            if (!ListUtils.isEmpty(e.getValue())) {
                Map<String, Object> gridStore = new HashMap<>();
                for (BillGroupModel.BillModel billModel : e.getValue()) {
                    if (TextUtils.isEmpty(billModel.getValue())
                            && ("necessaryField".equals(billModel.getAllowBlank())
                            || "F".equals(billModel.getAllowBlank())) && billModel.getIsdefault() == -1) {
                        iBill.showToast(billModel.getField() + "为必填项");
                        return null;
                    }
                    gridStore.put(billModel.getField(), billModel.getValue());
                }
                gridStoreList.add(gridStore);
            }
        }
        return gridStoreList;
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
            iBill.showToast("开始上传完成=" + mBillModel.getLength());
            iBill.updateFileOk();
            return;
        }
        iBill.showToast("开始上传附件！！");
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
                iBill.showToast("附件上传失败");
            }
        });

    }


    private void loadFormandGridDetail() {
        iBill.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url(mId <= 0 ? "mobile/common/getformandgriddetail.action" : "mobile/getformandgriddetail.action")
                        .addParams("condition", "1=1")
                        .addParams("caller", mCaller)
                        .addParams("id", mId)
                        .record(LOAD_FORM)
                , this);
    }

    //提交动作，增加判断节点是否有多人的情况
    private void selectApproval(String emName, String jsonObject) {
        JSONObject object = JSON.parseObject(jsonObject);
        String nodeId = JSONUtil.getText(object, "noid");
        String formCode = JSONUtil.getText(object, "formCode");
        int keyValue = JSONUtil.getInt(object, "keyValue");
        iBill.showLoading();
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", emName);
        params.put("nodeId", nodeId);
        requestCompanyHttp(new Parameter.Builder()
                        .url("common/takeOverTask.action")
                        .addParams("_noc", 1)
                        .addTag(TAG_KEY_VALUE, keyValue)
                        .addTag(TAG_FORM_CODE, formCode)
                        .addParams("params", JSONUtil.map2JSON(params))
                        .record(SELECT_APPROVAL)
                , this);
    }

    private final int TAG_KEY_VALUE = 0x21, TAG_FORM_CODE = 0x22;

    private void judgeApproval(int keyValue, String formcode) {
        requestCompanyHttp(new Parameter.Builder()
                        .url("common/getMultiNodeAssigns.action")
                        .addParams("condition", "1=1")
                        .addParams("caller", mCaller)
                        .addTag(TAG_KEY_VALUE, keyValue)
                        .addTag(TAG_FORM_CODE, formcode)
                        .addParams("id", keyValue)
                        .record(LOAD_JUDGE_APPROVAL)
                , this);
    }

    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        JSONObject jsonObject = JSON.parseObject(message);
        switch (what) {
            case LOAD_FORM:
//                handlerBill(jsonObject);
                handlerMultiBill(jsonObject);
                break;
            case SAVE_AND_SUBMIT:
                if (JSONUtil.getBoolean(jsonObject, "success")) {
                    if (mId <= 0) {
                        int keyvalue = JSONUtil.getInt(jsonObject, "keyvalue");
                        String formcode = JSONUtil.getText(jsonObject, "formcode");
                        judgeApproval(keyvalue, formcode);
                        iBill.showToast(R.string.save_success);
                    } else {
                        judgeApproval(mId, "");
                        iBill.showToast(R.string.save_success);
                    }
                }
                break;
            case LOAD_JUDGE_APPROVAL:
                int keyValue = 0;
                String formCode = "";
                if (tag.get(TAG_KEY_VALUE) != null && tag.get(TAG_FORM_CODE) != null) {
                    if (tag.get(TAG_KEY_VALUE) instanceof Integer) {
                        keyValue = (int) tag.get(TAG_KEY_VALUE);
                    }
                    if (tag.get(TAG_FORM_CODE) instanceof String) {
                        formCode = (String) tag.get(TAG_FORM_CODE);
                    }
                }
                if (jsonObject.containsKey("assigns")) {
                    JSONArray array = JSONUtil.getJSONArray(jsonObject, "assigns");
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
                        sendToSelect(keyValue, formCode, noid, data);
                } else {
                    iBill.commitSuccess(keyValue, formCode);
                }
                break;
            case SELECT_APPROVAL:
                keyValue = 0;
                formCode = "";
                if (tag.get(TAG_KEY_VALUE) != null && tag.get(TAG_FORM_CODE) != null) {
                    if (tag.get(TAG_KEY_VALUE) instanceof Integer) {
                        keyValue = (int) tag.get(TAG_KEY_VALUE);
                    }
                    if (tag.get(TAG_FORM_CODE) instanceof String) {
                        formCode = (String) tag.get(TAG_FORM_CODE);
                    }
                }
                iBill.commitSuccess(keyValue, formCode);
                break;
        }
        iBill.dimssLoading();
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {
        iBill.dimssLoading();
        if (JSONUtil.validateJSONObject(message)) {
            iBill.showToast(JSONUtil.getText(message, "exceptionInfo"));
        } else {
            iBill.showToast(message);
        }
        if (message.contains("该任务已经被接管，或该任务不存在!") && what == SELECT_APPROVAL) {
            int keyValue = 0;
            String formCode = "";
            if (tag.get(TAG_KEY_VALUE) != null && tag.get(TAG_FORM_CODE) != null) {
                if (tag.get(TAG_KEY_VALUE) instanceof Integer) {
                    keyValue = (int) tag.get(TAG_KEY_VALUE);
                }
                if (tag.get(TAG_FORM_CODE) instanceof String) {
                    formCode = (String) tag.get(TAG_FORM_CODE);
                }
            }
            iBill.commitSuccess(keyValue, formCode);
        }
    }

    private void sendToSelect(int keyValue, String formCode, String noid, JSONArray data) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        Map<String, Object> map = new HashMap<>();
        map.put("keyValue", keyValue);
        map.put("formCode", formCode);
        map.put("noid", noid);
        String json = JSONUtil.map2JSON(map);
        for (int i = 0; i < data.size(); i++) {
            bean = new SelectBean();
            bean.setName(data.getString(i));
            bean.setJson(json);
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", "选择审批人");
        iBill.startActivityForResult(intent, 0x22);
    }

    public void getEmnameByReturn(String text, String jsonObject) {
        LogUtil.i("gong", "text=" + text + "   ||jsonObject=" + jsonObject);
        if (StringUtil.isEmpty(text)) {
            return;
        }
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                selectApproval(name, jsonObject);
                return;
            }
        }
    }

    private final Comparator<BillGroupModel> mComparator = new Comparator<BillGroupModel>() {
        @Override
        public int compare(BillGroupModel billGroupModel, BillGroupModel t1) {
            int result = 1;
            if (billGroupModel.getMinDetno() - t1.getMinDetno() > 0) {
                result = 1;
            } else if (billGroupModel.getMinDetno() - t1.getMinDetno() < 0) {
                /*int bigIndex = billGroupModel.getGroupIndex();
                billGroupModel.setGroupIndex(t1.getGroupIndex());
                t1.setGroupIndex(bigIndex);*/

                result = -1;
            } else {
                result = 0;
            }
            return result;
        }
    };

    protected void handlerMultiBill(JSONObject resultObject) {
//        String localJson = CommonUtil.getAssetsJson(ct, "bill.json");
//        LogUtil.prinlnLongMsg("billJson", localJson);
        try {
//            JSONObject resultObject = JSON.parseObject(localJson);

            List<BillGroupModel> showBillModels = new ArrayList<>();
            JSONObject config = JSONUtil.getJSONObject(resultObject, "config");
            detailKeyField = JSONUtil.getText(config, "fo_detailkeyfield");
            keyField = JSONUtil.getText(config, "fo_keyfield");
            statusCodeField = JSONUtil.getText(config, "fo_statuscodefield");
            statusField = JSONUtil.getText(config, "fo_statusfield");
            detailMainKeyField = JSONUtil.getText(config, "fo_detailmainkeyfield");
            JSONObject data = JSONUtil.getJSONObject(resultObject, "data");
            JSONArray formdetail = JSONUtil.getJSONArray(data, "formdetail");
            Map<String, BillGroupModel> formdeMap = handlerFormdetail(formdetail);
            List<BillGroupModel> formBillModels = new ArrayList<>();
            if (formdeMap != null && !formdeMap.isEmpty()) {
                for (Map.Entry<String, BillGroupModel> entry : formdeMap.entrySet()) {
                    BillGroupModel entryValue = entry.getValue();
                    if (entryValue != null) {
                        //主表caller为单据caller
                        entryValue.setBillCaller(mCaller);
                        showBillModels.add(entryValue);
                        formBillModels.add(entryValue);
                    }
                }
                if (!ListUtils.isEmpty(formBillModels)) {
                    Collections.sort(showBillModels, mComparator);
                    Collections.sort(formBillModels, mComparator);
                }
            }
            multidetailgrid = JSONUtil.getBoolean(data, "multidetailgrid");
            if (multidetailgrid) {
                JSONArray othergridetail = JSONUtil.getJSONArray(data, "othergridetail");
                if (othergridetail != null && othergridetail.size() > 0) {
                    List<BillGroupModel.GridTab> otherGridTabs = handlerGirdTabs(showBillModels.size() + 1, othergridetail);
                    BillGroupModel.GridTab formGridTab = new BillGroupModel.GridTab();
                    formGridTab.setPosition(0);
                    formGridTab.setTitle("基础信息");
                    formGridTab.setCaller(mCaller);
                    formGridTab.setBillGroupModels(formBillModels);
                    otherGridTabs.add(0, formGridTab);
                    if (otherGridTabs.size() > 0) {
                        BillGroupModel billTab = new BillGroupModel();
                        billTab.setForm(false);
                        billTab.setGridTabs(otherGridTabs);
                        billTab.setGroupIndex(showBillModels.size());

                        showBillModels.add(billTab);

                        showBillModels.addAll(otherGridTabs.get(0).getBillGroupModels());

                        View topLayout = View.inflate(ct, R.layout.item_bill_tab, null);
                        TabLayout tabLayout = topLayout.findViewById(R.id.bill_tab_tl);
                        if (tabLayout != null) {
                            for (int i = 0; i < otherGridTabs.size(); i++) {
                                tabLayout.addTab(tabLayout.newTab().setText(otherGridTabs.get(i).getTitle()));
                            }
                            if (tabLayout.getTabCount() > 4) {
                                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                            } else {
                                tabLayout.setTabMode(TabLayout.MODE_FIXED);
                            }
                        }

                        iBill.addTopLayout(topLayout, tabLayout);
                    }
                }
            } else {
                JSONArray gridetail = JSONUtil.getJSONArray(data, "gridetail");
                Map<String, BillGroupModel> gridGroupModelMap = handlerGridetail(showBillModels.size(), gridetail);
                if (gridGroupModelMap != null && !gridGroupModelMap.isEmpty()) {
                    for (Map.Entry<String, BillGroupModel> entry : gridGroupModelMap.entrySet()) {
                        BillGroupModel entryValue = entry.getValue();
                        if (entryValue != null) {
                            //单从表的情况下，从表caller和主表一样
                            entryValue.setBillCaller(mCaller);
                            showBillModels.add(entryValue);
                        }
                    }
                }
            }
            LogUtil.i("gong", "showBillModels=" + JSON.toJSONString(showBillModels));
            iBill.setAdapter(showBillModels);
        } catch (Exception e) {

        }
    }

    private List<BillGroupModel.GridTab> handlerGirdTabs(int index, JSONArray othergridetail) {
        List<BillGroupModel.GridTab> gridTabs = new ArrayList<>();
        for (int i = 0; i < othergridetail.size(); i++) {
            JSONObject otherGrid = othergridetail.getJSONObject(i);
            if (otherGrid != null) {
                JSONArray detailgrid = otherGrid.getJSONArray("detailgrid");
                if (detailgrid != null && detailgrid.size() > 0) {
                    BillGroupModel.GridTab gridTab = new BillGroupModel.GridTab();
                    String dgcaller = JSONUtil.getText(otherGrid, "dgcaller");
                    gridTab.setTitle(JSONUtil.getText(otherGrid, "dgtitle"));
                    gridTab.setCaller(dgcaller);
                    gridTab.setPosition(i);

                    Map<String, BillGroupModel> gridGroupModelMap = handlerGridetail(index, detailgrid);
                    if (gridGroupModelMap != null && !gridGroupModelMap.isEmpty()) {
                        List<BillGroupModel> billGroupModels = new ArrayList<>();
                        for (Map.Entry<String, BillGroupModel> entry : gridGroupModelMap.entrySet()) {
                            BillGroupModel entryValue = entry.getValue();
                            if (entryValue != null) {
                                //多从表的情况下，从表caller与主表不同
                                entryValue.setBillCaller(dgcaller);
                                billGroupModels.add(entryValue);
                            }
                        }
                        gridTab.setBillGroupModels(billGroupModels);
                    }

                    gridTabs.add(gridTab);
                }
            }
        }
        return gridTabs;
    }

    //处理表单返回配置
    protected void handlerBill(JSONObject object) {
        LogUtil.i("gong", "object=" + object);
        List<BillGroupModel> showBillModels = new ArrayList<>();
        JSONObject config = JSONUtil.getJSONObject(object, "config");
        detailKeyField = JSONUtil.getText(config, "fo_detailkeyfield");
        keyField = JSONUtil.getText(config, "fo_keyfield");
        statusCodeField = JSONUtil.getText(config, "fo_statuscodefield");
        statusField = JSONUtil.getText(config, "fo_statusfield");
        detailMainKeyField = JSONUtil.getText(config, "fo_detailmainkeyfield");
        JSONObject data = JSONUtil.getJSONObject(object, "data");
        JSONArray formdetail = JSONUtil.getJSONArray(data, "formdetail");
        Map<String, BillGroupModel> formdeMap = handlerFormdetail(formdetail);
        if (formdeMap != null && !formdeMap.isEmpty()) {
            for (Map.Entry<String, BillGroupModel> entry : formdeMap.entrySet()) {
                showBillModels.add(entry.getValue());
            }
            if (!ListUtils.isEmpty(showBillModels)) {
                Collections.sort(showBillModels, mComparator);
            }
        }
        JSONArray gridetail = JSONUtil.getJSONArray(data, "gridetail");
        Map<String, BillGroupModel> gridGroupModelMap = handlerGridetail(showBillModels.size(), gridetail);
        if (gridGroupModelMap != null && !gridGroupModelMap.isEmpty()) {
            for (Map.Entry<String, BillGroupModel> entry : gridGroupModelMap.entrySet()) {
                if (entry.getValue() != null) {
                    showBillModels.add(entry.getValue());
                }
            }
        }
        LogUtil.i("gong", "showBillModels=" + JSON.toJSONString(showBillModels));
        iBill.setAdapter(showBillModels);
    }

    /**
     * 获取主表数据包含分组
     *
     * @param formdetail
     * @return
     */
    private Map<String, BillGroupModel> handlerFormdetail(JSONArray formdetail) {
        if (formdetail != null && !formdetail.isEmpty()) {
            JSONObject object = null;
            Map<String, BillGroupModel> modelMap = new LinkedHashMap<>();
            for (int i = 0; i < formdetail.size(); i++) {
                object = formdetail.getJSONObject(i);
                BillGroupModel.BillModel mBillModel = getBillModelByObject(object);
                String group = JSONUtil.getText(object, "fd_group");//是否允许为空(注:当作为标题的时候T:表示可以删除 F:表示不可删除)
                //判断组别
                if (modelMap.containsKey(group)) {
                    BillGroupModel mapBillGroupModel = modelMap.get(group);
                    if (mapBillGroupModel == null) {
                        mapBillGroupModel = new BillGroupModel();
                        mapBillGroupModel.setForm(true);
                        mapBillGroupModel.setGroup(group);
                        mapBillGroupModel.setGroupIndex(modelMap.size());
                        modelMap.put(group, mapBillGroupModel);
                    }
                    mBillModel.setGroupIndex(mapBillGroupModel.getGroupIndex());
                    float minDetno = mapBillGroupModel.getMinDetno();
                    if (minDetno > mBillModel.getDetno()) {
                        minDetno = mBillModel.getDetno();
                    }
                    mapBillGroupModel.setMinDetno(minDetno);
                    if (isShow(mBillModel)) {
                        mapBillGroupModel.addShow(mBillModel);
                    } else {
                        mapBillGroupModel.addHide(mBillModel);
                    }
                } else {
                    BillGroupModel mapBillGroupModel = new BillGroupModel();
                    mapBillGroupModel.setGroup(group);
                    mapBillGroupModel.setForm(true);
                    mapBillGroupModel.setGroupIndex(modelMap.size());
                    modelMap.put(group, mapBillGroupModel);
                    mBillModel.setGroupIndex(mapBillGroupModel.getGroupIndex());
                    if (isShow(mBillModel)) {
                        mapBillGroupModel.addShow(mBillModel);
                    } else {
                        mapBillGroupModel.addHide(mBillModel);
                    }
                }
            }
            return modelMap;
        } else {
            return null;
        }
    }

    /**
     * 获取从表配置
     *
     * @param index
     * @param formdetail
     * @return
     */
    private Map<String, BillGroupModel> handlerGridetail(int index, JSONArray formdetail) {
        if (formdetail != null && !formdetail.isEmpty()) {
            Map<String, BillGroupModel> modelMap = new LinkedHashMap<>();
            if (mId <= 0) {
                BillGroupModel mBillGroupModel = new BillGroupModel();
                mBillGroupModel.setGroupIndex(index);
                mBillGroupModel.setGridIndex(1);
                mBillGroupModel.setGroup("明细1");
                mBillGroupModel.setLastInType(true);
                mBillGroupModel.setForm(false);
                List<BillGroupModel.BillModel> hideBillFields = new ArrayList<>();//当前组隐藏的字段列表
                List<BillGroupModel.BillModel> showBillFields = new ArrayList<>();//当前组显示的字段列表
                for (int i = 0; i < formdetail.size(); i++) {
                    BillGroupModel.BillModel mBillModel = getBillModelByObject(formdetail.getJSONObject(i));
                    mBillModel.setGroupIndex(index);
                    if (isShow(mBillModel)) {
                        showBillFields.add(mBillModel);
                    } else {
                        hideBillFields.add(mBillModel);
                    }
                }
                mBillGroupModel.setHideBillFields(hideBillFields);
                mBillGroupModel.setShowBillFields(showBillFields);

                modelMap.put("明细1", mBillGroupModel);
            } else {
                JSONObject object = null;
                String oldGroup = null;
                for (int i = 0; i < formdetail.size(); i++) {
                    object = formdetail.getJSONObject(i);
                    BillGroupModel.BillModel mBillModel = getBillModelByObject(object);
                    String group = JSONUtil.getText(object, "dg_group");

                    if (modelMap.containsKey("明细" + group)) {
                        BillGroupModel mBillGroupModel = modelMap.get("明细" + group);
                        if (mBillGroupModel == null) {
                            mBillGroupModel = new BillGroupModel();
                            mBillGroupModel.setGroupIndex(index + modelMap.size() - 1);
                            mBillGroupModel.setGridIndex(modelMap.size() + 1);
                            mBillGroupModel.setGroup("明细" + (modelMap.size() + 1));
                            mBillGroupModel.setLastInType(true);
                            mBillGroupModel.setForm(false);

                            if (!TextUtils.isEmpty(oldGroup)) {
                                modelMap.get(oldGroup).setLastInType(false);
                            }
                            oldGroup = "明细" + group;
                            modelMap.put("明细" + group, mBillGroupModel);
                        }

                        mBillModel.setGroupIndex(mBillGroupModel.getGroupIndex());
                        if (isShow(mBillModel)) {
                            mBillGroupModel.addShow(mBillModel);
                        } else {
                            mBillGroupModel.addHide(mBillModel);
                        }
                    } else {
                        BillGroupModel mBillGroupModel = new BillGroupModel();
                        mBillGroupModel.setGroupIndex(index + modelMap.size());
                        mBillGroupModel.setGridIndex(modelMap.size() + 1);
                        mBillGroupModel.setGroup("明细" + (modelMap.size() + 1));
                        mBillGroupModel.setLastInType(true);
                        mBillGroupModel.setForm(false);
                        modelMap.put("明细" + group, mBillGroupModel);

                        mBillModel.setGroupIndex(mBillGroupModel.getGroupIndex());
                        if (isShow(mBillModel)) {
                            mBillGroupModel.addShow(mBillModel);
                        } else {
                            mBillGroupModel.addHide(mBillModel);
                        }

                        if (!TextUtils.isEmpty(oldGroup)) {
                            modelMap.get(oldGroup).setLastInType(false);
                        }
                        oldGroup = "明细" + group;
                    }
                }
            }
            return modelMap;
        } else {
            return null;
        }
    }

    private BillGroupModel.BillModel getBillModelByObject(JSONObject object) {
        BillGroupModel.BillModel mBillModel = new BillGroupModel.BillModel();
        String caption = JSONUtil.getText(object, "fd_caption", "dg_caption");//字段名称
        String value = JSONUtil.getText(object, "fd_value", "dg_value");//字段名称
        float fd_detno = JSONUtil.getFloat(object, "fd_detno");//序号
        int id = JSONUtil.getInt(object, "fd_id", "gd_id");//id
        int length = JSONUtil.getInt(object, "fd_maxlength", "dg_maxlength");//字符长度
        int appwidth = JSONUtil.getInt(object, "fd_appwidth", "dg_appwidth");//宽度
        int isdefault = JSONUtil.getInt(object, "mfd_isdefault", "mdg_isdefault");//是否显示
        String dbfind = JSONUtil.getText(object, "fd_dbfind");//是否是dbfind字段判定
        String type = JSONUtil.getText(object, "fd_type", "dg_type");//类型(标题类型为Constants.TYPE_TITLE,不触发点击事件等 )
        String logicType = JSONUtil.getText(object, "fd_logictype", "dg_logictype");//logic类型
        String readOnly = JSONUtil.getText(object, "fd_readonly");//是否只读
        String field = JSONUtil.getText(object, "fd_field", "dg_field");//字段
        String defValue = JSONUtil.getText(object, "fd_defaultvalue");//默认值
        String allowBlank = JSONUtil.getText(object, "fd_allowblank");//是否允许为空(注:当作为标题的时候T:表示可以删除 F:表示不可删除)
        String findFunctionName = JSONUtil.getText(object, "dg_findfunctionname");
        String updatable = JSONUtil.getText(object, "fd_modify", "dg_modify");

        if (TextUtils.isEmpty(defValue) && hashMap != null && hashMap.containsKey(field)) {
            defValue = hashMap.get(field);
        }
        if (logicType.equals("necessaryField")) {
            allowBlank = "F";
        }
        JSONArray combostore = JSONUtil.getJSONArray(object, "COMBOSTORE");
        if (!ListUtils.isEmpty(combostore)) {
            List<BillGroupModel.LocalData> localDatas = new ArrayList<>();
            for (int i = 0; i < combostore.size(); i++) {
                JSONObject combosModel = combostore.getJSONObject(i);
                BillGroupModel.LocalData mLocalData = new BillGroupModel.LocalData();
                mLocalData.display = JSONUtil.getText(combosModel, "DLC_DISPLAY");
                mLocalData.value = JSONUtil.getText(combosModel, "DLC_VALUE");
                localDatas.add(mLocalData);
            }
            mBillModel.setLocalDatas(localDatas);
        }
        //时间格式，默认添加时间（已取消）
//        if ((type.equals("D") || type.equals("T")) && TextUtils.isEmpty(defValue)) {
//            defValue = DateFormatUtil.long2Str(System.currentTimeMillis() + 1000 * 10 * 60, DateFormatUtil.YMD_HMS);
//            if (caption.contains("生日")) {
//                String day = DateFormatUtil.long2Str(System.currentTimeMillis(), DateFormatUtil.YMD);
//                mBillModel.setValue(day);
//                mBillModel.setDisplay(defValue);
//            }
//        }
        mBillModel.setFindFunctionName(findFunctionName);
        mBillModel.setDetno(fd_detno);
        mBillModel.setCaption(caption);
        mBillModel.setId(id);
        mBillModel.setValue(value);
        mBillModel.setLength(length);
        mBillModel.setAppwidth(appwidth);
        mBillModel.setIsdefault(isdefault);
        mBillModel.setDbfind(dbfind);
        mBillModel.setType(type);
        mBillModel.setLogicType(logicType);
        mBillModel.setReadOnly(readOnly);
        mBillModel.setField(field);
        mBillModel.setDefValue(defValue);
        mBillModel.setAllowBlank(allowBlank);
        mBillModel.setUpdatable(updatable == "T");
        return mBillModel;
    }

    private boolean isShow(BillGroupModel.BillModel mBillModel) {
        return mBillModel.getIsdefault() == -1 && !mBillModel.getType().equals("H");
    }


    public boolean isEnclosureNeedSubmit(BillGroupModel.BillModel billModel) {
        return billModel.getType().equals("FF") && TextUtils.isEmpty(billModel.getValue()) && !ListUtils.isEmpty(billModel.getLocalDatas());
    }
}
