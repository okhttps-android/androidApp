package com.modular.login.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.hmac.Md5Util;
import com.common.preferences.PreferenceUtils;
import com.core.app.Constants;
import com.core.base.SupportToolBarActivity;
import com.core.net.http.ViewUtil;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.widget.ClearEditText;
import com.core.widget.StrengthView;
import com.modular.login.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 个人用户注册
 */
public class PersonalRegActivity extends SupportToolBarActivity {
    public static final String REGEXP_MOBILE_CONTINENT = "1[0-9]{10}";

    private ClearEditText mUsernameEditText, mPhoneEditText, mPasswordEditText, mPasswordConfirmEditText;
    private TextView mConfirmButton, mSuccessButton;
    private LinearLayout mPasswordLinearLayout, mPasswordConfirmLinearLayout, mSuccessLinearLayout;
    private StrengthView mStrengthView;

    private int male_log = 1;
    private long bir_Timestamp = 1489141231;
    private String md5_password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_reg_activity);
         setTitle(getString(R.string.person_register));

        initView();
    }

    private void initView() {
        mUsernameEditText = findViewById(R.id.personal_reg_username_et);
        mPhoneEditText = findViewById(R.id.personal_reg_phone_et);
        mPasswordEditText = findViewById(R.id.personal_reg_password_et);
        mPasswordConfirmEditText = findViewById(R.id.personal_reg_password_confirm_et);
        mConfirmButton = findViewById(R.id.personal_reg_confirm_tv);
        mPasswordLinearLayout = findViewById(R.id.personal_reg_password_ll);
        mPasswordLinearLayout.setEnabled(false);
        mPasswordConfirmLinearLayout = findViewById(R.id.personal_reg_password_confirm_ll);
        mPasswordConfirmLinearLayout.setEnabled(false);
        mStrengthView = findViewById(R.id.personal_reg_strength_sv);
        mSuccessLinearLayout = findViewById(R.id.personal_reg_success_ll);
        mSuccessButton = findViewById(R.id.personal_reg_success_btn);

        mPhoneEditText.addTextChangedListener(new EnableTextWatcher());
        mUsernameEditText.addTextChangedListener(new EnableTextWatcher());
        mPasswordConfirmEditText.addTextChangedListener(new EnableTextWatcher());
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                StrengthView.Level passwordLevel = checkPasswordLevel(editable.toString());
                if (passwordLevel == StrengthView.Level.STRENGTH_NONE) {
                    mStrengthView.setVisibility(View.GONE);
                } else {
                    mStrengthView.setVisibility(View.VISIBLE);
                    mStrengthView.setLevel(passwordLevel);
                    if (passwordLevel == StrengthView.Level.STRENGTH_WEAK) {
                        mConfirmButton.setEnabled(false);
                    } else {
                        isConfirmEnable();
                    }
                }
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = mPhoneEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(phone) && phone.matches(REGEXP_MOBILE_CONTINENT)) {
                    String password1 = mPasswordEditText.getText().toString();
                    String password2 = mPasswordConfirmEditText.getText().toString();
                    if (password1 != null && password1.equals(password2)) {
                        registerJudge();
                    } else {
                        showToast("两次输入的密码不一致");
                    }
                } else {
                    if (TextUtils.isEmpty(phone)) {
                        showToast("手机号为空");
                    } else {
                        showToast("请填写正确的手机号");
                    }
                }
            }
        });

        mSuccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(PersonalRegActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    class EnableTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            isConfirmEnable();
        }
    }

    private void isConfirmEnable() {
        String phone = mPhoneEditText.getText().toString().trim();
        String username = mUsernameEditText.getText().toString().trim();
        String password1 = mPasswordEditText.getText().toString();
        String password2 = mPasswordConfirmEditText.getText().toString();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(username)) {
            mPasswordLinearLayout.setEnabled(false);
            mPasswordEditText.setEnabled(false);
            mPasswordConfirmLinearLayout.setEnabled(false);
            mPasswordConfirmEditText.setEnabled(false);
        } else {
            mPasswordLinearLayout.setEnabled(true);
            mPasswordEditText.setEnabled(true);
            mPasswordConfirmLinearLayout.setEnabled(true);
            mPasswordConfirmEditText.setEnabled(true);
        }

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2)) {
            mConfirmButton.setEnabled(false);
        } else {
            mConfirmButton.setEnabled(true);
        }
    }

    private void registerJudge() {
        if (!NetUtils.isNetWorkConnected(this)) {
            ToastMessage(getString(R.string.common_notlinknet));
        } else {
            md5_password = Md5Util.toMD5(mPasswordEditText.getText().toString());
            doRegiter();
        }
    }

    private void doRegiter() {
        progressDialog.dismiss();
        String url = Constants.IM_BASE_URL() + "user/appRegister";
        Map<String, Object> params = new HashMap<>();
        params.put("telephone", mPhoneEditText.getText().toString());
        params.put("password", md5_password);
        params.put("userType", 0);
        params.put("companyId", 0);
        params.put("nickname", mUsernameEditText.getText().toString());
        params.put("description", "");
        params.put("sex", male_log);
        params.put("birthday", bir_Timestamp);
        LogUtil.prinlnLongMsg("0x01params", "url=" + url + JSON.toJSONString(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, rHandler, headers, 0x01, null, null, "post");
    }

//    private void showSelectBirthdayDialog() {
//        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.YEAR_MONTH_DAY);
//        picker.setRange(1960, CalendarUtil.getYear());
//        picker.setOnThirdLevelPickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
//            @Override
//            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
//                Log.i(TAG, "onDateTimePicked:" + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
//                GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
//
//                long currentTime = System.currentTimeMillis() / 1000;
//                long birthdayTime = calendar.getTime().getTime() / 1000;
//                if (birthdayTime > currentTime) {
//                    ToastUtil.showToast(mContext, "亲!您的出生日期已经超过现在了哦!");
//                } else {
//                    birthday_et.setText(year + "-" + month + "-" + day);
//                    bir_Timestamp = birthdayTime;
//                }
//            }
//        });
//        picker.show();
//
//    }

    // // 1是男，0是女，2是全部
//    private void showSelectSexDialog() {
//        String[] sexs = new String[]{getString(R.string.user_body), getString(R.string.user_girl)};
//        int checkItem = 0;
//        new AlertDialog.Builder(this).setTitle(getString(R.string.select_sex_title))
//                .setSingleChoiceItems(sexs, checkItem, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (which == 0) {
//                            male_et.setText(R.string.user_body);
//                            male_log = 1;
//                        } else {
//                            male_et.setText(R.string.user_girl);
//                            male_log = 0;
//                        }
//                        dialog.dismiss();
//                    }
//                }).setCancelable(true).create().show();
//    }


    private Handler rHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String result = msg.getData().getString("result");
            Log.i("fanglh", result);
            switch (msg.what) {
                case 0x01:
                    if (!StringUtil.isEmpty(result)) {
                        if (result.contains("resultCode") && JSON.parseObject(result).getInteger("resultCode") == 1) {
                            showToast("注册成功");
                            PreferenceUtils.putString(PersonalRegActivity.this, LoginActivity.PASS_WORDS, mPasswordEditText.getText().toString().trim());
                            CommonUtil.setSharedPreferences(PersonalRegActivity.this, "user_phone", mPhoneEditText.getText().toString().trim());
                            mSuccessLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            showToast(JSON.parseObject(result).getString("resultMsg"));
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (JSONUtil.validate(result)) {
                        showToast(JSON.parseObject(result).getString("exceptionInfo"));
                    } else {
                        showToast(result);
                    }
                    progressDialog.dismiss();
                    mConfirmButton.setEnabled(true);
                    break;
            }
        }
    };

    public static StrengthView.Level checkPasswordLevel(String password) {
        String strongRegex = "^(?=.{8,20})(((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]))|((?=.*[0-9])((?=.*[a-zA-Z]))(?=.*[^a-zA-Z0-9]))).*$";
        String mediumRegex = "^(?=.{8,20})(((?=.*[0-9])(?=.*[a-z]))|((?=.*[0-9])(?=.*[A-Z]))).*$";
        if (TextUtils.isEmpty(password)) {
            return StrengthView.Level.STRENGTH_NONE;
        } else if (password.matches(strongRegex)) {
            return StrengthView.Level.STRENGTH_STRONG;
        } else if (password.matches(mediumRegex)) {
            return StrengthView.Level.STRENGTH_MEDIUM;
        } else {
            return StrengthView.Level.STRENGTH_WEAK;
        }
    }
}
