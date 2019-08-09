package com.xzjmyk.pm.activity.ui.erp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

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
import com.modular.appmessages.adapter.SchedultAdapter;
import com.xzjmyk.pm.activity.R;
import com.modular.appmessages.model.AllProcess;
import com.xzjmyk.pm.activity.ui.erp.net.HttpClient;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @注释：知会
 */
@SuppressWarnings("deprecation")
public class InformActivity extends BaseActivity {

    private EmptyLayout mEmptyLayout;
    private PullToRefreshListView listview_main;
    private SchedultAdapter adapter;
    private List<AllProcess> allList;
    private HttpClient hClient;

    private OnClickListener mErrorClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mEmptyLayout.showLoading();
            listview_main.setRefreshing(true);
            new Thread(new getallprocess_run()).start();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                allList = (ArrayList) msg.getData().getParcelableArrayList("accounts");
                if (allList == null || allList.isEmpty()) {
                    mEmptyLayout.showEmpty();
                } else {
                    adapter = new SchedultAdapter(InformActivity.this, allList);
                    listview_main.setAdapter(adapter);
                }
                listview_main.onRefreshComplete();
                System.out.println("执行时间：" + CommonUtil.getStringDate(System.currentTimeMillis()));
            } else if (msg.what == 0) {
                mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
                mEmptyLayout.showError();
                listview_main.onRefreshComplete();
            }
        }
        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informed_listview);
        ViewUtils.inject(this);
        setTitle("我的知会");
        String  mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        // 表示已读
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, Friend.ID_ERP_ZHIHUI);
        listview_main = (PullToRefreshListView) findViewById(R.id.listview_informed);
        mEmptyLayout = new EmptyLayout(this, listview_main.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyButtonClickListener(mErrorClickListener);
        mEmptyLayout.showLoading();
        new Thread(new getallprocess_run()).start();
        listview_main.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
                String password = setting.getString("user_password", null);
                String d = null;
                String t = null;
                if (password != null) {
                    d = "" + new Date().getTime();
                    Blowfish blowfish = new Blowfish("00" + d);
                    t = blowfish.encryptString(password);
                }
                       Intent intent =new Intent(InformActivity.this, WebViewLoadActivity.class);
					   if (allList!=null||!allList.isEmpty()) {
						   String url=allList.get(position-1).getLink();
						   System.out.println("url="+url);
						   String master=allList.get(position-1).getMaster();
						   intent.putExtra("url", url);
						   intent.putExtra("t",t);
						   intent.putExtra("d", d);
						   intent.putExtra("master", master);
						   startActivity(intent);
						  
					}
            }
        });

        listview_main.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mEmptyLayout.showLoading();
                new Thread(new getallprocess_run()).start();
            }
        });
    }

    public class afterEventOnclik implements Runnable {
        private String url;
        public afterEventOnclik(String url) {
            super();
            this.url = url;
        }

        @Override
        public void run() {
            HandlerInformAfter(url);
        }
    }

    public class getallprocess_run implements Runnable {
        @Override
        public void run() {
            System.out.println("执行时间：" + CommonUtil.getStringDate(System.currentTimeMillis()));
            loadData();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
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
                /**注释：取出任务书 */
                List<AllProcess> taskProcess = new ArrayList<AllProcess>();
                for (int i = 0; i < allProcesses.size(); i++) {
                    if (allProcesses.get(i).getTypecode().contains("pagingrelease")) {
                        taskProcess.add(allProcesses.get(i));
                    }
                    ;
                }

                if (!taskProcess.isEmpty()) {
                    intent_update.putExtra("count", taskProcess.size());
                } else {
                    intent_update.putExtra("count", "0");
                }
                intent_update.putExtra("type", "pagingrelease");
                sendBroadcast(intent_update);
                allList = taskProcess;
            }
            message.setData(bundle);
            message.what = 1;
            handler.sendMessage(message);
        } else {
            Looper.prepare();
            handler.sendEmptyMessage(0);
            Intent intent_update = new Intent("com.activity.update.data");
            intent_update.putExtra("count", "0");
            sendBroadcast(intent_update);
            Looper.loop();
        }

    }

    private void HandlerInformAfter(String url) {
        hClient = new HttpClient();
        String reString = null;
        try {
            reString = hClient.sendPostRequest(url, null);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (reString != null) {
            Map<String, Object> results = FlexJsonUtil.fromJson(reString);
            Looper.prepare();
            if ((Boolean) results.get("success")) {
                Toast.makeText(this, "处理成功！", Toast.LENGTH_LONG).show();
                new Thread(new getallprocess_run()).start();
            } else {
                Toast.makeText(this, "处理成功！", Toast.LENGTH_LONG).show();
            }
            Looper.loop();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent("com.app.home.update");
        intent.putExtra("falg", "home");
        sendBroadcast(intent);
    }
}