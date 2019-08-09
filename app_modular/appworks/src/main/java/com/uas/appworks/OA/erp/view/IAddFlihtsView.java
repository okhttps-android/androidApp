package com.uas.appworks.OA.erp.view;


import com.core.base.HttpImp;
import com.uas.appworks.OA.erp.model.FlightsModel;
import com.core.model.SelectEmUser;

import java.util.ArrayList;

/**
 * Created by Bitliker on 2017/2/9.
 */

public interface IAddFlihtsView extends HttpImp {

    void updateName(String name);//更新数据

    void updateTime(String time);//更新数据

    void updateDate(String date, boolean isUpdate);//更新数据

    void updateDepartment(String department);//更新数据

    void updateMunber(String munber);//更新数据

    void endActivity(FlightsModel model, boolean isUpdate);

    void showCollisionMan(ArrayList<SelectEmUser> mans);

    void showCollisionDefaultir(ArrayList<SelectEmUser> defaultirs);

    void setClickAble(boolean isClickAble);

    void isB2b(boolean isB2b);

}
