package com.core.model;

import android.support.annotation.IntDef;

import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.utils.TimeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitliker on 2017/7/21.
 */

public class Approval {
    public static final String VALUES_YES = "是";
    public static final String VALUES_NO = "否";
    public static final String VALUES_UNKNOWN = "未选择";

    public static final int
            TITLE = 11//标题
            , MAIN = 12  //主表
            , DETAIL = 13//从表
            , SETUPTASK = 14//历史审批要点
            , ENCLOSURE = 16//附件
            , POINTS = 17//要点
            , NODES_TAG = 18//审批节点标记
            , NODES = 19//审批节点
            , TAG = 20;//标题

    private boolean neerInput = false;//是否需要输入
    private boolean mustInput = false;//是否是必填字段
    private int id;
    private int type;
    private String idKey;
    private String caller;
    private String gCaller;
    private String coreKey;
    private String dfType;//返回的字段类型
    private String dbFind;//是否dbfind的判断
    private String caption = "";//字幕，表示备注
    private String values = "";//字幕对应的值显示
    private String oldValues = "";//变更前的值
    private String valuesKey = "";//字幕对应的值显示的key值
    private String renderer = "";
    private List<Data> datas = new ArrayList<>();

    public Approval(@Duration int type) {
        this.type = type;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        if (StringUtil.isEmpty(oldValues)) {
            oldValues = "内容为空";
        }
        this.oldValues = oldValues;
    }

    public String getDbFind() {
        return dbFind == null ? "" : dbFind;
    }

    public void setDbFind(String dbFind) {
        this.dbFind = dbFind;
    }

    public boolean isNeerInput() {
        return neerInput;
    }

    public void setNeerInput(boolean neerInput) {
        this.neerInput = neerInput;
    }

    public boolean isMustInput() {
        return mustInput;
    }

    public void setMustInput(boolean mustInput) {
        this.mustInput = mustInput;
    }

    public String getDfType() {
        return dfType == null ? "" : dfType;
    }

    public void setDfType(String dfType) {
        this.dfType = dfType;
    }

