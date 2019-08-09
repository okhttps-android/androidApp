package com.uas.appworks.OA.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.OA.erp.model.WorkReportBean;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2016/11/1.
 * Revised by RaoMeng on 2017/10/18
 * 工作汇报列表页面
 */

public class WorkDailyShowActivity extends BaseActivity {
    private final int DAILY_DETAIL_EDIT = 0x01;
    private final int WORK_DAILY_REQUEST = 101;
    private final int DELETE_REPORT_ITEM = 0x11;

    private VoiceSearchView voiceSearchView;
    private PullToRefreshListView mPullToRefreshListView;
    private int mCurrentPage = 1;
    private WorkReportAdapter mWorkReportAdapter;
    public EmptyLayout mEmptyLayout;
    private int mPosition;
    private int returnedData;
    private List<WorkReportBean> mWorkReportList;
    private String delete_succeed;
    private String mCaller = "WorkDaily";
    private int mReportType = Constants.WORK_REPORT_DAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        if (CommonUtil.isNetWorkConnected(this)) {
            progressDialog.show();
            initData();
        } else {
            ToastUtil.showToast(this, R.string.networks_out);
        }
        initClickEvent();
        super.onResume();
    }

    private void initClickEvent() {
        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                mWorkReportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                mWorkReportAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mWorkReportAdapter == null) {

                } else {
                    if (!StringUtil.isEmpty(editable.toString())) {
                        mWorkReportAdapter.getFilter().filter(editable.toString());
                    } else {
                        mWorkReportAdapter.getFilter().filter(null);
                    }
                }
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                List<WorkReportBean> workReportList = mWorkReportAdapter.getWorkReportList();
                int reallyPosition = (int) parent.getItemIdAtPosition(position);
                WorkReportBean workReportBean = workReportList.get(reallyPosition);
                if (!TextUtils.isEmpty(workReportList.get(position - 1).getReportStatus())
                        && workReportList.get(position - 1).getReportStatus().equals("在录入")) {  //列表中在录入状态直接跳到新增界面
                    Intent intent = new Intent(activity, WorkReportAddActivity.class);
                    intent.putExtra("id", workReportBean.getReportId());
                    intent.putExtra("rs_summary", workReportBean.getComment());
                    intent.putExtra("rs_plan", workReportBean.getPlan());
                    intent.putExtra("rs_experience", workReportBean.getExperience());
                    intent.putExtra("caller", mCaller);
                    intent.putExtra("rs_donetask", workReportBean.getContext());
                    intent.putExtra("rs_undotask", workReportBean.getUnfinishedTask());
                    intent.putExtra("report_type", mReportType);
                    intent.putExtra("resubmit", "unsub_tosub");
                    intent.putExtra("report_date", workReportBean.getDate());
                    if (mReportType == Constants.WORK_REPORT_DAY) {
                        intent.putExtra("report_weekdays", workReportBean.getWeekDays());
                    } else if (mReportType == Constants.WORK_REPORT_WEEK || mReportType == Constants.WORK_REPORT_MONTH) {
                        intent.putExtra("report_serial", workReportBean.getSerial());
                        intent.putExtra("report_startTime", workReportBean.getStartTime());
                        intent.putExtra("report_endTime", workReportBean.getEndTime());
                    }
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(WorkDailyShowActivity.this, DailydetailsActivity.class);
                    intent.putExtra("ID", workReportBean.getReportId());
                    intent.putExtra("Date", workReportBean.getDate());
                    intent.putExtra("Content", workReportBean.getComment());
                    intent.putExtra("WD_Status", workReportBean.getReportStatus());
                    intent.putExtra("Plan", workReportBean.getPlan());
                    intent.putExtra("Experience", workReportBean.getExperience());
                    intent.putExtra("Donetask", workReportBean.getContext());
                    intent.putExtra("Undotask", workReportBean.getUnfinishedTask());
                    intent.putExtra("fromwhere", "dailylist");
                    intent.putExtra("report_type", mReportType);
                    intent.putExtra("caller", mCaller);
                    if (mReportType == Constants.WORK_REPORT_DAY) {
                        intent.putExtra("report_date", workReportBean.getDate());
                        intent.putExtra("report_weekdays", workReportBean.getWeekDays());
                    } else if (mReportType == Constants.WORK_REPORT_WEEK || mReportType == Constants.WORK_REPORT_MONTH) {
                        intent.putExtra("report_serial", workReportBean.getSerial());
                        intent.putExtra("report_startTime", workReportBean.getStartTime());
                        intent.putExtra("report_endTime", workReportBean.getEndTime());
                    }
                    startActivityForResult(intent, DAILY_DETAIL_EDIT);
                }
            }
        });
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(WorkDailyShowActivity.this)) {
                    mCurrentPage = 1;
                    initData();
                } else {
                    ToastUtil.showToast(WorkDailyShowActivity.this, R.string.networks_out);
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mPullToRefreshListView.onRefreshComplete();
                            }
                        }, 200);
                    }
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(WorkDailyShowActivity.this)) {
                    mCurrentPage++;
                    initData();
                } else {
                    ToastUtil.showToast(WorkDailyShowActivity.this, R.string.networks_out);
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mPullToRefreshListView.onRefreshComplete();
                            }
                        }, 200);
                    }
                }
            }
        });
    }

    public void initView() {
        setContentView(R.layout.activity_work_daily);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.work_daily_context_ptlv);

        mEmptyLayout = new EmptyLayout(this, mPullToRefreshListView.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);

        mWorkReportList = new ArrayList<>();
        mWorkReportAdapter = new WorkReportAdapter(this);
        mWorkReportAdapter.setWorkReportList(mWorkReportList);
        mPullToRefreshListView.getRefreshableView().setAdapter(mWorkReportAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            mCaller = intent.getStringExtra("caller");
            mReportType = intent.getIntExtra("report_type", Constants.WORK_REPORT_DAY);
        }
        if (mReportType == Constants.WORK_REPORT_DAY) {
            setTitle(R.string.daily_report_record);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            setTitle(R.string.weekly_report_record);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            setTitle(R.string.monthly_report_record);
        }
    }

    public void initData() {
        String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/getWorkReports.action";
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(getApplicationContext(), "erp_username"));
        param.put("caller", mCaller);
        param.put("pageIndex", mCurrentPage);
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, mHandler, headers, WORK_DAILY_REQUEST, null, null, "post");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            putDownInput();
            final Intent intent = getIntent();
            if (!TextUtils.isEmpty(intent.getStringExtra("fromwhere"))
                    && intent.getStringExtra("fromwhere").equals("nosubmitdaily")) {
                finish();
            } else {
                Intent intent1 = new Intent(WorkDailyShowActivity.this, WorkReportMenuActivity.class);
                startActivity(intent1);
            }
        }
        super.onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        final Intent intent = getIntent();
        if (!TextUtils.isEmpty(intent.getStringExtra("fromwhere"))
                && intent.getStringExtra("fromwhere").equals("nosubmitdaily")) {
            finish();
        } else {
            Intent intent1 = new Intent(WorkDailyShowActivity.this, WorkReportMenuActivity.class);
            startActivity(intent1);
        }
        super.onBackPressed();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WORK_DAILY_REQUEST:
                    if (mCurrentPage == 1) {
                        mWorkReportList.clear();
                        ToastMessage(getString(R.string.common_refresh_finish));
                    } else {
                        ToastMessage(getString(R.string.common_up_finish));
                    }
                    String result = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("dailylist: ", result);
                    mPullToRefreshListView.setVisibility(View.VISIBLE);
                    mPullToRefreshListView.onRefreshComplete();
                    if (voiceSearchView != null)
                        voiceSearchView.setText("");
                    try {
                        com.alibaba.fastjson.JSONObject resultJsonObject = JSON.parseObject(result);
                        com.alibaba.fastjson.JSONArray listDataArray = resultJsonObject.getJSONArray("listdata");
                        if (listDataArray != null) {
                            for (int i = 0; i < listDataArray.size(); i++) {
                                com.alibaba.fastjson.JSONObject listDataObject = listDataArray.getJSONObject(i);
                                WorkReportBean workReportBean = new WorkReportBean();
                                if (mReportType == Constants.WORK_REPORT_DAY) {
                                    workReportBean = dailyReportBean(listDataObject);
                                } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                    workReportBean = weeklyReportBean(listDataObject);
                                } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                    workReportBean = monthlyReportBean(listDataObject);
                                }
                                mWorkReportList.add(workReportBean);
                            }
                        }
                        mWorkReportAdapter.notifyDataSetChanged();
                        if (mWorkReportAdapter.getWorkReportList().size() == 0) {
                            mEmptyLayout.showEmpty();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                    break;
                case DELETE_REPORT_ITEM:
                    int filterPosition = (int) msg.arg1;
                    int realPosition = (int) msg.arg2;
                    mWorkReportList.remove(realPosition);
                    if (mWorkReportAdapter.filterWorkReportList != null
                            && mWorkReportAdapter.filterWorkReportList.size() > filterPosition) {
                        mWorkReportAdapter.filterWorkReportList.remove(filterPosition);
                    }
                    mWorkReportAdapter.notifyDataSetChanged();
                    mPullToRefreshListView.postInvalidate();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                            progressDialog.dismiss();
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    };

    private WorkReportBean monthlyReportBean(JSONObject listDataObject) {
        WorkReportBean workReportBean = new WorkReportBean();
        workReportBean.setReportId(JSONUtil.getInt(listDataObject, "WM_ID"));
        workReportBean.setEmp(JSONUtil.getText(listDataObject, "WM_EMP"));
        workReportBean.setDate(JSONUtil.getText(listDataObject, "WM_DATE"));
        workReportBean.setComment(JSONUtil.getText(listDataObject, "WM_COMMENT"));
        workReportBean.setDepart(JSONUtil.getText(listDataObject, "WM_DEPART"));
        workReportBean.setJoname(JSONUtil.getText(listDataObject, "WM_JONAME"));
        workReportBean.setPlan(JSONUtil.getText(listDataObject, "WM_PLAN"));
        workReportBean.setExperience(JSONUtil.getText(listDataObject, "WM_EXPERIENCE"));
        workReportBean.setContext(JSONUtil.getText(listDataObject, "WM_CONTEXT"));
        workReportBean.setApprovalStatus(JSONUtil.getText(listDataObject, "STATUS"));
        workReportBean.setReportStatus(JSONUtil.getText(listDataObject, "WM_STATUS"));
        workReportBean.setUnfinishedTask(JSONUtil.getText(listDataObject, "WM_UNFINISHEDTASK"));
        workReportBean.setSerial(JSONUtil.getText(listDataObject, "WM_MONTH"));
        workReportBean.setStartTime(JSONUtil.getText(listDataObject, "WM_STARTTIME"));
        workReportBean.setEndTime(JSONUtil.getText(listDataObject, "WM_ENDTIME"));
        workReportBean.setRN(JSONUtil.getInt(listDataObject, "RN"));
        return workReportBean;
    }

    private WorkReportBean weeklyReportBean(JSONObject listDataObject) {
        WorkReportBean workReportBean = new WorkReportBean();
        workReportBean.setReportId(JSONUtil.getInt(listDataObject, "WW_ID"));
        workReportBean.setEmp(JSONUtil.getText(listDataObject, "WW_EMP"));
        workReportBean.setDate(JSONUtil.getText(listDataObject, "WW_DATE"));
        workReportBean.setComment(JSONUtil.getText(listDataObject, "WW_COMMENT"));
        workReportBean.setDepart(JSONUtil.getText(listDataObject, "WW_DEPART"));
        workReportBean.setJoname(JSONUtil.getText(listDataObject, "WW_JONAME"));
        workReportBean.setPlan(JSONUtil.getText(listDataObject, "WW_PLAN"));
        workReportBean.setExperience(JSONUtil.getText(listDataObject, "WW_EXPERIENCE"));
        workReportBean.setContext(JSONUtil.getText(listDataObject, "WW_CONTEXT"));
        workReportBean.setApprovalStatus(JSONUtil.getText(listDataObject, "STATUS"));
        workReportBean.setReportStatus(JSONUtil.getText(listDataObject, "WW_STATUS"));
        workReportBean.setUnfinishedTask(JSONUtil.getText(listDataObject, "WW_UNFINISHEDTASK"));
        workReportBean.setSerial(JSONUtil.getText(listDataObject, "WW_WEEK"));
        workReportBean.setStartTime(JSONUtil.getText(listDataObject, "WW_STARTTIME"));
        workReportBean.setEndTime(JSONUtil.getText(listDataObject, "WW_ENDTIME"));
        workReportBean.setRN(JSONUtil.getInt(listDataObject, "RN"));
        return workReportBean;
    }

    private WorkReportBean dailyReportBean(JSONObject listDataObject) {
        WorkReportBean workReportBean = new WorkReportBean();
        workReportBean.setReportId(JSONUtil.getInt(listDataObject, "WD_ID"));
        workReportBean.setEmp(JSONUtil.getText(listDataObject, "WD_EMP"));
        workReportBean.setDate(JSONUtil.getText(listDataObject, "WD_DATE"));
        workReportBean.setComment(JSONUtil.getText(listDataObject, "WD_COMMENT"));
        workReportBean.setDepart(JSONUtil.getText(listDataObject, "WD_DEPART"));
        workReportBean.setJoname(JSONUtil.getText(listDataObject, "WD_JONAME"));
        workReportBean.setPlan(JSONUtil.getText(listDataObject, "WD_PLAN"));
        workReportBean.setExperience(JSONUtil.getText(listDataObject, "WD_EXPERIENCE"));
        workReportBean.setContext(JSONUtil.getText(listDataObject, "WD_CONTEXT"));
        workReportBean.setApprovalStatus(JSONUtil.getText(listDataObject, "STATUS"));
        workReportBean.setReportStatus(JSONUtil.getText(listDataObject, "WD_STATUS"));
        workReportBean.setUnfinishedTask(JSONUtil.getText(listDataObject, "WD_UNFINISHEDTASK"));
        String wd_weekdays = JSONUtil.getText(listDataObject, "WD_WEEKDAYS");
        if (wd_weekdays != null && "monday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期一";
        } else if (wd_weekdays != null && "tuesday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期二";
        } else if (wd_weekdays != null && "wednesday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期三";
        } else if (wd_weekdays != null && "thursday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期四";
        } else if (wd_weekdays != null && "friday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期五";
        } else if (wd_weekdays != null && "saturday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期六";
        } else if (wd_weekdays != null && "sunday".equals(wd_weekdays.trim())) {
            wd_weekdays = "星期日";
        }
        workReportBean.setWeekDays(wd_weekdays);
        workReportBean.setEntryDate(JSONUtil.getText(listDataObject, "WD_ENTRYDATE"));
        workReportBean.setRN(JSONUtil.getInt(listDataObject, "RN"));
        return workReportBean;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == DAILY_DETAIL_EDIT) {
            if (resultCode == DailydetailsActivity.DELETE_SUCCEED) {
                if (data != null) {
                    delete_succeed = data.getStringExtra("delete_succeed");
                    returnedData = data.getIntExtra("deleted_id", 0);
                    if (mWorkReportList != null) {
                        for (int i = 0; i < mWorkReportList.size(); i++) {
                            int currid = mWorkReportList.get(i).getReportId();
                            if (currid == returnedData) {
                                mWorkReportList.remove(i);
                            }
                        }
                    }
                    if (mWorkReportAdapter.getWorkReportList() != null) {
                        for (int i = 0; i < mWorkReportAdapter.getWorkReportList().size(); i++) {
                            int currid = mWorkReportAdapter.getWorkReportList().get(i).getReportId();
                            if (currid == returnedData) {
                                mWorkReportAdapter.getWorkReportList().remove(i);
                            }
                        }
                    }
                    mWorkReportAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    private void putDownInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(voiceSearchView.getWindowToken(), 0);
    }

    public class WorkReportAdapter extends BaseAdapter implements Filterable {
        private static final int DELETE_DOC_REQUEST_SUCCESSFULLY = 1219;
        private List<WorkReportBean> workReportList;
        private List<WorkReportBean> filterWorkReportList;
        private Context mContext;
        private int mFilterPosition;
        private int mRealPosition;

        public WorkReportAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public List<WorkReportBean> getWorkReportList() {
            return workReportList;
        }

        public void setWorkReportList(List<WorkReportBean> workReportList) {
            this.workReportList = workReportList;
        }

        @Override
        public int getCount() {
            return workReportList == null ? 0 : workReportList.size();
        }

        @Override
        public Object getItem(int position) {
            return workReportList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.item_activity_workdaily, null);
                viewHolder.WorkDailyDate = (TextView) convertView.findViewById(R.id.item_activity_workdaily_time_tv);
                viewHolder.WorkDailySummary = (TextView) convertView.findViewById(R.id.item_activity_workdaily_summary_tv);
                viewHolder.WorkDailyStatus = (TextView) convertView.findViewById(R.id.item_activity_workdaily_status);
                viewHolder.WorkDailyDelete = (TextView) convertView.findViewById(R.id.unsubmit_delete_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String dateText = getDateText(workReportList, position);
            viewHolder.WorkDailyDate.setText(dateText);

            //为日报列表界面赋值（状态，总结）,注意已审批和待审批的字体颜色
            if (!TextUtils.isEmpty(workReportList.get(position).getReportStatus()) &&
                    workReportList.get(position).getReportStatus().equals("已审核")) {
                viewHolder.WorkDailyStatus.setTextColor(mContext.getResources().getColor(R.color.approval));
                viewHolder.WorkDailyStatus.setText(mContext.getString(R.string.status_approved));
                viewHolder.WorkDailyDelete.setVisibility(View.GONE);
            } else if (!TextUtils.isEmpty(workReportList.get(position).getReportStatus()) &&
                    workReportList.get(position).getReportStatus().equals("已提交")) {
                viewHolder.WorkDailyStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                viewHolder.WorkDailyStatus.setText(mContext.getString(R.string.status_pending));
                viewHolder.WorkDailyDelete.setVisibility(View.GONE);
            } else if (!TextUtils.isEmpty(workReportList.get(position).getReportStatus()) &&
                    workReportList.get(position).getReportStatus().equals("在录入")) {
                viewHolder.WorkDailyStatus.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                viewHolder.WorkDailyStatus.setText(mContext.getString(R.string.status_unsubmit));
                viewHolder.WorkDailyDelete.setVisibility(View.VISIBLE);
            }
            int VERSION_CODES = Build.VERSION.SDK_INT;
            if (VERSION_CODES <= 20) {
                //处理Android 4.4以下某些机型 android:ellipsize=“end”失效bug
                viewHolder.WorkDailySummary.setEllipsize(null);
            }
            viewHolder.WorkDailySummary.setText(workReportList.get(position).getComment());

            viewHolder.WorkDailyDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupWindowHelper.showAlart(WorkDailyShowActivity.this, getString(R.string.common_notice),
                            getString(R.string.delete_notice1), new PopupWindowHelper.OnSelectListener() {
                                @Override
                                public void select(boolean selectOk) {
                                    if (selectOk) {
                                        if (CommonUtil.isNetWorkConnected(mContext)) {
                                            int mkeyValue = workReportList.get(position).getReportId();
                                            mFilterPosition = position;
                                            for (int i = 0; i < mWorkReportList.size(); i++) {
                                                if (mWorkReportList.get(i).getReportId() == mkeyValue) {
                                                    mRealPosition = i;
                                                }
                                            }
                                            dodelete(mkeyValue);
                                        } else {
                                            ToastUtil.showToast(mContext, R.string.networks_out);
                                        }
                                    }
                                }
                            });
                }
            });

            return convertView;
        }

        private void dodelete(int mkeyValue) {
            String url = CommonUtil.getSharedPreferences(mContext, "erp_baseurl") + "/mobile/commondelete.action";
            Map<String, Object> formStoreMap = new HashMap<>();
            HashMap<String, Object> params = new HashMap<>();
            params.put("caller", mCaller);
            params.put("id", mkeyValue);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
            LogUtil.prinlnLongMsg("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
            ViewUtil.httpSendRequest(mContext, url, params, handler, headers, DELETE_DOC_REQUEST_SUCCESSFULLY, null, null, "get");
        }

        private String getDateText(List<WorkReportBean> workReportList, int position) {
            String dateText = null;
            if (mReportType == Constants.WORK_REPORT_DAY) {
                dateText = getDailyTimeStr(workReportList.get(position).getDate(), workReportList.get(position).getWeekDays());
                dateText = (dateText == null ? workReportList.get(position).getDate() : dateText);
            } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                dateText = getWeeklyTimeStr(workReportList.get(position).getStartTime(), workReportList.get(position).getEndTime(), workReportList.get(position).getSerial());
                dateText = (dateText == null ? ((TextUtils.isEmpty(workReportList.get(position).getSerial()) ? "" : "第" + workReportList.get(position).getSerial() + "周  ")
                        + workReportList.get(position).getDate()) : dateText);
            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                dateText = getMonthlyTimeStr(workReportList.get(position).getStartTime());
                dateText = (dateText == null ? ((TextUtils.isEmpty(workReportList.get(position).getSerial()) ? "" : workReportList.get(position).getSerial() + "月  ")
                        + workReportList.get(position).getDate()) : dateText);
            }
            return dateText;
        }

        private String getDailyTimeStr(String dailyDate, String dailyWeek) {
            try {
                String[] daily = dailyDate.split("-");
                StringBuilder yesterdayString = new StringBuilder("");
                if (daily != null && daily.length >= 3) {
                    yesterdayString.append(daily[0] + "年").append(daily[1] + "月").append(daily[2] + "日").append("  " + dailyWeek);
                }
                return yesterdayString.toString();
            } catch (Exception e) {
                return null;
            }
        }

        private String getWeeklyTimeStr(String weeklyStartDate, String weeklyEndDate, String weeklySerial) {
            try {
                String[] weekStartArray = weeklyStartDate.split("-");
                String[] weekEndArray = weeklyEndDate.split("-");
                StringBuilder weekTimeString = new StringBuilder("");
                if (weekStartArray != null && weekStartArray.length >= 3) {
                    String serial = TextUtils.isEmpty(weeklySerial) ? "" : weeklySerial + "周  ";
                    weekTimeString.append(weekStartArray[0] + "年").append(serial).append(weekStartArray[1] + "月").append(weekStartArray[2] + "日");
                    if (weekEndArray.length >= 3) {
                        weekTimeString.append("-" + weekEndArray[1] + "月").append(weekEndArray[2] + "日");
                    }
                }
                return weekTimeString.toString();
            } catch (Exception e) {
                return null;
            }
        }

        private String getMonthlyTimeStr(String monthlyDate) {
            try {
                String[] monthDate = monthlyDate.split("-");
                StringBuilder thisMonthString = new StringBuilder("");
                if (monthDate != null && monthDate.length >= 2) {
                    thisMonthString.append(monthDate[0] + "年  ").append(monthDate[1] + "月");
                }
                return thisMonthString.toString();
            } catch (Exception e) {
                return null;
            }
        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case DELETE_DOC_REQUEST_SUCCESSFULLY:
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String delete_result = msg.getData().getString("result");
                            if (JSON.parseObject(delete_result).containsKey("success") && JSON.parseObject(delete_result).getBoolean("success")) {
                                Message message = Message.obtain();
                                message.what = DELETE_REPORT_ITEM;
                                message.arg1 = mFilterPosition;
                                message.arg2 = mRealPosition;
                                mHandler.sendMessage(message);
                                String deleteResult = getResources().getString(R.string.report_delete_result);
                                String deleteSuccess = "";
                                if (mReportType == Constants.WORK_REPORT_DAY) {
                                    deleteSuccess = String.format(deleteResult, "日", "成功");
                                } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                    deleteSuccess = String.format(deleteResult, "周", "成功");
                                } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                    deleteSuccess = String.format(deleteResult, "月", "成功");
                                }
                                Toast.makeText(mContext, deleteSuccess, Toast.LENGTH_LONG).show();
