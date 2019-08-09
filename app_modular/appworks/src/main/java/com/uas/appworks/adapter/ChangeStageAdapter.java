package com.uas.appworks.adapter;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.ChangeStageBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/22 15:23
 */
public class ChangeStageAdapter extends BaseQuickAdapter<ChangeStageBean, BaseViewHolder> {

    public ChangeStageAdapter(@Nullable List<ChangeStageBean> data) {
        super(R.layout.item_change_stage, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final ChangeStageBean item) {
        helper.setText(R.id.item_change_stage_caption_tv, item.getName());
        helper.setText(R.id.item_change_stage_value_tv, item.getValue() == null ? "" : item.getValue());

        EditText valueEditText = helper.getView(R.id.item_change_stage_value_tv);
        if (item.getIsRequired() == 1) {
            valueEditText.setHint("请输入(必填)");
        } else {
            valueEditText.setHint("请输入(非必填)");
        }

        valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                item.setValue(editable.toString());
            }
        });
    }
}
