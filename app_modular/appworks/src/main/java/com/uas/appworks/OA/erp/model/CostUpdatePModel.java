package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/6/16.
 * function:
 */

public class CostUpdatePModel {

    /**
     * files : [{"fp_id":43163,"fp_path":"/usr/local/tomcat/webapps/postattach/U0747/09c68e4bc7544dfa84170b7b4c6c045b.jpeg","fp_size":378100,"fp_name":"Screenshot_2017-06-12-10-38-37.jpeg","fp_date":"2017-06-16 08:27:19","fp_man":"方龙海"},{"fp_id":43162,"fp_path":"/usr/local/tomcat/webapps/postattach/U0747/9b87575c099448b5bbfc4644a0b276d3.jpeg","fp_size":332932,"fp_name":"Screenshot_2017-06-15-20-18-50.jpeg","fp_date":"2017-06-16 08:27:19","fp_man":"方龙海"}]
     * success : true
     */

    private boolean success;
    private List<FilesBean> files;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<FilesBean> getFiles() {
        return files;
    }

    public void setFiles(List<FilesBean> files) {
        this.files = files;
    }

    public static class FilesBean {
        /**
         * fp_id : 43163
         * fp_path : /usr/local/tomcat/webapps/postattach/U0747/09c68e4bc7544dfa84170b7b4c6c045b.jpeg
         * fp_size : 378100
         * fp_name : Screenshot_2017-06-12-10-38-37.jpeg
         * fp_date : 2017-06-16 08:27:19
         * fp_man : 方龙海
         */

        private int fp_id;
        private String fp_path;
        private int fp_size;
        private String fp_name;
        private String fp_date;
        private String fp_man;

        public int getFp_id() {
            return fp_id;
        }

        public void setFp_id(int fp_id) {
            this.fp_id = fp_id;
        }

        public String getFp_path() {
            return fp_path;
        }

        public void setFp_path(String fp_path) {
            this.fp_path = fp_path;
        }

        public int getFp_size() {
            return fp_size;
        }

        public void setFp_size(int fp_size) {
            this.fp_size = fp_size;
        }

        public String getFp_name() {
            return fp_name;
        }

        public void setFp_name(String fp_name) {
            this.fp_name = fp_name;
        }

        public String getFp_date() {
            return fp_date;
        }

        public void setFp_date(String fp_date) {
            this.fp_date = fp_date;
        }

        public String getFp_man() {
            return fp_man;
        }

        public void setFp_man(String fp_man) {
            this.fp_man = fp_man;
        }
    }
}
