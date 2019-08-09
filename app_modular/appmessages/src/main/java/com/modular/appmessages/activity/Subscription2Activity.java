package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.widget.RecycleViewDivider;
import com.core.widget.crouton.Style;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.SubscriptionAdapter;
import com.modular.appmessages.db.SubsDao;
import com.modular.appmessages.model.SubMessage;
import com.modular.apputils.utils.PopupWindowHelper;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Subscription2Activity extends BaseActivity {
    private static final int REQUEST_SUBS_DETAIL = 0x20;
    private SimpleRefreshLayout swiperefresh;
    private RecyclerView recycler;

    private long daylong = 24 * 1000 * 3600;
    private long showTime;//当前显示的最早的时间，初始化进来为当前
    private String baseUrl;//网址
    private ArrayList<SubMessage> messages;
    private SubscriptionAdapter adapter;
    private RecycleViewDivider viewDivider;
    private RecycleViewDivider emptyDivider;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                stopFresh();
                switch (msg.what) {
                    case 0x11:
                        String message = msg.getData().getString("result");
                        if (!StringUtil.isEmpty(message) && JSON.parseObject(message).getBoolean("success")) {
                            JSONArray array = JSON.parseObject(message).getJSONArray("data");
                            if (ListUtils.isEmpty(array)) {
                                updataAdapter();
                                showToast(getString(R.string.common_list_empty));
                                return;
                            }
                            if (ListUtils.isEmpty(messages)) messages = new ArrayList<>();
                            if (msg.getData().getBoolean("isRefresh", false)) {
                                //下拉刷新
                                messages.clear();
                                reckonData(array, true);
                            } else {
                                //上啦加载
                                reckonData(array, false);
                            }
                        } else {
                            showToast(message == null ? "" : StringUtil.getChinese(message));
                        }
                        break;
                    case Constants.APP_SOCKETIMEOUTEXCEPTION:
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_subscribe, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.subscribe_manage) {
            Intent intent = new Intent();
            intent.setClass(this, SubcribeManageActivity.class);
            startActivity(intent);
        } else if (android.R.id.home == item.getItemId()) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription2);
        init();
        initView();
        initEvent();
    }


    private void initView() {
        swiperefresh = (SimpleRefreshLayout) findViewById(R.id.swiperefresh);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        ViewUtil.LoginERPTask(this, handler, 0x12);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ct);
        recycler.setLayoutManager(layoutManager);
        viewDivider = new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light));
        emptyDivider = new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 0, getResources().getColor(R.color.item_line));
        recycler.addItemDecoration(emptyDivider);
        adapter = new SubscriptionAdapter();
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        recycler.setItemAnimator(itemAnimator);
        recycler.setAdapter(adapter);
        loadData(true);//先数据库，后网络请求
    }

    private void init() {
        messages = new ArrayList<>();
        showTime = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " 00:00:00");
        baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
    }

    private void initEvent() {
//        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState != RecyclerView.SCROLL_STATE_IDLE || !isSlideToBottom()) return;
//
//            }
//        });
        swiperefresh.setEnabledPullUp(true);
        swiperefresh.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                showTime = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " 00:00:00");
                loadData(true);
            }

            @Override
            public void onLoadMore() {
                showTime -= daylong;
                loadData(false);//加载
            }
        });


        adapter.setOnClickListener(new SubscriptionAdapter.OnClickListener() {
            @Override
            public void click(View view, SubMessage bean, int position) {
                if (bean == null) bean = (SubMessage) view.getTag();
                if (bean == null) return;
                clickItem(bean, position);
            }
        });
        adapter.setOnLongClickListener(new SubscriptionAdapter.OnLongClickListener() {
            @Override
            public void longClick(View view, SubMessage bean, final int position) {
                if (bean == null) bean = (SubMessage) view.getTag();
                if (bean == null) return;
                final SubMessage finalBean = bean;
                PopupWindowHelper.showAlart(Subscription2Activity.this, getString(R.string.common_notice),
                        getString(R.string.delete_prompt), new PopupWindowHelper.OnSelectListener() {
                            @Override
                            public void select(boolean selectOk) {
                                if (selectOk) {
                                    longClickItem(finalBean, position);
                                }
                            }
                        });
            }
        });
    }

    public boolean isSlideToBottom() {
        if (recycler == null) return false;
        if (recycler.computeVerticalScrollExtent() + recycler.computeVerticalScrollOffset()
                >= recycler.computeVerticalScrollRange())
            return true;
        return false;
    }

    private void longClickItem(SubMessage bean, int position) {
        int i = position - 1;
        if (i >= 0 && i < messages.size()) {
            try {
                if (messages.get(i).getStatus() == 0) {//上一个为时间
                    if (position + 1 >= messages.size() || messages.get(position + 1).getStatus() == 0) {//下一个为时间或是没有
                        boolean delete = SubsDao.getInstance().deleteTime(messages.get(i));
                        if (delete) {
                            messages.remove(i);
                            position -= 1;
                        }
                    } else if (position + 1 < messages.size()) {
                        messages.get(position + 1).setStatus(1);
                        SubsDao.getInstance().upIsReadAndStatus(messages.get(position + 1));
                    }
                }
            } catch (Exception e) {

            }
        }
        boolean delete = SubsDao.getInstance().deleteByMessage(bean);
        if (delete) {
            messages.remove(position);
            updataAdapter();
            showToast(R.string.delete_all_succ);
        } else {
            showToast(R.string.delete_failed);
        }
    }

    private void clickItem(SubMessage bean, int position) {
        String url = baseUrl + "common/charts/mobileCharts.action";
        String title = bean.getTitle();
        Intent intent_web = new Intent("com.modular.main.WebViewCommActivity");
        intent_web.putExtra("url", url);
        intent_web.putExtra("p", title);
        intent_web.putExtra("cookie", true);
        intent_web.putExtra("subsact", "subsDetail");
        intent_web.putExtra("position", position);
        intent_web.putExtra("subsdata", messages);
        startActivityForResult(intent_web, REQUEST_SUBS_DETAIL);

    }

    private void loadData(boolean isRefresh) {
        if (isRefresh) {
            loadDataByNet(isRefresh);
        } else {
            if (!MyApplication.getInstance().isNetworkActive()) {
//                ViewUtil.ToastMessage(ct, ct.getResources().getString(R.string.networks_out), Style.holoRedLight, 2000);
                final List<SubMessage> chche = SubsDao.getInstance().queryByDate(TimeUtils.s_long_2_str(showTime));
                if (!ListUtils.isEmpty(chche)) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            messages.addAll(chche);
                            reckonData();
                        }
                    }, 500);
                }
                return;
            }
            loadDataByNet(isRefresh);
        }
    }

    //处理数据
    private void reckonData(JSONArray array, boolean isRef) {
        JSONObject object = null;
        SubMessage message = null;
        List<SubMessage> chche = new ArrayList<>();
        message = new SubMessage();
        message.setDate(TimeUtils.s_long_2_str(showTime));
        message.setRead(true);
        message.setStatus(0);
        chche.add(message);
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            message = new SubMessage();
            message.setCreateTime(object.getLong("CREATEDATE_"));
            int status = object.getInteger("STATUS_");
            message.setStatus(i == 0 ? 1 : 2);
            message.setTitle(object.getString("TITLE_"));
            message.setSubTitle(object.getString("SUMDATA_"));
            message.setRead(status == 0 ? false : true);
            message.setId(object.getInteger("ID_"));
            message.setNumId(object.getInteger("NUM_ID_"));
            message.setInstanceId(object.getInteger("INSTANCE_ID_"));
            if (message.getCreateTime() == 0) continue;
            message.setDate(TimeUtils.s_long_2_str(message.getCreateTime()));
            chche.add(message);
        }
        if (ListUtils.isEmpty(chche)) return;
        SubsDao.getInstance().createOrUpdata(chche);
        int start = messages.size();
        messages.addAll(chche);
        int end = messages.size();
        if (isRef)
            reckonData();
        else if (start == 0) {
            updataAdapter();
        } else
            adapter.notifyItemRangeInserted(start, end);
    }

    private void reckonData() {
        updataAdapter();
    }

    //从服务器获取数据
    private void loadDataByNet(boolean isRefresh) {
        if (!MyApplication.getInstance().isNetworkActive()) {
            ViewUtil.ToastMessage(ct, ct.getResources().getString(R.string.networks_out), Style.holoRedLight, 2000);
            return;
        }
        //获取网络数据
        String url = baseUrl + "common/desktop/subs/getSubs.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("count", 100);
        param.put("condition", "where to_char(createdate_,'yyyymmdd')='" + DateFormatUtil.long2Str(showTime, "yyyyMMdd") + "'");
        param.put("sessionId", CommonUtil.getSharedPreferences(this, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRefresh", isRefresh);
        ViewUtil.httpSendRequest(this, url, param, handler, headers, 0x11, message, bundle, "get");
    }

    private void stopFresh() {
        try {
            swiperefresh.stopRefresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //更新适配器数据
    private void updataAdapter() {
        stopFresh();
        if (adapter == null) {
            adapter = new SubscriptionAdapter();
            adapter.setMessages(messages);
            recycler.setAdapter(adapter);
        } else {
            adapter.setMessages(messages);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_SUBS_DETAIL && resultCode == 22 && data != null) {
                ArrayList<Integer> readSubs = data.getIntegerArrayListExtra("readsubs");
                List<SubMessage> chche = new ArrayList<>();
                boolean isUpdate = false;
                SubMessage e = null;
                for (int i = 0; i < readSubs.size(); i++) {
                    e = messages.get(readSubs.get(i));
                    if (e.isRead()) continue;
                    e.setRead(true);
                    isUpdate = true;
                    chche.add(e);
                }
                if (!isUpdate || ListUtils.isEmpty(chche)) {
                    Log.i("gongpengming", "ListUtils.isEmpty(chche)");
                    return;
                }
                SubsDao.getInstance().upIsReadByMessage(chche);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {

        }
    }
}
