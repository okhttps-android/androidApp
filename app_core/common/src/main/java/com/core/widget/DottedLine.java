package com.core.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.core.app.R;

/**
 * @author RaoMeng
 * @describe 竖直虚线
 * @date 2018/4/26 8:49
 */

public class DottedLine extends View {
    private Context context;

    public DottedLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.red));
        paint.setStrokeWidth(dip2px(context, 2));
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, 900);
        PathEffect effects = new DashPathEffect(new float[]{6, 4, 4, 4}, 2);
        paint.setPathEffect(effects);
        canvas.drawPath(path, paint);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
