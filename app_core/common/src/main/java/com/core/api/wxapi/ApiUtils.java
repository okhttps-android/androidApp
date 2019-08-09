package com.core.api.wxapi;

import android.content.Context;

import com.core.app.MyApplication;
import com.core.utils.CommonUtil;

/**
 * Created by Arison on 2017/3/3.
 */
public class ApiUtils {

    /**
      * @desc:判断当前系统属于哪种身份
      * @author：Arison on 2017/3/3
      */
    public static ApiModel getApiModel(){
        Context ct=MyApplication.getInstance().getApplicationContext();
        Boolean erpLogin= CommonUtil.getSharedPreferencesBoolean(ct, "erp_login");
        if(erpLogin){
            return new ApiUAS();
        }else{
            return new ApiPlatform();
        }
    }
    
}
