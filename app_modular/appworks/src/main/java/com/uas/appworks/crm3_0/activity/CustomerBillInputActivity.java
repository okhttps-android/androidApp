package com.uas.appworks.crm3_0.activity;

import android.content.Intent;

import android.text.TextUtils;
import android.widget.PopupWindow;

import com.baidu.mapapi.model.LatLng;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.utils.CommonUtil;
import com.core.widget.view.model.SelectAimModel;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.apputils.utils.UUHttpHelper;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CustomerBillInputAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * 客户|预录入客户录入界面
 */
public class CustomerBillInputActivity extends BillInputActivity {


    @Override
    public BillAdapter newBillAdapter(List<BillGroupModel> groupModels) {
        return new CustomerBillInputAdapter(ct, groupModels, this, new CustomerBillInputAdapter.CompanyClickListener() {
            @Override
            public void clickCompany(int position, BillGroupModel.BillModel model) {
                selectPosition = position;
                Intent intent = new Intent("com.modular.form.SelectAimActivity");
                startActivityForResult(intent, 0x201);
            }
        });
    }

    public void toDataFormList() {
        startActivity(new Intent(ct, CustomerListActivity.class)
                .putExtra(Constants.Intents.CALLER, mBillPresenter.getFormCaller())
                .putExtra(Constants.Intents.TITLE, getToolBarTitle()));
    }

    @Override
    public void commitSuccess(int keyValue, String code) {
        endActivity(keyValue);
//        batchDealCustomerLngLat(keyValue);
    }

    private void batchDealCustomerLngLat(final int keyValue) {
        boolean isGoodCus = !mBillPresenter.getFormCaller().equals("PreCustomer");
        String condition = isGoodCus ? ("cu_id=" + keyValue + "") : "1=2";
        String precondition = isGoodCus ? "1=2" : ("cu_id=" + keyValue + "");
        UUHttpHelper mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/batchDealCusotmerLngLat.action")
                        .addParams("condition", condition)
                        .addParams("precondition", precondition)
                , new OnSmartHttpListener() {
                    @Override
                    public void onSuccess(int what, String message, Tags tag) throws Exception {
                        endActivity(keyValue);
                    }

                    @Override
                    public void onFailure(int what, String message, Tags tag) throws Exception {
                        endActivity(keyValue);
                    }
                });

    }

    private void endActivity(int formId) {
        ToastMessage("提交成功！");
        if (((CustomerBillInputAdapter) mBillAdapter).isAddContact()) {
            HashMap<String, String> hashMap = new HashMap<>();
            List<BillGroupModel> mGroupModels = mBillAdapter.getBillGroupModels();
            for (BillGroupModel e : mGroupModels) {
                if (e.isForm() && !ListUtils.isEmpty(e.getShowBillFields())) {
                    for (BillGroupModel.BillModel billModel : e.getShowBillFields()) {
                        hashMap.put(billModel.getField(), billModel.getValue());
                    }
                }
            }
            startActivity(new Intent(ct, BillInputActivity.class)
                    .putExtra(Constants.Intents.CALLER, "Contact")
                    .putExtra(Constants.Intents.TITLE, "客户联系人")
                    .putExtra(Constants.Intents.ID, 0)
                    .putExtra(Constants.Intents.HASH_MAP, hashMap));
        } else {
            //进入详情界面
            /*Intent intent = new Intent("com.modular.form.erp.activity.CommonDocDetailsActivity");
            intent.putExtra("form_new_bill", true);
            intent.putExtra("caller", mBillPresenter.getFormCaller());
            intent.putExtra("keyValue", mBillPresenter.getFormId());
            startActivity(intent);*/

            startActivity(new Intent(ct, CustomerDetails3_0Activity.class)
                    .putExtra(Constants.Intents.CALLER, mBillPresenter.getFormCaller())
                    .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                    .putExtra(Constants.Intents.MY_DOIT, true)
                    .putExtra(Constants.Intents.ID, formId));
            //进入列表界面
//            startActivity(new Intent(ct, CustomerListActivity.class)
//                    .putExtra(Constants.Intents.CALLER, mBillPresenter.getFormCaller())
//                    .putExtra(Constants.Intents.TITLE, getToolBarTitle()));

        }

        finish();
    }

    private PopupWindow popupWindow = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (0x201 == requestCode && data != null) {
            SelectAimModel chcheAimModel = data.getParcelableExtra("data");
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            popupWindow = PopupWindowHelper.create(this, getString(R.string.perfect_company_name), chcheAimModel, new PopupWindowHelper.OnClickListener() {
                @Override
                public void result(SelectAimModel model) {
                    String name = model.getName();
                    String address = model.getAddress();
                    LatLng mLatLng = model.getLatLng();
                    handlerSelectDbFind(name, address, mLatLng, selectPosition);

                }
            }, null);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void handlerSelectDbFind(String name, String address, LatLng latLng, int groupId) {
        List<BillGroupModel> mBillGroupModels = mBillAdapter.getBillGroupModels();
        if (!ListUtils.isEmpty(mBillGroupModels)) {
            for (BillGroupModel mBillGroupModel : mBillGroupModels) {
                if (mBillGroupModel != null) {
                    if (!ListUtils.isEmpty(mBillGroupModel.getShowBillFields())) {
                        for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
                            if ("企业名称".equals(e.getCaption()) || "cu_name".equals(e.getField())) {
                                e.setValue(name);
                            } else if ("cu_add2".equals(e.getCaption()) || "cu_add1".equals(e.getField())) {
                                e.setValue(address);
                            }
                            if (latLng != null) {
                                if ("cu_lat".equals(e.getField())) {
                                    e.setValue(latLng.latitude + "");
                                    //经纬度
                                } else if ("cu_lng".equals(e.getField())) {
                                    e.setValue(latLng.longitude + "");
                                }
                            }
                        }
                    }
                }
            }
        }
        mBillAdapter.notifyDataSetChanged();
    }
}
