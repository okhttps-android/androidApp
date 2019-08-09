package com.uas.appworks.CRM.erp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.ToastUtil;
import com.uas.appworks.CRM.erp.adapter.DeviceManageAdapter;
import com.uas.appworks.CRM.erp.model.DeviceManage;
import com.uas.appworks.R;
import com.uas.appworks.activity.DeviceQueryActivity;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

public class DeviceManageActivity extends SupportToolBarActivity {

    private RecyclerView contantRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);
        initView();
    }

    private void initView() {
        contantRv = findViewById(R.id.contantRv);
        contantRv.setLayoutManager(new GridLayoutManager(ct, 4));
        List<DeviceManage> models = new ArrayList<>();
        //扫一扫
        models.add(new DeviceManage(1, R.drawable.icon_scan_device, StringUtil.getMessage(R.string.text_scan_device)).setCazz(CaptureActivity.class).setRequest(0x21));
        //设备查询
        models.add(new DeviceManage(2, R.drawable.icon_device_query, StringUtil.getMessage(R.string.text_device_query)).setCazz(DeviceQueryActivity.class).addString(Constants.FLAG.DEVICE_CALLER, "Device").addString(Constants.FLAG.DEVICE_WHICH_PAGE, Constants.FLAG.DEVICE_FROM_QUERY));
        //通用配置表单

        models.add(new DeviceManage(3, R.drawable.icon_application_use, StringUtil.getMessage(R.string.text_application_use)).setCazz(DeviceDataFormAddActivity.class).addString("caller", "DeviceChange!Use"));
        models.add(new DeviceManage(4, R.drawable.icon_scrap_application, StringUtil.getMessage(R.string.text_scrap_application)).setCazz(DeviceDataFormAddActivity.class).addString("caller", "DeviceChange!Scrap"));
        models.add(new DeviceManage(5, R.drawable.icon_maintenance, StringUtil.getMessage(R.string.text_maintenance)).setCazz(DeviceDataFormAddActivity.class).addString("caller", "DeviceChange!Maintain"));
        models.add(new DeviceManage(6, R.drawable.icon_fault_inspection, StringUtil.getMessage(R.string.text_fault_inspection)).setCazz(DeviceDataFormAddActivity.class).addString("caller", "DeviceChange!Inspect"));
        //周期盘点
        models.add(new DeviceManage(7, R.drawable.icon_cycle_count, StringUtil.getMessage(R.string.text_cycle_count)).setCazz(DeviceCycleCountActivity.class));
        models.add(new DeviceManage(8, R.drawable.icon_device_match, StringUtil.getMessage(R.string.text_device_query_match)).setCazz(DeviceMatchActivity.class));
        DeviceManageAdapter adapter = new DeviceManageAdapter(this, models);
        contantRv.setAdapter(adapter);
        adapter.setItemClickListener(new DeviceManageAdapter.ItemClickListener() {
            @Override
            public void itemClick(DeviceManage manage) {
                try {
                    clickDevice(manage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void clickDevice(final DeviceManage manage) throws Exception {
        if (manage != null) {
            Class cazz = manage.getCazz();
            final Intent intent = new Intent(ct, cazz);
            intent.putExtra("title", manage.getName());
            Bundle bundle = manage.getBundle();
            if (bundle != null) {
                intent.putExtra("data", bundle);
                if (bundle.keySet() != null) {
                    for (String e : bundle.keySet()) {
                        intent.putExtra(e, bundle.getString(e));
                    }
                }
            }
            if (manage.getRequest() > 0) {
                requestPermission(Manifest.permission.CAMERA, new Runnable() {
                    @Override
                    public void run() {
                        startActivityForResult(intent, manage.getRequest());
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(ct, R.string.not_camera_permission);
                    }
                });
            } else {
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 0x21 && resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        Log.d("scanurl", result);
//                        ToastUtil.showToast(ct, result);
                        startActivityForResult(new Intent(ct, ScanDetailActivity.class).putExtra("decode", result), 0x22);
                    }
                }
            } else if (requestCode == 0x22) {
                showDialog(data.getStringExtra("data"));
            }
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
}
