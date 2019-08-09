package com.xzjmyk.pm.activity.ui.circle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.common.ui.CameraUtil;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.MyPhoto;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.xmpp.dao.CircleMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.app.AppConstant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appme.other.activity.BasicInfoActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.adapter.PersonalQzoneAdapter;
import com.xzjmyk.pm.activity.bean.circle.Comment;
import com.xzjmyk.pm.activity.bean.circle.PublicMessage;
import com.xzjmyk.pm.activity.db.dao.MyPhotoDao;
import com.xzjmyk.pm.activity.util.im.helper.FileDataHelper;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.view.PMsgBottomView;
import com.xzjmyk.pm.activity.view.ResizeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.core.app.AppConstant.CIRCLE_TYPE_MY_BUSINESS;

/**
 * 工作圈个人中心
 * Created by FANGlh on 2016/12/4.
 */
public class PersonalQzoneActivity extends BaseActivity implements showCEView {
    /**
     * 本界面的类型 Constant.CIRCLE_TYPE_MY_BUSINESS,我的商务圈<br/>
     * Constant。CIRCLE_TYPE_PERSONAL_SPACE，个人空间<br/>
     */
    private int mType;
    /* mPageIndex仅用于商务圈情况下 */
    private int mPageIndex = 0;
    private PullToRefreshListView mPullToRefreshListView;//
    /* 封面视图 */
    private View mMyCoverView;// 封面root view
    private ImageView imgHead;// 封面图片ImageView
    private Button mInviteBtn;// 面试邀请按钮
    private ImageView mAvatarImg;// 用户头像
    private ResizeLayout mResizeLayout;
    private PMsgBottomView mPMsgBottomView;

    private List<PublicMessage> mMessages = new ArrayList<PublicMessage>();

    private PersonalQzoneAdapter mAdapter;

    private String mLoginUserId;// 当前登陆用户的UserId
    private String mLoginNickName;// 当前登陆用户的昵称

    /* 当前选择的是哪个用户的个人空间,仅用于查看个人空间的情况下 */
    private String mUserId;
    private String mNickName;
    public showCEView ceView;
    private File vFile;
    private Comparator<? super Comment> comp = new Comparator<Comment>() {
        @Override
        public int compare(Comment comment, Comment t1) {

            return (int) (comment.getTime() - t1.getTime());
        }
    };
    private TextView mQzonename;
    private DBManager manager;
    private PopupWindow mMoreWindow;
    private View mMoreMenuView;
    private TextView sendqzonetext_tv;
    private TextView sendqzonepicture_tv;
    private TextView sendqzonnevideo_tv;
    private TextView sendqzonecancel_tv;
    private Intent intent;
    private ImageView msendmessageiv;

    public void setShowCEViewListener(showCEView ceView) {
        this.ceView = ceView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        mLoginNickName = MyApplication.getInstance().mLoginUser.getNickName();
        if (TextUtils.isEmpty(mLoginUserId)) {// 容错
            return;
        }

        if (getIntent() != null) {
            mType = getIntent().getIntExtra(AppConstant.EXTRA_CIRCLE_TYPE, CIRCLE_TYPE_MY_BUSINESS);// 默认的为查看我的商务圈
            mUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
            mNickName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        }

        if (!isMyBusiness()) {// 如果查看的是个人空间的话，那么mUserId必须要有意义
            if (TextUtils.isEmpty(mUserId)) {// 没有带userId参数，那么默认看的就是自己的空间
                mUserId = mLoginUserId;
                mNickName = mLoginNickName;
            }
        }

        setContentView(R.layout.activity_business_circle);

        initView();
    }

    /**
     * 是否是商务圈类型
     *
     * @return
     */
    private boolean isMyBusiness() {
        return mType == CIRCLE_TYPE_MY_BUSINESS;
    }

    /**
     * 是否是个人空间类型之我的空间
     *
     * @return
     */
    private boolean isMySpace() {
        return mLoginUserId.equals(mUserId);
    }

