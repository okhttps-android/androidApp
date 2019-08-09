package com.xzjmyk.pm.activity.ui.erp.activity;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.model.Approve;
import com.core.widget.EmptyLayout;
import com.xzjmyk.pm.activity.R;

public class ApproveDisplayActivity extends BaseActivity implements OnClickListener {
    
	@ViewInject(R.id.lv_logs_approve)
	private ListView lv_logs_approve;
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

	public void initView() {
		  setContentView(R.layout.act_log_approve_view);
	      ViewUtils.inject(this);
		  setTitle("审批日志");
		mEmptyLayout=new EmptyLayout(this,lv_logs_approve);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	public void initData() {
		Intent intent=getIntent();
	      @SuppressWarnings("unchecked")
		  List<Approve> logsList=(List<Approve>) intent.getSerializableExtra("logslist");
		  System.out.println("size:"+logsList.size());
		  if (adapter==null) {
			 adapter=new LogsAdapter(ct, logsList);
			 lv_logs_approve.setAdapter(adapter);
			  if (adapter.getCount()==0){
				  mEmptyLayout.setEmptyMessage("暂无数据");
				  mEmptyLayout.showEmpty();
			  }
		  }else{
			adapter.notifyDataSetChanged();
		  }
	}


public class LogsAdapter extends BaseAdapter{
		
		private Context ct;
		@SuppressWarnings("unused")
		private LayoutInflater inflater;
		private List<Approve> list;
		
		public LogsAdapter(Context ct,List<Approve> list) {
			this.ct=ct;
			this.list=list;
			this.inflater = LayoutInflater.from(ct);
		}

		@Override
		public int getCount() {
			return list!=null?list.size():0;
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
			Logs logs=null; 
			if (view==null) {
				logs=new Logs();
				view=LayoutInflater.from(ct).inflate(R.layout.item_logs_approve, parent,false);
				logs.NODENAME=(TextView) view.findViewById(R.id.tv_NODENAME_value);
				logs.DEALMAN=(TextView) view.findViewById(R.id.tv_DEALMAN_value);
				logs.DEALTIME=(TextView) view.findViewById(R.id.tv_DEALTIME_value);
				logs.LAUNCHERNAME=(TextView) view.findViewById(R.id.tv_LAUNCHERNAME_value);
				logs.LAUNCHTIME=(TextView) view.findViewById(R.id.tv_LAUNCHTIME_value);
				logs.STATUS=(TextView) view.findViewById(R.id.tv_STATUS_value);
				logs.REAMRK=(TextView) view.findViewById(R.id.tv_REAMRK_value);
				logs.RESULT=(TextView) view.findViewById(R.id.tv_RESULT_value);
				view.setTag(logs);
			}else{
				logs=(Logs) view.getTag();
			}
			
			logs.NODENAME.setText(list.get(position).getNODENAME());
			logs.DEALMAN.setText(list.get(position).getDEALMAN());
			logs.DEALTIME.setText(list.get(position).getDEALTIME());
			logs.STATUS.setText(list.get(position).getSTATUS());
			logs.RESULT.setText(list.get(position).getRESULT());
			
			logs.REAMRK.setText(list.get(position).getREAMRK());
			logs.LAUNCHERNAME.setText(list.get(position).getLAUNCHERNAME());
			logs.LAUNCHTIME.setText(new  SimpleDateFormat("yyyy-MM-sdd HH:mm:ss").format(list.get(position).getLAUNCHTIME()));
		
			return view;
		}
	
		class Logs{
            public TextView NODENAME;     
            public TextView DEALMAN;   
            public TextView STATUS;   
            public TextView DEALTIME;  
            public TextView REAMRK;     
            public TextView LAUNCHERNAME;   
            public TextView LAUNCHTIME;   
            public TextView RESULT;
		 }
		
		
	}

}
