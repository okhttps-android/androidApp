package com.usoftchina.music;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.common.LogUtil;

/**
 * Created by Arison on 2018/6/4.
 */

public class MusicService extends Service {
    public MediaPlayer mediaPlayer;
    public boolean tag = false;
    
    private static final String TAG = "MusicService";

    public MusicService() {
    Log.i(TAG, "MusicService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG,"onCreate");
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return flags;
    }

    private void initMusic() {
        Log.i(TAG, "onStartCommand");
        try {
//            mediaPlayer =  MediaPlayer.create(this, R.raw.yingtang);
//            mediaPlayer.prepare();
//            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
     
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        public  MusicService getService() {
            Log.i(TAG, "getService");
            initMusic();
            return MusicService.this;
        }
    }

    public void playOrPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.i(TAG, "pause()");
        } else {
            Log.i(TAG, "start()");
            mediaPlayer.start();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.reset();
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
