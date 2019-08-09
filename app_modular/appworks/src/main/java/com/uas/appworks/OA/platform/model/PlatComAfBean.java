package com.uas.appworks.OA.platform.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/3/18.
 * function:
 */
public class PlatComAfBean  {

    /**
     * nodes : [{"jn_id":"2646238","jn_name":"部长15","jn_dealManId":"U0305","jn_dealManName":"吕全明","jn_dealTime":"2017-03-09 19:01:21","jn_dealResult":"同意","jn_nodeDescription":null},{"jn_id":"2646252","jn_name":"总监18","jn_dealManId":"U0303","jn_dealManName":"钟燕玲","jn_dealTime":"2017-03-09 21:22:07","jn_dealResult":"同意","jn_nodeDescription":null},{"jn_id":"2646327","jn_name":"总经理18","jn_dealManId":"U0102","jn_dealManName":"陈正亮","jn_dealTime":"2017-03-10 10:03:14","jn_dealResult":"同意","jn_nodeDescription":null}]
     * data : [{"jp_id":75559,"jp_caller":"workdaily","jp_keyvalue":106247,"jp_nodename":"部长15","jp_nodedealman":"u0305","jp_processdefid":"日报登记审批流_usoftsys-9","jp_nodedealmanname":"吕全明"},{"jp_id":75560,"jp_caller":"workdaily","jp_keyvalue":106247,"jp_nodename":"总监18","jp_nodedealman":"u0303","jp_processdefid":"日报登记审批流_usoftsys-9","jp_nodedealmanname":"钟燕玲"},{"jp_id":75561,"jp_caller":"workdaily","jp_keyvalue":106247,"jp_nodename":"总经理18","jp_nodedealman":"u0102","jp_processdefid":"日报登记审批流_usoftsys-9","jp_nodedealmanname":"陈正亮"}]
     * success : true
     */

    private boolean success;
    private List<NodesBean> nodes;
    private List<DataBean> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public static class NodesBean {
        /**
         * jn_id : 2646238
         * jn_name : 部长15
         * jn_dealManId : U0305
         * jn_dealManName : 吕全明
         * jn_dealTime : 2017-03-09 19:01:21
         * jn_dealResult : 同意
         * jn_nodeDescription : null
         */

        private String jn_id;
        private String jn_name;
        private String jn_dealManId;
        private String jn_dealManName;
        private String jn_dealTime;
        private String jn_result;
        private Object jn_nodeDescription;

        public String getJn_result() {
            return jn_result;
        }

        public void setJn_result(String jn_result) {
            this.jn_result = jn_result;
        }

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


        public Object getJn_nodeDescription() {
            return jn_nodeDescription;
        }

        public void setJn_nodeDescription(Object jn_nodeDescription) {
            this.jn_nodeDescription = jn_nodeDescription;
        }
    }

    public static class DataBean {
        /**
         * jp_id : 75559
         * jp_caller : workdaily
         * jp_keyvalue : 106247
         * jp_nodename : 部长15
         * jp_nodedealman : u0305
         * jp_processdefid : 日报登记审批流_usoftsys-9
         * jp_nodedealmanname : 吕全明
         */

        private int jp_id;
        private String jp_caller;
        private int jp_keyvalue;
        private String jp_nodename;
        private String jp_nodedealman;
        private String jp_processdefid;
        private String jp_nodedealmanname;

        public int getJp_id() {
            return jp_id;
        }

        public void setJp_id(int jp_id) {
            this.jp_id = jp_id;
        }

        public String getJp_caller() {
            return jp_caller;
        }

        public void setJp_caller(String jp_caller) {
            this.jp_caller = jp_caller;
        }

        public int getJp_keyvalue() {
            return jp_keyvalue;
        }

        public void setJp_keyvalue(int jp_keyvalue) {
            this.jp_keyvalue = jp_keyvalue;
        }

        public String getJp_nodename() {
            return jp_nodename;
        }

        public void setJp_nodename(String jp_nodename) {
            this.jp_nodename = jp_nodename;
        }

        public String getJp_nodedealman() {
            return jp_nodedealman;
        }

        public void setJp_nodedealman(String jp_nodedealman) {
            this.jp_nodedealman = jp_nodedealman;
        }

        public String getJp_processdefid() {
            return jp_processdefid;
        }

        public void setJp_processdefid(String jp_processdefid) {
            this.jp_processdefid = jp_processdefid;
        }

        public String getJp_nodedealmanname() {
            return jp_nodedealmanname;
        }

        public void setJp_nodedealmanname(String jp_nodedealmanname) {
            this.jp_nodedealmanname = jp_nodedealmanname;
        }
    }
}
