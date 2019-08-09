package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.fragment.OaRegisterFragment;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OARegisterActivity extends BaseActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int ma_id;
    private String ma_code;
    private String baseUrl;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            String message = (String) msg.getData().get("result");
            if (msg.what == 0x11) {
                JSONObject json = JSON.parseObject(message);
                if (!json.containsKey("success") || !json.getBoolean("success")) return;
                if (json.containsKey("participants") && json.getJSONObject("participants").containsKey("confirmed"))
                    confirmed = json.getJSONObject("participants").getJSONArray("confirmed");
                else
                    confirmed = new JSONArray();

                if (json.containsKey("participants") && json.getJSONObject("participants").containsKey("unconfirmed"))
                    unconfirmed = json.getJSONObject("participants").getJSONArray("unconfirmed");
                else
                    unconfirmed = new JSONArray();
                updataUI();
            }
        }
    };
    private JSONArray confirmed;
    private JSONArray unconfirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oaregister);
        ma_id = getIntent().getIntExtra("id", 4864);
        ma_code = getIntent().getStringExtra("code");
        initView();
        loadNetData();
    }

    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

    }

    private void updataUI() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(adapter);//给Tabs设置适配器
    }

    //获取网络数据
    private void loadNetData() {
        progressDialog.show();
        //获取网络数据
        if (baseUrl == null) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }
        String url = baseUrl + "mobile/crm/getMeetingDetailParticipants.action";
        String em_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        final Map<String, Object> param = new HashMap<>();
        String caller = "Meetingroomapply";
        param.put("caller", caller);
        param.put("emcode", em_code);
        param.put("formCondition", "ma_id=" + ma_code);
        param.put("gridCondition", "ma_id=" + ma_code);
        param.put("ma_code", ma_code);
        param.put("sessionId", sessionId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x11, null, null, "post");
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private String[] title = {getString(R.string.signined), getString(R.string.unsignined)};

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return OaRegisterFragment.getInstance(position == 0 ? confirmed : unconfirmed);
        }

        @Override
        public int getCount() {
            return title.length;
        }
    }
}
