package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.core.utils.CommonUtil;
import com.core.widget.view.MyGridView;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.datainquiry.activity.DataInquiryListActivity;
import com.uas.appworks.datainquiry.activity.DataInquiryMoreMenuActivity;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;
import com.uas.appworks.datainquiry.bean.GridMenuDataInquiryBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询九宫格菜单适配器
 */
public class DataInquiryMenuListAdapter extends BaseAdapter {
    private List<GridMenuDataInquiryBean> objects = new ArrayList<GridMenuDataInquiryBean>();
    private Context context;
    private LayoutInflater layoutInflater;
    private String mCurrentMaster;
    private String mCurrentUser;
    private int[] mGridColors = new int[]{R.color.data_inquiry_gird_menu_color1, R.color.data_inquiry_gird_menu_color2
            , R.color.data_inquiry_gird_menu_color3, R.color.data_inquiry_gird_menu_color4, R.color.data_inquiry_gird_menu_color5,
            R.color.data_inquiry_gird_menu_color6};

    public DataInquiryMenuListAdapter(Context context, List<GridMenuDataInquiryBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;

        mCurrentMaster = CommonUtil.getSharedPreferences(context, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(context, "erp_username");
    }

    public List<GridMenuDataInquiryBean> getObjects() {
        return objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public GridMenuDataInquiryBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry_menu, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((GridMenuDataInquiryBean) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(final GridMenuDataInquiryBean object, final ViewHolder holder, int position) {
        final int gridColor = mGridColors[(position) % mGridColors.length];
        holder.dataInquiryModulView.setBackgroundColor(context.getResources().getColor(gridColor));
        holder.dataInquiryModulTitleTv.setText(object.getModelName());

        final List<GridMenuDataInquiryBean.QueryScheme> querySchemes = object.getQuerySchemes();
        List<DataInquiryGirdItemBean> dataInquiryGirdItemBeans = new ArrayList<>();
        if (querySchemes != null) {
            if (querySchemes.size() > 9) {
                for (int i = 0; i < 8; i++) {
                    DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
                    dataInquiryGirdItemBean.setIconText(querySchemes.get(i).getScheme());
                    dataInquiryGirdItemBean.setColor(gridColor);
                    dataInquiryGirdItemBeans.add(dataInquiryGirdItemBean);
                }
                DataInquiryGirdItemBean girdItemBean = new DataInquiryGirdItemBean();
                girdItemBean.setColor(R.color.gray);
                girdItemBean.setIconText("更多");
                dataInquiryGirdItemBeans.add(girdItemBean);
            } else {
                for (int i = 0; i < querySchemes.size(); i++) {
                    DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
                    dataInquiryGirdItemBean.setIconText(querySchemes.get(i).getScheme());
                    dataInquiryGirdItemBean.setColor(gridColor);
                    dataInquiryGirdItemBeans.add(dataInquiryGirdItemBean);
                }
            }
            DataInquiryMenuGridAdapter dataInquiryMenuGridAdapter = new DataInquiryMenuGridAdapter(context, dataInquiryGirdItemBeans);
            holder.dataInquiryModulGv.setAdapter(dataInquiryMenuGridAdapter);
        }

        holder.dataInquiryModulGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if (position == 8 && querySchemes.size() > 9) {
                    intent = new Intent();
                    intent.setClass(context, DataInquiryMoreMenuActivity.class);
                    intent.putExtra("model_name", object.getModelName());
                    intent.putExtra("menu_color", gridColor);
                    intent.putExtra("all_report", (Serializable) querySchemes);

                    context.startActivity(intent);
                } else {
                    try {
                        String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(context,
                                mCurrentUser + mCurrentMaster + Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE);
                        List<DataInquiryGirdItemBean> recentBrowse = new ArrayList<DataInquiryGirdItemBean>();
                        if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
                            recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);
                            for (int i = 0; i < recentBrowse.size(); i++) {
                                if (recentBrowse.get(i).getIconText().equals(querySchemes.get(position).getScheme())) {
                                    recentBrowse.remove(i);
                                    break;
                                }
                            }
                        }
                        recentBrowse.add(0, ((DataInquiryMenuGridAdapter) holder.dataInquiryModulGv.getAdapter()).getObjects().get(position));

                        String recentJson = JSON.toJSON(recentBrowse).toString();
                        CommonUtil.setSharedPreferences(context
                                , mCurrentUser + mCurrentMaster + Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE
                                , recentJson);

                    } catch (Exception e) {

                    }

                    intent = new Intent();
                    intent.setClass(context, DataInquiryListActivity.class);
                    intent.putExtra("scheme", querySchemes.get(position));
                    context.startActivity(intent);
                }
            }
        });

    }

    protected class ViewHolder {
        private View dataInquiryModulView;
        private TextView dataInquiryModulTitleTv;
        private MyGridView dataInquiryModulGv;

        public ViewHolder(View view) {
            dataInquiryModulView = (View) view.findViewById(R.id.data_inquiry_modul_view);
            dataInquiryModulTitleTv = (TextView) view.findViewById(R.id.data_inquiry_modul_title_tv);
            dataInquiryModulGv = (MyGridView) view.findViewById(R.id.data_inquiry_modul_gv);
        }
    }
}
