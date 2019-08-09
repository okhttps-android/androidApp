package com.uas.appme.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.core.utils.CommonUtil;
import com.uas.appme.R;
import com.uas.appme.other.model.Master;

import java.util.ArrayList;
import java.util.List;


/**
 * @author LiuJie
 * @功能:自定义的对话框
 */
public class MasterDialog extends Dialog {

    private Context context;
    private LinearLayout layout;
    private String title;
    @SuppressWarnings("unused")
    private ScrollView scrollView;
    private ListView mListView;
    @SuppressWarnings("unused")
    private LinearLayout dataLayout;
    private MasterAdapter adapter;
    private LinearLayout blend_dialog_preview;

    public PickDialogListener pickDialogListener;

    public MasterDialog(Context context, String title, PickDialogListener listener) {
        super(context, R.style.blend_theme_dialog);
        this.context = context;
        this.title = title;
        this.pickDialogListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        layout = (LinearLayout) inflater.inflate(
                R.layout.act_list_master_view, null);
        TextView titleTextview = (TextView) layout.findViewById(R.id.blend_dialog_title);
        titleTextview.setText(title);
        TextView cancleTextView = (TextView) layout.findViewById(R.id.blend_dialog_cancle_btn);
        cancleTextView.setText(context.getResources().getString(R.string.app_dialog_close));
        blend_dialog_preview = (LinearLayout) layout.findViewById(R.id.blend_dialog_preview);
        mListView = (ListView) layout.findViewById(R.id.lv_master);
//		scrollView=(ScrollView) layout.findViewById(R.id.sv_content_data);
//		dataLayout=(LinearLayout) layout.findViewById(R.id.ly_panel_data);

        /**@注释：cancle button  */
        cancleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });

        this.setCanceledOnTouchOutside(true);
        this.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismiss();
            }
        });
        this.setContentView(layout);
    }

    public List<Master> items;

    /**
     * @注释：initView
     */
    public void initViewData(List<Master> masters) {
        items = masters;
        blend_dialog_preview.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        if (adapter == null) {
            adapter = new MasterAdapter(context, masters);
            mListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                dismiss();
                if (pickDialogListener != null) {
                    pickDialogListener.onListItemClick(position, items.get(position));

                }
            }
        });
    }

    public interface PickDialogListener {
        public void onListItemClick(int position, Master master);
    }

    public class MasterAdapter extends BaseAdapter {
        private Context context;
        private List<Master> list = new ArrayList<Master>();

        public MasterAdapter(Context context, List<Master> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_master_tv_cn, null);
                holder = new Holder();
                holder.blend_dialog_list_item_textview = (TextView) convertView.findViewById(R.id.blend_dialog_list_item_textview);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.blend_dialog_list_item_textview.setText(list.get(position).getMa_function());
            String master = CommonUtil.getSharedPreferences(context, "erp_master");
            if (list.get(position).getMa_user().equals(master)) {
                holder.blend_dialog_list_item_textview.setTextColor(context.getResources().getColor(R.color.light_green));
                @SuppressWarnings("deprecation")
                Drawable drawable = context.getResources().getDrawable(R.drawable.icon_select_master);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.blend_dialog_list_item_textview.setCompoundDrawables(null, null, drawable, null);
            } else {
                holder.blend_dialog_list_item_textview.setTextColor(context.getResources().getColor(R.color.lightblack));
                holder.blend_dialog_list_item_textview.setCompoundDrawables(null, null, null, null);
            }
            return convertView;
        }

        class Holder {
            TextView blend_dialog_list_item_textview;
        }
    }

    public MasterAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(MasterAdapter adapter) {
        this.adapter = adapter;
    }
}
