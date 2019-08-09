package com.core.xmpp.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.thread.FileDownloadThread;
import com.common.thread.ThreadUtil;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.model.Friend;
import com.core.model.User;
import com.core.model.XmppMessage;
import com.core.net.volley.FastVolley;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.downloader.DownloadListener;
import com.core.xmpp.downloader.Downloader;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;
import com.core.xmpp.utils.HtmlUtils;
import com.core.xmpp.utils.SmileyParser;
import com.core.xmpp.utils.flie.FileOpenWays;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.uas.applocation.UasLocationHelper;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;


@SuppressWarnings("unused")
public class ChatContentView extends PullDownListView implements VoicePlayer.OnMediaStateChange {
    private static final String TAG = ChatContentView.class.getSimpleName();
    private static final String HASHCODE = "HASHCODE";
    private Context mContext;
    private ArrayList<Integer> ints;
    private ArrayList<Integer> deleInts;
    /* 根据mLoginUserId和mToUserId 唯一确定一张表 */
    private String mLoginUserId;
    private String mToUserId;
    private boolean isShowCB = false;
    private List<ChatMessage> mChatMessages;
    private MessageEventListener mMessageEventListener;
    private int mDelayTime = 0;
    private LayoutInflater mInflater;
    private Handler mHandler = new Handler();
    private ChatContentAdapter mChatContentAdapter;

    private int mMaxWidth = 100;
    private int mMaxHeight = 200;

    private long mPlayVoiceId = -1;// 当前正在播放的声音消息的id
    private VoiceViewHolder mPlayVoiceViewHolder;// 当前正在播放的VocieViewHolder
    private VoicePlayer mVoicePlayer;

    // 匹配图标时用到的文件类型
    String[] fileTypes = new String[]{"apk", "avi", "bat", "bin", "bmp", "chm", "css", "dat", "dll", "doc", "docx",
            "dos", "dvd", "gif", "html", "ifo", "inf", "iso", "java", "jpeg", "jpg", "log", "m4a", "mid", "mov",
            "movie", "mp2", "mp2v", "mp3", "mp4", "mpe", "mpeg", "mpg", "pdf", "php", "png", "ppt", "pptx", "psd",
            "rar", "tif", "ttf", "txt", "wav", "wma", "wmv", "xls", "xlsx", "xml", "xsl", "zip"};

    public ChatContentView(Context context) {
        super(context);
        init(context);
    }

    public boolean isShowCB() {
        return isShowCB;
    }

    public void setShowCB(boolean showCB) {
        isShowCB = showCB;
    }

    public ArrayList<Integer> getInts() {
        isShowCB = false;
        if (deleInts != null && deleInts.size() > 0) {
            ints.removeAll(deleInts);
        }
        return ints;
    }

    public void setInts(ArrayList<Integer> ints) {
        this.ints = ints;
    }

    public ChatContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void reset() {
        if (mVoicePlayer != null) {
            mVoicePlayer.stop();
        }
    }

    private String mLoginNickName;
    private String mRoomNickName;

