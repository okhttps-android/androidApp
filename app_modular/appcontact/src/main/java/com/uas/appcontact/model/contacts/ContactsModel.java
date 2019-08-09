package com.uas.appcontact.model.contacts;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arison on 2017/7/18.
 * 通讯录合并
 */

public class ContactsModel implements Parcelable {
    
    private String imid;
    private String ownerId;
    private String name;
    private String company;
    private String whichsys;
    private String phone;
    private String email;
    private Integer type;
    public boolean isClick = false;


    public ContactsModel() {
    }

    public String getImid() {
        return imid;
    }

    public void setImid(String imid) {
        this.imid = imid;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getWhichsys() {
        return whichsys;
    }

    public void setWhichsys(String whichsys) {
        this.whichsys = whichsys;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imid);
        dest.writeString(this.ownerId);
        dest.writeString(this.name);
        dest.writeString(this.company);
        dest.writeString(this.whichsys);
        dest.writeString(this.phone);
        dest.writeString(this.email);
        dest.writeValue(this.type);
        dest.writeByte(this.isClick ? (byte) 1 : (byte) 0);
    }

    protected ContactsModel(Parcel in) {
        this.imid = in.readString();
        this.ownerId = in.readString();
        this.name = in.readString();
        this.company = in.readString();
        this.whichsys = in.readString();
        this.phone = in.readString();
        this.email = in.readString();
        this.type = (Integer) in.readValue(Integer.class.getClassLoader());
        this.isClick = in.readByte() != 0;
    }

    public static final Creator<ContactsModel> CREATOR = new Creator<ContactsModel>() {
        @Override
        public ContactsModel createFromParcel(Parcel source) {
            return new ContactsModel(source);
        }

        @Override
        public ContactsModel[] newArray(int size) {
            return new ContactsModel[size];
        }
    };
}
