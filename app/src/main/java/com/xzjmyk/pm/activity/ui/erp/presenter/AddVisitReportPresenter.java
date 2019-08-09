package com.xzjmyk.pm.activity.ui.erp.presenter;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonInterface;
import com.core.widget.view.model.SelectAimModel;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.IAddVisitReport;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Bitliker on 2017/5/4.
 */
public class AddVisitReportPresenter implements OnHttpResultListener {
    private final int LOAD_COMTACT = 0x216;
    private final int GET_VISIT_TYPE = 0x214;
    private final int LOAD_ID = 0x201;
    private final int LOAD_CODE = 0x202;
    private final int SAVEV_ISITRECORD = 0x211;
    private final int SUBMIT_VISITRECORD = 0x212;
    private final int UPDATA_VISITRECORD = 0x213;
    private IAddVisitReport iAddVisitReport = null;
    private Client client = null;
    private List<String> contacts = null;
    private boolean isB2b;
    private boolean isSubmiting = false;

    public AddVisitReportPresenter(IAddVisitReport iAddVisitReport) {
        this.iAddVisitReport = iAddVisitReport;
        client = new Client();
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
    }


    public void setClient(SelectAimModel model) {
        if (model == null) return;
        client.name = model.getName();
        client.address = model.getAddress();
        if (JSONUtil.validate(model.getObject())) {
            JSONObject object = JSON.parseObject(model.getObject());
            client.code = JSONUtil.getText(object, "CU_CODE");
            client.cuRealName = JSONUtil.getText(object, "CU_NAME");
            if (StringUtil.isEmpty(model.getName()))
                client.name = client.cuRealName;
            client.contact = JSONUtil.getText(object, "CU_CONTACT");
            if (StringUtil.isEmpty(model.getAddress()))
                client.address = JSONUtil.getText(object, "CU_ADD1");
            client.nichename = JSONUtil.getText(object, "CU_NICHESTEP");
            client.nichestep = "";
            if (!StringUtil.isEmpty(client.contact)) {
                List<String> contacts = new ArrayList<>();
                contacts.add(client.contact);
                iAddVisitReport.showContact(contacts);
            }
        }
    }

    public boolean canSubmit(List<String> contacts) {
        if (ListUtils.isEmpty(contacts))
            return false;
        this.contacts = contacts;
        for (String e : contacts) {
            if (StringUtil.isEmpty(e)) {
                return false;
            } else if (StringUtil.isEmpty(client.contact))
                client.contact = e;
        }
        return true;
    }

    public void submit(Bundle bundle) {
        isSubmiting = true;
        getIdByNet(bundle);
    }

    public String getCuName() {
        String name = "";
        if (client != null) {
            if (!StringUtil.isEmpty(client.cuRealName)) {
                name = client.cuRealName;
            } else {
                name = StringUtil.isEmpty(client.name) ? "" : client.name;
            }
        }
        return name;
    }

    public String getContact() {
        if (client == null) return "";
        if (StringUtil.isEmpty(client.contact)) return "";
        return client.contact;
    }

