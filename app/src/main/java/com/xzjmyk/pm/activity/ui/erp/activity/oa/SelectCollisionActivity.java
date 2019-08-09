package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.os.Bundle;
import android.text.Editable;
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
import com.core.base.OABaseActivity;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.SideBar;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.adapter.oa.SelectCollisionAdapter;
import com.xzjmyk.pm.activity.ui.erp.presenter.SelectCollisionPresenter;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.ISelectActiveView;

import java.util.List;

public class SelectCollisionActivity extends OABaseActivity implements ISelectActiveView {
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
    private SelectCollisionAdapter adapter;
    private SelectCollisionPresenter presenter;
    private int allSelect = 0;
    private boolean isClickCb = true;
    private EmptyLayout emptyLayout;
    private SelectCollisionTurnBean selectBean;
    private RelativeLayout rl_empty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_active);
        ViewUtils.inject(this);
        initEvent();
        initView();
    }

    private void initView() {
        rl_empty = findViewById(R.id.rl_empty);
        rl_empty.setVisibility(View.GONE);
        sidebar.setTextView(text_dialog);
        presenter = new SelectCollisionPresenter(this);
        if (getIntent() != null) {
            selectBean = getIntent().getParcelableExtra(OAConfig.MODEL_DATA);
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
        presenter.start(selectBean);
    }

    private void initEvent() {
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    presenter.search(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.sure(SelectCollisionActivity.this);
            }
        });
        sidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (adapter == null || ListUtils.isEmpty(adapter.getListData()) || StringUtil.isEmpty(s)) return;
                // 该字母首次出现的位置
                if ("↑".equals(s)) {
                    listView.setSelection(0);
                    return;
                }
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
                    presenter.sureSingle(SelectCollisionActivity.this, adapter.getListData().get(position).bean);
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
        adapter = new SelectCollisionAdapter(models);
        if (ListUtils.isEmpty(models)) {
            rl_empty.setVisibility(View.VISIBLE);
            sidebar.setVisibility(View.GONE);
            select_rl.setVisibility(View.GONE);
        } else {
            rl_empty.setVisibility(View.GONE);
            if (selectBean.getTitle().equals(getString(R.string.select_share_friend))){
                sidebar.setVisibility(View.GONE);
                adapter.setBaseSortEnable(false);
            }else {
                adapter.setBaseSortEnable(true);
                sidebar.setVisibility(View.VISIBLE);
            }
            if (!selectBean.isSingleAble())
                select_rl.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(adapter);
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


}
