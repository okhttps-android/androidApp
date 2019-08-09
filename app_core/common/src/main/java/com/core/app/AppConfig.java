package com.core.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.core.base.NotProguard;
import com.core.model.ConfigBean;

@NotProguard
public class AppConfig {
    public static final boolean COMPANY = true;//不可以乱动，调试接口
    public static final boolean IS_MISSION = true;//是否自动外勤
    public static final String TAG = "roamer";
    public static final boolean DEBUG = true;

    public final static String ALARMA_CLICK = "ALARMA_CLICK";//判断是否选中提醒
    public final static String AUTO_MISSION = "AUTO_MISSION";//是否自动外勤
    public final static String NEED_PROCESS = "NEED_PROCESS";//外勤设置是否需要审批
    public final static String FACE_SIGN = "FACE_SIGN";//外勤设置是否开启人脸打卡  1:打开 2:关闭  -1:没有人脸设置

    public final static String ALARM_MISSION_TIME = "ALARM_MISSION_TIME";//提醒时间
    public final static String ALARM_MISSION_DISTANCE = "ALARM_MISSION_DISTANCE";//有效距离
    public final static String AUTO_MISSION_TIME = "AUTO_MISSION_TIME";//预留时间
    public final static String HAVE_OUT_PLAN = "HAVE_OUT_PLAN";//是否有自动外勤计划

    public final static String IS_ADMIN = "IS_ADMIN";//管理员状态  1.管理员  2.非管理员

    /* 应用程序包名 */
    public static final String sPackageName = "com.xzjmyk.pm.activity";

    /* 请求配置的接口 */
    public static final String CONFIG_URL = "http://113.105.74.140:8092/config";
    //public static final String CONFIG_ URL = "http://192.168.253.155:8092/config";
    /* 分页的Size */
    public static final int PAGE_SIZE = 500;

    /* 基本地址 */
    public String apiUrl;// Api 的服务器地址
    public String uploadUrl;// 上传 的服务器地址
    public String downloadAvatarUrl;// 头像下载地址
    public String XMPPHost;// Xmpp 服务器地址
    public String XMPPDomain;// Xmpp群聊地址
    public int XMPP_PORT = 5222;
    public String help_url;// 帮助页面的地址
    public String MeetingHost;//音视频地址

    /* Api地址--》衍生地址 */
    /* 注册登录 */
    public String USER_REGISTER;// 注册
    public String VERIFY_TELEPHONE;// 验证手机号有没有被注册
    public String USER_LOGIN;// 登陆
    public String SEND_AUTH_CODE;// 获取手机验证码
    public String USER_LOGIN_AUTO;// 检测Token是否过期 0未更换 1更换过
    public String USER_PASSWORD_RESET;// 用户重置密码

    /* 用户 */
    public String USER_UPDATE;// 用户资料修改
    public String USER_GET_URL;// 获取用户资料，更新本地数据的接口
    public String USER_PHOTO_LIST;// 获取相册列表
    public String USER_QUERY;// 查询用户列表
    public String USER_NEAR;//查询搜索列表
    /* 附近 */
    public String NEARBY_USER;// 获取附近的用户

    /* 商务圈 */
    public String USER_CIRCLE_MESSAGE;// 获取某个人的商务圈消息
    public String DOWNLOAD_CIRCLE_MESSAGE;// 下载商务圈消息

    /* 好友 */
    public String FRIENDS_ATTENTION_LIST;// 获取关注列表
    public String FRIENDS_REMARK;// 备注好友
    public String FRIENDS_BLACKLIST_ADD;// 拉黑
    public String FRIENDS_ATTENTION_DELETE;// 取消关注
    public String FRIENDS_DELETE;// 删除好友
    public String FRIENDS_ATTENTION_ADD;// 加关注
    public String FRIENDS_BLACKLIST_DELETE;// 取消拉黑

    /* 群聊 */
    public String ROOM_ADD;// 创建群组
    public String ROOM_DELETE;// 删除群组----   删除
    public String ROOM_UPDATE;// 设置群组
    public String ROOM_GET;// 获取群组
    public String ROOM_LIST;// 获取群组列表
    public String ROOM_MEMBER_UPDATE;// 设置群员
    public String ROOM_MEMBER_DELETE;// 删除成员-----   退出群聊
    public String ROOM_MEMBER_GET;// 获取成员
    public String ROOM_MEMBER_LIST;// 获取成员列表
    public String ROOM_LIST_HIS;// 获取某个用户已经加入的房间列表
    public String ROOM_JOIN;// 加入房间
    /* 商务圈 */
    public String MSG_ADD_URL;// 发布一条公共消息的接口
    public String MSG_LIST;// 获取公共消息的接口
    public String MSG_USER_LIST;// 获取某个用户的最新公共消息
    public String MSG_GETS;// 根据IDS批量获取公共消息的接口(我的商务圈使用)
    public String MSG_GET;// 根据ID获取公共消息
    public String CIRCLE_MSG_DELETE;// 删除一条商务圈消息
    public String MSG_PRAISE_ADD;// 赞
    public String MSG_PRAISE_DELETE;// 取消赞
    public String CIRCLE_MSG_LATEST;// 获取人才榜最新消息
    public String CIRCLE_MSG_HOT;// 最热人才榜
    public String MSG_COMMENT_ADD;// 增加一条评论
    public String MSG_COMMENT_DELETE;// 删除一条评论
    public String MSG_COMMENT_LIST;// 获取评论列表
    /* 上传 的服务器地址--》衍生地址 */
    public String UPLOAD_URL;// 上传图片接口
    public String AVATAR_UPLOAD_URL;// 上传头像接口

