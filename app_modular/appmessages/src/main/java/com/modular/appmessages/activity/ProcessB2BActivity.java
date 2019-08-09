package com.modular.appmessages.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.modular.appmessages.R;
import com.uas.appworks.OA.platform.activity.PlatWDdetailyActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 不确定问题的出现与否，没有删除所有分页写法，只是屏蔽了上拉事件
 * update Bitliker by 17/4/10
 */
public class ProcessB2BActivity extends BaseActivity implements View.OnClickListener {


    private EmptyLayout mEmptyLayout;
    VoiceSearchView voiceSearchView;
    PullToRefreshListView mList;
    private TextView tv_process_me;
    private TextView tv_process_already;
    private TextView tv_process_un;
    private ImageView iv_back;

    private ProcessAdapter mAdapter;
    private JSONArray arrayUn = new JSONArray();
    private JSONArray arrayRe = new JSONArray();
    private JSONArray arrayMe = new JSONArray();

    private int tab_type = 1;
    private int page = 1;
    private int pageDone = 1;
    private final int SUSSCESS_un = 1;
    private final int SUSSCESS_already = 2;
    private final int SUSSCESS_me = 3;
    private int exceptionCount = 0;//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            LogUtil.i("data == null=");
            return;
        }
        boolean isRemove = data.getBooleanExtra("isRemove", false);
        LogUtil.i("isRemove=" + isRemove);
        if (isRemove) {
            if (requestCode == 0x322 || requestCode == 0x321) {
                if (!ListUtils.isEmpty(arrayUn) && arrayUn.size() > mPosition) {
                    LogUtil.i("notifyDataSetChanged");
                    arrayUn.remove(mPosition);
                    mAdapter.jsonArray = arrayUn;
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mList.onRefreshComplete();
            progressDialog.dismiss();
            String result = msg.getData().getString("result");
            if (StringUtil.isEmpty(result)) return;
            switch (msg.what) {
                case SUSSCESS_un:
                    itemArray = JSON.parseObject(result).getJSONArray("data");
                    if (page == 1) arrayUn.clear();
                    arrayUn.addAll(itemArray);
//                    array = CommonUtil.sortJsonArray(array);//排序
                    if (mAdapter == null) {
                        mAdapter = new ProcessAdapter(ct, arrayUn);
                        mList.setAdapter(mAdapter);
                    } else {
                        mAdapter.setJsonArray(arrayUn);
                        mAdapter.notifyDataSetChanged();
                    }
                    if (arrayUn.size() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    if (currentId != 0) {
                        mList.getRefreshableView().setSelection(currentId);
                    }
                    ToastMessage("数据刷新完毕");
                    progressDialog.dismiss();
                    tv_process_already.setEnabled(true);
                    break;
                case SUSSCESS_already:
                    itemArray = JSON.parseObject(result).getJSONArray("data");
                    if (pageDone == 1) arrayRe.clear();
                    arrayRe.addAll(itemArray);
                    if (mAdapter == null) {
                        mAdapter = new ProcessAdapter(ct, arrayRe);
                        mList.setAdapter(mAdapter);
                    } else {
                        mAdapter.setJsonArray(arrayRe);
                        mAdapter.notifyDataSetChanged();
                    }
                    if (arrayRe.size() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    if (currentId != 0) {
                        mList.getRefreshableView().setSelection(currentId);
                    }
                    ToastMessage("数据刷新完毕");
                    progressDialog.dismiss();
                    break;
                case SUSSCESS_me:
                    itemArray = JSON.parseObject(result).getJSONArray("data");
                    // if (page == 1)
                    arrayMe.clear();
                    arrayMe.addAll(itemArray);
                    if (mAdapter == null) {
                        mAdapter = new ProcessAdapter(ct, arrayMe);
                        mList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }

                    if (arrayMe.size() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    if (currentId != 0) {
                        mList.getRefreshableView().setSelection(currentId);
                    }
                    ToastMessage("数据加载完成");
                    progressDialog.dismiss();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    LogUtil.d(TAG, result);
                    exceptionCount++;
                    try {
                        if (exceptionCount <= 3) {
                            ToastMessage(result);
                            ViewUtil.ct = ct;
                            ViewUtil.LoginERPTask(ct, mHandler, 0x16);
                            progressDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tv_process_already.setEnabled(true);
                    break;
                case 0x16:
                    try {
                        initData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.APP_NOTNETWORK:
                    progressDialog.dismiss();
                    break;
            }
        }
    };
    private int currentId;
    private int mPosition;
    private JSONArray itemArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_msg);
        ViewUtils.inject(this);
        initView();
        initData();

    }

    private void initData() {
        if (CommonUtil.isNetWorkConnected(mContext)) {
            progressDialog.show();
            tv_process_already.setEnabled(false);
            loadProcessToDo(page);
        } else {
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }

    }
    @Override
    public boolean needNavigation() {
        return false;
    }
    private void initView() {
        setTitle("");
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mList = (PullToRefreshListView) findViewById(R.id.lv_process);
        tv_process_me = (TextView) findViewById(R.id.tv_process_me);
        tv_process_already = (TextView) findViewById(R.id.tv_process_already);
        tv_process_un = (TextView) findViewById(R.id.tv_process_un);
        iv_back = (ImageView) findViewById(R.id.back);
        mEmptyLayout = new EmptyLayout(this, mList.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
//        mEmptyLayout.showLoading();

        View view = LayoutInflater.from(ct).inflate(R.layout.plat_process_header, null);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        tv_process_already = (TextView) view.findViewById(R.id.tv_process_already);
        tv_process_un = (TextView) view.findViewById(R.id.tv_process_un);
        tv_process_me = (TextView) view.findViewById(R.id.tv_process_me);
        iv_back = (ImageView) view.findViewById(R.id.back);
        bar.setCustomView(view);
        tv_process_already.setOnClickListener(this);
        tv_process_me.setOnClickListener(this);
        tv_process_un.setOnClickListener(this);
        tv_process_un.setSelected(true);
        iv_back.setOnClickListener(this);
        mList.setMode(PullToRefreshBase.Mode.BOTH);
        mList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                switch (tab_type) {
                    case 1:
                        page = 1;
                        loadProcessToDo(page);
                        break;
                    case 2:
                        pageDone = 1;
                        loadProcesstoAlreadyDo(pageDone);
                        break;
                    case 3:
                        loadProcessAlreadyLaunch(page);
                        break;
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                switch (tab_type) {
                    case 1:
                        page++;
                        loadProcessToDo(page);
                        break;
                    case 2:
                        pageDone++;
                        loadProcesstoAlreadyDo(pageDone);
                        break;
                    case 3:
                        loadProcessAlreadyLaunch(page);
                        break;
                }

            }
        });

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProcessAdapter.ViewModel model = (ProcessAdapter.ViewModel) view.getTag();
                mPosition = (int) parent.getItemIdAtPosition(position);
                currentId = position;
                LogUtil.d(TAG, position + "");
                LogUtil.d(TAG, mPosition + "");
                String title = "";
                String url = "";
                String doc_type = "";
                int deal_id = -1;
                int detail_id = -1;
                switch (tab_type) {
                    case 1:
                        title = "待审批";
                        String codeValue = model.codeValue;
//                        if ("transferprocess".equals(model.typecode)||"process".equals(model.typecode) || "".equals(model.typecode)) {
//                            url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID;
//                        } else if ("procand".equals(model.typecode)) {
//                            url = "jsps/mobile/jprocand.jsp?nodeId=" + model.JP_NODEID;
//                        } else if ("unprocess".equals(model.typecode)) {
//                            url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID + "%26_do=1";
//                        }
                        if (ListUtils.isEmpty(arrayUn)) return;
                        doc_type = arrayUn.getJSONObject(mPosition).getString("jp_name");
                        detail_id = arrayUn.getJSONObject(mPosition).getInteger("jp_codevalue");
                        deal_id = arrayUn.getJSONObject(mPosition).getInteger("jp_id");
                        if (!StringUtil.isEmpty(doc_type) && "工作日报".equals(doc_type)) {
                            startActivityForResult(new Intent(ProcessB2BActivity.this, PlatWDdetailyActivity.class)
                                            .putExtra("fromwhere", "examine_and_approve")
                                            .putExtra("doc_type", doc_type)
                                            .putExtra("deal_id", deal_id)
                                            .putExtra("detail_id", detail_id)
                                            .putExtra("deal_type", 1)
                                    , 0x321); //TODO 你根据这个0x322 我的详情界面审批操作返回后这里改变列表数据状态，或者remove该条单据,resultcode == 0x41701
                        } else if (!StringUtil.isEmpty(doc_type) && "申诉单".equals(doc_type)) {
                            startActivityForResult(new Intent(ProcessB2BActivity.this, ComplaintDetailActivity.class)
                                            .putExtra("fromwhere", "examine_and_approve")
                                            .putExtra("whichpage", 5)
                                            .putExtra("deal_id", deal_id)
                                            .putExtra("detail_id", detail_id)
                                            .putExtra("deal_type", 1)
                                    , 0x321);
                        } else {
                            startActivityForResult(new Intent("com.modular.appwork.FormDetailActivity")
                                            .putExtra("fromwhere", "examine_and_approve")
                                            .putExtra("doc_type", doc_type)
                                            .putExtra("deal_id", deal_id)
                                            .putExtra("detail_id", detail_id)
                                            .putExtra("deal_type", 1)
                                    , 0x321);
                        }


                        break;
                    case 2:
                        if (ListUtils.isEmpty(arrayRe)) return;
                        doc_type = arrayRe.getJSONObject(mPosition).getString("jn_name");
                        detail_id = arrayRe.getJSONObject(mPosition).getInteger("jp_nodeid");
                        deal_id = arrayRe.getJSONObject(mPosition).getInteger("jp_keyvalue");
                        if (!StringUtil.isEmpty(doc_type) && "工作日报".equals(doc_type)) {
                            startActivityForResult(new Intent(ProcessB2BActivity.this, PlatWDdetailyActivity.class)
                                            .putExtra("fromwhere", "examine_and_approve")
                                            .putExtra("doc_type", doc_type)
                                            .putExtra("deal_id", deal_id)
                                            .putExtra("detail_id", detail_id)
                                            .putExtra("deal_type", 2)
                                    , 0x322); //
                        } else if (!StringUtil.isEmpty(doc_type) && "申诉单".equals(doc_type)) {
                            startActivityForResult(new Intent(ProcessB2BActivity.this, ComplaintDetailActivity.class)
                                            .putExtra("fromwhere", "examine_and_approve")
                                            .putExtra("whichpage", 5)
                                            .putExtra("deal_id", deal_id)
                                            .putExtra("detail_id", detail_id)
                                            .putExtra("deal_type", 2)
                                    , 0x322); //
                        } else {
                            startActivityForResult(new Intent("com.modular.appwork.FormDetailActivity")
                                            .putExtra("fromwhere", "examine_and_approve")
                                            .putExtra("doc_type", doc_type)
                                            .putExtra("deal_id", deal_id)
                                            .putExtra("detail_id", detail_id)
                                            .putExtra("deal_type", 2)
                                    , 0x322); //
                        }
                        break;
                    case 3:
                        title = "我发起的";
                        url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID + "%26_do=1";
                        break;
                }

//                String master = model.master == null ? CommonUtil.getSharedPreferences(ct, "erp_master") : model.master;
//                CommonUtil.loadWebView(ct, url, mTitle, master, null, null);
            }
        });

        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mAdapter == null) {
                    //Toast.makeText(getApplication(), "系统内部错误", Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.setmEmptyLayout(mEmptyLayout);
                    if (!StringUtil.isEmpty(editable.toString())) {
                        mAdapter.getFilter().filter(editable.toString());
                    } else {
                        mAdapter.getFilter().filter("");
                    }
                }
            }
        });


    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.tv_process_un == id) {
            progressDialog.show();
            tab_type = 1;
            mAdapter = null;
            tv_process_un.setSelected(true);
            tv_process_already.setSelected(false);
            tv_process_me.setSelected(false);
            mList.setMode(PullToRefreshBase.Mode.BOTH);
            loadProcessToDo(page);
        } else if (R.id.tv_process_already == id) {
            progressDialog.show();
            tab_type = 2;
            mAdapter = null;
            mList.setMode(PullToRefreshBase.Mode.BOTH);
            tv_process_un.setSelected(false);
            tv_process_already.setSelected(true);
            tv_process_me.setSelected(false);
            loadProcesstoAlreadyDo(pageDone);
        } else if (R.id.tv_process_me == id) {
            //                progressDialog.show();
            tab_type = 3;
            mAdapter = null;
            mList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            tv_process_un.setSelected(false);
            tv_process_already.setSelected(false);
            tv_process_me.setSelected(true);
//                loadProcessAlreadyLaunch(page);
            ToastMessage("该功能后续完善");
        } else if (R.id.back == id) {
            onBackPressed();
        }
    }

    private void loadProcessToDo(int page) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getAuditTodo;
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            progressDialog.dismiss();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("emuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));
        params.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "companyEnUu"));
        params.put("count", "1000");
        params.put("sessionId", CommonUtil.getB2BSession(MyApplication.getInstance()));
        params.put("page", page);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, SUSSCESS_un, null, null, "get");

    }


    private void loadProcesstoAlreadyDo(int pageDone) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getAuditDone;
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            progressDialog.dismiss();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("emuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));
        params.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "companyEnUu"));
        params.put("count", "1000");
        params.put("page", pageDone);
        params.put("sessionId", CommonUtil.getB2BSession(MyApplication.getInstance()));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, SUSSCESS_already, null, null, "get");

    }

    private void loadProcessAlreadyLaunch(int page) {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/process/alreadyLaunch.action";
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            progressDialog.dismiss();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("count", String.valueOf(page * 30));
        params.put("page", 1);
        params.put("sessionId", CommonUtil.getB2BSession(MyApplication.getInstance()));
        params.put("isMobile", "1");
        params.put("_do", "1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, SUSSCESS_me, null, null, "get");

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        mList.setRefreshing(true);
//
//    }

    public class ProcessAdapter extends BaseAdapter implements Filterable {

        private Context ct;
        private LayoutInflater inflater;
        private JSONArray jsonArray;
        private JSONArray originArray;
        private EmptyLayout mEmptyLayout;

        public EmptyLayout getmEmptyLayout() {
            return mEmptyLayout;
        }

        public void setmEmptyLayout(EmptyLayout mEmptyLayout) {
            this.mEmptyLayout = mEmptyLayout;
        }

        ProcessAdapter(Context ct, JSONArray array) {
            this.ct = ct;
            this.jsonArray = array;
            this.originArray = array;
            this.inflater = LayoutInflater.from(ct);
        }

        public void setJsonArray(JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

        @Override
        public int getCount() {
            return jsonArray == null ? 0 : jsonArray.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewModel model = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_process_state, parent, false);
                model = new ViewModel();
                model.name = (TextView) convertView.findViewById(R.id.tv_name);
                model.date = (TextView) convertView.findViewById(R.id.tv_date);
                model.status = (TextView) convertView.findViewById(R.id.tv_status);
                convertView.setTag(model);
            } else {
                model = (ViewModel) convertView.getTag();
            }

            switch (tab_type) {
                case 1:
 /*                   "jp_codevalue": 885,
                        "jp_id": 1686,
                        "jp_launcherid": 1000010143,
                        "jp_launchername": "陈爱平",
                        "jp_launchtime": 1490950746067,
                        "jp_name": "工作日报",
                        "jp_nodedealman": 1000009169,
                        "jp_nodeid": 1873,
                        "jp_status": "待审核"*/
                    String jp_launchername = jsonArray.getJSONObject(position).getString("jp_launchername");
                    String jp_name = jsonArray.getJSONObject(position).getString("jp_name");
                    String JP_NODEID = jsonArray.getJSONObject(position).getString("JP_NODEID");
                    if (!StringUtil.isEmpty(jp_name)) jp_name = jp_name.replace("流程", "");
                    Long jp_launchtime = jsonArray.getJSONObject(position).getLong("jp_launchtime");
                    String jp_status = "待审批";
                    String jp_typecode = jsonArray.getJSONObject(position).getString("typecode");
                    String jp_master = jsonArray.getJSONObject(position).getString("CURRENTMASTER");
                    String codevalue = jsonArray.getJSONObject(position).getString("jp_codevalue");

                    model.name.setText(jp_launchername + "的" + jp_name);
                    if (jp_launchtime != null) {
                        model.date.setText(DateFormatUtil.getStrDate4Date(new Date(jp_launchtime), "yyyy-MM-dd HH:mm"));
                    } else {
                        model.date.setText("");
                    }
                    ;
                    model.status.setText(jp_status);
                    model.JP_NODEID = JP_NODEID;
                    model.typecode = jp_typecode;
                    model.master = jp_master;
                    model.codeValue = codevalue;
                    break;
                case 2:
//                {
//                    "jn_dealmanid":1000002802,
//                        "jn_dealmanname":"移动测试员",
//                        "jn_dealresult":"不同意",
//                        "jn_dealtime":1490249444130,
//                        "jn_name":"加班单",
//                        "jn_operateddescription":"注意工作，别老加班",
//                        "jp_keyvalue":809,
//                        "jp_launcherid":1000009169,
//                        "jp_launchername":"移动测试",
//                        "jp_nodeid":281
//                }
                    String jn_dealmanname = jsonArray.getJSONObject(position).getString("jn_dealmanname");
                    jp_name = jsonArray.getJSONObject(position).getString("jn_name");
                    JP_NODEID = jsonArray.getJSONObject(position).getString("jp_nodeid");
                    if (!StringUtil.isEmpty(jp_name)) jp_name = jp_name.replace("流程", "");
                    String jn_dealtime = jsonArray.getJSONObject(position).getString("jn_dealtime");//JP_LAUNCHTIME
                    String jn_dealresult = jsonArray.getJSONObject(position).getString("jn_dealresult");
                    String doc_name = jsonArray.getJSONObject(position).getString("jp_launchername");
                    model.codeValue = jsonArray.getJSONObject(position).getString("jp_keyvalue");
                    model.name.setText(doc_name + "的" + jp_name);
                    if (jn_dealmanname != null)
                        model.date.setText(DateFormatUtil.long2Str(Long.valueOf(jn_dealtime), DateFormatUtil.YMD_HMS));
                    if (!StringUtil.isEmpty(jn_dealresult)) {
                        if ("不同意".equals(jn_dealresult)) {
                            com.common.ui.ViewUtils.textSpanForStyle(model.status, "已审批(" + jn_dealresult + ")",
                                    jn_dealresult, mContext.getResources().getColor(R.color.red));

                        } else {
                            com.common.ui.ViewUtils.textSpanForStyle(model.status, "已审批(" + jn_dealresult + ")",
                                    jn_dealresult, mContext.getResources().getColor(R.color.light_gray));
                        }
                    }
                    model.JP_NODEID = JP_NODEID;
                    break;
                case 3:
                    //JP_CODEVALUE
                    //JP_NODEDEALMANNAME
                    //JP_STATUS
                    //JP_LAUNCHTIME 
                    //JP_NODEDEALMANNAME
                    if (jsonArray.getJSONObject(position) != null) {
                        Long time = jsonArray.getJSONObject(position).getLong("JP_LAUNCHTIME");
                        String name = jsonArray.getJSONObject(position).getString("JP_NAME");
                        String code = jsonArray.getJSONObject(position).getString("JP_CODEVALUE");
                        String status = jsonArray.getJSONObject(position).getString("JP_STATUS");
                        String nodename = jsonArray.getJSONObject(position).getString("JP_NODEDEALMANNAME");
                        JP_NODEID = jsonArray.getJSONObject(position).getString("JP_NODEID");
                        String codename = jsonArray.getJSONObject(position).getString("JP_NODENAME");
                        if (!StringUtil.isEmpty(status)) {
                            if (status.equals("待审批")) {
                                status = "等待" + nodename + "(" + codename + ")审批";
                            }
                            if (status.equals("未通过")) {
                                status = "未通过" + nodename + "(" + codename + ")审批";
                            }
                        } else {
                            status = " ";
                        }
                        if (!StringUtil.isEmpty(name)) name = name.substring(0, name.length() - 2);
                        model.name.setText(name + "-" + code);
                        if (time != null) {
                            model.date.setText(DateFormatUtil.getStrDate4Date(new Date(time), "yyyy-MM-dd HH:mm"));
                        } else {
                            model.date.setText("");
                        }
                        if (status.contains("未通过")) {
//                            CommonUtil.textSpanForStyle(model.status, status,
//                                    status, mContext.getResources().getColor(R.color.red));
                            model.status.setTextColor(mContext.getResources().getColor(R.color.red));
                            model.status.setText(status);

                        } else {
                            model.status.setTextColor(mContext.getResources().getColor(R.color.light_gray));
                            model.status.setText(status);
                        }
                        model.JP_NODEID = JP_NODEID;
                    }

                    break;
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults searchResults = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        searchResults.values = tab_type == 1 ? arrayUn : arrayRe;
                        searchResults.count = (tab_type == 1 ? arrayUn : arrayRe).size();
                    } else {
                        JSONArray newArry = new JSONArray();
                        for (int i = 0; i < originArray.size(); i++) {
                            JSONObject jsonObject = originArray.getJSONObject(i);
                            Log.i("Arison", "performFiltering:" + jsonObject.toString());
                            String cu_name = "";
                            String time = "";
                            String status = "";
                            String nodename = "";
                            String jn_dealresult = "";
                            String jp_launchername = "";
                            String code = "";
                            switch (tab_type) {
                                case 1:
                                /*  "jp_codevalue": 885,
                                    "jp_id": 1686,
                                    "jp_launcherid": 1000010143,
                                    "jp_launchername": "陈爱平",
                                    "jp_launchtime": 1490950746067,
                                    "jp_name": "工作日报",
                                    "jp_nodedealman": 1000009169,
                                    "jp_nodeid": 1873,
                                    "jp_status": "待审核"*/
                                    cu_name = jsonObject.getString("jp_name") == null ? "" : jsonObject.getString("jp_name");
                                    long temp_long = jsonObject.getLong("jp_launchtime") == null ? 0 : jsonObject.getLong("jp_launchtime");
                                    time = DateFormatUtil.getStrDate4Date(new Date(temp_long), "yyyy-MM-dd HH:mm");
                                    status = jsonObject.getString("jp_status") == null ? "" : jsonObject.getString("jp_status");
                                    jp_launchername = jsonObject.getString("jp_launchername") == null ? "" : jsonObject.getString("jp_launchername");
                                    break;
                                case 2:
                                    //                {
                                    //                    "jn_dealmanid":1000002802,
                                    //                        "jn_dealmanname":"移动测试员",
                                    //                        "jn_dealresult":"不同意",
                                    //                        "jn_dealtime":1490249444130,
                                    //                        "jn_name":"加班单",
                                    //                        "jn_operateddescription":"注意工作，别老加班",
                                    //                        "jp_keyvalue":809,
                                    //                        "jp_launcherid":1000009169,
                                    //                        "jp_launchername":"移动测试",
                                    //                        "jp_nodeid":281
                                    //                }
                                    cu_name = jsonObject.getString("jn_name") == null ? "" : jsonObject.getString("jn_name");
                                    time = jsonObject.getString("jn_dealtime") == null ? "" : jsonObject.getString("jn_dealtime");
                                    jn_dealresult = jsonObject.getString("jn_dealresult") == null ? "" : jsonObject.getString("jn_dealresult");
                                    jp_launchername = jsonObject.getString("jp_launchername") == null ? "" : jsonObject.getString("jp_launchername");
                                    break;
                                case 3:
                                    cu_name = jsonObject.getString("JP_NAME") == null ? "" : jsonObject.getString("JP_NAME");
                                    temp_long = jsonObject.getLong("JP_LAUNCHTIME") == null ? 0 : jsonObject.getLong("JP_LAUNCHTIME");
                                    time = DateFormatUtil.getStrDate4Date(new Date(temp_long), "yyyy-MM-dd HH:mm");
                                    status = jsonObject.getString("JP_STATUS") == null ? "" : jsonObject.getString("JP_STATUS");
                                    nodename = jsonObject.getString("JP_NODEDEALMANNAME") == null ? "" : jsonObject.getString("JP_NODEDEALMANNAME");
                                    code = jsonObject.getString("JP_CODEVALUE") == null ? "" : jsonObject.getString("JP_CODEVALUE");

                                    break;
                            }
                            if (cu_name.contains(constraint)
                                    || time.contains(constraint)
                                    || status.contains(constraint)
                                    || jn_dealresult.contains(constraint)
                                    || jp_launchername.contains(constraint)
                                    || nodename.contains(constraint)
                                    || code.contains(constraint)) {
                                newArry.add(jsonObject);
                            }
                        }

                        searchResults.values = newArry;
                        searchResults.count = newArry.size();
                    }
                    return searchResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //装配数据
                    jsonArray = (JSONArray) results.values;
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                        mEmptyLayout.showEmpty();
                    }

                    notifyDataSetChanged();
                }
            };
        }

        class ViewModel {
            String codeValue;
            TextView name;
            TextView date;
            TextView status;
            String JP_NODEID;
            String typecode;
            String master;
        }
    }
}
