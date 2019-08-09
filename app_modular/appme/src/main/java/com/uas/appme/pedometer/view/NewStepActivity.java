package com.uas.appme.pedometer.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.core.app.Constants;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.uas.appme.R;
import com.uas.appme.pedometer.bean.StepEntity;
import com.uas.appme.pedometer.calendar.BeforeOrAfterCalendarView;
import com.uas.appme.pedometer.db.StepDataDao;
import com.uas.appme.pedometer.service.StepService;
import com.uas.appme.pedometer.utils.StepCountCheckUtil;
import com.uas.appme.pedometer.utils.StepUtils;
import com.uas.appme.pedometer.utils.TimeUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by FANGlh on 2017/4/13.
 * function:
 */
public class NewStepActivity extends SupportToolBarActivity implements Handler.Callback {

    /**
     * 屏幕长度和宽度
     */
    public static int screenWidth, screenHeight;
    private BeforeOrAfterCalendarView calenderView;
    private String curSelDate;
    private DecimalFormat df = new DecimalFormat("#.##");
    private List<StepEntity> stepEntityList = new ArrayList<>();
    private StepDataDao stepDataDao;
    private Boolean support_step;
    private LinearLayout movementCalenderLl;
    private TextView kmTimeTv;
    private TextView totalKmTv;
    private TextView stepsTimeTv;
    private TextView totalStepsTv;
    private TextView supportTv;
    private LinearLayout steps_history_ll;
    private TextView steps_history_tv;
    private TextView steps_track_tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newstep);
        initView();
        initData();
    }

    private void initView() {
        movementCalenderLl = (LinearLayout) findViewById(R.id.movement_records_calender_ll);
        kmTimeTv = (TextView) findViewById(R.id.movement_total_km_time_tv);
        totalKmTv = (TextView) findViewById(R.id.movement_total_km_tv);
        stepsTimeTv = (TextView) findViewById(R.id.movement_total_steps_time_tv);
        totalStepsTv = (TextView) findViewById(R.id.movement_total_steps_tv);
        supportTv = (TextView) findViewById(R.id.is_support_tv);
        steps_history_ll  = (LinearLayout) findViewById(R.id.steps_history_ll);
        steps_history_tv = (TextView) findViewById(R.id.steps_history_tv);
        steps_track_tv = (TextView) findViewById(R.id.steps_track_tv);

        steps_history_tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线效果
        curSelDate = TimeUtil.getCurrentDate();
//        curSelDate = DateFormatUtil.long2Str(DateFormatUtil.YMD);
        support_step = StepCountCheckUtil.isSupportStepCountSensor(this);
//                ||StepCountCheckUtil.isSupportStepCountSensor();
        Log.i("support_step",support_step+"");

    }

    private void initData() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        //放到获取宽度之后
        calenderView = new BeforeOrAfterCalendarView(this);
        movementCalenderLl.addView(calenderView);
        /**
         * 这里判断当前设备是否支持计步
         */
        if (support_step) {
            getRecordList();
            supportTv.setVisibility(View.GONE);
            steps_track_tv.setVisibility(View.GONE);
            setDatas();
            setupService();
            initListener();
        } else {
            totalStepsTv.setText("0");
            supportTv.setVisibility(View.VISIBLE);
            steps_track_tv.setVisibility(View.GONE);
            steps_history_ll.setVisibility(View.GONE);
        }
    }


    private void initListener() {
        calenderView.setOnBoaCalenderClickListener(new BeforeOrAfterCalendarView.BoaCalenderClickListener() {
            @Override
            public void onClickToRefresh(int position, String curDate) {
                //获取当前选中的时间
                curSelDate = curDate;
                //根据日期去取数据
                setDatas();
            }
        });

        steps_history_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),NewStepListActivity.class));
            }
        });

        steps_track_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private boolean isBind = false;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));
    private Messenger messenger;

    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
        Log.i("StepService","StepService开启了");
    }

    /**
     * 定时任务
     */
    private TimerTask timerTask;
    private Timer timer;
    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            /**
             * 设置定时器，每隔三秒钟去更新一次运动步数
             */
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        messenger = new Messenger(service);
                        Message msg = Message.obtain(null, Constants.MSG_FROM_CLIENT);
                        msg.replyTo = mGetReplyMessenger;
                        messenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 3000);
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    /**
     * 设置记录数据
     *
     */
    private void setDatas() {
        StepEntity stepEntity = stepDataDao.getCurDataByDate(curSelDate);
        //制作一些假数据

//        for(int k=1;k<20;k++){
//            StepEntity entity = new StepEntity();
//            entity.setCurDate("2017-09-"+k);
//            entity.setSteps(123*k+"");
//            stepDataDao.addNewData(entity);
//        }
        if (stepEntity != null) {
            int steps = Integer.parseInt(stepEntity.getSteps());

            //获取全局的步数
            totalStepsTv.setText(String.valueOf(steps));
            //计算总公里数
            totalKmTv.setText(countTotalKM(steps));
        } else {
            //获取全局的步数
            totalStepsTv.setText("0");
            //计算总公里数
            totalKmTv.setText("0");
        }

        //设置时间
        String time = TimeUtil.getWeekStr(curSelDate);
        kmTimeTv.setText(time);
        stepsTimeTv.setText(time);
        Log.i("today",time);
    }

    /**
     * 简易计算公里数，假设一步大约有0.7米
     *
     * @param steps 用户当前步数
     * @return
     */
    private String countTotalKM(int steps) {
        double totalMeters = steps * 0.7;
        //保留两位有效数字
        return df.format(totalMeters / 1000);
    }


    /**
     * 获取全部运动历史纪录
     */
    private void getRecordList() {
        //获取数据库
        stepDataDao = new StepDataDao(this);
        stepEntityList.clear();
        stepEntityList.addAll(stepDataDao.getAllDatas());
        // TODO: 在这里获取历史记录条数，当条数达到7条或以上时,显示更多历史记录
//        if (stepEntityList.size() >= 7) {
            steps_history_ll.setVisibility(View.GONE);
//        }else {
//            steps_history_ll.setVisibility(View.GONE);
//        }
        Log.i("stepEntityList", stepEntityList +"");
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            //这里用来获取到Service发来的数据
            case Constants.MSG_FROM_SERVER:

                //如果是今天则更新数据
                if (curSelDate.equals(TimeUtil.getCurrentDate())) {
                    //记录运动步数
                    int steps = msg.getData().getInt("steps");
                    //设置的步数
                    totalStepsTv.setText(String.valueOf(steps));
                    //计算总公里数
                    totalKmTv.setText(countTotalKM(steps));
                }
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得解绑Service，不然多次绑定Service会异常
        if (isBind) this.unbindService(conn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!CommonUtil.isReleaseVersion()
                && StepUtils.isCanStep())
            getMenuInflater().inflate(R.menu.menu_uusport_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        } else if (StepUtils.isCanStep() && !CommonUtil.isReleaseVersion() && item.getItemId() == R.id.more){
            startActivity(new Intent(this,UURanking.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
