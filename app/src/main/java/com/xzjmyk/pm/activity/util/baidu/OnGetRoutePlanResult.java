package com.xzjmyk.pm.activity.util.baidu;

import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * Created by Bitliker on 2017/4/13.
 */

public abstract class OnGetRoutePlanResult implements OnGetRoutePlanResultListener {
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        //走路
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        //公交
    }


    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        //自行车
    }
}
