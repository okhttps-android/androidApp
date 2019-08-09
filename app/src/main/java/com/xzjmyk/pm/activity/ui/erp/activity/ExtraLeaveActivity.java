package com.xzjmyk.pm.activity.ui.erp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.andreabaccega.widget.FormEditText;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.core.widget.SingleDialog;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.DbfindListActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.ExtraLeaveEntity;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExtraLeaveActivity extends BaseActivity implements OnClickListener {

	// 编辑框值
	@ViewInject(R.id.et_extra_no)
	private FormEditText et_extra_no;
	/** @注释：申请人员工 */
	@ViewInject(R.id.et_extra_encode)
	private FormEditText et_extra_encode;
	/** @注释：部门 */
	@ViewInject(R.id.et_extra_deparment)
	private FormEditText et_extra_deparment;
	/** @注释：员工类型 */
	@ViewInject(R.id.et_leader_no)
	private FormEditText et_leader_no;
	@ViewInject(R.id.et_leader_name)
	private FormEditText et_leader_name;
	/** @注释：事由类型 */
	@ViewInject(R.id.et_extra_resaon)
	private FormEditText et_extra_resaon;// 事由类型
	/** @注释：结束时间 */
	@ViewInject(R.id.et_extra_time)
	private FormEditText et_extra_time;// 结束时间
	/** @注释：事由说明 */
	@ViewInject(R.id.et_extra_resaon_say)
	private FormEditText et_extra_resaon_say;// 事由说明
	/** @注释：开始时间 */
	@ViewInject(R.id.et_extra_date)
	private FormEditText et_extra_date;// 开始时间

	// button
	@ViewInject(R.id.bt_extra_save)
	private Button bt_extra_save;
	@ViewInject(R.id.bt_commit)
	private RadioButton bt_extra_commit;
	@ViewInject(R.id.bt_update)
	private RadioButton bt_update;
	@ViewInject(R.id.bt_uncommit)
	private RadioButton bt_uncommit;
	@ViewInject(R.id.bt_add)
	private RadioButton bt_add;

	@ViewInject(R.id.ly_bottom_save)
	private LinearLayout ly_bottom_save;
	@ViewInject(R.id.ly_bottom_handler)
	private LinearLayout ly_bottom_handler;

	@ViewInject(R.id.ry_extra_no)
	private RelativeLayout ry_extra_no;
	@ViewInject(R.id.ry_extra_encode)
	private RelativeLayout ry_extra_encode;
	@ViewInject(R.id.ry_extra_deparment)
	private RelativeLayout ry_extra_deparment;
	@ViewInject(R.id.ry_leader_no)
	private RelativeLayout ry_leader_no;
	@ViewInject(R.id.ry_leader_name)
	private RelativeLayout ry_leader_name;
	@ViewInject(R.id.ry_extra_reason)
	private RelativeLayout ry_extra_reason;
	@ViewInject(R.id.ry_extra_time)
	private RelativeLayout ry_extra_time;
	@ViewInject(R.id.ry_extra_reason_say)
	private RelativeLayout ry_extra_reason_say;
	@ViewInject(R.id.ry_extra_date)
	private RelativeLayout ry_extra_date;


	private final static int SUCCESS_SAVE = 1;
	private final static int SUCCESS_PRE = 0;
	private final static int SUCCESS_PRECODE = 6;
	private final static int SUCCESS_COMMIT = 2;
	private final static int SUCCESS_UNCOMMIT = 3;
	private final static int SUCCESS_UPDATE = 4;
	private final static int SUCCESS_DELETE = 5;
	
	private final static int SUCCESS_REASON = 9;
	private final static int SUCCESS_MANKIND = 10;

	private DateTimePickerDialog dateDialog;
	private SingleDialog singleDialog;
	private SingleDialog singleTimeDialog;
	private int va_id;
	private String va_code;
	private String en_code;// dbfing员工编号
	private String jsondata;
	private String dialogTite;
	private List<String> lists = new ArrayList<String>();

	private Context ct;
	private CustomProgressDialog progressDialog;

	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS_COMMIT:
				progressDialog.dismiss();
				try {
					Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
							.getData().getString("result"));
					if ((Boolean) keMap.get("success")) {
						ToastMessage("提交成功！");
						bt_uncommit.setTextColor(getResources().getColor(
								R.color.black));
						bt_uncommit.setEnabled(true);
						bt_uncommit.setChecked(false);
						bt_extra_commit.setTextColor(getResources().getColor(
								R.color.gray));
						bt_extra_commit.setEnabled(false);
						bt_extra_commit.setChecked(true);
						bt_update.setTextColor(getResources().getColor(
								R.color.gray));
						bt_update.setEnabled(false);
						bt_update.setChecked(true);
						editnoclik();
					}
				} catch (Exception e) {
					messageDisplayCommit(msg);
					editnoclik();
				}
				System.out.println("提交 result:"
						+ msg.getData().getString("result"));
				break;
			case SUCCESS_DELETE:

				break;
			case SUCCESS_PRE:
				System.out.println("获取id result:"
						+ msg.getData().getString("result"));
				va_id = Integer.valueOf(FlexJsonUtil
						.fromJson(msg.getData().getString("result")).get("id")
						.toString());
				getCodeHttpData();
				break;
			case SUCCESS_PRECODE:
				va_code = FlexJsonUtil
						.fromJson(msg.getData().getString("result"))
						.get("code").toString();
				System.out.println("va_code=" + va_code);
				httpSave();
				break;
			case SUCCESS_SAVE:
				progressDialog.dismiss();
				String result = msg.getData().getString("result");
				try {
					Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
							.getData().getString("result"));
					if ((Boolean) keMap.get("success")) {
						ToastMessage("保存成功！");
						bt_update.setEnabled(true);
						bt_update.setChecked(false);
						bt_update.setTextColor(getResources().getColor(R.color.black));
						bt_extra_commit.setEnabled(true);
						bt_extra_commit.setChecked(false);
						bt_extra_commit.setTextColor(getResources().getColor(R.color.black));
						bt_uncommit.setEnabled(false);
						bt_uncommit.setChecked(true);
						bt_uncommit.setTextColor(getResources().getColor(R.color.gray));
						ly_bottom_save.setVisibility(View.GONE);
						ly_bottom_handler.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					messageDisplay(msg);
				}
				System.out.println("保存 result:" + result);
				break;
			case SUCCESS_UNCOMMIT:
				progressDialog.dismiss();
				try {
					Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
							.getData().getString("result"));
					if ((Boolean) keMap.get("success")) {
						ToastMessage("反提交成功！");
						bt_extra_commit.setTextColor(getResources().getColor(
								R.color.black));
						bt_extra_commit.setEnabled(true);
						bt_extra_commit.setChecked(false);
						bt_update.setTextColor(getResources().getColor(
								R.color.black));
						bt_update.setEnabled(true);
						bt_update.setChecked(false);
						bt_uncommit.setTextColor(getResources().getColor(
								R.color.gray));
						bt_uncommit.setEnabled(false);
						bt_uncommit.setChecked(true);
						editclik();
					}
				} catch (Exception e) {
					messageDisplay(msg);
				}
				System.out.println("反提交 result:"
						+ msg.getData().getString("result"));
				break;
			case SUCCESS_UPDATE:
				progressDialog.dismiss();
				try {
					Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
							.getData().getString("result"));
					if ((Boolean) keMap.get("success")) {
						ToastMessage("更新成功！");
					}
				} catch (Exception e) {
					messageDisplay(msg);
				}
				System.out.println("更新 result:"
						+ msg.getData().getString("result"));
				break;
			case Constants.SUCCESS_INITDATA:
				progressDialog.dismiss();
				// 初始化数据
				try {
					String jsondata = msg.getData().getString("result");
					Map<String, Object> map = FlexJsonUtil.fromJson(jsondata);
					Log.i("jsondata",
							"init paneldata  json=" + "["
									+ FlexJsonUtil.toJson(map.get("panelData"))
									+ "]");
					List<ExtraLeaveEntity> leaveEntities = FlexJsonUtil
							.fromJsonArray(
									"[" + FlexJsonUtil.toJson(map.get("panelData"))
											+ "]", ExtraLeaveEntity.class);
					initDataFromServer(leaveEntities);
				} catch (Exception e) {
					ViewUtil.ShowMessageTitle(ct, "数据解析异常");
				}

				break;
				
			case SUCCESS_MANKIND:
				progressDialog.dismiss();
				  lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
	       					"combdatas");
	            	   if (lists.isEmpty()) {
						lists.add("无");
					   }
	            	   showTimeDialog(findViewById(R.id.et_extra_no));
				break;
			case SUCCESS_REASON:
				progressDialog.dismiss();
				  lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
	       					"combdatas");
	            	   if (lists.isEmpty()) {
						lists.add("无");
					   }
				showSimpleDialog(findViewById(R.id.et_extra_resaon));
				break;
				case Constants.APP_SOCKETIMEOUTEXCEPTION:
					String exception=msg.getData().getString("result");
					ViewUtil.ToastMessage(mContext, exception);
					progressDialog.dismiss();
					break;
			default:
				break;
			}
		};
	};

	/**
	 * @param leaveEntities
	 */
	private void initDataFromServer(List<ExtraLeaveEntity> leaveEntities) {
		ExtraLeaveEntity leaveEntitie = leaveEntities.get(0);
		et_extra_encode.setText(leaveEntitie.getSa_appman());
		et_extra_deparment.setText(leaveEntitie.getSa_department());
		et_leader_no.setText(leaveEntitie.getSa_mankind());
		et_extra_resaon.setText(leaveEntitie.getSa_reason());
		et_extra_resaon_say.setText(leaveEntitie.getSa_reasonremark());

		
	
		et_extra_date.setText(leaveEntitie.getSa_appdate().substring(0,
				leaveEntitie.getSa_appdate().length()-3));
		et_extra_time.setText(leaveEntitie.getSa_enddate().substring(0, 
				leaveEntitie.getSa_enddate().length()-3));

		String status = leaveEntities.get(0).getSa_status();
		if (!StringUtil.isEmpty(status)) {
			if ("已提交".equals(status)) {
				bt_extra_commit.setTextColor(getResources().getColor(
						R.color.gray));
				bt_extra_commit.setEnabled(false);
				bt_extra_commit.setChecked(true);
				bt_update.setTextColor(getResources().getColor(R.color.gray));
				bt_update.setEnabled(false);
				bt_update.setChecked(true);
				editnoclik();
			}
			if ("在录入".equals(status)) {
				bt_uncommit.setTextColor(getResources().getColor(R.color.gray));
				bt_uncommit.setEnabled(false);
				bt_uncommit.setChecked(true);
				editclik();
			}
		}
		va_id = leaveEntitie.getSa_id();
		va_code = leaveEntitie.getSa_code();

		ly_bottom_save.setVisibility(View.GONE);
		ly_bottom_handler.setVisibility(View.VISIBLE);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	public void initView() {
		setContentView(R.layout.from_extra_leave);
		ViewUtils.inject(this);
		ct=this;
		progressDialog = CustomProgressDialog.createDialog(this);
		setTitle("特殊考勤");
		et_extra_date.setKeyListener(null);
		et_extra_time.setKeyListener(null);
		et_extra_resaon.setKeyListener(null);
		et_extra_encode.setKeyListener(null);
		et_leader_no.setKeyListener(null);
		et_extra_deparment.setKeyListener(null);
		et_extra_date.setFocusable(false);
		et_extra_time.setFocusable(false);
		et_extra_resaon.setFocusable(false);
		et_extra_encode.setFocusable(false);
		et_leader_no.setFocusable(false);
		et_extra_deparment.setFocusable(false);
		et_extra_encode.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));
		et_extra_encode.setOnClickListener(this);
		et_leader_no.setOnClickListener(this);
		et_extra_date.setOnClickListener(this);
		et_extra_resaon.setOnClickListener(this);
		et_extra_time.setOnClickListener(this);

		bt_extra_save.setOnClickListener(this);
		bt_extra_commit.setOnClickListener(this);
		bt_add.setOnClickListener(this);
		bt_update.setOnClickListener(this);
		bt_uncommit.setOnClickListener(this);

		ry_extra_no.setOnClickListener(this);
		ry_extra_encode.setOnClickListener(this);
		ry_extra_deparment.setOnClickListener(this);
		ry_leader_no.setOnClickListener(this);
		ry_leader_name.setOnClickListener(this);
		ry_extra_reason.setOnClickListener(this);
		ry_extra_time.setOnClickListener(this);
		ry_extra_reason_say.setOnClickListener(this);
		ry_extra_date.setOnClickListener(this);
	}

	public void initData() {
		Intent intent = getIntent();
		String formCondition = intent.getStringExtra("formCondition");
		String gridCondition = intent.getStringExtra("gridCondition");
		String caller = intent.getStringExtra("caller");
		if (!StringUtil.isEmpty(caller)) {
			String url = CommonUtil.getAppBaseUrl(this)
					+ "mobile/common/getPanel.action";
			String sessionId = CommonUtil.getSharedPreferences(this,
					"sessionId");
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("caller", caller);
			params.put("formCondition", formCondition);
			params.put("gridCondition", gridCondition);
			params.put("sessionId", sessionId);
			progressDialog.show();

			LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
			headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
			ViewUtil.httpSendRequest(ct, url, params, handler, headers, Constants.SUCCESS_INITDATA, null, null, "get");
		} else {
//			getPreHttpData();
//			getCodeHttpData();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ry_extra_time:
			showDialog(v);
			break;
		case R.id.et_extra_time:
			showDialog(v);
			break;
		case R.id.ry_extra_date:
			showDialog(v);
			break;
		case R.id.bt_extra_save:// 保存
			if (et_extra_date.testValidity()
					&& et_extra_encode.testValidity()
					&& et_extra_resaon.testValidity()
					&& et_extra_resaon_say.testValidity()
					&& et_extra_time.testValidity()
					&& et_leader_no.testValidity()) {
				boolean falg = ViewUtil.isCheckDateTime(et_extra_date.getText().toString()
						, et_extra_time.getText().toString()
						, "yyyy-MM-dd HH:mm");
				if (falg) {
					ToastMessage("结束时间小于开始时间！");
				}else{
				progressDialog.show();
				getPreHttpData();
				}
			}

			break;

		case R.id.bt_commit:// 提交
			progressDialog.show();
			httpCommit();
			break;
		case R.id.bt_add:// 新增
			editclik();
			et_extra_date.setText("");
			et_extra_time.setText("");
			et_extra_encode.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));
			et_extra_deparment.setText("");
			et_leader_no.setText("");
			et_extra_resaon.setText("");
			et_extra_resaon_say.setText("");
			ly_bottom_save.setVisibility(View.VISIBLE);
			ly_bottom_handler.setVisibility(View.GONE);
			break;
		case R.id.bt_uncommit:// 反提交
			progressDialog.show();
			httpUnCommit();
			break;
		case R.id.bt_update:// 更新
			if (et_extra_date.testValidity()
					&& et_extra_encode.testValidity()
					&& et_extra_resaon.testValidity()
					&& et_extra_resaon_say.testValidity()
					&& et_extra_time.testValidity()
					&& et_leader_no.testValidity()) {
				boolean falg = ViewUtil.isCheckDateTime(et_extra_date.getText().toString()
						,et_extra_time.getText().toString()
						,"yyyy-MM-dd HH:mm");
				if (falg) {
					ToastMessage("结束时间小于开始时间！");
				}else{
			progressDialog.show();
			httpUpdate();}}
			break;
		case R.id.et_extra_resaon:// 事由
			progressDialog.show();
			dialogTite = "事由";
			loadDataForServer("sa_reason", SUCCESS_REASON);
			break;
		case R.id.ry_extra_reason:
		
			break;
		case R.id.et_extra_date:
			showDialog(v);
			break;

		case R.id.ry_extra_encode:
			Intent et_dbfind = new Intent(ct, DbfindListActivity.class);
			startActivityForResult(et_dbfind, 1);
			break;
		case R.id.et_extra_encode:
			et_dbfind = new Intent(ct, DbfindListActivity.class);
			startActivityForResult(et_dbfind, 1);
			break;
		case R.id.et_leader_no:
			progressDialog.show();
			dialogTite = "员工类型";
			loadDataForServer("sa_mankind", SUCCESS_MANKIND);
			;
			break;

		default:
			break;
		}
	}

	/** @注释：获取主键ID */
	public void getPreHttpData() {
		String url = CommonUtil.getAppBaseUrl(ct) + "common/getId.action";
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("seq", "VACATION_SEQ");
		params.put("sessionId",
				CommonUtil.getSharedPreferences(ct, "sessionId"));

		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRE, null, null, "get");
	}

	/** @注释：获取Code */
	public void getCodeHttpData() {
		String url = CommonUtil.getAppBaseUrl(ct)
				+ "common/getCodeString.action";
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("type", "2");
		params.put("caller", "SpeAttendance");
		params.put("sessionId",
				CommonUtil.getSharedPreferences(ct, "sessionId"));
		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, handler, headers,  SUCCESS_PRECODE, null, null, "get");
	}

	/** @注释：保存 */
	public void httpSave() {
		ExtraLeaveEntity entity = getSaveJsonData();
		// 路径
		String url = CommonUtil.getAppBaseUrl(ct)
				+ "hr/attendance/saveSpeAttendance.action";
		Map<String,Object> params = new HashMap<String,Object>();

		System.out.println("url:" + url);
		jsondata = FlexJsonUtil.toJson(entity);
		System.out.println("formStore=" + jsondata);
		params.put("formStore", jsondata);
		params.put("sessionId",
				CommonUtil.getSharedPreferences(ct, "sessionId"));

		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_SAVE, null, null, "get");
	}

	private ExtraLeaveEntity getSaveJsonData() {
		ExtraLeaveEntity entity = new ExtraLeaveEntity();
		if (va_id != 0) {
			entity.setSa_id(va_id);
		}
		entity.setSa_code(va_code);
		entity.setSa_appmancode(CommonUtil.getSharedPreferences(mContext,"erp_username"));
		entity.setSa_appdate(et_extra_date.getText().toString());
		entity.setSa_enddate(et_extra_time.getText().toString());
		entity.setSa_appman(et_extra_encode.getText().toString());
//		entity.setSa_department(et_extra_deparment.getText().toString());
		entity.setSa_mankind(et_leader_no.getText().toString());
		entity.setSa_reason(et_extra_resaon.getText().toString());
		entity.setSa_reasonremark(et_extra_resaon_say.getText().toString());
		entity.setSa_status("在录入");
		entity.setSa_statuscode("ENTERING");
		entity.setSa_recorddate(new SimpleDateFormat("yyyy-MM-dd")
				.format(new Date()));
		entity.setSa_recorder(CommonUtil.getSharedPreferences(ct,
				"erp_emname"));
		return entity;
	}

	public void httpCommit() {
		String url = CommonUtil.getAppBaseUrl(ct)
				+ "hr/attendance/submitSpeAttendance.action";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", String.valueOf(va_id));
		params.put("caller", "SpeAttendance");
		params.put("sessionId",
				CommonUtil.getSharedPreferences(ct, "sessionId"));

		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_COMMIT, null, null, "get");
	}

	public void httpUpdate() {
		String url = CommonUtil.getAppBaseUrl(ct)
				+ "hr/attendance/updateSpeAttendance.action";
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", String.valueOf(va_id));
		params.put("caller", "SpeAttendance");
		if (StringUtil.isEmpty(jsondata)) {
			ExtraLeaveEntity entity = getSaveJsonData();
			jsondata = FlexJsonUtil.toJson(entity);
		}
		params.put("formStore", jsondata);
		params.put("sessionId",
				CommonUtil.getSharedPreferences(ct, "sessionId"));

		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UPDATE, null, null, "get");
	}

	public void httpUnCommit() {
		String url = CommonUtil.getAppBaseUrl(ct)
				+ "hr/attendance/resSubmitSpeAttendance.action";
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", String.valueOf(va_id));
		params.put("caller", "SpeAttendance");
		params.put("sessionId",
				CommonUtil.getSharedPreferences(ct, "sessionId"));

		LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UNCOMMIT, null, null, "get");
	}

	public void showDialog(final View v) {
		if (dateDialog == null) {
			dateDialog = new DateTimePickerDialog(this,
					System.currentTimeMillis());
		}

		dateDialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
			public void OnDateTimeSet(AlertDialog dia, long date) {
				if (v.getId() == R.id.et_extra_time) {
					et_extra_time.setText(CommonUtil.getStringDate(date));
					/** @注释：保证 初始化当前时间 */
					dateDialog = null;
				}
				if (v.getId() == R.id.et_extra_date) {
					et_extra_date.setText(CommonUtil.getStringDate(date));
					/** @注释：保证 初始化当前时间 */
					dateDialog = null;
				}

			}
		});

		if (!dateDialog.isShowing()) {
			dateDialog.show();
		}
	}

	public void showSimpleDialog(View view) {
		if (singleDialog == null) {
			singleDialog = new SingleDialog(ct, dialogTite,
					new SingleDialog.PickDialogListener() {

						@Override
						public void onListItemClick(int position, String value) {
							et_extra_resaon.setText(value);
						}
					});
			singleDialog.show();
			singleDialog.initViewData(lists);
		} else {
			singleDialog.show();
			singleDialog.initViewData(lists);
		}
	}

	public void showTimeDialog(View view) {
		if (singleTimeDialog == null) {
			singleTimeDialog = new SingleDialog(ct, dialogTite,
					new SingleDialog.PickDialogListener() {

						@Override
						public void onListItemClick(int position, String value) {
							et_leader_no.setText(value);
						}
					});
			singleTimeDialog.show();
			singleTimeDialog.initViewData(lists);
		} else {
			singleTimeDialog.show();
			singleTimeDialog.initViewData(lists);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (data == null) {
				return;
			}
			String en_name = data.getStringExtra("en_name");
			en_code = data.getStringExtra("en_code");
			et_extra_encode.setText(en_name);
			et_extra_deparment.setText(data.getStringExtra("en_depart"));
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 系统提示信息
	 * 
	 * @param msg
	 */
	private void messageDisplay(Message msg) {
		String message = FlexJsonUtil
				.fromJson(msg.getData().getString("result"))
				.get("exceptionInfo").toString();
		ViewUtil.ShowMessageTitle(ct, message);
		bt_extra_commit.setTextColor(getResources().getColor(R.color.grey));
		bt_uncommit.setTextColor(getResources().getColor(R.color.black));
	}

	/**
	 * 提交操作，异常也算成功，故写此方法
	 * 
	 * @param msg
	 */
	private void messageDisplayCommit(Message msg) {
		String message = FlexJsonUtil
				.fromJson(msg.getData().getString("result"))
				.get("exceptionInfo").toString();
		ViewUtil.ShowMessageTitle(ct, message);
		bt_extra_commit.setTextColor(getResources().getColor(R.color.grey));
		bt_extra_commit.setEnabled(false);
		bt_extra_commit.setChecked(true);
		bt_uncommit.setTextColor(getResources().getColor(R.color.black));
		bt_uncommit.setEnabled(true);
		bt_uncommit.setChecked(false);
		bt_update.setTextColor(getResources().getColor(R.color.grey));
		bt_update.setEnabled(false);
		bt_update.setChecked(true);
	}
	
	
	public void editnoclik(){
		et_extra_date.setEnabled(false);
		et_extra_time.setEnabled(false);
		et_extra_encode.setEnabled(false);
		et_leader_no.setEnabled(false);
		et_extra_resaon.setEnabled(false);
		et_extra_resaon_say.setEnabled(false);
	}
	
	public void editclik(){
		et_extra_date.setEnabled(true);
		et_extra_time.setEnabled(true);
		et_extra_encode.setEnabled(true);
		et_leader_no.setEnabled(true);
		et_extra_resaon.setEnabled(true);
		et_extra_resaon_say.setEnabled(true);
	}
	
	
	public void loadDataForServer(String field,int what){
    	Log.i("leave", "what="+what);
    	String url=CommonUtil.getAppBaseUrl(ct)+"mobile/common/getCombo.action";
		Map<String, String> param = new HashMap<String, String>();
		param.put("caller", "SpeAttendance");
		param.put("field", field);
		param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.getDataFormServer(ct,handler, url, param, what);
    }

}
