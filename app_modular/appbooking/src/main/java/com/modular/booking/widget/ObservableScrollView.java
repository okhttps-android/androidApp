package com.modular.booking.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;

/**
 * 监听滑动事件
 * Created by Arison on 2017/10/17.
 */
public class ObservableScrollView extends ScrollView {
    
      
    /**
     * 回调接口监听事件
     */
    private OnObservableScrollViewListener mOnObservableScrollViewListener;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    
    public interface OnObservableScrollViewListener {
        void onObservableScrollViewListener(int l, int t, int oldl, int oldt);
    }

    /**
     * This is called in response to an internal scroll in this view (i.e., the
     * view scrolled its own contents). This is typically as a result of
     * {@link #scrollBy(int, int)} or {@link #scrollTo(int, int)} having been
     * called.
     *
     * @param l Current horizontal scroll origin. 当前滑动的x轴距离
     * @param t Current vertical scroll origin. 当前滑动的y轴距离
     * @param oldl Previous horizontal scroll origin. 上一次滑动的x轴距离
     * @param oldt Previous vertical scroll origin. 上一次滑动的y轴距离
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnObservableScrollViewListener != null) {
            //将监听到的数据向外抛
            mOnObservableScrollViewListener.onObservableScrollViewListener(l, t, oldl, oldt);
        }
    }
    
}
