package com.modular.apputils.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.system.SystemUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;
import com.core.widget.view.MyGridView;
import com.me.imageloader.ImageLoaderUtil;
import com.modular.apputils.R;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.model.BillJump;
import com.modular.apputils.network.FileDownloader;
import com.modular.apputils.utils.BillTypeChangeUtils;
import com.modular.apputils.utils.OpenFilesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillDetailsAdapter extends RecyclerView.Adapter<BillDetailsAdapter.BaseViewHolder> {

    private Context ct;
    private List<BillGroupModel> mBillGroupModels;
    private List<BillGroupModel.BillModel> mShowBillModels;
    private List<BillGroupModel.BillModel> mUpdateBillModels;
    protected int mTabIndex = -1;
    private int mTabPosition;
    private List<BillGroupModel> mOldTabModels;

    public BillDetailsAdapter(Context ct, List<BillGroupModel> mBillGroupModels) {
        this.ct = ct;
        this.mBillGroupModels = mBillGroupModels;
        changeBillModel();
    }

    public void updateGroupModels(List<BillGroupModel> groupModels) {
        this.mBillGroupModels = groupModels;
        changeBillModel();
    }

    public BillGroupModel getBillGroupModel(int groupIndex) {
        if (ListUtils.isEmpty(mBillGroupModels)) {
            return null;
        } else {
            for (BillGroupModel billGroupModel : mBillGroupModels) {
                if (billGroupModel.getGroupIndex() == groupIndex) {
                    return billGroupModel;
                }
            }
            return null;
        }
    }

    public List<BillGroupModel.BillModel> getShowBillModels() {
        return mShowBillModels;
    }

    public List<BillGroupModel.BillModel> getUpdateBillModels() {
        return mUpdateBillModels;
    }

    public List<BillGroupModel> getBillGroupModels() {
        return mBillGroupModels;
    }

    /**
     * 当外界的因素引起mBillGroupModels变化时候，通过遍历将mBillGroupModels转成mShowBillModels进行显示
     */
    private void changeBillModel() {
        if (mShowBillModels == null) {
            mShowBillModels = new ArrayList<>();
        } else {
            mShowBillModels.clear();
        }
        if (mUpdateBillModels == null) {
            mUpdateBillModels = new ArrayList<>();
        } else {
            mUpdateBillModels.clear();
        }

        for (int i = 0; i < mBillGroupModels.size(); i++) {
            BillGroupModel e = mBillGroupModels.get(i);
            if (e != null) {
                List<BillGroupModel.GridTab> gridTabs = e.getGridTabs();
                if (gridTabs != null && gridTabs.size() > 0) {
                    BillGroupModel.BillModel mTabBillModel = new BillGroupModel.BillModel();
                    mTabBillModel.setType(BillGroupModel.Constants.TYPE_TAB);
                    mTabBillModel.setTabList(gridTabs);
                    mTabBillModel.setGroupIndex(i);
                    mShowBillModels.add(mTabBillModel);
                } else if (e.getShowBillFields() != null && !e.getShowBillFields().isEmpty()) {
                    if (!TextUtils.isEmpty(e.getGroup())) {
                        BillGroupModel.BillModel mTitleBillModel = new BillGroupModel.BillModel();
                        mTitleBillModel.setGroupIndex(i);
                        mTitleBillModel.setType(BillGroupModel.Constants.TYPE_TITLE);
                        mTitleBillModel.setCaption(e.getGroup());
                        mTitleBillModel.setAllowBlank(e.isDeleteAble() ? "T" : "F");
                        mShowBillModels.add(mTitleBillModel);
                    }
                    mShowBillModels.addAll(e.getShowBillFields());

                    if (e.getUpdateBillFields() != null && !e.getUpdateBillFields().isEmpty()) {
                        mUpdateBillModels.addAll(e.getUpdateBillFields());
                    }
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowBillModels != null && mShowBillModels.size() > position) {
            int itemViewType = BillTypeChangeUtils.getItemViewType(mShowBillModels.get(position).getType());
            if (itemViewType == 111) {
                mTabIndex = position;
            }
            return itemViewType;
        }
        return super.getItemViewType(position);
    }

    private LayoutInflater mLayoutInflater;

    public LayoutInflater getLayoutInflater() {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(ct);
        }
        return mLayoutInflater;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = null;
        switch (viewType) {
            case 0:
                viewHolder = new TitleViewHolder(parent, R.layout.item_bill_title);
                break;
            case 1:
                viewHolder = new TextViewHolder(parent, R.layout.item_bill_details);
                break;
            case 2:
                viewHolder = new EnclosureViewHolder(parent, R.layout.item_bill_enclosure);
                break;//附件类型
            case 111:
                viewHolder = new TabViewHolder(parent, R.layout.item_bill_tab);
                break;
            default:
                viewHolder = new BaseViewHolder(parent, R.layout.item_bill_title) {
                    @Override
                    public void initView(View view) {

                    }
                };
        }
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(mShowBillModels);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        BillGroupModel.BillModel model = null;
        if (mShowBillModels != null && mShowBillModels.size() > position) {
            model = mShowBillModels.get(position);
        }
        try {
            if (holder instanceof TextViewHolder) {
                bindText((TextViewHolder) holder, model, position);
            } else if (holder instanceof TitleViewHolder) {
                bindTitleView((TitleViewHolder) holder, model, position);
            } else if (holder instanceof EnclosureViewHolder) {
                bindEnclosure((EnclosureViewHolder) holder, model, position);
            } else if (holder instanceof TabViewHolder) {
                bindTabView((TabViewHolder) holder, model, position);
            }
        } catch (Exception e) {
            LogUtil.i("gong", position + "  e=" + e.getMessage());
        }

    }

    private void bindTabView(TabViewHolder holder, final BillGroupModel.BillModel model, final int position) {
        if (holder.mTabLayout.getTag() != null && holder.mTabLayout.getTag() instanceof MyTabChangeListener) {
            holder.mTabLayout.removeOnTabSelectedListener((TabLayout.OnTabSelectedListener) holder.mTabLayout.getTag());
        }
        MyTabChangeListener myTabChangeListener = new MyTabChangeListener(holder.mTabLayout, position);
        holder.mTabLayout.addOnTabSelectedListener(myTabChangeListener);
        holder.mTabLayout.setTag(myTabChangeListener);
    }

    public void bindTitleView(TitleViewHolder mTitleViewHolder, BillGroupModel.BillModel model, int position) throws Exception {
        if (model != null) {
            if (model.getAllowBlank().equals("F")) {
                mTitleViewHolder.deleteTv.setVisibility(View.GONE);
                mTitleViewHolder.deleteTv.setOnClickListener(null);
            } else {
                mTitleViewHolder.deleteTv.setVisibility(View.VISIBLE);
                mTitleViewHolder.deleteTv.setTag(model.getGroupIndex());
                mTitleViewHolder.deleteTv.setOnClickListener(null);
            }
            mTitleViewHolder.tvTitle.setText(model.getCaption());
        }
    }

    private void bindEnclosure(EnclosureViewHolder holder, BillGroupModel.BillModel model, int position) {
        holder.captionTv.setText(model.getCaption());
        holder.muchInputTv.setVisibility(model.getAllowBlank().equals("T") ? View.GONE : View.VISIBLE);
        EnclosureAdapter mEnclosureAdapter = null;
        if (!ListUtils.isEmpty(model.getLocalDatas())) {
            if (holder.ffGv.getTag(R.id.tag_key) != null && holder.ffGv.getTag(R.id.tag_key) instanceof EnclosureAdapter) {
                mEnclosureAdapter = (EnclosureAdapter) holder.ffGv.getTag(R.id.tag_key);
                mEnclosureAdapter.setLocalDatas(model.getLocalDatas());
                mEnclosureAdapter.setPosition(position);
            } else {
                mEnclosureAdapter = new EnclosureAdapter(position, model.getLocalDatas());
            }
        }
        holder.ffGv.setAdapter(mEnclosureAdapter);
    }

    private void bindText(TextViewHolder mViewHolder, BillGroupModel.BillModel field, int position) {
        mViewHolder.captionTv.setText(field.getCaption());
        mViewHolder.valuesTv.setText(field.getValue());
        BillJump mBillJump = field.getBillJump();
        if (mBillJump != null) {
            mViewHolder.valuesTv.setTag(R.id.tag_key, mBillJump);
            mViewHolder.valuesTv.setOnClickListener(mOnClickListener);
        } else if (field.getCaption().contains("手机") || field.getCaption().contains("电话")) {
//            mViewHolder.valuesTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            mViewHolder.valuesTv.setTag(R.id.tag_key, 1);
            mViewHolder.valuesTv.setTag(field.getValue());
            mViewHolder.valuesTv.setOnClickListener(mOnClickListener);
        } else {
//            mViewHolder.valuesTv.getPaint().setFlags(Paint.DEV_KERN_TEXT_FLAG);
            mViewHolder.valuesTv.setOnClickListener(null);
        }

        if (field.getGroupIndex() != 0 && position >= 1 && field.getGroupIndex() > mShowBillModels.get(position - 1).getGroupIndex()) {
            mViewHolder.titleTv.setVisibility(View.VISIBLE);
            String mGroupName = null;
            if (ListUtils.getSize(mBillGroupModels) > field.getGroupIndex()) {
                mGroupName = mBillGroupModels.get(field.getGroupIndex()).getGroup();
            }
            mViewHolder.titleTv.setText(TextUtils.isEmpty(mGroupName) ? "" : mGroupName);
        } else {
            mViewHolder.titleTv.setVisibility(View.GONE);
        }
    }

    public class MyTabChangeListener implements TabLayout.OnTabSelectedListener {
        private TabLayout mTabLayout;
        private int mPosition;

        public MyTabChangeListener(TabLayout tabLayout, int position) {
            mTabLayout = tabLayout;
            mPosition = position;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (this.mPosition >= 0 && mShowBillModels != null && mShowBillModels.size() > this.mPosition && mTabLayout != null) {
                mTabPosition = tab.getPosition();
                BillGroupModel.BillModel model = mShowBillModels.get(mPosition);
                if (model != null) {
                    List<BillGroupModel.GridTab> tabList = model.getTabList();
                    int tabGroupIndex = model.getGroupIndex();

                    List<BillGroupModel> newGroupModels = new ArrayList<>();
                    List<BillGroupModel> billGroupModels = mBillGroupModels.subList(0, tabGroupIndex + 1);
                    mOldTabModels = mBillGroupModels.subList(tabGroupIndex + 1, mBillGroupModels.size());

                    newGroupModels.addAll(billGroupModels);
                    BillGroupModel.GridTab gridTab = tabList.get(mTabPosition);
                    if (gridTab != null) {
                        List<BillGroupModel> tabGroupModels = gridTab.getBillGroupModels();

                        newGroupModels.addAll(tabGroupModels);
                    }
                    setBillGroupModels(newGroupModels);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    public void setBillGroupModels(List<BillGroupModel> mBillGroupModels) {
        this.mBillGroupModels = mBillGroupModels;
        changeBillModel();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag(R.id.tag_key) != null && view.getTag(R.id.tag_key) instanceof Integer) {
                int type = (int) view.getTag(R.id.tag_key);
                //类型  1.电话
                switch (type) {
                    case 1:
                        if (view.getTag() != null && view.getTag() instanceof String) {
                            String phone = (String) view.getTag();
                            SystemUtil.phoneAction(ct, phone);
                        }
                        break;
                }
            } else if (view.getTag(R.id.tag_key) != null && view.getTag(R.id.tag_key) instanceof BillJump) {
                BillJump mBillJump = (BillJump) view.getTag(R.id.tag_key);
                Intent intent = new Intent(ct, mBillJump.getJumpClass())
                        .putExtra(Constants.Intents.ID, mBillJump.getId())
                        .putExtra(Constants.Intents.TITLE, mBillJump.getTitle());
                HashMap<String, String> mParam = mBillJump.getParam();
                if (mParam != null) {
                    for (Map.Entry<String, String> mEntry : mParam.entrySet()) {
                        intent.putExtra(mEntry.getKey(), mEntry.getValue());
                    }
                }
                ct.startActivity(intent);
            } else if (R.id.content == view.getId() &&
                    view.getTag(R.id.tag_key) != null && view.getTag(R.id.tag_key) instanceof String
                    && view.getTag(R.id.tag_key2) != null && view.getTag(R.id.tag_key2) instanceof String) {
                String path = (String) view.getTag(R.id.tag_key);
                String name = (String) view.getTag(R.id.tag_key2);
                if (isImage(name)) {
                    Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                    intent.putExtra(AppConstant.EXTRA_IMAGE_URI, path);
                    ct.startActivity(intent);
                } else {
                    downFile(path);
                }
            }
        }
    };

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(ViewGroup parent, @LayoutRes int layoutId) {
            this(getLayoutInflater().inflate(layoutId, parent, false));
        }

        public abstract void initView(View view);

        public BaseViewHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }
    }


    class TextViewHolder extends BaseViewHolder {
        private TextView titleTv;
        private TextView captionTv;
        private TextView valuesTv;

        @Override
        public void initView(View view) {
            titleTv = (TextView) itemView.findViewById(R.id.titleTv);
            captionTv = (TextView) itemView.findViewById(R.id.captionTv);
            valuesTv = (TextView) itemView.findViewById(R.id.valuesTv);
        }

        public TextViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }
    }

    class TitleViewHolder extends BaseViewHolder {
        private TextView tvTitle;
        private TextView deleteTv;

        public TitleViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }

        @Override
        public void initView(View view) {
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            deleteTv = (TextView) view.findViewById(R.id.deleteTv);
        }

    }

    class TabViewHolder extends BaseViewHolder {
        TabLayout mTabLayout;

        public TabViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }

        @Override
        public void initView(View view) {
            mTabLayout = (TabLayout) view.findViewById(R.id.bill_tab_tl);
            final List<BillGroupModel.GridTab> tabList = mShowBillModels.get(mTabIndex).getTabList();
            if (tabList != null && tabList.size() > 0) {
                for (int i = 0; i < tabList.size(); i++) {
                    BillGroupModel.GridTab gridTab = tabList.get(i);
                    if (gridTab != null) {
                        mTabLayout.addTab(mTabLayout.newTab().setText(gridTab.getTitle()));
                    }
                }
            }
            if (mTabLayout.getTabCount() > 4) {
                mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                mTabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
        }

    }

    //附件类型
    class EnclosureViewHolder extends BaseViewHolder {
        private TextView captionTv;
        private TextView muchInputTv;
        private MyGridView ffGv;

        public EnclosureViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }

        @Override
        public void initView(View itemView) {
            captionTv = (TextView) itemView.findViewById(R.id.captionTv);
            muchInputTv = (TextView) itemView.findViewById(R.id.muchInputTv);
            ffGv = (MyGridView) itemView.findViewById(R.id.ffGv);
            ffGv.setNumColumns(4);
        }
    }

    private class EnclosureAdapter extends BaseAdapter {
        private int position;
        private List<BillGroupModel.LocalData> localDatas;

        public EnclosureAdapter(int position, List<BillGroupModel.LocalData> localDatas) {
            this.position = position;
            this.localDatas = localDatas;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public void setLocalDatas(List<BillGroupModel.LocalData> localDatas) {
            this.localDatas = localDatas;
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(localDatas);
        }

        @Override
        public Object getItem(int i) {
            return localDatas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.item_bill_enclosure_item, null);
            ImageView content = (ImageView) view.findViewById(R.id.content);
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            view.findViewById(R.id.btn_del).setVisibility(View.GONE);
            BillGroupModel.LocalData data = localDatas.get(i);
            String name = data.value == null ? "" : data.value;
            tvName.setText(name);
            if (isImage(name)) {
                ImageLoaderUtil.getInstance().loadImage(data.display, content);
            }
            content.setTag(R.id.tag_key, data.display);
            content.setTag(R.id.tag_key2, data.value);
            content.setOnClickListener(mOnClickListener);
            return view;
        }
    }

    private void downFile(String downloadUrl) {
        final CustomProgressDialog progressDialog = CustomProgressDialog.createDialog(ct);
        progressDialog.setTitile("正在预览");
        progressDialog.setMessage("正在生成附件预览，请勿关闭程序");
        if (progressDialog != null && ct != null) {
            progressDialog.show();
        }
        FileDownloader fileDownloader = new FileDownloader(downloadUrl, new FileDownloader.OnDownloaderListener() {
            @Override
            public void onProgress(long allProress, long progress) {
            }

            @Override
            public void onSuccess(final File file) {
                if (ct != null && ct instanceof BaseActivity) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    try {
                        ((BaseActivity) ct).requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new Runnable() {
                            @Override
                            public void run() {
                                OpenFilesUtils.openCommonFils(ct, file);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(ct, R.string.not_system_permission);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onFailure(String exception) {
                LogUtil.i("onFailure=" + (exception == null ? "" : exception));
                if (ct != null) {
                    ToastUtil.showToast(ct, exception);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
        fileDownloader.download(0L);
    }

    private boolean isImage(String name) {
        return name.toUpperCase().endsWith("JPEG")
                || name.toUpperCase().endsWith("JPG")
                || name.toUpperCase().endsWith("PNG");
    }
}
