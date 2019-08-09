package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.view.PullToRefreshSlideListView;

import java.util.ArrayList;

/**
 * 通知公告页面
 */
public class NoticeMenuActivity extends BaseActivity {
    private ArrayList<Bean> beans;
    public static final int NEWS_REC = 0x01, GONGGAO_REC = 0x03, NOTICES_REC = 0x02;
    private PullToRefreshSlideListView listView;
    private int GONGGAO_count = 0, NOTICE_count = 0, NES_count = 0;
    public static final String GONGGAO = "GONGGAO", NOTICE = "NOTICE", NES = "NES";
    private int[] src = {
            R.drawable.iconfont_bokexinwen, R.drawable.iconfont_tongzhi, R.drawable.iconfont_gonggao
    };
    private MAdapter adapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_menu);
      setTitle("通知公告");
        init();
        initView();
    }


    @Override
    public void onBackPressed() {
        sendResult();
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            sendResult();
            finish();
            overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendResult() {
        Intent intent = new Intent();
        intent.putExtra(NES, beans.get(0).unReNun);
        intent.putExtra(NOTICE, beans.get(1).unReNun);
        intent.putExtra(GONGGAO, beans.get(2).unReNun);
        setResult(RESULT_OK, intent);
    }


    private void initView() {
        listView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                distribute((i - 1), itemClick);
            }
        });
        adapter = new MAdapter();
        listView.setAdapter(adapter);
    }

    private Bean bean = null;   //临时的变量

    private void init() {
        Intent intent = getIntent();
        GONGGAO_count = intent.getIntExtra(GONGGAO, 0);
        NOTICE_count = intent.getIntExtra(NOTICE, 0);
        NES_count = intent.getIntExtra(NES, 0);
        beans = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            bean = new Bean();
            bean.imageSrc = src[i];
            distribute(i, setData);
            beans.add(bean);
        }
    }

    private int itemClick = 0x00a, setData = 0xaab;
    public void distribute(int i, int type) {
        switch (i) {
            case 0:
                if (type == itemClick) {
                    startActivityForResult(new Intent(NoticeMenuActivity.this, NewsTwoActivity.class).putExtra("type", 0), NEWS_REC);
                } else if (setData == type) {
                    bean.unReNun = NES_count;
                    if (bean.unReNun > 0) {
                        bean.subTitle = "您有新的新闻未阅读!";
                    } else {
                        bean.subTitle = "您暂无未阅读的新闻消息!";
                    }
                    bean.time = "刚刚";
                    bean.title = "新闻";
                }

                break;
            case 1:
                if (type == itemClick)
                    startActivityForResult(new Intent(NoticeMenuActivity.this, NoticesActivity.class).putExtra("type", 1), NOTICES_REC);
                else if (setData == type) {
                    bean.unReNun = NOTICE_count;
                    if (bean.unReNun > 0) {
                        bean.subTitle = "您有新的通知未阅读!";
                    } else {
                        bean.subTitle = "您暂无未阅读的通知!";
                    }
                    bean.time = "刚刚";
                    bean.title = "通知";
                }
                break;
            case 2:
                if (type == itemClick)
                    startActivityForResult(new Intent(NoticeMenuActivity.this, NoticesActivity.class).putExtra("type", 2), GONGGAO_REC);
                else if (setData == type) {
                    bean.unReNun = GONGGAO_count;
                    if (bean.unReNun > 0) {
                        bean.subTitle = "您有新的公告未阅读!";
                    } else {
                        bean.subTitle = "您暂无未阅读的公告!";
                    }
                    bean.time = "刚刚";
                    bean.title = "公告";
                }
                break;
        }
    }

    class Bean {
        String time;       //时间
        String title;    //标题
        String subTitle; //简介
        int unReNun;  //未读消息
        int imageSrc;  //图片资源
    }

    class MAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return beans.size();
        }

        @Override
        public Object getItem(int i) {
            return beans.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Bean bean = beans.get(i);
            MViewHolder holder = null;
            if (view == null) {
                holder = new MViewHolder();
                view = LayoutInflater.from(NoticeMenuActivity.this).inflate(R.layout.row_nearly_message, null);
                holder.imgHead = (ImageView) view.findViewById(R.id.avatar_img);
                holder.tvTime = (TextView) view.findViewById(R.id.time_tv);
                holder.tvSubTitle = (TextView) view.findViewById(R.id.content_tv);
                holder.tvTitle = (TextView) view.findViewById(R.id.nick_name_tv);
                holder.tvNum = (TextView) view.findViewById(R.id.num_tv);
                view.setTag(holder);
            } else {
                holder = (MViewHolder) view.getTag();
            }
            /*if (i % 2 == 0) {
                view.setBackgroundColor(getResources().getColor(R.color.item_color1));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.item_color2));
            }*/
            holder.imgHead.setBackgroundResource(R.color.transparent);
            holder.tvTime.setText(bean.time);
            holder.tvTitle.setText(bean.title);
            holder.tvSubTitle.setText(bean.subTitle);
            if (bean.unReNun > 0) {
                holder.tvNum.setText(bean.unReNun + "");
                holder.tvNum.setVisibility(View.VISIBLE);
            } else {
                holder.tvNum.setVisibility(View.GONE);
            }
            holder.imgHead.setImageDrawable(getResources().getDrawable(bean.imageSrc));
            return view;
        }

        class MViewHolder {
            TextView tvTime;
            TextView tvSubTitle;
            TextView tvTitle;
            TextView tvNum;
            ImageView imgHead;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NEWS_REC:
                if (resultCode == RESULT_OK) {
                    int num = data.getIntExtra(NES, -1);
                    if (num != -1) {
                        beans.get(0).unReNun -= num;
                        adapter.notifyDataSetChanged();

                    }
                }
                break;
            case NOTICES_REC:
                if (resultCode == RESULT_OK) {
                    int num = data.getIntExtra(NOTICE, -1);
                    if (num != -1) {
                        beans.get(1).unReNun -= num;
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            case GONGGAO_REC:
                if (resultCode == RESULT_OK) {
                    int num = data.getIntExtra(GONGGAO, -1);
                    if (num != -1) {
                        beans.get(2).unReNun -= num;
                        adapter.notifyDataSetChanged();

                    }
                }
                break;

        }
    }
}
