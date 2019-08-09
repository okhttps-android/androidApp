package com.uas.appworks.OA.platform.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.common.thread.ThreadPool;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.EasyFragment;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.utils.NetUtils;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.adapter.PopListAdapter;
import com.uas.appworks.OA.platform.activity.CharitSearchActivity;
import com.uas.appworks.OA.platform.adapter.ActivityAdapter;
import com.uas.appworks.OA.platform.model.CharitActModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/11/8.
 */

public class ActivityFragment extends EasyFragment implements View.OnClickListener {
    private HttpClient httpClient = new HttpClient.Builder(Constants.charitBaseUrl()).isDebug(true)
            .connectTimeout(5000)
            .readTimeout(5000).build();
    private PullToRefreshListView refreshListView;
    private BaseActivity baseActivity;
    private TextView statusTv;
    private EmptyLayout mEmptyLayout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        }
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_charitable_list;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            setHasOptionsMenu(true);
            initView();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.getItem(0);
        if (item!=null){
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    onOptionsItemSelected(menuItem);
                    return false;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            ct.startActivity(new Intent(ct, CharitSearchActivity.class).
                    putExtra("type", 2)
                    .putExtra("title", "活动搜索"));
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(R.id.refreshListView);
        mEmptyLayout = new EmptyLayout(ct, refreshListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        ViewStub headerVS = (ViewStub) findViewById(R.id.headerVS);
        headerVS.inflate();
        statusTv = (TextView) findViewById(R.id.statusTv);
        statusTv.setOnClickListener(this);
        findViewById(R.id.statusRl).setOnClickListener(this);

        allModels = new ArrayList<>();
        refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    toDetail((int) l);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                initData();
            }
        });
