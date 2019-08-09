package com.xzjmyk.pm.activity.video;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.common.LogUtil;
import com.common.file.CacheFileUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.xzjmyk.pm.activity.R;

import java.io.File;
import java.io.IOException;


/**
 * Created by gongpm on 2016/6/8.
 */
public class VideoActivity extends BaseActivity implements MediaRecorder.OnErrorListener {
    private Camera mCamera;
    private TextView tvTime;
    private Button btn;
    private CountDownTimer countDownTimer;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private File mVecordFile;
    private int timeLen = 0;  //时间长度
    public static final String EXTRA_RESULT_FILE_PATH = "result_file_path";
    public static final String EXTRA_RESULT_TIME_LEN = "result_time_len";
    public static final String EXTRA_RESULT_FILE_SIZE = "result_file_size";
    private int MAX_NUM = 30;
    private int SPEED = 1;
    private String filePath;
    private boolean isDoing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        surfaceView = (SurfaceView) findViewById(R.id.lib_surface_view);
        tvTime = (TextView) findViewById(R.id.lib_video_tv_time);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isDoing)return true;
                    requestPermission(Manifest.permission.CAMERA, new Runnable() {
                        @Override
                        public void run() {
                            isDoing = true;
                            beginHandler(MAX_NUM, SPEED);
                            record();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(ct,R.string.not_system_permission);  
                        }
                    });
                    
          
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!isDoing) return false;
                    countDownTimer.cancel();
                    tvTime.setText("录制完成");
                    stop();
                }
                return false;
            }
        });
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());

    }

    private void doTheEnd() {
        //TODO 拍摄成功后最后操作
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_FILE_PATH, filePath);
        intent.putExtra(EXTRA_RESULT_TIME_LEN, (MAX_NUM - timeLen));
        intent.putExtra(EXTRA_RESULT_FILE_SIZE, mVecordFile.length());
        setResult(RESULT_OK, intent);
        finish();
    }


    public void beginHandler(final int max_num, int speed) {
        countDownTimer = new CountDownTimer(max_num * 1000, speed * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLen = (int) (millisUntilFinished / 1000);
                tvTime.setText(getString(R.string.count_down) + timeLen);
            }

            @Override
            public void onFinish() {
                tvTime.setText(R.string.recording_completed);
                stop();
            }
        };
        countDownTimer.start();
    }

    /**
     * 初始化摄像头
     */
    private void initCamera() throws IOException {
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = Camera.open();
        } catch (Exception e) {

            freeCameraResource();
            Toast.makeText(MyApplication.getInstance(), "权限未开启，请检查", Toast.LENGTH_SHORT);
            finish();
        }
        if (mCamera == null)
            return;
        setCameraParams();
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();
    }

    /**
     * 设置摄像头为竖屏
     */
    private void setCameraParams() {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.set("orientation", "portrait");
            mCamera.setParameters(params);
        }
    }




    /**
     * 保存的位置处理
     */
    private void createRecordDir() {
        //录制的视频保存文件夹
//        File sampleDir = new File(Environment.getExternalStorageDirectory()
//                + File.separator + "ysb/video/");//录制视频的保存地址
//        if (!sampleDir.exists()) {
//            sampleDir.mkdirs();
//        }
//        File vecordDir = sampleDir;
        filePath = CacheFileUtil.getRandomVideoFilePath(this, MyApplication.getInstance().getLoginUserId());
        // 创建文件
        // mp4格式的录制的视频文件
//            mVecordFile = File.createTempFile("recording", ".mp4", vecordDir);
        mVecordFile = new File(filePath);
    }

    /**
     * 开始录制视频
     * 视频储存位置
     */
    public void record() {
        createRecordDir();
        try {
            initCamera();
            if (initRecord() == true) {
                mMediaRecorder.start();
            } else {
                releaseRecord();
            }
        } catch (IOException e) {

        } catch (Exception e) {

        }
    }

    /**
     * 停止拍摄
     */
    public void stop() {
        stopRecord();
        releaseRecord();
        freeCameraResource();
        doTheEnd();

    }


    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            } catch (IllegalStateException e) {

            } catch (RuntimeException e) {

            } catch (Exception e) {

            }
        }
    }

    /**
     * 释放资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder = null;
            if (mCamera != null)
                mCamera.lock();
        }
    }

    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 初始化
     *
     * @throws IOException
     */
    @SuppressLint("NewApi")
    private boolean initRecord() throws IOException {
        mMediaRecorder = new MediaRecorder();
        if (mCamera == null) {
            return false;
        }
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        CamcorderProfile highCameraProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        mMediaRecorder.setProfile(highCameraProfile);
        mMediaRecorder.setMaxDuration(100000); // MAXDuration 10 seconds
        mMediaRecorder.setMaxFileSize(20000000); // MAXSIZE 20 megabytes
        mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
        if (mVecordFile == null) {
            createRecordDir();
        }
        mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {

            return false;
        } catch (IOException e) {

            return false;
        }
        return true;
    }


    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {

        } catch (Exception e) {

        }
    }


    /**
     * 视频拍摄回调
     */
    class CustomCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                initCamera();
            } catch (IOException e) {

            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            freeCameraResource();
        }
    }
}
