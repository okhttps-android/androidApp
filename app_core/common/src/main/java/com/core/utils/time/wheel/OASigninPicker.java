package com.core.utils.time.wheel;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.core.utils.CommonUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Dong on 2016/5/13.
 * 年月日 时分秒
 */
public class OASigninPicker extends WheelPicker {

    /**
     * 年月日
     */
    public static final int YEAR_MONTH_DAY = 0;
    private ArrayList<String> years = new ArrayList<String>();
    private ArrayList<String> months = new ArrayList<String>();
    private ArrayList<String> days = new ArrayList<String>();
    private String yearLabel = "年", monthLabel = "月", dayLabel = "日";
    private int selectedYearIndex = 0, selectedMonthIndex = 0, selectedDayIndex = 0;
    private OnDateTimePickListener onDateTimePickListener;
    private int year, month, day = 0;
    private boolean mHaveMonth = true;
    private boolean mHaveDay = true;

    @IntDef(flag = false, value = {YEAR_MONTH_DAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public OASigninPicker(Activity activity) {
        super(activity);
        textSize = 16;//年月日时分，比较宽，设置字体小一点才能显示完整
        for (int i = CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy")) - 2; i <= CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy")); i++) {
            years.add(String.valueOf(i));
        }
        for (int i = 1; i <= 12; i++) {
            months.add(DateUtils.fillZero(i));
        }
        for (int i = 1; i <= 31; i++) {
            days.add(DateUtils.fillZero(i));
        }
    }

    public OASigninPicker(Activity activity, int startYear, int endYear) {
        super(activity);
        textSize = 16;//年月日时分，比较宽，设置字体小一点才能显示完整
        for (int i = startYear; i <= endYear; i++) {
            years.add(String.valueOf(i));
        }
        for (int i = 1; i <= 12; i++) {
            months.add(DateUtils.fillZero(i));
        }
        for (int i = 1; i <= 31; i++) {
            days.add(DateUtils.fillZero(i));
        }
    }

    public OASigninPicker(Activity activity, int startYear, int endYear, boolean haveDay) {
        super(activity);
        mHaveDay = haveDay;
        textSize = 16;//年月日时分，比较宽，设置字体小一点才能显示完整
        for (int i = startYear; i <= endYear; i++) {
            years.add(String.valueOf(i));
        }
        for (int i = 1; i <= 12; i++) {
            months.add(DateUtils.fillZero(i));
        }
        if (haveDay)
            for (int i = 1; i <= 31; i++) {
                days.add(DateUtils.fillZero(i));
            }
    }

    public OASigninPicker(Activity activity, int startYear, int endYear, boolean haveMonth, boolean haveDay) {
        super(activity);
        mHaveDay = haveDay;
        mHaveMonth = haveMonth;
        textSize = 16;//年月日时分，比较宽，设置字体小一点才能显示完整
        for (int i = startYear; i <= endYear; i++) {
            years.add(String.valueOf(i));
        }
        if (haveMonth) {
            for (int i = 1; i <= 12; i++) {
                months.add(DateUtils.fillZero(i));
            }
            if (haveDay) {
                for (int i = 1; i <= 31; i++) {
                    days.add(DateUtils.fillZero(i));
                }
            }
        }
    }

