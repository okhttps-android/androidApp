package com.core.xmpp.utils;


import com.core.app.MyApplication;

/**
 * 获取<a>标签 Pattern.compile("(<a[^<>]*>[^<>]*</a>)")
 * 
 * 
 */
public class HtmlUtils {

	/**
	 * @canClick 是不是可以点击 如:在列表展示的时候可以不用点击
	 */
	public static CharSequence transformSpanString(String msg, boolean canClick) {
		return addSmileysToMessage(deleteHtml(msg), canClick);
	}

	private static String deleteHtml(String msg) {
		if (msg == null)
			return "";

		msg = msg.replaceAll("<a href[^>]*>", "");
		msg = msg.replaceAll("</a>", " ");
		msg = msg.replaceAll("<img[^>]*/>", "");
		msg = msg.replaceAll("\n", "\r\n");
		return msg;
	}

	public static CharSequence transform200SpanString(String msg,
			boolean canClick) {
		CharSequence sequence = transformSpanString(msg, canClick);
		if (sequence.length() > 200) {
			return sequence.subSequence(0, 200);
		} else {
			return sequence;
		}
	}

	public static CharSequence transform50SpanString(String msg,
			boolean canClick) {
		CharSequence sequence = transformSpanString(msg, canClick);
		if (sequence.length() > 50) {
			return sequence.subSequence(0, 50);
		} else {
			return sequence;
		}
	}

	public static CharSequence addSmileysToMessage(String msg, boolean canClick) {
		SmileyParser parser = SmileyParser.getInstance(MyApplication.getInstance());
		return parser.addSmileySpans(msg, canClick);
	}
}
