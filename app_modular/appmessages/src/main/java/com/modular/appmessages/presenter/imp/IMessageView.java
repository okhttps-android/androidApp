package com.modular.appmessages.presenter.imp;

import com.core.model.Friend;
import com.core.utils.sortlist.BaseSortModel;
import com.modular.appmessages.model.MessageNew;

import java.util.List;

/**
 * Created by Bitliker on 2017/3/1.
 */

public interface IMessageView  {

    void showModel(List<BaseSortModel<Friend>> models);

    void clearSearch();

    void updateHeaderView(int type, int num, String subTitle,String time);

    void changeNet(boolean workConnected);

    void updateHeader(List<MessageNew> models);

    void updateSign(String message);

    void showProgress();
    void showToact(int resIds);
}
