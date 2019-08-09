package com.uas.appworks.CRM.erp.model;

import com.common.data.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/12/19.
 */

public class CycleCount {
    private boolean isExpand;
    private String id;
    private List<Data> datas;
    private List<Data> lowDatas;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public List<Data> getDatas() {
        return datas;
    }

    public List<Data> getLowDatas() {
        return lowDatas;
    }

    public void setDatas(List<Data> datas) {
        this.datas = datas;
    }

    public void addDatas(List<Data> datas) {
        if (this.datas == null) {
            this.datas = new ArrayList<>();
        }
        this.datas.addAll(datas);
    }

    public void addData(Data data) {
        if (this.datas == null) {
            this.datas = new ArrayList<>();
        }
        if (lowDatas == null) {
            lowDatas = new ArrayList<>();
            lowDatas.add(data);
        } else if (lowDatas.size() < 3) {
            lowDatas.add(data);
            if (ListUtils.getSize(lowDatas) > 3) {
                lowDatas.remove(lowDatas.size() - 1);
            }
        }
        this.datas.add(data);
    }

    public void addData(int i, Data data) {
        if (this.datas == null) {
            this.datas = new ArrayList<>();
        }
        if (lowDatas == null) {
            lowDatas = new ArrayList<>();
            lowDatas.add(data);
        } else {
            lowDatas.add(i, data);
            if (ListUtils.getSize(lowDatas) > 3) {
                lowDatas.remove(lowDatas.size() - 1);
            }
        }
        this.datas.add(i, data);
    }

    public static class Data {
        private boolean hasTwo;
        private String caption;
        private String values;
        private String caption2;
        private String values2;

        public Data(String caption, String values) {
            this.caption = caption;
            this.values = values;
        }

        public boolean isHasTwo() {
            return hasTwo;
        }

        public void setHasTwo(boolean hasTwo) {
            this.hasTwo = hasTwo;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getValues() {
            return values;
        }

        public void setValues(String values) {
            this.values = values;
        }

        public String getCaption2() {
            return caption2;
        }

        public void setCaption2(String caption2) {
            this.caption2 = caption2;
        }

        public String getValues2() {
            return values2;
        }

        public void setValues2(String values2) {
            this.values2 = values2;
        }
    }


}
