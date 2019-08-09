package com.uas.appworks.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.core.app.Constants;
import com.modular.apputils.adapter.EasyBaseAdapter;
import com.modular.apputils.model.EasyBaseModel;
import com.modular.apputils.widget.compactcalender.Lunar;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.activity.CustomerDetails3_0Activity;

import java.util.Date;
import java.util.List;

public class CustomerCareListAdapter extends EasyBaseAdapter {
    private int type;//1 遗忘客户、2、客户关怀

    public CustomerCareListAdapter(Context ct, List list, int type) {
        super(ct, list);
        this.type = type;
    }

    private void setCompoundDrawables(TextView tv){
//        Drawable drawable = ct.getResources().getDrawable(R.drawable.ic_customer_birthday);
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//必须设置图片大小，否则不显示
//        tv.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    public View bindView(View view, int position, EasyBaseModel model) {
        ViewHolder mViewHolder = null;
        if (view.getTag() == null) {
            mViewHolder = new ViewHolder();
            mViewHolder.nameTv = (TextView) view.findViewById(R.id.nameTv);
            mViewHolder.subTitleTv = (TextView) view.findViewById(R.id.subTitleTv);
            mViewHolder.tagTv = (TextView) view.findViewById(R.id.tagTv);
            if (type == 1) {
                mViewHolder.tagTv.setTextColor(0xffED0000);
                mViewHolder.subTitleTv.setText(model.getSubTitle());
                mViewHolder.tagTv.setText(model.getIconUrl());
            } else if (type == 2) {
                mViewHolder.tagTv.setTextColor(0xffFD8B1E);
                setCompoundDrawables(mViewHolder.subTitleTv);
                long time= DateFormatUtil.str2Long(model.getSubTitle(),DateFormatUtil.YMD_HMS);
                StringBuilder subTitleBuilder=new StringBuilder(DateFormatUtil.long2Str(time,DateFormatUtil.MD));
                subTitleBuilder.append(" | 农历  "+new Lunar(new Date(time)).toString());
                mViewHolder.subTitleTv.setText(subTitleBuilder.toString());
                String tag="今";
                int day=DateFormatUtil.differentDays(System.currentTimeMillis(),time);
                if (day!=0){
                    tag=String.valueOf(day)+"天";
                }
                mViewHolder.tagTv.setText(tag);
            }
            view.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) view.getTag();
        }
        mViewHolder.nameTv.setText(model.getTitle());
        view.setTag(R.id.tag_key,model);
        view.setOnClickListener(mOnClickListener);
        return view;
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag(R.id.tag_key) != null && view.getTag(R.id.tag_key) instanceof EasyBaseModel) {
                EasyBaseModel model= (EasyBaseModel) view.getTag(R.id.tag_key);
                ct.startActivity(new Intent(ct, CustomerDetails3_0Activity.class)
                        .putExtra(Constants.Intents.CALLER, "Customer!Base")
                        .putExtra(Constants.Intents.TITLE, "正式客户")
                        .putExtra(Constants.Intents.MY_DOIT, true)
                        .putExtra(Constants.Intents.MY_DOIT, true)
                        .putExtra(Constants.Intents.ID, model.getId()));
            }
        }
    };

    class ViewHolder {
        private TextView nameTv;
        private TextView subTitleTv;
        private TextView tagTv;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_customer_manage_customer_list;
    }
}