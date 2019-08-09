package com.xzjmyk.pm.activity.ui.erp.model.oa;

import com.baidu.mapapi.search.core.PoiInfo;

/**
 * Created by Bitliker on 2017/2/22.
 */

public class SearchLocationModel {

    private float distance;
    private PoiInfo poiInfo;

    public SearchLocationModel(PoiInfo poiInfo) {
        this.poiInfo = poiInfo;
    }


    public PoiInfo getPoiInfo() {
        return poiInfo;
    }

    public void setPoiInfo(PoiInfo poiInfo) {
        this.poiInfo = poiInfo;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
