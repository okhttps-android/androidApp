package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.common.data.ListUtils;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.utils.BaiduMapUtil;
import com.core.utils.ToastUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.baidu.DrivingRouteOverlay;
import com.xzjmyk.pm.activity.util.baidu.OnGetRoutePlanResult;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends BaseActivity {
    @ViewInject(R.id.baiduMap)
    private MapView baiduMap;
    private LatLng toLocation;


    private BroadcastReceiver upLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ViewUtils.inject(this);
        initReceiver();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.other_map) {
            //使用其他地图
            showPopupWindow();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(ct).unregisterReceiver(upLocationReceiver);
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOCATION_CHANGE);
        LocalBroadcastManager.getInstance(ct).registerReceiver(upLocationReceiver, filter);
    }

    private void initView() {
        if (toLocation == null && getIntent() != null)
            toLocation = getIntent().getParcelableExtra("toLocation");
        LatLng location = UasLocationHelper.getInstance().getUASLocation().getLocation();
        BaiduMapUtil.getInstence().setMapViewPoint(baiduMap, location);
        drawDriving(location, toLocation);
    }

    private void drawDriving(LatLng formLocation, LatLng toLocation) {
        //其中myLar myLon 是起点的经纬度  lat和lon是终点的经纬度
        RoutePlanSearch newInstance = RoutePlanSearch.newInstance();
        newInstance.setOnGetRoutePlanResultListener(listener);
        //驾车路线
        DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
        PlanNode from = PlanNode.withLocation(formLocation);  //设置起点
        PlanNode to = PlanNode.withLocation(toLocation);
        drivingOption.from(from);
        drivingOption.to(to);
        drivingOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST); //方案:最短距离 这个自己设置 比如时间短之类的
        newInstance.drivingSearch(drivingOption);
    }


    private OnGetRoutePlanResultListener listener = new OnGetRoutePlanResult() {

        public void onGetDrivingRouteResult(DrivingRouteResult result) {
            //驾车
            if (result == null || SearchResult.ERRORNO.RESULT_NOT_FOUND == result.error || ListUtils.isEmpty(result.getRouteLines()) ||
                    result.getRouteLines().get(0) == null) {
                Toast.makeText(getApplicationContext(), "未搜索到结果", Toast.LENGTH_LONG).show();
                return;
            }
            //开始处理结果了
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(baiduMap.getMap());
            baiduMap.getMap().setOnMarkerClickListener(overlay);// 把事件传递给overlay
            overlay.setData(result.getRouteLines().get(0));// 设置线路为第一条
            overlay.addToMap();
            overlay.zoomToSpan();
        }

    };



    private boolean isInstallBaidu() {
        return new File("/data/data/" + "com.baidu.BaiduMap").exists();
    }

    private boolean isInstallGaode() {
        return new File("/data/data/" + "com.autonavi.minimap").exists();
    }

    private boolean isInstallGoogle() {
        return isAvilible(this, "com.google.android.apps.maps");
    }

    //是否安装腾讯地图
    public boolean isInstallTencent() {
        try {
            if (!new File("/data/data/" + "com.tencent.map").exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    //开启百度导航
    public void startNaviBaidu() {
        try {
            Intent intent = new Intent();
            intent.setData(Uri.parse("baidumap://map/navi?location=" + toLocation.latitude + "," + toLocation.longitude));
            startActivity(intent);
        } catch (BaiduMapAppNotSupportNaviException e) {
            e.printStackTrace();
            ToastUtil.showToast(ct, "您尚未安装百度地图或地图版本过低");
        }
    }


    //高德地图,起点就是定位点
    // 终点是LatLng ll = new LatLng("你的纬度latitude","你的经度longitude");
    public void startNaviGao() {
        if (isAvilible(ct, "com.autonavi.minimap")) {
            try {
                //sourceApplication
                Intent intent = Intent.getIntent("androidamap://navi?sourceApplication=UU互联&poiname=到这里结束&lat=" + toLocation.latitude + "&lon=" + toLocation.longitude + "&dev=0");
                startActivity(intent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.showToast(ct, "您尚未安装高德地图或地图版本过低");
        }
    }


    //谷歌地图,起点就是定位点
    // 终点是LatLng ll = new LatLng("你的latitude","你的longitude");
    public void startNaviGoogle() {
        if (isAvilible(this, "com.google.android.apps.maps")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + toLocation.latitude + "," + toLocation.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            ToastUtil.showToast(ct, "您尚未安装谷歌地图或地图版本过低");
        }

    }


    public void startNaviTencent() {
        try {
            String url = "qqmap://map/routeplan?type=drive&to=" + "到这里结束" + "&tocoord=" + toLocation.latitude + "," + toLocation.longitude + "&policy=2&referer=myapp";
            Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {

        }
    }


    private PopupWindow selectWindow;

    private void showPopupWindow() {
        if (selectWindow == null) {
            selectWindow = initPopupWindow();
        }
        if (selectWindow==null){
            ToastUtil.showToast(ct,"当前手机没有安装相应的地图软件，请在应用市场上安装相应软件");
        }else{
            selectWindow.showAtLocation(getWindow().getDecorView().
                    findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
            DisplayUtil.backgroundAlpha(this, 0.7f);
        }

    }

    private void closePopupWindow() {
        if (selectWindow != null)
            selectWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    private PopupWindow initPopupWindow() {
        View viewContext = LayoutInflater.from(mContext).inflate(com.uas.appme.R.layout.pop_navigation_select, null);
        boolean hasMap = false;
        if (isInstallBaidu()) {
            hasMap = true;
            View view = viewContext.findViewById(R.id.baiduMapTv);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(mSelectOnclickLister);
            viewContext.findViewById(R.id.baiduMapTag).setVisibility(View.VISIBLE);
        } else {
            viewContext.findViewById(R.id.baiduMapTv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.baiduMapTag).setVisibility(View.GONE);
        }
        if (isInstallGaode()) {
            hasMap = true;
            View view = viewContext.findViewById(R.id.gaodeMapTv);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(mSelectOnclickLister);
            viewContext.findViewById(R.id.gaodeMapTag).setVisibility(View.VISIBLE);
        } else {
            viewContext.findViewById(R.id.gaodeMapTv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.gaodeMapTag).setVisibility(View.GONE);
        }
        if (isInstallTencent()) {
            hasMap = true;
            View view = viewContext.findViewById(R.id.tencentMapTv);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(mSelectOnclickLister);
            viewContext.findViewById(R.id.tencentMapTag).setVisibility(View.VISIBLE);
        } else {
            viewContext.findViewById(R.id.tencentMapTv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.tencentMapTag).setVisibility(View.GONE);
        }
        if (isInstallGoogle()) {
            hasMap = true;
            View view = viewContext.findViewById(R.id.googleMapTv);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(mSelectOnclickLister);
        } else {
            viewContext.findViewById(R.id.googleMapTv).setVisibility(View.GONE);
        }
        if (hasMap) {
            viewContext.findViewById(com.uas.appme.R.id.cancel_tv).setOnClickListener(mSelectOnclickLister);
            PopupWindow selectWindow = new PopupWindow(viewContext,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true);
            selectWindow.setAnimationStyle(com.uas.appme.R.style.MenuAnimationFade);
            selectWindow.setBackgroundDrawable(mContext.getResources().getDrawable(com.uas.appme.R.drawable.bg_popuwin));
            selectWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    closePopupWindow();
                }
            });
            return selectWindow;
        } else {
            return null;
        }

    }

    private View.OnClickListener mSelectOnclickLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.baiduMapTv) {
                startNaviBaidu();
            } else if (id == R.id.gaodeMapTv) {
                startNaviGao();
            } else if (id == R.id.tencentMapTv) {
                startNaviTencent();
            } else if (id == R.id.googleMapTv) {
                startNaviGoogle();
            } else {
                closePopupWindow();
            }

        }
    };
}
