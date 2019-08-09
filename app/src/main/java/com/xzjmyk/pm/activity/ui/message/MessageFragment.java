package com.xzjmyk.pm.activity.ui.message;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.baidu.aip.excep.activity.RealTimeDetectFaceActivty;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.common.system.PermissionUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.base.fragment.SupportToolBarFragment;
import com.core.model.Friend;
import com.core.model.OAConfig;
import com.core.model.WorkModel;
import com.core.net.utils.NetUtils;
import com.core.utils.ToastUtil;
import com.core.utils.sortlist.BaseSortModel;
import com.core.widget.DrawableCenterTextView;
import com.modular.appmessages.activity.ProcessB2BActivity;
import com.modular.appmessages.activity.ProcessMsgActivity;
import com.modular.appmessages.activity.Subscription2Activity;
import com.modular.appmessages.adapter.MessageNewAdapter;
import com.modular.appmessages.model.MessageHeader;
import com.modular.appmessages.model.MessageNew;
import com.modular.appmessages.presenter.MessagePresenter;
import com.modular.appmessages.presenter.imp.IMessageView;
import com.modular.appmessages.widget.SignRefreshLayout;
import com.modular.apputils.adapter.LinearItemDecoration;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.apputils.widget.WrapContentLinearLayoutManager;
import com.uas.appme.pedometer.view.UURanking;
import com.uas.appworks.OA.erp.activity.ChangeMobileActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.platform.task.TaskActivity;
import com.xzjmyk.pm.activity.ui.platform.task.TaskB2BActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Bitliker on 2017/3/1.
 */
public class MessageFragment extends SupportToolBarFragment implements IMessageView, View.OnClickListener {

    private DrawableCenterTextView message_net_set;
    private SignRefreshLayout mSignRefreshLayout;
    private RecyclerView mRecyclerView;

    //打卡界面
    private View signView;
    private ImageButton itemSignImage;
    private AppCompatTextView itemWorkTv;
    private AppCompatTextView itemOffkTv;

    private MessageNewAdapter mAdapter;
    private BaseActivity mContext;
    private MessagePresenter presenter;
    private PopupWindow setWindow;
    private int clickPosition = 0;
    private Boolean platform;

