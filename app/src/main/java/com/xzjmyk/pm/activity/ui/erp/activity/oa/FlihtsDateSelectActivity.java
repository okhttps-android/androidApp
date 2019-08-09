package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.OA.erp.model.FlightsModel;
import com.uas.appworks.OA.erp.model.FlightsTimeModel;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.view.calenderlist.DatePickerController;
import com.xzjmyk.pm.activity.ui.erp.view.calenderlist.DayPickerView;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.widget.MyListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * Created by FANGlh on 2017/4/20.
 * function:
 */
public class FlihtsDateSelectActivity extends BaseActivity implements DatePickerController {
    private static final int GET_FLIHTS_DATAS = 42001;
    private static final int FLIGHTS_UPDATE = 17503;
    JSONObject object = null;
    List<FlightsModel> models;
    private PopupWindow popupWindow = null;
    private MyListView flihts_list;
    private TextView rest_tv;
    private FlihtsDatasAdapter myAdapter;
    private Boolean isB2b;
    private DayPickerView dayPickerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        initView();
    }
    private void initView() {
        setContentView(R.layout.flihts_calendar_select);
        dayPickerView = (DayPickerView) findViewById(R.id.pickerView);
        dayPickerView.initDraw(this);
        models = new ArrayList<>();
        myAdapter = new FlihtsDatasAdapter();
        getFlihtsData();
    }
    @Override
    public int getMaxYear() {
        return 2018;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        Log.i("Day_Selected", year + "-" + Integer.valueOf(month + 1) + "-" + day);

        String date = year+CommonUtil.getZeroNumber(Integer.valueOf(month + 1))+CommonUtil.getZeroNumber(day);
        Log.i("date", date + "");
        if (!ListUtils.isEmpty(models) &&!StringUtil.isEmpty(date)){
            LogUtil.prinlnLongMsg("model",JSON.toJSONString(models));
            showFlihtsWindows(models, date);
        }
    }

    private void showFlihtsWindows(final List<FlightsModel> models, final String date) {
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.flihts_listview, null);

        flihts_list = (MyListView) contentView.findViewById(R.id.flihts_list);
        rest_tv = (TextView) contentView.findViewById(R.id.rest_tv);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 200);
        myAdapter.setModels(models);
        flihts_list.setAdapter(myAdapter);
        Log.i("sb", "setAdapter");

        ((MyListView) contentView.findViewById(R.id.flihts_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToastMessage(models.get(position).getName() + getString(R.string.sign_flights));
                daDateUpdate(models, date, position);
                popupWindow.dismiss();
            }
        });

        rest_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastMessage(getString(R.string.rest));
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_main));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }

    private void daDateUpdate(List<FlightsModel> models, String date,int position) {
        String url = CommonUtil.getAppBaseUrl(getApplicationContext())+"/mobile/updateEmpWorkDate.action";
//        String url = "http://192.168.253.252:8080/ERP/"+"mobile/updateEmpWorkDate.action";
        Map<String,Object> param = new HashMap<>();
        param.put("deptcodes", models.get(position).getHrorgsModel().getEmployeecode());
        param.put("emcodes",models.get(position).getEmployeesModel().getEmployeecode() == null ? "":
                models.get(position).getEmployeesModel().getEmployeecode());
        param.put("date",date);
        param.put("workcode", models.get(position).getCode() == null ? "" : models.get(position).getCode());
        Log.i("param", JSON.toJSONString(param));
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, FLIGHTS_UPDATE, null, null, "post");

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GET_FLIHTS_DATAS:
                    if (msg.getData() != null){
                        String fliht_result = msg.getData().getString("result");
                        JSONArray fliht_array = parseObject(fliht_result).getJSONArray("listdata");
                        if (!ListUtils.isEmpty(fliht_array)) {
                            handlerWork(fliht_array);
                        }
                    }
                    break;
                
                case FLIGHTS_UPDATE:
                    if (msg.getData() != null){
                        String flights_update_result = msg.getData().getString("result");
                        Log.i("flights_update_result",flights_update_result);
                        JSONObject resultJsonObject = JSON.parseObject(flights_update_result);
                        if (resultJsonObject == null){
                            ToastMessage(getString(R.string.update_failed));
                            return;
                        }else if (resultJsonObject.getBooleanValue("success")){
                            ToastMessage(getString(R.string.update_success));
                        }

                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };

    private void handlerWork(JSONArray array) {
        if (!ListUtils.isEmpty(models)) models.clear();
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            if (object.containsKey("ifDefaultClass") && object.getBoolean("ifDefaultClass"))
                models.add(0, handlerDefFlights(object));
            else
                models.add(handlerFlights(object));
           if (i == array.size() -1){
               Log.i("sb","showFlihtsWindows()");
            }
        }
    }

    private void getFlihtsData() {
        String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/getAllWorkDate.action";
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, GET_FLIHTS_DATAS, null, null, "post");
    }



    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(FlihtsDateSelectActivity.this, 1f);
            }
        });
    }

    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }


    public class FlihtsDatasAdapter extends BaseAdapter {
        List<FlightsModel> models;

        public List<FlightsModel> getModels() {return models;}
        public void setModels(List<FlightsModel> models) {this.models = models;}
        @Override
        public int getCount() {return models == null ? 0 : models.size();}
        @Override
        public Object getItem(int position) {
            return models.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView =  View.inflate(mContext, R.layout.item_flihts,null);
                viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (models.get(position).getType() == 2){
                viewHolder.docmainmsg_list.setText(models.get(position).getName());
            }else {
                viewHolder.docmainmsg_list.setText(models.get(position).getName() + " "+getString(R.string.sign_flights));
            }
            viewHolder.docmainmsg_value.setText(models.get(position).getTimeTable());
            return convertView;
        }
        class ViewHolder{
            TextView docmainmsg_list;
            TextView docmainmsg_value;
        }
    }

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
        employeesModel.setEmployeecode(JSONUtil.getText(object, "defaultmancode"));
        employeesModel.setEmployeeNames(JSONUtil.getText(object, "defaultman"));
//        hrorgsModel.setEmployeecode(JSONUtil.getText(object, "hrorgcode"));
//        hrorgsModel.setEmployeeNames(JSONUtil.getText(object, "hrorgname"));
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

    private String getByClass(JSONObject object, boolean work) {
        return work ? (object.containsKey("wd_onduty") ? object.getString("wd_onduty") : "") : (
                object.containsKey("wd_offduty") ? object.getString("wd_offduty") : "");
    }
}
