package com.module.recyclerlibrary.ui.refresh.simlpe;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.module.recyclerlibrary.R;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshView;


/**
 * Created by Bitliker on 2017/9/18.
 */

public class SimpleRefreshHeader extends BaseRefreshView {
    private TextView statusTV;
    private ImageView statusImg;

    public SimpleRefreshHeader(Context context, LayoutInflater mInflater) {
        super(context, mInflater);
        mInflater.inflate(R.layout.refresh_header, this);
        statusImg = (ImageView) findViewById(R.id.statusImg);
        statusTV = (TextView) findViewById(R.id.statusTV);

    }
    public void hintAll(){
        statusImg.setVisibility(GONE);
        statusTV.setVisibility(GONE);
    }


    @Override
    public void startAnim() {
        statusTV.setText(R.string.refreshing);
        statusImg.setImageResource(R.drawable.progress_round);
        AnimationDrawable animationDrawable = (AnimationDrawable) statusImg.getDrawable();
        animationDrawable.start();
    }

    @Override
    public void stopAnim() {
        statusImg.setImageResource(R.drawable.pull_down);
        statusTV.setText(R.string.pull_down_to_refresh);
    }

    @Override
    public void upStatus(int status) {
        switch (status) {
            case TRY_REFRESH:
                statusTV.setText(R.string.pull_down_to_refresh);
                statusImg.setImageResource(R.drawable.pull_down);
                break;
            case REFRESH:
                statusTV.setText(R.string.up_to_refresh);
                statusImg.setImageResource(R.drawable.pull_up);
                break;
        }
    }
}
