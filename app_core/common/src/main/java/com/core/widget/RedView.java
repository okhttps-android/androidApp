package com.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.app.R;

/**
 * Created by Bitliker on 2017/9/26.
 */

public class RedView extends RelativeLayout {

	private TextView nameTv;
	private TextView numTv;
	private ImageView redImg;

	public RedView(Context context) {
		this(context, null);
	}

	public RedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.inclue_comon_red, this);
		nameTv = (TextView) findViewById(R.id.nameTv);
		numTv = (TextView) findViewById(R.id.numTv);
		redImg = (ImageView) findViewById(R.id.redImg);
		TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RedView);
		try {
			String text = ta.getString(R.styleable.RedView_text);
			setName(text);
			int num = ta.getInteger(R.styleable.RedView_image, 0);
			if (num > 0) {
				numTv.setText(String.valueOf(num));
			} else {
				numTv.setText("");
			}
			boolean showImage = ta.getBoolean(R.styleable.RedView_showImage, true);
			if (showImage) {
				int image = ta.getResourceId(R.styleable.RedView_image, R.drawable.kaoqintongji);
				redImg.setImageResource(image);
			} else {
				redImg.setVisibility(GONE);
			}
		} finally {
			ta.recycle();
		}
	}

	public void setName(String message) {
		if (StringUtil.isEmpty(message)) {
			nameTv.setText("");
		}else{
			nameTv.setText(message);
		}
	}
}
