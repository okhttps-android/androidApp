package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.core.widget.SingleDialog;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.SaleSelectActivity;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假申请
 * Created by RaoMeng on 2016/10/18.
 */
public class AskForLeaveActivity extends BaseActivity implements View.OnTouchListener, View.OnClickListener {
    private FormEditText mExplainEt;
    private RelativeLayout mStartTimeRl, mEndTimeRl, mCategoryRl;
    private Button mCommitBtn;
    private DateTimePickerDialog dialog;
    private TextView mStartTimeTv, mEndTimeTv, mCategoryTv;

    private final static int CATEGORY_REQUEST = 101;
    private List<String> mCategoryList = new ArrayList<>();
    private SingleDialog singleDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_for_leave);
        setTitle("请假申请");

        initViews();
        initEvents();
        initDates();
    }

    private void initDates() {

    }

    private void initEvents() {
        mExplainEt.setOnTouchListener(this);
        mStartTimeRl.setOnClickListener(this);
        mEndTimeRl.setOnClickListener(this);
        mCategoryRl.setOnClickListener(this);
        mCommitBtn.setOnClickListener(this);
    }

    private void initViews() {
        mExplainEt = (FormEditText) findViewById(R.id.ask_leave_explain_et);
        mStartTimeRl = (RelativeLayout) findViewById(R.id.ask_leave_starttime_rl);
        mEndTimeRl = (RelativeLayout) findViewById(R.id.ask_leave_endtime_rl);
        mCategoryRl = (RelativeLayout) findViewById(R.id.ask_leave_category_rl);
        mCommitBtn = (Button) findViewById(R.id.ask_leave_commit_btn);

        mStartTimeTv = (TextView) findViewById(R.id.ask_leave_starttime_tv);
        mEndTimeTv = (TextView) findViewById(R.id.ask_leave_endtime_tv);
        mCategoryTv = (TextView) findViewById(R.id.ask_leave_category_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ask_leave_starttime_rl:
                showDataPickDialog(v);
                break;
            case R.id.ask_leave_endtime_rl:
                showDataPickDialog(v);
                break;
            case R.id.ask_leave_category_rl:
                progressDialog.show();
                loadCategory("va_vacationtype", CATEGORY_REQUEST);
                break;
            case R.id.ask_leave_commit_btn:
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CATEGORY_REQUEST:
                    progressDialog.dismiss();
                    mCategoryList = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                            "combdatas");
                    if (mCategoryList.isEmpty()) {
                        mCategoryList.add("无");
                    }
                    showSimpleDialog();
                    break;
            }
        }
    };

    public void showSimpleDialog() {
        if (singleDialog == null) {
            singleDialog = new SingleDialog(ct, "请假类型",
                    new SingleDialog.PickDialogListener() {
                        @Override
                        public void onListItemClick(int position, String value) {
                            mCategoryTv.setText(value);
                        }
                    });
            singleDialog.show();
            singleDialog.initViewData(mCategoryList);
        } else {
            singleDialog.show();
            singleDialog.initViewData(mCategoryList);
        }
    }

    /**
     * 获取请假类型
     *
     * @param field
     * @param what
     */
    public void loadCategory(String field, int what) {
        Log.i("leave", "what=" + what);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getCombo.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("caller", "Ask4Leave");
        param.put("field", field);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.getDataFormServer(ct, mHandler, url, param, what);
    }


    public void showDataPickDialog(final View view) {
        if (dialog == null) {
            dialog = new DateTimePickerDialog(this, System.currentTimeMillis());
        }

        dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dia, long date) {
                if ((view.getId() == R.id.ask_leave_starttime_rl)) {
                    mStartTimeTv.setText(CommonUtil.getStringDateMM(date));
                }

                if ((view.getId() == R.id.ask_leave_endtime_rl)) {
                    mEndTimeTv.setText(CommonUtil.getStringDateMM(date));
                }

                /** @注释：保证 初始化当前时间 */
                dialog = null;
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_oa_form, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.oa_form:
                Intent it_scale = new Intent(ct, SaleSelectActivity.class);
                it_scale.putExtra("caller", "Ask4Leave");
                it_scale.putExtra("title", "请假单查询");
                it_scale.putExtra("from", "SignMain");
                startActivity(it_scale);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
        if ((view.getId() == R.id.ask_leave_explain_et && CommonUtil.canVerticalScroll(mExplainEt))) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }
}
