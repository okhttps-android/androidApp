package com.uas.appme.pedometer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.uas.appme.R;
import com.uas.appme.pedometer.bean.StepsRankingBean;
import com.uas.appme.pedometer.utils.StepUtils;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by FANGlh on 2017/9/19.
 * function:
 */

public class UUAttentionAdapter extends BaseAdapter{
    private Context mContext;
    private StepsRankingBean model;

    public StepsRankingBean getModel() {
        return model;
    }

    public void setModel(StepsRankingBean model) {
        this.model = model;
    }
    public UUAttentionAdapter(Context mContext){this.mContext = mContext;}
    @Override
    public int getCount() {
        return ListUtils.isEmpty(model.getAttrank()) ? 0 : model.getAttrank().size();
    }

    @Override
    public Object getItem(int position) {
        return model.getAttrank().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView =  View.inflate(mContext, R.layout.item_uuattention,null);
            viewHolder.ranking_tv = (TextView) convertView.findViewById(R.id.ranking_tv);
            viewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            viewHolder.steps_tv = (TextView) convertView.findViewById(R.id.steps_tv);
            viewHolder.prise_tv = (TextView) convertView.findViewById(R.id.prise_tv);
            viewHolder.photo_img = (CircleImageView) convertView.findViewById(R.id.photo_img);
            viewHolder.prise_im = (ImageView) convertView.findViewById(R.id.prise_im);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name_tv.setText(model.getAttrank().get(position).getAs_username());
        viewHolder.steps_tv.setText(model.getAttrank().get(position).getAs_uusteps());
        viewHolder.prise_tv.setText(model.getAttrank().get(position).getAs_prise());

        AvatarHelper.getInstance().display(CommonUtil.getNumByString(model.getAttrank().get(position).getAs_userid()) + "", viewHolder.photo_img, true, false);//显示圆角图片
        viewHolder.ranking_tv.setText("第"+model.getAttrank().get(position).getRank()+"名");
        if (model.getAttrank().get(position).getPrised() ||
                (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAttrank().get(position).getAs_userid())  //自己获赞数 > 0
                        && CommonUtil.getNumByString(model.getAttrank().get(position).getAs_prise()) > 0))
            viewHolder.prise_im.setImageResource(R.drawable.praised);
        else
            viewHolder.prise_im.setImageResource(R.drawable.praise);

        viewHolder.prise_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    StepUtils.doStepPriseHttp(model,2,position);
            }
        });
        return convertView;
    }
    class ViewHolder{
        TextView ranking_tv;
        CircleImageView photo_img;
        TextView name_tv;
        TextView steps_tv;
        TextView prise_tv;
        ImageView prise_im;
    }
}
