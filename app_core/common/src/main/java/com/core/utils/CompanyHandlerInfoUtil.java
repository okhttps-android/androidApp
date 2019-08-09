package com.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.thread.ThreadUtil;
import com.core.app.MyApplication;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.HrorgsEntity;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 针对企业通讯录的接口的数据处理逻辑和保存工具
 * Created by Bitliker on 2017/2/28.
 */

public class CompanyHandlerInfoUtil {
    public static List<EmployeesEntity> getEmployees(JSONObject object) throws Exception {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) return getEmployeesByB2b(object);
        return getEmployeesByNet(object);
    }

    public static List<EmployeesEntity> getEmployeesByB2b(JSONObject object) throws Exception {
        if (object == null) return null;
        JSONArray array = null;
        if (object.containsKey("data") && object.get("data") instanceof JSONArray)
            array = object.getJSONArray("data");
        if (ListUtils.isEmpty(array)) return null;
        EmployeesEntity entity = null;
        JSONObject o = null;
        String master = CommonUtil.getMaster();
        final List<EmployeesEntity> emList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            o = array.getJSONObject(i);
            entity = new EmployeesEntity();
            entity.setEm_IMID(JSONUtil.getInt(o, "emimid"));
            entity.setEM_POSITION(JSONUtil.getText(o, "emrole"));
            entity.setEM_DEPART(JSONUtil.getText(o, "emdepart"));
            entity.setEM_EMAIL(JSONUtil.getText(o, "ememail"));
            entity.setEM_NAME(JSONUtil.getText(o, "emname"));
            entity.setEM_MOBILE(JSONUtil.getText(o, "emphone"));
            entity.setEM_CODE(String.valueOf(JSONUtil.getInt(o, "emcode")));
            entity.setWHICHSYS(master);
            emList.add(entity);
        }
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                DBManager manager = new DBManager();
                manager.saveEmployees(emList);
                manager.closeDB();
