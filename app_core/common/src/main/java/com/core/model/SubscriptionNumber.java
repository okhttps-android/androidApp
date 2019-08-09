package com.core.model;

/**
 * 订阅号实体类
 * Created by RaoMeng on 2016/9/21.
 */
public class SubscriptionNumber {
    private int id;//订阅号id
    private String title;//订阅号标题
    private String kind;//订阅号类型（公、私）
    private int status;//订阅号状态（已订阅，已申请，未订阅）
    private String type;//订阅号类别
    private String master;//订阅号所属账套
    private String username;//订阅号所属账号
    private int removed;//是否被本地移除
    private byte[] img;

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public int getRemoved() {
        return removed;
    }

    public void setRemoved(int removed) {
        this.removed = removed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "SubscriptionNumber{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", kind='" + kind + '\'' +
                ", status=" + status +
                '}';
    }
}
