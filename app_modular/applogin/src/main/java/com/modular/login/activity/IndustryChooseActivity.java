package com.modular.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.core.widget.RecycleViewDivider;
import com.core.widget.view.adapter.SecondaryListAdapter;
import com.modular.login.R;
import com.modular.login.adapter.IndustryAdapter;
import com.modular.login.model.ProfessionBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/26.
 */

public class IndustryChooseActivity extends BaseActivity {
    private List<ProfessionBean> mProfessionList;
    private RecyclerView mRecyclerView;
    private IndustryAdapter mIndustryAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecycleViewDivider mRecycleViewDivider;
    private List<SecondaryListAdapter.SecondaryListBean<String, String>> mSecondaryListBeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_industry_choose);
        setTitle("行业选择");

        init();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        mIndustryAdapter.setOnSubItemClickListener(new IndustryAdapter.OnSubItemClickListener() {
            @Override
            public void onSubItemClick(String message) {
                Intent intent = new Intent();
                intent.putExtra("industry", message);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void init() {
        mRecycleViewDivider = new RecycleViewDivider(this, LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) findViewById(R.id.industry_choose_rv);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(mRecycleViewDivider);

        mIndustryAdapter = new IndustryAdapter(this);
        initProfession();
        mSecondaryListBeen = new ArrayList<>();
    }

    private void initDatas() {
        for (int i = 0; i < mProfessionList.size(); i++) {
            ProfessionBean professionBean = mProfessionList.get(i);
            List<String> strings = new ArrayList<>();
            for (int j = 0; j < professionBean.getProfessionSecondTitles().size(); j++) {
                strings.add(professionBean.getProfessionSecondTitles().get(j).getProfessionSecondTitle());
            }
            SecondaryListAdapter.SecondaryListBean<String, String> secondaryListBean
                    = new SecondaryListAdapter.SecondaryListBean<>(professionBean.getProfessionFirstTitle(),
                    strings);

            mSecondaryListBeen.add(secondaryListBean);
        }
        mIndustryAdapter.setDatas(mSecondaryListBeen);
        mRecyclerView.setAdapter(mIndustryAdapter);
    }

    private void initProfession() {
        mProfessionList = new ArrayList<>();
        String profession = CommonUtil.getAssetsJson(this, "profession.json");
        try {
            JSONObject professionObject = new JSONObject(profession);
            Iterator<String> iterator = professionObject.keys();
            while (iterator.hasNext()) {
                ProfessionBean professionBean = new ProfessionBean();
                String professionFirstTitle = iterator.next().toString();
                professionBean.setProfessionFirstTitle(professionFirstTitle);

                List<ProfessionBean.ProfessionSecondBean> professionSecondList = new ArrayList<>();

                Object second = professionObject.opt(professionFirstTitle);
                if (second instanceof JSONArray) {
                    JSONArray secondArray = (JSONArray) second;
                    if (secondArray != null) {
                        for (int i = 0; i < secondArray.length(); i++) {
                            ProfessionBean.ProfessionSecondBean professionSecond = new ProfessionBean.ProfessionSecondBean();
                            String secondTitle = secondArray.optString(i);
                            professionSecond.setProfessionSecondTitle(secondTitle);

                            professionSecondList.add(professionSecond);
                        }
                    }
                } else if (second instanceof JSONObject) {
                    JSONObject secondObject = (JSONObject) second;
                    Iterator<String> secondKeys = secondObject.keys();
                    while (secondKeys.hasNext()) {
                        ProfessionBean.ProfessionSecondBean professionSecond = new ProfessionBean.ProfessionSecondBean();
                        String secondTitle = secondKeys.next().toString();
                        professionSecond.setProfessionSecondTitle(secondTitle);

                        List<String> thirdTitles = new ArrayList<>();
                        Object third = secondObject.opt(secondTitle);
                        if (third instanceof String) {
                            thirdTitles.add((String) third);
                        } else if (third instanceof JSONArray) {
                            JSONArray thirdArray = (JSONArray) third;
                            for (int i = 0; i < thirdArray.length(); i++) {
                                String thirdTitle = thirdArray.optString(i);
                                thirdTitles.add(thirdTitle);
                            }
                        }

                        professionSecond.setProfessionThirdTitles(thirdTitles);

                        professionSecondList.add(professionSecond);
                    }
                }

                professionBean.setProfessionSecondTitles(professionSecondList);

                mProfessionList.add(professionBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
