package com.xzjmyk.pm.activity.util.im.helper;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.common.ui.ImageUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.core.app.MyApplication;
import com.core.model.UploadFileResult;
import com.core.xmpp.model.ChatMessage;
import com.core.model.XmppMessage;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.net.volley.Result;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 专门用来上传的
 */
public class UploadEngine {
    public interface ImFileUploadResponse {
        void onSuccess(String toUserId, ChatMessage message);

        void onFailure(String toUserId, ChatMessage message);
    }

    public static final void uploadImFile(final String toUserId, final ChatMessage message, final ImFileUploadResponse response) {
        RequestParams params = new RequestParams();
        final String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        params.put("userId", loginUserId);
        params.put("access_token", MyApplication.getInstance().mAccessToken);
//		params.put("uploadFlag","3");

        try {
            if (XmppMessage.TYPE_IMAGE == message.getType())
                params.put("file1", ImageUtil.compressBitmapToFile(message.getFilePath(), 100, 300, 300));
            else
                params.put("file1", new File(message.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(MyApplication.getInstance().getConfig().UPLOAD_URL, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

                        String url = null;
                        if (arg0 == 200) {
                            UploadFileResult result = null;
                            try {
                                result = JSON.parseObject(new String(arg2), UploadFileResult.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (result == null || result.getResultCode() != Result.CODE_SUCCESS || result.getData() == null
                                    || result.getSuccess() != result.getTotal()) {

                            } else {
                                UploadFileResult.Data data = result.getData();
                                if (message.getType() == XmppMessage.TYPE_IMAGE) {
                                    if (data.getImages() != null && data.getImages().size() > 0) {
                                        url = data.getImages().get(0).getOriginalUrl();
                                    }
                                } else if (message.getType() == XmppMessage.TYPE_VOICE) {
                                    if (data.getAudios() != null && data.getAudios().size() > 0) {
                                        url = data.getAudios().get(0).getOriginalUrl();
                                    }
                                } else if (message.getType() == XmppMessage.TYPE_VIDEO) {
                                    if (data.getVideos() != null && data.getVideos().size() > 0) {
                                        url = data.getVideos().get(0).getOriginalUrl();
                                    }
                                } else if (message.getType() == XmppMessage.TYPE_FILE) {
                                    if (data.getFiles() != null && data.getFiles().size() > 0) {
                                        url = data.getFiles().get(0).getOriginalUrl();
                                    } else if (data.getVideos() != null && data.getVideos().size() > 0) {
                                        url = data.getVideos().get(0).getOriginalUrl();
                                    } else if (data.getAudios() != null && data.getAudios().size() > 0) {
                                        url = data.getAudios().get(0).getOriginalUrl();
                                    } else if (data.getImages() != null && data.getImages().size() > 0) {
                                        url = data.getImages().get(0).getOriginalUrl();
                                    } else if (data.getOthers() != null && data.getOthers().size() > 0) {
                                        url = data.getOthers().get(0).getOriginalUrl();
                                    } else {
                                    }
                                }
                            }
                        }

                        Log.d("roamer", "file url:" + url);

                        if (TextUtils.isEmpty(url)) {
                            if (response != null) {
                                response.onFailure(toUserId, message);
                                ChatMessageDao.getInstance().updateMessageUploadState(loginUserId, toUserId, message.get_id(), false, url);
                                Log.d("roamer", "url为空,让其响应为失败");
                            }
                        } else {
                            ChatMessageDao.getInstance().updateMessageUploadState(loginUserId, toUserId, message.get_id(), true, url);
                            if (response != null) {
                                Log.d("roamer", "上传文件成功了");
                                message.setContent(url);
                                message.setUpload(true);
                                response.onSuccess(toUserId, message);
                            }
                        }

                    }

                    @Override
                    public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                        // 失败就不用更新数据库了，默认值就是false
                        // ChatMessageDao.getInstance().updateMessageSendState(loginUserId,
                        // toUserId, msg_id, false);
                        Log.d("roamer", "上传失败了...");
                        Log.d("roamer", arg3.toString());
                        if (response != null) {
                            response.onFailure(toUserId, message);
                        }
                    }
                }

        );
    }

}
