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

public class SimpleRefreshFooter extends BaseRefreshView {
    private TextView statusTV;
    private ImageView statusImg;

    public SimpleRefreshFooter(Context context, LayoutInflater mInflater) {
        super(context, mInflater);
        mInflater.inflate(R.layout.refresh_footer, this);
        statusImg = (ImageView) findViewById(R.id.statusImg);
        statusTV = (TextView) findViewById(R.id.statusTV);
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
        statusImg.setImageResource(R.drawable.pull_up);
        statusTV.setText(R.string.pull_up_to_load);
    }

    @Override
    public void upStatus(int status) {
        switch (status) {
            case TRY_LOAD_MORE:
                statusTV.setText(R.string.pull_up_to_load);
                statusImg.setImageResource(R.drawable.pull_up);
                break;
            case LOAD_MORE:
                statusTV.setText(R.string.up_to_load);
                statusImg.setImageResource(R.drawable.pull_down);
                break;
        }
    }
}
