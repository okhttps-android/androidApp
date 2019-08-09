package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.base.OABaseActivity;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.utils.ToastUtil;
import com.core.widget.crouton.Crouton;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.OA.erp.model.FlightsModel;
import com.uas.appworks.OA.erp.presenter.AddFlihtsPresenter;
import com.uas.appworks.OA.erp.view.IAddFlihtsView;
import com.uas.appworks.R;

import java.util.ArrayList;


public class AddFlihtsActivity extends OABaseActivity implements IAddFlihtsView, View.OnClickListener {
    private final int TIME_TV = 0x11,
            DATE_TV = 0x12,
            DEPARTMENT_TV = 0x13,
            MUNBER_TV = 0x14,
            COLLISION_MUNBER_TV = 0x15,
            COLLISION_DEPARTMENT_TV = 0x16;

    private EditText rule_name_et;
    private TextView time_tv;
    private TextView date_tv;
    private LinearLayout calender_select_ll;
    private Button click_btn;
    private TextView department_tv;
    private TextView munber_tv;
    private TextView collision_department_tv;//冲突部门
    private TextView collision_munber_tv;//冲突员工
    private RelativeLayout department_rl;
    private RelativeLayout munber_rl;
    private AddFlihtsPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flihts);
        initView();
        presenter = new AddFlihtsPresenter(this);
        presenter.start(getIntent());
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (isB2b) {
            department_rl.setVisibility(View.GONE);
            munber_rl.setVisibility(View.GONE);
        }
        initEvent();
    }

    private void initView() {

        rule_name_et = (EditText) findViewById(R.id.rule_name_et);
        time_tv = (TextView) findViewById(R.id.time_tv);
        calender_select_ll = (LinearLayout) findViewById(R.id.calender_select_ll);
        date_tv = (TextView) findViewById(R.id.date_tv);
        department_rl = (RelativeLayout) findViewById(R.id.department_rl);
        department_tv = (TextView) findViewById(R.id.department_tv);
        collision_department_tv = (TextView) findViewById(R.id.collision_department_tv);
        munber_rl = (RelativeLayout) findViewById(R.id.munber_rl);
        munber_tv = (TextView) findViewById(R.id.munber_tv);
        collision_munber_tv = (TextView) findViewById(R.id.collision_munber_tv);
        click_btn = (Button) findViewById(R.id.click_btn);
    }

    private void initEvent() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (!isB2b) {
            findViewById(R.id.date_tv).setOnClickListener(this);
            findViewById(R.id.department_tv).setOnClickListener(this);
            findViewById(R.id.munber_tv).setOnClickListener(this);
            findViewById(R.id.calender_select_ll).setOnClickListener(this);
            collision_department_tv.setOnClickListener(this);
            collision_munber_tv.setOnClickListener(this);
        } else {
            rule_name_et.setFocusable(false);
            rule_name_et.setClickable(false);
        }
        findViewById(R.id.time_tv).setOnClickListener(this);
        findViewById(R.id.click_btn).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        SelectCollisionTurnBean bean = null;

        if (view.getId() == R.id.time_tv) {
            intent = new Intent("com.modular.appworks.FlihtsTimeActivity");
            presenter.putData2Intent(view.getId(), intent);
            startActivityForResult(intent, TIME_TV);
        } else if (view.getId() == R.id.date_tv) {
            intent = new Intent("com.modular.appworks.FlightsDateActivity");
            presenter.putData2Intent(view.getId(), intent);
            startActivityForResult(intent, DATE_TV);
        } else if (view.getId() == R.id.department_tv) {
            intent = new Intent("com.modular.common.SelectDepartmentActivity");
            intent.putExtra(OAConfig.STRING_DATA, presenter.getHrorgsEmCode());
            startActivityForResult(intent, DEPARTMENT_TV);
        } else if (view.getId() == R.id.munber_tv) {
            intent = new Intent("com.modular.main.SelectCollisionActivity");
            bean = new SelectCollisionTurnBean()
                    .setSelectCode(presenter.getEmployeeEmCode());
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, MUNBER_TV);
        } else if (view.getId() == R.id.click_btn) {
            if (isCanSubmit())
                presenter.submit(rule_name_et.getText().toString());
        } else if (view.getId() == R.id.collision_munber_tv) {
            if (ListUtils.isEmpty(mans)) return;
            intent = new Intent("com.modular.main.SelectCollisionActivity");
            bean = new SelectCollisionTurnBean()
                    .setSureText(getString(R.string.user_work))
                    .setSelectType(getString(R.string.member))
                    .setTitle(getString(R.string.conflict_personnel))
                    .setReBackSelect(false)
                    .setSelectCode(presenter.getEmployeeEmCode())
                    .setSelectList(mans);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, COLLISION_MUNBER_TV);
        } else if (view.getId() == R.id.collision_department_tv) {
            if (ListUtils.isEmpty(defaultirs)) return;
            intent = new Intent("com.modular.main.SelectCollisionActivity");
            bean = new SelectCollisionTurnBean()
                    .setSureText(getString(R.string.user_work))
                    .setSelectType(getString(R.string.department))
                    .setTitle(getString(R.string.conflict_department))
                    .setReBackSelect(false)
                    .setSelectCode(presenter.getEmployeeEmCode())
                    .setSelectList(defaultirs);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            presenter.putData2Intent(view.getId(), intent);
            startActivityForResult(intent, COLLISION_DEPARTMENT_TV);
        } else if (view.getId() == R.id.calender_select_ll) {
            startActivity(new Intent("com.modular.appworks.FlihtsDateSelectActivity"));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0x20 || data == null) return;
        switch (requestCode) {
            case TIME_TV:
                presenter.saveTime(data);
                break;
            case DATE_TV:
                presenter.saveDate(data);
                break;
            case DEPARTMENT_TV:
                presenter.saveHrorgs(data);
                break;
            case MUNBER_TV:
                presenter.saveEmployees(data);
                break;
            case COLLISION_MUNBER_TV:
                if (isClickAble)
                    presenter.saveCollisionEmployees(data);
                else
                    showToast(R.string.default_not_edit_department, R.color.load_warning);
                break;
            case COLLISION_DEPARTMENT_TV:
                if (isClickAble)
                    presenter.saveCollisionDepartment(data);
                else
                    showToast(R.string.default_not_edit_member, R.color.load_warning);
                break;
        }
    }


    @Override
    public void updateName(String name) {
        if (!StringUtil.isEmpty(name)) {
            rule_name_et.setText(name);
            Editable etext = rule_name_et.getText();
            Selection.setSelection(etext, etext.length());
        } else
            rule_name_et.setText("");
    }

    @Override
    public void updateTime(String time) {
        time_tv.setText(StringUtil.isEmpty(time) ? "" : time);
    }

    @Override
    public void updateDate(String date, boolean isUpdate) {
        if (isUpdate) {
            date_tv.setVisibility(View.GONE);
            calender_select_ll.setVisibility(View.VISIBLE);
            click_btn.setText(getString(R.string.common_update_button));
        }
        date_tv.setText(StringUtil.isEmpty(date) ? "" : date);
    }

    @Override
    public void updateDepartment(String department) {
        Log.i("gongpengming", "department=" + department);
        String showName = "";
        if (!StringUtil.isEmpty(department)) {
            showName = department.replaceAll("\'", "");
        }
        department_tv.setText(showName);
    }

    @Override
    public void updateMunber(String munber) {
        String showName = "";
        if (!StringUtil.isEmpty(munber)) {
            showName = munber.replaceAll("\'", "");
        }
        munber_tv.setText(showName + "");

    }

    @Override
    public void endActivity(FlightsModel model, boolean isUpdate) {
        Intent intent = new Intent();
        intent.putExtra("data", model);
        intent.putExtra("isUpdate", isUpdate);
        setResult(0x20, intent);
        finish();
    }

    private ArrayList<SelectEmUser> mans;

    @Override
    public void showCollisionMan(ArrayList<SelectEmUser> mans) {
        //TODO 处理不来，测试版本先隐藏
        this.mans = mans;
        if (ListUtils.isEmpty(mans)) {
            collision_munber_tv.setVisibility(View.GONE);
        } else {
            collision_munber_tv.setVisibility(View.VISIBLE);
            collision_munber_tv.setText(getString(R.string.conflict_personnel) + mans.size() + getString(R.string.a));
        }
    }

    private ArrayList<SelectEmUser> defaultirs;

    @Override
    public void showCollisionDefaultir(ArrayList<SelectEmUser> defaultirs) {
        //TODO 处理不来，测试版本先隐藏
        this.defaultirs = defaultirs;
        if (ListUtils.isEmpty(defaultirs)) {
            collision_department_tv.setVisibility(View.GONE);
        } else {
            collision_department_tv.setVisibility(View.VISIBLE);
            collision_department_tv.setText(getString(R.string.conflict_department) + defaultirs.size() + getString(R.string.a));
        }
    }

    boolean isClickAble = true;

    @Override
    public void setClickAble(boolean isClickAble) {
        rule_name_et.setClickable(isClickAble);
        rule_name_et.setFocusable(isClickAble);
        this.isClickAble = isClickAble;

    }

    @Override
    public void isB2b(boolean isB2b) {

    }

    public boolean isCanSubmit() {
        if (StringUtil.isEmpty(rule_name_et.getText().toString())) {
            ToastUtil.showToast(ct, R.string.work_name_not_be_null);
            return false;
        } else if (StringUtil.isEmpty(time_tv.getText().toString())) {
            Crouton.showToast(ct, R.string.not_null_work_time, R.color.load_warning);
            return false;
        }
        if (!getString(R.string.common_update_button).equals(click_btn.getText().toString())) {
            if (StringUtil.isEmpty(date_tv.getText().toString())) {
                Crouton.showToast(ct, R.string.not_null_work_day, R.color.load_warning);
                return false;
            }
        }
        if (!ListUtils.isEmpty(mans) || !ListUtils.isEmpty(defaultirs)) {
            showDeleteDialog();
            return false;
        }
        return true;
    }

    private void showDeleteDialog() {
        PopupWindowHelper.showAlart(AddFlihtsActivity.this,
                getString(R.string.common_dialog_title), getString(R.string.will_save_conflict),
                new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            presenter.submit(rule_name_et.getText().toString());
                        }
                    }
                });
    }
}
