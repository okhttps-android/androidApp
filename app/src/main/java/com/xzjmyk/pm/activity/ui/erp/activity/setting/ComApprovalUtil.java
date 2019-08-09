package com.xzjmyk.pm.activity.ui.erp.activity.setting;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.uas.appworks.OA.erp.model.CommonApprovalFlowBean;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGLH on 2017/7/7.
 * 审批流数据公用方法，获取审批人名字及头像
 */

public class ComApprovalUtil  {

    /**
     * 获取审批流列表审批人名字
     * @param ct
     * @param mCommonApprovalFlowBean
     * @param real_status
     * @param type type=1 拿名字，type = 2 ，拿imids
     * @return
     */
    public static List<String> getAfMsg(Context ct, CommonApprovalFlowBean mCommonApprovalFlowBean, String real_status, int type) {
        List<String> afpeople_names = new ArrayList<>();
        List<String> im_ids = new ArrayList<>();
        List<String> msg = new ArrayList<>();
        String em_code = "";
        String af_name = "";
        DBManager manager =  new DBManager();
        if ("已审核".equals(real_status) && (mCommonApprovalFlowBean.getData().size() == 0 ||
                mCommonApprovalFlowBean.getData().size() == mCommonApprovalFlowBean.getNodes().size())) {  // TODO 已审核直接从node里面取数据
            for (int i = 0; i < mCommonApprovalFlowBean.getNodes().size(); i++) {
                //取名字
                if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManName())) {
                    afpeople_names.add(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManName());
                } else {
                    afpeople_names.add("");
                }
                //取头像id
                if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManId())) {
                    em_code = mCommonApprovalFlowBean.getNodes().get(i).getJn_dealManId();
                    if (em_code.contains(",")) {
                        String str[] = em_code.split(",");
                        em_code = str[0];
//                                ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                    }
                } else {
                    em_code = " ";
                }
                try {
                    String whichsys = CommonUtil.getSharedPreferences(ct, "erp_master");
                    String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                    String selection = "em_code=? and whichsys=? ";
                    //获取数据库数据
                    EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                    if (bean != null) {
                        String imId = String.valueOf(bean.getEm_IMID());
                        Log.i("todo", "imId=" + imId);
                        im_ids.add(imId);
                    } else {
                        im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else if ("已提交".equals(real_status)) {
            String whichsys = CommonUtil.getSharedPreferences(ct, "erp_master");
            if (!ListUtils.isEmpty(afpeople_names))
                afpeople_names.clear();
            if (!ListUtils.isEmpty(im_ids))
                im_ids.clear();
            // TODO 已提交状态判断是否有变更处理人，所以得先去process中判断,好麻烦噢
            if (!ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {
                int processnum = mCommonApprovalFlowBean.getProcesss().size();
                //取process数据
                for (int i = 0; i < processnum; i++) {
                    if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealMan())) {
                        em_code = mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealMan();
                        if (em_code.contains(",")) {
                            String str[] = em_code.split(",");
                            em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                        }
                        String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                        String selection = "em_code=? and whichsys=? ";
                        try {
                            EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                            if (bean != null) {
                                //获取数据库数据获得名字
                                String imName = String.valueOf(bean.getEM_NAME());
                                if (!StringUtil.isEmpty(imName)) {
                                    af_name = imName;
                                }
                                //从数据库数据获得imid
                                String imId = String.valueOf(bean.getEm_IMID());
                                Log.i("todo", "imId=" + imId);
                                im_ids.add(imId);
                            } else {
                                im_ids.add("");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        im_ids.add("");
                    }

                    //获取process审批人姓名
                    if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealManName())) {
                        afpeople_names.add(mCommonApprovalFlowBean.getProcesss().get(i).getJp_nodeDealManName());
                    } else if (!TextUtils.isEmpty(af_name)) {
                        afpeople_names.add(af_name);
                    } else {
                        afpeople_names.add("");
                    }
                }

                //取data数据
                for (int j = processnum; j < mCommonApprovalFlowBean.getData().size(); j++) {
                    //取process之后的审批人名字
                    if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMANNAME())) {
                        afpeople_names.add(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMANNAME());
                    }else{
                        afpeople_names.add("");
                    }
                    //取process之后的imid
                    if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMAN())) {
                        em_code = mCommonApprovalFlowBean.getData().get(j).getJP_NODEDEALMAN();
                        if (em_code.contains(",")) {
                            String str[] = em_code.split(",");
                            em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                        }
                        String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                        String selection = "em_code=? and whichsys=? ";
                        try {
                            //获取数据库数据
                            EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                            if (bean != null) {
                                String imId = String.valueOf(bean.getEm_IMID());
                                Log.i("todo", "imId=" + imId);
                                im_ids.add(imId);
                            } else {
                                im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        im_ids.add("");
                    }
                }
            }

            // TODO 标准版刚刚提交时无变更时，全部取data数据
            if (ListUtils.isEmpty(mCommonApprovalFlowBean.getProcesss()) && !ListUtils.isEmpty(mCommonApprovalFlowBean.getData())) {
                for (int i = 0; i < mCommonApprovalFlowBean.getData().size(); i++) {
                    //取名字
                    if (manager == null) manager = new DBManager(ct);
                    if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMANNAME())) {
                        afpeople_names.add(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMANNAME());
                    } else {
                        afpeople_names.add("");
                    }
                    //取头像id
                    if (!TextUtils.isEmpty(mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMAN())) {
                        em_code = mCommonApprovalFlowBean.getData().get(i).getJP_NODEDEALMAN();
                        if (em_code.contains(",")) {
                            String str[] = em_code.split(",");
                            em_code = str[0];
//                                    ToastMessage("多人审批，头像已显示为首个");   //该情况只有在测试账号情况下出现
                        }
                    } else {
                        em_code = " ";
                    }
                    try {
                        String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                        String selection = "em_code=? and whichsys=? ";
                        //获取数据库数据
                        EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                        if (bean != null) {
                            String imId = String.valueOf(bean.getEm_IMID());
                            Log.i("todo", "imId=" + imId);
                            im_ids.add(imId);
                        } else {
                            im_ids.add("");
//                                            ToastMessage("审批流头像获取异常，已显示为默认");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        } else if ("已审核".equals(real_status) && mCommonApprovalFlowBean.getData().size() != 0
                && mCommonApprovalFlowBean.getData().size() != mCommonApprovalFlowBean.getNodes().size()) {
        }
        if (type == 1)
            msg =  afpeople_names;
        else if (type == 2)
            msg = im_ids;

        return msg;
    }

    public static String getImid(Context mContext,String em_code){
        DBManager manager = new DBManager(mContext);
        String im_id = "";
        try {
            String whichsys = CommonUtil.getSharedPreferences(mContext, "erp_master");
            String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
            String selection = "em_code=? and whichsys=? ";
            //获取数据库数据
            EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
            if (bean != null) {
                im_id= String.valueOf(bean.getEm_IMID());
                Log.i("todo", "im_id=" + im_id);
            } else {
                im_id = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return im_id;
    }
}
