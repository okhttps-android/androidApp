package com.modular.appmessages.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.thread.ThreadUtil;
import com.core.app.MyApplication;
import com.core.dao.DBManager;
import com.core.model.Approval;
import com.core.model.EmployeesEntity;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.widget.view.selectcalendar.bean.Data;
import com.modular.appmessages.R;
import com.modular.appmessages.model.ApprovalRecord;
import com.modular.appmessages.presenter.imp.IApproval;
import com.modular.apputils.utils.MathUtil;
import com.uas.appworks.OA.erp.activity.form.DataFormFieldActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.core.model.Approval.DETAIL;

/**
 * Created by Bitliker on 2017/7/7.
 */

public class ApprovaPresenter implements OnHttpResultListener {
    private final int LOAD_ALLHISTORY_NODES = 0x35;    //下拉历史审批要点
    private final int LOAD_TAKE_OVER_OTHER = 0x34;    //下一个处理人
    private final int LOAD_NEXT_STEPOFP_INSTANCE = 0x33;
    private final int LOAD_PROCESS_UPDATE = 0x32;//更新审批必填字段
    private final int LOAD_END_PROCESS = 0x31;   //结束流程
    private final int LOAD_TAKE_OVER = 0x30;    //接管
    private final int LOAD_FILE_PATHS = 0x29;//获取文件信息
    private final int LOAD_NEXT_PROCESS = 0x28;//获取下一条
    private final int LOAD_UPDATE_ASSIGNEE = 0x27;//更新处理人
    private final int LOAD_AGREE = 0x26;        //同意
    private final int LOAD_DISAGREE = 0x25;//不同意
    private final int LOAD_SETUP_TASK = 0x24;//审批要点
    private final int LOAD_CURRENT_NODE = 0x23;//获取当节点(nodeId,coller)
    private final int LOAD_JNODES = 0x22;//历史节点
    private final int LOAD_FORMAND_GRIDDATA = 0x21;//当前明细表

    private IApproval iApproval;
    private ApprovalRecord record;
    public String chchePoints = "";//审批要点缓存
    private String title;
    private String master;

//    private List<Approval> approvals;

    private List<Approval> hineApprovals;//隐藏字段
    private List<Approval> showApprovals;//显示字段

    private List<Approval> historyNodes;//历史审批

    private Approval titleApproval;
    private List<Approval> mainDetailList;//主从表
    private List<Approval> setuptasList;//历史审批要点
    private List<Approval> enclosureList;//附件
    private List<Approval> nodeList;//审批节点
    private List<Approval> pointsList;//要点

    private boolean submit, loading, endActivity;

    public ApprovaPresenter(IApproval iApproval, Intent intent) {
        this.iApproval = iApproval;
        endActivity = false;
        if (intent != null) {
            int id = intent.getIntExtra("nodeid", -1);
            Log.d("approvalId", id + "");
//            String imid = intent.getStringExtra("imid");
            title = intent.getStringExtra("title");
            master = intent.getStringExtra("master");
            if (StringUtil.isEmpty(master)) master = CommonUtil.getMaster();
            if (id != -1) {
                initLoad(id);
            } else {
                initLoad(0);
            }
        } else {
            initLoad(0);
        }
    }

    public boolean isApprovaling() {
        if (StringUtil.isEmpty(title)) return false;
        return title.equals(StringUtil.getMessage(R.string.title_approval));
    }

    public boolean isApprovaled() {
        if (StringUtil.isEmpty(title)) return false;
        return title.equals(StringUtil.getMessage(R.string.task_confimed));
    }

    public void trun2SetActivity(Activity activity) {
        if (loading) return;
        ArrayList<Data> fields = new ArrayList<>();
        ArrayList<Data> fieldsDis = new ArrayList<>();
        if (!ListUtils.isEmpty(hineApprovals)) {
            for (Approval approval : hineApprovals) {
                fields.add(new Data(approval.getType() == Approval.MAIN, approval));
            }
        }
        if (!ListUtils.isEmpty(showApprovals)) {
            for (Approval approval : showApprovals) {
                fieldsDis.add(new Data(approval.getType() == Approval.MAIN, approval));
            }
        }
        activity.startActivityForResult(new Intent(activity, DataFormFieldActivity.class)
                        .putParcelableArrayListExtra("fields", fields)
                        .putParcelableArrayListExtra("fieldsDis", fieldsDis)
                        .putExtra("master", master)
                        .putExtra("caller", record.caller),
                0x25);
    }

    public void closeDB() {
        endActivity = true;
    }

    public String getUrl(String baseUrl, String title) {
        String endStatus = (StringUtil.isEmpty(title) || title.equals(StringUtil.getMessage(R.string.title_approval)) ? "" : "%26_do=1");
        if (StringUtil.isEmpty(baseUrl) || endStatus.contains("26_do=1")) {
            baseUrl = "jsps/mobile/process.jsp?nodeId=";
        }
        return baseUrl + record.nodeId + endStatus;
    }

    public List<String> getNodesCanReturn() {
        List<String> list = new ArrayList<>();
        if (record.isForknode && !ListUtils.isEmpty(historyNodes)) {
            for (Approval a : historyNodes) {
                if (a.isMustInput() && a.isNeerInput()) {
                    list.add(a.getValuesKey());
                }
            }
        }
        return list;
    }

    public String getMaster() {
        return master == null ? CommonUtil.getMaster() : master;
    }

    public String getCaller() {
        return record == null ? "" : record.caller;
    }

    public void initLoad() {
        initLoad(record.nodeId);
    }

    private void initLoad(int nodeId) {
        record = new ApprovalRecord();
        titleApproval = new Approval(Approval.TITLE);
        mainDetailList = new ArrayList<>();//主从表
        setuptasList = new ArrayList<>();//历史审批要点
        enclosureList = new ArrayList<>();//附件
        nodeList = new ArrayList<>();//审批节点
        pointsList = new ArrayList<>();//要点
        hineApprovals = new ArrayList<>();
        showApprovals = new ArrayList<>();
        historyNodes = null;
        record.nodeId = nodeId;
        iApproval.initStatus();
        submit = false;
        loading = false;
        loadCurrentNode();
    }


