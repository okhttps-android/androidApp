package com.uas.appme.other.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.data.CalendarUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.broadcast.MsgBroadcast;
import com.core.broadcast.MucgroupUpdateUtil;
import com.core.dao.DBManager;
import com.core.model.Friend;
import com.core.model.HrorgsEntity;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.net.volley.StringJsonObjectRequest.Listener;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.DataLoadView;
import com.core.widget.view.MyGridView;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.model.MucRoom;
import com.core.xmpp.model.MucRoom.Notice;
import com.core.xmpp.model.MucRoomMember;
import com.core.app.AppConstant;
import com.uas.appme.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.xzjmyk.pm.activity.ui.message
 * @作者:王阳
 * @创建时间: 2015年10月16日 下午3:13:40
 * @描述: 房间的详细信息
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容: 添加EditText的字数限制
 */
public class RoomInfoActivity extends BaseActivity {
    private String mRoomJid;
    private String mLoginUserId;
    private Friend mRoom;
    private TextView mNoticeTv;
    private TextView sc_tv;//群消息屏蔽
    private MyGridView mGridView;
    private TextView mRoomNameTv;
    private TextView mRoomDescTv;
    private TextView mCreatorTv;
    private TextView mCountTv;
    private TextView mNickNameTv;
    private TextView mCreateTime;
    private DataLoadView mDataLoadView;
    private List<MucRoomMember> mMembers;
    private GridViewAdapter mAdapter;
    private RelativeLayout add_manage_rl; //添加管理员
    private boolean dataInvalidate = true;// 数据是否有效，判断标准时传递进来的Occupant
    // list的首个人，是不是当前用户（因为传递进来的时候就把当前用户放到首位）
    private int add_minus_count = 2;// +号和-号的个数，如果权限可以踢人，就是2个，如果权限不可以踢人，就是1个

