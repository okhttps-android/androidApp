package com.xzjmyk.pm.activity.ui.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.dao.UserDao;
import com.core.model.AttentionUser;
import com.core.model.CircleMessage;
import com.core.model.EmployeesEntity;
import com.core.model.HrorgsEntity;
import com.core.model.MyPhoto;
import com.core.model.User;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.helper.LoginHelper;
import com.core.utils.sp.UserSp;
import com.core.widget.DataLoadView;
import com.core.xmpp.dao.CircleMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.OnCompleteListener;
import com.core.xmpp.model.MucRoom;
import com.umeng.analytics.MobclickAgent;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.db.dao.MyPhotoDao;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.xzjmyk.pm.activity.ui.account
 * @作者:王阳
 * @创建时间: 2015年10月27日 下午3:03:06
 * @描述: 数据更新界面 ,下载的数据： 1、我的商务圈最新数据 2、我的通讯录 3、更新用户基本资料 4、我的相册下载
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容:
 */
public class DataDownloadActivity extends BaseActivity {

    private final int STATUS_NO_RESULT = 0;// 请求中，尚未返回
    private final int STATUS_FAILED = 1;// 已经返回，失败了
    private final int STATUS_SUCCESS = 2;// 已经返回，成功了
    private DataLoadView mDataLoadView;
    private String mLoginUserId;

