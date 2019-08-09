package com.modular.appmessages.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.Method;
import com.modular.appmessages.R;
import com.modular.appmessages.activity.ApprovalActivity;
import com.modular.apputils.adapter.ApprovalListAdapter;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.ApprovalList;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskListFragment extends ViewPagerLazyFragment implements OnSmartHttpListener {
    private final int LOAD_TO_DO = 1;
    private final int LOAD_TO_ALREADY = 2;
    private final int LOAD_TO_ME = 3;

    private int tabItem;
    private SimpleRefreshLayout mSimpleRefreshLayout;
    private RecyclerView mRecyclerView;
    private ApprovalListAdapter mApprovalListAdapter;
    private int page=1;

    public static TaskListFragment newInstance(int tabItem) {
        LogUtil.i("gong", "tabItem=" + tabItem);
        Bundle args = new Bundle();
        TaskListFragment fragment = new TaskListFragment();
        args.putInt("tabItem", tabItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tabItem==1){
            loadData();
        }
    }

    @Override
    protected void LazyData() {
        initView();
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(getContext());
    }

    @Override
    protected int inflater() {
        return R.layout.simply_refresh_recyclerview;
    }


    public void searchByKey(String text) {
        if (TextUtils.isEmpty(text)) {
            mApprovalListAdapter.getFilter().filter("");
        } else {
            mApprovalListAdapter.getFilter().filter(text);
        }
    }


    private void initView() {
        tabItem = getArguments().getInt("tabItem", 0);
        mSimpleRefreshLayout = findViewById(R.id.mSimpleRefreshLayout);
        EmptyRecyclerView mEmptyRecyclerView = findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSimpleRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                loadData();
            }

            @Override
            public void onLoadMore() {
                page++;
                loadData();
            }
        });
        if (tabItem == 1) {
            mSimpleRefreshLayout.setEnabledPullUp(false);
        }
        loadData();
    }

    public void loadData() {
        if (!mSimpleRefreshLayout.isRefreshing()) {
            progressDialog.show();
        }
        switch (tabItem) {
            case 1:
                loadProcessToDo();
                break;
            case 2:
                loadProcessAlready();
                break;
            case 3:
                loadProcessMe();
                break;
        }
    }


    private void loadProcessToDo() {
        Parameter.Builder builder = new Parameter.Builder();
        builder.url("common/desktop/process/toDo.action")
                .record(LOAD_TO_DO)
                .addParams("count", "1000")
                .addParams("page", "1")
                .showLog(true)
                .saveLog(false)
                .mode(Method.GET);
        requestCompanyHttp(builder, this);
    }

    private void loadProcessAlready() {
        Parameter.Builder builder = new Parameter.Builder();
        builder.url("common/desktop/process/alreadyDo.action")
                .record(LOAD_TO_ALREADY)
                .addParams("count", String.valueOf(page * 30))
                .addParams("page", page)
                .addParams("isMobile", "1")
                .addParams("_do", "1").showLog(true)
                .mode(Method.GET);
        requestCompanyHttp(builder, this);
    }

    private void loadProcessMe() {
        Parameter.Builder builder = new Parameter.Builder();
        builder.url("common/desktop/process/alreadyLaunch.action")
                .record(LOAD_TO_ME)
                .addParams("count", String.valueOf(page * 30))
                .addParams("page", page)
                .addParams("isMobile", "1")
                .addParams("_do", "1").showLog(true)
                .mode(Method.GET);
        requestCompanyHttp(builder, this);
    }




    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {
        progressDialog.dismiss();
        mSimpleRefreshLayout.stopRefresh();
        if (!StringUtil.isEmpty(message)) {
            ToastUtil.showToast(ct, message);
        }
    }

    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        LogUtil.i("gong","onSuccess="+message);
        switch (what) {
            case LOAD_TO_DO:
                handlerToDo(JSONUtil.getJSONArray(message, "data"));
                break;
            case LOAD_TO_ALREADY:
                handlerToDo(JSONUtil.getJSONArray(message, "data"));
                break;
            case LOAD_TO_ME:
                handlerToDo(JSONUtil.getJSONArray(message, "data"));
                break;
        }
        mSimpleRefreshLayout.stopRefresh();
        progressDialog.dismiss();
    }

    private void handlerToDo(JSONArray array) throws Exception {
        if (ListUtils.isEmpty(array)) {
            showAdapter(null);
        } else {
            List<ApprovalList> approvalLists = new ArrayList<>();
            ApprovalList e = null;
            JSONObject object = null;
            for (int i = 0; i < array.size(); i++) {
                object = array.getJSONObject(i);
                e = new ApprovalList();
                e.setStatus(JSONUtil.getText(object, "JP_STATUS"));
                if (tabItem != 1 || e.getStatus().equals("待审批")) {
                    e.setCaller(JSONUtil.getText(object, "JP_CALLER"));
                    e.setLauncherName(JSONUtil.getText(object, "JP_LAUNCHERNAME"));
                    e.setName(JSONUtil.getText(object, "JP_NAME"));
                    e.setNodeId(JSONUtil.getText(object, "JP_NODEID"));
                    e.setDealTime(JSONUtil.getTime(object, "JP_LAUNCHTIME", "JN_DEALTIME"));
                    e.setMaster(JSONUtil.getText(object, "CURRENTMASTER"));
                    e.setImid(JSONUtil.getText(object, "EM_IMID"));
                    e.setDealResult(JSONUtil.getText(object, "JN_DEALRESULT"));
                    e.setNodeDealMan(JSONUtil.getText(object, "JP_NODEDEALMANNAME"));
                    e.setOperatedDescription(JSONUtil.getText(object, "JN_OPERATEDDESCRIPTION"));
                    approvalLists.add(setSubTitle(e));
                }
            }
            showAdapter(approvalLists);
        }
    }

    private ApprovalList setSubTitle(ApprovalList e) {
        String jpStatus = "等待我审批";
        int statusColor = R.color.approvaling;
        switch (tabItem) {
            case 2:
                jpStatus = e.getDealResult();
                if (!StringUtil.isEmpty(jpStatus)) {
                    if (jpStatus.startsWith("不同意") || jpStatus.startsWith("结束流程") || jpStatus.startsWith("未通过")) {
                        jpStatus = "未通过";
                        statusColor = R.color.red;
                    } else if (jpStatus.startsWith("变更处理人")) {
                        statusColor = R.color.done_approval;
                        if (!StringUtil.isEmpty(e.getOperatedDescription())) {
                            jpStatus = "变更处理人（" + e.getOperatedDescription() + ")";
                        } else {
                            jpStatus = "变更处理人";
                        }
                    } else {
                        jpStatus = "已审批";
                        statusColor = R.color.done_approval;
                    }
                }
                break;
            case 3:
                jpStatus = e.getStatus();
                if (!StringUtil.isEmpty(jpStatus)) {
                    if (jpStatus.equals("待审批")) {
                        statusColor = R.color.approvaling;
                        jpStatus = "等待" + e.getNodeDealMan() + getString(R.string.approvel);
                    } else if (jpStatus.equals("未通过")) {
                        statusColor = R.color.red;
                    } else if (jpStatus.equals("已审批")) {
                        statusColor = R.color.titleBlue;
                    }
                }
                break;
        }
        e.setSubTitleColor(statusColor);
        e.setSubTitle(jpStatus);
        return e;
    }


    private void showAdapter(List<ApprovalList> approvalLists) {
        LogUtil.i("gong","showAdapter="+ListUtils.getSize(approvalLists));
        if (mLoadedListener != null) {
            mLoadedListener.loaded();
        }
        if (ListUtils.isEmpty(approvalLists) && page > 1) {
            page--;
        }
        if (tabItem == 1) {
            if (mUpdateNumListener != null) {
                mUpdateNumListener.update(ListUtils.getSize(approvalLists));
            }
            Collections.sort(approvalLists, mComparator);
        }
        if (mApprovalListAdapter == null) {
            mApprovalListAdapter = new ApprovalListAdapter(getContext(),tabItem, approvalLists, mOnItemClickListener);
            mRecyclerView.setAdapter(mApprovalListAdapter);
        } else {
            if (page == 1) {
                mApprovalListAdapter.setApprovalLists(approvalLists);
            } else {
                mApprovalListAdapter.addApprovalLists(approvalLists);
            }
        }
    }

    private ApprovalListAdapter.OnItemClickListener mOnItemClickListener = new ApprovalListAdapter.OnItemClickListener() {
        @Override
        public void itemClick(ApprovalList model) {
            if (model != null) {
                String title = "";
                switch (tabItem) {
                    case 1:
                        title = getString(R.string.title_approval);
                        break;
                    case 2:
                        title = getString(R.string.task_confimed);
                        break;
                    case 3:
                        title = getString(R.string.task_request_me);
                        break;
                }
                Intent intent = new Intent(ct, ApprovalActivity.class);
                intent.putExtra("imid", model.getImid());
                intent.putExtra("title", title);
                intent.putExtra("type", tabItem);
                intent.putExtra("master", model.getMaster());
                intent.putExtra("nodeid", Integer.valueOf(model.getNodeId()));
                startActivity(intent);
            }
        }
    };

    private Comparator mComparator = new Comparator<ApprovalList>() {
        @Override
        public int compare(ApprovalList o1, ApprovalList o2) {
            return String.valueOf(o2.getDealTime()).compareTo(String.valueOf(o1.getDealTime()));
        }
    };


    private ApprovalListFragment.UpdateNumListener mUpdateNumListener;

    public void setUpdateNumListener(ApprovalListFragment.UpdateNumListener mUpdateNumListener) {
        this.mUpdateNumListener = mUpdateNumListener;
    }

    private ApprovalListFragment.LoadedListener mLoadedListener;

    public void setLoadedListener(ApprovalListFragment.LoadedListener mLoadedListener) {
        this.mLoadedListener = mLoadedListener;
    }

    public interface UpdateNumListener {
        void update(int num);
    }

    public interface LoadedListener {
        void loaded();
    }

}