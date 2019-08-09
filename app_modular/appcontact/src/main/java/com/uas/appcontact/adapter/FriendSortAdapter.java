package com.uas.appcontact.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.model.Friend;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.sortlist.BaseSortModel;
import com.core.widget.crouton.Crouton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appcontact.R;
import com.uas.appcontact.db.TopContactsDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


public class FriendSortAdapter extends BaseAdapter implements SectionIndexer, StickyListHeadersAdapter {

    private Context mContext;
    private List<BaseSortModel<Friend>> mSortFriends;
    private boolean isRefeshed;
    private Map<Integer, String> isLog;


    public FriendSortAdapter(Context context, List<BaseSortModel<Friend>> sortFriends) {
        mContext = context;
        mSortFriends = (sortFriends == null ? new ArrayList<BaseSortModel<Friend>>() : sortFriends);
        isLog = new HashMap<>();
    }

    public void setData(List<BaseSortModel<Friend>> sortFriends) {
        this.mSortFriends = (sortFriends == null ? new ArrayList<BaseSortModel<Friend>>() : sortFriends);
        notifyDataSetChanged();
    }

    public List<BaseSortModel<Friend>> getmSortFriends() {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, parent, false);
            holder.header_img = (ImageView) convertView.findViewById(R.id.header_img);
            holder.phone_img = (ImageView) convertView.findViewById(R.id.phone_img);
            holder.tag_tv = (TextView) convertView.findViewById(R.id.tag_tv);
            holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) convertView.findViewById(R.id.sub_tv);
            holder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);
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
        final Friend friend = mSortFriends.get(position).getBean();
        final String phone = friend.getPhone();
        if (!StringUtil.isEmpty(phone)) {
            String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(phone.toString());
            if (matcher.matches()) {
                holder.phone_img.setVisibility(View.VISIBLE);
            } else {
                holder.phone_img.setVisibility(View.GONE);
            }
        }
        //TODO 没有控制好，先屏蔽
        if (friend.getTimeSend() != 0) holder.time_tv.setText("");
        else holder.time_tv.setText("");
        /*头像*/
        if (friend.getRoomFlag() == 0) {// 这是单个人
            if (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {// 系统消息的头像
//                holder.header_img.setImageResource(R.drawable.im_notice);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.im_notice, holder.header_img, MyApplication.mAvatarRoundImageOptions);
            } else if (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {// 新朋友的头像
//                holder.header_img.setImageResource(R.drawable.im_new_friends);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.im_new_friends, holder.header_img, MyApplication.mAvatarRoundImageOptions);
            } else if (Integer.valueOf(friend.getUserId()) == 0) {
//                holder.header_img.setImageResource(R.drawable.avatar_normal);
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.avatar_normal, holder.header_img, MyApplication.mAvatarRoundImageOptions);
            } else {// 其他
                if (isRefeshed) {
                    AvatarHelper.getInstance().display(friend.getUserId(), holder.header_img, true, true);//设定为每次刷新都会去删除缓存重新获取数据
                    isRefeshed = false;
                } else {
                    AvatarHelper.getInstance().display(friend.getUserId(), holder.header_img, true, false);//不会删除缓存
                }
            }
        } else {// 这是1个房间
            if (TextUtils.isEmpty(friend.getRoomCreateUserId())) {
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.avatar_normal, holder.header_img, MyApplication.mAvatarRoundImageOptions);
            } else {
                AvatarHelper.getInstance().displayAvatar(friend.getRoomCreateUserId(), holder.header_img, true);// 目前在备注名放房间的创建者Id
            }
        }
        /*昵称*/
        String name = friend.getShowName();
        holder.name_tv.setText(name);
        /*个性签名*/
        holder.sub_tv.setText(TextUtils.isEmpty(friend.getPhone()) ? "" : friend.getPhone());
        final View finalConvertView = convertView;
        holder.phone_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!StringUtil.isEmpty(phone)) {
                    String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                    Pattern regex = Pattern.compile(check);
                    Matcher matcher = regex.matcher(phone.toString());
                    if (matcher.matches()) {
//                        selectByPhone(phone, finalConvertView);
                        SystemUtil.phoneAction(mContext, phone);
                        LogUtil.i("通讯录界面 拨打电话\n" + JSON.toJSONString(friend));
                        if (friend != null) {
                            friend.setPhone(phone);
                        }
                        TopContactsDao.api().addGoodFriend(friend);
                    } else {
                        Crouton.makeText(mContext, R.string.not_format_phone);
                    }
                } else {
                    Crouton.makeText(mContext, R.string.not_phone);
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

    class ViewHolder {
        TextView tag_tv;
        ImageView header_img;
        TextView name_tv;
        TextView sub_tv;
        TextView time_tv;
        ImageView phone_img;
    }
}
