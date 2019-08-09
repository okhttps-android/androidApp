package com.baidu.aip.excep.model;

public class FaceVerify {
    private final int SCORE_PASS = 80;

    private String mpbBase64;
    private String userId;
    private String groupId;
    private String userInfo;
    private float score;

    public String getMpbBase64() {
        return mpbBase64;
    }

    public void setMpbBase64(String mpbBase64) {
        this.mpbBase64 = mpbBase64;
    }

    public boolean isPass(){
        return score>=SCORE_PASS;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
