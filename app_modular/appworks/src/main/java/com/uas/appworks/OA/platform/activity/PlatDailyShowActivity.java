package com.uas.appworks.OA.platform.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.activity.*;
import com.uas.appworks.OA.platform.adapter.PlatDailyAdapter;
import com.uas.appworks.OA.platform.model.PlatDailyBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by FANGlh on 2017/3/9.
 * function: 平台日报列表界面
 */
public class PlatDailyShowActivity extends BaseActivity {

    private VoiceSearchView voiceSearchView;
    private PullToRefreshListView mPullToRefreshListView;

    public EmptyLayout mEmptyLayout;
    private List<PlatDailyBean.DataBean> real_list;
    private List<PlatDailyBean.DataBean> click_list;
    private List<PlatDailyBean.DataBean> last_list;
    private PlatDailyAdapter mAdapter;
    private String delete_succeed;
    private int mCurrentPage = 1;
    private static final int GET_B2B_DAILY_LIST = 0307;
    private int returnedDataid;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
        super.onResume();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case GET_B2B_DAILY_LIST:
                    if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                        String b2b_daily_list = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("b2b_daily_list", b2b_daily_list);
                        handlePlatDailtData(b2b_daily_list);
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };

    private void handlePlatDailtData(String b2b_daily_list) {
        if (mCurrentPage == 1) {
            real_list.clear();
            ToastMessage(getString(R.string.common_refresh_finish));
        } else {
            ToastMessage(getString(R.string.common_up_finish));
        }
        mPullToRefreshListView.setVisibility(View.VISIBLE);
        mPullToRefreshListView.onRefreshComplete();

        if (voiceSearchView != null)
            voiceSearchView.setText("");
        try {
            JSONObject resultJsonObject = new JSONObject(b2b_daily_list);
            JSONArray dailydataArray = resultJsonObject.getJSONArray("data");
            if (mCurrentPage == 1 && dailydataArray == null && real_list.isEmpty()) {
                mEmptyLayout.showEmpty();
            } else {
                real_list.addAll(JSON.parseArray(dailydataArray.toString(), PlatDailyBean.DataBean.class));
                mAdapter.setPdata(real_list);
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEvent() {
        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null) return;
                if ("delete_succeed".equals(delete_succeed) && !ListUtils.isEmpty(last_list) && TextUtils.isEmpty(editable.toString())) {
                    mAdapter.setPdata(last_list);
                    Log.v("delete_succeed", delete_succeed);
                    mAdapter.notifyDataSetChanged();
                } else {
                    String strChche = editable.toString().replace(" ", "");//去除空格
                    strChche = strChche.replace(" ", " ");//去除空格
                    List<PlatDailyBean.DataBean> chche = new ArrayList<>();

                    if (ListUtils.isEmpty(real_list)) {
                        mEmptyLayout.showEmpty();
                        return;
                    } else {
                        for (PlatDailyBean.DataBean pdata : real_list) {
                            boolean b = getResult(pdata.getWd_date() + pdata.getWd_comment() + pdata.getWd_status(), strChche.trim());
                            if (b) {
                                chche.add(pdata);
                            }
                        }
                    }
                    mAdapter.setSearch_content(strChche);
                    mAdapter.setPdata(chche);
                    if (!TextUtils.isEmpty(strChche)) {
                        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                click_list = mAdapter.getPdata();
                if (!TextUtils.isEmpty(click_list.get(position - 1).getWd_status()) &&
                        click_list.get(position - 1).getWd_status().equals("在录入")) {
                    startActivity(new Intent(activity, WorkDailyAddActivity.class)
                            .putExtra("id", click_list.get(position == 0 ? 0 : (position - 1)).getWd_id())
                            .putExtra("rs_summary", click_list.get(position == 0 ? 0 : (position - 1)).getWd_comment())
                            .putExtra("rs_plan", click_list.get(position == 0 ? 0 : (position - 1)).getWd_plan())
                            .putExtra("rs_experience", click_list.get(position == 0 ? 0 : (position - 1)).getWd_experience())
                            .putExtra("resubmit", "unsub_tosub"));
                } else {
                    Intent intent = new Intent(activity, PlatWDdetailyActivity.class);
                    intent.putExtra("ID", click_list.get(position == 0 ? 0 : (position - 1)).getWd_id());
                    intent.putExtra("Date", TimeUtils.s_long_2_str(CommonUtil.getlongNumByString(click_list.get(position == 0 ? 0 : (position - 1)).getWd_date())));
                    intent.putExtra("Content", click_list.get(position == 0 ? 0 : (position - 1)).getWd_comment());
                    intent.putExtra("WD_Status", click_list.get(position == 0 ? 0 : (position - 1)).getWd_status());
                    intent.putExtra("Plan", click_list.get(position == 0 ? 0 : (position - 1)).getWd_plan());
                    intent.putExtra("Experience", click_list.get(position == 0 ? 0 : (position - 1)).getWd_experience());
                    intent.putExtra("fromwhere", "dailylist");
                    startActivityForResult(intent, 1219);
                }
            }
        });

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurrentPage = 1;
                initData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurrentPage++;
                initData();
            }
        });
    }

    private void initData() {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getdaily_list;
        Map<String, Object> param = new HashMap<>();
        param.put("pageNumber", mCurrentPage);
        param.put("pageSize", 10);
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, GET_B2B_DAILY_LIST, null, null, "get");
    }

    private void initView() {
        setContentView(R.layout.activity_work_daily);
        ViewUtils.inject(this);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.work_daily_context_ptlv);
        setTitle(getString(R.string.wd_recording_title));
        mEmptyLayout = new EmptyLayout(this, mPullToRefreshListView.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);

        real_list = new ArrayList<>();
        mAdapter = new PlatDailyAdapter(this);
        mAdapter.setPdata(real_list);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        real_list = mAdapter.getPdata();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1219) {
            if (resultCode == DailydetailsActivity.DELETE_SUCCEED) {
                if (data != null) {
                    delete_succeed = data.getStringExtra("delete_succeed");
                    returnedDataid = data.getIntExtra("deleted_id", 0);
                    if (real_list != null) {
                        last_list = real_list;
                        for (int i = 0; i < last_list.size(); i++) {
                            int currid = last_list.get(i).getWd_id();
                            if (currid == returnedDataid) {
                                last_list.remove(i);
//                                click_list.remove(mPosition - 1);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            putDownInput();
            final Intent intent = getIntent();
            if (!TextUtils.isEmpty(intent.getStringExtra("fromwhere"))
                    && intent.getStringExtra("fromwhere").equals("nosubmitdaily")) {
                finish();
            } else {
                Intent intent1 = new Intent(PlatDailyShowActivity.this, WorkReportMenuActivity.class);
                startActivity(intent1);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        final Intent intent = getIntent();
        if (!TextUtils.isEmpty(intent.getStringExtra("fromwhere"))
                && intent.getStringExtra("fromwhere").equals("nosubmitdaily")) {
            finish();
        } else {
            Intent intent1 = new Intent(PlatDailyShowActivity.this, WorkReportMenuActivity.class);
            startActivity(intent1);
        }
        super.onBackPressed();
    }

    //正则
    private static boolean getResult(String text, String str) {
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(text);
        return m.find();
    }

    private void putDownInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(voiceSearchView.getWindowToken(), 0);
    }


}
