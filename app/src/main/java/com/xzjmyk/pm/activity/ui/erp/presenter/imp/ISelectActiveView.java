package com.xzjmyk.pm.activity.ui.erp.presenter.imp;

import com.core.base.HttpImp;
import com.core.utils.sortlist.BaseSortModel;
import com.core.model.SelectEmUser;

import java.util.List;

/**
 * Created by Bitliker on 2017/2/14.
 */

public interface ISelectActiveView extends HttpImp {
    void addExist(String firstLetter);

    void showModel(List<BaseSortModel<SelectEmUser>> models);

    void showNumber(int number);

    void showSureText(String text);

    void isAllClicked(boolean clickAll);

}
