package com.uas.appworks.OA.erp.presenter;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.OA.erp.model.FlightsModel;
import com.uas.appworks.OA.erp.model.FlightsTimeModel;
import com.uas.appworks.OA.erp.view.IFlightsView;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * Created by Bitliker on 2017/1/16.
 */

public class FlightsPresernter implements OnHttpResultListener {

    private final int WORK_DATA = 0x11;
    private final int WORK_DELETE = 0x12;
    private IFlightsView iFlightsView;
    private boolean isB2b;

    public FlightsPresernter(IFlightsView iFlightsView) {
        this.iFlightsView = iFlightsView;
    }

    public void start() {
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        loadDate();
    }

    private void loadDate() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        showLoading();
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bundle = null;
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getAllWorkData :
                "mobile/getAllWorkDate.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .setWhat(WORK_DATA)
                .setParam(param).bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    public void deleteWork(List<FlightsModel> models, int position) {
        if (!MyApplication.getInstance().isNetworkActive()) {
            showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        if (ListUtils.isEmpty(models)) return;
        FlightsModel model = null;
        try {
            model = models.get(position);
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        showLoading();
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "WorkDate");
        param.put("wdcode", model.getCode());
        param.put("wd_id", model.getId());
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        String url = "mobile/deleteWorkDate.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .setWhat(WORK_DELETE)
                .setParam(param).bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    private void showLoading() {
        if (iFlightsView != null) iFlightsView.showLoading();
    }

    private void dimssLoading() {
        if (iFlightsView != null) iFlightsView.dimssLoading();
    }

    private void showToast(String message, int colorId) {
        if (iFlightsView != null) iFlightsView.showToast(message, colorId);
    }

    private void showToast(int reId, int colorId) {
        if (iFlightsView != null) iFlightsView.showToast(reId, colorId);
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        switch (what) {
            case WORK_DATA:
                if (isB2b) {
                    JSONObject object = JSON.parseObject(message).getJSONObject("data");
                    handlerWorkByB2b(object);
                } else {
                    JSONArray array = parseObject(message).getJSONArray("listdata");
                    handlerWork(array);
                }
                break;
            case WORK_DELETE:
                dimssLoading();
                int position = bundle.getInt("position");
                iFlightsView.deleteModel(position);
                break;
        }
    }


    @Override
    public void error(int what, String message, Bundle bundle) {
        dimssLoading();
    }

    private void handlerWork(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            iFlightsView.dimssLoading();
            iFlightsView.showModel(new ArrayList<FlightsModel>());
            return;
        }
        JSONObject object = null;
        List<FlightsModel> models = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            if (object.containsKey("ifDefaultClass") && object.getBoolean("ifDefaultClass"))
                models.add(0, handlerDefFlights(object));
            else
                models.add(handlerFlights(object));
        }
        iFlightsView.dimssLoading();
        iFlightsView.showModel(models);
    }


    private void handlerWorkByB2b(JSONObject object) {
        List<FlightsModel> models = new ArrayList<>();
        models.add(handlerFlights(object));
        iFlightsView.dimssLoading();
        iFlightsView.showModel(models);
    }

    /**
     * 处理正常班次
     *
     * @param object
     * @return
     */
    private FlightsModel handlerFlights(JSONObject object) {
        FlightsModel model = new FlightsModel();
        FlightsTimeModel timeModel = new FlightsTimeModel();
        EmployeesModel employeesModel = new EmployeesModel();
        EmployeesModel hrorgsModel = new EmployeesModel();
        timeModel.setEarlyTime(JSONUtil.getInt(object, "wd_earlytime"));
        timeModel.setWd_ondutyone(JSONUtil.getText(object, "wd_ondutyone"));
        timeModel.setWd_offdutyone(JSONUtil.getText(object, "wd_offdutyone"));
        timeModel.setWd_ondutytwo(JSONUtil.getText(object, "wd_ondutytwo"));
        timeModel.setWd_offdutytwo(JSONUtil.getText(object, "wd_offdutytwo"));
        timeModel.setWd_ondutythree(JSONUtil.getText(object, "wd_ondutythree"));
        timeModel.setWd_offdutythree(JSONUtil.getText(object, "wd_offdutythree"));
        String emcodes = JSONUtil.getText(object, "wd_emcode");
        if (StringUtil.isEmpty(emcodes))
            emcodes = JSONUtil.getText(object, "emcodes");

        String emnames = JSONUtil.getText(object, "wd_man");
        if (StringUtil.isEmpty(emnames))
            emnames = JSONUtil.getText(object, "emnames");

        employeesModel.setEmployeecode(emcodes);
        employeesModel.setEmployeeNames(emnames);

        String emdefaultorcodes = JSONUtil.getText(object, "wd_defaultorcode");
        if (StringUtil.isEmpty(emdefaultorcodes))
            emdefaultorcodes = JSONUtil.getText(object, "emdefaultorcodes");

        String emdefaultors = JSONUtil.getText(object, "wd_defaultor");
        if (StringUtil.isEmpty(emdefaultors))
            emdefaultors = JSONUtil.getText(object, "emdefaultors");
        hrorgsModel.setEmployeecode(emdefaultorcodes);
        hrorgsModel.setEmployeeNames(emdefaultors);
        model.setId(JSONUtil.getInt(object, "id"));
//        model.setCount(JSONUtil.getInt(object, "wd_pcount"));
        model.setCode(JSONUtil.getText(object, "wd_code"));
        model.setTimeModel(timeModel);
        if (isB2b)
            model.setDay(JSONUtil.getText(object, "wd_day", "day"));
        else
            model.setDay(JSONUtil.getText(object, "wd_day"));//返回为1，2，3
        model.setEmployeesModel(employeesModel);
        model.setHrorgsModel(hrorgsModel);
        model.setType(isB2b ? 2 : 1);
        model.setName(JSONUtil.getText(object, "wd_name"));
        try {
            String names = employeesModel.getEmployeeNames();
            if (!StringUtil.isEmpty(names)) {
                model.setCount(names.split(",").length);
            }
            String hrorgs = hrorgsModel.getEmployeeNames();
            if (!StringUtil.isEmpty(hrorgs)) {
                model.setDepartments(hrorgs.split(",").length);
            }
        } catch (Exception e) {

        }
        return model;
    }

