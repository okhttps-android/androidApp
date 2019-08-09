package com.uas.appworks.datainquiry.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.CircleTextView;
import com.core.widget.ClearEditText;
import com.core.widget.EmptyLayout;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;
import com.uas.appworks.datainquiry.bean.GridMenuReportStatisticsBean;
import com.uas.appworks.datainquiry.bean.ReportQuerySearchBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/14.
 */

public class ReportQuerySearchActivity extends BaseActivity {
    private ClearEditText mSearchEditText;
    private ImageView mVoiceImageView;
    private ListView mResultListView;
    private ReportQuerySearchAdapter mReportQuerySearchAdapter;
    private List<ReportQuerySearchBean> mQuerySearchBeanList;
    private String mCurrentMaster;
    private String mCurrentUser;
    private EmptyLayout mEmptyLayout;

    private int[] mColors = new int[]{R.color.data_inquiry_gird_menu_color1, R.color.data_inquiry_gird_menu_color2
            , R.color.data_inquiry_gird_menu_color3, R.color.data_inquiry_gird_menu_color4, R.color.data_inquiry_gird_menu_color5,
            R.color.data_inquiry_gird_menu_color6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_query_search);
        setTitle("报表统计");

        initViews();
        initEvents();
        initDatas();
    }

    private void initViews() {
        mSearchEditText = (ClearEditText) findViewById(R.id.report_query_search_et);
        mVoiceImageView = (ImageView) findViewById(R.id.report_query_search_voice_iv);
        mResultListView = (ListView) findViewById(R.id.report_query_search_lv);

        mEmptyLayout = new EmptyLayout(this, mResultListView);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");

        mQuerySearchBeanList = new ArrayList<>();
        mReportQuerySearchAdapter = new ReportQuerySearchAdapter(this, mQuerySearchBeanList);
        mResultListView.setAdapter(mReportQuerySearchAdapter);


        mCurrentMaster = CommonUtil.getSharedPreferences(this, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(this, "erp_username");
    }

    private void initEvents() {
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mReportQuerySearchAdapter == null) {
                    //Toast.makeText(getApplication(), "系统内部错误", Toast.LENGTH_SHORT).show();
                } else {
                    if (!StringUtil.isEmpty(s.toString())) {
                        mReportQuerySearchAdapter.getFilter().filter(s.toString());
                    } else {
                        mReportQuerySearchAdapter.getFilter().filter(null);
                    }
                }
            }
        });

        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
                dataInquiryGirdItemBean.setColor(mReportQuerySearchAdapter.getObjects().get(position).getColor());
                dataInquiryGirdItemBean.setIconText(mReportQuerySearchAdapter.getObjects().get(position).getListBean().getTitle());

                String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(ReportQuerySearchActivity.this,
                        mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE);
                List<DataInquiryGirdItemBean> recentBrowse = new ArrayList<DataInquiryGirdItemBean>();
                if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
                    try {
                        recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);

                        for (int i = 0; i < recentBrowse.size(); i++) {
                            if (mReportQuerySearchAdapter.getObjects().get(position).getListBean().getTitle() != null && mQuerySearchBeanList.get(position).getListBean().getTitle().equals(recentBrowse.get(i).getIconText())) {
                                recentBrowse.remove(i);
                            }
                        }
                    } catch (Exception e) {

                    }
                }

                recentBrowse.add(0, dataInquiryGirdItemBean);

                String recentJson = JSON.toJSON(recentBrowse).toString();
                CommonUtil.setSharedPreferences(ReportQuerySearchActivity.this
                        , mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE
                        , recentJson);

                Intent intent = new Intent();
                intent.setClass(ReportQuerySearchActivity.this, ReportQueryCriteriaActivity.class);
                intent.putExtra("reportinfo", mReportQuerySearchAdapter.getObjects().get(position).getListBean());
                startActivity(intent);
            }
        });

        mVoiceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizerDialog dialog = new RecognizerDialog(ReportQuerySearchActivity.this, null);
                dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
                dialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                        String s = mSearchEditText.getText().toString() + CommonUtil.getPlaintext(text);
                        mSearchEditText.setText(s);
                        mSearchEditText.setSelection(s.length());
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }
                });
                dialog.show();
            }
        });
    }

    private void initDatas() {
        String reportQueryMenuCache = CommonUtil.getSharedPreferences(this,
                mCurrentUser + mCurrentMaster + Constants.CONSTANT.REPORT_QUERY_MENU_CACHE);
        if (TextUtils.isEmpty(reportQueryMenuCache)) {
            mEmptyLayout.showEmpty();
        } else {
            analysisMenuData(reportQueryMenuCache);
        }
    }

    private void analysisMenuData(String result) {
        if (result != null) {
            try {
                JSONObject resultObject = new JSONObject(result);
                JSONArray dataArray = resultObject.optJSONArray("data");
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.optJSONObject(i);
                        if (dataObject != null) {
                            JSONArray listArray = dataObject.optJSONArray("list");
                            if (listArray != null) {
                                for (int j = 0; j < listArray.length(); j++) {
                                    ReportQuerySearchBean reportQuerySearchBean = new ReportQuerySearchBean();
                                    JSONObject listObject = listArray.optJSONObject(j);
                                    if (listObject != null) {
                                        GridMenuReportStatisticsBean.ListBean listBean = new GridMenuReportStatisticsBean.ListBean();
                                        String caller = optStringNotNull(listObject, "caller");
                                        String title = optStringNotNull(listObject, "title");
                                        String reportName = optStringNotNull(listObject, "reportName");

                                        listBean.setCaller(caller);
                                        listBean.setReportName(reportName);
                                        listBean.setTitle(title);

                                        reportQuerySearchBean.setListBean(listBean);
                                        reportQuerySearchBean.setColor(mColors[(i) % mColors.length]);
                                        mQuerySearchBeanList.add(reportQuerySearchBean);
                                    }
                                }
                            }
                        }
                    }
                }
                mReportQuerySearchAdapter.notifyDataSetChanged();
                if (mQuerySearchBeanList.size() == 0) {
                    mEmptyLayout.showEmpty();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public String optStringNotNull(JSONObject json, String key) {
        if (json.isNull(key)) {
            return "";
        } else {
            return json.optString(key, "");
        }
    }

    public class ReportQuerySearchAdapter extends BaseAdapter implements Filterable {
        private List<ReportQuerySearchBean> objects = new ArrayList<ReportQuerySearchBean>();
        private List<ReportQuerySearchBean> filterObjects = new ArrayList<ReportQuerySearchBean>();

        private Context context;
        private LayoutInflater layoutInflater;

        public ReportQuerySearchAdapter(Context context, List<ReportQuerySearchBean> objects) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public List<ReportQuerySearchBean> getObjects() {
            return objects;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public ReportQuerySearchBean getItem(int position) {
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry_more_menu, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            initializeViews((ReportQuerySearchBean) getItem(position), (ViewHolder) convertView.getTag());
            return convertView;
        }

        private void initializeViews(ReportQuerySearchBean object, ViewHolder holder) {
            holder.itemDataInquiryMoreMenuTitle.setText(object.getListBean().getTitle());
            holder.itemDataInquiryMoreMenuIcon.setText(object.getListBean().getTitle().substring(0, 1));
            holder.itemDataInquiryMoreMenuIcon.setMyBackgroundColor(context.getResources().getColor(object.getColor()));
        }

        protected class ViewHolder {
            private CircleTextView itemDataInquiryMoreMenuIcon;
            private TextView itemDataInquiryMoreMenuTitle;

            public ViewHolder(View view) {
                itemDataInquiryMoreMenuIcon = (CircleTextView) view.findViewById(R.id.item_data_inquiry_more_menu_icon);
                itemDataInquiryMoreMenuTitle = (TextView) view.findViewById(R.id.item_data_inquiry_more_menu_title);
            }
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        filterResults.values = mQuerySearchBeanList;
                        filterResults.count = mQuerySearchBeanList.size();
                    } else {
                        filterObjects = new ArrayList<>();
                        for (int i = 0; i < mQuerySearchBeanList.size(); i++) {
                            String title = mQuerySearchBeanList.get(i).getListBean().getTitle();
                            if (title.contains(constraint.toString())) {
                                filterObjects.add(mQuerySearchBeanList.get(i));
                            }
                        }

                        filterResults.values = filterObjects;
                        filterResults.count = filterObjects.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    objects = (List<ReportQuerySearchBean>) results.values;
                    notifyDataSetChanged();
                    if (results.count == 0 && mEmptyLayout != null) {
                        mEmptyLayout.showEmpty();
                    }
                }
            };
        }

    }

}
