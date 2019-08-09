package com.uas.applocation.test;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.uas.applocation.R;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.AreaUtils;
import com.uas.applocation.utils.CoordinateUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationTestSetActivity extends AppCompatActivity {
    private TextView currentInfoTv;
    private CheckBox userNativeCb;
    private CheckBox inventedCb;
    private EditText longitudeEd;
    private EditText latitudeEd;
    private Button saveBtn;
    private RelativeLayout inventedRl;
    private LinearLayout inventedLL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_test_set);
        initView();
    }

    private void initView() {
        currentInfoTv = (TextView) findViewById(R.id.currentInfoTv);
        userNativeCb = (CheckBox) findViewById(R.id.userNativeCb);
        inventedCb = (CheckBox) findViewById(R.id.inventedCb);
        longitudeEd = (EditText) findViewById(R.id.longitudeEd);
        latitudeEd = (EditText) findViewById(R.id.latitudeEd);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        inventedRl = findViewById(R.id.inventedRl);
        inventedLL = findViewById(R.id.inventedLL);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(LocationTestSetActivity.this)
                        .setTitle("UU提示")
                        .setMessage("保存模拟定位以后，将不会再自动定位，直到退出应用或关闭虚拟定位。是否确定保存")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                UASLocation mUASLocation = getInventedUASLocation();
                                currentInfoTv.setText(mUASLocation.toString());
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });

        UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();
        if (mUASLocation != null) {
            currentInfoTv.setText(mUASLocation.toString());
            boolean userNative = UasLocationHelper.getInstance().isOutChina();
            userNativeCb.setChecked(userNative);
            inventedRl.setVisibility(userNative ? View.VISIBLE : View.GONE);
            showInventedLL(inventedCb.isChecked());
            longitudeEd.setText(mUASLocation.getLongitude() + "");
            latitudeEd.setText(mUASLocation.getLatitude() + "");
        }
        userNativeCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
        inventedCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (compoundButton == userNativeCb) {
                UasLocationHelper.getInstance().setTest(b);
            } else if (inventedCb == compoundButton) {
                showInventedLL(b);
            }
        }
    };

    private void showInventedLL(boolean clicked) {
        int show = clicked ? View.VISIBLE : View.GONE;
        inventedLL.setVisibility(show);
        saveBtn.setVisibility(show);
    }

    private UASLocation getInventedUASLocation() {
        UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();
        float latitude = Float.valueOf(latitudeEd.getText().toString());
        float longitude = Float.valueOf(longitudeEd.getText().toString());
        Geocoder gc = new Geocoder(this, Locale.getDefault());
        List<Address> locationList = null;
        try {
            locationList = gc.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (locationList != null && locationList.size() > 0) {
            Address address = locationList.get(0);//得到Address实例
            mUASLocation.setLocationOk(true);
            mUASLocation.setProvince(address.getLocality());
            mUASLocation.setCityName(address.getSubAdminArea());
            mUASLocation.setCountry(address.getCountryName());
            mUASLocation.setName(address.getFeatureName());
            mUASLocation.setDistrict(address.getThoroughfare());
            mUASLocation.setAddress(address.getAddressLine(0));
            LatLng latlng = CoordinateUtils.common2Baidu(new LatLng(address.getLatitude(), address.getLongitude()));
            mUASLocation.setGpsLatitude(address.getLatitude());
            mUASLocation.setGpsLongitude(address.getLongitude());
            mUASLocation.setLatitude(latlng.latitude);
            mUASLocation.setLongitude(latlng.longitude);
        }
        return mUASLocation;
    }
}