    private void getIdByNet(Bundle bundle) {
        if (isB2b) return;
        iAddVisitReport.showLoading();
        Map<String, Object> param = new HashMap<>();
        param.put("seq", "VISITRECORD_SEQ");
        Request request = new Request.Bulider()
                .setWhat(LOAD_ID)
                .setUrl("common/getId.action")
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    public void loadContact(String code) {
        if (StringUtil.isEmpty(code)) return;
        Map<String, Object> param = new HashMap<>();
        param.put("page", 1);
        param.put("condition", "ct_sourcecode='" + code + "'");
        param.put("size", 1000);
        Request request = new Request.Bulider()
                .setWhat(LOAD_COMTACT)
                .setUrl("mobile/crm/getContactPerson.action")
                .setParam(param)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void getCodeByNet(Bundle bundle) {
        if (isB2b) return;
        Map<String, Object> param = new HashMap<>();
        param.put("type", 2);
        param.put("caller", "VisitRecord");
        Request request = new Request.Bulider()
                .setWhat(LOAD_CODE)
                .setUrl("common/getCodeString.action")
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /*保存拜访报告*/
    private void saveVisitRecord(Bundle bundle) {
        int vrId = bundle.getInt("vrId");
        String url = "crm/customermgr/saveVisitRecord.action";
        String name = CommonUtil.getName();
        Map<String, Object> param = new HashMap<>();
        param.put("param1", "[]");
        param.put("param2", "[]");
        param.put("param3", JSONUtil.map2JSON(getParam3(vrId, name)));
        param.put("param4", "[]");
        param.put("param5", "[]");
        param.put("param6", "[]");
        param.put("param7", "[]");
        param.put("caller", "VisitRecord");
        param.put("formStore", JSONUtil.map2JSON(getFormStore(vrId, name, bundle)));
        Request request = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setParam(param)
                .setBundle(bundle)
                .setWhat(SAVEV_ISITRECORD)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /*更新拜访报告状态，如果有外勤，更新外勤状态*/
    private void upStatus(Bundle bundle) {
        iAddVisitReport.showLoading();
        int vrId = bundle.getInt("vrId");
        int mpdId = bundle.getInt("mpdId");
        final Map<String, Object> param = new HashMap<>();
        param.put("vr_id", vrId);
        param.put("vp_id", mpdId);
        param.put("cu_nichestep", client.nichename);
        param.put("cu_code", client.code);
        param.put("bc_nichecode", client.nichestep);
        Request request = new Request.Bulider()
                .setWhat(UPDATA_VISITRECORD)
                .setUrl("mobile/crm/updateVistPlan.action")
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /*提交拜访报告*/
    private void submitVisitRecord(Bundle bundle) {
        if (isB2b) return;
        iAddVisitReport.showLoading();
        Map<String, Object> param = new HashMap<>();
        int vrId = bundle.getInt("vrId");
        param.put("id", vrId);
        param.put("caller", "VisitRecord");
        Request request = new Request.Bulider()
                .setWhat(SUBMIT_VISITRECORD)
                .setUrl("crm/customermgr/submitVisitRecord.action")
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void getVisitType(Bundle bundle) {
        if (isB2b) return;
        Map<String, Object> param = new HashMap<>();
        param.put("custcode", client.code);
        param.put("custname", client.name);
        Request request = new Request.Bulider()
                .setWhat(GET_VISIT_TYPE)
                .setUrl("mobile/crm/getVisitType.action")
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    /*保存联系人*/
    private void saveContact(String vrCode) {
        if (isB2b || ListUtils.isEmpty(contacts)) return;
        List<Map<String, Object>> formStores = new ArrayList<>();
        Map<String, Object> formStore = null;
        for (String e : contacts) {
            if (StringUtil.isEmpty(e)) continue;
            formStore = CommonInterface.getInstance().getFormStoreContact(vrCode, e, "", client.cuRealName, client.code, client.address, "");
            formStores.add(formStore);
        }
        CommonInterface.getInstance().addContact(formStores, this);

    }


    private Map<String, Object> getParam3(int vrId, String name) {
        Map<String, Object> param3 = new HashMap<>();
        param3.put("pl_detno", 1);
        param3.put("pl_name", name);
        param3.put("pl_vrid", vrId);//通过通用id接口获取id
        return param3;
    }

    private Map<String, Object> getFormStore(int vrId, String name, Bundle bundle) {
        if (bundle == null) return new HashMap<>();
        String vrCode = bundle.getString("vrCode");
        String remark = bundle.getString("remark");
        String detail = bundle.getString("detail");
        if (detail == null) detail = "";
        String type = bundle.getString("type");
        String vr_recorddate = bundle.getString("recorddate");
        Map<String, Object> formStore = new HashMap<>();
        formStore.put("vr_id", vrId);
        formStore.put("vr_code", vrCode);
        formStore.put("vr_visittime", "");
        formStore.put("vr_visitend", "");
        formStore.put("vr_statuscode", "ENTERING");
        formStore.put("vr_status", "在录入");
        formStore.put("vr_recorder", StringUtil.toHttpString(name));
        formStore.put("vr_recorddate", StringUtil.toHttpString(StringUtil.isEmpty(vr_recorddate) ? DateFormatUtil.long2Str(DateFormatUtil.YMD) : vr_recorddate));
        formStore.put("vr_contact", StringUtil.toHttpString(name));
        formStore.put("vr_cuuu", getString(client.code));
        formStore.put("vr_cuname", getString(client.name));
        formStore.put("vr_cucontact", getString(client.contact));
        formStore.put("vr_visitplace", getString(client.address));
        formStore.put("vr_nichestep", client.nichename);//关联商机
        formStore.put("vr_nichename", "");
        formStore.put("vr_class", StringUtil.isEmpty(type) ? "OfficeClerk" : type);//拜访类型（通过接口获取类型） OfficeClerk(客户拜访)|VisitRecord!Vender(原厂拜访)
        formStore.put("vr_title", StringUtil.toHttpString(remark));
        formStore.put("vr_detail", StringUtil.toHttpString(detail.replace("\n", " ").replace("\\n", " ")));
        return formStore;
    }

    private String getString(String message) {
        if (StringUtil.isEmpty(message)) return "";
        return StringUtil.toHttpString(message);
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (!isJSON) return;
        JSONObject object = JSON.parseObject(message);
        if (bundle == null) bundle = new Bundle();

        switch (what) {
            case LOAD_ID:
                int id = JSONUtil.getInt(object, "id");
                bundle.putInt("vrId", id);
                getCodeByNet(bundle);
                break;
            case LOAD_CODE://获取编号
                String code = object.getString("code");
                bundle.putString("vrCode", code);
                getVisitType(bundle);
                break;
            case SAVEV_ISITRECORD:
                iAddVisitReport.dimssLoading();
                if (bundle.getInt("mpdId") > 0) upStatus(bundle);
                else
                    submitVisitRecord(bundle);
                break;
            case UPDATA_VISITRECORD:
                iAddVisitReport.dimssLoading();
                submitVisitRecord(bundle);
                break;
            case SUBMIT_VISITRECORD:
                iAddVisitReport.dimssLoading();
                iAddVisitReport.showToast(R.string.save_success, R.color.load_submit);
                String vrCode = bundle.getString("vrCode");
                saveContact(vrCode);
                iAddVisitReport.finish();
                break;
            case CommonInterface.ADD_CONTACT:
                break;
            case GET_VISIT_TYPE:
                JSONArray array = object.getJSONArray("visittype");
                String type = "";
                if (!ListUtils.isEmpty(array)) {
                    type = JSONUtil.getText(array.getJSONObject(0), "typecode");
                }
                bundle.putString("type", type);
                saveVisitRecord(bundle);
                break;
            case LOAD_COMTACT:
                JSONArray datalist = JSONUtil.getJSONArray(object, "datalist");
                if (ListUtils.isEmpty(datalist)) return;
                List<String> models = new ArrayList<>();
                for (int i = 0; i < datalist.size(); i++) {
                    JSONObject contact = datalist.getJSONObject(i);
                    String name = JSONUtil.getText(contact, "ct_name");
                    models.add(name);
                }
                if (!ListUtils.isEmpty(models)) {
                    iAddVisitReport.showContact(models);
                }
                break;
            default:
                isSubmiting = false;
                iAddVisitReport.dimssLoading();
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        iAddVisitReport.dimssLoading();
    }

    private class Client {
        public String code;
        public String cuRealName;
        public String name;
        public String contact;
        public String address;
        public String nichestep;//当前阶段
        public String nichename;//当前名称
    }

}
