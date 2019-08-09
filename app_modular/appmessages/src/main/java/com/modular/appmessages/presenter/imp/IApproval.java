package com.modular.appmessages.presenter.imp;

import com.alibaba.fastjson.JSONArray;
import com.core.base.HttpImp;
import com.core.model.Approval;

import java.util.List;


/**
 * Created by Bitliker on 2017/7/7.
 */

public interface IApproval extends HttpImp {
    void sendToSelect(JSONArray array);

    void nodeDealMan(String nodeDealMan);

    void showModels(List<Approval> approvals,List<Approval> historyNodes);

    void initStatus();

    void endProcess();

    void showOpinion();

    void isApprove(boolean isApprove);
}
