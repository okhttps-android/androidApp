package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.andreabaccega.widget.FormEditText;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.SaleSelectActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

/**
 * 特殊考勤
 * Created by RaoMeng on 2016/10/18.
 */
public class SpecialAttendanceActivity extends BaseActivity implements View.OnTouchListener {
    private FormEditText mExplainEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_attendance);
       setTitle("特殊考勤");

        initViews();
        initEvents();
    }

    private void initEvents() {
        mExplainEt.setOnTouchListener(this);
    }

    private void initViews() {
        mExplainEt = (FormEditText) findViewById(R.id.special_attendance_explain_et);
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
                it_scale.putExtra("caller", "SpeAttendance");
                it_scale.putExtra("title", "特殊考勤查询");
                it_scale.putExtra("from", "SignMain");
                startActivity(it_scale);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
        if ((view.getId() == R.id.special_attendance_explain_et && CommonUtil.canVerticalScroll(mExplainEt))) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }
}
