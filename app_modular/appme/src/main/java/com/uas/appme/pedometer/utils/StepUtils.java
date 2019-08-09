package com.uas.appme.pedometer.utils;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.StepRankingFirstDao;
import com.core.model.Friend;
import com.core.model.StepRankingFirstBean;
import com.core.model.XmppMessage;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.pedometer.bean.PersonalStepBean;
import com.uas.appme.pedometer.bean.StepEntity;
import com.uas.appme.pedometer.bean.StepsRankingBean;
import com.uas.appme.pedometer.db.StepDataDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by FANGlh on 2017/9/21.
 * function: UU计步工具类 区分与 Track轨迹公共类
 */

public class StepUtils {


    /**
     * 最终的上传操作
     *
     * @param entity
     */
    public static void doSaveLocalStepsToHttps(StepEntity entity) {
        String steps = entity.getSteps();
        String date = TimeUtils.s_long_2_str(DateFormatUtil.str2Long(entity.getCurDate(), "yyyy年MM月dd日"));
        String em_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
        LogUtil.prinlnLongMsg("steps=,date=", steps + "," + date);
        if (StringUtil.isEmpty(steps) || StringUtil.isEmpty(date)) return;
        Map<String, Object> saveMap = new HashMap<>();
        saveMap.put("as_userid", MyApplication.getInstance().mLoginUser.getUserId());
        saveMap.put("as_username", em_name);
        saveMap.put("as_uusteps", steps);
        saveMap.put("as_date", date);
        String map = JSONUtil.map2JSON(saveMap);
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("appUUsteps")
                .add("map", map)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (o == null) return;
                LogUtil.prinlnLongMsg("appUUsteps", o.toString() + "");
            }
        }));
    }


    /**
     * 判断当前请求获取的步数与本地的步数比较，再选择是进行上传操作还是更新本地数据库操作
     *
     * @param s
     */
    public static void canUnloadToRemote(String s) {
        int remoteSteps = 0;
        PersonalStepBean mPersonalStepBean = new PersonalStepBean();
        try {
            mPersonalStepBean = JSON.parseObject(s.toString(), PersonalStepBean.class);
            String CURRENT_DATE = TimeUtil.getCurrentDate();
            StepDataDao stepDataDao = new StepDataDao(MyApplication.getInstance());
            StepEntity stepEntity = stepDataDao.getCurDataByDate(CURRENT_DATE);

        if (mPersonalStepBean == null || ListUtils.isEmpty(mPersonalStepBean.getWeekSteps())) {
            doSaveLocalStepsToHttps(stepEntity);
            return;
        }
            String yyyy_mm_dd =  TimeUtils.s_long_2_str(DateFormatUtil.str2Long(CURRENT_DATE, "yyyy年MM月dd日"));

            for (int i = 0; i < mPersonalStepBean.getWeekSteps().size(); i++) {
                if (yyyy_mm_dd.equals(mPersonalStepBean.getWeekSteps().get(i).getAs_date())) {  //今日时间yyyy-mm-dd ，在服务器中已保存
                    remoteSteps  = CommonUtil.getNumByString(mPersonalStepBean.getWeekSteps().get(i).getAs_uusteps());
                    break;
                }
            }
            if (stepEntity == null) {
                Intent intent = new Intent("addStepEntity");  //隔夜数据未清空，有点问题，先不用
                intent.putExtra("remoteSteps", remoteSteps);
                BaseConfig.getContext().sendBroadcast(intent);
                return;
            } else if (remoteSteps > CommonUtil.getNumByString(stepEntity.getSteps())) { //  本地步数小于服务端 ，更新本地步数，使其通服务器一样
                Intent intent = new Intent("updateStepEntity");
                intent.putExtra("remoteSteps", remoteSteps);
                BaseConfig.getContext().sendBroadcast(intent);
                LogUtil.prinlnLongMsg("stepDb.update","sendLocalBroadcast");
            } else if (remoteSteps <= CommonUtil.getNumByString(stepEntity.getSteps())) {
                doSaveLocalStepsToHttps(stepEntity);
            }
        } catch (Exception e) {
            LogUtil.prinlnLongMsg("stepDb.update", e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 判断当前手机硬件是否支持计步功能
     *
     * @return
     */

    public static boolean isCanStep() {
        return StepCountCheckUtil.isSupportStepCountSensor(MyApplication.getInstance());
    }


    /**
     * 获取服务器端个人今天目前的所存贮的步数请求
     *
     * @return
     */
    public static void doSaveLocalStepsToJudgeHttps() {
        if (!isCanStep() || !CommonUtil.isNetWorkConnected(MyApplication.getInstance()))
            return;  //不支持计步或者，本地数据与远程数据不符合上传条件则不上传
        if (MyApplication.getInstance() == null
                || MyApplication.getInstance().mLoginUser == null
                || MyApplication.getInstance().mLoginUser.getUserId() == null) return;
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("appSteps")
                .add("userid", MyApplication.getInstance().mLoginUser.getUserId())
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (o == null ||  (!JSONUtil.validate(o.toString()))) return;
                LogUtil.prinlnLongMsg("appSteps", o.toString());
                canUnloadToRemote(o.toString());
            }
        }));
    }


    /**
     * UURANKing列表点赞后的逻辑
     * @param model
     * @param type
     * @param position
     */
    public static void doStepPriseHttp(StepsRankingBean model, final int type, final int position) {
        Boolean doPrise = true;
        String userid = null;
        switch (type){
            case 2:
                userid = model.getAttrank().get(position).getAs_userid();
                if (model.getAttrank().get(position).getPrised()) //已经赞过了
                    doPrise = false;
                break;
            case 3:
                userid = model.getToalrank().get(position).getAs_userid();
                if (model.getToalrank().get(position).getPrised()) //已经赞过了
                    doPrise = false;
                break;
        }

        if (!StringUtil.isEmpty(userid) && MyApplication.getInstance().mLoginUser.getUserId().equals(userid))
            doPrise = false; //不可以赞自己
/*        if (!ListUtils.isEmpty(model.getPricelist())){
            for (int i = 0; i < model.getPricelist().size(); i++) {
                if (model.getPricelist().get(i).getAp_userid().equals(userid)){
                    doPrise = false;
                    break;//个人的点赞列表id中已经存在当前position的userid 则不点赞
                }else if (i==model.getPricelist().size()-1)
                    doPrise = true;
            }
        }else{
            doPrise = true;
        }*/

        Log.i("doPrise",doPrise.toString());
        if (!doPrise) return;

        //点赞操作
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("appStepPrise")
                .add("userid",userid) // 点赞对象ID
                .add("token",MyApplication.getInstance().mAccessToken)
                .add("selfid",MyApplication.getInstance().mLoginUser.getUserId())
                .add("selfname",CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname"))
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appStepPrise", o.toString()+"");
                try {
                    //TODO 请求操作成功后发广播更新排行版界面
                    //发送本地广播
                    Intent intent = new Intent();
                    intent.putExtra("type", type);
                    intent.putExtra("position",position);
                    intent.setAction(AppConstant.UPDATE_STEPRANKING_PRISE);
                    MsgBroadcast.sendLocalBroadcast(intent);
//                    LocalBroadcastManager.getInstance(MyApplication.getInstance().getApplicationContext()).sendBroadcast(new Intent(AppConstant.UPDATE_STEPRANKING_PRISE));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }));
    }



    /**
     * 分享UU运动的发消息方法，只要在调用该方法的上下文中bind mService就可以了
     * @param ownerId
     * @param ownerName
     * @param objectId
     * @param text
     */
    public static void sendMessage( CoreService mService,final String ownerId,String ownerName, final String objectId,  final String text) {

        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent(text);
        message.setFromUserName(ownerName);
        message.setFromUserId(ownerId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        if (interprect(ownerId,objectId,message)) {
            return;
        }
        Log.i("wang", "send message:" + JSON.toJSONString(message));
        // mHasSend = true;
        Log.d("roamer", "开始发送消息,ChatBottomView的回调 sendmessage");
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        ChatMessageDao.getInstance().saveNewSingleChatMessage(ownerId, objectId, message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                Log.d("roamer", "去更新服务器的数据");
                // UploadEngine.uploadImFile(mFriend.getUserId(), message, mUploadResponse);

            } else {
                Log.d("roamer", "sendChatMessage....");
                mService.sendChatMessage(objectId, message);
            }
        } else {
            Log.d("roamer", "sendChatMessage");
            mService.sendChatMessage(objectId, message);

        }
    }
    /**
     * 拦截发送的消息
     *
     * @param message
     */
    private static boolean interprect(String ownerId,String objectId,ChatMessage message) {
        int len = 0;
        List<Friend> mBlackList = FriendDao.getInstance().getAllBlacklists(ownerId);
        if (mBlackList!=null) {
            for (Friend friend : mBlackList) {
                if (friend.getUserId().equals(objectId)) {
                    Toast.makeText(MyApplication.getInstance(), "已经加入黑名单,无法发送消息", Toast.LENGTH_SHORT).show();
                    len++;
                }
            }
        }
        Log.d("wang", "....kkkkk");
        if (len != 0) {
            // finish();
            ListenerManager.getInstance().notifyMessageSendStateChange(ownerId, objectId,
                    message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
            return true;
        }
        return false;
    }


    //将每天的第一名信息保存在本地数据库，供第二天展示
    public static void doBeforeSavaRFIToLocal(final String my_userid,String token){
        //获取所有人的当天步数数据
            HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
            httpClient.Api().send(new HttpClient.Builder()
                    .url("appStepsrank")
                    .add("userid",my_userid)
                    .add("token",token)
                    .connectTimeout(10000)
                    .method(Method.POST)
                    .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
                @Override
                public void onResponse(Object o) {
                    LogUtil.prinlnLongMsg("appStepsrank", o.toString()+","+my_userid);
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("toalrank"))
                        doSavaRFIToLocal(o.toString(),my_userid);
                    else
                        return;
                }

                @Override
                public void onFailure(Object t) {
                    LogUtil.prinlnLongMsg("appStepsrank", JSON.toJSONString(t));
                }
            }));
    }

    private static void doSavaRFIToLocal(String result,String my_userid) {
        try {
            StepsRankingBean bean = JSON.parseObject(result.toString(),StepsRankingBean.class);
            if (bean == null) return;
            if (ListUtils.isEmpty(bean.getToalrank())) return;
            String my_rank,my_steps,f_userid,f_name= null;
            f_name = bean.getToalrank().get(0).getAs_username();
            f_userid = bean.getToalrank().get(0).getAs_userid();

            for (int i = 0; i < bean.getToalrank().size(); i++) {
                if (my_userid.equals(bean.getToalrank().get(i).getAs_userid())){
                    my_rank = bean.getToalrank().get(i).getRank()+"";
                    my_steps = bean.getToalrank().get(i).getAs_uusteps();

                    if (!StringUtil.isEmpty(f_name) && !StringUtil.isEmpty(f_userid) &&
                            !StringUtil.isEmpty(my_rank) && !StringUtil.isEmpty(my_steps)){
                        String CURRENT_DATE = TimeUtil.getCurrentDate();
                        StepRankingFirstDao dao = new StepRankingFirstDao();
                        StepRankingFirstBean fbean = null;

                        fbean = dao.getCurDataByDate(CURRENT_DATE);
                        LogUtil.prinlnLongMsg("fbean1",JSON.toJSONString(fbean));
                        if (fbean == null){  //进行新增操作
//                            fbean = new StepRankingFirstBean(null,CURRENT_DATE,my_userid,my_rank,my_steps,f_userid,f_name);
                            fbean = new StepRankingFirstBean();
                            fbean.setDate(CURRENT_DATE);
                            fbean.setMy_userid(my_userid);
                            fbean.setMy_rank(my_rank);
                            fbean.setMy_steps(my_steps);
                            fbean.setF_userid(f_userid);
                            fbean.setF_name(f_name);

                            dao.addNewRFIData(fbean);
                        }else {  //进行更新操作
//                            fbean = new StepRankingFirstBean(CURRENT_DATE,my_userid,my_rank,my_steps,f_userid,f_name);
                            fbean = new StepRankingFirstBean();
                            fbean.setDate(CURRENT_DATE);
                            fbean.setMy_userid(my_userid);
                            fbean.setMy_rank(my_rank);
                            fbean.setMy_steps(my_steps);
                            fbean.setF_userid(f_userid);
                            fbean.setF_name(f_name);

                            dao.updateCurDateRFI(fbean);
                        }

                        LogUtil.prinlnLongMsg("fbean2",JSON.toJSONString(fbean));
                    }

                    break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
