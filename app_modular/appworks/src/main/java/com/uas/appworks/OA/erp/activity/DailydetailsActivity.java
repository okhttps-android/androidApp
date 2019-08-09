package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.Approval;
import com.core.model.EmployeesEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerScrollView;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.lidroid.xutils.ViewUtils;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.OA.erp.adapter.DailyDetailsApprovalFlowAdapter;
import com.uas.appworks.OA.erp.model.CommonApprovalFlowBean;
import com.uas.appworks.OA.erp.model.WorkReportBean;
import com.uas.appworks.OA.erp.utils.approvautils.ApprovaNodeUtil;
import com.uas.appworks.OA.erp.utils.approvautils.NodeAdapter;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2016/11/1.
 * 日报详情页面
 */
public class DailydetailsActivity extends BaseActivity {
    private static final int DELETE_DOC_REQUEST_SUCCESSFULLY = 1207;
    private static final int DAILY_REFORSUB_SUCCESSFULLY = 1209;
    private static final int DAILY_REFORDELETE_SUCCESSFULLY = 1214;
    public static final int DELETE_SUCCEED = 1219;
    private static final int REFORDELETE_DOC_REQUEST_SUCCESSFULLY = 1220;
    private static final int PLAT_DELETE_DAILY = 308;
    private final static int APPROVAL_FLOW_NODE = 102;
    private final int SINGLE_WORK_REPORT = 0x11;

    private TextView wdd_data;
    private TextView wdd_status;
    private TextView wdd_summary;
    private TextView wdd_plan;
    private TextView wdd_experience;
    private String date;
    private String summary;
    private String plan;
    private String experience;
    private String wd_status;
    private MyListView approval_flowlv;
    private LinearLayout plan_ll;
    private LinearLayout experience_ll;
    private DailyDetailsApprovalFlowAdapter mDailyDetailsApprovalFlowAdapter;
    private CommonApprovalFlowBean mCommonApprovalFlowBean;

