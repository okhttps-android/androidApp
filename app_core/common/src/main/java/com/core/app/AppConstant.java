package com.core.app;

public class AppConstant {
    /* 进入SingleImagePreviewActivity需要带上的参数 */
    /**
     * 某些地方选择数据使用的常量
     */
    public static final String EXTRA_ACTION = "action";// 进入这个类的执行的操作
    public static final int ACTION_NONE = 0;// 不执行操作
    public static final int ACTION_SELECT = 1;// 执行选择操作
    public static final String EXTRA_SELECT_IDS = "select_ids";// 选择对应项目的ids
    public static final String EXTRA_SELECT_ID = "select_id";// 选择对应项目的id
    public static final String EXTRA_SELECT_NAME = "select_name";// 选择的对应项目的名称
    /**
     * 某些地方需要传递如ListView Position的数据
     */
    public static final String EXTRA_POSITION = "position";
    public static final int INVALID_POSITION = -1;

    // 用户信息参数，很多地方需要
    public static final String EXTRA_USER_ID = "userId";// userId
    public static final String EXTRA_NICK_NAME = "nickName";// nickName
    public static final String EXTRA_NICK_CODE = "usercode";// nickName
    public static final String EXTRA_EM_CODE = "usercode";// nickName
    public static final String EXTRA_IS_GROUP_CHAT = "isGroupChat";// 是否是群聊

    // BusinessCircleActivity需要的
    public static final String EXTRA_CIRCLE_TYPE = "circle_type";// 看的商务圈类型
    public static final int CIRCLE_TYPE_MY_BUSINESS = 0;// 看的商务圈类型,是我的商务圈
    public static final int CIRCLE_TYPE_PERSONAL_SPACE = 1;// 看的商务圈类型，是个人空间

    /**
     * 商务圈发布的常量
     */
    /* 发说说(图文) */
    public static final String EXTRA_IMAGES = "images";// 预览的那组图片
    public static final String EXTRA_CHANGE_SELECTED = "change_selected";// 是否可以改变选择，这样在ActivityResult中会回传重新选择的结果

    public static final String EXTRA_MSG_ID = "msg_id";// 公共消息id
    public static final String EXTRA_FILE_PATH = "file_path";// 语音、视频文件路径
    public static final String FILE_PAT_NAME = "file_name";//文件的名字
    public static final String EXTRA_IMAGE_FILE_PATH = "image_file_path";// 图片文件路径
    public static final String EXTRA_TIME_LEN = "time_len";// 语音、视频文件时长
    //位置经纬度
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    /* IM */
    public static final String EXTRA_FRIEND = "friend";

    /* 进入SingleImagePreviewActivity需要带上的参数 */
    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_IMAGE_SESSION = "image_session";


    public static final String EXTRA_URL = "url";
    public static final String EXTRA_TITLE = "title";


    public static final String UPHEAD = "UPHEAD";
    public  static  final  String BAIDU_PUSH = "BAIDU_PUSH";
    public static final String FRIEND = "friend";


    public static final String UPDATA_ERP_CHANGE = "com.app.home.update";//更新账套信息
    public static final String CHANGE_WORK_TASK = "CHANGE_WORK_TASK";//自动打卡任务变更
    public static final String CHANGE_MISSION_TASK = "CHANGE_MISSION_TASK";//自动外勤任务变更
    public static final String INIT_MISSION_TASK = "INIT_MISSION_TASK";//初始化外勤签到任务
    public static final String DEPOSIT_NOTICE_TASK = "DEPOSIT_NOTICE_TASK";//预约计划提醒任务
    public static final String NEW_FUNCTION_NOTICE = "NEW_FUNCTION_NOTICE";

    public static final String action_save_ = "NEW_FUNCTION_NOTICE";




    public static final String UPDATA_UUHELPER = "UPDATA_UUHELPER";
    public static final String UUSPORT_PUSH = "UUSPORT_PUSH";
    public static final String UPDATE_STEPRANKING_PRISE = "UPDATE_STEPRANKING_PRISE";

    public static final int RESULT_CODE = 0x865;
}
