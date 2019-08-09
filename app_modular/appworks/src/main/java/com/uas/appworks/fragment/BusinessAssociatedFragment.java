package com.uas.appworks.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.core.base.fragment.MvpBaseFragment;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.businessMineList.BusinessMineListContract;
import com.uas.appworks.activity.businessManage.businessMineList.BusinessMineListPresenterImpl;
import com.uas.appworks.adapter.BusinessMineAdapter;
import com.uas.appworks.model.bean.BusinessMineChildBean;
import com.uas.appworks.model.bean.CommonColumnsBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/24 14:07
 */
public class BusinessAssociatedFragment extends MvpBaseFragment<BusinessMineListContract.IBusinessMineListPresenter>
        implements BusinessMineListContract.IBusinessMineListView {

    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private TextView mEmptyText;
    private List<BusinessMineChildBean> mBusinessMineBeans;
    private BusinessMineAdapter mBusinessMineAdapter;
    private String mCaller;
    private String mCondition = "1 = 1";
    private int mPageIndex = 1;
    private int mPageSize = 100;

    @Override
    protected int getLayout() {
        return R.layout.fragment_business_associated;
    }

    @Override
    protected BusinessMineListContract.IBusinessMineListPresenter initPresenter() {
        return new BusinessMineListPresenterImpl();
    }

    @Override
    protected void initViews() {

        mRecyclerView = $(R.id.business_associated_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void initDatas() {

    }

    @Override
    public void requestListSuccess(List<BusinessMineChildBean> businessMineBeans, List<CommonColumnsBean> commonColumnsBeans) {

    }

    @Override
    public void requestListFail(String failMsg) {

    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}
