package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.data.TextUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.uas.appworks.OA.platform.config.ImageConfig;
import com.uas.appworks.OA.platform.model.CharitModel;
import com.uas.appworks.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Bitlike on 2017/11/7.
 */

public class CharitableAdapter extends BaseAdapter {
    private DecimalFormat df = new DecimalFormat(".##");
    private Context ct;
    private List<CharitModel> models;
    private MyClickListener onClickListener;
    private String keyWork;

    public CharitModel getModels(int i) {
        if (ListUtils.getSize(models) > i) {
            return models.get(i);
        } else {
            return null;
        }
    }

    public void setKeyWork(String keyWork) {
        this.keyWork = keyWork;
    }

    public void setModels(List<CharitModel> models) {
        this.models = models;
        notifyDataSetChanged();
    }


    public CharitableAdapter(Context ct, List<CharitModel> models, MyClickListener onClickListener) {
        this.ct = ct;
        this.models = models;
        this.onClickListener = onClickListener;
    }

    @Override
    public int getCount() {
        return ListUtils.getSize(models);
    }

    @Override
    public Object getItem(int i) {
        return models.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        CharitModel model = models.get(i);
        if (view == null) {
            holder = new ViewHolder();
            view = getLayoutInflater().inflate(R.layout.item_charitable_project, null);
            holder.contantImg = (ImageView) view.findViewById(R.id.contantImg);
            holder.titleTv = (TextView) view.findViewById(R.id.titleTv);
            holder.subTv = (TextView) view.findViewById(R.id.subTv);
            holder.targetTv = (TextView) view.findViewById(R.id.targetTv);
            holder.typeTv = (TextView) view.findViewById(R.id.typeTv);
            holder.giftBtn = (TextView) view.findViewById(R.id.giftBtn);
            holder.progressSb = view.findViewById(R.id.progressSb);
            holder.defTv = view.findViewById(R.id.defTv);
            holder.progressSb.setClickable(false);
            holder.progressSb.setEnabled(false);
            holder.progressSb.setFocusable(false);
            holder.progressSb.setPadding(0,0,0,0);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.targetTv.setText(model.getTarget() + "元");
        holder.typeTv.setText(model.getArea());
        holder.giftBtn.setTag(i);
        if (model.isEnded()) {
            holder.giftBtn.setBackgroundResource(R.color.hintColor);
        } else {
            holder.giftBtn.setBackgroundResource(R.color.indianred);
            holder.giftBtn.setOnClickListener(onClickListener);
        }
        if (StringUtil.isEmpty(keyWork)) {
            holder.titleTv.setText(model.getName());
            holder.subTv.setText(model.getProSummary());
        } else {
            TextUtil.create()
                    .addSection(model.getName())
                    .tint(keyWork, 0xef613b)
                    .showIn(holder.titleTv);
            TextUtil.create()
                    .addSection(model.getProSummary())
                    .tint(keyWork, 0xef613b)
                    .showIn(holder.subTv);
        }

        try {
            float mTarget = Float.valueOf(model.getTarget());
            float mTotalAmount = Float.valueOf(model.getTotalAmount());
            holder.progressSb.setMax((int) mTarget);
            holder.progressSb.setProgress((int) mTotalAmount);
            String defStr = "";
            if (mTotalAmount >= mTarget) {
                defStr = "筹款已完成";
            } else {
                float defAmount = mTarget - mTotalAmount;

                if (defAmount > 10000) {
                    float defRMB = defAmount / 10000;
                    defStr = "还差" + df.format(defRMB) + "万元";
                } else {
                    defStr = "还差" + df.format(defAmount) + "元";
                }
            }
            holder.defTv.setText(defStr);
        } catch (Exception e) {

        }


        final ViewHolder finalHoder = holder;
        final String url = model.getListImageUrl();
        finalHoder.contantImg.setTag(url);
        ImageLoader.getInstance().displayImage(model.getListImageUrl(), finalHoder.contantImg, ImageConfig.getCharitableImageOptions(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                LogUtil.i("onLoadingStarted view instanceof ImageView");
                finalHoder.contantImg.setImageResource(R.drawable.charitable_def_image);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                LogUtil.i("onLoadingFailed view instanceof ImageView");
                finalHoder.contantImg.setImageResource(R.drawable.charitable_def_image);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                LogUtil.i("onLoadingComplete view instanceof ImageView");
                if (finalHoder.contantImg.getTag() != null && url.equals(finalHoder.contantImg.getTag())) {
                    finalHoder.contantImg.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                LogUtil.i("onLoadingCancelled view instanceof ImageView");
                finalHoder.contantImg.setImageResource(R.drawable.charitable_def_image);
            }
        });

        return view;
    }

    private LayoutInflater inflater;

    private LayoutInflater getLayoutInflater() {
        return inflater == null ? inflater = LayoutInflater.from(ct) : inflater;

    }

    private class ViewHolder {
        ImageView contantImg;
        TextView titleTv;
        TextView subTv;
        TextView targetTv;
        TextView typeTv;
        TextView giftBtn;
        TextView defTv;
        SeekBar progressSb;

    }


    /**
     * 用于回调的抽象类
     *
     * @author Ivan Xu
     * 2014-11-26
     */
    public static abstract class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            myOnClick((Integer) v.getTag(), v);
        }

        public abstract void myOnClick(int position, View v);
    }
}
