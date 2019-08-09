package com.core.api.wxapi;

import com.common.config.BaseConfig;
import com.core.app.Constants;


/**
 * Created by Arison on 2017/3/2.
 * 在ApiBase中定义公共方法(接口写在这里)
 * 在ApiModel中定义公共逻辑方法
 */
public class ApiPlatform extends ApiBase implements ApiModel {
    private String mBaseUrl = "";

    public String getBaseUrl() {
        return mBaseUrl;
//        return "http://218.17.158.219:9000/b2b_test/";
    }

    public ApiPlatform() {
        if (BaseConfig.isDebug()) {
            mBaseUrl = "http://b2b.usoftchina.com/";
//            mBaseUrl = "http://218.17.158.219:9000/b2b-test/";
            super.login = Constants.ACCOUNT_CENTER_HOST + "sso/login";
        } else {
            mBaseUrl = "http://b2b.usoftchina.com/";
//            mBaseUrl = "http://218.17.158.219:9000/b2b-test/";
            super.login = Constants.ACCOUNT_CENTER_HOST + "sso/login";
        }
        //审批流
        super.getAuditDone = mBaseUrl + "mobile/approvalflow/getAuditDone";
        //通讯录人员列表
        super.getUsersInfo = mBaseUrl + "mobile/approvalflow/getUsersInfo";
        //任务
        super.task_save = mBaseUrl + "mobile/mobiletask/saveTask";
        super.task_list = mBaseUrl + "mobile/mobiletask/getAllTasks";
        super.task_reply = mBaseUrl + "mobile/mobiletask/getTaskReply";
        super.task_change = mBaseUrl + "mobile/mobiletask/changeTaskStaus";
        super.plat_isend_task = mBaseUrl + "mobile/mobiletask/myTasks"; //我发起的任务
        //日报、考勤单据
        super.first_add_workdaily = mBaseUrl + "mobile/workDaily/saveWorkDaily"; // 首次提交工作日报
        super.delete_work_daily = mBaseUrl + "mobile/workDaily/deleteById"; //删除日报
        super.update_work_daily = mBaseUrl + "mobile/workDaily/updateWorkDaily"; //更新日报
        super.getdaily_list = mBaseUrl + "mobile/workDaily/getWorkDaily"; //获取日报列表
        super.common_app_flow_nodes_url = mBaseUrl + "mobile/approvalflow/getNodesAndLog";//单据获取审批流节点数据
        super.common_doc_detaily_url = mBaseUrl + "mobile/detailCenter/getDetail"; //获取单据详情
        super.common_delete_approval_flow_url = mBaseUrl + "mobile/detailCenter/resubmitDocuments";  //通用删除的审批流
        super.delete_common_doc_url = mBaseUrl + "mobile/detailCenter/deleteDocuments"; //通用单据的删除
        //变更处理人
        super.common_change_dealman_url = mBaseUrl + "mobile/detailCenter/changeDealMan";
        super.second_msg_detaily = mBaseUrl + "mobile/release/detail"; //消息第二层获取详情
        super.back_ids = mBaseUrl + "mobile/release/detail/back";
        //打卡2.0
        super.get_plat_senior_setting_url = mBaseUrl + "mobile/advancedSettings/getAdvancedSettings";//获取平台高级设置时间
        super.save_plat_senior_time_url = mBaseUrl + "mobile/advancedSettings/saveAdvancedSettings";//保存平台高级设置
        super.sign_get_office_url = mBaseUrl + "mobile/clockAddress/getSignCardAddress";// 获取平台办公地址
        super.save_plat_office_address_url = mBaseUrl + "mobile/clockAddress/saveSignCardAddress";//保存平台办公地址
        super.delete_plat_office_address_url = mBaseUrl + "mobile/clockAddress/deleteById";
        super.saveVisitRecord = mBaseUrl + "mobile/workSchedule/saveVisitRecord";
        super.releaseCount = mBaseUrl + "mobile/release/count";
        super.countBack = mBaseUrl + "mobile/release/count/back";
        super.add_people = mBaseUrl + "mobile/adduser/user";
        super.saveSignApp = mBaseUrl + "mobile/signapp/saveSignApp";


        /*strat by Bitliker*/ //外勤
        super.saveOutAddress = mBaseUrl + "mobile/outplan/saveOutAddress";
        super.saveOutSet = mBaseUrl + "mobile/outplan/saveOutSet";
        super.saveOutPlan = mBaseUrl + "mobile/outplan/saveOutPlan";
        super.saveOutSign = mBaseUrl + "mobile/outplan/saveOutSign";
        super.workSchedule = mBaseUrl + "mobile/workSchedule/getWorkSchedule";
        super.getTaskCounts = mBaseUrl + "mobile/mobiletask/getTaskCounts";
        super.getOutPlan = mBaseUrl + "mobile/outplan/getOutPlan";
        super.getAllWorkData = mBaseUrl + "mobile/workData/getWorkData";
        super.saveWorkData = mBaseUrl + "mobile/workData/saveWorkData";
        super.getOutSet = mBaseUrl + "mobile/outplan/getOutSet";
        super.getOutAddress = mBaseUrl + "mobile/outplan/getOutAddress";
        super.getUserInfo = mBaseUrl + "mobile/userCenter/getUserInfo";
        super.punch_record_url = mBaseUrl + "mobile/signCardLog/getListdata";
        super.punch_schedule_url = mBaseUrl + "mobile/clockSetCenter/getSignCardInfo";//班次接口
        super.punch_worksignin_url = mBaseUrl + "mobile/signCardLog/saveSignCard";
        super.updateOutplanStatus = mBaseUrl + "mobile/outplan/updateStatus";
        /*end by Bitliker*/

        super.overtime_save_url = mBaseUrl + "mobile/workOvertime/saveWorkOvertime";
        super.travel_save_url = mBaseUrl + "mobile/feePlease/saveFeePlease";
        super.leave_save_url = mBaseUrl + "mobile/vacation/saveVacation";
        super.common_doc_examine_and_approve_url = mBaseUrl + "mobile/approvalflow/auditDocuments";
        super.getAuditTodo = mBaseUrl + "mobile/approvalflow/getAuditTodo";
        super.list_vacation = mBaseUrl + "mobile/vacation/getAllVacation";
        super.list_workOvertime = mBaseUrl + "mobile/workOvertime/getWorkOvertime";
        super.list_feePlease = mBaseUrl + "mobile/feePlease/getFeePlease";
    }

    public String getmBaseUrl() {
        return mBaseUrl;
    }
}