//        setAdapter(null);
        initData();
    }

    private void toDetail(int position) throws Exception {
        if (mAdapter != null && ListUtils.getSize(mAdapter.getModels()) > position) {
            CharitActModel model = mAdapter.getModels().get(position);
            IntentUtils.linkCommonWeb(ct, Constants.BASE_CHARIT_ACTIVITY_URL + model.getId()
                            + "/" + MyApplication.getInstance().getLoginUserId()
                    , StringUtil.getMessage(R.string.charitable)
                    , model.getActImg(), model.getName());
        }
    }


    private void initData() {
        loadData("", "全部");
    }

    private void loadData(String keyWork, final String status) {
        String activitys = PreferenceUtils.getString("activitys");
        if (!StringUtil.isEmpty(activitys)) try {
            handlerData(false, activitys, "全部");
        } catch (Exception e) {
        }

        if (!NetUtils.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
        } else {
            baseActivity.progressDialog.show();
            httpClient.Api().send(new HttpClient.Builder()
                    .url("activities")
                    .add("keyWork", keyWork)
                    .add("status", status)
                    .method(Method.GET)
                    .build(), new ResultSubscriber<>(new ResultListener<Object>() {
                @Override
                public void onResponse(Object o) {
                    try {
                        if (o != null) {
                            handlerData(true, o.toString(), status);
                        }
                    } catch (Exception e) {
                        if (e != null) {
                            LogUtil.i("e=" + e.getMessage());
                        }
                    }
                    refreshListView.onRefreshComplete();
                    baseActivity.progressDialog.dismiss();
                }


            }));
        }

    }

    private ActivityAdapter mAdapter;
    private List<CharitActModel> allModels;
    private List<String> selectStage = new ArrayList<>();


    private void handlerDataThread(String message, String status) throws Exception {
        List<CharitActModel> allModels = new ArrayList<>();
        if (JSONUtil.validateJSONObject(message)) {
            JSONObject object = JSON.parseObject(message);
            JSONArray array = JSONUtil.getJSONArray(object, "activityList");
            LogUtil.prinlnLongMsg("gongpengming", " array.size()=" + array.size());
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
            setAdapter(status, allModels);
            selectStage.clear();
            boolean have;
            selectStage.add("全部");
            for (CharitActModel e : allModels) {
                have = false;
                for (String b : selectStage) {
                    if (b.equals(e.getStage())) {
                        have = true;
                        break;
                    }
                }
                if (!have) {
                    selectStage.add(e.getStage());
                }
            }
        } else {
            setAdapter(status, allModels);
        }
        this.allModels = allModels;
        PreferenceUtils.putString("activitys", JSON.toJSONString(allModels));
    }

    private void handlerDataThread2(String message, String status) throws Exception {
        List<CharitActModel> allModels = new ArrayList<>();
        if (JSONUtil.validateJSONArray(message)) {
            LogUtil.prinlnLongMsg("gongpengming", "time1=" + System.currentTimeMillis());
            LogUtil.prinlnLongMsg("gongpengming", "message=" + message);
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
            LogUtil.prinlnLongMsg("gongpengming", "time2=" + DateFormatUtil.long2Str("HH:mm:ss"));
            setAdapter(status, allModels);
            selectStage.clear();
            boolean have;
            selectStage.add("全部");
            for (CharitActModel e : allModels) {
                have = false;
                for (String b : selectStage) {
                    if (b.equals(e.getStage())) {
                        have = true;
                        break;
                    }
                }
                if (!have) {
                    selectStage.add(e.getStage());
                }
            }
        } else {
            setAdapter(status, allModels);
        }
        this.allModels = allModels;
    }

    private void handlerData(final boolean isNet, final String message, final String status) throws Exception {
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isNet) {
                        handlerDataThread(message, status);
                    } else {
                        handlerDataThread2(message, status);
                    }
                } catch (Exception e) {
                    LogUtil.prinlnLongMsg("gongpengming", "e=" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void setAdapter(String status, List<CharitActModel> models) {
        final List<CharitActModel> showModels = new ArrayList<>();
        if (!"全部".equals(status)) {
            for (CharitActModel model : models) {
                if (status.equals(model.getStage())) {
                    showModels.add(model);
                }
            }
        } else {
            showModels.addAll(models);
        }
        OAHttpHelper.getInstance().post(new Runnable() {
            @Override
            public void run() {
                setAdapter(showModels);
            }
        });
    }

    private void setAdapter(List<CharitActModel> models) {
        if (mAdapter == null) {
            mAdapter = new ActivityAdapter(ct, models);
            refreshListView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
        }
        if (ListUtils.isEmpty(models)) {
            mEmptyLayout.showEmpty();
        }
        LogUtil.prinlnLongMsg("gongpengming", "time7=" + System.currentTimeMillis() / 1000);
        refreshListView.onRefreshComplete();
        baseActivity.progressDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.searchIv) {
        } else if (id == R.id.statusTv || id == R.id.statusRl) {
            showCommonWordsIV(statusTv);
        }
    }


    private void showCommonWordsIV(View view) {
        final PopupWindow window = new PopupWindow(ct);
        View windowView = LayoutInflater.from(ct).inflate(R.layout.item_list_pop, null);
        window.setContentView(windowView);
        ListView contentLV = (ListView) windowView.findViewById(R.id.contentLV);
        if (ListUtils.isEmpty(selectStage)) {
            selectStage.add("全部");
            selectStage.add("进行中");
            selectStage.add("已结束");
        }
        contentLV.setAdapter(new PopListAdapter(ct, selectStage));
        window.setTouchable(true);
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.color.white));
        window.getContentView().measure(0, 0);
        window.setHeight(DisplayUtil.dip2px(ct, 45 * ListUtils.getSize(selectStage)));
        window.setWidth(DisplayUtil.dip2px(ct, 80));
//        window.setAnimationStyle(R.style.MenuAnimationFade);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //在控件上方显示
        window.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] + view.getHeight());
        DisplayUtil.backgroundAlpha(ct, 0.6f);
        contentLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ListUtils.getSize(selectStage) > position) {
                    String message = selectStage.get(position);
                    selectStatus(message);
                    statusTv.setText(message);
                }
                window.dismiss();
            }
        });
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
    }


    private void selectStatus(final String message) {
        if (message == null || message.length() <= 0) {
            ToastUtil.showToast(ct, "选择错误");
        } else if (ListUtils.isEmpty(allModels)) {
            loadData("", message);
        } else {
            ThreadPool.getThreadPool().addTask(new Runnable() {
                @Override
                public void run() {
                    setAdapter(message, allModels);
                }
            });
        }
    }
}
