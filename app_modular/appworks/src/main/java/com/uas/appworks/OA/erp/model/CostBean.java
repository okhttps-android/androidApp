package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/6/13.
 * function:报销单录入获取报销类型、币种、明细实体类
 */

public class CostBean  {

    /**
     * sessionId : 6C0F406EF63784585F2F9867E907ADA5
     * data : {"formdetail":[{"fd_caption":"编号","mfd_caption":"编号","fd_field":"fp_code","fd_value":"FP17060120","fd_maxlength":40,"fd_group":"基本信息","fd_detno":1,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165297},{"fd_caption":"申请日期","mfd_caption":"申请日期","fd_field":"fp_billdate","fd_value":"2017-06-15 10:56:23","fd_maxlength":100,"fd_group":"基本信息","fd_detno":2,"fd_type":"D","fd_readonly":"F","mfd_isdefault":0,"fd_id":191908},{"fd_caption":"录入人","mfd_caption":"录入人","fd_field":"fp_recordman","fd_value":"方龙海","fd_maxlength":40,"fd_group":"基本信息","fd_detno":2,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165301},{"fd_caption":"录入日期","mfd_caption":"录入日期","fd_field":"fp_recorddate","fd_value":"2017-06-15 10:56:23","fd_maxlength":20,"fd_group":"基本信息","fd_detno":3,"fd_type":"D","fd_readonly":"T","mfd_isdefault":0,"fd_id":165303},{"fd_caption":"单据状态","mfd_caption":"单据状态","fd_field":"fp_status","fd_value":"在录入","fd_maxlength":20,"fd_group":"基本信息","fd_detno":4,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165300},{"fd_caption":"报销人编号","mfd_caption":"报销人编号","fd_field":"fp_pleasemancode","fd_value":"U0747","fd_maxlength":50,"fd_group":"基本信息","fd_detno":5,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":179895},{"fd_caption":"报销人","mfd_caption":"报销人","fd_field":"fp_pleaseman","fd_value":"方龙海","fd_maxlength":50,"fd_group":"基本信息","fd_detno":6,"fd_type":"SF","fd_readonly":"F","mfd_isdefault":0,"fd_id":165298},{"fd_caption":"报销部门","mfd_caption":"报销部门","fd_field":"fp_department","fd_value":"采购部","fd_maxlength":100,"fd_group":"基本信息","fd_detno":7,"fd_type":"SF","fd_readonly":"F","mfd_isdefault":0,"fd_id":165299},{"fd_caption":"费用类型","mfd_caption":"费用类型","fd_field":"fp_class","fd_value":"其他","fd_maxlength":50,"fd_group":"基本信息","fd_detno":8,"fd_type":"SF","fd_readonly":"F","mfd_isdefault":-1,"fd_id":165294},{"fd_caption":"币别","mfd_caption":"币别","fd_field":"fp_v13","fd_value":"RMB","fd_maxlength":150,"fd_group":"基本信息","fd_detno":9,"fd_type":"C","fd_readonly":"F","mfd_isdefault":-1,"fd_id":165319},{"fd_caption":"报销总额","mfd_caption":"报销总额","fd_field":"fp_pleaseamount","fd_value":88,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":10,"fd_type":"N","fd_readonly":"T","mfd_isdefault":-1,"fd_id":165302},{"fd_caption":"已转金额","mfd_caption":"已转金额","fd_field":"fp_n1","fd_value":0,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":11,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":165320},{"fd_caption":"还款金额","mfd_caption":"还款金额","fd_field":"fp_n6","fd_value":"","fd_maxlength":22,"fd_group":"基本信息","fd_detno":12,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":165295},{"fd_caption":"开户行","mfd_caption":"开户行","fd_field":"fp_v6","fd_value":"","fd_maxlength":300,"fd_group":"基本信息","fd_detno":13,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165315},{"fd_caption":"开户姓名","mfd_caption":"开户姓名","fd_field":"fp_v9","fd_value":"","fd_maxlength":100,"fd_group":"基本信息","fd_detno":14,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165316},{"fd_caption":"银行账号","mfd_caption":"银行账号","fd_field":"fp_v8","fd_value":"","fd_maxlength":150,"fd_group":"基本信息","fd_detno":15,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165317},{"fd_caption":"支付状态","mfd_caption":"支付状态","fd_field":"fp_v7","fd_value":"未支付","fd_maxlength":100,"fd_group":"基本信息","fd_detno":16,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165311},{"fd_caption":"费用内容","mfd_caption":"费用内容","fd_field":"fp_v3","fd_value":"好好睡","fd_maxlength":300,"fd_group":"基本信息","fd_detno":17,"fd_type":"MS","fd_readonly":"F","mfd_isdefault":-1,"fd_id":165318},{"fd_caption":"来源类型","mfd_caption":"来源类型","fd_field":"fp_sourcekind","fd_value":"","fd_maxlength":100,"fd_group":"基本信息","fd_detno":18,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165314},{"fd_caption":"来源单号","mfd_caption":"来源单号","fd_field":"fp_sourcecode","fd_value":"","fd_maxlength":100,"fd_group":"基本信息","fd_detno":19,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165313},{"fd_caption":"项目成本类型","mfd_caption":"项目成本类型","fd_field":"fp_prjkind","fd_value":"","fd_maxlength":100,"fd_group":"基本信息","fd_detno":20,"fd_type":"SF","fd_readonly":"T","mfd_isdefault":0,"fd_id":180612},{"fd_caption":"项目编号","mfd_caption":"项目编号","fd_field":"fp_prjcode","fd_value":"","fd_maxlength":100,"fd_group":"基本信息","fd_detno":21,"fd_type":"SF","fd_readonly":"T","mfd_isdefault":0,"fd_id":180613},{"fd_caption":"项目名称","mfd_caption":"项目名称","fd_field":"fp_prjname","fd_value":"","fd_maxlength":100,"fd_group":"基本信息","fd_detno":22,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":180614},{"fd_caption":"备注","mfd_caption":"备注","fd_field":"fp_remark","fd_value":"","fd_maxlength":300,"fd_group":"基本信息","fd_detno":24,"fd_type":"MS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165304},{"fd_caption":"审核人","mfd_caption":"审核人","fd_field":"fp_auditman","fd_value":"","fd_maxlength":40,"fd_group":"基本信息","fd_detno":25,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":165305},{"fd_caption":"审核日期","mfd_caption":"审核日期","fd_field":"fp_auditdate","fd_value":"2017-06-15 00:00:00","fd_maxlength":40,"fd_group":"基本信息","fd_detno":26,"fd_type":"D","fd_readonly":"T","mfd_isdefault":0,"fd_id":165306},{"fd_caption":"结案原因","mfd_caption":"结案原因","fd_field":"fp_endreason","fd_value":"","fd_maxlength":500,"fd_group":"基本信息","fd_detno":27,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":185927},{"fd_caption":"打印状态","mfd_caption":"打印状态","fd_field":"fp_printstatus","fd_value":"未打印","fd_maxlength":50,"fd_group":"基本信息","fd_detno":28,"fd_type":"SS","fd_readonly":"T","mfd_isdefault":0,"fd_id":181554},{"fd_caption":"岗位id","mfd_caption":"岗位id","fd_field":"fp_n4","fd_value":0,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":30,"fd_type":"H","fd_readonly":"T","mfd_isdefault":0,"fd_id":183711},{"fd_caption":"总借款金额","mfd_caption":"总借款金额","fd_field":"fp_sumjk","fd_value":0,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":99,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":197309},{"fd_caption":"总未归还金额","mfd_caption":"总未归还金额","fd_field":"fp_sumwgh","fd_value":0,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":100,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":197310},{"fd_caption":"本次还款总额","mfd_caption":"本次还款总额","fd_field":"fp_thishkamount","fd_value":0,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":101,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":197311},{"fd_caption":"报销金额(本位币)","mfd_caption":"报销金额(本位币)","fd_field":"fp_amount","fd_value":88,"fd_maxlength":100,"fd_group":"基本信息","fd_detno":146,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":197881},{"fd_caption":"打印次数","mfd_caption":"打印次数","fd_field":"fp_count","fd_value":0,"fd_maxlength":22,"fd_group":"基本信息","fd_detno":191,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":214528},{"fd_caption":"科目编号","mfd_caption":"科目编号","fd_field":"fp_v11","fd_value":"","fd_maxlength":100,"fd_group":"转银行登记信息","fd_detno":41,"fd_type":"SF","fd_readonly":"T","mfd_isdefault":0,"fd_id":165309},{"fd_caption":"科目名称","mfd_caption":"科目名称","fd_field":"fp_v10","fd_value":"","fd_maxlength":100,"fd_group":"转银行登记信息","fd_detno":42,"fd_type":"SF","fd_readonly":"T","mfd_isdefault":0,"fd_id":165310},{"fd_caption":"本次转金额","mfd_caption":"本次转金额","fd_field":"fp_n2","fd_value":0,"fd_maxlength":22,"fd_group":"转银行登记信息","fd_detno":43,"fd_type":"N","fd_readonly":"T","mfd_isdefault":0,"fd_id":165312},{"fd_caption":"单据类型","mfd_caption":"单据类型","fd_field":"fp_kind","fd_value":"费用报销单","fd_maxlength":40,"fd_group":"转银行登记信息","fd_detno":51,"fd_type":"H","fd_readonly":"T","mfd_isdefault":0,"fd_id":165296},{"fd_caption":"状态码","mfd_caption":"状态码","fd_field":"fp_statuscode","fd_value":"ENTERING","fd_maxlength":10,"fd_group":"转银行登记信息","fd_detno":52,"fd_type":"H","fd_readonly":"T","mfd_isdefault":0,"fd_id":165307},{"fd_caption":"ID ","mfd_caption":"ID ","fd_field":"fp_id","fd_value":38451,"fd_maxlength":22,"fd_group":"转银行登记信息","fd_detno":53,"fd_type":"H","fd_readonly":"T","mfd_isdefault":0,"fd_id":165308},{"fd_caption":"打印状态码","mfd_caption":"打印状态码","fd_field":"fp_printstatuscode","fd_value":"UNPRINT","fd_maxlength":50,"fd_group":"转银行登记信息","fd_detno":54,"fd_type":"H","fd_readonly":"T","mfd_isdefault":0,"fd_id":186273},{"fd_caption":"附件","mfd_caption":"附件","fd_field":"fb_attach","fd_value":"","fd_maxlength":50,"fd_group":"转银行登记信息","fd_detno":218,"fd_type":"MF","fd_readonly":"T","mfd_isdefault":0,"fd_id":214568}],"gridetail":[{"dg_group":1,"dg_caption":"ID ","dg_value":28935,"dg_field":"fpd_id","dg_maxlength":22,"dg_type":"N","mdg_caption":"ID ","mdg_isdefault":0,"dg_sequence":1,"dg_logictype":"keyField","gd_id":164919},{"dg_group":1,"dg_caption":"费用申请单号","dg_value":"FP17060120","dg_field":"fpd_code","dg_maxlength":50,"dg_type":"S","mdg_caption":"费用申请单号","mdg_isdefault":0,"dg_sequence":2,"dg_logictype":null,"gd_id":164920},{"dg_group":1,"dg_caption":"申请类型","dg_value":"费用报销单","dg_field":"fpd_class","dg_maxlength":100,"dg_type":"S","mdg_caption":"申请类型","mdg_isdefault":0,"dg_sequence":3,"dg_logictype":null,"gd_id":164921},{"dg_group":1,"dg_caption":"主表ID ","dg_value":38451,"dg_field":"fpd_fpid","dg_maxlength":22,"dg_type":"N","mdg_caption":"主表ID ","mdg_isdefault":0,"dg_sequence":4,"dg_logictype":"mainField","gd_id":164922},{"dg_group":1,"dg_caption":"序号","dg_value":1,"dg_field":"fpd_detno","dg_maxlength":22,"dg_type":"N","mdg_caption":"序号","mdg_isdefault":0,"dg_sequence":5,"dg_logictype":"detno","gd_id":164923},{"dg_group":1,"dg_caption":"费用项目","dg_value":"测试费用","dg_field":"fpd_d1","dg_maxlength":300,"dg_type":"DF","mdg_caption":"费用项目","mdg_isdefault":-1,"dg_sequence":6,"dg_logictype":"necessaryField","gd_id":170219},{"dg_group":1,"dg_caption":"预计金额","dg_value":0,"dg_field":"fpd_n2","dg_maxlength":22,"dg_type":"N","mdg_caption":"预计金额","mdg_isdefault":0,"dg_sequence":7,"dg_logictype":null,"gd_id":164927},{"dg_group":1,"dg_caption":"实际金额","dg_value":88,"dg_field":"fpd_total","dg_maxlength":22,"dg_type":"N","mdg_caption":"实际金额","mdg_isdefault":-1,"dg_sequence":8,"dg_logictype":"necessaryField","gd_id":164925},{"dg_group":1,"dg_caption":"财务核计金额","dg_value":88,"dg_field":"fpd_n1","dg_maxlength":22,"dg_type":"N","mdg_caption":"财务核计金额","mdg_isdefault":0,"dg_sequence":9,"dg_logictype":null,"gd_id":164926},{"dg_group":1,"dg_caption":"备注","dg_value":"","dg_field":"fpd_d7","dg_maxlength":200,"dg_type":"S","mdg_caption":"备注","mdg_isdefault":0,"dg_sequence":10,"dg_logictype":"necessaryField","gd_id":164924},{"dg_group":1,"dg_caption":"科目编号","dg_value":"660138","dg_field":"fpd_catecode","dg_maxlength":100,"dg_type":"S","mdg_caption":"科目编号","mdg_isdefault":0,"dg_sequence":11,"dg_logictype":null,"gd_id":170681}]}
     * success : true
     */

