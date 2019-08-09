package com.core.adapter;

/**
 * Created by Arison on 2017/11/8.
 */
public class ItemsSelectType1 {
    
    private String name;//名称
    private String en_name;//英文名称
    private boolean selected=false;//是否被选中

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEn_name() {
        return en_name;
    }

    public void setEn_name(String en_name) {
        this.en_name = en_name;
    }

    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
