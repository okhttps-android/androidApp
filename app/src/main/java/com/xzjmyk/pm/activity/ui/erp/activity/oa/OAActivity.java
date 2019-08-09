package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.VersionUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.common.thread.ThreadUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUAS;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.Employees;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonInterface;
import com.core.utils.TimeUtils;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.core.widget.view.selectcalendar.OACalendarView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.TaskAddErpActivity;
import com.uas.appworks.CRM.erp.activity.UserSelectActivity;
import com.uas.appworks.OA.erp.activity.AddMeetingActivity;
import com.uas.appworks.OA.erp.activity.DetailTaskActivity;
import com.uas.appworks.OA.erp.activity.ExpenseReimbursementActivity;
import com.uas.appworks.OA.erp.activity.MissionActivity;
import com.uas.appworks.OA.erp.activity.StatisticsActivity;
import com.uas.appworks.OA.erp.activity.WorkActivity;
import com.uas.appworks.OA.erp.activity.WorkDailyAddActivity;
import com.uas.appworks.OA.erp.activity.WorkReportMenuActivity;
import com.uas.appworks.OA.erp.activity.form.DataFormDetailActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.PlatLeaveAddActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.TravelActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.WorkExtraActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.crm.VisitReportAddActivity;
import com.xzjmyk.pm.activity.ui.erp.adapter.oa.OAAdapter;
import com.xzjmyk.pm.activity.ui.erp.adapter.oa.OACalenderViewPagerAdapter;
import com.xzjmyk.pm.activity.ui.erp.model.oa.OAModel;
import com.xzjmyk.pm.activity.ui.platform.task.TaskAddB2BActivity;
import com.xzjmyk.pm.activity.ui.platform.task.TaskDetailB2BActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.util.oa.OAHttpUtil;
import com.xzjmyk.pm.activity.view.crouton.Style;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by pengminggong on 2016/9/27.
 */
public class OAActivity extends SupportToolBarActivity implements View.OnClickListener, WorkDailyAddActivity.OnFinishOa, OnHttpResultListener {
    private final int LOAD_ORDERSCALL = 0x768;//获取动态caller
    private final int LOAD_SUBORDINATE = 0x767;//获取下属数据
    private final int LOAD_MENU_DATA = 0x766;//获取菜单数据
    private final int LOAD_NET_DATA = 0x765;//获取列表数据

    @ViewInject(R.id.oa_rl_choose)
    private LinearLayout oa_rl_choose;
    @ViewInject(R.id.yeas_tv)
    private TextView yeas_tv;
    @ViewInject(R.id.day_tv)
    private TextView day_tv;
    @ViewInject(R.id.viewPager)
    private ViewPager viewPager;
    @ViewInject(R.id.all_task_num)
    private TextView all_task_num;
    @ViewInject(R.id.ok_task_num)
    private TextView ok_task_num;
    @ViewInject(R.id.uok_task_num)
    private TextView uok_task_num;
    @ViewInject(R.id.week_tv)
    private TextView week_tv;
    @ViewInject(R.id.tv_signin_num)
    private TextView tv_signin_num;
    @ViewInject(R.id.tv_signout_num)
    private TextView tv_signout_num;
    @ViewInject(R.id.oa_listview)
    private MyListView oa_listview;
    @ViewInject(R.id.subord_log)
    private TextView subord_log;
    @ViewInject(R.id.my_log)
    private TextView my_log;
    @ViewInject(R.id.subord_log_tag)
    private View subord_log_tag;
    @ViewInject(R.id.my_log_tag)
    private View my_log_tag;
    @ViewInject(R.id.ok_scale)
    private ImageView ok_scale;
    @ViewInject(R.id.oa_more_menu_ll)
    private LinearLayout mMoreMenuLl;
    @ViewInject(R.id.more_menu_tv)
    private TextView mMoreMenuTv;
    @ViewInject(R.id.oamain_more_menu_ll)
    private LinearLayout mGetMoreMenuLl;
    @ViewInject(R.id.oamain_more_menu_iv)
    private ImageView mMoreMenuIv;
    @ViewInject(R.id.oa_more_cancel_ll)
    private LinearLayout mCancleMoreMenuLl;
    @ViewInject(R.id.oamain_ptrsv)
    private PullToRefreshScrollView mPullToRefreshScrollView;
    @ViewInject(R.id.oamain_sign_statistical_notice_tv)
    private TextView oamain_sign_statistical_notice_tv;
    @ViewInject(R.id.expense_reimbursement_hd_tv)
    private TextView expense_reimbursement_hd_tv;
    @ViewInject(R.id.meeting)
    private TextView meeting;
    @ViewInject(R.id.oamain_special_attendance_tv)
    private TextView special_attendance;

