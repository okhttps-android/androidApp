/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xzjmyk.pm.activity.util.im;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

import com.core.xmpp.utils.MyImageSpan;
import com.xzjmyk.pm.activity.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for annotating a CharSequence with spans to convert textual emoticons to graphical ones.
 */
public class SmileyParser {
    private static SmileyParser sInstance;

    public static SmileyParser getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SmileyParser.class) {
                if (sInstance == null) {
                    sInstance = new SmileyParser(context);
                }
            }
        }
        return sInstance;
    }

    private final Context mContext;
    private final Pattern mPattern;
    private final Pattern mHtmlPattern;

    private SmileyParser(Context context) {
        mContext = context;
        mPattern = buildPattern();
        mHtmlPattern = buildHtmlPattern();
    }

    public static class Smilies {
        public static int[][] getIds() {
            return IDS;
        }

        public static String[][] getTexts() {
            return TEXTS;
        }

        public static int textMapId(String text) {
            if (MAPS.containsKey(text)) {
                return MAPS.get(text);
            } else {
                return -1;
            }
        }


        private static final int[][] IDS = {
                {R.drawable.f_static_000, R.drawable.f_static_001, R.drawable.f_static_002, R.drawable.f_static_003, R.drawable.f_static_004,
                        R.drawable.f_static_005, R.drawable.f_static_006, R.drawable.f_static_007, R.drawable.f_static_008, R.drawable.f_static_009,
                        R.drawable.f_static_010, R.drawable.f_static_011, R.drawable.f_static_012, R.drawable.f_static_013, R.drawable.f_static_014,
                        R.drawable.f_static_015, R.drawable.f_static_016, R.drawable.f_static_017},


                {R.drawable.f_static_018, R.drawable.f_static_019, R.drawable.f_static_020, R.drawable.f_static_021, R.drawable.f_static_022,
                        R.drawable.f_static_023, R.drawable.f_static_024, R.drawable.f_static_025, R.drawable.f_static_026,
                        R.drawable.f_static_027, R.drawable.f_static_028, R.drawable.f_static_029, R.drawable.f_static_030,
                        R.drawable.f_static_031, R.drawable.f_static_032, R.drawable.f_static_033, R.drawable.f_static_034, R.drawable.f_static_035}
                , {
                R.drawable.f_static_036, R.drawable.f_static_037, R.drawable.f_static_038
        }
        };

        private static final String[][] TEXTS = {
                {"[龇牙笑]", "[顽皮]", "[流汗]", "[偷笑]", "[拜拜]",
                        "[敲头]", "[擦汗]", "[我晕]", "[鄙视]", "[大哭]",
                        "[笑哭]", "[嘘嘘]", "[酷]", "[狂笑]", "[委屈]",
                        "[便便]", "[可怜]", "[砍人]"},
                {"[可爱]", "[色]", "[害羞]", "[得意]", "[呕吐]",
                        "[微笑]", "[抱抱]", "[尴尬]", "[惊恐]", "[抽烟]",
                        "[坏笑]", "[嘴唇]", "[白眼]", "[傲慢]", "[奋斗]",
                        "[吃惊]", "[疑问]", "[睡觉]"},
                {"[邪恶]", "[哈哈]", "[吓]"}};

        private static final Map<String, Integer> MAPS = new HashMap<String, Integer>();

        static {
            // 取最小的长度，防止长度不一致出错
            int length = IDS.length > TEXTS.length ? TEXTS.length : IDS.length;
            for (int i = 0; i < length; i++) {
                int[] subIds = IDS[i];
                String[] subTexts = TEXTS[i];
                if (subIds == null || subTexts == null) {
                    continue;
                }
                int subLength = subIds.length > subTexts.length ? subTexts.length : subIds.length;
                for (int j = 0; j < subLength; j++) {
                    MAPS.put(TEXTS[i][j], IDS[i][j]);
                }
            }
        }
    }

    public static class Gifs {
        public static int[][] getIds() {
            return IDS;
        }

        public static String[][] getTexts() {
            return TEXTS;
        }

        public static int textMapId(String text) {
            if (MAPS.containsKey(text)) {
                return MAPS.get(text);
            } else {
                return -1;
            }
        }

        //动态表情
        private static final int[][] IDS = {
                {R.drawable.gif1, R.drawable.gif2, R.drawable.gif3, R.drawable.gif4, R.drawable.gif5, R.drawable.gif6,
                        R.drawable.gif7, R.drawable.gif8}, {R.drawable.gif9, R.drawable.gif10, R.drawable.gif11, R.drawable.gif12, R.drawable.gif13, R.drawable.gif14,
                R.drawable.gif15, R.drawable.gif16}
        };
        private static final String[][] TEXTS = {
                {"01.gif", "02.gif", "03.gif", "04.gif", "05.gif", "06.gif", "07.gif", "08.gif"},
                {"09.gif", "10.gif", "11.gif", "12.gif", "13.gif", "14.gif", "15.gif", "16.gif"}};
        private static final Map<String, Integer> MAPS = new HashMap<String, Integer>();

        static {
            // 取最小的长度，防止长度不一致出错
            int length = IDS.length > TEXTS.length ? TEXTS.length : IDS.length;
            for (int i = 0; i < length; i++) {
                int[] subIds = IDS[i];
                String[] subTexts = TEXTS[i];
                if (subIds == null || subTexts == null) {
                    continue;
                }
                int subLength = subIds.length > subTexts.length ? subTexts.length : subIds.length;
                for (int j = 0; j < subLength; j++) {
                    MAPS.put(TEXTS[i][j], IDS[i][j]);
                }
            }
        }
    }

    /**
     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.
     */
    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder();

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append('(');
        for (int i = 0; i < Smilies.TEXTS.length; i++) {
            for (int j = 0; j < Smilies.TEXTS[i].length; j++) {
                patternString.append(Pattern.quote(Smilies.TEXTS[i][j]));
                patternString.append('|');
            }
        }

        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    private Pattern buildHtmlPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        // StringBuilder patternString = new StringBuilder();

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        // patternString.append('(');
        // patternString.append(Pattern.quote("(<a)(\\w)+(?=</a>)"));
        // // Replace the extra '|' with a ')'
        // patternString.replace(patternString.length() - 1,
        // patternString.length(), ")");

        return Pattern.compile("(http://(\\S+?)(\\s))|(www.(\\S+?)(\\s))");
    }

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such as :-) with a graphical version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text, boolean canClick) {

        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = Smilies.textMapId(matcher.group());
            if (resId != -1) {
                builder.setSpan(new MyImageSpan(mContext, resId), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (canClick) {
            Matcher htmlmatcher = mHtmlPattern.matcher(text);
            while (htmlmatcher.find()) {
                builder.setSpan(new URLSpan(htmlmatcher.group()), htmlmatcher.start(), htmlmatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    ImageGetter imgGetter = new ImageGetter() {
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            drawable = Drawable.createFromPath(source); // 显示本地图片
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        }
    };

}
