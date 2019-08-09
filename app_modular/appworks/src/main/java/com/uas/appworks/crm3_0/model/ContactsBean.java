package com.uas.appworks.crm3_0.model;

/**
 * Created by Arison on 2018/9/18.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2018-09-17 17:51:27
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class ContactsBean implements Parcelable {

    private int linkmanId;
    private int imid;
    private String companyName;
    private String name;
    private String sex;
    private int age;
    private String position;
    private String department;
    private String  brithday;
    private int isDMakers;
    private String notes;
    private String phone;
    private String email;
    private String tel;
    private String imageUrl;

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public String getCompanyName() {
        return companyName;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getSex() {
        return sex;
    }

    public void setAge(int age) {
        this.age = age;
    }
    public int getAge() {
        return age;
    }

    public void setPosition(String position) {
        this.position = position;
    }
    public String getPosition() {
        return position;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    public String getDepartment() {
        return department;
    }

    public void setBrithday(String brithday) {
        this.brithday = brithday;
    }
    public String  getBrithday() {
        return brithday;
    }

    public void setIsDMakers(int isDMakers) {
        this.isDMakers = isDMakers;
    }
    public int getIsDMakers() {
        return isDMakers;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getNotes() {
        return notes;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getLinkmanId() {
        return linkmanId;
    }

    public void setLinkmanId(int linkmanId) {
        this.linkmanId = linkmanId;
    }

    public int getImid() {
        return imid;
    }

    public void setImid(int imid) {
        this.imid = imid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.linkmanId);
        dest.writeInt(this.imid);
        dest.writeString(this.companyName);
        dest.writeString(this.name);
        dest.writeString(this.sex);
        dest.writeInt(this.age);
        dest.writeString(this.position);
        dest.writeString(this.department);
        dest.writeString(this.brithday);
        dest.writeInt(this.isDMakers);
        dest.writeString(this.notes);
        dest.writeString(this.phone);
        dest.writeString(this.email);
        dest.writeString(this.tel);
        dest.writeString(this.imageUrl);
    }

    public ContactsBean() {
    }

    protected ContactsBean(Parcel in) {
        this.linkmanId = in.readInt();
        this.imid = in.readInt();
        this.companyName = in.readString();
        this.name = in.readString();
        this.sex = in.readString();
        this.age = in.readInt();
        this.position = in.readString();
        this.department = in.readString();
        this.brithday = in.readString();
        this.isDMakers = in.readInt();
        this.notes = in.readString();
        this.phone = in.readString();
        this.email = in.readString();
        this.tel = in.readString();
        this.imageUrl = in.readString();
    }

    public static final Parcelable.Creator<ContactsBean> CREATOR = new Parcelable.Creator<ContactsBean>() {
        @Override
        public ContactsBean createFromParcel(Parcel source) {
            return new ContactsBean(source);
        }

        @Override
        public ContactsBean[] newArray(int size) {
            return new ContactsBean[size];
        }
    };
}