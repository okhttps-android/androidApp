package com.uas.appworks.datainquiry.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.LogUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.dao.historical.HistoricalRecordBean;
import com.core.dao.historical.HistoricalRecordDao;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.ClearEditText;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.adapter.DataInquiryFlexAdapter;
import com.uas.appworks.datainquiry.adapter.DataInquirySchemeConditionAdapter;
import com.uas.appworks.datainquiry.adapter.DataInquiryTotalAdapter;
import com.uas.appworks.datainquiry.adapter.HistoricalRecordAdapter;
import com.uas.appworks.datainquiry.bean.DataInquiryFlexBean;
import com.uas.appworks.datainquiry.bean.DataInquiryTotalBean;
import com.uas.appworks.datainquiry.bean.GridMenuDataInquiryBean;
import com.uas.appworks.datainquiry.bean.SchemeConditionBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2017/8/3.
 * 数据查询列表页面
 */
public class DataInquiryListActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {
    private final int GET_DATA_RESULT = 0X11;
    private final int GET_QUERY_CRITERIA = 0X22;
    private ImageView mBackImageView, mFilterImageView, mVoiceImageView, mTotalHideImageView, mTotalSpreadImageView;
    private ClearEditText mSearchEditText;
    private GridMenuDataInquiryBean.QueryScheme mQueryScheme;
    private String mCaller, mSchemeName, mSchemeId, mCondition;

    private PullToRefreshListView mDataListView;
    private LinearLayout mFuzzyWholeLl, mFuzzyHistoryLl, mExactWholeLl, mExactOptionLl, mTotalLl;
    private TextView mHistoryClearTv;
    private MyListView mHistoryDataLv;
    private ListView mTotalLv, mExactOptionLv;
    private TextView mExactOptionResetTv, mExactOptionCancelTv, mExactOptionConfirmTv;
    private View mGrayBackGround;
    private RelativeLayout mTotalHideRl;
    private Animation mInAnimation, mOutAnimation;
    private int mPageIndex = 1, mPageSize = 10;
    private List<HistoricalRecordBean> mHistoricalRecordBeans;
    private HistoricalRecordAdapter mHistoricalRecordAdapter;
    private String mSearchField = "";
    private List<SchemeConditionBean> mAllSchemeConditions, mAppSchemeConditions, mfuzzySchemeConditionBeans, mResetSchemeConditionBeans;
    private DataInquirySchemeConditionAdapter mDataInquirySchemeConditionAdapter;
    private List<DataInquiryFlexBean> mDataInquiryFlexBeans;
    private DataInquiryFlexAdapter mDataInquiryFlexAdapter;
    private List<DataInquiryFlexBean.RowBean.RowChildBean> mAllRowChildBeans;
    private EmptyLayout mEmptyLayout;
    private int mOldPosition = -1;
    private DataInquiryTotalAdapter mDataInquiryTotalAdapter;
    private List<DataInquiryTotalBean> mDataInquiryTotalBeans;
    private boolean isConditionSuccess = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_QUERY_CRITERIA:
                    isConditionSuccess = true;
                    if (mDataListView.isRefreshing()) {
                        mDataListView.onRefreshComplete();
                    }
                    String result = msg.getData().getString("result");
                    analysisCondition(result);
                    break;
                case GET_DATA_RESULT:
                    if (mDataListView.isRefreshing()) {
                        mDataListView.onRefreshComplete();
                    }
                    progressDialog.dismiss();
                    if (mAllSchemeConditions == null || mAllSchemeConditions.size() == 0) {
                        mEmptyLayout.showEmpty();
                    } else {
                        analysisData(msg);
                    }
                    break;
                case 0x88:
                    mTotalLl.setVisibility(View.GONE);
                    mTotalSpreadImageView.setVisibility(View.GONE);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
//                    mDataListView.setVisibility(View.VISIBLE);
                    mTotalSpreadImageView.setVisibility(View.GONE);
                    if (mDataListView.isRefreshing())
                        mDataListView.onRefreshComplete();
                    progressDialog.dismiss();
                    ToastMessage(msg.getData().getString("result"));
                    if (mPageIndex > 1) {
                        mPageIndex--;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_inquiry_list);

