package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.SaleChartActivity;
import com.xzjmyk.pm.activity.ui.erp.model.ChildMenu;
import com.xzjmyk.pm.activity.ui.erp.model.ParentMenu;
import com.xzjmyk.pm.activity.ui.erp.net.HttpClient;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;
import com.core.widget.EmptyLayout;
import com.core.utils.FlexJsonUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author :LiuJie 2015年11月11日 上午10:31:45
 * @注释:统计碎片
 */
@SuppressWarnings("deprecation")
public class ChartListFragment extends Fragment implements OnClickListener {

	private String TAG = "ChartListFragment";
	private Context ct;
	private Activity activity;
	/** @注释：加载成功 */
	public static final int LOAD_SUCESS = 1;
	/** @注释：网络未连接 */
	public static final int LOAD_NOTNETWORK = 0;
	/** @注释： 服务器异常 */
	public static final int LOAD_EXCEPTION = 2;

	private String url;
	private Map<String, String> params = new HashMap<String, String>();
	@ViewInject(R.id.m_list)
	private ExpandableListView m_list;
	private ArrayList<ParentMenu> pMenus = new ArrayList<ParentMenu>();
	private ExpandableListAdapter adapter;
	private EmptyLayout mEmptyLayout;
	private CustomProgressDialog progressDialog;

	private OnClickListener mErrorClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mEmptyLayout.showLoading();
			initData();
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.iv_refesh:
//			initData();
//			break;
		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.act_home_statis_menu, container, false);
		Log.i(TAG, "onCreateView()");
		ViewUtils.inject(this, view);
		activity = getActivity();
		ct = activity;
		progressDialog = CustomProgressDialog.createDialog(ct);

		initView();
		initData();
		return view;
	}

	private void initView() {
		mEmptyLayout = new EmptyLayout(ct, m_list);
		mEmptyLayout.setEmptyButtonClickListener(mErrorClickListener);

		m_list.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
					long id) {
				if (!StringUtil.isEmpty(CommonUtil.getAppBaseUrl(ct))) {
					Intent it_chart = new Intent(ct, SaleChartActivity.class);
					it_chart.putExtra("Id", pMenus.get(groupPosition).getChildMenus().get(childPosition).getSt_id());
					it_chart.putExtra("type",
							pMenus.get(groupPosition).getChildMenus().get(childPosition).getSt_type());
					startActivity(it_chart);
				} else {
//					ViewUtil.ShowBasicNoTitle(ct);
				}
				return false;
			}
		});
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SUCESS:
				if (!pMenus.isEmpty()) {
					pMenus.clear();
				}
				String result = msg.getData().getString("result");
				Map<String, Object> rMap = FlexJsonUtil.fromJson(result);
				@SuppressWarnings("unchecked")
				Map<String, Object> cMap = (Map<String, Object>) rMap.get("Stats");
				// 遍历一级菜单取标题
				List<ChildMenu> cMenus = null;
				if (cMap != null) {
					@SuppressWarnings("rawtypes")
					Iterator iter = cMap.entrySet().iterator();
					while (iter.hasNext()) {
						ParentMenu pMenu = new ParentMenu();
						@SuppressWarnings("rawtypes")
						Map.Entry entry = (Map.Entry) iter.next();
						String key = (String) entry.getKey();
						pMenu.setTitle(key);
						cMenus = FlexJsonUtil.fromJsonArray(FlexJsonUtil.toJson(cMap.get(key)), ChildMenu.class);
						pMenu.setChildMenus(cMenus);
						pMenus.add(pMenu);
					}
				}
				adapter = new ExpandableListAdapter(ct, pMenus);
				m_list.setAdapter(adapter);
				if (adapter.getGroupCount() == 0) {
					mEmptyLayout.setShowEmptyButton(false);
					mEmptyLayout.setEmptyMessage("暂无数据！");
					mEmptyLayout.showEmpty();
				} else {
					// iv_refesh.setVisibility(View.GONE);
				}
				progressDialog.dismiss();
				break;
			default:
				break;
			}
		};
	};

	private void initData() {
		progressDialog.show();
		if (CommonUtil.isNetWorkConnected(activity)) {
			url = CommonUtil.getAppBaseUrl(activity) + "mobile/common/Stats.action";
			String sessionId = CommonUtil.getSharedPreferences(activity, "sessionId");
			params.put("sessionId", sessionId);
			startNetThread(url, params, handler);
		} else {
			mEmptyLayout.setEmptyMessage(getString(R.string.common_notlinknet));
			mEmptyLayout.showEmpty();
			progressDialog.dismiss();
		}
	}

	public class ExpandableListAdapter extends BaseExpandableListAdapter {
		private ArrayList<ParentMenu> list;
		private Context ct;

		public ExpandableListAdapter(Context ct, ArrayList<ParentMenu> list) {
			this.list = list;
			this.ct = ct;
		}

		@Override
		public int getGroupCount() {
			return list != null ? list.size() : 0;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return list.get(groupPosition).getChildMenus() != null ? list.get(groupPosition).getChildMenus().size() : 0;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return list.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return list.get(groupPosition).getChildMenus().get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			convertView =   RelativeLayout.inflate(ct, R.layout.act_menu_group, null);
			TextView grouptitle = (TextView) convertView.findViewById(R.id.tv_group_title);
			grouptitle.setText(list.get(groupPosition).getTitle());
			if (groupPosition%2==0){
				convertView.setBackgroundColor(ct.getResources().getColor(R.color.item_color1));
			}else{
				convertView.setBackgroundColor(ct.getResources().getColor(R.color.item_color2));
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			convertView = (RelativeLayout) RelativeLayout.inflate(ct, R.layout.act_menu_child, null);
			;
			TextView grouptitle = (TextView) convertView.findViewById(R.id.tv_child_title);
			grouptitle.setText(list.get(groupPosition).getChildMenus().get(childPosition).getSt_title());
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	/**
	 * @author LiuJie
	 * @功能:公共线程--开启网络请求任务 url:请求路径 params:参数
	 */
	public void startNetThread(final String url, final Map<String, String> params, final Handler handler_net) {

		new Thread(new Runnable() {
			@Override
			public void run() {

				if (CommonUtil.isNetWorkConnected(activity)) {
					/** @注释：处理网络请求返回结果 */
					String result = getDataFromServer(url, params);
					System.out.println("网络请求end：" + new Date().getTime());
					Message message = new Message();
					if (result != null) {
						Bundle bundle = new Bundle();
						bundle.putString("result", result);
						message.setData(bundle);
						message.what = LOAD_SUCESS;
						handler_net.sendMessage(message);
					} else {
						handler_net.sendEmptyMessage(LOAD_EXCEPTION);
					}
				} else {
					handler_net.sendEmptyMessage(LOAD_NOTNETWORK);
				}
			}
		}).start();
	}

	public String getDataFromServer(String url, Map<String, String> params) {
		String result = null;
		HttpClient hClient = new HttpClient();
		try {
			result = hClient.sendPostRequest(url, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);

		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
