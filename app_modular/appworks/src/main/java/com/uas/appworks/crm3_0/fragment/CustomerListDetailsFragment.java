package com.uas.appworks.crm3_0.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseToolBarActivity;
import com.core.utils.CommonUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.fragment.ViewPagerLazyFragment;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.model.BillListGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;
import com.uas.appworks.R;
import com.modular.apputils.adapter.BillListAdapter;
import com.uas.appworks.crm3_0.activity.BillSearchActivity;
import com.uas.appworks.crm3_0.activity.CustomerBillInputActivity;
import com.uas.appworks.crm3_0.activity.CustomerDetails3_0Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户列表中的列表界面
 */
public class CustomerListDetailsFragment extends ViewPagerLazyFragment {
    private final int PAGE_SIZE = 20;
    private final int LOAD_LIST_DATA = 0x11;
    private boolean isMe;
    private String mCaller;
    private String mCondition;
    private int mPageIndex = 1;
    private RecyclerView mRecyclerView;


    private UUHttpHelper mUUHttpHelper;
    private SimpleRefreshLayout mRefreshLayout;


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
                    .putExtra(Constants.Intents.TITLE, getActivity().getTitle())
            );
        }
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
//                    .putExtra(Constants.Intents.DETAILS_CLASS, CustomerDetails3_0Activity.class)
//                    .putExtra(Constants.Intents.MY_DOIT, mCondition != null && mCondition.contains("cu_sellercode="))
//                    .putExtra(Constants.Intents.CONDITION, mCondition)
//                    .putStringArrayListExtra(Constants.Intents.FIELD_CONFIG, fieldConfig)
//                    .putExtra(Constants.Intents.TITLE, getActivity().getTitle())
//            );
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public static CustomerListDetailsFragment newInstance(boolean isMe, String mCaller, String mCondition) {
        Bundle args = new Bundle();
        CustomerListDetailsFragment fragment = new CustomerListDetailsFragment();
        args.putString("mCaller", mCaller);
        args.putBoolean("isMe", isMe);
        args.putString("mCondition", mCondition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_customer_list_3_0;
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
            mCondition = args.getString("mCondition");
            mCaller = args.getString("mCaller");
            isMe = args.getBoolean("isMe");
        }

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
                BillListGroupModel groupModel = new BillListGroupModel();
                JSONObject data = listdata.getJSONObject(i);
                int id = JSONUtil.getInt(data, keyField);
                String status = JSONUtil.getText(data, "cu_auditstatus");

                for (int j = 0; j < columns.size(); j++) {
                    JSONObject column = columns.getJSONObject(j);
                    if (JSONUtil.getInt(column, "width") > 0) {
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

                        BillListGroupModel.BillListField billListField = new BillListGroupModel.BillListField();
                        if ("单据状态".equals(caption)) {
                            status = values;
                        }
                        billListField.setCaption(caption);
                        billListField.setField(dataIndex);
                        billListField.setValue(values);
                        billListField.setGroupIndex(index);
                        groupModel.setGroupIndex(index);
                        billListFields.add(billListField);
                    }
                }
                groupModel.setId(id);
                groupModel.setStatus(status);
                if (!ListUtils.isEmpty(billListFields)) {
                    groupModel.setBillFields(billListFields);
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
                    CharSequence title = getActivity().getTitle();
                    String titleStr = "";
                    if (!TextUtils.isEmpty(title)) {
                        titleStr = title.toString();
                        if (titleStr.contains("列表")) {
                            titleStr = titleStr.replace("列表", "");
                        }
                    }
                    if (mBillListGroupModel.getStatus().equals("在录入")) {
                        startActivity(new Intent(ct, CustomerBillInputActivity.class)
                                .putExtra(Constants.Intents.CALLER, mCaller)
                                .putExtra(Constants.Intents.TITLE, titleStr)
                                .putExtra(Constants.Intents.MY_DOIT, isMe)
                                .putExtra(Constants.Intents.ID, mBillListGroupModel.getId()));
                    } else {
                        startActivity(new Intent(ct, CustomerDetails3_0Activity.class)
                                .putExtra(Constants.Intents.CALLER, mCaller)
                                .putExtra(Constants.Intents.TITLE, titleStr)
                                .putExtra(Constants.Intents.MY_DOIT, isMe)
                                .putExtra(Constants.Intents.ID, mBillListGroupModel.getId()));
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


}