    public String getCaption() {
        return caption == null ? "" : caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getValues() {
        return values == null ? "" : values;
    }

    public String getgCaller() {
        return gCaller;
    }

    public void setgCaller(String gCaller) {
        this.gCaller = gCaller;
    }

    public String getCoreKey() {
        return coreKey;
    }

    public void setCoreKey(String coreKey) {
        this.coreKey = coreKey;
    }

    public String getNumber() {
        try {
            StringBuilder builder = new StringBuilder();
            int num = 1;
            String v1 = null;
            String v2 = null;
            if (values.contains(".")) {
                int fas = values.indexOf(".");
                v1 = values.substring(0, fas);//整数部分
                v2 = values.substring(fas, values.length());//小数部分
            } else {
                v1 = values;
                v2 = "";
            }
            for (int i = v1.length() - 1; i >= 0; i--) {
                char c = v1.charAt(i);
                builder.insert(0, c);
                if (i > 0 && num == 3 && c != '.') {
                    num = 0;
                    builder.insert(0, ',');
                }
                num++;
            }
            if (!StringUtil.isEmail(v2)) {
                int floatcolumn = floatcolumn();//保留小数后几位
                LogUtil.i("gong", values + "=" + floatcolumn);
                if (floatcolumn == 0) {
                    if (v2.length() - 1 > 3) {
                        builder.append(v2.substring(0, 3));
                    } else {
                        builder.append(v2);
                    }
                } else {
                    //计算小数点后几位
                    if (v2.length() > floatcolumn) {
                        builder.append(v2.substring(0, Math.min(floatcolumn + 1, v2.length())));
                    } else {
                        builder.append(v2);
                    }
                }
            }
            return builder.toString();
        } catch (Exception e) {
            return getValues();
        }
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getValuesKey() {
        return valuesKey == null ? "" : valuesKey;
    }

    public void setValuesKey(String valuesKey) {
        this.valuesKey = valuesKey;
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public List<Data> getDatas() {
        return datas;
    }

    public void setDatas(List<Data> datas) {
        this.datas = datas;
    }

    public int getType() {
        return type == 0 ? MAIN : type;
    }

    public void setType(@Duration int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdKey() {
        return idKey == null ? "" : idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    /**
     * @return 输入类型：0字符输入  1.数字输入  2.日期输入选择  3.下拉选择  4.dbfind
     */
    public int inputType() {
        if (isNumber())
            return 1;
        else if (isDftypeEQ("DT", "D")) {
            return 2;
        } else if (isDftypeEQ("C")) {
            return 3;
        } else if (isDBFind()) {
            return 4;
        } else if (isDftypeEQ("B", "YN")) {
            return 5;
        }
        return 0;
    }

    public boolean isSelect() {
        return dfType.equals("C")
                || "D".equals(dfType)
                || "DT".equals(dfType)
                || "C".equals(dfType)
                || "YN".equals(dfType)
                || "B".equals(dfType);
    }

    public void data2Values() {
        switch (dfType) {
            case "D":
                data2DType();
                break;
            case "B":
                if (values.equals("1")) {
                    values = VALUES_YES;
                } else {
                    values = VALUES_NO;
                }
                setOldSelectValues();
                break;
            case "YN":
                if (values.equals("-1")) {
                    values = VALUES_YES;
                } else if (values.equals("1") && type == DETAIL && isNeerInput()) {
                    values = VALUES_UNKNOWN;
                } else {
                    values = VALUES_NO;
                }
                setOldSelectValues();
                break;

            case "C":
                if (values.equals("-1")) {
                    values = VALUES_YES;
                } else if (values.equals("1") && type == DETAIL && isNeerInput()) {
                    values = VALUES_UNKNOWN;
                } else if (values.equals("0")) {
                    values = VALUES_NO;
                }
                setOldSelectValues();
                break;


        }
    }

    private void setOldSelectValues() {
        if (!StringUtil.isEmpty(oldValues)) {
            if (oldValues.equals("0")) {
                oldValues = VALUES_NO;
            } else if (oldValues.equals("-1") || oldValues.equals("1")) {
                oldValues = VALUES_YES;
            }
        }
    }

    private void data2DType() {
        if (values.endsWith("00:00:00")) {
            long time = TimeUtils.f_str_2_long(values);
            if (time > 0) {
                values = TimeUtils.s_long_2_str(time);
            }
        }
        if (!StringUtil.isEmpty(oldValues) && oldValues.endsWith("00:00:00")) {
            long time = TimeUtils.f_str_2_long(oldValues);
            if (time > 0) {
                oldValues = TimeUtils.s_long_2_str(time);
            }
        }
    }


    public void addValues(String str) {
        values = StringUtil.isEmpty(values) ? str : values + str;
    }

    public boolean isDftypeEQ(String... str) {
        return isEQ(getDfType(), str);
    }

    public boolean isDBFind() {
        return isEQ(getDbFind(), "T", "AT", "M", "DF");
    }

    public boolean isEQ(String key, String... str) {
        if (str != null && str.length > 0) {
            for (String s : str) {
                if (key.equals(s))
                    return true;
            }
        }
        return false;
    }

    public boolean isNumber() {
        return isDftypeEQ("N", "floatcolumn8", "SN") || getDfType().contains("floatcolumn");
    }

    public int floatcolumn() {
        if (getDfType().contains("floatcolumn")) {
            String number = getDfType().replace("floatcolumn", "");
            try {
                return Integer.valueOf(number);
            } catch (Exception e) {

            }
            return 0;
        }
        return 0;
    }

    public static class Data {
        public String value = "";
        public String display = "";

        public Data() {
        }

        public Data(String value, String display) {
            this.value = value;
            this.display = display;
        }
    }


    public void show() {
        Map<String, Object> map = new HashMap<>();
        map.put("neerInput", neerInput);
        map.put("mustInput", mustInput);
        map.put("id", id);
        map.put("idKey", idKey);
        map.put("type", type);
        map.put("dfType", dfType);
        map.put("caption", caption);
        map.put("oldValues", oldValues);
        map.put("values", values);
        map.put("valuesKey", valuesKey);
        LogUtil.i(JSONUtil.map2JSON(map));
        if (ListUtils.isEmpty(datas)) return;
        for (Data d : datas) {
            LogUtil.i("value=" + d.value + "|||" + "   display=" + d.display);
        }
    }

    @IntDef({TITLE, MAIN, DETAIL, ENCLOSURE, POINTS, NODES, TAG, SETUPTASK, NODES_TAG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }
}
