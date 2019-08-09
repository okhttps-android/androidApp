package com.xzjmyk.pm.activity.ui.message;

import android.os.Bundle;

import com.core.base.OABaseActivity;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.presenter.SubscriptionPresenter;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.ISubscriptionView;

public class SubscriptionActivity extends OABaseActivity implements ISubscriptionView {
    @ViewInject(R.id.listView)
    private PullToRefreshListView listView;

    private SubscriptionPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        ViewUtils.inject(this);
        presenter = new SubscriptionPresenter(this, this);
        presenter.start();
    }


}
