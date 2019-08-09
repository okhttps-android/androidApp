package com.core.model;

import android.support.annotation.IntDef;

import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bitliker on 2017/9/7.
 */

public class UUHelperModel {
    public static final int ARTICLE_SINGLE = 1, ARTICLE_MERGE = 2;

    private int id;
    private int type;
    private long timeSend;//发送时间
    private String date;//发送日期（通过timeSend）

    private String title;
    private String content;//显示内容
    private String imageUrl;//图片网址
    private String linkUrl;//链接网址
    private String iconUrl;//小图片网址
    private boolean readed;


    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", id);
        map.put("type", type);
        map.put("title", title);
        map.put("date", date);
        map.put("timeSend", timeSend);
        map.put("imageUrl", imageUrl);
        map.put("linkUrl", linkUrl);
        map.put("iconUrl", iconUrl);
        map.put("content", content);
        return JSONUtil.map2JSON(map);
    }


    /*通过数据源获取到数据封装成类*/
    public UUHelperModel(long timeSend) {
        this(timeSend, DateFormatUtil.long2Str(timeSend, DateFormatUtil.YMD_HMS), ARTICLE_SINGLE);
    }

    /*数据库获取到的数据封装成类*/
    public UUHelperModel(long timeSend, String date, int type) {
        this.timeSend = timeSend;
        this.date = date;
        this.type = type;
    }


    public UUHelperModel setId(int id) {
        this.id = id;
        return this;
    }

    public UUHelperModel setType(@Duration int type) {
        this.type = type;
        return this;
    }

    public UUHelperModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public UUHelperModel setContent(String content) {
        this.content = content;
        return this;
    }

    public UUHelperModel setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public UUHelperModel setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
        return this;
    }

    public UUHelperModel setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public UUHelperModel setReaded(boolean readed) {
        this.readed = readed;
        return this;
    }


    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public long getTimeSend() {
        return timeSend;
    }

    public String getDate() {
        return date == null ? "未知" : date;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public boolean isReaded() {
        return readed;
    }

    public boolean isTag() {
        return false;
    }

    @IntDef({ARTICLE_SINGLE, ARTICLE_MERGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }
}
