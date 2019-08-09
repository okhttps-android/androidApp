package com.uas.appme.settings.activity;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.SimpleView;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appme.R;
import com.uas.appme.settings.adapter.SystemAdminAdapter;
import com.uas.appme.settings.model.SystemAdminBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/6/8 11:39
 */
public class SystemAdminActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView {
    private final int GET_SYSTEM_ADMIN = 0x11;

    private RecyclerView mRecyclerView;
    private RefreshLayout mRefreshLayout;
    private AppCompatTextView mEmptyTextView;
    private List<SystemAdminBean> mSystemAdminBeanList;
    private SystemAdminAdapter mSystemAdminAdapter;
    private View mEmptyView;

    @Override
    protected int getLayout() {
        return R.layout.activity_system_admin;
    }

    @Override
    protected void initView() {
        setTitle(R.string.str_system_admin);

        mRecyclerView = $(R.id.system_admin_rv);
        mRefreshLayout = $(R.id.system_admin_refreshlayout);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(20));

        mSystemAdminBeanList = new ArrayList<>();
        mSystemAdminAdapter = new SystemAdminAdapter(mSystemAdminBeanList);
        mRecyclerView.setAdapter(mSystemAdminAdapter);

        mEmptyView = View.inflate(this, R.layout.common_empty_view, null);
        mEmptyTextView = mEmptyView.findViewById(R.id.emptyTv);
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initEvent() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getSystemAdmin();
            }
        });
    }

    @Override
    protected void initData() {
        getSystemAdmin();
    }

    private void getSystemAdmin() {
        mPresenter.httpRequest(this, CommonUtil.getAppBaseUrl(this),
                new HttpParams.Builder()
                        .url("mobile/getAdminUser.action")
                        .method(Method.POST)
                        .flag(GET_SYSTEM_ADMIN)
                        .addParam("master", CommonUtil.getSharedPreferences(this, "erp_master"))
                        .build());
    }

    @Override
    public void requestSuccess(int what, Object object) {
        try {
            mSystemAdminBeanList.clear();
            String result = object.toString();
            Log.d("systemadminresult", "success->" + result);

            if (TextUtils.isEmpty(result) || !JSONUtil.validate(result)) {
                mEmptyTextView.setText("系统管理员列表为空");
                mSystemAdminAdapter.setEmptyView(mEmptyView);
                return;
            }
            JSONObject resultObject = JSON.parseObject(result);
            JSONArray dataArray = resultObject.getJSONArray("data");
            if (dataArray == null || dataArray.size() == 0) {
                mEmptyTextView.setText("系统管理员列表为空");
                mSystemAdminAdapter.setEmptyView(mEmptyView);
                return;
            }

            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                if (dataObject != null) {
                    String name = JSONUtil.getText(dataObject, "name");
                    String mobile = JSONUtil.getText(dataObject, "mobile");
                    String position = JSONUtil.getText(dataObject, "position");

                    SystemAdminBean systemAdminBean = new SystemAdminBean(name, mobile, position);
                    mSystemAdminBeanList.add(systemAdminBean);
                }
            }
            if (mSystemAdminBeanList.size() == 0) {
                mEmptyTextView.setText("系统管理员列表为空");
                mSystemAdminAdapter.setEmptyView(mEmptyView);
                return;
            }
            mSystemAdminAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            mEmptyTextView.setText("系统管理员获取异常，请重试");
            mSystemAdminAdapter.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        Log.d("systemadminresult", "error->" + errorMsg);

        mEmptyTextView.setText(errorMsg);
        mSystemAdminAdapter.setEmptyView(mEmptyView);
    }

    @Override
    public void showLoading(String loadStr) {
        if (!mRefreshLayout.isRefreshing()) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(0);
        }
    }
}
