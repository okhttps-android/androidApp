package com.uas.appworks.OA.platform.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.cyberplayer.utils.G;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.data.TextUtil;
import com.common.preferences.PreferenceUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.utils.NetUtils;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnPlayListener;
import com.modular.apputils.utils.playsdk.AliPlay;
import com.uas.appworks.OA.platform.adapter.ActivityAdapter;
import com.uas.appworks.OA.platform.adapter.CharitableAdapter;
import com.uas.appworks.OA.platform.model.CharitActModel;
import com.uas.appworks.OA.platform.model.CharitModel;
import com.uas.appworks.R;
import com.uas.appworks.widget.SelectPlayPop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharitSearchActivity extends BaseActivity implements OnPlayListener {
    private HttpClient httpClient = new HttpClient.Builder(Constants.charitBaseUrl()).isDebug(true)
            .connectTimeout(5000)
            .readTimeout(5000).build();

    private PullToRefreshListView refreshListView;
    private EmptyLayout mEmptyLayout;
    private int type;
    private TextView findNumTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charit_search);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", 1);
          setTitle(R.string.charitable);
        } else {
            type = 1;
        }
        refreshListView = (PullToRefreshListView) findViewById(R.id.refreshListView);
        mEmptyLayout = new EmptyLayout(this, refreshListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        VoiceSearchView voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        findNumTv = (TextView) findViewById(R.id.findNumTv);
        refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtil.i("i==" + i);
                LogUtil.i("l==" + l);
                if (type == 1) {
                    if (charitableAdapter != null) {
                        CharitModel model = charitableAdapter.getModels((int) l);
                        if (model != null) {
                            IntentUtils.linkCommonWeb(ct, Constants.BASE_CHARIT_PROJECT_URL + model.getId()
                                            + "/" + MyApplication.getInstance().getLoginUserId()
                                    , StringUtil.getMessage(R.string.charitable),
                                    model.getMobileImg(), model.getName());
                        }
                    }
                } else {
                    if (activityAdapter != null && ListUtils.getSize(activityAdapter.getModels()) > l) {
                        CharitActModel model = activityAdapter.getModels().get((int) l);
                        if (model != null) {
                            IntentUtils.linkCommonWeb(ct, Constants.BASE_CHARIT_ACTIVITY_URL + model.getId()
                                            + "/" + MyApplication.getInstance().getLoginUserId()
                                    , StringUtil.getMessage(R.string.charitable)
                                    , model.getActImg(), model.getName());
                        }
                    }
                }
            }
        });
        voiceSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (TextUtils.isEmpty(v.getText())) {
                    loadData(v.getText().toString());
                }
                return false;
            }
        });
        loadData("");
    }

    private void loadData(String keyWork) {
        progressDialog.show();
        lastKeyWork = keyWork;
        if (type == 2) {
            loadActivity(keyWork);
        } else {
            loadProjects(keyWork);
        }
    }

    private String lastKeyWork;

    private void loadActivity(final String keyWork) {
        if (!NetUtils.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
            return;
        }
        if (StringUtil.isEmpty(keyWork)) {
            String activitys = PreferenceUtils.getString("activitys");
            try {
                handlerData(activitys);
            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
        }
        httpClient.Api().send(new HttpClient.Builder()
                .url("activities")
                .add("keyWork", keyWork)
                .add("status", "全部")
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                progressDialog.dismiss();
                if (!keyWork.equals(lastKeyWork)) return;
                try {
                    if (o != null) {
                        handlerData(o.toString());
                    }
                } catch (Exception e) {
                    progressDialog.dismiss();
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }
            }
        }));
    }

    private void loadProjects(final String keyWork) {
        httpClient.Api().send(new HttpClient.Builder()
                .url("projects")
                .add("area", "全部")
                .add("search", keyWork)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                progressDialog.dismiss();
                if (!keyWork.equals(lastKeyWork)) return;
                try {
                    if (o != null) {
                        handlerProjects(o.toString());
                    }
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }
            }
        }));
    }

    private void handlerData(String message) throws Exception {
        if (JSONUtil.validateJSONObject(message)) {
            handlerDataThread(message);
        } else if (JSONUtil.validateJSONArray(message)) {
            handlerDataThread2(message);
        } else {
            setAdapter(null);
        }
    }

    private void handlerDataThread(String message) throws Exception {
        List<CharitActModel> allModels = new ArrayList<>();
        if (JSONUtil.validateJSONObject(message)) {
            JSONObject object = JSON.parseObject(message);
            JSONArray array = JSONUtil.getJSONArray(object, "activityList");
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                CharitActModel e = new CharitActModel();
                e.setActImg(JSONUtil.getText(o, "actImg"));
                e.setId(JSONUtil.getInt(o, "id"));
                e.setName(JSONUtil.getText(o, "name"));
                e.setStage(JSONUtil.getText(o, "stage"));
                JSONArray awards = JSONUtil.getJSONArray(o, "awards");
                StringBuilder builder = new StringBuilder();
                for (int j = 0; j < awards.size(); j++) {
                    String awardLevel = JSONUtil.getText(awards.getJSONObject(j), "awardLevel");
                    String awardName = JSONUtil.getText(awards.getJSONObject(j), "awardName");
                    builder.append(awardLevel + ":" + awardName + "\n");
                }
                e.setSubTitle(builder.toString());
                allModels.add(e);
            }
        }
        setAdapter(allModels);
    }

    private void handlerDataThread2(String message) throws Exception {
        List<CharitActModel> allModels = new ArrayList<>();
        if (JSONUtil.validateJSONArray(message)) {
            JSONArray array = JSON.parseArray(message);
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                CharitActModel e = new CharitActModel();
                e.setActImg(JSONUtil.getText(o, "actImg"));
                e.setId(JSONUtil.getInt(o, "id"));
                e.setName(JSONUtil.getText(o, "name"));
                e.setStage(JSONUtil.getText(o, "stage"));
                e.setSubTitle(JSONUtil.getText(o, "subTitle"));
                allModels.add(e);
            }
        }
        setAdapter(allModels);
    }

    private ActivityAdapter activityAdapter;

    private void setAdapter(List<CharitActModel> models) {
        if (activityAdapter == null) {
            activityAdapter = new ActivityAdapter(ct, models);
            activityAdapter.setKeyWork(lastKeyWork);
            refreshListView.setAdapter(activityAdapter);
        } else {
            activityAdapter.setKeyWork(lastKeyWork);
            activityAdapter.setModels(models);
        }
        progressDialog.dismiss();
        showModels(models);
    }

    private <T> void showModels(List<T> models) {
        if (ListUtils.isEmpty(models)) {
            mEmptyLayout.showEmpty();
            findNumTv.setVisibility(View.GONE);
        } else {
            findNumTv.setVisibility(View.VISIBLE);
            String sAgeFormat = getResources().getString(R.string.find_num_project);
            String sFinalAge = String.format(sAgeFormat, models.size());
            findNumTv.setText(sFinalAge);
        }
    }

    private void handlerProjects(String message) throws Exception {
        LogUtil.i("message=" + message);
        JSONObject object = JSON.parseObject(message);
        JSONArray projectList = JSONUtil.getJSONArray(object, "projectList");
        List<CharitModel> models = JSON.parseArray(projectList.toJSONString(), CharitModel.class);
        List<CharitModel> showModels;
        if (StringUtil.isEmpty(lastKeyWork)) {
            showModels = models;
        } else {
            showModels = new ArrayList<>();
            for (CharitModel e : models) {
                if (e.getArea().toLowerCase().contains(lastKeyWork) ||
                        e.getName().toLowerCase().contains(lastKeyWork) ||
                        e.getProSummary().toLowerCase().contains(lastKeyWork)) {
                    showModels.add(e);
                }
            }
        }
        setProjectsAdapter(showModels);
    }

    private CharitableAdapter charitableAdapter = null;

    private void setProjectsAdapter(List<CharitModel> models) {
        if (charitableAdapter == null) {
            charitableAdapter = new CharitableAdapter(ct, models, mListener);
            charitableAdapter.setKeyWork(lastKeyWork);
            refreshListView.setAdapter(charitableAdapter);
        } else {
            charitableAdapter.setKeyWork(lastKeyWork);
            charitableAdapter.setModels(models);
        }
        showModels(models);
    }


    private CharitableAdapter.MyClickListener mListener = new CharitableAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View v) {
            SelectPlayPop.showPlay(CharitSearchActivity.this, charitableAdapter.getModels(position), new SelectPlayPop.OnSureListener() {
                @Override
                public void sure(double num, int type, CharitModel model) {
                    if (type == 1) {
                        LogUtil.i("选择了微信支付");
                    } else {
                        LogUtil.i("选择了支付宝支付");
                    }
                    loadOrderInfo(num, model);
                }
            });
        }
    };


    private void loadOrderInfo(Double amount, CharitModel model) {
        progressDialog.show();
        Map<String, Object> map = new HashMap<>();
        map.put("projectName", model.getName());
        map.put("amount", amount);
        map.put("proId", model.getId());
        String json = JSON.toJSONString(map);
        LogUtil.i("json=" + json);
        new HttpClient.Builder("http://lj.ubtob.com/alipay/")
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("appPay")
                        .add("jsonStr", json)
                        .method(Method.GET)
                        .build(), new ResultSubscriber<>(new ResultListener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        String message = o.toString();
                        LogUtil.i("message=" + message);
                        AliPlay.api().alipay(CharitSearchActivity.this, message, CharitSearchActivity.this);
                        progressDialog.dismiss();
                    }
                }));
    }

    @Override
    public void onSuccess(String resultStatus, String resultInfo) {
        ToastUtil.showToast(ct, "支付成功");
    }

    @Override
    public void onFailure(String resultStatus, String resultInfo) {
        ToastUtil.showToast(ct, "支付失败");
    }
}