    /**
     * 处理默认班次
     *
     * @param object
     * @return
     */
    private FlightsModel handlerDefFlights(JSONObject object) {
        FlightsModel model = new FlightsModel();
        FlightsTimeModel timeModel = new FlightsTimeModel();
        EmployeesModel employeesModel = new EmployeesModel();
        EmployeesModel hrorgsModel = new EmployeesModel();
        timeModel.setEarlyTime(JSONUtil.getInt(object, "wd_earlytime"));
        timeModel.setEarlyTime(JSONUtil.getInt(object, "wd_earlytime"));
        timeModel.setWd_ondutyone(JSONUtil.getText(object, "wd_ondutyone"));
        timeModel.setWd_offdutyone(JSONUtil.getText(object, "wd_offdutyone"));
        timeModel.setWd_ondutytwo(JSONUtil.getText(object, "wd_ondutytwo"));
        timeModel.setWd_offdutytwo(JSONUtil.getText(object, "wd_offdutytwo"));
        timeModel.setWd_ondutythree(JSONUtil.getText(object, "wd_ondutythree"));
        timeModel.setWd_offdutythree(JSONUtil.getText(object, "wd_offdutythree"));
        model.setTimeModel(timeModel);
        model.setEmployeesModel(employeesModel);
        model.setHrorgsModel(hrorgsModel);
        JSONObject o = null;
        if (object.containsKey("Class1")) {
            o = object.getJSONObject("Class1");
            timeModel.setWd_ondutyone(getByClass(o, true));
            timeModel.setWd_offdutyone(getByClass(o, false));
        }
        if (object.containsKey("Class2")) {
            o = object.getJSONObject("Class2");
            timeModel.setWd_ondutytwo(getByClass(o, true));
            timeModel.setWd_offdutytwo(getByClass(o, false));
        }
        if (object.containsKey("Class3")) {
            o = object.getJSONObject("Class3");
            timeModel.setWd_ondutythree(getByClass(o, true));
            timeModel.setWd_offdutythree(getByClass(o, false));
        }
        String emnames =JSONUtil.getText(object, "emnames", "defaultmancode");
        employeesModel.setEmployeecode(emnames);
        employeesModel.setEmployeeNames(JSONUtil.getText(object, "defaultman"));
        if (!StringUtil.isEmpty(emnames)) {
            String[] es = emnames.split(",");
            model.setCount(es.length);
        }
        model.setId(JSONUtil.getInt(object, "wd_id"));
        model.setCode(JSONUtil.getText(object, "wd_code"));
        model.setTimeModel(timeModel);
        model.setDay(JSONUtil.getText(object, "wd_day"));//返回为1，2，3
        model.setEmployeesModel(employeesModel);
        model.setHrorgsModel(hrorgsModel);
        model.setType(2);
        model.setName(JSONUtil.getText(object, "wd_name"));

        try {
            String names = employeesModel.getEmployeeNames();
            if (!StringUtil.isEmpty(names)) {
                model.setCount(names.split(",").length);
            }
            String hrorgs = hrorgsModel.getEmployeeNames();
            if (!StringUtil.isEmpty(hrorgs)) {
                model.setDepartments(hrorgs.split(",").length);
            }
        } catch (Exception e) {

        }
        return model;
    }

    private String getByClass(JSONObject object, boolean work) {
        return work ? (object.containsKey("wd_onduty") ? object.getString("wd_onduty") : "") : (
                object.containsKey("wd_offduty") ? object.getString("wd_offduty") : "");
    }
}
