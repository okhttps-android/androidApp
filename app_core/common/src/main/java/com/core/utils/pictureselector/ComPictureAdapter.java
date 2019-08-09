package com.core.utils.pictureselector;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.app.R;
import com.core.widget.SquareCenterImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by FANGlh on 2017/7/4.
 * function: 9张图片公用适配器
 */

public class ComPictureAdapter extends BaseAdapter {
    private ArrayList<String> mPhotoList;
    private Context mContext;
    private int MaxSiz; // 设置图片可选最大张数

    public int getMaxSiz() {
        return MaxSiz;
    }

    public void setMaxSiz(int maxSiz) {
        MaxSiz = maxSiz;
    }

    public ComPictureAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public ArrayList<String> getmPhotoList() {
        return mPhotoList;
    }

    public void setmPhotoList(ArrayList<String> mPhotoList) {
        this.mPhotoList = mPhotoList;
    }

    @Override
    public int getCount() {
        if (mPhotoList.size() >= getMaxSiz()) {
            return getMaxSiz() <= 0 ? 9 : getMaxSiz();
        }
        return mPhotoList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mPhotoList.size() == 0) {
            return 1;// View Type 1代表添加更多的视图
        } else if (mPhotoList.size() < getMaxSiz()) {
            if (position < mPhotoList.size()) {
                return 0;// View Type 0代表普通的ImageView视图
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == 0) {// 普通的视图
            SquareCenterImageView imageView = new SquareCenterImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String url = mPhotoList.get(position);
            if (url == null) {
                url = "";
            }
            ImageLoader.getInstance().displayImage(Uri.fromFile(new File(url)).toString(), imageView);
            return imageView;
        } else {
//            View view = View.inflate(mContext, R.layout.layout_circle_add_more_item, null);
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_circle_add_more_item,
                    parent, false);
            ImageView iconImageView = (ImageView) view.findViewById(R.id.icon_image_view);
            TextView voiceTextTv = (TextView) view.findViewById(R.id.text_tv);
            iconImageView.setBackgroundResource(R.drawable.add_picture);
            voiceTextTv.setText(R.string.qzone_add_picture);
            return view;
        }
    }

}