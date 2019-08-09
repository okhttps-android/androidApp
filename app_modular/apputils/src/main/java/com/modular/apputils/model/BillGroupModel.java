package com.modular.apputils.model;

import com.common.data.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态表单录入组对象
 * 单据由几个组组成  1.主表可以存在多个组  2.一个明细表由一个组组成
 * 注:由于类中类太多了,后续根据需求进行优化
 */
public class BillGroupModel implements Serializable {

    private boolean isDeleteAble;//是否可以删除
    private boolean isForm;//是否为主表
    private boolean lastInType;//是否当前单据明细的最后一个
    private int groupIndex;//当前组所在的整个显示集合里面的索引
    private int gridIndex;//当前组在明细表内的索引
    private float minDetno = 10000000;//最小的序号
    private String group;//组名
    private String keyField;//提交时候主表或明细表id字段名称
    private Map<String, Object> mTagMap;//附带信息

    private String billCaller;//表caller

    private List<BillModel> hideBillFields;//当前组隐藏的字段列表
    private List<BillModel> showBillFields;//当前组显示的字段列表
    private List<BillModel> updateBillFields;//当前组可更新的字段列表

    private List<GridTab> mGridTabs;

    public void updateTagMap(String key, Object values) {
        if (key == null) return;
        if (this.mTagMap == null) {
            this.mTagMap = new HashMap<>();
        }
        this.mTagMap.put(key, values);
    }

    public Map<String, Object> getTagMap() {
        return mTagMap;
    }

    public float getMinDetno() {
        return minDetno;
    }

    public void setMinDetno(float minDetno) {
        this.minDetno = minDetno;
    }

    public void setTagMap(Map<String, Object> mTagMap) {
        this.mTagMap = mTagMap;
    }

    public void addHide(BillModel e) {
        if (hideBillFields == null) {
            hideBillFields = new ArrayList<>();
        }
        hideBillFields.add(e);

    }

    public void addShow(BillModel e) {
        if (showBillFields == null) {
            showBillFields = new ArrayList<>();
        }
        showBillFields.add(e);
    }

    public void addUpdate(BillModel e) {
        if (updateBillFields == null) {
            updateBillFields = new ArrayList<>();
        }
        updateBillFields.add(e);
    }

    public boolean isDeleteAble() {
        return isDeleteAble;
    }

