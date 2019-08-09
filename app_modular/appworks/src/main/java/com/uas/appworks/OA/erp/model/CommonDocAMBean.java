package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2016/11/25.
 */
public class CommonDocAMBean {

    /**
     * datas : {"formdata":[{"wo_recorder":"臧亚诚","wo_mankind":"主管及以下","wo_recorddate":"2016-10-26 00:00:00","wo_emcode":"U0745","wo_job":"测试员","wo_cop":"优软科技","wo_code":"WO16100015","wo_worktask":null,"wo_remark":null,"wo_depart":null,"wo_status":"在录入","wo_hrorg":"测试科"}],"formconfigs":[{"FD_DETNO":2,"FD_CAPTION":"单据编号","FD_FIELD":"wo_code","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":3,"FD_CAPTION":"所属公司","FD_FIELD":"wo_cop","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":4,"FD_CAPTION":"人员类型","FD_FIELD":"wo_mankind","FD_GROUP":null,"MFD_ISDEFAULT":-1},{"FD_DETNO":5,"FD_CAPTION":"状态","FD_FIELD":"wo_status","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":6,"FD_CAPTION":"录入人","FD_FIELD":"wo_emcode","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":7,"FD_CAPTION":null,"FD_FIELD":"wo_recorder","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":8,"FD_CAPTION":"岗位名称","FD_FIELD":"wo_job","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":9,"FD_CAPTION":"组织名称","FD_FIELD":"wo_hrorg","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":10,"FD_CAPTION":"部门名称","FD_FIELD":"wo_depart","FD_GROUP":null,"MFD_ISDEFAULT":0},{"FD_DETNO":11,"FD_CAPTION":"录入时间","FD_FIELD":"wo_recorddate","FD_GROUP":null,"MFD_ISDEFAULT":-1},{"FD_DETNO":12,"FD_CAPTION":"备注","FD_FIELD":"wo_remark","FD_GROUP":null,"MFD_ISDEFAULT":-1},{"FD_DETNO":13,"FD_CAPTION":"工作任务","FD_FIELD":"wo_worktask","FD_GROUP":null,"MFD_ISDEFAULT":-1}],"griddata":[{"wod_enddate":"2016-10-27 14:23:00","wod_startdate":"2016-10-26 10:23:00","wod_id":25641,"wod_type":"出差加班","wod_count":28,"wod_woid":21383,"wod_detno":1,"wod_empname":"陈萍","wod_empcode":"chenp","wod_jiax4":"销售部"}],"gridconfigs":[{"DG_SEQUENCE":1,"DG_CAPTION":"关联id","DG_FIELD":"wod_woid","MDG_ISDEFAULT":0},{"DG_SEQUENCE":2,"DG_CAPTION":"id","DG_FIELD":"wod_id","MDG_ISDEFAULT":0},{"DG_SEQUENCE":3,"DG_CAPTION":"序号","DG_FIELD":"wod_detno","MDG_ISDEFAULT":-1},{"DG_SEQUENCE":4,"DG_CAPTION":"员工工号","DG_FIELD":"wod_empcode","MDG_ISDEFAULT":-1},{"DG_SEQUENCE":5,"DG_CAPTION":"员工姓名","DG_FIELD":"wod_empname","MDG_ISDEFAULT":-1},{"DG_SEQUENCE":6,"DG_CAPTION":"部门","DG_FIELD":"wod_jiax4","MDG_ISDEFAULT":0},{"DG_SEQUENCE":7,"DG_CAPTION":"加班类型","DG_FIELD":"wod_type","MDG_ISDEFAULT":-1},{"DG_SEQUENCE":9,"DG_CAPTION":"起始时间","DG_FIELD":"wod_startdate","MDG_ISDEFAULT":-1},{"DG_SEQUENCE":10,"DG_CAPTION":"截止时间","DG_FIELD":"wod_enddate","MDG_ISDEFAULT":-1},{"DG_SEQUENCE":11,"DG_CAPTION":"当天加班时数","DG_FIELD":"wod_count","MDG_ISDEFAULT":-1}]}
     * sessionId : B6E0CFC64DEE7E1A0BEF1DBAB535C2E4
     * success : true
     */

    private DatasBean datas;
    private String sessionId;
    private boolean success;

    public DatasBean getDatas() {
        return datas;
    }

