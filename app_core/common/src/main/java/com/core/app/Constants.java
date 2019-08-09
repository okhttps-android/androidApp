package com.core.app;

import android.os.Environment;

import com.common.config.BaseConfig;
import com.core.db.DatabaseTables;


public class Constants {

    //管理平台账户地址
    public static final String BASE_URL_LOGIN = "http://manage.ubtob.com/public/account";
    //账户中心地址
    public static final String ACCOUNT_CENTER_HOST = "https://sso.ubtob.com/";
    //            public static final String ACCOUNT_CENTER_HOST = "http://218.17.158.219:32323/";
    //    public static final String ACCOUNT_CENTER_HOST = "https://account.ubtob.com/";
    //IM正式地址
    public static final String IM_BASE_URL = "http://113.105.74.140:8092/";
    //IM测试地址
    public static final String IM_BASE_URL_TEST = "http://113.105.74.135:8092/";
    //询价服务正式地址
//    public static final String API_INQUIRY = "https://api-inquiry.usoftmall.com/";
    public static final String API_INQUIRY = "https://api-inquiry.usoftchina.com/";
//    public static final String API_INQUIRY = "http://218.17.158.219:24000/";
//    public static final String API_INQUIRY = "http://10.1.51.82:24002/";

    public static String IM_BASE_URL() {
        String url = "";
        if (BaseConfig.isDebug()) {
            url = IM_BASE_URL_TEST;
            url = IM_BASE_URL;
        } else {
            url = IM_BASE_URL;
        }
        return url;
    }

    public static final String WXPAY_APPID = "wxd1deafafe3fd9a21";
    public static final String WEIXIN_SECRET = "7475f0b85c140c68e7568c904fb68421";

    public static String charitBaseUrl() {

        return "http://lj.ubtob.com/app/";
    }

    //通讯录表
    public static final String TABLE_CONTANCTS = "CREATE TABLE  tbl_contacts " +
            "(id integer primary key autoincrement,"
            + "tf_name varchar(50),"
            + "tf_whichSys varchar(50),"
            + "tf_company varchar(50),"
            + "tf_phone varchar(50) ,"
            + "tf_email varchar(50),"
            + "tf_type integer,"
            + "tf_ownerId varchar(50),"
            + "tf_imId varchar(50) default '0')";

    //组织架构 员工信息
    public static final String SQL_EMPLOYEES_CREATETABLE = "CREATE TABLE EMPLOYEES "
            + "(id integer primary key autoincrement,"
            + "em_id integer,"
            + "em_code varchar(50) NOT NULL UNIQUE,"
            + "em_name varchar(50),"
            + "em_position varchar(50),"
            + "em_jobs varchar(50),"
            + "em_defaultorname varchar(50),"
            + "em_depart varchar(50),"
            + "em_tel varchar(50),"
            + "em_imid varchar(50) default '0',"
            + "em_mobile varchar(50),"
            + "em_email varchar(50),"
            + "em_uu varchar(50),"
            + "em_imageid integer,"
            + "company varchar(100),"
            + "whichsys varchar(50),"
            + "em_defaultorid integer,"
            + "em_flag varchar(50))";
    //组织架构表
    public static final String SQL_HRORGS_CREATETABLE = "CREATE TABLE HRORGS "
            + "(id integer primary key autoincrement,"
            + "or_id integer,"
            + "or_code varchar(50) NOT NULL UNIQUE,"
            + "or_name varchar(50),"
            + "or_subof varchar(50),"
            + "or_isleaf varchar(50),"
            + "company varchar(100),"
            + "whichsys varchar(50),"
            + "or_flag varchar(50),"
            + "or_headmanname varchar(50),"
            + "or_headmancode varchar(50),"
            + "or_remark integer" +
            ")";
    //组织架构 更新时间表
    public static final String SQL_empdate_CREATETABLE = "CREATE TABLE empdate "
            + "(id integer primary key autoincrement,"
            + "ed_lastdate varchar(50),"
            + "ed_kind varchar(50),"
            + "ed_company varchar(50),"
            + "ed_whichsys varchar(50)"
            + ")";
    //b2b消息通知表
    public static final String SQL_B2B_MSG = "CREATE TABLE B2BMSG "
            + "(id integer primary key autoincrement,"
            + "b2b_content varchar(50),"
            + "b2b_time varchar(50),"
            + "b2b_hasRead integer,"
            + "b2b_master varchar(50)"
            + ")";

