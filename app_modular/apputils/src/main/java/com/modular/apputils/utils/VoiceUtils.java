package com.modular.apputils.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.common.preferences.PreferenceUtils;
import com.core.app.MyApplication;

public class VoiceUtils {

    public static void signVoice(int rawId) {
        if (PreferenceUtils.getBoolean("signVoiceAble", true)) {
//            Uri notification = Uri.parse("android.resource://"+ MyApplication.getInstance().getPackageName()+"/"+ R.raw.voice_sign);
            Uri notification = Uri.parse("android.resource://" + MyApplication.getInstance().getPackageName() + "/" + rawId);
            Ringtone r = RingtoneManager.getRingtone(MyApplication.getInstance().getApplicationContext(), notification);
            r.play();
        }
    }


    //获取多媒体最大音量
    public static int getMediaMaxVolume() {
        return getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //获取多媒体音量
    public static int getMediaVolume() {
        return getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    //获取通话最大音量
    public static int getCallMaxVolume() {
        return getAudioManager().getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
    }

    public static void setMediaVolume(int volume) {
        getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, //音量类型
                volume,
                AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
    }
    // 关闭/打开扬声器播放
//    public void setSpeakerStatus(boolean on) {
//        if (on) { //扬声器
//            mAudioManager.setSpeakerphoneOn(true);
//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
//        } else {
//            // 设置最大音量
//            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
//            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, max, AudioManager.STREAM_VOICE_CALL);
//            // 设置成听筒模式
//            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            mAudioManager.setSpeakerphoneOn(false);// 关闭扬声器
//            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
//        }
//    }


    private static AudioManager getAudioManager() {
        return (AudioManager) MyApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
    }
}
