package com.uas.appme.pedometer.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.MyListView;
import com.core.xmpp.CoreService;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.ui.activity.ContactsActivity;
import com.uas.appme.R;
import com.uas.appme.pedometer.adapter.UUAttentionAdapter;
import com.uas.appme.pedometer.adapter.UURankingAdapter;
import com.uas.appme.pedometer.bean.StepEntity;
import com.uas.appme.pedometer.bean.StepsRankingBean;
import com.uas.appme.pedometer.db.StepDataDao;
import com.uas.appme.pedometer.utils.StepUtils;
import com.uas.appme.pedometer.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by FANGlh on 2017/9/19.
 * function:
 */

public class UURanking extends SupportToolBarActivity implements View.OnClickListener {
    private CircleImageView mPhotoImg;
    private TextView mNameTv;
    private TextView mRankingTv;
    private TextView mStepsTv;
    private TextView mPriseTv;
    private ImageView mPriseIm;
    private LinearLayout mAttentionLl;
    private MyListView mAttentionPtlv;
    private MyListView mAllPtlv;
    private String curSelDate;
    private String myem_name;
    private List<StepEntity> stepEntityList;
    private UURankingAdapter mRankAdapter;
    private UUAttentionAdapter mAttenAdapter;
    private ImageView mHeaderImg;
    private StepsRankingBean mStepsRankingBean;
    private int att_position = -1;
    private int all_position = -1;
    private int my_rank = -1;
    private CircleImageView first_ranking_im;
    private TextView first_ranking_tv;
//    private String first_ranking_imid = null;  //第一名imid
//    private String first_ranking_name = null; //第一名名字
    private String shareStepStr = "我正在使用UU运动计步功能，你也一起来吧";
    private CoreService mService;
    private Boolean canShowAtt = false; // Attend，All数据源不断改变，为了不重复刷新适配器，当符合canShowAtt == true && canShowAll == true 再一起刷新适配器
    private Boolean canShowAll = false;
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            initData();
            int brType = intent.getIntExtra("type",-1);
            int brposition = intent.getIntExtra("position",-1);