    //OA内勤签到表
    public static final String WORKTIBLE = "CREATE TABLE workdata("
            + "id integer UNIQUE,"//id  唯一不可重复
            + "emCode  varchar(10),"//员工编号
            + "master  varchar(20),"//账套
            + "date  varchar(20),"//日期  yyyy-MM-dd
            + "workStart  varchar(6),"//上班开始时间  hh:mm
            + "workTime  varchar(6),"//上班时间  hh:mm
            + "workend  varchar(6),"//上班结束时间  hh:mm
            + "workSignin  varchar(6),"//上班签到  hh:mm
            + "workAllegedly  varchar(6),"//上班申诉时间  hh:mm
            + "workAlarm INTEGER,"//上班提醒 0|1
            + "offStart  varchar(6),"//下班开始时间  hh:mm
            + "offTime  varchar(6),"//下班时间  hh:mm
            + "offend  varchar(6),"//下班结束时间  hh:mm
            + "offSignin  varchar(6),"//下班签到  hh:mm
            + "offAllegedly  varchar(6),"//下班申诉时间  hh:mm
            + "offAlarm INTEGER,"//下班提醒 0|1
            + "isNextDay INTEGER,"//是否是跨天 0|1
            + "leaveAlarm INTEGER"//离开提示次数  0|1
            + ")";


    //OA内勤签到表
    public static final String SIGNING = "CREATE TABLE signing("
            + "emCode  varchar(10),"//员工编号
            + "master  varchar(20),"//账套

            + "workTime  varchar(6),"//上班时间  hh:mm
            + "offTime  varchar(6),"//下班时间   hh:mm

            + "workSignin  varchar(6),"//上班签到时间 hh:mm
            + "offSignin  varchar(6),"//下班签到时间  hh:mm

            + "startTime  varchar(6),"//上班开始签到时间  hh:mm
            + "endTime  varchar(6),"//下班开始签到时间    hh:mm

            + "leave INTEGER,"//下班开始签到时间    hh:mm

            + "workAlarm INTEGER,"//上班提醒
            + "offAlarm INTEGER"//下班提醒

            + ")";

    /**
     * oa签到时间表
     */
    @Deprecated
    public static final String OA_SIGNIN_TIME = "CREATE TABLE Signin("
            + "emCode  varchar(10),"//员工编号
            + "master  varchar(20),"//账套

            + "workTime  varchar(6),"//上班时间  hh:mm
            + "offTime  varchar(6),"//下班时间   hh:mm

            + "workSignin  varchar(6),"//上班签到时间  hh:mm
            + "offSignin  varchar(6),"//下班签到时间  hh:mm

            + "startTime  varchar(6),"//上班开始签到时间  hh:mm
            + "endTime  varchar(6),"//下班开始签到时间     hh:mm

            + "workAlarm ,"//上班提醒
            + "offAlarm"//下班提醒 boolean

            + ")";


    public static final String SUB_MESSAGE = "CREATE TABLE submessage(" +
            "isRead ," +//员工账套
            "master  varchar(20)," +//员工账套
            "emcode  varchar(20)," +//员工编号
            "id integer," +//Id   连接时候使用
            "numId integer," +//Id   连接时候使用
            "instanceId integer," +//Id   连接时候使用
            "title  varchar(40)," +//主标题
            "subTitle varchar(40)," +//副标题
            "status integer," +//状态
            "date varchar(20)," +//yyyy-mm-dd
            "createTime" +//创建时间  与date关联
            ")";

