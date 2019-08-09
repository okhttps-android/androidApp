package com.core.model;


import java.util.ArrayList;
import java.util.List;

public class LoginEntity {
    //uas
    private String account;
    private String platform;
    private String master;
    private Integer enuu;
    private String website;
    private String name;
    private Integer masterId;
    private String businessCode;
    //b2b
    private Integer spaceId;
    private String userName;
    private List<Spaces> spaces = new ArrayList<>();

    public class Spaces {
        private Integer id;
        private String enuu;
        private String name;
        private String businessCode;

        public String getEnuu() {
            return enuu;
        }

        public void setEnuu(String enuu) {
            this.enuu = enuu;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getBusinessCode() {
            return businessCode;
        }

        public void setBusinessCode(String businessCode) {
            this.businessCode = businessCode;
        }
    }

    public Integer getMasterId() {
        return masterId;
    }

    public void setMasterId(Integer masterId) {
        this.masterId = masterId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEnuu() {
        return enuu;
    }

    public void setEnuu(Integer enuu) {
        this.enuu = enuu;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public List<Spaces> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Spaces> spaces) {
        this.spaces = spaces;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }


}
