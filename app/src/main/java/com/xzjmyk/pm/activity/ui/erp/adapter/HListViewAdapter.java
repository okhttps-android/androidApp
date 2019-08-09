package com.xzjmyk.pm.activity.ui.erp.adapter;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

@SuppressLint("NewApi")
public class HListViewAdapter extends BaseAdapter {

    private Context ct;
    private LayoutInflater inflater;
    private ArrayList<ArrayList<String>> lists;

    public HListViewAdapter(Context context, ArrayList<ArrayList<String>> lists) {
        super();
        this.lists = lists;
        this.ct = context;
        inflater = LayoutInflater.from(context);
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

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int index, View view, ViewGroup arg2) {
        ArrayList<String> list = lists.get(index);
        TextView[] views = new TextView[list.size()];
        
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_empty, null);

            LinearLayout topview = (LinearLayout) view.findViewById(R.id.ly_top_view); //根部布局
            LinearLayout ly_grid = new LinearLayout(ct);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            ly_grid.setLayoutParams(lp);
            ly_grid.setOrientation(LinearLayout.HORIZONTAL);
            //水平线条
            View horizontal = new View(ct);
            ViewGroup.LayoutParams hp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    CommonUtil.dip2px(ct, 1));
            horizontal.setLayoutParams(hp);
            horizontal.setBackground(ct.getResources().getDrawable(R.color.light_gray));

            /**@注释：创建textview  */
            for (int i = 0; i < list.size(); i++) {
                //垂直线条
                View vertical = new View(ct);
                ViewGroup.LayoutParams vp = new ViewGroup.LayoutParams(CommonUtil.dip2px(ct, 1),
                        ViewGroup.LayoutParams.MATCH_PARENT);
                vertical.setLayoutParams(vp);
                vertical.setBackground(ct.getResources().getDrawable(R.color.light_gray));

                TextView tView = new TextView(ct);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                if (i == 0) {
                    tView.setWidth(CommonUtil.dip2px(ct, 55));
                } else {
                    tView.setWidth(CommonUtil.dip2px(ct, 70));
                }
                tView.setMaxLines(8);
                //tView.setLines(1);
                //tView.setBackgroundColor(ct.getResources().getColor(R.color.red));
                tView.setLayoutParams(tp);
                tView.setGravity(Gravity.CENTER);
                tView.setPadding(CommonUtil.dip2px(ct, 10), CommonUtil.dip2px(ct, 10),
                        CommonUtil.dip2px(ct, 10), CommonUtil.dip2px(ct, 10));
                tView.setTextColor(ct.getResources().getColor(R.color.black));
                tView.setTextSize(12);
                views[i] = tView;
                //ly_grid.setGravity(Gravity.CENTER);
                ly_grid.addView(vertical);
                ly_grid.addView(tView);
            }

            topview.addView(ly_grid);
            topview.addView(horizontal);

            view.setTag(views);
        } else {
            views = (TextView[]) view.getTag();
        }
        view.setBackgroundColor(Color.WHITE);
        for (int i = 0; i < views.length; i++) {
            views[i].setText(list.get(i));
            views[i].setTextColor(ct.getResources().getColor(R.color.black));
        }

        if (index == 0) {
            //view.setBackgroundResource(R.color.head_bg);
        } else {
            if (index % 2 != 0) {
                view.setBackgroundColor(Color.argb(250, 255, 255, 255));
            } else {
                view.setBackgroundColor(Color.argb(250, 224, 243, 250));
            }
        }
        return view;
    }
}
