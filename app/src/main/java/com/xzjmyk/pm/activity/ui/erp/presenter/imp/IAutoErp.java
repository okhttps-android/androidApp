package com.xzjmyk.pm.activity.ui.erp.presenter.imp;

/**
 * 自动签到回调
 * Created by Bitliker on 2016/12/16.
 */
public interface IAutoErp {

    /**
     * 签到成功
     *
     * @param type   1.内勤签到  2.外勤签到
     * @param saveOk 是否保存到数据库成功
     */
    void success(int type, boolean saveOk);


    /**
     * 签到失败
     *
     * @param type    1.内勤签到  2.外勤签到
     * @param message 失败信息
     */
    void failure(int type, String message);
}
