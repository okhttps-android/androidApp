package com.xzjmyk.pm.activity.ui.find;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.system.DisplayUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.xzjmyk.pm.activity.R;
import com.core.xmpp.model.Area;
import com.xzjmyk.pm.activity.bean.circle.PublicMessage;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.base.EasyFragment;
import com.xzjmyk.pm.activity.ui.circle.PMsgDetailActivity;
import com.core.utils.ToastUtil;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 人才榜最热
 * 
 */
public class HotestFragment extends EasyFragment {

	private int mPageIndex = 0;
	private PullToRefreshGridView mPullToRefreshGridView;

	private List<PublicMessage> mMessages;
	private FindAdapter mAdapter;
	private boolean mNeedUpdate = true;
	private BaseActivity mActivity;

	public HotestFragment() {
		mMessages = new ArrayList<PublicMessage>();
	}

	@Override
	protected int inflateLayoutId() {
		return R.layout.fragment_find_inner;
	}

	@Override
	protected void onCreateView(Bundle savedInstanceState, boolean createView) {
		if (createView) {
			initView();
		}
	}

	private void initView() {
		mPullToRefreshGridView = (PullToRefreshGridView) findViewById(R.id.pull_refresh_grid_view);
		mAdapter = new FindAdapter(mMessages, getActivity());
		mPullToRefreshGridView.getRefreshableView().setAdapter(mAdapter);

		View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_list_empty_view, null);
		mPullToRefreshGridView.setEmptyView(emptyView);

		mPullToRefreshGridView.setShowIndicator(false);

		int padding = DisplayUtil.dip2px(getActivity(), 15);

		mPullToRefreshGridView.getRefreshableView().setPadding(padding, padding, padding, 5);

		mPullToRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				requestData(true);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				requestData(false);
			}
		});

		mPullToRefreshGridView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PublicMessage message = mMessages.get((int) parent.getItemIdAtPosition(position));
				Intent intent = new Intent(getActivity(), PMsgDetailActivity.class);
				intent.putExtra("public_message", message);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mNeedUpdate) {
			mNeedUpdate = false;
			mPullToRefreshGridView.post(new Runnable() {
				@Override
				public void run() {
					mPullToRefreshGridView.setPullDownRefreshing(200);
				}
			});
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (BaseActivity) getActivity();
	}

	private void requestData(final boolean isPullDwonToRefersh) {
		if (isPullDwonToRefersh) {
			mPageIndex = 0;
		}

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("pageIndex", mPageIndex + "");

		Area area = Area.getDefaultCity();
		if (area != null) {
			params.put("cityId", String.valueOf(area.getId()));// 城市Id
		} else {
			params.put("cityId", "0");
		}

		StringJsonArrayRequest<PublicMessage> request = new StringJsonArrayRequest<PublicMessage>(mActivity.mConfig.CIRCLE_MSG_HOT,
				new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						ToastUtil.showErrorNet(getActivity());
						mPullToRefreshGridView.onRefreshComplete();
					}
				}, new StringJsonArrayRequest.Listener<PublicMessage>() {
					@Override
					public void onResponse(ArrayResult<PublicMessage> result) {
						boolean success = Result.defaultParser(getActivity(), result, true);
						if (success) {
							mPageIndex++;
							if (isPullDwonToRefersh) {
								mMessages.clear();
							}
							List<PublicMessage> datas = result.getData();
							if (datas != null && datas.size() > 0) {
								mMessages.addAll(datas);
							}
							mAdapter.notifyDataSetChanged();
						}
						mPullToRefreshGridView.onRefreshComplete();
					}
				}, PublicMessage.class, params);
		mActivity.addDefaultRequest(request);
	}

}
