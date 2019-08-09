package com.modular.booking.adapter;

/**
 * Created by Arison on 2017/9/27.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.modular.booking.R;
import com.modular.booking.activity.services.BServiceAddActivity;
import com.modular.booking.activity.services.BServiceDetailActivity;
import com.modular.booking.model.SBListModel;

import java.util.ArrayList;
import java.util.List;

/**
  * @desc:预约列表
  * @author：Arison on 2017/9/27
  */
public class ItemBserviceListAdapter extends BaseAdapter {

    private List<SBListModel> objects = new ArrayList<SBListModel>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemBserviceListAdapter(Context context,List<SBListModel> objects) {
        this.context = context;
        this.objects=objects;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public List<SBListModel> getObjects() {
        return objects;
    }

    public void setObjects(List<SBListModel> objects) {
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public SBListModel getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_bservice_list, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((SBListModel)getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(final SBListModel object, ViewHolder holder) {
        AvatarHelper.getInstance().display(object.getUrl(),holder.ivIcon,false);
        holder.tvTitle.setText(object.getName());
        holder.tvType.setText(object.getType());
        holder.tvDistance.setText(object.getDistance());
        holder.tvCash.setText(object.getCash());
        holder.tvCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.phoneAction(context,object.getPhone());
            }
        });
        holder.model=object;
        holder.btBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SBListModel model = object;
                Intent intent =null;
                if (model.getType().equals("会所")||model.getType().equals("美容美发")){
                    intent = new Intent(context, BServiceDetailActivity.class);
                }else{
                    intent = new Intent(context, BServiceAddActivity.class);
                }
                intent.putExtra("model", model);
                context.startActivity(intent);
            }
        });
    }

    public class ViewHolder {
        private SBListModel model;
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvType;
        private TextView tvStart;
        private TextView tvDistance;
        public TextView tvCash;
        private Button btBook;

        public ViewHolder(View view) {
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvType = (TextView) view.findViewById(R.id.tvType);
           // tvStart = (TextView) view.findViewById(R.id.tvStart);
            tvDistance = (TextView) view.findViewById(R.id.tvDistance);
            tvCash = (TextView) view.findViewById(R.id.tvCash);
            btBook = (Button) view.findViewById(R.id.btBook);
        }
    }
}
