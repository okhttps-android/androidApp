package com.modular.appmessages.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.modular.appmessages.R;
import com.modular.appmessages.model.SubMessage;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by Bitliker on 2016/11/16.
 */
public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SubMessage> messages;
    private int[] srcs = {
            R.drawable.list_01, R.drawable.list_02, R.drawable.list_03,
            R.drawable.list_04, R.drawable.list_05, R.drawable.list_06,
            R.drawable.list_07, R.drawable.list_08, R.drawable.list_09,
            R.drawable.list_10, R.drawable.list_11, R.drawable.list_12,
            R.drawable.list_13, R.drawable.list_14, R.drawable.list_15
    };
    private int[] srcsBig = {
            R.drawable.uu_dy_image1, R.drawable.uu_dy_image2, R.drawable.uu_dy_image3, R.drawable.uu_dy_image4, R.drawable.uu_dy_image5, R.drawable.uu_dy_image6, R.drawable.uu_dy_image7
    };

    public List<SubMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SubMessage> messages) {
        if (ListUtils.isEmpty(messages)) {
            SubMessage empty = new SubMessage();
            empty.setStatus(3);
            if (this.messages == null)
                this.messages = new ArrayList<>();
            else this.messages.clear();
            this.messages.add(empty);
        } else
            this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getStatus();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_subscription_time, parent, false);
                return new TimeViewHolder(view);
            case 1:
                view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_subscription_first, parent, false);
                return new SubViewHolder(view);
            case 2:
                view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_subscription_more, parent, false);
                return new SubViewHolder(view);
            case 3:
                view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.recycler_empty_view, parent, false);
                return new EmptyViewHolder(view);
            default:
                view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_subscription_more, parent, false);
                return new SubViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            SubMessage bean = messages.get(position);
            if (holder instanceof TimeViewHolder) {
                TimeViewHolder viewHolder = (TimeViewHolder) holder;
                viewHolder.item_time_tv.setText(bean.getDate());
                viewHolder.itemView.setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.item_line));
            } else if (holder instanceof SubViewHolder) {
                boolean isFist = bean.getStatus() == 1;
                SubViewHolder viewHolder = (SubViewHolder) holder;
                String isRead = null;
                int isReColor;
                int imageSrc = 0;
                if (isFist) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(bean.getCreateTime());
                    imageSrc = srcsBig[c.get(Calendar.DAY_OF_WEEK) - 1];
                } else {
                    imageSrc = srcs[position % srcs.length];
                }
                displayFromDrawable(imageSrc, viewHolder.item_image);
                if (!bean.isRead()) {
                    isRead = MyApplication.getInstance().getString(R.string.unreaded);
                    isReColor = R.color.red;
                } else {
                    isRead = MyApplication.getInstance().getString(R.string.readed);
                    isReColor = isFist ? R.color.white : R.color.text_hine;
                }
                String title = StringUtil.isEmpty(bean.getTitle()) ? "" : bean.getTitle();
                String subTitle = StringUtil.isEmpty(bean.getSubTitle()) ? "" : bean.getSubTitle();
                viewHolder.item_isr_tv.setText(isRead);
                viewHolder.item_isr_tv.setTextColor(MyApplication.getInstance().getResources().getColor(isReColor));
                viewHolder.item_title_sub_tv.setText(subTitle);
                viewHolder.item_title_tv.setText(title);
                viewHolder.itemView.setTag(bean);
                viewHolder.itemView.setTag(R.id.position, position);
                initEvent(viewHolder.itemView);
                viewHolder.itemView.setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.white));

            }
        } catch (Exception e) {

        }
    }

    public void displayFromDrawable(int imageId, ImageView imageView) {
        // String imageUri = "drawable://" + R.drawable.image; // from drawables
        // (only images, non-9patch)
        ImageLoader.getInstance().displayImage("drawable://" + imageId,
                imageView);
    }

    private void initEvent(View itemView) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (onClickListener == null) return;
                    SubMessage subMessage = (SubMessage) view.getTag();
                    int position = (int) view.getTag(R.id.position);
                    if (subMessage == null) return;
                    onClickListener.click(view, subMessage, position);
                } catch (ClassCastException e) {

                } catch (Exception e) {

                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    if (onLongClickListener == null) return false;
                    SubMessage subMessage = (SubMessage) view.getTag();
                    int position = (int) view.getTag(R.id.position);
                    if (subMessage == null) return false;
                    onLongClickListener.longClick(view, subMessage, position);
                } catch (ClassCastException e) {
                } catch (Exception e) {
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return ListUtils.isEmpty(messages) ? 0 : messages.size();
    }

    public class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView item_time_tv;

        public TimeViewHolder(View itemView) {
            super(itemView);
            item_time_tv = (TextView) itemView.findViewById(R.id.item_time_tv);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SubViewHolder extends RecyclerView.ViewHolder {
        ImageView item_image;
        TextView item_isr_tv;
        TextView item_title_tv;
        TextView item_title_sub_tv;

        public SubViewHolder(View itemView) {
            super(itemView);
            item_image = (ImageView) itemView.findViewById(R.id.item_image);
            item_isr_tv = (TextView) itemView.findViewById(R.id.item_isr_tv);
            item_title_tv = (TextView) itemView.findViewById(R.id.item_title_tv);
            item_title_sub_tv = (TextView) itemView.findViewById(R.id.item_title_sub_tv);
        }
    }


    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public interface OnClickListener {
        void click(View view, SubMessage bean, int position);
    }

    public interface OnLongClickListener {
        void longClick(View view, SubMessage bean, int position);
    }
}
