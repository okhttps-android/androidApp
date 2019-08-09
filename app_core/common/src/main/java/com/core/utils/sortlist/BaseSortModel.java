package com.core.utils.sortlist;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseSortModel<T> implements Parcelable{
    public T bean;
    public String firstLetter;// 首字母
    public String wholeSpell;// 全拼
    public String simpleSpell;// 首字母简拼
    public boolean isClick = false;
    public BaseSortModel(){}

    protected BaseSortModel(Parcel in) {
        firstLetter = in.readString();
        wholeSpell = in.readString();
        simpleSpell = in.readString();
        isClick = in.readByte() != 0;
    }

    public static final Creator<BaseSortModel> CREATOR = new Creator<BaseSortModel>() {
        @Override
        public BaseSortModel createFromParcel(Parcel in) {
            return new BaseSortModel(in);
        }

        @Override
        public BaseSortModel[] newArray(int size) {
            return new BaseSortModel[size];
        }
    };

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public String getWholeSpell() {
        return wholeSpell;
    }

    public void setWholeSpell(String wholeSpell) {
        this.wholeSpell = wholeSpell;
    }

    public String getSimpleSpell() {
        return simpleSpell;
    }

    public void setSimpleSpell(String simpleSpell) {
        this.simpleSpell = simpleSpell;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(firstLetter);
        parcel.writeString(wholeSpell);
        parcel.writeString(simpleSpell);
        parcel.writeByte((byte) (isClick ? 1 : 0));
    }
}
