package com.uas.appme.pedometer.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

/**
 * Created by FANGlh on 2017/8/9.
 * function:
 */

public class CommonSensorUtils  {

    private Context context;

    public CommonSensorUtils() {

    }


    /**
     * 获取查看手机上有哪些传感器:
     * @param context
     */
    public static void checkSenior(Context context){
        //获取传感器管理器
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : deviceSensors) {
            Log.i("sensor", "------------------");
            Log.i("sensor", sensor.getName());
            Log.i("sensor", sensor.getVendor());
            Log.i("sensor", Integer.toString(sensor.getType()));
            Log.i("sensor", "------------------");
        }
    }
}
