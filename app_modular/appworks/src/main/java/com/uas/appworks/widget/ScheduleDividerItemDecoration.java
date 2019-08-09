package com.uas.appworks.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.system.DisplayUtil;

public class ScheduleDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private Context context;
    public ScheduleDividerItemDecoration(Context context) {
        super();
        this.context=context;
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        for ( int i = 0; i < childCount; i++ ) {
            View view = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(view);
            //第一个ItemView不需要绘制
            if ( index == 0 ) {
                continue;
            }
//            float dividerTop = view.getTop() - mDividerHeight;
//            float dividerLeft = parent.getPaddingLeft();
//            float dividerBottom = view.getTop();
//            float dividerRight = parent.getWidth() - parent.getPaddingRight();
//            c.drawRect(dividerLeft,dividerTop,dividerRight,dividerBottom,mPaint);
            c.drawLine(view.getX(),view.getTop(),view.getWidth(),view.getTop(),mPaint);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(DisplayUtil.dip2px(context,8), 0, 0, 2);
    }
}
