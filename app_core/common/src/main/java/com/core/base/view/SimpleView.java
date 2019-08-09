package com.core.base.view;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/5/4 9:48
 */
public interface SimpleView extends BaseView {
    void requestSuccess(int what, Object object);

    void requestError(int what, String errorMsg);
}
