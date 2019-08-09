package com.uas.appcontact.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.PermissionUtil;
import com.common.thread.ThreadPool;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.dao.DBManager;
import com.core.model.AttentionUser;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.model.NewFriendMessage;
import com.core.model.User;
import com.core.model.XmppMessage;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.utils.NetUtils;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.core.utils.CompanyHandlerInfoUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.core.xmpp.CoreService;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.dao.NewFriendDao;
import com.core.xmpp.listener.OnCompleteListener;
import com.core.xmpp.model.AddAttentionResult;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appcontact.R;
import com.uas.appcontact.adapter.ContactsAdapter;
import com.uas.appcontact.db.ContactsDao;
import com.uas.appcontact.model.contacts.Contacts;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.utils.ContactsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
  * @desc:单选和多选通讯录人员界面
  * @author：Arison on 2017/12/27
  */
public class ContactsActivity extends OABaseActivity implements ContactsAdapter.ResultItemsInface {

	private PullToRefreshListView mlist;
	private VoiceSearchView voiceSearchView;
	private RelativeLayout select_rl;
	private TextView mumber_tv;
	private CheckBox all_sure_cb;
	private boolean isClickCb = true;
	private boolean mBind;
	private CoreService mXmppService;
	private ContactsAdapter adapter;
	private ContactsDao contactsDao;
	private EmptyLayout emptyLayout;
	private List<ContactsModel> models = new ArrayList<>();
	private List<ContactsModel> tmodels = new ArrayList<>();
	private List<ContactsModel> allModels = new ArrayList<>();
	private int type = 0;//0默认情况。1 其它界面调用
	private boolean isSingleSelect = true;//true 单选  false 多选
	private String searchKey;
	private int allSelect = 0;

