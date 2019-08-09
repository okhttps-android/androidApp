package com.common.data;

import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;

import com.common.config.BaseConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {


	public static boolean hasOneEqual(String messgae, String... keys) {
		if (isEmpty(messgae) || keys == null || keys.length <= 0) return false;
		for (String key : keys) {
			if (messgae.equals(key))
				return true;
		}
		return false;
	}

	public static boolean hasOneEqualUpperCase(String messgae, String... keys) {
		if (isEmpty(messgae) || keys == null || keys.length <= 0) return false;
		for (String key : keys) {
			if (messgae.toUpperCase().equals(key.toUpperCase()))
				return true;
		}
		return false;
	}

	/*判断是不是一个合法的电子邮件地址*/
	public static boolean isEmail(String email) {
		if (email == null || email.trim().length() == 0)
			return false;
		return Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*").matcher(email).matches();
	}

	/* 是否是手机号 */
	public static boolean isMobileNumber(String mobiles) {
//		Matcher mat = Pattern.compile("^[1][3,5,7,8][0-9]\\\\d{8}$").matcher(mobiles);
//		return mat.matches();
		return true;
	}

	public static boolean isEmpty(String input) {
		if (input == null || "".equals(input))
			return true;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断指定字符是否被包含
	 *
	 * @param text 大字符
	 * @param str  小字符
	 * @return 是否包含
	 */
	public static boolean isInclude(String text, String str) {
		if (StringUtil.isEmpty(str))
			return true;
		if (StringUtil.isEmpty(text)) return false;
		try {
			Pattern p = Pattern.compile(str);
			Matcher m = p.matcher(text);
			return m.find();
		} catch (Exception e) {
			return false;
		}
	}

	/* 检测是否是正确的昵称格式 ( 3-10个字符)*/
	public static boolean isNickName(String nickName) {
		if (TextUtils.isEmpty(nickName)) {
			return false;
		}
		Matcher mat = Pattern.compile("^[\u4e00-\u9fa5_a-zA-Z0-9_]{0,15}$").matcher(nickName);
		return mat.matches();
	}

	/*判断两个字符串相等*/
	public static boolean strEquals(String s1, String s2) {
		if (s1 == s2) {// 引用相等直接返回true
			return true;
		}
		boolean emptyS1 = s1 == null || s1.trim().length() == 0;
		boolean emptyS2 = s2 == null || s2.trim().length() == 0;
		if (emptyS1 && emptyS2) {// 都为空，认为相等
			return true;
		}
		if (s1 != null) {
			return s1.equals(s2);
		}
		if (s2 != null) {
			return s2.equals(s1);
		}
		return false;
	}

	/*获取字符中的中文*/
	public static String getChinese(String message) {
		String reg = "[^\u4e00-\u9fa5]";
		return message.replaceAll(reg, "");
	}

	public static String getMessage(String message) {
		return StringUtil.isEmpty(message) ? "" : message;
	}

	public static String getMessage(int reid) {
		if (reid <= 0) return "";
		try {
			return BaseConfig.getContext().getString(reid);
		} catch (Exception e) {
			return "";
		}
	}


	public static String getTextRexHttp(TextView tv) {
		if (tv == null || TextUtils.isEmpty(tv.getText())) return "";
		return toHttpString(tv.getText().toString());
	}	public static String getText(TextView tv) {
		if (tv == null || TextUtils.isEmpty(tv.getText())) return "";
		return toHttpString(tv.getText().toString());
	}

	//Bitliker 从字符串中取出第一个数字
	public static int getFirstInt(String message, int defValue) {
		if (StringUtil.isEmpty(message)) return defValue;
		Pattern p = Pattern.compile("(\\d+)");
		Matcher m = p.matcher(message);
		if (m.find()) {
			return Integer.parseInt(m.group(0));
		}
		return defValue;
	}

	/*获取第一个括号里的值*/
	public static String getFirstBrackets(String str) {
		if (str == null) return "";
		Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String name = matcher.group();
			if (name != null && name.length() > 0)
				return name;
		}
		return "";
	}

	public static String getLastBracket(String str) {
		if (str == null) return "";
		String message = "";
		Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
		Matcher m = p.matcher(str);
		while (m.find()) {
			message = m.group().substring(1, m.group().length() - 1);
		}
		return message;
	}

	/*去掉特殊字符  Im部分*/
	public static String replaceSpecialChar(String str) {
		if (str != null && str.length() > 0) {
			return str.replaceAll("&#39;", "’").replaceAll("&#039;", "’").replaceAll("&nbsp;", " ").replaceAll("\r\n", "\n").replaceAll("\n", "\r\n");
		}
		return "";
	}

	/*特殊字符处理，转为可以上传的字符*/
	public static String toHttpString(String s) {
		if (StringUtil.isEmpty(s)) return "";
		StringBuffer sb = new StringBuffer();
		s.replaceAll("\n", "");
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '/':
					sb.append("\\/");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString().replaceAll("%", "");
	}

	public static void removieLast(StringBuilder builder) {
		if (builder.length() > 1)
			builder.deleteCharAt(builder.length() - 1);
	}

	/*EditText显示Error*/
	public static CharSequence editTextHtmlErrorTip(int resId) {
		return editTextHtmlErrorTip(BaseConfig.getContext().getString(resId));
	}

	public static CharSequence editTextHtmlErrorTip(String text) {
		CharSequence html = Html.fromHtml("<font color='red'>" + text + "</font>");
		return html;
	}
}