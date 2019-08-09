package com.core.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.core.app.MyApplication;


public class StackActivity extends DefaultResourceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityStack.getInstance().push(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityStack.getInstance().pop(this);
		if (isFinishing()) {
			if (!ActivityStack.getInstance().has()) {
				MyApplication.getInstance().destory();
			}
		}
	}
}
