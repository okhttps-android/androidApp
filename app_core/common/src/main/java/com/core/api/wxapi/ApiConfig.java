package com.core.api.wxapi;

/**
 * Created by Arison on 2017/3/2.
 */
public class ApiConfig {
    
    private static ApiConfig mInstance;
    
    private ApiModel mApiConfig;
    private ApiBase mApiBase;
    

    public ApiConfig(ApiModel api) {
        this.mApiConfig = api;
        
    }
    
    public static ApiConfig getInstance(ApiModel api){
        if (mInstance==null){
         synchronized (ApiConfig.class){
             if (mInstance==null){
                 mInstance=new ApiConfig(api);
             }
         }
        }
        return mInstance;
    }

    public ApiBase getmApiBase() {
        return (ApiBase)mApiConfig;
    }
    public ApiModel getmApiConfig() {
        return mApiConfig;
    }

    public void setmApiConfig(ApiModel mApiConfig) {
        this.mApiConfig = mApiConfig;
    }
    
    public ApiPlatform getmApiPlatform(){
        return  (ApiPlatform)mApiConfig;
    }
    
    public ApiUAS getmApiUAS(){
        return (ApiUAS)mApiConfig;
    }
}
