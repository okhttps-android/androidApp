package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.core.base.BaseActivity;
import com.core.widget.EmptyLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.LogsEntity;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author :LiuJie 2015年6月17日 下午5:23:22
 * @注释:logsDispaly 日志信息展示
 */
public class LogsDisplayActivty extends BaseActivity implements OnClickListener {


    @ViewInject(R.id.lv_logs)
    private ListView lv_logs;
    public EmptyLayout mEmptyLayout;
    Context ct;

    private LogsAdapter adapter;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        setContentView(R.layout.act_log_list_view);
        ViewUtils.inject(this);
        ct = this;
        setTitle("操作日志");
        mEmptyLayout = new EmptyLayout(this, lv_logs);
    }

    public void initData() {
        Intent intent = getIntent();
        List<LogsEntity> logsList = (List<LogsEntity>) intent.getSerializableExtra("logslist");
        System.out.println("size:" + logsList.size());
        if (adapter == null) {
            adapter = new LogsAdapter(ct, logsList);
            lv_logs.setAdapter(adapter);
            if (adapter.getCount() == 0) {
                mEmptyLayout.setEmptyMessage("暂无数据!");
                mEmptyLayout.showEmpty();
            }
        } else {
            adapter.notifyDataSetChanged();
        }

    }

    public class LogsAdapter extends BaseAdapter {

        private Context ct;
        @SuppressWarnings("unused")
        private LayoutInflater inflater;
        private List<LogsEntity> list;

        public LogsAdapter(Context ct, List<LogsEntity> list) {
            this.ct = ct;
            this.list = list;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Logs logs = null;
            if (view == null) {
                logs = new Logs();
                view = LayoutInflater.from(ct).inflate(R.layout.item_logs_view, parent, false);
                logs.man = (TextView) view.findViewById(R.id.tv_man_value);
                logs.date = (TextView) view.findViewById(R.id.tv_date_value);
                logs.content = (TextView) view.findViewById(R.id.tv_content_value);
                logs.result = (TextView) view.findViewById(R.id.tv_result_value);

                view.setTag(logs);
            } else {
                logs = (Logs) view.getTag();
            }

            logs.man.setText(list.get(position).getMl_man());
            logs.date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(list.get(position).getMl_date()));
            logs.content.setText(list.get(position).getMl_content());
            logs.result.setText(list.get(position).getMl_result());

            return view;
        }

        class Logs {
            public TextView man;
            public TextView date;
            public TextView content;
            public TextView result;
        }


    }

}
