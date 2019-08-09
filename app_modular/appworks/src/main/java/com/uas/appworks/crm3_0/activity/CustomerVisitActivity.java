package com.uas.appworks.crm3_0.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.base.BaseToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.modular.apputils.utils.UUHttpHelper;
import com.modular.apputils.widget.DivideRadioGroup;
import com.modular.apputils.widget.compactcalender.CompactCalendarView;
import com.modular.apputils.widget.compactcalender.domain.Event;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.uas.appworks.R;
import com.uas.appworks.model.VisitPlan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * CRM 3.0 客户拜访界面（拜访计划）
 */
public class CustomerVisitActivity extends BaseActivity {
    private final int LOAD_DATA = 0x11;
    private final int TAG_DATE = 0x13;//记录获取的值对象，比如（20181101 表示2018-11 我的数据 20181102 表示2018-11 我的下属数据）

    private DivideRadioGroup visitTypeRg;
    private RadioButton myVisitRb;
    private TextView monthTv;
    private TextView toDayTv;
    private CompactCalendarView mCalendarView;
    private RecyclerView mRecyclerView;
    private Date mCurrentDate;
    private SparseArray<List<VisitPlan>> datas;
    private UUHttpHelper mUUHttpHelper;
    private boolean dataUpdate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            startActivity(new Intent(ct, CustomerVisitBillInputActivity.class)
                    .putExtra(Constants.Intents.CALLER, "MobileOutPlans")
                    .putExtra(Constants.Intents.TITLE, "客户拜访计划")
                    .putExtra(Constants.Intents.ID, 0));
            dataUpdate = true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_visit);
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(this));
        initView();
    }


    private void initView() {
        datas = new SparseArray<>();
        mCurrentDate = new Date();
        myVisitRb = findViewById(R.id.myVisitRb);
        final View myLine = findViewById(R.id.myLine);
        final View otherLine = findViewById(R.id.otherLine);
        visitTypeRg = findViewById(R.id.visitTypeRg);
        monthTv = findViewById(R.id.monthTv);
        toDayTv = findViewById(R.id.newDayTv);
        mCalendarView = findViewById(R.id.compactcalendar_view);
        EmptyRecyclerView mEmptyRecyclerView = findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        myLine.setVisibility(View.VISIBLE);
        otherLine.setVisibility(View.INVISIBLE);
        visitTypeRg.setOnCheckedChangeListener(new DivideRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DivideRadioGroup group, int checkedId) {
                if (checkedId == R.id.myVisitRb) {
                    myLine.setVisibility(View.VISIBLE);
                    otherLine.setVisibility(View.INVISIBLE);
                } else if (R.id.otherVisitRb == checkedId) {
                    myLine.setVisibility(View.INVISIBLE);
                    otherLine.setVisibility(View.VISIBLE);
                }
                loadData(false);
            }
        });

        toDayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDate = new Date();
                mCalendarView.setCurrentDate(mCurrentDate);
                changeDay();
            }
        });
        mCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                mCurrentDate = dateClicked;
                changeDay();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mCurrentDate = firstDayOfNewMonth;
                changeDay();
            }
        });
        mCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        mCalendarView.setIsRtl(false);
        mCalendarView.displayOtherMonthDays(false);
        mCalendarView.setLocale(TimeZone.getDefault(), Locale.CHINESE);
        mCalendarView.setUseThreeLetterAbbreviation(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        changeDay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataUpdate) {
            loadData(true);
            dataUpdate = false;
        }
    }

    private void changeDay() {
        monthTv.setText(DateFormatUtil.date2Str(mCurrentDate, DateFormatUtil.YM));
        loadData(false);
    }

    private void loadData(boolean muchNet) {
        progressDialog.show();
        boolean isOther = !myVisitRb.isChecked();
        String month = DateFormatUtil.date2Str(mCurrentDate, DateFormatUtil.YM);
        int monthInt = 1;
        try {
            monthInt = Integer.valueOf(month.replace("-", "") + (isOther ? "02" : "01"));
        } catch (Exception e) {
        }
        if (muchNet) {
            loadData(isOther, monthInt, month);
        } else {
            List<VisitPlan> visitPlanArrayList = datas.get(monthInt);
            if (visitPlanArrayList == null) {
                loadData(isOther, monthInt, month);
            } else {
                handlerAndShowByDay(visitPlanArrayList);
            }
        }

    }

    private void loadData(boolean isOther, int monthInt, String month) {
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                .mode(Method.GET)
                .addParams("dataTime", month)
                .addParams("salesmanCode", CommonUtil.getEmcode())
                .addParams("flag", isOther ? 1 : 0)
                .addTag(TAG_DATE, monthInt)
                .record(LOAD_DATA)
                .url("mobile/crm/getvisitplanmsg.action"), mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            int monthInt = 1;
            if (tag != null && tag.get(TAG_DATE) != null && tag.get(TAG_DATE) instanceof Integer) {
                monthInt = (int) tag.get(TAG_DATE);
            }
            switch (what) {
                case LOAD_DATA:
                    handerData(monthInt, JSONUtil.getJSONArray(message, "visitPlan"));
                    break;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            if (JSONUtil.validateJSONObject(message)) {
                ToastUtil.showToast(ct, JSONUtil.getText(message, "exceptionInfo"));
            } else {
                ToastUtil.showToast(ct, message);
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    };

    private void handerData(int monthInt, JSONArray array) throws Exception {
        List<VisitPlan> visitPlanArrayList = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                VisitPlan mVisitPlan = new VisitPlan();
                JSONObject object = array.getJSONObject(i);
                int id = JSONUtil.getInt(object, "id");
                String code = JSONUtil.getText(object, "code");
                String customerName = JSONUtil.getText(object, "customer_name");
                String projectName = JSONUtil.getText(object, "xm_name");
                String customerAddress = JSONUtil.getText(object, "customer_address");
                long entryDate = JSONUtil.getTime(object, "entry_date");
                long startTime = JSONUtil.getTime(object, "start_time");
                long endTime = JSONUtil.getTime(object, "end_time");
                String status = JSONUtil.getText(object, "mpd_zdstatus", "status");
                String doman = JSONUtil.getText(object, "doman");
                String domanCode = JSONUtil.getText(object, "domanCode");
                String billstatus = JSONUtil.getText(object, "mp_status");

                mVisitPlan.setId(id);
                mVisitPlan.setCode(code);
                mVisitPlan.setRecordDay(DateFormatUtil.long2Str(entryDate, DateFormatUtil.YMD));
                mVisitPlan.setCustomerName(customerName);
                mVisitPlan.setProjectName(projectName);
                mVisitPlan.setCustomerAddress(customerAddress);
                mVisitPlan.setStartTime(startTime);
                mVisitPlan.setEndTime(endTime);
                mVisitPlan.setEntryDate(entryDate);
                mVisitPlan.setStatus(status);
                mVisitPlan.setDoman(doman);
                mVisitPlan.setDomanCode(domanCode);
                mVisitPlan.setBillStatus(billstatus);

                visitPlanArrayList.add(mVisitPlan);
            }
            datas.put(monthInt, visitPlanArrayList);
        }
        handlerAndShowByDay(visitPlanArrayList);
    }

    private ListAdapter mListAdapter;


    /**
     * 处理当月数据并显示
     *
     * @param visitPlanArrayList 获取到的月数据
     */
    private void handlerAndShowByDay(List<VisitPlan> visitPlanArrayList) {
        mCalendarView.removeAllEvents();
        if (ListUtils.isEmpty(visitPlanArrayList)) {
            setAdapter(visitPlanArrayList);
        } else {
            List<VisitPlan> showPlan = new ArrayList<>();
            String mCurrentDay = DateFormatUtil.date2Str(mCurrentDate, DateFormatUtil.YMD);
            for (VisitPlan visitPlan : visitPlanArrayList) {
                mCalendarView.addEvent(new Event(Color.argb(255, 169, 68, 65), visitPlan.getEntryDate()));
                if (mCurrentDay.equals(visitPlan.getRecordDay())) {
                    showPlan.add(visitPlan);
                }
            }
            setAdapter(showPlan);
        }
    }

    private void setAdapter(List<VisitPlan> visitPlanArrayList) {
        if (mListAdapter == null) {
            mListAdapter = new ListAdapter(visitPlanArrayList);
            mRecyclerView.setAdapter(mListAdapter);
        } else {
            mListAdapter.updateVisitPlans(visitPlanArrayList);
        }
        progressDialog.dismiss();
    }

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.VisitRecordViewHolder> {
        private List<VisitPlan> visitPlans;
        private LayoutInflater mLayoutInflater;

        public ListAdapter(List<VisitPlan> visitPlans) {
            this.visitPlans = visitPlans;
        }

        public void updateVisitPlans(List<VisitPlan> visitPlans) {
            this.visitPlans = visitPlans;
            notifyDataSetChanged();
        }

        @Override
        public VisitRecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new VisitRecordViewHolder(viewGroup);
        }

        public LayoutInflater getLayoutInflater() {
            if (mLayoutInflater == null) {
                mLayoutInflater = LayoutInflater.from(ct);
            }
            return mLayoutInflater;
        }

        @Override
        public int getItemCount() {
            return ListUtils.getSize(visitPlans);
        }

        //拜访记录visitRecord
        class VisitRecordViewHolder extends RecyclerView.ViewHolder {
            private TextView dateTv;
            private TextView contactTv;
            private TextView doManTv;
            private TextView statusTv;

            public VisitRecordViewHolder(ViewGroup viewGroup) {
                this(getLayoutInflater().inflate(R.layout.item_customer_detail_bottom_visitrecord, viewGroup, false));
            }

            public VisitRecordViewHolder(View itemView) {
                super(itemView);
                dateTv = (TextView) itemView.findViewById(R.id.dateTv);
                contactTv = (TextView) itemView.findViewById(R.id.contactTv);
                doManTv = (TextView) itemView.findViewById(R.id.doManTv);
                statusTv = (TextView) itemView.findViewById(R.id.statusTv);
                contactTv.setVisibility(View.GONE);

            }
        }

        @Override
        public void onBindViewHolder(VisitRecordViewHolder holder, int i) {
            VisitPlan mVisitPlan = visitPlans.get(i);
            if (!TextUtils.isEmpty(mVisitPlan.getCustomerName())) {
                holder.dateTv.setText("客户名称:" + mVisitPlan.getCustomerName());
            } else if (!TextUtils.isEmpty(mVisitPlan.getProjectName())) {
                holder.dateTv.setText("项目名称:" + mVisitPlan.getProjectName());
            }
//            holder.dateTv.setText(mVisitPlan.getRecordDay());
            holder.doManTv.setText(mVisitPlan.getDoman());
            String status = mVisitPlan.getStatus();
            String billStatus = mVisitPlan.getBillStatus();
            if ("已转单".equals(status)) {
                holder.statusTv.setText(status);
            } else {
                holder.statusTv.setText(billStatus);
            }
            holder.itemView.setTag(mVisitPlan);
            holder.itemView.setOnClickListener(mOnItemClickListener);
        }

        private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null && view.getTag() instanceof VisitPlan) {
                    VisitPlan mVisitPlan = (VisitPlan) view.getTag();
                    String billStatus = mVisitPlan.getBillStatus();
                    if ("在录入".equals(billStatus)) {
                        startActivity(new Intent(ct, CustomerVisitBillInputActivity.class)
                                .putExtra(Constants.Intents.CALLER, "MobileOutPlans")
                                .putExtra(Constants.Intents.TITLE, "客户拜访计划")
                                .putExtra(Constants.Intents.DETAILS_CLASS, CustomerVisitDetailsActivity.class)
                                .putExtra(Constants.Intents.INPUT_CLASS, CustomerVisitBillInputActivity.class)
                                .putExtra(Constants.Intents.ID, mVisitPlan.getId()));
                    } else {
                        startActivity(new Intent(ct, CustomerVisitDetailsActivity.class)
                                .putExtra(Constants.Intents.TITLE, "客户拜访详情")
                                .putExtra(Constants.Intents.MY_DOIT, myVisitRb.isChecked())
                                .putExtra(Constants.Intents.STATUS, mVisitPlan.getStatus())
                                .putExtra(Constants.Intents.BILL_STATUS, billStatus)
                                .putExtra(Constants.Intents.ID, mVisitPlan.getId())
                                .putExtra(Constants.Intents.CALLER, "MobileOutPlans")
                        );
                    }
                    dataUpdate = true;
                }
            }
        };
    }

}