    //所有订阅号表
    public static final String TABLE_ALL_SUBSCRIPTION = "CREATE TABLE AllSubs "
            + "(id integer primary key autoincrement,"
            + "subs_id integer,"
            + "subs_title varchar(50),"
            + "subs_kind varchar(50),"
            + "subs_status integer,"
            + "subs_type varchar(50),"
            + "subs_master varchar(50),"
            + "subs_username varchar(50),"
            + "subs_removed integer,"
            + "subs_img BLOB"
            + ")";


    //监控日志表
    public static final String AUTO_LOG = "CREATE TABLE SignAutoLog ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "aa_type TEXT, "
            + "aa_location TEXT, "
            + "aa_remark TEXT, "
            + "aa_date TEXT, "
            + "aa_telephone TEXT, "
            + "sendstatus TEXT"
            + ")";

    // 用于存贮每天UU运动排名第一的数据表
    public static final String STEPRANKING_FIRST = "CREATE TABLE StepRankingFirst ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "date TEXT,"
            + "my_userid TEXT,"
            + "my_rank TEXT, "
            + "my_steps TEXT, "
            + "f_userid TEXT, "
            + "f_name TEXT"
            + ")";


    //用于创建记步数统计表
    public static final String CREATE_BANNER = "create table step ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "curDate TEXT, "
            + "totalSteps TEXT)";

    /* //运行轨迹，实时定位点记录表
     public static final String TRACK_POINT = "CREATE TABLE trackPoint ("
             +"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
             +"latitude double(13,10)," //纬度
             +"longitude double(13,10)," //经度
             +"timestamp varchar(50)," //实时定位时的时间戳
             +"type varchar(10)," //类型 run、walk
             +"startTime varchar(50),"  //开始定位后全部默认为开始时间：yyyy-MM-dd HH:mm
             +"endTime varchar(50)"  //未点击结束前都为空 点击格式：yyyy-MM-dd HH:mm
             +")";*/
    //已订阅号表
    public static final String TABLE_MY_SUBSCRIPTION = "CREATE TABLE MySubs "
            + "(id integer primary key autoincrement,"
            + "subs_id integer,"
            + "subs_title varchar(50),"
            + "subs_kind varchar(50),"
            + "subs_type varchar(50),"
            + "subs_applied integer,"
            + "subs_master varchar(50),"
            + "subs_username varchar(50),"
            + "subs_img BLOB"
            + ")";

    //消息红点数据表
    public static final String EM_ERPNEWS = "CREATE TABLE em_erpnews ("
            + "id integer ,"
            + "master  varchar(20)," //员工账套
            + "emcode  varchar(20)," //员工编号
            + "count integer ,"
            + "title varchar(20),"//位置
            + "subTitle varchar(20),"//详细信息
            + "time varchar(20),"//时间
            + "type varchar(20),"//类型
            + "hierarchy integer,"//类型
            + "readTime integer,"//阅读时间
            + "isReaded integer,"//阅读状态，1阅读
            + "caller varchar(20),"//
            + "keyValue integer"//
            + ")";
    //考勤地址表
    public static final String WORK_LOCATION = "CREATE TABLE work_location ("
            + "id integer primary key autoincrement,"

            + "master  varchar(20)," //员工账套
            + "emcode  varchar(20)," //员工编号

            + "latitude double,"//纬度
            + "longitude double,"//经度
            + "shortName varchar(20),"//位置
            + "workaddr varchar(20),"//地址
            + "validrange integer,"//打卡距离
            + "innerdistance integer"//办公范围
            + ")";

