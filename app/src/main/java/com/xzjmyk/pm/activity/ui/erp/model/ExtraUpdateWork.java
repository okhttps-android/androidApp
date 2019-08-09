package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * @author :LiuJie 2015年7月27日 下午2:38:41
 * @注释:加班申请
 */
public class ExtraUpdateWork {
    private String wo_worktask;
    private String enuu;
    private String emcode;
    private int wo_id;

    public int getWo_id() {
        return wo_id;
    }

    public void setWo_id(int wo_id) {
        this.wo_id = wo_id;
    }

    public String getWo_worktask() {
        return wo_worktask;
    }

    public void setWo_worktask(String wo_worktask) {
        this.wo_worktask = wo_worktask;
    }

    public String getEnuu() {
        return enuu;
    }

    public void setEnuu(String enuu) {
        this.enuu = enuu;
    }

    public String getEmcode() {
        return emcode;
    }

    public void setEmcode(String emcode) {
        this.emcode = emcode;
    }
}
