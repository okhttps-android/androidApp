package com.xzjmyk.pm.activity.ui.erp.activity.oa;

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
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出差申请
 * Created by RaoMeng on 2016/10/18.
 */
public class TravelRequestActivity extends BaseActivity implements View.OnTouchListener, View.OnClickListener {
    private FormEditText mExplainEt;
    private RelativeLayout mStartTimeRl, mEndTimeRl, mBasisRl;
    private TextView mStartTimeTv, mEndTimeTv, mBasisTv;
    private FormEditText mDaysEt, mDestinationEt, mTargetEt, mProjectEt;
    private Button mCommitBtn;

    private final static int TRAVEL_BASIS_REQUEST = 102;
    private SingleDialog signDialog;
    private List<String> mBasisList = new ArrayList<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRAVEL_BASIS_REQUEST:
                    progressDialog.dismiss();
                    mBasisList = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get("combdatas");
                    if (mBasisList.isEmpty()) {
                        mBasisList.add("无");
                    }
                    showSignDialog(findViewById(R.id.et_extra_sign));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_request);
       setTitle("出差申请");

        initViews();
        initEvents();
    }

    private void initEvents() {
        mExplainEt.setOnTouchListener(this);
        mStartTimeRl.setOnClickListener(this);
        mEndTimeRl.setOnClickListener(this);
        mBasisRl.setOnClickListener(this);
        mCommitBtn.setOnClickListener(this);
    }

    private void initViews() {
        mExplainEt = (FormEditText) findViewById(R.id.travel_request_explain_et);
        mStartTimeRl = (RelativeLayout) findViewById(R.id.travel_request_starttime_rl);
        mEndTimeRl = (RelativeLayout) findViewById(R.id.travel_request_endtime_rl);
        mBasisRl = (RelativeLayout) findViewById(R.id.travel_request_basis_rl);
        mStartTimeTv = (TextView) findViewById(R.id.travel_request_starttime_tv);
        mEndTimeTv = (TextView) findViewById(R.id.travel_request_endtime_tv);
        mBasisTv = (TextView) findViewById(R.id.travel_request_basis_tv);
        mDaysEt = (FormEditText) findViewById(R.id.travel_request_days_et);
        mDestinationEt = (FormEditText) findViewById(R.id.travel_request_destination_et);
        mTargetEt = (FormEditText) findViewById(R.id.travel_request_target_et);
        mProjectEt = (FormEditText) findViewById(R.id.travel_request_project_et);
        mCommitBtn = (Button) findViewById(R.id.travel_request_commit_btn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_request_starttime_rl:
                CommonUtil.showDataPickDialog(this, mStartTimeTv);
                break;
            case R.id.travel_request_endtime_rl:
                CommonUtil.showDataPickDialog(this, mEndTimeTv);
                break;
            case R.id.travel_request_basis_rl:
                progressDialog.show();
                loadTravelBasis("fp_v6", TRAVEL_BASIS_REQUEST);
                break;
            case R.id.travel_request_commit_btn:
                break;
        }
    }

    /**
     * 获取考勤依据列表
     *
     * @param field
     * @param what
     */
    public void loadTravelBasis(String field, int what) {
        Log.i("leave", "what=" + what);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getCombo.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("caller", "FeePlease!CCSQ");
        param.put("field", field);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.getDataFormServer(ct, mHandler, url, param, what);
    }


    /**
     * 展示考勤列表
     *
     * @param view
     */
    public void showSignDialog(View view) {
        if (signDialog == null) {
            signDialog = new SingleDialog(ct, "考勤", new SingleDialog.PickDialogListener() {
                @Override
                public void onListItemClick(int position, String value) {
                    mBasisTv.setText(value);
                }
            });
            signDialog.show();
            signDialog.initViewData(mBasisList);
        } else {
            signDialog.show();
            signDialog.initViewData(mBasisList);
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
                it_scale.putExtra("caller", "FeePlease!CCSQ");
                it_scale.putExtra("title", "出差单查询");
                it_scale.putExtra("from", "SignMain");
                startActivity(it_scale);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
        if ((view.getId() == R.id.travel_request_explain_et && CommonUtil.canVerticalScroll(mExplainEt))) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }

}
