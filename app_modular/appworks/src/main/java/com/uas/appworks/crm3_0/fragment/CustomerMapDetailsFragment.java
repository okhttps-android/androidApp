package com.uas.appworks.crm3_0.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.utils.BaiduMapUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.fragment.ViewPagerLazyFragment;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.activity.BillSearchActivity;
import com.uas.appworks.crm3_0.activity.CustomerDetails3_0Activity;
import com.uas.appworks.model.CustomerMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户地图分布
 */
public class CustomerMapDetailsFragment extends ViewPagerLazyFragment {
    private final int PAGE_SIZE = 20;
    private final int LOAD_LIST_DATA = 0x11;
    private String mCaller;
    private String mCondition;
    private int mPageIndex = 1;
    private TextureMapView mMapView;
    private SimpleRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private UUHttpHelper mUUHttpHelper;
    private LatLng mapLatLng = null;
    private boolean isMe;
    public void onItemSelected(MenuItem item) {
        if (R.id.search == item.getItemId()) {
            BillListConfig billConfig = new BillListConfig();
            billConfig.setMe(mCondition != null && mCondition.contains("cu_sellercode="));
            billConfig.setNeedForward(false);
            billConfig.setCondition(mCondition);
            billConfig.setCaller(mCondition);
            billConfig.setTitle(getActivity().getTitle().toString());
            startActivity(new Intent(ct, BillSearchActivity.class)
                    .putExtra(Constants.Intents.CALLER, mCaller)
                    .putExtra(Constants.Intents.DETAILS_CLASS, CustomerDetails3_0Activity.class)
                    .putExtra(Constants.Intents.CONFIG, billConfig)
                    .putExtra(Constants.Intents.CONDITION, mCondition)
                    .putStringArrayListExtra(Constants.Intents.FIELD_CONFIG, fieldConfig)
                    .putExtra(Constants.Intents.TITLE, getActivity().getTitle()));
        }
    }
    public static CustomerMapDetailsFragment newInstance(boolean isMe,String mCaller, String mCondition) {
        Bundle args = new Bundle();
        CustomerMapDetailsFragment fragment = new CustomerMapDetailsFragment();
        args.putString("mCaller", mCaller);
        args.putBoolean("isMe", isMe);
        args.putString("mCondition", mCondition);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.search, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (R.id.search == item.getItemId()) {
//            startActivity(new Intent(ct, BillSearchActivity.class)
//                    .putExtra(Constants.Intents.CALLER, mCaller)
//                    .putExtra(Constants.Intents.MY_DOIT, isMe)
//                    .putExtra(Constants.Intents.DETAILS_CLASS, CustomerDetails3_0Activity.class)
//                    .putExtra(Constants.Intents.CONDITION, mCondition)
//                    .putStringArrayListExtra(Constants.Intents.FIELD_CONFIG, fieldConfig)
//                    .putExtra(Constants.Intents.TITLE, getActivity().getTitle())
//            );
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_customer_map_3_0;
    }

    @Override
    protected void LazyData() {
        Bundle args = getArguments();
        if (args != null) {
            mCondition = args.getString("mCondition");
            mCaller = args.getString("mCaller");
            isMe = args.getBoolean("isMe");
        }
        initView();
    }

    private void initView() {
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(MyApplication.getInstance()));
        mMapView =   findViewById(R.id.mMapView);
        mRefreshLayout = (SimpleRefreshLayout) findViewById(R.id.mRefreshLayout);
        EmptyRecyclerView mEmptyRecyclerView = (EmptyRecyclerView) findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
        mRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                loadListData();
            }

