package com.uas.appworks.datainquiry.bean;

import java.util.List;

/**
 * Created by RaoMeng on 2017/8/19.
 */
public class DataInquiryFlexBean {
    private List<RowBean> mRowBeans;
    private boolean isFlex;

    public List<RowBean> getRowBeans() {
        return mRowBeans;
    }

    public void setRowBeans(List<RowBean> rowBeans) {
        mRowBeans = rowBeans;
    }

    public boolean isFlex() {
        return isFlex;
    }

    public void setIsFlex(boolean isFlex) {
        this.isFlex = isFlex;
    }

    public static class RowBean {
        private List<RowChildBean> mRowChildBeans;

        public List<RowChildBean> getRowChildBeans() {
            return mRowChildBeans;
        }

        public void setRowChildBeans(List<RowChildBean> rowChildBeans) {
            mRowChildBeans = rowChildBeans;
        }

        public static class RowChildBean {
            private String mCaption;
            private String mValue;
            private String mField;
            private int width;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getCaption() {
                return mCaption;
            }

            public void setCaption(String caption) {
                mCaption = caption;
            }

            public String getValue() {
                return mValue;
            }

            public void setValue(String value) {
                mValue = value;
            }

            public String getField() {
                return mField;
            }

            public void setField(String field) {
                mField = field;
            }
        }
    }
}
