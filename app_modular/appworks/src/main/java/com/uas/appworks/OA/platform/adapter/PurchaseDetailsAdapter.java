package com.uas.appworks.OA.platform.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.data.TextUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.listener.EditChangeListener;
import com.uas.appworks.OA.platform.model.Purchase;
import com.uas.appworks.R;

import java.util.Date;
import java.util.List;


/**
 * Created by Bitlike on 2018/1/16.
 */

public class PurchaseDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Purchase> purchases;
    private LayoutInflater mInflater;
    private boolean canReply;

    public PurchaseDetailsAdapter(Context context, String status, List<Purchase> purchases) {
        this.context = context;
        this.purchases = purchases;
        mInflater = LayoutInflater.from(context);
        canReply = (status != null && status.equals(Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO));

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new TotalViewHolder(parent);
        } else if (viewType == ListUtils.getSize(purchases)) {
            return new BtnViewHolder(parent);
        }
        return new ViewHolder(parent);
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(purchases) + 1;
    }


    class BtnViewHolder extends RecyclerView.ViewHolder {
        Button replyBtn;

        public BtnViewHolder(ViewGroup viewGroup) {
            this(mInflater.inflate(R.layout.item_btn, viewGroup, false));
        }

        public BtnViewHolder(View itemView) {
            super(itemView);
            replyBtn = itemView.findViewById(R.id.replyBtn);
            replyBtn.setVisibility(canReply ? View.VISIBLE : View.GONE);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTv, remarksTv, totalTv, dateEd,amountTv,numberUnit;
        EditText numberEd, remarksInputEd;

        public ViewHolder(ViewGroup viewGroup) {
            this(mInflater.inflate(R.layout.item_purchase, viewGroup, false));
        }

        public ViewHolder(View itemView) {
            super(itemView);
            timeTv = itemView.findViewById(R.id.timeTv);
            numberUnit = itemView.findViewById(R.id.numberUnit);
            amountTv = itemView.findViewById(R.id.amountTv);
            remarksTv = itemView.findViewById(R.id.remarksTv);
            totalTv = itemView.findViewById(R.id.totalTv);
            dateEd = itemView.findViewById(R.id.dateEd);
            numberEd = itemView.findViewById(R.id.numberEd);
            remarksInputEd = itemView.findViewById(R.id.remarksInputEd);

        }
    }

    class TotalViewHolder extends RecyclerView.ViewHolder {
        TextView customerTv, addressTv, codeTv, timeTv, remarksTv, totalTv,currencyTv;

        public TotalViewHolder(ViewGroup viewGroup) {
            this(mInflater.inflate(R.layout.item_ls_purchase, viewGroup, false));
        }

        public TotalViewHolder(View itemView) {
            super(itemView);
            currencyTv = itemView.findViewById(R.id.currencyTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            codeTv = itemView.findViewById(R.id.codeTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            customerTv = itemView.findViewById(R.id.customerTv);
            remarksTv = itemView.findViewById(R.id.remarksTv);
            totalTv = itemView.findViewById(R.id.totalTv);

        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder && position < ListUtils.getSize(purchases)) {
            onBindViewHolder((ViewHolder) holder, position);
        } else if (holder instanceof TotalViewHolder) {
            onBindViewHolder((TotalViewHolder) holder, position);
        } else if (holder instanceof BtnViewHolder) {
            onBindViewHolder((BtnViewHolder) holder);
        }
    }

    private void onBindViewHolder(TotalViewHolder holder, int position) {
        Purchase purchase = purchases.get(position);
        holder.totalTv.setText(purchase.getTotal());
        holder.addressTv.setText(purchase.getAddress());
        holder.customerTv.setText(purchase.getCustomer());
        holder.codeTv.setText(purchase.getCode());
        holder.remarksTv.setText(TextUtils.isEmpty(purchase.getRemarks()) ? "无" : purchase.getRemarks());
        holder.timeTv.setText(purchase.getTime());
        holder.currencyTv.setText(purchase.getCurrency());
    }

    private void onBindViewHolder(BtnViewHolder holder) {
        holder.replyBtn.setFocusable(canReply);
        holder.replyBtn.setClickable(canReply);
        holder.replyBtn.setPressed(!canReply);
        int padd = DisplayUtil.dip2px(context, 10);
        if (canReply) {
            holder.replyBtn.setBackgroundResource(R.drawable.bg_bule_btn);
            holder.replyBtn.setOnClickListener(onClickListener);

        } else {
            holder.replyBtn.setBackgroundResource(R.drawable.bg_orange_btn_pass);
        }
        holder.replyBtn.setPadding(padd, padd, padd, padd);
    }

    private void onBindViewHolder(ViewHolder holder, int position) {
        Purchase purchase = purchases.get(position);
        holder.totalTv.setText(purchase.getTotal());
        holder.remarksTv.setText(purchase.getRemarks());
        holder.timeTv.setText(purchase.getCode());
        holder.amountTv.setText(purchase.getPrice());
        holder.dateEd.setText(purchase.getDate());
        holder.numberEd.setText(purchase.getNumber());
        holder.remarksInputEd.setText(purchase.getRemarksInput());
        holder.numberUnit.setText(purchase.getUnit());

        if (canReply && purchase.isCanInput()) {
            holder.dateEd.setTag(position);
            holder.dateEd.setOnClickListener(onClickListener);
            holder.numberEd.addTextChangedListener(new TextChangListener(position, 1));
            holder.remarksInputEd.addTextChangedListener(new TextChangListener(position, 2));
            holder.numberEd.setClickable(true);
            holder.numberEd.setFocusable(true);
            holder.remarksInputEd.setClickable(true);
            holder.remarksInputEd.setFocusable(true);
        } else {
            holder.dateEd.setOnClickListener(null);
            holder.numberEd.setClickable(false);
            holder.numberEd.setFocusable(false);
            holder.remarksInputEd.setClickable(false);
            holder.remarksInputEd.setFocusable(false);
        }


    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.dateEd && v.getTag() != null && v.getTag() instanceof Integer) {
                int position = (int) v.getTag();
                showDateSelect(position);
            } else if (v.getId() == R.id.replyBtn) {
                if (onReplyLisenter != null) {
                    onReplyLisenter.reply(purchases);
                }
            }
        }
    };

    private void showDateSelect(final int position) {
        DateTimePicker picker = new DateTimePicker((Activity) context, DateTimePicker.YEAR_MONTH_DAY);
        picker.setRange(2000, 2030);
        int year, month, day;
        Date time = null;
        if (ListUtils.getSize(purchases) > position && !StringUtil.isEmpty(purchases.get(position).getDate())) {
            time = DateFormatUtil.str2date(purchases.get(position).getDate(), DateFormatUtil.YMD);
        }
        year = CalendarUtil.getYear(time);
        month = CalendarUtil.getMonth(time);
        day = CalendarUtil.getDay(time);
        picker.setSelectedItem(year, month, day);
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                if (ListUtils.getSize(purchases) > position) {
                    purchases.get(position).setDate(year + "-" + month + "-" + day);
                    notifyItemChanged(position);
                }
            }
        });
        picker.show();
    }

    private class TextChangListener extends EditChangeListener {
        private int position;
        private int type;

        public TextChangListener(int position, int type) {
            this.position = position;
            this.type = type;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (this.position >= 0 && ListUtils.getSize(purchases) > this.position) {
                String message = s == null ? "" : s.toString();
                if (type == 1) {
                    purchases.get(this.position).setNumber(message);
                } else if (type == 2) {
                    purchases.get(this.position).setRemarksInput(message);
                }
            }
        }
    }

    private OnReplyLisenter onReplyLisenter;

    public void setOnReplyLisenter(OnReplyLisenter onReplyLisenter) {
        this.onReplyLisenter = onReplyLisenter;
    }

    public interface OnReplyLisenter {
        void reply(List<Purchase> purchases);
    }
}
