package com.modular.appmessages.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ObjectUtils;
import com.modular.appmessages.R;
import com.modular.appmessages.model.AllProcess;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * @注释：待办事宜
 * @Administrator 2014年10月10日 上午10:14:25
 */
public class SchedultAdapter extends BaseAdapter {
    public List<AllProcess> fList;
    public List<AllProcess> flistNew;
    private Context context;

    public SchedultAdapter() {

    }

    public SchedultAdapter(Context context, List<AllProcess> fArrayList) {
        this.context = context;
        this.fList = fArrayList;
    }

    public SchedultAdapter(Context context, List<AllProcess> fArrayList,
                           List<AllProcess> flistNew) {
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

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        AllProcess fields_object = fList.get(position);
        Store store = null;
        if (view == null) {
            store = new Store();
            view = LayoutInflater.from(context).inflate(
                    R.layout.dy_listview_items_demo_style2, parent, false);
            store.pTextView1 = (TextView) view.findViewById(R.id.TextView03);
            store.pTextView2 = (TextView) view.findViewById(R.id.TextView04);
            store.pTextView3 = (TextView) view.findViewById(R.id.TextView01);
            store.iv_website = (ImageView) view.findViewById(R.id.iv_website);
            store.pTextView4 = (TextView) view.findViewById(R.id.TextView02);
            store.tv_task_status = (TextView) view.findViewById(R.id.tv_task_status);
            view.setTag(store);
        } else {
            store = (Store) view.getTag();
        }

        view.setBackground(context.getResources().getDrawable(R.drawable.selector_me_menu_item_bg));
        if (fields_object.getTypecode().equals("pagingrelease")) {// 知会
            String pTextView3Str = "发起人：<font color='#990000'>"
                    + fields_object.getRecorder() + "</font>";
            CharSequence pTextView3HTML = Html.fromHtml(pTextView3Str);
            store.pTextView3.setText(pTextView3HTML);
            store.pTextView4.setText("" + fields_object.getId());
            String pTextView1Str = "<font color='#6666CC'>"
                    + fields_object.getTaskname().replaceAll("</br>", "")
                    + "</font>";
            CharSequence pTextView1HTML = Html.fromHtml(pTextView1Str);
            store.pTextView1.setText(pTextView1HTML);
            store.pTextView2.setVisibility(View.GONE);
            store.iv_website.setVisibility(View.GONE);
            // store.arrow.setVisibility(View.GONE);
        } else if (fields_object.getTypecode().contains("task")) {// 待办任务
//            if (StringUtil.isEmpty(fields_object.getLink())){
                store.iv_website.setVisibility(View.GONE);
//            }else{
//                store.iv_website.setVisibility(View.VISIBLE);
//            }
           
            store.pTextView1.setText(fields_object.getRecorder());
            if (!ObjectUtils.isEquals(null, fields_object.getDatetime())) {
                store.pTextView2.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(fields_object.getDatetime()));
            }
            store.tv_task_duration = fields_object.getEndTime();
            store.tv_ra_resourcecode = fields_object.getRa_resourcecode();
            store.taskCode = fields_object.getTaskcode();
            store.pTextView4.setText(fields_object.getTaskname());
            store.pTextView3.setText(fields_object.getTaskid());
            store.tv_task_status.setText(fields_object.getMainname());
            store.tv_task_description = fields_object.getDescribe() == null ? "未填写" : fields_object.getDescribe();
            store.tv_task_performer = fields_object.getDealpersoncode() == null ? "未填写" : fields_object.getDealpersoncode();
            store.attachs = fields_object.getAttachs();
            store.task_id = fields_object.getRa_taskid();
        } else {// 待办审批流
            store.pTextView3.setText("" + fields_object.getCodevalue());//编号
            store.pTextView4.setText("" + fields_object.getMainname());//名称
            store.pTextView1.setText(fields_object.getRecorder());//发起人
            store.tv_task_status.setText(fields_object.getStatus());//状态
            if (fields_object.getDatetime() != null) {
                store.pTextView2.setText(""
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(fields_object.getDatetime()));
            }
            store.iv_website.setVisibility(View.GONE);
        }
        return view;
    }

    public class Store {
        public String task_id;//取回复内容id
        public String attachs;//
        public String tv_ra_resourcecode;//
        public ImageView iv_website;//网址
        public TextView tv_task_status;//状态
        public String taskCode;
        public TextView nTextView;
        public TextView pTextView;
        public TextView pTextView1;// 发起人
        public TextView pTextView2;// 发起时间
        public TextView pTextView3;//编号
        public TextView pTextView4;//名称
        public String tv_task_performer;//执行人
        public String tv_task_description;//描述
        public String tv_task_duration;//持续时间
        public TextView urlTextView;
        public CheckBox bCheckBox;
        public ImageView arrow;
    }

}