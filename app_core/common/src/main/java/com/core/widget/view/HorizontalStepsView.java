package com.core.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.common.LogUtil;
import com.common.system.DisplayUtil;


/**
 * 绘制横向的进度控件
 * 1.绘制横线==》横线厚度、开始xy和结束xy、进度点
 * 2.绘制圈（点）==》半径、截至点、大圆半径、
 * 3.绘制圈内数字==》
 * 4.绘制进度下面文字描述
 * <p>
 * 未完成：
 * 1.越屏幕滑动
 * Created by Bitliker on 2017/5/17.
 */
public class HorizontalStepsView extends View {

    private int padding;
    private Surface surface;
    private int progress;//当前进度，注意，不能大于titles的长度
    private String[] titles = {"初次沟通", "立项评估", "产品演示", "合同签约", "样品报价", "多次交易"
            , "商务谈判", "需求分析", "完成交易", "完成交易"};

    public HorizontalStepsView(Context context) {
        this(context, null);
    }

    public HorizontalStepsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalStepsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        padding = DisplayUtil.dip2px(context, 10);
        surface = new Surface();
        surface.init();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtil.i("onMeasure ");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int bgWidth;
        if (widthMode == MeasureSpec.EXACTLY)
            bgWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        else
            bgWidth = DisplayUtil.dip2px(getContext(), 311);

