package com.modular.appmessages.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.model.Friend;
import com.core.model.XmppMessage;
import com.core.utils.TimeUtils;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.sortlist.BaseSortModel;
import com.core.widget.RedView;
import com.core.xmpp.utils.HtmlUtils;
import com.modular.appmessages.R;
import com.modular.appmessages.model.MessageHeader;
import com.modular.appmessages.model.MessageNew;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class MessageNewAdapter extends RecyclerView.Adapter<MessageNewAdapter.ViewHolder> {
    private Context ct;
    private List<MessageNew> models;
    private List<MessageNew> headerModels, contentModels;


    public List<MessageNew> getModels() {
        return models;
    }

    public MessageNewAdapter(Context ct) {
        this.ct = ct;
        this.models = new ArrayList<>();
        this.headerModels = new ArrayList<>();
        this.contentModels = new ArrayList<>();
    }

    public void addHeadModel(int item, MessageNew model) {
        if (model == null) {
            return;
        }
        if (this.headerModels == null) {
            this.headerModels = new ArrayList<>();
        }
        if (ListUtils.getSize(this.headerModels) > item) {
            this.headerModels.add(item, model);
        } else {
            this.headerModels.add(model);
        }

    }

    public void setContentModels(List<MessageNew> contentModels) {
        if (contentModels != null) {
            this.models.clear();
            this.contentModels = contentModels;
            this.models.addAll(this.headerModels);
            this.models.addAll(this.contentModels);
        }
    }

    public int getHeaderSize() {
        return ListUtils.getSize(headerModels);
    }

    public void setHeaderModels(List<MessageNew> models) {
        if (models != null) {
            this.models.clear();
            this.headerModels = models;
            this.models.addAll(this.headerModels);
            this.models.addAll(this.contentModels);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_message_header, viewGroup, false));
    }


    @Override
    public int getItemCount() {
        return ListUtils.getSize(models);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView headerImg;
        private TextView headerNumTv, titleTv, headerSubTv, timeTv;
        private RedView headerRv;

        public ViewHolder(View convertView) {
            super(convertView);
            headerImg = (ImageView) convertView.findViewById(R.id.headerImg);
            headerNumTv = (TextView) convertView.findViewById(R.id.headerNumTv);
            headerRv = (RedView) convertView.findViewById(R.id.headerRv);
            titleTv = (TextView) convertView.findViewById(R.id.titleTv);
            headerSubTv = (TextView) convertView.findViewById(R.id.headerSubTv);
            timeTv = (TextView) convertView.findViewById(R.id.timeTv);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        MessageNew model = models.get(i);
        viewHolder.itemView.setTag(R.id.tag_key, i);
        viewHolder.itemView.setTag(R.id.tag_key2, model);
        if (model != null && model.getT() != null) {
            if (model.getT() instanceof MessageHeader) {
                bindHeader((MessageHeader) model.getT(), viewHolder,i);
            } else if (model.getT() instanceof BaseSortModel) {
                BaseSortModel baseSortModel = (BaseSortModel) model.getT();
                if (baseSortModel.getBean() instanceof Friend) {
                    bindView(viewHolder, (Friend) baseSortModel.getBean(), i);
                }
            }
        }
    }


    private void bindHeader(MessageHeader model, ViewHolder hodler,int position) {
        hodler.itemView.setOnLongClickListener(null);
        hodler.itemView.setOnClickListener(mOnClickListener);
        hodler.headerImg.setTag(position);
        if (model != null) {
            hodler.headerImg.setImageResource(model.getIcon());
            if (model.getRedNum() > 0) {
                hodler.headerNumTv.setVisibility(View.VISIBLE);
                hodler.headerNumTv.setText(String.valueOf(model.getRedNum()));
            } else {
                hodler.headerNumTv.setVisibility(View.GONE);
                hodler.headerNumTv.setText("");
            }
            hodler.titleTv.setText(model.getName());
            hodler.headerSubTv.setText(model.getSubDoc());
            if (model.isHideRed()) {
                hodler.headerRv.setVisibility(View.GONE);
                hodler.timeTv.setVisibility(View.VISIBLE);
                hodler.timeTv.setText(model.getTime());
            } else {
                hodler.headerRv.setVisibility(View.VISIBLE);
                hodler.headerRv.setName(model.getRedMessage());
                hodler.timeTv.setVisibility(View.GONE);
            }
        }
    }

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            if (mItemTouchListener != null && view != null) {
                Object tag = view.getTag(R.id.tag_key);
                if (tag != null && tag instanceof Integer) {
                    int id = (int) tag;
                    mItemTouchListener.longClick(id);
                }
            }
            return false;
        }
    };
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mItemTouchListener != null && view != null) {
                Object tag = view.getTag(R.id.tag_key);
                Object tag2 = view.getTag(R.id.tag_key2);
                if (tag != null && tag instanceof Integer && tag2 != null && tag2 instanceof MessageNew) {
                    int id = (int) tag;
                    MessageNew model = (MessageNew) tag2;
                    mItemTouchListener.click(id, model);
                }
            }
        }
    };

    private void bindView(final ViewHolder holder, Friend friend, final int position) {
        holder.itemView.setOnLongClickListener(mOnLongClickListener);
        holder.itemView.setOnClickListener(mOnClickListener);
        holder.headerImg.setBackgroundResource(R.color.transparent);
        //设置头像
        if (friend.getType() == XmppMessage.TYPE_ERP) {
            doShowMsgPhotos(holder, friend);
            holder.timeTv.setText(TimeUtils.getFriendlyTimeDesc(MyApplication.getInstance(), friend.getTimeSend()));
        } else if (friend.getType() == XmppMessage.TYPE_UUHELPER) {
            holder.timeTv.setText(TimeUtils.getFriendlyTimeDesc(MyApplication.getInstance(), friend.getTimeSend()));
            holder.headerImg.setImageResource(R.drawable.icon_uuhelper);
        } else {
            if (friend.getRoomFlag() == 0) {// 这是单个人
                String url = AvatarHelper.getInstance().getAvatarUrl(friend.getUserId(), false);
                holder.headerImg.setTag(position);
                ImageLoader.getInstance().displayImage(url, holder.headerImg, mNormalImageOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        LogUtil.i("gong","onLoadingStarted");
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        LogUtil.i("gong","onLoadingFailed");
                        if (holder.headerImg.getTag() != null && holder.headerImg.getTag() instanceof Integer) {
                            int item = (int) holder.headerImg.getTag();
                            if (item == position) {
                                holder.headerImg.setImageResource(R.drawable.avatar_round);
                            }
                        }
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        LogUtil.i("gong","onLoadingComplete");
                        if (holder.headerImg.getTag() != null && holder.headerImg.getTag() instanceof Integer) {
                            int item = (int) holder.headerImg.getTag();
                            if (item == position) {
                                holder.headerImg.setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        LogUtil.i("gong","onLoadingCancelled");
                    }
                });
            } else {
                if (TextUtils.isEmpty(friend.getRoomCreateUserId())) {
                    holder.headerImg.setImageResource(R.drawable.qunliao);
                } else {
                    AvatarHelper.getInstance().displayAvatarPng(friend.getRoomCreateUserId(), holder.headerImg, true);// 目前在备注名放房间的创建者Id
                }
            }
            holder.timeTv.setText(TimeUtils.getFriendlyTimeDesc(MyApplication.getInstance(), friend.getTimeSend()));
        }

        //设置名字和时间
        holder.titleTv.setText(friend.getRemarkName() != null ? friend.getRemarkName() : friend.getNickName());
        //设置内容数据显示
        CharSequence content = "";
        if (friend.getType() == XmppMessage.TYPE_TEXT) {
            String s = StringUtil.replaceSpecialChar(friend.getContent());
            content = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
        } else {
            content = friend.getContent();
        }
        if (!TextUtils.isEmpty(content)) {
            holder.headerSubTv.setText(Html.fromHtml(content.toString()));
        } else {
            holder.headerSubTv.setText("");
        }
        //设置红点显示数量
        if (friend.getUnReadNum() > 0) {
            String numStr = friend.getUnReadNum() >= 99 ? "99+" : friend.getUnReadNum() + "";
            holder.headerNumTv.setText(numStr);
            holder.headerNumTv.setVisibility(View.VISIBLE);
        } else {
            holder.headerNumTv.setVisibility(View.GONE);
        }

    }
    DisplayImageOptions   mNormalImageOptions = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(false)
                .build();
    private void doShowMsgPhotos(ViewHolder holder, Friend friend) {
        String msg_type = friend.getDescription();
        if (!TextUtils.isEmpty(msg_type)) {
            int imageurl = 0;
            switch (msg_type) {
                case "note": // 通知公告
                    imageurl = R.drawable.home_image_03_u;
                    break;
                case "common": // 普通知会
                    imageurl = R.drawable.putongzhihui;
                    break;
                case "b2b": // b2b提醒
                    imageurl = R.drawable.b2btixing;
                    break;
                case "crm":  // CRM提醒
                    imageurl = R.drawable.crmtixing;
                    break;
                case "kpi": // 考勤提醒
                    imageurl = R.drawable.kaoqintixing;
                    break;
                case "meeting": // 会议提醒
                    imageurl = R.drawable.huiyitixing;
                    break;
                case "process": // 审批知会
                    imageurl = R.drawable.shenpizhihui;
                    break;
                case "job": // 稽核提醒
                    imageurl = R.drawable.jihetixing;
                    break;
                case "system": // 知会消息
                    imageurl = R.drawable.zhihuixiaoxi;
                    break;
                case "task": // 任务提醒
                    imageurl = R.drawable.home_image_02_u;
                    break;
                default:
                    imageurl = R.drawable.gongzuotixing;
            }
            holder.headerImg.setImageResource(imageurl);
        }
    }

    private ItemTouchListener mItemTouchListener;

    public void setItemTouchListener(ItemTouchListener mItemTouchListener) {
        this.mItemTouchListener = mItemTouchListener;
    }

    public interface ItemTouchListener {
        void longClick(int id);

        void click(int id, MessageNew messageNew);
    }
}
