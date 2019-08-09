package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.base.BaseActivity;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.ToastUtil;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchSelectActivity extends BaseActivity {
    private VoiceSearchView voiceSearchView;
    private PullToRefreshListView pullToRefreshListView;
    private RelativeLayout empty_rl;

    private int page = 1;
    private String seachString = "";
    private List<String> seachResult = null;
    private SeachAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_select);
        initView();
        initEvent();
    }

    private void initView() {
       setTitle(R.string.describe);

        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pullToRefreshListView);
        empty_rl = (RelativeLayout) findViewById(R.id.empty_rl);

        voiceSearchView.setHineText(getString(R.string.search_select_hint));
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        seachResult = new ArrayList<>();
        mAdapter = new SeachAdapter();
        pullToRefreshListView.setAdapter(mAdapter);
    }

    private void initEvent() {
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                String search = s.toString();
                if (StringUtil.isEmpty(search)) {
                    empty_rl.setVisibility(View.GONE);
                    if (!ListUtils.isEmpty(seachResult)) {
                        seachResult.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                    return;
                }
                page = 1;
                loadSeach(s.toString(), page);
            }
        });
        pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String result = (String) parent.getAdapter().getItem(position);
                LogUtil.i("result=" + result);
                Intent intent = new Intent();
                intent.putExtra("data", result);
                setResult(AppConstant.RESULT_CODE, intent);
                finish();
            }
        });
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                LogUtil.i("onRefresh");
                page++;
                loadSeach(seachString, page);
            }
        });
    }

    private void loadSeach(String seach, int page) {
        if (StringUtil.isEmpty(seach)) {
            empty_rl.setVisibility(View.GONE);
            return;
        }
        seachString = seach;
        String url = "mobile/crm/searchData.action";
        Map<String, Object> param = new HashMap<>();
        param.put("stringSearch", seach);
        param.put("page", page);
        Request request = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
            @Override
            public void result(int what, boolean isJSON, String message, Bundle bundle) {
                if (!isJSON) return;
                pullToRefreshListView.onRefreshComplete();
                JSONObject object = JSON.parseObject(message);
                JSONArray listdata = object.getJSONArray("listdata");
                if (SearchSelectActivity.this.page > 1) {
                    if (ListUtils.isEmpty(listdata))
                        ToastUtil.showToast(ct, R.string.nothing_now);
                    listdata.addAll(0, seachResult);
                }
                if (ListUtils.isEmpty(listdata)) {//为空
                    empty_rl.setVisibility(View.VISIBLE);
                    clearData();
                } else {
                    empty_rl.setVisibility(View.GONE);
                    List<String> netDatas = new ArrayList<>();
                    String result = null;
                    for (int i = 0; i < listdata.size(); i++) {
                        result = JSONUtil.getText(listdata.getJSONObject(i), "result");
                        if (!StringUtil.isEmpty(result)) netDatas.add(result);
                    }
                    if (SearchSelectActivity.this.page == 1) {
                        seachResult = netDatas;
                    } else {
                        if (seachResult == null) seachResult = new ArrayList<>();
                        seachResult.addAll(netDatas);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void error(int what, String message, Bundle bundle) {

            }
        });
    }

    private void clearData() {
        if (!ListUtils.isEmpty(seachResult)) seachResult.clear();

    }

    private class SeachAdapter extends BaseAdapter {
        @Override
        public void notifyDataSetChanged() {
            if (TextUtils.isEmpty(voiceSearchView.getText())) {
                clearData();
                empty_rl.setVerticalGravity(View.GONE);
            }
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(seachResult);
        }

        @Override
        public Object getItem(int position) {
            return seachResult.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoler holer = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(ct).inflate(R.layout.select_list_item, null);
                holer = new ViewHoler();
                holer.select_scb = (CheckBox) convertView.findViewById(R.id.select_scb);
                holer.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
                convertView.setTag(holer);
            } else {
                holer = (ViewHoler) convertView.getTag();
            }
            holer.select_scb.setVisibility(View.GONE);

            String result = seachResult.get(position);
            if (!StringUtil.isEmpty(result))
                holer.name_tv.setText(result);
            return convertView;
        }

        class ViewHoler {
            CheckBox select_scb;
            TextView name_tv;
        }
    }
}
