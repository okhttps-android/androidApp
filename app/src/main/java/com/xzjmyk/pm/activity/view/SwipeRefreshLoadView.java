package com.xzjmyk.pm.activity.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Bitliker on 2016/11/21.
 */

public class SwipeRefreshLoadView extends SwipeRefreshLayout {

    private RecyclerView recycler;

    public SwipeRefreshLoadView(Context context) {
        super(context);
    }

    public SwipeRefreshLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            if (recycler != null) return;
            // 判断容器有多少个孩子
            if (getChildCount() > 0) {
                // 判断第一个孩子是不是ListView
                if (getChildAt(0) instanceof ListView) {
                    // 创建ListView对象
                    recycler = (RecyclerView) getChildAt(0);
                    // 设置ListView的滑动监听
                    setListViewOnScroll();
                }
            }
        }
    }
    /**
     * 设置ListView的滑动监听
     */
    private void setListViewOnScroll() {
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }


        });





//        recycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView,
//                                             int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE
//                        && lastVisibleItem + 1 == adapter.getItemCount()) {
//                    mSwipeRefreshWidget.setRefreshing(true);
//                    // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
//                    handler.sendEmptyMessageDelayed(0, 3000);
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
//            }
//        });




    }
}
