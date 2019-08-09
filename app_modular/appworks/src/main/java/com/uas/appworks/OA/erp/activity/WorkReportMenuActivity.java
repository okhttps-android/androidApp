package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.widget.RecyclerViewGridDivider;
import com.uas.appworks.OA.erp.adapter.WorkReportMenuAdapter;
import com.uas.appworks.R;

/**
 * @author RaoMeng
 * @describe 工作汇报菜单页面
 * @date 2017/10/16
 */

public class WorkReportMenuActivity extends BaseActivity {
    private RecyclerView mMenuRecyclerView;
    private LinearLayoutManager mGridLayoutManager;
    private RecyclerViewGridDivider mGridDivider;
    private WorkReportMenuAdapter mWorkReportMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_report_menu);
        setTitle(R.string.str_work_report);
        initViews();
        mWorkReportMenuAdapter.setOnItemClickListener(new WorkReportMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int positon) {
                Intent intent = new Intent();
                intent.setClass(WorkReportMenuActivity.this, WorkReportAddActivity.class);
                int reportType = Constants.WORK_REPORT_DAY;
                if (positon == 0) {
                    reportType = Constants.WORK_REPORT_DAY;
                } else if (positon == 1) {
                    reportType = Constants.WORK_REPORT_WEEK;
                } else if (positon == 2) {
                    reportType = Constants.WORK_REPORT_MONTH;
                }
                intent.putExtra("report_type", reportType);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        mMenuRecyclerView = (RecyclerView) findViewById(R.id.work_report_menu_rv);

        mGridLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mMenuRecyclerView.setLayoutManager(mGridLayoutManager);
        mGridDivider = new RecyclerViewGridDivider(this, true);
        mMenuRecyclerView.addItemDecoration(mGridDivider);
        mWorkReportMenuAdapter = new WorkReportMenuAdapter(this);
        mMenuRecyclerView.setAdapter(mWorkReportMenuAdapter);
    }
}
