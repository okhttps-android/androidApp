package com.modular.apputils.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.MyGridView;
import com.me.imageloader.ImageLoaderUtil;
import com.modular.apputils.R;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.utils.BillTypeChangeUtils;
import com.modular.apputils.widget.VeriftyDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 录入单据列表item 共2种类型
 * 0.标题
 * 1.请选择
 */
public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BaseViewHolder>
        implements View.OnClickListener {

    public Context ct;
    protected List<BillGroupModel> mBillGroupModels;
    protected List<BillGroupModel.BillModel> mShowBillModels;
    //主表的字段列表
    protected List<BillGroupModel.BillModel> mFormBillModels;
    //可更新的字段列表
    protected List<BillGroupModel.BillModel> mUpdateBillModels;
    //所有字段列表
    protected List<BillGroupModel.BillModel> mAllBillModels;
    protected OnAdapterListener mOnAdapterListener;
    public int mTabIndex = -1;
    public int mTabPosition = 0;
    private List<BillGroupModel> mOldTabModels;

    public BillAdapter(Context ct, List<BillGroupModel> mBillGroupModels, OnAdapterListener mOnAdapterListener) {
        this.ct = ct;
        this.mBillGroupModels = mBillGroupModels;
        this.mOnAdapterListener = mOnAdapterListener;
        changeBillModel();
    }

    public BillAdapter(List<BillGroupModel.BillModel> showBillModels, Context ct, OnAdapterListener mOnAdapterListener) {
        this.ct = ct;
        this.mShowBillModels = showBillModels;
        this.mOnAdapterListener = mOnAdapterListener;
    }

    public void updateBillModelValues(int position, String values, String display) {
        if (position >= 0 && position < ListUtils.getSize(mShowBillModels)) {
            mShowBillModels.get(position).setValue(values);
            mShowBillModels.get(position).setDisplay(display);
            notifyItemChanged(position);
        }
    }

    public void addBillModelData(int position, String values, String display) {
        if (position >= 0 && position < ListUtils.getSize(mShowBillModels)) {
            if (mShowBillModels.get(position).getLocalDatas() == null) {
                mShowBillModels.get(position).setLocalDatas(new ArrayList<BillGroupModel.LocalData>());
            }
            BillGroupModel.LocalData data = new BillGroupModel.LocalData();
            data.value = values;
            data.display = display;
            mShowBillModels.get(position).getLocalDatas().add(data);
            notifyItemChanged(position);
        }
    }

    public void addBillModelData(int position, List<BillGroupModel.LocalData> datas) {
        if (position >= 0 && position < ListUtils.getSize(mShowBillModels)) {
            if (mShowBillModels.get(position).getLocalDatas() == null) {
                mShowBillModels.get(position).setLocalDatas(new ArrayList<BillGroupModel.LocalData>());
            }
            mShowBillModels.get(position).getLocalDatas().addAll(datas);
            notifyItemChanged(position);
        }
    }

    public void setBillGroupModels(List<BillGroupModel> mBillGroupModels) {
        this.mBillGroupModels = mBillGroupModels;
        changeBillModel();
    }

    public void setShowBillModels(List<BillGroupModel.BillModel> showBillModels) {
        this.mShowBillModels = showBillModels;
    }

    public List<BillGroupModel.BillModel> getmAllBillModels() {
        return mAllBillModels;
    }

    /**
     * 当外界的因素引起mBillGroupModels变化时候，通过遍历将mBillGroupModels转成mShowBillModels进行显示
     */
    private void changeBillModel() {
        if (mShowBillModels == null) {
            mShowBillModels = new ArrayList<>();
        } else {
            mShowBillModels.clear();
            //notifyDataSetChanged();
        }
        if (mFormBillModels == null) {
            mFormBillModels = new ArrayList<>();
        } else {
            mFormBillModels.clear();
        }
        if (mUpdateBillModels == null) {
            mUpdateBillModels = new ArrayList<>();
        } else {
            mUpdateBillModels.clear();
        }
        if (mAllBillModels == null) {
            mAllBillModels = new ArrayList<>();
        } else {
            mAllBillModels.clear();
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
                } else {
                    if (e.getShowBillFields() != null && !e.getShowBillFields().isEmpty()) {
                        if (!TextUtils.isEmpty(e.getGroup())) {
                            BillGroupModel.BillModel mTitleBillModel = new BillGroupModel.BillModel();
                            mTitleBillModel.setGroupIndex(i);
                            mTitleBillModel.setType(BillGroupModel.Constants.TYPE_TITLE);
                            mTitleBillModel.setCaption(e.getGroup());
                            mTitleBillModel.setAllowBlank(e.isDeleteAble() ? "T" : "F");
                            mShowBillModels.add(mTitleBillModel);
                        }
                        mShowBillModels.addAll(e.getShowBillFields());
                        if (!e.isForm() && e.isLastInType()) {
                            mShowBillModels.add(getAddModel(i));
                        }
     
                    }

                    if (e.getUpdateBillFields() != null && !e.getUpdateBillFields().isEmpty()) {
                        mUpdateBillModels.addAll(e.getUpdateBillFields());
                    }
                }

                if (e.isForm()) {
                    if (e.getShowBillFields() != null) {
                        mFormBillModels.addAll(e.getShowBillFields());
                    }
                    if (e.getHideBillFields() != null) {
                        mFormBillModels.addAll(e.getHideBillFields());
                    }
                }
                if (e.getShowBillFields() != null && !e.getShowBillFields().isEmpty()) {
                    mAllBillModels.addAll(e.getShowBillFields());
                }
                if (e.getHideBillFields() != null && !e.getHideBillFields().isEmpty()) {
                    mAllBillModels.addAll(e.getHideBillFields());
                }
            }
        }
    }

    private BillGroupModel.BillModel getAddModel(int index) {
        BillGroupModel.BillModel mTitleBillModel = new BillGroupModel.BillModel();
        mTitleBillModel.setGroupIndex(index);
        mTitleBillModel.setType(BillGroupModel.Constants.TYPE_ADD);
        mTitleBillModel.setCaption("添加单据");
        return mTitleBillModel;
    }

    public List<BillGroupModel> getBillGroupModels() {
        return mBillGroupModels;
    }

    public BillGroupModel getBillGroupModel(int groupIndex) {
        /*if (groupIndex >= 0 && ListUtils.getSize(mBillGroupModels) > groupIndex) {
            return mBillGroupModels.get(groupIndex);
        } else {
            return null;
        }*/
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

    public List<BillGroupModel.BillModel> getFormBillModels() {
        return mFormBillModels;
    }

    public List<BillGroupModel.BillModel> getShowBillModels() {
        return mShowBillModels;
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

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = null;
        switch (viewType) {
            case 0:
                viewHolder = new TitleViewHolder(parent, R.layout.item_bill_title);
                break;
            case 1:
                viewHolder = new InputViewHolder(parent, R.layout.item_bill_input_select);
                break;
            case 2:
                viewHolder = new EnclosureViewHolder(parent, R.layout.item_bill_enclosure);
                break;//附件类型
            case 110:
                viewHolder = new AddViewHolder(parent, R.layout.item_bill_add);
                break;
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
        return mShowBillModels == null ? 0 : mShowBillModels.size();
    }


    private LayoutInflater mLayoutInflater;

    public LayoutInflater getLayoutInflater() {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(ct);
        }
        return mLayoutInflater;
    }


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

    public class AddViewHolder extends BaseViewHolder {
        private TextView addTv;

        public AddViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }

        @Override
        public void initView(View view) {
            addTv = (TextView) view.findViewById(R.id.addTv);
        }
    }

    //选择和输入类型
    public class InputViewHolder extends BaseViewHolder {
        public TextView captionTv;
        public TextView muchInputTv;
        public ImageView selectIv;
        public EditText valuesEd;

        public InputViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
        }

        @Override
        public void initView(View itemView) {
            captionTv = (TextView) itemView.findViewById(R.id.captionTv);
            muchInputTv = (TextView) itemView.findViewById(R.id.muchInputTv);
            selectIv = (ImageView) itemView.findViewById(R.id.selectIv);
            valuesEd = (EditText) itemView.findViewById(R.id.valuesEd);
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
            if (mTabIndex != -1) {
                BillGroupModel.BillModel billModel = mShowBillModels.get(mTabIndex);
                final List<BillGroupModel.GridTab> tabList = billModel.getTabList();
                if (tabList != null && tabList.size() > 0) {
                    for (int i = 0; i < tabList.size(); i++) {
                        BillGroupModel.GridTab gridTab = tabList.get(i);
                        if (gridTab != null) {
                            mTabLayout.addTab(mTabLayout.newTab().setText(gridTab.getTitle()));
                        }
                    }
                }
                Log.d("raomengBill", "TabViewHolder---" + mTabPosition);
                if (mTabPosition >= 0) {
                    mTabLayout.getTabAt(mTabPosition).select();
                }
                if (mTabLayout.getTabCount() > 4) {
                    mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                } else {
                    mTabLayout.setTabMode(TabLayout.MODE_FIXED);
                }
            }
        }

    }


    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        BillGroupModel.BillModel model = null;
        if (mShowBillModels != null && mShowBillModels.size() > position) {
            model = mShowBillModels.get(position);
        }
        try {
            holder.itemView.setTag(model);
            if (holder instanceof InputViewHolder) {
                bindInputView((InputViewHolder) holder, model, position);
            } else if (holder instanceof TitleViewHolder) {
                bindTitleView((TitleViewHolder) holder, model, position);
            } else if (holder instanceof AddViewHolder) {
                bindAddView((AddViewHolder) holder, model, position);
            } else if (holder instanceof EnclosureViewHolder) {
                bindEnclosureView((EnclosureViewHolder) holder, model, position);
            } else if (holder instanceof TabViewHolder) {
                bindTabView((TabViewHolder) holder, model, position);
            }
        } catch (Exception e) {
            LogUtil.i("gong", position + "  e=" + e.getMessage());
        }

    }

    private void bindTabView(TabViewHolder holder, final BillGroupModel.BillModel model, final int position) {
        Log.d("raomengBill", "bindTabView");
        if (holder.mTabLayout.getTag() != null && holder.mTabLayout.getTag() instanceof MyTabChangeListener) {
            holder.mTabLayout.removeOnTabSelectedListener((TabLayout.OnTabSelectedListener) holder.mTabLayout.getTag());
        }
        /*int selectedTabPosition = holder.mTabLayout.getSelectedTabPosition();
        if (mTabPosition >= 0 && mTabPosition != selectedTabPosition) {
            holder.mTabLayout.getTabAt(mTabPosition).select();
//            switchTabData(mTabPosition);
        }*/
        MyTabChangeListener myTabChangeListener = new MyTabChangeListener(position);
        holder.mTabLayout.addOnTabSelectedListener(myTabChangeListener);
        int selectedTabPosition = holder.mTabLayout.getSelectedTabPosition();
        if (mTabPosition >= 0 && mTabPosition != selectedTabPosition) {
            holder.mTabLayout.getTabAt(mTabPosition).select();
        }
        holder.mTabLayout.setTag(myTabChangeListener);
    }

    //绑定附件字段
    private void bindEnclosureView(EnclosureViewHolder holder, BillGroupModel.BillModel model, int position) {
        if (model != null) {
            holder.captionTv.setText(model.getCaption());
            holder.muchInputTv.setVisibility(model.getAllowBlank().equals("F") ? View.VISIBLE : View.GONE);
            EnclosureAdapter mEnclosureAdapter = null;
            if (holder.ffGv.getTag(R.id.tag_key) != null && holder.ffGv.getTag(R.id.tag_key) instanceof EnclosureAdapter) {
                mEnclosureAdapter = (EnclosureAdapter) holder.ffGv.getTag(R.id.tag_key);
                mEnclosureAdapter.setLocalDatas(model.getLocalDatas());
                mEnclosureAdapter.setPosition(position);
            } else {
                mEnclosureAdapter = new EnclosureAdapter(position, model.getLocalDatas());
            }
            holder.ffGv.setAdapter(mEnclosureAdapter);
            holder.ffGv.setTag(model);
            holder.ffGv.setTag(R.id.tag_key2, position);
        }
    }


    private void bindAddView(AddViewHolder mAddViewHolder, BillGroupModel.BillModel model, int position) throws Exception {
        if (model != null) {
            mAddViewHolder.addTv.setTag(model.getGroupIndex());
            mAddViewHolder.addTv.setOnClickListener(this);
        }
    }

    public void bindInputView(InputViewHolder mInputViewHolder, BillGroupModel.BillModel model, int position) throws Exception {
        if (model != null) {
            if (mInputViewHolder.valuesEd.getTag() != null && mInputViewHolder.valuesEd.getTag() instanceof TextChangListener) {
                mInputViewHolder.valuesEd.removeTextChangedListener((TextChangListener) mInputViewHolder.valuesEd.getTag());
            }
            mInputViewHolder.captionTv.setText(model.getCaption());
            mInputViewHolder.muchInputTv.setVisibility(model.getAllowBlank().equals("F") ? View.VISIBLE : View.GONE);
            mInputViewHolder.valuesEd.setText(model.getValue());
            if (model.getType().equals("N")) {
                mInputViewHolder.valuesEd.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                mInputViewHolder.valuesEd.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
            }
            if (model.getReadOnly().equals("T")) {
                mInputViewHolder.valuesEd.setFocusable(false);
                mInputViewHolder.valuesEd.setClickable(true);
                mInputViewHolder.valuesEd.setOnClickListener(null);
                if (BillTypeChangeUtils.isSelect(model.getType())) {
                    mInputViewHolder.valuesEd.setHint("请选择");
                    mInputViewHolder.selectIv.setVisibility(View.VISIBLE);
                } else {
                    mInputViewHolder.valuesEd.setHint("请输入");
                    mInputViewHolder.selectIv.setVisibility(View.GONE);
                }
            } else if (BillTypeChangeUtils.isSelect(model.getType())) {
                //选择类型
                mInputViewHolder.valuesEd.setHint("请选择");
                mInputViewHolder.valuesEd.setFocusable(false);
                mInputViewHolder.valuesEd.setClickable(true);
                mInputViewHolder.selectIv.setVisibility(View.VISIBLE);
                mInputViewHolder.valuesEd.setTag(R.id.tag, position);
                mInputViewHolder.valuesEd.setTag(R.id.tag2, model);
                mInputViewHolder.valuesEd.setOnClickListener(this);
            } else {
                //输入类型
                mInputViewHolder.valuesEd.setHint("请输入");
                mInputViewHolder.selectIv.setVisibility(View.GONE);
                mInputViewHolder.valuesEd.setFocusable(true);
                mInputViewHolder.valuesEd.setClickable(false);
                mInputViewHolder.valuesEd.setFocusableInTouchMode(true);
                TextChangListener mTextChangListener = new TextChangListener(mInputViewHolder.valuesEd, position);
                mInputViewHolder.valuesEd.setTag(mTextChangListener);
                mInputViewHolder.valuesEd.addTextChangedListener(mTextChangListener);
                mInputViewHolder.valuesEd.setOnClickListener(null);
            }

        }
    }

    public void bindTitleView(TitleViewHolder mTitleViewHolder, BillGroupModel.BillModel model, int position) throws Exception {
        if (model != null) {
            if (model.getAllowBlank().equals("F")) {
                mTitleViewHolder.deleteTv.setVisibility(View.GONE);
                mTitleViewHolder.deleteTv.setOnClickListener(null);
            } else {
                mTitleViewHolder.deleteTv.setVisibility(View.VISIBLE);
                mTitleViewHolder.deleteTv.setTag(model.getGroupIndex());
                mTitleViewHolder.deleteTv.setOnClickListener(this);
            }
            mTitleViewHolder.tvTitle.setText(model.getCaption());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.deleteTv) {
            if (view.getTag() != null && view.getTag() instanceof Integer) {
                showDeleteGroup((Integer) view.getTag());
            }
        } else if (view.getId() == R.id.valuesEd) {
            if (mOnAdapterListener != null) {
                if (view.getTag(R.id.tag2) != null && view.getTag(R.id.tag2) instanceof BillGroupModel.BillModel) {
                    int position = view.getTag(R.id.tag) != null && view.getTag(R.id.tag) instanceof Integer ? ((int) view.getTag(R.id.tag)) : 0;
                    mOnAdapterListener.toSelect(position, (BillGroupModel.BillModel) view.getTag(R.id.tag2));
                }
            }

        } else if (view.getId() == R.id.addTv) {
            if (view.getTag() != null && view.getTag() instanceof Integer) {
                int groupIndex = (int) view.getTag();
                addGroups(groupIndex);
            }
        } else if (R.id.btn_del == view.getId()) {
            if (view.getTag(R.id.tag_key) != null && view.getTag(R.id.tag_key2) != null
                    && view.getTag(R.id.tag_key) instanceof Integer && view.getTag(R.id.tag_key2) instanceof Integer) {
                int position = (int) view.getTag(R.id.tag_key2);//当前在主列表的item
                int index = (int) view.getTag(R.id.tag_key);//当前在子列表的item
                if (position >= 0 && position < ListUtils.getSize(mShowBillModels)
                        && ListUtils.getSize(mShowBillModels.get(position).getLocalDatas()) > index) {
                    mShowBillModels.get(position).getLocalDatas().remove(index);
                    notifyItemChanged(position);
                }
            }
        } else if (R.id.content == view.getId()) {
            if (view.getTag(R.id.tag_key2) != null && view.getTag(R.id.tag_key2) instanceof Integer) {
                int position = (int) view.getTag(R.id.tag_key2);//当前在主列表的item
                if (mOnAdapterListener != null && position >= 0 && position < ListUtils.getSize(mShowBillModels)) {
                    mOnAdapterListener.toEnclosureSelect(position, mShowBillModels.get(position));
                }
            }
        }
    }

    protected void addGroups(int mGroupIndex) {
        BillGroupModel mBillGroupModel = mBillGroupModels.get(mGroupIndex);
        mBillGroupModel.setLastInType(false);
        int oldGridIndex = mBillGroupModel.getGridIndex();
        boolean isForm = mBillGroupModel.isForm();

        BillGroupModel newBillGroupModel = new BillGroupModel();
        newBillGroupModel.setForm(isForm);
        if (isForm) {
            newBillGroupModel.setGroup(mBillGroupModel.getGroup());
        } else {
            newBillGroupModel.setGroup("明细" + (oldGridIndex + 1));
        }
        newBillGroupModel.setGridIndex(oldGridIndex + 1);
        newBillGroupModel.setDeleteAble(true);
        newBillGroupModel.setLastInType(true);
        for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
            newBillGroupModel.addShow(new BillGroupModel.BillModel(e));
        }
        mBillGroupModels.add(mGroupIndex + 1, newBillGroupModel);
        setBillGroupModels(mBillGroupModels);

        if (mTabIndex != -1) {
            BillGroupModel.BillModel tabModel = mShowBillModels.get(mTabIndex);
            if (tabModel != null) {
                tabModel.getTabList().get(mTabPosition)
                        .setBillGroupModels(mBillGroupModels.subList(tabModel.getGroupIndex() + 1, mBillGroupModels.size()));
            }
        }

        notifyDataSetChanged();
    }

    public class TextChangListener extends EditChangeListener {
        EditText ed;
        private int position;

        public TextChangListener(EditText ed, int position) {
            this.ed = ed;
            this.position = position;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (this.position >= 0 && mShowBillModels != null && mShowBillModels.size() > this.position && ed != null) {
                String valueEt = ed.getText().toString();
                mShowBillModels.get(this.position).setValue(valueEt == null ? "" : valueEt);
            }
        }
    }


    public class MyTabChangeListener implements TabLayout.OnTabSelectedListener {

        public MyTabChangeListener(int position) {
            mTabIndex = position;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switchTabData(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    private void switchTabData(int tabPosition) {
        LogUtil.d("tab_1","tabPosition:"+tabPosition);
        notifyDataSetChanged();
        mTabPosition = tabPosition;
        LogUtil.d("tab_1","tabPosition:"+tabPosition+" mTabIndex:"+mTabIndex);
        if (mTabIndex >= 0 && mShowBillModels != null && mShowBillModels.size() > mTabIndex) {
            BillGroupModel.BillModel model = mShowBillModels.get(mTabIndex);
            LogUtil.d("tab_1","model:"+JSON.toJSONString(model));
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
                    LogUtil.prinlnLongMsg("tab_1", JSON.toJSONString(tabGroupModels));
                    newGroupModels.addAll(tabGroupModels);
                }
               
                setBillGroupModels(newGroupModels);
                notifyDataSetChanged();
            }
        }
    }

    public void switchTabIndex(int tabPosition) {
        if (mTabIndex >= 0 && mShowBillModels != null && mShowBillModels.size() > mTabIndex) {
            mTabPosition = tabPosition;
            notifyItemChanged(mTabIndex);
//            notifyDataSetChanged();
        }
    }

    private void showDeleteGroup(final int groupIndex) {
        new VeriftyDialog.Builder(ct)
                .setTitle(ct.getString(R.string.app_name))
                .setContent("是否确认删除该单据?")
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                        if (clickSure) {
                            if (mBillGroupModels != null && mBillGroupModels.size() > groupIndex && groupIndex >= 0) {
                                deleteGroup(groupIndex);
                            }
                        }
                    }
                });
    }

    public void deleteGroup(int groupIndex) {
        boolean isLastItem = mBillGroupModels.get(groupIndex).isLastInType();
        if (isLastItem && groupIndex - 1 > 0 && !mBillGroupModels.get(groupIndex - 1).isForm()) {
            mBillGroupModels.get(groupIndex - 1).setLastInType(true);
        }
        mBillGroupModels.remove(groupIndex);
        setBillGroupModels(mBillGroupModels);
        notifyDataSetChanged();
    }


    public interface OnAdapterListener {
        void toSelect(int position, BillGroupModel.BillModel model);

        void toEnclosureSelect(int position, BillGroupModel.BillModel model);
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
            return ListUtils.getSize(localDatas) < 9 ? (ListUtils.getSize(localDatas) + 1) : ListUtils.getSize(localDatas);
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
            Button btnDel = (Button) view.findViewById(R.id.btn_del);
            if (ListUtils.getSize(localDatas) > i) {
                BillGroupModel.LocalData data = localDatas.get(i);
                btnDel.setVisibility(View.VISIBLE);
                String name = data.value == null ? "" : data.value;
                tvName.setText(name);
                btnDel.setTag(R.id.tag_key2, position);
                btnDel.setTag(R.id.tag_key, i);
                btnDel.setOnClickListener(BillAdapter.this);
                if (isImage(name)) {
                    ImageLoaderUtil.getInstance().loadImage(data.display, content);
                }
            } else {
                tvName.setText("");
                btnDel.setVisibility(View.GONE);
                content.setImageResource(R.drawable.add_picture);

                content.setTag(R.id.tag_key2, position);
                content.setOnClickListener(BillAdapter.this);
            }
            return view;
        }
    }

    private boolean isImage(String name) {
        return name.toUpperCase().endsWith("JPEG")
                || name.toUpperCase().endsWith("JPG")
                || name.toUpperCase().endsWith("PNG");
    }
}
