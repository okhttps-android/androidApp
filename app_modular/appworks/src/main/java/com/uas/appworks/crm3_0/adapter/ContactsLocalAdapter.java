package com.uas.appworks.crm3_0.adapter;

/**
 * Created by Arison on 2018/9/19.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.utils.NotifyUtils;
import com.core.utils.sortlist.PingYinUtil;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Arison on 2017/7/18.
 */

public class ContactsLocalAdapter extends BaseAdapter {

    private Context ct;
    private ResultItemsInface resultItemsInface;
    private boolean isSingleSelect=false;
    private List<ContactsModel> models=new ArrayList<>();

    public ContactsLocalAdapter(Context ct, List<ContactsModel> datas){
        this.ct=ct;
        this.models=datas;
        this.resultItemsInface=(ResultItemsInface) ct;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public ContactsModel getItem(int position) {
        return models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView==null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(MyApplication.getInstance()).
                    inflate(R.layout.fragment_item_contacts_local, parent, false);
            holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) convertView.findViewById(R.id.sub_tv);
            holder.status_tv = (ImageView) convertView.findViewById(R.id.status_tv);
            holder.head_img =  convertView.findViewById(R.id.head_img);
            holder.tag_view = convertView.findViewById(R.id.tag_view);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final ContactsModel model=models.get(position);
       
        holder.targetId=  model.getImid();
        holder.model=model;
        ///设定为每次刷新都会去删除缓存重新获取数据
        //AvatarHelper.getInstance().display(model.getImid() + "", holder.head_img, true, false);
        
        String pName= PingYinUtil.getPingYin(model.getName());
        holder.head_img.setText(pName.substring(0,1));
        
        holder.name_tv.setText(model.getName());
        holder.sub_tv.setText(model.getPhone());
        holder.status_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( resultItemsInface!=null){
                    resultItemsInface.onResultForItems(v,model,position);
                }

                if (!StringUtil.isEmpty(model.getPhone())) {
                    String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                    Pattern regex = Pattern.compile(check);
                    Matcher matcher = regex.matcher(model.getPhone());
                    if (matcher.matches()) {
                        SystemUtil.phoneAction(ct, model.getPhone());
                    } else {
                        NotifyUtils.ToastMessage(ct,ct.getString(R.string.not_format_phone));
                    }
                } else {
                    NotifyUtils.ToastMessage(ct,ct.getString(R.string.not_phone));
                }
            }
        });
        return convertView;
    }

    public class ViewHolder {
        public  TextView
                name_tv,
                sub_tv;
        public ImageView status_tv;
        public TextView head_img;
        public View tag_view;
        public String targetId;
        public ContactsModel model;
    }

    public interface ResultItemsInface{
        void onResultForItems(View view, ContactsModel model, int position);
    }

    public List<ContactsModel> getModels() {
        return models;
    }

    public void setModels(List<ContactsModel> models) {
        this.models = models;
    }
}
