package com.uas.appme.pedometer.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.xmpp.CoreService;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.ui.activity.ContactsActivity;
import com.uas.appme.R;
import com.uas.appme.pedometer.bean.ComPolylineBean;
import com.uas.appme.pedometer.bean.PersonalStepBean;
import com.uas.appme.pedometer.utils.PolylineUtils;
import com.uas.appme.pedometer.utils.StepUtils;
import com.uas.appme.pedometer.utils.TimeUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by FANGlh on 2017/9/21.
 * function:
 */

public class PersonalPageActivity extends BaseActivity implements View.OnClickListener{

    private String title;
    private String userid;
    private String em_name;
    private int type;
    private TextView mMovementTotalKmTv;
    private TextView mMovementTotalKmTimeTv;
    private TextView mMovementTotalStepsTv;
    private TextView mMovementTotalStepsTimeTv;
    private PersonalStepBean mPersonalStepBean;
    private Button mbtnAttention;
    private LineChartView lineChart;
    private String shareStepStr = "我正在使用UU运动计步功能，你也一起来吧";
    private ImageView share_bitmap_im;
    private LinearLayout line_chart_ll;
    private int my_rank;
    private CoreService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_page_activity);
        bindService(CoreService.getIntent(), mConnection, BIND_AUTO_CREATE);
        initView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
    private void initView() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        userid = intent.getStringExtra("userid");
        em_name = intent.getStringExtra("em_name");
        type = intent.getIntExtra("type", -1);
        if (type == 1)
            my_rank = intent.getIntExtra("my_rank",-1);
      setTitle(title);

        LogUtil.i("userid",userid);
        mMovementTotalKmTv = (TextView) findViewById(R.id.movement_total_km_tv);
        mMovementTotalKmTimeTv = (TextView) findViewById(R.id.movement_total_km_time_tv);
        mMovementTotalStepsTv = (TextView) findViewById(R.id.movement_total_steps_tv);
        mMovementTotalStepsTimeTv = (TextView) findViewById(R.id.movement_total_steps_time_tv);
        mbtnAttention = (Button) findViewById(R.id.btn_attention);
        share_bitmap_im = (ImageView) findViewById(R.id.share_bitmap_im);
        mbtnAttention.setOnClickListener(this);
        mPersonalStepBean = new PersonalStepBean();
