package com.xzjmyk.pm.activity.ui.erp.adapter.oa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.sortlist.BaseSortModel;
import com.core.model.SelectEmUser;
import com.xzjmyk.pm.activity.R;

import java.util.List;


/**
 * Created by Bitliker on 2017/2/14.
 */

public class SelectCollisionAdapter extends BaseAdapter {
    private List<BaseSortModel<SelectEmUser>> listData;
    boolean BaseSortEnable=true;//默认是分类展示
    
    public void setBaseSortEnable(boolean baseSortEnable) {
        BaseSortEnable = baseSortEnable;
    }

    public SelectCollisionAdapter(List<BaseSortModel<SelectEmUser>> listData) {
        this.listData = listData;
    }

    public List<BaseSortModel<SelectEmUser>> getListData() {
        return listData;
    }

    public void setListData(List<BaseSortModel<SelectEmUser>> listData) {
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return ListUtils.isEmpty(listData) ? 0 : listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(MyApplication.getInstance()).
                    inflate(R.layout.item_select_active, parent, false);
            holder.tag_tv = (TextView) convertView.findViewById(R.id.tag_tv);
            holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) convertView.findViewById(R.id.sub_tv);
            holder.flight_tv = (TextView) convertView.findViewById(R.id.flight_tv);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
            holder.head_img = (ImageView) convertView.findViewById(R.id.head_img);
            holder.tag_view = convertView.findViewById(R.id.tag_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (BaseSortEnable){
            onBindViewHolder(holder, position);
        }else{
            onBindViewHolderBy(holder, position);
        }
       
        return convertView;

    }
    
    public void onBindViewHolderBy(ViewHolder holder, final int position){
        final BaseSortModel<SelectEmUser> model = listData.get(position);
        holder.cb.setChecked(model.isClick());
        holder.tag_tv.setVisibility(View.GONE);
        holder.tag_view.setVisibility(View.GONE);
        AvatarHelper.getInstance().display(model.getBean().getImId() + "", holder.head_img, true, false);//设定为每次刷新都会去删除缓存重新获取数据
        holder.name_tv.setText(model.getBean().getEmName());
        holder.sub_tv.setText((StringUtil.isEmpty(model.getBean().getDepart()) ? "" : (model.getBean().getDepart() + ">"))
                + " " + (StringUtil.isEmpty(model.getBean().getPosition()) ? "" : model.getBean().getPosition()));
        holder.cb.setFocusable(false);
        holder.cb.setClickable(false);
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BaseSortModel<SelectEmUser> model = listData.get(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        int section = getSectionForPosition(position);
        if (position == getPositionForSection(section)) {
            holder.tag_tv.setVisibility(View.VISIBLE);
            holder.tag_view.setVisibility(View.VISIBLE);
            holder.tag_tv.setText(model.getFirstLetter());
        } else {
            holder.tag_tv.setVisibility(View.GONE);
            holder.tag_view.setVisibility(View.GONE);
        }
        holder.cb.setChecked(model.isClick());
        AvatarHelper.getInstance().display(model.getBean().getImId() + "", holder.head_img, true, false);//设定为每次刷新都会去删除缓存重新获取数据
        holder.name_tv.setText(model.getBean().getEmName());
        holder.sub_tv.setText((StringUtil.isEmpty(model.getBean().getDepart()) ? "" : (model.getBean().getDepart() + ">"))
                + " " + (StringUtil.isEmpty(model.getBean().getPosition()) ? "" : model.getBean().getPosition()));
        holder.cb.setFocusable(false);
        holder.cb.setClickable(false);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < listData.size(); i++) {
            String sortStr = listData.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return listData.get(position).getFirstLetter().charAt(0);
    }


    class ViewHolder {
        TextView tag_tv,
                name_tv,
                sub_tv,
                flight_tv;
        CheckBox cb;
        ImageView head_img;
        View tag_view;
    }
}
