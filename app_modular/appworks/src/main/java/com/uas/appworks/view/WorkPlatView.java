package com.uas.appworks.view;

import com.core.base.view.BaseView;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/9 14:27
 */

public interface WorkPlatView extends BaseView {
    void requestSuccess(int what, Object object);

    void requestError(int what, String errorMsg);
}
