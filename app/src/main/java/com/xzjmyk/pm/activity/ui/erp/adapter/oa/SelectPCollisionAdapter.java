package com.xzjmyk.pm.activity.ui.erp.adapter.oa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.sortlist.BaseSortModel;
import com.core.model.SelectEmUser;
import com.xzjmyk.pm.activity.R;

import java.util.List;

import static com.xzjmyk.pm.activity.R.id.status_tv;

/**
 * Created by Bitliker on 2017/2/14.
 */

public class SelectPCollisionAdapter extends BaseAdapter {
    private List<BaseSortModel<SelectEmUser>> listData;
    private BaseActivity act;
    private int type=0;

    public SelectPCollisionAdapter(List<BaseSortModel<SelectEmUser>> listData, OnStatusClickListener onStatusClickListener) {
        this.onStatusClickListener = onStatusClickListener;
        this.listData = listData;
    }

    public List<BaseSortModel<SelectEmUser>> getListData() {
        return listData;
    }

    public void setListData(List<BaseSortModel<SelectEmUser>> listData) {
        this.listData = listData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getCount() {
        return ListUtils.isEmpty(listData) ? 0 : listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(MyApplication.getInstance()).
                    inflate(R.layout.item_select_p_active, parent, false);
            holder.tag_tv = (TextView) convertView.findViewById(R.id.tag_tv);
            holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) convertView.findViewById(R.id.sub_tv);
            holder.status_tv = (TextView) convertView.findViewById(status_tv);
            holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
            holder.head_img = (ImageView) convertView.findViewById(R.id.head_img);
            holder.tag_view = convertView.findViewById(R.id.tag_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        onBindViewHolder(holder, position);
        return convertView;

    }

    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final BaseSortModel<SelectEmUser> model = listData.get(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        int section = getSectionForPosition(position);
        if (position == getPositionForSection(section)) {
            holder.tag_tv.setVisibility(View.VISIBLE);
            holder.tag_view.setVisibility(View.VISIBLE);
            holder.tag_tv.setText(model.getFirstLetter());
        } else {
            holder.tag_tv.setVisibility(View.GONE);
            holder.tag_view.setVisibility(View.GONE);
        }
        final SelectEmUser user = model.getBean();
        holder.cb.setChecked(model.isClick());
        AvatarHelper.getInstance().display(user.getImId() + "", holder.head_img, true, false);//设定为每次刷新都会去删除缓存重新获取数据
        holder.name_tv.setText(user.getEmName());
        holder.sub_tv.setText((StringUtil.isEmpty(user.getDepart()) ? "" : (user.getDepart() + ">"))
                + " " + (StringUtil.isEmpty(user.getPosition()) ? "" : user.getPosition()));
        int bgRid;
        int status;
        int textColor;
        if (user.getImId() == 0) {//邀请注册
            status = R.string.invite;
            bgRid = R.drawable.bg_green_button;
            textColor = R.color.white;
        } else if (StringUtil.isEmpty(user.getTag()) || "0".equals(user.getTag())) {//添加好友
            status = R.string.add;
            bgRid = R.drawable.bg_blue_button;
            textColor = R.color.white;
        } else {
            status = R.string.added;
            textColor = R.color.text_hine;
            bgRid = 0;
        }
        if (bgRid != 0) {
            holder.status_tv.setBackgroundResource(bgRid);
        } else {
            holder.status_tv.setBackgroundResource(0);
        }
        if (status != 0)
            holder.status_tv.setText(status);
        if (textColor != 0)
            holder.status_tv.setTextColor(MyApplication.getInstance().getResources().getColor(textColor));

        holder.status_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof TextView) {
                    String message = ((TextView) v).getText().toString();
                    LogUtil.i("message=" + message);
                    LogUtil.i("position=" + position);
                    LogUtil.i("user=" + user.getEmName());
                    if (onStatusClickListener != null && !StringUtil.isEmpty(message) && !message.equals(MyApplication.getInstance().getString(R.string.added))) {
                        onStatusClickListener.onClick(user, position, message);
                    }
                }
            }
        });
        holder.cb.setFocusable(false);
        holder.cb.setClickable(false);
        if (type==1){
            holder.status_tv.setVisibility(View.GONE);
        }
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < listData.size(); i++) {
            String sortStr = listData.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return listData.get(position).getFirstLetter().charAt(0);
    }


    class ViewHolder {
        TextView tag_tv,
                name_tv,
                sub_tv,
                status_tv;
        CheckBox cb;
        ImageView head_img;
        View tag_view;
    }

    private OnStatusClickListener onStatusClickListener;

    public void setOnStatusClickListener(OnStatusClickListener onStatusClickListener) {
        this.onStatusClickListener = onStatusClickListener;
    }

    public interface OnStatusClickListener {
        void onClick(SelectEmUser user, int position, String message);
    }
}
