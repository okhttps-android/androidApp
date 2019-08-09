package com.uas.appworks.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Arison on 2018/10/11.
 */

public class PhoneContactsUtils {
    /**
     * 写入手机联系人
     */
    public static void addContact(Activity activity,String name, List<String> numbers,
                            String email, String company, String positon) throws Exception {
        if (numbers == null) {
            Toast.makeText(activity, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentResolver resolver = activity.getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentValues values = new ContentValues();
        long contact_id = ContentUris.parseId(resolver.insert(uri, values));
        //插入data表
        uri = Uri.parse("content://com.android.contacts/data");

        //add Name
        values.put("raw_contact_id", contact_id);
        values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/name");
        values.put("data2", name);
        values.put("data1", name);
        resolver.insert(uri, values);
        values.clear();

        //add Phone
        for (int i = 0; i < numbers.size(); i++) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/phone_v2");
            values.put("data2", "2");   //手机
            values.put("data1", numbers.get(i));
            resolver.insert(uri, values);
            values.clear();
        }


        if (!TextUtils.isEmpty(email)) {
            //add email
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Email.TYPE, "1");   //邮箱
            values.put(ContactsContract.CommonDataKinds.Email.DATA, email);
            resolver.insert(uri, values);
            values.clear();
        }

        if (!TextUtils.isEmpty(company)) {
            //add organization
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Organization.TITLE, positon);   //职务
            values.put(ContactsContract.CommonDataKinds.Organization.COMPANY, company);   //公司
            resolver.insert(uri, values);
            values.clear();
        }

        Toast.makeText(activity, "插入号码成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更改联系人
     */
    public static void changeContact(Activity activity,String name, List<String> numbers,
                               String email, String company, String positon) throws Exception {
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data._ID}, "display_name=?", new String[]{name}, null);
        if (cursor == null) {
            return;
        }

        if (cursor.moveToFirst()) {
            uri = Uri.parse("content://com.android.contacts/data");
            int id = cursor.getInt(0);
            ContentValues values = new ContentValues();
            for (int i = 0; i < numbers.size(); i++) {
                values.put("data1", numbers.get(i));
                resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2", id + ""});
                values.clear();
            }

        } else {
            Toast.makeText(activity, "没有找到号码", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    public static void deleteContact(Activity activity,String name) throws Exception {
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data._ID}, "display_name=?", new String[]{name}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            resolver.delete(uri, "display_name=?", new String[]{name});
            uri = Uri.parse("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
        }
    }
}
