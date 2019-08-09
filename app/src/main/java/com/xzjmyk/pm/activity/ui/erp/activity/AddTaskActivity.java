/**
 *
 */
package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.SwitchView;
import com.core.widget.view.TagGroup;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.DbfindListActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.util.im.RecordUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author LiuJie
 */
public class AddTaskActivity extends BaseActivity implements OnClickListener {


    @ViewInject(R.id.et_task_remark)
    private EditText et_task_remark;
    @ViewInject(R.id.et_task_people)
    private TagGroup et_task_people;
    @ViewInject(R.id.cb_task_reply)
    private SwitchView cb_task_reply;
    @ViewInject(R.id.et_task_startime)
    private TextView et_task_startime;
    @ViewInject(R.id.task_startime_rl)
    private RelativeLayout task_startime_rl;
    @ViewInject(R.id.et_task_name)
    private EditText et_task_name;
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

    private DateTimePickerDialog dialog;
    private final static int LOAD_SUCCESS_ADD = 1;

    @ViewInject(R.id.iv_delete_voice)
    private ImageView iv_delete_voice;

    private Context ct;

    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_SUCCESS_ADD:
//				progressDialog.dismiss();
                    String result = msg.getData().getString("result");
                    Log.i(TAG, result);
                    JSONObject object = JSON.parseObject(result);
                    Boolean falg = object.getBoolean("success");
                    if (falg) {
                        ViewUtil.ShowMessageTitle(ct, "保存成功！");
                        mhandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                onBackPressed();
                            }
                        }, 1000);
                    } else {
                        ViewUtil.ShowMessageTitle(ct, "保存失败！");
                    }
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
    private int year;
    private int month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Time time = new Time("GMT+8");
        time.setToNow();
        year = time.year;
        month = time.month + 1;
        initView();
    }

    /* (non-Javadoc)
     * @see com.erp.base.BaseAcivity#initView()
     */
    public void initView() {
        setContentView(R.layout.act_task_add);
        ViewUtils.inject(this);
        ct = this;
        TAG = "AddTaskActivity";
       setTitle("添加任务");
        String people = getIntent().getStringExtra("people") == null ? "未填写" : getIntent().getStringExtra("people");
        bt_task_add.setOnClickListener(this);
        task_startime_rl.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == event.ACTION_DOWN)
                    showDateDialog();
                return false;
            }
        });
        et_task_people.setTags(people);
        et_task_people.setOnKeyListener(null);
        et_task_people.setIsAppendMode(true);
        iv_find.setOnClickListener(this);
        bt_voice_play.setOnClickListener(this);
        bt_recode.setOnTouchListener(new VoiceTouchListen());
        iv_delete_voice.setOnClickListener(this);
    }



    private void showDateDialog() {
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
                String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00";
                et_task_startime.setText(time);
            }
        });
        picker.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_delete_voice:
                if (finalFile != null && finalFile.isFile()) {
                    lay_voice_layout.setVisibility(View.GONE);//删除文件;sd卡未删除
                    view_line.setVisibility(View.INVISIBLE);
                    tempFiles.clear();
                    mRecord_Time = 0;
                    finalFile = null;
                }
                break;
            case R.id.iv_find:
                Intent intent = new Intent(ct, DbfindListActivity.class).putExtra("listViewMode", ListView.CHOICE_MODE_MULTIPLE_MODAL);
                startActivityForResult(intent, 2);
                break;
            case R.id.bt_task_add:
                if (!StringUtil.isEmpty(et_task_startime.getText().toString())
                        && et_task_people.getTags().length > 0
                        && !StringUtil.isEmpty(et_task_remark.getText().toString())) {
                    boolean falg = ViewUtil.isCheckDateTime(
                            CommonUtil.getStringDate(System.currentTimeMillis()),
                            et_task_startime.getText().toString(), "yyyy-MM-dd HH:mm:ss");
                    if (et_task_remark.getText().toString().length() <= 200) {
                        if (!falg) {
                            if (!et_task_people.getTags()[0].equals("未填写")) {
                                //保存附件
                                if (finalFile != null) {
                                    saveAmrFile();//附件保存
                                } else {
                                    saveTask(null); //无附件保存任务
                                }
                            } else {
                                ViewUtil.ShowMessageTitle(ct, "未选择处理人！");
                            }

                        } else {
                            ViewUtil.ShowMessageTitle(ct, "截止时间小于当前时间！");
                        }
                    } else {
                        ViewUtil.ShowMessageTitleAutoDismiss(ct, "任务描述已经超过200字符的限制！", 1000);
                    }
                } else {
                    ViewUtil.ShowMessageTitle(ct, "请输入完整的信息！");
                }
                break;
