package com.common.data;

import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 * Created by Administrator on 2016/3/25.
 */
public class RegexUtil {
    public static final String REGEX_EMAIL = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";//邮箱
    public static final String REGEX_MOBILE = "(\\+\\d+)?1[34578]\\d{9}$";//手机号码
    public static final String REGEX_NUMBER = "^-?[1-9]\\d*$";//数字
    public static final String REGEX_FLOAT = "\\-?[1-9]\\d+(\\.\\d+)?";//小数
    public static final String REGEX_CHINESE = "[^\u4e00-\u9fa5]";//中文
    public static final String REGEX_BIRTHDAY = "[1-9]{4}([-./])\\d{1,2}\\1\\d{1,2}";//生日
    public static final String REGEX_IPADDRESS = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))";//ip地址
    public static final String REGEX_URL = "(http|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?";//网址

    public static boolean checkRegex(String text, String regex) {
        return Pattern.matches(regex, text);
    }


}
