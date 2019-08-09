package com.modular.booking.activity.services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.utils.DistanceUtils;
import com.core.widget.DrawableCenterTextView;
import com.core.widget.view.MyGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.imageloader.ImageLoaderUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemBserviceHeaderAdapter;
import com.modular.booking.adapter.ItemBserviceListAdapter;
import com.modular.booking.adapter.ItemPopListAdapter;
import com.modular.booking.model.ItemsSelectType1;
import com.modular.booking.model.SBListModel;
import com.modular.booking.model.SBMenuModel;
import com.modular.booking.widget.ObservableScrollView;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc:服务预约
 * @author：Arison on 2017/9/26
 */
public class BServicesActivity extends OABaseActivity implements View.OnClickListener, ObservableScrollView.OnObservableScrollViewListener {
    private PullToRefreshListView mListView;
    ItemBserviceHeaderAdapter itemAdapter;
    ItemBserviceListAdapter mListAdapter;
    List<SBListModel> modelList = new ArrayList<>();
    private ImageView mBackImageView;
    private DrawableCenterTextView mSearchTv;
    private RelativeLayout mRelativeTop;
    private Drawable drawBg;
    private int pageSize = 1;
    private TextView tv_distance;
    private Banner mBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window window = getWindow();
//           // window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            //window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
//        }

        setContentView(R.layout.activity_bservices);

        initView();
        initEvent();
        intData();
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    private void initView() {
        setTitle("");
        View view = LayoutInflater.from(ct).inflate(R.layout.action_book_service, null);
        mBackImageView = (ImageView) view.findViewById(R.id.book_service_back);
        mSearchTv = (DrawableCenterTextView) view.findViewById(R.id.book_service_et);
        mRelativeTop = (RelativeLayout) view.findViewById(R.id.book_service_action);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        drawBg = getResources().getDrawable(R.color.antionbarcolor);
        //  drawBg.setAlpha(0);
        // bar.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#330000ff")));
//        bar.setBackgroundDrawable(drawBg);
        bar.setCustomView(view);


        mListView = (PullToRefreshListView) findViewById(R.id.mListView);
        initHeader();
        mListAdapter = new ItemBserviceListAdapter(mContext, modelList);
        mListView.setAdapter(mListAdapter);
    }

