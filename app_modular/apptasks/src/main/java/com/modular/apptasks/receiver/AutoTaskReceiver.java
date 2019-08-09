package com.modular.apptasks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.common.LogUtil;
import com.core.app.AppConstant;
import com.core.base.BaseActivity;
import com.core.broadcast.MsgBroadcast;
import com.modular.apptasks.presenter.SchedulePresenter;
import com.modular.apptasks.util.AlarmUtil;
import com.uas.appworks.model.Schedule;

public class AutoTaskReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.length() > 0) {
                LogUtil.i("action=" + action);
                switch (action) {
                    case AlarmUtil.ACTION_WORK:
                        Intent work = new Intent();
                        work.setAction(AppConstant.CHANGE_WORK_TASK);
                        work.putExtra(AppConstant.CHANGE_WORK_TASK, true);
                        MsgBroadcast.sendLocalBroadcast(work);
                        break;
                    case AlarmUtil.ACTION_MISSION:
                        Intent mission = new Intent();
                        mission.setAction(AppConstant.CHANGE_MISSION_TASK);
                        mission.putExtra(AppConstant.CHANGE_MISSION_TASK, true);
                        MsgBroadcast.sendLocalBroadcast(mission);
                        break;
                    case AlarmUtil.ACTION_SCHEDULE:
                        try {
                            Schedule mSchedule = intent.getParcelableExtra(AlarmUtil.PARCELABLE);
                            if (mSchedule == null) {
                                SchedulePresenter.getInstance().startSchedule();
                                return;
                            }

                            String mRemarks = mSchedule.getRemarks();
                            if (TextUtils.isEmpty(mRemarks)) {
                                SchedulePresenter.getInstance().startSchedule();
                                return;
                            }
                            String content = "";
                            if (mRemarks.length() > 10) {
                                content = "您的日程 [" + mRemarks.substring(0, 9) + "...] 即将开始";
                            } else {
                                content = "您的日程 [" + mRemarks + "] 即将开始";
                            }
                            Intent schedule = new Intent();
                            schedule.setAction(BaseActivity.PROMPT_ACTION);
                            schedule.putExtra(BaseActivity.CONTENT, content);
                            MsgBroadcast.sendLocalBroadcast(schedule);
                            SchedulePresenter.getInstance().startSchedule();
                        } catch (Exception e) {

                        }
                        break;
                }
            }
        }
    }

}