    public String UPLOAD_CHANDLD_URL;

    /* 头像下载地址--》衍生地址 */
    public String AVATAR_ORIGINAL_PREFIX;// 头像原图前缀地址
    public String AVATAR_THUMB_PREFIX;// 头像缩略图前缀地址
    public String APP_QUER_YUSER;// 添加联系人接口

    /**
     * 会改变的配置
     **/
    public static AppConfig initConfig(Context context, ConfigBean configBean) {
        if (configBean == null) {// 即时从服务器上获取匹配信息失败，也要new一个空的configBean传进来
            configBean = new ConfigBean();
        }
        SharedPreferences configSharePre = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        Editor editor = configSharePre.edit();
        /* 1、Api 的服务器地址 */
        String apiUrl = configBean.getApiUrl();
        Log.d("wang", "apiUrl initConfig" + apiUrl);
        if (TextUtils.isEmpty(apiUrl)) {
            apiUrl = configSharePre.getString("apiUrl", "http://192.168.1.240/api/v1/");
        } else {
            editor.putString("apiUrl", apiUrl);
        }

        /* 2、上传 的服务器地址 */
        String uploadUrl = configBean.getUploadUrl();
        if (TextUtils.isEmpty(uploadUrl)) {
            uploadUrl = configSharePre.getString("uploadUrl", "http://192.168.1.240/");
        } else {
            editor.putString("uploadUrl", uploadUrl);
        }

        /* 3、头像下载地址 */
        String downloadAvatarUrl = configBean.getDownloadAvatarUrl();
        if (TextUtils.isEmpty(downloadAvatarUrl)) {
            downloadAvatarUrl = configSharePre.getString("downloadAvatarUrl", "http://192.168.1.240/");
        } else {
            editor.putString("downloadAvatarUrl", downloadAvatarUrl);
        }

        /* 4、Xmpp 服务器IP地址 */
        String XMPPHost = configBean.getXMPPHost();
        if (TextUtils.isEmpty(XMPPHost)) {
            XMPPHost = configSharePre.getString("XMPPHost", "121.37.30.15");
        } else {
            editor.putString("XMPPHost", XMPPHost);
        }
        /* 5、Xmpp 服务器域名 */
        String XMPPDomain = configBean.getXMPPDomain();
        if (TextUtils.isEmpty(XMPPDomain)) {
            XMPPDomain = configSharePre.getString("XMPPDomain", "www.youjob.co");
        } else {
            editor.putString("XMPPDomain", XMPPDomain);
        }
        editor.commit();

        /* 赋值给AppConfig对象 */
        AppConfig config = new AppConfig();
        config.apiUrl = apiUrl;
        config.uploadUrl = uploadUrl;
        config.downloadAvatarUrl = downloadAvatarUrl;
        config.XMPPHost = XMPPDomain;// 直接使用域名，不使用IP,IP连接会出现问题，第一次连接Xmpp会发出连接异常关闭的错误，具体原因未知
        config.XMPPDomain = XMPPDomain;
        config.help_url = configBean.getHelpURL();// help_url没有缓存
        config.MeetingHost = configBean.getMeetingHost();
        initApiUrls(config);// apiUrl
        initOthersUrls(config);
        return config;
    }

