package com.uas.appme.settings.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.CalendarUtil;
import com.common.data.ListUtils;
import com.core.base.BaseActivity;
import com.core.utils.time.wheel.DatePicker;
import com.uas.appme.R;
import com.uas.appme.settings.model.BRest;

import java.util.List;

/**
 * Created by Bitliker on 2017/10/13.
 */

public class BRestAdapter extends RecyclerView.Adapter<BRestAdapter.ViewHolder> {
	private BaseActivity ct;
	private List<BRest> models;

	public BRestAdapter(BaseActivity ct, List<BRest> models) {
		this.ct = ct;
		this.models = models;
	}

	public void updateData(int position, BRest model) {
		if (position >= 0 && position < getItemCount()) {
			this.models.get(position).update(model);
			notifyItemChanged(position);
		}
	}

	public List<BRest> getModels() {
		return models;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(parent);
	}


	@Override
	public int getItemCount() {
		return ListUtils.getSize(models);
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		TextView delete_tv, name_tv, date_tv, additem_tv;
		RelativeLayout name_rl, date_rl;

		public ViewHolder(ViewGroup parent) {
			this(LayoutInflater.from(ct).inflate(R.layout.item_b_rest, parent, false));
		}

		public ViewHolder(View itemView) {
			super(itemView);
			delete_tv = (TextView) itemView.findViewById(R.id.delete_tv);
			name_tv = (TextView) itemView.findViewById(R.id.company_tv);
			date_tv = (TextView) itemView.findViewById(R.id.date_tv);
			additem_tv = (TextView) itemView.findViewById(R.id.additem_tv);
			name_rl = (RelativeLayout) itemView.findViewById(R.id.company_rl);
			date_rl = (RelativeLayout) itemView.findViewById(R.id.date_rl);
		}
	}


	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		final BRest model = models.get(position);

		if (position == 0) {
			holder.delete_tv.setVisibility(View.GONE);
			holder.delete_tv.setOnClickListener(null);
		} else {
			holder.delete_tv.setVisibility(View.VISIBLE);
			holder.delete_tv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					models.remove(model);
					notifyItemRemoved(position);
					notifyDataSetChanged();
				}
			});
		}

		//只能设置一个
		if (position == getItemCount() - 1 && (1 != 1)) {
			holder.additem_tv.setVisibility(View.VISIBLE);
			holder.additem_tv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BRest newModel = new BRest(models.get(position).getType());
					models.add(newModel);
					notifyItemInserted(position);
					notifyDataSetChanged();
				}
			});
		} else {
			holder.additem_tv.setVisibility(View.GONE);
			holder.additem_tv.setOnClickListener(null);
		}
		if (model.getType() == 1) {
			holder.name_rl.setVisibility(View.VISIBLE);
			holder.name_tv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (onItemClickListener != null) {
						onItemClickListener.itemClick(position);
					}
				}
			});
		} else {
			holder.name_rl.setVisibility(View.GONE);
			holder.name_tv.setOnClickListener(null);
		}
		holder.date_tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimeSelect(position);
			}
		});
		holder.name_tv.setText(model.getUsername());
		holder.date_tv.setText(model.getDate());
	}


	private void showTimeSelect(final int position) {
		DatePicker picker = new DatePicker(ct, DatePicker.YEAR_MONTH_DAY);
		picker.setRange(2015, 2019, true);
		picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
		picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
			@Override
			public void onDatePicked(String year, String month, String day) {
				String time = year + "-" + month + "-" + day;
				if (getItemCount() > position) {
					models.get(position).setDate(time);
					notifyItemChanged(position);
					notifyItemRangeChanged(position, getItemCount());
				}
			}
		});
		picker.show();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	private OnItemClickListener onItemClickListener;

	public interface OnItemClickListener {
		void itemClick(int position);
	}


}
