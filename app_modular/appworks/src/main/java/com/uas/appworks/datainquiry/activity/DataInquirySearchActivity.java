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
import com.uas.appworks.datainquiry.bean.DataInquirySearchBean;
import com.uas.appworks.datainquiry.bean.GridMenuDataInquiryBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/14.
 */

public class DataInquirySearchActivity extends BaseActivity {
    private ClearEditText mSearchEditText;
    private ImageView mVoiceImageView;
    private ListView mResultListView;
    private DataInquirySearchAdapter mDataInquirySearchAdapter;
    private List<DataInquirySearchBean> mDataInquirySearchBeanList;
    private String mCurrentMaster;
    private String mCurrentUser;
    private EmptyLayout mEmptyLayout;

    private int[] mColors = new int[]{R.color.data_inquiry_gird_menu_color1, R.color.data_inquiry_gird_menu_color2
            , R.color.data_inquiry_gird_menu_color3, R.color.data_inquiry_gird_menu_color4, R.color.data_inquiry_gird_menu_color5,
            R.color.data_inquiry_gird_menu_color6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_inquiry_search);
        setTitle("数据查询");

        initViews();
        initEvents();
        initDatas();
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
                if (mDataInquirySearchAdapter == null) {

                } else {
                    if (!StringUtil.isEmpty(s.toString())) {
                        mDataInquirySearchAdapter.getFilter().filter(s.toString());
                    } else {
                        mDataInquirySearchAdapter.getFilter().filter(null);
                    }
                }
            }
        });

        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataInquiryGirdItemBean dataInquiryGirdItemBean = new DataInquiryGirdItemBean();
                dataInquiryGirdItemBean.setColor(mDataInquirySearchAdapter.getObjects().get(position).getColor());
                dataInquiryGirdItemBean.setIconText(mDataInquirySearchAdapter.getObjects().get(position).getQueryScheme().getScheme());

                String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(DataInquirySearchActivity.this,
                        mCurrentUser + mCurrentMaster + Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE);
                List<DataInquiryGirdItemBean> recentBrowse = new ArrayList<DataInquiryGirdItemBean>();
                if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
                    try {
                        recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);

                        for (int i = 0; i < recentBrowse.size(); i++) {
                            if (mDataInquirySearchAdapter.getObjects().get(position).getQueryScheme().getScheme() != null
                                    && mDataInquirySearchAdapter.getObjects().get(position).getQueryScheme().getScheme().equals(recentBrowse.get(i).getIconText())) {
                                recentBrowse.remove(i);
                            }
                        }
                    } catch (Exception e) {

                    }
                }

                recentBrowse.add(0, dataInquiryGirdItemBean);

                String recentJson = JSON.toJSON(recentBrowse).toString();
                CommonUtil.setSharedPreferences(DataInquirySearchActivity.this
                        , mCurrentUser + mCurrentMaster + Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE
                        , recentJson);

                Intent intent = new Intent();
                intent.setClass(DataInquirySearchActivity.this, DataInquiryListActivity.class);
                intent.putExtra("scheme", mDataInquirySearchAdapter.getObjects().get(position).getQueryScheme());
                startActivity(intent);
            }
        });

        mVoiceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizerDialog dialog = new RecognizerDialog(DataInquirySearchActivity.this, null);
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
        String dataInquiryMenuCache = CommonUtil.getSharedPreferences(this,
                mCurrentUser + mCurrentMaster + Constants.CONSTANT.DATA_INQUIRY_MENU_CACHE);
        if (TextUtils.isEmpty(dataInquiryMenuCache)) {
            mEmptyLayout.showEmpty();
        } else {
            analysisMenuData(dataInquiryMenuCache);
        }
    }

    private void initViews() {
        mSearchEditText = (ClearEditText) findViewById(R.id.data_inquiry_search_et);
        mVoiceImageView = (ImageView) findViewById(R.id.data_inquiry_search_voice_iv);
        mResultListView = (ListView) findViewById(R.id.data_inquiry_search_lv);

        mEmptyLayout = new EmptyLayout(this, mResultListView);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");

        mDataInquirySearchBeanList = new ArrayList<>();
        mDataInquirySearchAdapter = new DataInquirySearchAdapter(this, mDataInquirySearchBeanList);
        mResultListView.setAdapter(mDataInquirySearchAdapter);


        mCurrentMaster = CommonUtil.getSharedPreferences(this, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(this, "erp_username");
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
                                    JSONObject listObject = listArray.optJSONObject(j);
                                    if (listObject != null) {
                                        String title = optStringNotNull(listObject, "title");
                                        String caller = optStringNotNull(listObject, "caller");
                                        JSONArray schemeArray = listObject.optJSONArray("schemes");
                                        if (schemeArray != null) {
                                            for (int k = 0; k < schemeArray.length(); k++) {
                                                JSONObject schemeObject = schemeArray.optJSONObject(k);
                                                if (schemeObject != null) {
                                                    DataInquirySearchBean dataInquirySearchBean = new DataInquirySearchBean();

                                                    String scheme = optStringNotNull(schemeObject, "scheme");
                                                    String schemeId = optStringNotNull(schemeObject, "schemeId");

                                                    GridMenuDataInquiryBean.QueryScheme queryScheme = new GridMenuDataInquiryBean.QueryScheme();
                                                    queryScheme.setTitle(title);
                                                    queryScheme.setCaller(caller);
                                                    queryScheme.setScheme(scheme);
                                                    queryScheme.setSchemeId(schemeId);

                                                    dataInquirySearchBean.setQueryScheme(queryScheme);
                                                    dataInquirySearchBean.setColor(mColors[(i) % mColors.length]);

                                                    mDataInquirySearchBeanList.add(dataInquirySearchBean);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mDataInquirySearchAdapter.notifyDataSetChanged();
                if (mDataInquirySearchBeanList.size() == 0) {
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

    public class DataInquirySearchAdapter extends BaseAdapter implements Filterable {
        private List<DataInquirySearchBean> objects = new ArrayList<DataInquirySearchBean>();
        private List<DataInquirySearchBean> filterObjects = new ArrayList<DataInquirySearchBean>();

        private Context context;
        private LayoutInflater layoutInflater;

        public DataInquirySearchAdapter(Context context, List<DataInquirySearchBean> objects) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public List<DataInquirySearchBean> getObjects() {
            return objects;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public DataInquirySearchBean getItem(int position) {
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
            initializeViews((DataInquirySearchBean) getItem(position), (ViewHolder) convertView.getTag());
            return convertView;
        }

        private void initializeViews(DataInquirySearchBean object, ViewHolder holder) {
            holder.itemDataInquiryMoreMenuTitle.setText(object.getQueryScheme().getScheme());
            holder.itemDataInquiryMoreMenuIcon.setText(object.getQueryScheme().getScheme().substring(0, 1));
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
                        filterResults.values = mDataInquirySearchBeanList;
                        filterResults.count = mDataInquirySearchBeanList.size();
                    } else {
                        filterObjects = new ArrayList<>();
                        for (int i = 0; i < mDataInquirySearchBeanList.size(); i++) {
                            String title = mDataInquirySearchBeanList.get(i).getQueryScheme().getScheme();
                            if (title.contains(constraint.toString())) {
                                filterObjects.add(mDataInquirySearchBeanList.get(i));
                            }
                        }

                        filterResults.values = filterObjects;
                        filterResults.count = filterObjects.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    objects = (List<DataInquirySearchBean>) results.values;
                    notifyDataSetChanged();
                    if (results.count == 0 && mEmptyLayout != null) {
                        mEmptyLayout.showEmpty();
                    }
                }
            };
        }

    }
}
