package com.uas.appworks.CRM.erp.imp;

import com.core.base.HttpImp;
import com.core.widget.view.model.SelectAimModel;

import java.util.List;

/**
 * Created by Bitliker on 2017/1/12.
 */

public interface ISelectAim extends HttpImp {

    void showModel(List<SelectAimModel> models);
}
