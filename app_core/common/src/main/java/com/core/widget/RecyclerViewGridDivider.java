package com.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * @author RaoMeng
 * @describe RecyclerView网格形式分割线
 * @date 2017/10/17
 */

public class RecyclerViewGridDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    /**
     * 是否需要绘制最后一行的底部
     */
    private boolean needLastRow = false;

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public RecyclerViewGridDivider(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
        try {
            mDivider = typedArray.getDrawable(0);
        } finally {
            typedArray.recycle();
        }
    }

    public RecyclerViewGridDivider(Context context, boolean needLastRow) {
        this(context);
        this.needLastRow = needLastRow;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontalLine(c, parent, state);
        drawVerticalLine(c, parent, state);
    }

    private void drawVerticalLine(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();

            int left = childView.getRight() + layoutParams.rightMargin;
            int top = childView.getTop() - layoutParams.topMargin;
            int right = left + mDivider.getIntrinsicWidth();
            int bottom = childView.getBottom() + layoutParams.bottomMargin;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private void drawHorizontalLine(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();

            int left = childView.getLeft() - layoutParams.leftMargin;
            int top = childView.getBottom() + layoutParams.bottomMargin;
            int right = childView.getRight() + layoutParams.rightMargin + mDivider.getIntrinsicWidth();
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private int getSpanCount(RecyclerView parent) {
        int spanCount = 0;

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }

        return spanCount;
    }

    /**
     * 判断是否是最后一列
     *
     * @param parent
     * @param position
     * @param spanCount
     * @param childCount
     * @return
     */
    private boolean isLastColum(RecyclerView parent, int position, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int orientation = ((GridLayoutManager) layoutManager).getOrientation();
            if (orientation == GridLayoutManager.HORIZONTAL) {
                if (position >= (childCount - (childCount % spanCount))) {
                    return true;
                }
            } else if (orientation == GridLayoutManager.VERTICAL) {
                if ((position + 1) % spanCount == 0) {
                    return true;
                }
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.HORIZONTAL) {
                if (position >= (childCount % spanCount)) {
                    return true;
                }
            } else if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((position + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是最后一行
     *
     * @param parent
     * @param position
     * @param spanCount
     * @param childCount
     * @return
     */
    private boolean isLastRow(RecyclerView parent, int position, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int orientation = ((GridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == GridLayoutManager.VERTICAL) {
                childCount = childCount - ((childCount % spanCount) == 0 ? spanCount : (childCount % spanCount));
                // 如果是最后一行，则不需要绘制底部
                if (position >= childCount) {
                    return true;
                }
            } else {
                // 如果是最后一行，则不需要绘制底部
                if ((position + 1) % spanCount == 0) {
                    return true;
                }
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - ((childCount % spanCount) == 0 ? spanCount : (childCount % spanCount));
                // 如果是最后一行，则不需要绘制底部
                if (position >= childCount) {
                    return true;
                }
            } else {
                // 如果是最后一行，则不需要绘制底部
                if ((position + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        boolean lastRow = isLastRow(parent, itemPosition, spanCount, childCount);
        boolean lastColum = isLastColum(parent, itemPosition, spanCount, childCount);

        outRect.set(0, 0, lastColum ? 0 : mDivider.getIntrinsicWidth()
                , ((!needLastRow) && lastRow) ? 0 : mDivider.getIntrinsicHeight());

    }
}
