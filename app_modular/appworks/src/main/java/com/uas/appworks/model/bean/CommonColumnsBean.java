package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/24 17:15
 */
public class CommonColumnsBean {


    /**
     * dataIndex : bc_address
     * caption : 企业地址
     * width : 200
     * type : null
     * format : null
     * render : null
     */

    private String dataIndex;
    private String caption;
    private int width;
    private String type;
    private String format;
    private String render;

    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }
}
