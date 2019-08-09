package com.uas.appme.other.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.core.base.BaseActivity;
import com.core.widget.crouton.Crouton;
import com.lidroid.xutils.ViewUtils;
import com.uas.appme.R;

import java.util.Timer;
import java.util.TimerTask;

/**
  * @desc:更新单个编辑框的值
  * @author：Arison on 2016/9/27
  */
public class UpdateSingleTextActivity extends BaseActivity {
    private EditText mNickEditText;
    private ImageView mCancelImageView;

    private String mNickname;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_single_text);
        ViewUtils.inject(this);
        mNickEditText = (EditText) findViewById(R.id.update_nickname_et);
        mCancelImageView = (ImageView) findViewById(R.id.update_nickname_calcel_iv);

        mNickEditText.setHint(getString(R.string.common_input2)+getString(R.string.user_nickname));
        mNickEditText.setFocusable(true);
        mNickEditText.setFocusableInTouchMode(true);
        mNickEditText.requestFocus();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) mNickEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mNickEditText,0);
            }
        },200);
        setTitle(getString(R.string.user_nickname));
        intent = getIntent();
        if (intent != null){
            mNickname = intent.getStringExtra("nickname");
        }
        if (mNickname != null){
            mNickEditText.setText(mNickname);
            mNickEditText.setSelection(mNickname.length());
        }
        mCancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNickEditText.setText(null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_apply_subscribe,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.subscribe_apply){
            if (mNickEditText.getText().toString().trim() != null){
                intent.putExtra("newnickname",mNickEditText.getText().toString().trim());
                setResult(11, intent);
                finish();
            }else {
                Crouton.makeText(UpdateSingleTextActivity.this,R.string.common_input, 0xffff4444,1500).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
