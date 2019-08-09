package com.uas.appworks.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.SimpleView;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.uas.appworks.R;
import com.uas.appworks.adapter.TimeHelperSettingAdapter;
import com.uas.appworks.model.bean.TimeHelperSettingBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author RaoMeng
 * @describe 时间助手设置页面
 * @date 2018/8/27 17:51
 */
public class TimeHelperSettingActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView {
    private static final int FLAG_LOADCONFIG = 0x21;
    private static final int FLAG_SAVECONFIG = 0x22;

    private RecyclerView mRecyclerView;
    private List<TimeHelperSettingBean> mTimeHelperSettingBeans;
    private TimeHelperSettingAdapter mTimeHelperSettingAdapter;
    private Map<String, Integer> mSettings = new HashMap<>();

    @Override
    protected int getLayout() {
        return R.layout.activity_time_helper_setting;
    }

    @Override
    protected void initView() {
        setTitle(getString(R.string.title_time_helper_setting));

        mRecyclerView = $(R.id.time_helper_setting_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        mTimeHelperSettingBeans = new ArrayList<>();
        mTimeHelperSettingAdapter = new TimeHelperSettingAdapter(mTimeHelperSettingBeans);
        mRecyclerView.setAdapter(mTimeHelperSettingAdapter);
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        mPresenter.httpRequest(this, "https://mobile.ubtob.com:8443/",
                new HttpParams.Builder()
                        .url("schedule/config/loadConfig")
                        .method(Method.GET)
                        .flag(FLAG_LOADCONFIG)
                        .addParam("imid", MyApplication.getInstance().getLoginUserId())
                        .build());
    }

    @Override
    public void requestSuccess(int what, Object object) {
        try {
            switch (what) {
                case FLAG_LOADCONFIG:
                    analysisLoadConfig(object);
                    break;
                case FLAG_SAVECONFIG:
                    Toast.makeText(ct, "保存成功", Toast.LENGTH_LONG).show();
                    setResult(0x11);
                    onBackPressed();
                    break;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        switch (what) {
            case FLAG_LOADCONFIG:
            case FLAG_SAVECONFIG:
                toast(errorMsg);
                break;
        }
    }

    private void analysisLoadConfig(Object object) {
        if (object == null) {
            toast("用户配置获取失败");
            return;
        }
        String result = object.toString();
        Log.d("timehelperconfig", result);
        if (!JSONUtil.validate(result)) {
            toast("用户配置获取失败");
            return;
        }
        JSONObject resultObject = JSON.parseObject(result);
        JSONObject dataObject = resultObject.getJSONObject("data");
        if (dataObject == null) {
            toast("用户配置获取失败");
            return;
        }
        Set<String> keys = dataObject.keySet();
        if (keys == null) {
            toast("用户配置获取失败");
            return;
        }
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            TimeHelperSettingBean timeHelperSettingBean = new TimeHelperSettingBean();
            String next = iterator.next();
            switch (next) {
                case "schedule":
                    timeHelperSettingBean.setName("日程");
                    break;
                case "meeting":
                    timeHelperSettingBean.setName("会议");
                    break;
                case "book":
                    timeHelperSettingBean.setName("预约");
                    break;
                case "outSign":
                    timeHelperSettingBean.setName("外勤");
                    break;
                case "outWork":
                    timeHelperSettingBean.setName("出差");
                    break;
                default:
                    timeHelperSettingBean.setName(null);
                    break;
            }
            if (timeHelperSettingBean.getName() != null) {
                int config = JSONUtil.getInt(dataObject, next);
                timeHelperSettingBean.setConfig(next);
                timeHelperSettingBean.setChecked(config);

                mSettings.put(next, config);
                mTimeHelperSettingBeans.add(timeHelperSettingBean);
            }

        }
        mTimeHelperSettingAdapter.setSettings(mSettings);
        mTimeHelperSettingAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_time_helper_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_time_helper_setting_save) {

            mPresenter.httpRequest(this, "https://mobile.ubtob.com:8443/",
                    new HttpParams.Builder()
                            .url("schedule/config/saveConfig")
                            .method(Method.POST)
                            .flag(FLAG_SAVECONFIG)
                            .addParam("imid", MyApplication.getInstance().getLoginUserId())
                            .addParam("schedule", mTimeHelperSettingAdapter.getSettings().get("schedule"))
                            .addParam("meeting", mTimeHelperSettingAdapter.getSettings().get("meeting"))
                            .addParam("book", mTimeHelperSettingAdapter.getSettings().get("book"))
                            .addParam("outSign", mTimeHelperSettingAdapter.getSettings().get("outSign"))
                            .addParam("outWork", mTimeHelperSettingAdapter.getSettings().get("outWork"))
                            .build());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
