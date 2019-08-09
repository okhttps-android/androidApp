package com.uas.appworks.OA.erp.model.form;

import android.os.Parcel;
import android.os.Parcelable;

import com.core.widget.view.selectcalendar.bean.Data;

import java.util.ArrayList;

/**
 * @desc:分组实体类
 * @author：Arison on 2016/11/14
 */
public class GroupData implements Parcelable {

    private int id;
    private String name;
    private ArrayList<Data> datas = new ArrayList<>();
   
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Data> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<Data> datas) {
        this.datas = datas;
    }

    /**
     * 获取Item内容
     *
     * @param pPosition
     * @return
     */
    public Object getItem(int pPosition) {
        // Category排在第一位  
        if (pPosition == 0) {
            return this;
        } else {
            return datas.get(pPosition - 1);
        }
    }

    /**
     * 当前类别Item总数。分组也需要占用一个Item
     *
     * @return
     */
    public int getItemCount() {
        return datas.size() + 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeTypedList(this.datas);
    }

    public GroupData() {
    }

    protected GroupData(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.datas = in.createTypedArrayList(Data.CREATOR);
    }

    public static final Creator<GroupData> CREATOR = new Creator<GroupData>() {
        @Override
        public GroupData createFromParcel(Parcel source) {
            return new GroupData(source);
        }

        @Override
        public GroupData[] newArray(int size) {
            return new GroupData[size];
        }
    };
}