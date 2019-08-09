package com.uas.appworks.datainquiry.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.data.JSONUtil;
import com.core.base.BaseActivity;
import com.core.net.ProgressDownloader;
import com.core.net.ProgressResponseBody;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.crouton.Crouton;
import com.core.widget.crouton.Style;
import com.tencent.smtt.sdk.TbsReaderView;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.uas.appworks.datainquiry.Constants.CONSTANT.PDF_FILE_NAME;
import static com.uas.appworks.datainquiry.Constants.CONSTANT.PDF_FILE_PATH;

/**
 * Created by RaoMeng on 2017/8/17.
 * 报表统计PDF文件下载并展示页面
 */
public class PDFDownloadActivity extends BaseActivity implements View.OnClickListener, ProgressResponseBody.ProgressListener, TbsReaderView.ReaderCallback {
    private final int OBATIN_PDF_PATH = 100;

    private RelativeLayout mRootLayout,mDownloadRelativeLayout;
    private TextView mStateTextView, mRemainTextView, mRedownloadTextView, mExitTextView, mPauseTextView, mLoadTextView;
    private ProgressBar mDownloadProgressBar;
    private LinearLayout mErrorLinearLayout;
    private TbsReaderView mTbsReaderView;

    private String downloadUrl = "", mReportName = "", mCondition, mTitle, replace;
    private ProgressDownloader mDownloader;
    private long breakPoints, contentLength, totalBytes;
    private File file;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.CONSTANT.DOWNLOAD_PROGRESS:
                    try {
                        int progress = (int) msg.obj;
                        Log.d("progress", progress + ":" + contentLength);
                        mStateTextView.setText(R.string.str_downloading);
                        mDownloadProgressBar.setProgress(progress);
                        long remain = 100 - (((progress * 1024 * 100) / contentLength));
                        if (remain < 0) {
                            remain = 0;
                        }
                        mRemainTextView.setText(remain + "");
                    } catch (Exception e) {

                    }
                    break;
                case Constants.CONSTANT.DOWNLOAD_SUCCESS:
                    mPauseTextView.setVisibility(View.GONE);
                    mStateTextView.setText("报表文件生成预览成功!");
                    mRemainTextView.setText("0");
                    displayFromFile(new File(PDF_FILE_PATH, PDF_FILE_NAME));
                    break;
                case Constants.CONSTANT.PDF_OVERLOAD:
                    mPauseTextView.setVisibility(View.GONE);
                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                    mStateTextView.setText("数据量过载，报表文件获取失败！");
                    mDownloadProgressBar.setProgress(0);
                    mRemainTextView.setText(100 + "");
                    break;
                case OBATIN_PDF_PATH:
                    String result = msg.getData().getString("result");
                    analysisPdfPath(result);
                    break;
                case Constants.CONSTANT.DOWNLOAD_FAILED:
                    mPauseTextView.setVisibility(View.GONE);
                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                    String info = "";
                    if (msg.obj != null) {
                        info = "\n" + (String) msg.obj;
                    }
                    mStateTextView.setText("报表文件预览失败" + info);
                    mDownloadProgressBar.setProgress(0);
                    mRemainTextView.setText(100 + "");
                    break;
                case com.core.app.Constants.APP_SOCKETIMEOUTEXCEPTION:
                    result = msg.getData().getString("response");
                    String exception = "系统错误";
                    if (JSONUtil.validate(result)) {
                        if (JSON.parse(result) instanceof com.alibaba.fastjson.JSONObject) {
                            exception = JSON.parseObject(result).getString("exceptionInfo");
                        }
                    }
                    if (TextUtils.isEmpty(exception)) {
                        analysisPdfPath(result);
                    } else {
                        mPauseTextView.setVisibility(View.GONE);
                        mErrorLinearLayout.setVisibility(View.VISIBLE);
                        mStateTextView.setText(R.string.data_exception);
                        mDownloadProgressBar.setProgress(0);
                        mRemainTextView.setText(100 + "");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_download);

