package com.uas.appworks.crm3_0.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.core.utils.sortlist.BaseSortModel;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.model.ContactsBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


public class ContactSortAdapter extends BaseAdapter implements SectionIndexer, StickyListHeadersAdapter {

    private BaseActivity mContext;
    private List<BaseSortModel<ContactsBean>> mSortFriends;
    private boolean isRefeshed;
    private Map<Integer, String> isLog;
    
    private FrameLayout frameLayout;

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public void setFrameLayout(FrameLayout frameLayout) {
        this.frameLayout = frameLayout;
    }

    public ContactSortAdapter(BaseActivity context, List<BaseSortModel<ContactsBean>> sortFriends) {
        mContext = context;
        mSortFriends = (sortFriends == null ? new ArrayList<BaseSortModel<ContactsBean>>() : sortFriends);
        isLog = new HashMap<>();
    }

    public void setData(List<BaseSortModel<ContactsBean>> sortFriends) {
        this.mSortFriends = (sortFriends == null ? new ArrayList<BaseSortModel<ContactsBean>>() : sortFriends);
        notifyDataSetChanged();
    }

    public List<BaseSortModel<ContactsBean>> getmSortFriends() {
        return mSortFriends;
    }

    public void setisRefeshed(boolean isRefeshed) {
        this.isRefeshed = isRefeshed;
        isLog.clear();
    }

    @Override
    public int getCount() {
        return mSortFriends.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        if (convertView == null) {
            holder = new ViewHolder();
         
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_contacts_me, parent, false);
            holder. tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.  tvPosition = (TextView)  convertView.findViewById(R.id.tv_position);
            holder.  tvCompanyName = (TextView)  convertView.findViewById(R.id.tv_company_name);
            holder.  tvPhone = (TextView)  convertView.findViewById(R.id.tv_phone);
            holder.  ivIcon = (ImageView)  convertView.findViewById(R.id.iv_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
//        if (position == getPositionForSection(section)) {
//            holder.tag_tv.setVisibility(View.VISIBLE);
//            holder.tag_tv.setText(mSortFriends.get(position).getFirstLetter());
//        } else {
//            holder.tag_tv.setVisibility(View.GONE);
//        }
        final ContactsBean object= mSortFriends.get(position).getBean();
        holder.tvName.setText(object.getName());
        holder.tvCompanyName.setText(object.getCompanyName());
        if (!StringUtil.isEmpty(object.getPhone())){
            holder.tvPhone.setText(object.getPhone().split("/")[0]);
        }
        holder.tvPosition.setText(object.getPosition());
        holder.bean=object;

        if (!StringUtil.isEmpty(object.getPhone())){
            final String phone=object.getPhone().split("/")[0];
            holder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!StringUtil.isEmpty(phone)) {
                        String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                        Pattern regex = Pattern.compile(check);
                        Matcher matcher = regex.matcher(phone);
                        if (matcher.matches()) {
                            if (mContext instanceof Activity){
                                SystemUtil.phoneAction(mContext, phone);
                            }else{
                                SystemUtil.phoneAction(mContext, phone);
                            }

                        } else {
                           // NotifyUtils.ToastMessage(mContext,mContext.getString(com.uas.appworks.R.string.not_format_phone));
                            ToastUtil.showToast(MyApplication.getInstance(),mContext.getString(com.uas.appworks.R.string.not_format_phone));
                        }
                    } else {
                        ToastUtil.showToast(MyApplication.getInstance(),mContext.getString(com.uas.appworks.R.string.not_phone));
                       // NotifyUtils.ToastMessage(mContext,mContext.getString(com.uas.appworks.R.string.not_phone));
                    }
                }
            });
            holder.tvPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!StringUtil.isEmpty(phone)) {
                        String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                        Pattern regex = Pattern.compile(check);
                        Matcher matcher = regex.matcher(phone);
                        if (matcher.matches()) {
                            if (mContext instanceof Activity){
                                SystemUtil.phoneAction(mContext, phone);
                            }
                        } else {
                            ToastUtil.showToast(MyApplication.getInstance(),MyApplication.getInstance().getString(R.string.not_format_phone));
                        }
                    } else {
                        ToastUtil.showToast(MyApplication.getInstance(),MyApplication.getInstance().getString(R.string.not_phone));
                    }
                }
            });
        }
   
    
        return convertView;
    }


    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mSortFriends.get(position).getFirstLetter().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mSortFriends.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_head, parent, false);
            viewHolder = new HeaderViewHolder();
            viewHolder.cityLetterTextView = (TextView) convertView.findViewById(R.id.head);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (HeaderViewHolder) convertView.getTag();
        }
        viewHolder.cityLetterTextView.setText(mSortFriends.get(position).getFirstLetter());

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return mSortFriends.get(position).getFirstLetter().charAt(0);
    }

    class HeaderViewHolder {
        TextView cityLetterTextView;
    }

    public class ViewHolder {
        private TextView tvName;
        private TextView tvPosition;
        private TextView tvCompanyName;
        private TextView tvPhone;
        private ImageView ivIcon;
        public ContactsBean bean;
    }
}
