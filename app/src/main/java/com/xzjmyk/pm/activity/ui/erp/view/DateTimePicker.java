package com.xzjmyk.pm.activity.ui.erp.view;

import java.util.Calendar;





import android.content.Context;
import android.text.format.DateFormat;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.NumberPicker.OnValueChangeListener;

import com.xzjmyk.pm.activity.R;

public class DateTimePicker extends FrameLayout implements Formatter
{
	private final NumberPicker mYearSpinner;
	private final NumberPicker mDateSpinner;
	private final NumberPicker mHourSpinner;
	private final NumberPicker mMinuteSpinner;
	private Calendar mDate;
    private int mHour,mMinute; 
    private String[] mDateDisplayValues = new String[7];
    private String[] mYearDisplayValues = new String[7];
    private OnDateTimeChangedListener mOnDateTimeChangedListener;
    
    public DateTimePicker(Context context)
	{
    	super(context);
    	 mDate = Calendar.getInstance();
    	 
         mHour=mDate.get(Calendar.HOUR_OF_DAY);
         //mMinute=mDate.get(Calendar.MINUTE);
    	 mMinute=0;
    	 inflate(context, R.layout.datedialog, this);
    	 
    	 mYearSpinner=(NumberPicker)this.findViewById(R.id.np_year);
    	 mYearSpinner.setMinValue(0);
    	 mYearSpinner.setMaxValue(6);
    	 updateYearControl();
    	 mYearSpinner.setOnValueChangedListener(mOnYearChangedListener);
    	 
    	 mDateSpinner=(NumberPicker)this.findViewById(R.id.np_date);
    	 mDateSpinner.setMinValue(0); 
         mDateSpinner.setMaxValue(6);
       
         updateDateControl();
    	 mDateSpinner.setOnValueChangedListener(mOnDateChangedListener);
    	 
    	 
    	 
    	 mHourSpinner=(NumberPicker)this.findViewById(R.id.np_hour);
    	 mHourSpinner.setMaxValue(23);
    	 mHourSpinner.setMinValue(0);
    	 mHourSpinner.setValue(mHour);
    	 mHourSpinner.setFormatter(this);
    	 mHourSpinner.setOnValueChangedListener(mOnHourChangedListener);
    	 
    	 mMinuteSpinner=(NumberPicker)this.findViewById(R.id.np_minute);
    	 mMinuteSpinner.setMaxValue(3);
    	 mMinuteSpinner.setMinValue(0);
    	 mMinuteSpinner.setValue(0);
    	 mMinuteSpinner.setDisplayedValues(new String[]{"00","15","30","45"});
    	 mMinuteSpinner.setFormatter(this);
    	 mMinuteSpinner.setOnValueChangedListener(mOnMinuteChangedListener);
    	 
    	 //初始化指定日期
    	 mMinute=Integer.valueOf(mMinuteSpinner.getDisplayedValues()[0].toString());
    	 mDate.set(Calendar.MINUTE, mMinute);
    	 onDateTimeChanged();
	}
    
    
    private OnValueChangeListener mOnYearChangedListener=new OnValueChangeListener()
   	{
   		@Override
   		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
   		{
   			
   			mDate.add(Calendar.YEAR, newVal - oldVal);
   		    
   		     mYearSpinner.setMaxValue(6);
   			 updateYearControl();
   			 onDateTimeChanged();
   		   
   			
   		}
   	};
       
    private OnValueChangeListener mOnDateChangedListener=new OnValueChangeListener()
	{
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
			mDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
			updateDateControl();
			onDateTimeChanged();
		}
	};
    
    private OnValueChangeListener mOnHourChangedListener=new OnValueChangeListener()
	{
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
			mHour=mHourSpinner.getValue();
			onDateTimeChanged();
		}
	};
	
	  private OnValueChangeListener mOnMinuteChangedListener=new OnValueChangeListener()
		{
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal)
			{
				mMinute=Integer.valueOf(mMinuteSpinner.getDisplayedValues()[newVal].toString());
				onDateTimeChanged();
			}
		};
	
	private void updateDateControl() 
    {
	 	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, -7 / 2 - 1);
        mDateSpinner.setDisplayedValues(null);
        for (int i = 0; i < 7; ++i) 
        {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            //mDateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal);
            mDateDisplayValues[i] = (String) DateFormat.format("MM-dd", cal);
        }
        mDateSpinner.setDisplayedValues(mDateDisplayValues);
        mDateSpinner.setValue(7 / 2);
        mDateSpinner.invalidate();
    }
	
	private void updateYearControl() 
    {
	 	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
       
          cal.add(Calendar.YEAR, -7 / 2 - 1);
          mYearSpinner.setDisplayedValues(null);
          
    	 for (int i = 0; i < 7; ++i) 
         {
             cal.add(Calendar.YEAR, 1);
             //mDateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal);
           
             mYearDisplayValues[i] = (String) DateFormat.format("yyyy", cal);
          
         }
    
         mYearSpinner.setDisplayedValues(mYearDisplayValues);
         mYearSpinner.setValue(7 / 2);
         mYearSpinner.invalidate();
          
       
    }
	  public interface OnDateTimeChangedListener 
	  {
	        void onDateTimeChanged(DateTimePicker view, int year, int month, int day, int hour, int minute);
	  }
	
	  public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) 
	  {
	        mOnDateTimeChangedListener = callback;
	   }
	  
	  private void onDateTimeChanged() 
	  {
	        if (mOnDateTimeChangedListener != null)
	        {
	            mOnDateTimeChangedListener.onDateTimeChanged(this, mDate.get(Calendar.YEAR),
	            		mDate.get(Calendar.MONTH), mDate.get(Calendar.DAY_OF_MONTH),mHour, mMinute);
	        }
	    }

	@Override
	public String format(int value){
		 String tmpStr = String.valueOf(value);
	       if (value < 10) {
	           tmpStr = "0" + tmpStr;
	       }
	       return tmpStr;
	}
	
	public Calendar getCalendar(){
		return mDate;
	}
}
