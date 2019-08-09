package com.modular.booking.adapter;

/**
 * Created by Arison on 2017/9/27.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.modular.booking.R;
import com.modular.booking.model.SBMenuModel;

import java.util.ArrayList;
import java.util.List;

public class ItemBserviceHeaderAdapter extends BaseAdapter {

    private List<SBMenuModel> objects = new ArrayList<SBMenuModel>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemBserviceHeaderAdapter(Context context,List<SBMenuModel> objects ) {
        this.context = context;
        this.objects=objects;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_bservice_header, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((SBMenuModel) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(SBMenuModel object, ViewHolder holder) {
       holder.tvTitle.setText(object.getTitle());
       holder.tvDesc.setText(object.getDesc());
       holder.tvDesc.setTextColor(object.getDescColor());
      // holder.ivMenu.setBackgroundResource(object.getIcon());
       holder.ivMenu.setImageResource(object.getIcon());
       holder.menuModel=object;
     //  AvatarHelper.getInstance().display(object.getUrl(),holder.ivMenu,true);
    }

    public class ViewHolder {
        public SBMenuModel menuModel;
        public TextView tvTitle;
        public TextView tvDesc;
        public ImageView ivMenu;

        public ViewHolder(View view) {
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvDesc = (TextView) view.findViewById(R.id.tv_desc);
            ivMenu = (ImageView) view.findViewById(R.id.iv_menu);
        }
    }
}