    private EmptyLayout mEmptyLayput_approvalflow;
    private int mkeyValue;
    private String mCaller = "WorkDaily";
    private int mNoc = 1;
    private DBManager manager;
    private List<String> im_ids;
    private List<String> afpeople_names;
    private ImageView unsubmit_iv;
    private LinearLayout resanddel_ll;
    private LinearLayout resubmit_ll;
    private LinearLayout delete_ll;
    private String fromwhere;
    private WebSettings done_settings;
    private String submittype;
    private TextView resubmit_tv;
    private LinearLayout done_task_ll;
    private TextView done_task_tv;
    private LinearLayout undo_task_ll;
    private TextView undo_task_tv;
    private String donetask;
    private String em_code;
    private int do_pro_num;
    private String af_name;
    private WebView donetask_wv;
    private WebView undotask_wv;
    private String undotask;
    private WebSettings undo_settings;
    private View hide_above_af;
    private ImageView photo_im;
    private TextView name_tv;
    private TextView section_tv;
    private CustomerScrollView csv_hide;
    private RelativeLayout common_docdata_rl;
    private String em_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
    private int mReportType = Constants.WORK_REPORT_DAY;
    private String mReportDate = "";
    private String mReportWeekdays = "";
    private String mReportSerial = "";
    private String mReportStartTime = "";
    private String mReportEndTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initClickEvent();
    }

    private void initClickEvent() {

        resubmit_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isNetWorkConnected(DailydetailsActivity.this)) {
                    if (!TextUtils.isEmpty(submittype) && submittype.equals("unsubmit")) {//未提交状态
                        Intent intent = new Intent(activity, WorkReportAddActivity.class);
                        intent.putExtra("caller", mCaller);
                        intent.putExtra("id", mkeyValue);
                        intent.putExtra("rs_summary", summary);
                        intent.putExtra("rs_plan", plan);
                        intent.putExtra("rs_experience", experience);
                        intent.putExtra("rs_donetask", donetask);
                        intent.putExtra("rs_undotask", undotask);
                        intent.putExtra("resubmit", "resubmit");
                        intent.putExtra("report_type", mReportType);
                        intent.putExtra("report_date", mReportDate);
                        if (mReportType == Constants.WORK_REPORT_DAY) {
                            intent.putExtra("report_weekdays", mReportWeekdays);
                        } else if (mReportType == Constants.WORK_REPORT_WEEK || mReportType == Constants.WORK_REPORT_MONTH) {
                            intent.putExtra("report_serial", mReportSerial);
                            intent.putExtra("report_startTime", mReportStartTime);
                            intent.putExtra("report_endTime", mReportEndTime);
                        }
                        startActivity(intent);
                        Log.i("dosubmit_id", mkeyValue + "");
                        finish();
                    } else {                                                             //提交状态
                        PopupWindowHelper.showAlart(DailydetailsActivity.this,
                                getString(R.string.common_notice), getString(R.string.daily_resubmit_notice1),
                                new PopupWindowHelper.OnSelectListener() {
                                    @Override
                                    public void select(boolean selectOk) {
                                        if (selectOk) {
                                            String retype = "reforsub";
                                            doresubmit(retype);
                                        }
                                    }
                                });
                    }
                } else {
                    ToastUtil.showToast(DailydetailsActivity.this, R.string.networks_out);
                }

            }
        });

        delete_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isNetWorkConnected(DailydetailsActivity.this)) {
                    if (!TextUtils.isEmpty(submittype) && submittype.equals("unsubmit")) {
                        PopupWindowHelper.showAlart(DailydetailsActivity.this,
                                getString(R.string.common_notice), getString(R.string.delete_notice1),
                                new PopupWindowHelper.OnSelectListener() {
                                    @Override
                                    public void select(boolean selectOk) {
                                        if (selectOk) {
                                            dodelete(mkeyValue);
                                        }
                                    }
                                });

                    } else {
                        PopupWindowHelper.showAlart(DailydetailsActivity.this,
                                getString(R.string.common_notice), getString(R.string.delete_sumited_notice1),
                                new PopupWindowHelper.OnSelectListener() {
                                    @Override
                                    public void select(boolean selectOk) {
                                        if (selectOk) {
                                            String retype = "refordelete";
                                            doresubmit(retype);
                                        }
                                    }
                                });
                    }
                } else {
                    ToastUtil.showToast(DailydetailsActivity.this, R.string.networks_out);
                }


            }
        });

        wdd_summary.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SystemUtil.copyText(getApplicationContext(), wdd_summary.getText().toString());
                return true;
            }
        });

        wdd_plan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SystemUtil.copyText(getApplicationContext(), wdd_plan.getText().toString());
                return true;
            }
        });

        wdd_experience.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SystemUtil.copyText(getApplicationContext(), wdd_experience.getText().toString());
                return true;
            }
        });
    }


    public void initView() {

        setContentView(R.layout.activity_workdaily_detail);
        ViewUtils.inject(this);

        //findid
        wdd_data = (TextView) findViewById(R.id.work_daily_detail_time_tv);
        wdd_data.setText("");
        wdd_status = (TextView) findViewById(R.id.work_daily_detail_status_tv);
        wdd_summary = (TextView) findViewById(R.id.work_daily_detail_summary_tv);
        wdd_summary.setText("");
        wdd_plan = (TextView) findViewById(R.id.work_daily_detail_plan_tv);
        wdd_plan.setText("");
        wdd_experience = (TextView) findViewById(R.id.work_daily_detail_experience_tv);
        wdd_experience.setText("");
        approval_flowlv = (MyListView) findViewById(R.id.work_daily_detail_approval_flow_lv);
        plan_ll = (LinearLayout) findViewById(R.id.work_daily_detail_plan_ll);
        experience_ll = (LinearLayout) findViewById(R.id.work_daily_detail_experience_ll);
        unsubmit_iv = (ImageView) findViewById(R.id.work_daily_unsubmit_iv);
        resanddel_ll = (LinearLayout) findViewById(R.id.item_common_docui_res_and_del_ll);
        resubmit_ll = (LinearLayout) findViewById(R.id.common_docui_resubmit_ll);
        delete_ll = (LinearLayout) findViewById(R.id.common_docui_delete_ll);
        resubmit_tv = (TextView) findViewById(R.id.common_docui_resubmit_tv);
        done_task_ll = (LinearLayout) findViewById(R.id.work_daily_detail_donetask_ll);
        undo_task_ll = (LinearLayout) findViewById(R.id.work_daily_detail_undotask_ll);
        donetask_wv = (WebView) findViewById(R.id.donetask_wv);
        undotask_wv = (WebView) findViewById(R.id.undotask_wv);
        hide_above_af = findViewById(R.id.hide_above_af);
        photo_im = (ImageView) findViewById(R.id.common_docui_photo_img);
        name_tv = (TextView) findViewById(R.id.common_docui_name_tv);
        section_tv = (TextView) findViewById(R.id.common_docui_Section_tv);
        csv_hide = (CustomerScrollView) findViewById(R.id.csv_hide);
        common_docdata_rl = (RelativeLayout) findViewById(R.id.common_docdata_rl);


        done_settings = donetask_wv.getSettings();
        done_settings.setSupportZoom(true);
        done_settings.setTextSize(WebSettings.TextSize.SMALLER);//已完成任务设置web字体大小

        undo_settings = undotask_wv.getSettings();
        undo_settings.setSupportZoom(true);
        undo_settings.setTextSize(WebSettings.TextSize.SMALLER); //未完成任务设置web字体大小

        mEmptyLayput_approvalflow = new EmptyLayout(this, approval_flowlv);
        mEmptyLayput_approvalflow.setShowEmptyButton(false);
        mEmptyLayput_approvalflow.setShowErrorButton(false);
        mEmptyLayput_approvalflow.setShowLoadingButton(false);
        //加载审批流的适配器
        mCommonApprovalFlowBean = new CommonApprovalFlowBean();
        mDailyDetailsApprovalFlowAdapter = new DailyDetailsApprovalFlowAdapter(this);

        manager = new DBManager();
        im_ids = new ArrayList<>();
        afpeople_names = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            mReportType = intent.getIntExtra("report_type", Constants.WORK_REPORT_DAY);
            mCaller = intent.getStringExtra("caller");
        }
        if (mReportType == Constants.WORK_REPORT_DAY) {
            setTitle(getString(R.string.daily_detaily));
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            setTitle(R.string.weekly_report_detail);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            setTitle(R.string.monthly_report_detail);
        }

    }

    private boolean isNodeSuccess = false, isReportSuccess = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case APPROVAL_FLOW_NODE:
                    isNodeSuccess = true;
                    String wdaf_result = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("wdaf_result", wdaf_result);
                    handlerNode(wdaf_result);
