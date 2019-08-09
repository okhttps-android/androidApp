package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * @author :LiuJie 2015年7月27日 下午3:17:06
 * @注释:出差申请单
 */
public class TravelUpdateEntity {
    private String fp_v3;// 出差线路----出差事由
    private String fp_prestartdate;
    private String fp_preenddate;
    private String enuu;
    private String emcode;
    private int fp_id;

    public int getFp_id() {
        return fp_id;
    }

    public void setFp_id(int fp_id) {
        this.fp_id = fp_id;
    }

    public String getFp_v3() {
        return fp_v3;
    }

    public void setFp_v3(String fp_v3) {
        this.fp_v3 = fp_v3;
    }

    public String getFp_prestartdate() {
        return fp_prestartdate;
    }

    public void setFp_prestartdate(String fp_prestartdate) {
        this.fp_prestartdate = fp_prestartdate;
    }

    public String getFp_preenddate() {
        return fp_preenddate;
    }

    public void setFp_preenddate(String fp_preenddate) {
        this.fp_preenddate = fp_preenddate;
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
