package com.modular.appmessages.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.preferences.SharedUtil;
import com.core.dao.DBManager;
import com.core.model.SubscriptionNumber;
import com.core.utils.CommonUtil;
import com.modular.appmessages.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 被移除订阅号的item适配器
 * Created by RaoMeng on 2016/10/31.
 */
public class AllRemovedSubsItemAdapter extends BaseAdapter {
    private List<SubscriptionNumber> mSubscriptionNumbers;
    private Context mContext;
    private List<Object> keyStrings;
    private List<Object> cacheKeyStrings;
    private List<String> allKeyStrings;
    private String currentMaster;//当前账套
    private String currentUser;//当前账号
    private DBManager mDbManager;
    private Handler mHandler;

    public AllRemovedSubsItemAdapter(List<SubscriptionNumber> mSubscriptionNumbers, Context mContext, Handler mHandler) {
        this.mSubscriptionNumbers = mSubscriptionNumbers;
        this.mContext = mContext;
        this.mHandler = mHandler;
        keyStrings = new ArrayList<>();
        cacheKeyStrings = new ArrayList<>();
        allKeyStrings = new ArrayList<>();
        mDbManager = new DBManager();
        currentMaster = CommonUtil.getSharedPreferences(mContext, "erp_master");
        currentUser = CommonUtil.getSharedPreferences(mContext, "erp_username");

        String cacheKeys = SharedUtil.getString(currentMaster + currentUser + "subs");
        String allCacheKeys = SharedUtil.getString(currentMaster + currentUser + "allsubs");
        if (allCacheKeys != null){
            String[] allCacheKeysArray = allCacheKeys.split(",");
            for (int i = 0; i < allCacheKeysArray.length; i++) {
                allKeyStrings.add(allCacheKeysArray[i]);
            }
        }
        if (cacheKeys != null) {
            String[] cacheKeysArray = cacheKeys.split(",");
            for (int i = 0; i < cacheKeysArray.length; i++) {
                cacheKeyStrings.add(cacheKeysArray[i]);
            }
        }
    }

    @Override
    public int getCount() {
        return mSubscriptionNumbers.size();
    }

    @Override
    public Object getItem(int position) {
        return mSubscriptionNumbers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = View.inflate(mContext, R.layout.layout_list_manage_all_subs_item,null);
            viewHolder = new ViewHolder();
            viewHolder.addTextView = (TextView) convertView.findViewById(R.id.manage_all_subs_list_item_add_tv);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.manage_all_subs_list_item_name_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameTextView.setText(mSubscriptionNumbers.get(position).getTitle());
        viewHolder.addTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubscriptionNumbers.get(position).setRemoved(0);
                mDbManager.updateAllSubs(mSubscriptionNumbers.get(position));
                cacheKeyStrings.add(mSubscriptionNumbers.get(position).getType());
                String type = mSubscriptionNumbers.get(position).getType();
                mSubscriptionNumbers.remove(position);
                notifyDataSetChanged();
                keyStrings = ListUtils.getSingleElement(cacheKeyStrings);
                initKeyStrings();

                if (mSubscriptionNumbers.size() == 0){
                    Message message = Message.obtain();
                    message.what = 111;
                    message.obj = type;
                    mHandler.sendMessage(message);
                }
            }
        });
        return convertView;
    }


    private void initKeyStrings() {
        List<String> finalKeys = new ArrayList<>();
        if (allKeyStrings.size() != 0){
            for (int i = 0; i < allKeyStrings.size(); i++) {
                String key = allKeyStrings.get(i);
                for (int j = 0; j < keyStrings.size(); j++) {
                    if (key.equals(keyStrings.get(j))){
                        finalKeys.add(keyStrings.get(j).toString());
                    }
                }
            }
        }

        StringBuilder keyStringBuilder = null;
        if (finalKeys.size() != 0) {
            keyStringBuilder = new StringBuilder();
            for (int i = 0; i < finalKeys.size(); i++) {
                keyStringBuilder.append("," + finalKeys.get(i));
            }
            if (keyStringBuilder.length() > 2) {
                keyStringBuilder.delete(0, 1);
            }
            SharedUtil.putString(currentMaster + currentUser + "subs", keyStringBuilder.toString());
        } else {
            SharedUtil.putString(currentMaster + currentUser + "subs", null);
        }

    }


    class ViewHolder{
        TextView nameTextView;
        TextView addTextView;
    }
}
