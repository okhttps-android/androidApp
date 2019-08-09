package com.uas.appworks.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.widget.RecycleViewDivider;
import com.me.network.app.base.HttpParams;
import com.uas.appworks.R;
import com.uas.appworks.adapter.WorkModuleSortAddAdapter;
import com.uas.appworks.adapter.WorkModuleSortDeleteAdapter;
import com.uas.appworks.model.bean.WorkMenuBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/14 13:45
 */

public class WorkModuleSortActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView {
    private RecyclerView mAddRecyclerView, mDeleteRecyclerView;
    private List<WorkMenuBean> mAddWorkMenuBeans, mDeleteWorkMenuBeans;
    private WorkModuleSortAddAdapter mWorkModuleSortAddAdapter;
    private WorkModuleSortDeleteAdapter mWorkModuleSortDeleteAdapter;
    private TextView mAddTextView, mDeleteTextView;

    private RecyclerView.LayoutManager mLayoutManager;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_work_module_sort;
    }

    @Override
    protected void initView() {
       setTitle(R.string.work_module_custom);

        mAddRecyclerView = $(R.id.work_module_sort_add_rv);
        mDeleteRecyclerView = $(R.id.work_module_sort_delete_rv);
        mAddTextView = $(R.id.work_module_sort_add_tv);
        mDeleteTextView = $(R.id.work_module_sort_delete_tv);

        mAddWorkMenuBeans = new ArrayList<>();
        mDeleteWorkMenuBeans = new ArrayList<>();

        mWorkModuleSortAddAdapter = new WorkModuleSortAddAdapter(this, mAddWorkMenuBeans);
        mWorkModuleSortDeleteAdapter = new WorkModuleSortDeleteAdapter(this, mDeleteWorkMenuBeans);

        mLayoutManager = new LinearLayoutManager(this);
        mAddRecyclerView.setLayoutManager(mLayoutManager);
        mAddRecyclerView.setNestedScrollingEnabled(false);
        mAddRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAddRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light)));

        mLayoutManager = new LinearLayoutManager(this);
        mDeleteRecyclerView.setLayoutManager(mLayoutManager);
        mDeleteRecyclerView.setNestedScrollingEnabled(false);
        mDeleteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mDeleteRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light)));

        mAddRecyclerView.setAdapter(mWorkModuleSortAddAdapter);
        mDeleteRecyclerView.setAdapter(mWorkModuleSortDeleteAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {

        mWorkModuleSortAddAdapter.setOnItemDragListener(new WorkModuleSortAddAdapter.OnItemDragListener() {
            @Override
            public void onItemDrag(RecyclerView.ViewHolder viewHolder) {
                if (mItemTouchHelper != null) {
                    mItemTouchHelper.startDrag(viewHolder);
                }
            }
        });

        mWorkModuleSortAddAdapter.setOnDeleteItemClickListener(new WorkModuleSortAddAdapter.OnDeleteItemClickListener() {
            @Override
            public void onDeleteItemClick(View view, int position) {
                if (mAddWorkMenuBeans != null && mDeleteWorkMenuBeans != null && mAddWorkMenuBeans.size() > 0) {
                    mDeleteWorkMenuBeans.add(0, mAddWorkMenuBeans.get(position));
                    mAddWorkMenuBeans.remove(position);
                    mWorkModuleSortAddAdapter.notifyDataSetChanged();
                    mWorkModuleSortDeleteAdapter.notifyDataSetChanged();
                    visiableOrGone();
                }
            }
        });

        mWorkModuleSortDeleteAdapter.setOnAddItemClickListener(new WorkModuleSortDeleteAdapter.OnAddItemClickListener() {
            @Override
            public void onAddItemClick(View view, int position) {
                if (mDeleteWorkMenuBeans != null && mAddWorkMenuBeans != null && mDeleteWorkMenuBeans.size() > 0) {
                    mAddWorkMenuBeans.add(mDeleteWorkMenuBeans.get(position));
                    mDeleteWorkMenuBeans.remove(position);
                    mWorkModuleSortAddAdapter.notifyDataSetChanged();
                    mWorkModuleSortDeleteAdapter.notifyDataSetChanged();
                    visiableOrGone();
                }
            }
        });
    }

    @Override
    protected void initData() {
        mPresenter.uasRequest(this, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
    }

    @Override
    public void requestSuccess(int what, Object object) {
        if (what == Constants.LOAD_WORK_MENU_CACHE) {
            List<WorkMenuBean> menuTypeBeans = (List<WorkMenuBean>) object;
            for (int i = 0; i < menuTypeBeans.size(); i++) {
                WorkMenuBean workMenuBean = menuTypeBeans.get(i);
                if (workMenuBean.isModuleVisible()) {
                    mAddWorkMenuBeans.add(workMenuBean);
                } else {
                    mDeleteWorkMenuBeans.add(workMenuBean);
                }
            }

            mWorkModuleSortAddAdapter.notifyDataSetChanged();
            mWorkModuleSortDeleteAdapter.notifyDataSetChanged();

            visiableOrGone();

            mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    //首先回调的方法 返回int表示是否监听该方向
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    int fromPosition = viewHolder.getAdapterPosition();
                    int toPosition = target.getAdapterPosition();
                    if (fromPosition < toPosition) {
                        for (int i = fromPosition; i < toPosition; i++) {
                            Collections.swap(mAddWorkMenuBeans, i, i + 1);
                        }
                    } else {
                        for (int i = fromPosition; i > toPosition; i--) {
                            Collections.swap(mAddWorkMenuBeans, i, i - 1);
                        }
                    }

                    mWorkModuleSortAddAdapter.notifyItemMoved(fromPosition, toPosition);
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
                    return true;
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    //是否可拖拽
                    return true;
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                        viewHolder.itemView.setBackgroundColor(WorkModuleSortActivity.this.getResources().getColor(R.color.item_drag_color));
                        viewHolder.itemView.setScaleX(1.05f);
                        viewHolder.itemView.setScaleY(1.05f);
                    }
                    super.onSelectedChanged(viewHolder, actionState);
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    viewHolder.itemView.setBackgroundColor(WorkModuleSortActivity.this.getResources().getColor(R.color.white));
                    viewHolder.itemView.setScaleX(1.0f);
                    viewHolder.itemView.setScaleY(1.0f);
                }
            });
            mItemTouchHelper.attachToRecyclerView(mAddRecyclerView);
        }
    }

    private void visiableOrGone() {
        if (mWorkModuleSortAddAdapter.getWorkMenuBeans().size() == 0) {
            mAddTextView.setVisibility(View.GONE);
            mAddRecyclerView.setVisibility(View.GONE);
        } else {
            mAddTextView.setVisibility(View.VISIBLE);
            mAddRecyclerView.setVisibility(View.VISIBLE);
        }
        if (mWorkModuleSortDeleteAdapter.getWorkMenuBeans().size() == 0) {
            mDeleteTextView.setVisibility(View.GONE);
            mDeleteRecyclerView.setVisibility(View.GONE);
        } else {
            mDeleteTextView.setVisibility(View.VISIBLE);
            mDeleteRecyclerView.setVisibility(View.VISIBLE);
        }

        this.getRootView().postInvalidate();
    }

    @Override
    public void requestError(int what, String errorMsg) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.sort_complete) {
            item.setEnabled(false);
            List<WorkMenuBean> resultList = new ArrayList<>();
            List<WorkMenuBean> addWorkMenuBeans = mWorkModuleSortAddAdapter.getWorkMenuBeans();
            List<WorkMenuBean> deleteWorkMenuBeans = mWorkModuleSortDeleteAdapter.getWorkMenuBeans();
            if (addWorkMenuBeans != null) {
                resultList.addAll(addWorkMenuBeans);
            }
            if (deleteWorkMenuBeans != null) {
                resultList.addAll(deleteWorkMenuBeans);
            }
            String resultJson = JSON.toJSONString(resultList);
            CommonUtil.setUniqueSharedPreferences(this, Constants.WORK_MENU_CACHE, resultJson);
            toast(R.string.save_success);
            setResult(Constants.WORK_MODULE_SORT_REQUEST);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    item.setEnabled(true);
                    if (WorkModuleSortActivity.this == null
                            || WorkModuleSortActivity.this.isDestroyed()
                            || WorkModuleSortActivity.this.isFinishing()) {
                        return;
                    }
                    finish();
                }
            }, 1000);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
