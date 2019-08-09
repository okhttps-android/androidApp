package com.modular.booking.activity.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.modular.booking.R;
import com.modular.booking.adapter.LayoutShoppingCartItemAdapter;
import com.modular.booking.model.ShoppingEntity;

import java.util.List;


/**
 * 购物车面板
 */
public class ShoppingCartPanel extends FrameLayout {
    
    TextView mClearTxt;
//    RecyclerView mRecyclerView;
     ListView  lv_data;

    private LayoutShoppingCartItemAdapter layoutShoppingCartItemAdapter;

//    private ShoppingCartListAdapter mAdapter;

    public ShoppingCartPanel(Context context) {
        this(context, null);
    }

    public ShoppingCartPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_shopping_cart_panel, this);
        mClearTxt=view.findViewById(R.id.txt_clear);
//        mRecyclerView=view.findViewById(R.id.recycler_view);
        lv_data=view.findViewById(R.id.lv_data);
        mClearTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearShoppingCart();
            }
        });
        layoutShoppingCartItemAdapter=new LayoutShoppingCartItemAdapter(context);
        initViews();
        refreshPanel();
    }

    private void initViews() {
//     mAdapter = new ShoppingCartListAdapter();
       lv_data.setAdapter(layoutShoppingCartItemAdapter);
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void refreshPanel() {
        ShoppingCart shoppingCart = ShoppingCart.getInstance();
        List<ShoppingEntity> entities = shoppingCart.getShoppingList();
        LogUtil.d("ShopCar", "面板data："+JSON.toJSONString(entities));
        layoutShoppingCartItemAdapter.setItems(entities);
    }
    
    /**
     * 清空购物车
     */
    private void clearShoppingCart() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_clear_shopping_cart_title)
                .setMessage(R.string.dialog_clear_shopping_cart_message)
                .setCancelable(false)
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShoppingCart.getInstance().clearAll();
//                        mAdapter.clearItems();
                        
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }
}