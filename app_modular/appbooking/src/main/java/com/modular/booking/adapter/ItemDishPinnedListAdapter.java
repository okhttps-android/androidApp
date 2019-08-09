package com.modular.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.PinnedSectionListView;
import com.modular.booking.R;
import com.modular.booking.activity.utils.ShoppingCart;
import com.modular.booking.model.Product;
import com.modular.booking.model.ProductCategory;
import com.modular.booking.widget.ShoppingCountView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arison on 2018/1/24.
 */

public class ItemDishPinnedListAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter{

   private List<ProductCategory> productCategories=new ArrayList<>();
   
   private LayoutInflater layoutInflater;

    private View mAnimTargetView;
    
   public ItemDishPinnedListAdapter(Context ct, List<ProductCategory> datas){
       this.productCategories=datas;
       this.layoutInflater=LayoutInflater.from(ct);
       
   }

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    public void setAnimTargetView(View animTargetView) {
        mAnimTargetView = animTargetView;
    }
    @Override
    public boolean isItemViewTypePinned(int viewType) {
        
       return viewType==ProductCategory.SECTION;
    }

    @Override
    public int getItemViewType(int position) {
        return productCategories.get(position).getType();
    }
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return productCategories.size();
    }

    @Override
    public ProductCategory getItem(int i) {
        return productCategories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        
       ProductCategory productCategory=productCategories.get(position);
       if (productCategory.getType()==ProductCategory.SECTION){
           ItemHeaderHolder viewHolder;
//           if (convertView==null){
               convertView = layoutInflater.inflate(
                       R.layout.item_dish_list_head, null);
               viewHolder=new ItemHeaderHolder(convertView);
               convertView.setTag(viewHolder);
//           }else{
//               viewHolder= (ItemHeaderHolder) convertView.getTag();
//           }
          viewHolder.title.setText(productCategory.getName());
       }else if(productCategory.getType()==ProductCategory.ITEM){
           ItemViewHolder viewHolder;
//           if (convertView==null){
               convertView = layoutInflater.inflate(
                       R.layout.item_dish_list, null);
               viewHolder=new ItemViewHolder( convertView);
               convertView.setTag(viewHolder);
//           }else{
//               viewHolder= (ItemViewHolder) convertView.getTag();
//           }
       
            final Product product=productCategory.getProduct();
           int quantity = ShoppingCart.getInstance().getQuantityForProduct(product);
            viewHolder.product=product;
           AvatarHelper.getInstance().display("http://p0.meituan.net/deal/__16971854__3919079.jpg@380w_214h_1e_1c",  viewHolder. img_product_photo,true);
        
           LogUtil.d("ShopCar","---item----"+position);
           viewHolder. shoppingCountView.setShoppingCount(quantity);
          // viewHolder. shoppingCountView.setAnimTargetView(mAnimTargetView);
           viewHolder. shoppingCountView.setOnShoppingClickListener(new ShoppingCountView.ShoppingClickListener() {
              @Override
              public void onAddClick(int num) {
                  if (!ShoppingCart.getInstance().add(product)) {
                      
                  }
                  LogUtil.d("ShopCar", JSON.toJSONString(ShoppingCart.getInstance().getShoppingList()));
              }

              @Override
              public void onMinusClick(int num) {
                  if (!ShoppingCart.getInstance().delete(product)) {

                  }
                  LogUtil.d("ShopCar", JSON.toJSONString(ShoppingCart.getInstance().getShoppingList()));
              }
          });
           viewHolder.  txt_product_name.setText(productCategory.getProduct().getName());
           viewHolder.  txt_product_month_sales.setText("月售0");
           viewHolder.  txt_product_price.setText("￥"+productCategory.getProduct().getUnitItems().get(0).getPrice());
       }
        
        return convertView;
    }
    
    class ItemHeaderHolder{
        TextView title;
        public ItemHeaderHolder(View convertView) {
          title=convertView.findViewById(R.id.txt_title);
        }
    }
    
   public class ItemViewHolder{
       public  ImageView img_product_photo;
       public  TextView  txt_product_name;
       public  TextView  txt_product_month_sales;
       public  TextView  txt_product_price;
       public  ShoppingCountView shoppingCountView;
       public  RelativeLayout rl_dish_product ;
       public  Product  product;
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