    private void initHeader() {
        View headview = LayoutInflater.from(ct).inflate(R.layout.header_bservices_grid, null);
        mListView.getRefreshableView().addHeaderView(headview);
        tv_distance = findViewById(R.id.tv_distance);
        MyGridView mGridView = findViewById(R.id.mGridView);
        mBanner = findViewById(R.id.city_industry_service_main_banner);
        mBanner.setImageLoader(new BannerImageLoader());
        
        List<String> imgList=new ArrayList<>();
        imgList.add("http://113.105.74.140:8081/u/123/100123/201801/o/f5624e77ca374a319a28d06c92752fbb.png");
        imgList.add("http://113.105.74.140:8081/u/123/100123/201801/o/f5624e77ca374a319a28d06c92752fbb.png");
        imgList.add("http://113.105.74.140:8081/u/123/100123/201801/o/f5624e77ca374a319a28d06c92752fbb.png");
        mBanner.update(imgList);
        mBanner.start();
        
        
        List<SBMenuModel> menuModels = new ArrayList<>();
        SBMenuModel menuModel = new SBMenuModel();
        menuModel.setUrl("");
        menuModel.setCode("10003");
        menuModel.setIcon(R.drawable.icon_food);
        menuModel.setDesc(getString(R.string.fulldelicious));
        menuModel.setDescColor(Color.parseColor("#FDC34F"));
        menuModel.setTitle(getString(R.string.service_foods));
        menuModels.add(menuModel);

        menuModel = new SBMenuModel();
        menuModel.setUrl("");
        menuModel.setCode("10004");
        menuModel.setIcon(R.drawable.icon_hair);
        menuModel.setDesc(getString(R.string.fashion));
        menuModel.setTitle(getString(R.string.beautysalons));
        menuModel.setDescColor(Color.parseColor("#FE7F19"));
        menuModels.add(menuModel);

        menuModel = new SBMenuModel();
        menuModel.setUrl("");
        menuModel.setCode("10006");
        menuModel.setIcon(R.drawable.icon_ktv);
        menuModel.setDesc(getString(R.string.musicfestival));
        menuModel.setTitle("KTV");
        menuModel.setDescColor(Color.parseColor("#2ED3AE"));
        menuModels.add(menuModel);

        menuModel = new SBMenuModel();
        menuModel.setUrl("");
        menuModel.setCode("10002");
        menuModel.setIcon(R.drawable.icon_sport);
        menuModel.setDesc(getString(R.string.takeaction));
        menuModel.setTitle(getString(R.string.exercise));
        menuModel.setDescColor(Color.parseColor("#F96173"));
        menuModels.add(menuModel);

        menuModel = new SBMenuModel();
        menuModel.setUrl("");
        menuModel.setCode("10005");
        menuModel.setIcon(R.drawable.icon_club);
        menuModel.setDesc(getString(R.string.discountforreservation));
        menuModel.setTitle(getString(R.string.Club));
        menuModel.setDescColor(Color.parseColor("#65EA6E"));
        menuModels.add(menuModel);

        menuModel = new SBMenuModel();
        menuModel.setUrl("");
        menuModel.setCode("10001");
        menuModel.setIcon(R.drawable.icon_hospital);
        menuModel.setDesc(getString(R.string.healthassistant));
        menuModel.setTitle(getString(R.string.Hospitalregistration));
        menuModel.setDescColor(Color.parseColor("#D357DE"));
        menuModels.add(menuModel);

        itemAdapter = new ItemBserviceHeaderAdapter(mContext, menuModels);
        mGridView.setAdapter(itemAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemBserviceHeaderAdapter.ViewHolder viewHolder = (ItemBserviceHeaderAdapter.ViewHolder) view.getTag();
                Intent intent = new Intent(mContext, BServiceListActivity.class);
                intent.putExtra("SBMenuModel", viewHolder.menuModel);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        drawBg.setAlpha(80);
        mBanner.stopAutoPlay();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                drawBg.setAlpha(255);
//            }
//        },10);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                drawBg.setAlpha(255);
//            }
//        }, 10);
    }

    private void initEvent() {
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        tv_distance.setOnClickListener(this);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {


            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                pageSize = 1;
                if (tv_distance.getText().toString().equals("全部") || tv_distance.getText().toString().equals("距离")) {
                    loadListData(pageSize);
                } else {
                    loadSeachData(tv_distance.getTag().toString(), pageSize);
                }

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                pageSize++;
                if (tv_distance.getText().toString().equals("全部") || tv_distance.getText().toString().equals("距离")) {
                    loadListData(pageSize);
                } else {
                    loadSeachData(tv_distance.getTag().toString(), pageSize);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SBListModel model = mListAdapter.getItem((int) id);
//                ItemBserviceListAdapter.ViewHolder viewHolder= (ItemBserviceListAdapter.ViewHolder) view.getTag();
//                viewHolder.tvCash.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        CommonUtil.phoneAction(mContext,model.getPhone());
//                    }
//                });
                Intent intent = null;
                if (model.getType().equals("会所") || model.getType().equals("美容美发")) {
                    intent = new Intent(ct, BServiceDetailActivity.class);
                } else {
                    intent = new Intent(ct, BServiceAddActivity.class);
                }
                intent.putExtra("model", model);
                startActivity(intent);
            }
        });
        //监听滚动事件
//        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            private int DISTANCE =900;
//
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int offset = 0;
//                if (firstVisibleItem == 0||firstVisibleItem==1) {
//                    View firstItem = mListView.getRefreshableView().getChildAt(0);
//                    if (firstItem != null) {
//                        offset = 0 - firstItem.getTop()+120;
//                    }
//                } else {
//                    offset =DISTANCE-1;
//                }
//
//                float percent = (offset * 1f) / (DISTANCE * 1f) ;
//                if (percent >=0 && percent < 1) {
//                    drawBg.setAlpha((int)(percent *255));
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        getWindow().setStatusBarColor(getResources().getColor(R.color.antionbarcolor));
//                    }
//                }else if(percent>=1&&firstVisibleItem!=1){
//                    drawBg.setAlpha((int)(percent *255));
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        getWindow().setStatusBarColor(getResources().getColor(R.color.antionbarcolor));
//                    }
//                }else if(percent==1){
//                    drawBg.setAlpha(255);
//                }
//                LogUtil.d(TAG,"offset:"+offset);
//                LogUtil.d(TAG,"percent:"+(int)(percent *255));
//             
//
//            }
//        });

        mBackImageView.setOnClickListener(this);
        mSearchTv.setOnClickListener(this);
    }

