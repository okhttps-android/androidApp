package com.modular.appmessages.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.Method;
import com.modular.appmessages.R;
import com.modular.appmessages.model.ApprovalList;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * 未完成
 * 1.搜索
 * 2.已审批的状态显示
 * Created by Bitlike on 2018/1/31.
 */

public class ApprovalListActivity extends BaseNetActivity implements View.OnClickListener, OnSmartHttpListener {
    private final int LOAD_TO_DO = 1;
    private final int LOAD_TO_ALREADY = 2;
    private final int LOAD_TO_ME = 3;

    private PullToRefreshListView mListView;
    private EmptyLayout mEmptyLayout;
    private ApprovalListAdapter mApprovalListAdapter;

    private boolean refresh;
    private int tab;
    private RelativeLayout tv_process_already_rl;
    private RelativeLayout tv_process_rl;
    private RelativeLayout tv_process_me_rl;
    private TextView tv_process_un_num;
    private VoiceSearchView mVoiceSearchView;

    @Override
    public int getLayoutRes() {
        return  R.layout.activity_process_msg;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_process_msg;
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(this);
    }

    @Override
    protected void init() throws Exception {
        tab = 1;
        initView();
        initLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tab == 1) {
            mVoiceSearchView.setText("");
            loadProcessToDo();
        }

    }

    private void initLoad() {
        loadProcessToDo();
    }


    private void initView() {
        mVoiceSearchView = findViewById(R.id.voiceSearchView);
        mListView = findViewById(R.id.lv_process);
        mEmptyLayout = new EmptyLayout(this, mListView.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);

        initActionbar();

        mVoiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mApprovalListAdapter != null) {
                    if (TextUtils.isEmpty(s)) {
                        mApprovalListAdapter.getFilter().filter("");
                    } else {
                        mApprovalListAdapter.getFilter().filter(s.toString());
                    }
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mApprovalListAdapter != null) {
                    ApprovalList model = mApprovalListAdapter.getItem((int) id);
                    if (model != null) {
                        String title = "";
                        switch (tab) {
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
                        intent.putExtra("type", tab);
                        intent.putExtra("master", model.getMaster());
                        intent.putExtra("nodeid", Integer.valueOf(model.getNodeId()));
                        startActivity(intent);
                    }
                }
            }
        });
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                switch (tab) {
                    case 1:
                        refresh = true;
                        loadProcessToDo();
                        break;
                    case 2:
                        loadProcessAlready(page = 1);
                        break;
                    case 3:
                        loadProcessMe(page = 1);
                        break;
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                switch (tab) {
                    case 1:
                        loadProcessToDo();
                        break;
                    case 2:
                        loadProcessAlready(++page);
                        break;
                    case 3:
                        loadProcessMe(++page);
                        break;
                }
            }
        });

    }


    private void initActionbar() {
        View view = LayoutInflater.from(ct).inflate(R.layout.process_header, null);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        tv_process_already_rl = (RelativeLayout) view.findViewById(R.id.tv_process_already_rl);
        tv_process_rl = (RelativeLayout) view.findViewById(R.id.tv_process_rl);
        tv_process_me_rl = (RelativeLayout) view.findViewById(R.id.tv_process_me_rl);
        tv_process_un_num = (TextView) view.findViewById(R.id.tv_process_un_num);
        bar.setCustomView(view);
        tv_process_already_rl.setOnClickListener(this);
        tv_process_me_rl.setOnClickListener(this);
        tv_process_rl.setOnClickListener(this);
        tv_process_rl.setSelected(true);
        view.findViewById(R.id.back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.back == id) {
            onBackPressed();
        } else if (id == R.id.tv_process_rl) {
            tab = 1;
            setClick();
            loadProcessToDo();
            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        } else if (id == R.id.tv_process_already_rl) {
            tab = 2;
            setClick();
            loadProcessAlready(page = 1);
            mListView.setMode(PullToRefreshBase.Mode.BOTH);
        } else if (id == R.id.tv_process_me_rl) {
            tab = 3;
            setClick();
            loadProcessMe(page = 1);
            mListView.setMode(PullToRefreshBase.Mode.BOTH);

        }
    }

    private void setClick() {
        tv_process_already_rl.setSelected(false);
        tv_process_me_rl.setSelected(false);
        tv_process_rl.setSelected(false);
        switch (tab) {
            case 1:
                tv_process_rl.setSelected(true);
                break;
            case 2:
                tv_process_already_rl.setSelected(true);
                break;
            case 3:
                tv_process_me_rl.setSelected(true);
                break;
        }
    }


    private void loadProcessToDo() {
        showProgress();
        String emCode = CommonUtil.getMaster();
        Parameter.Builder builder = new Parameter.Builder();
        builder.url("common/desktop/process/toDo.action")
                .record(LOAD_TO_DO)
                .addParams("count", "1000")
                .addParams("page", "1")
                .showLog(true)
                .saveLog(refresh && !StringUtil.isEmpty(emCode) && emCode.equals("DATACENTER"))
                .mode(Method.GET);
        requestCompanyHttp(builder, this);
        if (refresh) {
            refresh = false;
        }
    }

    private void loadProcessAlready(int page) {
        showProgress();
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

    private void loadProcessMe(int page) {
        showProgress();
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
    public void onSuccess(int what, String message, Tags tag) throws Exception {
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
        mVoiceSearchView.setText("");
        mListView.onRefreshComplete();
        dismissProgress();
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {
        dismissProgress();
        mListView.onRefreshComplete();
        if (!StringUtil.isEmpty(message)) {
            ToastUtil.showToast(ct, message);
        }
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
                if (tab != 1 || e.getStatus().equals("待审批")) {
                    LogUtil.i("e.getStatus()=" + e.getStatus());
                    LogUtil.i("tab=" + tab);
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
                    approvalLists.add(e);
                }
            }
            showAdapter(approvalLists);
        }
    }

    private int page;

    private void showAdapter(List<ApprovalList> approvalLists) {
        ToastUtil.showToast(ct, getString(R.string.common_up_finish));
        if (ListUtils.isEmpty(approvalLists) && page <= 1) {
            mEmptyLayout.showEmpty();
            if (tab == 1) {
                tv_process_un_num.setVisibility(View.GONE);
            }
        } else {
            if (tab == 1) {
                tv_process_un_num.setVisibility(View.VISIBLE);
                int numSize = ListUtils.getSize(approvalLists);
                tv_process_un_num.setText(numSize > 99 ? "99+" : (numSize <= 0 ? "" : String.valueOf(numSize)));
                Collections.sort(approvalLists, mComparator);
            }
        }
        if (mApprovalListAdapter == null) {
            mApprovalListAdapter = new ApprovalListAdapter(approvalLists);
            mListView.setAdapter(mApprovalListAdapter);
        } else {
            mApprovalListAdapter.setApprovalLists(approvalLists);
        }
    }

    private Comparator mComparator = new Comparator<ApprovalList>() {

        @Override
        public int compare(ApprovalList o1, ApprovalList o2) {
            return String.valueOf(o2.getDealTime()).compareTo(String.valueOf(o1.getDealTime()));
        }
    };


    private class ApprovalListAdapter extends BaseAdapter implements Filterable {
        private List<ApprovalList> approvalLists;
        private List<ApprovalList> showModels;

        public ApprovalListAdapter(List<ApprovalList> approvalLists) {
            this.approvalLists = approvalLists;
            this.showModels = this.approvalLists;
        }

        public void setApprovalLists(List<ApprovalList> approvalLists) {
            this.approvalLists = approvalLists;
            this.showModels = this.approvalLists;
            notifyDataSetChanged();
        }

        public void addApprovalLists(List<ApprovalList> approvalLists) {
            if (ListUtils.isEmpty(this.approvalLists)) {
                setApprovalLists(approvalLists);
            } else {
                this.approvalLists.addAll(approvalLists);
                this.showModels = this.approvalLists;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(this.showModels);
        }

        @Override
        public ApprovalList getItem(int position) {
            return ListUtils.getSize(this.showModels) > position ? this.showModels.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("WrongViewCast")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler mViewHodler = null;
            if (convertView == null) {
                mViewHodler = new ViewHodler();
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_process_state, null);
                mViewHodler.tv_name = convertView.findViewById(R.id.tv_name);
                mViewHodler.tv_date = convertView.findViewById(R.id.tv_date);
                mViewHodler.tv_status = convertView.findViewById(R.id.tv_status);
                mViewHodler.photo_img = convertView.findViewById(R.id.photo_img);
                ImageView photo_me = convertView.findViewById(R.id.photo_me);
                photo_me.setVisibility(View.GONE);
                convertView.setTag(mViewHodler);
            } else {
                mViewHodler = (ViewHodler) convertView.getTag();
            }
            ApprovalList model = getItem(position);

            String jpName = model.getName() == null ? "" : (model.getName().replace("流程", ""));


            String name = "";
            if (tab == 3) {
                name = "我" + "的" + jpName;
            } else {
                name = model.getLauncherName() + "的" + jpName;
            }
            mViewHodler.tv_name.setText(name);
            mViewHodler.tv_date.setText(DateFormatUtil.long2Str(model.getDealTime(), "MM-dd HH:mm") + "");
            String jpStatus = "等待我审批";
            int statusColor = R.color.approvaling;
            switch (tab) {
                case 2:
                    jpStatus = model.getDealResult();
                    if (!StringUtil.isEmpty(jpStatus)) {
                        if (jpStatus.startsWith("不同意") || jpStatus.startsWith("结束流程") || jpStatus.startsWith("未通过")) {
                            jpStatus = "未通过";
                            statusColor = R.color.red;
                        } else if (jpStatus.startsWith("变更处理人")) {
                            statusColor = R.color.done_approval;
                            if (!StringUtil.isEmpty(model.getOperatedDescription())) {
                                jpStatus = "变更处理人（" + model.getOperatedDescription() + ")";
                            } else {
                                jpStatus = "变更处理人";
                            }
                        } else {
                            jpStatus = "已审批";
                            statusColor = R.color.titleBlue;
                        }
                    }
                    break;
                case 3:
                    jpStatus = model.getStatus();
                    if (!StringUtil.isEmpty(jpStatus)) {
                        if (jpStatus.equals("待审批")) {
                            statusColor = R.color.approvaling;
                            jpStatus = "等待" + model.getNodeDealMan() + getString(R.string.approvel);
                        } else if (jpStatus.equals("未通过")) {
                            statusColor = R.color.red;
                        } else if (jpStatus.equals("已审批")) {
                            statusColor = R.color.titleBlue;
                        }
                    }
                    break;
            }
            mViewHodler.tv_status.setTextColor(getResources().getColor(statusColor));
            mViewHodler.tv_status.setText(jpStatus);

            if (!StringUtil.isEmpty(model.getImid())) {
                AvatarHelper.getInstance().display(model.getImid(), mViewHodler.photo_img, true, false);//显示圆角图片
            } else {
                String imageUri = "drawable://" + R.drawable.common_header_boy;
                AvatarHelper.getInstance().display(imageUri, mViewHodler.photo_img, true);
            }
            return convertView;
        }


        private class ViewHodler {
            private TextView tv_name;
            private TextView tv_date;
            private TextView tv_status;
            private ImageView photo_img;
        }


        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults searchResults = new FilterResults();
                    List<ApprovalList> values = new ArrayList<>();
                    if (constraint == null || constraint.length() <= 0) {
                        values = approvalLists;
                    } else {
                        for (ApprovalList e : approvalLists) {
                            if (e.hasContext(constraint))
                                values.add(e);
                        }
                    }
                    searchResults.values = values;
                    searchResults.count = values.size();
                    return searchResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    showModels = (List<ApprovalList>) results.values;
                    notifyDataSetChanged();
                    if (mApprovalListAdapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                }
            };
        }
    }
}
