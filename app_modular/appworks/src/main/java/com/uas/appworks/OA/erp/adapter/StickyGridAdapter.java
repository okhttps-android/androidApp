package com.uas.appworks.OA.erp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.app.R;
import com.core.widget.view.selectcalendar.bean.Data;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.List;


/**
 * Created by Arison on 2016/11/23.
 */
public class StickyGridAdapter extends BaseAdapter implements
        StickyGridHeadersSimpleAdapter {

    private List<Data> hasHeaderIdList;
    private LayoutInflater mInflater;
    private GridView mGridView;

    public StickyGridAdapter(Context context, List<Data> hasHeaderIdList, GridView mGridView) {
        mInflater = LayoutInflater.from(context);
        this.mGridView = mGridView;
        this.hasHeaderIdList = hasHeaderIdList;
    }

    @Override
    public long getHeaderId(int i) {
        return hasHeaderIdList.get(i).getGroupId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.grid_item_simpletext, parent, false);
            mViewHolder.mfield = (TextView) convertView.findViewById(R.id.tv_field);
            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if (hasHeaderIdList.get(position).isSelected()) {
            mViewHolder.mfield.setSelected(true);
        } else {
            mViewHolder.mfield.setSelected(false);
        }
        mViewHolder.mfield.setText(hasHeaderIdList.get(position).getName());
        return convertView;
    }


    @Override
    public View getHeaderView(int i, View convertView, ViewGroup parent) {
        HeaderViewHolder mHeaderHolder;
        if (convertView == null) {
            mHeaderHolder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.grid_item_header, parent, false);
            mHeaderHolder.mTextView = (TextView) convertView.findViewById(R.id.tv_name);
            mHeaderHolder.mDesc = (TextView) convertView.findViewById(R.id.tv_desc);
            convertView.setTag(mHeaderHolder);

        } else {
            mHeaderHolder = (HeaderViewHolder) convertView.getTag();
        }
        if (StringUtil.isEmpty(hasHeaderIdList.get(i).getGroup())) {
            mHeaderHolder.mTextView.setText("基本资料");
            mHeaderHolder.mDesc.setVisibility(View.GONE);
        } else {
            mHeaderHolder.mTextView.setText(hasHeaderIdList.get(i).getGroup());
            mHeaderHolder.mDesc.setVisibility(View.GONE);
        }

        return convertView;
    }


    @Override
    public int getCount() {
        return hasHeaderIdList.size();
    }

    @Override
    public Object getItem(int position) {
        return hasHeaderIdList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder {
        public TextView mfield;


    }

    public static class HeaderViewHolder {
        public TextView mTextView;
        public TextView mDesc;
    }

    public List<Data> getHasHeaderIdList() {
        return hasHeaderIdList;
    }

    public void setHasHeaderIdList(List<Data> hasHeaderIdList) {
        this.hasHeaderIdList = hasHeaderIdList;
    }
}
