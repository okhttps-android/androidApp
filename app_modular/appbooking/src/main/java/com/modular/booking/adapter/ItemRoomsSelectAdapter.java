package com.modular.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.utils.helper.AvatarHelper;
import com.modular.booking.R;
import com.modular.booking.model.SBMenuModel;

import java.util.ArrayList;
import java.util.List;

public class ItemRoomsSelectAdapter extends BaseAdapter {

    private List<SBMenuModel> objects = new ArrayList<SBMenuModel>();

    private Context context;
    private LayoutInflater layoutInflater;
    private OnImageClickListener mOnImageClickListener;
    private OnBookClickListener mOnBookClickListener;

    public void setmOnImageClickListener(OnImageClickListener mOnImageClickListener) {
        this.mOnImageClickListener = mOnImageClickListener;
    }

    public void setmOnBookClickListener(OnBookClickListener mOnBookClickListener) {
        this.mOnBookClickListener = mOnBookClickListener;
    }

    public ItemRoomsSelectAdapter(Context context, List<SBMenuModel> data) {
        this.context = context;
        this.objects = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public SBMenuModel getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_rooms_select, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((SBMenuModel) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(SBMenuModel object, ViewHolder holder, final int position) {
        if (StringUtil.isEmpty(object.getUrl())) {
            AvatarHelper.getInstance().display(object.getCode(), holder.ivItem, true, false);
        } else {
            AvatarHelper.getInstance().display(object.getUrl(), holder.ivItem, true);
        }
        holder.tvName.setText(object.getTitle());
        holder.model = object;
        if (object.isBooking()) {
            holder.tvBookAction.setText("已预定");
            holder.tvBookAction.setBackgroundResource(R.drawable.bg_bule_btn);
            holder.tvBookAction.setSelected(true);
        } else {
            holder.tvBookAction.setBackgroundResource(R.drawable.bg_bule_btn);
            holder.tvBookAction.setText("预定");
            holder.tvBookAction.setSelected(false);
        }

        holder.ivItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnImageClickListener != null) {
                    mOnImageClickListener.onImageClick(v, position);
                }
            }
        });

        holder.tvBookAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBookClickListener != null) {
                    mOnBookClickListener.onBookClick(v, position);
                }
            }
        });

    }

    public class ViewHolder {
        public RelativeLayout rlImage;
        public ImageView ivItem;
        private TextView tvName;
        public TextView tvBookAction;
        public SBMenuModel model;

        public ViewHolder(View view) {
            rlImage = (RelativeLayout) view.findViewById(R.id.rlImage);
            tvName = (TextView) view.findViewById(R.id.tvName);
            ivItem = (ImageView) view.findViewById(R.id.ivItem);
            tvBookAction = (TextView) view.findViewById(R.id.tvBookAction);

        }
    }

    public interface OnImageClickListener {
        void onImageClick(View view, int position);
    }

    public interface OnBookClickListener {
        void onBookClick(View view, int position);
    }
}
