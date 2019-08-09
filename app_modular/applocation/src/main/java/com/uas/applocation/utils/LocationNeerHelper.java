package com.uas.applocation.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.applocation.Interface.OnBaiduPoiListener;
import com.uas.applocation.Interface.OnSearchLocationListener;
import com.uas.applocation.R;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;

import java.util.ArrayList;
import java.util.List;

public class LocationNeerHelper {

    private static LocationNeerHelper INSTANCE;

    public static LocationNeerHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (LocationNeerHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocationNeerHelper();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 获取当前位置周边位置信息
     *
     * @param ct
     * @param radius                    范围
     * @param pagerNum                  页数
     * @param mOnSearchLocationListener
     */
    public void loadDataByNeer(Context ct, int radius, int pagerNum, OnSearchLocationListener mOnSearchLocationListener) {
        loadDataByNeer(ct, radius, pagerNum, null, mOnSearchLocationListener);
    }

    /**
     * 获取指定位置周边位置信息
     *
     * @param ct
     * @param radius                    范围
     * @param pagerNum                  页数
     * @param latLng                    指定位置点
     * @param mOnSearchLocationListener
     */
    public void loadDataByNeer(Context ct, int radius, int pagerNum, LatLng latLng, OnSearchLocationListener mOnSearchLocationListener) {
        if (latLng == null) {
            UASLocation local = UasLocationHelper.getInstance().getUASLocation();
            latLng = new LatLng(local.getLatitude(), local.getLongitude());
        }
        if (UasLocationHelper.getInstance().getLocationManager().isOutChina()) {
            loadNativeByNeer(ct, latLng.latitude, latLng.longitude, radius, mOnSearchLocationListener);
        } else {
            loadBaiduByNeer(radius, pagerNum, latLng, mOnSearchLocationListener);
        }
    }

    /**
     * 根据数据的关键字搜索为位置点
     *
     * @param ct
     * @param city                      城市
     * @param keyWord
     * @param pagerNum
     * @param mOnSearchLocationListener
     */
    public void searchByInput(Context ct, String city, String keyWord, int pagerNum, OnSearchLocationListener mOnSearchLocationListener) {
        UASLocation local = UasLocationHelper.getInstance().getUASLocation();
        if (local == null) {
            local = new UASLocation(UASLocation.TYPE_BAIDU);
        }
        if (TextUtils.isEmpty(city)) {
            city = local.getCityName();
        }

        if (UasLocationHelper.getInstance().getLocationManager().isOutChina()) {
            loadByInput(ct, keyWord, mOnSearchLocationListener);
        } else {
            searchBaiduByInput(city, keyWord, pagerNum, mOnSearchLocationListener);
        }
    }


    //获取百度定位周边为位置点
    private static void loadBaiduByNeer(int radius, int pagerNum, LatLng latLng, final OnSearchLocationListener mOnSearchLocationListener) {
        PoiNearbySearchOption option = new PoiNearbySearchOption();
        PoiSearch mPoiSearch = PoiSearch.newInstance();
        option.keyword("公司")
                .radius(radius)
                .pageNum(pagerNum)
                .pageCapacity(40)
                .location(latLng);
        mPoiSearch.setOnGetPoiSearchResultListener(new OnBaiduPoiListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (mOnSearchLocationListener != null) {
                    if (poiResult != null) {
                        List<PoiInfo> mPoiInfos = poiResult.getAllPoi();
                        if (mPoiInfos != null && mPoiInfos.size() > 0) {
                            List<UASLocation> locations = new ArrayList<>();
                            UASLocation location = null;
                            for (PoiInfo e : mPoiInfos) {
                                location = new UASLocation(UASLocation.TYPE_BAIDU);
                                location.setLongitude(e.location.longitude);
                                location.setLatitude(e.location.latitude);
                                location.setName(e.name);
                                location.setAddress(e.address);
                                locations.add(location);
                            }
                            mOnSearchLocationListener.onCallBack(true, locations);
                        } else {
                            mOnSearchLocationListener.onCallBack(false, null);
                        }
                    } else {
                        mOnSearchLocationListener.onCallBack(false, null);
                    }
                }
            }
        });
        mPoiSearch.searchNearby(option);
    }


    private static void searchBaiduByInput(String city, String location, int pagerNum, final OnSearchLocationListener mOnSearchLocationListener) {
        try {
            PoiSearch mPoiSearch = PoiSearch.newInstance();
            mPoiSearch.setOnGetPoiSearchResultListener(new OnBaiduPoiListener() {
                @Override
                public void onGetPoiResult(PoiResult poiResult) {
                    if (mOnSearchLocationListener != null) {
                        if (poiResult != null && poiResult.getAllPoi() != null) {
                            List<PoiInfo> poiLists = poiResult.getAllPoi();
                            List<UASLocation> mUASLocations = new ArrayList<>();
                            for (PoiInfo e : poiLists) {
                                UASLocation model = ModelChangeUtils.poiInfo2Location(e);
                                if (model==null||(model.getLatitude()==0&&model.getLatitude()==0)){
                                }else{
                                    mUASLocations.add(model);
                                }
                            }
                            mOnSearchLocationListener.onCallBack(true, mUASLocations);
                        } else {
                            mOnSearchLocationListener.onCallBack(false, null);
                        }
                    }
                }
            });
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city(city)
                    .keyword(location)
                    .pageNum(pagerNum)
                    .pageCapacity(1000)
            );
        } catch (Exception e) {
            if (e != null)
                Log.i("gongpengming", "e =" + e.getMessage());
        }
    }


    //获取google地图信息
    private HttpClient mHttpClient;

    private HttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new HttpClient.Builder("https://maps.googleapis.com/maps/api/place/").isDebug(false)
                    .connectTimeout(5000)
                    .readTimeout(5000).build();
        }
        return mHttpClient;
    }

    /**
     * 获取当前位置附近位置
     *
     * @param ct
     * @param latitude
     * @param longitude
     * @param mOnSearchLocationListener
     */
    public void loadNativeByNeer(final Context ct, double latitude, double longitude, float radius, final OnSearchLocationListener mOnSearchLocationListener) {
        HttpClient request = new HttpClient.Builder()
                .url("nearbysearch/json")
                .isDebug(true)
                .add("location", latitude + "," + longitude)
                .add("radius", radius)
                .add("keyword", "公司")
                .add("output", "json")
                .method(Method.GET)
                .add("key", ct.getString(R.string.app_google_key))
                .build();
        getHttpClient().Api().send(request, new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object s) {
                try {
                    JSONObject object = JSON.parseObject(s.toString());
                    if (object != null
                            && object.containsKey("status") && "OK".equals(object.getString("status").toUpperCase())
                            && object.containsKey("results") && mOnSearchLocationListener != null) {
                        handlerNeer(object.getJSONArray("results"), mOnSearchLocationListener);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Object t) {
                if (mOnSearchLocationListener != null) {
                    mOnSearchLocationListener.onCallBack(false, null);
                }
            }
        }));
    }

    private void handlerNeer(JSONArray array, OnSearchLocationListener mOnSearchLocationListener) throws Exception {
        JSONObject object;
        UASLocation mUASLocation;
        List<UASLocation> mUASLocations = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            mUASLocation = new UASLocation(UASLocation.TYPE_NATIVE);
            String name = object.getString("name");
            String address = null;
            if (object.containsKey("vicinity")) {
                address = object.getString("vicinity");
            }
            if (TextUtils.isEmpty(address) && object.containsKey("formatted_address")) {
                address = object.getString("formatted_address");
            }
//            String province = object.getString("province");
            String city = object.getString("city");//TODO 城市
//            String area = object.getString("area");
            mUASLocation.setName(name);
            mUASLocation.setAddress(address);
            mUASLocation.setCityName(city);
            JSONObject geometry = object.getJSONObject("geometry");
            if (geometry != null) {
                JSONObject location = geometry.getJSONObject("location");
                if (location != null) {
                    float lat = location.getFloat("lat");
                    float lng = location.getFloat("lng");
                    if (lat > 0 && lng > 0) {
                        mUASLocation.setLatitude(lat);
                        mUASLocation.setLongitude(lng);
                    }
                }
            }
            mUASLocations.add(mUASLocation);
        }
        mOnSearchLocationListener.onCallBack(true, mUASLocations);
    }

    /**
     * 通过输入进行搜索
     *
     * @param ct
     * @param keyWord
     * @param mOnSearchLocationListener
     */
    public void loadByInput(Context ct, String keyWord, final OnSearchLocationListener mOnSearchLocationListener) {
        if (ct == null || mOnSearchLocationListener == null)
            return;
        HttpClient.Builder requestBuilder = new HttpClient.Builder()
                .isDebug(true)
                .url("textsearch/json")
                .add("query", keyWord)
                .method(Method.GET)
                .add("key", ct.getString(R.string.app_google_key));
        HttpClient request = requestBuilder.build();
        getHttpClient().Api().send(request, new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object s) {
                try {
                    JSONObject object = JSON.parseObject(s.toString());
                    if (object != null
                            && object.containsKey("status") && "OK".equals(object.getString("status").toUpperCase())
                            && object.containsKey("results") && mOnSearchLocationListener != null) {
                        handlerNeer(object.getJSONArray("results"), mOnSearchLocationListener);
                    }
                } catch (Exception e) {

                }
            }
            @Override
            public void onFailure(Object t) {
                if (mOnSearchLocationListener != null) {
                    mOnSearchLocationListener.onCallBack(false, null);
                }
            }
        }));
    }

}
