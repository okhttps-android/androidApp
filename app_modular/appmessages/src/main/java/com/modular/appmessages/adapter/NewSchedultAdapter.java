package com.modular.appmessages.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.ObjectUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.modular.appmessages.R;
import com.modular.appmessages.model.AllProcess;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by FANGlh on 2017/4/5.
 * function:新的ERP待办工作任务列表适配器
 */
public class NewSchedultAdapter extends BaseAdapter {
    public List<AllProcess> fList;
    public List<AllProcess> flistNew;
    public List<AllProcess> real_List;
    public String search_content;
    public String iSend = "";

    public String getiSend() {
        return iSend;
    }

    public void setiSend(String iSend) {
        this.iSend = iSend;
    }

    public String getSearch_content() {
        return search_content;
    }

    public void setSearch_content(String search_content) {
        this.search_content = search_content;
    }

    public List<AllProcess> getReal_List() {
        return real_List;
    }

    public void setReal_List(List<AllProcess> real_List) {
        this.real_List = real_List;
    }

    private Context context;

    public NewSchedultAdapter() {
    }

    public NewSchedultAdapter(Context context, List<AllProcess> fArrayList) {
        this.context = context;
        this.fList = fArrayList;
    }

    public NewSchedultAdapter(Context context, List<AllProcess> fArrayList, List<AllProcess> flistNew) {
        this.context = context;
        this.fList = fArrayList;
        this.flistNew = flistNew;
    }

    @Override
    public int getCount() {
        if (fList == null || fList.isEmpty()) {
            return 0;
        }
        return fList.size();
    }

    @Override
    public Object getItem(int position) {
        return fList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        AllProcess fields_object = fList.get(position);
        Store store = null;
        if (view == null) {
            store = new Store();
            view = LayoutInflater.from(context).inflate(
                    R.layout.item_approval_list, parent, false);
            store.tv_title = view.findViewById(R.id.titleTv);
            view.findViewById(R.id.headImage).setVisibility(View.GONE);
            store.tv_date = view.findViewById(R.id.statusTv);
            store.tv_status = view.findViewById(R.id.subTitle);
            view.setTag(store);
        } else {
            store = (Store) view.getTag();
        }
        try {
            bindView(store, fields_object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void bindView(Store store, AllProcess fields_object) throws Exception {
        if (fields_object.getTypecode().equals("pagingrelease")) {// 知会
            String pTextView3Str = "发起人：<font color='#990000'>"
                    + fields_object.getRecorder() + "</font>";
            CharSequence pTextView3HTML = Html.fromHtml(pTextView3Str);
            store.pTextView3 = pTextView3HTML;
            store.pTextView4 = "" + fields_object.getId();
            String pTextView1Str = "<font color='#6666CC'>"
                    + fields_object.getTaskname().replaceAll("</br>", "")
                    + "</font>";
            CharSequence pTextView1HTML = Html.fromHtml(pTextView1Str);
            store.pTextView1 = pTextView1HTML;
        } else if (fields_object.getTypecode().contains("task")) {// 待办任务


            store.pTextView1 = fields_object.getRecorder();
            if (!ObjectUtils.isEquals(null, fields_object.getDatetime())) {
                store.pTextView2 = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                        .format(fields_object.getDatetime());
            }
            store.tv_task_duration = fields_object.getEndTime();
            store.tv_ra_resourcecode = fields_object.getRa_resourcecode();
            store.taskCode = fields_object.getTaskcode();
            store.pTextView4 = fields_object.getTaskname();
            store.pTextView3 = fields_object.getTaskid();
            store.tv_task_status = fields_object.getMainname();
            store.tv_task_description = fields_object.getDescribe() == null ? "未填写" : fields_object.getDescribe();
            store.tv_task_performer = fields_object.getDealpersoncode() == null ? "未填写" : fields_object.getDealpersoncode();
            store.attachs = fields_object.getAttachs();
            store.task_id = fields_object.getRa_taskid();
            String recoder_name = "";
            if (!StringUtil.isEmpty(iSend) && "iSend".equals(iSend)) {
                recoder_name = "";
            } else {
                recoder_name = fields_object.getRecorder() + MyApplication.getInstance().getString(R.string.task_sended);
            }
            store.tv_title.setText(recoder_name + fields_object.getTaskname());
            store.tv_status.setText(fields_object.getMainname());

            if (!ObjectUtils.isEquals(null, fields_object.getDatetime())) {
                store.tv_date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm")
                        .format(fields_object.getDatetime()));
            }
        } else {// 待办审批流
            store.pTextView3 = "" + fields_object.getCodevalue();//编号
            store.pTextView4 = "" + fields_object.getMainname();//名称
            store.pTextView1 = fields_object.getRecorder();//发起人
            store.tv_task_status = fields_object.getStatus();//状态
            if (fields_object.getDatetime() != null) {
                store.pTextView2 = ""
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm")
                        .format(fields_object.getDatetime());
            }
        }
    }

    public class Store {
        public String task_id;//取回复内容id
        public String attachs;//
        public String tv_ra_resourcecode;//
        public String taskCode;
        public String tv_task_performer;//执行人
        public String tv_task_description;//描述
        public String tv_task_duration;//持续时间

        public TextView tv_title;
        public TextView tv_date;
        public TextView tv_status;

        public CharSequence pTextView1;// 发起人
        public CharSequence pTextView2;// 发起时间
        public CharSequence pTextView3;//编号
        public CharSequence pTextView4;//名称
        public CharSequence tv_task_status;//状态

    }

}