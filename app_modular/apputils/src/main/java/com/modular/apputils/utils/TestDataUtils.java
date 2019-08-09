package com.modular.apputils.utils;

import com.core.app.MyApplication;
import com.core.utils.CommonUtil;

//读取测试数据工具类
public class TestDataUtils {

    public static String getTestData() {
        return CommonUtil.getAssetsJson(MyApplication.getInstance(), "oa_test_data.json");
    }

    public final static String getBillLocalData(){
        return "{\n" +
                "\t\"sessionId\": \"0AB31468C9BE7501DBFD6F2CCB0F28A4\",\n" +
                "\t\"data\": {\n" +
                "\t\t\"formdetail\": [{\n" +
                "\t\t\t\"fd_caption\": \"交易币别\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": \"sa_rate\",\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_currency\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 7,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401870\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"汇率\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 22,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_rate\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 8,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401897\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"收款条件\",\n" +
                "\t\t\t\"fd_dbfind\": \"PT\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": \"sa_payments\",\n" +
                "\t\t\t\"fd_type\": \"PT\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_paymentscode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 10,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401752\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"收款条件\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"PT\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_payments\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 11,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401753\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"应收客户\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": \"sa_apcustname\",\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_apcustcode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 12,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401867\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"应收客户名称\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_apcustname\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 13,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401766\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"收货客户\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": \"sa_shcustname\",\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_shcustcode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 14,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401754\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"收货客户名称\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_shcustname\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 15,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401764\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"收货地址\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 500,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_toplace\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 16,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401765\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"运输方式\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"供应商送货\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"供应商送货\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 1\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"海运\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"海运\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 2\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"陆运\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"陆运\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 3\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"客户自提\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"客户自提\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 4\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"快递\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"快递\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 5\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"空运\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"空运\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 6\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"fd_type\": \"C\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_transport\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 18,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401890\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"出货方式\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"整机\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"整机\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 1\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"CKD\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"CKD\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 2\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"整机+SKD\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"整机+SKD\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 3\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"SKD+CKD\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"SKD+CKD\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 4\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"SKD\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"SKD\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 5\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"fd_type\": \"C\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_chfs\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"商务信息\",\n" +
                "\t\t\t\"fd_detno\": 24,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401768\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"销售订单号\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_code\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 1,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401887\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"日期\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"D\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_date\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"2018-09-10\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 2,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401749\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"订单类型\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_kind\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 3,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401750\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"单据状态\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 30,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": \"Highlight:{blue=在录入,red=已审核}\",\n" +
                "\t\t\t\"fd_field\": \"sa_status\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"在录入\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 4,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401892\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"客户\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": \"sa_custname\",\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": \"link:jsps/scm/sale/customerBase.jsp?formCondition=cu_codeIS{sa_custcode}\",\n" +
                "\t\t\t\"fd_field\": \"sa_custcode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 5,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401889\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"客户名称\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 200,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_custname\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 6,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401866\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"部门编号\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": \"sa_departmentname\",\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_departmentcode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 20,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401769\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"部门名称\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_departmentname\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 21,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401755\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"业务员号\",\n" +
                "\t\t\t\"fd_dbfind\": \"T\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": \"sa_seller\",\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_sellercode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 22,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401885\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"业务员\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SF\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_seller\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 23,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401751\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"客户PO号\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_pocode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 23.5,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401888\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"备注\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 1000,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"MS\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_remark\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 40,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401895\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"客户ID\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_custid\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 51,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401759\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"单据状态编码\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 30,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_statuscode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"ENTERING\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 52,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401760\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"ID\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_id\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 53,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401761\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"来源ID\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_sourceid\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 54,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401762\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"录入人ID\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_recorderid\",\n" +
                "\t\t\t\"fd_defaultvalue\": 1025328,\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 55,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401893\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"收款方式\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_paymentsid\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 56,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401869\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"抛转状态\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_sync\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 57,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401861\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"关联商机ID\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_bcid\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 59,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401868\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"复审状态\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_recheckstatus\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 230,\n" +
                "\t\t\t\"fd_appwidth\": 0,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 418999\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"复审状态码\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_recheckstatuscode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"基本信息\",\n" +
                "\t\t\t\"fd_detno\": 231,\n" +
                "\t\t\t\"fd_appwidth\": 0,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 419000\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"下单类型_标识B2C订单\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 20,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"商城订单\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"B2C\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 0\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"fd_type\": \"C\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_ordertype\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"提示信息\",\n" +
                "\t\t\t\"fd_detno\": 301,\n" +
                "\t\t\t\"fd_appwidth\": 0,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 420983\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"运费\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 22,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"N\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": \"floatNumber:8\",\n" +
                "\t\t\t\"fd_field\": \"sa_fare\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"提示信息\",\n" +
                "\t\t\t\"fd_detno\": 442,\n" +
                "\t\t\t\"fd_appwidth\": 0,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 420979\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"预收金额\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"N\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_prepayamount\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 25,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401860\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"来源单号\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_sourcecode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 26,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401862\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"来源类型\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_sourcetype\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 27,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401863\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"通知单状态\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_turnstatus\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 30,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401896\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"发货状态\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_sendstatus\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 31,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401758\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"打印状态\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 12,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_printstatus\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"未打印\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 32,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401891\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"打印次数\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"N\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": \"floatNumber:6\",\n" +
                "\t\t\t\"fd_field\": \"sa_count\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 33,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401864\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"录入人\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_recorder\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"龚鹏明\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 34,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401748\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"录入日期\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"D\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_recorddate\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"2018-09-10 09:33:38\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 35,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401757\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"最后更新人\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_updateman\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 36,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401859\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"最后更新日\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"D\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_updatedate\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 37,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401894\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"审核人\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": \"link:jsps/scm/sale/customerBase.jsp?formCondition=cu_codeIS{sa_custcode}\",\n" +
                "\t\t\t\"fd_field\": \"sa_auditman\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 38,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401770\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"审核日期\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"D\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_auditdate\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 39,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": -1,\n" +
                "\t\t\t\"fd_id\": 401858\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"自动取单价\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 4,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"H\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"F\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_getprice\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"0\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 50,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401756\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"是否负利润\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 10,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_minus\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 124,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401899\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"商城采购单号\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 50,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"fd_type\": \"SS\",\n" +
                "\t\t\t\"fd_readonly\": \"T\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_b2cpucode\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 125,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 401898\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"fd_caption\": \"类型\",\n" +
                "\t\t\t\"fd_dbfind\": \"F\",\n" +
                "\t\t\t\"fd_maxlength\": 100,\n" +
                "\t\t\t\"fd_logictype\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"试产\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"试产\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 0\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_VALUE\": \"量产\",\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"量产\",\n" +
                "\t\t\t\t\"DLC_DETNO\": 1\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"fd_type\": \"C\",\n" +
                "\t\t\t\"fd_readonly\": \"F\",\n" +
                "\t\t\t\"fd_allowblank\": \"T\",\n" +
                "\t\t\t\"fd_render\": null,\n" +
                "\t\t\t\"fd_field\": \"sa_type\",\n" +
                "\t\t\t\"fd_defaultvalue\": \"\",\n" +
                "\t\t\t\"fd_group\": \"管理信息\",\n" +
                "\t\t\t\"fd_detno\": 150,\n" +
                "\t\t\t\"fd_appwidth\": 2,\n" +
                "\t\t\t\"mfd_isdefault\": 0,\n" +
                "\t\t\t\"fd_id\": 408584\n" +
                "\t\t}],\n" +
                "\t\t\"gridetail\": [{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 60,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250424,\n" +
                "\t\t\t\"dg_caption\": \"序号\",\n" +
                "\t\t\t\"dg_field\": \"sd_detno\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 1,\n" +
                "\t\t\t\"dg_logictype\": \"detno\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"link:jsps/scm/product/product.jsp?formConditionISpr_code={sd_prodcode}\",\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 140,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250420,\n" +
                "\t\t\t\"dg_caption\": \"物料编号\",\n" +
                "\t\t\t\"dg_field\": \"sd_prodcode\",\n" +
                "\t\t\t\"dg_maxlength\": 100,\n" +
                "\t\t\t\"dg_type\": \"DF\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": \"Product|pr_code\",\n" +
                "\t\t\t\"dg_sequence\": 2,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250518,\n" +
                "\t\t\t\"dg_caption\": \"平台批号\",\n" +
                "\t\t\t\"dg_field\": \"sd_b2cbarcode\",\n" +
                "\t\t\t\"dg_maxlength\": 50,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 9.2,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"scm_sale_sdqty\",\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250430,\n" +
                "\t\t\t\"dg_caption\": \"数量\",\n" +
                "\t\t\t\"dg_field\": \"sd_qty\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 12,\n" +
                "\t\t\t\"dg_logictype\": \"necessaryField\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"rowClass\",\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250482,\n" +
                "\t\t\t\"dg_caption\": \"定价\",\n" +
                "\t\t\t\"dg_field\": \"sd_purcprice\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 13,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 120,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 259586,\n" +
                "\t\t\t\"dg_caption\": \"上次销售单价\",\n" +
                "\t\t\t\"dg_field\": \"sd_lastprice\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 14,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 90,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250414,\n" +
                "\t\t\t\"dg_caption\": \"单价\",\n" +
                "\t\t\t\"dg_field\": \"sd_price\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 14,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"sd_price\",\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 265746,\n" +
                "\t\t\t\"dg_caption\": \"原单价\",\n" +
                "\t\t\t\"dg_field\": \"sd_cbm\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 0,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 14.1,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250508,\n" +
                "\t\t\t\"dg_caption\": \"折扣率\",\n" +
                "\t\t\t\"dg_field\": \"sd_discount\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 15,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 90,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250416,\n" +
                "\t\t\t\"dg_caption\": \"总额\",\n" +
                "\t\t\t\"dg_field\": \"sd_total\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"nfloatcolumn6\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 15,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 273206,\n" +
                "\t\t\t\"dg_caption\": \"新金额\",\n" +
                "\t\t\t\"dg_field\": \"sd_total_user\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"nfloatcolumn6\",\n" +
                "\t\t\t\"dg_appwidth\": 0,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 15.1,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250474,\n" +
                "\t\t\t\"dg_caption\": \"税率%\",\n" +
                "\t\t\t\"dg_field\": \"sd_taxrate\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 16,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 90,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250438,\n" +
                "\t\t\t\"dg_caption\": \"不含税单价\",\n" +
                "\t\t\t\"dg_field\": \"sd_costprice\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 17,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 90,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250434,\n" +
                "\t\t\t\"dg_caption\": \"不含税金额\",\n" +
                "\t\t\t\"dg_field\": \"sd_taxtotal\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 18,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250506,\n" +
                "\t\t\t\"dg_caption\": \"成本单价\",\n" +
                "\t\t\t\"dg_field\": \"sd_costingprice\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 19,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"defaultValue:-1\",\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"-1\",\n" +
                "\t\t\t\t\"DLC_DETNO\": \"0\",\n" +
                "\t\t\t\t\"DLC_VALUE\": \"是\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"0\",\n" +
                "\t\t\t\t\"DLC_DETNO\": \"1\",\n" +
                "\t\t\t\t\"DLC_VALUE\": \"否\"\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250484,\n" +
                "\t\t\t\"dg_caption\": \"是否特价\",\n" +
                "\t\t\t\"dg_field\": \"sd_isspecial\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"C\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 20,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"rowClass\",\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"-1\",\n" +
                "\t\t\t\t\"DLC_DETNO\": \"0\",\n" +
                "\t\t\t\t\"DLC_VALUE\": \"是\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"0\",\n" +
                "\t\t\t\t\"DLC_DETNO\": \"1\",\n" +
                "\t\t\t\t\"DLC_VALUE\": \"否\"\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250452,\n" +
                "\t\t\t\"dg_caption\": \"是否保税\",\n" +
                "\t\t\t\"dg_field\": \"sd_bonded\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"C\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 21,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 110,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250410,\n" +
                "\t\t\t\"dg_caption\": \"交货日期\",\n" +
                "\t\t\t\"dg_field\": \"sd_delivery\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"D\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 22,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 110,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250526,\n" +
                "\t\t\t\"dg_caption\": \"PMC回复日期\",\n" +
                "\t\t\t\"dg_field\": \"sd_pmcdate\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"D\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 23,\n" +
                "\t\t\t\"dg_logictype\": \"unauto\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 120,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250460,\n" +
                "\t\t\t\"dg_caption\": \"PMC备注\",\n" +
                "\t\t\t\"dg_field\": \"sd_pmcremark\",\n" +
                "\t\t\t\"dg_maxlength\": 100,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 24,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": \"link:jsps/scm/product/productBase.jsp?formConditionISpr_code={sd_custprodcode}\",\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250454,\n" +
                "\t\t\t\"dg_caption\": \"客户物料编号\",\n" +
                "\t\t\t\"dg_field\": \"sd_custprodcode\",\n" +
                "\t\t\t\"dg_maxlength\": 30,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": \"ProductCustomer|pc_custprodcode\",\n" +
                "\t\t\t\"dg_sequence\": 26,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250520,\n" +
                "\t\t\t\"dg_caption\": \"客户物料名称\",\n" +
                "\t\t\t\"dg_field\": \"sd_custproddetail\",\n" +
                "\t\t\t\"dg_maxlength\": 100,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 26.1,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250486,\n" +
                "\t\t\t\"dg_caption\": \"客户型号\",\n" +
                "\t\t\t\"dg_field\": \"sd_prodcustcode\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 27,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [{\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"-1\",\n" +
                "\t\t\t\t\"DLC_DETNO\": \"0\",\n" +
                "\t\t\t\t\"DLC_VALUE\": \"是\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"DLC_DISPLAY\": \"0\",\n" +
                "\t\t\t\t\"DLC_DETNO\": \"1\",\n" +
                "\t\t\t\t\"DLC_VALUE\": \"否\"\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250494,\n" +
                "\t\t\t\"dg_caption\": \"是否超预测数\",\n" +
                "\t\t\t\"dg_field\": \"sd_noforecast\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"C\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 29,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 120,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250476,\n" +
                "\t\t\t\"dg_caption\": \"预测单号\",\n" +
                "\t\t\t\"dg_field\": \"sd_forecastcode\",\n" +
                "\t\t\t\"dg_maxlength\": 100,\n" +
                "\t\t\t\"dg_type\": \"DF\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": \"SaleForecastDetail!BySale|sf_code\",\n" +
                "\t\t\t\"dg_sequence\": 30,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250478,\n" +
                "\t\t\t\"dg_caption\": \"预测序号\",\n" +
                "\t\t\t\"dg_field\": \"sd_forecastdetno\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 31,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250470,\n" +
                "\t\t\t\"dg_caption\": \"提前期\",\n" +
                "\t\t\t\"dg_field\": \"sd_leadtime\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 35,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250472,\n" +
                "\t\t\t\"dg_caption\": \"报关价\",\n" +
                "\t\t\t\"dg_field\": \"sd_bgprice\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 36,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 120,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250412,\n" +
                "\t\t\t\"dg_caption\": \"描述\",\n" +
                "\t\t\t\"dg_field\": \"sd_description\",\n" +
                "\t\t\t\"dg_maxlength\": 120,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 37,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 200,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250450,\n" +
                "\t\t\t\"dg_caption\": \"备注1\",\n" +
                "\t\t\t\"dg_field\": \"sd_remark\",\n" +
                "\t\t\t\"dg_maxlength\": 200,\n" +
                "\t\t\t\"dg_type\": \"MS\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 38,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 150,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250462,\n" +
                "\t\t\t\"dg_caption\": \"备注2\",\n" +
                "\t\t\t\"dg_field\": \"sd_remark2\",\n" +
                "\t\t\t\"dg_maxlength\": 200,\n" +
                "\t\t\t\"dg_type\": \"MS\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 39,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250458,\n" +
                "\t\t\t\"dg_caption\": \"BOM ID\",\n" +
                "\t\t\t\"dg_field\": \"sd_bomid\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 40,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250466,\n" +
                "\t\t\t\"dg_caption\": \"状态\",\n" +
                "\t\t\t\"dg_field\": \"sd_status\",\n" +
                "\t\t\t\"dg_maxlength\": 30,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 43,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 120,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250480,\n" +
                "\t\t\t\"dg_caption\": \"结案冻结原因\",\n" +
                "\t\t\t\"dg_field\": \"sd_barcode\",\n" +
                "\t\t\t\"dg_maxlength\": 20,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 44,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250496,\n" +
                "\t\t\t\"dg_caption\": \"已转制造数量\",\n" +
                "\t\t\t\"dg_field\": \"sd_tomakeqty\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 45,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 110,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250406,\n" +
                "\t\t\t\"dg_caption\": \"组装开工日期\",\n" +
                "\t\t\t\"dg_field\": \"sd_packagedate\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"D\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 46,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250502,\n" +
                "\t\t\t\"dg_caption\": \"供应商比例(%)\",\n" +
                "\t\t\t\"dg_field\": \"sd_vendorrate\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 47,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250428,\n" +
                "\t\t\t\"dg_caption\": \"ID\",\n" +
                "\t\t\t\"dg_field\": \"sd_id\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 48,\n" +
                "\t\t\t\"dg_logictype\": \"keyField\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250426,\n" +
                "\t\t\t\"dg_caption\": \"SAID\",\n" +
                "\t\t\t\"dg_field\": \"sd_said\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 49,\n" +
                "\t\t\t\"dg_logictype\": \"mainField\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250422,\n" +
                "\t\t\t\"dg_caption\": \"物料id\",\n" +
                "\t\t\t\"dg_field\": \"sd_prodid\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 50,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250500,\n" +
                "\t\t\t\"dg_caption\": \"商机id\",\n" +
                "\t\t\t\"dg_field\": \"sd_bcid\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 51,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250468,\n" +
                "\t\t\t\"dg_caption\": \"备品数量\",\n" +
                "\t\t\t\"dg_field\": \"sd_readyqty\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 52,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250464,\n" +
                "\t\t\t\"dg_caption\": \"来源明细ID\",\n" +
                "\t\t\t\"dg_field\": \"sd_sourceid\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 53,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250488,\n" +
                "\t\t\t\"dg_caption\": \"预测明细ID\",\n" +
                "\t\t\t\"dg_field\": \"sd_saleforecastdetailid\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 54,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250432,\n" +
                "\t\t\t\"dg_caption\": \"SACODE\",\n" +
                "\t\t\t\"dg_field\": \"sd_code\",\n" +
                "\t\t\t\"dg_maxlength\": 100,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 55,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250418,\n" +
                "\t\t\t\"dg_caption\": \"状态编码\",\n" +
                "\t\t\t\"dg_field\": \"sd_statuscode\",\n" +
                "\t\t\t\"dg_maxlength\": 30,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 56,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 0,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250504,\n" +
                "\t\t\t\"dg_caption\": \"发货日期\",\n" +
                "\t\t\t\"dg_field\": \"sd_senddate\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"D\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 57,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 100,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250510,\n" +
                "\t\t\t\"dg_caption\": \"毛利率%\",\n" +
                "\t\t\t\"dg_field\": \"sd_bodycost\",\n" +
                "\t\t\t\"dg_maxlength\": 12,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 67,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250512,\n" +
                "\t\t\t\"dg_caption\": \"是否负利润\",\n" +
                "\t\t\t\"dg_field\": \"sd_minus\",\n" +
                "\t\t\t\"dg_maxlength\": 10,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 143,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 250514,\n" +
                "\t\t\t\"dg_caption\": \"BOM成本价\",\n" +
                "\t\t\t\"dg_field\": \"sd_bomprice\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"N\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 221,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 110,\n" +
                "\t\t\t\"mdg_isdefault\": -1,\n" +
                "\t\t\t\"gd_id\": 250528,\n" +
                "\t\t\t\"dg_caption\": \"结案日期\",\n" +
                "\t\t\t\"dg_field\": \"sd_enddate\",\n" +
                "\t\t\t\"dg_maxlength\": 0,\n" +
                "\t\t\t\"dg_type\": \"D\",\n" +
                "\t\t\t\"dg_appwidth\": 2,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 301,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"dg_renderer\": null,\n" +
                "\t\t\t\"COMBOSTORE\": [],\n" +
                "\t\t\t\"dg_width\": 80,\n" +
                "\t\t\t\"mdg_isdefault\": 0,\n" +
                "\t\t\t\"gd_id\": 279686,\n" +
                "\t\t\t\"dg_caption\": \"所属工厂\",\n" +
                "\t\t\t\"dg_field\": \"sd_factory\",\n" +
                "\t\t\t\"dg_maxlength\": 50,\n" +
                "\t\t\t\"dg_type\": \"S\",\n" +
                "\t\t\t\"dg_appwidth\": 0,\n" +
                "\t\t\t\"dg_findfunctionname\": null,\n" +
                "\t\t\t\"dg_sequence\": 376,\n" +
                "\t\t\t\"dg_logictype\": null\n" +
                "\t\t}]\n" +
                "\t},\n" +
                "\t\"config\": {\n" +
                "\t\t\"fo_keyfield\": \"sa_id\",\n" +
                "\t\t\"fo_statusfield\": \"sa_status\",\n" +
                "\t\t\"fo_detailkeyfield\": \"sd_id\",\n" +
                "\t\t\"fo_statuscodefield\": \"sa_statuscode\",\n" +
                "\t\t\"fo_detailmainkeyfield\": \"sd_said\"\n" +
                "\t},\n" +
                "\t\"success\": true\n" +
                "}";
    }
}
