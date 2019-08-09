package com.module.recyclerlibrary.ui.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.module.recyclerlibrary.R;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


/**
 * Created by Bitlike on 2018/2/28.
 */

public class EmptyRecyclerView extends FrameLayout {
    private View emptyView;
    private RecyclerView mRecyclerView;
    private int emptyImage;
    private String emptyText;
    private GifDrawable mGifDrawable;
    private Context mContext;

    public EmptyRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public EmptyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mRecyclerView = new RecyclerView(context, attrs, defStyleAttr) {
            @Override
            public void setAdapter(Adapter adapter) {
                final Adapter oldAdapter = getAdapter();
                if (oldAdapter != null) {
                    oldAdapter.unregisterAdapterDataObserver(observer);
                }
                super.setAdapter(adapter);
                if (adapter != null) {
                    adapter.registerAdapterDataObserver(observer);
                }
                checkIfEmpty();
            }
        };


        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.EmptyRecyclerView);
        int emptyView = R.layout.common_empty_view;
        try {
            this.emptyText = array.getString(R.styleable.EmptyRecyclerView_emptyText);
            this.emptyImage = array.getResourceId(R.styleable.EmptyRecyclerView_emptyImage, R.drawable.gif_empty_view);
            emptyView = array.getResourceId(R.styleable.EmptyRecyclerView_emptyLayout, R.layout.common_empty_view);
        } finally {
            array.recycle();
        }

        initEmptyView(context, emptyView);
    }

    private void initEmptyView(Context context, int reId) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        emptyView = mInflater.inflate(reId, null);
        post(new Runnable() {
            @Override
            public void run() {
                addView(emptyView);
                addView(mRecyclerView);
                checkIfEmpty();
            }
        });
    }

    //设置没有内容时，提示用户的空布局
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public View getEmptyView() {
        return emptyView;
    }

    private final RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };


    private GifImageView emptyImg;
    private TextView emptyTv;

    private void checkIfEmpty() {
        if (emptyView != null && mRecyclerView.getAdapter() != null) {
            final boolean emptyViewVisible =
                    mRecyclerView.getAdapter().getItemCount() == 0;
            if (emptyViewVisible) {
                mRecyclerView.setVisibility(GONE);
                emptyView.setVisibility(VISIBLE);

                if (emptyImg == null) {
                    emptyImg = emptyView.findViewById(R.id.emptyImg);
                    if (emptyImage > 0) {
                        try {
                            mGifDrawable = new GifDrawable(mContext.getResources(), emptyImage);
                            emptyImg.setImageDrawable(mGifDrawable);
                        } catch (IOException e) {
                            emptyImg.setImageResource(emptyImage);
                        }
                    }
                }
                if (emptyTv == null) {
                    emptyTv = emptyView.findViewById(R.id.emptyTv);
                    if (!TextUtils.isEmpty(emptyText)) {
                        emptyTv.setText(emptyText);
                    }
                }
            } else {
                mRecyclerView.setVisibility(VISIBLE);
                emptyView.setVisibility(GONE);
            }
        } else {
            mRecyclerView.setVisibility(VISIBLE);
            emptyView.setVisibility(GONE);
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
        }
        state = null;
    }
}