        initViews();
        initEvents();
        initDatas();
    }

    private void initDatas() {
        mCondition = "1 = 1";

        mHistoricalRecordAdapter = new HistoricalRecordAdapter(DataInquiryListActivity.this, mHistoricalRecordBeans);
        mHistoryDataLv.setAdapter(mHistoricalRecordAdapter);

        try {
            List<HistoricalRecordBean> historicalRecordBeansById = HistoricalRecordDao.getInstance().getHistoricalRecordBeansById(mSchemeId);
            if (historicalRecordBeansById.size() > 0) {
                for (int i = 0; i < historicalRecordBeansById.size(); i++) {
                    if (i < 6) {
                        mHistoricalRecordBeans.add(historicalRecordBeansById.get(i));
                    } else {
                        HistoricalRecordDao.getInstance().deleteFromHistoricalRecordByField(mSchemeId, historicalRecordBeansById.get(i).getSearchField());
                    }
                }
                mHistoricalRecordAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {

        }


        if (!CommonUtil.isNetWorkConnected(this)) {
            mEmptyLayout.setErrorMessage(getString(R.string.networks_out));
            mEmptyLayout.showError();
        } else {
            getQueryCriteria();
        }

    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    private void initViews() {
        setTitle("");
        View view = LayoutInflater.from(ct).inflate(R.layout.action_data_inquiry_list, null);
        mBackImageView = (ImageView) view.findViewById(R.id.back);
        mFilterImageView = (ImageView) view.findViewById(R.id.data_inquiry_filter_iv);
        mVoiceImageView = (ImageView) view.findViewById(R.id.data_inquiry_voice_iv);
        mSearchEditText = (ClearEditText) view.findViewById(R.id.data_inquiry_filter_et);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(view);

        mDataListView = (PullToRefreshListView) findViewById(R.id.data_inquiry_list_ptlv);
        mDataListView.setMode(PullToRefreshBase.Mode.BOTH);
        mFuzzyWholeLl = (LinearLayout) findViewById(R.id.data_inquiry_list_fuzzy_ll);
        mFuzzyHistoryLl = (LinearLayout) findViewById(R.id.data_inquiry_list_history_ll);
        mHistoryClearTv = (TextView) findViewById(R.id.data_inquiry_history_clear_tv);
        mHistoryDataLv = (MyListView) findViewById(R.id.data_inquiry_list_history_data_lv);
        mExactWholeLl = (LinearLayout) findViewById(R.id.data_inquiry_list_exact_ll);
        mExactOptionLl = (LinearLayout) findViewById(R.id.data_inquiry_list_option_ll);
        mExactOptionLv = (ListView) findViewById(R.id.data_inquiry_list_option_lv);
        mExactOptionCancelTv = (TextView) findViewById(R.id.data_inquiry_list_option_cancel_tv);
        mExactOptionResetTv = (TextView) findViewById(R.id.data_inquiry_list_option_reset_tv);
        mExactOptionConfirmTv = (TextView) findViewById(R.id.data_inquiry_list_option_confirm_tv);
        mTotalLl = (LinearLayout) findViewById(R.id.data_inquiry_list_total_ll);
        mTotalHideImageView = (ImageView) findViewById(R.id.data_inquiry_list_total_hide_iv);
        mTotalSpreadImageView = (ImageView) findViewById(R.id.data_inquiry_list_total_spread_iv);
        mTotalLv = (ListView) findViewById(R.id.data_inquiry_list_total_lv);
        mGrayBackGround = findViewById(R.id.data_inquiry_list_gray_bg);
        mTotalHideRl = (RelativeLayout) findViewById(R.id.data_inquiry_list_total_hide_rl);

//        mFuzzyWholeLl.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        mExactWholeLl.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mHistoricalRecordBeans = new ArrayList<>();
        mAllSchemeConditions = new ArrayList<>();
        mAppSchemeConditions = new ArrayList<>();
        mfuzzySchemeConditionBeans = new ArrayList<>();
        mResetSchemeConditionBeans = new ArrayList<>();
        mDataInquiryFlexBeans = new ArrayList<>();
        mAllRowChildBeans = new ArrayList<>();
        mDataInquiryTotalBeans = new ArrayList<>();
        mInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_search_popin);
        mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_search_popout);

        mEmptyLayout = new EmptyLayout(this, mDataListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");

        mDataInquirySchemeConditionAdapter = new DataInquirySchemeConditionAdapter(this, mAppSchemeConditions);
        mExactOptionLv.setAdapter(mDataInquirySchemeConditionAdapter);

        mDataInquiryFlexAdapter = new DataInquiryFlexAdapter(this, mDataInquiryFlexBeans);
        mDataListView.setAdapter(mDataInquiryFlexAdapter);

        mDataInquiryTotalAdapter = new DataInquiryTotalAdapter(this, mDataInquiryTotalBeans);
        mDataInquiryTotalAdapter.setHandler(mHandler);
        mTotalLv.setAdapter(mDataInquiryTotalAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            mQueryScheme = (GridMenuDataInquiryBean.QueryScheme) intent.getSerializableExtra("scheme");
            mSchemeId = mQueryScheme.getSchemeId();
            mSchemeName = mQueryScheme.getScheme();
            mCaller = mQueryScheme.getCaller();
        }
    }

    private void initEvents() {
        mBackImageView.setOnClickListener(this);
        mFilterImageView.setOnClickListener(this);
        mVoiceImageView.setOnClickListener(this);
        mHistoryClearTv.setOnClickListener(this);
        mExactOptionCancelTv.setOnClickListener(this);
        mExactOptionResetTv.setOnClickListener(this);
        mExactOptionConfirmTv.setOnClickListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.setOnClickListener(this);

        mDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int reallyPosition = (int) parent.getItemIdAtPosition(position);
                if (CommonUtil.isRepeatClick()) {
                    if (mOldPosition == reallyPosition) {
                        mDataInquiryFlexAdapter.getObjects().get(reallyPosition).setIsFlex(!mDataInquiryFlexAdapter.getObjects().get(reallyPosition).isFlex());
                        mDataInquiryFlexAdapter.notifyDataSetChanged();
                    } else {
                        mOldPosition = reallyPosition;
                    }
                }
                mOldPosition = reallyPosition;
            }
        });

        mDataListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(DataInquiryListActivity.this)) {
                    mPageIndex = 1;
                    if (isConditionSuccess) {
                        getSchemeData();
                    } else {
                        getQueryCriteria();
                    }
                } else {
                    mDataListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDataListView.onRefreshComplete();
                        }
                    }, 500);
                    mEmptyLayout.setErrorMessage(getString(R.string.networks_out));
                    mEmptyLayout.showError();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(DataInquiryListActivity.this)) {
                    if (isConditionSuccess) {
                        mPageIndex++;
                        getSchemeData();
                    } else {
                        mPageIndex = 1;
                        getQueryCriteria();
                    }
                } else {
                    mDataListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDataListView.onRefreshComplete();
                        }
                    }, 500);
                    mEmptyLayout.setErrorMessage(getString(R.string.networks_out));
                    mEmptyLayout.showError();
                }
            }
        });

        mSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mExactWholeLl.getVisibility() == View.VISIBLE) {
                        mExactWholeLl.setVisibility(View.GONE);
                    }
                    if (mHistoricalRecordAdapter.getObjects().size() > 0) {
                        mGrayBackGround.setVisibility(View.VISIBLE);
                        if (mFuzzyWholeLl.getVisibility() == View.GONE) {
                            mFuzzyWholeLl.setVisibility(View.VISIBLE);
                            mFuzzyHistoryLl.startAnimation(mInAnimation);
                            mHistoryDataLv.setEnabled(true);
                        }
                    } else {
                        mGrayBackGround.setVisibility(View.GONE);
                    }

                } else {
                    CommonUtil.closeKeybord(mSearchEditText, DataInquiryListActivity.this);
                    if (mFuzzyWholeLl.getVisibility() == View.VISIBLE) {
                        mFuzzyWholeLl.setVisibility(View.GONE);
                        mGrayBackGround.setVisibility(View.GONE);
                        mHistoryDataLv.setEnabled(false);
                    }

                }
            }
        });

        mGrayBackGround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.closeKeybord(mSearchEditText, DataInquiryListActivity.this);
                if (mFuzzyWholeLl.getVisibility() == View.VISIBLE) {
                    mFuzzyHistoryLl.startAnimation(mOutAnimation);
                }
                if (mExactWholeLl.getVisibility() == View.VISIBLE) {
                    mExactOptionLl.startAnimation(mOutAnimation);
                }
                mGrayBackGround.setVisibility(View.GONE);
            }
        });

        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSearchEditText.clearFocus();
                mGrayBackGround.setVisibility(View.GONE);
                mExactWholeLl.setVisibility(View.GONE);
                mFuzzyWholeLl.setVisibility(View.GONE);
                mHistoryDataLv.setEnabled(false);
                CommonUtil.closeKeybord(mSearchEditText, DataInquiryListActivity.this);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mHistoryDataLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoricalRecordBean historicalRecordBean = mHistoricalRecordAdapter.getObjects().get(position);
                mSearchEditText.setText(historicalRecordBean.getSearchField());

                searchEvent();
            }
        });

        mTotalHideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTotalLl.getVisibility() == View.VISIBLE) {
                    mTotalLl.setVisibility(View.GONE);
                }
                mTotalSpreadImageView.setVisibility(View.VISIBLE);
            }
        });

        mTotalSpreadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTotalLl.getVisibility() == View.GONE) {
                    mTotalLl.setVisibility(View.VISIBLE);
                }
            }
        });

        mTotalHideRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTotalLl.getVisibility() == View.VISIBLE) {
                    mTotalLl.setVisibility(View.GONE);
                }
                mTotalSpreadImageView.setVisibility(View.VISIBLE);
            }
        });
    }


    private void analysisData(Message msg) {
        String result = msg.getData().getString("result");
        if (result != null) {
            mDataInquiryTotalBeans.clear();
            if (mPageIndex == 1) {
                mDataInquiryFlexBeans.clear();
            }
            LogUtil.prinlnLongMsg("schemedata", msg.getData().getString("result"));
            try {
                JSONObject resultObject = new JSONObject(result);
                JSONArray listArray = resultObject.optJSONArray("listdata");
                JSONObject totalObject = resultObject.optJSONObject("summaryField");

                if ((listArray == null || listArray.length() < mPageSize) && mPageIndex > 1) {
                    ToastMessage("已加载全部数据");
                }
                if (totalObject != null) {
                    for (int j = 0; j < mAllSchemeConditions.size(); j++) {
                        String field = mAllSchemeConditions.get(j).getField();
                        String caption = mAllSchemeConditions.get(j).getCaption();

                        if (field != null) {
                            if (!totalObject.isNull(field)) {
                                DataInquiryTotalBean dataInquiryTotalBean = new DataInquiryTotalBean();
                                double total = totalObject.optDouble(field);
                                dataInquiryTotalBean.setCaption(caption);
                                dataInquiryTotalBean.setField(field);
                                dataInquiryTotalBean.setTotal(total);

                                mDataInquiryTotalBeans.add(dataInquiryTotalBean);
                            }
                        }
                    }
                }

                if (listArray != null && listArray.length() > 0 && mDataInquiryTotalBeans.size() > 0) {
                    if (mPageIndex == 1) {
                        mTotalLl.setVisibility(View.VISIBLE);
                    }
                    mTotalSpreadImageView.setVisibility(View.VISIBLE);
                } else {
                    if (mPageIndex == 1) {
                        mTotalLl.setVisibility(View.GONE);
                        mTotalSpreadImageView.setVisibility(View.GONE);
                    }
                }

                mDataInquiryTotalAdapter.notifyDataSetChanged();

                if (listArray != null && listArray.length() > 0) {
                    if (!TextUtils.isEmpty(mSearchField)) {
                        HistoricalRecordBean historicalRecordBean = new HistoricalRecordBean();
                        historicalRecordBean.setSchemeId(mSchemeId);
                        historicalRecordBean.setSchemeName(mSchemeName);
                        historicalRecordBean.setSearchField(mSearchField);
                        HistoricalRecordDao.getInstance().saveToHistoricalRecord(historicalRecordBean);

                        try {
                            mHistoricalRecordBeans.clear();
                            List<HistoricalRecordBean> historicalRecordBeansById = HistoricalRecordDao.getInstance().getHistoricalRecordBeansById(mSchemeId);
                            if (historicalRecordBeansById.size() > 0) {
                                for (int i = 0; i < historicalRecordBeansById.size(); i++) {
                                    if (i < 6) {
                                        mHistoricalRecordBeans.add(historicalRecordBeansById.get(i));
                                    }
                                }
                                mHistoricalRecordAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {

                        }
                    }

                    for (int i = 0; i < listArray.length(); i++) {
                        mAllRowChildBeans = new ArrayList<>();
                        JSONObject dataObject = listArray.optJSONObject(i);
                        if (dataObject != null) {
                            DataInquiryFlexBean dataInquiryFlexBean = new DataInquiryFlexBean();
                            dataInquiryFlexBean.setIsFlex(false);

                            List<DataInquiryFlexBean.RowBean> rowBeans = new ArrayList<>();

                            for (int j = 0; j < mAllSchemeConditions.size(); j++) {
                                String field = mAllSchemeConditions.get(j).getField();
                                String type = mAllSchemeConditions.get(j).getType();
                                String caption = mAllSchemeConditions.get(j).getCaption();
                                String value = dataObject.getString(field);

                                if (value != null) {
                                    DataInquiryFlexBean.RowBean.RowChildBean rowChildBean = new DataInquiryFlexBean.RowBean.RowChildBean();
                                    rowChildBean.setCaption(caption);
                                    if ("null".equals(value)) {
                                        rowChildBean.setValue("");
                                    } else {
                                        if (("D".equals(type) || "CD".equals(type)) && value.length() > 8) {
                                            value = value.substring(0, value.length() - 8);
                                        }
                                        rowChildBean.setValue(value);
                                    }
                                    rowChildBean.setField(field);
                                    rowChildBean.setWidth(mAllSchemeConditions.get(j).getWidth());

                                    mAllRowChildBeans.add(rowChildBean);
                                }
                            }

                            for (int k = 0; k < mAllRowChildBeans.size(); k++) {
                                DataInquiryFlexBean.RowBean rowBean = new DataInquiryFlexBean.RowBean();

                                List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = new ArrayList<>();

                                DataInquiryFlexBean.RowBean.RowChildBean rowChildBean1 = mAllRowChildBeans.get(k);
                                childBeans.add(rowChildBean1);
                                if (rowChildBean1.getWidth() <= 100 && k < (mAllRowChildBeans.size() - 1)) {
                                    DataInquiryFlexBean.RowBean.RowChildBean rowChildBean2 = mAllRowChildBeans.get(k + 1);
                                    if (rowChildBean2.getWidth() <= 100) {
                                        childBeans.add(rowChildBean2);
                                        k++;
                                    }
                                }

                                rowBean.setRowChildBeans(childBeans);

                                rowBeans.add(rowBean);
                            }

                            ////////////////////////////////////////
                            /*List<DataInquiryFlexBean.RowBean.RowChildBean> shortChildBeans = new ArrayList<>();
                            List<DataInquiryFlexBean.RowBean.RowChildBean> longChildBeans = new ArrayList<>();
                            for (int k = 0; k < mAllRowChildBeans.size(); k++) {
                                DataInquiryFlexBean.RowBean.RowChildBean rowChildBean = mAllRowChildBeans.get(k);
                                if (rowChildBean.getWidth() <= 100) {
                                    shortChildBeans.add(rowChildBean);
                                } else {
                                    longChildBeans.add(rowChildBean);
                                }
                            }

                            for (int k = 0; k < (shortChildBeans.size() / 2); k++) {
                                DataInquiryFlexBean.RowBean rowBean = new DataInquiryFlexBean.RowBean();

                                List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = new ArrayList<>();
                                childBeans.add(shortChildBeans.get(k * 2));
                                childBeans.add(shortChildBeans.get((k * 2) + 1));

                                rowBean.setRowChildBeans(childBeans);

                                rowBeans.add(rowBean);
                            }

                            if ((shortChildBeans.size() % 2) != 0) {
                                DataInquiryFlexBean.RowBean rowBean = new DataInquiryFlexBean.RowBean();

                                List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = new ArrayList<>();
                                childBeans.add(shortChildBeans.get(shortChildBeans.size() - 1));

                                rowBean.setRowChildBeans(childBeans);

                                rowBeans.add(rowBean);
                            }

                            for (int k = 0; k < longChildBeans.size(); k++) {
                                DataInquiryFlexBean.RowBean rowBean = new DataInquiryFlexBean.RowBean();

                                List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = new ArrayList<>();
                                childBeans.add(longChildBeans.get(k));

                                rowBean.setRowChildBeans(childBeans);

                                rowBeans.add(rowBean);
                            }
*/
                            dataInquiryFlexBean.setRowBeans(rowBeans);

                            mDataInquiryFlexBeans.add(dataInquiryFlexBean);
                        }
                    }
                    mDataInquiryFlexAdapter.notifyDataSetChanged();
                } else {
                    mDataInquiryFlexAdapter.notifyDataSetChanged();
                    mEmptyLayout.showEmpty();
                }

            } catch (JSONException e) {
                mDataInquiryFlexAdapter.notifyDataSetChanged();
                if (mDataInquiryFlexBeans.size() == 0) {
                    mEmptyLayout.showEmpty();
                }
                e.printStackTrace();
            }
        }
    }

    private void analysisCondition(String result) {
        if (result != null) {
            LogUtil.prinlnLongMsg("querycriteria", result);
            try {
                JSONObject resultObject = new JSONObject(result);
                JSONArray dataArray = resultObject.optJSONArray("data");
                if (dataArray != null && dataArray.length() > 0) {
                    getSchemeData();
                    String fuzzyHint = "";
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.optJSONObject(i);
                        if (dataObject != null) {
                            SchemeConditionBean schemeConditionBean = new SchemeConditionBean();
                            String position = optStringNotNull(dataObject, "position");
                            String field = optStringNotNull(dataObject, "field");
                            boolean appCondition = dataObject.optBoolean("appCondition");
                            int width = (int) optLongNotNull(dataObject, "width");
                            String caption = optStringNotNull(dataObject, "caption");
                            String defaultValue = optStringNotNull(dataObject, "defaultValue");
                            String type = optStringNotNull(dataObject, "type");
                            String table = optStringNotNull(dataObject, "table");

                            if (!dataObject.isNull("properties") && ("CBG".equals(type) || "EC".equals(type) || "C".equals(type) || "R".equals(type))) {
                                JSONArray properties = dataObject.optJSONArray("properties");
                                if (properties != null) {
                                    List<SchemeConditionBean.Property> propertyList = new ArrayList<>();
                                    for (int j = 0; j < properties.length(); j++) {
                                        JSONObject propertyObject = properties.optJSONObject(j);
                                        SchemeConditionBean.Property property = new SchemeConditionBean.Property();
                                        String value = optStringNotNull(propertyObject, "value");
                                        String display = optStringNotNull(propertyObject, "display");

                                        property.setDisplay(display);
                                        property.setValue(value);
                                        property.setState(false);

                                        if (!"$ALL".equals(display) && !"$ALL".equals(value)) {
                                            propertyList.add(property);
                                        }
                                    }
                                    schemeConditionBean.setProperties(propertyList);
                                }
                            } else {
                                ArrayList<SchemeConditionBean.Property> properties = new ArrayList<>();
                                if ("N".equals(type)) {
                                    for (int m = 0; m < 2; m++) {
                                        SchemeConditionBean.Property property = new SchemeConditionBean.Property();
                                        properties.add(property);
                                    }
                                } else if ("D".equals(type) || "CD".equals(type) || "YM".equals(type) || "YMV".equals(type)) {
                                    for (int m = 0; m < 2; m++) {
                                        SchemeConditionBean.Property property = new SchemeConditionBean.Property();
                                        if (m == 0) {
                                            if ("YM".equals(type) || "YMV".equals(type)) {
                                                property.setDisplay(getMonthFirstDay("yyyyMM"));
                                                property.setValue(getMonthFirstDay("yyyyMM"));
                                            } else {
                                                property.setDisplay(getMonthFirstDay("yyyy-MM-dd"));
                                                property.setValue(getMonthFirstDay("yyyy-MM-dd"));
                                            }
                                        } else if (m == 1) {
                                            if ("YM".equals(type) || "YMV".equals(type)) {
                                                property.setDisplay(getMonthLastDay("yyyyMM"));
                                                property.setValue(getMonthLastDay("yyyyMM"));
                                            } else {
                                                property.setDisplay(getMonthLastDay("yyyy-MM-dd"));
                                                property.setValue(getMonthLastDay("yyyy-MM-dd"));
                                            }
                                        }
                                        properties.add(property);
                                    }
                                } else if ("CBG".equals(type) || "EC".equals(type) || "C".equals(type) || "R".equals(type)) {

                                } else {
                                    SchemeConditionBean.Property property = new SchemeConditionBean.Property();
                                    property.setValue(defaultValue);
                                    property.setDisplay(defaultValue);
                                    properties.add(property);
                                }
                                schemeConditionBean.setProperties(properties);
                            }

                            schemeConditionBean.setPosition(position);
                            schemeConditionBean.setField(field);
                            schemeConditionBean.setAppCondition(appCondition);
                            schemeConditionBean.setWidth(width);
                            schemeConditionBean.setCaption(caption);
                            schemeConditionBean.setDefaultValue(defaultValue);
                            schemeConditionBean.setType(type);
                            schemeConditionBean.setTable(table);

                            mAllSchemeConditions.add(schemeConditionBean);

                            if (schemeConditionBean.isAppCondition()) {
                                mAppSchemeConditions.add(schemeConditionBean);
                                if ("S".equals(type)) {
                                    mfuzzySchemeConditionBeans.add(schemeConditionBean);
                                }
                            }
                        }
                    }

                    Collections.sort(mAllSchemeConditions);
                    Collections.sort(mAppSchemeConditions);

                    try {
                        mResetSchemeConditionBeans = deepCopy(mAppSchemeConditions);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    mDataInquirySchemeConditionAdapter.notifyDataSetChanged();

                    for (int i = 0; i < mAppSchemeConditions.size(); i++) {
                        if ("S".equals(mAppSchemeConditions.get(i).getType())) {
                            fuzzyHint = fuzzyHint + mAppSchemeConditions.get(i).getCaption() + "/";
                        }
                    }
                    if (fuzzyHint.length() > 0) {
                        fuzzyHint = fuzzyHint.substring(0, fuzzyHint.length() - 1);
                        mSearchEditText.setHint(fuzzyHint);
                    } else {
                        mSearchEditText.setHint("搜索");
                    }
                } else {
                    progressDialog.dismiss();
                    mEmptyLayout.showEmpty();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.data_inquiry_filter_iv) {
            if (mFuzzyWholeLl.getVisibility() == View.VISIBLE) {
                mFuzzyWholeLl.setVisibility(View.GONE);
                mHistoryDataLv.setEnabled(false);
            }
            mSearchEditText.clearFocus();
            mSearchField = "";
            if (mAppSchemeConditions.size() > 0) {
                if (mExactWholeLl.getVisibility() == View.VISIBLE) {
                    mExactOptionLl.startAnimation(mOutAnimation);
                    mGrayBackGround.setVisibility(View.GONE);
                } else {
                    mDataInquirySchemeConditionAdapter.resetTouchPosition();
                    mExactOptionLl.startAnimation(mInAnimation);
                    mExactWholeLl.setVisibility(View.VISIBLE);
                    mGrayBackGround.setVisibility(View.VISIBLE);
                }
            }

        } else if (i == R.id.back) {
            onBackPressed();

        } else if (i == R.id.data_inquiry_voice_iv) {
            getVoice();

        } else if (i == R.id.data_inquiry_history_clear_tv) {
            HistoricalRecordDao.getInstance().deleteFromHistoricalRecordById(mSchemeId);
            mHistoricalRecordBeans.clear();
            mHistoricalRecordAdapter.notifyDataSetChanged();
            mFuzzyWholeLl.setVisibility(View.GONE);
            mGrayBackGround.setVisibility(View.GONE);

        } else if (i == R.id.data_inquiry_list_option_cancel_tv) {
            mExactWholeLl.setVisibility(View.GONE);
            mGrayBackGround.setVisibility(View.GONE);
            CommonUtil.closeKeybord(mSearchEditText, DataInquiryListActivity.this);

        } else if (i == R.id.data_inquiry_list_option_reset_tv) {
            mAppSchemeConditions.clear();
            try {
                List<SchemeConditionBean> conditionBeans = deepCopy(mResetSchemeConditionBeans);
                if (conditionBeans != null) {
                    mAppSchemeConditions.addAll(conditionBeans);
                    mDataInquirySchemeConditionAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (i == R.id.data_inquiry_list_option_confirm_tv) {
            if (!CommonUtil.isNetWorkConnected(this)) {
                ToastMessage(getString(R.string.networks_out));
            } else {
                exactQuery();
            }

        } else if (i == R.id.data_inquiry_filter_et) {
            if (mExactWholeLl.getVisibility() == View.VISIBLE) {
                mExactWholeLl.setVisibility(View.GONE);
            }
            if (mHistoricalRecordAdapter.getObjects().size() > 0) {
                if (mGrayBackGround.getVisibility() == View.GONE) {
                    mGrayBackGround.setVisibility(View.VISIBLE);
                }
                if (mFuzzyWholeLl.getVisibility() == View.GONE) {
                    mFuzzyWholeLl.setVisibility(View.VISIBLE);
                    mFuzzyHistoryLl.startAnimation(mInAnimation);
                    mHistoryDataLv.setEnabled(true);
                }
            } else {
                if (mGrayBackGround.getVisibility() == View.VISIBLE) {
                    mGrayBackGround.setVisibility(View.GONE);
                }
            }
        }
    }

    private void exactQuery() {
        mCondition = "";
        for (int i = 0; i < mAppSchemeConditions.size(); i++) {
            SchemeConditionBean schemeConditionBean = mAppSchemeConditions.get(i);
            if ("N".equals(schemeConditionBean.getType()) && schemeConditionBean.getProperties().size() == 2) {
                if (!TextUtils.isEmpty(schemeConditionBean.getProperties().get(0).getDisplay())
                        && !TextUtils.isEmpty(schemeConditionBean.getProperties().get(1).getDisplay())) {
                    mCondition = mCondition + "("
                            + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                            + schemeConditionBean.getField()
                            + " >= " + schemeConditionBean.getProperties().get(0).getDisplay()
                            + " and "
                            + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                            + schemeConditionBean.getField()
                            + " <= " + schemeConditionBean.getProperties().get(1).getDisplay() + ") and ";

                } else if (!TextUtils.isEmpty(schemeConditionBean.getProperties().get(0).getDisplay())) {
                    mCondition = mCondition + "("
                            + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                            + schemeConditionBean.getField()
                            + " >= " + schemeConditionBean.getProperties().get(0).getDisplay() + ") and ";
                } else if (!TextUtils.isEmpty(schemeConditionBean.getProperties().get(1).getDisplay())) {
                    mCondition = mCondition + "("
                            + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                            + schemeConditionBean.getField()
                            + " <= " + schemeConditionBean.getProperties().get(1).getDisplay() + ") and ";
                }

            } else if (("D".equals(schemeConditionBean.getType()) || "CD".equals(schemeConditionBean.getType())) && schemeConditionBean.getProperties().size() == 2) {
                mCondition = mCondition + "("
                        + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                        + schemeConditionBean.getField()
                        + " >= to_date(\'" + schemeConditionBean.getProperties().get(0).getDisplay()
                        + "\',\'yyyy-MM-dd\') and "
                        + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                        + schemeConditionBean.getField()
                        + " <= to_date(\'" + schemeConditionBean.getProperties().get(1).getDisplay()
                        + "\',\'yyyy-MM-dd\')) and ";
            } else if ("YMV".equals(schemeConditionBean.getType()) && schemeConditionBean.getProperties().size() == 2) {
                mCondition = mCondition + "(ym_view_param.set_from("
                        + schemeConditionBean.getProperties().get(0).getDisplay()
                        + ") = " + schemeConditionBean.getProperties().get(0).getDisplay()
                        + " and ym_view_param.set_to("
                        + schemeConditionBean.getProperties().get(1).getDisplay()
                        + ") = " + schemeConditionBean.getProperties().get(1).getDisplay()
                        + ") and ";
            } else if ("YM".equals(schemeConditionBean.getType()) && schemeConditionBean.getProperties().size() == 2) {
                mCondition = mCondition + "("
                        + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                        + schemeConditionBean.getField()
                        + " >= \'" + schemeConditionBean.getProperties().get(0).getDisplay()
                        + "\' and "
                        + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                        + schemeConditionBean.getField()
                        + " <= \'" + schemeConditionBean.getProperties().get(1).getDisplay()
                        + "\') and ";
            } else if ("CBG".equals(schemeConditionBean.getType()) || "C".equals(schemeConditionBean.getType())
                    || "R".equals(schemeConditionBean.getType()) || "EC".equals(schemeConditionBean.getType())) {
                String gridCondition = "";
                int selectedCount = 0;
                for (int j = 0; j < schemeConditionBean.getProperties().size(); j++) {
                    if (schemeConditionBean.getProperties().get(j).isState()) {
                        selectedCount++;
                        gridCondition = gridCondition
                                + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                                + schemeConditionBean.getField() + " = \'"
                                + schemeConditionBean.getProperties().get(j).getValue() + "\' or ";
                    }
                }
                if (selectedCount > 0) {
                    gridCondition = gridCondition.substring(0, gridCondition.length() - 4);

                    mCondition = mCondition + "(" + gridCondition + ") and ";
                }
            } else {
                if (schemeConditionBean.getProperties().size() == 1) {
                    if (!TextUtils.isEmpty(schemeConditionBean.getProperties().get(0).getDisplay())) {
                        mCondition = mCondition + "("
                                + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                                + schemeConditionBean.getField()
                                + " like \'%" + schemeConditionBean.getProperties().get(0).getDisplay()
                                + "%\') and ";
                    }
                }
            }
        }

        if (mCondition.length() >= 5) {
            mCondition = mCondition.substring(0, mCondition.length() - 5);
        }

        Log.d("exactCondition", mCondition);

        mPageIndex = 1;
        mExactWholeLl.setVisibility(View.GONE);
        mGrayBackGround.setVisibility(View.GONE);
        progressDialog.show();
        getSchemeData();
    }

    private void getVoice() {
        RecognizerDialog dialog = new RecognizerDialog(this, null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                String s = mSearchEditText.getText().toString() + CommonUtil.getPlaintext(text);
                mSearchEditText.setText(s);
                mSearchEditText.setSelection(s.length());

                if (b) {
                    searchEvent();
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        dialog.show();

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_SEND
                || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            searchEvent();
            return true;
        }
        return false;
    }

    private void searchEvent() {
        if (!CommonUtil.isNetWorkConnected(this)) {
            ToastMessage(getString(R.string.networks_out));
        } else {
            String searchField = mSearchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(searchField)) {
                if (mfuzzySchemeConditionBeans.size() == 0) {
                    ToastMessage("请前往系统配置模糊搜索字段，或尝试高级查询方式");
                    mSearchEditText.setText("");
                } else {
                    mCondition = "";
                    for (int i = 0; i < mfuzzySchemeConditionBeans.size(); i++) {
                        mCondition = mCondition + "(upper("
                                + (TextUtils.isEmpty(mfuzzySchemeConditionBeans.get(i).getTable()) ? "" : (mfuzzySchemeConditionBeans.get(i).getTable() + "."))
                                + mfuzzySchemeConditionBeans.get(i).getField()
                                + ") like upper(\'%" + searchField + "%\')) or ";
                    }
                    if (mCondition.length() >= 4) {
                        mCondition = mCondition.substring(0, mCondition.length() - 4);
                        mCondition = "(" + mCondition + ")";
                    }

                    Log.d("fuzzyCondition", mCondition);
                }

            } else {
                mCondition = "1 = 1";
            }
            mPageIndex = 1;
            mSearchField = searchField;
            mSearchEditText.clearFocus();
            mFuzzyWholeLl.setVisibility(View.GONE);
            mGrayBackGround.setVisibility(View.GONE);
            mHistoryDataLv.setEnabled(false);
            progressDialog.show();
            getSchemeData();
        }
    }

    private void getQueryCriteria() {
        if (mQueryScheme != null) {
            progressDialog.show();
            String url = CommonUtil.getAppBaseUrl(this) + "mobile/qry/schemeCondition.action";
            Map<String, Object> params = new HashMap<>();
            params.put("caller", mQueryScheme.getCaller());
            params.put("id", mQueryScheme.getSchemeId());
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, mHandler, headers, GET_QUERY_CRITERIA, null, null, "post");
        }
    }

    private void getSchemeData() {
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/qry/schemeResult.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("id", mSchemeId);
        params.put("pageIndex", mPageIndex);
        params.put("pageSize", mPageSize);
        params.put("condition", mCondition);
        params.put("master", CommonUtil.getMaster());
        params.put("sessionUser", CommonUtil.getEmcode());
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, mHandler, headers, GET_DATA_RESULT, null, null, "post");
    }

    public String optStringNotNull(JSONObject json, String key) {
        if (json.isNull(key)) {
            return "";
        } else {
            return json.optString(key, "");
        }
    }

    public long optLongNotNull(JSONObject json, String key) {
        if (json.isNull(key)) {
            return 0;
        } else {
            return json.optLong(key, 0);
        }
    }

    public <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }

    /**
     * 得到本月的第一天
     */
    public static String getMonthFirstDay(String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMinimum(Calendar.DAY_OF_MONTH));
//        calendar.set( Calendar.DATE, 1);
        SimpleDateFormat simpleFormate = new SimpleDateFormat(pattern);
        return simpleFormate.format(calendar.getTime());
    }

    /**
     * 得到本月的最后一天
     */
    public static String getMonthLastDay(String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMaximum(Calendar.DAY_OF_MONTH));
//        calendar.set( Calendar.DATE, 1);
//        calendar.roll(Calendar.DATE, - 1);
        SimpleDateFormat simpleFormate = new SimpleDateFormat(pattern);
        return simpleFormate.format(calendar.getTime());
    }

}
