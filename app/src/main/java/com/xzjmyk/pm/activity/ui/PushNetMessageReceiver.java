package com.xzjmyk.pm.activity.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.baidu.android.pushservice.PushMessageReceiver;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.net.http.ViewUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appworks.activity.ScheduleActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import org.apache.http.Header;

import java.util.List;


/**
 * @desc:推送接口回调
 * @author：Administrator on 2016/4/8 15:29
 */
public class PushNetMessageReceiver extends PushMessageReceiver {

    @Override
    public void onBind(Context context, int errorCode, String appid, String userId, String channelId,
                       String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid=" + appid + " userId=" + userId
                + " channelId=" + channelId + " requestId=" + requestId;
        Log.i("wang", responseString);
        update(channelId);
        RequestParams params = new RequestParams();
        params.put("channelId", channelId);
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("deviceId", 1);
        AsyncHttpClient client = new AsyncHttpClient();
        SharedPreferences configSharePre = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = configSharePre.edit();
        String url = configSharePre.getString("apiUrl", "http://192.168.1.240/api/vg1/");
        Log.i("wang", url + "user/channelId/set");
        client.post(url + "user/channelId/set", params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                Log.d("wang", "上传失败" + arg3.toString());

            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                // TODO Auto-generated method stub
                Log.d("wang", "上传channelId成功了");
            }

        });
    }

    private void update(String channelId) {
        String imid = MyApplication.getInstance().getLoginUserId();
        Log.d("wang", "channelId=" + channelId + "||imid=" + imid);
        HttpClient mHttpClient = new HttpClient.Builder()
                .url("channelset/saveChannelset")
                .add("imid", imid)
                .add("deviceType", 3)
                .add("channelId", channelId)
                .build();
        HttpClient httpClient = new HttpClient.Builder(com.core.utils.CommonUtil.getSchedulerBaseUrl()).isDebug(false)
                .connectTimeout(5000)
                .readTimeout(5000).build();
        httpClient.Api().post(mHttpClient, new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                Log.d("wang", "onResponse上传channelId成功了" + o.toString());
            }

            @Override
            public void onFailure(Object t) {
                Log.d("wang", "onResponse 上传channelId失败了" + t.toString());
            }
        }));
    }

    @Override
    public void onDelTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onListTags(Context arg0, int arg1, List<String> arg2, String arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessage(Context arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * @desc:待审批流程，我的任务，通知公告消息到来置顶
     * @author：Administrator on 2016/4/20 16:46
     */
    @Override
    public void onNotificationArrived(Context ct, String title, String content, String params) {
        if (JSONUtil.validate(params)) {
            String platform = JSON.parseObject(params).getString("platform");
            String msgContent = JSON.parseObject(params).getString("content");
            String master = JSON.parseObject(params).getString("master");
            String pageTitle = JSON.parseObject(params).getString("pageTitle");
            Log.i("Arison", "" + pageTitle);
            if (!StringUtil.isEmpty(pageTitle)) {
                if (pageTitle.contains("商务消息")) {
                    CommonUtil.pushProcessB2bMsg(ct, msgContent, master);
                }
            }

            if (StringUtil.isEmpty(platform)) {
                CommonUtil.pushProcessMsg(ct, MyApplication.getInstance().mLoginUser.getUserId());
            } else {
                MsgBroadcast.broadcastMsgUiUpdate(ct);
            }

        }
    }


    /**
     * @desc:点击操作 ERP系统或者B2B系统
     * @author：Administrator on 2016/4/20 16:42
     */
    @Override
    public void onNotificationClicked(Context ct, String title, String content, String params) {
        Log.i("wang", "title=" + title);
        Log.i("wang", "content=" + content);
        Log.i("wang", "params=" + params);
        if (!TextUtils.isEmpty(title) && title.equals("UU日程")) {
            ct.startActivity(new Intent(ct, ScheduleActivity.class));
        } else if (JSONUtil.validate(params)) {
            String titlePage = JSON.parseObject(params).getString("title");
            String url = JSON.parseObject(params).getString("url");
            String master = JSON.parseObject(params).getString("master");
            String masterId = JSON.parseObject(params).getString("masterId");
            String uu = JSON.parseObject(params).getString("uu");
            String platform = JSON.parseObject(params).getString("platform");

            if (!StringUtil.isEmpty(platform)) {
                if ("B2B".equals(platform)) {
                    Log.i("wang", "params=" + platform);
                    Log.i("wang", "url=" + url);
                    Log.i("wang", "titlePage=" + titlePage);
                    CommonUtil.loadWebViewToB2B(ct, url, titlePage);
                }
                if ("ERP".equals(platform)) {
                    CommonUtil.loadWebView(ct, url, titlePage, master, masterId, uu);
                }
            } else {
//				ViewUtil.ToastMessage(ct, "缺少参数：platform");
            }
        } else {
            ViewUtil.ToastMessage(ct, "服务器未指定需要的参数");
        }
    }

    @Override
    public void onSetTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUnbind(Context arg0, int arg1, String arg2) {

        Log.i("wang", arg1 + "++++++" + arg2);
    }


}
