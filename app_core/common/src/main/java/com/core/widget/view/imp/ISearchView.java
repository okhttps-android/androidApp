package com.core.widget.view.imp;

import com.baidu.mapapi.model.LatLng;
import com.core.base.HttpImp;
import com.core.widget.view.model.SearchLocationModel;

import java.util.List;

/**
 * Created by Bitliker on 2017/2/6.
 */

public interface ISearchView extends HttpImp {

    void showPoiList(List<SearchLocationModel> models, String distanceTag);

    void showPoiPoint(LatLng location);

    void showNotNetWork();
}
