/**
 * 
 */
package com.xzjmyk.pm.activity.ui.erp.activity;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.LargeValueFormatter;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.adapter.HListViewAdapter;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.utils.FlexJsonUtil;
import com.common.data.ObjectUtils;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;
import com.core.widget.view.ListViewInScroller;
import com.xzjmyk.pm.activity.ui.erp.view.MonPickerDialog;
import com.xzjmyk.pm.activity.ui.erp.view.MyMarkerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LiuJie 销售表
 */
public class SaleChartActivity extends BaseActivity implements OnClickListener{


	private String url;
	private Map<String, Object> params = new HashMap<String, Object>();
	private List<HashMap<String, Object>> dlist=new ArrayList<HashMap<String,Object>>();
	private ArrayList<ArrayList<String>> gridlists = new ArrayList<ArrayList<String>>();
	/**@注释：直方图  */
	@ViewInject(R.id.bc_scale_chart_view)
	private BarChart mChart;
	/**@注释：饼图  */
	@ViewInject(R.id.bc_scale_piechart_view)
	private PieChart pieChart;

	private BarData data;
	private ArrayList<BarDataSet> dataSets;
	private MyMarkerView mv;
	private float xZoom = 8f;//x轴显示数
	private Typeface tf;
	/**@注释：组装日期格式  */
	private Map<String, Object> dateMap=new HashMap<String, Object>();
	/**@注释：饼图  */
	private final static int TYPE_PINCHART=2;
	/**@注释：直方图  */
	private final static int TYPE_BARCHART=1;

	private Calendar calendar;
	private MonPickerDialog dialog;
	private Context ct;
	
	@ViewInject(R.id.tv_premonth)
	private TextView tv_premonth;
	@ViewInject(R.id.tv_date)
	private TextView tv_date;
	@ViewInject(R.id.tv_nextmonth)
	private TextView tv_nextmonth;
	private boolean falg=true;
	
	@ViewInject(R.id.lv_grid_dispaly)
	private ListViewInScroller lv_grid_dispaly;
	
	private HListViewAdapter hl_adapter;
	@ViewInject(R.id.layout)
	private HorizontalScrollView layout;
	private CustomProgressDialog progressDialog;


	private final static int LOAD_PIE_SUCCESS=1;

	private final static int LOAD_LINE_SUCCESS=2;
	
