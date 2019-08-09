package com.xzjmyk.pm.activity.ui.groupchat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.system.DisplayUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.model.Friend;
import com.xzjmyk.pm.activity.bean.oa.MeetUserEntity;
import com.xzjmyk.pm.activity.broadcast.MucgroupUpdateUtil;
import com.core.xmpp.dao.FriendDao;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.sortlist.BaseSortModel;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.util.im.CreateRoomUtil;
import com.common.data.ListUtils;
import com.xzjmyk.pm.activity.ui.message.MucChatActivity;
import com.xzjmyk.pm.activity.util.im.Constants;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.xzjmyk.pm.activity.view.HorizontalListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建群
 * create by Bitliker 2016/11/2
 */
public class CreateGroupActivity extends BaseActivity {
    @ViewInject(R.id.ok_btn)
    private Button ok_btn;
    @ViewInject(R.id.recycler)
    private RecyclerView recycler;
    @ViewInject(R.id.h_listview)
    private HorizontalListView h_listview;

    private String loginUserId = null;
    private List<Friend> selectFriend;
    private List<BaseSortModel<Friend>> beans;
    private HorListViewAdapter hAdapter;
    private RecycAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ViewUtils.inject(this);
        init();
        initView();
        initEvent();
    }

    private void initEvent() {
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectFriend.size() > 0){
                    showCreateGroupDialog();
                }else {
                    ToastMessage("选择人员为空");
                    return;
                }
            }
        });
        h_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == selectFriend.size()) return;
                for (int i = 0; i < beans.size(); i++) {
                    if (beans.get(i).getBean().getUserId().equals(selectFriend.get(position).getUserId())) {
                        beans.get(i).setClick(!beans.get(i).isClick);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
                selectFriend.remove(position);
                hAdapter.notifyDataSetChanged();
                ok_btn.setText(getOkText());
            }
        });
    }


    private void init() {
        loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        //获取好友
        List<Friend> friends = FriendDao.getInstance().getFriends(loginUserId);
        selectFriend = new ArrayList<>();
        beans = new ArrayList<>();
        BaseSortModel<Friend> bean = null;
        for (Friend e : friends) {
            bean = new BaseSortModel<>();
            bean.setBean(e);
            beans.add(bean);
        }
    }

    private void initView() {
        recycler.setLayoutManager(new LinearLayoutManager(ct));
        adapter = new RecycAdapter();
        recycler.setAdapter(adapter);
        hAdapter = new HorListViewAdapter();
        h_listview.setAdapter(hAdapter);
        ok_btn.setText(getOkText());
    }

    /*计算人数*/
    public String getOkText() {
        return getString(R.string.common_sure)+" ( " + selectFriend.size() + " )";
    }

    /*弹出创建群窗口*/
    private void showCreateGroupDialog() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_muc_room, null);
        final ClearEditText roomNameEdit = (ClearEditText) rootView.findViewById(R.id.room_name_edit);
        TextView tvName = (TextView) rootView.findViewById(R.id.creater_name);
        TextView tvNum = (TextView) rootView.findViewById(R.id.tv_num);
        tvNum.setText(selectFriend.size() + "/1000");
        tvName.setText(MyApplication.getInstance().mLoginUser.getNickName());
        final ClearEditText roomDescEdit = (ClearEditText) rootView.findViewById(R.id.room_desc_edit);

        ToastUtil.addEditTextNumChanged(ct, roomNameEdit, 8);// 设置EditText的字数限制
        ToastUtil.addEditTextNumChanged(ct, roomDescEdit, 20);

        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.create_room).setView(rootView).create();
        rootView.findViewById(R.id.sure_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String roomName = roomNameEdit.getText().toString().trim();//防止空,或者输入空格
                if (TextUtils.isEmpty(roomName)) {
                    ToastUtil.showToast(mContext, R.string.room_name_empty_error);
                    return;
                }
                String roomDesc = roomDescEdit.getText().toString();
                if (TextUtils.isEmpty(roomName)) {
                    ToastUtil.showToast(mContext, R.string.room_des_empty_error);
                    return;
                }
                ArrayList<MeetUserEntity> entities = new ArrayList<>();
                MeetUserEntity entity = null;
                final String[] noticeFriendList = new String[selectFriend.size()];
                for (int i = 0; i < selectFriend.size(); i++) {
                    entity = new MeetUserEntity();
                    noticeFriendList[i] = selectFriend.get(i).getUserId();
                    entity.setImId(selectFriend.get(i).getUserId());
                    entity.setName(selectFriend.get(i).getNickName());
                    entity.setEmCode(selectFriend.get(i).getEmCode());
                    entities.add(entity);
                }
                CreateRoomUtil.getInstance().createRoom(ct, entities, roomName, roomDesc, new CreateRoomUtil.OnCreateRoomListener() {
                    @Override
                    public void result(boolean isOk, String jid) {
                        if (isOk) {
                            ToastUtil.showToast(ct, "已经为您的会议建立群组");
                            Intent intent = new Intent(ct, MucChatActivity.class);
                            intent.putExtra(AppConstant.EXTRA_USER_ID, jid);
                            intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
                            intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                            intent.putExtra(Constants.GROUP_JOIN_NOTICE, noticeFriendList);
                            startActivity(intent);
                            sendBroadcast(new Intent(MucgroupUpdateUtil.ACTION_UPDATE));
                            finish();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public class RecycAdapter extends RecyclerView.Adapter<RecycAdapter.ViewHolder> {
        @Override
        public RecycAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ct).inflate(R.layout.row_select_contacts, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecycAdapter.ViewHolder holder, final int position) {
            final BaseSortModel<Friend> bean = beans.get(position);
            AvatarHelper.getInstance().displayAvatar(bean.getBean().getUserId(), holder.avatarImg, true);
            holder.userNameTv.setText(bean.getBean().getNickName());
            holder.checkBox.setChecked(bean.isClick);
            holder.checkBox.setFocusable(false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    beans.get(position).setClick(!beans.get(position).isClick);
                    notifyItemChanged(position);
                    if (beans.get(position).isClick)
                        selectFriend.add(beans.get(position).getBean());
                    else
                        selectFriend.remove(beans.get(position).getBean());
                    hAdapter.notifyDataSetChanged();
                    ok_btn.setText(getOkText());
                }
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView avatarImg;
            TextView userNameTv;
            CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                avatarImg = (ImageView) itemView.findViewById(R.id.avatar_img);
                userNameTv = (TextView) itemView.findViewById(R.id.user_name_tv);
                checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
            }
        }

        @Override
        public int getItemCount() {
            return ListUtils.isEmpty(beans) ? 0 : beans.size();
        }
    }

    public class HorListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return ListUtils.isEmpty(selectFriend) ? 1 : selectFriend.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return selectFriend.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 37);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            if (getCount() == 1 || position == selectFriend.size()) {
                imageView.setImageResource(R.drawable.dot_avatar);
            } else {
                AvatarHelper.getInstance().displayAvatar(selectFriend.get(position).getUserId(), imageView, true);
            }
            return convertView;
        }
    }
}
