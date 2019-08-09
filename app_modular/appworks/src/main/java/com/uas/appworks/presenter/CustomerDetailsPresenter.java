package com.uas.appworks.presenter;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;

import com.modular.apputils.presenter.BillDetailsPresenter;
import com.uas.appworks.model.CustomerBindBill;
import com.uas.appworks.presenter.imp.ICustomerDetails;

import java.util.ArrayList;


public class CustomerDetailsPresenter extends BillDetailsPresenter {
    private final int LOAD_RELATION_DETAILS = 0x15;//获取下面信息数据


    public CustomerDetailsPresenter(Context ct, ICustomerDetails mIBillDetails) {
        super(ct, mIBillDetails);
    }


    public void start(Intent intent) {
        super.start(intent);
        loadRelationDetails();
    }


    private void loadRelationDetails() {
        mIBillDetails.showLoading();
        requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerRelationDetails.action")
                        .addParams("customerId", mId)
                        .record(LOAD_RELATION_DETAILS)
                , mOnSmartHttpListener);
    }


    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject jsonObject = JSON.parseObject(message);
            switch (what) {

                case LOAD_RELATION_DETAILS:
                    handlerRelationDetails(jsonObject);
                    break;


            }
            mIBillDetails.dimssLoading();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            mIBillDetails.dimssLoading();
            if (LOAD_RELATION_DETAILS == what) {

            } else if (JSONUtil.validateJSONObject(message)) {
                mIBillDetails.showToast(JSONUtil.getText(message, "exceptionInfo"));
            } else {
                mIBillDetails.showToast(message);
            }

        }
    };

    private void handlerRelationDetails(JSONObject jsonObject) throws Exception {
        JSONArray business = JSONUtil.getJSONArray(jsonObject, "business");
        JSONArray cus_contacts = JSONUtil.getJSONArray(jsonObject, "cus_contacts");
        JSONArray visit_report = JSONUtil.getJSONArray(jsonObject, "visit_report");
        JSONArray cus_address = JSONUtil.getJSONArray(jsonObject, "cus_address");
        ArrayList<CustomerBindBill> mCusBusiness = new ArrayList<>();
        for (int i = 0; i < business.size(); i++) {
            CustomerBindBill bindBill = new CustomerBindBill();
            JSONObject object = business.getJSONObject(i);
            int id = JSONUtil.getInt(object, "id");
            String name = JSONUtil.getText(object, "bu_name");
            String bu_domancode = JSONUtil.getText(object, "bu_domancode");
            String bu_type = JSONUtil.getText(object, "bu_type");
            String bu_code = JSONUtil.getText(object, "bu_code");
            String stage = JSONUtil.getText(object, "bu_stage");
            String status = JSONUtil.getText(object, "bu_status");
            String date = JSONUtil.getText(object, "bu_date");
            bindBill.setId(id);
            bindBill.setCode(bu_code);
            bindBill.setType(bu_type);
            bindBill.setDomancode(bu_domancode);
            bindBill.setName(name);
            bindBill.setSubName(stage);
            bindBill.setStatus(status);
            bindBill.setDate(date);
            mCusBusiness.add(bindBill);
        }

        ArrayList<CustomerBindBill> mCusContacts = new ArrayList<>();
        for (int i = 0; i < cus_contacts.size(); i++) {
            CustomerBindBill bindBill = new CustomerBindBill();
            JSONObject object = cus_contacts.getJSONObject(i);
            int id = JSONUtil.getInt(object, "id");
            String name = JSONUtil.getText(object, "co_name");
            String position = JSONUtil.getText(object, "co_position");
            String phone = JSONUtil.getText(object, "co_phone");
            bindBill.setId(id);
            bindBill.setName(name);
            bindBill.setSubName(position);
            bindBill.setDate(phone);
            mCusContacts.add(bindBill);
        }

        ArrayList<CustomerBindBill> mCusReport = new ArrayList<>();
        for (int i = 0; i < visit_report.size(); i++) {
            CustomerBindBill bindBill = new CustomerBindBill();
            JSONObject object = visit_report.getJSONObject(i);
            int id = JSONUtil.getInt(object, "vis_id");
            String name = JSONUtil.getText(object, "vis_man");
            String contact = JSONUtil.getText(object, "vis_contact");
            String date = JSONUtil.getText(object, "vis_date");
            String status = JSONUtil.getText(object, "vis_status");
            bindBill.setId(id);
            bindBill.setName(name);
            bindBill.setSubName(contact);
            bindBill.setDate(date);
            bindBill.setStatus(status);
            mCusReport.add(bindBill);
        }

        ArrayList<CustomerBindBill> mCusAddress = new ArrayList<>();
        for (int i = 0; i < cus_address.size(); i++) {
            CustomerBindBill bindBill = new CustomerBindBill();
            JSONObject object = cus_address.getJSONObject(i);
            int id = JSONUtil.getInt(object, "address_id");
            String name = JSONUtil.getText(object, "name");
            String position = JSONUtil.getText(object, "position");
            String address = JSONUtil.getText(object, "address");
            float longitude = JSONUtil.getFloat(object, "longitude");
            float latitude = JSONUtil.getFloat(object, "latitude");
            if (!StringUtil.isEmpty(position)||!StringUtil.isEmpty(address)){
                bindBill.setId(id);
                bindBill.setName(name);
                bindBill.setSubName(position);
                bindBill.setAddress(address);
                bindBill.setLongitude(longitude);
                bindBill.setLatitude(latitude);
                mCusAddress.add(bindBill);
            }
        }
        ( (ICustomerDetails)mIBillDetails).setBottomDatas(mCusBusiness, mCusContacts, mCusReport, mCusAddress);
    }


}
