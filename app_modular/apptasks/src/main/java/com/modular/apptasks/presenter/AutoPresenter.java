package com.modular.apptasks.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.thread.ThreadUtil;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.dao.WorkLocationDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.MissionModel;
import com.core.model.WorkLocationModel;
import com.core.model.WorkModel;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.modular.apptasks.util.AlarmUtil;
import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.OA.erp.utils.AutoErpSigninUitl;
import com.uas.appworks.dao.MissionDao;

import java.util.List;

/**
 * 创建新的自动任务管理类，由MainActivity控制，和MainActivity生命周期同步
 * 1.定时任务由服务的循环变成Alarm模式，节约电
 * Created by Bitliker on 2017/10/11.
 */
public class AutoPresenter {
	private final String[] flags = {
			AppConstant.CHANGE_WORK_TASK,//
			AppConstant.CHANGE_MISSION_TASK,//外勤计划列表有变化时候
			AppConstant.UPDATA_ERP_CHANGE//切换账套
	};

	public static AutoPresenter onCreate() {
		return new AutoPresenter();
	}

	private AutoErpSigninUitl signinUitl;

	private AutoPresenter() {
		signinUitl = new AutoErpSigninUitl();
		initBroadcast();
		reckonWork(true);
		reckonMission(true);
		CommonInterface.getInstance().judgeManager(null);
		CommonInterface.getInstance().getOutSetInfo(null);
	}


	public void onDestroy() {
		LocalBroadcastManager.getInstance(BaseConfig.getContext()).unregisterReceiver(updateReceiver);
	}


	/* 初始化广播	 */
	private void initBroadcast() {
		IntentFilter filter = new IntentFilter();
		if (flags != null && flags.length > 0) {
			for (String e : flags)
				filter.addAction(e);
		}
		LocalBroadcastManager.getInstance(BaseConfig.getContext()).registerReceiver(updateReceiver, filter);
	}

