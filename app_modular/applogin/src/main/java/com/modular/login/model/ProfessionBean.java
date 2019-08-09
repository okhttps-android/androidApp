package com.modular.login.model;

import java.util.List;

/**
 * Created by RaoMeng on 2017/9/23.
 */

public class ProfessionBean {
    private String mProfessionFirstTitle = "";
    private List<ProfessionSecondBean> mProfessionSecondTitles;

    public String getProfessionFirstTitle() {
        return mProfessionFirstTitle;
    }

    public void setProfessionFirstTitle(String professionFirstTitle) {
        mProfessionFirstTitle = professionFirstTitle;
    }

    public List<ProfessionSecondBean> getProfessionSecondTitles() {
        return mProfessionSecondTitles;
    }

    public void setProfessionSecondTitles(List<ProfessionSecondBean> professionSecondTitles) {
        mProfessionSecondTitles = professionSecondTitles;
    }

    public static class ProfessionSecondBean {
        private String mProfessionSecondTitle = "";
        private List<String> mProfessionThirdTitles;

        public String getProfessionSecondTitle() {
            return mProfessionSecondTitle;
        }

        public void setProfessionSecondTitle(String professionSecondTitle) {
            mProfessionSecondTitle = professionSecondTitle;
        }

        public List<String> getProfessionThirdTitles() {
            return mProfessionThirdTitles;
        }

        public void setProfessionThirdTitles(List<String> professionThirdTitles) {
            mProfessionThirdTitles = professionThirdTitles;
        }

    }

}
