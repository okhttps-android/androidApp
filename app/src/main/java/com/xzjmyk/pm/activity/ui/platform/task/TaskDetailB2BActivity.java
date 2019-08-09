package com.xzjmyk.pm.activity.ui.platform.task;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.net.HttpUtil;
import com.core.net.http.ViewUtil;
import com.core.widget.CustomerListView;
import com.core.utils.TimeUtils;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskDetailB2BActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.bt_task_huifu)
    private Button bt_task_huifu;
    @ViewInject(R.id.bt_task_queren)
    private Button bt_task_queren;
    @ViewInject(R.id.bt_task_bohui)
    private Button bt_task_bohui;
    @ViewInject(R.id.ll_bt_task)
    private LinearLayout ll_bt_task;
    @ViewInject(R.id.reply_ll)
    private LinearLayout reply_ll;
    @ViewInject(R.id.et_task_name)
    private EditText et_task_name;

    @ViewInject(R.id.tv_task_endtime)
    private TextView tv_task_endtime;
    @ViewInject(R.id.tv_task_name)
    private TextView tv_task_name;
    @ViewInject(R.id.tv_task_emcode)
    private TextView tv_task_emcode;
    @ViewInject(R.id.tv_task_performer)
    private TextView tv_task_performer;
    @ViewInject(R.id.tv_task_starttime)
    private TextView tv_task_starttime;
    @ViewInject(R.id.tv_task_duration)
    private TextView tv_task_duration;
    @ViewInject(R.id.tv_task_status)
    private TextView tv_task_status;
    @ViewInject(R.id.tv_task_describe)
    private TextView tv_task_describe;

    @ViewInject(R.id.lay_voice_task)
    private LinearLayout lay_voice_task;// 语音展示
    @ViewInject(R.id.iv_recode)
    private ImageView iv_recode;// 播放
    @ViewInject(R.id.tv_voice_msg)
    private TextView tv_voice_msg;

    @ViewInject(R.id.iv_taskMsg)
    private CustomerListView iv_taskMsg;

    SimpleAdapter adapter;

    private Context ct;


    private String taskId;
    private final static int LOAD_EM_NAME = 6;
    private final static int LOAD_FILE_SUCCESS = 2;
    private Handler mhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            try {
                switch (msg.what) {
                    case Constants.LOAD_SUCCESS:
                        String result = msg.getData().getString("result");
                        if (!JSONUtil.validate(result)) return;
                        JSONObject object = JSON.parseObject(result);
                        Boolean falg = object.getBoolean("success");
                        if (falg) {
                            ViewUtil.ShowMessageTitle(ct, getString(R.string.make_adeal_success));
                            Intent intent = new Intent();
                            String task = tv_task_emcode.getText().toString();
                            int type = 0;
                            if (!StringUtil.isEmpty(task) && task.equals(CommonUtil.getName())) {
                                type = 1;
                            }
                            intent.putExtra("type", type);
                            setResult(0x20, intent);
//                        goBackMenu();
                            finish();
                        } else {
                            ViewUtil.ShowMessageTitle(ct, getString(R.string.make_adeal_failed));
                        }
                        break;
                    case LOAD_EM_NAME:
                        result = msg.getData().getString("result");
                        object = JSON.parseObject(result);
                        String em_name = object.getString("em_name");
                        if (!StringUtil.isEmpty(em_name)) {
                            CommonUtil.setSharedPreferences(ct, "erp_emname", em_name);
                        }
                        break;
                    case LOAD_FILE_SUCCESS:
                        result = msg.getData().getString("result");
                        JSONObject root = JSON.parseObject(result);
                        String fileUrl = root.getString("url");
                        if (fileUrl.length() > 2) {
                            lay_voice_task.setVisibility(View.VISIBLE);
                            fileUrl = fileUrl.substring(1, fileUrl.length() - 1).split(", ")[0];
                            String url = CommonUtil.getAppBaseUrl(ct);
                            String ipaddress = url.split("//")[1].split("/")[0];
                            String file_url = "http://" + ipaddress + "/" + "postattach" + fileUrl.split("postattach")[1];
                            downFileToSD(fileUrl, file_url);
                        } else {
                            lay_voice_task.setVisibility(View.GONE);
                        }
                        break;
                    case LOAD_SUCCESS_MSGTASK:
                        result = msg.getData().getString("result");
                        if (adapter == null) {
                            adapter = new SimpleAdapter(ct, JSON.parseObject(result).getJSONArray("listdata"));
                            iv_taskMsg.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
//				progressDialog.dismiss();
                        break;
                    case Constants.APP_SOCKETIMEOUTEXCEPTION:
                        result = msg.getData().getString("result");
                        ToastMessage(result);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {

            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        setContentView(R.layout.act_task_detail);
        ViewUtils.inject(this);
        ct = this;
        setTitle(getString(R.string.taskdetail_title));
        TAG = "DetailTask";
        bt_task_bohui.setOnClickListener(this);
        bt_task_huifu.setOnClickListener(this);
        bt_task_queren.setOnClickListener(this);
        iv_recode.setOnClickListener(this);
        if (getIntent() != null && !getIntent().getBooleanExtra("isMe", true)) {
            reply_ll.setVisibility(View.GONE);
        }
    }

    public void initData() {
        //loadDataMsgTask(ra_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

        tv_task_name.setText(intent.getStringExtra("taskname") == null ? getString(R.string.common_noinput) : intent.getStringExtra("taskname"));
        // 提出人
        tv_task_emcode
                .setText(intent.getStringExtra("taskemcode") == null ? getString(R.string.common_noinput) : intent.getStringExtra("taskemcode"));
        // 执行人performer
        tv_task_performer
                .setText(intent.getStringExtra("performer") == null ? getString(R.string.common_noinput) : intent.getStringExtra("performer"));
        tv_task_status.setText(intent.getStringExtra("status") == null ? getString(R.string.common_noinput) : intent.getStringExtra("status"));
        tv_task_duration.setText(intent.getStringExtra("duration") == null ? getString(R.string.common_noinput) : intent.getStringExtra("duration"));
        tv_task_describe
                .setText(intent.getStringExtra("description") == null ? getString(R.string.common_noinput) : intent.getStringExtra("description"));
        tv_task_starttime
                .setText(intent.getStringExtra("tasktime") == null ? getString(R.string.common_noinput) : intent.getStringExtra("tasktime"));
//                .setText(intent.getStringExtra("tasktime") == null ? "未填写" :
//                        DateFormatUtil.long2Str(TimeUtils.f_str_2_long(intent.getStringExtra("tasktime")), "yyyy-MM-dd HH:mm"));
        taskId = intent.getStringExtra("taskid");
        tv_task_endtime.
                setText(intent.getStringExtra("endtime") == null ? getString(R.string.common_noinput) :
                        DateFormatUtil.long2Str(TimeUtils.f_str_2_long(intent.getStringExtra("endtime")), "yyyy-MM-dd HH:mm"));

        String ra_taskid = intent.getStringExtra("ra_taskid");
        String attachs = intent.getStringExtra("attachs");
        if (!StringUtil.isEmpty(attachs)) {
            // 加载语音文件
            String url = CommonUtil.getAppBaseUrl(ct) + "/common/downloadbyId.action?id=" + attachs.split(";")[0];
            downFileToSD("/uu/recorder_download/" + attachs.split(";")[0] + ".amr", url);
        } else {
            // 没有语音文件
            lay_voice_task.setVisibility(View.GONE);
        }
        String status = intent.getStringExtra("status");
        if ("进行中".equals(status)) {
            // 执行人
//            if (getIsHandle(intent.getStringExtra("performer"))) {
            ll_bt_task.setVisibility(View.GONE);
            bt_task_huifu.setVisibility(View.VISIBLE);
            et_task_name.setVisibility(View.VISIBLE);
//            } else {
//                et_task_name.setVisibility(View.GONE);
//                ll_bt_task.setVisibility(View.GONE);
//                bt_task_huifu.setVisibility(View.GONE);
//            }

        } else if ("待确认".equals(status)) {
            // 提出人等于当前用户
//            if (getIsHandle(intent.getStringExtra("taskemcode"))) {
            ll_bt_task.setVisibility(View.VISIBLE);
            bt_task_huifu.setVisibility(View.GONE);
            et_task_name.setVisibility(View.VISIBLE);
//            } else {
//                et_task_name.setVisibility(View.GONE);
//                ll_bt_task.setVisibility(View.GONE);
//                bt_task_huifu.setVisibility(View.GONE);
//            }

        } else if ("已完成".equals(status)) {
            et_task_name.setVisibility(View.GONE);
            ll_bt_task.setVisibility(View.GONE);
            bt_task_huifu.setVisibility(View.GONE);
        }
        //加载任务回复内容
        loadDataMsgTask(ra_taskid);
    }

    private boolean getIsHandle(String str) {
        if (str == null) return false;
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        //在未知情况下出现获取不到的情况
        emname = (StringUtil.isEmpty(emname) ? MyApplication.getInstance().mLoginUser.getNickName() : emname);
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(emname);
        return m.find();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_task_huifu:
                Map<String, String> param = new HashMap<String, String>();
                if (!StringUtil.isEmpty(et_task_name.getText().toString())) {
                    String formStore = "{\n" +
                            "\"id\":\"" + taskId + "\",\n" +//单据id
                            "\"status\":\"待确认\",\n" +//状态
                            "\"statuscode\":\"unconfirmed\"\n" +//状态码
                            "}";
                    String gridStore = "{\n" +
                            "\"taskid\":\"" + taskId + "\",\n" +//单据id
//                            "\"replyman\":\"" + CommonUtil.getSharedPreferences(this, "b2b_uu") + "\",\n" +//个人uu
                            "\"replyman\":\"" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname") + "\",\n" +//个人名字
                            "\"Replytime\":\"" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS) + "\",\n" +
                            "\"Replydetail\":\"" + et_task_name.getText().toString().trim() + "\",\n" +//回复内容
                            "\"uu\":\"" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu() + "\"\n" +//企业uu enUU
                            "}";
                    param.put("formStore", formStore);
                    param.put("gridStore", gridStore);
//                    sendDataToServer("plm/record/endBillTask.action", param);
                    sendDataToB2BServer(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_change, param);
                } else {
                    ViewUtil.ShowMessageTitle(ct, getString(R.string.task_input_detail_info));
                }
                break;
            case R.id.bt_task_bohui:
                if (!StringUtil.isEmpty(et_task_name.getText().toString())) {
                    param = new HashMap<String, String>();
                    String formStore = "{\n" +
                            "\"id\":\"" + taskId + "\",\n" +//单据id
                            "\"status\":\"进行中\",\n" +//状态
                            "\"statuscode\":\"start\"\n" +//状态码
                            "}";
                    String gridStore = "{\n" +
                            "\"taskid\":\"" + taskId + "\",\n" +//单据id
//                            "\"replyman\":\"" + CommonUtil.getSharedPreferences(this, "b2b_uu") + "\",\n" +//个人uu
                            "\"replyman\":\"" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname") + "\",\n" +//个人名字
                            "\"Replytime\":\"" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS) + "\",\n" +
                            "\"Replydetail\":\"" + et_task_name.getText().toString().trim() + "\",\n" +//回复内容
                            "\"uu\":\"" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu() + "\"\n" +//企业uu enUU
                            "}";
                    param.put("formStore", formStore);
                    param.put("gridStore", gridStore);
//                    sendDataToServer("plm/record/endBillTask.action", param);
                    sendDataToB2BServer(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_change, param);
                } else {
                    ViewUtil.ShowMessageTitle(ct, getString(R.string.task_input_detail_info));
                }

                break;
            case R.id.bt_task_queren:
                if (!StringUtil.isEmpty(et_task_name.getText().toString())) {
                    param = new HashMap<String, String>();
                    String formStore = "{\n" +
                            "\"id\":\"" + taskId + "\",\n" +//单据id
                            "\"status\":\"已完成\",\n" +//状态
                            "\"statuscode\":\"done\"\n" +//状态码
                            "}";
                    String gridStore = "{\n" +
                            "\"taskid\":\"" + taskId + "\",\n" +//单据id
//                            "\"replyman\":\"" + CommonUtil.getSharedPreferences(this, "b2b_uu") + "\",\n" +//个人uu
                            "\"replyman\":\"" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname") + "\",\n" +//个人名字
                            "\"Replytime\":\"" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS) + "\",\n" +
                            "\"Replydetail\":\"" + et_task_name.getText().toString().trim() + "\",\n" +//回复内容
                            "\"uu\":\"" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu() + "\"\n" +//企业uu enUU
                            "}";
                    param.put("formStore", formStore);
                    param.put("gridStore", gridStore);
//                    sendDataToServer("plm/record/endBillTask.action", param);
                    sendDataToB2BServer(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_change, param);
                } else {
                    ViewUtil.ShowMessageTitle(ct, getString(R.string.task_input_detail_info));
                }
                break;
            case R.id.iv_recode:
                if (filepath != null) {
                    tv_voice_msg.setText("正在播放...");
                    playVolice(filepath);
                }
                break;
            default:
                break;
        }
    }

    public void sendDataToServer(String url, Map<String, String> param) {
//		progressDialog.show();
        url = CommonUtil.getAppBaseUrl(ct) + url;
        Log.i(TAG, url);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.startNetThread(ct, url, param, mhandler, Constants.LOAD_SUCCESS, null, null, "get");
    }

    public void sendDataToB2BServer(String url, Map<String, String> param) {
        param.put("sessionId", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.startNetThread(ct, url, param, mhandler, Constants.LOAD_SUCCESS, null, null, "post");
    }

    public void sendDataToServer(String url, Map<String, Object> param, int what) {
        param.put("sessionId", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, param, mhandler, headers, what, null, null, "get");
    }

    public void getAMRFileForServer(String url, Map<String, Object> param) {
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mhandler, headers, LOAD_FILE_SUCCESS, null, null, "get");
    }

    private MediaPlayer mMediaPlayer;

    public void download(final String url, final String path) {
        Log.i(TAG, "版本号：" + getAndroidSDKVersion());
        Log.i(TAG, "url path:" + url);
        Log.i(TAG, "SD path:" + path);
        if (getAndroidSDKVersion() > 19) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    filepath = HttpUtil.download(url, path);
                }
            }).start();
        } else {
            // 判断版本号
            HttpUtils http = new HttpUtils();
            http.download(url, path, false, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                    false, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                    new RequestCallBack<File>() {

                        @Override
                        public void onStart() {
                            ViewUtil.ToastMessage(ct, "开始下载");
                        }

                        @Override
                        public void onLoading(long total, long current, boolean isUploading) {
                        }

                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            ViewUtil.ToastMessage(ct, "下载成功");
                            filepath = responseInfo.result.getAbsolutePath();

                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            ViewUtil.ToastMessage(ct, msg);
                            Log.i("result", msg);
                        }
                    });
        }

    }

    private String filepath;

    /**
     * @author Administrator
     * @功能:播放声音
     */
    public void playVolice(String path) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            // 添加录音的路径
            try {
                File file = new File(path);
                // final FileInputStream fis = new FileInputStream(file);
                mMediaPlayer.setDataSource(path);
                // 准备
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // 停止播放
                        if (mMediaPlayer != null) {
                            mMediaPlayer.stop();
                            mMediaPlayer = null;
                            tv_voice_msg.setText("语音");
                            ViewUtil.ToastMessage(ct, "播放完毕！");

                        }

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
                mMediaPlayer.stop();
                mMediaPlayer = null;
                tv_voice_msg.setText("语音");
            }
        }

    }

    private void downFileToSD(String sd_path, String file_url) {
        if (CommonUtil.isExitsSdcard()) {
            Log.i(TAG, file_url);
            String path = null;
            try {
                path = Environment.getExternalStorageDirectory().getCanonicalFile() + sd_path;
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(path);
            if (file.isFile() && file != null) {
                filepath = path;
            } else {
                download(file_url, path);
            }

        } else {
            ViewUtil.ShowMessageTitle(ct, "没有插入SD卡");
        }
    }

    ;

    public int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }

    final int LOAD_SUCCESS_MSGTASK = 8;

    /**
     * @author Administrator
     * @功能:获取信息任务
     */
    public void loadDataMsgTask(String ra_id) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().task_reply;
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", ra_id);
        sendDataToServer(url, param, LOAD_SUCCESS_MSGTASK);
    }


    private void goBackMenu() {
        mhandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                onBackPressed();
            }
        }, 800);
    }


    public class SimpleAdapter extends BaseAdapter {

        private JSONArray jsonArray;
        private LayoutInflater inflater;


        public SimpleAdapter(Context ct, JSONArray data) {
            this.inflater = LayoutInflater.from(ct);
            this.jsonArray = data;
        }


        @Override
        public int getCount() {
            return jsonArray == null ? 0 : jsonArray.size();
        }

        @Override
        public Object getItem(int position) {
            return jsonArray == null ? null : jsonArray.getJSONArray(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewModel model = null;
            if (view == null) {
                view = inflater.inflate(R.layout.items_taskmsg_card, parent, false);
                model = new ViewModel();
                model.template_man = (TextView) view.findViewById(R.id.tv_task_man);
                model.template_time = (TextView) view.findViewById(R.id.tv_task_time);
                model.template_content = (TextView) view.findViewById(R.id.tv_task_content);
                view.setTag(model);
            } else {
                model = (ViewModel) view.getTag();
            }

            model.template_man.setText(jsonArray.getJSONObject(position).getString("replyman"));
            if (!TextUtils.isEmpty(jsonArray.getJSONObject(position).getString("replytime"))
                    && !"null".equals(jsonArray.getJSONObject(position).getString("replytime"))) {
                model.template_time.setText(DateFormatUtil.long2Str(Long.parseLong(jsonArray.getJSONObject(position).getString("replytime")), DateFormatUtil.YMD_HMS));
            }
            model.template_content.setText(jsonArray.getJSONObject(position).getString("replydetail"));

            return view;
        }

        class ViewModel {
            TextView template_man;
            TextView template_time;
            TextView template_content;
        }

    }
}