	private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || StringUtil.isEmpty(intent.getAction())) return;
			LogUtil.i("onReceive=" + intent.getAction());
			switch (intent.getAction()) {
				case AppConstant.CHANGE_WORK_TASK:
					//判断是否开启任务
					if (intent.getBooleanExtra(AppConstant.CHANGE_WORK_TASK, true)) {
						reckonWork(false);
					}
					break;
				case AppConstant.UPDATA_ERP_CHANGE://账套改变
					CommonInterface.getInstance().getOutSetInfo(null);
					CommonInterface.getInstance().judgeManager(null);
					reckonWork(true);
					reckonMission(true);
					break;
				case AppConstant.CHANGE_MISSION_TASK:
					if (intent.getBooleanExtra(AppConstant.CHANGE_MISSION_TASK, true)) {
						reckonMission(false);
					}
					break;

			}
		}
	};


	private void reckonWork(final boolean loadAble) {
		ThreadUtil.getInstance().addTask(new Runnable() {
			@Override
			public void run() {
				reckonWorkThread(loadAble);
			}
		});
	}

	private void reckonMission(final boolean loadAble) {
		ThreadUtil.getInstance().addTask(new Runnable() {
			@Override
			public void run() {
				try {
					reckonMissionThread(loadAble);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/***********************start reckon work********************************************/
	/**
	 * 尽最少的计算，判断时候需要打卡或是下一次打卡的时间戳
	 *
	 * @param loadAble 是否允许请求
	 */
	private void reckonWorkThread(boolean loadAble) {
		LogUtil.i("gong","reckonWorkThread");
		List<WorkModel> models = WorkModelDao.getInstance().queryAuto();
		if (!ListUtils.isEmpty(models)) {
			String newHHMM = DateFormatUtil.long2Str(DateFormatUtil.HM);
			for (WorkModel m : models) {
				if (satisfyModelThread(newHHMM, m)) {
					break;
				}
			}
		} else if (loadAble) {
			OAHttpHelper.getInstance().post(new Runnable() {
				@Override
				public void run() {
					signinUitl.loadWorkSet();
				}
			});
		}
	}


	/*判断班次是否符合打卡或是提交定时任务*/
	private boolean satisfyModelThread(String newHHMM, WorkModel model) {
		//1.判断上班(打卡条件：在上班开始时间到上班时间可以打卡)
		//1.1 判断上班有没有打卡
		if (StringUtil.isEmpty(model.getWorkSignin())) {
			//1.2 判断当前时间是否在上班结束时间之前,当属于该范围，可以为他打卡
			if (newHHMM.compareTo(model.getWorkTime()) <= 0) {
				//1.3 判断当前时间在上班开始时间之后，符合打卡==》前往打卡
				if (newHHMM.compareTo(model.getWorkStart()) >= 0) {
					saveWorkLogThread(true, model);
					return true;
				} else {//当前时间在上班开始时间之前，需要定闹铃==》前往闹铃
					pushAlarmThread(true, model);
					return true;
				}

			}//如果不符合，需要计算下班范围
		}
		//2.判断下班
		//2.1 判断下班有没有打卡
		if (StringUtil.isEmpty(model.getOffSignin())) {
			//2.2 判断当前时间是否在下班结束范围之内,当属于该范围，可以为他打卡
			if (newHHMM.compareTo(model.getOffend()) <= 0) {
				//1.3 判断当前时间在下班时间之后，符合打卡==》前往打卡
				if (newHHMM.compareTo(model.getOffTime()) >= 0) {
					LogUtil.i("gong","符合打卡");
					saveWorkLogThread(false, model);
					return true;
				} else {
					pushAlarmThread(false, model);
					return true;
				}
			}
		}
		return false;
	}

	/*符合条件，进行判断距离和打卡操作*/
	private void saveWorkLogThread(final boolean isWork, final WorkModel model) {
		LogUtil.i("gong","请求位置");
		UasLocationHelper.getInstance().requestLocation(new OnLocationListener() {
			@Override
			public void onReceiveLocation(UASLocation mUASLocation) {
				Log.i("gong","				break;onReceiveLocation");
				//判断是否符合打卡
				boolean isInWorkPlce=false;
				List<WorkLocationModel> beanList = WorkLocationDao.getInstance().queryByEnCode();
				if (!ListUtils.isEmpty(beanList)) {
					for (WorkLocationModel bean : beanList) {
						if (LocationDistanceUtils.distanceMeBack(bean.getLocation()) < bean.getValidrange()) {
							isInWorkPlce=true;
							break;
						}
					}
				}
				if (isInWorkPlce) {
					signinUitl.signinWork(isWork,model);
				}else {
					//不符合打卡情况下，去判断下一次打卡时间
					ThreadUtil.getInstance().addTask(new Runnable() {
						@Override
						public void run() {
							pushAlarmThread(isWork, model);
						}
					});
				}
			}
		});
	}

	/*提交定时任务*/
	private void pushAlarmThread(boolean isWork, WorkModel model) {
		long nextTime = 0;
		long startTime = 0;
		long endTime = 0;
		if (isWork) {
			long workStart = DateFormatUtil.hhmm2Long(model.getWorkStart());
			long workTime = DateFormatUtil.hhmm2Long(model.getWorkTime());
			startTime = Math.max(workStart, workTime - 1000 * 30 * 60);
			endTime = workTime;
		} else {
			startTime = DateFormatUtil.hhmm2Long(model.getOffTime());
			endTime = startTime + 1000 * 30 * 60;
		}
		while (startTime <= endTime) {
			if (startTime > System.currentTimeMillis()) {
				nextTime = startTime;

			}
			startTime += 5 * 60 * 1000;
		}
		AlarmUtil.startAlarm(AlarmUtil.ID_WORK, AlarmUtil.ACTION_WORK, nextTime);
	}


	/******************end reckon work**************************/

	/******************end reckon Mission**************************/

	private void reckonMissionThread(boolean loadAble) throws Exception {
		List<MissionModel> models = MissionDao.getInstance().queryByEnCode();
		if (!ListUtils.isEmpty(models)) {
			boolean isAuto = CommonUtil.getUserRole().equals("3") || PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
			if (isAuto) {
				//获取外勤的打卡
				nextMissionTimeThread(models);
			}
		} else if (loadAble) {
			OAHttpHelper.getInstance().post(new Runnable() {
				@Override
				public void run() {
					signinUitl.loadMissionPlan();
				}
			});
		}
	}


	/**
	 * 要求：
	 * 1.进来先判断当前符合不符合签到
	 *
	 * @param models
	 */
	private void nextMissionTimeThread(List<MissionModel> models) throws Exception {
		if (!ListUtils.isEmpty(models)) {
			MissionModel minModel = getMinSignMission(models);
			if (minModel != null) {//获取到打卡对象，说明需要打卡
				final MissionModel sigMissionModel = minModel;
				LogUtil.i("符合外勤打卡" + JSON.toJSONString(sigMissionModel));
				OAHttpHelper.getInstance().post(new Runnable() {
					@Override
					public void run() {
						LogUtil.i("主线程" + JSON.toJSONString(sigMissionModel));
						signinUitl.signinMission(sigMissionModel);
						//由于一次只能打一次卡，防止有符合打卡的多个单据出现太长时间不能打卡的情况
						AlarmUtil.startAlarm(AlarmUtil.ID_MISSION, AlarmUtil.ACTION_MISSION, System.currentTimeMillis() + 5 * 60 * 1000);
					}
				});
			} else {
				long mintime = getMinCallTime(models);
				AlarmUtil.startAlarm(AlarmUtil.ID_MISSION, AlarmUtil.ACTION_MISSION, mintime);
			}
		}
	}

	private long getMinCallTime(List<MissionModel> models) throws Exception {
		long minTime = -1;
		for (MissionModel model : models) {
			float distance = LocationDistanceUtils.distanceMe(model.getLatLng());
			long neerTimeByDis = getTimeByDistance(distance);
			long modelMinTime = neerTimeByDis;
			if (!StringUtil.isEmpty(model.getRealTime())) {//如果这个单据没有打过卡，那只需要计算当前位置和目的地的距离就可以计算出这个单需要什么时候回调
				long lastSign = lastSignMission(model);
				modelMinTime = Math.max(neerTimeByDis, lastSign + 18 * 60 * 1000);
			}
			if (minTime < 0 || minTime > modelMinTime) {
				minTime = modelMinTime;
			}
		}
		return minTime;
	}

	/**
	 * 判断距离和时间的对比为：一秒钟缩小20米   distance/20=s(所需要的秒数)
	 *
	 * @param distance
	 * @return
	 */
	private long getTimeByDistance(float distance) {
		return (long) (System.currentTimeMillis() + (distance / 20) * 1000);
	}

	/**
	 * 获取最符合打卡的单据
	 *
	 * @param models
	 * @return
	 */
	private MissionModel getMinSignMission(List<MissionModel> models) throws Exception {
		MissionModel minModel = null;
		float minDistance = 0;
		int companyDistance = PreferenceUtils.getInt(AppConfig.ALARM_MISSION_DISTANCE, 500);
		for (MissionModel model : models) {
			LogUtil.i("json=" + JSON.toJSONString(model));
			if (timeAllowMission(model)) {//时间上间隔是否足够签到
				if (model.getLatLng() != null) {//定位信息完整
					float distance = LocationDistanceUtils.distanceMe(model.getLatLng());
					if (distance > 0) {//获取当前与目的地的距离
						//优先判断签退
						if (model.getStatus() != 2 && distance > companyDistance && !StringUtil.isEmpty(model.getRealTime())) {
							//符合外勤签退  1.判断进行中的外勤计划，如果离开了签到最后一次，结束该计划
							minModel = model;
							minModel.setStatus(2);//结束该外勤
							break;
						} else if (distance < companyDistance && (model.getType() != 1 || model.getStatus() != 2)) {//判断签到
							if (minDistance <= 0 || minDistance > distance) {//2.如果第一个不存在，判断获取最近的位置
								minDistance = distance;
								minModel = model;
								if (minModel.getStatus() == 2)
									minModel.setStatus(3);
							}
						}
					}
				}
			}
		}//end for	
		return minModel;
	}

	/**
	 * 判断该对应班次最后一次签到时间和当前是否有足够长的时间间隔
	 *
	 * @param model
	 * @return
	 */
	private boolean timeAllowMission(MissionModel model) {
		if (model == null) return false;
		if (StringUtil.isEmpty(model.getRealTime())) return true;
		return long2LastTime(lastSignMission(model));
	}

	private long lastSignMission(MissionModel model) {
		String lastTime = StringUtil.isEmpty(model.getRealLeave()) ? model.getRealTime() : model.getRealLeave();
		return DateFormatUtil.str2Long(lastTime, DateFormatUtil.YMD_HMS);
	}

	/**
	 * 判断最后一次签到是否相隔足够长的时间
	 *
	 * @param last 最后一次签到时间  long
	 * @return
	 */
	private boolean long2LastTime(long last) {
		if ((System.currentTimeMillis() - last) > (15 * 60 * 1000)) {
			//如果遍历到了，但是时间相差很小，就更新
			return true;
		} else
			return false;
	}
}
