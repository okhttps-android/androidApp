package com.xzjmyk.pm.activity.ui.platform.task;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.Blowfish;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.net.http.HttpClient;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.FlexJsonUtil;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.NewSchedultAdapter;
import com.modular.appmessages.model.AllProcess;
import com.modular.apputils.widget.MenuVoiceSearchView;
import com.uas.appworks.CRM.erp.activity.TaskAddErpActivity;
import com.uas.appworks.OA.erp.activity.DetailTaskActivity;
import com.xzjmyk.pm.activity.view.DivideRadioGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @注释：任务书
 * @Administrator 2014年10月10日 下午3:26:54
 */

@SuppressWarnings({"unused", "deprecation"})
public class TaskActivity extends SupportToolBarActivity implements OnClickListener {
    private final int PAGE_SIZE = 2000;
    private EmptyLayout mEmptyLayout;
    private Context context;
    private PullToRefreshListView listview_main;
    private NewSchedultAdapter adapter;
    private List<AllProcess> allList = new ArrayList<AllProcess>();
    private List<AllProcess> waitList = new ArrayList<>();
    private List<AllProcess> doneList = new ArrayList<>();
    private List<AllProcess> isendList = new ArrayList<>();
    private List<AllProcess> real_List = new ArrayList<>();
    private String login_Name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
    private HttpClient hClient;
    private String task_url = "jsps/mobile/task.jsp?caller=ResourceAssignment!Bill%26id=";

    private int tab_type = 1;
    private MenuVoiceSearchView voiceSearchView;
    private DivideRadioGroup tabGroup;

    private int mCurrentPage = 1;
    private int mPosition = -1;


