package com.uas.appworks.OA.erp.utils.approvautils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.Approval;
import com.core.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitliker on 2017/8/11.
 */

public class ApprovaNodeUtil {

    public static List<Approval> handlerNode(DBManager manager, String message) {
        JSONObject object = JSON.parseObject(message);
        JSONArray nodes = JSONUtil.getJSONArray(object, "nodes");
        JSONArray processs = JSONUtil.getJSONArray(object, "processs");
        JSONArray datas = JSONUtil.getJSONArray(object, "data");
        final List<Approval> approvals = getNodDatas(datas);
        if (!ListUtils.isEmpty(processs)) {
            mergeNode(processs, approvals, false);
        }
        if (!ListUtils.isEmpty(nodes)) {
            mergeNode(nodes, approvals, true);
        }
        if (!ListUtils.isEmpty(approvals)) {
            //判断当前
            for (Approval a : approvals) {
                if (a.getIdKey().contains("待审批")) {
                    a.setValues("");
                }
                String[] emcode = a.getDfType().split(",");
                if (!StringUtil.isEmpty(emcode[0])) {
                    int imId = getImByCode(manager, emcode[0]);
                    a.setId(imId);
                }
            }
            return approvals;

        }
        return null;
    }

    private static List<Approval> getNodDatas(JSONArray datas) {
        List<Approval> approvals = new ArrayList<>();
        for (int i = datas.size() - 1; i >= 0; i--) {
            JSONObject object = datas.getJSONObject(i);
            String nodeName = JSONUtil.getText(object, "JP_NODENAME");//当前结点名称
            String emCode = JSONUtil.getText(object, "JP_NODEDEALMAN");//节点处理人编号
            String manName = JSONUtil.getText(object, "JP_NODEDEALMANNAME");//
            Approval approval = new Approval(Approval.NODES);
            approval.setCaption(manName);
            approval.setDfType(emCode);
            approval.setValuesKey(nodeName);
            approvals.add(approval);
        }
        return approvals;
    }

    private static void mergeNode(JSONArray array, List<Approval> approvals, boolean isLog) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject o = array.getJSONObject(i);
            String name = JSONUtil.getText(o, "jp_nodeName", "jn_name");//节点名称
            long launchTime =  JSONUtil.getTime(o, "jp_launchTime", "jn_dealTime");//时间
            String status = JSONUtil.getText(o, "jp_status");//状态
            String nodeDealCode = JSONUtil.getText(o, "jp_nodeDealMan", "jn_dealManId");//处理人编号
            String nodeDealName = JSONUtil.getText(o, "jp_nodeDealManName", "jn_dealManName");//执行人
            String nodeDescription = JSONUtil.getText(o, "jn_nodeDescription");//执行操作
            for (Approval a : approvals) {
                if (name.equals(a.getValuesKey())) {//为当前结点
                    if (!isLog) {
                        a.setDfType(nodeDealCode);
                        a.setCaption(nodeDealName);
                        a.setIdKey(status);
                        if (launchTime > 0) {
                            a.setValues(DateFormatUtil.long2Str(launchTime, "MM-dd HH:mm"));
                        }
                    } else {
                        if (a.isDftypeEQ(nodeDealCode)) {
                            if (launchTime > 0) {
                                a.setValues(DateFormatUtil.long2Str(launchTime, "MM-dd HH:mm"));
                            }
                            if (!StringUtil.isEmpty(nodeDescription)) {
                                a.setIdKey(a.getIdKey() + "(" + nodeDescription + ")");
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private static int getImByCode(DBManager manager, String emCode) {
        if (manager == null) manager = new DBManager();
        String whichsys = CommonUtil.getMaster();
        if (StringUtil.isEmpty(emCode) || StringUtil.isEmpty(whichsys)) return 0;
        String[] selectionArgs = {emCode, whichsys};
        String selection = "em_code=? and whichsys=?";
        EmployeesEntity employeesEntity = manager.selectForEmployee(selectionArgs, selection);
        if (employeesEntity == null || employeesEntity.getEm_IMID() == 0) return 0;
        return employeesEntity.getEm_IMID();
    }

}
