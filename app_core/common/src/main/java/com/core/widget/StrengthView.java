package com.core.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.system.DisplayUtil;

/**
 * @author RaoMeng
 * @describe 密码强度
 * @date 2018/5/6 21:38
 */
public class StrengthView extends View {

    public enum Level {
        STRENGTH_NONE("无", Color.parseColor("#C5C4C4"), 0), STRENGTH_WEAK("低", Color.BLACK, 1), STRENGTH_MEDIUM("中", Color.BLACK, 2), STRENGTH_STRONG("高", Color.BLACK, 3);
        String levelStr;
        int levelColor;
        int index;

        Level(String levelStr, int levelColor, int index) {
            this.levelStr = levelStr;
            this.levelColor = levelColor;
            this.index = index;
        }
    }

    private int mTextSize;
    private Level mLevel;//强度等级
    private Paint mPaint;
    float levelWidth;
    float gap;
    float levelHeight;
    float textPadding;
    // 文字尺寸
    private float mTextWidth;
    private float mTextHeight;

    public Level getLevel() {
        return mLevel;
    }

    public void setLevel(Level level) {
        mLevel = level;
        invalidate();
    }

    public StrengthView(Context context) {
        this(context, null, 0);
    }

    public StrengthView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrengthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStrengthView(context, attrs, defStyleAttr);
    }

    private void initStrengthView(Context context, AttributeSet attrs, int defStyleAttr) {
        levelWidth = DisplayUtil.dip2px(context, 26);
        gap = DisplayUtil.dip2px(context, 8);
        levelHeight = DisplayUtil.dip2px(context, 4);
        textPadding = DisplayUtil.dip2px(context, 8);
        mTextSize = DisplayUtil.sp2px(context, 14);

        mLevel = Level.STRENGTH_NONE;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mTextSize);
        Rect rect = new Rect();
        mPaint.getTextBounds(Level.STRENGTH_NONE.levelStr, 0, Level.STRENGTH_NONE.levelStr.length(), rect);
        mTextWidth = rect.width();
        mTextHeight = rect.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth;
        int measuredHeight;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        measuredWidth = widthMode == MeasureSpec.EXACTLY ? widthSize : ((int) (getPaddingLeft() + getPaddingRight() + mTextWidth + (Level.values().length - 1) * levelWidth + (Level.values().length - 2) * gap + textPadding));
        measuredHeight = heightMode == MeasureSpec.EXACTLY ? heightSize : ((int) (getPaddingTop() + getPaddingBottom() + mTextHeight));
        // 固定套路，保存控件宽高值
        setMeasuredDimension(measuredWidth, measuredHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int currentIndex = mLevel == null ? 0 : mLevel.index;
        float startLeft = getPaddingLeft();

        int levelCount = Level.values().length - 1;
        for (int i = 1; i <= levelCount; i++) {
            if (i > currentIndex) {
                mPaint.setColor(Level.STRENGTH_NONE.levelColor);
            } else {
                mPaint.setColor(Level.values()[i].levelColor);
            }

            canvas.drawRect(startLeft, getPaddingTop(), startLeft + levelWidth, getPaddingTop() + levelHeight, mPaint);
            startLeft += levelWidth;
            if (i != levelCount) {
                mPaint.setColor(Color.WHITE);
                canvas.drawRect(startLeft, getPaddingTop(), startLeft + gap, getPaddingTop() + levelHeight, mPaint);
                startLeft += gap;
            }
        }

        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(mTextSize);
        String levelText = mLevel == null ? Level.STRENGTH_NONE.levelStr : mLevel.levelStr;
        // 计算text的baseline
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        // baseline思路：先设为控件的水平中心线，再调整到文本区域的水平中心线上
        // fontMetrics的top/bottom/ascent/descent属性值，是基于baseline为原点的，上方为负值，下方为正！
        float baseLine =
                getPaddingTop()
                        + levelHeight / 2
                        + ((Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent)) / 2 - Math.abs(fontMetrics.descent));
        canvas.drawText(levelText, startLeft + textPadding, baseLine, mPaint);
    }
}
