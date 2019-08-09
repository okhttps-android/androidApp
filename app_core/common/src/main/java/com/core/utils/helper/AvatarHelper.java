package com.core.utils.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.common.LogUtil;
import com.core.app.MyApplication;
import com.core.app.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户头像的上传和获取
 */
public class AvatarHelper {

    private Map<String, Long> mCheckTimeMaps;
    private Handler mHandler;

    private AvatarHelper() {
        mCheckTimeMaps = new HashMap<String, Long>();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static AvatarHelper INSTANCE;

    public static AvatarHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (AvatarHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AvatarHelper();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 当自己上传了新的头像了，立即删除缓存
     *
     * @param userId
     */
    public void deleteAvatar(String userId) {
        final String url1 = getAvatarUrl(userId, true);
        final String url2 = getAvatarUrl(userId, false);
        if (!TextUtils.isEmpty(url1)) {
            deleteCache(url1);
        }
        if (!TextUtils.isEmpty(url2)) {
            deleteCache(url2);
        }
    }

    /**
     * 删除缓存在本地和内存中的图片
     *
     * @param url
     */
    private void deleteCache(String url) {
        try {
            final File localFile = ImageLoader.getInstance().getDiscCache().get(url);
            if (localFile != null && localFile.exists()) {
                localFile.delete();
            }
            List<String> keys = MemoryCacheUtil.findCacheKeysForImageUri(url, ImageLoader.getInstance().getMemoryCache());
            if (keys != null && keys.size() > 0) {
                for (String key : keys) {
                    ImageLoader.getInstance().getMemoryCache().remove(key);
                }
            }
        } catch (NullPointerException e) {

        } finally {
        }
//        if (ImageLoader.getInstance().getDiscCache() == null)
//            return;

    }

    public void displayRoomAvatar(String userId, final ImageView imageView, final boolean isThumb) {
        final String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Long lastCheckTime = mCheckTimeMaps.get(url);
        if (lastCheckTime == null || System.currentTimeMillis() - lastCheckTime > 0.1 * 60 * 1000) {// 至少间隔5分钟检测一下
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long lastModifyTime = getLastModify(url);
                    long localLastModified = 0;
                    final File localFile = ImageLoader.getInstance().getDiscCache().get(url);
                    if (localFile != null && localFile.exists()) {
                        localLastModified = localFile.lastModified();
                    }
                    final boolean delete = localLastModified < lastModifyTime;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCheckTimeMaps.put(url, System.currentTimeMillis());
                            if (delete) {
                                if (localFile != null) {
                                    localFile.delete();
                                }
                                List<String> keys = MemoryCacheUtil.findCacheKeysForImageUri(url, ImageLoader.getInstance().getMemoryCache());
                                if (keys != null && keys.size() > 0) {
                                    for (String key : keys) {
                                        ImageLoader.getInstance().getMemoryCache().remove(key);
                                    }
                                }
                            }
                            display(url, imageView, isThumb);
                        }
                    });

                }
            }).start();
        } else {
            display(url, imageView, isThumb);
        }
    }

    public void displayAvatar(String userId, final ImageView imageView, final boolean isThumb) {
        final String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Long lastCheckTime = mCheckTimeMaps.get(url);
        if (lastCheckTime == null || System.currentTimeMillis() - lastCheckTime > 5 * 60 * 1000) {// 至少间隔5分钟检测一下
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long lastModifyTime = getLastModify(url);
                    long localLastModified = 0;
                    final File localFile = ImageLoader.getInstance().getDiscCache().get(url);
                    if (localFile != null && localFile.exists()) {
                        localLastModified = localFile.lastModified();
                    }
                    final boolean delete = localLastModified < lastModifyTime;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCheckTimeMaps.put(url, System.currentTimeMillis());
                            if (delete) {
                                if (localFile != null) {
                                    localFile.delete();
                                }
                                List<String> keys = MemoryCacheUtil.findCacheKeysForImageUri(url, ImageLoader.getInstance().getMemoryCache());
                                if (keys != null && keys.size() > 0) {
                                    for (String key : keys) {
                                        ImageLoader.getInstance().getMemoryCache().remove(key);
                                    }
                                }
                            }
                            display(url, imageView, isThumb);
                        }
                    });

                }
            }).start();
        } else {
            display(url, imageView, isThumb);
        }
    }


    public void displayAvatarPng(String userId, final ImageView imageView, final boolean isThumb) {
        final String url = getAvatarUrlPng(userId, isThumb);
        Log.i("Arison", "AvatarHelper:displayAvatar:140:" + url);
        if (TextUtils.isEmpty(url)) {
            return;
        }
//        imageView.setTag(url);
        Long lastCheckTime = mCheckTimeMaps.get(url);
        if (lastCheckTime == null || System.currentTimeMillis() - lastCheckTime > 5 * 60 * 1000) {// 至少间隔5分钟检测一下
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long lastModifyTime = getLastModify(url);
                    long localLastModified = 0;
                    final File localFile = ImageLoader.getInstance().getDiscCache().get(url);
                    if (localFile != null && localFile.exists()) {
                        localLastModified = localFile.lastModified();
                    }
                    final boolean delete = localLastModified < lastModifyTime;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCheckTimeMaps.put(url, System.currentTimeMillis());
                            if (delete) {
                                if (localFile != null) {
                                    localFile.delete();
                                }
                                List<String> keys = MemoryCacheUtil.findCacheKeysForImageUri(url, ImageLoader.getInstance().getMemoryCache());
                                if (keys != null && keys.size() > 0) {
                                    for (String key : keys) {
                                        ImageLoader.getInstance().getMemoryCache().remove(key);
                                    }
                                }
                            }
                            display(url, imageView, isThumb);
                        }
                    });

                }
            }).start();
        } else {
            display(url, imageView, isThumb);
        }
    }

    private long getLastModify(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection == null) {
            return 0;
        } else {
            connection.setDoOutput(false);
            connection.setDoInput(true);
            try {
                connection.connect();
                return connection.getLastModified();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        }
        return 0;
    }

    public void display(final String url, ImageView imageView, boolean isThumb) {
        if (isThumb) {
            ImageLoader.getInstance().displayImage(url, imageView, MyApplication.mAvatarRoundImageOptions);
        } else {
            ImageLoader.getInstance().displayImage(url, imageView, MyApplication.mAvatarNormalImageOptions);
        }
    }

    public void display(String userId, final ImageView imageView, final boolean isThumb, boolean isDeleteCache) {
        final String url = getAvatarUrl(userId, isThumb);
        LogUtil.d(url);
        if (isDeleteCache) {
            deleteCache(url);
        }
        if (isThumb) {
            ImageLoader.getInstance().displayImage(url, imageView, MyApplication.mAvatarRoundImageOptions);
        } else {
            ImageLoader.getInstance().displayImage(url, imageView, MyApplication.mAvatarRoundImageOptions);
        }
    }


    public DisplayImageOptions mCircularImageOptions;

    public DisplayImageOptions getCircularImageOptions() {
        if (mCircularImageOptions == null) {
            mCircularImageOptions = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .displayer(new RoundedBitmapDisplayer(360))
                    .resetViewBeforeLoading(true)
                    .showImageForEmptyUri(R.drawable.ic_login_default_header)
                    .showImageOnFail(R.drawable.ic_login_default_header)
                    .showImageOnLoading(R.drawable.ic_login_default_header).build();
        }
        return mCircularImageOptions;

    }
    public void displayCircular(final String url, ImageView imageView, boolean isThumb) {
        if (isThumb) {
            ImageLoader.getInstance().displayImage(url, imageView,  getCircularImageOptions());
        } else {
            ImageLoader.getInstance().displayImage(url, imageView,  getCircularImageOptions());
        }
    }
    public void displayCircular(String userId, final ImageView imageView, final boolean isThumb, boolean isDeleteCache) {
        final String url = getAvatarUrl(userId, isThumb);
        LogUtil.d(url);
        if (isDeleteCache) {
            deleteCache(url);
        }
        if (isThumb) {
            ImageLoader.getInstance().displayImage(url, imageView, getCircularImageOptions());
        } else {
            ImageLoader.getInstance().displayImage(url, imageView, getCircularImageOptions());
        }
    }

    //根据loginUserId，获取该用户头像的Bitmap对象
    //Caused by: Android.os.NetworkOnMainThreadException，查了下原因上在4.0之后在主线程里面执行Http请求都会报这个错，大概是怕Http请求时间太长造成程序假死的情况吧。

    /**
     * 图片的url 转 Bitmap对象
     *
     * @param userId
     * @param isThumb
     * @return
     */
    public static Bitmap returnBitmap(String userId, boolean isThumb) {
        final String urlpath = getAvatarUrl(userId, isThumb);
        Bitmap mBitmap = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            mBitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mBitmap;
    }

    /**
     * 图片的 Bitmap对象转 url
     *
     * @param bitmap
     * @return
     */
    public static String doBitmapTurnToStringurl(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);
    }

    private void display(String url, ImageAware imageAware, boolean isThumb) {
        if (isThumb) {
            ImageLoader.getInstance().displayImage(url, imageAware, MyApplication.mAvatarRoundImageOptions);
        } else {
            ImageLoader.getInstance().displayImage(url, imageAware, MyApplication.mAvatarNormalImageOptions);
        }
    }

    public void displayAvatar(String userId, final ImageAware imageAware, final boolean isThumb) {
        final String url = getAvatarUrl(userId, isThumb);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Long lastCheckTime = mCheckTimeMaps.get(url);
        if (lastCheckTime == null || System.currentTimeMillis() - lastCheckTime > 1 * 60 * 1000) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("wang", "lastChecttime<<<<");
                    long lastModifyTime = getLastModify(url);
                    long localLastModified = 0;
                    final File localFile = ImageLoader.getInstance().getDiscCache().get(url);
                    if (localFile != null && localFile.exists()) {
                        localLastModified = localFile.lastModified();
                    }
                    final boolean delete = localLastModified < lastModifyTime;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCheckTimeMaps.put(url, System.currentTimeMillis());
                            if (delete) {
                                if (localFile != null) {
                                    localFile.delete();
                                }
                                List<String> keys = MemoryCacheUtil.findCacheKeysForImageUri(url, ImageLoader.getInstance().getMemoryCache());
                                if (keys != null && keys.size() > 0) {
                                    for (String key : keys) {
                                        ImageLoader.getInstance().getMemoryCache().remove(key);
                                    }
                                }
                            }
                            display(url, imageAware, isThumb);
                        }
                    });
                }
            }).start();
        } else {
            display(url, imageAware, isThumb);
        }
    }

    public static String getRoomAvatarUrl(String userId, boolean isThumb) {
        String url = null;
        if (isThumb) {
            url = MyApplication.getInstance().getConfig().AVATAR_THUMB_PREFIX + "/" + "/" + userId + ".jpg";
        } else {
            url = MyApplication.getInstance().getConfig().AVATAR_ORIGINAL_PREFIX + "/" + "/" + userId + ".jpg";
        }
        return url;
    }

    public static String getAvatarUrl(String userId, boolean isThumb) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        int userIdInt = -1;
        try {
            userIdInt = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (userIdInt == -1 || userIdInt == 0) {
            return null;
        }

        int dirName = userIdInt % 10000;
        String url = null;
        if (isThumb) {
            url = MyApplication.getInstance().getConfig().AVATAR_THUMB_PREFIX + "/" + dirName + "/" + userId + ".jpg";
        } else {
            url = MyApplication.getInstance().getConfig().AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + userId + ".jpg";
        }
        Log.i("fanglhuserId=", userId + "");
        Log.i("fanglhdirName=", dirName + "");
        Log.i("fanglhurl=", url);
        return url;
    }


    public static String getAvatarUrlPng(String userId, boolean isThumb) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        int userIdInt = -1;
        try {
            userIdInt = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (userIdInt == -1 || userIdInt == 0) {
            return null;
        }

        int dirName = userIdInt % 10000;
        String url = null;
        if (isThumb) {
            url = MyApplication.getInstance().getConfig().AVATAR_THUMB_PREFIX + "/" + dirName + "/" + userId + ".png";
        } else {
            url = MyApplication.getInstance().getConfig().AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + userId + ".png";
        }

        return url;
    }


    // // 无用
    // public void displayResumeAvatar(String userId, ImageView imageView, boolean isThumb) {
    // String url = getResumeAvatar(userId, isThumb);
    // if (TextUtils.isEmpty(url)) {
    // return;
    // }
    // // Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(url);
    // // if (bitmap != null && !bitmap.isRecycled()) {
    // // ImageLoader.getInstance().displayImage(url, imageView,
    // // MyApplication.mRoundImageOptions);
    // // return;
    // // }
    // //
    // // File file = ImageLoader.getInstance().getDiscCache().get(url);
    // // if (file != null && file.exists()) {
    // // long localLastModified=file.lastModified();
    // // if(System.currentTimeMillis()-localLastModified>24L*60*60*1000){//图片过期
    // //
    // // }
    // // }
    //
    // if (isThumb) {
    // ImageLoader.getInstance().displayImage(url, imageView, MyApplication.mAvatarRoundImageOptions);
    // } else {
    // ImageLoader.getInstance().displayImage(url, imageView, MyApplication.mAvatarNormalImageOptions);
    // }
    // }
    //
    // // 无用
    // public void displayResumeAvatar(String userId, ImageAware imageAware, boolean isThumb) {
    // String url = getResumeAvatar(userId, isThumb);
    // if (TextUtils.isEmpty(url)) {
    // return;
    // }
    // // Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(url);
    // // if (bitmap != null && !bitmap.isRecycled()) {
    // // ImageLoader.getInstance().displayImage(url, imageView,
    // // MyApplication.mRoundImageOptions);
    // // return;
    // // }
    // //
    // // File file = ImageLoader.getInstance().getDiscCache().get(url);
    // // if (file != null && file.exists()) {
    // // long localLastModified=file.lastModified();
    // // if(System.currentTimeMillis()-localLastModified>24L*60*60*1000){//图片过期
    // //
    // // }
    // // }
    //
    // if (isThumb) {
    // ImageLoader.getInstance().displayImage(url, imageAware, MyApplication.mAvatarRoundImageOptions);
    // } else {
    // ImageLoader.getInstance().displayImage(url, imageAware, MyApplication.mAvatarNormalImageOptions);
    // }
    // }
    //
    // // 无用
    // public static String getResumeAvatar(String userId, boolean isThumb) {
    // if (TextUtils.isEmpty(userId)) {
    // return null;
    // }
    // int userIdInt = -1;
    // try {
    // userIdInt = Integer.parseInt(userId);
    // } catch (NumberFormatException e) {
    // e.printStackTrace();
    // }
    // if (userIdInt == -1 || userIdInt == 0) {
    // return null;
    // }
    //
    // int dirName = userIdInt % 10000;
    // String url = null;
    // if (isThumb) {
    // url = MyApplication.getInstance().getConfig().RESUME_AVATAR_THUMB_PREFIX + "/" + dirName + "/" + userId + ".jpg";
    // } else {
    // url = MyApplication.getInstance().getConfig().RESUME_AVATAR_ORIGINAL_PREFIX + "/" + dirName + "/" + userId + ".jpg";
    // }
    // return url;
    // }
}
