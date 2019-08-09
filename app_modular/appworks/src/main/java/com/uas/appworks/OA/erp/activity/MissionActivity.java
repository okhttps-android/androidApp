package com.uas.appworks.OA.erp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.aip.excep.activity.RealTimeDetectFaceActivty;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.MissionModel;
import com.core.model.SelectBean;
import com.core.utils.BaiduMapUtil;
import com.core.utils.OnGetDrivingRouteResult;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.model.SelectAimModel;
import com.lidroid.xutils.ViewUtils;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.OA.erp.adapter.MissionAdapter;
import com.uas.appworks.OA.erp.model.IMission;
import com.uas.appworks.OA.erp.presenter.IMissionPresenter;
import com.uas.appworks.OA.erp.presenter.MissionPresenter;
import com.uas.appworks.OA.erp.utils.MostLinearLayoutManager;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 外勤打卡
 */
public class MissionActivity extends OABaseActivity implements View.OnClickListener, IMission, MissionAdapter.OnitemClickListener {
    private final int NUMBER_SELECT = 112;
    private RecyclerView recycler;
    public int position;//调转指针
    private IMissionPresenter presenter;
    private MissionAdapter adapter;
    private int flag;
    private boolean showSubmit;
    private boolean adminStatus;
    private boolean locationOk = true;
    private MissionModel mMissionModel;

