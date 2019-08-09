package com.module.recyclerlibrary.listener;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Bitliker on 2017/9/18.
 */

public abstract class OnRecyclerClickLister implements RecyclerView.OnItemTouchListener {

    private GestureDetectorCompat mGestureDetectorCompat;
    private RecyclerView mRecyclerView;

    public OnRecyclerClickLister(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mGestureDetectorCompat = new GestureDetectorCompat(mRecyclerView.getContext(),
                new GestureListener());
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        //触摸事件
        mGestureDetectorCompat.onTouchEvent(e);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //拦截事件
        mGestureDetectorCompat.onTouchEvent(e);
        return false;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //是否拦截触摸时间
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            View childe = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (childe != null) {
                RecyclerView.ViewHolder VH = mRecyclerView.getChildViewHolder(childe);
                onItemClick(VH);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            View childe = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (childe != null) {
                RecyclerView.ViewHolder VH = mRecyclerView.getChildViewHolder(childe);
                onItemLongClick(VH);
            }
        }
    }


    public abstract void onItemLongClick(RecyclerView.ViewHolder vh);

    public abstract void onItemClick(RecyclerView.ViewHolder vh);
}
