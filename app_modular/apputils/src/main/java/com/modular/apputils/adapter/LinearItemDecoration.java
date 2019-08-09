package com.modular.apputils.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.modular.apputils.R;

/**
 * Created by Bitlike on 2018/1/12.
 */

public class LinearItemDecoration extends RecyclerView.ItemDecoration {
    private int dividerHeight;
    private Paint dividerPaint;

    public LinearItemDecoration(Context context) {
        dividerHeight = 2;
        dividerPaint = new Paint();
        dividerPaint.setColor(context.getResources().getColor(R.color.item_line));
    }

    //可以实现类似绘制背景的效果，内容在上面
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        for (int i = 0; i < childCount - 1; i++) {
            View view = parent.getChildAt(i);
            float top = view.getBottom();
            float bottom = view.getBottom() + dividerHeight;
            c.drawRect(left, top, right, bottom, dividerPaint);
        }


    }

//    //可以绘制在内容的上面，覆盖内容
//    @Override
//    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDrawOver(c, parent, state);
//        Paint paint=   new Paint();
//        paint.setColor(Color.RED);
//        c.drawText("这个是onDrawOver",20,20,paint);
//    }

    //可以实现类似padding的效果
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = dividerHeight;//类似加了一个bottom padding
    }
}