//                                notifyDataSetInvalidated();
                            }
                        }
                        break;

                    default:
                        if (msg.getData() != null) {
                            if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                                Toast.makeText(mContext, msg.getData().getString("result"), Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                }
            }
        };

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        filterResults.values = mWorkReportList;
                        filterResults.count = mWorkReportList.size();
                        filterWorkReportList = null;
                    } else {
                        filterWorkReportList = new ArrayList<>();
                        for (int i = 0; i < mWorkReportList.size(); i++) {
//                            String date = mWorkReportList.get(i).getDate();
                            String date = getDateText(mWorkReportList, i);
                            String comment = mWorkReportList.get(i).getComment();
                            String approvalStatus = "";
                            if (!TextUtils.isEmpty(mWorkReportList.get(i).getReportStatus()) &&
                                    mWorkReportList.get(i).getReportStatus().equals("已审核")) {
                                approvalStatus = mContext.getString(R.string.status_approved);
                            } else if (!TextUtils.isEmpty(mWorkReportList.get(i).getReportStatus()) &&
                                    mWorkReportList.get(i).getReportStatus().equals("已提交")) {
                                approvalStatus = mContext.getString(R.string.status_pending);
                            } else if (!TextUtils.isEmpty(mWorkReportList.get(i).getReportStatus()) &&
                                    mWorkReportList.get(i).getReportStatus().equals("在录入")) {
                                approvalStatus = mContext.getString(R.string.status_unsubmit);
                            }
                            if (date.contains(constraint.toString())
                                    || comment.contains(constraint.toString())
                                    || approvalStatus.contains(constraint.toString())) {
                                filterWorkReportList.add(mWorkReportList.get(i));
                            }
                        }

                        filterResults.values = filterWorkReportList;
                        filterResults.count = filterWorkReportList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    workReportList = (List<WorkReportBean>) results.values;
                    notifyDataSetChanged();
                    //防止出现重影
                    mPullToRefreshListView.postInvalidate();
                    if (results.count == 0 && mEmptyLayout != null) {
                        mEmptyLayout.showEmpty();
                    }
                }
            };
        }

        class ViewHolder {
            TextView WorkDailyDate;
            TextView WorkDailySummary;
            TextView WorkDailyStatus;
            TextView WorkDailyDelete;
        }
    }
}
