package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe B2B附件实体
 * @date 2018/1/23 11:13
 */

public class B2BAttachBean {
    private String mName;
    private String mPath;
    private long mSize;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }
}
