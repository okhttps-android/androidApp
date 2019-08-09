package com.xzjmyk.pm.activity.bean.oa;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bitliker on 2017/5/15.
 */

public class IntentBaseParam implements Parcelable {
    public static final int DEFAULT_RESULTCODE = 0x51;//默认返回resultCode
    public static final String DEFAULT_RESULTKEY = "resultKey";
    protected boolean isShowSearchEdit;//是否显示搜索框,默认是显示
    protected int resultCode;//返回码
    protected String resultKey;//返回数据的map参数key
    protected String title;//显示的标题


    public IntentBaseParam() {

    }

    protected IntentBaseParam(Parcel in) {
        isShowSearchEdit = in.readByte() != 0;
        resultCode = in.readInt();
        resultKey = in.readString();
        title = in.readString();
    }

    public static final Creator<IntentBaseParam> CREATOR = new Creator<IntentBaseParam>() {
        @Override
        public IntentBaseParam createFromParcel(Parcel in) {
            return new IntentBaseParam(in);
        }

        @Override
        public IntentBaseParam[] newArray(int size) {
            return new IntentBaseParam[size];
        }
    };

    public boolean isShowSearchEdit() {
        return isShowSearchEdit;
    }

    public void setShowSearchEdit(boolean showSearchEdit) {
        isShowSearchEdit = showSearchEdit;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultKey() {
        return resultKey;
    }

    public void setResultKey(String resultKey) {
        this.resultKey = resultKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isShowSearchEdit ? 1 : 0));
        dest.writeInt(resultCode);
        dest.writeString(resultKey);
        dest.writeString(title);
    }
}
