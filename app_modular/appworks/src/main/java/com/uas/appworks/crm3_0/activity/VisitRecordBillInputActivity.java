package com.uas.appworks.crm3_0.activity;


import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.core.app.Constants;
import com.core.utils.CommonUtil;
import com.modular.apputils.activity.BillDetailsActivity;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.activity.BillListActivity;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.model.BillListConfig;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

//拜访报告录入界面
public class VisitRecordBillInputActivity extends BillInputActivity {

//    @Override
//    public void setAdapter(List<BillGroupModel> groupModels) {
//        if (!ListUtils.isEmpty(groupModels)) {
//            BillGroupModel groupModel = groupModels.get(0);
//            if (groupModel != null && !ListUtils.isEmpty(groupModel.getShowBillFields())) {
//                for (BillGroupModel.BillModel billModel : groupModel.getShowBillFields()) {
//                    if ("HOS".equals(billModel.getType()) && "vr_group".equals(billModel.getField())) {
//                        billModel.setType("C");
//                        break;
//                    }
//                }
//            }
//        }
//        super.setAdapter(groupModels);
//    }

    public void toDataFormList() {
        String emCode = CommonUtil.getEmcode();
        ArrayList<BillListConfig> billListConfigs = new ArrayList<>();
        BillListConfig mBillListConfig = new BillListConfig();
        mBillListConfig.setTitle("我负责的");
        mBillListConfig.setCaller(mBillPresenter.getFormCaller());
        mBillListConfig.setCondition("vr_recordercode=\'" + emCode + "\'");
        billListConfigs.add(mBillListConfig);
        mBillListConfig = new BillListConfig();
        mBillListConfig.setTitle("我下属的");
        mBillListConfig.setCaller(mBillPresenter.getFormCaller());
        String mCondition = "(" +
                "vr_recordercode in ( " +
                " select em_code from employee left join job on em_defaulthsid=jo_id  where jo_subof= " +
                " (select em_defaulthsid from employee where em_code =\'" + emCode + "\') " +
                ")" +
                ")";
        mBillListConfig.setCondition(mCondition);
        billListConfigs.add(mBillListConfig);
        startActivity(new Intent(ct, BillListActivity.class)
                .putExtra(Constants.Intents.CONFIG, billListConfigs)
                .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                .putExtra(Constants.Intents.DETAILS_CLASS, BillDetailsActivity.class)
        );
    }

    @Override
    public void commitSuccess(final int keyValue, String code) {
        ToastMessage("提交成功！");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ct == null) return;
                startActivity(new Intent(ct, BillDetailsActivity.class)
                        .putExtra(Constants.Intents.CALLER, mBillPresenter.getFormCaller())
                        .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                        .putExtra(Constants.Intents.ID, keyValue)
                        .putExtra(Constants.Intents.INPUT_CLASS, VisitRecordBillInputActivity.class)
                        .putExtra(Constants.Intents.MY_DOIT, true));
                finish();
                overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
            }
        }, 2000);
    }

    @Override
    public void toSelect(int position, BillGroupModel.BillModel model) {
        if ("cup_name".equals(model.getField())) {
            String master = CommonUtil.getMaster();
            if ("DATACENTER".equals(master) || "N_SHYZ".equals(master) || "N_AJC".equals(master)) {
                List<BillGroupModel.BillModel> allBillModels = mBillAdapter.getmAllBillModels();
                String vr_cuuu = "", vr_prjcode = "", vr_prjandcus_user = "";
                for (BillGroupModel.BillModel billModel : allBillModels) {
                    if (!TextUtils.isEmpty(billModel.getValue())) {
                        if ("vr_cuuu".equals(billModel.getField())) {
                            vr_cuuu = billModel.getValue();
                        }
                        if ("vr_prjcode".equals(billModel.getField())) {
                            vr_prjcode = billModel.getValue();
                        }
                    }
                    if ("vr_prjandcus_user".equals(billModel.getField())) {
                        vr_prjandcus_user = billModel.getValue();
                    }
                }

                if (TextUtils.isEmpty(vr_cuuu) && TextUtils.isEmpty(vr_prjcode)) {
                    toast("请先选择客户编号或项目编号");
                    return;
                }
                String condition = "sign=\'" + vr_prjandcus_user + "\'";
                findBydbFind(model, condition);
            } else {
                super.toSelect(position, model);
            }
        } else {
            super.toSelect(position, model);
        }
    }

    //    @Override
//    public void toSelect(int position, BillGroupModel.BillModel model) {
//        if ("C".equals(model.getType()) && "vr_group".equals(model.getField())) {
//            LogUtil.i("gong", "position=" + position);
//            selectPosition = position;
//            doSelectDealMan();
//        } else {
//            super.toSelect(position, model);
//        }
//
//    }
//
//    private void doSelectDealMan() {
//        Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
//        SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
//                .setTitle("选择可阅读人")
//                .setSingleAble(false);
//        intent.putExtra(OAConfig.MODEL_DATA, bean);
//        startActivityForResult(intent, 0x265);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 0x265 && resultCode == 0x20 && data != null) {
//            ArrayList<SelectEmUser> selectEmUsers = data.getParcelableArrayListExtra("data");
//            if (!ListUtils.isEmpty(selectEmUsers)) {
//                StringBuilder names = new StringBuilder();
//                for (SelectEmUser selectEmUser : selectEmUsers) {
//                    names.append(selectEmUser.getEmName() + ";");
//                }
//                StringUtil.removieLast(names);
//                mBillAdapter.updateBillModelValues(selectPosition, names.toString(), names.toString());
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
}
