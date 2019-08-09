package com.xzjmyk.pm.activity.ui.platform.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.Blowfish;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.Friend;
import com.core.net.http.ViewUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.xmpp.dao.FriendDao;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.modular.appmessages.adapter.NewSchedultAdapter;
import com.modular.appmessages.model.AllProcess;
import com.uas.appworks.CRM.erp.activity.TaskAddErpActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.WebViewLoadActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.view.DivideRadioGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xzjmyk.pm.activity.util.oa.CommonUtil.getEmcode;

public class TaskB2BActivity extends SupportToolBarActivity implements View.OnClickListener {


    private EmptyLayout mEmptyLayout;
    private Context context;
    private PullToRefreshListView listview_main;
    //    private SchedultAdapter adapter;
    private NewSchedultAdapter adapter;
    private List<AllProcess> allList = new ArrayList<AllProcess>();
    private List<AllProcess> waitList = new ArrayList<>();
    private List<AllProcess> doneList = new ArrayList<>();
    private List<AllProcess> isendList = new ArrayList<>();
    private List<AllProcess> real_List = new ArrayList<>();
    private String login_Name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
    private int tab_type = 1;
    private DivideRadioGroup tabGroup;
    private VoiceSearchView voiceSearchView;
    private int mCurrentPage = 1;
    private int mPosition = -1;
    private Handler handler = new Handler() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 2) {
                    progressDialog.dismiss();
                    String result = msg.getData().getString("result");
                    JSONObject dataroot = JSON.parseObject(result);
                    JSONArray data = dataroot.getJSONArray("listdata");
                    if (!ListUtils.isEmpty(allList)) allList.clear();
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
//                            "detail":"测试",
//                                    "doman":"移动测试",
//                                    "domancode":1000009169,
//                                    "enddate":1489983120000,
//                                    "id":44,
//                                    "recorddate":1490057817693,
//                                    "recorder":"移动测试",
//                                    "recordercode":1000009169,
//                                    "startdate":1489972320000,
//                                    "status":"进行中",
//                                    "statuscode":"start",
//                                    "uu":10030994
                            AllProcess process = new AllProcess();
                            JSONObject item = data.getJSONObject(i);
                            process.setTaskid(String.valueOf(item.get("id")));//id
                            process.setRecorder(item.getString("recorder"));//发起人
                            process.setRecorderid(item.getString("recordercode"));//发起人
                            process.setStatus(item.getString("status"));//状态
                            process.setMainname(item.getString("status"));//状态
                            process.setTaskname(item.getString("taskname"));//名称
                            process.setDatetime(item.getDate("startdate"));//发起时间
                            // process.setLink(task_url + item.get("ra_id"));
                            process.setTypecode("task");
                            process.setDescribe(item.getString("detail"));
                            process.setDuration(item.getString("duration"));
                            process.setRa_taskid(String.valueOf(item.get("id")));
                            process.setDealpersoncode(item.getString("doman"));
                            process.setRa_resourcecode(item.getString("domancode"));
                            process.setTaskcode(item.getString("taskcode"));
                            process.setAttachs(item.getString("attachs"));
                            process.setEndTime(item.getString("enddate"));
                            allList.add(process);
                        }
                        doClassifyAllList(allList); // TODO 分类
                    } else {
                        sendServiceForISend(mCurrentPage);
                        mEmptyLayout.showEmpty();
                        listview_main.onRefreshComplete();
                    }
                }

                if (msg.what == 3) {
                    progressDialog.dismiss();
                    String isendresult = msg.getData().getString("result");
                    JSONObject dataroot = JSON.parseObject(isendresult);
                    JSONArray data = dataroot.getJSONArray("listdata");
                    LogUtil.prinlnLongMsg("isendresult", isendresult);
                    if (!ListUtils.isEmpty(data)) {
                        String emCode = CommonUtil.getEmcode();
                        boolean isref = false;
                        for (int i = 0; i < data.size(); i++) {
//                            "detail":"测试",
//                                    "doman":"移动测试",
//                                    "domancode":1000009169,
//                                    "enddate":1489983120000,
//                                    "id":44,
//                                    "recorddate":1490057817693,
//                                    "recorder":"移动测试",
//                                    "recordercode":1000009169,
//                                    "startdate":1489972320000,
//                                    "status":"进行中",
//                                    "statuscode":"start",
//                                    "uu":10030994
                            AllProcess process = new AllProcess();
                            JSONObject item = data.getJSONObject(i);
                            process.setTaskid(String.valueOf(item.get("id")));//id
                            process.setRecorder(item.getString("recorder"));//发起人
                            process.setRecorderid(item.getString("recordercode"));//发起人
                            process.setStatus(item.getString("status"));//状态
                            process.setMainname(item.getString("status"));//状态
                            process.setTaskname(item.getString("taskname"));//名称
                            process.setDatetime(item.getDate("startdate"));//发起时间
                            // process.setLink(task_url + item.get("ra_id"));
                            process.setTypecode("task");
                            process.setDescribe(item.getString("detail"));
                            process.setDuration(item.getString("duration"));
                            process.setRa_taskid(String.valueOf(item.get("id")));
                            process.setDealpersoncode(item.getString("doman"));
                            process.setRa_resourcecode(item.getString("domancode"));
                            process.setTaskcode(item.getString("taskcode"));
                            process.setAttachs(item.getString("attachs"));
                            process.setEndTime(item.getString("enddate"));
                            if ("待确认".equals(process.getStatus()) && process.getRecorderid().equals(emCode)
                                    && !process.getRa_resourcecode().equals(emCode)) {
                                waitList.add(process);
                                isref = true;
                            }
                            isendList.add(process);

                            if (i == data.size() - 1) {
                                LogUtil.prinlnLongMsg("isendList3", JSON.toJSONString(isendList));
                                doShowListData();
                            }
                        }
                        if (isref && tab_type == 1) {
                            sort();
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        doShowListData();
                    }
                }
                if (msg.what == Constants.APP_SOCKETIMEOUTEXCEPTION) {
                    String exception = msg.getData().getString("result");
                    mEmptyLayout.setErrorMessage(exception);
                    mEmptyLayout.showError();
                    listview_main.onRefreshComplete();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void doShowListData() {
        if (tab_type == 1) {
            adapter = new NewSchedultAdapter(TaskB2BActivity.this, waitList);
            real_List = waitList;
        }

        if (tab_type == 2) {
            adapter = new NewSchedultAdapter(TaskB2BActivity.this, doneList);
            real_List = doneList;
        }

        if (tab_type == 3) {
            adapter = new NewSchedultAdapter(TaskB2BActivity.this, isendList);
            real_List = isendList;
            adapter.setiSend("iSend");
        }
        LogUtil.prinlnLongMsg("tab_type ,real_List", tab_type + "," + JSON.toJSONString(real_List));
        listview_main.setAdapter(adapter);
        if (adapter.getCount() != 0) {
            sendHomeBroadcast(adapter.getCount());
        } else {
            mEmptyLayout.showEmpty();
        }
        listview_main.onRefreshComplete();
    }

    private void doClassifyAllList(List<AllProcess> allList) {
        if (mCurrentPage == 1) {
            if (!ListUtils.isEmpty(real_List)) real_List.clear();
            ToastMessage("刷新成功");
        } else {
            ToastMessage("加载完毕");
        }
        listview_main.onRefreshComplete();
        LogUtil.prinlnLongMsg("login_Name,allList", login_Name + JSON.toJSONString(allList));
        String emcode = getEmcode();
        if (ListUtils.isEmpty(allList)) return;
        for (int i = 0; i < allList.size(); i++) {
            // 待处理
            if ((emcode.equals(allList.get(i).getRa_resourcecode()) &&
                    "进行中".equals(allList.get(i).getStatus()))
                    || (emcode.equals(allList.get(i).getRecorderid()) &&
                    "待确认".equals(allList.get(i).getStatus()))) {
                waitList.add(allList.get(i));
            }

            //已处理
            if ((emcode.equals(allList.get(i).getRa_resourcecode()) &&
                    "已完成".equals(allList.get(i).getStatus()))
                    || (!emcode.equals(allList.get(i).getRecorderid()) &&
                    "待确认".equals(allList.get(i).getStatus()))) {
                doneList.add(allList.get(i));
            }

            //我发起的
            if (emcode.equals(allList.get(i).getRecorderid()) && !"待确认".equals(allList.get(i).getStatus())) {
                isendList.add(allList.get(i));
            }
            if (i == allList.size() - 1) {
                LogUtil.prinlnLongMsg("waitList", JSON.toJSONString(waitList));
                LogUtil.prinlnLongMsg("doneList", JSON.toJSONString(doneList));
                LogUtil.prinlnLongMsg("isendList", JSON.toJSONString(isendList));
                sendServiceForISend(mCurrentPage);  //  进来请求我发起的任务数据
            }
        }
        sort();
    }

    private void sort() {
        Comparator comparator = new Comparator<AllProcess>() {
            @Override
            public int compare(AllProcess lhs, AllProcess rhs) {
                return (int) (rhs.getDatetime().getTime() - lhs.getDatetime().getTime());
            }
        };
        if (!ListUtils.isEmpty(isendList))
            Collections.sort(isendList, comparator);
        if (!ListUtils.isEmpty(waitList))
            Collections.sort(waitList, comparator);
        if (!ListUtils.isEmpty(doneList))
            Collections.sort(doneList, comparator);
    }
    @Override
    public int getToolBarId() {
        return com.modular.appmessages.R.id.cycleCountToolBar;
    }

    @Override
    public int getLayoutRes() {
        return com.modular.appmessages.R.layout.acativity_task_list;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = TaskB2BActivity.this;
        ViewUtils.inject(this);
        setTitle("待办工作");
        String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();

        FriendDao.getInstance().markUserMessageRead(mLoginUserId, Friend.ID_ERP_TASK);
        listview_main = (PullToRefreshListView) findViewById(R.id.listview_schedult);
        listview_main.setMode(PullToRefreshBase.Mode.BOTH);

        mEmptyLayout = new EmptyLayout(this, listview_main.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.showEmpty();
//        mEmptyLayout.showLoading();
//        sendResquestForServer("进行中", 2,mCurrentPage);
        initHeaderView();//  头布局显示
        initData();
        initSearchEvent();
        listview_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                NewSchedultAdapter.Store store = (NewSchedultAdapter.Store) view.getTag();
                Intent intent = new Intent(context, TaskDetailB2BActivity.class);
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
                intent.putExtra("endtime", DateFormatUtil.long2Str(Long.valueOf(store.tv_task_duration), DateFormatUtil.YMD_HMS));
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
                Intent intent = new Intent(context, WebViewLoadActivity.class);
                if (allList != null || !allList.isEmpty()) {
                    String url = allList.get(position - 1).getLink();
                    String master = allList.get(position - 1).getMaster();
                    if (master == null) {
                        master = CommonUtil.getSharedPreferences(context, "erp_master");
                    }
                    intent.putExtra("url", url);
                    intent.putExtra("t", t);
                    intent.putExtra("d", d);
                    intent.putExtra("master", master);
                    startActivity(intent);

                }
            }
        });

//        listview_main.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
////                refreshing();
//                listview_main.getLoadingLayoutProxy().setPullLabel("下拉刷新");
////                if (tab_type == 3){
////                    sendServiceForISend();
////                }else {
//                sendResquestForServer("", 2,mCurrentPage);
////                }
//            }
//        });
        listview_main.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!ListUtils.isEmpty(waitList)) waitList.clear();
                if (!ListUtils.isEmpty(doneList)) doneList.clear();
                if (!ListUtils.isEmpty(isendList)) isendList.clear();
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
        progressDialog.show();
        sendResquestForServer("", 2, mCurrentPage); //  进来请求所有任务数据
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
                    adapter = new NewSchedultAdapter(TaskB2BActivity.this, real_List);
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
                    boolean b = getResult(real_List.get(i).getRecorder() + real_List.get(i).getDealpersoncode()
                            + real_List.get(i).getStatus() + real_List.get(i).getTaskname()
                            + real_List.get(i).getEndTime() + real_List.get(i).getDatetime(), strChche.trim());

                    if (b) {
                        chche.add(real_List.get(i));
                    }
                    if (i == real_List.size() - 1) {
                        adapter = new NewSchedultAdapter(TaskB2BActivity.this, chche);
                        listview_main.setAdapter(adapter);
                        if (adapter.getCount() != 0) {
                            sendHomeBroadcast(adapter.getCount());
                        } else {
                            mEmptyLayout.showEmpty();
                        }
                        listview_main.onRefreshComplete();
                    }
                }
                Log.v("chche", JSON.toJSONString(chche));
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
        voiceSearchView = findViewById(com.modular.appmessages.R.id.mVoiceSearchView);
        findViewById(com.modular.appmessages.R.id.backImg).setOnClickListener(this);
        findViewById(com.modular.appmessages.R.id.addImg).setOnClickListener(this);
        tabGroup = findViewById(com.modular.appmessages.R.id.tabGroup);
        final View todoLine = findViewById(com.modular.appmessages.R.id.todoLine);
        final View doneDealLine = findViewById(com.modular.appmessages.R.id.doneDealLine);
        final View byMeLine = findViewById(com.modular.appmessages.R.id.byMeLine);
        tabGroup.setOnCheckedChangeListener(new DivideRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DivideRadioGroup group, int checkedId) {
                todoLine.setVisibility(View.GONE);
                doneDealLine.setVisibility(View.GONE);
                byMeLine.setVisibility(View.GONE);
                if (checkedId == com.modular.appmessages.R.id.todoRb) {
                    todoLine.setVisibility(View.VISIBLE);
                    tab_type = 1;
                    adapter = new NewSchedultAdapter(ct, waitList);
                    real_List = waitList;
                } else if (checkedId == com.modular.appmessages.R.id.doneDealRb) {
                    doneDealLine.setVisibility(View.VISIBLE);
                    tab_type = 2;
                    adapter = new NewSchedultAdapter(ct, doneList);
                    real_List = doneList;
                } else if (checkedId == com.modular.appmessages.R.id.byMeRb) {
                    byMeLine.setVisibility(View.VISIBLE);
                    tab_type = 3;
                    adapter = new NewSchedultAdapter(ct, isendList);
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


    private void refreshing() {
        if (state_all == null || state_finish == null
                || state_start == null || state_taskforme == null
                || state_unconfirmed == null) {
            mEmptyLayout.showLoading();
            listview_main.getLoadingLayoutProxy().setPullLabel("下拉更新");
            sendResquestForServer("进行中", 2, mCurrentPage);
            return;
        }
        if (state_all.isSelected()) {
            mEmptyLayout.showLoading();
            listview_main.getLoadingLayoutProxy().setPullLabel("下拉更新");
//            new Thread(new getallprocess_run()).start();
        }
        if (state_finish.isSelected()) {
            sendResquestForServer("FINISHED", 2, mCurrentPage);
        }
        if (state_start.isSelected()) {
            sendResquestForServer("进行中", 2, mCurrentPage);
        }
        if (state_unconfirmed.isSelected()) {
            sendResquestForServer("UNCONFIRMED", 2, mCurrentPage);
        }

    }


    public void sendHomeBroadcast(int count) {
        Intent intent_update = new Intent("com.activity.update.data");
        intent_update.putExtra("count", String.valueOf(count));
        sendBroadcast(intent_update);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent("com.app.home.update");
        intent.putExtra("falg", "home");
        sendBroadcast(intent);
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
            state_taskforme.setVisibility(View.GONE);
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
        state_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResquestForServer("进行中", 2, mCurrentPage);
                popupWindow.dismiss();
                state_start.setSelected(true);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(false);
                state_taskforme.setSelected(false);
            }
        });
        state_finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendResquestForServer("已完成", 2, mCurrentPage);
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(true);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(false);
                state_taskforme.setSelected(false);
            }
        });
        state_unconfirmed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendResquestForServer("待确认", 2, mCurrentPage);
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(true);
                state_all.setSelected(false);
                state_taskforme.setSelected(false);
            }
        });
        state_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();//loadWebView("jsps/mobile/addTask.jsp");
                startActivityForResult(new Intent(context, TaskAddB2BActivity.class), 0x17);
            }
        });

        state_taskforme.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(false);
                state_taskforme.setSelected(true);
            }
        });

        state_all.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                state_start.setSelected(false);
                state_finish.setSelected(false);
                state_unconfirmed.setSelected(false);
                state_all.setSelected(true);
                sendResquestForServer("", 2, mCurrentPage);
            }
        });
    }

    /**
     * @注释：状态码
     */
    public void sendResquestForServer(String status, int handlerWhat, int mCurrentPage) {
        progressDialog.show();
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_list;
        Map<String, Object> params = new HashMap<String, Object>();
        params = new HashMap<>();
        params.put("emuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));
        params.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        params.put("pageNumber", mCurrentPage);
        params.put("pageSize", "100");
        params.put("status", status);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(context, url, params, handler, headers, handlerWhat, null, null, "get");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_state, menu);
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.reset) {
//            View view = getWindow().findViewById(item.getItemId());
//            showWindow(view);
//        }
        if (item.getItemId() == R.id.add_item) {
            startActivityForResult(new Intent(context, TaskAddB2BActivity.class), 0x17);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x17) {
//            refreshing();
            if (!ListUtils.isEmpty(waitList)) waitList.clear();
            if (!ListUtils.isEmpty(doneList)) doneList.clear();
            if (!ListUtils.isEmpty(isendList)) isendList.clear();
            mCurrentPage = 1;
            sendResquestForServer("", 2, mCurrentPage); // update
        } else if (requestCode == 0x20 && resultCode == 0x20) {
            if (mPosition != -1 && !ListUtils.isEmpty(real_List)) {
                if (data != null) {
                    int type = data.getIntExtra("type", 0);
                    if (type == 0 || real_List.get(mPosition).getMainname().equals("待确认")) {
                        if (doneList == null)
                            doneList = new ArrayList<>();
                        real_List.get(mPosition).setMainname("已完成");
                        doneList.add(0, real_List.get(mPosition));
                        real_List.remove(mPosition);
                        adapter.notifyDataSetChanged();
                    } else {
                        real_List.get(mPosition).setMainname("待确认");
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    real_List.remove(mPosition);
                    adapter.notifyDataSetChanged();
                }
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addImg:
                startActivityForResult(new Intent(context, TaskAddErpActivity.class), 0x17);
                break;
            case  R.id.backImg:
                onBackPressed();
                break;
        }
    }

    private void sendServiceForISend(int mCurrentPage) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().plat_isend_task;
        Map<String, Object> params = new HashMap<String, Object>();
        params = new HashMap<>();
        params.put("emuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));
        params.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        params.put("pageNumber", mCurrentPage);
        params.put("pageSize", "100");
        params.put("status", "");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(context, url, params, handler, headers, 3, null, null, "get");
    }


}
