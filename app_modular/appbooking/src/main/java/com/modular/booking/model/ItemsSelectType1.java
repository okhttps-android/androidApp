package com.modular.booking.model;

/**
 * Created by Arison on 2017/11/8.
 */
public class ItemsSelectType1 {
    
    private String name;//名称
    private boolean selected=false;//是否被选中

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
