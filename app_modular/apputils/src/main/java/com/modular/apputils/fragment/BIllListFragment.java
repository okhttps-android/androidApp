package com.modular.apputils.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.R;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.adapter.BillListAdapter;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillConfig;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.model.BillListGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BIllListFragment extends ViewPagerLazyFragment {
    private final int PAGE_SIZE = 20;
    private final int LOAD_LIST_DATA = 0x11;

    private BillListConfig billConfig;

    private int mPageIndex = 1;
    private RecyclerView mRecyclerView;
    private Class mDetailsClass;
    private UUHttpHelper mUUHttpHelper;
    private SimpleRefreshLayout mRefreshLayout;


    public static BIllListFragment newInstance(BillListConfig billConfig, Class mDetailsClass) {
        Bundle args = new Bundle();
        BIllListFragment fragment = new BIllListFragment();
        args.putParcelable("billConfig", billConfig);
        args.putSerializable("mDetailsClass", mDetailsClass);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.search == item.getItemId()) {
            startActivity(new Intent("com.modular.work.crm3_0.activity.BillSearchActivity")
                    .putExtra(Constants.Intents.CALLER, billConfig.getCaller())
                    .putExtra(Constants.Intents.DETAILS_CLASS, mDetailsClass)
                    .putExtra(Constants.Intents.MY_DOIT, billConfig.isMe())
                    .putExtra(Constants.Intents.CONDITION, billConfig.getCondition())
                    .putStringArrayListExtra(Constants.Intents.FIELD_CONFIG, fieldConfig)
                    .putExtra(Constants.Intents.CONFIG, billConfig)
                    .putExtra(Constants.Intents.TITLE, getActivity().getTitle())
            );
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected int inflateLayoutId() {
        return R.layout.common_refreshlayout_recycler;
    }

    @Override
    protected void LazyData() {
        setHasOptionsMenu(true);
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(MyApplication.getInstance()));
        initView();
    }

    private void initView() {
        Bundle args = getArguments();
        if (args != null) {
            billConfig = args.getParcelable("billConfig");
             Serializable mSerializable= args.getSerializable("mDetailsClass");
             if (mSerializable!=null&& mSerializable instanceof Class){
                 mDetailsClass= (Class) mSerializable;
             }
        }
        findViewById(R.id.mSearchView).setVisibility(View.GONE);
        mRefreshLayout = findViewById(R.id.mRefreshLayout);
        EmptyRecyclerView mEmptyRecyclerView = findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
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
                .addParams("caller", billConfig.getCaller())
                .addParams("condition", billConfig.getCondition())
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
            LogUtil.i("gong", "onFailure=" + message);
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
        JSONArray columns = JSONUtil.getJSONArray(object, "columns");
        JSONArray listdata = JSONUtil.getJSONArray(object, "listdata");
        String keyField = JSONUtil.getText(object, "keyField");
        String pfField = JSONUtil.getText(object, "pfField");
        List<BillListGroupModel> groupModels = null;
        if (!ListUtils.isEmpty(columns) && !ListUtils.isEmpty(listdata)) {
            groupModels = new ArrayList<>();
            int index = -1;
            for (int i = 0; i < listdata.size(); i++) {
                List<BillListGroupModel.BillListField> billListFields = null;
                List<BillListGroupModel.BillListField> hideBillListFields = null;
                BillListGroupModel groupModel = new BillListGroupModel();
                JSONObject data = listdata.getJSONObject(i);
                int id = JSONUtil.getInt(data, keyField);
                String status = JSONUtil.getText(data, "cu_auditstatus");
                int showNum = 0;
                for (int j = 0; j < columns.size(); j++) {
                    JSONObject column = columns.getJSONObject(j);
                    if (billListFields == null) {
                        billListFields = new ArrayList<>();
                        index++;
                    }
                    String caption = JSONUtil.getText(column, "caption");
                    String dataIndex = JSONUtil.getText(column, "dataIndex");
                    String values = JSONUtil.getText(data, dataIndex);
                    if (!TextUtils.isEmpty(dataIndex) && i == 0) {
                        fieldConfig.add(dataIndex);
                    }
                    if ("状态".equals(caption)||"单据状态".equals(caption)){
                        status= values;
                    }
                    BillListGroupModel.BillListField billListField = new BillListGroupModel.BillListField();
                    billListField.setCaption(caption);
                    billListField.setField(dataIndex);
                    billListField.setValue(values);
                    billListField.setGroupIndex(index);
                    groupModel.setGroupIndex(index);
                    if (JSONUtil.getInt(column, "width") > 0 && (billConfig.getShowItemNum() <= 0 || billConfig.getShowItemNum() >showNum)) {
                        billListFields.add(billListField);
                        showNum++;
                    } else {
                        if (hideBillListFields==null){
                            hideBillListFields=new ArrayList<>();
                        }
                        hideBillListFields.add(billListField);
                    }

                }
                groupModel.setId(id);
                groupModel.setStatus(status);
                if (!ListUtils.isEmpty(billListFields)) {
                    groupModel.setBillFields(billListFields);
                    if (!ListUtils.isEmpty(hideBillListFields)) {
                        groupModel.setHideBillFields(hideBillListFields);
                    }
                    groupModels.add(groupModel);
                }
            }
        }
        setAdapter(groupModels);
    }


    private BillListAdapter mListAdapter;

    private void setAdapter(List<BillListGroupModel> groupModels) {
        if (mListAdapter == null) {
            mListAdapter = new BillListAdapter(ct, groupModels, new BillListAdapter.OnAdapterListener() {
                @Override
                public void onClick(BillListGroupModel mBillListGroupModel) {
                    if (mOnBillListListener != null) {
                        mOnBillListListener.itemClick(billConfig, mBillListGroupModel.getId(), mBillListGroupModel);
                    }
                }
            });
            mRecyclerView.setAdapter(mListAdapter);
        } else {
            List<BillListGroupModel> showModels = mListAdapter.getGroupModels();
            if (mPageIndex <= 1) {
                showModels = groupModels;
            } else {
                if (showModels == null) {
                    showModels = new ArrayList<>();
                }
                if (!ListUtils.isEmpty(groupModels)) {
                    showModels.addAll(groupModels);
                }
            }
            mListAdapter.updateGroupModels(showModels);
        }
        if (ListUtils.isEmpty(groupModels) && mPageIndex > 1) {
            mPageIndex--;

        }
    }


    private OnBillListListener mOnBillListListener;

    public BIllListFragment setOnBillListListener(OnBillListListener mOnBillListListener) {
        this.mOnBillListListener = mOnBillListListener;
        return this;
    }

    public interface OnBillListListener {
        void itemClick(BillListConfig billConfig, int formId, BillListGroupModel mBillListGroupModel);
    }


}

