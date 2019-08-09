package com.modular.appmessages.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Bitliker on 2017/9/6.
 */

public class ApprovalUtil {

    /**
     * @desc:JSONArraya排序
     * @author：Arison on 2016/11/8
     */
    public static JSONArray sortJsonArray(JSONArray jsonArr) {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.size(); i++) {
            if ("待审批".equals(jsonArr.getJSONObject(i).getString("JP_STATUS"))) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "JP_LAUNCHTIME";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                Long valA = null;
                Long valB = null;
                try {
                    valA = (Long) a.get(KEY_NAME);
                    valB = (Long) b.get(KEY_NAME);
                } catch (JSONException e) {
                    //do something
                }
                int result = valA.compareTo(valB);
                return -result;
            }
        });
        jsonArr.clear();
        for (int i = 0; i < jsonValues.size(); i++) {
            jsonArr.add(i, jsonValues.get(i));

        }
        return jsonArr;
    }

}
