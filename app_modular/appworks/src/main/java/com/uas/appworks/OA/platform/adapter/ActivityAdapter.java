package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.data.TextUtil;
import com.core.app.MyApplication;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.uas.appworks.OA.platform.config.ImageConfig;
import com.uas.appworks.OA.platform.model.CharitActModel;
import com.uas.appworks.R;

import java.util.List;

/**
 * Created by Bitlike on 2017/11/8.
 */

public class ActivityAdapter extends BaseAdapter {
    private Context ct;
    private List<CharitActModel> models;
    private String newtime = "";
    private String keyWork;

    public ActivityAdapter(Context ct, List<CharitActModel> models) {
        this.ct = ct;
        this.models = models;
        newtime = DateFormatUtil.long2Str("yyyy.MM.dd HH:mm");
    }

    public void setKeyWork(String keyWork) {
        this.keyWork = keyWork;
    }

    public List<CharitActModel> getModels() {
        return models;
    }

    public void setModels(List<CharitActModel> models) {
        this.models = models;
        newtime = DateFormatUtil.long2Str("yyyy.MM.dd HH:mm");
        notifyDataSetChanged();
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHoder hoder = null;
        CharitActModel model = models.get(i);
        if (view == null) {
            hoder = new ViewHoder();
            view = LayoutInflater.from(ct).inflate(R.layout.item_activity_list, null);
            hoder.contantImg = (ImageView) view.findViewById(R.id.contantImg);
            hoder.statusTv = (TextView) view.findViewById(R.id.statusTv);
            hoder.titleTv = (TextView) view.findViewById(R.id.titleTv);
            hoder.subTv = (TextView) view.findViewById(R.id.subTv);
            view.setTag(hoder);
        } else {
            hoder = (ViewHoder) view.getTag();
        }
        final ViewHoder finalHoder=hoder;
        final String  url=model.getActImg();
        finalHoder.contantImg.setTag(url);
        ImageLoader.getInstance().displayImage(model.getActImg(), finalHoder.contantImg, ImageConfig.getCharitableImageOptions(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                finalHoder.contantImg.setImageResource(R.drawable.charitable_def_image);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                finalHoder.contantImg.setImageResource(R.drawable.charitable_def_image);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (finalHoder.contantImg.getTag()!=null&& url.equals(finalHoder.contantImg.getTag())){
                    finalHoder.contantImg.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                finalHoder.contantImg.setImageResource(R.drawable.charitable_def_image);
            }
        });
        StringBuilder str = new StringBuilder("");
        if (!ListUtils.isEmpty(model.getAwards())) {
            for (CharitActModel.AwardsBean b : model.getAwards()) {
                str.append(b.getAwardLevel() + ":" + b.getAwardName() + "\n");
            }
            str.deleteCharAt(str.length() - 1);
        }
        if (StringUtil.isEmpty(keyWork)) {
            hoder.titleTv.setText(model.getName());
            hoder.subTv.setText(str.toString());
        } else {
            TextUtil.create()
                    .addSection(model.getName())
                    .tint(keyWork, 0xef613b)
                    .showIn(hoder.titleTv);
            TextUtil.create()
                    .addSection(str.toString())
                    .tint(keyWork, 0xef613b)
                    .showIn(hoder.subTv);
        }
        String status = model.getStage();
        int colorId = R.color.indianred;
        if (!StringUtil.isEmpty(status)) {
            if (status.equals("兑奖中")) {
                colorId = R.color.reactivity;
            } else if ("已结束".equals(status)) {
                colorId = R.color.activityed;
            } else {
                colorId = R.color.activitying;
            }
        }
        hoder.statusTv.setTextColor(ct.getResources().getColor(colorId));
        hoder.statusTv.setText(status);
        return view;
    }

    private class ViewHoder {
        ImageView contantImg;
        TextView statusTv;
        TextView titleTv;
        TextView subTv;
    }


}
