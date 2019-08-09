package com.core.widget.view;

import android.content.Context;
import android.util.AttributeSet;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

/**
 * Created by Arison on 2017/2/28.
 */
public class MyStickyGridHeadersGridView extends StickyGridHeadersGridView {
    public MyStickyGridHeadersGridView(Context context) {
        super(context);
    }

    public MyStickyGridHeadersGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyStickyGridHeadersGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
