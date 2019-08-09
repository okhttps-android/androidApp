package com.modular.booking.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.common.LogUtil;
import com.core.app.MyApplication;
import com.modular.booking.R;
import com.modular.booking.activity.utils.ShoppingCart;
import com.modular.booking.base.BaseViewHolder;
import com.modular.booking.model.Product;
import com.modular.booking.model.ShoppingEntity;
import com.modular.booking.widget.ShoppingCountView;

/**
 * author: cheikh.wang on 16/11/24
 * email: wanghonghi@126.com
 */

public class ShoppingCartItemViewHolder extends BaseViewHolder<ShoppingEntity> {
    
    TextView mNameTxt;
    TextView mPriceTxt;
    ShoppingCountView mShoppingCountView;

    public ShoppingCartItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bindView(View view) {
        mNameTxt= view.findViewById(R.id.txt_name);
        mPriceTxt= view.findViewById(R.id.txt_price);
        mShoppingCountView= view.findViewById(R.id.shopping_count_view);
    }

    public void bind(ShoppingEntity entity) {
        mNameTxt.setText(entity.getName());
        mPriceTxt.setText(MyApplication.getInstance().getString(R.string.label_price, entity.getTotalPrice()));
        
        final Product finalProduct = entity.getProduct();
        int quantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
        LogUtil.d("ShopCar","更新商品数量："+quantity);
        mShoppingCountView.setShoppingCount(quantity);
        mShoppingCountView.setOnShoppingClickListener(new ShoppingCountView.ShoppingClickListener() {
            @Override
            public void onAddClick(int num) {
                if (!ShoppingCart.getInstance().add(finalProduct)) {
                    LogUtil.d("ShopCar","添加商品失败！");
                    int oldQuantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
                    mShoppingCountView.setShoppingCount(oldQuantity);
                }
            }

            @Override
            public void onMinusClick(int num) {
                if (!ShoppingCart.getInstance().delete(finalProduct)) {
             
                    int oldQuantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
                    mShoppingCountView.setShoppingCount(oldQuantity);
                }
            }
        });
    }
}
