package com.modular.booking.activity.utils;

import com.common.LogUtil;
import com.modular.booking.activity.services.DishSelectActivity;
import com.modular.booking.model.Product;
import com.modular.booking.model.ShoppingEntity;
import com.modular.booking.utils.EventMessage;
import com.modular.booking.utils.RxBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arison on 2018/1/30.
 */

public class ShoppingCart {
    private String mBusinessId;
    private Map<String, ShoppingEntity> mShoppingList;

    private static ShoppingCart instance;

    public static ShoppingCart getInstance() {
        if (instance == null) {
            LogUtil.d("ShopCar","购物车初始化 init()");
            instance = new ShoppingCart();
        }

        return instance;
    }

    private ShoppingCart() {
        mShoppingList = new HashMap<>();
    }

    /**
     * 往购物车内添加商品
     * @param product 添加的商品对象
     * @return 是否添加成功
     */
    public boolean add(Product product) {
        String id = product.getCode();
        if (mShoppingList.isEmpty()) {
            // 第一次添加需要记录商家ID
            mBusinessId = product.getBusinessId();
            // 通过Product对象初始化一个ShoppingEntity对象
            ShoppingEntity entity = ShoppingEntity.initWithProduct(product);
            mShoppingList.put(id, entity);
//            sendChangeEvent();
            RxBus.getInstance().send(
                    new EventMessage(DishSelectActivity.TAG));
            return true;
        } else if (mBusinessId.equals(product.getBusinessId())) {
            ShoppingEntity entity = mShoppingList.containsKey(id) ? mShoppingList.get(id) : null;
            if (entity == null) {
                entity = ShoppingEntity.initWithProduct(product);
            } else {
                entity.setQuantity(entity.getQuantity() + 1);
            }
            mShoppingList.put(id, entity);
//            sendChangeEvent();
            RxBus.getInstance().send(
                    new EventMessage(DishSelectActivity.TAG));
            return true;
        }
        return false;
    }


    /**
     * 往购物车里减少商品
     * @param product 需要减少的商品对象
     * @return 是否减少成功
     */
    public boolean delete(Product product) {
        String id = product.getCode();
        if (mShoppingList.containsKey(id)) {
            ShoppingEntity entity = mShoppingList.get(id);
            int originQuantity = entity.getQuantity();
            if (originQuantity > 1) {
                entity.setQuantity(--originQuantity);
                mShoppingList.put(id, entity);
//                sendChangeEvent();
                RxBus.getInstance().send(
                        new EventMessage(DishSelectActivity.TAG));
                return true;
            } else if (originQuantity == 1) {
                mShoppingList.remove(id);
//                sendChangeEvent();
                RxBus.getInstance().send(
                        new EventMessage(DishSelectActivity.TAG));
                return true;
            }
        }
        return false;
    }


    /**
     * 往购物车里添加指定数量的商品
     * @param product 需要添加的商品对象
     * @return 是否添加成功
     */
    public boolean set(Product product, int quantity) {

        return false;
    }


    /**
     * 清空购物车里的所有数据
     */
    public void clearAll() {
        mShoppingList.clear();
        RxBus.getInstance().send(
                new EventMessage(DishSelectActivity.TAG));
//        sendChangeEvent();
    }


    /**
     * 获取商家ID
     * @return 商家ID
     */
    public String getBusinessId() {
        return mBusinessId;
    }

    /**
     * 获取购物车里所有商品的总价
     * @return 商品总价
     */
    public double getTotalPrice() {
        double totalPrice = 0.0d;
        for (ShoppingEntity entry : mShoppingList.values()) {
            totalPrice += entry.getTotalPrice();
        }

        return totalPrice;
    }


    /**
     * 获取购物车里所有商品的数量
     * @return 商品数量
     */
    public int getTotalQuantity() {
        int totalQuantity = 0;
        for (ShoppingEntity entry : mShoppingList.values()) {
            totalQuantity += entry.getQuantity();
        }

        return totalQuantity;
    }

    /**
     * 获取购物车里指定商品的数量
     * @param product 指定的商品
     * @return 商品数量
     */
    public int getQuantityForProduct(Product product) {
        String id = product.getCode();
        if (mShoppingList.containsKey(id)) {
            return mShoppingList.get(id).getQuantity();
        }

        return 0;
    }

    /**
     * 获取购物车的选购列表
     * @return 选购列表
     */
    public List<ShoppingEntity> getShoppingList() {
        List<ShoppingEntity> entities = new ArrayList<>();
        for (ShoppingEntity entry : mShoppingList.values()) {
            entities.add(entry);
        }
        return entities;
    }



  
}
