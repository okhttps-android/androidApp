package com.xzjmyk.pm.activity.ui.erp.view;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

/**
 * @author LiuJie
 * @功能:自定义的对话框
 */
public class PickDialog extends Dialog {
   
	private Context context;
	private LinearLayout layout;
	private String title;
	private ScrollView scrollView;
	private LinearLayout dataLayout;
	private LinearLayout blend_dialog_preview;
	//private PickDialogListener pickDialogListener;
	public PickDialog(Context context,String title) {
		super(context, R.style.blend_theme_dialog);
		this.context=context;
		this.title=title;
		//this.pickDialogListener=pickDialogListener;
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater =LayoutInflater.from(context);
		 layout = (LinearLayout) inflater.inflate(
				R.layout.act_list_product_view, null);
		TextView titleTextview = (TextView) layout.findViewById(R.id.blend_dialog_title);
		titleTextview.setText(title);
		TextView cancleTextView = (TextView) layout.findViewById(R.id.blend_dialog_cancle_btn);
		cancleTextView.setText(context.getResources().getString(R.string.no));
		blend_dialog_preview = (LinearLayout) layout.findViewById(R.id.blend_dialog_preview);
	    
		scrollView=(ScrollView) layout.findViewById(R.id.sv_content_data);
		dataLayout=(LinearLayout) layout.findViewById(R.id.ly_panel_data);
		
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
	
	/**@注释：initView  */
	public void initViewData(Map<String, Object> data){
		CreatePanel2(data);
		blend_dialog_preview.setVisibility(View.GONE);
		scrollView.setVisibility(View.VISIBLE);
	}
	/*"物料编号": "LJQ1214", 
    "物料名称": "连接器", 
    "物料规格": "AXE524127", 
    "单位": "PCS", 
    "类型": "连接器", 
    "承认状态": "未认可", 
    "总库存": 6, 
    "良品库存": 6, 
    "不良品库存": 0*/
	public void CreatePanel2(Map<String, Object> data){
		TextView num =(TextView) layout.findViewById(R.id.tv_pro_no_value);
		TextView name=(TextView) layout.findViewById(R.id.tv_pro_name_value);
		TextView rule=(TextView) layout.findViewById(R.id.tv_pro_rule_value);
		TextView units=(TextView) layout.findViewById(R.id.tv_pro_units_value);
		TextView type=(TextView) layout.findViewById(R.id.tv_pro_type_value);
		TextView status=(TextView) layout.findViewById(R.id.tv_pro_status_value);
		TextView total=(TextView) layout.findViewById(R.id.tv_pro_total_value);
		TextView liangpin=(TextView) layout.findViewById(R.id.tv_pro_accepted_value);
		TextView buliangpin=(TextView)layout.findViewById(R.id.tv_pro_rejects_value);
		
		 num.setText((data.get("物料编号")!=null)?data.get("物料编号").toString():"未填写");
		 name.setText((data.get("物料名称")!=null)?data.get("物料名称").toString():"未填写");
		 rule.setText((data.get("物料规格")!=null)?data.get("物料规格").toString():"未填写");
		 units.setText((data.get("单位")!=null)?data.get("单位").toString():"未填写");
		 type.setText((data.get("类型")!=null)?data.get("类型").toString():"未填写");
		 status.setText((data.get("承认状态")!=null)?data.get("承认状态").toString():"未填写");
		 total.setText((data.get("总库存")!=null)?data.get("总库存").toString():"未填写");
		 liangpin.setText((data.get("良品库存")!=null)?data.get("良品库存").toString():"未填写");
		 buliangpin.setText((data.get("不良品库存")!=null)?data.get("不良品库存").toString():"未填写");
		
	}
	
	
	
	public void CreatePanel(Map<String, Object> data){
		 @SuppressWarnings("rawtypes")
		Set set = data.entrySet();
		//创建
		for (@SuppressWarnings("rawtypes")
		Iterator iter = set.iterator(); iter.hasNext();) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
			Object value = entry.getValue();
			/**@注释：创建 RelativeLayout */
			RelativeLayout rLayout = new RelativeLayout(context);
			LayoutParams l = new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			//l.height = CommonUtil.dip2px(ct, 30);
			rLayout.setMinimumHeight(CommonUtil.dip2px(context, 30));
			TextView tView = new TextView(context);
			tView.setId((int)(Math.random() * 100) + 1);
			LayoutParams tv = new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			tv.addRule(RelativeLayout.CENTER_VERTICAL);
			tv.leftMargin = 30;
			tv.rightMargin = 10;
			tView.setWidth(CommonUtil.dip2px(context, 90));
			tView.setMaxWidth(CommonUtil.dip2px(context, 100));
			tView.setTextSize(16);
			tView.setGravity(Gravity.RIGHT);
			//值
			tView.setText(key+":");
			tView.setLayoutParams(tv);
			rLayout.addView(tView);
			
			TextView mView=new TextView(context);
			LayoutParams mp = new LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			mp.addRule(RelativeLayout.CENTER_VERTICAL);
			mp.addRule(RelativeLayout.RIGHT_OF, tView.getId());
			mp.leftMargin = 30;
			mp.rightMargin = 20;
			mView.setTextSize(16);
			mView.setText(value+"");
			mView.setLayoutParams(mp);
			rLayout.addView(mView);
			
			rLayout.setLayoutParams(l);
			dataLayout.addView(rLayout);
		}
	}

	
	public interface PickDialogListener {
		public void onLeftBtnClick();
		public void onRightBtnClick();
		public void onListItemClick(int position, String string);
		public void onListItemLongClick(int position, String string);
		public void onCancel();
	}


}
