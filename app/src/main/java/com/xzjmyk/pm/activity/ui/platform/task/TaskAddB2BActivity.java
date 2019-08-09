package com.xzjmyk.pm.activity.ui.platform.task;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.OAConfig;
import com.core.net.http.ViewUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.SwitchView;
import com.core.widget.view.TagGroup;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.SelectCollisionActivity;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.util.im.RecordUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskAddB2BActivity extends BaseActivity implements View.OnClickListener, RecognizerDialogListener {


    @ViewInject(R.id.et_task_remark)
    private EditText et_task_remark;
    @ViewInject(R.id.et_task_people)
    private TagGroup et_task_people;
    @ViewInject(R.id.cb_task_reply)
    private SwitchView cb_task_reply;


    @ViewInject(R.id.et_startime)
    private TextView et_startime;//开始时间

    @ViewInject(R.id.et_task_name)
    private EditText et_task_name;
    @ViewInject(R.id.et_title)
    private EditText et_title;
    @ViewInject(R.id.voice_search_iv)
    private ImageView voice_search_iv;

    @ViewInject(R.id.et_endtime)
    private TextView et_endtime;
    @ViewInject(R.id.bt_task_add)
    private Button bt_task_add;


    @ViewInject(R.id.iv_recode)
    private ImageView bt_recode;//录音
    @ViewInject(R.id.ic_voice_center)
    private RelativeLayout ic_voice_center;//麦克风布局
    @ViewInject(R.id.voice_display_voice_layout)
    private LinearLayout voice_display_voice_layout;//语音文件布局
    //展示页面控件voice_display_voice_layout
    @ViewInject(R.id.voice_display_voice_play)
    private ImageView bt_voice_play;//播放按钮
    @ViewInject(R.id.voice_display_voice_progressbar)
    private ProgressBar display_voice_progressbar;//进度
    @ViewInject(R.id.voice_display_voice_time)
    private TextView display_voice_time;//秒
    //麦克风布局文件控件
    @ViewInject(R.id.tv_voice_times)
    private TextView tv_voice_times;//录音时间
    @ViewInject(R.id.iv_record)
    private ImageView iv_record;//声麦图片
    @ViewInject(R.id.tv_voice_tips)
    private TextView tv_voice_tips;//麦克风信息提示

    private static final String PATH = "/sdcard/uu/record/";// 录音存储路径
    private String typeAmr = ".amr";
    private String selectNames;
    private String selectCode = null;

    private static final int MAX_TIME = 60;// 最长录音时间
    private static final int MIN_TIME = 2;// 最短录音时间

    private static final int RECORD_NO = 0; // 不在录音
    private static final int RECORD_ING = 3; // 正在录音
    private static final int RECORD_ED = 2; // 完成录音
    private int mRecord_State = 0; // 录音的状态
    private int mMAXVolume;// 最大音量高度
    private int mMINVolume;// 最小音量高度
    private boolean mPlayState; // 播放状态

    private int mPlayCurrentPosition;// 当前播放的时间
    @ViewInject(R.id.lay_voice_layout)
    private LinearLayout lay_voice_layout;
    @ViewInject(R.id.view_line)
    private View view_line;
    @ViewInject(R.id.iv_find)
    private ImageView iv_find;

    @ViewInject(R.id.et_task_startime)
    private TextView et_task_startime;
    private DateTimePickerDialog dialog;
    private final static int LOAD_SUCCESS_ADD = 1;

    @ViewInject(R.id.iv_delete_voice)
    private ImageView iv_delete_voice;
    @ViewInject(R.id.chaosonren_rl)
    private RelativeLayout chaosonren_rl;
    private Context ct;
    private int year;
    private int month;
    private String[] tagValues;
    private int save = 0;
    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_SUCCESS_ADD:
//				progressDialog.dismiss();
                    String result = msg.getData().getString("result");
                    Log.i(TAG, result + "");
                    JSONObject object = JSON.parseObject(result);
                    Boolean falg = object.getBoolean("success");
                    if (falg) {
                        ViewUtil.ShowMessageTitle(ct, getString(R.string.task_send_success));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    } else {
                        ViewUtil.ShowMessageTitle(ct, getString(R.string.send_failed));
                    }
                    progressDialog.dismiss();
                    break;
                case RECORD_NO://停止录音

                    if (mRecord_State == RECORD_ING) {
                        mRecord_State = RECORD_ED;
                        try {
                            // 停止录音
                            mRecordUtil.stop();
                            // 初始化录音音量
                            mRecord_Volume = 0;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case RECORD_ING://正在录音
                    // 显示录音时间
                    tv_voice_times.setText((int) mRecord_Time + "″");
                    // 根据录音声音大小显示效果

                    break;
                case VOICE_UPDATE://麦克风更新界面
                    // 显示录音时间
                    tv_voice_times.setText((int) mRecord_Time + "/60″");// ((int)
                    display_voice_time.setText((int) mRecord_Time + "”");
                    // 音量大小的动画
                    if (mRecord_Volume < 300.0) {
                        iv_record
                                .setBackgroundResource(R.drawable.chat_icon_voice1);
                    } else if (mRecord_Volume >= 300.0 && mRecord_Volume < 1000) {
                        iv_record
                                .setBackgroundResource(R.drawable.chat_icon_voice2);
                    } else if (mRecord_Volume >= 1000.0 && mRecord_Volume < 2000) {
                        iv_record
                                .setBackgroundResource(R.drawable.chat_icon_voice3);
                    } else if (mRecord_Volume >= 2000.0 && mRecord_Volume < 4000) {
                        iv_record
                                .setBackgroundResource(R.drawable.chat_icon_voice4);
                    } else if (mRecord_Volume >= 4000.0 && mRecord_Volume < 10000) {
                        iv_record
                                .setBackgroundResource(R.drawable.chat_icon_voice5);
                    } else if (mRecord_Volume >= 10000.0) {
                        iv_record
                                .setBackgroundResource(R.drawable.chat_icon_voice6);
                    }
                    break;
                case 6:
                    String id = msg.getData().getString("id");
                    saveTask(id);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    ViewUtil.ToastMessage(ct, msg.getData().getString("result"));
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visit_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://关键代码
                onBackPressed();
                break;
            case R.id.save:
                if (save == 0 && MyApplication.getInstance().isNetworkActive()) {
                    saveTask("");
                }
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Time time = new Time("GMT+8");
        time.setToNow();
        year = time.year;
        month = time.month + 1;
        initView();
    }


    public void initView() {
        setContentView(R.layout.act_taskb2b_add);
        ViewUtils.inject(this);
        chaosonren_rl.setVisibility(View.GONE);  // 抄送人接口暂无参数时
        ct = this;
        TAG = "AddTaskActivity";
        setTitle(getString(R.string.task_add));
        String people = getIntent().getStringExtra("people") == null ? "" : getIntent().getStringExtra("people");
        bt_task_add.setOnClickListener(this);
        if (!StringUtil.isEmpty(people)) {
            et_task_people.setTags(people);
            selectNames = people;
        }
        et_task_people.setOnKeyListener(null);


        iv_find.setOnClickListener(this);
        bt_voice_play.setOnClickListener(this);
        et_startime.setOnClickListener(this);
        et_task_startime.setOnClickListener(this);
        bt_recode.setOnTouchListener(new VoiceTouchListen());
        iv_delete_voice.setOnClickListener(this);

        voice_search_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizerDialogUtil.showRecognizerDialog(ct, TaskAddB2BActivity.this);
            }
        });
    }

    private void showDateDialog(Context ct, final TextView tv) {
        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                String time = year + "-" + month + "-" + day + " " + hour + ":" + minute;
                tv.setText(time);
            }
        });
        picker.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_startime:
