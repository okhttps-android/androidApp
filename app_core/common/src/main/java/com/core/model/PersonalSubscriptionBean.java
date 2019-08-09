package com.core.model;

/**
 * Created by RaoMeng on 2016/9/20.
 */
public class PersonalSubscriptionBean {
    private int NUM_ID;
    private String TITLE;
    private String KIND;
    private String TYPE;
    private int ISAPPLED;
    private String MASTER;
    private String USERNAME;
    private byte[] IMG;

    public byte[] getIMG() {
        return IMG;
    }

    public void setIMG(byte[] IMG) {
        this.IMG = IMG;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public String getMASTER() {
        return MASTER;
    }

    public void setMASTER(String MASTER) {
        this.MASTER = MASTER;
    }

    public int getNUM_ID() {
        return NUM_ID;
    }

    public void setNUM_ID(int NUM_ID) {
        this.NUM_ID = NUM_ID;
    }

    public String getTITLE() {
        return TITLE;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    public String getKIND() {
        return KIND;
    }

    public void setKIND(String KIND) {
        this.KIND = KIND;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public int getISAPPLED() {
        return ISAPPLED;
    }

    public void setISAPPLED(int ISAPPLED) {
        this.ISAPPLED = ISAPPLED;
    }

    @Override
    public String toString() {
        return "PersonalSubscriptionBean{" +
                "NUM_ID=" + NUM_ID +
                ", TITLE='" + TITLE + '\'' +
                ", KIND='" + KIND + '\'' +
                ", TYPE='" + TYPE + '\'' +
                ", ISAPPLED=" + ISAPPLED +
                '}';
    }
}
