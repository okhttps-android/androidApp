package com.uas.appworks.model.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.SerializedName;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/19 16:27
 */
public class CommonFormBean implements MultiItemEntity {
    public static final int COMMON_FORM_GRAY_LINE = 1;
    public static final int COMMON_FORM_CONTENT_ITEM = 2;

    private int mItemType = -1;

    /**
     * fd_caption : 客户名称
     * mfd_caption : 企业名称
     * fd_field : bc_custname
     * fd_value : 明年
     * fd_maxlength : 100
     * fd_group : 基本信息
     * fd_detno : 4
     * fd_type : SF
     * fd_readonly : F
     * mfd_isdefault : -1
     * fd_id : 265674
     */

    @SerializedName("fd_caption")
    private String caption;
    @SerializedName("mfd_caption")
    private String mcaption;
    @SerializedName("fd_field")
    private String field;
    @SerializedName("fd_value")
    private String value;
    @SerializedName("fd_maxlength")
    private int maxlength;
    @SerializedName("fd_group")
    private String group;
    @SerializedName("fd_detno")
    private double detno;
    @SerializedName("fd_type")
    private String type;
    @SerializedName("fd_readonly")
    private String readonly;
    @SerializedName("mfd_isdefault")
    private int isdefault;
    @SerializedName("fd_id")
    private int id;

    public void setItemType(int itemType) {
        mItemType = itemType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMcaption() {
        return mcaption;
    }

    public void setMcaption(String mcaption) {
        this.mcaption = mcaption;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public double getDetno() {
        return detno;
    }

    public void setDetno(double detno) {
        this.detno = detno;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReadonly() {
        return readonly;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public int getIsdefault() {
        return isdefault;
    }

    public void setIsdefault(int isdefault) {
        this.isdefault = isdefault;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getItemType() {
        return mItemType;
    }
}
