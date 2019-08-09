package com.uas.appworks.model.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/6 19:52
 */

public class WorkMenuBean {

    /**
     * moduleName : 企业应用
     * moduleList : [{"isLocalMenu":true,"menuName":"数据查询","menuIcon":"ic_menu","menuActivity":"","menuUrl":"","isHide":false},{"isLocalMenu":true,"menuName":"报表统计","menuIcon":"ic_menu","menuActivity":"","menuUrl":"","isHide":false}]
     */
    @JSONField(name = "moduleName")
    private String moduleName = "";
    @JSONField(name = "isLocalModule")
    private boolean isLocalModule = true;
    @JSONField(name = "moduleTag")
    private String moduleTag = "";
    @JSONField(name = "moduleId")
    private String moduleId = "";
    @JSONField(name = "moduleVisible")
    private boolean moduleVisible = true;
    @JSONField(name = "moduleList")
    private List<ModuleListBean> moduleList;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isLocalModule() {
        return isLocalModule;
    }

    public void setIsLocalModule(boolean isLocalModule) {
        this.isLocalModule = isLocalModule;
    }

    public String getModuleTag() {
        return moduleTag;
    }

    public void setModuleTag(String moduleTag) {
        this.moduleTag = moduleTag;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public boolean isModuleVisible() {
        return moduleVisible;
    }

    public void setModuleVisible(boolean moduleVisible) {
        this.moduleVisible = moduleVisible;
    }

    public List<ModuleListBean> getModuleList() {
        return moduleList;
    }

    public void setModuleList(List<ModuleListBean> moduleList) {
        this.moduleList = moduleList;
    }

    public static class ModuleListBean {
        /**
         * isLocalMenu : true
         * menuName : 数据查询
         * menuIcon : ic_menu
         * menuActivity :
         * menuTag:
         * menuUrl :
         * caller : ,
         * isHide : false
         */

        @JSONField(name = "isLocalMenu")
        private boolean isLocalMenu = true;
        @JSONField(name = "menuName")
        private String menuName = "";
        @JSONField(name = "menuIcon")
        private String menuIcon = "";
        @JSONField(name = "menuActivity")
        private String menuActivity = "";
        @JSONField(name = "menuTag")
        private String menuTag = "";
        @JSONField(name = "menuUrl")
        private String menuUrl = "";
        @JSONField(name = "caller")
        private String caller = "";
        @JSONField(name = "isHide")
        private boolean isHide = false;

        public boolean isLocalMenu() {
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

        public String getMenuTag() {
            return menuTag;
        }

        public void setMenuTag(String menuTag) {
            this.menuTag = menuTag;
        }

        public String getMenuUrl() {
            return menuUrl;
        }

        public void setMenuUrl(String menuUrl) {
            this.menuUrl = menuUrl;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public boolean isHide() {
            return isHide;
        }

        public void setIsHide(boolean isHide) {
            this.isHide = isHide;
        }
    }


    /**
     * 除了TURE,Y和1,其他都为false
     *
     * @param s
     * @return
     */
    public boolean toBoolFalse(String s) {
        if (s != null && (s.equals('Y') || s.equals("1") || s.toUpperCase().equals("TRUE"))) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 除了FALSE,N,0,null,其他都为true
     *
     * @param s
     * @return
     */
    public boolean toBoolTrue(String s) {
        if (s != null) {
            if ((s.equals('N') || s.equals("0") || s.toUpperCase().equals("FALSE"))) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * 将布尔值转换为Y或者N
     *
     * @param b
     * @return
     */
    public String boolToStr(boolean b) {
        if (b) {
            return "Y";
        } else {
            return "N";
        }
    }

    /**
     * 将布尔值转换为1或者0
     *
     * @param b
     * @return
     */
    public int boolToInt(boolean b) {
        if (b) {
            return 1;
        } else {
            return 0;
        }
    }
}
