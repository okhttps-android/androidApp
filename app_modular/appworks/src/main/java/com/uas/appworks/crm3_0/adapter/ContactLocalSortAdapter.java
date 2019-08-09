package com.uas.appworks.crm3_0.adapter;

import android.content.Context;
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
import com.core.utils.ToastUtil;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


public class ContactLocalSortAdapter extends BaseAdapter implements SectionIndexer, StickyListHeadersAdapter {

    private Context mContext;
    private List<BaseSortModel<ContactsModel>> mSortFriends;
    private boolean isRefeshed;
    private Map<Integer, String> isLog;

    private FrameLayout frameLayout;

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public void setFrameLayout(FrameLayout frameLayout) {
        this.frameLayout = frameLayout;
    }

    public ContactLocalSortAdapter(Context context, List<BaseSortModel<ContactsModel>> sortFriends) {
        mContext = context;
        mSortFriends = (sortFriends == null ? new ArrayList<BaseSortModel<ContactsModel>>() : sortFriends);
        isLog = new HashMap<>();
    }

    public void setData(List<BaseSortModel<ContactsModel>> sortFriends) {
        this.mSortFriends = (sortFriends == null ? new ArrayList<BaseSortModel<ContactsModel>>() : sortFriends);
        notifyDataSetChanged();
    }

    public List<BaseSortModel<ContactsModel>> getmSortFriends() {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_item_contacts_local, parent, false);

            holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) convertView.findViewById(R.id.sub_tv);
            holder.status_tv = (ImageView) convertView.findViewById(R.id.status_tv);
            holder.head_img =  convertView.findViewById(R.id.head_img);
            holder.tag_view = convertView.findViewById(R.id.tag_view);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ContactsModel  model= mSortFriends.get(position).getBean();

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
                if (!StringUtil.isEmpty(model.getPhone())) {
                    String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                    Pattern regex = Pattern.compile(check);
                    Matcher matcher = regex.matcher(model.getPhone());
                    if (matcher.matches()) {
                        SystemUtil.phoneAction(mContext, model.getPhone());
                    } else {
                      //  NotifyUtils.ToastMessage(mContext,mContext.getString(R.string.not_format_phone));
                        ToastUtil.showToast(MyApplication.getInstance(),mContext.getString(R.string.not_format_phone));
                    }
                } else {
                   // NotifyUtils.ToastMessage(mContext,mContext.getString(R.string.not_phone));
                    ToastUtil.showToast(MyApplication.getInstance(),mContext.getString(R.string.not_format_phone));
                }
            }
        });
        
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
        public  TextView
                name_tv,
                sub_tv;
        public ImageView status_tv;
        public TextView head_img;
        public View tag_view;
        public String targetId;
        public ContactsModel model;
    }
}
