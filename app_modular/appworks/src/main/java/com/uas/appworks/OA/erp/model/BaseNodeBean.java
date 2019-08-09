package com.uas.appworks.OA.erp.model;

public class BaseNodeBean {
    /**
     * 节点Id
     */
    protected int id;
    /**
     * 节点父id
     */
    protected int pId;
    /**
     * 节点name
     */
    protected String name;


    public BaseNodeBean(int id, int pId, String name) {
        super();
        this.id = id;
        this.pId = pId;
        this.name = name;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
