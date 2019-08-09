package com.common.preferences;

/**
 * Created by Bitliker on 2017/9/26.
 */

public class RedSpUtil extends SpUtil {

    private static RedSpUtil api;

    public static RedSpUtil api() {
        RedSpUtil inst = api;
        if (inst == null) {
            synchronized (RedSpUtil.class) {
                inst = api;
                if (inst == null) {
                    inst = new RedSpUtil();
                    api = inst;
                }
            }
        }
        return inst;
    }

    private RedSpUtil() {

    }

    @Override
    protected String getName() {
        return "redConfig";
    }


    public boolean getDataInquiry() {
        return getBoolean("DataInquiry", false);
    }

    public void putDataInquiry(boolean isDataInquiry) {
        put("DataInquiry", isDataInquiry);
    }

    public boolean getReportStatis() {
        return getBoolean("ReportStatis", false);
    }

    public void putReportStatis(boolean isReportStatis) {
        put("ReportStatis", isReportStatis);
    }


}
