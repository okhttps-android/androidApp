package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2016/11/4.
 */
public class CommonApprovalFlowBean {

    /**
     * processs : [{"jp_id":311065,"jp_name":"工作日报流程","jp_launcherId":"U0747","jp_launcherName":"方龙海","jp_form":"日报登记审批流_USOFTSYS.5728195","jp_nodeId":"5728209","jp_nodeName":"部长15","jp_nodeDealMan":"U0305","jp_nodeDealManName":"吕全明","jp_launchTime":1482487441000,"jp_stayMinutes":0,"jp_caller":"WorkDaily","jp_table":"WorkDaily left join employee on em_code=wd_empcode","jp_keyValue":102431,"jp_processInstanceId":"日报登记审批流_USOFTSYS.5728195","jp_status":"已审批","jp_keyName":"wd_id","jp_url":"jsps/oa/persontask/workDaily/addWorkDaily.jsp","jp_formStatus":"wd_status","jp_flag":0,"jp_formDetailKey":null,"jp_codevalue":"2016121378","jp_pagingid":1414484,"jp_processdefid":"日报登记审批流_USOFTSYS-7","jp_processnote":null,"jp_realjobid":null},{"jp_id":311226,"jp_name":"工作日报流程","jp_launcherId":"U0747","jp_launcherName":"方龙海","jp_form":"日报登记审批流_USOFTSYS.5728195","jp_nodeId":"5742063","jp_nodeName":"总监18","jp_nodeDealMan":"U0303","jp_nodeDealManName":"钟燕玲","jp_launchTime":1482574149000,"jp_stayMinutes":0,"jp_caller":"WorkDaily","jp_table":"WorkDaily left join employee on em_code=wd_empcode","jp_keyValue":102431,"jp_processInstanceId":"日报登记审批流_USOFTSYS.5728195","jp_status":"已审批","jp_keyName":"wd_id","jp_url":"jsps/oa/persontask/workDaily/addWorkDaily.jsp","jp_formStatus":"wd_status","jp_flag":0,"jp_formDetailKey":null,"jp_codevalue":"2016121378","jp_pagingid":1414753,"jp_processdefid":"日报登记审批流_USOFTSYS-7","jp_processnote":null,"jp_realjobid":null},{"jp_id":311251,"jp_name":"工作日报流程","jp_launcherId":"U0747","jp_launcherName":"方龙海","jp_form":"日报登记审批流_USOFTSYS.5728195","jp_nodeId":"5742258","jp_nodeName":"总经理18","jp_nodeDealMan":"U0102","jp_nodeDealManName":"陈正亮","jp_launchTime":1482634135000,"jp_stayMinutes":0,"jp_caller":"WorkDaily","jp_table":"WorkDaily left join employee on em_code=wd_empcode","jp_keyValue":102431,"jp_processInstanceId":"日报登记审批流_USOFTSYS.5728195","jp_status":"已审批","jp_keyName":"wd_id","jp_url":"jsps/oa/persontask/workDaily/addWorkDaily.jsp","jp_formStatus":"wd_status","jp_flag":0,"jp_formDetailKey":null,"jp_codevalue":"2016121378","jp_pagingid":1414806,"jp_processdefid":"日报登记审批流_USOFTSYS-7","jp_processnote":null,"jp_realjobid":null}]
     * nodes : [{"jn_id":"2629608","jn_name":"部长15","jn_dealManId":"U0305","jn_dealManName":"吕全明","jn_dealTime":"2016-12-24 18:09:09","jn_dealResult":"同意","jn_operatedDescription":null,"jn_nodeDescription":null,"jn_infoReceiver":null,"jn_processInstanceId":"日报登记审批流_USOFTSYS.5728195","jn_holdtime":0,"jn_attachs":null,"jn_attach":"T"},{"jn_id":"2629636","jn_name":"总监18","jn_dealManId":"U0303","jn_dealManName":"钟燕玲","jn_dealTime":"2016-12-25 10:48:54","jn_dealResult":"同意","jn_operatedDescription":null,"jn_nodeDescription":null,"jn_infoReceiver":null,"jn_processInstanceId":"日报登记审批流_USOFTSYS.5728195","jn_holdtime":0,"jn_attachs":null,"jn_attach":"T"},{"jn_id":"2629773","jn_name":"总经理18","jn_dealManId":"U0102","jn_dealManName":"陈正亮","jn_dealTime":"2016-12-26 10:01:20","jn_dealResult":"同意","jn_operatedDescription":null,"jn_nodeDescription":null,"jn_infoReceiver":null,"jn_processInstanceId":"日报登记审批流_USOFTSYS.5728195","jn_holdtime":0,"jn_attachs":null,"jn_attach":"T"}]
     * data : [{"JP_ID":69909,"JP_CALLER":"WorkDaily","JP_KEYVALUE":102431,"JP_NODENAME":"部长15","JP_NODEDEALMAN":"U0305","JP_PROCESSDEFID":"日报登记审批流_USOFTSYS-7","JP_NODEDEALMANNAME":"吕全明","JP_NEWNODEDEALMAN":null,"JP_NEWNODEDEALMANNAME":null,"JP_CANEXTRA":null,"JP_EXTRAMAN":null,"JP_EXTRAMANNAME":null},{"JP_ID":69910,"JP_CALLER":"WorkDaily","JP_KEYVALUE":102431,"JP_NODENAME":"总监18","JP_NODEDEALMAN":"U0303","JP_PROCESSDEFID":"日报登记审批流_USOFTSYS-7","JP_NODEDEALMANNAME":"钟燕玲","JP_NEWNODEDEALMAN":null,"JP_NEWNODEDEALMANNAME":null,"JP_CANEXTRA":null,"JP_EXTRAMAN":null,"JP_EXTRAMANNAME":null},{"JP_ID":69911,"JP_CALLER":"WorkDaily","JP_KEYVALUE":102431,"JP_NODENAME":"总经理18","JP_NODEDEALMAN":"U0102","JP_PROCESSDEFID":"日报登记审批流_USOFTSYS-7","JP_NODEDEALMANNAME":"陈正亮","JP_NEWNODEDEALMAN":null,"JP_NEWNODEDEALMANNAME":null,"JP_CANEXTRA":null,"JP_EXTRAMAN":null,"JP_EXTRAMANNAME":null}]
     * success : true
     * currentnode : {"node":5742258,"jd":19780,"instanceId":"日报登记审批流_USOFTSYS.5728195","processDefId":"日报登记审批流_USOFTSYS-7","nodename":"总经理18","type":"JPROCESS"}
     * jprocands : []
     */

