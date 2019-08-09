package com.uas.appme.settings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.base.SupportToolBarActivity;
import com.core.widget.view.SmoothCheckBox;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appme.R;
import com.uas.appme.settings.model.Business;

import java.util.ArrayList;


public class SelectLanguageActivity extends SupportToolBarActivity {

    private PullToRefreshListView mlist;
    private BussinessDetailAdapter mAdapter;
    private ArrayList<Business> mData = new ArrayList<Business>();
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        ViewUtils.inject(this);
        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        menu.findItem(R.id.btn_save).setTitle(getString(R.string.common_save_button));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_save) {
            if (!TextUtils.isEmpty(currentLanguage)) {
                switchLanguage(currentLanguage);
                switchLanguageAction();
            } else {
                switchLanguage("rCN");
                switchLanguageAction();
            }
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private void initView() {
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);
        mlist.setMode(PullToRefreshBase.Mode.DISABLED);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BussinessDetailAdapter.ViewHolder holder = (BussinessDetailAdapter.ViewHolder) view.getTag();
                //ToastMessage();
                final boolean flag = !mAdapter.getMdata().get(position - 1).isChecked;
                currentLanguage = holder.tv_business_name.getHint().toString();
                for (Business model : mAdapter.getMdata()) {
                    model.setIsChecked(false);
                }
                //把源数据清空 
                for (Business model : mData) {
                    model.setIsChecked(false);
                }
                mAdapter.getMdata().get(position - 1).setIsChecked(flag);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                }, 190);

            }
        });
    }

    private void initData() {
        currentLanguage = PreferenceUtils.getString(this, "language", "rCN");
        Business b = new Business();
        b.setName(getString(R.string.language_sys));
        b.setCode("sys");
        if (currentLanguage.equals("sys"))
            b.setIsChecked(true);
        mData.add(b);

        b = new Business();
        b.setCode("rCN");
        b.setName(getString(R.string.language_rCN));
        if (currentLanguage.equals("rCN"))
            b.setIsChecked(true);
        mData.add(b);
        b = new Business();
        b.setCode("rTW");
        b.setName(getString(R.string.language_rTW));
        if (currentLanguage.equals("rTW"))
            b.setIsChecked(true);
        mData.add(b);
        b = new Business();
        b.setName(getString(R.string.language_en));
        b.setCode("en");
        if (currentLanguage.equals("en"))
            b.setIsChecked(true);
        mData.add(b);
        mAdapter = new BussinessDetailAdapter(this, mData);
        mlist.setAdapter(mAdapter);
    }


    public class BussinessDetailAdapter extends BaseAdapter {
        private Context ct;
        private ArrayList<Business> mdata = new ArrayList<>();
        private LayoutInflater inflater;
        private String searchkeys;

        public ArrayList<Business> getMdata() {
            return mdata;
        }

        public BussinessDetailAdapter(Context ct, ArrayList<Business> data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mdata.size();
        }

        @Override
        public Object getItem(int position) {
            return mdata.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_business_customer, null);
                holder = new ViewHolder();
                holder.cb_left = (SmoothCheckBox) convertView.findViewById(R.id.cb_left);
                holder.tv_business_name = (TextView) convertView.findViewById(R.id.tv_business_name);
                holder.tv_business_leader = (TextView) convertView.findViewById(R.id.tv_business_leader);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.cb_left.setFocusable(false);
            holder.cb_left.setEnabled(false);
            holder.cb_left.setClickable(false);
            holder.code = mdata.get(position).getCode();
            holder.cb_left.setChecked(mdata.get(position).isChecked(), mdata.get(position).isChecked());

            if (!StringUtil.isEmpty(searchkeys)) {
                holder.tv_business_name.setText(mdata.get(position).getName());
                holder.tv_business_name.setHint(mdata.get(position).getCode());
                holder.tv_business_leader.setVisibility(View.GONE);
            } else {
                holder.tv_business_leader.setVisibility(View.GONE);
                holder.tv_business_name.setText(mdata.get(position).getName());
                holder.tv_business_name.setHint(mdata.get(position).getCode());
            }
            return convertView;
        }

        class ViewHolder {
            SmoothCheckBox cb_left;
            TextView tv_business_name;
            TextView tv_business_leader;
            String code;
        }
    }

    public void switchLanguageAction() {
        Intent it = new Intent("com.modular.main.MainActivity");
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        overridePendingTransition(0, 0);
    }

}
