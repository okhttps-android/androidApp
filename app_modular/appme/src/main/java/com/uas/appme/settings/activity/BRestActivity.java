package com.uas.appme.settings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.view.Activity.SelectActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;
import com.uas.appme.settings.adapter.BRestAdapter;
import com.uas.appme.settings.model.BRest;
import com.uas.appworks.OA.erp.utils.MostLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.common.data.JSONUtil.getJSONArray;

/**
 * 员工和商家消息日设置界面
 * by Bitliker
 */
public class BRestActivity extends BaseActivity implements BRestAdapter.OnItemClickListener, View.OnClickListener {
	private final int MAN_SELECT = 11;

	private RecyclerView contentRV;
	private int type;
	private int position;
	private BRestAdapter mAdapter;
	private String companyid;
	private String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brest);
		setTitle(getString(R.string.updata));
		initView();
		initData();
	}

	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
			String data = intent.getStringExtra("updateData");
			type = intent.getIntExtra("type", -1);
			if (!StringUtil.isEmpty(data) && JSONUtil.validateJSONObject(data)) {
				JSONObject object = JSON.parseObject(data);
				companyid = JSONUtil.getText(object, "sf_companyid", "sc_companyid");
				String companyname = JSONUtil.getText(object, "sf_companyname", "sc_companyname");
				String date = JSONUtil.getText(object, "sf_date", "sc_date");
				id = JSONUtil.getText(object, "sf_id", "sc_id");
				String userid = JSONUtil.getText(object, "sf_userid","sc_userid");
				String username = JSONUtil.getText(object, "sf_username","sc_username");
				BRest model = new BRest(type);
				model.setCompanyname(companyname);
				model.setCompanyid(companyid);
				model.set_id(id);
				model.setUserId(userid);
				model.setUsername(username);
				long time = DateFormatUtil.str2Long(date, DateFormatUtil.YMD_HMS);
				if (time > 0) {
					model.setDate(DateFormatUtil.long2Str(time, DateFormatUtil.YMD));
				}
				List<BRest> models = new ArrayList<>();
				models.add(model);
				mAdapter = new BRestAdapter(this, models);
				contentRV.setAdapter(mAdapter);
				if (type == 1) {
					mAdapter.setOnItemClickListener(this);
				}
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && MAN_SELECT == requestCode) {
			SelectBean bean = data.getParcelableExtra("data");
			if (bean != null) {
				String json = bean.getJson();
				if (JSONUtil.validateJSONObject(json)) {
					JSONObject object = JSON.parseObject(json);
					LogUtil.prinlnLongMsg("fanglh1",JSON.toJSONString(object));
					if (object != null) {
						BRest model = new BRest(type);
						model.setCompanyid(JSONUtil.getText(object, "sm_companyid"));
						model.setCompanyname(JSONUtil.getText(object, "sm_companyname"));
						model.setUserId(JSONUtil.getText(object, "sm_userid"));
						model.setUsername(JSONUtil.getText(object, "sm_username"));
						model.set_id(id);
						if (mAdapter != null) {
							mAdapter.updateData(position, model);
						}
						LogUtil.prinlnLongMsg("fanglh2",JSON.toJSONString(model));
					}
				}
			}
		}
	}

	private void initView() {
		contentRV = (RecyclerView) findViewById(R.id.contentRV);
		findViewById(R.id.saveBtn).setOnClickListener(this);
		contentRV.setItemAnimator(new DefaultItemAnimator());
		contentRV.setLayoutManager(new MostLinearLayoutManager(ct));
		findViewById(R.id.deleteBtn).setOnClickListener(this);

	}

	private boolean isSubmit;

	@Override
	public void itemClick(int position) {
		if (isSubmit) return;
		isSubmit = true;
		this.position = position;
		loadServiceMan();
	}

	@Override
	public void onClick(View v) {
		if (!CommonUtil.isNetWorkConnected(ct)) {
			ToastMessage(getString(R.string.common_notlinknet));
			ToastMessage(getString(R.string.networks_out));
			return;
		}
		if (v.getId() == R.id.saveBtn) {
			submitSave();
		}else if (v.getId() == R.id.deleteBtn){
			deDelete();
		}
	}

	private void deDelete() {
		Intent intent = getIntent();
		String updateData = intent.getStringExtra("updateData");
		if (StringUtil.isEmpty(updateData)) return;
		type = intent.getIntExtra("type", -1);
		String keyfield = null;
		String tablename = null;
		String id = null;

		try {
			switch (type){
                case 0:
                    keyfield = "sc_id";
                    id = JSON.parseObject(updateData).getString("sc_id");
                    tablename="ServciecompanyDayoff";
                    break;
                case 1:
					keyfield = "sf_id";
					id = JSON.parseObject(updateData).getString("sf_id");
					tablename="ServicemanDayoff";
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
							Toast.makeText(ct,getString(R.string.delete_all_succ),Toast.LENGTH_LONG).show();
							setResult(20);
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//load 选择服务人员
	public void loadServiceMan() {
		progressDialog.show();
		new HttpClient.Builder(Constants.IM_BASE_URL())
				.isDebug(BaseConfig.isDebug())
				.build()
				.Api()
				.send(new HttpClient.Builder()
						.url("/user/appStoreman")
						.add("companyid", StringUtil.isEmpty(companyid) ? CommonUtil.getSharedPreferences(ct, "erp_uu") : companyid)
						.add("serviceid", "0")
						.add("token", MyApplication.getInstance().mAccessToken)
						.method(Method.GET)
						.build(), new ResultSubscriber<>(new ResultListener<Object>() {
					@Override
					public void onResponse(Object o) {
						LogUtil.i(o.toString());
						if (JSONUtil.validateJSONObject(o.toString())) {
							JSONArray array = getJSONArray(o.toString(), "result");
							SelectBean bean = null;
							ArrayList<SelectBean> selectBeens = new ArrayList<SelectBean>();

							if (!ListUtils.isEmpty(array)) {
								for (int i = 0; i < array.size(); i++) {
									JSONObject object = array.getJSONObject(i);
									bean = new SelectBean();
									int id = JSONUtil.getInt(object, "sm_id");
									String name = JSONUtil.getText(object, "sm_username");
									bean.setId(id);
									bean.setFields(String.valueOf(id));
									bean.setName(name);
									bean.setJson(object.toJSONString());
									selectBeens.add(bean);
								}
							} else {
								ToastUtil.showToast(ct, "当前公司还没有设置员工");
							}
							if (!ListUtils.isEmpty(selectBeens)) {
								Intent intent = new Intent(ct, SelectActivity.class)
										.putExtra("type", 2)
										.putExtra("title", "选择人员")
										.putParcelableArrayListExtra("data", selectBeens);
								startActivityForResult(intent, MAN_SELECT);
							}
						}
						isSubmit = false;
						progressDialog.dismiss();
					}
				}));
	}


	public void submitSave() {
		if (isSubmit) return;
		isSubmit = true;
		Map<String, Object> map = getSaveMap();
		if (map == null) {
			isSubmit = false;
			return;
		}
		LogUtil.i(JSONUtil.map2JSON(map));
		new HttpClient.Builder(Constants.IM_BASE_URL())
				.isDebug(BaseConfig.isDebug())
				.build()
				.Api()
				.send(new HttpClient.Builder()
						.url(type == 1 ? "/user/appPersonRest" : "/user/appStoreRest")
						.add("map", JSONUtil.map2JSON(map))
						.add("token", MyApplication.getInstance().mAccessToken)
						.method(Method.POST)
						.build(), new ResultSubscriber<>(new ResultListener<Object>() {

					@Override
					public void onResponse(Object o) {
						if (JSONUtil.validateJSONObject(o.toString())) {
							ToastMessage(o.toString());
							if (JSONUtil.getBoolean(o.toString(), "result")) {
								setResult(20);
								Toast.makeText(ct,getString(R.string.update_success),Toast.LENGTH_LONG).show();
								finish();
							}
						}
						isSubmit = false;
					}
				}));


	}

	private Map<String, Object> getSaveMap() {
		if (mAdapter == null || ListUtils.isEmpty(mAdapter.getModels())) {
			//TODO show must imput
			return null;
		}
		BRest model = mAdapter.getModels().get(0);
		if (model == null) {
			//TODO show must imput
			return null;
		}
		LogUtil.i(JSON.toJSONString(model));
		Map<String, Object> map = new HashMap<>();
		String date = null;
		String sc_companyname = null;
		String sc_companyid = null;

		if (StringUtil.isEmpty(model.getDate())) {
			ToastUtil.showToast(ct, "请选择日期");
			return null;
		} else {
			date = model.getDate();
		}
		if (StringUtil.isEmpty(model.getCompanyname())) {
			ToastUtil.showToast(ct, "公司名称无效");
			return null;
		} else {
			sc_companyname = model.getCompanyname();
			sc_companyid = model.getCompanyid();
		}
		if (type == 1) {
			map.put("sf_companyname", sc_companyname);
			map.put("sf_companyid", sc_companyid);
			map.put("sf_date", date);
			if (StringUtil.isEmpty(model.getUsername())) {
				ToastUtil.showToast(ct, "请选择人员");
				return null;
			} else {
				map.put("sf_username", model.getUsername());
				map.put("sf_userid", model.getUserId());
			}
			map.put("sf_id", StringUtil.isEmpty(model.get_id()) ? "0" : model.get_id());
		} else {
			map.put("sc_companyname", sc_companyname);
			map.put("sc_companyid", sc_companyid);
			map.put("sc_date", date);
			map.put("sc_id", StringUtil.isEmpty(model.get_id()) ? "0":model.get_id());
		}
		return map;
	}

}
