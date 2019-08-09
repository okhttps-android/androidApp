package com.module.recyclerlibrary.ui.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.module.recyclerlibrary.R;


public abstract class BaseRefreshLayout extends ViewGroup {
    private boolean mEnablePullDown;// 是否允许下拉刷新
    private boolean mEnablePullUp;// 是否允许上拉加载
    private Drawable mPullBgDrawable = null;// 拉动部分背景(color|drawable)
    private onRefreshListener mListener;// 事件监听接口

    private LayoutInflater mInflater;// 布局填充器对象
    private Scroller mLayoutScroller;  // 用于平滑滑动的Scroller对象
    private final int SCROLL_SPEED = 650;  // Scroller的滑动速度
    private int mReachBottomScroll; // 当滚动到内容最底部时Y轴所需要滑动的举例
    private int mEffectiveScroll; // 最小有效滑动距离(滑动超过该距离才视作一次有效的滑动刷新/加载操作)
    private int lastChildIndex;// 最后一个content-child-view的index
    private int mLayoutContentHeight; // ViewGroup的内容高度(不包括header与footer的高度)

    private BaseRefreshView headerView; //头布局
    private BaseRefreshView footView; //尾布局

    public BaseRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.BaseRefreshLayout);
        try {
            mEnablePullDown = array.getBoolean(R.styleable.BaseRefreshLayout_enablePullDown, true);
            mEnablePullUp = array.getBoolean(R.styleable.BaseRefreshLayout_enablePullUp, true);
            mPullBgDrawable = array.getDrawable(R.styleable.BaseRefreshLayout_pullBackground);
            mEffectiveScroll = (int) array.getDimension(R.styleable.BaseRefreshLayout_fectiveScroll, dip2px(context, -1));
            if (mEffectiveScroll <= 0) {
                mEffectiveScroll = (int) context.getResources().getDimension(R.dimen.refresh_item_height);
            }
        } finally {
            array.recycle();
        }
        // 实例化布局填充器
        mInflater = LayoutInflater.from(context);
        // 实例化Scroller
        mLayoutScroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        lastChildIndex = getChildCount() - 1;
        // 添加上拉刷新部分
        if (mEnablePullDown)
            addLayoutHeader();
        // 添加下拉加载部分
        if (mEnablePullUp)
            addLayoutFooter();
    }

    private void addLayoutHeader() {
        headerView = getHeader(mInflater);
        // 通过LayoutInflater获取从布局文件中获取header的view对象
        if (mPullBgDrawable != null)
            headerView.setBackgroundDrawable(mPullBgDrawable);
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        // 将Header添加进Layout当中
        addView(headerView, params);
    }


    /**
     * 添加下拉加载布局作为footer
     */
    private void addLayoutFooter() {
        // 通过LayoutInflater获取从布局文件中获取footer的view对象
        footView = getFooter(mInflater);
        if (mPullBgDrawable != null)
            footView.setBackgroundDrawable(mPullBgDrawable);
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        // 将footer添加进Layout当中
        addView(footView, params);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 遍历进行子视图的测量工作
        for (int i = 0; i < getChildCount(); i++) {
            // 通知子视图进行测量
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 重置(避免重复累加)
        mLayoutContentHeight = 0;
        // 遍历进行子视图的置位工作
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == headerView) { // 头视图隐藏在ViewGroup的顶端
                child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
            } else if (child == footView) { // 尾视图隐藏在ViewGroup所有内容视图之后
                child.layout(0, mLayoutContentHeight, child.getMeasuredWidth(), mLayoutContentHeight + child.getMeasuredHeight());
            } else { // 内容视图根据定义(插入)顺序,按由上到下的顺序在垂直方向进行排列
                child.layout(0, mLayoutContentHeight, child.getMeasuredWidth(), mLayoutContentHeight + child.getMeasuredHeight());
                if (index <= lastChildIndex) {
                    if (child instanceof ScrollView) {
                        mLayoutContentHeight += getMeasuredHeight();
                        continue;
                    }
                    mLayoutContentHeight += child.getMeasuredHeight();
                }
            }
        }
        // 计算到达内容最底部时ViewGroup的滑动距离
        mReachBottomScroll = mLayoutContentHeight - getMeasuredHeight();
    }


    // 普通状态
    protected final int NORMAL = 0;
    // 意图刷新
    protected final int TRY_REFRESH = 1;
    // 刷新状态
    protected final int REFRESH = 2;
    // 意图加载
    protected final int TRY_LOAD_MORE = 3;
    // 加载状态
    protected final int LOAD_MORE = 4;
    // Layout状态
    private int status = NORMAL;
    // 用于计算滑动距离的Y坐标中介
    private int mLastYMoved, mDownYMoved;
    // 用于判断是否拦截触摸事件的Y坐标中介
    private int mLastYIntercept;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        // 记录此次触摸事件的y坐标
        int y = (int) event.getY();
        // 判断触摸事件类型
        switch (event.getAction()) {
            // Down事件
            case MotionEvent.ACTION_DOWN: {
                // 记录下本次系列触摸事件的起始点Y坐标
                mDownYMoved = mLastYMoved = y;
                // 不拦截ACTION_DOWN，因为当ACTION_DOWN被拦截，后续所有触摸事件都会被拦截
                intercept = false;
                break;
            }
            // Move事件
            case MotionEvent.ACTION_MOVE: {
                if (y > mLastYIntercept && mEnablePullDown) { // 下滑操作
                    // 获取最顶部的子视图
                    View child = getChildAt(0);
                    if (child instanceof AdapterView) {
                        intercept = avPullDownIntercept(child);
                    } else if (child instanceof ScrollView) {
                        intercept = svPullDownIntercept(child);
                    } else if (child instanceof RecyclerView) {
                        intercept = rvPullDownIntercept(child);
                    }else if(child instanceof EmptyRecyclerView){
                        RecyclerView recycler= (( EmptyRecyclerView)child).getRecyclerView();
                        intercept = rvPullDownIntercept(recycler);
                    }  else if (child instanceof WebView) {
                        intercept = webViewPullDownIntercept(child);
                    }
                } else if (y < mLastYIntercept && mEnablePullUp) { // 上拉操作
                    // 获取最底部的子视图
                    View child = getChildAt(lastChildIndex);
                    if (child instanceof AdapterView) {
                        intercept = avPullUpIntercept(child);
                    } else if (child instanceof ScrollView) {
                        intercept = svPullUpIntercept(child);
                    } else if (child instanceof RecyclerView) {
                        intercept = rvPullUpIntercept(child);
                    }else if(child instanceof EmptyRecyclerView){
                        RecyclerView recycler= (( EmptyRecyclerView)child).getRecyclerView();
                        intercept = rvPullUpIntercept(recycler);
                    }
                } else {
                    intercept = false;
                }
                break;
            }
            // Up事件
            case MotionEvent.ACTION_UP: {
                intercept = false;
                break;
            }
        }
        mLastYIntercept = y;
        return intercept;
    }

    private boolean avPullDownIntercept(View child) {
        boolean intercept = true;
        AdapterView adapterChild = (AdapterView) child;
        // 判断AbsListView是否已经到达内容最顶部
        if (adapterChild.getFirstVisiblePosition() != 0
                || adapterChild.getChildAt(0).getTop() != 0) {
            // 如果没有达到最顶端，则仍然将事件下放
            intercept = false;
        }
        return intercept;
    }

    private boolean avPullUpIntercept(View child) {
        boolean intercept = false;
        AdapterView adapterChild = (AdapterView) child;

        // 判断AbsListView是否已经到达内容最底部
        if (adapterChild.getLastVisiblePosition() == adapterChild.getCount() - 1
                && (adapterChild.getChildAt(adapterChild.getChildCount() - 1).getBottom() == getMeasuredHeight())) {
            // 如果到达底部，则拦截事件
            intercept = true;
        }
        return intercept;
    }

    private boolean svPullDownIntercept(View child) {
        boolean intercept = false;
        if (child.getScrollY() <= 0) {
            intercept = true;
        }
        return intercept;
    }

    private boolean svPullUpIntercept(View child) {
        boolean intercept = false;
        ScrollView scrollView = (ScrollView) child;
        View scrollChild = scrollView.getChildAt(0);

        if (scrollView.getScrollY() >= (scrollChild.getHeight() - scrollView.getHeight())) {
            intercept = true;
        }
        return intercept;
    }

    private boolean webViewPullDownIntercept(View child) {
        boolean intercept = false;
        WebView recyclerChild = (WebView) child;
        if (recyclerChild.getScrollY() == 0) {
            intercept = true;
        }
        return intercept;
    }

    private boolean rvPullDownIntercept(View child) {
        boolean intercept = false;

        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollOffset() <= 0)
            intercept = true;

        return intercept;
    }

    private boolean rvPullUpIntercept(View child) {
        boolean intercept = false;
        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollExtent() + recyclerChild.computeVerticalScrollOffset()
                >= recyclerChild.computeVerticalScrollRange())
            intercept = true;

        return intercept;
    }

    private final float effectiveScrollMultiple = 2.3f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                // 计算本次滑动的Y轴增量(距离)
                int dy = mLastYMoved - y;
                // 如果滑动增量小于0，即下拉操作
                if (dy < 0) {//表示往下滑
                    if (mEnablePullDown || (mDownYMoved - y > 0 && mEnablePullUp)) {
                        // 如果下拉的距离小于mLayoutHeader1/2的高度,则允许滑动
                        if (getScrollY() > 0 || Math.abs(getScrollY()) <= mEffectiveScroll * effectiveScrollMultiple) {
                            if (status != TRY_LOAD_MORE && status != LOAD_MORE) {
                                scrollBy(0, dy / 2);
                                if (status != REFRESH) {
                                    if (getScrollY() <= 0) {
                                        if (status != TRY_REFRESH) {
                                            updateStatus(TRY_REFRESH);
                                        } else if (Math.abs(getScrollY()) > mEffectiveScroll) {
                                            updateStatus(REFRESH);
                                        }
                                    }
                                }
                            } else {
                                if (getScrollY() > 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy / 2);
                                    if (getScrollY() < mReachBottomScroll + mEffectiveScroll) {
                                        updateStatus(TRY_LOAD_MORE);
                                    }
                                }
                            }
                        }
                    }
                } else if (dy > 0) {
                    if (mEnablePullUp || (mDownYMoved - y < 0 && mEnablePullDown)) {
                        if (getScrollY() <= mReachBottomScroll + mEffectiveScroll * effectiveScrollMultiple) {
                            // 进行Y轴上的滑动
                            if (status != TRY_REFRESH && status != REFRESH) {
                                scrollBy(0, dy / 2);
                                if (status != LOAD_MORE) {
                                    if (getScrollY() >= mReachBottomScroll) {
                                        if (status != TRY_LOAD_MORE)
                                            updateStatus(TRY_LOAD_MORE);

                                        if (getScrollY() >= mReachBottomScroll + mEffectiveScroll)
                                            updateStatus(LOAD_MORE);
                                    }
                                }
                            } else {
                                if (getScrollY() <= 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy / 2);
                                    if (Math.abs(getScrollY()) < mEffectiveScroll)
                                        updateStatus(TRY_REFRESH);
                                }
                            }
                        }
                    }
                }
                // 记录y坐标
                mLastYMoved = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                // 判断本次触摸系列事件结束时,Layout的状态
                switch (status) {
                    case NORMAL: {
                        upWithStatusNormal();
                        break;
                    }
                    case TRY_REFRESH: {
                        upWithStatusTryRefresh();
                        break;
                    }

                    case REFRESH: {
                        upWithStatusRefresh();
                        break;
                    }

                    case TRY_LOAD_MORE: {
                        upWithStatusTryLoadMore();
                        break;
                    }

                    case LOAD_MORE: {
                        upWithStatusLoadMore();
                        break;
                    }
                }
            }
        }
        mLastYIntercept = 0;
        postInvalidate();
        return true;
    }

    private void updateStatus(int status) {
        if (footView != null) {
            footView.upStatus(status);
        }
        if (headerView != null) {
            headerView.upStatus(status);
        }
        if (status != NORMAL) {
            this.status = status;
        }
    }

    private void upWithStatusNormal() {

    }

    private void upWithStatusTryRefresh() {
        // 取消本次的滑动
        mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
        status = NORMAL;
        if (headerView != null) {
            headerView.stopAnim();
        }
    }


    private void upWithStatusRefresh() {
        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (-mEffectiveScroll)), SCROLL_SPEED);
        if (headerView != null) {
            headerView.startAnim();
        }
        // 通过Listener接口执行刷新时的监听事件
        if (mListener != null) {
            mListener.onRefresh();
        }
    }

    private void upWithStatusTryLoadMore() {
        //取消加载
        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - mReachBottomScroll), SCROLL_SPEED);
        status = NORMAL;
        if (footView != null) {
            footView.stopAnim();
        }
    }


    private void upWithStatusLoadMore() {
        //加载
        mLayoutScroller.startScroll(0, getScrollY(), 0, -((getScrollY() - mEffectiveScroll) - mReachBottomScroll), SCROLL_SPEED);
        if (footView != null) {
            footView.startAnim();
        }
        // 通过Listener接口执行加载时的监听事件
        if (mListener != null) {
            mListener.onLoadMore();
        }
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mLayoutScroller.computeScrollOffset()) {
            scrollTo(0, mLayoutScroller.getCurrY());
        }
        postInvalidate();
    }

    private final int STOP_REFRESH = 1;
    private final int STOP_LOAD_MORE = 2;

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_REFRESH: {
                    mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
                    status = NORMAL;
                    if (headerView != null) {
                        headerView.stopAnim();
                    }
                    break;
                }
                case STOP_LOAD_MORE: {
                    mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - mReachBottomScroll), SCROLL_SPEED);
                    status = NORMAL;
                    if (footView != null) {
                        footView.stopAnim();
                    }
                    break;
                }
            }
        }
    };

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void stopTryRefresh() {
        Message msg = mUIHandler.obtainMessage(STOP_REFRESH);
        mUIHandler.sendMessage(msg);
    }

    private void stopTryLoadMore() {
        Message msg = mUIHandler.obtainMessage(STOP_LOAD_MORE);
        mUIHandler.sendMessage(msg);
    }

    public void stopRefresh() {
        if (status == LOAD_MORE) {
            stopTryLoadMore();
        } else if (status == REFRESH) {
            stopTryRefresh();
        }
        status = NORMAL;
    }

    public boolean isRefreshing() {
        return status == LOAD_MORE || status == REFRESH;
    }

    public void setEnabledPullUp(boolean enabledPullUp) {
        this.mEnablePullUp = enabledPullUp;
        if (!mEnablePullUp) {
            if (footView != null && footView.isShown())
                footView.setVisibility(View.GONE);
        }
    }

    public void setEnablePullDown(boolean enablePullDown) {
        this.mEnablePullDown = enablePullDown;
        if (!this.mEnablePullDown) {
            if (headerView != null && headerView.isShown())
                headerView.setVisibility(View.GONE);
        }
    }

    public void setOnRefreshListener(onRefreshListener listener) {
        mListener = listener;
    }

    public interface onRefreshListener {
        void onRefresh();

        void onLoadMore();
    }


    protected abstract BaseRefreshView getHeader(LayoutInflater mInflater);

    protected abstract BaseRefreshView getFooter(LayoutInflater mInflater);


}
