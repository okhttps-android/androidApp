package com.modular.appmessages.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.Editable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.modular.appmessages.R;
import com.modular.appmessages.util.ApprovalUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @desc:
 * @author：Arison on 2016/11/1
 */
public class ProcessMsgActivity extends BaseActivity implements View.OnClickListener {
    private EmptyLayout mEmptyLayout;
    private VoiceSearchView voiceSearchView;
    private PullToRefreshListView mList;
    private RelativeLayout tv_process_me_rl;
    private RelativeLayout tv_process_already_rl;
    private RelativeLayout tv_process_rl;
    private ImageView iv_back;


    private ProcessAdapter mAdapter;
    private JSONArray array = new JSONArray();

    private int tab_type = 1;
    private int page = 1;
    private final int SUSSCESS_un = 1;
    private final int SUSSCESS_already = 2;
    private final int SUSSCESS_me = 3;
    private int exceptionCount = 0;//
    private List<String> im_ids;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String result = msg.getData().getString("result");
            if (StringUtil.isEmpty(result)) return;
            mList.onRefreshComplete();
            switch (msg.what) {
                case SUSSCESS_un:
                    JSONArray itemArray = null;
                    try {
                        itemArray = JSON.parseObject(result).getJSONArray("data");
                        if (page == 1) array.clear();
                        array.addAll(itemArray);
                        array = ApprovalUtil.sortJsonArray(array);//排序
                        if (mAdapter == null) {
                            mAdapter = new ProcessAdapter(ct, array);
                            mList.setAdapter(mAdapter);
                        } else {
                            mAdapter.setData(array);
                            mAdapter.notifyDataSetChanged();
                        }
                        ToastMessage(getString(R.string.common_up_finish));
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        ToastMessage("系统错误！");
                    }

                    if (array.size() == 0) {
                        mEmptyLayout.showEmpty();
                        tv_process_un_num.setVisibility(View.GONE);
                    } else {
//                        handleImids(1,array);
                        getEmimids(array);
                        tv_process_un_num.setVisibility(View.VISIBLE);
                        int numSize = ListUtils.getSize(array);
                        tv_process_un_num.setText(numSize > 99 ? "99+" : (numSize <= 0 ? "" : String.valueOf(numSize)));
                    }

                    if (tab_type == 1 && mPosition > 0) {
                        if (mPosition < array.size()) {
                            mList.getRefreshableView().setSelection(mPosition);
                        }
                    }
                    progressDialog.dismiss();
                    break;
                case SUSSCESS_already:
                    itemArray = JSON.parseObject(result).getJSONArray("data");
                    // if (page == 1) 
                    array.clear();
                    array.addAll(itemArray);
                    if (mAdapter == null) {
                        mAdapter = new ProcessAdapter(ct, array);
                        mList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (array.size() == 0) {

                        mEmptyLayout.showEmpty();
                    } else {
//                       handleImids(2,array);
                        getEmimids(array);
                    }
                    if (currentId != 0) {
//                        mList.getRefreshableView().setSelection(0);
                    }
                    ToastMessage(getString(R.string.common_up_finish));
                    progressDialog.dismiss();
                    break;
                case SUSSCESS_me:
                    itemArray = JSON.parseObject(result).getJSONArray("data");
                    // if (page == 1)
                    array.clear();
                    array.addAll(itemArray);
                    if (mAdapter == null) {
                        mAdapter = new ProcessAdapter(ct, array);
                        mList.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (array.size() == 0) {
                        mEmptyLayout.showEmpty();
                    } else {
//                        handleImids(3, array);
                    }
                    ToastMessage(getString(R.string.common_up_finish));
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
                    progressDialog.dismiss();
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
                    ToastMessage(getString(R.string.common_notlinknet));
                    break;
            }
        }
    };
    private int mPosition;