	private LinearLayout include_tag;
	private ArrayList<ContactsModel> stateModels = new ArrayList<>();//记住状态值


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		ViewUtils.inject(this);
		contactsDao = ContactsDao.getInstance();
		initView();
		initData();
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogUtil.i("onServiceDisconnected");
			mXmppService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXmppService = ((CoreService.CoreServiceBinder) service).getService();
			LogUtil.i("onServiceConnected");
			LogUtil.i("mXmppService=" + (mXmppService == null));
		}
	};

	private void initView() {
		mlist = (PullToRefreshListView) findViewById(R.id.mList);
		all_sure_cb = (CheckBox) findViewById(R.id.all_sure_cb);
		mumber_tv = (TextView) findViewById(R.id.mumber_tv);
		include_tag = (LinearLayout) findViewById(R.id.include_tag);
		select_rl = (RelativeLayout) findViewById(R.id.select_rl);
		voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
		if (getIntent() != null) {
			type = getIntent().getIntExtra("type", 0);
			String title = getIntent().getStringExtra("title");
			isSingleSelect = getIntent().getBooleanExtra("isSingleSelect", true);
			stateModels = getIntent().getParcelableArrayListExtra("models");
			if (isSingleSelect) {
				select_rl.setVisibility(View.GONE);
			} else {
				select_rl.setVisibility(View.VISIBLE);
			}
			if (!StringUtil.isEmpty(title)) {
			setTitle(title);
			} else {
				setTitle(getString(R.string.common_Contact_person));
			}
		}
		//设置为空显示列表
		emptyLayout = new EmptyLayout(ct, mlist.getRefreshableView());
		emptyLayout.setShowLoadingButton(false);
		emptyLayout.setShowEmptyButton(false);
		emptyLayout.setShowErrorButton(false);
//        emptyLayout.setEmptyViewRes(R.layout.book_empty_list);

		mBind = activity.bindService(getIntent(), mServiceConnection, activity.BIND_AUTO_CREATE);
		mlist.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
		mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				ThreadPool.getThreadPool().addTask(new Runnable() {
					@Override
					public void run() {
						if (NetUtils.isNetWorkConnected(MyApplication.getInstance())) {
							if (!ListUtils.isEmpty(tmodels)) {
								tmodels.clear();
							}
							contactsDao.delete();//删除缓存
							loadUUFriendForNet();//先加载UU好友---》企业架构人员----》本地通讯录
						} else {
							getCaceData(true);//先加载UU好友---》企业架构人员----》本地通讯录
						}
					}
				});
			}
		});
		mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactsAdapter.ViewHolder viewHolder = (ContactsAdapter.ViewHolder) view.getTag();
				if (type == 0) {//默认情况---点击查看详情
					if (viewHolder.model.getType() == 3) {
						return;
					}
					Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
					intent.putExtra(AppConstant.EXTRA_NICK_CODE, viewHolder.sub_tv.getText().toString());
					intent.putExtra(AppConstant.EXTRA_USER_ID, viewHolder.model.getImid());
					intent.putExtra(AppConstant.EXTRA_NICK_NAME, viewHolder.name_tv.getText().toString());
					//intent.putExtra(AppConstant.EXTRA_EM_CODE, friend.getEmCode());
					Friend friend = new Friend();
					friend.setPhone(viewHolder.model.getPhone());
					friend.setUserId(viewHolder.model.getImid());
					friend.setRemarkName(viewHolder.model.getName());
					//friend.setStatus();
					intent.putExtra("friend", friend);
					startActivity(intent);
				} else if (type == 1) { //预约---获取对象界面
					//预约对象
					if (isSingleSelect) {
						//单选
						Intent intent = new Intent();
						intent.putExtra("data", viewHolder.model);
						setResult(0x20, intent);
						finish();
					} else {
						//多选
						LogUtil.d(TAG,"models size:"+adapter.getModels().size());
						LogUtil.prinlnLongMsg(TAG,"models:"+JSON.toJSONString(adapter.getModels()));
						
						boolean isChecked = !viewHolder.checkBox.isChecked();
						LogUtil.d("ContactsActivity", "isChecked:" + isChecked);
						setSelectNumber(isChecked);
						adapter.getItem(position - 1).setClick(isChecked);
						adapter.notifyDataSetChanged();
					}

				}
			}
		});

		voiceSearchView.addTextChangedListener(new EditChangeListener() {
			@Override
			public void afterTextChanged(Editable s) {
//				sreachKeyWorkOld(s == null ? "" : s.toString());
				sreachKeyWork(s == null ? "" : s.toString());
			}
		});

		all_sure_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (isClickCb) {
					if (adapter == null) return;
					for (int i = 0; i < adapter.getModels().size(); i++) {
						adapter.getModels().get(i).setClick(b);
					}
					allSelect = getSelectNumber().size();
					mumber_tv.setText(getString(R.string.selected) + allSelect + " " + "人员");
					adapter.notifyDataSetChanged();
					all_sure_cb.setText(b ? R.string.cancel_select_all : R.string.select_all);
				}
				isClickCb = true;
			}
		});

		//确认按钮
		findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (StringUtil.isEmpty(voiceSearchView.getSearch_edit().getText().toString())) {
					ArrayList<ContactsModel> models = getSelectNumber();
					Intent intent = new Intent();
					intent.putParcelableArrayListExtra("data", models);
					activity.setResult(0x01, intent);
					activity.finish();
				}else{
					ToastMessage("请清除查询条件便可以点击确认操作！");
				}
			}
		});

	}

	public ArrayList<ContactsModel> getSelectNumber() {
		ArrayList<ContactsModel> models = new ArrayList<>();
		for (int i = 0; i < adapter.getModels().size(); i++) {
			if (adapter.getModels().get(i).isClick) {
				if (StringUtil.isEmpty(adapter.getModels().get(i).getImid())) {
					adapter.getItem(i).setImid("0");
				}
				models.add(adapter.getItem(i));
			}
		}
		LogUtil.d("myTest", "选择的联系人：" + JSON.toJSONString(models));
		return models;
	}

	/**
	 * 搜索，原版  by Arison
	 *
	 * @param keyWork
	 */
	private void sreachKeyWorkOld(String keyWork) {
		searchKey = keyWork;
		LogUtil.d("arison", " searchKey:" + searchKey);
		if (!StringUtil.isEmpty(keyWork)) {
			mlist.setMode(PullToRefreshBase.Mode.DISABLED);
			//contactsDao.find(s.toString());
			List<ContactsModel> cacheData = contactsDao.find(keyWork);
			if (!ListUtils.isEmpty(stateModels)) {
				for (int i = 0; i < cacheData.size(); i++) {
					for (int j = 0; j < stateModels.size(); j++) {
						if (cacheData.get(i).getImid().equals(stateModels.get(j).getImid())) {
							cacheData.get(i).setClick(stateModels.get(j).isClick);
						}
					}
				}
			}
			if (adapter != null) {
				if (ListUtils.isEmpty(cacheData)) {
					searchContactsByNet(keyWork);
					return;
				}
				models.clear();
				models.addAll(cacheData);
				adapter.notifyDataSetChanged();
				//查询需要优化
				//getStateByPhones(cacheData);
				if (models.size() == 0) {
					emptyLayout.showEmpty();
				}
			} else {

			}
		} else {
			mlist.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
			getCaceData(false);
		}
	}

	/**
	 * 搜索，原版  by Bitliker
	 *
	 * @param keyWork
	 */
	private synchronized void sreachKeyWork(final String keyWork) {
		searchKey = keyWork;
		LogUtil.i("allModels=" + allModels.size());
		if (StringUtil.isEmpty(keyWork)) {
			mlist.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
			models.clear();
			models.addAll(allModels);
			adapter.notifyDataSetChanged();
		} else {
			LogUtil.i("start for=" + System.currentTimeMillis());
			List<ContactsModel> keyWorkModels = new ArrayList<>();
			for (ContactsModel e : allModels) {
				if (includeKeyWork(e, searchKey)) {
					keyWorkModels.add(e);
				}
			}
			if (!ListUtils.isEmpty(stateModels)) {
				for (int i = 0; i < keyWorkModels.size(); i++) {
					for (int j = 0; j < stateModels.size(); j++) {
						if (keyWorkModels.get(i).getImid().equals(stateModels.get(j).getImid())) {
							keyWorkModels.get(i).setClick(stateModels.get(j).isClick);
						}
					}
				}
			}
			LogUtil.i("end for=" + System.currentTimeMillis());
			updateAdapter(keyWorkModels);
		}
	}

	private Comparator<ContactsModel> comparator;

	public Comparator<ContactsModel> getComparator() {
		return comparator == null ? comparator = new Comparator<ContactsModel>() {
			@Override
			public int compare(ContactsModel t1, ContactsModel t2) {
				return t1.getType() - t2.getType();
			}
		} : comparator;
	}

	private void updateAdapter(List<ContactsModel> models) {
		if (adapter != null) {
			if (ListUtils.isEmpty(models)) {
				searchContactsByNet(searchKey);
			} else {
				this.models.clear();
				this.models.addAll(models);
				Collections.sort(this.models, getComparator());
				adapter.notifyDataSetChanged();
				if (models.size() == 0) {
					emptyLayout.showEmpty();
				}
			}
		}
	}

	private boolean includeKeyWork(ContactsModel e, String searchKey) {
		return hasOneContainsUpperCase(searchKey, e.getCompany(), e.getName(), e.getPhone(), e.getImid(), e.getEmail());
	}

	public static boolean hasOneContainsUpperCase(String key, String... message) {
		if (StringUtil.isEmpty(key)) return true;
		if (message == null || message.length <= 0) return false;
		for (String e : message) {
			if (e != null && e.toUpperCase().contains(key.toUpperCase()))
				return true;
		}
		return false;
	}

	/**
	 * 点击后
	 *
	 * @param isClicked 点击后是否为选中状态
	 */
	private void setSelectNumber(boolean isClicked) {
		allSelect += isClicked ? 1 : -1;
		if (allSelect < 0)
			allSelect = 0;
		if (!isClicked && all_sure_cb.isChecked()) {
			isClickCb = false;
			all_sure_cb.setChecked(false);
			all_sure_cb.setText(R.string.select_all);
		} else if (isClicked && !all_sure_cb.isChecked() && !ListUtils.isEmpty(adapter.getModels()) && allSelect == adapter.getModels().size()) {
			isClickCb = false;
			all_sure_cb.setChecked(true);
			all_sure_cb.setText(R.string.cancel_select_all);
		}
		mumber_tv.setText(getString(R.string.selected) + allSelect + " " + "人员");
	}

	private void initData() {
		if (!ListUtils.isEmpty(models)) {
			models.clear();
		}
		showLoading();
		adapter = new ContactsAdapter(this, models);
		adapter.setSingleSelect(isSingleSelect);
		mlist.setAdapter(adapter);
		String[] permissions = {Manifest.permission.READ_CONTACTS};
		if (PermissionUtil.lacksPermissions(ct, permissions)) {
			PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permissions);
		} else {
			getCaceData(false);
			isHasPermiss = true;
			LogUtil.d("Test", "有权限@....");
		}
	}

	boolean isHasPermiss = false;

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PermissionUtil.DEFAULT_REQUEST) {
			if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				ToastUtil.showToast(ct, R.string.not_system_permission);
				isHasPermiss = false;
				getCaceData(false);
				LogUtil.d("Test", "没有权限....");
			} else {
				isHasPermiss = true;
				getCaceData(false);
				LogUtil.d("Test", "有权限....");
			}
		}
	}


	//1
	public void loadCompanyContacts() {
		ThreadPool.getThreadPool().addTask(new Runnable() {
			@Override
			public void run() {
				List<EmployeesEntity> emdatas = getEmListByDB();
				if (emdatas != null) {
					LogUtil.d("Test", "企业架构人员不为空！");
					for (EmployeesEntity entity : emdatas) {
						ContactsModel model = new ContactsModel();
						model.setImid(String.valueOf(entity.getEm_IMID()));
						model.setType(2);
						model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
						model.setName(entity.getEM_NAME());
						model.setEmail(entity.getEM_EMAIL());
						model.setPhone(entity.getEM_MOBILE());
						model.setWhichsys(entity.getWHICHSYS());
						model.setCompany(entity.getCOMPANY());
						tmodels.add(model);
					}
					//执行第二步
					loadLocalContacts();
				} else {
					//联网取企业架构
					loadUASFriendsNet();
				}
			}
		});
	}

	//2
	public void loadIMContacts() {
		ThreadPool.getThreadPool().addTask(new Runnable() {
			@Override
			public void run() {
				List<Friend> friends = FriendDao.getInstance().getFriends(MyApplication.getInstance().mLoginUser.getUserId());
				if (friends != null) {
					LogUtil.d("Test", "IM好友不为空!");
					for (Friend entity : friends) {
						ContactsModel model = new ContactsModel();
						model.setImid(String.valueOf(entity.getUserId()));
						model.setType(1);
						model.setName(entity.getShowName());
						model.setEmail("");
						model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
						model.setPhone(entity.getPhone());
						model.setWhichsys(CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
						model.setCompany("");
						tmodels.add(model);
					}
					loadCompanyContacts();
				} else {
					//联网取UU好友数据
					loadUUFriendForNet();
				}

			}

		});
	}

	//3需要权限
	public void loadLocalContacts() {
		ThreadPool.getThreadPool().addTask(new Runnable() {
			@Override
			public void run() {
				if (isHasPermiss) {
					List<Contacts> contacts = ContactsUtils.getContacts1();
					if (contacts != null) {
						for (Contacts entity : contacts) {
							ContactsModel model = new ContactsModel();
							model.setImid("0");
							model.setName(StringUtil.isEmpty(entity.getName()) ? entity.getNickname() : entity.getName());
							model.setType(3);
							model.setEmail("");
							model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
							model.setPhone(entity.getPhone());
							model.setWhichsys(CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
							model.setCompany("");
							tmodels.add(model);

						}
						contactsDao.save(tmodels);
						getCaceData(false);
					}
				} else {
					//没权限
					contactsDao.save(tmodels);
					getCaceData(false);
				}

			}
		});
	}

	/**
	 * @desc:加载通讯录数据
	 */
	public void getCaceData(final boolean isHasNet) {
		ThreadPool.getThreadPool().addTask(new Runnable() {
			@Override
			public void run() {
				final List<ContactsModel> lists = getLocalContactsByDB();
				//上传
				if (!ListUtils.isEmpty(stateModels)) {
					for (int i = 0; i < lists.size(); i++) {
						for (int j = 0; j < stateModels.size(); j++) {
							if (lists.get(i).getImid().equals(stateModels.get(j).getImid())) {
								lists.get(i).setClick(stateModels.get(j).isClick);
							}
						}
					}
				}
				ThreadPool.getThreadPool().addTask(new Runnable() {
					@Override
					public void run() {
						List<ContactsModel> contacts = new ArrayList<>();
						for (ContactsModel model : lists) {
							if ("0".equals(model.getImid()) || StringUtil.isEmpty(model.getImid())) {
								contacts.add(model);
							}
						}
						uploadData(contacts);
					}
				});
				boolean falg = !ListUtils.isEmpty(lists) && !isHasNet;
				if (falg) {
					LogUtil.d("Test", "取缓存数据:" + falg);
					getStateByPhones(lists);
					allModels.clear();
					allModels.addAll(lists);
					OAHttpHelper.getInstance().post(new Runnable() {
						@Override
						public void run() {
							//更新ui
							if (adapter != null) {
								models.clear();
								models.addAll(lists);
								Collections.sort(models, getComparator());
								adapter.notifyDataSetChanged();
								if (adapter.getCount() == 0) emptyLayout.showEmpty();
								mlist.onRefreshComplete();
								voiceSearchView.getSearch_edit().setHint(lists.size() + "位联系人");
								dimssLoading();
								allSelect = getSelectNumber().size();
								mumber_tv.setText(getString(R.string.selected) + allSelect + " " + "人员");
							}
						}
					});

				} else {
					LogUtil.d("Test", "-----------缓存为空！---------");
					if (!ListUtils.isEmpty(tmodels)) {
						tmodels.clear();
					}
					contactsDao.delete();//删除缓存
					loadUUFriendForNet();//先加载UU好友---》企业架构人员----》本地通讯录
				}
			}
		});
	}

	private List<ContactsModel> getLocalContactsByDB() {
		if (StringUtil.isEmpty(searchKey)) {
			List<ContactsModel> models = contactsDao.find();
			return models;
		} else {
			return contactsDao.find(searchKey);
		}
	}

	private List<EmployeesEntity> getEmListByDB() {
		DBManager manager = new DBManager();
		String master = CommonUtil.getMaster();
		List<EmployeesEntity> emList = null;
		if (!StringUtil.isEmpty(master)) {
			try {
				emList = manager.select_getEmployee(new String[]{master}, "whichsys=?");
			} catch (Exception e) {

			}
		}
		manager.closeDB();
		return emList;
	}


	@Deprecated
	private void getStateByPhones(final List<ContactsModel> models) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (ContactsModel u : models) {
			if (u.getType() == 3) {
				if (i == models.size() - 1) {
					builder.append(u.getPhone());
				} else {
					builder.append(u.getPhone() + ",");
				}
			}
			i++;
		}
		HashMap<String, String> params = new HashMap<>();
		String myUserId = MyApplication.getInstance().mLoginUser.getUserId();
		params.put("token", MyApplication.getInstance().mAccessToken);
		params.put("userid", myUserId);
		params.put("telephones", builder.toString());
		LogUtil.d("array builder.toString=" + builder.toString());
		StringJsonObjectRequest<String> request = new StringJsonObjectRequest<String>(
				MyApplication.getInstance().getConfig().APP_QUER_YUSER, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				ToastUtil.showErrorNet(MyApplication.getInstance());
			}
		}, new StringJsonObjectRequest.Listener<String>() {
			@Override
			public void onResponse(ObjectResult<String> result) {
				String message = result.toString();
				if (!StringUtil.isEmpty(message)) {
					LogUtil.d("Test", "message:" + message);
					try {
						hanlderAppQueryuserInThread(message);
					} catch (Exception e) {

					}
				}
			}
		}, String.class, params);
		MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
	}

	@Deprecated
	private void hanlderAppQueryuserInThread(String message) {
		if (JSONUtil.validate(message)) {
			JSONObject object = JSON.parseObject(message);
			String resultData = object.getString("resultData");
			object = object.parseObject(resultData);
			String user = object.getString("user");
			JSONArray array = JSON.parseArray(user);
			LogUtil.i("array=" + array);

			if (!ListUtils.isEmpty(array)) {
				JSONObject o = null;
//                for (ContactsModel model:models){
//                    model.setType(3);
//                }
				for (int i = 0; i < array.size(); i++) {
					o = array.getJSONObject(i);
					String telephone = JSONUtil.getText(o, "telephone");
					String isfriend = JSONUtil.getText(o, "isfriend");
					int _id = JSONUtil.getInt(o, "_id");
					for (int j = 0; j < models.size(); j++) {
						ContactsModel model = models.get(j);
						if (model.getPhone().equals(telephone)) {
							if (isfriend.equals("0")) {
								model.setType(2);//非好友
								model.setImid(String.valueOf(_id));
								contactsDao.update(model, "0");
							} else if (isfriend.equals("1")) {
								model.setType(1);//好友
								model.setImid(String.valueOf(_id));
								contactsDao.update(model, "0");
							}
						}
					}
				}
				adapter.notifyDataSetChanged();
			}
		}
		//计算全部数据完成

	}

	@Override
	public void onResultForItems(View view, ContactsModel model, int position) {
		TextView textView = (TextView) view;
		String add = MyApplication.getInstance().getString(R.string.add);
		String invite = MyApplication.getInstance().getString(R.string.invite);
		if (textView.getText().toString().equals(add)) {
			addUser(model, position);
		}
		if (textView.getText().toString().equals(invite)) {
			invite(model, position);
		}
	}


	//TODO 添加好友
	private void addUser(final ContactsModel user, final int position) {
		if (user == null) {
			return;
		}
		showLoading();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("access_token", MyApplication.getInstance().mAccessToken);
		params.put("toUserId", String.valueOf(user.getImid()));
		StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
				MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_ADD, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				dimssLoading();
				ToastUtil.showErrorNet(activity);
			}
		}, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
			@Override
			public void onResponse(ObjectResult<AddAttentionResult> result) {
				dimssLoading();
				boolean success = Result.defaultParser(MyApplication.getInstance(), result, true);
				String message = result.toString();
				if (success) {
					showToast(R.string.add_attention_succ, R.color.load_submit);
					// 添加为关注
					User mUser = new User();
					mUser.setUserId(String.valueOf(user.getImid()));//已经开通了UU IM的人
					mUser.setNickName(user.getName());//手机通讯录的名字
					// 发送推送的消息
					NewFriendMessage mess = NewFriendMessage.createWillSendMessage(
							MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_FRIEND, null, mUser);
					LogUtil.i("mXmppService=" + (mXmppService == null));
					if (mXmppService != null)
						mXmppService.sendNewFriendMessage(mUser.getUserId(), mess);
					// 添加为好友
					NewFriendDao.getInstance().ascensionNewFriend(mess, Friend.STATUS_FRIEND);


					ContactsModel tData = models.get(position);
					if (StringUtil.isEmpty(tData.getWhichsys())) {
						tData.setWhichsys("");
					}
					tData.setType(1);
					LogUtil.d("Test", "tdata:" + JSON.toJSONString(tData));
					contactsDao.update(tData);
					adapter.notifyDataSetChanged();
				}
			}
		}, AddAttentionResult.class, params);
		MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
	}


	private void invite(ContactsModel user, int position) {
		LogUtil.i("invite");
		final String name = CommonUtil.getName();
		final String phone = user.getPhone().trim().replaceAll(" ", "");

//        if (!com.xzjmyk.pm.activity.util.StringUtil.isMobileNumber(phone)) {
//            showToast("选择人员电话号码为空或是格式不正确", R.color.load_submit);
//            return;
//        }
		StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
				Request.Method.POST, "http://message.ubtob.com/sms/send", new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				dimssLoading();
			}
		}, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
			@Override
			public void onResponse(ObjectResult<AddAttentionResult> result) {
				String message = result.toString();
				showToast("短信发送成功", R.color.load_submit);
				LogUtil.i("message=" + message);
			}
		}, AddAttentionResult.class, null) {
			@Override
			public byte[] getBody() throws AuthFailureError {
				String param = "{\"receiver\":\"" + phone + "\",\"params\":[\"" + name + "\"],\"templateId\":\"4b60e18b-de2e-410f-9de1-819265d9e636\"}";
				LogUtil.i("param=" + param);
				return param.getBytes();
			}

			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};
		MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBind) {
			activity.unbindService(mServiceConnection);
		}
	}

	private void loadUASFriendsNet() {
		CommonInterface.getInstance().loadCompanyData(new CommonInterface.OnResultListener() {
			@Override
			public void result(@NonNull boolean success, @NonNull int what, @Nullable String message) {
				try {
					if (success) {
						LogUtil.d("Test","系统通讯录： message:"+message);
						String role = CommonUtil.getUserRole();
						if ("1".equals(role)) {
							//个人用户不需要加载企业架构
							loadLocalContacts();
						}
						boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
						JSONObject object = JSON.parseObject(message);
						List<EmployeesEntity> emdatas = null;
						if (isB2b) {
							emdatas = CompanyHandlerInfoUtil.getEmployeesByB2b(object);
						} else {
							emdatas = CompanyHandlerInfoUtil.getEmployeesByNet(object);
						}
						if (ListUtils.isEmpty(emdatas)) return;
						for (EmployeesEntity entity : emdatas) {
							ContactsModel model = new ContactsModel();
							model.setImid(String.valueOf(entity.getEm_IMID()));
							model.setType(2);
							model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
							model.setName(entity.getEM_NAME());
							model.setEmail(entity.getEM_EMAIL());
							model.setPhone(entity.getEM_MOBILE());
							model.setWhichsys(entity.getWHICHSYS());
							model.setCompany(entity.getCOMPANY());
							tmodels.add(model);
						}
						loadLocalContacts();
					} else {
						loadLocalContacts();
					}
				} catch (Exception e) {
					loadLocalContacts();
				}
			}
		});
	}


	String HASHCODE = Integer.toHexString(this.hashCode()) + "@";

	private void loadUUFriendForNet() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("access_token", MyApplication.getInstance().mAccessToken);
		LogUtil.d("Test", "url:" + MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_LIST);
		LogUtil.d("Test", "access_token" + MyApplication.getInstance().mAccessToken);
		StringJsonArrayRequest<AttentionUser> request = new StringJsonArrayRequest<AttentionUser>(
				MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_LIST, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				loadCompanyContacts();
			}
		}, new StringJsonArrayRequest.Listener<AttentionUser>() {
			@Override
			public void onResponse(ArrayResult<AttentionUser> result) {
				LogUtil.d("Test", "result:" + result);
				boolean success = Result.defaultParser(MyApplication.getInstance(), result, false);
				if (success) {
					FriendDao.getInstance().addAttentionUsers(OAHttpHelper.getInstance(), MyApplication.getInstance().mLoginUser.getUserId(), result.getData(),
							new OnCompleteListener() {
								@Override
								public void onCompleted() {
									List<Friend> friends = FriendDao.getInstance().getFriends(MyApplication.getInstance().mLoginUser.getUserId());
									if (!ListUtils.isEmpty(friends)) {
										for (Friend entity : friends) {
											ContactsModel model = new ContactsModel();
											model.setImid(String.valueOf(entity.getUserId()));
											model.setType(1);
											model.setName(entity.getShowName());
											model.setEmail("");
											model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
											model.setPhone(entity.getPhone());
											//uu好友
											model.setWhichsys(CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
											model.setCompany("");
											tmodels.add(model);
										}
									}
									loadUASFriendsNet();
								}
							});
				} else {
					loadUASFriendsNet();
				}
			}
		}, AttentionUser.class, params);
		MyApplication.getInstance().getFastVolley().addDefaultRequest(HASHCODE, request);
	}

	private void searchContactsByNet(String key) {
		String url = Constants.IM_BASE_URL() + "user/appSearch";
		Map<String, Object> params = new HashMap<>();
		params.put("token", MyApplication.getInstance().mAccessToken);
		params.put("name", key);

		LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x01, null, null, "post");
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0x01:
					if (TextUtils.isEmpty(voiceSearchView.getText())) {
						updateAdapter(allModels);
					} else {
						try {
							String result = msg.getData().getString("result");
							String root = JSON.parseObject(result).getString("result");
							JSONArray array = JSON.parseArray(root);
							List<ContactsModel> xmodels = new ArrayList<>();
							for (int i = 0; i < array.size(); i++) {
								ContactsModel model = new ContactsModel();
								model.setType(2);
								model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
								model.setImid(array.getJSONObject(i).getString("imid"));
								model.setName(array.getJSONObject(i).getString("nickname"));
								model.setPhone(array.getJSONObject(i).getString("telephone"));
								xmodels.add(model);
							}
							if (!ListUtils.isEmpty(models)) {
								models.clear();
								models.addAll(xmodels);
							} else {
								models.addAll(xmodels);
							}
							if (adapter != null) {
								adapter.notifyDataSetChanged();
							}
							if (adapter.getCount() == 0) {
								emptyLayout.showEmpty();
							}
						} catch (Exception e) {

						}
					}
					break;
			}
		}
	};


	//
	public void uploadData(List<ContactsModel> models) {
		if (!ListUtils.isEmpty(models)) {
			StringBuilder mapBuilder = new StringBuilder("[");
			for (int i = 0; i < models.size(); i++) {
				if (i == models.size() - 1) {
					mapBuilder.append("{\n" +
							"\"am_telephone\":\"" + models.get(i).getPhone() + "\",\n" +
							"\"am_username\":\"" + models.get(i).getName() + "\",\n" +
							"\"am_userid\":\"" + 0 + "\"\n" +
							"}]");
				} else {
					mapBuilder.append("{\n" +
							"\"am_telephone\":\"" + models.get(i).getPhone() + "\",\n" +
							"\"am_username\":\"" + models.get(i).getName() + "\",\n" +
							"\"am_userid\":\"" + 0 + "\"\n" +
							"},");
				}
			}

			HttpClient httpClient = new HttpClient.Builder().url(Constants.IM_BASE_URL())
					.add("comParam", "param")
					.isDebug(true).build(true);
			httpClient.Api().send(new HttpClient.Builder()
					.url("/user/appMobileContact")
					.header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
					.add("token", MyApplication.getInstance().mAccessToken)
					.add("userid", MyApplication.getInstance().mLoginUser.getUserId())
					.add("username", MyApplication.getInstance().mLoginUser.getNickName())
					.add("map", mapBuilder.toString())
					.method(Method.POST)
					.build(), new ResultSubscriber<>(new ResultListener<Object>() {

				@Override
				public void onResponse(Object o) {
					LogUtil.prinlnLongMsg("HttpLogs", o.toString());
					LogUtil.d("HttpLogs", "result:" + o.toString());
					if (JSONUtil.validate(o.toString())) {

					}
				}
			}));
		}
	}
}