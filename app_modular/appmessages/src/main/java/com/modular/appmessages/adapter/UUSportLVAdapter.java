package com.modular.appmessages.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.core.model.StepRankingFirstBean;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.modular.appmessages.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by FANGlh on 2017/9/28.
 * function:
 */

public class UUSportLVAdapter extends BaseAdapter{
    private Context mContext;
    List<StepRankingFirstBean> models;

    public List<StepRankingFirstBean> getModels() {
        return models;
    }

    public void setModels(List<StepRankingFirstBean> models) {
        this.models = models;
    }

    public UUSportLVAdapter(Context mContext){this.mContext = mContext;}

    @Override
    public int getCount() {return ListUtils.isEmpty(models) ? 0 : models.size();}
    @Override
    public Object getItem(int position) {return ListUtils.isEmpty(models) ? null : models.get(position);}
    @Override
    public long getItemId(int position) {return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView =  View.inflate(mContext, R.layout.item_dayrank,null);
            viewHolder.mTimeTV = (TextView) convertView.findViewById(R.id.timeTV);
            viewHolder.mRankInfoRl = (RelativeLayout) convertView.findViewById(R.id.rank_info_rl);
            viewHolder.mRankTv = (TextView) convertView.findViewById(R.id.rank_tv);
            viewHolder.mRankTvStr = (TextView) convertView.findViewById(R.id.rank_tv_str);
            viewHolder.mStepsTv = (TextView) convertView.findViewById(R.id.steps_tv);
            viewHolder.mRstepsTvStr = (TextView) convertView.findViewById(R.id.rsteps_tv_str);
            viewHolder.mLine = (View) convertView.findViewById(R.id.line);
            viewHolder.mChampionIm = (CircleImageView) convertView.findViewById(R.id.champion_im);
            viewHolder.mChampionDescTv = (TextView) convertView.findViewById(R.id.champion_desc_tv);
            viewHolder.mrankLl = (LinearLayout) convertView.findViewById(R.id.rank_ll);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (CommonUtil.getNumByString(models.get(getCount() - position -1).getMy_rank()) <10){
            viewHolder.mRankTv.setTextColor(mContext.getResources().getColor(R.color.approvaling));
            viewHolder.mStepsTv.setTextColor(mContext.getResources().getColor(R.color.approvaling));
        }else {
            viewHolder.mRankTv.setTextColor(mContext.getResources().getColor(R.color.green));
            viewHolder.mStepsTv.setTextColor(mContext.getResources().getColor(R.color.green));
        }

        viewHolder.mTimeTV.setText(models.get(getCount() - position -1).getDate());
        viewHolder.mRankTv.setText(models.get(getCount() - position -1).getMy_rank());
        viewHolder.mStepsTv.setText(models.get(getCount() - position -1).getMy_steps());
        viewHolder.mChampionDescTv.setText(models.get(getCount() - position-1).getF_name() + "夺得了"
                + models.get(getCount() - position -1).getDate() +"排行榜冠军");
        AvatarHelper.getInstance().displayAvatar(models.get(getCount() - position -1).getF_userid(), viewHolder.mChampionIm, true);

        return convertView;
    }

    class ViewHolder{
        TextView mTimeTV;
        TextView mRankTv;
        TextView mRankTvStr;
        TextView mStepsTv;
        TextView mRstepsTvStr;
        TextView mChampionDescTv;
        View mLine;
        RelativeLayout mRankInfoRl;
        CircleImageView mChampionIm;
        LinearLayout mrankLl;
    }
}
