package com.xzjmyk.pm.activity.bean.oa;

/**
 * 主表单配置实体类
 * Created by FANGlh on 2016/11/25.
 */
public class CommonDocMainConfigBean  {
    private int FD_DETNO;
    private String FD_CAPTION;
    private String FD_FIELD;
    private Object FD_GROUP;
    private  int MFD_ISDEFAULT;

    public int getFD_DETNO() {
        return FD_DETNO;
    }

    public void setFD_DETNO(int FD_DETNO) {
        this.FD_DETNO = FD_DETNO;
    }

    public String getFD_CAPTION() {
        return FD_CAPTION;
    }

    public void setFD_CAPTION(String FD_CAPTION) {
        this.FD_CAPTION = FD_CAPTION;
    }

    public String getFD_FIELD() {
        return FD_FIELD;
    }

    public void setFD_FIELD(String FD_FIELD) {
        this.FD_FIELD = FD_FIELD;
    }

    public Object getFD_GROUP() {
        return FD_GROUP;
    }

    public void setFD_GROUP(Object FD_GROUP) {
        this.FD_GROUP = FD_GROUP;
    }

    public int getMFD_ISDEFAULT() {
        return MFD_ISDEFAULT;
    }

    public void setMFD_ISDEFAULT(int MFD_ISDEFAULT) {
        this.MFD_ISDEFAULT = MFD_ISDEFAULT;
    }
}
