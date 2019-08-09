package com.xzjmyk.pm.activity.ui.erp.activity.secretary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.sp.UserSp;
import com.core.widget.CustomerScrollView;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.core.widget.view.selectcalendar.OACalendarView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.modular.booking.activity.business.BBookingAddActivity;
import com.modular.booking.activity.business.BBookingDetailActivity;
import com.modular.booking.activity.services.BServiceAddActivity;
import com.modular.booking.activity.services.BServicesActivity;
import com.modular.booking.activity.shares.BBSharesListActivity;
import com.modular.booking.activity.shares.MuiltSelectShareActivity;
import com.modular.booking.adapter.BookAdapter;
import com.modular.booking.model.BookingModel;
import com.modular.booking.model.SBListModel;
import com.uas.appworks.activity.TimeHelperActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.ui.erp.adapter.oa.OACalenderViewPagerAdapter;
import com.xzjmyk.pm.activity.ui.erp.model.book.SureBookModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookingListActivity extends SupportToolBarActivity implements View.OnClickListener {

    @ViewInject(R.id.tv_book_me)
    TextView tv_book_me;
    @ViewInject(R.id.tv_book_shared)
    TextView tv_book_shared;
    @ViewInject(R.id.yeas_tv)
    TextView yeas_tv;
    @ViewInject(R.id.day_tv)
    TextView day_tv;
    @ViewInject(R.id.week_tv)
    TextView week_tv;
    @ViewInject(R.id.viewPager)
    ViewPager viewPager;
    @ViewInject(R.id.lv_book)
    MyListView lv_book;
    @ViewInject(R.id.sv_top)
    CustomerScrollView sv_top;

    private TextView tvPersonalNum;
    private TextView mBookPersonal;
    private TextView tvBusinessNum;
    private TextView mBookBusiness;
    private TextView mBookService;
    private TextView mBookShares;

    private EmptyLayout emptyLayout;
    private Date curDate;
    private int posItem;

    private ArrayList<BookingModel> mDatas = new ArrayList<>();
    private ArrayList<BookingModel> mShareDatas = new ArrayList<>();
    private ArrayList<BookingModel> mShareMuiltDatas = new ArrayList<>();

    private BookAdapter mAdapter;
    private OACalendarView calendarView;
    private OACalenderViewPagerAdapter pagerAdapter;//当前Viewpager适配器
    private List<SureBookModel> mSureBookModel;
    private String mWhichPage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);
        setTitle(getString(R.string.booking_menu));
        ViewUtils.inject(this);
        initView();
        initEvent();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initData(curDate);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_about) {
            Intent intent = new Intent(mContext, MuiltSelectShareActivity.class);
            intent.putExtra("model", mShareMuiltDatas);
            startActivity(intent);
        }

        if (item.getItemId() == android.R.id.home && mWhichPage == null) {
            Intent it = new Intent(BookingListActivity.this, MainActivity.class);
            startActivity(it);
            overridePendingTransition(R.anim.anim_activity_out, R.anim.anim_activity_in);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initEvent() {
        tv_book_me.setOnClickListener(this);
        tv_book_shared.setOnClickListener(this);
        mBookBusiness.setOnClickListener(this);
        mBookPersonal.setOnClickListener(this);
        mBookService.setOnClickListener(this);
        mBookShares.setOnClickListener(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                LogUtil.d("----------------------------------------------------" + position);
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
                loadListData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        lv_book.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookAdapter.ViewHolder viewHolder = (BookAdapter.ViewHolder) view.getTag();
                Bundle bundle = new Bundle();
                bundle.putParcelable("model", viewHolder.model);
                LogUtil.d("OnItemClickListener", "model:" + JSON.toJSONString(viewHolder.model));
                bundle.putBoolean("isShared", tv_book_shared.isSelected());
                if ("个人".equals(viewHolder.model.getKind())) {
                    startActivity(new Intent(mContext, BookingDetailActivity.class)
                            .putExtras(bundle));
                } else if ("服务".equals(viewHolder.model.getKind())) {
                    SBListModel model = new SBListModel();
                    model.setId(Integer.valueOf(viewHolder.model.getAb_id()));
                    model.setAddress(viewHolder.model.getAb_address());
                    model.setType(viewHolder.model.getAb_content());//行业关键代码
                    model.setIndustrycode(viewHolder.model.getAb_content());
                    model.setEndtime(viewHolder.model.getAb_endtime());
                    model.setName(viewHolder.model.getAb_bman());

                    // model.setType(viewHolder.model.gett);
                    startActivity(new Intent(mContext, BServiceAddActivity.class)
                            .putExtra("model", model)
                            .putExtra("isEdited", false)
                            .putExtra("dataService", viewHolder.model.getData_service()));
                } else {
                    startActivity(new Intent(mContext, BBookingDetailActivity.class)
                            .putExtras(bundle));
                }

            }
        });


    }

    private void initData(Date date) {
        setDateTag(date);
        loadListData();
    }

    private void initView() {
        tvPersonalNum = (TextView) findViewById(R.id.tv_personal_num);
        tvBusinessNum = (TextView) findViewById(R.id.tv_business_num);
        mBookPersonal = (TextView) findViewById(R.id.mBookPersonal);
        mBookBusiness = (TextView) findViewById(R.id.mBookBusiness);
        mBookService = (TextView) findViewById(R.id.mBookService);
        mBookShares = (TextView) findViewById(R.id.mBookShares);

        Intent intent = getIntent();
        if (intent != null) {
            String cDate = intent.getStringExtra("curDate");
            mWhichPage = intent.getStringExtra("whichPage");
            if (!StringUtil.isEmpty(cDate)) {
                getIntent().getExtras().putString("curDate", "");
                curDate = DateFormatUtil.getDate4StrDate(cDate, "yyyy-MM-dd");
            } else {
                curDate = new Date(System.currentTimeMillis());
            }
        } else {
            curDate = new Date(System.currentTimeMillis());
        }

        tv_book_me.setSelected(true);
        posItem = OACalenderViewPagerAdapter.MAX_NUM / 2;
        //设置为空显示列表
        emptyLayout = new EmptyLayout(ct, lv_book);
        emptyLayout.setShowLoadingButton(false);
        emptyLayout.setShowEmptyButton(false);
        emptyLayout.setShowErrorButton(false);
        emptyLayout.setEmptyViewRes(R.layout.book_empty_list);

        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viewPager.getLayoutParams(); // 取控件mGrid当前的布局参数   搜索
        linearParams.height = (getResources().getDisplayMetrics().heightPixels * 1 / 3);// 当控件的高强制设成50象素
        viewPager.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件myGrid
        pagerAdapter = new OACalenderViewPagerAdapter(ct, curDate);
        viewPager.setAdapter(pagerAdapter);

        viewPager.setCurrentItem(OACalenderViewPagerAdapter.MAX_NUM / 2);
        mAdapter = new BookAdapter(mContext, mDatas);
        mAdapter.setCurrentDate(curDate);
        lv_book.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mBookPersonal:
                startActivity(new Intent(mContext, BookingAddActivity.class)
                        .putExtra("whichPage", mWhichPage));
                break;
            case R.id.mBookBusiness:
                startActivity(new Intent(mContext, BBookingAddActivity.class)
                        .putExtra("whichPage", mWhichPage));
                break;
            case R.id.mBookService:
                startActivity(new Intent(mContext, BServicesActivity.class));
                break;
            case R.id.mBookShares:
                Intent intent = new Intent(mContext, BBSharesListActivity.class);
                intent.putExtra("model", mShareDatas);
                startActivity(intent);
                break;
            case R.id.tv_book_me:
                tv_book_shared.setSelected(false);
                tv_book_me.setSelected(true);
                if (mAdapter != null) {
                    mAdapter.setType(1);
                }
                loadListData();
                break;
            case R.id.tv_book_shared:
                tv_book_shared.setSelected(true);
                tv_book_me.setSelected(false);
                if (mAdapter != null) {
                    mAdapter.setType(2);
                }
                loadListData();
                break;
        }
    }

    private void setDateTag(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int yeas = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        yeas_tv.setText(yeas + "年" + month + "月");
        day_tv.setText(day + "日");
        week_tv.setText(CalendarUtil.getWeek(DateFormatUtil.getFormat(DateFormatUtil.YMD).format(date)));
    }

    private void setCalenderOnClicklistener() {
        if (calendarView == null && pagerAdapter != null && pagerAdapter.getmViews() != null) {
            calendarView = pagerAdapter.getmViews().get(posItem);
        }
        if (calendarView != null)
            calendarView.setDateListener(new OACalendarView.OnSelectDateListener() {
                @Override
                public void result(Date date) {
                    curDate = date;
                    setDateTag(date);
                    loadListData();
                    // setAdapterBeans();
                }
            });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //setCalenderOnClicklistener();
    }


    private void loadListData() {
//        showLoading();
        String url = Constants.IM_BASE_URL() + "user/appBookingList";
        Map<String, Object> params = new HashMap<>();
        params.put("token", UserSp.getInstance(MyApplication.getInstance()).getAccessToken(""));
        params.put("userid", MyApplication.getInstance().mLoginUser.getUserId());
        params.put("telephone", UserSp.getInstance(MyApplication.getInstance()).getTelephone(""));
        params.put("yearmonth", DateFormatUtil.getStrDate4Date(curDate, "yyyyMM"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x01, null, null, "post");
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            dimssLoading();
            setCalenderOnClicklistener();
            switch (msg.what) {
                case 0x01:
                    try {
                        String result = msg.getData().getString("result");
                        if (!ListUtils.isEmpty(mDatas)) {
                            mDatas.clear();
                        }
                        if (JSON.parseObject(result).getString("resultMsg") != null) {
                            ToastMessage(JSON.parseObject(result).getString("resultMsg"));
                            emptyLayout.showEmpty();
                            return;
                        }
                        mShareMuiltDatas.clear();
//                        if (tv_book_me.isSelected()) {
                        JSONArray jsonArray = JSON.parseArray(JSON.parseObject(result).getString("bookinglist"));
                        JSONArray buinessArray = JSON.parseArray(JSON.parseObject(result).getString("businessList"));
                        JSONArray serviceArray = JSON.parseArray(JSON.parseObject(result).getString("servicelist"));
                        if (jsonArray != null) {
                            Set<Integer> tags = new HashSet<>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                BookingModel model = new BookingModel();
                                model.setAb_address(object.getString("ab_address"));
                                model.setAb_bman(object.getString("ab_bman"));
                                model.setAb_bmanid(object.getString("ab_bmanid"));
                                model.setAd_reason(object.getString("ad_reason"));
                                model.setAb_reason(object.getString("ab_reason"));
                                model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                                model.setAb_content(object.getString("ab_content"));
                                model.setAb_endtime(object.getString("ab_endtime"));
                                model.setAb_id(object.getString("ab_id"));
                                model.setAb_latitude(object.getString("ab_latitude"));
                                model.setAb_longitude(object.getString("ab_longitude"));
                                model.setAb_recorddate(object.getString("ab_recorddate"));
                                model.setAb_recordid(object.getString("ab_recordid"));
                                model.setAb_recordman(object.getString("ab_recordman"));
                                model.setAb_sharestatus(object.getString("ab_sharestatus"));
                                model.setAb_starttime(object.getString("ab_starttime"));
                                model.setAb_type(object.getString("ab_type"));
                                model.setKind(object.getString("kind"));
                                tags.add(Integer.valueOf(model.getAb_starttime().substring(8, 10)));
                                if (model.getAb_starttime().contains(DateFormatUtil.getStrDate4Date(curDate, "yyyy-MM-dd"))) {
                                    mDatas.add(model);
                                    if ("已确认".equals(model.getAb_confirmstatus())) {
                                        mShareMuiltDatas.add(model);
                                    }
                                }
                            }

                            if (buinessArray != null) {
                                for (int i = 0; i < buinessArray.size(); i++) {
                                    JSONObject object = buinessArray.getJSONObject(i);
                                    BookingModel bModel = new BookingModel();
                                    bModel.setAb_address(object.getString("ab_address"));
                                    bModel.setAb_bman(object.getString("ab_bman"));
                                    bModel.setAb_bmanid(object.getString("ab_bmanid"));
                                    bModel.setAd_reason(object.getString("ad_reason"));
                                    bModel.setAb_reason(object.getString("ab_reason"));
                                    bModel.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                                    bModel.setAb_content(object.getString("ab_content"));
                                    bModel.setAb_endtime(object.getString("ab_endtime"));
                                    bModel.setAb_id(object.getString("ab_id"));
                                    bModel.setAb_latitude(object.getString("ab_latitude"));
                                    bModel.setAb_longitude(object.getString("ab_longitude"));
                                    bModel.setAb_recorddate(object.getString("ab_recorddate"));
                                    bModel.setAb_recordid(object.getString("ab_recordid"));
                                    bModel.setAb_recordman(object.getString("ab_recordman"));
                                    bModel.setAb_sharestatus(object.getString("ab_sharestatus"));
                                    bModel.setAb_starttime(object.getString("ab_starttime"));
                                    bModel.setAb_type(object.getString("ab_type"));
                                    bModel.setKind(object.getString("kind"));
                                    tags.add(Integer.valueOf(bModel.getAb_starttime().substring(8, 10)));
                                    if (bModel.getAb_starttime().contains(DateFormatUtil.getStrDate4Date(curDate, "yyyy-MM-dd"))) {
                                        mDatas.add(bModel);
                                        if ("已确认".equals(bModel.getAb_confirmstatus())) {
                                            mShareMuiltDatas.add(bModel);
                                        }
                                    }
                                }
                            }

                            //服务预约
                            if (serviceArray != null) {
                                for (int i = 0; i < serviceArray.size(); i++) {
                                    JSONObject object = serviceArray.getJSONObject(i);
                                    BookingModel model = new BookingModel();
                                    model.setAb_address(object.getString("sb_address"));
                                    model.setAb_bman(object.getString("sb_companyname"));//被预约人
                                    model.setAb_bmanid(object.getString("sb_companyid"));//被预约人的IMID
                                    // model.setAd_reason(object.getString("ad_reason"));
                                    model.setAb_confirmstatus(object.getString("sb_status"));
                                    model.setAb_content(object.getString("sb_industry"));
                                    model.setAb_endtime(object.getString("sb_endtime"));
                                    model.setAb_id(object.getString("sb_id"));
//                                model.setAb_latitude(object.getString("ab_latitude"));
//                                model.setAb_longitude(object.getString("ab_longitude"));
                                    model.setAb_recorddate(object.getString("ab_recorddate"));
                                    model.setAb_recordid(object.getString("sb_recordid"));
                                    model.setAb_recordman(object.getString("sb_recordor"));
                                    model.setAb_sharestatus("未共享");
                                    model.setAb_starttime(object.getString("sb_starttime"));
                                    model.setAb_type(object.getString("sb_stname"));//科目
                                    model.setKind(object.getString("kind"));//关键字段
                                    model.setData_service(object.toJSONString());
                                    tags.add(Integer.valueOf(model.getAb_endtime().substring(8, 10)));
                                    if (model.getAb_endtime().contains(DateFormatUtil.getStrDate4Date(curDate, "yyyy-MM-dd"))) {
                                        mDatas.add(model);
                                    }
                                }
                            }

                            if (DateFormatUtil.getStrDate4Date(curDate, "yyyy-MM-dd").equals(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd"))) {
                                JSONArray tenArray = JSON.parseArray(JSON.parseObject(result).getString("tenlist"));

                                for (int i = 0; i < tenArray.size(); i++) {
                                    JSONObject object = tenArray.getJSONObject(i);
                                    BookingModel model = new BookingModel();
                                    model.setAb_address(object.getString("ab_address"));
                                    model.setAb_bman(object.getString("ab_bman"));
                                    model.setAb_bmanid(object.getString("ab_bmanid"));
                                    model.setAd_reason(object.getString("ad_reason"));
                                    model.setAb_reason(object.getString("ab_reason"));
                                    model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                                    model.setAb_content(object.getString("ab_content"));
                                    model.setAb_endtime(object.getString("ab_endtime"));
                                    model.setAb_id(object.getString("ab_id"));
                                    model.setAb_latitude(object.getString("ab_latitude"));
                                    model.setAb_longitude(object.getString("ab_longitude"));
                                    model.setAb_recorddate(object.getString("ab_recorddate"));
                                    model.setAb_recordid(object.getString("ab_recordid"));
                                    model.setAb_recordman(object.getString("ab_recordman"));
                                    model.setAb_sharestatus(object.getString("ab_sharestatus"));
                                    model.setAb_starttime(object.getString("ab_starttime"));
                                    model.setAb_type(object.getString("ab_type"));
                                    model.setKind(object.getString("kind"));
                                    mDatas.add(model);
                                    if ("已确认".equals(model.getAb_confirmstatus())) {
                                        mShareMuiltDatas.add(model);
                                    }
                                }
                                JSONArray btenArray = JSON.parseArray(JSON.parseObject(result).getString("businesstenlist"));
                                for (int i = 0; i < btenArray.size(); i++) {
                                    JSONObject object = btenArray.getJSONObject(i);
                                    BookingModel model = new BookingModel();
                                    model.setAb_address(object.getString("ab_address"));
                                    model.setAb_bman(object.getString("ab_bman"));
                                    model.setAb_bmanid(object.getString("ab_bmanid"));
                                    model.setAd_reason(object.getString("ad_reason"));
                                    model.setAb_reason(object.getString("ab_reason"));
                                    model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                                    model.setAb_content(object.getString("ab_content"));
                                    model.setAb_endtime(object.getString("ab_endtime"));
                                    model.setAb_id(object.getString("ab_id"));
                                    model.setAb_latitude(object.getString("ab_latitude"));
                                    model.setAb_longitude(object.getString("ab_longitude"));
                                    model.setAb_recorddate(object.getString("ab_recorddate"));
                                    model.setAb_recordid(object.getString("ab_recordid"));
                                    model.setAb_recordman(object.getString("ab_recordman"));
                                    model.setAb_sharestatus(object.getString("ab_sharestatus"));
                                    model.setAb_starttime(object.getString("ab_starttime"));
                                    model.setAb_type(object.getString("ab_type"));
                                    model.setKind(object.getString("kind"));
                                    mDatas.add(model);
                                    if ("已确认".equals(model.getAb_confirmstatus())) {
                                        mShareMuiltDatas.add(model);
                                    }
                                }
                            }


                            if (mAdapter != null) {
                                mAdapter.setCurrentDate(curDate);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                mAdapter = new BookAdapter(mContext, mDatas);
                                mAdapter.setCurrentDate(curDate);
                                lv_book.setAdapter(mAdapter);
                            }
                            calendarView.setDecoratDays(tags);
//                                Calendar c = Calendar.getInstance();
//                                c.setTime(curDate);
//                                int day = c.get(Calendar.DAY_OF_MONTH);
                            //                               calendarView.setDownIndex(day);
                            if (mDatas.size() == 0) {
                                emptyLayout.setEmptyMessage(getString(R.string.book_empty));
                                emptyLayout.showEmpty();
                            }
                        }
//                        } else {


                        mShareDatas.clear();
                        JSONArray shareArray = JSON.parseArray(JSON.parseObject(result).getString("sharelist"));
                        JSONArray businessshare = JSON.parseArray(JSON.parseObject(result).getString("businessshare"));
                        if (shareArray != null) {
                            for (int i = 0; i < shareArray.size(); i++) {
                                JSONObject object = shareArray.getJSONObject(i);
                                BookingModel model = new BookingModel();
                                model.setAb_address(object.getString("ab_address"));
                                model.setAb_bman(object.getString("ab_bman"));
                                model.setAb_bmanid(object.getString("ab_bmanid"));
                                model.setAd_reason(object.getString("ad_reason"));
                                model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                                model.setAb_content(object.getString("ab_content"));
                                model.setAb_endtime(object.getString("ab_endtime"));
                                model.setAb_id(object.getString("ab_id"));
                                model.setAb_latitude(object.getString("ab_latitude"));
                                model.setAb_longitude(object.getString("ab_longitude"));
                                model.setAb_recorddate(object.getString("ab_recorddate"));
                                model.setAb_recordid(object.getString("ab_recordid"));
                                model.setAb_recordman(object.getString("ab_recordman"));
                                model.setAb_sharestatus(object.getString("ab_sharestatus"));
                                model.setAb_starttime(object.getString("ab_starttime"));
                                model.setAb_type(object.getString("ab_type"));
                                model.setKind(object.getString("kind"));
                                mShareDatas.add(model);
                            }

                            if (businessshare != null) {
                                for (int i = 0; i < businessshare.size(); i++) {
                                    JSONObject object = businessshare.getJSONObject(i);
                                    BookingModel model = new BookingModel();
                                    model.setAb_address(object.getString("ab_address"));
                                    model.setAb_bman(object.getString("ab_bman"));
                                    model.setAb_bmanid(object.getString("ab_bmanid"));
                                    model.setAd_reason(object.getString("ad_reason"));
                                    model.setAb_reason(object.getString("ab_reason"));
                                    model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                                    model.setAb_content(object.getString("ab_content"));
                                    model.setAb_endtime(object.getString("ab_endtime"));
                                    model.setAb_id(object.getString("ab_id"));
                                    model.setAb_latitude(object.getString("ab_latitude"));
                                    model.setAb_longitude(object.getString("ab_longitude"));
                                    model.setAb_recorddate(object.getString("ab_recorddate"));
                                    model.setAb_recordid(object.getString("ab_recordid"));
                                    model.setAb_recordman(object.getString("ab_recordman"));
                                    model.setAb_sharestatus(object.getString("ab_sharestatus"));
                                    model.setAb_starttime(object.getString("ab_starttime"));
                                    model.setAb_type(object.getString("ab_type"));
                                    model.setKind(object.getString("kind"));
                                    mShareDatas.add(model);
                                }
                            }
                        }

                    } catch (Exception e) {

                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    String result = msg.getData().getString("result");
                    ToastMessage(result);
                    break;
                case Constants.APP_NOTNETWORK:
                    ToastMessage(msg.getData().getString("result"));
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWhichPage == null) {
            Intent it = new Intent(BookingListActivity.this, MainActivity.class);
            startActivity(it);
            overridePendingTransition(R.anim.anim_activity_out, R.anim.anim_activity_in);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && "timeHelper".equals(mWhichPage)) {
            Intent it = new Intent(BookingListActivity.this, TimeHelperActivity.class);
            startActivity(it);
            overridePendingTransition(R.anim.anim_activity_out, R.anim.anim_activity_in);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
