package com.modular.apputils.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseToolBarActivity;
import com.core.utils.CommonUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.apputils.R;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.model.BillJump;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.presenter.imp.IBillDetails;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BillDetailsPresenter extends BaseNetPresenter {
    private final int LOAD_FORM = 0x11;//获取配置接口
    private final int LOAD_FILE_PATHS = 0x12;//获取附件详情
    private final int UN_SUBMIT = 0x13;//反提交

    protected IBillDetails mIBillDetails;
    protected String mCaller;//当前单据的Caller
    protected int mId;//当前单据拥有的id，新增默认为0
    protected Class mInputClass;
    protected List<BillJump> mBillJumps;

    private String detailKeyField;//从表id字段
    private String keyField;//主表id字段
    private String statusCodeField;//状态码字段
    private String statusField;//状态字段
    private String detailMainKeyField;//从表
    private boolean multidetailgrid = false;
    private String mStatusKey;

    public String getCaller() {
        return mCaller;
    }

    public Class getInputClass() {
        return mInputClass;
    }

    public BillDetailsPresenter(Context ct, IBillDetails mIBillDetails) {
        super(ct);
        this.mIBillDetails = mIBillDetails;
    }

    @Override
    public String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(ct);
    }

    public void start(Intent intent) {
        if (intent != null) {
            mCaller = intent.getStringExtra(Constants.Intents.CALLER);
            String mTitle = intent.getStringExtra(Constants.Intents.TITLE);
            mId = intent.getIntExtra(Constants.Intents.ID, 0);
            Serializable mInputSerializable = intent.getSerializableExtra(Constants.Intents.INPUT_CLASS);
            if (mInputSerializable != null && mInputSerializable instanceof Class) {
                mInputClass = (Class) mInputSerializable;
            }
            mBillJumps = intent.getParcelableArrayListExtra(Constants.Intents.BILL_JUMPS);
            if (mTitle != null) {
                mIBillDetails.setTitle(mTitle);
            }
        }
        loadFormandGridDetail();
    }

    public int getId() {
        return mId;
    }

    public void unSubmit(String status) {
        LogUtil.i("gong", "status=" + status);
        if (status != null && status.equals("在录入")) {
            ct.startActivity(new Intent(ct, mInputClass == null ? BillInputActivity.class : mInputClass)
                    .putExtra(Constants.Intents.CALLER, mCaller)
                    .putExtra(Constants.Intents.TITLE, ((BaseToolBarActivity) ct).getToolBarTitle())
                    .putExtra(Constants.Intents.ID, mId)
            );
            mIBillDetails.finish();
        } else {
            mIBillDetails.showLoading();
            requestCompanyHttp(new Parameter.Builder()
                            .url("mobile/commonres.action")
                            .addParams("caller", mCaller)
                            .addParams("id", mId)
                            .record(UN_SUBMIT)
                    , mOnSmartHttpListener);
        }

    }

    private void loadFormandGridDetail() {
        mIBillDetails.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/getformandgriddetail.action")
                        .addParams("condition", "1=1")
                        .addParams("caller", mCaller)
                        .addParams("id", mId)
                        .record(LOAD_FORM)
                , mOnSmartHttpListener);
    }

    /*获取附件  */
    private void loadFilePaths(String attachs) {
        if (StringUtil.isEmpty(attachs) || "null".equals(attachs)) {
            return;
        }
        mIBillDetails.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("common/getFilePaths.action")
                        .addParams("field", "fb_attach")
                        .addParams("id", attachs)
                        .record(LOAD_FILE_PATHS)
                , mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject jsonObject = JSON.parseObject(message);
            switch (what) {
                case LOAD_FORM:
//                    handlerFormData(JSONUtil.getJSONObject(jsonObject, "datas"));
                    handlerMultiBill(jsonObject);
                    break;
                case UN_SUBMIT:
                    mIBillDetails.showToast("反提交成功");
                    ct.startActivity(new Intent(ct, mInputClass == null ? BillInputActivity.class : mInputClass)
                            .putExtra(Constants.Intents.CALLER, mCaller)
                            .putExtra(Constants.Intents.TITLE, ((BaseToolBarActivity) ct).getToolBarTitle())
                            .putExtra(Constants.Intents.ID, mId)
                    );
                    mIBillDetails.finish();
                    break;
                case LOAD_FILE_PATHS:
                    handlerEnclosure(JSONUtil.getJSONArray(jsonObject, "files"));
                    break;

            }
            mIBillDetails.dimssLoading();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            mIBillDetails.dimssLoading();
            if (JSONUtil.validateJSONObject(message)) {
                mIBillDetails.showToast(JSONUtil.getText(message, "exceptionInfo"));
            } else {
                mIBillDetails.showToast(message);
            }

        }
    };

    private void handlerEnclosure(final JSONArray array) throws Exception {
        List<BillGroupModel.LocalData> mLocalDatas = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                if (o == null) {
                    continue;
                }
                BillGroupModel.LocalData mLocalData = new BillGroupModel.LocalData();
                int id = JSONUtil.getInt(o, "fp_id");
                mLocalData.value = JSONUtil.getText(o, "fp_name");
                mLocalData.display = getImageUrl(id);
                mLocalDatas.add(mLocalData);
            }
        }
        if (!ListUtils.isEmpty(mLocalDatas)) {
            mIBillDetails.setFilePaths(mLocalDatas);
        }
    }


    private String getImageUrl(int id) {
        return CommonUtil.getAppBaseUrl(MyApplication.getInstance()) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username") +
                "&master=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
    }

    private final Comparator<BillGroupModel> mComparator = new Comparator<BillGroupModel>() {
        @Override
        public int compare(BillGroupModel billGroupModel, BillGroupModel t1) {
            return (billGroupModel.getMinDetno() > t1.getMinDetno()) ? 1 : -1;
        }
    };

    private void handlerMultiBill(JSONObject resultObject) {
        try {
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
            if (formdeMap != null && !formdeMap.isEmpty()) {
                for (Map.Entry<String, BillGroupModel> entry : formdeMap.entrySet()) {
                    showBillModels.add(entry.getValue());
                }
                if (!ListUtils.isEmpty(showBillModels)) {
                    Collections.sort(showBillModels, mComparator);
                }
            }
            multidetailgrid = JSONUtil.getBoolean(data, "multidetailgrid");
            if (multidetailgrid) {
                JSONArray othergridetail = JSONUtil.getJSONArray(data, "othergridetail");
                if (othergridetail != null && othergridetail.size() > 0) {
                    List<BillGroupModel.GridTab> otherGridTabs = handlerGirdTabs(showBillModels.size() + 1, othergridetail);
                    if (otherGridTabs.size() > 0) {
                        BillGroupModel billTab = new BillGroupModel();
                        billTab.setForm(false);
                        billTab.setGridTabs(otherGridTabs);
                        billTab.setGroupIndex(showBillModels.size());

                        showBillModels.add(billTab);

                        showBillModels.addAll(otherGridTabs.get(0).getBillGroupModels());
                    }
                }
            } else {
                JSONArray gridetail = JSONUtil.getJSONArray(data, "gridetail");
                Map<String, BillGroupModel> gridGroupModelMap = handlerGridetail(showBillModels.size(), gridetail);
                if (gridGroupModelMap != null && !gridGroupModelMap.isEmpty()) {
                    for (Map.Entry<String, BillGroupModel> entry : gridGroupModelMap.entrySet()) {
                        if (entry.getValue() != null) {
                            showBillModels.add(entry.getValue());
                        }
                    }
                }
            }
            LogUtil.i("gong", "showBillModels=" + JSON.toJSONString(showBillModels));
            mIBillDetails.setAdapter(showBillModels);

            mIBillDetails.updateStatus(null);
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
                    gridTab.setTitle(JSONUtil.getText(otherGrid, "dgtitle"));
                    gridTab.setCaller(JSONUtil.getText(otherGrid, "dgcaller"));
                    gridTab.setPosition(i);

                    Map<String, BillGroupModel> gridGroupModelMap = handlerGridetail(index, detailgrid);
                    if (gridGroupModelMap != null && !gridGroupModelMap.isEmpty()) {
                        List<BillGroupModel> billGroupModels = new ArrayList<>();
                        for (Map.Entry<String, BillGroupModel> entry : gridGroupModelMap.entrySet()) {
                            if (entry.getValue() != null) {
                                billGroupModels.add(entry.getValue());
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
            /*if (!multidetailgrid) {
                BillGroupModel mBillGroupModel = new BillGroupModel();
                mBillGroupModel.setGroupIndex(index);
                mBillGroupModel.setGroup("明细表");
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

                modelMap.put("明细表", mBillGroupModel);
            } else {*/
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
//            }
            return modelMap;
        } else {
            return null;
        }
    }

    private boolean isShow(BillGroupModel.BillModel mBillModel) {
        return mBillModel.getIsdefault() == -1 && !mBillModel.getType().equals("H");
    }

    private BillGroupModel.BillModel getBillModelByObject(JSONObject object) {
        String status = null;

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

        if (logicType.equals("necessaryField")) {
            allowBlank = "F";
        }

        if (TextUtils.isEmpty(mStatusKey) && ("单据状态".equals(caption) || "状态".equals(caption))) {
            mStatusKey = field;
            status = value;

            if (!TextUtils.isEmpty(status)) {
                mIBillDetails.updateStatus(status);
            }
        }

        //判断附件
        if ("FF".equals(type) && !StringUtil.isEmpty(value)) {
            loadFilePaths(value);
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
        mBillModel.setUpdatable("T".equals(updatable));
        return mBillModel;
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
            String statusKey = handlerModelByObject(mGroupModel, formconfigs, formdata);
            status = JSONUtil.getText(formdata, "cu_auditstatus", statusKey);
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
                mGroupModel.setDeleteAble(false);
                handlerModelByObject(mGroupModel, gridconfigs, griddata);
                mBillGroupModels.add(mGroupModel);
            }
        }
        if (!TextUtils.isEmpty(status)) {
            mIBillDetails.updateStatus(status);
        }
        mIBillDetails.setAdapter(mBillGroupModels);
    }


    private String handlerModelByObject(BillGroupModel mGroupModel, JSONArray configs, JSONObject object) {
        String statusKey = null;

        for (int i = 0; i < configs.size(); i++) {
            JSONObject config = configs.getJSONObject(i);
            if (config == null) {
                continue;
            }
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
            if (!ListUtils.isEmpty(mBillJumps)) {
                for (BillJump mBillJump : mBillJumps) {
                    if (mBillJump.getCaption().equals(caption) || mBillJump.getField().equals(field)) {
                        mBillModel.setBillJump(mBillJump);
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(statusKey) && ("单据状态".equals(caption) || "状态".equals(caption))) {
                statusKey = field;
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
            //判断附件
            if ("FF".equals(mBillModel.getType()) && !StringUtil.isEmpty(mBillModel.getValue())) {
                loadFilePaths(mBillModel.getValue());
            }
            if (mBillModel.getIsdefault() == -1 && !mBillModel.getType().equals("H")) {
                mGroupModel.addShow(mBillModel);
            } else {
                mGroupModel.addHide(mBillModel);
            }


        }
        return statusKey;
    }


    public void saveAndSubmit(List<BillGroupModel> mGroupModels) {
        mIBillDetails.showLoading();
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
            if (TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 &&
                    ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                mIBillDetails.showToast(e.getCaption() + "为必填项");
                mIBillDetails.showLoading();
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
            mIBillDetails.dimssLoading();
            return;
        }

        String formStoreStr = JSONUtil.map2JSON(formStore);
        String gridStoreStr = JSONUtil.map2JSON(gridStoreList);

        LogUtil.i("update", "formStoreStr=" + formStoreStr);
        LogUtil.i("update", "gridStoreStr=" + gridStoreStr);

        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        String emCode = CommonUtil.getEmcode();
        com.me.network.app.http.HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(ct),
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
                        Toast.makeText(ct, R.string.update_success, Toast.LENGTH_SHORT).show();
                        mIBillDetails.dimssLoading();

                        mIBillDetails.updateDetail(mGroupModels);
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        mIBillDetails.dimssLoading();
                        Toast.makeText(ct, failStr, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void saveAndSubmit(List<BillGroupModel.BillModel> formFields, List<BillGroupModel.GridTab> otherGridList, int flag) {
        Map<String, Object> formStore = new HashMap<>();
        for (BillGroupModel.BillModel e : formFields) {
            if (TextUtils.isEmpty(e.getValue()) && e.getIsdefault() == -1 &&
                    ("necessaryField".equals(e.getAllowBlank()) || "F".equals(e.getAllowBlank()))) {
                mIBillDetails.showToast(e.getCaption() + "为必填项");
                mIBillDetails.dimssLoading();
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
                        mIBillDetails.showToast(billModel.getCaption() + "为必填项");
                        mIBillDetails.dimssLoading();
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
            mIBillDetails.showToast("开始上传完成=" + mBillModel.getLength());
            mIBillDetails.updateFileOk();
            return;
        }
        mIBillDetails.showToast("开始上传附件！！");
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
                mIBillDetails.showToast("附件上传失败");
            }
        });

    }

}
