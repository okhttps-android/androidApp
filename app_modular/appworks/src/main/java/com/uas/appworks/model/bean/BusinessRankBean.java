package com.uas.appworks.model.bean;

import android.support.annotation.NonNull;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:51
 */
public class BusinessRankBean implements Comparable<BusinessRankBean> {

    /**
     * fnum : 0
     * snun : 0
     * srates :
     * name : 余佳1
     * rank : 1
     * bnum : 0
     * nnum : 0
     */

    private int fnum;
    private int snun;
    private String srates;
    private String name;
    private String rank;
    private int bnum;
    private int nnum;

    public int getFnum() {
        return fnum;
    }

    public void setFnum(int fnum) {
        this.fnum = fnum;
    }

    public int getSnun() {
        return snun;
    }

    public void setSnun(int snun) {
        this.snun = snun;
    }

    public String getSrates() {
        return srates;
    }

    public void setSrates(String srates) {
        this.srates = srates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getBnum() {
        return bnum;
    }

    public void setBnum(int bnum) {
        this.bnum = bnum;
    }

    public int getNnum() {
        return nnum;
    }

    public void setNnum(int nnum) {
        this.nnum = nnum;
    }


    @Override
    public int compareTo(@NonNull BusinessRankBean businessRankBean) {
        try {
            int rankThis = Integer.parseInt(this.rank);
            int rankOther = Integer.parseInt(businessRankBean.rank);

            return rankThis - rankOther;
        } catch (Exception e) {
            return 1;
        }
    }
}
