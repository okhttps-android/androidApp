package com.xzjmyk.pm.activity.ui.erp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.me.imageloader.ImageLoaderUtil;
import com.uas.appworks.model.bean.WorkMenuBean;
import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;
import java.util.List;

public class WorkMenuChildAdapter extends BaseAdapter {

    private List<WorkMenuBean.ModuleListBean> objects = new ArrayList<WorkMenuBean.ModuleListBean>();

    private Context context;
    private LayoutInflater layoutInflater;
    private Resources mResources;

    public WorkMenuChildAdapter(Context context, List<WorkMenuBean.ModuleListBean> objects) {
        this.context = context;
        this.objects = objects;
        this.layoutInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public WorkMenuBean.ModuleListBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_work_menu_child, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((WorkMenuBean.ModuleListBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(WorkMenuBean.ModuleListBean object, ViewHolder holder) {
        if (object.isLocalMenu()) {
            try {
                holder.workMenuChildNameTv.setText(mResources.getIdentifier(object.getMenuName(), "string", context.getPackageName()));
            } catch (Exception e) {
                holder.workMenuChildNameTv.setText(object.getMenuName());
            }
        } else {
            holder.workMenuChildNameTv.setText(object.getMenuName());
        }
        if (TextUtils.isEmpty(object.getMenuIcon())) {
            holder.workMenuChildIconIv.setImageResource(R.drawable.defaultpic);
        } else {
            if (object.isLocalMenu()) {
                try {
                    holder.workMenuChildIconIv.setImageResource(mResources.getIdentifier(object.getMenuIcon(), "drawable", context.getPackageName()));
                } catch (Exception e) {
                    holder.workMenuChildIconIv.setImageResource(R.drawable.defaultpic);
                }
            } else {
                ImageLoaderUtil.getInstance().loadImage(object.getMenuIcon(), holder.workMenuChildIconIv);
            }
        }

    }

    protected class ViewHolder {
        private ImageView workMenuChildIconIv;
        private TextView workMenuChildNameTv;

        public ViewHolder(View view) {
            workMenuChildIconIv = (ImageView) view.findViewById(R.id.work_menu_child_icon_iv);
            workMenuChildNameTv = (TextView) view.findViewById(R.id.work_menu_child_name_tv);
        }
    }
}
