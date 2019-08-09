package com.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

import com.core.app.R;


/**
 * Created by RaoMeng on 2017/8/10.
 */
public class CircleTextView extends TextView {

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 画笔颜色，默认白色
     */
    private int mBackgroundColor = 0xFFFFFF;

    /**
     * 是否填充颜色
     */
    private boolean isFillColor = true;

    public CircleTextView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleTextView);
        mBackgroundColor = typedArray.getColor(R.styleable.CircleTextView_backgroundColor, mBackgroundColor);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAntiAlias(true);
        if (isFillColor) {
            mPaint.setColor(mBackgroundColor);
//            mPaint.setColor(getResources().getColor(R.color.colorAccent));
            mPaint.setStyle(Paint.Style.FILL);
        }

        RectF rectF = new RectF();
        int radius = getMeasuredHeight() > getMeasuredWidth() ? getMeasuredHeight() : getMeasuredWidth();
        rectF.set(getPaddingLeft(), getPaddingTop(), radius - getPaddingRight(), radius - getPaddingBottom());
        canvas.drawArc(rectF, 0, 360, false, mPaint);

        super.onDraw(canvas);
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setMyBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidate();
    }

    public boolean isFillColor() {
        return isFillColor;
    }

    public void setIsFillColor(boolean isFillColor) {
        this.isFillColor = isFillColor;
        invalidate();
    }

}
