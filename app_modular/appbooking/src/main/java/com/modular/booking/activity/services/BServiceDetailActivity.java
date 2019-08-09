package com.modular.booking.activity.services;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemBserviceStoremanListAdapter;
import com.modular.booking.model.SBListModel;
import com.modular.booking.model.SBStoremanModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/30.
 * 指定技师页面
 */

public class BServiceDetailActivity extends OABaseActivity implements View.OnClickListener {
	private TextView mBookStoreTv;
	private PullToRefreshListView mBookDetailPtlv;
	private HttpClient mHttpClient;
	private ItemBserviceStoremanListAdapter mStoremanListAdapter;
	private List<SBStoremanModel> mStoremanModels;
	private EmptyLayout mEmptyLayout;
	private String mCompanyId, mServiceId;
	private SBListModel model;
	private TextView guide_title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_service_detail);

		initViews();
		initEvents();
		initDatas();
	}

	private void initDatas() {
		progressDialog.show();
		mCompanyId = model.getCompanyid();
		mServiceId = "0";
		LogUtil.prinlnLongMsg("appStoreman","companyid = "+ mCompanyId + ",serviceid = " +mServiceId + ",Token = " + MyApplication.getInstance().mAccessToken);
		mHttpClient.Api().send(new HttpClient.Builder()
				.url("/user/appStoreman")
				.add("companyid", mCompanyId)
				.add("serviceid", mServiceId)
				.add("Token", MyApplication.getInstance().mAccessToken)
				.method(Method.POST)
				.build(), new ResultSubscriber<>(new ResultListener<Object>() {
			@Override
			public void onResponse(Object o) {
				progressDialog.dismiss();
				try {
					LogUtil.d("appstoreman", o.toString());
					if (JSONUtil.validate(o.toString())) {
						JSONObject resultObject = JSON.parseObject(o.toString());
						JSONArray resultArray = resultObject.getJSONArray("result");
						if (!ListUtils.isEmpty(resultArray)) {
							for (int i = 0; i < resultArray.size(); i++) {
								JSONObject jsonObject = resultArray.getJSONObject(i);
								SBStoremanModel sbStoremanModel = new SBStoremanModel();
								sbStoremanModel.setSm_companyid(jsonObject.getString("sm_companyid"));
								sbStoremanModel.setSm_companyname(jsonObject.getString("sm_companyname"));
								sbStoremanModel.setSm_id(jsonObject.getString("sm_id"));
								sbStoremanModel.setSm_level(jsonObject.getString("sm_level"));
								sbStoremanModel.setSm_stid(jsonObject.getString("sm_stid"));
								sbStoremanModel.setSm_stname(jsonObject.getString("sm_stname"));
								sbStoremanModel.setSm_telephone(jsonObject.getString("sm_telephone"));
								sbStoremanModel.setSm_userid(jsonObject.getString("sm_userid"));
								sbStoremanModel.setSm_username(jsonObject.getString("sm_username"));
								mStoremanModels.add(sbStoremanModel);
							}
							mStoremanListAdapter.notifyDataSetChanged();
						}
					}
					if (mStoremanModels.size() == 0) {
						mEmptyLayout.showEmpty();
					}
				} catch (Exception e) {

				}
			}
		}));
	}

	private void initEvents() {
		mBookStoreTv.setOnClickListener(this);
		mBookDetailPtlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SBStoremanModel bean = mStoremanListAdapter.getItem((int) id);
				Intent intent = new Intent(ct, BServiceAddActivity.class);
				intent.putExtra("model", model);
				intent.putExtra("sb_userid", bean.getSm_userid());
				intent.putExtra("sb_username", bean.getSm_username());
				startActivity(intent);
			}
		});
	}

	private void initViews() {
		mBookStoreTv = (TextView) findViewById(R.id.book_service_detail_store_tv);
		guide_title=(TextView) findViewById(R.id.guide_title);
		mBookDetailPtlv = (PullToRefreshListView) findViewById(R.id.book_service_detail_ptlv);
		mBookDetailPtlv.setMode(PullToRefreshBase.Mode.DISABLED);

		mEmptyLayout = new EmptyLayout(this, mBookDetailPtlv.getRefreshableView());
		mEmptyLayout.setShowLoadingButton(false);
		mEmptyLayout.setShowErrorButton(false);
		mEmptyLayout.setShowEmptyButton(false);

		if (getIntent() != null) {
			model = getIntent().getParcelableExtra("model");
			setTitle(model.getName());
			LogUtil.prinlnLongMsg("myTest", "model:" + JSON.toJSONString(model));
			if ("10005".equals(model.getIndustrycode())){
				//会所
				mBookStoreTv.setText("不指定技师，预约门店");
				guide_title.setText("指定技师");
			}else if("10004".equals(model.getIndustrycode())){
				//美容美化
				mBookStoreTv.setText("不指定发型师，预约门店");
				guide_title.setText("指定发型师");
			}
		}

		mHttpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
		mStoremanModels = new ArrayList<>();
		mStoremanListAdapter = new ItemBserviceStoremanListAdapter(this, mStoremanModels);
		mStoremanListAdapter.setModel(model);
		mBookDetailPtlv.setAdapter(mStoremanListAdapter);
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.book_service_detail_store_tv) {
			Intent intent = new Intent(ct, BServiceAddActivity.class);
			intent.putExtra("model", model);
			startActivity(intent);
		}
	}
}
