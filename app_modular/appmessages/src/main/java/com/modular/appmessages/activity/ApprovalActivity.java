package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.view.AndroidBug5497Workaround2;
import com.core.model.Approval;
import com.core.model.OAConfig;
import com.core.model.SelectBean;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.StatusBarUtil;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.Activity.SelectActivity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.ApprovalAdapter;
import com.modular.appmessages.adapter.CrashLinearLayoutManager;
import com.modular.appmessages.presenter.ApprovaPresenter;
import com.modular.appmessages.presenter.imp.IApproval;
import com.modular.apputils.activity.SelectNetAcitivty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ApprovalActivity extends BaseActivity implements IApproval, ApprovalAdapter.OnChangeClickListener, View.OnClickListener, RecognizerDialogListener {
    RelativeLayout opinionRL;//操作和意见输入
    FormEditText opinionET;//意见
    RecyclerView contentRV;
    ImageView inputTagIV;
    LinearLayout operationLL;

    private ApprovaPresenter mPresenter;
    private ApprovalAdapter mAdapter;
    private boolean isApprove;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.oa_approval_set && mAdapter != null && !ListUtils.isEmpty(mAdapter.getApprovals())) {
            mPresenter.trun2SetActivity(this);
        } else if (item.getItemId() == R.id.returnOld) {
            Intent intent = new Intent(ct, AppWebViewActivity.class);
            String title = getIntent().getStringExtra("title");
            String url = mPresenter.getUrl(findViewById(R.id.takeOverTV).getVisibility() == View.VISIBLE ? "jsps/mobile/jprocand.jsp?nodeId=" : "", title);
            intent.putExtra("url", url);
            intent.putExtra("p", title);
            intent.putExtra("approval", true);
            intent.putExtra("master", mPresenter.getMaster());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aproval_set, menu);
        MenuItem item = menu.getItem(1);
        item.setTitle("返回旧版");
        boolean hiteAble = !PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false);
        if (hiteAble) {
            MenuItem setItem = menu.getItem(0);
            setItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.closeDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == 0x20) {
            SelectEmUser user = data.getParcelableExtra("data");
            if (user != null && !StringUtil.isEmpty(user.getEmCode())) {
                mPresenter.updateAssignee(user.getEmCode(), StringUtil.getTextRexHttp(opinionET));
            }
        } else if (0x22 == requestCode || 0x21 == requestCode) {
            String name = null;
            if (data != null) {
                SelectBean d = data.getParcelableExtra("data");
                name = d.getName();
            }
            if (0x21 == requestCode) {
                if (!StringUtil.isEmpty(name)) {
                    getEmnameByReturn(name);
                } else {
                    mPresenter.loadNextProcess();
                }
            } else if (0x22 == requestCode) {
                if (!StringUtil.isEmpty(name)) {
                    if (name.equals("回退给制单人")) {
                        name = "RECORDER";
                    }
                    mPresenter.disAgree(StringUtil.getTextRexHttp(opinionET), name);
                }
            }
        } else if (0x25 == requestCode && 0x25 == resultCode) {
            mPresenter.initLoad();
        } else if (90 == requestCode && 90 == resultCode) {
            String json = data.getStringExtra("data");
            try {
                updateDbFindKey(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateDbFindKey(String message) throws Exception {
        if (!StringUtil.isEmpty(message) && JSONUtil.validateJSONObject(message)) {
            JSONObject object = JSON.parseObject(message);
            if (mAdapter != null) {
                List<Approval> models = mAdapter.getDbFind();
                for (Approval e : models) {
                    if (e.isNeerInput() && (e.getType() == Approval.MAIN || e.getType() == Approval.DETAIL)) {
                        for (Map.Entry<String, Object> map : object.entrySet()) {
                            if (map.getKey().equals(e.getValuesKey()) && map.getValue() != null) {
                                e.setValues(map.getValue().toString());
                            }
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);
////        AndroidBug5497Workaround2.assistActivity(this);
        initView();
    }

    private void initView() {
        opinionRL = (RelativeLayout) findViewById(R.id.opinionRL);
        operationLL = findViewById(R.id.operationLL);
        opinionET = (FormEditText) findViewById(R.id.opinionET);
        contentRV = (RecyclerView) findViewById(R.id.contentRV);
        inputTagIV = (ImageView) findViewById(R.id.inputTagIV);
        contentRV.setLayoutManager(new CrashLinearLayoutManager(ct));
        findViewById(R.id.commonWordsIV).setOnClickListener(this);//常用语
        findViewById(R.id.nextTV).setOnClickListener(this);
        findViewById(R.id.voiceIV).setOnClickListener(this);
        findViewById(R.id.changedealmanTV).setOnClickListener(this);
        findViewById(R.id.disagreeTV).setOnClickListener(this);
        findViewById(R.id.agreeTV).setOnClickListener(this);
        findViewById(R.id.takeOverTV).setOnClickListener(this);
        String title = getIntent().getStringExtra("title");
        opinionET.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && inputTagIV.getVisibility() == View.VISIBLE) {
                    inputTagIV.setVisibility(View.GONE);
                } else if (s.length() <= 0 && inputTagIV.getVisibility() == View.GONE) {
                    inputTagIV.setVisibility(View.VISIBLE);
                }
            }
        });
        if (!StringUtil.isEmpty(title)) {
            setTitle(title);
            if (getString(R.string.title_approval).equals(title)) {
                opinionRL.setVisibility(View.VISIBLE);
                operationLL.setVisibility(View.VISIBLE);
                if (isApprove) {
                    findViewById(R.id.disagreeTV).setVisibility(View.GONE);
                }
            } else {
                opinionRL.setVisibility(View.GONE);
                operationLL.setVisibility(View.GONE);
            }
        }
        mPresenter = new ApprovaPresenter(this, getIntent());
        final View mRootLL = findViewById(R.id.mRootLL);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mRootLL.post(new Runnable() {
                @Override
                public void run() {
                    int top = StatusBarUtil.getStatusBarHeight(ct);
                    mRootLL.invalidate(0, 0, mRootLL.getMeasuredWidth(), mRootLL.getMeasuredHeight() +top);
                    mRootLL.setTranslationY(-top);
                }
            });
        }
    }

    public void toDbFind(Approval approval) {
        startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                        .putExtra("fieldKey", approval.getValuesKey())
                        .putExtra("corekey", approval.getCoreKey())
                        .putExtra("caller", approval.getCaller())
                        .putExtra("gCaller", approval.getgCaller())
                        .putExtra("isForm", approval.getType() == Approval.MAIN)
                , 90);

    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) return;
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                mPresenter.loadTakeOver2Other(name);
                return;
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.agreeTV) {
            List<Approval> approvals = null;
            if (mAdapter != null) {
                approvals = mAdapter.getApprovals();
            }
            mPresenter.loadProcessUpdate(StringUtil.getTextRexHttp(opinionET), approvals);
        } else if (R.id.disagreeTV == id) {
            List<String> nodes = mPresenter.getNodesCanReturn();
            if (StringUtil.isEmpty(StringUtil.getTextRexHttp(opinionET))) {
                opinionET.setFocusable(true);
                showToast(R.string.approval_opinion_error, R.color.load_submit);
            } else {
                if (ListUtils.isEmpty(nodes)) {
                    mPresenter.disAgree(StringUtil.getTextRexHttp(opinionET), null);
                } else {
                    sendToSelect(nodes, 0x22, "选择回退节点");
                }
            }
        } else if (R.id.nextTV == id) {
            hineOpinion();
            mPresenter.loadNextProcess();
        } else if (R.id.voiceIV == id) {
            RecognizerDialogUtil.showRecognizerDialog(ct, this);
        } else if (R.id.commonWordsIV == id) {
            showCommonWordsIV();
        } else if (R.id.takeOverTV == id) {
            mPresenter.loadTakeOver();
        }
    }

    private void selectEmCode() {
        if (StringUtil.isEmpty(StringUtil.getTextRexHttp(opinionET))) {
            showToast(R.string.approval_opinion_error, R.color.load_submit);
            return;
        }
        Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
        SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                .setSureText(getString(R.string.common_sure))
                .setSelectType(getString(R.string.member))
                .setTitle(getString(R.string.select_user))
                .setSingleAble(true)
                .setReBackSelect(false);
        intent.putExtra(OAConfig.MODEL_DATA, bean);
        startActivityForResult(intent, 0x20);
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        if (!StringUtil.isEmpty(text)) {
            opinionET.setText(opinionET.getText() + text);
        }
    }

    @Override
    public void onError(SpeechError speechError) {

    }


    @Override
    public void nodeDealMan(String nodeDealMan) {
        LogUtil.i("gong", "nodeDealMan=" + nodeDealMan);
        if (StringUtil.isEmpty(nodeDealMan)) {
            findViewById(R.id.changedealmanTV).setVisibility(View.GONE);
            findViewById(R.id.disagreeTV).setVisibility(View.GONE);
            findViewById(R.id.agreeTV).setVisibility(View.GONE);
            findViewById(R.id.takeOverTV).setVisibility(View.VISIBLE);
        } else {
            //TODO 先不判断当前处理人是不是自己
//            String emcode = CommonUtil.getEmcode();
//            if (nodeDealMan.contains(emcode)) {
            findViewById(R.id.changedealmanTV).setVisibility(View.GONE);
            findViewById(R.id.agreeTV).setVisibility(View.VISIBLE);
            findViewById(R.id.takeOverTV).setVisibility(View.GONE);
            if (!isApprove) {
                findViewById(R.id.disagreeTV).setVisibility(View.VISIBLE);
            }
//            } else {
//                opinionRL.setVisibility(View.GONE);
//            }
        }
    }


    @Override
    public void showModels(List<Approval> approvals, List<Approval> historyNodes) {
        LogUtil.i("gong", "showModels=" + approvals.size());
        mAdapter = new ApprovalAdapter(this, approvals, historyNodes, mPresenter.isApprovaling());
        contentRV.setHasFixedSize(false);
        contentRV.setItemAnimator(new DefaultItemAnimator());
        contentRV.setAdapter(mAdapter);
        mAdapter.setOnChangeClickListener(this);
    }

    @Override
    public void initStatus() {
        findViewById(R.id.changedealmanTV).setVisibility(View.GONE);
        findViewById(R.id.disagreeTV).setVisibility(View.GONE);
        findViewById(R.id.agreeTV).setVisibility(View.GONE);
        findViewById(R.id.takeOverTV).setVisibility(View.GONE);
        opinionRL.setVisibility(View.GONE);
        opinionET.setText("");
        isApprove = false;
        findViewById(R.id.disagreeTV).setVisibility(View.VISIBLE);
        if (mAdapter != null) {
            mAdapter.setApprovals(null);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void endProcess() {
        new MaterialDialog.Builder(ct).title(MyApplication.getInstance().getString(R.string.app_dialog_title))
                .content(R.string.end_approval)
                .positiveText(MyApplication.getInstance().getString(R.string.common_sure)).autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
//                        finish();
                        dialog.dismiss();
                        mPresenter.loadNextProcess();
                    }
                })