            LogUtil.prinlnLongMsg("brType,brposition",brType+","+brposition+"");
            try {
                if (brType != -1 && brposition != -1){
                    if (brType == 2){  // 点赞的是AttRank
                        mStepsRankingBean.getAttrank().get(brposition).setPrised(true);
                        mStepsRankingBean.getAttrank().get(brposition).setAs_prise(
                                String.valueOf(CommonUtil.getNumByString(mStepsRankingBean.getAttrank().get(brposition).getAs_prise())+1));
                        mAttenAdapter.notifyDataSetChanged();
                        if (ListUtils.isEmpty(mStepsRankingBean.getToalrank())) return;
                        for (int i = 0; i < mStepsRankingBean.getToalrank().size(); i++) { //去更新AllRank
                            if (mStepsRankingBean.getAttrank().get(brposition).getAs_userid().equals(mStepsRankingBean.getToalrank().get(i).getAs_userid())){
                                mStepsRankingBean.getToalrank().get(i).setAs_prise(
                                        String.valueOf(CommonUtil.getNumByString(mStepsRankingBean.getToalrank().get(i).getAs_prise())+1));
                                mStepsRankingBean.getToalrank().get(i).setPrised(true);
                                mRankAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }else if (brType == 3){  // 点赞的是AllRank人地方
                        mStepsRankingBean.getToalrank().get(brposition).setPrised(true);
                        mStepsRankingBean.getToalrank().get(brposition).setAs_prise(
                                String.valueOf(CommonUtil.getNumByString(mStepsRankingBean.getToalrank().get(brposition).getAs_prise())+1));
                        mRankAdapter.notifyDataSetChanged();
                        if (ListUtils.isEmpty(mStepsRankingBean.getAttrank())) return;
                        for (int i = 0; i < mStepsRankingBean.getAttrank().size(); i++) { //去更新AttRank
                            if (mStepsRankingBean.getToalrank().get(brposition).getAs_userid().equals(mStepsRankingBean.getAttrank().get(i).getAs_userid())){
                                mStepsRankingBean.getAttrank().get(i).setAs_prise(
                                        String.valueOf(CommonUtil.getNumByString(mStepsRankingBean.getAttrank().get(i).getAs_prise())+1));
                                mStepsRankingBean.getAttrank().get(i).setPrised(true);
                                mAttenAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uu_ranking_activity);
        StepUtils.doSaveLocalStepsToJudgeHttps();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter(AppConstant.UPDATE_STEPRANKING_PRISE));
        bindService(CoreService.getIntent(), mConnection, BIND_AUTO_CREATE);
        initView();
        initData();
        initEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
    private void initView() {
        mHeaderImg = (ImageView) findViewById(R.id.header_background_im);
        mPhotoImg = (CircleImageView) findViewById(R.id.photo_img);
        mNameTv = (TextView) findViewById(R.id.name_tv);
        mRankingTv = (TextView) findViewById(R.id.ranking_tv);
        mStepsTv = (TextView) findViewById(R.id.steps_tv);
        mPriseTv = (TextView) findViewById(R.id.prise_tv);
        mPriseIm = (ImageView) findViewById(R.id.prise_im);
        mAttentionLl = (LinearLayout) findViewById(R.id.attention_ll);
        mAttentionPtlv = (MyListView) findViewById(R.id.attention_ptlv);
        mAllPtlv = (MyListView) findViewById(R.id.all_ptlv);
        findViewById(R.id.invite_friends_tv).setOnClickListener(this);
        curSelDate = TimeUtil.getCurrentDate();
        stepEntityList = new ArrayList<>();
        mRankAdapter = new UURankingAdapter(this);
        mAttenAdapter = new UUAttentionAdapter(this);
        findViewById(R.id.my_info_rl).setOnClickListener(this);
        first_ranking_im = (CircleImageView)findViewById(R.id.first_ranking_im);
        first_ranking_tv = (TextView) findViewById(R.id.first_ranking_tv);

        mHeaderImg.requestFocus();
        mStepsRankingBean = new StepsRankingBean();

        myem_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
        mNameTv.setText(myem_name+"");
        String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        AvatarHelper.getInstance().display(loginUserId, mPhotoImg, true, false);

        //初始化个人信息，先从本地数据库取
        StepDataDao stepDataDao = new StepDataDao(this);
        StepEntity stepEntity = stepDataDao.getCurDataByDate(curSelDate);
        if (stepEntity != null) {
            int steps = Integer.parseInt(stepEntity.getSteps());
            //获取全局的步数
            mStepsTv.setText(String.valueOf(steps));
        } else {
            //获取全局的步数
            mStepsTv.setText("0");
        }

    }
    private int Timeout = 0;
    private int testRequest = 0;
    private void initData() {
        if (!CommonUtil.isNetWorkConnected(this)) {
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        progressDialog.show();
        //获取所有人的当天步数数据
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("appStepsrank")
                .add("userid",MyApplication.getInstance().mLoginUser.getUserId())
                .add("token",MyApplication.getInstance().mAccessToken)
                .connectTimeout(10000)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("c", Timeout+"-"+o.toString()+"");
                LogUtil.prinlnLongMsg("appStepsranktestRequest", testRequest+"-"+o.toString()+"");
                if ((o.toString().contains("resultCode") && o.toString().contains("resultMsg"))
                        || (o.toString().contains("data") &&
                        "java.lang.NullPointerException".equals(JSON.parseObject(o.toString()).getString("data"))) ){
                    testRequest ++;
                    if (testRequest >= 10) {
                        progressDialog.dismiss();
                        ToastMessage("数据获取异常，请稍后再试");
                    }else {   // 数据返回异常再请求 ，最多10次
                        initData();
                    }
                }else if (o.toString().contains("toalrank"))
                    handleData(o.toString());
                else{
                    progressDialog.dismiss();
                    ToastMessage("数据获取异常，请稍后再试");
                }
            }

            @Override
            public void onFailure(Object t) {
                Timeout++; // 请求超时后再请求再次请求直到拿到数据，最多3次请求
                LogUtil.prinlnLongMsg("appStepsrankflh", Timeout+"");
                if (Timeout == 3) {
                    progressDialog.dismiss();
                    ToastMessage(getString(R.string.too_long_to_http));
                }
            }
        }));


    }

    private void handleData(String s) {
        try {
            mStepsRankingBean = JSON.parseObject(s.toString(),StepsRankingBean.class);
            if (mStepsRankingBean == null)  {
                progressDialog.dismiss();
                Log.i("mStepsRankingBean","mStepsRankingBean==null");
                ToastMessage("数据获取异常，请稍后再试");
                return;
            }
            //getPricelist（）有数据不为空，自己点赞过别人
            if (!ListUtils.isEmpty(mStepsRankingBean.getPricelist())){

                //循环从点赞表与关注表对比，getAttrank（）的_userid在getPricelist（）中存在，则设置setPrised(true)，显示红心
                if (!ListUtils.isEmpty(mStepsRankingBean.getAttrank())){
                    for (int i = 0; i < mStepsRankingBean.getAttrank().size(); i++) {
                        for (int j = 0; j<mStepsRankingBean.getPricelist().size();j++) {
                            if (mStepsRankingBean.getAttrank().get(i).getAs_userid().equals(mStepsRankingBean.getPricelist().get(j).getAp_userid())) {
                                mStepsRankingBean.getAttrank().get(i).setPrised(true);
                                break;
                            } else
                                mStepsRankingBean.getAttrank().get(i).setPrised(false);
                        }
                        //关心的人中为自己时，看自己的被点赞次数>0 设置setPrised(true)
                        if (MyApplication.getInstance().mLoginUser.getUserId().equals(mStepsRankingBean.getAttrank().get(i).getAs_userid())
                                && CommonUtil.getNumByString(mStepsRankingBean.getAttrank().get(i).getAs_prise())>0)
                            mStepsRankingBean.getAttrank().get(i).setPrised(true);

                        if (i==mStepsRankingBean.getAttrank().size()-1){
                            handleAttendRank(mStepsRankingBean);
                        }
                    }
                }else {
                    canShowAtt=true;
                    showAttAndAllRank();
                }

                //循环从点赞表与关注表对比，getToalrank（）的_userid在getPricelist（）中存在，则设置setPrised(true)，显示红心
                if (!ListUtils.isEmpty(mStepsRankingBean.getToalrank())){
                    for (int i = 0; i < mStepsRankingBean.getToalrank().size(); i++) {
                        for (int j = 0; j < mStepsRankingBean.getPricelist().size();j++) {
                            if (mStepsRankingBean.getToalrank().get(i).getAs_userid().equals(mStepsRankingBean.getPricelist().get(j).getAp_userid())) {
                                mStepsRankingBean.getToalrank().get(i).setPrised(true);
                                break;
                            } else
                                mStepsRankingBean.getToalrank().get(i).setPrised(false);

                        }
                        //所有人中为自己时，看自己的被点赞次数>0 设置setPrised(true)
                        if (MyApplication.getInstance().mLoginUser.getUserId().equals(mStepsRankingBean.getToalrank().get(i).getAs_userid()) &&
                                CommonUtil.getNumByString(mStepsRankingBean.getToalrank().get(i).getAs_prise())>0)
                                mStepsRankingBean.getToalrank().get(i).setPrised(true);

                        if (i==mStepsRankingBean.getToalrank().size()-1){
                            canShowAll=true;
                            showAttAndAllRank();
                        }

                    }
                }else
                    progressDialog.dismiss();

            }else {  //getPricelist（）数据为空，自己没点赞过别人
                if (!ListUtils.isEmpty(mStepsRankingBean.getToalrank())){
                    for (int i = 0; i < mStepsRankingBean.getToalrank().size(); i++) {
                        mStepsRankingBean.getToalrank().get(i).setPrised(false);
                        if (i==mStepsRankingBean.getToalrank().size()-1){
                            canShowAll=true;
                            showAttAndAllRank();
                        }
                    }
                }else
                    progressDialog.dismiss();

                if (!ListUtils.isEmpty(mStepsRankingBean.getAttrank())){
                    for (int i = 0; i < mStepsRankingBean.getAttrank().size(); i++) {
                        mStepsRankingBean.getAttrank().get(i).setPrised(false);
                        if (i==mStepsRankingBean.getAttrank().size()-1){
                            handleAttendRank(mStepsRankingBean);
                        }
                    }
                }else {
                    canShowAtt=true;
                    showAttAndAllRank();
                }

            }
            if (mStepsRankingBean.getAttrank() != null && mStepsRankingBean.getAttrank().size()>0)
                mAttentionLl.setVisibility(View.VISIBLE);
            else
                mAttentionLl.setVisibility(View.GONE);
        }catch (Exception e){
            progressDialog.dismiss();
            ToastMessage("数据解析异常，请稍后再试");
            e.printStackTrace();
            Log.i("Exceptionflh",testRequest+"数据解析异常，请稍后再试");
            LogUtil.prinlnLongMsg("Exceptionflh",testRequest+"-"+e.toString());
        }
    }


    //关注的人在排行榜的排名获取
    private void handleAttendRank(StepsRankingBean mStepsRankingBean) {
        if (!ListUtils.isEmpty(mStepsRankingBean.getAttrank()) && !ListUtils.isEmpty(mStepsRankingBean.getToalrank())){
            for (int i = 0; i < mStepsRankingBean.getAttrank().size(); i++) {
                for (int j=0;j<mStepsRankingBean.getToalrank().size();j++){
                    if (!StringUtil.isEmpty(mStepsRankingBean.getAttrank().get(i).getAs_userid()) &&
                            !StringUtil.isEmpty(mStepsRankingBean.getToalrank().get(j).getAs_userid()) &&
                            mStepsRankingBean.getAttrank().get(i).getAs_userid().equals(mStepsRankingBean.getToalrank().get(j).getAs_userid()) &&
                            !StringUtil.isEmpty(mStepsRankingBean.getToalrank().get(j).getRank()+"")){
                        mStepsRankingBean.getAttrank().get(i).setRank(mStepsRankingBean.getToalrank().get(j).getRank());
                        break;
                    }
                }
                if (i==mStepsRankingBean.getAttrank().size()-1){
                    canShowAtt = true;
                    showAttAndAllRank();
                }
            }
        }
    }

    private void showAttAndAllRank() {
        if (canShowAtt && canShowAll){
            showMyInfo();

            mRankAdapter.setModel(mStepsRankingBean);
            mAllPtlv.setAdapter(mRankAdapter);
            mRankAdapter.notifyDataSetChanged();

            mAttenAdapter.setModel(mStepsRankingBean);
            mAttentionPtlv.setAdapter(mAttenAdapter);
            mAttenAdapter.notifyDataSetChanged();

            LogUtil.prinlnLongMsg("mStepsRankingBeanLast", JSON.toJSONString(mStepsRankingBean)+"");
        }
        Log.i("showAttAndAllRank",testRequest+"-"+canShowAtt+","+canShowAll);
//        if (MyApplication.getInstance().mLoginUser.getUserId().equals("108340"))
//            ToastMessage("canShowAtt,canShowAll="+canShowAtt+","+canShowAll);
//        testRequest++;
//        if (testRequest<999999)
//            initData();
    }

    private void showMyInfo() {
        if (mStepsRankingBean == null || ListUtils.isEmpty(mStepsRankingBean.getToalrank())) return;
        //显示当前登录人的步数排名
        for (int i = 0; i < mStepsRankingBean.getToalrank().size(); i++) {
            if (MyApplication.getInstance().mLoginUser.getUserId().equals(mStepsRankingBean.getToalrank().get(i).getAs_userid())){
                mRankingTv.setText("第" + mStepsRankingBean.getToalrank().get(i).getRank()+"名");
                my_rank =  mStepsRankingBean.getToalrank().get(i).getRank();  //显示个人排名
                mStepsTv.setText(mStepsRankingBean.getToalrank().get(i).getAs_uusteps());
                mPriseTv.setText(mStepsRankingBean.getToalrank().get(i).getAs_prise());
                if (Integer.valueOf(mStepsRankingBean.getToalrank().get(i).getAs_prise()) > 0)
                    mPriseIm.setImageResource(R.drawable.praised);
                break;
            }
        }

        //显示第一名信息
        String first_ranking_imid = mStepsRankingBean.getToalrank().get(0).getAs_userid();
        String first_ranking_name = mStepsRankingBean.getToalrank().get(0).getAs_username();
        if (!StringUtil.isEmpty(first_ranking_imid) && !StringUtil.isEmpty(first_ranking_name)){
            AvatarHelper.getInstance().display(CommonUtil.getNumByString(first_ranking_imid) + "", first_ranking_im, true, false);//显示圆角图片
            first_ranking_tv.setText(first_ranking_name+"\t" + getString(R.string.who_is_champion));
        }
        doPositionFocus();
    }


    private void initEvents() {
        mAttentionPtlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inTentToPersonal(2,position);
            }
        });
        mAllPtlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inTentToPersonal(3,position);
            }
        });
    }



    private void inTentToPersonal(int type, int position) {
        final Intent intent = new Intent(this,PersonalPageActivity.class);
        String title = "";
        String userid = "";
        String em_name = "";

        switch (type){
            case 1:
                title = "我";
                userid = MyApplication.getInstance().mLoginUser.getUserId();
                em_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
                all_position=-1;
                att_position=-1;
                break;
            case 2:
                title = mStepsRankingBean.getAttrank().get(position).getAs_username();
                userid = mStepsRankingBean.getAttrank().get(position).getAs_userid();
                em_name = mStepsRankingBean.getAttrank().get(position).getAs_username();
                all_position=-1;att_position=position;

                if (MyApplication.getInstance().mLoginUser.getUserId().equals(userid)) {
                    type = 1;
                    title = "我";
                }
                break;
            case 3:
                title = mStepsRankingBean.getToalrank().get(position).getAs_username();
                userid = mStepsRankingBean.getToalrank().get(position).getAs_userid();
                em_name = mStepsRankingBean.getToalrank().get(position).getAs_username();
                all_position=position;att_position=-1;

                if (MyApplication.getInstance().mLoginUser.getUserId().equals(userid)) {
                    type = 1;
                    title = "我";
                }else {
                    if (mStepsRankingBean.getAttrank() == null) return;
                    for (int i = 0; i < mStepsRankingBean.getAttrank().size(); i++) {
                        if (mStepsRankingBean.getToalrank().get(position).getAs_userid()
                                .equals(mStepsRankingBean.getAttrank().get(i).getAs_userid())){  //当点击的所有人某一人在关注人的遍历中存在时
                            type = 2;
                            break;
                        }else if (i == mStepsRankingBean.getAttrank().size()-1){
                            type = 3;
                        }

                    }
                }

                break;
        }

        if (type == -1 || StringUtil.isEmpty(title) || StringUtil.isEmpty(userid) || StringUtil.isEmpty(em_name))  return;

        intent.putExtra("type", type);
        intent.putExtra("my_rank",my_rank);
        intent.putExtra("title", title +"的主页");
        intent.putExtra("userid", userid);
        intent.putExtra("em_name", em_name);
        startActivityForResult(intent,0x01);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_info_rl){
            inTentToPersonal(1,0);
        }else if (v.getId() == R.id.invite_friends_tv){
            Intent intent = new Intent(this, ContactsActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("title", "分享好友");
            startActivityForResult(intent, 0x03);
            Toast.makeText(ct,"只能分享给UU好友",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x01 && resultCode == 0x02){
            initData();
        }else if (requestCode == 0x03 ){
            if (data == null){
                ToastMessage("只能分享给UU好友");
                return;
            }
            ContactsModel model = data.getParcelableExtra("data");
            String ownerId = MyApplication.getInstance().mLoginUser.getUserId();
            String ownerName = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
            String objectId = model.getImid();
            StepUtils.sendMessage(mService,ownerId,ownerName,objectId,shareStepStr);
            LogUtil.d("0x01",JSON.toJSONString(model));
        }
    }

    //对于处理点击关注或者取消关注后返回的焦点聚焦，回到之前位置的问题，不然会出现被挤出去情况
    private void doPositionFocus() {
        if (all_position != -1 && all_position<mStepsRankingBean.getToalrank().size()){
            mAllPtlv.setSelection(all_position);
        }else if (all_position != -1 && all_position >= mStepsRankingBean.getToalrank().size()){
            mAllPtlv.setSelection(mStepsRankingBean.getToalrank().size()-1);
        }else if (att_position != -1 && att_position<mStepsRankingBean.getAttrank().size()){
            mAttentionPtlv.setSelection(att_position);
        }else if (att_position != -1 && att_position >= mStepsRankingBean.getAttrank().size()) {
            mAttentionPtlv.setSelection(mStepsRankingBean.getAttrank().size() - 1);
        }
        progressDialog.dismiss();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((CoreService.CoreServiceBinder) service).getService();

        }
    };
}
