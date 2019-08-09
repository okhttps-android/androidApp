package com.core.widget.view.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.common.config.BaseConfig;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.R;
import com.core.base.OABaseActivity;
import com.core.utils.BaiduMapUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.adapter.SearchLocationAdapter;
import com.core.widget.view.imp.ISearchView;
import com.core.widget.view.imp.SearchPresenter;
import com.core.widget.view.model.SearchLocationModel;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;

import java.util.List;


public class SearchLocationActivity extends OABaseActivity implements ISearchView {
	private EditText search_edit;
	private MapView bmapView;
	private PullToRefreshListView listview;

	private SearchPresenter presenter;
	private SearchLocationAdapter mAdapter;
	private EmptyLayout mEmptyLayout;
	private TextView messageTv;

	private BroadcastReceiver locationRceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent!=null&&intent.getBooleanExtra(Constants.ACTION_LOCATION_CHANGE,false)){
				presenter.init(getIntent());
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(BaseConfig.getContext()).unregisterReceiver(locationRceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_location);
		initView();
		initEvent();

	}

	private void initEvent() {
		search_edit.addTextChangedListener(new EditChangeListener() {
			@Override
			public void afterTextChanged(Editable s) {
				listview.setVisibility(View.VISIBLE);
				presenter.search(s.toString());
//                if (s.toString().length() > 0){
//                    listview.setVisibility(View.VISIBLE);
//                    presenter.search(s.toString());
//                }else {
//                    listview.setVisibility(View.INVISIBLE);
//                }

			}
		});
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				List<SearchLocationModel> chche = mAdapter.getListData();
				int item = (position - 1) <= 0 ? 0 : (position - 1);
				if (ListUtils.isEmpty(chche) || chche.size() <= item) return;
				presenter.endActivity(SearchLocationActivity.this, chche.get(item).getPoiInfo());
			}
		});
	}

	private void initView() {

		LocalBroadcastManager.getInstance(BaseConfig.getContext()).registerReceiver(locationRceiver, new IntentFilter(Constants.ACTION_LOCATION_CHANGE));
		search_edit = (EditText) findViewById(R.id.search_edit);
		bmapView = (MapView) findViewById(R.id.bmapView);
		listview = (PullToRefreshListView) findViewById(R.id.listview);
		mEmptyLayout = new EmptyLayout(this, listview.getRefreshableView());
		mEmptyLayout.setShowLoadingButton(false);
		mEmptyLayout.setShowEmptyButton(false);
		mEmptyLayout.setShowErrorButton(false);
		mEmptyLayout.setEmptyViewRes(R.layout.empty_locayion);
		messageTv = (TextView) mEmptyLayout.getEmptyView().findViewById(R.id.messageTv);
		presenter = new SearchPresenter(this);
		UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();

		showPoiPoint(new LatLng(mUASLocation.getLatitude(),mUASLocation.getLongitude()));
		requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, new Runnable() {
			@Override
			public void run() {
				UasLocationHelper.getInstance().requestLocation();
				presenter.init(getIntent());
			}
		}, new Runnable() {
			@Override
			public void run() {
				ToastUtil.showToast(ct, R.string.not_system_permission);
			}
		});
	}

	@Override
	public void showPoiList(List<SearchLocationModel> models, String distanceTag) {
		if (ListUtils.isEmpty(models)) {
			mEmptyLayout.showEmpty();
			if (messageTv != null && presenter.isHineOutSize()) {
				messageTv.setText("当前没有找到符合打卡的位置，请移步至打卡位置进行打卡！！");
			}
		}
		if (mAdapter == null) {
			mAdapter = new SearchLocationAdapter(ct, models, distanceTag);
			listview.setAdapter(mAdapter);
		} else {
			mAdapter.setListData(models, distanceTag);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void showPoiPoint(LatLng location) {
		BaiduMapUtil.getInstence().setMapViewPoint(bmapView, location, true);
	}

	public void showNotNetWork() {
		ToastUtil.showToast(this, R.string.networks_out);
	}



}
