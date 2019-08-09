package com.common.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Bitliker on 2017/8/21.
 */
public class ViewUtils {
    /**
     * android 动态设置控件长度宽度
     *
     * @param view   控件对象
     * @param width
     * @param height
     */
    public static void setLayoutHandW(View view, int width, int height) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        linearParams.width = width;
        linearParams.height = height;
        view.setLayoutParams(linearParams);
    }

    public static void textSpanForStyle(
            TextView view,
            String input,
            String match,
            int color) {
        SpannableStringBuilder style = new SpannableStringBuilder(input);
        Pattern highlight = Pattern.compile(match);
        Matcher m = highlight.matcher(style.toString());
        while (m.find()) {
//            style.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            style.setSpan(new ForegroundColorSpan(color), m.start(), m.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new StrikethroughSpan(), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            style.setSpan(new UnderlineSpan(), m.start(), m.end(),
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        view.setText(style);
    }


    public static void move2PositionSmooth(LinearLayoutManager manager, RecyclerView mRecyclerView, int position) {
        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        if (position <= firstItem) {
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            int top = mRecyclerView.getChildAt(position - firstItem).getTop();
            mRecyclerView.scrollBy(0, top);
        } else {
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    public static void move2Position(LinearLayoutManager manager, RecyclerView mRecyclerView, int position) {
        if (position < 0) return;
        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        if (position <= firstItem) {
            mRecyclerView.scrollToPosition(position);
        } else if (position <= lastItem) {
            int top = mRecyclerView.getChildAt(position - firstItem).getTop();
            mRecyclerView.scrollBy(0, top);
        } else {
            mRecyclerView.scrollToPosition(position);
        }
    }
}
