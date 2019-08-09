package com.core.utils;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.common.data.ListUtils;
import com.core.app.R;

import java.util.List;

/**
 * 百度地图工具类（重复代码过多，放在工具类中）
 * Created by Bitliker on 2016/7/15.
 */
public class BaiduMapUtil {
    private static BaiduMapUtil instence = null;
    private PoiNearbySearchOption option;//设置搜索的条件
    private RoutePlanSearch drivingSearch;//设置查询路线的条件

    private BaiduMapUtil() {
    }

    public static BaiduMapUtil getInstence() {
        if (instence == null) {
            synchronized (BaiduMapUtil.class) {
                instence = new BaiduMapUtil();
            }
        }
        return instence;
    }

    //在使用时候在生命周期结束应该调用该方法
    public void onDestroy() {
        try {
            if (drivingSearch != null) drivingSearch.destroy();
            if (option != null) option = null;
        } catch (Exception e) {

        }
    }


    /****************** 设置在地图上显示 ******************/
    /**
     * 设置当前地图显示位置
     *
     * @param mapView mapView视图控件
     * @param point   显示位置
     */
    public void setMapViewPoint(MapView mapView, LatLng point) {
        setMapViewPoint(mapView, point, false);
    }


    /**
     * 将定点显示在指定位置
     *
     * @param mapView 地图控件
     * @param point   显示位置
     * @param isClear 是否清除前面的点
     */
    public void setMapViewPoint(MapView mapView, LatLng point, boolean isClear) {
        try {
            if (point == null) return;
            // 构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
            // 构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            if (isClear) mapView.getMap().clear();
            // 在地图上添加Marker，并显示
            mapView.getMap().addOverlay(option);
            mapView.showZoomControls(false);
            MapStatus mapStatus = new MapStatus.Builder().zoom(mapView.getMap().getMaxZoomLevel() - 3).target(point).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mapStatus);
            mapView.getMap().animateMapStatus(u);//设置为中心显示

        } catch (Exception e) {

        }
    }
    public void setMapViewPoint(TextureMapView mapView, LatLng point, boolean isClear) {
        try {
            if (point == null) return;
            // 构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
            // 构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            if (isClear) mapView.getMap().clear();
            // 在地图上添加Marker，并显示
            mapView.getMap().addOverlay(option);
            mapView.showZoomControls(false);
            MapStatus mapStatus = new MapStatus.Builder().zoom(mapView.getMap().getMaxZoomLevel() - 3).target(point).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mapStatus);
            mapView.getMap().animateMapStatus(u);//设置为中心显示

        } catch (Exception e) {

        }
    }
    public void setMapViewPoint(MapView mapView, List<LatLng> points, boolean isClear) {
        if (ListUtils.isEmpty(points)) return;
        try {
            if (isClear) mapView.getMap().clear();
            // 构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
            LatLng theOne=null;
            for (LatLng point : points) {
                if (point == null || point.longitude == -1 || point.latitude == -1) continue;
                if (theOne==null){
                    theOne=point;
                }
                // 构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
                // 在地图上添加Marker，并显示
                mapView.getMap().addOverlay(option);
            }
            mapView.showZoomControls(false);
            MapStatus mapStatus = new MapStatus.Builder().zoom(mapView.getMap().getMaxZoomLevel() - 3).target(theOne).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mapStatus);
            mapView.getMap().animateMapStatus(u);//设置为中心显示
        } catch (Exception e) {

        }
    }

    public void getDrivingRoute(LatLng from, LatLng to, OnGetRoutePlanResultListener onGetRoutePlanResultListener) {
        drivingSearch = RoutePlanSearch.newInstance();
        drivingSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
        PlanNode stNode = PlanNode.withLocation(from);
        PlanNode enNode = PlanNode.withLocation(to);
        drivingSearch.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode)
                .to(enNode));
    }


}
