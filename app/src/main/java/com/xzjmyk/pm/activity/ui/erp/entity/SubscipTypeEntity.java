package com.xzjmyk.pm.activity.ui.erp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 订阅类型
 * Created by gongpm on 2016/7/15.
 */
public class SubscipTypeEntity implements Parcelable {
    private int id;
    private String title;
    private String kind;
    private boolean clicked;

    protected SubscipTypeEntity(Parcel in) {
        id = in.readInt();
        title = in.readString();
        kind = in.readString();
        clicked = in.readByte() != 0;
    }
public SubscipTypeEntity(){}
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(kind);
        dest.writeByte((byte) (clicked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubscipTypeEntity> CREATOR = new Creator<SubscipTypeEntity>() {
        @Override
        public SubscipTypeEntity createFromParcel(Parcel in) {
            return new SubscipTypeEntity(in);
        }

        @Override
        public SubscipTypeEntity[] newArray(int size) {
            return new SubscipTypeEntity[size];
        }
    };

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
