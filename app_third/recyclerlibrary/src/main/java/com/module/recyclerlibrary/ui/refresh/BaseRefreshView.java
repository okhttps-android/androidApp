package com.module.recyclerlibrary.ui.refresh;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by Bitliker on 2017/9/18.
 */

public abstract class BaseRefreshView extends RelativeLayout {
    // 普通状态
    protected final int NORMAL = 0;
    // 意图刷新
    protected final int TRY_REFRESH = 1;
    // 刷新状态
    protected final int REFRESH = 2;
    // 意图加载
    protected final int TRY_LOAD_MORE = 3;
    // 加载状态
    protected final int LOAD_MORE = 4;


    public BaseRefreshView(Context context, LayoutInflater mInflater) {
        super(context);
    }


    public abstract void startAnim();

    public abstract void stopAnim();

    public abstract void upStatus(int status);


}
