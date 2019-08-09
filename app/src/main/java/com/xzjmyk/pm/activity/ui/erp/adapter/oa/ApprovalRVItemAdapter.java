//package com.xzjmyk.pm.activity.ui.erp.adapter.oa;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.support.v7.widget.RecyclerView;
//import android.text.Editable;
//import android.text.Html;
//import android.text.InputType;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.webkit.WebView;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.xzjmyk.pm.activity.AppConstant;
//import com.core.app.MyApplication;
//import com.xzjmyk.pm.activity.R;
//import com.xzjmyk.pm.activity.helper.AvatarHelper;
//import com.xzjmyk.pm.activity.ui.erp.entity.EditChangeListener;
//import com.core.model.Approval;
//import com.xzjmyk.pm.activity.ui.erp.util.ListUtils;
//import com.xzjmyk.pm.activity.ui.erp.util.Lg;
//import com.xzjmyk.pm.activity.ui.erp.util.OpenFilesUtils;
//import com.xzjmyk.pm.activity.ui.erp.util.StringUtils;
//import com.xzjmyk.pm.activity.ui.tool.SingleImagePreviewActivity;
//import com.xzjmyk.pm.activity.util.CalendarUtils;
//import com.core.utils.timeutils.wheel.DatePicker;
//
//import java.io.File;
//import java.util.List;
//
///**
// * Created by Bitliker on 2017/7/10.
// */
//
//public class ApprovalRVItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private Activity ct;
//    private int type;
//    private List<Approval.Item> itemDatas;
//    private int itemPosition = 0;
//
//    public ApprovalRVItemAdapter(Activity ct, int type, int itemPosition, List<Approval.Item> itemDatas) {
//        this.ct = ct;
//        this.type = type;
//        this.itemPosition = itemPosition;
//        this.itemDatas = itemDatas;
//    }
//
//
//    public List<Approval.Item> getItemDatas() {
//        return itemDatas;
//    }
//
//    public void setItemDatas(int type, List<Approval.Item> itemDatas) {
//        this.type = type;
//        this.itemDatas = itemDatas;
//        notifyDataSetChanged();
//        notifyItemRangeChanged(0, itemDatas.size());
//    }
//
//    @Override
//    public int getItemCount() {
//        return ListUtils.getSize(itemDatas);
//    }
//
//
//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (this.type == Approval.ENCLOSURE) {
//            return new EnclosureViewHolder(parent);
//        } else if (this.type == Approval.NODES) {
//            return new NodeViewHolder(parent);
//        } else if (this.type == Approval.POINTS) {
//            return new PointsViewHolder(parent);
//        } else {
//            return new BaseRVViewHodler(parent);
//        }
//
//
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof EnclosureViewHolder) {
//            bindEnclosure((EnclosureViewHolder) holder, position);
//        } else if (holder instanceof NodeViewHolder) {
//            bindNodeView((NodeViewHolder) holder, position);
//        } else if (holder instanceof PointsViewHolder) {
//            bindPointsView((PointsViewHolder) holder, position);
//        } else if (holder instanceof BaseRVViewHodler) {
//            bindBaseRVView((BaseRVViewHodler) holder, position);
//        }
//    }
//
//    private void bindEnclosure(EnclosureViewHolder holder, final int position) {
//        String name = StringUtil.isEmpty(itemDatas.get(position).caption) ? "" : itemDatas.get(position).caption;
//        holder.nameTv.setText(Html.fromHtml("<u>" + name + "</u>"));
//        holder.nameTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gotoReadEnclosure(itemDatas.get(position));
//            }
//        });
//    }
//
//    private void gotoReadEnclosure(Approval.Item item) {
//        String url = item.data;
//        if (!StringUtil.isEmpty(item.caption)) {
//            if (item.caption.endsWith("jpeg") || item.caption.endsWith("jpg") || item.caption.endsWith("png")) {
//                Intent intent = new Intent(ct, SingleImagePreviewActivity.class);
//                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, url);
//                ct.startActivity(intent);
//            } else {
//                OpenFilesUtils.downLoadFile(url, item.caption, new OpenFilesUtils.OnFileLoadListener() {
//                    @Override
//                    public void onLoadIng(int progress, int allProgress) {
//                        //TODO 下载进行中回调
//                    }
//                    @Override
//                    public void onSuccess(File file) {
//                        OpenFilesUtils.openCommonFils(ct, file);
//                    }
//
//                    @Override
//                    public void onFailure(String exception) {
//                        Lg.i("exception=" + exception);
//                    }
//                });
//            }
//        }
//    }
//
//    private void bindNodeView(NodeViewHolder holder, int position) {
//        Approval.Item itemData = itemDatas.get(position);
//        int textColor = R.color.hintColor;
//        if (!StringUtil.isEmpty(itemData.status)) {
//            holder.valuesTv.setText(itemData.status);
//            if (itemData.status.startsWith("待审批")) {
//                textColor = R.color.approvaling;
//            } else if (itemData.status.startsWith("未通过")) {
//                textColor = R.color.crimson;
//            }
//        } else {
//            holder.valuesTv.setText("");
//        }
//        holder.valuesTv.setTextColor(ct.getResources().getColor(textColor));
//        holder.timeTv.setText(StringUtil.isEmpty(itemData.values) ? "" : itemData.values);
//        holder.keyTv.setText(StringUtil.isEmpty(itemData.caption) ? "" : itemData.caption);
//        int reId = R.drawable.weishenpi;
//        if (!StringUtil.isEmpty(itemData.status)) {
//            String status = itemData.status.split("\\(")[0];
//            if (StringUtil.isEmpty(status)) status = "";
//            if ("已审批".equals(status)) {
//                reId = R.drawable.node_finished3;
//            } else if ("待审批".equals(status)) {
//                reId = R.drawable.daishenpi;
//            } else if ("未通过".equals(status)) {
//                reId = R.drawable.node_wait3;
//            } else {
//                reId = R.drawable.weishenpi;
//            }
//        }
//        holder.statusIV.setImageResource(reId);
//        AvatarHelper.getInstance().display(itemData.data, holder.handIv, true, false);
//    }
//
//    private void bindPointsView(final PointsViewHolder holder, final int position) {
//        final Approval.Item itemData = itemDatas.get(position);
//        if (itemDatas.size() > position + 1) {
//            holder.line.setVisibility(View.VISIBLE);
//        } else {
//            holder.line.setVisibility(View.GONE);
//        }
//        holder.captionTV.setText(itemData.caption);
//        if (itemData.neerInput) {
//            if (itemData.isSelect()) {
//                if (itemData.mustInput) {
//                    holder.valueTv.setHint(R.string.common_select);
//                } else {
//                    holder.valueTv.setHint(R.string.common_select_not_must);
//                }
//                setViewShowAble(false, holder.valueEt);
//                setViewShowAble(true, holder.valueTv);
//                holder.valueTv.setText(itemData.values);
//                holder.valueTv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (itemData.inputType() == 2) {
//                            showTimeSelect(holder.valueTv, position);
//                        } else {
//                            selectItem(itemData, position);
//                        }
//                    }
//                });
//            } else {
//                if (itemData.mustInput) {
//                    holder.valueEt.setHint(R.string.common_input);
//                } else {
//                    holder.valueEt.setHint(R.string.common_input1);
//                }
//                if (itemData.type.equals("N")) {
//                    holder.valueEt.setInputType(InputType.TYPE_CLASS_NUMBER);
//                } else {
//                    holder.valueEt.setInputType(InputType.TYPE_CLASS_TEXT);
//                }
//                setViewShowAble(true, holder.valueEt);
//                setViewShowAble(false, holder.valueTv);
//                holder.valueEt.setText(itemData.values);
//                holder.valueEt.addTextChangedListener(new TextChangListener(holder, position));
//            }
//        } else {
//            setViewShowAble(false, holder.valueEt);
//            setViewShowAble(true, holder.valueTv);
//            holder.valueTv.setCompoundDrawables(null, null, null, null);
//            holder.valueTv.setText(itemData.values);
//        }
//
//    }
//
//    private void bindBaseRVView(final BaseRVViewHodler holder, final int position) {
//        final Approval.Item itemData = itemDatas.get(position);
//        holder.captionTV.setText(itemData.caption);
//        if (!StringUtil.isEmpty(itemData.values)) {
//            if (holder.valueTv.getVisibility() == View.VISIBLE) {
//                holder.valueTv.setText(itemData.values);
//            } else if (holder.valueEt.getVisibility() == View.VISIBLE) {
//                holder.valueEt.setText(itemData.values);
//            }
//        }
//        if (itemData.neerInput) {
//            setViewShowAble(true, holder.valueEt);
//            setViewShowAble(false, holder.valueTv, holder.valueWeb);
//            holder.valueEt.setText(itemData.values);
//            if (itemData.mustInput) {
//                holder.valueEt.setHint(R.string.common_input);
//            } else {
//                holder.valueEt.setHint(R.string.common_input1);
//            }
//            //0字符输入  1.数字输入  2.日期输入选择  3.下拉选择  4.多选选择
//            switch (itemData.inputType()) {
//                case 2:
//                case 3:
//                case 4:
//                    if (itemData.mustInput) {
//                        holder.valueEt.setHint(R.string.common_select);
//                    } else {
//                        holder.valueEt.setHint(R.string.common_select_not_must);
//                    }
//                    holder.valueEt.setFocusable(true);
//                    holder.valueEt.setFocusableInTouchMode(false);
//                    holder.valueEt.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (itemData.inputType() == 2) {
//                                showTimeSelect(holder.valueEt, position);
//                            } else {
//                                selectItem(itemData, position);
//                            }
//                        }
//                    });
//                    break;
//                case 1:
//                    holder.valueEt.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    holder.valueEt.addTextChangedListener(new TextChangListener(holder, position));
//                    break;
//                default:
//                    holder.valueEt.setInputType(InputType.TYPE_CLASS_TEXT);
//                    holder.valueEt.addTextChangedListener(new TextChangListener(holder, position));
//            }
//        } else {
//            if (isWeb(itemData.values)) {
//                setViewShowAble(true, holder.valueWeb);
//                setViewShowAble(false, holder.valueEt, holder.valueTv);
//                holder.valueWeb.loadDataWithBaseURL(null, itemData.values, "text/html", "utf-8", null);
//            } else {
//                setViewShowAble(true, holder.valueTv);
//                setViewShowAble(false, holder.valueEt, holder.valueWeb);
//                holder.valueTv.setText(itemData.values);
//            }
//        }
//    }
//
//    private boolean isWeb(String values) {
//        return values != null && values.length() > 100 && values.contains("<br>");
//    }
//
//    private void showTimeSelect(final TextView showView, final int position) {
//        DatePicker picker = new DatePicker(ct, DatePicker.YEAR_MONTH_DAY);
//        picker.setRange(2015, 2019, true);
//        picker.setSelectedItem(CalendarUtils.getCurrentYear(), CalendarUtils.getCurrentMonth(), CalendarUtils.getCurrentDate());
//        picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
//            @Override
//            public void onDatePicked(String year, String month, String day) {
//                String time = year + "-" + month + "-" + day;
//                Lg.i("time=" + time);
//                showView.setText(time);
//                itemDatas.get(position).values = time;
//            }
//        });
//        picker.show();
//    }
//
//    private void selectItem(Approval.Item itemData, final int position) {
//        if (!ListUtils.isEmpty(itemData.datas)) {
//            String[] items = new String[itemData.datas.size()];
//            for (int i = 0; i < itemData.datas.size(); i++) {
//                items[i] = itemData.datas.get(i).display;
//            }
//            new MaterialDialog.Builder(ct)
//                    .title(itemData.caption)
//                    .items(items)
//                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
//                        @Override
//                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                            if (!TextUtils.isEmpty(text)) {
//                                itemDatas.get(position).values = text.toString();
//                                notifyItemChanged(position);
//                            }
//                            return true;
//                        }
//                    }).positiveText(MyApplication.getInstance().getString(R.string.common_sure)).show();
//
//        }
//    }
//
//    private class EnclosureViewHolder extends RecyclerView.ViewHolder {
//        TextView nameTv;
//
//        public EnclosureViewHolder(ViewGroup parent) {
//            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_tag, parent, false));
//        }
//
//        public EnclosureViewHolder(View itemView) {
//            super(itemView);
//            nameTv = (TextView) itemView.findViewById(R.id.tagTv);
//        }
//    }
//
//    private class NodeViewHolder extends RecyclerView.ViewHolder {
//        ImageView handIv, statusIV;
//        TextView timeTv, keyTv, valuesTv;
//
//        public NodeViewHolder(ViewGroup parent) {
//            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_node, parent, false));
//        }
//
//        public NodeViewHolder(View itemView) {
//            super(itemView);
//            timeTv = (TextView) itemView.findViewById(R.id.timeTv);
//            keyTv = (TextView) itemView.findViewById(R.id.keyTv);
//            valuesTv = (TextView) itemView.findViewById(R.id.valuesTv);
//            handIv = (ImageView) itemView.findViewById(R.id.handIv);
//            statusIV = (ImageView) itemView.findViewById(R.id.statusIV);
//        }
//    }
//
//    private class PointsViewHolder extends BaseViewHolder {
//        View line;
//
//        public PointsViewHolder(ViewGroup parent) {
//            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_points, parent, false));
//        }
//
//        public PointsViewHolder(View itemView) {
//            super(itemView);
//            line = itemView.findViewById(R.id.line);
//        }
//    }
//
//    private class BaseViewHolder extends RecyclerView.ViewHolder {
//        TextView captionTV, valueTv;
//        EditText valueEt;
//        FrameLayout valuesFl;
//
//        public BaseViewHolder(View itemView) {
//            super(itemView);
//            captionTV = (TextView) itemView.findViewById(R.id.captionTV);
//            valueTv = (TextView) itemView.findViewById(R.id.valueTv);
//            valueEt = (EditText) itemView.findViewById(R.id.valueEt);
//            valuesFl = (FrameLayout) itemView.findViewById(R.id.valuesFl);
//        }
//    }
//
//    private class BaseRVViewHodler extends BaseViewHolder {
//        WebView valueWeb;
//
//        public BaseRVViewHodler(ViewGroup parent) {
//            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_rv, parent, false));
//        }
//
//        public BaseRVViewHodler(View itemView) {
//            super(itemView);
//            valueWeb = (WebView) itemView.findViewById(R.id.valueWeb);
//        }
//    }
//
//    private void setViewShowAble(boolean showAble, View... views) {
//        if (views != null && views.length > 0) {
//            for (View v : views) {
//                v.setVisibility(showAble ? View.VISIBLE : View.GONE);
//            }
//        }
//    }
//
//    private class TextChangListener extends EditChangeListener {
//        BaseViewHolder hodler;
//        private int position;
//
//        public TextChangListener(BaseViewHolder hodler, int position) {
//            this.hodler = hodler;
//            this.position = position;
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            Lg.i("afterTextChanged=" + s);
//            if (this.position >= 0) {
//                if (this.hodler.valueEt != null && this.hodler.valueEt.getVisibility() == View.VISIBLE) {
//                    String valueEt = this.hodler.valueEt.getText().toString();
//                    itemDatas.get(this.position).values = valueEt == null ? "" : valueEt;
//                    Lg.i("V1=" + valueEt);
//                }
//            }
//        }
//    }
//
//}
