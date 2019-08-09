package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.SaleSelectActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

/**
 * 加班申请
 * Created by RaoMeng on 2016/10/18.
 */
public class OvertimeApplyActivity extends BaseActivity implements View.OnTouchListener, View.OnClickListener {
    private FormEditText mExplainEt;
    private RelativeLayout mStartTimeRl,mEndTimeRl;
    private TextView mStartTimeTv,mEndTimeTv;
    private Button mCommitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overtime_apply);
        setTitle("加班申请");

        initViews();
        initEvents();
    }

    private void initEvents() {
        mExplainEt.setOnTouchListener(this);
        mStartTimeRl.setOnClickListener(this);
        mEndTimeRl.setOnClickListener(this);
        mCommitBtn.setOnClickListener(this);
    }

    private void initViews() {
        mExplainEt = (FormEditText) findViewById(R.id.overtime_apply_explain_et);
        mStartTimeRl = (RelativeLayout) findViewById(R.id.overtime_apply_starttime_rl);
        mEndTimeRl = (RelativeLayout) findViewById(R.id.overtime_apply_endtime_rl);
        mStartTimeTv = (TextView) findViewById(R.id.overtime_apply_starttime_tv);
        mEndTimeTv = (TextView) findViewById(R.id.overtime_apply_endtime_tv);
        mCommitBtn = (Button) findViewById(R.id.overtime_apply_commit_btn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.overtime_apply_starttime_rl:
                CommonUtil.showDataPickDialog(this,mStartTimeTv);
                break;
            case R.id.overtime_apply_endtime_rl:
                CommonUtil.showDataPickDialog(this,mEndTimeTv);
                break;
            case R.id.overtime_apply_commit_btn:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_oa_form, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.oa_form:
                Intent it_scale = new Intent(ct,SaleSelectActivity.class);
                it_scale.putExtra("caller", "Workovertime");
                it_scale.putExtra("title", "加班申请查询");
                it_scale.putExtra("from", "SignMain");
                startActivity(it_scale);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
        if ((view.getId() == R.id.overtime_apply_explain_et && CommonUtil.canVerticalScroll(mExplainEt))) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }

}
