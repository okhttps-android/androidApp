package com.modular.appmessages.adapter;

import android.Manifest;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.ListUtils;
import com.common.data.RegexUtil;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.model.Approval;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.time.wheel.DatePicker;
import com.core.widget.CustomProgressDialog;
import com.core.widget.listener.EditChangeListener;
import com.modular.appmessages.R;
import com.modular.appmessages.activity.ApprovalActivity;
import com.modular.apputils.network.FileDownloader;
import com.modular.apputils.utils.OpenFilesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitliker on 2017/7/21.
 */

public class ApprovalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ApprovalActivity ct;
    private List<Approval> approvals;
    private List<Approval> historyNodes;
    private boolean isApprovaling;
    private int dbfindItem = -1;


    public ApprovalAdapter(ApprovalActivity ct, List<Approval> approvals, List<Approval> historyNodes, boolean isApprovaling) {
        this.isApprovaling = isApprovaling;
        this.ct = ct;
        this.approvals = approvals;
        if (historyNodes != null) {
            this.historyNodes = historyNodes;
        } else {
            this.historyNodes = new ArrayList<>();
        }
    }

    public List<Approval> getDbFind() {
        if (ListUtils.getSize(approvals) > dbfindItem && dbfindItem != -1) {
            List<Approval> changeApproval = new ArrayList<>();
            int type = approvals.get(dbfindItem).getType();
            for (int i = dbfindItem; i >= 0; i--) {
                if (type == approvals.get(i).getType()) {
                    changeApproval.add(approvals.get(i));
                } else {
                    break;
                }
            }
            for (int i = dbfindItem + 1; i < ListUtils.getSize(approvals); i++) {
                if (type == approvals.get(i).getType()) {
                    changeApproval.add(approvals.get(i));
                } else {
                    break;
                }
            }
            return changeApproval;
        } else {
            return approvals;
        }
    }

    public List<Approval> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(approvals);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (this.approvals.get(viewType).getType()) {
            case Approval.TITLE:
                return new TitleViewHolder(parent);
            case Approval.MAIN://列表
            case Approval.DETAIL:
            case Approval.SETUPTASK:
                return new BaseRVViewHodler(parent);
            case Approval.ENCLOSURE://单行列表
                return new EnclosureViewHolder(parent);
            case Approval.TAG:
                return new TagViewHolder(parent);
            case Approval.POINTS:
                return new PointsViewHolder(parent);
            case Approval.NODES:
                return new NodeViewHolder(parent);
            case Approval.NODES_TAG:
                return new NodeTagViewHolder(parent);
            default:
                return new BaseRVViewHodler(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof TitleViewHolder) {
                bindTitleView((TitleViewHolder) holder, position);
            } else if (holder instanceof BaseRVViewHodler) {
                bindBaseRVView((BaseRVViewHodler) holder, position);
            } else if (holder instanceof EnclosureViewHolder) {
                bindEnclosureView((EnclosureViewHolder) holder, position);
            } else if (holder instanceof TagViewHolder) {
                bindTAGView((TagViewHolder) holder, position);
            } else if (holder instanceof PointsViewHolder) {
                bindPointsView((PointsViewHolder) holder, position);
            } else if (holder instanceof NodeViewHolder) {
                bindNodeView((NodeViewHolder) holder, position);
            } else if (holder instanceof NodeTagViewHolder) {
                bindNodeTagView((NodeTagViewHolder) holder, position);
            }
        } catch (Exception e) {
            if (e != null) {
                LogUtil.i("e=" + e.getMessage());
            }

        }
    }

    private void bindNodeTagView(final NodeTagViewHolder holder, final int position) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isNode = v.getId() == R.id.nodeTv;
                if ((isNode && holder.nodeTag.getVisibility() == View.GONE) || (!isNode && holder.historyTag.getVisibility() == View.GONE)) {
                    List<Approval> newApprovals = new ArrayList<Approval>();
                    List<Approval> approvals1 = approvals.subList(0, position + 1);
                    List<Approval> approvals2 = approvals.subList(position + 1, approvals.size());
                    newApprovals.addAll(approvals1);
                    newApprovals.addAll(historyNodes);
                    historyNodes = approvals2;
                    if (newApprovals.get(position).getType() == Approval.NODES_TAG) {
                        newApprovals.get(position).setNeerInput(!isNode);
                    }
                    approvals = newApprovals;
                    notifyItemRangeChanged(position, approvals.size() - 1);
                }
            }
        };
        holder.historyTV.setOnClickListener(listener);
        holder.nodeTv.setOnClickListener(listener);
        if (approvals.get(position).isNeerInput()) {
            holder.nodeTv.setTextColor(ct.getResources().getColor(R.color.text_main));
            holder.historyTV.setTextColor(ct.getResources().getColor(R.color.titleBlue));
            holder.nodeTag.setVisibility(View.GONE);
            holder.historyTag.setVisibility(View.VISIBLE);
        } else {
            holder.nodeTv.setTextColor(ct.getResources().getColor(R.color.titleBlue));
            holder.historyTV.setTextColor(ct.getResources().getColor(R.color.text_main));
            holder.nodeTag.setVisibility(View.VISIBLE);
            holder.historyTag.setVisibility(View.GONE);
        }
    }

    private void bindNodeView(NodeViewHolder holder, int position) {
        Approval approval = approvals.get(position);
        if (position > 0 && approvals.get(position - 1).getType() == Approval.NODES_TAG || approvals.get(position - 1).getType() == Approval.NODES) {
            holder.padding.setVisibility(View.GONE);
        } else {
            holder.padding.setVisibility(View.VISIBLE);
        }
        if (position > 0 && approvals.get(position - 1).getType() == Approval.NODES) {
            holder.lineTop.setVisibility(View.VISIBLE);
        } else {
            holder.lineTop.setVisibility(View.GONE);
        }
        if (position > 0 && ListUtils.getSize(approvals) > (position + 1)) {
            holder.lineBottom.setVisibility(View.VISIBLE);
        } else {
            holder.lineBottom.setVisibility(View.GONE);
        }

        int textColor = R.color.hintColor;
        int reId = R.drawable.daishenpi;
        String status = "";
        if (!StringUtil.isEmpty(approval.getIdKey())) {
            holder.valuesTv.setText(StringUtil.getFirstBrackets(approval.getIdKey()));
            if (approval.getIdKey().startsWith("待审批")) {
                textColor = R.color.approvaling;
                reId = R.drawable.daishenpi;
                status = "等待" + "" + "审批";
            } else if (approval.getIdKey().startsWith("未通过") || approval.getIdKey().startsWith("结束") || approval.getIdKey().startsWith("不同意")) {
                textColor = R.color.done_approval;
                reId = R.drawable.node_finished3;
                status = "不同意";
            } else if (approval.getIdKey().startsWith("已审批") || approval.getIdKey().startsWith("变更") || approval.getIdKey().startsWith("同意")) {
                reId = R.drawable.node_finished3;
                status = "已审批";
            }
        } else {
            holder.valuesTv.setText("");
        }
        holder.dateTv.setText(TextUtils.isEmpty(approval.getValues()) ? "" : approval.getValues());
        holder.timeTv.setText(TextUtils.isEmpty(approval.getDbFind()) ? "" : approval.getDbFind());
        holder.statusTv.setText(status);
        holder.statusTv.setTextColor(ct.getResources().getColor(textColor));
        holder.keyTv.setText(approval.getCaption());
        holder.statusIV.setImageResource(reId);
        AvatarHelper.getInstance().display(String.valueOf(approval.getId()), holder.handIv, true, false);
        if (reId == R.drawable.daishenpi && isApprovaling && status.startsWith("等待") && status.endsWith("审批")) {
            holder.changeUser.setVisibility(View.VISIBLE);
            holder.changeUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onChangeClickListener != null)
                        onChangeClickListener.click();
                }
            });
        } else {
            holder.changeUser.setVisibility(View.GONE);
        }

    }

    private void bindPointsView(final PointsViewHolder holder, final int position) {
        final Approval approval = approvals.get(position);
        if (approvals.size() > position + 1) {
            holder.line.setVisibility(View.VISIBLE);
        } else {
            holder.line.setVisibility(View.GONE);
        }
        holder.captionTV.setText(approval.getCaption());
        if (approval.isNeerInput()) {
            if (approval.isSelect()) {
                if (approval.isMustInput()) {
                    holder.valueTv.setHint(R.string.common_select);
                } else {
                    holder.valueTv.setHint(R.string.common_select_not_must);
                }
                setViewShowAble(false, holder.valueEt);
                setViewShowAble(true, holder.valueTv);
                holder.valueTv.setText(approval.getValues());
                holder.valueTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (approval.inputType() == 2) {
                            showTimeSelect(holder.valueTv, position);
                        } else {
                            selectItem(approval, position);
                        }
                    }
                });
            } else {
                if (approval.isMustInput()) {
                    holder.valueEt.setHint(R.string.common_input);
                } else {
                    holder.valueEt.setHint(R.string.common_input1);
                }
                if (approval.isDftypeEQ("N")) {
                    holder.valueEt.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    holder.valueEt.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                setViewShowAble(true, holder.valueEt);
                setViewShowAble(false, holder.valueTv);
                holder.valueEt.setText(approval.getValues());
                holder.valueEt.addTextChangedListener(new TextChangListener(holder, position));
            }
        } else {
            setViewShowAble(false, holder.valueEt);
            setViewShowAble(true, holder.valueTv);
            holder.valueTv.setCompoundDrawables(null, null, null, null);
            holder.valueTv.setText(approval.getValues());
        }

    }

    private void bindEnclosureView(EnclosureViewHolder holder, final int position) {
        String name = approvals.get(position).getCaption();
        holder.enclosureTv.setText(TextUtils.isEmpty(name) ? "" : name);
        holder.enclosureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoReadEnclosure(approvals.get(position));
            }
        });

        if ((position + 1) < ListUtils.getSize(approvals) && approvals.get(position).getType() != approvals.get(position + 1).getType()) {
            holder.endView.setVisibility(View.VISIBLE);
        } else {
            holder.endView.setVisibility(View.GONE);
        }
    }

    private void bindTAGView(TagViewHolder holder, final int position) {
        holder.valuesRG.setVisibility(View.GONE);
        String name = approvals.get(position).getCaption();
        if (approvals.get(position).getType() == Approval.ENCLOSURE) {//附件列表
            holder.line.setVisibility(View.GONE);
            holder.padding.setVisibility(View.GONE);
            holder.nameTv.setBackgroundResource(R.drawable.bg_enclosure);
            holder.nameTv.setTextColor(ct.getResources().getColor(R.color.text_main));
            holder.nameTv.setText(Html.fromHtml("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + " " + name + " &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"));
            holder.nameTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoReadEnclosure(approvals.get(position));
                }
            });
        } else {
            holder.nameTv.setTextColor(ct.getResources().getColor(R.color.text_bule));
            holder.nameTv.setBackgroundResource(0);
            if (ListUtils.getSize(approvals) > position + 1 && position > 0) {//标题
                if (approvals.get(position + 1).getType() == approvals.get(position - 1).getType()) {
                    holder.line.setVisibility(View.VISIBLE);
                    holder.padding.setVisibility(View.GONE);
                } else {
                    holder.line.setVisibility(View.GONE);
                    holder.padding.setVisibility(View.VISIBLE);
                    if (name.trim().equals("附件")) {
                        holder.nameTv.setTextColor(ct.getResources().getColor(R.color.text_bule));
                    } else {
                        holder.nameTv.setTextColor(ct.getResources().getColor(R.color.text_bule));
                        if (!TextUtils.isEmpty(approvals.get(position).getValues())) {
                            holder.valuesRG.setOnCheckedChangeListener(null);
                            if (approvals.get(position).getValues().equals(Approval.VALUES_YES)) {
                                holder.yesRB.setChecked(true);
                                holder.notRB.setChecked(false);
                            } else if (approvals.get(position).getValues().equals(Approval.VALUES_NO)) {
                                holder.yesRB.setChecked(false);
                                holder.notRB.setChecked(true);
                            }
                            holder.valuesRG.setVisibility(View.VISIBLE);
                            holder.valuesRG.setTag(position);
                            holder.valuesRG.setOnCheckedChangeListener(mOnCheckedChangeListener);
                        }
                    }
                }
            }
            if (!StringUtil.isEmpty(name)) {
                holder.nameTv.setVisibility(View.VISIBLE);
                holder.nameTv.setText(name);
            } else {
                holder.nameTv.setVisibility(View.GONE);
            }
        }
    }

    private void gotoReadEnclosure(Approval approval) {
        String url = approval.getIdKey();
        final CustomProgressDialog progressDialog = CustomProgressDialog.createDialog(ct);
        progressDialog.setTitile("正在预览");
        progressDialog.setMessage("正在生成附件预览，请勿关闭程序");
        if (!StringUtil.isEmpty(approval.getCaption())) {
            if (isImage(approval.getCaption())) {
                Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, url);
                ct.startActivity(intent);
            } else {
                downFile(url);
            }
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
                LogUtil.i("onSuccess=" + (file == null ? "" : file.getPath()));
                LogUtil.i("file isfile=" + (file.isFile()));
                if (ct != null) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    try {
                        ct.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new Runnable() {
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

    private void bindBaseRVView(final BaseRVViewHodler holder, final int position) {
        holder.valuesRG.setVisibility(View.GONE);
        final Approval approval = approvals.get(position);
        holder.captionTV.setText(approval.getCaption());
        holder.oldValueTv.setVisibility(View.GONE);
        if (!StringUtil.isEmpty(approval.getValues())) {
            if (holder.valueTv.getVisibility() == View.VISIBLE) {
                holder.valueTv.setText(approval.getValues());
            } else if (holder.valueEt.getVisibility() == View.VISIBLE) {
                holder.valueEt.setText(approval.getValues());
            }
        }
        if (approval.isNeerInput()) {
            setViewShowAble(true, holder.valueEt);
            setViewShowAble(false, holder.valueTv, holder.valueWeb);
            holder.valueEt.setText(approval.getValues());
            if (approval.isMustInput()) {
                holder.valueEt.setHint(R.string.common_input);
            } else {
                holder.valueEt.setHint(R.string.common_input1);
            }
            //0字符输入  1.数字输入  2.日期输入选择  3.下拉选择  4.多选选择
            switch (approval.inputType()) {
                case 2:
                case 3:
                case 4:
                    if (approval.isMustInput()) {
                        holder.valueEt.setHint(R.string.common_select);
                    } else {
                        holder.valueEt.setHint(R.string.common_select_not_must);
                    }
                    holder.valueEt.setFocusable(true);
                    holder.valueEt.setFocusableInTouchMode(false);
                    holder.valueEt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int type = approval.inputType();
                            if (type == 2) {
                                showTimeSelect(holder.valueEt, position);
                            } else if (type == 4) {
                                ct.toDbFind(approval);
                                dbfindItem = position;
                            } else {
                                selectItem(approval, position);
                            }
                        }
                    });
                    break;
                case 5:
                    setViewShowAble(false, holder.valueEt);
                    setViewShowAble(true, holder.valuesRG);
                    if (approval.getValues().equals(Approval.VALUES_YES) || approval.getValues().equals("0")) {
                        holder.yesRB.setChecked(true);
                        holder.notRB.setChecked(false);
                    } else if (approval.getValues().equals(Approval.VALUES_NO)) {
                        holder.yesRB.setChecked(false);
                        holder.notRB.setChecked(true);
                    }
                    holder.valuesRG.setTag(position);
                    holder.valuesRG.setOnCheckedChangeListener(mOnCheckedChangeListener);
                    break;
                case 1:
                    holder.valueEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    holder.valueEt.addTextChangedListener(new TextChangListener(holder, position));
                    break;
                default:
                    holder.valueEt.setInputType(InputType.TYPE_CLASS_TEXT);
                    holder.valueEt.addTextChangedListener(new TextChangListener(holder, position));
            }
        } else {
            if (isWeb(approval)) {
                setViewShowAble(true, holder.valueWeb);
                setViewShowAble(false, holder.valueEt, holder.valueTv);
                LogUtil.i("isWeb" + approval.getValues());
                holder.valueWeb.loadDataWithBaseURL(null, approval.getValues(), "text/html", "utf-8", null);
            } else {
                setViewShowAble(true, holder.valueTv);
                setViewShowAble(false, holder.valueEt, holder.valueWeb);
                if (!StringUtil.isEmpty(approval.getOldValues())) {//存在变更
                    holder.oldValueTv.setVisibility(View.VISIBLE);
                    holder.oldValueTv.setText(Html.fromHtml(approval.getOldValues()));
                    holder.valueTv.setText(getOldValues(approval.getOldValues(), approval.getValues()));
                } else {
                    if (approval.isNumber()) {
                        holder.valueTv.setText(approval.getNumber());
                    } else {
                        holder.valueTv.setText(Html.fromHtml(approval.getValues()));
//                        holder.valueTv.setText(Html.fromHtml(approval.getValues()));
                    }
                }
            }
        }
    }

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getTag() != null && group.getTag() instanceof Integer) {
                int position = (int) group.getTag();
                if (ListUtils.getSize(approvals) > position) {
                    Approval approval = approvals.get(position);
                    boolean select = checkedId == R.id.yesRB;
                    if (approval.getType() == Approval.TAG) {//全部采纳和全部不采纳
                        approval.setValues(select ? Approval.VALUES_YES : Approval.VALUES_NO);
                        for (int i = position + 1; i < ListUtils.getSize(approvals); i++) {
                            approval = approvals.get(i);
                            if (approval.getType() == Approval.DETAIL && approval.inputType() == 5 && approval.isNeerInput()) {
                                approval.setValues(select ? Approval.VALUES_YES : Approval.VALUES_NO);
                                notifyItemChanged(i);
                            }
                        }
                    } else {
                        if (approval.inputType() == 5) {
                            approval.setValues(select ? Approval.VALUES_YES : Approval.VALUES_NO);
                        }
                    }
                }
            }
        }
    };

    private CharSequence getOldValues(String oldValues, String values) {
        return Html.fromHtml("<font color='#f10813'>" + values + "</font>");
    }

    private boolean isWeb(Approval approval) {
        String values = approval.getValues();
        return "MS".equals(approval.getDfType()) && values != null && values.contains("table");
    }

    private void showTimeSelect(final TextView showView, final int position) {
        DatePicker picker = new DatePicker(ct, DatePicker.YEAR_MONTH_DAY);
        picker.setRange(2015, 2019, true);
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
            @Override
            public void onDatePicked(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                LogUtil.i("time=" + time);
                showView.setText(time);
                approvals.get(position).setValues(time);
            }
        });
        picker.show();
    }

    private void selectItem(Approval approval, final int position) {
        if (!ListUtils.isEmpty(approval.getDatas())) {
            String[] items = new String[approval.getDatas().size()];
            for (int i = 0; i < approval.getDatas().size(); i++) {
                items[i] = approval.getDatas().get(i).display;
            }
            new MaterialDialog.Builder(ct)
                    .title(approval.getCaption())
                    .items(items)
                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (!TextUtils.isEmpty(text)) {
                                approvals.get(position).setValues(text.toString());
                                notifyItemChanged(position);
                            }
                            return true;
                        }
                    }).positiveText(MyApplication.getInstance().getString(R.string.common_sure)).show();

        }
    }

    private void bindTitleView(TitleViewHolder holder, int position) {
        Approval approval = approvals.get(position);
        holder.titleTv.setText(approval.getCaption());
        AvatarHelper.getInstance().display(approval.getIdKey(), holder.handIv, true, false);
        if (approval.getId() > 0) {
            holder.statusIv.setVisibility(View.VISIBLE);
            if (approval.getId() > 0)
                holder.statusIv.setImageResource(approval.getId());
        } else {
            holder.statusIv.setVisibility(View.GONE);
        }
    }

    private class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv;
        ImageView handIv, statusIv;

        public TitleViewHolder(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_title, parent, false));
        }

        public TitleViewHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.titleTv);
            handIv = (ImageView) itemView.findViewById(R.id.handIv);
            statusIv = (ImageView) itemView.findViewById(R.id.statusIv);
        }
    }

    private class EnclosureViewHolder extends RecyclerView.ViewHolder {
        TextView enclosureTv;
        View endView;

        public EnclosureViewHolder(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_enclosure, parent, false));
        }

        public EnclosureViewHolder(View itemView) {
            super(itemView);
            enclosureTv = itemView.findViewById(R.id.nameTv);
            endView = itemView.findViewById(R.id.endView);
        }
    }

    private class TagViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv;
        View padding, line;
        RadioGroup valuesRG;
        RadioButton yesRB, notRB;

        public TagViewHolder(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_tag, parent, false));
        }

        public TagViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.tagTv);
            padding = itemView.findViewById(R.id.padding);
            line = itemView.findViewById(R.id.line);
            valuesRG = itemView.findViewById(R.id.valuesRG);
            yesRB = itemView.findViewById(R.id.yesRB);
            notRB = itemView.findViewById(R.id.notRB);
        }
    }

    private class NodeViewHolder extends RecyclerView.ViewHolder {
        ImageView handIv, statusIV, changeUser;
        TextView statusTv, keyTv, valuesTv, dateTv, timeTv;
        View padding, lineBottom, lineTop;

        public NodeViewHolder(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_node, parent, false));
        }

        public NodeViewHolder(View itemView) {
            super(itemView);
            statusTv = (TextView) itemView.findViewById(R.id.statusTv);
            timeTv = (TextView) itemView.findViewById(R.id.timeTv);
            dateTv = (TextView) itemView.findViewById(R.id.dateTv);
            keyTv = (TextView) itemView.findViewById(R.id.keyTv);
            valuesTv = (TextView) itemView.findViewById(R.id.valuesTv);
            handIv = (ImageView) itemView.findViewById(R.id.handIv);
            statusIV = (ImageView) itemView.findViewById(R.id.statusIV);
            changeUser = (ImageView) itemView.findViewById(R.id.changeUser);
            lineBottom = itemView.findViewById(R.id.lineBottom);
            lineTop = itemView.findViewById(R.id.lineTop);
            padding = itemView.findViewById(R.id.padding);
            changeUser.setVisibility(View.GONE);
        }
    }

    private class NodeTagViewHolder extends RecyclerView.ViewHolder {
        TextView nodeTv, historyTV;
        View nodeTag, historyTag;

        public NodeTagViewHolder(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_node_tag, parent, false));
        }

        public NodeTagViewHolder(View itemView) {
            super(itemView);
            nodeTv = (TextView) itemView.findViewById(R.id.nodeTv);
            historyTV = (TextView) itemView.findViewById(R.id.historyTV);
            nodeTag = itemView.findViewById(R.id.nodeTag);
            historyTag = itemView.findViewById(R.id.historyTag);
            historyTag.setVisibility(View.GONE);
        }
    }

    private class PointsViewHolder extends BaseViewHolder {
        View line;

        public PointsViewHolder(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_points, parent, false));
        }

        public PointsViewHolder(View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.line);

        }
    }

    private class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView captionTV, valueTv;
        EditText valueEt;
        FrameLayout valuesFl;

        public BaseViewHolder(View itemView) {
            super(itemView);
            captionTV = (TextView) itemView.findViewById(R.id.captionTV);
            valueTv = (TextView) itemView.findViewById(R.id.valueTv);
            valueEt = (EditText) itemView.findViewById(R.id.valueEt);
            valuesFl = (FrameLayout) itemView.findViewById(R.id.valuesFl);

        }
    }

    private class BaseRVViewHodler extends BaseViewHolder {
        WebView valueWeb;
        TextView oldValueTv;
        RadioGroup valuesRG;
        RadioButton yesRB, notRB;

        public BaseRVViewHodler(ViewGroup parent) {
            this(LayoutInflater.from(ct).inflate(R.layout.item_approval_rv, parent, false));
        }

        public BaseRVViewHodler(View itemView) {
            super(itemView);
            valueWeb = (WebView) itemView.findViewById(R.id.valueWeb);
            valuesRG = itemView.findViewById(R.id.valuesRG);
            yesRB = itemView.findViewById(R.id.yesRB);
            notRB = itemView.findViewById(R.id.notRB);
            oldValueTv = (TextView) itemView.findViewById(R.id.oldValueTv);
            oldValueTv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //删除线
            oldValueTv.getPaint().setAntiAlias(true);// 抗锯齿
        }
    }

    private void setViewShowAble(boolean showAble, View... views) {
        if (views != null && views.length > 0) {
            for (View v : views) {
                v.setVisibility(showAble ? View.VISIBLE : View.GONE);
            }
        }
    }

    private class TextChangListener extends EditChangeListener {
        BaseViewHolder hodler;
        private int position;

        public TextChangListener(BaseViewHolder hodler, int position) {
            this.hodler = hodler;
            this.position = position;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (this.position >= 0) {
                if (this.hodler.valueEt != null && this.hodler.valueEt.getVisibility() == View.VISIBLE) {
                    String valueEt = this.hodler.valueEt.getText().toString();
                    approvals.get(this.position).setValues(valueEt == null ? "" : valueEt);
                }
            }
        }
    }

    private OnChangeClickListener onChangeClickListener;

    public void setOnChangeClickListener(OnChangeClickListener onChangeClickListener) {
        this.onChangeClickListener = onChangeClickListener;
    }

    public interface OnChangeClickListener {
        void click();
    }


    private boolean isImage(String name) {
        return name.toUpperCase().endsWith("jpeg".toUpperCase())
                || name.toUpperCase().endsWith("jpg".toUpperCase())
                || name.toUpperCase().endsWith("png".toUpperCase());
    }
}