//            case R.id.et_task_startime:
//                showDialog(v);
//                break;
            case R.id.voice_display_voice_play:
                // 播放录音
                if (!mPlayState) {
                    mMediaPlayer = new MediaPlayer();
                    // 添加录音的路径
                    try {
                        mMediaPlayer.setDataSource(finalFile.getAbsolutePath());
                        // 准备
                        mMediaPlayer.prepare();
                        // 播放
                        mMediaPlayer.start();
                        // 修改播放状态
                        mPlayState = true;
                        // 根据时间修改界面
                        new Thread(new Runnable() {

                            public void run() {

                                display_voice_progressbar
                                        .setMax((int) mRecord_Time);
                                mPlayCurrentPosition = 0;
                                while (mMediaPlayer.isPlaying()) {
                                    mPlayCurrentPosition = mMediaPlayer
                                            .getCurrentPosition() / 1000;
                                    display_voice_progressbar
                                            .setProgress(mPlayCurrentPosition);
                                }
                            }
                        }).start();
                        mPlayState = true;
                        // 修改播放图标
                        bt_voice_play
                                .setImageResource(R.drawable.globle_player_btn_stop);

                        mMediaPlayer
                                .setOnCompletionListener(new OnCompletionListener() {
                                    // 播放结束后调用
                                    public void onCompletion(MediaPlayer mp) {
                                        // 停止播放
                                        mMediaPlayer.stop();
                                        // 修改播放状态
                                        mPlayState = false;
                                        // 停止播放图标
                                        //stopRecordAnimation();
                                        // 修改播放图标
                                        bt_voice_play
                                                .setImageResource(R.drawable.globle_player_btn_play);
                                        // 初始化播放数据
                                        mPlayCurrentPosition = 0;
                                        display_voice_progressbar
                                                .setProgress(mPlayCurrentPosition);

                                    }
                                });
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                    if (mMediaPlayer != null) {
                        // 根据播放状态修改显示内容
                        if (mMediaPlayer.isPlaying()) {
                            mPlayState = false;
                            mMediaPlayer.stop();
                            // 修改播放图标
                            bt_voice_play
                                    .setImageResource(R.drawable.globle_player_btn_play);
                            // 初始化播放数据
                            mPlayCurrentPosition = 0;
                            display_voice_progressbar
                                    .setProgress(mPlayCurrentPosition);
                            // 停止播放图标
                            //stopRecordAnimation();

                        } else {
                            mPlayState = false;
                            bt_voice_play
                                    .setImageResource(R.drawable.globle_player_btn_play);
                            // 初始化播放数据
                            mPlayCurrentPosition = 0;
                            display_voice_progressbar
                                    .setProgress(mPlayCurrentPosition);
                            // 停止播放图标
                            //	stopRecordAnimation();

                        }
                    }
                }


                break;
            default:
                break;
        }
    }

    /**
     * @author Administrator
     * @功能:触摸事件
     */
    class VoiceTouchListen implements OnTouchListener {
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
                    mergeARMFiles();
                    lay_voice_layout.setVisibility(View.VISIBLE);
                    view_line.setVisibility(View.VISIBLE);
                    //合成音频文件
                    return true;
                default:
                    return false;
            }

        }

    }

    //保存任务
    private void saveTask(String id) {
        int type = cb_task_reply.isChecked() == true ? 1 : 0;
        String name = "来自" + CommonUtil.getSharedPreferences(ct, "erp_username") + "的任务";
        String formStore;
        String[] people = et_task_people.getTags();
        String resourcename = "";
        for (int i = 0; i < people.length; i++) {
            if (i == people.length - 1) {
                resourcename = resourcename + people[i].trim();
            } else {
                resourcename = resourcename + people[i].trim() + ",";
            }
        }

        if (id == null) {

            formStore = "{'name':'" + name + "'"
                    + ",'startdate':'" + CommonUtil.getStringDate(System.currentTimeMillis()) + "',"
                    + "'enddate':'" + et_task_startime.getText().toString() + "',"
                    + "'type':'" + type + "','resourcename':'" + resourcename + "',"
                    + "'description':'" + et_task_remark.getText().toString() + "'"
                    + "}";
        } else {
            //上传附件
            formStore = "{'name':'" + name + "'"
                    + ",'startdate':'" + CommonUtil.getStringDate(System.currentTimeMillis()) + "',"
                    + "'enddate':'" + et_task_startime.getText().toString() + "',"
                    + "'type':'" + type + "','resourcename':'" + resourcename + "',"
                    + "'description':'" + et_task_remark.getText().toString() + "',"
                    + "'attachs':'" + id + "'"
                    + "}";
        }
        Log.i("fromStore", formStore);
        sendDataToServer(formStore);
    }

    //保存附件
    private void saveAmrFile() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
        params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        params.addBodyParameter("file", finalFile);
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/uploadAttachs.action";
        Log.i("url", "url:" + url);
        Log.i("url", "file:" + finalFile.getAbsolutePath());
        HttpUtils http = new HttpUtils();
        http.send(HttpMethod.POST,
                url,
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        ViewUtil.ToastMessage(ct, "开始上传...");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        if (isUploading) {
                        } else {
                        }
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i("json", responseInfo.result);
                        ViewUtil.ToastMessage(ct, "上传成功：");
                        JSONObject root = JSON.parseObject(responseInfo.result);
                        String id = root.getString("id");
                        id = id.substring(1, id.length() - 1);
                        Bundle bundle = new Bundle();
                        bundle.putString("id", id);
                        Message message = new Message();
                        message.what = 6;
                        message.setData(bundle);
                        mhandler.sendMessage(message);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        ViewUtil.ToastMessage(ct, "上传失败：" + msg);
                    }
                });
    }

    /**
     * @author Administrator
     * @功能:获取合并文件对象
     */
    private File getSumFile(boolean isTempFile) {
        if (finalFile != null && finalFile.isFile()) {
            return finalFile;
        }
        if (!Environment.getExternalStorageState().
                equals(Environment.MEDIA_MOUNTED)) {
            Log.w("Waring", "检测到你的手机没有插入SD卡，请插入SD后再试！");
        }
        try {
            File parentFile = new File(Environment.getExternalStorageDirectory()
                    .getCanonicalFile() + "/uu/" + "recorder_sum");
            if (!parentFile.exists() || parentFile == null) {//如果目录不存在
                parentFile.mkdirs();//创建parentFile目录
            }
            finalFile = new File(parentFile, "real.amr");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return finalFile;
    }


    /**
     * 通过FileOutputStream、与FileInputStream方式
     * 将多个文件进行合并，并删除原文件
     */
    public void mergeARMFiles() {
        if (tempFiles.isEmpty()) return;//如果还没录制则，不进行合并
        File realFile = getSumFile(false);
        try {
            FileOutputStream fos = new FileOutputStream(realFile);
            for (int i = 0; i < tempFiles.size(); i++) {//遍历tempFiles集合，合并所有临时文件
                FileInputStream fis = new FileInputStream(tempFiles.get(i));
                byte[] tmpBytes = new byte[fis.available()];
                int length = tmpBytes.length;//文件长度
                //头文件
                if (i == 0) {
                    while (fis.read(tmpBytes) != -1) {
                        fos.write(tmpBytes, 0, length);
                    }
                }
                //之后的文件，去掉头文件就可以了.amr格式的文件的头信息为 6字节
                else {
                    while (fis.read(tmpBytes) != -1) {
                        fos.write(tmpBytes, 6, length - 6);
                    }
                }
                fos.flush();
                fis.close();
            }
            fos.close();//所有的文件合并结束，关闭输出流
            Log.i("info", "此次录音文件：" + realFile.getName() + " 已保存到：" +
                    realFile.getAbsolutePath() + "目录下");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //删除合并过的临时文件
        for (File file : tempFiles) {
            if (file.exists()) {
                //file.delete();
            }
        }
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
//		progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "plm/task/addbilltask.action";
        Log.i(TAG, url);
        Log.i(TAG, formStore);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("formStore", formStore);
        //param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(
                ct, url,
                param,
                mhandler, headers, LOAD_SUCCESS_ADD, null, null, "get");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (data == null) {
                    return;
                }
                if (resultCode == 2) {
                    String en_name = data.getStringExtra("en_name");
                    et_task_people.setTags(en_name);
                }
                if (resultCode == 1) {
                    String values = data.getStringExtra("employees");
                    Log.i("fromStore", values);
                    String[] tag_values = values.split(",");
                    et_task_people.setTags(tag_values);
                }
                break;
            case 1:

                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
