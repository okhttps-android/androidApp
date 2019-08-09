package com.xzjmyk.pm.activity.ui.circle.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.widget.MediaController;
import android.widget.VideoView;

import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.bean.circle.PublicMessage;

public class DMsgVideoHeaderView extends PMsgTypeView {

    private ContentLoadingProgressBar progress;
    private final MediaController mediaController;
    private final VideoView videoVV;

    public DMsgVideoHeaderView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.header_view_video, this);
        videoVV = (VideoView) findViewById(R.id.videoVV);
        progress = (ContentLoadingProgressBar) findViewById(R.id.progress);
        mediaController = new MediaController(context);
        mediaController.setMediaPlayer(videoVV);
        videoVV.setMediaController(mediaController);
        mediaController.setEnabled(false);
        videoVV.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progress.setVisibility(GONE);
            }
        });
        videoVV.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                progress.setVisibility(GONE);
                return false;
            }
        });
        videoVV.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoVV.resume();
            }
        });

    }

    @Override
    public void attachPublicMessage(PublicMessage message) {
        videoVV.setVideoURI(Uri.parse(message.getFirstVideo()));
        videoVV.start();
    }

    @Override
    public void onPause() {
        videoVV.pause();
    }

    @Override
    public void onResume() {
        videoVV.resume();
    }

    @Override
    public void onDestory() {

    }

    @SuppressWarnings("deprecation")
    public void setLandscapeMode() {
//        int screenwidth = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
//        int screenheight = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
//        setLayoutParams(new AbsListView.LayoutParams(screenwidth, screenheight));
    }

    public void setPortraitMode() {// 设置动态高度为255dp
//        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(
//                R.dimen.pmsg_media_view_height)));
    }

    public void showHide() {
    }
}
