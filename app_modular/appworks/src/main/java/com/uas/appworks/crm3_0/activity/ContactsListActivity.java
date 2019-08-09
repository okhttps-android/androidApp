package com.uas.appworks.crm3_0.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.common.system.DisplayUtil;
import com.core.base.BaseActivity;
import com.modular.apputils.widget.MenuVoiceSearchView;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.adapter.ContactsLocalAdapter;
import com.uas.appworks.crm3_0.fragment.ContactsListFragment;
import com.uas.appworks.crm3_0.fragment.LocalContactsListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
   * @desc:联系人列表+选项卡
   * @author：Arison on 2018/9/12
   */
public class ContactsListActivity extends BaseActivity implements  ContactsLocalAdapter.ResultItemsInface {

     private String[] tabTitle ;
     private TabLayout mTabLayout;
     private ViewPager mViewPager;
     private ContactsListFragment fragmentMeList,fragmentCusList;
     private LocalContactsListFragment localContactsListFragment;
     private MenuVoiceSearchView mVoiceSearchView;
    
     
     

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabTitle = new String[]{"我的联系人", "手机通讯录"};
        initView();
    }

     private void initView() {
         mTabLayout = findViewById(R.id.mTabLayout);
         mViewPager = findViewById(R.id.mViewPager);
         mVoiceSearchView = findViewById(R.id.mVoiceSearchView);
         mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
         mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[0]));//添加tab选项卡
         mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[1]));
//         mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[2]));
         findViewById(R.id.backImg).setOnClickListener(mOnClickListener);
         findViewById(R.id.addImg).setOnClickListener(mOnClickListener);
         ViewPageAdapter mAdapter = new ViewPageAdapter(getSupportFragmentManager());
         mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
         mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来
         
         mViewPager.setCurrentItem(getIntent().getIntExtra("tabItem",0));
       
         mVoiceSearchView.getSearch_edit().setKeyListener(null);
         mVoiceSearchView.getSearch_edit().setFocusable(false);
         mVoiceSearchView.getSearch_edit().setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(mContext,ContactSearchActivity.class));
             }
         });
     }

     @Override
     public int getToolBarId() {
         return R.id.cycleCountToolBar;
     }

     @Override
     public int getLayoutRes() {
         return R.layout.activity_contacts_list;
     }

    @Override
    public void onResultForItems(View view, ContactsModel model, int position) {
        
    }


    private class ViewPageAdapter extends FragmentPagerAdapter {


         public ViewPageAdapter(FragmentManager fm) {
             super(fm);
         }

         @Override
         public Fragment getItem(int position) {
             ContactsListFragment fragment=null;
             int tabItem = position + 1;
             switch (position){
                 case 0:
                     if (fragmentMeList==null){
                         fragmentMeList=ContactsListFragment.newInstance(tabItem);
                     }
                     fragment=fragmentMeList;
                     break;
                 case 1:
                     if ( localContactsListFragment==null){
                         localContactsListFragment=LocalContactsListFragment.newInstance(tabItem);
                     }
                     return localContactsListFragment;
                  //   break;
             }
             return fragment;
         }

         @Override
         public CharSequence getPageTitle(int position) {
             return tabTitle[position];
         }

         @Override
         public int getCount() {
             return tabTitle.length;
         }
     }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.backImg) {
                onBackPressed();
            }
            if(view.getId()==R.id.addImg){
                showPopupWindow(view);
            }
        }
    };



    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getPopData(),
                    R.layout.item_pop_list,
                    new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                           startActivity(new Intent(ContactsListActivity.this,ContactsAddActivity.class));
                           break;
                        case 1:
                            startActivity(new Intent(ContactsListActivity.this,ContactsAddActivity.class)
                            .putExtra("action",0));
                            break;
                        case 2:
                            startActivity(new Intent(ContactsListActivity.this,ContactsAddActivity.class)
                                    .putExtra("action",1));
                            break;
                    }
                    closePoppupWindow();
                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() / 3, windowManager.getDefaultDisplay().getHeight() / 3);
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ContactsListActivity.this, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
    }

    private void closePoppupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }


    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<>();
        map = new HashMap<String, Object>();
        map.put("item_name", "添加联系人");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("item_name","通讯录导入");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("item_name","名片导入");
        list.add(map);
        return list;
    }
}
