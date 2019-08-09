package com.xzjmyk.pm.activity.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.xmpp.model.MucRoom;
import com.core.xmpp.model.MucRoomMember;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.core.net.volley.FastVolley;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;

import java.util.HashMap;
import java.util.List;

public class SelectActivity extends AppCompatActivity {

    private String HASHCODE;
    private ClearEditText editText;
    private ListView listView;
    private String roomId;
    private AppConfig mConfig;
    private FastVolley mFastVolley;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initView();
    }

    private List<MucRoomMember> mMember;
    private List<MucRoomMember> mMembers;

    private void initView() {
        roomId = getIntent().getStringExtra("roomId");
        editText = (ClearEditText) findViewById(R.id.search_edit);
        listView = (ListView) findViewById(R.id.listview);
        init();
        loadMembers();
        editText.setVisibility(View.GONE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String name;
                String anme;
                mMembers.clear();
                for (int j = 0; j < mMember.size(); j++) {
                }


//                adapter.notifyDataSetChanged();
            }
        });
    }

    private void init() {
        mConfig = MyApplication.getInstance().getConfig();
        mFastVolley = MyApplication.getInstance().getFastVolley();
        HASHCODE = Integer.toHexString(this.hashCode()) + "@";// 加上@符号，将拼在一起的两个HashCode分开

    }

    private void loadMembers() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", roomId);
        StringJsonObjectRequest<MucRoom> request = new StringJsonObjectRequest<MucRoom>(mConfig.ROOM_GET, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(SelectActivity.this);
            }
        }, new StringJsonObjectRequest.Listener<MucRoom>() {
            @Override
            public void onResponse(ObjectResult<MucRoom> result) {
                boolean success = Result.defaultParser(SelectActivity.this, result, true);
                if (success && result.getData() != null) {
                    mMembers = result.getData().getMembers();
                    mMember = result.getData().getMembers();
                    updataUI();
                } else {

                }
            }
        }, MucRoom.class, params);
        addDefaultRequest(request);
    }

    private void updataUI() {
        if (mMembers != null && mMembers.size() > 0) {
            adapter = new Adapter();
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            ToastUtil.showToast(this, "为空的哟");
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 点击时候返回数据
                Intent intent = new Intent();
                intent.putExtra("NickName", mMembers.get(i).getNickName());
                intent.putExtra("UserId", mMembers.get(i).getUserId());
                setResult(MucChatActivity.RETURN_CODE, intent);
                finish();
            }
        });
    }

    public void addDefaultRequest(Request<?> request) {
        mFastVolley.addDefaultRequest(HASHCODE, request);
    }

    class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMembers.size();
        }

        @Override
        public Object getItem(int i) {
            return mMembers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(SelectActivity.this).inflate(R.layout.item_mmenber, null);
                viewHolder.img = (ImageView) view.findViewById(R.id.avatar_img);
                viewHolder.tvName = (TextView) view.findViewById(R.id.name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            AvatarHelper.getInstance().displayAvatar(mMembers.get(i).getUserId(), viewHolder.img, false);// 目前在备注名放房间的创建者Id
            viewHolder.tvName.setText(mMembers.get(i).getNickName());
            return view;
        }

        class ViewHolder {
            ImageView img;
            TextView tvName;
        }
    }
}
