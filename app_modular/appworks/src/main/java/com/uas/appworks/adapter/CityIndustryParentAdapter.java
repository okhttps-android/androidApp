package com.uas.appworks.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.widget.view.MyGridView;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.CityIndustryMenuBean;

import java.util.ArrayList;
import java.util.List;

public class CityIndustryParentAdapter extends BaseAdapter {
    private List<CityIndustryMenuBean> mCityIndustryMenuBeans = new ArrayList<CityIndustryMenuBean>();
    private Context mContext;
    private LayoutInflater layoutInflater;
    private CityIndustryChildAdapter mCityIndustryChildAdapter;

    public CityIndustryParentAdapter(Context context, List<CityIndustryMenuBean> cityIndustryMenuBeans) {
        this.mContext = context;
        this.layoutInflater = LayoutInflater.from(context);
        mCityIndustryMenuBeans = cityIndustryMenuBeans;
    }

    @Override
    public int getCount() {
        return mCityIndustryMenuBeans.size();
    }

    @Override
    public CityIndustryMenuBean getItem(int position) {
        return mCityIndustryMenuBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_city_industry_parent, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((CityIndustryMenuBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(final CityIndustryMenuBean object, ViewHolder holder) {
        holder.cityIndustryParentTitleTv.setText(object.getSt_name());
        if (object.getServes() != null) {
            mCityIndustryChildAdapter = new CityIndustryChildAdapter(mContext, object.getServes());
            holder.cityIndustryParentGv.setAdapter(mCityIndustryChildAdapter);
        }

        holder.cityIndustryParentGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent("com.modular.work.CommonDataFormActivity");
                intent.putExtra("serve_id", object.getServes().get(position).getSv_id() + "");
                intent.putExtra("title", object.getServes().get(position).getSv_name() + "");
                mContext.startActivity(intent);
            }
        });
    }

    protected class ViewHolder {
        private TextView cityIndustryParentTitleTv;
        private MyGridView cityIndustryParentGv;

        public ViewHolder(View view) {
            cityIndustryParentTitleTv = (TextView) view.findViewById(R.id.city_industry_parent_title_tv);
            cityIndustryParentGv = (MyGridView) view.findViewById(R.id.city_industry_parent_gv);
        }
    }
}
