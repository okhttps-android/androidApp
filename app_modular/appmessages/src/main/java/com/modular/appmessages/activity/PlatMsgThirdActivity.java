package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.modular.appmessages.R;

/**
 * Created by FANGlh on 2017/4/28.
 * function:
 */
public class PlatMsgThirdActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private TextView plat_msg_third_tv;

    private void initView() {
        setContentView(R.layout.plat_msg_third);
        plat_msg_third_tv = (TextView) findViewById(R.id.plat_msg_third_tv);
    }

    private void initData() {
        Intent intent = getIntent();
        String msg_title = intent.getStringExtra("title");
        String msg_context = intent.getStringExtra("msg_context");

        Log.i("msg_title,msg_context", msg_title + "," + msg_context);
        if (!StringUtil.isEmpty(msg_title))
            setTitle(msg_title + getString(R.string.doc_detail));
        plat_msg_third_tv.setText(Html.fromHtml(msg_context) + "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra("read_status", true);
            setResult(22, intent);
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("read_status", true);
        setResult(22, intent);
        finish();
        super.onBackPressed();
    }
}
