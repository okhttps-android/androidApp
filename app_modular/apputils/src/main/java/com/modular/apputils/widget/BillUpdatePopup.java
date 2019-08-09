package com.modular.apputils.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.modular.apputils.R;
import com.modular.apputils.adapter.BillUpdateAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.utils.RecyclerItemDecoration;

import java.util.List;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.InputMethodUtils;

/**
 * @author RaoMeng
 * @describe
 * @date 2019/1/6 14:57
 */
public class BillUpdatePopup extends BasePopupWindow implements View.OnClickListener, BillUpdateAdapter.OnAdapterListener {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private TextView mCancelTextView, mConfirmTextView;
    private BillUpdateAdapter mBillUpdateAdapter;
    private OnUpdateSelectListener mOnUpdateSelectListener;

    public BillUpdatePopup(Context context, OnUpdateSelectListener onUpdateSelectListener) {
        super(context);
        mContext = context;
        this.mOnUpdateSelectListener = onUpdateSelectListener;

        setAllowDismissWhenTouchOutside(false);
//        setBlurBackgroundEnable(true);

        initViews(context);

        mCancelTextView.setOnClickListener(this);
        mConfirmTextView.setOnClickListener(this);
    }

    private void initViews(Context context) {
        mRecyclerView = findViewById(R.id.pop_bill_input_update_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));

        mCancelTextView = findViewById(R.id.pop_bill_input_update_cancel_tv);
        mConfirmTextView = findViewById(R.id.pop_bill_input_update_confirm_tv);
    }

    public List<BillGroupModel> getGroupModels() {
        return mBillUpdateAdapter.getBillGroupModels();
    }

    public BillGroupModel getBillGroupModel(int groupIndex) {
        return mBillUpdateAdapter.getBillGroupModel(groupIndex);
    }

    public void notifyDataSetChanged() {
        mBillUpdateAdapter.notifyDataSetChanged();
    }

    public BillUpdatePopup setGroupModels(List<BillGroupModel> groupModels) {
        if (groupModels != null) {
            if (mBillUpdateAdapter == null) {
                mBillUpdateAdapter = new BillUpdateAdapter(mContext, groupModels, this);
                mRecyclerView.setAdapter(mBillUpdateAdapter);
            } else {
                mBillUpdateAdapter.setBillGroupModels(groupModels);
                mBillUpdateAdapter.notifyDataSetChanged();
            }
        }
        return this;
    }

    public BillUpdateAdapter getBillUpdateAdapter() {
        return mBillUpdateAdapter;
    }

    public void updateBillModelValues(int position, String values, String display) {
        mBillUpdateAdapter.updateBillModelValues(position, values, display);
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.pop_bill_input_update);
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return getDefaultScaleAnimation(true);
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return getDefaultScaleAnimation(false);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.pop_bill_input_update_cancel_tv) {
            InputMethodUtils.close(mRecyclerView);
            dismiss();
        } else if (i == R.id.pop_bill_input_update_confirm_tv) {
            if (mOnUpdateSelectListener != null) {
                mOnUpdateSelectListener.onUpdateConfirm(mBillUpdateAdapter.getBillGroupModels(),mBillUpdateAdapter.getUpdateBillModels());
            }
        } else {
        }
    }

    @Override
    public void toSelect(int position, BillGroupModel.BillModel model) {
        if (mOnUpdateSelectListener != null) {
            mOnUpdateSelectListener.onUpdateSelect(position, model);
        }
    }

    @Override
    public void toEnclosureSelect(int position, BillGroupModel.BillModel model) {
        if (mOnUpdateSelectListener != null) {
            mOnUpdateSelectListener.onUpdateEnclosure(position, model);
        }
    }

    public interface OnUpdateSelectListener {
        void onUpdateSelect(int position, BillGroupModel.BillModel model);

        void onUpdateEnclosure(int position, BillGroupModel.BillModel model);

        void onUpdateConfirm(List<BillGroupModel> billGroupModels,List<BillGroupModel.BillModel> updateBillModels);
    }
}
