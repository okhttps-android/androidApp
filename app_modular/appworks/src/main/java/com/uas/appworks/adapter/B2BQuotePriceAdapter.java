package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.B2BQuotePriceBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe 分段报价列表
 * @date 2018/1/16 20:35
 */

public class B2BQuotePriceAdapter extends RecyclerView.Adapter<B2BQuotePriceAdapter.QuotePriceViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<B2BQuotePriceBean> mB2BQuotePriceBeans;
    private boolean isEditable = true;

    public B2BQuotePriceAdapter(Context context, List<B2BQuotePriceBean> b2BQuotePriceBeans) {
        mContext = context;
        mB2BQuotePriceBeans = b2BQuotePriceBeans;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        notifyDataSetChanged();
    }

    public List<B2BQuotePriceBean> getB2BQuotePriceBeans() {
        return mB2BQuotePriceBeans;
    }

    @Override
    public QuotePriceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_list_subsection_quotation, parent, false);
        return new QuotePriceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final QuotePriceViewHolder holder, final int position) {
        final B2BQuotePriceBean b2BQuotePriceBean = mB2BQuotePriceBeans.get(position);
        holder.mNumTextView.setText((position + 1) + "");

        if (b2BQuotePriceBean == null) {
            return;
        }

        if (isEditable) {
            if (position == 0) {
                holder.mAmountEditText.setVisibility(View.GONE);
                holder.mPriceEditText.setVisibility(View.VISIBLE);
                holder.mAmountTextView.setVisibility(View.VISIBLE);
                holder.mPriceTextView.setVisibility(View.GONE);

                holder.mAmountTextView.setText("0");
            } else {
                holder.mAmountEditText.setVisibility(View.VISIBLE);
                holder.mPriceEditText.setVisibility(View.VISIBLE);
                holder.mAmountTextView.setVisibility(View.GONE);
                holder.mPriceTextView.setVisibility(View.GONE);
            }

            holder.mDeleteLine.setVisibility(View.VISIBLE);

            if (mB2BQuotePriceBeans.size() == 1) {
                holder.mDeleteImageView.setVisibility(View.INVISIBLE);
            } else {
                holder.mDeleteImageView.setVisibility(View.VISIBLE);
            }

            if (holder.mAmountEditText.getTag() instanceof TextWatcher) {
                holder.mAmountEditText.removeTextChangedListener((TextWatcher) holder.mAmountEditText.getTag());
            }
            if (holder.mPriceEditText.getTag() instanceof TextWatcher) {
                holder.mPriceEditText.removeTextChangedListener((TextWatcher) holder.mPriceEditText.getTag());
            }

            holder.mAmountEditText.setText(b2BQuotePriceBean.getAmount());
            holder.mPriceEditText.setText(b2BQuotePriceBean.getPrice());
            holder.mDeleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mDeleteImageView.setEnabled(false);
                    if (mB2BQuotePriceBeans.size() > 0) {
                        mB2BQuotePriceBeans.remove(position);
                        notifyDataSetChanged();
                    }
                    holder.mDeleteImageView.setEnabled(true);
                }
            });

            TextWatcher amountTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String s = editable.toString();
                    b2BQuotePriceBean.setAmount(s);
                }
            };

            TextWatcher priceTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String s = editable.toString();
                    b2BQuotePriceBean.setPrice(s);
                }
            };

            holder.mAmountEditText.addTextChangedListener(amountTextWatcher);
            holder.mAmountEditText.setTag(amountTextWatcher);

            holder.mPriceEditText.addTextChangedListener(priceTextWatcher);
            holder.mPriceEditText.setTag(priceTextWatcher);
        } else {
            holder.mAmountEditText.setVisibility(View.GONE);
            holder.mPriceEditText.setVisibility(View.GONE);
            holder.mAmountTextView.setVisibility(View.VISIBLE);
            holder.mPriceTextView.setVisibility(View.VISIBLE);

            holder.mDeleteImageView.setVisibility(View.GONE);
            holder.mDeleteLine.setVisibility(View.GONE);

            holder.mAmountTextView.setText(b2BQuotePriceBean.getAmount());
            holder.mPriceTextView.setText(b2BQuotePriceBean.getPrice());

            holder.mDeleteImageView.setOnClickListener(null);
        }


    }

    @Override
    public int getItemCount() {
        return mB2BQuotePriceBeans == null ? 0 : mB2BQuotePriceBeans.size();
    }

    class QuotePriceViewHolder extends RecyclerView.ViewHolder {
        private TextView mNumTextView;
        private EditText mAmountEditText, mPriceEditText;
        private ImageView mDeleteImageView;
        private TextView mAmountTextView, mPriceTextView;
        private View mDeleteLine;

        public QuotePriceViewHolder(View itemView) {
            super(itemView);
            mNumTextView = (TextView) itemView.findViewById(R.id.list_subsection_quotation_num_tv);
            mAmountEditText = (EditText) itemView.findViewById(R.id.list_subsection_quotation_amount_et);
            mPriceEditText = (EditText) itemView.findViewById(R.id.list_subsection_quotation_price_et);
            mDeleteImageView = (ImageView) itemView.findViewById(R.id.list_subsection_quotation_delete_iv);
            mAmountTextView = (TextView) itemView.findViewById(R.id.list_subsection_quotation_amount_tv);
            mPriceTextView = (TextView) itemView.findViewById(R.id.list_subsection_quotation_price_tv);
            mDeleteLine = itemView.findViewById(R.id.list_subsection_quotation_delete_line);
        }
    }
}
