package com.uas.appworks.model.bean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/6 19:52
 */

public class MenuTypeBean {

    /**
     * typeName : 企业应用
     * typeList : [{"isLocalMenu":true,"menuName":"数据查询","menuIcon":"ic_menu","menuActivity":"","menuUrl":"","isHide":false},{"isLocalMenu":true,"menuName":"报表统计","menuIcon":"ic_menu","menuActivity":"","menuUrl":"","isHide":false}]
     */

    private String typeName;
    private boolean typeVisible;
    private List<TypeListBean> typeList;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isTypeVisible() {
        return typeVisible;
    }

    public void setTypeVisible(boolean typeVisible) {
        this.typeVisible = typeVisible;
    }

    public List<TypeListBean> getTypeList() {
        return typeList;
    }

    public void setTypeList(List<TypeListBean> typeList) {
        this.typeList = typeList;
    }

    public static class TypeListBean {
        /**
         * isLocalMenu : true
         * menuName : 数据查询
         * menuIcon : ic_menu
         * menuActivity :
         * menuUrl :
         * isHide : false
         */

        private boolean isLocalMenu;
        private String menuName;
        private String menuIcon;
        private String menuActivity;
        private String menuUrl;
        private boolean isHide;

        public boolean isIsLocalMenu() {
            return isLocalMenu;
        }

        public void setIsLocalMenu(boolean isLocalMenu) {
            this.isLocalMenu = isLocalMenu;
        }

        public String getMenuName() {
            return menuName;
        }

        public void setMenuName(String menuName) {
            this.menuName = menuName;
        }

        public String getMenuIcon() {
            return menuIcon;
        }

        public void setMenuIcon(String menuIcon) {
            this.menuIcon = menuIcon;
        }

        public String getMenuActivity() {
            return menuActivity;
        }

        public void setMenuActivity(String menuActivity) {
            this.menuActivity = menuActivity;
        }

        public String getMenuUrl() {
            return menuUrl;
        }

        public void setMenuUrl(String menuUrl) {
            this.menuUrl = menuUrl;
        }

        public boolean isIsHide() {
            return isHide;
        }

        public void setIsHide(boolean isHide) {
            this.isHide = isHide;
        }
    }
}
