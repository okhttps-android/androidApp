package com.modular.apputils.utils;

import android.text.TextUtils;

import com.modular.apputils.model.BillGroupModel;


public class BillTypeChangeUtils {


    /**
     * 获取item的显示类型
     *
     * @param dfType
     * @return -1,异常显示
     * 0.标题
     * 1.选择
     * 2.输入
     */
    public static int getItemViewType(String dfType) {
        if (TextUtils.isEmpty(dfType)) return -1;
        switch (dfType.toUpperCase()) {
            case BillGroupModel.Constants.TYPE_TITLE:
                return 0;
            case BillGroupModel.Constants.TYPE_ADD:
                return 110;
            case BillGroupModel.Constants.TYPE_TAB:
                return 111;
            case "C":
            case "SF":
            case "DF":
            case "S":
            case "SS":
                return 1;
            case "FF":
                return 2;
            default:
                return 1;
        }
    }


    public static boolean isSelect(String dfType) {
        if (TextUtils.isEmpty(dfType)) return false;
        switch (dfType.toUpperCase()) {
            case "D":
            case "DT":
            case "C":
            case "MF":
            case "SF":
            case "DF":
                return true;
        }
        return false;
    }


    /**
     * 将uas的类型转为adapter type可以使用的int
     *
     * @param dfType
     * @return
     */
    public static int getType(String dfType) {
        if (TextUtils.isEmpty(dfType)) return -1;
        switch (dfType.toUpperCase()) {
            case "SF":
                return 1;
            case "DF":
                return 2;
            default:
                return getTypeVague(dfType);
        }
    }

    /**
     * 将uas的类型转为adapter type可以使用的int,使用模糊匹配
     *
     * @param dfType
     * @return
     */
    public static int getTypeVague(String dfType) {
        if (TextUtils.isEmpty(dfType)) return -1;
        String dfTypeUpperCase = dfType.toUpperCase();
        if (isContains(dfTypeUpperCase, "")) {
            return 1000;
        } else {
            return -1;
        }
    }

    public static boolean isContains(String key, String... str) {
        if (str != null && str.length > 0) {
            for (String s : str) {
                if (key.contains(s))
                    return true;
            }
        }
        return false;
    }

    public static String getDbType(int type) {
        switch (type) {
            case 1:
                return "SF";
            case 2:
                return "DF";
            default:
                return "";
        }
    }


}
