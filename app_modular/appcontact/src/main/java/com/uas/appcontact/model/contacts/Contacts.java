package com.uas.appcontact.model.contacts;

/**
 * Created by Arison on 2017/6/19.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @功能:联系人
 */
public class Contacts implements Parcelable {
    
    public int id;
    public int rawid;
    public String name = "未填写";//姓名
    public String nickname;//昵称
    public String phone;
    public List<String> phones = new ArrayList<>();//多个手机人号码
    public String phoneAddress;
    public boolean ischecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneAddress() {
        return phoneAddress;
    }

    public void setPhoneAddress(String phoneAddress) {
        this.phoneAddress = phoneAddress;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRawid() {
        return rawid;
    }

    public void setRawid(int rawid) {
        this.rawid = rawid;
    }

    public boolean isIschecked() {
        return ischecked;
    }

    public void setIschecked(boolean ischecked) {
        this.ischecked = ischecked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Contacts() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        /*将Person的成员写入Parcel， 
         * 注：Parcel中的数据是按顺序写入和读取的，即先被写入的就会先被读取出来 
         */
        dest.writeInt(id);
        dest.writeInt(rawid);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(nickname);
        //dest.writeStringList(phones); //报错 
        dest.writeString(phoneAddress);
        dest.writeByte((byte) (ischecked ? 1 : 0));
    }

    //该静态域是必须要有的，而且名字必须是CREATOR，否则会出错  
    public static final Creator<Contacts> CREATOR =
            new Creator<Contacts>() {

                @Override
                public Contacts createFromParcel(Parcel source) {
                    //从Parcel读取通过writeToParcel方法写入的Person的相关成员信息  
                    Contacts contacts = new Contacts();
                    contacts.id = source.readInt();
                    contacts.rawid = source.readInt();
                    contacts.name = source.readString();
                    contacts.phone = source.readString();
                    contacts.nickname = source.readString();
                    //contacts.phones=source.readStringList();
                    contacts.phoneAddress = source.readString();
                    contacts.ischecked = source.readByte() != 0;
                    return contacts;
                }

                @Override
                public Contacts[] newArray(int size) {
                    return new Contacts[size];
                }
            };
}
