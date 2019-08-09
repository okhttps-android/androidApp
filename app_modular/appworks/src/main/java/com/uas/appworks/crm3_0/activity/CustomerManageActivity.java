package com.uas.appworks.crm3_0.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.TextUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.base.OABaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.DatePicker;
import com.core.widget.MyListView;
import com.core.widget.view.MyGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.modular.apputils.activity.BillDetailsActivity;
import com.modular.apputils.activity.BillListActivity;
import com.modular.apputils.adapter.EasyBaseAdapter;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.model.EasyBaseModel;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CustomerCareListAdapter;
import com.uas.appworks.presenter.CustomerManagePresenter;
import com.uas.appworks.presenter.imp.ICustomerManage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerManageActivity extends OABaseActivity implements ICustomerManage {
    private MyGridView addGridView;
    private TextView showCustomerTv;
    private MyGridView showCustomerGv;
    private ImageView showCustomerRefIv;
    private TextView showCustomerRefTimeTv;
    private TextView forgetCustomerTv;
    private MyListView forgetCustomerLv;
    private ImageView forgetCustomerRefIv;
    private TextView forgetCustomerRefTimeTv;
    private TextView visitTv;
    private MyListView visitLv;
    private ImageView visitRefIv;
    private TextView visitRefTimeTv;

    private TextView customerCareTv;
    private TextView customerCareRefTimeTv;
    private MyListView customerCareLv;
    private ImageView customerCareRefIv;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    private CustomerManagePresenter mCustomerManagePresenter = null;

    private EasyBaseAdapter showCustomerAdapter;//客户看板适配器
    private CustomerCareListAdapter mCustomerForgetAdapter;//遗忘客户适配器
    private CustomerCareListAdapter mCustomerCareAdapter;//客户关怀适配器
    private EasyBaseAdapter mVisitAdapter;//拜访记录

    private PopupWindow menuPopWindow;
    private RotateAnimation mRotateAnimation;//动画

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            showPopupWindow(getWindow().findViewById(R.id.add_item));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_manage);
        init();
    }


    protected void init() {
        customerCareTv = findViewById(R.id.customerCareTv);
        customerCareRefTimeTv = findViewById(R.id.customerCareRefTimeTv);
        customerCareLv = findViewById(R.id.customerCareLv);
        customerCareRefIv = findViewById(R.id.customerCareRefIv);
        mPullToRefreshScrollView = findViewById(R.id.mPullToRefreshScrollView);

        addGridView = (MyGridView) findViewById(R.id.addGridView);

        showCustomerTv = (TextView) findViewById(R.id.showCustomerTv);
        showCustomerGv = (MyGridView) findViewById(R.id.showCustomerGv);
        showCustomerRefIv = (ImageView) findViewById(R.id.showCustomerRefIv);
        showCustomerRefTimeTv = (TextView) findViewById(R.id.showCustomerRefTimeTv);

        forgetCustomerTv = (TextView) findViewById(R.id.forgetCustomerTv);
        forgetCustomerLv = (MyListView) findViewById(R.id.forgetCustomerLv);
        forgetCustomerRefIv = (ImageView) findViewById(R.id.forgetCustomerRefIv);
        forgetCustomerRefTimeTv = (TextView) findViewById(R.id.forgetCustomerRefTimeTv);

        visitTv = (TextView) findViewById(R.id.visitTv);
        visitLv = (MyListView) findViewById(R.id.visitLv);
        visitRefIv = (ImageView) findViewById(R.id.visitRefIv);
        visitRefTimeTv = (TextView) findViewById(R.id.visitRefTimeTv);

        addGridView.setOnItemClickListener(mOnItemClickListener);
        showCustomerGv.setOnItemClickListener(mOnItemClickListener);
//        forgetCustomerLv.setOnItemClickListener(mOnItemClickListener);
        visitLv.setOnItemClickListener(mOnItemClickListener);
//        customerCareLv.setOnItemClickListener(mOnItemClickListener);
        showCustomerTv.setOnClickListener(mOnClickListener);
        showCustomerRefIv.setOnClickListener(mOnClickListener);
        forgetCustomerTv.setOnClickListener(mOnClickListener);
        forgetCustomerRefIv.setOnClickListener(mOnClickListener);
        customerCareTv.setOnClickListener(mOnClickListener);
        customerCareRefIv.setOnClickListener(mOnClickListener);
        visitTv.setOnClickListener(mOnClickListener);
        visitRefIv.setOnClickListener(mOnClickListener);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                showCustomerTv.setText(DateFormatUtil.long2Str(DateFormatUtil.YM));
                mCustomerManagePresenter.loadAllData();
            }
        });

        addGridView.setAdapter(new EasyBaseAdapter(ct, getAddItems()) {
            @Override
            public View bindView(View view, int position, EasyBaseModel model) {
                ViewHolder mViewHolder = null;
                if (view.getTag() == null) {
                    mViewHolder = new ViewHolder();
                    mViewHolder.addIv = view.findViewById(R.id.addIv);
                    mViewHolder.addTv = view.findViewById(R.id.addTv);
                    view.setTag(mViewHolder);
                } else {
                    mViewHolder = (ViewHolder) view.getTag();
                }
                mViewHolder.addIv.setImageResource(model.getIconId());
                mViewHolder.addTv.setText(model.getTitle());
                return view;
            }

            class ViewHolder {
                private ImageView addIv;
                private TextView addTv;
            }

            @Override
            public int getLayoutRes() {
                return R.layout.item_customer_manage_add;
            }
        });
        showCustomerTv.setText(DateFormatUtil.long2Str(DateFormatUtil.YM));

        mCustomerManagePresenter = new CustomerManagePresenter(ct, this);
        mCustomerManagePresenter.loadAllData();

    }

    private Animation getImgAnimation() {
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            mRotateAnimation.setInterpolator(lin);
            mRotateAnimation.setDuration(1000);//设置动画持续时间
            mRotateAnimation.setRepeatCount(-1);//设置重复次数
            mRotateAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            mRotateAnimation.setStartOffset(10);//执行前的等待时间
        }
        return mRotateAnimation;
    }

    public void showPopupWindow(View parent) {
        if (menuPopWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = view.findViewById(R.id.mList);
            SimpleAdapter adapter = new SimpleAdapter(ct, getPopData(),
                    R.layout.item_pop_list, new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    toAddPager(position);
                    if (menuPopWindow != null) {
                        menuPopWindow.dismiss();
                    }
                }
            });
            menuPopWindow = new PopupWindow(view);
            menuPopWindow.setWidth(DisplayUtil.dip2px(ct, 140));
            menuPopWindow.setHeight(DisplayUtil.dip2px(ct, 180));
        }
        // 使其聚集
        menuPopWindow.setFocusable(true);
        // 设置允许在外点击消失
        menuPopWindow.setOutsideTouchable(true);
        menuPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        menuPopWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.top_bubble));
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        menuPopWindow.showAsDropDown(parent, -DisplayUtil.dip2px(ct, 50), 0);
    }

    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        /*map.put("item_name", "正式客户");
        list.add(map);*/

        map = new HashMap<>();
        map.put("item_name", "预录入客户");
        list.add(map);

        map = new HashMap<>();
        map.put("item_name", "客户联系人");
        list.add(map);

        String master = CommonUtil.getMaster();
