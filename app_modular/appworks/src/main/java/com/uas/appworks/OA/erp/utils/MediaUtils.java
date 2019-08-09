package com.uas.appworks.OA.erp.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.uas.appworks.R;

import java.io.IOException;

/**
 * Created by Bitlike on 2018/4/25.
 */

public class MediaUtils {
    //TODO 打卡成功
    public static void saveLogOk(Context ct) {
        if (1 == 1) return;
        Uri notification = Uri.parse("android.resource://" + ct.getPackageName() + "/" + R.raw.rec);
        Ringtone r = RingtoneManager.getRingtone(ct.getApplicationContext(), notification);
        r.play();

    }

    /**
     * 播放语音
     *
     * @param path
     */
    public static void playSound(String path) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
