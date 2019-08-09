package com.uas.appme.settings.adapter;

import android.content.Context;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.uas.appme.R;
import com.uas.appworks.OA.erp.model.KVMode;

import java.util.List;

/**
 * Created by FANGlh on 2017/11/29.
 * function:
 */

public class WagesDetailsAdapter extends BaseAdapter {
    private List<KVMode> modeList;
    private Context mContext;

    public WagesDetailsAdapter(Context mContext){
        this.mContext = mContext;
    }
    public List<KVMode> getModeList() {
        return modeList;
    }
    public void setModeList(List<KVMode> modeList) {
        this.modeList = modeList;
    }
    @Override
    public int getCount() {
        return ListUtils.isEmpty(modeList) ? 0 : modeList.size();
    }
    @Override
    public Object getItem(int position) {
        return modeList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WagesHolder wHolder;
        if (convertView == null) {
            wHolder = new WagesHolder();
            convertView = View.inflate(mContext, R.layout.item_comkey_value_salary, null);
            wHolder.key = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
            wHolder.value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
            convertView.setTag(wHolder);
        } else {
            wHolder = (WagesHolder) convertView.getTag();
        }
        TextPaint tp = wHolder.value .getPaint();
        if(position == modeList.size() - 1){
            tp.setFakeBoldText(true);
        }else {
            tp.setFakeBoldText(false);
        }
        wHolder.key.setText(modeList.get(position).getKey()+"");
        wHolder.value.setText(modeList.get(position).getValue()+"");
        return convertView;
    }

    class WagesHolder{
        TextView key,value;
    }
}
