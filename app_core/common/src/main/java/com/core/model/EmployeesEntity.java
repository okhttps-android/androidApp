package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.core.app.R;
import com.core.app.MyApplication;

public class EmployeesEntity implements Parcelable {

    private int EM_ID = 0;
    private String EM_CODE = "";
    private String EM_NAME = MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String EM_POSITION = MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String EM_DEFAULTORNAME = MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String EM_DEPART = MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String EM_MOBILE = MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String EM_EMAIL =MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String EM_UU = "";
    private String COMPANY = MyApplication.getInstance().getString(R.string.common_noinput) ;
    private String WHICHSYS = "";
    private int Em_defaultorid = 0;
    private String FLAG = "";
    private int Em_IMID = 0;
    private boolean isClick;//by gongpengming 偷懒 不想开新类  所以在原类中添加属性，如果出现错误，即使联系修改

    public EmployeesEntity() {
    }

    protected EmployeesEntity(Parcel in) {
        EM_ID = in.readInt();
        EM_CODE = in.readString();
        EM_NAME = in.readString();
        EM_POSITION = in.readString();
        EM_DEFAULTORNAME = in.readString();
        EM_DEPART = in.readString();
        EM_MOBILE = in.readString();
        EM_EMAIL = in.readString();
        EM_UU = in.readString();
        COMPANY = in.readString();
        WHICHSYS = in.readString();
        Em_defaultorid = in.readInt();
        FLAG = in.readString();
        Em_IMID = in.readInt();
        isClick = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(EM_ID);
        dest.writeString(EM_CODE);
        dest.writeString(EM_NAME);
        dest.writeString(EM_POSITION);
        dest.writeString(EM_DEFAULTORNAME);
        dest.writeString(EM_DEPART);
        dest.writeString(EM_MOBILE);
        dest.writeString(EM_EMAIL);
        dest.writeString(EM_UU);
        dest.writeString(COMPANY);
        dest.writeString(WHICHSYS);
        dest.writeInt(Em_defaultorid);
        dest.writeString(FLAG);
        dest.writeInt(Em_IMID);
        dest.writeByte((byte) (isClick ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmployeesEntity> CREATOR = new Creator<EmployeesEntity>() {
        @Override
        public EmployeesEntity createFromParcel(Parcel in) {
            return new EmployeesEntity(in);
        }

        @Override
        public EmployeesEntity[] newArray(int size) {
            return new EmployeesEntity[size];
        }
    };

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    public int getEm_IMID() {
        return Em_IMID;
    }

    public void setEm_IMID(int em_IMID) {
        Em_IMID = em_IMID;
    }

    public int getEM_ID() {
        return this.EM_ID;
    }

    public void setEM_ID(int EM_ID) {
        this.EM_ID = EM_ID;
    }

    public String getEM_EMAIL() {
        return EM_EMAIL;
    }

    public void setEM_EMAIL(String eM_EMAIL) {
        EM_EMAIL = eM_EMAIL;
    }

    public String getEM_CODE() {
        return this.EM_CODE;
    }

    public void setEM_CODE(String EM_CODE) {
        this.EM_CODE = EM_CODE;
    }

    public String getEM_NAME() {
        return this.EM_NAME;
    }

    public void setEM_NAME(String EM_NAME) {
        this.EM_NAME = EM_NAME;
    }

    public String getEM_POSITION() {
        return this.EM_POSITION;
    }

    public void setEM_POSITION(String EM_POSITION) {
        this.EM_POSITION = EM_POSITION;
    }

    public String getEM_DEFAULTORNAME() {
        return this.EM_DEFAULTORNAME;
    }

    public void setEM_DEFAULTORNAME(String EM_DEFAULTORNAME) {
        this.EM_DEFAULTORNAME = EM_DEFAULTORNAME;
    }

    public String getEM_DEPART() {
        return this.EM_DEPART;
    }

    public void setEM_DEPART(String EM_DEPART) {
        this.EM_DEPART = EM_DEPART;
    }

    public String getEM_MOBILE() {
        return this.EM_MOBILE;
    }

    public void setEM_MOBILE(String EM_MOBILE) {
        this.EM_MOBILE = EM_MOBILE;
    }

    public String getEM_UU() {
        return EM_UU;
    }

    public void setEM_UU(String eM_UU) {
        EM_UU = eM_UU;
    }

    public String getCOMPANY() {
        return this.COMPANY;
    }

    public void setCOMPANY(String COMPANY) {
        this.COMPANY = COMPANY;
    }

    public String getWHICHSYS() {
        return this.WHICHSYS;
    }

    public void setWHICHSYS(String WHICHSYS) {
        this.WHICHSYS = WHICHSYS;
    }

    public int getEm_defaultorid() {
        return Em_defaultorid;
    }

    public void setEm_defaultorid(int em_defaultorid) {
        Em_defaultorid = em_defaultorid;
    }

    public String getFLAG() {
        return FLAG;
    }

    public void setFLAG(String fLAG) {
        FLAG = fLAG;
    }
}
