package com.xzjmyk.pm.activity.ui.message.uas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.DBManager;
import com.core.model.B2BMsg;
import com.core.widget.view.Activity.CommonWebviewActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class B2bMsgActivity extends BaseActivity {
    private BussinessDetailAdapter mAdapter;
    @ViewInject(R.id.list_business)
    private PullToRefreshListView mlist;
    private List<B2BMsg> mData = new ArrayList<B2BMsg>();
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MsgBroadcast.ACTION_MSG_UI_UPDATE)) {
                initData(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b2b_msg);
        Intent intent = new Intent(this, B2bMsgActivity.class);
        Log.i("Arison", "" + intent.toURI());
        initView();
        initData(false);
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mlist.isRefreshing()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mlist.setRefreshing(true);
                }
            });
        }
    }

    private void initView() {
        ViewUtils.inject(this);
        setTitle("商务消息");
        registerReceiver(mUpdateReceiver, new IntentFilter(MsgBroadcast.ACTION_MSG_UI_UPDATE));
    }

    private void initListener() {
        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                initData(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BussinessDetailAdapter.ViewHolder holder = (BussinessDetailAdapter.ViewHolder) view.getTag();

              /*  String phone = CommonUtil.getSharedPreferences(B2bMsgActivity.this, "user_phone");
                String password = CommonUtil.getSharedPreferences(B2bMsgActivity.this, "user_password");
                String url = "http://www.ubtob.com/signin#/redirect/" + phone + "/" + password;
                IntentUtils.webLinks(B2bMsgActivity.this, url, "我的商务");*/
                B2BMsg msg = new B2BMsg();
                Log.i("Arison", "id=" + holder.id);
                msg.setId(holder.id);
                msg.setMaster(CommonUtil.getSharedPreferences(B2bMsgActivity.this, "erp_master"));
                msg.setContent(holder.tv_content.getText().toString());
                msg.setHasRead(1);
                Log.i("Arison", "" + holder.tv_content.getText().toString());
                final DBManager db = new DBManager(B2bMsgActivity.this);
                db.updateB2b(msg);

                holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(B2bMsgActivity.this).
                                title("友情提示").content("您确定删除本条商务消息")
                                .negativeText("取消")
                                .positiveText("删除")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        B2BMsg msg = new B2BMsg();
                                        msg.setId(holder.id);
                                        msg.setMaster(CommonUtil.getSharedPreferences(B2bMsgActivity.this, "erp_master"));
                                        db.deleteB2b(msg);
                                        refreshUi();
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                    }
                                }).show();
                    }
                });
                refreshUi();
           
                Intent intent = new Intent();
                intent.setClass(mContext, CommonWebviewActivity.class);
                try {
                    if(!StringUtil.isEmpty(holder.tv_content.getText().toString())){
                        intent.putExtra("scan_url", holder.tv_content.getText().toString().split("#")[1]);
                        Log.d("image_url", holder.tv_content.getText().toString().split("#")[1]);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                startActivity(new Intent(BusinessDetailActivty.this, BusinessDetailInfoActivity.class));
            }
        });
    }

    private void refreshUi() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                initData(false);
            }
        });
    }

    public DBManager db;

    private void initData(boolean update) {
        db = new DBManager(this);
        String master = CommonUtil.getSharedPreferences(this, "erp_master");
        Log.i("Arison", "" + master);
        List<B2BMsg> data = db.queryB2bList(new String[]{master}, " b2b_master=? ");
        Log.i("Arison", "" + JSON.toJSONString(data));
        if (!ListUtils.isEmpty(data)) {
            if (mAdapter == null) {
                mData.clear();
                mData = data;
                mAdapter = new BussinessDetailAdapter(this, mData);
                mlist.setAdapter(mAdapter);
            } else {
                if (update) {
                    mData.add(0, data.get(0));
                }
                mData.clear();
                mData.addAll(data);
                mAdapter.notifyDataSetChanged();
            }
            if (mAdapter.getCount() == 0) {

            }
        } else {
            if (mAdapter != null) {
                mData.clear();
                mAdapter.notifyDataSetChanged();
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mlist.onRefreshComplete();
            }
        }, 1000);
        db.closeDB();
    }

    private class BussinessDetailAdapter extends BaseAdapter {
        private Context ct;
        private List<B2BMsg> mdata = new ArrayList<>();
        private LayoutInflater inflater;

        public BussinessDetailAdapter(Context ct, List<B2BMsg> data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mdata.size();
        }

        @Override
        public Object getItem(int position) {
            return mdata.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_business_msg, null);
                holder = new ViewHolder();
                holder.tv_content = (TextView) convertView.findViewById(R.id.b2b_msg_content);
                holder.tv_time = (TextView) convertView.findViewById(R.id.b2b_msg_time);
                holder.ll_root = (LinearLayout) convertView.findViewById(R.id.ll_root);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_content.setText(mdata.get(position).getContent());
            holder.tv_time.setText(mdata.get(position).getTime());
            holder.id = mdata.get(position).getId();
            if (mData.get(position).getHasRead() == 1) {
                holder.ll_root.setBackgroundResource(R.drawable.shape_crm_card_click);
            } else {
                holder.ll_root.setBackgroundResource(R.drawable.shape_crm_card);
            }

            return convertView;
        }


        class ViewHolder {
            int id;
            LinearLayout ll_root;
            TextView tv_content;
            TextView tv_time;
            ImageView iv_delete;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDB();
        unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MsgBroadcast.broadcastMsgUiUpdate(this);
    }
}
