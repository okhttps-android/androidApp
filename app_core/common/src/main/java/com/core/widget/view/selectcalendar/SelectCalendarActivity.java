package com.core.widget.view.selectcalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUAS;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.view.selectcalendar.bean.Data;
import com.core.widget.view.selectcalendar.bean.DataState;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @desc:公共选择日历日期类
 * @author：Arison on 2017/1/18
 */
public class SelectCalendarActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private LinearLayout ll_date_start;
    private LinearLayout ll_date_end;
    private LinearLayout ll_time_start;
    private LinearLayout ll_time_end;
    private LinearLayout ll_bottom;
    private LinearLayout ll_time_point;//时间点
    private LinearLayout ll_time_top;

    private TextView tv_time_point;
    private TextView tv_date_start;
    private TextView tv_date_end;

    private TextView v_date_start;//时间开始
    private TextView v_date_end;//时间结束

    private TextView v_time_start;
    private TextView v_time_end;

    private GridView gv_date_list;
    private Button click_btn;

    private List<DataState> mTimeData = new ArrayList<>();
    private GridDataAdapter gAdapter;
    private PagerAdapter pagerAdapter;//当前Viewpager适配器

    private int MAX_PAGER = 30;
    private Date[] date = new Date[MAX_PAGER];

    private String currentSelectDate;

    private String field;

    private boolean startDate = true;
    private boolean startTime = true;

    private int status;//0:在录入（无值）1：在录入（有值）
    private boolean swich = false;//true 按时间段   false按时间
    private boolean isMenuShuffle = false;//默认隐藏
    private Boolean mWorkovertime = false;
    private String start_hour_min = "";
    private String end_hour_min = "";
    private String first_start_date;
    private String first_end_date;
    private String first_start_time;
    private String first_end_time;
    private Boolean breastfeeding = false;
    private String imId;
    private int type;//0:默认erp：1：小秘书
    private ArrayList<DataState> totals = new ArrayList<>();
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    parsedResult(msg.getData().getString("result"));
                    break;
                case 0x5:
                    LogUtil.d(msg.getData().getString("result"));
                    parsedResult(msg.getData().getString("result"));
                    break;
                case 0x06:
                    String result = msg.getData().getString("result");
                    final String resu = result;
                    try {

                        OAHttpHelper.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                parseBookTimeResult(resu);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x07:
                    result = msg.getData().getString("result");
                    final String endtime = JSON.parseObject(result).getString("endtime");
                    final String starttime = JSON.parseObject(result).getString("starttime");
                    OAHttpHelper.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!ListUtils.isEmpty(mTimeData)) {
                                    mTimeData.clear();
                                }
                                mTimeData.addAll(timeAddMuilt(starttime, endtime, 15));
                                gAdapter = new GridDataAdapter(mContext, mTimeData);
                                gv_date_list.setAdapter(gAdapter);
                                if (gAdapter.getCount() == 0) {
                                    initGridData();
                                }
                            } catch (Exception e) {
                                initGridData();
                            }

                            if (!StringUtil.isEmpty(imId)) {
                                getBookingTime(imId);
                            }
                        }
                    });

                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    initGridData();
                    break;
                case Constants.APP_NOTNETWORK:
                    initGridData();
                    break;
            }
        }
    };

    private String serviceId;
    private String businessType;
    private String companyId;
    private String bookType;
    private String bStartTime;
    private String bEndTime;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_date_select, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isMenuShuffle || breastfeeding) {
            menu.findItem(R.id.oa_leave).setVisible(true);
            menu.findItem(R.id.oa_signin_set).setVisible(true);
        } else {
            menu.findItem(R.id.oa_signin_set).setVisible(false);
            menu.findItem(R.id.oa_leave).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.oa_leave)
            if (swich) {
                //按时间请假
                setTitle("按时间请假");
                getSupportActionBar().setSubtitle("只适合普通请假");
                ll_bottom.setVisibility(View.GONE);
                swich = false;
                startDate = true;
                startTime = true;

                tv_date_start.setText("开始时间");
                tv_date_end.setText("结束时间");

                currentSelectDate = null;
                v_date_start.setText(first_start_date);
                v_date_end.setText(first_end_date);
                v_time_start.setText(first_start_time);
                v_time_end.setText(first_end_time);
                swichDateButton(true);
                ll_date_end.setEnabled(false);
                ll_time_end.setEnabled(false);
            } else {
                //按时间段请假
                setTitle("按时段请假");
                getSupportActionBar().setSubtitle("只适合特殊请假");
                ll_bottom.setVisibility(View.VISIBLE);
                swich = true;
                startDate = true;
                startTime = true;

                currentSelectDate = null;
                tv_date_start.setText("开始日期");
                tv_date_end.setText("结束日期");
                v_date_start.setText(first_start_date);
                v_date_end.setText(first_end_date);
                v_time_start.setText(first_start_time);
                v_time_end.setText(first_end_time);
                swichTimeButton(true);
                ll_date_end.setEnabled(false);
                ll_time_end.setEnabled(false);
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_calendar);
        initView();
        initData();
    }


    private Integer id;
    private Data object;

    private void initView() {
        setTitle("选择时间");
        ll_time_top = (LinearLayout) findViewById(R.id.ll_time_top);
        ll_time_point = (LinearLayout) findViewById(R.id.ll_time_point);
        tv_time_point = (TextView) findViewById(R.id.tv_time_point);
        ll_date_start = (LinearLayout) findViewById(R.id.ll_date_start);
        tv_date_start = (TextView) findViewById(R.id.tv_date_start);
        v_date_start = (TextView) findViewById(R.id.v_date_start);
        ll_date_end = (LinearLayout) findViewById(R.id.ll_date_end);
        v_date_end = (TextView) findViewById(R.id.v_date_end);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        ll_time_start = (LinearLayout) findViewById(R.id.ll_time_start);
        v_time_start = (TextView) findViewById(R.id.v_time_start);
        ll_time_end = (LinearLayout) findViewById(R.id.ll_time_end);
        v_time_end = (TextView) findViewById(R.id.v_time_end);
        gv_date_list = (GridView) findViewById(R.id.gv_date_list);
        click_btn = (Button) findViewById(R.id.click_btn);
        gv_date_list = (GridView) findViewById(R.id.gv_date_list);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tv_date_end = (TextView) findViewById(R.id.tv_date_end);
        v_date_start = (TextView) findViewById(R.id.v_date_start);
        swichDateButton(true);
        Intent intent = getIntent();
        String startDate = "";
        String endDate = "";
        if (intent != null) {
            type = intent.getIntExtra("type", 0); //默认是0
            field = intent.getStringExtra("field");
            id = intent.getIntExtra("id", 0);
            object = intent.getParcelableExtra("object");
            imId = intent.getStringExtra("imId");
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            //服务预约时间段参数
            serviceId = intent.getStringExtra("serviceId");
            businessType = intent.getStringExtra("businessType");
            bookType = intent.getStringExtra("bookType");
            companyId = intent.getStringExtra("companyId");
            bStartTime = intent.getStringExtra("startTime");
            bEndTime = intent.getStringExtra("endTime");
            //预约功能模块---时间段，时间点预约设置
            if (!StringUtil.isEmpty(bookType)) {
                if ("0".equals(bookType)) {
                    type = 3;//设置单一时间点
                } else if ("1".equals(bookType)) {
                    type = 4;//回到预约--设置时间段
                }
            }

            String mcaller = intent.getStringExtra("caller");
            String bf = intent.getStringExtra("breastfeeding");
            if (!StringUtil.isEmpty(bf) && "breastfeeding".equals(bf)) {
                breastfeeding = true;
            }
            if (!TextUtils.isEmpty(mcaller) && ("Workovertime".equals(mcaller) || "ExtraWork$".equals(mcaller))) {
                mWorkovertime = true;
            }
            if (type != 2&&type!=5) {
                v_date_start.setText(startDate);
                v_date_end.setText(startDate);
            }
            isMenuShuffle = intent.getBooleanExtra("hasMenu", false);
            LogUtil.d("start:" + startDate + " end:" + endDate + " field:" + field);
            if (!StringUtil.isEmpty(field)) {
                if (field.contains("end") || field.contains("date2")) {
                    //结束时间
                    swichDateButton(false);
                    this.startDate = false;
                }
            }

            if (type != 2&&type!=5) {
                if (!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)) {
                    v_date_start.setText(startDate.substring(0, startDate.length() - 3));
                    v_date_end.setText(endDate.substring(0, endDate.length() - 3));
                    currentSelectDate = startDate.substring(0, 10);
                    first_start_date = startDate.substring(0, 10);
                    first_end_date = endDate.substring(0, 10);
                    first_start_time = startDate.substring(11, startDate.length());
                    first_end_time = endDate.substring(11, endDate.length());
                    Log.v("startDate1", startDate);
                    Log.v("endDate1", endDate);
                    Log.v("v_date_start1", v_date_start.getText().toString());
                    Log.v("v_date_end1", v_date_end.getText().toString());
                    Log.d("currentSelectDate1", currentSelectDate);
                } else {
                    String currentDate = DateFormatUtil.long2Str("yyyy-MM-dd HH:mm");
                    v_date_start.setText(currentDate.substring(0, currentDate.length()));
                    v_date_end.setText(currentDate.substring(0, currentDate.length()));
                    currentSelectDate = currentDate.substring(0, 10);
                    first_start_date = currentDate.substring(0, 10);
                    first_end_date = currentDate.substring(0, 10);
                    first_start_time = currentDate.substring(11, currentDate.length());
                    first_end_time = currentDate.substring(11, currentDate.length());
                    Log.v("startDate2", currentDate);
                    Log.v("endDate2", currentDate);
                    Log.v("v_date_start2", v_date_start.getText().toString());
                    Log.v("v_date_end2", v_date_end.getText().toString());
                    Log.d("currentSelectDate2", currentSelectDate);
                }

                if (type == 3) {
                    ll_time_point.setVisibility(View.VISIBLE);
                    ll_time_top.setVisibility(View.GONE);
                    tv_time_point.setText(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm"));
                }
            }

        }
        initListener();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    private void initListener() {
        ll_date_start.setOnClickListener(this);
        ll_date_end.setOnClickListener(this);
        ll_time_start.setOnClickListener(this);
        ll_time_end.setOnClickListener(this);
        //ll_date_end.setEnabled(false);//不可点击
        ll_time_end.setEnabled(false);//不可点击
        click_btn.setOnClickListener(this);

        gv_date_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridDataAdapter.ViewModle modle = (GridDataAdapter.ViewModle) view.getTag();
                gAdapter.setSelected(position);
                gAdapter.notifyDataSetChanged();
                if (type == 1 || type == 4||type==3) {
                    if (!modle.clicked) {
                        ToastMessage("该时间段不能预约！");
                        return;
                    }
                }
                if (type == 3) {
                    tv_time_point.setText(tv_time_point.getText().toString().substring(0, 10) + " " + modle.tv_text.getText().toString());
                }
                if (type == 2||type==5) {
                    //按时间请假：
                    if (startDate) {//按钮状态
                        //开始时间
                        v_date_start.setText(modle.tv_text.getText().toString());
                        //切换到开始时间
                        swichDateButton(false);//要不要清楚日历状态？
                        startDate = false;
                        v_date_end.setText(v_date_start.getText().toString());
                        start_hour_min = modle.tv_text.getText().toString();
                    } else {
                        //结束时间
                        v_date_end.setText(modle.tv_text.getText().toString());
                        end_hour_min = modle.tv_text.getText().toString();

                    }
                    return;
                }

                if (StringUtil.isEmpty(currentSelectDate)) {
                    ToastMessage("请先选择日期,再选择时间段！");
                    return;
                }
                if ((StringUtil.isEmpty(currentSelectDate) &&
                        StringUtil.isEmpty(v_date_end.getText().toString())) ||
                        (StringUtil.isEmpty(currentSelectDate) &&
                                StringUtil.isEmpty(v_date_start.getText().toString()))) {
                    ToastMessage("请先选择日期,再选择时间段！");
                    return;
                }


                if (swich) {
                    //时间段请假：
                    if (startTime) {
                        v_time_start.setText(modle.tv_text.getText().toString());
                        swichTimeButton(false);
                        swichDateButton(false);
                        startTime = false;
                        startDate = false;
                        currentSelectDate = null;//清空
                        v_date_end.setText(v_date_start.getText().toString());
                        v_time_end.setText(v_time_start.getText().toString());
                    } else {
                        v_time_end.setText(modle.tv_text.getText().toString());
                    }
                } else {
                    //按时间请假：
                    if (startDate) {//按钮状态
                        //开始时间
                        v_date_start.setText(currentSelectDate + " " + modle.tv_text.getText().toString());
                        //切换到开始时间
                        swichDateButton(false);//要不要清楚日历状态？
                        startDate = false;
                        if (!StringUtil.isEmpty(v_date_start.getText().toString())) {
                            currentSelectDate = v_date_start.getText().toString().substring(0, 10);//清空
                            Log.d("currentSelectDate3", currentSelectDate);
                        } else {
                            currentSelectDate = null;//清空
                        }
                        v_date_end.setText(v_date_start.getText().toString());
                        start_hour_min = modle.tv_text.getText().toString();
                    } else {
                        //结束时间
                        Log.d("currentSelectDate4", currentSelectDate);
                        v_date_end.setText(currentSelectDate + " " + modle.tv_text.getText().toString());
                        end_hour_min = modle.tv_text.getText().toString();

                    }
                }
            }
        });

    }

    // TODO: 2017/10/16 本日历选择界面适配多种业务功能类型 
    // TODO: 支持不可选择的时间一律变灰色 
    private void initData() {
        switch (type) {
            case 0://普通请假时间段选择    动态获取时间段信息
                sendRequest();//获取班次信息
                break;
            case 1://预约时间段---繁忙赶时间段不可点击   固定 时间间隔十五分钟
                getBookingTotalTime(imId);//获取预约总时间
                break;
            case 2://预约时间段设置--在设置界面   固定时间段间隔 十五分钟
                initTimeData();
                viewPager.setVisibility(View.GONE);
                currentSelectDate = "";
                break;
            case 5://预约时间段设置--在设置界面  固定时间段间隔  半小时
                initTimeData();
                viewPager.setVisibility(View.GONE);
                currentSelectDate = "";
                break;
            case 3://服务预约 时间点选择(限定开始和截止时间段)   固定时间间隔半小时
                initTimeData();
                getBServiceTimes();
                break;
            case 4://服务预约 时间段选择(限定开始和截止时间段)  固定时间间隔半小时
                initTimeData();
                getBServiceTimes();
                break;
        }
        initCalender();
    }

    private int posItem;           //当前为滑动到的哪个Viewpager
    private Date curDate;    //当前显示的日期  包含年月日信息
    private int selectDay;

    private void initCalender() {
        setDate();
        selectDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        listenerMap = new HashMap<>();
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viewPager.getLayoutParams(); // 取控件mGrid当前的布局参数   搜索
        linearParams.height = (getResources().getDisplayMetrics().heightPixels * 1 / 3);// 当控件的高强制设成50象素
        viewPager.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件myGrid
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(MAX_PAGER / 2);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                posItem = position;
                Calendar c = Calendar.getInstance();
                c.setTime(date[position]);
                c.set(Calendar.DAY_OF_MONTH, selectDay);
                curDate = c.getTime();
                setDateTag(curDate);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private void setDateTag(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int yeas = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String today = DateFormatUtil.long2Str(DateFormatUtil.YMD);
        currentSelectDate = DateFormatUtil.getStrDate4Date(date, "yyyy-MM-dd");
//        if (today.compareTo(currentSelectDate) > 0 && type == 1){
//            ToastMessage("不能预约过去时间");
//            return;
//        }else {
        String m = "";
        String d = "";
        if (month < 10) {
            m = "0" + month;
        } else {
            m = month + "";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = day + "";
        }
        if (startDate) {
            v_date_start.setText(yeas + "-" + m + "-" + d + " " + start_hour_min);
            Log.d("Slide_date88", yeas + "-" + month + "-" + day + " " + start_hour_min);
        } else {
            v_date_end.setText(yeas + "-" + m + "-" + d + " " + end_hour_min);

            Log.d("Slide_date88", yeas + "-" + month + "-" + day + " " + end_hour_min);
        }

        getBookingTime(imId);
//        }

    }

    /**
     * @desc:初始化表格数据
     * @author：Arison on 2017/1/18
     */
    private void initGridData() {
        if (!ListUtils.isEmpty(mTimeData)) {
            mTimeData.clear();
        }
        if (type == 0 || type == 3) {
            initTimeData(30);
        } else {
            initTimeData(15);
        }
        if (mContext != null) {
            gAdapter = new GridDataAdapter(mContext, mTimeData);
            gv_date_list.setAdapter(gAdapter);
        }
    }

    private void initTimeData(int minute) {
        if (mWorkovertime) {
            mTimeData.clear();
            mTimeData.addAll(timeAddMuilt("00:00", "23:59", minute));
        } else {
            if (type == 0) {
                mTimeData.addAll(timeAddMuilt("08:30", "18:30", minute));
            } else {
                mTimeData.addAll(timeAddMuilt("08:00", "20:00", minute));
            }
        }
    }

    private void initTimeData() {
        if (!ListUtils.isEmpty(mTimeData))mTimeData.clear();
        if (type == 2) {
            mTimeData.addAll(timeAddMuilt("00:00", "23:45", 15));
            if (mContext != null) {
                gAdapter = new GridDataAdapter(mContext, mTimeData);
                gv_date_list.setAdapter(gAdapter);
            }
        }
        if (type==5){
            mTimeData.addAll(timeAddMuilt("00:00", "23:45", 30));
            if (mContext != null) {
                gAdapter = new GridDataAdapter(mContext, mTimeData);
                gv_date_list.setAdapter(gAdapter);
            }
        }
        if (type == 3 || type == 4) {//服务预约
            mTimeData.addAll(timeAddMuilt(bStartTime, bEndTime, 30));
            if (mContext != null) {
                gAdapter = new GridDataAdapter(mContext, mTimeData);
                gv_date_list.setAdapter(gAdapter);
            }
        }
    }

    public void parsedResult(String result) {
        if (result == null) {
            result = "{\"ifDefaultClass\":false,\"wd_degree\":2,\"wd_earlytime\":null,\"comAddressdata\":[{\"CS_WORKADDR\":\"深圳市南山区\",\"CS_VALIDRANGE\":\"300\",\"CS_ID\":10141,\"CS_LATITUDE\":\"22.540676518856678\",\"CS_LONGITUDE\":\"113.9528745854545\",\"CS_SHORTNAME\":\"宇声数码技术公司\",\"CS_CODE\":\"2017020234\",\"CS_INNERDISTANCE\":500,\"success\":true},{\"CS_WORKADDR\":\"广东省深圳市南山区高新区科技南六路29号万德莱大厦南座6楼\",\"CS_VALIDRANGE\":\"300\",\"CS_ID\":10143,\"CS_LATITUDE\":\"22.5416028163184\",\"CS_LONGITUDE\":\"113.95309916183191\",\"CS_SHORTNAME\":\"深圳市中兴供应链有限公司\",\"CS_CODE\":\"2017020236\",\"CS_INNERDISTANCE\":500,\"success\":true}],\"count\":null,\"wd_code\":\"TEST1\",\"Class3\":{\"wd_offend\":null,\"wd_onduty\":null,\"wd_offduty\":null,\"wd_onbeg\":null},\"Class2\":{\"wd_offend\":\"20:00\",\"wd_onduty\":\"13:30\",\"wd_offduty\":\"18:00\",\"wd_onbeg\":\"13:00\"},\"wd_id\":111281,\"Class1\":{\"wd_offend\":\"12:30\",\"wd_onduty\":\"08:30\",\"wd_offduty\":\"12:00\",\"wd_onbeg\":\"07:00\"},\"wd_pcount\":null,\"wd_name\":\"测试1\",\"ifNeedSignCard\":true,\"innerdistance\":null,\"distance\":null,\"sessionId\":\"729F70FB568EF25CC7F1CEE14A0900EE\",\"comaddressset\":false,\"longitude\":null,\"latitude\":null,\"success\":true,\"wd_day\":null}";
        }
        try {
            JSONObject root = JSON.parseObject(result);
            JSONObject Class1 = null;
            JSONObject Class2 = null;
            JSONObject Class3 = null;
            if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                Class1 =
                        root.getJSONObject("class1");
                Class2 =
                        root.getJSONObject("class2");
                Class3 =
                        root.getJSONObject("class3");
            } else {
                Class1 =
                        root.getJSONObject("Class1");
                Class2 =
                        root.getJSONObject("Class2");
                Class3 =
                        root.getJSONObject("Class3");
            }
            int type = 0;// 默认没有班次
            String wd_onduty = Class1.getString("wd_onduty");// 上班时间
            if (wd_onduty != null) {
                String wd_offduty = Class1.getString("wd_offduty");//结束时间
                type = 1;
                //获取时间累加，加半小时
                mTimeData.addAll(timeAddMuilt(wd_onduty, wd_offduty));
            }
            wd_onduty = Class2.getString("wd_onduty");// 上班时间
            if (wd_onduty != null) {
                String wd_offduty = Class2.getString("wd_offduty");//结束时间
                type = 2;
                mTimeData.addAll(timeAddMuilt(wd_onduty, wd_offduty));
            }
            wd_onduty = Class3.getString("wd_onduty");// 上班时间
            if (wd_onduty != null) {
                String wd_offduty = Class3.getString("wd_offduty");//结束时间
                type = 3;
                mTimeData.addAll(timeAddMuilt(wd_onduty, wd_offduty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ListUtils.isEmpty(mTimeData)) {
            initTimeData(30);
        }
        if (mWorkovertime) {
            initTimeData(30);
        }
        gAdapter = new GridDataAdapter(MyApplication.getInstance(), mTimeData);
        gv_date_list.setAdapter(gAdapter);
    }

    /**
     * 返回指定时间段内  循环累加半小时
     *
     * @param start
     * @param end   08:30-12:30  [首 +中间值累加半小时 + 尾]
     * @return
     */
    public List<DataState> timeAddMuilt(String start, String end) {
        LogUtil.d("Test", "timeAddMuilt start:");
        start = "2017-07-13 " + start;
        end = "2017-07-13 " + end;
        List<DataState> result = new ArrayList<>();
        DataState dataState = new DataState();
        dataState.setValue(start.substring(11, 16));
        dataState.setClicked(true);
        result.add(dataState);
        while (true) {
            start = DateFormatUtil.add(
                    DateFormatUtil.getDate4StrDate(start, "yyyy-MM-dd HH:mm"),
                    "yyyy-MM-dd HH:mm",
                    1 * 1 * 30 * 60 * 1000);
            System.out.println(start);
            if (start.compareTo(end) >= 0) {
                DataState dataState1 = new DataState();
                dataState1.setValue(end.substring(11, 16));
                dataState1.setClicked(true);
                result.add(dataState1);
                break;
            } else {
                DataState dataState2 = new DataState();
                dataState2.setValue(start.substring(11, 16));
                dataState2.setClicked(true);
                result.add(dataState2);
            }
        }
        return result;
    }


    /**
     * 返回指定时间段内  循环累加半小时
     *
     * @param start
     * @param end   08:30-12:30  [首 +中间值累加半小时 + 尾]
     * @return
     */
    public List<DataState> timeAddMuilt(String start, String end, int minute) {
        LogUtil.d("Test", "timeAddMuilt start:");
        if (StringUtil.isEmpty(start)||StringUtil.isEmpty(end)){
            start="08:30";
            end="21:30";
        }
        List<DataState> result = new ArrayList<>();
        DataState dataState = new DataState();
        dataState.setValue(start);
        dataState.setClicked(true);
        result.add(dataState);
        int i = 0;
        while (true) {
            start = DateFormatUtil.add(DateFormatUtil.getDate4StrDate(start, "HH:mm"),
                    "HH:mm", 1 * 1 * minute * 60 * 1000);
            if (start.compareTo(end) >= 0) {
                DataState dataState1 = new DataState();
                dataState1.setValue(end);
                dataState1.setClicked(true);
                result.add(dataState1);
                break;
            } else {
                if (start.equals("00:00")) {
                    break;
                }
                DataState dataState2 = new DataState();
                dataState2.setValue(start);
                dataState2.setClicked(true);
                result.add(dataState2);
            }
            i++;
        }
        LogUtil.d("Test", "timeAddMuilt end:");
        return result;
    }


    private void setDate() {
        //当前天在 MAX_PAGER/2 位置
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        for (int i = 0; i < MAX_PAGER; i++) {
            c.setTime(date);
            c.add(Calendar.MONTH, i - (MAX_PAGER / 2));
            c.set(Calendar.DAY_OF_MONTH, 1);
            this.date[i] = c.getTime();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_date_start) {
            startDate = true;
            ll_date_end.setEnabled(true);
            swichDateButton(startDate);
        } else if (v.getId() == R.id.ll_date_end) {
            startDate = false;
            swichDateButton(startDate);
            v_date_end.setText(v_date_start.getText().toString());
        } else if (v.getId() == R.id.ll_time_start) {
            startTime = true;
            ll_time_end.setEnabled(true);
            swichTimeButton(startTime);
        } else if (v.getId() == R.id.ll_time_end) {
            startTime = false;
            swichTimeButton(startTime);
            v_date_end.setText(v_date_start.getText().toString());
        } else if (v.getId() == R.id.click_btn) {
            if (type == 3) {
                if (!StringUtil.isEmpty(tv_time_point.getText().toString())) {
                    String today = DateFormatUtil.long2Str(DateFormatUtil.YMD_HM);
                    if (today.compareTo(tv_time_point.getText().toString()) >= 0){
                        ToastMessage("不能预约过去时间");
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("startDate", tv_time_point.getText().toString());
                    setResult(0x11, intent);
                    finish();
                }else{
                    ToastMessage("请选择时间！");
                }
            }
            if (swich) {
                //时间段
                if (!StringUtil.isEmpty(v_date_start.getText().toString()) && !
                        StringUtil.isEmpty(v_date_end.getText().toString())) {
                    if (!checkDateStr(v_date_end.getText().toString() + " " + v_time_end.getText().toString())) {
                        if (type == 1 || type == 4) {
                            ToastMessage("不能预约过去时间!");
                        } else {
                            ToastMessage("结束时间 请选择具体时间点!");
                        }
                        return;
                    }
                    if (!checkDateStr(v_date_start.getText().toString() + " " + v_time_start.getText().toString())) {
                        if (type == 1 || type == 4) {
                            ToastMessage("不能预约过去时间!");
                        } else {
                            ToastMessage("开始时间 请选择具体时间点!");
                        }
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("startDate", v_date_start.getText().toString() + " " + v_time_start.getText().toString());
                    intent.putExtra("endDate", v_date_end.getText().toString() + " " + v_time_end.getText().toString());
                    intent.putExtra("object", object);
                    intent.putExtra("field", field);
                    intent.putExtra("id", id);
                    setResult(0x11, intent);
                    finish();

                } else {
                    ToastMessage("请选择开始时间和结束时间！");
                }
            } else {
                //时间
                if (!StringUtil.isEmpty(v_date_start.getText().toString()) && !
                        StringUtil.isEmpty(v_date_end.getText().toString())) {
                    if (type == 2||type==5) {
                        setResultIntent();
                        return;
                    }

                    if (!checkDateStr(v_date_end.getText().toString())) {
                        if (type == 1 || type == 4) {
                            ToastMessage("不能预约过去时间!");
                        } else {
                            ToastMessage("结束时间 请选择具体时间点!");
                        }
                        return;
                    }
                    if (!checkDateStr(v_date_start.getText().toString())) {
                        if (type == 1 || type == 4) {
                            ToastMessage("不能预约过去时间!");
                        } else {
                            ToastMessage("开始时间 请选择具体时间点!");
                        }
                        return;
                    }

                    if (type == 1 || type == 4) {
                        if (v_date_start.getText().toString().compareTo(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS)) < 0) {
                            ToastMessage("不能预约过去的时间！");
                            return;
                        }
                        //类型等于小秘书的时候
                        //判断不能跨天预约
                        if (!v_date_end.getText().toString().substring(0, 10).equals(v_date_start.getText().toString().substring(0, 10))) {
                            ToastMessage("不能跨天预约！");
                            return;
                        }
                        //判断时间段不能有被预约的天数
                        String start = v_date_start.getText().toString().substring(11, 16);
                        String end = v_date_end.getText().toString().substring(11, 16);
                        List<DataState> dataStates = timeAddMuilt(start, end, 15);
                        for (int i = 0; i < dataStates.size(); i++) {
                            DataState dataState = dataStates.get(i);
                            for (int j = 0; j < totals.size(); j++) {
                                DataState nData = totals.get(j);
                                if (dataState.getValue().equals(nData.getValue())) {
                                    ToastMessage("该时间段不能预约！");
                                    return;
                                }
                            }
                        }

                        //结束时间不能小于或等于开始时间
                        if (v_date_end.getText().toString().compareTo(v_date_start.getText().toString()) <= 0) {
                            ToastMessage("结束时间不能小于或等于开始时间");
                            return;
                        }
                        //getUsersBusyTime();
                        //return;
                    }
                    setResultIntent();

                } else {
                    ToastMessage("请选择开始时间和结束时间！");
                }
            }
        }
    }

    private void setResultIntent() {
        Intent intent = new Intent();
        intent.putExtra("startDate", v_date_start.getText().toString());
        intent.putExtra("endDate", v_date_end.getText().toString());
        intent.putExtra("object", object);
        intent.putExtra("field", field);
        intent.putExtra("id", id);
        setResult(0x11, intent);
        finish();
    }


    /**
     * @desc:发送网络请求
     * @author：Arison on 2017/2/15
     */
    private void sendRequest() {
        if (ApiUtils.getApiModel() instanceof ApiUAS) {
            String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") +
                    "mobile/getWorkDate.action";
            Map<String, Object> param = new HashMap<>();
            param.put("date", DateFormatUtil.getStrDate4Date(new Date(), "yyyyMMdd"));
            param.put("master", CommonUtil.getSharedPreferences(mContext, "erp_master"));
            param.put("emcode", CommonUtil.getSharedPreferences(mContext, "erp_username"));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
            ViewUtil.httpSendRequest(this, url, param, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
        } else if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_schedule_url;
            Map<String, Object> param = new HashMap<>();
            param.put("date", DateFormatUtil.getStrDate4Date(new Date(), "yyyyMMdd"));
            param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(this, url, param, mHandler, headers, 0x5, null, null, "get");
        }
    }

    private boolean checkDateStr(String date_string) {
        // 利用java中的SimpleDateFormat类，指定日期格式，注意yyyy,MM大小写
        // 这里的日期格式要求javaAPI中有详细的描述，不清楚的话可以下载相关API查看
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // SimpleDateFormat format=new SimpleDateFormat("yyyyMM");
        // 设置日期转化成功标识
        boolean dateflag = true;
        // 这里要捕获一下异常信息
        try {
            Date date = format.parse(date_string);
        } catch (ParseException e) {
            dateflag = false;
        } finally {
            // 成功：true ;失败:false
            System.out.println("日期是否满足要求" + dateflag);
        }
        return dateflag;
    }

    private void swichDateButton(boolean swich) {
        ll_date_start.setSelected(swich);
        ll_date_end.setSelected(!swich);
    }

    private void swichTimeButton(boolean swich) {
        ll_time_start.setSelected(swich);
        ll_time_end.setSelected(!swich);
    }

    /**
     * @desc:表格适配器
     * @author：Arison on 2017/1/18
     */
    private class GridDataAdapter extends BaseAdapter {

        private Context ct;
        private List<DataState> mData = new ArrayList<>();
        private LayoutInflater inflater;
        private int selected = -1;

        GridDataAdapter(Context ct, List<DataState> data) {
            this.ct = ct;
            this.mData = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getSelected() {
            return selected;
        }

        public void setSelected(int selected) {
            this.selected = selected;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewModle modle = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_simple_text, parent, false);
                modle = new ViewModle();
                modle.tv_text = (TextView) convertView.findViewById(R.id.tv_text);
                convertView.setTag(modle);
            } else {
                modle = (ViewModle) convertView.getTag();
            }
            modle.tv_text.setText(mData.get(position).getValue());
            if (mData.get(position).isClicked()) {
                modle.clicked = true;
                modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.black));
                modle.tv_text.setBackgroundResource(R.drawable.bg_select_blue);
                if (selected == position) {
                    modle.tv_text.setSelected(true);
                    modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.white));
                } else {
                    modle.tv_text.setSelected(false);
                }
            } else {
                modle.clicked = false;
                modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.light_gray));
                modle.tv_text.setBackgroundResource(R.drawable.bg_select_red);
                modle.tv_text.setSelected(false);
            }
            return convertView;
        }

        class ViewModle {
            TextView tv_text;
            boolean clicked;
        }
    }


    class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarDateFragmet.getInstance(date[position], selectDay);
        }

        @Override
        public int getCount() {
            return MAX_PAGER;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, final int position, Object object) {
            try {
                CalendarDateFragmet fragmet = (CalendarDateFragmet) object;
                fragmet.setDownDay(selectDay);
                ;
            } catch (Exception e) {

            }
        }


    }

    private Map<String, OnTaskChangeListener> listenerMap;

    public void setListener(String key, OnTaskChangeListener listener) {
        listenerMap.put(key, listener);
    }

    public interface OnTaskChangeListener {
        void onChange(Set<Integer> in);
    }

    /*当点击了指定日期（点击日期、滑动时候触发）*/

    /**
     * @desc:点击日历控件触发
     * @author：Arison on 2017/10/12
     */
    public void setClickDay(Date date) {
        currentSelectDate = DateFormatUtil.getStrDate4Date(date, "yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        selectDay = c.get(Calendar.DAY_OF_MONTH);
        String today = DateFormatUtil.long2Str(DateFormatUtil.YMD);
        if (type == 3) {
            if (today.compareTo(currentSelectDate) > 0){
                ToastMessage("不能预约过去时间");
            }else{
                tv_time_point.setText(currentSelectDate+" "+tv_time_point.getText().toString().substring(11,16));
            }

        }
        if (today.compareTo(currentSelectDate) > 0 && (type == 1 || type == 4)) {
            ToastMessage("不能预约过去时间");
            //按时间
            currentSelectDate = "";
            // v_date_start.setText(DateFormatUtil.getStrDate4Date(date,"yyyy-MM-dd"));
            //按时间请假：
            if (startDate) {//按钮状态
                //开始时间
                v_date_start.setText(currentSelectDate);
            } else {
                //结束时间
                v_date_end.setText(currentSelectDate);
                if (type == 1 || type == 4) {
                    startDate = true;
                    ll_date_end.setEnabled(true);
                    swichDateButton(startDate);
                    v_date_start.setText(currentSelectDate);
                }
            }
            return;
        } else {
            if (swich) {
                //按时间段
                if (startDate) {
                    v_date_start.setText(currentSelectDate);
                } else {
                    v_date_end.setText(currentSelectDate);
                }

            } else {
                //按时间
                // v_date_start.setText(DateFormatUtil.getStrDate4Date(date,"yyyy-MM-dd"));
                //按时间请假：
                if (startDate) {//按钮状态
                    //开始时间
                    v_date_start.setText(currentSelectDate);
                } else {
                    //结束时间
                    v_date_end.setText(currentSelectDate);
                    if (type == 1 || type == 4) {
                        startDate = true;
                        ll_date_end.setEnabled(true);
                        swichDateButton(startDate);
                        v_date_start.setText(currentSelectDate);
                    }
                }
            }
            if (type==1) {
                getBookingTime(imId);
            }else if(type==4||type==3){
                getBServiceTimes();
            }
        }


    }


    /**
     * @desc:获取总的时间段
     * @author：Arison on 2017/6/28
     */
    private void getBookingTotalTime(String imId) {
        if (type == 1) {
            String url = Constants.IM_BASE_URL() + "user/appUsertime";
            Map<String, Object> params = new HashMap<>();
            params.put("token", MyApplication.getInstance().mAccessToken);
            params.put("userid", imId);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x07, null, null, "post");
        }
    }


    /**
     * @desc:获取预约时间
     * @author：Arison on 2017/6/28
     */
    public void getBookingTime(String imId) {
        if (type == 1) {
            String url = Constants.IM_BASE_URL() + "user/appBusytime";
            String ym;
            if (startDate) {
                ym = DateFormatUtil.formatChange(v_date_start.getText().toString().replace("-", ""), "yyyyMMdd");
            } else {
                ym = DateFormatUtil.formatChange(v_date_end.getText().toString().replace("-", ""), "yyyyMMdd");
            }
            Map<String, Object> params = new HashMap<>();
            params.put("token", MyApplication.getInstance().mAccessToken);
            params.put("userid", imId);
            params.put("currentid", MyApplication.getInstance().mLoginUser.getUserId());
            params.put("yearmonth", ym);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x06, null, null, "post");
        }
    }


    private void parseBookTimeResult(String result) {
        String busytime = JSON.parseObject(result).getString("busytime");
        JSONArray jsonArray = JSON.parseArray(busytime);
        if (totals != null) totals.clear();
        if (jsonArray != null) {
            if (jsonArray.size() != 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String sDate = object.getString("ab_starttime");
                    String eDate = object.getString("ab_endtime");
                    sDate = sDate.substring(11, 16);
                    eDate = eDate.substring(11, 16);
                    LogUtil.d("Test", "时间段：" + sDate + "-" + eDate);
                    List<DataState> dataStates = timeAddMuilt(sDate, eDate, 15);
                    totals.addAll(dataStates);
                }
                LogUtil.d("Test", "被预约的时间段：" + JSON.toJSONString(totals));
                for (int j = 0; j < mTimeData.size(); j++) {
                    DataState dataState = mTimeData.get(j);
                    mTimeData.get(j).setClicked(true);
                    for (int n = 0; n < totals.size(); n++) {
                        DataState nDataState = totals.get(n);
                        if (dataState.getValue().equals(nDataState.getValue())) {
                            dataState.setClicked(false);
                        }
                    }
                }
                if (gAdapter != null) {
                    gAdapter.notifyDataSetChanged();
                }
            } else {
                //默认时间段：00:00-23:59
                for (int y = 0; y < mTimeData.size(); y++) {
                    mTimeData.get(y).setClicked(true);
                }
                if (gAdapter != null) {
                    gAdapter.notifyDataSetChanged();
                }
            }
        } else {
            for (int y = 0; y < mTimeData.size(); y++) {
                mTimeData.get(y).setClicked(true);
            }
            if (gAdapter != null) {
                gAdapter.notifyDataSetChanged();
            }
            //默认时间段：00:00-23:59
        }

    }


    private void parseSBookTimeResult(String result) {
        String busytime = JSON.parseObject(result).getString("reslut");
        JSONArray jsonArray = JSON.parseArray(busytime);
        if (totals != null) totals.clear();
        if (jsonArray != null) {
            if (jsonArray.size() != 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String sDate = object.getString("sb_starttime");
                    String eDate = object.getString("sb_endtime");
                    if (StringUtil.isEmpty(sDate)){
                        sDate=eDate;
                    }
                    sDate = sDate.substring(11, 16);
                    eDate = eDate.substring(11, 16);
                    LogUtil.d("Test", "时间段：" + sDate + "-" + eDate);
                    List<DataState> dataStates = timeAddMuilt(sDate, eDate, 15);
                    totals.addAll(dataStates);
                }
                LogUtil.d("Test", "被预约的时间段：" + JSON.toJSONString(totals));
                for (int j = 0; j < mTimeData.size(); j++) {
                    DataState dataState = mTimeData.get(j);
                    mTimeData.get(j).setClicked(true);
                    for (int n = 0; n < totals.size(); n++) {
                        DataState nDataState = totals.get(n);
                        if (dataState.getValue().equals(nDataState.getValue())) {
                            dataState.setClicked(false);
                        }
                    }
                }
                if (gAdapter != null) {
                    gAdapter.notifyDataSetChanged();
                }
            } else {
                initTimeData();
            }
        } else {
            initTimeData();
        }

    }


    //服务预约时间段
    private void getBServiceTimes() {
        String ym;
        if (startDate) {
            ym = DateFormatUtil.formatChange(v_date_start.getText().toString().replace("-", ""), "yyyyMMdd");
        } else {
            ym = DateFormatUtil.formatChange(v_date_end.getText().toString().replace("-", ""), "yyyyMMdd");
        }
        LogUtil.d("HttpLogs", "businessType:" + businessType);
        //如果有人，就是人的id 。如果没有人，就是服务项目名称
        LogUtil.d("HttpLogs", "commonid:" + serviceId);
        LogUtil.d("HttpLogs", "ym:" + ym);
        LogUtil.d("HttpLogs", "companyId:" + companyId);
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appServiceBusytime")
                .add("type", businessType)
                .add("commonid", serviceId)
                .add("yearmonth", ym)
                .add("userid",MyApplication.getInstance().mLoginUser.getUserId())
                .add("companyid", companyId)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<Object>(new ResultListener<Object>() {

            @Override
            public void onResponse(Object o) {
                LogUtil.d("HttpLogs", "result:" + o.toString());
                if (JSONUtil.validate(o.toString())) {
                    String result = o.toString();
                    final String resu = result;
                    try {
                        OAHttpHelper.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    parseSBookTimeResult(resu);
                                } catch (Exception e) {

                                }
                            }
                        });
                    } catch (Exception e) {
//                        e.printStackTrace();
                        //initTimeData();
                    }
                } else {
                  //  initTimeData();
                }

            }
        }));
    }

}
