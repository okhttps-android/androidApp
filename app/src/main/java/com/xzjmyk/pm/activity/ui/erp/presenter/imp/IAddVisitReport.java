package com.xzjmyk.pm.activity.ui.erp.presenter.imp;

import com.core.base.HttpImp;

import java.util.List;

/**
 * Created by Bitliker on 2017/5/4.
 */

public interface IAddVisitReport extends HttpImp {
    void finish();

    void showContact(List<String> contactNames);
}