    private void intData() {
        loadListData(pageSize);
        initBannerData();
    }

    
    private void initBannerData(){
          HttpClient httpClient=new HttpClient.Builder(Constants.IM_BASE_URL()).build();
                 httpClient.Api().send(new HttpClient.Builder()
                 .url("user/appAd")
                 .add("token",MyApplication.getInstance().mAccessToken)
                 .method(Method.POST)
                 .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {
          
                     @Override
                     public void onResponse(Object o) {
                         try {
                             LogUtil.d(TAG,toString());
                             String result=  JSON.parseObject(o.toString()).getString("result");
                             String url=  JSON.parseArray(result).getJSONObject(0).getString("aa_urlc");
                             List<String> imgList=new ArrayList<>();
                             imgList.add(url);
                             imgList.add(url);
                             imgList.add(url);
                             mBanner.update(imgList);
                             mBanner.start();
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }));
        
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        drawBg.setAlpha(120);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        drawBg.setAlpha(255);
//    }

    public void loadSeachData(String distance, final int pageSize) {
        showLoading();
        UASLocation helper = UasLocationHelper.getInstance().getUASLocation();
        //distance, type, longitude, latitude, pageIndex, token
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appSearchStore")
                .add("type", "0")
                .add("distance", distance)
                .add("longitude", helper.getLongitude())
                .add("latitude", helper.getLatitude())
                .add("pageIndex", String.valueOf(pageSize))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                dimssLoading();
                mListView.onRefreshComplete();
                try {
                    if (pageSize == 1) {
                        modelList.clear();
                    }
                    LogUtil.d("o.toString" + o.toString());
                    if (JSONUtil.validate(o.toString())) {
                        JSONObject object = JSON.parseObject(o.toString());
                        JSONArray jsonArray = object.getJSONArray("reslut");
                        if (!ListUtils.isEmpty(jsonArray)) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject mObject = jsonArray.getJSONObject(i);
                                SBListModel sbListModel = new SBListModel();
                                sbListModel.setName(mObject.getString("sc_companyname"));
                                sbListModel.setAddress(mObject.getString("sc_address"));
                                sbListModel.setBookType(mObject.getString("sc_booktype"));
                                sbListModel.setUrl(mObject.getString("sc_imageurl"));
                                sbListModel.setType(mObject.getString("sc_industry"));
                                sbListModel.setIndustrycode(mObject.getString("sc_industrycode"));
                                sbListModel.setCompanyid(mObject.getString("sc_uu"));
                                sbListModel.setPhone(mObject.getString("sc_telephone"));

                                String sc_latitude = mObject.getString("sc_latitude");
                                String sc_longitude = mObject.getString("sc_longitude");
                                sbListModel.setLatitude(sc_latitude);
                                sbListModel.setLongitude(sc_longitude);
                                sbListModel.setIntroduce(mObject.getString("sc_introduce"));
                                LatLng latLng = new LatLng(Double.valueOf(sc_latitude), Double.valueOf(sc_longitude));
                                float distance = LocationDistanceUtils.distanceMe(latLng);
                                sbListModel.setDistance(String.valueOf(DistanceUtils.mToKm(distance, "#.0")));
                                sbListModel.setCash(mObject.getString("sc_telephone"));
                                sbListModel.setImid(mObject.getString("sc_imid"));
                                sbListModel.setId(mObject.getInteger("sc_id"));
                                sbListModel.setStarttime(mObject.getString("sc_starttime"));
                                sbListModel.setEndtime(mObject.getString("sc_endtime"));
                                modelList.add(sbListModel);
                            }
                            LogUtil.d("myTest", JSON.toJSONString(modelList));
                            if (mListAdapter == null) {
                                mListAdapter = new ItemBserviceListAdapter(mContext, modelList);
                                mListView.setAdapter(mListAdapter);
                            } else {
                                mListAdapter.notifyDataSetChanged();
                            }
                            //mListAdapter.notifyDataSetChanged();
                        } else {
                            ToastMessage("没有更多数据");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }


    public void loadListData(final int pageSize) {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appStoreList")
                .add("type", "0")
                .add("pageIndex", String.valueOf(pageSize))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                mListView.onRefreshComplete();
                try {
                    if (pageSize == 1) {
                        modelList.clear();
                    }
                    LogUtil.d("o.toString" + o.toString());
                    if (JSONUtil.validate(o.toString())) {
                        JSONObject object = JSON.parseObject(o.toString());
                        JSONArray jsonArray = object.getJSONArray("reslut");
                        if (!ListUtils.isEmpty(jsonArray)) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject mObject = jsonArray.getJSONObject(i);
                                SBListModel sbListModel = new SBListModel();
                                sbListModel.setName(mObject.getString("sc_companyname"));
                                sbListModel.setAddress(mObject.getString("sc_address"));
                                sbListModel.setBookType(mObject.getString("sc_booktype"));
                                sbListModel.setUrl(mObject.getString("sc_imageurl"));
                                sbListModel.setType(mObject.getString("sc_industry"));
                                sbListModel.setIndustrycode(mObject.getString("sc_industrycode"));
                                sbListModel.setCompanyid(mObject.getString("sc_uu"));
                                sbListModel.setPhone(mObject.getString("sc_telephone"));

                                String sc_latitude = mObject.getString("sc_latitude");
                                String sc_longitude = mObject.getString("sc_longitude");
                                sbListModel.setLatitude(sc_latitude);
                                sbListModel.setLongitude(sc_longitude);
                                sbListModel.setIntroduce(mObject.getString("sc_introduce"));
                                LatLng latLng = new LatLng(Double.valueOf(sc_latitude), Double.valueOf(sc_longitude));
                                float distance = LocationDistanceUtils.distanceMe(latLng);
                                sbListModel.setDistance(String.valueOf(DistanceUtils.mToKm(distance, "#.0")));
                                sbListModel.setCash(mObject.getString("sc_telephone"));
                                sbListModel.setImid(mObject.getString("sc_imid"));
                                sbListModel.setId(mObject.getInteger("sc_id"));
                                sbListModel.setStarttime(mObject.getString("sc_starttime"));
                                sbListModel.setEndtime(mObject.getString("sc_endtime"));
                                modelList.add(sbListModel);
                            }
                            LogUtil.d("myTest", JSON.toJSONString(modelList));
                            if (mListAdapter == null) {
                                mListAdapter = new ItemBserviceListAdapter(mContext, modelList);
                                mListView.setAdapter(mListAdapter);
                            } else {
                                mListAdapter.notifyDataSetChanged();
                            }
                            //mListAdapter.notifyDataSetChanged();
                        } else {
                            ToastMessage("没有更多数据");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.book_service_back) {
            onBackPressed();
        } else if (id == R.id.book_service_et) {
            Intent intent = new Intent();
//            intent.setClass(this, BServiceSearchActivity.class);
            intent.setClass(this, BServiceListActivity.class);
            SBMenuModel menuModel=new SBMenuModel();
            menuModel.setCode("0");
            intent.putExtra("SBMenuModel", menuModel);
            startActivity(intent);
        } else if (id == R.id.tv_distance) {
            //搜索距离-showPopupWindow
            showPopupWindow(v);
        }
    }


    //距离popupWindow
    private PopupWindow popupWindow = null;
    private int selectId;

    public void showPopupWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
//            SelectSimpleAdapter adapter = new SelectSimpleAdapter(ct, getPopData(),
//                    R.layout.item_pop_list);
            List<ItemsSelectType1> datas = getItemsSelect();
            final ItemPopListAdapter adapter = new ItemPopListAdapter(ct, datas);
            plist.setAdapter(adapter);
            Drawable drawable = getResources().getDrawable(R.drawable.selector_check_items);
            plist.setSelector(drawable);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    pageSize = 1;
                    selectId = position;
                    adapter.setSelectId(selectId);
                    adapter.notifyDataSetChanged();
//                    ItemPopListAdapter.ViewHolder viewHolder= (ItemPopListAdapter.ViewHolder) view.getTag();
//                    adapter.getItem(position).setSelected(!viewHolder.checkBox.isChecked());
//                    adapter.notifyDataSetChanged();
                    switch (position) {
                        case 0:
                            loadSeachData("500", pageSize);
                            tv_distance.setText("0.5km");
                            tv_distance.setTag("500");
                            break;
                        case 1:
                            loadSeachData("1000", pageSize);
                            tv_distance.setText("1km");
                            tv_distance.setTag("1000");
                            break;
                        case 2:
                            loadSeachData("2000", pageSize);
                            tv_distance.setText("2km");
                            tv_distance.setTag("2000");
                            break;
                        case 3:
                            loadSeachData("5000", pageSize);
                            tv_distance.setText("5km");
                            tv_distance.setTag("5000");
                            break;
                        case 4:
                            loadSeachData("10000", pageSize);
                            tv_distance.setText("10km");
                            tv_distance.setTag("10000");
                            break;
                        case 5:
                            loadSeachData("20000", pageSize);
                            tv_distance.setText("20km");
                            tv_distance.setTag("20000");
                            break;
                        case 6:
                            loadSeachData("30000", pageSize);
                            tv_distance.setText("30km");
                            tv_distance.setTag("30000");
                            break;
                        case 7:
                            loadListData(pageSize);
                            tv_distance.setText("全部");
                            tv_distance.setTag("全部");
                            break;
                        default:
                            break;
                    }

                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() / 3, windowManager.getDefaultDisplay().getHeight() / 3);
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(activity, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
    }


    private List<ItemsSelectType1> getItemsSelect() {
        List<ItemsSelectType1> list = new ArrayList<>();
        ItemsSelectType1 model = new ItemsSelectType1();
        model.setName("0.5km");
        list.add(model);

        model = new ItemsSelectType1();
        model.setName("1km");
        list.add(model);

        model = new ItemsSelectType1();
        model.setName("2km");
        list.add(model);


        model = new ItemsSelectType1();
        model.setName("5km");
        list.add(model);


        model = new ItemsSelectType1();
        model.setName("10km");
        list.add(model);

        model = new ItemsSelectType1();
        model.setName("20km");
        list.add(model);

        model = new ItemsSelectType1();
        model.setName("30km");
        list.add(model);

        model = new ItemsSelectType1();
        model.setName("全部");
        list.add(model);

        return list;

    }

    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("item_name", "0.5km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "1km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "2km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "5km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "10km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "20km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "30km");
        list.add(map);
        map = new HashMap<>();
        map.put("item_name", "全部");
        list.add(map);
        return list;
    }

    @Override
    public void onObservableScrollViewListener(int l, int t, int oldl, int oldt) {

    }

    private class BannerImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoaderUtil.getInstance().loadImage(path.toString(), imageView);
        }
    }
}
