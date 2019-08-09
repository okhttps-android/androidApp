package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.common.LogUtil;

/**
 * 通用选择员工和选择部门通用实体类
 * Created by Bitliker on 2017/2/14.
 */

public class SelectEmUser implements Parcelable {

    private int imId;       //imid,员工时候存在
    private int emId;       //emId,员工时候存在
    private int defaultorid;//部门id
    private int number;     //部门人员数量

    private String emCode;  //emcode(人员编号，部门编号)
    private String emName;  //名字
    private String position;//职位
    private String depart;//部门名称

    private String tag;//标记，如班次名称（当tag==“1”的时候表示是自己好友）

    private boolean isClick;//是否点击了

    public SelectEmUser() {
    }

    public SelectEmUser(EmployeesEntity e) {
        setDefaultorid(e.getEm_defaultorid());
        setDepart(e.getEM_DEPART());
        setEmName(e.getEM_NAME());
        setImId(e.getEm_IMID());
        setEmCode(e.getEM_CODE());
        setPosition(e.getEM_POSITION());
        setEmId(e.getEM_ID());
        LogUtil.i("getDefaultorid="+getDefaultorid());
        LogUtil.i("getDepart="+getDepart());
        LogUtil.i("getEmName="+getEmName());
        LogUtil.i("getImId="+getImId());
        LogUtil.i("getEmCode="+getEmCode());
        LogUtil.i("getPosition="+getPosition());
        LogUtil.i("getEmId="+getEmId());
    }


    protected SelectEmUser(Parcel in) {
        imId = in.readInt();
        emId = in.readInt();
        defaultorid = in.readInt();
        number = in.readInt();
        emCode = in.readString();
        emName = in.readString();
        position = in.readString();
        depart = in.readString();
        tag = in.readString();
        isClick = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imId);
        dest.writeInt(emId);
        dest.writeInt(defaultorid);
        dest.writeInt(number);
        dest.writeString(emCode);
        dest.writeString(emName);
        dest.writeString(position);
        dest.writeString(depart);
        dest.writeString(tag);
        dest.writeByte((byte) (isClick ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SelectEmUser> CREATOR = new Creator<SelectEmUser>() {
        @Override
        public SelectEmUser createFromParcel(Parcel in) {
            return new SelectEmUser(in);
        }

        @Override
        public SelectEmUser[] newArray(int size) {
            return new SelectEmUser[size];
        }
    };

    public int getImId() {
        return imId;
    }

    public void setImId(int imId) {
        this.imId = imId;
    }

    public int getEmId() {
        return emId;
    }

    public void setEmId(int emId) {
        this.emId = emId;
    }

    public int getDefaultorid() {
        return defaultorid;
    }

    public void setDefaultorid(int defaultorid) {
        this.defaultorid = defaultorid;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getEmCode() {
        return emCode;
    }

    public void setEmCode(String emCode) {
        this.emCode = emCode;
    }

    public String getEmName() {
        return emName;
    }

    public void setEmName(String emName) {
        this.emName = emName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }
}
