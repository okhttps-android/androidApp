package com.uas.appme.other.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.core.base.BaseActivity;
import com.uas.appme.R;

/**
  * @desc:更新性别
  * @author：Arison on 2016/9/27
  */
public class UpdateSexActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mMaleLinearLayout,mFeMaleLinearLayout;
    private CheckBox mMaleCheckBox,mFemaleCheckBox;
    private String mSex;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_sex);
        setTitle(getString(R.string.user_sex));
        initViews();

        intent = getIntent();
        mSex = intent.getStringExtra("sex");
        if (getString(R.string.user_body).equals(mSex)){
            mMaleCheckBox.setChecked(true);
            mFemaleCheckBox.setChecked(false);
        }else if (getString(R.string.user_girl).equals(mSex)){
            mMaleCheckBox.setChecked(false);
            mFemaleCheckBox.setChecked(true);
        }

        initEvents();

    }

    private void initEvents() {
        mMaleCheckBox.setOnClickListener(this);
        mFemaleCheckBox.setOnClickListener(this);
    }

    private void initViews() {
        mMaleLinearLayout = (LinearLayout) findViewById(R.id.update_sex_male_ll);
        mFeMaleLinearLayout = (LinearLayout) findViewById(R.id.update_sex_female_ll);
        mMaleCheckBox = (CheckBox) findViewById(R.id.update_sex_male_cb);
        mFemaleCheckBox = (CheckBox) findViewById(R.id.update_sex_female_cb);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() ==  R.id.update_sex_male_cb){
            mMaleCheckBox.setChecked(true);
            mFemaleCheckBox.setChecked(false);
            intent.putExtra("newsex", getString(R.string.user_body));
        }else if (v.getId() == R.id.update_sex_female_cb){
            mMaleCheckBox.setChecked(false);
            mFemaleCheckBox.setChecked(true);
            intent.putExtra("newsex", getString(R.string.user_girl));
        }
        setResult(22, intent);
    }
}
