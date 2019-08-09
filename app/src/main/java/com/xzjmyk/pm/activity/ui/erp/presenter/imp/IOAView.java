package com.xzjmyk.pm.activity.ui.erp.presenter.imp;

import com.core.base.HttpImp;
import com.xzjmyk.pm.activity.ui.erp.model.oa.OAModel;

import java.util.List;

/**
 * Created by Bitliker on 2017/3/29.
 */

public interface IOAView extends HttpImp {

    void setDate(String date);

    void showModel(List<OAModel> models);

    void selectTag(boolean isMe);//当前选择的tag类型，1==下属  2==我的

    void setTaskList(List<Integer> taskList);

    void setCompletionNum(int AllNum, int okNum, int notNum);

}
