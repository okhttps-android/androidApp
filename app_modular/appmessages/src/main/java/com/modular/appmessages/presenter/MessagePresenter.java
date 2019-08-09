package com.modular.appmessages.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.SystemUtil;
import com.common.thread.ThreadUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.MessageDao;
import com.core.dao.UUHelperDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.Friend;
import com.core.model.MessageModel;
import com.core.model.OAConfig;
import com.core.model.UUHelperModel;
import com.core.model.WorkModel;
import com.core.model.XmppMessage;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.modular.appmessages.R;
import com.modular.appmessages.activity.ApprovalNewListActivity;
import com.modular.appmessages.activity.BusinessTargetsActivity;
import com.modular.appmessages.activity.MsgsSecondCommonActivity;
import com.modular.appmessages.activity.ProcessB2BActivity;
import com.modular.appmessages.activity.RealTimeFormActivity;
import com.modular.appmessages.activity.Subscription2Activity;
import com.modular.appmessages.activity.UUHelperActivity;
import com.modular.appmessages.db.SubsDao;
import com.modular.appmessages.model.MessageHeader;
import com.modular.appmessages.model.MessageNew;
import com.modular.appmessages.model.SubMessage;
import com.modular.appmessages.presenter.imp.IMessageView;
import com.modular.appmessages.util.ApprovalUtil;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.SignUtils;
import com.modular.apputils.utils.SwitchUtil;
import com.modular.apputils.utils.UUHttpHelper;
import com.modular.apputils.utils.VoiceUtils;
import com.modular.booking.activity.services.BServiceListActivity;
import com.modular.booking.model.SBMenuModel;
import com.uas.appworks.OA.platform.activity.BusinessTravelActivity;
import com.uas.appworks.OA.platform.activity.PurchaseDetailsActivity;
import com.uas.appworks.activity.ScheduleActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Bitliker on 2017/3/1.
 */

public class MessagePresenter implements OnHttpResultListener {
    private final int LOAD_EMNEWS = 0x11;
    private final int LOAD_SUBS = 0x12;
    private final int LOAD_PROCESS = 0x13;
    private final int LOAD_TASK = 0x14;
    private final int LOAD_EMNEWS_DETAILS = 0x15;
    private final int LOAD_B2B_COUNT = 0x16;//获取b2b的审批和任务数量
    private final int LOAD_BOOKING = 0x17;//小秘书红点
    private final int LOAD_REAL_TIME = 0x18;
    private final int IS_COMPANY_ADMIN = 0x19;
    private final int LOAD_SCHEDULE = 0x20;//获取日程数据

    public static final int REAL_TIME_FORM = 7;
    public static final int BUSINESS_STATISTICS = 8;

    private String SUB_READ_TIME;//订阅好点击阅读时间

    private List<BaseSortModel<Friend>> mFriendList;
    private Comparator<BaseSortModel<Friend>> erpComparator;
    private IMessageView iMessageView;
    private Activity ct;
    private String subReadTime;//订阅号点击时间

