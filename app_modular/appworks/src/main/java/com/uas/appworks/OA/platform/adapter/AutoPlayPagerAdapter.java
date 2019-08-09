package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.common.LogUtil;
import com.modular.apputils.widget.AutoPagerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appworks.OA.platform.config.ImageConfig;
import com.uas.appworks.OA.platform.model.Carousel;

import java.util.List;

public class AutoPlayPagerAdapter extends AutoPagerAdapter implements View.OnClickListener {

	private Context ct;
	private List<Carousel> list;
	private ItemClickListener itemClickListener;


	public void setList(List<Carousel> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public AutoPlayPagerAdapter(Context ct, List<Carousel> list, ItemClickListener itemClickListener) {
		this.ct = ct;
		this.list = list;
		this.itemClickListener = itemClickListener;
	}

	@Override
	public View getView(LayoutInflater layoutInflater, int position) {
		ImageView imageView = new ImageView(ct.getApplicationContext());
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setLayoutParams(params);
		Carousel c = list.get(getPositionForIndicator(position));
		ImageLoader.getInstance().displayImage(c.getImageUrl(), imageView, ImageConfig.getCharitableImageOptions());
		imageView.setTag(c);
		imageView.setOnClickListener(this);
		return imageView;
	}

	@Override
	public int getDataCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public void onClick(View view) {
		Carousel c = (Carousel) view.getTag();
		if (c!=null){
			itemClickListener.clickItem( c);
		}else{
		}
	}


	public interface ItemClickListener {
		void clickItem(Carousel carousel);
	}
}