	private Handler handler_chart=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_PIE_SUCCESS:
				String result=msg.getData().getString("result");
				initPieChart(result);
				progressDialog.dismiss();
				break;
				case LOAD_LINE_SUCCESS:
					handlerUpdateUI(msg);
					break;
			default:
				break;
			}
		};
	};
	private int type;

	@Override
	public void onClick(View v) {
       switch (v.getId()) {
	    case R.id.tv_date:
	    	 dialog=new MonPickerDialog(
         			SaleChartActivity.this, 
         			DateSet, 
         			calendar.get(Calendar.YEAR), 
         			calendar.get(Calendar.MONTH),
         			calendar.get(Calendar.DAY_OF_MONTH));
         	dialog.show();
         	falg=true;
	    	break;
		   case R.id.tv_premonth:
			   if (id==7) {
				   calendar.add(Calendar.DATE, -1);
				   String date = new SimpleDateFormat("yyyy年MM月dd日").format(calendar.getTime());
				   tv_date.setText(date);
				   String year=new SimpleDateFormat("yyyyMM").format(calendar.getTime());
				   dateMap.put("YEAR", year);
				   dateMap.put("MONTH", calendar.get(Calendar.MONTH) + 1);
				   dateMap.put("DAY", calendar.get(Calendar.DATE));
			   }else{
				   calendar.add(Calendar.MONTH, -1);
				   String date = new SimpleDateFormat("yyyy年MM月").format(calendar.getTime());
				   tv_date.setText(date);
				   dateMap.put("YEAR", calendar.get(Calendar.YEAR));
				   dateMap.put("MONTH", calendar.get(Calendar.MONTH) + 1);
				   dateMap.put("DAY", calendar.get(Calendar.DATE));
			   }
			   initData();
			   break;
		   case R.id.tv_nextmonth:
			   if (id==7) {
				   calendar.add(Calendar.DATE, +1);
				   String date = new SimpleDateFormat("yyyy年MM月dd日").format(calendar.getTime());
				   tv_date.setText(date);
				   String year=new SimpleDateFormat("yyyyMM").format(calendar.getTime());
				   dateMap.put("YEAR", year);
				   dateMap.put("MONTH", calendar.get(Calendar.MONTH) + 1);
				   dateMap.put("DAY", calendar.get(Calendar.DATE));
			   }else{
				   calendar.add(Calendar.MONTH, +1);
				   String  date = new SimpleDateFormat("yyyy年MM月").format(calendar.getTime());
				   tv_date.setText(date);
				   dateMap.put("YEAR", calendar.get(Calendar.YEAR));
				   dateMap.put("MONTH", calendar.get(Calendar.MONTH) + 1);
				   dateMap.put("DAY", calendar.get(Calendar.DATE));
			   }
			   initData();
			   break;
		default:
			break;
		}
	}

	private int id;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	public void initView() {
		setContentView(R.layout.act_statistical_barchart_scale);
		ViewUtils.inject(this);
		ct=this;
		TAG="SaleChartActivity";
		progressDialog = CustomProgressDialog.createDialog(this);
	    setTitle("统计分析");

        calendar = Calendar.getInstance(); // 设置当前日期

		/**@注释：获取type 饼图，直方图  */
		Intent intent=getIntent();
		id=intent.getIntExtra("Id", 0);
		type=intent.getIntExtra("type", 1);

		if (id==7) {//銷售按天統計
			String date = new SimpleDateFormat("yyyy年MM月dd日").format(calendar.getTime());
			tv_date.setText(date);
			dateMap.put("YEAR",  new SimpleDateFormat("yyyyMM").format(calendar.getTime()));
			dateMap.put("MONTH", calendar.get(Calendar.MONTH) + 1);
			dateMap.put("DAY", calendar.get(Calendar.DATE));
			tv_nextmonth.setText("下一天");
			tv_premonth.setText("上一天");
		}else{
			String date = new SimpleDateFormat("yyyy年MM月").format(calendar.getTime());
			tv_date.setText(date);
			dateMap.put("YEAR", calendar.get(Calendar.YEAR));
			dateMap.put("MONTH", calendar.get(Calendar.MONTH) + 1);
			dateMap.put("DAY", calendar.get(Calendar.DATE));
			tv_nextmonth.setText("下一月");
			tv_premonth.setText("上一月");
		}

		tv_date.setOnClickListener(this);
		tv_premonth.setOnClickListener(this);
		tv_nextmonth.setOnClickListener(this);
		
		
        /**@注释：init chart  */
		mv = new MyMarkerView(this, R.layout.custom_marker_view);
		mChart.setMarkerView(mv);
		mChart.setDescription("");
		mChart.setMaxVisibleValueCount(60);
		mChart.setPinchZoom(false);
		
		mChart.setDrawBarShadow(false);
		mChart.setDrawGridBackground(false);

		XAxis xAxis = mChart.getXAxis();
		xAxis.setPosition(XAxisPosition.BOTTOM);
		// xAxis.setSpaceBetweenLabels(30);
		xAxis.setLabelsToSkip(0);
		xAxis.setAxisLineColor(getResources().getColor(R.color.red));
		xAxis.setSpaceBetweenLabels(8);
		xAxis.setAxisLineWidth(0f);
		
		xAxis.setDrawGridLines(false);
		mChart.getAxisLeft().setDrawGridLines(false);
		mChart.animateY(2500);
		
		 tf = Typeface.createFromAsset(getAssets(),
				"OpenSans-Regular.ttf");

		Legend l = mChart.getLegend();
		l.setPosition(LegendPosition.BELOW_CHART_LEFT);
		l.setTypeface(tf);

		XAxis xl = mChart.getXAxis();
		xl.setTypeface(tf);

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.setTypeface(tf);
		leftAxis.setValueFormatter(new LargeValueFormatter());
		leftAxis.setDrawGridLines(false);
		leftAxis.setSpaceTop(25f);
		leftAxis.setAxisLineColor(getResources().getColor(R.color.red));


		mChart.getAxisRight().setEnabled(false);
		mChart.setMaxVisibleValueCount(10);
		
		mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

			@Override
			public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
			}
			@Override
			public void onNothingSelected() {

			}
		});
		
		
		ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
		ArrayList<String> xVals = new ArrayList<String>();
		BarDataSet set1 = new BarDataSet(yVals1, "日销售额");
		set1.setColor(Color.rgb(164, 228, 251));
		dataSets = new ArrayList<BarDataSet>();
		dataSets.add(set1);
		data = new BarData(xVals, dataSets);
		mChart.setNoDataText("没有数据可显示");
		mChart.setData(data);
		mChart.invalidate();
		
		/**@注释：init pieChart */
		pieChart.setUsePercentValues(true);
	    pieChart.setDescription("");
	    pieChart.setDragDecelerationFrictionCoef(0.95f);
	    pieChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
	    pieChart.setDrawHoleEnabled(true);
	    pieChart.setHoleColorTransparent(true);
        
	    pieChart.setTransparentCircleColor(Color.WHITE);
        
	    pieChart.setHoleRadius(58f);
	    pieChart.setTransparentCircleRadius(61f);

	    pieChart.setDrawCenterText(true);   

	    pieChart.setRotationAngle(0);
	    pieChart.setRotationEnabled(true);
	    //pieChart.setOnChartValueSelectedListener(this);
	    pieChart.setCenterText("");
	    pieChart.setNoDataText("没有数据可显示");
	    pieChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);
        Legend lpie = pieChart.getLegend();
        lpie.setPosition(LegendPosition.RIGHT_OF_CHART);
        lpie.setXEntrySpace(7f);
        lpie.setYEntrySpace(5f);
	}
	

	public void initData() {
		gridlists.clear();
		if (hl_adapter!=null) {
			hl_adapter.notifyDataSetChanged();
		}
		url= CommonUtil.getAppBaseUrl(this)+"mobile/common/getStatsByConfig.action";

		String sessionId= CommonUtil.getSharedPreferences(this, "sessionId");
		progressDialog.show();
		/**@注释：加载不同图 */
		if (type==TYPE_BARCHART) {
			/**@注释：隐藏饼图，显示直方图  */
			mChart.setVisibility(View.VISIBLE);
			pieChart.setVisibility(View.GONE);
			params.put("Id",String.valueOf(id));
			params.put("sessionId",sessionId);
			if (!dateMap.isEmpty()) {
				String dateStr= FlexJsonUtil.toJson(dateMap);
				params.put("Config", dateStr);
			}
			LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
			headers.put("Cookie", "JSESSIONID="+ CommonUtil.getSharedPreferences(ct, "sessionId"));
			ViewUtil.httpSendRequest(ct, url, params, handler_chart, headers, LOAD_LINE_SUCCESS, null, null, "get");
		}else if(type==TYPE_PINCHART) {
			mChart.setVisibility(View.GONE);/**@注释：隐藏直方图，显示饼图  */
			pieChart.setVisibility(View.VISIBLE);
			params.put("Id",String.valueOf(id));
			params.put("sessionId",sessionId);
			if (!dateMap.isEmpty()) {
				String dateStr=FlexJsonUtil.toJson(dateMap);
				params.put("Config", dateStr);
			}
			LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
			headers.put("Cookie", "JSESSIONID="+ CommonUtil.getSharedPreferences(ct, "sessionId"));
			ViewUtil.httpSendRequest(ct, url, params,  handler_chart, headers, LOAD_PIE_SUCCESS, null, null, "get");
			pieChart.animateY(3000);
		}
	}

	@SuppressWarnings("unchecked")
	public void handlerUpdateUI(Message msg) {
		String result =msg.getData().getString("result");
		System.out.println("result:"+result);
		Map<String, Object> rMap=FlexJsonUtil.fromJson(result);
		String xKey=(String) rMap.get("keyField");
		String yKey=(String) rMap.get("valueField");
		String title=(String)rMap.get("title");
		setTitle(title);
		dlist=(List<HashMap<String, Object>>) rMap.get("data");
		
		
		/**@注释：初始化表格  */
		ArrayList<String> list = new ArrayList<String>();
        list.add("");
        list.add(xKey);
        list.add(yKey);
        gridlists.add(list);
        
		
		ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
		ArrayList<String> xVals = new ArrayList<String>();
		if (dlist!=null) {
			for (int i = 0; i < dlist.size(); i++) {
			String yString;
			try {
				yString = dlist.get(i).get(yKey).toString();
			} catch (Exception e) {
				yString="0";
				ViewUtil.ToastMessage(ct, "Y坐标解析失败！");
				e.printStackTrace();
			}
				BarEntry entry = new BarEntry(Float.valueOf(yString), i);
				entry.setData("销售详细信息展示！" + i);
				yVals1.add(entry);
			}
			
			/**@注释：x坐标  */
			DecimalFormat format =new DecimalFormat("#,##0.####;(#)");
			String xFieldStr="";
			for (int i = 0; i < dlist.size() ; i++) {
				xFieldStr=String.valueOf(dlist.get(i).get(xKey));
				xVals.add(xFieldStr);
				ArrayList<String> tList=new ArrayList<String>();
				tList.add(String.valueOf(i+1));
				if (dlist.get(i).get(xKey)!=null) {
					tList.add(get3BrStr(dlist.get(i).get(xKey).toString(),6));
				}else {
					tList.add(get3BrStr("未填写",6));
				}
				
				if (dlist.get(i).get(yKey)!=null) {
					tList.add(format.format(new BigDecimal(String.valueOf(dlist.get(i).get(yKey)))));
				}else{
					tList.add(format.format(new BigDecimal(0)));
				}
				gridlists.add(tList);
			}
		}
		
		
		BarDataSet set1 = new BarDataSet(yVals1, "月销售额(单位:人民币 元)");
		set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
		 //set1.setVisible(false);
	     //set1.setDrawValues(false);
		dataSets.clear();
		dataSets.add(set1);
		
		data=new BarData(xVals,dataSets);
		/**@注释：清空任何缩放  */
		mChart.zoom(0, 0, 0, 0);
		if (dlist!=null) {
			mChart.zoom(dlist.size()/xZoom, 0, 0, 0);
		}
		mChart.setData(data);
		//mChart.getLegend().setEnabled(false);
		mChart.invalidate();
		Log.i("Arison", "gridlists:" + JSON.toJSONString(gridlists));
		if (hl_adapter==null) {
			hl_adapter=new HListViewAdapter(ct, gridlists);
			lv_grid_dispaly.setAdapter(hl_adapter);
		}else{
		   hl_adapter.notifyDataSetChanged();
		}

		layout.setVisibility(View.VISIBLE);
		progressDialog.dismiss();
	}
	
	
	/**@注释：截取指定字符插入空格  */
	public String get3BrStr(String str,int size){
		    int length = str.length();  
		    if (length<=size) {
			return str;
		    }
	        int n=(length + size-1)/size;
		    int from = 0;  
		    int to = 0;  
		    StringBuilder builder = new StringBuilder();  
		    for (int i = 0; i < n; i++) {  
		        from = to;  
		        to = from + size;  
		        to = to > length ? length : to;  
		        if (i==n-1) {
					builder.append(str.subSequence(from, to));
				}else{
			      builder.append(str.subSequence(from, to)).append("\n");
				} 
		    }  
		    String result = builder.toString();
		    return result;
	}
	
	/**
	 * @author LiuJie
	 * @功能:初始化PieChart
	 */
	public void initPieChart(String result){
		System.out.println(TAG+" result:"+result);
		Map<String, Object> rMap=FlexJsonUtil.fromJson(result);
		String xKey=(String) rMap.get("keyField");
		String yKey=(String) rMap.get("valueField");
		String title=(String)rMap.get("title");
		setTitle(title);
		
		ArrayList<String> list = new ArrayList<String>();/**@注释：初始化表格  */
        list.add("");
        list.add(xKey);
        list.add(yKey);
        
        gridlists.add(list);
		
		@SuppressWarnings("unchecked")
		List<HashMap<String, Object>> plist=(List<HashMap<String, Object>>) rMap.get("data");
		int size=plist.size();
		/**@注释：百分比  */
		ArrayList<Entry> yVals1 = new ArrayList<Entry>();
		float total=0;
		for (int i = 0; i < size; i++) {
			if (!ObjectUtils.isEquals(plist.get(i).get(yKey), null)) {
				int v=(Integer) plist.get(i).get(yKey);
				total=total+v;
			}else {
				int v=0;
				total=total+v;
			}
		   
		}
		for (int i = 0; i < size; i++) {
			if (!ObjectUtils.isEquals(plist.get(i).get(yKey), null)) {
			 float v=(Integer) plist.get(i).get(yKey);
			 yVals1.add(new Entry(v/total,i));
			}else {
				 float v=0;
				 yVals1.add(new Entry(v/total,i));
			}
		}
		
		/**@注释：x横坐标  */
		DecimalFormat format =new DecimalFormat("#,##0.####;(#)");
		ArrayList<String> xVals = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			String y=(String) plist.get(i).get(xKey);
			xVals.add(y);
			ArrayList<String> tList=new ArrayList<String>();
			tList.add(String.valueOf(i+1));
			tList.add(plist.get(i).get(xKey).toString());
	        if (ObjectUtils.isEquals(plist.get(i).get(yKey), null)) plist.get(i).put(yKey, 0);
			tList.add(format.format(new BigDecimal(String.valueOf(plist.get(i).get(yKey)))));
			gridlists.add(tList);
		} 
		
		PieDataSet dataSet = new PieDataSet(yVals1, "月份");
	    dataSet.setSliceSpace(3f);
	    dataSet.setSelectionShift(5f);
		ArrayList<Integer> colors = new ArrayList<Integer>();
		colors.add(Color.rgb(165,42,42));
		colors.add(Color.rgb(240,128,128));
		colors.add(Color.rgb(128,128,0));
		colors.add(Color.rgb(250,128,114));
		colors.add(Color.rgb(0,128,128));
		colors.add(Color.rgb(184,134,11));
		colors.add(Color.rgb(153,50,204));
		colors.add(Color.rgb(051,204,051));
		colors.add(Color.rgb(255,055,055));
        dataSet.setColors(colors);
		PieData data = new PieData(xVals, dataSet);
	    data.setValueFormatter(new PercentFormatter());
	    data.setValueTextSize(11f);
	    data.setValueTextColor(Color.WHITE);
	    data.setValueTypeface(tf);
	    pieChart.setData(data);
	    pieChart.setDescription(title);
	    // undo all highlights
	     pieChart.highlightValues(null);
         pieChart.setDrawHoleEnabled(false);
	     pieChart.invalidate();
	     /**@注释：init adapter  */
	     if (hl_adapter==null) {
				hl_adapter=new HListViewAdapter(ct, gridlists);
				lv_grid_dispaly.setAdapter(hl_adapter);
			}else{
			   hl_adapter.notifyDataSetChanged();
			}
		layout.setVisibility(View.VISIBLE);
	}
	
	
	
	
	/**@注释：日期选择监听  */
	OnDateSetListener DateSet =new OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			if (falg) {

            calendar.set(Calendar.YEAR, year);/**@注释：此方法执行两次  */
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String date=new SimpleDateFormat("yyyy年MM月").format(calendar.getTime());   
		    tv_date.setText(date);
		    
		    dateMap.put("YEAR",  calendar.get(Calendar.YEAR));
			dateMap.put("MONTH",  calendar.get(Calendar.MONTH)+1);
			initData();
			falg=false;
			}
		}
		
		
	};
	 
}
