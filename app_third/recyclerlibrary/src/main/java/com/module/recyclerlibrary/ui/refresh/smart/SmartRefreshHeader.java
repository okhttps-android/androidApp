package com.module.recyclerlibrary.ui.refresh.smart;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.module.recyclerlibrary.R;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshView;

/**
 * Created by Bitliker on 2017/9/18.
 */

public class SmartRefreshHeader extends BaseRefreshView {
    GhostLoadingView ghostLoadingView;
    TextView statusTV;
    private final ImageView statusImg;

    public SmartRefreshHeader(Context context, LayoutInflater mInflater) {
        super(context, mInflater);
        mInflater.inflate(R.layout.refresh_smart_header, this);
        ghostLoadingView = (GhostLoadingView) findViewById(R.id.srl_glv_pull_down);
        statusTV = (TextView) findViewById(R.id.statusTV);
        statusImg = (ImageView) findViewById(R.id.statusImg);

    }

    @Override
    public void startAnim() {
        statusTV.setVisibility(GONE);
        statusImg.setVisibility(GONE);
        statusTV.setText(R.string.pull_down_to_refresh);
        statusImg.setImageResource(R.drawable.pull_down);
        ghostLoadingView.setVisibility(VISIBLE);
        ghostLoadingView.startAnim();
    }

    @Override
    public void stopAnim() {
        ghostLoadingView.stopAnim();
        statusTV.setVisibility(VISIBLE);
        statusImg.setVisibility(VISIBLE);
        ghostLoadingView.setVisibility(GONE);

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
