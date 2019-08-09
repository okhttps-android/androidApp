package com.core.model;

/**
 * 自动打卡变量集合
 * 1.上班地址（数组）
 * 2.迟到早退时间。。。最早打卡时间
 * 3.是否自动打卡自动外勤
 * 4.打卡范围 办公范围
 * 5.
 * Created by Bitliker on 2017/2/9.
 */

public class OAConfig {
    public static final String ARRAY_DATA = "ARRAY_DATA";//oa模块中通用调转页面专递的ParcelableArrayListExtra
    public static final String MODEL_DATA = "MODEL_DATA";//oa模块中通用调转页面专递的ParcelableExtra
    public static final String STRING_DATA = "STRING_DATA";//oa模块中通用调转页面专递的String

    public static final String AUTO_SIGIN_ALART = "AUTO_SIGIN_ALART";//oa模块中通用调转页面专递的String

    //关于自动内勤的相关变量
    public static boolean loadWorkSeted = false;//是否下拉过高级考勤数据
    public static boolean autosign = true;       //是否自动考勤
    public static int earlyoff = 0;     //早退时间
    public static int overlatetime = 0; //严重迟到时间
    public static int latetime = 0;    //迟到时间(在该时间内不算迟到)
    public static int nonclass = 90;   //矿工时间
    public static boolean needValidateFace = false;   //是否需要人脸识别打卡
    public static String days = "1,2,3,4,5";
    public static String name = "默认班次";


    public static boolean canShowRed = false;



}