//                .positiveText(R.string.load_tonext_approval)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        mPresenter.loadNextProcess();
//                    }
//                })
                .show();
    }

    @Override
    public void showOpinion() {
        if (mPresenter.isApprovaling()) {
            opinionRL.setVisibility(View.VISIBLE);
            if (isApprove) {
                findViewById(R.id.disagreeTV).setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void isApprove(boolean isApprove) {
        this.isApprove = isApprove;
        findViewById(R.id.disagreeTV).setVisibility(View.GONE);
    }

    public void hineOpinion() {
        if (mPresenter.isApprovaling()) {
            opinionRL.setVisibility(View.GONE);
        }
    }


    @Override
    public void sendToSelect(JSONArray data) {
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            nodes.add(data.getString(i));
        }
        sendToSelect(nodes, 0x21, "选择审批人");
    }

    public void sendToSelect(List<String> seletcNodes, int requestCode, String title) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(seletcNodes);
        for (String s : set) {
            bean = new SelectBean();
            bean.setName(s);
            bean.setClick(false);
            beans.add(bean);
        }
        if (requestCode == 0x22) {
            bean = new SelectBean();
            bean.setName("回退给制单人");
            beans.add(0, bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", title);
        startActivityForResult(intent, requestCode);
    }


    @Override
    public void click() {
        selectEmCode();
    }

    private void showCommonWordsIV() {
        final PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(R.layout.item_list_pop, null);
        window.setContentView(view);
        ListView contentLV = view.findViewById(R.id.contentLV);
        final String[] messages = {"赞", "OK", "加油", "好的", "请及时完成"};
        contentLV.setAdapter(new PopListAdapter(messages));
        window.setTouchable(true);
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.pop_round_bg));
        window.getContentView().measure(0, 0);
        window.setHeight(DisplayUtil.dip2px(this, 230));
        window.setWidth(DisplayUtil.dip2px(this, 100));
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        //获取需要在其上方显示的控件的位置信息
        View v = findViewById(R.id.commonWordsIV);
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        //在控件上方显示
        window.showAtLocation(v, Gravity.NO_GRAVITY, (location[0] + v.getWidth() / 2) - window.getWidth() / 2, location[1] - window.getHeight());
        contentLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = messages[position];
                opinionET.setText(StringUtil.getTextRexHttp(opinionET) + message);
                window.dismiss();
            }
        });
    }


    private class PopListAdapter extends BaseAdapter {
        String[] messages = null;

        public PopListAdapter(String[] messages) {
            this.messages = messages;
        }

        @Override
        public int getCount() {
            return messages == null ? 0 : messages.length;
        }

        @Override
        public Object getItem(int position) {
            return messages[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_pop_list, null);
                holder = new ViewHolder();
                holder.tv_text = (TextView) convertView.findViewById(R.id.tv_item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_text.setText(messages[position]);
            return convertView;
        }

        class ViewHolder {
            TextView tv_text;
        }
    }


    @Override
    public void showLoading() {
        try {
            progressDialog.show();
        } catch (Exception e) {

        }
    }

    @Override
    public void dimssLoading() {
        try {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {

        }
    }

    @Override
    public void showToast(String message, int colorId) {
        super.showToast(message);
    }

    @Override
    public void showToast(String message) {
        LogUtil.i("gong", "message=" + message);
        super.showToast(message);
    }

    @Override
    public void showToast(int reId, int colorId) {
        super.showToast(reId);
    }


    @Override
    public void setTitleStyles(int styles) {

    }
}