    private void initView() {
        initTopTitleBar();
        initCoverView();

        mMoreMenuView = View.inflate(getApplicationContext(), R.layout.layout_menu_send_qzone_message, null);
        sendqzonetext_tv = (TextView) mMoreMenuView.findViewById(R.id.send_qzone_text);
        sendqzonepicture_tv = (TextView) mMoreMenuView.findViewById(R.id.send_qzone_picture);
        sendqzonnevideo_tv = (TextView) mMoreMenuView.findViewById(R.id.send_qzone_video);
        sendqzonecancel_tv = (TextView) mMoreMenuView.findViewById(R.id.send_qzone_cancel);
        mMoreWindow = new PopupWindow(mMoreMenuView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mMoreWindow.setAnimationStyle(R.style.MenuAnimationFade);
        mMoreWindow.setBackgroundDrawable(new BitmapDrawable());
        mMoreWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closeMorePopupWindow();
            }
        });


        sendqzonetext_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(PersonalQzoneActivity.this, SendShuoshuoActivity.class);
                intent.putExtra("type", 0);
                startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                closeMorePopupWindow();
            }
        });
        intent = new Intent();
        sendqzonepicture_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(PersonalQzoneActivity.this, SendShuoshuoActivity.class);
                intent.putExtra("type", 1);
                startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                closeMorePopupWindow();
            }
        });
        sendqzonnevideo_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(PersonalQzoneActivity.this, SendVideoActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                closeMorePopupWindow();
            }
        });
        sendqzonecancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMorePopupWindow();
            }
        });



        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPMsgBottomView = (PMsgBottomView) findViewById(R.id.bottom_view);

        mResizeLayout = (ResizeLayout) findViewById(R.id.resize_layout);
        mResizeLayout.setOnResizeListener(new ResizeLayout.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                if (oldh < h) {// 键盘被隐藏

                }
            }
        });

        mPMsgBottomView.setPMsgBottomListener(new PMsgBottomView.PMsgBottomListener() {
            @Override
            public void sendText(String text) {
                if (mCommentReplyCache != null) {
                    mCommentReplyCache.text = text;
                    addComment(mCommentReplyCache);
                    mPMsgBottomView.hide();
                }
            }
        });
        mPullToRefreshListView.getRefreshableView().addHeaderView(mMyCoverView, null, false);
        mAdapter = new PersonalQzoneAdapter(this, mMessages);
        if (isMySpace()) {
            mAdapter.setType(AppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
        } else {
            mAdapter.setType(CIRCLE_TYPE_MY_BUSINESS);
        }

//        setListenerAudio(mAdapter); //设置借口回调


        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PublicMessage message = mMessages.get((int) parent.getItemIdAtPosition(position));
                Intent intent = new Intent(PersonalQzoneActivity.this, PMsgDetailActivity.class);
                intent.putExtra("public_message", message);
                startActivity(intent);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnScrollListener(
                new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (mPMsgBottomView.getVisibility() != View.GONE) {
                            mPMsgBottomView.hide();
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    }
                }));

        if (isMyBusiness()) {
            readFromLocal();
        } else {
            requestData(true);
        }


    }

    private void initTopTitleBar() {
        if (isMyBusiness()) {
            setTitle("个人中心");
        } else {
            if (isMySpace()) {
                setTitle(R.string.my_space);
            } else {
                String name = FriendDao.getInstance().getRemarkName(mLoginUserId, mUserId);
                if (TextUtils.isEmpty(name)) {
                    name = mNickName;
                }
                setTitle(name);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMyBusiness() || isMySpace()) {// 允许发布说说等
            getMenuInflater().inflate(R.menu.menu_qzone_message, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private static final int REQUEST_CODE_SEND_MSG = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId){
            case R.id.personal_qzone_center_message:
//                mMoreWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
//                DisplayUtil.backgroundAlpha(mContext, 0.5f);
                ToastMessage("工作圈互动消息尚未完善");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeMorePopupWindow() {
        if (mMoreWindow != null) {
            mMoreWindow.dismiss();
//            mMoreWindow = null;
            DisplayUtil.backgroundAlpha(mContext, 1f);

        }

    }

    private void initCoverView() {
        mMyCoverView = LayoutInflater.from(this).inflate(R.layout.per_center_space_cover_view, null);
        imgHead = (ImageView) mMyCoverView.findViewById(R.id.cover_img);
        mInviteBtn = (Button) mMyCoverView.findViewById(R.id.invite_btn);
        mAvatarImg = (ImageView) mMyCoverView.findViewById(R.id.avatar_img);
        mQzonename = (TextView) mMyCoverView.findViewById(R.id.qzone_name);
        msendmessageiv = (ImageView) mMyCoverView.findViewById(R.id.send_qzone_message_iv);
        String en_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        manager = new DBManager(this);

        msendmessageiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMoreWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                DisplayUtil.backgroundAlpha(mContext, 0.5f);
            }
        });
        //TODo 显示个人头像左边名字 by：FANGlh
        try{
            List<EmployeesEntity> db = manager.select_getEmployee(
                    new String[]{CommonUtil.getSharedPreferences(ct, "erp_master"),
                            CommonUtil.getSharedPreferences(ct, "erp_username")}
                    , "whichsys=? and em_code=? ");

            if (!ListUtils.isEmpty(db)){
                if (!StringUtil.isEmpty(db.get(0).getEM_NAME())){
                    mQzonename.setTextColor(mContext.getResources().getColor(R.color.white));
                    mQzonename.setText(db.get(0).getEM_NAME());
                }else{
                    mQzonename.setText(MyApplication.getInstance().mLoginUser.getNickName());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        // 邀请按钮
        mInviteBtn.setVisibility(View.GONE);// TODO 面试邀请按钮放这里太难看了，隐藏掉算求
        // 头像
        if (isMyBusiness() || isMySpace()) {
            AvatarHelper.getInstance().displayAvatar(mLoginUserId, mAvatarImg, true);
        } else {
            AvatarHelper.getInstance().displayAvatar(mUserId, mAvatarImg, true);
        }
        mAvatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 进入个人资料页
                Intent intent = new Intent(PersonalQzoneActivity.this,BasicInfoActivity.class);
                if (isMyBusiness() || isMySpace()) {
                    intent.putExtra(AppConstant.EXTRA_USER_ID, mLoginUserId);
                } else {
                    intent.putExtra(AppConstant.EXTRA_USER_ID, mUserId);
                }
                startActivity(intent);
            }
        });

//        if (isMyBusiness() || isMySpace()) {
//            mCoverImg.setUserId(mLoginUserId);
//        } else {
//            mCoverImg.setUserId(mUserId);
//        }

        imgHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mPhotos == null || mPhotos.size() <= 0) {
//                    return;
//                }
//                ArrayList<String> images = new ArrayList<String>();
//                for (int i = 0; i < mPhotos.size(); i++) {
//                    images.add(mPhotos.get(i).getOriginalUrl());
//                }
//                Intent intent = new Intent(BusinessCircleActivity.this, MultiImagePreviewActivity.class);
//                intent.putExtra(AppConstant.EXTRA_IMAGES, images);
//                startActivity(intent);
                showSelectAvatarDialog();
            }
        });
        //   loadPhotos();

        String url = PreferenceUtils.getString(this, HEAD_PHONE, "o");
        File file = new File(url);
        if (isMyBusiness() || isMySpace()) {
            if (url != null && !url.equals("o")) {
                if (file.exists()) {
                    imgHead.setImageBitmap(BitmapFactory.decodeFile(url));
//                    ImageLoader.getInstance().displayImage(url, imgHead);
                } else {
                    imgHead.setImageResource(R.drawable.qzone_phone);
                }
            } else {
                imgHead.setImageResource(R.drawable.qzone_phone);
            }
        } else {
            imgHead.setImageResource(R.drawable.qzone_phone);
        }
    }

    private void showSelectAvatarDialog() {
        String[] items = new String[]{getString(R.string.c_take_picture), getString(R.string.c_photo_album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.select_avatar).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            takePhoto();
                        } else {
                            selectPhoto();
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 4;
    private Uri mNewPhotoUri;

    public void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCameraIntent, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }

    public void saveBitmap(Bitmap bitmap) {
        if (vFile == null)
            vFile = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/com.xzjmyk.pm.activity/files/Pictures/", "backhead.jpg");
        if (!vFile.exists()) {
            File vDirPath = vFile.getParentFile();
            vDirPath.mkdirs();
        } else {
            if (vFile.exists()) {
                vFile.delete();
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(vFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            PreferenceUtils.putString(this, HEAD_PHONE, vFile.toString());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_CROP_PHOTO);
    }


    private void loadPhotos() {
        if (isMyBusiness() || isMySpace()) {// 自己的，那么就直接从数据库加载我的相册
            mPhotos = MyPhotoDao.getInstance().getPhotos(mLoginUserId);
            setCoverPhotos(mPhotos);
            return;
        }
        // 别人的，那么就从网上请求
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("userId", mUserId);

        StringJsonArrayRequest<MyPhoto> request = new StringJsonArrayRequest<MyPhoto>(mConfig.USER_PHOTO_LIST, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
            }
        }, new StringJsonArrayRequest.Listener<MyPhoto>() {
            @Override
            public void onResponse(ArrayResult<MyPhoto> result) {
                boolean success = Result.defaultParser(PersonalQzoneActivity.this, result, false);
                if (success) {
                    mPhotos = result.getData();
                    setCoverPhotos(mPhotos);
                }
            }
        }, MyPhoto.class, params);
        addDefaultRequest(request);
    }

    private void setCoverPhotos(List<MyPhoto> photos) {


        if (photos == null || photos.size() <= 0) {
            return;
        }
        String[] coverPhotos = new String[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            coverPhotos[i] = photos.get(i).getOriginalUrl();
        }
//        mCoverImg.setImages(coverPhotos);
    }

    private List<MyPhoto> mPhotos = null;

    private void readFromLocal() {
        FileDataHelper.readArrayData(this, mLoginUserId, FileDataHelper.FILE_BUSINESS_CIRCLE, new StringJsonArrayRequest.Listener<PublicMessage>() {
            @Override
            public void onResponse(ArrayResult<PublicMessage> result) {
                if (result != null && result.getData() != null && result.getData().size() > 0) {
                    mMessages.clear();
                    mMessages.addAll(result.getData());
                    mAdapter.notifyDataSetInvalidated();
                }
                requestData(true);
            }
        }, PublicMessage.class);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onStop() {
//        if (mCoverImg != null) {
//            mCoverImg.onStop();
//        }
        if (listener != null) {
            listener.ideChange();
        }
        listener = null;
        super.onStop();
    }

    /**
     * 接口,调用外部类的方法,让应用不可见时停止播放声音
     */
    ListenerAudio listener;

    public void setListenerAudio(ListenerAudio listener) {
        this.listener = listener;
    }

    public interface ListenerAudio {
        void ideChange();
    }

    private File mCurrentFile;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEND_MSG) {
            if (resultCode == Activity.RESULT_OK) {// 发说说成功
                String messageId = data.getStringExtra(AppConstant.EXTRA_MSG_ID);
                CircleMessageDao.getInstance().addMessage(mLoginUserId, messageId);
                requestData(true);
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {// 拍照返回再去裁减
            if (resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imgHead.setImageBitmap(bitmap);
                saveBitmap(bitmap);

            }
        } else if (requestCode == REQUEST_CODE_PICK_CROP_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    Uri o = Uri.fromFile(new File(path));
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this,MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    ImageLoader.getInstance().displayImage(mNewPhotoUri.toString(), imgHead);
                    PreferenceUtils.putString(this, HEAD_PHONE, mCurrentFile.toString());
                } else {
                    ToastUtil.showToast(this, mNewPhotoUri.toString());
                }
            }

        }
    }

    private static String HEAD_PHONE = "head_phone";
    /********** 公共消息的数据请求部分 *********/
    /**
     * 请求公共消息
     *
     * @param isPullDwonToRefersh 是下拉刷新，还是上拉加载
     */
    private void requestData(boolean isPullDwonToRefersh) {
        if (isMyBusiness()) {
            requestMyBusiness(isPullDwonToRefersh);
        } else {
            requestSpace(isPullDwonToRefersh);
        }
    }

    private void requestMyBusiness(final boolean isPullDwonToRefersh) {

        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        String messageId = null;
        if (!isPullDwonToRefersh && mMessages.size() > 0) {
            messageId = mMessages.get(mMessages.size() - 1).getMessageId();
            params.put("messageId", messageId);
        }
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("pageSize", "" + 10);
        params.put("type", "0");
//        params.put("flag", 1+"");
        StringJsonArrayRequest<PublicMessage> request = new StringJsonArrayRequest<PublicMessage>(
                mConfig.MSG_LIST, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(PersonalQzoneActivity.this);
            }
        }, new StringJsonArrayRequest.Listener<PublicMessage>() {
            @Override
            public void onResponse(ArrayResult<PublicMessage> result) {
                Log.i("Arison", "商务信息：" + JSON.toJSONString(result));
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    List<PublicMessage> datas = result.getData();
                    for (int i = 0; i < datas.size(); i++) {
                        Collections.sort(datas.get(i).getComments(), comp);
                    }
                    if (isPullDwonToRefersh) {
                        mMessages.clear();
                    }
                    if (datas != null && datas.size() > 0) {// 没有更多数据
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            FileDataHelper.writeFileData(PersonalQzoneActivity.this, mLoginUserId, FileDataHelper.FILE_BUSINESS_CIRCLE, result);
                            PreferenceUtils.putLong(mContext, "TIMEMILL", datas.get(0).getTime());
                        }
                        mMessages.addAll(datas);
                    }
                    mAdapter.notifyDataSetChanged(true);
                } else {

                }
                mPullToRefreshListView.onRefreshComplete();
            }
        }, PublicMessage.class, params);
        addDefaultRequest(request);
    }

    private void requestSpace(final boolean isPullDwonToRefersh) {
        String messageId = null;
        if (!isPullDwonToRefersh && mMessages.size() > 0) {
            messageId = mMessages.get(mMessages.size() - 1).getMessageId();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("userId", mUserId);
        params.put("flag", PublicMessage.FLAG_NORMAL + "");

        if (!TextUtils.isEmpty(messageId)) {
            params.put("messageId", messageId);
        }
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));

        StringJsonArrayRequest<PublicMessage> request = new StringJsonArrayRequest<PublicMessage>(mConfig.MSG_USER_LIST, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(PersonalQzoneActivity.this);
                mPullToRefreshListView.onRefreshComplete();
            }
        }, new StringJsonArrayRequest.Listener<PublicMessage>() {
            @Override
            public void onResponse(ArrayResult<PublicMessage> result) {
                boolean success = Result.defaultParser(PersonalQzoneActivity.this, result, true);
                if (success) {
                    List<PublicMessage> datas = result.getData();
                    if (isPullDwonToRefersh) {
                        mMessages.clear();
                    }
                    if (datas != null && datas.size() > 0) {// 没有更多数据
                        mMessages.addAll(datas);
                    }
                    mAdapter.notifyDataSetChanged(true);
                }
                mPullToRefreshListView.onRefreshComplete();
            }
        }, PublicMessage.class, params);
        addDefaultRequest(request);
    }


    private void addComment(CommentReplyCache cache) {
        Comment comment = new Comment();
        comment.setUserId(mLoginUserId);
        comment.setNickName(mLoginNickName);
        comment.setToUserId(cache.toUserId);
        comment.setToNickname(cache.toNickname);
        comment.setBody(cache.text);
        addComment(cache.messagePosition, comment);
    }

    /**
     * 添加一条回复
     *
     * @param position 回复的索引（用于本地更新适配器和获取messageID）
     * @param comment  回复的内容对象
     */
    private void addComment(final int position, final Comment comment) {
        final PublicMessage message = mMessages.get(position);
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("messageId", message.getMessageId());
        if (!TextUtils.isEmpty(comment.getToUserId())) {
            params.put("toUserId", comment.getToUserId());
        }
        if (!TextUtils.isEmpty(comment.getToNickname())) {
            params.put("toNickname", comment.getToNickname());
        }
        params.put("body", comment.getBody());

        StringJsonObjectRequest<String> request = new StringJsonObjectRequest<String>(mConfig.MSG_COMMENT_ADD, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(PersonalQzoneActivity.this);
            }
        }, new StringJsonObjectRequest.Listener<String>() {
            @Override
            public void onResponse(ObjectResult<String> result) {
                boolean success = Result.defaultParser(PersonalQzoneActivity.this, result, true);
                if (success && result.getData() != null) {
                    List<Comment> comments = message.getComments();
                    if (comments == null) {
                        comments = new ArrayList<Comment>();
                        message.setComments(comments);
                    }
                    comment.setCommentId(result.getData());
                    comments.add(comment);//  防止评论时候显示在第一个位置
                    mAdapter.notifyDataSetChanged(false);
                }
            }
        }, String.class, params);
        addDefaultRequest(request);
    }

    public void showCommentEnterView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        mCommentReplyCache = new CommentReplyCache();
        mCommentReplyCache.messagePosition = messagePosition;
        mCommentReplyCache.toUserId = toUserId;
        mCommentReplyCache.toNickname = toNickname;
        if (TextUtils.isEmpty(toUserId) || TextUtils.isEmpty(toNickname) || TextUtils.isEmpty(toShowName)) {
            mPMsgBottomView.setHintText("");
        } else {
            mPMsgBottomView.setHintText(getString(R.string.replay_text, toShowName));
        }
        mPMsgBottomView.show();
    }


    class CommentReplyCache {
        int messagePosition;// 消息的Position
        String toUserId;
        String toNickname;
        String text;
    }

    CommentReplyCache mCommentReplyCache = null;

    @Override
    public void onBackPressed() {
        if (mPMsgBottomView != null && mPMsgBottomView.getVisibility() == View.VISIBLE) {
            mPMsgBottomView.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        showCommentEnterView(messagePosition, toUserId, toNickname, toShowName);
    }
}