//                    WDAFshow(wdaf_result);
                    break;
                case SINGLE_WORK_REPORT:
                    isReportSuccess = true;
                    String reportResult = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("reportResult", reportResult);
                    JSONObject resultObject = JSON.parseObject(reportResult);
                    JSONArray dataArray = resultObject.getJSONArray("data");
                    if (dataArray != null && dataArray.size() > 0) {
                        initDetailNet(dataArray);
                    } else {
                        initDetailLocal();
                    }

                    if (isNodeSuccess) {
                        progressDialog.dismiss();
                    }

                    break;
                case DAILY_REFORSUB_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String resubmit_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("RESUBMITTED_message", resubmit_result);
                            String unsubmitResult = getResources().getString(R.string.report_unsubmit_result);
                            String unsubmitSuccess = "";
                            if (mReportType == Constants.WORK_REPORT_DAY) {
                                unsubmitSuccess = String.format(unsubmitResult, "日", "成功");
                            } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                unsubmitSuccess = String.format(unsubmitResult, "周", "成功");
                            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                unsubmitSuccess = String.format(unsubmitResult, "月", "成功");
                            }
                            Toast.makeText(ct, unsubmitSuccess, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(activity, WorkReportAddActivity.class);
                            intent.putExtra("caller", mCaller);
                            intent.putExtra("id", mkeyValue);
                            intent.putExtra("rs_summary", summary);
                            intent.putExtra("rs_plan", plan);
                            intent.putExtra("rs_experience", experience);
                            intent.putExtra("rs_donetask", donetask);
                            intent.putExtra("rs_undotask", undotask);
                            intent.putExtra("resubmit", "resubmit");
                            intent.putExtra("report_type", mReportType);
                            intent.putExtra("report_date", mReportDate);
                            if (mReportType == Constants.WORK_REPORT_DAY) {
                                intent.putExtra("report_weekdays", mReportWeekdays);
                            } else if (mReportType == Constants.WORK_REPORT_WEEK || mReportType == Constants.WORK_REPORT_MONTH) {
                                intent.putExtra("report_serial", mReportSerial);
                                intent.putExtra("report_startTime", mReportStartTime);
                                intent.putExtra("report_endTime", mReportEndTime);
                            }
                            startActivity(intent);
                            finish();
                        }
                    }
                    break;
                case DAILY_REFORDELETE_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String resfordelete_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("RESUBMITTED_message", resfordelete_result);
                            dodelete(mkeyValue);
                        }
                    }
                    break;
                case REFORDELETE_DOC_REQUEST_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String delete_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("refordelete_result", delete_result);
                            if (JSON.parseObject(delete_result).containsKey("success") && JSON.parseObject(delete_result).getBoolean("success")) {
                                dodelete(mkeyValue);
                            }
                        }
                    }
                    break;
                case DELETE_DOC_REQUEST_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String delete_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("delete_result", delete_result);
                            if (JSON.parseObject(delete_result).containsKey("success") && JSON.parseObject(delete_result).getBoolean("success")) {
                                String deleteResult = getResources().getString(R.string.report_delete_result);
                                String deleteSuccess = "";
                                if (mReportType == Constants.WORK_REPORT_DAY) {
                                    deleteSuccess = String.format(deleteResult, "日", "成功");
                                } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                    deleteSuccess = String.format(deleteResult, "周", "成功");
                                } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                    deleteSuccess = String.format(deleteResult, "月", "成功");
                                }
                                Toast.makeText(ct, deleteSuccess, Toast.LENGTH_LONG).show();
                                if (!TextUtils.isEmpty(fromwhere) && "submitdaily".equals(fromwhere)) {
                                    startActivity(new Intent(DailydetailsActivity.this, WorkDailyShowActivity.class)
                                            .putExtra("report_type", mReportType).putExtra("caller", mCaller));
                                    finish();
                                } else if (!TextUtils.isEmpty(fromwhere) && "dailylist".equals(fromwhere)) {
                                    Intent intent = new Intent();
                                    intent.putExtra("delete_succeed", "delete_succeed");
                                    intent.putExtra("deleted_id", mkeyValue);
                                    setResult(DELETE_SUCCEED, intent);
                                    progressDialog.dismiss();
                                    finish();
                                }
                            }
                            progressDialog.dismiss();
                        }
                    }
                    break;


                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };

    private void initDetailNet(JSONArray dataArray) {
        JSONObject listDataObject = dataArray.getJSONObject(0);
        WorkReportBean workReportBean = new WorkReportBean();
        if (mReportType == Constants.WORK_REPORT_DAY) {
            workReportBean = dailyReportBean(listDataObject);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            workReportBean = weeklyReportBean(listDataObject);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            workReportBean = monthlyReportBean(listDataObject);
        }

        date = workReportBean.getDate();
        summary = workReportBean.getComment();
        if (!TextUtils.isEmpty(workReportBean.getReportStatus())) {
            wd_status = workReportBean.getReportStatus();
        }
        if (!TextUtils.isEmpty(workReportBean.getPlan())) {
            plan = workReportBean.getPlan();
        } else {
            plan_ll.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(workReportBean.getExperience())) {
            experience = workReportBean.getExperience();
        } else {
            experience_ll.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(workReportBean.getContext())
                && !"null".equals(workReportBean.getContext())) {
            donetask = workReportBean.getContext();
            donetask_wv.loadDataWithBaseURL(null, donetask.toLowerCase(), "text/html", "utf-8", null);
        } else {
            done_task_ll.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(workReportBean.getUnfinishedTask())
                && !"null".equals(workReportBean.getUnfinishedTask())) {
            undotask = workReportBean.getUnfinishedTask();
            undotask_wv.loadDataWithBaseURL(null, undotask.toLowerCase(), "text/html", "utf-8", null);
        } else {
            undo_task_ll.setVisibility(View.GONE);
        }
        wdd_summary.setText(summary);
        if ("已审核".equals(wd_status)) {
            wdd_status.setTextColor(getResources().getColor(R.color.approval));
            wdd_status.setText(getString(R.string.status_approved));
            resanddel_ll.setVisibility(View.GONE);
        } else if ("已提交".equals(wd_status)) {
            wdd_status.setTextColor(getResources().getColor(R.color.no_approval));
            wdd_status.setText(getString(R.string.status_pending));
            resanddel_ll.setVisibility(View.VISIBLE);
        } else if ("在录入".equals(wd_status)) {
            wdd_status.setTextColor(getResources().getColor(R.color.done_approval));
            wdd_status.setText(getString(R.string.status_unsubmit));
            resanddel_ll.setVisibility(View.VISIBLE);
            submittype = "unsubmit";
            resubmit_tv.setText("提交");
        }

        wdd_plan.setText(plan);
        wdd_experience.setText(experience);

        String time = "";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            mReportDate = workReportBean.getDate();
            mReportWeekdays = workReportBean.getWeekDays();
            time = getDailyTimeStr(mReportDate, mReportWeekdays);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            mReportSerial = workReportBean.getSerial();
            mReportStartTime = workReportBean.getStartTime();
            mReportEndTime = workReportBean.getEndTime();
            if (TextUtils.isEmpty(mReportSerial)
                    || TextUtils.isEmpty(mReportStartTime)
                    || TextUtils.isEmpty(mReportEndTime)) {
                time = date;
            } else {
                time = getWeeklyTimeStr(mReportStartTime, mReportEndTime, mReportSerial);
            }
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            mReportSerial = workReportBean.getSerial();
            mReportStartTime = workReportBean.getStartTime();
            mReportEndTime = workReportBean.getEndTime();
            if (TextUtils.isEmpty(mReportSerial)
                    || TextUtils.isEmpty(mReportStartTime)
                    || TextUtils.isEmpty(mReportEndTime)) {
                time = date;
            } else {
                time = getMonthlyTimeStr(mReportStartTime);
            }
        }
        wdd_data.setText(time);
    }

    private void initDetailLocal() {
        Intent intent = getIntent();
        date = intent.getStringExtra("Date");
        summary = intent.getStringExtra("Content");
        if (!TextUtils.isEmpty(intent.getStringExtra("WD_Status"))) {
            wd_status = intent.getStringExtra("WD_Status");
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("Plan"))) {
            plan = intent.getStringExtra("Plan");
        } else {
            plan_ll.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("Experience"))) {
            experience = intent.getStringExtra("Experience");
        } else {
            experience_ll.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("Donetask"))
                && !"null".equals(intent.getStringExtra("Donetask"))) {
            donetask = intent.getStringExtra("Donetask");
//            done_task_tv.setText(Html.fromHtml(donetask)); 将html格式转化为普通文本
            donetask_wv.loadDataWithBaseURL(null, donetask.toLowerCase(), "text/html", "utf-8", null);
        } else {
            done_task_ll.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(intent.getStringExtra("Undotask"))
                && !"null".equals(intent.getStringExtra("Undotask"))) {
            undotask = intent.getStringExtra("Undotask");
            undotask_wv.loadDataWithBaseURL(null, undotask.toLowerCase(), "text/html", "utf-8", null);
        } else {
            undo_task_ll.setVisibility(View.GONE);
        }
        wdd_summary.setText(summary);
        if ("已审核".equals(wd_status)) {
            wdd_status.setTextColor(getResources().getColor(R.color.approval));
            wdd_status.setText(getString(R.string.status_approved));
            resanddel_ll.setVisibility(View.GONE);
        } else if ("已提交".equals(wd_status)) {
            wdd_status.setTextColor(getResources().getColor(R.color.no_approval));
            wdd_status.setText(getString(R.string.status_pending));
            resanddel_ll.setVisibility(View.VISIBLE);
        } else if ("在录入".equals(wd_status)) {
            wdd_status.setTextColor(getResources().getColor(R.color.done_approval));
            wdd_status.setText(getString(R.string.status_unsubmit));
            resanddel_ll.setVisibility(View.VISIBLE);
            submittype = "unsubmit";
            resubmit_tv.setText("提交");
        }

        wdd_plan.setText(plan);
        wdd_experience.setText(experience);

        String time = "";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            mReportDate = intent.getStringExtra("report_date");
            mReportWeekdays = intent.getStringExtra("report_weekdays");
            time = getDailyTimeStr(mReportDate, mReportWeekdays);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            mReportSerial = intent.getStringExtra("report_serial");
            mReportStartTime = intent.getStringExtra("report_startTime");
            mReportEndTime = intent.getStringExtra("report_endTime");
            if (TextUtils.isEmpty(mReportSerial)
                    || TextUtils.isEmpty(mReportStartTime)
                    || TextUtils.isEmpty(mReportEndTime)) {
                time = date;
            } else {
                time = getWeeklyTimeStr(mReportStartTime, mReportEndTime, mReportSerial);
            }
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            mReportSerial = intent.getStringExtra("report_serial");
            mReportStartTime = intent.getStringExtra("report_startTime");
            mReportEndTime = intent.getStringExtra("report_endTime");
            if (TextUtils.isEmpty(mReportSerial)
                    || TextUtils.isEmpty(mReportStartTime)
                    || TextUtils.isEmpty(mReportEndTime)) {
                time = date;
            } else {
                time = getMonthlyTimeStr(mReportStartTime);
            }
        }
        wdd_data.setText(time);
    }


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

    private void handlerNode(String message) {
        List<Approval> approvals = ApprovaNodeUtil.handlerNode(manager, message);
        int position = -1;
        if (approvals != null) {
            for (int i = approvals.size() - 1; i >= 0; i--) {
                if (position == -1 && approvals.get(i).getIdKey().contains("待审批")) {
                    position = i;
                }
                if (position != -1 && i < position) {
                    approvals.get(i).setIdKey("");
                    approvals.get(i).setValues("");
                }
            }
        }

        if (!ListUtils.isEmpty(approvals)) {
            NodeAdapter nodeAdapter = new NodeAdapter(this, approvals);
            approval_flowlv.setAdapter(nodeAdapter);
        } else {
            //TODO 为空时候
        }
        if (isReportSuccess) {
            progressDialog.dismiss();
        }
    }

    /**
     * 日报审批流获取并处理
     *
     * @param wdaf_result
     */
    private void WDAFshow(String wdaf_result) {
        try {
            JSONObject resultJsonObject = JSON.parseObject(wdaf_result);
            //JSONArray dailynodeArray = resultJsonObject.getJSONArray("nodes");
            if (resultJsonObject == null) {
                approval_flowlv.setVisibility(View.GONE);
                hide_above_af.setVisibility(View.GONE);
            } else {
                mCommonApprovalFlowBean = JSON.parseObject(resultJsonObject.toString(), CommonApprovalFlowBean.class);
                getapplypeomsg(mCommonApprovalFlowBean);
                em_code = new String();
                if (manager == null) manager = new DBManager(mContext);
                if ("已审核".equals(wd_status) && (mCommonApprovalFlowBean.getData().size() == 0 ||
                        mCommonApprovalFlowBean.getData().size() == mCommonApprovalFlowBean.getNodes().size())) {
                    for (int i = 0; i < mCommonApprovalFlowBean.getNodes().size(); i++) {
                        //取名字
                        if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManName())) {
                            afpeople_names.add(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManName());
                        } else {
                            afpeople_names.add("");
                        }
                        //取头像id
                        if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManId())) {
                            em_code = mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManId();
                            if (em_code.contains(",")) {
                                String str[] = em_code.split(",");
                                em_code = str[0];
//                                ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                            }
                        } else {
                            em_code = " ";
                        }
                        try {
                            String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
                            String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                            String selection = "em_code=? and whichsys=? ";
                            //获取数据库数据
                            EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                            if (bean != null) {
                                String imId = String.valueOf(bean.getEm_IMID());
                                Log.i("todo", "imId=" + imId);
                                im_ids.add(imId);
                            } else {
                                im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if ("已提交".equals(wd_status)) {
                    String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");

                    //已提交状态判断是否有变更处理人，所以得先去process中判断,好麻烦噢
                    if (!ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {
                        int processnum = mCommonApprovalFlowBean.getProcesss().size();
                        //取process数据
                        for (int i = 0; i < processnum; i++) {
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealMan())) {
                                em_code = mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealMan();
                                if (em_code.contains(",")) {
                                    String str[] = em_code.split(",");
                                    em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                                }
                                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                                String selection = "em_code=? and whichsys=? ";
                                try {
                                    EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                                    if (bean != null) {
                                        //获取数据库数据获得名字
                                        String imName = String.valueOf(bean.getEM_NAME());
                                        if (!StringUtil.isEmpty(imName)) {
                                            af_name = imName;
                                        }
                                        //从数据库数据获得imid
                                        String imId = String.valueOf(bean.getEm_IMID());
                                        Log.i("todo", "imId=" + imId);
                                        im_ids.add(imId);
                                    } else {
                                        im_ids.add("");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                im_ids.add("");
                            }

                            //获取process审批人姓名
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealManName())) {
                                afpeople_names.add(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealManName());
                            } else if (!TextUtils.isEmpty(af_name)) {
                                afpeople_names.add(af_name);
                            } else {
                                afpeople_names.add("");
                            }
                        }

                        //取data数据
                        for (int j = processnum; j < mCommonApprovalFlowBean.getData().size(); j++) {
                            //取process之后的审批人名字
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMANNAME())) {
                                afpeople_names.add(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMANNAME());
                            } else {
                                afpeople_names.add("");
                            }
                            //取process之后的imid
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMAN())) {
                                em_code = mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMAN();
                                if (em_code.contains(",")) {
                                    String str[] = em_code.split(",");
                                    em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                                }
                                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                                String selection = "em_code=? and whichsys=? ";
                                try {
                                    //获取数据库数据
                                    EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                                    if (bean != null) {
                                        String imId = String.valueOf(bean.getEm_IMID());
                                        Log.i("todo", "imId=" + imId);
                                        im_ids.add(imId);
                                    } else {
                                        im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                im_ids.add("");
                            }
                        }
                    }

                    // 标准版刚刚提交时无变更时，全部取data数据
                    if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {
                        for (int i = 0; i < mCommonApprovalFlowBean.getData().size(); i++) {
                            //取名字
                            if (manager == null) manager = new DBManager(mContext);
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMANNAME())) {
                                afpeople_names.add(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMANNAME());
                            } else {
                                afpeople_names.add("");
                            }
                            //取头像id
                            if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMAN())) {
                                em_code = mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMAN();
                                if (em_code.contains(",")) {
                                    String str[] = em_code.split(",");
                                    em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                                }
                            } else {
                                em_code = " ";
                            }
                            try {
                                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                                String selection = "em_code=? and whichsys=? ";
                                //获取数据库数据
                                EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                                if (bean != null) {
                                    String imId = String.valueOf(bean.getEm_IMID());
                                    Log.i("todo", "imId=" + imId);
                                    im_ids.add(imId);
                                } else {
                                    im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                } else if ("已审核".equals(wd_status) && mCommonApprovalFlowBean.getData().size() != 0
                        && mCommonApprovalFlowBean.getData().size() != mCommonApprovalFlowBean.getNodes().size()) {
                    approval_flowlv.setVisibility(View.GONE);
                }
                Log.i("last_afpeople_names", "空" + afpeople_names.toString());
                Log.i("last_imids", im_ids.toString());
                afpeople_names.clear();
                im_ids.clear();
                handlerNode(wdaf_result);
                mDailyDetailsApprovalFlowAdapter.setIm_ids(im_ids);
                mDailyDetailsApprovalFlowAdapter.setAfpeople_names(afpeople_names);
                // 设置数据之前先通过数据库获取到所有的imid保存到内存中
                mDailyDetailsApprovalFlowAdapter.setmCommonApprovalFlowBean(mCommonApprovalFlowBean);
                approval_flowlv.setAdapter(mDailyDetailsApprovalFlowAdapter);

                if (ListUtils.isEmpty(mCommonApprovalFlowBean.getData()) && ListUtils.isEmpty(mCommonApprovalFlowBean.getNodes())) {
                    hide_above_af.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressDialog.dismiss();
    }

    private void getapplypeomsg(CommonApprovalFlowBean mCommonApprovalFlowBean) {
        if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss())) {
            common_docdata_rl.setVisibility(View.VISIBLE);
            name_tv.setText(em_name + "");
            String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
            AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
            return;
        }
        String applypeo_name = mCommonApprovalFlowBean.getProcesss().get(0).getJp_launcherName();
        String applypeo_number = mCommonApprovalFlowBean.getProcesss().get(0).getJp_launcherId();

        name_tv.setText(applypeo_name);
        if (!applypeo_number.isEmpty()) {
            String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
            String[] selectionArgs = {applypeo_number == null ? "" : applypeo_number, whichsys};
            String selection = "em_code=? and whichsys=? ";

            try {
                //获取数据库数据
                EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                if (bean != null) {
                    String imId = String.valueOf(bean.getEm_IMID());
                    String imName = String.valueOf(bean.getEM_NAME());
                    String imDepartment = String.valueOf(bean.getEM_DEPART());
                    String imPosition = String.valueOf(bean.getEM_POSITION());

                    name_tv.setText(imName);
                    if (!StringUtil.isEmpty(imId)) {
                        AvatarHelper.getInstance().display(imId, photo_im, true, false);
                    } else {
                        String imageUri = "drawable://" + R.drawable.common_header_boy;
                        AvatarHelper.getInstance().display(imageUri, photo_im, true);
                    }//显示圆角图片
                    section_tv.setText(imDepartment + ">" + imPosition);

                    Log.i("aptodo", "imId=" + imId + "imName" + imName + "imDepartment" + imDepartment + "imPosition" + imPosition);
                } else {
//                    ToastMessage("单据申请人数据获取异常");
                    name_tv.setText(em_name + "");
                    String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                    AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        common_docdata_rl.setVisibility(View.VISIBLE);
    }


    // 删除
    private void dodelete(int mkeyValue) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/commondelete.action";
        Map<String, Object> formStoreMap = new HashMap<>();
        HashMap<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("id", mkeyValue);
        Log.i("dodeleted", mkeyValue + "");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        LogUtil.prinlnLongMsg("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, handler, headers, DELETE_DOC_REQUEST_SUCCESSFULLY, null, null, "get");
    }

    // 反提交之操作：清除审批流和更改单据状态
    private void doresubmit(String retype) {
        progressDialog.show();
        String resubmit_url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/commonres.action";//这个接口仅仅是反提交操作
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", mkeyValue);
        Log.i("resubmitid", mkeyValue + "");
        params.put("caller", mCaller);
        LogUtil.d(JSON.toJSONString(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        if (retype.equals("reforsub")) {  //反提交之重新提交请求
            ViewUtil.httpSendRequest(this, resubmit_url, params, handler, headers, DAILY_REFORSUB_SUCCESSFULLY, null, null, "post");
        } else if (retype.equals("refordelete")) { //反提交之删除请求
            ViewUtil.httpSendRequest(this, resubmit_url, params, handler, headers, REFORDELETE_DOC_REQUEST_SUCCESSFULLY, null, null, "post");
        }
    }

    public void initData() {
        // 获取、处理、赋值从日报列表界面或者添加日报界面点击跳转传来的数据
        final Intent intent = getIntent();
        mkeyValue = intent.getIntExtra("ID", -1);
        fromwhere = intent.getStringExtra("fromwhere");

        // 当获取到正确的单据caller和单据id时 请求获取审批流数据
        if (!TextUtils.isEmpty(mCaller) && mkeyValue != -1) {
            if (CommonUtil.isNetWorkConnected(this)) {
                progressDialog.show();
                getSingleWorkReports();
                getCurrentJnodes();
            } else {
                initDetailLocal();
            }
        } else {
            approval_flowlv.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
        }
        csv_hide.setVisibility(View.VISIBLE);
    }

    private void getSingleWorkReports() {
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/getsingleWorkReports.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", mCaller);
        param.put("id", mkeyValue);
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, SINGLE_WORK_REPORT, null, null, "post");
    }

    private void getCurrentJnodes() {
        String url = CommonUtil.getAppBaseUrl(this) + "common/getCurrentJnodes.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", mCaller);
        param.put("keyValue", mkeyValue);
        param.put("_noc", mNoc);
        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, APPROVAL_FLOW_NODE, null, null, "post");
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
            return date;
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
            return date;
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
            return date;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!TextUtils.isEmpty(fromwhere) && fromwhere.equals("submitdaily")) {
                startActivity(new Intent(activity, WorkDailyShowActivity.class)
                        .putExtra("fromwhere", "dodaily").putExtra("report_type", mReportType).putExtra("caller", mCaller));
                finish();
            } else {
                finish();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(fromwhere) && fromwhere.equals("submitdaily")) {
            startActivity(new Intent(activity, WorkDailyShowActivity.class)
                    .putExtra("fromwhere", "dodaily").putExtra("report_type", mReportType).putExtra("caller", mCaller));
            finish();
        } else {
            super.onBackPressed();
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.closeDB();
        }
    }
}