    //外勤计划表
    public static final String TABLE_MISSION = "CREATE TABLE mission ("
            + "id integer UNIQUE,"//id  唯一不可重复
            + "status integer,"//状态  1.结束   2.进行中
            + "master  varchar(20)," //员工账套
            + "emcode  varchar(20)," //员工编号
            + "company varchar(50),"//公司
            + "companyAddr varchar(50),"//公司地址
            + "signinNum integer,"//签到次数
            + "latitude Double,"//纬度
            + "longitude Double,"//经度
            + "date varchar(50),"//日期 yyyy-MM-dd
            + "visittime varchar(50),"//预计到达时间 yyyy-MM-dd HH:mm:ss
            + "realvisitTime varchar(50),"//实际到达时间yyyy-MM-dd HH:mm:ss
            + "realLeaveTime varchar(50),"//实际离开时间yyyy-MM-dd HH:mm:ss
            //update by 2016/12/19
            + "distance Double,"//距离  米
            + "type integer,"//距离  米
            + "remark varchar(50),"//距离  米
            + "location varchar(50),"//录入位置
            + "recorddate varchar(50)"//录入时间yyyy-MM-dd HH:mm:ss
            + ")";

    //数据查询历史记录表
    public static final String TABLE_HISTORICAL_RECORD = "create table "
            + DatabaseTables.HistoricalRecordTable.NAME + "("
            + "_id integer primary key autoincrement, "
            + DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID + ", "
            + DatabaseTables.HistoricalRecordTable.Cols.SCHEME_NAME + ", "
            + DatabaseTables.HistoricalRecordTable.Cols.SEARCH_FIELD + ")";

    //UU助手
    public static final String TABLE_UUHELPER_RECORD = "create table "
            + DatabaseTables.UUHelperTable.NAME + "("
            + DatabaseTables.UUHelperTable.Cols.ID + " integer primary key autoincrement"
            + "," + DatabaseTables.UUHelperTable.Cols.USER_ID
            + "," + DatabaseTables.UUHelperTable.Cols.TIME_SEND
            + "," + DatabaseTables.UUHelperTable.Cols.DATE
            + "," + DatabaseTables.UUHelperTable.Cols.IMAGE_URL
            + "," + DatabaseTables.UUHelperTable.Cols.ICON_URL
            + "," + DatabaseTables.UUHelperTable.Cols.LINK_URL
            + "," + DatabaseTables.UUHelperTable.Cols.CONTENT
            + "," + DatabaseTables.UUHelperTable.Cols.READED
            + "," + DatabaseTables.UUHelperTable.Cols.TITLE
            + "," + DatabaseTables.UUHelperTable.Cols.TYPE
            + ")";

    //TopContactsTable
    public static final String TABLE_TOPCONTACTS_RECORD = "create table "
            + DatabaseTables.TopContactsTable.NAME + "("
            + DatabaseTables.TopContactsTable.Cols.USER_ID
            + "," + DatabaseTables.TopContactsTable.Cols.OWNER_ID
            + "," + DatabaseTables.TopContactsTable.Cols.PHONE
            + "," + DatabaseTables.TopContactsTable.Cols.NAME
            + "," + DatabaseTables.TopContactsTable.Cols.EM_CODE
            + "," + DatabaseTables.TopContactsTable.Cols.LAST_TIME
            + "," + DatabaseTables.TopContactsTable.Cols.STATUS
            + ")";
    public static String IS_NOTIFICATION = "is_notification";//是是否进行通知
    /**
     * @desc:常量
     * @author：Administrator on 2016/5/13 10:21
     */
    public final static int TYPE_CHAT_MANAGE = 2;
    public final static int TYPE_CHAT_All = 1;

    public final static String ANDROID_USER_AGENT_VALUE = "android_agent";
    public final static String ANDROID_USER_AGENT_KEY = "User-Agent";
    /**
     * @desc:http 请求状态码
     * @author：Arison on 2016/7/18
     */
    public static final int HTTP_SUCCESS_INIT = 1;
    public final static int APP_SOCKETIMEOUTEXCEPTION = 99;//网络请求超时，错误，404响应

