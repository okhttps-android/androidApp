package com.xzjmyk.pm.activity.ui.erp.view;
import java.util.Calendar;






import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.format.DateUtils;

public class DateTimePickerDialog extends AlertDialog implements OnClickListener
{
	private DateTimePicker mDateTimePicker;
	private Calendar mDate = Calendar.getInstance();
	private OnDateTimeSetListener mOnDateTimeSetListener;

	@SuppressWarnings("deprecation")
	public DateTimePickerDialog(Context context, long date)
	{
		super(context);
		mDateTimePicker = new DateTimePicker(context);
		setView(mDateTimePicker);
		mDateTimePicker.setOnDateTimeChangedListener(new DateTimePicker.OnDateTimeChangedListener()
		{
			@Override
			public void onDateTimeChanged(DateTimePicker view, int year, int month, int day, int hour, int minute)
			{
				mDate.set(Calendar.YEAR, year);
				mDate.set(Calendar.MONTH, month);
				mDate.set(Calendar.DAY_OF_MONTH, day);
				mDate.set(Calendar.HOUR_OF_DAY, hour);
				mDate.set(Calendar.MINUTE, minute);
				mDate.set(Calendar.SECOND, 0);
				int weekDay=  mDate.get(Calendar.DAY_OF_WEEK);
				setTitle(year + "年" + (format(month+1)) + "月"+format(day)+"日 "+format(hour)+":"+format(minute)+" "
						+getWeek(weekDay));
				/**@注释：系统格式会发生年份越界情况，有兴趣可以查看源码细看  */
				// updateTitle(mDate.getTimeInMillis());
			}
		});

		setButton("设置", this);
		setButton2("取消", (OnClickListener)null);
		//date外部的日期long，这里不采用外部的日期
		mDate.setTimeInMillis(mDateTimePicker.getCalendar().getTimeInMillis());
		updateTitle(mDateTimePicker.getCalendar().getTimeInMillis());
	}


	public String format(int value) {
		String tmpStr = String.valueOf(value);
		if (value < 10) {
			tmpStr = "0" + tmpStr;
		}
		return tmpStr;
	}

	public String getWeek(int weekDay){
		switch (weekDay) {
			case Calendar.MONDAY:
				return "星期一";
			case Calendar.TUESDAY:
				return "星期二";

			case Calendar.WEDNESDAY:
				return "星期三";
			case Calendar.THURSDAY:
				return "星期四";
			case Calendar.FRIDAY:
				return "星期五";
			case Calendar.SATURDAY:
				return "星期六";
			case Calendar.SUNDAY:
				return "星期日";
			default:
				break;
		}
		return "";
	}

	public interface OnDateTimeSetListener
	{
		void OnDateTimeSet(AlertDialog dialog, long date);
	}

	private void updateTitle(long date)
	{
		int flag = DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY| DateUtils.FORMAT_SHOW_TIME;
		setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
	}

	public void setOnDateTimeSetListener(OnDateTimeSetListener callBack)
	{
		mOnDateTimeSetListener = callBack;
	}

	public void onClick(DialogInterface arg0, int arg1)
	{
		if (mOnDateTimeSetListener != null)
		{
			mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis());
		}
	}
}
