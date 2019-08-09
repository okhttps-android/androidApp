package com.xzjmyk.pm.activity.ui.erp.adapter.oa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.oa.OAModel;
import com.common.data.ListUtils;
import com.xzjmyk.pm.activity.util.oa.OAHttpUtil;

import java.util.List;

/**
 * Created by Bitliker on 2017/4/11.
 */

public class OAAdapter extends BaseAdapter {
    private Context ct;
    private List<OAModel> models;
    private OAHttpUtil util = null;

    public OAAdapter(Context ct, OAHttpUtil util, List<OAModel> models) {
        this.ct = ct;
        this.util = util;
        this.models = models;
    }

    public void setModels(List<OAModel> models) {
        this.models = models;

    }

    public List<OAModel> getModels() {
        return models;
    }

    @Override
    public int getCount() {
        return ListUtils.isEmpty(models) ? 0 : models.size();
    }

    @Override
    public Object getItem(int position) {
        return models.get(position);
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
            convertView = LayoutInflater.from(ct).inflate(R.layout.item_oa_list, null);
            holder.status_img = (ImageView) convertView.findViewById(R.id.status_img);
            holder.title_tv = (TextView) convertView.findViewById(R.id.title_tv);
            holder.status_tv = (TextView) convertView.findViewById(R.id.status_tv);
            holder.address_tv = (TextView) convertView.findViewById(R.id.address_tv);
            holder.handler_tv = (TextView) convertView.findViewById(R.id.handler_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            bindView(position, holder);
        } catch (Exception e) {
        }
        return convertView;
    }

    private void bindView(int position, ViewHolder holder) throws Exception {
        OAModel model = models.get(position);
        holder.title_tv.setText(model.getTitle2Remark());
        holder.address_tv.setText(model.getAddress2Time());
        holder.handler_tv.setVisibility(showHandlerAble(model) ? View.VISIBLE : View.GONE);
        //当前有三个类型，任务日程（status_tv） 拜访报告（status_img） 外勤计划（status_img）
        String status = model.getStatus();
        String handler = model.isMe() ? "" : model.getHandler();
        holder.status_img.setVisibility(model.isMission() ? View.VISIBLE : View.GONE);
        if (model.isTask()) {
            holder.status_tv.setText(status);
        } else if (model.isMission()) {
            int recId = -1;
            handler += model.getTime2Str("HH:mm");
            if (util.isMissionOk(model))
                recId = R.drawable.mission_plan_ok;
            else recId = R.drawable.mission_plan_lose;
            if (recId != -1) holder.status_img.setImageResource(recId);
            else holder.status_img.setVisibility(View.GONE);
        } else if (model.isVisitRecord()) {
            handler += model.getTime2Str("HH:mm");
            holder.status_tv.setText(R.string.visited);
        }
        holder.handler_tv.setText(handler);
        if (model.isMission())
            holder.status_tv.setText("");
    }


    class ViewHolder {
        ImageView status_img;
        TextView title_tv, status_tv, address_tv, handler_tv;
    }

    /**
     * 1.我的：1.外勤或是拜访 有开始时间和结束时间
     * 2.下属：1.有执行人  2.外勤或是拜访 有开始时间和结束时间
     *
     * @param model
     * @return
     */
    private boolean showHandlerAble(OAModel model) {
        if (model.isMe()) {
            if (!model.isTask() && (model.getStartdate() > 0 || model.getEnddate() > 0))
                return true;
            return false;
        } else {
            if (!StringUtil.isEmpty(model.getHandler()) || (!model.isTask() && (model.getStartdate() > 0 || model.getEnddate() > 0)))
                return true;
            else return false;
        }
    }

}