//        LineChartView line_chart = findViewById(R.id.line_chart);
        if (type == 3)  // type 1、2:我、已关注的人
            mbtnAttention.setVisibility(View.VISIBLE);
        else
            mbtnAttention.setVisibility(View.GONE);
        lineChart = (LineChartView)findViewById(R.id.line_chart);
        line_chart_ll = (LinearLayout) findViewById(R.id.line_chart_ll);

        initData();
    }

    private int appStepsPost = 0;
    private void initData() {
        if (!CommonUtil.isNetWorkConnected(this)) {
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        if (StringUtil.isEmpty(userid)) return;
        progressDialog.show();
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.getHeaders().remove("Content-Type");
        httpClient.Api().send(new HttpClient.Builder()
                .url("appSteps")
                .add("userid", userid)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appSteps", o.toString());
                if (o.toString().contains("com.mysql.jdbc.exceptions.jdbc4") || (o.toString().contains("resultCode") && o.toString().contains("resultMsg"))){
                    appStepsPost++;

                    if (appStepsPost < 10)
                        initData();
                    else
                        ToastMessage("网络慢，请稍后再试");
                    return;
                }else {
                    try {
                        mPersonalStepBean = JSON.parseObject(o.toString(),PersonalStepBean.class);
                        showStepsInfo();
                        initPolylineData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }


    //初始化折线数据
    private void initPolylineData() {
        if (mPersonalStepBean == null) return;
        if (ListUtils.isEmpty(mPersonalStepBean.getMonthSteps()))  return;
        int startI = 0;
        List<ComPolylineBean> polyList = new ArrayList<>();
        if (mPersonalStepBean.getMonthSteps().size() <= 7)  //只展示近7天数据
            startI = 0;
        else
            startI = mPersonalStepBean.getMonthSteps().size() -7;


        for (int i=startI;i<mPersonalStepBean.getMonthSteps().size();i++){
            if (!StringUtil.isEmpty(mPersonalStepBean.getMonthSteps().get(i).getAs_date())
                    && !StringUtil.isEmpty(mPersonalStepBean.getMonthSteps().get(i).getAs_uusteps())){

                long l = DateFormatUtil.str2Long(mPersonalStepBean.getMonthSteps().get(i).getAs_date(),"yyyy-MM-dd");
                String date = DateFormatUtil.getStrDate4Date(new Date(l), "MM-dd");
                ComPolylineBean cBean = new ComPolylineBean(date,CommonUtil.getNumByString(mPersonalStepBean.getMonthSteps().get(i).getAs_uusteps()));
                polyList.add(cBean);
                if (i == mPersonalStepBean.getMonthSteps().size()-1){
                    PolylineUtils.initLineChart(lineChart,polyList);
                    progressDialog.dismiss();
                }
            }
        }
    }

    private void showStepsInfo() {
        if (mPersonalStepBean == null || ListUtils.isEmpty(mPersonalStepBean.getWeekSteps()) || ListUtils.isEmpty( mPersonalStepBean.getMonthSteps()))  return;
        int weekSteps_size = mPersonalStepBean.getWeekSteps().size();
        int monthSteps_size = mPersonalStepBean.getMonthSteps().size();
        String curDate = TimeUtils.s_long_2_str(DateFormatUtil.str2Long(TimeUtil.getCurrentDate(), "yyyy年MM月dd日"));
        String curSteps = "";
        Log.i("curDate",curDate);
        for (int i = 0; i < monthSteps_size; i++) {
            if (mPersonalStepBean.getMonthSteps().get(i).getAs_date().equals(curDate)){
                curSteps = mPersonalStepBean.getMonthSteps().get(i).getAs_uusteps();
                mMovementTotalStepsTv.setText(curSteps);
                mMovementTotalKmTv.setText(countTotalKM(CommonUtil.getNumByString(curSteps)));
                break;
            }
        }
        String now_time = TimeUtil.getWeekStr(TimeUtil.getCurrentDate());
        mMovementTotalKmTimeTv.setText(now_time);
        mMovementTotalStepsTimeTv.setText(now_time);

        //获取点赞信息
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("appPriseList")
                .add("userid",userid)
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appPriseList", o.toString()+"");
                try {

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }));
    }

    private void doAttendHandle(int attention_type) {
        HttpClient httpClient = new HttpClient.Builder(Constants.BASE_STEP_URL).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("appUUSet")
                .add("userids", userid)
                .add("id",MyApplication.getInstance().mLoginUser.getUserId())
                .add("type", attention_type)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (o == null) return;
                LogUtil.prinlnLongMsg("appUUSet", o.toString());
                setResult(0x02);
                finish();
            }
        }));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (type != 3)
            getMenuInflater().inflate(R.menu.menu_step, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.title) {
            showPopupWindow();
        }
        return super.onOptionsItemSelected(item);
    }
    private PopupWindow setWindow = null;//
    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.step_person_page_setting, null);
        if (type == 1){  //自己
            viewContext.findViewById(R.id.share_friend_tv).setVisibility(View.VISIBLE);
//            viewContext.findViewById(R.id.share_monments_tv).setVisibility(View.VISIBLE);
        }else if (type == 2){ //关注的人
            viewContext.findViewById(R.id.cancel_attention_tv).setVisibility(View.VISIBLE);
        }else if (type == 3){  //普通好友
            viewContext.findViewById(R.id.not_rankingwith_tv).setVisibility(View.VISIBLE);
        }

        viewContext.findViewById(R.id.share_friend_tv).setOnClickListener(this);
        viewContext.findViewById(R.id.share_monments_tv).setOnClickListener(this);
        viewContext.findViewById(R.id.cancel_attention_tv).setOnClickListener(this);
        viewContext.findViewById(R.id.not_rankingwith_tv).setOnClickListener(this);

        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }
    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }
    /**
     * 简易计算公里数，假设一步大约有0.7米
     *
     * @param steps 用户当前步数
     * @return
     */
    private String countTotalKM(int steps) {
        DecimalFormat df = new DecimalFormat("#.##");
        double totalMeters = steps * 0.7;
        //保留两位有效数字
        return df.format(totalMeters / 1000);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_attention){
            doAttendHandle(1);
        }else if (v.getId() == R.id.cancel_attention_tv){
            doAttendHandle(0);
        }else if (v.getId() == R.id.share_friend_tv){
            Intent intent = new Intent(this, ContactsActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("title", "分享好友");
            startActivityForResult(intent, 0x01);
            Toast.makeText(ct,"只能分享给UU好友",Toast.LENGTH_LONG).show();
//            ToastMessage("share_friend_tv");
            closePopupWindow();
        }else if (v.getId() == R.id.share_monments_tv){
            Intent intent = new Intent(this,ShareStepsActivity.class);
            intent.putExtra("my_rank",my_rank);
            intent.putExtra("my_steps",mMovementTotalStepsTv.getText().toString());
            intent.putExtra("im_ids",userid);
            startActivity(intent);
            closePopupWindow();
        }else if (v.getId() == R.id.not_rankingwith_tv){
//            ToastMessage("not_rankingwith_tv");
            closePopupWindow();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (requestCode == 0x01){
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
