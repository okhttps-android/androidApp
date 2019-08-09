package com.xzjmyk.pm.activity.ui.circle;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.xzjmyk.pm.activity.R;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 */
public class BaiduMapActivity extends FragmentActivity {

	private double latitude;
	private double longitude;
	private String userName;

	private MapView mMapView;
	InfoWindow mInfoWindow = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_baidu_map);
		if (getIntent() != null) {
			latitude = getIntent().getDoubleExtra("latitude", 0);
			longitude = getIntent().getDoubleExtra("longitude", 0);
			userName = getIntent().getStringExtra("userName");
		}

		mMapView = (MapView) findViewById(R.id.bmapView);

		// 定义Maker坐标点
		LatLng point = new LatLng(latitude, longitude);
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(point).icon(bitmap).title(userName);
		// 在地图上添加Marker，并显示
		mMapView.getMap().addOverlay(option);
		
		MapStatus mapStatus = new MapStatus.Builder().zoom(mMapView.getMap().getMaxZoomLevel() - 3).target(point).build();
		MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mapStatus);
		mMapView.getMap().animateMapStatus(u);//设置为中心显示

		mMapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				String name = arg0.getTitle();
				if (mInfoWindow == null) {
					// 创建InfoWindow展示的view
					Button button = new Button(BaiduMapActivity.this);
					button.setBackgroundResource(R.drawable.popup);
					button.setText(name);
					// 创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
					mInfoWindow = new InfoWindow(button, arg0.getPosition(), -47);// 显示InfoWindow
					mMapView.getMap().showInfoWindow(mInfoWindow);
				} else {
					mInfoWindow = null;
					mMapView.getMap().hideInfoWindow();
				}
				return false;
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}
}
