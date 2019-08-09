package com.uas.appworks.datainquiry.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/16.
 */
public class GridMenuDataInquiryBean implements Serializable {
    private String modelName;
    private List<QueryScheme> mQuerySchemes;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<QueryScheme> getQuerySchemes() {
        return mQuerySchemes;
    }

    public void setQuerySchemes(List<QueryScheme> querySchemes) {
        mQuerySchemes = querySchemes;
    }

    public static class QueryScheme implements Serializable {
        private String title;
        private String caller;
        private String scheme;
        private String schemeId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getSchemeId() {
            return schemeId;
        }

        public void setSchemeId(String schemeId) {
            this.schemeId = schemeId;
        }
    }
}
