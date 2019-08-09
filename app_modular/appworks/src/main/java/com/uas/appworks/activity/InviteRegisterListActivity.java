package com.uas.appworks.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.interfac.OnVoiceCompleteListener;
import com.core.utils.CommonUtil;
import com.core.widget.ClearEditText;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.R;
import com.uas.appworks.adapter.InviteRegisterListAdapter;
import com.uas.appworks.model.bean.RegisterListBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 邀请注册明细页面
 * @date 2018/3/25 16:47
 */

public class InviteRegisterListActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView {
    private ClearEditText mSearchEt;
    private ImageView mVoiceIv;
    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;

    private int mState;
    private List<RegisterListBean> mRegisterListBeans;
    private InviteRegisterListAdapter mInviteRegisterListAdapter;
    private String mKeyWord = "", mListState = "";
    private int mPageIndex = 1, mPageSize = 20;

    @Override
    protected int getLayout() {
        return R.layout.activity_invite_register_list;
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            mState = intent.getIntExtra(Constants.FLAG.INVITE_REGISTER_LIST_STATE, -1);

            if (mState == Constants.FLAG.STATE_INVITE) {
                setTitle(R.string.invite_detail);
                mListState = "";
            } else if (mState == Constants.FLAG.STATE_REGISTER) {
                setTitle(R.string.register_detail);
                mListState = "done";
            } else if (mState == Constants.FLAG.STATE_UNREGISTER) {
                setTitle(R.string.unregister_detail);
                mListState = "todo";
            }
        }

