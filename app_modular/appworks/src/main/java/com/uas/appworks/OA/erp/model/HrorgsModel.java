package com.uas.appworks.OA.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bitliker on 2017/2/8.
 */

public class HrorgsModel extends BaseNodeBean implements Parcelable {
    private String code;

    public HrorgsModel(int id, int pId, String name, String code) {
        super(id, pId, name);
        this.code = code;
    }


    protected HrorgsModel(Parcel in) {
        super(in.readInt(), in.readInt(), in.readString());
        code = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(pId);
        dest.writeString(name);
        dest.writeString(code);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HrorgsModel> CREATOR = new Creator<HrorgsModel>() {
        @Override
        public HrorgsModel createFromParcel(Parcel in) {
            return new HrorgsModel(in);
        }

        @Override
        public HrorgsModel[] newArray(int size) {
            return new HrorgsModel[size];
        }
    };

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
