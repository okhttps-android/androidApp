package com.uas.appworks.CRM.erp.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.model.Approval;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/12/19.
 */

public class CycleCountAdd {
    private int id;
    private boolean allowblank;
    private String field;
    private String caption;
    private String values;
    private String type;
    private String which;
    private List<Approval.Data> datas = new ArrayList<>();

    public CycleCountAdd(JSONObject object) {
        setValues(JSONUtil.getText(object, "fd_defaultvalue"));
        setField(JSONUtil.getText(object, "fd_field"));
        setCaption(JSONUtil.getText(object, "fd_caption"));
        setType(JSONUtil.getText(object, "fd_type"));
        if ("T".equals(JSONUtil.getText(object, "fd_dbfind"))) {
            setType("DBFIND");
        }
        if (getType().equals("C")) {
            JSONArray combostores = JSONUtil.getJSONArray(object, "COMBOSTORE");
            if (!ListUtils.isEmpty(combostores)) {
                for (int i = 0; i < combostores.size(); i++) {
                    JSONObject o = combostores.getJSONObject(i);
                    datas.add(new Approval.Data(JSONUtil.getText(o, "DLC_VALUE"), JSONUtil.getText(o, "DLC_DISPLAY")));
                }
                if (1 == combostores.size()) {
                    setValues(datas.get(0).display);
                }
            }
        }
        setAllowblank(JSONUtil.getText(object, "fd_allowblank").equals("T"));
        setId(JSONUtil.getInt(object, "fd_id"));
        setWhich("from");
    }

    public List<Approval.Data> getDatas() {
        return datas;
    }

    public String getType() {
        return type == null ? "" : type;

    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAllowblank() {
        return allowblank;
    }

    public void setAllowblank(boolean allowblank) {
        this.allowblank = allowblank;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }


    public String getValues() {
        return values == null ? "" : values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getWhich() {
        return which;
    }

    public void setWhich(String which) {
        this.which = which;
    }
}