        mSearchEt = (ClearEditText) $(R.id.invite_register_list_search_et);
        mVoiceIv = (ImageView) $(R.id.invite_register_list_search_voice_iv);
        mRefreshLayout = (RefreshLayout) $(R.id.invite_register_list_refresh);
        mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);

        mRecyclerView = (RecyclerView) $(R.id.invite_register_list_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));

        mRegisterListBeans = new ArrayList<>();
        mInviteRegisterListAdapter = new InviteRegisterListAdapter(this, mRegisterListBeans);
        mRecyclerView.setAdapter(mInviteRegisterListAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                if (!CommonUtil.isNetWorkConnected(mContext)) {
                    toast(R.string.networks_out);
                    mRefreshLayout.finishRefresh(500, false);
                } else {
                    mRefreshLayout.resetNoMoreData();
                    mPageIndex = 1;
                    requestList();
                }
            }
        });

        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                if (!CommonUtil.isNetWorkConnected(mContext)) {
                    toast(R.string.networks_out);
                    mRefreshLayout.finishLoadMore(500, false, false);
                } else {
                    mPageIndex++;
                    requestList();
                }
            }
        });

        mSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String text = mSearchEt.getText().toString().trim();
                    mKeyWord = text;
                    mPageIndex = 1;
                    showLoading("");
                    requestList();
                    return true;
                }
                return false;
            }
        });

        mVoiceIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.getVoiceText(mContext, mSearchEt, new OnVoiceCompleteListener() {
                    @Override
                    public void onVoiceComplete(String text) {
                        mKeyWord = text;
                        mPageIndex = 1;
                        showLoading("");
                        requestList();
                    }
                });
            }
        });

        mInviteRegisterListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                RegisterListBean registerListBean = mRegisterListBeans.get(position);
                if (registerListBean.getState() == RegisterListBean.STATE_REGISTER) {
                    Intent intent = new Intent();
                    intent.setClass(InviteRegisterListActivity.this, RegisterDetailActivity.class);
                    intent.putExtra(Constants.FLAG.REGISTERED_ENTERPRISE_INFO, registerListBean.getJson());
                    intent.putExtra(Constants.FLAG.REGISTERED_ENTERPRISE_FLAG, Constants.FLAG.REGISTERED_LIST);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void initData() {
        if (!CommonUtil.isNetWorkConnected(this)) {
            toast(R.string.networks_out);
            mInviteRegisterListAdapter.setEmptyView(R.layout.view_net_error, (ViewGroup) mRecyclerView.getParent());
        } else {
            showLoading("");
            requestList();
        }
    }

    private void requestList() {
        HttpRequest.getInstance().sendRequest(new ApiPlatform().getBaseUrl(),
                new HttpParams.Builder()
                        .url("public/invitation/records")
                        .method(Method.GET)
                        .setParams(getParams())
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        Log.d("registerlistsuccess", o.toString());
                        if (o != null) {
                            String result = o.toString();
                            if (!JSONUtil.validate(result)) {
                                hideLoading();
                                if (mPageIndex == 1) {
                                    mRegisterListBeans.clear();
                                    mInviteRegisterListAdapter.notifyDataSetChanged();
                                    mInviteRegisterListAdapter.setEmptyView(R.layout.view_empty, (ViewGroup) mRecyclerView.getParent());
                                }
                                return;
                            }
                            JSONObject resultObject = JSON.parseObject(result);
                            JSONArray contentArray = resultObject.getJSONArray("content");
                            if (contentArray == null || contentArray.size() == 0) {
                                if (mPageIndex == 1) {
                                    hideLoading();
                                    mRegisterListBeans.clear();
                                    mInviteRegisterListAdapter.notifyDataSetChanged();
                                    mInviteRegisterListAdapter.setEmptyView(R.layout.view_empty, (ViewGroup) mRecyclerView.getParent());
                                } else {
                                    progressDialog.dismiss();
                                    if (mRefreshLayout != null && mRefreshLayout.isLoading()) {
                                        mRefreshLayout.finishLoadmoreWithNoMoreData();
                                    }
                                }
                                return;
                            }

                            if (mPageIndex == 1) {
                                mRegisterListBeans.clear();
                            }
                            if (contentArray.size() < mPageSize && mPageIndex > 1 && mRefreshLayout != null && mRefreshLayout.isLoading()) {
                                mRefreshLayout.finishLoadmoreWithNoMoreData();
                            }
                            hideLoading();
                            for (int i = 0; i < contentArray.size(); i++) {
                                JSONObject contentObject = contentArray.getJSONObject(i);
                                if (contentObject != null) {
                                    RegisterListBean registerListBean = new RegisterListBean();
                                    registerListBean.setEnName(JSONUtil.getText(contentObject, "vendname"));
                                    registerListBean.setLinkman(JSONUtil.getText(contentObject, "vendusername"));
                                    registerListBean.setPhone(JSONUtil.getText(contentObject, "vendusertel"));
                                    long date = contentObject.getLongValue("date");
                                    if (date != 0) {
                                        registerListBean.setInviteDate(DateFormatUtil.long2Str(date, DateFormatUtil.YMD));
                                    }
                                    long registerDate = contentObject.getLongValue("registerDate");
                                    if (registerDate != 0) {
                                        registerListBean.setRegisterDate(DateFormatUtil.long2Str(registerDate, DateFormatUtil.YMD));
                                    }

                                    JSONObject userObject = contentObject.getJSONObject("user");
                                    if (userObject != null) {
                                        String inviteName = JSONUtil.getText(userObject, "userName");
                                        registerListBean.setInviteName(inviteName);
                                    }

                                    String active = JSONUtil.getText(contentObject, "active");
                                    if ("0".equals(active)) {
                                        registerListBean.setState(RegisterListBean.STATE_UNREGISTER);
                                    } else if ("1".equals(active)) {
                                        registerListBean.setState(RegisterListBean.STATE_REGISTER);
                                    }

                                    registerListBean.setJson(contentObject.toJSONString());

                                    mRegisterListBeans.add(registerListBean);
                                }
                            }
                            mInviteRegisterListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        hideLoading();
                        toast(failStr);
                        Log.d("registerlistfail", failStr);
                        if (mPageIndex > 1) {
                            mPageIndex--;
                        }
                    }
                });
    }

    private Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("enUU", CommonUtil.getEnuuLong(this));
        params.put("userUU", CommonUtil.getUseruuLong(this));
        params.put("userTel", MyApplication.getInstance().mLoginUser.getTelephone());
        params.put("page", mPageIndex);
        params.put("count", mPageSize);
        params.put("keyword", mKeyWord);
        params.put("_state", mListState);
        params.put("businessCode", CommonUtil.getSharedPreferences(this, Constants.CACHE.EN_BUSINESS_CODE));
        return params;
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(0);
        }
        if (mRefreshLayout != null && mRefreshLayout.isLoading()) {
            mRefreshLayout.finishLoadmore(0);
        }
    }

    @Override
    public void requestSuccess(int what, Object object) {

    }

    @Override
    public void requestError(int what, String errorMsg) {

    }
}
