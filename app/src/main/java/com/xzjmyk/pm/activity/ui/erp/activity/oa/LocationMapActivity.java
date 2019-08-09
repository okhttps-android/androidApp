package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.utils.BaiduMapUtil;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.applocation.Interface.OnSearchLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.applocation.utils.LocationNeerHelper;
import com.uas.applocation.utils.ModelChangeUtils;
import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择地址
 * 1.外勤签到、打卡签到、会议详情、工作圈发动态定位
 * 2、参数：1、title(标题String) 2、style(主题 int) 3、Single(是否单选 boolean) 7、qzone_select_add (工作圈定位设置自定义标题String)
 * 4、search(是否有搜索框 boolean) 5、resultcode(返回码 int) 6、type(搜索方式int 1.搜索附近 2.搜索地名)
 */
public class LocationMapActivity extends BaseActivity implements OnGetPoiSearchResultListener {
    public static final int REQUCODE = 0x103;  //返回码
    public static final int HIDDEN_REQUCODE = 0x104;
    public static final String REQUESTNAME = "LocationMapActivity_RE";//返回数据提取KEY
    public static final String HIDDEN_REQUESTNAME = "LocationMapActivity_EMPTY";//工作圈定位隐藏KEY
    private MapView mapView;
    private PullToRefreshListView listView;
    private List<PoiInfo> listData;  //周围地址主体；列表
    private LocationMapAdapter adapter;
    private int selectItem = 0;
    private PoiInfo myPoInfo;
    private int location;
    private LatLng point;
    private TextView hidden_location_tv;
    private LinearLayout qzone_map_ll;
    private TextView location_city_tv;
    private RelativeLayout search_rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setThisTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location);
        location = getIntent().getIntExtra("data", 500);

        String qzone_select_add = getIntent().getStringExtra("qzone_select_add");
        if (!TextUtils.isEmpty(qzone_select_add) && qzone_select_add.equals("qzone_select_add")) {
            setTitle(R.string.select_locaton);
        }
        init();
        initView();
        LocationNeerHelper.getInstance().loadDataByNeer(ct, location, 0, point, new OnSearchLocationListener() {
            @Override
            public void onCallBack(boolean isSuccess, List<UASLocation> locations) {
                if (isSuccess){
                    updataByHandle(locations);
                }
            }
        });
    }

    //设置主题
    private void setThisTheme() {
        String action = getIntent().getStringExtra("action");

        int theme = 0;
        if (!StringUtil.isEmpty(action)) {
            if (action.equals("SigninFragment")) {
                theme = getSharedPreferences("cons", MODE_PRIVATE).getInt("theme",
                        R.style.OAThemeSignin);
            } else if (action.equals("MeetDetailsActivity")) {
                theme = getSharedPreferences("cons", MODE_PRIVATE).getInt("theme",
                        R.style.OAThemeMeet);
            } else if (action.equals("Outoffice")) {
                theme = getSharedPreferences("cons", MODE_PRIVATE).getInt("theme",
                        R.style.OAThemeOutOffice);
            }
            setTheme(theme);
        }
    }

    private void initView() {

        mapView = (MapView) findViewById(R.id.bmapView);
        listView = (PullToRefreshListView) findViewById(R.id.listview);
        listView.setEmptyView(R.layout.view_empty);
        BaiduMapUtil.getInstence().setMapViewPoint(mapView, point);
        adapter = new LocationMapAdapter();
        listView.setAdapter(adapter);
        if (!TextUtils.isEmpty(getIntent().getStringExtra("qzoneaddr"))) {
            mapView.setVisibility(View.GONE);
            search_rl.setVisibility(View.GONE);
        }
        //选择
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                if (listData != null && listData.size() > selectItem)
                    intent.putExtra(REQUESTNAME, listData.get(i - 1));
                setResult(REQUCODE, intent);
                LocationMapActivity.this.finish();
                // selectItem = i - 1;
                //adapter.notifyDataSetChanged();
                //BaiduMapUtil.getInstence().setMapViewPoint(mapView, listData.get(i - 1).location, true);
            }
        });
        hidden_location_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(HIDDEN_REQUESTNAME, "");
                setResult(HIDDEN_REQUCODE, intent);
                LocationMapActivity.this.finish();
            }
        });

    }

    private void updataByHandle(List<UASLocation> mUASLocations) {
        if (listData==null){
            listData=new ArrayList<>();
        }else{
            listData.clear();
        }
        for (UASLocation e:mUASLocations)
        {
            listData.add(ModelChangeUtils.location2PoiInfo(e));
        }
            if (!ListUtils.isEmpty(listData)) {
                listData.add(0, myPoInfo);
                adapter.notifyDataSetChanged();
            }

    }

    private void init() {
        search_rl = (RelativeLayout) findViewById(R.id.search_rl);
        hidden_location_tv = (TextView) findViewById(R.id.hidden_location_tv);
        qzone_map_ll = (LinearLayout) findViewById(R.id.qzone_map_ll);
        location_city_tv = (TextView) findViewById(R.id.location_city_tv);
        String addr = new String();
        if (!TextUtils.isEmpty(getIntent().getStringExtra("qzoneaddr"))) {
            addr = getIntent().getStringExtra("qzoneaddr");
            qzone_map_ll.setVisibility(View.VISIBLE);

            Log.i("qzoneaddr", getIntent().getStringExtra("qzoneaddr"));
            if (!TextUtils.isEmpty(UasLocationHelper.getInstance().getUASLocation().getCityName())) {
                location_city_tv.setText(UasLocationHelper.getInstance().getUASLocation().getCityName());
            }
        } else if (!TextUtils.isEmpty(getIntent().getStringExtra("addr"))) {
            addr = getIntent().getStringExtra("addr");
//            qzone_map_ll.setVisibility(View.GONE);
        }
        String addr2 = UasLocationHelper.getInstance().getUASLocation().getName();
        UASLocation mUasLocation = UasLocationHelper.getInstance().getUASLocation();
        point = new LatLng(mUasLocation.getLatitude(), mUasLocation.getLongitude());
        myPoInfo = new PoiInfo();
        myPoInfo.location = point;
        myPoInfo.address = mUasLocation.getAddress();
        myPoInfo.name = StringUtil.isEmpty(addr) ? (StringUtil.isEmpty(addr2) ? "我的位置" : addr2) : addr;

    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult != null) {
            listData = poiResult.getAllPoi();
            if (listData != null && listData.size() > 0) {
                listData.add(0, myPoInfo);
                adapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
    }


    class LocationMapAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (listData == null) {
                return 0;
            }
            return listData.size();
        }

        @Override
        public Object getItem(int i) {
            if (listData == null) {
                return 0;
            }
            return listData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                view = LayoutInflater.from(LocationMapActivity.this).inflate(R.layout.item_location_ls, null);
                holder = new ViewHolder();
                holder.address_tv = (TextView) view.findViewById(R.id.address_tv);
                holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holder.distance_tv = (TextView) view.findViewById(R.id.distance_tv);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.address_tv.setText(listData.get(i).address);
            holder.name_tv.setText(listData.get(i).name);
            if (".0".equals(LocationDistanceUtils.distanceMeStr(listData.get(i).location)))
                holder.distance_tv.setText( "0m");
            else
                holder.distance_tv.setText(LocationDistanceUtils.distanceMe(listData.get(i).location) + "m");
            return view;
        }

        class ViewHolder {
            TextView name_tv, address_tv, distance_tv;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:   //点击保存返回数据
                Intent intent = new Intent();
                if (listData != null && listData.size() > selectItem)
                    intent.putExtra(REQUESTNAME, listData.get(selectItem));
                setResult(REQUCODE, intent);
                LocationMapActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
