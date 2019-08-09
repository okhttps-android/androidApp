package com.core.model;

/**
 * Created by FANGlh on 2017/11/3.
 * function:
 */

public class StepRankingFirstBean {
    /**
     + "date TEXT,"
     + "my_userid TEXT,"
     + "my_rank TEXT, "
     + "my_steps TEXT, "
     + "f_userid TEXT, "
     + "f_name TEXT,"
     */

    private int _id;
    private String date;
    private String my_userid;
    private String my_rank;
    private String my_steps;
    private String f_userid;
    private String f_name;

    public StepRankingFirstBean(int _id,String date, String my_userid, String my_rank, String my_steps, String f_userid, String f_name) {
        this._id = _id;
        this.date = date;
        this.my_userid = my_userid;
        this.my_rank = my_rank;
        this.my_steps = my_steps;
        this.f_userid = f_userid;
        this.f_name = f_name;
    }

    public StepRankingFirstBean(){

    }
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMy_userid() {
        return my_userid;
    }

    public void setMy_userid(String my_userid) {
        this.my_userid = my_userid;
    }

    public String getMy_rank() {
        return my_rank;
    }

    public void setMy_rank(String my_rank) {
        this.my_rank = my_rank;
    }

    public String getMy_steps() {
        return my_steps;
    }

    public void setMy_steps(String my_steps) {
        this.my_steps = my_steps;
    }

    public String getF_userid() {
        return f_userid;
    }

    public void setF_userid(String f_userid) {
        this.f_userid = f_userid;
    }

    public String getF_name() {
        return f_name;
    }

    public void setF_name(String f_name) {
        this.f_name = f_name;
    }
}