    public void setDeleteAble(boolean deleteAble) {
        isDeleteAble = deleteAble;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean form) {
        isForm = form;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getGridIndex() {
        return gridIndex;
    }

    public void setGridIndex(int gridIndex) {
        this.gridIndex = gridIndex;
    }

    public boolean isLastInType() {
        return lastInType;
    }

    public void setLastInType(boolean lastInType) {
        this.lastInType = lastInType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getBillCaller() {
        return billCaller;
    }

    public void setBillCaller(String billCaller) {
        this.billCaller = billCaller;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }


    public List<BillModel> getHideBillFields() {
        return hideBillFields;
    }

    public void setHideBillFields(List<BillModel> hideBillFields) {
        this.hideBillFields = hideBillFields;
    }


    public List<BillModel> getShowBillFields() {
        return showBillFields;
    }

    public void setShowBillFields(List<BillModel> showBillFields) {
        this.showBillFields = showBillFields;
    }

    public List<BillModel> getUpdateBillFields() {
        return updateBillFields;
    }

    public void setUpdateBillFields(List<BillModel> updateBillFields) {
        this.updateBillFields = updateBillFields;
    }

    public List<GridTab> getGridTabs() {
        return mGridTabs;
    }

    public void setGridTabs(List<GridTab> gridTabs) {
        mGridTabs = gridTabs;
    }

    public static class BillTitleModel implements Serializable {
        private int groupIndex;//组索引
        private boolean isDelete;
        private String showName;

        public BillTitleModel(int groupIndex) {
            this.groupIndex = groupIndex;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public void setGroupIndex(int groupIndex) {
            this.groupIndex = groupIndex;
        }

        public boolean isDelete() {
            return isDelete;
        }

        public void setDelete(boolean delete) {
            isDelete = delete;
        }

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }
    }

    /**
     * 字段详情
     */
    public static class BillModel implements Serializable {
        private int id;//id
        private int groupIndex;//所在组索引
        private float detno = 1000000;//序号
        private int length;//字符长度
        private int appwidth;//宽度
        private int isdefault;//是否显示
        private String dbfind;//是否是dbfind字段判定
        private String caption;//字段名称
        private String type;//类型(标题类型为Constants.TYPE_TITLE,不触发点击事件等 )
        private String logicType;//logic类型
        private String readOnly;//是否只读
        private String field;//字段
        private String value;//值
        private String display;//上传值
        private String defValue;//默认值
        private String findFunctionName;//默认值
        private String allowBlank;//是否允许为空(注:当作为标题的时候T:表示可以删除 F:表示不可删除)
        private List<LocalData> localDatas;//获取到的本地选择数据
        private BillJump mBillJump;//判断是否需要要跳转字段
        private List<GridTab> mTabList;
        private boolean updatable;//是否可更新

        public BillModel() {
        }

        public BillModel(BillModel e) {
            this.id = e.id;
            this.groupIndex = e.groupIndex + 1;
            this.length = e.length;
            this.detno = e.detno;
            this.appwidth = e.appwidth;//宽度
            this.isdefault = e.isdefault;//是否
            this.dbfind = e.dbfind;//是否
            this.caption = e.caption;//字
            this.type = e.type;//类型(标
            this.logicType = e.logicType;
            this.readOnly = e.readOnly;//
            this.field = e.field;//字段
            this.value = "";//值
            this.defValue = e.defValue;//
            this.allowBlank = e.allowBlank;
            this.findFunctionName = e.findFunctionName;
            this.mBillJump = e.mBillJump;
            this.mTabList = e.mTabList;
            this.updatable = e.updatable;
        }

        public float getDetno() {
            return detno;
        }

        public void setDetno(float detno) {
            this.detno = detno;
        }

        public BillJump getBillJump() {
            return mBillJump;
        }

        public void setBillJump(BillJump mBillJump) {
            this.mBillJump = mBillJump;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public void setGroupIndex(int groupIndex) {
            this.groupIndex = groupIndex;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getAppwidth() {
            return appwidth;
        }

        public void setAppwidth(int appwidth) {
            this.appwidth = appwidth;
        }

        public int getIsdefault() {
            return isdefault;
        }

        public void setIsdefault(int isdefault) {
            this.isdefault = isdefault;
        }

        public String getDbfind() {
            return dbfind;
        }

        public void setDbfind(String dbfind) {
            this.dbfind = dbfind;
        }

        public String getCaption() {
            return caption == null ? "" : caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getType() {
            return type == null ? "" : type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLogicType() {
            return logicType;
        }

        public void setLogicType(String logicType) {
            this.logicType = logicType;
        }

        public String getReadOnly() {
            return readOnly == null ? "" : readOnly;
        }

        public void setReadOnly(String readOnly) {
            this.readOnly = readOnly;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }


        public String getValue() {
            return StringUtil.isEmpty(value) ? (defValue == null ? "" : defValue) : value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDisplay() {
            return StringUtil.isEmpty(display) ? getValue() : display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public String getDefValue() {
            return defValue;
        }

        public void setDefValue(String defValue) {
            this.defValue = defValue;
        }

        public String getFindFunctionName() {
            return findFunctionName;
        }

        public void setFindFunctionName(String findFunctionName) {
            this.findFunctionName = findFunctionName;
        }

        public String getAllowBlank() {
            return allowBlank == null ? "" : allowBlank;
        }

        public void setAllowBlank(String allowBlank) {
            this.allowBlank = allowBlank;
        }

        public List<LocalData> getLocalDatas() {
            return localDatas;
        }

        public void setLocalDatas(List<LocalData> localDatas) {
            this.localDatas = localDatas;
        }

        public List<GridTab> getTabList() {
            return mTabList;
        }

        public void setTabList(List<GridTab> tabList) {
            mTabList = tabList;
        }

        public boolean isUpdatable() {
            return updatable;
        }

        public void setUpdatable(boolean updatable) {
            this.updatable = updatable;
        }
    }

    public static class GridTab implements Serializable {
        private String title;
        private String caller;
        private int position;
        private List<BillGroupModel> mBillGroupModels;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public List<BillGroupModel> getBillGroupModels() {
            return mBillGroupModels;
        }

        public void setBillGroupModels(List<BillGroupModel> billGroupModels) {
            mBillGroupModels = billGroupModels;
        }

    }

    /**
     * 当C类型时候,本地选择数据
     */
    public static class LocalData implements Serializable {
        public String value = "";//显示的值  ||附件时候表示 路径，文件名
        public String display = "";//上传的值 ||附件时候表示上传的附件id
    }

    public interface Constants {
        String TYPE_TITLE = "LOCAL_TITLE";
        String TYPE_ADD = "LOCAL_ADD";
        String TYPE_TAB = "LOCAL_TAB";
    }

    @Override
    public String toString() {
        return "BillGroupModel{" +
                "isDeleteAble=" + isDeleteAble +
                ", isForm=" + isForm +
                ", lastInType=" + lastInType +
                ", groupIndex=" + groupIndex +
                ", minDetno=" + minDetno +
                ", group='" + group + '\'' +
                ", keyField='" + keyField + '\'' +
                ", mTagMap=" + mTagMap +
                ", hideBillFields=" + hideBillFields +
                ", showBillFields=" + showBillFields +
                ", mGridTabs=" + mGridTabs +
                '}';
    }
}
