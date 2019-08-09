package com.core.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewInScroller extends ListView {

	public ListViewInScroller(Context context) {
		super(context);
	}
	
	public ListViewInScroller(Context context, AttributeSet attrs,
                              int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ListViewInScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**注释：解决只显示一行的问题 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 int expandSpec = MeasureSpec.makeMeasureSpec(   
	                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);   
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
