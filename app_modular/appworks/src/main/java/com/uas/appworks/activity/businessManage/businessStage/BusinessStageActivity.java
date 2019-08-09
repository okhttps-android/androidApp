package com.uas.appworks.activity.businessManage.businessStage;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.system.DisplayUtil;
import com.core.base.activity.MvpBaseActivity;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.businessChangeStage.BusinessChangeStageActivity;
import com.uas.appworks.adapter.BusinessStageAdapter;
import com.uas.appworks.adapter.BusinessStageMenuAdapter;
import com.uas.appworks.model.bean.BusinessStageBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 商机阶段详情
 * @date 2018/9/12 17:53
 */
public class BusinessStageActivity extends MvpBaseActivity<BusinessStageContract.IBusinessStagePresenter>
        implements BusinessStageContract.IBusinessStageView {
    private final int FILL_BUSINESS_RECORD = 0x20;

    private RecyclerView mRecyclerView, mMenuRecyclerView;
    private List<BusinessStageBean> mBusinessStageBeans;
    private BusinessStageBean mCurrentBusinessStage;
    private BusinessStageAdapter mBusinessStageAdapter;
    private View mEmptyView;
    private TextView mEmptyText, mCancelTextView;
    private PopupWindow mMenuPopupWindow;
    private BusinessStageMenuAdapter mBusinessStageMenuAdapter;
    private String mStageCode;
    private String mBcCode, mBctype;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_stage;
    }

    @Override
    protected void initView() {
        setTitle("商机阶段详情");

        mRecyclerView = findViewById(R.id.business_stage_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBusinessStageBeans = new ArrayList<>();
        mBusinessStageAdapter = new BusinessStageAdapter(mBusinessStageBeans);
        mRecyclerView.setAdapter(mBusinessStageAdapter);

        initMenuPop();
    }

    private void initMenuPop() {
        View menuView = View.inflate(this, R.layout.pop_business_stage_menu, null);
        mMenuPopupWindow = new PopupWindow(menuView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        int screenHeigh = getResources().getDisplayMetrics().heightPixels;
        mMenuPopupWindow.setHeight(Math.round(screenHeigh * 0.5f));
        mMenuRecyclerView = menuView.findViewById(R.id.pop_business_stage_rv);
        mMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMenuRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        mBusinessStageMenuAdapter = new BusinessStageMenuAdapter(mBusinessStageBeans);
        mMenuRecyclerView.setAdapter(mBusinessStageMenuAdapter);

        mCancelTextView = menuView.findViewById(R.id.pop_business_stage_cancel_tv);

        mMenuPopupWindow.setOutsideTouchable(true);
        mMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mMenuPopupWindow.setAnimationStyle(R.style.MenuAnimationFade);
        mMenuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mMenuPopupWindow != null) {
                    mMenuPopupWindow.dismiss();
                }
                DisplayUtil.backgroundAlpha(mContext, 1f);
            }
        });
    }

    @Override
    protected BusinessStageContract.IBusinessStagePresenter initPresenter() {
        return new BusinessStagePresenterImpl();
    }

    @Override
    protected void initEvent() {
        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenuPop();
            }
        });

        mBusinessStageMenuAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                closeMenuPop();
                startActivityForResult(new Intent(mContext, BusinessChangeStageActivity.class)
                        .putExtra("nextStage", mBusinessStageBeans.get(position))
                        .putExtra("currentStage", mCurrentBusinessStage)
                        .putExtra("whichPage", "businessStage")
                        .putExtra("bc_code", mBcCode), FILL_BUSINESS_RECORD);
            }
        });
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            List<BusinessStageBean> stageBeans = (List<BusinessStageBean>) intent.getSerializableExtra("stageBeans");
            if (stageBeans != null) {
                mBusinessStageBeans.addAll(stageBeans);
            }
            mCurrentBusinessStage = (BusinessStageBean) intent.getSerializableExtra("currentStage");
            mStageCode = intent.getStringExtra("stageCode");
            mBcCode = intent.getStringExtra("bc_code");
            mBctype = intent.getStringExtra("bc_type");
        }

        if (mBusinessStageBeans == null || mBusinessStageBeans.size() == 0) {
            mPresenter.requestStageList(this);
        } else {
            int currentPos = -1;
            if (mBusinessStageBeans != null) {
                for (int i = 0; i < mBusinessStageBeans.size(); i++) {
                    BusinessStageBean businessStageBean = mBusinessStageBeans.get(i);
                    if (businessStageBean != null) {
                        try {
                            if (mCurrentBusinessStage.getBS_CODE().equals(businessStageBean.getBS_CODE())) {
                                currentPos = i;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
            mBusinessStageAdapter.setCurrentPos(currentPos);
            mBusinessStageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change_business_stage, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.change_business_stage) {
            if (mBusinessStageBeans.size() == 0) {
                toast("商机阶段为空，无法切换");
                return true;
            }
            mMenuPopupWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content)
                    , Gravity.BOTTOM, 0, 0);
            DisplayUtil.backgroundAlpha(mContext, 0.5f);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeMenuPop() {
        if (mMenuPopupWindow != null) {
            mMenuPopupWindow.dismiss();
        }
        DisplayUtil.backgroundAlpha(mContext, 1f);
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void requestStageSuccess(List<BusinessStageBean> businessStageBeans) {
        int currentPos = -1;
        if (businessStageBeans != null) {
            for (int i = 0; i < businessStageBeans.size(); i++) {
                BusinessStageBean businessStageBean = businessStageBeans.get(i);
                if (businessStageBean != null) {
                    if (mBctype != null && mBctype.equals(businessStageBean.getBS_TYPE())) {
                        mBusinessStageBeans.add(businessStageBean);
                        try {
                            if (mCurrentBusinessStage.getBS_CODE().equals(businessStageBean.getBS_CODE())) {
                                currentPos = mBusinessStageBeans.size() - 1;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }
        mBusinessStageAdapter.setCurrentPos(currentPos);
        mBusinessStageAdapter.notifyDataSetChanged();
        mBusinessStageMenuAdapter.notifyDataSetChanged();
        if (mBusinessStageBeans.size() == 0) {
            if (mEmptyView == null || mEmptyText == null) {
                mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
                mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
            }
            mEmptyText.setText("商机阶段为空");
            mBusinessStageAdapter.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void requestStageFail(String failStr) {
        if (mEmptyView == null || mEmptyText == null) {
            mEmptyView = View.inflate(mContext, R.layout.layout_commom_empty, null);
            mEmptyText = mEmptyView.findViewById(R.id.common_empty_tv);
        }
        mEmptyText.setText(failStr);
        mBusinessStageAdapter.setEmptyView(mEmptyView);
    }
}
