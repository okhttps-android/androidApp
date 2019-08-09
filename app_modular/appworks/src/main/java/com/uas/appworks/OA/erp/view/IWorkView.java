package com.uas.appworks.OA.erp.view;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.core.base.HttpImp;
import com.core.model.WorkModel;

import java.util.List;

/**
 * 管理器向显示界面连接的接口
 * Created by Bitliker on 2016/12/12.
 */
public interface IWorkView extends HttpImp {

    void showDistance(float distance);//显示距离

    void showLocation(String location);//显示当前位置

    void showModel(boolean isFree, List<WorkModel> models);//显示数据

    void showFristMac();//显示第一次打开是否同意提交

    void showErrorMac();//显示mac地址错误提示

    void showNotLocation();//显示没有获取到当前位置

    void setPois(List<PoiInfo> pois, LatLng latLng);//设置当前允许的条件下获取到的所有的附近位置

    void setErrorMac(String message);//设置当前mac地址错误提示信息
}
