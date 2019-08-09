package com.uas.appworks.CRM.erp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.common.LogUtil;
import com.uas.appworks.CRM.erp.model.CycleCount;
import com.uas.appworks.R;

import java.util.List;

/**
 * Created by Bitlike on 2017/12/19.
 */

public class CycleCountAdapter extends BaseAdapter {
    private Context context;
    private boolean needShowAll;
    private List<CycleCount> models;

    public void setNeedShowAll(boolean needShowAll) {
        this.needShowAll = needShowAll;
    }

    public CycleCountAdapter(Context context, List<CycleCount> models) {
        this.context = context;
        this.models = models;
    }

    public void setModels(List<CycleCount> models) {
        this.models = models;
        notifyDataSetChanged();
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        CycleCount model = models.get(i);
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_rl_listview, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        List<CycleCount.Data> datas = null;
        if (model.isExpand()) {
            holder.expandImg.setImageResource(R.drawable.ic_menu_spread);
            datas = model.getDatas();
        } else {
            holder.expandImg.setImageResource(R.drawable.ic_menu_retract);
            datas = model.getLowDatas();
        }
        ListAdapter mAdapter = holder.mListView.getAdapter();
        if (mAdapter != null && mAdapter instanceof DataAdapter) {
            ((DataAdapter) mAdapter).setModels(datas);
        } else {
            holder.mListView.setAdapter(new DataAdapter(context, datas));
        }
        view.setTag(R.id.tag_key2, model);
        view.setOnClickListener(onClickListener);
        if (needShowAll) {
            holder.expandImg.setVisibility(View.VISIBLE);
            holder.expandImg.setTag(R.id.tag_key, i);
            holder.expandImg.setOnClickListener(onClickListener);
        } else {
            holder.expandImg.setVisibility(View.GONE);
        }
        return view;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                LogUtil.i("进来点击事件");
                if (view.getId() == R.id.expandImg) {
                    LogUtil.i("进来拓展点击事件");
                    int i = (int) view.getTag(R.id.tag_key);
                    if (models != null && models.size() > i) {
                        models.get(i).setExpand(!models.get(i).isExpand());
                        notifyDataSetChanged();
                    }
                } else {
                    LogUtil.i("进来单机点击事件");
                    CycleCount model = (CycleCount) view.getTag(R.id.tag_key2);
                    if (onItemClickListener != null) {
                        onItemClickListener.click(model);
                    }
                }
            } catch (Exception e) {
                LogUtil.i("e=" + e.getMessage());

            }
        }
    };
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void click(CycleCount model);
    }

    @Override
    public int getCount() {
        return models == null ? 0 : models.size();
    }

    @Override
    public Object getItem(int i) {
        return models.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    class ViewHolder {
        ListView mListView;
        ImageView expandImg;

        public ViewHolder(View itemView) {
            mListView = itemView.findViewById(R.id.listView);
            expandImg = itemView.findViewById(R.id.expandImg);

            mListView.setEnabled(false);
            mListView.setFocusable(false);
            mListView.setClickable(false);
            mListView.setPressed(false);
        }
    }

    public static class DataAdapter extends BaseAdapter {
        private List<CycleCount.Data> models;
        private Context context;

        public void setModels(List<CycleCount.Data> models) {
            this.models = models;
            notifyDataSetChanged();
        }

        public DataAdapter(Context context, List<CycleCount.Data> models) {
            this.context = context;
            this.models = models;
        }

        @Override
        public int getCount() {
            return models == null ? 0 : models.size();
        }


        @Override
        public Object getItem(int i) {
            return models.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CycleCount.Data model = models.get(i);
            ViewHolder holder = null;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_device_data, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            try {
                if (model.isHasTwo()) {
                    holder.twoLL.setVisibility(View.VISIBLE);
                    holder.caption2Tv.setText(model.getCaption2() + ":");
                    holder.values2Tv.setText(model.getValues2());
                } else {
                    holder.twoLL.setVisibility(View.GONE);
                }
                holder.caption1Tv.setText(model.getCaption() + ":");
                holder.values1Tv.setText(model.getValues());
            } catch (Exception e) {

            }
            return view;
        }

        class ViewHolder {
            LinearLayout twoLL;
            TextView caption1Tv, values1Tv, caption2Tv, values2Tv;

            public ViewHolder(View itemView) {
                twoLL = itemView.findViewById(R.id.twoLL);
                caption1Tv = itemView.findViewById(R.id.caption1Tv);
                values1Tv = itemView.findViewById(R.id.values1Tv);
                values2Tv = itemView.findViewById(R.id.values2Tv);
                caption2Tv = itemView.findViewById(R.id.caption2Tv);
            }
        }
    }
}
