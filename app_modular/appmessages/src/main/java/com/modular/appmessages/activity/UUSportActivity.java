package com.modular.appmessages.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.app.AppConstant;
import com.core.base.BaseActivity;
import com.core.dao.StepRankingFirstDao;
import com.core.model.StepRankingFirstBean;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.UUSportLVAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/9/27.
 * function:
 */

public class UUSportActivity extends BaseActivity implements View.OnClickListener {
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    };
    private LinearLayoutManager manager;
    private MyListView content_mylv;
    private UUSportLVAdapter lvAdapter;
    private List<StepRankingFirstBean> model;
    private StepRankingFirstDao mStepRankingFirstDao;
    private EmptyLayout mEmptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uusport);
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter(AppConstant.UUSPORT_PUSH));
        initView();
        initData();
    }

    private void initData() {
        model = mStepRankingFirstDao.getAllRFIDatas();
        StepRankingFirstBean mm = mStepRankingFirstDao.getCurDataByDate("2017年11月09日");
        LogUtil.prinlnLongMsg("fanglh", JSON.toJSONString(model)+","+JSON.toJSONString(mm));
        if (ListUtils.isEmpty(model)){
            mEmptyLayout.showEmpty();
            return;
        }
        lvAdapter.setModels(model);
        lvAdapter.notifyDataSetChanged();
    }

    private void initView() {
        manager = new LinearLayoutManager(ct);
        content_mylv = (MyListView) findViewById(R.id.content_mylv);

        lvAdapter = new UUSportLVAdapter(this);
        content_mylv.setAdapter(lvAdapter);

        findViewById(R.id.step_rank_ll).setOnClickListener(this);

        model = new ArrayList<>();
        mStepRankingFirstDao = new StepRankingFirstDao();

        mEmptyLayout = new EmptyLayout(this, content_mylv);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
    }

    @Override
    public void onClick(View v) {
       if (v.getId() == R.id.step_rank_ll){
           startActivity(new Intent("com.modular.appme.UURanking"));
       }
    }
}
