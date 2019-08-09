package com.core.widget.view.model;

/**
 * 因为考虑到查询地址的时候传的参数太多了，所以定义一个参数实体类
 * Created by Bitliker on 2017/2/22.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;
import com.uas.applocation.UasLocationHelper;

/**
 * 调用地图显示界面，由于多处调用，所以参数太多，整理为一个参数类中
 * create by Bitliker 2017/2/23
 */
public class SearchPoiParam implements Parcelable {
    public static final int DEFAULT_RESULTCODE = 0x51;
    public static final String DEFAULT_RESULTKEY = "resultKey";

    private boolean isShowSearchEdit;//是否显示搜索框
    private int resultCode;//返回码
    private String resultKey;//返回数据的map参数key
    private int riceTimes;//显示的距离是米的多少倍数，(如果是km==1000)
    private String distanceTag;//显示距离的单位
    private LatLng contrastLatLng;//作为对比距离的位置，如果为空就取当前位置
    private String title;//显示的标题

    private int type;//搜索类型 1.表示搜索附近  2.表示搜索名字位置
    private int radius;//搜索附近时候选择范围大小
    private String keyWork;//搜索名字位置时候位置名称

    private boolean hineOutSize;
    private float showRange;

    public SearchPoiParam() {
        isShowSearchEdit = true;
        riceTimes = 1;
        type = 1;
        resultCode = 0x20;
        resultKey = DEFAULT_RESULTKEY;
        distanceTag = "m";
        title = "地址微调";
        radius = 200;
        hineOutSize = false;
        showRange = 1000;
        contrastLatLng = UasLocationHelper.getInstance().getUASLocation().getLocation();
    }

    public SearchPoiParam(boolean isShowSearchEdit, int resultCode, String resultKey, int riceTimes, String distanceTag, LatLng contrastLatLng, String title, int type, int radius, String keyWork) {
        this.isShowSearchEdit = isShowSearchEdit;
        this.resultCode = resultCode;
        this.resultKey = resultKey;
        this.riceTimes = riceTimes;
        this.distanceTag = distanceTag;
        this.contrastLatLng = contrastLatLng;
        this.title = title;
        this.type = type;
        this.radius = radius;
        this.keyWork = keyWork;
    }

    public boolean isShowSearchEdit() {
        return isShowSearchEdit;
    }

    public boolean isHineOutSize() {
        return hineOutSize;
    }

    public void setHineOutSize(boolean hineOutSize) {
        this.hineOutSize = hineOutSize;
    }

    public float getShowRange() {
        return showRange;
    }

    public void setShowRange(float showRange) {
        this.hineOutSize = true;
        this.showRange = showRange;
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

    public int getRiceTimes() {
        return riceTimes;
    }

    public void setRiceTimes(int riceTimes) {
        this.riceTimes = riceTimes;
    }

    public String getDistanceTag() {
        return distanceTag;
    }

    public void setDistanceTag(String distanceTag) {
        this.distanceTag = distanceTag;
    }

    public LatLng getContrastLatLng() {
        return contrastLatLng;
    }

    public void setContrastLatLng(LatLng contrastLatLng) {
        this.contrastLatLng = contrastLatLng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRadius() {
        return radius<=0?200:radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getKeyWork() {
        return keyWork;
    }

    public void setKeyWork(String keyWork) {
        this.keyWork = keyWork;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isShowSearchEdit ? (byte) 1 : (byte) 0);
        dest.writeInt(this.resultCode);
        dest.writeString(this.resultKey);
        dest.writeInt(this.riceTimes);
        dest.writeString(this.distanceTag);
        dest.writeParcelable(this.contrastLatLng, flags);
        dest.writeString(this.title);
        dest.writeInt(this.type);
        dest.writeInt(this.radius);
        dest.writeString(this.keyWork);
        dest.writeByte(this.hineOutSize ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.showRange);
    }

    protected SearchPoiParam(Parcel in) {
        this.isShowSearchEdit = in.readByte() != 0;
        this.resultCode = in.readInt();
        this.resultKey = in.readString();
        this.riceTimes = in.readInt();
        this.distanceTag = in.readString();
        this.contrastLatLng = in.readParcelable(LatLng.class.getClassLoader());
        this.title = in.readString();
        this.type = in.readInt();
        this.radius = in.readInt();
        this.keyWork = in.readString();
        this.hineOutSize = in.readByte() != 0;
        this.showRange = in.readFloat();
    }

    public static final Creator<SearchPoiParam> CREATOR = new Creator<SearchPoiParam>() {
        @Override
        public SearchPoiParam createFromParcel(Parcel source) {
            return new SearchPoiParam(source);
        }

        @Override
        public SearchPoiParam[] newArray(int size) {
            return new SearchPoiParam[size];
        }
    };
}
