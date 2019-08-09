package com.uas.appworks.CRM.erp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.utils.CommonUtil;
import com.uas.appworks.R;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class HListViewOneAdapter extends BaseAdapter {
    private Context ct;
    private LayoutInflater inflater;
    private ArrayList<ArrayList<String>> lists;

    public HListViewOneAdapter(Context context, ArrayList<ArrayList<String>> lists) {
        super();
        this.lists = lists;
        this.ct = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lists != null ? lists.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        return lists.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int index, View view, ViewGroup arg2) {
        ArrayList<String> items=lists.get(index);
        ViewHolder holder=null;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_grid_tv, null);
            holder.ll_root= (LinearLayout) view.findViewById(R.id.ll_root);
            holder.tv_item=new TextView[items.size()];
            for (int j=0;j<items.size();j++){
                float weight=1.0f;
                if (j==0){
                    weight=0.2f;
                }else if(j==2){
                    weight=0.6f;
                }else if (j==3){
                    weight=0.2f;
                }else{
                    weight=3f;
                }
                TextView tv= CreateTextView(ct,weight);
                holder.tv_item[j]=tv;
                holder.ll_root.addView(tv);
            }
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        for (int i=0;i<holder.tv_item.length;i++){
            if (!StringUtil.isEmpty(items.get(i))) {
                holder.tv_item[i].setText(items.get(i));
                if (i!=0) {
                    holder.tv_item[i].setTextSize(14);
                   // holder.tv_item[i].setMinWidth(CommonUtil.dip2px(ct, 50));
                }else{
                    if (index!=0){
                        holder.tv_item[i].setText("●");
                        holder.tv_item[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
                        holder.tv_item[i].setTextColor(getColor(items.get(i)));
//                        holder.tv_item[i].setMinWidth(CommonUtil.dip2px(ct, 50));
//                        holder.tv_item[i].setMinHeight(CommonUtil.dip2px(ct, 50));
                    }else{
                        holder.tv_item[i].setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                        holder.tv_item[i].setTextColor(ct.getResources().getColor(R.color.black));
                      //  holder.tv_item[i].setBackgroundColor(ct.getResources().getColor(R.color.yellow_home));
                    }
                }
            }
        }
        return view;
    }

    private int getColor(String color){
        try{
            return Color.parseColor(color);
        }catch (Exception e){
            return 0xe9e9e9;
        }
    }
  public  class ViewHolder {
      public TextView[] tv_item;
      public  LinearLayout ll_root;
    }

    public TextView CreateTextView(Context ct,float weight){
        TextView tView=new TextView(ct);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                CommonUtil.dip2px(ct, 72),
                LayoutParams.MATCH_PARENT,weight);
        tView.setMinHeight(CommonUtil.dip2px(ct, 30));
        tView.setLayoutParams(tp);
        tView.setGravity(Gravity.CENTER);
        tView.setBackground(ct.getResources().getDrawable(R.drawable.shape_btn_nomargin));
        tView.setTextColor(ct.getResources().getColor(R.color.black));
        tView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
        return tView;
    }
}
