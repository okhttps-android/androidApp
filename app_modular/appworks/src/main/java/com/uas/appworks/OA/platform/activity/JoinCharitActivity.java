package com.uas.appworks.OA.platform.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.utils.NetUtils;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appworks.OA.platform.adapter.JoinCharitAdapter;
import com.uas.appworks.OA.platform.model.JoinModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

public class JoinCharitActivity extends BaseActivity {

    private int type = 1;
    private PullToRefreshListView refreshListView;
    private TextView numTv;
    private TextView charitTv;
    private EmptyLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_charitable_list);
        initView();
        initData();
    }


    private void initView() {
        Intent intent = getIntent();
        String num = "";
        String sumMoney = "";
        if (intent != null) {
            type = intent.getIntExtra("type", 1);
            num = intent.getStringExtra("sumNum");
            String title = intent.getStringExtra("title");
            if (type == 1) {
                sumMoney = intent.getStringExtra("sumMoney");
            }

            if (!StringUtil.isEmpty(title)) {
                setTitle(title);
            }
        }
        refreshListView = (PullToRefreshListView) findViewById(R.id.refreshListView);
        emptyLayout = new EmptyLayout(ct, refreshListView.getRefreshableView());
        emptyLayout.setShowLoadingButton(false);
        emptyLayout.setShowEmptyButton(false);
        emptyLayout.setShowErrorButton(false);
        emptyLayout.setEmptyViewRes(com.core.app.R.layout.view_empty);
        if (type == 2) {
            refreshListView.getRefreshableView().addHeaderView(getActivityHeadler(num));
        } else {
            refreshListView.getRefreshableView().addHeaderView(getProjectHeadler(num, sumMoney));
        }

        refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mAdapter != null) {
                    LogUtil.i("i=" + i);
                    LogUtil.i("l=" + l);
                    JoinModel model = mAdapter.getModel((int) l);
                    if (model != null) {
                        IntentUtils.linkCommonWeb(ct,
                                (type == 2 ? Constants.BASE_CHARIT_ACTIVITY_URL
                                        : Constants.BASE_CHARIT_PROJECT_URL)
                                        + model.getId()
                                        + "/" + MyApplication.getInstance().getLoginUserId()
                                , StringUtil.getMessage(R.string.charitable)
                                , null, null);
                    }
                }
            }
        });

        setAdapter(null);
    }


    private View getProjectHeadler(String num, String sumMoney) {
        View view = LayoutInflater.from(ct).inflate(R.layout.handler_join_project, null);
        numTv = (TextView) view.findViewById(R.id.numTv);
        charitTv = (TextView) view.findViewById(R.id.charitTv);
        numTv.setText(String.valueOf(StringUtil.getFirstInt(num, 0)));
        charitTv.setText(sumMoney == null ? "" : sumMoney);
        return view;
    }

    private View getActivityHeadler(String num) {
        View view = LayoutInflater.from(ct).inflate(R.layout.handler_join_activity, null);
        numTv = (TextView) view.findViewById(R.id.numTv);
        numTv.setText(String.valueOf(StringUtil.getFirstInt(num, 0)));
        return view;
    }


    private void initData() {
        if (!NetUtils.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
            return;
        }
        progressDialog.show();
        HttpClient httpClient = new HttpClient.Builder(Constants.charitBaseUrl()).isDebug(true).build();
        HttpClient builder = new HttpClient.Builder()
                .url((type == 2 ? "joinActivity" : "donateDetail") + "/" + MyApplication.getInstance().getLoginUserId())
                .method(Method.GET)
                .build();
        httpClient.Api().send(builder, new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    handleData(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("userFragement initData Exception" + e.getMessage());
                    }
                }
                progressDialog.dismiss();
            }
        }));
    }

    private JoinCharitAdapter mAdapter;

    private void handleData(String message) {
        LogUtil.i("message=" + message);

        JSONObject object = JSON.parseObject(message);
        JSONArray array = JSONUtil.getJSONArray(object, "projectRecodeList", "activityRecodeList");
        if (!ListUtils.isEmpty(array)) {
            JSONObject o;
            List<JoinModel> models = new ArrayList<>();
            JoinModel model = null;
            for (int i = 0; i < array.size(); i++) {
                o = array.getJSONObject(i);
                model = new JoinModel();
                JSONObject projectOrActivity = JSONUtil.getJSONObject(o, "activity", "project");
                String time = type == 1 ? JSONUtil.getText(o, "time") : JSONUtil.getText(projectOrActivity, "luckyTime");
                String sub = JSONUtil.getText(o, "status", "isGetAward");
                String name = JSONUtil.getText(projectOrActivity, "name");
                String status = null;
                if (type == 2) {
                    String stage = JSONUtil.getText(projectOrActivity, "stage");
                    status = getStatus(stage);
                } else {
                    status = JSONUtil.getText(projectOrActivity, "overdue", "stage");
                }

                int id = JSONUtil.getInt(projectOrActivity, "id");
                float amount = JSONUtil.getFloat(o, "amount");
                model.setId(id);
                model.setAmount(amount);
                model.setName(name);
                model.setTime(time);
                model.setSub(sub);
                model.setStatus(status);
                models.add(model);
            }
            setAdapter(models);
        } else {
            emptyLayout.showEmpty();
        }
    }

    private String getStatus(String stage) {
        switch (stage) {
            case "0":
                return "进行中";
            case "1":
                return "待开奖";
            case "2":
                return "待兑奖";
            case "3":
                return "已结束";
        }
        return stage;
    }

    private void setAdapter(List<JoinModel> models) {
        if (mAdapter == null) {
            mAdapter = new JoinCharitAdapter(ct, type, models);
            refreshListView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
            mAdapter.notifyDataSetChanged();
        }
        if (ListUtils.isEmpty(models)) {
            emptyLayout.showEmpty();
        }
    }
}
