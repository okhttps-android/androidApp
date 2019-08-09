package com.uas.applocation.utils;

import android.location.Address;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.uas.applocation.model.UASLocation;

/**
 * 类转换工具
 */
public class ModelChangeUtils {

    public static PoiInfo location2PoiInfo(UASLocation mUasLocation) {
        PoiInfo mPoiInfo = new PoiInfo();
        mPoiInfo.location = new LatLng(mUasLocation.getLatitude(), mUasLocation.getLongitude());
        mPoiInfo.name = mUasLocation.getName();
        mPoiInfo.address = mUasLocation.getAddress();
        mPoiInfo.city = mUasLocation.getCityName();
        return mPoiInfo;
    }

    public static UASLocation poiInfo2Location(PoiInfo mPoiInfo) {
        UASLocation mUasLocation = new UASLocation(UASLocation.TYPE_BAIDU);
        if (mPoiInfo != null) {
            if (mPoiInfo.location!=null){
                mUasLocation.setLatitude(mPoiInfo.location.latitude);
                mUasLocation.setLongitude(mPoiInfo.location.longitude);
            }
            mUasLocation.setName(mPoiInfo.name);
            mUasLocation.setAddress(mPoiInfo.address);
            mUasLocation.setCityName(mPoiInfo.city);
        }
        return mUasLocation;
    }

    public static UASLocation address2Location(Address address) {
        UASLocation mUASLocation = new UASLocation(UASLocation.TYPE_NATIVE);
        mUASLocation.setLocationOk(true);
        mUASLocation.setProvince(address.getLocality());
        mUASLocation.setCityName(address.getSubAdminArea());
        mUASLocation.setCountry(address.getCountryName());
        mUASLocation.setName(address.getFeatureName());
        mUASLocation.setDistrict(address.getThoroughfare());
        mUASLocation.setAddress(address.getAddressLine(0));
        return mUASLocation;
    }

}