    private boolean mXmppBind;
    private CoreService mCoreService;
    public final static String ROOM_NAME = "ROOMNAME";
    private Button btnExit;
    private RelativeLayout remove_manage_rl;
    private boolean iscreater = false;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        members = new ArrayList<>();
        setContentView(R.layout.activity_room_info);
        if (getIntent() != null) {
            mRoomJid = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
           setTitle(getIntent().getStringExtra(ROOM_NAME));
        }
        if (TextUtils.isEmpty(mRoomJid)) {
            return;
        }
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        mRoom = FriendDao.getInstance().getFriend(mLoginUserId, mRoomJid);
        if (mRoom == null || TextUtils.isEmpty(mRoom.getRoomId())) {
            return;
        }
        // 绑定服务
        mXmppBind = bindService(CoreService.getIntent(), mXmppServiceConnection, BIND_AUTO_CREATE);
        initView();
        loadMembers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXmppBind) {
            unbindService(mXmppServiceConnection);
        }
    }

    private ServiceConnection mXmppServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.CoreServiceBinder) service).getService();
        }
    };

    private void initView() {
        remove_manage_rl = (RelativeLayout) findViewById(R.id.remove_manage_rl);
        add_manage_rl = (RelativeLayout) findViewById(R.id.add_manage_rl);
        sc_tv = (TextView) findViewById(R.id.shield_chat_text);
        mNoticeTv = (TextView) findViewById(R.id.notice_tv);
        mGridView = (MyGridView) findViewById(R.id.grid_view);
        mRoomNameTv = (TextView) findViewById(R.id.room_name_tv);
        mRoomDescTv = (TextView) findViewById(R.id.room_desc_tv);
        mCreatorTv = (TextView) findViewById(R.id.creator_tv);
        mCountTv = (TextView) findViewById(R.id.count_tv);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mCreateTime = (TextView) findViewById(R.id.create_timer);
        mDataLoadView = (DataLoadView) findViewById(R.id.data_load_view);
        mDataLoadView.setLoadingEvent(new DataLoadView.LoadingEvent() {
            @Override
            public void load() {
                loadMembers();
            }
        });
        btnExit = (Button) findViewById(R.id.btn_exit);
        mGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!doDel) {
                    return false;
                }
                doDel = false;
                mAdapter.notifyDataSetInvalidated();
                return false;
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (add_minus_count == 1) {
                    if (mMembers == null) return;
                    if (position == mMembers.size() - 1) {
                        List<String> existIds = new ArrayList<String>();
                        for (int i = 0; i < mMembers.size() - 1; i++) {
                            existIds.add(mMembers.get(i).getUserId());
                        }
                        // 去添加人
                        Intent intent = new Intent("com.modular.main.AddContactsActivity");
                        intent.putExtra("id", mRoom.get_id());
                        intent.putExtra("roomId", mRoom.getRoomId());
                        intent.putExtra("roomJid", mRoomJid);
                        intent.putExtra("roomName", mRoomNameTv.getText().toString());
                        intent.putExtra("roomDes", mRoomDescTv.getText().toString());
                        intent.putExtra("exist_ids", JSON.toJSONString(existIds));
                        startActivityForResult(intent, 1);
                    } else {
                        if (!doDel && !doBannedVoice) {
                            MucRoomMember member = mMembers.get(position);
                            if (member != null) {
                                Intent intent = new Intent(RoomInfoActivity.this, BasicInfoActivity.class);
                                intent.putExtra(AppConstant.EXTRA_USER_ID, member.getUserId());
                                startActivity(intent);
                            }
                        }
                    }
                } else if (add_minus_count == 2) {
                    if (position == mMembers.size() - 2) {
                        List<String> existIds = new ArrayList<String>();
                        for (int i = 0; i < mMembers.size() - 2; i++) {
                            existIds.add(mMembers.get(i).getUserId());
                        }
                        // 去添加人
                        Intent intent = new Intent("com.modular.main.AddContactsActivity");
                        intent.putExtra("id", mRoom.get_id());
                        intent.putExtra("roomId", mRoom.getRoomId());
                        intent.putExtra("roomJid", mRoomJid);
                        intent.putExtra("roomName", mRoomNameTv.getText().toString());
                        intent.putExtra("roomDes", mRoomDescTv.getText().toString());
                        intent.putExtra("exist_ids", JSON.toJSONString(existIds));
                        startActivityForResult(intent, 1);
                    } else if (position == mMembers.size() - 1) {
                        // delete
                        doDel = true;
                        mAdapter.notifyDataSetInvalidated();
                    } else {
                        if (!doDel && !doBannedVoice) {
                            MucRoomMember member = mMembers.get(position);
                            if (member != null) {
                                Intent intent = new Intent(RoomInfoActivity.this, BasicInfoActivity.class);
                                intent.putExtra(AppConstant.EXTRA_USER_ID, member.getUserId());
                                startActivity(intent);
                            }
                        }
                    }
                }
            }
        });
    }


    private void loadMembers() {
        mDataLoadView.showLoading();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mRoom.getRoomId());
        StringJsonObjectRequest<MucRoom> request = new StringJsonObjectRequest<MucRoom>(mConfig.ROOM_GET, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                mDataLoadView.showFailed();
            }
        }, new Listener<MucRoom>() {
            @Override
            public void onResponse(ObjectResult<MucRoom> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                Log.i("Arison", "RoomInfoActivity:onResponse:258:" + JSON.toJSONString(result.getData()));
                if (success && result.getData() != null) {
                    mDataLoadView.showSuccess();
                    updateUI(result.getData());
                } else {
                    ToastUtil.showErrorData(mContext);
                    mDataLoadView.showFailed();
                }
            }
        }, MucRoom.class, params);
        addDefaultRequest(request);
    }

    private void updateUI(final MucRoom mucRoom) {
        List<Notice> notices = mucRoom.getNotices();
        if (notices != null && !notices.isEmpty()) {
            String text = notices.get(0).getText();
            mNoticeTv.setText(text);
        } else {
            mNoticeTv.setText(R.string.no_notice);
        }

        mRoomNameTv.setText(mucRoom.getName());
        mRoomDescTv.setText(mRoom.getDescription());
        mCreatorTv.setText(mucRoom.getNickName());
        mCountTv.setText(mucRoom.getMaxUserSize() + "");
        int s = FriendDao.getInstance().getFriendStatus(mLoginUserId, mRoom.getUserId());
        if (s == -1) {
            sc_tv.setText(R.string.no_block_room_message);
        } else {
            sc_tv.setText(R.string.block_room_message);
        }
        mCreateTime.setText(TimeUtils.s_long_2_str(mucRoom.getCreateTime() * 1000));
        String myNickName = "";
        boolean isHaveM = false;
        if (mMembers != null) mMembers.clear();
        else mMembers = new ArrayList<>();
        List<MucRoomMember> munber = mucRoom.getMembers();
        MucRoomMember create = null;
        MucRoomMember my = null;
        if (munber != null) {
            for (int i = 0; i < munber.size(); i++) {
                if (munber.get(i).getRole() == 2) {
                    isHaveM = true;
                }
                String userId = munber.get(i).getUserId();
                if (!userId.equals(mucRoom.getUserId())) {
                    mMembers.add(munber.get(i));
                } else {
                    create = munber.get(i);
                }
                if (userId.equals(mLoginUserId)) {
                    myNickName = munber.get(i).getNickName();
                    my = munber.get(i);
                }


            }
            if (my != null) {// 将我自己移动到第一个的位置
                mMembers.remove(my);
                mMembers.add(0, my);
            }
            if (create != null) {
                if (!create.getUserId().equals(mLoginUserId))
                    mMembers.add(0, create);
            }
        }

        mAdapter = new GridViewAdapter(mMembers);
        mGridView.setAdapter(mAdapter);
        if (TextUtils.isEmpty(myNickName)) {
            mNickNameTv.setText(MyApplication.getInstance().mLoginUser.getNickName());
        } else {
            mNickNameTv.setText(myNickName);
        }

        if (mucRoom.getUserId().equals(mLoginUserId)) {// 我是创建者
            iscreater = true;
            if (btnExit == null) {
                btnExit = (Button) findViewById(R.id.btn_exit);
            }
            String[] compName = mucRoom.getName().split("-");
            add_minus_count = 2;
            add_manage_rl.setVisibility(View.VISIBLE);
            if (isHaveM) {
                remove_manage_rl.setVisibility(View.VISIBLE);
                remove_manage_rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAdministratorsDialog(REMOVE);
                    }
                });
            } else {
                remove_manage_rl.setVisibility(View.GONE);
            }
            findViewById(R.id.room_name_arrow_img).setVisibility(View.VISIBLE);
            findViewById(R.id.room_desc_arrow_img).setVisibility(View.VISIBLE);
            findViewById(R.id.banned_voice_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.banned_delete_rl).setVisibility(View.GONE);
            findViewById(R.id.exit_room_rl).setVisibility(View.VISIBLE);
            btnExit.setText(R.string.delete_room);

            if (mucRoom.getCategory() == 1) {
                if (CommonUtil.getSharedPreferences(mContext, "erp_commpany").equals(compName[0])) {
                    btnExit.setText(R.string.delete_room);

                } else {
                    btnExit.setText("用户未在当前账套上");
                    btnExit.setClickable(false);
                    btnExit.setVisibility(View.GONE);
                }
            }
            add_manage_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAdministratorsDialog(ADD);
                }
            });

            findViewById(R.id.room_name_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // change room name
                    showChangeRoomNameDialog(mRoomNameTv.getText().toString().trim());
                }
            });

            findViewById(R.id.room_desc_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // change room des
                    showChangeRoomDesDialog(mRoomDescTv.getText().toString().trim());
                }
            });
            findViewById(R.id.banned_voice_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {// 禁言
                    // change room des
                    doBannedVoice = true;
                    mAdapter.notifyDataSetChanged();
                }
            });

            //TODO 处理群消息屏蔽事件
            findViewById(R.id.shield_chat_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//屏蔽群消息
                    if (sc_tv.getText().toString().equals(R.string.block_room_message)) {
                        addBlacklist(mRoom);
                        sc_tv.setText(R.string.no_block_room_message);
                    } else if (sc_tv.getText().toString().equals(R.string.no_block_room_message)) {
                        removeBlacklist(mRoom);
                        sc_tv.setText(R.string.block_room_message);
                    }
                }
            });
        } else {
            if (my != null && my.getRole() == 2) {
                add_minus_count = 2;
            } else {
                add_minus_count = 1;
            }
            if (btnExit == null) {
                btnExit = (Button) findViewById(R.id.btn_exit);
            }
            btnExit.setText(R.string.exit_room);
            add_manage_rl.setVisibility(View.GONE);
            remove_manage_rl.setVisibility(View.GONE);
            findViewById(R.id.room_name_arrow_img).setVisibility(View.INVISIBLE);
            findViewById(R.id.room_desc_arrow_img).setVisibility(View.INVISIBLE);
            findViewById(R.id.banned_voice_rl).setVisibility(View.GONE);
            findViewById(R.id.banned_delete_rl).setVisibility(View.GONE);
            findViewById(R.id.exit_room_rl).setVisibility(View.VISIBLE);
            findViewById(R.id.room_name_rl).setOnClickListener(null);
            findViewById(R.id.room_desc_rl).setOnClickListener(null);
            findViewById(R.id.banned_voice_rl).setOnClickListener(null);
            findViewById(R.id.banned_delete_rl).setOnClickListener(null);


            findViewById(R.id.shield_chat_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//屏蔽群消息
                    if (sc_tv.getText().toString().equals(getString(R.string.block_room_message))) {
                        addBlacklist(mRoom);
                        sc_tv.setText(R.string.no_block_room_message);
                    } else if (sc_tv.getText().toString().equals(R.string.no_block_room_message)) {
                        removeBlacklist(mRoom);
                        sc_tv.setText(R.string.block_room_message);
                    }
                }
            });


        }
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRoom(mucRoom);
            }
        });

        findViewById(R.id.nick_name_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNickNameDialog(mNickNameTv.getText().toString().trim());
            }
        });

        if (add_minus_count == 1) {
            mMembers.add(null);// 一个+号
        } else if (add_minus_count == 2) {
            mMembers.add(null);// 一个+号
            mMembers.add(null);// 一个－号
        }
        // 添加新公告
        findViewById(R.id.notice_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change room des
                showNewNoticeDialog(mNoticeTv.getText().toString());
            }
        });
    }

    private int ADD = 1;
    private int REMOVE = 0;
    List<MucRoomMember> members;

    /**
     * 管理员操作选项
     *
     * @param h //操作 add为添加  remover删除
     */
    private void showAdministratorsDialog(final int h) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.activity_select_contacts, null);
        ListView list = (ListView) rootView.findViewById(R.id.list_view);
        Button ok_btn = (Button) rootView.findViewById(R.id.ok_btn);
        ok_btn.setText(R.string.common_sure);
        ListViewAdapter adapter = null;
        String title = null;
        if (h == ADD) {
            title = getString(R.string.add_administrators);
            members.clear();
            for (int i = 0; i < mMembers.size() - add_minus_count; i++) {
                if (mMembers.get(i).getRole() == 3) {
                    members.add(mMembers.get(i));
                }
            }
            adapter = new ListViewAdapter(members);
        } else {
            title = getString(R.string.remove_administrators);
            members.clear();
            for (int i = 0; i < mMembers.size() - add_minus_count; i++) {
                if (mMembers.get(i).getRole() == 2) {
                    members.add(mMembers.get(i));
                }
            }
            adapter = new ListViewAdapter(members);
        }
        list.setAdapter(adapter);
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(title).setView(rootView)
                .create();
        ok_btn.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          boolean isfrist = true;
                                          StringBuilder strId = new StringBuilder();
                                          if (h == ADD) {
                                              for (int i = 0; i < members.size(); i++) {
                                                  if (members.get(i).getRole() == 2) {
                                                      if (isfrist) {
                                                          isfrist = false;
                                                      } else {
                                                          strId.append(",");
                                                      }
                                                      strId.append(members.get(i).getUserId());

                                                  }
                                              }
                                          } else {
                                              for (int i = 0; i < members.size(); i++) {
                                                  if (members.get(i).getRole() == 3) {
                                                      if (isfrist) {
                                                          isfrist = false;
                                                      } else {
                                                          strId.append(",");
                                                      }
                                                      strId.append(members.get(i).getUserId());
                                                  }
                                              }
                                          }
                                          if (strId.length() > 0)
                                              updata(dialog, h, strId.toString());
