package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/6/13.
 * function:
 */

public class CostSingleBean  {

    /**
     * sessionId : EDFDA46CEA544B4D707E34BB3566A854
     * combdatas : [{"DISPLAY":"16.8%经费","VALUE":"16.8%经费"},{"DISPLAY":"1.2%经费","VALUE":"1.2%经费"},{"DISPLAY":"42%","VALUE":"42%"},{"DISPLAY":"40%","VALUE":"40%"},{"DISPLAY":"1%经费","VALUE":"1%经费"},{"DISPLAY":"定额经费","VALUE":"定额经费"},{"DISPLAY":"其他","VALUE":"其他"}]
     */

    private String sessionId;
    private List<CombdatasBean> combdatas;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<CombdatasBean> getCombdatas() {
        return combdatas;
    }

    public void setCombdatas(List<CombdatasBean> combdatas) {
        this.combdatas = combdatas;
    }

    public static class CombdatasBean {
        /**
         * DISPLAY : 16.8%经费
         * VALUE : 16.8%经费
         */

        private String DISPLAY;
        private String VALUE;

        public String getDISPLAY() {
            return DISPLAY;
        }

        public void setDISPLAY(String DISPLAY) {
            this.DISPLAY = DISPLAY;
        }

        public String getVALUE() {
            return VALUE;
        }

        public void setVALUE(String VALUE) {
            this.VALUE = VALUE;
        }
    }
}
