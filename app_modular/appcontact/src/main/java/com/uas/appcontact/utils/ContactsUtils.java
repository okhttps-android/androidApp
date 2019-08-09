package com.uas.appcontact.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.uas.appcontact.model.contacts.Contacts;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理本地通讯录工具类
 * Created by Arison on 2017/6/19.
 * <p>
 * updata by bitliker on 2017/8/1 由于该工具类绑定由个别模块的activity 的内部类，无法实现抽离
 */

public class ContactsUtils {

    //查询所有联系人  

    public static List<Contacts> getContacts() {
        List<Contacts> contacts = new ArrayList<>();
        ContentResolver resolver = MyApplication.getInstance().getContentResolver();
        Cursor idCursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{android.provider.ContactsContract.Contacts._ID}, null, null, null);
        while (idCursor.moveToNext()) { //空指针
            Contacts contact = new Contacts();
            //获取到raw_contacts表中的id  
            int id = idCursor.getInt(0);
            String idstr = idCursor.getString(idCursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
            contact.setId(id);
            //根据获取到的ID查询data表中的数据  
            //Uri  uri = Uri.parse("content://com.android.contacts/contacts/" + id + "/data");
            Cursor dataCursor = resolver.query(
                    android.provider.ContactsContract.Data.CONTENT_URI,
//                    new String[]{
//                            android.provider.ContactsContract.Data.DATA1,
//                            android.provider.ContactsContract.Data.MIMETYPE,
//                            android.provider.ContactsContract.Data.RAW_CONTACT_ID}
                    null,
                    android.provider.ContactsContract.Data.CONTACT_ID + "=" + idstr,
                    null, null);
            while (dataCursor.moveToNext()) {
                Log.i("rawid", "-------------\n");
                String data = dataCursor.getString(dataCursor.getColumnIndex(android.provider.ContactsContract.Data.DATA1));
                String mimetype = dataCursor.getString(
                        dataCursor.getColumnIndex(android.provider.ContactsContract.Data.MIMETYPE));
                int rawid = dataCursor.getInt(2);
                contact.setRawid(rawid);
                Log.i("rawid", "某联系人下：" + rawid + "");
                if (mimetype.contains("/name")) {
                    contact.setName(data);
                }
                if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    int phoneType = dataCursor.getInt(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    contact.getPhones().add(data);
                    //手机号
                    if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        String mobile = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact.setPhone(mobile);
                    }
                    //住宅
                    if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                        if (StringUtil.isEmpty(contact.getPhone())) {
                            String mobile = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setPhone(mobile);
                        }
                    }
                    //单位
                    if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                        if (StringUtil.isEmpty(contact.getPhone())) {
                            String mobile = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setPhone(mobile);
                        }
                    }
                    //其它
                    if (StringUtil.isEmpty(contact.getPhone())) {
                        //String mobile = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.number));
                        contact.setPhone(data);
                    }
                }
                if (mimetype.contains("/nickname")) {
                    contact.setNickname(data);
                }
            }
            contacts.add(contact);
            dataCursor.close();
        }
        idCursor.close();
        return contacts;
    }

    public static List<Contacts> getContacts1() {
        List<Contacts> contacts = new ArrayList<>();
        String[] mContactsProjection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID
        };

        String contactsId;
        String phoneNum;
        String name;
        ContentResolver cr = MyApplication.getInstance().getContentResolver();
        //查询contacts表中的所有数据  
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, mContactsProjection, null, null, null);
       if (cursor!=null) {
           if (cursor.getCount() > 0) {
               while (cursor.moveToNext()) {
                   contactsId = cursor.getString(0);
                   phoneNum = cursor.getString(1);
                   name = cursor.getString(2);

                   Contacts model = new Contacts();
                   if (!StringUtil.isEmpty(phoneNum)) {
                       String phone = phoneNum.replace(" ", "").replace("-", "");
                       if (phone.length() >= 11) {
                           phone = phone.substring(phone.length() - 11, phone.length());
                       }
                       model.setPhone(phone);
                   } else {
                       model.setPhone("0");
                   }
                   if (!StringUtil.isMobileNumber(model.getPhone())) {
                       continue;
                   }
                   model.setName(name);
                   model.setId(Integer.valueOf(contactsId));
                   contacts.add(model);
               }
           }
       }
        return contacts;
    }


    public static void testAddContact() {
        for (int i = 0; i < 5500; i++) {
            addContact("测试" + i, "123456783" + i, "");
            LogUtil.d("Test", "导入数据i=" + i);
        }
    }

    private static void addContact(String name, String number, String email) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
            return;
        }
        ContentValues values = new ContentValues();
        Uri rawContactUri = BaseConfig.getContext().getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        //设置内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        //设置联系人名字
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        //向联系人URI添加联系人名字
        MyApplication.getInstance().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        //设置内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        //设置联系人电话号码
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
        //设置电话类型
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        //向联系人URI添加联系人电话号码
        MyApplication.getInstance().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        //设置内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        //设置联系人email
        values.put(ContactsContract.CommonDataKinds.Email.DATA, email);
        //设置Email类型
        values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME);
        //向联系人URI添加联系人名字
        MyApplication.getInstance().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);


    }
}
