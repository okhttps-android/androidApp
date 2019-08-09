package com.modular.apputils.presenter.imp;


import android.content.Intent;
import android.view.View;

import com.core.base.HttpImp;
import com.modular.apputils.model.BillGroupModel;

import java.util.List;

public interface IBill extends HttpImp {
    void setAdapter(List<BillGroupModel> groupModels);

    void setTitle(CharSequence title);

    void commitSuccess(int keyValue, String code);//提交成功

    void startActivityForResult(Intent intent, int requestCode);

    void updateFileOk();

    void addTopLayout(View ...view);
}
