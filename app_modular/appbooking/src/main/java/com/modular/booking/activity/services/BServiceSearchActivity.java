package com.modular.booking.activity.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.interfac.OnVoiceCompleteListener;
import com.core.utils.CommonUtil;
import com.core.utils.DistanceUtils;
import com.core.widget.ClearEditText;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemBserviceListAdapter;
import com.modular.booking.model.SBListModel;
import com.uas.applocation.utils.LocationDistanceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/30.
 * 服务服务预约搜索页面
 */

public class BServiceSearchActivity extends OABaseActivity implements View.OnClickListener {
    private ImageView mBackImageView, mVoiceImageView;
    private ClearEditText mSearchEditText;
    private PullToRefreshListView mSearchListView;
    private ItemBserviceListAdapter mBserviceListAdapter;
    private List<SBListModel> mBServiceList;
    private EmptyLayout mEmptyLayout;
    private int mPageIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_service_search);

        initViews();
        initEvents();
        loadListData(mPageIndex);
    }

    private void initEvents() {
        mBackImageView.setOnClickListener(this);
        mVoiceImageView.setOnClickListener(this);
        
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s.toString())){
                if (!ListUtils.isEmpty(mBServiceList)){
                    List<SBListModel> tempList=new ArrayList<>();
                    for (int i = 0; i <mBServiceList.size() ; i++) {
                        if (mBServiceList.get(i).getName().contains(s.toString())){
                           tempList.add(mBServiceList.get(i));
                        }
                    }
                    if (ListUtils.isEmpty(tempList)) mEmptyLayout.showEmpty();
                    LogUtil.d(TAG,"tempList:"+JSON.toJSONString(tempList.toString()));
                    mBserviceListAdapter.setObjects(tempList);
                    mBserviceListAdapter.notifyDataSetChanged();
                }
            }else{
                mBserviceListAdapter.setObjects(mBServiceList);
                mBserviceListAdapter.notifyDataSetChanged();
            }
            }
        });
        mSearchListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPageIndex++;
                loadListData(mPageIndex);
            }
        });
        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SBListModel model = mBserviceListAdapter.getItem((int) id);
                Intent intent =null;
                if (model.getType().equals("会所")||model.getType().equals("美容美发")){
                    intent = new Intent(ct, BServiceDetailActivity.class);
                }else{
                    intent = new Intent(ct, BServiceAddActivity.class);
                }
                intent.putExtra("model", model);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    private void initViews() {
        setTitle("");
        View view = LayoutInflater.from(ct).inflate(R.layout.action_book_service_search, null);
        mBackImageView = (ImageView) view.findViewById(R.id.book_service_search_back);
        mVoiceImageView = (ImageView) view.findViewById(R.id.book_service_search_voice_iv);
        mSearchEditText = (ClearEditText) view.findViewById(R.id.book_service_search_et);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(view);

        mBServiceList = new ArrayList<>();
        mBserviceListAdapter = new ItemBserviceListAdapter(this, mBServiceList);
        mSearchListView =  findViewById(R.id.book_service_search_ptlv);
        mSearchListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mSearchListView.setAdapter(mBserviceListAdapter);

        mEmptyLayout = new EmptyLayout(this, mSearchListView.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
    }

    public void loadListData(final int pageIndex) {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appStoreList")
                .add("type", "0")
                .add("pageIndex", pageIndex + "")
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (mSearchListView.isRefreshing())
                        mSearchListView.onRefreshComplete();
                    LogUtil.d("o.toString" + o.toString());
                    if (JSONUtil.validate(o.toString())) {
                        if (pageIndex == 1)
                            mBServiceList.clear();
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
                                sbListModel.setPhone(mObject.getString("sc_telephone"));
                                sbListModel.setIndustrycode(mObject.getString("sc_industrycode"));
                                sbListModel.setCompanyid(mObject.getString("sc_uu"));
                                String sc_latitude=mObject.getString("sc_latitude");
                                String sc_longitude=mObject.getString("sc_longitude");
                                sbListModel.setLatitude(sc_latitude);
                                sbListModel.setLongitude(sc_longitude);
                                sbListModel.setIntroduce(mObject.getString("sc_introduce"));
                                LatLng latLng = new LatLng(Double.valueOf(sc_latitude), Double.valueOf(sc_longitude));
                                float distance = LocationDistanceUtils.distanceMe(latLng);
                                sbListModel.setDistance(String.valueOf(DistanceUtils.mToKm(distance,"#.0")));
                                sbListModel.setCash(mObject.getString("sc_telephone"));
                                sbListModel.setImid(mObject.getString("sc_imid"));
                                sbListModel.setId(mObject.getInteger("sc_id"));
                                sbListModel.setStarttime(mObject.getString("sc_starttime"));
                                sbListModel.setEndtime(mObject.getString("sc_endtime"));
                                mBServiceList.add(sbListModel);
                            }
                        } else {
                            if (mPageIndex > 1)
                                mPageIndex--;
                        }
                        LogUtil.d("mBServiceList","mBServiceList.size:"+mBServiceList.size());
                        mBserviceListAdapter.setObjects(mBServiceList);
                        mBserviceListAdapter.notifyDataSetChanged();
                        if (mBServiceList.size() == 0) {
                            mEmptyLayout.showEmpty();
                        }
                    }
                } catch (Exception e) {
                    LogUtil.d("o.toString 发生异常" + e.toString());
                    e.printStackTrace();
                }
            }
        }));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.book_service_search_back) {
            onBackPressed();
        } else if (id == R.id.book_service_search_voice_iv) {
            CommonUtil.getVoiceText(this, mSearchEditText, new OnVoiceCompleteListener() {
                @Override
                public void onVoiceComplete(String text) {
//                    ToastUtil.showToast(BServiceSearchActivity.this, "语音输入完毕");
                }
            });
        }
    }
}
