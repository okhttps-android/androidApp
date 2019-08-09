package com.uas.appworks.crm3_0.activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.utils.CommonUtil;
import com.core.widget.view.model.SelectAimModel;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.apputils.utils.UUHttpHelper;
import com.uas.applocation.UasLocationHelper;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CustomerVisitBillAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户拜访计划新增界面
 */
public class CustomerVisitBillInputActivity extends BillInputActivity {
    private UUHttpHelper mUUHttpHelper;
    private final int TAG_COMPANY_SELECT = 0x651;
    private final int TAG_ADDRESS_SELECT = 0x652;
    private int keyValue;
    private SelectAimModel address;//当前地址

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.list) {
                menu.getItem(i).setVisible(false);
                menu.getItem(i).setEnabled(false);
                break;
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveAndSubmit) {
            if (!CommonUtil.isRepeatClick(4000)) {
                submitBill();
            }
        } else if (item.getItemId() == com.modular.apputils.R.id.list) {
            toDataFormList();
        }
        return false;
    }

    private void submitBill() {
        String mp_cuname = "";
        String mp_address = "";
        String mp_longitude = "";
        String mp_latitude = "";

        List<BillGroupModel.BillModel> allBillModels = mBillAdapter.getmAllBillModels();
        for (BillGroupModel.BillModel billModel : allBillModels) {
            if ("mp_address".equals(billModel.getField()) && isShow(billModel)) {
                mp_address = billModel.getValue();
            }
            if (StringUtil.hasOneEqual(billModel.getField(), "mp_cuname", "mp_xmmc_user") &&
                    !TextUtils.isEmpty(billModel.getValue()) && isShow(billModel)) {
                mp_cuname = billModel.getValue();
            }
            if ("mp_longitude".equals(billModel.getField())) {
                mp_longitude = billModel.getValue();
            }
            if ("mp_latitude".equals(billModel.getField())) {
                mp_latitude = billModel.getValue();
            }
        }

        if (StringUtil.isEmpty(mp_address)) {
            toast("当前地址不能为空");
            return;
        }

//        if (!StringUtil.isEmpty(mp_cuname) || !StringUtil.isEmpty(mp_address)) {
        if (address == null) {
            address = new SelectAimModel();
        }
        address.setName(mp_cuname);
        address.setAddress(mp_address);
        if (address.getLatLng() == null) {
            try {
                float lng = Float.parseFloat(mp_longitude);
                float lat = Float.parseFloat(mp_latitude);
                if (lat != 0 || lat != 0) {
                    LatLng latLng = new LatLng(lat, lng);
                    address.setLatLng(latLng);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        loadLatLng(StringUtil.isEmpty(mp_address) ? mp_cuname : mp_address);
        saveAddressAndSubmit();
    }

    private boolean isShow(BillGroupModel.BillModel mBillModel) {
        return mBillModel.getIsdefault() == -1 && !mBillModel.getType().equals("H");
    }

    @Override
    public void setAdapter(List<BillGroupModel> groupModels) {
        if (!ListUtils.isEmpty(groupModels)) {
            for (int i = 0; i < groupModels.size(); i++) {
                BillGroupModel groupModel = groupModels.get(i);
                if (i == 1) {
                    for (BillGroupModel.BillModel billModel : groupModel.getShowBillFields()) {
                        switch (billModel.getField()) {
                            case "mpd_personnum"://人员编号
                                String emCode = CommonUtil.getEmcode();
                                billModel.setDefValue(emCode);
                                billModel.setValue(emCode);
                                billModel.setReadOnly("T");
                                break;
                            case "mpd_personname":
                                String name = CommonUtil.getName();
                                billModel.setDefValue(name);
                                billModel.setValue(name);
                                billModel.setReadOnly("T");
                                break;
                            case "mpd_type":
                                billModel.setDefValue("跟进人");
                                billModel.setValue("跟进人");
                                billModel.setReadOnly("T");
                                break;
                            case "mp_address":
                                billModel.setDefValue(UasLocationHelper.getInstance().getUASLocation().getAddress());
                                break;
                            case "mpd_actdate":
                            case "mpd_outdate":
                                billModel.setDefValue("");
                                break;
                        }
                    }
                } else if (!ListUtils.isEmpty(groupModel.getShowBillFields())) {
                    for (BillGroupModel.BillModel billModel : groupModel.getShowBillFields()) {
                        switch (billModel.getField()) {
                            case "mp_address":
//                                billModel.setDefValue(UasLocationHelper.getInstance().getUASLocation().getAddress());
                                billModel.setType("DF");
                                break;
                            case "mpd_actdate":
                            case "mpd_outdate":
                                billModel.setDefValue("");
                                break;
                            default: {
                                switch (billModel.getCaption()) {
                                    case "当前地址":
//                                        billModel.setDefValue(UasLocationHelper.getInstance().getUASLocation().getAddress());
                                        billModel.setType("DF");
                                        break;
                                    case "实际离开时间":
                                    case "实际到达时间":
                                        billModel.setDefValue("");
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        super.setAdapter(groupModels);
    }


    @Override
    public BillAdapter newBillAdapter(List<BillGroupModel> groupModels) {
        return new CustomerVisitBillAdapter(ct, groupModels, this);
    }

    @Override
    public void toSelect(int position, BillGroupModel.BillModel model) {
        if ((model.getCaption().equals("公司名称") || "mpd_company".equals(model.getField()))) {
            selectPosition = model.getGroupIndex();
            Intent intent = new Intent("com.modular.form.SelectAimActivity");
            startActivityForResult(intent, TAG_COMPANY_SELECT);
        } else if (("mp_address".equals(model.getField()))) {
            List<BillGroupModel.BillModel> allBillModels = mBillAdapter.getmAllBillModels();
            String companyName = "";
            for (BillGroupModel.BillModel billModel : allBillModels) {
                if (StringUtil.hasOneEqual(billModel.getField(), "mp_cuname", "mp_xmmc_user") &&
                        !TextUtils.isEmpty(billModel.getValue()) && isShow(billModel)) {
                    companyName = billModel.getValue();
                }
            }
            selectPosition = model.getGroupIndex();
            Intent intent = new Intent("com.modular.form.SelectAimActivity");
            intent.putExtra("search", companyName);
            startActivityForResult(intent, TAG_ADDRESS_SELECT);
        } else if ("mp_bccode".equals(model.getField())) {
            String master = CommonUtil.getMaster();
            if ("DATACENTER".equals(master) || "N_SHYZ".equals(master) || "N_AJC".equals(master)) {
                List<BillGroupModel.BillModel> allBillModels = mBillAdapter.getmAllBillModels();
                String mp_cucode = "", mp_xmbh_user = "", mp_prjandcus_user = "";
                for (BillGroupModel.BillModel billModel : allBillModels) {
                    if (!TextUtils.isEmpty(billModel.getValue())) {
                        if ("mp_cucode".equals(billModel.getField())) {
                            mp_cucode = billModel.getValue();
                        }
                        if ("mp_xmbh_user".equals(billModel.getField())) {
                            mp_xmbh_user = billModel.getValue();
                        }
                    }
                    if ("mp_prjandcus_user".equals(billModel.getField())) {
                        mp_prjandcus_user = billModel.getValue();
                    }
                }

                if (TextUtils.isEmpty(mp_cucode) && TextUtils.isEmpty(mp_xmbh_user)) {
                    toast("请先选择客户编号或项目编号");
                    return;
                }
                String condition = "sign=\'" + mp_prjandcus_user + "\'";
                findBydbFind(model, condition);
            } else {
                super.toSelect(position, model);
            }
        } else {
            super.toSelect(position, model);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == TAG_COMPANY_SELECT || requestCode == TAG_ADDRESS_SELECT) && data != null) {//选择公司
            SelectAimModel chcheAimModel = data.getParcelableExtra("data");
            sureSelectAim(chcheAimModel);
        }
    }

    @Override
    protected void handlerSelectDbFind(JSONObject object, int groupId, boolean isForm) {
        super.handlerSelectDbFind(object, groupId, isForm);
    }


    private void sureSelectAim(SelectAimModel entity) {
        if (entity == null || selectPosition < 0 || selectPosition > ListUtils.getSize(mBillAdapter.getBillGroupModels())) {
            selectPosition = -1;
            return;
        }
        String company = StringUtil.isEmpty(entity.getName()) ? "" : entity.getName();
        String companyAddress = StringUtil.isEmpty(entity.getAddress()) ? "" : entity.getAddress();
        BillGroupModel mBillGroupModel = mBillAdapter.getBillGroupModel(selectPosition);
        if (mBillGroupModel != null) {
            if (entity.getLatLng() != null) {
                mBillGroupModel.updateTagMap("company", entity.getName());
                mBillGroupModel.updateTagMap("companyAddress", entity.getAddress());
                mBillGroupModel.updateTagMap("latitude", entity.getLatLng().latitude);
                mBillGroupModel.updateTagMap("longitude", entity.getLatLng().longitude);
                mBillGroupModel.updateTagMap("visitTime", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
            }
            if (!ListUtils.isEmpty(mBillGroupModel.getShowBillFields())) {
                for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
                    if ("mpd_company".equals(e.getField()) || "公司名称".equals(e.getCaption())) {
                        e.setValue(company);
                    } else if ("mpd_address".equals(e.getField()) || "公司地址".equals(e.getCaption())) {
                        e.setValue(companyAddress);
                    } else if ("mp_address".equals(e.getField()) || "当前地址".equals(e.getCaption())) {
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
                    if ("mpd_company".equals(e.getField()) || "公司名称".equals(e.getCaption())) {
                        e.setValue(company);
                    } else if ("mpd_address".equals(e.getField()) || "公司地址".equals(e.getCaption())) {
                        e.setValue(companyAddress);
                    } else if ("mp_address".equals(e.getField()) || "当前地址".equals(e.getCaption())) {
                        e.setValue(companyAddress);
                        if (address == null) {
                            address = new SelectAimModel();
                        }
                        address.setLatLng(entity.getLatLng());
                    }
                }
            }
        }
        mBillAdapter.notifyDataSetChanged();
    }

    @Override
    public void commitSuccess(final int keyValue, String code) {
        this.keyValue = keyValue;
        ToastMessage("提交成功！");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) return;
                /*startActivity(new Intent(ct, CustomerVisitDetailsActivity.class)
                        .putExtra(Constants.Intents.TITLE, "客户拜访详情")
                        .putExtra(Constants.Intents.MY_DOIT, true)
                        .putExtra(Constants.Intents.STATUS, "已提交")
                        .putExtra(Constants.Intents.ID, keyValue)
                        .putExtra(Constants.Intents.CALLER, "MobileOutPlans"));*/
                finish();
                overridePendingTransition(com.modular.apputils.R.anim.anim_activity_in, com.modular.apputils.R.anim.anim_activity_out);
            }
        }, 2000);
    }


    private void saveAddress() {
        if (1 == 1) return;
        if (mUUHttpHelper == null) {
            mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        }
        List<BillGroupModel> mBillGroupModels = mBillAdapter.getBillGroupModels();
        if (!ListUtils.isEmpty(mBillGroupModels)) {
            boolean hasSubmit = false;
            for (BillGroupModel mBillGroupModel : mBillGroupModels) {
                Map<String, Object> mTagMap = mBillGroupModel.getTagMap();
                if (mTagMap != null
                        && mTagMap.get("company") != null
                        && mTagMap.get("companyAddress") != null
                        && mTagMap.get("visitTime") != null
                        && mTagMap.get("longitude") != null
                        && mTagMap.get("latitude") != null
                ) {
                    saveOutAddress(mTagMap);
                    hasSubmit = true;
                    mBillGroupModel.setTagMap(null);
                    break;
                }
            }
            if (!hasSubmit) {
                ToastMessage("提交成功！");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mContext == null) return;
                        /*startActivity(new Intent(ct, CustomerVisitDetailsActivity.class)
                                .putExtra(Constants.Intents.TITLE, "客户拜访详情")
                                .putExtra(Constants.Intents.MY_DOIT, true)
                                .putExtra(Constants.Intents.STATUS, "已提交")
                                .putExtra(Constants.Intents.ID, keyValue)
                                .putExtra(Constants.Intents.CALLER, "MobileOutPlans"));*/
                        finish();
                        overridePendingTransition(com.modular.apputils.R.anim.anim_activity_in, com.modular.apputils.R.anim.anim_activity_out);
                    }
                }, 2000);
            }
        }

    }

    private void loadLatLng(String address) {
        if (StringUtil.isEmpty(address)) return;
        if (mUUHttpHelper == null) {
            mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        }
        progressDialog.show();
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/getLngAndLat.action")
                        .addParams("address", address.trim())
                        .record(11)
                        .mode(Method.GET)
                , mOnSmartHttpListener
        );
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

        if (mUUHttpHelper == null) {
            mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        }
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/addOutAddress.action")
                        .addParams("caller", "lp")
                        .mode(Method.POST).record(13)
                        .addParams("formStore", JSONUtil.map2JSON(formStore))
                , mOnSmartHttpListener
        );
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            progressDialog.dismiss();
            switch (what) {
                case 11:
                    LogUtil.i("gong", "message=" + message);
                    JSONObject object = JSON.parseObject(message);
                    float lat = JSONUtil.getFloat(object, "lat");
                    float lng = JSONUtil.getFloat(object, "lng");
                    if (lat != 0 || lat != 0) {
                        LatLng latLng = new LatLng(lat, lng);
                        address.setLatLng(latLng);
                    }

                    saveAddressAndSubmit();
                    break;
                case 13:
                    saveAddress();
                    break;
            }
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            progressDialog.dismiss();
            if (what == 13) {
                saveAddress();
            }
        }
    };

    private void saveAddressAndSubmit() {
        List<BillGroupModel> billGroupModels = mBillAdapter.getBillGroupModels();
        if (address == null || address.getLatLng() == null) {
            toast("地址信息为空，请重新选择当前地址");
            return;
        }

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

        mBillPresenter.saveAndSubmit(billGroupModels);
    }


}
