package com.usoftchina.pay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.LogUtil;
import com.core.utils.StatusBarUtil;
import com.lg.lrcview_master.DefaultLrcParser;
import com.lg.lrcview_master.LrcRow;
import com.lg.lrcview_master.LrcView.OnLrcClickListener;
import com.lg.lrcview_master.LrcView.OnSeekToListener;
import com.usoftchina.music.MusicService;
import com.usoftchina.music.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainMusicActivity extends AppCompatActivity {
	private MediaPlayer mPlayer;
	private SeekBar mPlayerSeekBar;
	private SeekBar mLrcSeekBar;
	private Button mPlayBtn;
	private com.lg.lrcview_master.LrcView mLrcView;
	private	TextView tv_timeStart;
	private	TextView tv_timeTotal;

	PhoneBroadcastReceiver phoneBroadcastReceiver;

	private Toast mPlayerToast;
	private Toast mLrcToast;
	private Toolbar toolbar;
	private MusicService musicService;
    private static final String TAG = "MainMusicActivity";
	//  在Activity中调用 bindService 保持与 Service 的通信
	private void bindServiceConnection() {
		LogUtil.d(TAG,"bindServiceConnection()");
		Intent intent = new Intent(MainMusicActivity.this, MusicService.class);
//		startService(intent);
		bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
	}
	
	//  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.d(TAG,"onServiceConnected()");
			musicService = ((MusicService.MyBinder) (service)).getService();
			LogUtil.d(TAG,"musicService"+musicService.mediaPlayer);
			musicService.mediaPlayer.setOnCompletionListener(onCompletionListener);
			if (!musicService.mediaPlayer.isPlaying()){
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						float scalingFactor = com.lg.lrcview_master.LrcView.MIN_SCALING_FACTOR + 0*(com.lg.lrcview_master.LrcView.MAX_SCALING_FACTOR- com.lg.lrcview_master.LrcView.MIN_SCALING_FACTOR)/100;
						LogUtil.d("MusicApp","scalingFactor:"+scalingFactor+"");
						mLrcView.setLrcScalingFactor(scalingFactor);

						musicService.playOrPause();
						mLrcView.setLrcRows(getLrcRows());
						handler.sendEmptyMessage(0);
						mPlayBtn.setText("暂停");
						mPlayBtn.setSelected(true);
					}
				},1000);
			}
			//musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicService = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(TAG,"OnCreate()");
		setContentView(R.layout.activity_main_music);
		initViews();
		initPlayer();
		
		phoneBroadcastReceiver=new PhoneBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		filter.addAction("android.intent.action.PHONE_STATE");
		registerReceiver(phoneBroadcastReceiver, filter);

		toolbar=findViewById(R.id.commonToolBar);
		StatusBarUtil.immersive(this, 0x00000000, 0.0f);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		StatusBarUtil.setPaddingSmart(this, toolbar);
		toolbar.setNavigationIcon(R.drawable.back_black);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
