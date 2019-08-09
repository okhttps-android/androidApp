package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.core.app.MyApplication;
import com.core.app.R;

import java.util.List;


/**
 * Created by Bitliker on 2017/4/17.
 */

public class SelectCollisionTurnBean implements Parcelable {

    private String title;//标题
    private String sureText = MyApplication.getInstance().getString(R.string.common_sure);//选择按钮显示
    private String selectType = MyApplication.getInstance().getString(R.string.personnel);//提示选择的类型
    private boolean reBackSelect = true;//是否返回选中人员，否返回非选中人员
    private String selectCode;//选中的人员Code
    private int resultCode = 0x20;//返回的code
    private boolean singleAble = false;//是否单选
    private List<SelectEmUser> selectList;
    

    public SelectCollisionTurnBean() {

    }


    protected SelectCollisionTurnBean(Parcel in) {
        title = in.readString();
        sureText = in.readString();
        selectType = in.readString();
        reBackSelect = in.readByte() != 0;
        selectCode = in.readString();
        resultCode = in.readInt();
        singleAble = in.readByte() != 0;
        selectList = in.createTypedArrayList(SelectEmUser.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(sureText);
        dest.writeString(selectType);
        dest.writeByte((byte) (reBackSelect ? 1 : 0));
        dest.writeString(selectCode);
        dest.writeInt(resultCode);
        dest.writeByte((byte) (singleAble ? 1 : 0));
        dest.writeTypedList(selectList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SelectCollisionTurnBean> CREATOR = new Creator<SelectCollisionTurnBean>() {
        @Override
        public SelectCollisionTurnBean createFromParcel(Parcel in) {
            return new SelectCollisionTurnBean(in);
        }

        @Override
        public SelectCollisionTurnBean[] newArray(int size) {
            return new SelectCollisionTurnBean[size];
        }
    };

    public String getTitle() {
        return title==null?"":title;
    }


    public String getSureText() {
        return sureText;
    }


    public String getSelectType() {
        return selectType;
    }

    public boolean isReBackSelect() {
        return reBackSelect;
    }


    public String getSelectCode() {
        return selectCode;
    }


    public int getResultCode() {
        return resultCode;
    }


    public boolean isSingleAble() {
        return singleAble;
    }

    public List<SelectEmUser> getSelectList() {
        return selectList;
    }

    public SelectCollisionTurnBean setSingleAble(boolean singleAble) {
        this.singleAble = singleAble;
        return this;
    }

    public SelectCollisionTurnBean setResultCode(int resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public SelectCollisionTurnBean setSelectCode(String selectCode) {
        this.selectCode = selectCode;
        return this;
    }

    public SelectCollisionTurnBean setReBackSelect(boolean reBackSelect) {
        this.reBackSelect = reBackSelect;
        return this;
    }

    public SelectCollisionTurnBean setSelectType(String selectType) {
        this.selectType = selectType;
        return this;
    }

    public SelectCollisionTurnBean setSureText(String sureText) {
        this.sureText = sureText;
        return this;
    }

    public SelectCollisionTurnBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public SelectCollisionTurnBean setSelectList(List<SelectEmUser> selectList) {
        this.selectList = selectList;
        return this;
    }
}
