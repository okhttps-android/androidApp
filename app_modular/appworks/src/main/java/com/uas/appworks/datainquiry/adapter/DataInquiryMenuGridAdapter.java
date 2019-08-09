package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.widget.CircleTextView;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询九宫格菜单子项适配器
 */
public class DataInquiryMenuGridAdapter extends BaseAdapter {

    private List<DataInquiryGirdItemBean> objects = new ArrayList<DataInquiryGirdItemBean>();

    private Context context;
    private LayoutInflater layoutInflater;

    public DataInquiryMenuGridAdapter(Context context, List<DataInquiryGirdItemBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public List<DataInquiryGirdItemBean> getObjects() {
        return objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public DataInquiryGirdItemBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_grid_data_inquiry_menu, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((DataInquiryGirdItemBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(DataInquiryGirdItemBean object, ViewHolder holder) {
        holder.gridDataInquiryMenuContent.setText(object.getIconText());
        if ("更多".equals(object.getIconText())) {
            holder.gridDataInquiryMenuIcon.setText("···");
        } else {
            if (object.getIconText().length() >= 1) {
                holder.gridDataInquiryMenuIcon.setText(object.getIconText().substring(0, 1));
            }
        }
        holder.gridDataInquiryMenuIcon.setMyBackgroundColor(context.getResources().getColor(object.getColor()));

    }

    protected class ViewHolder {
        private CircleTextView gridDataInquiryMenuIcon;
        private TextView gridDataInquiryMenuContent;

        public ViewHolder(View view) {
            gridDataInquiryMenuIcon = (CircleTextView) view.findViewById(R.id.grid_data_inquiry_menu_icon);
            gridDataInquiryMenuContent = (TextView) view.findViewById(R.id.grid_data_inquiry_menu_content);
        }
    }


}
