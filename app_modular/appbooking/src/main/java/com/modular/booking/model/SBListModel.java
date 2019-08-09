package com.modular.booking.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSON;

/**
 * 预约列表
 * Created by Arison on 2017/9/27.
 */
public class SBListModel implements Parcelable {
	//    "sc_address":"深圳南山区科技园",
//            "sc_adminname":"吕全明",
//            "sc_companyname":"北大医院",
//            "sc_id":"1",
//            "sc_imageurl":"http://113.105.74.140:8081/u/123/100123/201709/o/ab6d93f74f9b4ec7a06f7dbfd725ec38.png",
//            "sc_imid":"100254",
//            "sc_industry":"医疗",
//            "sc_industrycode":"10003",
//            "sc_latitude":"22",
//            "sc_longitude":"11",
//            "sc_rank":"10",
//            "sc_telephone":"13430818775",
//            "sc_uu":"201"
	private String url;
	private String name;
	private String type;//行业名
	private String industrycode;//行业号码
	private String stars;
	private String latitude;
	private String longitude;
	private String distance;
	private String cash;
	private String phone;//手机号码
	private String bookType;//新加字段，当0时候，为单选时间，1时候为时间段
	private String imid;//新加字段
	private String companyid;//新加字段
	private String address;//新加字段
	private String introduce;//简介
	private int id;//新加字段
	private String starttime;
	private String	 endtime;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStars() {
		return stars;
	}

	public void setStars(String stars) {
		this.stars = stars;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getDistance() {
		return distance;
	}
  
	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	public String getBookType() {
		return bookType;
	}

	public void setBookType(String bookType) {
		this.bookType = bookType;
	}

	public String getImid() {
		return imid;
	}

	public void setImid(String imid) {
		this.imid = imid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

	public String getCompanyid() {
		return companyid;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getIndustrycode() {
		return industrycode;
	}

	public void setIndustrycode(String industrycode) {
		this.industrycode = industrycode;
	}

	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	public SBListModel() {
	}


	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.url);
		dest.writeString(this.name);
		dest.writeString(this.type);
		dest.writeString(this.industrycode);
		dest.writeString(this.stars);
		dest.writeString(this.latitude);
		dest.writeString(this.longitude);
		dest.writeString(this.distance);
		dest.writeString(this.cash);
		dest.writeString(this.phone);
		dest.writeString(this.bookType);
		dest.writeString(this.imid);
		dest.writeString(this.companyid);
		dest.writeString(this.address);
		dest.writeString(this.introduce);
		dest.writeInt(this.id);
		dest.writeString(this.starttime);
		dest.writeString(this.endtime);
	}

	protected SBListModel(Parcel in) {
		this.url = in.readString();
		this.name = in.readString();
		this.type = in.readString();
		this.industrycode = in.readString();
		this.stars = in.readString();
		this.latitude = in.readString();
		this.longitude = in.readString();
		this.distance = in.readString();
		this.cash = in.readString();
		this.phone = in.readString();
		this.bookType = in.readString();
		this.imid = in.readString();
		this.companyid = in.readString();
		this.address = in.readString();
		this.introduce = in.readString();
		this.id = in.readInt();
		this.starttime = in.readString();
		this.endtime = in.readString();
	}

	public static final Creator<SBListModel> CREATOR = new Creator<SBListModel>() {
		@Override
		public SBListModel createFromParcel(Parcel source) {
			return new SBListModel(source);
		}

		@Override
		public SBListModel[] newArray(int size) {
			return new SBListModel[size];
		}
	};
}
