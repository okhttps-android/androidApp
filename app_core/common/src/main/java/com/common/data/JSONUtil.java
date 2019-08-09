package com.common.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitliker on 2017/8/15.
 */

public class JSONUtil {

    public static <T> String map2JSON(Map<String, T> map) {
        if (map == null) return "";
        /*StringBuilder builder = new StringBuilder("{\n");
        for (Map.Entry<String, T> e : map.entrySet()) {
            builder.append("\"" + e.getKey() + "\":");
            if (e.getValue() instanceof String || e.getValue() instanceof CharSequence) {
                builder.append("\"" + e.getValue() + "\",\n");
            }else if (e.getValue() instanceof Map) {
                builder.append( map2JSON((Map)e.getValue()) + ",\n");
            }  else {
                builder.append(e.getValue() + ",\n");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);
        builder.append("\n}");*/
        return JSON.toJSONString(map);
    }

    public static <T> String map2JSON(List<Map<String, T>> maps) {
        if (maps == null || maps.size() <= 0) return "";
        /*StringBuilder builder = new StringBuilder("[\n");
        for (Map<String, T> m : maps) {
            builder.append("{\n");
            for (Map.Entry<String, T> e : m.entrySet()) {
                builder.append("\"" + e.getKey() + "\":");
                if (e.getValue() instanceof String || e.getValue() instanceof CharSequence) {
                    builder.append("\"" + StringUtil.toHttpString((String) e.getValue()) + "\",\n");
                } else {
                    builder.append(e.getValue() + ",\n");
                }
            }
            StringUtil.removieLast(builder);
            StringUtil.removieLast(builder);
            builder.append("\n},\n");
        }
        StringUtil.removieLast(builder);
        StringUtil.removieLast(builder);
        builder.append("\n]");*/
        return JSON.toJSONString(maps);
    }

    public static String param2Url(String url, Map<String, Object> param) {
        if (StringUtil.isEmpty(url))
            return "";
        StringBuilder urlBuilder = new StringBuilder(url);
        if (param == null || param.isEmpty()) {
            return urlBuilder.toString();
        }
        if (!url.contains("?"))
            urlBuilder.append("?");
        else urlBuilder.append("&");
        for (Map.Entry<String, Object> e : param.entrySet()) {
            if (e.getValue() == null || StringUtil.isEmpty(e.getKey()))
                continue;
            String value = null;
            try {
                value = URLEncoder.encode(e.getValue().toString(), "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                value = e.getValue().toString();
            }
            urlBuilder.append(String.format("%s=%s", e.getKey(), value));
            urlBuilder.append("&");
        }
        if (urlBuilder.length() > 1)
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        return urlBuilder.toString();
    }

    public static JSONObject getJSONObject(String message, String... keys) {
        try {
            return getJSONObject(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static JSONArray getJSONArray(String message, String... keys) {
        try {
            return getJSONArray(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static boolean getBoolean(String message, String... keys) {
        try {
            return getBoolean(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getText(String message, String... keys) {
        try {
            return getText(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return "";
        }
    }

    public static int getInt(String message, String... keys) {
        try {
            return getInt(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getLong(String message, String... keys) {
        try {
            return getLong(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return 0;
        }
    }

    public static double getDouble(String message, String... keys) {
        try {
            return getDouble(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return 0;
        }
    }

    public static float getFloat(String message, String... keys) {
        try {
            return getFloat(JSON.parseObject(message), keys);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean satisfyGet(JSONObject object, String... keys) {
        return !(object == null || keys == null || keys.length <= 0);
    }

    public static JSONObject getJSONObject(JSONObject object, String... keys) {
        if (satisfyGet(object, keys)) {
            try {
                for (String key : keys) {
                    if (key != null && object.containsKey(key) && object.get(key) != null && object.get(key) instanceof JSONObject) {
                        return object.getJSONObject(key);
                    }
                }
            } catch (Exception e) {
                return new JSONObject();
            }
        }
        return new JSONObject();
    }

    public static JSONArray getJSONArray(JSONObject object, String... keys) {
        try {
            if (object == null || keys == null || keys.length <= 0) return new JSONArray();
            for (String key : keys) {
                if (object.containsKey(key) && object.get(key) instanceof JSONArray) {
                    return object.getJSONArray(key);
                }
            }
        } catch (Exception e) {
            return new JSONArray();
        }
        return new JSONArray();
    }

    public static boolean getBoolean(JSONObject object, String... keys) {
        try {
            String bool = getDataForJson(object, keys);
            return Boolean.valueOf(bool);
        } catch (Exception e) {
            return false;
        }

    }

    public static String getText(JSONObject object, String... keys) {
        try {
            String t = getDataForJson(object, keys);
            if (StringUtil.isEmpty(t) || StringUtil.hasOneEqual(t, "(null)", "null")) {
                return "";
            } else
                return t;
        } catch (Exception e) {
            return "";
        }
    }

    public static int getInt(JSONObject object, String... keys) {
        try {
            String i = getDataForJson(object, keys);
            return Integer.valueOf(i);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getLong(JSONObject object, String... keys) {
        try {
            String i = getDataForJson(object, keys);
            return i == null ? 0 : Long.valueOf(i);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getDouble(JSONObject object, String... keys) {
        try {
            String d = getDataForJson(object, keys);
            return Double.valueOf(d);
        } catch (Exception e) {
            return 0;

        }
    }

    public static float getFloat(JSONObject object, String... keys) {
        try {
            String f = getDataForJson(object, keys);
            return Float.valueOf(f);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getDataForJson(JSONObject object, String... keys) {
        if (object == null || keys == null || keys.length <= 0) return null;
        Object o = null;
        for (String key : keys) {
            o = object.get(key);
            if (o != null)
                return o.toString();
        }
        return null;
    }


    /*获取接口时间格式，由于独立版和erp版本返回的时间格式不一样 long 和 String */
    public static long getTime(JSONObject jsonObject, String... keys) {
        if (jsonObject == null || keys == null || keys.length <= 0) return 0;
        for (String k : keys) {
            try {
                if (jsonObject.containsKey(k)) {
                    Object o = jsonObject.get(k);
                    if (o instanceof String) {
                        return DateFormatUtil.str2Long((String) o, DateFormatUtil.YMD_HMS);
                    } else if (o instanceof Long) {
                        return (long) o;
                    }
                }
            } catch (Exception e) {

            }
        }
        return 0;
    }

    /*验证一个字符串是否是合法的JSON串*/
    public static boolean validate(String message) {
        if (StringUtil.isEmpty(message)) return false;
        try {
            JSON.parse(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*验证一个字符串是否是合法的JSON串*/
    public static boolean validateJSONObject(String message) {
        if (StringUtil.isEmpty(message)) return false;
        try {
            JSON.parseObject(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*验证一个字符串是否是合法的JSON串*/
    public static boolean validateJSONArray(String message) {
        if (StringUtil.isEmpty(message)) return false;
        try {
            JSON.parseArray(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
