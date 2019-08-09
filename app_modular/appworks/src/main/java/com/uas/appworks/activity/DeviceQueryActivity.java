package com.uas.appworks.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.dao.historical.HistoricalRecordBean;
import com.core.dao.historical.HistoricalRecordDao;
import com.core.interfac.OnVoiceCompleteListener;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.core.widget.EmptyLayout;
import com.core.widget.MyListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.uas.appworks.CRM.erp.activity.ScanDetailActivity;
import com.uas.appworks.OA.erp.activity.CommonDocDetailsActivity;
import com.uas.appworks.R;
import com.uas.appworks.adapter.DeviceQueryConditionAdapter;
import com.uas.appworks.datainquiry.adapter.DataInquiryFlexAdapter;
import com.uas.appworks.datainquiry.adapter.HistoricalRecordAdapter;
import com.uas.appworks.datainquiry.bean.DataInquiryFlexBean;
import com.uas.appworks.datainquiry.bean.SchemeConditionBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 设备查询
 * @date 2017/12/18 18:58
 */

public class DeviceQueryActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView, View.OnClickListener, View.OnFocusChangeListener {
    private final int OBTAIN_DEVICE_LIST = 0x11;

    private PullToRefreshListView mDataListView;
    private ImageView mTotalImage, mBackImageView, mFilterImageView, mVoiceImageView, mScanImageView;
    private View mGrayBackGround;
    private ClearEditText mSearchEditText;
    private LinearLayout mHistoryWholeLl, mOptionWholeLl, mHistoryLayout, mOptionLayout;
    private TextView mClearHistoryTextView, mOptionCancelTextView, mOptionResetTextView, mOptionConfirmTextView;
    private MyListView mHistoryListView;
    private ListView mOptionListView;
    private List<SchemeConditionBean> mConditionBeans, mResetConditionBeans, mfuzzyConditionBeans;
    private DeviceQueryConditionAdapter mDeviceQueryConditionAdapter;
    private List<HistoricalRecordBean> mHistoricalRecordBeans;
    private HistoricalRecordAdapter mHistoricalRecordAdapter;
    private EmptyLayout mEmptyLayout;
    private Animation mInAnimation, mOutAnimation;

    private String fuzzyHint = "", mCondition = "1 = 1", mBaseCondition = "";
    private int mPageIndex = 1, mPageSize = 25;
    private List<DataInquiryFlexBean> mDeviceQueryFlexBeans;
    private DataInquiryFlexAdapter mDeviceQueryFlexAdapter;
    private List<DataInquiryFlexBean.RowBean.RowChildBean> mRowChildBeans, mAllRowChildBeans;
    private String mFuzzyField = "";
    private int mOldPosition = -1;
    private String mCaller = "Device", mClass, mWhichPage;

    @Override
    protected int getLayout() {
        return R.layout.activity_device_query_list;
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    @Override
    protected void initView() {
        setTitle("");
        View view = LayoutInflater.from(ct).inflate(R.layout.action_data_inquiry_list, null);
        mBackImageView = (ImageView) view.findViewById(R.id.back);
        mFilterImageView = (ImageView) view.findViewById(R.id.data_inquiry_filter_iv);
        mVoiceImageView = (ImageView) view.findViewById(R.id.data_inquiry_voice_iv);
        mSearchEditText = (ClearEditText) view.findViewById(R.id.data_inquiry_filter_et);
        mScanImageView = (ImageView) view.findViewById(R.id.data_inquiry_filter_scan_iv);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(view);

        mDataListView = $(R.id.data_inquiry_list_ptlv);
        mTotalImage = $(R.id.data_inquiry_list_total_spread_iv);
        mGrayBackGround = $(R.id.data_inquiry_list_gray_bg);
        mHistoryWholeLl = $(R.id.data_inquiry_list_fuzzy_ll);
        mHistoryLayout = $(R.id.data_inquiry_list_history_ll);
        mOptionWholeLl = $(R.id.data_inquiry_list_exact_ll);
        mOptionLayout = $(R.id.data_inquiry_list_option_ll);
        mClearHistoryTextView = $(R.id.data_inquiry_history_clear_tv);
        mHistoryListView = $(R.id.data_inquiry_list_history_data_lv);
        mOptionListView = $(R.id.data_inquiry_list_option_lv);
        mOptionCancelTextView = $(R.id.data_inquiry_list_option_cancel_tv);
        mOptionResetTextView = $(R.id.data_inquiry_list_option_reset_tv);
        mOptionConfirmTextView = $(R.id.data_inquiry_list_option_confirm_tv);

        mInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_search_popin);
        mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_search_popout);