    private Handler handler = new Handler() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    //allList = (ArrayList) msg.getData().getParcelableArrayList("accounts");
                    if (allList == null || allList.isEmpty()) {
                        mEmptyLayout.showEmpty();
                    } else {
                        adapter = new NewSchedultAdapter(TaskActivity.this, allList);
                        listview_main.setAdapter(adapter);
                    }
                    listview_main.onRefreshComplete();
                } else if (msg.what == 0) {
                    mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
                    mEmptyLayout.showError();
                    listview_main.onRefreshComplete();
                }
                if (msg.what == 2) {
                    progressDialog.dismiss();
                    String result = msg.getData().getString("result");
                    JSONObject dataroot = JSON.parseObject(result);
                    JSONArray data = dataroot.getJSONArray("data");
                    if (!ListUtils.isEmpty(allList)) allList.clear();
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            AllProcess process = new AllProcess();
                            JSONObject item = data.getJSONObject(i);
                            //process.setId(item.get("ra_id"));//id
                            process.setTaskid(String.valueOf(item.get("ra_id")));//id
                            process.setRecorder(item.getString("recorder"));//发起人
                            process.setStatus(item.getString("ra_status"));//状态
                            process.setMainname(item.getString("ra_status"));//状态
                            process.setTaskname(item.getString("ra_taskname"));//名称
                            process.setDatetime(item.getDate("ra_startdate"));//发起时间
                            process.setLink(task_url + item.get("ra_id"));
                            process.setTypecode(item.getString("ra_type"));
                            process.setDescribe(item.getString("description"));
                            process.setDuration(item.getString("duration"));
                            process.setRa_taskid(item.getString("ra_taskid"));
                            process.setDealpersoncode(item.getString("ra_resourcename"));
                            process.setRa_resourcecode(item.getString("ra_resourcecode"));
                            process.setTaskcode(item.getString("taskcode"));
                            process.setAttachs(item.getString("attachs"));
                            process.setEndTime(item.getString("ra_enddate"));
                            allList.add(process);
                        }

                        doClassifyAllList(allList);
                    } else {
                        mEmptyLayout.showEmpty();
                        listview_main.onRefreshComplete();
                    }
                }
                if (msg.what == LOAD_EM_NAME) {
                    String result = msg.getData().getString("result");
                    Log.i("Task", result + "");
                    JSONObject object = JSON.parseObject(result);
                    String em_name = object.getString("em_name");
                    String em_id = object.getString("em_id");
                    if (!StringUtil.isEmpty(em_name)) {
                        CommonUtil.setSharedPreferences(getApplicationContext(), "erp_emname", em_name);
                        CommonUtil.setSharedPreferences(getApplicationContext(), "erp_emid", em_id);
                    }
                }
                //请求超时  全局返回异常
                if (msg.what == Constants.APP_SOCKETIMEOUTEXCEPTION) {
                    String exception = msg.getData().getString("result");
                    mEmptyLayout.setErrorMessage(exception + "," + getString(R.string.task_refresh_again));
                    mEmptyLayout.showError();
                    listview_main.onRefreshComplete();
                    ViewUtil.AutoLoginErp(TaskActivity.this);
                    Log.i("login", "自动登录");
                    progressDialog.dismiss();
                }
            } catch (Exception e) {

            }
        }

    };
    private ViewGroup topFl;

    private void doClassifyAllList(List<AllProcess> allList) {
        if (mCurrentPage == 1) {
            if (!ListUtils.isEmpty(real_List)) real_List.clear();
            if (!ListUtils.isEmpty(waitList)) waitList.clear();
            if (!ListUtils.isEmpty(doneList)) doneList.clear();
            if (!ListUtils.isEmpty(isendList)) isendList.clear();
            ToastUtil.showToast(this, R.string.common_refresh_finish, topFl);
        } else {
            ToastUtil.showToast(this, R.string.common_up_finish, topFl);
        }
        listview_main.onRefreshComplete();
        if (ListUtils.isEmpty(allList)) {
            mEmptyLayout.showEmpty();
            return;
        }
        for (int i = 0; i < allList.size(); i++) {
            // 待处理
            if ((login_Name.equals(allList.get(i).getDealpersoncode()) &&
                    "进行中".equals(allList.get(i).getStatus()))
                    || (login_Name.equals(allList.get(i).getRecorder()) &&
                    "待确认".equals(allList.get(i).getStatus()))) {
                waitList.add(allList.get(i));
            }

            //已处理
            if ((login_Name.equals(allList.get(i).getDealpersoncode()) &&
                    "已完成".equals(allList.get(i).getStatus()))
                    || (login_Name.equals(allList.get(i).getDealpersoncode()) &&
                    "待确认".equals(allList.get(i).getStatus()))) {
                doneList.add(allList.get(i));
            }

            //我发起的
            if ((login_Name.equals(allList.get(i).getRecorder()) &&
                    "进行中".equals(allList.get(i).getStatus())) ||
                    (login_Name.equals(allList.get(i).getRecorder()) &&
                            "已完成".equals(allList.get(i).getStatus()))) {
                isendList.add(allList.get(i));
            }
            if (i == allList.size() - 1) {
                if (tab_type == 1) {
                    if (ListUtils.isEmpty(waitList)) {
                        mEmptyLayout.showEmpty();
                        return;
                    } else {
                        adapter = new NewSchedultAdapter(TaskActivity.this, waitList);
                        real_List = waitList;
                    }
                }
                if (tab_type == 2) {
                    adapter = new NewSchedultAdapter(TaskActivity.this, doneList);
                    real_List = doneList;
                }

                if (tab_type == 3) {
                    adapter = new NewSchedultAdapter(TaskActivity.this, isendList);
                    adapter.setiSend("iSend");
                    real_List = isendList;
                }
                listview_main.setAdapter(adapter);
                if (adapter.getCount() != 0) {
                    sendHomeBroadcast(adapter.getCount());
                } else {
                    mEmptyLayout.showEmpty();
                }
                listview_main.onRefreshComplete();
            }
        }

    }


    @Override
    public int getToolBarId() {
        return R.id.cycleCountToolBar;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.acativity_task_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = TaskActivity.this;
        ViewUtils.inject(this);
        // 表示已读
        listview_main = (PullToRefreshListView) findViewById(R.id.listview_schedult);   /**注释：initview */
        listview_main.setMode(PullToRefreshBase.Mode.BOTH);
        /**注释：自定义view */
        mEmptyLayout = new EmptyLayout(this, listview_main.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        //加载网络数据
        new Thread(new getallprocess_run()).start();
//        sendResquestForServer("START", 2);
        initHeaderView();//  头布局显示
        if (CommonUtil.isNetWorkConnected(ct)) {
            sendResquestForServerAll(2, mCurrentPage);  // update TODO :出息进来请求所有任务数据
        } else {
            ToastUtil.showToast(this, R.string.networks_out, tabGroup);
        }
        initSearchEvent();
        /**注释：监听 */
        listview_main.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                NewSchedultAdapter.Store store = (NewSchedultAdapter.Store) view.getTag();
                Intent intent = new Intent(context, DetailTaskActivity.class);
                intent.putExtra("description", store.tv_task_description);
                intent.putExtra("duration", store.tv_task_duration);
                intent.putExtra("status", store.tv_task_status);

                intent.putExtra("taskid", store.pTextView3);//编号
                intent.putExtra("taskname", store.pTextView4);//名称
                intent.putExtra("taskemcode", store.pTextView1);//发起人
                intent.putExtra("tasktime", store.pTextView2);//发起时间

                intent.putExtra("performer", store.tv_task_performer);//处理人
                intent.putExtra("emcode", store.tv_ra_resourcecode);//处理人编号
                intent.putExtra("taskcode", store.taskCode);//处理人编号
                intent.putExtra("ra_taskid", store.task_id);//取回复内容id
                intent.putExtra("attachs", store.attachs);
                intent.putExtra("endtime", store.tv_task_duration);
                Log.i("tasktime1,endtime", store.pTextView2+ "," + store.tv_task_duration);
                startActivityForResult(intent, 0x20);
            }

            /**
             * @param position
             */
            private void loadWebView(int position) {
                SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
                String password = setting.getString("user_password", null);
                String d = null;
                String t = null;
                if (password != null) {
                    d = "" + new Date().getTime();
                    Blowfish blowfish = new Blowfish("00" + d);
                    t = blowfish.encryptString(password);
                }
                Intent intent = new Intent("com.modular.main.WebViewLoadActivity");
                if (allList != null || !allList.isEmpty()) {
                    String url = allList.get(position - 1).getLink();
                    String master = allList.get(position - 1).getMaster();
                    if (master == null) {
                        master = CommonUtil.getSharedPreferences(context, "erp_master");
                    }
                    IntentUtils.webLinks(context, url, t);
                    intent.putExtra("url", url);
                    intent.putExtra("t", t);
                    intent.putExtra("d", d);
                    intent.putExtra("master", master);
                    startActivity(intent);

                }
            }
        });

