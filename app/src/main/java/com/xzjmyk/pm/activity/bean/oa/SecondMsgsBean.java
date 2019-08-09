package com.xzjmyk.pm.activity.bean.oa;

import java.util.List;

/**
 * Created by FANGlh on 2017/3/1.
 * function:
 */
public class SecondMsgsBean {

    /**
     * allCount : 4
     * success : true
     * sessionId : 4FE799E3C9CE9160FBE14AB1445A98BE
     * listdata : [{"id":1462206,"releaser":"徐健","createTime":"2017-02-13 00:00:00","subTitle":"会议通知&nbsp;[02-13 10:01]<a href=\"javascript:openUrl('jsps/oa/meeting/meetingroomapply.jsp?formCondition=ma_id=30447&gridCondition=md_maid=30447')\" style=\"font-size:18px; color:red;\">查看会议详情<\/a><\/br>","title":"会议提醒"},{"id":1463907,"releaser":"陈萍","createTime":"2017-02-13 00:00:00","subTitle":"会议通知&nbsp;[02-13 16:54]<a href=\"javascript:openUrl('jsps/oa/meeting/meetingroomapply.jsp?formCondition=ma_id=30451&gridCondition=md_maid=30451')\" style=\"font-size:18px; color:red;\">会议已取消，查看会议详情<\/a><\/br>","title":"会议提醒"},{"id":1463150,"releaser":"徐健","createTime":"2017-02-13 00:00:00","subTitle":"会议通知&nbsp;[02-13 10:56]<a href=\"javascript:openUrl('jsps/oa/meeting/meetingroomapply.jsp?formCondition=ma_id=30451&gridCondition=md_maid=30451')\" style=\"font-size:18px; color:red;\">查看会议详情<\/a><\/br>","title":"会议提醒"},{"id":1462698,"releaser":"徐健","createTime":"2017-02-13 00:00:00","subTitle":"会议通知&nbsp;[02-13 10:12]<a href=\"javascript:openUrl('jsps/oa/meeting/meetingroomapply.jsp?formCondition=ma_id=30447&gridCondition=md_maid=30447')\" style=\"font-size:18px; color:red;\">会议变更，查看会议详情<\/a><\/br>","title":"会议提醒"}]
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
         * id : 1462206
         * releaser : 徐健
         * createTime : 2017-02-13 00:00:00
         * subTitle : 会议通知&nbsp;[02-13 10:01]<a href="javascript:openUrl('jsps/oa/meeting/meetingroomapply.jsp?formCondition=ma_id=30447&gridCondition=md_maid=30447')" style="font-size:18px; color:red;">查看会议详情</a></br>
         * title : 会议提醒
         */

        private int id;
        private String releaser;
        private String createTime;
        private String subTitle;
        private String title;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getReleaser() {
            return releaser;
        }

        public void setReleaser(String releaser) {
            this.releaser = releaser;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
