package com.uas.appworks.crm3_0.activity;


import android.content.Intent;
import android.view.Menu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.utils.ToastUtil;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.presenter.BillPresenter;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CustomerContactAdapter;
import com.uas.appworks.presenter.CustomerContactBillPresenter;
import com.uas.appworks.presenter.imp.IContact;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * 客户联系人详情|录入界面
 */
public class CustomerContactActivity extends BillInputActivity {
    private boolean isMe;


    @Override
    public void setAdapter(List<BillGroupModel> groupModels) {
        super.setAdapter(groupModels);
    }

    @Override
    public BillPresenter newBillPresenter() {
        return new CustomerContactBillPresenter(this, this, new IContact() {
            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void deleteDetailOk(int deleteIndex) {
                ((CustomerContactAdapter) mBillAdapter).superDeleteGroup(deleteIndex);
            }

            @Override
            public void setFilePaths(int mGroupIndex, List<BillGroupModel.LocalData> mLocalDatas) {
                if (mBillAdapter != null) {
                    BillGroupModel mBillModel = mBillAdapter.getBillGroupModel(mGroupIndex);
                    if (mBillModel != null && !ListUtils.isEmpty(mBillModel.getShowBillFields())) {
                        for (BillGroupModel.BillModel billModel : mBillModel.getShowBillFields()) {
                            if (billModel.getType().equals("FF")) {
                                billModel.setLocalDatas(mLocalDatas);
                                mBillAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            }
        });
    }

    @Override
    public BillAdapter newBillAdapter(List<BillGroupModel> groupModels) {
        return new CustomerContactAdapter(ct, groupModels, this, new CustomerContactAdapter.CustomerContactAdapterListener() {
            @Override
            public void deleteGroup(int groupIndex, int ctId) {
                getPresenter().deleteDetail(groupIndex, ctId);
            }
        });
    }

    @Override
    public void commitSuccess(int keyValue, String code) {
        ToastUtil.showToast(ct, "保存成功！！");
        finish();
    }

    @Override
    public void init() {
        super.init();
        Intent intent = getIntent();
        if (intent != null) {
            isMe = intent.getBooleanExtra(Constants.Intents.MY_DOIT, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMe) {
            return super.onCreateOptionsMenu(menu);
        } else {
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_DB_FIND:
                if (data == null) return;
                String json = data.getStringExtra("data");
                int groupIndex = data.getIntExtra("groupId", 0);
                boolean isForm = data.getBooleanExtra("isForm", true);
                JSONObject object = JSON.parseObject(json);
                int cu_id = JSONUtil.getInt(object, "cu_id");
                handlerSelectDbFind(object, groupIndex, isForm);
                if (cu_id > 0) {
                    getPresenter().updateId(cu_id);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public CustomerContactBillPresenter getPresenter() {
        return (CustomerContactBillPresenter) mBillPresenter;
    }
}
