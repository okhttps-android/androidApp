package com.uas.appworks.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.me.imageloader.ImageLoaderUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.CityIndustryMenuBean;

import java.util.ArrayList;
import java.util.List;

public class CityIndustryChildAdapter extends BaseAdapter {
    private List<CityIndustryMenuBean.ServesBean> mServesBeans = new ArrayList<CityIndustryMenuBean.ServesBean>();
    private Context context;
    private LayoutInflater layoutInflater;

    public CityIndustryChildAdapter(Context context, List<CityIndustryMenuBean.ServesBean> servesBeans) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        mServesBeans = servesBeans;
    }

    @Override
    public int getCount() {
        return mServesBeans.size();
    }

    @Override
    public CityIndustryMenuBean.ServesBean getItem(int position) {
        return mServesBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_city_industry_child, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((CityIndustryMenuBean.ServesBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(CityIndustryMenuBean.ServesBean object, ViewHolder holder) {
        holder.cityIndustryChildNameTv.setText(object.getSv_name());
        if (TextUtils.isEmpty(object.getSv_logourl().getMobile())) {
            holder.cityIndustryChildIconIv.setImageResource(R.drawable.defaultpic);
        } else {
            ImageLoaderUtil.getInstance().loadImage(object.getSv_logourl().getMobile(), holder.cityIndustryChildIconIv);
        }
    }

    protected class ViewHolder {
        private ImageView cityIndustryChildIconIv;
        private TextView cityIndustryChildNameTv;

        public ViewHolder(View view) {
            cityIndustryChildIconIv = (ImageView) view.findViewById(R.id.city_industry_child_icon_iv);
            cityIndustryChildNameTv = (TextView) view.findViewById(R.id.city_industry_child_name_tv);
        }
    }
}
