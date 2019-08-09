package com.modular.booking.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.utils.helper.AvatarHelper;
import com.modular.booking.R;
import com.modular.booking.activity.utils.ShoppingCart;
import com.modular.booking.model.Product;
import com.modular.booking.model.ProductCategory;
import com.modular.booking.widget.ShoppingCountView;

import java.util.ArrayList;
import java.util.List;


public class ItemDishPinHeaderAdapter extends SectionedBaseAdapter {

    private LayoutInflater layoutInflater;
    private List<ProductCategory> mCategories=new ArrayList<>();
    private View mAnimTargetView;
    public void setAnimTargetView(View animTargetView) {
        mAnimTargetView = animTargetView;
    }
    public ItemDishPinHeaderAdapter(Activity activity) {
       this. layoutInflater = LayoutInflater.from(activity);
    }

    public void setItems(List<ProductCategory> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }

    @Override
    public Product getItem(int section, int position) {
        List<Product> products = mCategories.get(section).getProducts();
        return products.get(position);
    }

    @Override
    public long getItemId(int section, int position) {
        return position;
    }

    @Override
    public int getSectionCount() {
        return mCategories != null ? mCategories.size() : 0;
    }

    @Override
    public int getCountForSection(int section) {
        if (mCategories != null) {
            List<Product> products = mCategories.get(section).getProducts();
            if (!ListUtils.isEmpty(products)) {
                return products.size();
            }
        }
        return 0;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        ItemDishPinHeaderAdapter.ItemViewHolder viewHolder;
           if (convertView==null){
                convertView = layoutInflater.inflate(
                R.layout.item_dish_list, null);
                viewHolder=new  ItemDishPinHeaderAdapter.ItemViewHolder( convertView);
                convertView.setTag(viewHolder);
           }else{
                viewHolder= (ItemViewHolder) convertView.getTag();
           }

        Product product = getItem(section, position);
        int quantity = ShoppingCart.getInstance().getQuantityForProduct(product);
        viewHolder.product=product;
        AvatarHelper.getInstance().display("http://p0.meituan.net/deal/__16971854__3919079.jpg@380w_214h_1e_1c",  viewHolder. img_product_photo,true);
        LogUtil.d("ShopCar","---item----"+position);
        viewHolder. shoppingCountView.setShoppingCount(quantity);
        viewHolder. shoppingCountView.setAnimTargetView(mAnimTargetView);
        viewHolder. shoppingCountView.setOnShoppingClickListener(new ShoppingCountView.ShoppingClickListener() {
            @Override
            public void onAddClick(int num) {
                if (!ShoppingCart.getInstance().add(product)) {

                }
            }

            @Override
            public void onMinusClick(int num) {
                if (!ShoppingCart.getInstance().delete(product)) {

                }
            }
        });
        viewHolder.  txt_product_name.setText(product.getName());
        viewHolder.  txt_product_month_sales.setText("月售0");
        viewHolder.  txt_product_price.setText("￥"+product.getUnitItems().get(0).getPrice());
        
        return convertView;
    }

    @Override
    public View getSectionHeaderView(int position, View convertView, ViewGroup parent) {
        ItemDishPinHeaderAdapter.ItemHeaderHolder viewHolder;
           if (convertView==null){
                convertView = layoutInflater.inflate(
                R.layout.item_dish_list_head, null);
                viewHolder=new ItemDishPinHeaderAdapter.ItemHeaderHolder(convertView);
                 convertView.setTag(viewHolder);
           }else{
               viewHolder= (ItemHeaderHolder) convertView.getTag();
           }
            ProductCategory productCategory = mCategories.get(position);
            viewHolder.title.setText(productCategory.getName());
           return   convertView;
    }



    class ItemHeaderHolder{
        TextView title;
        public ItemHeaderHolder(View convertView) {
            title=convertView.findViewById(R.id.txt_title);
        }
    }

    public class ItemViewHolder{
        public ImageView img_product_photo;
        public TextView txt_product_name;
        public TextView  txt_product_month_sales;
        public TextView  txt_product_price;
        public ShoppingCountView shoppingCountView;
        public RelativeLayout rl_dish_product ;
        public Product  product;
        public ItemViewHolder(View convertView) {
            img_product_photo=convertView.findViewById(R.id.img_product_photo);
            txt_product_name=convertView.findViewById(R.id.txt_product_name);
            txt_product_month_sales=convertView.findViewById(R.id.txt_product_month_sales);
            txt_product_price =convertView.findViewById(R.id.txt_product_price);
            shoppingCountView=convertView.findViewById(R.id.shopping_count_view);
            rl_dish_product=convertView.findViewById(R.id.rl_dish_product);
        }
    }
}
