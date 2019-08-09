package com.uas.appworks.OA.erp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.aip.excep.activity.RealTimeDetectFaceActivty;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.OAConfig;
import com.core.model.WorkModel;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.OASigninPicker;
import com.lidroid.xutils.ViewUtils;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.apputils.utils.WifiReceiverUtil;
import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.appworks.OA.erp.activity.form.WorkLogsActivity;
import com.uas.appworks.OA.erp.adapter.WorkAdapter;
import com.uas.appworks.OA.erp.presenter.WorkPresenter;
import com.uas.appworks.OA.erp.view.IWorkView;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 整改后的签到界面
 * <p>
 * create by Bitliker by 2016/12/04
 */
public class WorkActivity extends OABaseActivity implements View.OnClickListener, IWorkView {

    public static final int ALLEGEDLY = 0x16;
    private final int PHONE_CHANGE = 0x15;
    private final int ADDRESS_CHANGE = 0x14;
    private RecyclerView listview;//列表
    private TextView office_addr;//当前位置
    private TextView unoffice_mm;//距离
    private TextView date_tv;//当前日期
    private View line;//当前日期
    private RelativeLayout empty_rl;//当前日期
    private TextView empty_tv;//当前日期

    private PopupWindow setWindow = null;//
    private WorkAdapter adapter;
    private boolean isToday = true;
    private WorkPresenter presenter;
    private String macAddress;
    private String macError;//变更mac地址
    private long selectTime;
    private boolean isAdmin = false;
    private boolean isChangePoi = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (presenter != null && !isChangePoi) presenter.upDateLocation();
        }
    };
    private OnLocationListener mOnLocationListener = new OnLocationListener() {
        @Override
        public void onReceiveLocation(UASLocation mUASLocation) {
            if (presenter != null && !isChangePoi) presenter.upDateLocation();
        }
    };

    public OnLocationListener getOnLocationListener() {
        return mOnLocationListener;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signin_set, menu);
        menu.getItem(0).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.title) {
            showPopupWindow();
        } else if (item.getItemId() == R.id.oa_leave) {
            startActivity(new Intent(ct, WorkLogsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(ct).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOCATION_CHANGE);
        LocalBroadcastManager.getInstance(ct).registerReceiver(receiver, filter);
        ViewUtils.inject(this);
        initView();
        initLcation();
    }

    @Override
    protected void onResume() {
        UasLocationHelper.getInstance().requestLocation(mOnLocationListener);
        super.onResume();
    }

    boolean isRuning = false;

    private void initLcation() {
        isRuning = true;
    }

    private void initView() {
        String mMasterName = CommonUtil.getSharedPreferences(ct, "Master_ch");
        if (StringUtil.isEmpty(mMasterName)) {
            setTitle(R.string.activity_signing);
        } else {
            setTitle(mMasterName);
        }
        isAdmin = getIntent() == null ? false : getIntent().getBooleanExtra(AppConfig.IS_ADMIN, false);
        listview = (RecyclerView) findViewById(R.id.listview);
        empty_tv = (TextView) findViewById(R.id.empty_tv);
        office_addr = (TextView) findViewById(R.id.office_addr);
        unoffice_mm = (TextView) findViewById(R.id.unoffice_mm);
        line = findViewById(R.id.line);
        empty_rl = (RelativeLayout) findViewById(R.id.empty_rl);
        date_tv = (TextView) findViewById(R.id.date_tv);
        selectTime = System.currentTimeMillis();
        getMac();
        presenter = new WorkPresenter(this, macAddress);
        adapter = new WorkAdapter(ct);
        listview.setLayoutManager(new LinearLayoutManager(ct));
        listview.setAdapter(adapter);
        date_tv.setText(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy年MM月dd日") + " " +
                CalendarUtil.getWeek(System.currentTimeMillis()));
        findViewById(R.id.signin_btn).setOnClickListener(this);
        findViewById(R.id.unoffice_).setOnClickListener(this);
        date_tv.setOnClickListener(this);
        presenter.start(true);
    }

    @Override
    public void showDistance(float distance) {
        if (distance == -1)
            unoffice_mm.setText("");
        else if (distance != 0)
            unoffice_mm.setText("约" + distance + "米");
        else unoffice_mm.setText("约0米");
    }

    @Override
    public void showLocation(String location) {
        if (!StringUtil.isEmpty(location))
            office_addr.setText(location);

    }


    @Override
    public void showModel(boolean isFree, List<WorkModel> models) {
        if (isFree) {
            line.setVisibility(View.GONE);
            if (ListUtils.isEmpty(models)) {
                showEmptyView(true, true);
            } else {
                showEmptyView(false, true);
                adapter.setModels(isToday, isFree, selectTime, models);
            }
        } else {
            if (ListUtils.isEmpty(models)) {
                showEmptyView(true, false);
                line.setVisibility(View.GONE);
            } else {
                showEmptyView(false, false);
                line.setVisibility(View.VISIBLE);
            }
            adapter.setModels(isToday, isFree, selectTime, models);
        }

    }


    @Override
    public void showFristMac() {
        showDialog(1, getString(R.string.show_frist_mac));
    }

    @Override
    public void showErrorMac() {
        showDialog(2, getString(R.string.other_phone_error));
    }

    @Override
    public void setErrorMac(String message) {
        macError = message;
    }

    /**
     * 显示提示框
     *
     * @param type    1.第一次绑定mac  2.mac错误，进入修改
     * @param message
     */
    private void showDialog(final int type, final String message) {
        PopupWindowHelper.showAlart(this, getString(R.string.app_name), message, new PopupWindowHelper.OnSelectListener() {
            @Override
            public void select(boolean selectOk) {

                if (selectOk) {
                    switch (type) {
                        case 1:
                            presenter.submitByFrist((ArrayList<WorkModel>) adapter.getModels(), macAddress);
                            break;
                        case 2:
                            Intent intent = new Intent(ct, ChangeMobileActivity.class);
                            intent.putExtra("macAddress", macAddress);
                            startActivityForResult(intent, PHONE_CHANGE);
                            break;
                    }

                }
            }
        });
    }


    private MaterialDialog notLocationDialog = null;

    @Override
    public void showNotLocation() {
        if (notLocationDialog == null)
            notLocationDialog = new MaterialDialog.Builder(ct).title(R.string.prompt_title).
                    content(R.string.allow_location)
                    .positiveText(R.string.common_sure).
                            autoDismiss(false).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                        }
                    }).build();
        if (!notLocationDialog.isShowing())
            notLocationDialog.show();
    }

    @Override
    public void setPois(List<PoiInfo> pois, LatLng latLng) {
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;

        if (view.getId() == R.id.signin_btn) {//点击打卡按钮
            if (OAConfig.needValidateFace) {
                ArrayList<WorkModel> arrayList = (ArrayList<WorkModel>) adapter.getModels();
                isShowTocstAble = true;
                if (isSubmitAble() && presenter.isSubmitAble(arrayList)) {
                    requestPermission(Manifest.permission.CAMERA, new Runnable() {
                        @Override
                        public void run() {
                            startActivityForResult(new Intent(ct, RealTimeDetectFaceActivty.class), 0x223);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(ct, R.string.not_camera_permission);
                        }
                    });
                }
            } else {
                presenter.submit(null, macAddress);
            }
        } else if (view.getId() == R.id.date_tv) {
//			isShowTocstAble = true;
//			showDateDialog();
        } else if (view.getId() == R.id.unoffice_) {
            isShowTocstAble = true;
            presenter.gotoLocationActivity(WorkActivity.this);
        } else if (view.getId() == R.id.super_setting_tv) {
            startActivityForResult(new Intent(ct, SignSeniorSettingActivity.class), 0x12);
            closePopupWindow();
        } else if (view.getId() == R.id.work_setting_tv) {
            intent = new Intent(ct, FlightsActivity.class);
            startActivityForResult(intent, 0x12);
            closePopupWindow();
        } else if (view.getId() == R.id.office_addr_setting_tv) {
            startActivityForResult(new Intent(ct, OfficeAddressSettingsActivity.class), 0x12);
            closePopupWindow();
        } else if (view.getId() == R.id.my_rule_setting_tv) {
            intent = new Intent(ct, MyRuleSetActivity.class);
            presenter.showMyRele(intent, adapter.getModels());
            startActivity(intent);
            closePopupWindow();
        } else if (view.getId() == R.id.cancel_tv) {
            closePopupWindow();
        }
    }


    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.pop_work_activity, null);
        if (!isAdmin) {
            viewContext.findViewById(R.id.super_setting_tv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.work_setting_tv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.office_addr_setting_tv).setVisibility(View.GONE);
        } else {
            viewContext.findViewById(R.id.super_setting_tv).setOnClickListener(this);
            viewContext.findViewById(R.id.work_setting_tv).setOnClickListener(this);
            viewContext.findViewById(R.id.office_addr_setting_tv).setOnClickListener(this);
        }
        viewContext.findViewById(R.id.my_rule_setting_tv).setOnClickListener(this);
        viewContext.findViewById(R.id.cancel_tv).setOnClickListener(this);
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }

    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    //显示时间选择器
    private void showDateDialog() {
        OASigninPicker picker = new OASigninPicker(this, CalendarUtil.getYear() - 1, CalendarUtil.getYear());
        picker.setRange(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                selectTime = DateFormatUtil.str2Long(time, DateFormatUtil.YMD);
                isToday = time.equals(DateFormatUtil.long2Str(DateFormatUtil.YMD)) ? true : false;
                date_tv.setText(year + "年" + month + "月" + day + "日 " + CalendarUtil.getWeek(selectTime));
                presenter.loadWorkData(selectTime, true);
            }
        });
        picker.show();
    }

    public void getMac() {
        macAddress = SystemUtil.getMac(ct);
        if (StringUtil.isEmpty(macAddress)) {
            //当没有获取到MACAddress的时候，判断wifi状态，如果wifi为未启动
            //mac地址不合法时候   提示开启wifi
            WifiReceiverUtil wifiReceiver = new WifiReceiverUtil();
            wifiReceiver.regReceiver(ct, new WifiReceiverUtil.OnWifiStatusChangeLinstener() {
                @Override
                public void callBack(boolean isOpen) {
                    if (isOpen) {
                        macAddress = SystemUtil.getMac(ct);
                    }
                }
            });
        }
    }


    private void showEmptyView(boolean isShow, boolean isFree) {
        if (isFree) {
            empty_tv.setText(getResources().getString(R.string.free_work));
        } else {
            empty_tv.setText(getResources().getString(R.string.not_find_work));
        }
        if (isShow) {
            empty_rl.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
        } else {
            empty_rl.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x223 && RESULT_OK == resultCode) {
            presenter.submit(null, macAddress);
        } else if (0x12 == requestCode) {
            isShowTocstAble = false;
            presenter.loadWorkData(selectTime, isShowTocstAble);
        }
        if (data == null) return;
        if (requestCode == ADDRESS_CHANGE && resultCode == ADDRESS_CHANGE) {//地址微调
            PoiInfo poi = data.getParcelableExtra("resultKey");
            if (poi == null || presenter == null) return;
            isChangePoi = true;
            presenter.changPoi(poi);
        } else if (requestCode == PHONE_CHANGE && resultCode == 0x20) {
            boolean isChange = data.getBooleanExtra("isChange", false);
            if (isChange)
                macError = getResources().getString(R.string.mac_changing);
        } else if (ALLEGEDLY == requestCode && 0x20 == resultCode) {
            boolean isChange = data.getBooleanExtra("isChange", false);
            if (isChange)
                presenter.loadWorkData(selectTime, isShowTocstAble);
        }
    }

    private boolean isSubmitAble() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            showToast(R.string.networks_out, R.color.load_warning);
            return false;
        } else if (!isToday) {
            showToast(R.string.not_today, R.color.load_warning);
            return false;
        } else if (!StringUtil.isEmpty(macError)) {
            showToast(macError, R.color.load_warning);
            return false;
        } else if (TextUtils.isEmpty(unoffice_mm.getText())) {
            showToast(R.string.not_addr_message, R.color.load_warning);
            return false;
        }
        return true;
    }

    private boolean isShowTocstAble = true;

    @Override
    public void showToast(String message, int colorId) {
        if (isShowTocstAble)
            super.showToast(message, colorId);
    }

    @Override
    public void showToast(int reId, int colorId) {
        if (isShowTocstAble)
            super.showToast(reId, colorId);
    }
}
