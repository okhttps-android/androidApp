package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.modular.apputils.widget.SrollViewPager;
import com.modular.apputils.widget.ViewPagerIndicator;
import com.uas.appworks.OA.platform.model.Carousel;
import com.uas.appworks.OA.platform.model.CharitSelectModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/11/7.
 */

public class CharitableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private Context ct;
	private List<CharitSelectModel> models;
	private final LayoutInflater inflater;

	public CharitableListAdapter(Context ct, List<CharitSelectModel> models) {
		if (ct == null) new NullPointerException("ct is null");
		this.ct = ct;
		this.models = models;
		inflater = LayoutInflater.from(ct);
	}

	@Override
	public int getItemViewType(int position) {
		return models.get(position).getType();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case 0:
				return new MoneyAmountViewHolder(parent);
			case 2:
				break;
			default:
				return new ListViewHolder(parent);
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		CharitSelectModel model = models.get(position);
		if (holder instanceof MoneyAmountViewHolder) {
			bindMoneyAmount((MoneyAmountViewHolder) holder, model);
		}
	}


	private void bindMoneyAmount(MoneyAmountViewHolder holder, CharitSelectModel model) {
		holder.moneyAmountTv.setText(model.getTitle());
	}


	@Override
	public int getItemCount() {
		return ListUtils.getSize(models);
	}


	private class MoneyAmountViewHolder extends RecyclerView.ViewHolder {
		TextView moneyAmountTv;

		public MoneyAmountViewHolder(ViewGroup parent) {
			this(inflater.inflate(R.layout.item_money_amount, parent, false));
		}

		public MoneyAmountViewHolder(View itemView) {
			super(itemView);
			moneyAmountTv = (TextView) itemView.findViewById(R.id.moneyAmountTv);
		}
	}

	private class ListViewHolder extends RecyclerView.ViewHolder {

		public ListViewHolder(ViewGroup parent) {
			this(inflater.inflate(R.layout.item_charitable_list, parent, false));
		}

		public ListViewHolder(View itemView) {
			super(itemView);
		}
	}


}
