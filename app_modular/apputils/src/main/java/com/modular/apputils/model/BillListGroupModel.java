package com.modular.apputils.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 单据列表组
 */
public class BillListGroupModel {
    private int id;//id
    private int groupIndex;//当前组所在的整个显示集合里面的索引
    private String status;
    private List<BillListField> billFields;
    private List<BillListField> hideBillFields;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public List<BillListField> getBillFields() {
        return billFields;
    }

    public void setBillFields(List<BillListField> billFields) {
        this.billFields = billFields;
    }

    public List<BillListField> getHideBillFields() {
        return hideBillFields;
    }

    public void setHideBillFields(List<BillListField> hideBillFields) {
        this.hideBillFields = hideBillFields;
    }

    public static class BillListField implements Parcelable{
        private String statusKey;
        private int groupIndex;//所在组索引
        private String caption;//字段名称
        private String field;//字段
        private String value;//值

        public BillListField() {
        }

        protected BillListField(Parcel in) {
            statusKey = in.readString();
            groupIndex = in.readInt();
            caption = in.readString();
            field = in.readString();
            value = in.readString();
        }

        public static final Creator<BillListField> CREATOR = new Creator<BillListField>() {
            @Override
            public BillListField createFromParcel(Parcel in) {
                return new BillListField(in);
            }

            @Override
            public BillListField[] newArray(int size) {
                return new BillListField[size];
            }
        };

        public String getStatusKey() {
            return statusKey;
        }

        public void setStatusKey(String statusKey) {
            this.statusKey = statusKey;
        }


        public int getGroupIndex() {
            return groupIndex;
        }

        public void setGroupIndex(int groupIndex) {
            this.groupIndex = groupIndex;
        }

        public String getCaption() {
            return caption == null ? "" : caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getValue() {
            return value == null ? "" : value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(statusKey);
            parcel.writeInt(groupIndex);
            parcel.writeString(caption);
            parcel.writeString(field);
            parcel.writeString(value);
        }
    }
}
