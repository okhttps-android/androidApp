package com.common.preferences;

/**
 * Created by Bitliker on 2017/8/10.
 */

public class SystemSpUtil extends SpUtil {
    private static SystemSpUtil api;

    public static SystemSpUtil api() {
        SystemSpUtil inst = api;
        if (inst == null) {
            synchronized (SystemSpUtil.class) {
                inst = api;
                if (inst == null) {
                    inst = new SystemSpUtil();
                    api = inst;
                }
            }
        }
        return inst;
    }

    @Override
    protected String getName() {
        return "baseSystem";
    }
}
