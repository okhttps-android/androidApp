package com.uas.appworks.presenter;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.utils.CommonUtil;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.EasyBaseModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.presenter.BaseNetPresenter;
import com.uas.appworks.presenter.imp.ICustomerManage;

import java.util.ArrayList;
import java.util.List;

public class CustomerManagePresenter extends BaseNetPresenter {
    private final int LOAD_SHOW_CUSTOMER_ITEMS = 111;
    private final int LOAD_FORGET_CUSTOMER = 112;
    private final int LOAD_CUSTOMER_CARE = 113;
    private final int LOAD_VISIT_STATISTICS = 114;

    private final ICustomerManage iCustomerManage;
    private final String ME_EMCODE;

    public CustomerManagePresenter(Context ct, ICustomerManage iCustomerManage) {
        super(ct);
        this.iCustomerManage = iCustomerManage;
        this.ME_EMCODE = CommonUtil.getEmcode();
    }

    @Override
    public String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(ct);
    }


    public void loadAllData() {
        loadShowCustomerItems(DateFormatUtil.long2Str(DateFormatUtil.YM));
        loadForgetCustomer();
        loadCustomerCare();
        loadVisitStatistics();
        handlerVisitStatistics(new JSONObject());
        handlerShowCustomerItems(new JSONObject());
    }

    //客户看板
    public void loadShowCustomerItems(String date) {
        iCustomerManage.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerBoardShow.action")
                        .addParams("dataTime", date)
                        .addParams("salesmanCode", ME_EMCODE)
                        .record(LOAD_SHOW_CUSTOMER_ITEMS)
                , mOnSmartHttpListener);


    }

    //遗忘客户
    public void loadForgetCustomer() {
        iCustomerManage.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerForget.action")
                        .addParams("pageIndex", 1)
                        .addParams("pageSize", 3)
                        .addParams("salesmanCode", ME_EMCODE)
                        .record(LOAD_FORGET_CUSTOMER)
                , mOnSmartHttpListener);
    }

    //客户关怀
    public void loadCustomerCare() {
        iCustomerManage.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerCare.action")
                        .addParams("pageIndex", 1)
                        .addParams("pageSize", 3)
                        .addParams("salesmanCode", ME_EMCODE)
                        .record(LOAD_CUSTOMER_CARE)
                , mOnSmartHttpListener);

    }

    public void loadVisitStatistics() {
        iCustomerManage.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerStatistics.action")
                        .addParams("dataTime", DateFormatUtil.long2Str(DateFormatUtil.YM))
                        .addParams("salesmanCode", ME_EMCODE)
                        .record(LOAD_VISIT_STATISTICS)
                , mOnSmartHttpListener);
    }


    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject jsonObject = JSON.parseObject(message);
            LogUtil.i("gong", what + "||onSuccess||" + message);
            switch (what) {
                case LOAD_SHOW_CUSTOMER_ITEMS://客户看板
                    handlerShowCustomerItems(jsonObject);
                    break;
                case LOAD_FORGET_CUSTOMER://遗忘客户
                    handlerForgetCustomer(JSONUtil.getJSONArray(jsonObject, "datas"));
                    break;
                case LOAD_CUSTOMER_CARE://客户关怀handlerForgetCare
                    handlerForgetCare(JSONUtil.getJSONArray(jsonObject, "datas"));
                    break;
                case LOAD_VISIT_STATISTICS:
                    handlerVisitStatistics(jsonObject);
                    break;
            }
            iCustomerManage.dimssLoading();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            LogUtil.i("gong", what + "||onFailure||" + message);
            iCustomerManage.dimssLoading();
            if (JSONUtil.validateJSONObject(message)) {
                iCustomerManage.showToast(JSONUtil.getText(message, "exceptionInfo"));
            } else {
                iCustomerManage.showToast(message);
            }

        }
    };

    private void handlerVisitStatistics(JSONObject jsonObject) {
        List<EasyBaseModel> models = new ArrayList<>();
        models = new ArrayList<>();
        int cNum = JSONUtil.getInt(jsonObject, "cNum");//新增客户
        int vPNum = JSONUtil.getInt(jsonObject, "vPNum");//客户联系人
        int vRNum = JSONUtil.getInt(jsonObject, "vRNum");//客户拜访数
        models.add(new EasyBaseModel()
                .setTitle(String.valueOf(cNum))
                .setSubTitle(String.valueOf(vPNum))
                .setIconUrl(String.valueOf(vRNum)));
        iCustomerManage.setVisitAdapter(models);
    }

    private void handlerShowCustomerItems(JSONObject jsonObject) {
        List<EasyBaseModel> models = new ArrayList<>();
        int lNum = jsonObject.containsKey("allNum") ?
                JSONUtil.getInt(jsonObject, "allNum") : JSONUtil.getInt(jsonObject, "cNum");//客户联系人
        int cNum = JSONUtil.getInt(jsonObject, "cNum");//新增客户
        int vNum = JSONUtil.getInt(jsonObject, "vNum");//客户拜访数
        int oNum = JSONUtil.getInt(jsonObject, "oNum");//外勤数
        models.add(new EasyBaseModel().setTitle("客户总数").setSubTitle(String.valueOf(lNum)));
        models.add(new EasyBaseModel().setTitle("新增客户数").setSubTitle(String.valueOf(cNum)));
        models.add(new EasyBaseModel().setTitle("客户拜访数").setSubTitle(String.valueOf(vNum)));
        models.add(new EasyBaseModel().setTitle("拜访客户数").setSubTitle(String.valueOf(oNum)));
        iCustomerManage.setShowCustomerAdapter(models);
    }

    private void handlerForgetCare(JSONArray array) {
        List<EasyBaseModel> models = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = JSONUtil.getInt(object, "id");
                String name = JSONUtil.getText(object, "name");//客户名称
                String birthday = DateFormatUtil.long2Str(JSONUtil.getTime(object, "birthday"), DateFormatUtil.YMD_HMS);//跟进时间
                String state = JSONUtil.getText(object, "state");//状态
                models.add(new EasyBaseModel().setId(id).setTitle(name).setSubTitle(birthday).setIconUrl(state));
            }
        }
        iCustomerManage.setCustomerCareAdapter(models);
    }

    private void handlerForgetCustomer(JSONArray array) {
        List<EasyBaseModel> models = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = JSONUtil.getInt(object, "id");
                String name = JSONUtil.getText(object, "name");//客户名称
                String fpTime = DateFormatUtil.long2Str(JSONUtil.getTime(object, "fpTime"), DateFormatUtil.YMD_HMS);//跟进时间
                String state = JSONUtil.getText(object, "state");//状态
                models.add(new EasyBaseModel().setId(id).setTitle(name).setSubTitle("最后跟进时间：" + fpTime).setIconUrl(state));
            }
        }
        iCustomerManage.setForgetCustomerAdapter(models);
    }


    private List<EasyBaseModel> getVisitStatistics() {
        List<EasyBaseModel> models = new ArrayList<>();
        models.add(new EasyBaseModel().setTitle("黄艳").setSubTitle("最后跟进时间：2018-7-23").setIconUrl("进行中"));
        models.add(new EasyBaseModel().setTitle("黄艳").setSubTitle("最后跟进时间：2018-7-23").setIconUrl("进行中"));
        models.add(new EasyBaseModel().setTitle("黄艳").setSubTitle("最后跟进时间：2018-7-23").setIconUrl("进行中"));
        return models;
    }


}
