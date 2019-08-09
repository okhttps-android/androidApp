package com.uas.appworks.presenter.imp;

import com.modular.apputils.model.BillGroupModel;

import java.util.List;

public interface IContact {
    void updateStatus(String status);

    void deleteDetailOk(int deleteIndex);
    void setFilePaths(int mGroupIndex,List<BillGroupModel.LocalData> mLocalDatas);
}
