package com.core.widget.view.selectcalendar.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.core.model.Approval;


/**
 * @desc:动态表单字段 item数据实体类
 * @author：Arison on 2016/11/14
 */
public class Data implements Parcelable {

    private int groupId;//组ID---控件需要用到
    private String group;//组名----多组的情况
    private boolean isSelected;
    private boolean isForm;//是否是主表
    private int detno;//编号
    private String readonly;//是否只读
    private String allowblank;//是否允许为空
    private int isDefault;//是否展示和隐藏
    private String type;//字段类型
    private String name;//文本字段名
    private int isNeed;//必填
    private String fd_logictype;//单选，多选可选值
    private String fd_defaultvalue;//单选，多选默认值
    private String value;//编辑框的内容-界面显示
    private String displayValue;//下拉字段隐藏的值
    private String field;//字段名
    private String formStoreKey;//formstore key 上传需要用到的key
    private String gridCaller;//从表caller
    private int fdid;
    private boolean isEditing = true;//是否正在编辑
    private String maxlength;//最大长度
    private String detailId;//关联主表id
    private String detailDid;//明细表id


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getDetno() {
        return detno;
    }

    public void setDetno(int detno) {
        this.detno = detno;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean form) {
        isForm = form;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    public String getReadonly() {
        return readonly;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public String getAllowblank() {
        return allowblank;
    }

    public void setAllowblank(String allowblank) {
        this.allowblank = allowblank;
    }

    public int getIsNeed() {
        return isNeed;
    }

    public void setIsNeed(int isNeed) {
        this.isNeed = isNeed;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getFormStoreKey() {
        return formStoreKey;
    }

    public void setFormStoreKey(String formStoreKey) {
        this.formStoreKey = formStoreKey;
    }

    public String getGridCaller() {
        return gridCaller;
    }

    public void setGridCaller(String gridCaller) {
        this.gridCaller = gridCaller;
    }

    public int getFdid() {
        return fdid;
    }

    public void setFdid(int fdid) {
        this.fdid = fdid;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    public String getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getDetailDid() {
        return detailDid;
    }

    public void setDetailDid(String detailDid) {
        this.detailDid = detailDid;
    }

    public String getFd_logictype() {
        return fd_logictype;
    }

    public void setFd_logictype(String fd_logictype) {
        this.fd_logictype = fd_logictype;
    }

    public String getFd_defaultvalue() {
        return fd_defaultvalue;
    }

    public void setFd_defaultvalue(String fd_defaultvalue) {
        this.fd_defaultvalue = fd_defaultvalue;
    }


    public Data() {
    }

    public Data(boolean isMain, Approval approval) {
        this.groupId = isMain ? 0 : 1;
        group = isMain ? "基本信息" : "明细项1";
        this.field = approval.getValuesKey();
        this.name = approval.getCaption();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.groupId);
        dest.writeString(this.group);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeInt(this.detno);
        dest.writeString(this.readonly);
        dest.writeInt(this.isDefault);
        dest.writeString(this.type);
        dest.writeString(this.name);
        dest.writeInt(this.isNeed);
        dest.writeString(this.fd_logictype);
        dest.writeString(this.fd_defaultvalue);
        dest.writeString(this.value);
        dest.writeString(this.displayValue);
        dest.writeString(this.field);
        dest.writeString(this.formStoreKey);
        dest.writeInt(this.fdid);
        dest.writeByte(this.isEditing ? (byte) 1 : (byte) 0);
        dest.writeString(this.maxlength);
        dest.writeString(this.detailId);
        dest.writeString(this.detailDid);
    }

    protected Data(Parcel in) {
        this.groupId = in.readInt();
        this.group = in.readString();
        this.isSelected = in.readByte() != 0;
        this.detno = in.readInt();
        this.readonly = in.readString();
        this.isDefault = in.readInt();
        this.type = in.readString();
        this.name = in.readString();
        this.isNeed = in.readInt();
        this.fd_logictype = in.readString();
        this.fd_defaultvalue = in.readString();
        this.value = in.readString();
        this.displayValue = in.readString();
        this.field = in.readString();
        this.formStoreKey = in.readString();
        this.fdid = in.readInt();
        this.isEditing = in.readByte() != 0;
        this.maxlength = in.readString();
        this.detailId = in.readString();
        this.detailDid = in.readString();
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel source) {
            return new Data(source);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };
}