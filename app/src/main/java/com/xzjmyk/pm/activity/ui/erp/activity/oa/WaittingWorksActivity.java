package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.base.BaseActivity;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;

/**
 * Created by FANGlh on 2017/2/24.
 * function:待办工作界面显示，其他类似界面可以参照这里
 */
public class WaittingWorksActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.waitting_work_plv)
    private PullToRefreshListView myplv;
    private String msg_title = "待办工作";
    private NewMsgAdapter msgAdapter;
    private PopupWindow setWindow = null;
    private int mPosition;
    private EmptyLayout mEmptyLayout;
    private int mSize = 10;
    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waitting_works);
        setTitle(msg_title);
        ViewUtils.inject(this);
        initView();
        initMsgsData();
        NewMsgsEvent();
    }

    private void initView() {
        mEmptyLayout = new EmptyLayout(this, myplv.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        myplv.setMode(PullToRefreshBase.Mode.BOTH);

        msgAdapter = new NewMsgAdapter();
        myplv.getRefreshableView().setAdapter(msgAdapter);
    }

    private void initMsgsData() {

    }

    private void NewMsgsEvent() {

        myplv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase refreshView) { //TODO 下拉刷新
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myplv.onRefreshComplete();
                    }
                },3000);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) { //TODO 上拉加载
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myplv.onRefreshComplete();
                    }
                }, 3000);
            }
        });
        myplv.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                ToastMessage("点击position = " + mPosition);
            }
        });

        myplv.getRefreshableView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                ToastMessage("长按position = " + mPosition);
                showPopupWindow();
                return true;
            }
        });
    }

    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(mContext).inflate(R.layout.msgs_long_click,null);
        viewContext.findViewById(R.id.msg_delete_tv).setOnClickListener(this);
        viewContext.findViewById(R.id.msg_markread_tv).setOnClickListener(this);
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }
    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.msg_delete_tv:
                doDeleteMsg();     //TODO 删除操作
                closePopupWindow();
                break;
            case R.id.msg_markread_tv:
                doMarkReadMsg();   // TODO 标为已读
                closePopupWindow();
                break;
        }
    }

    private void doMarkReadMsg() {

    }

    private void doDeleteMsg() {
        mSize--;
        msgAdapter.notifyDataSetChanged();
        Toast.makeText(ct, "删除成功", Toast.LENGTH_SHORT).show();

        if (mSize == 0) mEmptyLayout.showEmpty();
    }

    //TODO 列表适配器
    public  class NewMsgAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(ct,R.layout.item_newmsgs,null);
                viewHolder = new ViewHolder();
                viewHolder.msgs_img = (ImageView) convertView.findViewById(R.id.msgs_img);
                viewHolder.msgs_nums_tv = (TextView) convertView.findViewById(R.id.msgs_nums_tv);
                viewHolder.msgs_title_tv = (TextView) convertView.findViewById(R.id.msgs_time_tv);
                viewHolder.msgs_content_tv = (TextView) convertView.findViewById(R.id.msgs_content_tv);
                viewHolder.msgs_time_tv = (TextView) convertView.findViewById(R.id.msgs_time_tv);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView msgs_img;
            TextView msgs_nums_tv;
            TextView msgs_title_tv;
            TextView msgs_content_tv;
            TextView msgs_time_tv;
        }
    }
}
