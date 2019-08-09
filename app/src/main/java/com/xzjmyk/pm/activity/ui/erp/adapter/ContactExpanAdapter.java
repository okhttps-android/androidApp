package com.xzjmyk.pm.activity.ui.erp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.xzjmyk.pm.activity.R;
import com.core.model.Friend;
import com.core.utils.helper.AvatarHelper;
import com.common.data.ListUtils;

import java.util.List;

/**
 * Created by pengminggong on 2016/10/13.
 */

public class ContactExpanAdapter extends BaseExpandableListAdapter {
    private Context ct;
    private List<List<Friend>> friends;
    public ContactExpanAdapter(Context ct, List<List<Friend>> friends) {
        this.ct = ct;
        this.friends = friends;
    }

    public List<List<Friend>> getFriends() {
        return friends;
    }

    public void setFriends(List<List<Friend>> friends) {
        this.friends = friends;
    }

    @Override
    public int getGroupCount() {
        return ListUtils.isEmpty(friends) ? 0 : friends.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return ListUtils.isEmpty(friends) ? 0 : ListUtils.isEmpty(friends.get(i)) ? 0 : friends.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return friends.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return friends.get(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(ct).inflate(R.layout.contact_expan_title, viewGroup, false);
        TextView tv = (TextView) view.findViewById(R.id.num_tv);
        ImageView next_img = (ImageView) view.findViewById(R.id.next_img);
        int num = 0;
        if (!ListUtils.isEmpty(friends) && !ListUtils.isEmpty(friends.get(i)))
            num = friends.get(i).size();
        tv.setText("常用联系人(" + num + ")");
        if (b) {
            next_img.setImageResource(R.drawable.oa_up);
        } else {
            next_img.setImageResource(R.drawable.oa_next);
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(ct).inflate(R.layout.contact_item, viewGroup, false);
            holder.header_img = (ImageView) view.findViewById(R.id.header_img);
            holder.phone_img = (ImageView) view.findViewById(R.id.phone_img);
            holder.tag_tv = (TextView) view.findViewById(R.id.tag_tv);
            holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
            holder.sub_tv = (TextView) view.findViewById(R.id.sub_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        Friend friend = friends.get(i).get(i1);
           /*头像*/
        if (friend.getRoomFlag() == 0) {// 这是单个人
            if (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {// 系统消息的头像
                holder.header_img.setImageResource(R.drawable.im_notice);
            } else if (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {// 新朋友的头像
                holder.header_img.setImageResource(R.drawable.im_new_friends);
            } else if (Integer.valueOf(friend.getUserId()) == 0) {
                holder.header_img.setImageResource(com.xzjmyk.pm.activity.R.drawable.avatar_normal);
            } else {// 其他
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), holder.header_img, true);//不会删除缓存
            }
        } else {// 这是1个房间
            if (TextUtils.isEmpty(friend.getRoomCreateUserId())) {
                holder.header_img.setImageResource(R.drawable.avatar_normal);
            } else {
                AvatarHelper.getInstance().displayAvatar(friend.getRoomCreateUserId(), holder.header_img, true);// 目前在备注名放房间的创建者Id
            }
        }
                /*昵称*/
        String name = friend.getRemarkName();
        if (TextUtils.isEmpty(name)) {
            name = friend.getNickName();
        }
        holder.name_tv.setText(name);
        /*个性签名*/
        holder.sub_tv.setText((StringUtil.isEmpty(friend.getDepart()) ? "" : (friend.getDepart() + ">"))
                + " " + (StringUtil.isEmpty(friend.getPosition()) ? "" : friend.getPosition()));
        return view;
    }

    class ViewHolder {
        TextView tag_tv, name_tv, sub_tv;
        ImageView header_img, phone_img;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        //子控件可以点击
        return true;
    }
}
