package com.xzjmyk.pm.activity.util.im;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.ListUtils;
import com.common.data.NumberUtils;
import com.common.file.PropertiesUtil;
import com.common.thread.ThreadPool;
import com.common.ui.ImageUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.utils.helper.AvatarHelper;
import com.xzjmyk.pm.activity.ui.erp.net.HttpUtil;
import com.xzjmyk.pm.activity.ui.groupchat.SelectContactsActivity;
import com.core.utils.ToastUtil;
import com.xzjmyk.pm.activity.util.imageloader.BitmapUtil;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 合成群头像操作
 * Created by gongp on 2016/8/9.
 */
public class RoomImageUtil {
    private static final String TAG = "UPDATA";
    private static RoomImageUtil instance = null;
    private String photoId = null;
    private File image = null;//合成文件
    private Context context = null;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x11) {
                if (image == null || context == null) return;
                String roomjId = msg.getData().getString("id");
                uploadAvatar(context, image, roomjId);
            }

        }
    };
    private UpImageListener listener;

    private RoomImageUtil() {
    }

    public static RoomImageUtil getInstance() {
        if (instance == null) {
            synchronized (RoomImageUtil.class) {
                instance = new RoomImageUtil();
            }
        }
        return instance;
    }

    /**
     * 创建群头像入口
     *
     * @param context
     * @param inviteUsers 群id列表
     * @param mRoomJid    群id
     */
    public void uploadAvatar(final Context context, final List<String> inviteUsers, final String mRoomJid, UpImageListener listener) {
        this.context = context;
        this.listener = listener;
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                //八位不重复随机数
                photoId = NumberUtils.generateNumber2();//八位不重复随机数
                image = createChatImage(context, inviteUsers);
                Message msg = mhandler.obtainMessage();
                msg.getData().putString("id", mRoomJid);
                msg.what = 0x11;
                mhandler.sendMessage(msg);
            }
        });
    }

    /**
     * @param context
     * @param count
     * @return
     */
    private List<SelectContactsActivity.MyBitmapEntity> getBitmapEntitys(Context context, int count) {
        List<SelectContactsActivity.MyBitmapEntity> mList = new LinkedList<SelectContactsActivity.MyBitmapEntity>();
        String value = PropertiesUtil.readData(context, String.valueOf(count),
                R.raw.data);
        String[] arr1 = value.split(";");
        int length = arr1.length;
        for (int i = 0; i < length; i++) {
            String content = arr1[i];
            String[] arr2 = content.split(",");
            SelectContactsActivity.MyBitmapEntity entity = null;
            for (int j = 0; j < arr2.length; j++) {
                entity = new SelectContactsActivity.MyBitmapEntity();
                entity.x = Float.valueOf(arr2[0]);
                entity.y = Float.valueOf(arr2[1]);
                entity.width = Float.valueOf(arr2[2]);
                entity.height = Float.valueOf(arr2[3]);
            }
            mList.add(entity);
        }
        return mList;
    }

    /**
     * 上传文件头像到服务器
     *
     * @param context
     * @param file    文件地址
     * @param roomjId 房间id
     */
    private void uploadAvatar(final Context context, File file, final String roomjId) {
        if (!file.exists()) {// 文件不存在
            return;
        }
        RequestParams params = new RequestParams();
        params.put("userId", photoId);//群主id+当前系统时间
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(MyApplication.getInstance().getConfig().AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }
                if (listener != null)
                    listener.callBack(true, photoId);
                if (success) {
                    ToastUtil.showToast(context, R.string.upload_avatar_success);
                    //更新服务器
                    updateIMChatImageId(roomjId, photoId);
                } else {
                    ToastUtil.showToast(context, R.string.upload_avatar_failed);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                ToastUtil.showToast(context, R.string.upload_avatar_failed);
                if (listener != null)
                    listener.callBack(true, null);
            }
        });
    }

    /**
     * @功能:上传文件后，需要上传更新图像id
     * @author:Arisono
     * @param:
     * @return:
     */
    private void updateIMChatImageId(String roomjId, String photoId) {
        String url = MyApplication.getInstance().getConfig().apiUrl + "room/setRelationGroupPhoto";
        final String requestTag = "loginManagerSystem";
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", roomjId);
        params.put("photoid", photoId);
        StringJsonObjectRequest<String> mRequest = new StringJsonObjectRequest<String>(
                Request.Method.GET, url,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                },
                new StringJsonObjectRequest.Listener<String>() {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        Log.i("gongpengming", "上传头像成功" + result.getData());
                    }
                }, String.class, params, true);
        mRequest.setTag(requestTag);
        MyApplication.getInstance().getFastVolley().addDefaultRequest(TAG, mRequest);
    }

    /**
     * 合成头像群成员
     *
     * @param context
     * @param inviteUsers
     * @return
     */
    private File createChatImage(Context context, List<String> inviteUsers) {
        File file = null;
        if (!ListUtils.isEmpty(inviteUsers)) {
            int size = inviteUsers.size() > 9 ? 9 : inviteUsers.size();
            List<SelectContactsActivity.MyBitmapEntity> mEntityList = getBitmapEntitys(context, size);
            Bitmap mBitmaps[] = new Bitmap[size];
            for (int i = 0; i < size; i++) {
                String url = AvatarHelper.getAvatarUrl(inviteUsers.get(i), false);
                Bitmap nextBitmap = null;
                String filepath = HttpUtil.download(url, com.common.file.FileUtils.getSDRoot() + "/uu/chat/head" + i + ".png");
                nextBitmap = ImageUtil.compressBitmapWithFilePath(filepath, 300, 300);
                if (nextBitmap == null) {
                    nextBitmap = ImageUtil.compressBitmapWithResources(context, R.drawable.avatar_normal, 300, 300);
                }
                Bitmap tempBitmap = ThumbnailUtils.extractThumbnail(nextBitmap, (int) mEntityList
                        .get(i).width, (int) mEntityList.get(i).width);
                mBitmaps[i] = tempBitmap;
            }
            Bitmap combineBitmap = BitmapUtil.getCombineBitmaps(mEntityList, mBitmaps);
            try {
                file = BitmapUtil.saveFile(combineBitmap, "chatImage.png");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public interface UpImageListener {
        void callBack(boolean isOk, String id);
    }
}
