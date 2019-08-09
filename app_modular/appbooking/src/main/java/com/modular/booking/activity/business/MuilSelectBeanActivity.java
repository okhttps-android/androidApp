package com.modular.booking.activity.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.core.base.BaseActivity;
import com.modular.booking.R;

@Deprecated
public class MuilSelectBeanActivity extends BaseActivity{

    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muil_select_bean);
        if (getIntent()!=null)
        {
            name=getIntent().getStringExtra("name");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==R.id.app_about) {
          setResult(0x01,new Intent().putExtra("name",name));
          finish();
        }

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
}
