package com.core.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.common.LogUtil;
import com.core.app.R;
import com.core.utils.CommonUtil;
import com.core.utils.StatusBarUtil;
import com.core.utils.ToastUtil;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

/**
 * Created by Bitlike on 2018/5/3.
 */
public class BaseToolBarActivity extends AppCompatActivity {
    private FrameLayout contentFl;
    private Toolbar commonToolBar;
    private TextView commonTitleTv;
    protected ACProgressFlower loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.base_bar_layout);
        loadingView = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY).build();

        contentFl = findViewById(R.id.contentFl);
        int rceId = getLayoutRes();
        if (rceId > 0) {
            setContentView(rceId);
        }
        if (needCommonToolBar()) {
            initCommonToolbar(getToolBarId());
        }
    }

    @Override
    protected void onPause() {
        CommonUtil.closeKeybord(this);
        super.onPause();
    }

    public CharSequence getToolBarTitle() {
        if (commonTitleTv != null && !TextUtils.isEmpty(commonTitleTv.getText())) {
            return commonTitleTv.getText();
        } else if (getSupportActionBar() != null && !TextUtils.isEmpty(getSupportActionBar().getTitle())) {
            return getSupportActionBar().getTitle();
        }
        return "";
    }

    public int getToolBarId() {
        return R.id.toolbarVs;
    }

    public void setCommonToolBar(Toolbar commonToolBar) {
        this.commonToolBar = commonToolBar;
    }

    public void initCommonToolbar(int toolBarId) {
        if (commonToolBar != null || toolBarId != R.id.toolbarVs) {
            if (commonToolBar == null) {
                commonToolBar = findViewById(toolBarId);
            }
            initCommonToolbar();
        } else {
            ViewStub stub = findViewById(R.id.toolbarVs);
            stub.inflate();
            commonToolBar = findViewById(R.id.commonToolBar);
            commonTitleTv = findViewById(R.id.commonTitleTv);
            initCommonToolbar();
            if (needNavigation()) {
                setNavigation(0, null, null);
            }
            if (commonTitleTv != null) {
                commonTitleTv.setText(getTitle());
            }
        }
    }

    public void initCommonToolbar() {
        LogUtil.d("arison", "commonToolBar=" + commonToolBar);
        if (commonToolBar != null) {
            StatusBarUtil.immersive(this, 0x00000000, 0.0f);
            setSupportActionBar(commonToolBar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            LogUtil.d("arison", "标题栏设置padding");
            StatusBarUtil.setPaddingSmart(this, commonToolBar);
            commonToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    onOptionsItemSelected(menuItem);
                    return false;
                }
            });
        }
    }

    /**
     * 初始化导航键
     *
     * @param iconId          返回图标（优先）
     * @param icon            返回图标（当iconId小于0时）
     * @param onClickListener 导航键监听事件
     */
    public final void setNavigation(int iconId, Drawable icon, View.OnClickListener onClickListener) {
        if (iconId > 0) {
            commonToolBar.setNavigationIcon(iconId);
        } else if (icon != null) {
            commonToolBar.setNavigationIcon(icon);
        } else {
            commonToolBar.setNavigationIcon(R.drawable.ic_baseutil_back);
        }
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            };
        }
        //设置返回按钮的点击事件
        commonToolBar.setNavigationOnClickListener(onClickListener);
        setToolBarMenuClickListener(null);
    }

    //是否需要返回键
    public boolean needNavigation() {
        return true;
    }

    //是否需要通用的toolbar
    public boolean needCommonToolBar() {
        return true;
    }

    /*隐藏toolbar*/
    protected final void hideToolBar() {
        if (commonToolBar != null) {
            commonToolBar.setVisibility(View.GONE);
        }
    }

    /*显示toolbar*/
    public final void showToolBar() {
        if (commonToolBar != null && commonToolBar.getVisibility() == View.GONE) {
            commonToolBar.setVisibility(View.VISIBLE);
        }
    }

    /*设置toolbar的点击监听器*/
    public final void setToolBarMenuClickListener(Toolbar.OnMenuItemClickListener onclick) {
        if (commonToolBar != null) {
            if (onclick == null) {
                onclick = new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        onOptionsItemSelected(item);
                        return false;
                    }
                };
            }
            commonToolBar.setOnMenuItemClickListener(onclick);
        }
    }

    public final void setTitle(int titleId) {
        super.setTitle(titleId);
        if (titleId > 0) {
            if (commonTitleTv != null) {
                commonTitleTv.setText(titleId);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(titleId);
            }
        }

    }

    /*设置标题*/
    public final void setTitle(CharSequence title) {
        super.setTitle(title);
        if (title != null) {
            if (commonTitleTv != null) {
                commonTitleTv.setText(title);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    /**
     * 更改为自定义的ToolBar，此时默认的Toobar将会制空
     * TODO 存在问题：前面如果设置的返回键监听事件会失效，发现该方法没用
     *
     * @param toolBarId 当前布局里toolbar的资源id
     * @return 返回新的Toobar
     */
    @Deprecated
    public Toolbar updataToolBar(int toolBarId) {
        Drawable navigationIcon = null;
        if (commonToolBar != null) {
            if (needNavigation()) {
                navigationIcon = commonToolBar.getNavigationIcon();
            }
        }
        commonToolBar = contentFl.findViewById(toolBarId);
        if (commonToolBar != null) {
            setSupportActionBar(commonToolBar);
            //设置actionBar的标题是否显示，对应ActionBar.DISPLAY_SHOW_TITLE。
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            if (needNavigation()) {
                setNavigation(0, navigationIcon, null);
            }
        }
        return commonToolBar;
    }

    public int getLayoutRes() {
        return 0;
    }

    @Override
    public void setContentView(View contentView) {
        setContentView(contentView, null);
    }

    @SuppressLint("ResourceType")
    @Override
    public void setContentView(@LayoutRes int layoutId) {
        if (layoutId > 0) {
            if (contentFl != null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(layoutId, contentFl);
            } else {
                super.setContentView(layoutId);
            }
        }
    }

    @Override
    public void setContentView(View contentView, ViewGroup.LayoutParams params) {
        if (contentView != null) {
            if (contentFl != null) {
                contentFl.removeAllViews();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                }
                contentFl.addView(contentView, params);
            } else {
                super.setContentView(contentView);
            }
        }
    }


    public void showToast(CharSequence message) {
        if (contentFl != null && !TextUtils.isEmpty(message)) {
            ToastUtil.showToast(this, message, contentFl);
        } else {
            ToastUtil.showToast(this, message);
        }
    }

    public void showToast(int messageId) {
        if (contentFl != null && messageId != 0) {
            ToastUtil.showToast(this, messageId, contentFl);
        } else {
            ToastUtil.showToast(this, messageId);
        }
    }

    public FrameLayout getContentFl() {
        return contentFl;
    }

    public Toolbar getCommonToolBar() {
        return commonToolBar;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
