package com.core.api.wxapi;

import com.core.app.MyApplication;
import com.core.utils.CommonUtil;

/**
 * Created by Arison on 2017/3/2.
 */
public abstract class ApiBase {
    //定义各种公共接口
    public String login;//登录
    public String getMasters;//获取账套
    public String cookie = "";
    public String enuu = "";

    //消息
    public String obtain_announce_url;//公告中心：通过企业uu获取所有公告
    public String news_center_url;//新闻中心
    public String notification_center_url;//通知中心
    public String getUserInfo;//用户信息查询：（管理员/非管理员）

    //uas 考勤单据
    public String travel_request_url;//出差申请
    public String leave_application_url;//请假单
    public String work_overtime_url;//加班申请

    //审批流接口
    public String getAuditTodo;//待审批
    public String getAuditDone;//已审批
    public String getUsersInfo;//人员信息
    //任务
    public String task_save;
    public String task_list;
    public String task_reply;
    public String task_change;
    public String plat_isend_task;

    //  考勤单据列表接口
    public String list_vacation;//请假单列表
    public String list_workOvertime;//加班单列表
    public String list_feePlease;//出差单列表

    //考勤班次
    public String punch_address_url;//打卡地址
    public String punch_schedule_url;//打卡班次
    public String punch_record_url;//员工打卡记录
    //打卡办公地址设置
    public String sign_get_office_url; // 获取平台办公地址
    public String save_plat_office_address_url; //保存平台办公地址
    public String delete_plat_office_address_url; // 删除平台打卡地址
    public String punch_worksignin_url;//打卡
    //打卡2.0
    public String getAllWorkData;//获取打卡班次
    public String saveWorkData;//提交打卡数据

    //打卡高级设置
    public String get_plat_senior_setting_url;//获取平台高级设置时间
    public String save_plat_senior_time_url;//保存平台高级设置

    //外勤
    public String saveOutAddress;//保存外勤地址
    public String getOutAddress;//获取外勤地址
    public String saveOutSet;//保存外勤设置
    public String getOutSet;//获取外勤设置
    public String getOutPlan;//获取外勤计划列表
    public String saveOutPlan;//保存外勤计划
    public String saveOutSign;//保存外勤打卡记录
    public String updateOutplanStatus;

    //oa首页
    public String workSchedule;//工作日程  oa首页

    public String getTaskCounts;//获取首页消息数量

    //工作日报
    public String first_add_workdaily;  // 首次提交工作日报
    public String delete_work_daily; //删除日报
    public String update_work_daily;//更新日报
    public String getdaily_list; //获取日报列表
    public String overtime_save_url;//加班保存
    public String travel_save_url;//出差保存
    public String leave_save_url;//请假保存
    public String common_doc_examine_and_approve_url;

    public String common_app_flow_nodes_url;//单据获取审批流节点数据
    public String common_doc_detaily_url; //获取单据详情

    public String common_delete_approval_flow_url; //通用删除的审批流
    public String delete_common_doc_url;//通用单据的删除

    public String common_change_dealman_url; //变更处理人
    public String saveVisitRecord;//保存拜访报告
    public String releaseCount;//获取消息红点外层接口
    public String countBack;//阅读消息红点
    public String second_msg_detaily; // 消息第二层红点
    public String back_ids;
    public String add_people;
    public String saveSignApp;//提交申述

    public String getCookie() {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            //获取不同身份的cookie
            cookie = CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_cookie");
        }
        if (ApiUtils.getApiModel() instanceof ApiUAS) {

        }
        return cookie;
    }

    public void setCookie(String cookie) {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            //保存不同身份的cookie
            CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_cookie", cookie);
        }
        if (ApiUtils.getApiModel() instanceof ApiUAS) {

        }
        this.cookie = cookie;
    }

    public String getEnuu() {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            //获取不同身份的cookie
            enuu = CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu");
        }
        if (ApiUtils.getApiModel() instanceof ApiUAS) {

        }
        return enuu;
    }

    public void setEnuu(String enuu) {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            //保存不同身份的cookie
            CommonUtil.setSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu", enuu);
        }
        if (ApiUtils.getApiModel() instanceof ApiUAS) {

        }
        this.enuu = enuu;
    }
}