    private int mMenuFlag = 0;
    private EmptyLayout emptyLayout;
    private boolean isCrm = false;
    private boolean isMe = false;
    private boolean hasOther = true;
    private Animation mAnimation;
    private OAHttpUtil util = new OAHttpUtil();
    private List<OAModel> meModels;
    private List<OAModel> otherModels;
    private OAAdapter adapter; //当前列表的数据
    private OACalenderViewPagerAdapter pagerAdapter;//当前Viewpager适配器
    private Date curDate;    //当前显示的日期  包含年月日信息
    private Set<Integer> meInt;     //我的任务
    private Set<Integer> otherInt;  //我的下属的任务
    private int posItem;           //当前为滑动到的哪个Viewpager
    private List<Map<String, Object>> menusMap = new ArrayList<>();//菜单动态数据
    private int loginNum = 0;//重新登陆次数
    private String caller_workextra, caller_travel;
    private boolean scrollable = true;
    private float downY;
    private float upY;
    private float moveY;
    private boolean isB2b;
    private OACalendarView calendarView;
    private String workDailyAdd;
    private long lastTime;
    @ViewInject(R.id.release_switch_tv3)
    private TextView release_switch_tv3;
    @ViewInject(R.id.oamain_expense_reimbursement_ll)
    private LinearLayout oamain_expense_reimbursement_ll;
    @ViewInject(R.id.oamain_more_menu_hd_tv)
    private TextView oamain_more_menu_hd_tv;
    private TextView mWorkReport;
    boolean isPlatform = ApiUtils.getApiModel() instanceof ApiPlatform;
    private View otherEmptyView;
    private View meEmptyEView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isCrm) {
                startActivity(new Intent(activity, MainActivity.class));
            } else {
                startActivity(new Intent(activity, MainActivity.class));
            }
            finish();
        } else if (item.getItemId() == R.id.add_item) {
            if (isCrm) {//从客户
                startActivityForResult(getVisitClass(ct), 0x20);
            } else
                showPopupWindow(getWindow().findViewById(R.id.add_item));
        } else {
            return super.onOptionsItemSelected(item);

        }
        return true;
    }


    @Override
    protected void onResume() {
        int new_function_notice = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.NEW_FUNCTION_NOTICE);
        if (new_function_notice == 1) {
            oamain_sign_statistical_notice_tv.setVisibility(View.INVISIBLE);
        } else {
            oamain_sign_statistical_notice_tv.setVisibility(View.VISIBLE);
        }

        int new_expense_reimbursement_notice = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.NEW_EXPENSE_REIMBURSEMENT_NOTICE);
        if (new_expense_reimbursement_notice == 1) {
            expense_reimbursement_hd_tv.setVisibility(View.INVISIBLE);
        } else {
            expense_reimbursement_hd_tv.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (isCrm) {
            // startActivity(new Intent(activity, ClientActivity.class));
        } else {
            startActivity(new Intent(activity, MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x20) {
            loadNetData(DateFormatUtil.date2Str(curDate, "yyyyMM"));
        } else if (requestCode == 0x11 && resultCode == 0x11) {//选择下属
            if (data == null) return;
            Employees entity = data.getParcelableExtra("data");
            if (entity == null) return;
            String otherName = entity.getEm_name();
            if (!StringUtil.isEmpty(otherName) && !ListUtils.isEmpty(otherModels)) {
                List<OAModel> filterModels = new ArrayList<>();
                for (OAModel e : otherModels) {
                    if (StringUtil.isInclude(e.getHandler(), otherName))
                        filterModels.add(e);
                }
                Set<Integer> filterTasks = util.getTaskList(curDate, filterModels);
                setDateToListener(filterTasks);
                setAdapterBeans(filterModels);
            }
        } else if (requestCode == 0x21) {
            loadNetData(DateFormatUtil.date2Str(curDate, "yyyyMM"));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (isB2b) {
            setContentView(R.layout.plat_oamain);
        } else {
            setContentView(R.layout.activity_oamain);
        }
        ViewUtils.inject(this);
        initView();
        initEvent();
    }


    private void initEvent() {
        findViewById(R.id.signin).setOnClickListener(this);
        findViewById(R.id.outoffice).setOnClickListener(this);
        findViewById(R.id.meeting).setOnClickListener(this);
        mWorkReport = (TextView) findViewById(R.id.worklog);
        if (isPlatform) {
            mWorkReport.setText(R.string.oaworkdaily_title);
        } else {
            mWorkReport.setText(R.string.str_work_report);
        }
        mWorkReport.setOnClickListener(this);
        findViewById(R.id.erp).setOnClickListener(this);
        findViewById(R.id.outtask).setOnClickListener(this);
        findViewById(R.id.subord_log).setOnClickListener(this);
        findViewById(R.id.my_log).setOnClickListener(this);
        findViewById(R.id.ok_scale).setOnClickListener(this);
        findViewById(R.id.oamain_sign_statistical_tv).setOnClickListener(this);
        findViewById(R.id.oamain_expense_reimbursement_ll).setOnClickListener(this);
        mGetMoreMenuLl.setOnClickListener(this);
        mMoreMenuLl.setOnClickListener(this);
        mCancleMoreMenuLl.setOnClickListener(this);
        findViewById(R.id.oamain_overtime_apply_tv).setOnClickListener(this);
        findViewById(R.id.oamain_special_attendance_tv).setOnClickListener(this);
        oa_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String master = CommonUtil.getMaster();
                if ("DATACENTER".equals(master) || "N_SHYZ".equals(master) || "N_AJC".equals(master)) {
                    return;
                }

                if (adapter == null || ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= i)
                    return;
                OAModel e = adapter.getModels().get(i);
                if (e == null || StringUtil.isEmpty(e.getJson()) || !JSONUtil.validate(e.getJson()))
                    return;
                JSONObject object = JSON.parseObject(e.getJson());
                Intent intent = null;
                if (e.isTask()) {
                    if (isB2b)
                        intent = new Intent(ct, TaskDetailB2BActivity.class);
                    else intent = new Intent(ct, DetailTaskActivity.class);
                    LogUtil.i(JSONUtil.getText(object, "department", "detail", "description"));
                    LogUtil.i(JSONUtil.getText(object, "description"));
                    intent.putExtra("description", JSONUtil.getText(object, "department", "detail", "description"));
                    intent.putExtra("isMe", isMe);
                    intent.putExtra("duration", JSONUtil.getText(object, "description", "department", "detail"));
                    intent.putExtra("status", JSONUtil.getText(object, "status"));
                    intent.putExtra("taskname", JSONUtil.getText(object, "name", "taskname"));//名称
                    intent.putExtra("taskemcode", JSONUtil.getText(object, "recorder"));//发起人
                    intent.putExtra("performer", JSONUtil.getText(object, "handler", "doman"));//处理人编号
                    intent.putExtra("taskcode", object.getString("taskcode"));//处理人编号
                    intent.putExtra("ra_taskid", String.valueOf(JSONUtil.getInt(object, "ra_taskid", "ra_id", "id")));//取回复内容id
                    intent.putExtra("taskid", String.valueOf(JSONUtil.getInt(object, "ra_id", "ra_taskid", "id")));//编号
                    if (isB2b) {
                        intent.putExtra("tasktime", TimeUtils.f_long_2_str(JSONUtil.getLong(object, "startdate")));//发起时间
                        intent.putExtra("endtime", TimeUtils.f_long_2_str(JSONUtil.getLong(object, "enddate")));//发起时间
                    } else {
                        intent.putExtra("endtime", object.getString("enddate"));
                        String tasktime = JSONUtil.getText(object, "startdate");
                        if (!StringUtil.isEmpty(tasktime)) {
                            tasktime = DateFormatUtil.long2Str(TimeUtils.f_str_2_long(tasktime), "yyyy-MM-dd HH:mm");
                        }
                        intent.putExtra("tasktime", tasktime);//发起时间
                    }
                    startActivityForResult(intent, isMe ? 0x20 : 0x21);
                } else if (e.isVisitRecord()) {
                    intent = getVisitClass(ct);
                    intent.putExtra("type", 2);
                    String chche = object.containsKey("status") ? object.getString("status") == null ? "" : object.getString("status") : "";
                    boolean me = (isMe && !"已拜访".equals(chche) && !"".equals(chche));
                    intent.putExtra("isMe", isMe);
                    intent.putExtra("isAgen", true);
                    intent.putExtra("data", object.toString());
                    startActivityForResult(intent, isMe ? 0x20 : 0x21);
                } else if (e.isMission()) {
                    if (util.isMissionOk(e)) {//外勤计划
                        intent = getVisitClass(ct);
                        intent.putExtra("type", 3);
                        intent.putExtra("isOutplan", true);
                        intent.putExtra("isMe", isMe);//可以提交拜访报告
                        intent.putExtra("data", object.toString());
                        startActivityForResult(intent, isMe ? 0x20 : 0x21);
                    } else {
                        showToast(getString(R.string.outplan_undone));
                    }
                }
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Calendar c = Calendar.getInstance();
                c.setTime(curDate);
                int day = c.get(Calendar.DAY_OF_MONTH);
                calendarView = pagerAdapter.getmViews().get(position);
                setCalenderOnClicklistener();
                posItem = position;
                if (day <= 0) return;  // update:2017-4-21-11:06 这里day未获取到闪退了 已做判空处理
                calendarView.setDownIndex(day);
                curDate = calendarView.getDownDate();
                setDateTag(curDate);
                if (CommonUtil.isNetWorkConnected(getApplication())) {
                    loadNetData(DateFormatUtil.date2Str(curDate, "yyyyMM"));
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (CommonUtil.isNetWorkConnected(getApplicationContext())) {
                    loadNetData(DateFormatUtil.date2Str(curDate, "yyyyMM"));
                } else {
                    ViewUtil.ToastMessage(OAActivity.this, getString(R.string.common_notlinknet), Style.holoRedLight, 2000);
                    if (mPullToRefreshScrollView.isRefreshing()) {
//                        requestSuccess();
                        mPullToRefreshScrollView.onRefreshComplete(1000);
                    }
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {

            }
        });
        doReleaeSwitch();
    }

    private void initView() {
        subord_log.setTextColor(getResources().getColor(R.color.approval_select_tab));
        subord_log_tag.setBackgroundResource(R.color.approval_select_tab);
        isCrm = !(getIntent() == null || getIntent().getIntExtra("type", 0) != 1);
        Intent intent = getIntent();
        workDailyAdd = intent.getStringExtra("WorkDailyAdd");
        //设置为空显示列表
        emptyLayout = new EmptyLayout(ct, oa_listview);
        emptyLayout.setShowLoadingButton(false);
        emptyLayout.setShowEmptyButton(false);
        emptyLayout.setShowErrorButton(false);
        ViewGroup view = (ViewGroup) LayoutInflater.from(ct).inflate(R.layout.oa_empty_list, null);
        otherEmptyView = view.findViewById(R.id.otherView);
        meEmptyEView = view.findViewById(R.id.meView);
        emptyLayout.setEmptyView(view);
        adapter = new OAAdapter(ct, util, new ArrayList<OAModel>());
        oa_listview.setAdapter(adapter);
        curDate = new Date(System.currentTimeMillis());
        meInt = new HashSet<>();
        otherInt = new HashSet<>();
        String title = "";
        if (isCrm) {
            oa_rl_choose.setVisibility(View.GONE);
            title = getString(R.string.crmmain_customer_visit);
            subord_log.setText(getString(R.string.Subordinates_visit));
            my_log.setText(getString(R.string.my_visit));
        } else {
            title = getString(R.string.work_calender);
        }
        setTitle(title);
        setDateTag(new Date());
        if (!isB2b) {
            loadSubordinate();
            getMenuData();
        }
        loadNetData(String.valueOf(DateFormatUtil.long2Str("yyyyMM")));
        posItem = OACalenderViewPagerAdapter.MAX_NUM / 2;
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viewPager.getLayoutParams(); // 取控件mGrid当前的布局参数   搜索
        linearParams.height = (getResources().getDisplayMetrics().heightPixels * 1 / 3);// 当控件的高强制设成50象素
        viewPager.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件myGrid
        pagerAdapter = new OACalenderViewPagerAdapter(ct, curDate);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(OACalenderViewPagerAdapter.MAX_NUM / 2);
        //判断是否是管理员
        CommonInterface.getInstance().judgeManager(null);
        CommonInterface.getInstance().getOutSetInfo(null);

        doShowSpecialView();
        //获取动态表单caller
//        loadOrdersCall();

    }

    private void doShowSpecialView() {
        // 考勤统计红点
        int new_function_notice = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.NEW_FUNCTION_NOTICE);
        if (new_function_notice == 1) {
            oamain_sign_statistical_notice_tv.setVisibility(View.INVISIBLE);
        } else {
            oamain_sign_statistical_notice_tv.setVisibility(View.VISIBLE);
        }

        // 报销单红点
        int new_expense_reimbursement_notice = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.NEW_EXPENSE_REIMBURSEMENT_NOTICE);
        if (new_expense_reimbursement_notice == 1) {
            expense_reimbursement_hd_tv.setVisibility(View.INVISIBLE);
        } else {
            expense_reimbursement_hd_tv.setVisibility(View.VISIBLE);
        }

        if (isB2b) {
            meeting.setVisibility(View.INVISIBLE);
            special_attendance.setVisibility(View.INVISIBLE);
        }
        if (CommonUtil.getSharedPreferencesBoolean(MyApplication.getInstance(), Constants.new_signin)) {
            tv_signin_num.setVisibility(View.GONE);
        }
        if (CommonUtil.getSharedPreferencesBoolean(MyApplication.getInstance(), Constants.new_signout)) {
            tv_signout_num.setVisibility(View.GONE);
        }

        //更多红点
        int more_function = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.MORE_FUNCTION);
        if (more_function == 1) {
            oamain_more_menu_hd_tv.setVisibility(View.GONE);
        } else {
            oamain_more_menu_hd_tv.setVisibility(View.VISIBLE);
        }
    }

    private void doReleaeSwitch() {
        release_switch_tv3.setVisibility(View.GONE);
        oamain_expense_reimbursement_ll.setVisibility(View.VISIBLE);
    }


    /*请求列表数据*/
    public void loadNetData(String /*yyyyMM*/date) {
        progressDialog.show();
        final Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(this, "erp_username"));
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().workSchedule :
                "mobile/crm/gettaskscheduleandvisitplanmsg.action";
        param.put("date", date);
        Bundle bundle = new Bundle();
        bundle.putString("date", date);
        Request request = new Request.Bulider()
                .setUrl(url)
                .setParam(param)
                .setBundle(bundle)
                .setWhat(LOAD_NET_DATA)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /*重新登陆*/
    private void returnLogin() {
        loginNum++;
        if (loginNum > 1) return;
        ViewUtil.ct = ct;
        ViewUtil.LoginERPTask(ct, ViewUtil.handler, 0x16);
    }

    /*获取索凌菜单接口*/
    private void getMenuData() {
        String url = "mobile/oa/getmenuconfig.action";
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getEmcode());
        Request request = new Request.Bulider()
                .setUrl(url)
                .setParam(param)
                .setWhat(LOAD_MENU_DATA)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /*获取下属*/
    private void loadSubordinate() {
        progressDialog.show();
        String url = "mobile/crm/getstaffmsg.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getEmcode());
        Request request = new Request.Bulider()
                .setUrl(url)
                .setParam(param)
                .setWhat(LOAD_SUBORDINATE)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    /*获取动态caller*/
    public void loadOrdersCall() {
        if (isB2b) return;
        String url = "mobile/oa/getoaconifg.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setParam(new HashMap<String, Object>())
                .setWhat(LOAD_ORDERSCALL)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (!isJSON) return;
        try {
            JSONObject object = JSON.parseObject(message);
            switchResult(what, object, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchResult(int what, JSONObject object, Bundle bundle) throws Exception {
        if (mPullToRefreshScrollView.isRefreshing()) {
            mPullToRefreshScrollView.onRefreshComplete();
        }
        switch (what) {
            case LOAD_NET_DATA:
                boolean isCurDate = true;
                String date = DateFormatUtil.date2Str(curDate, "yyyyMM");
                if (bundle != null) {
                    date = bundle.getString("date");
                    isCurDate = !StringUtil.isEmpty(date) && date.equals(DateFormatUtil.date2Str(curDate, "yyyyMM"));
                }
                if (isCurDate)
                    handlerListData(object, date);
                progressDialog.dismiss();
                break;
            case LOAD_MENU_DATA:
                handlerMenuData(JSONUtil.getJSONArray(object, "listdata"));
                break;
            case LOAD_SUBORDINATE:
                if (ListUtils.isEmpty(object.getJSONArray("datas"))) {//无下属
                    hasOther = false;
                    chaneTAG(true);
                }
                break;
            case LOAD_ORDERSCALL:
                handlerOrderscall(JSONUtil.getJSONArray(object, "listdata"));
                break;
            default:
                handerError("", object);
                break;
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        try {
            handerError(message, JSONUtil.validate(message) ? JSON.parseObject(message) : null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handlerListData(final JSONObject jsonObject, final String date) throws Exception {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    handlerDataInThread(jsonObject, date);
                } catch (Exception e) {

                }
            }
        });
    }

    private synchronized void handlerDataInThread(final JSONObject jsonObject, final String date) throws Exception {
        meModels = util.getModelByJSON(jsonObject, "me", isCrm, true);
        otherModels = util.getModelByJSON(jsonObject, "other", isCrm, false);
        meInt = util.getTaskList(curDate, meModels);
        LogUtil.d("Test", JSON.toJSONString(meInt) + "  model:" + JSON.toJSONString(meModels));
        otherInt = util.getTaskList(curDate, otherModels);
        OAHttpHelper.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (!StringUtil.isEmpty(date) && !date.equals(DateFormatUtil.date2Str(curDate, "yyyyMM")))
                    return;
                if (!jsonObject.containsKey("other") && !isMe) {
                    hasOther = false;
                    chaneTAG(true);
                }
                loadSuccess();
            }
        });
    }

    private void handlerMenuData(JSONArray menuArray) throws Exception {
        if (ListUtils.isEmpty(menuArray)) return;
        for (int i = 0; i < menuArray.size(); i++) {
            JSONObject object = menuArray.getJSONObject(i);
            String MM_CALLER = object.getString("MM_CALLER");
            String MM_NAME = object.getString("MM_NAME");
            Map<String, Object> temp = new LinkedHashMap<>();
            temp.put("item_name", MM_NAME);
            temp.put("item_caller", MM_CALLER);
            menusMap.add(temp);
        }
    }

    private void handlerOrderscall(JSONArray array) throws Exception {
        if (ListUtils.isEmpty(array)) return;
        for (int i = 0; i < array.size(); i++) {
            JSONObject jb = array.getJSONObject(i);
            if ("加班申请".equals(jb.getString("MO_NAME"))) {
                caller_workextra = jb.getString("MO_CALLER");
            }
            if ("出差申请".equals(jb.getString("MO_NAME"))) {
                caller_travel = jb.getString("MO_CALLER");
            }
        }
    }

    private void handerError(String message, JSONObject object) throws Exception {
        progressDialog.dismiss();
        if (mPullToRefreshScrollView.isRefreshing()) {
            loadSuccess();
            mPullToRefreshScrollView.onRefreshComplete();
        }
        if (StringUtil.isInclude(message, getString(R.string.talking_lost))) {
            returnLogin();
            return;
        }
        String error = "";
        if (object != null) {
            error = JSONUtil.getText(object, "exceptionInfo");
        } else {
            error = StringUtil.isEmpty(message) ? getString(R.string.Program_error) : StringUtil.getChinese(message);
        }
        if (!getString(R.string.Program_error).equals(error))
            error = getString(R.string.error_notice1);
        if (System.currentTimeMillis() - lastTime > 8000) {
            lastTime = System.currentTimeMillis();
            if (!StringUtil.isEmpty(workDailyAdd) && "WorkDailyAdd".equals(workDailyAdd)) {
            } else {
                showToast(error);
            }
        }

    }


    private void loadSuccess() {//下拉成功后添加到适配器
        setAdapterBeans();
        setDateToListener(isMe ? meInt : otherInt);
        setCalenderOnClicklistener();
    }

    private void setAdapterBeans() {
        setAdapterBeans(isMe ? meModels : otherModels);
    }

    private void showEmpty() {
        if (isMe) {
            meEmptyEView.setVisibility(View.VISIBLE);
            otherEmptyView.setVisibility(View.GONE);
        } else {
            meEmptyEView.setVisibility(View.GONE);
            otherEmptyView.setVisibility(View.VISIBLE);
        }
        emptyLayout.showEmpty();
    }

    private void setAdapterBeans(List<OAModel> showModel) {
        if (ListUtils.isEmpty(showModel)) {
            adapter.setModels(showModel);
            adapter.notifyDataSetChanged();
            showEmpty();
            CommonUtil.textSpanForStyle(all_task_num, getString(R.string.work_calender) + "   " + 0, 0 + "", getResources().getColor(R.color.approval_select_tab));
            CommonUtil.textSpanForStyle(ok_task_num, getString(R.string.done) + "   " + 0, 0 + "", getResources().getColor(R.color.approval_select_tab));
            CommonUtil.textSpanForStyle(uok_task_num, getString(R.string.undone) + "   " + 0, 0 + "", getResources().getColor(R.color.approval_select_tab));
            return;
        }
        int ok = 0, size = 0;
        List<OAModel> filterModels = new ArrayList<>();
        for (OAModel e : showModel) {
            if (util.canShow(e, curDate)) {
                size++;
                filterModels.add(e);
                if (util.isTaskOk(e))
                    ok++;
            }
        }
        if (ListUtils.isEmpty(filterModels)) {
            showEmpty();
        }
        CommonUtil.textSpanForStyle(all_task_num, getString(R.string.work_calender) + "   " + size, size + "", getResources().getColor(R.color.approval_select_tab));
        CommonUtil.textSpanForStyle(ok_task_num, getString(R.string.done) + "   " + ok, ok + "", getResources().getColor(R.color.approval_select_tab));
        CommonUtil.textSpanForStyle(uok_task_num, getString(R.string.undone) + "   " + (size - ok), (size - ok) + "", getResources().getColor(R.color.approval_select_tab));
        //当前日期小于等于选中日期
        if (adapter == null) {
            adapter = new OAAdapter(ct, util, filterModels);
            oa_listview.setAdapter(adapter);
        } else {
            adapter.setModels(filterModels);
            adapter.notifyDataSetChanged();
        }
    }

    //弹出菜单
    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = view.findViewById(R.id.mList);
            SimpleAdapter adapter = new SimpleAdapter(ct, getPopData(),
                    R.layout.item_pop_list, new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LogUtil.i("onItemClick");
                    Intent intent = null;
                    popupWindow.dismiss();
                    switch (position) {
                        case 0:
                            if (ApiUtils.getApiModel() instanceof ApiPlatform)
                                intent = new Intent(ct, TaskAddB2BActivity.class);
                            else
                                intent = new Intent(ct, TaskAddErpActivity.class);
                            intent.putExtra("type", 0);
                            break;
                        case 1:
                            if (ApiUtils.getApiModel() instanceof ApiPlatform)
                                intent = new Intent(ct, TaskAddB2BActivity.class);
                            else
                                intent = new Intent(ct, TaskAddErpActivity.class);
                            intent.putExtra("type", 1);
                            break;
                        case 2:
                            intent = getVisitClass(ct);
                            break;
                        default:
                            String caller = (String) menusMap.get(position).get("item_caller");
                            String name = (String) menusMap.get(position).get("item_name");
                            LogUtil.d("caller:" + caller);
                            intent = new Intent(ct, DataFormDetailActivity.class)
                                    .putExtra("caller", caller)
                                    .putExtra("title", name);
                            break;
                    }
                    if (intent != null) {
                        startActivityForResult(intent, 0x20);
                    }
                }
            });
            popupWindow = new PopupWindow(view);
