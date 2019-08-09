/**
 * 
 */
package com.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * @author LiuJie
 *
 */
public class NScrollerGridView extends GridView {

	
	 /**
	 * @param context
	 */
	public NScrollerGridView(Context context) {
		super(context);
	}
	
	

	public NScrollerGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}



	public NScrollerGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}



	/* (non-Javadoc)
	 * @see android.widget.GridView#onMeasure(int, int)
	 */
	/**注释：解决只显示一行的问题 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 int expandSpec = MeasureSpec.makeMeasureSpec(   
	                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);   
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