    public final static int LOAD_SUCCESS = 1;
    public final static int SocketTimeoutException = 0;//ERP登录成功
    public final static int APP_NOTNETWORK = 111;//网络未连接
    public final static int SUCCESS_LOGIN = 3;//获取用户开通的平台信息
    public final static int SUCCESS_B2B = 4;//B2B登录成功
    public final static int SUCCESS_ERP = 5;//ERP登录成功
    public final static int SUCCESS_INITDATA = 7;
    public final static int FIRST_MSG_REQUEST = 17022801;

    //新功能红点
    public final static String new_signin = "signin";
    public final static String new_signout = "signout";
    public final static String new_oa = "oa_menu";
    public final static String new_business = "business";
    public final static String NEW_SETING = "seting_2";
    public final static String NEW_UURUN = "NEW_UURUN";
    public final static String NEW_ME_TAG = "ME_TAG";
    public final static String SET_SIGN_AUTO = "set_sign_auto";
    public final static String SET_SIGN_IN = "set_sign_in";
    public final static String SET_SIGN_LANGUAGE = "set_sign_language";
    public final static String SET_BOOKING_TIME = "set_booking_time";
    public final static String SET_BOOKING = "set_booking";
    public final static String SET_CALL = "set_call";

    public final static String MESSAGE_YUYUE = "message_yuyue";
    public final static String MESSAGE_RUN = "message_run";
    public final static String MESSAGE_DINGYUE = "message_dingyue";

    public final static String MESSAGE_FOOD = "message_food";
    public final static String MESSAGE_HAIR = "message_hair";
    public final static String MESSAGE_KTV = "message_ktv";
    public final static String MESSAGE_SPORT = "message_sport";
    public final static String MESSAGE_CLUB = "message_club";
    public final static String MESSAGE_HOSPITAL = "message_hospital";
    public final static String MESSAGE_REAL_TIME = "message_real_time";
    public final static String MESSAGE_BUSINESS_STATISTICS = "MESSAGE_business_statistics";


    public final static String NEW_FUNCTION_NOTICE = "NEWFUNCTIONNOTICE";
    public final static String NEW_EXPENSE_REIMBURSEMENT_NOTICE = "NEWEXPENSEREIMBURSEMENTNOTICE";
    public final static String MORE_FUNCTION = "MORE_FUNCTION";

    public final static String SET_UU_NEW = "set_uu_new";

//    public static void cleanRed(Context ct) throws PackageManager.NameNotFoundException {
//        int versionCode = PreferenceUtils.getInt("versionCode", 0);
//        LogUtil.i("versionCode=" + versionCode);
//        int appVersionCode = SystemUtil.getVersionCode(ct);
//        if (appVersionCode > versionCode) {
//            LogUtil.i("getVersionCode(ct)>versionCode");
//            PreferenceUtils.putInt("versionCode", appVersionCode);
//            CommonUtil.putSharedPreferencesBoolean(ct, new_oa, false);
//            PreferenceUtils.putInt(MainActivity.NEW_FUNCTION_NOTICE, 0);
//        }
//    }

    public static String INSTANT_MESSAGE = "instant_message";//转发消息的标记
    public static String INSTANT_MESSAGE_FILE = "instant_message_file";//转发文件稍有不同
    public static String INSTANT_SEND = "instant_send";//转发
    public static String CHAT_MESSAGE_DELETE_ACTION = "chat_message_delete";
    public static String CHAT_MESSAGE_DELETE_FRIENDID = "chat_message_delete_friendid";//删除消息要带过去的朋友id
    public static String CHAT_REMOVE_MESSAGE_FALG = "CHAT_REMOVE_MESSAGE_FALG";
    public static String CHAT_REMOVE_MESSAGE_POSITION = "CHAT_REMOVE_MESSAGE_POSITION";
    public static String ONRECORDSTART = "onrecordstart";
    public static String GROUP_JOIN_NOTICE = "group_join_notice";//加入新群的通知


    public static String GROUP_JOIN_NOTICE_ACTION = "group_join_notice_action";//加入新群的通知
    public static String GROUP_JOIN_NOTICE_FRIEND_ID = "group_join_notice_friend_id";//加入新群发送朋友的id