    @NonNull
    @Override
    protected View makeCenterView() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        WheelView yearView = new WheelView(activity.getBaseContext());
        yearView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        yearView.setTextSize(textSize);
        yearView.setTextColor(textColorNormal, textColorFocus);
        yearView.setLineVisible(lineVisible);
        yearView.setLineColor(lineColor);
        yearView.setOffset(offset);
        layout.addView(yearView);
        TextView yearTextView = new TextView(activity);
        yearTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        yearTextView.setTextSize(textSize);
        yearTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(yearLabel)) {
            yearTextView.setText(yearLabel);
        }
        layout.addView(yearTextView);

        final WheelView monthView = new WheelView(activity.getBaseContext());
        monthView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        monthView.setTextSize(textSize);
        monthView.setTextColor(textColorNormal, textColorFocus);
        monthView.setLineVisible(lineVisible);
        monthView.setLineColor(lineColor);
        monthView.setOffset(offset);
        layout.addView(monthView);
        TextView monthTextView = new TextView(activity);
        monthTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        monthTextView.setTextSize(textSize);
        monthTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(monthLabel)) {
            monthTextView.setText(monthLabel);
        }
        if (mHaveMonth) {
            layout.addView(monthTextView);
        }

        final WheelView dayView = new WheelView(activity.getBaseContext());
        dayView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        dayView.setTextSize(textSize);
        dayView.setTextColor(textColorNormal, textColorFocus);
        dayView.setLineVisible(lineVisible);
        dayView.setLineColor(lineColor);
        dayView.setOffset(offset);
        layout.addView(dayView);
        TextView dayTextView = new TextView(activity);
        dayTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        dayTextView.setTextSize(textSize);
        dayTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(dayLabel)) {
            dayTextView.setText(dayLabel);
        }
        if (mHaveDay) {
            layout.addView(dayTextView);
        }

        yearView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                selectedYearIndex = selectedIndex;
                int maxDays = 0, maxMonth = 0;
                if (mHaveMonth) {
                    if (isRange() && item.equals(getIntMonth(year))) {
                        maxMonth = month;
                        if (getSelectedMonth().equals(getIntMonth(month))) {
                            maxDays = day;
                        } else {
                            maxDays = DateUtils.calculateDaysInMonth(stringToYearMonthDay(item), stringToYearMonthDay(months.get(selectedMonthIndex)));
                        }
                    } else {
                        maxMonth = 12;
                        maxDays = DateUtils.calculateDaysInMonth(stringToYearMonthDay(item), stringToYearMonthDay(months.get(selectedMonthIndex)));
                    }
                }

                if (mHaveDay) {
                    days.clear();
                    for (int i = 1; i <= maxDays; i++) {
                        days.add(DateUtils.fillZero(i));
                    }
                    if (selectedDayIndex >= maxDays) {
                        //年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                        selectedDayIndex = days.size() - 1;
                    }
                    dayView.setItems(days, selectedDayIndex);
                }

                if (mHaveMonth) {
                    months.clear();
                    for (int i = 1; i <= maxMonth; i++) {
                        months.add(DateUtils.fillZero(i));
                    }
                    if (selectedMonthIndex >= maxMonth) {
                        //年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                        selectedMonthIndex = months.size() - 1;
                    }
                    monthView.setItems(months, selectedMonthIndex);
                }
            }
        });

        if (mHaveMonth) {
            monthView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                    selectedMonthIndex = selectedIndex;
                    int maxDays = 0;
                    if (isRange() && getSelectedYear().equals(String.valueOf(year)) && months.get(selectedIndex).equals(getIntMonth(month))) {
                        maxDays = day;
                    } else
                        maxDays = DateUtils.calculateDaysInMonth(stringToYearMonthDay(years.get(selectedYearIndex)), stringToYearMonthDay(item));
                    if (mHaveDay) {
                        days.clear();
                        for (int i = 1; i <= maxDays; i++) {
                            days.add(DateUtils.fillZero(i));
                        }
                        if (selectedDayIndex >= maxDays) {
                            //年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                            selectedDayIndex = days.size() - 1;
                        }
                        dayView.setItems(days, selectedDayIndex);
                    }

                }
            });
        }

        if (mHaveDay)
            dayView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                    selectedDayIndex = selectedIndex;
                }
            });
        yearView.setItems(years, selectedYearIndex);
        if (mHaveMonth) {
            monthView.setItems(months, selectedMonthIndex);
        }
        if (mHaveDay) {
            dayView.setItems(days, selectedDayIndex);
        }
        return layout;
    }

    private boolean isRange() {
        if (year != 0 && month != 0 && day != 0)
            return true;
        else return false;
    }

    @Override
    protected void onSubmit() {
        if (onDateTimePickListener != null) {
            String year = getSelectedYear();
            String month = mHaveMonth ? getSelectedMonth() : "";
            String day = mHaveDay ? getSelectedDay() : "";
            onDateTimePickListener.setTime(year, month, day);
        }
    }

    /**
     * Gets selected year.
     *
     * @return the selected year
     */
    public String getSelectedYear() {
        return years.get(selectedYearIndex);
    }

    private String getIntMonth(int t) {
        return (t < 10 ? "0" : "") + String.valueOf(t);
    }

    /**
     * Gets selected month.
     *
     * @return the selected month
     */
    public String getSelectedMonth() {
        return months.get(selectedMonthIndex);
    }

    /**
     * Gets selected day.
     *
     * @return the selected day
     */
    public String getSelectedDay() {
        return days.get(selectedDayIndex);
    }

    private int stringToYearMonthDay(String text) {
        if (text.startsWith("0")) {
            //截取掉前缀0以便转换为整数
            text = text.substring(1);
        }
        return Integer.parseInt(text);
    }

    /**
     * Sets label.
     *
     * @param yearLabel  the year label
     * @param monthLabel the month label
     * @param dayLabel   the day label
     */
    public void setLabel(String yearLabel, String monthLabel, String dayLabel, String hourLabel, String minuteLabel) {
        this.yearLabel = yearLabel;
        this.monthLabel = monthLabel;
        this.dayLabel = dayLabel;
    }


    public void setRange(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

    }

    private int findItemIndex(ArrayList<String> items, int item) {
        //折半查找有序元素的索引
        int index = Collections.binarySearch(items, item, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                String lhsStr = lhs.toString();
                String rhsStr = rhs.toString();
                lhsStr = lhsStr.startsWith("0") ? lhsStr.substring(1) : lhsStr;
                rhsStr = rhsStr.startsWith("0") ? rhsStr.substring(1) : rhsStr;
                return Integer.parseInt(lhsStr) - Integer.parseInt(rhsStr);
            }
        });
        if (index < 0) {
            index = 0;
        }
        return index;
    }

    public void setSelectedItem(int yeas) {
        selectedYearIndex = findItemIndex(years, yeas);
//        selectedMonthIndex = findItemIndex(months, month);
//        selectedDayIndex = findItemIndex(days, day);
    }

    public void setSelectedItem(int yeas, int month) {
        selectedYearIndex = findItemIndex(years, yeas);
        selectedMonthIndex = findItemIndex(months, month);
//        selectedDayIndex = findItemIndex(days, day);
    }

    public void setSelectedItem(int yeas, int month, int day) {
        selectedYearIndex = findItemIndex(years, yeas);
        selectedMonthIndex = findItemIndex(months, month);
        selectedDayIndex = findItemIndex(days, day);
    }

    /**
     * The interface On DateTime pick listener.
     */
    public interface OnDateTimePickListener {
        void setTime(String year, String month, String day);
    }

    public void setOnDateTimePickListener(OnDateTimePickListener listener) {
        this.onDateTimePickListener = listener;
    }

}
