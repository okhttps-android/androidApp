package com.uas.appworks.crm3_0.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.StatusBarUtil;
import com.core.widget.listener.EditChangeListener;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BillInputActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.model.BillListGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.modular.apputils.widget.MenuVoiceSearchView;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;
import com.uas.appworks.R;
import com.modular.apputils.adapter.BillListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BillSearchActivity extends OABaseActivity {
    private final int TAG_KEY_WORD = 0x12;
    private final int LOAD_LIST_DATA = 0x11;
    private final int PAGE_SIZE = 20;

    private SimpleRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private UUHttpHelper mUUHttpHelper;
    private int mPageIndex = 1;
    private String mCaller;
    private String mMustCondition;
    private ArrayList<String> fieldConfig;
    private String lastKeyWord;
    private boolean isMe;
    private Class mDetailsActivity;
    private BillListConfig billConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_search);
        initView();
    }

    @Override
    public int getToolBarId() {
        return R.id.mToolbar;
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            isMe = intent.getBooleanExtra(Constants.Intents.MY_DOIT, false);
            mCaller = intent.getStringExtra(Constants.Intents.CALLER);
            mMustCondition = intent.getStringExtra(Constants.Intents.CONDITION);
            billConfig = intent.getParcelableExtra(Constants.Intents.CONFIG);
            fieldConfig = intent.getStringArrayListExtra(Constants.Intents.FIELD_CONFIG);
            Serializable mSerializable = intent.getSerializableExtra(Constants.Intents.DETAILS_CLASS);
            if (mSerializable != null && mSerializable instanceof Class) {
                mDetailsActivity = (Class) mSerializable;
            }
            String mTitle = intent.getStringExtra(Constants.Intents.TITLE);
            if (mTitle != null) {
                setTitle(mTitle);
            }
        }
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(MyApplication.getInstance()));
        final MenuVoiceSearchView mVoiceSearchView = (MenuVoiceSearchView) findViewById(R.id.mVoiceSearchView);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        StatusBarUtil.setPaddingSmart(this, mToolbar);
        mRefreshLayout = (SimpleRefreshLayout) findViewById(R.id.mSimpleRefreshLayout);
        EmptyRecyclerView mEmptyRecyclerView = (EmptyRecyclerView) findViewById(R.id.mEmptyRecyclerView);
        findViewById(R.id.backImg).setOnClickListener(mOnClickListener);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(TextUtils.isEmpty(mVoiceSearchView.getText()) ? "" : mVoiceSearchView.getText().toString());
            }

            @Override
            public void onLoadMore() {
                mPageIndex++;
                loadData(TextUtils.isEmpty(mVoiceSearchView.getText()) ? "" : mVoiceSearchView.getText().toString());
            }
        });
        mVoiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                mPageIndex = 1;
                loadData(TextUtils.isEmpty(editable) ? "" : editable.toString());
            }
        });
        showLoading();
        loadData(null);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.backImg) {
                onBackPressed();
            }
        }
    };


    private void loadData(String keyWord) {
        if (keyWord == null) {
            keyWord = "";
        }
        this.lastKeyWord = keyWord;
        StringBuilder builder = new StringBuilder(mMustCondition);
        if (!TextUtils.isEmpty(keyWord) && !ListUtils.isEmpty(fieldConfig)) {
            builder.append(" and ( ");
            for (String e : fieldConfig) {
                builder.append("upper(" + e + ") like '%" + keyWord.toUpperCase() + "%' or ");
            }
            if (builder.length() > 3) {
                builder.delete(builder.length() - 3, builder.length() - 1);
            }
            builder.append(" )");
        }
        LogUtil.i("gong", "builder=" + builder.toString());
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                .record(LOAD_LIST_DATA)
                .addParams("caller", mCaller)
                .addParams("condition", builder.toString())
                .mode(Method.GET)
                .addTag(TAG_KEY_WORD, keyWord)
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
                    if (tag.get(TAG_KEY_WORD) != null && tag.get(TAG_KEY_WORD) instanceof String) {
                        String keyWord = (String) tag.get(TAG_KEY_WORD);
                        if (keyWord.equals(lastKeyWord)) {
                            handlerListData(object);
                        }
                    }
                    break;
            }
            mRefreshLayout.stopRefresh();
            dimssLoading();
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            LogUtil.i("gong", "onFailure=" + message);
            dimssLoading();
        }
    };

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
                    if ("状态".equals(caption)){
                        status= values;
                    }
                    BillListGroupModel.BillListField billListField = new BillListGroupModel.BillListField();
                    billListField.setCaption(caption);
                    billListField.setField(dataIndex);
                    billListField.setValue(values);
                    billListField.setGroupIndex(index);
                    groupModel.setGroupIndex(index);
                    if (JSONUtil.getInt(column, "width") > 0 && (billConfig==null||billConfig.getShowItemNum() <= 0 || billConfig.getShowItemNum() >showNum)) {
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
                    if (mBillListGroupModel.getStatus().equals("在录入")) {
                        startActivity(new Intent(ct, CustomerBillInputActivity.class)
                                .putExtra(Constants.Intents.CALLER, mCaller)
                                .putExtra(Constants.Intents.TITLE, getTitle())
                                .putExtra(Constants.Intents.ID, mBillListGroupModel.getId()));
                    } else if (mDetailsActivity != null) {
                        ArrayList<BillListGroupModel.BillListField> fields = new ArrayList<>();
                        if (billConfig!=null&&billConfig.isNeedForward()) {
                            if (!ListUtils.isEmpty(mBillListGroupModel.getBillFields())) {
                                fields.addAll(mBillListGroupModel.getBillFields());
                            }
                            if (!ListUtils.isEmpty(mBillListGroupModel.getHideBillFields())) {
                                fields.addAll(mBillListGroupModel.getHideBillFields());
                            }
                        }
                        startActivity(new Intent(ct, mDetailsActivity)
                                .putExtra(Constants.Intents.CALLER, billConfig.getCaller())
                                .putExtra(Constants.Intents.TITLE, getTitle())
                                .putExtra(Constants.Intents.MY_DOIT, billConfig.isMe())
                                .putExtra(Constants.Intents.INPUT_CLASS, BillInputActivity.class)
                                .putExtra(Constants.Intents.BILL_LIST_FIELD_FORWARD, fields)
                                .putExtra(Constants.Intents.ID, mBillListGroupModel.getId()));
                    } else {
                        //进入通用详情界面
                        startActivity(new Intent("com.modular.form.erp.activity.CommonDocDetailsActivity")
                                .putExtra("caller", billConfig.getCaller())
                                .putExtra("keyValue", mBillListGroupModel.getId())
                                .putExtra("update", "1")
                                .putExtra(Constants.Intents.MY_DOIT, billConfig.isMe())
                                .putExtra("title", getTitle())
                                .putExtra("statusKey", "")
                                .putExtra("status", "已提交"));
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
