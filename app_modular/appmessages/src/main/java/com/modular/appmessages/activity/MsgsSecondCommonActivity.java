package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.dao.MessageDao;
import com.core.model.MessageModel;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.modular.appmessages.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2017/2/24.
 * function: 消息第二层 通用界面
 */
public class MsgsSecondCommonActivity extends SupportToolBarActivity implements View.OnClickListener {
    private static final int GET_SECOND_MSGS = 17022801;
    private static final int MSG_MARKED_READED = 0x35;
    private static final int SECOND_MSG_DETAILY = 0x329;
    private static final int BACK_IDS = 0x3291;
    private PullToRefreshListView myplv;
    private TextView more_msg_tv;
    private String msg_title;
    private String msg_type;
    private NewMsgAdapter msgAdapter;
    private PopupWindow setWindow = null;
    private int mPosition;
    private EmptyLayout mEmptyLayout;
    private int mark_position;
    private int mark_unread_position;
    private String baseUrl;//网址
    private List<MessageModel> msgModel;
    private int msg_id;
    private String em_code;
    private int unread_nums = 0;
    private List<Integer> jump_position;
    private String mark_time;
    private Boolean item_readed;
    private TextView msg_markread_tv;
    private int aftermarktime_news = 0;
    private TextView msg_detail_tv;
    private Boolean platform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waitting_works);
        ViewUtils.inject(this);
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        initView();
        NewMsgsEvent();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) endActivity();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endActivity();
    }

    private void endActivity() {
        LogUtil.i("endActivity");
        if (!StringUtil.isEmpty(msg_type))
            MessageDao.getInstance().updateLastMessageByType(msg_type);
        finish();
    }

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_SECOND_MSGS:
                    String secondmsg_result = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("secondmsg_result", secondmsg_result);
                    try {
                        JSONObject resultJsonObject = JSON.parseObject(secondmsg_result);
                        JSONArray msgsArray = resultJsonObject.getJSONArray("listdata");
                        if (!ListUtils.isEmpty(msgsArray)) {
                            handleMsgsArray(msgsArray);
                            if (platform) {
                                String ids = resultJsonObject.getString("ids");
                                if (!StringUtil.isEmpty(ids) && platform) {
                                    doPlatBackids(ids);
                                }
                            }
                        } else {
                            loadLocalData(msg_type);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case BACK_IDS:
                    if (msg.getData() != null) {
                        String ids_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("ids_result", ids_result);
                    }
                    break;
//                case SECOND_MSG_DETAILY:
//                    if (msg.getData() != null){
//                        String platsecondmsg_result = msg.getData().getString("result");
//                        Lg.prinlnLongMsg("platsecondmsg_result", platsecondmsg_result);
//                    }
//                    break;
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

    private void doPlatBackids(String ids) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().back_ids;
        Map<String, Object> param = new HashMap<>();
        param.put("ids", ids);
//        param.put("venduu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
//        param.put("vendUseruu",Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, mhandler, headers, BACK_IDS, null, null, "post");

    }


    /**
     * 处理获取网络获取到的数据，先更新本地数据库，再加载本地数据库
     *
     * @param msgsArray
     */
    private void handleMsgsArray(JSONArray msgsArray) {
        JSONObject object = null;
        List<MessageModel> models = new ArrayList<>();
        MessageModel model = null;
        for (int i = 0; i < msgsArray.size(); i++) {
            try {
                object = msgsArray.getJSONObject(i);
                model = new MessageModel();
                model.setId(object.getInteger("id"));
                model.setTitle(object.getString("title"));
                if (platform) {
                    model.setTime(DateFormatUtil.long2Str(object.getLongValue("createTime"), "yyyy-MM-dd HH:mm"));
                } else {
                    model.setTime(object.getString("createTime"));
                }
                model.setSubTitle(object.getString("subTitle"));
                model.setHierarchy(1);
                model.setType(msg_type);
                model.setCount(1);
                models.add(model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (MessageDao.getInstance().createOrinstart(models)) {
            loadLocalData(msg_type);
        }
    }

    private void initView() {
        myplv = (PullToRefreshListView) findViewById(R.id.waitting_work_plv);
        more_msg_tv = (TextView) findViewById(R.id.more_msg_tv);
        mEmptyLayout = new EmptyLayout(this, myplv.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        myplv.setMode(PullToRefreshBase.Mode.BOTH);

        msgAdapter = new NewMsgAdapter();
        myplv.getRefreshableView().setAdapter(msgAdapter);
        msgModel = new ArrayList<>();
        jump_position = new ArrayList<>();
        //  处理接收来自消息第一层界面传来的type ，然后请求网络加载该type的详细消息
        Intent intent = getIntent();
        msg_type = intent.getStringExtra("type");
        msg_title = intent.getStringExtra("title");
        em_code = intent.getStringExtra("emcode") == null ? CommonUtil.getSharedPreferences(ct, "erp_username") : intent.getStringExtra("emcode");
        mark_time = intent.getStringExtra("readTime");
        Log.i("mark_time,msg_type", mark_time + msg_type + "");
        if (!StringUtil.isEmpty(msg_title)) {
            setTitle(msg_title);
        }
        if (platform && MyApplication.getInstance().isNetworkActive() && !"kpi".equals(msg_type)) {
            progressDialog.show();
            loadNet();
        } else {
            loadLocalData(msg_type);
        }
//
//        if (MyApplication.getInstance().isNetworkActive()) { //有网下优先请求数据进行本地数据更新再显示本地数据库消息
//
//        } else {
//            loadLocalData(msg_type);  //无网下直接获取数据库显示老数据
//            ToastMessage(getResources().getString(R.string.networks_out));
//            progressDialog.dismiss();
//        }

    }

    /**
     * 请求网络获取消息数据
     */
    private void loadNet() {
        if (platform) {
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().second_msg_detaily;
            Map<String, Object> param = new HashMap<>();
            param.put("type", msg_type);
            param.put("venduu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
            param.put("vendUseruu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(getApplicationContext(), url, param, mhandler, headers, GET_SECOND_MSGS, null, null, "get");
        } else {
            String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/queryEmNewsDetails.action";
            Map<String, Object> param = new HashMap<>();
            param.put("emcode", em_code);
            param.put("type", msg_type);
            LinkedHashMap headers = new LinkedHashMap();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
            ViewUtil.httpSendRequest(getApplicationContext(), url, param, mhandler, headers, GET_SECOND_MSGS, null, null, "post");
        }
    }

    /**
     * 加载本地数据
     *
     * @param msg_type
     */
    private void loadLocalData(String msg_type) {
        msgModel = MessageDao.getInstance().queryByType(msg_type);
        if (!ListUtils.isEmpty(msgModel)) { //当数据库中有数据时，
            if (!TextUtils.isEmpty(mark_time)) {
                doSecondMarkSave(msgModel, mark_time); // 当第一层 有标为已读标志时，进行第二次更新本地及新来消息否读状态
            } else {
                doDataShow(msgModel);   // 当第一层 没有标为已读标志时，直接显示第一次更新后的本地数据库
            }
        } else {
            progressDialog.dismiss();
            mEmptyLayout.showEmpty();
        }
    }


    /**
     * 将本地最后要显示的数据加载到适配器中，以及更多消息显示逻辑、点击事件
     *
     * @param msgModel
     */
    private void doDataShow(List<MessageModel> msgModel) {
        jump_position.clear();
        for (int i = 0; i < msgModel.size(); i++) {
            if (!msgModel.get(i).isReaded()) {
                unread_nums++; // 统计未读消息条数
                jump_position.add(i);
            }
            if (i == msgModel.size() - 1) {
                if (unread_nums > 10 && msgModel.size() >= unread_nums) {
                    more_msg_tv.setVisibility(View.VISIBLE);
                    more_msg_tv.setText(unread_nums + "条新消息");
                    more_msg_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 页面滚动到第XXX条新消息
                            more_msg_tv.setVisibility(View.GONE);
                            myplv.getRefreshableView().setSelection(jump_position.get(0) + 1);
//                            ToastMessage(jump_position.get(0) + "");
                        }
                    });
                } else {
                    more_msg_tv.setVisibility(View.GONE);
                }
            }
        }

        msgAdapter.setMsgModel(msgModel);
        msgAdapter.notifyDataSetChanged();
        myplv.getRefreshableView().setSelection(msgModel.size() - 1);
    }

    private void doSecondMarkSave(List<MessageModel> msgModel, String mark_time) {
        for (int i = 0; i < msgModel.size(); i++) {
            if (mark_time.compareTo(msgModel.get(i).getTime()) >= 0) {
                msgModel.get(i).setReaded(true);
            }
        }
        MessageDao.getInstance().createOrinstart(msgModel);
        doDataShow(msgModel);
    }

    private void NewMsgsEvent() {
        myplv.setMode(PullToRefreshBase.Mode.DISABLED);
//        myplv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
//            @Override
//            public void onPullDownToRefresh(PullToRefreshBase refreshView) { //TODO 下拉刷新
//                if (MyApplication.getInstance().isNetworkActive()) {
//                    if (!"kpi".equals(msg_type)) {
//                        loadNet();
//                    }
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastMessage(getString(R.string.common_down_finish));
//                            myplv.onRefreshComplete();
//                        }
//                    }, 3000);
//                } else {
//                    myplv.onRefreshComplete();
//                    ToastMessage(getResources().getString(R.string.networks_out));
//                }
//
//            }

//            @Override
//            public void onPullUpToRefresh(PullToRefreshBase refreshView) { //TODO 上拉加载
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastMessage(getString(R.string.common_up_finish));
//                        myplv.onRefreshComplete();
//                    }
//                }, 3000);
//            }
//        });
        myplv.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                msg_id = msgModel.get(mPosition).getId();
                MessageModel model = msgModel.get(mPosition);
//                ToastMessage("点击position = " + mPosition + "," + " msg_id = " + msg_id);
                if (platform) {
                    Log.i("msg_title,msg_context", msg_title + "," + Html.fromHtml(msgModel.get(mPosition).getSubTitle()));
                    startActivityForResult(new Intent(MsgsSecondCommonActivity.this, PlatMsgThirdActivity.class)
                                    .putExtra("title", msg_title)
                                    .putExtra("msg_context", msgModel.get(mPosition).getSubTitle())
                            , MSG_MARKED_READED);
                } else {
                    if ("kpi".equals(msg_type)) {
                        new AlertDialog.Builder(mContext)
                                .setTitle(msg_title)
                                .setMessage(Html.fromHtml(msgModel.get(mPosition).getSubTitle()))
                                .setPositiveButton(getString(R.string.common_sure), null).show();
                        msgModel.get(mPosition).setReaded(true);
                        MessageDao.getInstance().upStatusByType("kpi", true);// 考勤消息进入第二层后全部标为已读
                        msgAdapter.notifyDataSetChanged();
                    } else {
                        doClickEvent(mPosition, model);
                    }
                }


            }
        });

        myplv.getRefreshableView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                msg_id = msgModel.get(mPosition).getId();
                item_readed = msgModel.get(mPosition).isReaded();
                if (!"kpi".equals(msg_type)) {
                    showPopupWindow();
                }
                return true;
            }
        });
        myplv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:// 触摸按下时的操作
                        break;
                    case MotionEvent.ACTION_MOVE:// 触摸移动时的操作:滑动屏幕时取消更多消息提醒
                        more_msg_tv.setVisibility(View.GONE);
                        break;
                    case MotionEvent.ACTION_UP:  // 触摸抬起时的操作
                        break;
                }
                return false;
            }
        });

    }


    private void doClickEvent(int mPosition, MessageModel model ) {
        baseUrl = CommonUtil.getAppBaseUrl(ct);
        String url = baseUrl + "mobile/message/getDetail.action?id=" + model.getId();
        Intent intent_web = new Intent(ct,MsgThirdWebActivity.class);
        intent_web.putExtra(IntentUtils.KEY_URL, url);
        intent_web.putExtra(IntentUtils.KEY_TITLE, msg_title);
        intent_web.putExtra(IntentUtils.KEY_NEER_COOKIE, true);
        intent_web.putExtra(IntentUtils.KEY_NEER_SHARE, false);
        intent_web.putExtra(IntentUtils.KEY_SHARE_IMAGE, "");
        intent_web.putExtra(IntentUtils.KEY_SHARE_CONTENT, "");
        intent_web.putExtra("caller",model.getCaller());
        intent_web.putExtra("keyValue",model.getKeyValue());
        startActivityForResult(intent_web, MSG_MARKED_READED);
//        intent_web.putExtra("url", url + "?id=" + msg_id);
//        intent_web.putExtra("title", msg_title);
//        intent_web.putExtra("cookie", true);
//        startActivityForResult(intent_web, MSG_MARKED_READED);

        Log.d("322msg_id", msg_id + ":" + url);
        msgModel.get(mPosition).setReaded(true);
        MessageDao.getInstance().upStatus(msg_id, msg_type, true);//点击保存本地为已读状态操作
        msgAdapter.notifyDataSetChanged();
    }

    private void showPopupWindow() {
        showMarkReadPW();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }


    private void showMarkReadPW() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.second_msgs_long_click, null);
        viewContext.findViewById(R.id.msg_delete_tv).setOnClickListener(this);

        msg_detail_tv = (TextView) viewContext.findViewById(R.id.msg_detail_tv);
        msg_detail_tv.setOnClickListener(this);
        msg_markread_tv = (TextView) viewContext.findViewById(R.id.msg_markread_tv);
        msg_markread_tv.setOnClickListener(this);
        msg_markread_tv.setText("");
        if (msgModel.get(mPosition).isReaded()) {
            msg_markread_tv.setText(getString(R.string.mark_readed));
        } else {
            msg_markread_tv.setText(getString(R.string.mark_unread));
        }
        Log.i("Mark_Status", (msgModel.get(mPosition).isReaded()) + "");
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }

    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    @Override
    public void onClick(View v) {
        closePopupWindow();
        int id = v.getId();
        if (R.id.msg_delete_tv == id) {
            doDeleteMsg();     // 删除操作
        } else if (R.id.msg_markread_tv == id) {
            if (item_readed) {
                doMarkUnReadMsg();
            } else {
                doMarkReadedMsg();   //  标为已读
            }
        } else if (R.id.msg_detail_tv == id) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(mContext)
                            .setTitle(msg_title)
                            .setMessage(Html.fromHtml(msgModel.get(mPosition).getSubTitle()))
                            .setPositiveButton(getString(R.string.common_sure), null).show();

                    doMarkReadedMsg();   //  标为已读
                }
            }, 500);

        }


    }

    private void doMarkUnReadMsg() {
        msgModel.get(mPosition).setReaded(false);
        MessageDao.getInstance().upStatus(msg_id, msg_type, false); //标为未读保存本地
        msgAdapter.notifyDataSetChanged();

//        MessageDao.getInstance().upStatusByType(msg_type,false);
        MessageDao.getInstance().unReadForTwoFloor(msg_id, msg_type, false);
    }

    private void doMarkReadedMsg() {
        msgModel.get(mPosition).setReaded(true);
        MessageDao.getInstance().upStatus(msg_id, msg_type, true); //标为已读保存本地
        msgAdapter.notifyDataSetChanged();
    }

    private void doDeleteMsg() {
        msgModel.remove(mPosition);
        //  删除本地数据库操作
        MessageDao.getInstance().deleteByid(msg_id);
        msgAdapter.notifyDataSetInvalidated();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == MSG_MARKED_READED && resultCode == 22 && data != null) {
                msgModel.get(mPosition).setReaded(true);
                MessageDao.getInstance().upStatus(msg_id, msg_type, true);//点击保存本地为已读状态操作
                msgAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //TODO 列表适配器
    public class NewMsgAdapter extends BaseAdapter {
        private List<MessageModel> msgModel;

        public void setMsgModel(List<MessageModel> msgModel) {
            this.msgModel = msgModel;
        }

        @Override
        public int getCount() {
            return msgModel == null ? 0 : msgModel.size();
        }

        @Override
        public Object getItem(int position) {
            return msgModel.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(ct, R.layout.item_secondmsgs, null);
                viewHolder = new ViewHolder();
                viewHolder.msgs_img = (ImageView) convertView.findViewById(R.id.msgs_img);
                viewHolder.msgs_nums_tv = (TextView) convertView.findViewById(R.id.msgs_nums_tv);
                viewHolder.msgs_title_tv = (TextView) convertView.findViewById(R.id.msgs_title_tv);
                viewHolder.msgs_content_tv = (TextView) convertView.findViewById(R.id.msgs_content_tv);
                viewHolder.msgs_time_tv = (TextView) convertView.findViewById(R.id.msgs_time_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            int imageurl = 0;
            switch (msg_type) {
                case "note": // 通知公告
                    imageurl = R.drawable.tongzhis;
                    break;
                case "common": // 普通知会
                    imageurl = R.drawable.putongs;
                    break;
                case "b2b": // b2b提醒
                    imageurl = R.drawable.b2bs;
                    break;
                case "crm":  // CRM提醒
                    imageurl = R.drawable.crms;
                    break;
                case "kpi": // 考勤提醒
                    imageurl = R.drawable.kaoqings;
                    break;
                case "meeting": // 会议提醒
                    imageurl = R.drawable.huiyis;
                    break;
                case "process": // 审批知会
                    imageurl = R.drawable.shenpis;
                    break;
                case "job": // 稽核提醒
                    imageurl = R.drawable.jihes;
                    break;
                case "system": // 知会消息
                    imageurl = R.drawable.zhihuis;
                    break;
                case "task": // 任务提醒
                    imageurl = R.drawable.renwus;
                    break;
                default:
                    imageurl = R.drawable.putongs;
            }

            viewHolder.msgs_img.setImageResource(imageurl);
            viewHolder.msgs_title_tv.setText(msgModel.get(position).getTitle());
            if (!TextUtils.isEmpty(msgModel.get(position).getTime())) {
                viewHolder.msgs_time_tv.setText(msgModel.get(position).getTime());
            }
            viewHolder.msgs_content_tv.setText(Html.fromHtml(msgModel.get(position).getSubTitle()));
            if (msgModel.get(position).isReaded()) {
                viewHolder.msgs_nums_tv.setVisibility(View.GONE);
            } else {
                viewHolder.msgs_nums_tv.setVisibility(View.VISIBLE);
            }
            if (position == msgModel.size() - 1) {
                progressDialog.dismiss(); //数据加载最后一个时才取消圆圈动画
            }
            return convertView;
        }

        class ViewHolder {
            ImageView msgs_img;
            TextView msgs_nums_tv;
            TextView msgs_title_tv;
            TextView msgs_content_tv;
            TextView msgs_time_tv;
        }
    }
}