        int bgHeight;
        if (heightMode == MeasureSpec.EXACTLY)
            bgHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        else
            bgHeight = DisplayUtil.dip2px(getContext(), 80);
        surface.startX = getPaddingLeft() + surface.pointsRadius + padding;//定义X抽
        surface.endX = bgWidth - surface.stopRadius - padding;
        surface.lineY = Math.min(surface.stopRadius + DisplayUtil.dip2px(getContext(), 5), bgHeight / 2);
    }

    public void setProgress(int progress, String[] titles) {
        if (titles == null || progress > titles.length)
            new Exception("titles can not be null  or progress can not out of titles.length");
        this.progress = progress;
        if (titles.length > 0) {
            this.titles = titles;
        }
        invalidate();

    }

    private float downX, downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.i("MotionEvent.ACTION_DOWN");
                downX = event.getRawX();
                downY = event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                LogUtil.i("MotionEvent.ACTION_UP");
                float upX = event.getRawX();
                float upY = event.getRawY();
                if (upX - downX < 30 && upY - downY < 30) {
                    if (upY < surface.lineY + surface.stopRadius && upY > surface.lineY - surface.stopRadius) {
                        for (int i = 0; i < titles.length; i++) {
                            float x = surface.startX + surface.itemWidth * i;//获得当前的X坐标
                            if (upX < x + surface.stopRadius && upX > x - surface.stopRadius && onClickListener != null) {
                                String message = titles[i];
                                onClickListener.onClick(i, message);
                            }
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LogUtil.i("onLayout changed=" + changed);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        surface.itemWidth = getWidth() / (titles.length);
        drawLine(canvas);
        drawPointsAndText(canvas);
    }

    private void drawLine(Canvas canvas) {
        if (progress > 0) {
            float middle = surface.startX + surface.itemWidth * progress;//获取中间点
            surface.linePaint.setColor(surface.finishColor);
            canvas.drawLine(surface.startX, surface.lineY, middle, surface.lineY, surface.linePaint);
            surface.linePaint.setColor(surface.unfinishColor);
            canvas.drawLine(middle, surface.lineY, surface.endX, surface.lineY, surface.linePaint);
        } else {
            surface.linePaint.setColor(surface.unfinishColor);
            canvas.drawLine(surface.startX, surface.lineY, surface.endX, surface.lineY, surface.linePaint);
        }
    }

    private void drawPointsAndText(Canvas canvas) {
        if (titles == null || titles.length <= 0) return;
        for (int i = 0; i < titles.length; i++) {
            float x = surface.startX + surface.itemWidth * i;//获得当前的X坐标
            //外围点
            drawCurrentPoints(canvas, i, x);
            //绘制索引点
            drawPoints(canvas, i, x);

            //绘制圈内索引值
            drawNumberInPoints(canvas, i, x);

            //绘制描述文字
            drawMessage(canvas, i);
        }
    }

    private void drawNumberInPoints(Canvas canvas, int i, float x) {
        canvas.drawText(String.valueOf(i + 1), x, surface.lineY + surface.pointstextPaint.getTextSize() / 2 - DisplayUtil.dip2px(getContext(), 1), surface.pointstextPaint);
    }

    private void drawMessage(Canvas canvas, int i) {
        String title = titles[i];
//        float textX = surface.textPaint.measureText(title);
        float textY = surface.lineY + surface.stopRadius + surface.textPaint.getTextSize();

        int length = title.length();//剩余字符数
        int currentPosition = 0;
        int line = 0;
        while (length > currentPosition) {
            if (line > 2) {
                String mag = "...";
                canvas.drawText(mag, surface.startX + (i * surface.itemWidth), textY + surface.textPaint.getTextSize() * line, surface.textPaint);
                currentPosition = length;
            } else {
                String mag = title.substring(currentPosition, Math.min(length, currentPosition + 2));
                canvas.drawText(mag, surface.startX + (i * surface.itemWidth), textY + surface.textPaint.getTextSize() * line, surface.textPaint);

            }
            line++;
            currentPosition += 2;
        }

//        if (textX > surface.itemWidth) {
//            String mag1 = title.substring(0, title.length() / 2);
//            String mag2 = title.substring(title.length() / 2, title.length());
//            canvas.drawText(mag1, surface.startX + (i * surface.itemWidth), textY, surface.textPaint);
//            canvas.drawText(mag2, surface.startX + (i * surface.itemWidth), textY + surface.textPaint.getTextSize(), surface.textPaint);
//        } else {
//            canvas.drawText(title, surface.startX + (i * surface.itemWidth), textY, surface.textPaint);
//        }
    }

    private void drawPoints(Canvas canvas, int i, float x) {
        if (i <= progress) {
            surface.pointsPaint.setColor(surface.finishColor);
            surface.textPaint.setColor(surface.finishColor);
        } else {
            surface.pointsPaint.setColor(surface.unfinishColor);
            surface.textPaint.setColor(surface.unfinishColor);
        }
        canvas.drawCircle(x, surface.lineY, surface.pointsRadius, surface.pointsPaint);
    }

    //外围点
    private void drawCurrentPoints(Canvas canvas, int i, float x) {
        if (progress == i) {
            canvas.drawCircle(x, surface.lineY, surface.stopRadius, surface.stopPaint);
        }
    }

    private class Surface {
        private int itemWidth;//每个item所占有的空间宽度

        private Paint linePaint;//线的画笔
        private Paint pointsPaint;//点的画笔
        private Paint stopPaint;//当前点的画笔
        private Paint textPaint;//描述文字画笔
        private Paint pointstextPaint;//圈中文字画笔

        //Color  主要分三种颜色 1.已经完成的进度颜色  2.当前进度提示颜色  3.没有完成的进度颜色
        private int finishColor;//已经完成的进度颜色
        private int unfinishColor;//当前进度提示颜色
        private int currentColor;//没有完成的进度颜色

        private float startX;//开始X轴
        private float endX;//结束X轴
        private float lineY;//线所在的Y轴

        private float pointsRadius;//点半径
        private float stopRadius;//当前进度点的半径
        private int pointsTextSize;//圈中点的字体大小
        private int textSize;//描述文字


        public void init() {
            unfinishColor = Color.parseColor("#cdcbcc");
            finishColor = Color.parseColor("#FFA500");
            currentColor = Color.parseColor("#FFB6C1");

            pointsRadius = DisplayUtil.dip2px(getContext(), 9);
            stopRadius = DisplayUtil.dip2px(getContext(), 12);
            pointsTextSize = DisplayUtil.sp2px(getContext(), 11);
            textSize = DisplayUtil.sp2px(getContext(), 13);

            linePaint = new Paint();
            linePaint.setAntiAlias(true);
            linePaint.setStyle(Paint.Style.FILL);
            linePaint.setStrokeWidth(DisplayUtil.dip2px(getContext(), 3));
            linePaint.setTextSize(DisplayUtil.dip2px(getContext(), 3));
            linePaint.setTextAlign(Paint.Align.CENTER);

            pointsPaint = new Paint(linePaint);
            pointsPaint.setColor(unfinishColor);
            stopPaint = new Paint(linePaint);
            stopPaint.setColor(currentColor);

            pointstextPaint = new Paint(linePaint);
            pointstextPaint.setTextSize(pointsTextSize);
            pointstextPaint.setColor(Color.WHITE);
            textPaint = new Paint(pointstextPaint);
            textPaint.setTextSize(textSize);
        }
    }

    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(int position, String title);
    }
}
