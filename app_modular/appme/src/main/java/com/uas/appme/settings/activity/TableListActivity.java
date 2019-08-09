package com.uas.appme.settings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;
import com.uas.appme.settings.model.TableMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/11/22.
 * function:
 */

public class TableListActivity extends BaseActivity {
    private MyListView tableMylist;
    private TableAdapter myAdapter;
    private List<TableMode> modeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("桌位设置");
        setContentView(R.layout.person_setting_list_activity);
        initView();
        initData();
        initEvents();

    }

    private void initEvents() {
        tableMylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("fanglh","18:47");
                startActivityForResult(new Intent(ct, TableSetActivity.class)
                                .putExtra("updateData", JSON.toJSONString(modeList.get(position)))
                        ,20);
                Log.i("fanglh","18:48");
            }
        });

    }

    private void initData() {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appDeskList")
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appDeskList", o.toString()+"");
                try {
                    JSONArray array = JSON.parseObject(o.toString()).getJSONArray("result");
                    modeList = JSON.parseArray(array.toString(),TableMode.class);
                    if (ListUtils.isEmpty(modeList)) mEmptyLayout.showEmpty();
                    myAdapter.setModeList(modeList);
                    myAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Object t) {
            }
        }));
    }
    private EmptyLayout mEmptyLayout;
    private void initView() {
        tableMylist = (MyListView) findViewById(R.id.psetting_list);
        myAdapter = new TableAdapter(this);
        modeList = new ArrayList<>();

        mEmptyLayout = new EmptyLayout(this, tableMylist);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);

        myAdapter.setModeList(modeList);
        tableMylist.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == 20) {
            //TODO 重更新
            initData();
        }
    }
    private class TableAdapter extends BaseAdapter {
        private List<TableMode> modeList;
        private Context mContext;

        public List<TableMode> getModeList() {return modeList;}
        public void setModeList(List<TableMode> modeList) {this.modeList = modeList;}

        public TableAdapter(Context mContext){
            this.mContext = mContext;
        }
        @Override
        public int getCount() {
            return ListUtils.isEmpty(modeList) ? 0 : modeList.size();
        }
        @Override
        public Object getItem(int position) {
            return modeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TableView tableView = null;
            if (convertView == null){
                tableView = new TableView();
                convertView =  View.inflate(mContext, R.layout.table_item,null);
                tableView.type_et = (FormEditText)convertView.findViewById(R.id.type_et);
                tableView.deskcode_et = (FormEditText) convertView.findViewById(R.id.deskcode_et);
                tableView.number_et = (FormEditText) convertView.findViewById(R.id.number_et);
                tableView.remark_et = (FormEditText) convertView.findViewById(R.id.remark_et);
                tableView.days_et = (FormEditText) convertView.findViewById(R.id.days_et);
                convertView.setTag(tableView);
                tableView.type_et.setFocusable(false);
                tableView.type_et.setKeyListener(null);
                tableView.deskcode_et.setFocusable(false);
                tableView.deskcode_et.setKeyListener(null);
                tableView.number_et.setFocusable(false);
                tableView.number_et.setKeyListener(null);
                tableView.remark_et.setFocusable(false);
                tableView.remark_et.setKeyListener(null);

            }else {
                tableView = (TableView) convertView.getTag();
            }
            //showdata
            tableView.type_et.setText(modeList.get(position).getAs_type()+"");
            tableView.deskcode_et.setText(modeList.get(position).getAs_deskcode()+"");
            tableView.number_et.setText(modeList.get(position).getAs_number()+"");
            tableView.remark_et.setText(modeList.get(position).getAs_remark()+"");
            tableView.days_et.setText(modeList.get(position).getAs_booknumber()+"");
            return convertView;
        }

        class TableView{
            FormEditText type_et,deskcode_et,number_et,remark_et,days_et;
        }
    }
}
