package com.xzjmyk.pm.activity.ui.erp.model.oa;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bitliker on 2017/5/4.
 */

public class BaseSelectParam implements Parcelable {
    private boolean single;//是否单选
    private String title;   //标题

    private String url;//url（通过这个判断是否需要进行网络请求）
    private HashMap<String, Object> param;//请求参数
    private String listKey; //获取jsonArray的key值
    private String showKey;//获取jsonArray对象中的显示的key值

    private ArrayList<BaseSelectModel> selectModels;//本地选择

    public BaseSelectParam() {
        single = true;
        url = "";
        title = "";
        listKey = "";
        showKey = "";
        param = new HashMap<>();
        selectModels = new ArrayList<>();
    }


    protected BaseSelectParam(Parcel in) {
        single = in.readByte() != 0;
        title = in.readString();
        url = in.readString();
        param = (HashMap<String, Object>) in.readSerializable();
        listKey = in.readString();
        showKey = in.readString();
        selectModels = in.createTypedArrayList(BaseSelectModel.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (single ? 1 : 0));
        dest.writeString(title);
        dest.writeString(url);
        dest.writeSerializable(param);
        dest.writeString(listKey);
        dest.writeString(showKey);
        dest.writeTypedList(selectModels);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseSelectParam> CREATOR = new Creator<BaseSelectParam>() {
        @Override
        public BaseSelectParam createFromParcel(Parcel in) {
            return new BaseSelectParam(in);
        }

        @Override
        public BaseSelectParam[] newArray(int size) {
            return new BaseSelectParam[size];
        }
    };


    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getParam() {
        return param;
    }

    public void setParam(HashMap<String, Object> param) {
        this.param = param;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    public String getShowKey() {
        return showKey;
    }

    public void setShowKey(String showKey) {
        this.showKey = showKey;
    }

    public ArrayList<BaseSelectModel> getSelectModels() {
        return selectModels;
    }

    public void setSelectModels(ArrayList<BaseSelectModel> selectModels) {
        this.selectModels = selectModels;
    }
}