//                CommonUtil.showDateDialog(mContext, v);
                showDateDialog(this, et_startime);
                break;
            case R.id.et_task_startime:
                showDateDialog(this, et_task_startime);
                break;
            case R.id.iv_find:
                Intent intent = new Intent(ct, SelectCollisionActivity.class);
                SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                        .setTitle(getString(R.string.select_doman))
                        .setSingleAble(false)
                        .setSelectCode(selectCode);
                intent.putExtra(OAConfig.MODEL_DATA, bean);
                startActivityForResult(intent, 0x01);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        et_task_remark.setText(et_task_remark.getText().toString() + CommonUtil.getPlaintext(text));
    }

    @Override
    public void onError(SpeechError speechError) {

    }

    /**
     * @author Administrator
     * @功能:触摸事件
     */
    class VoiceTouchListen implements View.OnTouchListener {
        int yLeng = -50;// 划动的距离

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.i("task_Y", event.getY() + "");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN://
                    startRecodeIng(v, event);
                    return true;
                case MotionEvent.ACTION_MOVE://移动事件

                    if (event.getY() < yLeng) {
                        tv_voice_tips.setText("松开手指，取消发送");
                        tv_voice_tips.setTextColor(Color.RED);
                    } else {
                        tv_voice_tips.setText("手指上滑,取消发送");
                        tv_voice_tips.setTextColor(Color.WHITE);
                    }
                    return true;
                case MotionEvent.ACTION_UP://上滑事件
                    stopRecodeIng(v, event, yLeng);
                    //合成音频文件
                    Log.i("Voice", "File path：" + mRecordPath);
                    File file = new File(mRecordPath);
                    tempFiles.add(file);
//                    mergeARMFiles();
                    lay_voice_layout.setVisibility(View.VISIBLE);
                    view_line.setVisibility(View.VISIBLE);
                    //合成音频文件
                    return true;
                default:
                    return false;
            }

        }

    }

    private void saveTask(String id) {
        if (StringUtil.isEmpty(et_title.getText().toString())) {
            ToastMessage(getString(R.string.task_title_must_input));
            return;
        }
        if (StringUtil.isEmpty(et_task_remark.getText().toString())) {
            ToastMessage(getString(R.string.task_detail_must_input));
            return;
        }

        String resourcename = "";
        if (tagValues != null && tagValues.length > 0) {
            try {
                resourcename = getResourceName(tagValues);
            } catch (Exception e) {
                if (StringUtil.isEmpty(et_task_people.getInputTagText().toString())) {
                    ToastMessage(getString(R.string.task_doman_must_input));
                    return;
                } else if (et_task_people.getInputTagText().toString().length() > 1000) {
                    ToastMessage(getString(R.string.task_limit_doman));
                    return;
                }
            }
        } else {
            ToastMessage(getString(R.string.task_doman_must_input));
            return;
        }
        if (StringUtil.isEmpty(et_startime.getText().toString())) {
            ToastMessage(getString(R.string.task_startT_must_input));
            return;
        }
        if (StringUtil.isEmpty(et_task_startime.getText().toString())) {
            ToastMessage(getString(R.string.task_endT_must_input));
            return;
        }
        if (et_startime.getText().toString().compareTo(et_task_startime.getText().toString()) >= 0) {
            ToastMessage(getString(R.string.endT_large_startT));
            return;
        }

        String formStore = "{\n" +
                "\"detail\":\"" + et_task_remark.getText().toString() + "\",\n" +
                "\"recordercode\":\"" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu") + "\",\n" +
                "\"uu\":\"" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu() + "\",\n" +
                "\"taskname\":\"" + et_title.getText().toString() + "\",\n" +//任务名称
                "\"domancode\":\"" + resourcename + "\",\n" +//执行人
                "\"startdate\":\"" + et_startime.getText().toString() + ":00" + "\",\n" +
                "\"enddate\":\"" + et_task_startime.getText().toString() + ":00" + "\",\n" +
                "}";
        sendDataToServer(formStore);
    }

    private String getResourceName(String[] tagValues) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (String e : tagValues)
            if (!StringUtil.isEmpty(e))
                builder.append(e.trim() + ",");
        StringUtil.removieLast(builder);
        return builder.toString();
    }


    /**
     * 用于存放要合并的文件的集合
     **/
    private List<File> tempFiles = new ArrayList<File>();
    /**
     * 合并之后的文件
     **/
    private File finalFile;

    private static String mRecordPath = "";// 录音的存储名称
    private MediaPlayer mMediaPlayer;
    private RecordUtil mRecordUtil;
    private float mRecord_Time = 0;// 录音的时间
    private double mRecord_Volume;// 麦克风获取的音量值
    private final static int VOICE_UPDATE = 5;

    /**
     * @author Administrator
     * @功能:录音
     */
    private void startRecodeIng(View v, MotionEvent event) {
        //v.setPressed(true);
        ic_voice_center.setVisibility(View.VISIBLE);
        tv_voice_tips.setText("松开手指，取消发送");
        // 开始录音
        // 修改录音状态
        mRecord_State = RECORD_ING;
        // 设置录音保存路径
        mRecordPath = PATH + UUID.randomUUID().toString() + typeAmr;
        // 实例化录音工具类
        mRecordUtil = new RecordUtil(mRecordPath);
        // 开始录音
        try {
            mRecordUtil.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 初始化录音时间
                //mRecord_Time = 0;
                while (mRecord_State == RECORD_ING) {
                    if (mRecord_Time >= MAX_TIME) {
                        //停止
                        mhandler.sendEmptyMessage(RECORD_NO);
                    } else {
                        // 每隔200毫秒就获取声音音量并更新界面显示
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mRecord_Time += 0.2;
                        //更新音量
                        if (mRecord_State == RECORD_ING) {
                            mRecord_Volume = mRecordUtil.getAmplitude();
                            mhandler.sendEmptyMessage(VOICE_UPDATE);
                        }
                    }
                }
            }
        }).start();
    }


    /**
     * @author Administrator
     * @功能:停止录音 y滑动距离
     */
    private void stopRecodeIng(View v, MotionEvent event, int y) {
        ic_voice_center.setVisibility(View.INVISIBLE);
        if (mRecord_State == RECORD_ING) {
            // 修改录音状态
            mRecord_State = RECORD_ED;
            try {
                mRecordUtil.stop();
                mRecord_Volume = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (event.getY() < y) {
                // 显示提醒
                Toast.makeText(ct, "放弃录音",
                        Toast.LENGTH_SHORT).show();
                // 修改录音状态
                mRecord_State = RECORD_NO;
                // 修改录音时间
                mRecord_Time = 0;
                // 修改显示界面
            } else {
                bt_voice_play
                        .setImageResource(R.drawable.globle_player_btn_play);
                tv_voice_times.setText((int) mRecord_Time + "″");

            }

        }

    }

    /**
     * @author Administrator
     * @功能:添加任务
     */
    public void sendDataToServer(String formStore) {
        save = 1;
        progressDialog.show();
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_save;
        Log.i(TAG, url + "");
        Log.i(TAG, formStore + "");
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("formStore", formStore);
        //param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(
                ct, url,
                param,
                mhandler, headers, LOAD_SUCCESS_ADD, null, null, "post");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 2:
                if (resultCode == 2) {
                    String en_name = data.getStringExtra("en_name");
                    et_task_people.setTags(en_name);
                }
                if (resultCode == 1) {
                    String values = data.getStringExtra("employees");
                    Log.i("fromStore", values + "");
                    String[] tag_values = values.split(",");
                    et_task_people.setTags(tag_values);
                }
                break;
            case 0x01://执行人多选
                List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
                if (ListUtils.isEmpty(employeesList)) {
                    selectNames = "";
                    selectCode = "";
                    tagValues = null;
                    et_task_people.setVisibility(View.GONE);
                    return;
                } else {
                    et_task_people.setVisibility(View.VISIBLE);
                }
                int i = 0;
                StringBuilder select = new StringBuilder();
                StringBuilder selectCode = new StringBuilder();
                tagValues = new String[employeesList.size()];
                for (SelectEmUser e : employeesList) {
                    select.append(e.getEmName() + ",");
                    selectCode.append(e.getEmCode() + ",");
                    tagValues[i++] = e.getEmCode();
                }
                StringUtil.removieLast(select);
                StringUtil.removieLast(selectCode);
                selectNames = select.toString();
                this.selectCode = selectCode.toString();
                String[] tag = selectNames.split(",");
                et_task_people.setTags(tag);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

