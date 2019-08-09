package com.xzjmyk.pm.activity.ui.erp.model.oa;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用于返回选择的项目和提供选择的列表
 * Created by pengminggong on 2016/9/29.
 */
public class BaseSelectModel implements Parcelable {

    private int id;
    private boolean clicked;
    private String code;
    private String name;
    private String detailJson;

    public BaseSelectModel() {
        id = 0;
        clicked = false;
        code = null;
        name = null;
        detailJson = null;
    }

    protected BaseSelectModel(Parcel in) {
        id = in.readInt();
        clicked = in.readByte() != 0;
        code = in.readString();
        name = in.readString();
        detailJson = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeByte((byte) (clicked ? 1 : 0));
        dest.writeString(code);
        dest.writeString(name);
        dest.writeString(detailJson);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseSelectModel> CREATOR = new Creator<BaseSelectModel>() {
        @Override
        public BaseSelectModel createFromParcel(Parcel in) {
            return new BaseSelectModel(in);
        }

        @Override
        public BaseSelectModel[] newArray(int size) {
            return new BaseSelectModel[size];
        }
    };


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }
}