        initViews();
        initEvents();
        obtainPdfPath();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            //设置全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
                //取消全屏
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    private void obtainPdfPath() {
        mStateTextView.setText(R.string.obtain_pdf_path);
        mDownloadProgressBar.setProgress(0);
        mRemainTextView.setText(100 + "");

        String url = replace + "report/pdf/path";
        Map<String, Object> params = new HashMap<>();
        params.put("u", CommonUtil.getSharedPreferences(ct, "erp_master"));
        params.put("pf", "phone");
        params.put("r", mReportName);
        params.put("w", "where " + mCondition);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, mHandler, headers, OBATIN_PDF_PATH, null, null, "get");
    }

    private void initViews() {
        Intent intent = getIntent();
        if (intent != null) {
            mCondition = intent.getStringExtra("condition");
            mReportName = intent.getStringExtra("reportName");
            mTitle = intent.getStringExtra("title");
        }

        if (mTitle != null && getSupportActionBar() != null) {
            setTitle(mTitle);
        }

        try {
            replace = CommonUtil.getIP(URI.create(CommonUtil.getReportUrl(this))).toString() + "/";
//            replace = "http://print.ubtob.com/";
        } catch (Exception e) {
            try {
                String appBaseUrl = CommonUtil.getReportUrl(this);
                replace = appBaseUrl.substring(0, appBaseUrl.length() - 1);
                replace = replace.substring(0, replace.lastIndexOf("/") + 1);
            } catch (Exception exception) {

            }
        }

        mStateTextView = (TextView) findViewById(R.id.pdf_download_state_tv);
        mRemainTextView = (TextView) findViewById(R.id.pdf_download_remain_tv);
        mDownloadProgressBar = (ProgressBar) findViewById(R.id.pdf_download_progress_pb);
        mDownloadRelativeLayout = (RelativeLayout) findViewById(R.id.pdf_download_progress_rl);
        mRedownloadTextView = (TextView) findViewById(R.id.pdf_download_redownload_tv);
        mExitTextView = (TextView) findViewById(R.id.pdf_download_exit_tv);
        mErrorLinearLayout = (LinearLayout) findViewById(R.id.pdf_download_error_menu_ll);
        mPauseTextView = (TextView) findViewById(R.id.pdf_download_pause_tv);
        mLoadTextView = (TextView) findViewById(R.id.pdf_download_load_tv);
        mRootLayout = (RelativeLayout) findViewById(R.id.pdf_download_rl);
        mTbsReaderView = new TbsReaderView(this, this);
        mRootLayout.addView(mTbsReaderView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void initEvents() {
        mExitTextView.setOnClickListener(this);
        mRedownloadTextView.setOnClickListener(this);
        mDownloadRelativeLayout.setOnClickListener(this);
        mPauseTextView.setOnClickListener(this);
        mLoadTextView.setOnClickListener(this);
    }


    private void analysisPdfPath(String result) {
        if (result != null) {
            try {
                JSONObject responseObject = new JSONObject(result);
                if (!responseObject.isNull("success") && !responseObject.optBoolean("success")) {
                    Message message = Message.obtain();
                    message.what = Constants.CONSTANT.DOWNLOAD_FAILED;
                    message.obj = responseObject.optString("message");
                    mHandler.sendMessage(message);
                } else {
                    boolean overload = responseObject.optBoolean("overload");
                    int pageSize = -1;
                    if (!responseObject.isNull("pageSize")) {
                        pageSize = responseObject.optInt("pageSize");
                    }
                    if (overload) {
                        mHandler.sendEmptyMessage(Constants.CONSTANT.PDF_OVERLOAD);
                    } else if (pageSize == 0) {
                        Message message = Message.obtain();
                        message.what = Constants.CONSTANT.DOWNLOAD_FAILED;
                        message.obj = "报表文件内容为空";
                        mHandler.sendMessage(message);
                    } else {
                        downloadUrl = responseObject.optString("path");
                        if (!TextUtils.isEmpty(downloadUrl)) {
                            downloadUrl = replace + "report/" + downloadUrl;
                            downloadPdf();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadPdf() {
        mPauseTextView.setVisibility(View.VISIBLE);
        breakPoints = 0L;
        File directory = new File(PDF_FILE_PATH);
        if (!directory.exists() && !directory.isDirectory()) {
            boolean mkdirs = directory.mkdirs();

        } else {
            CommonUtil.delAllFile(PDF_FILE_PATH);
        }
        file = new File(PDF_FILE_PATH, PDF_FILE_NAME);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("pdfurl", downloadUrl == null ? "" : downloadUrl);
        mDownloader = new ProgressDownloader(downloadUrl, file, PDFDownloadActivity.this);
        mDownloader.download(0L);
    }

    private void displayFromFile(File fileName) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = new Bundle();
                    bundle.putString("filePath", PDF_FILE_PATH + "/" + PDF_FILE_NAME);
                    bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());

                    readPdf(bundle, 3);
                }
            }, 400);
        } catch (Exception e) {
            Bundle bundle = new Bundle();
            bundle.putString("filePath", PDF_FILE_PATH + "/" + PDF_FILE_NAME);
            bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());

            readPdf(bundle, 3);
        }
    }

    private void readPdf(Bundle bundle, int repeat) {
        try {
            boolean result = mTbsReaderView.preOpen("pdf", false);
            if (result) {
                mDownloadRelativeLayout.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                Crouton.makeText((Activity) mContext, getString(R.string.revolving_mobile_phone_full_screen_browsing), Style.holoGreenLight, 1500).show();

                mTbsReaderView.openFile(bundle);
            } else {
                repeat--;
                if (repeat >= 0) {
                    readPdf(bundle, repeat);
                } else {
                    displayfail();
                }
            }
        } catch (Exception e) {
            displayfail();
        }
    }

    private void displayfail() {
        mPauseTextView.setVisibility(View.GONE);
        mErrorLinearLayout.setVisibility(View.VISIBLE);
        mLoadTextView.setVisibility(View.VISIBLE);
        mStateTextView.setText("报表文件加载失败！");
        mDownloadProgressBar.setProgress(0);
        mRemainTextView.setText(100 + "");
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.pdf_download_exit_tv) {
            onBackPressed();
        } else if (i == R.id.pdf_download_redownload_tv) {
            mStateTextView.setText(R.string.str_downloading);
            mErrorLinearLayout.setVisibility(View.GONE);
            mDownloadProgressBar.setProgress(0);
            mRemainTextView.setText(100 + "");
            if (TextUtils.isEmpty(downloadUrl)) {
                obtainPdfPath();
            } else {
                downloadPdf();
            }
        } else if (i == R.id.pdf_download_pause_tv) {
            if (getString(R.string.pause_download).equals(mPauseTextView.getText().toString())) {
                mPauseTextView.setText(R.string.continue_download);
                mStateTextView.setText(R.string.download_paused);
                mDownloader.pause();
                ToastUtil.showToast(this, "预览暂停");
                // 存储此时的totalBytes，即断点位置。
                breakPoints = totalBytes;
            } else if (getString(R.string.continue_download).equals(mPauseTextView.getText().toString())) {
                mPauseTextView.setText(R.string.pause_download);
                mStateTextView.setText(R.string.str_downloading);
                ToastUtil.showToast(this, "预览继续");
                mDownloader.download(breakPoints);
            }
        } else if (i == R.id.pdf_download_load_tv) {
            mErrorLinearLayout.setVisibility(View.GONE);
            mLoadTextView.setVisibility(View.GONE);
            mStateTextView.setText(R.string.str_reloading);
            mDownloadProgressBar.setProgress((int) (contentLength / 1024));
            mRemainTextView.setText(0 + "");
            displayFromFile(new File(PDF_FILE_PATH, PDF_FILE_NAME));
        }
    }

    @Override
    public void onPreExecute(long contentLength) {
        // 文件总长只需记录一次，要注意断点续传后的contentLength只是剩余部分的长度
        if (this.contentLength == 0L) {
            this.contentLength = contentLength;
            mDownloadProgressBar.setMax((int) (contentLength / 1024));
        }
    }

    @Override
    public void update(long totalBytes, boolean done) {
        // 注意加上断点的长度
        this.totalBytes = totalBytes + breakPoints;
        int progress = (int) (totalBytes + breakPoints) / 1024;
        Message message = Message.obtain();
        message.what = Constants.CONSTANT.DOWNLOAD_PROGRESS;
        message.obj = progress;
        mHandler.sendMessage(message);
        if (done) {
            mHandler.sendEmptyMessage(Constants.CONSTANT.DOWNLOAD_SUCCESS);
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {
        Log.e("tbscallback", "-->" + integer + "-->" + o.toString() + "-->" + o1.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
    }
}