//                                          else
//                                              ToastUtil.showToast(ct, "请选择添加人员");
                                      }
                                  }
        );
        dialog.show();
    }

    private void updata(final AlertDialog dialog, int type, String userId) {
        String url = "/room/setRoomManger";
        Map<String, String> params = new HashMap<>();
        params.put("roomId", mRoomJid);
        params.put("userId", userId);
        params.put("kind", type + "");
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.apiUrl + url, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
            }
        }, new Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    ToastMessage(getString(R.string.submit_success));
                    dialog.dismiss();
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    private class ListViewAdapter extends BaseAdapter {

        private List<MucRoomMember> members;

        public ListViewAdapter(List<MucRoomMember> mMembers) {
            this.members = mMembers;
        }

        @Override
        public int getCount() {
            return members.size();
        }

        @Override
        public Object getItem(int position) {
            return members.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            viewholder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
                holder = new viewholder();
                holder.img = (ImageView) convertView.findViewById(R.id.avatar_img);
                holder.tv = (TextView) convertView.findViewById(R.id.user_name_tv);
                holder.cb = (CheckBox) convertView.findViewById(R.id.check_box);
                convertView.setTag(holder);
            } else {
                holder = (viewholder) convertView.getTag();
            }
            final MucRoomMember mucRoomMember = members.get(position);
            AvatarHelper.getInstance().displayAvatar(mucRoomMember.getUserId(), holder.img, false);
            holder.tv.setText(mucRoomMember.getNickName());
            holder.cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mucRoomMember.getRole() == 3) {
                        mucRoomMember.setRole(2);
                    } else {
                        mucRoomMember.setRole(3);
                    }
                }
            });
            if (mucRoomMember.getRole() < 3) {
                holder.cb.setFocusable(false);
            }
            return convertView;
        }

        class viewholder {
            ImageView img;
            TextView tv;
            CheckBox cb;
        }
    }

    private void showNewNoticeDialog(final String notice) {
        final EditText editText = new EditText(this);
        editText.setLines(2);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.add_notice).setView(editText)
                .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(text) || text.equals(notice)) {
                            return;
                        }
                        updateRoom(null, text, null);
                    }
                }).setNegativeButton(getString(R.string.common_cancel), null);
        builder.create().show();

    }

    private AlertDialog.Builder builderChangeNickNameDialog = null;

    /**
     * 修改群昵称
     *
     * @param nickName
     */
    private void showChangeNickNameDialog(final String nickName) {
        if (builderChangeNickNameDialog == null) {
            final EditText editText = new EditText(this);
            editText.setMaxLines(2);
            editText.setLines(2);
            editText.setText(nickName);
            //		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
            ToastUtil.addEditTextNumChanged(RoomInfoActivity.this, editText, 8);
            editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            builderChangeNickNameDialog = new AlertDialog.Builder(this).setTitle(R.string.change_my_nickname).setView(editText)
                    .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String text = editText.getText().toString().trim();
                            if (TextUtils.isEmpty(text) || text.equals(nickName)) {
                                return;
                            }
                            updateNickName(text);
                        }
                    }).setNegativeButton(getString(R.string.common_cancel), null);

            builderChangeNickNameDialog.create().show();
            builderChangeNickNameDialog = null;
        }
    }

    private void showChangeRoomNameDialog(final String roomName) {
        final EditText editText = new EditText(this);
        editText.setMaxLines(2);
        editText.setLines(2);
        editText.setText(roomName);
        //		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        ToastUtil.addEditTextNumChanged(RoomInfoActivity.this, editText, 8);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.change_room_name).setView(editText)
                .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(text) || text.equals(roomName)) {
                            return;
                        }
                        updateRoom(text, null, null);
                    }
                }).setNegativeButton(getString(R.string.common_cancel), null);
        builder.create().show();
    }

    private void showChangeRoomDesDialog(final String roomDes) {
        final EditText editText = new EditText(this);
        editText.setMaxLines(2);
        editText.setLines(2);
        editText.setText(roomDes);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        ToastUtil.addEditTextNumChanged(RoomInfoActivity.this, editText, 20);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.change_room_des).setView(editText)
                .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(text) || text.equals(roomDes)) {
                            return;
                        }
                        updateRoom(null, null, text);
                    }
                }).setNegativeButton(getString(R.string.common_cancel), null);
        builder.create().show();
    }

    private boolean doDel = false;
    private boolean doBannedVoice = false;


    private class GridViewAdapter extends BaseAdapter {

        private final List<MucRoomMember> members;

        public GridViewAdapter(List<MucRoomMember> members) {
            this.members = members;
        }

        @Override
        public int getCount() {
            return members.size();
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
            View view = LayoutInflater.from(RoomInfoActivity.this).inflate(R.layout.item_room_info_view, parent, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.content);
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            Button button = (Button) view.findViewById(R.id.btn_del);
            if (position > members.size() - (add_minus_count + 1)) {
                if (add_minus_count == 1) {
                    if (position == members.size() - 1) {
                        imageView.setBackgroundResource(R.drawable.bg_room_info_add_btn);
                    }
                } else {
                    if (position == members.size() - 2) {
                        imageView.setBackgroundResource(R.drawable.bg_room_info_add_btn);
                    }
                    if (position == members.size() - 1) {
                        imageView.setBackgroundResource(R.drawable.bg_room_info_minus_btn);
                    }
                }
                button.setVisibility(View.GONE);

                if (doDel | doBannedVoice) {
                    view.setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            } else {
                // String id=ChatJID.getID(mOccupants.get(position).getJid());
                tvName.setText(members.get(position).getNickName());
                AvatarHelper.getInstance().displayAvatar(members.get(position).getUserId(), imageView, true);
                if (doDel | doBannedVoice) {
                    if (iscreater) {  //如果是创建者
                        if (members.get(position).getRole() != 1)
                            button.setVisibility(View.VISIBLE);
                        else
                            button.setVisibility(View.GONE);
                    } else {
                        if (members.get(position).getRole() == 3)
                            button.setVisibility(View.VISIBLE);
                        else
                            button.setVisibility(View.GONE);
                    }
                } else {
                    button.setVisibility(View.GONE);
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!dataInvalidate) {
                            return;
                        }
                        if (add_minus_count == 1) {
                            return;
                        }
                        if (doDel) {
                            if (members.get(position).getUserId().equals(mLoginUserId)) {
                                ToastUtil.showToast(mContext, R.string.can_not_remove_self);
                                return;
                            }
                            deleteMember(position, members.get(position).getUserId());
                        } else if (doBannedVoice) {
                            if (members.get(position).getUserId().equals(mLoginUserId)) {
                                ToastUtil.showToast(mContext, R.string.can_not_banned_self);
                                return;
                            }
                            showBanndedVoiceDialog(position, members.get(position).getUserId());
                        }

                    }
                });
            }
            return view;
        }

    }

    private void showBanndedVoiceDialog(final int position, final String userId) {
        CharSequence[] items = getResources().getStringArray(R.array.gags_select);
        new AlertDialog.Builder(mContext).setTitle(R.string.banned_voice).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int time = 0;
                int daySeconds = 24 * 60 * 60;
                switch (which) {
                    case 0:
                        time = 0;
                        break;
                    case 1:
                        time = daySeconds;
                        break;
                    case 2:
                        time = daySeconds * 3;
                        break;
                    case 3:
                        time = daySeconds * 7;
                        break;
                    case 4:
                        time = daySeconds * 15;
                        break;
                    case 5:
                        time = daySeconds * 30;
                        break;
                }
                bannedVoice(position, userId, CalendarUtil.getSecondMillion() + time);
            }
        }).create().show();
    }

    private void bannedVoice(final int position, String userId, final int time) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("userId", userId);
        params.put("talkTime", String.valueOf(time));

        final ProgressDialog dialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
        ProgressDialogUtil.show(dialog);

        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.ROOM_MEMBER_UPDATE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                ProgressDialogUtil.dismiss(dialog);
            }
        }, new Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                ProgressDialogUtil.dismiss(dialog);
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    if (time > CalendarUtil.getSecondMillion()) {
                        ToastUtil.showToast(mContext, R.string.gag_success);
                    } else {
                        ToastUtil.showToast(mContext, R.string.no_gag_success);
                    }
                    doBannedVoice = false;
                    mAdapter.notifyDataSetInvalidated();
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    //删除
    private void deleteRoom(final MucRoom sortFriend) {
        //根据mLoginuerId获取or_id,删除群需要去更新erp服务器数据
        final int or_id = getOr_id();
        boolean deleteRoom = false;
        if (mLoginUserId.equals(sortFriend.getUserId())) {
            //我是群主
            deleteRoom = true;
        }
        String url = null;
        if (deleteRoom) {
            url = MyApplication.getInstance().getConfig().ROOM_DELETE;
        } else {
            url = MyApplication.getInstance().getConfig().ROOM_MEMBER_DELETE;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", sortFriend.getId());
        if (!deleteRoom) {
            params.put("userId", mLoginUserId);
        }

        final ProgressDialog dialog = ProgressDialogUtil.init(this, null, getString(R.string.please_wait));
        ProgressDialogUtil.show(dialog);

        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(url, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ProgressDialogUtil.dismiss(dialog);
                ToastUtil.showErrorNet(RoomInfoActivity.this);
            }
        }, new Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean success = Result.defaultParser(RoomInfoActivity.this, result, true);
                if (success) {
                    deleteFriend(mRoom);
                    updateCreateFalgToERP(or_id);
                }
                ProgressDialogUtil.dismiss(dialog);
                ToastUtil.showToast(RoomInfoActivity.this, R.string.submit_success);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        startActivity(new Intent(RoomInfoActivity.this, CommonFragmentActivity.class));
                        Intent intent = new Intent(MucgroupUpdateUtil.ACTION_UPDATE);
                        setResult(0x9088, intent);
                        sendBroadcast(intent);
                        finish();
                    }
                }, 1000);
            }
        }, Void.class, params);
        this.addDefaultRequest(request);
    }

    private int getOr_id() {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) return 0;
        DBManager db = new DBManager();
        String master = CommonUtil.getMaster();
        String emCode = CommonUtil.getEmcode();
        //查找本地数据库，管理群存在，则不创建
        List<HrorgsEntity> hlist = db.queryHrorgList(new String[]{master, emCode}, "whichsys=? and or_headmancode=?");
        int or_id = 0;
        if (!ListUtils.isEmpty(hlist)) {
            or_id = hlist.get(0).getOr_id();
        }
        db.closeDB();
        return or_id;
    }


    private void deleteFriend(final Friend sortFriend) {

        Friend friend = sortFriend;
        // 删除这个房间
        FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());

        // 更新消息界面
        MsgBroadcast.broadcastMsgNumReset(this);
        MsgBroadcast.broadcastMsgUiUpdate(this);

        exitMucChat(friend.getUserId());
