package com.uas.appworks.OA.erp.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import com.core.model.OAConfig;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.OA.erp.model.FlightsModel;
import com.uas.appworks.OA.erp.model.FlightsTimeModel;
import com.uas.appworks.OA.erp.model.HrorgsModel;
import com.core.model.SelectEmUser;
import com.uas.appworks.OA.erp.view.IAddFlihtsView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitliker on 2017/2/9.
 */

public class AddFlihtsPresenter implements OnHttpResultListener {
    private final int SUBMIT = 0x11;
    private final int MAN_DEFAULTOR = 0x12;
    private FlightsModel model = null;//唯一模型
    boolean isUpdate = false;//判断是否是更新，不然是保存

    private IAddFlihtsView iAddFlihtsView;
    private int conuInDefaultor = 0;
    private boolean isB2b;

    public AddFlihtsPresenter(IAddFlihtsView iAddFlihtsView) {
        this.iAddFlihtsView = iAddFlihtsView;
    }

    public void start(Intent intent) {
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        model = intent.getParcelableExtra("data");
        isUpdate = intent.getBooleanExtra("isUpdate", false);
        String title = null;
        if (model == null) {
            title = MyApplication.getInstance().getString(R.string.add_flihts);
            model = new FlightsModel();
        } else {
            title = MyApplication.getInstance().getString(R.string.edit_flihts);
            if (model.getEmployeesModel() != null)
                iAddFlihtsView.updateMunber(model.getEmployeesModel().getEmployeeNames());
            if (model.getHrorgsModel() != null)
                iAddFlihtsView.updateDepartment(model.getHrorgsModel().getEmployeeNames());
            String time = model.getTime();
            String date = model.getWeek();
            iAddFlihtsView.updateDate(date, isUpdate);
            iAddFlihtsView.updateTime(time);
            iAddFlihtsView.updateName(model.getName());
            iAddFlihtsView.setClickAble(!(MyApplication.getInstance().getString(R.string.default_work).equals(model.getName()) || model.getType() == 2));
            iAddFlihtsView.isB2b(isB2b);
        }
        iAddFlihtsView.setTitle(title);
    }

    public void submit(String name) {
        name = StringUtil.toHttpString(name);
        name = name.replace("\n", "").replace(" ", "").replace("%", "");
        if (StringUtil.isEmpty(name)) {
            iAddFlihtsView.showToast(R.string.not_allowed_name, R.color.load_warning);
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "WorkDate");
        Map<String, Object> formStore = new HashMap<>();
        //id和code
        if (model != null && model.getId() != 0)
            formStore.put("wd_id", model.getId());
        else formStore.put("wd_id", "");
        if (model != null && !StringUtil.isEmpty(model.getCode()))
            formStore.put("wd_code", model.getCode());
        else formStore.put("wd_code", "");
        //班次名称
        formStore.put("wd_name", name);
        //TODO 人数
        //录入时间
        if (isB2b)
            formStore.put("wd_recorddate", TimeUtils.f_long_2_str(System.currentTimeMillis()));
        else if (!isUpdate)
            formStore.put("wd_recorddate", TimeUtils.f_long_2_str(System.currentTimeMillis()));

        //考勤时间
        FlightsTimeModel timeModel = model.getTimeModel();
        if (timeModel != null) {
            formStore.put("wd_ondutyone", timeModel.getWd_ondutyone());
            formStore.put("wd_offdutyone", timeModel.getWd_offdutyone());
            formStore.put("wd_ondutytwo", timeModel.getWd_ondutytwo());
            formStore.put("wd_offdutytwo", timeModel.getWd_offdutytwo());
            formStore.put("wd_ondutythree", timeModel.getWd_ondutythree());
            formStore.put("wd_offdutythree", timeModel.getWd_offdutythree());
            formStore.put("wd_earlytime", timeModel.getEarlyTime());
            int degree = 0;
            if (!StringUtil.isEmpty(timeModel.getWd_ondutyone()))
                degree++;
            if (!StringUtil.isEmpty(timeModel.getWd_ondutytwo()))
                degree++;
            if (!StringUtil.isEmpty(timeModel.getWd_ondutythree()))
                degree++;
            formStore.put("wd_degree", degree);
        } else {
            iAddFlihtsView.showToast(R.string.not_null_work_time, R.color.load_error);
            return;
        }
        if (!isUpdate) {
            if (!StringUtil.isEmpty(model.getDay())) {
                formStore.put("wd_day", model.getDay());
            } else {
                iAddFlihtsView.showToast(R.string.not_null_work_day, R.color.load_error);
                return;
            }
        } else {
            formStore.put("wd_day", model.getDay());
        }

        //start 人员数据
        EmployeesModel employeesModel = model.getEmployeesModel();
        String employeesName;
        String employeesCode;
        int employNumber = 0;
        if (employeesModel != null) {
            if (StringUtil.isEmpty(employeesModel.getEmployeecode()))
                employeesCode = "";
            else {
                employeesCode = employeesModel.getEmployeecode();
                employNumber = employeesModel.getEmployeecode().split(",").length;
            }
            employeesName = StringUtil.isEmpty(employeesModel.getEmployeeNames()) ? "" : employeesModel.getEmployeeNames();
        } else {
            employeesName = "";
            employeesCode = "";
        }
        formStore.put("wd_pcount", conuInDefaultor + employNumber);
        formStore.put("wd_emcode", getNameOrCodeAdd(employeesCode));
        formStore.put("wd_man", getNameOrCodeAdd(employeesName));
        //end 人员数据

        //处理部门数据
        EmployeesModel hrorgsModel = model.getHrorgsModel();
        String hrorgsName;
        String hrorgsCode;
        if (hrorgsModel != null) {
            hrorgsCode = StringUtil.isEmpty(hrorgsModel.getEmployeecode()) ? "" : hrorgsModel.getEmployeecode();
            hrorgsName = StringUtil.isEmpty(hrorgsModel.getEmployeeNames()) ? "" : hrorgsModel.getEmployeeNames();
        } else {
            hrorgsName = "";
            hrorgsCode = "";
        }
        if (isB2b) {
            formStore.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            formStore.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        }
        formStore.put("wd_defaultorcode", getNameOrCodeAdd(hrorgsCode));
        formStore.put("wd_defaultor", getNameOrCodeAdd(hrorgsName));
        //end 部门
        param.put("formStore", JSONUtil.map2JSON(formStore));
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        iAddFlihtsView.showLoading();
        String action = isUpdate ? "mobile/updateWorkDate.action" : "mobile/saveWorkDate.action";

        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveWorkData : action;
        Request request = new Request.Bulider()
                .setWhat(SUBMIT)
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .setUrl(url)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }


    public void loadManAndDefaultor(String manCode, String defaultorCode) {
        Bundle bundle = null;
        String url = "mobile/getManAndDefaultor.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        formStore.put("wd_emcode", StringUtil.isEmpty(manCode) ? "" : manCode.replaceAll("\'", ""));
        formStore.put("wd_defaultorcode", StringUtil.isEmpty(defaultorCode) ? "" : defaultorCode.replaceAll("\'", ""));
        param.put("formStore", JSONUtil.map2JSON(formStore));
        Request request = new Request.Bulider()
                .setWhat(MAN_DEFAULTOR)
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .setUrl(url)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    public void saveTime(Intent data) {
        FlightsTimeModel timeModel = data.getParcelableExtra("data");
        model.setTimeModel(timeModel);
        String time = model.getTime();
        iAddFlihtsView.updateTime(time);
    }

    public void saveDate(Intent data) {
        String days = data.getStringExtra("data");
        model.setDay(days);
        iAddFlihtsView.updateDate(model.getWeek(), isUpdate);
    }

    private String hrorgsCode = "";
    private String employeesCode = "";

    public void saveHrorgs(Intent data) {
        List<HrorgsModel> hrorgsList = data.getParcelableArrayListExtra("data");
        conuInDefaultor = data.getIntExtra("number", 0);
        String hrorgsName = getHrorgs(hrorgsList, true);
        hrorgsCode = getHrorgs(hrorgsList, false);
        EmployeesModel hrorgs = new EmployeesModel();
        hrorgs.setEmployeecode(hrorgsCode);
        hrorgs.setEmployeeNames(hrorgsName);
        model.setHrorgsModel(hrorgs);
        iAddFlihtsView.updateDepartment(hrorgsName);
        loadManAndDefaultor(employeesCode, hrorgsCode);
    }

    public void saveEmployees(Intent data) {
        List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
        String employeesName = getMumber(employeesList, true);
        employeesCode = getMumber(employeesList, false);
        EmployeesModel employees = new EmployeesModel();
        employees.setEmployeeNames(employeesName);
        employees.setEmployeecode(employeesCode);
        model.setEmployeesModel(employees);
        iAddFlihtsView.updateMunber(employeesName);
        loadManAndDefaultor(employeesCode, hrorgsCode);
    }


    /**
     * 选择冲突人员
     *
     * @param data
     */
    public void saveCollisionEmployees(Intent data) {
        List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
        handlerCollision(true, employeesList);
    }

    /**
     * 选择冲突部门
     *
     * @param data
     */
    public void saveCollisionDepartment(Intent data) {
        List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
        handlerCollision(false, employeesList);
    }

    /**
     * 处理去除冲突人员后数据
     *
     * @param isEmployees
     * @param notSelect
     */
    private void handlerCollision(boolean isEmployees, List<SelectEmUser> notSelect) {
        if (ListUtils.isEmpty(notSelect)) return;
        EmployeesModel bean = isEmployees ? model.getEmployeesModel() : model.getHrorgsModel();
        StringBuilder noSelectEmCodeBuilder = new StringBuilder();
        StringBuilder noSelectEmNameBuilder = new StringBuilder();
        for (SelectEmUser e : notSelect) {
            noSelectEmCodeBuilder.append(e.getEmCode() + ",");
            noSelectEmNameBuilder.append(e.getEmName() + ",");
        }
        String noSelectEmCode = noSelectEmCodeBuilder.toString();
        String noSelectEmName = noSelectEmNameBuilder.toString();
        if (bean != null) {
            String names = bean.getEmployeeNames();
            if (!StringUtil.isEmpty(names)) {
                String emName = getNameOrCodeByCollision(names, noSelectEmName);
                bean.setEmployeeNames(emName);
                if (isEmployees)
                    iAddFlihtsView.updateMunber(emName);
                else
                    iAddFlihtsView.updateDepartment(emName);
            }
            String codes = bean.getEmployeecode();
            if (!StringUtil.isEmpty(codes)) {
                String emCode = getNameOrCodeByCollision(codes, noSelectEmCode);
                if (isEmployees)
                    employeesCode = emCode;
                else
                    hrorgsCode = emCode;
                bean.setEmployeecode(emCode);
            }
            if (isEmployees)
                model.setEmployeesModel(bean);
            else
                model.setHrorgsModel(bean);
        }
        loadManAndDefaultor(employeesCode, hrorgsCode);
    }


    private StringBuilder removieLast(StringBuilder b) {
        try {
            if (b.length() > 1)
                b.deleteCharAt(b.length() - 1);
        } catch (StringIndexOutOfBoundsException e) {
            Log.i("gongpengming", "removieLast" + e.getMessage());
        }
        return b;
    }


    private String getNameOrCodeByCollision(String names, String text) {
        if (!StringUtil.isEmpty(names)) {
            StringBuilder namBbuilder = new StringBuilder();
            String[] aas = names.replace("\'", "").split(",");
            if (aas != null && aas.length > 0) {
                for (int i = 0; i < aas.length; i++) {
                    if (!StringUtil.isInclude(text, aas[i]))
                        namBbuilder.append(aas[i] + ",");
                }
            }
            return removieLast(namBbuilder).toString();
        }
        return null;
    }

    private String getMumber(List<SelectEmUser> employeesList, boolean isName) {
        if (!ListUtils.isEmpty(employeesList)) {
            StringBuilder employee = new StringBuilder();
            for (int i = 0; i < employeesList.size(); i++) {
                employee.append("\'" + (isName ? employeesList.get(i).getEmName()
                        : employeesList.get(i).getEmCode()) + "\'" + ",");
            }
            removieLast(employee);
            return employee.toString();
        }
        return "";
    }

    /**
     * 通过HrorgsModel列表获取对象
     *
     * @param hrorgsList
     * @param isName     是否是获取名字  否则获取编号
     * @return
     */
    private String getHrorgs(List<HrorgsModel> hrorgsList, boolean isName) {
        if (!ListUtils.isEmpty(hrorgsList)) {
            StringBuilder hrorgs = new StringBuilder();
            for (int i = 0; i < hrorgsList.size(); i++) {
                hrorgs.append("\'" + (isName ? hrorgsList.get(i).getName() : hrorgsList.get(i).getCode()) + "\'" + ",");
            }
            removieLast(hrorgs);
            return hrorgs.toString();
        }
        return "";
    }


    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (!isJSON) {
            //TODO 返回错误
            return;
        }
        JSONObject object = JSON.parseObject(message);
        switch (what) {
            case SUBMIT:
                iAddFlihtsView.dimssLoading();
                if (model == null)
                    model = new FlightsModel();
                String name = null;
                if (bundle != null) {
                    name = bundle.getString("name");
                }
                model.setName(StringUtil.isEmpty(name) ? "" : name);
                model.setType(1);
                int id = JSONUtil.getInt(object, "id");
                if (id != 0)
                    model.setId(id);
                iAddFlihtsView.endActivity(model, isUpdate);
                break;
            case MAN_DEFAULTOR://冲突部门和人员
                handlerManAndDefaultir(object);
                break;

        }
    }