    public void setDatas(DatasBean datas) {
        this.datas = datas;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class DatasBean {
        private List<FormdataBean> formdata;
        private List<FormconfigsBean> formconfigs;
        private List<GriddataBean> griddata;
        private List<GridconfigsBean> gridconfigs;

        public List<FormdataBean> getFormdata() {
            return formdata;
        }

        public void setFormdata(List<FormdataBean> formdata) {
            this.formdata = formdata;
        }

        public List<FormconfigsBean> getFormconfigs() {
            return formconfigs;
        }

        public void setFormconfigs(List<FormconfigsBean> formconfigs) {
            this.formconfigs = formconfigs;
        }

        public List<GriddataBean> getGriddata() {
            return griddata;
        }

        public void setGriddata(List<GriddataBean> griddata) {
            this.griddata = griddata;
        }

        public List<GridconfigsBean> getGridconfigs() {
            return gridconfigs;
        }

        public void setGridconfigs(List<GridconfigsBean> gridconfigs) {
            this.gridconfigs = gridconfigs;
        }

        public static class FormdataBean {
            /**
             * wo_recorder : 臧亚诚
             * wo_mankind : 主管及以下
             * wo_recorddate : 2016-10-26 00:00:00
             * wo_emcode : U0745
             * wo_job : 测试员
             * wo_cop : 优软科技
             * wo_code : WO16100015
             * wo_worktask : null
             * wo_remark : null
             * wo_depart : null
             * wo_status : 在录入
             * wo_hrorg : 测试科
             */

            private String wo_recorder;
            private String wo_mankind;
            private String wo_recorddate;
            private String wo_emcode;
            private String wo_job;
            private String wo_cop;
            private String wo_code;
            private Object wo_worktask;
            private Object wo_remark;
            private Object wo_depart;
            private String wo_status;
            private String wo_hrorg;

            public String getWo_recorder() {
                return wo_recorder;
            }

            public void setWo_recorder(String wo_recorder) {
                this.wo_recorder = wo_recorder;
            }

            public String getWo_mankind() {
                return wo_mankind;
            }

            public void setWo_mankind(String wo_mankind) {
                this.wo_mankind = wo_mankind;
            }

            public String getWo_recorddate() {
                return wo_recorddate;
            }

            public void setWo_recorddate(String wo_recorddate) {
                this.wo_recorddate = wo_recorddate;
            }

            public String getWo_emcode() {
                return wo_emcode;
            }

            public void setWo_emcode(String wo_emcode) {
                this.wo_emcode = wo_emcode;
            }

            public String getWo_job() {
                return wo_job;
            }

            public void setWo_job(String wo_job) {
                this.wo_job = wo_job;
            }

            public String getWo_cop() {
                return wo_cop;
            }

            public void setWo_cop(String wo_cop) {
                this.wo_cop = wo_cop;
            }

            public String getWo_code() {
                return wo_code;
            }

            public void setWo_code(String wo_code) {
                this.wo_code = wo_code;
            }

            public Object getWo_worktask() {
                return wo_worktask;
            }

            public void setWo_worktask(Object wo_worktask) {
                this.wo_worktask = wo_worktask;
            }

            public Object getWo_remark() {
                return wo_remark;
            }

            public void setWo_remark(Object wo_remark) {
                this.wo_remark = wo_remark;
            }

            public Object getWo_depart() {
                return wo_depart;
            }

            public void setWo_depart(Object wo_depart) {
                this.wo_depart = wo_depart;
            }

            public String getWo_status() {
                return wo_status;
            }

            public void setWo_status(String wo_status) {
                this.wo_status = wo_status;
            }

            public String getWo_hrorg() {
                return wo_hrorg;
            }

            public void setWo_hrorg(String wo_hrorg) {
                this.wo_hrorg = wo_hrorg;
            }
        }

        public static class FormconfigsBean {
            /**
             * FD_DETNO : 2
             * FD_CAPTION : 单据编号
             * FD_FIELD : wo_code
             * FD_GROUP : null
             * MFD_ISDEFAULT : 0
             */

            private int FD_DETNO;
            private String FD_CAPTION;
            private String FD_FIELD;
            private Object FD_GROUP;
            private int MFD_ISDEFAULT;

            public int getFD_DETNO() {
                return FD_DETNO;
            }

            public void setFD_DETNO(int FD_DETNO) {
                this.FD_DETNO = FD_DETNO;
            }

            public String getFD_CAPTION() {
                return FD_CAPTION;
            }

            public void setFD_CAPTION(String FD_CAPTION) {
                this.FD_CAPTION = FD_CAPTION;
            }

            public String getFD_FIELD() {
                return FD_FIELD;
            }

            public void setFD_FIELD(String FD_FIELD) {
                this.FD_FIELD = FD_FIELD;
            }

            public Object getFD_GROUP() {
                return FD_GROUP;
            }

            public void setFD_GROUP(Object FD_GROUP) {
                this.FD_GROUP = FD_GROUP;
            }

            public int getMFD_ISDEFAULT() {
                return MFD_ISDEFAULT;
            }

            public void setMFD_ISDEFAULT(int MFD_ISDEFAULT) {
                this.MFD_ISDEFAULT = MFD_ISDEFAULT;
            }

            @Override
            public String toString() {
                return "FormconfigsBean{" +
                        "FD_DETNO=" + FD_DETNO +
                        ", FD_CAPTION='" + FD_CAPTION + '\'' +
                        ", FD_FIELD='" + FD_FIELD + '\'' +
                        ", FD_GROUP=" + FD_GROUP +
                        ", MFD_ISDEFAULT=" + MFD_ISDEFAULT +
                        '}';
            }
        }

        public static class GriddataBean {
            /**
             * wod_enddate : 2016-10-27 14:23:00
             * wod_startdate : 2016-10-26 10:23:00
             * wod_id : 25641
             * wod_type : 出差加班
             * wod_count : 28
             * wod_woid : 21383
             * wod_detno : 1
             * wod_empname : 陈萍
             * wod_empcode : chenp
             * wod_jiax4 : 销售部
             */

            private String wod_enddate;
            private String wod_startdate;
            private int wod_id;
            private String wod_type;
            private String wod_count;
            private int wod_woid;
            private int wod_detno;
            private String wod_empname;
            private String wod_empcode;
            private String wod_jiax4;

            public String getWod_enddate() {
                return wod_enddate;
            }

            public void setWod_enddate(String wod_enddate) {
                this.wod_enddate = wod_enddate;
            }

            public String getWod_startdate() {
                return wod_startdate;
            }

            public void setWod_startdate(String wod_startdate) {
                this.wod_startdate = wod_startdate;
            }

            public int getWod_id() {
                return wod_id;
            }

            public void setWod_id(int wod_id) {
                this.wod_id = wod_id;
            }

            public String getWod_type() {
                return wod_type;
            }

            public void setWod_type(String wod_type) {
                this.wod_type = wod_type;
            }

            public String getWod_count() {
                return wod_count;
            }

            public void setWod_count(String wod_count) {
                this.wod_count = wod_count;
            }

            public int getWod_woid() {
                return wod_woid;
            }

            public void setWod_woid(int wod_woid) {
                this.wod_woid = wod_woid;
            }

            public int getWod_detno() {
                return wod_detno;
            }

            public void setWod_detno(int wod_detno) {
                this.wod_detno = wod_detno;
            }

            public String getWod_empname() {
                return wod_empname;
            }

            public void setWod_empname(String wod_empname) {
                this.wod_empname = wod_empname;
            }

            public String getWod_empcode() {
                return wod_empcode;
            }

            public void setWod_empcode(String wod_empcode) {
                this.wod_empcode = wod_empcode;
            }

            public String getWod_jiax4() {
                return wod_jiax4;
            }

            public void setWod_jiax4(String wod_jiax4) {
                this.wod_jiax4 = wod_jiax4;
            }
        }

        public static class GridconfigsBean {
            /**
             * DG_SEQUENCE : 1
             * DG_CAPTION : 关联id
             * DG_FIELD : wod_woid
             * MDG_ISDEFAULT : 0
             */

            private int DG_SEQUENCE;
            private String DG_CAPTION;
            private String DG_FIELD;
            private int MDG_ISDEFAULT;

            public int getDG_SEQUENCE() {
                return DG_SEQUENCE;
            }

            public void setDG_SEQUENCE(int DG_SEQUENCE) {
                this.DG_SEQUENCE = DG_SEQUENCE;
            }

            public String getDG_CAPTION() {
                return DG_CAPTION;
            }

            public void setDG_CAPTION(String DG_CAPTION) {
                this.DG_CAPTION = DG_CAPTION;
            }

            public String getDG_FIELD() {
                return DG_FIELD;
            }

            public void setDG_FIELD(String DG_FIELD) {
                this.DG_FIELD = DG_FIELD;
            }

            public int getMDG_ISDEFAULT() {
                return MDG_ISDEFAULT;
            }

            public void setMDG_ISDEFAULT(int MDG_ISDEFAULT) {
                this.MDG_ISDEFAULT = MDG_ISDEFAULT;
            }

            @Override
            public String toString() {
                return "GridconfigsBean{" +
                        "DG_SEQUENCE=" + DG_SEQUENCE +
                        ", DG_CAPTION='" + DG_CAPTION + '\'' +
                        ", DG_FIELD='" + DG_FIELD + '\'' +
                        ", MDG_ISDEFAULT=" + MDG_ISDEFAULT +
                        '}';
            }
        }
    }
}