            @Override
            public void onLoadMore() {
                mPageIndex++;
                loadListData();
            }
        });
        loadListData();
    }

    private void loadListData() {
        if (!mRefreshLayout.isRefreshing()) {
            showProgress();
        }
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                .record(LOAD_LIST_DATA)
                .addParams("caller", mCaller)
                .addParams("condition", mCondition)
                .mode(Method.GET)
                .addParams("page", mPageIndex)
                .addParams("pageSize", PAGE_SIZE)
                .url("mobile/common/list.action"), mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject object = JSON.parseObject(message);
            switch (what) {
                case LOAD_LIST_DATA:
                    handlerListData(object);
                    break;
            }
            mRefreshLayout.stopRefresh();
            dismissProgress();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            dismissProgress();
        }
    };
    private ArrayList<String> fieldConfig;

    private void handlerListData(JSONObject object) throws Exception {
        if (fieldConfig == null) {
            fieldConfig = new ArrayList<>();
        } else {
            fieldConfig.clear();
        }
        mapLatLng = null;
        JSONArray columns = JSONUtil.getJSONArray(object, "columns");
        JSONArray listdata = JSONUtil.getJSONArray(object, "listdata");
        String keyField = JSONUtil.getText(object, "keyField");
        List<CustomerMap> mCustomerMaps = null;
        if (!ListUtils.isEmpty(columns) && !ListUtils.isEmpty(listdata)) {
            mCustomerMaps = new ArrayList<>();
            String titleKey = "cu_name";
            String addressKey = "cu_add1";
            for (int j = 0; j < columns.size(); j++) {
                JSONObject column = columns.getJSONObject(j);
                if (JSONUtil.getInt(column, "width") > 0) {
                    String caption = JSONUtil.getText(column, "caption");
                    String dataIndex = JSONUtil.getText(column, "dataIndex");
                    if (!TextUtils.isEmpty(dataIndex)) {
                        fieldConfig.add(dataIndex);
                    }
                    if ("地址".equals(caption)) {
                        addressKey = dataIndex;
                    }
                    if ("客户名称".equals(caption)) {
                        titleKey = dataIndex;
                    }
                }
            }
            for (int i = 0; i < listdata.size(); i++) {
                JSONObject data = listdata.getJSONObject(i);
                float lng = JSONUtil.getFloat(data, "cu_lng");
                float lat = JSONUtil.getFloat(data, "cu_lat");
                int id = JSONUtil.getInt(data, keyField);
                CustomerMap mCustomerMap = new CustomerMap();
                mCustomerMap.setId(id);
                mCustomerMap.setLng(lng);
                mCustomerMap.setLat(lat);
                mCustomerMap.setTitle(JSONUtil.getText(data, titleKey));
                mCustomerMap.setAddress(JSONUtil.getText(data, addressKey));
                mCustomerMap.setJsonObject(data);
                mCustomerMaps.add(mCustomerMap);
                if (mapLatLng == null && lat != 0 && lng != 0) {
                    mapLatLng = new LatLng(lat, lng);
                }
            }
        }
        setAdapter(mCustomerMaps);
    }


    private BillListAdapter mListAdapter;

    private void setAdapter(List<CustomerMap> mCustomerMaps) {
        BaiduMapUtil.getInstence().setMapViewPoint(mMapView, mapLatLng, true);
        if (mListAdapter == null) {
            mListAdapter = new BillListAdapter(mCustomerMaps);
            mRecyclerView.setAdapter(mListAdapter);
        } else {

            if (mPageIndex <= 1) {
                mListAdapter.updateGroupModels(mCustomerMaps);
            } else {
                mListAdapter.addGroupModels(mCustomerMaps);
            }
        }
        if (ListUtils.isEmpty(mCustomerMaps) && mPageIndex > 1) {
            mPageIndex--;
        }
    }


    private class BillListAdapter extends RecyclerView.Adapter<BillListAdapter.ViewHolder> {
        private List<CustomerMap> mCustomerMaps;

        public BillListAdapter(List<CustomerMap> mCustomerMaps) {
            this.mCustomerMaps = mCustomerMaps;
        }

        public void updateGroupModels(List<CustomerMap> mCustomerMaps) {
            this.mCustomerMaps = mCustomerMaps;
            notifyDataSetChanged();
        }

        public void addGroupModels(List<CustomerMap> mCustomerMaps) {
            if (!ListUtils.isEmpty(mCustomerMaps)) {
                if (this.mCustomerMaps != null) {
                    this.mCustomerMaps.addAll(mCustomerMaps);
                } else {
                    this.mCustomerMaps = mCustomerMaps;
                }
                notifyDataSetChanged();
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
            return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_customer_bill_map, viewGroup, false));
        }

        @Override
        public int getItemCount() {
            int mItemCount = ListUtils.getSize(mCustomerMaps);
            return mItemCount;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView doneTv;
            private TextView nameTv;
            private TextView addressTv, lookTv;

            public ViewHolder(View itemView) {
                super(itemView);
                doneTv = (TextView) itemView.findViewById(R.id.doneTv);
                lookTv = (TextView) itemView.findViewById(R.id.lookTv);
                nameTv = (TextView) itemView.findViewById(R.id.nameTv);
                addressTv = (TextView) itemView.findViewById(R.id.addressTv);
            }
        }


        @Override
        public void onBindViewHolder(ViewHolder mViewHolder, int position) {
            CustomerMap mCustomerMap = mCustomerMaps.get(position);
            mViewHolder.nameTv.setText(mCustomerMap.getTitle());
            mViewHolder.addressTv.setText(mCustomerMap.getAddress());
            if (mCustomerMap.getLat() == 0 && mCustomerMap.getLng() == 0) {
                mViewHolder.doneTv.setText("去定位");
                mViewHolder.doneTv.setVisibility(View.VISIBLE);
                mViewHolder.doneTv.setTag(mCustomerMap);
                mViewHolder.doneTv.setOnClickListener(mOnClickListener);
            } else {
                mViewHolder.doneTv.setText("去拜访");
                mViewHolder.doneTv.setVisibility(View.INVISIBLE);
            }
            mViewHolder.itemView.setTag(mCustomerMap);
            mViewHolder.itemView.setOnClickListener(mOnClickListener);
            mViewHolder.lookTv.setTag(mCustomerMap);
            mViewHolder.lookTv.setOnClickListener(mOnClickListener);
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null && view.getTag() instanceof CustomerMap) {
                    CharSequence title = getActivity().getTitle();
                    String titleStr="";
                    if (!TextUtils.isEmpty(title)) {
                        titleStr=title.toString();
                        if (titleStr.contains("列表")){
                            titleStr= titleStr.replace("列表","");
                        }
                    }
                    CustomerMap mCustomerMap = (CustomerMap) view.getTag();
                    if (view.getId() == R.id.doneTv) {
                        batchDealCusotmerLngLat(mCustomerMap.getId());
                    } else if (R.id.lookTv == view.getId()) {
                        startActivity(new Intent(ct, CustomerDetails3_0Activity.class)
                                .putExtra(Constants.Intents.CALLER, mCaller)
                                .putExtra(Constants.Intents.MY_DOIT, isMe)
                                .putExtra(Constants.Intents.TITLE, titleStr)
                                .putExtra(Constants.Intents.ID, mCustomerMap.getId()));
                    } else {
                        if (mCustomerMap.getLat() != 0 || mCustomerMap.getLng() != 0) {
                            LatLng mLatLng = new LatLng(mCustomerMap.getLat(), mCustomerMap.getLng());
                            BaiduMapUtil.getInstence().setMapViewPoint(mMapView, mLatLng, true);
                        }
                    }
                }
            }
        };
    }

    private void batchDealCusotmerLngLat(int keyValue) {
        boolean isGoodCus = !mCaller.equals("PreCustomer");
        String condition = isGoodCus ? ("cu_id=" + keyValue + "") : "1=2";
        String precondition = isGoodCus ? "1=2" : ("cu_id=" + keyValue + "");
        UUHttpHelper mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/batchDealCusotmerLngLat.action")
                        .addParams("condition", condition)
                        .addParams("precondition", precondition)
                , new OnSmartHttpListener() {
                    @Override
                    public void onSuccess(int what, String message, Tags tag) throws Exception {
                        ToastUtil.showToast(ct, "更新成功，请重新刷新列表");
                    }

                    @Override
                    public void onFailure(int what, String message, Tags tag) throws Exception {
                        ToastUtil.showToast(ct, "更新失败");
                    }
                });

    }
}
