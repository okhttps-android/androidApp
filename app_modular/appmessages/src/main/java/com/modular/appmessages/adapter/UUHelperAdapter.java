package com.modular.appmessages.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.core.dao.UUHelperDao;
import com.core.model.UUHelperModel;
import com.core.utils.IntentUtils;
import com.modular.appmessages.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;


/**
 * Created by Bitliker on 2017/9/7.
 */

public class UUHelperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context ct;
    private List<UUHelperModel> models;
    private LayoutInflater inflater;

    public UUHelperAdapter(Context ct, List<UUHelperModel> models) {
        this.ct = ct;
        this.models = models;
    }

    public List<UUHelperModel> getModels() {
        return models;
    }

    public void setModels(List<UUHelperModel> models) {
        this.models = models;
    }

    public void insertStart(List<UUHelperModel> startModels) {
        if (startModels == null || startModels.size() <= 0) return;
        if (models == null || models.size() <= 0) {
            setModels(startModels);
            notifyDataSetChanged();
        } else {
            models.addAll(0, startModels);
            notifyItemRangeInserted(0, startModels.size());
        }
    }

    public void insertEnd(List<UUHelperModel> startModels) {
        if (startModels == null || startModels.size() <= 0) return;
        if (models == null || models.size() <= 0) {
            setModels(startModels);
            notifyDataSetChanged();
        } else {
            int startItem = models.size();
            models.addAll(startModels);
            notifyItemRangeInserted(startItem, models.size());
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        models.get(viewType).getType();
        return new SingleHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UUHelperModel model = models.get(position);
        if (holder instanceof SingleHolder) {
            bindSingleHolder((SingleHolder) holder, model, position);
        } else if (holder instanceof TimeHolder) {
            ((TimeHolder) holder).timeTV.setText(model.getDate());
        } else if (holder instanceof ViewHolder) {
            bindViewHolder((ViewHolder) holder, model, position);
        }
    }

    private void bindSingleHolder(SingleHolder holder, final UUHelperModel model, final int position) {
        holder.timeTV.setText(model.getDate());
        holder.titleTV.setText(model.getTitle());
        holder.contentTV.setText(model.getContent());
        ImageLoader.getInstance().displayImage(model.getImageUrl(), holder.imageIV);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(model);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongItemClick(model, position);
                return false;
            }
        });
    }

    private void bindViewHolder(ViewHolder holder, final UUHelperModel model, final int position) {
        if (position > 0) {
            if (models.get(position - 1).isTag()) {//上一个是时间
                holder.fristRL.setVisibility(View.VISIBLE);
                holder.normalRL.setVisibility(View.GONE);
                ImageLoader.getInstance().displayImage(model.getImageUrl(), holder.fristIV);
                holder.fristTV.setText(model.getContent());
            } else {//上一个不是时间
                holder.fristRL.setVisibility(View.GONE);
                holder.normalRL.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(model.getIconUrl(), holder.normalIV);
                holder.normalTV.setText(model.getContent());
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(model);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongItemClick(model, position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(models);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout fristRL, normalRL;
        ImageView fristIV, normalIV;
        TextView fristTV, normalTV;
        View line;

        public ViewHolder(ViewGroup parent) {
            this(getViewByLayout(R.layout.item_uuhelper_item, parent));
        }

        public ViewHolder(View itemView) {
            super(itemView);
            fristRL = (RelativeLayout) itemView.findViewById(R.id.fristRL);
            normalRL = (RelativeLayout) itemView.findViewById(R.id.normalRL);
            fristIV = (ImageView) itemView.findViewById(R.id.fristIV);
            normalIV = (ImageView) itemView.findViewById(R.id.normalIV);
            fristTV = (TextView) itemView.findViewById(R.id.fristTV);
            normalTV = (TextView) itemView.findViewById(R.id.normalTV);
            line = itemView.findViewById(R.id.line);
        }
    }

    class TimeHolder extends RecyclerView.ViewHolder {
        TextView timeTV;

        public TimeHolder(ViewGroup parent) {
            this(getViewByLayout(R.layout.item_uuhelper_time, parent));
        }

        public TimeHolder(View itemView) {
            super(itemView);
            timeTV = (TextView) itemView.findViewById(R.id.timeTV);
        }
    }


    class SingleHolder extends RecyclerView.ViewHolder {
        TextView contentTV, titleTV, timeTV;
        ImageView imageIV;

        public SingleHolder(ViewGroup parent) {
            this(getViewByLayout(R.layout.item_uuhelper_single, parent));
        }

        public SingleHolder(View itemView) {
            super(itemView);
            imageIV = (ImageView) itemView.findViewById(R.id.imageIV);
            titleTV = (TextView) itemView.findViewById(R.id.titleTV);
            contentTV = (TextView) itemView.findViewById(R.id.contentTV);
            timeTV = (TextView) itemView.findViewById(R.id.timeTV);
        }
    }

    private View getViewByLayout(int layout, ViewGroup parent) {
        if (inflater == null) {
            inflater = LayoutInflater.from(ct);
        }
        return inflater.inflate(layout, parent, false);
    }

    private void onItemClick(UUHelperModel model) {
        UUHelperDao.getInstance().updateRead(model.getId());
        String url = model.getLinkUrl();
        IntentUtils.linkCommonWeb(ct, url, "UU 助手", model.getImageUrl(), model.getContent());
    }

    private void onLongItemClick(final UUHelperModel model, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ct);
        builder.setTitle(R.string.prompt_title).setMessage("确定删除" + model.getContent() + "吗?").setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UUHelperDao.getInstance().deleteData(model.getId());
                models.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, models.size());
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }
}
