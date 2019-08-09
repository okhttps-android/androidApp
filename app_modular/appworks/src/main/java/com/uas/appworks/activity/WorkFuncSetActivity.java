package com.uas.appworks.activity;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpParams;
import com.uas.appworks.R;
import com.uas.appworks.adapter.WorkFuncSetParentAdapter;
import com.uas.appworks.model.bean.WorkMenuBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 工作台应用设置页面
 * @date 2017/11/14 15:25
 */

public class WorkFuncSetActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView {
    private RecyclerView mFuncRecyclerView;
    private List<WorkMenuBean> mWorkMenuBeans;
    private List<WorkMenuBean> mHideWorkMenuBeans;
    private WorkFuncSetParentAdapter mWorkFuncSetParentAdapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_work_plat_func_set;
    }

    @Override
    protected void initView() {
       setTitle(R.string.work_func_set);

        mFuncRecyclerView = $(R.id.work_plat_func_set_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        mFuncRecyclerView.setLayoutManager(linearLayoutManager);
        mFuncRecyclerView.setNestedScrollingEnabled(false);

        mWorkMenuBeans = new ArrayList<>();
        mHideWorkMenuBeans = new ArrayList<>();
        mWorkFuncSetParentAdapter = new WorkFuncSetParentAdapter(this, mWorkMenuBeans);
        mFuncRecyclerView.setAdapter(mWorkFuncSetParentAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        mPresenter.uasRequest(this, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
    }


    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void requestSuccess(int what, Object object) {
        if (what == Constants.LOAD_WORK_MENU_CACHE) {
            List<WorkMenuBean> menuTypeBeans = (List<WorkMenuBean>) object;
            mWorkMenuBeans.clear();
            for (int i = 0; i < menuTypeBeans.size(); i++) {
                WorkMenuBean workMenuBean = menuTypeBeans.get(i);
                if (workMenuBean.isModuleVisible()) {
                    mWorkMenuBeans.add(workMenuBean);
                } else {
                    mHideWorkMenuBeans.add(workMenuBean);
                }
            }
            mWorkFuncSetParentAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.sort_complete) {
            item.setEnabled(false);
            List<WorkMenuBean> workMenuBeans = mWorkFuncSetParentAdapter.getWorkMenuBeans();
            workMenuBeans.addAll(mHideWorkMenuBeans);
            String resultJson = JSON.toJSONString(workMenuBeans);
            CommonUtil.setUniqueSharedPreferences(this, Constants.WORK_MENU_CACHE, resultJson);
            toast(R.string.save_success);
            setResult(Constants.WORK_FUNC_SET);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    item.setEnabled(true);
                    if (WorkFuncSetActivity.this == null
                            || WorkFuncSetActivity.this.isDestroyed()
                            || WorkFuncSetActivity.this.isFinishing()) {
                        return;
                    }
                    finish();
                }
            }, 1000);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
