package com.core.base;

import android.os.Bundle;

import com.common.data.StringUtil;
import com.core.app.R;
import com.core.utils.ToastUtil;

/**
 * Created by Bitliker on 2017/1/17.
 */

public class OABaseActivity extends BaseActivity implements HttpImp {
    @Override
    public void showLoading() {
        try {
            progressDialog.show();
        } catch (Exception e) {

        }
    }

    @Override
    public void dimssLoading() {
        try {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {

        }
    }

    @Override
    public void showToast(String message, int colorId) {
        ToastUtil.showToast(ct, message);
    }

    @Override
    public void showToast(String message) {
        ToastUtil.showToast(ct, message);
    }

    @Override
    public void showToast(int reId, int colorId) {
        ToastUtil.showToast(ct, reId);

    }

    @Override
    public void showToast(int reId) {
        ToastUtil.showToast(ct, reId);
    }



    @Override
    public void setTitleStyles(int reid) {
        int theme = getSharedPreferences("cons", MODE_PRIVATE).getInt("theme", reid);
        setTheme(theme);
    }

    public void error(int what, int statuCode, String message, Bundle bundle) {
        if (!StringUtil.isEmpty(message)) {
            showToast(message, R.color.load_error);
            dimssLoading();
        }
    }

}
