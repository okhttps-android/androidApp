package com.modular.apputils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.modular.apputils.R;

import java.util.ArrayList;
import java.util.List;

public class PopListAdapter extends BaseAdapter {
	private Context ct;
	private List<String> selectStage = new ArrayList<>();

	public PopListAdapter(Context ct, List<String> selectStage) {
		this.ct = ct;
		this.selectStage = selectStage;
	}



	@Override
	public int getCount() {
		return ListUtils.getSize(selectStage);
	}

	@Override
	public Object getItem(int position) {
		return selectStage.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ct).inflate(R.layout.item_pop_list, null);
			holder = new ViewHolder();
			holder.tv_text = (TextView) convertView.findViewById(R.id.tv_item_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_text.setText(selectStage.get(position));
		return convertView;
	}

	class ViewHolder {
		TextView tv_text;
	}
}