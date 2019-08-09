package com.xzjmyk.pm.activity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * Created by 123 on 2016/4/19.
 */
public class ButtonEnabled extends Button implements View.OnClickListener {
    public ButtonEnabled(Context context) {
        super(context);
    }

    public ButtonEnabled(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public ButtonEnabled(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    @Override
    public void onClick(View view) {
       if (this.isEnabled()){

       }else{

       }
    }
    interface OnEnabledListener{
//        public void
    }
}
