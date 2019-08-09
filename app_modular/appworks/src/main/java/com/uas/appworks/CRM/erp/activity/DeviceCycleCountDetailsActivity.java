package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appworks.CRM.erp.adapter.CycleCountAdapter;
import com.uas.appworks.CRM.erp.model.CycleCount;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/12/19.
 */

public class DeviceCycleCountDetailsActivity extends SupportToolBarActivity {
    private HttpClient httpClient = null;
    private ListView mListView;
    private boolean isAct;
    private EmptyLayout mEmptyLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_cycle_details);
        initView();
        initData();
    }

    private String id;

    private void initData() {
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
        Intent intent = getIntent();
        if (intent != null) {
            isAct = intent.getBooleanExtra("isAct", false);
            id = intent.getStringExtra("id");
            String title = isAct ? "已盘点" : "未盘点";
            if (title != null) {
             setTitle(title);
            }
            String modelJSON = intent.getStringExtra("models");
            List<CycleCount.Data> models = JSON.parseArray(modelJSON, CycleCount.Data.class);
            mListView.setAdapter(new CycleCountAdapter.DataAdapter(ct, models));
        }
        loadData();
    }

    private void initView() {
        mListView = findViewById(R.id.mListView);
        mEmptyLayout = new EmptyLayout(this, mListView);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
    }

    private void loadData() {
        progressDialog.show();
        String condition = (isAct ? "nvl(dc_actionresult,' ')<>' '" : "nvl(dc_actionresult,' ')=' '") + "  and dc_dbid='" + id + "'";
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/common/getGridPanel.action")
                .add("caller", "DeviceBatch!Stock")
                .add("condition", condition)
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
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        }));
    }


    private void handleData(String message) throws Exception {
        JSONObject object = JSON.parseObject(message);
        JSONArray columns = JSONUtil.getJSONArray(object, "gridItem");
        JSONArray listdata = JSONUtil.getJSONArray(object, "gridData");
        List<CycleCount> moreListData = new ArrayList<>();
        for (int i = 0; i < listdata.size(); i++) {
            moreListData.add(handleFrom(columns, listdata.getJSONObject(i)));
        }
        setAdapter(moreListData);
    }

    private CycleCount handleFrom(JSONArray columns, JSONObject data) throws Exception {
        CycleCount cycleCount = new CycleCount();
        for (int i = 0; i < columns.size(); i++) {
            JSONObject item = columns.getJSONObject(i);
            String field = JSONUtil.getText(item, "dataIndex", "field", "dg_field");
            String caption = JSONUtil.getText(item, "caption", "dg_caption");
            int width = JSONUtil.getInt(item, "width", "dg_appwidth");
            String type = JSONUtil.getText(item, "dg_type", "type");
            String values = null;

            if (type.equals("datefield")) {
                values = DateFormatUtil.long2Str(JSONUtil.getLong(data, field.toUpperCase()), "yyyy-MM-dd");
            } else {
                values = JSONUtil.getText(data, field.toUpperCase());
            }

            CycleCount.Data o = new CycleCount.Data(caption, values);
            if (caption.equals("ID")) {
                cycleCount.setId(values);
            }
//            if (width < 100 && columns.size() > (i + 1) && JSONUtil.getInt(columns.getJSONObject(i + 1), "width") < 100) {
//                //有两个
//                JSONObject item2 = columns.getJSONObject(i + 1);
//                String field2 = JSONUtil.getText(item2, "dataIndex", "field");
//                String caption2 = JSONUtil.getText(item2, "caption");
//                String values2 = JSONUtil.getText(data, field2);
//                o.setCaption2(caption2);
//                o.setValues2(values2);
//                o.setHasTwo(true);
//                if (caption.equals("ID")) {
//                    cycleCount.setId(values);
//                }
//                i++;
//            }
            if (width > 0) {
                if (caption.equals("设备编号")) {
                    cycleCount.addData(0, o);
                } else if (caption.equals("设备名称") && ListUtils.getSize(cycleCount.getDatas()) > 1) {
                    cycleCount.addData(1, o);
                } else {
                    cycleCount.addData(o);
                }
            }
        }
        return cycleCount;
    }

    private CycleCountAdapter mAdapter;

    private void setAdapter(List<CycleCount> models) {
        if (mAdapter == null) {
            mAdapter = new CycleCountAdapter(ct, models);
            mAdapter.setNeedShowAll(true);
            mAdapter.setOnItemClickListener(new CycleCountAdapter.OnItemClickListener() {
                @Override
                public void click(CycleCount model) {
                    LogUtil.i("model.getDatas()=" + JSON.toJSONString(model.getDatas()));
                }
            });
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
        }
        if (ListUtils.isEmpty(models)) {
            mEmptyLayout.showEmpty();
        }
    }
}
