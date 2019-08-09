package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.data.ObjectUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.Friend;
import com.core.net.http.ViewUtil;
import com.core.widget.EmptyLayout;
import com.core.xmpp.dao.FriendDao;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.NoticeData;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @功能:通知公告
 */
public class NoticesActivity extends BaseActivity {

    private PullToRefreshListView mlist;
    private ProgressBar progress_bar;
    private EmptyLayout mEmptyLayout;
    private CardItemAdapter adapter;
    private int type;
    private Context ct;
    private int gnum = 0, nnum = 0;
    private int currentpage = 1;
    private List<NoticeData> mNoticeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_notice_list);
        mlist = (PullToRefreshListView) findViewById(R.id.list_notice);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        initView();
        initData();
    }

    public void initView() {
        TAG = "NoticesActivity";
        ct = this;
        setTitle("通知");

        mNoticeData = new ArrayList<>();
        adapter = new CardItemAdapter(this,mNoticeData);
        mlist.getRefreshableView().setAdapter(adapter);

        mEmptyLayout = new EmptyLayout(this, mlist.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        type = getIntent().getIntExtra("type", 0);
        mlist.setMode(PullToRefreshBase.Mode.BOTH);
        mlist.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CardItemAdapter.ItemModel model = (CardItemAdapter.ItemModel) view.getTag();
                Intent intent = new Intent(NoticesActivity.this, NewsDetailActivity.class);
                intent.putExtra("id", model.id);
                intent.putExtra("type", type);
                startActivity(intent);
                model.img.setImageResource(R.drawable.notice_img_2);
                if (model.hasRead == -1) {
                    if (type == 1) {//通知
                        nnum++;
                    } else {//公告
                        gnum++;
                    }
                }

            }
        });

        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                currentpage = 1;
                initData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                currentpage++;
                initData();
            }
        });
    }

    @Override
    public void onBackPressed() {
        sendResult();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            sendResult();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendResult() {
        Intent intent = new Intent();
        if (type == 1) {//通知
            intent.putExtra(NoticeMenuActivity.NOTICE, nnum);
        } else { //公告
            intent.putExtra(NoticeMenuActivity.GONGGAO, gnum);

        }
        setResult(RESULT_OK, intent);
    }


    public void initData() {
        mlist.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
        String url =null;
        final Map<String, Object> param = new HashMap<>();
       // param.put("count", "1000");
        param.put("page",currentpage);
        param.put("pageSize",10);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        if (getIntent().getIntExtra("type", 0) == 1) {
            url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/note/inform.action";
            setTitle("通知");
            String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
            // 表示已读
            FriendDao.getInstance().markUserMessageRead(mLoginUserId, Friend.ID_ERP_NOTICE);
        } else {
            url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/note/notice.action";
            setTitle("公告");
            String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
            // 表示已读
            FriendDao.getInstance().markUserMessageRead(mLoginUserId, Friend.ID_ERP_GONGGAO);
        }
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, Constants.LOAD_SUCCESS, null, null, "get");
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.LOAD_SUCCESS:
                    if (currentpage == 1){
                        mNoticeData.clear();
                        ToastMessage("刷新成功");
                    }else {
                        ToastMessage("加载完毕");
                    }
                    String result = msg.getData().getString("result");
                    Log.i("handleMessage: ",result);
                    progress_bar.setVisibility(View.GONE);
                    mlist.setVisibility(View.VISIBLE);
                    mlist.onRefreshComplete();
                    try {
                        JSONObject resultJsonObject = new JSONObject(result);
                        JSONArray dataArray = resultJsonObject.getJSONArray("data");
                        if (currentpage == 1 && dataArray.length() == 0){
                            mEmptyLayout.showEmpty();
                        }else {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject currentObject = dataArray.getJSONObject(i);
                                NoticeData noticeData  = JSON.parseObject(currentObject.toString(), NoticeData.class);
                                mNoticeData.add(noticeData);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    result = msg.getData().getString("result");
                    Log.i(TAG, result);
                    mEmptyLayout.setErrorMessage(result + ",请刷新重试！");
                    mEmptyLayout.showError();
                    ViewUtil.AutoLoginErp(NoticesActivity.this);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public class CardItemAdapter extends BaseAdapter {

       // private NoticeEntity newsEntities;
        private List<NoticeData> noticeDataList;
        private LayoutInflater inflater;

        public CardItemAdapter(Context ct, List<NoticeData> noticeDataList) {
            this.noticeDataList = noticeDataList;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return noticeDataList == null ? 0 : noticeDataList.size();

        }

        @Override
        public Object getItem(int position) {
            return noticeDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemModel model = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_card_notice, parent, false);
                model = new ItemModel();
                model.img = (ImageView) convertView.findViewById(R.id.img);
//				model.tv_title=(TextView) convertView.findViewById(R.id.tv_title);
                model.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
//				model.tv_count=(TextView) convertView.findViewById(R.id.tv_count);
                model.tv_theme = (TextView) convertView.findViewById(R.id.tv_theme);
                model.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
//				model.tv_hasRead=(TextView)convertView.findViewById(R.id.tv_hasRead);
                convertView.setTag(model);
            } else {
                model = (ItemModel) convertView.getTag();
            }

          /*  if (position % 2 == 0) {
                convertView.setBackgroundColor(getResources().getColor(R.color.item_color1));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.item_color2));
            }*/

           // List<NoticeData> noticeDataList = noticeDataList;
//			model.tv_title.setText(datas.get(position).getNO_TITLE());
            model.tv_time.setText(CommonUtil.transferLongToDate("yyyy-MM-dd HH:mm:ss",
                    noticeDataList.get(position).getNO_APPTIME()));
            model.tv_theme.setText(noticeDataList.get(position).getNO_TITLE().replace("&nbsp;",""));
            model.tv_author.setText(noticeDataList.get(position).getNO_APPROVER());
            model.id = noticeDataList.get(position).getNO_ID();
            model.hasRead = noticeDataList.get(position).getSTATUS() == null ? -1 : (int) noticeDataList.get(position).getSTATUS();
            if (!ObjectUtils.isEquals(noticeDataList.get(position).getSTATUS(), null)) {
                if (((int) noticeDataList.get(position).getSTATUS()) != -1) {
                    model.img.setImageResource(R.drawable.notice_img_1);
                } else {
                    if (((int) noticeDataList.get(position).getSTATUS()) == -1) {
                        model.img.setImageResource(R.drawable.notice_img_2);
                    }
                }
            } else {
                model.img.setImageResource(R.drawable.notice_img_1);
            }

//			model.tv_count.setVisibility(View.GONE);
            return convertView;
        }

        class ItemModel {
            private int id;
            //			private TextView tv_title;
            private TextView tv_time;
            private ImageView img;
            //			private TextView tv_count;
            private TextView tv_theme;
            private TextView tv_author;
            private int hasRead;//已读，未读
        }

    }

}
