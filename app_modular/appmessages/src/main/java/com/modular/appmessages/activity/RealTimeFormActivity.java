package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.RealTimeFormMenuAdapter;
import com.modular.appmessages.model.RealTimeFormMenuBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 实时看板菜单页面
 * @date 2017/10/25 18:46
 */

public class RealTimeFormActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private RealTimeFormMenuAdapter mRealTimeFormMenuAdapter;
    private List<RealTimeFormMenuBean> mRealTimeFormMenuBeen;
    private int[] mGridColors = new int[]{R.color.data_inquiry_gird_menu_color1, R.color.data_inquiry_gird_menu_color2
            , R.color.data_inquiry_gird_menu_color3, R.color.data_inquiry_gird_menu_color4, R.color.data_inquiry_gird_menu_color5,
            R.color.data_inquiry_gird_menu_color6};
    private LinearLayout mEmptyLayout;
    private TextView mEmptyTextView;
    private boolean isB2b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_form);
        initView();
        initData();
        mRealTimeFormMenuAdapter.setOnItemClickListener(new RealTimeFormMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = CommonUtil.getAppBaseUrl(RealTimeFormActivity.this)
                        + "mobile/mobileRealTimeCharts.action?numId=" + mRealTimeFormMenuAdapter.getRealTimeFormMenuBeen().get(position).getId();
                String title = mRealTimeFormMenuAdapter.getRealTimeFormMenuBeen().get(position).getTitle();
                Intent intent = new Intent("com.modular.main.WebViewCommActivity");
                intent.putExtra("url", url);
                intent.putExtra("title", title);
                intent.putExtra("ORIENTATION_PORTRAIT", false);
                intent.putExtra("cookie", true);
                startActivity(intent);
            }
        });
    }

    private void initData() {
//        final String realTimeCache = CommonUtil.getSharedPreferences(this, Constants.REAL_TIME_CACHE);
//        if (!TextUtils.isEmpty(realTimeCache) && JSONUtil.validate(realTimeCache)) {
//            JSONArray realTimeArray = JSON.parseArray(realTimeCache);
//            if (realTimeArray != null && realTimeArray.size() > 0) {
//                for (int i = 0; i < realTimeArray.size(); i++) {
//                    RealTimeFormMenuBean realTimeFormMenuBean = new RealTimeFormMenuBean();
//                    JSONObject realTimeObject = realTimeArray.getJSONObject(i);
//                    realTimeFormMenuBean.setId(JSONUtil.getInt(realTimeObject, "ID"));
//                    realTimeFormMenuBean.setTitle(JSONUtil.getText(realTimeObject, "TITLE"));
//                    realTimeFormMenuBean.setColor(mGridColors[(i) % mGridColors.length]);
//
//                    mRealTimeFormMenuBeen.add(realTimeFormMenuBean);
//                }
//            }
//
//            mRealTimeFormMenuAdapter.notifyDataSetChanged();
//        } else {
        progressDialog.show();
        HttpClient mHttpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(RealTimeFormActivity.this)).build();
        mHttpClient.Api().send(new HttpClient.Builder()
                .url("mobile/getRealTimeSubs.action")
                .add("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"))
                .header("Cookie", CommonUtil.getErpCookie(ct))
                .method(Method.GET)
                .build(), new ResultSubscriber<Object>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                progressDialog.dismiss();
                String result = o.toString();
                JSONObject object = JSON.parseObject(result);

                JSONArray subsArray = object.getJSONArray("subs");
                if (subsArray != null && subsArray.size() > 0) {
                    for (int i = 0; i < subsArray.size(); i++) {
                        RealTimeFormMenuBean realTimeFormMenuBean = new RealTimeFormMenuBean();
                        JSONObject realTimeObject = subsArray.getJSONObject(i);
                        realTimeFormMenuBean.setId(JSONUtil.getInt(realTimeObject, "ID"));
                        realTimeFormMenuBean.setTitle(JSONUtil.getText(realTimeObject, "TITLE"));
                        realTimeFormMenuBean.setColor(mGridColors[(i) % mGridColors.length]);

                        mRealTimeFormMenuBeen.add(realTimeFormMenuBean);
                    }
                    mRealTimeFormMenuAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showToast(RealTimeFormActivity.this, "看板数据为空");
                    mEmptyLayout.setVisibility(View.VISIBLE);
                    mEmptyTextView.setText("看板数据为空");
                }
            }

            @Override
            public void onFailure(Object t) {
                progressDialog.dismiss();
                ToastUtil.showToast(RealTimeFormActivity.this, "看板数据获取失败");
                mEmptyLayout.setVisibility(View.VISIBLE);
                mEmptyTextView.setText("看板数据获取失败");
            }
        }));
//        }
    }

    private void initView() {
        setTitle(R.string.real_time_form);
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        mRecyclerView = (RecyclerView) findViewById(R.id.real_time_form_rv);
        mEmptyLayout = (LinearLayout) findViewById(R.id.real_time_form_empty_ll);
        mEmptyTextView = (TextView) findViewById(R.id.real_time_form_msg_tv);

        mGridLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRealTimeFormMenuBeen = new ArrayList<>();
        mRealTimeFormMenuAdapter = new RealTimeFormMenuAdapter(this, mRealTimeFormMenuBeen);
        mRecyclerView.setAdapter(mRealTimeFormMenuAdapter);
    }
}
