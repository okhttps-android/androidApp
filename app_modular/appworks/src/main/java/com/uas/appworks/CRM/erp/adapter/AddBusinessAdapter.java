package com.uas.appworks.CRM.erp.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.widget.listener.EditChangeListener;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.R;

import java.util.List;


/**
 * Created by Bitliker on 2017/5/8
 */
public class AddBusinessAdapter extends BaseAdapter {


    private List<EmployeesModel> contacts;
    private Activity activity;

    public AddBusinessAdapter(Activity activity, List<EmployeesModel> contacts) {
        this.contacts = contacts;
        this.activity = activity;
    }

    public List<EmployeesModel> getContacts() {
        return contacts;
    }

    public void setContacts(List<EmployeesModel> contacts) {
        this.contacts = contacts;
    }

    @Override
    public int getCount() {
        return ListUtils.getSize(this.contacts);
    }

    @Override
    public Object getItem(int position) {
        return this.contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final EmployeesModel contact = contacts.get(position);
        convertView = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_business_contact, null);
        holder = new ViewHolder();
        holder.name_et = (EditText) convertView.findViewById(R.id.company_tv);
        holder.phone_et = (EditText) convertView.findViewById(R.id.company_add_tv);
        holder.item_tv = (TextView) convertView.findViewById(R.id.item);
        holder.delete_tv = (TextView) convertView.findViewById(R.id.delete_tv);
        holder.item_tv.setText(MyApplication.getInstance().getString(R.string.common_Contact_person) + (position + 1));
        holder.name_et.setText(StringUtil.getMessage(contact.getEmployeeNames()));
        holder.phone_et.setText(StringUtil.getMessage(contact.getEmployeecode()));
        holder.name_et.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                contact.setEmployeeNames(s.toString());
            }
        });
        holder.phone_et.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                contact.setEmployeecode(s.toString());
            }
        });
        holder.delete_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(holder.name_et.getText()) || !TextUtils.isEmpty(holder.phone_et.getText())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.prompt_title).setMessage(R.string.sure_delete_content).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                contacts.remove(contact);
                                notifyDataSetChanged();
                            } catch (Exception e) {
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                } else {
                    contacts.remove(contact);
                    notifyDataSetChanged();
                }
            }
        });
        holder.delete_tv.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        return convertView;
    }

    static final class ViewHolder {
        EditText name_et;
        EditText phone_et;
        TextView item_tv;
        TextView delete_tv;
    }

//    public interface

}