    private void getEmimids(JSONArray array) {
        if (!ListUtils.isEmpty(im_ids)) im_ids.clear();
        for (int i = 0; i < array.size(); i++) {
            im_ids.add(array.getJSONObject(i).getString("EM_IMID") + "");
            if (i == array.size() - 1 && !ListUtils.isEmpty(im_ids)) {
                mAdapter.setImids(im_ids);
                mAdapter.notifyDataSetChanged();
                LogUtil.prinlnLongMsg("im_ids", im_ids.toString());
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.closeDB();
        }
    }

    private DBManager manager = null;


    private int currentId;
    private TextView tv_process_un_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_msg);
        initView();
    }

    private void initData() {
        if (CommonUtil.isNetWorkConnected(mContext)) {
            loadProcessToDo(page);
        } else {
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }
    }

    private void initView() {
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mList = (PullToRefreshListView) findViewById(R.id.lv_process);
        tv_process_me_rl = (RelativeLayout) findViewById(R.id.tv_process_me_rl);
        tv_process_already_rl = (RelativeLayout) findViewById(R.id.tv_process_already_rl);
        tv_process_rl = (RelativeLayout) findViewById(R.id.tv_process_rl);
        iv_back = (ImageView) findViewById(R.id.back);
        im_ids = new ArrayList<>();
        mEmptyLayout = new EmptyLayout(this, mList.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);

        View view = LayoutInflater.from(ct).inflate(R.layout.process_header, null);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        tv_process_already_rl = (RelativeLayout) view.findViewById(R.id.tv_process_already_rl);
        tv_process_rl = (RelativeLayout) view.findViewById(R.id.tv_process_rl);
        tv_process_me_rl = (RelativeLayout) view.findViewById(R.id.tv_process_me_rl);
        iv_back = (ImageView) view.findViewById(R.id.back);
        tv_process_un_num = (TextView) view.findViewById(R.id.tv_process_un_num);
        bar.setCustomView(view);
        tv_process_already_rl.setOnClickListener(this);
        tv_process_me_rl.setOnClickListener(this);
        tv_process_rl.setOnClickListener(this);
        tv_process_rl.setSelected(true);
        iv_back.setOnClickListener(this);
        mList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                mPosition = -1;
                voiceSearchView.setText("");
                switch (tab_type) {
                    case 1:
                        loadProcessToDo(page);
                        break;
                    case 2:
                        loadProcesstoAlreadyDo(page);
                        break;
                    case 3:
                        loadProcessAlreadyLaunch(page);
                        break;
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                mPosition = -1;
                voiceSearchView.setText("");
                switch (tab_type) {
                    case 1:
                        loadProcessToDo(page);
                        break;
                    case 2:
                        loadProcesstoAlreadyDo(page);
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
                mPosition = (int) parent.getItemIdAtPosition(position);
                ProcessAdapter.ViewModel model = (ProcessAdapter.ViewModel) view.getTag();
                currentId = position;
                LogUtil.d(TAG, position + "");
                String title = "";
                String url = "";
                String d_imids = "";
                switch (tab_type) {
                    case 1:
                        title = getString(R.string.title_approval);
                        d_imids = im_ids.get(mPosition);
                        if ("transferprocess".equals(model.typecode) || "process".equals(model.typecode) || "".equals(model.typecode)) {
                            url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID;
                        } else if ("procand".equals(model.typecode)) {
                            url = "jsps/mobile/jprocand.jsp?nodeId=" + model.JP_NODEID;
                        } else if ("unprocess".equals(model.typecode)) {
                            url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID + "%26_do=1";
                        }

                        break;
                    case 2:
                        title = getString(R.string.task_confimed);
                        d_imids = im_ids.get(mPosition);
                        url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID + "%26_do=1";
                        break;
                    case 3:
                        title = getString(R.string.task_request_me);
                        url = "jsps/mobile/process.jsp?nodeId=" + model.JP_NODEID + "%26_do=1";
                        break;
                }
                if (StringUtil.isEmpty(d_imids)) {
                    d_imids = MyApplication.getInstance().mLoginUser.getUserId(); //审批详情界面：这里显示是我发起的，取当前登录当前用户的头像
                }
                LogUtil.i("url=" + url);
                String master = model.master == null ? CommonUtil.getSharedPreferences(ct, "erp_master") : model.master;

//                if (CommonUtil.isReleaseVersion()) {
//                    CommonUtil.loadWebView(ct, url, mTitle, master, null, null);
//                    Intent intent = new Intent(ct, AppWebViewActivity.class);
//                    intent.putExtra("url", url);
//                    intent.putExtra("p", mTitle);
//                    intent.putExtra("master", master);
//                    intent.putExtra("nodeid", Integer.valueOf(model.JP_NODEID));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                } else {
                Intent intent = new Intent(ct, ApprovalActivity.class);
                intent.putExtra("imid", d_imids);
                intent.putExtra("title", title);
                intent.putExtra("type", tab_type);
                intent.putExtra("master", master);
                intent.putExtra("nodeid", Integer.valueOf(model.JP_NODEID));
                startActivity(intent);
//                }
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
            public void afterTextChanged(Editable s) {
                if (mAdapter == null) {
                    //Toast.makeText(getApplication(), "系统内部错误", Toast.LENGTH_SHORT).show();
                } else {
                    if (!StringUtil.isEmpty(voiceSearchView.getText().toString())) {
                        mAdapter.getFilter().filter(voiceSearchView.getText().toString());
                    } else {
                        mAdapter.getFilter().filter("");
                    }
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (tab_type == 1) {
            loadProcessToDo(page);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.tv_process_rl == id) {
            progressDialog.show();
            page = 1;
            tab_type = 1;
            mAdapter = null;
            tv_process_rl.setSelected(true);
            tv_process_already_rl.setSelected(false);
            tv_process_me_rl.setSelected(false);
            mList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            loadProcessToDo(page);
            voiceSearchView.setText("");
        } else if (R.id.tv_process_already_rl == id) {
            progressDialog.show();
            page = 1;
            tab_type = 2;
            mAdapter = null;
            mList.setMode(PullToRefreshBase.Mode.BOTH);
            tv_process_rl.setSelected(false);
            tv_process_already_rl.setSelected(true);
            tv_process_me_rl.setSelected(false);
            loadProcesstoAlreadyDo(page);
            voiceSearchView.setText("");
        } else if (R.id.tv_process_me_rl == id) {
            progressDialog.show();
            page = 1;
            tab_type = 3;
            mAdapter = null;
            mList.setMode(PullToRefreshBase.Mode.BOTH);
            tv_process_rl.setSelected(false);
            tv_process_already_rl.setSelected(false);
            tv_process_me_rl.setSelected(true);
            loadProcessAlreadyLaunch(page);
            voiceSearchView.setText("");
        } else if (R.id.back == id) {
            onBackPressed();
        }

    }


    private void loadProcessToDo(int page) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/process/toDo.action";
        Map<String, Object> params = new HashMap<>();
        // count=10&page=1&limit=25
        params.put("count", "1000");
//        params.put("isMobile","1");
        params.put("page", "1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, SUSSCESS_un, null, null, "get");

    }


    private void loadProcesstoAlreadyDo(int page) {
        //       progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/process/alreadyDo.action";
        Map<String, Object> params = new HashMap<>();
        params.put("count", String.valueOf(page * 30));
        params.put("page", 1);
        params.put("isMobile", "1");
        params.put("_do", "1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, SUSSCESS_already, null, null, "get");

    }

    private void loadProcessAlreadyLaunch(int page) {
        //      progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/process/alreadyLaunch.action";
        Map<String, Object> params = new HashMap<>();
        params.put("count", String.valueOf(page * 30));
        params.put("page", 1);
        params.put("isMobile", "1");
        params.put("_do", "1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, SUSSCESS_me, null, null, "get");

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (tab_type == 1) {
            voiceSearchView.setText("");
            initData();
        }

    }


    public class ProcessAdapter extends BaseAdapter implements Filterable {

        private LayoutInflater inflater;
        private JSONArray originArray;
        private JSONArray jsonArray;
        private List<String> imids;


        public void setImids(List<String> imids) {
            this.imids = imids;
        }

        ProcessAdapter(Context ct, JSONArray array) {
            this.jsonArray = array;
            this.originArray = array;
            this.inflater = LayoutInflater.from(ct);
        }

        public void setData(JSONArray array) {
            this.jsonArray = array;
            this.originArray = array;
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
                model.imid = (ImageView) convertView.findViewById(R.id.photo_img);
                model.meimid = (CircleImageView) convertView.findViewById(R.id.photo_me);
                convertView.setTag(model);
            } else {
                model = (ViewModel) convertView.getTag();
            }


            switch (tab_type) {
                case 1:
                    //JP_LAUNCHERNAME
                    //JP_NAME
                    //JP_STATUS
                    //JP_LAUNCHTIME
                    String jp_launchername = jsonArray.getJSONObject(position).getString("JP_LAUNCHERNAME");
                    String jp_name = jsonArray.getJSONObject(position).getString("JP_NAME");
                    String JP_NODEID = jsonArray.getJSONObject(position).getString("JP_NODEID");
                    if (!StringUtil.isEmpty(jp_name)) jp_name = jp_name.replace("流程", "");
                    Long jp_launchtime = jsonArray.getJSONObject(position).getLong("JP_LAUNCHTIME");
                    String jp_status = jsonArray.getJSONObject(position).getString("JP_STATUS");
                    String jp_typecode = jsonArray.getJSONObject(position).getString("TYPECODE");
                    String jp_master = jsonArray.getJSONObject(position).getString("CURRENTMASTER");
                    int tdem_imid = JSONUtil.getInt(jsonArray.getJSONObject(position), "EM_IMID");
                    model.name.setText(jp_launchername + "的" + jp_name);
                    if (jp_launchtime != null) {
                        model.date.setText(DateFormatUtil.getStrDate4Date(new Date(jp_launchtime), "MM-dd HH:mm") + "");
                    } else {
                        model.date.setText("");
                    }

//                    model.status.setText(jp_status);
                    model.status.setTextColor(getResources().getColor(R.color.approvaling));
                    model.status.setText("等待我审批");
                    model.JP_NODEID = JP_NODEID;
                    model.typecode = jp_typecode;
                    model.master = jp_master;
                    model.imid.setVisibility(View.VISIBLE);
                    model.meimid.setVisibility(View.GONE);
                    //设置显示审批人头像
                    if (tdem_imid != -1) {
                        AvatarHelper.getInstance().display(tdem_imid + "", model.imid, true, false);//显示圆角图片
                    } else {
                        String imageUri = "drawable://" + R.drawable.common_header_boy;
                        AvatarHelper.getInstance().display(imageUri, model.imid, true);
                    }
                    break;
                case 2:
                    //JN_DEALMANNAME
                    //JP_NAME
                    //JN_DEALTIME
                    //JN_DEALRESULT
                    String jn_dealmanname = jsonArray.getJSONObject(position).getString("JP_LAUNCHERNAME");
                    jp_name = jsonArray.getJSONObject(position).getString("JP_NAME");
                    JP_NODEID = jsonArray.getJSONObject(position).getString("JP_NODEID");
                    if (!StringUtil.isEmpty(jp_name)) jp_name = jp_name.replace("流程", "");
                    String jn_dealtime = jsonArray.getJSONObject(position).getString("JN_DEALTIME");//JP_LAUNCHTIME
                    String jn_dealresult = jsonArray.getJSONObject(position).getString("JN_DEALRESULT");
                    int done_emid = JSONUtil.getInt(jsonArray.getJSONObject(position), "EM_IMID");
                    model.name.setText(jn_dealmanname + "的" + jp_name);
                    if (!StringUtil.isEmpty(jn_dealtime)) {
                        String ttt = DateFormatUtil.getStrDate4Date(new Date(TimeUtils.f_str_2_long(jn_dealtime)), "MM-dd HH:mm");
                        model.date.setText(ttt + "");
                    } else {
                        model.date.setText("");
                    }
                    if (!StringUtil.isEmpty(jn_dealresult)) {
                        int statusTextId = R.color.done_approval;
                        if (jn_dealresult.startsWith("不同意") || jn_dealresult.startsWith("结束流程") || jn_dealresult.startsWith("未通过")) {
                            jn_dealresult = "未通过";
                            statusTextId = R.color.red;
                        } else if (jn_dealresult.startsWith("变更处理人")) {
                            statusTextId = R.color.done_approval;
                            if (!StringUtil.isEmpty(jsonArray.getJSONObject(position).getString("JN_OPERATEDDESCRIPTION"))) {
                                jn_dealresult = "变更处理人（" + jsonArray.getJSONObject(position).getString("JN_OPERATEDDESCRIPTION") + ")";
                            } else {
                                jn_dealresult = "变更处理人";
                            }
                        } else {
                            jn_dealresult = "已审批";
                            statusTextId = R.color.titleBlue;
                        }
                        model.status.setText(jn_dealresult);
                        model.status.setTextColor(getResources().getColor(statusTextId));
                    } else {
                        model.status.setText("");
                    }
                    model.JP_NODEID = JP_NODEID;
                    model.imid.setVisibility(View.VISIBLE);
                    model.meimid.setVisibility(View.GONE);
                    //设置显示审批人头像
                    if (done_emid != -1) {
                        AvatarHelper.getInstance().display(done_emid + "", model.imid, true, false);//显示圆角图片
                    } else {
                        String imageUri = "drawable://" + R.drawable.common_header_boy;
                        AvatarHelper.getInstance().display(imageUri, model.imid, true);
                    }
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
                        int me_emid = JSONUtil.getInt(jsonArray.getJSONObject(position), "EM_IMID");
                        if (!StringUtil.isEmpty(status)) {
                            if (status.equals("待审批")) {
                                model.status.setTextColor(getResources().getColor(R.color.approvaling));
                                nodename = nodename == null ? "" : nodename;
                                status = "等待" + nodename
                                        //+ "(" + codename + ")"
                                        + getString(R.string.approvel);
                            } else if (status.equals("未通过")) {
                                model.status.setTextColor(getResources().getColor(R.color.red));
//                                status = getString(R.string.Did_not_pass) + nodename + "(" + codename + ")" + getString(R.string.approvel);
//                                status = "已拒绝";
                            } else if (status.equals("已审批")) {
                                model.status.setTextColor(getResources().getColor(R.color.titleBlue));
//                                status = "已同意";
                            }
                        } else {
                            status = " ";
                        }

                        if (!StringUtil.isEmpty(name)) name = name.substring(0, name.length() - 2);
//                        model.name.setText(name + "-" + code);
                        model.name.setText(name);
                        if (time != null) {
                            model.date.setText(DateFormatUtil.getStrDate4Date(new Date(time), "MM-dd HH:mm") + "");
                        } else {
                            model.date.setText("");
                        }
                        if (status.contains("未通过")) {
                            model.status.setTextColor(mContext.getResources().getColor(R.color.red));

                        }
                        model.status.setText(status);

                        model.JP_NODEID = JP_NODEID;
                        //设置显示审批人头像
                        model.imid.setVisibility(View.VISIBLE);
                        model.meimid.setVisibility(View.GONE);
                        if (me_emid != -1) {
                            AvatarHelper.getInstance().display(me_emid + "", model.imid, true, false);//显示圆角图片
                        } else {
                            String imageUri = "drawable://" + R.drawable.common_header_boy;
                            AvatarHelper.getInstance().display(imageUri, model.imid, true);
                        }
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
                        searchResults.values = array;
                        searchResults.count = array.size();
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
                                    cu_name = jsonObject.getString("JP_NAME") == null ? "" : jsonObject.getString("JP_NAME");
                                    long temp_long = jsonObject.getLong("JP_LAUNCHTIME") == null ? 0 : jsonObject.getLong("JP_LAUNCHTIME");
                                    time = DateFormatUtil.getStrDate4Date(new Date(temp_long), "yyyy-MM-dd HH:mm");
                                    status = jsonObject.getString("JP_STATUS") == null ? "" : jsonObject.getString("JP_STATUS");
                                    jp_launchername = jsonObject.getString("JP_LAUNCHERNAME") == null ? "" : jsonObject.getString("JP_LAUNCHERNAME");
                                    break;
                                case 2:
                                    cu_name = jsonObject.getString("JP_NAME") == null ? "" : jsonObject.getString("JP_NAME");
                                    time = jsonObject.getString("JN_DEALTIME") == null ? "" : jsonObject.getString("JN_DEALTIME");
                                    jn_dealresult = jsonObject.getString("JN_DEALRESULT") == null ? "" : jsonObject.getString("JN_DEALRESULT");
                                    jp_launchername = jsonObject.getString("JP_LAUNCHERNAME") == null ? "" : jsonObject.getString("JP_LAUNCHERNAME");
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
                    jsonArray = (JSONArray) results.values;
                    LogUtil.d("过滤后：" + JSON.toJSONString(jsonArray));
                    notifyDataSetChanged();
                    if (mAdapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                }
            };
        }

        class ViewModel {
            TextView name;
            TextView date;
            TextView status;
            String JP_NODEID;
            String typecode;
            String master;
            ImageView imid;
            CircleImageView meimid;
        }
    }
}
