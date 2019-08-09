package com.common.data;

import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

/**
 * TextUtil
 * 拼接不同颜色文字的工具类--建造者模式
 * Created by wangzhengyang on 2017/2/20.
 * 使用：
 * <code>
 * //拼接模式
 * TextUtil.create()
 * .addSection("这是")
 * .addTintSection("绿色",0x00ff00)
 * .addSection("字体，这是")
 * .addTintSection("蓝色",0x0000ff)
 * .addSection("字体。")
 * .showIn(mTvPackage);
 * <p>
 * //上色模式
 * TextUtil.create()
 * .addSection("这是红色字体，这是蓝色字体，这是绿色字体。")
 * .tint("红色", 0xff0000)
 * .tint("蓝色", 0x0000ff)
 * .tint("绿色", 0x00ff00)
 * .tint("字体", Color.CYAN)
 * .tint("这是", Color.MAGENTA)
 * .showIn(mTipView);
 * </code>
 */

public final class TextUtil {

    private static final String TAG = "TextUtil";

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {

        private StringBuilder builder;

        public Builder() {
            builder = new StringBuilder();
        }

        public Builder addSection(@NonNull String section) {
            builder.append(section);
            return this;
        }


        public Builder configColor(int color) {
            builder.append("<font color='");
            builder.append(color);
            builder.append("'>");
            return this;
        }

        public Builder complete() {
            builder.append("</font>");
            return this;
        }

        public Builder addTintSection(@NonNull String section, int color) {
            return configColor(color).addSection(section).complete();
        }

        public Builder addBgSection(@NonNull String section, int color) {
            return startBg(color).addSection(section).endBg();
        }

        public Builder startBg(int color) {
            builder.append("<font color='");
            builder.append(color);
            builder.append("'>");
            return this;
        }

        public Builder endBg() {
            builder.append("</font>");
            return this;
        }

        public Builder tint(String section, int color) {
            int fromIndex = 0;
            int count = 0;
            while (true) {
                int startIndex = getSection().indexOf(section, fromIndex);
                if (startIndex == -1) break;
                int endIndex = startIndex + section.length();
                tint(section, startIndex, endIndex, color);
                String concat = section.concat("</font>");
                fromIndex = getSection().lastIndexOf(concat) + concat.length();
                ++count;
            }
            Log.i(TAG, "tint: count = " + count);
            return this;
        }

        private Builder tint(String section, int startIndex, int endIndex, int color) {
            section = create().addTintSection(section, color).getSection();
            builder.replace(startIndex, endIndex, section);
            return this;
        }

        public void showIn(TextView textView) {
            textView.setText(Html.fromHtml(builder.toString()));
            builder = null;
        }

        public String getSection() {
            return builder.toString();
        }
    }

}