    private String filter;//搜索数据
    private int emnewsNum, subsNum, processNum, taskNum, bookingNum, uuHelperNum, scheduleNum;//红点消息分类数量
    private String[] RECEIVER_LIST = {ConnectivityManager.CONNECTIVITY_ACTION, OAConfig.AUTO_SIGIN_ALART, MsgBroadcast.ACTION_MSG_COMPANY_UPDATE, "com.app.home.update"
            , MsgBroadcast.ACTION_MSG_UI_UPDATE};
    private BroadcastReceiver dataChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (StringUtil.isEmpty(action)) return;
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                iMessageView.changeNet(SystemUtil.isNetWorkConnected(MyApplication.getInstance()));
            } else if (OAConfig.AUTO_SIGIN_ALART.equals(action)) {
                loadNews(isB2b);//获取消息
            } else if (action.equals("com.app.home.update") || action.equals(MsgBroadcast.ACTION_MSG_COMPANY_UPDATE)
                    || action.equals(MsgBroadcast.ACTION_MSG_UI_UPDATE)) {
                initHeaderModels();
                loadData();
            }
        }
    };
    private boolean isB2b;


    public MessagePresenter(Activity ct, IMessageView iMessageView, UnReaderListener unReaderListener) {
        this.ct = ct;
        this.unReaderListener = unReaderListener;
        mFriendList = new ArrayList<>();
        if (iMessageView == null)
            new NullPointerException("IMessageView not be null");
        this.iMessageView = iMessageView;
        if (RECEIVER_LIST != null && RECEIVER_LIST.length > 0) {
            IntentFilter dateFilter = new IntentFilter();
            for (String f : RECEIVER_LIST)
                dateFilter.addAction(f);
            LocalBroadcastManager.getInstance(ct).registerReceiver(dataChangeReceiver, dateFilter);
        }
        initHeaderModels();
        if (mSignUtils == null) {
            mSignUtils = new SignUtils(mSignListener);
        }
    }


    public void loadData() {
        String role = CommonUtil.getUserRole();
        isB2b = false;
        if (role.equals("2")) {//Erp用户

            SUB_READ_TIME = CommonUtil.getMaster() + "SUB_READ_TIME";//订阅好点击阅读时间
            subReadTime = PreferenceUtils.getString(SUB_READ_TIME);
            loadRealTime();//获取实时看板数据
            loadTaskData();//获取任务接口
            loadProcessToDo();//获取审批流接口
            loadSubData();
            loadNews(isB2b);//获取消息
            if (!isB2b) {
                loadSchedule();
            }
        } else if (role.equals("3")) {//B2b用户
            isB2b = true;
            loadB2bNewsCount();
            loadNews(isB2b);//获取消息

        }
        isCompanyAdmin();
        loadBookingNewNum(role);//预约红点接口
    }

    /**
     * 判断当前用户是否为商家管理员
     */
    private void isCompanyAdmin() {
        Map<String, Object> param = new HashMap<>();
        param.put("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu"));
        param.put("userid", MyApplication.getInstance().mLoginUser.getUserId());
        param.put("token", MyApplication.getInstance().mAccessToken);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));

        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl("user/appCompanyAdmin")
                .setMode(Request.Mode.POST)
                .setWhat(IS_COMPANY_ADMIN)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, 1, this);
    }

    /**
     * 获取实时看板数据
     */
    private void loadRealTime() {
        Map<String, Object> param = new HashMap<>();
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));

        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl("mobile/getRealTimeSubs.action")
                .setMode(Request.Mode.GET)
                .setWhat(LOAD_REAL_TIME)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void loadB2bNewsCount() {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getTaskCounts;
        Request request = new Request.Bulider()
                .setParam(new HashMap<String, Object>())
                .setUrl(url)
                .setMode(Request.Mode.GET)
                .setWhat(LOAD_B2B_COUNT)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    /**
     * 获取订阅号消息接口
     */
    private void loadSubData() {
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        param.put("count", 100);
        param.put("condition", "where to_char(createdate_,'yyyymmdd')='" + DateFormatUtil.long2Str("yyyyMMdd") + "'");
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));

        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl("common/desktop/subs/getSubs.action")
                .setMode(Request.Mode.GET)
                .setWhat(LOAD_SUBS)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /**
     * 获取消息红点提醒接口
     *
     * @param isB2b
     */
    private void loadNews(boolean isB2b) {
        Map<String, Object> param = new HashMap<>();
        if (isB2b) {
            param.put("venduu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            param.put("vendUseruu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        } else
            param.put("emcode", CommonUtil.getEmcode());
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().releaseCount : "mobile/queryEmNews.action";
        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl(url)
                .setMode(Request.Mode.GET)
                .setWhat(LOAD_EMNEWS)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /**
     * 获取日程
     */
    private void loadSchedule() {
        if (mUUHttpHelper == null) {
            mUUHttpHelper = new UUHttpHelper(CommonUtil.getSchedulerBaseUrl());
        }
        String sessionId= CommonUtil.getSharedPreferences(ct, "sessionId");
        mUUHttpHelper.requestHttp(new Parameter.Builder()
                .url("schedule/getSchedule")
                .addParams("imid", MyApplication.getInstance().getLoginUserId())
                .addParams("uasUrl", CommonUtil.getAppBaseUrl(ct))
                .addParams("emcode", CommonUtil.getEmcode())
                .addParams("master", CommonUtil.getMaster())
                .addParams("sessionId",sessionId)
                .addSuperHeaders("Cookie", "JSESSIONID=" + sessionId)
                .record(LOAD_SCHEDULE), mOnSmartHttpListener);

    }

    private UUHttpHelper mUUHttpHelper;
    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            JSONObject object = JSON.parseObject(message);
            switch (what) {
                case LOAD_SCHEDULE:
                    if (JSONUtil.getBoolean(object, "success")) {
                        int unRead = JSONUtil.getInt(object, "data");
                        showScheduleNum(unRead);
                    }
                    break;
            }
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {

        }
    };

    private void postReadNews(String ids) {
        Map<String, Object> param = new HashMap<>();
        param.put("ids", ids);
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().countBack;
        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setBundle(null)
                .setWhat(LOAD_EMNEWS_DETAILS)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /**
     * 获取获取任务接口
     */
    private void loadTaskData() {
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        param.put("_noc", 1);
        param.put("page", 1);
        param.put("pageSize", 100);
        param.put("status", "");
        param.put("caller", "ResourceAssignment");
        String em_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        String emName = CommonUtil.getSharedPreferences(ct, "erp_emname");
        String condition =
                "((ra_resourcecode='" + em_code + "' and  ra_status='进行中')" +
                        " or ( recorder='" + emName + "' and ra_status='待确认'))" +
                        " and nvl(class,' ')<>'projecttask'";
        param.put("condition", condition);
        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl("common/datalist/data.action")
                .setMode(Request.Mode.GET)
                .setBundle(null)
                .setWhat(LOAD_TASK)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    /**
     * @desc:审批流接口
     * @author：Arison on 2016/11/15
     */
    private void loadProcessToDo() {
        Map<String, Object> param = new HashMap<>();
        param.put("count", "1000");
        param.put("page", 1);//默认获取第一页

        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl("common/desktop/process/toDo.action")
                .setMode(Request.Mode.GET)
                .setBundle(null)
                .setWhat(LOAD_PROCESS)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    private void loadBookingNewNum(String role) {
        Map<String, Object> param = new HashMap<>();
        param.put("token", MyApplication.getInstance().mAccessToken);
        param.put("userid", MyApplication.getInstance().mLoginUser.getUserId());//默认获取第一页
        Bundle bundle = new Bundle();
        bundle.putString("role", role);
        Request request = new Request.Bulider()
                .setParam(param)
                .setUrl("user/appCount")
                .setMode(Request.Mode.GET)
                .setBundle(bundle)
                .setWhat(LOAD_BOOKING)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, 1, this);
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (!isJSON) {
            //TODO 返回数据错误
            return;
        }
        JSONObject object = JSON.parseObject(message);
        switch (what) {
            case LOAD_BOOKING:
                //{"count":"1"}
                bookingNum = Integer.valueOf(object.getString("count"));
                iMessageView.updateHeaderView(2, Integer.valueOf(object.getString("count")), "", "");
                updateForUnReader();
//				iMessageView.showModel(null);  ///tODO 这个引起了闪一下的问题(可能存在会有刷新不过来的问题)
                if (bundle != null) {
                    String role = bundle.getString("role");
                    if (role != null && role.equals("1")) {
                        saveErp2DB(null);
                    }
                }
                break;
            case LOAD_EMNEWS:
                if (!object.containsKey("listdata")) {
                    handlerNewsFormErp(new JSONArray());
                    return;
                }
                String ids = object.getString("ids");
                if (!StringUtil.isEmpty(ids) && isB2b)
                    postReadNews(ids);
                JSONArray array = object.getJSONArray("listdata");
                handlerNewsFormErp(array);
                break;
            case LOAD_SUBS://获取订阅号接口
                handlerSub(object);
                break;
            case LOAD_PROCESS://获取审批流接口
                handlerProcess(object);
                break;
            case LOAD_TASK://获取任务接口
                handlerTask(object);
                break;
            case LOAD_EMNEWS_DETAILS:
                String type = bundle.getString("type");
                boolean isReaded = bundle.getBoolean("isReaded");
                JSONArray msgsArray = object.getJSONArray("listdata");
                if (!ListUtils.isEmpty(msgsArray)) {
                    handleMsgsArray(type, msgsArray);
                }
                handlerEndOfReadOrDelete(type, isReaded);
                break;
            case LOAD_B2B_COUNT:
                if (object.containsKey("processcount")) {
                    processNum = JSONUtil.getInt(object, "processcount");
                    String title = JSONUtil.getText(object, "lastProcess");
                    long time = JSONUtil.getTime(object, "lastProcessTime") / 1000;
                    if (time == 0) time = System.currentTimeMillis() / 1000;
                    iMessageView.updateHeaderView(4, processNum, title, TimeUtils.getFriendlyTimeDesc(ct, (int) time));
                    updateForUnReader();
                }
                if (object.containsKey("taskcount")) {
                    taskNum = JSONUtil.getInt(object, "taskcount");
                    String title = JSONUtil.getText(object, "lasttask");
                    long time = JSONUtil.getTime(object, "lasttaskTime") / 1000;
                    if (time == 0) time = System.currentTimeMillis() / 1000;
                    String taskTime = TimeUtils.getFriendlyTimeDesc(ct, (int) time);
                    iMessageView.updateHeaderView(5, taskNum, title, taskTime);
                    updateForUnReader();
                }

                break;
            case LOAD_REAL_TIME:
                JSONArray subsArray = object.getJSONArray("subs");
                if (subsArray != null && subsArray.size() > 0) {
                    CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext()
                            , Constants.REAL_TIME_CACHE, subsArray.toString());
                    int size = subsArray.size();
                    iMessageView.updateHeaderView(REAL_TIME_FORM, size, "", "");
                } else {
                    iMessageView.updateHeaderView(REAL_TIME_FORM, 0, "", "");
                }
                break;
            case IS_COMPANY_ADMIN:
                String result = JSONUtil.getText(object, "result");
                if ("0".equals(result)) {
                    iMessageView.updateHeaderView(BUSINESS_STATISTICS, 0, "", "");
                } else if ("1".equals(result)) {
                    iMessageView.updateHeaderView(BUSINESS_STATISTICS, 1, "", "");
                }
                break;
            case LOAD_SCHEDULE:
                //TODO 获取日程数据
                break;
            default:
                break;
        }
    }


    @Override
    public void error(int what, String message, Bundle bundle) {

    }


    /**
     * 处理获取网络获取到的数据，先更新本地数据库，再加载本地数据库
     *
     * @param msgsArray
     */
    private void handleMsgsArray(String type, JSONArray msgsArray) {
        JSONObject object = null;
        final List<MessageModel> models = new ArrayList<>();
        MessageModel model = null;
        for (int i = 0; i < msgsArray.size(); i++) {
            try {
                object = msgsArray.getJSONObject(i);
                model = new MessageModel();
                model.setId(object.getInteger("id"));
                model.setTitle(object.getString("title"));
                model.setSubTitle(object.getString("subTitle"));
                model.setTime(DateFormatUtil.long2Str(JSONUtil.getTime(object, "createTime"), "yyyy-MM-dd HH:mm"));
                model.setHierarchy(1);
                model.setType(type);
                model.setCount(1);
                models.add(model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                MessageDao.getInstance().createOrinstart(models);
            }
        });
    }

    /**
     * 处理任务相关数据
     *
     * @param object
     */
    private void handlerTask(JSONObject object) {
        taskNum = 0;
        String subTitle = null;
        String time = null;
        if (object != null && object.containsKey("data")) {
            JSONArray array = object.getJSONArray("data");
            taskNum = array.size();
            if (!ListUtils.isEmpty(array)) {
                subTitle = array.getJSONObject(0).getString("ra_taskname");
                time = array.getJSONObject(0).getString("ra_startdate");
            } else {
                subTitle = "";
                time = "";
            }
        }
        String taskTime = TimeUtils.getFriendlyTimeDesc(ct, (int) (DateFormatUtil.str2Long(time, DateFormatUtil.YMD_HMS) / 1000));
        iMessageView.updateHeaderView(5, taskNum, subTitle, taskTime);
        updateForUnReader();
    }


    /**
     * 处理审批了相关数据
     *
     * @param object
     */
    private void handlerProcess(JSONObject object) {
        JSONArray itemArray = object.getJSONArray("data");
        if (!ListUtils.isEmpty(itemArray))
            itemArray = ApprovalUtil.sortJsonArray(itemArray);
        String subTitle = null;
        long time = 0;
        if (!ListUtils.isEmpty(itemArray)) {
            processNum = itemArray.size();
            if (processNum > 0) {
                subTitle = itemArray.getJSONObject(0).getString("JP_LAUNCHERNAME") + "的"
                        + itemArray.getJSONObject(0).getString("JP_NAME");
                time = JSONUtil.getTime(itemArray.getJSONObject(0), "JP_LAUNCHTIME", "JP_REMINDDATE");
            }
        } else {
            processNum = 0;
        }
        String taskTime = TimeUtils.getFriendlyTimeDesc(ct, (int) (time / 1000));
        iMessageView.updateHeaderView(4, processNum, subTitle, taskTime);
        updateForUnReader();
    }

    private void handlerSub(final JSONObject object) {
        if (!StringUtil.isEmpty(subReadTime) && subReadTime.compareTo(DateFormatUtil.long2Str(DateFormatUtil.YMD)) >= 0) {
            showsubsNum(0, "", "");
            return;
        }
        int num = 0;
        String title = "";
        String sub = "";
        if (object.containsKey("data")) {
            //TODO 未验证修改
            JSONArray array = JSONUtil.getJSONArray(object, "data");
            if (!ListUtils.isEmpty(array)) {
                for (int i = 0; i < array.size(); i++) {
                    if (JSONUtil.getInt(array.getJSONObject(i), "STATUS_") == 0) {
                        num += 1;
                    }
                }
                title = JSONUtil.getText(array.getJSONObject(0), "TITLE_");
                long time = JSONUtil.getLong(array.getJSONObject(0), "CREATEDATE_");
                sub = TimeUtils.getFriendlyTimeDesc(ct, (int) (time / 1000));
            }

        }
        showsubsNum(num, title, sub);
    }

    private void showsubsNum(int num, String title, String time) {
        subsNum = num;
        updateForUnReader();
        iMessageView.updateHeaderView(6, num, num > 0 ? title : "", time);
    }

    private void showScheduleNum(int num) {
        scheduleNum = num;
        updateForUnReader();
        iMessageView.updateHeaderView(21, num, num > 0 ? "你有新的日程" : "", "");
    }

    private void saveSubs2Db(JSONObject o) throws Exception {
        JSONArray array = o.getJSONArray("data");
        if (ListUtils.isEmpty(array)) return;
        SubMessage message = null;
        JSONObject object = null;
        List<SubMessage> chche = new ArrayList<>();
        message = new SubMessage();
        message.setDate(DateFormatUtil.long2Str(DateFormatUtil.YMD));
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
    }

    /**
     * 处理消息红点消息
     *
     * @param array
     */
    private void handlerNewsFormErp(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            saveErp2DB(null);
            return;
        }
        JSONObject object = null;
        JSONObject detail = null;
        List<MessageModel> models = new ArrayList<>();
        List<MessageModel> detailModels = new ArrayList<>();
        MessageModel model = null;
        MessageModel detailModel = null;
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            model = new MessageModel();
            model.setTitle(object.getString("title"));
            model.setSubTitle(object.getString("lastMessage"));
            model.setTime(DateFormatUtil.long2Str(JSONUtil.getTime(object, "lastTime"), "yyyy-MM-dd HH:mm"));
            String type = object.getString("type");
            model.setType(type);
            if (object.containsKey("count") && object.get("count") != null)
                model.setCount(object.getInteger("count"));
            if (StringUtil.isEmpty(model.getTitle()) || StringUtil.isEmpty(model.getSubTitle()))
                continue;
            JSONArray details = JSONUtil.getJSONArray(object, "detail");
            models.add(model);
            if (!ListUtils.isEmpty(details)) {
                for (int j = 0; j < details.size(); j++) {
                    detail = details.getJSONObject(j);
                    detailModel = new MessageModel();
                    detailModel.setId(JSONUtil.getInt(detail, "id"));
                    detailModel.setTitle(JSONUtil.getText(detail, "title"));
                    detailModel.setSubTitle(JSONUtil.getText(detail, "subTitle"));
                    detailModel.setTime(DateFormatUtil.long2Str(JSONUtil.getTime(detail, "createTime"), "yyyy-MM-dd HH:mm"));
                    detailModel.setType(type);
                    detailModel.setHierarchy(1);
                    detailModel.setCaller(JSONUtil.getText(detail, "caller"));
                    detailModel.setKeyValue(JSONUtil.getInt(detail, "keyValue"));
                    detailModels.add(detailModel);
                }
            }
        }
        saveErp2DB(models, detailModels);
    }

    /**
     * 保存erp数据到数据库
     *
     * @param models
     */
    private void saveErp2DB(final List<MessageModel> models) {
        saveErp2DB(models, null);
    }

    private void saveErp2DB(final List<MessageModel> models, final List<MessageModel> detailModels) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ListUtils.isEmpty(models)) {
                    MessageDao.getInstance().createOrinstart(models, true);
                }
                final List<BaseSortModel<Friend>> chche = loadDataByImAsync();//loadDataByImAsync()
                OAHttpHelper.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mFriendList == null) mFriendList = new ArrayList<>();
                        mFriendList.clear();
                        if (chche != null)
                            mFriendList.addAll(chche);
                        showByEndIm();
                    }
                });
                if (!ListUtils.isEmpty(detailModels)) {
                    MessageDao.getInstance().createOrinstart(detailModels);
                }
            }
        }).start();
    }

    /**
     * 通过返回数据，转变成相对应列表数据
     *
     * @param lastTime
     * @return
     */
    private int getErpTime(String lastTime) {
        if (StringUtil.isEmpty(lastTime)) {
            return 0;
        } else {
            return (int) (DateFormatUtil.str2Long(lastTime, "yyyy-MM-dd HH:mm") / 1000);
        }
    }

    /**
     * 获取索引，用于排序
     *
     * @param mode
     */
    private final void setSortCondition(BaseSortModel<Friend> mode) throws Exception {
        Friend friend = mode.getBean();
        if (friend == null) {
            return;
        }
        String name = friend.getShowName();
        String wholeSpell = PingYinUtil.getPingYin(name);
        if (!TextUtils.isEmpty(wholeSpell)) {
            String firstLetter = Character.toString(wholeSpell.charAt(0));
            mode.setWholeSpell(wholeSpell);
            mode.setFirstLetter(firstLetter);
            mode.setSimpleSpell(PingYinUtil.converterToFirstSpell(name));
        } else {// 如果全拼为空，理论上是一种错误情况，因为这代表着昵称为空
            mode.setWholeSpell("#");
            mode.setFirstLetter("#");
            mode.setSimpleSpell("#");
        }
    }


    /**
     * 异步请求加载新的筛选条件的数据
     * <p/>
     * 是下拉刷新，还是上拉加载
     */
    private List<BaseSortModel<Friend>> loadDataByImAsync() {
        try {
            String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
            List<Friend> friends = FriendDao.getInstance().getNearlyFriendMsg(mLoginUserId);
            List<MessageModel> model = MessageDao.getInstance().queryFirstFloor();
            List<UUHelperModel> uuHelperModels = UUHelperDao.getInstance().getAllModels();
            setFriendName(friends, mLoginUserId);
            return handlerErpAndIm(friends, model, uuHelperModels);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showByEndIm() {
        iMessageView.clearSearch();
        iMessageView.showModel(mFriendList);
        iMessageView.showModel(mFriendList);
        updateForUnReader();
    }

    /**
     * 整合erp和im消息内容
     *
     * @param friends
     * @param models
     */
    private List<BaseSortModel<Friend>> handlerErpAndIm(List<Friend> friends, List<MessageModel> models, List<UUHelperModel> uuHelperItems) throws Exception {
        if (friends == null) friends = new ArrayList<>();
        List<BaseSortModel<Friend>> chche = new ArrayList<>();
        emnewsNum = 0;
        //处理消息数据库的数据
        if (!ListUtils.isEmpty(models)) {
            for (MessageModel m : models) {
                Friend friend = new Friend();
                friend.setNickName(m.getTitle());
                friend.setContent(m.getSubTitle());
                friend.setTimeSend(getErpTime(m.getTime()));
                friend.setType(XmppMessage.TYPE_ERP);
                friend.setDescription(m.getType());
                friend.set_id(m.getId());
                friend.setClickNum(m.getCount());
                friend.setPhone(m.getReadTime());
                int count = m.isReaded() ? 0 : m.getCount();
                emnewsNum += count;
                friend.setUnReadNum(count);
                friends.add(friend);
            }
        }
        if (!ListUtils.isEmpty(uuHelperItems)) {
            Friend friend = new Friend();
            friend.setNickName("UU 助手");
            friend.setType(XmppMessage.TYPE_UUHELPER);
            UUHelperModel lastModel = null;
            int unReadUnm = 0;
            for (UUHelperModel model : uuHelperItems) {
                if (!model.isReaded()) {
                    lastModel = model;
                    unReadUnm++;
                }
            }
            friend.setClickNum(unReadUnm);
            uuHelperNum = unReadUnm;
            friend.setUnReadNum(unReadUnm);
            if (lastModel != null) {
                friend.set_id(lastModel.getId());
                friend.setTimeSend((int) (lastModel.getTimeSend() / 1000));
                friend.setContent(lastModel.getTitle());
            } else {
                friend.setContent("");
            }
            friends.add(friend);
        }
        //处理im数据库和消息列表合并后的数据
        if (friends != null && friends.size() > 0) {
            for (int i = 0; i < friends.size(); i++) {
                BaseSortModel<Friend> mode = new BaseSortModel<>();
                mode.setBean(friends.get(i));
                setSortCondition(mode);
                chche.add(mode);
            }
        }
        if (erpComparator == null) {
            erpComparator = new Comparator<BaseSortModel<Friend>>() {
                public int compare(BaseSortModel<Friend> s1, BaseSortModel<Friend> s2) {
                    return (s1.getBean().getTimeSend() - s2.getBean().getTimeSend());
                }
            };
        }
        Collections.sort(chche, erpComparator);
        return chche;
    }

    private void setFriendName(List<Friend> friends, String id) {
        List<Friend> f = FriendDao.getInstance().getFriends(id);
        if (friends == null) return;
        for (int i = 0; i < friends.size(); i++) {
            for (int j = 0; j < f.size(); j++) {
                if (friends.get(i).get_id() == f.get(j).get_id()) {
                    friends.get(i).setNickName(f.get(j).getNickName());
                    friends.get(i).setRemarkName(f.get(j).getRemarkName());
                    break;
                }
            }
        }
    }

    /**
     * 删除该类型文件
     *
     * @param position
     */
    public void deleteListByType(int position) {
        if (ListUtils.isEmpty(mFriendList) || mFriendList.size() < position || position < 0) return;
        Friend friend = mFriendList.get(position).getBean();
        if (friend.getType() == XmppMessage.TYPE_ERP) {
            MessageDao.getInstance().deleteBytype(friend.getDescription());
            loadData();

        } else if (friend.getType() == XmppMessage.TYPE_UUHELPER) {
            UUHelperDao.getInstance().deleteData(-1);
            if (!ListUtils.isEmpty(mFriendList) && mFriendList.size() > position) {
                mFriendList.remove(position);
                iMessageView.showModel(mFriendList);
            }
        } else {
            deleteByIm(friend, position);
        }
    }

    /**
     * 阅读该类型文件
     *
     * @param position
     */
    public void readerAllByType(int position) {
        if (ListUtils.isEmpty(mFriendList) || mFriendList.size() < position) return;
        Friend friend = mFriendList.get(position).getBean();
        if (friend.getType() == XmppMessage.TYPE_ERP) {
            MessageDao.getInstance().upStatusByType(friend.getDescription(), true);
            loadData();
        } else if (friend.getType() == XmppMessage.TYPE_UUHELPER) {
            UUHelperDao.getInstance().updateRead(true);
        } else {
            if (friend.getUnReadNum() > 0) {
                MsgBroadcast.broadcastMsgNumUpdate(ct, false, friend.getUnReadNum());
                friend.setUnReadNum(0);
                iMessageView.showModel(mFriendList);
            }
        }
    }

    /**
     * 设置未阅读该类型文件
     *
     * @param position
     */
    public void unReaderAllByType(int position) {
        if (ListUtils.isEmpty(mFriendList) || mFriendList.size() < position) return;
        Friend friend = mFriendList.get(position).getBean();
        if (friend.getType() == XmppMessage.TYPE_ERP) {
            MessageDao.getInstance().upStatusByType(friend.getDescription(), false);
        } else if (friend.getType() == XmppMessage.TYPE_UUHELPER) {
            UUHelperDao.getInstance().updateRead(false);
        } else {
            if (friend.getUnReadNum() > 0) {
                MsgBroadcast.broadcastMsgNumUpdate(ct, false, friend.getUnReadNum());
                friend.setUnReadNum(0);
                iMessageView.showModel(mFriendList);
            }
        }
    }

    private void handlerEndOfReadOrDelete(String type, boolean isReaded) {
        if (isReaded) {
            MessageDao.getInstance().upStatusByType(type, true);
        } else {
            MessageDao.getInstance().deleteBytype(type);
        }
        loadData();
    }

    /**
     * 计算搜索显示新的内容
     *
     * @param
     */
    public void search(String filter) {
        this.filter = filter;
        List<BaseSortModel<Friend>> chcheShow = new ArrayList<>();
        if (!ListUtils.isEmpty(mFriendList)) {
            for (int i = 0; i < mFriendList.size(); i++) {
                BaseSortModel<Friend> mode = mFriendList.get(i);
                // 获取筛选的数据
                if (canShowbyFilter(mode)) {
                    chcheShow.add(mode);
                }

            }
        }
        iMessageView.showModel(chcheShow);
    }

    public WorkModel getCurrentWork() {
        List<WorkModel> workModels = WorkModelDao.getInstance().query(true);
        WorkModel workModel = null;
        String newsTime = DateFormatUtil.long2Str(DateFormatUtil.HM);
        if (!ListUtils.isEmpty(workModels)) {
            for (WorkModel e : workModels) {
                if (newsTime.compareTo(e.getWorkStart()) >= 0 && newsTime.compareTo(e.getOffend()) <= 0) {
                    workModel = e;
                    break;
                }
            }
        }
        return workModel;
    }


    private boolean canShowbyFilter(BaseSortModel<Friend> mode) {
        if (TextUtils.isEmpty(filter) || mode.getSimpleSpell().startsWith(filter) || mode.getWholeSpell().startsWith(filter)
                || mode.getBean().getShowName().startsWith(filter)) return true;
        return false;

    }


    private void deleteByIm(Friend friend, int position) {
        String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        if (friend.getRoomFlag() == 0) {
            if (friend.getUnReadNum() > 0) {
                MsgBroadcast.broadcastMsgNumUpdate(ct, false, friend.getUnReadNum());
            }
            BaseSortModel<Friend> mode = mFriendList.get(position);
            mFriendList.remove(mode);
            // 如果是普通的人，从好友表中删除最后一条消息的记录，这样就不会查出来了
            FriendDao.getInstance().resetFriendMessage(mLoginUserId, friend.getUserId());
            // 消息表中删除
            ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
        } else {
            deleteFriend(mLoginUserId, mFriendList.get(position));
        }
        iMessageView.showModel(mFriendList);
    }

    private void deleteFriend(final String loginUserId, final BaseSortModel<Friend> sortFriend) {
        Friend friend = sortFriend.getBean();
        if (friend.getUnReadNum() > 0) {
            MsgBroadcast.broadcastMsgNumUpdate(ct, false, friend.getUnReadNum());
        }
        mFriendList.remove(sortFriend);
        // 删除这个房间
        FriendDao.getInstance().deleteFriend(loginUserId, friend.getUserId());
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friend.getUserId());
        if (this.unReaderListener != null) {
            this.unReaderListener.exitMucChat(friend.getUserId());
        }
    }

    /**
     * 计算调转到那个界面
     *
     * @param mContext
     * @param position
     */
    public void turn2NextAct(Activity mContext, int position) {
        Friend friend = mFriendList.get(position).getBean();
        if (friend == null) {
            return;
        }
        if (friend.getType() == XmppMessage.TYPE_ERP) {
            //消息
            turn2ERp(friend);
            return;
        } else if (friend.getType() == XmppMessage.TYPE_UUHELPER) {
            ct.startActivity(new Intent(ct, UUHelperActivity.class));
            UUHelperDao.getInstance().updateRead(true);
            return;
        }
        if (friend.getRoomFlag() == 0) {
            if (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {// 新朋友消息
                mContext.startActivity(new Intent("com.modular.appcontact.NewFriendActivity"));
            } else {
                Intent intent = new Intent("com.modular.message.ChatActivity");
                intent.putExtra(AppConstant.FRIEND, friend);
                mContext.startActivity(intent);
            }
        } else {
            Intent intent = new Intent("com.modular.message.MucChatActivity");
            intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
            intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
            mContext.startActivity(intent);
        }
        //将红点去除
        if (friend.getUnReadNum() > 0) {
            MsgBroadcast.broadcastMsgNumUpdate(mContext, false, friend.getUnReadNum());
            friend.setUnReadNum(0);
        }
    }

    /**
     * 调转到对应ERP相关知会里面去
     *
     * @param friend
     */
    private void turn2ERp(Friend friend) {
        String description = friend.getDescription();
        if (StringUtil.isEmpty(description)) return;
        Intent intent = new Intent(ct, MsgsSecondCommonActivity.class);
        intent.putExtra("type", description);
        intent.putExtra("title", friend.getNickName());
        intent.putExtra("newmsgs", friend.getClickNum());
        intent.putExtra("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        if ("kpi".equals(description.trim()))
            intent.putExtra("readTime", TimeUtils.f_long_2_str(System.currentTimeMillis()));
        else
            intent.putExtra("readTime", friend.getPhone());
        ct.startActivity(intent);
        MessageDao.getInstance().upStatus(friend.get_id(), friend.getDescription(), true);
    }


    /*跟新未读红点信息*/
    private void updateForUnReader() {
//         bookingNum +   去除小秘书红点
        int num = subsNum + processNum + uuHelperNum + emnewsNum + taskNum + scheduleNum;
        if (this.unReaderListener != null) {
            this.unReaderListener.setUnReader(num);
        }
    }


    public void setSubReadTime(String subReadTime) {
        PreferenceUtils.putString(SUB_READ_TIME, subReadTime);
        subsNum = 0;
        updateForUnReader();
        //TODO 判断订阅有没有新消息没读
        iMessageView.updateHeaderView(6, subsNum, "", "");
        this.subReadTime = subReadTime;
    }

    public void onDestroyView(Context mContext) {
        try {
            mContext.unregisterReceiver(dataChangeReceiver);
        } catch (Exception e) {

        }
    }

    private UnReaderListener unReaderListener;

    private SignUtils mSignUtils;

    public boolean isCanPaly() {
        if (mSignUtils == null) {
            mSignUtils = new SignUtils(mSignListener);
        }
        int resId = mSignUtils.judgeFrontFace();
        if (resId == -1) {
            return true;
        } else {
            iMessageView.showToact(resId);
            return false;
        }
    }

    public void signWork(boolean needMac, WorkModel work) {
        if (mSignUtils == null) {
            mSignUtils = new SignUtils(mSignListener);
        }
        if (work == null) {
            work = getCurrentWork();
        }
        iMessageView.showProgress();
        if (needMac) {
            mSignUtils.sign(isB2b, work);
        } else {
            mSignUtils.signFristMac(isB2b, work);

        }
    }

    private SignUtils.SignListener mSignListener = new SignUtils.SignListener() {
        @Override
        public void sign(boolean signOk, String message) {
            if (signOk) {
                VoiceUtils.signVoice(R.raw.voice_sign);

            }
            iMessageView.updateSign(message);
        }
    };

    public String getMac() {
        if (mSignUtils != null) {
            return mSignUtils.getMac();
        } else {
            return SystemUtil.getMac(ct);
        }
    }

    public interface UnReaderListener {
        void setUnReader(int number);

        void exitMucChat(String userId);
    }


    private void initHeaderModels() {
        List<MessageNew> models = new ArrayList<>();
        String role = CommonUtil.getUserRole();
        if (role.equals("1")) {//个人用户
            models.addAll(getPersonalHeader());
        } else if (role.equals("3")) {//b2b用户
            models.addAll(getB2bHeader());
        } else {
            models.addAll(getErpHeader());
        }

        if (1 == 1) {
            MessageNew h = new MessageNew();
            MessageHeader model = new MessageHeader(StringUtil.getMessage(R.string.my_scheduler));
            model.setIcon(R.drawable.icon_menu_my_scheduler);
            model.setSubDoc("");
            model.setType(21);
            model.setRedKey("my_scheduler");
            h.setT(model);
            models.add(h);
        }
        if (PreferenceUtils.getInt("UUSTEP", -1) == 1) {
            //显示UU运动
            MessageNew h = new MessageNew();
            MessageHeader model = new MessageHeader(StringUtil.getMessage(R.string.set_sport));
            model.setIcon(R.drawable.uu_run);
            model.setSubDoc("");
            model.setRedKey(Constants.MESSAGE_RUN);
            model.setType(3);
            model.setTag("");
            h.setT(model);
            models.add(h);
        }

        if (SwitchUtil.showShebeiguanli()) {
            MessageNew h = new MessageNew();
            MessageHeader model = new MessageHeader("设备管理");
            model.setIcon(R.drawable.uu_run);
            model.setSubDoc("");
            model.setRedKey(Constants.MESSAGE_RUN);
            model.setType(10);
            model.setTag("");
            h.setT(model);
            models.add(h);
        }
        iMessageView.updateHeader(models);
    }

    private List<MessageNew> getErpHeader() {
        List<MessageNew> models = new ArrayList<>();

        MessageNew h = new MessageNew();
        MessageHeader model = new MessageHeader(StringUtil.getMessage(R.string.msg_approval));
        model.setIcon(R.drawable.home_image_01_u);
        model.setSubDoc("");
        model.setRedKey("");
        model.setType(4);
        model.setTag("");
        h.setT(model);
        models.add(h);

        h = new MessageNew();
        model = new MessageHeader(StringUtil.getMessage(R.string.msg_work));
        model.setIcon(R.drawable.daibangongzuo);
        model.setSubDoc("");
        model.setRedKey("");
        model.setType(5);
        model.setTag("");
        h.setT(model);
        models.add(h);

        h = new MessageNew();
        model = new MessageHeader(StringUtil.getMessage(R.string.msg_subscribe));
        model.setIcon(R.drawable.tingyue);
        model.setSubDoc("");
        model.setRedKey(Constants.MESSAGE_DINGYUE);
        model.setRedMessage(StringUtil.getMessage(R.string.msg_subscribe_data));
        model.setType(6);
        model.setTag("");
        h.setT(model);
        models.add(h);

//        h = new MessageNew();
//        model = new MessageHeader(StringUtil.getMessage(R.string.booking_menu));
//        model.setIcon(R.drawable.icon_yuyue3);
//        model.setSubDoc("");
//        model.setRedKey(Constants.MESSAGE_YUYUE);
//        model.setType(2);
//        model.setTag("");
//        h.setT(model);
//        models.add(h);
        return models;
    }

    private List<MessageNew> getB2bHeader() {
        List<MessageNew> models = new ArrayList<>();
        MessageNew h = new MessageNew();
        MessageHeader model = new MessageHeader(StringUtil.getMessage(R.string.msg_approval));
        model.setIcon(R.drawable.home_image_01_u);
        model.setSubDoc("");
        model.setRedKey("");
        model.setType(4);
        model.setTag("");
        h.setT(model);
        models.add(h);

        h = new MessageNew();
        model = new MessageHeader(StringUtil.getMessage(R.string.msg_work));
        model.setIcon(R.drawable.daibangongzuo);
        model.setSubDoc("");
        model.setRedKey("");
        model.setType(5);
        model.setTag("");
        h.setT(model);
        models.add(h);

//        h = new MessageNew();
//        model = new MessageHeader(StringUtil.getMessage(R.string.booking_menu));
//        model.setIcon(R.drawable.icon_yuyue3);
//        model.setSubDoc("");
//        model.setRedKey(Constants.MESSAGE_YUYUE);
//        model.setType(2);
//        model.setTag("");
//        h.setT(model);
//        models.add(h);
        return models;
    }

    private List<MessageNew> getPersonalHeader() {
        List<MessageNew> models = new ArrayList<>();
        MessageNew header = new MessageNew();
        MessageHeader model =null;

//        model = new MessageHeader(StringUtil.getMessage(R.string.booking_menu));
//        model.setIcon(R.drawable.icon_yuyue3);
//        model.setSubDoc("");
//        model.setRedKey(Constants.MESSAGE_YUYUE);
//        model.setType(2);
//        model.setTag("");
//        header.setT(model);
//        models.add(header);

        header = new MessageNew();
        model = new MessageHeader("餐饮");
        model.setIcon(R.drawable.icon_food);
        model.setSubDoc("美味齐全");
        model.setRedKey(Constants.MESSAGE_FOOD);
        model.setType(1);
        model.setTag("10003");
        header.setT(model);
        models.add(header);

        header = new MessageNew();
        model = new MessageHeader("美容美发");
        model.setIcon(R.drawable.icon_hair);
        model.setSubDoc("时尚潮流");
        model.setRedKey(Constants.MESSAGE_HAIR);
        model.setType(1);
        model.setTag("10004");
        header.setT(model);
        models.add(header);

        header = new MessageNew();
        model = new MessageHeader("KTV");
        model.setIcon(R.drawable.icon_ktv);
        model.setSubDoc("音乐节");
        model.setRedKey(Constants.MESSAGE_KTV);
        model.setType(1);
        model.setTag("10006");
        header.setT(model);
        models.add(header);

        model = new MessageHeader("运动健身");
        model.setIcon(R.drawable.icon_sport);
        model.setSubDoc("hi起来");
        model.setRedKey(Constants.MESSAGE_SPORT);
        model.setType(1);
        model.setTag("10002");
        header.setT(model);
        models.add(header);

        header = new MessageNew();
        model = new MessageHeader("会所");
        model.setIcon(R.drawable.icon_club);
        model.setSubDoc("预约有优惠");
        model.setRedKey(Constants.MESSAGE_CLUB);
        model.setType(1);
        model.setTag("10005");
        header.setT(model);
        models.add(header);

        header = new MessageNew();
        model = new MessageHeader("医院挂号");
        model.setIcon(R.drawable.icon_hospital);
        model.setSubDoc("您的健康助手");
        model.setRedKey(Constants.MESSAGE_HOSPITAL);
        model.setType(1);
        model.setTag("10001");
        header.setT(model);
        models.add(header);
        return models;
    }


    public void turn2ActByHeader(Activity ct, MessageHeader model) {
        Intent intent = null;
        switch (model.getType()) {
            case 1://个人版本的服务预约预约类型
                SBMenuModel menuModel = new SBMenuModel();
                menuModel.setUrl("");
                menuModel.setCode(model.getTag());
                menuModel.setIcon(model.getIcon());
                menuModel.setDesc(model.getSubDoc());
                menuModel.setTitle(model.getName());
                intent = new Intent(ct, BServiceListActivity.class);
                intent.putExtra("SBMenuModel", menuModel);
                ct.startActivity(intent);
                break;
            case 2://服务预约主页
                ct.startActivity(new Intent("com.modular.booking.BookingListActivity"));
                break;
            case 3://uu运动
//				ct.startActivity(new Intent(ct, UUSportActivity.class));
                ct.startActivity(new Intent("com.modular.appme.UURanking"));
                break;
            case 4:
                if (isB2b) {
                    ct.startActivity(new Intent(ct, ProcessB2BActivity.class));
                } else {
                    ct.startActivity(new Intent(ct, ApprovalNewListActivity.class));
                }
                break;
            case 5:
                if (isB2b) {
                    ct.startActivity(new Intent("com.modular.task.TaskB2BActivity"));
                } else {
                    ct.startActivity(new Intent("com.modular.task.TaskActivity"));
                }
                break;
            case 6:
                setSubReadTime(DateFormatUtil.long2Str(DateFormatUtil.YMD));
                ct.startActivity(new Intent(ct, Subscription2Activity.class));
                break;
            case REAL_TIME_FORM:
                intent = new Intent(ct, RealTimeFormActivity.class);
                ct.startActivity(intent);
                break;
            case BUSINESS_STATISTICS:
                intent = new Intent(ct, BusinessTargetsActivity.class);
                ct.startActivity(intent);
                break;
            case 9:
                intent = new Intent(ct, BusinessTravelActivity.class);
                ct.startActivity(intent);
                break;
            case 10:
                intent = new Intent(ct, PurchaseDetailsActivity.class).putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, "待回复");
                ct.startActivity(intent);
                break;
            case 21:
                intent = new Intent(ct, ScheduleActivity.class);
                ct.startActivity(intent);
                break;
            default:
                break;
        }
    }
}
