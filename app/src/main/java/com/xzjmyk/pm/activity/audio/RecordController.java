package com.xzjmyk.pm.activity.audio;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.xzjmyk.pm.im.audio.MP3Recorder;

public class RecordController implements View.OnTouchListener,RecordStateListener {
	private final int UP_MOVE_CHECK_NUM = 80;
	private Context mContext;
	private RecordPopWindow mRecordPopWindow;
	private long mLastTouchUpTime = System.currentTimeMillis();
	private MP3Recorder mRecordManager;
	private int mLastY = 0;
	private int timeLen;

	public RecordController(Context context) {
		mContext = context;
		mRecordPopWindow = new RecordPopWindow(mContext);
		mRecordManager = MP3Recorder.getInstance();
		mRecordManager.setRecordStateListener(this);
	}

	private boolean canVoice() {
		long now = System.currentTimeMillis();
		return now - mLastTouchUpTime > 500;
	}

	private RecordListener mRecordListener;

	public void setRecordListener(RecordListener listener) {
		mRecordListener = listener;
	}

	/**
	 * 判断是否在上滑
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean upMove(int y) {
		if ((mLastY - y) > UP_MOVE_CHECK_NUM) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mLastY = (int) event.getY();
			if (canVoice() && !mRecordManager.isRecording()) {
				if (mRecordListener != null) {
					mRecordListener.onRecordStart();
				}
				mRecordPopWindow.startRecord();
				mRecordManager.start();
				final MotionEvent ev = event;
				ev.setAction(MotionEvent.ACTION_MOVE);
				v.dispatchTouchEvent(ev);
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (mRecordManager.isRecording()) {
				if (!mRecordPopWindow.isRubishVoiceImgShow()) {
					if (upMove((int) event.getY())) {
						mRecordPopWindow.setRubishTip();
					}
				} else {
					if (!upMove((int) event.getY())) {
						mRecordPopWindow.hideRubishTip();
					}
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			mLastY = 0;
			if(mRecordManager.isRecording()){
				mLastTouchUpTime = System.currentTimeMillis();
			}
			if (mRecordPopWindow.isRubishVoiceImgShow()) {
				mRecordManager.cancel();
			} else {
				mRecordManager.stop();
			}
			
		}
		return true;
	}

	@Override
	public void onRecordStarting() {
		mRecordPopWindow.show();
	}

	@Override
	public void onRecordStart() {

	}

	@Override
	public void onRecordFinish(String file) {
		mRecordPopWindow.dismiss();
		if (mRecordListener != null) {
			mRecordListener.onRecordSuccess(file, timeLen);
		}
	}

	@Override
	public void onRecordCancel() {
		mRecordPopWindow.dismiss();
		if (mRecordListener != null) {
			mRecordListener.onRecordCancel();
		}
	}

	@Override
	public void onRecordVolumeChange(int v) {
		mRecordPopWindow.setVoicePercent(v);
	}


	@Override
	public void onRecordTimeChange(int seconds) {
		mRecordPopWindow.setVoiceSecond(seconds);
		if (seconds > 60) {
			timeLen = 60;
			mRecordManager.stop();
		} else {
			timeLen = seconds;
		}
	}

	@Override
	public void onRecordError() {
		mRecordPopWindow.dismiss();
		mRecordListener.onRecordCancel();
		Toast.makeText(mContext, "录音出错啦！", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRecordTooShoot() {
		mRecordPopWindow.dismiss();
		mRecordListener.onRecordCancel();
		Toast.makeText(mContext, "录音时间太短啦！", Toast.LENGTH_SHORT).show();
	}

	public void cancel() {
		if (mRecordPopWindow != null) {
			mRecordPopWindow.dismiss();
		}
		if (mRecordManager != null) {
			mRecordManager.cancel();
		}
	}

}
