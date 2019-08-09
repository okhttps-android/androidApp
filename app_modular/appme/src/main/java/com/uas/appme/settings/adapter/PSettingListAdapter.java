package com.uas.appme.settings.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uas.appme.R;
import com.uas.appme.settings.model.PersonSetingBean;

/**
 * Created by FANGlh on 2017/10/12.
 * function:
 */

public class PSettingListAdapter extends BaseAdapter {
    private PersonSetingBean model;
    private Context mContext;

    public PSettingListAdapter(Context mContext){this.mContext = mContext;}
    public PersonSetingBean getModel() {return model;}
    public void setModel(PersonSetingBean model) {this.model = model;}

    @Override
    public int getCount() {
        return model == null ? 0 : model.getResult().size();
    }

    @Override
    public Object getItem(int position) {
        return model.getResult().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView =  View.inflate(mContext, R.layout.psetting_item,null);
            viewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            viewHolder.company_tv = (TextView) convertView.findViewById(R.id.company_tv);
            viewHolder.department_tv = (TextView) convertView.findViewById(R.id.department_tv);
            viewHolder.position_tv = (TextView) convertView.findViewById(R.id.position_tv);
            viewHolder.phone_tv = (TextView) convertView.findViewById(R.id.phone_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name_tv.setText(model.getResult().get(position).getSm_username());
        viewHolder.company_tv.setText(model.getResult().get(position).getSm_companyname());
        viewHolder.department_tv.setText(model.getResult().get(position).getSm_stname());
        viewHolder.position_tv.setText(model.getResult().get(position).getSm_level());
        viewHolder.phone_tv.setText(model.getResult().get(position).getSm_telephone());
        return convertView;
    }

    class ViewHolder{
        TextView name_tv,company_tv,department_tv,position_tv,phone_tv;
    }


}
