package com.modular.appmessages.model;

import com.common.data.StringUtil;
import com.common.preferences.RedSpUtil;

/**
 * Created by Bitliker on 2017/10/19.
 */

public class MessageHeader {
    /**
     * 1.个人版本的服务预约预约类型
     * 2.服务预约主页
     * 3.uu运动
     * 4.审批流
     * 5.代办工作
     * 6.订阅号
     * 7.一元捐
     * 21.我的日程
     */
    private int type;//根据类型来决定要调转的界面
    private int icon;
    private int redNum;
    private boolean hideRed;
    private String name;
    private String subDoc;
    private String redKey;
    private String redMessage;
    private String time;
    private String tag;

    public MessageHeader(String name) {
        this.name = name;
    }

    public MessageHeader setTime(String time) {
        this.time = time;
        return this;
    }

    public MessageHeader setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public MessageHeader setRedMessage(String redMessage) {
        this.redMessage = redMessage;
        return this;
    }

    public MessageHeader setType(int type) {
        this.type = type;
        return this;
    }


    public MessageHeader setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public MessageHeader setRedNum(int redNum) {
        this.redNum = redNum;
        return this;
    }

    public MessageHeader setName(String name) {
        this.name = name;
        return this;
    }

    public MessageHeader setSubDoc(String subDoc) {
        this.subDoc = subDoc;
        return this;
    }

    public void hideRed() {
        if (redKey != null) {
            RedSpUtil.api().put(redKey, true);
            this.hideRed = true;
        }
    }

    public MessageHeader setRedKey(String redKey) {
        this.redKey = redKey;
        if (StringUtil.isEmpty(redKey)) {
            this.hideRed = true;
        } else {
            this.hideRed = RedSpUtil.api().getBoolean(redKey, false);
        }
        return this;
    }

    public int getIcon() {
        return icon;
    }

    public int getRedNum() {
        return redNum;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public String getSubDoc() {
        return subDoc == null ? "" : subDoc;
    }

    public String getRedKey() {
        return redKey;
    }

    public boolean isHideRed() {
        return hideRed;
    }

    public int getType() {
        return type;
    }

    public String getRedMessage() {
        return redMessage == null ? "" : redMessage;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public String getTag() {
        return tag == null ? "" : tag;
    }
}
