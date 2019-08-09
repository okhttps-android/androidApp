package com.uas.appme.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;
import com.uas.appme.settings.adapter.PSettingListAdapter;
import com.uas.appme.settings.model.PersonSetingBean;

/**
 * Created by FANGlh on 2017/10/11.
 * function:
 */

public class PersonSettingListActivity extends BaseActivity {

    private PersonSetingBean mList;
    private MyListView psetting_list;
    private PSettingListAdapter myAdapter;
    private EmptyLayout mEmptyLayout;
    private String sc_industry;
    private String sc_industrycode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_setting_list_activity);
        initView();
        initData();
        initEvents();
    }


    private void initView() {
        mList = new PersonSetingBean();
        psetting_list = (MyListView) findViewById(R.id.psetting_list);
        myAdapter = new PSettingListAdapter(this);
        psetting_list.setAdapter(myAdapter);



        mEmptyLayout = new EmptyLayout(this,psetting_list);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
    }
    private void initData() {
        LogUtil.prinlnLongMsg("initData()","initData()");
        setTitle(getString(R.string.person_list));
        //接收商家类型
        sc_industry = getIntent().getStringExtra("sc_industry");
        sc_industrycode = getIntent().getStringExtra("sc_industrycode");
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreman")
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("serviceid",0)
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appStoreman", o.toString()+"");
                try {
                    mList = JSON.parseObject(o.toString(),PersonSetingBean.class);
                    myAdapter.setModel(mList);
                    myAdapter.notifyDataSetChanged();

                    if (mList == null || ListUtils.isEmpty(mList.getResult()))
                        mEmptyLayout.showEmpty();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    private void initEvents() {
        psetting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivityForResult(new Intent(ct,PersonSettingActivity.class)
                .putExtra("mdoel",JSON.toJSONString(mList))
                .putExtra("position",position),20);
                LogUtil.prinlnLongMsg("intentmodel",JSON.toJSONString(mList.getResult().get(position)));
            }
        });
        psetting_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showPopupWindow(position);
                return true;
            }
        });
    }

    private PopupWindow setWindow = null;
    private void showPopupWindow(int longPosition) {
        showMarkReadPW(longPosition);
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void showMarkReadPW(final int longPosition) {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.bsetting_delete, null);
        viewContext.findViewById(R.id.msg_delete_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDeleteHandle(longPosition);
            }
        });
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }
    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }
    private void doDeleteHandle(int longPosition) {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreDel")
                .add("keyfield", "sm_id")
                .add("id",mList.getResult().get(longPosition).getSm_id())
                .add("tablename","ServiceMan")
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("appStoreDel", o.toString() + "");
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        initData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        closePopupWindow();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == 20) {
            initData();
        }
    }
}