    @Override
    public void error(int what, String message, Bundle bundle) {
        iAddFlihtsView.dimssLoading();
        if (!StringUtil.isEmpty(message))
            iAddFlihtsView.showToast(message, R.color.load_error);
    }

    private void handlerManAndDefaultir(JSONObject object) {
        if (!object.containsKey("listdata")) return;
        JSONArray array = object.getJSONArray("listdata");
        if (!ListUtils.isEmpty(array)) {
            JSONObject man = array.getJSONObject(0);
            if (man.containsKey("man")) {
                handlerMan(man.getJSONArray("man"));
            } else if (man.containsKey("defaultor")) {
                handlerDefaultor(man.getJSONArray("defaultor"));
            }
        }

        if (array.size() > 1) {
            JSONObject defaultor = array.getJSONObject(1);
            if (defaultor.containsKey("defaultor")) {
                handlerDefaultor(defaultor.getJSONArray("defaultor"));
            } else if (defaultor.containsKey("man")) {
                handlerMan(defaultor.getJSONArray("man"));
            }
        }
    }

    private void handlerMan(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            iAddFlihtsView.showCollisionMan(null);
            return;
        }
        SelectEmUser user = null;
        JSONObject o = null;
        ArrayList<SelectEmUser> chche = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            o = array.getJSONObject(i);
            if (o.containsKey("workdatename") && !StringUtil.isEmpty(o.getString("workdatename"))
                    && o.containsKey("em_name") && !StringUtil.isEmpty(o.getString("em_name"))) {
                String workdatecode = o.getString("workdatecode");
                if (isUpdate && model != null && !StringUtil.isEmpty(model.getCode()) && !StringUtil.isEmpty(workdatecode) && model.getCode().equals(workdatecode)) {
                    continue;
                }
                user = new SelectEmUser();
                user.setClick(true);
                user.setDepart(o.getString("em_defaultorname"));
                user.setPosition(o.getString("em_position"));
                user.setEmName(o.getString("em_name"));
                user.setEmCode(o.getString("ew_emcode"));
                user.setTag(o.getString("workdatename"));
                user.setImId(0);
                chche.add(user);
            }
        }

        iAddFlihtsView.showCollisionMan(chche);
    }

    private void handlerDefaultor(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            iAddFlihtsView.showCollisionDefaultir(null);
            return;
        }
        SelectEmUser user = null;
        JSONObject o = null;
        ArrayList<SelectEmUser> chche = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            o = array.getJSONObject(i);
            String workdatecode =JSONUtil.getText(o, "workdatescode");
            if (!StringUtil.isEmpty(workdatecode) && !StringUtil.isEmpty(JSONUtil.getText(o, "workdatesname")) && !StringUtil.isEmpty(JSONUtil.getText(o, "conflictem_defaultorcode"))) {
                if (isUpdate && model != null && !StringUtil.isEmpty(model.getCode()) && model.getCode().equals(workdatecode)) {
                    continue;
                }
                user = new SelectEmUser();
                user.setClick(true);
                user.setNumber(o.getInteger("defaultormancount"));
                user.setEmName(o.getString("conflictem_defaultorname"));
                user.setEmCode(o.getString("conflictem_defaultorcode"));
                user.setTag(o.getString("workdatesname"));
                user.setImId(0);
                chche.add(user);

            }
        }
        iAddFlihtsView.showCollisionDefaultir(chche);
    }

    private String getNameOrCodeAdd(String name) {
        try {
            if (StringUtil.isEmpty(name)) return "";
            name = name.replaceAll("\'", "");
            String[] names = name.split(",");
            StringBuilder builder = new StringBuilder();
            for (String e : names) {
                builder.append("\'" + e + "\'" + ",");
            }
            StringUtil.removieLast(builder);
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void putData2Intent(int id, Intent intent) {
        if (id == R.id.date_tv){
            String day = model.getDay();
            Log.i("gongpengming", "day=" + day);
            intent.putExtra("day", day);
        }else if (id == R.id.time_tv){
            FlightsTimeModel timeModel = model.getTimeModel();
            intent.putExtra("model", timeModel);
        }else if (id == R.id.munber_tv){
            EmployeesModel employeesModel = model.getEmployeesModel();
            intent.putExtra(OAConfig.STRING_DATA, employeesModel.getEmployeeNames());
        }
    }

    public String getEmployeeEmCode() {
        if (model == null) return "";
        if (model.getEmployeesModel() == null) return "";
        return model.getEmployeesModel().getEmployeecode();
    }

    public String getHrorgsEmCode() {
        if (model == null) return "";
        if (model.getHrorgsModel() == null) return "";
        return model.getHrorgsModel().getEmployeeNames();
    }
}
