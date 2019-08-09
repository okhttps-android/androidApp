package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.core.widget.MyListView;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.bean.DataInquiryFlexBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询列表伸缩菜单适配器
 */
public class DataInquiryFlexAdapter extends BaseAdapter {
    private final int mFlexLines = 3;
    private List<DataInquiryFlexBean> objects = new ArrayList<DataInquiryFlexBean>();
    private List<List<DataInquiryFlexBean.RowBean.RowChildBean>> mRowChildBeans = new ArrayList<>();

    private Context context;
    private LayoutInflater layoutInflater;
    private boolean isDevice = false;

    public DataInquiryFlexAdapter(Context context, List<DataInquiryFlexBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public DataInquiryFlexAdapter(Context context, List<DataInquiryFlexBean> objects, boolean isDevice) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
        this.isDevice = isDevice;
    }

    public List<List<DataInquiryFlexBean.RowBean.RowChildBean>> getRowChildBeans() {
        return mRowChildBeans;
    }

    public void setRowChildBeans(List<List<DataInquiryFlexBean.RowBean.RowChildBean>> rowChildBeans) {
        mRowChildBeans = rowChildBeans;
    }

    public List<DataInquiryFlexBean> getObjects() {
        return objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public DataInquiryFlexBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (isDevice) {
                convertView = layoutInflater.inflate(R.layout.item_list_device_query, null);
            } else {
                convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry, null);
            }
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((DataInquiryFlexBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(final DataInquiryFlexBean object, ViewHolder holder) {
        holder.itemDataInquiryFlexLv.setEnabled(false);
        holder.itemDataInquiryFlexLv.setFocusable(false);
        holder.itemDataInquiryFlexLv.setClickable(false);
        holder.itemDataInquiryFlexLv.setPressed(false);

        List<DataInquiryFlexBean.RowBean> rowBeans = new ArrayList<>();
        if (object.isFlex()) {
            rowBeans = object.getRowBeans();
            holder.itemDataInquiryFlexIv.setImageResource(R.drawable.ic_menu_spread);
        } else {
            holder.itemDataInquiryFlexIv.setImageResource(R.drawable.ic_menu_retract);
            for (int i = 0; i < (object.getRowBeans().size() < mFlexLines ? object.getRowBeans().size() : mFlexLines); i++) {
                rowBeans.add(object.getRowBeans().get(i));
            }
        }
        DataInquiryFlexChildAdapter dataInquiryFlexChildAdapter = new DataInquiryFlexChildAdapter(context, rowBeans);

        holder.itemDataInquiryFlexLv.setAdapter(dataInquiryFlexChildAdapter);
        if (isDevice) {
            holder.itemDataInquiryFlexIv.setVisibility(View.GONE);
        } else {
            if (object.getRowBeans().size() <= mFlexLines || rowBeans.size() < mFlexLines) {
                holder.itemDataInquiryFlexIv.setVisibility(View.INVISIBLE);
            } else {
                holder.itemDataInquiryFlexIv.setVisibility(View.VISIBLE);
            }
            holder.itemDataInquiryFlexIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    object.setIsFlex(!object.isFlex());
                    notifyDataSetChanged();
                }
            });
        }

//        holder.itemDataInquiryFlexRl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (CommonUtil.isRepeatClick()) {
//                    object.setIsFlex(!object.isFlex());
//                    notifyDataSetChanged();
//                }
//            }
//        });
    }

    protected class ViewHolder {
        private RelativeLayout itemDataInquiryFlexRl;
        private ImageView itemDataInquiryFlexIv;
        private MyListView itemDataInquiryFlexLv;

        public ViewHolder(View view) {
            itemDataInquiryFlexRl = (RelativeLayout) view.findViewById(R.id.item_data_inquiry_flex_rl);
            itemDataInquiryFlexIv = (ImageView) view.findViewById(R.id.item_data_inquiry_flex_iv);
            itemDataInquiryFlexLv = (MyListView) view.findViewById(R.id.item_data_inquiry_flex_lv);
        }
    }

}
