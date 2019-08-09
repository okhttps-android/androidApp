package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.MyListView;
import com.core.widget.view.model.SelectAimModel;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.modular.apputils.utils.PopupWindowHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.presenter.AddVisitReportPresenter;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.IAddVisitReport;

import java.util.ArrayList;
import java.util.List;

public class AddVisitReportActivity extends OABaseActivity implements IAddVisitReport, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int SELECT_REMARK = 0x55;
    private static final int SELECT_COMPANY = 0x56;
    private static final int SELECT_CONTACT = 0x57;
    @ViewInject(R.id.date_tv)
    private TextView date_tv;
    @ViewInject(R.id.company_tv)
    private TextView company_tv;
    @ViewInject(R.id.company_add_tv)
    private TextView company_add_tv;
    @ViewInject(R.id.remark_tv)
    private TextView remark_tv;
    @ViewInject(R.id.content_et)
    private EditText content_et;
    @ViewInject(R.id.contact_lv)
    private MyListView contact_lv;

    public int position;//点击的联系人索引

    private AddVisitReportPresenter presenter = null;
    private VisitAddContactAdapter adapter;
    private int mpd_id;
    private boolean saveAble = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visiting_report);
        ViewUtils.inject(this);
        setTitle(R.string.visitrecord);
        initData();
        initEvent();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) return;
        int type = intent.getIntExtra("type", -1);
        if (type == -1) {
            presenter = new AddVisitReportPresenter(this);
            date_tv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD));
            initView("");
        } else {
            presenter = new AddVisitReportPresenter(this);
            String message = getIntent().getStringExtra("data");
            JSONObject object = JSON.parseObject(message);
            String custname = JSONUtil.getText(object, "custname", "vr_cuname", "mpd_company");//客户名称
            String cuaddress = JSONUtil.getText(object, "address", "vr_cuaddress", "mpd_address");//客户地址
            String remark = JSONUtil.getText(object, "vr_title", "mpd_remark");//拜访目的,主题
            String cucontact = JSONUtil.getText(object, "vr_cucontact");//联系人
            String context = JSONUtil.getText(object, "vr_detail");//拜访内容
            String vr_recorddate = JSONUtil.getText(object, "vr_recorddate", "mpd_actdate", "mpd_arrivedate");
            String nichestep = JSONUtil.getText(object, "vr_nichestep");//商机阶段
            String cutype = JSONUtil.getText(object, "vr_class");//拜访类型
            String code = JSONUtil.getText(object, "vr_code");//单据编号
            mpd_id = JSONUtil.getInt(object, "mpd_id");
            saveAble = getIntent().getBooleanExtra("isMe", true) && (type != 2);
            content_et.setText(context);
            company_tv.setText(custname);
            company_add_tv.setText(cuaddress);
            remark_tv.setText(remark);
            SelectAimModel model = new SelectAimModel();
            model.setName(custname);
            model.setAddress(cuaddress);
            presenter.setClient(model);
            presenter.loadContact(code);
            date_tv.setText(TimeUtils.s_long_2_str(DateFormatUtil.str2Long(vr_recorddate, DateFormatUtil.YMD_HMS)));
            initView(cucontact);
        }
    }


    private void initView(String cucontact) {
        List<String> contactNames = new ArrayList<>();
        contactNames.add(cucontact);
        showContact(contactNames);
    }

    private void initEvent() {
        if (saveAble) {
            if (mpd_id <= 0) {//不是拜访报告
                findViewById(R.id.company_tv).setOnClickListener(this);
                findViewById(R.id.remark_tv).setOnClickListener(this);
                findViewById(R.id.date_tv).setOnClickListener(this);
            }
            findViewById(R.id.additem_tv).setOnClickListener(this);
            findViewById(R.id.submit_btn).setOnClickListener(this);
            contact_lv.setOnItemClickListener(this);
            findViewById(R.id.voice_search_iv).setOnClickListener(this);
        } else {
            findViewById(R.id.submit_btn).setVisibility(View.GONE);
            findViewById(R.id.additem_tv).setVisibility(View.GONE);
            content_et.setFocusable(false);
            content_et.setClickable(false);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AddVisitReportActivity.this.position = position;
        Intent intent = new Intent(ct, SelectRemarkActivity.class)
                .putExtra("isContact", true)
                .putExtra("contact", presenter.getContact())
                .putExtra("cuname", presenter.getCuName());
        startActivityForResult(intent, SELECT_CONTACT);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.additem_tv://添加联系人按钮
                adapter.getContactNames().add("");
                adapter.notifyDataSetChanged();
                break;
            case R.id.submit_btn://提交按钮
                if (canSubmit()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("remark", StringUtil.getTextRexHttp(remark_tv));
                    bundle.putString("detail", StringUtil.getTextRexHttp(content_et));
                    bundle.putInt("mpdId", mpd_id);
                    bundle.putString("recorddate", StringUtil.getTextRexHttp(date_tv));
                    presenter.submit(bundle);
                }
                break;
            case R.id.date_tv://选择日期
                showDateDialog();
                break;
            case R.id.company_tv://选择单位
                intent = new Intent(ct, SelectAimActivity.class).putExtra("type", 1);
                startActivityForResult(intent, SELECT_COMPANY);
                break;
            case R.id.remark_tv://选择目的
                intent = new Intent(ct, SelectRemarkActivity.class);
                startActivityForResult(intent, SELECT_REMARK);
                break;
            case R.id.voice_search_iv:
                RecognizerDialogUtil.showRecognizerDialog(ct, new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                        if (!StringUtil.isEmpty(text))
                            content_et.setText(content_et.getText().toString() + text);
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }
                });
                break;
        }
    }

    private boolean canSubmit() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastUtil.showToast(ct, getString(R.string.networks_out));
            return false;
        } else if (TextUtils.isEmpty(company_tv.getText())) {
            ToastUtil.showToast(ct, getString(R.string.visit_company) + getString(R.string.is_must_input));
            return false;
        } else if (TextUtils.isEmpty(company_add_tv.getText())) {
            ToastUtil.showToast(ct, getString(R.string.visit_address) + getString(R.string.is_must_input));
            return false;
        } else if (TextUtils.isEmpty(remark_tv.getText())) {
            ToastUtil.showToast(ct, getString(R.string.visit_aim) + getString(R.string.is_must_input));
            return false;
        } else if (!presenter.canSubmit(adapter.getContactNames())) {
            ToastUtil.showToast(ct, R.string.contect_is_much);
            return false;
        } else if (TextUtils.isEmpty(content_et.getText())) {
            ToastUtil.showToast(ct, getString(R.string.context_is_much));
            return false;
        }
        return true;
    }

    //显示时间选择器
    private void showDateDialog() {
        OASigninPicker picker = new OASigninPicker(this);
        picker.setRange(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                date_tv.setText(time);
            }
        });
        picker.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        switch (requestCode) {
            case SELECT_COMPANY:
                SelectAimModel entity = data.getParcelableExtra("data");
                PopupWindowHelper.create(this, getString(R.string.perfect_company_name), entity, new PopupWindowHelper.OnClickListener() {
                    @Override
                    public void result(SelectAimModel model) {
                        LogUtil.i("model=" + model.getName());
                        company_tv.setText(model.getName());
                        company_add_tv.setText(model.getAddress());
                        presenter.setClient(model);
                    }
                });
                break;
            case SELECT_REMARK:
                String message = data.getStringExtra("data");
                String remark = StringUtil.isEmpty(message) ? getResources().getString(R.string.maintain_customers) : message;
                remark_tv.setText(remark);
                break;
            case SELECT_CONTACT:
                message = data.getStringExtra("data");
                String contact = StringUtil.isEmpty(message) ? getResources().getString(R.string.maintain_customers) : message;
                List<String> contactNames = adapter.getContactNames();
                if (!ListUtils.isEmpty(contactNames) && contactNames.size() > this.position)
                    contactNames.set(this.position, contact);
                adapter.setContactNames(contactNames);
                break;
        }
    }

    @Override
    public void showContact(List<String> contactNames) {
        if (adapter == null) {
            adapter = new VisitAddContactAdapter(contactNames);
            contact_lv.setAdapter(adapter);
        } else {
            adapter.setContactNames(contactNames);
        }
    }

    private class VisitAddContactAdapter extends BaseAdapter {
        private List<String> contactNames;

        public VisitAddContactAdapter(List<String> contactNames) {
            this.contactNames = contactNames;
        }

        public List<String> getContactNames() {
            return contactNames;
        }

        public void setContactNames(List<String> contactNames) {
            this.contactNames = contactNames;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(contactNames);
        }

        @Override
        public Object getItem(int position) {
            return contactNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHodler hodler = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_visit_contact, parent, false);
                hodler = new ViewHodler();
                hodler.contact_tv = (TextView) convertView.findViewById(R.id.contact_tv);
                hodler.delete_tv = (TextView) convertView.findViewById(R.id.delete_tv);
                convertView.setTag(hodler);
            } else
                hodler = (ViewHodler) convertView.getTag();
            String nameShow = (contactNames == null || contactNames.size() < position || StringUtil.isEmpty(contactNames.get(position))) ? "" : contactNames.get(position);
            hodler.contact_tv.setText(nameShow);
            if (saveAble && position != 0) {
                hodler.delete_tv.setVisibility(View.VISIBLE);
                hodler.delete_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteContact(position);
                    }
                });
            } else
                hodler.delete_tv.setVisibility(View.GONE);

            return convertView;
        }

        public void deleteContact(final int position) {
            PopupWindowHelper.showAlart(AddVisitReportActivity.this,
                    getString(R.string.prompt_title), getString(R.string.sure_delete_content),
                    new PopupWindowHelper.OnSelectListener() {
                        @Override
                        public void select(boolean selectOk) {
                            contactNames.remove(position);
                            adapter = new VisitAddContactAdapter(contactNames);
                            contact_lv.setAdapter(adapter);
                        }
                    });
        }

        class ViewHodler {
            private TextView contact_tv, delete_tv;
        }
    }
}
