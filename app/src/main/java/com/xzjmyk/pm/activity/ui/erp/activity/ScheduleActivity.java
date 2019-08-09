package com.xzjmyk.pm.activity.ui.erp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.common.data.Blowfish;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.Friend;
import com.core.utils.FlexJsonUtil;
import com.core.widget.EmptyLayout;
import com.core.xmpp.dao.FriendDao;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.modular.appmessages.adapter.NewSchedultAdapter;
import com.xzjmyk.pm.activity.R;
import com.modular.appmessages.model.AllProcess;
import com.xzjmyk.pm.activity.ui.erp.net.HttpClient;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @注释：代办事宜
 * @Administrator 2014年10月10日 上午10:05:02
 */
@SuppressWarnings("deprecation")
@SuppressLint("HandlerLeak")
public class ScheduleActivity extends BaseActivity {

    private EmptyLayout mEmptyLayout;
    private PullToRefreshListView listview_main;
//    private SchedultAdapter adapter;
    private NewSchedultAdapter adapter;
    private List<AllProcess> allList;
    private HttpClient hClient;


    private Handler handler = new Handler() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //allList= (ArrayList)msg.getData().getParcelableArrayList("accounts");
                if (allList == null || allList.isEmpty()) {
                    Log.i("Arison", "加载数据为空！");
                    adapter = new NewSchedultAdapter(ScheduleActivity.this, allList);
                    listview_main.setAdapter(adapter);
                    mEmptyLayout.showEmpty();
                } else {
                    Log.i("Arison", "加载数据！size=" + allList.size());
                    adapter = new NewSchedultAdapter(ScheduleActivity.this, allList);
                    listview_main.setAdapter(adapter);
                }

                listview_main.onRefreshComplete();
            } else if (msg.what == 0) {
                mEmptyLayout.setErrorMessage("服务器异常！请刷新重试！");
                mEmptyLayout.showError();
                listview_main.onRefreshComplete();
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_listview);
        ViewUtils.inject(this);
        listview_main = (PullToRefreshListView) findViewById(R.id.listview_schedult);
      setTitle("待审批流");
        String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        // 表示已读
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, Friend.ID_ERP_PROCESS);

        mEmptyLayout = new EmptyLayout(this, listview_main.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.showLoading();

        listview_main.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
                        String password = setting.getString("user_password", null);
                        String d = null, t = null;
                        if (password != null) {
                            d = "" + new Date().getTime();
                            Blowfish blowfish = new Blowfish("00" + d);
                            t = blowfish.encryptString(password);
                        }
                        Intent intent = new Intent(ScheduleActivity.this, WebViewLoadActivity.class);
                        if (allList != null || !allList.isEmpty()) {
                            String url = allList.get(position - 1).getLink();
                            String master = allList.get(position - 1).getMaster();
                            intent.putExtra("url", url);
                            intent.putExtra("t", t);
                            intent.putExtra("d", d);
                            intent.putExtra("master", master);
                            startActivity(intent);
//
                        }
                    }
                }, 0);
            }
        });

        listview_main.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loaddata();
            }
        });
    }

    public class getallprocess_run implements Runnable {
        @Override
        public void run() {
            loadData();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        Looper.prepare();
        hClient = new HttpClient();
        String reString = null;
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/getallprocess.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("employeeCode", CommonUtil.getSharedPreferences(this, "erp_username"));
        param.put("currentMaster", CommonUtil.getSharedPreferences(this, "erp_master"));
        param.put("sessionId", CommonUtil.getSharedPreferences(this, "sessionId"));
        try {
            reString = hClient.sendGetRequest(url, param);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Message message = new Message();
        Bundle bundle = new Bundle();
        if (reString != null && !reString.equals("500")) {
            Map<String, Object> results = FlexJsonUtil.fromJson(reString);
            if (results.get("allProcess") != null) {
                String count = results.get("count").toString();
                Intent intent_update = new Intent("com.activity.update.data");
                getSharedPreferences("setting", MODE_PRIVATE).edit().putString("count", count).commit();
                List<AllProcess> allProcesses = new ArrayList<AllProcess>();
                try {
                    allProcesses = FlexJsonUtil.fromJsonArray(FlexJsonUtil.toJson(results.get("allProcess")), AllProcess.class);
                } catch (Exception e) {
                }
                List<AllProcess> taskProcess = new ArrayList<AllProcess>();
                for (int i = 0; i < allProcesses.size(); i++) {
                    if (!allProcesses.get(i).getTypecode().contains("task") && !allProcesses.get(i).getTypecode().contains("pagingrelease") && !allProcesses.get(i).getTypecode().contains("dingyue")) {
                        taskProcess.add(allProcesses.get(i));
                    }
                    ;
                }
                if (!taskProcess.isEmpty()) {
                    intent_update.putExtra("count", String.valueOf(taskProcess.size()));
                } else {
                    intent_update.putExtra("count", "0");
                }
                intent_update.putExtra("type", "daiban");
                intent_update.putExtra("totalcount", count);
                sendBroadcast(intent_update);
                // bundle.putParcelableArrayList("accounts", (ArrayList<? extends Parcelable>) taskProcess);
                allList = taskProcess;
            }
            message.setData(bundle);
            message.what = 1;
            handler.sendMessage(message);
        } else {
            //Toast.makeText(ScheduleActivity.this, "网络请求失败，请重新请求", Toast.LENGTH_LONG).show();
            handler.sendEmptyMessage(0);
            Intent intent_update = new Intent("com.activity.update.data");
            intent_update.putExtra("count", "0");
            sendBroadcast(intent_update);
        }
        Looper.loop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private OnrenewListener listener = null;

    public interface OnrenewListener {
        public void updata(int num);
    }

    public void setOnrenewListener(OnrenewListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.app.home.update");
        intent.putExtra("falg", "home");
        sendBroadcast(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        listview_main.setRefreshing(true);
        loaddata();
    }

    /**
     * @author Administrator
     * @功能:统一开启线程加载数据
     */
    private void loaddata() {
        mEmptyLayout.showLoading();
        listview_main.setRefreshing(true);
        if (CommonUtil.isNetWorkConnected(this)) {
            new Thread(new getallprocess_run()).start();
            Log.i("Arison", "initData()");
        } else {
            listview_main.onRefreshComplete();
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }
    }

}
