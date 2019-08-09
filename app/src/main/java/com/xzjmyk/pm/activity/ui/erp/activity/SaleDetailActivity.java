package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.data.JSONUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.model.Approve;
import com.xzjmyk.pm.activity.ui.erp.model.DetailColumns;
import com.xzjmyk.pm.activity.ui.erp.model.LogsEntity;
import com.xzjmyk.pm.activity.ui.erp.model.PanelItems;
import com.core.net.http.ViewUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.view.ListViewInScroller;
import com.xzjmyk.pm.activity.ui.erp.view.PickDialog;
import com.core.utils.FlexJsonUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :LiuJie 2015年6月10日 上午8:43:55
 * @注释:订单明细
 */
public class SaleDetailActivity extends BaseActivity implements OnClickListener {

	/** @注释：url */
	private String url;
	private String id;
	/** @注释：params */
	private Map<String, Object> params = new HashMap<String, Object>();
	@ViewInject(R.id.ly_panel_data)
	private LinearLayout ly_panel_data;
	@ViewInject(R.id.lv_detail_data)
	private ListViewInScroller lv_detail_data;
//	@ViewInject(R.id.tv_title)
//	private TextView tv_title;
	@ViewInject(R.id.tv_log_handler)
	private TextView tv_log_handler;
	@ViewInject(R.id.tv_log_approve)
	private TextView tv_log_approve;

	private String caller;

	@ViewInject(R.id.tv_panel_title)
	private TextView tv_panel_title;
	@ViewInject(R.id.tv_detail_title)
	private TextView tv_detail_title;

	private DetailAdapter adapter;
	private List<PanelItems> panelItems;
	private Map<String, Object> panelMap;
	/** @注释：物料数据 */
	private Map<String, Object> dataMap = null;
	/** @注释：对话框 */
	private PickDialog pickDialog;

	public EmptyLayout mEmptyLayout;
	private Context ct;

	private final static int LOG_SUCCESS = 1;
	private final static int LOG_APPROVE_SUCCESS = 3;
	private final static int PRO_SUCCESS = 2;

	private final static int LOAD_PLANEL=4;

	private Handler sale_handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case Constants.APP_SOCKETIMEOUTEXCEPTION:
					if(JSONUtil.validate(msg.getData().getString("result"))){
						String messge=JSON.parseObject(msg.getData().getString("exception")).getString("exception");
						ViewUtil.ToastMessage(ct, messge);
					}
					mEmptyLayout.setEmptyMessage("暂无明细记录！");
					tv_panel_title.setVisibility(View.GONE);
					tv_detail_title.setVisibility(View.GONE);
					mEmptyLayout.showEmpty();
					break;
			   case LOG_SUCCESS:
				String reString = msg.getData().getString("result");
				   Log.i("liujie",reString);
				Map<String, Object> rMap = FlexJsonUtil.fromJson(reString);
				List<LogsEntity> logsList = FlexJsonUtil
						.fromJsonArray(FlexJsonUtil.toJson(rMap.get("logs")),
								LogsEntity.class);
				Intent it_log = new Intent(ct, LogsDisplayActivty.class);
				it_log.putExtra("logslist", (Serializable) logsList);
				startActivity(it_log);
				break;
			case PRO_SUCCESS:
				String result = msg.getData().getString("result");
				System.out.println("+  result=:" + result);
				Map<String, Object> pMap = FlexJsonUtil.fromJson(result);
				dataMap = null;
				if (dataMap == null) {
					dataMap = (Map<String, Object>) pMap.get("data");
					if (!dataMap.isEmpty()) {
						pickDialog = new PickDialog(ct, "物料信息");
						pickDialog.show();
						new Handler().postDelayed(new Runnable() {
							public void run() {
								pickDialog.initViewData(dataMap);
							}
						}, 100);
					} else {
						ToastMessage("服务器繁忙！");
					}
				} else {
					pickDialog.show();
				}
				break;
			case LOG_APPROVE_SUCCESS:
				/** @注释：list isNULL */
				reString = msg.getData().getString("result");
				System.out.println("data=:" + reString);
				rMap = FlexJsonUtil.fromJson(reString);
				List<Approve> appList = FlexJsonUtil.fromJsonArray(
						FlexJsonUtil.toJson(rMap.get("data")), Approve.class);
				Intent it_log_app = new Intent(ct, ApproveDisplayActivity.class);
				it_log_app.putExtra("logslist", (Serializable) appList);
				startActivity(it_log_app);
				break;

