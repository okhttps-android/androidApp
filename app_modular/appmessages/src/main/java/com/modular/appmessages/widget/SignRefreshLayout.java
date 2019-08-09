package com.modular.appmessages.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.modular.appmessages.R;
import com.modular.apputils.widget.EmptyRecyclerView;


/**
 * 1.通过代码setSignShow 弹出打卡头布局
 * Created by Bitlike on 2018/4/28.
 */

public class SignRefreshLayout extends ViewGroup {
    private boolean mEnablePullDown;// 是否允许下拉刷新
    private onRefreshListener mListener;// 事件监听接口

    private LayoutInflater mInflater;// 布局填充器对象
    private Scroller mLayoutScroller;  // 用于平滑滑动的Scroller对象
    private final int SCROLL_SPEED = 650;  // Scroller的滑动速度
    private int mReachBottomScroll; // 当滚动到内容最底部时Y轴所需要滑动的举例
    private int mEffectiveScroll; // 最小有效滑动距离(滑动超过该距离才视作一次有效的滑动刷新/加载操作)
    private int lastChildIndex;// 最后一个content-child-view的index
    private int mLayoutContentHeight; // ViewGroup的内容高度(不包括header与footer的高度)

    private View headerView; //头布局
    private final int headerHeight;

    public SignRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEffectiveScroll = (int) context.getResources().getDimension(R.dimen.refresh_item_height);
        // 实例化布局填充器
        mInflater = LayoutInflater.from(context);
        // 实例化Scroller
        mLayoutScroller = new Scroller(context);
        mEffectiveScroll = headerHeight = DisplayUtil.dip2px(context, 120);
        mEnablePullDown = true;
    }

    public void setEnablePullDown(boolean mEnablePullDown) {
        this.mEnablePullDown = mEnablePullDown;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        lastChildIndex = getChildCount() - 1;
        // 添加上拉刷新部分
        if (mEnablePullDown) {
            addLayoutHeader();
            yuyinCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    PreferenceUtils.putBoolean("signVoiceAble", b);
                }
            });
        }

    }

    private void addLayoutHeader() {
        headerView = getHeader(mInflater);
        // 通过LayoutInflater获取从布局文件中获取header的view对象
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, headerHeight);
        // 将Header添加进Layout当中
        addView(headerView, params);
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
                child.layout(0, -DisplayUtil.dip2px(getContext(), 120), child.getMeasuredWidth(), 0);
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
    protected final int SIGN_TRY_REFRESH = 3;
    protected final int SIGN_REFRESH = 4;


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
                    if (child instanceof RecyclerView) {
                        intercept = rvPullDownIntercept(child);
                    } else if (child instanceof ScrollView) {
                        intercept = svPullDownIntercept(child);
                    } else if (child instanceof EmptyRecyclerView) {
                        RecyclerView recycler = ((EmptyRecyclerView) child);
                        intercept = rvPullDownIntercept(recycler);
                    }
                } else if (y < mLastYIntercept) {//上划
                    View child = getChildAt(0);
                    if (status == SIGN_REFRESH) {
                        intercept = true;
                    } else {
                        intercept = false;
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
    private boolean rvPullUpIntercept(View child) {
        boolean intercept = false;
        RecyclerView recyclerChild = (RecyclerView) child;
        if (recyclerChild.computeVerticalScrollExtent() + recyclerChild.computeVerticalScrollOffset()
                >= recyclerChild.computeVerticalScrollRange())
            intercept = true;

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

    private boolean svPullDownIntercept(View child) {
        boolean intercept = false;
        if (child.getScrollY() <= 0) {
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


    private final float effectiveScrollMultiple = 2f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                // 计算本次滑动的Y轴增量(距离)
                int dy = mLastYMoved - y;
                // 如果滑动增量小于0，即下拉操作
                if (dy < 0) {//表示往下滑
                    if (mEnablePullDown) {
                        // 如果下拉的距离小于mLayoutHeader1/2的高度,则允许滑动
                        if (getScrollY() > 0 || Math.abs(getScrollY()) <= mEffectiveScroll * effectiveScrollMultiple) {
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
                        }
                    } else if (!isEnablePullDown() && Math.abs(getScrollY()) <= mEffectiveScroll) {
                        scrollBy(0, dy / 2);
                        if (Math.abs(getScrollY()) > mEffectiveScroll / 2) {
                            updateStatus(SIGN_REFRESH);
                        } else {
                            updateStatus(SIGN_TRY_REFRESH);
                        }
                    }
                } else if (dy > 0) {//上拉
                    if (mEnablePullDown) {
                        if (getScrollY() <= mReachBottomScroll + mEffectiveScroll * effectiveScrollMultiple) {
                            // 进行Y轴上的滑动
                            if (status != TRY_REFRESH && status != REFRESH) {
                                scrollBy(0, dy / 2);
                            } else {
                                if (getScrollY() <= 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy / 2);
                                    if (Math.abs(getScrollY()) < mEffectiveScroll)
                                        updateStatus(TRY_REFRESH);
                                }
                            }
                        }
                    } else if (!isEnablePullDown()) {
                        scrollBy(0, dy / 2);
                        if (Math.abs(getScrollY()) > mEffectiveScroll / 2) {
                            updateStatus(SIGN_REFRESH);
                        } else {
                            updateStatus(SIGN_TRY_REFRESH);
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
                    case NORMAL:
                        upWithStatusNormal();
                        break;
                    case TRY_REFRESH:
                        upWithStatusTryRefresh();
                        break;
                    case REFRESH:
                        upWithStatusRefresh();
                        break;
                    case SIGN_REFRESH:
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (-mEffectiveScroll)), SCROLL_SPEED);
                        break;
                    case SIGN_TRY_REFRESH:
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
                        mUIHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showSignView(false);
                                setEnablePullDown(true);
                            }
                        }, SCROLL_SPEED);
                        break;
                }
            }
        }
        mLastYIntercept = 0;
        postInvalidate();
        return true;
    }

    private void updateStatus(int status) {
        if (headerView != null) {
            upStatus(status);
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
            stopAnim();
        }
    }


    private void upWithStatusRefresh() {
        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (-mEffectiveScroll)), SCROLL_SPEED);
        if (headerView != null) {
            startAnim();
        }
        // 通过Listener接口执行刷新时的监听事件
        if (mListener != null) {
            mListener.onRefresh();
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

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_REFRESH: {
                    mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
                    status = NORMAL;
                    if (headerView != null) {
                        stopAnim();
                    }
                    break;
                }
            }
        }
    };


    private void stopTryRefresh() {
        Message msg = mUIHandler.obtainMessage(STOP_REFRESH);
        mUIHandler.sendMessage(msg);
    }


    public void stopRefresh() {
        if (status == REFRESH) {
            stopTryRefresh();
        }
        status = NORMAL;
    }

    public boolean isRefreshing() {
        return status == REFRESH;
    }


    public void setOnRefreshListener(onRefreshListener listener) {
        mListener = listener;
    }

    public interface onRefreshListener {
        void onRefresh();

    }

    private View signView;
    private AppCompatCheckBox yuyinCb;
    private View simplyView;
    private ImageView statusImg;
    private TextView statusTV;

    private void showSignView(boolean sign) {
        if (sign) {
            mEffectiveScroll = DisplayUtil.dip2px(getContext(), 120);
            simplyView.setVisibility(GONE);
            signView.setVisibility(VISIBLE);
        } else {
            mEffectiveScroll = DisplayUtil.dip2px(getContext(), 80);
            simplyView.setVisibility(VISIBLE);
            signView.setVisibility(GONE);
        }
    }


    public void setSignShow(boolean show) {
        if (!mLayoutScroller.isFinished()) {
            return;
        }
        if (isEnablePullDown() || show) {
            //显示打卡界面
            showSignView(true);
            mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (-mEffectiveScroll)), SCROLL_SPEED);
            setEnablePullDown(false);
            updateStatus(SIGN_REFRESH);
        } else {
            updateStatus(SIGN_TRY_REFRESH);
            mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
            mUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showSignView(false);
                    setEnablePullDown(true);
                }
            }, SCROLL_SPEED);
        }
    }

    public View getSignView() {
        return signView;
    }

    public boolean isEnablePullDown() {
        return simplyView != null && simplyView.getVisibility() == VISIBLE;
    }

    protected View getHeader(LayoutInflater mInflater) {
        View view = mInflater.inflate(R.layout.header_sign, null);
        signView = view.findViewById(R.id.signView);
        simplyView = view.findViewById(R.id.simplyView);
        yuyinCb = view.findViewById(R.id.yuyinCb);
        yuyinCb.setChecked(PreferenceUtils.getBoolean("signVoiceAble", true));
        initSimpleView();
        showSignView(false);
        return view;
    }

    private void initSimpleView() {
        statusImg = simplyView.findViewById(R.id.statusImg);
        statusTV = simplyView.findViewById(R.id.statusTV);

    }


    private void upStatus(int status) {
        statusImg.setAnimation(null);
        if (isEnablePullDown()) {
            switch (status) {
                case TRY_REFRESH:
                    statusTV.setText(R.string.pull_down_to_refresh);
                    statusImg.setImageResource(R.drawable.ic_refresh_pull_down);
                    break;
                case REFRESH:
                    statusTV.setText(R.string.up_to_refresh);
                    statusImg.setImageResource(R.drawable.ic_refresh_pull_up);
                    break;
            }
        }
    }

    private void stopAnim() {
        statusImg.setAnimation(null);
        statusImg.setImageResource(R.drawable.ic_refresh_pull_down);
        statusTV.setText(R.string.pull_down_to_refresh);

    }

    private void startAnim() {
        statusTV.setText(R.string.refreshing);
        statusImg.setImageResource(R.drawable.ic_baseutil_simple_load);
        statusImg.setAnimation(getImgAnimation());
    }

    private RotateAnimation rotate;

    private Animation getImgAnimation() {
        if (rotate == null) {
            rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            rotate.setInterpolator(lin);
            rotate.setDuration(1000);//设置动画持续时间
            rotate.setRepeatCount(-1);//设置重复次数
            rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            rotate.setStartOffset(10);//执行前的等待时间
        }
        return rotate;
    }

}
