package com.uas.appworks.CRM.erp.presenter;

import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.widget.view.model.SelectAimModel;
import com.uas.applocation.Interface.OnSearchLocationListener;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationNeerHelper;
import com.uas.applocation.utils.ModelChangeUtils;
import com.uas.appworks.CRM.erp.imp.ISelectAim;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitliker on 2017/1/12.
 */

public class SelectAimPresenter implements OnHttpResultListener {
    private ISelectAim iSelectAim;
    private final int LOAD = 0x11;
    private final int LOAD_CUSTOMER = 0x12;//拜访计划客户名称数据
    private List<SelectAimModel> models = new ArrayList<>();//从服务其返回的数据列表
    private List<SelectAimModel> keyList;//搜索企业筛选的人员
    private int seachMapType = 0;//下拉百度地图数据类型  1.获取附近数据  2.获取全国数据  2.获取城市数据
    private String keyWork;
    private boolean isB2b;


    public SelectAimPresenter(ISelectAim iSelectAim) {
        if (iSelectAim == null) {
            new NullPointerException("ISelectAim is null");
        }
        this.iSelectAim = iSelectAim;
    }

    /******
     * start 对外接口
     **********/
    public void start(int type) {
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (type == 1 && !isB2b) {
            iSelectAim.setTitle(MyApplication.getInstance().getString(R.string.select_client));
            loadCustomerData();
        } else {
            loadOutAddress();
        }
    }

    public void seachByKey(String keyWork) {
        this.keyWork = keyWork;
        if (keyList == null) {
            keyList = new ArrayList<>();
        } else {
            keyList.clear();
        }
        if (ListUtils.isEmpty(models)) {
            if (StringUtil.isEmpty(keyWork)) {
                loadNeer();
            } else {
                loadDataByChina();
            }
        } else {
            if (StringUtil.isEmpty(keyWork)) {
                for (SelectAimModel m : models)
                    m.setFirst(false);
                iSelectAim.showModel(models);
                return;
            } else {
                boolean isFirst = true;
                for (SelectAimModel e : models) {
                    if (isIncude(keyWork, e)) {
                        if (isFirst) {
                            e.setFirst(true);
                            isFirst = false;
                        } else
                            e.setFirst(isFirst);
                        keyList.add(e);
                    }
                }
                loadDataByChina();
            }

        }

    }

    /******
     * end 对外接口
     **********/


    /***********************
     * 内部计算函数
     ***********************************/

