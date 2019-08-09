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

public class SmartRefreshFooter extends BaseRefreshView {
    EatBeanLoadingView ghostLoadingView;
    TextView statusTV;
    private final ImageView statusImg;

    public SmartRefreshFooter(Context context, LayoutInflater mInflater) {
        super(context, mInflater);
        mInflater.inflate(R.layout.refresh_smart_footer, this);
        ghostLoadingView = (EatBeanLoadingView) findViewById(R.id.srl_elv_pull_up);
        statusTV = (TextView) findViewById(R.id.statusTV);
        statusImg = (ImageView) findViewById(R.id.statusImg);
    }

    @Override
    public void startAnim() {
        statusTV.setVisibility(GONE);
        statusTV.setText(R.string.pull_up_to_load);
        statusImg.setImageResource(R.drawable.pull_up);
        statusImg.setVisibility(GONE);
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
