package com.modular.apputils.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.system.DisplayUtil;
import com.modular.apputils.R;


/**
 * Created by Bitlike on 2017/12/12.
 */

public class TravelDirectionView extends View {

    private int padding = 0;
    private int paddingTop = 0;
    private Paint paint;


    private String title;
    private String time;
    private float titleSize;
    private float timeSize = 0;


    public TravelDirectionView(Context context) {
        this(context, null);
    }

    public TravelDirectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        title = "";
        time = "";
        float defTextSize = DisplayUtil.dip2px(context, 16);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TravelDirectionView);
        timeSize = typedArray.getDimension(R.styleable.TravelDirectionView_timeSize, defTextSize);
        titleSize = typedArray.getDimension(R.styleable.TravelDirectionView_titleSize, defTextSize);
        title = typedArray.getString(R.styleable.TravelDirectionView_title);
        time = typedArray.getString(R.styleable.TravelDirectionView_time);
        typedArray.recycle();
        padding = DisplayUtil.dip2px(context, 8);
        paddingTop = DisplayUtil.dip2px(context, 3);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
    }


    public void  setData(String title,String time){
        this.title = title;
        this.time = time;
        invalidate();
    }
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    public void setTime(String time) {
        this.time = time;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        canvas.drawLine(padding, height / 2, width - padding, height / 2, paint);
        canvas.drawLine(width - padding, height / 2, width - padding - padding, height / 2 - padding, paint);

        paint.setTextSize(titleSize);
        float titleLength = paint.measureText(title);
        canvas.drawText(title, (width - titleLength) / 2, height / 2 - paddingTop, paint);
        paint.setTextSize(timeSize);
        float timeLength = paint.measureText(time);
        canvas.drawText(time, (width - timeLength) / 2, height / 2 + paddingTop + timeSize, paint);

    }
}
