package com.modular.booking.activity.shares;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.core.base.SupportToolBarActivity;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.modular.booking.R;
import com.modular.booking.activity.business.BBookingDetailActivity;
import com.modular.booking.adapter.BookAdapter;
import com.modular.booking.model.BookingModel;

import java.util.ArrayList;


public class BBSharesListActivity extends SupportToolBarActivity {
    
    private ArrayList<BookingModel> mDatas = new ArrayList<>();
    private BookAdapter mAdapter;
    private PullToRefreshListView mlist;
    private EmptyLayout emptyLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shares_list);
        initView();
    }
    
    private void initView(){
       setTitle(getString(R.string.booking_shared));
        mlist=  findViewById(R.id.shareList);
        emptyLayout=new EmptyLayout(mContext,mlist.getRefreshableView());
        if (getIntent()!=null){
            mDatas=getIntent().getParcelableArrayListExtra("model");
            mAdapter=new BookAdapter(mContext,mDatas);
            mAdapter.setTime(true);
            mlist.setAdapter(mAdapter);
            if (mAdapter.getCount()==0){
                emptyLayout.setEmptyMessage(getString(R.string.book_empty));
                emptyLayout.showEmpty();
            }
        }

        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookAdapter.ViewHolder viewHolder = (BookAdapter.ViewHolder) view.getTag();
                Bundle bundle = new Bundle();
                bundle.putParcelable("model", viewHolder.model);
                bundle.putBoolean("isShared", true);
                if ("个人".equals(viewHolder.model.getKind())){
                    startActivity(new Intent("com.modular.booking.BookingDetailActivity")
                            .putExtras(bundle)  );
                }else{
                    startActivity(new Intent(mContext, BBookingDetailActivity.class)
                            .putExtras(bundle)  );
                }

            }
        });
    }
}
