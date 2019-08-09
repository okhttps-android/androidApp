package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.widget.MenuVoiceSearchView;
import com.uas.appworks.CRM.erp.adapter.CycleCountAdapter;
import com.uas.appworks.CRM.erp.model.CycleCount;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/11/22.
 */

public class DeviceCycleCountActivity extends SupportToolBarActivity implements View.OnClickListener {
    private HttpClient httpClient = null;
    private List<CycleCount> allCycleCount = new ArrayList<>();
    private PullToRefreshListView mRefreshListView;
    private MenuVoiceSearchView mVoiceSearchView;
    private int pageIndex;
    private EmptyLayout mEmptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_device_cycle;
    }

    private void initView() {
        initActionbar();
        mRefreshListView = findViewById(R.id.mRefreshListView);
        mRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mEmptyLayout = new EmptyLayout(this, mRefreshListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");
        mRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData(pageIndex = 1);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData(++pageIndex);
            }
        });
    }

    @Override
    public int getToolBarId() {
        return R.id.cycleCountToolBar;
    }

    private void initActionbar() {
        findViewById(R.id.addImg).setOnClickListener(this);
        findViewById(R.id.backImg).setOnClickListener(this);
        mVoiceSearchView = findViewById(R.id.mVoiceSearchView);
        mVoiceSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                loadData(pageIndex = 1);
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backImg) {
            onBackPressed();
        } else if (R.id.addImg == id) {
            startActivityForResult(new Intent(ct, DeviceCycleCountAddActivity.class), 0x12);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (0x12 == requestCode && 0x12 == resultCode) {
            loadData(pageIndex = 1);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            ToastUtil.showToast(ct, R.string.data_exception);
            finish();
        } else {
            String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
            String emCode = CommonUtil.getEmcode();
            httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(MyApplication.getInstance()))
                    .isDebug(true)
                    .add("sessionId", sessionId)
                    .add("master", CommonUtil.getSharedPreferences(ct, "erp_master"))
                    .add("sessionUser", emCode)
                    .add("sessionId", sessionId)
                    .header("Cookie", "JSESSIONID=" + sessionId)
                    .header("sessionUser", emCode)
                    .build();
            loadData(pageIndex = 1);
        }
    }

    private void loadData(int pageIndex) {
        if (!mRefreshListView.isRefreshing()) {
            progressDialog.show();
        }
        String condition = null;
        Editable editable = mVoiceSearchView.getText();
        if (editable != null && !StringUtil.isEmpty(editable.toString())) {
            String text = editable.toString();
            condition = "db_code like '%" + text + "%' or db_inman like '%" + text + "%'  or db_devtype like '%" + text + "%' or db_centercode like '%" + text + "%'";
        }
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/common/list.action")
                .add("condition", "db_class='周期盘点' and db_statuscode='COMMITED'" + (StringUtil.isEmpty(condition) ? "" : ("  and (" + condition + ")")))
                .add("caller", "DeviceBatch!Stock")
                .add("page", pageIndex)
                .add("pageSize", 25)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    handleData(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }
                mRefreshListView.onRefreshComplete();
                progressDialog.dismiss();
            }
        }));
    }


    private void handleData(String message) throws Exception {
        JSONObject object = JSON.parseObject(message);
        JSONArray columns = JSONUtil.getJSONArray(object, "columns");
        JSONArray listdata = JSONUtil.getJSONArray(object, "listdata");
        List<CycleCount> moreListData = new ArrayList<>();
        for (int i = 0; i < listdata.size(); i++) {
            moreListData.add(handleFrom(columns, listdata.getJSONObject(i)));
        }
        if (pageIndex == 1) {
            allCycleCount = moreListData;
            if (ListUtils.isEmpty(moreListData)) {
                mEmptyLayout.showEmpty();
            }
        } else {
            allCycleCount.addAll(moreListData);
        }
        setAdapter(allCycleCount);
    }

    private CycleCount handleFrom(JSONArray columns, JSONObject data) throws Exception {
        CycleCount cycleCount = new CycleCount();
        for (int i = 0; i < columns.size(); i++) {
            JSONObject item = columns.getJSONObject(i);
            String field = JSONUtil.getText(item, "dataIndex", "field");
            String caption = JSONUtil.getText(item, "caption");
            int width = JSONUtil.getInt(item, "width");
            String values = JSONUtil.getText(data, field);
            CycleCount.Data o = new CycleCount.Data(caption, values);
            if (caption.equals("ID")) {
                cycleCount.setId(values);
            }
            if (width > 0) {
                if (width < 100 && columns.size() > (i + 1) && JSONUtil.getInt(columns.getJSONObject(i + 1), "width") < 100) {
                    //有两个
                    JSONObject item2 = columns.getJSONObject(i + 1);
                    String field2 = JSONUtil.getText(item2, "dataIndex", "field");
                    String caption2 = JSONUtil.getText(item2, "caption");
                    String values2 = JSONUtil.getText(data, field2);
                    o.setCaption2(caption2);
                    o.setValues2(values2);
                    o.setHasTwo(true);
                    if (caption.equals("ID")) {
                        cycleCount.setId(values);
                    }
                    i++;
                }
                cycleCount.addData(o);
            }
        }
        return cycleCount;
    }


    private CycleCountAdapter mAdapter;

    private void setAdapter(List<CycleCount> models) {
        if (mAdapter == null) {
            mAdapter = new CycleCountAdapter(ct, models);
            mAdapter.setOnItemClickListener(new CycleCountAdapter.OnItemClickListener() {
                @Override
                public void click(CycleCount model) {
                    startActivity(new Intent(ct, DeviceCycleCountInfoActivity.class)
                            .putExtra("models", JSON.toJSONString(model.getDatas()))
                            .putExtra("id", model.getId()));
                }
            });
            mRefreshListView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
        }
    }


}
