package com.uas.appcontact.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.common.ui.ViewHolder;
import com.core.model.User;
import com.core.utils.helper.AvatarHelper;
import com.uas.appcontact.R;
import com.uas.applocation.UasLocationHelper;

import java.text.DecimalFormat;
import java.util.List;

public class UserAdapter extends BaseAdapter {

	private List<User> mUsers;
	private Context mContext;

	public UserAdapter(List<User> users, Context context) {
		mUsers = users;
		mContext = context;
	}

	@Override
	public int getCount() {
		return mUsers.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.row_user, parent, false);
		}
		ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
		TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
		
		TextView des_tv = ViewHolder.get(convertView, R.id.des_tv);

		final User user = mUsers.get(position);
		// 设置头像
		AvatarHelper.getInstance().displayAvatar(user.getUserId(), avatar_img, true);
		
		double latitude = 0;
		double longitude = 0;
		double latitude_end = 0;
		double longitude_end = 0;
		if (user != null && user.getLoginLog() != null) {
			latitude = user.getLoginLog().getLatitude();
			longitude = user.getLoginLog().getLongitude();
		}
		
		if (UasLocationHelper.getInstance().getUASLocation().getLatitude() != 0 && UasLocationHelper.getInstance().getUASLocation().getLongitude() != 0) {
			latitude_end = UasLocationHelper.getInstance().getUASLocation().getLatitude();
			longitude_end = UasLocationHelper.getInstance().getUASLocation().getLongitude();
		}
		if (latitude != 0 && longitude != 0 && latitude_end != 0 && longitude_end != 0) {
			LatLng point_start = new LatLng(latitude, longitude);
			LatLng point_end = new LatLng(latitude_end , longitude_end);
			double distance  = DistanceUtil.getDistance(point_start, point_end);
			DecimalFormat df=new DecimalFormat(".##");
			String value = df.format(distance);
			des_tv.setText("距离 "+ value + " 米");
		}else{
			des_tv.setText("该好友未公开位置信息");
		}

		// 名称显示，因为这个人可能是我的好友，所以先从好友里面查询，看是不是有备注名
		// String name = FriendDao.getInstance().getRemarkName(mLoginUserId,
		// user.getUserId());
		// if (TextUtils.isEmpty(name)) {
		// name = user.getNickName();
		// }
		nick_name_tv.setText(user.getNickName());

		// 个性签名
		
		return convertView;
	}

}