    private long time = System.currentTimeMillis();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (System.currentTimeMillis() - time < (3 * 60 * 1000)) return;
            updateLocation();
        }
    };

    private void updateLocation() {
        time = System.currentTimeMillis();
        locationOk = UasLocationHelper.getInstance().getUASLocation().isLocationOk();
        if (locationOk && adapter != null && !ListUtils.isEmpty(adapter.getModels())) {
            for (int i = 0; i < adapter.getModels().size(); i++) {
                if (adapter.getModels().get(i).getStatus() != 1) {
                    adapter.getModels().get(i).setRecorddate(TimeUtils.f_long_2_str(System.currentTimeMillis()));
                    adapter.getModels().get(i).setLocation(UasLocationHelper.getInstance().getUASLocation().getName());
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        ViewUtils.inject(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOCATION_CHANGE);
        LocalBroadcastManager.getInstance(ct).registerReceiver(receiver, filter);
        initView();
        initEvent();
        UasLocationHelper.getInstance().requestLocation(new OnLocationListener() {
            @Override
            public void onReceiveLocation(UASLocation mUASLocation) {
                updateLocation();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mission, menu);
        if (!adminStatus) {
            MenuItem item = menu.getItem(0);
            if (item.getItemId() == R.id.title) {
                item.setVisible(false);
            }
        }
//		MenuItem sign = menu.getItem(1);
//		if (sign.getItemId() == R.id.sign) {
//			sign.setVisible(false);
//		}
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        endActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            endActivity();
            return true;
        } else if (item.getItemId() == R.id.title) {
            Intent intent = new Intent(ct, MissionSetActivity.class);
            intent.putExtra(AppConfig.IS_ADMIN, adminStatus);
            startActivityForResult(intent, 0x22);
        } else if (item.getItemId() == R.id.sign) {
            if (adapter != null) {
                presenter.sign(adapter.getModels());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initEvent() {
        if (!showSubmit) {
            findViewById(R.id.click_btn).setVisibility(View.GONE);
            findViewById(R.id.additem_tv).setVisibility(View.GONE);
        }
        findViewById(R.id.click_btn).setOnClickListener(this);
        findViewById(R.id.additem_tv).setOnClickListener(this);
    }

    private void initView() {
        setTitle(R.string.activity_mission_plan);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        locationOk = UasLocationHelper.getInstance().getUASLocation().isLocationOk();
        if (getIntent() != null) {
            flag = getIntent().getIntExtra("flag", 0);
            showSubmit = getIntent().getBooleanExtra("showSubmit", true);
        }
        String addrName = UasLocationHelper.getInstance().getUASLocation().getName();
        if (StringUtil.isEmpty(addrName) && !locationOk)
            showNotLocation();
        adminStatus = getIntent() == null ? false : getIntent().getBooleanExtra(AppConfig.IS_ADMIN, false);
        adapter = new MissionAdapter(this, this);
        recycler.setLayoutManager(new MostLinearLayoutManager(ct));
        recycler.setAdapter(adapter);
        presenter = new MissionPresenter(this);
        presenter.start(getIntent());
    }


    private void endActivity() {
        LocalBroadcastManager.getInstance(ct).unregisterReceiver(receiver);
        boolean isAuto = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
        if (ApiUtils.getApiModel() instanceof ApiPlatform || isAuto) {
            setResult(0x20);
            finish();
        } else {
            if (flag == 1) {
                //启动手动外勤
                Intent intent = new Intent("com.modualr.appworks.OutofficeActivity");
                intent.putExtra(AppConfig.IS_ADMIN, true);//上传管理员状态
                startActivity(intent);
                finish();
            } else if (flag == 2) {
                setResult(0x20);
                finish();
            }
        }
    }


    @Override
    public void showModels(List<MissionModel> models) {
        dimssLoading();
        if (adapter != null) {
            adapter.setModels(models);
            adapter.notifyDataSetChanged();
//            if (ListUtils.isEmpty(models)) return;
//            for (int i = 0; i < models.size(); i++) {
//                if (models.get(i).getStatus() == 4 && StringUtil.isEmpty(models.get(i).getVisitTime())) {
//                    position = i;
//                    float dis = Float.valueOf(BaiduMapUtil.getInstence().getDistance(models.get(i).getLatLng()));
//                    setEndTime(models.get(i).getLatLng(), dis);
//                }
//            }
        } else {//容错
            adapter = new MissionAdapter(this, this);
            recycler.setLayoutManager(new MostLinearLayoutManager(ct));
            recycler.setAdapter(adapter);
        }
    }

    @Override
    public void showFinds(List<SelectBean> models) {
        Intent intent = new Intent(this, SelectActivity.class)
                .putExtra("type", 2)
                .putExtra("title", getString(R.string.login_company_select))
                .putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) models);
        startActivityForResult(intent, NUMBER_SELECT);
    }

    @Override
    public void changModelStatus(int status, int postion) {
        if (ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= postion) return;
        adapter.getModels().get(postion).setStatus(status);
        adapter.notifyItemChanged(postion);
    }

    @Override
    public void faceSign(MissionModel missionModel) {
        mMissionModel = missionModel;
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

    @Override
    public void faceUpload(MissionModel mission, String faceBase64) {
        presenter.uploadFace(this, mission, faceBase64);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.additem_tv) {
            if (!ListUtils.isEmpty(adapter.getModels())) {
                addEntity(adapter.getModels());
                adapter.notifyItemInserted(adapter.getModels().size() - 1);
            }
        } else if (view.getId() == R.id.click_btn) {
            if (!locationOk) {
                showNotLocation();
                return;
            }
            presenter.submit(adapter.getModels());
        }
    }


    @Override
    public void click(final int position, MissionModel model, View view) {
        Intent intent = null;
        MissionActivity.this.position = position;
        if (view.getId() == R.id.item_company_tv) {
            if (model == null || model.getStatus() == 1) return;
            intent = new Intent("com.modular.form.SelectAimActivity");
            startActivityForResult(intent, 0x20);
        } else if (view.getId() == R.id.item_delete_tv) {
            try {
                MissionModel m = adapter.getModels().get(position);
                if (!StringUtil.isEmpty(m.getCompanyName()) || !StringUtil.isEmpty(m.getCompanyAddr()) ||
                        !StringUtil.isEmpty(m.getVisitTime())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ct);
                    builder.setTitle(R.string.prompt_title).setMessage(R.string.sure_delete_mission).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                if (adapter == null || ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= position)
                                    return;

                                adapter.getModels().remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, adapter.getModels().size());
                            } catch (Exception e) {

                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                } else {
                    adapter.getModels().remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, adapter.getModels().size());
                }
            } catch (Exception e) {
                if (e != null)
                    LogUtil.i("Exception e=" + e.getMessage());
            }
        } else if (view.getId() == R.id.item_reckontime_tv) {
            if (model == null || model.getStatus() == 1) return;
            showDateDialog();
        } else if (view.getId() == R.id.item_remark_tv) {
            if (model == null || model.getStatus() == 1) return;
            intent = new Intent("com.modular.appworks.SelectRemarkActivity");
            startActivityForResult(intent, 0x23);
        } else if (view.getId() == R.id.navigation_tv) {
            intent = new Intent("com.modular.appworks.NavigationActivity");
            intent.putExtra("toLocation", model.getLatLng());
            startActivityForResult(intent, 0x23);
        }
    }

    public void showDateDialog() {
        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2010, 2030);
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay(), CalendarUtil.getHour(), CalendarUtil.getMinute());
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00";
                adapter.getModels().get(position).setVisitTime(time);
                adapter.notifyItemChanged(position);
            }
        });
        picker.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        try {
            onResult(requestCode, resultCode, data);
        } catch (Exception e) {

        }
    }

    private SelectAimModel chcheAimModel = null;
    private PopupWindow popupWindow = null;

    private void onResult(int requestCode, int resultCode, Intent data) throws Exception {
        if (requestCode == 0x20) {//选择公司
            chcheAimModel = data.getParcelableExtra("data");
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            popupWindow = PopupWindowHelper.create(this, getString(R.string.perfect_company_name), chcheAimModel, new PopupWindowHelper.OnClickListener() {
                @Override
                public void result(SelectAimModel model) {
                    sureSelectAim(model);
                }
            }, new PopupWindowHelper.OnFindLikerListener() {
                @Override
                public void click(String licker) {
                    presenter.finder(licker);
                }
            });
        } else if (0x22 == requestCode) {//外勤设置
            boolean isAuto = data.getBooleanExtra("isAuto", true);
            if (!isAuto && !(ApiUtils.getApiModel() instanceof ApiPlatform)) {
                if (flag == 1) {
                    //启动手动外勤
                    Intent intent = new Intent("com.modualr.appworks.OutofficeActivity");
                    intent.putExtra(AppConfig.IS_ADMIN, true);
                    startActivity(intent);
                    finish();
                } else if (flag == 2) {
                    setResult(0x20);
                    finish();
                }
            }
        } else if (0x23 == requestCode) {
            String message = data.getStringExtra("data");
            String remark = StringUtil.isEmpty(message) ? getResources().getString(R.string.maintain_customers) : message;
            adapter.getModels().get(position).setRemark(remark);
            adapter.notifyItemChanged(position);
        } else if (NUMBER_SELECT == requestCode) {
            SelectBean bean = data.getParcelableExtra("data");
            if (chcheAimModel != null && bean != null) {
                chcheAimModel.setName(bean.getName());
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                popupWindow = PopupWindowHelper.create(this, getString(R.string.perfect_company_name), chcheAimModel, new PopupWindowHelper.OnClickListener() {
                    @Override
                    public void result(SelectAimModel model) {
                        sureSelectAim(model);
                    }
                }, new PopupWindowHelper.OnFindLikerListener() {
                    @Override
                    public void click(String licker) {
                        presenter.finder(licker);
                    }
                });
            }

        } else if (requestCode == 0x223 && RESULT_OK == resultCode) {
            String faceBase64 = data.getStringExtra(Constants.Intents.FACE_SIGN_BASE64);
            presenter.signinMission(mMissionModel, faceBase64);
        }
    }


    private void sureSelectAim(SelectAimModel entity) {
        if (entity == null) return;
        if (ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= position)
            return;
        adapter.getModels().get(position).setCompanyName(StringUtil.isEmpty(entity.getName()) ? "" : entity.getName());
        adapter.getModels().get(position).setCompanyAddr(StringUtil.isEmpty(entity.getAddress()) ? "" : entity.getAddress());
        adapter.getModels().get(position).setVisitcount(1 + entity.getTimes());
        if (entity.getLatLng() != null) {
            adapter.getModels().get(position).setLatLng(entity.getLatLng());
            adapter.getModels().get(position).setDistance(LocationDistanceUtils.distanceMe(adapter.getModels().get(position).getLatLng()));
        }
        adapter.notifyItemChanged(position);
        if (entity.getLatLng() != null) {
            try {
                float dis = LocationDistanceUtils.distanceMe(entity.getLatLng());
                setEndTime(entity.getLatLng(), dis);
                chcheAimModel = null;
            } catch (ClassCastException e) {
                if (e != null)
                    LogUtil.i("ClassCastException " + e.getMessage());
            }
        }
    }

    /**
     * @param location
     * @param dis
     * @update by 2017/1/11
     */
    private void setEndTime(LatLng location, final double dis) {
        BaiduMapUtil.getInstence().getDrivingRoute(UasLocationHelper.getInstance().getUASLocation().getLocation(), location,
                new OnGetDrivingRouteResult() {
                    @Override
                    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                        try {
                            List<DrivingRouteLine> list = drivingRouteResult.getRouteLines();
                            if (ListUtils.isEmpty(list)) {
                                if (dis != 0) {
                                    setVoisitTime((long) (dis / 3));
                                } else
                                    setVoisitTime(800);
                                return;
                            }
                            int minTime = 0;
                            for (DrivingRouteLine e : list) {
                                if (minTime == 0 || minTime > e.getDuration()) {
                                    minTime = e.getDuration();
                                }
                            }
                            setVoisitTime(minTime);
                        } catch (Exception e) {

                        }
                    }
                });

    }

    /**
     * 注意异常处理，由于是一个延迟的过程，可能出现在时间计算出来前删除该条数据
     *
     * @param time 秒
     * @update by 2017/1/11
     */
    private void setVoisitTime(long time) throws Exception {
        if (adapter == null || ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= position)
            return;
        String realTime = TimeUtils.f_long_2_str(System.currentTimeMillis() + (time * 1000));
        adapter.getModels().get(position).setVisitTime(realTime);
        adapter.notifyItemChanged(position);
    }

    public void showNotLocation() {
        int stringId = 0;
        if (MyApplication.getInstance().isNetworkActive()) {
            stringId = R.string.allow_location;
            MaterialDialog dialog = new MaterialDialog.Builder(ct).title(R.string.prompt_title).
                    content(stringId)
                    .positiveText(R.string.sure).
                            autoDismiss(false).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                        }
                    }).build();
            dialog.show();
        } else {
            stringId = R.string.networks_out;
            showToast(stringId, R.color.load_message);
        }
    }

    private void addEntity(List<MissionModel> entitys) {
        MissionModel entity = new MissionModel();
        entity.setLocation(UasLocationHelper.getInstance().getUASLocation().getName());//当前位置
        entity.setRecorddate(TimeUtils.f_long_2_str(System.currentTimeMillis()));//当前时间
        entity.setStatus(0);
        entity.setType(1);//当前时间
        entitys.add(entity);
    }

    @Override
    public void showLoading() {
        super.showLoading();
    }
}
