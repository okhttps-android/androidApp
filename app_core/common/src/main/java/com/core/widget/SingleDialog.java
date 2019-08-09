package com.core.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.core.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LiuJie
 * @功能:自定义的对话框
 */
public class SingleDialog extends Dialog {
   
	private Context context;
	private LinearLayout layout;
	private String title;
	private ListView mListView;
	private SimpleAdapter adapter;
	/**@注释：进度条  */
	private LinearLayout blend_dialog_preview;
	
	public PickDialogListener pickDialogListener;
	public SingleDialog(Context context, String title, PickDialogListener listener) {
		super(context, R.style.blend_theme_dialog);
		this.context=context;
		this.title=title;
		this.pickDialogListener=listener;
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater =LayoutInflater.from(context);
		 layout = (LinearLayout) inflater.inflate(
				R.layout.act_list_single_view, null);
		TextView titleTextview = (TextView) layout.findViewById(R.id.blend_dialog_title);
		TextView cancleTextView = (TextView) layout.findViewById(R.id.blend_dialog_cancle_btn);
		blend_dialog_preview = (LinearLayout) layout.findViewById(R.id.blend_dialog_preview);
		mListView=(ListView) layout.findViewById(R.id.lv_master);
		titleTextview.setText(title);
		cancleTextView.setText(context.getResources().getString(R.string.no));
		
		/**@注释：cancle button  */
		cancleTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
		
		this.setCanceledOnTouchOutside(true);
		this.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dismiss();
			}
		});
		this.setContentView(layout);
	}
	
	public List<String> items;
	/**@注释：initView  */
	public void initViewData(List<String> lists){
		items=lists;
		blend_dialog_preview.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		if (adapter==null) {
			adapter=new SimpleAdapter(context, lists);
			mListView.setAdapter(adapter);
		}else{
			adapter.notifyDataSetChanged();
		}
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(pickDialogListener!=null){
					pickDialogListener.onListItemClick(position, items.get(position));
				    adapter.setSelection(position);
				    //adapter.notifyDataSetChanged();
				    dismiss();
				}
			}});
		
		
	}
	
	public interface PickDialogListener {
		public void onListItemClick(int position, String value);
	}

	public class SimpleAdapter extends BaseAdapter{
		private Context context;
		private int cur_index=-1;
		private List<String> list=new ArrayList<String>();
		public SimpleAdapter(Context context, List<String> list){
			this.context=context;
			this.list=list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder =null;
			if(convertView==null){
				convertView=LayoutInflater.from(context).inflate(R.layout.item_master_tv_cn, null);
				holder=new Holder();
				holder.blend_dialog_list_item_textview=(TextView) convertView.findViewById(R.id.blend_dialog_list_item_textview);
				convertView.setTag(holder);
			}else{
				holder=(Holder) convertView.getTag();
			}
			holder.blend_dialog_list_item_textview.setText(list.get(position));
			
			if (cur_index==position) {
				holder.blend_dialog_list_item_textview.setTextColor(
						context.getResources().getColor(R.color.red));
			}else{
		
				holder.blend_dialog_list_item_textview.setTextColor(
						context.getResources().getColor(R.color.lightblack));
			}
			return convertView;
		}
		
		public void setSelection(int position){
			this.cur_index=position;
			notifyDataSetChanged();
		}
		class Holder{
			TextView blend_dialog_list_item_textview;

		}

	}

}
