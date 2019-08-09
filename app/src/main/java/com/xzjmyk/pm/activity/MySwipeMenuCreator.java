package com.xzjmyk.pm.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;

/**
 * Created by PROD on 2016/9/1.
 */
public class MySwipeMenuCreator implements SwipeMenuCreator {
    private Context mContext;

    public MySwipeMenuCreator(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void create(SwipeMenu menu) {
        switch (menu.getViewType()){
            case 0:
//                menu.removeMenuItem(deleteItem);
                break;
            case -1:
                SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
                deleteItem.setBackground(new ColorDrawable(mContext.getResources().getColor(R.color.red)));
                deleteItem.setTitleColor(mContext.getResources().getColor(R.color.white));
                deleteItem.setTitleSize(15);
                deleteItem.setTitle("取消订阅");
                deleteItem.setWidth(dp2px(100));

                menu.addMenuItem(deleteItem);
                break;
        }

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                mContext.getResources().getDisplayMetrics());
    }
}
