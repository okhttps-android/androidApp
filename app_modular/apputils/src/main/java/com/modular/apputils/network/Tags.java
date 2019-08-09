package com.modular.apputils.network;

import android.util.SparseArray;

/**
 * Created by Bitliker on 2017/8/16.
 */

public class Tags extends SparseArray<Object> {

    public static final int DEF_TAG = 2054;
    public static final int RECORD_TAG = 2055;


    public int getRecord() {
        Object t = get(RECORD_TAG);
        return t != null && t instanceof Integer ? (int) t : -1;
    }

    public Object getTag() {
        return get(DEF_TAG);
    }

    public void tag(Object t) {
        put(DEF_TAG, t);
    }

    public void record(int code) {
        put(RECORD_TAG, code);
    }
}
