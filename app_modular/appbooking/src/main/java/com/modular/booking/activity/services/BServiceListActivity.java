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
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemBserviceListAdapter;
import com.modular.booking.model.SBListModel;
import com.modular.booking.model.SBMenuModel;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.utils.LocationDistanceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc:
 * @author：Arison on 2017/9/28
 */
public class BServiceListActivity extends OABaseActivity {
    private VoiceSearchView voiceSearchView;
    private PullToRefreshListView mListView;
    private ItemBserviceListAdapter mListAdapter;
    private List<SBListModel> modelList = new ArrayList<>();
    private SBMenuModel menuModel;
    private EmptyLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bservices);

        initView();
        initEvent();
        initData();
    }


    private void initView() {
        setTitle("");
        if (getIntent() != null) {
            menuModel = getIntent().getParcelableExtra("SBMenuModel");
//            setTitle(menuModel.getTitle());
            LogUtil.d("myTest", "menuModel:" + JSON.toJSONString(menuModel));
        }
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mListView = (PullToRefreshListView) findViewById(R.id.mListView);
        emptyLayout = new EmptyLayout(mContext, mListView.getRefreshableView());
        mListAdapter = new ItemBserviceListAdapter(mContext, modelList);
        mListView.setAdapter(mListAdapter);

        initSearchView();
    }

    private ImageView mBackImageView, mVoiceImageView;
    private ClearEditText mSearchEditText;
    private EmptyLayout mEmptyLayout;
    @Override
    public boolean needNavigation() {
        return false;
    }
    private void initSearchView() {
        View view = LayoutInflater.from(ct).inflate(R.layout.action_book_service_search, null);
        mBackImageView = (ImageView) view.findViewById(R.id.book_service_search_back);
        mVoiceImageView = (ImageView) view.findViewById(R.id.book_service_search_voice_iv);
        mSearchEditText = (ClearEditText) view.findViewById(R.id.book_service_search_et);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(view);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mVoiceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.getVoiceText(ct, mSearchEditText, new OnVoiceCompleteListener() {
                    @Override
                    public void onVoiceComplete(String text) {
//                        ToastUtil.showToast(BServiceListActivity.this, "语音输入完毕");
                    }
                });
            }
        });

        mEmptyLayout = new EmptyLayout(this, mListView.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (!ListUtils.isEmpty(modelList)) {
                        List<SBListModel> tempList = new ArrayList<>();
                        for (int i = 0; i < modelList.size(); i++) {
                            if (modelList.get(i).getName().contains(s.toString())) {
                                tempList.add(modelList.get(i));
                            }
                        }
                        if (ListUtils.isEmpty(tempList)) mEmptyLayout.showEmpty();
                        LogUtil.d(TAG, "tempList:" + JSON.toJSONString(tempList.toString()));
                        mListAdapter.setObjects(tempList);
                        mListAdapter.notifyDataSetChanged();

                    }
                } else {
                    mListAdapter.setObjects(modelList);
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i("position=" + position);
                LogUtil.i("id=" + id);
                SBListModel model = mListAdapter.getItem((int) id);
                Intent intent = null;
                if (model.getType().equals("会所") || model.getType().equals("美容美发")) {
                    intent = new Intent(ct, BServiceDetailActivity.class);
                } else {
                    intent = new Intent(ct, BServiceAddActivity.class);
                }
                LogUtil.d(TAG, "model:" + JSON.toJSONString(model));
                intent.putExtra("model", model);
                startActivity(intent);
                //@update by Bitliker 由于不同类型的预约详情界面差别很大，需要判断当前点击的进行分类
            }
        });
    }

    private void initData() {
        if (menuModel != null) {
            loadListData(menuModel.getCode(), 1);
        }
    }


    public void loadListData(String type, int page) {
        UasLocationHelper.getInstance().requestLocation();
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appStoreList")
                .add("type", type)
                .add("pageIndex", page)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                modelList.clear();
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
                            sbListModel.setPhone(mObject.getString("sc_telephone"));
                            sbListModel.setIndustrycode(mObject.getString("sc_industrycode"));
                            String sc_latitude = mObject.getString("sc_latitude");
                            String sc_longitude = mObject.getString("sc_longitude");
                            sbListModel.setLatitude(sc_latitude);
                            sbListModel.setLongitude(sc_longitude);
                            sbListModel.setIntroduce(mObject.getString("sc_introduce"));
                            LatLng latLng = new LatLng(Double.valueOf(sc_latitude), Double.valueOf(sc_longitude));
                            float distance = LocationDistanceUtils.distanceMe(latLng);
                            sbListModel.setDistance(String.valueOf(DistanceUtils.mToKm(distance, "#.0")));
                            sbListModel.setCash(mObject.getString("sc_telephone"));
                            sbListModel.setCompanyid(mObject.getString("sc_uu"));
                            sbListModel.setImid(mObject.getString("sc_imid"));
                            sbListModel.setId(mObject.getInteger("sc_id"));
                            sbListModel.setStarttime(mObject.getString("sc_starttime"));
                            sbListModel.setEndtime(mObject.getString("sc_endtime"));
                            modelList.add(sbListModel);
                        }
                        LogUtil.d("myTest", JSON.toJSONString(modelList));
                        mListAdapter = new ItemBserviceListAdapter(mContext, modelList);
                        mListView.setAdapter(mListAdapter);
                        if (mListAdapter.getCount() == 0) {
                            emptyLayout.showEmpty();
                        }
                        //mListAdapter.notifyDataSetChanged();
                    } else {
                        if (mListAdapter.getCount() == 0) {
                            emptyLayout.showEmpty();
                        }
                    }
                }
            }
        }));
    }
}
