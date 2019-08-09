package com.core.api.wxapi;

import com.core.app.MyApplication;
import com.core.utils.CommonUtil;


/**
 * Created by Arison on 2017/3/2.
 */
public class ApiUAS extends ApiBase implements ApiModel {


    public ApiUAS() {
        super.login= CommonUtil.getAppBaseUrl(MyApplication.getInstance())+"mobile/login.action";
    }
}
