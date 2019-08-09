package com.modular.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.app.MyApplication;
import com.modular.booking.R;
import com.modular.booking.model.ShoppingEntity;

import java.util.ArrayList;
import java.util.List;

public class ItemFoodsDishlistAdapter extends BaseAdapter {

    private List<ShoppingEntity> objects = new ArrayList<ShoppingEntity>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemFoodsDishlistAdapter(Context context,List<ShoppingEntity> data) {
        this.context = context;
        this.objects=data;
        this.layoutInflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public ShoppingEntity getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_foods_dishlist, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((ShoppingEntity)getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(ShoppingEntity object, ViewHolder holder) {
        holder.tvDishName.setText(object.getName());
        holder.tvDishNum.setText(MyApplication.getInstance().getString(R.string.label_price, object.getUnitPrice())+"/份");
        holder.tvDishPrice.setText(""+object.getQuantity());
    }

    protected class ViewHolder {
        private ImageView ivDish;
        private TextView tvDishName;
        private TextView tvDishNum;
        private TextView tvDishPrice;

        public ViewHolder(View view) {
            ivDish = (ImageView) view.findViewById(R.id.iv_dish);
            tvDishName = (TextView) view.findViewById(R.id.tv_dish_name);
            tvDishNum = (TextView) view.findViewById(R.id.tv_dish_num);
            tvDishPrice = (TextView) view.findViewById(R.id.tv_dish_price);
        }
    }
}
