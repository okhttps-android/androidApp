package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.baidu.mapapi.map.MapView;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.InputMethodUtil;
import com.core.base.OABaseActivity;
import com.core.utils.BaiduMapUtil;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.model.SelectAimModel;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.modular.apputils.widget.RecycleViewDivider;
import com.uas.applocation.UasLocationHelper;
import com.uas.appworks.CRM.erp.adapter.SelectAimAdapter;
import com.uas.appworks.CRM.erp.imp.ISelectAim;
import com.uas.appworks.CRM.erp.presenter.SelectAimPresenter;
import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;
import java.util.List;

/*选择拜访目的地*/
public class SelectAimActivity extends OABaseActivity implements ISelectAim {

    //    @ViewInject(R.id.search_edit)
    @ViewInject(R.id.voiceSearchView)
    private VoiceSearchView voiceSearchView;
    @ViewInject(R.id.mapView)
    private MapView mapView;
    @ViewInject(R.id.listview)
    private RecyclerView listview;
    private SelectAimAdapter adapter;
    private SelectAimPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_aim);
        ViewUtils.inject(this);
        initView();
        initEvent();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        int type = 0;
        String search = "";
        if (intent != null) {
            type = intent.getIntExtra("type", 0);
            String title = intent.getStringExtra("title");
            if (!StringUtil.isEmpty(title))
                setTitle(title);
            search = intent.getStringExtra("search");
        }
        presenter = new SelectAimPresenter(this);
        presenter.start(type);

        if (!StringUtil.isEmpty(search)) {
            voiceSearchView.setText(search);
        }
    }

    private void initEvent() {
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                input = input.replaceAll(" ", "").replaceAll("\n", "");
                presenter.seachByKey(input);
            }
        });
//        search_edit.addTextChangedListener(new EditChangeListener() {
//            @Override
//            public void afterTextChanged(Editable editable) {
//                String input = editable.toString();
//                input = input.replaceAll(" ", "").replaceAll("\n", "");
//                if (lastInput.equals(input)) return;
//                lastInput = input;
//                presenter.seachByKey(input);
//            }
//        });
        adapter.setOnitemClickListener(new SelectAimAdapter.OnitemClickListener() {
            @Override
            public void click(SelectAimModel model) {
                if (model.getType() == 1) return;
                Intent intent = new Intent();
                intent.putExtra("data", model);
                setResult(0x20, intent);
                finish();
            }
        });

        listview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    InputMethodUtil.hideInput(ct, voiceSearchView.getSearch_edit());
                }
            }

        });
    }

    private void initView() {
        setTitle(R.string.activity_select_aims);
        BaiduMapUtil.getInstence().setMapViewPoint(mapView, UasLocationHelper.getInstance().getUASLocation().getLocation());
        listview.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewDivider viewDivider = new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light));
        adapter = new SelectAimAdapter();
        listview.addItemDecoration(viewDivider);
        listview.setAdapter(adapter);
    }


    //唯一
    @Override
    public synchronized void showModel(List<SelectAimModel> models) {
        if (!TextUtils.isEmpty(voiceSearchView.getSearch_edit().getText())) {
            mapView.setVisibility(View.GONE);
        } else mapView.setVisibility(View.VISIBLE);
        if (ListUtils.isEmpty(models)) {
            models = new ArrayList<>();
            SelectAimModel model = new SelectAimModel();
            model.setType(1);
            models.add(model);
        } else if (models.size() > 1) {
            for (int i = 0; i < models.size(); i++) {
                if (models.get(i).getType() == 1) {
                    models.remove(i);
                    i--;
                }
            }
        }
        adapter.setModels(models);
        adapter.notifyDataSetChanged();
    }
}
