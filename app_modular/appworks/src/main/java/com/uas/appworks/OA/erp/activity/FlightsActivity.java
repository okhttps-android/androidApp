package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.common.data.ListUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.base.OABaseActivity;
import com.core.widget.RecycleViewDivider;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.OA.erp.adapter.FlightsAdapter;
import com.uas.appworks.OA.erp.model.FlightsModel;
import com.uas.appworks.OA.erp.presenter.FlightsPresernter;
import com.uas.appworks.OA.erp.utils.MostLinearLayoutManager;
import com.uas.appworks.OA.erp.view.IFlightsView;
import com.uas.appworks.R;

import java.util.List;


public class FlightsActivity extends OABaseActivity implements IFlightsView, FlightsAdapter.OnItemClickListener {

    private FlightsPresernter presernter;
    private RecyclerView recyclerView;
    private FlightsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flights);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ct));
        RecycleViewDivider viewDivider = new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light));
        recyclerView.addItemDecoration(viewDivider);
        initView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!(ApiUtils.getApiModel() instanceof ApiPlatform))
            getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.add_item == item.getItemId()) {
            addMenuClicking(null);
        } else if (android.R.id.home == item.getItemId()) {
            endActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        endActivity();
        super.onBackPressed();
    }

    @Override
    public void showModel(List<FlightsModel> models) {
        if (adapter == null) {
            adapter = new FlightsAdapter(models);
            recyclerView.setLayoutManager(new MostLinearLayoutManager(ct));
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(this);
        } else {
            adapter.setModels(models);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void deleteModel(int position) {
        if (adapter == null || ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= position)
            return;
        adapter.getModels().remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, adapter.getModels().size());
    }


    private void addMenuClicking(FlightsModel model) {
        Intent intent = new Intent(ct, AddFlihtsActivity.class);
        intent.putExtra("data", model);
        intent.putExtra("isUpdate", model != null);
        startActivityForResult(intent, 0x21);
    }

    private void initView() {
        presernter = new FlightsPresernter(this);
        presernter.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && 0x21 == requestCode && 0x20 == resultCode) {
            presernter.start();
        }
    }

    private int clickPosition = 0;

    @Override
    public void click(FlightsModel model, int position, boolean itemView) {
        if (itemView) {
//            if (model.getType() == 2) return;
            clickPosition = position;
            addMenuClicking(model);
        } else {
            if (ListUtils.isEmpty(adapter.getModels()) || adapter.getModels().size() <= position)
                return;
//            adapter.getModels().remove(position);
//            adapter.notifyItemRemoved(position);
//            adapter.notifyItemRangeChanged(position, adapter.getModels().size());
            showDeleteDialog(position);
        }
    }

    private void showDeleteDialog(final int position) {
        PopupWindowHelper.showAlart(FlightsActivity.this, getString(R.string.common_dialog_title)
                , getString(R.string.delete_back_default_work), new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            presernter.deleteWork(adapter.getModels(), position);
                        }
                    }
                });
    }

    private void endActivity() {
        Intent intent = new Intent();
        setResult(0x20, intent);
    }

}
