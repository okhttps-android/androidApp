package com.xzjmyk.pm.activity.ui.erp.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.NewsData;
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
 * @功能:原生界面新闻
 */
public class NewsTwoActivity extends BaseActivity {
    @ViewInject(R.id.list_news)
    private PullToRefreshListView mlist;
    @ViewInject(R.id.progress_bar)
    private ProgressBar progress_bar;
    private CardItemAdapter adapter;
    public EmptyLayout mEmptyLayout;
    private Context ct;
    private int num = 0;
    private int currentpage = 1;
    private List<NewsData> mNewsDataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        setContentView(R.layout.act_news_list);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        ct = this;
        ViewUtils.inject(this);
        TAG = "NewsTwoActivity";

        mNewsDataList = new ArrayList<>();
        adapter = new CardItemAdapter(ct, mNewsDataList);
        mlist.getRefreshableView().setAdapter(adapter);

        String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        // 表示已读
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, Friend.ID_ERP_NEWS);

        mEmptyLayout = new EmptyLayout(this, mlist.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        setTitle("新闻");
        mlist.setMode(PullToRefreshBase.Mode.BOTH);
        mlist.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CardItemAdapter.ItemModel model = (CardItemAdapter.ItemModel) view.getTag();
                Intent intent = new Intent(NewsTwoActivity.this, NewsDetailActivity.class);
                intent.putExtra("id", model.id);
                startActivity(intent);
                model.img.setImageResource(R.drawable.notice_img_2);
                if (model.hasRead == -1) {
                    num++;
                }
            }
        });

        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                currentpage = 1;
                initData();

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
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
        intent.putExtra(NoticeMenuActivity.NES, num);
        setResult(RESULT_OK, intent);
    }

    public void initData() {
        mlist.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
        String url = CommonUtil.getAppBaseUrl(ct) + "common/desktop/news/getNews.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("page", currentpage);
        param.put("pageSizepageSize",10);
       // param.put("count",1000);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, Constants.LOAD_SUCCESS, null, null, "get");
    }
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.LOAD_SUCCESS:
                    if (currentpage == 1){
                        mNewsDataList.clear();
                        ToastMessage("刷新成功");
                    }else{
                        ToastMessage("加载完毕");
                    }
                    String result = msg.getData().getString("result");
                   // Log.i("handleMessage: ",result);
                    progress_bar.setVisibility(View.GONE);
                    mlist.setVisibility(View.VISIBLE);
                    mlist.onRefreshComplete();
                    try {
                        JSONObject resultJsonObject = new JSONObject(result);
                        JSONArray dataArray = resultJsonObject.getJSONArray("data");
                        if (currentpage == 1 && dataArray == null && mNewsDataList.isEmpty()){
                            mEmptyLayout.showEmpty();
                        }else {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject currentObject = dataArray.getJSONObject(i);
                                NewsData newsData = JSON.parseObject(currentObject.toString(), NewsData.class);
                                mNewsDataList.add(newsData);
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
                    progress_bar.setVisibility(View.GONE);
                    ViewUtil.AutoLoginErp(NewsTwoActivity.this);
                    break;
                default:
                    break;
            }
        }


    };


    public class CardItemAdapter extends BaseAdapter {
       // private NewsEntity newsEntities;
        private List<NewsData> newsDataList;
        private Context ct;
        private LayoutInflater inflater;

        public CardItemAdapter(Context ct, List<NewsData> newsDataList) {
            this.ct = ct;
            this.newsDataList = newsDataList;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return newsDataList == null ? 0 : newsDataList.size();

        }

        @Override
        public Object getItem(int position) {
            return newsDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

            /*if (position % 2 == 0) {
                convertView.setBackgroundColor(getResources().getColor(R.color.item_color1));

            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.item_color2));

            }*/
//			model.tv_title.setText(datas.get(position).getNE_THEME());
            model.tv_time.setText(CommonUtil.transferLongToDate("yyyy-MM-dd HH:mm:ss",
                    newsDataList.get(position).getNE_RELEASEDATE()));

//			model.tv_count.setText(datas.get(position).getNE_BROWSENUMBER()+"");
            model.tv_theme.setText(newsDataList.get(position).getNE_THEME());
            model.tv_author.setText("作者:" + newsDataList.get(position).getNE_RELEASER());
            model.id = newsDataList.get(position).getNE_ID();
            if (!ObjectUtils.isEquals(newsDataList.get(position).getSTATUS(), null)) {
                if (((int) newsDataList.get(position).getSTATUS()) != -1) {
                    model.img.setImageResource(R.drawable.notice_img_1);
                    model.hasRead = -1;

                } else {
                    if (((int) newsDataList.get(position).getSTATUS()) == -1) {
                        model.img.setImageResource(R.drawable.notice_img_2);
                        model.hasRead = 1;

                    }
                }
            } else {
                model.img.setImageResource(R.drawable.notice_img_1);
                model.hasRead = -1;

            }
            return convertView;
        }


        class ItemModel {
            private int id;
            private ImageView img;
            //			private TextView tv_title;
            private TextView tv_time;
            //			private TextView tv_count;
            private TextView tv_theme;
            private TextView tv_author;
            private int hasRead;//已读，未读
        }

    }
}