    /**
     * start 提交部分
     */
    /*变更处理人 submiting*/
    public void updateAssignee(String emCode, String nodeLog) {
        if (StringUtil.isEmpty(nodeLog)) {
            iApproval.showToast(R.string.approval_opinion_error, R.color.load_submit);
            return;
        }
        if (submit) {
            iApproval.showToast(R.string.submit_cannot_submit_again, R.color.load_submit);
            return;
        }
        if (StringUtil.isEmpty(emCode)) {
            loadNextProcess();
            return;
        }
        iApproval.showLoading();
        String url = "common/setAssignee.action";
        Map<String, Object> param = new HashMap<>();
        param.put("taskId", record.nodeId);
        param.put("master", master);
        param.put("assigneeId", emCode);//新处理人的员工编号
        param.put("processInstanceId", record.processInstanceId);//流程实例id
        param.put("description", nodeLog);//流程实例id
        param.put("_center", "0");//是否集团账套
        param.put("_noc", 1);//权限管控
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setWhat(LOAD_UPDATE_ASSIGNEE)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*接管 submiting*/
    public void loadTakeOver() {
        if (submit || loading) return;
        submit = true;
        iApproval.showLoading();
        String url = "common/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        param.put("em_code", CommonUtil.getEmcode());
        param.put("nodeId", record.nodeId);
        param.put("master", master);
        param.put("needreturn", true);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setWhat(LOAD_TAKE_OVER)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*提交更新必填字段 submiting*/
    public void loadProcessUpdate(String nodeLog, List<Approval> approvals) {
        if (!canSubmit())
            return;
        submit = true;
        StringBuilder pointsBuilder = new StringBuilder();
        if (!inputAllPoints(pointsBuilder, approvals)) {
            submit = false;
            return;
        }
        chchePoints = pointsBuilder.toString();
        String url = "common/processUpdate.action";
        Map<String, Object> param = new HashMap<>();
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> formStore = new HashMap<>();
        if (!inputAllInput(params, formStore, approvals)) {
            submit = false;
            return;
        }
        if ((formStore.isEmpty() || formStore.size() <= 1) && ListUtils.isEmpty(params)) {
            agree(nodeLog);
            return;
        }
        iApproval.showLoading();
        param.put("caller", record.caller);
        param.put("master", master);
        param.put("processInstanceId", record.processInstanceId);
        String form = JSONUtil.map2JSON(formStore);
        param.put("formStore", StringUtil.isEmpty(form) ? "{}" : form);
        param.put("param", JSONUtil.map2JSON(params));
        Bundle bundle = new Bundle();
        bundle.putString("nodeLog", nodeLog);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setBundle(bundle)
                .setWhat(LOAD_PROCESS_UPDATE)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*同意 submiting end loadProcessUpdate*/
    private void agree(String nodeLog) {
        LogUtil.i("agree");
        iApproval.showLoading();
        String points = StringUtil.getMessage(chchePoints);
        points.replaceAll("@", "").replaceAll("\\@", "").replaceAll("\\\\@", "");
        String url = "common/review.action";
        Map<String, Object> param = new HashMap<>();
        param.put("taskId", record.nodeId);
        param.put("nodeName", record.nodeName);
        param.put("nodeLog", StringUtil.getMessage(nodeLog));
        param.put("result", true);
        param.put("master", master);
        param.put("attachs", "");//附件id
        param.put("_center", "0");//是否集团账套
        param.put("_noc", 1);//权限管控
        param.put("holdtime", 4311);//holdtime
        param.put("customDes", points);//审批要点customDes=1.11111(2)
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setWhat(LOAD_AGREE)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    //结束流程
    private void loadEndProcess() {
        LogUtil.i("loadEndProcess");
        //结束流程接口部分
//        String url = "common/endProcessInstance.action";
//        Map<String, Object> param = new HashMap<>();
//        param.put("processInstanceId", record.processInstanceId);
//        param.put("holdtime", 435);
//        param.put("nodeId", record.nodeId);
//        param.put("master", master);
        iApproval.showLoading();
        String url = "common/review.action";
        Map<String, Object> param = new HashMap<>();
        param.put("taskId", record.nodeId);
        param.put("nodeName", record.nodeName);
        param.put("nodeLog", "流程异常，结束流程");
        param.put("master", master);
        param.put("result", false);
        param.put("backTaskName", "RECORDER");//jn_name
        param.put("attachs", "");//附件id
        param.put("_noc", 1);//权限管控
        param.put("holdtime", 4311);//holdtime
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_END_PROCESS)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*获取下一个节点 loading*/
    public void loadNextProcess() {
        if (loading) return;
        iApproval.showToast(R.string.loadtonext_approval, R.color.load_submit);
        iApproval.showLoading();
        String url = "common/getNextProcess.action";
        Map<String, Object> param = new HashMap<>();
        param.put("taskId", record.nodeId);
        param.put("master", master);
        param.put("_noc", 1);//权限管控
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setWhat(LOAD_NEXT_PROCESS)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*不同意 submiting*/
    public void disAgree(String nodeLog, String backTaskName) {
        if (StringUtil.isEmpty(nodeLog)) {
            iApproval.showToast(R.string.approval_opinion_error, R.color.load_submit);
            return;
        }

        if (!canSubmit()) return;
        iApproval.showLoading();
        String url = "common/review.action";
        Map<String, Object> param = new HashMap<>();
        param.put("taskId", record.nodeId);
        param.put("nodeName", record.nodeName);
        param.put("nodeLog", nodeLog);
        param.put("master", master);
        param.put("result", false);
        if (StringUtil.isEmpty(backTaskName)) backTaskName = "RECORDER";
        param.put("backTaskName", backTaskName);//jn_name
        param.put("attachs", "");//附件id
        param.put("_noc", 1);//权限管控
        param.put("holdtime", 4311);//holdtime
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setWhat(LOAD_DISAGREE)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*获取下一节点审批人 loading*/
    public void judgeApprovers() {
        String url = "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", record.caller);
        param.put("id", record.id);
        param.put("master", master);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_NEXT_STEPOFP_INSTANCE)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*选择审批人后提交给他处理下一节点 submiting  end judgeApprovers */
    public void loadTakeOver2Other(String emCode) {
        iApproval.showLoading();
        String url = "common/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        params.put("em_code", emCode);
        params.put("nodeId", record.chcheNode);

        param.put("params", JSONUtil.map2JSON(params));
        param.put("_noc", "1");
        param.put("master", master);

        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setMode(Request.Mode.POST)
                .setWhat(LOAD_TAKE_OVER_OTHER)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }
    /**
     * end 提交部分
     */

    /**
     * start下拉部分
     */
    /*下拉当前节点数据  loading*/
    private void loadCurrentNode() {
        if (loading) return;
        iApproval.showLoading();
        String url = "common/getCurrentNode.action";
        Map<String, Object> param = new HashMap<>();
        param.put("jp_nodeId", record.nodeId);
        param.put("master", master);
        param.put("_noc", 1);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_CURRENT_NODE)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*下拉明细表数据 loading*/
    private void loadDetailedList() {
        if (loading) return;
        loading = true;
        iApproval.showLoading();
        String url = "mobile/common/getformandgriddata.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", record.caller);
        param.put("master", master);
        param.put("id", record.id);
        param.put("isprocess", 1);
        param.put("config", 1);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_FORMAND_GRIDDATA)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*下拉历史审批要点*/
    private void loadAllHistoryNodes() {
        iApproval.showLoading();
        String url = "common/getAllHistoryNodes.action";
        Map<String, Object> param = new HashMap<>();
        param.put("master", master);
        param.put("processInstanceId", record.processInstanceId);
        param.put("_noc", 1);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_ALLHISTORY_NODES)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*下拉审批要点 loading*/
    private void loadCustomSetupOfTask() {
        iApproval.showLoading();
        String url = "common/getCustomSetupOfTask.action";
        Map<String, Object> param = new HashMap<>();
        param.put("_noc", 1);
        param.put("master", master);
        param.put("nodeId", record.nodeId);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_SETUP_TASK)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*获取历史节点数据 loading*/
    private void loadJnodes(String historyNode) {
        iApproval.showLoading();
        String url = "common/getCurrentJnodes.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", record.caller);
        param.put("keyValue", record.id);
        param.put("_noc", 1);
        param.put("master", master);
        Bundle bundle = new Bundle();
        bundle.putString("historyNode", historyNode);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_JNODES)
                .setBundle(bundle)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /*获取附件  */
    private void loadFilePaths(String attachs) {
        if (StringUtil.isEmpty(attachs) || "null".equals(attachs)) {
            return;
        }
        iApproval.showLoading();
        String url = "common/getFilePaths.action";
        Map<String, Object> param = new HashMap<>();
        param.put("field", "fb_attach");
        param.put("master", master);
        param.put("id", attachs);
        Request.Bulider bulider = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_FILE_PATHS)
                .setParam(param);
        OAHttpHelper.getInstance().requestHttp(bulider.bulid(), this);
    }

    /**
     * end下拉部分
     */

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (endActivity) {
            loading = false;
            submit = false;
            return;
        }
        try {
            if (!isJSON) {
                LogUtil.i("!isJSON");
                iApproval.showToast(StringUtil.isEmpty(message) ? "程序错误" : message, R.color.load_submit);
                return;
            }
            hander(what, JSON.parseObject(message), bundle);
        } catch (Exception e) {
            if (e != null) {
                LogUtil.i("result Exception=" + e.getMessage());
            }
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        LogUtil.i("message=" + message);
        if (endActivity) return;
        iApproval.dimssLoading();
        switch (what) {
            case LOAD_AGREE:
                if (message.contains("程序错误")) {
                    loadEndProcess();
                }
                break;
            case LOAD_TAKE_OVER:  /*提交部分*/
            case LOAD_PROCESS_UPDATE:
            case LOAD_NEXT_STEPOFP_INSTANCE:
            case LOAD_TAKE_OVER_OTHER:
                break;
        }
        submit = false;
        String errorKey = "exceptionInfo";
        if (JSONUtil.validate(message) && !StringUtil.isEmpty(JSONUtil.getText(parseObject(message), errorKey))) {
            String remain = JSONUtil.getText(parseObject(message), errorKey);
            iApproval.showToast(remain, R.color.load_submit);
        } else {
            iApproval.showToast(message, R.color.load_submit);
        }
    }

    private void hander(int what, final JSONObject object, Bundle bundle) throws Exception {
        switch (what) {
            case LOAD_CURRENT_NODE:
                handlerCurrentNode(JSONUtil.getJSONObject(object, "info"));
                break;
            case LOAD_FORMAND_GRIDDATA:
                ThreadUtil.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handlerFormandGriddataInThread(JSONUtil.getJSONObject(object, "datas"));
                        } catch (Exception e) {
                            LogUtil.i("handlerFormandGriddataInThread =" + e.getMessage());
                            loading = false;
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case LOAD_ALLHISTORY_NODES:
                loadJnodes(object.toString());
//                handlerHistorySetuptask(object);
                break;
            case LOAD_JNODES:
                handlerNode(object, bundle);
                break;
            case LOAD_SETUP_TASK:
                handlerSetupTask(object);
                break;
            case LOAD_NEXT_PROCESS:
                if (loading) return;
                int nextnode = JSONUtil.getInt(object, "nodeId");
                if (nextnode > 0) {
                    initLoad(nextnode);
                } else {
                    iApproval.finish();
                    ToastUtil.showToast(MyApplication.getInstance(), R.string.load_not_next_data);
                }
                break;
            case LOAD_UPDATE_ASSIGNEE:
                nextnode = JSONUtil.getInt(object, "nextnode");
                if (nextnode > 0) {
                    iApproval.showToast(R.string.load_next_approval, R.color.load_submit);
                    initLoad(nextnode);
                } else {
                    iApproval.showToast(StringUtil.getMessage(R.string.make_adeal_success) + StringUtil.getMessage(R.string.load_not_next_data), R.color.load_submit);
                    iApproval.finish();
                }
                break;
            case LOAD_FILE_PATHS:
                handlerEnclosure(JSONUtil.getJSONArray(object, "files"));
                break;
            case LOAD_TAKE_OVER://接管成功后
                if (JSONUtil.getBoolean(object, "success")) {
                    initLoad(record.nodeId);
                } else {
                    iApproval.showToast(R.string.load_take_over_error, R.color.load_submit);
                }
                break;
            case LOAD_DISAGREE:
                nextnode = JSONUtil.getInt(object, "nextnode");
                if (nextnode > 0) {
                    iApproval.showToast(R.string.load_next_approval, R.color.load_submit);
                    initLoad(nextnode);
                } else {
                    iApproval.showToast(StringUtil.getMessage(R.string.make_adeal_success) + StringUtil.getMessage(R.string.load_not_next_data), R.color.load_submit);
                    iApproval.finish();
                }
                break;
            case LOAD_AGREE:
                iApproval.showToast(R.string.make_adeal_success, R.color.load_submit);
                judgeApprovers();
                break;
            case LOAD_NEXT_STEPOFP_INSTANCE:
                handlerNextStepoInstance(object);
                break;
            case LOAD_TAKE_OVER_OTHER:
                loadNextProcess();
                break;
            case LOAD_END_PROCESS:
                iApproval.endProcess();
                break;
            case LOAD_PROCESS_UPDATE:
                String nodeLog = null;
                if (bundle != null) {
                    nodeLog = bundle.getString("nodeLog");
                }
                agree(nodeLog);
                break;
        }
        iApproval.dimssLoading();
    }


    private void handlerNextStepoInstance(JSONObject object) {
        if (object.containsKey("assigns")) {
            JSONArray array = JSONUtil.getJSONArray(object, "assigns");
            JSONObject o = array.getJSONObject(0);
            String noid = JSONUtil.getText(o, "JP_NODEID");
            JSONArray data = null;
            if (o != null && o.containsKey("JP_CANDIDATES")) {
                data = o.getJSONArray("JP_CANDIDATES");
            }
            if (!StringUtil.isEmpty(noid) && data != null && data.size() > 0) {
                record.chcheNode = noid;
                iApproval.sendToSelect(data);
            } else {
                loadNextProcess();
            }
        } else {
            loadNextProcess();
        }
    }

    //处理当前结点信息
    private void handlerCurrentNode(JSONObject object) throws Exception {
        if (object != null && !loading) {
            record.processInstanceId = JSONUtil.getText(object, "InstanceId");
            record.isForknode = JSONUtil.getInt(object, "forknode") == 0;
            JSONObject currentnode = JSONUtil.getJSONObject(object, "currentnode");
            if (currentnode != null) {
                String recordName = JSONUtil.getText(currentnode, "jp_launcherName");
                record.currentNodeMan = JSONUtil.getText(currentnode, "jp_nodeDealMan");
                String launcherCode = JSONUtil.getText(currentnode, "jp_launcherId");
                iApproval.nodeDealMan(record.currentNodeMan);
                String nodeName = JSONUtil.getText(currentnode, "jp_nodeName");
                int keyValue = JSONUtil.getInt(currentnode, "jp_keyValue");
                record.title = record.callerName = JSONUtil.getText(currentnode, "jp_name");
                record.status = JSONUtil.getText(currentnode, "jp_status");
                String caller = JSONUtil.getText(currentnode, "jp_caller");
                if (keyValue != 0) {
                    record.id = keyValue;
                }
                if (!StringUtil.isEmpty(record.title)) {
                    if (!StringUtil.isEmpty(recordName)) {
                        record.title = recordName + " " + record.title;
                    }
                }
                if (!StringUtil.isEmpty(caller)) {
                    record.caller = caller;
                }
                if (!StringUtil.isEmpty(nodeName)) {
                    record.nodeName = nodeName;
                }
                if (StringUtil.isEmpty(record.imid)) {
                    record.imid = String.valueOf(getImByCode(launcherCode));
                }
            }
            JSONObject button = JSONUtil.getJSONObject(object, "button");
            if (currentnode != null) {
                record.needInputKeys = JSONUtil.getText(button, "jt_neccessaryfield");
            }
            handerTitle(0);
            loadDetailedList();//获取明细表
        } else {
            LogUtil.i("loading=" + loading);
            loading = false;
        }
    }

    private void handlerFormandGriddataInThread(JSONObject object) throws Exception {
        if (object != null) {
            //计算主表
            JSONArray formdatas = JSONUtil.getJSONArray(object, "formdata");
            JSONArray formconfigs = JSONUtil.getJSONArray(object, "formconfigs");
            List<Approval> mainDetailList = new ArrayList<>();
            if (!ListUtils.isEmpty(formconfigs)) {
                final JSONObject formdata = ListUtils.isEmpty(formdatas) ? null : formdatas.getJSONObject(0);
                final JSONObject changeData = ListUtils.getSize(formdatas) <= 1 ? null : formdatas.getJSONObject(1);
                if (!ListUtils.isEmpty(formconfigs)) {
                    final List<Approval> mainApproval = formandGriddata(JSONUtil.getJSONObject(changeData, "change-new"), formdata, formconfigs, record.caller, true, true);
                    mainDetailList.addAll(mainApproval);
                }
            }
            //计算从表
            JSONArray griddatas = JSONUtil.getJSONArray(object, "griddata");
            JSONArray gridconfigs = JSONUtil.getJSONArray(object, "gridconfigs");
            final List<Approval> detailedList = new ArrayList<>();
            if (!ListUtils.isEmpty(gridconfigs)) {
                if (ListUtils.isEmpty(griddatas)) {
                    formandGriddata(null, null, gridconfigs, record.caller, false, true);
                } else {
                    for (int i = 0; i < griddatas.size(); i++) {
                        //获取到单个明细表单
                        final List<Approval> detailedApproval = formandGriddata(null, griddatas.getJSONObject(i),
                                gridconfigs, record.caller,
                                false, i == 0);
                        if (!ListUtils.isEmpty(detailedApproval)) {
                            Approval approval = new Approval(Approval.TAG);

                            if (!TextUtils.isEmpty(record.currentNodeMan)
                                    && record.caller.toUpperCase().equals("INQUIRY")
                                    && isApprovaling()
                                    && CommonUtil.getEmcode().equals(record.currentNodeMan)) {
                                approval.setValues("初始化");
                            }
                            approval.setCaption(i == 0 ? record.getCallerName() + " 明细" : "");
                            detailedApproval.add(0, approval);
                            detailedList.addAll(detailedApproval);

                        }
                    }
                }
            }
            //计算多从表
            JSONArray othergrids = JSONUtil.getJSONArray(object, "othergrids");
            if (!ListUtils.isEmpty(othergrids)) {
                JSONObject o = null;
                String caller = null;
                JSONArray otherGriddata = null;
                JSONArray otherGridconfigs = null;
                String name = null;
                for (int i = 0; i < othergrids.size(); i++) {
                    o = othergrids.getJSONObject(i);
                    name = JSONUtil.getText(o, "name");
                    caller = JSONUtil.getText(o, "caller");
                    otherGriddata = JSONUtil.getJSONArray(o, "griddata");
                    otherGridconfigs = JSONUtil.getJSONArray(o, "gridconfigs");
                    if (!ListUtils.isEmpty(otherGriddata) && !ListUtils.isEmpty(otherGridconfigs)) {
                        for (int j = 0; j < otherGriddata.size(); j++) {
                            //获取到单个明细表单
                            final List<Approval> detailedApproval = formandGriddata(null, otherGriddata.getJSONObject(j), otherGridconfigs, caller, false, false);
                            if (!ListUtils.isEmpty(detailedApproval)) {
                                Approval approval = new Approval(Approval.TAG);
                                approval.setCaption(j == 0 ? name + " 明细" : "");
                                detailedApproval.add(0, approval);
                                detailedList.addAll(detailedApproval);
                            }
                        }
                    }
                }
            }
            mainDetailList.addAll(detailedList);
            ApprovaPresenter.this.mainDetailList = mainDetailList;
            setData2ListThread();
            OAHttpHelper.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    if (isApprovaling()) {
                        loadCustomSetupOfTask();
                    } else {
                        loadAllHistoryNodes();
                    }
                }
            });
        } else {
            iApproval.showToast(R.string.not_data_from_formandgrid);
        }
    }

    private List<Approval> formandGriddata(final JSONObject changeData,
                                           final JSONObject data,
                                           final JSONArray configs,
                                           String caller,
                                           boolean isMain,
                                           boolean addHint) throws Exception {
        List<Approval> approvals = new ArrayList<>();
        String idTag = "";
        int id = 0;
        StringBuilder merged = new StringBuilder();
        for (int i = 0; i < configs.size(); i++) {
            JSONObject config = configs.getJSONObject(i);
            if (config == null) continue;
            Approval approval = new Approval(isMain ? Approval.MAIN : DETAIL);
            String caption = JSONUtil.getText(config, "FD_CAPTION", "DG_CAPTION");//获取第一个字段字段名称
            String valueKey = JSONUtil.getText(config, "FD_FIELD", "DG_FIELD");
            JSONArray combostore = JSONUtil.getJSONArray(config, "COMBOSTORE");
            String type = JSONUtil.getText(config, "FD_TYPE", "DG_TYPE");
            String dbFind = JSONUtil.getText(config, "FD_DBFIND", "DG_TYPE");
            //添加类型
            approval.setDbFind(dbFind);
            approval.setDfType(type);
            approval.setCaption(caption);
            approval.setValuesKey(valueKey);
            int isdefault = JSONUtil.getInt(config, "MFD_ISDEFAULT", "MDG_ISDEFAULT");
            int appwidth = JSONUtil.getInt(config, "FD_APPWIDTH", "DG_APPWIDTH");
            boolean showAble = data != null && data.containsKey(valueKey);
            String values = JSONUtil.getText(data, valueKey);
            String newValues = JSONUtil.getText(changeData, valueKey);
            if (showAble && !StringUtil.isEmpty(newValues) && !newValues.equals(values)) {
                approval.setOldValues(values);
            } else {
                newValues = values;
            }
            if (!isMain) {
                String findTionName = JSONUtil.getText(config, "DG_FINDFUNCTIONNAME");
                String renderer = JSONUtil.getText(config, "DG_RENDERER");
                approval.setRenderer(renderer);
                if (!StringUtil.isEmpty(findTionName) && findTionName.contains("|")) {
                    int hhitem = findTionName.indexOf('|');
                    String gCaller = findTionName.substring(0, hhitem);
                    String coreKey = findTionName.substring(hhitem + 1, findTionName.length());
                    approval.setgCaller(gCaller);
                    approval.setCoreKey(coreKey);
                }
                if (!StringUtil.isEmpty(renderer) && renderer.contains("formula:")) {
                    try {
                        renderer = renderer.substring("formula:".length(), renderer.length());
                        renderer = getOperator(renderer, data);
                        if (renderer.contains("字段需要设置为app显示")) {
                            newValues = renderer;
                        } else {
                            newValues = renderer;
                            double val = MathUtil.eval(renderer);
                            if (val != Double.NEGATIVE_INFINITY && val != Double.POSITIVE_INFINITY) {
                                newValues = getDecimalFormat(val);
                            }
                        }

                    } catch (Exception e) {
                    }
                }
            }

            if (approval.isDftypeEQ("PF")) {
                Approval enclosure = new Approval(Approval.ENCLOSURE);
                String path = TextUtils.isEmpty(newValues) ? values : newValues;
                enclosure.setIdKey(getImageUrl(path));
                LogUtil.i("gong", "path=" + path + "||\ngetIdKey=" + enclosure.getIdKey());
                String[] splits = path.split("\\.");
                String suffix = "jpg";
                if (splits != null && splits.length > 1) {
                    suffix = splits[splits.length - 1];
                }
                enclosure.setCaption(caption + "." + suffix);
                List<Approval> enclosureList = new ArrayList<>();
                enclosureList.add(enclosure);
                addEnclosure(enclosureList);
                setData2ListThread();
                continue;
            }
            if (approval.isDftypeEQ("FF")
                    || ("detailAttach".equals(approval.getRenderer()))
                    || (record.title.contains("公章用印申请流程") && "附件".equals(caption))) {
                if (isMain) {
                    final String fb_attach = newValues;
                    OAHttpHelper.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            loadFilePaths(fb_attach);
                        }
                    });
                } else {
                    LogUtil.i("newValues=" + newValues);
                    String[] attchs = newValues.split(";");
                    if (attchs != null && attchs.length > 1) {
                        String attchName = attchs[0];
                        String attch = attchs[1];
                        try {
                            Approval enclosure = new Approval(Approval.ENCLOSURE);
                            enclosure.setId(Integer.valueOf(attch));
                            enclosure.setIdKey(getImageUrl(enclosure.getId()));
                            enclosure.setCaption(attchName);
                            List<Approval> enclosureList = new ArrayList<>();
                            enclosureList.add(enclosure);
                            addEnclosure(enclosureList);
                            setData2ListThread();
                        } catch (ClassCastException e) {
                            LogUtil.i("e=" + e.getMessage());
                        }
                    }
                }
                continue;
            }
            //获取Id
            if (!StringUtil.isEmpty(caption) && (caption.equals("ID") || caption.equals("id"))) {
                idTag = valueKey;
                id = JSONUtil.getInt(data, valueKey);
            }
            if (approval.isDftypeEQ("H")
                    || isdefault != -1
                    || appwidth == 0
                    || (!isMain && JSONUtil.getInt(config, "DG_WIDTH") == 0)) {
                continue;
            }
            if (!StringUtil.isEmpty(caption)) {
                if (showAble) {
                    approval.setValues(newValues); //获取第一个字段的值
                    if (addHint) {
                        showApprovals.add(approval);
                    }
                } else if (addHint) {
                    hineApprovals.add(approval);
                }
            }
            if (StringUtil.isEmpty(valueKey) || StringUtil.isEmpty(caption) || (merged.length() > 0 && merged.toString().contains("," + valueKey + ","))) {
                continue;
            }
            //添加下拉数据
            if (!ListUtils.isEmpty(combostore)) {
                for (int j = 0; j < combostore.size(); j++) {
                    JSONObject object = combostore.getJSONObject(j);
                    String value = JSONUtil.getText(object, "DLC_VALUE");
                    String display = JSONUtil.getText(object, "DLC_DISPLAY");
                    if (!StringUtil.isEmpty(value) || !StringUtil.isEmpty(display))
                        approval.getDatas().add(new Approval.Data(display, value));
                }
            }
            boolean mergeAble = appwidth == 1 || (approval.isDftypeEQ("MT"));
            approval.setMustInput(true);
            if (!StringUtil.isEmpty(record.needInputKeys)
                    && ("," + record.needInputKeys + ",").contains("," + valueKey + ",")) {
                approval.setNeerInput(true);
                if (approval.getDatas().size() <= 0) {
                    if (approval.isDftypeEQ("YN", "C")) {
                        approval.getDatas().add(new Approval.Data("-1", Approval.VALUES_YES));
                        approval.getDatas().add(new Approval.Data("0", Approval.VALUES_NO));
                    } else if (approval.isDftypeEQ("B")) {
                        approval.getDatas().add(new Approval.Data("1", Approval.VALUES_YES));
                        approval.getDatas().add(new Approval.Data("0", Approval.VALUES_NO));
                    }
                }
            }
            if (!isApprovaling()) {
                approval.setNeerInput(false);
            }
            approval.data2Values();
            if ((!approval.isNeerInput() && StringUtil.isEmpty(approval.getValues())) || !showAble
                    || approval.getValues().equals("null") || !showAble
                    || approval.getValues().equals("(null)")) {
                continue;//如果不是要输入的对象，同时显示值为空，需要隐藏去
            }
            //合并字段
            if (mergeAble && !approval.isDBFind() && !approval.isNeerInput()) {
                String valueTagKey = JSONUtil.getText(config, "FD_LOGICTYPE", "DG_LOGICTYPE");//获取第二个值的key
                if (!StringUtil.isEmpty(valueTagKey)) {
                    String valueTag = JSONUtil.getText(data, valueTagKey);
                    if (!StringUtil.isEmpty(valueTag)) {
                        merged.append("," + valueTagKey + ",");
                        approval.addValues("/" + valueTag);
                    }
                }
            }
            approval.setCaller(caller);
            approvals.add(approval);
        }
        for (Approval approval : approvals) {
            approval.setId(id);
            approval.setIdKey(idTag);
        }
        return approvals;
    }

    private String getOperator(String renderer, JSONObject object) {
        String[] strs = renderer.split("[^a-z^A-Z^_]");
        for (String str : strs) {
            if (!StringUtil.isEmpty(str)) {
                String val = JSONUtil.getText(object, str);
                if (StringUtil.isEmpty(val)) {
                    return str + "字段需要设置为app显示";
                } else {
                    renderer = renderer.replaceAll(str, val);
                }
            }
        }
        return renderer;
    }

    private synchronized void addEnclosure(List<Approval> enclosureList) {
        if (enclosureList != null) {
            if (this.enclosureList.size() <= 0) {
                Approval tag = new Approval(Approval.TAG);
                tag.setCaption("附件");
                enclosureList.add(0, tag);
            }
            this.enclosureList.addAll(enclosureList);
        }
    }

    private List<Approval> handlerHistorySetuptask(JSONObject object) {
        JSONArray nodes = JSONUtil.getJSONArray(object, "nodes");
        List<Approval> nodeApprovals = new ArrayList<>();
        if (!ListUtils.isEmpty(nodes)) {
            List<Approval> setuptasks = new ArrayList<>();
            JSONObject node = null;
            List<Approval> itemSetuptask = null;
            for (int i = nodes.size() - 1; i >= 0; i--) {
                node = nodes.getJSONObject(i);
                itemSetuptask = getSetuptask(node);
                if (!ListUtils.isEmpty(itemSetuptask)) {
                    setuptasks.addAll(itemSetuptask);
                }
                nodeApprovals.add(getNodeApproval(node));
            }
            if (!ListUtils.isEmpty(setuptasks)) {
                ApprovaPresenter.this.setuptasList = setuptasks;
                setData2ListThread();
            }
        }
        return nodeApprovals;
    }

    private List<Approval> getSetuptask(JSONObject node) {
        String name = JSONUtil.getText(node, "jn_dealManName");
        Approval approval = new Approval(Approval.TAG);
        approval.setCaption(name + "的审批记录");
        List<Approval> itemSetuptasks = getSetuptaskByData(JSONUtil.getText(node, "jn_operatedDescription"));
        if (!ListUtils.isEmpty(itemSetuptasks)) {
            itemSetuptasks.add(0, approval);
        }
        return itemSetuptasks;
    }

    //    //1.forknode==0 (非并行节点)    2.jn_dealResult == '同意'   3.jn_attach == 'T'
    private Approval getNodeApproval(JSONObject object) {
        String nodeName = JSONUtil.getText(object, "jn_name");//当前结点名称
        String emCode = JSONUtil.getText(object, "jn_dealManId");//节点处理人编号
        String manName = JSONUtil.getText(object, "jn_dealManName");//处理人名字
        String dealTime = JSONUtil.getText(object, "jn_dealTime");//审批时间
        String result = JSONUtil.getText(object, "jn_dealResult");//审批结果
        String attach = JSONUtil.getText(object, "jn_attach");//选择类型
        String description = JSONUtil.getText(object, "jn_nodeDescription");//审批意见
        Approval approval = new Approval(Approval.NODES);
        approval.setNeerInput("T".equals(attach));
        approval.setMustInput(result.equals("同意"));
        result = setNodeStatus(result);
        approval.setCaption(manName);
        approval.setDfType(emCode);
        if (!StringUtil.isEmpty(dealTime)) {
            approval.setValues(DateFormatUtil.long2Str(TimeUtils.f_str_2_long(dealTime), "MM-dd HH:mm"));
        }
        StringBuilder resultBuilder = new StringBuilder();
        if (!StringUtil.isEmpty(result)) {
            resultBuilder.append(result);
        }
        if (!StringUtil.isEmpty(description)) {
            resultBuilder.append("(" + description.replace("\\n", "\n") + ")");
        }
        approval.setIdKey(resultBuilder.toString());
        approval.setValuesKey(nodeName);
        if (!StringUtil.isEmpty(emCode)) {
            int imId = getImByCode(emCode);
            approval.setId(imId);
        }
        return approval;
    }

    private DecimalFormat column2DF;

    private String getDecimalFormat(double val) throws Exception {
        if (column2DF == null) {
            column2DF = new DecimalFormat("######0.00");
        }
        return column2DF.format(val);
    }

    /**
     * itemData.caption //处理人名称
     * itemData.type //处理人编号
     * itemData.valuesKey //节点名称
     * temData.status//状态
     * temData.values//时间点
     * <p>
     * 处理审批节点
     * 1.先处理data数据获取数据列表
     * 2.获取node，获取审批意见和时间
     * 3.获取processs，获取审批状态
     */
    private void handlerNode(final JSONObject object, final Bundle bundle) {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                boolean showNode = true;
                JSONArray nodes = JSONUtil.getJSONArray(object, "nodes");
                JSONArray processs = JSONUtil.getJSONArray(object, "processs");
                JSONArray datas = JSONUtil.getJSONArray(object, "data");
                List<Approval> approvals = getNodDatas(datas);
                if (bundle != null && !StringUtil.isEmpty(bundle.getString("historyNode"))) {
                    historyNodes = handlerHistorySetuptask(JSON.parseObject(bundle.getString("historyNode")));
                }
                if ((isApprovaled() || ListUtils.isEmpty(approvals)) && !ListUtils.isEmpty(historyNodes)) {
                    showNode = false;
                    approvals = historyNodes;
                }
                if (showNode && !ListUtils.isEmpty(processs)) {
                    mergeNode(processs, approvals, false);
                }
                if (showNode && !ListUtils.isEmpty(nodes)) {
                    mergeNode(nodes, approvals, true);
                }
                boolean hanNotApproval = false;
                /**
                 * 已审批
                 * 未通过
                 * 已结束
                 * 待审批
                 */
                for (int i = 0; i < approvals.size(); i++) {
                    Approval a = approvals.get(i);
                    if (!a.getIdKey().startsWith("已审批")
                            && !a.getIdKey().startsWith("未通过")
                            && !a.getIdKey().startsWith("不同意")
                            && !a.getIdKey().startsWith("已结束")) {
                        hanNotApproval = true;
                        if (a.getIdKey().startsWith("待审批")) {
                            a.setValues("");
                        }
                    } else if (a.getIdKey().startsWith("未通过") && i == 0) {
                        record.status = "未通过";
                    }
                    String emcode = null;
                    if (a.getDfType().contains(",")) {
                        String[] emcodes = a.getDfType().split(",");
                        if (!StringUtil.isEmpty(emcodes[0])) {
                            emcode = emcodes[0];
                        }
                    } else {
                        emcode = a.getDfType();
                    }
                    if (!StringUtil.isEmpty(emcode)) {
                        int imId = getImByCode(emcode);
                        a.setId(imId);
                    }
                }
                int reId = -1;
                if (showNode) {
                    if ("未通过".equals(record.status) || "已结束".equals(record.status) || "已审批".equals(record.status)) {
                        showNode = false;
                        approvals = historyNodes;
                    }
                }
                if ("未通过".equals(record.status)) {
                    reId = R.drawable.unapproved;
                } else if (!hanNotApproval && !isApprovaling()) {//没有未审批的数据
                    reId = R.drawable.approved;
                }
                handerTitle(reId);
                if (!ListUtils.isEmpty(approvals) && !ListUtils.isEmpty(historyNodes) && showNode) {
                    Approval nodeTag = new Approval(Approval.NODES_TAG);
                    approvals.add(0, nodeTag);
                }
                ApprovaPresenter.this.nodeList = approvals;
                setData2ListThread();
                loading = false;
            }
        });
    }

    private void handerTitle(int reId) {
        Approval approval = new Approval(Approval.TITLE);
        if (!StringUtil.isEmpty(record.title)) {
            approval.setCaption(record.title);
        }
        if (!StringUtil.isEmpty(record.imid)) {
            approval.setIdKey(record.imid);
        }
        if (reId > 0) {
            approval.setId(reId);
        }
        titleApproval = approval;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            ThreadUtil.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    setData2ListThread();
                }
            });
        } else {
            setData2ListThread();
        }
    }

    private void handlerEnclosure(final JSONArray array) throws Exception {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                List<Approval> enclosureList = new ArrayList<>();
                if (!ListUtils.isEmpty(array)) {
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        if (o == null) continue;
                        Approval enclosure = new Approval(Approval.ENCLOSURE);
                        int id = JSONUtil.getInt(o, "fp_id");
                        enclosure.setId(id);
                        enclosure.setIdKey(getImageUrl(id));
                        enclosure.setCaption(JSONUtil.getText(o, "fp_name"));
                        enclosureList.add(enclosure);
                    }
                }
                if (!ListUtils.isEmpty(enclosureList)) {
                    addEnclosure(enclosureList);
                    setData2ListThread();
                }
            }
        });
    }

    private boolean equalsOne(String text, String... str) {
        if (StringUtil.isEmpty(text) || str == null || str.length <= 0) return false;
        for (String s : str) {
            if (text.equals(s)) return true;
        }
        return false;
    }

    /**
     * 已审批
     * 未通过
     * 已结束
     * 待审批
     */
    private String setNodeStatus(String status) {
        if (equalsOne(status, "同意")) {
            status = "已审批";
        } else if (equalsOne(status, "不同意", "不通过")) {
            status = "未通过";
        } else if (equalsOne(status, "结束流程")) {
            status = "已结束";
        }
        return status;
    }

    private void mergeNode(JSONArray array, List<Approval> approvals, boolean isLog) {
        String myCode = CommonUtil.getEmcode();
        for (int i = 0; i < array.size(); i++) {
            JSONObject o = array.getJSONObject(i);
            String name = JSONUtil.getText(o, "jp_nodeName", "jn_name");//节点名称
            long launchTime = JSONUtil.getTime(o, "jp_launchTime", "jn_dealTime");//时间
            String status = JSONUtil.getText(o, "jp_status", "jn_dealResult");//状态
            String nodeDealCode = JSONUtil.getText(o, "jp_nodeDealMan", "jn_dealManId");//处理人编号
            String nodeDealName = JSONUtil.getText(o, "jp_nodeDealManName", "jn_dealManName");//执行人
            String nodeDescription = JSONUtil.getText(o, "jn_nodeDescription");//执行操作
            boolean hanEnd = false;
            setNodeStatus(status);
            for (int j = approvals.size() - 1; j >= 0; j--) {
                Approval a = approvals.get(j);
                if (hanEnd && !equalsOne(status, "待审批")) {
                    continue;
                }
                //当前节点和该节点一致
                if (a.getValuesKey().equals(record.nodeName)) {
                    hanEnd = true;
                }
                LogUtil.i("gong", "name=" + name);
                LogUtil.i("gong", "a.getValuesKey()=" + a.getValuesKey());
                if (name.equals(a.getValuesKey())) {//为当前结点
                    if (!isLog) {
                        a.setDfType(nodeDealCode);
                        a.setCaption(nodeDealName);
                        if (status.equals("待审批")) {
                            LogUtil.i("gong", "status=" + status);
                            if (myCode.equals(nodeDealCode)) {
                                a.setIdKey(status);
                            } else {
                                a.setIdKey("");
                            }
                        } else {
                            a.setIdKey(status);
                        }
                    } else {
                        if (launchTime > 0) {
                            a.setValues(DateFormatUtil.long2Str(launchTime, "MM-dd"));
                            a.setDbFind(DateFormatUtil.long2Str(launchTime, "HH:mm"));
                        }
                        if (a.isDftypeEQ(nodeDealCode)) {
                            if (status.equals("不同意")) {
                                a.setIdKey("未通过");
                            } else if (status.equals("同意")) {
                                a.setIdKey("已审批");
                            }
                            if (!hanEnd && !StringUtil.isEmpty(nodeDescription)) {
                                a.setIdKey(a.getIdKey() + "(" + nodeDescription + ")");
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 从datas中获取节点数（节点处理人、系欸但处理人编号、节点名称）
     *
     * @param datas
     * @return
     */
    private List<Approval> getNodDatas(JSONArray datas) {
        List<Approval> approvals = new ArrayList<>();
        for (int i = datas.size() - 1; i >= 0; i--) {
            JSONObject object = datas.getJSONObject(i);
            String nodeName = JSONUtil.getText(object, "JP_NODENAME");//当前结点名称
            String emCode = JSONUtil.getText(object, "JP_NODEDEALMAN");//节点处理人编号
            String manName = JSONUtil.getText(object, "JP_NODEDEALMANNAME");
            Approval approval = new Approval(Approval.NODES);
            approval.setCaption(StringUtil.isEmpty(manName) ? nodeName : manName);
            approval.setDfType(emCode);
            approval.setValuesKey(nodeName);
            approvals.add(approval);
        }
        return approvals;
    }


    private List<Approval> getSetuptaskByData(String data) {
        if (!StringUtil.isEmpty(data)) {
            //需要把该数据填充到对应上面
            String[] datas = data.split(";");
            List<Approval> itemSetuptasks = new ArrayList<>();
            Approval approval = null;
            if (datas != null && datas.length > 0) {
                for (int j = 0; j < datas.length; j++) {
                    String description = datas[j];
                    if (description.contains("(")) {
                        String caption = description.substring(0, description.indexOf("("));
                        String values = StringUtil.getFirstBrackets(description);
                        if (!StringUtil.isEmpty(caption) && !StringUtil.isEmpty(values)) {
                            approval = new Approval(Approval.SETUPTASK);
                            approval.setNeerInput(false);
                            approval.setCaption(caption);
                            approval.setValues(values);
                            itemSetuptasks.add(approval);
                        }
                    }
                }
            }
            return itemSetuptasks;
        }
        return null;
    }

    private void handlerSetupTask(final JSONObject object) throws Exception {
        ThreadUtil.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                final int isApprove = JSONUtil.getInt(object, "isApprove");
                JSONArray arrayCS = JSONUtil.getJSONArray(object, "cs");
                if (!ListUtils.isEmpty(arrayCS)) {
                    String data = JSONUtil.getText(object, "data");
                    String[] datas = null;
                    if (!StringUtil.isEmpty(data)) {
                        //需要把该数据填充到对应上面
                        datas = data.split(";");
                    }
                    List<Approval> pointsList = new ArrayList<>();
                    for (int i = 0; i < arrayCS.size(); i++) {
                        Approval itemData = getItemBySetupTask(arrayCS.getString(i), datas);
                        if (itemData != null) {
                            pointsList.add(itemData);
                        }
                    }
                    if (!ListUtils.isEmpty(pointsList)) {
                        Approval points = new Approval(Approval.TAG);
                        points.setCaption(StringUtil.getMessage(R.string.approval_points));
                        pointsList.add(0, points);
                        ApprovaPresenter.this.pointsList = pointsList;
                        setData2ListThread();
                    }
                }
                OAHttpHelper.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (isApprove == 1) {
                            iApproval.isApprove(true);
                        }
                        loadAllHistoryNodes();
                    }
                });
            }
        });

    }

    /**
     * arrayCS:
     * ..^S代表该字段为字符串类型，
     * ..^N为数据型，
     * ..^D为日期型
     * <p>
     * 注：
     * ..$N为非必填字段，
     * ..$Y表示该字段为必填字段，
     */
    private Approval getItemBySetupTask(String cs, String[] datas) {
        if (!StringUtil.isEmpty(cs)) {
            Approval approval = new Approval(Approval.POINTS);
            String[] css = cs.split("\\^");
            if (!StringUtil.isEmpty(css[0])) {
                approval.setCaption(css[0]);
                String tag = css[1];
                if (!StringUtil.isEmpty(tag)) {
                    String[] tags = tag.split("\\$");
                    approval.setDfType(tags[0]);
                    String neer = tags[1];
                    String data = StringUtil.getLastBracket(neer);
                    if (StringUtil.isEmpty(data)) {
                        data = "是;否";
                    }
                    String[] combostore = data.split(";");
                    if (combostore != null && combostore.length > 0) {
                        for (String v : combostore) {
                            if (v != null && v.length() > 0)
                                approval.getDatas().add(new Approval.Data(v, v));
                        }
                    }
                    if (!StringUtil.isEmpty(neer)) {
                        approval.setMustInput(neer.startsWith("Y"));
                        if (neer.contains("@A")) {
                            approval.setDfType("@A");
                        } else if (neer.contains("@")) {
                            String[] rets = neer.split("@");
                            if (rets != null && rets.length > 1) {
                                approval.setDfType("@" + rets[1]);
                            }
                        }
                    }
                }
                if (datas != null && datas.length > 0) {
                    for (String data : datas) {
                        if (data.startsWith(approval.getCaption())) {
                            String values = StringUtil.getFirstBrackets(data);
                            if (!StringUtil.isEmpty(values)
                                    && !values.equals("null")
                                    && !values.equals("(null)")
                                    && !values.equals("(null")) {
                                approval.setValues(values);
                            }
                        }
                    }
                }
                approval.setNeerInput(isApprovaling());
                if (!approval.isNeerInput() && StringUtil.isEmpty(approval.getValues())) {
                    return null;
                }
                return approval;
            }
        }
        return null;
    }

    private int getImByCode(String emCode) {
        DBManager manager = DBManager.getInstance();
        String whichsys = CommonUtil.getMaster();
        if (StringUtil.isEmpty(emCode) || StringUtil.isEmpty(whichsys)) return 0;
        String[] selectionArgs = {emCode, whichsys};
        String selection = "em_code=? and whichsys=?";
        EmployeesEntity employeesEntity = manager.selectForEmployee(selectionArgs, selection);
        if (employeesEntity == null || employeesEntity.getEm_IMID() == 0) return 0;
        return employeesEntity.getEm_IMID();
    }

    private String getImageUrl(String path) {
        return CommonUtil.getAppBaseUrl(MyApplication.getInstance()) + "common/download.action?path=" + path + "&sessionId=" +
                CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username") +
                "&master=" + master;
    }

    private String getImageUrl(int id) {
        return CommonUtil.getAppBaseUrl(MyApplication.getInstance()) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username") +
                "&master=" + master;
    }

    private boolean canSubmit() {
        if (submit || loading) {
            iApproval.showToast(R.string.submit_cannot_submit_again, R.color.load_submit);
            return false;
        }
        return true;
    }

    //判断要点
    private boolean inputAllPoints(StringBuilder customDes, List<Approval> approvals) {
        if (!ListUtils.isEmpty(approvals)) {
            for (Approval approval : approvals) {
                if (approval.getType() == Approval.POINTS) {
                    if (approval.isMustInput() && StringUtil.isEmpty(approval.getValues())) {
                        String message = StringUtil.getMessage(R.string.approval_points) + " " + approval.getCaption() + " " + StringUtil.getMessage(R.string.is_must_input);
                        iApproval.showToast(message, R.color.load_submit);
                        return false;
                    }
                    if (!StringUtil.isEmpty(approval.getCaption())
                            && !StringUtil.isEmpty(approval.getValues()) && customDes != null) {
                        if (approval.getValues().contains("@")) {
                            String message = StringUtil.getMessage(R.string.approval_points) + " " + approval.getCaption() + " 带有特殊字符";
                            iApproval.showToast(message, R.color.load_submit);
                            return false;
                        }
                        if (approval.isDftypeEQ("@A")) {
                            customDes.append(approval.getCaption() + "(" + approval.getValues() + ")" + "@A@;");
                        } else if (!StringUtil.isEmpty(approval.getDfType()) && approval.getDfType().contains("@")) {
                            customDes.append(approval.getCaption() + "(" + approval.getValues() + ")" + approval.getDfType() + "@;");
                        } else {
                            customDes.append(approval.getCaption() + "(" + approval.getValues() + ");");
                        }

                    }
                }
            }
            StringUtil.removieLast(customDes);
        }
        return true;
    }

    private boolean inputAllInput(List<Map<String, Object>> params, Map<String, Object> formStore, List<Approval> approvals) {
        if (!ListUtils.isEmpty(approvals)) {
            List<Approval> mainList = new ArrayList();
            List<List<Approval>> detailList = new ArrayList<>();
            List<Approval> detail = new ArrayList<>();
            for (int i = 0; i < approvals.size(); i++) {
                Approval approval = approvals.get(i);
                if (approval.getType() == Approval.MAIN) {
                    mainList.add(approval);
                } else if (approval.getType() == DETAIL) {
                    detail.add(approval);
                    if (approvals.size() > i + 1 && approvals.get(i + 1).getType() != approval.getType()) {
                        detailList.add(detail);
                        detail = new ArrayList<>();
                    }
                }
            }
            Map<String, Object> formstore = putItem2Params(true, mainList);
            if (formstore == null) {
                return false;
            }
            formStore.putAll(formstore);

            for (List<Approval> details : detailList) {
                Map<String, Object> param = putItem2Params(true, details);
                if (param == null) {
//                    iApproval.showToast("明细行有必填字段未填写");
                    //将明细表必填项添加出来
                    return false;
                } else if (!param.isEmpty() && param.size() > 1) {
                    params.add(param);
                }
            }
        }
        return true;
    }

    /**
     * @param showTocat
     * @param approvals
     * @return null:表示不通过
     */
    private Map<String, Object> putItem2Params(boolean showTocat, List<Approval> approvals) {
        Map<String, Object> formstore = new HashMap<>();
        for (Approval approval : approvals) {
            if (approval.isNeerInput()) {
                if (StringUtil.isEmpty(approval.getValues())) {
                    String message = StringUtil.getMessage(R.string.must_input_key) + " " + approval.getCaption() + " " + StringUtil.getMessage(R.string.is_must_input);
                    if (showTocat) {
                        iApproval.showToast(message);
                    }
                    return null;
                } else {
                    if (approval.getValues().equals(Approval.VALUES_UNKNOWN)) {
                        formstore.put(approval.getValuesKey(), "1");//添加特殊字符判断
                    } else {
                        boolean isputed = false;
                        for (Approval.Data data : approval.getDatas()) {
                            isputed = false;
                            if (data.display.equals(approval.getValues())) {
                                formstore.put(approval.getValuesKey(),
                                        StringUtil.isEmpty(data.value) ? approval.getValues() : data.value);
                                isputed = true;
                                break;
                            }
                        }
                        if (!isputed) {
                            formstore.put(approval.getValuesKey(), approval.getValues());
                        }
                    }
                }
            }
            if (!StringUtil.isEmpty(approval.getIdKey()) && approval.getId() > 0) {
                formstore.put(approval.getIdKey(), approval.getId());
            }
        }
        return formstore;
    }


    private synchronized void setData2ListThread() {
        final List<Approval> approvals = new ArrayList<>();
        approvals.add(titleApproval);
        approvals.addAll(mainDetailList);
        approvals.addAll(setuptasList);
        approvals.addAll(enclosureList);
        approvals.addAll(pointsList);
        approvals.addAll(nodeList);
        OAHttpHelper.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (!loading && ApprovaPresenter.this.titleApproval != null && !StringUtil.isEmpty(titleApproval.getCaption()) && isApprovaling()) {
                    iApproval.showOpinion();
                }
                iApproval.showModels(approvals, historyNodes);
            }
        });
    }

}