//                    new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() / 3, windowManager.getDefaultDisplay().getHeight() / 3);
            popupWindow.setWidth(DisplayUtil.dip2px(ct, 140));
            popupWindow.setHeight(DisplayUtil.dip2px(ct, 135));
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(OAActivity.this, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.top_bubble));
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(parent, -DisplayUtil.dip2px(ct, 50), 0);
    }

    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        if (getIntent() == null || getIntent().getIntExtra("type", 0) != 1) {
            map.put("item_name", getString(R.string.oacreat_task));
            list.add(map);
            map = new HashMap<>();
            map.put("item_name", getString(R.string.oacreat_calender));
            list.add(map);
            map = new HashMap<>();
        }
        String master = CommonUtil.getMaster();
        if (!("DATACENTER".equals(master) || "N_SHYZ".equals(master) || "N_AJC".equals(master))) {
            map.put("item_name", getString(R.string.crm_add_visitrecord));
            list.add(map);
        }
        if (menusMap != null) {
            if (menusMap.size() != 0) {
                for (int i = 0; i < menusMap.size(); i++) {
                    list.add(menusMap.get(i));
                }
            }
        }
        menusMap = list;
        return list;
    }

    //设置标签
    private void chaneTAG(boolean isMe) {
        this.isMe = isMe;
        if (isMe) {
            if (!isCrm && !subord_log.getText().toString().trim().equals(getString(R.string.oasubordinate_work_calendar))) {
                subord_log.setText(getString(R.string.oasubordinate_work_calendar));
            }
            setDateToListener(meInt);
            ok_scale.setVisibility(View.INVISIBLE);
            my_log.setTextColor(getResources().getColor(R.color.approval_select_tab));
            my_log_tag.setBackgroundResource(R.color.approval_select_tab);
            subord_log.setTextColor(getResources().getColor(R.color.text_main));
            subord_log_tag.setBackgroundResource(R.color.item_line);
        } else {
            setDateToListener(otherInt);
            ok_scale.setVisibility(View.VISIBLE);
            my_log.setTextColor(getResources().getColor(R.color.text_main));
            subord_log.setTextColor(getResources().getColor(R.color.approval_select_tab));
            subord_log_tag.setBackgroundResource(R.color.approval_select_tab);
            my_log_tag.setBackgroundResource(R.color.item_line);
        }
        setAdapterBeans();
    }


    private void setDateTag(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int yeas = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
//        yeas_tv.setText(yeas + getString(R.string.common_year) + month + getString(R.string.common_month));
//        day_tv.setText(day + getString(R.string.common_day));
        yeas_tv.setText(yeas + "年" + month + "月");
        day_tv.setText(day + "日");
        week_tv.setText(CalendarUtil.getWeek(DateFormatUtil.getFormat(DateFormatUtil.YMD).format(date)));
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.ok_scale:  //筛选
                intent = new Intent(ct, UserSelectActivity.class);
                intent.putExtra("single", true);
                intent.putExtra("net", true);
                startActivityForResult(intent, 0x11);
                break;
            case R.id.signin://打卡签到
//                startActivity(new Intent(activity, SigninActivity.class));
                intent = new Intent(activity, WorkActivity.class);
                boolean isAdmin = PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false);
                intent.putExtra(AppConfig.IS_ADMIN, isAdmin);//上传管理员状态
                startActivity(intent);
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), Constants.new_signin, true);
                tv_signin_num.setVisibility(View.GONE);
                break;
            case R.id.outoffice:  //外勤签到
                boolean is = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
                if (isB2b || is) {
                    intent = new Intent(activity, MissionActivity.class);
                    intent.putExtra("flag", 1);
                } else {
                    intent = new Intent(activity, OutofficeActivity.class);
                }
                boolean isAdmain = PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false);
                intent.putExtra(AppConfig.IS_ADMIN, isAdmain);//上传管理员状态
                startActivityForResult(intent, 0x21);
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), Constants.new_signout, true);
                tv_signout_num.setVisibility(View.GONE);
                break;
            case R.id.meeting://会议管理
                startActivity(new Intent(activity, AddMeetingActivity.class));
                break;
            case R.id.worklog://工作日志
                if (isPlatform) {
                    startActivity(new Intent(activity, WorkDailyAddActivity.class));
                } else {
                    startActivity(new Intent(activity, WorkReportMenuActivity.class));
                }
                break;
            case R.id.subord_log://我的下属工作日志
                if (isMe)
                    if (hasOther) {
                        chaneTAG(false);
                    } else {
                        showToast(getString(R.string.account_notice1));
                    }
                break;
            case R.id.my_log://我的工作日志
                if (!isMe)
                    chaneTAG(true);
                break;
            case R.id.oamain_more_menu_ll://更多
                PreferenceUtils.putInt(Constants.MORE_FUNCTION, 1);
                oamain_more_menu_hd_tv.setVisibility(View.GONE);
                if (!isB2b) {
                    if (mMenuFlag % 2 == 0) {
                        mAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_button);
                        mMoreMenuLl.setVisibility(View.VISIBLE);
                        mMoreMenuTv.setText(getString(R.string.oapackup_title));
                        scrollable = false;
                    } else {
                        mAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_button2);
                        mMoreMenuLl.setVisibility(View.GONE);
                        mMoreMenuTv.setText(getString(R.string.oamore_tltle));
                        scrollable = true;
                    }
                    mMoreMenuIv.startAnimation(mAnimation);
                    mMenuFlag++;
                } else {
                    ToastMessage(getString(R.string.nothing_now));
                }
                break;
            case R.id.oa_more_cancel_ll:
                mAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_button2);
                mMoreMenuIv.startAnimation(mAnimation);
                mMoreMenuTv.setText(getString(R.string.oamore_tltle));
                mMoreMenuLl.setVisibility(View.GONE);
                scrollable = true;
                mMenuFlag++;
                break;
            case R.id.erp://请假申请
                // startActivity(new Intent(activity, ErpMenActivity.class));//旧的考勤界面
                if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                    startActivity(new Intent(activity, PlatLeaveAddActivity.class));
                } else if (ApiUtils.getApiModel() instanceof ApiUAS) {
                    startActivity(new Intent(activity, DataFormDetailActivity.class).putExtra("caller", "Ask4Leave"));
                }
                break;
            case R.id.outtask://出差申请
                if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                    startActivity(new Intent(activity, TravelActivity.class));
                } else if (ApiUtils.getApiModel() instanceof ApiUAS) {
                    if (StringUtil.isEmpty(caller_travel)) {
                        startActivity(new Intent(activity, DataFormDetailActivity.class)
                                .putExtra("caller", "FeePlease!CCSQ"));
                    } else {
                        startActivity(new Intent(activity, DataFormDetailActivity.class)
                                .putExtra("caller", caller_travel));
                    }
                }
                break;
            case R.id.oamain_overtime_apply_tv://加班申请
                if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                    startActivity(new Intent(activity, WorkExtraActivity.class));
                } else if (ApiUtils.getApiModel() instanceof ApiUAS) {
                    if (StringUtil.isEmpty(caller_workextra)) {
                        startActivity(new Intent(activity, DataFormDetailActivity.class)
                                .putExtra("caller", "Workovertime"));
                    } else {
                        startActivity(new Intent(activity, DataFormDetailActivity.class)
                                .putExtra("caller", caller_workextra));
                    }

                }
                break;
            case R.id.oamain_special_attendance_tv://特殊考勤
                if (!(ApiUtils.getApiModel() instanceof ApiPlatform)) {
                    startActivity(new Intent(activity, DataFormDetailActivity.class).putExtra("caller", "SpeAttendance"));
                }
                break;
            case R.id.oamain_sign_statistical_tv: //考勤统计
                startActivity(new Intent(activity, StatisticsActivity.class));
                PreferenceUtils.putInt(Constants.NEW_FUNCTION_NOTICE, 1);
                break;
            case R.id.oamain_expense_reimbursement_ll:  //费用报销
                startActivity(new Intent(activity, ExpenseReimbursementActivity.class));
                PreferenceUtils.putInt(Constants.NEW_EXPENSE_REIMBURSEMENT_NOTICE, 1);
                break;
        }
    }

    public void setDateToListener(Set<Integer> in) {
        OACalendarView calendarView = getCalendarView();
        if (calendarView != null)
            calendarView.setDecoratDays(in);
    }

    private void setCalenderOnClicklistener() {
        OACalendarView calendarView = getCalendarView();
        if (calendarView != null)
            calendarView.setDateListener(new OACalendarView.OnSelectDateListener() {
                @Override
                public void result(Date date) {
                    setClickDay(date);
                }
            });
    }

    private OACalendarView getCalendarView() {
        if (calendarView == null && pagerAdapter != null && pagerAdapter.getmViews() != null) {
            calendarView = pagerAdapter.getmViews().get(posItem);
        }
        return calendarView;
    }

    @Override
    public void onFinish() {
        this.finish();
    }

    /*当点击了指定日期（点击日期、滑动时候触发）*/
    public void setClickDay(Date date) {
        curDate = date;
        setDateTag(date);
        setAdapterBeans();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (scrollable) {
            return super.dispatchTouchEvent(ev);
        } else {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downY = ev.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    upY = ev.getRawY();
                    if (Math.abs(upY - downY) > ViewConfiguration.get(OAActivity.this).getScaledTouchSlop()) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveY = ev.getRawY();
                    if (Math.abs(moveY - downY) > ViewConfiguration.get(OAActivity.this).getScaledTouchSlop()) {
                        return true;
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public Intent getVisitClass(Context ct) {
        return new Intent(ct, VersionUtil.canShowCrm2_0() ? AddVisitReportActivity.class : VisitReportAddActivity.class);
    }
}
