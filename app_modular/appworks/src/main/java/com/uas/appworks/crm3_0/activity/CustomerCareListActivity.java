package com.uas.appworks.crm3_0.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.base.OABaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.EasyBaseModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CustomerCareListAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomerCareListActivity extends OABaseActivity {
    private final int LOAD_CUSTOMER_CARE = 0x11;
    private final int LOAD_FORGET_CUSTOMER = 0x12;
    private String ME_EMCODE;
    private int mPageIndex = 1;
    private final int PAGE_SIZE = 100;
    private int type;
    private UUHttpHelper mUUHttpHelper;
    private PullToRefreshListView mListView;
    private EmptyLayout mEmptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_refresh_listview);
        this.ME_EMCODE = CommonUtil.getEmcode();
        initView();
    }

    private void initView() {
        if (getIntent() != null) {
            type = getIntent().getIntExtra(Constants.Intents.TYPE, 1);
            String mTitle = getIntent().getStringExtra(Constants.Intents.TITLE);
            if (mTitle != null) {
                setTitle(mTitle);
            }
        }
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        findViewById(R.id.mSearchView).setVisibility(View.GONE);
        mListView = findViewById(R.id.mListView);
        mEmptyLayout = new EmptyLayout(this, mListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPageIndex = 1;
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPageIndex++;
                loadData();
            }
        });
        loadData();
    }

    private void loadData() {
        if (type == 1) {
            loadForgetCustomer();
        } else {
            loadCustomerCare();
        }

    }

    private void loadCustomerCare() {
        showLoading();
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerCare.action")
                        .addParams("pageIndex", mPageIndex)
                        .addParams("pageSize", PAGE_SIZE)
                        .addParams("salesmanCode", ME_EMCODE)
                        .record(LOAD_CUSTOMER_CARE)
                , mOnSmartHttpListener);
    }

    //遗忘客户
    public void loadForgetCustomer() {
        showLoading();
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/crm/customerForget.action")
                        .addParams("pageIndex", mPageIndex)
                        .addParams("pageSize", PAGE_SIZE)
                        .addParams("salesmanCode", ME_EMCODE)
                        .record(LOAD_FORGET_CUSTOMER)
                , mOnSmartHttpListener);
    }


    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject jsonObject = JSON.parseObject(message);
            LogUtil.i("gong", what + "||onSuccess||" + message);
            switch (what) {
                case LOAD_FORGET_CUSTOMER://遗忘客户
                    handlerForgetCustomer(JSONUtil.getJSONArray(jsonObject, "datas"));
                    break;
                case LOAD_CUSTOMER_CARE://客户关怀handlerForgetCare
                    handlerForgetCare(JSONUtil.getJSONArray(jsonObject, "datas"));
                    break;

            }
            if (mListView != null && mListView.isRefreshing()) {
                mListView.onRefreshComplete();
            }
            dimssLoading();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            LogUtil.i("gong", what + "||onFailure||" + message);
            if (JSONUtil.validateJSONObject(message)) {
                showToast(JSONUtil.getText(message, "exceptionInfo"));
            } else {
                showToast(message);
            }
            if (mListView != null && mListView.isRefreshing()) {
                mListView.onRefreshComplete();
            }
            dimssLoading();

        }
    };

    private void handlerForgetCustomer(JSONArray array) {
        List<EasyBaseModel> models = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = JSONUtil.getInt(object, "id");
                String name = JSONUtil.getText(object, "name");//客户名称
                String fpTime = DateFormatUtil.long2Str(JSONUtil.getTime(object, "fpTime"), DateFormatUtil.YMD_HMS);//跟进时间
                String state = JSONUtil.getText(object, "state");//状态
                models.add(new EasyBaseModel().setId(id).setTitle(name).setSubTitle("最后跟进时间：" + fpTime).setIconUrl(state));
            }
        }
        setForgetCustomerAdapter(models);
    }

    private void handlerForgetCare(JSONArray array) {
        List<EasyBaseModel> models = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = JSONUtil.getInt(object, "id");
                String name = JSONUtil.getText(object, "name");//客户名称
                String birthday = DateFormatUtil.long2Str(JSONUtil.getTime(object, "birthday"), DateFormatUtil.YMD_HMS);//跟进时间
                String state = JSONUtil.getText(object, "state");//状态
                models.add(new EasyBaseModel().setId(id).setTitle(name).setSubTitle(birthday).setIconUrl(state));
            }
        }
        setCustomerCareAdapter(models);
    }


    private CustomerCareListAdapter mListAdapter;

    public void setCustomerCareAdapter(List<EasyBaseModel> models) {
        if (mListAdapter == null) {
            mListAdapter = new CustomerCareListAdapter(ct, models, 2);
            mListView.setAdapter(mListAdapter);
        } else {
            List<EasyBaseModel> showModels = null;
            if (mPageIndex == 1) {
                showModels = models;
            } else if (!ListUtils.isEmpty(models)) {
                showModels = new ArrayList<>();
                showModels.addAll(models);
            }
            mListAdapter.updateModels(showModels);
        }
        if (ListUtils.isEmpty(models)) {
            mEmptyLayout.showEmpty();
        }
    }

    public void setForgetCustomerAdapter(List<EasyBaseModel> models) {
        if (mListAdapter == null) {
            mListAdapter = new CustomerCareListAdapter(ct, models, 1);
            mListView.setAdapter(mListAdapter);
        } else {
            List<EasyBaseModel> showModels = null;
            if (mPageIndex == 1) {
                showModels = models;
            } else if (!ListUtils.isEmpty(models)) {
                showModels = new ArrayList<>();
                showModels.addAll(models);
            }
            mListAdapter.updateModels(showModels);
        }
        if (ListUtils.isEmpty(models)) {
            mEmptyLayout.showEmpty();
        }
    }

}
