package com.uas.appme.settings.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
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

public class TableSetActivity extends BaseActivity implements View.OnClickListener {
    private MyListView mComList;
    private RelativeLayout mAddNewRl;
    private Button mSaveBt;
    private Button mDeleteBtn;
    private String updateData;
    private List<TableMode> tableList;
    private String as_companyid;
    private TableAdapter myAdapter;
    private String as_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_set_activity);
        initView();
        initData();
    }

    private void initData() {
        updateData = getIntent().getStringExtra("updateData");
        if(StringUtil.isEmpty(updateData)){
            mSaveBt.setVisibility(View.VISIBLE);
            mDeleteBtn.setVisibility(View.GONE);
            TableMode model = new TableMode();
            model.setAs_deskcode("");
            model.setAs_number("");
            model.setAs_type("");
            model.setAs_remark("");
//            model.setAs_id("0");
            model.setAs_companyid(as_companyid);
            model.setAs_booknumber("");
            model.setAs_maxperson("");
            tableList.add(model);
            myAdapter.notifyDataSetChanged();
        }else{
            mAddNewRl.setVisibility(View.GONE);
            mSaveBt.setText(getString(R.string.common_update_button));
            mSaveBt.setVisibility(View.VISIBLE);
            mDeleteBtn.setVisibility(View.VISIBLE);

            as_id = JSON.parseObject(updateData).getString("as_id");
            TableMode model = new TableMode();
            model.setAs_id(as_id);
            model.setAs_companyid(JSON.parseObject(updateData).getString("as_companyid"));
            model.setAs_deskcode(JSON.parseObject(updateData).getString("as_deskcode"));
            model.setAs_number(JSON.parseObject(updateData).getString("as_number"));
            model.setAs_type(JSON.parseObject(updateData).getString("as_type"));
            model.setAs_remark(JSON.parseObject(updateData).getString("as_remark"));
            model.setAs_booknumber(JSON.parseObject(updateData).getString("as_booknumber"));
            model.setAs_maxperson(JSON.parseObject(updateData).getString("as_maxperson"));
            tableList.add(model);
            myAdapter.notifyDataSetChanged();
        }
    }

    private void initView() {
        setTitle(getString(R.string.table_setting));

        mComList = (MyListView) findViewById(R.id.com_list);
        mAddNewRl = (RelativeLayout) findViewById(R.id.add_new_rl);
        mSaveBt = (Button) findViewById(R.id.save_bt);
        mDeleteBtn = (Button) findViewById(R.id.delete_btn);
        mAddNewRl.setOnClickListener(this);
        mSaveBt.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);

        myAdapter = new TableAdapter(this);
        tableList = new ArrayList<>();
        myAdapter.setModeList(tableList);
        mComList.setAdapter(myAdapter);
        as_companyid =  CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        LogUtil.prinlnLongMsg("fanglh", JSON.toJSONString(tableList));
        if (id == R.id.add_new_rl){
            if (!canAddOrSave()) return;
                TableMode model = new TableMode();
                model.setAs_deskcode("");
                model.setAs_number("");
                model.setAs_type("");
                model.setAs_remark("");
                model.setAs_id("0");
                model.setAs_companyid(as_companyid);
                model.setAs_maxperson("");
                model.setAs_booknumber("");
                tableList.add(model);
                myAdapter.notifyDataSetChanged();
        }else if (id == R.id.save_bt){
            if (!CommonUtil.isNetWorkConnected(this)) {
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            if (canAddOrSave()) {
                if (!StringUtil.isEmpty(updateData) && !StringUtil.isEmpty(as_id))
                    doUpdateOrDelete(0);
                else
                    doSaveToHttps();
            }
        }else if (id == R.id.delete_btn){
            doUpdateOrDelete(1);
        }
    }

    private void doUpdateOrDelete(final int type) {
        TableMode udModel = new TableMode();
        udModel.setAs_remark(tableList.get(0).getAs_remark());
        udModel.setAs_type(tableList.get(0).getAs_type());
        udModel.setAs_number(tableList.get(0).getAs_number());
        udModel.setAs_deskcode(tableList.get(0).getAs_deskcode());
        udModel.setAs_companyid(tableList.get(0).getAs_companyid());
        udModel.setAs_id(tableList.get(0).getAs_id());
        LogUtil.prinlnLongMsg("udModel",JSON.toJSONString(udModel));
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appDeskUpdate")
                .add("map",JSON.toJSONString(udModel))
                .add("type",type)
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appDeskUpdate", o.toString()+"");
                if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")) {
                    if (type == 0)
                        Toast.makeText(ct,getString(R.string.update_success),Toast.LENGTH_LONG).show();
                    else if (type == 1)
                        Toast.makeText(ct,getString(R.string.delete_succeed_notice1),Toast.LENGTH_LONG).show();

                    setResult(20);
                    finish();
                }
            }

            @Override
            public void onFailure(Object t) {
            }
        }));
    }

    private void doSaveToHttps() {

        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appBatchDesk")
                .add("map",JSON.toJSONString(tableList))
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appBatchDesk", o.toString()+"");
                if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")) {
                    Toast.makeText(ct,getString(R.string.save_success),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ct,TableListActivity.class));
                    finish();
                }else{
                    ToastMessage(getString(R.string.net_expection));
                }
            }

            @Override
            public void onFailure(Object t) {
            }
        }));
    }

    private Boolean canAddOrSave(){
        boolean canOrNot = false;
        int c_size = tableList.size();
        if (!StringUtil.isEmpty(tableList.get(c_size-1).getAs_deskcode()) &&
                !StringUtil.isEmpty(tableList.get(c_size-1).getAs_remark()) &&
                !StringUtil.isEmpty(tableList.get(c_size-1).getAs_number()) &&
                !StringUtil.isEmpty(tableList.get(c_size-1).getAs_booknumber())&&
                !StringUtil.isEmpty(tableList.get(c_size-1).getAs_maxperson())){
            canOrNot = true;
        }else {
            ToastMessage(getString(R.string.input_all_msg));
            canOrNot = false;
        }
        return canOrNot;
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
                tableView.maxperson_et = (FormEditText) convertView.findViewById(R.id.maxperson_et);
                convertView.setTag(tableView);

                tableView.maxperson_et.setFocusable(false);
                tableView.maxperson_et.setKeyListener(null);
                tableView.type_et.setFocusable(false);
                tableView.type_et.setKeyListener(null);
                tableView.deskcode_et.setFocusable(false);
                tableView.deskcode_et.setKeyListener(null);
                tableView.number_et.setFocusable(false);
                tableView.number_et.setKeyListener(null);
                tableView.remark_et.setFocusable(false);
                tableView.remark_et.setKeyListener(null);
                tableView.days_et.setFocusable(false);
                tableView.days_et.setKeyListener(null);

            }else {
                tableView = (TableView) convertView.getTag();
            }

            //clickevent
            tableView.type_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectType(position);
                }
            });
            tableView.deskcode_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    doInput(1,position);
                }
            });
            tableView.number_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doInput(2,position);
                }
            });
            tableView.remark_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doInput(3,position);
                }
            });
            tableView.days_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doInput(4,position);
                }
            });
            tableView.maxperson_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doInput(5,position);
                }
            });

            //showdata
            tableView.type_et.setText(modeList.get(position).getAs_type()+"");
            tableView.deskcode_et.setText(modeList.get(position).getAs_deskcode()+"");
            tableView.number_et.setText(modeList.get(position).getAs_number()+"");
            tableView.remark_et.setText(modeList.get(position).getAs_remark()+"");
            tableView.days_et.setText(modeList.get(position).getAs_booknumber());
            tableView.maxperson_et.setText(modeList.get(position).getAs_maxperson()+"");
            return convertView;
        }

        class TableView{
            FormEditText type_et,deskcode_et,number_et,remark_et,days_et,maxperson_et;
        }
    }
    private PopupWindow popupWindow = null;
    private void doInput(final int type, final int position) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.item_edit_location_pop, null);

        // 设置按钮的点击事件
        final EditText editname_et = (EditText) contentView.findViewById(R.id.editname_et);
        final EditText editname_et2 = (EditText) contentView.findViewById(R.id.editname_et2);
        TextView title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (type == 1){
            editname_et.setVisibility(View.VISIBLE);
            editname_et2.setVisibility(View.GONE);
            title_tv.setText(getString(R.string.pi_tablecode));
            editname_et.setText(tableList.get(position).getAs_deskcode()+"");
        } else if(type==2) {
            editname_et.setVisibility(View.GONE);
            editname_et2.setVisibility(View.VISIBLE);
            title_tv.setText(getString(R.string.pi_number));
            editname_et2.setHint(getString(R.string.common_input));
            editname_et2.setText(tableList.get(position).getAs_number()+"");
        } else if (type == 3){
            editname_et.setVisibility(View.VISIBLE);
            editname_et2.setVisibility(View.GONE);
            title_tv.setText(getString(R.string.pi_remark));
            editname_et.setText(tableList.get(position).getAs_remark()+"");
        }else if (type == 4){
            editname_et.setVisibility(View.GONE);
            editname_et2.setVisibility(View.VISIBLE);
            title_tv.setText(getString(R.string.pi_bookdays));
            editname_et2.setHint(getString(R.string.common_input));
            editname_et2.setText(tableList.get(position).getAs_booknumber()+"");
        }else if (type == 5){
            editname_et.setVisibility(View.GONE);
            editname_et2.setVisibility(View.VISIBLE);
            title_tv.setText(getString(R.string.pi_seatnumber));
            editname_et2.setHint(getString(R.string.common_input));
            editname_et2.setText(tableList.get(position).getAs_maxperson()+"");
        }
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 145);

        contentView.findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type==1){
                    if (StringUtil.isEmpty(editname_et.getText().toString())){
                        ToastMessage(getString(R.string.pi_tablecode));
                        return;
                    }else {
                        tableList.get(position).setAs_deskcode(editname_et.getText().toString());
                        myAdapter.notifyDataSetChanged();
                    }
                }else if (type==2){
                    if (StringUtil.isEmpty(editname_et2.getText().toString())){
                        ToastMessage(getString(R.string.pi_number));
                        return;
                    }else {
                        tableList.get(position).setAs_number(editname_et2.getText().toString());
                        myAdapter.notifyDataSetChanged();
                    }
                }else if (type == 3){
                    if (StringUtil.isEmpty(editname_et.getText().toString())){
                        ToastMessage(getString(R.string.pi_remark));
                        return;
                    }else {
                        tableList.get(position).setAs_remark(editname_et.getText().toString());
                        myAdapter.notifyDataSetChanged();
                    }
                }else if (type == 4){
                    if (StringUtil.isEmpty(editname_et2.getText().toString())){
                        ToastMessage(getString(R.string.pi_bookdays));
                        return;
                    }else {
                        tableList.get(position).setAs_booknumber(editname_et2.getText().toString());
                        myAdapter.notifyDataSetChanged();
                    }
                }else if (type == 5){
                    if (StringUtil.isEmpty(editname_et2.getText().toString())){
                        ToastMessage(getString(R.string.pi_seatnumber));
                        return;
                    }else {
                        tableList.get(position).setAs_maxperson(editname_et2.getText().toString());
                        myAdapter.notifyDataSetChanged();
                    }
                }
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(getResources().getDrawable(com.uas.appworks.R.drawable.pop_round_bg));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }

    private void showSelectType(final int position) {
        // 1是男，0是女，2是全部
        String[] sexs = new String[]{ getString(R.string.b_table), getString(R.string.m_table),getString(R.string.s_table)};
        int checkItem = 0;
        new AlertDialog.Builder(this).setTitle(getString(R.string.pi_tabletype))
                .setSingleChoiceItems(sexs, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ttype = null;
                        String code = null;
                        if (which == 0) {
                            ttype =  getString(R.string.b_table);
                            code = "A";
                        }else if (which == 1){
                            ttype =  getString(R.string.m_table);
                            code = "B";
                        }else {
                            ttype =  getString(R.string.s_table);
                            code = "C";
                        }
                       if (JSON.toJSONString(tableList).contains(code)){
                            ToastMessage(getString(R.string.table_repeat));
                            return;
                       }
                        tableList.get(position).setAs_deskcode(code);
                        tableList.get(position).setAs_type(ttype);
                        myAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
    }

    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(TableSetActivity.this, 1f);
            }
        });
    }
    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (StringUtil.isEmpty(updateData)) {
            getMenuInflater().inflate(R.menu.bsetting_more, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bsetting_more, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.booking_set_list){
            startActivity(new Intent(ct,TableListActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
