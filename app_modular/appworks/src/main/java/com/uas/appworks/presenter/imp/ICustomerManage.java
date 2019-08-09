package com.uas.appworks.presenter.imp;

import com.core.base.HttpImp;
import com.modular.apputils.model.EasyBaseModel;

import java.util.List;

public interface ICustomerManage extends HttpImp {

    void setShowCustomerAdapter(List<EasyBaseModel> models);//设置客户看板适配器
    void setForgetCustomerAdapter(List<EasyBaseModel> models);//设置遗忘客户适配器
    void setCustomerCareAdapter(List<EasyBaseModel> models);//设置客户关怀适配器
    void setVisitAdapter(List<EasyBaseModel> models);//设置拜访统计适配器
}
