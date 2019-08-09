package com.modular.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.app.MyApplication;
import com.modular.booking.R;
import com.modular.booking.activity.utils.ShoppingCart;
import com.modular.booking.model.Product;
import com.modular.booking.model.ShoppingEntity;
import com.modular.booking.widget.ShoppingCountView;

import java.util.ArrayList;
import java.util.List;

public class LayoutShoppingCartItemAdapter extends BaseAdapter {

    private List<ShoppingEntity> objects = new ArrayList<ShoppingEntity>();

    private Context context;
    private LayoutInflater layoutInflater;

    public LayoutShoppingCartItemAdapter(Context context) {
        this.context = context;
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

    public List<ShoppingEntity> getObjects() {
        return objects;
    }

    public void setItems(List<ShoppingEntity> items) {
        objects.clear();
        objects.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_shopping_cart_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((ShoppingEntity)getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(ShoppingEntity entity, ViewHolder holder) {
        holder.mNameTxt.setText(entity.getName());
        holder.mPriceTxt.setText(MyApplication.getInstance().getString(R.string.label_price, entity.getTotalPrice()));
        final Product finalProduct = entity.getProduct();
        int quantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
        holder. mShoppingCountView.setShoppingCount(quantity);
        holder. mShoppingCountView.setOnShoppingClickListener(new ShoppingCountView.ShoppingClickListener() {
            @Override
            public void onAddClick(int num) {
                if (!ShoppingCart.getInstance().add(finalProduct)) {
                    int oldQuantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
                    holder.  mShoppingCountView.setShoppingCount(oldQuantity);
                }
            }

            @Override
            public void onMinusClick(int num) {
                if (!ShoppingCart.getInstance().delete(finalProduct)) {
                    int oldQuantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
                    holder. mShoppingCountView.setShoppingCount(oldQuantity);
                }
            }
        });
    }

    protected class ViewHolder {
        private TextView mNameTxt;
        private TextView mPriceTxt;
        private ShoppingCountView mShoppingCountView;

        public ViewHolder(View view) {
            mNameTxt = (TextView) view.findViewById(R.id.txt_name);
            mPriceTxt = (TextView) view.findViewById(R.id.txt_price);
            mShoppingCountView = (ShoppingCountView) view.findViewById(R.id.shopping_count_view);
        }
    }
}
