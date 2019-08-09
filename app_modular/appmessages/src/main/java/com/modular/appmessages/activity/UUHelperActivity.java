package com.modular.appmessages.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.common.data.ListUtils;
import com.common.ui.ViewUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.dao.UUHelperDao;
import com.core.model.UUHelperModel;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.UUHelperAdapter;

import java.util.List;

public class UUHelperActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView contentRV;
    private UUHelperAdapter mAdapter;
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    };
    private LinearLayoutManager manager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_uuhelper, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuMe) {
            startActivity(new Intent(ct, UUHelperDetailsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uuhelper);
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter(AppConstant.UPDATA_UUHELPER));
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
//        UUHelperDao.getInstance().updateRead();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        super.onDestroy();
    }

    private void initView() {
        contentRV = (RecyclerView) findViewById(R.id.contentRV);
        contentRV.setItemAnimator(new DefaultItemAnimator());
        manager = new LinearLayoutManager(ct);
        contentRV.setLayoutManager(manager);
        findViewById(R.id.successfulTV).setOnClickListener(this);
        findViewById(R.id.experienceTV).setOnClickListener(this);
        findViewById(R.id.serviceTV).setOnClickListener(this);

    }

    private void initData() {
        List<UUHelperModel> models = UUHelperDao.getInstance().getAllModels();
        showModel(models);
    }

    private void showModel(final List<UUHelperModel> models) {
        if (mAdapter == null) {
            mAdapter = new UUHelperAdapter(ct, models);
            contentRV.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(models);
            mAdapter.notifyDataSetChanged();
        }
        ViewUtils.move2Position(manager, contentRV, ListUtils.getSize(models) - 1);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        String url = null;
        if (id == R.id.successfulTV) {
            url = "https://m.eqxiu.com/s/bTnuQkKM";
            IntentUtils.linkCommonWeb(ct, url, "关于UU", null, null);
        } else if (id == R.id.experienceTV) {
            url = "http://113.105.74.140:8080/new/";
            IntentUtils.linkCommonWeb(ct, url, "功能介绍", null, null);
        } else if (id == R.id.serviceTV) {
            CommonUtil.setSharedPreferences(this, Constants.SET_CALL, true);
            Intent intent = new Intent("com.modular.main.FeedbackActivity");
            intent.putExtra("type", 1);
            intent.putExtra(AppConstant.EXTRA_URL, mConfig.help_url);
            intent.putExtra(AppConstant.EXTRA_TITLE, "用户反馈");
            startActivity(intent);
        } else if (R.id.inputImg == id) {
        }
    }

}
