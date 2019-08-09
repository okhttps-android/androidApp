package com.modular.booking.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.core.utils.helper.AvatarHelper;
import com.modular.booking.R;
import com.modular.booking.activity.services.BServiceAddActivity;
import com.modular.booking.model.SBListModel;
import com.modular.booking.model.SBStoremanModel;

import java.util.ArrayList;
import java.util.List;

public class ItemBserviceStoremanListAdapter extends BaseAdapter {

    private List<SBStoremanModel> objects = new ArrayList<SBStoremanModel>();

    private Context context;
    private LayoutInflater layoutInflater;
    private SBListModel model;

    public ItemBserviceStoremanListAdapter(Context context, List<SBStoremanModel> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }
   
    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public SBStoremanModel getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public SBListModel getModel() {
        return model;
    }

    public void setModel(SBListModel model) {
        this.model = model;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_bservice_storeman_list, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews( getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(final SBStoremanModel object, ViewHolder holder) {
        holder.bserviceStoremanTitleTv.setText(object.getSm_username());
        AvatarHelper.getInstance().displayAvatar(object.getSm_userid(),  holder.bserviceStoremanIv, true);
        holder.model=object;
        holder.bserviceStoremanOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (context!=null) {
                  SBStoremanModel bean =object;
                  Intent intent = new Intent(context, BServiceAddActivity.class);
                  intent.putExtra("model", model);
                  intent.putExtra("sb_userid", bean.getSm_userid());
                  intent.putExtra("sb_username", bean.getSm_username());
                  context.startActivity(intent);
              }
            }
        });
    }

    protected class ViewHolder {
        private SBStoremanModel model;
        private ImageView bserviceStoremanIv;
        private TextView bserviceStoremanTitleTv;
        private TextView tvType;
        private RatingBar bserviceStoremanRateRb;
        private TextView bserviceStoremanPreferentialTv;
        private Button bserviceStoremanOrderBtn;

        public ViewHolder(View view) {
            bserviceStoremanIv = (ImageView) view.findViewById(R.id.bservice_storeman_iv);
            bserviceStoremanTitleTv = (TextView) view.findViewById(R.id.bservice_storeman_title_tv);
            tvType = (TextView) view.findViewById(R.id.tvType);
            bserviceStoremanRateRb = (RatingBar) view.findViewById(R.id.bservice_storeman_rate_rb);
            bserviceStoremanPreferentialTv = (TextView) view.findViewById(R.id.bservice_storeman_preferential_tv);
            bserviceStoremanOrderBtn = (Button) view.findViewById(R.id.bservice_storeman_order_btn);
        }
    }
}