    private String sessionId;
    private DataBean data;
    private boolean success;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class DataBean {
        private List<FormdetailBean> formdetail;
        private List<GridetailBean> gridetail;

        public List<FormdetailBean> getFormdetail() {
            return formdetail;
        }

        public void setFormdetail(List<FormdetailBean> formdetail) {
            this.formdetail = formdetail;
        }

        public List<GridetailBean> getGridetail() {
            return gridetail;
        }

        public void setGridetail(List<GridetailBean> gridetail) {
            this.gridetail = gridetail;
        }

        public static class FormdetailBean {
            /**
             * fd_caption : 编号
             * mfd_caption : 编号
             * fd_field : fp_code
             * fd_value : FP17060120
             * fd_maxlength : 40
             * fd_group : 基本信息
             * fd_detno : 1
             * fd_type : SS
             * fd_readonly : T
             * mfd_isdefault : 0
             * fd_id : 165297
             */

            private String fd_caption;
            private String mfd_caption;
            private String fd_field;
            private String fd_value;
            private int fd_maxlength;
            private String fd_group;
            private int fd_detno;
            private String fd_type;
            private String fd_readonly;
            private int mfd_isdefault;
            private int fd_id;

            public String getFd_caption() {
                return fd_caption;
            }

            public void setFd_caption(String fd_caption) {
                this.fd_caption = fd_caption;
            }

            public String getMfd_caption() {
                return mfd_caption;
            }

            public void setMfd_caption(String mfd_caption) {
                this.mfd_caption = mfd_caption;
            }

            public String getFd_field() {
                return fd_field;
            }

            public void setFd_field(String fd_field) {
                this.fd_field = fd_field;
            }

            public String getFd_value() {
                return fd_value;
            }

            public void setFd_value(String fd_value) {
                this.fd_value = fd_value;
            }

            public int getFd_maxlength() {
                return fd_maxlength;
            }

            public void setFd_maxlength(int fd_maxlength) {
                this.fd_maxlength = fd_maxlength;
            }

            public String getFd_group() {
                return fd_group;
            }

            public void setFd_group(String fd_group) {
                this.fd_group = fd_group;
            }

            public int getFd_detno() {
                return fd_detno;
            }

            public void setFd_detno(int fd_detno) {
                this.fd_detno = fd_detno;
            }

            public String getFd_type() {
                return fd_type;
            }

            public void setFd_type(String fd_type) {
                this.fd_type = fd_type;
            }

            public String getFd_readonly() {
                return fd_readonly;
            }

            public void setFd_readonly(String fd_readonly) {
                this.fd_readonly = fd_readonly;
            }

            public int getMfd_isdefault() {
                return mfd_isdefault;
            }

            public void setMfd_isdefault(int mfd_isdefault) {
                this.mfd_isdefault = mfd_isdefault;
            }

            public int getFd_id() {
                return fd_id;
            }

            public void setFd_id(int fd_id) {
                this.fd_id = fd_id;
            }
        }

        public static class GridetailBean {
            /**
             * dg_group : 1
             * dg_caption : ID
             * dg_value : 28935
             * dg_field : fpd_id
             * dg_maxlength : 22
             * dg_type : N
             * mdg_caption : ID
             * mdg_isdefault : 0
             * dg_sequence : 1
             * dg_logictype : keyField
             * gd_id : 164919
             */

            private int dg_group;
            private String dg_caption;
            private String dg_value;
            private String dg_field;
            private int dg_maxlength;
            private String dg_type;
            private String mdg_caption;
            private int mdg_isdefault;
            private int dg_sequence;
            private String dg_logictype;
            private int gd_id;

            public int getDg_group() {
                return dg_group;
            }

            public void setDg_group(int dg_group) {
                this.dg_group = dg_group;
            }

            public String getDg_caption() {
                return dg_caption;
            }

            public void setDg_caption(String dg_caption) {
                this.dg_caption = dg_caption;
            }

            public String getDg_value() {
                return dg_value;
            }

            public void setDg_value(String dg_value) {
                this.dg_value = dg_value;
            }

            public String getDg_field() {
                return dg_field;
            }

            public void setDg_field(String dg_field) {
                this.dg_field = dg_field;
            }

            public int getDg_maxlength() {
                return dg_maxlength;
            }

            public void setDg_maxlength(int dg_maxlength) {
                this.dg_maxlength = dg_maxlength;
            }

            public String getDg_type() {
                return dg_type;
            }

            public void setDg_type(String dg_type) {
                this.dg_type = dg_type;
            }

            public String getMdg_caption() {
                return mdg_caption;
            }

            public void setMdg_caption(String mdg_caption) {
                this.mdg_caption = mdg_caption;
            }

            public int getMdg_isdefault() {
                return mdg_isdefault;
            }

            public void setMdg_isdefault(int mdg_isdefault) {
                this.mdg_isdefault = mdg_isdefault;
            }

            public int getDg_sequence() {
                return dg_sequence;
            }

            public void setDg_sequence(int dg_sequence) {
                this.dg_sequence = dg_sequence;
            }

            public String getDg_logictype() {
                return dg_logictype;
            }

            public void setDg_logictype(String dg_logictype) {
                this.dg_logictype = dg_logictype;
            }

            public int getGd_id() {
                return gd_id;
            }

            public void setGd_id(int gd_id) {
                this.gd_id = gd_id;
            }
        }
    }
}
