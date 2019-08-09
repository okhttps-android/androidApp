package com.uas.appworks.datainquiry.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.system.PermissionUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.adapter.ReportQueryConditionAdapter;
import com.uas.appworks.datainquiry.bean.GridMenuReportStatisticsBean;
import com.uas.appworks.datainquiry.bean.ReportConditionBean;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2017/8/16.
 * 报表统计查询页面
 */
public class ReportQueryCriteriaActivity extends BaseActivity implements View.OnClickListener {
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 0X22;
    private final int GET_OPTION_DATA = 0X11;
    private ListView mOptionListView;
    private TextView mCancelTextView, mResetTextView, mConfirmTextView;
    private GridMenuReportStatisticsBean.ListBean mReportInfo;
    private String mCondition = "", mDefaultCondition = "";
    private List<ReportConditionBean> mReportConditionBeans, mResetReportConditionBeans;
    private ReportQueryConditionAdapter mReportQueryConditionAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_OPTION_DATA:
                    progressDialog.dismiss();
                    String result = msg.getData().getString("result");
                    if (result != null) {
                        LogUtil.prinlnLongMsg("querycriteria", msg.getData().getString("result"));
                        try {
                            JSONObject resultObject = new JSONObject(result);
                            mDefaultCondition = optStringNotNull(resultObject, "defaultCondition");
                            JSONArray conditionArray = resultObject.optJSONArray("listCondition");
                            if (conditionArray != null) {
                                String fuzzyHint = "";
                                for (int i = 0; i < conditionArray.length(); i++) {
                                    JSONObject conditionObject = conditionArray.optJSONObject(i);
                                    if (conditionObject != null) {
                                        ReportConditionBean reportConditionBean = new ReportConditionBean();
                                        String field = optStringNotNull(conditionObject, "field");
                                        String title = optStringNotNull(conditionObject, "title");
                                        String type = optStringNotNull(conditionObject, "type");
                                        String readOnly = optStringNotNull(conditionObject, "readOnly");

                                        if (!conditionObject.isNull("properties") && ("CBG".equals(type) || "EC".equals(type) || "C".equals(type) || "R".equals(type))) {
                                            JSONArray properties = conditionObject.optJSONArray("properties");
                                            if (properties != null) {
                                                List<ReportConditionBean.Property> propertyList = new ArrayList<>();
                                                for (int j = 0; j < properties.length(); j++) {
                                                    JSONObject propertyObject = properties.optJSONObject(j);
                                                    ReportConditionBean.Property property = new ReportConditionBean.Property();
                                                    String value = optStringNotNull(propertyObject, "value");
                                                    String display = optStringNotNull(propertyObject, "display");

                                                    property.setDisplay(display);
                                                    property.setValue(value);
                                                    property.setState(false);

                                                    if (!"$ALL".equals(display) && !"$ALL".equals(value))
                                                        propertyList.add(property);
                                                }
                                                reportConditionBean.setProperties(propertyList);
                                            }
                                        } else {
                                            ArrayList<ReportConditionBean.Property> properties = new ArrayList<>();
                                            if ("N".equals(type)) {
                                                for (int m = 0; m < 2; m++) {
                                                    ReportConditionBean.Property property = new ReportConditionBean.Property();
                                                    properties.add(property);
                                                }
                                            } else if ("D".equals(type) || "CD".equals(type)) {
                                                for (int m = 0; m < 2; m++) {
                                                    ReportConditionBean.Property property = new ReportConditionBean.Property();
                                                    if (m == 0) {
                                                        property.setDisplay(getMonthFirstDay());
                                                        property.setValue(getMonthFirstDay());
                                                    } else if (m == 1) {
                                                        property.setDisplay(getMonthLastDay());
                                                        property.setValue(getMonthLastDay());
                                                    }
                                                    properties.add(property);
                                                }
                                            } else if ("CBG".equals(type) || "C".equals(type) || "R".equals(type)) {

                                            } else {
                                                ReportConditionBean.Property property = new ReportConditionBean.Property();
                                                properties.add(property);
                                            }
                                            reportConditionBean.setProperties(properties);
                                        }

                                        reportConditionBean.setField(field);
                                        reportConditionBean.setTitle(title);
                                        reportConditionBean.setType(type);
                                        reportConditionBean.setReadOnly(readOnly);

                                        mReportConditionBeans.add(reportConditionBean);
                                    }
                                }

                                try {
                                    mResetReportConditionBeans = deepCopy(mReportConditionBeans);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }

                                mReportQueryConditionAdapter.notifyDataSetChanged();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    ToastMessage(msg.getData().getString("result"));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_query_criteria);

        initViews();
        initEvents();
        initDatas();
    }

    private void initViews() {
        setTitle(R.string.report_query);

        mOptionListView = (ListView) findViewById(R.id.report_query_criteria_lv);
        mCancelTextView = (TextView) findViewById(R.id.report_query_criteria_cancel_btn);
        mResetTextView = (TextView) findViewById(R.id.report_query_criteria_reset_btn);
        mConfirmTextView = (TextView) findViewById(R.id.report_query_criteria_confirm_btn);

        mReportConditionBeans = new ArrayList<>();
        mReportQueryConditionAdapter = new ReportQueryConditionAdapter(this, mReportConditionBeans);
        mOptionListView.setAdapter(mReportQueryConditionAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            mReportInfo = (GridMenuReportStatisticsBean.ListBean) intent.getSerializableExtra("reportinfo");
        }

        if (mReportInfo != null) {
            setTitle(mReportInfo.getTitle() == null ? "" : mReportInfo.getTitle());
        }
    }

    private void initEvents() {
        mCancelTextView.setOnClickListener(this);
        mResetTextView.setOnClickListener(this);
        mConfirmTextView.setOnClickListener(this);
    }

    private void initDatas() {
        if (!CommonUtil.isNetWorkConnected(this)) {
            ToastMessage(getString(R.string.networks_out));
        } else {
            if (mReportInfo != null) {
                progressDialog.show();
                String url = CommonUtil.getAppBaseUrl(this) + "mobile/qry/reportCondition.action";
                Map<String, Object> params = new HashMap<>();
                params.put("caller", mReportInfo.getCaller());
                params.put("title", mReportInfo.getTitle());
                LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                ViewUtil.httpSendRequest(this, url, params, mHandler, headers, GET_OPTION_DATA, null, null, "post");
            } else {
                ToastMessage("方案信息获取失败！");
            }
        }


    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.report_query_criteria_confirm_btn) {
            if (PermissionUtil.lacksPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PermissionUtil.requestPermission(this, REQUEST_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                gotoPDFLoadActivity();
            }


        } else if (i == R.id.report_query_criteria_reset_btn) {
            mReportConditionBeans.clear();
            try {
                List<ReportConditionBean> conditionBeans = deepCopy(mResetReportConditionBeans);
                if (conditionBeans != null) {
                    mReportConditionBeans.addAll(conditionBeans);
                    mReportQueryConditionAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (i == R.id.report_query_criteria_cancel_btn) {
            onBackPressed();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoPDFLoadActivity();
            } else {
//                new AlertDialog.Builder(this).setTitle(R.string.prompt_title)
//                        .setMessage("查看报表文件需要开启读写手机权限")
//                        .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = getAppDetailSettingIntent(ReportQueryCriteriaActivity.this);
//                                startActivity(intent);
//                            }
//                        })
//                        .setNegativeButton(R.string.cancel, null)
//                        .create().show();
                ToastMessage("查看报表文件需要开启读写手机权限,请在设置中手动开启");
            }
        }
    }


    private void gotoPDFLoadActivity() {
        mCondition = "";
        for (int i = 0; i < mReportConditionBeans.size(); i++) {
            ReportConditionBean reportConditionBean = mReportConditionBeans.get(i);
            if ("N".equals(reportConditionBean.getType()) && reportConditionBean.getProperties().size() == 2) {
                if (!TextUtils.isEmpty(reportConditionBean.getProperties().get(0).getDisplay())
                        && !TextUtils.isEmpty(reportConditionBean.getProperties().get(1).getDisplay())) {
                    mCondition = mCondition + "(" + reportConditionBean.getField()
                            + " >= " + reportConditionBean.getProperties().get(0).getDisplay()
                            + " and " + reportConditionBean.getField()
                            + " <= " + reportConditionBean.getProperties().get(1).getDisplay() + ") and ";

                } else if (!TextUtils.isEmpty(reportConditionBean.getProperties().get(0).getDisplay())) {
                    mCondition = mCondition + "(" + reportConditionBean.getField()
                            + " >= " + reportConditionBean.getProperties().get(0).getDisplay() + ") and ";
                } else if (!TextUtils.isEmpty(reportConditionBean.getProperties().get(1).getDisplay())) {
                    mCondition = mCondition + "(" + reportConditionBean.getField()
                            + " <= " + reportConditionBean.getProperties().get(1).getDisplay() + ") and ";
                }

            } else if (("D".equals(reportConditionBean.getType()) || "CD".equals(reportConditionBean.getType())) && reportConditionBean.getProperties().size() == 2) {
                mCondition = mCondition + "(" + reportConditionBean.getField()
                        + " >= to_date(\'" + reportConditionBean.getProperties().get(0).getDisplay()
                        + "\',\'yyyy-MM-dd\') and " + reportConditionBean.getField()
                        + " <= to_date(\'" + reportConditionBean.getProperties().get(1).getDisplay()
                        + "\',\'yyyy-MM-dd\')) and ";
            } else if ("CBG".equals(reportConditionBean.getType()) || "C".equals(reportConditionBean.getType())
                    || "R".equals(reportConditionBean.getType()) || "EC".equals(reportConditionBean.getType())) {
                String gridCondition = "";
                int selectedCount = 0;
                for (int j = 0; j < reportConditionBean.getProperties().size(); j++) {
                    if (reportConditionBean.getProperties().get(j).isState()) {
                        selectedCount++;
                        gridCondition = gridCondition + reportConditionBean.getField() + " = \'"
                                + reportConditionBean.getProperties().get(j).getValue() + "\' or ";
                    }
                }
                if (selectedCount > 0) {
                    gridCondition = gridCondition.substring(0, gridCondition.length() - 4);

                    mCondition = mCondition + "(" + gridCondition + ") and ";
                }
            } else {
                if (reportConditionBean.getProperties().size() == 1) {
                    if (!TextUtils.isEmpty(reportConditionBean.getProperties().get(0).getDisplay())) {
                        mCondition = mCondition + "(" + reportConditionBean.getField()
                                + " = \'" + reportConditionBean.getProperties().get(0).getDisplay() + "\')"
                                + " and ";
                    }
                }
            }
        }

        if (mCondition.length() >= 5) {
            mCondition = mCondition.substring(0, mCondition.length() - 5);
        }

        Log.d("exactCondition", mCondition);
        Log.d("defaultCondition", mDefaultCondition);


        Intent intent = new Intent();
        intent.setClass(this, PDFDownloadActivity.class);
        if (mCondition.length() != 0) {
            if (!TextUtils.isEmpty(mDefaultCondition)) {
                intent.putExtra("condition", mCondition + " and " + mDefaultCondition);
            } else {
                intent.putExtra("condition", mCondition);
            }
        } else if (!TextUtils.isEmpty(mDefaultCondition)) {
            intent.putExtra("condition", mDefaultCondition);
        } else {
//            ToastMessage("请确定查询条件!");
//            return;
            intent.putExtra("condition", "1 = 1");
        }
        if (mReportInfo != null) {
            intent.putExtra("reportName", mReportInfo.getReportName());
            intent.putExtra("title", mReportInfo.getTitle());
        }
        startActivity(intent);
    }

    public String optStringNotNull(JSONObject json, String key) {
        if (json.isNull(key)) {
            return "";
        } else {
            return json.optString(key, "");
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
    public static String getMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMinimum(Calendar.DAY_OF_MONTH));
//        calendar.set( Calendar.DATE, 1);
        SimpleDateFormat simpleFormate = new SimpleDateFormat("yyyy-MM-dd");
        return simpleFormate.format(calendar.getTime());
    }

    /**
     * 得到本月的最后一天
     */
    public static String getMonthLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMaximum(Calendar.DAY_OF_MONTH));
//        calendar.set( Calendar.DATE, 1);
//        calendar.roll(Calendar.DATE, - 1);
        SimpleDateFormat simpleFormate = new SimpleDateFormat("yyyy-MM-dd");
        return simpleFormate.format(calendar.getTime());
    }


    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    public static Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }
}
