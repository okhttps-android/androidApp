package com.xzjmyk.pm.activity.ui.erp.model.oa;

/**
 * Created by Bitliker on 2017/7/7.
 */

public class CurrentNode  {


    /**
     * forknode : 0
     * button : {"jb_id":3100,"jb_caller":null,"jb_buttonname":"保存请假原因","jb_buttonid":"Ask4Leave1","jb_fields":"基本资料","jb_message":null,"jt_neccessaryfield":"va_remark, va_remark2"}
     * InstanceId : 请假申请_UAS_DEV.4310001
     * dealmanname : 臧亚诚
     * currentnode : {"jp_id":285860,"jp_name":"请假申请流程","jp_launcherId":"U0730","jp_launcherName":"卢浩光","jp_form":"请假申请_UAS_DEV.4310001","jp_nodeId":"4310016","jp_nodeName":"task 4","jp_nodeDealMan":"U0745","jp_nodeDealManName":null,"jp_launchTime":1499311281000,"jp_stayMinutes":0,"jp_caller":"Ask4Leave","jp_table":"Vacation","jp_keyValue":18845,"jp_processInstanceId":"请假申请_UAS_DEV.4310001","jp_status":"已审批","jp_keyName":"va_id","jp_url":"jsps/hr/attendance/ask4leave.jsp","jp_formStatus":"va_status","jp_flag":0,"jp_formDetailKey":null,"jp_codevalue":"AL17060006","jp_pagingid":0,"jp_processdefid":"请假申请_UAS_DEV-13","jp_processnote":"请假事由: test\r\n","jp_realjobid":null}
     */

    private int forknode;
    private ButtonBean button;
    private String InstanceId;
    private String dealmanname;
    private CurrentnodeBean currentnode;

    public CurrentNode(){
    }
    public int getForknode() {
        return forknode;
    }

    public void setForknode(int forknode) {
        this.forknode = forknode;
    }

    public ButtonBean getButton() {
        return button;
    }

    public void setButton(ButtonBean button) {
        this.button = button;
    }

    public String getInstanceId() {
        return InstanceId;
    }

    public void setInstanceId(String InstanceId) {
        this.InstanceId = InstanceId;
    }

    public String getDealmanname() {
        return dealmanname;
    }

    public void setDealmanname(String dealmanname) {
        this.dealmanname = dealmanname;
    }

    public CurrentnodeBean getCurrentnode() {
        return currentnode;
    }

    public void setCurrentnode(CurrentnodeBean currentnode) {
        this.currentnode = currentnode;
    }

    public static class ButtonBean {
        /**
         * jb_id : 3100
         * jb_caller : null
         * jb_buttonname : 保存请假原因
         * jb_buttonid : Ask4Leave1
         * jb_fields : 基本资料
         * jb_message : null
         * jt_neccessaryfield : va_remark, va_remark2
         */

        private int jb_id;
        private Object jb_caller;
        private String jb_buttonname;
        private String jb_buttonid;
        private String jb_fields;
        private Object jb_message;
        private String jt_neccessaryfield;

        public int getJb_id() {
            return jb_id;
        }

        public void setJb_id(int jb_id) {
            this.jb_id = jb_id;
        }

        public Object getJb_caller() {
            return jb_caller;
        }

        public void setJb_caller(Object jb_caller) {
            this.jb_caller = jb_caller;
        }

        public String getJb_buttonname() {
            return jb_buttonname;
        }

        public void setJb_buttonname(String jb_buttonname) {
            this.jb_buttonname = jb_buttonname;
        }

        public String getJb_buttonid() {
            return jb_buttonid;
        }

        public void setJb_buttonid(String jb_buttonid) {
            this.jb_buttonid = jb_buttonid;
        }

        public String getJb_fields() {
            return jb_fields;
        }

        public void setJb_fields(String jb_fields) {
            this.jb_fields = jb_fields;
        }

        public Object getJb_message() {
            return jb_message;
        }

        public void setJb_message(Object jb_message) {
            this.jb_message = jb_message;
        }

        public String getJt_neccessaryfield() {
            return jt_neccessaryfield;
        }

        public void setJt_neccessaryfield(String jt_neccessaryfield) {
            this.jt_neccessaryfield = jt_neccessaryfield;
        }
    }

    public static class CurrentnodeBean {
        /**
         * jp_id : 285860
         * jp_name : 请假申请流程
         * jp_launcherId : U0730
         * jp_launcherName : 卢浩光
         * jp_form : 请假申请_UAS_DEV.4310001
         * jp_nodeId : 4310016
         * jp_nodeName : task 4
         * jp_nodeDealMan : U0745
         * jp_nodeDealManName : null
         * jp_launchTime : 1499311281000
         * jp_stayMinutes : 0
         * jp_caller : Ask4Leave
         * jp_table : Vacation
         * jp_keyValue : 18845
         * jp_processInstanceId : 请假申请_UAS_DEV.4310001
         * jp_status : 已审批
         * jp_keyName : va_id
         * jp_url : jsps/hr/attendance/ask4leave.jsp
         * jp_formStatus : va_status
         * jp_flag : 0
         * jp_formDetailKey : null
         * jp_codevalue : AL17060006
         * jp_pagingid : 0
         * jp_processdefid : 请假申请_UAS_DEV-13
         * jp_processnote : 请假事由: test

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
        private Object jp_nodeDealManName;
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
        private String jp_processnote;
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

        public Object getJp_nodeDealManName() {
            return jp_nodeDealManName;
        }

        public void setJp_nodeDealManName(Object jp_nodeDealManName) {
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

        public String getJp_processnote() {
            return jp_processnote;
        }

        public void setJp_processnote(String jp_processnote) {
            this.jp_processnote = jp_processnote;
        }

        public Object getJp_realjobid() {
            return jp_realjobid;
        }

        public void setJp_realjobid(Object jp_realjobid) {
            this.jp_realjobid = jp_realjobid;
        }
    }


}
