package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.B2BDetailListBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/16 16:12
 */

public class B2BDetailListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<B2BDetailListBean> mB2BDetailListBeans;
    private boolean isEditable = true;

    public B2BDetailListAdapter(Context context, List<B2BDetailListBean> b2BDetailListBeans) {
        mContext = context;
        mB2BDetailListBeans = b2BDetailListBeans;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public List<B2BDetailListBean> getB2BDetailListBeans() {
        return mB2BDetailListBeans;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case B2BDetailListBean.TYPE_DETAIL_TEXT:
                itemView = mLayoutInflater.inflate(R.layout.layout_list_b2b_detail_text, parent, false);
                return new DetailTextViewHolder(itemView);
            case B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE:
                itemView = mLayoutInflater.inflate(R.layout.layout_list_b2b_white_detail_text, parent, false);
                return new DetailWhiteTextViewHolder(itemView);
            case B2BDetailListBean.TYPE_DETAIL_EDIT:
                itemView = mLayoutInflater.inflate(R.layout.layout_list_b2b_detail_edit, parent, false);
                return new DetailEditViewHolder(itemView);
            case B2BDetailListBean.TYPE_DETAIL_OPTION:
                itemView = mLayoutInflater.inflate(R.layout.layout_list_b2b_detail_option, parent, false);
                return new DetailOptionViewHolder(itemView);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        B2BDetailListBean b2BDetailListBean = mB2BDetailListBeans.get(position);
        if (holder instanceof DetailTextViewHolder) {
            bindDetailTextViewHolder((DetailTextViewHolder) holder, b2BDetailListBean);
        } else if (holder instanceof DetailWhiteTextViewHolder) {
            bindDetailWhiteTextViewHolder((DetailWhiteTextViewHolder) holder, b2BDetailListBean);
        } else if (holder instanceof DetailEditViewHolder) {
            bindDetailEditViewHolder((DetailEditViewHolder) holder, b2BDetailListBean);
        } else if (holder instanceof DetailOptionViewHolder) {
            bindDetailOptionViewHolder((DetailOptionViewHolder) holder, b2BDetailListBean);
        }
    }

    private void bindDetailTextViewHolder(DetailTextViewHolder holder, B2BDetailListBean b2BDetailListBean) {
        holder.captionTextView.setText(b2BDetailListBean.getCaption() + ":");
        holder.valueTextView.setText(b2BDetailListBean.getValue());
    }

    private void bindDetailEditViewHolder(DetailEditViewHolder holder, final B2BDetailListBean b2BDetailListBean) {
        holder.captionTextView.setText(b2BDetailListBean.getCaption() + ":");
        holder.unitTextView.setText(b2BDetailListBean.getUnit());

        if (isEditable) {
            holder.valueEditText.setVisibility(View.VISIBLE);
            holder.valueTextView.setVisibility(View.GONE);

            holder.valueEditText.setText(b2BDetailListBean.getValue());

            int editType = b2BDetailListBean.getEditType();
            if (editType == B2BDetailListBean.EDIT_TYPE_TEXT) {
                holder.valueEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            } else if (editType == B2BDetailListBean.EDIT_TYPE_DECIMAL) {
                holder.valueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else if (editType == B2BDetailListBean.EDIT_TYPE_NUMBER) {
                holder.valueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            holder.valueEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    b2BDetailListBean.setValue(editable.toString());
                }
            });
        } else {
            holder.valueEditText.setVisibility(View.GONE);
            holder.valueTextView.setVisibility(View.VISIBLE);

            holder.valueTextView.setText(b2BDetailListBean.getValue());
        }

    }

    private void bindDetailWhiteTextViewHolder(DetailWhiteTextViewHolder holder, B2BDetailListBean b2BDetailListBean) {
        holder.captionTextView.setText(b2BDetailListBean.getCaption() + ":");
        holder.valueTextView.setText(b2BDetailListBean.getValue());
    }

    private void bindDetailOptionViewHolder(final DetailOptionViewHolder holder, final B2BDetailListBean b2BDetailListBean) {

        holder.captionTextView.setText(b2BDetailListBean.getCaption() + ":");
        holder.valueTextView.setText(b2BDetailListBean.getValue());

        if (isEditable) {
            final List<String> options = b2BDetailListBean.getOptions();
            holder.valueTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String value = b2BDetailListBean.getValue();
                    int selectIndex = 0;
                    for (int i = 0; i < options.size(); i++) {
                        if (value != null && value.equals(options.get(i))) {
                            selectIndex = i;
                            break;
                        }
                    }

                    new MaterialDialog.Builder(mContext)
                            .title(R.string.please_select_currency)
                            .items(options)
                            .itemsCallbackSingleChoice(selectIndex, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                                    String option = options.get(which);
//                                holder.valueTextView.setText(option);
                                    b2BDetailListBean.setValue(option);
                                    notifyDataSetChanged();
                                    return true;
                                }
                            }).positiveText(mContext.getString(com.core.app.R.string.common_sure)).build().show();
                }
            });
        } else {
            holder.valueTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mB2BDetailListBeans == null ? 0 : mB2BDetailListBeans.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mB2BDetailListBeans.get(position).getItemType();
    }

    class DetailTextViewHolder extends RecyclerView.ViewHolder {
        private TextView captionTextView, valueTextView;

        public DetailTextViewHolder(View itemView) {
            super(itemView);
            captionTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_text_caption_tv);
            valueTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_text_value_tv);
        }
    }

    class DetailWhiteTextViewHolder extends RecyclerView.ViewHolder {
        private TextView captionTextView, valueTextView;

        public DetailWhiteTextViewHolder(View itemView) {
            super(itemView);
            captionTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_white_text_caption_tv);
            valueTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_white_text_value_tv);
        }
    }

    class DetailEditViewHolder extends RecyclerView.ViewHolder {
        private TextView captionTextView, unitTextView;
        private EditText valueEditText;
        private TextView valueTextView;

        public DetailEditViewHolder(View itemView) {
            super(itemView);
            captionTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_edit_caption_tv);
            unitTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_edit_unit_tv);
            valueEditText = (EditText) itemView.findViewById(R.id.list_b2b_detail_edit_value_et);
            valueTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_edit_value_tv);
        }
    }

    class DetailOptionViewHolder extends RecyclerView.ViewHolder {
        private TextView captionTextView, valueTextView;

        public DetailOptionViewHolder(View itemView) {
            super(itemView);
            captionTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_option_caption_tv);
            valueTextView = (TextView) itemView.findViewById(R.id.list_b2b_detail_option_value_tv);
        }
    }
}
