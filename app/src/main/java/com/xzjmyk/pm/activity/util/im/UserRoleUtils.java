package com.xzjmyk.pm.activity.util.im;

import android.view.View;

import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.utils.CommonUtil;
import com.uas.appme.settings.activity.SettingActivity;
import com.xzjmyk.pm.activity.ui.erp.fragment.WorkPlatFragment;
import com.xzjmyk.pm.activity.ui.me.MeFragment;
import com.xzjmyk.pm.activity.ui.message.MessageFragment;


/**
 * Created by Arison on 2017/8/7.
 */

public class UserRoleUtils {

    public static String getUserRole(){
        String userRole= CommonUtil.getSharedPreferences(MyApplication.getInstance(),"userRole");
        return userRole==null?"":userRole;
    }

    public static void checkUserRole(Object fragment, View view) {
        String userRole= CommonUtil.getSharedPreferences(MyApplication.getInstance(),"userRole");
        try {
            if (!StringUtil.isEmpty(userRole)){
                if (userRole.equals("1")){//个人用户
                    if (fragment instanceof MessageFragment){
                        view.findViewById(R.id.schedule_rl).setVisibility(View.GONE);//审批流
                        view.findViewById(R.id.waitting_work_rl).setVisibility(View.GONE);//待办工作
                        view.findViewById(R.id.subscribe_rl).setVisibility(View.GONE);//我的订阅
                    }
                    if (fragment instanceof WorkPlatFragment){
                        view.findViewById(R.id.rl_uas_sys).setVisibility(View.GONE);
                        view.findViewById(R.id.my_client_rl).setVisibility(View.GONE);
                        view.findViewById(R.id.setting_rl).setVisibility(View.GONE);
                    }
                    if (fragment instanceof MeFragment){
                        view.findViewById(R.id.rl_company_change).setVisibility(View.GONE);
                        view.findViewById(R.id.rl_master_change).setVisibility(View.GONE);
                    }
                    if (fragment instanceof SettingActivity){
                        view.findViewById(R.id.sign_in_rl).setVisibility(View.GONE);
                        view.findViewById(R.id.sign_out_rl).setVisibility(View.GONE);
                    }
                }
                if (userRole.equals("2")){//uas用户
                    if (fragment instanceof MessageFragment){
                        view.findViewById(R.id.schedule_rl).setVisibility(View.VISIBLE);//审批流
                        view.findViewById(R.id.waitting_work_rl).setVisibility(View.VISIBLE);//待办工作
                        view.findViewById(R.id.subscribe_rl).setVisibility(View.VISIBLE);//我的订阅
                    }
                    if (fragment instanceof WorkPlatFragment){
                        view.findViewById(R.id.rl_uas_sys).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.my_client_rl).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.setting_rl).setVisibility(View.VISIBLE);
                    }
                    if (fragment instanceof MeFragment){
                        view.findViewById(R.id.rl_company_change).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.rl_master_change).setVisibility(View.VISIBLE);
                    }
                    if (fragment instanceof SettingActivity){
                        view.findViewById(R.id.sign_in_rl).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.sign_out_rl).setVisibility(View.VISIBLE);
                    }
                }
                if(userRole.equals("3")){//平台用户
                    if (fragment instanceof MessageFragment){
                        view.findViewById(R.id.schedule_rl).setVisibility(View.GONE);//审批流
                        view.findViewById(R.id.waitting_work_rl).setVisibility(View.GONE);//待办工作
                        view.findViewById(R.id.subscribe_rl).setVisibility(View.GONE);//我的订阅
                    }
                    if (fragment instanceof WorkPlatFragment){
                       // view.findViewById(R.id.rl_uas_sys).setVisibility(View.GONE);
                        view.findViewById(R.id.my_client_rl).setVisibility(View.GONE);
                        view.findViewById(R.id.setting_rl).setVisibility(View.GONE);
                    }
                }
            }else{
    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