    private boolean success;
    private CurrentnodeBean currentnode;
    private List<ProcesssBean> processs;
    private List<NodesBean> nodes;
    private List<DataBean> data;
    private List<?> jprocands;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CurrentnodeBean getCurrentnode() {
        return currentnode;
    }

    public void setCurrentnode(CurrentnodeBean currentnode) {
        this.currentnode = currentnode;
    }

    public List<ProcesssBean> getProcesss() {
        return processs;
    }

    public void setProcesss(List<ProcesssBean> processs) {
        this.processs = processs;
    }

    public List<NodesBean> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodesBean> nodes) {
        this.nodes = nodes;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public List<?> getJprocands() {
        return jprocands;
    }

    public void setJprocands(List<?> jprocands) {
        this.jprocands = jprocands;
    }

    public static class CurrentnodeBean {
        /**
         * node : 5742258
         * jd : 19780
         * instanceId : 日报登记审批流_USOFTSYS.5728195
         * processDefId : 日报登记审批流_USOFTSYS-7
         * nodename : 总经理18
         * type : JPROCESS
         */

        private int node;
        private int jd;
        private String instanceId;
        private String processDefId;
        private String nodename;
        private String type;

        public int getNode() {
            return node;
        }

        public void setNode(int node) {
            this.node = node;
        }

        public int getJd() {
            return jd;
        }

        public void setJd(int jd) {
            this.jd = jd;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public String getProcessDefId() {
            return processDefId;
        }

        public void setProcessDefId(String processDefId) {
            this.processDefId = processDefId;
        }

        public String getNodename() {
            return nodename;
        }

        public void setNodename(String nodename) {
            this.nodename = nodename;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class ProcesssBean {
        /**
         * jp_id : 311065
         * jp_name : 工作日报流程
         * jp_launcherId : U0747
         * jp_launcherName : 方龙海
         * jp_form : 日报登记审批流_USOFTSYS.5728195
         * jp_nodeId : 5728209
         * jp_nodeName : 部长15
         * jp_nodeDealMan : U0305
         * jp_nodeDealManName : 吕全明
         * jp_launchTime : 1482487441000
         * jp_stayMinutes : 0
         * jp_caller : WorkDaily
         * jp_table : WorkDaily left join employee on em_code=wd_empcode
         * jp_keyValue : 102431
         * jp_processInstanceId : 日报登记审批流_USOFTSYS.5728195
         * jp_status : 已审批
         * jp_keyName : wd_id
         * jp_url : jsps/oa/persontask/workDaily/addWorkDaily.jsp
         * jp_formStatus : wd_status
         * jp_flag : 0
         * jp_formDetailKey : null
         * jp_codevalue : 2016121378
         * jp_pagingid : 1414484
         * jp_processdefid : 日报登记审批流_USOFTSYS-7
         * jp_processnote : null
         * jp_realjobid : null
         */

        private int jp_id;
        private String jp_name;
        private String jp_launcherId;
        private String jp_launcherName;
        private String jp_form;
        private String jp_nodeId;
        private String jp_nodeName;
        private String jp_nodeDealMan;
        private String jp_nodeDealManName;
        private long jp_launchTime;
        private int jp_stayMinutes;
        private String jp_caller;
        private String jp_table;
        private int jp_keyValue;
        private String jp_processInstanceId;
        private String jp_status;
        private String jp_keyName;
        private String jp_url;
        private String jp_formStatus;
        private int jp_flag;
        private Object jp_formDetailKey;
        private String jp_codevalue;
        private int jp_pagingid;
        private String jp_processdefid;
        private Object jp_processnote;
        private Object jp_realjobid;

        public int getJp_id() {
            return jp_id;
        }

        public void setJp_id(int jp_id) {
            this.jp_id = jp_id;
        }

        public String getJp_name() {
            return jp_name;
        }

        public void setJp_name(String jp_name) {
            this.jp_name = jp_name;
        }

        public String getJp_launcherId() {
            return jp_launcherId;
        }

        public void setJp_launcherId(String jp_launcherId) {
            this.jp_launcherId = jp_launcherId;
        }

        public String getJp_launcherName() {
            return jp_launcherName;
        }

        public void setJp_launcherName(String jp_launcherName) {
            this.jp_launcherName = jp_launcherName;
        }

        public String getJp_form() {
            return jp_form;
        }

        public void setJp_form(String jp_form) {
            this.jp_form = jp_form;
        }

        public String getJp_nodeId() {
            return jp_nodeId;
        }

        public void setJp_nodeId(String jp_nodeId) {
            this.jp_nodeId = jp_nodeId;
        }

        public String getJp_nodeName() {
            return jp_nodeName;
        }

        public void setJp_nodeName(String jp_nodeName) {
            this.jp_nodeName = jp_nodeName;
        }

        public String getJp_nodeDealMan() {
            return jp_nodeDealMan;
        }

        public void setJp_nodeDealMan(String jp_nodeDealMan) {
            this.jp_nodeDealMan = jp_nodeDealMan;
        }

        public String getJp_nodeDealManName() {
            return jp_nodeDealManName;
        }

        public void setJp_nodeDealManName(String jp_nodeDealManName) {
            this.jp_nodeDealManName = jp_nodeDealManName;
        }

        public long getJp_launchTime() {
            return jp_launchTime;
        }

        public void setJp_launchTime(long jp_launchTime) {
            this.jp_launchTime = jp_launchTime;
        }

        public int getJp_stayMinutes() {
            return jp_stayMinutes;
        }

        public void setJp_stayMinutes(int jp_stayMinutes) {
            this.jp_stayMinutes = jp_stayMinutes;
        }

        public String getJp_caller() {
            return jp_caller;
        }

        public void setJp_caller(String jp_caller) {
            this.jp_caller = jp_caller;
        }

        public String getJp_table() {
            return jp_table;
        }

        public void setJp_table(String jp_table) {
            this.jp_table = jp_table;
        }

        public int getJp_keyValue() {
            return jp_keyValue;
        }

        public void setJp_keyValue(int jp_keyValue) {
            this.jp_keyValue = jp_keyValue;
        }

        public String getJp_processInstanceId() {
            return jp_processInstanceId;
        }

        public void setJp_processInstanceId(String jp_processInstanceId) {
            this.jp_processInstanceId = jp_processInstanceId;
        }

        public String getJp_status() {
            return jp_status;
        }

        public void setJp_status(String jp_status) {
            this.jp_status = jp_status;
        }

        public String getJp_keyName() {
            return jp_keyName;
        }

        public void setJp_keyName(String jp_keyName) {
            this.jp_keyName = jp_keyName;
        }

        public String getJp_url() {
            return jp_url;
        }

        public void setJp_url(String jp_url) {
            this.jp_url = jp_url;
        }

        public String getJp_formStatus() {
            return jp_formStatus;
        }

        public void setJp_formStatus(String jp_formStatus) {
            this.jp_formStatus = jp_formStatus;
        }

        public int getJp_flag() {
            return jp_flag;
        }

        public void setJp_flag(int jp_flag) {
            this.jp_flag = jp_flag;
        }

        public Object getJp_formDetailKey() {
            return jp_formDetailKey;
        }

        public void setJp_formDetailKey(Object jp_formDetailKey) {
            this.jp_formDetailKey = jp_formDetailKey;
        }

        public String getJp_codevalue() {
            return jp_codevalue;
        }

        public void setJp_codevalue(String jp_codevalue) {
            this.jp_codevalue = jp_codevalue;
        }

        public int getJp_pagingid() {
            return jp_pagingid;
        }

        public void setJp_pagingid(int jp_pagingid) {
            this.jp_pagingid = jp_pagingid;
        }

        public String getJp_processdefid() {
            return jp_processdefid;
        }

        public void setJp_processdefid(String jp_processdefid) {
            this.jp_processdefid = jp_processdefid;
        }

        public Object getJp_processnote() {
            return jp_processnote;
        }

        public void setJp_processnote(Object jp_processnote) {
            this.jp_processnote = jp_processnote;
        }

        public Object getJp_realjobid() {
            return jp_realjobid;
        }

        public void setJp_realjobid(Object jp_realjobid) {
            this.jp_realjobid = jp_realjobid;
        }
    }

    public static class NodesBean {
        /**
         * jn_id : 2629608
         * jn_name : 部长15
         * jn_dealManId : U0305
         * jn_dealManName : 吕全明
         * jn_dealTime : 2016-12-24 18:09:09
         * jn_dealResult : 同意
         * jn_operatedDescription : null
         * jn_nodeDescription : null
         * jn_infoReceiver : null
         * jn_processInstanceId : 日报登记审批流_USOFTSYS.5728195
         * jn_holdtime : 0
         * jn_attachs : null
         * jn_attach : T
         */

        private String jn_id;
        private String jn_name;
        private String jn_dealManId;
        private String jn_dealManName;
        private String jn_dealTime;
        private String jn_dealResult;
        private Object jn_operatedDescription;
        private Object jn_nodeDescription;
        private Object jn_infoReceiver;
        private String jn_processInstanceId;
        private int jn_holdtime;
        private Object jn_attachs;
        private String jn_attach;

        public String getJn_id() {
            return jn_id;
        }

        public void setJn_id(String jn_id) {
            this.jn_id = jn_id;
        }

        public String getJn_name() {
            return jn_name;
        }

        public void setJn_name(String jn_name) {
            this.jn_name = jn_name;
        }

        public String getJn_dealManId() {
            return jn_dealManId;
        }

        public void setJn_dealManId(String jn_dealManId) {
            this.jn_dealManId = jn_dealManId;
        }

        public String getJn_dealManName() {
            return jn_dealManName;
        }

        public void setJn_dealManName(String jn_dealManName) {
            this.jn_dealManName = jn_dealManName;
        }

        public String getJn_dealTime() {
            return jn_dealTime;
        }

        public void setJn_dealTime(String jn_dealTime) {
            this.jn_dealTime = jn_dealTime;
        }

        public String getJn_dealResult() {
            return jn_dealResult;
        }

        public void setJn_dealResult(String jn_dealResult) {
            this.jn_dealResult = jn_dealResult;
        }

        public Object getJn_operatedDescription() {
            return jn_operatedDescription;
        }

        public void setJn_operatedDescription(Object jn_operatedDescription) {
            this.jn_operatedDescription = jn_operatedDescription;
        }

        public Object getJn_nodeDescription() {
            return jn_nodeDescription;
        }

        public void setJn_nodeDescription(Object jn_nodeDescription) {
            this.jn_nodeDescription = jn_nodeDescription;
        }

        public Object getJn_infoReceiver() {
            return jn_infoReceiver;
        }

        public void setJn_infoReceiver(Object jn_infoReceiver) {
            this.jn_infoReceiver = jn_infoReceiver;
        }

        public String getJn_processInstanceId() {
            return jn_processInstanceId;
        }

        public void setJn_processInstanceId(String jn_processInstanceId) {
            this.jn_processInstanceId = jn_processInstanceId;
        }

        public int getJn_holdtime() {
            return jn_holdtime;
        }

        public void setJn_holdtime(int jn_holdtime) {
            this.jn_holdtime = jn_holdtime;
        }

        public Object getJn_attachs() {
            return jn_attachs;
        }

        public void setJn_attachs(Object jn_attachs) {
            this.jn_attachs = jn_attachs;
        }

        public String getJn_attach() {
            return jn_attach;
        }

        public void setJn_attach(String jn_attach) {
            this.jn_attach = jn_attach;
        }
    }

    public static class DataBean {
        /**
         * JP_ID : 69909
         * JP_CALLER : WorkDaily
         * JP_KEYVALUE : 102431
         * JP_NODENAME : 部长15
         * JP_NODEDEALMAN : U0305
         * JP_PROCESSDEFID : 日报登记审批流_USOFTSYS-7
         * JP_NODEDEALMANNAME : 吕全明
         * JP_NEWNODEDEALMAN : null
         * JP_NEWNODEDEALMANNAME : null
         * JP_CANEXTRA : null
         * JP_EXTRAMAN : null
         * JP_EXTRAMANNAME : null
         */

        private int JP_ID;
        private String JP_CALLER;
        private int JP_KEYVALUE;
        private String JP_NODENAME;
        private String JP_NODEDEALMAN;
        private String JP_PROCESSDEFID;
        private String JP_NODEDEALMANNAME;
        private Object JP_NEWNODEDEALMAN;
        private Object JP_NEWNODEDEALMANNAME;
        private Object JP_CANEXTRA;
        private Object JP_EXTRAMAN;
        private Object JP_EXTRAMANNAME;

        public int getJP_ID() {
            return JP_ID;
        }

        public void setJP_ID(int JP_ID) {
            this.JP_ID = JP_ID;
        }

        public String getJP_CALLER() {
            return JP_CALLER;
        }

        public void setJP_CALLER(String JP_CALLER) {
            this.JP_CALLER = JP_CALLER;
        }

        public int getJP_KEYVALUE() {
            return JP_KEYVALUE;
        }

        public void setJP_KEYVALUE(int JP_KEYVALUE) {
            this.JP_KEYVALUE = JP_KEYVALUE;
        }

        public String getJP_NODENAME() {
            return JP_NODENAME;
        }

        public void setJP_NODENAME(String JP_NODENAME) {
            this.JP_NODENAME = JP_NODENAME;
        }

        public String getJP_NODEDEALMAN() {
            return JP_NODEDEALMAN;
        }

        public void setJP_NODEDEALMAN(String JP_NODEDEALMAN) {
            this.JP_NODEDEALMAN = JP_NODEDEALMAN;
        }

        public String getJP_PROCESSDEFID() {
            return JP_PROCESSDEFID;
        }

        public void setJP_PROCESSDEFID(String JP_PROCESSDEFID) {
            this.JP_PROCESSDEFID = JP_PROCESSDEFID;
        }

        public String getJP_NODEDEALMANNAME() {
            return JP_NODEDEALMANNAME;
        }

        public void setJP_NODEDEALMANNAME(String JP_NODEDEALMANNAME) {
            this.JP_NODEDEALMANNAME = JP_NODEDEALMANNAME;
        }

        public Object getJP_NEWNODEDEALMAN() {
            return JP_NEWNODEDEALMAN;
        }

        public void setJP_NEWNODEDEALMAN(Object JP_NEWNODEDEALMAN) {
            this.JP_NEWNODEDEALMAN = JP_NEWNODEDEALMAN;
        }

        public Object getJP_NEWNODEDEALMANNAME() {
            return JP_NEWNODEDEALMANNAME;
        }

        public void setJP_NEWNODEDEALMANNAME(Object JP_NEWNODEDEALMANNAME) {
            this.JP_NEWNODEDEALMANNAME = JP_NEWNODEDEALMANNAME;
        }

        public Object getJP_CANEXTRA() {
            return JP_CANEXTRA;
        }

        public void setJP_CANEXTRA(Object JP_CANEXTRA) {
            this.JP_CANEXTRA = JP_CANEXTRA;
        }

        public Object getJP_EXTRAMAN() {
            return JP_EXTRAMAN;
        }

        public void setJP_EXTRAMAN(Object JP_EXTRAMAN) {
            this.JP_EXTRAMAN = JP_EXTRAMAN;
        }

        public Object getJP_EXTRAMANNAME() {
            return JP_EXTRAMANNAME;
        }

        public void setJP_EXTRAMANNAME(Object JP_EXTRAMANNAME) {
            this.JP_EXTRAMANNAME = JP_EXTRAMANNAME;
        }
    }
}
