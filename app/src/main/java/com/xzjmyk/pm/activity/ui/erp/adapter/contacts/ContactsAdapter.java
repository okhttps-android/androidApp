package com.xzjmyk.pm.activity.ui.erp.adapter.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.utils.helper.AvatarHelper;
import com.uas.appcontact.model.contacts.ContactsModel;

import java.util.ArrayList;
import java.util.List;

import static com.xzjmyk.pm.activity.R.id.status_tv;

/**
 * Created by Arison on 2017/7/18.
 */

public class ContactsAdapter extends BaseAdapter {
    
    private Context ct;
    private ResultItemsInface resultItemsInface;
    private List<ContactsModel> models=new ArrayList<>();
    
    public ContactsAdapter(Context ct, List<ContactsModel> datas){
        this.ct=ct;
        this.models=datas;
        this.resultItemsInface=(ResultItemsInface) ct;
    }
    
    @Override
    public int getCount() {
        return models.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView==null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(MyApplication.getInstance()).
                    inflate(R.layout.item_contact_layout, parent, false);
            holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) convertView.findViewById(R.id.sub_tv);
            holder.status_tv = (TextView) convertView.findViewById(status_tv);
            holder.head_img = (ImageView) convertView.findViewById(R.id.head_img);
            holder.tag_view = convertView.findViewById(R.id.tag_view);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final ContactsModel model=models.get(position);
        holder.targetId=  model.getImid();
        holder.model=model;
///设定为每次刷新都会去删除缓存重新获取数据
        AvatarHelper.getInstance().display(model.getImid() + "", holder.head_img, true, false);
        holder.name_tv.setText(model.getName());
        holder.sub_tv.setText(model.getPhone());
        int bgRid=0;
        int status=0;
        int textColor=0;
        switch (model.getType()){
            case 0://默认不展示
                holder.status_tv.setVisibility(View.GONE);
                status = R.string.invite;
                bgRid = R.drawable.bg_green_button;
                textColor = R.color.white;
                break;
            case 1://UU好友
                holder.status_tv.setVisibility(View.VISIBLE);
                status = R.string.added;
                textColor = R.color.text_hine;
                bgRid = 0;
                break;
            case 2://非UU好友
                holder.status_tv.setVisibility(View.VISIBLE);
                status = R.string.add;
                bgRid = R.drawable.bg_blue_button;
                textColor = R.color.white;
                break;
            case 3://邀请注册  imid为0的数据
                holder.status_tv.setVisibility(View.VISIBLE);
                status = R.string.invite;
                bgRid = R.drawable.bg_green_button;
                textColor = R.color.white;
                break;
            
        }

        if (bgRid != 0) {
            holder.status_tv.setBackgroundResource(bgRid);
        } else {
            holder.status_tv.setBackgroundResource(0);
        }
        if (status != 0){
            holder.status_tv.setText(status);
        }
        if (textColor != 0) {
            holder.status_tv.setTextColor(MyApplication.getInstance().getResources().getColor(textColor));
        }
        holder.status_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if ( resultItemsInface!=null){
                  resultItemsInface.onResultForItems(v,model,position);
              }
            }
        });
        return convertView;
    }

   public class ViewHolder {
       public     TextView 
                  name_tv,
                  sub_tv,
                  status_tv;
       public ImageView head_img;
       public View tag_view;
       public String targetId;
       public ContactsModel model;
    }
    
    public interface ResultItemsInface{
         void onResultForItems(View view,ContactsModel model,int position);
    }
}