				case LOAD_PLANEL:
                       handlerUpdateUI(msg);
					break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	public void initView() {
		setContentView(R.layout.act_sale_detail_list);
		TAG = "SaleDetailActivity";
		ct=this;
		ViewUtils.inject(this);
		tv_log_handler.setOnClickListener(this);
		tv_log_approve.setOnClickListener(this);
		mEmptyLayout = new EmptyLayout(this, lv_detail_data);
	}

	public void initData() {
		setTitle("订单明细");
		ly_panel_data.removeAllViews();
		Intent intent = getIntent();
		caller = intent.getStringExtra("caller");
		String formCondition = intent.getStringExtra("formCondition");
		String gridCondition = intent.getStringExtra("gridCondition");
		String sessionId = CommonUtil.getSharedPreferences(this, "sessionId");
		url = CommonUtil.getAppBaseUrl(this) + "mobile/common/getPanel.action";
	    Log.i("saledetail","url"+url);
	    Log.i("saledetail","caller"+caller);
	    Log.i("saledetail","formCondition"+formCondition);
	    Log.i("saledetail","gridCondition"+gridCondition);
	    Log.i("saledetail", "sessionId" + sessionId);
		params.put("caller", caller);
		params.put("formCondition", formCondition);
		params.put("gridCondition", gridCondition);
		params.put("sessionId", sessionId);
		id = formCondition.split("=")[1];

		mEmptyLayout.showLoading();

		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, sale_handler, headers, LOAD_PLANEL, null, null, "get");
	}

	public void handlerUpdateUI(android.os.Message msg) {
		Bundle bundle = msg.getData();
		String result = bundle.getString("result");
		System.out.println(TAG + " result=" + result);
		Map<String, Object> dmap = FlexJsonUtil.fromJson(result);
		List<DetailColumns> detailColumns = FlexJsonUtil.fromJsonArray(
				FlexJsonUtil.toJson(dmap.get("detailColumns")),
				DetailColumns.class);

		List<Object> detailData = (List<Object>) dmap.get("detailDatas");
		/** @注释：创建面板 */
		/** @注释：避免多次刷新，导致多次创建主面板 */
		panelItems = new ArrayList<PanelItems>();
		panelItems = FlexJsonUtil.fromJsonArray(
				FlexJsonUtil.toJson(dmap.get("panelItems")), PanelItems.class);
		panelMap = new HashMap<String, Object>();
		panelMap = (Map<String, Object>) dmap.get("panelData");
		if (panelItems != null && panelMap != null) {
			CreatePanel(panelItems, panelMap);
		}

		if (adapter == null) {
			adapter = new DetailAdapter(ct, detailData, detailColumns);
			lv_detail_data.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
		if (adapter.getCount() == 0) {
			mEmptyLayout.setEmptyMessage("暂无明细记录！");
			tv_panel_title.setVisibility(View.VISIBLE);
			tv_detail_title.setVisibility(View.VISIBLE);
			mEmptyLayout.showEmpty();
		} else {
			tv_panel_title.setVisibility(View.VISIBLE);
			tv_detail_title.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * @author LiuJie
	 * @功能:代码创建panel控件
	 */
	public void CreatePanel(List<PanelItems> items, Map<String, Object> data) {
		// 创建
		for (int i = 0; i < items.size(); i++) {
			if (!"ID".equals(items.get(i).getCaption())&&
					!"id".equals(items.get(i).getCaption())) {
				RelativeLayout rLayout = new RelativeLayout(ct);
				LayoutParams l = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				TextView tView = new TextView(ct);
				tView.setId(i + 1);
				LayoutParams tv = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				tv.addRule(RelativeLayout.CENTER_VERTICAL);
				tv.leftMargin = 30;
				tv.rightMargin = 10;
				tv.topMargin = 7;
				tv.bottomMargin = 7;
				tView.setWidth(CommonUtil.dip2px(ct, 90));
				tView.setMaxWidth(CommonUtil.dip2px(ct, 100));
				tView.setTextSize(16);
				tView.setGravity(Gravity.RIGHT);
				// 值
				tView.setText(items.get(i).getCaption() + ":");
				tView.setLayoutParams(tv);
				rLayout.addView(tView);

				TextView mView = new TextView(ct);
				LayoutParams mp = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				mp.addRule(RelativeLayout.CENTER_VERTICAL);
				mp.addRule(RelativeLayout.RIGHT_OF, tView.getId());
				mp.leftMargin = 30;
				mp.rightMargin = 20;
				mp.topMargin = 7;
				mp.bottomMargin = 7;
				mView.setTextSize(16);
				mView.setText(data.get(items.get(i).getField()) + "");
				mView.setLayoutParams(mp);
				rLayout.addView(mView);

				rLayout.setLayoutParams(l);
				ly_panel_data.addView(rLayout);
				View view = new View(ct);
				LayoutParams v = new LayoutParams(
						LayoutParams.MATCH_PARENT, CommonUtil.dip2px(ct, 1));
				view.setLayoutParams(v);
				view.setBackgroundColor(getResources().getColor(
						R.color.lightgray));
//				ly_panel_data.addView(view);
			}
		}
	}

	/** @注释：明细数据适配器 */
	public class DetailAdapter extends BaseAdapter {

		private Context ct;
		private LayoutInflater inflater;
		private List<Object> datas;
		private List<DetailColumns> columns;

		public DetailAdapter(Context ct, List<Object> datas,
				List<DetailColumns> columns) {
			this.ct = ct;
			this.datas = datas;
			this.columns = columns;
			this.inflater = LayoutInflater.from(ct);
		}

		@Override
		public int getCount() {
			if (datas == null || datas.isEmpty()) {
				return 0;
			}
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings({ "unchecked", "deprecation" })
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			/** @注释：临时数组 */
			TextView[] values = new TextView[columns.size()];
			Map<String, Object> dMap = new HashMap<String, Object>();
			dMap = (Map<String, Object>) datas.get(position);

			if (view == null) {
				view = inflater.inflate(R.layout.act_sale_detail_item, null);
				LinearLayout ly = (LinearLayout) view
						.findViewById(R.id.ly_sale_view);
				for (int i = 0; i < columns.size(); i++) {

					RelativeLayout rLayout = new RelativeLayout(ct);
					LayoutParams l = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);

					// l.topMargin=CommonUtil.dip2px(ct, 10);
					l.height = CommonUtil.dip2px(ct, 50);

					TextView tView = new TextView(ct);
					tView.setId(i + 1);
					LayoutParams tv = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					tv.addRule(RelativeLayout.CENTER_VERTICAL);
					tv.leftMargin = 10;
					tv.rightMargin = 10;
					tView.setWidth(CommonUtil.dip2px(ct, 90));
					tView.setMaxWidth(CommonUtil.dip2px(ct, 100));
					tView.setTextSize(16);
					tView.setGravity(Gravity.RIGHT);
					// 值
					tView.setText(columns.get(i).getCaption() + ":");
					tView.setLayoutParams(tv);
					rLayout.addView(tView);

					TextView mView = new TextView(ct);
					LayoutParams mp = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					mp.addRule(RelativeLayout.CENTER_VERTICAL);
					mp.addRule(RelativeLayout.RIGHT_OF, tView.getId());
					mp.leftMargin = 20;
					mp.rightMargin = 20;
					mView.setTextSize(16);

					final Object code = dMap.get(columns.get(i).getDataIndex())
							;
					// lisenter
					if ("PRODUCT".equals(columns.get(i).getRender())) {
						mView.setTextColor(getResources()
								.getColor(R.color.blue));
						mView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
						mView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								/** @注释：弹出对话框 */
								String url = CommonUtil.getAppBaseUrl(ct)
										+ "mobile/common/getProductDetail.action";
								String sessionId = CommonUtil
										.getSharedPreferences(
												SaleDetailActivity.this,
												"sessionId");
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("code", code);
								params.put("sessionId", sessionId);

								LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
								headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
								ViewUtil.httpSendRequest(ct, url, params, sale_handler, headers, PRO_SUCCESS, null, null, "get");
							}
						});
					}

					values[i] = mView;
//					 mView.setText(data.get(items.get(i).getField()));
					mView.setLayoutParams(mp);
					rLayout.addView(mView);

					View line = new View(ct);
					LayoutParams v = new LayoutParams(
							LayoutParams.MATCH_PARENT, CommonUtil.dip2px(ct, 1));
					line.setLayoutParams(v);
					line.setBackgroundColor(getResources().getColor(
							R.color.lightgray));
//					ly.addView(line);
					ly.addView(rLayout);
//					ly.setBackground(ct.getResources().getDrawable(
//							R.drawable.shape_linear_detail));
					ly.setBackgroundDrawable(ct.getResources().getDrawable(
							R.drawable.shape_linear_detail));

				}

				view.setTag(values);
			} else {
				values = (TextView[]) view.getTag();
			}

			for (int i = 0; i < columns.size(); i++) {
				if (dMap.get(columns.get(i).getDataIndex()) instanceof String) {
					values[i].setText(dMap.get(columns.get(i).getDataIndex())
							.toString());
				}
			}

			return view;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tv_log_handler:
			String sessionId = CommonUtil.getSharedPreferences(
					SaleDetailActivity.this, "sessionId");
			url = CommonUtil.getAppBaseUrl(this)
					+ "/common/getMessageLogs.action";
			params.clear();
			params.put("caller", caller);
			params.put("id", id);
			params.put("sessionId", sessionId);

			LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
			headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
			ViewUtil.httpSendRequest(ct, url, params, sale_handler, headers, LOG_SUCCESS, null, null, "get");
			break;
		case R.id.tv_log_approve:
			sessionId = CommonUtil.getSharedPreferences(
					SaleDetailActivity.this, "sessionId");
			url = CommonUtil.getAppBaseUrl(this)
					+ "mobile/common/getProcessNodes.action";
			params.clear();
			params.put("caller", caller);
			params.put("Id", id);
			params.put("sessionId", sessionId);

			 headers=new LinkedHashMap<>();
			headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
			ViewUtil.httpSendRequest(ct, url, params, sale_handler, headers, LOG_APPROVE_SUCCESS, null, null, "get");
			break;
		default:
			break;
		}
	}

	public ViewGroup CreateEmptyView(String message) {
		RelativeLayout rLayout = new RelativeLayout(ct);
		LayoutParams l = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		l.height = CommonUtil.dip2px(ct, 30);
		rLayout.setBackgroundColor(getResources().getColor(R.color.white));

		TextView tView = new TextView(ct);
		LayoutParams tv = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tv.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.leftMargin = 30;
		tv.rightMargin = 10;
		tView.setWidth(CommonUtil.dip2px(ct, 90));
		tView.setMaxWidth(CommonUtil.dip2px(ct, 100));
		tView.setTextSize(16);
		tView.setGravity(Gravity.CENTER);
		tView.setText(message);
		tView.setLayoutParams(tv);
		rLayout.addView(tView);
		return rLayout;
	}
}
