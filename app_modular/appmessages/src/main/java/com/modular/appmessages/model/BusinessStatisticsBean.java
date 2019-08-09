package com.modular.appmessages.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/3 11:17
 */

public class BusinessStatisticsBean implements Serializable {

    /**
     * industry : KTV
     * targets : [{"targetName":"房间预订时数列表","typelist":"0","targetDetails":[{"detailName":"日期","detailValue":""},{"detailName":"时数","detailValue":""},{"detailName":"房间名称","detailValue":""}]},{"targetName":"订单数量","typelist":"1","targetDetails":[{"detailName":"日期","detailValue":""},{"detailName":"数量","detailValue":""}]},{"targetName":"客户资料","typelist":"2","targetDetails":[{"detailName":"姓名","detailValue":""},{"detailName":"电话","detailValue":""},{"detailName":"预约次数","detailValue":""}]}]
     */

    private String industry;
    private List<TargetsBean> targets;

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public List<TargetsBean> getTargets() {
        return targets;
    }

    public void setTargets(List<TargetsBean> targets) {
        this.targets = targets;
    }

    public static class TargetsBean implements Serializable {
        /**
         * targetName : 房间预订时数列表
         * typelist : 0
         * targetDetails : [{"detailName":"日期","detailValue":""},{"detailName":"时数","detailValue":""},{"detailName":"房间名称","detailValue":""}]
         */

        private String targetName;
        private String typelist;
        private List<TargetDetailsBean> targetDetails;

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public String getTypelist() {
            return typelist;
        }

        public void setTypelist(String typelist) {
            this.typelist = typelist;
        }

        public List<TargetDetailsBean> getTargetDetails() {
            return targetDetails;
        }

        public void setTargetDetails(List<TargetDetailsBean> targetDetails) {
            this.targetDetails = targetDetails;
        }

        public static class TargetDetailsBean implements Serializable {
            /**
             * detailName : 日期
             * detailKey :
             * detailValue :
             */

            private String detailName;
            private String detailKey;
            private String detailValue;

            public String getDetailName() {
                return detailName;
            }

            public void setDetailName(String detailName) {
                this.detailName = detailName;
            }

            public String getDetailKey() {
                return detailKey;
            }

            public void setDetailKey(String detailKey) {
                this.detailKey = detailKey;
            }

            public String getDetailValue() {
                return detailValue;
            }

            public void setDetailValue(String detailValue) {
                this.detailValue = detailValue;
            }
        }
    }
}