    public void setRoomNickName(String roomNickName) {
        mRoomNickName = roomNickName;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw > h) {
            mHandler.removeCallbacks(runnable);
            mHandler.postDelayed(runnable, mDelayTime);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            scrollToBottom();
        }
    };

    private void init(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDelayTime = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);
        mVoicePlayer = new VoicePlayer();
        mVoicePlayer.setOnMediaStateChangeListener(this);
        setCacheColorHint(0x00000000);
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        mLoginNickName = MyApplication.getInstance().mLoginUser.getNickName();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mMessageEventListener != null) {
                mMessageEventListener.onEmptyTouch();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setData(List<ChatMessage> chatMessages) {
        mChatMessages = chatMessages;
        mChatContentAdapter = new ChatContentAdapter();
        setAdapter(mChatContentAdapter);
        mChatContentAdapter.notifyDataSetInvalidated();

    }

    /**
     * 这个方法必须调用
     *
     * @param toUserId
     */
    public void setToUserId(String toUserId) {
        mToUserId = toUserId;
    }

    public void notifyDataSetInvalidated(boolean scrollToBottom) {
        if (mChatContentAdapter == null) {
            return;
        }
        mChatContentAdapter.notifyDataSetChanged();
        if (scrollToBottom)
            scrollToBottom();
    }

    public void notifyDataSetChanged() {
        if (mChatContentAdapter == null) {
            return;
        }
        mChatContentAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    /*
     * private String getLengthDesc(int seconds) { if (seconds < 60) { seconds =
     * 1000 * seconds; } int s = seconds / 1000; int m = (seconds % 1000) / 100;
     * return (s + "." + m + "''"); }
     */
    public void setImageMaxWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
    }

    public void setImageMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }

    public void setMessageEventListener(MessageEventListener listener) {
        mMessageEventListener = listener;
    }

    public void scrollToBottom() {
        if (mChatMessages == null) {
            return;
        }
        setSelection(mChatMessages.size());
    }

    public static interface MessageEventListener {
        public void onEmptyTouch();// 点击空白处，让输入框归位

        public void onMyAvatarClick();

        public void onFriendAvatarClick(String friendUserId);

        void onFriendAvatarLongClick(String friendUserId);

        public void onMessageClick(ChatMessage chatMessage);

        public void onMessageLongClick(ChatMessage chatMessage);

        public void onSendAgain(ChatMessage chatMessage);
    }

    public class ChatContentAdapter extends BaseAdapter {
        private static final int VIEW_SYSTEM = 0;

        private static final int VIEW_FROM_ME_TEXT = 1;
        private static final int VIEW_FROM_ME_IMAGE = 2;
        private static final int VIEW_FROM_ME_VOICE = 3;
        private static final int VIEW_FROM_ME_LOCATION = 4;
        private static final int VIEW_FROM_ME_GIF = 5;
        private static final int VIEW_FROM_ME_VIDEO = 6;
        private static final int VIEW_FROM_ME_FILE = 13;
        private static final int VIEW_FROM_ME_CARD = 15;

        private static final int VIEW_TO_ME_TEXT = 7;
        private static final int VIEW_TO_ME_IMAGE = 8;
        private static final int VIEW_TO_ME_VOICE = 9;
        private static final int VIEW_TO_ME_LOCATION = 10;
        private static final int VIEW_TO_ME_GIF = 11;
        private static final int VIEW_TO_ME_VIDEO = 12;
        private static final int VIEW_TO_ME_FILE = 14;
        private static final int VIEW_TO_ME_CARD = 16;

        public int getCount() {
            return mChatMessages.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 17;
        }

        @Override
        public int getItemViewType(int position) {
            int messageType = mChatMessages.get(position).getType();
            if (messageType == XmppMessage.TYPE_TIP) {
                return VIEW_SYSTEM;// 消息提示
            }
            if (mChatMessages.get(position).getFromUserId().compareToIgnoreCase(mLoginUserId) == 0) {// 我的消息
                switch (messageType) {
                    case XmppMessage.TYPE_TEXT:
                        return VIEW_FROM_ME_TEXT;
                    case XmppMessage.TYPE_IMAGE:
                        return VIEW_FROM_ME_IMAGE;
                    case XmppMessage.TYPE_VOICE:
                        return VIEW_FROM_ME_VOICE;
                    case XmppMessage.TYPE_LOCATION:
                        return VIEW_FROM_ME_LOCATION;
                    case XmppMessage.TYPE_GIF:
                        return VIEW_FROM_ME_GIF;
                    case XmppMessage.TYPE_VIDEO:
                        return VIEW_FROM_ME_VIDEO;
                    case XmppMessage.TYPE_FILE:
                        return VIEW_FROM_ME_FILE;
                    case XmppMessage.TYPE_CARD:
                        return VIEW_FROM_ME_CARD;
                }
            } else {
                switch (messageType) {
                    case XmppMessage.TYPE_TEXT:
                        return VIEW_TO_ME_TEXT;
                    case XmppMessage.TYPE_IMAGE:
                        return VIEW_TO_ME_IMAGE;
                    case XmppMessage.TYPE_VOICE:
                        return VIEW_TO_ME_VOICE;
                    case XmppMessage.TYPE_LOCATION:
                        return VIEW_TO_ME_LOCATION;
                    case XmppMessage.TYPE_GIF:
                        return VIEW_TO_ME_GIF;
                    case XmppMessage.TYPE_VIDEO:
                        return VIEW_TO_ME_VIDEO;
                    case XmppMessage.TYPE_FILE:
                        return VIEW_TO_ME_FILE;
                    case XmppMessage.TYPE_CARD:
                        return VIEW_TO_ME_CARD;
                }
            }
            return VIEW_SYSTEM;// 消息提示
        }

        @SuppressLint("NewApi")
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int viewType = getItemViewType(position);
            SystemViewHolder systemViewHolder = null;
            ContentViewHolder contentViewHolder = null;
            if (convertView == null || ((Integer) convertView.getTag(R.id.tag_key_list_item_type)) != viewType) {
                if (viewType == VIEW_SYSTEM) {
                    convertView = mInflater.inflate(R.layout.chat_item_system, parent, false);
                    systemViewHolder = new SystemViewHolder();
                    systemViewHolder.chat_time_tv = (TextView) convertView.findViewById(R.id.chat_time_tv);
                    systemViewHolder.chat_content_tv = (TextView) convertView.findViewById(R.id.chat_content_tv);
                } else if (viewType == VIEW_FROM_ME_TEXT) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_text, parent, false);
                    TextViewHolder holder = new TextViewHolder();
                    holder.chat_text = (TextView) convertView.findViewById(R.id.chat_from_text);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_IMAGE) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_image, parent, false);
                    ImageViewHolder holder = new ImageViewHolder();
                    holder.chat_warp_view = (FrameLayout) convertView.findViewById(R.id.chat_from_warp_view);
                    holder.chat_image = (ImageView) convertView.findViewById(R.id.chat_from_image);
                    holder.img_progress = (ProgressBar) convertView.findViewById(R.id.img_progress);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_VOICE) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_voice, parent, false);
                    VoiceViewHolder holder = new VoiceViewHolder();
                    holder.chat_warp_view = (LinearLayout) convertView.findViewById(R.id.chat_from_warp_view);
                    holder.chat_voice = (LinearLayout) convertView.findViewById(R.id.chat_from_voice);
                    holder.chat_voice_icon = (ImageView) convertView.findViewById(R.id.chat_from_voice_icon);
                    holder.chat_voice_anim = (ImageView) convertView.findViewById(R.id.chat_from_voice_anim);
                    holder.chat_voice_length = (TextView) convertView.findViewById(R.id.chat_from_voice_length);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_LOCATION) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_location, parent, false);
                    LocationViewHolder holder = new LocationViewHolder();
                    holder.chat_location = (RelativeLayout) convertView.findViewById(R.id.chat_from_location);
                    holder.chat_address = (TextView) convertView.findViewById(R.id.chat_from_address);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_GIF) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_gif, parent, false);
                    GifViewHolder holder = new GifViewHolder();
                    holder.chat_gif_view = (GifImageView) convertView.findViewById(R.id.chat_from_gif_view);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_VIDEO) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_video, parent, false);
                    VideoViewHolder holder = new VideoViewHolder();
                    holder.chat_warp_view = (FrameLayout) convertView.findViewById(R.id.chat_from_warp_view);
                    holder.chat_thumb = (ImageView) convertView.findViewById(R.id.chat_from_thumb);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_FILE) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_file, parent, false);
                    FileViewHolder holder = new FileViewHolder();
                    holder.chat_warp_file = (ImageView) convertView.findViewById(R.id.chat_from_file);
                    holder.chat_file_name = (TextView) convertView.findViewById(R.id.file_name);
                    holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.chat_from_warp_view);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_FROM_ME_CARD) {
                    convertView = mInflater.inflate(R.layout.chat_from_item_card, parent, false);
                    CardViewHolder holder = new CardViewHolder();
                    holder.chat_warp_head = (ImageView) convertView.findViewById(R.id.chat_from_head);
                    holder.chat_person_name = (TextView) convertView.findViewById(R.id.person_name);
                    holder.chat_person_sex = (TextView) convertView.findViewById(R.id.person_sex);
                    holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.chat_from_warp_view);
                    holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                    holder.failed_img_view = (ImageView) convertView.findViewById(R.id.failed_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_TEXT) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_text, parent, false);
                    TextViewHolder holder = new TextViewHolder();
                    holder.chat_text = (TextView) convertView.findViewById(R.id.chat_to_text);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_IMAGE) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_image, parent, false);
                    ImageViewHolder holder = new ImageViewHolder();
                    holder.chat_warp_view = (FrameLayout) convertView.findViewById(R.id.chat_to_warp_view);
                    holder.chat_image = (ImageView) convertView.findViewById(R.id.chat_to_image);
                    holder.img_progress = (ProgressBar) convertView.findViewById(R.id.img_progress);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_VOICE) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_voice, parent, false);
                    VoiceViewHolder holder = new VoiceViewHolder();
                    holder.chat_warp_view = (LinearLayout) convertView.findViewById(R.id.chat_to_warp_view);
                    holder.chat_voice = (LinearLayout) convertView.findViewById(R.id.chat_to_voice);
                    holder.chat_voice_icon = (ImageView) convertView.findViewById(R.id.chat_to_voice_icon);
                    holder.chat_voice_anim = (ImageView) convertView.findViewById(R.id.chat_to_voice_anim);
                    holder.chat_voice_length = (TextView) convertView.findViewById(R.id.chat_to_voice_length);
                    holder.voice_progress = (ProgressBar) convertView.findViewById(R.id.voice_progress);
                    holder.unread_img_view = (ImageView) convertView.findViewById(R.id.unread_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_LOCATION) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_location, parent, false);
                    LocationViewHolder holder = new LocationViewHolder();
                    holder.chat_location = (RelativeLayout) convertView.findViewById(R.id.chat_to_location);
                    holder.chat_address = (TextView) convertView.findViewById(R.id.chat_to_address);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_GIF) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_gif, parent, false);
                    GifViewHolder holder = new GifViewHolder();
                    holder.chat_gif_view = (GifImageView) convertView.findViewById(R.id.chat_to_gif_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_VIDEO) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_video, parent, false);
                    VideoViewHolder holder = new VideoViewHolder();
                    holder.chat_warp_view = (FrameLayout) convertView.findViewById(R.id.chat_to_warp_view);
                    holder.chat_thumb = (ImageView) convertView.findViewById(R.id.chat_to_thumb);
                    holder.video_progress = (ProgressBar) convertView.findViewById(R.id.video_progress);
                    holder.unread_img_view = (ImageView) convertView.findViewById(R.id.unread_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_FILE) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_file, parent, false);
                    FileViewHolder holder = new FileViewHolder();
                    holder.chat_warp_file = (ImageView) convertView.findViewById(R.id.chat_to_file);
                    holder.chat_file_name = (TextView) convertView.findViewById(R.id.file_name);
                    holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.chat_to_warp_view);
                    holder.file_progress = (ProgressBar) convertView.findViewById(R.id.file_progress);
                    holder.unread_img_view = (ImageView) convertView.findViewById(R.id.unread_img_view);
                    contentViewHolder = holder;
                } else if (viewType == VIEW_TO_ME_CARD) {
                    convertView = mInflater.inflate(R.layout.chat_to_item_card, parent, false);
                    CardViewHolder holder = new CardViewHolder();
                    holder.chat_warp_head = (ImageView) convertView.findViewById(R.id.chat_to_head);
                    holder.chat_person_name = (TextView) convertView.findViewById(R.id.person_name);
                    holder.chat_person_sex = (TextView) convertView.findViewById(R.id.person_sex);
                    holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.chat_to_warp_view);
                    holder.card_progress = (ProgressBar) convertView.findViewById(R.id.card_progress);
                    holder.unread_img_view = (ImageView) convertView.findViewById(R.id.unread_img_view);
                    contentViewHolder = holder;
                }
                convertView.setTag(R.id.tag_key_list_item_type, viewType);
                if (systemViewHolder != null) {
                    convertView.setTag(R.id.tag_key_list_item_view, systemViewHolder);
                } else if (contentViewHolder != null) {
                    contentViewHolder.cb_choose = (CheckBox) convertView.findViewById(R.id.cb_remove);
                    contentViewHolder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);
                    contentViewHolder.chat_head_iv = (ImageView) convertView.findViewById(R.id.chat_head_iv);
                    contentViewHolder.nick_name = (TextView) convertView.findViewById(R.id.nick_name);
                    convertView.setTag(R.id.tag_key_list_item_view, contentViewHolder);
                }
            } else {
                if (viewType == VIEW_SYSTEM) {
                    systemViewHolder = (SystemViewHolder) convertView.getTag(R.id.tag_key_list_item_view);
                } else {
                    contentViewHolder = (ContentViewHolder) convertView.getTag(R.id.tag_key_list_item_view);
                }
            }

			/* 设置数据 */
            final ChatMessage message = mChatMessages.get(position);
            if (viewType == VIEW_SYSTEM) {
                systemViewHolder.chat_time_tv.setText(DateFormatUtil.long2Str(message.getTimeSend() * 1000, DateFormatUtil.YMD));
                systemViewHolder.chat_content_tv.setText(message.getContent());
                return convertView;
            }
            if (isShowCB) {    //点击更多时候
                contentViewHolder.cb_choose.setVisibility(View.VISIBLE);
                contentViewHolder.cb_choose.setChecked(false);
                contentViewHolder.cb_choose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            ints.add(position);
                        } else {
                            deleInts.add(position);
                        }
                    }
                });
            } else {
                contentViewHolder.cb_choose.setVisibility(View.GONE);
            }
            /* 是否显示日期 */
            boolean showTime = true;
            if (position >= 1) {
                ChatMessage prevMessage = mChatMessages.get(position - 1);
                int prevTime = prevMessage.getTimeSend();
                int nowTime = message.getTimeSend();
                if (nowTime - prevTime < 15 * 60) {// 小于15分钟，不显示
                    showTime = false;
                }
            }
            if (showTime) {
                contentViewHolder.time_tv.setVisibility(View.VISIBLE);
                contentViewHolder.time_tv.setText(getChatTime(message.getTimeSend()));
            } else {
                contentViewHolder.time_tv.setVisibility(View.GONE);
            }
            /* 处理From和To不一样的地方 */
            if (viewType >= VIEW_FROM_ME_TEXT && viewType <= VIEW_FROM_ME_VIDEO || viewType == VIEW_FROM_ME_FILE
                    || viewType == VIEW_FROM_ME_CARD) {// MSG_FROM_ME
                if (mLoginUserId.equals(Friend.ID_SYSTEM_MESSAGE)) {// 我就是系统账号，那么显示系统头像
                    contentViewHolder.chat_head_iv.setImageResource(R.drawable.im_notice);
                } else {// 其他
                    AvatarHelper.getInstance().displayAvatar(mLoginUserId, contentViewHolder.chat_head_iv, true);
                }
                if (viewType == VIEW_FROM_ME_CARD) {
                    CardViewHolder imageViewHolder = (CardViewHolder) contentViewHolder;
                    AvatarHelper.getInstance().displayAvatar(message.getCardId(), imageViewHolder.chat_warp_head, true);
                }
                contentViewHolder.chat_head_iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMessageEventListener.onMyAvatarClick();
                    }
                });
                // 如果消息是从我发出的，就会有个发送的状态
                switch (message.getMessageState()) {
                    case ChatMessageListener.MESSAGE_SEND_ING:
                        // Log.d("roamer","contentview..MESSAGE_SEND_ING");
                        contentViewHolder.progress.setVisibility(View.VISIBLE);
                        contentViewHolder.failed_img_view.setVisibility(View.GONE);
                        break;
                    case ChatMessageListener.MESSAGE_SEND_SUCCESS:
                        // Log.d("roamer","contentview..MESSAGE_SEND_SUCCESS");
                        contentViewHolder.progress.setVisibility(View.GONE);
                        contentViewHolder.failed_img_view.setVisibility(View.GONE);
                        break;
                    case ChatMessageListener.MESSAGE_SEND_FAILED:
                        // Log.d("roamer","contentview..MESSAGE_SEND_ING");
                        contentViewHolder.progress.setVisibility(View.GONE);
                        contentViewHolder.failed_img_view.setVisibility(View.VISIBLE);
                        break;
                }
                contentViewHolder.failed_img_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.getMessageState() == ChatMessageListener.MESSAGE_SEND_FAILED)
                            mMessageEventListener.onSendAgain(message);
                    }
                });
            } else {
                final String fromUserId = message.getFromUserId();
                final String fromUserName = message.getFromUserName();
                if (fromUserId.equals(Friend.ID_SYSTEM_MESSAGE)) {// 好友是系统账号，那么显示系统头像
                    contentViewHolder.chat_head_iv.setImageResource(R.drawable.im_notice);
                } else {// 其他
                    AvatarHelper.getInstance().displayAvatar(fromUserId, contentViewHolder.chat_head_iv, true);
                }

                contentViewHolder.chat_head_iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMessageEventListener.onFriendAvatarClick(fromUserId);
                    }
                });
                contentViewHolder.chat_head_iv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mMessageEventListener.onFriendAvatarLongClick(fromUserName);
                        return true;
                    }
                });
            }

            // 显示昵称
            if (message.getFromUserId().equals(mLoginUserId)) {  //当消息的发送人是本儿时候不显示名字
                contentViewHolder.nick_name.setVisibility(View.GONE);
                contentViewHolder.nick_name.setText(mLoginNickName);
            } else {     //非本人
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, message.getFromUserId());
                if (TextUtils.isEmpty(mRoomNickName)) {
                    contentViewHolder.nick_name.setVisibility(View.GONE);
                    contentViewHolder.nick_name.setText(mLoginNickName);
                } else {
                    contentViewHolder.nick_name.setVisibility(View.VISIBLE);
//                    if (message.getType() == 8) {
//
//                        getUserByUserId(message.getFromUserId(), contentViewHolder);
//                    } else {
                    contentViewHolder.nick_name.setText(friend == null ? message.getFromUserName() :
                            (friend.getRemarkName() != null ? friend.getRemarkName() :
                                    friend.getNickName()));
//                    }
                }
            }
            View longView = null;
            /* 处理具体显示的消息内容 */
            switch (message.getType()) {
                case XmppMessage.TYPE_TEXT: {
                    String s = StringUtil.replaceSpecialChar(message.getContent());
                    CharSequence charSequence = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
                    ((TextViewHolder) contentViewHolder).chat_text.setText(charSequence);
                    // ((TextViewHolder)
                    // contentViewHolder).chat_text.setOnLongClickListener(this);
                    longView = ((TextViewHolder) contentViewHolder).chat_text;

                }
                break;
                case XmppMessage.TYPE_IMAGE: {
                    boolean imageFromDisk = false;
                    File file = null;
                    if (message.isMySend()) {
                        String filePath = message.getFilePath();
                        if (!TextUtils.isEmpty(filePath)) {
                            file = new File(filePath);
                            if (file.exists()) {
                                imageFromDisk = true;
                            }
                        }
                    }
//                    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.picture_isloading).build();            //加载图片时的图片
//                            .showImageForEmptyUri(R.drawable.ic_empty)         //没有图片资源时的默认图片
//                            .showImageOnFail(R.drawable.ic_error)              //加载失败时的图片
                    if (imageFromDisk) {
                        // Log.d("wang","imageFromDisk...");
//                        ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(),
//                                ((ImageViewHolder) contentViewHolder).chat_image, options,new ImageLoadingFromPathListener(
//                                        message.getContent(), ((ImageViewHolder) contentViewHolder).img_progress));
                        ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(),
                                ((ImageViewHolder) contentViewHolder).chat_image, new ImageLoadingFromPathListener(
                                        message.getContent(), ((ImageViewHolder) contentViewHolder).img_progress));
                    } else if (message.getContent() != null) {
                        // Log.d("wang","message.getContent()...");
//                        ImageLoader.getInstance().displayImage(message.getContent(),
//                                ((ImageViewHolder) contentViewHolder).chat_image,options,
//                                new ImageLoadingFromUrlListener(((ImageViewHolder) contentViewHolder).img_progress));
                        ImageLoader.getInstance().displayImage(message.getContent(),
                                ((ImageViewHolder) contentViewHolder).chat_image,
                                new ImageLoadingFromUrlListener(((ImageViewHolder) contentViewHolder).img_progress));
                    }
                    longView = ((ImageViewHolder) contentViewHolder).chat_warp_view;

                }

                break;
                case XmppMessage.TYPE_VOICE: {
                    VoiceViewHolder voiceHolder = (VoiceViewHolder) contentViewHolder;
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) voiceHolder.chat_voice.getLayoutParams();
                    params.width = DisplayUtil.getVoiceViewWidth(mContext, message.getTimeLen());
                    voiceHolder.chat_voice.requestLayout();
                    voiceHolder.chat_voice_length.setText(message.getTimeLen() + "s");
                    if (mPlayVoiceId == -1 || mPlayVoiceId != message.get_id()) {
                        voiceHolder.chat_voice_anim.setVisibility(View.GONE);
                        voiceHolder.chat_voice_icon.setVisibility(View.VISIBLE);
                    } else {
                        voiceHolder.chat_voice_anim.setVisibility(View.VISIBLE);
                        voiceHolder.chat_voice_icon.setVisibility(View.GONE);
                        mPlayVoiceViewHolder = voiceHolder;
                    }

                    if (!message.isMySend()) {
                        voiceHolder.voice_progress.setVisibility(View.GONE);
                        if (!message.isRead()) {
                            voiceHolder.unread_img_view.setVisibility(View.VISIBLE);
                        } else {
                            voiceHolder.unread_img_view.setVisibility(View.GONE);
                        }

                    }

                    // 是否要去下载
                    boolean voicefromDisk = false;
                    File voicefile = null;
                    String filePath = message.getFilePath();
                    if (!TextUtils.isEmpty(filePath)) {
                        voicefile = new File(filePath);
                        if (voicefile.exists()) {
                            voicefromDisk = true;
                        }
                    }
                    if (!voicefromDisk) {
                        Downloader.getInstance().addDownload(message.getContent(), voiceHolder.progress,
                                new VoiceDownloadListener(message));
                    }
                    longView = voiceHolder.chat_warp_view;
                }
                break;
                case XmppMessage.TYPE_LOCATION: {
                    Log.d("roamer", "....TYPE_LOCATION....");
                    LocationViewHolder locationViewHolder = (LocationViewHolder) contentViewHolder;
                    if (!TextUtils.isEmpty(message.getContent())) {
                        locationViewHolder.chat_location.setVisibility(View.VISIBLE);
                        locationViewHolder.chat_address.setText(message.getContent());

                        locationViewHolder.chat_location.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                Intent intent = new Intent("com.modular.baidu.BaiduMapActivity");
                                intent.putExtra("latitude", Double.valueOf(message.getLocation_x()));
                                intent.putExtra("longitude", Double.valueOf(message.getLocation_y()));
                                intent.putExtra("userName", message.getFromUserName());
                                mContext.startActivity(intent);
                            }
                        });
                    } else {
                        String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
                        locationViewHolder.chat_location.setVisibility(View.VISIBLE);
                        locationViewHolder.chat_address.setText(address);

                        locationViewHolder.chat_location.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent("com.modular.baidu.BaiduMapActivity");
                                intent.putExtra("latitude", Double.valueOf(message.getLocation_x()));
                                intent.putExtra("longitude", Double.valueOf(message.getLocation_y()));
                                intent.putExtra("userName", message.getFromUserName());
                                mContext.startActivity(intent);
                            }
                        });
                    }
                    longView = locationViewHolder.chat_location;
                }
                break;
                case XmppMessage.TYPE_GIF: {
                    Log.d("roamer", "....TYPE_GIF....");
                    GifViewHolder gifViewHolder = (GifViewHolder) contentViewHolder;
                    String gifName = message.getContent();
                    int resId = SmileyParser.Gifs.textMapId(gifName);
                    if (resId != -1) {
                        int margin = DisplayUtil.dip2px(mContext, 20);
                        RelativeLayout.LayoutParams paramsL = (RelativeLayout.LayoutParams) gifViewHolder.chat_gif_view
                                .getLayoutParams();
                        paramsL.setMargins(margin, 0, margin, 0);
                        gifViewHolder.chat_gif_view.setImageResource(resId);
                    } else {
                        gifViewHolder.chat_gif_view.setImageBitmap(null);
                    }
                    longView = gifViewHolder.chat_gif_view;
                }
                break;

                case XmppMessage.TYPE_VIDEO: {
                    try {
                        VideoViewHolder videoViewHolder = (VideoViewHolder) contentViewHolder;
                        if (!message.isMySend()) {
                            videoViewHolder.video_progress.setVisibility(View.GONE);
                            if (!message.isRead()) {
                                videoViewHolder.unread_img_view.setVisibility(View.VISIBLE);
                            } else {
                                videoViewHolder.unread_img_view.setVisibility(View.GONE);
                            }
                        }

                        // 是否要去下载
                        boolean downLoad = true;
                        File file = null;
                        String filePath = message.getFilePath();
                        if (!TextUtils.isEmpty(filePath)) {
                            file = new File(filePath);
                            if (file.exists()) {
                                downLoad = false;
                            }
                        }
                        videoViewHolder.chat_thumb.setImageResource(R.drawable.defaultpic);
                        if (downLoad) {// 去下载
                            videoViewHolder.chat_thumb.setTag(message.get_id());// 设置Tag，防止在下载完成设置图片的时候，Item被其他视图回收使用了，覆盖其他的视图了
                            Downloader.getInstance().addDownload(message.getContent(), videoViewHolder.video_progress,
                                    new VideoDownloadListener(message, videoViewHolder.chat_thumb));
                        } else {// 不需要加载，直接拿本地的
                            Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(filePath);
                            if (bitmap == null || bitmap.isRecycled()) {
                                bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND);
                                if (filePath == null || bitmap == null) {

                                } else {
                                    ImageLoader.getInstance().getMemoryCache().put(filePath, bitmap);
                                }
                            }
                            if (bitmap != null && !bitmap.isRecycled()) {
                                videoViewHolder.chat_thumb.setImageBitmap(bitmap);
                            } else {
                                videoViewHolder.chat_thumb.setImageBitmap(null);
                            }
                        }
                        longView = videoViewHolder.chat_warp_view;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                case XmppMessage.TYPE_CARD:
                    //TODO  标记
                    CardViewHolder cardViewHolder = (CardViewHolder) contentViewHolder;
                    if (!TextUtils.isEmpty(message.getContent())) {
                        cardViewHolder.chat_head_iv.setVisibility(View.VISIBLE);
                        cardViewHolder.chat_person_name.setText(MyApplication.getInstance().getString(R.string.user_nickname) + message.getContent());
                        AvatarHelper.getInstance().displayAvatar(message.getObjectId(), cardViewHolder.chat_warp_head, true);
                        if (!message.isMySend()) {
                            cardViewHolder.card_progress.setVisibility(View.GONE);
                            if (!message.isRead()) {
                                cardViewHolder.unread_img_view.setVisibility(View.VISIBLE);
                            } else {
                                cardViewHolder.unread_img_view.setVisibility(View.GONE);
                            }
                        }

                    } else {
                    }
                    longView = cardViewHolder.relativeLayout;
                    break;
                case XmppMessage.TYPE_FILE:
                    FileViewHolder fileViewHolder = (FileViewHolder) contentViewHolder;
                    if (!message.isMySend()) {
                        fileViewHolder.file_progress.setVisibility(View.GONE);
                        if (!message.isRead()) {
                            fileViewHolder.unread_img_view.setVisibility(View.VISIBLE);
                        } else {
                            fileViewHolder.unread_img_view.setVisibility(View.GONE);
                        }
                    }

                    // 是否要去下载
                /*boolean downLoad = true;
                File file = null;
				String filePath = message.getFilePath();
				if (!TextUtils.isEmpty(filePath)) {
					file = new File(filePath);
					if (file.exists()) {
						downLoad = false;
					}
				} else {
					filePath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/shiku";
				}*/
                    String filePath = message.getFilePath();
                    String name = null;
                    boolean downLoad = true;
                    if (filePath != null) {

                        int pointIndex = filePath.lastIndexOf(".");
                        if (pointIndex != -1) {
                            String type = filePath.substring(pointIndex + 1).toLowerCase();
                            for (int i = 0; i < fileTypes.length; i++) {
                                if (type.equals(fileTypes[i])) {
                                    try {
                                        int resId = getResources().getIdentifier(type, "drawable", mContext.getPackageName());
                                        fileViewHolder.chat_warp_file.setImageResource(resId);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }
                        int start = filePath.lastIndexOf("/");
                        name = filePath.substring(start + 1).toLowerCase();
                        Log.d("roamer", "filename::" + name);
                    }
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sk/" + name);
                    if (file.exists()) {
                        downLoad = false;
                    }
                    fileViewHolder.chat_file_name.setText(name);
                    if (downLoad) {// 去下载
                        DownloadFileToSD(message.getContent(), name);
                    /*Downloader.getInstance().addDownload(message.getContent(), fileViewHolder.file_progress,
                            new FileDownloadListener(message));*/

                    }
                    longView = fileViewHolder.relativeLayout;
                    break;
            }
            if (longView != null) {// 设置长按弹出复制,转发窗口
                setLongClickInterface(message, longView, position);

            }
            /* 处理点击时间的监听 */
            if (viewType == VIEW_FROM_ME_VOICE || viewType == VIEW_TO_ME_VOICE) {
                final VoiceViewHolder voiceHolder = (VoiceViewHolder) contentViewHolder;
                voiceHolder.chat_warp_view.setOnClickListener(null);
                voiceHolder.chat_warp_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isDownload = false;
                        String filePath = message.getFilePath();
                        if (!TextUtils.isEmpty(filePath)) {
                            File voicefile = new File(filePath);
                            if (voicefile.exists()) {
                                isDownload = true;
                            }
                        }

                        if (isDownload) {
                            if (!message.isMySend() && !message.isRead()) {
                                message.setRead(true);
                                ChatMessageDao.getInstance().updateMessageReadState(mLoginUserId, mToUserId,
                                        message.get_id(), true);
                                if (voiceHolder.unread_img_view != null) {
                                    voiceHolder.unread_img_view.setVisibility(View.GONE);
                                }
                            }
                            play(voiceHolder, message);
                        }
                    }
                });
            } else if (viewType == VIEW_FROM_ME_IMAGE || viewType == VIEW_TO_ME_IMAGE) {
                ImageViewHolder imageViewHolder = (ImageViewHolder) contentViewHolder;
                imageViewHolder.chat_warp_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                        intent.putExtra(AppConstant.EXTRA_IMAGE_URI, message.getContent());
                        mContext.startActivity(intent);
                        ((Activity) mContext).overridePendingTransition(0, 0);
                    }
                });
            } else if (viewType == VIEW_FROM_ME_VIDEO || viewType == VIEW_TO_ME_VIDEO) {
                final VideoViewHolder videoViewHolder = (VideoViewHolder) contentViewHolder;
                videoViewHolder.chat_warp_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!message.isMySend() && !message.isRead()) {
                            message.setRead(true);
                            ChatMessageDao.getInstance().updateMessageReadState(mLoginUserId, mToUserId,
                                    message.get_id(), true);
                            if (videoViewHolder.unread_img_view != null) {
                                videoViewHolder.unread_img_view.setVisibility(View.GONE);
                            }
                        }

                        Intent intent = new Intent("com.modular.tool.VideoPlayActivity");
                        // 是否要去下载
                        boolean downLoad = true;
                        File file = null;
                        String filePath = message.getFilePath();
                        if (!TextUtils.isEmpty(filePath)) {
                            file = new File(filePath);
                            if (file.exists()) {
                                downLoad = false;
                            }
                        }
                        if (downLoad) {
                            intent.putExtra(AppConstant.EXTRA_FILE_PATH, message.getContent());
                        } else {
                            intent.putExtra(AppConstant.EXTRA_FILE_PATH, filePath);
                        }
                        mContext.startActivity(intent);
                    }
                });
            } else if (viewType == VIEW_FROM_ME_FILE || viewType == VIEW_TO_ME_FILE) {
                final FileViewHolder fileViewHolder = (FileViewHolder) contentViewHolder;
                fileViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //						Toast.makeText(mContext, "文件保存在:" + message.getFilePath(), 0).show();

                        if (!message.isMySend() && !message.isRead()) {
                            message.setRead(true);
                            ChatMessageDao.getInstance().updateMessageReadState(mLoginUserId, mToUserId,
                                    message.get_id(), true);
                            if (fileViewHolder.unread_img_view != null) {
                                fileViewHolder.unread_img_view.setVisibility(View.GONE);
                            }
                        }
                        if (!message.isMySend()) {//是接收到的文件才会去打开

                            Intent intent = new Intent("com.modular.afilechooser.FileReceiverActivity");
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(Constants.INSTANT_MESSAGE, message);
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        } else {//是自己的文件就会去选择打开方式
                            FileOpenWays open = new FileOpenWays(mContext);
                            open.openFiles(message.getFilePath());
                        }
                    }
                });
            } else if (viewType == VIEW_TO_ME_LOCATION && viewType == VIEW_FROM_ME_LOCATION) {// 此处没有用
                // ,上面已经设置了点击事件
                LocationViewHolder locationViewHolder = (LocationViewHolder) contentViewHolder;
                locationViewHolder.chat_location.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent("com.modular.baidu.BaiduMapActivity");
                        intent.putExtra("latitude", message.getLocation_x());
                        intent.putExtra("longitude", message.getLocation_y());
                        intent.putExtra("userName", message.getFromUserName());
                        mContext.startActivity(intent);
                    }
                });
            } else if (viewType == VIEW_TO_ME_CARD || viewType == VIEW_FROM_ME_CARD) {
                final CardViewHolder cardViewHolder = (CardViewHolder) contentViewHolder;
                cardViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        String userId = message.getObjectId();
                        if (!message.isMySend() && !message.isRead()) {
                            message.setRead(true);
                            ChatMessageDao.getInstance().updateMessageReadState(mLoginUserId, mToUserId,
                                    message.get_id(), true);
                            if (cardViewHolder.unread_img_view != null) {
                                cardViewHolder.unread_img_view.setVisibility(View.GONE);
                            }
                        }
//                        if (viewType == VIEW_TO_ME_CARD) {  //发给我的时候
//                            //缺少cardid;只能根据名称来获取信息
//                            Friend friend = FriendDao.getInstance().getFriendByKey("nickName", message.getFromUserName());
//                            if (friend != null) {
//                                userId = friend.getUserId();
//                            } else {
//                                DBManager dbManager = new DBManager(mContext);
//                                String master = CommonUtil.getSharedPreferences(mContext, "erp_master");
//                                List<EmployeesEntity> employeeList = dbManager.select_getEmployee(new String[]{message.getFromUserName(), master}, "em_name=? and whichsys=?");
//                                if (!ListUtils.isEmpty(employeeList)) {
//                                    userId = String.valueOf(employeeList.get(0).getEm_IMID());
//                                } else {
////                                    ViewUtil.ToastMessage(MyApplication.getInstance(), "企业架构数据没有初始化！");
//                                }
//                            }
//                        } else {
//                            userId = message.getCardId();
//                        }

                        if (userId == null) {
                            Intent intent = new Intent("com.modular.view.CardInfoActivity");
                            intent.putExtra(AppConstant.EXTRA_NICK_NAME, message.getContent());
                            mContext.startActivity(intent);
                        } else {
                            Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
                            intent.putExtra(AppConstant.EXTRA_USER_ID, userId);
                            mContext.startActivity(intent);
                        }


                    }
                });
            }
            // contentViewHolder.chat_warp_view.setOnLongClickListener(new
            // OnLongClickListener() {
            //
            // @Override
            // public boolean onLongClick(View v) {
            // Log.d("roamer",
            // "contentViewHolder.chat_warp_view OnLongClickListener");
            // return false;
            // }
            // });
            // contentViewHolder.gif_bg.setOnLongClickListener(new
            // OnLongClickListener() {
            //
            // @Override
            // public boolean onLongClick(View v) {
            // Log.d("roamer",
            // "contentViewHolder.gif_bg OnLongClickListener");
            // return false;
            // }
            // });

            // contentViewHolder.chat_head_iv.setOnClickListener(new
            // OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // // TODO
            // // mMessageEventListener.onAvatarClick(friendEntity)
            // }
            // });
            return convertView;
        }

        private AppConfig mConfig = MyApplication.getInstance().getConfig();
        private FastVolley mFastVolley = MyApplication.getInstance().getFastVolley();

        private void getUserByUserId(String fromUserId, final ContentViewHolder contentViewHolder) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("access_token", MyApplication.getInstance().mAccessToken);
            params.put("userId", fromUserId);
            StringJsonObjectRequest<User> request = new StringJsonObjectRequest<User>(mConfig.USER_GET_URL,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            ToastUtil.showErrorNet(mContext);
                        }
                    }, new StringJsonObjectRequest.Listener<User>() {

                @Override
                public void onResponse(ObjectResult<User> result) {
                    boolean success = Result.defaultParser(mContext, result, true);
                    if (success && result.getData() != null) {
                        User mUser = result.getData();

                        contentViewHolder.nick_name.setText(mUser.getNickName());
                    } else {
                        contentViewHolder.nick_name.setText("");
                    }
                }
            }, User.class, params);
            mFastVolley.addDefaultRequest(HASHCODE, request);
        }

        private void setLongClickInterface(final ChatMessage message, View view, final int position) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
                    vib.vibrate(40);// 只震动一秒，一次
                    // Toast.makeText(mContext, "长按了", 0).show();
                    // Log.d("wang", "长按了文本内容");
                    // 实例化SelectMessageWindow
                    menuWindow = new SelectMessageWindow(mContext, new ClickListener(message, position),
                            message.getType());
                    // 显示窗口
                    menuWindow.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    return false;
                }
            });
        }
    }

    /**
     * 下载文件到sd卡
     *
     * @param dowloadUrl
     * @param name
     */
    public void DownloadFileToSD(final String dowloadUrl, final String name) {
        // 获取SD卡目录
        final String dowloadDir = Environment.getExternalStorageDirectory()
                + "/sk/";
        File file = new File(dowloadDir);
        //创建下载目录
        if (!file.exists()) {
            file.mkdirs();
        }

        //读取下载线程数，如果为空，则单线程下载
        final int downloadTN = 2;
    /*//如果下载文件名为空则获取Url尾为文件名	String fileName =  url.substring(fileNameStart);*/
        //启动文件下载线程
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                int threadNum = 2;
                FileDownloadThread[] fds = new FileDownloadThread[threadNum];
                try {
                    URL url = new URL(dowloadUrl);
                    URLConnection conn = url.openConnection();
                    //获取下载文件的总大小
                    int fileSize = conn.getContentLength();
                    //计算每个线程要下载的数据量
                    int blockSize = fileSize / threadNum;
                    // 解决整除后百分比计算误差
                    int downloadSizeMore = (fileSize % threadNum);
                    File file = new File(dowloadDir + name);
                    for (int i = 0; i < threadNum; i++) {
                        //启动线程，分别下载自己需要下载的部分
                        FileDownloadThread fdt = new FileDownloadThread(url, file,
                                i * blockSize, (i + 1) * blockSize - 1);
                        fdt.setName("Thread" + i);
                        fdt.start();
                        fds[i] = fdt;
                    }
                    boolean finished = false;
                    while (!finished) {
                        // 先把整除的余数搞定
                        int downloadedSize = downloadSizeMore;
                        finished = true;
                        for (int i = 0; i < fds.length; i++) {
                            downloadedSize += fds[i].getDownloadSize();
                            if (!fds[i].isFinished()) {
                                finished = false;
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    public class ClickListener implements View.OnClickListener {
        private ChatMessage message;
        private int position;

        public ClickListener(ChatMessage message, int position) {
            this.message = message;
            this.position = position;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            int id = v.getId();
            if (id == R.id.btn_copy) {
                String s = StringUtil.replaceSpecialChar(message.getContent());
                CharSequence charSequence = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
                // 获得剪切板管理者,复制文本内容
                ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(charSequence);
            } else if (R.id.btn_instant == id) {
                Intent intent = new Intent("com.modular.message.InstantMessageActivity");
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.INSTANT_MESSAGE, message);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
            } else if (R.id.btn_delete == id) {
                ToastUtil.showToast(mContext, R.string.delete_all_succ);
              /* if(mChatMessages!=null){
                   mChatMessages.remove(position);
               }*/
                Intent broadcast = new Intent(Constants.CHAT_MESSAGE_DELETE_ACTION);//发送广播去界面更新
                broadcast.putExtra(Constants.CHAT_REMOVE_MESSAGE_FALG, 1);
                broadcast.putExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, position);
                mContext.sendBroadcast(broadcast);
            } else if (R.id.btn_more == id) {
                if (ints == null) {
                    ints = new ArrayList<>();
                }
                if (deleInts == null) {
                    deleInts = new ArrayList<>();

                }
                deleInts.clear();
                ints.clear();
                if (mChatContentAdapter != null) {
                    isShowCB = true;
                    mChatContentAdapter.notifyDataSetChanged();
                }
                Intent broadcast1 = new Intent(Constants.CHAT_MESSAGE_DELETE_ACTION);//发送广播去界面更新
                broadcast1.putExtra(Constants.CHAT_REMOVE_MESSAGE_FALG, 2);
                mContext.sendBroadcast(broadcast1);
            }
        }
    }

    SelectMessageWindow menuWindow;

    class ImageLoadingFromUrlListener implements ImageLoadingListener {
        private ProgressBar progressBar;

        public ImageLoadingFromUrlListener(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            if (arg1 != null && arg2 != null && !arg2.isRecycled()) {
                ((ImageView) arg1).setImageBitmap(arg2);
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (arg1 != null) {
                ((ImageView) arg1).setImageResource(R.drawable.image_download_fail_icon);
            }
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    class ImageLoadingFromPathListener implements ImageLoadingListener {
        private String url;
        private ProgressBar progressBar;

        public ImageLoadingFromPathListener(String url, ProgressBar progressBar) {
            this.url = url;
            this.progressBar = progressBar;
        }

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            if (arg1 != null && arg2 != null && !arg2.isRecycled()) {
                ((ImageView) arg1).setImageBitmap(arg2);
            }
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            if (arg1 != null && !TextUtils.isEmpty(url)) {
                ImageLoader.getInstance().displayImage(url, (ImageView) arg1,
                        new ImageLoadingFromUrlListener(progressBar));
            }
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {

        }
    }

    private class VoiceDownloadListener implements DownloadListener {
        private ChatMessage message;

        public VoiceDownloadListener(ChatMessage message) {
            this.message = message;
        }

        @Override
        public void onStarted(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(String uri, com.core.xmpp.downloader.FailReason failReason, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onComplete(String uri, String filePath, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            message.setFilePath(filePath);
            ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, message.get_id(), true,
                    filePath);
        }

        @Override
        public void onCancelled(String uri, View view) {

        }

    }

    private class FileDownloadListener implements DownloadListener {
        private ChatMessage message;

        public FileDownloadListener(ChatMessage message) {
            this.message = message;
        }

        @Override
        public void onStarted(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(String uri, com.core.xmpp.downloader.FailReason failReason, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }

        }


        @Override
        public void onComplete(String uri, String filePath, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            message.setFilePath(filePath);
            ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, message.get_id(), true,
                    filePath);
        }

        @Override
        public void onCancelled(String uri, View view) {

        }

    }

    private class VideoDownloadListener implements DownloadListener {
        private ChatMessage message;
        private ImageView imageView;

        public VideoDownloadListener(ChatMessage message, ImageView imageView) {
            this.message = message;
            this.imageView = imageView;
        }

        @Override
        public void onStarted(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(String uri, com.core.xmpp.downloader.FailReason failReason, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }

        }


        @SuppressLint("NewApi")
        @Override
        public void onComplete(String uri, String filePath, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            message.setFilePath(filePath);
            ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, message.get_id(), true,
                    filePath);
            if (imageView != null && ((Integer) imageView.getTag()) == message.get_id()) {
                Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(filePath);
                if (bitmap == null || bitmap.isRecycled()) {
                    bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND);
                    ImageLoader.getInstance().getMemoryCache().put(filePath, bitmap);
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageBitmap(null);
                }
            }
        }

        @Override
        public void onCancelled(String uri, View view) {

        }

    }

    /***************
     * ViewHolder
     *****************************/
    class SystemViewHolder {
        TextView chat_time_tv;
        TextView chat_content_tv;
    }

    abstract class ContentViewHolder {
        TextView time_tv;
        ImageView chat_head_iv;
        CheckBox cb_choose;
        ProgressBar progress;// 只有From的item有
        ImageView failed_img_view;// 只有From的item有
        TextView nick_name;
    }

    class TextViewHolder extends ContentViewHolder {
        TextView chat_text;
    }

    class ImageViewHolder extends ContentViewHolder {
        FrameLayout chat_warp_view;
        ImageView chat_image;
        ProgressBar img_progress;
    }

    class VoiceViewHolder extends ContentViewHolder {
        LinearLayout chat_warp_view;
        LinearLayout chat_voice;
        ImageView chat_voice_icon;
        ImageView chat_voice_anim;
        TextView chat_voice_length;
        ProgressBar voice_progress;// 只有To_me才有
        ImageView unread_img_view;// 只有To_me才有
    }

    class LocationViewHolder extends ContentViewHolder {
        RelativeLayout chat_location;
        TextView chat_address;
    }

    class GifViewHolder extends ContentViewHolder {
        GifImageView chat_gif_view;
    }

    class VideoViewHolder extends ContentViewHolder {
        FrameLayout chat_warp_view;
        ImageView chat_thumb;
        ProgressBar video_progress;// 只有To_me才有
        ImageView unread_img_view;// 只有To_me才有
    }

    class FileViewHolder extends ContentViewHolder {
        RelativeLayout relativeLayout;
        ImageView chat_warp_file;
        TextView chat_file_name;
        ProgressBar file_progress;// 只有To_me才有
        ImageView unread_img_view;// 只有To_me才有
    }

    class CardViewHolder extends ContentViewHolder {
        RelativeLayout relativeLayout;
        ImageView chat_warp_head;
        TextView chat_person_name;
        TextView chat_person_sex;
        ProgressBar card_progress;// 只有To_me才有
        ImageView unread_img_view;// 只有To_me才有
    }

    public void stopPlayVoice() {
        if (mVoicePlayer != null) {
            mVoicePlayer.stop();
        }
    }
    /********************** 播放声音 ***********************************/
    /**
     * @param viewHolder
     */
    private void play(VoiceViewHolder viewHolder, ChatMessage message) {
        if (mPlayVoiceId == -1) {// 没有在播放
            String voicePath = message.getFilePath();
            try {
                mVoicePlayer.play(voicePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayVoiceId = message.get_id();
            viewHolder.chat_voice_anim.setVisibility(View.VISIBLE);
            viewHolder.chat_voice_icon.setVisibility(View.GONE);
            mPlayVoiceViewHolder = viewHolder;
        } else {
            if (mPlayVoiceId == message.get_id()) {
                mVoicePlayer.stop();
                mPlayVoiceId = -1;
                viewHolder.chat_voice_anim.setVisibility(View.GONE);
                viewHolder.chat_voice_icon.setVisibility(View.VISIBLE);
                mPlayVoiceViewHolder = null;
            } else {// 正在播放别的， 在播放这个
                mVoicePlayer.keepStop();
                mPlayVoiceId = -1;
                if (mPlayVoiceViewHolder != null) {
                    mPlayVoiceViewHolder.chat_voice_anim.setVisibility(View.GONE);
                    mPlayVoiceViewHolder.chat_voice_icon.setVisibility(View.VISIBLE);
                }

                String voicePath = message.getFilePath();
                try {
                    mVoicePlayer.play(voicePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPlayVoiceId = message.get_id();
                viewHolder.chat_voice_anim.setVisibility(View.VISIBLE);
                viewHolder.chat_voice_icon.setVisibility(View.GONE);
                mPlayVoiceViewHolder = viewHolder;
            }

        }

    }

    @Override
    public void onFinishPlay(MediaPlayer player) {
        mPlayVoiceId = -1;
        if (mPlayVoiceViewHolder != null) {
            mPlayVoiceViewHolder.chat_voice_anim.setVisibility(View.GONE);
            mPlayVoiceViewHolder.chat_voice_icon.setVisibility(View.VISIBLE);
        }
        mPlayVoiceViewHolder = null;
    }

    @Override
    public void onErrorPlay() {
        mPlayVoiceId = -1;
        if (mPlayVoiceViewHolder != null) {
            mPlayVoiceViewHolder.chat_voice_anim.setVisibility(View.GONE);
            mPlayVoiceViewHolder.chat_voice_icon.setVisibility(View.VISIBLE);
        }
        mPlayVoiceViewHolder = null;
    }

    @Override
    public void onSecondsChange(int seconds) {
    }


    private String getChatTime(long timeSend) {
        String date1 = DateFormatUtil.long2Str(timeSend * 1000, DateFormatUtil.YMD);
        String date2 = DateFormatUtil.long2Str(DateFormatUtil.YMD);
        if (date1.compareToIgnoreCase(date2) == 0) {// 是同一天
            return DateFormatUtil.long2Str(timeSend * 1000, DateFormatUtil.HM);
        } else {
            return DateFormatUtil.long2Str(timeSend * 1000, "yyyy-MM-dd HH:mm");
        }
    }


}