    public static String OFFLINE_TIME = "offline_time";//离线时间
    public static String LAST_OFFLINE_TIME = "last_offline_time";


    public static String AUDIO_PHONENUMBER = "audio_phonenumber";//语音
    public static String IS_AUDIO_OR_VIDEO = "is_audio_or_video";//是语音还是视频


    public final static int REQUEST_MANAGE_ALL_SUBSCRIPTON = 101;
    public final static int RESULT_MANAGE_ALL_SUBSCRIPTION = 102;


    public static final String ACTION_LOCATION_CHANGE = "action_location_Change";//位置改变发送广播
    public static final String ACTION_WORK_SIGNIN = "action_work_signin";//当时间合理的时候回调计算内勤打卡

    public static final String ENTERPRISE_REGISTER_CODE = "enterprise_register_code";
    public static int WORK_REPORT_DAY = 111;
    public static int WORK_REPORT_WEEK = 112;
    public static int WORK_REPORT_MONTH = 113;
    public static String REAL_TIME_CACHE = "real_time_cache";

    //设置计步相关迁移
    public static String BAIDU_PUSH = "BAIDUPUSH";
    public static String UU_STEP = "UUSTEP";
    public static String B_SETTINGRED = "B_SETTINGRED";
    public static String UU_STEP_RED = "UU_STEP_RED";
    public static final int MSG_FROM_CLIENT = 0;
    public static final int MSG_FROM_SERVER = 1;
    public static final String BASEURL = "http://news-at.zhihu.com/api/4/";
    public static final String START = "start-image/1080*1776";
    public static String BASE_STEP_URL = "http://113.105.74.140:8092/user/";

    //工作菜单缓存
    public static String WORK_MENU_CACHE = "work_menu_cache";
    public static String WORK_OVERTIME_CALLER_CACHE = "work_overtime_caller_cache";
    public static String WORK_TRAVEL_CALLER_CACHE = "work_travel_caller_cache";
    public static final int LOAD_WORK_MENU_CACHE = 0x01;
    public static final int WORK_MODULE_SORT_REQUEST = 0x11;
    public static final int WORK_FUNC_SET = 0x03;

    public static final String B2B_UID_CACHE = "b2b_uid_cache";
    public static final String B2B_SESSION_CACHE = "b2b_session_cache";

    public static String BASE_CHARIT_ACTIVITY_URL = "http://lj.ubtob.com/mobile#/activity/detail/";
    public static String BASE_CHARIT_PROJECT_URL = "http://lj.ubtob.com/mobile#/project/detail/";

    public static String SPLASH_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UU/splash";
    public static String SPLASH_FILE_NAME = "splash";

    public interface FLAG {
        //webview Cookie缓存
        String WEBVIEW_COOKIE = "webview_cookie";
        /**
         * 设备管理
         */
        String DEVICE_CALLER = "device_caller";
        String DEVICE_CLASS = "device_class";
        String DEVICE_WHICH_PAGE = "device_which_page";
        String DEVICE_FROM_COMMON = "device_from_common";
        String DEVICE_FROM_QUERY = "device_from_query";
        String MODEL = "model";

        /**
         * 一元捐
         */
        int WEIXIN_PAY = 101;
        int API_PAY = 102;

        /**
         * B2B商务
         */
        String B2B_COMPANY_BEAN = "b2b_company_bean";

        String EXTRA_B2B_LIST_ID = "extra_b2b_list_id";
        String EXTRA_B2B_LIST_STATE = "extra_b2b_list_state";
        String EXTRA_B2B_LIST_ENUU = "extra_b2b_list_enuu";
        String EXTRA_B2B_LIST_JSON = "extra_b2b_list_json";
        String EXTRA_B2B_LIST_TEL = "extra_b2b_list_tel";