    private Comparator<BaseSortModel<Friend>> comparator;
    private MessagePresenter.UnReaderListener unReaderListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            mContext = (BaseActivity) context;
        }
        if (context instanceof MessagePresenter.UnReaderListener) {
            unReaderListener = (MessagePresenter.UnReaderListener) context;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroyView(mContext);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.quick_sign) {
           setSignViewData(false);

        }
        return super.onOptionsItemSelected(item);
    }

    public void switchLanguageAction() {
        Intent it = new Intent("com.modular.main.MainActivity");
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_message;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            platform = ApiUtils.getApiModel() instanceof ApiPlatform;
            initView();
            initEvent();
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadData();
        if (PreferenceUtils.getBoolean("hasAutoSign", false)) {
            setSignViewData(true);
            PreferenceUtils.putBoolean("hasAutoSign", false);
        }
    }

    private MessageNewAdapter.ItemTouchListener mItemTouchListener = new MessageNewAdapter.ItemTouchListener() {
        @Override
        public void longClick(int id) {


            clickPosition = id - mAdapter.getHeaderSize();
            if (clickPosition < 0) clickPosition = 0;
            showPopupWindow();
        }

        @Override
        public void click(int id, MessageNew messageNew) {
            if (messageNew != null && messageNew.getT() != null) {
                if (messageNew.getT() instanceof MessageHeader) {
                    MessageHeader model = (MessageHeader) messageNew.getT();
                    if (model != null) {
                        if (!model.isHideRed()) {//如果没有被隐藏，需要更新
                            model.hideRed();
                            mAdapter.notifyItemChanged(id);
                        }
                        presenter.turn2ActByHeader(ct, model);
                    }
                } else {
                    presenter.turn2NextAct(mContext, id - mAdapter.getHeaderSize());
                }
            }
        }
    };


    private void initEvent() {
        message_net_set.setOnClickListener(this);
        mSignRefreshLayout.setOnRefreshListener(new SignRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadData();
            }
        });

    }


    private void initView() {
        Toolbar toolbar = getCommonToolBar();
        if (toolbar != null) {
            toolbar.setBackgroundResource(R.drawable.common_toolbar_message_bg);
        }
        setTitle(R.string.contact_title);
        message_net_set = findViewById(R.id.message_net_set);
        mSignRefreshLayout = findViewById(R.id.mSignRefreshLayout);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(ct, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new LinearItemDecoration(ct));
        presenter = new MessagePresenter(mContext, this, unReaderListener);
        signView = mSignRefreshLayout.getSignView();
        if (signView != null) {
            itemSignImage = signView.findViewById(R.id.itemSignImage);
            itemWorkTv = signView.findViewById(R.id.itemWorkTv);
            itemOffkTv = signView.findViewById(R.id.itemOffkTv);
            itemSignImage.setOnClickListener(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x223) {
            if (resultCode == getActivity().RESULT_OK) {
                sign(true);
            }
        }

    }

    public void showProgress() {
        if (mContext != null && mContext.progressDialog != null) {
            mContext.progressDialog.show();
        }
    }

    private void setSignViewData(boolean muchShow) {
        if (mContext != null && mContext.progressDialog != null) {
            mContext.progressDialog.dismiss();
        }
        WorkModel work = presenter.getCurrentWork();
        if (work != null) {
            if (itemWorkTv != null) {
                if (TextUtils.isEmpty(work.getWorkSignin())) {
                    itemWorkTv.setTextColor(getResources().getColor(R.color.message_not_sign_time));
                    itemWorkTv.setText("未打卡（" + work.getWorkTime() + "）");
                } else {
                    itemWorkTv.setText(work.getWorkSignin() + ":00");
                    itemWorkTv.setTextColor(getResources().getColor(R.color.white));
                }
            }
            if (itemOffkTv != null) {
                if (TextUtils.isEmpty(work.getOffSignin())) {
                    itemOffkTv.setTextColor(getResources().getColor(R.color.message_not_sign_time));
                    itemOffkTv.setText("未打卡（" + work.getOffTime() + "）");
                } else {
                    itemOffkTv.setText(work.getOffSignin() + ":00");
                    itemOffkTv.setTextColor(getResources().getColor(R.color.white));
                }
            }
            mSignRefreshLayout.setTag(work);
            mSignRefreshLayout.setSignShow(muchShow);
        } else {
            ToastUtil.showToast(ct, "未找到班次！！", getContentView());
        }
    }

    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(mContext.getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(mContext, 0.4f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.msgs_long_click, null);
        viewContext.findViewById(R.id.msg_delete_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.deleteListByType(clickPosition);
                closePopupWindow();
            }
        });
        viewContext.findViewById(R.id.msg_markread_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.readerAllByType(clickPosition);
                closePopupWindow();
            }
        });
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
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
        DisplayUtil.backgroundAlpha(mContext, 1f);
    }


    //点击时间回调
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message_net_set://点击无网络情况
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            case R.id.schedule_rl://审批流
                if (platform) {
                    startActivity(new Intent(mContext, ProcessB2BActivity.class));
                } else {
                    LogUtil.d("arison","点击我的审批界面");
                    startActivity(new Intent(mContext, ProcessMsgActivity.class));
                }
                break;
            case R.id.waitting_work_rl://待办工作
                if (platform) {
                    startActivity(new Intent(mContext, TaskB2BActivity.class));
                } else {
                    startActivity(new Intent(mContext, TaskActivity.class));
                }
                break;
            case R.id.subscribe_rl://我的订阅
                presenter.setSubReadTime(DateFormatUtil.long2Str(DateFormatUtil.YMD));
                startActivity(new Intent(mContext, Subscription2Activity.class));
                break;
            case R.id.msg_delete_tv://删除
                break;
            case R.id.msg_markread_tv://标为已读
                
                break;
            case R.id.uustep_rl:
                startActivity(new Intent(mContext, UURanking.class));
                PreferenceUtils.putBoolean(Constants.UU_STEP_RED, true);
                break;
            case R.id.itemSignImage:

                if (OAConfig.needValidateFace) {
                    String[] permissions = new String[]{Manifest.permission.CAMERA};
                    if (PermissionUtil.lacksPermissions(ct, permissions)) {
                        requestPermissions(permissions, PermissionUtil.DEFAULT_REQUEST);
                    } else {
                        if (presenter.isCanPaly()) {
                            startActivityForResult(new Intent(ct, RealTimeDetectFaceActivty.class), 0x223);
                        }
                    }
                } else {
                    sign(false);
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.DEFAULT_REQUEST) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.showToast(ct, com.uas.appworks.R.string.not_camera_permission);
            } else {
                if (presenter.isCanPaly()) {
                    startActivityForResult(new Intent(ct, RealTimeDetectFaceActivty.class), 0x223);
                }
            }
        }
    }

    private void sign(boolean needMac) {
        WorkModel work = null;
        if (mSignRefreshLayout != null && mSignRefreshLayout.getTag() != null) {
            Object tag = mSignRefreshLayout.getTag();
            if (tag instanceof WorkModel) {
                work = (WorkModel) tag;
            }
        }
        if (NetUtils.isNetWorkConnected(ct)) {
            presenter.signWork(needMac, work);
        } else {
            showToact(R.string.networks_out);
        }
    }

    @Override
    public void showModel(List<BaseSortModel<Friend>> models) {
        sortModels(models);
        if (mSignRefreshLayout.isEnablePullDown() && mSignRefreshLayout.isRefreshing()) {
            mSignRefreshLayout.stopRefresh();
        }
        List<MessageNew> messageNews = new ArrayList<>();
        if (!ListUtils.isEmpty(models)) {
            for (BaseSortModel<Friend> e : models) {
                MessageNew<BaseSortModel<Friend>> t = new MessageNew<>();
                t.setT(e);
                t.setType(1);
                messageNews.add(t);
            }
        }
        if (mAdapter == null) {
            mAdapter = new MessageNewAdapter(ct);
            mAdapter.setContentModels(messageNews);
            mAdapter.setItemTouchListener(mItemTouchListener);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setContentModels(messageNews);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void sortModels(List<BaseSortModel<Friend>> models) {
        if (ListUtils.isEmpty(models)) return;
        if (comparator == null)
            comparator = new Comparator<BaseSortModel<Friend>>() {
                @Override
                public int compare(BaseSortModel<Friend> lhs, BaseSortModel<Friend> rhs) {
                    return rhs.getBean().getTimeSend() - lhs.getBean().getTimeSend();
                }
            };
        Collections.sort(models, comparator);
    }


    @Override
    public void clearSearch() {
    }


    @Override
    public void updateHeaderView(int type, int num, String subTitle, String time) {
        if (mAdapter != null && !ListUtils.isEmpty(mAdapter.getModels())) {
            boolean isUpdated = false;
            if (type != MessagePresenter.REAL_TIME_FORM && type != MessagePresenter.BUSINESS_STATISTICS) {
                for (int i = 0; i < mAdapter.getModels().size(); i++) {
                    MessageNew e = mAdapter.getModels().get(i);
                    if (e != null && e.getT() != null && e.getT() instanceof MessageHeader) {
                        MessageHeader h = (MessageHeader) e.getT();
                        if (h.getType() == type) {
                            isUpdated = true;
                            h.setSubDoc(subTitle);
                            h.setRedNum(num);
                            h.setTime(time);
                            mAdapter.notifyItemChanged(i, e);
                            break;
                        }
                    }
                }
            }
            if (type == MessagePresenter.REAL_TIME_FORM || type == MessagePresenter.BUSINESS_STATISTICS) {
                updateHideOrDisplay(num, isUpdated, type);
            }
        }
        if (mSignRefreshLayout.isEnablePullDown() && mSignRefreshLayout.isRefreshing()) {
            mSignRefreshLayout.stopRefresh();
        }
    }

    //更新是否显示红点
    private boolean updateHideOrDisplay(int num, boolean isUpdated, int type) {
        boolean isExist = false;
        int position = -1;
        for (int i = 0; i < mAdapter.getModels().size(); i++) {
            MessageNew model = mAdapter.getModels().get(i);
            if (model != null && model.getT() != null && model.getT() instanceof MessageHeader) {
                MessageHeader messageHeader = (MessageHeader) model.getT();
                if (messageHeader.getType() == type) {
                    position = i;
                    isExist = true;
                    break;
                }
            }

        }
        if (num > 0) {
            if (!isExist) {
                isUpdated = true;
                if (type == MessagePresenter.REAL_TIME_FORM) {
                    MessageHeader model = new MessageHeader(getString(R.string.real_time_form));
                    model.setIcon(R.drawable.ic_real_time_form);
                    model.setRedKey(Constants.MESSAGE_REAL_TIME);
                    model.setType(MessagePresenter.REAL_TIME_FORM);
                    MessageNew news = new MessageNew();
                    news.setT(model);
                    mAdapter.addHeadModel(2, news);
                } else if (type == MessagePresenter.BUSINESS_STATISTICS) {
                    MessageHeader model = new MessageHeader(StringUtil.getMessage(R.string.business_statistics));
                    model.setIcon(R.drawable.ic_business_statistics);
                    model.setRedKey(Constants.MESSAGE_BUSINESS_STATISTICS);
                    model.setType(MessagePresenter.BUSINESS_STATISTICS);
                    MessageNew news = new MessageNew();
                    news.setT(model);
                    mAdapter.getModels().add(1000, news);
                }
            }
        } else {
            isUpdated = true;
            if (position >= 0 && position <= mAdapter.getModels().size()) {
                mAdapter.getModels().remove(position);
            }
        }
        if (isUpdated) {
            mAdapter.notifyDataSetChanged();
        }
        return isUpdated;
    }

    @Override
    public void changeNet(boolean workConnected) {
        if (message_net_set != null) {
            if (!workConnected) {
                message_net_set.setVisibility(View.VISIBLE);
            } else {
                message_net_set.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void updateHeader(List<MessageNew> models) {
        if (mAdapter == null) {
            mAdapter = new MessageNewAdapter(ct);
            mAdapter.setItemTouchListener(mItemTouchListener);
            mAdapter.setHeaderModels(models);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setHeaderModels(models);
        }
    }

    @Override
    public void updateSign(String message) {
        if (message.equals(getString(R.string.show_frist_mac))) {
            showDialog(1, message);
        } else if (message.contains("不是考勤打卡常用设备,是否需要更换")) {
            showDialog(2, getString(R.string.other_phone_error));
        } else if (message.contains("设备正处于申请变更绑定阶段")) {
            showToact(R.string.mac_changing);
        } else if (message.contains("该设备已被他人绑定")) {
            showToact(R.string.mac_other);
        } else {
            ToastUtil.showToast(ct, message, getContentView());
        }
        setSignViewData(true);
    }

    public void showToact(int resIds) {
        ToastUtil.showToast(ct, resIds, getContentView());
    }

    /**
     * 显示提示框
     *
     * @param type    1.第一次绑定mac  2.mac错误，进入修改
     * @param message
     */
    private void showDialog(final int type, final String message) {
        PopupWindowHelper.showAlart(getActivity(), getString(R.string.app_name), message, new PopupWindowHelper.OnSelectListener() {
            @Override
            public void select(boolean selectOk) {

                if (selectOk) {
                    switch (type) {
                        case 1:
                            sign(false);
                            break;
                        case 2:
                            Intent intent = new Intent(ct, ChangeMobileActivity.class);
                            intent.putExtra("macAddress", presenter.getMac());
                            startActivity(intent);
                            break;
                    }

                }
            }
        });
    }

}
