package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe 智慧产城公告
 * @date 2017/11/23 20:51
 */

public class CityIndustryAnnounceBean {
    private String mTitle;
    private String mContent;
    private String mUrl;

    private CityIndustryAnnounceBean(Builder builder) {
        this.mTitle = builder.mTitle;
        this.mContent = builder.mContent;
        this.mUrl = builder.mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public String getUrl() {
        return mUrl;
    }

    public static class Builder {
        private String mTitle;
        private String mContent;
        private String mUrl;

        public Builder title(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder content(String content) {
            this.mContent = content;
            return this;
        }

        public Builder url(String url) {
            this.mUrl = url;
            return this;
        }

        public CityIndustryAnnounceBean build() {
            return new CityIndustryAnnounceBean(this);
        }
    }
}
