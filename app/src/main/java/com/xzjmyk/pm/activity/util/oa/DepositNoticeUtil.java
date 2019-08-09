package com.xzjmyk.pm.activity.util.oa;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.NotificationManage;
import com.core.utils.TimeUtils;
import com.uas.appme.pedometer.utils.TimeUtil;
import com.xzjmyk.pm.activity.ui.erp.activity.secretary.BookingListActivity;
import com.xzjmyk.pm.activity.ui.erp.model.book.SureBookModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ${FANGLH} on 2017/7/18.
 * Function： 预约计划，定时提醒：
 */

public class DepositNoticeUtil {
    private boolean isB2b;
    private static NotificationManage notificationManage;
    private static List<SureBookModel> mSureBookModel = new ArrayList<>();

    public DepositNoticeUtil() {
        notificationManage = new NotificationManage();
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = msg.getData().getString("result");
            switch (msg.what) {
                case 0x01:
                    if (!StringUtil.isEmpty(result)) {
                        if (JSON.parseObject(result).getString("resultMsg") != null) {
                            //先不提示
//                            Toast.makeText(MyApplication.getInstance(), JSON.parseObject(result).getString("resultMsg"), Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            if (!ListUtils.isEmpty(mSureBookModel))
                                mSureBookModel.clear();
                            JSONArray array = JSON.parseObject(result).getJSONArray("bookinglist");
                            if (!ListUtils.isEmpty(array)) {
                                mSureBookModel.addAll(JSON.parseArray(array.toString(), SureBookModel.class));
                                getsureBookData();
                                Intent intent = new Intent();
                                intent.putExtra(AppConstant.DEPOSIT_NOTICE_TASK, true);
                                intent.setAction(AppConstant.DEPOSIT_NOTICE_TASK);
                                MsgBroadcast.sendLocalBroadcast(intent);
                                Log.i("0x01result", intent.toString());
                            }
                        }
                    }
                    LogUtil.prinlnLongMsg("0x01result", result);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (JSONUtil.validate(result)) {
                        Toast.makeText(MyApplication.getInstance(), JSON.parseObject(result).getString("exceptionInfo"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MyApplication.getInstance(), result, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    public void loadDepostNotice() {
        String curDate = TimeUtils.s_long_2_str(DateFormatUtil.str2Long(TimeUtil.getCurrentDate(), "yyyy年MM月dd日"));
        String yyyymmdd = curDate.replaceAll("-", "");
        String url = Constants.IM_BASE_URL() + "user/appCurrentList";
        Map<String, Object> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("userid", MyApplication.getInstance().mLoginUser.getUserId());
        params.put("yearmonth", yyyymmdd);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId"));
        ViewUtil.httpSendRequest(MyApplication.getInstance(), url, params, mHandler, headers, 0x01, null, null, "post");
    }

    public static void isNoticeTime(final List<SureBookModel> models) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            isNoticeTimeInMainLooper(models);
        } else {
            OAHttpHelper.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    isNoticeTimeInMainLooper(models);
                }
            });
        }
    }

    /**
     * 判断model里面的数据是否符合提醒条件
     *
     * @param models
     */
    public static void isNoticeTimeInMainLooper(List<SureBookModel> models) {
        for (SureBookModel model : models) {

            // 暂时去掉定位
/*            LatLng latLng = new LatLng(Double.valueOf(model.getAb_latitude()), Double.valueOf(model.getAb_longitude()));
            float distance = BaiduMapUtil.getInstence().autoDistance(latLng);
            if (distance == -1f) {
                continue;
            }*/
            if (model.getAb_starttime().compareTo(TimeUtils.f_long_2_str(System.currentTimeMillis())) > 0) {
//                setNoticeTime(model.getAb_starttime(), latLng, distance);    // 暂时去掉定位
                setNotice2Time(model.getAb_starttime());
            }
            LogUtil.prinlnLongMsg("startTime,current_T", model.getAb_starttime() + "," + TimeUtils.f_long_2_str(System.currentTimeMillis()));

        }
    }

    private static void setNotice2Time(String startTime) {
        long startNoticeTime = TimeUtils.f_str_2_long(startTime) - 20 * 60 * 1000;
        if (startNoticeTime <= System.currentTimeMillis()) {
            //TODO 提醒
            Log.i("notificationManage", "开始提醒");
            notificationManage.sendNotification(MyApplication.getInstance(),
                    "您有预约计划即将开始！", BookingListActivity.class);
        } else {
            Log.i("notificationManage", "提醒时间不符合");
        }
    }

    public static List<SureBookModel> getsureBookData() {
        return mSureBookModel;
    }


}