        String STATE_PURCHASE_ORDER_TODO = "todo";
        String STATE_PURCHASE_ORDER_END = "end";
        String STATE_PURCHASE_ORDER_DONE = "done";

        String STATE_CUSTOMER_INQUIRY_TODO = "todo";
        String STATE_CUSTOMER_INQUIRY_DONE = "done";
        String STATE_CUSTOMER_INQUIRY_AGREED = "agreed";
        String STATE_CUSTOMER_INQUIRY_END = "end";
        String STATE_CUSTOMER_INQUIRY_REFUSED = "refused";
        String STATE_CUSTOMER_INQUIRY_INVALID = "invalid";
        String STATE_CUSTOMER_INQUIRY_ABANDONED = "abandoned";

        String STATE_PUBLIC_INQUIRY_DONE = "public_done";
        String STATE_PUBLIC_INQUIRY_TODO = "public_todo";
        String STATE_PUBLIC_INQUIRY_INVALID = "public_invalid";

        String STATE_COMPANY_BUSINESS_DONE = "company_done";
        String STATE_COMPANY_BUSINESS_TODO = "company_todo";
        String STATE_COMPANY_BUSINESS_INVALID = "company_invalid";

        String GET_LOCAL_ENUU = "get_local_enuu";

        int RESULT_PURCHASE_ORDER = 111;
        int RESULT_CUSTOMER_INQUIRY = 112;
        int RESULT_PUBLIC_INQUIRY = 113;
        int RESULT_COMPANY_BUSINESS = 114;

        /*邀请注册*/
        String INVITE_REGISTER_LIST_STATE = "invite_register_list_state";
        int STATE_INVITE = 101;
        int STATE_REGISTER = 102;
        int STATE_UNREGISTER = 103;
        String REGISTERED_ENTERPRISE_INFO = "registered_enterprise_info";
        String REGISTERED_ENTERPRISE_FLAG = "registered_enterprise_flag";
        int REGISTERED_LIST = 201;
        int REGISTERED_DETAIL = 202;

        /*密码重置*/
        String MODIFY_PASSWORD_EMAIL = "modify_password_email";
        String MODIFY_PASSWORD_TOKEN = "modify_password_token";

        String COMMON_WHICH_PAGE = "common_which_page";

        //商机类型
        String BUSINESS_TYPE = "business_type";
    }

    public interface CACHE {
        String ACCOUNT_CENTER_TOKEN = "account_center_token";

        String B2B_BUSINESS_ENUU = "b2b_business_enuu";
        String EN_BUSINESS_CODE = "en_business_code";

        String MODIFY_PASSWORD_VERIFY_CODE = "modify_password_verify_code";

        String CACHE_SPLASH_URL = "cache_splash_url";

        String OBTAIN_SCHEDULE_DATE = "obtain_schedule_date";
    }

    public interface Intents {
        String MY_DOIT = "isMe";//是否我负责
        String CALLER = "mCaller";
        String STATUS = "mStatus";
        String BILL_STATUS = "bill_status";
        String CONDITION = "mCondition";
        String ID = "mId";
        String TITLE = "mTitlte";
        String TYPE = "mType";
        String HASH_MAP = "hashMap";//录入界面带进去的内容
        String FIELD_CONFIG = "fieldConfig";//搜索配置
        String CONFIG = "config";//配置
        String DETAILS_CLASS = "mDetailsClass";//进入详情界面
        String BILL_JUMPS = "mBillJumps";//详情界面中对于个别字段的处理
        String INPUT_CLASS = "mInputClass";//进入编辑界面
        String LIST_CONDITION = "mListCondition";//列表Condition
        String DB_FIND_CONDITION = "dbfindCondition";//录入界面，dbfind字段添加的前提Condition
        String BILL_LIST_FIELD_FORWARD = "Bill_List_Field_Forward";//录入界面，dbfind字段添加的前提Condition

        String FACE_SIGN_BASE64 = "face_sign_base64";//人脸识别的base64编码
    }
}
