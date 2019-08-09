package com.modular.apputils.model;

import android.text.TextUtils;

public class ApprovalList {
    private String imid;
    private String nodeId;
    private String caller;
    private String keyValue;
    private String master;
    private String name;//审批名称
    private String status;//状态
    private String launcherName;//发起人名称
    private String launcherCode;//发起人名称
    private String nodeDealMan;//节点处理人
    private long dealTime;//处理时间
    private String dealResult;//处理结果
    private String operatedDescription;//变更处理人
    private int subTitleColor;
    private String subTitle;
    private String codeValue;
    private String jp_form;

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public int getSubTitleColor() {
        return subTitleColor;
    }

    public void setSubTitleColor(int subTitleColor) {
        this.subTitleColor = subTitleColor;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getLauncherName() {
        return launcherName;
    }

    public void setLauncherName(String launcherName) {
        this.launcherName = launcherName;
    }

    public String getImid() {
        return imid;
    }

    public String getLauncherCode() {
        return launcherCode;
    }

    public void setLauncherCode(String launcherCode) {
        this.launcherCode = launcherCode;
    }

    public void setImid(String imid) {
        this.imid = imid;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeDealMan() {
        return nodeDealMan;
    }

    public void setNodeDealMan(String nodeDealMan) {
        this.nodeDealMan = nodeDealMan;
    }

    public long getDealTime() {
        return dealTime;
    }

    public void setDealTime(long dealTime) {
        this.dealTime = dealTime;
    }

    public String getDealResult() {
        return dealResult;
    }

    public void setDealResult(String dealResult) {
        this.dealResult = dealResult;
    }

    public String getOperatedDescription() {
        return operatedDescription;
    }

    public void setOperatedDescription(String operatedDescription) {
        this.operatedDescription = operatedDescription;
    }

    public String getJp_form() {
        return jp_form;
    }

    public void setJp_form(String jp_form) {
        this.jp_form = jp_form;
    }

    public boolean hasContext(CharSequence constraint) {
        return contain(constraint, imid, nodeId, caller, keyValue, master, name,
                status, launcherName, nodeDealMan, dealResult, operatedDescription);
    }

    private boolean contain(CharSequence constraint, CharSequence... main) {
        if (main == null || main.length <= 0) return false;
        for (CharSequence e : main) {
            if (!TextUtils.isEmpty(e)
                    && e.toString().toUpperCase().contains(constraint.toString().toUpperCase()))
                return true;
        }
        return false;
    }
}