    private int circle_msg_download_status = STATUS_NO_RESULT;// 商务圈ids下载
    private int address_user_download_status = STATUS_NO_RESULT;// 通讯录下载
    private int user_info_download_status = STATUS_NO_RESULT;// 个人基本资料下载
    private int user_photo_download_status = STATUS_NO_RESULT;// 我的相册下载
    private int room_download_status = STATUS_NO_RESULT;// 我的房间下载
    private int company_download_status = STATUS_NO_RESULT;//企业架构数据返回
    private Dialog mBackDialog;
    private DBManager manager;
    private final int LOAD_COMPANY_DATA = 1;//企业架构数据

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case LOAD_COMPANY_DATA:
                    Log.i("newdata", "handleMessage:" + msg.getData().getString("result"));
                    writeData(msg);//写入缓存
                    company_download_status = STATUS_SUCCESS;
                    if (address_user_download_status != STATUS_SUCCESS) {// 没有成功，就下载
                        address_user_download_status = STATUS_NO_RESULT;// 初始化下载状态
                        downloadAddressBook();
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    Log.i("newdata", "handleMessage:" + msg.getData().getString("result"));
                    company_download_status = STATUS_FAILED;
                    if (address_user_download_status != STATUS_SUCCESS) {// 没有成功，就下载
                        address_user_download_status = STATUS_NO_RESULT;// 初始化下载状态
                        downloadAddressBook();
                    }
                    break;

            }
        }
    };


    public void writeData(android.os.Message msg) {
        String result = msg.getData().getString("result");
        if (!JSONUtil.validate(result)){
            return;
        }
        
        JSONObject jsonobject = JSON.parseObject(result);
        if(jsonobject==null){
            return;
        }
        String server_time = jsonobject.getString("sysdate");//服务器系统时间
        //实体类
        List<HrorgsEntity> hrorgsEntities = JSON.parseArray(jsonobject.getString("hrorgs"), HrorgsEntity.class);
        List<EmployeesEntity> employeesEntities = JSON.parseArray(jsonobject.getString("employees"), EmployeesEntity.class);
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        HrorgsEntity hrEntity = DBManager.getInstance().select_getRootData(new String[]{master}, "whichsys=?");
        boolean isFristLoad = true;//是否第一次加载
        if (hrEntity != null) {
            isFristLoad = false;
        }
        if (!hrorgsEntities.isEmpty() || !employeesEntities.isEmpty()) {
            insertDataSqlite(isFristLoad, hrorgsEntities, employeesEntities,
                    server_time);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_download);
        String name = CommonUtil.getName();
        if (!StringUtil.isEmpty(name))
            MobclickAgent.onProfileSignIn(name);
        UserSp.getInstance(DataDownloadActivity.this).setUpdate(false);// 进入下载资料界面，就将该值赋值false
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        //mHandler = new Handler();
        setTitle(R.string.data_update);
        initView();
        manager=DBManager.getInstance();
        startDownload();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//       ToastUtil.showToast(this,"系统内存不足，当前界面销毁！");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        ToastUtil.showToast(this, "当前界面恢复！");
    }

    private void initView() {
        mDataLoadView = (DataLoadView) findViewById(R.id.data_load_view);
        mDataLoadView.setLoadingEvent(new DataLoadView.LoadingEvent() {
            @Override
            public void load() {
                startDownload();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DBManager.getInstance().closeDB();
    }

    private void startDownload() {
        mDataLoadView.showLoading();
        //company_download_status=STATUS_SUCCESS;
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (isB2b) company_download_status = STATUS_NO_RESULT;
        if (company_download_status != STATUS_SUCCESS) {
            company_download_status = STATUS_NO_RESULT;
            downloadCompanysContact();//下载企业架构数据
        }
        if (circle_msg_download_status != STATUS_SUCCESS) {// 没有成功，就下载
            circle_msg_download_status = STATUS_NO_RESULT;// 初始化下载状态
            downloadCircleMessage();
        }

        if (user_info_download_status != STATUS_SUCCESS) {// 没有成功，就下载
            user_info_download_status = STATUS_NO_RESULT;// 初始化下载状态
            downloadUserInfo();
        }

        if (user_photo_download_status != STATUS_SUCCESS) {// 没有成功，就下载
            user_photo_download_status = STATUS_NO_RESULT;// 初始化下载状态
            downloadUserPhoto();
        }
        if (room_download_status != STATUS_SUCCESS) {// 没有成功，就下载
            room_download_status = STATUS_NO_RESULT;// 初始化下载状态
            downloadRoom();
        }
    }

    /**
     * @desc:下载企业通讯录数据
     * @author：Arison on 2016/9/22
     */
    public void downloadCompanysContact() {
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        String commpany = CommonUtil.getSharedPreferences(ct, "erp_commpany");
        String lastdate = "";
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/getAllHrorgEmps.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("master", master);
        param.put("lastdate", lastdate);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers,
                LOAD_COMPANY_DATA, null, null, "get");

    }

    private void endDownload() {
        // 只有有一个下载没返回，那么就继续等待   ---等待请求----
        if (circle_msg_download_status == STATUS_NO_RESULT || address_user_download_status == STATUS_NO_RESULT
                || user_info_download_status == STATUS_NO_RESULT || user_photo_download_status == STATUS_NO_RESULT
                || room_download_status == STATUS_NO_RESULT || company_download_status == STATUS_NO_RESULT) {
            return;
        }
        // 只要有一个下载失败，那么显示更新失败。就继续下载
        if (circle_msg_download_status == STATUS_FAILED || address_user_download_status == STATUS_FAILED
                || user_info_download_status == STATUS_FAILED || user_photo_download_status == STATUS_FAILED
                || room_download_status == STATUS_FAILED || company_download_status == STATUS_FAILED) {
            //mDataLoadView.showFailed();
            EnterHome();
        } else {// 所有数据加载完毕,跳转回用户操作界面
            EnterHome();
        }
    }

    private void EnterHome() {
        if (mBackDialog != null && mBackDialog.isShowing()) {
            mBackDialog.dismiss();
        }
        UserSp.getInstance(DataDownloadActivity.this).setUpdate(true);
        if (mContext != null) {
            LoginHelper.broadcastLogin(mContext);
        }
        // 此处BUG：如果MainActivity不存在，那么这个之前的界面都不能销毁，后面可以加个广播销毁他们
        Intent intent = new Intent(DataDownloadActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//Compat
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
        overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    /**
     * 下载商务圈消息
     */
    private void downloadCircleMessage() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        StringJsonArrayRequest<CircleMessage> request = new StringJsonArrayRequest<CircleMessage>(
                mConfig.DOWNLOAD_CIRCLE_MESSAGE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                circle_msg_download_status = STATUS_FAILED;// 失败
                endDownload();
            }
        }, new StringJsonArrayRequest.Listener<CircleMessage>() {
            @Override
            public void onResponse(ArrayResult<CircleMessage> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    CircleMessageDao.getInstance().addMessages(mHandler, mLoginUserId, result.getData(),
                            new OnCompleteListener() {
                                @Override
                                public void onCompleted() {
                                    circle_msg_download_status = STATUS_SUCCESS;// 成功
                                    endDownload();
                                }
                            });
                } else {
                    circle_msg_download_status = STATUS_FAILED;// 失败
                    endDownload();
                }
            }
        }, CircleMessage.class, params);
        addDefaultRequest(request);
    }

    /**
     * 下载我的关注，包括我的好友
     */
    private void downloadAddressBook() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        LogUtil.i("mAccessToken=" + MyApplication.getInstance().mAccessToken);
        StringJsonArrayRequest<AttentionUser> request = new StringJsonArrayRequest<AttentionUser>(
                mConfig.FRIENDS_ATTENTION_LIST, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                address_user_download_status = STATUS_FAILED;// 失败
                endDownload();
            }
        }, new StringJsonArrayRequest.Listener<AttentionUser>() {
            @Override
            public void onResponse(ArrayResult<AttentionUser> result) {
                boolean success = Result.defaultParser(mContext, result, false);
                Log.i("DataDownload", "我的关注：" +JSON.toJSONString(result));
                Log.i("DataDownload", "success:" + success);
                //服务器此处返回用户不存在，理论上是不能去掉success判断的
//						if (success) {
                FriendDao.getInstance().addAttentionUsers(mHandler, mLoginUserId, result.getData(),
                        new OnCompleteListener() {
                            @Override
                            public void onCompleted() {
                                address_user_download_status = STATUS_SUCCESS;// 成功
                                endDownload();
                            }
                        });
//						} else {
//							address_user_download_status = STATUS_FAILED;// 失败
//							endDownload();
//						}
            }
        }, AttentionUser.class, params);
        addDefaultRequest(request);
    }

    /**
     * 下载个人基本资料
     */
    private void downloadUserInfo() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);

        StringJsonObjectRequest<User> request = new StringJsonObjectRequest<User>(mConfig.USER_GET_URL,
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ToastUtil.showErrorNet(mContext);
                        user_info_download_status = STATUS_FAILED;// 失败
                        endDownload();
                    }
                }, new StringJsonObjectRequest.Listener<User>() {
            @Override
            public void onResponse(ObjectResult<User> result) {
                boolean updateSuccess = false;
                if (Result.defaultParser(mContext, result, true)) {
                    User user = result.getData();
                    updateSuccess = UserDao.getInstance().updateByUser(user);
                    // 设置登陆用户信息
                    if (updateSuccess) {// 如果成功，那么就将User的详情赋值给全局变量
                        MyApplication.getInstance().mLoginUser = user;
                    }
                }
                if (updateSuccess) {
                    user_info_download_status = STATUS_SUCCESS;// 成功
                } else {
                    user_info_download_status = STATUS_FAILED;// 失败
                }
                endDownload();
            }
        }, User.class, params);
        addDefaultRequest(request);
    }

    /**
     * 下载我的相册
     */
    private void downloadUserPhoto() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);

        StringJsonArrayRequest<MyPhoto> request = new StringJsonArrayRequest<MyPhoto>(mConfig.USER_PHOTO_LIST,
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ToastUtil.showErrorNet(mContext);
                        user_photo_download_status = STATUS_FAILED;// 失败
                        endDownload();
                    }
                }, new StringJsonArrayRequest.Listener<MyPhoto>() {
            @Override
            public void onResponse(ArrayResult<MyPhoto> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    MyPhotoDao.getInstance().addPhotos(mHandler, mLoginUserId, result.getData(),
                            new OnCompleteListener() {
                                @Override
                                public void onCompleted() {
                                    user_photo_download_status = STATUS_SUCCESS;// 成功
                                    endDownload();
                                }
                            });
                } else {
                    user_photo_download_status = STATUS_FAILED;// 失败
                    endDownload();
                }
            }
        }, MyPhoto.class, params);
        addDefaultRequest(request);
    }

    /**
     * 下载我的房间
     */
    private void downloadRoom() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("type", "0");
        params.put("pageIndex", "0");
        params.put("pageSize", "200");// 给一个尽量大的值

        StringJsonArrayRequest<MucRoom> request = new StringJsonArrayRequest<MucRoom>(mConfig.ROOM_LIST_HIS,
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ToastUtil.showErrorNet(mContext);
                        room_download_status = STATUS_FAILED;// 失败
                        endDownload();
                    }
                }, new StringJsonArrayRequest.Listener<MucRoom>() {
            @Override
            public void onResponse(ArrayResult<MucRoom> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                Log.i("DataDownload", "我的房间：" + JSON.toJSONString(result));
                if (success) {
                    FriendDao.getInstance().addRooms(mHandler, mLoginUserId, result.getData(),
                            new OnCompleteListener() {
                                @Override
                                public void onCompleted() {
                                    room_download_status = STATUS_SUCCESS;// 成功
                                    endDownload();
                                }
                            });
                } else {
                    room_download_status = STATUS_FAILED;// 失败
                    endDownload();
                }
            }
        }, MucRoom.class, params);
        addDefaultRequest(request);
    }


    /**
     * @author Administrator
     * @功能:写入数据库 isFristLoad 标志是否是第一次加载(数据库是否有缓存);
     * 因为第一次，flag字段不管是更新或者是插入，都执行插入操作;
     */
    public void insertDataSqlite(boolean isFristLoad,
                                 List<HrorgsEntity> hrorgsEntities,
                                 List<EmployeesEntity> employeesEntities
            , String servertime) {
        Log.i(TAG, "开始时间：" + CommonUtil.getStringDate(System.currentTimeMillis()));
        if (isFristLoad) {
            try {
                DBManager.getInstance().saveHrogrs(hrorgsEntities);
                DBManager.getInstance().saveEmployees(employeesEntities);
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            synSqliteDataforServer(hrorgsEntities, employeesEntities);
        }
        DBManager.getInstance().deleteHrogrsAndEmployees();
        Map<String, Object> dateCaches = new HashMap<String, Object>();
        String time = dateMinute(servertime);
        Log.i("缓存时间相减后", "缓存时间相减后：" + time);
        dateCaches.put("ed_lastdate", time);
        dateCaches.put("ed_kind", "通讯录");
        dateCaches.put("ed_company", CommonUtil.getSharedPreferences(ct, "erp_commpany"));
        dateCaches.put("ed_whichsys", CommonUtil.getSharedPreferences(ct, "erp_master"));
        String db_time = DBManager.getInstance().select_getCacheTime(new String[]{}, "ed_company=? and ed_whichsys=?");
        if (db_time != null) {
            DBManager.getInstance().updateCacheTime(dateCaches);
        } else {
            DBManager.getInstance().saveCacheTime(dateCaches);
        }
        Log.i("结束时间", "结束时间：" + CommonUtil.getStringDate(System.currentTimeMillis()));
    }

    /**
     * @author Administrator
     * @功能:时间减法
     */
    private String dateMinute(String datetime) {
        String str = datetime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    /**
     * @author Administrator
     * @功能:同步数据
     */
    private void synSqliteDataforServer(List<HrorgsEntity> hrorgsEntities, List<EmployeesEntity> employeesEntities) {
        if (!hrorgsEntities.isEmpty()) {
            List<HrorgsEntity> insertHrorgsList = new ArrayList<HrorgsEntity>();
            List<HrorgsEntity> updateHrorgsList = new ArrayList<HrorgsEntity>();
            for (int i = 0; i < hrorgsEntities.size(); i++) {
                HrorgsEntity entity = hrorgsEntities.get(i);
                String or_code = entity.getOr_code();
                String whichsys = entity.getWhichsys();
                if ("UPDATE".equals(entity.getFlag())) {
                    HrorgsEntity hentity = DBManager.getInstance().select_getRootData(new String[]{or_code, whichsys}, "or_code=? and whichsys=?");
                    if (hentity != null) {
                        updateHrorgsList.add(entity);
                    } else {
                        insertHrorgsList.add(entity);
                    }
                } else if ("INSERT".equals(entity.getFlag())) {
                    insertHrorgsList.add(entity);
                }
            }
            DBManager.getInstance().saveHrogrs(insertHrorgsList);
            DBManager.getInstance().updateHrogrs(updateHrorgsList);
        }
        if (!employeesEntities.isEmpty()) {
            List<EmployeesEntity> insertEmployeesList = new ArrayList<EmployeesEntity>();
            List<EmployeesEntity> updateEmployeesList = new ArrayList<EmployeesEntity>();
            for (int i = 0; i < employeesEntities.size(); i++) {
                EmployeesEntity eEntity = employeesEntities.get(i);
                String em_code = eEntity.getEM_CODE();
                String whichsys = eEntity.getWHICHSYS();
                if (StringUtil.isEmpty(em_code)) return;
                if ("UPDATE".equals(eEntity.getFLAG())) {
                    List<EmployeesEntity> tempEntity = DBManager.getInstance().select_getEmployee(new String[]{em_code, whichsys}, "em_code=? and whichsys=?");
                    if (tempEntity.isEmpty()) {
                        insertEmployeesList.add(eEntity);
                    } else {
                        updateEmployeesList.add(eEntity);
                    }
                } else if ("INSERT".equals(eEntity.getFLAG())) {
                    insertEmployeesList.add(eEntity);
                }
            }
            DBManager.getInstance().saveEmployees(insertEmployeesList);
            DBManager.getInstance().updateEmployees(updateEmployeesList);
        }
    }


    @Override
    public void onBackPressed() {
        doBack();
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    private void doBack() {
        if (mBackDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.app_dialog_title)
                    .setMessage(R.string.data_not_update_exit).setNegativeButton(getString(R.string.common_cancel), null)
                    .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoginHelper.broadcastLoginGiveUp(DataDownloadActivity.this);
                            finish();
                        }
                    });
            mBackDialog = builder.create();
        }
        mBackDialog.show();
    }

}
