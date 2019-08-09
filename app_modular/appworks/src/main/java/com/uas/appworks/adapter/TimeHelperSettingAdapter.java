package com.uas.appworks.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.core.widget.view.SwitchView;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.TimeHelperSettingBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/8/28 8:57
 */
public class TimeHelperSettingAdapter extends BaseQuickAdapter<TimeHelperSettingBean, BaseViewHolder> {
    private Map<String, Integer> mSettings = new HashMap<>();

    public void setSettings(Map<String, Integer> settings) {
        mSettings = settings;
    }

    public Map<String, Integer> getSettings() {
        return mSettings;
    }

    public TimeHelperSettingAdapter(@Nullable List<TimeHelperSettingBean> data) {
        super(R.layout.item_time_helper_setting, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final TimeHelperSettingBean item) {
        helper.setText(R.id.item_time_helper_setting_tv, item.getName());

        if ("schedule".equals(item.getConfig())) {
            ((SwitchView) helper.getView(R.id.item_time_helper_setting_sv)).setChecked(true);
            ((SwitchView) helper.getView(R.id.item_time_helper_setting_sv)).setEnabled(false);
        } else {
            ((SwitchView) helper.getView(R.id.item_time_helper_setting_sv)).setEnabled(true);
            ((SwitchView) helper.getView(R.id.item_time_helper_setting_sv)).setChecked(item.getChecked() == 1);

            ((SwitchView) helper.getView(R.id.item_time_helper_setting_sv)).setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean isChecked) {
                    item.setChecked(isChecked ? 1 : 0);
                    if (mSettings != null) {
                        mSettings.put(item.getConfig(), isChecked ? 1 : 0);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}
