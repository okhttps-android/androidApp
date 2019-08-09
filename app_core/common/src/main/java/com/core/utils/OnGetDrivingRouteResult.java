package com.core.utils;

import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * Created by Bitliker on 2016/12/9.
 */

public abstract class OnGetDrivingRouteResult implements OnGetRoutePlanResultListener {
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }


    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }
}
