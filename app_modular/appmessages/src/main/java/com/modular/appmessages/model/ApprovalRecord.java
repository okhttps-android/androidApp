package com.modular.appmessages.model;

import com.common.data.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bitliker on 2017/7/20.
 */

public class ApprovalRecord {
    public boolean isForknode = false;
    public int id = 0;
    public int nodeId = 0;
    public String title;
    public String chcheNode = "";
    public String imid = "";
    public String status = "";
    public String currentNodeMan = "";//当前节点的人员编号
    public String nodeName = "";
    public String needInputKeys = "";
    public String showNeedMessage = "";//保存必填字段时候，，没有填写时候提示信息
    public String processInstanceId = "";
    public String caller = "";
    public String callerName = "";

    public String getCallerName() {
        return callerName.replace("流程", "");
    }

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("nodeId", nodeId);
        map.put("status", status);
    map.put("nodeName", nodeName);
        map.put("needInputKeys", needInputKeys);
        map.put("showNeedMessage", showNeedMessage);
        map.put("processInstanceId", processInstanceId);
        map.put("caller", caller);
        return JSONUtil.map2JSON(map);
    }
}
