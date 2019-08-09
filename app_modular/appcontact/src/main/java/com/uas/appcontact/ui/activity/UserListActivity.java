package com.uas.appcontact.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.User;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.utils.ToastUtil;
import com.core.app.AppConstant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appcontact.R;
import com.uas.appcontact.adapter.UserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserListActivity extends BaseActivity {
    private PullToRefreshListView mPullToRefreshListView;

    private List<User> mUsers;
    private UserAdapter mAdapter;

    private int mPageIndex = 0;

    private String mKeyWord;// 关键字(keyword)
    private int mSex;// 城市Id(cityId)
    private int mMinAge;// 行业Id(industryId)
    private int mMaxAge;// 职能Id(fnId)
    private int mShowTime;// 日期(days)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.search);
        if (getIntent() != null) {
            mKeyWord = getIntent().getStringExtra("key_word");
            mSex = getIntent().getIntExtra("sex", 2);
            mMinAge = getIntent().getIntExtra("min_age", 0);
            mMaxAge = getIntent().getIntExtra("max_age", 0);
            mShowTime = getIntent().getIntExtra("show_time", 0);
        }
        mUsers = new ArrayList<User>();
        mAdapter = new UserAdapter(mUsers, this);
        setContentView(R.layout.layout_pullrefresh_list);
        initView();
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_empty_view, null);
        mPullToRefreshListView.setEmptyView(emptyView);

        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setShowIndicator(false);

//        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//                requestData(true);
//            }
//        });

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
                intent.putExtra(AppConstant.EXTRA_USER_ID, mUsers.get((int) id).getUserId());
                startActivity(intent);
            }
        });
        requestData(true);
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", String.valueOf(mPageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        if (!TextUtils.isEmpty(mKeyWord)) {
            params.put("nickname", mKeyWord);
        }
        if (mSex != 2) {
            params.put("sex", String.valueOf(mSex));
        }

        if (mMinAge != 0) {
            params.put("minAge", String.valueOf(mMinAge));
        }

        if (mMaxAge != 0) {
            params.put("maxAge", String.valueOf(mMaxAge));
        }

        params.put("active", String.valueOf(mShowTime));

        StringJsonArrayRequest<User> request = new StringJsonArrayRequest<User>(mConfig.USER_QUERY, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                mPullToRefreshListView.onRefreshComplete();
            }
        }, new StringJsonArrayRequest.Listener<User>() {
            @Override
            public void onResponse(ArrayResult<User> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    mPageIndex++;
                    if (isPullDwonToRefersh) {
                        mUsers.clear();
                    }
                    List<User> datas = result.getData();
                    if (datas != null && datas.size() > 0) {
//                        addDatas(datas, mSex);
                        mUsers.addAll(datas);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                mPullToRefreshListView.onRefreshComplete();
            }
        }, User.class, params);
        addDefaultRequest(request);
    }

    private void addDatas(List<User> datas, int mSex) {
        switch (mSex) {
            case 0:
                for (int i = 0; i < datas.size(); i++) {
                    if (datas.get(i).getSex() != 0) {
                        datas.remove(i);
                        i--;
                    }
                }
                break;
            case 1:
                for (int i = 0; i < datas.size(); i++) {

                    if (datas.get(i).getSex() != 1)
                        datas.remove(i);
                }
                break;
            default:
                break;

        }
        mUsers.addAll(datas);
    }
}
