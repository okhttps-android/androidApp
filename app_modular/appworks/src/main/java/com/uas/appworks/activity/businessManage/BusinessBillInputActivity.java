package com.uas.appworks.activity.businessManage;

import android.content.Intent;
import android.widget.Toast;

import com.core.app.Constants;
import com.modular.apputils.activity.BillInputActivity;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/25 10:03
 */
public class BusinessBillInputActivity extends BillInputActivity {

    @Override
    public void toDataFormList() {
        startActivity(new Intent(mContext, BusinessCompanyListActivity.class)
                .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, BusinessCompanyListActivity.PAGE_BUSINESS_COMPANY));
    }

    @Override
    public void commitSuccess(int keyValue, String code) {
        Toast.makeText(this, "商机新建成功", Toast.LENGTH_LONG).show();
        startActivity(new Intent(mContext, BusinessCompanyListActivity.class)
                .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, BusinessCompanyListActivity.PAGE_BUSINESS_COMPANY));
        finish();
    }

}
