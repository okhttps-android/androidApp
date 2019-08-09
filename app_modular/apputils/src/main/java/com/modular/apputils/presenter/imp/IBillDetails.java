package com.modular.apputils.presenter.imp;

import com.core.base.HttpImp;
import com.modular.apputils.model.BillGroupModel;

import java.util.List;

public interface IBillDetails extends HttpImp {
    void updateStatus(String status);

    void setAdapter(List<BillGroupModel> groupModels);

    void setFilePaths(List<BillGroupModel.LocalData> mLocalDatas);

    void updateDetail(List<BillGroupModel> mGroupModels);

    void updateFileOk();
}