//        CardcastActivity activity = (CardcastActivity) getActivity();
//        activity.exitMucChat(friend.getUserId());
    }


    private void exitMucChat(String toUserId) {
        if (mCoreService != null) {
            mCoreService.exitMucChat(toUserId);
        }
    }


    /**
     * @desc:移除黑名单
     * @author：Administrator on 2016/3/25 10:03
     */
    public void removeBlacklist(final Friend sortFriend) {
        FriendDao.getInstance().updateFriendStatus(mLoginUserId, sortFriend.getUserId(), Friend.STATUS_FRIEND);
        int s = FriendDao.getInstance().getFriendStatus(mLoginUserId, sortFriend.getUserId());
//        ViewUtil.ToastMessage(this, "取消群消息成功！");
    }

    /**
     * @注释：拉黑
     */
    private void addBlacklist(final Friend sortFriend) {
        FriendDao.getInstance().updateFriendStatus(mLoginUserId, sortFriend.getUserId(), Friend.STATUS_BLACKLIST);
        int s = FriendDao.getInstance().getFriendStatus(mLoginUserId, sortFriend.getUserId());
//        ViewUtil.ToastMessage(this, "屏蔽群消息成功！");
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("access_token", MyApplication.getInstance().mAccessToken);
//        params.put("toUserId", sortFriend.getUserId());
//       final ProgressDialog mProgressDialog = ProgressDialogUtil.init(RoomInfoActivity.this, null, getString(R.string.please_wait));
//        ProgressDialogUtil.show(mProgressDialog);
//        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(MyApplication.getInstance().getConfig().FRIENDS_BLACKLIST_ADD, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError arg0) {
//                ProgressDialogUtil.dismiss(mProgressDialog);
//                ToastUtil.showErrorNet(RoomInfoActivity.this);
//            }
//        }, new StringJsonObjectRequest.Listener<Void>() {
//            @Override
//            public void onResponse(ObjectResult<Void> result) {
//
//                boolean success = Result.defaultParser(RoomInfoActivity.this, result, true);
//                if (success) {
//                    FriendDao.getInstance().updateFriendStatus(sortFriend.getOwnerId(), sortFriend.getUserId(),
//                            Friend.STATUS_BLACKLIST);
//                    FriendHelper.addBlacklistExtraOperation(mLoginUserId, sortFriend.getUserId());
//
//					/* 发送加入黑名单的通知 */
//                    if (sortFriend.getStatus() == Friend.STATUS_FRIEND) {// 之前是好友，需要发消息让那个人不能看我的商务圈
//                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(MyApplication.getInstance().mLoginUser,
//                                XmppMessage.TYPE_BLACK, null, sortFriend);
//                        //((CardcastActivity) getActivity()).sendNewFriendMessage(sortFriend.getBean().getUserId(), message);// 加入黑名单
//                    }
//
//                    ToastUtil.showToast(RoomInfoActivity.this, R.string.add_blacklist_succ);
//                    // 更新消息界面
//                    MsgBroadcast.broadcastMsgUiUpdate(RoomInfoActivity.this);
//                }
//                ProgressDialogUtil.dismiss(mProgressDialog);
//            }
//        }, Void.class, params);
////		mActivity.addDefaultRequest(request);
//        MyApplication.getInstance().getFastVolley().addDefaultRequest(HASHCODE, request);

    }

    String HASHCODE = Integer.toHexString(this.hashCode()) + "@";

    private void deleteMember(final int position, String userId) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("userId", userId);

        final ProgressDialog dialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
        ProgressDialogUtil.show(dialog);

        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.ROOM_MEMBER_DELETE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                ProgressDialogUtil.dismiss(dialog);
            }
        }, new Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                ProgressDialogUtil.dismiss(dialog);

                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    //System.out.println(result.toString()+"-----------------------");
                    mMembers.remove(position);
                    mAdapter.notifyDataSetInvalidated();
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    private void updateRoom(final String roomName, final String roomNotice, final String roomDes) {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mRoom.getRoomId());
        if (!TextUtils.isEmpty(roomName)) {
            params.put("roomName", roomName);
        }
        if (!TextUtils.isEmpty(roomNotice)) {
            params.put("notice", roomNotice);
        }
        if (!TextUtils.isEmpty(roomDes)) {
            params.put("desc", roomDes);
        }

        final ProgressDialog dialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
        ProgressDialogUtil.show(dialog);

        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.ROOM_UPDATE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                ProgressDialogUtil.dismiss(dialog);
            }
        }, new Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                ProgressDialogUtil.dismiss(dialog);
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    ToastUtil.showToast(mContext, R.string.update_success);
                    if (!TextUtils.isEmpty(roomName)) {
                        mRoomNameTv.setText(roomName);
                        mRoom.setNickName(roomName);
                        // 不去存入数据库，因为修改了房间名称后，会发一条推送，处理这条推送即可
                    }
                    if (!TextUtils.isEmpty(roomNotice)) {
                        // 修改了notice，也会有推送过来
                        mNoticeTv.setText(roomNotice);
                    }
                    if (!TextUtils.isEmpty(roomDes)) {
                        mRoomDescTv.setText(roomDes);
                        mRoom.setDescription(roomDes);
                        // 更新数据库
                        FriendDao.getInstance().createOrUpdateFriend(mRoom);
                    }
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    private void updateNickName(final String nickName) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mRoom.getRoomId());
        params.put("userId", mLoginUserId);
        params.put("nickname", nickName);
        final ProgressDialog dialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
        ProgressDialogUtil.show(dialog);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.ROOM_MEMBER_UPDATE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);
                ProgressDialogUtil.dismiss(dialog);
            }
        }, new Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                ProgressDialogUtil.dismiss(dialog);
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    ToastUtil.showToast(mContext, R.string.update_success);
                    mNickNameTv.setText(nickName);
                    String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                    FriendDao.getInstance().updateNickName(loginUserId, mRoom.getUserId(), nickName);
                    ChatMessageDao.getInstance().updateNickName(loginUserId, mRoom.getUserId(), loginUserId, nickName);
                    mRoom.setRoomMyNickName(nickName);
                    FriendDao.getInstance().createOrUpdateFriend(mRoom);
                    ListenerManager.getInstance().notifyNickNameChanged(mRoom.getUserId(), loginUserId, nickName);
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadMembers();
        }
    }


    /**
     * @desc:自动创建群后，避免重复，需要更新标识
     * @author：Administrator on 2016/5/17 16:59
     */
    public void updateCreateFalgToERP(int or_id) {
        String url = CommonUtil.getAppBaseUrl(mContext) + "/mobile/update_hrorgmobile.action";
        Map<String, Object> params = new HashMap<>();
        params.put("orid", or_id);
        params.put("kind", 0);//删除
        params.put("sessionId", CommonUtil.getSharedPreferences(mContext, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
        ViewUtil.httpSendRequest(mContext, url, params, handler, headers, 1, null, null, "get");
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = msg.getData().getString("result");
                    if (JSONUtil.validate(result)) {
                        String or_id = JSON.parseObject(result).getString("or_id");
                        if (!StringUtil.isEmpty(or_id)) {
                            if (mContext == null) return;
                            DBManager db = new DBManager(mContext);
                            String master = CommonUtil.getSharedPreferences(mContext, "erp_master");
                            db.updateHrogrRemark(Integer.valueOf(or_id), 0, master);
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    result = msg.getData().getString("result");
                    ToastUtil.showToast(ct,result);
                    break;
            }
        }
    };
}
