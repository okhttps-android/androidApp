package com.xzjmyk.pm.activity.ui.erp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.widget.view.MyGridView;
import com.uas.appworks.model.bean.WorkMenuBean;
import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;
import java.util.List;

public class WorkParentLayoutAdapter extends BaseAdapter {

    private List<WorkMenuBean> objects = new ArrayList<WorkMenuBean>();

    private Context context;
    private LayoutInflater layoutInflater;

    public WorkParentLayoutAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public WorkMenuBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_work_parent_layout, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((WorkMenuBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(WorkMenuBean object, ViewHolder holder) {
        //TODO implement
    }

    protected class ViewHolder {
        private TextView workParentTitleTv;
        private MyGridView workParentGv;

        public ViewHolder(View view) {
            workParentTitleTv = (TextView) view.findViewById(R.id.work_parent_title_tv);
            workParentGv = (MyGridView) view.findViewById(R.id.work_parent_gv);
        }
    }
}
