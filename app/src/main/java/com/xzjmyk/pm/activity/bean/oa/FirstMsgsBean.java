package com.xzjmyk.pm.activity.bean.oa;

import java.util.List;

/**
 * Created by FANGlh on 2017/3/1.
 * function:
 */
public class FirstMsgsBean {
    /**
     * allCount : 7
     * success : true
     * sessionId : B8F9A2A6C76EACC4F48BFD9BE92A5C19
     * listdata : [{"title":"通知公告","count":1,"lastMessage":"有新的会议纪要，请注意查收!<\/br><span style='font-weight:bold' >标题:<\/span><a style=\"padding-left:10px;\" href=\"javascript:parent.openUrl('jsps/oa/meeting/meetingDoc.jsp?formCondition=md_idIS7306')\">1213<\/a><\/br>","type":"note"},{"title":"普通知会","count":4,"lastMessage":"会议通知&nbsp;[02-13 16:54]<a href=\"javascript:openUrl('jsps/oa/meeting/meetingroomapply.jsp?formCondition=ma_id=30451&gridCondition=md_maid=30451')\" style=\"font-size:18px; color:red;\">会议已取消，查看会议详情<\/a><\/br>","type":"common"},{"title":"b2b提醒","count":2,"lastMessage":"采购回复&nbsp;[02-20 19:14]<a href=\"javascript:openUrl('jsps/scm/purchase/purchase.jsp?formCondition=pu_id=50706185&gridCondition=pd_puid=50706185')\" style=\"font-size:18px; color:red;\">采购单:B2BMP170200063<\/a><\/br>","type":"b2b"}]
     */

    private int allCount;
    private boolean success;
    private String sessionId;
    private List<ListdataBean> listdata;

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<ListdataBean> getListdata() {
        return listdata;
    }

    public void setListdata(List<ListdataBean> listdata) {
        this.listdata = listdata;
    }

    public static class ListdataBean {
        /**
         * title : 通知公告
         * count : 1
         * lastMessage : 有新的会议纪要，请注意查收!</br><span style='font-weight:bold' >标题:</span><a style="padding-left:10px;" href="javascript:parent.openUrl('jsps/oa/meeting/meetingDoc.jsp?formCondition=md_idIS7306')">1213</a></br>
         * type : note
         */

        private String title;
        private int count;
        private String lastMessage;
        private String type;
        private String lastTime;

        public String getLastTime() {
            return lastTime;
        }

        public void setLastTime(String lastTime) {
            this.lastTime = lastTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
