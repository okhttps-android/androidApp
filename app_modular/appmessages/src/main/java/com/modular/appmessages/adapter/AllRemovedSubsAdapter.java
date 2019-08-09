package com.modular.appmessages.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.model.SubscriptionNumber;
import com.core.widget.MyListView;
import com.modular.appmessages.R;
import com.modular.appmessages.model.AllSubscriptonKindMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 被隐藏的所有订阅号的适配器
 * Created by RaoMeng on 2016/10/31.
 */
public class AllRemovedSubsAdapter extends BaseAdapter {
    private List<AllSubscriptonKindMessage> mAllSubscriptonKindMessages;
    private Context mContext;
    private List<SubscriptionNumber> mSubscriptionNumbers;
    private AllRemovedSubsItemAdapter mAllRemovedSubsItemAdapter;
    public AllRemovedSubsAdapter(List<AllSubscriptonKindMessage> mAllSubscriptonKindMessages, Context mContext) {
        this.mAllSubscriptonKindMessages = mAllSubscriptonKindMessages;
        this.mContext = mContext;

        mSubscriptionNumbers = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mAllSubscriptonKindMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mAllSubscriptonKindMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = View.inflate(mContext, R.layout.layout_list_manage_all_subs,null);
            viewHolder = new ViewHolder();
            viewHolder.subsListView = (MyListView) convertView.findViewById(R.id.manage_all_subs_list_mlv);
            viewHolder.typeTextView = (TextView) convertView.findViewById(R.id.manage_all_subs_list_type_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.typeTextView.setText(mAllSubscriptonKindMessages.get(position).getSubscriptionKind());
        mSubscriptionNumbers = mAllSubscriptonKindMessages.get(position).getSubscriptionNumbers();
        mAllRemovedSubsItemAdapter = new AllRemovedSubsItemAdapter(mSubscriptionNumbers,mContext,mHandler);
        viewHolder.subsListView.setAdapter(mAllRemovedSubsItemAdapter);
        return convertView;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 111){
                String key = (String) msg.obj;
                for (int i = 0; i < mAllSubscriptonKindMessages.size(); i++) {
                    if (mAllSubscriptonKindMessages.get(i).getSubscriptionKind().equals(key)){
                        mAllSubscriptonKindMessages.remove(i);
                        notifyDataSetChanged();
                    }
                }
            }
        }
    };

    class ViewHolder{
        TextView typeTextView;
        MyListView subsListView;
    }
}