//        if (!("DATACENTER".equals(master) || "N_SHYZ".equals(master) || "N_AJC".equals(master))) {
        map = new HashMap<>();
        map.put("item_name", "拜访报告");
        list.add(map);
//        }

//        map = new HashMap<>();
//        map.put("item_name", "正式客户");
//        list.add(map);
        return list;
    }


    private List<EasyBaseModel> getAddItems() {
        List<EasyBaseModel> models = new ArrayList<>();
//        models.add(new EasyBaseModel().setTitle("新建客户").setIconId(R.drawable.icon_customer_manage_add));
//        models.add(new EasyBaseModel().setTitle("正式客户").setIconId(R.drawable.icon_customer_manage_formal));
        models.add(new EasyBaseModel().setTitle("预录入客户").setIconId(R.drawable.icon_customer_manage_entry));
        models.add(new EasyBaseModel().setTitle("客户联系人").setIconId(R.drawable.icon_customer_manage_contact));
        models.add(new EasyBaseModel().setTitle("拜访报告").setIconId(R.drawable.icon_customer_manage_visit_report));
        return models;
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (R.id.forgetCustomerTv == id) {
                startActivity(new Intent(ct, CustomerCareListActivity.class)
                        .putExtra(Constants.Intents.TITLE, "遗忘客户")
                        .putExtra(Constants.Intents.TYPE, 1));
            } else if (R.id.customerCareTv == id) {
                startActivity(new Intent(ct, CustomerCareListActivity.class)
                        .putExtra(Constants.Intents.TITLE, "客户关怀")
                        .putExtra(Constants.Intents.TYPE, 2));
            }
            if (R.id.showCustomerTv == id) {
                showDataSelect();
            } else if (showCustomerRefIv == view || forgetCustomerRefIv == view || customerCareRefIv == view || visitRefIv == view) {
                view.setAnimation(getImgAnimation());
                view.startAnimation(getImgAnimation());
                if (showCustomerRefIv == view) {
                    mCustomerManagePresenter.loadShowCustomerItems(showCustomerTv.getText().toString());
                } else if (forgetCustomerRefIv == view) {
                    mCustomerManagePresenter.loadForgetCustomer();
                } else if (customerCareRefIv == view) {
                    mCustomerManagePresenter.loadCustomerCare();
                } else if (visitRefIv == view) {
                    mCustomerManagePresenter.loadVisitStatistics();
                }
            }
        }
    };
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            int mAbsListViewId = adapterView.getId();
            if (mAbsListViewId == R.id.addGridView) {
                toListPager(position);
            } else if (mAbsListViewId == R.id.showCustomerGv) {
                toShowCustomerPager(position);
            } else if (R.id.forgetCustomerLv == mAbsListViewId) {

            }
        }
    };


    private void showDataSelect() {
        DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
        picker.setRange(CalendarUtil.getYear() - 10, CalendarUtil.getYear() + 10);
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1);
        picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
            @Override
            public void onDatePicked(String year, String month) {
                if (!TextUtils.isEmpty(year) && !TextUtils.isEmpty(month)) {
                    showCustomerTv.setText(year + "-" + month);
                    mCustomerManagePresenter.loadShowCustomerItems(showCustomerTv.getText().toString());
                }
            }
        });
        picker.show();
    }

    private void toListPager(int position) {
        switch (position) {
            /*case 0:
                startActivity(new Intent(ct, CustomerListActivity.class)
                        .putExtra(Constants.Intents.CALLER, "Customer!Base")
                        .putExtra(Constants.Intents.TITLE, "正式客户"));
                break;*/
            case 0:
                startActivity(new Intent(ct, CustomerListActivity.class)
                        .putExtra(Constants.Intents.CALLER, "PreCustomer")
                        .putExtra(Constants.Intents.TITLE, "预录入客户"));
                break;
            case 1:
                String emCode = CommonUtil.getEmcode();
                ArrayList<BillListConfig> billListConfigs = new ArrayList<>();
                BillListConfig mBillListConfig = new BillListConfig();
                mBillListConfig.setMe(true);
                mBillListConfig.setTitle("我负责的");
                mBillListConfig.setShowItemNum(4);
                mBillListConfig.setNeedForward(true);
                mBillListConfig.setCaller("Contact");
                mBillListConfig.setCondition("cu_sellercode=\'" + emCode + "\'");
                billListConfigs.add(mBillListConfig);
                mBillListConfig = new BillListConfig(mBillListConfig);
                mBillListConfig.setTitle("我下属的");
                mBillListConfig.setMe(false);
                String mCondition = "cu_sellercode in (select em_code from employee left join job on em_defaulthsid=jo_id where jo_subof=(select em_defaulthsid from employee where em_code =\'" + emCode + "\'))";
                HashMap<Object, Object> dbfindCondition = new HashMap<>();
                dbfindCondition.put("cu_code", "cu_sellercode='" + CommonUtil.getEmcode() + "'");
                mBillListConfig.setCondition(mCondition);
                billListConfigs.add(mBillListConfig);
                startActivity(new Intent(ct, BillListActivity.class)
                        .putExtra(Constants.Intents.DB_FIND_CONDITION, dbfindCondition)
                        .putExtra(Constants.Intents.CONFIG, billListConfigs)
                        .putExtra(Constants.Intents.TITLE, "客户联系人")
                        .putExtra(Constants.Intents.DETAILS_CLASS, CustomerContactDetailActivity.class)
                );
                break;
            case 2:
                emCode = CommonUtil.getEmcode();
                billListConfigs = new ArrayList<>();
                mBillListConfig = new BillListConfig();
                mBillListConfig.setTitle("我的");
                mBillListConfig.setMe(true);
                mBillListConfig.setCaller("VisitRecord");
                mBillListConfig.setCondition("vr_recordercode=\'" + emCode + "\'");
                billListConfigs.add(mBillListConfig);
                mBillListConfig = new BillListConfig();
                mBillListConfig.setTitle("我下属的");
                mBillListConfig.setCaller("VisitRecord");
                mCondition = "(" +
                        "vr_recordercode in ( " +
                        " select em_code from employee left join job on em_defaulthsid=jo_id  where jo_subof= " +
                        " (select em_defaulthsid from employee where em_code =\'" + emCode + "\') " +
                        ")" +
                        ")";
                mBillListConfig.setCondition(mCondition);
                billListConfigs.add(mBillListConfig);
                startActivity(new Intent(ct, BillListActivity.class)
                        .putExtra(Constants.Intents.INPUT_CLASS, VisitRecordBillInputActivity.class)
                        .putExtra(Constants.Intents.CONFIG, billListConfigs)
                        .putExtra(Constants.Intents.TITLE, "拜访报告")
                        .putExtra(Constants.Intents.DETAILS_CLASS, BillDetailsActivity.class)
                );
                break;
        }
    }

    private void toAddPager(int position) {
        String mCaller = "";
        String mTitle = "";
        int mId = 0;
        Class clazz = null;
        HashMap<String, String> dbfindCondition = null;
        switch (position) {
            /*case 0:
                mCaller = "Customer!Base";
                mTitle = "正式客户";
                clazz = CustomerBillInputActivity.class;
                break;*/
            case 0:
                mCaller = "PreCustomer";
                mTitle = "预录入客户";
                clazz = CustomerBillInputActivity.class;
                break;
            case 1:
                dbfindCondition = new HashMap<>();
                dbfindCondition.put("cu_code", "cu_sellercode='" + CommonUtil.getEmcode() + "'");
                mCaller = "Contact";
                mTitle = "客户联系人";
                mId = -1;
                clazz = CustomerContactActivity.class;
                break;
            case 2:
                mCaller = "VisitRecord";
                mTitle = "拜访报告";
                clazz = VisitRecordBillInputActivity.class;
                break;
        }
        startActivity(new Intent(ct, clazz)
                .putExtra(Constants.Intents.CALLER, mCaller)
                .putExtra(Constants.Intents.TITLE, mTitle)
                .putExtra(Constants.Intents.ID, mId)
                .putExtra(Constants.Intents.MY_DOIT, true)
                .putExtra(Constants.Intents.DB_FIND_CONDITION, dbfindCondition)
        );
    }

    private void toShowCustomerPager(int position) {
//        switch (position) {
//            case 2:
//                startActivity(new Intent(ct, CustomerListActivity.class));
//                break;
//        }
    }


    @Override
    public void setShowCustomerAdapter(List<EasyBaseModel> models) {
        mPullToRefreshScrollView.onRefreshComplete();
        showCustomerRefIv.setAnimation(null);
        showCustomerRefTimeTv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        if (showCustomerAdapter == null) {
            showCustomerAdapter = new EasyBaseAdapter(ct, models) {
                @Override
                public View bindView(View view, int position, EasyBaseModel model) {
                    ViewHolder mViewHolder = null;
                    if (view.getTag() == null) {
                        mViewHolder = new ViewHolder();
                        mViewHolder.titleTv = view.findViewById(R.id.titleTv);
                        mViewHolder.numTv = view.findViewById(R.id.numTv);
                        view.setTag(mViewHolder);
                    } else {
                        mViewHolder = (ViewHolder) view.getTag();
                    }
                    mViewHolder.titleTv.setText(model.getTitle());
                    String unit = "个";
                    switch (position) {
                        case 2:
                            unit = "次";
                            break;
                        case 3:
                            unit = "条";
                            break;
                    }
                    TextUtil.create().addTintSection(model.getSubTitle(), 0xff0E80F1)
                            .addBgSection(unit, 0xff666666)
                            .showIn(mViewHolder.numTv);
                    return view;
                }

                class ViewHolder {
                    private TextView titleTv;
                    private TextView numTv;
                }

                @Override
                public int getLayoutRes() {
                    return R.layout.item_customer_manage_show_customer;
                }
            };
            showCustomerGv.setAdapter(showCustomerAdapter);
        } else {
            showCustomerAdapter.updateModels(models);
        }
    }

    @Override
    public void setForgetCustomerAdapter(List<EasyBaseModel> models) {
        mPullToRefreshScrollView.onRefreshComplete();
        forgetCustomerRefIv.setAnimation(null);
        forgetCustomerRefTimeTv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        if (mCustomerForgetAdapter == null) {
            mCustomerForgetAdapter = new CustomerCareListAdapter(ct, models, 1);
            forgetCustomerLv.setAdapter(mCustomerForgetAdapter);
        } else {
            mCustomerForgetAdapter.updateModels(models);
        }
    }

    @Override
    public void setCustomerCareAdapter(List<EasyBaseModel> models) {
        mPullToRefreshScrollView.onRefreshComplete();
        customerCareRefIv.setAnimation(null);
        customerCareRefTimeTv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        if (mCustomerCareAdapter == null) {
            mCustomerCareAdapter = new CustomerCareListAdapter(ct, models, 2);
            customerCareLv.setAdapter(mCustomerCareAdapter);
        } else {
            mCustomerCareAdapter.updateModels(models);
        }
    }

    @Override
    public void setVisitAdapter(List<EasyBaseModel> models) {
        mPullToRefreshScrollView.onRefreshComplete();
        visitRefIv.setAnimation(null);
        visitRefTimeTv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        if (mVisitAdapter == null) {
            mVisitAdapter = new EasyBaseAdapter(ct, models) {
                @Override
                public View bindView(View view, int position, EasyBaseModel model) {
                    ViewHolder mViewHolder = null;
                    if (view.getTag() == null) {
                        mViewHolder = new ViewHolder();
                        mViewHolder.nameTv = (TextView) view.findViewById(R.id.nameTv);
                        mViewHolder.customerNumTv = (TextView) view.findViewById(R.id.customerNumTv);
                        mViewHolder.visitPlanTv = (TextView) view.findViewById(R.id.visitPlanTv);
                        mViewHolder.visitReportNunTv = (TextView) view.findViewById(R.id.visitReportNunTv);
                        view.setTag(mViewHolder);
                    } else {
                        mViewHolder = (ViewHolder) view.getTag();
                    }
                    mViewHolder.nameTv.setText(CommonUtil.getName());
                    mViewHolder.customerNumTv.setText(model.getTitle());
                    mViewHolder.visitPlanTv.setText(model.getSubTitle());
                    mViewHolder.visitReportNunTv.setText(model.getIconUrl());
                    return view;
                }

                class ViewHolder {
                    private TextView nameTv;
                    private TextView customerNumTv;
                    private TextView visitPlanTv;
                    private TextView visitReportNunTv;
                }

                @Override
                public int getLayoutRes() {
                    return R.layout.item_customer_manage_visit_statistics;
                }
            };
            visitLv.setAdapter(mVisitAdapter);
        } else {
            mVisitAdapter.updateModels(models);
        }
    }

}
