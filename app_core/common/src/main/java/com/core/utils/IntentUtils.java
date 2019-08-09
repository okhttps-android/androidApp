package com.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.common.LogUtil;


public class IntentUtils {
    public static final String KEY_URL = "url";
    public static final String KEY_TITLE = "title";
    public static final String KEY_NEER_COOKIE = "neerCookie";
    public static final String KEY_NEER_SHARE = "neerShare";
    public static final String KEY_SHARE_IMAGE = "shareImage";
    public static final String KEY_SHARE_CONTENT = "shareContent";


    public static void webLinks(Context ct, String url, String text) {
        Intent intent_web = new Intent("com.modular.main.WebViewCommActivity");
        intent_web.putExtra("url", url);
        intent_web.putExtra("p", text);
        ct.startActivity(intent_web);
    }

    public static void linkCommonWeb(Context ct, String url) {
        linkCommonWeb(ct, url, null, null, null, false, false);
    }

    public static void linkCommonWeb(Context ct, String url, String title) {
        LogUtil.i("url=" + url);
        linkCommonWeb(ct, url, title, null, null, false, false);
    }

    public static void linkCommonWeb(Context ct,
                                     String url,
                                     String title,
                                     String imageUrl,
                                     String content) {
        linkCommonWeb(ct, url, title, imageUrl, content, true, false);
    }

    public static void linkCommonWeb(Context ct,
                                     String url,
                                     String title,
                                     String imageUrl,
                                     String content,
                                     boolean neerShare,
                                     boolean neerCookie,
                                     boolean needEnablePullDown) {
        Intent intent_web = new Intent("com.modular.message.MessageWebActivity");
        intent_web.putExtra(KEY_URL, url);
        intent_web.putExtra(KEY_TITLE, title);
        intent_web.putExtra(KEY_NEER_COOKIE, neerCookie);
        intent_web.putExtra(KEY_NEER_SHARE, neerShare);
        intent_web.putExtra(KEY_SHARE_IMAGE, imageUrl);
        intent_web.putExtra(KEY_SHARE_CONTENT, content);
        intent_web.putExtra("EnablePullDown", needEnablePullDown);
        if (ct instanceof Activity){
            ((Activity)ct).startActivityForResult(intent_web,0x21);
        }else{
            ct.startActivity(intent_web);
        }
    }

    public static void linkCommonWeb(Context ct,
                                     String url,
                                     String title,
                                     String imageUrl,
                                     String content,
                                     boolean neerShare,
                                     boolean neerCookie) {
        linkCommonWeb(ct,url,title,imageUrl,content,neerShare,neerCookie,true);
    }


}