//                DatabaseManager.getInstance().closeDatabase();
            }
        });
        return emList;
    }

    private static void save2DbAsyn(final JSONObject jsonobject, final List<HrorgsEntity> hrorgsEntities, final List<EmployeesEntity> employeesEntities) {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    writeData(jsonobject, hrorgsEntities, employeesEntities);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static List<HrorgsEntity> getHrorgsByNet(JSONObject jsonobject) throws Exception {
        List<HrorgsEntity> hrorgsEntities = JSON.parseArray(jsonobject.getString("hrorgs"), HrorgsEntity.class);
        List<EmployeesEntity> employeesEntities = JSON.parseArray(jsonobject.getString("employees"), EmployeesEntity.class);
        save2DbAsyn(jsonobject, hrorgsEntities, employeesEntities);
        return hrorgsEntities;
    }

    public static List<EmployeesEntity> getEmployeesByNet(JSONObject jsonobject) throws Exception {
        List<HrorgsEntity> hrorgsEntities = JSON.parseArray(jsonobject.getString("hrorgs"), HrorgsEntity.class);
        List<EmployeesEntity> employeesEntities = JSON.parseArray(jsonobject.getString("employees"), EmployeesEntity.class);
        save2DbAsyn(jsonobject, hrorgsEntities, employeesEntities);
        return employeesEntities;
    }

    private static void writeData(JSONObject jsonobject, List<HrorgsEntity> hrorgsEntities, List<EmployeesEntity> employeesEntities) throws Exception {
        String server_time = jsonobject.getString("sysdate");//服务器系统时间
        //实体类
        String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
        DBManager manager = new DBManager();
        HrorgsEntity hrEntity = manager.select_getRootData(new String[]{master}, "whichsys=?");
        boolean isFristLoad = true;//是否第一次加载
        if (hrEntity != null) {
            isFristLoad = false;
        }
        if (!hrorgsEntities.isEmpty() || !employeesEntities.isEmpty()) {
            insertDataSqlite(manager, isFristLoad, hrorgsEntities, employeesEntities, server_time);
        }
        manager.closeDB();
    }

    /**
     * @author Administrator
     * @功能:写入数据库 isFristLoad 标志是否是第一次加载(数据库是否有缓存);
     * 因为第一次，flag字段不管是更新或者是插入，都执行插入操作;
     */
    private static void insertDataSqlite(DBManager manager, boolean isFristLoad, List<HrorgsEntity> hrorgsEntities, List<EmployeesEntity> employeesEntities, String servertime) {
        if (isFristLoad) {
            manager.saveHrogrs(hrorgsEntities);
            manager.saveEmployees(employeesEntities);
        } else {
            synSqliteDataforServer(manager, hrorgsEntities, employeesEntities);
        }
        manager.deleteHrogrsAndEmployees();
        Map<String, Object> dateCaches = new HashMap<String, Object>();
        String time = dateMinute(servertime);
        dateCaches.put("ed_lastdate", time);
        dateCaches.put("ed_kind", "通讯录");
        dateCaches.put("ed_company", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_commpany"));
        dateCaches.put("ed_whichsys", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        String db_time = manager.select_getCacheTime(new String[]{}, "ed_company=? and ed_whichsys=?");
        if (db_time != null) {
            manager.updateCacheTime(dateCaches);
        } else {
            manager.saveCacheTime(dateCaches);
        }
    }

    /**
     * @author Administrator
     * @功能:同步数据
     */
    private static void synSqliteDataforServer(DBManager manager, List<HrorgsEntity> hrorgsEntities, List<EmployeesEntity> employeesEntities) {
        if (!hrorgsEntities.isEmpty()) {
            List<HrorgsEntity> insertHrorgsList = new ArrayList<HrorgsEntity>();
            List<HrorgsEntity> updateHrorgsList = new ArrayList<HrorgsEntity>();
            for (int i = 0; i < hrorgsEntities.size(); i++) {
                HrorgsEntity entity = hrorgsEntities.get(i);
                String or_code = entity.getOr_code();
                String whichsys = entity.getWhichsys();
                if ("UPDATE".equals(entity.getFlag())) {
                    HrorgsEntity hentity = manager.select_getRootData(new String[]{or_code, whichsys}, "or_code=? and whichsys=?");
                    if (hentity != null) {
                        updateHrorgsList.add(entity);
                    } else {
                        insertHrorgsList.add(entity);
                    }
                } else if ("INSERT".equals(entity.getFlag())) {
                    insertHrorgsList.add(entity);
                }
            }
            manager.saveHrogrs(insertHrorgsList);
            manager.updateHrogrs(updateHrorgsList);
        }
        if (!employeesEntities.isEmpty()) {
            List<EmployeesEntity> insertEmployeesList = new ArrayList<EmployeesEntity>();
            List<EmployeesEntity> updateEmployeesList = new ArrayList<EmployeesEntity>();
            for (int i = 0; i < employeesEntities.size(); i++) {
                EmployeesEntity eEntity = employeesEntities.get(i);
                String em_code = eEntity.getEM_CODE();
                String whichsys = eEntity.getWHICHSYS();
                if (StringUtil.isEmpty(em_code)) return;
                if ("UPDATE".equals(eEntity.getFLAG())) {
                    List<EmployeesEntity> tempEntity = manager.select_getEmployee(new String[]{em_code, whichsys}, "em_code=? and whichsys=?");
                    if (tempEntity.isEmpty()) {
                        insertEmployeesList.add(eEntity);
                    } else {
                        updateEmployeesList.add(eEntity);
                    }
                } else if ("INSERT".equals(eEntity.getFLAG())) {
                    insertEmployeesList.add(eEntity);
                }
            }
            manager.saveEmployees(insertEmployeesList);
            manager.updateEmployees(updateEmployeesList);
        }
    }

    /**
     * @author Administrator
     * @功能:时间减法
     */
    private static String dateMinute(String datetime) {
        String str = datetime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

}
