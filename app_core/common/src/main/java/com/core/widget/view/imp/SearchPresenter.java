package com.core.widget.view.imp;

import android.content.Intent;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.widget.view.model.SearchLocationModel;
import com.core.widget.view.model.SearchPoiParam;
import com.uas.applocation.Interface.OnSearchLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.applocation.utils.LocationNeerHelper;
import com.uas.applocation.utils.ModelChangeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 地址搜索
 * Created by Bitliker on 2017/2/6.
 * 参数
 * type(搜索方式int 1.搜索附近 2.搜索地名)
 * 1.radius(范围 int)
 * 2.keyWork(搜索值)
 * <p>
 * 公共：
 * 1、title(标题String)
 * 2、style(主题 int)
 * 3、latlng(对比距离的位置,如没有则表示当前位置)
 * 4、resultCode(返回码 int)
 * 5、resultName(返回key String )
 * 6、isKm（是否使用km距离计算 boolean）
 */

public class SearchPresenter {
    private List<SearchLocationModel> neerList;  //周围地址主体；列表,只有在搜索周围的时候存在
    private int pageNum = 0;


    private ISearchView iSearchView;

    private int radius = 200;//范围 默认200米
    private SearchPoiParam param;
    private LatLng location;//所在的位置
    private boolean isLoadChina;//当前搜索为搜索全国


    public SearchPresenter(ISearchView iSearchView) {
        if (iSearchView == null)//防止传入一个为空的isearchview
            new NullPointerException("ISearchView 不能为null");
        this.iSearchView = iSearchView;
    }


    private OnSearchLocationListener mOnSearchLocationListener = new OnSearchLocationListener() {
        @Override
        public void onCallBack(boolean isSuccess, List<UASLocation> locations) {
            List<SearchLocationModel> listData = new ArrayList<>();
            if (!isSuccess) {
                if (isLoadChina) {
                    isLoadChina = false;
                    LocationNeerHelper.getInstance().searchByInput(MyApplication.getInstance(),null, param.getKeyWork(), pageNum, mOnSearchLocationListener);
                }
            } else {
                if (!ListUtils.isEmpty(locations)) {
                    UASLocation mUASLocation = locations.get(0);
                    iSearchView.showPoiPoint(new LatLng(mUASLocation.getLatitude(), mUASLocation.getLongitude()));
                    for (int i = 0; i < locations.size(); i++) {
                        if (StringUtil.isEmpty(locations.get(i).getName()) || StringUtil.isEmpty(locations.get(i).getAddress())) {
                            locations.remove(i);
                            i--;
                        } else {
                            listData.add(new SearchLocationModel(ModelChangeUtils.location2PoiInfo(locations.get(i))));
                        }
                    }
                }

            }
            if (param.getType() == 1) {
                if (neerList == null) neerList = new ArrayList<>();
                neerList.addAll(listData);
            }
            showData(listData);
        }
    };


    /**
     * 初始化数据
     *
     * @param intent 转入的控制参数
     */
    public void init(Intent intent) {
        if (intent == null) return;
        param = intent.getParcelableExtra("data");
        if (param == null)
            new NullPointerException("param is Null");
        if (!StringUtil.isEmpty(param.getTitle())) {
            iSearchView.setTitle(param.getTitle());
        }
        initData();
    }

    public void search(String keyWork) {
        if (StringUtil.isEmpty(keyWork)) {
            if (param.getType() == 2 || ListUtils.isEmpty(neerList))
                loadDataByNeer();
            else
                showData(neerList);
            return;
        }
        param.setKeyWork(keyWork);
        if (param.getType() == 1) {//附近搜索
            List<SearchLocationModel> chche = new ArrayList<>();
            if (!ListUtils.isEmpty(neerList)) {
                for (SearchLocationModel e : neerList) {
                    String text = e.getPoiInfo().city + e.getPoiInfo().name + e.getPoiInfo().address;
                    boolean isOK = StringUtil.isInclude(text, param.getKeyWork());
                    if (isOK)
                        chche.add(e);
                }
            }
            showData(chche);
        } else {
            loadDataByChina();
        }
    }

    public void endActivity(BaseActivity ct, PoiInfo poi) {
        Intent intent = new Intent();
        intent.putExtra(param.getResultKey(), poi);
        ct.setResult(param.getResultCode(), intent);
        ct.finish();
    }


    private void initData() {
        radius=param.getRadius();
        if (!MyApplication.getInstance().isNetworkActive()) {
            iSearchView.showNotNetWork();
            return;
        }
        switch (param.getType()) {
            case 1:
                loadDataByNeer();
                break;
            case 2:
                if (StringUtil.isEmpty(param.getKeyWork())) {
                    loadDataByNeer();
                } else {
                    loadDataByChina();
                }
                break;
            default:
                break;
        }
    }


    private void loadDataByChina() {
        isLoadChina = true;
        LocationNeerHelper.getInstance().searchByInput(MyApplication.getInstance(),"中国", param.getKeyWork(), pageNum, mOnSearchLocationListener);
    }

    /**
     * 获取当前位置附近位置点
     */
    private void loadDataByNeer() {
        UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();
        neerList = new ArrayList<>();
        location = new LatLng(mUASLocation.getLatitude(), mUASLocation.getLongitude());
        PoiInfo thisInfo = new PoiInfo();
        thisInfo.location = location;
        thisInfo.name = mUASLocation.getName();
        thisInfo.city = mUASLocation.getCityName();
        thisInfo.address = mUASLocation.getAddress();
        neerList.add(new SearchLocationModel(thisInfo));
        LocationNeerHelper.getInstance().loadDataByNeer(MyApplication.getInstance(), radius, pageNum, mOnSearchLocationListener);

    }


    /**
     * 获取完数据后进入显示，先进行排序
     *
     * @param chche
     */
    private void showData(List<SearchLocationModel> chche) {
        LogUtil.i("gong","param.isHineOutSize()="+param.isHineOutSize());
        LogUtil.i("gong","chche.isHineOutSize()="+chche.size());
        List<SearchLocationModel> showModels = new ArrayList<>();
        if (!ListUtils.isEmpty(chche)) {
            for (int i = 0; i < chche.size(); i++) {
                float distance = LocationDistanceUtils.getDistance(chche.get(i).getPoiInfo().location, param.getContrastLatLng());
                chche.get(i).setDistance(distance);
                if (!param.isHineOutSize() || param.getShowRange() > distance) {
                    showModels.add(chche.get(i));
                }
            }
            Comparator<SearchLocationModel> comparator = new Comparator<SearchLocationModel>() {
                public int compare(SearchLocationModel s1, SearchLocationModel s2) {
                    if (s1.getDistance() == s2.getDistance()) return 0;
                    return s1.getDistance() > s2.getDistance() ? 1 : -1;
                }
            };
            Collections.sort(showModels, comparator);
        }
        iSearchView.showPoiList(showModels, param.getDistanceTag());
    }

    public boolean isHineOutSize() {
        return param.isHineOutSize();
    }
}