    private static void initApiUrls(AppConfig config) {
        String apiUrl = config.apiUrl;
//        if (BaseConfig.isDebug()){
//            apiUrl=Constants.IM_BASE_URL();
//        }
        /* 登陆注册 */
        config.USER_REGISTER = apiUrl + "user/register";// 注册
        config.VERIFY_TELEPHONE = apiUrl + "verify/telephone";// 验证手机号有没有被注册
        config.USER_LOGIN = apiUrl + "user/login";// 登陆
        config.SEND_AUTH_CODE = apiUrl + "basic/randcode/sendSms";// 获取手机验证码
        config.USER_LOGIN_AUTO = apiUrl + "user/login/auto";// 检测Token是否过期
        config.USER_PASSWORD_RESET = apiUrl + "user/password/reset";// 用户重置密码
        /* 用户 */
        config.USER_UPDATE = apiUrl + "user/update";// 用户资料修改
        config.USER_GET_URL = apiUrl + "user/get";// 获取用户资料，更新本地数据的接口
        config.USER_PHOTO_LIST = apiUrl + "user/photo/list";// 获取相册列表
        config.USER_QUERY = apiUrl + "user/query";// 查询用户列表

        config.USER_NEAR = apiUrl + "nearby/user";//查询搜索列表

        /* 附近 */
        config.NEARBY_USER = apiUrl + "nearby/user";// 获取附近的用户

        /* 商务圈 */
        config.USER_CIRCLE_MESSAGE = apiUrl + "b/circle/msg/user/ids";// 获取某个人的商务圈消息
        config.DOWNLOAD_CIRCLE_MESSAGE = apiUrl + "b/circle/msg/ids";// 下载商务圈消息

        /* 好友 */
        config.FRIENDS_ATTENTION_LIST = apiUrl + "friends/attention/list";// 获取关注列表
        config.FRIENDS_REMARK = apiUrl + "friends/remark";// 备注好友
        config.FRIENDS_BLACKLIST_ADD = apiUrl + "friends/blacklist/add";// 拉黑
        config.FRIENDS_ATTENTION_DELETE = apiUrl + "friends/attention/delete";// 取消关注
        config.FRIENDS_DELETE = apiUrl + "friends/delete";// 删除好友
        config.FRIENDS_ATTENTION_ADD = apiUrl + "friends/attention/add";// 加关注
        config.APP_QUER_YUSER = apiUrl + "user/appQueryuser";// 添加通讯录
        config.FRIENDS_BLACKLIST_DELETE = apiUrl + "friends/blacklist/delete";// 取消拉黑

        /* 群聊 */
        config.ROOM_ADD = apiUrl + "room/add";// 创建群组
        config.ROOM_DELETE = apiUrl + "room/delete";// 删除群组
        config.ROOM_UPDATE = apiUrl + "room/update";// 设置群组
        config.ROOM_GET = apiUrl + "room/get";// 获取群组
        config.ROOM_LIST = apiUrl + "room/list";// 获取群组列表
        config.ROOM_MEMBER_UPDATE = apiUrl + "room/member/update";// 设置群员
        config.ROOM_MEMBER_DELETE = apiUrl + "room/member/delete";// 删除成员
        config.ROOM_MEMBER_GET = apiUrl + "room/member/get";// 获取成员
        config.ROOM_MEMBER_LIST = apiUrl + "room/member/list";// 获取成员列表
        config.ROOM_LIST_HIS = apiUrl + "/room/list/his";// 获取某个用户已加入的房间列表
        config.ROOM_JOIN = apiUrl + "/room/join";// 加入房间

        /* 商务圈 */
        config.MSG_ADD_URL = apiUrl + "b/circle/msg/add";// 发布一条公共消息的接口
        config.MSG_LIST = apiUrl + "b/circle/msg/list";// 获取公共消息的接口
        config.MSG_USER_LIST = apiUrl + "b/circle/msg/user";// 获取某个用户的最新公共消息
        config.MSG_GETS = apiUrl + "b/circle/msg/gets";// 根据IDS批量获取公共消息的接口(我的商务圈使用)
        config.MSG_GET = apiUrl + "b/circle/msg/get";// 根据ID获取公共消息
        config.CIRCLE_MSG_DELETE = apiUrl + "b/circle/msg/delete";// 删除一条商务圈消息
        config.MSG_PRAISE_ADD = apiUrl + "b/circle/msg/praise/add";// 赞
        config.MSG_PRAISE_DELETE = apiUrl + "b/circle/msg/praise/delete";// 取消赞
        config.CIRCLE_MSG_LATEST = apiUrl + "b/circle/msg/latest";// 最新人才榜
        config.CIRCLE_MSG_HOT = apiUrl + "b/circle/msg/hot";// 最热人才榜
        config.MSG_COMMENT_ADD = apiUrl + "b/circle/msg/comment/add";// 增加一条评论
        config.MSG_COMMENT_DELETE = apiUrl + "b/circle/msg/comment/delete";// 删除一条评论
        config.MSG_COMMENT_LIST = apiUrl + "b/circle/msg/comment/list";// 删除一条评论

    }

    private static void initOthersUrls(AppConfig config) {
        // uploadUrl
        config.UPLOAD_URL = config.uploadUrl + "upload/UploadServlet";// 上传图片接口
        config.AVATAR_UPLOAD_URL = config.uploadUrl + "upload/UploadAvatarServlet";// 上传头像接口
        // downloadAvatarUrl
        config.AVATAR_ORIGINAL_PREFIX = config.downloadAvatarUrl + "avatar/o";// 头像原图前缀地址
        config.AVATAR_THUMB_PREFIX = config.downloadAvatarUrl + "avatar/t";// 头像缩略图前缀地址
    }


}
