package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.OA.erp.model.MeetEntity;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MeetingActivity extends BaseActivity {
    private static final int PAGE_SIZE = 10;
    private final int whatLoad = 0x11;
    private int requestCode = 0x13;
    private int basePager = 1;
    private PullToRefreshListView list_business;
    private VoiceSearchView voiceSearchView;
    private MesstingAdapter adapter;
    private List<MeetEntity> entities;//网络获取来的item总数
    private String baseUrl;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            String message = (String) msg.getData().get("result");
            switch (msg.what) {
                case whatLoad:
                    JSONObject json = JSON.parseObject(message);
                    if (json.containsKey("listdata") && json.getJSONArray("listdata").size() > 0) {
                        List<MeetEntity> chches = JSON.parseArray(json.getJSONArray("listdata").toJSONString(), MeetEntity.class);
                        updateUI(chches);
                    }
                    if (voiceSearchView != null)
                        voiceSearchView.setText("");
                    if (list_business.isRefreshing())
                        list_business.onRefreshComplete();
                    break;
                case Constants.HTTP_SUCCESS_INIT:
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    break;
            }
        }
    };
    private EmptyLayout mEmptyLayout;

    private void updateUI(List<MeetEntity> listdata) {
        //更新界面
        if (adapter == null) {
            adapter = new MesstingAdapter();
            list_business.setAdapter(adapter);
        }
        if (basePager == 1) {
            entities = listdata;
        } else {
            if (entities == null)
                entities = listdata;
            else entities.addAll(listdata);
        }
        if (ListUtils.isEmpty(entities)){
            mEmptyLayout.showEmpty();
        }
        adapter.setData(entities);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        list_business = (PullToRefreshListView) findViewById(R.id.list_business);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mEmptyLayout = new EmptyLayout(this, list_business.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        baseUrl = CommonUtil.getSharedPreferences(ct, "erp_baseurl");
        initView();
        listener();
    }

    private void initView() {

        progressDialog.show();
        loadNetData(basePager);

    }

    private void listener() {
        list_business.setMode(PullToRefreshBase.Mode.BOTH);
        list_business.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (entities != null) entities.clear();
                loadNetData(1);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNetData(basePager + 1);

            }
        });


        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                String strChche = editable.toString().replace(" ", "");//去除空格
                strChche = strChche.replace(" ", "");//去除空格
                List<MeetEntity> chche = new ArrayList<>();
                if (entities == null || entities.size() <= 0) return;
                for (MeetEntity e : entities) {
                    boolean b = false;
                    try {
                        b = getResult(e.getMa_theme() + e.getMa_recorder(), strChche.trim());
                    } catch (PatternSyntaxException r) {
                        r.printStackTrace();
                    }
                    if (b) {
                        chche.add(e);
                    }
                }
                adapter.setData(chche);
            }
        });
        list_business.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MeetingActivity.this, MeetDetailsActivity.class);
                MeetEntity entity = adapter.getData().get((i - 1 < 0 ? 0 : (i - 1)));
                intent.putExtra("data", entity);
                intent.putExtra("item", (i - 1 < 0 ? 0 : (i - 1)));
                startActivityForResult(intent, requestCode);
            }
        });


    }

    //正则
    private static boolean getResult(String text, String str) {
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(text);
        return m.find();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            activity.startActivityForResult(new Intent(activity, AddMeetingActivity.class), requestCode);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //获取网络数据
    private void loadNetData(int pager) {
        basePager = pager;
        //获取网络数据
        String url = baseUrl + "mobile/common/list.action";
        String emcode = CommonUtil.getSharedPreferences(ct, "erp_username");
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        String caller = "MeetingroomapplyDetail"; //新的
//        String caller = "Meetingroomapply"; // 旧的
        String condition = "MD_EMCODE=" + "\'" + emcode + "\'";
//        String condition = "1=1";
        final Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        param.put("currentMaster", CommonUtil.getSharedPreferences(this, "erp_master"));
        param.put("emcode", emcode);
        param.put("page", basePager);
        param.put("pageSize", PAGE_SIZE);
        param.put("condition", condition);
        param.put("sessionId", sessionId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, whatLoad, null, null, "post");
    }


    class MesstingAdapter extends BaseAdapter {

        private List<MeetEntity> entities;

        public MesstingAdapter() {
        }

        public MesstingAdapter(List<MeetEntity> entities) {
            this.entities = entities;
        }

        public void setData(List<MeetEntity> entities) {
            this.entities = entities;
            notifyDataSetChanged();
        }

        public List<MeetEntity> getData() {
            return entities;
        }

        @Override
        public int getCount() {
            return entities == null ? 0 : entities.size();
        }

        @Override
        public Object getItem(int i) {
            return entities == null ? null : entities.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewholder = null;
            if (view == null) {
                view = LayoutInflater.from(MeetingActivity.this).inflate(R.layout.oa_meeting_item, null);
                viewholder = new ViewHolder();
                viewholder.oa_meeting_name_tv = (TextView) view.findViewById(R.id.oa_meeting_name_tv);
                viewholder.oa_meeting_status_tv = (TextView) view.findViewById(R.id.oa_meeting_status_tv);
                viewholder.oa_meeting_start = (TextView) view.findViewById(R.id.oa_meeting_start);
                viewholder.oa_meeting_end = (TextView) view.findViewById(R.id.oa_meeting_end);
                viewholder.oa_meeting_start_date = (TextView) view.findViewById(R.id.oa_meeting_start_date);
                viewholder.oa_meeting_end_date = (TextView) view.findViewById(R.id.oa_meeting_end_date);
                viewholder.oa_meeting_addr_tv = (TextView) view.findViewById(R.id.oa_meeting_addr_tv);
                viewholder.oa_meeting_user_tv = (TextView) view.findViewById(R.id.oa_meeting_user_tv);
                viewholder.oa_meeting_tag_tv = (TextView) view.findViewById(R.id.oa_meeting_tag_tv);
                view.setTag(viewholder);
            } else {
                viewholder = (ViewHolder) view.getTag();
            }
            //当系统时间<开始时间 会议状态为未开始，
            // 当会议开始时间<系统时间会议状态为进行中，当会议发起人点击结束会议按钮，
            // 状态更新为已结束，否则即使超过会议结束时间状态也是进行中
            String status = entities.get(i).getMa_stage();
            if (status == null || status.trim().length() <= 0) {
                if (TimeUtils.f_str_2_long(entities.get(i).getMa_starttime()) <= System.currentTimeMillis()) {
                    status = getResources().getString(R.string.doing);
                } else {
                    status = getResources().getString(R.string.not_bigan);
                }
                entities.get(i).setMa_stage(status);
            }
            if (status != null) {
                int statusColor = R.color.meeting_end_status;
                if (getResources().getString(R.string.doing).equals(status)) {
                    statusColor = R.color.meeting_start_status;
                } else {
                    statusColor = R.color.meeting_end_status;
                }
                viewholder.oa_meeting_status_tv.setText(status);
                viewholder.oa_meeting_status_tv.setTextColor(getResources().getColor(statusColor));
            }
            long startLong = TimeUtils.f_str_2_long(entities.get(i).getMa_starttime());
            long endLong = TimeUtils.f_str_2_long(entities.get(i).getMa_endtime());
            viewholder.oa_meeting_start.setText(DateFormatUtil.long2Str(startLong, "MM月dd日"));
            viewholder.oa_meeting_end.setText(DateFormatUtil.long2Str(endLong, "MM月dd日"));
            viewholder.oa_meeting_start_date.setText(DateFormatUtil.long2Str(startLong, "HH:mm"));
            viewholder.oa_meeting_end_date.setText(DateFormatUtil.long2Str(endLong, "HH:mm"));
            viewholder.oa_meeting_name_tv.setText(entities.get(i).getMa_theme());
            viewholder.oa_meeting_addr_tv.setText(entities.get(i).getMa_mrname());
            viewholder.oa_meeting_user_tv.setText(entities.get(i).getMa_recorder());
            viewholder.oa_meeting_tag_tv.setText(StringUtil.isEmpty(entities.get(i).getMa_type()) ? getString(R.string.common_noinput) : entities.get(i).getMa_type());
            return view;
        }

        private void bindData(ViewHolder viewholder, int i) {
            MeetEntity entity = entities.get(i);
            //当系统时间<开始时间 会议状态为未开始，
            // 当会议开始时间<系统时间会议状态为进行中，当会议发起人点击结束会议按钮，
            // 状态更新为已结束，否则即使超过会议结束时间状态也是进行中
            String str = entity.getMa_stage();
            if (str == null || str.trim().length() <= 0) {
                if (TimeUtils.f_str_2_long(entity.getMa_starttime()) > System.currentTimeMillis())
                    str = getResources().getString(R.string.not_bigan);
                else str = getResources().getString(R.string.doing);
            }
            entities.get(i).setMa_stage(str);
            viewholder.oa_meeting_status_tv.setText(entities.get(i).getMa_stage());
            long startLong = TimeUtils.f_str_2_long(entity.getMa_starttime());
            long endLong = TimeUtils.f_str_2_long(entity.getMa_endtime());
            viewholder.oa_meeting_start.setText(DateFormatUtil.long2Str(startLong, "MM月dd日"));
            viewholder.oa_meeting_end.setText(DateFormatUtil.long2Str(endLong, "MM月dd日"));
            viewholder.oa_meeting_start_date.setText(DateFormatUtil.long2Str(startLong, "HH:mm"));
            viewholder.oa_meeting_end_date.setText(DateFormatUtil.long2Str(endLong, "HH:mm"));
            viewholder.oa_meeting_name_tv.setText(entity.getMa_theme());
            viewholder.oa_meeting_addr_tv.setText(entity.getMa_mrname());
            viewholder.oa_meeting_user_tv.setText(entity.getMa_recorder());
            viewholder.oa_meeting_tag_tv.setText(StringUtil.isEmpty(entity.getMa_type()) ? getString(R.string.common_noinput) : entity.getMa_type());
            if (entity.getMa_tag() == null || entity.getMa_tag().length() <= 0) return;
        }

        class ViewHolder {
            TextView oa_meeting_name_tv,    //会议名称
                    oa_meeting_status_tv, //会议状态
                    oa_meeting_start,//会议开始日期
                    oa_meeting_end,//会议结束日期
                    oa_meeting_start_date,//会议开始时间
                    oa_meeting_end_date,//会议结束时间
                    oa_meeting_addr_tv,//会议地址
                    oa_meeting_user_tv,//会议发起人
                    oa_meeting_tag_tv;//会议标签
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (requestCode == this.requestCode) {
            if (resultCode == 0x14) {
                if ("data".equals(data.getStringExtra("data"))) {
                    list_business.setRefreshing(true);
                    loadNetData(1);
                }
            } else if (resultCode == 0x15) {
                loadNetData(1);
//                Log.i("gongpengming", "resultCode == 0x15");
//                int i = data.getIntExtra("item", -1);
//                Log.i("gongpengming", "i == " + i);
//                if (i >= 0 && data.getBooleanExtra("data", false)) {//是否结束会议
//                    Log.i("gongpengming", "进来了" + i);
//                    entities = adapter.getData();
//                    entities.get(i).setStatus("已结束");
//                    Log.i("gongpengming", "进来了222" + entities.get(i).getStatus());
//                }
//                adapter.setData(entities);
            }
        }
    }
}
