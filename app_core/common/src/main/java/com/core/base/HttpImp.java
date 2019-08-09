package com.core.base;

/**
 * 有网络请求的接口
 * Created by Bitliker on 2016/12/15.
 */
public interface HttpImp {

    void showLoading();//显示刷新

    void dimssLoading();//关闭刷新

    void showToast(String message, int colorId);//显示提交的返回提示

    void showToast(String message);//显示提交的返回提示

    void showToast(int reId, int colorId);//显示提交的返回提示

    void showToast(int reId);//显示提交的返回提示

    void setTitle(CharSequence title);//设置标题栏

    void setTitleStyles(int styles);//设置主题

    void finish();

}
