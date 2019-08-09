package com.uas.appworks.CRM.erp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uas.appworks.CRM.erp.model.SimpleData;
import com.uas.appworks.R;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


/**
 * Created by Arison on 2016/12/21.
 */
public class StickyListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<SimpleData> simpleDatas;
    private LayoutInflater inflater;

    public StickyListAdapter(Context context, List<SimpleData> datas) {
        inflater = LayoutInflater.from(context);
        this.simpleDatas=datas;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return simpleDatas.get(position).getGroupId();
    }
    
    @Override
    public int getCount() {
        return simpleDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return simpleDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.simple_list_item_3, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(simpleDatas.get(position).getLeft());

        holder.text2.setText(simpleDatas.get(position).getRight());

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.list_item_header, parent, false);
            holder.view_line=convertView.findViewById(R.id.view_line);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        if (simpleDatas.get(position).getGroupId()==0){
            holder.view_line.setVisibility(View.GONE);
        }else{
            holder.view_line.setVisibility(View.VISIBLE);
        }
        return convertView;
    }



    class HeaderViewHolder {
        TextView text;
         View  view_line;
    }

    class ViewHolder {
        TextView text;
        TextView text2;
    }

}