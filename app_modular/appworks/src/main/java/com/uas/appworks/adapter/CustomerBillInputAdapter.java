package com.uas.appworks.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.MyGridView;
import com.core.widget.view.SwitchView;
import com.me.imageloader.ImageLoaderUtil;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.utils.BillTypeChangeUtils;
import com.modular.apputils.widget.VeriftyDialog;
import com.uas.appworks.R;

import java.util.List;

public class CustomerBillInputAdapter extends BillAdapter {

    private CompanyClickListener mCompanyClickListener;
    private boolean addContact = false;//保存后继续新增联系人

    public CustomerBillInputAdapter(Context ct, List<BillGroupModel> mBillGroupModels,
                                    BillAdapter.OnAdapterListener mOnAdapterListener,
                                    CompanyClickListener mCompanyClickListener) {
        super(ct, mBillGroupModels, mOnAdapterListener);
        this.mCompanyClickListener = mCompanyClickListener;
    }

    public boolean isAddContact() {
        return addContact;
    }


    @Override
    public int getItemViewType(int position) {
        if (ListUtils.getSize(getShowBillModels()) > position) {
            return BillTypeChangeUtils.getItemViewType(getShowBillModels().get(position).getType());
        } else {
            return 220;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public BillAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 220) {
            return new CustomerBillInputAdapter.AddcontentViewHolder(parent, R.layout.item_customer_bill_add_contact);
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    class AddcontentViewHolder extends BillAdapter.BaseViewHolder {
        private SwitchView saveContactSv;

        public AddcontentViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }

        @Override
        public void initView(View view) {
            saveContactSv = (SwitchView) view.findViewById(R.id.saveContactSv);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull CustomerBillInputAdapter.BaseViewHolder holder, int position) {
        if (holder instanceof AddcontentViewHolder) {
            bindAddContentView((AddcontentViewHolder) holder);
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    public void bindAddContentView(AddcontentViewHolder holder) {
        holder.saveContactSv.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                addContact = isChecked;
            }
        });
    }

    public interface CompanyClickListener {
        void clickCompany(int position, BillGroupModel.BillModel model);
    }

    public void bindInputView(InputViewHolder mInputViewHolder, BillGroupModel.BillModel model, int position) throws Exception {
        if (model != null) {
            if (mInputViewHolder.valuesEd.getTag() != null && mInputViewHolder.valuesEd.getTag() instanceof TextChangListener) {
                mInputViewHolder.valuesEd.removeTextChangedListener((TextChangListener) mInputViewHolder.valuesEd.getTag());
            }
            mInputViewHolder.captionTv.setText(model.getCaption());
            mInputViewHolder.muchInputTv.setVisibility(model.getAllowBlank().equals("F") ? View.VISIBLE : View.GONE);
            mInputViewHolder.valuesEd.setText(model.getValue());
            if (model.getType().equals("N")) {
                mInputViewHolder.valuesEd.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                mInputViewHolder.valuesEd.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            if (model.getReadOnly().equals("T")) {
                mInputViewHolder.valuesEd.setFocusable(false);
                mInputViewHolder.valuesEd.setClickable(true);
                mInputViewHolder.valuesEd.setOnClickListener(null);
            } else if (BillTypeChangeUtils.isSelect(model.getType()) || isCompany(model)) {
                //选择类型
                mInputViewHolder.valuesEd.setHint("请选择");
                mInputViewHolder.valuesEd.setFocusable(false);
                mInputViewHolder.valuesEd.setClickable(true);
                mInputViewHolder.selectIv.setVisibility(View.VISIBLE);
                mInputViewHolder.valuesEd.setTag(com.modular.apputils.R.id.tag, position);
                mInputViewHolder.valuesEd.setTag(com.modular.apputils.R.id.tag2, model);
                mInputViewHolder.valuesEd.setOnClickListener(this);
            } else {
                //输入类型
                mInputViewHolder.valuesEd.setHint("请输入");
                mInputViewHolder.selectIv.setVisibility(View.GONE);
                mInputViewHolder.valuesEd.setFocusable(true);
                mInputViewHolder.valuesEd.setClickable(false);
                mInputViewHolder.valuesEd.setFocusableInTouchMode(true);
                TextChangListener mTextChangListener = new TextChangListener(mInputViewHolder.valuesEd, position);
                mInputViewHolder.valuesEd.setTag(mTextChangListener);
                mInputViewHolder.valuesEd.addTextChangedListener(mTextChangListener);
                mInputViewHolder.valuesEd.setOnClickListener(null);
            }

        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == com.modular.apputils.R.id.valuesEd) {
            if (mOnAdapterListener != null) {
                if (view.getTag(com.modular.apputils.R.id.tag2) != null && view.getTag(com.modular.apputils.R.id.tag2) instanceof BillGroupModel.BillModel) {
                    int position = view.getTag(com.modular.apputils.R.id.tag) != null && view.getTag(com.modular.apputils.R.id.tag) instanceof Integer ? ((int) view.getTag(com.modular.apputils.R.id.tag)) : 0;
                    BillGroupModel.BillModel mBillModel = (BillGroupModel.BillModel) view.getTag(com.modular.apputils.R.id.tag2);
                    if (isCompany(mBillModel)) {
                        mCompanyClickListener.clickCompany(mBillModel.getGroupIndex(),mBillModel);
                    } else {
                        mOnAdapterListener.toSelect(position, mBillModel);
                    }
                }
            }

        } else {
            super.onClick(view);
        }
    }

    private boolean isCompany(BillGroupModel.BillModel mBillModel ){
        return mBillModel.getCaption().equals("企业名称")||mBillModel.getField().equals("cu_name");
    }
}
