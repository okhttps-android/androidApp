package com.uas.appme.settings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.core.widget.view.Activity.MultiImagePreviewActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appme.R;
import com.uas.appme.settings.model.BSettingPlaceBean;
import com.uas.appme.settings.model.CompanyRestListBean;
import com.uas.appme.settings.model.EmployeeRestListBean;

import java.util.ArrayList;


/**
 * Created by ${FANGLH} on 2017/10/14.
 * Function：
 */

public class BSetComRestListActivity extends BaseActivity {

	private MyListView psetting_list;
	private String sc_industry;
	private String sc_industrycode;
	private EmptyLayout mEmptyLayout;
	private EmployeeRestListBean mListe;
	private CompanyRestListBean mListc;
	private BSettingPlaceBean mListp;
	private String type;
	private EomployeeRestAdapter emyAdapter;
	private CompanyRestAdapter cmyAdapter;
	private PlaceSettingAdapter pmyAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_setting_list_activity);
		initView();
		initData();
		initEvents();
	}

	private void initEvents() {
		psetting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (type) {
					case "eomployee":
						startActivityForResult(new Intent(ct, BRestActivity.class)
								.putExtra("updateData", JSON.toJSONString(mListe.getResult().get(position)))
								.putExtra("type", 1), 20);
						LogUtil.prinlnLongMsg("updateData", JSON.toJSONString(mListe.getResult().get(position)));
						break;
					case "company":
						startActivityForResult(new Intent(ct, BRestActivity.class)
								.putExtra("updateData", JSON.toJSONString(mListc.getResult().get(position)))
								.putExtra("type", 0), 20);
						LogUtil.prinlnLongMsg("updateData", JSON.toJSONString(mListc.getResult().get(position)));
						break;
					case "place":
						startActivityForResult(new Intent(ct, BSettingLocationActivity.class)
								.putExtra("updateData", JSON.toJSONString(mListp.getResult().get(position)))
						,20);
						LogUtil.prinlnLongMsg("updateData", JSON.toJSONString(mListp.getResult().get(position)));
						break;
				}
			}
		});

		psetting_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				showPopupWindow(position);
				return true;
			}
		});

	}
	private PopupWindow setWindow = null;
	private void showPopupWindow(int longPosition) {
		showMarkReadPW(longPosition);
		setWindow.showAtLocation(getWindow().getDecorView().
				findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
		DisplayUtil.backgroundAlpha(this, 0.4f);
	}

	private void showMarkReadPW(final int longPosition) {
		View viewContext = LayoutInflater.from(ct).inflate(R.layout.bsetting_delete, null);
		viewContext.findViewById(R.id.msg_delete_tv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doDeleteHandle(longPosition);
			}
		});
		setWindow = new PopupWindow(viewContext,
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		setWindow.setAnimationStyle(R.style.MenuAnimationFade);
		setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
		setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				closePopupWindow();
			}
		});
	}

	private void doDeleteHandle(int longPosition) {
		if (!CommonUtil.isNetWorkConnected(ct)){
			ToastMessage(getString(R.string.common_notlinknet));
			return;
		}
		String keyfield = null;
		String tablename = null;
		String id = null;
		switch (type) {
			case "eomployee":
				keyfield = "sf_id";
				id = mListe.getResult().get(longPosition).getSf_id();
				tablename="ServicemanDayoff";
				break;
			case "company":
				keyfield = "sc_id";
				id = mListc.getResult().get(longPosition).getSc_id();
				tablename="ServciecompanyDayoff";
				break;
			case "place":
				keyfield = "st_id";
				id = mListp.getResult().get(longPosition).getSt_id();
				tablename="ServiceType";
				break;
		}

		HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
		httpClient.Api().send(new HttpClient.Builder()
				.url("user/appStoreDel")
				.add("keyfield", keyfield)
				.add("id",id)
				.add("tablename",tablename)
				.add("token", MyApplication.getInstance().mAccessToken)
				.method(Method.GET)
				.build(), new ResultSubscriber<>(new ResultListener<Object>() {
			@Override
			public void onResponse(Object o) {
				LogUtil.prinlnLongMsg("appStoreDel", o.toString() + "");
				try {
					if (!JSONUtil.validate(o.toString()) || o == null) return;
					if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
						initData();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));

		closePopupWindow();
	}


	private void closePopupWindow() {
		if (setWindow != null)
			setWindow.dismiss();
		DisplayUtil.backgroundAlpha(this, 1f);
	}
	private void initView() {
		//接收商家类型
		sc_industry = getIntent().getStringExtra("sc_industry");
		sc_industrycode = getIntent().getStringExtra("sc_industrycode");
		type = getIntent().getStringExtra("type");

		psetting_list = (MyListView) findViewById(R.id.psetting_list);
		emyAdapter = new EomployeeRestAdapter(this);
		cmyAdapter = new CompanyRestAdapter(this);
		pmyAdapter = new PlaceSettingAdapter(this);

		mListe = new EmployeeRestListBean();
		mListc = new CompanyRestListBean();
		mListp = new BSettingPlaceBean();

		mEmptyLayout = new EmptyLayout(this, psetting_list);
		mEmptyLayout.setShowEmptyButton(false);
		mEmptyLayout.setShowErrorButton(false);
		mEmptyLayout.setShowLoadingButton(false);


		switch (type) {
			case "eomployee":
				setTitle(getString(R.string.e_relaxday));
				psetting_list.setAdapter(emyAdapter);
				break;
			case "company":
				setTitle(getString(R.string.c_relaxday));
				psetting_list.setAdapter(cmyAdapter);
				break;
			case "place":
				setTitle(getString(R.string.room_location));
				psetting_list.setAdapter(pmyAdapter);
				break;
		}
	}

	private void initData() {
		LogUtil.prinlnLongMsg("initData()","initData()");
		String url = null;

		switch (type) {
			case "eomployee":
				url = "user/appCompanyRest";
				break;
			case "company":
				url = "user/appManRest";
				break;
			case "place":
				url = " user/appPlaceList";
				break;

		}
		if (!CommonUtil.isNetWorkConnected(ct)){
			ToastMessage(getString(R.string.common_notlinknet));
			return;
		}
		HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
		httpClient.Api().send(new HttpClient.Builder()
				.url(url)
//				.add("companyid", 201)
				.add("companyid",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
				.add("token", MyApplication.getInstance().mAccessToken)
				.method(Method.GET)
				.build(), new ResultSubscriber<>(new ResultListener<Object>() {
			@Override
			public void onResponse(Object o) {
				LogUtil.prinlnLongMsg("ComRest", o.toString() + "");
				if (!JSONUtil.validate(o.toString()) || o == null) return;
				try {

					switch (type) {
						case "eomployee":
							mListe = JSON.parseObject(o.toString(), EmployeeRestListBean.class);
							emyAdapter.setModel(mListe);
							emyAdapter.notifyDataSetChanged();
							break;
						case "company":
							mListc = JSON.parseObject(o.toString(), CompanyRestListBean.class);
							cmyAdapter.setModel(mListc);
							cmyAdapter.notifyDataSetChanged();
							break;
						case "place":
							mListp = JSON.parseObject(o.toString(), BSettingPlaceBean.class);
							pmyAdapter.setModelList(mListp);
							pmyAdapter.notifyDataSetChanged();
							break;

					}
					if (mListe == null || ListUtils.isEmpty(mListe.getResult())
							|| mListc == null || ListUtils.isEmpty(mListc.getResult())
							|| mListp == null || ListUtils.isEmpty(mListp.getResult()))
						mEmptyLayout.showEmpty();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.add_new, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == R.id.add) {
//			switch (type) {
//				case "eomployee":
//					startActivity(new Intent(this, BComSetEmployeeRestActivity.class)
//							.putExtra("sc_industry", sc_industry)
//							.putExtra("sc_industrycode", sc_industrycode));
//					break;
//				case "company":
//					startActivity(new Intent(this, BComSetCompanyRestActivity.class)
//							.putExtra("sc_industry", sc_industry)
//							.putExtra("sc_industrycode", sc_industrycode));
//					break;
//				case "place":
//					startActivity(new Intent(this, BSettingLocationActivity.class)
//							.putExtra("sc_industry", sc_industry)
//							.putExtra("sc_industrycode", sc_industrycode));
//					break;
//
//			}
//		}
//		return super.onOptionsItemSelected(item);
//	}

	private class PlaceSettingAdapter extends BaseAdapter {
		private Context mContext;
		private BSettingPlaceBean model;

		public BSettingPlaceBean getModel() {
			return model;
		}

		public void setModelList(BSettingPlaceBean model) {
			this.model = model;
		}

		public PlaceSettingAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return model == null ? 0 : model.getResult().size();
		}

		@Override
		public Object getItem(int position) {
			return model.getResult().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = View.inflate(mContext, R.layout.com_location_item, null);
				viewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
				viewHolder.image_im = (ImageView) convertView.findViewById(R.id.image_im);
				viewHolder.sTime_rl = (RelativeLayout) convertView.findViewById(R.id.service_time_rl);
				viewHolder.sTime_tv = (TextView) convertView.findViewById(R.id.service_time_tv);
				viewHolder.image_rl = (RelativeLayout) convertView.findViewById(R.id.image_rl);
				if("会所".equals(sc_industry) )
					viewHolder.sTime_rl.setVisibility(View.VISIBLE);
				else
					viewHolder.sTime_rl.setVisibility(View.GONE);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			//显示名称
			viewHolder.name_tv.setText(model.getResult().get(position).getSt_name());

			//显示服务时间
			viewHolder.sTime_tv.setText(model.getResult().get(position).getSt_servicetime());

			//显示照片
			if (!StringUtil.isEmpty(model.getResult().get(position).getSt_imageurl())){
				viewHolder.image_rl.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage(model.getResult().get(position).getSt_imageurl(),viewHolder.image_im);
			}else
				viewHolder.image_rl.setVisibility(View.GONE);


			viewHolder.image_rl.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ArrayList<String> mPhotoList = new ArrayList<>();
					mPhotoList.add(model.getResult().get(position).getSt_imageurl());
					Intent intent = new Intent(ct, MultiImagePreviewActivity.class);
					intent.putExtra(AppConstant.EXTRA_IMAGES, mPhotoList);
					intent.putExtra(AppConstant.EXTRA_POSITION, 0);
					intent.putExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
					startActivity(intent);
				}
			});
			return convertView;
		}

		class ViewHolder {
			TextView name_tv,sTime_tv;
			ImageView image_im;
			RelativeLayout sTime_rl,image_rl;
		}
	}

	private class EomployeeRestAdapter extends BaseAdapter {
		private Context mContext;
		private EmployeeRestListBean model;

		public EmployeeRestListBean getModel() {
			return model;
		}

		public void setModel(EmployeeRestListBean model) {
			this.model = model;
		}

		public EomployeeRestAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return model == null ? 0 : model.getResult().size();
		}

		@Override
		public Object getItem(int position) {
			return model.getResult().size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = View.inflate(mContext, R.layout.com_rest_item, null);
				viewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
				viewHolder.date_tv = (TextView) convertView.findViewById(R.id.date_tv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.name_tv.setText(model.getResult().get(position).getSf_username());
			viewHolder.date_tv.setText(DateFormatUtil.long2Str(DateFormatUtil.str2Long(model.getResult().get(position).getSf_date(), DateFormatUtil.YMD_HMS), DateFormatUtil.YMD));
			return convertView;
		}

		class ViewHolder {
			TextView name_tv;
			TextView date_tv;
		}
	}

	private class CompanyRestAdapter extends BaseAdapter {
		private Context mContext;
		private CompanyRestListBean model;

		public CompanyRestListBean getModel() {
			return model;
		}

		public void setModel(CompanyRestListBean model) {
			this.model = model;
		}

		public CompanyRestAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return model == null ? 0 : model.getResult().size();
		}

		@Override
		public Object getItem(int position) {
			return model.getResult().size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = View.inflate(mContext, R.layout.com_rest_item, null);
				viewHolder.date_tv = (TextView) convertView.findViewById(R.id.date_tv);
				viewHolder.name_rl = (RelativeLayout) convertView.findViewById(R.id.name_rl);
				viewHolder.line = convertView.findViewById(R.id.line);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.name_rl.setVisibility(View.GONE);
			viewHolder.line.setVisibility(View.GONE);
			viewHolder.date_tv.setText(DateFormatUtil.long2Str(DateFormatUtil.str2Long(model.getResult().get(position).getSc_date(), DateFormatUtil.YMD_HMS), DateFormatUtil.YMD));

			return convertView;
		}

		class ViewHolder {
			TextView date_tv;
			RelativeLayout name_rl;
			View line;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 20 && resultCode == 20) {
			//TODO 重更新
			initData();
		}
	}
}