//			   onBackPressed();
				moveTaskToBack(true);
			}
		});
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"musicService:"+musicService);
		if (musicService!=null){
			Log.d(TAG,"musicService mediaPlayer:"+musicService.mediaPlayer);
		}
	}

	private void initViews() {
		mLrcView = (com.lg.lrcview_master.LrcView) findViewById(R.id.lrcView);
		mLrcView.setOnSeekToListener(onSeekToListener);
		mLrcView.setOnLrcClickListener(onLrcClickListener);
		tv_timeStart = findViewById(R.id.tv_timeStart);
		tv_timeTotal=findViewById(R.id.tv_timeTotal);
		mPlayerSeekBar = (SeekBar) findViewById(R.id.include_player_seekbar);
		mLrcSeekBar = (SeekBar) findViewById(R.id.include_lrc_seekbar);
		mLrcSeekBar.setMax(100);
		//为seekbar设置当前的progress
		mLrcSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		mPlayBtn = (Button) findViewById(R.id.btnPlay);
		mPlayerSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		mPlayBtn.setOnClickListener(onClickListener);
		//getCommonToolBar().setBackgroundResource(R.color.transparent);
		
	}

	
	private void initPlayer() {
//		mPlayer = MediaPlayer.create(this, R.raw.yingtang);
//		mPlayer.setOnCompletionListener(onCompletionListener);
		bindServiceConnection();
	}
	OnCompletionListener onCompletionListener = new OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			mPlayBtn.setText("play");
			//mPlayBtn.setSelected(false);
			mLrcView.reset();
			handler.removeMessages(0);
			mPlayerSeekBar.setProgress(0);

			musicService.mediaPlayer.start();
			mLrcView.setLrcRows(getLrcRows());
			handler.sendEmptyMessage(0);
			mPlayBtn.setText("暂停");
			mPlayBtn.setSelected(true);
		}
	};
	
	OnLrcClickListener onLrcClickListener = new OnLrcClickListener() {

		@Override
		public void onClick() {
			//Toast.makeText(getApplicationContext(), "歌词被点击啦", Toast.LENGTH_SHORT).show();
		}
	};
	OnSeekToListener onSeekToListener = new OnSeekToListener() {

		@Override
		public void onSeekTo(int progress) {
			musicService.mediaPlayer.seekTo(progress);
			
		}
	};
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			try {
				mPlayerSeekBar.setMax(musicService.mediaPlayer.getDuration());
				mPlayerSeekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
				handler.sendEmptyMessageDelayed(0, 100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	};
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(v == mPlayBtn){
				if("play".equals(mPlayBtn.getText())){
					musicService.playOrPause();
					mLrcView.setLrcRows(getLrcRows());
					handler.sendEmptyMessage(0);
					mPlayBtn.setText("暂停");
					mPlayBtn.setSelected(true);
				}else{
					if(musicService.mediaPlayer.isPlaying()){
						musicService.playOrPause();
						mPlayBtn.setText("播放");
						mPlayBtn.setSelected(false);
					}else{
						mPlayBtn.setSelected(true);
						musicService.playOrPause();
						mPlayBtn.setText("暂停");
					}
				}
			}
		}
	};

	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(seekBar == mPlayerSeekBar){
				musicService.mediaPlayer.seekTo(seekBar.getProgress());
				handler.sendEmptyMessageDelayed(0, 100);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if(seekBar == mPlayerSeekBar){
				handler.removeMessages(0);
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			try {
				if(seekBar == mPlayerSeekBar){
                    mLrcView.seekTo(progress, true,fromUser);
    //				Log.d("timeStr",mLrcView.getmLrcRows().get(0).getTime()+"");
                    Log.d("timeStrA",progress+"");
                    tv_timeStart.setText(formatTimeFromProgress(progress));
                    tv_timeTotal.setText(formatTimeFromProgress(musicService.mediaPlayer.getDuration()));
                    if(fromUser){
                        
                        showPlayerToast(formatTimeFromProgress(progress));
                    }
                }else if(seekBar == mLrcSeekBar){
                    float scalingFactor = com.lg.lrcview_master.LrcView.MIN_SCALING_FACTOR + progress*(com.lg.lrcview_master.LrcView.MAX_SCALING_FACTOR- com.lg.lrcview_master.LrcView.MIN_SCALING_FACTOR)/100;
                    mLrcView.setLrcScalingFactor(scalingFactor);
                    showLrcToast((int)(scalingFactor*100)+"%");
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};
	/**
	 * 将播放进度的毫米数转换成时间格式
	 * 如 3000 --> 00:03 
	 * @param progress
	 * @return
	 */
	private String formatTimeFromProgress(int progress){
		//总的秒数 
		int msecTotal = progress/1000;
		int min = msecTotal/60;
		int msec = msecTotal%60;
		String minStr = min < 10 ? "0"+min:""+min;
		String msecStr = msec < 10 ? "0"+msec:""+msec;
		return minStr+":"+msecStr;
	}
	/**
	 * 获取歌词List集合
	 * @return
	 */
	private List<LrcRow> getLrcRows(){
		List<LrcRow> rows = null;
		InputStream is = getResources().openRawResource(R.raw.yingtanglrc);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line ;
		StringBuffer sb = new StringBuffer();
		try {
			while((line = br.readLine()) != null){
				sb.append(line+"\n");
			}
			System.out.println(sb.toString());
			rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rows;
	}

	private TextView mPlayerToastTv;
	private void showPlayerToast(String text){
		if(mPlayerToast == null){
			mPlayerToast = new Toast(this);
			mPlayerToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
			mPlayerToast.setView(mPlayerToastTv);
			mPlayerToast.setDuration(Toast.LENGTH_SHORT);
		}
		mPlayerToastTv.setText(text);
		mPlayerToast.show();
	}
	private TextView mLrcToastTv;
	private void showLrcToast(String text){
		if(mLrcToast == null){
			mLrcToast = new Toast(this);
			mLrcToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
			mLrcToast.setView(mLrcToastTv);
			mLrcToast.setDuration(Toast.LENGTH_SHORT);
		}
		mLrcToastTv.setText(text);
		mLrcToast.show();
	}

	
		@Override
	protected void onDestroy() {
		super.onDestroy();
			closeMusic();
			if (musicService.mediaPlayer!=null){
				musicService.mediaPlayer.release();
				musicService.mediaPlayer=null;
			}
		
	}

	private void closeMusic() {
		try {
			handler.removeMessages(0);
			musicService.mediaPlayer.stop();
			mLrcView.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}


	class PhoneBroadcastReceiver   extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "onReceive-----广播");
			// 如果是拨打电话
			if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				Log.i(TAG, "拨打电话--------------");
				musicService.mediaPlayer.pause();
				mPlayBtn.setSelected(false);
				//拨打电话会优先,收到此广播. 再收到 android.intent.action.PHONE_STATE 的 TelephonyManager.CALL_STATE_OFFHOOK 状态广播;();
			} else {
			Log.i(TAG, "接听电话-------------");
				// 如果是来电
				TelephonyManager tManager = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				//电话的状态
				switch (tManager.getCallState()) {
					case TelephonyManager.CALL_STATE_RINGING:
						//等待接听状态
						Log.i(TAG, "等待接听状态------");
						//musicService.playOrPause();
						musicService.mediaPlayer.pause();
						mPlayBtn.setSelected(false);
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						//接听状态
						musicService.mediaPlayer.pause();
						mPlayBtn.setSelected(false);
						Log.i(TAG, "接听状态-------");
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						Log.i(TAG, "挂断状态-------");
						//挂断状态
						musicService.mediaPlayer.pause();
						mPlayBtn.setSelected(false);
						break;
				}
			}
		}
	}
}
