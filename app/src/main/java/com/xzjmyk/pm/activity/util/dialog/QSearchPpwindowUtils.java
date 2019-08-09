package com.xzjmyk.pm.activity.util.dialog;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.core.app.Constants;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.ui.erp.model.QSCModel;
import com.modular.booking.model.BookingModel;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.app.MyActivityManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by FANGlh on 2017/8/4.
 * function:
 */

public class QSearchPpwindowUtils{
    public QSearchPpwindowUtils() {
    }

    public static void qSearchWindows(final Activity activity){
        final boolean[] isqSearch = {true};
        View contentView = LayoutInflater.from(activity).inflate(R.layout.judge_qsearch_window, null);
        DisplayMetrics dm = MyApplication.getInstance().getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(activity, 250);
        h_screen = DisplayUtil.dip2px(activity, 150);
        final PopupWindow  popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable( MyApplication.getInstance().getResources().getDrawable(R.drawable.pop_round_bg));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(activity,popupWindow,0.4f);
        contentView.findViewById(R.id.next_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();  //下次进入再说
                isqSearch[0] = false;
            }
        });
        contentView.findViewById(R.id.no_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putInt(MainActivity.Q_SEARCH, 1);
                popupWindow.dismiss();  //不再提示
                isqSearch[0] = false;
            }
        });
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isqSearch[0]&&activity!=null&&!activity.isFinishing()){
                    RecognizerDialogUtil.showRecognizerDialog(activity,new RecognizerDialogListener() {
                        @Override
                        public void onResult(RecognizerResult recognizerResult, boolean b) {
                            String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                            if (!StringUtil.isEmpty(text)){
                                handlerQSearch(activity,CommonUtil.getPlaintext(text));  //处理好的语音进行刷选判断逻辑
                            }
                        }
                        @Override
                        public void onError(SpeechError speechError) {
                        }
                    });
//                    handlerQSearch(activity,"预约你");
                    popupWindow.dismiss();
                }
            }
        },6000);
    }

    private static void handlerQSearch(Activity activity, String text) {
        Log.i("handlerQSearch",text+"");
        String url=null;
        int request_code = -1;
        Map<String,Object> params = new HashMap<>();

        if (text.contains("预约")){
            url =   Constants.IM_BASE_URL() + "user/appBookingList";
            params.put("token", MyApplication.getInstance().mAccessToken);
            params.put("userid", MyApplication.getInstance().mLoginUser.getUserId());
            params.put("telephone",MyApplication.getInstance().mLoginUser.getTelephone());
            params.put("yearmonth", DateFormatUtil.getStrDate4Date(new Date(System.currentTimeMillis()), "yyyyMM"));
            request_code = 0x01;
        }else {
            Toast.makeText(MyActivityManager.getCurrentActivity(),"导航"+text+"功能未实现",Toast.LENGTH_LONG).show();
        }
        CommonHttp(activity,url,params,request_code);

    }
    private static void CommonHttp(Activity activity, String comurl, Map<String, Object> params,int request_code) {
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(activity, "sessionId"));
        ViewUtil.httpSendRequest(activity, comurl, params, cHandler, headers, request_code, null, null, "get");
    }


    private static Handler cHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = msg.getData().getString("result");
            switch(msg.what){
                case 0x01:
                    if (!StringUtil.isEmpty(result)){
                        Log.i("0x01result", result);
//                        QSearchDatasUtils.handleBookDatas(result);
                        handleBookDatas(0x01,result);
                    }
                    break;
                case 0x02:
                    break;
                case 0x03:
                    break;
                case 0x04:
                    break;
                case 0x05:
                    break;
                case 0x06:
                    break;
                case 0x07:
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (JSONUtil.validate(result)) {
                        ToastMessage(JSON.parseObject(result).getString("exceptionInfo"));
                    } else {
                        ToastMessage(result);
                    }
                    break;
            }
        }
    };

    private static void handleBookDatas(int handleCode, String result) {
        try {
            final ArrayList<BookingModel> BookingModelList = new ArrayList<>();
            final ArrayList<QSCModel>  qscModelsList = new ArrayList<>();
            if (JSON.parseObject(result).getString("resultMsg") != null) {
                return;
            }
            JSONArray jsonArray = JSON.parseArray(JSON.parseObject(result).getString("bookinglist"));
            if (jsonArray != null) {
                Set<Integer> tags = new HashSet<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    BookingModel model = new BookingModel();
                    QSCModel qscModel = new QSCModel();
                    model.setAb_address(object.getString("ab_address"));
                    model.setAb_bman(object.getString("ab_bman"));
                    model.setAb_bmanid(object.getString("ab_bmanid"));
                    model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                    model.setAb_content(object.getString("ab_content"));
                    model.setAb_endtime(object.getString("ab_endtime"));
                    model.setAb_id(object.getString("ab_id"));
                    model.setAb_latitude(object.getString("ab_latitude"));
                    model.setAb_longitude(object.getString("ab_longitude"));
                    model.setAb_recorddate(object.getString("ab_recorddate"));
                    model.setAb_recordid(object.getString("ab_recordid"));
                    model.setAb_recordman(object.getString("ab_recordman"));
                    model.setAb_sharestatus(object.getString("ab_sharestatus"));
                    model.setAb_starttime(object.getString("ab_starttime"));
                    model.setAb_type(object.getString("ab_type"));
                    tags.add(Integer.valueOf(model.getAb_starttime().substring(8, 10)));

                    qscModel.setKey1(object.getString("ab_recordman"));
                    qscModel.setKey2(object.getString("ab_starttime"));
                    qscModel.setKey3(object.getString("ab_content"));
//                    if (model.getAb_starttime().contains(DateFormatUtil.getStrDate4Date(new Date(System.currentTimeMillis()), "yyyy-MM-dd"))) {
                        BookingModelList.add(model);
                        qscModelsList.add(qscModel);
//                    }

                    if (i == jsonArray.size() - 1){
                        LogUtil.prinlnLongMsg("qscModelsList",JSON.toJSONString(qscModelsList));
                        LogUtil.prinlnLongMsg("BookingModelList",JSON.toJSONString(BookingModelList));
                        QSComShowPpUtils.qSComShowPp(0x01,qscModelsList,BookingModelList.toString());

                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void ToastMessage(String message) {
        ViewUtil.ToastMessage(MyApplication.getInstance(), message);
    }
    public  static void setbg(final Activity activity , PopupWindow popupWindow, float alpha) {
        setBackgroundAlpha(activity, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(activity, 1f);
            }
        });
    }
    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public static void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }
}
