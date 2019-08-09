package com.uas.appworks.OA.erp.activity.form;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.DatePicker;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.uas.appworks.OA.erp.adapter.WorkLogsAdapter;
import com.uas.appworks.OA.erp.model.WorkLogs;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WorkLogsActivity extends BaseNetActivity {
    private final int LOAD_KEY = 11;
    private WorkLogsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView monthTv;
    private String date;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_work_logs;
    }

    @Override
    protected void init() throws Exception {
        initView();
        date=DateFormatUtil.long2Str("yyyy-MM");
        loadData(date);
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(this);
    }

    private void initView() {
        EmptyRecyclerView mEmptyRecyclerView = findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
        View view = LayoutInflater.from(ct).inflate(R.layout.menu_work_logs, null);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(view);
        monthTv = view.findViewById(R.id.monthTv);
        monthTv.setText(DateFormatUtil.long2Str("MM") + "月");
        monthTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSlelctDate();
            }
        });
    }

    private void showSlelctDate() {
        DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
        picker.setRange(2017, 2030);
        picker.setSelectedItem(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1);
        picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
            @Override
            public void onDatePicked(String year, String month) {
                date=year + "-" + month;
                loadData(date);
                monthTv.setText(month + "月");
            }
        });
        picker.show();

    }


    private void loadData(String date) {
        if (!NetUtils.isNetWorkConnected(ct)) {
            showToast(R.string.networks_out);
            return;
        }
        Parameter.Builder bilder = new Parameter.Builder()
                .url("mobile/getEffectiveWorkdata.action")
                .addParams("em_code", CommonUtil.getEmcode())
                .record(LOAD_KEY)
                .autoProgress(true)
                .addParams("date", date);
        requestCompanyHttp(bilder, mOnSmartHttpListener);
    }


    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            if (LOAD_KEY == what) {
                LogUtil.i("gong", "message=" + message);
//                message = TestDataUtils.getTestData();
//                LogUtil.i("gong", "testData=" + message);
                handleData(JSONUtil.getJSONArray(message, "listDatas"));
            }
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {

        }
    };

    private void handleData(JSONArray listDatas) {
        List<WorkLogs> models = new ArrayList<>();
        if (!ListUtils.isEmpty(listDatas)) {
            JSONObject object;
            WorkLogs mWorkLogs;
            long thisTimes = DateFormatUtil.str2Long(DateFormatUtil.long2Str(DateFormatUtil.YMD), DateFormatUtil.YMD);
            for (int i = 0; i < listDatas.size(); i++) {
                object = listDatas.getJSONObject(i);
                mWorkLogs = new WorkLogs();
                mWorkLogs.setDate(JSONUtil.getText(object, "date"));
                if (thisTimes == mWorkLogs.getWorkTimes()){
                    continue;
                }
                mWorkLogs.setLate(JSONUtil.getInt(object, "late"));
                mWorkLogs.setEarly(JSONUtil.getInt(object, "early"));
                mWorkLogs.setWorkDate(JSONUtil.getBoolean(object, "isWorkDate"));
                findClass(JSONUtil.getJSONObject(object, "class1"), mWorkLogs);
                findClass(JSONUtil.getJSONObject(object, "class2"), mWorkLogs);
                findClass(JSONUtil.getJSONObject(object, "class3"), mWorkLogs);
                models.add(mWorkLogs);
            }
        }
        Collections.sort(models, mComparator);

        if (mAdapter == null) {
            mAdapter = new WorkLogsAdapter(ct, models);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setWorkLogs(models);
            mAdapter.notifyDataSetChanged();
        }
    }


    private Comparator<WorkLogs> mComparator = new Comparator<WorkLogs>() {
        @Override
        public int compare(WorkLogs t1, WorkLogs t2) {
            if (!TextUtils.isEmpty(t2.getDate()) && !TextUtils.isEmpty(t1.getDate())) {
                return t1.getDate().compareTo(t2.getDate());
            }
            return 0;
        }
    };

    /**
     * @param classObject
     * @param mWorkLogs
     */
    private void findClass(JSONObject classObject, WorkLogs mWorkLogs) {
        if (classObject != null && !classObject.isEmpty()) {
            String wd_offduty_sign = JSONUtil.getText(classObject, "wd_offduty_sign");
            String wd_onduty = JSONUtil.getText(classObject, "wd_onduty");
            String wd_onduty_sign = JSONUtil.getText(classObject, "wd_onduty_sign");
            String wd_offduty = JSONUtil.getText(classObject, "wd_offduty");
            boolean onduty_apprecord = JSONUtil.getBoolean(classObject, "onduty_apprecord");
            boolean offduty_apprecord = JSONUtil.getBoolean(classObject, "offduty_apprecord");
            mWorkLogs.addShift(wd_onduty, wd_onduty_sign, wd_offduty, wd_offduty_sign, onduty_apprecord, offduty_apprecord);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (0x11==requestCode){
            loadData(date);
        }

    }
}
