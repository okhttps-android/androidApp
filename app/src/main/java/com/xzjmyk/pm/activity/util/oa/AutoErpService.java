//package com.xzjmyk.pm.activity.util.oa;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.v4.content.LocalBroadcastManager;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.alibaba.fastjson.JSON;
//import com.common.LogUtil;
//import com.common.data.StringUtil;
//import com.common.preferences.PreferenceUtils;
//import com.common.thread.ThreadUtil;
//import com.core.app.AppConfig;
//import com.core.app.AppConstant;
//import com.core.app.MyApplication;
//import com.core.broadcast.MsgBroadcast;
//import com.core.dao.work.WorkModelDao;
//import com.core.net.http.http.OAHttpHelper;
//import com.core.utils.NotificationManage;
//import com.core.utils.TimeUtils;
//import com.uas.appme.pedometer.service.StepService;
//import com.uas.appworks.OA.erp.utils.AutoErpSigninUitl;
//import com.uas.appworks.dao.MissionDao;
//import com.xzjmyk.pm.activity.ui.MainActivity;
//import com.xzjmyk.pm.activity.ui.erp.model.book.SureBookModel;
//import com.xzjmyk.pm.activity.ui.me.TimeStatisticsActivity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//
///**
// * 自动Erp服务，开启线程
// */
//public class AutoErpService extends Service {
//
//	private NotificationManage notificationManage;//通知管理器
//
//	private final long INTERVAL = 2 * 60 * 1000;//每两分钟回来一次，防止时间太久出现回不来情况 通过 iter来判断是否进入判断
//	private final int DNOTICE_ITER = 10; //TODO 测试用1 ，之后改为10
//
//	private AutoErpSigninUitl signinUitl;
//	private DepositNoticeUtil dnoticeUtil;
//	private boolean canDNotice = false;
//
//	private final String[] flags = {
//			AppConstant.CHANGE_WORK_TASK,//
//			AppConstant.CHANGE_MISSION_TASK,//
//			AppConstant.INIT_MISSION_TASK,//初始化外勤签到任务
//			AppConstant.UPDATA_ERP_CHANGE,//更换公司
//			AppConstant.DEPOSIT_NOTICE_TASK,//预约计划提醒任务
//			MsgBroadcast.ACTION_MSG_COMPANY_UPDATE//切换账套
//	};
//
//
//	private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			try {
//				if (intent == null || StringUtil.isEmpty(intent.getAction())) return;
//				else if (intent.getAction().equals(AppConstant.UPDATA_ERP_CHANGE)
//						|| intent.getAction().equals(MsgBroadcast.ACTION_MSG_COMPANY_UPDATE)) {//账套变更，重新刷新数据，初始化任务
//					String falg = intent.getStringExtra("falg");
//					if (StringUtil.isEmpty(falg) || falg.equals("home")) {
//
//						init();
//					}
//				} else if (intent.getAction().equals(AppConstant.DEPOSIT_NOTICE_TASK)) {
//					canDNotice = intent.getBooleanExtra(AppConstant.DEPOSIT_NOTICE_TASK, true);
//					log("canDNotice=" + canDNotice);
//					if (canDNotice && !isDNoticeRuning) {
//						startDNoticeTask();  //进行预约计划提醒操作
//					} else {
//						dnoticeIter = 0;
//					}
//				}
//			} catch (Exception e) {
//				if (e != null) log("BroadcastReceiver Exception=" + e.getMessage());
//			}
//			//记住上次状态，开启计步服务
//			if (PreferenceUtils.getInt(MyApplication.getInstance(), MainActivity.UU_STEP) == 1 && intent.getAction().equals("uu.step.destory")) {
//				Intent i = new Intent(context, StepService.class);
//				context.startService(i);
//			}
//		}
//	};
//
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		throw new UnsupportedOperationException("Not yet implemented");
//	}
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		try {
//			initBroadcast();//初始化广播接受器
//			initDB();//初始化数据库数据，1.删除昨天数据   2.下拉当天数据
//			init();//初始化
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return super.onStartCommand(intent, flags, startId);
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		LocalBroadcastManager.getInstance(MyApplication.getInstance()).unregisterReceiver(updateReceiver);
//		//TODO 获取此刻精确时间并转换为yyyy-MM-dd HH:mm:ss
//		String nowtime = TimeUtils.f_long_2_str(System.currentTimeMillis());
//		String saved_time = PreferenceUtils.getString(MyApplication.getInstance(), TimeStatisticsActivity.Killed);
//
//		if (!TextUtils.isEmpty(saved_time)) {
//			PreferenceUtils.putString(TimeStatisticsActivity.Killed, saved_time + "," + nowtime);
//		} else {
//			PreferenceUtils.putString(TimeStatisticsActivity.Killed, nowtime);
//
//		}
//	}
//
//
//	/**
//	 * 初始化广播
//	 */
//	private void initBroadcast() throws Exception {
//		IntentFilter filter = new IntentFilter();
//		if (flags != null && flags.length > 0) {
//			for (String e : flags)
//				filter.addAction(e);
//		}
//		LocalBroadcastManager.getInstance(MyApplication.getInstance()).registerReceiver(updateReceiver, filter);
//		registerReceiver(updateReceiver, filter);
//	}
//
//
//	private int dnoticeIter;
//	private Boolean isDNoticeRuning = false;
//	private boolean dnotice = false;
//
//	/**
//	 * 当天已确认的预约计划通知
//	 *
//	 * @throws Exception
//	 */
//	private void startDNoticeTask() throws Exception {
//		Log.i("startDNoticeTask", "startDNoticeTask");
//		dnoticeIter = 0;
//		isDNoticeRuning = true;
//		ThreadUtil.getInstance().addLoopTask(new Runnable() {
//			@Override
//			public void run() {
//				while (true) {
//					try {
//						if (!isCanDNotice()) {
//							isDNoticeRuning = false;
//							break;
//						}
//						dnotice = true;
//						isDNoticeRuning = true;
//						int i = DNOTICE_ITER - 1;
//						Log.i("dnoticeIter", dnoticeIter + "");
//
//						if (dnoticeIter == i) {
////                            MyApplication.getInstance().getBdLocationHelper().requestLocation();   // 暂时去掉定位
//						}
//						if (dnoticeIter == 0 || dnoticeIter == DNOTICE_ITER) {
//							dnoticeIter = 0;
//							//TODO 修改
//							DepositNoticeUtil.isNoticeTime(mSureBookModel);
//
//						}
//						dnoticeIter++;
//						dnotice = false;
//						Thread.sleep(INTERVAL);
//					} catch (InterruptedException e) {
//						if (e != null)
//							log("startDNoticeTask InterruptedException=" + e.getMessage());
//					} catch (Exception e) {
//						if (e != null)
//							log("startDNoticeTask Exception=" + e.getMessage());
//					}
//				}
//			}
//		});
//
//	}
//
//
//	/**
//	 * 初始化数据库,1.清空昨天数据  2.缓存当天数据
//	 */
//	private void initDB() {
//		//  刚进来时候清空昨天数据
//		try {
//			WorkModelDao.getInstance().clearByDate(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
//			MissionDao.getInstance().clearAll(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
//		} catch (Exception e) {
//			if (e != null)
//				log("initDB Exception=" + e.getMessage());
//		}
//		try {//TODO 缓存数据
//
//		} catch (Exception e) {
//
//		}
//	}
//
//	private void init() throws Exception {
//		MyApplication.getInstance().getBdLocationHelper().requestLocation();
//		if (dnoticeUtil == null)
//			dnoticeUtil = new DepositNoticeUtil();
//		if (signinUitl == null)
//			signinUitl = new AutoErpSigninUitl();
//		OAHttpHelper.getInstance().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					initDNotice();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}, 5000);
//
//	}
//
//	/**
//	 * 获取已确认预约
//	 */
//	private List<SureBookModel> mSureBookModel;
//
//	private void initDNotice() {
//		DepositNoticeUtil m = new DepositNoticeUtil();
//		m.loadDepostNotice();
//		mSureBookModel = new ArrayList<>();
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				mSureBookModel = DepositNoticeUtil.getsureBookData();
//				LogUtil.prinlnLongMsg("mSureBookModel", JSON.toJSONString(mSureBookModel));
//			}
//		}, 5000);
//	}
//
//
//	private boolean isCanDNotice() {
//		return canDNotice;
//	}
//
//
//	/**
//	 * start handler date
//	 */
//	//防止空值
//	private NotificationManage getNotificationManage() {
//		if (notificationManage == null)
//			notificationManage = new NotificationManage();
//		return notificationManage;
//	}
//
//	/**
//	 * 打印信息方法
//	 *
//	 * @param message 信息内容
//	 */
//	private void log(String message) {
//		try {
//			if (!AppConfig.DEBUG || StringUtil.isEmpty(message)) return;
//			Log.i("gongpengming", message);
//		} catch (Exception e) {
//			if (e != null)
//				Log.i("gongpengming", "log Exception=" + e.getMessage());
//		}
//	}
//}
