package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/1/21.
 * function:
 */
public class OfficeAddressBean {

    /**
     * sessionId : 43EB69A8CBC6B932F82BE09F8AB98B73
     * success : true
     * listdata : [{"CS_WORKADDR":"广东省深圳市南山科技园科技南五路英唐大厦","CS_VALIDRANGE":"100","CS_ID":9619,"CS_LATITUDE":"22.54068486390692","CS_LONGITUDE":"113.9531350940523","CS_SHORTNAME":"深圳市润唐智能生活电器有限公司","CS_CODE":"2017010065","CS_INNERDISTANCE":500,"success":true},{"CS_WORKADDR":"深圳市南山区科技南五路5","CS_VALIDRANGE":"100","CS_ID":9620,"CS_LATITUDE":"22.54069320895665","CS_LONGITUDE":"113.9531261109972","CS_SHORTNAME":"英唐智控","CS_CODE":"2017010066","CS_INNERDISTANCE":500,"success":true},{"CS_WORKADDR":"深圳市南山区科技园科技中一路","CS_VALIDRANGE":"100","CS_ID":9618,"CS_LATITUDE":"22.546000557212412","CS_LONGITUDE":"113.94107983411239","CS_SHORTNAME":"腾讯大厦","CS_CODE":"2017010064","CS_INNERDISTANCE":500,"success":true},{"CS_WORKADDR":"英唐大厦","CS_VALIDRANGE":"100","CS_ID":9594,"CS_LATITUDE":"113.9529997697171","CS_LONGITUDE":"22.540450740157528","CS_SHORTNAME":"优软科技","CS_CODE":"2017010041","CS_INNERDISTANCE":0,"success":true}]
     */

    private String sessionId;
    private boolean success;
    private List<ListdataBean> listdata;

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

    public List<ListdataBean> getListdata() {
        return listdata;
    }

    public void setListdata(List<ListdataBean> listdata) {
        this.listdata = listdata;
    }

    public static class ListdataBean {
        /**
         * CS_WORKADDR : 广东省深圳市南山科技园科技南五路英唐大厦
         * CS_VALIDRANGE : 100
         * CS_ID : 9619
         * CS_LATITUDE : 22.54068486390692
         * CS_LONGITUDE : 113.9531350940523
         * CS_SHORTNAME : 深圳市润唐智能生活电器有限公司
         * CS_CODE : 2017010065
         * CS_INNERDISTANCE : 500
         * success : true
         */

        private String CS_WORKADDR;
        private String CS_VALIDRANGE;
        private int CS_ID;
        private String CS_LATITUDE;
        private String CS_LONGITUDE;
        private String CS_SHORTNAME;
        private String CS_CODE;
        private int CS_INNERDISTANCE;
        private boolean success;

        public String getCS_WORKADDR() {
            return CS_WORKADDR;
        }

        public void setCS_WORKADDR(String CS_WORKADDR) {
            this.CS_WORKADDR = CS_WORKADDR;
        }

        public String getCS_VALIDRANGE() {
            return CS_VALIDRANGE;
        }

        public void setCS_VALIDRANGE(String CS_VALIDRANGE) {
            this.CS_VALIDRANGE = CS_VALIDRANGE;
        }

        public int getCS_ID() {
            return CS_ID;
        }

        public void setCS_ID(int CS_ID) {
            this.CS_ID = CS_ID;
        }

        public String getCS_LATITUDE() {
            return CS_LATITUDE;
        }

        public void setCS_LATITUDE(String CS_LATITUDE) {
            this.CS_LATITUDE = CS_LATITUDE;
        }

        public String getCS_LONGITUDE() {
            return CS_LONGITUDE;
        }

        public void setCS_LONGITUDE(String CS_LONGITUDE) {
            this.CS_LONGITUDE = CS_LONGITUDE;
        }

        public String getCS_SHORTNAME() {
            return CS_SHORTNAME;
        }

        public void setCS_SHORTNAME(String CS_SHORTNAME) {
            this.CS_SHORTNAME = CS_SHORTNAME;
        }

        public String getCS_CODE() {
            return CS_CODE;
        }

        public void setCS_CODE(String CS_CODE) {
            this.CS_CODE = CS_CODE;
        }

        public int getCS_INNERDISTANCE() {
            return CS_INNERDISTANCE;
        }

        public void setCS_INNERDISTANCE(int CS_INNERDISTANCE) {
            this.CS_INNERDISTANCE = CS_INNERDISTANCE;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}

