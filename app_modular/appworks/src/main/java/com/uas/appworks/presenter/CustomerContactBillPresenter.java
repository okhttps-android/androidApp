package com.uas.appworks.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.presenter.BillPresenter;
import com.modular.apputils.presenter.imp.IBill;
import com.uas.appworks.presenter.imp.IContact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerContactBillPresenter extends BillPresenter {
    private IContact iContact;
    private final int LOAD_FORM_INPUT = 0x31;
    private final int LOAD_FORM_DETAILS = 0x32;
    private final int DELETE_DETAIL = 0x33;
    private final int TAG_DELETE_INDEX = 0x41;
    private final int LOAD_FILE_PATHS = 0x42;
    private final int TAG_GROUP_INDEX = 0x43;


    public CustomerContactBillPresenter(Context ct, IBill iBill, IContact iContact) {
        super(ct, iBill);
        this.iContact = iContact;
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
        if (mId > 0) {
            loadDetails();
        } else {
            loadFormandGridData();
        }
    }


    private void loadFormandGridData() {
        iBill.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url(mId <= 0 ? "mobile/common/getformandgriddetail.action" : "mobile/getformandgriddetail.action")
                        .addParams("condition", "1=1")
                        .addParams("caller", mCaller)
                        .addParams("id", mId)
                        .record(LOAD_FORM_INPUT)
                , this);
    }

    public void updateId(int mId) {
        this.mId = mId;
        loadDetails();
    }

    private void loadDetails() {
        iBill.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/common/getformandgriddata.action")
                        .addParams("caller", mCaller)
                        .addParams("id", mId)
                        .record(LOAD_FORM_DETAILS)
                , this);
    }

    @Override
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

        List<Map<String, Object>> gridStoreList = new ArrayList<>();
        for (List<BillGroupModel.BillModel> e : gridBillMap) {
            if (!ListUtils.isEmpty(e)) {
                Map<String, Object> gridStore = new HashMap<>();
                for (BillGroupModel.BillModel billModel : e) {
                    if (TextUtils.isEmpty(billModel.getValue())
                            && ("necessaryField".equals(billModel.getAllowBlank())
                            || "F".equals(billModel.getAllowBlank())) && billModel.getIsdefault() == -1) {
                        iBill.showToast(billModel.getField() + "为必填项");
                        iBill.dimssLoading();
                        return;
                    }
                    if (isEnclosureNeedSubmit(billModel)) {
                        pushEnclosure(billModel);
                        return;
                    }
                    gridStore.put(billModel.getField(), billModel.getDisplay());
                }
                if (!gridStore.containsKey("ct_id")) {
                    gridStore.put("ct_id", "");
                }
                if (!gridStore.containsKey("ct_cuid")) {
                    gridStore.put("ct_cuid", mId);
                }
                gridStoreList.add(gridStore);
            }
        }
        if (formStore == null || gridStoreList == null) {
            iBill.dimssLoading();
            return;
        }
        requestCompanyHttp(new Parameter.Builder()
                        .url(mId == 0 ? "mobile/oa/commonSaveAndSubmit.action" : "mobile/commonUpdate.action")
                        .addParams("caller", mCaller)
                        .mode(Method.POST)
                        .addParams("keyid", String.valueOf(mId))
                        .addParams("formStore", JSONUtil.map2JSON(formStore))
                        .addParams("gridStore", JSONUtil.map2JSON(gridStoreList))
                        .record(SAVE_AND_SUBMIT)
                , this);
    }

    /*获取附件  */
    private void loadFilePaths(int mGroupIndex, String attachs) {
        if (StringUtil.isEmpty(attachs) || "null".equals(attachs)) {
            return;
        }
        iBill.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("common/getFilePaths.action")
                        .addParams("field", "fb_attach")
                        .addParams("id", attachs)
                        .addTag(TAG_GROUP_INDEX, mGroupIndex)
                        .record(LOAD_FILE_PATHS)
                , this);
    }

    public void deleteDetail(int groupIndex, int ctId) {
        iBill.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("common/deleteDetail.action")
                        .addParams("caller", mCaller)
                        .addParams("gridcaller", mCaller)
                        .mode(Method.POST)
                        .addTag(TAG_DELETE_INDEX, groupIndex)
                        .addParams("condition", "ct_id=" + ctId)
                        .record(DELETE_DETAIL)
                , this);
    }

    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        switch (what) {
            case LOAD_FORM_INPUT:
                handlerMultiBill(JSON.parseObject(message));
                break;
            case LOAD_FORM_DETAILS:
                handlerFormData(JSONUtil.getJSONObject(message, "datas"));
                break;
            case DELETE_DETAIL:
                if (tag.get(TAG_DELETE_INDEX) != null && tag.get(TAG_DELETE_INDEX) instanceof Integer) {
                    int deleteIndex = (int) tag.get(TAG_DELETE_INDEX);
                    iContact.deleteDetailOk(deleteIndex);
                }
                break;
            case LOAD_FILE_PATHS:
                int mGroupIndex = 0;
                if (tag != null && tag.get(TAG_GROUP_INDEX) != null && tag.get(TAG_GROUP_INDEX) instanceof Integer) {
                    mGroupIndex = (int) tag.get(TAG_GROUP_INDEX);
                }
                handlerEnclosure(mGroupIndex, JSONUtil.getJSONArray(JSON.parseObject(message), "files"));
                break;
            default:
                super.onSuccess(what, message, tag);

        }
        iBill.dimssLoading();
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {
        if (LOAD_FILE_PATHS != what) {
            super.onFailure(what, message, tag);
        }
    }

    private void handlerEnclosure(final int mGroupIndex, final JSONArray array) throws Exception {
        List<BillGroupModel.LocalData> mLocalDatas = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                if (o == null) continue;
                BillGroupModel.LocalData mLocalData = new BillGroupModel.LocalData();
                int id = JSONUtil.getInt(o, "fp_id");
                mLocalData.value = JSONUtil.getText(o, "fp_name");
                mLocalData.display = getImageUrl(id);
                mLocalDatas.add(mLocalData);
            }
        }
        if (!ListUtils.isEmpty(mLocalDatas)) {
            iContact.setFilePaths(mGroupIndex, mLocalDatas);
        }
    }


    private String getImageUrl(int id) {
        return CommonUtil.getAppBaseUrl(MyApplication.getInstance()) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username") +
                "&master=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
    }

    private void handlerFormData(JSONObject datas) throws Exception {
        String status = null;
        List<BillGroupModel> mBillGroupModels = new ArrayList<>();
        //配置主表
        JSONArray formdatas = JSONUtil.getJSONArray(datas, "formdata");
        if (!ListUtils.isEmpty(formdatas)) {
            JSONObject formdata = formdatas.getJSONObject(0);//主表数据
            JSONArray formconfigs = JSONUtil.getJSONArray(datas, "formconfigs");//主表配置
            BillGroupModel mGroupModel = new BillGroupModel();
            mGroupModel.setForm(true);
            mGroupModel.setDeleteAble(false);
            mGroupModel.setGroupIndex(0);
            mGroupModel.setGroup(" ");
            handlerModelByObject(mGroupModel, formconfigs, formdata);
            status = JSONUtil.getText(formdata, "cu_auditstatus");
            mBillGroupModels.add(mGroupModel);
        }
        JSONArray gridconfigs = JSONUtil.getJSONArray(datas, "gridconfigs");
        if (!ListUtils.isEmpty(gridconfigs)) {
            JSONArray griddatas = JSONUtil.getJSONArray(datas, "griddata");
            for (int i = 0; i < griddatas.size(); i++) {
                JSONObject griddata = griddatas.getJSONObject(i);
                BillGroupModel mGroupModel = new BillGroupModel();
                mGroupModel.setForm(false);
                mGroupModel.setGroupIndex(i + 1);
                mGroupModel.setGroup("明细表" + mGroupModel.getGroupIndex());
                mGroupModel.setDeleteAble(i != 0);
                handlerModelByObject(mGroupModel, gridconfigs, griddata);
                mGroupModel.setLastInType(i == griddatas.size() - 1);

                mBillGroupModels.add(mGroupModel);
            }
        }
        if (!TextUtils.isEmpty(status)) {
            iContact.updateStatus(status);
        }
        iBill.setAdapter(mBillGroupModels);
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
            JSONArray combostore = JSONUtil.getJSONArray(config, "COMBOSTORE");//本地值
            BillGroupModel.BillModel mBillModel = new BillGroupModel.BillModel();
            mBillModel.setFindFunctionName(findFunctionName);
            mBillModel.setCaption(caption);
            mBillModel.setAppwidth(appwidth);
            mBillModel.setGroupIndex(mGroupModel.getGroupIndex());
            mBillModel.setIsdefault(isdefault);
            mBillModel.setDbfind(dbFind);
            mBillModel.setType(type);
            mBillModel.setLogicType(logicType);
            mBillModel.setField(field);
            mBillModel.setAllowBlank(allowBlank);
            mBillModel.setValue(JSONUtil.getText(object, field));
            if ("ct_attach".equals(mBillModel.getField())) {
                mBillModel.setType("FF");
            }
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
                if ("FF".equals(mBillModel.getType()) && !StringUtil.isEmpty(mBillModel.getValue())) {
                    loadFilePaths(mBillModel.getGroupIndex(), mBillModel.getValue());
                }
                mGroupModel.addShow(mBillModel);
            } else {
                mGroupModel.addHide(mBillModel);
            }

        }
    }

}
