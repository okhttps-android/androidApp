package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.fragment.ChartListFragment;
import com.xzjmyk.pm.activity.ui.erp.fragment.QueryGridFragment;
import com.xzjmyk.pm.activity.ui.erp.model.ImageInfo;
import com.core.widget.NScrollerGridView;

import java.util.ArrayList;

/**
  * @desc:考勤统计
  * @author：Arison on 2016/10/20
  */
public class QueryActivity extends BaseActivity {

    private String TAG = "HomeFragment";
    @SuppressWarnings("unused")
    private Context ct;
    private FragmentActivity activity;
    @ViewInject(R.id.gv_home_display)
    private NScrollerGridView gv_disGridView;
    private ArrayList<ImageInfo> data = new ArrayList<ImageInfo>(); // 菜单数据
    @SuppressWarnings("unused")
    private final static int SUCCESS_LOGINAPP = 0;
    @ViewInject(R.id.view_empty)
    private RelativeLayout emptyLayout;
    @ViewInject(R.id.buttonEmpty)
    private Button tryBut;
    @ViewInject(R.id.textViewMessage)
    private TextView msg;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.fragment_tab_simple);
        ViewUtils.inject(this);
        setTitle("查询");
        activity = this;
        initView();
    }

    public void initView() {
        fragmentManager = activity.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        QueryGridFragment queryGridFragment = new QueryGridFragment();

        fragmentTransaction.replace(R.id.fm_content, queryGridFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_button, menu);
        return true;
    }

    int type = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_find) {
            if (type == 1) {

            } else {
                type = 1;
                QueryGridFragment queryGridFragment = new QueryGridFragment();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fm_content, queryGridFragment);
                setTitle("查询");
                fragmentTransaction.commit();
            }

        }
        if (item.getItemId() == R.id.button_chart) {
            if (type == 2) {

            } else {
                type = 2;
                ChartListFragment queryGridFragment = new ChartListFragment();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fm_content, queryGridFragment);
                setTitle("统计");
                fragmentTransaction.commit();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
