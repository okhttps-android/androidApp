package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.uas.appworks.OA.platform.model.JoinModel;
import com.uas.appworks.R;

import java.util.List;

/**
 * Created by Bitlike on 2017/11/13.
 */

public class JoinCharitAdapter extends BaseAdapter {
	private Context context;
	private int type = 1;
	private List<JoinModel> models;


	public JoinModel getModel(int i) {
		if (ListUtils.getSize(models) > i && i >= 0) {
			return models.get(i);
		}
		return null;
	}

	public void setModels(List<JoinModel> models) {
		this.models = models;
	}


	public JoinCharitAdapter(Context context, int type, List<JoinModel> models) {
		this.context = context;
		this.type = type;
		this.models = models;
	}

	@Override
	public int getCount() {
		return ListUtils.getSize(models);
	}

	@Override
	public Object getItem(int i) {
		return models.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(R.layout.item_join, null);
			holder.nameTv = (TextView) view.findViewById(R.id.nameTv);
			holder.statusTv = (TextView) view.findViewById(R.id.statusTv);
			holder.timeTv = (TextView) view.findViewById(R.id.timeTv);
			holder.subTv = (TextView) view.findViewById(R.id.subTv);
			holder.amountTv = (TextView) view.findViewById(R.id.amountTv);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		JoinModel model = models.get(i);
		holder.nameTv.setText(model.getName());
		int colorId = R.color.text_hine;
		String status = model.getStatus();
		if (!StringUtil.isEmpty(status)) {
			if (status.equals("兑奖中")) {
				colorId = R.color.reactivity;
			} else if ("已结束".equals(status)) {
				colorId = R.color.activityed;
			} else {
				colorId = R.color.activitying;
			}
		}
		holder.statusTv.setTextColor(context.getResources().getColor(colorId));
		holder.statusTv.setText(status);
		if (StringUtil.isEmpty(model.getTime())) {
			holder.timeTv.setVisibility(View.GONE);
		} else {
			holder.timeTv.setVisibility(View.VISIBLE);
			holder.timeTv.setText(model.getTime());
		}
		holder.subTv.setText(model.getSub());
		if (type == 1) {
			holder.amountTv.setVisibility(View.VISIBLE);
			holder.amountTv.setText(String.valueOf(model.getAmount()));
		} else {
			holder.amountTv.setVisibility(View.GONE);
		}
		return view;
	}


	class ViewHolder {
		TextView nameTv;
		TextView statusTv;
		TextView timeTv;
		TextView subTv;
		TextView amountTv;
	}
}
