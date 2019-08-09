package com.modular.booking.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arison on 2018/1/24.
 * 产品类别
 */
public class ProductCategory implements Parcelable {

    public static final int ITEM = 0;
    public static final int SECTION = 1;
    public int type=ProductCategory.SECTION; 
    private int DishCategoryId;
    private String  ParentCode;
    private String  Code;
    private String  Name;
    private Product product;
    private List<Product> products=new ArrayList<>();

    public int getDishCategoryId() {
        return DishCategoryId;
    }

    public void setDishCategoryId(int dishCategoryId) {
        DishCategoryId = dishCategoryId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getParentCode() {
        return ParentCode;
    }

    public void setParentCode(String parentCode) {
        ParentCode = parentCode;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ProductCategory() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.DishCategoryId);
        dest.writeString(this.ParentCode);
        dest.writeString(this.Code);
        dest.writeString(this.Name);
        dest.writeParcelable(this.product, flags);
        dest.writeTypedList(this.products);
    }

    protected ProductCategory(Parcel in) {
        this.type = in.readInt();
        this.DishCategoryId = in.readInt();
        this.ParentCode = in.readString();
        this.Code = in.readString();
        this.Name = in.readString();
        this.product = in.readParcelable(Product.class.getClassLoader());
        this.products = in.createTypedArrayList(Product.CREATOR);
    }

    public static final Creator<ProductCategory> CREATOR = new Creator<ProductCategory>() {
        @Override
        public ProductCategory createFromParcel(Parcel source) {
            return new ProductCategory(source);
        }

        @Override
        public ProductCategory[] newArray(int size) {
            return new ProductCategory[size];
        }
    };
}
