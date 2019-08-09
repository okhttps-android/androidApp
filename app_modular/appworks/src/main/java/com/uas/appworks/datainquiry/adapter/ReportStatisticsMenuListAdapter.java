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
import com.uas.appworks.datainquiry.activity.ReportQueryCriteriaActivity;
import com.uas.appworks.datainquiry.activity.ReportStatisticsMoreMenuActivity;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;
import com.uas.appworks.datainquiry.bean.GridMenuReportStatisticsBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 报表统计九宫格【更多】菜单列表适配器
 */
public class ReportStatisticsMenuListAdapter extends BaseAdapter {
    private List<GridMenuReportStatisticsBean> objects = new ArrayList<GridMenuReportStatisticsBean>();
    private Context context;
    private LayoutInflater layoutInflater;
    private String mCurrentMaster;
    private String mCurrentUser;

    private int[] mGridColors = new int[]{R.color.data_inquiry_gird_menu_color1, R.color.data_inquiry_gird_menu_color2
            , R.color.data_inquiry_gird_menu_color3, R.color.data_inquiry_gird_menu_color4, R.color.data_inquiry_gird_menu_color5,
            R.color.data_inquiry_gird_menu_color6};

    public ReportStatisticsMenuListAdapter(Context context, List<GridMenuReportStatisticsBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;

        mCurrentMaster = CommonUtil.getSharedPreferences(context, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(context, "erp_username");
    }

    public List<GridMenuReportStatisticsBean> getObjects() {
        return objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public GridMenuReportStatisticsBean getItem(int position) {
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
        initializeViews((GridMenuReportStatisticsBean) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(final GridMenuReportStatisticsBean object, final ViewHolder holder, int position) {
        final int gridColor = mGridColors[(position) % mGridColors.length];
        holder.dataInquiryModulView.setBackgroundColor(context.getResources().getColor(gridColor));
        holder.dataInquiryModulTitleTv.setText(object.getModelName());

        final List<GridMenuReportStatisticsBean.ListBean> listBeans = object.getList();
        List<DataInquiryGirdItemBean> dataInquiryGirdItemBeans = new ArrayList<>();
        if (listBeans != null) {
            if (listBeans.size() > 9) {
                for (int i = 0; i < 8; i++) {
                    DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
                    dataInquiryGirdItemBean.setIconText(listBeans.get(i).getTitle());
                    dataInquiryGirdItemBean.setColor(gridColor);
                    dataInquiryGirdItemBeans.add(dataInquiryGirdItemBean);
                }
                DataInquiryGirdItemBean girdItemBean = new DataInquiryGirdItemBean();
                girdItemBean.setColor(R.color.gray);
                girdItemBean.setIconText("更多");
                dataInquiryGirdItemBeans.add(girdItemBean);
            } else {
                for (int i = 0; i < listBeans.size(); i++) {
                    DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
                    dataInquiryGirdItemBean.setIconText(listBeans.get(i).getTitle());
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
                if (position == 8 && listBeans.size() > 9) {
                    intent = new Intent();
                    intent.setClass(context, ReportStatisticsMoreMenuActivity.class);
                    intent.putExtra("model_name", object.getModelName());
                    intent.putExtra("menu_color", gridColor);
                    intent.putExtra("all_report", (Serializable) listBeans);

                    context.startActivity(intent);
                } else {
                    try {
                        String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(context,
                                mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE);
                        List<DataInquiryGirdItemBean> recentBrowse = new ArrayList<DataInquiryGirdItemBean>();
                        if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
                            recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);
                            for (int i = 0; i < recentBrowse.size(); i++) {
                                if (recentBrowse.get(i).getIconText().equals(listBeans.get(position).getTitle())) {
                                    recentBrowse.remove(i);
                                    break;
                                }
                            }
                        }
                        recentBrowse.add(0, ((DataInquiryMenuGridAdapter) holder.dataInquiryModulGv.getAdapter()).getObjects().get(position));

                        String recentJson = JSON.toJSON(recentBrowse).toString();
                        CommonUtil.setSharedPreferences(context
                                , mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE
                                , recentJson);

                    } catch (Exception e) {

                    }

                    intent = new Intent();
                    intent.setClass(context, ReportQueryCriteriaActivity.class);
                    intent.putExtra("reportinfo", listBeans.get(position));
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