//        listview_main.setOnRefreshListener(new OnRefreshListener<ListView>() {
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
////                refreshing();
//
//                listview_main.getLoadingLayoutProxy().setPullLabel("下拉刷新");
//                sendResquestForServerAll(2,mCurrentPage);
//            }
//        });

        listview_main.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
//                if (!ListUtils.isEmpty(waitList)) waitList.clear();
//                if (!ListUtils.isEmpty(doneList)) doneList.clear();
//                if (!ListUtils.isEmpty(isendList)) isendList.clear();
                mCurrentPage = 1;
                sendResquestForServerAll(2, mCurrentPage);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurrentPage++;
                sendResquestForServerAll(2, mCurrentPage);
            }
        });
    }

    private void initSearchEvent() {
        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                LogUtil.prinlnLongMsg("real_List", real_List + "");
                String strChche = editable.toString().replace(" ", "");//去除空格
                if (ListUtils.isEmpty(real_List)) {
                    mEmptyLayout.showEmpty();
                    return;
                }

                if (StringUtil.isEmpty(strChche)) {
                    adapter = new NewSchedultAdapter(TaskActivity.this, real_List);
                    listview_main.setAdapter(adapter);
                    if (adapter.getCount() != 0) {
                        sendHomeBroadcast(adapter.getCount());
                    } else {
                        mEmptyLayout.showEmpty();
                    }
                    listview_main.onRefreshComplete();
                }
                strChche = strChche.replace(" ", " ");//去除空格
                List<AllProcess> chche = new ArrayList<>();
                if (StringUtil.isEmpty(strChche)) return;

                for (int i = 0; i < real_List.size(); i++) {
                    boolean b = false;
                    try {
                        b = getResult(real_List.get(i).getRecorder() + real_List.get(i).getDealpersoncode()
                                + real_List.get(i).getStatus() + real_List.get(i).getTaskname()
                                + real_List.get(i).getEndTime() + real_List.get(i).getDatetime(), strChche.trim());
                    } catch (PatternSyntaxException e) {
                        e.printStackTrace();
                    }
                    if (b) {
                        chche.add(real_List.get(i));
                    }
                    if (i == real_List.size() - 1) {
                        adapter = new NewSchedultAdapter(TaskActivity.this, chche);
                        listview_main.setAdapter(adapter);
                        if (adapter.getCount() != 0) {
                            sendHomeBroadcast(adapter.getCount());
                        } else {
                            mEmptyLayout.showEmpty();
                        }
                        listview_main.onRefreshComplete();
                    }
                }
                Log.v("chche", JSON.toJSONString(chche) + "");
            }
        });
    }

    //正则
    private static boolean getResult(String text, String str) {
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(text);
        return m.find();
    }

    private void initHeaderView() {
        voiceSearchView = findViewById(R.id.mVoiceSearchView);
        findViewById(R.id.backImg).setOnClickListener(this);
        findViewById(R.id.addImg).setOnClickListener(this);
        tabGroup = findViewById(R.id.tabGroup);
        topFl = findViewById(R.id.topFl);
        final View todoLine = findViewById(R.id.todoLine);
        final View doneDealLine = findViewById(R.id.doneDealLine);
        final View byMeLine = findViewById(R.id.byMeLine);
        tabGroup.setOnCheckedChangeListener(new DivideRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DivideRadioGroup group, int checkedId) {
                todoLine.setVisibility(View.GONE);
                doneDealLine.setVisibility(View.GONE);
                byMeLine.setVisibility(View.GONE);
                if (checkedId == R.id.todoRb) {
                    todoLine.setVisibility(View.VISIBLE);
                    tab_type = 1;
                    adapter = new NewSchedultAdapter(TaskActivity.this, waitList);
                    real_List = waitList;
                } else if (checkedId == R.id.doneDealRb) {
                    doneDealLine.setVisibility(View.VISIBLE);
                    tab_type = 2;
                    adapter = new NewSchedultAdapter(TaskActivity.this, doneList);
                    real_List = doneList;
                } else if (checkedId == R.id.byMeRb) {
                    byMeLine.setVisibility(View.VISIBLE);
                    tab_type = 3;
                    adapter = new NewSchedultAdapter(TaskActivity.this, isendList);
                    adapter.setiSend("iSend");
                    real_List = isendList;
                }
                if (adapter == null) return;
                listview_main.setAdapter(adapter);
                if (adapter.getCount() != 0) {
                    sendHomeBroadcast(adapter.getCount());
                } else {
                    mEmptyLayout.showEmpty();
                }
                listview_main.onRefreshComplete();
            }
        });
        voiceSearchView.setOnClickListener(this);
        tab_type = 1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addImg:
                startActivityForResult(new Intent(context, TaskAddErpActivity.class), 0x17);
                break;
            case R.id.backImg:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getEmNameForServer();//网络请求
    }


    private void refreshing() {
        if (state_all == null || state_finish == null
                || state_start == null || state_taskforme == null
                || state_unconfirmed == null) {
            mEmptyLayout.showLoading();
            listview_main.getLoadingLayoutProxy().setPullLabel("下拉更新");
            sendResquestForServer("START", 2);
            return;
        }
        if (state_all.isSelected()) {
            mEmptyLayout.showLoading();
            listview_main.getLoadingLayoutProxy().setPullLabel("下拉更新");
            new Thread(new getallprocess_run()).start();
        }
        if (state_finish.isSelected()) {
            sendResquestForServer("FINISHED", 2);
        }
        if (state_start.isSelected()) {
            sendResquestForServer("START", 2);
        }
        if (state_unconfirmed.isSelected()) {
            sendResquestForServer("UNCONFIRMED", 2);
        }
        if (state_taskforme.isSelected()) {
            sendResquestForServerOther(2);
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
        System.out.println("json:" + reString);
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
                //更新首页条数
                intent_update.putExtra("countTotal", allProcesses.size() + "");

                for (int i = 0; i < allProcesses.size(); i++) {
                    if (allProcesses.get(i).getTypecode().contains("task")) {
                        taskProcess.add(allProcesses.get(i));
                    }

                }
                if (!taskProcess.isEmpty()) {
                    intent_update.putExtra("count", String.valueOf(taskProcess.size()));
                } else {
                    intent_update.putExtra("count", "0");
                }
                intent_update.putExtra("type", "task");
                sendBroadcast(intent_update);
                allList = taskProcess;
                // bundle.putParcelableArrayList("accounts", (ArrayList<? extends Parcelable>) taskProcess);
            }
            message.setData(bundle);
            message.what = 1;
            //handler.sendMessage(message);
        } else {
            Looper.prepare();
            //handler.sendEmptyMessage(0);
            Intent intent_update = new Intent("com.activity.update.data");
            intent_update.putExtra("count", "0");
            sendBroadcast(intent_update);
            Looper.loop();
        }
    }

    public void sendHomeBroadcast(int count) {
        Intent intent_update = new Intent("com.activity.update.data");
        intent_update.putExtra("count", String.valueOf(count));
        sendBroadcast(intent_update);
    }

    /**
     * @author LiuJie
     * 线程
     */
    public class getallprocess_run implements Runnable {
        @Override
        public void run() {
            if (!CommonUtil.isNetWorkConnected(ct)) {
                handler.sendEmptyMessage(0);
                return;
            }
            loadData();
        }
    }


    public void openNetSet() {
        Intent intent = null; //判断手机系统的版本  即API大于10 就是3.0或以上版本
        if (android.os.Build.VERSION.SDK_INT > 10) {
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        context.startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent("com.app.home.update");
        intent.putExtra("falg", "home");
        sendBroadcast(intent);
    }

    private void loadWebView(String url) {
        SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
        String password = setting.getString("user_password", null);
        String d = null;
        String t = null;
        if (password != null) {
            d = new Date().getTime() + "";
            Blowfish blowfish = new Blowfish("00" + d);
            t = blowfish.encryptString(password);
        }
        Intent intent = new Intent("com.modular.main.WebViewLoadActivity");
        intent.putExtra("url", url);
        intent.putExtra("t", t);
        intent.putExtra("d", d);
        intent.putExtra("p", "添加任务");
        intent.putExtra("master", CommonUtil.getSharedPreferences(context, "erp_master"));
        startActivity(intent);
    }


    private PopupWindow popupWindow = null;
    private TextView state_title;
    private Button state_all = null;
    private Button state_start = null;
    private Button state_finish = null;
    private Button state_unconfirmed = null;
    private Button state_add = null;
    private Button state_taskforme = null;

    private void showWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.act_pop_taskstate, null);
            state_start = (Button) view.findViewById(R.id.btn_state_START);
            state_finish = (Button) view.findViewById(R.id.btn_state_FINISHED);
            state_unconfirmed = (Button) view.findViewById(R.id.btn_state_UNCONFIRMED);
            state_add = (Button) view.findViewById(R.id.btn_state_ADD);
            state_all = (Button) view.findViewById(R.id.btn_state_ALL);
            state_taskforme = (Button) view.findViewById(R.id.btn_state_TASKFOME);
            // 创建一个PopuWidow对象
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight() / 3);
            state_start.setSelected(true);
            state_finish.setSelected(false);
            state_unconfirmed.setSelected(false);
            state_all.setSelected(false);
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        int xPos = windowManager.getDefaultDisplay().getWidth() / 2
                - popupWindow.getWidth() / 2;
        Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:"
                + windowManager.getDefaultDisplay().getWidth() / 2);
        Log.i("coder", "popupWindow.getWidth()/2:" + popupWindow.getWidth() / 2);
        Log.i("coder", "xPos:" + xPos);
        popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
        state_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResquestForServer("START", 2);
                popupWindow.dismiss();
                state_start.setSelected(true);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(false);
                state_taskforme.setSelected(false);
            }
        });
        state_finish.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendResquestForServer("FINISHED", 2);
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(true);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(false);
                state_taskforme.setSelected(false);
            }
        });
        state_unconfirmed.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendResquestForServer("UNCONFIRMED", 2);
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(true);
                state_all.setSelected(false);
                state_taskforme.setSelected(false);
            }
        });
        state_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();//loadWebView("jsps/mobile/addTask.jsp");
                TaskActivity.this.startActivityForResult(new Intent(context, TaskAddErpActivity.class), 0x17);
            }
        });

        state_taskforme.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                sendResquestForServerOther(2);
                state_start.setSelected(false);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(false);
                state_taskforme.setSelected(true);
            }
        });

        state_all.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(true);
                sendResquestForServerAll(2, mCurrentPage);
            }
        });
    }

    /**
     * @注释：状态码
     */
    public void sendResquestForServer(String status, int handlerWhat) {
        String url = CommonUtil.getAppBaseUrl(context) + "common/datalist/data.action";
        Map<String, Object> mparams = new HashMap<>();
        mparams.put("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"));
        mparams.put("caller", "ResourceAssignment");
        mparams.put("page", "1");
        mparams.put("pageSize", PAGE_SIZE);
        mparams.put("_noc", "1");
        String em_code = CommonUtil.getSharedPreferences(context, "erp_username");
        String erp_emid = CommonUtil.getSharedPreferences(context, "erp_emid");
        String condition = null;
        if ("START".equals(status)) {
            condition = "(ra_statuscode='UNCONFIRMED' and recorderid='" + erp_emid + "') or (ra_resourcecode='" + em_code + "' and ra_statuscode='START') and nvl(class,' ')<>'projecttask'";
        } else if ("UNCONFIRMED".equals(status)) {
            condition = "ra_resourcecode='" + em_code + "'" + " and recorderid='" + erp_emid + "'  and ra_statuscode='" + status + "'" + " and nvl(class,' ')<>'projecttask'";
        } else
            condition = "ra_resourcecode='" + em_code + "' and ra_statuscode='" + status + "'" + " and nvl(class,' ')<>'projecttask'";
        mparams.put("condition", condition);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"));
        ViewUtil.httpSendRequest(context, url, mparams, handler, headers, handlerWhat, null, null, "get");
    }


    /**
     * @注释： 带条件的调转
     */
    public void sendResquestForServerAll(int handlerWhat, int mCurrentPage) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(context) + "common/datalist/data.action";
        Map<String, Object> mparams = new HashMap<String, Object>();
        mparams.put("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"));
        mparams.put("caller", "ResourceAssignment");
        mparams.put("page", mCurrentPage);
        mparams.put("pageSize", PAGE_SIZE);
        mparams.put("_noc", "1");
        mparams.put("status", "");
        String em_code = CommonUtil.getSharedPreferences(context, "erp_username");
        String emName = CommonUtil.getSharedPreferences(context, "erp_emname");
        String condition = "(recorder='" + emName + "' or ra_resourcecode='" + em_code + "')" + " and nvl(class,' ')<>'projecttask'";
        mparams.put("condition", condition);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"));
        ViewUtil.httpSendRequest(context, url, mparams, handler, headers, handlerWhat, null, null, "get");
    }

    /**
     * @注释： 下属任务
     */
    public void sendResquestForServerOther(int handlerWhat) {
        String url = CommonUtil.getAppBaseUrl(context) + "common/datalist/data.action";
        Map<String, Object> mparams = new HashMap<String, Object>();
        mparams.put("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"));
        mparams.put("caller", "ResourceAssignment");
        mparams.put("page", "1");
        mparams.put("pageSize", PAGE_SIZE);
        mparams.put("_noc", "1");
        String em_code = CommonUtil.getSharedPreferences(context, "erp_emid");
        mparams.put("condition", "recorderid='" + em_code + "'" + "and handstatuscode<>'FINISHED'" + " and nvl(class,' ')<>'projecttask'");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"));
        ViewUtil.httpSendRequest(
                context, url,
                mparams,
                handler, headers, handlerWhat, null, null, "get");
    }

    public void sendDataToServer(String url, Map<String, String> param, int what) {
        url = CommonUtil.getAppBaseUrl(this) + url;
        param.put("sessionId", CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.startNetThread(this, url, param, handler, what,
                null, null, "get");
    }

    private final static int LOAD_EM_NAME = 6;

    private void getEmNameForServer() {
        String url = "mobile/getEmployeeByCode.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("em_code", CommonUtil.getSharedPreferences(this, "erp_username"));
        sendDataToServer(url, param, LOAD_EM_NAME);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x17) {
//            refreshing();
            //  录入一张单据后刷新所有界面
            if (!ListUtils.isEmpty(waitList)) waitList.clear();
            if (!ListUtils.isEmpty(doneList)) doneList.clear();
            if (!ListUtils.isEmpty(isendList)) isendList.clear();
            mCurrentPage = 1;
            sendResquestForServerAll(2, mCurrentPage);
        } else if (requestCode == 0x20 && resultCode == 0x20) {
            if (mPosition != -1 && !ListUtils.isEmpty(real_List)) {
                if (data != null) {
                    int type = data.getIntExtra("type", 0);
                    if (type == 0 || real_List.get(mPosition).getMainname().equals("待确认")) {
                        if (doneList == null)
                            doneList = new ArrayList<>();
                        real_List.get(mPosition).setMainname(getString(R.string.confirmed));
                        doneList.add(0, real_List.get(mPosition));
                        real_List.remove(mPosition);
                        adapter.notifyDataSetChanged();
                    } else {
                        real_List.get(mPosition).setMainname(getString(R.string.confirmed));
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    real_List.remove(mPosition);
                    adapter.notifyDataSetChanged();
                }
            }

        }
    }
}