    //从服务器获取数据
    private void loadOutAddress() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            if (iSelectAim != null)
                iSelectAim.showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        if (iSelectAim != null)
            iSelectAim.showLoading();

        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        param.put(isB2b ? "pageNumber" : "pageIndex", 1);
        param.put("pageSize", 1000);
        String condition = "1=1";
        if (!isB2b) {
            condition = "md_emcode='" + CommonUtil.getEmcode() + "'";
        }
        param.put("condition", condition);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getOutAddress : "mobile/getOutAddressDate.action";
        Request request = new Request.Bulider()
                .setMode(Request.Mode.GET)
                .setUrl(url)
                .setWhat(LOAD)
                .setParam(param).bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    /**
     * @desc：客户拜访下拉 客户名称
     * @author：Arison on 2017/1/17
     */
    private void loadCustomerData() {
        log("loadOutAddress");
        if (!MyApplication.getInstance().isNetworkActive()) {
            if (iSelectAim != null)
                iSelectAim.showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        if (iSelectAim != null)
            iSelectAim.showLoading();

        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        param.put("sellercode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        String url = "mobile/crm/getCustomerbySeller.action";
        Request request = new Request.Bulider()
                .setMode(Request.Mode.GET)
                .setUrl(url)
                .setWhat(LOAD_CUSTOMER)
                .setParam(param).bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    //下载附近数据
    private void loadNeer() {
        seachMapType = 1;
        LocationNeerHelper.getInstance().loadDataByNeer(MyApplication.getInstance(), 200, 0, mOnSearchLocationListener);
    }

    //下载全国数据
    private void loadDataByChina() {
        seachMapType = 2;
        LocationNeerHelper.getInstance().searchByInput(MyApplication.getInstance(), "中国", keyWork, 0, mOnSearchLocationListener);
    }

    //下载本城市数据
    private void loadDataByCity() {
        seachMapType = 3;
        LocationNeerHelper.getInstance().searchByInput(MyApplication.getInstance(), null, keyWork, 0, mOnSearchLocationListener);
    }

    private OnSearchLocationListener mOnSearchLocationListener = new OnSearchLocationListener() {
        @Override
        public void onCallBack(boolean isSuccess, List<UASLocation> locations) {
            if (StringUtil.isEmpty(keyWork) && !ListUtils.isEmpty(models)) {
                for (SelectAimModel e : models)
                    e.setFirst(false);
                iSelectAim.showModel(models);
            } else if (!isSuccess) {
                if (seachMapType == 1) {
                    loadDataByChina();
                    return;
                } else if (seachMapType == 2) {
                    loadDataByCity();
                    return;
                } else {
                    showEmpty();
                    return;
                }
            } else {
                if (seachMapType==1&&!StringUtil.isEmpty(keyWork)){
                    return;
                }
                SelectAimModel model = null;
                boolean isFirst = true;
                List<PoiInfo> chches = new ArrayList<>();
                for (UASLocation e : locations) {
                    chches.add(ModelChangeUtils.location2PoiInfo(e));
                }
                List<SelectAimModel> chchesModels=new ArrayList<>();
                for (int i = 0; i < chches.size(); i++) {
                    if (StringUtil.isEmpty(chches.get(i).name) || StringUtil.isEmpty(chches.get(i).address))
                        continue;
                    model = new SelectAimModel();
                    model.setType(3);
                    model.setName(chches.get(i).name);
                    model.setAddress(chches.get(i).address);
                    model.setLatLng(chches.get(i).location);
                    if (isFirst) {
                        model.setFirst(true);
                        isFirst = false;
                    } else {
                        model.setFirst(false);
                    }
                    chchesModels.add(model);
                }
                if (ListUtils.isEmpty(chchesModels)) {
                    showEmpty();
                } else {
                    List<SelectAimModel> show = new ArrayList<>();
                    if (!ListUtils.isEmpty(keyList))
                        show.addAll(keyList);
                    show.addAll(chchesModels);
                    iSelectAim.showModel(show);
                }
            }

        }
    };


    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (isJSON && (what == LOAD || what == LOAD_CUSTOMER)) {
            JSONArray array = JSON.parseObject(message).getJSONArray(what == LOAD ? "data" : "customers");
            if (what == LOAD) {
                handleDada(array);
            } else {
                handleDadaForCustomer(array);
            }
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        if (iSelectAim != null)
            iSelectAim.dimssLoading();
    }


    //处理请求下来的数据
    private void handleDada(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            loadNeer();
            if (iSelectAim != null)
                iSelectAim.dimssLoading();
            return;
        }
        SelectAimModel model = null;
        JSONObject object = null;
        String company = isB2b ? "company" : "MD_COMPANY";
        String address = isB2b ? "md_address" : "MD_ADDRESS";
        String md_latitude = isB2b ? "md_latitude" : "MD_LATITUDE";
        String md_longitude = isB2b ? "md_longitude" : "MD_LONGITUDE";
        String md_visitcount = isB2b ? "md_visitcount" : "MD_VISITCOUNT";
        String md_visittime = isB2b ? "md_visittime" : "MD_VISITTIME";
        List<SelectAimModel> models = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            try {
                model = new SelectAimModel();
                object = array.getJSONObject(i);
                model.setAddress(object.getString(address));
                model.setTimes(object.getInteger(md_visitcount));
                model.setName(object.getString(company));
//                model.seti(object.getInteger("MD_ID"));
                model.setType(2);
                float longitude = object.getFloatValue(md_longitude);
                float latitude = object.getFloatValue(md_latitude);
                if (longitude != 0 && latitude != 0) {
                    model.setLatLng(new LatLng(latitude, longitude));
                }
                try {
                    model.setTime(TimeUtils.f_long_2_str(object.getLong(md_visittime)));
                } catch (Exception e) {
                    if (e != null)
                        log("e.getMessage" + e.getMessage());
                }
                models.add(model);

            } catch (Exception e) {

            }
        }
        showByLoad(models);
    }


    //处理请求下来的数据
    private void handleDadaForCustomer(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            loadNeer();
            if (iSelectAim != null)
                iSelectAim.dimssLoading();
            return;
        }
        SelectAimModel model = null;
        JSONObject object = null;
        List<SelectAimModel> models = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            try {
                model = new SelectAimModel();
                object = array.getJSONObject(i);
                model.setAddress(object.getString("CU_ADD1"));
                // model.setTimes(object.getInteger("MD_VISITCOUNT"));
                model.setName(object.getString("CU_NAME"));
                model.setObject(JSON.toJSONString(object));
                model.setTimes(-1);
//                model.seti(object.getInteger("MD_ID"));
                model.setType(2);
                models.add(model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showByLoad(models);
    }


    private void showByLoad(List<SelectAimModel> models) {
        this.models = models;
        if (iSelectAim != null) {
            iSelectAim.showModel(models);
            iSelectAim.dimssLoading();
        }
    }

    private void showEmpty() {
        if (!StringUtil.isEmpty(keyWork)) {//有搜索
            if (keyList == null || hasEmpty(keyList)) {
                keyList = new ArrayList<>();
            }
            show(keyList);
        } else
            show(models);
    }

    private boolean hasEmpty(List<SelectAimModel> keyList) {
        if (ListUtils.isEmpty(keyList)) return false;
        for (SelectAimModel e : keyList)
            if (e.getType() == 1) return true;
        return false;
    }

    private void show(List<SelectAimModel> models) {
        if (iSelectAim != null) {
            iSelectAim.showModel(models);
            iSelectAim.dimssLoading();
        }
    }

    private boolean isIncude(String key, SelectAimModel model) {
        String text = model.getAddress() + model.getTime() + model.getName();
        boolean isInclude = StringUtil.isInclude(text, key);
        LogUtil.i("gong", "isIncude=" + "\n text=" + text + "\n key=" + key + " ==" + isInclude);
        return isInclude;
    }

    private void log(String message) {
        try {
            if (!AppConfig.DEBUG || StringUtil.isEmpty(message)) return;
            Log.i("gongpengming", message);
        } catch (Exception e) {
            if (e != null)
                Log.i("gongpengming", "show Exception" + e.getMessage());
        }
    }
}