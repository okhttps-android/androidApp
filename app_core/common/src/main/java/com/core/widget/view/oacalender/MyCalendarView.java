package com.core.widget.view.oacalender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.common.system.DisplayUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by pengminggong on 2016/9/20.
 */
public class MyCalendarView extends View implements View.OnTouchListener {
    //日期记录
    private Date curDate; // 当前日历显示的月
    private Date downDate; // 手指按下状态时临时日期
    //索引
    private int todayIndex; // 今天的索引
    private int downIndex = -1; // 按下的格子索引
    private int startIndex; // 开始的格子索引(当月)
    private int endIndex; // 开始的格子索引(当月)
    //装饰物 decorat
    private String decoratDays;
    private int maxDay;//当月有多少天


    private Calendar calendar;
    private Surface surface;
    private int[] date; // 日历显示格子数目

    public MyCalendarView(Context context) {
        this(context, null);
    }

    public MyCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surface = new Surface();
        curDate = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        todayIndex = calendar.get(Calendar.DAY_OF_MONTH);
        setBackgroundColor(surface.bgColor);
        surface.density = getResources().getDisplayMetrics().density;
        //计算格子数
        calendar.set(Calendar.DAY_OF_MONTH, 1);//设置为当月第一天
        startIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;//获取第一天在当周的星期几，返回多1，所以减1
        maxDay = calendar.getActualMaximum(Calendar.DATE);//获取当月有多少天
        //如果从星期日开始作为第一天
        date = new int[maxDay + startIndex];
        //如果从星期一作为开始第一天         date = new int[daysCountOfMonth + dayInWeek-1];
        endIndex = date.length - 1;
        todayIndex += startIndex - 1;
        for (int i = startIndex, j = 1; i < date.length; i++, j++) {
            date[i] = j;
        }
        downIndex = todayIndex;
        setOnTouchListener(this);
    }


    private void setCalendar(Date d) {
        curDate = d;
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//设置为当月第一天
        startIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;//获取第一天在当周的星期几，返回多1，所以减1
        maxDay = calendar.getActualMaximum(Calendar.DATE);//获取当月有多少天

        //如果从星期日开始作为第一天
        date = new int[maxDay + startIndex];
        endIndex = date.length - 1;
        for (int i = startIndex, j = 1; i < date.length; i++, j++) {
            date[i] = j;
        }
        if (isThisMonth())
            downIndex = todayIndex;
        else
            downIndex = startIndex;
        surface.init();
    }

    /**
     * 它有三种模式：计算视图大小
     * UNSPECIFIED(未指定),父元素不对子元素施加任何束缚，子元素可以得到任意想要的大小；(具体值)
     * EXACTLY(完全)，父元素决定子元素的确切大小，子元素将被限定在给定的边界里而忽略它本身大小；(match_parent)
     * AT_MOST(至多)，子元素至多达到指定大小的值。(wrap_parent)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                surface.width = widthSize;
                break;
            case MeasureSpec.EXACTLY:
                surface.width = getResources().getDisplayMetrics().widthPixels;
                break;
            case MeasureSpec.AT_MOST:
                surface.width = getResources().getDisplayMetrics().widthPixels;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                surface.height = heightSize;
                break;
            case MeasureSpec.EXACTLY:
                surface.height = (getResources().getDisplayMetrics().heightPixels * 1 / 3);
                break;
            case MeasureSpec.AT_MOST:
                surface.height = (getResources().getDisplayMetrics().heightPixels * 1 / 3);
                break;
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(surface.width, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(surface.height, heightMode);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        surface.width = getResources().getDisplayMetrics().widthPixels;
//        surface.height = (getResources().getDisplayMetrics().heightPixels * 1 / 3);
//        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.width, View.MeasureSpec.EXACTLY);
//        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.height, View.MeasureSpec.EXACTLY);
//        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            surface.init();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    float x;
    float y;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - x) < 10 && Math.abs(event.getY() - y) < 10) {//为点击事件
                    reckonClick();
                } else {
                    return true;
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画星期
        drawWeek(canvas);
        //画背景
        drawSelect(canvas);
        //画日期
        drawDay(canvas);
    }

    //绘画点击的内容
    private void drawSelect(Canvas canvas) {
        if (downIndex >= startIndex && downIndex <= endIndex) {
            //圆的最中心
            float cellY = surface.weekHeight + surface.cellHeight * (downIndex / 7) + surface.cellHeight / 2;
            float cellX = surface.cellWidth * (downIndex % 7) + surface.cellWidth / 2;
//            float radius = Math.min(surface.cellHeight, surface.cellWidth) * surface.downScale;
            canvas.drawCircle(cellX, cellY, surface.downTaxtSize, surface.selectPaint);
        }
    }

    //画日期
    private void drawDay(Canvas canvas) {
        String chche = "";
        //画上个月日期
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, -1);
        int startItem = calendar.getActualMaximum(Calendar.DATE) - startIndex + 1;//获取当月有多少天
        calendar.set(Calendar.DAY_OF_MONTH, startItem);
        //使文字垂直居中
        Paint.FontMetrics fontMetrics = surface.hineDatePaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
//        float dayTextY = surface.weekHeight +(surface.cellHeight +surface.dateTaxtSize) / 2;
        float dayTextY = surface.weekHeight + (surface.cellHeight + fontHeight) / 2 - fontMetrics.bottom;

        for (int i = 0; i < startIndex; i++) {
            float dayTextX = i * surface.cellWidth + (surface.cellWidth - surface.hineDatePaint.measureText("今")) / 2f;
            canvas.drawText(calendar.get(Calendar.DAY_OF_MONTH) + "", dayTextX, dayTextY, surface.hineDatePaint);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        //画当月
        for (int i = startIndex; i < date.length; i++) {
            int item = i % surface.weekText.length;
            if (i != 0 && item == 0) {
                dayTextY += surface.cellHeight;
            }
            if (todayIndex == i && isThisMonth()) {
                chche = "今";
                surface.datePaint.setColor(surface.todayColor);
            } else {
                chche = String.valueOf(date[i]);
                surface.datePaint.setColor(surface.dateColor);
            }
            if (downIndex == i) {
                surface.datePaint.setColor(surface.selectColor);
            }
            float dayTextX = surface.cellWidth * (i % surface.weekText.length) +
                    (surface.cellWidth - surface.datePaint.measureText(chche)) / 2f;
            canvas.drawText(chche, dayTextX, dayTextY, surface.datePaint);
            //画装饰物
            int day = (i - startIndex + 1);
            if (decoratDays != null && !decoratDays.isEmpty() && getIsDecorat(day)) {
                drawDecorat(canvas, i, surface.decorPaint);
            }
        }
        //画下个月日期
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int next = date.length % 7 == 0 ? 0 : (date.length + (7 - date.length % 7));
        for (int i = date.length; i < next; i++) {
            float dayTextX = surface.cellWidth * (i % surface.weekText.length) + (surface.cellWidth - surface.datePaint.measureText("今")) / 2f;
            canvas.drawText(calendar.get(Calendar.DAY_OF_MONTH) + "", dayTextX, dayTextY, surface.hineDatePaint);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /*判断*/
    private boolean getIsDecorat(int day) {
        Pattern p = Pattern.compile("," + day + ",");//遍历对象
        Matcher m = p.matcher(decoratDays);//遍历源
        return m.find();
    }

    /*判断是否是当月*/
    public boolean isThisMonth() {
        calendar.setTime(curDate);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        calendar.setTime(new Date());
        if (year != calendar.get(Calendar.YEAR))
            return false;
        if (month != calendar.get(Calendar.MONTH))
            return false;
        return true;
    }
    /*end 判断*/

    /**
     * 绘画装饰物
     *
     * @param index      date索引
     * @param decorPaint 画笔
     */
    private void drawDecorat(Canvas canvas, int index, Paint decorPaint) {
        //圆的最中心
        float cellY = surface.weekHeight + surface.cellHeight * (index / 7) + surface.cellHeight / 2;
        float cellX = surface.cellWidth * (index % 7) + surface.cellWidth / 2;
//        float radius = Math.min(surface.cellWidth, surface.weekHeight) * surface.downScale;
        canvas.drawCircle(cellX, cellY, surface.downTaxtSize, decorPaint);
    }

    //画星期
    private void drawWeek(Canvas canvas) {
        //1.星期的Y抽位置,星期字体为星期框高度的一半，所以在3/4的位置开始绘画
        float weekTextY = surface.weekHeight - (surface.weekHeight - surface.weekTaxtSize) / 2;
        for (int i = 0; i < surface.weekText.length; i++) {
            //Paint.measureText  获取该字的宽度值
            float weekTextX = surface.cellWidth * i + (surface.cellWidth - surface.weekPaint.measureText(surface.weekText[i])) / 2f;
            canvas.drawText(surface.weekText[i], weekTextX, weekTextY, surface.weekPaint);
        }
    }

    /*计算判断的点击的索引*/
    private void reckonClick() {
        int indexX = (int) Math.floor(x / surface.cellWidth);
        int indexY = (int) Math.floor((y - surface.weekHeight) / surface.cellHeight);
        //当前点击的索引
        int downIndex = indexY * 7 + indexX;
        if (downIndex < startIndex || downIndex > endIndex) {
            return;
        }
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, date[downIndex]);
        downDate = calendar.getTime();
        boolean isClickAgen = this.downIndex == downIndex;
        if (this.downIndex != downIndex) {//可以点击
            this.downIndex = downIndex;
        } else {
            this.downIndex = -1;
        }
        if (dateListener != null)
            this.dateListener.result(isClickAgen, downDate);
        invalidate();
    }

    /*计算判断的点击的索引*/
    private boolean setReckon() {
        int indexX = (int) Math.floor(x / surface.cellWidth);
        int indexY = (int) Math.floor((y - surface.weekHeight) / surface.cellHeight);
        downIndex = indexY * 7 + indexX;
        if (downIndex < startIndex || downIndex > endIndex) return false;//当点击的是非本月的日期时候
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, date[downIndex]);
        downDate = calendar.getTime();
        return true;
    }

    public Date getCurDate() {
        return curDate;
    }

    /*设置月份*/
    public void setCurDate(Date date) {
        curDate = date;
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;//获取第一天在当周的星期几，返回多1，所以减1
        maxDay = calendar.getActualMaximum(Calendar.DATE);//获取当月有多少天
        //如果从星期日开始作为第一天
        this.date = new int[maxDay + startIndex];
        endIndex = this.date.length - 1;
        for (int i = startIndex, j = 1; i < this.date.length; i++, j++) {
            this.date[i] = j;
        }
//        if (isThisMonth())
//            downIndex = todayIndex;
//        else {
//            calendar.setTime(date);
//            downIndex = startIndex + calendar.get(Calendar.DAY_OF_MONTH) - 1;
//        }
        surface.init();
        invalidate();
    }

    /*设置为上个月*/
    private void setNextMonth() {
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setCalendar(calendar.getTime());
    }

    /*设置为下个月*/
    private void setLastMonth() {
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setCalendar(calendar.getTime());
    }

    public void setDecoratDays(Set<Integer> decoratDays) {
        this.decoratDays = "";
        StringBuilder builder = new StringBuilder();
        builder.append(",");
        for (Integer e : decoratDays) {
            builder.append(e + ",");
        }
        this.decoratDays = builder.toString();
        invalidate();
    }

    private class Surface {
        private float density;//手机屏幕密度
        private int width;//整个控件宽度
        private int height;   //整个控件高度
        private float weekHeight;//周的方框高度
        private float cellWidth; // 日期方框宽度
        private float cellHeight; // 日期方框高度
        private int bgColor = 0xEBE9E9;
        private int weekColor = Color.BLACK;//周视图的画笔颜色
        private int dateColor = Color.BLACK;//月视图的画笔颜色x
        private int selectColor = Color.WHITE;//选择中的画笔颜色
        private int selectBgColor = Color.RED;//选择中的画笔颜色
        private int todayColor = Color.RED;//选择中的画笔颜色
        private int decorColor = Color.RED;//装饰画笔颜色
        private int hineDateColor = Color.parseColor("#BFD3D3D3");//装饰画笔颜色

        private Paint weekPaint;//周视图的画笔
        private Paint datePaint;//月视图的画笔
        private Paint selectPaint;//选择中的画笔
        private Paint decorPaint;//装饰画笔
        private Paint hineDatePaint;//非本月日期

        private float weekTaxtSize = 55f;
        private float dateTaxtSize = 55f;
        private float downTaxtSize;
        private String[] weekText = {"日", "一", "二", "三", "四", "五", "六"};

        private void init() {
            dateTaxtSize = weekTaxtSize = DisplayUtil.dip2px(getContext(), 18);
            downTaxtSize = (dateTaxtSize + DisplayUtil.dip2px(getContext(), 10)) / 2;
            //计算周框的高度 1.获取日期共多少行
            int dateRowNum = date.length / 7 + (date.length % 7 > 0 ? 1 : 0);
            cellHeight = weekHeight = height / (dateRowNum + 1);
            cellWidth = width / 7f;
            selectPaint = new Paint();//选择中的画笔
            selectPaint.setColor(selectBgColor);
            selectPaint.setAntiAlias(true);
            decorPaint = new Paint();//装饰画笔
            decorPaint.setColor(decorColor);
            decorPaint.setStyle(Paint.Style.STROKE);
            decorPaint.setStrokeWidth(3);
            decorPaint.setAntiAlias(true);

            weekPaint = new Paint();//周视图的画笔
            weekPaint.setColor(weekColor);
            weekPaint.setAntiAlias(true);
            weekPaint.setTextSize(weekTaxtSize);

            datePaint = new Paint();//日期视图的画笔
            datePaint.setColor(dateColor);
            datePaint.setAntiAlias(true);
            datePaint.setTextSize(dateTaxtSize);

            hineDatePaint = new Paint();//日期视图的画笔
            hineDatePaint.setColor(hineDateColor);
            hineDatePaint.setAntiAlias(true);
            hineDatePaint.setTextSize(dateTaxtSize);
        }
    }

    // 接口管理
    public void setDateListener(OnSelectDateListener dateListener) {
        this.dateListener = dateListener;
    }

    private OnSelectDateListener dateListener;

    public interface OnSelectDateListener {
        /**
         * @param isClickAgen
         * @param date        日期对象
         */
        void result(boolean isClickAgen, Date date);
    }

}
