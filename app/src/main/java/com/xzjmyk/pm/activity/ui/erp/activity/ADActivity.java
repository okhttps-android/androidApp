package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.common.LogUtil;
import com.common.preferences.PreferenceUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.modular.login.activity.LoginActivity;
import com.core.base.BaseActivity;


/**
 * 广告页
 */
public class ADActivity extends BaseActivity {
    private final String IS_FIRST = "IS_FIRST";

    private int type;
    private final int[] IDS = {R.drawable.index_a, R.drawable.index_b, R.drawable.index_c, R.drawable.index_d};
    @ViewInject(R.id.ad_vp)
    private ViewPager ad_vp;
    @ViewInject(R.id.rg)
    private RadioGroup rg;
    @ViewInject(R.id.rb1)
    private RadioButton rb1;
    @ViewInject(R.id.rb2)
    private RadioButton rb2;
    @ViewInject(R.id.rb3)
    private RadioButton rb3;
    @ViewInject(R.id.rb4)
    private RadioButton rb4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        ViewUtils.inject(this);
        type = getIntent().getIntExtra("type", 0);
        getSupportActionBar().hide();
        initView();

    }

    private void initView() {
        ad_vp.setAdapter(new MViewPagerAdapter());
        ad_vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == (IDS.length - 1)) {

                }
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        rb1.setChecked(true);
//                        btn.setVisibility(View.GONE);
                        break;
                    case 1:
                        rb2.setChecked(true);
//                        btn.setVisibility(View.GONE);
                        break;
                    case 2:
                        rb3.setChecked(true);
//                        btn.setVisibility(View.GONE);
                        break;
                    case 3:
                        rb4.setChecked(true);
//                        btn.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int item = 0;
                switch (checkedId) {
                    case R.id.rb1:
                        item = 0;
                        break;
                    case R.id.rb2:
                        item = 1;
                        break;
                    case R.id.rb3:
                        item = 2;
                        break;
                    case R.id.rb4:
                        item = 3;
                        break;
                }
                ad_vp.setCurrentItem(item);
            }
        });
    }

    @Override
    public void onBackPressed() {
        start2Next();
        super.onBackPressed();
    }

    private void start2Next() {
        if (type == 0)
            startActivity(new Intent(this, LoginActivity.class));
        else
            startActivity(new Intent(this, MainActivity.class));
        PreferenceUtils.putBoolean(IS_FIRST, false);
        finish();
    }

    private class MViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return IDS.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView image = new ImageView(ADActivity.this);
            image.setImageResource(IDS[position]);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (position == 3) {
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start2Next();
                    }
                });
            }
            container.addView(image);
            return image;
        }
    }
}