        mTotalImage.setVisibility(View.GONE);

        mEmptyLayout = new EmptyLayout(this, mDataListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");

        mHistoricalRecordBeans = new ArrayList<>();
        mHistoricalRecordAdapter = new HistoricalRecordAdapter(this, mHistoricalRecordBeans);
        mHistoryListView.setAdapter(mHistoricalRecordAdapter);

        mDeviceQueryFlexBeans = new ArrayList<>();
        mDeviceQueryFlexAdapter = new DataInquiryFlexAdapter(mContext, mDeviceQueryFlexBeans, true);
        mDataListView.setAdapter(mDeviceQueryFlexAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            mCaller = intent.getStringExtra(Constants.FLAG.DEVICE_CALLER);
            mClass = intent.getStringExtra(Constants.FLAG.DEVICE_CLASS);
            mWhichPage = intent.getStringExtra(Constants.FLAG.DEVICE_WHICH_PAGE);
        }

        if (TextUtils.isEmpty(mCaller)) {
            mCaller = "Device";
        }

        if (TextUtils.isEmpty(mWhichPage)) {
            mWhichPage = Constants.FLAG.DEVICE_FROM_QUERY;
        }

        if (!TextUtils.isEmpty(mClass)) {
            String dc_class = "dc_class";
//            if (mCaller != null && mCaller.equals("DeviceChange!Maintain")) {
//                dc_class = "db_class";
//            }
            mBaseCondition = "(" + dc_class + " = \'" + mClass + "\')";
            mCondition = mBaseCondition;
        }
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {
        mBackImageView.setOnClickListener(this);
        mFilterImageView.setOnClickListener(this);
        mScanImageView.setOnClickListener(this);
        mVoiceImageView.setOnClickListener(this);
        mClearHistoryTextView.setOnClickListener(this);
        mOptionCancelTextView.setOnClickListener(this);
        mOptionResetTextView.setOnClickListener(this);
        mOptionConfirmTextView.setOnClickListener(this);
        mGrayBackGround.setOnClickListener(this);
        mSearchEditText.setOnClickListener(this);

        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSearchEditText.clearFocus();
                mGrayBackGround.setVisibility(View.GONE);
                mOptionWholeLl.setVisibility(View.GONE);
                mHistoryWholeLl.setVisibility(View.GONE);
                mHistoryListView.setEnabled(false);
                CommonUtil.closeKeybord(mSearchEditText, DeviceQueryActivity.this);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mDataListView.setMode(PullToRefreshBase.Mode.BOTH);
        mDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int reallyPosition = (int) parent.getItemIdAtPosition(position);
                if (Constants.FLAG.DEVICE_FROM_COMMON.equals(mWhichPage)) {
                    String keyValue = null, status = null, title = null;
                    if (ListUtils.getSize(mDeviceQueryFlexAdapter.getRowChildBeans()) <= reallyPosition)
                        return;
                    List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = mDeviceQueryFlexAdapter.getRowChildBeans().get(reallyPosition);
                    for (DataInquiryFlexBean.RowBean.RowChildBean rowChildBean : childBeans) {
                        if ("dc_id".equals(rowChildBean.getField())) {
                            keyValue = rowChildBean.getValue();
                        }
                        if ("dc_class".equals(rowChildBean.getField())) {
                            title = rowChildBean.getValue();
                        }
                        if ("dc_status".equals(rowChildBean.getField())) {
                            status = rowChildBean.getValue();
                        }
                        if (keyValue != null && status != null) {
                            break;
                        }
                    }
                    startActivityForResult(new Intent(mContext, CommonDocDetailsActivity.class)
                            .putExtra("caller", mCaller)
                            .putExtra("keyValue", keyValue)
                            .putExtra("title", StringUtil.isEmpty(title) ? "单据" : title)
                            .putExtra("device", true)
                            .putExtra("status", status), 0x981);

                } else if (Constants.FLAG.DEVICE_FROM_QUERY.equals(mWhichPage)) {
                    String decode = null;
                    if (ListUtils.getSize(mDeviceQueryFlexAdapter.getRowChildBeans()) <= reallyPosition) {
                        return;
                    }
                    List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = mDeviceQueryFlexAdapter.getRowChildBeans().get(reallyPosition);
                    for (DataInquiryFlexBean.RowBean.RowChildBean rowChildBean : childBeans) {
                        if ("de_code".equals(rowChildBean.getField())) {
                            decode = rowChildBean.getValue();
                            break;
                        }
                    }

                    startActivityForResult(new Intent(ct, ScanDetailActivity.class).putExtra("decode", decode), 0x22);
                } else {
                    if (CommonUtil.isRepeatClick()) {
                        if (mOldPosition == reallyPosition) {
                            mDeviceQueryFlexAdapter.getObjects().get(reallyPosition).setIsFlex(!mDeviceQueryFlexAdapter.getObjects().get(reallyPosition).isFlex());
                            mDeviceQueryFlexAdapter.notifyDataSetChanged();
                        } else {
                            mOldPosition = reallyPosition;
                        }
                    }
                    mOldPosition = reallyPosition;
                }
            }
        });
        mDataListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mPageIndex = 1;
                    obtainDeviceList();
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
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mPageIndex++;
                    obtainDeviceList();
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

        mSearchEditText.setOnFocusChangeListener(this);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    fuzzySearch();
                    return true;
                }
                return false;
            }
        });

        mHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoricalRecordBean historicalRecordBean = mHistoricalRecordAdapter.getObjects().get(position);
                mSearchEditText.setText(historicalRecordBean.getSearchField());

                fuzzySearch();
            }
        });
    }

    @Override
    protected void initData() {
        mConditionBeans = new ArrayList<>();
        mResetConditionBeans = new ArrayList<>();
        mfuzzyConditionBeans = new ArrayList<>();

        initConditions();
        mDeviceQueryConditionAdapter = new DeviceQueryConditionAdapter(this, mConditionBeans);
        mOptionListView.setAdapter(mDeviceQueryConditionAdapter);
        initFuzzyHint();

        try {
            List<HistoricalRecordBean> historicalRecordBeansById = HistoricalRecordDao.getInstance().getHistoricalRecordBeansById(mCaller);
            if (historicalRecordBeansById.size() > 0) {
                for (int i = 0; i < historicalRecordBeansById.size(); i++) {
                    if (i < 6) {
                        mHistoricalRecordBeans.add(historicalRecordBeansById.get(i));
                    } else {
                        HistoricalRecordDao.getInstance().deleteFromHistoricalRecordByField(mCaller, historicalRecordBeansById.get(i).getSearchField());
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
            obtainDeviceList();
        }
    }

    private void obtainDeviceList() {
        Map<String, Object> params = new HashMap<>();
        params.put("caller", mCaller);
        params.put("condition", mCondition);
        params.put("page", mPageIndex);
        params.put("pageSize", mPageSize);

        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("Cookie", CommonUtil.getErpCookie(mContext));

        HttpParams httpParams = new HttpParams.Builder()
                .url("mobile/common/list.action")
                .method(Method.GET)
                .setHeaders(header)
                .setParams(params)
                .flag(OBTAIN_DEVICE_LIST)
                .build();
        mPresenter.uasRequest(this, httpParams);
    }

    private void initFuzzyHint() {
        for (int i = 0; i < mConditionBeans.size(); i++) {
            if ("S".equals(mConditionBeans.get(i).getType())) {
                fuzzyHint = fuzzyHint + mConditionBeans.get(i).getCaption() + "/";
            }
        }
        if (fuzzyHint.length() > 0) {
            fuzzyHint = fuzzyHint.substring(0, fuzzyHint.length() - 1);
            mSearchEditText.setHint(fuzzyHint);
        } else {
            mSearchEditText.setHint("搜索");
        }
    }

    private void initConditions() {
        if ("Device".equals(mCaller)) {
            SchemeConditionBean schemeConditionBean = null;

            schemeConditionBean = initCondition("设备编号", "de_code", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("设备名称", "de_name", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("设备规格", "de_spec", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);


            schemeConditionBean = initCondition("设备种类", "de_kind", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);


            schemeConditionBean = initCondition("设备类型", "de_type", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("所属线别", "de_currentlinecode", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("所属部门", "de_currentcentercode", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("使用人员", "de_currentuser", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("设备管理员", "de_manageman", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = new SchemeConditionBean();
            schemeConditionBean.setCaption("状态");
            schemeConditionBean.setField("de_runstatus");
            schemeConditionBean.setType("CBG");

            List<SchemeConditionBean.Property> properties = new ArrayList<>();
            SchemeConditionBean.Property property = new SchemeConditionBean.Property();
            property.setDisplay("闲置中");
            property.setValue("UNUSED");
            properties.add(property);

            property = new SchemeConditionBean.Property();
            property.setDisplay("正常使用");
            property.setValue("USING");
            properties.add(property);

            property = new SchemeConditionBean.Property();
            property.setDisplay("故障中");
            property.setValue("BREAKIND");
            properties.add(property);

            property = new SchemeConditionBean.Property();
            property.setDisplay("已报废");
            property.setValue("SCRAPPED");
            properties.add(property);

            property = new SchemeConditionBean.Property();
            property.setDisplay("已盘亏");
            property.setValue("LOSSED");
            properties.add(property);

            schemeConditionBean.setProperties(properties);
            mConditionBeans.add(schemeConditionBean);
        } else {
            SchemeConditionBean schemeConditionBean
                    = initCondition("申请单号", "dc_code", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

            schemeConditionBean = initCondition("设备编号", "dc_devcode", "S");
            mConditionBeans.add(schemeConditionBean);
            mfuzzyConditionBeans.add(schemeConditionBean);

        }
        try {
            mResetConditionBeans = CommonUtil.deepCopy(mConditionBeans);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x981) {
            mPageIndex = 1;
            obtainDeviceList();
        } else if (requestCode == 0x22) {
            if (data != null) {
                showDialog(data.getStringExtra("data"));
            }
        } else if (requestCode == 0x33 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Log.d("scanurl", result);
                    startActivityForResult(new Intent(ct, ScanDetailActivity.class).
                            putExtra("decode", result), 0x34);
                }
            }
        } else if (requestCode == 0x34) {
            showDialog(data.getStringExtra("data"));
        }
    }

    private void showDialog(String message) {
        if (StringUtil.isEmpty(message)) return;
        new MaterialDialog.Builder(ct)
                .title(R.string.app_dialog_title)
                .content(message)
                .positiveText(MyApplication.getInstance().getString(R.string.app_dialog_ok))
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @NonNull
    private SchemeConditionBean initCondition(String caption, String field, String type) {
        ArrayList<SchemeConditionBean.Property> properties = new ArrayList<>();
        SchemeConditionBean.Property property = new SchemeConditionBean.Property();
        property.setValue("");
        property.setDisplay("");
        properties.add(property);

        SchemeConditionBean schemeConditionBean = new SchemeConditionBean();
        schemeConditionBean.setCaption(caption);
        schemeConditionBean.setField(field);
        schemeConditionBean.setType(type);
        schemeConditionBean.setProperties(properties);
        return schemeConditionBean;
    }

    private void fuzzySearch() {
        if (!CommonUtil.isNetWorkConnected(this)) {
            ToastMessage(getString(R.string.networks_out));
        } else {
            String fuzzyField = mSearchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(fuzzyField)) {
                if (mfuzzyConditionBeans.size() == 0) {
                    ToastMessage("请前往系统配置模糊搜索字段，或尝试高级查询方式");
                    mSearchEditText.setText("");
                } else {
                    mCondition = "";
                    for (int i = 0; i < mfuzzyConditionBeans.size(); i++) {
                        mCondition = mCondition + "(upper("
                                + (TextUtils.isEmpty(mfuzzyConditionBeans.get(i).getTable()) ? "" : (mfuzzyConditionBeans.get(i).getTable() + "."))
                                + mfuzzyConditionBeans.get(i).getField()
                                + ") like upper(\'%" + fuzzyField + "%\')) or ";
                    }
                    if (mCondition.length() >= 4) {
                        mCondition = mCondition.substring(0, mCondition.length() - 4);
                        mCondition = "(" + mCondition + ")";
                    }
                    if (!TextUtils.isEmpty(mClass)) {
                        mCondition = mBaseCondition + (TextUtils.isEmpty(mCondition) ? "" : " and " + mCondition);
                    }
                    Log.d("fuzzyCondition", mCondition);
                }

            } else {
                if (!TextUtils.isEmpty(mClass)) {
                    mCondition = mBaseCondition;
                } else {
                    mCondition = "1 = 1";
                }
            }
            mPageIndex = 1;
            mFuzzyField = fuzzyField;
            mSearchEditText.clearFocus();
            mHistoryWholeLl.setVisibility(View.GONE);
            mGrayBackGround.setVisibility(View.GONE);
            mHistoryListView.setEnabled(false);
            obtainDeviceList();
        }
    }

    @Override
    public void showLoading(String loadStr) {
        if (!mDataListView.isRefreshing()) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
        if (mDataListView.isRefreshing()) {
            mDataListView.onRefreshComplete();
        }
    }

    @Override
    public void requestSuccess(int what, Object object) {
        if (what == OBTAIN_DEVICE_LIST) {
            String result = object.toString();
            if (result != null) {
                LogUtil.prinlnLongMsg("deviceQuery", result);
                if (mPageIndex == 1) {
                    mDeviceQueryFlexBeans.clear();
                }
                try {
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray listdataArray = resultObject.getJSONArray("listdata");
                    JSONArray columnsArray = resultObject.getJSONArray("columns");
                    if (listdataArray == null || listdataArray.size() == 0) {
                        mDeviceQueryFlexAdapter.notifyDataSetChanged();
                        if (mPageIndex == 1) {
                            mEmptyLayout.showEmpty();
                        }
                        return;
                    }
                    if (columnsArray == null || columnsArray.size() == 0) {
                        mDeviceQueryFlexAdapter.notifyDataSetChanged();
                        if (mPageIndex == 1) {
                            mEmptyLayout.showEmpty();
                        }
                        return;
                    }

                    List<SchemeConditionBean> columnsConditionBeans = new ArrayList<>();
                    for (int i = 0; i < columnsArray.size(); i++) {
                        JSONObject columnsObject = columnsArray.getJSONObject(i);
                        if (columnsObject != null) {
                            int width = JSONUtil.getInt(columnsObject, "width");
//                            if (width > 0) {
                            SchemeConditionBean columnsConditionBean = new SchemeConditionBean();
                            columnsConditionBean.setCaption(JSONUtil.getText(columnsObject, "caption"));
                            columnsConditionBean.setField(JSONUtil.getText(columnsObject, "dataIndex"));
                            columnsConditionBean.setWidth(JSONUtil.getInt(columnsObject, "width"));
                            columnsConditionBean.setType(JSONUtil.getText(columnsObject, "type"));

                            columnsConditionBeans.add(columnsConditionBean);
//                            }
                        }
                    }

                    if (columnsConditionBeans.size() == 0) {
                        mDeviceQueryFlexAdapter.notifyDataSetChanged();
                        if (mPageIndex == 1) {
                            mEmptyLayout.showEmpty();
                        }
                        return;
                    }

                    /**
                     * 存储查询历史记录
                     */
                    if (!TextUtils.isEmpty(mFuzzyField)) {
                        HistoricalRecordBean historicalRecordBean = new HistoricalRecordBean();
                        historicalRecordBean.setSchemeId(mCaller);
                        historicalRecordBean.setSchemeName(mCaller);
                        historicalRecordBean.setSearchField(mFuzzyField);
                        HistoricalRecordDao.getInstance().saveToHistoricalRecord(historicalRecordBean);

                        mHistoricalRecordBeans.clear();
                        List<HistoricalRecordBean> historicalRecordBeansById
                                = HistoricalRecordDao.getInstance().getHistoricalRecordBeansById(mCaller);
                        if (historicalRecordBeansById.size() > 0) {
                            for (int i = 0; i < historicalRecordBeansById.size(); i++) {
                                if (i < 6) {
                                    mHistoricalRecordBeans.add(historicalRecordBeansById.get(i));
                                }
                            }
                            mHistoricalRecordAdapter.notifyDataSetChanged();
                        }
                    }

                    /**
                     * 列表数据解析
                     */
                    List<List<DataInquiryFlexBean.RowBean.RowChildBean>> rowChildBeans = new ArrayList<>();
                    for (int i = 0; i < listdataArray.size(); i++) {
                        mRowChildBeans = new ArrayList<>();
                        mAllRowChildBeans = new ArrayList<>();
                        JSONObject listdataObject = listdataArray.getJSONObject(i);
                        if (listdataObject != null) {
                            DataInquiryFlexBean deviceQueryFlexBean = new DataInquiryFlexBean();
                            deviceQueryFlexBean.setIsFlex(false);

                            List<DataInquiryFlexBean.RowBean> rowBeans = new ArrayList<>();

                            for (int j = 0; j < columnsConditionBeans.size(); j++) {
                                int width = columnsConditionBeans.get(j).getWidth();
                                String field = columnsConditionBeans.get(j).getField();
                                String type = columnsConditionBeans.get(j).getType();
                                String caption = columnsConditionBeans.get(j).getCaption();

                                String value = JSONUtil.getText(listdataObject, field);

                                if (value != null) {
                                    DataInquiryFlexBean.RowBean.RowChildBean rowChildBean = new DataInquiryFlexBean.RowBean.RowChildBean();
                                    rowChildBean.setCaption(caption);
                                    rowChildBean.setValue(value);
                                    rowChildBean.setField(field);
                                    rowChildBean.setWidth(width);

                                    mAllRowChildBeans.add(rowChildBean);
                                    if (width > 0) {
                                        mRowChildBeans.add(rowChildBean);
                                    }
                                }
                            }
                            rowChildBeans.add(mAllRowChildBeans);

                            for (int k = 0; k < mRowChildBeans.size(); k++) {
                                DataInquiryFlexBean.RowBean rowBean = new DataInquiryFlexBean.RowBean();

                                List<DataInquiryFlexBean.RowBean.RowChildBean> childBeans = new ArrayList<>();

                                DataInquiryFlexBean.RowBean.RowChildBean rowChildBean1 = mRowChildBeans.get(k);
                                childBeans.add(rowChildBean1);
                                if (rowChildBean1.getWidth() <= 100 && k < (mRowChildBeans.size() - 1)) {
                                    DataInquiryFlexBean.RowBean.RowChildBean rowChildBean2 = mRowChildBeans.get(k + 1);
                                    if (rowChildBean2.getWidth() <= 100) {
                                        childBeans.add(rowChildBean2);
                                        k++;
                                    }
                                }

                                rowBean.setRowChildBeans(childBeans);

                                rowBeans.add(rowBean);
                            }
                            deviceQueryFlexBean.setRowBeans(rowBeans);
                            mDeviceQueryFlexBeans.add(deviceQueryFlexBean);
                        }
                    }
                    mDeviceQueryFlexAdapter.setRowChildBeans(rowChildBeans);
                    mDeviceQueryFlexAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    mDeviceQueryFlexAdapter.notifyDataSetChanged();
                    if (mDeviceQueryFlexBeans.size() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        if (what == OBTAIN_DEVICE_LIST) {
            ToastMessage(errorMsg);
            if (mPageIndex > 1) {
                mPageIndex--;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.data_inquiry_filter_iv) {
            if (mHistoryWholeLl.getVisibility() == View.VISIBLE) {
                mHistoryWholeLl.setVisibility(View.GONE);
                mHistoryListView.setEnabled(false);
            }
            mSearchEditText.clearFocus();
            mFuzzyField = "";
            if (mConditionBeans.size() > 0) {
                if (mOptionWholeLl.getVisibility() == View.VISIBLE) {
                    mOptionLayout.startAnimation(mOutAnimation);
                    mGrayBackGround.setVisibility(View.GONE);
                } else {
                    mDeviceQueryConditionAdapter.resetTouchPosition();
                    mOptionLayout.startAnimation(mInAnimation);
                    mOptionWholeLl.setVisibility(View.VISIBLE);
                    mGrayBackGround.setVisibility(View.VISIBLE);
                }
            }
        } else if (i == R.id.back) {
            onBackPressed();
        } else if (i == R.id.data_inquiry_voice_iv) {
            CommonUtil.getVoiceText(this, mSearchEditText, new OnVoiceCompleteListener() {
                @Override
                public void onVoiceComplete(String text) {
                    fuzzySearch();
                }
            });
        } else if (i == R.id.data_inquiry_history_clear_tv) {
            HistoricalRecordDao.getInstance().deleteFromHistoricalRecordById(mCaller);
            mHistoricalRecordBeans.clear();
            mHistoricalRecordAdapter.notifyDataSetChanged();
            mHistoryWholeLl.setVisibility(View.GONE);
            mGrayBackGround.setVisibility(View.GONE);
        } else if (i == R.id.data_inquiry_list_option_cancel_tv) {
            mGrayBackGround.setVisibility(View.GONE);
            mOptionWholeLl.setVisibility(View.GONE);
            CommonUtil.closeKeybord(mSearchEditText, DeviceQueryActivity.this);
        } else if (i == R.id.data_inquiry_list_option_reset_tv) {
            mConditionBeans.clear();
            try {
                List<SchemeConditionBean> conditionBeans = CommonUtil.deepCopy(mResetConditionBeans);
                if (conditionBeans != null) {
                    mConditionBeans.addAll(conditionBeans);
                    mDeviceQueryConditionAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (i == R.id.data_inquiry_list_option_confirm_tv) {
            mGrayBackGround.setVisibility(View.GONE);
            if (!CommonUtil.isNetWorkConnected(this)) {
                ToastMessage(getString(R.string.networks_out));
            } else {
                exactQuery();
            }
        } else if (i == R.id.data_inquiry_list_gray_bg) {
            CommonUtil.closeKeybord(mSearchEditText, DeviceQueryActivity.this);
            mGrayBackGround.setVisibility(View.GONE);
            if (mHistoryWholeLl.getVisibility() == View.VISIBLE) {
                mHistoryLayout.startAnimation(mOutAnimation);
            }
            if (mOptionWholeLl.getVisibility() == View.VISIBLE) {
                mOptionLayout.startAnimation(mOutAnimation);
            }
        } else if (i == R.id.data_inquiry_filter_et) {
            if (mOptionWholeLl.getVisibility() == View.VISIBLE) {
                mOptionWholeLl.setVisibility(View.GONE);
            }
            if (mHistoricalRecordAdapter.getObjects().size() > 0) {
                if (mGrayBackGround.getVisibility() == View.GONE) {
                    mGrayBackGround.setVisibility(View.VISIBLE);
                }
                if (mHistoryWholeLl.getVisibility() == View.GONE) {
                    mHistoryWholeLl.setVisibility(View.VISIBLE);
                    mHistoryLayout.startAnimation(mInAnimation);
                    mHistoryListView.setEnabled(true);
                }
            } else {
                if (mGrayBackGround.getVisibility() == View.VISIBLE) {
                    mGrayBackGround.setVisibility(View.GONE);
                }
            }
        } else if (i == R.id.data_inquiry_filter_scan_iv) {
            requestPermission(Manifest.permission.CAMERA, new Runnable() {
                @Override
                public void run() {
                    startActivityForResult(new Intent(DeviceQueryActivity.this, CaptureActivity.class)
                            , 0x33);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(ct, R.string.not_camera_permission);
                }
            });
        }
    }

    private void exactQuery() {
        mCondition = "";
        for (int i = 0; i < mConditionBeans.size(); i++) {
            SchemeConditionBean schemeConditionBean = mConditionBeans.get(i);
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
                        mCondition = mCondition + "(upper("
                                + (TextUtils.isEmpty(schemeConditionBean.getTable()) ? "" : (schemeConditionBean.getTable() + "."))
                                + schemeConditionBean.getField()
                                + ") like upper(\'%" + schemeConditionBean.getProperties().get(0).getDisplay()
                                + "%\')) and ";
                    }
                }
            }
        }

        if (mCondition.length() >= 5) {
            mCondition = mCondition.substring(0, mCondition.length() - 5);
            mCondition = "(" + mCondition + ")";
        }
        if (!TextUtils.isEmpty(mClass)) {
            mCondition = mBaseCondition + (TextUtils.isEmpty(mCondition) ? "" : " and " + mCondition);
        }
        Log.d("exactCondition", mCondition);
        mPageIndex = 1;
        mOptionWholeLl.setVisibility(View.GONE);
        mGrayBackGround.setVisibility(View.GONE);
        obtainDeviceList();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            if (mOptionWholeLl.getVisibility() == View.VISIBLE) {
                mOptionWholeLl.setVisibility(View.GONE);
            }
            if (mHistoricalRecordAdapter.getObjects().size() > 0) {
                mGrayBackGround.setVisibility(View.VISIBLE);
                if (mHistoryWholeLl.getVisibility() == View.GONE) {
                    mHistoryWholeLl.setVisibility(View.VISIBLE);
                    mHistoryLayout.startAnimation(mInAnimation);
                    mHistoryListView.setEnabled(true);
                }
            } else {
                mGrayBackGround.setVisibility(View.GONE);
            }

        } else {
            CommonUtil.closeKeybord(mSearchEditText, DeviceQueryActivity.this);
            if (mHistoryWholeLl.getVisibility() == View.VISIBLE) {
                mHistoryWholeLl.setVisibility(View.GONE);
                mGrayBackGround.setVisibility(View.GONE);
                mHistoryListView.setEnabled(false);
            }

        }
    }

}
