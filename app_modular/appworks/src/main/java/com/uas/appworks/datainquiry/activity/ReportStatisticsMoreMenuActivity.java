package com.uas.appworks.datainquiry.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.datainquiry.adapter.DataInquiryMoreMenuAdapter;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;
import com.uas.appworks.datainquiry.bean.GridMenuReportStatisticsBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 报表统计九宫格更多菜单页面
 */
public class ReportStatisticsMoreMenuActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView mMenuListView;
    private List<String> mMenuStrings;
    private DataInquiryMoreMenuAdapter mDataInquiryMoreMenuAdapter;
    private int mColor;
    private String mModelName;
    private List<GridMenuReportStatisticsBean.ListBean> mListBeans;
    private String mCurrentMaster;
    private String mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_inquiry_more_menu);

        mMenuListView = (ListView) findViewById(R.id.data_inquiry_more_menu_lv);
        mMenuStrings = new ArrayList<>();

        mCurrentMaster = CommonUtil.getSharedPreferences(this, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(this, "erp_username");

        Intent intent = getIntent();
        if (intent != null) {
            mColor = intent.getIntExtra("menu_color", -1);
            mModelName = intent.getStringExtra("model_name");
            mListBeans = (List<GridMenuReportStatisticsBean.ListBean>) intent.getSerializableExtra("all_report");

            setTitle(mModelName);

            if (mListBeans != null) {
                for (int i = 0; i < mListBeans.size(); i++) {
                    mMenuStrings.add(mListBeans.get(i).getTitle());
                }
                mDataInquiryMoreMenuAdapter = new DataInquiryMoreMenuAdapter(this, mMenuStrings);
                mDataInquiryMoreMenuAdapter.setColor(mColor);

                mMenuListView.setAdapter(mDataInquiryMoreMenuAdapter);
            }
        }

        mMenuListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
        dataInquiryGirdItemBean.setColor(mColor);
        dataInquiryGirdItemBean.setIconText(mListBeans.get(position).getTitle());

        String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(ReportStatisticsMoreMenuActivity.this,
                mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE);
        List<DataInquiryGirdItemBean> recentBrowse = new ArrayList<DataInquiryGirdItemBean>();
        if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
            try {
                recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);

                for (int i = 0; i < recentBrowse.size(); i++) {
                    if (mListBeans.get(position).getTitle() != null && mListBeans.get(position).getTitle().equals(recentBrowse.get(i).getIconText())) {
                        recentBrowse.remove(i);
                    }
                }
            } catch (Exception e) {

            }
        }

        recentBrowse.add(0, dataInquiryGirdItemBean);

        String recentJson = JSON.toJSON(recentBrowse).toString();
        CommonUtil.setSharedPreferences(ReportStatisticsMoreMenuActivity.this
                , mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE
                , recentJson);

        Intent intent = new Intent();
        intent.setClass(this, ReportQueryCriteriaActivity.class);
        intent.putExtra("reportinfo", mListBeans.get(position));
        startActivity(intent);
    }
}
