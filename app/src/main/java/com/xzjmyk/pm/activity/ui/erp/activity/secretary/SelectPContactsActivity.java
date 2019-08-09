package com.xzjmyk.pm.activity.ui.erp.activity.secretary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.PermissionUtil;
import com.core.base.OABaseActivity;
import com.core.model.OAConfig;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.SideBar;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.adapter.oa.SelectPCollisionAdapter;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.ISelectActiveView;

import java.util.List;


/**
 * Created by Arison on 2017/6/19.
 * 选择手机本地通讯录联系人
 */

public class SelectPContactsActivity extends OABaseActivity implements ISelectActiveView, SelectPCollisionAdapter.OnStatusClickListener {

    @ViewInject(R.id.recyclerview)
    private ListView listView;
    @ViewInject(R.id.select_rl)
    private RelativeLayout select_rl;
    @ViewInject(R.id.voiceSearchView)
    private VoiceSearchView voiceSearchView;
    @ViewInject(R.id.sidebar)
    private SideBar sidebar;
    @ViewInject(R.id.text_dialog)
    private TextView text_dialog;
    @ViewInject(R.id.mumber_tv)
    private TextView mumber_tv;
    @ViewInject(R.id.sure_tv)
    private TextView sure_tv;
    @ViewInject(R.id.all_sure_cb)
    private CheckBox all_sure_cb;

    private SelectPCollisionAdapter adapter;
    private SelectPContactsPresenter presenter;
    private int allSelect = 0;
    private boolean isClickCb = true;
    private EmptyLayout emptyLayout;
    private SelectCollisionTurnBean selectBean;
    private int type;
    private boolean isMenuShuffle=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_active);
        ViewUtils.inject(this);
        initEvent();
        initView();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(isMenuShuffle)
        {
            menu.findItem(R.id.search).setVisible(true);
            menu.findItem(R.id.search).setIcon(getResources().getDrawable(R.drawable.icon_new_friend));
        }else
        {
            menu.findItem(R.id.search).setVisible(false);
           
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nearby, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            Intent intent = new Intent(ct, SelectPContactsActivity.class);
            SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                    .setTitle(getString(R.string.app_local_contacts))
                    .setSingleAble(true)
                    .setSelectCode(null);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            intent.putExtra("isMenuShuffle",false);
            intent.putExtra("type",0);
            startActivityForResult(intent, 0x01);
        }

        if (item.getItemId() == android.R.id.home) {
           onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
    

    @Override
    protected void onResume() {
        super.onResume();
        String[] permissions = {Manifest.permission.READ_CONTACTS};
        if (PermissionUtil.lacksPermissions(ct, permissions)) {
            PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permissions);
        } else {
            presenter.start(selectBean, getType());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.DEFAULT_REQUEST) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //TODO 没有获取到权限
                LogUtil.i("没有获取到权限");
                ToastUtil.showToast(ct,R.string.not_system_permission);
            } else {
                LogUtil.i("已经用户赋予权限获取到权限");
                
                presenter.start(selectBean, type);
                
            }
        }
    }

    private int getType() {
        if (getIntent() != null) {
            return getIntent().getIntExtra("type", 0);
        } else {
            return 0;
        }
    }

    private void initView() {
        emptyLayout = new EmptyLayout(ct, listView);
        emptyLayout.setShowLoadingButton(false);
        emptyLayout.setShowEmptyButton(false);
        emptyLayout.setShowErrorButton(false);
        emptyLayout.setEmptyViewRes(R.layout.view_empty);
        sidebar.setTextView(text_dialog);
        presenter = new SelectPContactsPresenter(this);
        if (getIntent() != null) {
            selectBean = getIntent().getParcelableExtra(OAConfig.MODEL_DATA);
            isMenuShuffle=getIntent().getBooleanExtra("isMenuShuffle",false);
            type= getType();
            if (selectBean == null) {
                LogUtil.i("selectBean == null");
                new NullPointerException("selectBean cannot be Null");
            } else if (!StringUtil.isEmpty(selectBean.getTitle()))
               setTitle(selectBean.getTitle());
            select_rl.setVisibility(selectBean.isSingleAble() ? View.GONE : View.VISIBLE);
        } else {
            LogUtil.i("selectBean == null");
            new NullPointerException("Intent cannot be Null");
        }


    }


    private void initEvent() {
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                //TODO 搜索
                presenter.search(s);
            }
        });
        findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.sure(SelectPContactsActivity.this);
            }
        });
        sidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                if ("↑".equals(s)) {
                    listView.setSelection(0);
                    return;
                }
                if(adapter==null)return;
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    listView.setSelection(position);
                }
            }

            @Override
            public void onTouchingUp() {
            }
        });
        all_sure_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isClickCb) {
                    if (adapter == null) return;
                    presenter.changeChecked(b, adapter.getListData());
                    all_sure_cb.setText(b ? R.string.cancel_select_all : R.string.select_all);
                }
                isClickCb = true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter == null) return;
                if (selectBean.isSingleAble()) {
                    if (ListUtils.isEmpty(adapter.getListData()) || position >= adapter.getListData().size()) {
                        return;
                    }
                    presenter.sureSingle(SelectPContactsActivity.this, adapter.getListData().get(position).bean);
                } else {
                    boolean isClicked = !adapter.getListData().get(position).isClick();
                    setSelectNumber(isClicked);
                    adapter.getListData().get(position).setClick(isClicked);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public void addExist(String firstLetter) {
        sidebar.addExist(firstLetter);
    }

    @Override
    public void showModel(List<BaseSortModel<SelectEmUser>> models) {
        adapter = new SelectPCollisionAdapter(models, this);
        adapter.setType(type);
        listView.setAdapter(adapter);
        
        if (ListUtils.isEmpty(models)) {
            emptyLayout.showEmpty();
            sidebar.setVisibility(View.GONE);
            select_rl.setVisibility(View.GONE);
        } else {
            sidebar.setVisibility(View.VISIBLE);
            if (!selectBean.isSingleAble())
                select_rl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showNumber(int number) {
        allSelect = number;
        mumber_tv.setText(getString(R.string.selected) + number + " " + selectBean.getSelectType());
    }

    @Override
    public void showSureText(String text) {
        sure_tv.setText(text);
    }

    @Override
    public void isAllClicked(boolean clickAll) {
        if (clickAll) {
            isClickCb = false;
            all_sure_cb.setChecked(clickAll);
        }
        all_sure_cb.setText(clickAll ? R.string.cancel_select_all : R.string.select_all);
    }

    /**
     * 点击后
     *
     * @param isClicked 点击后是否为选中状态
     */
    private void setSelectNumber(boolean isClicked) {
        allSelect += isClicked ? 1 : -1;
        if (allSelect < 0)
            allSelect = 0;
        if (!isClicked && all_sure_cb.isChecked()) {
            isClickCb = false;
            all_sure_cb.setChecked(false);
            all_sure_cb.setText(R.string.select_all);
        } else if (isClicked && !all_sure_cb.isChecked() && !ListUtils.isEmpty(adapter.getListData()) && allSelect == adapter.getListData().size()) {
            isClickCb = false;
            all_sure_cb.setChecked(true);
            all_sure_cb.setText(R.string.cancel_select_all);
        }
        mumber_tv.setText(getString(R.string.selected) + allSelect + " " + selectBean.getSelectType());
    }

    @Override
    public void onClick(SelectEmUser user, int position, String message) {
        presenter.onClickStatus(user, position, message);
    }
}
