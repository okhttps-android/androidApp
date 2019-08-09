package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.DrawableCenterTextView;
import com.core.widget.crouton.Crouton;
import com.core.widget.view.Activity.SelectActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.OA.erp.model.WorkReportBean;
import com.uas.appworks.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author RaoMeng
 * @describe 工作汇报新建页面
 * @date 2017/10/17 14:15
 */

public class WorkReportAddActivity extends BaseActivity implements View.OnClickListener {
    private final int WORD_RESTRICTION_NUMBER = 1000;
    private static final int DAILY_SUBMITTED_SUCCESSFULLY = 0x01;  //提交请求成功后返回
    private static final int CLEAR_AF_UPDATE_DOC_STATE = 0x02;
    private static final int DAILY_RESUBMITTED_SUCCESSFULLY = 0x03;  //反提交请求成功后返回
    private static final int LAST_SUBMIT_SUCCESSFULLY = 0x04;
    private static final int GRAB_JOB_CONTENT = 0x05;
    private static final int GET_MULTINODE_ASSIGNS = 0x06;
    private static final int SELECT_APPROVERS = 0x07;
    private static final int WORK_REPORT_LIST = 0x08;

    private TextView mReportTimeTv;
    private RadioGroup mReportTimeRg;
    private RadioButton mReportTimeBeforeRb;
    private RadioButton mReportTimePresentRb;
    private LinearLayout mOldScheduleLl;
    private TextView mOldScheduleTitleTv;
    private DrawableCenterTextView mOldScheduleEmptyTv;
    private TextView mOldScheduleContentTv;
    private TextView mSummaryTitleTv;
    private TextView mSummaryErrorTv;
    private ImageView mSummaryVoiceIv;
    private FormEditText mSummaryContentEt;
    private TextView mNewScheduleTitleTv;
    private TextView mNewScheduleErrorTv;
    private ImageView mNewScheduleVoiceIv;
    private FormEditText mNewScheduleContentEt;
    private TextView mExperienceTitleTv;
    private TextView mExperienceErrorTv;
    private ImageView mExperienceVoiceIv;
    private ImageView mExperienceShareIv;
    private FormEditText mExperienceContentEt;
    private LinearLayout mNewScheduleLl, mDateLinearLayout;
    private Button mSubmitButton;

    private String fromqzone;
    private String resubmit;

    private String mFinishedTask;
    private String mUnfinishedTask;
    private String mNodeId;

    private String mExperienceText = "";
    private String mNewScheduleText = "";
    private String mSummaryText = "";

    private String mCaller = "WorkDaily";

    private int mReportType = Constants.WORK_REPORT_DAY;
    private int mkeyValue;
    private HttpClient mHttpClient;
    private boolean isPresentModify = false, isBeforeModify = false, isOptionEvent = false, isUpdateModify = false;

    private String mTodayDate, mYesterdayDate, mTodayWeek, mYesterdayWeek;
    private String mThisWeekStartDate, mThisWeekEndDate, mLastWeekStartDate, mLastWeekEndDate, mThisWeekSerial, mLastWeekSerial;
    private String mThisMonthStartDate, mThisMonthEndDate, mLastMonthStartDate, mLastMonthEndDate, mThisMonthSerial, mLastMonthSerial;
    private String mTodayTime = "", mYesterdayTime = "", mThisWeekTime = "", mLastWeekTime = "", mThisMonthTime = "", mLastMonthTime = "";
    private String mDailyReportDate = "", mDailyReportWeek = "", mWeekReportStartDate = "", mWeekReportEndDate = "", mMonthReportStartDate = "", mMonthReportEndDate = "", mWeekReportSerial = "", mMonthReportSerial = "";

    private String mUpdateDate = "", mUpdateWeekdays = "", mUpdateSerial = "", mUpdateStartTime = "", mUpdateEndTime = "", mUpdateSummary = "", mUpdateSchedule = "", mUpdateExperience = "";
    private boolean isOnlyUpdate = false, isPresentExist = false, isBeforeExist = false;
    private WorkReportBean mInitPresentWorkReportBean, mInitBeforeWorkReportBean;
    private String mPresentPlan = "", mBeforePlan = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case DAILY_SUBMITTED_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("submit_message", result);
                            if (JSON.parseObject(result).containsKey("success") && JSON.parseObject(result).getBoolean("success")) {
                                String submitSuccess = getResultStr(R.string.report_submit_result, "成功");
                                Toast.makeText(ct, submitSuccess, Toast.LENGTH_SHORT).show();
                                try {
                                    if (mReportType == Constants.WORK_REPORT_DAY) {
                                        mkeyValue = new JSONObject(result).getJSONArray("data").getJSONObject(0).getInt("WD_ID");
                                        mFinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_CONTEXT");
                                        mUnfinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_UNFINISHEDTASK");
                                    } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                        mkeyValue = new JSONObject(result).getJSONArray("data").getJSONObject(0).getInt("WW_ID");
                                        mFinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WW_CONTEXT");
                                        mUnfinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WW_UNFINISHEDTASK");
                                    } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                        mkeyValue = new JSONObject(result).getJSONArray("data").getJSONObject(0).getInt("WM_ID");
                                        mFinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WM_CONTEXT");
                                        mUnfinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WM_UNFINISHEDTASK");
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                doGrabJobContent(mkeyValue);
                            } else {
                                String submitfail = getResultStr(R.string.report_submit_result, "失败");
                                Crouton.makeText(ct, submitfail);
                                progressDialog.dismiss();
                                mSubmitButton.setEnabled(true);
                            }

                        }
                    }
                    break;
                case CLEAR_AF_UPDATE_DOC_STATE:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("updata_message", result);
                            if (JSON.parseObject(result).containsKey("success") && JSON.parseObject(result).getBoolean("success")) {
                                boolean isResubmit = (!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit")) ||
                                        (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub"));
                                if (isResubmit || (mReportTimePresentRb.isChecked() && isPresentExist)
                                        || (mReportTimeBeforeRb.isChecked() && isBeforeExist)) {
                                    String updateSuccess = getResultStr(R.string.report_update_result, "成功");
                                    ToastMessage(updateSuccess);
                                    if (isOnlyUpdate) {
                                        progressDialog.dismiss();
                                        if (isResubmit || isOptionEvent) {
                                            Intent intent1 = new Intent(WorkReportAddActivity.this, WorkDailyShowActivity.class);
                                            intent1.putExtra("caller", mCaller);
                                            intent1.putExtra("report_type", mReportType);
                                            if (isOptionEvent) {
                                                intent1.putExtra("fromwhere", "nosubmitdaily");
                                            }
                                            startActivityForResult(intent1, WORK_REPORT_LIST);
                                        }
                                        if (!isOptionEvent) {
                                            finish();
                                        }
                                    } else {
                                        submitWorkReport();
                                    }
                                }
                            }
                        }
                    }
                    break;

                case LAST_SUBMIT_SUCCESSFULLY:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("LAST_SUBMIT_message", result);
                            if (JSON.parseObject(result).containsKey("success") && JSON.parseObject(result).getBoolean("success")) {
                                String resubmitSuccess = getResultStr(R.string.report_resubmit_result, "成功");
                                Toast.makeText(ct, resubmitSuccess, Toast.LENGTH_SHORT).show();
                                try {
                                    if (mReportType == Constants.WORK_REPORT_DAY) {
                                        mFinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_CONTEXT");
                                        mUnfinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WD_UNFINISHEDTASK");
                                    } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                        mFinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WW_CONTEXT");
                                        mUnfinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WW_UNFINISHEDTASK");
                                    } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                        mFinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WM_CONTEXT");
                                        mUnfinishedTask = new JSONObject(result).getJSONArray("data").getJSONObject(0).getString("WM_UNFINISHEDTASK");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                doGrabJobContent(mkeyValue);
                            } else {
                                String resubmitfail = getResultStr(R.string.report_resubmit_result, "失败");
                                Crouton.makeText(ct, resubmitfail);
                                progressDialog.dismiss();
                                mSubmitButton.setEnabled(true);
                            }
                        }
                    }
                    break;
                case GRAB_JOB_CONTENT:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("GRAB_JOB_CONTENT", result);
                            if (!TextUtils.isEmpty(mFinishedTask) || !TextUtils.isEmpty(mUnfinishedTask)) {
                                ToastMessage(getString(R.string.workcontext_grap_success));
                            }
                            judgeApprovers(mkeyValue);

                        }
                    }
                    break;

                case GET_MULTINODE_ASSIGNS:
                    String message = bundle.getString("result");
                    if (!StringUtil.isEmpty(message) && JSONUtil.validate(message)) {
                        com.alibaba.fastjson.JSONObject object = JSON.parseObject(message);
                        if (object.containsKey("assigns")) {
                            JSONArray array = JSON.parseObject(message).getJSONArray("assigns");
                            com.alibaba.fastjson.JSONObject o = array.getJSONObject(0);
                            String noid = "";
                            if (o != null && o.containsKey("JP_NODEID")) {
                                noid = o.getString("JP_NODEID");
                            }
                            JSONArray data = null;
                            if (o != null && o.containsKey("JP_CANDIDATES")) {
                                data = o.getJSONArray("JP_CANDIDATES");
                            }
                            if (!StringUtil.isEmpty(noid) && data != null && data.size() > 0) {
                                sendToSelect(noid, data);
                            } else {
                                progressDialog.dismiss();
                                jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                            }

                        } else {
                            progressDialog.dismiss();
                            jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                        }
                    } else {
                        progressDialog.dismiss();
                        jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                    }
                    break;
                case SELECT_APPROVERS:
//                    progressDialog.dismiss();
                    jumptododetail(mkeyValue); //延时跳转，确认抓取成功
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                            progressDialog.dismiss();
                            mSubmitButton.setEnabled(true);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_report_add);
        Intent intent = getIntent();
        if (intent != null) {
            mReportType = intent.getIntExtra("report_type", Constants.WORK_REPORT_DAY);

            resubmit = intent.getStringExtra("resubmit");
            mkeyValue = intent.getIntExtra("id", 0);
        }
        initViews();
        initEvents();
        initDatas(intent);
    }

    private void initDatas(Intent intent) {
        // 判断有没从列表界面、详情界面返回的重新提交的数据
        if (!TextUtils.isEmpty(resubmit)) {
            mDateLinearLayout.setVisibility(View.VISIBLE);
            mReportTimeRg.setVisibility(View.GONE);
            mOldScheduleLl.setVisibility(View.GONE);
            if ("unsub_tosub".equals(resubmit)) {
                mSubmitButton.setText(getString(R.string.common_submit_button));
            } else if ("resubmit".equals(resubmit)) {
                mSubmitButton.setText(getString(R.string.common_resubmit_button));
            }
            mUpdateSummary = intent.getStringExtra("rs_summary");
            if (TextUtils.isEmpty(mUpdateSummary)) {
                mUpdateSummary = "";
            }
            mSummaryContentEt.setText(mUpdateSummary);

            mUpdateSchedule = intent.getStringExtra("rs_plan");
            if (TextUtils.isEmpty(mUpdateSchedule)) {
                mUpdateSchedule = "";
            }
            mNewScheduleContentEt.setText(mUpdateSchedule);

            mUpdateExperience = intent.getStringExtra("rs_experience");
            if (TextUtils.isEmpty(mUpdateExperience)) {
                mUpdateExperience = "";
            }
            mExperienceContentEt.setText(mUpdateExperience);

            if (!TextUtils.isEmpty(intent.getStringExtra("rs_donetask"))) {
                mFinishedTask = intent.getStringExtra("rs_donetask");
            }
            if (!TextUtils.isEmpty(intent.getStringExtra("rs_undotask"))) {
                mUnfinishedTask = intent.getStringExtra("rs_undotask");
            }

            String time = "";
            mUpdateDate = intent.getStringExtra("report_date");
            if (mReportType == Constants.WORK_REPORT_DAY) {
                mUpdateWeekdays = intent.getStringExtra("report_weekdays");
                time = getDailyTimeStr(mUpdateDate, mUpdateWeekdays);
            } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                mUpdateSerial = intent.getStringExtra("report_serial");
                mUpdateStartTime = intent.getStringExtra("report_startTime");
                mUpdateEndTime = intent.getStringExtra("report_endTime");
                if (TextUtils.isEmpty(mUpdateSerial)
                        || TextUtils.isEmpty(mUpdateStartTime)
                        || TextUtils.isEmpty(mUpdateEndTime)) {
                    time = mUpdateDate;
                } else {
                    time = getWeeklyTimeStr(mUpdateStartTime, mUpdateEndTime);
                }
            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                mUpdateSerial = intent.getStringExtra("report_serial");
                mUpdateStartTime = intent.getStringExtra("report_startTime");
                mUpdateEndTime = intent.getStringExtra("report_endTime");
                if (TextUtils.isEmpty(mUpdateSerial)
                        || TextUtils.isEmpty(mUpdateStartTime)
                        || TextUtils.isEmpty(mUpdateEndTime)) {
                    time = mUpdateDate;
                } else {
                    time = getMonthlyTimeStr(mUpdateStartTime);
                }
            }
            mReportTimeTv.setText(time);
        } else {
            mDateLinearLayout.setVisibility(View.GONE);
            mReportTimeRg.setVisibility(View.VISIBLE);
            mOldScheduleLl.setVisibility(View.VISIBLE);
            if (CommonUtil.isNetWorkConnected(this)) {
                getWorkReportInit();
            } else {
                ToastUtil.showToast(this, R.string.networks_out);
            }
        }


    }

    private void initEvents() {
        mExperienceVoiceIv.setOnClickListener(this);
        mSummaryVoiceIv.setOnClickListener(this);
        mNewScheduleVoiceIv.setOnClickListener(this);
        mExperienceShareIv.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);
        mReportTimeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.work_report_add_time_before_rb) {
                    beforeCheckedEvent();
                } else if (checkedId == R.id.work_report_add_time_present_rb) {
                    presentCheckedEvent();
                }
            }
        });


        // 对输入的三项内容进行动态监听限制字数，只提醒，不限制输入
        mSummaryContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSummaryContentEt.getText().toString().length() > WORD_RESTRICTION_NUMBER) {
                    mSummaryErrorTv.setVisibility(View.VISIBLE);
                } else {
                    mSummaryErrorTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSummaryText = mSummaryContentEt.getText().toString();
                if (isPresentExist && mReportTimePresentRb.isChecked()
                        && !mSummaryText.equals(mInitPresentWorkReportBean.getComment())) {
                    isPresentModify = true;
                } else {
                    isPresentModify = false;
                }
                if (isBeforeExist && mReportTimeBeforeRb.isChecked()
                        && !mSummaryText.equals(mInitBeforeWorkReportBean.getComment())) {
                    isBeforeModify = true;
                } else {
                    isBeforeModify = false;
                }
                if (!TextUtils.isEmpty(resubmit)) {
                    if (!mSummaryText.equals(mUpdateSummary)) {
                        isUpdateModify = true;
                    } else {
                        isUpdateModify = false;
                    }
                }

            }
        });

        mNewScheduleContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mNewScheduleContentEt.getText().toString().length() > WORD_RESTRICTION_NUMBER) {
                    mNewScheduleErrorTv.setVisibility(View.VISIBLE);
                } else {
                    mNewScheduleErrorTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNewScheduleText = mNewScheduleContentEt.getText().toString();
                if (isPresentExist && mReportTimePresentRb.isChecked()
                        && !mNewScheduleText.equals(mInitPresentWorkReportBean.getPlan())) {
                    isPresentModify = true;
                } else {
                    isPresentModify = false;
                }
                if (isBeforeExist && mReportTimeBeforeRb.isChecked()
                        && !mNewScheduleText.equals(mInitBeforeWorkReportBean.getPlan())) {
                    isBeforeModify = true;
                } else {
                    isBeforeModify = false;
                }
                if (!TextUtils.isEmpty(resubmit)) {
                    if (!mNewScheduleText.equals(mUpdateSchedule)) {
                        isUpdateModify = true;
                    } else {
                        isUpdateModify = false;
                    }
                }
            }
        });

        mExperienceContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mExperienceContentEt.getText().toString().length() > WORD_RESTRICTION_NUMBER) {
                    mExperienceErrorTv.setVisibility(View.VISIBLE);
                } else {
                    mExperienceErrorTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mExperienceText = mExperienceContentEt.getText().toString();
                if (isPresentExist && mReportTimePresentRb.isChecked()
                        && !mExperienceText.equals(mInitPresentWorkReportBean.getExperience())) {
                    isPresentModify = true;
                } else {
                    isPresentModify = false;
                }
                if (isBeforeExist && mReportTimeBeforeRb.isChecked()
                        && !mExperienceText.equals(mInitBeforeWorkReportBean.getExperience())) {
                    isBeforeModify = true;
                } else {
                    isBeforeModify = false;
                }
                if (!TextUtils.isEmpty(resubmit)) {
                    if (!mExperienceText.equals(mUpdateExperience)) {
                        isUpdateModify = true;
                    } else {
                        isUpdateModify = false;
                    }
                }
            }
        });
    }

    private String getResultStr(int resultResource, String result) {
        String submitResult = getResources().getString(resultResource);
        String resultStr = "";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            resultStr = String.format(submitResult, "日", result);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            resultStr = String.format(submitResult, "周", result);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            resultStr = String.format(submitResult, "月", result);
        }
        return resultStr;
    }

    private void presentCheckedEvent() {
        if (mReportType == Constants.WORK_REPORT_DAY) {
            mDailyReportDate = mTodayDate;
            mDailyReportWeek = mTodayWeek;
            mReportTimeTv.setText(mTodayTime);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            mWeekReportSerial = mThisWeekSerial;
            mWeekReportStartDate = mThisWeekStartDate;
            mWeekReportEndDate = mThisWeekEndDate;
            mReportTimeTv.setText(mThisWeekTime);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            mMonthReportSerial = mThisMonthSerial;
            mMonthReportStartDate = mThisMonthStartDate;
            mMonthReportEndDate = mThisMonthEndDate;
            mReportTimeTv.setText(mThisMonthTime);
        }

        if (!TextUtils.isEmpty(mPresentPlan)) {
            mOldScheduleEmptyTv.setVisibility(View.GONE);
            mOldScheduleContentTv.setVisibility(View.VISIBLE);
            mOldScheduleContentTv.setText(mPresentPlan);
        } else {
            mOldScheduleEmptyTv.setVisibility(View.VISIBLE);
            mOldScheduleContentTv.setText("");
            mOldScheduleContentTv.setVisibility(View.GONE);
        }

        if (isPresentExist) {
            mSummaryContentEt.setText(mInitPresentWorkReportBean.getComment());
            mNewScheduleContentEt.setText(mInitPresentWorkReportBean.getPlan());
            mExperienceContentEt.setText(mInitPresentWorkReportBean.getExperience());

            mkeyValue = mInitPresentWorkReportBean.getReportId();
            mUnfinishedTask = mInitPresentWorkReportBean.getUnfinishedTask();
            mFinishedTask = mInitPresentWorkReportBean.getContext();

            mUpdateDate = mInitPresentWorkReportBean.getDate();
            mUpdateWeekdays = mTodayWeek;
            if (mReportType == Constants.WORK_REPORT_WEEK) {
                mUpdateSerial = mThisWeekSerial;
            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                mUpdateSerial = mThisMonthSerial;
            }
            mUpdateStartTime = mInitPresentWorkReportBean.getStartTime();
            mUpdateEndTime = mInitPresentWorkReportBean.getEndTime();
        } else {
            mSummaryContentEt.setText("");
            mNewScheduleContentEt.setText("");
            mExperienceContentEt.setText("");

            mkeyValue = 0;
            mUnfinishedTask = "";
            mFinishedTask = "";

            mUpdateDate = "";
            mUpdateWeekdays = "";
            mUpdateSerial = "";
            mUpdateStartTime = "";
            mUpdateEndTime = "";
        }
    }

    private void beforeCheckedEvent() {
        if (mReportType == Constants.WORK_REPORT_DAY) {
            mDailyReportDate = mYesterdayDate;
            mDailyReportWeek = mYesterdayWeek;
            mReportTimeTv.setText(mYesterdayTime);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            mWeekReportSerial = mLastWeekSerial;
            mWeekReportStartDate = mLastWeekStartDate;
            mWeekReportEndDate = mLastWeekEndDate;
            mReportTimeTv.setText(mLastWeekTime);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            mMonthReportSerial = mLastMonthSerial;
            mMonthReportStartDate = mLastMonthStartDate;
            mMonthReportEndDate = mLastMonthEndDate;
            mReportTimeTv.setText(mLastMonthTime);
        }

        if (!TextUtils.isEmpty(mBeforePlan)) {
            mOldScheduleEmptyTv.setVisibility(View.GONE);
            mOldScheduleContentTv.setVisibility(View.VISIBLE);
            mOldScheduleContentTv.setText(mBeforePlan);
        } else {
            mOldScheduleEmptyTv.setVisibility(View.VISIBLE);
            mOldScheduleContentTv.setText("");
            mOldScheduleContentTv.setVisibility(View.GONE);
        }

        if (isBeforeExist) {
            mSummaryContentEt.setText(mInitBeforeWorkReportBean.getComment());
            mNewScheduleContentEt.setText(mInitBeforeWorkReportBean.getPlan());
            mExperienceContentEt.setText(mInitBeforeWorkReportBean.getExperience());

            mkeyValue = mInitBeforeWorkReportBean.getReportId();
            mUnfinishedTask = mInitBeforeWorkReportBean.getUnfinishedTask();
            mFinishedTask = mInitBeforeWorkReportBean.getContext();

            mUpdateDate = mInitBeforeWorkReportBean.getDate();
            mUpdateWeekdays = mYesterdayWeek;
            if (mReportType == Constants.WORK_REPORT_WEEK) {
                mUpdateSerial = mLastWeekSerial;
            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                mUpdateSerial = mLastMonthSerial;
            }
            mUpdateStartTime = mInitBeforeWorkReportBean.getStartTime();
            mUpdateEndTime = mInitBeforeWorkReportBean.getEndTime();
        } else {
            mSummaryContentEt.setText("");
            mNewScheduleContentEt.setText("");
            mExperienceContentEt.setText("");

            mkeyValue = 0;
            mUnfinishedTask = "";
            mFinishedTask = "";

            mUpdateDate = "";
            mUpdateWeekdays = "";
            mUpdateSerial = "";
            mUpdateStartTime = "";
            mUpdateEndTime = "";
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.work_report_add_submit_btn) {
            if (mSummaryContentEt.testValidity()) {
                if (mNewScheduleContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER &&
                        mExperienceContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER) {
                    saveAndSubmitReport();
                } else {
                    ToastUtil.showToast(this, "字数已超限！");
                }
            }
        } else if (i == R.id.work_report_add_experience_voice_iv) {
            CommonUtil.getVoiceText(this, mExperienceContentEt, null);
        } else if (i == R.id.work_report_add_summary_voice_iv) {
            CommonUtil.getVoiceText(this, mSummaryContentEt, null);
        } else if (i == R.id.work_report_add_new_schedule_voice_iv) {
            CommonUtil.getVoiceText(this, mNewScheduleContentEt, null);
        } else if (i == R.id.work_report_add_experience_share_iv) {
            if (StringUtil.isEmpty(mExperienceContentEt.getText().toString())) {
                ToastMessage(getString(R.string.share_experience_notice));
            } else {
                Intent intent = new Intent("com.modilar.circle.SendShuoshuoActivity");
                intent.putExtra("Experience", mExperienceContentEt.getText().toString());
                intent.putExtra("report_type", mReportType);
                intent.putExtra("type", 0);
                if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {
                    startActivity(intent);
                    finish();
                } else {
                    startActivity(intent);
                }
            }
        }

    }

    private void initViews() {
        mReportTimeTv = (TextView) findViewById(R.id.work_report_add_time_tv);
        mReportTimeRg = (RadioGroup) findViewById(R.id.work_report_add_time_rg);
        mReportTimeBeforeRb = (RadioButton) findViewById(R.id.work_report_add_time_before_rb);
        mReportTimePresentRb = (RadioButton) findViewById(R.id.work_report_add_time_present_rb);
        mOldScheduleLl = (LinearLayout) findViewById(R.id.work_report_add_old_schedule_ll);
        mOldScheduleTitleTv = (TextView) findViewById(R.id.work_report_add_old_schedule_title_tv);
        mOldScheduleEmptyTv = (DrawableCenterTextView) findViewById(R.id.work_report_add_old_schedule_empty_tv);
        mOldScheduleContentTv = (TextView) findViewById(R.id.work_report_add_old_schedule_content_tv);
        mSummaryTitleTv = (TextView) findViewById(R.id.work_report_add_summary_title_tv);
        mSummaryErrorTv = (TextView) findViewById(R.id.work_report_add_summary_error_tv);
        mSummaryVoiceIv = (ImageView) findViewById(R.id.work_report_add_summary_voice_iv);
        mSummaryContentEt = (FormEditText) findViewById(R.id.work_report_add_summary_content_et);
        mNewScheduleTitleTv = (TextView) findViewById(R.id.work_report_add_new_schedule_title_tv);
        mNewScheduleErrorTv = (TextView) findViewById(R.id.work_report_add_new_schedule_error_tv);
        mNewScheduleVoiceIv = (ImageView) findViewById(R.id.work_report_add_new_schedule_voice_iv);
        mNewScheduleContentEt = (FormEditText) findViewById(R.id.work_report_add_new_schedule_content_et);
        mExperienceTitleTv = (TextView) findViewById(R.id.work_report_add_experience_title_tv);
        mExperienceErrorTv = (TextView) findViewById(R.id.work_report_add_experience_error_tv);
        mExperienceVoiceIv = (ImageView) findViewById(R.id.work_report_add_experience_voice_iv);
        mExperienceShareIv = (ImageView) findViewById(R.id.work_report_add_experience_share_iv);
        mExperienceContentEt = (FormEditText) findViewById(R.id.work_report_add_experience_content_et);
        mNewScheduleLl = (LinearLayout) findViewById(R.id.work_report_add_new_schedule_ll);
        mDateLinearLayout = (LinearLayout) findViewById(R.id.work_report_add_date_ll);
        mSubmitButton = (Button) findViewById(R.id.work_report_add_submit_btn);

        mHttpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(this)).build();
        mInitPresentWorkReportBean = new WorkReportBean();
        mInitBeforeWorkReportBean = new WorkReportBean();

        if (mReportType == Constants.WORK_REPORT_DAY) {
            setTitle(R.string.oaworkdaily_title);
            mCaller = "WorkDaily";
            mReportTimeBeforeRb.setText(R.string.str_yesterday);
            mReportTimePresentRb.setText(R.string.str_today);
            mOldScheduleTitleTv.setText(R.string.work_schedule_today);
            mOldScheduleEmptyTv.setText(R.string.work_schedule_empty_yesterday);
            mSummaryTitleTv.setText(R.string.work_summary_today);
            mSummaryContentEt.setHint(R.string.please_input_work_summary_today);
            mNewScheduleTitleTv.setText(R.string.work_schedule_tomorrow);
            mNewScheduleContentEt.setHint(R.string.please_input_work_schedule_tomorrow);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            setTitle(R.string.oaworkweekly_title);
            mCaller = "WorkWeekly";
            mReportTimeBeforeRb.setText(R.string.str_last_week);
            mReportTimePresentRb.setText(R.string.str_this_week);
            mOldScheduleTitleTv.setText(R.string.work_schedule_this_week);
            mOldScheduleEmptyTv.setText(R.string.work_schedule_empty_last_week);
            mSummaryTitleTv.setText(R.string.work_summary_this_week);
            mSummaryContentEt.setHint(R.string.please_input_work_summary_this_week);
            mNewScheduleTitleTv.setText(R.string.work_schedule_next_week);
            mNewScheduleContentEt.setHint(R.string.please_input_work_schedule_next_week);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            setTitle(R.string.oaworkmonthly_title);
            mCaller = "WorkMonthly";
            mReportTimeBeforeRb.setText(R.string.str_last_month);
            mReportTimePresentRb.setText(R.string.str_this_month);
            mOldScheduleTitleTv.setText(R.string.work_schedule_this_mohth);
            mOldScheduleEmptyTv.setText(R.string.work_schedule_empty_last_month);
            mSummaryTitleTv.setText(R.string.work_summary_this_month);
            mSummaryContentEt.setHint(R.string.please_input_work_summary_this_month);
            mNewScheduleTitleTv.setText(R.string.work_schedule_next_month);
            mNewScheduleContentEt.setHint(R.string.please_input_work_schedule_next_month);
        }
    }

    /**
     * 获取工作汇报（日报、周报、月报）的初始化数据
     */
    private void getWorkReportInit() {
        progressDialog.show();
        mHttpClient.Api().send(new HttpClient.Builder()
                        .url("mobile/getWorkReportInit.action")
                        .add("emcode", CommonUtil.getSharedPreferences(this, "erp_username"))
                        .add("caller", mCaller)
                        .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"))
                        .method(Method.POST).build()
                , new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        try {
                            String result = o.toString();
                            LogUtil.prinlnLongMsg("workreportinit", result);
                            com.alibaba.fastjson.JSONObject resultObject = JSON.parseObject(result);
                            com.alibaba.fastjson.JSONObject dataObject = resultObject.getJSONObject("data");
                            if (dataObject != null) {
                                mDateLinearLayout.setVisibility(View.VISIBLE);
                                if (mReportType == Constants.WORK_REPORT_DAY) {
                                    initDailyReport(dataObject);
                                } else if (mReportType == Constants.WORK_REPORT_WEEK) {
                                    initWeeklyReport(dataObject);
                                } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                                    initMonthlyReport(dataObject);
                                }
                                if (mReportTimePresentRb.isChecked()) {
                                    if (!TextUtils.isEmpty(mPresentPlan)) {
                                        mOldScheduleEmptyTv.setVisibility(View.GONE);
                                        mOldScheduleContentTv.setVisibility(View.VISIBLE);
                                        mOldScheduleContentTv.setText(mPresentPlan);
                                    } else {
                                        mOldScheduleEmptyTv.setVisibility(View.VISIBLE);
                                        mOldScheduleContentTv.setText("");
                                        mOldScheduleContentTv.setVisibility(View.GONE);
                                    }
                                } else if (mReportTimeBeforeRb.isChecked()) {
                                    if (!TextUtils.isEmpty(mBeforePlan)) {
                                        mOldScheduleEmptyTv.setVisibility(View.GONE);
                                        mOldScheduleContentTv.setVisibility(View.VISIBLE);
                                        mOldScheduleContentTv.setText(mBeforePlan);
                                    } else {
                                        mOldScheduleEmptyTv.setVisibility(View.VISIBLE);
                                        mOldScheduleContentTv.setText("");
                                        mOldScheduleContentTv.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                progressDialog.dismiss();
                                mDateLinearLayout.setVisibility(View.GONE);
                                ToastUtil.showToast(WorkReportAddActivity.this, "工作汇报初始数据获取异常");
                            }
                            initReportMsg(resultObject);

                        } catch (Exception e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            mDateLinearLayout.setVisibility(View.GONE);
                            ToastUtil.showToast(WorkReportAddActivity.this, "工作汇报初始数据获取异常");
                        }
                    }

                    @Override
                    public void onFailure(Object t) {
                        progressDialog.dismiss();
                        mDateLinearLayout.setVisibility(View.GONE);
                        ToastUtil.showToast(WorkReportAddActivity.this, "工作汇报初始数据获取异常");
                    }
                }));
    }

    private void initReportMsg(com.alibaba.fastjson.JSONObject resultObject) {
        JSONArray todayDataArray = resultObject.getJSONArray("todayData");
        JSONArray yesterdayDataArray = resultObject.getJSONArray("YesterdayData");
        com.alibaba.fastjson.JSONObject todayDataObject = null;
        com.alibaba.fastjson.JSONObject yesterdayDataObject = null;
        if (todayDataArray != null && todayDataArray.size() != 0) {
            todayDataObject = todayDataArray.getJSONObject(0);
        }
        if (yesterdayDataArray != null && yesterdayDataArray.size() != 0) {
            yesterdayDataObject = yesterdayDataArray.getJSONObject(0);
        }

        if (mReportType == Constants.WORK_REPORT_DAY) {
            if (todayDataObject != null) {
                initDailyReport(mInitPresentWorkReportBean, todayDataObject);
            }
            if (yesterdayDataObject != null) {
                initDailyReport(mInitBeforeWorkReportBean, yesterdayDataObject);
            }
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            if (todayDataObject != null) {
                initWeeklyReport(mInitPresentWorkReportBean, todayDataObject);
            }
            if (yesterdayDataObject != null) {
                initWeeklyReport(mInitBeforeWorkReportBean, yesterdayDataObject);
            }
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            if (todayDataObject != null) {
                initMonthlyReport(mInitPresentWorkReportBean, todayDataObject);
            }
            if (yesterdayDataObject != null) {
                initMonthlyReport(mInitBeforeWorkReportBean, yesterdayDataObject);
            }
        }
        if ("在录入".equals(mInitPresentWorkReportBean.getReportStatus())) {
            isPresentExist = true;
        } else {
            isPresentExist = false;
        }

        if ("在录入".equals(mInitBeforeWorkReportBean.getReportStatus())) {
            isBeforeExist = true;
        } else {
            isBeforeExist = false;
        }

        if (mReportTimePresentRb.isChecked() && isPresentExist) {
            mSummaryContentEt.setText(mInitPresentWorkReportBean.getComment());
            mNewScheduleContentEt.setText(mInitPresentWorkReportBean.getPlan());
            mExperienceContentEt.setText(mInitPresentWorkReportBean.getExperience());

            mkeyValue = mInitPresentWorkReportBean.getReportId();
            mUnfinishedTask = mInitPresentWorkReportBean.getUnfinishedTask();
            mFinishedTask = mInitPresentWorkReportBean.getContext();

            mUpdateDate = mInitPresentWorkReportBean.getDate();
            mUpdateWeekdays = mTodayWeek;
            if (mReportType == Constants.WORK_REPORT_WEEK) {
                mUpdateSerial = mThisWeekSerial;
            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                mUpdateSerial = mThisMonthSerial;
            }
            mUpdateStartTime = mInitPresentWorkReportBean.getStartTime();
            mUpdateEndTime = mInitPresentWorkReportBean.getEndTime();
        }
        if (mReportTimeBeforeRb.isChecked() && isBeforeExist) {
            mSummaryContentEt.setText(mInitBeforeWorkReportBean.getComment());
            mNewScheduleContentEt.setText(mInitBeforeWorkReportBean.getPlan());
            mExperienceContentEt.setText(mInitBeforeWorkReportBean.getExperience());

            mkeyValue = mInitBeforeWorkReportBean.getReportId();
            mUnfinishedTask = mInitBeforeWorkReportBean.getUnfinishedTask();
            mFinishedTask = mInitBeforeWorkReportBean.getContext();

            mUpdateDate = mInitBeforeWorkReportBean.getDate();
            mUpdateWeekdays = mYesterdayWeek;
            if (mReportType == Constants.WORK_REPORT_WEEK) {
                mUpdateSerial = mLastWeekSerial;
            } else if (mReportType == Constants.WORK_REPORT_MONTH) {
                mUpdateSerial = mLastWeekSerial;
            }
            mUpdateStartTime = mInitBeforeWorkReportBean.getStartTime();
            mUpdateEndTime = mInitBeforeWorkReportBean.getEndTime();
        }
    }

    private void initMonthlyReport(WorkReportBean workReportBean, com.alibaba.fastjson.JSONObject initDataObject) {
        workReportBean.setReportId(JSONUtil.getInt(initDataObject, "WM_ID"));
        workReportBean.setEmp(JSONUtil.getText(initDataObject, "WM_EMP"));
        workReportBean.setDate(JSONUtil.getText(initDataObject, "WM_DATE"));
        workReportBean.setComment(JSONUtil.getText(initDataObject, "WM_COMMENT"));
        workReportBean.setDepart(JSONUtil.getText(initDataObject, "WM_DEPART"));
        workReportBean.setJoname(JSONUtil.getText(initDataObject, "WM_JONAME"));
        workReportBean.setPlan(JSONUtil.getText(initDataObject, "WM_PLAN"));
        workReportBean.setExperience(JSONUtil.getText(initDataObject, "WM_EXPERIENCE"));
        workReportBean.setContext(JSONUtil.getText(initDataObject, "WM_CONTEXT"));
        workReportBean.setApprovalStatus(JSONUtil.getText(initDataObject, "STATUS"));
        workReportBean.setReportStatus(JSONUtil.getText(initDataObject, "WM_STATUS"));
        workReportBean.setUnfinishedTask(JSONUtil.getText(initDataObject, "WM_UNFINISHEDTASK"));
        workReportBean.setSerial(JSONUtil.getText(initDataObject, "WM_MONTH"));
        workReportBean.setStartTime(JSONUtil.getText(initDataObject, "WM_STARTTIME"));
        workReportBean.setEndTime(JSONUtil.getText(initDataObject, "WM_ENDTIME"));
        workReportBean.setRN(JSONUtil.getInt(initDataObject, "RN"));
    }

    private void initWeeklyReport(WorkReportBean workReportBean, com.alibaba.fastjson.JSONObject initDataObject) {
        workReportBean.setReportId(JSONUtil.getInt(initDataObject, "WW_ID"));
        workReportBean.setEmp(JSONUtil.getText(initDataObject, "WW_EMP"));
        workReportBean.setDate(JSONUtil.getText(initDataObject, "WW_DATE"));
        workReportBean.setComment(JSONUtil.getText(initDataObject, "WW_COMMENT"));
        workReportBean.setDepart(JSONUtil.getText(initDataObject, "WW_DEPART"));
        workReportBean.setJoname(JSONUtil.getText(initDataObject, "WW_JONAME"));
        workReportBean.setPlan(JSONUtil.getText(initDataObject, "WW_PLAN"));
        workReportBean.setExperience(JSONUtil.getText(initDataObject, "WW_EXPERIENCE"));
        workReportBean.setContext(JSONUtil.getText(initDataObject, "WW_CONTEXT"));
        workReportBean.setApprovalStatus(JSONUtil.getText(initDataObject, "STATUS"));
        workReportBean.setReportStatus(JSONUtil.getText(initDataObject, "WW_STATUS"));
        workReportBean.setUnfinishedTask(JSONUtil.getText(initDataObject, "WW_UNFINISHEDTASK"));
        workReportBean.setSerial(JSONUtil.getText(initDataObject, "WW_WEEK"));
        workReportBean.setStartTime(JSONUtil.getText(initDataObject, "WW_STARTTIME"));
        workReportBean.setEndTime(JSONUtil.getText(initDataObject, "WW_ENDTIME"));
        workReportBean.setRN(JSONUtil.getInt(initDataObject, "RN"));
    }

    private void initDailyReport(WorkReportBean workReportBean, com.alibaba.fastjson.JSONObject initDataObject) {
        workReportBean.setReportId(JSONUtil.getInt(initDataObject, "WD_ID"));
        workReportBean.setEmp(JSONUtil.getText(initDataObject, "WD_EMP"));
        workReportBean.setDate(JSONUtil.getText(initDataObject, "WD_DATE"));
        workReportBean.setComment(JSONUtil.getText(initDataObject, "WD_COMMENT"));
        workReportBean.setDepart(JSONUtil.getText(initDataObject, "WD_DEPART"));
        workReportBean.setJoname(JSONUtil.getText(initDataObject, "WD_JONAME"));
        workReportBean.setPlan(JSONUtil.getText(initDataObject, "WD_PLAN"));
        workReportBean.setExperience(JSONUtil.getText(initDataObject, "WD_EXPERIENCE"));
        workReportBean.setContext(JSONUtil.getText(initDataObject, "WD_CONTEXT"));
        workReportBean.setApprovalStatus(JSONUtil.getText(initDataObject, "STATUS"));
        workReportBean.setReportStatus(JSONUtil.getText(initDataObject, "WD_STATUS"));
        workReportBean.setUnfinishedTask(JSONUtil.getText(initDataObject, "WD_UNFINISHEDTASK"));
        String wd_weekdays = JSONUtil.getText(initDataObject, "WD_WEEKDAYS");
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
        workReportBean.setEntryDate(JSONUtil.getText(initDataObject, "WD_ENTRYDATE"));
        workReportBean.setRN(JSONUtil.getInt(initDataObject, "RN"));
    }

    private void initMonthlyReport(com.alibaba.fastjson.JSONObject dataObject) {
        mThisMonthStartDate = JSONUtil.getText(dataObject, "wm_starttime");
        mThisMonthEndDate = JSONUtil.getText(dataObject, "wm_endtime");
        mLastMonthStartDate = JSONUtil.getText(dataObject, "wm_starttimeOld");
        mLastMonthEndDate = JSONUtil.getText(dataObject, "wm_endtimeOld");
        mThisMonthSerial = JSONUtil.getText(dataObject, "wm_month");
        mLastMonthSerial = JSONUtil.getText(dataObject, "wm_monthOld");

        mPresentPlan = JSONUtil.getText(dataObject, "wm_plan");
        mBeforePlan = JSONUtil.getText(dataObject, "wm_planOld");

        mThisMonthTime = getMonthlyTimeStr(mThisMonthStartDate);
        mLastMonthTime = getMonthlyTimeStr(mLastMonthStartDate);

        progressDialog.dismiss();
        if (mReportTimeBeforeRb.isChecked()) {
            mMonthReportSerial = mLastMonthSerial;
            mMonthReportStartDate = mLastMonthStartDate;
            mMonthReportEndDate = mLastMonthEndDate;
            mReportTimeTv.setText(mLastMonthTime);
        } else if (mReportTimePresentRb.isChecked()) {
            mMonthReportSerial = mThisMonthSerial;
            mMonthReportStartDate = mThisMonthStartDate;
            mMonthReportEndDate = mThisMonthEndDate;
            mReportTimeTv.setText(mThisMonthTime);
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
            return mUpdateDate;
        }
    }

    private void initWeeklyReport(com.alibaba.fastjson.JSONObject dataObject) {
        mThisWeekStartDate = JSONUtil.getText(dataObject, "ww_starttime");
        mThisWeekEndDate = JSONUtil.getText(dataObject, "ww_endtime");
        mLastWeekStartDate = JSONUtil.getText(dataObject, "ww_starttimeOld");
        mLastWeekEndDate = JSONUtil.getText(dataObject, "ww_endtimeOld");
        mThisWeekSerial = JSONUtil.getText(dataObject, "ww_week");
        mLastWeekSerial = JSONUtil.getText(dataObject, "ww_weekOld");

        mPresentPlan = JSONUtil.getText(dataObject, "ww_plan");
        mBeforePlan = JSONUtil.getText(dataObject, "ww_planOld");

        mThisWeekTime = getWeeklyTimeStr(mThisWeekStartDate, mThisWeekEndDate);
        mLastWeekTime = getWeeklyTimeStr(mLastWeekStartDate, mLastWeekEndDate);

        progressDialog.dismiss();
        if (mReportTimeBeforeRb.isChecked()) {
            mWeekReportSerial = mLastWeekSerial;
            mWeekReportStartDate = mLastWeekStartDate;
            mWeekReportEndDate = mLastWeekEndDate;
            mReportTimeTv.setText(mLastWeekTime);
        } else if (mReportTimePresentRb.isChecked()) {
            mWeekReportSerial = mThisWeekSerial;
            mWeekReportStartDate = mThisWeekStartDate;
            mWeekReportEndDate = mThisWeekEndDate;
            mReportTimeTv.setText(mThisWeekTime);
        }
    }

    private String getWeeklyTimeStr(String weeklyStartDate, String weeklyEndDate) {
        try {
            String[] weekStartArray = weeklyStartDate.split("-");
            String[] weekEndArray = weeklyEndDate.split("-");
            StringBuilder weekTimeString = new StringBuilder("");
            if (weekStartArray != null && weekStartArray.length >= 3) {
                weekTimeString.append(weekStartArray[0] + "年  ").append(weekStartArray[1] + "月").append(weekStartArray[2] + "日");
                if (weekEndArray.length >= 3) {
                    weekTimeString.append("-" + weekEndArray[1] + "月").append(weekEndArray[2] + "日");
                }
            }
            return weekTimeString.toString();
        } catch (Exception e) {
            return mUpdateDate;
        }
    }

    private void initDailyReport(com.alibaba.fastjson.JSONObject dataObject) {
        mTodayDate = JSONUtil.getText(dataObject, "wd_date");
        mYesterdayDate = JSONUtil.getText(dataObject, "wd_dateOld");
        mTodayWeek = JSONUtil.getText(dataObject, "wd_weekDays");
        mYesterdayWeek = JSONUtil.getText(dataObject, "wd_weekDaysOld");

        mPresentPlan = JSONUtil.getText(dataObject, "wd_plan");
        mBeforePlan = JSONUtil.getText(dataObject, "wd_planOld");

        mTodayTime = getDailyTimeStr(mTodayDate, mTodayWeek);
        mYesterdayTime = getDailyTimeStr(mYesterdayDate, mYesterdayWeek);

        progressDialog.dismiss();
        if (mReportTimeBeforeRb.isChecked()) {
            mDailyReportDate = mYesterdayDate;
            mDailyReportWeek = mYesterdayWeek;
            mReportTimeTv.setText(mYesterdayTime);
        } else if (mReportTimePresentRb.isChecked()) {
            mDailyReportDate = mTodayDate;
            mDailyReportWeek = mTodayWeek;
            mReportTimeTv.setText(mTodayTime);
        }
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
            return mUpdateDate;
        }
    }

    public void doGrabJobContent(int mkeyValue) {
        //跳转之前抓取工作内容
        String catchUrl = "oa/persontask/catchWorkContent.action";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            catchUrl = "oa/persontask/catchWorkContent.action";
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            catchUrl = "oa/persontask/catchWorkContentWeekly.action";
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            catchUrl = "oa/persontask/catchWorkContentMonthly.action";
        }
        String url = CommonUtil.getAppBaseUrl(this) + catchUrl;
        HashMap<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("id", mkeyValue);
        LogUtil.d(JSON.toJSONString(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, handler, headers, GRAB_JOB_CONTENT, null, null, "post");
    }


    private void saveAndSubmitReport() {
        StringBuilder builder = new StringBuilder();
        if (!StringUtil.isEmpty(mSummaryContentEt.getText().toString())) {
            builder.append(getString(R.string.wd_summary_title) + mSummaryContentEt.getText().toString() + "\n");
        } else {
            Crouton.makeText(ct, R.string.add_summed);
            return;
        }
        if (!StringUtil.isEmpty(mNewScheduleContentEt.getText().toString())) {
            builder.append(getString(R.string.wd_plan_title) + mNewScheduleContentEt.getText().toString() + "\n");
        }

        if (!StringUtil.isEmpty(mExperienceContentEt.getText().toString())) {
            builder.append(getString(R.string.wd_experience_title) + mExperienceContentEt.getText().toString() + "\n");
        }
        if (MyApplication.getInstance().isNetworkActive()) {
            showsubmitDialog();
        } else {
            ToastMessage(getResources().getString(R.string.networks_out));
        }
    }

    private void showsubmitDialog() {
        String submiString = getResources().getString(R.string.report_submit_notice);
        String submitNotice = "";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            submitNotice = String.format(submiString, "日");
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            submitNotice = String.format(submiString, "周");
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            submitNotice = String.format(submiString, "月");
        }
        PopupWindowHelper.showAlart(WorkReportAddActivity.this, getString(R.string.common_notice),
                submitNotice, new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            sendWorkDailyByErp();
                        }
                    }
                });
    }

    private void sendWorkDailyByErp() {
        progressDialog.show();
        mSubmitButton.setEnabled(false);
        //执行重新提交之更改单据状态操作
        boolean isResubmit = (!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit") && mkeyValue != 0) ||
                (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub") && mkeyValue != 0);
        if (isResubmit || (mReportTimePresentRb.isChecked() && isPresentExist)
                || (mReportTimeBeforeRb.isChecked() && isBeforeExist)) {
            isOnlyUpdate = false;
            updateWorkReport();
        } else {
            Map<String, Object> formStoreMap = obtainFormStore();

            String url = CommonUtil.getAppBaseUrl(this) + "/mobile/addWorkReport.action";
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", mCaller);
            params.put("formStore", formStore);
            LogUtil.d(JSON.toJSONString(params));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, handler, headers, DAILY_SUBMITTED_SUCCESSFULLY, null, null, "post");
            Log.i("flhaddworkdaily: ", formStore + " ");
        }

    }

    /**
     * 更新工作汇报（不修改单据状态）
     */
    private void updateWorkReport() {
        String sb_summary = string2Json(mSummaryText);
        String sb_plan = string2Json(mNewScheduleText);
        String sb_experience = string2Json(mExperienceText);

        Map<String, Object> formStoreMap2 = obtainUpdateFormStore(sb_summary, sb_plan, sb_experience);

        //更新单据数据
        String updateUrl = "/oa/persontask/updateWorkDaily.action";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            updateUrl = "/oa/persontask/updateWorkDaily.action";
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            updateUrl = "/oa/persontask/updateWorkWeekly.action";
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            updateUrl = "/oa/persontask/updateWorkMonthly.action";
        }
        String url = CommonUtil.getAppBaseUrl(this) + updateUrl;
        HashMap<String, Object> params = new HashMap<>();
        String formStore2 = JSONUtil.map2JSON(formStoreMap2);
        Log.d("updatework", formStore2);
        params.put("caller", mCaller);
        params.put("formStore", formStore2);
        LogUtil.d(JSON.toJSONString(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, handler, headers, CLEAR_AF_UPDATE_DOC_STATE, null, null, "post");
    }

    /**
     * 提交工作汇报
     */
    private void submitWorkReport() {
        // 更新之后再提交覆盖之前同id单据
        Map<String, Object> params = new HashMap<>();
        String submitUrl = "/oa/persontask/submitWorkDaily.action";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            submitUrl = "/oa/persontask/submitWorkDaily.action";
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            submitUrl = "/oa/persontask/submitWorkWeekly.action";
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            submitUrl = "/oa/persontask/submitWorkMonthly.action";
        }
        String url = CommonUtil.getAppBaseUrl(ct) + submitUrl;
        params.put("caller", mCaller);
        params.put("id", mkeyValue);
        LinkedHashMap<String, Object> last_headers = new LinkedHashMap<>();
        last_headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, last_headers, LAST_SUBMIT_SUCCESSFULLY, null, null, "post");
    }


    @NonNull
    private Map<String, Object> obtainUpdateFormStore(String sb_summary, String sb_plan, String sb_experience) {
        Map<String, Object> formStoreMap = new HashMap<>();
        if (mReportType == Constants.WORK_REPORT_DAY) {
            formStoreMap.put("wd_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("wd_comment", sb_summary);
            formStoreMap.put("wd_plan", sb_plan);
            formStoreMap.put("wd_experience", sb_experience);
            formStoreMap.put("wd_id", String.valueOf(mkeyValue));   //这个地方之前传int类型调试很久都更新不了，只能用String类型，不明白后台处理机制
            formStoreMap.put("wd_date", mUpdateDate);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            formStoreMap.put("ww_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("ww_comment", sb_summary);
            formStoreMap.put("ww_plan", sb_plan);
            formStoreMap.put("ww_experience", sb_experience);
            formStoreMap.put("ww_id", String.valueOf(mkeyValue));   //这个地方之前传int类型调试很久都更新不了，只能用String类型，不明白后台处理机制
            formStoreMap.put("ww_week", mUpdateSerial);
            formStoreMap.put("ww_starttime", mUpdateStartTime);
            formStoreMap.put("ww_endtime", mUpdateEndTime);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            formStoreMap.put("wm_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("wm_comment", sb_summary);
            formStoreMap.put("wm_plan", sb_plan);
            formStoreMap.put("wm_experience", sb_experience);
            formStoreMap.put("wm_id", String.valueOf(mkeyValue));   //这个地方之前传int类型调试很久都更新不了，只能用String类型，不明白后台处理机制
            formStoreMap.put("wm_month", mUpdateSerial);
            formStoreMap.put("wm_starttime", mUpdateStartTime);
            formStoreMap.put("wm_endtime", mUpdateEndTime);
        }

        return formStoreMap;
    }

    @NonNull
    private Map<String, Object> obtainFormStore() {
        Map<String, Object> formStoreMap = new HashMap<>();
        boolean isResubmit = (!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit")) ||
                (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub"));
        if (mReportType == Constants.WORK_REPORT_DAY) {
            formStoreMap.put("wd_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("wd_comment", mSummaryText);
            if (mNewScheduleLl.getVisibility() == View.VISIBLE) {
                formStoreMap.put("wd_plan", mNewScheduleText);
            }
            formStoreMap.put("wd_experience", mExperienceText);
            formStoreMap.put("wd_date", isResubmit ? mUpdateDate : mDailyReportDate);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            formStoreMap.put("ww_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("ww_comment", mSummaryText);
            if (mNewScheduleLl.getVisibility() == View.VISIBLE) {
                formStoreMap.put("ww_plan", mNewScheduleText);
            }
            formStoreMap.put("ww_experience", mExperienceText);
            formStoreMap.put("ww_week", isResubmit ? mUpdateSerial : mWeekReportSerial);
            formStoreMap.put("ww_starttime", isResubmit ? mUpdateStartTime : mWeekReportStartDate);
            formStoreMap.put("ww_endtime", isResubmit ? mUpdateEndTime : mWeekReportEndDate);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            formStoreMap.put("wm_empcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            formStoreMap.put("wm_comment", mSummaryText);
            if (mNewScheduleLl.getVisibility() == View.VISIBLE) {
                formStoreMap.put("wm_plan", mNewScheduleText);
            }
            formStoreMap.put("wm_experience", mExperienceText);
            formStoreMap.put("wm_month", isResubmit ? mUpdateSerial : mMonthReportSerial);
            formStoreMap.put("wm_starttime", isResubmit ? mUpdateStartTime : mMonthReportStartDate);
            formStoreMap.put("wm_endtime", isResubmit ? mUpdateEndTime : mMonthReportEndDate);
        }
        return formStoreMap;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        boolean isResubmit = !TextUtils.isEmpty(resubmit) && (resubmit.equals("resubmit") || resubmit.equals("unsub_tosub"));
        if (isResubmit) {

        } else {
            getMenuInflater().inflate(R.menu.menu_list, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.push) {
            isOptionEvent = true;
            leavePressEvent();
        }

        if (item.getItemId() == android.R.id.home) {
            isOptionEvent = false;
            leavePressEvent();
        }
        return true;
    }

    /**
     * 页面离开事件（是否保存单据为在录入状态或者更新汇报内容）
     */
    private void leavePressEvent() {
        final Intent intent = getIntent();
        fromqzone = intent.getStringExtra("fromqzone");
        final boolean isResubmit = !TextUtils.isEmpty(resubmit) && ("unsub_tosub".equals(resubmit) || "resubmit".equals(resubmit));
        if ((isResubmit && isUpdateModify) || (mReportTimePresentRb.isChecked() && isPresentExist && isPresentModify)
                || (mReportTimeBeforeRb.isChecked() && isBeforeExist && isBeforeModify)) {
            PopupWindowHelper.showAlart(WorkReportAddActivity.this,
                    getString(R.string.common_notice), "正在离开录入页面，是否更新汇报内容？",
                    new PopupWindowHelper.OnSelectListener() {
                        @Override
                        public void select(boolean selectOk) {
                            if (selectOk) {
                                if (mSummaryContentEt.testValidity()) {
                                    if (mNewScheduleContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER &&
                                            mExperienceContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER) {
                                        if (CommonUtil.isNetWorkConnected(WorkReportAddActivity.this)) {
                                            isOnlyUpdate = true;
                                            progressDialog.show();
                                            updateWorkReport();
                                        } else {
                                            ToastUtil.showToast(WorkReportAddActivity.this, R.string.networks_out);
                                        }

                                    } else {
                                        ToastUtil.showToast(WorkReportAddActivity.this, "字数已超限！");
                                    }
                                }
                            } else {
                                if (isResubmit || isOptionEvent) {
                                    Intent intent1 = new Intent(WorkReportAddActivity.this, WorkDailyShowActivity.class);
                                    intent1.putExtra("caller", mCaller);
                                    intent1.putExtra("report_type", mReportType);
                                    if (isOptionEvent) {
                                        intent1.putExtra("fromwhere", "nosubmitdaily");
                                    }
                                    startActivityForResult(intent1, WORK_REPORT_LIST);
                                }
                                if (!isOptionEvent) {
                                    finish();
                                }
                            }
                        }
                    });
        } else {
            if (((mReportTimePresentRb.isChecked() && !isPresentExist)
                    || (mReportTimeBeforeRb.isChecked() && !isBeforeExist)) && !isResubmit) {
                if (mSummaryText.length() > 0 ||
                        mNewScheduleText.length() > 0 ||
                        mExperienceText.length() > 0) {
                    if (mNewScheduleContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER &&
                            mExperienceContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER) {
                        PopupWindowHelper.showAlart(WorkReportAddActivity.this, getString(R.string.common_notice),
                                getString(R.string.work_report_exit_notice), new PopupWindowHelper.OnSelectListener() {
                                    @Override
                                    public void select(boolean selectOk) {
                                        if (selectOk) {
                                            if (mSummaryContentEt.testValidity()) {
                                                if (mNewScheduleContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER &&
                                                        mExperienceContentEt.getText().toString().length() <= WORD_RESTRICTION_NUMBER) {
                                                    if (CommonUtil.isNetWorkConnected(WorkReportAddActivity.this)) {
                                                        progressDialog.show();
                                                        saveWorkReport();
                                                    } else {
                                                        ToastUtil.showToast(WorkReportAddActivity.this, R.string.networks_out);
                                                    }
                                                } else {
                                                    ToastUtil.showToast(WorkReportAddActivity.this, "字数已超限！");
                                                }

                                            }
                                        } else {
                                            boolean isResubmit = !TextUtils.isEmpty(resubmit) && ("unsub_tosub".equals(resubmit) || "resubmit".equals(resubmit));
                                            if (isResubmit || isOptionEvent) {
                                                Intent optionIntent = new Intent(WorkReportAddActivity.this, WorkDailyShowActivity.class);
                                                optionIntent.putExtra("caller", mCaller);
                                                optionIntent.putExtra("report_type", mReportType);
                                                if (isOptionEvent) {
                                                    optionIntent.putExtra("fromwhere", "nosubmitdaily");
                                                }
                                                startActivityForResult(optionIntent, WORK_REPORT_LIST);
                                            } else if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {

                                            } else {
                                                Intent intent1 = new Intent(WorkReportAddActivity.this, WorkReportMenuActivity.class);
                                                startActivity(intent1);
                                            }
                                            if (!isOptionEvent) {
                                                finish();
                                            }
                                        }
                                    }
                                });
                    }
                } else if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {
                    finish();
                } else {
                    leaveIntent(isResubmit);
                }
            } else if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {
                finish();
            } else {
                leaveIntent(isResubmit);
            }

        }

    }

    private void leaveIntent(boolean isResubmit) {
        Intent intent1 = null;
        if (isResubmit || isOptionEvent) {
            intent1 = new Intent(WorkReportAddActivity.this, WorkDailyShowActivity.class);
            intent1.putExtra("caller", mCaller);
            intent1.putExtra("report_type", mReportType);
            if (isOptionEvent) {
                intent1.putExtra("fromwhere", "nosubmitdaily");
            }
            startActivityForResult(intent1, WORK_REPORT_LIST);
        } else {
            intent1 = new Intent(WorkReportAddActivity.this, WorkReportMenuActivity.class);
            startActivity(intent1);
        }
        if (!isOptionEvent) {
            finish();
        }
    }

    private void saveWorkReport() {
        String saveUrl = "oa/persontask/saveWorkDaily.action";
        if (mReportType == Constants.WORK_REPORT_DAY) {
            saveUrl = "oa/persontask/saveWorkDaily.action";
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            saveUrl = "oa/persontask/saveWorkWeekly.action";
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            saveUrl = "oa/persontask/saveWorkMonthly.action";
        }
        Map<String, Object> formStoreMap = obtainFormStore();
        String formStore = JSON.toJSONString(formStoreMap);
        mHttpClient.Api().send(new HttpClient.Builder()
                .url(saveUrl)
                .add("caller", mCaller)
                .add("formStore", formStore)
                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"))
                .method(Method.POST).build(), new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    progressDialog.dismiss();
                    Log.d("savetoentry", o.toString());
                    Toast.makeText(WorkReportAddActivity.this, "保存为在录入状态成功", Toast.LENGTH_SHORT).show();
                    boolean isResubmit = !TextUtils.isEmpty(resubmit) && ("unsub_tosub".equals(resubmit) || "resubmit".equals(resubmit));
                    if (isResubmit || isOptionEvent) {
                        Intent optionIntent = new Intent(WorkReportAddActivity.this, WorkDailyShowActivity.class);
                        optionIntent.putExtra("caller", mCaller);
                        optionIntent.putExtra("report_type", mReportType);
                        if (isOptionEvent) {
                            optionIntent.putExtra("fromwhere", "nosubmitdaily");
                        }
                        startActivityForResult(optionIntent, WORK_REPORT_LIST);

                    } else if (!TextUtils.isEmpty(fromqzone) && "fromqzone".equals(fromqzone)) {

                    } else {
                        Intent intent1 = new Intent(WorkReportAddActivity.this, WorkReportMenuActivity.class);
                        startActivity(intent1);
                    }
                    if (!isOptionEvent) {
                        finish();
                    }
                } catch (Exception e) {
                    progressDialog.dismiss();
                    ToastUtil.showToast(WorkReportAddActivity.this, getString(R.string.save_to_entry_status_failed));
                }
            }

            @Override
            public void onFailure(Object t) {
                progressDialog.dismiss();
                if (JSONUtil.validate(t.toString())) {
                    com.alibaba.fastjson.JSONObject failObject = JSON.parseObject(t.toString());
                    String errorMsg = JSONUtil.getText(failObject, "exceptionInfo");
                    if (TextUtils.isEmpty(errorMsg)) {
                        ToastUtil.showToast(WorkReportAddActivity.this, getString(R.string.save_to_entry_status_failed));
                    } else {
                        ToastUtil.showToast(WorkReportAddActivity.this, errorMsg);
                    }
                } else {
                    ToastUtil.showToast(WorkReportAddActivity.this, getString(R.string.save_to_entry_status_failed));
                }
            }
        }));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x22) {
            if (data != null) {
                SelectBean b = data.getParcelableExtra("data");
                if (b == null) {
                    return;
                }
                String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                getEmnameByReturn(name);
            } else {
                jumptododetail(mkeyValue);
            }
        } else if (requestCode == WORK_REPORT_LIST) {
            mDateLinearLayout.setVisibility(View.GONE);
            mReportTimeRg.setVisibility(View.VISIBLE);
            mOldScheduleLl.setVisibility(View.VISIBLE);
            mSummaryContentEt.setText("");
            mNewScheduleContentEt.setText("");
            mExperienceContentEt.setText("");
            isPresentExist = false;
            isBeforeExist = false;
            mInitPresentWorkReportBean = new WorkReportBean();
            mInitBeforeWorkReportBean = new WorkReportBean();
            if (CommonUtil.isNetWorkConnected(this)) {
                getWorkReportInit();
            } else {
                ToastUtil.showToast(this, R.string.networks_out);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOptionEvent = false;
    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) {
            return;
        }
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                selectApprovers(name);
            } else {
                progressDialog.dismiss();
                jumptododetail(mkeyValue);
            }
        } else {
            progressDialog.dismiss();
            jumptododetail(mkeyValue);
        }
    }

    private void sendToSelect(String noid, JSONArray data) {
        this.mNodeId = noid;
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        for (int i = 0; i < data.size(); i++) {
            bean = new SelectBean();
            bean.setName(data.getString(i));
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", getString(R.string.select_approvel_people));
        startActivityForResult(intent, 0x22);
    }

    private void selectApprovers(String emName) {
        String url = CommonUtil.getAppBaseUrl(this) + "common/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", emName);
        params.put("nodeId", mNodeId);
        param.put("_noc", 1);
        param.put("params", JSONUtil.map2JSON(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, SELECT_APPROVERS, null, null, "post");
    }

    private void judgeApprovers(int mkeyValue) {
        String url = CommonUtil.getAppBaseUrl(this) + "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", mCaller);
        param.put("id", mkeyValue);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, GET_MULTINODE_ASSIGNS, null, null, "post");
    }


    public void jumptododetail(int mkeyValue) {
        Intent intent = new Intent(activity, DailydetailsActivity.class);
        intent.putExtra("Date", DateFormatUtil.long2Str(DateFormatUtil.YMD));
        intent.putExtra("caller", mCaller);
        intent.putExtra("ID", mkeyValue);
        intent.putExtra("fromwhere", "submitdaily");
        intent.putExtra("Content", mSummaryText);
        intent.putExtra("WD_Status", "已提交");
        intent.putExtra("Plan", mNewScheduleText);
        intent.putExtra("Experience", mExperienceText);
        intent.putExtra("Donetask", mFinishedTask);
        intent.putExtra("Undotask", mUnfinishedTask);
        intent.putExtra("report_type", mReportType);
        intent.putExtra("caller", mCaller);
        boolean isResubmit = (!TextUtils.isEmpty(resubmit) && resubmit.equals("resubmit")) ||
                (!TextUtils.isEmpty(resubmit) && resubmit.equals("unsub_tosub"));
        if (mReportType == Constants.WORK_REPORT_DAY) {
            intent.putExtra("report_date", isResubmit ? mUpdateDate : mDailyReportDate);
            intent.putExtra("report_weekdays", isResubmit ? mUpdateWeekdays : mDailyReportWeek);
        } else if (mReportType == Constants.WORK_REPORT_WEEK) {
            intent.putExtra("report_serial", isResubmit ? mUpdateSerial : mWeekReportSerial);
            intent.putExtra("report_startTime", isResubmit ? mUpdateStartTime : mWeekReportStartDate);
            intent.putExtra("report_endTime", isResubmit ? mUpdateEndTime : mWeekReportEndDate);
        } else if (mReportType == Constants.WORK_REPORT_MONTH) {
            intent.putExtra("report_serial", isResubmit ? mUpdateSerial : mMonthReportSerial);
            intent.putExtra("report_startTime", isResubmit ? mUpdateStartTime : mMonthReportStartDate);
            intent.putExtra("report_endTime", isResubmit ? mUpdateEndTime : mMonthReportEndDate);
        }
        progressDialog.dismiss();
        mSubmitButton.setEnabled(true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        isOptionEvent = false;
        leavePressEvent();
    }

    /**
     * JSON字符串特殊字符处理，比如：“\A1;1300”
     *
     * @param s
     * @return String
     */
    public String string2Json(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

}
