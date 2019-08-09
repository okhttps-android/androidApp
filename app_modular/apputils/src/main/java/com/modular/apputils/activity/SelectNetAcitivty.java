package com.modular.apputils.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.OABaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.R;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitlike on 2017/11/27.
 */

public class SelectNetAcitivty extends OABaseActivity {
    private HttpClient httpClient;
    private String lastKey;
    private int page = 1;

    //intent
    private String caller;//传进来的主表caller
    private String gCaller;//传进来的附表caller，当是明细表时候存在
    private String fieldKey;
    private String corekey;
    private boolean dataForm;

    private PullToRefreshListView refreshListView;
    private EmptyLayout mEmptyLayout;
    private int groupId;
    private String mDefCondition;//默认的前提mDefCondition

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_net_select);
        initView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent() != null && getIntent().getBooleanExtra("device", false)) {
            getMenuInflater().inflate(R.menu.menu_me_scan, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.me_scan) {
            requestPermission(Manifest.permission.CAMERA, new Runnable() {
                @Override
                public void run() {
                    startActivityForResult(new Intent(ct, CaptureActivity.class), 0x21);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(ct, R.string.not_camera_permission);
                }
            });


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 0x21 && resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        LogUtil.i("result=" + result);
                        lastKey = result;
                        showLoading();
                        loadSearch(page = 1, result);
                    }
                }
            }
        }
    }

    private boolean isDevice;
    private boolean isForm;

    private void initView() {

        Intent intent = getIntent();
        if (intent != null) {
            fieldKey = intent.getStringExtra("fieldKey");
            caller = intent.getStringExtra("caller");
            gCaller = intent.getStringExtra("gCaller");
            dataForm = intent.getBooleanExtra("dataForm", false);
            corekey = intent.getStringExtra("corekey");
            isDevice = intent.getBooleanExtra("isDevice", false);
            groupId = intent.getIntExtra("groupId", 0);
            mDefCondition = intent.getStringExtra("mCondition");
            isForm = intent.getBooleanExtra("isForm", true);
        }
        VoiceSearchView voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        refreshListView = (PullToRefreshListView) findViewById(R.id.refreshListView);
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mEmptyLayout = new EmptyLayout(this, refreshListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                lastKey = s == null ? "" : s.toString();
                if (dataForm) {
                    findByLocal();
                } else if (isDevice && !TextUtils.isEmpty(lastKey)) {
                    loadSearchDevice(lastKey);
                } else {
                    loadSearch(page = 1, lastKey);
                }
            }
        });
        voiceSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                lastKey = StringUtil.getText(v);
                if (dataForm) {
                    findByLocal();
                } else if (isDevice && !TextUtils.isEmpty(lastKey)) {
                    loadSearchDevice(lastKey);
                } else {
                    loadSearch(page = 1, lastKey);
                }
                return false;
            }
        });
        if (dataForm) {
            refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        } else {
            refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    loadSearch(page = 1, lastKey);
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    loadSearch(++page, lastKey);

                }
            });
        }

        String baseUrl = CommonUtil.getAppBaseUrl(this);
        String sessionId = CommonUtil.getSharedPreferences(this, "sessionId");
        httpClient = new HttpClient.Builder(baseUrl)
                .isDebug(true)
                .add("master", CommonUtil.getMaster())
                .add("sessionUser", CommonUtil.getEmcode())
                .add("sessionId", sessionId)
                .connectTimeout(5000)
                .isDebug(true)
                .readTimeout(5000)
                .build();
        loadSearch(page = 1, lastKey = "");
        showLoading();
    }

    private void findByLocal() {
        List<Bean> showModels = null;
        if (ListUtils.getSize(allModels) > 0) {
            if (TextUtils.isEmpty(lastKey)) {
                showModels = allModels;
            } else {
                showModels = new ArrayList<>();
                for (Bean e : allModels) {
                    if (e != null && !TextUtils.isEmpty(e.showName) && e.showName.toUpperCase().contains(lastKey)) {
                        showModels.add(e);
                    }
                }
            }
            showByAdapter(showModels);
        }
    }

    private void loadSearchDevice(final String keyWork) {
        refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        HttpClient.Builder builder = new HttpClient.Builder();
        boolean isForm = StringUtil.isEmpty(gCaller);
        builder.url("mobile/device/getSearchData.action")
                .add("caller", isForm ? caller : gCaller)
                .add("name", fieldKey)
                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"))
                .add("condition", keyWork)
                .method(Method.GET);
        httpClient.Api().send(builder.build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!lastKey.equals(keyWork)) return;
                try {
                    handleData(o.toString());
                    dimssLoading();
                } catch (Exception e) {
                    dimssLoading();
                    if (mAdapter == null || ListUtils.isEmpty(mAdapter.models)) {
                        mEmptyLayout.showEmpty();
                    }
                }
                refreshListView.onRefreshComplete();
            }
        }));

    }


    private String getCondition(String keyWork) {
        StringBuilder builder = new StringBuilder(StringUtil.isEmpty(mDefCondition) ? "" : (mDefCondition + " and ("));
        if (configMap == null || configMap.isEmpty()) {
            builder.append("upper(" + (StringUtil.isEmpty(corekey) ? fieldKey : corekey) + ") like '%" + keyWork.toUpperCase() + "%'");
        } else {
            for (Map.Entry<String, String> e : configMap.entrySet()) {
                builder.append("upper(" + e.getKey() + ") like '%" + keyWork.toUpperCase() + "%' or ");
            }
            if (builder.length() > 3) {
                builder.delete(builder.length() - 3, builder.length() - 1);
            }
        }
        if (!StringUtil.isEmpty(mDefCondition)) {
            builder.append(" ) ");
        }
        return builder.toString();
    }


    private void loadSearch(int page, final String keyWork) {
        refreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        String condition = null;
        if (StringUtil.isEmpty(keyWork)) {
            if (StringUtil.isEmpty(mDefCondition)) {
                condition = "1=1";
            } else {
                condition = mDefCondition;
            }
        } else {
            condition = getCondition(keyWork);
        }
        LogUtil.i("gong", "condition=" + condition);
        HttpClient.Builder builder = new HttpClient.Builder();
        boolean isForm = StringUtil.isEmpty(gCaller);
        builder.url("mobile/common/dbfind.action")
                .add("which", isForm ? "form" : "grid")
                .add("caller", isForm ? caller : gCaller)
                .add("field", fieldKey)
                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"))
                .add("condition", condition)
                .add("ob", "")
                .isDebug(true)
                .add("_config", "")
                .add("page", page)
                .add("pageSize", dataForm ? 10000 : 20)
                .method(Method.GET);
        if (!isForm) {
            builder.add("gridField", fieldKey)
                    .add("gridCaller", caller);//主从（单据caller   副从表传 本身的caller）
        }
        httpClient.Api().send(builder.isDebug(true).build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!lastKey.equals(keyWork)) return;
                try {
                    handleData(o.toString());
                    dimssLoading();
                } catch (Exception e) {
                    dimssLoading();
                    if (mAdapter == null || ListUtils.isEmpty(mAdapter.models)) {
                        mEmptyLayout.showEmpty();
                    }
                }
                refreshListView.onRefreshComplete();
            }
        }));
    }

    private ListAdapter mAdapter = null;


    private void handleData(String messgae) throws Exception {
        JSONObject object = JSON.parseObject(messgae);
        JSONArray dbfinds = JSONUtil.getJSONArray(object, "dbfinds", "gridDbfinds");
        String dataStr = JSONUtil.getText(object, "data");
        JSONArray data = JSON.parseArray(dataStr);
        setData2Adapter(data, dbfinds);
        LogUtil.i("gong", "message=" + messgae);
    }


    private Map<String, String> configMap;
    private String fieldKeyLike = null;

    private void setData2Adapter(JSONArray data, JSONArray dbfinds) throws Exception {
        if (configMap == null || TextUtils.isEmpty(fieldKeyLike)) {
            configMap = new LinkedHashMap<>();
            //获取配置
            JSONObject config = null;
            for (int i = 0; i < dbfinds.size(); i++) {
                config = dbfinds.getJSONObject(i);
                String dbGridField = JSONUtil.getText(config, "dbGridField", "ds_dbfindfield");//显示值对应字段名
                String field = JSONUtil.getText(config, "field", "ds_gridfield");//实际字段名
                if (!StringUtil.isEmpty(dbGridField) && !StringUtil.isEmpty(field)) {
                    if (field.equals(fieldKey)) {
                        fieldKeyLike = dbGridField;
                    }
                    configMap.put(dbGridField, field);
                }
            }
        }
        JSONObject o = null;
        Bean b = null;
        List<Bean> models = new ArrayList<>();
        Map<String, String> jsonMap = null;
        for (int i = 0; i < data.size(); i++) {
            o = data.getJSONObject(i);
            b = new Bean();
            b.name = JSONUtil.getText(o, fieldKeyLike);
            jsonMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : configMap.entrySet()) {
                jsonMap.put(e.getValue(), JSONUtil.getText(o, e.getKey()));
            }
            try {
                b.showName = getShowName(jsonMap);
            } catch (Exception e) {

            }
            b.json = JSONUtil.map2JSON(jsonMap);
            if (!StringUtil.isEmpty(b.name))
                models.add(b);
        }
        if (TextUtils.isEmpty(lastKey) && dataForm) {
            allModels = models;
        }
        showByAdapter(models);
    }

    private List<Bean> allModels = null;

    private void showByAdapter(List<Bean> models) {
        if (mAdapter == null) {
            mAdapter = new ListAdapter(models);
            refreshListView.setAdapter(mAdapter);
            refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mAdapter != null) {
                        Bean model = mAdapter.getModels((int) l);
                        setResult(90, new Intent()
                                .putExtra("data", model.json)
                                .putExtra("groupId", groupId)
                                .putExtra("isForm", isForm));
                        finish();

                    }
                }
            });

        } else {
            if (page == 1) {
                mAdapter.setModels(models);
            } else {
                mAdapter.addModls(models);
            }
        }
        if (ListUtils.isEmpty(models)) {
            mEmptyLayout.showEmpty();
        }
    }

    private <T> String getShowName(Map<String, T> object) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, T> e : object.entrySet()) {
            if (!TextUtils.isEmpty(e.getValue() + "")) {
                builder.append(e.getValue() + ",");
            }
        }
        StringUtil.removieLast(builder);
        return builder.toString();
    }

    class ListAdapter extends BaseAdapter {
        private List<Bean> models;

        public Bean getModels(int item) {
            if (ListUtils.getSize(models) > item) {
                return models.get(item);
            }
            return null;
        }

        public ListAdapter(List<Bean> models) {
            this.models = models;
        }

        public void setModels(List<Bean> models) {
            this.models = models;
            notifyDataSetChanged();
        }

        private void addModls(List<Bean> models) {
            if (this.models == null) {
                this.models = new ArrayList<>();
            }
            this.models.addAll(models);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(this.models);
        }

        @Override
        public Object getItem(int i) {
            return models.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHoler holer = null;
            if (view == null) {
                holer = new ViewHoler();
                view = LayoutInflater.from(ct).inflate(R.layout.select_list_item, null);
                holer.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holer.select_scb = (CheckBox) view.findViewById(R.id.select_scb);
                holer.select_scb.setVisibility(View.GONE);
                view.setTag(holer);
            } else {
                holer = (ViewHoler) view.getTag();
            }
            Bean bean = models.get(i);
            holer.name_tv.setText(bean.getShowName());
            return view;
        }

        class ViewHoler {
            TextView name_tv;
            CheckBox select_scb;
        }
    }

    private class Bean {
        String name;
        String json;
        String showName;

        public String getShowName() {
            return StringUtil.isEmpty(showName) ? name : showName;
        }
    }
}
